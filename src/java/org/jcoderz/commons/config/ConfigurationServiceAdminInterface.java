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

import java.rmi.RemoteException;
import java.util.Set;

//import org.jcoderz.commons.ArgumentMalformedException;

/**
 * ConfigurationService's Administration interface.
 *
 * This class holds all business logic of the service related to administrative
 * tasks like getting all configuration keys or single configuration values.
 *
 * For future releases the modification or update mechanisms to manipulate
 * configuration data will be extended here.
 *
 */
public interface ConfigurationServiceAdminInterface
      extends ConfigurationServiceCommonInterface
{
   /**
    * Returns an immutable Set containing all keys present in the
    * configuration. The keys are <code>ConfigurationKey</code> objects.
    * In case there are no keys, the Set is empty.
    *
    * @return the Set of all existing <code>ConfigurationKey</code> objects.
    *       Or empty set if no data is stored.
    * @throws RemoteException if a remote call fails.
    * @throws ConfigurationInitializationFailedException if the requested
    *       configuration keys could not be fetched, or if a key could not be
    *       transformed to a valid ConfigurationKey instance.
    *       This could only occur, for general config resource problems,
    *       therefor the initialization failure is thrown instead of the
    *       underlying ArgumentMalformedException.
    */
   Set getKeys ()
         throws RemoteException, ConfigurationInitializationFailedException;

   // TODO: define setter methods
   /**
    * Stores the boolean value that is associated with the given key into
    * the configuration.
    *
    * @param key ConfigurationKey that is the key for a stored configuration
    *        value
    * @param value boolean value that represents a configuration parameter
    * @throws RemoteException if a remote call fails.
    * @throws ArgumentMalformedException Is thrown to indicate the illegal use
    *         of a null object as input parameter.
    *
   void setBoolean (ConfigurationKey key, boolean value)
         throws RemoteException, ArgumentMalformedException;
    */

   /**
    * Stores the int value that is associated with the given key into
    * the configuration.
    *
    * @param key ConfigurationKey that is the key for a stored configuration
    *        value
    * @param value int value that represents a configuration parameter
    * @return the previous "new value"
    * @throws RemoteException if a remote call fails.
    * @throws ArgumentMalformedException Is thrown to indicate the illegal use
    *         of a null object as input parameter.
    *
   void setInt (ConfigurationKey key, int value)
         throws RemoteException, ArgumentMalformedException;
    */

   /**
    * Stores the long value that is associated with the given key into
    * the configuration.
    *
    * @param key ConfigurationKey that is the key for a stored configuration
    *        value
    * @param value long value that represents a configuration parameter
    * @throws RemoteException if a remote call fails.
    * @throws ArgumentMalformedException Is thrown to indicate the illegal use
    *         of a null object as input parameter.
    *
   void setLong (ConfigurationKey key, long value)
         throws RemoteException, ArgumentMalformedException;
    */

   /**
    * Stores the String value that is associated with the given key into
    * the configuration. The String must not exceed 1000 characters in length.
    *
    * @param key ConfigurationKey that is the key for a stored configuration
    *        value
    * @param value String value that represent a configuration parameter
    * @throws RemoteException if a remote call fails.
    * @throws ConfigurationObjectSizeExceededException in case the parameter
    *         'value' exceeds 1000 in length.
    * @throws ArgumentMalformedException Is thrown to indicate the illegal use
    *         of a null object as input parameter.<br>
    *         It is not allowed to use null for parameter value.
    *
   void setString (ConfigurationKey key, String value)
         throws RemoteException, ConfigurationObjectSizeExceededException,
                ArgumentMalformedException;
    */

}
