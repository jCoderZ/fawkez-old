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

import junit.framework.TestCase;

public class AspectPatternTest extends TestCase
{

  public void testAspectPattern()
  {
    AspectPattern pat
      = new AspectPattern("int foo.*.Bar.method()");
    assertEquals("No modifier!", pat.getModifiers(), 0);
    assertEquals("method\\(\\)I", pat.getMethodRegex());
    assertTrue(pat.matches(0,
        "foo/test/Bar", "method()I"));
    assertFalse(pat.matches(0,
        "foo/test/Bar", "method(I)I"));
  }

  public void testAspectPattern2()
  {
    AspectPattern pat
      = new AspectPattern("int *.method(*)");
    assertEquals("No modifier!", pat.getModifiers(), 0);
    assertEquals("method\\(\\[*([VZCBSIFJD]|L[^;]+;)\\)I", pat.getMethodRegex());
    assertEquals("[^/]*", pat.getClassRegex());
    assertTrue(pat.matches(0,
        "Foo", "method(I)I"));
    assertTrue(pat.matches(0,
        "Foo", "method([I)I"));
    assertTrue(pat.matches(0,
        "Foo", "method(Ljava/lang/String;)I"));
    assertFalse(pat.matches(0,
        "Foo", "method()I"));
    assertFalse(pat.matches(0,
        "pkg/Foo", "method(I)I"));
    assertFalse(pat.matches(0,
        "pkg/Foo", "method(II)I"));
    assertFalse(pat.matches(0,
        "Foo", "method()I"));
    assertFalse(pat.matches(0,
        "Foo", "method(I)V"));
  }

  public void testAspectPattern3()
  {
    AspectPattern pat
      = new AspectPattern("int foo.*.*.method(*,int)");
    assertEquals("No modifier!", pat.getModifiers(), 0);
//    assertEquals("method\\(\\[*([VZCBSIFJD]|L[^;]+;)\\)I", pat.mMethodPattern);
//    assertEquals("[^/]*", pat.mClassName);
    assertTrue(pat.matches(0,
        "foo/foo/Foo", "method(JI)I"));
    assertTrue(pat.matches(0,
        "foo/test/Bar", "method(Ljava/lang/String;I)I"));
    assertTrue(pat.matches(0,
        "foo/test/Bar", "method(II)I"));
    assertFalse(pat.matches(0,
        "foo/Bar", "method(II)I"));
    assertFalse(pat.matches(0,
        "foo/test/Bar", "method(IJ)I"));
    assertFalse(pat.matches(0,
        "foo/test/Bar", "method(II)V"));
    assertFalse(pat.matches(0,
        "test/test/Bar", "method(II)I"));
  }

  public void testAspectPattern4()
  {
    AspectPattern pat
      = new AspectPattern("public * org.jcoderz..*.*(..)");
    assertTrue("Pattern should match class!" + pat,
        pat.matchClass("org/jcoderz/tracing/Test"));
    assertTrue("Pattern should match method!" + pat,
        pat.matchMethod("method(JI)I"));
    assertTrue("Pattern should match!" + pat, pat.matches(1,
        "org/jcoderz/tracing/Test", "method(JI)I"));
  }

  public void testAspectPattern5()
  {
    AspectPattern pat
      = new AspectPattern("public * org.jcoderz.tracing.logging.test.*.*(..)");
    assertTrue("Pattern should match class!" + pat,
        pat.matchClass("org/jcoderz/tracing/logging/test/Test"));
    assertTrue("Pattern should match method!" + pat,
        pat.matchMethod("method(JI)I"));
    assertTrue("Pattern should match!" + pat, pat.matches(1,
        "org/jcoderz/tracing/logging/test/Test", "method(JI)I"));
  }

  public void testAspectPattern6()
  {
    AspectPattern pat
      = new AspectPattern("* *.set*(..)");
    assertTrue("Pattern should match class!" + pat,
        pat.matchClass("Test"));
    assertTrue("Pattern should match method!" + pat,
        pat.matchMethod("setMethod(JI)I"));
    assertFalse("Pattern should not match class!" + pat,
        pat.matchClass("com/Test"));
  }

  public void testAspectPattern7()
  {
    AspectPattern pat
      = new AspectPattern("public void Account.set*(*)");
    assertTrue("Pattern should match class! " + pat,
        pat.matchClass("Account"));
    assertTrue("Pattern should match method! " + pat,
        pat.matchMethod("setMethod(I)V"));
    assertTrue("Pattern should match method! " + pat,
        pat.matchAccess(Opcodes.ACC_PUBLIC));
    assertFalse("Pattern should not access level! " + pat,
        pat.matchAccess(Opcodes.ACC_PRIVATE));
    assertFalse("Pattern should not match method! " + pat,
        pat.matchMethod("getMethod(I)V"));
    assertFalse("Pattern should not match class! " + pat,
        pat.matchClass("com.Account"));
  }

  public void testAspectPattern8()
  {
    AspectPattern pat
      = new AspectPattern("public static void ...main(String[])");
    assertTrue("Pattern should match class! " + pat,
        pat.matchClass("Account"));
    assertTrue("Pattern should match class! " + pat,
        pat.matchClass("com/Account"));
    assertTrue("Pattern should match method! " + pat,
        pat.matchMethod("main([Ljava/lang/String;)V"));
    assertTrue("Pattern should match access level! " + pat,
        pat.matchAccess(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC));
    assertFalse("Pattern should not match access level! " + pat,
        pat.matchAccess(Opcodes.ACC_STATIC));
    assertFalse("Pattern should not access level! " + pat,
        pat.matchAccess(Opcodes.ACC_PRIVATE));
    assertFalse("Pattern should not match method! " + pat,
        pat.matchMethod("getMethod(I)V"));
  }

  public void testAspectPattern9()
  {
    AspectPattern pat
      = new AspectPattern("public static * org.jcoderz.tracing.db.DaoUtil.*(..)");
    assertTrue("Pattern should match class! " + pat,
        pat.matchClass("org/jcoderz/tracing/db/DaoUtil"));
    assertTrue("Pattern should match method! " + pat + " '"
        + pat.getMethodPattern() + "'",
        pat.matchMethod("create(Ljava/lang/Object;)Ljava/lang/Object;"));
    assertTrue("Pattern should match access level! " + pat,
        pat.matchAccess(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC));
    assertFalse("Pattern should not match access level! " + pat,
        pat.matchAccess(Opcodes.ACC_STATIC));
    assertFalse("Pattern should not access level! " + pat,
        pat.matchAccess(Opcodes.ACC_PRIVATE));
  }
}
