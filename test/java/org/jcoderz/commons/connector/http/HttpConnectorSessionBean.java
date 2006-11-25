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

import javax.ejb.SessionBean;

/**
 * Session bean calling the http connector.
 * Used for client sided testcases that starts a simple
 * http server in a separate thread.
 *
 * @ejb.bean name="HttpConnectorSession"
 *    type="Stateless"
 *    jndi-name="HttpConnectorSession"
 *    remote-business-interface="org.jcoderz.commons.connector.http.HttpConnectorSessionInterface"
 * @ejb.interface extends="javax.ejb.EJBObject"
 * @ejb.transaction type="Supports"
 *
 *
 * @ejb.resource-ref res-auth="Container"
 *    res-name="eis/HttpConnector"
 *    res-type="org.jcoderz.commons.connector.Http.HttpConnectionFactory"
 *
 * @weblogic.resource-description res-ref-name="eis/HttpConnector"
 *    jndi-name="eis/HttpConnector"
 *
 *
 * @weblogic.enable-call-by-reference True
 *
 */
public abstract class HttpConnectorSessionBean
      extends HttpConnectorSessionImpl
      implements SessionBean
{
   // nothing to do here
}
