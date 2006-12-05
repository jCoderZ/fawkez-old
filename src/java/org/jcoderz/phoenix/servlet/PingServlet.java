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
package org.jcoderz.phoenix.servlet;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jcoderz.commons.util.IoUtil;

/**
 * This servlet sends a simple ping to the given host and returns a
 * 1 pixel image with a color representing the host status.
 *
 * @web.servlet name="ping"
 * @web.servlet-mapping url-pattern="/ping/*"
 *
 * @author Andreas Mandel
 */
public final class PingServlet
      extends HttpServlet
{
   /**
    * Timezone used by the protocol.
    */
   public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

   /** The format used to write date values. */
   public static final String DATE_TIME_FORMAT
         = "yyyy-MM-dd HH:mm:ss.SSS";

   private static final int HOST_NOT_REACHED = -1;
   private static final int UNKNOWN_HOST = -2;

   private static final int ILLEGAL_ARGUMENT = -3;
   private static final int HOST_REFUSED = -4;

   private static final long serialVersionUID = 1L;

   private static final int MAX_PING_CACHE_SIZE = 1000;
   private static final Map PING_CACHE
         = Collections.synchronizedMap(new HashMap());
   private static long sLastCacheUpdate = 0;
   private static boolean sUpdateInProgreess = false;

   private static final long VALIDITY_TIME = 10 * 60 * 1000;

   /**
    * If a value in the cache in not used for this period of time it is
    * removed from the cache.
    */
   private static final long MAX_IDLE_TIME = 7 * 24 * 60 * 60 * 1000;

   private static final int SOCKET_TIMEOUT = 1000;

   private static final long FAST_RESPONSE = 5;

   private static final int COLOR_OFFSET_RED =  13;
   private static final int COLOR_OFFSET_GREEN = COLOR_OFFSET_RED + 1;
   private static final int COLOR_OFFSET_BLUE = COLOR_OFFSET_GREEN + 1;
   private static final int BG_OFFSET_RED = COLOR_OFFSET_BLUE + 1;
   private static final int BG_OFFSET_GREEN = BG_OFFSET_RED + 1;
   private static final int BG_OFFSET_BLUE = BG_OFFSET_GREEN + 1;

   private static final byte [] IMAGE_DATA
         = {
            'G', 'I', 'F', '8', '7', 'a', //
            1, 0, // logical width
            1, 0, // logical height
            (byte) 0x80, //
            2, // BG color index
            0,  //
            0, 0, 0, // COLOR 1 (used)
            0, 0, 0, // COLOR 2 (unused)
            44,      // IMAGE
            0, 0,    // Left POS
            0, 0,    // Right POS
            1, 0,    // WIDTH
            1, 0,    // HEIGHT
            0,
            2, 2, 68, 01, 0, 59
      };

   private static final int ECHO_PORT = 22;

   protected void doPost (HttpServletRequest req, HttpServletResponse rsp)
         throws IOException
   {
      doGet(req, rsp);
   }

   protected void doGet (HttpServletRequest req, HttpServletResponse rsp)
         throws IOException
   {
      String hostname = req.getParameter("host");

      final String clear = req.getParameter("clear");
      if (clear != null && clear.length() > 0)
      {
         PING_CACHE.clear();
      }

      if (hostname == null || hostname.length() == 0)
      {
         hostname = req.getPathInfo();
         if (hostname != null && hostname.charAt(0) == '/')
         {
            hostname = hostname.substring(1);
         }
      }

      if (hostname == null || hostname.length() == 0)
      {
         dumpCacheInfo(rsp);
      }
      else
      {
         sendAnswer(check(hostname), rsp);
      }

      updateCache();
   }

   private void dumpCacheInfo (HttpServletResponse rsp)
         throws IOException
   {
      rsp.setContentType("text/plain");

      final StringWriter sw = new StringWriter();
      final PrintWriter data = new PrintWriter(sw);
      synchronized (PING_CACHE)
      {
         data.println("Last Update: " + dateToString(sLastCacheUpdate));
         data.println("Update in progress: " + sUpdateInProgreess);
         data.println("Number of stored results: " + PING_CACHE.size());
         data.println("Cached Data:");
         data.println("---");
      }
      final PrintWriter out = rsp.getWriter();
      out.print(sw.getBuffer().toString());

      try
      {
         final Iterator i = PING_CACHE.values().iterator();

         while (i.hasNext())
         {
            final PingResult result = (PingResult) i.next();
            out.println(result);
         }
         out.println("---");
      }
      catch (ConcurrentModificationException ex)
      {
         out.println("Uups... somebody updated the cache... try again!");
      }
   }

   private void updateCache ()
   {
      try
      {
         final long now = System.currentTimeMillis();
         final long lastUpdate;
         final boolean alreadyUpdating;
         synchronized (PING_CACHE)
         {
            lastUpdate = sLastCacheUpdate;
            alreadyUpdating = sUpdateInProgreess;
            sUpdateInProgreess = true;
         }

         if (lastUpdate + VALIDITY_TIME < now && !alreadyUpdating)
         {
            final Iterator i = PING_CACHE.values().iterator();

            while (i.hasNext())
            {
               final PingResult result = (PingResult) i.next();
               if (result.getLastUsed() < (now - MAX_IDLE_TIME))
               {
                  i.remove();
               }
               else if (result.getExpires() < now)
               {
                  new Thread()
                  {
                     public void run ()
                     {
                        check(result.getHostname()); // update entry not list?
                     };
                  }.start();
               }
            }

            synchronized (PING_CACHE)
            {
               sLastCacheUpdate = now;
            }
         }
      }
      catch (ConcurrentModificationException ex)
      {
         // we will catch up next time
      }
      finally
      {
         synchronized (PING_CACHE)
         {
            sUpdateInProgreess = false;
         }
      }
   }


   private void sendAnswer (PingResult result, HttpServletResponse rsp)
         throws IOException
   {
      rsp.setContentType("image/gif");
      rsp.setContentLength(IMAGE_DATA.length);
      rsp.setHeader("Cache-Control", "public");

      final byte[] responseData = new byte[IMAGE_DATA.length];
      System.arraycopy(IMAGE_DATA, 0, responseData, 0, IMAGE_DATA.length);

      if (result != null)
      {
         rsp.setDateHeader("Last-Modified", result.getLastModified());
         rsp.setDateHeader("Expires", result.getExpires());
         final Color color = resultToColor(result.getResult());

         responseData[COLOR_OFFSET_RED] = (byte) color.getRed();
         responseData[COLOR_OFFSET_GREEN] = (byte) color.getGreen();
         responseData[COLOR_OFFSET_BLUE] = (byte) color.getBlue();
         responseData[BG_OFFSET_RED] = (byte) color.getRed();
         responseData[BG_OFFSET_GREEN] = (byte) color.getGreen();
         responseData[BG_OFFSET_BLUE] = (byte) color.getBlue();
         rsp.setHeader("response-value", String.valueOf(result.getResult()));
         rsp.setHeader("response-host", result.getHostname());
      }

      rsp.getOutputStream().write(responseData);
   }


   private Color resultToColor (long result)
   {
      final Color responseColor;
      if (result == UNKNOWN_HOST)
      {
         responseColor = Color.RED;
      }
      else if (result == ILLEGAL_ARGUMENT)
      {
         responseColor = Color.ORANGE;
      }
      else if (result == HOST_REFUSED)
      {
         responseColor = Color.YELLOW;
      }
      else if (result < 0)
      {
         responseColor = Color.BLACK;
      }
      else if (result <= FAST_RESPONSE)
      {
         responseColor = Color.GREEN.brighter();
      }
      else if (result <= (FAST_RESPONSE + FAST_RESPONSE))
      {
         responseColor = Color.GREEN;
      }
      else if (result <= (FAST_RESPONSE + FAST_RESPONSE + FAST_RESPONSE))
      {
         responseColor = Color.GREEN.darker();
      }
      else
      {
         responseColor = Color.GREEN.darker().darker();
      }
      return responseColor;
   }

   protected PingResult check (String host)
   {
      final long now = System.currentTimeMillis();

      PingResult result = (PingResult) PING_CACHE.get(host);
      if (result == null)
      {
          result = new PingResult(host, ping(host));
          PING_CACHE.put(host, result);
      }
      else
      {
            // only one update per host
            synchronized (result)
            {
               if (result.getExpires() < now)
               {  // this might take some time!
                  result.setResult(ping(host));
               }
            }
      }

      // just to be save against memory attacks
      if (PING_CACHE.size() > MAX_PING_CACHE_SIZE)
      {
         PING_CACHE.clear();
      }

      return result;
   }


   protected static long ping (String hostname)
   {
      final long result;
      if (hostname == null || hostname.length() == 0)
      {
         result = ILLEGAL_ARGUMENT;
      }
      else
      {
         InetAddress host = null;
         try
         {
            host = InetAddress.getByName(hostname);
         }
         catch (UnknownHostException ex)
         {
            // hmmm
         }

         if (host == null)
         {
            result = UNKNOWN_HOST;
         }
         else
         {
            result = ping(host);
         }
      }
      return result;
   }


   protected static long ping (InetAddress host)
   {
      final long timer = System.currentTimeMillis();
      DataInputStream dis = null;
      Socket t = null;
      long result = -1;
      try
      {
         t = new Socket(host, ECHO_PORT);
         t.setTcpNoDelay(true);
         t.setSoTimeout(SOCKET_TIMEOUT);
         dis = new DataInputStream(t.getInputStream());
         dis.readByte();
         result = System.currentTimeMillis() - timer;
      }
      catch (IOException e)
      {
         final String message = e.getMessage();
         if (message != null && message.indexOf("refused") != -1)
         {
            result = HOST_REFUSED;
         }
         else
         {
            result = HOST_NOT_REACHED;
         }
      }
      finally
      {
         IoUtil.close(dis);
         IoUtil.close(t);
      }
      return result;
   }

   static String dateToString (long time)
   {
      final DateFormat formater
            = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.US);
      formater.setTimeZone(TIME_ZONE);
      return formater.format(new Date(time));
   }

   private class PingResult
   {
      private final String mHostname;
      private long mResult;
      private long mExpires;
      private long mLastModified;
      private long mLastUsed;

      PingResult (String hostname, long result)
      {
         final long now = System.currentTimeMillis();
         mExpires = now + VALIDITY_TIME;
         mLastModified = now;
         mHostname = hostname;
         mResult = result;
         mLastUsed = now;
      }

      /** {@inheritDoc} */
      public synchronized String toString ()
      {
         final StringBuffer buffer = new StringBuffer();
         buffer.append("[PingResult: ");
         buffer.append(mHostname);
         buffer.append(" result: ");
         buffer.append(mResult);
         buffer.append(" expires: ");
         buffer.append(dateToString(mExpires));
         buffer.append(" lastModified: ");
         buffer.append(dateToString(mLastModified));
         buffer.append(" lastUsed: ");
         buffer.append(dateToString(mLastUsed));
         buffer.append(']');
         return buffer.toString();
      }

      String getHostname ()
      {
         return mHostname;
      }

      synchronized long getExpires ()
      {
         return mExpires;
      }

      synchronized long getLastModified ()
      {
         return mLastModified;
      }

      synchronized void setResult (long result)
      {
         final long now = System.currentTimeMillis();
         mExpires = now + VALIDITY_TIME;
         if (result != mResult)
         {
            mLastModified = now;
            mResult = result;
         }
      }

      synchronized long getResult ()
      {
         mLastUsed = System.currentTimeMillis();
         return mResult;
      }

      synchronized long getLastUsed ()
      {
         return mLastUsed;
      }
   }
}
