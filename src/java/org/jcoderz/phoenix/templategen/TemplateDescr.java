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
package org.jcoderz.phoenix.templategen;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Albrecht Messner
 */
public class TemplateDescr
{
   private String mDescription = null;
   private final Map mParameterMap = new HashMap();
   private final List mParameterList = new ArrayList();
   private final Map mFiles = new HashMap();

   public String getDescription ()
   {
      return mDescription;
   }

   public Map getParameterMap ()
   {
      return mParameterMap;
   }

   public List getParameterList ()
   {
      return mParameterList;
   }

   public Map getFilesMap ()
   {
      return mFiles;
   }

   public void parseDescription (String xmlDescription)
         throws IOException, ParserConfigurationException,
               SAXException, FactoryConfigurationError
   {
      final DefaultHandler docHandler = new DefaultHandler() {

         private final StringBuffer mCharBuffer = new StringBuffer();
         private Parameter mParam = null;

         /** {@inheritDoc} */
         public void startElement (
               String uri,
               String localName,
               String qName,
               Attributes attributes)
               throws SAXException
         {
            try
            {
               if ("parameter".equals(qName))
               {
                  final String name = attributes.getValue("name");
                  final int minLength
                        = Integer.parseInt(attributes.getValue("minLength"));
                  final int maxLength
                        = Integer.parseInt(attributes.getValue("maxLength"));
                  final boolean multiLine
                        = Boolean.valueOf(attributes.getValue("multiLine"))
                        .booleanValue();
                  mParam = new Parameter(name, minLength, maxLength, multiLine);
               }
               else if ("description".equals(qName))
               {
                  mCharBuffer.setLength(0);
               }
               else if ("default".equals(qName)
                     || "regexp".equals(qName))
               {
                  if (mParam == null)
                  {
                     throw new SAXException(
                           "'default' and 'regexp' tags may only occur "
                           + " inside a 'parameter' tag");
                  }
                  mCharBuffer.setLength(0);
               }
               else if ("file".equals(qName))
               {
                  final String source = attributes.getValue("source");
                  final String target = attributes.getValue("target");
                  mFiles.put(source, target);
               }
            }
            catch (SAXException e)
            {
               e.printStackTrace();
               throw e;
            }
            catch (RuntimeException e)
            {
               e.printStackTrace();
               throw e;
            }
         }

         /** {@inheritDoc} */
         public void endElement (String uri, String localName, String qName)
               throws SAXException
         {
            try
            {
               if (qName.equals("parameter"))
               {
                  mParameterMap.put(mParam.getName(), mParam);
                  mParameterList.add(mParam);

                  // System.out.println(param);
                  mParam = null;
               }
               else if (qName.equals("description"))
               {
                  if (mParam != null)
                  {
                     mParam.setDescription(mCharBuffer.toString().trim());
                  }
                  else if (mDescription == null)
                  {
                     mDescription = mCharBuffer.toString().trim();
                  }
                  else
                  {
                     throw new SAXException(
                           "'description' element not expected here");
                  }
                  mCharBuffer.setLength(0);
               }
               else if (qName.equals("default"))
               {
                  mParam.setDefaultValue(mCharBuffer.toString().trim());
                  mCharBuffer.setLength(0);
               }
               else if (qName.equals("regexp"))
               {
                  mParam.setRegexp(mCharBuffer.toString().trim());
                  mCharBuffer.setLength(0);
               }
            }
            catch (SAXException e)
            {
               e.printStackTrace();
               throw e;
            }
            catch (RuntimeException e)
            {
               e.printStackTrace();
               throw e;
            }
         }

         /** {@inheritDoc} */
         public void characters (char[] ch, int start, int length)
               throws SAXException
         {
             mCharBuffer.append(ch, start, length);
         }
      };

      final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
      final StringReader sr = new StringReader(xmlDescription);
      parser.parse(new InputSource(sr), docHandler);
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      final StringBuffer sbuf = new StringBuffer();
      sbuf.append("[ParameterDescription");
      sbuf.append("\n   description=").append(mDescription);
      sbuf.append("\n   files=").append(mFiles.toString());
      sbuf.append("\n   parameters=");
      sbuf.append(mParameterMap.toString());
      sbuf.append(']');
      return sbuf.toString();
   }
}
