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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This ConfigurationCache implementation is based on a property file.
 *
 */
public final class ConfigurationCacheByPropertiesImpl
      implements ConfigurationCacheInterface
{
   // the property file's bundle name for the configuration
   private static final String BUNDLE_NAME
         = "org.jcoderz.commons.config.configuration";

   /**
    * Name of this class
    */
   private static final transient
      String CLASSNAME = ConfigurationCacheByPropertiesImpl.class.getName();

   /**
    * Logger for this class
    */
   private static final transient Logger logger = Logger.getLogger(CLASSNAME);

   /**
    * The resource bundle where the config data is cached.
    * It contains the cached Configuration values.
    * The key is a ConfigurationKey object and the value
    * a String representing the value.
    */
   private ResourceBundle mResourceBundle = null;

   /**
    * cache the configuration keys also, to prevent set creation for every
    * getKeys() call
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
    * @see ConfigurationCacheByPropertiesImpl#current
    */
   private ConfigurationCacheByPropertiesImpl ()
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
   public static ConfigurationCacheByPropertiesImpl current ()
   {
      final ConfigurationCacheByPropertiesImpl result
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
    * @see ConfigurationCacheInterface#getString
    * Returns the String value set for the given configuration key.
    * @param key the key to look up.
    * @return the value as string.
    * @throws ConfigurationValueNotFoundException if the key is not set or
    *       the resource was not found.
    */
   public String getString (String key)
         throws ConfigurationValueNotFoundException
   {
      final String result;
      try
      {
         result = mResourceBundle.getString(key);
         CfgLogMessage.ConfigurationValueRead.log(key, result);
      }
      catch (MissingResourceException e)
      {
         throw new ConfigurationValueNotFoundException(key, e);
      }
      return result;
   }

   /**
    * @see ConfigurationCacheInterface#getKeys()
    */
   public Set getKeys ()
   {
      return mConfigurationKeys;
   }


   /**
    * @see ConfigurationServiceInterface#addConfigurationListener(ConfigurationListener)
    */
   public void addConfigurationListener (ConfigurationListener newListener)
   {
      mRegisteredServices.add(newListener);
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
         mResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
         final Enumeration keys = mResourceBundle.getKeys();
         mConfigurationKeys = new HashSet(java.util.Collections.list(keys));
      }
      catch (Exception e)
      {
         final ConfigurationInitializationFailedException cife
               = new ConfigurationInitializationFailedException(e);
         // CHECKME: create special config cache reload exception?
         cife.addParameter("DETAIL",
               "Exception while updating ConfigurationCache.");
         throw cife;
      }

      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, method);
      }
      notifyServices();
   }

   /**
    * Notifies all registered services about the cache update.
    */
   private void notifyServices ()
   {
      final Iterator it = mRegisteredServices.iterator();
      final ConfigUpdateEvent event = new ConfigUpdateEvent(this,
         ConfigUpdateEvent.CACHE_UPDATED);
      while (it.hasNext())
      {
         ((ConfigurationListener) it.next()).updateConfiguration(event);
      }
   }


   /**
    * Local helper class holding a member initialized by calling
    * the private constructor of the singleton.
    */
   private static final class ConfigurationCacheHolder
   {
      private static final ConfigurationCacheByPropertiesImpl INSTANCE
         = new ConfigurationCacheByPropertiesImpl();
   }
}
