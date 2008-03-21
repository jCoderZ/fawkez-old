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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.jcoderz.commons.util.ThrowableUtil;



/**
 * Implements code common to all Exceptions.
 * <p>
 * The two base exceptions {@link org.jcoderz.commons.BaseException}
 * and {@link org.jcoderz.commons.BaseRuntimeException} and the
 * {@link org.jcoderz.commons.LogEvent} use a object of this class
 * as a member and delegate all common calls to this member.
 * </p>
 * <p>
 * This class also implements the special
 * {@link org.jcoderz.commons.Loggable} that allows to add named
 * parameters and to get a more detailed logging, with an assigned
 * error message.
 * </p>
 * <p>
 * The error response id is used to mark log entries with an unique id. This id
 * is also returned to the client (caller). If the client reports the error
 * response id it can be used to find a specific log entries more quickly. If
 * the nested exception has already an error response id, it is re-used for this
 * exception and will not get a new one.
 * </p>
 * Functionality provided by this class is:
 * <ul>
 * <li>Create a unique <code>ERROR_RESPONSE_ID</code> parameter for each
 * instance.</li>
 * <li>Create a message that contains all parameters for a full informational
 * toString() output.</li>
 * <li>Handles nested exceptions so that all information is available and
 * avoids duplicate information for the JDK1.4 environment which supports nested
 * exceptions itself.</li>
 * <li>Holds the constant names for commonly used exception parameters.</li>
 * </ul>
 *
 * @author Andreas Mandel
 */
public class LoggableImpl
      implements Serializable, Loggable
{
   /** Name of this class. */
   public static final String CLASSNAME = LoggableImpl.class.getName();

   /** Logger used for this class. */
   public static final Logger logger = Logger.getLogger(CLASSNAME);

   /** Key used for the log message info parameter object. */
   public static final String MESSAGE_INFO_PARAMETER_NAME = "_MESSAGE_INFO";

   /** Key used for the error response id added to the loggable. */
   public static final String TRACKING_NUMBER_PARAMETER_NAME
         = "_TRACKING_NUMBER";

   /** Key used for the root cause added to the loggable. */
   public static final String CAUSE_PARAMETER_NAME = "_CAUSE";

   /** Key used for the thread id parameter object. */
   public static final String THREAD_ID_PARAMETER_NAME = "_THREAD_ID";

   /** Key used for the instance id parameter object. */
   public static final String INSTANCE_ID_PARAMETER_NAME = "_INSTANCE_ID";

   /** Key used for the node id parameter object. */
   public static final String NODE_ID_PARAMETER_NAME = "_NODE_ID";

   /** Key used for the event time parameter of the loggable. */
   public static final String EVENT_TIME_PARAMETER_NAME = "_TIME";

   /** Name of the application of the loggable. */
   public static final String APPLICATION_NAME_PARAMETER_NAME = "_APPLICATION";

   /** Name of the group of the loggable. */
   public static final String GROUP_NAME_PARAMETER_NAME = "_GROUP";

   /** This nodes id. */
   public static final String NODE_ID = getStaticNodeId();

   /** Id for this instance. */
   public static final String INSTANCE_ID;

   static final long serialVersionUID = 1;

   /**
    * In the first step use bea specific instance name, which is set as system
    * property with the following name.
    * TODO: Make this bea-independent, requires entry in logging.properties,
    * system property, or something alike.
    */
   private static final String INSTANCE_NAME_PROPERTY = "weblogic.Name";

   /** Random generator to create pseudo unique Ids for each loggable. */
   private static final Random RANDOM_ID_GENERATOR = new Random();

   private static final ThreadIdHolder THREAD_ID_GENERATOR
         = new ThreadIdHolder();

   private static final String DUMMY_INSTANCE_ID
         = "P" + Integer.toHexString(RANDOM_ID_GENERATOR.nextInt());
   private static final String DUMMY_NODE_ID = "127.0.0.1";

   /**
    * list of parameter for this exception The list is not thread save!
    */
   private final Map mParameters = new HashMap();

   /**
    * Remember the ERROR_RESPONSE_ID. Intention is to log this id with the
    * exception and pass the Id to the recipient. It should be really easy to
    * find the exception in the log.
    */
   private final String mTrackingNumber;

   /** The error ID for this loggable */
   private final LogMessageInfo mLogMessageInfo;

   /** The point in time when this event occurred. */
   private final long mEventTime;

   /** The node id. */
   private final String mNodeId;

   /** The id for this instance id. */
   private final String mInstanceId;

   /** The thread id. */
   private final long mThreadId;

   /** The Throwable that caused this loggable. */
   private Throwable mCause;

   /** The outer exception, where this loggable belongs to. */
   private Loggable mOuter;

   private String mClassName = null;
   private String mMethodName = null;

   static
   {
      INSTANCE_ID = getStaticInstanceId();
   }

   LoggableImpl (Loggable outer, LogMessageInfo errorId)
   {
      this(outer, errorId, THREAD_ID_GENERATOR.getThreadId(),
            INSTANCE_ID, NODE_ID);
   }

   LoggableImpl (Loggable outer, LogMessageInfo errorId, Throwable cause)
   {
      this(outer, errorId, THREAD_ID_GENERATOR.getThreadId(), INSTANCE_ID,
            NODE_ID, cause);
   }

   LoggableImpl (Loggable outer, LogMessageInfo errorId, long threadId,
         String instanceId, String nodeId)
   {
      mEventTime = System.currentTimeMillis();
      mTrackingNumber = Integer.toHexString(RANDOM_ID_GENERATOR.nextInt());
      mLogMessageInfo = errorId;
      mThreadId = threadId;
      mInstanceId = instanceId;
      mNodeId = nodeId;
      mOuter = outer;
      initInternalParameters();
   }

   LoggableImpl (Loggable outer, LogMessageInfo errorId, long threadId,
         String instanceId, String nodeId, Throwable cause)
   {
      mEventTime = System.currentTimeMillis();
      if (cause instanceof Loggable)
      {
         mTrackingNumber = ((Loggable) cause).getTrackingNumber();
      }
      else
      {
         mTrackingNumber = Integer.toHexString(RANDOM_ID_GENERATOR.nextInt());
      }
      mLogMessageInfo = errorId;
      mThreadId = threadId;
      mInstanceId = instanceId;
      mNodeId = nodeId;
      mOuter = outer;
      initCause(cause);
      initInternalParameters();
   }

   /**
    * Sets the cause of this throwable.
    *
    * This method should be called after the call to the
    * {@link java.lang.Throwable#initCause(java.lang.Throwable)}for the case
    * the super call fails.
    *
    * @param cause the cause of this Exception.
    */
   public final void initCause (Throwable cause)
   {
      mCause = cause;
      addParameter(CAUSE_PARAMETER_NAME, cause);
      ThrowableUtil.fixChaining(cause);
   }

   /**
    * Adds a new named parameter. The parameter is added at the end of the list
    * of parameters. The same <code>name</code> might occur several times.
    *
    * @param name the name of the parameter.
    * @param value The value of the parameter
    */
   public final void addParameter (String name, Serializable value)
   {
      List values = (List) mParameters.get(name);
      if (values == null)
      {
         values = new ArrayList();
         mParameters.put(name, values);
      }
      values.add(value);
   }

   /** {@inheritDoc} */
   public List getParameter (String name)
   {
      final List values = (List) mParameters.get(name);

      final List result;
      if (values != null)
      {
         result = Collections.unmodifiableList(values);
      }
      else
      {
         result = Collections.EMPTY_LIST;
      }
      return result;
   }

   /** {@inheritDoc} */
   public Set getParameterNames ()
   {
      return Collections.unmodifiableSet(mParameters.keySet());
   }

   /** {@inheritDoc} */
   public final LogMessageInfo getLogMessageInfo ()
   {
      return mLogMessageInfo;
   }

   /** {@inheritDoc} */
   public final String getTrackingNumber ()
   {
      return mTrackingNumber;
   }

   /** {@inheritDoc} */
   public final long getEventTime ()
   {
      return mEventTime;
   }

   /** {@inheritDoc} */
   public final String getNodeId ()
   {
      return mNodeId;
   }

   /** {@inheritDoc} */
   public final String getInstanceId ()
   {
      return mInstanceId;
   }

   /** {@inheritDoc} */
   public final long getThreadId ()
   {
      return mThreadId;
   }

   /** {@inheritDoc} */
   public Throwable getCause ()
   {
      return mCause;
   }

   /** {@inheritDoc} */
   public void log ()
   {
      getSource();
      logger.logp(getLogMessageInfo().getLogLevel(), mClassName, mMethodName,
            getMessage(), mOuter);
   }

   /** {@inheritDoc} */
   public String getMessage ()
   {
      return getLogMessageInfo().formatMessage(mParameters, new StringBuffer())
            .toString();
   }

   /** {@inheritDoc} */
   public String getSourceClass ()
   {
       getSource();
       return mClassName;
   }

   /** {@inheritDoc} */
   public String getSourceMethod ()
   {
       getSource();
       return mMethodName;
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      final StringBuffer sb = new StringBuffer();
      getLogMessageInfo().formatMessage(mParameters, sb);
      sb.append(hashCode());
      sb.append('@');
      sb.append(CLASSNAME);
      return sb.toString();
   }

   private void initInternalParameters ()
   {
      addParameter(MESSAGE_INFO_PARAMETER_NAME, mLogMessageInfo);
      addParameter(TRACKING_NUMBER_PARAMETER_NAME, mTrackingNumber);
      addParameter(EVENT_TIME_PARAMETER_NAME, new Long(mEventTime));
      addParameter(THREAD_ID_PARAMETER_NAME, new Long(mThreadId));
      addParameter(INSTANCE_ID_PARAMETER_NAME, mInstanceId);
      addParameter(NODE_ID_PARAMETER_NAME, mNodeId);
      addParameter(APPLICATION_NAME_PARAMETER_NAME,
            mLogMessageInfo.getAppName());
      addParameter(GROUP_NAME_PARAMETER_NAME, mLogMessageInfo.getGroupName());
   }

   private final void getSource ()
   {
      // not analyzed yet.
      if (mMethodName == null || mClassName == null)
      {
          final StackTraceElement[] stack = (new Throwable()).getStackTrace();
          // First, search back to a method in the Logger class.
          int ix = 0;
          boolean found = false;
          while (ix < stack.length)
          {
             final StackTraceElement frame = stack[ix];
             final String cname = frame.getClassName();
             if (cname.equals(CLASSNAME))
             {
                found = true;
             }
             else if (found)
             {
                break;
             }
             ix++;
          }
          // Now search for the first frame before the "LoggableImpl" class or
          // LogMessageInfo class.
          while (ix < stack.length)
          {
             final StackTraceElement frame = stack[ix];
             try
             {
                final String cname = frame.getClassName();
                final Class clazz = Class.forName(cname);
                if (! (Loggable.class.isAssignableFrom(clazz)
                      || LogMessageInfo.class.isAssignableFrom(clazz)))
                {
                   // We've found the relevant frame.
                   setMethodAndClass(frame);
                   break;
                }
             }
             catch (ClassNotFoundException e)
             {
                setMethodAndClass(frame);
                break;
             }
             ix++;
          }
      }
   }

   private void setMethodAndClass (final StackTraceElement frame)
   {
      mClassName = frame.getClassName();
      mMethodName = frame.getMethodName();
      final String fileName = frame.getFileName();
      final int lineNumber = frame.getLineNumber();
      if (fileName != null)
      {
         if (frame.getLineNumber() >= 0)
         {
            mMethodName = frame.getMethodName()
                  + "(" + fileName + ":" + lineNumber + ")";
         }
         else
         {
            mMethodName = frame.getMethodName() + "(" + fileName + ")";
         }
      }
      else
      {
         mMethodName = frame.getMethodName() + "()";
      }
   }

   private static String getStaticNodeId ()
   {
      String nodeId = DUMMY_NODE_ID;
      try
      {
         nodeId = InetAddress.getLocalHost().getHostAddress();
      }
      catch (UnknownHostException e)
      {
         System.err.println("Error retrieving inet address of local host, "
               + "setting " + DUMMY_NODE_ID + " as node id");
      }
      return nodeId;
   }

   private static String getStaticInstanceId ()
   {
      return System.getProperty(INSTANCE_NAME_PROPERTY, DUMMY_INSTANCE_ID);
   }


   private static class ThreadIdHolder
         extends ThreadLocal
   {
      private static final long INITIAL_THREAD_ID = 10L;
      private static long sNextThreadId = INITIAL_THREAD_ID;

      protected Object initialValue ()
      {
         return new Long(sNextThreadId++);
      }

      long getThreadId ()
      {
         return ((Long) get()).longValue();
      }
   }
}
