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
package org.jcoderz.phoenix.sqlparser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task for the SQL Transformer.
 * 
 * @author Michael Griffel
 */
public class SqlTransformerTask
      extends MatchingTask
{
   /** list of input files. */
   private final List mFilesets = new ArrayList();
   /** the output directory. */
   private File mOutputDir;
   /** terminate ant build on error. */
   private boolean mFailOnError = false;
   /** force output of target files even if they already exist. */
   private boolean mForce = false;
   /** the metaInf file (optional). */
   private File mMetaInfFile = null;

   
   /**
    * Specifies the output directory.
    * @param d the output directory.
    */
   public void setTodir (File d)
   {
      mOutputDir = d;
   }
   
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
    * Sets the force output of target files flag to the given value.
    *
    * @param b Whether we should force the generation of output files.
    */
   public void setForce (boolean b)
   {
      mForce = b;
   }
   
   /**
    * Specifies the MetaInf file (optional parameter).
    * @param f the MetaInf file.
    */
   public void setMetainf (File f)
   {
      mMetaInfFile = f;
   }
   
   /**
    * Adds the given files to the file set.
    * @param set the file set to add.
    */
   public void addFileset (FileSet set)
   {
      mFilesets.add(set);
   }
   
   /**
    * 
    * Execute this task.
    * 
    * @throws BuildException An building exception occurred.
    */
   public void execute () 
         throws BuildException
   {
      try
      {
         checkAttributes();
       
         final File[] files = getSuiteFiles();
         int transformedFiles = 0;
         for (int i = 0; i < files.length; i++)
         {
            final File inFile = files[i];
            final File outFile = new File(mOutputDir, inFile.getName());
            if (mForce || inFile.lastModified() > outFile.lastModified())
            {
               log("Transforming file: " + inFile, Project.MSG_INFO);
               final SqlTransformer transformer 
                     = new SqlTransformer(
                        inFile.getAbsolutePath(), 
                        outFile.getAbsolutePath(), 
                         mMetaInfFile != null 
                            ? mMetaInfFile.getAbsolutePath() : null, false);
               transformer.execute();
               transformedFiles++;
            }
            else
            {
               log(inFile + " omitted as " 
                     + outFile + " is up to date.", Project.MSG_VERBOSE);
            }
         }
         if (transformedFiles > 0)
         {
            log(transformedFiles + " of " + files.length 
                  + " files transformed successfully", Project.MSG_INFO);
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

   
   private void checkAttributes ()
   {
      if (mOutputDir == null)
      {
         throw new BuildException(
               "Missing mandatory attribute 'todir'.", getLocation());
      }
      ensureDirectoryFor(mOutputDir);
      
      if (mMetaInfFile != null && !mMetaInfFile.exists())
      {
         throw new BuildException(
               "Cannot find META-INF file '" + mMetaInfFile + "'.", 
               getLocation());         
      }
   }

   /**
    * Ensure the directory exists for a given directory name.
    *
    * @param targetFile the directory name that is required.
    * @exception BuildException if the directories cannot be created.
    */
   private static void ensureDirectoryFor (File directory)
         throws BuildException 
   {
       if (!directory.exists()) 
       {
           if (!directory.mkdirs()) 
           {
               throw new BuildException("Unable to create directory: "
                                        + directory.getAbsolutePath());
           }
       }
   }
   
   private File[] getSuiteFiles ()
   {
      final Set set = new HashSet();

      final Iterator iterator = mFilesets.iterator();
      while (iterator.hasNext())
      {
         final FileSet fs = (FileSet) iterator.next();

         final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
         final File dir = fs.getDir(getProject());
         final String[] filenames = ds.getIncludedFiles();
         for (int i = 0; i < filenames.length; i++)
         {
            set.add(new File(dir, filenames[i]));
         }
      }

      final int size = set.size();
      log("Found " + size + " files in " + mFilesets.size() + " filesets",
            Project.MSG_VERBOSE);

      return (File[]) set.toArray(new File[size]);
   }   
}
