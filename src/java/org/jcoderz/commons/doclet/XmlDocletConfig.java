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
package org.jcoderz.commons.doclet;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.util.ArraysUtil;
import org.jcoderz.commons.util.Assert;


/**
 * Value object for the XmlDoclet configuration values.
 *
 * @author Andreas Mandel
 */
public final class XmlDocletConfig
      implements Serializable, Cloneable
{
   static final int OPTION_NOT_KNOWN = 0;
   static final int ERROR_WITH_OPTION = -1;

   /** The full qualified name of this class. */
   private static final String CLASSNAME = XmlDocletConfig.class.getName();

   /** The logger to use. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   private static final long serialVersionUID = -202900277699915981L;

   private static final Map OPTION_MAP;

   static
   {
      final Map tempMap = new HashMap();

      final XmlDocletOption outputDirectory
            = new XmlDocletOption("-d", 2, "Output Directory")
            {
               public boolean validOption (XmlDocletConfig cfg, String[] args)
                     throws ArgumentMalformedException
               {
                  return cfg.parseOutputDirectory(args);
               }
            };
      tempMap.put(outputDirectory.getOption(), outputDirectory);
      final XmlDocletOption link
            = new XmlDocletOption("-link", 2, "Link reference")
            {
               public boolean validOption (XmlDocletConfig cfg, String[] args)
                     throws ArgumentMalformedException
               {
                  return cfg.parseLink(args);
               }
            };
      tempMap.put(link.getOption(), link);

      OPTION_MAP = Collections.unmodifiableMap(tempMap);
   }

   /** Output directory, to generate the output to. */
   private File mOutputDirectory;

   /** List of link urls. */
   private final List mLinkUrls = new ArrayList();


   /** {@inheritDoc} */
   public Object clone ()
         throws CloneNotSupportedException
   {
      return super.clone();
   }

   /**
    * Parses the given option with the arguments and updates this
    * option object accordingly.
    * @param optionValue the option to be set.
    * @param args the arguments that came with this option.
    * @throws ArgumentMalformedException if the option could not be
    *       parsed.
    * @see com.sun.javadoc.Doclet#validOptions(java.lang.String[][], com.sun.javadoc.DocErrorReporter)
    */
   public void parseOption (String optionValue, String[] args)
         throws ArgumentMalformedException
   {
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, "parseOption(String, String[])",
               new Object [] {optionValue, ArraysUtil.toString(args)});
      }
      final XmlDocletOption option
            = (XmlDocletOption) OPTION_MAP.get(optionValue);

      if (option == null)
      {
          logger.fine("Gracefully ignoring unknown option " + optionValue 
              + " with value " + ArraysUtil.toString(args) + ".");
      }
      else
      {
          if (args.length != (option.getNumberOfArguments() - 1))
          {
             throw new ArgumentMalformedException("number of arguments",
                   String.valueOf(args.length - 1), "The option '" + optionValue
                   + "' expects " + (option.getNumberOfArguments() - 1)
                   + " arguments.");
          }
          option.validOption(this, args);
      }
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, "parseOption(String, String[])");
      }
   }

   /**
    * Returns the option length for the given option. A value of 2
    * implies that the argument takes one option. The value 0
    * {@link #OPTION_NOT_KNOWN} means that the option is unknown and a
    * value of -1 {@link #ERROR_WITH_OPTION} denotes an error.
    * @param optionValue the option to be checked.
    * @return the option length for the given option.
    * @see com.sun.javadoc.Doclet#optionLength(java.lang.String)
    */
   public int optionLength (String optionValue)
   {
      final XmlDocletOption option
            = (XmlDocletOption) OPTION_MAP.get(optionValue);

      final int result;
      if (option == null)
      {
         result = OPTION_NOT_KNOWN;
      }
      else
      {
         result = option.getNumberOfArguments();
      }
      return result;
   }

   /**
    * Returns the output directory set.
    * @return the output directory set.
    */
   public File getOutputDirectory ()
   {
      return mOutputDirectory;
   }

   private boolean parseLink (String[] args)
   {
      URL url;
      try
      {
         url = new URL(args[0]);
         mLinkUrls.add(url);
      }
      catch (MalformedURLException ex)
      {
         throw new ArgumentMalformedException("link", args[0], ex.getMessage(),
               ex);
      }

      // TODO Auto-generated method stub
      return false;
   }


   private boolean parseOutputDirectory (String[] args)
         throws ArgumentMalformedException
   {
      Assert.notNull(args[0], "d");
      mOutputDirectory = new File(args[0]);
      if (!mOutputDirectory.isDirectory())
      {
         throw new ArgumentMalformedException("outputDirectory", args[0],
               "Output directory must be set to a directory.");
      }
      return true;
   }


   private abstract static class XmlDocletOption
   {
      private final String mOption;
      private final int mNumberOfArguments;
      private final String mShortDescription;

      XmlDocletOption (String option, int numberOfArguments, String description)
      {
         mOption = option;
         mNumberOfArguments = numberOfArguments;
         mShortDescription = description;
      }

      abstract boolean validOption (XmlDocletConfig cfg, String [] args)
            throws ArgumentMalformedException;

      int getNumberOfArguments ()
      {
         return mNumberOfArguments;
      }

      String getOption ()
      {
         return mOption;
      }

      String getShortDescription ()
      {
         return mShortDescription;
      }
   }
}
