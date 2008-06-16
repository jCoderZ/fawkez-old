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
package org.jcoderz.guidelines.snippets;


import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class description goes here.
 * @author Firstname Lastname
 */
public class JCoderZJavaExample
{
    /* A class implementation comment can go here. */

    /** MIN_WIDTH documentation comment. */
    public static final int MIN_WIDTH = 4;

    /** MAX_WIDTH documentation comment. */
    public static final int MAX_WIDTH = 999;

    /** BAD_PARAM documentation comment. */
    public static final int BAD_PARAM = -1;

    /** INTERNAL_ERROR documentation comment. */
    public static final int INTERNAL_ERROR = -2;

    /** classVar1 documentation comment. */
    protected static int sClassVar1;

    /**
     * The logger.
     */
    private static final Logger logger =
            Logger.getLogger(JCoderZJavaExample.class.getName());

    private static final String CLASS_NAME = "JCoderZJavaExample";

    /**
     * classVar2 documentation comment that happens to be more than one
     * line long.
     */
    private static Object sClassVar2;

    /** instanceVar2 documentation comment. */
    protected int mInstanceVar2;

    /** instanceVar3 documentation comment. */
    private Object[] mInstanceVar3;

    /**
     * ... Constructor blah documentation comment...
     */
    public JCoderZJavaExample ()
    {
        // ...implementation goes here...
        if (logger.isLoggable(Level.FINER))
        {
            logger.finer("Sample constructor " + this);
        }
    }

    /**
     * ... method doSomething documentation comment...
     */
    public final void setupConfiguration ()
    {
        // write message to log
        logger.info("Message");
    }

    /**
     * ... method doSomething documentation comment...
     */
    public void doSomething ()
    {
        // ...implementation goes here...
    }

    /**
     * ...method doSomethingElse documentation comment...
     * @param someParam this is the input parameter
     * @return result The method returns an int.
     */
    public int doSomethingElse (Object someParam)
    {
        int result = BAD_PARAM;

        if (logger.isLoggable(Level.FINER))
        {
            logger.entering(CLASS_NAME, "doSomethingElse",
                    new Object[] {someParam});
        }

        // ...implementation goes here...
        if (BAD_PARAM == result)
        {
            final RuntimeException ex 
                    = new RuntimeException("Bad parameter");
            if (logger.isLoggable(Level.FINER))
            {
                logger.throwing(CLASS_NAME, "doSomethingElse", ex);
            }
            throw ex;
        }
        try
        {
            result = doSometingFine();
        }
        catch (Throwable th)
        {
            result = INTERNAL_ERROR;
        }

        // FIXME: finally must log exiting() function
        if (logger.isLoggable(Level.FINER))
        {
            logger.exiting(CLASS_NAME, "doSomethingElse", 
                    new Integer(result));
        }
        return result;
    }

    /**
     * Method doSomethingElse documentation comment.
     * @return result The method returns an int.
     */
    private int doSometingFine ()
    {
        // ...implementation goes here...
        return 0;
    }

}
