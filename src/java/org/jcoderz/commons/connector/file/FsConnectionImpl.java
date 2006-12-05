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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.SecurityException;

import org.jcoderz.commons.connector.ConnectionBase;
import org.jcoderz.commons.connector.ConnectionNotificationListener;
import org.jcoderz.commons.util.IoUtil;


/**
 * Implements the File System Connection.
 * This connection handle provides physical interraction to the File System.
 * This behavior is not really conform to the JCA specification, but satisfy
 * requirenments to provide set of base operations on the underlying File
 * System.
 *
 */
class FsConnectionImpl
      extends ConnectionBase
      implements FsConnection
{
   public static final String TMP_FILE_PREFIX = "FWK";
   public static final String TMP_FILE_SUFFIX = "fwk";


   /** The full qualified name of this class. */
   private static final transient String CLASSNAME = FsConnectionImpl.class
         .getName();

   /** The logger to use. */
   private static final transient Logger logger = Logger.getLogger(CLASSNAME);

   /** Random instance to use for generating of backup file names. */
   private static final Random RANDOM = new Random();

   /** Helds closable instances. */
   private final Set mCloseables = new HashSet();

   /** Random handle for debug logging. */
   private final int mIndex;
   /** To string holder. */
   private final String mStringified;

   /** The file temp dir. **/
   private final File mTmpDir;

   private final long mFileTransferChunkSize;

   /**
    * Constructor.
    * @param cnl The listener interesting on this connection.
    * @param props Configuration properties.
    */
   public FsConnectionImpl (ConnectionNotificationListener cnl,
         Properties props)
   {
      super(cnl);
      synchronized (RANDOM)
      {
         mIndex = RANDOM.nextInt(Integer.MAX_VALUE);
         mStringified = "FsConnectionImpl[" + mIndex + "]";
      }

      String tmp = null;
      if (props != null)
      {
         tmp = props.getProperty(FsConnectionFactory.PROP_TEMP_DIR);
      }

      if (tmp == null)
      {
         tmp = System.getProperty("java.io.tmpdir");
      }

      final String chunkSize = props.getProperty(
         FsConnectionFactory.PROP_FILE_TRANSFER_CHUNK_SIZE,
         String.valueOf(
               FsConnectionFactory.FILE_TRANSFER_CHUNK_SIZE_DEF_VALUE));

      mTmpDir = new File(tmp);
      mFileTransferChunkSize = Long.valueOf(chunkSize).longValue();
      if (logger.isLoggable(Level.FINER))
      {
         logger.finer("Created FsConnection with attributes TempDir " + mTmpDir
               + ", FileTransferChunkSize " + chunkSize);
      }
   }

   /** {@inheritDoc} */
   public void close ()
         throws ResourceException
   {
      logger.entering(CLASSNAME, "close");
      setClosed();

      // be sure that all file access handles are closed
      closeAllCloseables();
      logger.exiting(CLASSNAME, "close");
   }

   /** {@inheritDoc} */
   public boolean isExists (String file)
         throws ResourceException
   {
      final String method = "isExists";
      logger.entering(CLASSNAME, method, file);
      assertValid();

      final boolean result = isFileExists(new File(file));

      logger.exiting(CLASSNAME, method, Boolean.valueOf(result));
      return result;
   }

   static javax.resource.spi.SecurityException
         createReadAccessSecurityException (String file,
               java.lang.SecurityException se)
   {
      final javax.resource.spi.SecurityException rse
            = new javax.resource.spi.SecurityException(
               "The SecurityManager denies read access to the file '"
                     + file + "' or to the parent derectory.");
      se.initCause(se);
      return rse;
   }

   /** {@inheritDoc} */
   public boolean deleteFile (String file)
         throws ResourceException
   {
      assertValid();
      return deleteFile(new File(file));
   }

   /** {@inheritDoc} */
   public String [] listFiles (final String dir)
         throws ResourceException
   {
      final String method = "listFiles";
      logger.entering(CLASSNAME, method, dir);
      assertValid();

      final File file = new File(dir);
      final String [] result;

      try
      {
         result = file.list();
      }
      catch (java.lang.SecurityException se)
      {
         final javax.resource.spi.SecurityException rse
               = new javax.resource.spi.SecurityException("The SecurityManager "
                     + "denies read access to the directory '" + dir + "'.");
         rse.initCause(se);
         logger.throwing(CLASSNAME, method, rse);
         throw rse;
      }

      logger.exiting(CLASSNAME, method, result);
      return result;
   }

   /** {@inheritDoc} */
   public void renameFile (final String from, final String to)
         throws ResourceException
   {
      final String method = "rename";
      logger.entering(CLASSNAME, method, new Object [] {from, to});
      assertValid();

      moveFile(from, to);
      logger.exiting(CLASSNAME, method);
   }

   /** {@inheritDoc} */
   public void moveFile (final String src, final String dest)
         throws ResourceException
   {
      final String method = "move";
      logger.entering(CLASSNAME, method, new Object [] {src, dest});
      assertValid();

      final File srcFile = new File(src);
      final File destFile = new File(dest);

      ResourceException re = null;
      File backupFile = null;

      if (!isFileExists(srcFile))
      {
         re = new ResourceException("File '" + srcFile.toString()
               + "' does not exist.");
         logger.throwing(CLASSNAME, method, re);
         throw re;
      }

      if (!srcFile.canWrite())
      {
         re = new ResourceException("File '" + srcFile.toString()
               + "' is not writable.");
         logger.throwing(CLASSNAME, method, re);
         throw re;
      }

      if (isFileExists(destFile))
      {

         if (!destFile.canWrite())
         {
            re = new ResourceException("The destination file '"
                  + destFile.toString() + "' does already exist and is not "
                  + "writable.");
            logger.throwing(CLASSNAME, method, re);
            throw re;
         }

         backupFile = getBackupFile(destFile);
         re = rename(destFile, backupFile);

         if (re != null)
         {
            logger.throwing(CLASSNAME, method, re);
            throw re;
         }
      }
      else
      {
         createParentDirs(destFile);
      }

      re = rename(srcFile, destFile);

      if (re != null)
      {
         if (backupFile != null)
         {
            // rollback
            final ResourceException e = rename(backupFile, destFile);
            if (e != null)
            {
               logger.log(Level.WARNING, "Error while file renaming.", e);
            }
         }

         logger.throwing(CLASSNAME, method, re);
         throw re;
      }

      if (backupFile != null && !backupFile.delete())
      {
         re = new ResourceException("Could not delete file '"
               + backupFile.toString() + "'.");

         logger.throwing(CLASSNAME, method, re);
         throw re;
      }

      logger.exiting(CLASSNAME, method);
   }
   
   /** {@inheritDoc} */
   public String createTempFile ()
         throws ResourceException
   {
      final String method = "createTempFile";
      logger.entering(CLASSNAME, method);
      assertValid();

      final String result = createNewFile(null);

      logger.exiting(CLASSNAME, method, result);
      return result;
   }

   /** {@inheritDoc} */
   public String createTempFile (final String dir)
         throws ResourceException
   {
      final String method = "createTempFile";
      logger.entering(CLASSNAME, method, dir);
      assertValid();

      final File f = new File(dir);
      if (!isFileExists(f) || !f.isDirectory())
      {
         final ResourceException re = new ResourceException(
            "Failed to create a new file. A directory named '" + dir
               + "' does not exist.");

         logger.throwing(CLASSNAME, method, re);
         throw re;
      }

      final String result = createNewFile(dir);

      logger.exiting(CLASSNAME, method, result);
      return result;
   }

   /** {@inheritDoc} */
   public boolean createFile (String file)
         throws ResourceException
   {
      final String method = "createFile";
      logger.entering(CLASSNAME, method, file);
      assertValid();

      final File f = new File(file);

      // check if the file does already exist
      if (isFileExists(f))
      {
         final ResourceException re = new ResourceException(
            "Failed to create a new file. A file named '" + file
               + "' does already exist.");
         logger.throwing(CLASSNAME, method, re);
         throw re;
      }

      // check all necessary parent directories do exist.
      createParentDirs(f);

      final boolean result;
      try
      {
         result = f.createNewFile();
      }
      catch (IOException e)
      {
         final ResourceException re = new ResourceException(
               "Failed to create a new file '" + file + "'.");
         re.initCause(e);
         logger.throwing(CLASSNAME, method, re);
         throw re;
      }
      catch (java.lang.SecurityException se)
      {
         final javax.resource.spi.SecurityException rse
            = new javax.resource.spi.SecurityException(
                  "Failed to create a new file.");
         rse.initCause(se);
         logger.throwing(CLASSNAME, method, rse);
         throw rse;
      }

      logger.exiting(CLASSNAME, method, Boolean.valueOf(result));

      return result;
   }

   /**
    * @param method
    * @throws ResourceException
    * @throws SecurityException
    */
   private void createParentDirs (final File f)
         throws ResourceException, SecurityException
   {
      final String method = "createParentDirs";
      logger.entering(CLASSNAME, method, f);
      final File parent = f.getParentFile();
      if (!isFileExists(parent))
      {
         // if not -> creates all missing parent directories
         try
         {
            if (!parent.mkdirs())
            {
               final ResourceException re = new ResourceException("Failed to "
                     + "create all necessary file directories: "
                        + parent.toString());
               logger.throwing(CLASSNAME, method, re);
               throw re;
            }
            else
            {
               logger.finer("Created all necessary parant directories "
                     + parent.toString());
            }
         }
         catch (java.lang.SecurityException se)
         {
            final javax.resource.spi.SecurityException rse
                  = new javax.resource.spi.SecurityException("Failed to create "
                        + "all necessary file directories: "
                        + parent.toString());
            rse.initCause(se);
            logger.throwing(CLASSNAME, method, rse);
            throw rse;
         }
      }
      logger.exiting(CLASSNAME, method);
   }

   /** {@inheritDoc} */
   public String renameToTempFile (String file)
         throws ResourceException
   {
      final String method = "renameToTempFile";
      logger.entering(CLASSNAME, method, file);
      assertValid();

      final String result;
      try
      {
         final File tempFile = createTempFileInTempDir();
         tempFile.delete();
         final File srcFile = new File(file);

         if (srcFile.renameTo(tempFile))
         {
            result = tempFile.getAbsolutePath();
         }
         else
         {
            final ResourceException re = new ResourceException(
                  "The rename operation failed.");
            logger.throwing(CLASSNAME, method, re);
            throw re;
         }
      }
      catch (SecurityException se)
      {
         final javax.resource.spi.SecurityException rse
               = new javax.resource.spi.SecurityException("Security Manager "
                     + "does not allow the file '" + file + "' to be renamed or"
                           + " a temp file to be created");
         rse.initCause(se);
         logger.throwing(CLASSNAME, method, rse);
         throw rse;
      }
      catch (IOException ioe)
      {
         final ResourceException re = new ResourceException("The rename "
               + "operation failed.");
         re.initCause(ioe);
         logger.throwing(CLASSNAME, method, re);
         throw re;
      }

      logger.exiting(CLASSNAME, method, result);
      return result;
   }

   private File createTempFileInTempDir ()
         throws IOException
   {
      return  File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX, mTmpDir);
   }

   /**
    * Creates a new file. If the parameter <code>file</code> is null - creates
    * an empty file in the default temporary-file directory; if the parameter
    * <code>file</code> denotes to an existing directory - creates an empty file
    * in this directory; otherwise creates an empty file named
    * <code>file</code>.
    *
    * @param file null, directory name or file name.
    *
    * @return The name of the created file.
    *
    * @throws ResourceException thrown if the file could not be created.
    */
   private String createNewFile (final String file)
         throws ResourceException
   {
      final String method = "createNewFile";
      logger.entering(CLASSNAME, method, file);
      assertValid();
      final File resultFile;
      if (file == null)
      {
         resultFile = createTempDir(mTmpDir);
      }
      else
      {
         final File f = new File(file);
         if (isFileExists(f) && f.isDirectory())
         {
            resultFile = createTempDir(f);
         }
         else
         {
            resultFile = crateNewFile(f);
         }
      }
      final String result = resultFile.toString();
      logger.exiting(CLASSNAME, method, result);
      return result;
   }

   /**
    * Creates a new file <code>file</code>.
    * @param file The file to be created.
    * @return The <code>file</code>.
    * @throws ResourceException thrown if the file could not be created.
    */
   File crateNewFile (final File file)
         throws ResourceException
   {
      final String msg = "Failed to create '" + file.getAbsolutePath() + "'";
      try
      {
         if (!file.createNewFile())
         {
            final ResourceException re = new ResourceException(
               "Failed to create a new file. The file '" + file.toString()
                     + "' does already exist.");
            throw re;
         }
      }
      catch (IOException e)
      {
         final ResourceException re = new ResourceException(msg);
         re.initCause(e);
         throw re;
      }
      catch (java.lang.SecurityException se)
      {
         final javax.resource.spi.SecurityException rse
            = new javax.resource.spi.SecurityException(msg);
         rse.initCause(se);
         throw rse;
      }
      return file;
   }

   /**
    * Creates a new temp file within of the given temp dir.
    * @param tempDirFile The temp dir
    * @return The new temp file.
    * @throws ResourceException thrown if the file could not be created.
    */
   File createTempDir (final File tempDirFile)
         throws ResourceException, javax.resource.spi.SecurityException
   {
      final File result;
      final String msg = "Failed to create a new temp file, temp dir '"
         + tempDirFile.toString() + "'";
      try
      {
         result = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX,
               tempDirFile);
      }
      catch (IOException e)
      {
         final ResourceException re = new ResourceException(msg);
         re.initCause(e);
         throw re;
      }
      catch (java.lang.SecurityException se)
      {
         final javax.resource.spi.SecurityException rse
            = new javax.resource.spi.SecurityException(msg);
         rse.initCause(se);
         throw rse;
      }
      return result;
   }

   /**
    * Renames file <code>src</code> to <code>dest</code>.
    * @param src file to be renamed.
    * @param dest the new file name.
    * @return ResourceException in case of the rename operation failes.
    */
   private ResourceException rename (final File src, final File dest)
   {
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, "rename", new Object [] {src, dest});
      }

      ResourceException re = null;

      if (!src.renameTo(dest))
      {
         // probably the underlying native implementation does not support
         // renameTo on different file systems.
         try
         {
            // copy the file
            copyFilesChunk(src, dest, mFileTransferChunkSize);

            // delete the source file
            deleteFile(src);
         }
         catch (ResourceException r)
         {
            re = r;
         }
      }

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, "rename", re);
      }
      return re;
   }

   /**
    * @param file The original file.
    * @return The File instance that can be used for back up. The File does yet
    * not exist.
    * @throws SecurityException thown in case of security problem
    */
   public static File getBackupFile (final File file)
         throws SecurityException
   {
      File backupFile = getSuggestionForBackupFile(file);
      while (isFileExists(backupFile))
      {
         backupFile = getSuggestionForBackupFile(file);
      }
      return backupFile;
   }

   /**
    * @param file The original file.
    * @return The suggestion for a backup file.
    */
   public static File getSuggestionForBackupFile (final File file)
   {
      return new File(file.getParent(),
            file.getName() + Integer.toString(RANDOM.nextInt()));
   }

   /** {@inheritDoc} */
   public RandomAccessFile getRandomAccessFile (String file, String mode)
         throws ResourceException, FileNotFoundException
   {
      final String method = "getRandomAccessFile";
      logger.entering(CLASSNAME, method, new Object [] {file, mode});
      assertValid();

      final RandomAccessFileWrapper raf;
      try
      {
         raf = new RandomAccessFileWrapper(this, file, mode);
      }
      catch (java.lang.SecurityException se)
      {
         final javax.resource.spi.SecurityException rse
               = createReadAccessSecurityException(file, se);
         logger.throwing(CLASSNAME, method, rse);
         throw rse;
      }

      mCloseables.add(raf);
      logger.exiting(CLASSNAME, method, raf);
      return raf;
   }

   /** {@inheritDoc} */
   public FileInputStream getFileInputStream (String file)
         throws ResourceException, FileNotFoundException
   {
      final String method = "getFileInputStream";

      logger.entering(CLASSNAME, method, file);
      assertValid();

      final FileInputStreamWrapper fis;

      try
      {
         fis = new FileInputStreamWrapper(this, file);
      }
      catch (java.lang.SecurityException se)
      {
         final javax.resource.spi.SecurityException rse
               = createReadAccessSecurityException(file, se);
         logger.throwing(CLASSNAME, method, rse);
         throw rse;
      }

      mCloseables.add(fis);
      logger.exiting(CLASSNAME, method, fis);
      return fis;
   }

   /**
    * Sets this connection to the state cleaned up. All futher calls on the
    * public methods of this connection will throw a ResourceException.
    */
   public void cleanUp ()
   {
      logger.entering(CLASSNAME, "cleanUp");
      super.cleanUp();
      closeAllCloseables();
      logger.exiting(CLASSNAME, "cleanUp");
   }

   /**
    * Closess all RandomAccessFile handles, if yet not closed
    */
   private void closeAllCloseables ()
   {
      logger.entering(CLASSNAME, "closeAllCloseables");
      final Iterator itr = mCloseables.iterator();
      Closeable c = null;
      while (itr.hasNext())
      {
         c = (Closeable) itr.next();
         try
         {
            c.close();
         }
         catch (IOException e)
         {
            logger.log(Level.FINE, "Could not close the Closeable '" + c + "'.",
                  e);
         }
      }
      mCloseables.clear();

      logger.exiting(CLASSNAME, "closeAllCloseables");
   }

   /**
    * Removes the Closeable <code>c</code> from the list of closeable objects.
    * @param c Closeable to be removed from the list of closeable objects.
    */
   public void closeableClosed (Closeable c)
   {
      logger.entering(CLASSNAME, "closeableClosed", c);
      mCloseables.remove(c);
      logger.exiting(CLASSNAME, "closeableClosed");
   }

   /**
    * Deletes the supplied file or directory <code>file</code>.
    *
    * @param file The file or directory to be deleted.
    *
    * @return if and only if the file or directory is successfully deleted;
    *    false otherwise
    *
    * @throws ResourceException If a security manager does not allow the file to
    *    be deleted.
    */
   static boolean deleteFile (final File file)
         throws SecurityException
   {
      final String method = "deleteFile";
      logger.entering(CLASSNAME, method, file);
      final boolean result;
      try
      {
         result = file.delete();
      }
      catch (java.lang.SecurityException se)
      {
         final javax.resource.spi.SecurityException rse
               = new javax.resource.spi.SecurityException(
                     "The SecurityManager denies delete access to the file '"
                        + file.toString() + "'.");
         rse.initCause(se);
         logger.throwing(CLASSNAME, method, rse);
         throw rse;
      }
      logger.exiting(CLASSNAME, method, Boolean.valueOf(result));
      return result;
   }

   static void copyFilesChunk (File src, File dist, long chunkSize)
         throws ResourceException
   {
      final String method = "copyFiles";
      final boolean finer = logger.isLoggable(Level.FINER);
      if (finer)
      {
         logger.entering(CLASSNAME, method, new Object [] {src, dist});
      }
      FileInputStream fis = null;
      FileOutputStream fos = null;
      FileChannel srcChannel = null;
      FileChannel dstChannel = null;
      try
      {
         // Create input stream on the source
         fis = new FileInputStream(src);

         // Create output stream on the source
         fos = new FileOutputStream(dist);

         // Create channel on the source
         srcChannel = fis.getChannel();

         // Create channel on the destination
         dstChannel = fos.getChannel();

         // Determine the channel size
         final long size = srcChannel.size();

         logger.finer("srcChannel.size() " + size);
         long chunk = chunkSize < size ?  chunkSize : size;
         long currentpos = 0;
         long chunkTransfered = 1L;
         long transfered = 0;

         while (chunkTransfered > 0 && transfered != size)
         {
            // Copy file contents from source to destination
            chunkTransfered = dstChannel.transferFrom(srcChannel, currentpos,
                  chunk);
            currentpos = chunk + currentpos;

            if (finer)
            {
               logger.finer("Using chunk " + chunk + "Copied next chunk  "
                     + chunkTransfered + ", new pos  " + currentpos);
            }

            if (chunk != chunkTransfered)
            {
               final ResourceException re = new ResourceException(
                  "Failed to copy file '" + src.toString() + "' to '"
                     + dist.toString()
                     + "' Not all bytes could be transfered.");
               logger.throwing(CLASSNAME, method, re);
               throw re;
            }
            transfered = transfered + chunkTransfered;
            if (chunkSize >= size - transfered)
            {
               chunk = size - transfered;
            }

            if (finer)
            {
               logger.finer("Transfered " + transfered + " bytes from " + src
                     + " to " + dist + ".");
            }
         }
      }
      catch (IOException e)
      {
         final ResourceException re = new ResourceException(
            "Failed to copy file '" + src.toString() + "' to '"
               + dist.toString() + "'.");
         re.initCause(e);
         logger.throwing(CLASSNAME, method, re);
         throw re;
      }
      finally
      {
         // Close the channels
         IoUtil.close(srcChannel);
         IoUtil.close(dstChannel);

         // Close the streams
         IoUtil.close(fis);
         IoUtil.close(fos);
      }

      logger.exiting(CLASSNAME, method);
   }


   /** {@inheritDoc} */
   public String toString ()
   {
      return mStringified;
   }

   /** {@inheritDoc} */
   public int hashCode ()
   {
      return mIndex;
   }

   /** {@inheritDoc} */
   public boolean equals (Object obj)
   {
      return (obj instanceof FsConnectionImpl
            && ((FsConnectionImpl) obj).mIndex == this.mIndex);
   }

   static boolean isFileExists (final File file)
         throws SecurityException
   {
      boolean result = false;
      try
      {
         result = file.exists();

         if (!result)
         {
            final File parent = file.getParentFile();
            if (parent != null)
            {
               final File [] lst = parent.listFiles(new SingleFileFilter(file));
               if (lst != null && lst.length == 1 && lst[0].equals(file))
               {
                  result = true;
               }
            }
         }
      }
      catch (java.lang.SecurityException se)
      {
         final javax.resource.spi.SecurityException rse
               = createReadAccessSecurityException(file.toString(), se);
         throw rse;
      }
      return result;
   }

   private static final class SingleFileFilter
         implements FileFilter
   {
      private final File mFile;

      public SingleFileFilter (File org)
      {
         mFile = org;
      }
      public boolean accept (File pathname)
      {
         return mFile.equals(pathname);
      }
   }

}
