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
public class CreateTableStatement extends SqlStatement
{
   private String mTableName;
   private String mBeanName;
   private String mAdditionalJavadoc;
   private boolean mOptimisticVersionCount;
   private boolean mSkipAppserverSupport;
   
   private List mColumns = new ArrayList();

   private List mIndexes = new ArrayList();
   
   private List mFkConstraints = new ArrayList();
   
   public CreateTableStatement (String tableName)
   {
      mTableName = tableName;
   }
   
   /**
    * Returns all columns of this table.
    * @return a list of ColumnSpec objects describing the columns of this table
    */
   public final List getColumns ()
   {
      return mColumns;
   }

   /**
    * Returns the name of this table.
    * @return the name of this table
    */
   public final String getTableName ()
   {
      return mTableName;
   }

   /**
    * Adds a column definition to this table.
    * @param column a column definition
    */
   public final void addColumn (ColumnSpec column)
   {
      mColumns.add(column);
   }
   
   /**
    * Retrieves a column by its name.
    * @param colName the name of the column
    * @return the column specification, or null
    *         if no column by that name is found
    */
   public final ColumnSpec getColumnByName (String colName)
   {
      ColumnSpec result = null;
      for (final Iterator it = mColumns.iterator(); it.hasNext(); )
      {
         final ColumnSpec col = (ColumnSpec) it.next();
         if (col.getColumnName().equalsIgnoreCase(colName))
         {
            result = col;
            break;
         }
      }
      return result;
   }

   /**
    * Returns a readable string representation.
    * @return a readable string representation 
    */
   public final String toString ()
   {
      final StringBuffer sbuf = new StringBuffer();
      sbuf.append("[CREATE TABLE Statement: name=").append(mTableName);
      sbuf.append(", bean name=").append(mBeanName);
      sbuf.append(", annotation=").append(getAnnotation());
      for (final Iterator it = mColumns.iterator(); it.hasNext();)
      {
         final ColumnSpec col = (ColumnSpec) it.next();
         sbuf.append(",\n   col=").append(col);
      }
      sbuf.append(']');
      return sbuf.toString();
   }
   /**
    * @return Returns the beanName.
    */
   public String getBeanName ()
   {
      return mBeanName;
   }
   /**
    * @param beanName The beanName to set.
    */
   public void setBeanName (String beanName)
   {
      mBeanName = beanName;
   }
   
   public List getIndexes ()
   {
      return mIndexes;
   }
   
   public void addIndex (CreateIndexStatement stmt)
   {
      mIndexes.add(stmt);
   }
   
   public void addFkConstraint (FkConstraint constraint)
   {
      mFkConstraints.add(constraint);
   }
   
   public List getFkConstraints ()
   {
      return mFkConstraints;
   }
   
   /**
    * @return Returns the additionalJavadoc.
    */
   public String getAdditionalJavadoc ()
   {
      return mAdditionalJavadoc;
   }

   /**
    * @param additionalJavadoc The additionalJavadoc to set.
    */
   public void setAdditionalJavadoc (String additionalJavadoc)
   {
      mAdditionalJavadoc = additionalJavadoc;
   }

   public void setOptimisticVersionCount (boolean b)
   {
      mOptimisticVersionCount = b;
   }
   
   public boolean isOptimisticVersionCount ()
   {
      return mOptimisticVersionCount;
   }

   /**
    * @param b
    */
   public void setSkipAppserverSupport (boolean b)
   {
      mSkipAppserverSupport = b;
   }
   
   /**
    * @return Returns the skipAppserverSupport.
    */
   public boolean isSkipAppserverSupport ()
   {
      return mSkipAppserverSupport;
   }
}
