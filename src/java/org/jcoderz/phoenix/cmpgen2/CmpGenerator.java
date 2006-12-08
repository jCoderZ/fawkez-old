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
 package org.jcoderz.phoenix.cmpgen2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.phoenix.sqlparser.ColumnSpec;
import org.jcoderz.phoenix.sqlparser.CreateTableStatement;
import org.jcoderz.phoenix.sqlparser.SqlParser;
import org.jcoderz.phoenix.sqlparser.SqlScanner;
import org.jcoderz.phoenix.sqlparser.SqlStatement;


/**
 * This is a velocity based version of the CMP generator.
 * @author Albrecht Messner
 */
public class CmpGenerator
{
   /** The full qualified name of this class. */
   private static final String CLASSNAME = CmpGenerator.class.getName();
   /** The logger to use. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private static final String ARRAY_MAGIC = "[]";
   private static final int ARRAY_MAGIC_LENGTH = ARRAY_MAGIC.length();

   private final String mOutputBaseDirectory;
   private final String mPackagePrefix;
   private final String mDataSource;
   private File mOutputDirectory;
   private final String mTemplateDir;
   private final boolean mOverwrite;

   /**
    * Construct CMP bean generator.
    *
    * @param outputDir the output directory
    * @param pkgPrefix the package prefix
    * @param dataSource the jndi name of the data source
    */
   public CmpGenerator (String outputDir, String pkgPrefix, String dataSource,
         String templateDir, boolean overwrite)
   {
      mOutputBaseDirectory = outputDir;
      mPackagePrefix = pkgPrefix;
      mDataSource = dataSource;
      mTemplateDir = templateDir;
      mOverwrite = overwrite;
   }

   private static void usage (String errorText)
   {
      if (errorText != null)
      {
         System.err.println(errorText);
      }
      System.err.println("Usage: CmpGenerator ");
      System.err.println(
            "   -i <inputfile>        Input SQL file");
      System.err.println("   -d <outputdir>        Base output directory");
      System.err.println(
            "                         Package subdirs will be created here");
      System.err.println(
            "   -p <package>          Package of generated classes");
      System.err.println("                         Defaults to 'org.jcoderz'");
      System.err.println(
            "   -ds <datasource>      JNDI lookup name of bean's datasource");
      System.err.println(
            "   -t <templatedir>      Directory where bean "
            + "templates can be found");
      System.err.println("   -o                    Overwrite existing files");
      System.exit(1);
   }

   /**
    * Main method.
    * @param args command line args
    */
   public static void main (String[] args)
         throws Exception
   {
      String outputDir = ".";
      String pkgPrefix = "org.jcoderz";
      String dataSource = "jdbc/default";
      String inputFile = null;
      String templateDir = null;

      boolean overwrite = false;

      String currentArg = null;

      try
      {
         for (int i = 0; i < args.length; i++)
         {
            currentArg = args[i];
            if (currentArg.equals("-d"))
            {
               outputDir = args[++i];
            }
            else if (currentArg.equals("-i"))
            {
               inputFile = args[++i];
            }
            else if (currentArg.equals("-p"))
            {
               pkgPrefix = args[++i];
            }
            else if (currentArg.equals("-ds"))
            {
               dataSource = args[++i];
            }
            else if (currentArg.equals("-t"))
            {
               templateDir = args[++i];
            }
            else if (currentArg.equals("-o"))
            {
               overwrite = true;
            }
            else
            {
               usage("Unknown command line option " + currentArg);
            }
         }
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         usage("Command line option '" + currentArg + "' requires an option");
      }

      if (inputFile == null || templateDir == null)
      {
         usage("Mandatory parameter missing");
      }

      final CmpGenerator generator =
         new CmpGenerator(
               outputDir, pkgPrefix, dataSource, templateDir, overwrite);
      generator.generateCmpBeans(inputFile);
   }

   /**
    * Entry method for CMP bean generator.
    *
    * @param inputFile the input file
    */
   public final void generateCmpBeans (String inputFile)
         throws Exception
   {
      final String methodName = "generateCmpBeans(String)";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName, new Object[] {inputFile});
      }

      try
      {
         setUp();

         final FileInputStream fin = new FileInputStream(inputFile);
         final SqlScanner sqlScanner = new SqlScanner(fin);
         final SqlParser sqlParser = new SqlParser(sqlScanner);
         final List sqlStatements = sqlParser.parse();
         for (final Iterator it = sqlStatements.iterator(); it.hasNext();)
         {
            final SqlStatement statement = (SqlStatement) it.next();
            if (statement instanceof CreateTableStatement)
            {
               // mBeanImportList.clear();
               // mHelperImportList.clear();
               generateCmpBean((CreateTableStatement) statement);
            }
            else
            {
               logger.info("Skipping statement " + statement);
            }
         }
      }
      catch (Exception x)
      {
         logger.log(Level.SEVERE, "Error during CMP generation", x);
         throw x;
      }

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName);
      }
   }

   public String sqlNameToJavaName (String sqlName)
   {
      final StringReader rdr = new StringReader(sqlName);
      int c;
      boolean capitalizeNext = true;
      final StringBuffer result = new StringBuffer();
      try
      {
         while ((c = rdr.read()) != -1)
         {
            final char chr = (char) c;
            switch (chr)
            {
               case '_':
                  capitalizeNext = true;
                  break;
               default:
                  if (capitalizeNext)
                  {
                     result.append(Character.toUpperCase(chr));
                     capitalizeNext = false;
                  }
                  else
                  {
                     result.append(chr);
                  }
                  break;
            }
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException("Huh???", e);
      }
      return result.toString();
   }

   /**
    * This method capitalizes the first character of the given string and,
    * assuming that the argument is usually a java type, also takes care
    * of arrays by replacing "[]" with "Array".
    * @param s input to be modified.
    * @return adapted String.
    */
   public String capitalize (String s)
   {
      final String str;
      if (s.endsWith(ARRAY_MAGIC))
      {
         str = s.substring(0, s.length() - ARRAY_MAGIC_LENGTH) + "Array";
      }
      else
      {
         str = s;
      }
      final char c = str.charAt(0);
      final String result;
      if (Character.isUpperCase(c))
      {
          result = str;
      }
      else
      {
         result = String.valueOf(Character.toUpperCase(c)) + str.substring(1);
      }
      return result;
   }

   public String getUnqualifiedJavaType (ColumnSpec column)
         throws CmpGeneratorException
   {
      final String result = TypeMapping.getJavaType(column, false);
      if (result == null)
      {
         throw new RuntimeException("TypeMapping returned null for " + column);
      }
      return result;
   }

   public String getQualifiedJavaType (ColumnSpec column)
         throws CmpGeneratorException
   {
      return TypeMapping.getJavaType(column, true);
   }

   public String unqualifyType (String type)
   {
      return TypeMapping.unqualifyType(type);
   }

   private void generateCmpBean (CreateTableStatement stmt)
         throws Exception
   {
      if (! hasPrimaryKey(stmt))
      {
         logger.info("*** No PK Field available for "
               + stmt.getTableName() + ", skipping bean creation");
         return;
      }

      String baseName = stmt.getBeanName() + "Entity";
      if (baseName == null)
      {
         baseName = sqlNameToJavaName(stmt.getTableName());
      }
      logger.info("*** Start creation of bean " + baseName);

      final String valueInterface = mergeTemplate(
            getVelocityContext(stmt, baseName), "GenerateValue.vtl");
      final File valueIfOutputFile = new File(mOutputDirectory,
            baseName + "Value.java");
      writeFile(valueIfOutputFile, valueInterface);

      final String valueImpl = mergeTemplate(
            getVelocityContext(stmt, baseName), "GenerateValueImpl.vtl");
      final File valueImplOutputFile = new File(mOutputDirectory,
            baseName + "ValueImpl.java");
      writeFile(valueImplOutputFile, valueImpl);

      final String bean = mergeTemplate(
            getVelocityContext(stmt, baseName), "GenerateBean.vtl");
      final File beanOutputFile = new File(mOutputDirectory,
            baseName + "Bean.java");
      writeFile(beanOutputFile, bean);

      if (checkIfHelperRequired(stmt))
      {
         final String helper = mergeTemplate(
               getVelocityContext(stmt, baseName), "GenerateHelper.vtl");
         final File helperOutputFile = new File(mOutputDirectory,
               baseName + "TypeConverter.java");
         writeFile(helperOutputFile, helper);
      }
   }

   private String mergeTemplate (VelocityContext ctx, String templateFile)
         throws Exception
   {
      final StringWriter sw = new StringWriter();
      final Template template = Velocity.getTemplate(templateFile);
      template.merge(ctx, sw);
      return sw.getBuffer().toString();
   }

   private void writeFile (File outputFile, final String data)
         throws IOException
   {
      if (outputFile.exists() && !mOverwrite)
      {
         throw new RuntimeException(
               "Output file " + outputFile + " already exists");
      }
      final FileWriter fout = new FileWriter(outputFile);
      try
      {
          fout.write(data);
      }
      finally
      {
          IoUtil.close(fout);
      }
      logger.info("Wrote file " + outputFile + " successfully");
   }

   /**
    * @param stmt
    * @param baseName
    * @return
    * @throws Exception
    */
   private VelocityContext getVelocityContext (CreateTableStatement stmt,
         String baseName)
         throws Exception
   {
      final Properties velocityProps = new Properties();
      velocityProps.put(RuntimeConstants.RESOURCE_LOADER, "file");
      velocityProps.put("file.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
      velocityProps.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, 
              mTemplateDir);
      velocityProps.put(RuntimeConstants.VM_LIBRARY, "macros.vm");
      // velocityProps.put("runtime.log", "/tmp/velocity.log");
      velocityProps.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
              logger.isLoggable(Level.FINER)
                  ? "org.apache.velocity.runtime.log.SimpleLog4JLogSystem"
                  : "org.apache.velocity.runtime.log.NullLogSystem");
      Velocity.init(velocityProps);
      final VelocityContext ctx = new VelocityContext();
      ctx.put("stmt", stmt);
      ctx.put("baseName", baseName);
      ctx.put("package", mPackagePrefix);
      ctx.put("datasource", mDataSource);
      ctx.put("cmpgen", this);
      return ctx;
   }

   /**
    *
    * @throws CmpGeneratorException
    */
   private void setUp () throws CmpGeneratorException
   {
      File outputDir = new File(mOutputBaseDirectory);
      if (!outputDir.exists())
      {
         throw new CmpGeneratorException("Output directory does not exist");
      }
      if (!outputDir.isDirectory())
      {
         throw new CmpGeneratorException(
            mOutputBaseDirectory + " is not a directory");
      }

      final StringTokenizer tok = new StringTokenizer(mPackagePrefix, ".");

      while (tok.hasMoreTokens())
      {
         outputDir = new File(outputDir, tok.nextToken());
      }
      logger.finer("Generating package subdirectories for " + outputDir);
      outputDir.mkdirs();
      mOutputDirectory = outputDir;
   }


   private boolean hasPrimaryKey (CreateTableStatement statement)
   {
      boolean hasPrimaryKey = false;
      for (final Iterator it = statement.getColumns().iterator(); 
          it.hasNext(); )
      {
         final ColumnSpec column = (ColumnSpec) it.next();
         if (column.isPrimaryKey())
         {
            hasPrimaryKey = true;
            break;
         }
      }
      return hasPrimaryKey;
   }

   /**
    * Checks if a helper class is required for this bean
    *
    * a helper class is required as soon as a custom java type is used
    * with a load method and a store method.
    *
    * @param statement the parsed SQL create statement
    * @return true if a helper class is required, false otherwise
    */
   public boolean checkIfHelperRequired (CreateTableStatement statement)
   {
      boolean needHelper = false;

      for (final Iterator it = statement.getColumns().iterator(); 
          it.hasNext(); )
      {
         final ColumnSpec column = (ColumnSpec) it.next();
         if (column.getJavaType() != null)
         {
            if (column.getStoreMethod() != null)
            {
               needHelper = true;
               break;
            }
         }
      }
      return needHelper;
   }

   public String getVersion ()
   {
      return "$Revision: 1.9 $";
   }

   public Set buildBeanImportList (CreateTableStatement statement)
         throws CmpGeneratorException
   {
      final Set beanImportList = new TreeSet();
      for (final Iterator it = statement.getColumns().iterator(); 
          it.hasNext(); )
      {
         final ColumnSpec column = (ColumnSpec) it.next();
         final String javaType = TypeMapping.getJavaType(column, true);
         if (javaType != null
               && !javaType.startsWith("java.lang")
               && !TypeMapping.isPrimitiveType(javaType))
         {
            beanImportList.add(javaType);
         }
      }

      beanImportList.add("org.jcoderz.commons.types.Period");
      beanImportList.add("javax.ejb.CreateException");
      return beanImportList;
   }

   public Set buildHelperImportList (CreateTableStatement statement)
         throws CmpGeneratorException
   {
      final Set helperImportList = new TreeSet();
      for (final Iterator it = statement.getColumns().iterator(); 
          it.hasNext(); )
      {
         final ColumnSpec column = (ColumnSpec) it.next();
         final String javaType = TypeMapping.getJavaType(column, true);
         if (javaType != null
               && !javaType.equals("java.sql.Date") // don't import date since
                                                    // refs to date are always
                                                    // fully qualified.
               && !javaType.startsWith("java.lang")
               && !TypeMapping.isPrimitiveType(javaType))
         {
            helperImportList.add(javaType);
         }
         final String complexType = column.getJavaType();
         if (complexType != null
               && !complexType.startsWith("java.lang")
               && !TypeMapping.isPrimitiveType(complexType))
         {
            helperImportList.add(complexType);
         }
      }

      // extra imports:
      helperImportList.add("org.jcoderz.commons.InconsistentDatabaseException");
      helperImportList.add("org.jcoderz.commons.types.Date");
      helperImportList.add("org.jcoderz.commons.types.Period");
      helperImportList.add("org.jcoderz.commons.util.Assert");

      return helperImportList;
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      return "[Phoenix CmpGenerator " + getVersion() + "]";
   }

   public boolean isPrimitiveType (String type)
   {
      return TypeMapping.isPrimitiveType(type);
   }
}
