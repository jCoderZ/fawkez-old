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
package org.jcoderz.phoenix.dbview;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.jcoderz.commons.util.Constants;
import org.jcoderz.commons.util.DbUtil;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.commons.util.XmlUtil;


/**
 * Converts the db content into xml.
 *
 * @author Andreas Mandel
 */
public class DbView
{
   public static final String DATASOURCE = "java:comp/env/jdbc/svs/db";

   /** Line separator to be used in output files. */
   public static final String LINE_SEPARATOR = Constants.LINE_SEPARATOR;

   private static final int MILLIS_PER_SECOND 
           = org.jcoderz.commons.types.Date.MILLIS_PER_SECOND;
   private static final String SELECT_ALL_TABLES = "select * from tab";
   private static final String CLASSNAME = DbView.class.getName();
   private static final Logger logger = Logger.getLogger(CLASSNAME);
   private static final int INDENT = 3;


   private final StringBuffer mStringBuffer = new StringBuffer();
   private final Map mTypeMapper = new HashMap();
   private final DateFormat mDateFormater
         = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", 
                 Constants.SYSTEM_LOCALE);

   {
      mDateFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
   }

   private String mDbDriver = Constants.ORACLE_DRIVER_CLASS_NAME;
   private int mNumberOfColumns;
   private String mSqlStatement;
   private String mDbUrl;
   private String mDbUser;
   private String mDbPasswd;
   private File mOutputDir;
   private File mOutputFile;
   private Level mLogLevel;


   public static void main (String[] args)
         throws SecurityException
   {
      final DbView main = new DbView();
      try
      {
         main.parseArguments(args);
         if (main.mSqlStatement == null)
         {
            main.dumpAllTables();
         }
         else
         {
            main.performQuery();
         }
      }
      catch (IllegalArgumentException ex)
      {
         System.err.println(ex.getMessage());
         System.err.println("Usage:");
         System.err.println(DbView.class.getName());
         System.err.println(" -dbUrl [-dbUser scott -dbPasswd tiger] "
               + "-sql \"select * from tab\" "
               + "-outFile foo.xml");
         System.err.println("DbView -dbUrl [-dbUser scott -dbPasswd tiger] "
               + "-outDir sql/");
         System.err.println();
         System.err.println("To add type converters use:");
         System.err.println("-type ROW_NAME JAVA_CLASS FROM_DB TO_DISPLAY");
         System.err.println("ex.: -type STATUS com.encous.foo.Status fromInt "
               + "toDisplayString");
         System.err.println();
         System.err.println("To use a db driver other than oracle add:");
         System.err.println("-dbDriver your.db.driver");
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   public void dumpAllTables ()
         throws IOException, ClassNotFoundException, SQLException
   {
      dumpAllTables(mOutputDir);
   }

   public void performQuery ()
         throws IOException, ClassNotFoundException, SQLException
   {
      performConvertion (mOutputFile, mSqlStatement);
   }

   public void dumpAllTables (File dir)
         throws IOException, ClassNotFoundException, SQLException
   {
      Connection dbConnection = null;
      PreparedStatement statement = null;
      try
      {
         dbConnection = getConnectionFromDbUrl();

         performConvertion(new File(dir, "tables.xml").getCanonicalPath(),
               dbConnection, SELECT_ALL_TABLES);

         statement = dbConnection.prepareStatement(SELECT_ALL_TABLES);
         final ResultSet rs = statement.executeQuery();
         while (rs.next())
         {
            final String tableName = rs.getString(1);
            performConvertion(
                  new File(dir, tableName + ".xml").getCanonicalPath(),
                  dbConnection, "select * from " + tableName);
         }
      }
      finally
      {
          DbUtil.close(statement);
          DbUtil.close(dbConnection);
      }

   }

   private void parseArguments (String[] args)
         throws IOException, NoSuchMethodException, ClassNotFoundException
   {
      try
      {
         for (int i = 0; i < args.length; )
         {
            if (args[i].equals("-sql"))
            {
               mSqlStatement = args[++i];
            }
            else if (args[i].equals("-dbUrl"))
            {
               mDbUrl = args[++i];
            }
            else if (args[i].equals("-dbUser"))
            {
               mDbUser = args[++i];
            }
            else if (args[i].equals("-dbPasswd"))
            {
               mDbPasswd = args[++i];
            }
            else if (args[i].equals("-type"))
            {
               final String rowname = args[++i];
               final String classname = args[++i];
               final String fromDbType = args[++i];
               final String toDisplay = args[++i];
               addTypeMapping(rowname, classname, fromDbType, toDisplay);
            }
            else if (args[i].equals("-outDir"))
            {
               mOutputDir = new File(args[++i]);
            }
            else if (args[i].equals("-outFile"))
            {
               mOutputFile = new File(args[++i]);
            }
            else if (args[i].equals("-dbDriver"))
            {
               mDbDriver = args[++i];
            }
            else if (args[i].equals("-loglevel"))
            {
               mLogLevel = Level.parse(args[i + 1]);
               Logger.getLogger("").setLevel(mLogLevel);
            }
            else
            {
               throw new IllegalArgumentException(
                     "Invalid argument '" + args[i]  + "'");
            }
            ++i;
         }

         checkParameters();
      }
      catch (IndexOutOfBoundsException e)
      {
         throw new IllegalArgumentException("Missing value for "
            + args[args.length - 1]);
      }
   }

   private void checkParameters ()
        throws IllegalArgumentException
   {
       if (!mOutputDir.isDirectory())
       {
          throw new RuntimeException("out dir must be a directory.");
       }
       if (mOutputFile.isDirectory())
       {
          throw new RuntimeException(
                "out file must not be a directory.");
       }
       if (mOutputDir == null && mOutputFile == null)
       {
           throw new IllegalArgumentException(
               "Need either output dir or output file.");
       }
       if (mSqlStatement != null && mOutputFile != null)
       {
           throw new IllegalArgumentException(
              "Need output file for sql statement.");
       }
       if (mSqlStatement == null && mOutputDir == null)
       {
           throw new IllegalArgumentException(
                   "Need output dir for global dump.");
       }
   }

   public void performConvertion (File file, String query)
         throws IOException, ClassNotFoundException, SQLException
   {
      PrintWriter out = null;
      Connection dbConnection = null;
      PreparedStatement statement = null;
      try
      {
         final FileOutputStream stream = new FileOutputStream(file);
         final OutputStreamWriter writer = new OutputStreamWriter(stream,
               Charset.forName("us-ascii"));
         out = new PrintWriter(writer);
         dbConnection = getConnectionFromDbUrl();
         statement = dbConnection.prepareStatement(query);
         final ResultSet rs = statement.executeQuery();
         xmlOpen(null, dbConnection.getMetaData().getURL(), query, out);
         metaData2Xml(rs.getMetaData(), out);
         resultSet2Xml(rs, out);
         xmlClose(out);

      }
      finally
      {
         IoUtil.close(out);
         DbUtil.close(statement);
         DbUtil.close(dbConnection);
      }
   }

   public void addTypeMapping (String typeName, String typeClass,
         String fromDb, String toString)
         throws SecurityException, NoSuchMethodException, ClassNotFoundException
   {
      mTypeMapper.put(typeName,
            new TypeMapper(typeName, typeClass, fromDb, toString));
   }

   private void performConvertion (String fileName,
         final Connection dbConnection, String query)
         throws IOException, SQLException
   {
      PrintWriter out = null;
      PreparedStatement statement = null;
      try
      {
         final FileOutputStream stream = new FileOutputStream(fileName);
         final OutputStreamWriter writer = new OutputStreamWriter(stream,
               Charset.forName("us-ascii"));
         out = new PrintWriter(writer);
         statement = dbConnection.prepareStatement(query);
         final ResultSet rs = statement.executeQuery();
         xmlOpen(null, dbConnection.getMetaData().getURL(), query, out);
         metaData2Xml(rs.getMetaData(), out);
         resultSet2Xml(rs, out);
         xmlClose(out);

      }
      finally
      {
          IoUtil.close(out);
          DbUtil.close(statement);
      }
   }

   private Connection getConnectionFromDataSource ()
         throws Exception
   {
      final Context ctx = new InitialContext();
      final DataSource ds = (DataSource) ctx.lookup(DATASOURCE);
      final Connection con = ds.getConnection();
      return con;
   }

   private Connection getConnectionFromDbUrl ()
         throws ClassNotFoundException, SQLException
   {
      Class.forName(mDbDriver);
      final Connection con;

      if (mDbUser == null)
      {
         con = DriverManager.getConnection(mDbUrl);
      }
      else
      {
         con = DriverManager.getConnection(mDbUrl, mDbUser, mDbPasswd);
      }

      return con;
   }

   private void xmlOpen (String dataSource, String dataBaseUri,
         String statement, PrintWriter out)
         throws IOException, SQLException
   {
      out.print("<result statement='");
      out.print(XmlUtil.attributeEscape(statement));
      out.println("'");
      if (dataSource != null)
      {
         out.print("        data-source='");
         out.print(XmlUtil.attributeEscape(dataSource));
         out.println("'");
      }
      if (dataBaseUri != null)
      {
         out.print("        db-uri='");
         out.print(XmlUtil.attributeEscape(dataBaseUri));
         out.println("'");
      }
      out.print("        creation-date='");
      out.print(display(new Date()));
      out.println("'>");
   }

   private void xmlClose (PrintWriter out)
   {
      out.println("</result>");
   }

   private void metaData2Xml (ResultSetMetaData md, PrintWriter out)
         throws IOException, SQLException
   {
      mNumberOfColumns = md.getColumnCount();
      out.print("   <meta-data number-of-columns='");
      out.print(mNumberOfColumns);
      out.println("'>");
      for (int column = 1; column <= mNumberOfColumns; column++)
      {
         out.println("      <column");
         metaDataAsString("name", md.getColumnName(column), out);
         metaDataAsString("type-name", md.getColumnTypeName(column), out);
         metaDataAsString("display-name", md.getColumnLabel(column), out);
         metaDataAsString("class-name", md.getColumnClassName(column), out);
         try
         {
            metaDataAsString("precision", md.getPrecision(column), out);
         }
         catch (NumberFormatException ex)
         {
            // this happens for lobs in oracle seems to denote infinity.
         }
         metaDataAsString("scale", md.getScale(column), out);
         metaDataAsString("catalog-name", md.getCatalogName(column), out);
         metaDataAsString("schema-name", md.getSchemaName(column), out);
         metaDataAsString("table-name", md.getTableName(column), out);
         metaDataAsStringNullable("is-nullable", md.isNullable(column), out);
         metaDataAsString("type", md.getColumnType(column), out);
         metaDataAsString("display-size", md.getColumnDisplaySize(column), out);
         out.println("      />");
      }
      out.println("   </meta-data>");
   }


   private void metaDataAsStringNullable (
         String attributeName, int nullable, PrintWriter out)
   {
      switch (nullable)
      {
         case ResultSetMetaData.columnNoNulls:
            metaDataAsString(attributeName, "no-nulls", out);
            break;
         case ResultSetMetaData.columnNullable:
            metaDataAsString(attributeName, "nullable", out);
            break;
         case ResultSetMetaData.columnNullableUnknown:
            metaDataAsString(attributeName, "unknown", out);
            break;
         default:
            metaDataAsString(attributeName, "illeagal", out);
         break;
      }
   }

   /**
    * @param string
    * @param i
    * @param out
    */
   private void metaDataAsString (
         String attributeName, int i, PrintWriter out)
   {
      metaDataAsString(attributeName, String.valueOf(i), out);
   }


   /**
    * @param string
    * @param string2
    * @param out
    */
   private void metaDataAsString (String attributeName,
         String attributeValue, PrintWriter out)
   {
      out.print("              ");
      out.print(attributeName);
      out.print("='");
      out.print(XmlUtil.attributeEscape(attributeValue));
      out.println("'");
   }



   private void resultSet2Xml (ResultSet rs, PrintWriter out)
         throws IOException, SQLException
   {
      out.println("   <result-set>");
      while (rs.next())
      {
         out.print("      <row row-number='");
         out.print(rs.getRow());
         out.println("'>");
         result2Xml(rs, out);
         out.println("      </row>");
      }
      out.println("   </result-set>");
   }

   private void result2Xml (ResultSet rs, PrintWriter out)
         throws SQLException, IOException
   {
      for (int column = 1; column <= mNumberOfColumns; column++)
      {
         final Object o = typedGetter(rs, column);
         out.print("         <column");
         if (o == null)
         {
            out.println(" isNull='true'>");
         }
         else
         {
            out.println(">");
         }
         final String name = rs.getMetaData().getColumnName(column);
         final String typeName = rs.getMetaData().getColumnTypeName(column);
         object2Xml(o, name, typeName, out);
         out.println("         </column>");
      }

   }

   private Object typedGetter (ResultSet rs, int column)
         throws SQLException, IOException
   {
      final Object result;
      final int type = rs.getMetaData().getColumnType(column);
      switch (type)
      {
         case Types.TIMESTAMP:
            result = convertTimestamp(rs.getTimestamp(column));
            break;
         case Types.DATE:
         case Types.TIME:
            // FIXME: Take care for milisecond & timezone!?
            result = rs.getDate(column);
            break;
         case Types.CLOB:
            result = readNclob(rs, column);
            break;
         case Types.BLOB:
            result = readBlob(rs, column);
            break;
         default:
            result = rs.getObject(column);
      }
      return result;
   }

   private Object readBlob (ResultSet rs, int column) 
         throws SQLException
   {
      final Blob blob = rs.getBlob(column);
      final byte [] data;
      if (blob != null)
      {
         final long length = blob.length();
         if (length > Integer.MAX_VALUE)
         {
            data = "Length of Blob exceeds maximum of MAX_INT".getBytes();
         }
         else
         {
            data = blob.getBytes(1, (int) length);
         }
      }
      else
      {
         data = null;
      }
      return data;
   }

   private String readNclob (ResultSet rs, int column)
         throws SQLException, IOException
   {
      final String result;
      final Reader reader = rs.getCharacterStream(column);
      try
      {
         result = IoUtil.readFully(reader);
      }
      finally
      {
         IoUtil.close(reader);
      }
      return result;
   }


   private void object2Xml (Object object, String name, String typeName,
         PrintWriter out)
         throws SQLException
   {
      if (object != null)
      {
         out.print("            <raw>");
         out.print(XmlUtil.escape(String.valueOf(object)));
         out.println("</raw>");
         final String display = objectFormater(object, name, typeName);
         if (display != null)
         {
            out.print("            <display>");
            out.print(XmlUtil.escape(display));
            out.println("</display>");
         }
      }
      else
      {
         out.println("            <raw/>");
      }
   }

   private String objectFormater (Object object, String name, String typeName)
         throws SQLException
   {
      String result = null;

      if (mTypeMapper.containsKey(name))
      {
         try
         {
            final TypeMapper mapper = (TypeMapper) mTypeMapper.get(name);
            result = mapper.toDisplay(object);
         }
         catch (IllegalArgumentException e)
         {
            result = "Failed to convert type '" + e.toString() + "'.";
            logger.log(Level.WARNING, result, e);
         }
         catch (IllegalAccessException e)
         {
            result = "Failed to convert type '" + e.toString() + "'.";
            logger.log(Level.WARNING, result, e);

         }
         catch (InvocationTargetException e)
         {
            result = "Failed to convert type '" + e.toString() + "'.";
            logger.log(Level.WARNING, result, e);
         }
         catch (RuntimeException e)
         {
            result = "Failed to convert type '" + e.toString() + "'.";
            logger.log(Level.WARNING, result, e);
         }
      }
      else if (object instanceof Date)
      {
         result = display((Date) object);
      }
      else if (object instanceof String)
      {
         result = display((String) object);
      }
      else
      {
         result = null;
      }
      return result;
   }


   private String display (Date d)
   {
      final String result;
      if (d == null)
      {
         result = null;
      }
      else
      {
         result = mDateFormater.format(d);
      }
      return result;
   }

   private String display (String str)
   {
      final String result;
      if (str == null)
      {
         result = null;
      }
      else if (str.indexOf('<') != -1)
      {
         final String detect = str.trim();
         if (detect.indexOf('<') == 0
               && detect.lastIndexOf('>') == (detect.length() - 1))
         {
            result = formatXml(str);
         }
         else
         {
            result = null;
         }
      }
      else
      {
         result = null;
      }

      return result;
   }

   private static Date convertTimestamp (Timestamp ts)
   {
      final Date d;
      if (ts != null)
      {
         // ts.getTime does not return millis....
         d = new Date(((ts.getTime() / MILLIS_PER_SECOND) * MILLIS_PER_SECOND)
               + (ts.getNanos() 
                   / org.jcoderz.commons.types.Date.NANOS_PER_MILLI));
      }
      else
      {
         d = null;
      }
      return d;
   }

   /**
    * Simple xml formatter,
    * This code might fail for several input. In this case the
    * original input is returned.
    * @param org the input to be formated.
    * @return the input in xml formated (human readable) form or the
    *   input string.
    */
   public String formatXml (String org)
   {
      String result = org;

      boolean nestedTag = false;
      try
      {
         final String in = org.trim();
         if (in.charAt(0) == '<') // && sb.charAt(1) == '?')
         {
            int indent = 0;
            mStringBuffer.setLength(0);
            for (int t = 0; t < in.length(); t++)
            {
               char c = in.charAt(t);

               switch (c)
               {
                  case '<':
                     t++;
                     c = in.charAt(t);
                     switch (c)
                     {
                        case '/':
                           if (!nestedTag)
                           {
                              indent -= INDENT;
                              mStringBuffer.append("</");
                           }
                           else
                           {
                              mStringBuffer.append('\n');
                              indent -= INDENT;
                              indent(indent, mStringBuffer);
                              mStringBuffer.append("</");
                           }
                           nestedTag = true;
                           break;
                        case '?':
                        case '!':
                           if (t != 1)
                           {
                              mStringBuffer.append('\n');
                           }
                           mStringBuffer.append('<');
                           mStringBuffer.append(c);
                           break;
                        default:
                           nestedTag = false;
                           mStringBuffer.append('\n');
                           indent(indent, mStringBuffer);
                           mStringBuffer.append('<');
                           mStringBuffer.append(c);
                           indent += INDENT;
                           break;
                     }
                     break;
                  case '/':
                     mStringBuffer.append(c);
                     if (in.charAt(t + 1) == '>')
                     {
                        indent -= INDENT;
                        nestedTag = true;
                     }
                     break;
                  case '\n':
                  case '\r':
                     break;
                  case '>':
                  default:
                     mStringBuffer.append(c);
                     break;
               }
            }
            result = mStringBuffer.toString();
         }
      }
      catch (Exception ex)
      {
         result = org;
         // no foramted output...
      }


      return result;
   }

   private static void indent (final int i, StringBuffer b)
   {
      for (int j = i; j > 0; j--)
      {
         b.append(' ');
      }
   }

   private static class TypeMapper
   {
      private final Class mTypeClass;
      private final Method mFromDb;
      private final Method mToString;
      private final String mTypeName;
      private final Class mInputType;

      public TypeMapper (String typeName, String typeClass, String fromDb,
            String toString)
            throws SecurityException, NoSuchMethodException,
               ClassNotFoundException
      {
         this(typeName, Class.forName(typeClass), fromDb, toString);
      }

      public TypeMapper (String typeName, Class typeClass, String fromDb,
            String toString)
            throws SecurityException, NoSuchMethodException
      {
         mTypeClass = typeClass;
         mToString = typeClass.getMethod(toString, null);
//         mFromDb = typeClass.getMethod(fromDb, new Class[] {Object.class});
         final Method [] methods = typeClass.getMethods();
         Method method = null;
         for (int i = 0; i < methods.length; i++)
         {
            if (methods[i].getReturnType() == mTypeClass
               &&  methods[i].getName().startsWith(fromDb)
               &&  ((methods[i].getModifiers() & Modifier.STATIC) != 0))
            {
               method = methods[i];
               break;
            }
         }

         mFromDb = method;
         mInputType = method.getParameterTypes()[0];
         mTypeName = typeName;
      }

      public String toDisplay (Object in)
            throws IllegalArgumentException, IllegalAccessException,
               InvocationTargetException
      {
         Object type = null;
         try
         {
            type = mFromDb.invoke(null, new Object[] {in});
         }
         catch (Exception ex)
         {
            if (in instanceof Number)
            {
               if (mInputType == Integer.TYPE)
               {
                  type = mFromDb.invoke(null,
                        new Object[]
                        {
                           new Integer(((Number) in).intValue())
                        });
               }
               else if (mInputType == Long.TYPE)
               {
                  type = mFromDb.invoke(null,
                        new Object[]
                        {
                           new Long(((Number) in).longValue())
                        });
               }
            }
            else
            {
               final IllegalArgumentException axe 
                     = new IllegalArgumentException(
                        "Could not map type for object '" + String.valueOf(in)
                        + "' of class " + in.getClass().getName()
                        + " into expected converter "
                        + "type " + mInputType.getName() + ".");
               axe.initCause(ex);
               throw axe;
            }
         }
         return (String) mToString.invoke(type, null);
      }
   }
}
