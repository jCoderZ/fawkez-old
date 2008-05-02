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

import org.jcoderz.commons.ArgumentMalformedException;

/**
 * Common ConfigurationService's business interface with
 * getter method to get configuration parameters.
 *
 * On this level only primitve types String, int, long and boolean are used.
 * The wellformed and complex typed interfaces for the specific services are
 * using this more simple interface.
 *
 */
public interface ConfigurationServiceCommonInterface
{
   /**
    * Returns the boolean value that is associated with the given key.
    *
    * @param key ConfigurationKey that is the key for a stored configuration
    *        value.
    * @return the boolean value that is associated with the given key.
    * @throws RemoteException if a remote call fails.
    * @throws ConfigurationValueNotFoundException in case there is no match to
    *         key.
    * @throws ConfigurationTypeConversionFailedException in case the found value
    *         could not be converted to boolean type.
    * @throws ArgumentMalformedException Is thrown to indicate the illegal use
    *         of a null object as input parameter.
    */
   boolean getBoolean (ConfigurationKey key)
         throws RemoteException, ConfigurationValueNotFoundException,
               ConfigurationTypeConversionFailedException,
               ArgumentMalformedException;


   /**
    * Returns the int value that is associated with the given key.
    *
    * @param key ConfigurationKey that is the key for a stored configuration
    *        value.
    * @return the int value that is associated with the given key.
    * @throws RemoteException if a remote call fails.
    * @throws ConfigurationValueNotFoundException in case there is no match to
    *         the key.
    * @throws ConfigurationTypeConversionFailedException in case the found value
    *         could not be converted to int type.
    * @throws ArgumentMalformedException Is thrown to indicate the illegal use
    *         of a null object as input parameter.
    * @throws NumberFormatException in case the stored value cannot be
    *         converted into an int
    */
   int getInt (ConfigurationKey key)
         throws RemoteException, ConfigurationValueNotFoundException,
               ConfigurationTypeConversionFailedException,
               ArgumentMalformedException, NumberFormatException;


   /**
    * Returns the long value that is associated with the given key.
    *
    * @param key ConfigurationKey that is the key for a stored configuration
    *        value.
    * @return the long value that is associated with the given key.
    * @throws RemoteException if a remote call fails.
    * @throws ConfigurationValueNotFoundException in case there is no match to
    *         the key.
    * @throws ConfigurationTypeConversionFailedException in case the found value
    *         could not be converted to long type.
    * @throws ArgumentMalformedException Is thrown to indicate the illegal use
    *         of a null object as input parameter.
    */
   long getLong (ConfigurationKey key)
         throws RemoteException, ConfigurationValueNotFoundException,
               ConfigurationTypeConversionFailedException,
               ArgumentMalformedException;


   /**
    * Returns the String value that is associated with the given key.
    *
    * @param key ConfigurationKey that is the key for a stored configuration
    *        value.
    * @return the String value that is associated with the given key.
    * @throws RemoteException if a remote call fails.
    * @throws ConfigurationValueNotFoundException in case there is no match to
    *         the key.
    * @throws ArgumentMalformedException Is thrown to indicate the illegal use
    *         of a null object as input parameter.
    */
   String getString (ConfigurationKey key)
         throws RemoteException, ConfigurationValueNotFoundException,
                ArgumentMalformedException;

}
