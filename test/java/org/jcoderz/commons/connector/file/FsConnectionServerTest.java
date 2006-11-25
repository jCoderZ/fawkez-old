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
import java.io.RandomAccessFile;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jcoderz.commons.InternalErrorException;
import org.jcoderz.commons.ServerTestCase;
import org.jcoderz.commons.util.IoUtil;


/**
 *
 * Server testcases for the commons file connector.
 *
 */
public class FsConnectionServerTest
      extends ServerTestCase
{
   /** The full qualified name of this class. */
   private static final String CLASSNAME
         = FsConnectionServerTest.class.getName();
   /** The logger to use. */
   private static final transient Logger logger = Logger.getLogger(CLASSNAME);

   private static final int MAX_CONNECTIONS = 100;

   private static final String MSG_LOOKUP_FAILED
         = "FsConnection lookup failed.";
   private static final String MSG_ERROR_CLOSING
         = "Error while connection closing.";

   private static final long CHUNK_SIZE = 1000;

   /**
    * Creates the testsuite.
    * @return Test The testsuite
    */
   public static Test suite ()
   {
      final TestSuite suite;
      if (hasTestCases())
      {
         suite = getSuite(FsConnectionServerTest.class);
      }
      else
      {
         suite = new TestSuite(FsConnectionServerTest.class);
      }
      return suite;
   }

   /**
    * Tests FsConnection lookup.
    */
   public void testConnectionLookup ()
   {
      try
      {
         final FsConnection fc = FsConnectionUtil.getFileSystemConnection();
         assertNotNull(MSG_LOOKUP_FAILED, fc);
         FsConnectionUtil.close(fc);
      }
      catch (InternalErrorException iee)
      {
         iee.log();
         fail(MSG_LOOKUP_FAILED +  ", caught exception " + iee.getMessage());
      }
   }


   /**
    * Tests connection reusage.
    */
   public void testCloseConnection ()
   {
      for (int i = 0; i < MAX_CONNECTIONS; i++)
      {
         final FsConnection fc = FsConnectionUtil.getFileSystemConnection();
         try
         {
            fc.close();
         }
         catch (ResourceException e)
         {
            logger.log(Level.SEVERE, MSG_ERROR_CLOSING, e);
            fail(MSG_ERROR_CLOSING);
         }
      }
   }

   /**
    * Tests {@link FsConnection#close()}.
    */
   public void testCloseConnection3 ()
   {
      for (int i = 0; i < MAX_CONNECTIONS; i++)
      {
         final FsConnection fc1 = FsConnectionUtil.getFileSystemConnection();
         final FsConnection fc2 = FsConnectionUtil.getFileSystemConnection();
         final FsConnection fc3 = FsConnectionUtil.getFileSystemConnection();
         try
         {
            fc1.close();
            fc2.close();
            fc3.close();
         }
         catch (ResourceException e)
         {
            logger.log(Level.SEVERE, MSG_ERROR_CLOSING, e);
            fail(MSG_ERROR_CLOSING);
         }
      }
   }

   /**
    * Tests the methd {@link FsConnection#createTempFile()}.
    *
    */
   public void testCreateTempFile ()
   {
      final FsConnection fc = FsConnectionUtil.getFileSystemConnection();
      try
      {
         final String f = fc.createTempFile();
         logger.finer("Created temp file " + f);
         fc.deleteFile(f);
         fc.close();
      }
      catch (ResourceException e)
      {
         final String msg = "testCreateTempFile failed";
         logger.log(Level.SEVERE, msg, e);
         fail(msg);
      }
   }

   /**
    * Tests handling of big files.
    */
   public void testBigFile ()
   {
      final String msg = "testBigFile failed";
      final FsConnection fc = FsConnectionUtil.getFileSystemConnection();
      RandomAccessFile raf = null;
      try
      {
         final String f = fc.createTempFile();
         logger.finer("Created temp file " + f);
         final String copyF = f + "Copy";
         raf = fc.getRandomAccessFile(f, "rw");

         raf.seek(FsConnectionFactory.FILE_TRANSFER_CHUNK_SIZE_DEF_VALUE + 1);
         raf.writeChars("Just a teststring!");
         logger.finer("Writen " + raf.length() + " bytes into the file " + f);
         IoUtil.close(raf);
         raf = null;

         fc.moveFile(f, copyF);
         logger.finer("Moved " + f + " to " + copyF);
         assertTrue("The destination file " + copyF
               + " does not exist after the file moving.", fc.isExists(copyF));
         assertFalse("Just moved file " + copyF + " does exits",
               fc.isExists(f));

         fc.deleteFile(copyF);
         logger.finer("Deleted " + copyF);
         assertFalse("Deleted file " + copyF + " exists.", fc.isExists(copyF));
      }
      catch (ResourceException r)
      {
         logger.log(Level.SEVERE, msg, r);
         fail(msg);
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, msg, e);
         fail(msg);
      }

      IoUtil.close(raf);
      FsConnectionUtil.close(fc);
   }

   /**
    * Tests handling of big files.
    */
   public void testUserProperties ()
   {
      final String msg = "testUserProperties failed";
      final Properties props = new Properties();
      FsConnection fc = FsConnectionUtil.getFileSystemConnection();
      RandomAccessFile raf = null;
      try
      {
         final String tmpDir = fc.createTempFile();
         fc.deleteFile(tmpDir);
         fc.close();
         final File dir = new File(tmpDir);
         dir.mkdirs();

         props.setProperty(FsConnectionFactory.PROP_TEMP_DIR, dir.toString());
         props.setProperty(FsConnectionFactory.PROP_FILE_TRANSFER_CHUNK_SIZE,
               String.valueOf(CHUNK_SIZE));
         fc = FsConnectionUtil.getFileSystemConnection(props);

         final String tmp = fc.createTempFile();
         final File tmpFile = new File(tmp);
         assertEquals("Newly created temp file " + tmpFile
               + " has a wrong parent dir. Expected " + dir,
                  tmpFile.getParentFile(), dir);

         raf = fc.getRandomAccessFile(tmp, "rw");

         raf.seek(CHUNK_SIZE + CHUNK_SIZE);
         raf.writeChars("Just a teststring!");
         logger.finer("Writen " + raf.length() + " bytes into the file "
               + tmpFile);
         IoUtil.close(raf);

         final String copyF = tmp + "Copy";

         fc.moveFile(tmp, copyF);
         fc.deleteFile(copyF);
         dir.delete();
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, msg, e);
         fail(msg);
      }

      IoUtil.close(raf);
      FsConnectionUtil.close(fc);
   }
}
