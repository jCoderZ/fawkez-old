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
package org.jcoderz.phoenix.jabber;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.GroupChat;
import org.jivesoftware.smack.XMPPConnection;

/**
 * Simply sends a jabber message in a jabber group.
 *
 * <p>To setup different than the default connection call
 * <code>JabberConnection.getInstance().setup(...)</code> prior the
 * first call to say().</p>
 *
 * <p>This implementation tries to establish a static connection to a
 * group chat and re-use this connection for further messages.</p>
 *
 * <p>By design there is only one connection per VM/classloader possible.</p>
 *
 * @author Andreas Mandel
 */
public final class Jabber
{
   private static final int JABBER_DEFAULT_PORT = 5222;
   private static final String CLASSNAME = Jabber.class.getName();
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private Jabber ()
   {
      // main class - no instances allowed.
   }

   /**
    * Shortcut to send message directly from the command line.
    * TODO: Allow different chat server.
    * @param args
    * @throws Exception
    */
   public static void main (String[] args)
         throws Exception
   {
      JabberConnection.getInstance().say(args[0]);
   }

   /**
    * Sends the given text message to the GroupChat.
    * @param msg the message to send.
    * @throws RuntimeException if sending fails (even after retry).
    */
   public static void say (String msg)
   {
      JabberConnection.getInstance().say(msg);
   }

   private static class JabberConnection
   {
      private static final JabberConnection INSTANCE = new JabberConnection();
      private static final int GRACEFUL_PERIOD = 1000;
      
      private final String mHostname;
      private String mJabberUserName = "cc";
      private String mJabberUserPassword = "cc42";
      private String mJabberServerName = "jabber.org";
      private String mJabberHostAddress = "jabber.org";
      private int mJabberHostPort = JABBER_DEFAULT_PORT;
      private String mJabberMucName = "jcoderz@conference.jabber.org";

      private String mJabberMucAlias;

      private XMPPConnection mConnection;
      private GroupChat mGroupChat;

      JabberConnection ()
      {
         String localhost;
         try
         {
            localhost = InetAddress.getLocalHost().getHostName();
         }
         catch (Exception ex)
         {
            localhost = "localhost";
         }
         mHostname = localhost;
         mJabberMucAlias
               = System.getProperty("user.name", "cruise.control")
                  + "@" + mHostname;
      }

      /**
       * Returns the one and only instance of the jabber connection.
       * @return the one and only instance of the jabber connection.
       */
      public static JabberConnection getInstance ()
      {
         return INSTANCE;
      }

      /**
       * Setup the connection parameters to be used.
       * If there was already a connection to a group chat, this
       * connection is closed.
       * The connection is established after setting the bew parameters.
       * Be prepared to catch a runtime exception if the connection fails.
       * @param jabberHostName The host name of the JabberServer to connect to
       *        might also be it's numeric ip address.
       * @param jabberHostPort The port to connect to (most likely 5222)
       * @param jabberServerName The "virtual" server name of the jabber
       *        server (the part after the @ in the JIDs)
       * @param jabberUserName The user name to connect as. This is
       *        the part before the @ in the JID.
       * @param jabberUserPassword The password to use.
       * @param jabberMucName The full qualified name of the MUC.
       *        (eg. talk@conf.jabber.org)
       */
      public synchronized void setup (String jabberHostName,
            int jabberHostPort, String jabberServerName, String jabberUserName,
            String jabberUserPassword, String jabberMucName)
      {
         clear();
         mJabberHostAddress = jabberHostName;
         mJabberHostPort = jabberHostPort;
         mJabberServerName = jabberServerName;
         mJabberUserName = jabberUserName;
         mJabberUserPassword = jabberUserPassword;
         mJabberMucName = jabberMucName;
         checkConnection();
      }

      /**
       * Sends the given text message to the GroupChat
       * @param message the message to send.
       * @throws RuntimeException if sending fails (even after retry).
       */
      public synchronized void say (String message)
      {
         try
         {
            checkConnection();
            mGroupChat.sendMessage(message);
         }
         catch (Exception ex)
         {
            logger.log(Level.WARNING, "Exception sending message."
                  + " Will retry", ex);
            clear();
            try
            {
               checkConnection();
               mGroupChat.sendMessage(message);
            }
            catch (Exception e)
            {
               clear();
               throw new RuntimeException("Jabber send message failed fatal!",
                     e);
            }
         }
      }

      public void finalize ()
            throws Throwable
      {
         clear();
         super.finalize();
      }


      /**
       * Checks and ensures that a connection is established.
       * @throws RuntimeException if connecting fails (even after retry).
       */
      public void checkConnection ()
            throws RuntimeException
      {
         try
         {
            checkConnectionRaw();
         }
         catch (Exception ex)
         {
            logger.log(Level.WARNING, "Exception while reconnecting (1).", ex);
            mGroupChat = null;
         }

         try
         {
            checkConnectionRaw();
         }
         catch (Exception ex)
         {
            logger.log(Level.WARNING, "Exception while reconnecting (2).",
                  ex);
            mConnection = null;
            mGroupChat = null;
         }
         try
         {
            checkConnectionRaw();
         }
         catch (Exception ex)
         {
            mConnection = null;
            mGroupChat = null;
            throw new RuntimeException("Jabber connect failed fatal!", ex);
         }
      }


      private void checkConnectionRaw ()
            throws Exception
      {
         if (mConnection == null || !mConnection.isConnected())
         {
            clear();
            mConnection = new XMPPConnection (// CHECKME: new SSLXMPPConnection(
                  mJabberHostAddress, mJabberHostPort, mJabberServerName);
            mGroupChat = null;
            logger.info("New connection to jabber server. TLS: "
                  + mConnection.isUsingTLS() + " secure: "
                  + mConnection.isSecureConnection());
            if (!mConnection.isConnected())
            {
               Thread.sleep(GRACEFUL_PERIOD);
            }
         }

         if (!mConnection.isAuthenticated())
         {
            mConnection.login(mJabberUserName, mJabberUserPassword, mHostname);
            mGroupChat = null;
            logger.info("Login to jabber server. ("
                  + mConnection.isAuthenticated() + ").");
         }

         if (mGroupChat == null)
         {
            mGroupChat = mConnection.createGroupChat(mJabberMucName);
            logger.info("New group chat generated.");
         }

         if (!mGroupChat.isJoined())
         {
            mGroupChat.join(mJabberMucAlias);
            if (!mGroupChat.isJoined())
            {
               Thread.sleep(GRACEFUL_PERIOD);
            }
            logger.info("Joined to group chat. (" + mGroupChat.isJoined()
                  + ").");
         }
      }

      private void clear ()
      {
         try
         {
            if (mGroupChat != null)
            {
               mGroupChat.leave();
            }
         }
         catch (Exception ex)
         {
            // be silent
         }
         mGroupChat = null;
         try
         {
            if (mConnection != null)
            {
               mConnection.close();
            }
         }
         catch (Exception ex)
         {
            // be silent
         }
         mConnection = null;
      }

   }


}
