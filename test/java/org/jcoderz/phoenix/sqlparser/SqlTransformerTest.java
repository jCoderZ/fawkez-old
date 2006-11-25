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
import org.jcoderz.commons.TestCase;

/**
 * @author Albrecht Messner
 */
public class SqlTransformerTest
      extends TestCase
{
   /**
    * tests the SqlCommentFilter.
    * @throws Exception if the testcase fails
    */
   public void testSqlCommentFilter ()
         throws Exception
   {
       final String baseDir = getBaseDir().getAbsolutePath();
      final String inFile = baseDir + "/test/data/config_create_table.sql";
      final String outFile = baseDir + "/build/config_create_table.sql";
      final String metainfFile = baseDir 
          + "/test/data/sqltransformer-metainf.xml";
      
      final String[] args = {
            "-f",
            "-i", inFile,
            "-o", outFile,
            "-m", metainfFile };
      SqlTransformer.main(args);
   }
   
   /**
    * Tests whole directory transformation.
    */
   public void testDirectoryTransformation ()
   {
       final String baseDir = getBaseDir().getAbsolutePath();
      final String metainfFile = baseDir + "/test/data/sqltransformer-metainf.xml";
      final String inDir = baseDir + "/test/data";
      final String outDir = baseDir + "/build/sql.transformed";
      final File d = new File(outDir);
      d.mkdir();
      
      final String[] args = {
            "-f",
            "-d", inDir,
            "-t", outDir,
            "-m", metainfFile };
      SqlTransformer.main(args);
   }
}
