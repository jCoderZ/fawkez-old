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
import java.io.FilenameFilter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Provides utility functions for Ant task.
 * 
 * @author Michael Griffel
 */
public final class AntTaskUtil
{
   private AntTaskUtil ()
   {
      // no instances allowed -- provides only static helper methods.
   }

   /**
    * Ensure the directory exists for a given directory name.
    *
    * @param directory the directory name that is required.
    * @exception BuildException if the directories cannot be created.
    */
   public static void ensureDirectory (File directory)
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

   /**
    * Ensure the directory exists for a given file.
    *
    * @param targetFile the file for which the directories are required.
    * @exception BuildException if the directories cannot be created.
    */
   public static void ensureDirectoryForFile (File targetFile)
         throws BuildException 
   {
       ensureDirectory(targetFile.getParentFile());
   }

   public static void dot2pic (Task task, String name, File dotFile, 
         String imageFormat, boolean failOnError)
   {
      final DotTask dot = new DotTask();
      dot.setProject(task.getProject());
      dot.setTaskName("dot");
      dot.setFailonerror(failOnError);
      dot.setFormat(imageFormat);
      dot.setIn(dotFile.getAbsoluteFile());
      final File picFile = new File(dotFile.getParentFile(), 
            name + "." + dot.getFileExtension());
      dot.setOut(picFile);
      dot.execute();
   }

   public static String stripFileExtention (String fileWithExtension)
   {
      final int lastIndexOfDot = fileWithExtension.lastIndexOf('.');
      final String result;
      if (lastIndexOfDot != -1)
      {
         result = fileWithExtension.substring(0, lastIndexOfDot);
      }
      else
      {
         result = fileWithExtension;
      }
      return result;
   }

   public static void renderDotFiles (Task task, File dotDir, 
         boolean failOnError)
   {
      final File[] dotFiles = dotDir.listFiles(new FilenameFilter()
            {
               public boolean accept (File dir, String name)
               {
                  final boolean result;
                  if (name.endsWith(".dot"))
                  {
                     result = true;
                  }
                  else
                  {
                     result = false;
                  }
                  return result;
               }
            });
   
      if (dotFiles != null)
      {
         for (int i = 0; i < dotFiles.length; i++)
         {
            final File dotFile = dotFiles[i];
            final String name = stripFileExtention(dotFile.getName());
            dot2pic(task, name, dotFile, "svg", failOnError);
      
            task.log("Generated diagrams for '" + name + "'");
         }
      }
      else
      {
         task.log("No .dot files found to render", Project.MSG_VERBOSE);
      }
   }
   

}
