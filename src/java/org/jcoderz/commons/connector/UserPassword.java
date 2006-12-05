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
package org.jcoderz.commons.connector;

/**
 * Provides UserName and Password.
 *
 */
public final class UserPassword
{
   /** A UserPassword instance with a null userName and null password. */
   public static final UserPassword EMPTY_USER_PASSWORD = new UserPassword ();

   private final String mUser;
   private final String mPassword;
   private final int mHashCode;

   private UserPassword (UserPassword up)
   {
      mHashCode = up.mHashCode;
      mUser = up.mUser;
      mPassword = up.mPassword;
   }

   private UserPassword ()
   {
      this(null, null);
   }

   private UserPassword (String user, String password)
   {
      mUser = user;
      mPassword = password;
      mHashCode = new StringBuffer(UserPassword.class.getName()).append(mUser)
            .append(mPassword).toString().hashCode();
   }

   /**
    * Constructs a new UserPassword instance containing exactly the same user
    * and password as the <code>up</code> instance provides.
    *
    * @param up The UserPassword instance to be cloned.
    *
    * @return a new UserPassword instance containing the same user and
    *    password as the <code>up</code> instance provides.
    */
   public static UserPassword fromUserPassword (UserPassword up)
   {
      final UserPassword result;
      if (EMPTY_USER_PASSWORD.equals(up))
      {
         result = EMPTY_USER_PASSWORD;
      }
      else
      {
         result = new UserPassword(up);
      }

      return result;
   }

   /**
    * Returns a new UserPassword instance wrapping the given <code>user</code>
    * and <code>password</code>.
    *
    * @param user UserName
    * @param password Password
    *
    * @return a new UserPassword instance wrapping the given <code>user</code>
    *    and <code>password</code>.
    */
   public static UserPassword fromUserPassword (final String user,
         final String password)
   {
      final UserPassword result;
      if (user == null && password == null)
      {
         result = EMPTY_USER_PASSWORD;
      }
      else
      {
         result = new UserPassword(user, password);
      }

      return result;
   }


   /**
    * @return Returns the Password.
    */
   public String getPassword ()
   {
      return mPassword;
   }
   /**
    * @return Returns the User.
    */
   public String getUserName ()
   {
      return mUser;
   }

   /**
    * Returns true if the UserPassword instance <code>up</code> contains a null
    * UserName and null Password.
    *
    * @param up UserPassword to be checked.
    *
    * @return true if the UserPassword instance <code>up</code> contains a null
    * UserName and null Password; otherwise false.
    */
   public static boolean isEmpty (UserPassword up)
   {
      return UserPassword.EMPTY_USER_PASSWORD.equals(up);
   }

   /** {@inheritDoc} */
   public boolean equals (Object other)
   {
      boolean result = false;
      if (this == other)
      {
         result = true;
      }
      else if (other != null)
      {
         if (mHashCode == other.hashCode() && other instanceof UserPassword)
         {
            final UserPassword up = (UserPassword) other;
            result = isStringsEquals(mUser, up.mUser)
                  && isStringsEquals(mPassword, up.mPassword);
         }
      }

      return result;
   }

   /** {@inheritDoc} */
   public int hashCode ()
   {
      return mHashCode;
   }

   private static boolean isStringsEquals (final String a, final String b)
   {
      boolean equals = false;
      if (a == null && b == null)
      {
         equals = true;
      }
      else if (a != null && a.equals(b))
      {
         equals = true;
      }
      return equals;
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      return new StringBuffer("<UserPassword user: '").append(mUser)
         .append("', password: 'xxx'>").toString();
   }
}
