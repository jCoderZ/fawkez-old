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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author Albrecht Messner
 */
public class TemplateZip
{
   private static final String DESCRIPTION_FILE = "template.xml";
   private static final int READ_BUFFER_SIZE = 1024;

   private List mTemplateList;
   private TemplateDescr mDescription;

   private String mFileName;

   public TemplateZip (String fileName)
   {
      mTemplateList = new ArrayList();
      mFileName = fileName;
   }

   public void readTemplateFile ()
         throws IOException,
            ParserConfigurationException,
            SAXException,
            FactoryConfigurationError, TemplateGeneratorException
   {
      final ZipInputStream zin = new ZipInputStream(
           new FileInputStream(mFileName));
      ZipEntry entry;

      while ((entry = zin.getNextEntry()) != null)
      {
         if (entry.isDirectory())
         {
            System.err.println("Ignoring directory entry " + entry.getName());
            continue;
         }

         final File entryFile = new File(entry.getName());
         final String baseName = entryFile.getName();

         int read;
         byte[] buffer = new byte[READ_BUFFER_SIZE];
         final ByteArrayOutputStream data = new ByteArrayOutputStream();
         while ((read = zin.read(buffer)) != -1)
         {
            data.write(buffer, 0, read);
         }

         final String dataStr = new String(data.toByteArray());

         if (baseName.equals(DESCRIPTION_FILE))
         {
            mDescription = new TemplateDescr();
            mDescription.parseDescription(dataStr);
         }
         else
         {
            final Template t = new Template(baseName, dataStr);
            mTemplateList.add(t);
         }
      }

      completeTemplates();

      checkTemplate();
   }

   private void completeTemplates () throws TemplateGeneratorException
   {
      // set the target for all files
      for (final Iterator it = mTemplateList.iterator(); it.hasNext();)
      {
         final Template t = (Template) it.next();
         final String targetName
               = (String) mDescription.getFilesMap().get(t.getSourceName());
         if (targetName == null)
         {
            throw new TemplateGeneratorException(
                  "File description for file '"
                  + t.getSourceName()
                  + "' not found in template description");
         }
         t.setTargetName(targetName);
      }
   }

   private void checkTemplate () throws TemplateGeneratorException
   {
      if (mDescription == null)
      {
         throw new TemplateGeneratorException(
               "Description file '"
               + DESCRIPTION_FILE
               + "' missing in template zip file " + mFileName);
      }

      final Set allParams = new HashSet();
      for (final Iterator it = mTemplateList.iterator(); it.hasNext(); )
      {
         final Template t = (Template) it.next();
         allParams.addAll(t.getParameters());
      }

      for (final Iterator it = allParams.iterator(); it.hasNext(); )
      {
         final String param = (String) it.next();
         if (! mDescription.getParameterMap().containsKey(param))
         {
            throw new TemplateGeneratorException(
                  "Parameter description for parameter '" + param
                  + "' not found in template.xml");
         }
      }
   }

   public TemplateDescr getDescription ()
   {
      return mDescription;
   }

   public List getTemplates ()
   {
      return mTemplateList;
   }

   public static void main (String[] args)
         throws IOException,
            ParserConfigurationException,
            SAXException,
            FactoryConfigurationError,
            TemplateGeneratorException
   {
      final TemplateZip tz = new TemplateZip("D:\\temp\\foo.zip");
      tz.readTemplateFile();
      System.out.println("Got " + tz.getTemplates().size() + " templates");
   }
}
