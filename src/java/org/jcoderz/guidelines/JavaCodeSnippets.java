/*
 * $Id: JavaCodeSnippets.java 255 2006-07-12 14:38:21Z amandel $
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
package org.jcoderz.guidelines;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;

import org.jcoderz.commons.util.XmlUtil;


/**
 * Cuts code samples out of java classes.
 *
 * @author mgriffel
 */
public final class JavaCodeSnippets
{
   private static final String JAVA_CODE_SNIPPET_XML = "code-snippet-catalog.xml";
   private static final String NEWLINE = System.getProperty("line.separator");
   private static final String BEGIN_SNIPPET_TAG = "BEGIN SNIPPET:";
   private static final int NUMBER_OF_ARGUMENTS = 2;

   /**
    * Main method.
    * @param args program args.
    * @throws IOException File not found, read error.
    */
   public static void main (String[] args) throws IOException
   {
      try
      {
         if (args.length != NUMBER_OF_ARGUMENTS)
         {
            usage ();
         }

         JavaCodeSnippets jcs = new JavaCodeSnippets();
      
         final File srcDir = new File(args[0]);      
         final File outDir = new File(args[1]);
      
         if (!srcDir.isDirectory())
         {
            throw new RuntimeException("The srcDir '" + srcDir 
                  + "'is not a valid directory");
         }

         if (!outDir.isDirectory())
         {
            throw new RuntimeException("The outDir '" + outDir 
                  + "'is not a valid directory");
         }
         
         jcs.generateSnippets(srcDir, outDir);
         jcs.writeCodeSnippetCatalog (outDir);         
      }
      catch (Exception e)
      {
         e.printStackTrace();
         System.exit(-1);
      }
   }

   private void generateSnippets (File srcDir, File outDir) 
      throws FileNotFoundException, IOException
   {
      final File[] files = srcDir.listFiles(new FilenameFilter()
      {
         public boolean accept (File dir, String name)
         {
            boolean ret = false;
            if (name.endsWith(".java"))
            {
               ret = true;
            }
            return ret;
         }
      });
      
      for (int i = 0; i < files.length; i++)
      {
         System.out.println("Processing file '" + files[i].getName() + "'");
         generateSnippet(files[i], outDir);
      }
   }

   private void generateSnippet (File src, File outDir) 
      throws FileNotFoundException, IOException
   {
      final BufferedReader in = new BufferedReader(new FileReader(src));
      String line = null;
      int lineno = 0;
      int indent = 0;
      boolean inSnippetCode = false;
      final StringBuffer snippet = new StringBuffer();
      String snippetFilename = null;

      while ((line = in.readLine()) != null)
      {
         ++lineno;
         if (line.matches(".*// " + BEGIN_SNIPPET_TAG + " [a-zA-Z0-9\\.]*[ ]*"))
         {
            inSnippetCode = true;
            indent = line.indexOf("//") != -1 ? line.indexOf("//") : 0;
            snippetFilename = line.substring(
                  line.indexOf(BEGIN_SNIPPET_TAG)
                     + BEGIN_SNIPPET_TAG.length()).trim();
            continue;
         }
         else if (inSnippetCode && line.matches(".*// PAUSE SNIPPET.*"))
         {
            inSnippetCode = false;
         }
         else if (!inSnippetCode && line.matches(".*// RESUME SNIPPET.*"))
         {
            inSnippetCode = true;
            continue;
         }
         else if (inSnippetCode && line.matches(".*// END SNIPPET.*"))
         {
            inSnippetCode = false;
            final String codeSnippet = snippet.toString();
            System.out.println("### writing snippet: " +  snippetFilename 
                  + " indent: " + indent);
            final File outFilename = new File(outDir, snippetFilename);
            final FileOutputStream out
                  = new FileOutputStream(outFilename);

            out.write(codeSnippet.getBytes());
            out.close();

            snippet.setLength(0);
         }
         
         if (inSnippetCode)
         {
            if (line.length() > indent)
            {
               line = line.substring(indent);
            }
            snippet.append(XmlUtil.escape(line + NEWLINE));
         }
      }
      in.close();
   }

   /**
    * @param codeSnippetList
    * @param outDir
    */
   private void writeCodeSnippetCatalog (File dir)
         throws IOException
   {
      final File[] files = dir.listFiles(new FilenameFilter()
            {
         /**
          * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
          */
         public boolean accept (File dir, String name)
         {
            boolean ret = false;
            if (name.endsWith(".xml") && ! name.equals(JAVA_CODE_SNIPPET_XML))
            {
               ret = true;
            }
            return ret;
         }
      });
      
      final PrintStream out = new PrintStream(new FileOutputStream(
                  new File(dir, JAVA_CODE_SNIPPET_XML)));

      out.println("<?xml version='1.0' encoding='ISO-8859-1'?>");
      out.println();
      for (int i = 0; i < files.length; i++)
      {
         File file = files[i];
         String s = file.getName();
         String n = s;
         if (s.lastIndexOf('.') != -1)
         {
            n = s.substring(0, s.lastIndexOf("."));
         }
         out.println("<!ENTITY " + n + " SYSTEM \"" + s + "\">");
         
      }      
      out.close();
   }

   /**
    * Print usage message
    */
   private static void usage ()
   {
      System.out.println("Usage: java JavaCodeSnippets srcDir outDir");
      System.exit(-1);
   }
}
