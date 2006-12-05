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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jcoderz.commons.util.Constants;
import org.jcoderz.phoenix.sqlparser.ColumnSpec;
import org.jcoderz.phoenix.sqlparser.NumericAttribute;


/**
 * @author Albrecht Messner
 */
public final class TypeMapping
{
   private static final String[] JAVA_PRIMITIVE_TYPES = {
      Byte.TYPE.getName(),
      Short.TYPE.getName(),
      Integer.TYPE.getName(),
      Long.TYPE.getName(),
      Float.TYPE.getName(),
      Double.TYPE.getName(),
      Character.TYPE.getName(),
      Boolean.TYPE.getName(),
      "byte[]"
   };
   
   private static final String[] PRIMITIVE_TYPE_WRAPPERS = {
      Byte.class.getName(),
      Short.class.getName(),
      Integer.class.getName(),
      Long.class.getName(),
      Float.class.getName(),
      Double.class.getName(),
      Character.class.getName(),
      Boolean.class.getName(),
      "byte[]"
   };
   
   // these get mapped to java.lang.String
   private static final String[] STRING_TYPES = {
      "CHAR",
      "CHAR2",
      "NCHAR",
      "NCHAR2",
      "VARCHAR",
      "VARCHAR2",
      "NVARCHAR",
      "NVARCHAR2"
      };

   private static final String[] TIMESTAMP_TYPES = {
      "TIMESTAMP"
      };
   
   private static final String[] DATE_TYPES = {
      "DATE"
      };
   
   // these get mapped to numeric types
   private static final String[] NUMERIC_TYPES = {
      "NUMBER",
      "NUMERIC",
      "DECIMAL",
      "INTEGER",
      "INT",
      "FLOAT",
      "REAL"
      };
      
   private static final String[] FLOAT_TYPES = {
      "FLOAT",
      "REAL"
   };
   
   private static final int INTEGER_PRECISION_LIMIT = 10;
   private static final int LONG_PRECISION_LIMIT = 19;


   // put all arrays into hash sets for faster lookup
   private static final Set STRING_TYPE_SET = new HashSet();
   private static final Set NUMERIC_TYPE_SET = new HashSet();
   private static final Set TIMESTAMP_TYPE_SET = new HashSet();
   private static final Set DATE_TYPE_SET = new HashSet();
   private static final Set FLOAT_TYPE_SET = new HashSet();
   private static final Set JAVA_PRIMITIVE_TYPE_SET = new HashSet();

   static
   {
      STRING_TYPE_SET.addAll(Arrays.asList(STRING_TYPES));
      NUMERIC_TYPE_SET.addAll(Arrays.asList(NUMERIC_TYPES));
      TIMESTAMP_TYPE_SET.addAll(Arrays.asList(TIMESTAMP_TYPES));
      DATE_TYPE_SET.addAll(Arrays.asList(DATE_TYPES));
      FLOAT_TYPE_SET.addAll(Arrays.asList(FLOAT_TYPES));
      JAVA_PRIMITIVE_TYPE_SET.addAll(Arrays.asList(JAVA_PRIMITIVE_TYPES));
   }
   
   /**
    * private constructor to avoid instantiation
    */
   private TypeMapping ()
   {
   }
   
   /**
    * Finds the appropriate type mapping from a given column spec.
    * 
    * Note that the type returned here is the "simple" type as it is stored
    * in the database, not the "complex" type.
    * 
    * @param column the column specification
    * @param fullyQualified whether the type should contain the package
    *        name or not
    * @return the simple java type used to store the column in the db
    * @throws CmpGeneratorException if no type mapping can be found
    */
   public static final String getJavaType (
         ColumnSpec column,
         boolean fullyQualified)
         throws CmpGeneratorException
   {
      String javaType = column.getJavaType();
      if (javaType == null)
      {
         // this gets a mapping for character data types
         javaType = getTypeMapping(column.getColumnType());
         
         // no character type, try to find a numeric type
         if (javaType == null)
         {
            final List sqlTypeAttributes = column.getDatatypeAttributes();
            int att1 = 0, att2 = 0;
            if (sqlTypeAttributes.size() > 0)
            {
               att1 = ((NumericAttribute) sqlTypeAttributes.get(0)).getNumber();
               if (sqlTypeAttributes.size() > 1)
               {
                  att2 = ((NumericAttribute) sqlTypeAttributes.get(1))
                      .getNumber();
               }
            }
            javaType =
               getNumberTypeMapping(
                  column.getColumnType(),
                  att1,
                  att2);
         }
      }
      else
      {
         // a java type has been specified
         final String loadMethod = column.getLoadMethod();
         if (loadMethod != null)
         {
            // hey! it's a complex type
            // the "simple" java type can be found in the signature of the
            // load method
            final int openParen = loadMethod.indexOf("(");
            final int closeParen = loadMethod.indexOf(")");
            if (openParen == -1
               || closeParen == -1
               || closeParen <= openParen)
            {
               throw new CmpGeneratorException(
                  loadMethod + " is an invalid load method signature");
            }
            javaType = loadMethod.substring(openParen + 1, closeParen).trim();
            if (javaType.length() <= 0)
            {
               throw new CmpGeneratorException(
                  loadMethod + " is an invalid load method signature");
            }
         }
      }
      
      if (javaType == null)
      {
         throw new CmpGeneratorException("No type mapping found for column "
            + column);
      }

      // ok, now we've got a java type. if it is a primitive type
      // and the column is nullable, then we must use the wrapper object
      // instead
      if (isPrimitiveType(javaType) && !column.isNotNull())
      {
         javaType = primitiveToObject(javaType);
      }

      // check if we should return the type fully qualified
      if (!fullyQualified)
      {
         javaType = unqualifyType(javaType);
      }

      return javaType;
   }

   /**
    * Returns everything after the last dot in a type name.
    * @param typeName a java type name
    * @return the unqualified type name, or the argument if
    *         it was not a qualified java type name
    */
   public static String unqualifyType (String typeName)
   {
      final int dotIndex = typeName.lastIndexOf(".");
      return typeName.substring(dotIndex + 1);
   }

   /**
    * Returns the fully qualified java type to which an SQL type is mapped.
    * 
    * @param sqlType the name of the sql type
    * @return the FQ type name of the java type, or null if this method can not
    *         find a type mapping
    */
   public static final String getTypeMapping (String sqlType)
   {
      String javaType;
      
      final String s = sqlType.toUpperCase();
      if (STRING_TYPE_SET.contains(s))
      {
         javaType = String.class.getName();
      }
      else if (TIMESTAMP_TYPE_SET.contains(s))
      {
         javaType = Timestamp.class.getName();
      }
      else if (DATE_TYPE_SET.contains(s))
      {
         javaType = Date.class.getName();
      }
      else if (NUMERIC_TYPE_SET.contains(s))
      {
         javaType = null;
      }
      else
      {
         javaType = Object.class.getName();
      }
      
      return javaType;
   }
   
   /**
    * Returns a type mapping for numeric types.
    * 
    * @param sqlType the name of the sql type
    * @param precision the precision of the sql type
    * @param scale the scale of the sql type, or 0 if no scale given
    * @return the name of the appropriate java type
    */
   public static final String getNumberTypeMapping (
      String sqlType,
      int precision,
      int scale)
   {
      String javaType;
      final String s = sqlType.toUpperCase(Constants.SYSTEM_LOCALE);

      if (precision < 0 || scale < 0)
      {
         throw new IllegalArgumentException(
                 "Scale and precision must be non-negative");
      }

      if (NUMERIC_TYPE_SET.contains(s))
      {
         if (scale > 0 || FLOAT_TYPE_SET.contains(s))
         {
            return BigDecimal.class.getName();
         }
         else
         {
            if (precision < INTEGER_PRECISION_LIMIT)
            {
               javaType = Integer.TYPE.getName();
            }
            else if (precision < LONG_PRECISION_LIMIT)
            {
               javaType = Long.TYPE.getName();
            }
            else
            {
               javaType = BigDecimal.class.getName();
            }
         }
      }
      else
      {
         throw new IllegalArgumentException(s + " is not a numeric SQL type");
      }
      
      return javaType;
   }
   
   /**
    * Determines whether a given type is primitive.
    * 
    * @param type the name of a java type
    * @return true if the type is primitive, false otherwise
    */
   public static final boolean isPrimitiveType (String type)
   {
      boolean result = false;
      if (JAVA_PRIMITIVE_TYPE_SET.contains(type))
      {
         result = true;
      }
      return result;
   }

   /**
    * Maps a java primitive type to its corresponding wrapper object.
    * @param primitiveType the name of a primitive type
    * @return the corresponding wrapper object
    * @throws IllegalArgumentException if primitiveType is not a primitive type
    */
   public static final String primitiveToObject (String primitiveType)
   {
      if (!isPrimitiveType(primitiveType))
      {
         throw new IllegalArgumentException(
               "Can't map "
               + primitiveType
               + " to its Object wrapper because it is not a primitive type");
      }
      String objectWrapperName = null;
      for (int i = 0; i < JAVA_PRIMITIVE_TYPES.length; i++)
      {
         if (JAVA_PRIMITIVE_TYPES[i].equals(primitiveType))
         {
            objectWrapperName = PRIMITIVE_TYPE_WRAPPERS[i];
            break;
         }
      }
      return objectWrapperName;
   }

}
