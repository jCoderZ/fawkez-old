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
package org.jcoderz.commons.util;

import org.xml.sax.Attributes;

/**
 * This class holds utility methods for common xml topics.
 *
 * @author Andreas Mandel
 */
public final class XmlUtil
{
   /**
    * Immutable and always empty version of a
    * {@link org.xml.sax.Attributes} object.
    */
   public static final Attributes EMPTY_ATTRIBUTES = new EmptyAttribute();

   /** No instances. */
   private XmlUtil ()
   {
      //  No instances.
   }

   /**
    * Immutable implementation of a empty attribute list.
    * @see Attributes
    */
   private static final class EmptyAttribute
         implements Attributes
   {
       /** {@inheritDoc} */
      public int getLength ()
      {
         return 0;
      }

      /** {@inheritDoc} */
      public String getLocalName (int index)
      {
         return null;
      }

      /** {@inheritDoc} */
      public String getQName (int index)
      {
         return null;
      }

      /** {@inheritDoc} */
      public String getType (int index)
      {
         return null;
      }

      /** {@inheritDoc} */
      public String getURI (int index)
      {
         return null;
      }

      /** {@inheritDoc} */
      public String getValue (int index)
      {
         return null;
      }

      /** {@inheritDoc} */
      public int getIndex (String qName)
      {
         return -1;
      }

      /** {@inheritDoc} */
      public String getType (String qName)
      {
         return null;
      }

      /** {@inheritDoc} */
      public String getValue (String qName)
      {
         return null;
      }

      /** {@inheritDoc} */
      public int getIndex (String uri, String localName)
      {
         return -1;
      }

      /** {@inheritDoc} */
      public String getType (String uri, String localName)
      {
         return null;
      }

      /** {@inheritDoc} */
      public String getValue (String uri, String localName)
      {
         return null;
      }
   }
   
   /**
    * Encode a string so that it can be safely used as attribute value in
    * XML output.
    * @param attribute the attribute value to be encoded.
    * @return a string representing the attribute value that can be safely 
    *         used in XML output.
    */
   public static String attributeEscape (String attribute)
   {
      final StringBuffer sb = new StringBuffer();
      if (attribute != null)
      {
         char c;
         final int l = attribute.length();
         for (int i = 0; i < l; i++)
         {
            c = attribute.charAt(i);
            switch (c)
            {
               case '<':
                  sb.append("&lt;");
                  break;
               case '>':
                   sb.append("&gt;");
                   break;
               case '\'':
                  sb.append("&apos;");
                  break;
               case '"':
                   sb.append("&quot;");
                   break;
               case '&':
                  sb.append("&amp;");
                  break;
               default :
                  if (c > Byte.MAX_VALUE)
                  {
                     sb.append("&#x");
                     sb.append(Integer.toHexString(c));
                     sb.append(';');
                  }
                  else
                  {
                     sb.append(c);
                  }
            }
         }
      }
      return sb.toString();
   }

   /**
    * Encode a string so that it can be safely used as text in an element
    * for XML output.
    * @param text the element text body.
    * @return a string so that it can be safely used as text in an element
    *       for XML output.
    */
   public static String escape (String text)
   {
      final StringBuffer sb = new StringBuffer();
      if (text != null)
      {
         char c;
         final int l = text.length();
         for (int i = 0; i < l; i++)
         {
            c = text.charAt(i);
            switch (c)
            {
               case '<':
                  sb.append("&lt;");
                  break;
               case '>':  // only needed to avoid ]]>
                   sb.append("&gt;");
                   break;
               case '&':
                  sb.append("&amp;");
                  break;
               default :
                  if (c > Byte.MAX_VALUE)
                  {
                     sb.append("&#x");
                     sb.append(Integer.toHexString(c));
                     sb.append(';');
                  }
                  else
                  {
                     sb.append(c);
                  }
            }
         }
      }
      return sb.toString();
   }

}
