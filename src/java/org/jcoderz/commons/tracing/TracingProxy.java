/*
 * $Id: LoggingProxy.java 1011 2008-06-16 17:57:36Z amandel $
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.tracing.Tracer.TracingToken;

/**
 * <p>
 * This class can be used to proxy any object, providing tracing for 
 * all <i>interfaces</i> of the object.
 * </p>
 * <p>
 * <b>Note:</b> Java Dynamic Proxies only work on <i>interfaces</i>.
 * The object returned by the {@link #getProxy(Object)} can be cast to
 * any interface implemented by the argument or one of its ancestors. It
 * can't, however, be cast to an implementation class.
 * </p>
 *
 * @author Andreas Mandel
 */
public final class TracingProxy
      implements InvocationHandler
{
   private final Object mRealObject;
   private final String mRealObjectClassName;
   private final Tracer mObjectTracer;

   /**
    * Create a proxy that directs all calls to the real object and logs all
    * method calls with entering/exiting/throwing, using the given logger.
    *
    * @param realObject the object for which a proxy is created
    * @param logger the logger to which calls are logged
    */
   private TracingProxy (Object realObject, Tracer tracer)
   {
      mRealObject = realObject;
      mRealObjectClassName = mRealObject.getClass().getName();
      mObjectTracer = tracer;
   }

   /**
    * Static factory that wraps an object into a proxy depending on the
    * log level for that object.
    *
    * @param obj an object for which a proxy should be created
    * @return a logging proxy for the obj, if the log level for that
    *       object is FINER or finest, the object itself otherwise
    */
   public static Object getProxy (Object obj, Class tracerClass)
   {
      final String classname = obj.getClass().getName();
      final Tracer tracer = getTracer(tracerClass, classname);

      final Object proxy;
      if (tracer.isTracing())
      {
         // collect all interfaces implemented by this objects class and
         // its super classes
         //  Note: We do not add super-interfaces here....
         final Set interfaces = new HashSet();
         Class currentClass = obj.getClass();
         while (currentClass != null)
         {
            interfaces.addAll(Arrays.asList(currentClass.getInterfaces()));
            currentClass = currentClass.getSuperclass();
         }

         proxy = Proxy.newProxyInstance(
               obj.getClass().getClassLoader(),
               (Class[]) interfaces.toArray(new Class[interfaces.size()]),
               new TracingProxy(obj, tracer));
      }
      else
      {
         proxy = obj;
      }
      return proxy;
   }

   /**
    * Log the entering, exiting and throwing events of the proxied object.
    *
    * @see java.lang.reflect.InvocationHandler#invoke(
    *       java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    */
   public Object invoke (Object proxy, Method method, Object[] args)
         throws Throwable
   {
      final TracingToken tt;
      if (mObjectTracer.isTracing())
      {
         if (args == null || !mObjectTracer.isTracingArguments())
         {
            tt = mObjectTracer.entering(
                mRealObjectClassName, method.getName());
         }
         else
         {
             tt = mObjectTracer.entering(
                  mRealObjectClassName, method.getName(), args);
         }
      }
      else
      {
          tt = null;
      }

      final Object result = invokeMethod(method, args, tt);

      if (tt != null)
      {
         if (result != null 
             || method.getReturnType() != Void.TYPE 
             || !mObjectTracer.isTracingArguments())
         {
           mObjectTracer.exiting(tt, result);
         }
         else
         {
           mObjectTracer.exiting(tt);
         }
      }
      return result;
   }

   private static Tracer getTracer(Class tracer, String className)
   {
       final Tracer result;
       try
       {
           final Method method 
               = tracer.getMethod("getTracer", new Class[] {String.class});
           if (!Modifier.isStatic(method.getModifiers()))
           {
               throw new ArgumentMalformedException(
                   "tracer", tracer, 
                   "Factory method 'getTracer' must be static.");
           }
           if (!Modifier.isPublic(method.getModifiers()))
           {
               throw new ArgumentMalformedException(
                   "tracer", tracer, 
                   "Factory method 'getTracer' must be public.");
           }
           result = (Tracer) method.invoke(null, new Object[] {className});
       }
       catch (IllegalArgumentException e)
       {
           throw new ArgumentMalformedException(
               "tracer", tracer, 
               "The static tracer factory did not accept the String argument.",
               e);
       }
       catch (IllegalAccessException e)
       {
           throw new ArgumentMalformedException(
               "tracer", tracer, 
               "The static tracer factory did deny access.",
               e);
       }
       catch (InvocationTargetException e)
       {
           throw new ArgumentMalformedException(
               "tracer", tracer, 
               "The static tracer factory threw an exception with detail: "
               + e.getMessage() + ".",
               e);
       }
       catch (SecurityException e)
       {
           throw new ArgumentMalformedException(
               "tracer", tracer, 
               "Failed to look up static factory method.",
               e);
       }
       catch (NoSuchMethodException e)
       {
           throw new ArgumentMalformedException(
               "tracer", tracer, 
               "The Tracer implementation must implement a static factory " 
               + "method 'public Tracer getTracer(String)'.",
               e);
       }
       return result;
   }
   
   private Object invokeMethod (
       Method method, Object[] args, TracingToken tt)
       throws Throwable
   {
      final Object result;
      try
      {
         result = method.invoke(mRealObject, args);
      }
      catch (InvocationTargetException x)
      {
         if (tt != null)
         {
            mObjectTracer.throwing(tt, x.getCause());
         }
         throw x.getCause();
      }
      catch (Exception x)
      {
         if (tt != null)
         {
            mObjectTracer.throwing(tt, x);
         }
         throw x;
      }
      return result;
   }
}
