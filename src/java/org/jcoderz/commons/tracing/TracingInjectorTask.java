/*
 * $Id: ArraysUtil.java 1011 2008-06-16 17:57:36Z amandel $
 *
 * Copyright 2006, The jCoderZ.org Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials
 *      provided with the distribution.
 *    * Neither the name of the jCoderZ.org Project nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jcoderz.commons.tracing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.jcoderz.commons.tracing.TracingInjector.Matcher;
import org.jcoderz.commons.tracing.TracingInjector.MethodMatcher;
import org.jcoderz.commons.util.IoUtil;

/**
 * Ant task for TracingInjection.
 *
 * <p>This ant task takes a fileset <code>fileset</code> as input and
 * one directory <code>destDir</code> as output. It copies ALL files
 * from the input to the output, if the do not exist at the destination
 * OR if the file last modified date of the source is newer than the
 * target. Needed directories are created as used.</p>
 *
 * <p>As a 'side effect' all class files (*.class) that match
 * one of the patterns given in the <code>patternFile</code> get
 * JDK14 tracing logging (entering/exiting/throwing) injected in
 * all methods that match the given pattern.</p>
 *
 * <p>If you set <code>java5</code> to <code>true</code> the
 * generated code uses the much more efficient factory methods
 * to box native types that had been introduced with java5
 * (<code>new Integer(int i)</code> vs.
 * <code>Integer.valueOf(int i)</code>). Nevertheless this only affects
 * the code that is guarded with an <code>isLoggable(..)</code> so there no
 * runtime penalty in the generated code if logging is disabled.</p>
 *
 * <p>The <code>patternFile</code> is read line by line and each line
 * is expected to be either empty, start with a '#' or '//' or hold a
 * valid aspectJ like pattern as defined in the
 * AspectPattern according to <a href="http://aspectwerkz.codehaus.org/definition_issues.html">
 *    aspectwerkz</a>. Call the target with '-v' to see
 * the regular expression that is generated out of the input pattern.</p>
 *
 * <p>The task supports a <code>verbose</code> attribute. If set to true
 * a detailed log will be generated about what is going on. This is done
 * by increasing the JDK logging loglevel - be aware of possible
 * side effects.</p>
 *
 * The following code fragment defines the
 * <code>tracing-injector</code> ant task.<pre>
 *   &lt;taskdef name="tracing-injector"
 *       classname="org.jcoderz.commons.tracing.TracingInjectorTask"
 *       classpath="lib/tracing-injector-0.1.jar"/>
 * </pre>
 *
 * A valid usage sample is:<pre>
 *   &lt;tracing-injector destDir="build/classes-log"
 *       patternFile="config/tracing"
 *       java5="false" verbose="false">
 *       &lt;fileset dir="build/classes"/>
 *     &lt;/tracing-injector>
 * </pre>
 *
 * TODO: Support different Tracers to be set.
 *
 * @author Andreas Mandel
 */
public class TracingInjectorTask extends Task
{
//  * <pre>
//  * TODO: Refine Documentation
//  * TODO: Implement validate method
//  * TODO: Add dependency checker (AsM lib)
//  * TODO: If required allow JAR as input and/or output
//  * </pre>
//  *
  private final List mSrcFiles = new ArrayList();
  private String mDestDirName;
  private File mDestDir;
  private final List mSourceFiles = new LinkedList();
  private File mPatternFile;
  private Matcher mMatcher;
  private boolean mJava5 = false;
  private boolean mPai = false;
  private boolean mVerbose = false;

  /**
   *
   * @param fileset files to be collected.
   */
  public void addFileset(FileSet fileset)
  {
    mSrcFiles.add(fileset);
  }

  public void setDestDir(String destDir)
  {
    mDestDirName = destDir;
  }

  public void setPatternFile(File patternFile)
  {
    mPatternFile = patternFile;
  }

  public void setJava5(boolean java5)
  {
    mJava5 = java5;
  }

  public void setPai(boolean pai)
  {
    mPai = pai;
  }

  public void setVerbose(boolean verbose)
  {
    mVerbose = verbose;
  }

  /**
   *
   */
  public void execute()
  {
    validate();

    // BUILD MATCHER Object...
    try
    {
      mMatcher = new MethodMatcher(mPatternFile);
      // IF loglevel??
      log("Read matcher from " + mPatternFile, Project.MSG_VERBOSE);
      log(String.valueOf(mMatcher), Project.MSG_VERBOSE);
    }
    catch (IOException ex)
    {
      throw new BuildException("Could not build matcher from file.", ex);
    }

    for (final Iterator itFSets = mSrcFiles.iterator(); itFSets.hasNext();)
    {
      final FileSet fs = (FileSet) itFSets.next();
      final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
      final File base  = ds.getBasedir();
      final String[] includedFiles = ds.getIncludedFiles();
      for (int i = 0; i < includedFiles.length; i++)
      {
        mSourceFiles.add(new SourceFile(
            new File(base, includedFiles[i]), includedFiles[i]));
      }
    }
    try
    {
      inject();
    }
    catch (IOException ex)
    {
      throw new BuildException(ex);
    }
  }

  /**
   *
   * @throws IOException
   */
  public void inject()
    throws IOException
  {
    while (!mSourceFiles.isEmpty())
    {
      final SourceFile mySource = (SourceFile) mSourceFiles.remove(0);

      final File targetFile = getTargetForSource(mySource);

      if (!targetFile.exists()
          || mySource.getAbsoluteFile().lastModified()
            > targetFile.lastModified())
      {
        ensurePath(targetFile);
        if (mySource.getRelativeFileName().endsWith(".class"))
        {
          try
          {
            log("About to work on " + mySource.getAbsoluteFile()
                + ".", Project.MSG_VERBOSE);
            TracingInjector.inject(mySource.getAbsoluteFile(), targetFile,
                mMatcher, mJava5, mPai);
          }
          catch (RuntimeException ex)
          {
            log("Failed with " + mySource.mAbsoluteFile + " got "
                + ex, Project.MSG_DEBUG);
            if (ex.getMessage() != null
                && ex.getMessage().startsWith("JSR/RET are not supported "))
            {
              IoUtil.copy(mySource.getAbsoluteFile(), targetFile);
            }
            else
            {
              // THIS IS ANT 1.7.0 :-(
              log("About to only copy " + mySource.getAbsoluteFile()
                  + " cause " + ex + ".", ex, Project.MSG_ERR);
              IoUtil.copy(mySource.getAbsoluteFile(), targetFile);
            }
          }
        }
        else
        {
          log("About to copy " + mySource.getAbsoluteFile()
              + ".", Project.MSG_VERBOSE);
          IoUtil.copy(mySource.getAbsoluteFile(), targetFile);
        }
      }
    }
  }

  /**
   *
   * @param file
   * @throws IOException
   */
  private void ensurePath(File file)
    throws IOException
  {
    if (!file.exists())
    {
      if (!file.getParentFile().exists())
      {
        if (!file.getParentFile().mkdirs())
        {
          throw new IOException("Could not create dir for target files.");
        }
      }
    }
  }

  /**
   *
   * @param mySource
   * @return
   */
  private File getTargetForSource(SourceFile mySource)
  {
    return new File(mDestDir, mySource.getRelativeFileName());
  }

  /**
   *
   */
  private void validate()
  {
    mDestDir = new File(mDestDirName);
    if (mVerbose)
    {
      Logger.getLogger(
          TracingInjectorTask.class.getPackage().getName()).setLevel(Level.FINEST);
      final Handler[] handlers = Logger.getLogger("").getHandlers();
      final int amountOfHandlers = handlers.length;
      for (int i = 0; i < amountOfHandlers; i++)
      {
         try
         {
            handlers[i].setLevel(Level.FINEST);
         }
         catch (Exception ex)
         {
            //ignore
         }
      }
    }
  }

  /**
   *
   * @author Andreas Mandel
   */
  private static class SourceFile
  {
    private final File mAbsoluteFile;
    private final String mRelativeFileName;

    /**
     *
     * @param absoluteFile
     * @param relativeFileName
     */
    public SourceFile(File absoluteFile, String relativeFileName)
    {
      mAbsoluteFile = absoluteFile;
      mRelativeFileName = relativeFileName;
    }

    public File getAbsoluteFile()
    {
      return mAbsoluteFile;
    }

    public String getRelativeFileName()
    {
      return mRelativeFileName;
    }
  }
}
