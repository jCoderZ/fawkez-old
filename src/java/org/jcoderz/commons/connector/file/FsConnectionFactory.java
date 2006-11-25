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
package org.jcoderz.commons.connector.file;

import java.io.Serializable;
import java.util.Properties;

import javax.resource.Referenceable;
import javax.resource.ResourceException;

/**
 * This factory provides an interface to get a connection to the File System.
 * The File System Connector does not provide the
 * {@link javax.resource.cci} interface. A connector's client should use
 * this factory to get a connection to the File System instead.
 *
 */
public interface FsConnectionFactory
      extends Serializable, Referenceable
{
   /** Default value for chunk size, 20 MByte. */
   static final long FILE_TRANSFER_CHUNK_SIZE_DEF_VALUE = 20000000L;

   /** Property temp dir. */
   String PROP_TEMP_DIR = "TempDir";

   /** Property to define the chunk size used while file transfering. */
   String PROP_FILE_TRANSFER_CHUNK_SIZE = "FileTransferChunkSize";

   /**
    * Returns a connection to the File System.
    * @return a connection to the File System.
    * @throws ResourceException Failed to get a connection.
    */
   FsConnection getConnection ()
         throws ResourceException;
   /**
    * Returns a connection to the File System.
    * @param props The properties to use.
    * @return a connection to the File System.
    * @throws ResourceException Failed to get a connection.
    */
   FsConnection getConnection (Properties props)
         throws ResourceException;
}
