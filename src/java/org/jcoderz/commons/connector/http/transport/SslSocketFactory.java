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
package org.jcoderz.commons.connector.http.transport;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.jcoderz.commons.connector.InitializingSslFailedException;
import org.jcoderz.commons.util.Assert;



/**
 * Factory used to create SSLSocket.
 */
public final class SslSocketFactory
      implements SecureProtocolSocketFactory
{
   /** The class name used for logging */
   private static final String
         CLASSNAME = SslSocketFactory.class.getName();
   /** The logger in use */
   private static final transient Logger
         logger = Logger.getLogger(CLASSNAME);

   /** Cache for SSLSocketFactories */
   private static final ThreadLocal SSL_SOCKET_FACTORIES = new ThreadLocal();

   private final String mKeyStoreLocation;
   private final String mKeyStorePassword;
   private final String mTrustStoreLocation;
   private final String mTrustStorePassword;
   private final String mKeyAlias;
   private final String mKeyPassword;
   private KeyStore mKeyStore = null;
   private KeyStore mTrustStore = null;

   /**
    * Constructor.
    * @param keyStoreLocation
    *          the location of the key store in use
    * @param keyStorePassword
    *          the password of the key store in use
    * @param trustStoreLocation
    *          the location of the trust store in use
    * @param trustStorePassword
    *          the password of the trust store in use
    * @param keyAlias
    *          the alias of the key in use
    * @param keyPassword
    *          the password of the key in use
    */
   public SslSocketFactory (
         String keyStoreLocation,
         String keyStorePassword,
         String trustStoreLocation,
         String trustStorePassword,
         String keyAlias,
         String keyPassword)
   {
      Assert.notNull(keyStoreLocation, "keyStoreLocation");
      Assert.notNull(keyStorePassword, "keyStorePassword");
      Assert.notNull(trustStoreLocation, "trustStoreLocation");
      Assert.notNull(trustStorePassword, "trustStorePassword");
      Assert.notNull(keyAlias, "keyAlias");
      Assert.notNull(keyPassword, "keyPassword");
      mKeyStoreLocation = keyStoreLocation;
      mKeyStorePassword = keyStorePassword;
      mTrustStoreLocation = trustStoreLocation;
      mTrustStorePassword = trustStorePassword;
      mKeyAlias = keyAlias;
      mKeyPassword = keyPassword;
   }

  /**
   * Constructor.
   * @param keyStore the keystore to use
   * @param trustStore the truststore in use
   * @param keyAlias the alias of the key in use
   * @param keyPassword the password of the key in use
   */
   public SslSocketFactory (
         KeyStore keyStore,
         KeyStore trustStore,
         String keyAlias,
         String keyPassword)
   {
      Assert.notNull(keyStore, "keyStore");
      Assert.notNull(keyAlias, "keyAlias");
      Assert.notNull(keyPassword, "keyPassword");
      mKeyStore = keyStore;
      mTrustStore = trustStore;
      mKeyAlias = keyAlias;
      mKeyPassword = keyPassword;
      mKeyStoreLocation = null;
      mKeyStorePassword = null;
      mTrustStoreLocation = null;
      mTrustStorePassword = null;
   }

   /**
    * Gets a CtsKeyManager as a specific X509KeyManager for the alias in use.
    *
    * @return KeyManager[]  contains only one KeyManager for the alias in use
    * @throws GeneralSecurityException
    *          in case of an keystore failure
    */
   private KeyManager[] getKeyManagers ()
   {
      final KeyManager manager
            = new HttpsKeyManager(null, mKeyStore, mKeyAlias, mKeyPassword);
      final KeyManager[] managers = {manager};
      return managers;
   }

   /**
    * Gets the TrustManagers for the specified algorithm.
    *
    * @return TrustManager[]  the TrustManger for the algorithm
    * @throws GeneralSecurityException
    *          in case of an keystore failure
    */
   private TrustManager[] getTrustManagers ()
         throws GeneralSecurityException
   {
      final TrustManagerFactory tmf
            = TrustManagerFactory.getInstance(
                  Security.getProperty("ssl.TrustManagerFactory.algorithm"));
      tmf.init(mTrustStore);
      return tmf.getTrustManagers();
   }

   private SSLSocketFactory getSslSocketFactory ()
         throws IOException, FileNotFoundException
   {
      SSLSocketFactory result;
      result = (SSLSocketFactory) SSL_SOCKET_FACTORIES.get();

      if (result == null)
      {
         logger.fine("Creating new SSL_SOCKET_FACTORY for Thread.");
         SSLContext ctx = null;
         try
         {
            // loading keystore/truststore if necessary (test mode only!!)
            if (mKeyStore == null)
            {
               logger.finest("Loading keystore from file system - "
                     + mKeyStoreLocation);
               final char[] passphraseKeyStore
                     = mKeyStorePassword.toCharArray();
               mKeyStore = KeyStore.getInstance("JKS");
               mKeyStore.load(new FileInputStream(
                     mKeyStoreLocation), passphraseKeyStore);
            }
            if (mTrustStore == null)
            {
               logger.finest("Loading truststore from file system - "
                     + mTrustStoreLocation);
               final char[] passphraseTrustStore
                     = mTrustStorePassword.toCharArray();
               mTrustStore = KeyStore.getInstance("JKS");
               mTrustStore.load(new FileInputStream(
                     mTrustStoreLocation), passphraseTrustStore);
            }

            if (!mKeyStore.containsAlias(mKeyAlias))
            {
               final String reason
                     = "Keystore does not contain key for alias "
                        + "<" + mKeyAlias + ">";
               final InitializingSslFailedException sse
                     = new InitializingSslFailedException(reason);
               throw sse;
            }
            ctx = SSLContext.getInstance("TLS");
            ctx.init(getKeyManagers(), getTrustManagers(), null);
         }
         catch (GeneralSecurityException gse)
         {
            final String reason = gse.getMessage();
            final InitializingSslFailedException sse
                  = new InitializingSslFailedException(reason, gse);
            throw sse;
         }
         result = ctx.getSocketFactory();
         SSL_SOCKET_FACTORIES.set(result);
      }
      return result;
   }

   /**
    * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int, org.apache.commons.httpclient.params.HttpConnectionParams)
    */
   public Socket createSocket (
         String host, int port, InetAddress localAddress ,
         int localPort, HttpConnectionParams params)
         throws IOException, UnknownHostException, ConnectTimeoutException
   {
      // This is an IBM JSSE workaround: SSLSocket.connect() does not
      // connect the socket in the IBM JSSE implementation, so we
      // first connect a plain TCP socket and then layer it with an
      // SSL Socket.
      final Socket tcpSock = new Socket();
      final SocketAddress endPoint = new InetSocketAddress(host, port);
      tcpSock.connect(endPoint, params.getConnectionTimeout());
      final Socket sock
            = getSslSocketFactory().createSocket(tcpSock, host, port, true);
      return sock;
   }

   /**
    * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int)
    */
   public Socket createSocket (String host, int port)
         throws IOException, UnknownHostException
   {
      throw new UnsupportedOperationException("Method not supported");
   }

   /**
    * @see org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory#createSocket(java.net.Socket, java.lang.String, int, boolean)
    */
   public Socket createSocket (
         Socket socket, String host, int port, boolean autoClose)
   {
      throw new UnsupportedOperationException("Method not supported");
   }

   /**
    * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int)
    */
   public Socket createSocket (
         String arg0, int arg1, InetAddress arg2, int arg3)
         throws IOException, UnknownHostException
   {
      throw new UnsupportedOperationException("Method not supported");
   }
}
