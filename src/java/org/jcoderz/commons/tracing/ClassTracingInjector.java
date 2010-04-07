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

import org.jcoderz.commons.tracing.TracingInjector.Matcher;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Takes care to add Tracing to the methods.
 * @author mandelan
 */
public class ClassTracingInjector
{
  private static final int STATIC_BLOCK_LINE_NUMBER = 1;
  private static final String CLASSNAME = ClassTracingInjector.class.getName();
  private static final Logger logger = Logger.getLogger(CLASSNAME);
  private final ClassNode mClassNode;
  private final String mClassName;
  private boolean mStaticLoggerInserted = false;
  private MethodNode mStaticInit;
  private final boolean mJava5;
  private final boolean mPai;

  /**
   *
   * @param cn
   * @param java5
   * @param pai
   */
  public ClassTracingInjector(ClassNode cn, boolean java5, boolean pai)
  {
    mPai = pai;
    mJava5 = java5;
    mClassNode = cn;
    mClassName  = cn.name.replace('/', '.');
  }

  /**
   * Injects logging to all methods that match the given matcher.
   * @param matcher the matcher to identify methods to inject.
   */
  public void inject (Matcher matcher)
  {
    // No tracing for interfaces.
    if ((mClassNode.access & Opcodes.ACC_INTERFACE) == 0)
    {
      final Iterator i = mClassNode.methods.iterator();
      while (i.hasNext())
      {
        final MethodNode mn = (MethodNode) i.next();
        if (matcher.matches(mClassNode, mn))
        {
          if (logger.isLoggable(Level.FINEST))
          {
            logger.finest("Will inject tracing into: "
                + AsmUtil.toString(mClassNode, mn));
          }
          final MethodTracingInjector mi
            = new MethodTracingInjector(mn, this);
          mi.inject();
        }
        else
        {
          if (logger.isLoggable(Level.FINEST))
          {
            logger.finest("No match: "
                + AsmUtil.toString(mClassNode, mn));
          }
        }
        if ("<clinit>".equals(mn.name) && mStaticInit == null)
        {
          mStaticInit = mn;
        }
      }
      if (!mStaticLoggerInserted)
      {
        MethodNode mn;
        if (mStaticInit == null)
        {
          if (logger.isLoggable(Level.FINEST))
          {
            logger.finest("Creating new static initializer for class: "
                + AsmUtil.toString(mClassNode));
          }
          mn = new MethodNode(
            Opcodes.ACC_STATIC,  "<clinit>", "()V", null, null);
          mn.instructions = new InsnList();
          final LabelNode start = new LabelNode();
          mn.instructions.add(start);
          mn.instructions.add(new LineNumberNode(STATIC_BLOCK_LINE_NUMBER, start));
          mn.instructions.add(new InsnNode(Opcodes.RETURN));
          mClassNode.methods.add(mn);
        }
        else
        {
          mn = mStaticInit;
          if (logger.isLoggable(Level.FINEST))
          {
            logger.finest("Using existing static initializer: "
                + AsmUtil.toString(mClassNode, mn));
          }
        }
        final InsnList cmd = new InsnList();
        injectStaticLoggerMember(cmd);
        // get first none label node
        AbstractInsnNode first = mn.instructions.getFirst();
        while (first.getNext() != null
            && first.getType() == AbstractInsnNode.LABEL)
        {
          first = first.getNext();
        }
        mn.instructions.insertBefore(first, cmd);
        mStaticLoggerInserted = true;
      }
    }
    else
    { // No tracing for interfaces.
      if (logger.isLoggable(Level.FINEST))
      {
        logger.finest("No tracing in interfaces! "
            + AsmUtil.toString(mClassNode));
      }

    }
  }

  public ClassNode getClassNode ()
  {
    return mClassNode;
  }

  public String getClassName ()
  {
    return mClassName;
  }

  /**
   *
   * @param cmd
   */
  public void getStaticLoggerOnStack (InsnList cmd)
  {
    cmd.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        mClassNode.name,
        TracingInjector.STATIC_LOGGER_FIELD_NAME,
        "Ljava/util/logging/Logger;"));
  }

  /**
   * Load the class name reference to the stack.
   * @param cmd the instruction list where to add the command to.
   */
  public void getClassNameOnStack(InsnList cmd)
  {
    cmd.add(new LdcInsnNode(mClassName));
  }

  /**
   * Injects the static logger member and takes care for the
   * static initialization.
   * @param cmd the command list to add the code to.
   */
  public void injectStaticLoggerMember (InsnList cmd)
  {
    if (!mStaticLoggerInserted)
    {
      final FieldNode staticLogger
        = new FieldNode(
            Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL
          + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC,
          TracingInjector.STATIC_LOGGER_FIELD_NAME,
          "Ljava/util/logging/Logger;",
          null,
          null);
      mClassNode.fields.add(staticLogger);
      getClassNameOnStack(cmd);
      cmd.add(new MethodInsnNode(
          Opcodes.INVOKESTATIC,
          "java/util/logging/Logger",
          "getLogger",
          "(Ljava/lang/String;)Ljava/util/logging/Logger;"));
      cmd.add(new FieldInsnNode(
          Opcodes.PUTSTATIC,
          mClassNode.name,
          TracingInjector.STATIC_LOGGER_FIELD_NAME,
          "Ljava/util/logging/Logger;"));
      mStaticLoggerInserted = true;
    }
  }

  public boolean isJava5()
  {
    return mJava5;
  }

  public boolean isPai()
  {
    return mPai;
  }
}
