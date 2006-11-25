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
package org.jcoderz.commons.connector.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import javax.resource.ResourceException;

/**
 * Test the File interface.
 *
 */
public class FsConnectionInterfaceTest
      extends FsTestCase

{
   private static final Random RANDOM = new Random();
   private final List mFiles = new ArrayList();

   private File mFileA;
   private File mFileB;

   /** {@inheritDoc} */
   public void setUp ()
         throws Exception
   {
      super.setUp();
      mFileA = File.createTempFile("AAA", "A");
      mFileB = File.createTempFile("BBB", "B");
      mFiles.add(mFileA);
      mFiles.add(mFileB);
   }

   /**
    * Tests the method {@link FsConnection#isExists(String)}.
    */
   public void testIsExists ()
   {
      final FsConnection c = getConnection();
      final String nonExisingFile = "ThisFileDoesNotExist";

      try
      {
         assertEquals("Check existing file failed.",
               c.isExists(mFileA.toString()), mFileA.exists());
         assertFalse("Check non existing file failed.",
               c.isExists(nonExisingFile));

         c.close();
      }
      catch (ResourceException e)
      {
         fail("Test is exist failed. " + e.getMessage());
      }
   }

   /**
    * Tests the method {@link FsConnection#renameFile(String, String)}.
    */
   public void testRenameFile ()
   {
      final FsConnection c = getConnection();

      try
      {
         final File fileC = File.createTempFile("CCC", "C");
         fileC.delete();
         c.renameFile(mFileA.toString(), fileC.toString());
         mFiles.add(fileC);
         assertTrue("Rename failed.", !mFileA.exists() && fileC.exists());

         c.renameFile(fileC.toString(), mFileA.toString());
         assertTrue("Rename back failed.", mFileA.exists() && !fileC.exists());

         try
         {
            c.renameFile(fileC.toString(), mFileA.toString());
            fail("Rename of a non existing file should throw the "
                  + "ResourceException.");
         }
         catch (ResourceException re)
         {
            // expected
         }

         c.close();
      }
      catch (Exception e)
      {
         fail("Test is exist failed. " + e.getMessage());
      }
   }

   /**
    * Tests the method {@link FsConnection#listFiles(String)}.
    */
   public void testList ()
   {
      final FsConnection c = getConnection();

      try
      {
         String [] list = c.listFiles(mFileA.getParent());
         assertNotNull("listFiles returned null, expected an array.", list);
         assertTrue("The method listFile must return an array of size >= 1",
               list.length > 0);

         list = c.listFiles(mFileA.toString());
         assertNull("The listFiles must return null if the method's argument is"
               + " a regular file.", list);

         c.close();
      }
      catch (ResourceException re)
      {
         fail("Test list file failed. " + re.getMessage());
      }
   }

   /**
    * Tests the method {@link FsConnection#getRandomAccessFile(String, String)}.
    */
   public void testRafBasePath ()
   {
      final FsConnection c = getConnection();

      try
      {
         final RandomAccessFile rfa = c.getRandomAccessFile(mFileB.toString(),
               "r");
         try
         {
            rfa.length();
            rfa.close();
         }
         catch (IOException e1)
         {
            fail("Unexpected exception" + e1.toString());
         }
         c.close();
      }
      catch (ResourceException re)
      {
         fail("testRafBasePath failed. " + re.getMessage());
      }
      catch (FileNotFoundException e)
      {
         fail("File " + mFileB.toString() + " was not found: "
               + e.getMessage());
      }
   }


   /**
    * Tests the behaviour of the instance RandomAccessFile after the connection
    * has been closed.
    */
   public void testRafConnectionClosed ()
   {
      final FsConnection c = getConnection();

      try
      {
         final RandomAccessFile rfa = c.getRandomAccessFile(mFileB.toString(),
               "r");

         c.close();

         try
         {
            rfa.length();
            fail("Must throw a IOException");
         }
         catch (IOException e1)
         {
            // expected
         }
      }
      catch (ResourceException re)
      {
         fail("testRafBasePath failed. " + re.getMessage());
      }
      catch (FileNotFoundException e)
      {
         fail("File " + mFileB.toString() + " was not found: "
               + e.getMessage());
      }
   }

   /**
    * Tests methods creating new files.
    */
   public void testCreateFile ()
   {
      final FsConnection c = getConnection();

      try
      {
         String tmp = c.createTempFile();
         final File f = createFileInstance(tmp);

         assertTrue("Expected temp file does not exist.", f.exists());
         f.delete();
         f.mkdir();

         tmp = c.createTempFile(f.toString());
         final File f1 =  createFileInstance(tmp);
         assertTrue("Expected temp file does not exist.", f1.exists());

         deleteFile(f1);

         assertTrue("Could not create a new file '" + f1.toString() + "'.",
               c.createFile(f1.toString()));
         f1.delete();
         final File f2 = createFileInstance(f.toString() + File.separator + "A"
               + File.separator + "B" + File.separator + "C");

         assertTrue("Could not create a new file '" + f2.toString() + "'.",
               c.createFile(f2.toString()));

         deleteFile(f);
         deleteFile(f2);

         c.close();
      }
      catch (ResourceException e)
      {
         e.printStackTrace();
         fail("Test 'create temp file' failed. " + e.getMessage());
      }
   }

   /**
    * Tests the method {@link FsConnection#renameToTempFile(String)}.
    */
   public void testRenameToTempFile ()
   {
      final FsConnection c = getConnection();

      try
      {
         final File file = File.createTempFile("CCC", "C");
         mFiles.add(file);

         final String renamed = c.renameToTempFile(file.getAbsolutePath());
         assertNotNull("Method FsConnection.renameToTempFile must return a "
               + "non-null filename.", renamed);
         assertFalse("The file to be renamed and temp file must differ.",
               file.getAbsolutePath().compareTo(renamed) == 0);

         final File renamedFile = createFileInstance(renamed);
         assertTrue("The renamed file does not exist.", renamedFile.exists());
         assertFalse("The original file exists after it has been renamed.",
               file.exists());

         file.setReadOnly();

         try
         {
            c.renameToTempFile(file.getAbsolutePath());
            fail("Renaming of a read-only file must throw a ResourceException");
         }
         catch (ResourceException re)
         {
            // expected exception
         }

         deleteFile(file);

         try
         {
            c.renameToTempFile(file.getAbsolutePath());
            fail("Renaming of a non-existing file must throw a "
                  + "ResourceException");
         }
         catch (ResourceException re)
         {
            // expected exception
         }

         c.close();
      }
      catch (Exception e)
      {
         fail("Test 'RenameToTempFile' failed. " + e.getMessage());
      }
   }



   /**
    * Tests the method {@link FsConnection#getFileInputStream(String)}.
    */
   public void testFileInputStreamBasePath ()
   {
      final FsConnection c = getConnection();

      try
      {
         final FileInputStream fis = c.getFileInputStream(mFileB.toString());
         try
         {
            fis.available();
            fis.close();
         }
         catch (IOException e1)
         {
            fail("Unexpected exception" + e1.toString());
         }
         c.close();
      }
      catch (ResourceException re)
      {
         fail("testRafBasePath failed. " + re.getMessage());
      }
      catch (FileNotFoundException e)
      {
         fail("File " + mFileB.toString() + " was not found: "
               + e.getMessage());
      }
   }

   /**
    * Tests the method {@link FsConnection#moveFile(String, String)}.
    */
   public void testMoveFile ()
   {
      try
      {
         final FsConnection c = getConnection();
         final File src = File.createTempFile("CCC", "C");
         final File dest = new File((src.getParentFile()).getCanonicalPath()
               + File.separator
               + String.valueOf(RANDOM.nextInt(Integer.MAX_VALUE))
               + File.separator + "dest.tmp");
         dest.deleteOnExit();
         src.deleteOnExit();
         c.moveFile(src.toString(), dest.toString());
         assertTrue("Destination file does not exist", dest.exists());
         assertFalse("Source file exists", src.exists());
         c.deleteFile(dest.toString());
         c.deleteFile(dest.getParentFile().toString());
         c.close();
      }
      catch (IOException ioE)
      {
         fail("testMoveFile failed. " + ioE.getMessage());
      }
      catch (ResourceException re)
      {
         fail("testMoveFile failed. " + re.getMessage());
      }

   }

   /** {@inheritDoc} */
   public void tearDown ()
         throws Exception
   {
      super.tearDown();
      for (int i = 0; i < mFiles.size(); i++)
      {
         final File file = (File) mFiles.get(i);
         if (file != null && file.exists())
         {
            del(file);
         }
      }
   }

   /**
    * The tmp dir used during filegeneration must be configurable. This method
    * tests using of the right tmp dir.
    *
    */
   public void testTempDir ()
   {
      FsConnection fc = getConnection();
      final File sysTmpDir = new File(System.getProperty("java.io.tmpdir"));
      String tmp = null;
      File tmpFile = null;
      try
      {
         tmp = fc.createTempFile();
         tmpFile = new File(tmp).getParentFile();
         assertEquals("The temp dir used by fs '" + tmpFile
               + "' does not match the system temp dir '" + sysTmpDir +  "'",
                  sysTmpDir, tmpFile);
         fc.deleteFile(tmp);


         new File(tmp).mkdir();
         final File newTd = new File(tmp).getParentFile();
         fc.deleteFile(tmp);
         fc.close();

         final Properties props = new Properties();
         props.put(FsConnectionFactory.PROP_TEMP_DIR, newTd.toString());

         fc = getConnection(props);
         tmp = fc.createTempFile();
         tmpFile = new File(tmp).getParentFile();
         assertEquals("The temp dir used by fs '" + tmpFile
               + "' does not match the config temp dir '" + newTd +  "'",
               newTd, tmpFile);
         fc.deleteFile(tmp);
         fc.deleteFile(newTd.toString());
         fc.close();
      }
      catch (ResourceException e)
      {
         e.printStackTrace();
         fail("testTempDir failed. " + e.getMessage());
      }

   }

   private File createFileInstance (String file)
   {
      final File f = new File(file);
      mFiles.add(f);
      return f;
   }

   /**
    * Deletes the file if exists.
    * @param file to be deleted.
    */
   private void deleteFile (File file)
   {
      del(file);
      mFiles.remove(file);
   }

   private void del (File file)
   {
      if (file.isDirectory())
      {
         final File [] f = file.listFiles();
         for (int i = 0; i < f.length; i++)
         {
            del(f[i]);
         }
      }

      file.delete();
   }
}
