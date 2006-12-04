//
// Copyright (C) 2006 Media Saturn Systemzentrale. All rights reserved.
//
// $Project: Inventory$
// $Revision:$
// $Date:$
// $Log[10]$
//

package org.jcoderz.commons.taskdefs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.caucho.hessian.client.HessianProxyFactory;
import com.luntsys.luntbuild.facades.BuildParams;
import com.luntsys.luntbuild.facades.Constants;
import com.luntsys.luntbuild.facades.ILuntbuild;
import com.luntsys.luntbuild.facades.lb12.BuildFacade;
import com.luntsys.luntbuild.facades.lb12.ScheduleFacade;

/**
 *
 */
public class LuntBuildTask
  extends Task
{
  /** Schedule start policy: allows multiple schedules to be running simultaneously. */
  public static final String START_MULTIPLE = "startMultiple";
  /** Schedule start policy: skips execution if schedule is currently running. */
  public static final String SKIP_IF_RUNNING = "skipIfRunning";
  /** Schedule start policy: fails this task if schedule is currently running. */
  public static final String FAIL_IF_RUNNING = "failIfRunning";
  
  private static final List POLICY_LIST = new ArrayList();
  static
  {
    POLICY_LIST.add(START_MULTIPLE);
    POLICY_LIST.add(SKIP_IF_RUNNING);
    POLICY_LIST.add(FAIL_IF_RUNNING);
  }
  
  /** Wait time between kicking off schedule and start polling status
   *  and between schedule termination and log retrieval. */
  private static final int WAIT_PERIOD = 5000;
  /** Interval to poll build server. */
  private static final int POLL_INTERVAL = 2000;
  /** Buffer size to read from HTTP stream when retrieving artifacts. */
  private static final int BUFFER_SIZE = 512;

  private String mLuntUrl;
  private String mUserName;
  private String mPassword;
  private String mProjectName;
  private String mScheduleName;
  private String mStartPolicy = FAIL_IF_RUNNING;
  private boolean mWaitForSchedule = true;
  private String mToDir;
  private List mArtifacts = new ArrayList();

  private ILuntbuild mLuntServer;
  
  /**
   * @param luntUrl The luntUrl to set.
   */
  public void setLuntUrl(String luntUrl)
  {
    mLuntUrl = luntUrl;
  }

  /**
   * @param userName The userName to set.
   */
  public void setUserName(String userName)
  {
    mUserName = userName;
  }
  
  /**
   * @param password The password to set.
   */
  public void setPassword(String password)
  {
    mPassword = password;
  }

  /**
   * @param projectName The projectName to set.
   */
  public void setProjectName(String projectName)
  {
    mProjectName = projectName;
  }
  
  /**
   * @param scheduleName The scheduleName to set.
   */
  public void setScheduleName(String scheduleName)
  {
    mScheduleName = scheduleName;
  }
  
  /**
   * @param startPolicy The startPolicy to set.
   */
  public void setStartPolicy(String startPolicy)
  {
    if (!POLICY_LIST.contains(startPolicy))
    {
      throw new BuildException("Invalid start policy " + startPolicy + ", must be one of " + POLICY_LIST);
    }
    mStartPolicy = startPolicy;
  }
  
  /**
   * @param waitForSchedule The waitForSchedule to set.
   */
  public void setWaitForSchedule(boolean waitForSchedule)
  {
    mWaitForSchedule = waitForSchedule;
  }

  /**
   * @param toDir The toDir to set.
   */
  public void setToDir(String toDir)
  {
    mToDir = toDir;
  }

  /**
   * Adds an artifact for retrieval.
   * @param artifact
   */
  public void addArtifact(Artifact artifact)
  {
    mArtifacts.add(artifact);
  }
  
  public void execute() throws BuildException
  {
    checkParameters();
    try
    {
      
      final ScheduleFacade schedule = getSchedule();
      final boolean startSchedule;
      if (schedule.getStatus() == Constants.SCHEDULE_STATUS_RUNNING)
      {
        if (mStartPolicy.equals(START_MULTIPLE))
        {
          startSchedule = true;
        }
        else if (mStartPolicy.equals(SKIP_IF_RUNNING))
        {
          startSchedule = false; 
        }
        else
        {
          throw new BuildException("Can't start build because schedule is already running");
        }
      }
      else
      {
        startSchedule = true;
      }
      
      if (startSchedule)
      {
        startSchedule();
      }
      
    }
    catch (BuildException x)
    {
      throw x;
    }
    catch (Exception x)
    {
      throw new BuildException(x);
    }
  }

  private void startSchedule() throws InterruptedException, IOException
  {
    log("Starting build for " + mProjectName + "/" + mScheduleName + " on server " + mLuntUrl, Project.MSG_INFO);
    getLuntServer().triggerBuild(mProjectName, mScheduleName, getBuildParams());

    if (mWaitForSchedule)
    {
      waitForSchedule();
    }
  }

  private void waitForSchedule() throws InterruptedException, IOException
  {
    Thread.sleep(WAIT_PERIOD);
    log("Waiting for build " + getLuntServer().getLastBuild(mProjectName, mScheduleName).getVersion() + " to finish");
 
    while (getSchedule().getStatus() == Constants.SCHEDULE_STATUS_RUNNING)
    {
      log("Schedule running", Project.MSG_VERBOSE);
      Thread.sleep(POLL_INTERVAL);
    }
    final int termStatus = getSchedule().getStatus();
    switch (termStatus)
    {
      case Constants.SCHEDULE_STATUS_SUCCESS:
        log("LuntBuild schedule " + mProjectName + "/" + mScheduleName + " succeeded");
        dumpLogFile();
        retrieveArtifacts();
        break;
      case Constants.SCHEDULE_STATUS_FAILED:
        log("LuntBuild schedule " + mProjectName + "/" + mScheduleName + " FAILED");
        dumpLogFile();
        throw new BuildException("LuntBuild schedule " + mProjectName + "/" + mScheduleName + " FAILED");
      default:
        throw new BuildException("Unexpected status for schedule " + mProjectName + "/" + mScheduleName + ": " + termStatus);
    }
  }

  private ScheduleFacade getSchedule() throws MalformedURLException
  {
    return getLuntServer().getScheduleByName(mProjectName, mScheduleName);
  }

  /**
   * @throws IOException 
   * 
   */
  private void retrieveArtifacts() throws IOException
  {
    final BuildFacade currentBuild = getLuntServer().getLastBuild(mProjectName, mScheduleName);
    final String buildLogUrl = currentBuild.getBuildLogUrl();
    final String path = buildLogUrl.substring(0, buildLogUrl.lastIndexOf('/'));
    final String artifactsBaseUrl = path + "/artifacts/";
    log("Artifacts base URL: " + artifactsBaseUrl, Project.MSG_VERBOSE);
    HttpURLConnection.setFollowRedirects(true);
    
    for (final Iterator it = mArtifacts.iterator(); it.hasNext(); )
    {
      final String artifactName = ((Artifact)it.next()).getName();
      final File outputFile = new File(new File(mToDir), artifactName);
      if (outputFile.exists())
      {
        throw new BuildException("Output file " + outputFile + " already exists");
      }
      final String artifactUrl = artifactsBaseUrl + artifactName;
      log("Retrieving artifact " + artifactName);
      log("Retrieving from URL: " + artifactUrl, Project.MSG_VERBOSE);
      log("Writing to file: " + mToDir + File.separator + outputFile, Project.MSG_VERBOSE);
      final HttpURLConnection con = (HttpURLConnection)new URL(artifactUrl).openConnection();
      con.setDoOutput(true);
      con.addRequestProperty("Keep-alive", "false");
      
      con.connect();
      if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
      {
        throw new BuildException("Failed while retrieving artifact " + artifactUrl + ": " + con.getResponseMessage());
      }
      
      writeArtifactToFile(outputFile, con);
    }
  }

  private void writeArtifactToFile(final File outputFile, final HttpURLConnection con) throws IOException, FileNotFoundException
  {
    int read = 0;
    final byte[] buf = new byte[BUFFER_SIZE];
    InputStream artifactInput = null;
    OutputStream artifactOutput = null;
    try
    {
      artifactInput = con.getInputStream();
      artifactOutput = new FileOutputStream(outputFile);
      while ((read = artifactInput.read(buf)) > 0)
      {
        artifactOutput.write(buf, 0, read);
      }
    }
    finally
    {
      close(artifactInput);
      close(artifactOutput);
    }
  }

  private void close(InputStream inputStream)
  {
    if (inputStream != null)
    {
      try
      {
        inputStream.close();
      }
      catch (IOException x)
      {
        log("Failed to close java.io.InputStream: " + x.getMessage(), Project.MSG_WARN);
      }
    }
  }

  private void close(OutputStream outputStream)
  {
    if (outputStream != null)
    {
      try
      {
        outputStream.close();
      }
      catch (IOException x)
      {
        log("Failed to close java.io.OutputStream: " + x.getMessage(), Project.MSG_WARN);
      }
    }
  }

  /**
   * @throws InterruptedException 
   * @throws IOException 
   * 
   */
  private void dumpLogFile() throws InterruptedException, IOException
  {
    Thread.sleep(WAIT_PERIOD);
    final BuildFacade currentBuild = getLuntServer().getLastBuild(mProjectName, mScheduleName);

    final String buildLogUrlHtml = currentBuild.getBuildLogUrl();
    log("Build log URL (HTML format): " + buildLogUrlHtml, Project.MSG_VERBOSE);
    final String buildLogUrlTxt = buildLogUrlHtml.substring(0, buildLogUrlHtml.lastIndexOf(".html")) + ".txt";
    log("Build log URL (Text format): " + buildLogUrlTxt, Project.MSG_VERBOSE);

    final URL buildLog = new URL(buildLogUrlTxt);
    final HttpURLConnection con = (HttpURLConnection)buildLog.openConnection();
    log("Got HTTP code " + con.getResponseCode(), Project.MSG_VERBOSE);

    final InputStream is = con.getInputStream();
    int read = 0;
    final byte[] buf = new byte[256];
    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
    while ((read = is.read(buf)) > 0)
    {
      bout.write(buf, 0, read);
    }
    final String buildLogData = new String(bout.toByteArray());
    log("===== START Build log =====", Project.MSG_INFO);
    log(buildLogData, Project.MSG_INFO);
    log("===== END Build log =====", Project.MSG_INFO);
  }

  private BuildParams getBuildParams()
  {
    final BuildParams params = new BuildParams();
    
    params.setBuildNecessaryCondition("always");
    params.setBuildType(Constants.BUILD_TYPE_CLEAN);
    params.setLabelStrategy(Constants.LABEL_NONE);
    params.setNotifyStrategy(Constants.NOTIFY_NONE);
    params.setPostbuildStrategy(Constants.POSTBUILD_NONE);
    // params.setScheduleId()
    params.setTriggerDependencyStrategy(Constants.TRIGGER_NONE_DEPENDENT_SCHEDULES);
    
    return params;
  }
  
  /**
   * @param luntUrl
   */
  private void checkNotNull(Object obj, String name) 
  {
    if (obj == null)
    {
      throw new BuildException("Parameter " + name + " missing");
    }
  }

  /**
   * 
   */
  private void checkParameters()
  {
    checkNotNull(mLuntUrl, "luntUrl");
    checkNotNull(mUserName, "userName");
    checkNotNull(mPassword, "password");
    checkNotNull(mProjectName, "projectName");
    checkNotNull(mScheduleName, "scheduleName");
    if (mArtifacts.size() > 0)
    {
      if (mToDir == null)
      {
        throw new BuildException("'toDir' must be set if artifacts are set");
      }
      else
      {
        AntTaskUtil.ensureDirectory(new File(mToDir));
      }
      if (!mWaitForSchedule)
      {
        throw new BuildException("Can't retrieve artifacts when waitForBuild == false");
      }
    }
  }

  private ILuntbuild getLuntServer() throws MalformedURLException
  {
    if (mLuntServer == null)
    {
      HessianProxyFactory factory = new HessianProxyFactory();
      factory.setUser(mUserName);
      factory.setPassword(mPassword);
      mLuntServer = (ILuntbuild)factory.create(ILuntbuild.class, mLuntUrl);
    }
    return mLuntServer;
  }
  
  public static final class Artifact
  {
    private String mName;
    
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
      mName = name;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
      return mName;
    }
  }

  public static void main(String[] args)
  {
    final Project dummy = new Project();
    DefaultLogger logger = new DefaultLogger();

    logger.setMessageOutputLevel(Project.MSG_INFO);
    logger.setOutputPrintStream(System.out);
    logger.setErrorPrintStream(System.err);
    dummy.addBuildListener(logger);
    
    final LuntBuildTask lbr = new LuntBuildTask();
    lbr.setProject(dummy);
    lbr.setProjectName("test");
    lbr.setScheduleName("on-demand");
    lbr.setLuntUrl("http://dev130wks0007:8080/luntbuild/app.do?service=hessian");
    lbr.setUserName("luntbuild");
    lbr.setPassword("geheim42");

    final Artifact a1 = new Artifact();
    a1.setName("artifact1.txt");
    lbr.addArtifact(a1);
    final Artifact a2 = new Artifact();
    a2.setName("artifact2.txt");
    lbr.addArtifact(a2);

    lbr.setToDir("C:\\temp");
    lbr.execute();
  }
}
