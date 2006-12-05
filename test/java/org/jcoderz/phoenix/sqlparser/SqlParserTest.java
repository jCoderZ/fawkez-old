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
import java.io.FileFilter;
import java.io.FileInputStream;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author Albrecht Messner
 */
public class SqlParserTest extends TestCase
{
   /**
    * Constructor for TestSqlParser.
    * @param name the testcase name
    */
   public SqlParserTest (String name)
   {
      super(name);
   }

   /**
    * main method
    * @param args command line args
    */
   public static void main (String[] args)
   {
      TestRunner.run(suite());
   }

   /**
    * create a new SQL parser test for all files in the test/data directory
    * 
    * @return the a test suite with all tests
    */
   public static Test suite ()
   {
      final String basedir = System.getProperty("basedir", ".");
      File dataDir = new File(basedir + File.separator + "test/data");
      FileFilter sqlFilter = new FileFilter() {
         public final boolean accept (File pathname)
         {
            return pathname.getName().endsWith(".sql");
         }
      };
      File[] sqlFiles = dataDir.listFiles(sqlFilter);

      TestSuite suite = new TestSuite();

      for (int i = 0; i < sqlFiles.length; i++)
      {
         suite.addTest(new ParseFileTest("testSqlParser", sqlFiles[i]));
      }
      
      return suite;
   }
   
   static class ParseFileTest extends TestCase
   {
      private File mSqlFile;

      public ParseFileTest (String name, File sqlFile)
      {
         super(name);
         mSqlFile = sqlFile;
      }
      
      /** {@inheritDoc} */
      protected void runTest () throws Throwable
      {
         testSqlParser();
      }
      
      public void testSqlParser ()
            throws Exception
      {
         SqlScanner scanner = new SqlScanner(new FileInputStream(mSqlFile));
         SqlParser parser = new SqlParser(scanner);
         parser.parse();
      }
   }
}
