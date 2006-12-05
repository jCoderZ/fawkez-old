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
package org.jcoderz.phoenix.jcoverage;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.cobertura.instrument.Main;

import org.jcoderz.commons.util.FileUtils;
import org.jcoderz.commons.util.JarUtils;


/**
 * 
 * @author Michael Griffel
 */
public final class Instrumenter
{
   private static final transient String CLASSNAME
      = Instrumenter.class.getName();

   private static final transient Logger logger
      = Logger.getLogger(CLASSNAME);

   private final ConfigurationParameters mConfig;

   private final File mBaseTempDir 
      = new File(System.getProperty("java.io.tmpdir"));

   /**
    * Constructor.
    * @param config the configuration parameters.
    */
   public Instrumenter (ConfigurationParameters config)
   {
      mConfig = config;
   }

   /**
    * Main method.
    * @param args command line arguments.
    */
   public static void main (String[] args)
   {
      try
      {
         final Main instrumenter = new Main(); 
         instrumenter.main(args); 

         logger.info("Instrumentation of '" + args[0] + "' done.");
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private static void parseArguments (String[] args)
   {
       final List arguments = new ArrayList();
      try
      {
         for (int i = 0; i < args.length; )
         {
            logger.info("Parsing argument '" 
                  + args[i] + "' = '" + args[i + 1] + "'");
            
            if (args[i].equals("-jcoverage"))
            {
               // FIXME: result.setJcoverage(new File(args[i + 1]));
            }
            else if (args[i].equals("-log4j"))
            {
               // FIXME: result.setLog4j(new File(args[i + 1]));
            }
            else if (args[i].equals("-archive"))
            {
               // FIXME: result.setArchive(new File(args[i + 1]));
            }
            else if (args[i].equals("-loglevel"))
            {
               Logger.getLogger("").setLevel(Level.parse(args[i + 1]));
            }
            else
            {
               throw new IllegalArgumentException("Invalid argument '" 
                     + args[i]  + "'");
            }
            
            ++i;
            ++i;
         }
      }
      catch (IndexOutOfBoundsException e)
      {
         throw new IllegalArgumentException("Missing value for " 
               + args[args.length - 1]);
      }
      
      // FIXME: checkFileExists(result.getArchive());
      // FIXME: checkFileExists(result.getLog4j());
      // FIXME: checkFileExists(result.getJcoverage());
      
   }

   /**
    * Checks if the given file exists.
    * @param f the file to check
    * @throws IllegalArgumentException if the file does not exists.
    */
   private static void checkFileExists (File f)
   {
      if (!f.exists())
      {
         throw new IllegalArgumentException("The file '" 
               + f.getAbsolutePath() + "' does not exists.");
      }
   }

   /**
    * Run JCoverage instrumentation on the archive file.
    * @throws IOException in case of an I/O error.
    */
   public final void instrument () 
         throws IOException
   {
      instrument(mConfig.getArchive());
   }
   
   /**
    * Run JCoverage instrumentation on file <code>f</code>.
    * @param f file to instrument.
    * @throws IOException in case of an I/O error.
    */
   public final void instrument (File f) 
         throws IOException
   {
      if (isArchive(f))
      {
         final File tempDir 
               = FileUtils.createTempDir(mBaseTempDir, f.getName());
         JarUtils.extractJarArchive(tempDir, f);
      
         instrument(tempDir);
      
         if (!f.delete())
         {
            throw new IOException("Cannot remove file " + f); 
         }
                  
         JarUtils.createJarArchive(tempDir, f);
         FileUtils.rmdir(tempDir);
         logger.info("Creating new jar " + f);
      }
      else if (f.isDirectory())
      {
         instrumentClasses(f);
         findNestedArchives(f);
      }
   }

   /**
    * @param f
    */
   private void instrumentClasses (File f) 
         throws IOException
   {
      final List classPath = new ArrayList();
      findClassPath(f, classPath);
      final List classFiles = new ArrayList();
      for (final Iterator pathEntrys = classPath.iterator(); 
            pathEntrys.hasNext();)
      {
         final File originDir = (File) pathEntrys.next();
         classFiles.clear();
         findClassFiles(originDir, originDir, classFiles);

         final File tmpDir = FileUtils.createTempDir(
               mBaseTempDir, "instrumented-classes");

         instrument(classFiles, originDir, tmpDir);

         // move back to origin basedir
         FileUtils.copySlashStar(tmpDir, originDir);

         // copy coverage file to jar location
         // the jcoverage*ser files are written to the current working directory
         // of the JVM!
         /*
         final List jcoverageFiles = FileUtils.findFile(
               new File("."), "jcoverage.*\\.ser");
         for (final Iterator iterator = jcoverageFiles.iterator(); 
               iterator.hasNext();)
         {
            final File file = (File) iterator.next();
            FileUtils.copy(file, originDir);
         }
         */
         
         // add jcoverage.jar/log4j classes
         JarUtils.extractJarArchive(originDir, mConfig.getJcoverage());
         JarUtils.extractJarArchive(originDir, mConfig.getLog4j());

         FileUtils.rmdir(tmpDir);
      }      
   }

   /**
    * Calls the Instrument class from the jcoverage package.
    * 
    * @param classFiles
    * @param classpath
    * @param destinationDir
    * @throws IOException
    */
   private void instrument (
         List classFiles, File classpath, File destinationDir) 
         throws IOException
   {
      // Instrumentation
      final List args = new ArrayList();
      args.add("-d");
      args.add(destinationDir.getCanonicalPath());
      args.add("-ignore");
      args.add("org.apache.log4j.*");
      args.add("-basedir");
      args.add(classpath.getCanonicalPath());
      args.addAll(classFiles);

      logger.finest("Instrument with the following args: " + args);
           
      Main.main((String[]) args.toArray(new String[0]));
   }
   
   /**
    * @param pathEntry
    * @param pathEntry2
    * @param classFiles
    */
   private void findClassFiles (File basePath, File file, List classFiles) 
         throws IOException
   {
      if (file.isFile())
      {
         final String className = FileUtils.getRelativePath(basePath, file);
         if (className.endsWith(".class") && className.indexOf("$") == -1
               && className.matches(".*org.jcoderz.*"))
         {
            //className = className.substring(
            //      0, className.length() - ".class".length());
            /*
            if (File.separator.equals("\\"))
            {
               className = className.replaceAll("\\\\", ".");
            }
            else
            {   
               className = className.replaceAll(File.separator, ".");
            }
            */
            classFiles.add(className);
         }
      }
      else if (file.isDirectory())
      {
         final File [] files = file.listFiles();
        
         for (int i = 0; files != null && i < files.length; i++)
         {
            findClassFiles(basePath, files[i], classFiles);
         }
      }
   }

   /**
    * @param f
    * @param classPath
    */
   private void findClassPath (File f, List classPath)
   {
      final File [] files = f.listFiles(new FileFilter()
            {
               public boolean accept (File pathname)
               {
                  return pathname.isDirectory();
               }
            });

      boolean found = false;
      for (int i = 0; !found && i < files.length; i++)
      {
         if (files[i].getName().equals("com") 
             || files[i].getName().equals("junit"))
         {
            classPath.add(files[i].getParentFile());
            found = true;
         }
      }
      if (!found)
      {
         for (int i = 0; i < files.length; i++)
         {
            findClassPath(files[i], classPath);
         }
      }
   }

   /**
    * @param f
    */
   private void findNestedArchives (File file) 
         throws IOException
   {
      if (file == null)
      {
         // done...
      }
      else if (file.isDirectory())
      {
         final File [] files = file.listFiles();
         for (int i = 0; i < files.length; i++)
         {
            findNestedArchives(files[i]);
         }
      }
      else 
      {
         if (isArchive(file))
         {
            instrument(file);
         }
      }
   }

   private boolean isArchive (File file) 
         throws IOException
   {
      return file.getCanonicalPath().matches(".*\\.[jerw]ar");
   }
   
   private static class ConfigurationParameters
   {
      private File mArchive = null;
      private File mLog4j = null;
      private File mJcoverage = null;      

      /**
       * Returns the archive.
       * @return the archive.
       */
      public final File getArchive ()
      {
         return mArchive;
      }

      /**
       * Sets the archive to given <code>archive</code>.
       * @param archive The archive to set.
       */
      public final void setArchive (File archive)
      {
         mArchive = archive;
      }

      /**
       * Returns the jcoverage.
       * @return the jcoverage.
       */
      public final File getJcoverage ()
      {
         return mJcoverage;
      }

      /**
       * Sets the jcoverage to given <code>jcoverage</code>.
       * @param jcoverage The jcoverage to set.
       */
      public final void setJcoverage (File jcoverage)
      {
         mJcoverage = jcoverage;
      }

      /**
       * Returns the log4j.
       * @return the log4j.
       */
      public final File getLog4j ()
      {
         return mLog4j;
      }

      /**
       * Sets the log4j to given <code>log4j</code>.
       * @param log4j The log4j to set.
       */
      public final void setLog4j (File log4j)
      {
         mLog4j = log4j;
      }

   }
   
}
