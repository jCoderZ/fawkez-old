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

import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509KeyManager;

import org.jcoderz.commons.connector.InitializingSslFailedException;
import org.jcoderz.commons.util.Assert;


/**
 * This class implements the X509KeyManager interface and
 * allows to select a specific key for client authentification.
 *
 */
public class HttpsKeyManager
      implements X509KeyManager
{
   /** The parent X509KeyManager */
   private final X509KeyManager mManager;
   /** The KeyStore this KeyManager uses */
   private final KeyStore mKeyStore;
   private final String mKeyAlias;
   private final String mKeyPassword;

   /** Lazy init cache for private key. */
   private PrivateKey mPrivateKey;

   /**
    * Constructor.
    *
    * @param  parent       the parent X509KeyManager
    * @param  keystore     the KeyStore we derive our client certs and keys from
    * @param  keyAlias     the alias for key in use
    * @param  keyPassword  the password used for alias
    */
   public HttpsKeyManager (
         X509KeyManager parent, KeyStore keystore,
         String keyAlias, String keyPassword)
   {
      mManager = parent;
      mKeyStore = keystore;
      mKeyAlias = keyAlias;
      mKeyPassword = keyPassword;
   }

   /**
    * Gets the one alias set in constructor.
    * Currently,  keyType and issuers are both ignored.
    *
    * @param  keyType  the type of private key the server expects (RSA,
    *                  DSA, etc.)
    * @param  issuers  the CA certificates we are narrowing our selection
    *                  on.
    * @return          the ClientAliases value
    */
   public String[] getClientAliases (String keyType, Principal[] issuers)
   {
      return new String[] {mKeyAlias};
   }

   /**
    * Gets the list of server aliases for the SSLServerSockets.
    *
    * @param  keyType  the type of private key the server expects (RSA,
    *                  DSA, etc.)
    * @param  issuers  the CA certificates we are narrowing our selection
    *                  on.
    * @return          the ServerAliases value
    */
   public String[] getServerAliases (String keyType, Principal[] issuers)
   {
      return mManager.getServerAliases(keyType, issuers);
   }

   /**
    * Gets the Certificate chain for a particular alias.
    *
    * @param  alias  the client alias
    * @return        the CertificateChain value
    */
   public X509Certificate[] getCertificateChain (String alias)
   {
      assertAlias(alias);
      final X509Certificate[] chain;
      try
      {
         final Certificate[] certs = mKeyStore.getCertificateChain(alias);
         Assert.notNull(certs, "certs");
         chain = new X509Certificate[certs.length];
         for (int i = 0; i < chain.length; i++)
         {
            chain[i] = (X509Certificate) certs[i];
         }
         // chain = (X509Certificate[])mKeyStore.getCertificateChain(alias);

      }
      catch (KeyStoreException kse)
      {
         final String reason
               = "Unable to obtain certificate chain for alias "
                  + "<" + alias + ">";
         final InitializingSslFailedException sse
               = new InitializingSslFailedException(reason, kse);
         throw sse;
      }
      return chain;
   }

   /**
    * Gets the Private Key for a particular alias.
    *
    * @param  alias  the client alias
    * @return        the PrivateKey value
    */
   public PrivateKey getPrivateKey (String alias)
   {
      assertAlias(alias);
      if (mPrivateKey == null)
      {
         try
         {
            mPrivateKey = (PrivateKey) mKeyStore.getKey(
                  alias, mKeyPassword.toCharArray());
         }
         catch (GeneralSecurityException gse)
         {
            final String reason
                  = "Unable to obtain private key for alias "
                     + "<" + alias + ">";
            final InitializingSslFailedException sse
                  = new InitializingSslFailedException(reason, gse);
            throw sse;
         }
      }
      return mPrivateKey;
   }

   /** {@inheritDoc} */
   public String chooseClientAlias (
         String[] keyType, Principal[] issuers, Socket socket)
   {
      return mKeyAlias;
   }

   /** {@inheritDoc} */
   public String chooseServerAlias (
         String keyType, Principal[] issuers, Socket socket)
   {
      return mManager.chooseServerAlias(keyType, issuers, socket);
   }

   /**
    * Asserts that the given alias is the one set for constructor.
    * @param alias  the alias to assert
    */
   private void assertAlias (String alias)
   {
      if (!alias.equals(mKeyAlias))
      {
         final String reason
               = "Unexpected alias <" + alias + ">";
         final InitializingSslFailedException sse
               = new InitializingSslFailedException(reason);
         throw sse;
      }
   }
}
