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
package org.jcoderz.phoenix.javadoc;

import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

/**
 * @author Michael Rumpf
 */
public class JcoderzTaglet
   implements Taglet
{
   private static final String[] TAGS = new String[]
      {"ejb.interface-method",
       "ejb.create-method",
       "ejb.data-object",
       "ejb.ejb-ref",
       "ejb.ejb-external-ref",
       "ejb.bean",
       "ejb.create-method",
       "ejb.data-object",
       "ejb.ejb-ref",
       "ejb.finder",
       "ejb.interface",
       "ejb.interface-method",
       "ejb.permission",
       "ejb.persistent-field",
       "ejb.pk",
       "ejb.pk-field",
       "ejb.transaction",
       "ejb.persistent-field",
       "ejb.security-role-ref",
       "ejb.security-roles",
       "ejb.dao",
       "ejb.resource-ref",
       "ejb.persistence",
       "jcoderz.admintool.sampleOutput",
       "jcoderz.admintool.example",
       "jcoderz.admintool.optional_param",
       "jcoderz.concurrency-control",
       "jcoderz.dbms-column",
       "jcoderz.dbms-table",
       "jcoderz.ds-jndi-name",
       "jcoderz.finder-method",
       "jsp.attribute",
       "jsp.tag",
       "web.ejb-ref",
       "web.servlet",
       "web.servlet-mapping",
       "web.servlet-init-param",
       "web.env-entry",
       "web.security-role",
       "weblogic.data-source-name",
       "weblogic.enable-call-by-reference",
       "weblogic.persistence",
       "weblogic.cache",
       "weblogic.delay-database-insert-until",
       "weblogic.automatic-key-generation",
       "weblogic.transaction",
       "weblogic.resource-description",
       "weblogic.transaction-descriptor",
       "weblogic.dbms-column-type",
       "@annotation"};

   /**
    * Register this Taglet.
    *
    * @param tagletMap the map to register this tag to.
    */
   public static void register (Map tagletMap)
   {
     final JcoderzTaglet tag = new JcoderzTaglet();

     // loop through the tags
     for (int i = 0; i < TAGS.length; i++)
     {
        final Taglet t = (Taglet) tagletMap.get(TAGS[i]);
        if (t != null)
        {
           tagletMap.remove(TAGS[i]);
        }
        tagletMap.put(TAGS[i], tag);
      }
   }

   /**
    * The name of the taglet.
    * @return The name of the taglet.
    */
   public String getName ()
   {
      return "jCoderZ Javadoc 1.4.x Fix";
   }

   /**
    * Will return true since xdoclet
    * can be used in field documentation.
    *
    * @return true since xdoclet tags
    * can be used in field documentation and false
    * otherwise.
    */
   public boolean inField ()
   {
      return true;
   }

   /**
    * Will return true since xdoclet tag
    * can be used in constructor documentation.
    *
    * @return true since xdoclet tag
    * can be used in constructor documentation and false
    * otherwise.
    */
   public boolean inConstructor ()
   {
      return true;
   }

   /**
    * Will return true since xdoclet tag
    * can be used in method documentation.
    *
    * @return true since xdoclet tag
    * can be used in method documentation and false
    * otherwise.
    */
   public boolean inMethod ()
   {
      return true;
   }

   /**
    * Will return true since xdoclet tag
    * can be used in method documentation.
    *
    * @return true since xdoclet tag
    * can be used in overview documentation and false
    * otherwise.
    */
   public boolean inOverview ()
   {
      return true;
   }

   /**
    * Will return true since xdoclet tag
    * can be used in package documentation.
    *
    * @return true since xdoclet tag
    * can be used in package documentation and false
    * otherwise.
    */
   public boolean inPackage ()
   {
       return true;
   }

   /**
    * Will return true since xdoclet tag
    * can be used in type documentation (classes or interfaces).
    *
    * @return true since xdoclet tag
    * can be used in type documentation and false
    * otherwise.
    */
   public boolean inType ()
   {
       return true;
   }

   /**
    * Will return false since xdoclet tags are not
    * inline tags.
    *
    * @return false since xdoclet tags
    * are not inline tags.
    */
   public boolean isInlineTag ()
   {
       return false;
   }

   /**
    * Return empty string as the tags should not be shown in Javadoc.
    * @param tag the doclet tag encountered
    * @return empty string.
    */
   public String toString (Tag tag)
   {
      return "";
   }

   /**
    * Return empty string as the tags should not be shown in Javadoc.
    * @param tags the doclet tags encountered
    * @return empty string.
    */
    public String toString (Tag[] tags)
    {
        return "";
    }
}

