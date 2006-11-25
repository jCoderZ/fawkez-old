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


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Handler interface for the chart2d.dtd.
 * @author Netbeans code generator
 * @author Andreas Mandel
 */
public interface Chart2DHandler
{

   /**
    * An empty element event handling method.
    * @param meta value or null
    */
   public void handleGraphLabelsLinesStyle (final Attributes meta)
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startCategory (final Attributes meta) 
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endCategory () 
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startMultiColorsProperties (final Attributes meta)
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endMultiColorsProperties () 
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startLBChart2D (final Attributes meta) 
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endLBChart2D () 
         throws SAXException;

   /**
    * A data element event handling method.
    * @param data value or null
    * @param meta attributes
    */
   public void handleLegendLabelsTexts (final String data, 
         final Attributes meta) 
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startDataset (final Attributes meta) 
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endDataset () 
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void handlePieChart2DProperties (final Attributes meta)
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startGraphChart2DProperties (final Attributes meta)
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endGraphChart2DProperties () 
         throws SAXException;

   /**
    * A data element event handling method.
    * @param data value or null
    * @param meta attributes
    */
   public void handleAxisLabelText (final String data, final Attributes meta) 
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startChart2D (final Attributes meta) 
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endChart2D () 
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startLLChart2D (final Attributes meta) 
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endLLChart2D () 
         throws SAXException;

   /**
    * An empty element event handling method.
    * @param meta value or null
    */
   public void handleGraphNumbersLinesStyle (final Attributes meta)
         throws SAXException;

   /**
    * A data element event handling method.
    * @param data value or null
    * @param meta attributes
    */
   public void handleData (final java.lang.String data, final Attributes meta)
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startPieChart2D (final Attributes meta) 
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endPieChart2D () 
         throws SAXException;

   /**
    * An empty element event handling method.
    * @param meta value or null
    */
   public void handleObject2DProperties (final Attributes meta)
         throws SAXException;

   /**
    * An empty element event handling method.
    * @param meta value or null
    */
   public void handleWarningRegionProperties (final Attributes meta)
         throws SAXException;

   /**
    * An empty element event handling method.
    * @param meta value or null
    */
   public void handleChart2DProperties (final Attributes meta)
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startSet (final Attributes meta) 
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endSet () 
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startGraphProperties (final Attributes meta)
         throws SAXException;

   /**
    * A container element end event handling method.
    */
   public void endGraphProperties () 
         throws SAXException;

   /**
    * A data element event handling method.
    * @param data value or null
    * @param meta attributes
    */
   public void handleColorsCustom (final String data, final Attributes meta) 
         throws SAXException;

   /**
    * A container element start event handling method.
    * @param meta attributes
    */
   public void startLegendProperties (final Attributes meta)
         throws SAXException;

   /**
    * A container element end event handling method.
    *
    */
   public void endLegendProperties () 
         throws SAXException;

}
