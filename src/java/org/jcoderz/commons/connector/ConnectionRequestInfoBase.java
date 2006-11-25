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
package org.jcoderz.commons.connector;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * The base class for all ConnectionRequestInfo implementations.
 *
 */
public abstract class ConnectionRequestInfoBase
      implements ConnectionRequestInfo
{
   private UserPassword mUp = UserPassword.EMPTY_USER_PASSWORD;

   /**
    * Sets user name and password for this request info.
    *
    * @param userName User Name to be set.
    * @param password Passwort to be set.
    */
   public void setUserPassword (String userName, String password)
   {
      mUp = UserPassword.fromUserPassword(userName, password);
   }

   /**
    * Sets the UserPassword <code>up</code> for this request info.
    *
    * @param up the UserPassword to be set.
    */
   public void setUserPassword (UserPassword up)
   {
      mUp = UserPassword.fromUserPassword(up);
   }

   /**
    * Returns the UserPassword of this request info.
    * @return the UserPassword of this request info.
    */
   public UserPassword getUserPassword ()
   {
      return mUp;
   }
}
