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

import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Base class to implement an XML filter.
 *
 * Subclass this class and you can control the XML output by invoking the
 * method {@link #enableOutput(boolean)}. To inject data use the
 * {@link org.apache.xml.serialize.XMLSerializer} provided by the method
 * {@link #getXmlSerializer()}.
 *
 * @author Michael Griffel
 * @author Andreas Mandel
 */
public class XmlFilterBase
      implements DTDHandler, ContentHandler
{
   /**
    * The default output format: XML using UTF-8 encoding w/o indentation.
    */
   public static final OutputFormat DEFAULT_OUTPUT_FORMAT
         = new OutputFormat(Method.XML, null, false);

   private final XMLSerializer mSerializer;
   private boolean mOutputEnabled = true;

   /**
    * Creates a new XmlFilter that delegates all output to the given
    * serializer.
    * @param serializer the serializer to delegate to.
    */
   public XmlFilterBase (XMLSerializer serializer)
   {
      mSerializer = serializer;
   }

   /**
    * Sets the output enabled flag.
    * @param outputEnabled the value to set the output enabled flag to.
    */
   public final void enableOutput (boolean outputEnabled)
   {
      mOutputEnabled = outputEnabled;
   }

   /** {@inheritDoc} */
   public void characters (char[] ch, int start, int length)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.characters(ch, start, length);
      }
   }

   /** {@inheritDoc} */
   public void endDocument ()
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.endDocument();
      }
   }

   /** {@inheritDoc} */
   public void endElement (String namespaceURI, String localName, String qName)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.endElement(namespaceURI, localName, qName);
      }
   }

   /** {@inheritDoc} */
   public void endPrefixMapping (String prefix)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.endPrefixMapping(prefix);
      }
   }

   /** {@inheritDoc} */
   public void ignorableWhitespace (char[] ch, int start, int length)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.ignorableWhitespace(ch, start, length);
      }
   }

   /** {@inheritDoc} */
   public void notationDecl (String name, String publicId, String systemId)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.notationDecl(name, publicId, systemId);
      }
   }

   /** {@inheritDoc} */
   public void processingInstruction (String target, String data)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.processingInstruction(target, data);
      }
   }

   /** {@inheritDoc} */
   public void setDocumentLocator (Locator locator)
   {
      if (mOutputEnabled)
      {
         mSerializer.setDocumentLocator(locator);
      }
   }

   /** {@inheritDoc} */
   public void skippedEntity (String name)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.skippedEntity(name);
      }
   }

   /** {@inheritDoc} */
   public void startDocument ()
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.startDocument();
      }
   }

   /** {@inheritDoc} */
   public void startElement (String namespaceURI, String localName,
         String qName, Attributes atts)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.startElement(namespaceURI, localName, qName, atts);
      }
   }

   /** {@inheritDoc} */
   public void startPrefixMapping (String prefix, String uri)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.startPrefixMapping(prefix, uri);
      }
   }

   /** {@inheritDoc} */
   public void unparsedEntityDecl (String name, String publicId,
         String systemId, String notationName)
         throws SAXException
   {
      if (mOutputEnabled)
      {
         mSerializer.unparsedEntityDecl(name, publicId, systemId, notationName);
      }
   }


   /**
    * Returns the used XML serializer.
    * @return the used XML serializer.
    */
   protected final XMLSerializer getXmlSerializer ()
   {
      return mSerializer;
   }
}
