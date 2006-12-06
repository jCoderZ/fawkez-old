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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.jcoderz.phoenix.sqlparser.jaxb.Index;
import org.jcoderz.phoenix.sqlparser.jaxb.SqlMetainf;
import org.jcoderz.phoenix.sqlparser.jaxb.Table;

/**
 * @author Albrecht Messner
 */
public class SqlTransformer
{
   private static final String DEFAULT_KEY = "default";
   private static final int INITIAL = 0;
   private static final int IN_CREATE = 1;
   private final File mInputFile;
   private final File mOutputFile;
   private File mMetainfFile;
   private final boolean mForce;
   
   private int mState;
   private TokenType mObjectType;
   private String mObjectName;

   private final Map mIndexMap = new HashMap();
   private final Map mTableMap = new HashMap();

   /**
    * Constructor.
    * @param inFile the input file
    * @param outFile the output file
    * @param metainfFile file containing meta information
    * @param force force transformation flag
    */
   public SqlTransformer (
         String inFile, String outFile, String metainfFile, boolean force)
   {
      mInputFile = new File(inFile);
      mOutputFile = new File(outFile);
      if (metainfFile != null)
      {
         mMetainfFile = new File(metainfFile);
         parseMetainfFile();
      }
      mForce = force;
   }
   
   /**
    * Executes transformation based on timestamp checking and force flag.
    */
   public void execute ()
   {
      try
      {
         if (checkFiles())
         {
            filterComments();
         }
         else if (mForce)
         {
            System.out.println("Forcing transformation...");
            filterComments();
         }
         else
         {
            System.out.println(
                  "Output file is newer than input file, skipping.");
         }
      }
      // possible exceptions: FileNotFound, JAXB, IO, Parse
      catch (Exception e)
      {
         System.err.println(
               "SQL Transformation failed for file " + mInputFile);
         throw new RuntimeException("SQL Transformation failed: " + e, e);
      }
   }
   
   /**
    * Check status of input and output files.
    * @return true if the transformation needs to be performed,
    *       false otherwise
    * @throws IOException if the input file does not exist
    */
   public boolean checkFiles ()
         throws IOException
   {
      boolean result = true;
      if (! mInputFile.exists())
      {
         throw new IOException("Input file " + mInputFile + " does not exist.");
      }
      
      if (mOutputFile.exists()
            && mInputFile.lastModified() < mOutputFile.lastModified())
      {
         result = false;
      }
      
      return result;
   }
   
   /**
    * Filters all comments out of the input file and writes the filtered
    * SQL to the output file.
    * 
    * @throws FileNotFoundException if the input file could not be opened
    * @throws ParseException if the input file could not be parsed
    */
   public void filterComments () throws FileNotFoundException, ParseException
   {
      //System.out.println("SqlCommentFilter: transforming "
      //      + mInputFile + " to " + mOutputFile);

      final ScannerInterface scanner
            = new SqlScanner(new FileInputStream(mInputFile));
      scanner.setReportWhitespace(true);
      
      final PrintWriter p2w 
              = new PrintWriter(new FileOutputStream(mOutputFile));

      Token token;
      StringBuffer sbuf = new StringBuffer();
      while ((token = scanner.nextToken()).getType() != TokenType.EOF)
      {
         parserHook(token, sbuf);

         final TokenType type = token.getType();

         // System.out.println("Token: " + token);
         if (type == TokenType.NEWLINE)
         {
            final String s = sbuf.toString();
            if (! (s.trim().length() == 0))
            {
               // System.out.println("Writing : '" + s + "'");
               p2w.println(s);
            }
            else
            {
               // System.out.println("Skipping: '" + s + "'");
            }
            sbuf = new StringBuffer();
         }
         else if (type != TokenType.COMMENT)
         {
            sbuf.append(token.getValue());
            if (type == TokenType.SEMICOLON)
            {
               // separate statements with an empty line
               sbuf.append('\n');
            }
         }
      }
      if (! (sbuf.toString().trim().length() == 0))
      {
         p2w.println(sbuf.toString());
      }
      
      p2w.flush();
      p2w.close();
   }

   private void parserHook (Token token, StringBuffer out)
   {
      final TokenType type = token.getType();
      if (type == TokenType.WHITESPACE || type == TokenType.NEWLINE)
      {
         // nop
      }
      else if (type == TokenType.CREATE)
      {
         if (mState != INITIAL)
         {
            throw new IllegalStateException("Expected state to be INITIAL");
         }
         mState = IN_CREATE;
      }
      else if (type == TokenType.TABLE
            || type == TokenType.INDEX)
      {
         if (mState == IN_CREATE)
         {
            mObjectType = type;
         }
      }
      else if (mState == IN_CREATE && mObjectType != null
            && mObjectName == null)
      {
         if (type != TokenType.IDENTIFIER)
         {
            throw new IllegalStateException(
                  "Expected identifier but got " + token);
         }
         mObjectName = token.getValue();
      }
      else if (type == TokenType.SEMICOLON)
      {
         if (mState == IN_CREATE && mObjectType != null && mObjectName != null)
         {
            // now we're at the end of a "CREATE TABLE"
            // or "CREATE INDEX" clause
            printMetaInf(out);
         }
         mState = INITIAL;
         mObjectType = null;
         mObjectName = null;
      }
   }

   
   /**
    * @param out
    */
   private void printMetaInf (StringBuffer out)
   {
      if (mMetainfFile != null)
      {
         final String metaInf;
         final Map metainfMap = getMetainfMap(mObjectType);
         
         if (metainfMap.get(mObjectName.toUpperCase()) == null)
         {
            metaInf = (String) metainfMap.get(DEFAULT_KEY);
         }
         else
         {
            metaInf = (String) metainfMap.get(mObjectName.toUpperCase());
         }

         out.append('\n');
         out.append(metaInf);
      }
   }

   private Map getMetainfMap (TokenType t)
   {
      final Map result;
      if (t == TokenType.INDEX)
      {
         result = mIndexMap;
      }
      else if (t == TokenType.TABLE)
      {
         result = mTableMap;
      }
      else
      {
         throw new IllegalArgumentException("Illegal Token Type: " + t);
      }
      return result;
   }
   
   private void parseMetainfFile ()
   {
      SqlMetainf metaInf = null;
      try
      {
         final JAXBContext ctx
               = JAXBContext.newInstance("org.jcoderz.phoenix.sqlparser.jaxb",
                  this.getClass().getClassLoader());
         final Unmarshaller unmarsh = ctx.createUnmarshaller();
         unmarsh.setValidating(true);
         metaInf = (SqlMetainf) unmarsh.unmarshal(mMetainfFile);
      }
      catch (JAXBException e)
      {
         e.printStackTrace();
         System.exit(1);
      }
      mTableMap.put(DEFAULT_KEY, metaInf.getCreateTable().getDefault());
      for (final Iterator it = metaInf.getCreateTable().getTable().iterator();
         it.hasNext(); )
      {
         final Table tab = (Table) it.next();
         if (mTableMap.containsKey(tab.getName()))
         {
            throw new IllegalArgumentException(
                  "Table " + tab.getName() + " exists twice in "
                  + mMetainfFile.getName());
         }
         mTableMap.put(tab.getName().toUpperCase(), tab.getValue());
      }
      
      mIndexMap.put(DEFAULT_KEY, metaInf.getCreateIndex().getDefault());
      for (final Iterator it = metaInf.getCreateIndex().getIndex().iterator();
         it.hasNext(); )
      {
         final Index ind = (Index) it.next();
         if (mIndexMap.containsKey(ind.getName()))
         {
            throw new IllegalArgumentException(
                  "Index " + ind.getName() + " exists twice in "
                  + mMetainfFile.getName());
         }
         mIndexMap.put(ind.getName().toUpperCase(), ind.getValue());
      }
   }

   /**
    * Main method.
    * @param args command line args
    */
   public static void main (String[] args)
   {
      final Options opts = parseCommandLine(args);
      checkOptions(opts);

      if (opts.mUseFiles)
      {
         final SqlTransformer filter = new SqlTransformer(
               opts.mInputFile, opts.mOutputFile, opts.mMetainfFile,
               opts.mForce);
         filter.execute();
      }
      else
      {
         final List files = checkAndListFiles(opts);
         for (final Iterator it = files.iterator(); it.hasNext(); )
         {
            final File inFile = (File) it.next();
            final File outFile = new File(opts.mOutDir, inFile.getName());
            final SqlTransformer filter = new SqlTransformer(
                  inFile.getAbsolutePath(), outFile.getAbsolutePath(),
                  opts.mMetainfFile, opts.mForce);
            filter.execute();
         }
      }
   }
   
   private static List checkAndListFiles (Options opts)
   {
      final File inDir = new File(opts.mInDir);
      checkDir(inDir);
      final File outDir = new File(opts.mOutDir);
      checkDir(outDir);
      
      final List list = new ArrayList();
      final FileFilter ff = new FileFilter() {
         public boolean accept (File pathname)
         {
            final boolean result;
            if (pathname.getName().endsWith(".sql"))
            {
               result = true;
            }
            else
            {
               result = false;
            }
            return result;
         }
      };
      final File[] inFiles = inDir.listFiles(ff);
      list.addAll(Arrays.asList(inFiles));
      return list;
   }
   
   private static void checkDir (File dir)
   {
      if (! dir.exists())
      {
         System.err.println("Directory " + dir + " does not exist");
         System.exit(1);
      }      
   }

   /**
    * @param opts
    */
   private static void checkOptions (final Options opts)
   {
      if (opts.mUseFiles && opts.mUseDirs)
      {
         System.err.println("Specify either '-i' and '-o' options "
               + "or '-d' and '-t' options.");
         usage();
      }
      
      if (opts.mUseFiles)
      {
         if (opts.mInputFile == null || opts.mOutputFile == null)
         {
            usage();
         }
      }
      else if (opts.mUseDirs)
      {
         if (opts.mInDir == null || opts.mOutDir == null)
         {
            usage();
         }
      }
      else
      {
         usage();
      }
   }

   private static Options parseCommandLine (String[] args)
   {
      final Options opts = new Options();
      int i = 0;
      try
      {
         for (i = 0; i < args.length; i++)
         {
            if (args[i].equals("-i"))
            {
               opts.mInputFile = args[++i];
               opts.mUseFiles = true;
            }
            else if (args[i].equals("-o"))
            {
               opts.mOutputFile = args[++i];
               opts.mUseFiles = true;
            }
            else if (args[i].equals("-d"))
            {
               opts.mInDir = args[++i];
               opts.mUseDirs = true;
            }
            else if (args[i].equals("-t"))
            {
               opts.mOutDir = args[++i];
               opts.mUseDirs = true;
            }
            else if (args[i].equals("-f"))
            {
               opts.mForce = true;
            }
            else if (args[i].equals("-m"))
            {
               opts.mMetainfFile = args[++i]; 
            }
            else
            {
               usage();
            }
         }
      }
      catch (ArrayIndexOutOfBoundsException x)
      {
         System.err.println("Error: argument "
            + args[i - 1] + " requires an option");
         usage();
      }
      return opts;
   }

   private static void usage ()
   {
      System.err.println("Usage: SqlCommentFilter");
      System.err.println("   -i <input_file>    ... input file to transform");
      System.err.println("   -o <output_file>   ... output file to write to");
      System.err.println("   -d <input_dir>     ... transform all files from"
            + " directory");
      System.err.println("   -t <to_dir>        ... write files to directory");
      System.err.println("   -m <metainf_file>  ... use sql metainf file");
      System.err.println("   -f                 ... force transformation even"
            + " if input file is newer");
      System.err.println("Note: you can either give '-i' and '-o' or ");
      System.err.println("      '-d' and '-t'");
      System.exit(1);
   }
   
   private static class Options
   {
      private String mInputFile = null;
      private String mOutputFile = null;
      private boolean mUseFiles = false;
      
      private String mInDir = null;
      private String mOutDir = null;
      private boolean mUseDirs = false;
      
      private String mMetainfFile = null;
      private boolean mForce = false;
   }
}
