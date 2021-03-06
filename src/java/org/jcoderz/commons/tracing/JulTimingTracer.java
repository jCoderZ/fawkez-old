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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A Tracer implementation that used {@link java.util.logging} entering 
 * exiting logger as tracing sink and adds timing information.
 * The format of the added timing information is '(after {0}ms)', the
 * precision relies on {@link System#currentTimeMillis()}.
 * 
 * See {@link Logger#entering(String, String)} for an example.
 *
 * @author Andreas Mandel
 */
public class JulTimingTracer
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
    private JulTimingTracer (String tracerName)
    {
        mLogger = Logger.getLogger(tracerName);
        mClassName = tracerName;
    }

    /**
     * Factory for a tracer for classes with the given name.
     * @param className the name of the class that uses this tracer.
     * @return a tracer for the given class that dumps trace output to 
     *      java logging.
     */
    public static JulTimingTracer getTracer (String className)
    {
        return new JulTimingTracer(className);
    }


    /**
     * Returns true, if the level of the underlying java logger is
     * {@link Level#FINER} or below.
     * @see org.jcoderz.commons.tracing.Tracer#isTracing()
     */
    public boolean isTracing ()
    {
        return mLogger.isLoggable(Level.FINER);
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
     * Delegates to {@link Logger#entering(String, String)}.
     * The className is expected to be the same as the tracer name and 
     * is silently ignored.
     * @see Tracer#entering(java.lang.String, java.lang.String)
     */
    public TracingToken entering (String className, String methodName)
    {
        final TracingToken result;
        if (isTracing())
        {
            mLogger.entering(getClassName(), methodName);
            result = new JulTimingTracingToken(methodName);
        }
        else
        {
            result = null;
        }
        return result;
    }

    /**
     * Delegates to {@link Logger#entering(String, String, Object)}.
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
            mLogger.entering(getClassName(), methodName, argument);
            result = new JulTimingTracingToken(methodName);
        }
        else
        {
            result = null;
        }
        return result;
    }

    /**
     * Delegates to {@link Logger#entering(String, String, Object)}.
     * The className is expected to be the same as the tracer name and 
     * is silently ignored.
     * @see Tracer#entering(String, String, Object[])
     */
    public TracingToken entering (String className, String methodName,
        Object[] arguments)
    {
        final TracingToken result;
        if (isTracing())
        {
            mLogger.entering(getClassName(), methodName, arguments);
            result = new JulTimingTracingToken(methodName);
        }
        else
        {
            result = null;
        }
        return result;
    }

    /**
     * Logs the method exit like 
     * {@link Logger#exiting(String, String)} with additional
     * execution time information.
     * If the token is not a {@link JulTimingTracingToken} the call is 
     * silently ignored.
     * @see Tracer#exiting(TracingToken)
     */
    public void exiting (TracingToken token)
    {
        if (token instanceof JulTimingTracingToken)
        {
            final JulTimingTracingToken julTracingToken 
                = (JulTimingTracingToken) token;
            mLogger.logp(
                Level.FINER, mClassName, julTracingToken.getMethodName(), 
                "RETURN (after {0}ms)", 
                new Long(julTracingToken.getElapsedMillis()));
            mLogger.exiting(getClassName(), julTracingToken.getMethodName());
        }
    }

    /**
     * Logs the returned result like 
     * {@link Logger#exiting(String, String, Object)} with additional
     * execution time information.
     * If the token is not a {@link JulTimingTracingToken} the call is 
     * silently ignored.
     * @see Tracer#exiting(TracingToken, java.lang.Object)
     */
    public void exiting (TracingToken token, Object result)
    {
        if (token instanceof JulTimingTracingToken)
        {
            final JulTimingTracingToken julTracingToken 
                = (JulTimingTracingToken) token;
            mLogger.logp(
                Level.FINER, mClassName, julTracingToken.getMethodName(), 
                "RETURN {0} (after {1}ms)", 
                new Object[] 
                { 
                    result, new Long(julTracingToken.getElapsedMillis()) 
                });
        }
    }

    /**
     * Logs the thrown exception like 
     * {@link Logger#throwing(String, String, Throwable)} with additional
     * execution time information.
     * If the token is not a {@link JulTimingTracingToken} the call is 
     * silently ignored.
     * @see Tracer#throwing(TracingToken, java.lang.Throwable)
     */
    public void throwing (TracingToken token, Throwable thrown)
    {
        if (token instanceof JulTimingTracingToken)
        {
            final JulTimingTracingToken julTracingToken 
                = (JulTimingTracingToken) token;
            final LogRecord record 
                = new LogRecord(Level.FINER, "THROW (after {0}ms)");
            record.setSourceClassName(mClassName);
            record.setSourceMethodName(julTracingToken.getMethodName());
            record.setParameters(
                new Object[] {new Long(julTracingToken.getElapsedMillis())});
            record.setThrown(thrown);
            mLogger.log(record);
        }
    }
    
    /**
     * Stores context information for the {@link JulTimingTracer}.
     * @see TracingToken
     * @author Andreas Mandel
     */
    private class JulTimingTracingToken implements TracingToken
    {
        private final String mMethodName;
        private final long mStartTime = System.currentTimeMillis();
        
        public JulTimingTracingToken (String methodName)
        {
            mMethodName = methodName;
        }

        public String getMethodName ()
        {
            return mMethodName;
        }

        public long getElapsedMillis ()
        {
            // ignore the fact that this might overflow
            return System.currentTimeMillis() - mStartTime;
        }
        
    }
}
