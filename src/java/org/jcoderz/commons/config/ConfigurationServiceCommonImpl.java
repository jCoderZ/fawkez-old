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
import org.jcoderz.commons.util.Constants;


/**
 * Implementation of the common ConfigurationService business methods.
 *
 * This class holds all business logic of the service.
 * Implementing the business interface ConfigurationServiceCommonInterface.
 *
 */
public class ConfigurationServiceCommonImpl
      implements ConfigurationServiceCommonInterface
{
   /** class name for use in logging. */
   private static final String CLASSNAME 
           = ConfigurationServiceCommonImpl.class.getName();

   /** class logger. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   /**
    * Name of a parameter in a few get.. methods.
    * This member exists only for checkstyles's sake.
    */
   private static final transient
      String PARAM_KEY_OF_GET_METHODS = "key";


   /**
    * {@inheritDoc}
    * FIXME: activate ejb tag if XDoclet BugFix is available
    * @XXXejb.interface-method view-type="remote"
    * @XXXejb.transaction type="Required"
    */
   public boolean getBoolean (ConfigurationKey key)
         throws ConfigurationValueNotFoundException,
               ConfigurationTypeConversionFailedException,
               ArgumentMalformedException
   {
      Assert.notNull(key, PARAM_KEY_OF_GET_METHODS);
      boolean bReturn;

      final String valueAsString = getString(key);
      final Boolean value = Boolean.valueOf(valueAsString);
      if (value.booleanValue())
      {
         bReturn = true;
      }
      else if (valueAsString.toUpperCase(Constants.SYSTEM_LOCALE)
            .equals("FALSE"))
      {
         bReturn = false;
      }
      else
      {
         throw new ConfigurationTypeConversionFailedException(
                  valueAsString, key, "Boolean");
      }
      return bReturn;
   }


   /**
    * {@inheritDoc}
    * FIXME: activate ejb tag if XDoclet BugFix is available
    * @XXXejb.interface-method view-type="remote"
    * @XXXejb.transaction type="Required"
    */
   public int getInt (ConfigurationKey key)
         throws ConfigurationValueNotFoundException,
               ConfigurationTypeConversionFailedException,
               ArgumentMalformedException
   {
      Assert.notNull(key, PARAM_KEY_OF_GET_METHODS);
      int iReturn;

      final String valueAsString = getString(key);
      try
      {
         iReturn = Integer.parseInt(valueAsString);
      }
      catch (NumberFormatException nfe)
      {
         throw new ConfigurationTypeConversionFailedException(
                  valueAsString, key, "Integer", nfe);
      }
      return iReturn;
   }


   /**
    * {@inheritDoc}
    * FIXME: activate ejb tag if XDoclet BugFix is available
    * @XXXejb.interface-method view-type="remote"
    * @XXXejb.transaction type="Required"
    */
   public long getLong (ConfigurationKey key)
         throws ConfigurationValueNotFoundException,
               ConfigurationTypeConversionFailedException,
               ArgumentMalformedException
   {
      Assert.notNull(key, PARAM_KEY_OF_GET_METHODS);
      long lReturn;

      final String valueAsString = getString(key);
      try
      {
         lReturn = Long.parseLong(valueAsString);
      }
      catch (NumberFormatException nfe)
      {
         throw new ConfigurationTypeConversionFailedException(
                  valueAsString, key, "Long", nfe);
      }
      return lReturn;
   }


   /**
    * {@inheritDoc}
    * FIXME: activate ejb tag if XDoclet BugFix is available
    * @XXXejb.interface-method view-type="remote"
    * @XXXejb.transaction type="Required"
    */
   public String getString (ConfigurationKey key)
         throws ConfigurationValueNotFoundException,
                ArgumentMalformedException
   {
      Assert.notNull(key, PARAM_KEY_OF_GET_METHODS);
      return getConfigurationCacheCurrent().getString(key.toString());
   }

   protected ConfigurationCacheInterface getConfigurationCacheCurrent ()
   {
      // for database
      return ConfigurationCacheByDbReadOnlyImpl.current();
      // for properties file
      //return ConfigurationCacheByPropertiesImpl.current();
   }

}
