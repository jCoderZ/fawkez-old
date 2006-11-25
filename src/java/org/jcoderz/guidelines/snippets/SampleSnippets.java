// BEGIN SNIPPET: CopyrightHeader.xml
/*
 * $Id: SampleSnippets.java 259 2006-07-21 07:55:02Z amandel $
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
// END SNIPPET
package org.jcoderz.guidelines.snippets;


// BEGIN SNIPPET: SingleTypeImports.xml
import java.io.IOException;
import java.io.Serializable;
// END SNIPPET
import java.util.ArrayList;
import java.util.List;
import java.util.EventObject;

// BEGIN SNIPPET: OnDemandImports.xml
import java.util.*; // DON'T
// END SNIPPET
import java.util.logging.Logger;


import javax.naming.Context;
import javax.servlet.http.HttpServletResponse;


/**
 * Blabla.
 *
 * @author mrumpf
 */
public class SampleSnippets
        implements Serializable
{
    private static final int DEFAULT_SIZE = 30;
    private static final int SIZE = 30;

    // utility class
    private SampleSnippets ()
    {

    }

    // BEGIN SNIPPET: ExampleClass.xml
    /**
     * The Example class provides ...
     * @author Stephen Mohr
     * @author Oliver Griffin
     */
    public class ExampleClass
    {
        // ...
    }
    // END SNIPPET

    private static int funtionCallReturn ()
    {
        final java.util.ArrayList list = new java.util.ArrayList();
        int size = list.size();
      
        if (size > 0)
        {   
            // BEGIN SNIPPET: ReturnStatement.xml
            return list.size();
            // PAUSE SNIPPET
        }
        else
        {   
            // RESUME SNIPPET      
            // OR
            return (size != 0 ? size : DEFAULT_SIZE);
            // END SNIPPET
        }
    }

    private static void statements ()
    {
        int i = 0;

        // BEGIN SNIPPET: IfStatement.xml
        if (i > 0)
        {
            // ...
        }

        if (i > 0)
        {
            // ...
        }
        else
        {
            // ...
        }

        if (i > 0)
        {
            // ...
        }
        else if (i == 0)
        {
            // ...
        }
        else
        {
            // ...
        }
        // END SNIPPET

        // BEGIN SNIPPET: ForStatement.xml
        for (int j = 0; j < SIZE; j++)
        {
            // ...
        }
        // END SNIPPET

        // BEGIN SNIPPET: WhileLoop.xml
        while (i > 0)
        {
            // ...
        }
        // END SNIPPET

        // FIXME: this should not be allowed!
        // BEGIN SNIPPET: SimpleWhileLoop.xml
        while (--i > 0);  // DON'T
        // END SNIPPET

        // BEGIN SNIPPET: DoWhileLoop.xml
        do
        {
            // ...
        }
        while (i > 0);
        // END SNIPPET

        // BEGIN SNIPPET: Switch.xml
        switch (i)
        {
            case HttpServletResponse.SC_ACCEPTED:
                // ...
                /* falls through */
            case HttpServletResponse.SC_BAD_REQUEST:
                // ...
                break;
            case HttpServletResponse.SC_CONTINUE:
                // ...
                break;
            default:
                throw new RuntimeException("Unexpected condition.");
                // no break here because position is unreachable!
        }
        // END SNIPPET

        // BEGIN SNIPPET: TryCatchFinally.xml
        try
        {
            // ...
        }
        catch (IllegalArgumentException ex)
        {
            // ...
        }

        try
        {
            // ...
        }
        catch (IllegalArgumentException ex)
        {
            // ...
        }
        finally
        {
            // ...
        }

        try
        {
            // ...
        }
        finally
        {
            // ...
        }
        // END SNIPPET
    }

    private static boolean aMethod (byte a, Object b)
    {
        return true;
    }

    // BEGIN SNIPPET: MethodDeclarationWhitespace.xml
    private static void anotherMethod (int a, int b)
    {
        // ...
    }
    // END SNIPPET

    private static void whitespace ()
    {
        int a = 0;
        int b = 1;
        int c = -1;
        // BEGIN SNIPPET: BinaryOperatorWhitespace.xml
        int d = 1;
        b = (a + b) / (c * ++d);
        System.out.println("c=" + c + "\n");
        // END SNIPPET

        final Integer x = new Integer(b);
        // BEGIN SNIPPET: Casts.xml
        final boolean result = aMethod((byte) a, (Object) x);
        anotherMethod((int) (a + 1), ((int) (b + MAX_LOOPS)) + 1);
        // END SNIPPET
    }

    // BEGIN SNIPPET: Constants.xml
    static final int MIN_WIDTH = 4;
    static final int MAX_WIDTH = 999;
    static final int GET_THE_CPU = 1;
    static final long serialVersionUID = -7064645359225861305L;
    static final Logger logger 
            = Logger.getLogger(SampleSnippets.class.getName());
    // END SNIPPET

    private static void naming ()
    {
        // BEGIN SNIPPET: VariableNames.xml
        int i;
        char c;
        float myWidth;
        // END SNIPPET
    }

    // BEGIN SNIPPET: VariableDeclarations.xml
    void yetAnotherMethod ()
    {
        int int1 = 0; // beginning of method block

        if (int1 == 0)
        {
            int int2 = 0; // beginning of "if" block

            // ...
        }
    }
    // END SNIPPET

    private static final int MAX_LOOPS = 100;

    private static void practices ()
    {
        // BEGIN SNIPPET: VariableDeclarationsException.xml
        for (int i = 0; i < MAX_LOOPS; i++)
        {
            // ...
        }
        // END SNIPPET
    }

    // BEGIN SNIPPET: HideGlobalMember.xml
    int count; // should be mCount anyway!

    void counter ()
    {
        if (count > 0)
        {
            int count;  // DON'T

            // ...
        }

        // ...
    }
    // END SNIPPET

    // BEGIN SNIPPET: InterfaceServiceDeclaration.xml
    public interface ActionListener
    {
        void actionPerformed (EventObject event);
    }
    // END SNIPPET

    // BEGIN SNIPPET: InterfaceCapabilitiesDeclaration.xml
    public interface Runnable
    {
        void run ();
    }

    public interface Accessible
    {
        Context getContext ();
    }
    // END SNIPPET

    public static final int MAX_CUSTOMERS = 100;

    // BEGIN SNIPPET: PluralizeSample.xml
    private Object[] mCustomers = new Object[MAX_CUSTOMERS];

    void addCustomer (int index, Object customer)
    {
        mCustomers[index] = customer;
    }
    // END SNIPPET

    // BEGIN SNIPPET: IndentationExtendsAndImplements.xml
    public class IndentionSample
        extends SampleSnippets
        implements Serializable, Cloneable, Comparable
    {   
        /**
         * 
         */
        public void doSomething (int length)
            throws IOException
        // ...
    // END SNIPPET
        {
            final List servers = new ArrayList();
            /*
            // BEGIN SNIPPET: IndentationSampleExtended.xml
            final SimpleBusinessResultException e
                    = new SimpleBusinessResultException(
                        ResultCode.SPLIT_AUTHORIZATION_SPLIT_INDEX_UNEXPECTED);
             // END SNIPPET
             */
        }
        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo (Object o)
        {
            // TODO Auto-generated method stub
            return 0;
        }
    }

}
