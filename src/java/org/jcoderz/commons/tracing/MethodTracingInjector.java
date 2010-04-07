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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.util.Assert;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Takes care to add Tracing to the methods.
 * @author mandelan
 */
public class MethodTracingInjector
{
  private static final String CLASSNAME
    = MethodTracingInjector.class.getName();
  private static final Logger logger
    = Logger.getLogger(CLASSNAME);
  private static final String JAVA_LOGGER = "java/util/logging/Logger";

  /** Link to the class injector where this method belongs to. */
  private final ClassTracingInjector mClassInjector;
  /** The asm MethodNode of the method under injection. */
  private final MethodNode mMethodNode;
  /** Method name as it is used in the logger calls. */
  private final String mMethodName;
  /** The local variable that is used to sore the is loggable boolean. */
  private LocalVariableNode mIsLoggableVar;
  /** Method start label - after logging injection */
  private LabelNode mMethodStart;
  /**
   * Method start label - before logging injection - after the
   * isLoggable assignment.
   */
  private LabelNode mOldMethodStart;
  /** Label at the very end of the method. */
  private LabelNode mMethodEnd;

  /**
   * Create a new MethodTracingInjector.
   * @param mn the method node where to inject the tracing logging.
   * @param ci the class injector where this method belongs to
   */
  public MethodTracingInjector(MethodNode mn, ClassTracingInjector ci)
  {
    mClassInjector = ci;
    mMethodNode = mn;
    mMethodName = mn.name;
  }

  /**
   * Do the job.
   */
  public void inject ()
  {
    // NO abstract nor native methods
    if ((mMethodNode.access
        & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0)
    {
      int lineNumber = getLineNumber();
      if (logger.isLoggable(Level.FINEST))
      {
        logger.finest("Injecting tracing into "
            + AsmUtil.toString(mMethodNode) + "at line " + lineNumber
            +  '.');
      }
      mMethodStart = new LabelNode();
      mOldMethodStart = getOrCreateStartLabel();
      mMethodEnd = getOrCreateEndLabel();


      final InsnList cmd = new InsnList();
      cmd.add(mMethodStart);
      if (lineNumber != -1)
      {
        cmd.add(new LineNumberNode(lineNumber - 1, mMethodStart));
      }
      if ("<clinit>".equals(mMethodNode.name))
      { // Refactor??
        mClassInjector.injectStaticLoggerMember(cmd);
      }

      ensureLocalVariablesAreSet();

      // Create boolean method var isLoggable....
      injectIsLoggableVariable(cmd);

      final LabelNode labelAfterEnteringLogging = isLoggable(cmd);
      injectEnteringLogging(cmd);
      cmd.add(labelAfterEnteringLogging);

      // TAKE CARE FOR LABEL!
      // Must be shifted down for tryCatchBlocks
      // MUST stay for Variables -> Move Variables!
      // fix var map:
      final Iterator i = mMethodNode.localVariables.iterator();
      while (i.hasNext())
      {
        final LocalVariableNode var = (LocalVariableNode) i.next();

        if (var.start.equals(mOldMethodStart))
        {
          var.start = mMethodStart;
        }
      }
      mMethodNode.instructions.insertBefore(mOldMethodStart, cmd);
      cmd.clear();

      final TryCatchBlockNode tryCatch
        = injectThrowingLogging(cmd,
          labelAfterEnteringLogging);
      if (tryCatch != null)
      {
        mMethodNode.tryCatchBlocks.add(tryCatch);
        mMethodNode.instructions.insertBefore(mMethodEnd, cmd);
      }
      cmd.clear();

      injectExitingLogger();
    }
    else
    {
      if (logger.isLoggable(Level.FINEST))
      {
        logger.finest("No tracing in native / abstract methods! "
            + AsmUtil.toString(mMethodNode));
      }
    }
  }

  private int getLineNumber()
  {
    int result = -1;
    final Iterator i = mMethodNode.instructions.iterator();
    while (i.hasNext())
    {
      final AbstractInsnNode current = (AbstractInsnNode) i.next();
      if (current.getType() == AbstractInsnNode.LINE)
      {
        final LineNumberNode lineNumber = (LineNumberNode) current;
        result = lineNumber.line;
        break;
      }
    }
    return result;
  }

  private int getLastLineNumber()
  {
    int result = -1;
    final Iterator i = mMethodNode.instructions.iterator();
    while (i.hasNext())
    {
      final AbstractInsnNode current = (AbstractInsnNode) i.next();
      if (current.getType() == AbstractInsnNode.LINE)
      {
        final LineNumberNode lineNumber = (LineNumberNode) current;
        result = Math.max(lineNumber.line, result);
      }
    }
    return result;
  }

  private void injectEnteringLogging(InsnList cmd)
  {
    // Check arguments
    final Type[] arguments;
    if (mMethodNode.desc != null)
    {
      arguments = Type.getArgumentTypes(mMethodNode.desc);
    }
    else
    {
      arguments = new Type[0];
    }

    mClassInjector.getStaticLoggerOnStack(cmd);
    mClassInjector.getClassNameOnStack(cmd);
    getMethodNameOnStack(cmd);
    if (arguments == null || arguments.length == 0)
    {
      cmd.add(new MethodInsnNode(
          Opcodes.INVOKEVIRTUAL,
          JAVA_LOGGER,
          "entering",
          "(Ljava/lang/String;Ljava/lang/String;)V"));
    }
    else  if (arguments.length == 1)
    {
      boxMe(cmd, isStatic() ? 0 : 1, arguments[0]);
      cmd.add(new MethodInsnNode(
          Opcodes.INVOKEVIRTUAL,
          JAVA_LOGGER,
          "entering",
          "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"));
    }
    else
    {
      AsmUtil.loadConstant(cmd, arguments.length);
      cmd.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));

      final int[] argPos = getArguments(arguments);
      for (int i = 0; i < arguments.length; i++)
      {
        cmd.add(new InsnNode(Opcodes.DUP));
        AsmUtil.loadConstant(cmd, i);
        boxMe(cmd, argPos[i], arguments[i]);
        cmd.add(new InsnNode(Opcodes.AASTORE));
      }
      cmd.add(new MethodInsnNode(
          Opcodes.INVOKEVIRTUAL,
          JAVA_LOGGER,
          "entering",
          "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V"));
    }
  }

  private void injectExitingLogger()
  {
    final Iterator i = mMethodNode.instructions.iterator();
    while (i.hasNext())
    {
      final AbstractInsnNode current = (AbstractInsnNode) i.next();
      final int opcode = current.getOpcode();

      LabelNode labelAfterLogging = null;
      InsnList cmd = null;
      switch (opcode)
      {
        case Opcodes.RETURN:
          cmd = new InsnList();
          labelAfterLogging = isLoggable(cmd);
          mClassInjector.getStaticLoggerOnStack(cmd);
          mClassInjector.getClassNameOnStack(cmd);
          getMethodNameOnStack(cmd);
          cmd.add(new MethodInsnNode(
              Opcodes.INVOKEVIRTUAL,
              JAVA_LOGGER,
              "exiting",
              "(Ljava/lang/String;Ljava/lang/String;)V"));
          cmd.add(labelAfterLogging);
          mMethodNode.instructions.insertBefore(current, cmd);
          break;
        case Opcodes.IRETURN:
        case Opcodes.LRETURN:
        case Opcodes.FRETURN:
        case Opcodes.DRETURN:
        case Opcodes.ARETURN:
          cmd = new InsnList();
          final Type type = Type.getReturnType(mMethodNode.desc);
          labelAfterLogging = isLoggable(cmd);
          final LocalVariableNode resultVar
            = storeTypeOnStackInNewVariable(cmd, labelAfterLogging,
              type);

          mClassInjector.getStaticLoggerOnStack(cmd);
          mClassInjector.getClassNameOnStack(cmd);
          getMethodNameOnStack(cmd);

          boxMe(cmd, resultVar);
          cmd.add(new MethodInsnNode(
              Opcodes.INVOKEVIRTUAL,
              JAVA_LOGGER,
              "exiting",
              "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)V"));
          cmd.add(labelAfterLogging);
          mMethodNode.instructions.insertBefore(current, cmd);
          break;
        default:
          // stepping forward in method
          break;
      }
    }
  }

  private TryCatchBlockNode injectThrowingLogging(InsnList cmd,
      LabelNode labelAfterEnteringLogging)
  {
    final LabelNode lastStatement = beforeLastStatement();
    TryCatchBlockNode result = null;
    if (!near(lastStatement, labelAfterEnteringLogging))
    {
      final LabelNode methodEnd = new LabelNode();
      cmd.add(methodEnd);
      final LabelNode labelAfterThrowingLogging = isLoggable(cmd);


      final LocalVariableNode exVar
        = storeTypeOnStackInNewVariable(
            cmd, labelAfterThrowingLogging, TracingInjector.THROWABLE_TYPE);

      mClassInjector.getStaticLoggerOnStack(cmd);
      mClassInjector.getClassNameOnStack(cmd);
      getMethodNameOnStack(cmd);
      cmd.add(new VarInsnNode(Opcodes.ALOAD, exVar.index));
      cmd.add(new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                JAVA_LOGGER,
                "throwing",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V"));
      cmd.add(labelAfterThrowingLogging);

      cmd.add(new InsnNode(Opcodes.ATHROW));
      final LabelNode theEnd = new LabelNode();
      cmd.add(theEnd);
  //    mIsLoggableVar.end = theEnd;

      result  = new TryCatchBlockNode(labelAfterEnteringLogging,
          lastStatement, methodEnd,
          TracingInjector.THROWABLE_TYPE.getInternalName());
    }
    return result;
  }

  /**
   * Checks if the 2 labels are near to each other (not separated by code).
   * @return true if the 2 labels are near to each other
   */
  private boolean near(LabelNode labelA, LabelNode labelB)
  {
    boolean result = false;
    AbstractInsnNode a = labelA;
    while (a != null &&
        ( a.getType() == AbstractInsnNode.LABEL
            || a.getType() == AbstractInsnNode.LINE))
    {
      if (a == labelB)
      {
        result = true;
        break;
      }
      a = a.getNext();
    }
    if (!result)
    {
      a = labelA;
      while (a != null &&
          ( a.getType() == AbstractInsnNode.LABEL
              || a.getType() == AbstractInsnNode.LINE))
      {
        if (a == labelB)
        {
          result = true;
          break;
        }
        a = a.getPrevious();
      }
    }
    return result;
  }

  private LabelNode beforeLastStatement()
  {
    AbstractInsnNode last = mMethodNode.instructions.getLast();

    while (last != null &&
        (last.getType() == AbstractInsnNode.LINE
            || last.getType() == AbstractInsnNode.LABEL))
    {
      last = last.getPrevious();
    }

    // now check for a label...
    AbstractInsnNode label
      = (last == null ? mMethodNode.instructions.getLast() : last);
    while (label != null && label.getType() == AbstractInsnNode.LINE)
    {
      label = label.getPrevious();
    }

    final LabelNode result;
    if (label != null && label.getType() == AbstractInsnNode.LABEL)
    {
      result = (LabelNode) label;
    }
    else if (last == null)
    {
      result = new LabelNode();
      mMethodNode.instructions.insert(result);
    }
    else
    {
      result = new LabelNode();
      mMethodNode.instructions.insertBefore(last, result);
    }
    return result;
  }

  /**
   *
   */
  private void ensureLocalVariablesAreSet()
  {
    if (mMethodNode.localVariables == null)
    {
      mMethodNode.localVariables = new ArrayList();
    }
  }

  private void injectIsLoggableVariable (InsnList cmd)
  {
    final int varIndex = getFreeLocalIndex();

    final LabelNode varStartNode = new LabelNode();
    mIsLoggableVar
      = new LocalVariableNode(
          TracingInjector.IS_LOGGABLE_FIELD_NAME + varIndex,
          Type.BOOLEAN_TYPE.getDescriptor(),
          (String) null, varStartNode, mMethodEnd, varIndex);
    mMethodNode.localVariables.add(mIsLoggableVar);

    mClassInjector.getStaticLoggerOnStack(cmd);
    cmd.add(new FieldInsnNode(
        Opcodes.GETSTATIC,
        "java/util/logging/Level",
        "FINER", "Ljava/util/logging/Level;"));
    cmd.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        JAVA_LOGGER,
        "isLoggable",
        "(Ljava/util/logging/Level;)Z"));
    cmd.add(new VarInsnNode(Opcodes.ISTORE, mIsLoggableVar.index));
    cmd.add(varStartNode);
  }

  /**
   * Returns an array holding the Variable index of the corresponding Argument.
   * @param types an array holding the argument Types.
   * @return
   */
  private int[] getArguments(Type[] types)
  {
    final int[] result = new int[types.length];
    int cur = isStatic() ? 0 : 1;
    for (int pos = 0; pos < types.length; pos++)
    {
      result[pos] = cur;
      cur += types[pos].getSize();
    }
    return result;
  }

  /**
   *
   * @param cmd
   */
  private void getMethodNameOnStack(InsnList cmd)
  {
    cmd.add(new LdcInsnNode(mMethodName));
  }

  /**
   *
   * @return
   */
  private int getFreeLocalIndex ()
  {
    final Iterator i = mMethodNode.localVariables.iterator();
    int maxIndex = -1;
    while (i.hasNext())
    {
      final LocalVariableNode node = (LocalVariableNode) i.next();
      if (node.index >= maxIndex)
        // >= needed because index could be reused with larger type
      {
        if (node.desc.equals(Type.DOUBLE_TYPE.getDescriptor())
            || node.desc.equals(Type.LONG_TYPE.getDescriptor()))
        {
          maxIndex = node.index + 1;
        }
        else
        {
          maxIndex = node.index;
        }
      }
    }
    maxIndex++;

    final int other = getFreeLocalByCode();
    return Math.max(other, maxIndex);
  }

  /**
   *
   * @return
   */
  private int getFreeLocalByCode ()
  {
     int maxIndex = -1;
     final Iterator i = mMethodNode.instructions.iterator();
     while (i.hasNext())
     {
       final AbstractInsnNode insn = (AbstractInsnNode) i.next();
       if (insn instanceof VarInsnNode)
       {
         final VarInsnNode varInsnNode = (VarInsnNode) insn;
         if (varInsnNode.var >= maxIndex)
         { // >= needed because index could be reused with larger type
           switch (varInsnNode.getOpcode())
           {
             case Opcodes.ILOAD:
             case Opcodes.FLOAD:
             case Opcodes.ALOAD:
             case Opcodes.ISTORE:
             case Opcodes.FSTORE:
             case Opcodes.ASTORE:
               maxIndex = varInsnNode.var;
               break;

             case Opcodes.LLOAD:
             case Opcodes.DLOAD:
             case Opcodes.LSTORE:
             case Opcodes.DSTORE:
               maxIndex = varInsnNode.var + 1;
               break;
             case Opcodes.RET:
               // ignore already covered with the ASTORE
               break;
             default:
               Assert.assertTrue(
                   "Unexpected opcode " + insn.getOpcode()
                   + " in VarInsnNode operation.", false);
             break;
           }
         }
       }
     }

     return maxIndex + 1;
  }

  /**
   * Expects data of type type on the Stacks and stores it in a new Variable,
   * the stack content stays unchanged!
   * @param cmd the instruction list to add the commands.
   * @param end intended end label for the validity of the variable
   * @param type the type of the data to store
   * @return the new generated variable
   */
  private LocalVariableNode storeTypeOnStackInNewVariable(InsnList cmd,
      LabelNode end, final Type type)
  {
    // TODO: Could be more efficient in reusing free indexes
    // But we can not relay on the localVariables table...

    // CREATE VAR
    // The label is the position AFTER the xSTORE cmd.
    final LabelNode startLabel = new LabelNode();
    final int index = getFreeLocalIndex();
    final LocalVariableNode resultVar
      = new LocalVariableNode(
          TracingInjector.RESULT_VAR_FIELD_NAME + index,
        type.getDescriptor(), (String) null, startLabel, end,
        index);
    mMethodNode.localVariables.add(resultVar);

    // CREATE CODE
    cmd.add(new InsnNode(type.getSize() == 1 ? Opcodes.DUP : Opcodes.DUP2));
    cmd.add(new VarInsnNode(
        type.getOpcode(Opcodes.ISTORE), resultVar.index));
    cmd.add(startLabel);
    // Load the var again (could use the DUP above)
//    cmd.add(new VarInsnNode(
//        type.getOpcode(Opcodes.ILOAD), resultVar.index));
    return resultVar;
  }

  /**
   *
   * @return
   */
  private boolean isStatic ()
  {
    return (mMethodNode.access & Opcodes.ACC_STATIC) != 0;
  }

  /**
   *
   * @param cmd
   * @param var
   */
  private void boxMe(InsnList cmd, LocalVariableNode var)
  {
    Assert.notNull(cmd, "cmd");
    Assert.notNull(var, "var");
    boxMe(cmd, var.index, Type.getType(var.desc));
  }

  /**
   *
   * @param cmd
   * @param index
   * @param type
   */
  private void boxMe(InsnList cmd, int index, Type type)
  {
    switch (type.getSort())
    {
      case Type.SHORT:
        boxMe(cmd, type, "java/lang/Short", index);
        break;
      case Type.INT:
        boxMe(cmd, type, "java/lang/Integer", index);
        break;
      case Type.CHAR:
        boxMe(cmd, type, "java/lang/Character", index);
        break;
      case Type.BOOLEAN:
        boxMe(cmd, type, "java/lang/Boolean", index);
        break;
      case Type.LONG:
        boxMe(cmd, type, "java/lang/Long", index);
        break;
      case Type.FLOAT:
        boxMe(cmd, type, "java/lang/Float", index);
        break;
      case Type.DOUBLE:
        boxMe(cmd, type, "java/lang/Double", index);
        break;
      case Type.BYTE:
        boxMe(cmd, type, "java/lang/Byte", index);
        break;
      case Type.ARRAY:
      case Type.OBJECT:
        cmd.add(new VarInsnNode(
            type.getOpcode(Opcodes.ILOAD), index));
        break;
      default:
        throw new RuntimeException("Unexpected type " + type.getDescriptor());
    }
  }

  /**
   *
   * @param instr
   * @param type
   * @param boxClassName
   * @param index
   */
  private void boxMe(InsnList instr, Type type, String boxClassName,
      int index)
  {
    if (!(type.getSort() == Type.BOOLEAN) && !mClassInjector.isJava5())
    {
      // new Xxxxx(...)
      instr.add(new TypeInsnNode(Opcodes.NEW, boxClassName));
      instr.add(new InsnNode(Opcodes.DUP));
      instr.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), index));
      instr.add(new MethodInsnNode(
        Opcodes.INVOKESPECIAL, boxClassName, "<init>",
        "(" + type.getDescriptor() + ")V"));
    }
    else
    { // since JDK 1.5 version (except for boolean):
      // Xxxxx.valueOf(...)
      instr.add(new VarInsnNode(
        type.getOpcode(Opcodes.ILOAD), index));
      instr.add(new MethodInsnNode(
          Opcodes.INVOKESTATIC, boxClassName, "valueOf",
          "(" + type.getDescriptor() +  ")L" + boxClassName + ";"));
    }
  }

  /**
   *
   * @return
   */
  private LabelNode getOrCreateEndLabel ()
  {
    final AbstractInsnNode last = mMethodNode.instructions.getLast();
    final LabelNode result;
    if (last.getType() == AbstractInsnNode.LABEL)
    {
      result = (LabelNode) last;
    }
    else
    {
      // need a new end Label...
      result = new LabelNode();
      mMethodNode.instructions.add(result);
    }
    return result;
  }

  /**
   *
   * @return
   */
  private LabelNode getOrCreateStartLabel ()
  {
    final AbstractInsnNode first = mMethodNode.instructions.getFirst();
    final LabelNode result;
    if (first.getType() == AbstractInsnNode.LABEL)
    {
      result = (LabelNode) first;
    }
    else
    { // need a start Label...
      result = new LabelNode();
      mMethodNode.instructions.insertBefore(first, result);
    }
    return result;
  }

  /**
   * Create ifLoggable code and return a label to jump to
   * in case of a false result.
   * @param cmd the instruction list to add the instructions to.
   * @return the node that is used as target if the isLoggable
   *  evaluates to false.
   */
  private LabelNode isLoggable (InsnList cmd)
  {
    cmd.add(new VarInsnNode(Opcodes.ILOAD, mIsLoggableVar.index));
    final LabelNode afterLogging = new LabelNode();
    cmd.add(new JumpInsnNode(Opcodes.IFEQ, afterLogging));
    return afterLogging;
  }
}
