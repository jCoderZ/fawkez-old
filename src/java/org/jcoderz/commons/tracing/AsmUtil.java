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


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;


/**
 * Utility classes around the ASM package. See http://asm.objectweb.org/
 * 
 * @author Andreas Mandel
 */
public final class AsmUtil
{
    /** Maximum value that can be set using the Opcodes.ICONST_0. */
    public static final int MAX_ICONST = 5;

    /** Maximum value that can be set using the Opcodes.BIPUSH. */
    public static final int MAX_BIPUSH = 127;

    /** Maximum value that can be set using the Opcodes.SIPUSH. */
    public static final int MAX_SIPUSH = 32767;

    /** No instances. */
    private AsmUtil ()
    {
        // No instances.
    }

    /**
     * Add a command to the stack that loads the given integer constant.
     * The method takes care to use the most efficient opcode.
     * 
     * @param cmd the command list where to add the command.
     * @param i the value to be loaded.
     */
    public static void loadConstant (InsnList cmd, int i)
    {
        if (i <= MAX_ICONST)
        {
            cmd.add(new InsnNode(Opcodes.ICONST_0 + i));
        }
        else if (i <= MAX_BIPUSH)
        {
            cmd.add(new IntInsnNode(Opcodes.BIPUSH, i));
        }
        else if (i <= MAX_SIPUSH)
        {
            cmd.add(new IntInsnNode(Opcodes.SIPUSH, i));
        }
        else
        {
            cmd.add(new IntInsnNode(Opcodes.LDC, i));
        }
    }

    /**
     * To string helper to get nice comments about the MethodNode.
     * 
     * @param mn the method node.
     * @return a human readable string representation describing the
     *         method node
     */
    public static String toString (MethodNode mn)
    {
        return toString(mn.access) + " " + mn.name + " " + mn.desc;
    }

    /**
     * Human readable name of the class.
     * 
     * @param classNode the asm class node.
     * @return Human readable name of the class.
     */
    public static String toString (ClassNode classNode)
    {
        return (toString(classNode.access)
            + ((classNode.access & Opcodes.ACC_INTERFACE) == 0
                ? " class "
                : " interface ") + classNode.name).trim();
    }

    /**
     * Human readable name of the method.
     * 
     * @param cn the asm class node.
     * @param mn the asm method node.
     * @return Human readable name of the class and method.
     */
    public static String toString (ClassNode cn, MethodNode mn)
    {
        return toString(mn.access) + " " + cn.name + "#" + mn.name + mn.desc;
    }

    /**
     * Human readable name of the local variable.
     * 
     * @param node the asm local variable node.
     * @return Human readable name of the local variable.
     */
    public static String toString (LocalVariableNode node)
    {
        return node.name + ": " + node.desc + "@" + node.index + "["
            + node.start + ":" + node.end + "]";
    }

    /**
     * Returns a string representation containing the access keywords
     * implied by the given int. See {@link Opcodes} ACC* for the
     * values.
     * 
     * @param access the int value holding the access bits. See
     *        {@link Opcodes} ACC* for the values.
     * @return a string representation containing the access keywords
     *         implied by the given int.
     */
    public static String toString (int access)
    {
        final StringBuffer sb = new StringBuffer();
        if ((access & Opcodes.ACC_PUBLIC) != 0)
        {
            sb.append("public ");
        }
        if ((access & Opcodes.ACC_PROTECTED) != 0)
        {
            sb.append("protected ");
        }
        if ((access & Opcodes.ACC_PRIVATE) != 0)
        {
            sb.append("private ");
        }
        if ((access & Opcodes.ACC_STATIC) != 0)
        {
            sb.append("static ");
        }
        if ((access & Opcodes.ACC_FINAL) != 0)
        {
            sb.append("final ");
        }
        if ((access & Opcodes.ACC_SYNCHRONIZED) != 0)
        {
            sb.append("synchronized ");
        }
        if ((access & Opcodes.ACC_VOLATILE) != 0)
        {
            sb.append("volatile ");
        }
        if ((access & Opcodes.ACC_TRANSIENT) != 0)
        {
            sb.append("transient ");
        }
        if ((access & Opcodes.ACC_NATIVE) != 0)
        {
            sb.append("native ");
        }
        if ((access & Opcodes.ACC_ABSTRACT) != 0)
        {
            sb.append("abstract ");
        }
        if (sb.length() > 0)
        {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
