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
package org.jcoderz.commons;


import java.io.Serializable;
import java.util.List;
import java.util.Set;


/**
 * This is the Base class for all runtime exceptions.
 * 
 * <p>In the {@link org.jcoderz.commons package overview} you can find a 
 * general statement when to use runtime exceptions.</p>
 * 
 * <p>This class can never be directly used. Services must implement a 
 * general service specific Exception from which they can derive more 
 * concrete service specific exceptions. There are some common used 
 * exceptions available as direct subclasses of this class. If 
 * appropriate this classes must be used prior generating own 
 * classes.</p>
 * 
 * <p>Most functionality is implemented and documented by the 
 * {@link org.jcoderz.commons.LoggableImpl} which is used a member of
 * objects of this class. Other stuff is handled by the base class 
 * {@link java.lang.Exception}.</p>
 *
 *
 * @see org.jcoderz.commons
 * @author Andreas Mandel
 */
public class BaseRuntimeException
      extends RuntimeException
      implements Loggable
{
   static final long serialVersionUID = 2L;
   
   /** The loggable implementation. */
   private final LoggableImpl mLoggable;
   
   /**
    * Constructor getting an log message info.
    * 
    * @param messageInfo the log message info for this exception
    */
   protected BaseRuntimeException (LogMessageInfo messageInfo)
   {
      super(messageInfo.getSymbol());
      mLoggable = new LoggableImpl(this, messageInfo);
   }

   /**
    * Constructor getting an log message info and a root exception.
    * 
    * @param messageInfo the log message info for this exception
    * @param cause the problem that caused this exception to be thrown
    */
   protected BaseRuntimeException (LogMessageInfo messageInfo, Throwable cause)
   {
      super(messageInfo.getSymbol(), cause);
      mLoggable = new LoggableImpl(this, messageInfo, cause);
   }

   /**
    * @see java.lang.Throwable#initCause(java.lang.Throwable)
    */
   public Throwable initCause (Throwable cause)
   {
      super.initCause(cause);
      mLoggable.initCause(cause);
      return this;
   }
   
   /**
    * @see Loggable#addParameter(String, Serializable)
    */
   public final void addParameter (String name, Serializable value)
   {
      mLoggable.addParameter(name, value);
   }

   /**
    * @see Loggable#getInstanceId()
    */
   public String getInstanceId ()
   {
      return mLoggable.getInstanceId();
   }

   /**
    * @see Loggable#getMessage()
    */
   public final String getMessage () 
   {
      return mLoggable.getMessage();
   }
   
   /**
    * @see Loggable#log()
    */
   public final void log ()
   {
      mLoggable.log();
   }

   /**
    * @see java.lang.Throwable#getCause()
    */
   public Throwable getCause ()
   {
      return mLoggable.getCause();
   }
   
   /**
    * @see Loggable#getEventTime()
    */
   public long getEventTime ()
   {
      return mLoggable.getEventTime();
   }
   
   /**
    * @see Loggable#getLogMessageInfo()
    */
   public LogMessageInfo getLogMessageInfo ()
   {
      return mLoggable.getLogMessageInfo();
   }
   
   /**
    * @see Loggable#getNodeId()
    */
   public String getNodeId ()
   {
      return mLoggable.getNodeId();
   }
   
   /**
    * @see Loggable#getParameter(String)
    */
   public List getParameter (String name)
   {
      return mLoggable.getParameter(name);
   }
   
   /**
    * @see Loggable#getParameterNames()
    */
   public Set getParameterNames ()
   {
      return mLoggable.getParameterNames();
   }

   /**
    * @see Loggable#getThreadId()
    */
   public long getThreadId ()
   {
      return mLoggable.getThreadId();
   }
   
   /**
    * @see Loggable#getTrackingNumber()
    */
   public String getTrackingNumber ()
   {
      return mLoggable.getTrackingNumber();
   }

   LoggableImpl getExceptionImpl ()
   {
      return mLoggable;
   }
}
