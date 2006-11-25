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
package org.jcoderz.commons.connector.http;

import java.rmi.RemoteException;

import org.jcoderz.commons.RemoteCallFailureException;
import org.jcoderz.commons.connector.ConnectorConfiguration;
import org.jcoderz.commons.config.ConfigurationServiceContainerFactory;
import org.jcoderz.commons.config.ConfigurationServiceInterface;


/**
 * Factory class to obtain a connector configuration.
 *
 */
public final class ConfigurationFactory
{
   private ConfigurationFactory ()
   {
      // not accessible
   }

   /**
    * Gets the connector configuration obtained from config service.
    *
    * @return ConnectorConfiguration providing getter methods for all
    *          connector parameter values defined
    */
   public static ConnectorConfiguration getConfiguration ()
   {
      final ConnectorConfiguration result;
      final ConfigurationServiceInterface configService
            = ConfigurationServiceContainerFactory.createLocalService();
      try
      {
         result = (ConnectorConfiguration)
               configService.getServiceConfiguration(
            "org.jcoderz.commons.connector.ConnectorConfiguration");
      }
      catch (RemoteException e)
      {
         throw new RemoteCallFailureException(e);
      }
      return result;
   }
}
