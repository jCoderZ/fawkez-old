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
package org.jcoderz.commons.taskdefs;

import javax.xml.transform.Transformer;

import org.apache.tools.ant.BuildException;


/**
 * Ant task that generates classes out of the log message info XML document.
 * <p>
 * Documentation of this Ant task can be found in
 * {@link org.jcoderz.commons.taskdefs}.
 *
 * @author Michael Griffel
 */
public final class LogMessageGenerator
      extends XsltBasedTask
{
   /** The default stylesheet name. */
   private static final String DEFAULT_STYLESHEET
         = "generate-log-message-info.xsl";

   /** The application name. */
   private String mApplication = null;

   /**
    * Sets the application (short) name. This parameter is required.
    * @param s The application (short) name.
    */
   public void setApplication (String s)
   {
      mApplication = s;
   }

   /**
    * @see org.jcoderz.commons.taskdefs.XsltBasedTask#getDefaultStyleSheet()
    */
   String getDefaultStyleSheet ()
   {
      return DEFAULT_STYLESHEET;
   }

   /**
    * @see org.jcoderz.commons.taskdefs.XsltBasedTask#setAdditionalTransformerParameters(javax.xml.transform.Transformer)
    */
   void setAdditionalTransformerParameters (Transformer transformer)
   {
      transformer.setParameter("application-short-name", mApplication);
      transformer.setParameter("application-name", mApplication);
   }

   /**
    * @see org.jcoderz.commons.taskdefs.XsltBasedTask#checkAttributes()
    */
   void checkAttributes ()
         throws BuildException
   {
      super.checkAttributes();

      if (mApplication == null)
      {
         throw new BuildException(
               "Missing mandatory attribute 'application'.", getLocation());
      }

   }
}
