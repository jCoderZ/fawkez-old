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

import java.io.File;

import javax.xml.transform.Transformer;

import org.apache.tools.ant.BuildException;

/**
 * Ant task that generates the configuration initialization files
 * out of an XML document.
 * <p>
 * Documentation of this Ant task can be found in
 * {@link org.jcoderz.commons.taskdefs}.
 *
 * @author Michael Griffel
 */
public final class ConfigurationInitFileGenerator
      extends XsltBasedTask
{
   /** The default stylesheet name. */
   private static final String DEFAULT_STYLESHEET
         = "generate-config-init-file.xsl";

   /** The application name. */
   private String mApplication = null;
   /** The group short names. */
   private String mGroups = null;
   /** The mode. */
   private String mMode = "PROPERTIES";

   /**
    * Sets the application (short) name. This parameter is required.
    * @param s The application (short) name.
    */
   public void setApplication (String s)
   {
      mApplication = s;
   }

   /**
    * Sets the group short names. This parameter is required.
    * @param s The group short names.
    */
   public void setGroups (String s)
   {
      mGroups = s;
   }

   /**
    * Sets the mode. This parameter is optional.
    * @param s The group short names.
    */
   public void setMode (String s)
   {
      mMode = s.toUpperCase();
   }

   /** {@inheritDoc} */
   String getDefaultStyleSheet ()
   {
      return DEFAULT_STYLESHEET;
   }

   /** {@inheritDoc} */
   void setAdditionalTransformerParameters (Transformer transformer)
   {
      transformer.setParameter("application-short-name", mApplication);
      transformer.setParameter("application-name", mApplication);
      transformer.setParameter("group-short-name", mGroups);
      transformer.setParameter("mode", mMode);
   }

   /** {@inheritDoc} */
   void checkAttributes ()
         throws BuildException
   {
      // fake destDir
      setDestdir(new File("."));

      super.checkAttributes();

      if (mApplication == null)
      {
         throw new BuildException(
               "Missing mandatory attribute 'application'.", getLocation());
      }
      if (mGroups == null)
      {
         throw new BuildException(
               "Missing mandatory attribute 'groups'.", getLocation());
      }

   }

}
