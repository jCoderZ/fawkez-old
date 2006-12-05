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
package org.jcoderz.phoenix.chart2d;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;


/**
 * The class reads XML documents according to specified DTD and
 * translates all related events into Chart2DHandler events.
 * <p>
 * Usage sample:
 * 
 * <pre>
 *         Chart2DParser parser = new Chart2DParser(...);
 *         parser.parse(new InputSource(&quot;...&quot;));
 * </pre>
 * 
 * <p>
 * <b>Warning:</b> the class is machine generated. DO NOT MODIFY
 * </p>
 */
public class Chart2DParser implements ContentHandler
{
   private static final String UNEXPECTED_CHARACTERS_EVENT 
       = "Unexpected characters() event! (Missing DTD?)";
   private final StringBuffer mBuffer;
   private final Chart2DHandler mHandler;
   private final Stack mContext;
   private final EntityResolver mResolver;
   private final EntityResolver mRootResolver;

   /**
    * Creates a parser instance.
    * @param hdlr handler interface implementation (never
    *        <code>null</code>
    * @param rslvr SAX entity resolver implementation or
    *        <code>null</code>. It is recommended that it could be
    *        able to resolve at least the DTD.
    */
   public Chart2DParser (final Chart2DHandler hdlr, final EntityResolver rslvr)
   {
      mRootResolver = rslvr;

      mResolver = new MyResolver();
      mHandler = hdlr;
      mBuffer = new StringBuffer();
      mContext = new Stack();
   }

   /** {@inheritDoc} */
   public final void setDocumentLocator (Locator locator)
   {
       // NOOP
   }

   /** {@inheritDoc} */
   public final void startDocument () 
         throws SAXException
   {
   }

   /** {@inheritDoc} */
   public final void endDocument () 
         throws SAXException
   {
   }

   /** {@inheritDoc} */
   public final void startElement (String ns, String name, String qname, 
         Attributes attrs) 
         throws SAXException
   {
      dispatch(true);
      mContext.push(new Object[] {qname, new AttributesImpl(attrs)});
      if ("GraphLabelsLinesStyle".equals(name))
      {
         mHandler.handleGraphLabelsLinesStyle(attrs);
      }
      else if ("Category".equals(name))
      {
         mHandler.startCategory(attrs);
      }
      else if ("MultiColorsProperties".equals(name))
      {
         mHandler.startMultiColorsProperties(attrs);
      }
      else if ("LBChart2D".equals(name))
      {
         mHandler.startLBChart2D(attrs);
      }
      else if ("Dataset".equals(name))
      {
         mHandler.startDataset(attrs);
      }
      else if ("PieChart2DProperties".equals(name))
      {
         mHandler.handlePieChart2DProperties(attrs);
      }
      else if ("GraphChart2DProperties".equals(name))
      {
         mHandler.startGraphChart2DProperties(attrs);
      }
      else if ("Chart2D".equals(name))
      {
         mHandler.startChart2D(attrs);
      }
      else if ("LLChart2D".equals(name))
      {
         mHandler.startLLChart2D(attrs);
      }
      else if ("GraphNumbersLinesStyle".equals(name))
      {
         mHandler.handleGraphNumbersLinesStyle(attrs);
      }
      else if ("PieChart2D".equals(name))
      {
         mHandler.startPieChart2D(attrs);
      }
      else if ("Object2DProperties".equals(name))
      {
         mHandler.handleObject2DProperties(attrs);
      }
      else if ("WarningRegionProperties".equals(name))
      {
         mHandler.handleWarningRegionProperties(attrs);
      }
      else if ("Chart2DProperties".equals(name))
      {
         mHandler.handleChart2DProperties(attrs);
      }
      else if ("Set".equals(name))
      {
         mHandler.startSet(attrs);
      }
      else if ("GraphProperties".equals(name))
      {
         mHandler.startGraphProperties(attrs);
      }
      else if ("LegendProperties".equals(name))
      {
         mHandler.startLegendProperties(attrs);
      }
   }

   /** {@inheritDoc} */
   public final void endElement (String ns, String name, String qname) 
         throws SAXException
   {
      dispatch(false);
      mContext.pop();
      if ("Category".equals(name))
      {
         mHandler.endCategory();
      }
      else if ("MultiColorsProperties".equals(name))
      {
         mHandler.endMultiColorsProperties();
      }
      else if ("LBChart2D".equals(name))
      {
         mHandler.endLBChart2D();
      }
      else if ("Dataset".equals(name))
      {
         mHandler.endDataset();
      }
      else if ("GraphChart2DProperties".equals(name))
      {
         mHandler.endGraphChart2DProperties();
      }
      else if ("Chart2D".equals(name))
      {
         mHandler.endChart2D();
      }
      else if ("LLChart2D".equals(name))
      {
         mHandler.endLLChart2D();
      }
      else if ("PieChart2D".equals(name))
      {
         mHandler.endPieChart2D();
      }
      else if ("Set".equals(name))
      {
         mHandler.endSet();
      }
      else if ("GraphProperties".equals(name))
      {
         mHandler.endGraphProperties();
      }
      else if ("LegendProperties".equals(name))
      {
         mHandler.endLegendProperties();
      }
   }

   /** {@inheritDoc} */
   public final void characters (char[] chars, int start, int len)
         throws SAXException
   {
      mBuffer.append(chars, start, len);
   }

   /** {@inheritDoc} */
   public final void ignorableWhitespace (char[] chars, int start, int len)
         throws SAXException
   {
   }

   /** {@inheritDoc} */
   public final void processingInstruction (String target, String data) 
         throws SAXException
   {
   }

   /** {@inheritDoc} */
   public final void startPrefixMapping (final String prefix, final String uri) 
         throws SAXException
   {
   }

   /** {@inheritDoc} */
   public final void endPrefixMapping (final String prefix)
         throws SAXException
   {
   }

   /** {@inheritDoc} */
   public final void skippedEntity (String name) 
         throws SAXException
   {
   }

   private void dispatch (final boolean fireOnlyIfMixed) 
         throws SAXException
   {
      if (fireOnlyIfMixed && (mBuffer.length() == 0))
      {
         return; // skip it
      }

      final Object[] ctx = (Object[]) mContext.peek();
      final String here = (String) ctx[0];
      final Attributes attrs = (Attributes) ctx[1];
      if ("LegendLabelsTexts".equals(here))
      {
         if (fireOnlyIfMixed)
         {
            throw new IllegalStateException(
                  UNEXPECTED_CHARACTERS_EVENT);
         }
         mHandler.handleLegendLabelsTexts((mBuffer.length() == 0) 
               ? null : mBuffer.toString(), attrs);
      }
      else if ("AxisLabelText".equals(here))
      {
         if (fireOnlyIfMixed)
         {
            throw new IllegalStateException(
                  UNEXPECTED_CHARACTERS_EVENT);
         }
         mHandler.handleAxisLabelText((mBuffer.length() == 0) 
               ? null : mBuffer.toString(), attrs);
      }
      else if ("Data".equals(here))
      {
         if (fireOnlyIfMixed)
         {
            throw new IllegalStateException(
                  UNEXPECTED_CHARACTERS_EVENT);
         }
         mHandler.handleData((mBuffer.length() == 0) 
               ? null : mBuffer.toString(), attrs);
      }
      else if ("ColorsCustom".equals(here))
      {
         if (fireOnlyIfMixed)
         {
            throw new IllegalStateException(
                  UNEXPECTED_CHARACTERS_EVENT);
         }
         mHandler.handleColorsCustom((mBuffer.length() == 0) 
               ? null : mBuffer.toString(), attrs);
      }
      else
      {
         // do not care
      }
      mBuffer.delete(0, mBuffer.length());
   }

   /**
    * The recognizer entry method taking an InputSource.
    * @param input InputSource to be parsed.
    * @throws java.io.IOException on I/O error.
    * @throws SAXException propagated exception thrown by a
    *         DocumentHandler.
    * @throws javax.xml.parsers.ParserConfigurationException a parser
    *         satisfining requested configuration can not be created.
    * @throws javax.xml.parsers.FactoryConfigurationError if the
    *         implementation can not be instantiated.
    */
   public void parse (final InputSource input)
         throws SAXException, ParserConfigurationException, IOException
   {
      parse(input, this);
   }

   /**
    * The recognizer entry method taking a URL.
    * @param url URL source to be parsed.
    * @throws java.io.IOException on I/O error.
    * @throws SAXException propagated exception thrown by a
    *         DocumentHandler.
    * @throws javax.xml.parsers.ParserConfigurationException a parser
    *         satisfining requested configuration can not be created.
    * @throws javax.xml.parsers.FactoryConfigurationError if the
    *         implementation can not be instantiated.
    */
   public void parse (final java.net.URL url)
         throws SAXException, ParserConfigurationException, IOException
   {
      parse(new InputSource(url.toExternalForm()), this);
   }

   /**
    * The recognizer entry method taking an Inputsource.
    * @param input InputSource to be parsed.
    * @throws java.io.IOException on I/O error.
    * @throws SAXException propagated exception thrown by a
    *         DocumentHandler.
    * @throws javax.xml.parsers.ParserConfigurationException a parser
    *         satisfining requested configuration can not be created.
    * @throws javax.xml.parsers.FactoryConfigurationError if the
    *         implementation can not be instantiated.
    */
   public static void parse (final InputSource input,
         final Chart2DHandler handler)
         throws SAXException, ParserConfigurationException, IOException
   {
      parse(input, new Chart2DParser(handler, null));
   }

   /**
    * The recognizer entry method taking a URL.
    * @param url URL source to be parsed.
    * @throws java.io.IOException on I/O error.
    * @throws SAXException propagated exception thrown by a
    *         DocumentHandler.
    * @throws javax.xml.parsers.ParserConfigurationException a parser
    *         satisfining requested configuration can not be created.
    * @throws javax.xml.parsers.FactoryConfigurationError if the
    *         implementation can not be instantiated.
    */
   public static void parse (final URL url, final Chart2DHandler handler)
         throws SAXException, ParserConfigurationException, IOException
   {
      parse(new InputSource(url.toExternalForm()), handler);
   }

   private static void parse (final InputSource input,
         final Chart2DParser recognizer)
         throws SAXException, ParserConfigurationException, IOException
   {
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(true); // the code was generated according
      // DTD
      factory.setNamespaceAware(true); // the code was generated
      // according DTD
      final XMLReader parser = factory.newSAXParser().getXMLReader();
      parser.setContentHandler(recognizer);
      parser.setErrorHandler(recognizer.getDefaultErrorHandler());
      if (recognizer.mResolver != null)
      {
         parser.setEntityResolver(recognizer.mResolver);
      }
      parser.parse(input);
   }

   /**
    * Creates default error handler used by this parser.
    * @return org.xml.sax.ErrorHandler implementation
    */
   protected ErrorHandler getDefaultErrorHandler ()
   {
      return new ErrorHandler()
      {
         public void error (SAXParseException ex) 
               throws SAXException
         {
            if (mContext.isEmpty())
            {
               System.err.println("Missing DOCTYPE.");
            }
            throw ex;
         }

         public void fatalError (SAXParseException ex) 
               throws SAXException
         {
            throw ex;
         }

         public void warning (SAXParseException ex) 
               throws SAXException
         {
            // ignore
         }
      };
   }


   class MyResolver implements EntityResolver
   {
      public InputSource resolveEntity (String publicId, String systemId)
            throws SAXException, IOException
      {
         InputSource result = null;
         if ((systemId != null && systemId.equals("chart2d.dtd"))
               || (publicId != null && publicId.equals("chart2d.dtd"))
               || (systemId != null && systemId
                     .equals("-//The jCoderZ Project//Chart2D//EN"))
               || (publicId != null && publicId
                     .equals("-//The jCoderZ Project//Chart2D//EN")))
         {
            // return a special input source
            final InputStream s 
                = getClass().getResourceAsStream("chart2d.dtd");
            if (s != null)
            {
               result = new InputSource(s);
            }
         }
         else
         {
            // use the default behaviour 
            if (mRootResolver != null)
            {
               result = mRootResolver.resolveEntity(publicId, systemId);
            }
         }
         return result;
      }
   }
}
