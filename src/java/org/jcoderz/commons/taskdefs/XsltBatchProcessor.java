/*
 * $Id$
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
package org.jcoderz.commons.taskdefs;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.jcoderz.commons.util.IoUtil;


/**
 * Ant task that performs XSL transformation for a set of input files.
 *
 * @author Michael Griffel
 */
public class XsltBatchProcessor
    extends Task
{
    private String mStyleSheet = "default.xsl";

    private FileSet mFiles = new FileSet();

    private boolean mResolveExternalEntities = true;

    /** terminate ant build on error. */
    private boolean mFailOnError = false;

    /**
     * Set whether we should fail on an error.
     *
     * @param b Whether we should fail on an error.
     */
    public void setFailonerror (boolean b)
    {
        mFailOnError = b;
    }

    /**
     * Set the XSL Stylesheet to use.
     *
     * @param f The name of the XSL Stylesheet file.
     * @see XsltBasedTask#getDefaultStyleSheet()
     */
    public void setXsl (String f)
    {
        mStyleSheet = f;
    }

    /**
     * XML files that are used as input documents for the
     * transformation.
     *
     * @param fs fileset of XML files.
     */
    public void addFiles (FileSet fs)
    {
        mFiles = fs;
    }

    /**
     * {@inheritDoc}
     */
    public void execute ()
        throws BuildException
    {
        try
        {
            final XsltBasedTask xsltTask = new XsltBasedTask()
            {
                String getDefaultStyleSheet ()
                {
                    return mStyleSheet;
                }
            };
            final Project myProject = getProject();
            final DirectoryScanner ds = mFiles.getDirectoryScanner(myProject);
            final String[] includedFiles = ds.getIncludedFiles();
            log("Transforming " + includedFiles.length + "files in directory "
                + ds.getBasedir());
            for (int i = 0; i < includedFiles.length; i++)
            {
                final String f = includedFiles[i];
                final File orig = new File(ds.getBasedir(), f);
                final File out;
                try
                {
                    out = File.createTempFile("jcoderz", "tmp");
                }
                catch (IOException e)
                {
                    throw new BuildException(
                        "Failed to create temp file: " + e, e);
                }
                xsltTask.setProject(myProject);
                xsltTask.setTaskName("xslt");
                xsltTask.setIn(orig);
                xsltTask.setOut(out);
                xsltTask.setDestdir(myProject.getBaseDir());
                xsltTask.setForce(true);
                xsltTask.setFailonerror(mFailOnError);
                xsltTask.setLogLevel(Project.MSG_VERBOSE);
                xsltTask.resolveExternalEntities(mResolveExternalEntities);
                log("Transforming file " + orig, Project.MSG_VERBOSE);
                xsltTask.execute();
                if (out.exists())
                {
                    if (!orig.delete())
                    {
                        throw new BuildException("Failed to delete " + orig);
                    }
                    if (!out.renameTo(orig))
                    {
                        // try copy && delete
                        try
                        {
                            safeMove(out, orig);
                        }
                        catch (IOException e)
                        {
                            throw new BuildException("Failed to move file "
                                + out, e);
                        }
                    }
                }
            }
        }
        catch (BuildException e)
        {
            if (mFailOnError)
            {
                throw e;
            }
            log(e.getMessage(), Project.MSG_ERR);
        }
    }

    /**
     * If set to <tt>false</tt>, external entities will not be
     * resolved.
     *
     * @param b new value.
     */
    public void resolveExternalEntities (boolean b)
    {
        mResolveExternalEntities = b;
    }

    private void safeMove (File source, File dest)
        throws IOException
    {
        final FileInputStream in = new FileInputStream(source);
        final FileOutputStream out = new FileOutputStream(dest);
        try
        {
            IoUtil.copy(in, out);
            if (!source.delete())
            {
                throw new BuildException("Failed to delete " + source);
            }
        }
        finally
        {
            IoUtil.close(in);
            IoUtil.close(out);
        }
    }
}
