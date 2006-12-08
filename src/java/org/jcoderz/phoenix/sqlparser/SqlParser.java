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

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Albrecht Messner
 */
public class SqlParser
{
   private static final String CLASSNAME = SqlParser.class.getName();
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private static final SpecialColumnComment TIMESTAMP_SPECIAL_COMMENT
         = new SpecialColumnComment();

   private static final SpecialColumnComment DATE_SPECIAL_COMMENT
         = new SpecialColumnComment();

   static
   {
      Token t;
      try
      {
         t = new Token(TokenType.COMMENT,
               "-- @cmpgen.java-type=org.jcoderz.commons.types.Date");
         TIMESTAMP_SPECIAL_COMMENT.parseComment(t);
         DATE_SPECIAL_COMMENT.parseComment(t);

         t = new Token(TokenType.COMMENT,
               "-- @cmpgen.store-method=toSqlTimestamp()");
         TIMESTAMP_SPECIAL_COMMENT.parseComment(t);
         t = new Token(TokenType.COMMENT,
               "-- @cmpgen.store-method=toSqlDate()");
         DATE_SPECIAL_COMMENT.parseComment(t);

         t = new Token(TokenType.COMMENT,
               "-- @cmpgen.load-method=fromSqlTimestamp(java.sql.Timestamp)");
         TIMESTAMP_SPECIAL_COMMENT.parseComment(t);
         t = new Token(TokenType.COMMENT,
               "-- @cmpgen.load-method=fromSqlDate(java.sql.Date)");
         DATE_SPECIAL_COMMENT.parseComment(t);
      }
      catch (ParseException e)
      {
         throw new RuntimeException("This must not happen!", e);
      }
   }

   private final ScannerInterface mScanner;

   private final Stack mTokenStack = new Stack();

   private SpecialColumnComment mSpecialColumnComment;
   private SpecialAnnotationComment mColumnAnnotation;

   private List mSpecialStatementComments = new ArrayList();
   private SpecialAnnotationComment mStatementAnnotation;

   /**
    * Create a new SQL Parser.
    *
    * @param scanner the SQL Scanner that delivers tokens to this parser
    */
   public SqlParser (ScannerInterface scanner)
   {
      mScanner = scanner;
      mScanner.setReportWhitespace(false);
   }

   /**
    * Wrapper method for ScannerInterface.nextToken().
    *
    * @return returns the next token from the stack of the scanner,
    *       or null if the stack is empty,
    *
    * @throws ParseException
    */
   private Token getNextToken () throws ParseException
   {
      Token result;
      if (mTokenStack.isEmpty())
      {
         result = mScanner.nextToken();
      }
      else
      {
         result = (Token) mTokenStack.pop();
      }

      if (logger.isLoggable(Level.FINER))
      {
         final Exception x = new Exception();
         int index = 1;
         final StackTraceElement caller = x.getStackTrace()[index];
         StackTraceElement callerParent = null;
         try
         {
            callerParent = x.getStackTrace()[++index];
         }
         catch (ArrayIndexOutOfBoundsException x2)
         {
            // ignore, the stack is simply not long enough
         }

         logger.finest("TOKEN: " + result + " to " + caller
               + " called by " + callerParent);
      }

      return result;
   }

   /**
    * gives parser a chance to return a token if it has read too much
    *
    * @param token
    */
   private void pushToken (Token token)
   {
      mTokenStack.push(token);
   }

   /**
    * Read tokens from the scanner until EOF and parse them.
    *
    * @return a list of parsed SQL statements
    * @throws ParseException if an error occurs
    */
   public final List parse ()
      throws ParseException
   {
      final List tableObjects = new ArrayList();
      final List indexObjects = new ArrayList();
      final List sequenceObjects = new ArrayList();

      Token token;
      while ((token = getNextToken()).getType() != TokenType.EOF)
      {
         final SqlStatement stmt = handleStartOfStatement(token);
         if (stmt != null)
         {
            if (stmt instanceof CreateTableStatement)
            {
               tableObjects.add(stmt);
            }
            else if (stmt instanceof CreateIndexStatement)
            {
               indexObjects.add(stmt);
            }
            else if (stmt instanceof CreateSequenceStatement)
            {
               sequenceObjects.add(stmt);
            }
            logger.info("Parsed statement: " + stmt);
         }
      }

      for (final Iterator it = indexObjects.iterator(); it.hasNext(); )
      {
         final CreateIndexStatement stmt = (CreateIndexStatement) it.next();
         final String tname = stmt.getTableName();
         boolean found = false;
         for (final Iterator it2 = tableObjects.iterator(); it2.hasNext(); )
         {
            final CreateTableStatement stmt2 
                = (CreateTableStatement) it2.next();
            if (stmt2.getTableName().equalsIgnoreCase(tname))
            {
               stmt2.addIndex(stmt);
               found = true;
               break;
            }
         }

         if (! found)
         {
            throw new ParseException(
                  "Index on table " + tname
                  + " declared, but create statement for table " + tname
                  + " not found", -1, -1);
         }
      }

      final List result = new ArrayList();
      result.addAll(tableObjects);
      result.addAll(sequenceObjects);
      return result;
   }

   private SqlStatement handleStartOfStatement (Token token)
      throws ParseException
   {
      SqlStatement result = null;
      if (token.getType() == TokenType.CREATE)
      {
         logger.finer("Start parsing CREATE statement");
         result = handleCreateStatement();
      }
      else if (token.getType() == TokenType.ALTER
            || token.getType() == TokenType.DROP
            || token.getType() == TokenType.INSERT
            || token.getType() == TokenType.DELETE
            || token.getType() == TokenType.SELECT)
      {
         logger.info("Skipping " + token.getValue() + " statement");
         eatUntilSemicolon();
      }
      else if (token.getType() == TokenType.COMMENT)
      {
         pushToken(token);
         parseStatementComment();
      }
      else
      {
         logger.finer("Unable to handle token " + token);
         throw new ParseException(
            "Unexpected token",
            mScanner.getLine(),
            mScanner.getColumn());
      }
      return result;
   }

   private SqlStatement handleCreateStatement ()
      throws ParseException
   {
      final String methodName = "handleCreateStatement()";
      logger.entering(CLASSNAME, methodName);


      Token token = getNextToken();
      SqlStatement result = null;

      final TokenType type = token.getType();

      // we can only parse CREATE TABLE and CREATE INDEX statements
      if (type == TokenType.TABLE)
      {
         token = assertNextToken(TokenType.IDENTIFIER);
         result = new CreateTableStatement(token.getValue());

         assertNextToken(TokenType.OPEN_PAREN);

         do
         {
            parseRelationalPropOrComment((CreateTableStatement) result);

            token = getNextToken();
            if (token.getType() == TokenType.CLOSE_PAREN)
            {
               // we've reached the end of the relational properties
               break;
            }
            else if (token.getType() == TokenType.COMMENT)
            {
               pushToken(token);
               continue;
            }
            else
            {
               pushToken(token);
               continue;
            }
         }
         while (true);

         eatUntilSemicolon();

         if (mSpecialStatementComments.size() > 0)
         {
            for (final Iterator it = mSpecialStatementComments.iterator();
                  it.hasNext(); )
            {
               final SpecialStatementComment comment
                     = (SpecialStatementComment) it.next();
               if (comment.getType() == SpecialStatementComment.TYPE_BEAN_NAME)
               {
                  ((CreateTableStatement) result).setBeanName(
                        comment.getContent());
               }
               else if (
                     comment.getType() == SpecialStatementComment.TYPE_JAVADOC)
               {
                  ((CreateTableStatement) result).setAdditionalJavadoc(
                        comment.getContent());
               }
               else if (comment.getType()
                     == SpecialStatementComment.TYPE_OPTIMISTIC_VERSION_COUNT)
               {
                  ((CreateTableStatement) result).
                        setOptimisticVersionCount(true);
               }
               else if (comment.getType()
                     == SpecialStatementComment.TYPE_SKIP_APPSERVER_SUPPORT)
               {
                  ((CreateTableStatement) result).
                        setSkipAppserverSupport(true);
               }

            }
            mSpecialStatementComments.clear();
         }
      }
      else if (type == TokenType.UNIQUE
            || type == TokenType.BITMAP
            || type == TokenType.INDEX)
      {
         boolean isUnique = false;
         if (type == TokenType.UNIQUE)
         {
            assertNextToken(TokenType.INDEX);
            isUnique = true;
         }
         else if (type == TokenType.BITMAP)
         {
            assertNextToken(TokenType.INDEX);
         }

         token = assertNextToken(TokenType.IDENTIFIER);
         final CreateIndexStatement stmt 
             = new CreateIndexStatement(token.getValue());
         stmt.setUnique(isUnique);
         parseCreateIndexStatement(stmt);
         result = stmt;
         eatUntilSemicolon();
      }
      else if (type == TokenType.SEQUENCE)
      {
         token = assertNextToken(TokenType.IDENTIFIER);
         final CreateSequenceStatement stmt
               = new CreateSequenceStatement(token.getValue());

         parseCreateSequenceStatement(stmt);

         result = stmt;
         eatUntilSemicolon();

         logger.info("Parsed sequence: " + stmt);
      }
      else
      {
         logger.info("Can't parse create statement for "
               + token.getValue() + ", skipping.");
         eatUntilSemicolon();
      }

      if (mStatementAnnotation != null)
      {
         result.setAnnotation(mStatementAnnotation.getAnnotation());
         mStatementAnnotation = null;
      }

      logger.exiting(CLASSNAME, methodName, result);
      return result;
   }

   private void parseCreateSequenceStatement (CreateSequenceStatement stmt)
         throws ParseException
   {
      Token token;
      while ((token = getNextToken()).getType() != TokenType.SEMICOLON)
      {
         final TokenType type = token.getType();
         if (type == TokenType.INCREMENT)
         {
            assertNextToken(TokenType.BY);
            token = assertNextToken(TokenType.NUMERIC_LITERAL);
            stmt.setIncrementBy(Long.parseLong(token.getValue()));
         }
         else if (type == TokenType.START)
         {
            assertNextToken(TokenType.WITH);
            token = assertNextToken(TokenType.NUMERIC_LITERAL);
            stmt.setStartWith(new Long(Long.parseLong(token.getValue())));
         }
         else if (type == TokenType.MAXVALUE)
         {
            token = assertNextToken(TokenType.NUMERIC_LITERAL);
            stmt.setMaxValue(new Long(Long.parseLong(token.getValue())));
         }
         else if (type == TokenType.NOMAXVALUE)
         {
            stmt.setNoMaxValue(true);
         }
         else if (type == TokenType.MINVALUE)
         {
            token = assertNextToken(TokenType.NUMERIC_LITERAL);
            stmt.setMinValue(new Long(Long.parseLong(token.getValue())));
         }
         else if (type == TokenType.NOMINVALUE)
         {
            stmt.setNoMinValue(true);
         }
         else if (type == TokenType.CYCLE)
         {
            stmt.setCycle(true);
         }
         else if (type == TokenType.NOCYCLE)
         {
            stmt.setCycle(false);
         }
         else if (type == TokenType.CACHE)
         {
            token = assertNextToken(TokenType.NUMERIC_LITERAL);
            stmt.setCache(Long.parseLong(token.getValue()));
         }
         else if (type == TokenType.NOCACHE)
         {
            stmt.setCache(0);
         }
         else if (type == TokenType.ORDER)
         {
            stmt.setOrder(true);
         }
         else if (type == TokenType.NOORDER)
         {
            stmt.setOrder(false);
         }
         else
         {
            unexpectedToken(token);
         }
      }

      // put the semicolon back on the stack
      pushToken(token);
   }

   private void parseCreateIndexStatement (CreateIndexStatement result)
         throws ParseException
   {
      // the CREATE (UNIQUE|BITMAP) INDEX <name> part has already been eaten.
      assertNextToken(TokenType.ON);
      Token token = assertNextToken(TokenType.IDENTIFIER);
      result.setTableName(token.getValue());

      assertNextToken(TokenType.OPEN_PAREN);

      while (true)
      {
         token = assertNextToken(TokenType.IDENTIFIER);
         result.addColumn(token.getValue());

         token = getNextToken();
         if (token.getType() == TokenType.CLOSE_PAREN)
         {
            break;
         }
         else if (! (token.getType() == TokenType.COMMA))
         {
            unexpectedToken(token);
         }
      }
   }

   /**
    * Parse a single column spec, or an out-of-line constraint.
    */
   private void parseRelationalPropOrComment (CreateTableStatement createStmt)
      throws ParseException
   {
      final Token token = getNextToken();
      if (isStartOfOutOfLineConstraint(token))
      {
         // delegate
         pushToken(token);
         parseOutOfLineConstraint(createStmt);
      }
      else if (token.getType() == TokenType.IDENTIFIER)
      {
         // delegate
         pushToken(token);
         parseColumnSpec(createStmt);
      }
      else if (token.getType() == TokenType.COMMENT)
      {
         // delegate
         pushToken(token);
         parseColumnComment();
      }
      else
      {
         unexpectedToken(token);
      }
   }

   private void parseColumnSpec (CreateTableStatement createStmt)
         throws ParseException
   {
      final ColumnSpec colSpec = new ColumnSpec();

      // column name
      Token token = assertNextToken(TokenType.IDENTIFIER);
      colSpec.setColumnName(token.getValue());

      // data type
      token = assertNextToken(TokenType.IDENTIFIER);
      colSpec.setColumnType(token.getValue());

      if (token.getValue().equalsIgnoreCase("TIMESTAMP")
            && mSpecialColumnComment == null)
      {
         mSpecialColumnComment = TIMESTAMP_SPECIAL_COMMENT;
      }
      else if (token.getValue().equalsIgnoreCase("DATE")
            && mSpecialColumnComment == null)
      {
         mSpecialColumnComment = DATE_SPECIAL_COMMENT;
      }

      token = getNextToken();
      if (token.getType() == TokenType.OPEN_PAREN)
      {
         token = assertNextToken(TokenType.NUMERIC_LITERAL);
         colSpec.addDatatypeAttribute(new NumericAttribute(token.getValue()));
         token = getNextToken();
         if (token.getType() == TokenType.COMMA)
         {
            token = assertNextToken(TokenType.NUMERIC_LITERAL);
            colSpec.addDatatypeAttribute(
                    new NumericAttribute(token.getValue()));
            assertNextToken(TokenType.CLOSE_PAREN);
         }
         else if (token.getType() != TokenType.CLOSE_PAREN)
         {
            throw new ParseException("Unexpected token: " + token,
               mScanner.getLine(), mScanner.getColumn());
         }
      }
      else
      {
         pushToken(token);
      }

      parseColumnAttributes(colSpec, createStmt);

      token = getNextToken();
      if (token.getType() != TokenType.COMMA)
      {
         pushToken(token);
      }

      // at this point the full colspec is parsed
      if (mSpecialColumnComment != null)
      {
         mSpecialColumnComment.validate();
         colSpec.setJavaType(mSpecialColumnComment.getJavaType());
         colSpec.setLoadMethod(mSpecialColumnComment.getLoadMethod());
         colSpec.setStoreMethod(mSpecialColumnComment.getStoreMethod());
         colSpec.setCurrencyColumn(mSpecialColumnComment.getCurrencyColumn());

         colSpec.setIsPeriodDefined(
               mSpecialColumnComment.getPeriodFieldName() != null
               && mSpecialColumnComment.getPeriodEndDateColumn() != null);
         colSpec.setPeriodFieldName(
               mSpecialColumnComment.getPeriodFieldName());
         colSpec.setPeriodEndDateColumn(
               mSpecialColumnComment.getPeriodEndDateColumn());
         colSpec.setWeblogicColumnType(
               mSpecialColumnComment.getWeblogicColumnType());

         colSpec.setSkipInInterface(mSpecialColumnComment.isSkipInInterface());
         mSpecialColumnComment = null;
      }
      if (mColumnAnnotation != null)
      {
         colSpec.setAnnotation(mColumnAnnotation.getAnnotation());
         mColumnAnnotation = null;
      }

      createStmt.addColumn(colSpec);
   }

   private void parseStatementComment ()
         throws ParseException
   {
      final Token token = assertNextToken(TokenType.COMMENT);
      final String val = token.getValue();

      if (val.indexOf("@cmpgen") != -1)
      {
         final SpecialStatementComment comment
               = SpecialStatementComment.parseComment(token);
         if (comment != null)
         {
            mSpecialStatementComments.add(comment);
         }
      }
      else if (val.indexOf("@@annotation") != -1)
      {
         if (mStatementAnnotation == null)
         {
            mStatementAnnotation = new SpecialAnnotationComment();
         }
         mStatementAnnotation.parseComment(token);
      }
   }


   private void parseColumnComment () throws ParseException
   {
      final Token token = assertNextToken(TokenType.COMMENT);
      final String str = token.getValue();
      if (str.indexOf("@cmpgen") != -1)
      {
         if (mSpecialColumnComment == null)
         {
            mSpecialColumnComment = new SpecialColumnComment();
         }
         mSpecialColumnComment.parseComment(token);
      }
      else if (str.indexOf("@@annotation") != -1)
      {
         if (mColumnAnnotation != null)
         {
            throw new ParseException("Only one annotation per column allowed",
                  mScanner.getLine(), mScanner.getColumn());
         }
         mColumnAnnotation = new SpecialAnnotationComment();
         mColumnAnnotation.parseComment(token);
      }
      else
      {
         logger.finer("Ignoring non-special comment " + token);
      }
   }

   /**
    * parse a set of column attributes.
    *
    * this is supposed to parse everything after the column name and data type,
    * namely:
    * <ul>
    *    <li>a default value for the column</li>
    *    <li>inline constraints</li>
    * </ul>
    */
   private void parseColumnAttributes (
         ColumnSpec colSpec, CreateTableStatement stmt)
         throws ParseException
   {
      Token token;

      token = getNextToken();
      TokenType type = token.getType();
      if (type == TokenType.COMMA
            || type == TokenType.CLOSE_PAREN)
      {
         pushToken(token);
      }
      else if (type == TokenType.DEFAULT)
      {
         // default clause: DEFAULT expr
         // read until end of EXPR, which is either a comma,
         // a closing parentheses or the start of an inline constraint
         final StringBuffer defltExpr = new StringBuffer();
         mScanner.setReportWhitespace(true);
         while (true)
         {
            token = getNextToken();
            type = token.getType();
            if (type == TokenType.OPEN_PAREN)
            {
               defltExpr.append(token.getValue());
               defltExpr.append(eatUntilClosingParen());
               defltExpr.append(
                     assertNextToken(TokenType.CLOSE_PAREN).getValue());
            }
            else if (type == TokenType.COMMA
                  || type == TokenType.CLOSE_PAREN
                  || isStartOfInlineConstraint(token))
            {
               pushToken(token);
               break;
            }
            else
            {
               defltExpr.append(token.getValue());
            }
         }
         mScanner.setReportWhitespace(false);
         final DefaultClause dfltClause 
             = new DefaultClause(defltExpr.toString());
         colSpec.addAttribute(dfltClause);
      }
      else if (isStartOfInlineConstraint(token))
      {
         // return the token we just read,
         // because it's the start of a constraint
         pushToken(token);
         parseInlineConstraint(colSpec, stmt);
      }
      else
      {
         unexpectedToken(token);
      }

      while (true)
      {
         token = getNextToken();
         logger.finer("Checking for more inline constraints: got " + token);
         pushToken(token);
         if (isStartOfInlineConstraint(token))
         {
            parseInlineConstraint(colSpec, stmt);
         }
         else if (token.getType() == TokenType.COMMA
               || token.getType() == TokenType.CLOSE_PAREN)
         {
            break;
         }
         else
         {
            unexpectedToken(token);
         }
      }
   }

   private void parseInlineConstraint (
         ColumnSpec column, CreateTableStatement stmt)
         throws ParseException
   {
      Token token;
      TokenType type;

      String constraintName = "unnamed";
      token = getNextToken();
      type = token.getType();

      if (type == TokenType.CONSTRAINT)
      {
         token = assertNextToken(TokenType.IDENTIFIER);
         constraintName = token.getValue();
      }
      else
      {
         pushToken(token);
      }

      token = getNextToken();
      type = token.getType();
      if (type == TokenType.NOT)
      {
         assertNextToken(TokenType.NULL);
         column.setNotNull(true);
         logger.finer("Parsed inline constraint " + constraintName + ": "
               + column.getColumnName() + " is not nullable");
      }
      else if (type == TokenType.NULL)
      {
         column.setNotNull(false);
         logger.finer("Parsed inline constraint " + constraintName + ": "
               + column.getColumnName() + " is nullable");
      }
      else if (type == TokenType.UNIQUE)
      {
         column.setUnique(true);
         logger.finer("Parsed inline constraint " + constraintName + ": "
               + column.getColumnName() + " is unique");
      }
      else if (type == TokenType.PRIMARY)
      {
         assertNextToken(TokenType.KEY);
         column.setPrimaryKey(true);
         logger.finer("Parsed inline constraint " + constraintName + ": "
               + column.getColumnName() + " is primary key");
      }
      else if (type == TokenType.CHECK)
      {
         final StringBuffer checkCondition = new StringBuffer();
         assertNextToken(TokenType.OPEN_PAREN);
         mScanner.setReportWhitespace(true);
         checkCondition.append(eatUntilClosingParen());
         mScanner.setReportWhitespace(false);
         assertNextToken(TokenType.CLOSE_PAREN);
         logger.finer("Parsed inline constraint " + constraintName + ": "
               + column.getColumnName() + " check condition: "
               + checkCondition);
      }
      else if (type == TokenType.REFERENCES)
      {
         final String refTable 
             = assertNextToken(TokenType.IDENTIFIER).getValue();
         String refColumn = null;

         token = getNextToken();
         if (token.getType() == TokenType.OPEN_PAREN)
         {
            refColumn = assertNextToken(TokenType.IDENTIFIER).getValue();
            assertNextToken(TokenType.CLOSE_PAREN);
         }
         else
         {
            refColumn = column.getColumnName();
            pushToken(token);
         }

         String deleteSemantic = "default";

         token = getNextToken();
         type = token.getType();
         if (type.equals(TokenType.ON))
         {
            assertNextToken(TokenType.DELETE);
            token = getNextToken();
            type = token.getType();
            if (type == TokenType.SET)
            {
               assertNextToken(TokenType.NULL);
               deleteSemantic  = "set null";
            }
            else
            {
               assertToken(token, TokenType.CASCADE);
               deleteSemantic = "cascade";
            }
         }
         else
         {
            // oops, no on delete clause
            pushToken(token);
         }

         final FkConstraint constraint = new FkConstraint(
               constraintName, column.getColumnName(), refTable, refColumn);
         stmt.addFkConstraint(constraint);
         logger.finer("Parsed inline constraint " + constraintName + ": "
               + column.getColumnName() + " references "
               + refTable
               + (refColumn != null ? "." + refColumn : "")
               + " with " + deleteSemantic + " delete semantic");
      }
   }

   private boolean isStartOfInlineConstraint (Token token)
   {
      final TokenType type = token.getType();
      return type == TokenType.NOT
            || type == TokenType.NULL
            || type == TokenType.UNIQUE
            || type == TokenType.PRIMARY
            || type == TokenType.CHECK
            || type == TokenType.REFERENCES
            || type == TokenType.CONSTRAINT;
   }

   private boolean isStartOfOutOfLineConstraint (Token token)
   {
      final TokenType type = token.getType();
      return type == TokenType.CONSTRAINT
            || type == TokenType.UNIQUE
            || type == TokenType.PRIMARY
            || type == TokenType.FOREIGN
            || type == TokenType.CHECK;
   }

   private StringBuffer eatUntilClosingParen ()
      throws ParseException
   {
      final StringBuffer sbuf = new StringBuffer();

      Token token;
      while ((token = getNextToken()).getType() != TokenType.CLOSE_PAREN)
      {
         sbuf.append(token.getValue());
         if (token.getType() == TokenType.OPEN_PAREN)
         {
            final StringBuffer nested = eatUntilClosingParen();
            sbuf.append(nested);
            token = assertNextToken(TokenType.CLOSE_PAREN);
            sbuf.append(token.getValue());
         }
         logger.finest("Skipping token " + token);
      }
      pushToken(token);

      return sbuf;
   }

   private Token eatUntilSemicolon ()
      throws ParseException
   {
      Token token;
      while (true)
      {
         token = getNextToken();
         if (token.getType() == TokenType.SEMICOLON)
         {
            break;
         }
         logger.fine("Skipping token " + token);
      }
      return token;
   }

   private void parseOutOfLineConstraint (CreateTableStatement statement)
      throws ParseException
   {
      String constraintName = "unnamed";

      Token token;
      TokenType type;

      // 1. parse "CONSTRAINT <name>"
      token = getNextToken();
      type = token.getType();
      if (type == TokenType.CONSTRAINT)
      {
         token = assertNextToken(TokenType.IDENTIFIER);
         constraintName = token.getValue();
      }
      else
      {
         pushToken(token);
      }

      // 2. parse the constraint itself
      token = getNextToken();
      type = token.getType();

      if (type == TokenType.UNIQUE)
      {
         // UNIQUE ( column [, column...] )
         assertNextToken(TokenType.OPEN_PAREN);
         final List uniqueCols = new ArrayList();
         while ((token = getNextToken()).getType() != TokenType.CLOSE_PAREN)
         {
            if (token.getType() != TokenType.COMMA)
            {
               uniqueCols.add(token.getValue());
            }
         }
         logger.finer("Parsed OOL constraint "
               + constraintName + ": UNIQUE " + uniqueCols);
         addUniqueColumns(statement, uniqueCols);
      }
      else if (type == TokenType.PRIMARY)
      {
         // PRIMARY KEY ( column [, column...] )
         assertNextToken(TokenType.KEY);
         assertNextToken(TokenType.OPEN_PAREN);
         final List pkCols = new ArrayList();
         while ((token = getNextToken()).getType() != TokenType.CLOSE_PAREN)
         {
            if (token.getType() != TokenType.COMMA)
            {
               pkCols.add(token.getValue());
            }
         }
         logger.finer("Parsed OOL constraint "
               + constraintName + ": PRIMARY KEY " + pkCols);
         addPkColumns(statement, pkCols);
      }
      else if (type == TokenType.FOREIGN)
      {
         parseOolForeignKey(constraintName, statement);
      }
      else if (type == TokenType.CHECK)
      {
         token = assertNextToken(TokenType.OPEN_PAREN);
         final StringBuffer checkCondition = new StringBuffer();
         checkCondition.append(token.getValue());
         mScanner.setReportWhitespace(true);
         checkCondition.append(eatUntilClosingParen());
         mScanner.setReportWhitespace(false);
         token = assertNextToken(TokenType.CLOSE_PAREN);
         checkCondition.append(token.getValue());
         logger.finer("Parsed OOL constraint "
            + constraintName + ": CHECK " + checkCondition);
      }

      parseConstraintState();

      token = getNextToken();
      if (token.getType() != TokenType.COMMA)
      {
         pushToken(token);
      }
   }

   /**
    * Method to parse an oracle constraint state.
    *
    * This actually just parses a subset of an constraint state,
    * namely the ENABLE/DISABLE state.
    *
    * @throws ParseException
    */
   private void parseConstraintState ()
         throws ParseException
   {
      final Token token = getNextToken();
      final TokenType type = token.getType();
      if (type == TokenType.ENABLE
            || type == TokenType.DISABLE)
      {
         logger.finer("Parsed constraint state: " + token);
      }
      else if (type == TokenType.COMMA
            || type == TokenType.CLOSE_PAREN
            || type == TokenType.COMMENT)
      {
         // end of constraint reached
         pushToken(token);
      }
      else
      {
         unexpectedToken(token);
      }
   }

   private void parseOolForeignKey (String constraintName,
         CreateTableStatement stmt)
         throws ParseException
   {
      Token token;
      TokenType type;

      // FOREIGN KEY "(" column [, column...] ")"
      //    REFERENCES schema.object [ "(" column [, column...] ")" ]
      //    [ ON DELETE ( CASCADE | SET NULL ) ]
      //    [ (ENABLE | DISABLE) ]
      assertNextToken(TokenType.KEY);
      assertNextToken(TokenType.OPEN_PAREN);
      final List fkCols = new ArrayList();
      while ((token = getNextToken()).getType() != TokenType.CLOSE_PAREN)
      {
         if (token.getType() != TokenType.COMMA)
         {
            fkCols.add(token.getValue());
         }
      }
      assertNextToken(TokenType.REFERENCES);
      token = assertNextToken(TokenType.IDENTIFIER);
      final String refTable = token.getValue();
      final List refColumns = new ArrayList();
      token = getNextToken();
      if (token.getType() == TokenType.OPEN_PAREN)
      {
         while (true)
         {
            token = assertNextToken(TokenType.IDENTIFIER);
            refColumns.add(token.getValue());
            
            token = getNextToken();
            
            if (token.getType() == TokenType.CLOSE_PAREN)
            {
               break;
            }
            else if (token.getType() == TokenType.COMMA)
            {
               // expected
               continue;
            }
            else
            {
               unexpectedToken(token);
            }
         }
      }
      else
      {
         refColumns.addAll(fkCols);
         pushToken(token);
      }

      token = getNextToken();
      type = token.getType();
      String deleteSemantic = "default";
      if (type == TokenType.ON)
      {
         assertNextToken(TokenType.DELETE);
         token = getNextToken();
         if (token.getType() == TokenType.SET)
         {
            assertNextToken(TokenType.NULL);
            deleteSemantic = "set null";
         }
         else if (token.getType() == TokenType.CASCADE)
         {
            deleteSemantic = "cascade";
         }
         else
         {
            unexpectedToken(token);
         }
      }
      else
      {
         pushToken(token);
      }

      // constraint state
      String constraintState = null;
      token = getNextToken();
      type = token.getType();
      if (type == TokenType.ENABLE
            || type == TokenType.DISABLE)
      {
         constraintState = token.getValue();
      }
      else
      {
         pushToken(token);
      }

      token = getNextToken();
      type = token.getType();
      if (type == TokenType.COMMA
            || type == TokenType.CLOSE_PAREN
            || type == TokenType.COMMENT)
      {
         // uff, we're done
         pushToken(token);
         
         final FkConstraint constraint = new FkConstraint(
               constraintName, fkCols, refTable, refColumns);
         stmt.addFkConstraint(constraint);
         logger.finer("Parsed OOL constraint "
               + constraintName + ": FOREIGN KEY: " + fkCols
               + " reference " + refTable
               + "(" + refColumns + ")"
               + " with " + deleteSemantic + " delete semantic"
               + (constraintState != null
                  ? (" and constraint state " + constraintState)
                  : ""));
      }
      else
      {
         unexpectedToken(token);
      }
   }

   private void addUniqueColumns (CreateTableStatement statement, List ukCols)
         throws ParseException
   {
      for (final Iterator it = ukCols.iterator(); it.hasNext(); )
      {
         final String colName = (String) it.next();
         final ColumnSpec col = statement.getColumnByName(colName);
         if (col == null)
         {
            throw new ParseException("Column " + colName + " not found",
                  mScanner.getLine(), mScanner.getColumn());
         }
         col.setUnique(true);
      }
   }

   /**
    * convenience method to add a set of columns as PK columns to
    * a create statement.
    *
    * @param pkCols list of column names (Strings) that should be PK
    */
   private void addPkColumns (CreateTableStatement statement, List pkCols)
         throws ParseException
   {
      // first check that no primary key exists
      checkForPrimaryKey(statement);

      for (final Iterator it = pkCols.iterator(); it.hasNext(); )
      {
         final String colName = (String) it.next();
         final ColumnSpec col = statement.getColumnByName(colName);
         if (col == null)
         {
            throw new ParseException("Column " + colName + " not found",
                  mScanner.getLine(), mScanner.getColumn());
         }
         col.setPrimaryKey(true);
      }
   }

   /**
    * checks if the statement already has a column with PK
    *
    * @param statement a SQL create table statement
    * @throws ParseException if a primary key exists in the statement
    */
   private void checkForPrimaryKey (CreateTableStatement statement)
      throws ParseException
   {
      for (final Iterator it = statement.getColumns().iterator(); 
          it.hasNext(); )
      {
         final ColumnSpec col = (ColumnSpec) it.next();
         if (col.isPrimaryKey())
         {
            throw new ParseException(col.getColumnName()
                  + " is already primary key",
                  mScanner.getLine(), mScanner.getColumn());
         }
      }
   }

   /**
    * assert that the next token is of the expected type return next token
    * @param expectedType
    * @throws ParseException
    */
   private Token assertNextToken (TokenType expectedType)
      throws ParseException
   {
      // final String methodName = "assertNextToken(TokenType)";
      // logger.entering(CLASSNAME, methodName, expectedType);
      final Token nextToken = getNextToken();
      if (nextToken.getType() != expectedType)
      {
         throw new ParseException(
            "Unexpected token: expected "
               + expectedType
               + " but got "
               + nextToken.getType(),
            mScanner.getLine(),
            mScanner.getColumn());
      }

      // logger.exiting(CLASSNAME, methodName, nextToken);
      return nextToken;
   }

   private void unexpectedToken (Token token) throws ParseException
   {
      throw new ParseException(
            "Unexpected token: " + token,
            mScanner.getLine(),
            mScanner.getColumn());
   }

   private void assertToken (Token token, TokenType expectedType)
         throws ParseException
   {
      if (token.getType() != expectedType)
      {
         throw new ParseException(
            "Unexpected token: expected "
               + expectedType
               + " but got "
               + token.getType(),
            mScanner.getLine(),
            mScanner.getColumn());
      }
   }

   /**
    * Main method is just to test parser.
    *
    * @param args command line arguments
    * @throws Exception if something goes wrong
    */
   public static void main (String[] args)
      throws Exception
   {
      final FileInputStream fin = new FileInputStream(args[0]);
      final SqlScanner scanner = new SqlScanner(fin);
      final SqlParser parser = new SqlParser(scanner);
      parser.parse();
   }
}
