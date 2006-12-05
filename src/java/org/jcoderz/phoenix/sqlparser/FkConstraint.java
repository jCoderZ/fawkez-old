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
import java.util.List;

/**
 * TODO Write javadoc!
 * 
 * @author Albrecht Messner
 */
public class FkConstraint
{
   private final String mName;
   private final List mColumns;
   private final String mRefTable;
   private final List mRefColumns;

   public FkConstraint (
         String name, List columns, String refTable, List refColumns)
   {
      mName = name;
      mColumns = columns;
      mRefTable = refTable;
      mRefColumns = refColumns;
   }
   
   public FkConstraint (
         String name, String column, String refTable, String refColumn)
   {
      mName = name;
      List colList = new ArrayList();
      colList.add(column);
      mColumns = colList;
      mRefTable = refTable;
      List refList = new ArrayList();
      refList.add(refColumn);
      mRefColumns = refList;
   }
   
   /**
    * @return Returns the name.
    */
   public String getName ()
   {
      return mName;
   }
   
   /**
    * @return Returns the columns.
    */
   public List getColumns ()
   {
      return mColumns;
   }
   
   /**
    * @return Returns the refTable.
    */
   public String getRefTable ()
   {
      return mRefTable;
   }
   
   /**
    * @return Returns the refColumns.
    */
   public List getRefColumns ()
   {
      return mRefColumns;
   }
   
   /** {@inheritDoc} */
   public String toString ()
   {
      return "[FkConstraint columns: " + getColumns() + " references "
            + getRefTable() + "(" + getRefColumns() + ")]"; 
   }
}
