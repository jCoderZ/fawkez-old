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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class defines a simple http server used for testing
 * the HttpConnectionImpl based on the commons-httpclient lib.
 */
public class SimpleServer
      extends Thread
{
   /** Class name used for logging. */
   private static final String CLASSNAME
         = SimpleServer.class.getName();
   /** Logger in use. */
   private static final Logger logger
         = Logger.getLogger(CLASSNAME);
   private static final int SOCKET_TIMEOUT = 10000;
   private static final int JOIN_TIMEFRAME = 5000;
   private static final int WAITING_LOOPS = 10;
   /** Thread for stopping the server. */
   private final Terminator mTerminator;
   /** Flag indicating that the server have to be stopped. */
   private boolean mDoStop = false;
   /** Used for sychronize stopping the server. */
   private final Object mMonitor = new Object();
   /** Handler for client requests. */
   private final Map mHandler = new HashMap();
   /** Server port. */
   private final int mPort;
   /** Flag indicating if the server has been started. */
   private volatile boolean mServerStarted = false;


   /**
    * Constructor.
    * @param port
    *          th server port in use
    */
   public SimpleServer (int port)
   {
      mPort = port;
      mTerminator = new Terminator(this);
   }

   /**
    * Main method. Starts a thread for terminating the server and starts
    * a ClientHandler thread for every incomming request.
    * @param args
    *          args include the port number to use
    */
   public static void main (String[] args)
   {
      if (args.length != 1)
      {
         logger.info("Usage: SimpleServer <port>");
         return;
      }
      final SimpleServer server = new SimpleServer(Integer.parseInt(args[0]));
      server.startServer();
      server.stopServer();
   }

   /**
    * Starts the server thread.
    */
   public void run ()
   {
      startServer();
      stopServer();
   }

   /**
    * Creates a client socket for every incoming request and creates a
    * ClientHandler thread to handle the request/response.
    * Server will be stopped after a certain timeframe.
    */
   public void startServer ()
   {
      logger.info("Starting server..");
      try
      {
         final ServerSocket s = new ServerSocket(mPort);
         s.setSoTimeout(SOCKET_TIMEOUT);
         int clientCounter = 0;
         int waitingCounter = 0;
         ClientHandler client = null;
         logger.info("Waiting for connection on " + s);
         while (!haveToStop() && waitingCounter < WAITING_LOOPS)
         {
            Socket incoming = null;
            try
            {
               mServerStarted = true;
               incoming = s.accept();
            }
            catch (SocketTimeoutException ste)
            {
               waitingCounter++;
            }

            if (incoming != null)
            {
               clientCounter++;
               client = new ClientHandler(incoming, clientCounter);
               mHandler.put(Integer.toString(clientCounter), client);
               client.start();
            }
         }
      }
      catch (Exception e)
      {
         logger.warning("Failed to start SimpleServer: " + e);
         e.printStackTrace();
      }
   }

   /**
    * Sets the flag for stopping the server.
    */
   public void doStop ()
   {
      synchronized (mMonitor)
      {
         mDoStop = true;
      }
   }

   /**
    * Checks the flag for stopping the server.
    * @return boolean
    *          the flag for stopping the server
    */
   public boolean haveToStop ()
   {
      synchronized (mMonitor)
      {
         return mDoStop;
      }
   }

   /**
    * Stops the client handler threads.
    *
    */
   public void stopServer ()
   {
      logger.info("\n---Stopping Server with " + mHandler.size()
            + " client handler---\n");
      final Iterator it = mHandler.keySet().iterator();
      while (it.hasNext())
      {
         try
         {
            final String clientId = (String) it.next();
            final ClientHandler client = (ClientHandler) mHandler.get(clientId);
            if (client != null)
            {
               client.haveToStop();
               client.join(JOIN_TIMEFRAME);
               if (client.isAlive())
               {
                  interruptClient(client);
               }
            }
         }
         catch (Exception ex)
         {
            // ignore
         }
      }
      if (mTerminator != null && mTerminator.isAlive())
      {
         mTerminator.interrupt();
      }

      logger.warning("---Stopping DONE---");
   }

   /**
    * Returns <tt>true</tt> if the server has been started.
    * @return <tt>true</tt> if the server has been started;
    *       <tt>false</tt> otherwise.
    */
   public boolean isServerStarted ()
   {
      return mServerStarted;
   }
   /**
    * Interrupts the client handler thread.
    * @param client
    *          the thread to interrupt
    * @throws InterruptedException
    *          if interrupt fails
    */
   private void interruptClient (ClientHandler client)
         throws InterruptedException
   {
      try
      {
         client.interrupt();
         logger.info("Needed to interrupt client handler thread.");
      }
      catch (Exception ex)
      {
         logger.warning(
            "Exception while interrupting client handler thread: "
            + ex.getMessage());
      }
      client.join(JOIN_TIMEFRAME);
      if (client.isAlive())
      {
         logger.warning("unable to interrupt client thread");
      }
   }
}
