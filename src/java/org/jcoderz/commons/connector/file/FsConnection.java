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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import javax.resource.ResourceException;

/**
 * The File System Connection Interface.
 *
 */
public interface FsConnection
{
   /**
    * Initiates close of the connection handle at the application level.
    * A connection client is required to call this method after the
    * connection is no more in use.
    *
    * @throws ResourceException thrown if close on a connection handle fails.
    * Any invalid connection close invocation should also throw this exception.
    */
   void close ()
         throws ResourceException;

   /**
    * Tests whether the givent file or directory <code>file</code> exists.
    *
    * @param file The file or directory to be checked.
    * @return True if and only the givent file or directory <code>file</code>
    *    exists; false otherwise.
    * @throws ResourceException The Security method denies read access to
    *    the file or directory <code>file</code>.
    */
   boolean isExists (String file)
         throws ResourceException;

   /**
    * Deletes the supplied file or directory <code>file</code>.
    *
    * @param file The file or directory to be deleted.
    *
    * @return if and only if the file or directory is successfully deleted;
    *    false otherwise
    *
    * @throws ResourceException If a security manager does not allow the file to
    *    be deleted.
    */
   boolean deleteFile (String file)
         throws ResourceException;

   /**
    * Renames the supplied file <code>from</code> to <code>to</code>.
    *
    * @param from The file to be renamed.
    * @param to The new file name.
    *
    * @throws ResourceException Thrown to indicate that the file
    *    <code>from</code> could not be deleted. For example: The Security
    *    method denies write access to the file or directory <code>to</code>.
    */
   void renameFile (String from, String to)
         throws ResourceException;

   /**
    * Moves the file <code>src</code> to <code>dest</code>.
    *
    * @param src The file to be moved from.
    * @param dest The file to be moved to.
    *
    * @throws ResourceException Thrown to indicate that the file
    *    <code>src</code> could not be moved to the <code>dest</code>.
    *    For example: The Security method denies write access to the file
    *    or directory <code>dest</code>.
    */
   void moveFile (String src, String dest)
         throws ResourceException;

   /**
    * Returns an array of strings naming the files and directories in the
    * directory <code>dir</code>.
    *
    * @see java.io.File#list()
    *
    * @param dir The directory whose file's list has be retrieved.
    *
    * @return  If the given path <code>dir</code> does not exist or does not
    * denote a directory, then this method returns null. Otherwise returns the
    * list of files in the directory <code>dir</code>.
    *
    * @throws ResourceException Thrown in error cases.
    *
    */
   String [] listFiles (String dir)
         throws ResourceException;

   /**
    * Returns a random access file.
    *
    * @param file The system-dependent filename
    * @param mode The access mode
    *
    * @return RandomAccessFile instance.
    *
    * @throws ResourceException thrown in error cases.
    * @throws FileNotFoundException if the file exists but is a directory rather
    *       than a regular file, or cannot be opened or created for any other
    *       reason.
    */
   RandomAccessFile getRandomAccessFile (String file, String mode)
         throws ResourceException, FileNotFoundException;

   /**
    * Returns a FileInputStream instance.
    *
    * @param file The system-dependent filename
    *
    * @return FileInputStream instance.
    *
    * @throws ResourceException thrown in error cases.
    * @throws FileNotFoundException If the file does not exist, is a directory
    * rather than a regular file, or for some other reason cannot be opened
    * for reading.
    */
   FileInputStream getFileInputStream (String file)
         throws ResourceException, FileNotFoundException;


   /**
    * Creates an empty file in the default temporary-file directory.
    *
    * @return The name of the created file.
    *
    * @throws ResourceException If a security manager does not allow a file to
    * be created or a file could not be created for some other reasons.
    */
   String createTempFile ()
         throws ResourceException;

   /**
    * Creates an empty file in the supplied <code>dir</code> directory.
    *
    * @param dir The parent directory for the file to be created.
    *
    * @return The name of the created file.
    *
    * @throws ResourceException If the supplied <code>dir</code> directory does
    *    not exist, If a security manager does not allow a file to
    *    be created or a file could not be created for some other reasons.
    */
   String createTempFile (String dir)
         throws ResourceException;

   /**
    * Creates a new, empty file named by the given parameter <code>file</code>
    * if and only if a file with this name does not yet exist.
    * Any required parent folders will be created if they do not exist yet.
    *
    * @param file The name of the file to be created.
    *
    * @return true If the file <code>file</code> does not exist and was
    *    successfully created; false if the named file already exists.
    *
    * @throws ResourceException If a security manager does not allow a file to
    *    be created or a file could not be created for some other reasons.
    */
   boolean createFile (String file)
         throws ResourceException;

   /**
    * Renames the as parameter passed file <code>file</code> to a temp file in
    * the default temporary-file directory.
    *
    * @param file The name of the file to be reanmed.
    *
    * @return The name of the temp file.
    *
    * @throws ResourceException If a security manager does not allow the file to
    *    be renamed or a temp file to be created; or if a file could not be
    *    renamed for some other reasons.
    */
   String renameToTempFile (String file)
         throws ResourceException;
}
