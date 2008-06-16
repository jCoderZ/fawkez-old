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

import java.rmi.RemoteException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;

import org.jcoderz.commons.InternalErrorException;
import org.jcoderz.commons.RemoteCallFailureException;
import org.jcoderz.commons.connector.ConnectorConfiguration;
import org.jcoderz.commons.config.ConfigurationServiceContainerFactory;
import org.jcoderz.commons.config.ConfigurationServiceInterface;


/**
 * This class provides utility functions for the file system connector.
 *
 * @author Michael Griffel
 */
public final class FsConnectionUtil
{
   /** The JNDI name of the file system connector. */
   public static final String FILE_SYSTEM_CONNECTOR_JNDI_NAME
         = "FileSystemConnector";

   /** The JNDI name of the file system connector. */
   public static final String FILE_SYSTEM_CONNECTOR_EIS_NAME
         = "java:comp/env/eis/FileSystemConnector";

   /** The full qualified name of this class. */
   private static final String CLASSNAME
         = FsConnectionUtil.class.getName();

   /** The logger to use. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private static Properties sProps;
   private static boolean sConfigRead;

   private FsConnectionUtil ()
   {
      // no instances allowed - provides only static helper methods.
   }

   /**
    * Returns a file system connection.
    * @return a file system connection.
    */
   public static FsConnection getFileSystemConnection ()
   {
      return getFileSystemConnection(getConfigProperties());
   }

   /**
    * Returns a file system connection.
    * @param props Connection properties to use
    * @return a file system connection.
    */
   public static FsConnection getFileSystemConnection (final Properties props)
   {
      InitialContext context = null;
      final FsConnection connection;
      try
      {
         context = new InitialContext();
         final FsConnectionFactory factory
               = (FsConnectionFactory) context.lookup(
                  FILE_SYSTEM_CONNECTOR_EIS_NAME);
         connection = factory.getConnection(props);
      }
      // possible exceptions: ResourceException, NamingException
      catch (Exception e)
      {
         // CHECKME: catch ResourceException? provider connector specific ex.?
         throw new InternalErrorException(
               "Failed to create file system connection", e);
      }
      finally
      {
         if (context != null)
         {
            try
            {
               context.close();
            }
            catch (NamingException e)
            {
               logger.log(Level.FINER,
                     "Failed to close InitialContext: " + e, e);
            }
         }
      }
      return connection;
   }

   /**
    * Closes the file system connection (safe).
    *
    * This method tries to close the given file system connection and
    * if an ResourceException occurs a message with the level
    * {@link Level#FINE} is logged. It's safe to pass a
    * <code>null</code> reference for the argument.
    *
    * @param in the file system connection that should be closed.
    */
   public static void close (FsConnection in)
   {
      if (in != null)
      {
         try
         {
            in.close();
         }
         catch (ResourceException x)
         {
            logger.log(Level.FINE, "Error while closing file "
                  + "system connection: FsConnection.close()", x);
         }
      }
   }

   /**
    * Gets the connector configuration obtained from config service.
    *
    * @return ConnectorConfiguration providing getter methods for all
    *          connector parameter values defined
    */
   static ConnectorConfiguration getConfiguration ()
   {
      final ConnectorConfiguration result;
      final ConfigurationServiceInterface configService
            = ConfigurationServiceContainerFactory.createLocalService();
      try
      {
         result = (ConnectorConfiguration)
               configService.getServiceConfiguration(
            "org.jcoderz.commons.connector.ConnectorConfiguration");
      }
      catch (RemoteException e)
      {
         throw new RemoteCallFailureException(e);
      }
      return result;
   }

   private static synchronized Properties getConfigProperties ()
   {
      if (!sConfigRead)
      {
         final ConnectorConfiguration cc = getConfiguration();
         final String tmpDir = cc.getFileTempDir();

         logger.fine("Using temp dir from config '" + tmpDir + "'.");
         if (tmpDir != null && tmpDir.length() > 0 && !tmpDir.equals("_EMPTY_"))
         {
            sProps = new Properties();
            sProps.put(FsConnectionFactory.PROP_TEMP_DIR, tmpDir);
         }

         sConfigRead = true;
      }

      return sProps;
   }
}
