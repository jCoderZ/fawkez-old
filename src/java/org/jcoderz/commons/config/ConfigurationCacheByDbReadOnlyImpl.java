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
package org.jcoderz.commons.config;

import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.EntityNotFoundException;

/**
 * This ConfigurationCache implementation is based on a ReadOnly EntityBean
 * that stores its data within a database table.
 *
 */
public final class ConfigurationCacheByDbReadOnlyImpl
      implements ConfigurationCacheInterface
{
   /** Name of this class. */
   private static final String CLASSNAME 
           = ConfigurationCacheByDbReadOnlyImpl.class.getName();

   /** Logger for this class. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   /** The mutex object to synchronize the config keys map. */
   private static final Object MUTEX_KEYS = new Object();

   /** The mutex object to synchronize the services set. */
   private static final Object MUTEX_SERVICES = new Object();


   /**
    * cache the configuration keys also, to prevent set creation for every
    * {@link #getKeys()} call
    */
   private Set mConfigurationKeys = null;

   /**
    * This Set contains the Services that have been registered to receive
    * notifications when the cache has been changed.
    * The Set contains objects that implement the ConfigurationListener
    * interface.
    */
   private final Set mRegisteredServices = new HashSet();

   private boolean mIsInitialized;
   private Exception mInitException;

   /**
    * Singleton has no public constructor
    *
    * @see ConfigurationCacheByDbReadOnlyImpl#current()
    */
   private ConfigurationCacheByDbReadOnlyImpl ()
   {
      try
      {
         init();
         mIsInitialized = true;
         mInitException = null;
      }
      catch (Exception x)
      {
         mIsInitialized = false;
         mInitException = x;
         final ConfigurationInitializationFailedException sysEvt
               = new ConfigurationInitializationFailedException(x);
         sysEvt.log();
      }
   }


   /**
    * Returns a reference to the one and only object instance of this
    * class. Use this method to access the object's methods.
    *
    * @return a reference to the one and only object instance of this class.
    */
   public static ConfigurationCacheByDbReadOnlyImpl current ()
   {
      final ConfigurationCacheByDbReadOnlyImpl result
            = ConfigurationCacheHolder.INSTANCE;
      if (! result.mIsInitialized)
      {
         throw new ConfigurationInitializationFailedException(
               result.mInitException);
      }
      return result;
   }


   /**
    * Setup configuration cache as MBean and initialize cache.
    */
   private void init ()
   {
      final String method = "init";

      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, method);
      }

      reloadCache();

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, method);
      }
   }

   /**
    * {@inheritDoc}
    * Returns the String value set for the given configuration key.
    * @param key the key to look up.
    * @return the value as string.
    * @throws ConfigurationValueNotFoundException if the key is not set or
    *       the resource was not found.
    */
   public String getString (String key)
         throws ConfigurationValueNotFoundException
   {
      final String methodName = "getString";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, methodName, key);
      }
      final String result;
      try
      {
         // No caching in this class is used for the config entry,
         // because EntityBean is defined with ReadOnly and timeout=0.
         final ConfigEntity entity
               = ConfigEntityHelper.findReadOnlyByPrimaryKey(key);
         result = entity.getValue();
         CfgLogMessage.ConfigurationValueRead.log(key, result);
      }
      catch (EntityNotFoundException e)
      {
         throw new ConfigurationValueNotFoundException(key, e);
      }
      catch (RemoteException re)
      {
         throw new ConfigurationValueNotFoundException(key, re);
      }

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, methodName, result);
      }
      return result;
   }

   /** {@inheritDoc} */
   public Set getKeys ()
   {
      final Set result;

      synchronized (MUTEX_KEYS)
      {
         result = Collections.unmodifiableSet(mConfigurationKeys);
      }

      return result;
   }


   /** {@inheritDoc} */
   public void addConfigurationListener (ConfigurationListener newListener)
   {
      synchronized (MUTEX_SERVICES)
      {
         mRegisteredServices.add(new WeakReference(newListener));
      }
   }


   /**
    * Reloads the internal configuration cache from resource bundle.
    * If an exception occurs during, the old cached properties are
    * returned.
    */
   public void reloadCache ()
   {
      final String method = "reloadCache";
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, method);
      }

      try
      {
         final Collection all = ConfigEntityHelper.findAllReadOnly();
         final Set keys = new HashSet(all.size());
         for (final Iterator iter = all.iterator(); iter.hasNext();)
         {
            final ConfigEntity element = (ConfigEntity) iter.next();
            keys.add(element.getConfigKey());
         }
         // This block violates the j2ee spec.
         synchronized (MUTEX_KEYS)
         {
            mConfigurationKeys = keys;
         }
      }
      catch (Exception e)
      {
         final ConfigurationInitializationFailedException cife
               = new ConfigurationInitializationFailedException(e);
         // CHECKME: create special config cache reload exception
         cife.addParameter("DETAIL",
               "Exception while updating ConfigurationCache.");
         throw cife;
      }

      notifyServices();

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, method);
      }
   }

   /**
    * Notifies all registered services about the cache update.
    */
   private void notifyServices ()
   {
      final ConfigUpdateEvent event = new ConfigUpdateEvent(this,
            ConfigUpdateEvent.CACHE_UPDATED);

      final List alive_listeners = new ArrayList();
      final List dead_listeners = new ArrayList();

      synchronized (MUTEX_SERVICES)
      {
         final Iterator it = mRegisteredServices.iterator();
         while (it.hasNext())
         {
            final WeakReference wk = (WeakReference) it.next();
            final ConfigurationListener cl = (ConfigurationListener) wk.get();
            if (cl == null)
            {
               dead_listeners.add(wk);
            }
            else
            {
               alive_listeners.add(cl);
            }
         }
         mRegisteredServices.removeAll(dead_listeners);
      }

      for (int i = 0; i < alive_listeners.size(); i++)
      {
         ((ConfigurationListener) alive_listeners.get(i))
               .updateConfiguration(event);
      }
   }

   /**
    * Local helper class holding a member initialized by calling
    * the private constructor of the singleton.
    */
   private static final class ConfigurationCacheHolder
   {
      private static final ConfigurationCacheByDbReadOnlyImpl INSTANCE
         = new ConfigurationCacheByDbReadOnlyImpl();
   }
}
