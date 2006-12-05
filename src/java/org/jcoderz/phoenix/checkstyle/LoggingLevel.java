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
package org.jcoderz.phoenix.checkstyle;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * This check makes sure that a class' source code does not
 * contain illegal logging levels in calls to the Java Logging
 * API. Such a check is interesting when you have a logging
 * message framework that uses generated classes for messages up to
 * a certain level in order to guarantee certain information
 * in these messages when written to the log file.
 * One example scenario is when log file entries should be sent
 * to a management console where an operator is looking at the
 * messages and needs to take actions depending on the severity
 * of the information he receives. In such cases it is important
 * that these logging messages need a special format to contain
 * all the necessary information. In our special case an XML
 * was used from which exception or warning message classes
 * had been created. Those classes had to be used for all log
 * levels INFO and above as these logging records were
 * automatically forwarded to the operators sitting in from of
 * a management console, monitoring the application behaviour.
 * For debugging purposes the developer was allowed to use
 * only the logging levels below INFO so that no debugging
 * message accidentally was sent to the management console.
 *
 * The default configuration of this check is:
 * <ul>
 *    <li>LoggerName = logger</li>
 *    <li>AllowedLoggerMethods 
 *        = fine,finer,entering,exiting,throwing,finest</li>
 *    <li>LogCallMaxLevel = FINE</li>
 * </ul>
 * The class' logger instance variable must have the name 'logger'.
 * Only calls to the methods 'fine', 'finer', 'entering', 'exiting',
 * 'throwing', and 'finest' are allowed. For the methods 'log',
 * 'logp', and 'logrb' only the logging level FINE and below is
 * allowed. You can override these settings by specifying
 * the properties in the checkstyle configuration file.
 *
 */
public class LoggingLevel
      extends Check
{
   private static final int [] TOKEN_LIST = new int[] {TokenTypes.METHOD_CALL};

   /** The required name of the logger. Default is 'logger'. */
   private static final String DEFAULT_LOGGER_NAME = "logger";

   /** This is the maximum allowed level for logXYZ() calls */
   private static final Level LOG_CALL_MAX_ALLOWED_LEVEL = Level.FINE;
   /** This prefix covers the methods: log, logp, logrb */
   private static final String LOG_CALL_PREFIX = "log";

   /** A set of all logger method names besides logXYZ(). */
   private static final Set LOGGER_METHODS = new HashSet();

   /** A set of allowed logger method names besides logXYZ(). */
   private static final Set ALLOWED_LOGGER_METHODS = new HashSet();

   private String mLoggerName = DEFAULT_LOGGER_NAME;
   private Level mLogCallMaxLevel = LOG_CALL_MAX_ALLOWED_LEVEL;

   static
   {
      // All logger methods
      LOGGER_METHODS.add("severe");
      LOGGER_METHODS.add("warning");
      LOGGER_METHODS.add("info");
      LOGGER_METHODS.add("config");
      LOGGER_METHODS.add("fine");
      LOGGER_METHODS.add("finer");
      LOGGER_METHODS.add("entering");
      LOGGER_METHODS.add("exiting");
      LOGGER_METHODS.add("throwing");
      LOGGER_METHODS.add("finest");

      // The allowed logger methods
      ALLOWED_LOGGER_METHODS.add("fine");
      ALLOWED_LOGGER_METHODS.add("finer");
      ALLOWED_LOGGER_METHODS.add("entering");
      ALLOWED_LOGGER_METHODS.add("exiting");
      ALLOWED_LOGGER_METHODS.add("throwing");
      ALLOWED_LOGGER_METHODS.add("finest");
   }

   /**
    * Sets the name of the logger instance.
    * The default name is 'logger'.
    *
    * @param loggerName The name of the logger.
    */
   public void setLoggerName (final String loggerName)
   {
      mLoggerName = loggerName;
   }

   /**
    * Sets the names of allowed logger methods.
    * The default names are: 'entering', 'exiting', 'throwing', 'fine',
    * 'finer', and 'finest'.
    *
    * @param allowedLoggerMethods The names of allowed logger methods,
    *       separated by colons.
    */
   public void setAllowedLoggerMethods (final String allowedLoggerMethods)
   {
      final StringTokenizer st = new StringTokenizer(
            allowedLoggerMethods, ",");

      ALLOWED_LOGGER_METHODS.clear();
      while (st.hasMoreTokens())
      {
         final String tok = st.nextToken();
         if (tok != null)
         {
            ALLOWED_LOGGER_METHODS.add(tok.trim());
         }
      }
   }

   /**
    * Sets the maximum allowed level for logger methods
    * starting with 'log' (log, logp, logrb).
    * The default level is 'FINE'.
    *
    * @param logCallMaxLevel The maximum allowed logger level for
    * the methods log, logp, and logrb.
    */
   public void setLogCallMaxLevel (final String logCallMaxLevel)
   {
      mLogCallMaxLevel = Level.parse(logCallMaxLevel);
   }

   /** {@inheritDoc} */
   public int[] getDefaultTokens ()
   {
      final int [] rc = new int[TOKEN_LIST.length];
      System.arraycopy(TOKEN_LIST, 0, rc, 0, rc.length);
      return rc;
   }

   /** {@inheritDoc} */
   public void visitToken (final DetailAST ast)
   {
      switch (ast.getType())
      {
         case TokenTypes.METHOD_CALL:
            visitMethodCall(ast);

         default:
            break;
      }
   }

   /**
    * Visits a method call token. Since we are interested in logger.method calls
    * only, the first child must be a DOT type, otherwise no interest.
    *
    * @param methCall The visited token
    */
   private void visitMethodCall (final DetailAST methCall)
   {
      final DetailAST dot = methCall.findFirstToken(TokenTypes.DOT);

      if (dot != null)
      {
         // the first child of the dot-Token is the variable name, which is
         // 'logger' in our case, otherwise not interested.
         final DetailAST varName = dot.findFirstToken(TokenTypes.IDENT);
         if (varName.getText().equals(mLoggerName))
         {
            visitLoggerCall(methCall, varName);
         }
      }
   }

   /**
    * Visits a logger call. The supplied node is the logger variable, the next
    * sibling the called logger method.
    *
    * @param methCall The method call node.
    * @param logger The Logger variable used in the logger call.
    */
   private void visitLoggerCall (
         final DetailAST methCall,
         final DetailAST logger)
   {
      final DetailAST method = (DetailAST) logger.getNextSibling();
      if (method != null)
      {
         final String methodName = method.getText();
   
         if (methodName.startsWith(LOG_CALL_PREFIX))
         {
            visitExpressionList(methCall, method);
         }
         else
         {
            // ignore all other methods than those defined above
            if (LOGGER_METHODS.contains(methodName)
                  && !ALLOWED_LOGGER_METHODS.contains(methodName))
            {
               logDisallowedLoggerMethod(method);
            }
         }
      }
   }

   /**
    * Checks the first parameter given to the supplied method call
    *
    * @param methCall The method call node.
    * @param method The called logger method.
    */
   private void visitExpressionList (
         final DetailAST methCall,
         final DetailAST method)
   {
      final DetailAST expressionList = methCall.
            findFirstToken(TokenTypes.ELIST);
      if (expressionList != null)
      {
         final DetailAST level = expressionList.findFirstToken(TokenTypes.EXPR);
         if (level != null)
         {
            final DetailAST dot = level.findFirstToken(TokenTypes.DOT);
            visitLoggerLevel(methCall, dot);
         }
      }
   }

   private void visitLoggerLevel (
         final DetailAST methCall,
         final DetailAST dot)
   {
      /* in this case we are interested in the last child, which gives the
         log level. */
      final DetailAST logLevel = dot.findFirstToken(TokenTypes.IDENT);
      if (logLevel != null)
      {
         final DetailAST levelName = (DetailAST) logLevel.getNextSibling();
         if (levelName != null)
         {
            final Level level = Level.parse(levelName.getText());
            if (level.intValue() > mLogCallMaxLevel.intValue())
            {
               logDisallowedLogLevel(levelName);
            }
         }
      }
   }

   /**
    * Logs the disallowed logger level.
    *
    * @param level The disallowed log level.
    */
   private void logDisallowedLogLevel (final DetailAST level)
   {
      // trace.loglevel=Maximum allowed log level for trace log is 
      //     ''{0}'' but was ''{1}''.
      log(level.getLineNo(), "trace.loglevel",
            new Object[] {mLogCallMaxLevel, level.getText()});
   }

   /**
    * Logs the disallowed logger method call.
    *
    * @param method The disallowed logger method.
    */
   private void logDisallowedLoggerMethod (final DetailAST method)
   {
      // trace.logmethod=Logger method ''{0}'' is not allowed.
      log(method.getLineNo(), "trace.logmethod",
            new Object[] {method.getText()});
   }
}
