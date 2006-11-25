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
public class CreateIndexStatement
      extends SqlStatement
{
   private String mIndexName;
   private String mTableName;
   
   private boolean mIsUnique;
   
   private List mColumnNames;
   
   public CreateIndexStatement (String indexName)
   {
      mIndexName = indexName;
      mColumnNames = new ArrayList();
   }
   
   public void addColumn (String columnName)
   {
      mColumnNames.add(columnName);
   }
   
   /**
    * @return Returns the tableName.
    */
   public String getTableName ()
   {
      return mTableName;
   }
   /**
    * @param tableName The tableName to set.
    */
   public void setTableName (String tableName)
   {
      mTableName = tableName;
   }
   /**
    * @return Returns the columnNames.
    */
   public List getColumnNames ()
   {
      return mColumnNames;
   }
   /**
    * @return Returns the indexName.
    */
   public String getIndexName ()
   {
      return mIndexName;
   }
   
   /**
    * @see java.lang.Object#toString()
    */
   public String toString ()
   {
      StringBuffer sbuf = new StringBuffer();
      sbuf.append("[CREATE INDEX Statement: Index name = ").append(mIndexName);
      sbuf.append(", table = ").append(mTableName);
      for (Iterator it = mColumnNames.iterator(); it.hasNext(); )
      {
         String colName = (String) it.next();
         sbuf.append(", column = " + colName);
      }
      sbuf.append("]");
      return sbuf.toString();
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
}
