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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.jcoderz.commons.util.IoUtil;
import org.xml.sax.SAXException;

/**
 * @author Albrecht Messner
 */
public class TemplateGenerator
{
   private final TemplateZip mTemplateZip;

   public TemplateGenerator (String templateZipName)
         throws IOException,
               ParserConfigurationException,
               SAXException,
               FactoryConfigurationError,
               TemplateGeneratorException
   {
      mTemplateZip = new TemplateZip(templateZipName);
      mTemplateZip.readTemplateFile();
   }

   public String getTemplateDescription ()
   {
      return mTemplateZip.getDescription().getDescription();
   }

   public List getParameterList ()
   {
      return mTemplateZip.getDescription().getParameterList();
   }

   public byte[] parametrizeTemplates (Map parameterMap)
         throws IOException, TemplateGeneratorException
   {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final ZipOutputStream archive = new ZipOutputStream(bos);

      // check values
      for (final Iterator it = mTemplateZip.getDescription()
            .getParameterList().iterator(); it.hasNext(); )
      {
         final Parameter p = (Parameter) it.next();
         final String val = (String) parameterMap.get(p.getName());
         if (val == null)
         {
            throw new TemplateGeneratorException("Value for parameter "
                  + p.getName() + " missing");
         }
         p.checkValue(val);
      }

      for (final Iterator it = mTemplateZip.getTemplates().iterator(); 
          it.hasNext(); )
      {
         final Template t = (Template) it.next();
         final String parametrizedTemplate = t.parametrize(parameterMap);

         final byte[] entryData = parametrizedTemplate.getBytes();
         final ZipEntry entry = new ZipEntry(t.parametrizeTarget(parameterMap));

         archive.putNextEntry(entry);
         archive.write(entryData, 0, entryData.length);
         archive.closeEntry();
      }

      archive.flush();
      archive.close();
      return bos.toByteArray();
   }

   protected static String getJcoderzHeader (String type)
      throws TemplateGeneratorException
   {
      if (type.equals("java"))
      {
         final String resource = "build/resources/jcoderz_java_header.txt";
         try
         {
            final InputStream is
                  = TemplateGenerator.class.getResourceAsStream(resource);
            if (is == null)
            {
               throw new IOException("Could not load resource " + resource);
            }
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IoUtil.copy(is, bout);
            return new String(bout.toByteArray());
         }
         catch (IOException x)
         {
            throw new TemplateGeneratorException(
                  "Could not read resource " + resource, x);
         }
      }
      else
      {
         throw new TemplateGeneratorException(
               "Don't have jCoderZ header for type " + type);
      }
   }
}















