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
package org.jcoderz.phoenix.sqlparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test for the {@link org.jcoderz.phoenix.sqlparser.SqlScanner}.
 *  
 * @author Michael Griffel
 */
public class SqlScannerTest 
      extends TestCase
{
   private static final String SQL_DIRECTORY = "test/data";
   /**
    * Generating this test suite.
    * @return this test suite.
    */
   public static Test suite ()
   {
      final File dir = new File(getSqlDirectory());
      final String[] sqlFiles = dir.list(new SqlFilenameFilter());
      if (sqlFiles == null || sqlFiles.length == 0)
      {
         throw new RuntimeException("[Error] " 
               + "No SQL files found at '" + dir + "'!");
      }      

      final TestSuite suite = new TestSuite();
      for (int i = 0; i < sqlFiles.length; i++)
      {
         suite.addTest(new XmlFileValidation(dir, sqlFiles[i]));
      }
      
      return suite;
   }
   
   private static String getSqlDirectory ()
   {
      return System.getProperty("basedir", ".") 
         + File.separator + SQL_DIRECTORY;
   }

   static class SqlFilenameFilter 
         implements FilenameFilter
   {
      /**
       * @see FilenameFilter#accept(java.io.File, java.lang.String)
       */
      public boolean accept (File dir, String name)
      {
         return name.endsWith(".sql");
      }
   }


   static class XmlFileValidation 
         extends TestCase
   {      
      private final String mSqlFileName;
      private final File mDir;

      public XmlFileValidation (File dir, String sqlFileName)
      {
         super(sqlFileName);
         mSqlFileName = sqlFileName;
         mDir = dir;
      }

      public void runTest () 
            throws Throwable
      {
         testSqlFile();
      }

      public void testSqlFile ()
            throws Exception
      {
         final File file = new File(mDir, mSqlFileName);
         final StringWriter writer = new StringWriter();
         try
         {
            final SqlScanner scanner
                  = new SqlScanner(new FileInputStream(file));

            final List tokens = new ArrayList();
   
            for (;;)
            {
               final Token t = scanner.nextToken();
               if (t.getType() == TokenType.EOF)
               {
                  break;
               }
               tokens.add(t);
            }
   
            for (final Iterator iterator = tokens.iterator(); 
                  iterator.hasNext();)
            {
               final Token t = (Token) iterator.next();
               writer.write(t.getValue());
            }
            writer.close();
         }
         catch (Exception e)
         {
            e.printStackTrace();
            fail("Expected well-formed SQL file " + file + " : " + e);
         }
         diff(file, new LineNumberReader(new StringReader(writer.toString())));
      }

      private void diff (File file, LineNumberReader reader) 
            throws Exception
      {
         final LineNumberReader org 
               = new LineNumberReader(new FileReader(file));
         for (;;)
         {
            final String line1 = org.readLine();
            if (line1 == null) // EOF
            {
               break; 
            }
            final String line2 = reader.readLine();
            if (line2 == null) // EOF
            {
               fail("Unexpected end of file"); 
            }
            assertEquals("File: " + file + " Line " + org.getLineNumber() 
                  + " should be equals", line1, line2);
               
         }
      }
  
   }
}
