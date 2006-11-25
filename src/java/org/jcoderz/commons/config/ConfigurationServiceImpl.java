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

import java.util.logging.Logger;

import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.util.Assert;


/**
 * Implementation of the ConfigurationService business methods.
 *
 * This class holds all business logic of the service to fetch specifc
 * configuration data.
 * Implementing the business interface ConfigurationServiceInterface.
 *
 * On this level only primitve types String, int, long and boolean are used.
 * The wellformed and complex typed interfaces for the specific services are
 * using this more simple interface.
 *
 * IMPORTANT:
 * Because of XDoclet Bug in current version, we define deligators to the super
 * class with ejb-tags here.
 * These should be deleted if XDoclet is fixed.
 *
 */
public class ConfigurationServiceImpl
      extends ConfigurationServiceCommonImpl
      implements ConfigurationServiceInterface
{
   /** class name for use in logging */
   private static final transient
      String CLASSNAME = ConfigurationServiceImpl.class.getName();

   //private static final transient String PACKAGE_NAME
   //      = ConfigurationServiceImpl.class.getPackage().getName();

   /**
    * class logger
    */
   private static final transient Logger logger = Logger.getLogger(CLASSNAME);

    /**
    * @see ConfigurationServiceInterface#addConfigurationListener(ConfigurationListener)
    *
    * @ejb.interface-method view-type="remote"
    * @ejb.transaction type="Required"
    */
   public void addConfigurationListener (ConfigurationListener listener)
         throws ArgumentMalformedException
   {
      Assert.notNull(listener, "listener");
      getConfigurationCacheCurrent().addConfigurationListener(listener);
   }


   /**
    * @see ConfigurationServiceInterface#getServiceConfiguration(String)
    */
   public ServiceConfiguration getServiceConfiguration (
         String classname)
         throws ArgumentMalformedException
   {
      Assert.notNull(classname, "classname");
      ServiceConfiguration config;
      try
      {
         config = (ServiceConfiguration) Class.forName(
               classname + "Impl").newInstance();
      }
      catch (Exception e)
      {
         throw new ConfigurationFactoryFailedException(classname, e);
      }
      return config;
   }

   // IMPORTANT:
   // Because of XDoclet Bug in current version, we define the deligators
   // here with ejb-tags.
   // Normally the implementation from CommonImpl is already inherited.
   // FIXME: This should be changed when XDoclet BugFix is available.

   /**
    * @see ConfigurationServiceCommonInterface#getBoolean(ConfigurationKey)
    *
    * @ejb.interface-method view-type="remote"
    * @ejb.transaction type="Required"
    */
   public boolean getBoolean (ConfigurationKey key)
         throws ConfigurationValueNotFoundException,
               ConfigurationTypeConversionFailedException,
               ArgumentMalformedException
   {
      return super.getBoolean(key);
   }


   /**
    * @see ConfigurationServiceCommonInterface#getInt(ConfigurationKey)
    *
    * @ejb.interface-method view-type="remote"
    * @ejb.transaction type="Required"
    */
   public int getInt (ConfigurationKey key)
         throws ConfigurationValueNotFoundException,
               ConfigurationTypeConversionFailedException,
               ArgumentMalformedException
   {
      return super.getInt(key);
   }


   /**
    * @see ConfigurationServiceCommonInterface#getLong(ConfigurationKey)
    *
    * @ejb.interface-method view-type="remote"
    * @ejb.transaction type="Required"
    */
   public long getLong (ConfigurationKey key)
         throws ConfigurationValueNotFoundException,
               ConfigurationTypeConversionFailedException,
               ArgumentMalformedException
   {
      return super.getLong(key);
   }


   /**
    * @see ConfigurationServiceCommonInterface#getString(ConfigurationKey)
    *
    * @ejb.interface-method view-type="remote"
    * @ejb.transaction type="Required"
    */
   public String getString (ConfigurationKey key)
         throws ConfigurationValueNotFoundException,
                ArgumentMalformedException
   {
      return super.getString(key);
   }
}
