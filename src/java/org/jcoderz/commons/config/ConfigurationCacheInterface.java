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

import java.util.Set;

import org.jcoderz.commons.ArgumentMalformedException;


/**
 * ConfigurationCache Interface.
 * This level is introduced to allow different cache implementations and
 * backends.
 * These are needed to reflect both development phases and newer technical
 * approaches.
 *
 */
public interface ConfigurationCacheInterface
{

   /**
    * Returns the String value that is associated with the given key.
    *
    * @param key Configuration Key as string that is the key for a stored
    *         configuration value.
    * @return the String value that is associated with the given key.
    * @throws ConfigurationValueNotFoundException in case there is no match to
    *         the key.
    * @throws ArgumentMalformedException Is thrown to indicate the illegal use
    *         of a null object as input parameter.
    */
   String getString (String key)
         throws ConfigurationValueNotFoundException,
                ArgumentMalformedException;


   /**
    * Returns an immutable Set containing all keys present in the
    * configuration. The keys are <code>ConfigurationKey</code> objects.
    * In case there are no keys, the Set is empty.
    *
    * @return the Set of all existing <code>ConfigurationKey</code> objects.
    */
   Set getKeys ();

   /** {@inheritDoc} */
   void addConfigurationListener (ConfigurationListener newListener);

   /**
    * Reloads the internal configuration cache from resource bundle.
    * If an exception occurs during the reload, the old cached properties are
    * returned.
    */
   void reloadCache ();
}
