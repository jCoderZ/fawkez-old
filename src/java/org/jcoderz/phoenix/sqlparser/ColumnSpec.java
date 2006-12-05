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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Albrecht Messner
 */
public class ColumnSpec
{
   private String mColumnName;
   private String mColumnType;
   
   private String mJavaType;
   private String mStoreMethod;
   private String mLoadMethod;
   private String mCurrencyColumn;
   private String mPeriodFieldName;
   private String mPeriodEndDateColumn;
   private String mWeblogicColumnType;
   private boolean mPeriodDefined = false;
   private boolean mSkipInInterface = false;
   
   private List mDatatypeAttributes = new ArrayList();
   private List mAttributes = new ArrayList();
   
   private boolean mIsNotNull = false;
   private boolean mIsPrimaryKey = false;
   private boolean mIsUnique = false;
   
   private String mAnnotation;

   /**
    * Returns the name of this column.
    * @return the name of this column
    */
   public final String getColumnName ()
   {
      return mColumnName;
   }

   /**
    * Sets the name of this column.
    * @param string the name of this column
    */
   public final void setColumnName (String string)
   {
      mColumnName = string;
   }

   /**
    * Returns the data type for this column.
    * @return the data type for this column
    */
   public final String getColumnType ()
   {
      return mColumnType;
   }


   /**
    * Sets the data type for this column.
    * @param string the data type
    */
   public final void setColumnType (String string)
   {
      mColumnType = string;
   }

   
   /**
    * Returns a list of all attributes of this colspec.
    * The elements of this list are of type ColumnAttribute.
    * @return a list of all attributes of this colspec
    */
   public final List getAttributes ()
   {
      return mAttributes;
   }

   /**
    * Adds an attribute to this column spec.
    * @param attr the attribute to add
    */
   public final void addAttribute (ColumnAttribute attr)
   {
      mAttributes.add(attr);
   }

   /**
    * Adds an attribute to the datatype of this colspec.
    * @param attr the datatype attribute that should be added
    */
   public final void addDatatypeAttribute (ColumnAttribute attr)
   {
      mDatatypeAttributes.add(attr);
   }

   /**
    * Returns the datatype attributes of this colspec.
    * @return a list of datatype attributes
    */
   public final List getDatatypeAttributes ()
   {
      return mDatatypeAttributes;
   }

   /**
    * Returns the java type to which this column maps.
    * @return the java type to which this column maps
    */
   public final String getJavaType ()
   {
      return mJavaType;
   }

   /**
    * Sets the java type for this column.
    * @param type the java type for this column
    */
   public final void setJavaType (String type)
   {
      mJavaType = type;
   }

   /**
    * Returns the method used when loading the custom type from the db.
    * @return the method used when loading the custom type from the db
    */
   public final String getLoadMethod ()
   {
      return mLoadMethod;
   }

   /**
    * Sets the method used when loading the custom type from the db.
    * @param methodName the method used when loading the custom type from the db
    */
   public final void setLoadMethod (String methodName)
   {
      mLoadMethod = methodName;
   }

   /**
    * Returns the method used when storing the type to the db.
    * @return the method used when storing the type to the db
    */
   public final String getStoreMethod ()
   {
      return mStoreMethod;
   }

   /**
    * Sets the method used when storing the type to the db.
    * @param methodName the method used when storing the type to the db
    */
   public final void setStoreMethod (String methodName)
   {
      mStoreMethod = methodName;
   }

   /**
    * Returns whether this column is not null.
    * @return true if this column is not null, false otherwise
    */
   public final boolean isNotNull ()
   {
      return mIsNotNull;
   }
   
   /**
    * Set whether this column is not null.
    * @param b flag indicating whether this column is not null
    */
   public final void setNotNull (boolean b)
   {
      mIsNotNull = b;
   }

   /**
    * Returns whether this column is primary key.
    * @return true if this column is primary key, false otherwise
    */
   public final boolean isPrimaryKey ()
   {
      return mIsPrimaryKey;
   }

   /**
    * Set whether this column is primary key or not.
    * @param b flag indicating whether this column is primary key
    */
   public final void setPrimaryKey (boolean b)
   {
      mIsPrimaryKey = b;
   }

   /**
    * @return Returns the isUnique.
    */
   public boolean isUnique ()
   {
      return mIsUnique;
   }
   /**
    * @param isUnique The isUnique to set.
    */
   public void setUnique (boolean isUnique)
   {
      mIsUnique = isUnique;
   }   
   
   /** {@inheritDoc} */
   public final String toString ()
   {
      final StringBuffer sbuf = new StringBuffer();
      sbuf.append("[ColumnSpec name=").append(mColumnName);
      sbuf.append(", sqlType=").append(mColumnType);
      sbuf.append(", javaType=").append(mJavaType);
      sbuf.append(", storeMethod=").append(mStoreMethod);
      sbuf.append(", loadMethod=").append(mLoadMethod);
      if (mIsNotNull)
      {
         sbuf.append(", not null");
      }
      if (mIsPrimaryKey)
      {
         sbuf.append(", primary key");
      }
      if (mIsUnique)
      {
         sbuf.append(", unique");
      }
      for (final Iterator it = mDatatypeAttributes.iterator(); it.hasNext(); )
      {
         final ColumnAttribute attr = (ColumnAttribute) it.next();
         sbuf.append(",\n      Data Type Attribute: ").append(attr);
      }
      for (final Iterator it = mAttributes.iterator(); it.hasNext(); )
      {
         final ColumnAttribute attr = (ColumnAttribute) it.next();
         sbuf.append(",\n      Column Attribute: ").append(attr);
      }
      return sbuf.toString();
   }

   public void setAnnotation (String annotation)
   {
      mAnnotation = annotation;
   }
   
   public String getAnnotation ()
   {
      return mAnnotation;
   }

   /**
    * @return Returns the currencyColumn.
    */
   public String getCurrencyColumn ()
   {
      return mCurrencyColumn;
   }

   /**
    * @param currencyColumn The currencyColumn to set.
    */
   public void setCurrencyColumn (String currencyColumn)
   {
      mCurrencyColumn = currencyColumn;
   }
   
   public boolean isPeriodDefined ()
   {
      return mPeriodDefined;
   }

   public void setIsPeriodDefined (boolean periodDefined)
   {
      mPeriodDefined = periodDefined;
   }

   public String getPeriodFieldName ()
   {
      return mPeriodFieldName;
   }

   public void setPeriodFieldName (String periodFieldName)
   {
      mPeriodFieldName = periodFieldName;
   }
   
   public String getPeriodEndDateColumn ()
   {
      return mPeriodEndDateColumn;
   }

   public void setPeriodEndDateColumn (String endDateColumn)
   {
      mPeriodEndDateColumn = endDateColumn;
   }
   
   public boolean isSkipInInterface ()
   {
      return mSkipInInterface;
   }
   
   public void setSkipInInterface (boolean b)
   {
      mSkipInInterface = b;
   }

   public String getWeblogicColumnType ()
   {
      return mWeblogicColumnType;
   }
   
   public void setWeblogicColumnType (String weblogicColumnType)
   {
      mWeblogicColumnType = weblogicColumnType;
   }

   public boolean isWeblogicColumnTypeDefined ()
   {
      return mWeblogicColumnType != null;
   }
}
