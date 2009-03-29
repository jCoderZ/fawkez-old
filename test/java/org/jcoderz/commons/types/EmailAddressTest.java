/*
 * Copyright 2008, The Achievo Deutschland AG. All rights reserved.
 *
 * $Id$
 */
package org.jcoderz.commons.types;

import junit.framework.TestCase;

import org.jcoderz.commons.ArgumentMalformedException;

/**
 * This test case checks basic functionality of the Email type.
 *
 * @author Michael Rumpf
 */
public class EmailAddressTest extends TestCase
{
  public void testNullParameter ()
  {
    try
    {
      new EmailAddress(null);
      fail("ArgumentMalformedException should be thrown");
    }
    catch (ArgumentMalformedException ex)
    {
      // expected
    }
  }

  public void testEmptyParameter ()
  {
    try
    {
      new EmailAddress("");
      fail("ArgumentMalformedException should be thrown");
    }
    catch (ArgumentMalformedException ex)
    {
      // expected
    }
  }

  public void testNoAtSign ()
  {
    try
    {
      new EmailAddress("aaabbb.com");
      fail("ArgumentMalformedException should be thrown");
    }
    catch (ArgumentMalformedException ex)
    {
      // expected
    }
  }

  public void testInvalidLocalPart ()
  {
    try
    {
      new EmailAddress("xx(yy)zz@achievo.com");
      fail("ArgumentMalformedException should be thrown");
    }
    catch (ArgumentMalformedException ex)
    {
      // expected
    }
  }

  public void testGoodEmail ()
  {
    final EmailAddress email = new EmailAddress("test@example.com");
    assertEquals(
        "Unexpected name in valid email address.", "test", email.getName());
    assertEquals(
        "Unexpected domain part in valid email address.", "example.com",
        email.getDomain());
  }
}
