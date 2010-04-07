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


/**
 * Interface for classes that collect tracing information about a certain 
 * class.
 * 
 * Instances must also hold a static factory method that returns a 
 * specific tracer, with a given name
 * <pre>
 *   public static Tracer getTracer(String className);
 * </pre>
 * the returned tracer must be multi-thread-save.
 * 
 * @author Andreas Mandel
 *
 */
public interface Tracer
{
    public boolean isTracing();

    /**
     * Indicates if the tracer is evaluating the contents of the arguments.
     * Otherwise the entering/exiting method without the arguments / result 
     * can be called by the traced code.
     * This value can change during runtime, but it is only needed to 
     * check this once, before the entering call.  
     * @return true, if the tracer is evaluating the contents of the arguments. 
     */
    public boolean isTracingArguments();
    
    /**
     * <p>A null return value is a indication that tracing is 
     * currently not enabled.</b>
     * @param className name of the class 
     * @param methodName name of the method entered.
     * @return a token, holding the context for the followup 
     *  exiting / throwing call.
     */
    public TracingToken entering(String className, String methodName);
    public TracingToken entering(String className, String methodName, Object argument);
    public TracingToken entering(String className, String methodName, Object[] arguments);
    public void exiting(TracingToken token);
    public void exiting(TracingToken token, Object result);
    public void throwing(TracingToken token, Throwable thrown);


    public interface TracingToken
    {
    }
}
