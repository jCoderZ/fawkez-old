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

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

import org.jcoderz.commons.connector.ManagedConnectionFactoryBase;


/**
 * Implements the {@link FsConnectionFactory} interface.
 *
 */
public class FsConnectionFactoryImpl
      implements FsConnectionFactory
{
   private static final String METHOD_GET_CONNECTION = "getConnection";

   /** The full qualified name of this class. */
   private static final transient String CLASSNAME
         = FsConnectionFactoryImpl.class.getName();
   /** The logger to use. */
   private static final transient Logger logger = Logger.getLogger(CLASSNAME);

   /** The <code>serialVersionUID</code>. */
   private static final long serialVersionUID = 1L;

   /** Properties from the deployment descriptor, or default. */
   private final Properties mDdProps;

   /** Reference to this ConnectionFactory. */
   private Reference mReference;

   /**
    * The ConnectionManager to use.
    * In case of <b>two tier scenario</b> the  ConnectionManager is an instance
    * of the DefaultConnectionManager.
    * In case of <b>three tier scenario</b> this Manager is provided by
    * the Application Server.
    */
   private final ConnectionManager mConnectionManager;

   /** The underlying ManagedConnectionFactory. */
   private final ManagedConnectionFactoryBase mMcf;

   /**
    * Constructs a new FsConnectionFactoryImpl.
    *
    * @param cm The ConnectionManager to use.
    * @param mcf The underlying ManagedConnectionFactory.
    * @param props The properties to use. These properties come from the
    * deployment descriptor if ones defined; otherwise these the default
    * properties.
    */
   public FsConnectionFactoryImpl (final ConnectionManager cm,
         final ManagedConnectionFactoryBase mcf, Properties props)
   {
      final boolean finer = logger.isLoggable(Level.FINER);
      if (finer)
      {
         logger.entering(CLASSNAME, "FsConnectionFactoryImpl",
               new Object [] {cm, mcf, props});
      }
      mConnectionManager = cm;
      mMcf = mcf;
      mDdProps = props;

      if (finer)
      {
         logger.exiting(CLASSNAME, "FsConnectionFactoryImpl");
      }
   }

   /**
    * Returns a connection to the File System.
    * @return a connection to the File System.
    * @throws ResourceException Failed to get a connection.
    * @see FsConnectionFactory#getConnection()
    */
   public FsConnection getConnection ()
         throws ResourceException
   {
      logger.entering(CLASSNAME, METHOD_GET_CONNECTION);

      final FsConnectionRequestInfo fsCri = new FsConnectionRequestInfo(
            mDdProps);
      final FsConnection result = allocateConnection(fsCri);

      logger.exiting(CLASSNAME, METHOD_GET_CONNECTION, result);
      return result;
   }


   /**
    * @see org.jcoderz.commons.connector.file.FsConnectionFactory#getConnection(java.util.Properties)
    */
   public FsConnection getConnection (Properties props)
         throws ResourceException
   {
      logger.entering(CLASSNAME, METHOD_GET_CONNECTION, props);

      // Merge the properties:
      // DdProps --> Client Props
      final Properties p = new Properties();
      p.putAll(mDdProps);
      if (props != null)
      {
         p.putAll(props);
      }

      if (logger.isLoggable(Level.FINER))
      {
         logger.finer("Using properties " + p.toString());
      }
      final FsConnectionRequestInfo fsCri = new FsConnectionRequestInfo(p);
      final FsConnection result = allocateConnection(fsCri);

      logger.exiting(CLASSNAME, METHOD_GET_CONNECTION, result);
      return result;
   }

   /**
    * @param fsCri
    * @return
    * @throws ResourceException
    */
   private FsConnection allocateConnection (final FsConnectionRequestInfo fsCri)
         throws ResourceException
   {
      fsCri.setUserPassword(mMcf.getUserPassword());
      final FsConnection result = (FsConnection) mConnectionManager
            .allocateConnection(mMcf, fsCri);
      return result;
   }

   /**
    * Sets the reference for this ConnectionFactory.
    * This method is called by deployment code.
    *
    * @param reference The reference for this ConnectionFactory.
    *
    * @see javax.resource.Referenceable#setReference(javax.naming.Reference)
    */
   public void setReference (Reference reference)
   {
      mReference = reference;
   }

   /**
    * Returns the non-null Reference of this for this ConnectionFactory.
    * @return The non-null Reference of this for this ConnectionFactory.
    * @throws NamingException never thrown.
    * @see javax.naming.Referenceable#getReference()
    */
   public Reference getReference ()
         throws NamingException
   {
      return mReference;
   }

   /**
    * @return ConnectionManager.
    */
   public ConnectionManager getConnectionManager ()
   {
      return mConnectionManager;
   }
}
