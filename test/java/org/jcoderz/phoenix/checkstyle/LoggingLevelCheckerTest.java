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

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 * Checks the checkstyle logging level checker.
 *
 */
public class LoggingLevelCheckerTest
      extends TestCase
{
   private static final Logger logger
         = Logger.getLogger(LoggingLevelCheckerTest.class.getName());

   public void testDummy ()
   {
      // NOP
   }
   
   /**
    * Test logger methods.
    */
   public void loggerMethodTest ()
   {
      logger.finest("A finest message");
      logger.throwing("LoggingLevelCheckerTest", "constructor",
            new Exception("Test"));
      logger.entering("LoggingLevelCheckerTest", "constructor");
      logger.exiting("LoggingLevelCheckerTest", "constructor");
      logger.finer("A finer message");
      logger.fine("A fine message");
      logger.config("A config message");
      logger.info("An info message");
      logger.warning("A warning message");
      logger.severe("A severe message");
   }
   /**
    * Test logger levels.
    */
   public void loggerLevelTest ()
   {
      logger.log(Level.ALL, "An all message");
      logger.log(Level.FINEST, "A finest message");
      logger.log(Level.FINER, "A finer message");
      logger.log(Level.FINE, "A fine message");
      logger.log(Level.CONFIG, "A config message");
      logger.log(Level.INFO, "An info message");
      logger.log(Level.WARNING, "A warning message");
      logger.log(Level.SEVERE, "A severe message");
      logger.log(Level.OFF, "An off message");
   }
}
