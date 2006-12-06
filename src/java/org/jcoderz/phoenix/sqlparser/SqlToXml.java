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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

/**
 * @author Albrecht Messner
 */
public class SqlToXml
{
   private File mInputFile;
   private File mOutputFile;
   
   public SqlToXml (String inFileName, String outFileName)
   {
      mInputFile = new File(inFileName);
      mOutputFile = new File(outFileName);
   }

   public void transformSqlToXml ()
         throws FileNotFoundException, ParseException
   {
      final ScannerInterface scanner
            = new SqlScanner(new FileInputStream(mInputFile));
      final SqlParser parser = new SqlParser(scanner);

      final PrintWriter pw = new PrintWriter(new FileOutputStream(mOutputFile));
      pw.println("<tables>");

      final List statements  = parser.parse();
      for (final Iterator it = statements.iterator(); it.hasNext(); )
      {
         final SqlStatement stmt = (SqlStatement) it.next();
         if (stmt instanceof CreateTableStatement)
         {
            transformStatementToXml((CreateTableStatement) stmt, pw);
         }
         else if (stmt instanceof CreateSequenceStatement)
         {
            transformStatementToXml((CreateSequenceStatement) stmt, pw);
         }
      }
      
      pw.println("</tables>");
      pw.flush();
      pw.close();
   }

   private void transformStatementToXml (CreateSequenceStatement stmt, 
           PrintWriter pw)
   {
      pw.println("<sequence name=\"" + stmt.getName() + "\">");
      pw.println("   <desc>");
      pw.println(stmt.getAnnotation() != null ? stmt.getAnnotation() : "");
      pw.println("   </desc>");
      pw.println("</sequence>");
   }

   private void transformStatementToXml (CreateTableStatement stmt, 
           PrintWriter out)
   {
      out.println("<table name=\"" + stmt.getTableName() + "\">");
      
      if (stmt.getAnnotation() != null)
      {
         out.println(stmt.getAnnotation());         
      }
      
      for (final Iterator it = stmt.getColumns().iterator(); it.hasNext(); )
      {
         final ColumnSpec column = (ColumnSpec) it.next();
         out.print("   <column "
               + "name=\"" + column.getColumnName() + "\" "
               + "type=\"" + column.getColumnType());
         StringBuffer sbuf = null;
         for (final Iterator it2 = column.getDatatypeAttributes().iterator();
               it2.hasNext(); )
         {
            if (sbuf == null)
            {
               sbuf = new StringBuffer();
               sbuf.append('(');
            }
            else
            {
               sbuf.append(',');
            }
            final NumericAttribute attr = (NumericAttribute) it2.next();
            sbuf.append(attr.getNumber());
         }
         if (sbuf != null)
         {
            sbuf.append(')');
            out.print(sbuf);
         }
         
         out.println("\">");
         if (column.getAnnotation() != null)
         {
            out.println(column.getAnnotation());
         }
         if (column.isNotNull())
         {
            out.println("      <notnull/>");
         }
         if (column.isPrimaryKey())
         {
            out.println("      <primarykey/>");
         }
         if (column.isUnique())
         {
            out.println("      <unique/>");
         }
         out.println("   </column>");
      }

      for (final Iterator it = stmt.getFkConstraints().iterator(); 
          it.hasNext(); )
      {
         final FkConstraint fk = (FkConstraint) it.next();
         out.println("   <fk name=\"" + fk.getName() + "\""
               + " references=\"" + fk.getRefTable() + "\">");
         out.println("      <columns>");
         for (final Iterator it2 = fk.getColumns().iterator(); it2.hasNext(); )
         {
            out.println("         <col>" + it2.next() + "</col>");
         }
         out.println("      </columns>");
         out.println("      <refcolumns>");
         for (final Iterator it2 = fk.getRefColumns().iterator(); 
             it2.hasNext(); )
         {
            out.println("         <col>" + it2.next() + "</col>");
         }
         out.println("      </refcolumns>");
         out.println("   </fk>");
      }
      
      for (final Iterator it = stmt.getIndexes().iterator(); it.hasNext(); )
      {
         final CreateIndexStatement idxStmt = (CreateIndexStatement) it.next();
         out.println("   <index name=\"" + idxStmt.getIndexName() + "\">");
         if (idxStmt.isUnique())
         {
            out.println("      <unique/>");  
         }
         out.println("      <desc>");
         out.println(
               idxStmt.getAnnotation() != null ? idxStmt.getAnnotation() : "");
         out.println("      </desc>");
         for (final Iterator it2 = idxStmt.getColumnNames().iterator();
               it2.hasNext(); )
         {
            out.println("      <column name=\"" + it2.next() + "\"/>");
         }
         out.println("   </index>");
      }
      out.println("</table>");
   }

   public static void main (String[] args)
   {
      String inputFile = null;
      String outputFile = null;
      
      int i = 0;
      try
      {
         for (i = 0; i < args.length; i++)
         {
            if (args[i].equals("-i"))
            {
               inputFile = args[++i];
            }
            else if (args[i].equals("-o"))
            {
               outputFile = args[++i];
            }
         }
         
      }
      catch (ArrayIndexOutOfBoundsException x)
      {
         System.err.println("Error: argument "
            + args[i - 1] + " requires an option");
         usage();
      }
      
      if (inputFile == null || outputFile == null)
      {
         usage();
      }
      
      final SqlToXml transformer = new SqlToXml(inputFile, outputFile);
      try
      {
         transformer.transformSqlToXml();
      }
      catch (Exception e)
      {
         System.err.println("File transformation failed: " + e.getMessage());
         e.printStackTrace(System.err);
         System.exit(1);
      }
   }
   
   private static void usage ()
   {
      System.err.println("Usage: SqlToXml -i <input_file> -o <output_file>");
      System.exit(1);
   }
}
