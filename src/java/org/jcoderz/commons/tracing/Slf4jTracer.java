/*
 * $Id: ArraysUtil.java 1011 2008-06-16 17:57:36Z amandel $
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
package org.jcoderz.commons.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Tracer implementation that used {@link org.slf4j.Logger} 
 * as tracing sink.
 * See {@link java.util.logging.Logger#entering(String, String)} for an 
 * example of the format and semantic used.
 * 
 * TODO: Need Marker for classname & methodname? How?
 *
 * @author Andreas Mandel
 */
public class Slf4jTracer
    implements Tracer
{
    /** The backing java logger. */
    private final Logger mLogger;
    /** Name of the class where this logger belongs to. */
    private final String mClassName;
    
    
    /**
     * Create a new instance of this Tracer.
     * The underlying java logger is instantly created. 
     * @param tracerName the name of the tracer to be created, should be 
     *     equal to the name of the class that uses the tracer.
     */
    private Slf4jTracer (String tracerName)
    {
        mLogger = LoggerFactory.getLogger(tracerName);
        mClassName = tracerName;
    }

    /**
     * Factory for a tracer for classes with the given name.
     * @param className the name of the class that uses this tracer.
     * @return a tracer for the given class that dumps trace output to 
     *      java logging.
     */
    public static Slf4jTracer getTracer (String className)
    {
        return new Slf4jTracer(className);
    }


    /**
     * Returns true, if the level of the underlying slf4j logger has
     * trace enabled.
     * @see Logger#isTraceEnabled()
     */
    public boolean isTracing ()
    {
        return mLogger.isTraceEnabled();
    }

    /**
     * Returns always true.
     * @see org.jcoderz.commons.tracing.Tracer#isTracingArguments()
     */
    public boolean isTracingArguments ()
    {
        return true;
    }

    /**
     * Returns the Name of the class where this tracer belongs to.
     * @return the Name of the class where this tracer belongs to.
     */
    public String getClassName ()
    {
        return mClassName;
    }

    /**
     * Delegates to {@link Logger#trace(org.slf4j.Marker, String).
     * The className is expected to be the same as the tracer name and 
     * is silently ignored.
     * @see Tracer#entering(java.lang.String, java.lang.String)
     */
    public TracingToken entering (String className, String methodName)
    {
        final TracingToken result;
        if (isTracing())
        {
            mLogger.trace(/* getClassName(), methodName, */ "ENTRY");
            result = new Slf4jTracingToken(methodName);
        }
        else
        {
            result = null;
        }
        return result;
    }

    /**
     * Delegates to {@link Logger#trace(String, Object)}.
     * The className is expected to be the same as the tracer name and 
     * is silently ignored.
     * @see Tracer#entering(String, String, Object)
     */
    public TracingToken entering (String className, String methodName,
        Object argument)
    {
        final TracingToken result;
        if (isTracing())
        {
            mLogger.debug(/* getClassName(), methodName, */ "ENTRY {}", 
                argument);
            result = new Slf4jTracingToken(methodName);
        }
        else
        {
            result = null;
        }
        return result;
    }

    /**
     * Delegates to {@link Logger#trace(String, String, Object)}.
     * The className is expected to be the same as the tracer name and 
     * is silently ignored.
     * @see Tracer#entering(String, String, Object[])
     */
    public TracingToken entering (
        String className, String methodName, Object[] arguments)
    {
        final TracingToken result;
        if (isTracing())
        {
            String msg = "ENTRY";
            if (arguments == null ) 
            {
               result = entering(className, methodName);
            }
            else
            {
                for (int i = 0; i < arguments.length; i++) 
                {
                    msg = msg + " {}";
                }
                mLogger.trace(/*getClassName(), methodName,*/ msg, arguments);
                result = new Slf4jTracingToken(methodName);
            }
        }
        else
        {
            result = null;
        }
        return result;
    }

    /**
     * Delegates to {@link Logger#trace(String, Object)} the message
     * string used is "RETURN".
     * If the token is not a {@link Slf4jTracingToken} the call is 
     * silently ignored.
     * @see Tracer#exiting(TracingToken)
     */
    public void exiting (TracingToken token)
    {
        if (token instanceof Slf4jTracingToken)
        {
            // final Slf4jTracingToken tt = (Slf4jTracingToken) token;
            mLogger.trace(/* getClassName(), tt.getMethodName(), */ "RETURN");
        }
    }

    /**
     * Delegates to {@link Logger#trace(String, Object)}.
     * If the token is not a {@link Slf4jTracingToken} the call is 
     * silently ignored.
     * @see Tracer#exiting(TracingToken, java.lang.Object)
     */
    public void exiting (TracingToken token, Object result)
    {
        if (token instanceof Slf4jTracingToken)
        {
            // final Slf4jTracingToken tt = (Slf4jTracingToken) token;
            mLogger.trace(
                /* getClassName(), tt.getMethodName(), */ "RETURN {}",
                result);
        }
    }

    /**
     * Delegates to {@link Logger#throwing(String, String, Throwable)}.
     * If the token is not a {@link Slf4jTracingToken} the call is 
     * silently ignored.
     * @see Tracer#throwing(TracingToken, java.lang.Throwable)
     */
    public void throwing (TracingToken token, Throwable thrown)
    {
        if (token instanceof Slf4jTracingToken)
        {
            // final Slf4jTracingToken tt = (Slf4jTracingToken) token;
            mLogger.trace(
                /* getClassName(), tt.getMethodName(), */ "THROW",
                thrown);
        }
    }
    
    /**
     * Stores context information for the {@link Slf4jTracer}.
     * @see TracingToken
     * @author Andreas Mandel
     */
    private class Slf4jTracingToken implements TracingToken
    {
        private final String mMethodName;
        
        public Slf4jTracingToken (String methodName)
        {
            mMethodName = methodName;
        }

        public String getMethodName ()
        {
            return mMethodName;
        }
        
    }
}
