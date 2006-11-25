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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;

import org.jcoderz.commons.connector.ManagedConnectionFactoryBase;
import org.jcoderz.commons.connector.UserPassword;


/**
 * Implements the {@link javax.resource.spi.ManagedConnectionFactory} interface.
 *
 */
public class FsManagedConnectionFactoryImpl
      extends ManagedConnectionFactoryBase
{
   /** Min value for chunk size, just 1KByte. */
   public static final long FILE_TRANSFER_CHUNK_SIZE_MIN_VALUE = 1000L;
   /** Max value for chunk size, just {@link Integer#MAX_VALUE}. */
   public static final long FILE_TRANSFER_CHUNK_SIZE_MAX_VALUE
         = Integer.MAX_VALUE;

   /** The full qualified name of this class. */
   private static final String CLASSNAME
         = FsManagedConnectionFactoryImpl.class.getName();

   /** The logger to use. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   /** Generated <code>serialVersionUID</code>. */
   private static final long serialVersionUID = 3258409517263172660L;

   private final Properties mProps = new Properties();

   protected ManagedConnection createManagedConnectionImpl (UserPassword up,
         ConnectionRequestInfo cri)
   {
      final boolean finer = logger.isLoggable(Level.FINER);
      if (finer)
      {
         logger.entering(CLASSNAME, "createManagedConnectionImpl",
               new Object [] {up, cri});
      }
      final ConnectionRequestInfo info;

      if (cri == null)
      {
         final FsConnectionRequestInfo fsCri = new FsConnectionRequestInfo();
         fsCri.setUserPassword(up);
         info = fsCri;
      }
      else
      {
         info = cri;
      }

      final ManagedConnection result = new FsManagedConnectionImpl(this, up,
            cri); // CHECKME: use info variable?

      if (finer)
      {
         logger.exiting(CLASSNAME, "createManagedConnectionImpl", result);
      }
      return result;
   }


   protected boolean isMatchingManagedConnection (ManagedConnection mc,
         UserPassword up, ConnectionRequestInfo cri)
         throws ResourceException
   {
      final boolean finer = logger.isLoggable(Level.FINER);
      if (finer)
      {
         logger.entering(CLASSNAME, "isMatchingManagedConnection",
               new Object [] {mc, up, cri});
      }
      boolean result = false;

      if ((mc instanceof FsManagedConnectionImpl)
            && (cri instanceof FsConnectionRequestInfo))
      {
         final FsManagedConnectionImpl mci = (FsManagedConnectionImpl) mc;

         if (mci.getUserPassword().equals(up))
         {
            result = true;
         }
      }

      if (finer)
      {
         logger.exiting(CLASSNAME, "isMatchingManagedConnection",
               Boolean.valueOf(result));
      }

      return result;
   }


   /**
    * Creates a new FsConnectionFactoryImpl instance.
    * @param cm The ConectionManager to use.
    * @throws ResourceException thrown in error cases.
    *
    * @see org.jcoderz.commons.connector.ManagedConnectionFactoryBase#createConnectionFactoryImpl(javax.resource.spi.ConnectionManager)
    */
   protected Object createConnectionFactoryImpl (ConnectionManager cm)
         throws ResourceException
   {
      final boolean finer = logger.isLoggable(Level.FINER);
      if (finer)
      {
         logger.entering(CLASSNAME, "createConnectionFactoryImpl", cm);
      }

      checkProps();
      final FsConnectionFactoryImpl result = new FsConnectionFactoryImpl(cm,
            this, mProps);
      if (finer)
      {
         logger.exiting(CLASSNAME, "createConnectionFactoryImpl", result);
      }
      return result;
   }

   private void checkProps ()
   {
      if (!mProps.containsKey(
            FsConnectionFactory.PROP_FILE_TRANSFER_CHUNK_SIZE))
      {
         mProps.setProperty(FsConnectionFactory.PROP_FILE_TRANSFER_CHUNK_SIZE,
               String.valueOf(
                     FsConnectionFactory.FILE_TRANSFER_CHUNK_SIZE_DEF_VALUE));
      }
   }

   /**
    * Sets the chunk size used while file transfering.
    * If a file size exceeds the chunk size, the file will be transfered chunk
    * by chunk until all bytes will have been transfered. The Min value for this
    * property is
    * {@link FsManagedConnectionFactoryImpl#FILE_TRANSFER_CHUNK_SIZE_MIN_VALUE},
    * the Max value is
    * {@link FsManagedConnectionFactoryImpl#FILE_TRANSFER_CHUNK_SIZE_MAX_VALUE}.
    * If this property is not defined in the deployment descriptor the file
    * connector will use the default value
    * {@linkplain FsConnectionFactory#FILE_TRANSFER_CHUNK_SIZE_DEF_VALUE}.
    * Too small value will probably slow the performance down, and too large
    * value may cause a resource's allocation problem. The value of this
    * property should be adjusted to the underlying os, file system and
    * available memory.
    * A connection client can overwrite this value by passing a property
    * object while retrieving a connection by calling the method
    * {@link FsConnectionFactory#getConnection(Properties)}.
    *
    * @param size the new chunk size to set.
    */
   public void setFileTransferChunkSize (Long size)
   {
      final String method = "setFileTransferChunkSize";
      logger.entering(CLASSNAME, method, size);

      final String usedSize;
      if (size.longValue() < FILE_TRANSFER_CHUNK_SIZE_MIN_VALUE
            || size.longValue() > FILE_TRANSFER_CHUNK_SIZE_MAX_VALUE)
      {
         if (logger.isLoggable(Level.FINE))
         {
            logger.fine("The given file transfer chunk " + size
                  + " could not be set. The chunk size should be between "
                  + FILE_TRANSFER_CHUNK_SIZE_MIN_VALUE + " and "
                  + FILE_TRANSFER_CHUNK_SIZE_MAX_VALUE
                  + ". The file connector wiil use the default chunk size "
                  + FsConnectionFactory.FILE_TRANSFER_CHUNK_SIZE_DEF_VALUE);
         }
         usedSize = String.valueOf(
               FsConnectionFactory.FILE_TRANSFER_CHUNK_SIZE_DEF_VALUE);
      }
      else
      {
         usedSize = size.toString();
      }

      mProps.setProperty(FsConnectionFactory.PROP_FILE_TRANSFER_CHUNK_SIZE,
            String.valueOf(usedSize));

      logger.exiting(CLASSNAME, method);
   }

   /**
    * Defines the temporary directory to be used by this connector. The default
    * value will be retrieved from the system property 'java.io.tmpdir'.
    * A connection client can overwrite this value by passing the property
    * {@linkplain FsConnectionFactory#PROP_TEMP_DIR} while retrieving a
    * connection by calling the method
    * {@link FsConnectionFactory#getConnection(Properties)}.
    *
    * @param dir temp dir to set.
    */
   public void setTempDir (String dir)
   {
      logger.entering(CLASSNAME, "setTempDir", dir);
      mProps.setProperty(FsConnectionFactory.PROP_TEMP_DIR, dir);
      logger.exiting(CLASSNAME, "setTempDir");
   }
}
