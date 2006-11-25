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
package org.jcoderz.commons;

import javax.ejb.SessionBean;

/**
 * Session bean for running JUnit tests inside a server.
 * Don't call this yourself, but implement a subclasses of
 * {@link ServerTestCase} which will use this to run
 * itself inside the server.
 *
 * @ejb.bean name="CommonsTestRunnerSession"
 *    type="Stateless"
 *    jndi-name="CommonsTestRunnerSession"
 *    remote-business-interface="org.jcoderz.commons.CommonsTestRunnerSessionInterface"
 * @ejb.interface extends="javax.ejb.EJBObject"
 * @ejb.transaction type="Supports"
 *
 * // JDBC
 * @ejb.resource-ref res-ref-name="jdbc/fawkez"
 *    res-type="javax.sql.DataSource"
 *    res-auth="Container"
 *    jndi-name="jdbc/fawkez"
 *
 * // FS Connector
 * @ejb.resource-ref res-auth="Container"
 *    res-name="eis/FileSystemConnector"
 *    res-type="org.jcoderz.commons.connector.file.FsConnectionFactory"
 * @weblogic.resource-description res-ref-name="eis/FileSystemConnector"
 *    jndi-name="eis/FileSystemConnector"
 *
 * // HTTP Connector
 * @ejb.resource-ref res-auth="Container"
 *    res-name="eis/HttpConnector"
 *    res-type="org.jcoderz.commons.connector.Http.HttpConnectionFactory"
 * @weblogic.resource-description res-ref-name="eis/HttpConnector"
 *    jndi-name="eis/HttpConnector"
 *
 * @ejb.ejb-ref ejb-name="CommonsTestRunnerSession"
 *    ref-name="ejb/CommonsTestRunnerSession"
 * 
 * TODO If you want to test your beans using the TestRunner, you'll have
 * to add your ejb-refs here.
 *
 * @author Michael Griffel
 */
public abstract class CommonsTestRunnerSessionBean
      extends CommonsTestRunnerSessionImpl
      implements SessionBean
{
   // nothing to do here
}
