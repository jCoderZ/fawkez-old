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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jcoderz.commons.util.Base64Util;
import org.jcoderz.commons.util.Constants;
import org.jcoderz.commons.util.HexUtil;
import org.jcoderz.commons.util.XmlUtil;


/**
 * Simple servlet to perform base 64 decoding.
 *
 * @web.servlet name="base64"
 * @web.servlet-mapping url-pattern="/base64"
 *
 * @author Andreas Mandel
 */
public class Base64DecoderServlet
      extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   private static final String ENCODED_PARAMETER_NAME = "encoded";

   /** {@inheritDoc} */
   protected void doPost (HttpServletRequest request,
         HttpServletResponse response)
         throws IOException

   {
      doGet(request, response);
   }

   /** {@inheritDoc} */
   protected void doGet (HttpServletRequest request,
         HttpServletResponse response)
         throws IOException
   {
      final PrintWriter out = response.getWriter();
      final String encodedData = request.getParameter(ENCODED_PARAMETER_NAME);
      final byte[] data = Base64Util.decode(encodedData);

      response.setContentType("text/html");
      out.println("<html><head><title>Simple base64 decoder</title>");
      out.println("</head>");
      out.println("<body>");
      out.println("<form method='post'>");
      out.println("<textarea tabindex='1' name='" + ENCODED_PARAMETER_NAME
            + "' rows='5' cols='100' wrap='soft'>");
      if (encodedData != null && data == null)
      {
         out.println(encodedData);
      }
      out.println("</textarea>");
      out.println("<input tabindex='2' type='submit' value='Decode' "
            + "name='Decode' accesskey='d' title='Decode message [alt-d]'/>");
      out.println("</form>");

      if (encodedData != null)
      {
         if (data != null)
         {
            dumpResult(out, data);
         }
         else
         {
            out.println("<hr />");
            out.println("<h2>Invalid base64 data!</h2>");
            out.println("<hr />");
         }
      }

      out.println("</body>");
      out.println("</html>");
   }

   private void dumpResult (PrintWriter out, byte[] data)
         throws IOException
   {
      out.println("<hr />");
      final String xml 
          = XmlUtil.formatXml(new String(data, Constants.ENCODING_UTF8));
      if (xml != null)
      {
         out.println("<pre>");
         out.println(XmlUtil.escape(xml));
         out.println("</pre>");
         out.println("<hr />");
      }

      // hexdump...
      final String hexDump = HexUtil.dump(data);
      out.println("<pre>");
      out.println(XmlUtil.escape(hexDump));
      out.println("</pre>");
      out.println("<hr />");
   }

   /** {@inheritDoc} */
   public String getServletInfo ()
   {
      return "Simple base64 decoder servlet.";
   }

}
