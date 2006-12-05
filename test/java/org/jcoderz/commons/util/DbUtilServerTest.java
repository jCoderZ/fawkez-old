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
package org.jcoderz.commons.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.jcoderz.commons.ServerTestCase;


/**
 * Tests the LimitedBatchSizePreparedStatement in the DbUtil class.
 * @author Albrecht Messner
 */
public class DbUtilServerTest
      extends ServerTestCase
{
   private static final int TEST_BATCH_SIZE = 10;

   /** {@inheritDoc} */
   protected void setUp ()
         throws Exception
   {
      Connection con = null;
      Statement stmt = null;
      try
      {
         con = getConnection();
         stmt = con.createStatement();
         try
         {
            stmt.executeUpdate("DROP TABLE tst_dbutil");
         }
         catch (SQLException x)
         {
            // ignore, table might not exist
         }
         stmt.executeUpdate(
               "CREATE TABLE tst_dbutil "
               + "(id number(10) primary key, name varchar2(100))");
      }
      finally
      {
         DbUtil.close(stmt);
         DbUtil.close(con);
      }
   }

   /**
    * Test what happens when we call executeBatch without having a SQL
    * statement in the prepared statement.
    * @throws Exception if the testcase fails
    */
   public void testDuplicateBatchUpdate ()
         throws Exception
   {
      doTestDuplicateBatchUpdate(false);
   }

   public void testDuplicateBatchUpdateLbsStatement ()
         throws Exception
   {
      doTestDuplicateBatchUpdate(true);
   }

   /**
    * Add a batch to a prepared statement and then call <code>execute</code>
    * instead of <code>executeBatch</code>.
    * @throws Exception if the testcase fails
    */
   public void testBatchWithExecute ()
         throws Exception
   {
      doTestBatchWithExecute(false);
   }

   /**
    * Add a batch to a prepared statement and then call <code>execute</code>
    * instead of <code>executeBatch</code>.
    * @throws Exception if the testcase fails
    */
   public void testBatchWithExecuteLbsStatement ()
         throws Exception
   {
      doTestBatchWithExecute(true);
   }

   /**
    * Run addBatch, clearBatch and then executeBatch, check that no rows
    * are created.
    * @throws Exception if the testcase fails
    */
   public void testClearBatch ()
         throws Exception
   {
      doTestClearStatement(false);
   }

   /**
    * Run addBatch, clearBatch and then executeBatch, check that no rows
    * are created.
    * @throws Exception if the testcase fails
    */
   public void testClearBatchLbsStatement ()
         throws Exception
   {
      doTestClearStatement(true);
   }

   private void doTestDuplicateBatchUpdate (boolean wrapPstmt)
         throws Exception
   {
      Connection con = null;
      PreparedStatement pstmt = null;
      try
      {
         con = getConnection();
         pstmt = con.prepareStatement(InsertRow.QUERY);
         if (wrapPstmt)
         {
            pstmt = DbUtil.getLimitedBatchSizePreparedStatement(
                  pstmt, TEST_BATCH_SIZE);
         }
         pstmt.setInt(InsertRow.PARAM_ID, 1);
         pstmt.setString(InsertRow.PARAM_NAME, "hans wurscht");
         pstmt.addBatch();

         final int[] countArray = pstmt.executeBatch();
         assertEquals("Should have one statement", 1, countArray.length);
         assertTrue("Should have one row updated",
               1 == countArray[0] // one row
               || Statement.SUCCESS_NO_INFO == countArray[0]
            );

         final int[] secondUpdate = pstmt.executeBatch();
         assertEquals("Should have empty count array", 0, secondUpdate.length);
      }
      finally
      {
         DbUtil.close(pstmt);
         DbUtil.close(con);
      }
   }

   private void doTestBatchWithExecute (boolean wrapPstmt)
         throws Exception
   {
      Connection con = null;
      PreparedStatement pstmt = null;
      try
      {
         con = getConnection();
         pstmt = con.prepareStatement(InsertRow.QUERY);
         if (wrapPstmt)
         {
            pstmt = DbUtil.getLimitedBatchSizePreparedStatement(
                  pstmt, TEST_BATCH_SIZE);
         }
         pstmt.setInt(InsertRow.PARAM_ID, 1);
         pstmt.setString(InsertRow.PARAM_NAME, "hans wurscht");
         pstmt.addBatch();

         pstmt.execute();
         fail("Execute should throw exception when statement has batch");
      }
      catch (SQLException x)
      {
         // expected
      }
      finally
      {
         DbUtil.close(pstmt);
         DbUtil.close(con);
      }

      assertEquals("Expected no rows", 0, countTable());
   }

   private void doTestClearStatement (boolean wrapPstmt)
         throws Exception
   {
      Connection con = null;
      PreparedStatement pstmt = null;
      try
      {
         con = getConnection();
         pstmt = con.prepareStatement(InsertRow.QUERY);
         if (wrapPstmt)
         {
            pstmt = DbUtil.getLimitedBatchSizePreparedStatement(
                  pstmt, TEST_BATCH_SIZE);
         }
         pstmt.setInt(InsertRow.PARAM_ID, 1);
         pstmt.setString(InsertRow.PARAM_NAME, "hans wurscht");
         pstmt.addBatch();

         pstmt.clearBatch();

         pstmt.executeBatch();
      }
      finally
      {
         DbUtil.close(pstmt);
         DbUtil.close(con);
      }

      assertEquals("Expected no rows", 0, countTable());

   }

   private int countTable ()
         throws NamingException, SQLException
   {
      Connection con = null;
      Statement stmt = null;
      ResultSet rset = null;
      final int count;
      try
      {
         con = getConnection();
         stmt = con.createStatement();
         rset = stmt.executeQuery(CountRows.QUERY);
         assertTrue("Empty result set", rset.next());
         count = rset.getInt(CountRows.RESULT_COUNT);
      }
      finally
      {
         DbUtil.close(stmt);
         DbUtil.close(con);
      }
      return count;
   }

   private Connection getConnection ()
         throws NamingException, SQLException
   {
      final Context ctx = new InitialContext();
      final DataSource ds = (DataSource) ctx.lookup("FIXME");
      return ds.getConnection();
   }

   private static final class InsertRow
   {
      static final String QUERY
            = "INSERT INTO tst_dbutil (id, name) VALUES (?, ?)";
      static final int PARAM_ID = 1;
      static final int PARAM_NAME = 2;
   }

   private static final class CountRows
   {
      static final String QUERY = "SELECT count(*) FROM tst_dbutil";
      static final int RESULT_COUNT = 1;
   }
}
