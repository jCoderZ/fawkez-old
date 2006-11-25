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

import java.io.IOException;
import java.io.InputStream;

/**
 * Class for stopping the server.
 */
public class Terminator
      extends Thread
{
   private final SimpleServer mServer;

   /**
    * Constructor.
    * @param server
    *          the server to stop
    */
   public Terminator (SimpleServer server)
   {
      mServer = server;
      start();
   }

   /**
    * Starting terminator thread.
    */
   public void run ()
   {
      waitForQuit ();
      mServer.doStop();
   }

   /**
    * Waits for the user to enter Q or q (followed by a return).
    *
    * @return  int
    *          the exit status (nonzero indicates abnormal termination)
    */
   public int waitForQuit ()
   {
      final InputStream in = System.in;
      char answer = '\0';
      int  exitResult = 0;

      //System.out.print("\'q' for Quit: ");
      while (!((answer == 'q') || (answer == 'Q')))
      {
         try
         {
            final int n = in.available();
            if (n > 0)
            {
               final byte[] b = new byte[n];
               final int result = in.read(b);
               if (result == -1)
               {
                  in.close();
                  break;
               }
               answer = new String(b).charAt(0);
            }
            if (interrupted())
            {
               in.close();
               break;
            }
         }
         catch (IOException e)
         {
            e.printStackTrace();
            exitResult = 1;
         }
      }
      return exitResult;
   }
}

