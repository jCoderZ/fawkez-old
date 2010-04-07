/*
 * $Id: ArraysUtil.java 1011 2008-06-16 17:57:36Z amandel $
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
package org.jcoderz.commons.tracing;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.util.FileUtils;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.commons.util.StringUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * TODO: Create unit tests!
 * TODO: Might add 'this' to &lt;init> exiting logger
 * TODO: Store {@link System#currentTimeMillis()} / nanos
 *  to compute time!
 * TODO: Create a method compressing the localVariableTable
 *       + create localVariableTable if none is given! (seen with
 *       aspectj generated classes)
 * TODO: Instead of boolean indicating if loggable store
 *       reference to logger or null in method variable
 *
 * @author mandelan
 */
public class TracingInjector
{
  static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
  static final String PREFIX = "LGI$";
  static final String LOCAL_PREFIX = "lgi$";
  static final String STATIC_LOGGER_FIELD_NAME
    = PREFIX + "LGR";
  static final String IS_LOGGABLE_FIELD_NAME
    = LOCAL_PREFIX + "isLoggable";
  static final String RESULT_VAR_FIELD_NAME
    = LOCAL_PREFIX + "result";

  private static final String CLASSNAME = TracingInjector.class.getName();
  private static final Logger logger = Logger.getLogger(CLASSNAME);

  /**
   *
   * @author Andreas Mandel
   */
  public interface Matcher
  {
    /**
     *
     * @param cn
     * @return
     */
    public boolean matches (ClassNode cn);

    /**
     *
     * @param mn
     * @return
     */
    public boolean matches (MethodNode mn);

    /**
     *
     * @param cn
     * @param mn
     * @return
     */
    public boolean matches (ClassNode cn, MethodNode mn);
  }

  /**
   *
   * @author Andreas Mandel
   */
  public static class MethodMatcher implements Matcher
  {
    public final static String COMMENT = "#";
    public final static String COMMENT2 = "//";
    final List mPatterString = new ArrayList();
    final List mPatterns = new ArrayList();

    /**
     * Method matcher that is configured by the given File.
     * 
     * @param inFile
     * @throws IOException
     */
    public MethodMatcher (File inFile)
      throws IOException
    {
      LineNumberReader reader = null;
      final FileReader fr = new FileReader(inFile);
      try
      {
        reader = new LineNumberReader(fr);
        String line = reader.readLine();
        while (line != null)
        {
          if (!StringUtil.isEmptyOrNull(line) && !line.startsWith(COMMENT)
              && !line.startsWith(COMMENT2))
          {
            mPatterString.add(line.trim());
            final AspectPattern aspectPattern = new AspectPattern(line.trim());
            mPatterns.add(aspectPattern);
          }
          line = reader.readLine();
        }
      }
      finally
      {
        IoUtil.close(reader);
        IoUtil.close(fr);
      }
    }

    /** {@inheritDoc} */
    public String toString ()
    {
      return mPatterns.toString();
    }

    /**
     * @param cn
     * @return
     */
    public boolean matches(ClassNode cn)
    {
      final Iterator i = mPatterns.iterator();
      boolean matched = false;
      while (i.hasNext() && !matched)
      {
        final AspectPattern pattern = (AspectPattern) i.next();
        matched |= pattern.matchClass(cn.name);
        // TODO: Check inheritance!!!
        // return matcher!!!
      }
      return matched;
    }

    /**
     * @param mn
     * @return
     */
    public boolean matches(MethodNode mn)
    {
      throw new UnsupportedOperationException("TODO");
    }

    /**
     * @param cn
     * @param mn
     * @return
     */
    public boolean matches(ClassNode cn, MethodNode mn)
    {
      final Iterator i = mPatterns.iterator();
      boolean matched = false;
      while (i.hasNext() && !matched)
      {
        final AspectPattern pattern = (AspectPattern) i.next();
        if (pattern.matchClass(cn.name))
          // TODO: Check inheritance!!!
        {
          matched |=
            pattern.matchAccess(mn.access)
            && pattern.matchMethod(mn.desc);
        }
      }
      return matched;
    }
  }

  /**
   *
   * @param inFile
   * @param outFile
   * @param matcher
   * @param java5
   * @param pai
   * @throws IOException
   */
  public static void inject (File inFile, File outFile, Matcher matcher,
      boolean java5, boolean pai)
    throws IOException
  {
    final ClassNode cn = new ClassNode();
    final FileInputStream is = new FileInputStream(inFile);
    try
    {
      final ClassReader cr = new ClassReader(is);
      cr.accept(cn, 0);
    }
    finally
    {
      IoUtil.close(is);
    }
    if (matcher.matches(cn))
    {
      if (logger.isLoggable(Level.FINE))
      {
        logger.fine("Will inject tracing into: " + cn.name);
      }
      new ClassTracingInjector(cn, java5, pai).inject(matcher);
      final FileOutputStream os = new FileOutputStream(outFile);
      try
      {
        final ClassWriter cw = new ClassWriter(
            /* ClassWriter.COMPUTE_FRAMES +*/ ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        os.write(cw.toByteArray());
      }
      finally
      {
        IoUtil.close(os);
      }
    }
    else
    {
      if (logger.isLoggable(Level.FINE))
      {
        logger.fine("Direct copy for: " + cn.name + " (no match)");
      }
      FileUtils.copy(inFile, outFile);
    }
  }
}
