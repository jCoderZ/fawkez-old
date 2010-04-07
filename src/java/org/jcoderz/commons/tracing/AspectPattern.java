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


import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.jcoderz.commons.ArgumentMalformedException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


/**
 * This class allows to match aspectJ like patterns with the java
 * internal class representation.
 * <p>
 * The following limitations / specialties should be considered:
 * <ul>
 * <li>the '+' is not supported so you can not check for classes
 * implementing a certain interface.</li>
 * <li><code>throws</code> is not supported as a part of the matching
 * signature.</li>
 * <li>The pattern is based on the information you can find at <a
 * href="http://aspectwerkz.codehaus.org/definition_issues.html">
 * aspectwerkz</a></li>
 * <li>The method name <code>new</code> can be used to refer to the
 * constructor (<code>&lt;init></code>) and to the class (
 * <code>&lt;clinit></code>) initialization.</li>
 * </ul>
 * TODO: Javadoc 
 * TODO: Support '+' for inheritance 
 * 
 * @author Andreas Mandel
 */
public class AspectPattern
{
    private final String mPattern;

    private final String mClassRegex;

    private final String mMethodRegex;

    private String mReturnPattern;

    /** The tracer class that should be called if the pattern matches. */
    private String mTracerClass; 

    private int mModifiers;

    private Pattern mClassMatcher;

    private Pattern mMethodMatcher;

    /**
     * Creates new aspect pattern matcher. The input is translated into
     * regular expressions which are compiled and stored in this
     * AspectPattern.
     * 
     * @param in the aspectJ like pattern.
     */
    public AspectPattern (String in)
    {
        mPattern = in;
        int pos = 0;
        pos = getTracerClass(0);
        pos = getModifiers(pos);
        pos = getReturnType(pos);
        final int paraPos = mPattern.indexOf('(', pos);
        final int methodPos = mPattern.substring(pos, paraPos).lastIndexOf('.')
            + pos;
        // TODO Support +!
        mClassRegex = convertToPattern(mPattern.substring(pos, methodPos));
        final String methodName = convertToMethod(mPattern.substring(
            methodPos + 1, paraPos));
        final String arguments = convertToArgumets(mPattern.substring(
            paraPos + 1, mPattern.length() - 1));
        mMethodRegex = methodName + "\\(" + arguments + "\\)" + mReturnPattern;
    }

    /**
     * Parse the optional name of the tracer class to be used.
     * As a side effect {@link #mTracerClass} is set.
     * @param i the pos where to start parsing 
     * @return the pos after the tracer class name.
     */
    private int getTracerClass (int i)
    {
        int pos = i;
        int end = i;
        while (pos < mPattern.length() 
            && Character.isWhitespace(mPattern.charAt(pos)))
        {
            pos++;
        }
        if (pos < mPattern.length() 
            && mPattern.charAt(pos) == '(')
        {
            end = mPattern.indexOf(')', pos);
            if (end == -1)
            {
                throw new ArgumentMalformedException(
                    "pattern", mPattern, 
                    "Opening '(' for tracing class name is not closed.");
            }
            mTracerClass = mPattern.substring(pos + 1, end);
            end += 1;
            while (end < mPattern.length() 
                && Character.isWhitespace(mPattern.charAt(end)))
            {
                end++;
            }
        }
        return end;
    }

    /**
     * Tests if the given class with the given access flags matches this
     * pattern.
     * 
     * @param acc the access flags.
     * @param className name of the class in internal representation.
     * @param arguments argument string in internal representation.
     * @return true if the given parameters match this matcher.
     */
    public boolean matches (int acc, String className, String arguments)
    {
        return ((acc & mModifiers) == mModifiers
            && className.matches(mClassRegex) 
            && arguments.matches(mMethodRegex));
    }

    /**
     * @param substring
     * @return
     */
    private String convertToArgumets (String substring)
    {
        final String[] args = substring.split(",");
        final StringBuffer pattern = new StringBuffer();
        final Iterator i = Arrays.asList(args).iterator();
        while (i.hasNext())
        {
            final String arg = (String) i.next();
            if (!org.jcoderz.commons.util.StringUtil.isEmptyOrNull(arg))
            {
                final Type argType = tryBasicType(arg);
                if (argType == null)
                {
                    if ("*".equals(arg))
                    {
                        pattern.append("\\[*([VZCBSIFJD]|L[^;]+;)");
                    }
                    else if ("..".equals(arg))
                    {
                        pattern.append(".*");
                    }
                    else
                    {
                        pattern.append(convertToArgumentPattern(arg));
                    }
                }
                else
                {
                    pattern.append(argType.getDescriptor());
                }
            }
        }
        return pattern.toString();
    }

    /**
     * @param method
     * @return
     */
    private String convertToMethod (String method)
    {
        String result;
        // NEW!!!
        if ("new".equals(method))
        {
            if ((mModifiers & Opcodes.ACC_STATIC) != 0)
            {
                result = "<init>";
            }
            else
            {
                result = "<clinit>";
            }
        }
        else
        {
            result = method.replaceAll("\\*", ".*");
        }
        return result;
    }

    /**
     * @param pos
     * @return
     */
    private int getReturnType (int pos)
    {
        final int result = (mPattern + " ").indexOf(' ', pos) + 1;
        final String typeString = mPattern.substring(pos, result - 1);
        final Type returnType = tryBasicType(typeString);
        if (returnType == null)
        {
            mReturnPattern = convertToArgumentPattern(typeString);
        }
        else
        {
            mReturnPattern = returnType.getDescriptor();
        }
        return result;
    }

    /**
     * @param typeString
     * @return
     */
    private String convertToPattern (String typeString)
    {
        String resultPattern = typeString;
        if (org.jcoderz.commons.util.StringUtil.isNullOrEmpty(typeString))
        {
            resultPattern = "";
        }
        else if ("*".equals(typeString))
        {
            resultPattern = "[^/]*"; // any
        }
        else
        {
            resultPattern = resultPattern.replaceAll("\\.", "/");
            resultPattern = resultPattern.replaceAll("\\*", "[^\\/]*");
            resultPattern = resultPattern.replaceAll("//", ".*");
            // NO 'java.lang / java.util Magic if package is given or
            // wildcards are used.
            if (resultPattern.indexOf('/') < 0
                && resultPattern.indexOf('*') < 0)
            {
                final StringBuffer pattern = new StringBuffer("(java/util/");
                pattern.append(resultPattern);
                pattern.append('|');
                pattern.append("java/lang/");
                pattern.append(resultPattern);
                pattern.append('|');
                pattern.append(resultPattern);
                pattern.append(')');
                resultPattern = pattern.toString();
            }
        }
        return resultPattern;
    }

    /**
     * @param typeString
     * @return
     */
    private String convertToArgumentPattern (String typeString)
    {
        String resultPattern = typeString;
        if (org.jcoderz.commons.util.StringUtil.isNullOrEmpty(typeString))
        {
            resultPattern = "";
        }
        else if ("*".equals(typeString))
        {
            resultPattern = "\\[*([VZCBSIFJD]|L[^;]+;)";
        }
        else
        {
            String arrayPrefix = "";
            // take care for arrays!!!
            while (resultPattern.endsWith("[]"))
            {
                arrayPrefix += "\\[";
                resultPattern 
                    = resultPattern.substring(0, resultPattern.length()
                        - "[]".length());
            }
            resultPattern = resultPattern.replaceAll("\\.", "/");
            resultPattern = resultPattern.replaceAll("\\*", "[^\\/]*");
            resultPattern = resultPattern.replaceAll("//", ".*");
            // NO 'java.lang / java.util Magic if package is given or
            // wildcards are used.
            if (resultPattern.indexOf('/') >= 0
                || resultPattern.indexOf('*') >= 0)
            {
                final StringBuffer pattern = new StringBuffer(arrayPrefix);
                pattern.append('L');
                pattern.append(resultPattern);
                pattern.append(';');
                resultPattern = pattern.toString();
            }
            else
            {
                final StringBuffer pattern = new StringBuffer("(");
                pattern.append(arrayPrefix);
                pattern.append("Ljava/util/");
                pattern.append(resultPattern);
                pattern.append(';');
                pattern.append('|');
                pattern.append(arrayPrefix);
                pattern.append("Ljava/lang/");
                pattern.append(resultPattern);
                pattern.append(';');
                pattern.append('|');
                pattern.append(arrayPrefix);
                pattern.append('L');
                pattern.append(resultPattern);
                pattern.append(";)");
                resultPattern = pattern.toString();
            }
        }
        return resultPattern;
    }

    /**
     * @param typeString
     * @return
     */
    private Type tryBasicType (String typeString)
    {
        final StringBuffer type = new StringBuffer();
        String in = typeString;
        while (in.endsWith("[]"))
        {
            type.append('[');
            in = in.substring(0, in.length() - "[]".length());
        }
        if ("int".equals(in))
        {
            type.append('I');
        }
        else if ("void".equals(in))
        {
            type.append('V');
        }
        else if ("boolean".equals(in))
        {
            type.append('Z');
        }
        else if ("char".equals(in))
        {
            type.append('C');
        }
        else if ("short".equals(in))
        {
            type.append('S');
        }
        else if ("float".equals(in))
        {
            type.append('F');
        }
        else if ("long".equals(in))
        {
            type.append('J');
        }
        else if ("double".equals(in))
        {
            type.append('D');
        }
        else
        {
            type.setLength(0);
        }
        Type resultType = null;
        if (type.length() != 0)
        {
            resultType = Type.getType(type.toString());
            if (resultType.getSort() == Type.OBJECT)
            {
                resultType = null;
            }
        }
        return resultType;
    }

    /**
     * @param pos
     * @return
     */
    private int getModifiers (int pos)
    {
        int result = pos;
        int oldResult;
        do
        {
            oldResult = result;
            result = checkModifier("public ", Opcodes.ACC_PUBLIC, result);
            result = checkModifier("private ", Opcodes.ACC_PRIVATE, result);
            result = checkModifier("protected ", Opcodes.ACC_PROTECTED, result);
            result = checkModifier("static ", Opcodes.ACC_STATIC, result);
            result = checkModifier("final ", Opcodes.ACC_FINAL, result);
            result 
                = checkModifier(
                    "synchronized ", Opcodes.ACC_SYNCHRONIZED, result);
            result 
                = checkModifier(
                    "deprecated ", Opcodes.ACC_DEPRECATED, result);
        }
        while (result != oldResult);
        return result;
    }

    /**
     * @param modifier
     * @param acc
     * @param pos
     * @return
     */
    private int checkModifier (String modifier, int acc, int pos)
    {
        int result = pos;
        if (mPattern.startsWith(modifier, pos))
        {
            mModifiers |= acc;
            result += modifier.length();
        }
        return result;
    }

    public int getModifiers ()
    {
        return mModifiers;
    }

    public String getMethodPattern ()
    {
        return mMethodRegex;
    }

    public String getClassRegex ()
    {
        return mClassRegex;
    }

    // HOWTO Handle inheritance?
    /**
     * @param internalClassname
     * @return
     */
    public boolean matchClass (String internalClassname)
    {
        if (mClassMatcher == null)
        {
            mClassMatcher = Pattern.compile(mClassRegex);
        }
        return mClassMatcher.matcher(internalClassname).matches();
    }

    /**
     * @param methodDesc
     * @return
     */
    public boolean matchMethod (String methodDesc)
    {
        if (mMethodMatcher == null)
        {
            mMethodMatcher = Pattern.compile(mMethodRegex);
        }
        return mMethodMatcher.matcher(methodDesc).matches();
    }

    /** {@inheritDoc} */
    public String toString ()
    {
        return mPattern + " = '" + AsmUtil.toString(mModifiers) + " "
            + mClassRegex + "#" + mMethodRegex + "'";
    }

    /**
     * @param access
     * @return
     */
    public boolean matchAccess (int access)
    {
        return (access & mModifiers) == mModifiers;
    }

    String getMethodRegex ()
    {
        return mMethodRegex;
    }

    public String getTracerClass ()
    {
        return mTracerClass;
    }
}
