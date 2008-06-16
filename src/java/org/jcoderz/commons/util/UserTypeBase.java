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
package org.jcoderz.commons.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 *
 * Base class for Hibernate User Types. This class is used by the
 * Hibernate binding for StrongTypes. There exist subclasses for
 * the specific kinds of StrongTypes like RestrictedString.
 *
 * @author thomas.bodemer
 */
public abstract class UserTypeBase implements UserType
{

    /**
     * {@inheritDoc}
     */
    public abstract int[] sqlTypes ();

    /**
     * {@inheritDoc}
     */
    public abstract Class returnedClass ();

    /**
     * {@inheritDoc}
     */
    public boolean equals (Object x, Object y)
    {
      return ObjectUtil.equals(x, y);
    }

    /**
     * {@inheritDoc}
     */
    public Object deepCopy (Object value)
    {
      return value;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isMutable ()
    {
      return false;
    }

    /**
     * {@inheritDoc}
     */
    public abstract Object nullSafeGet (ResultSet resultSet, String[] types,
        Object owner)
      throws HibernateException, SQLException;

    /**
     * {@inheritDoc}
     */
    public abstract void nullSafeSet (PreparedStatement statement, Object value,
        int index)
      throws HibernateException, SQLException;

    /**
     * {@inheritDoc}
     */
    public int hashCode (Object x) throws HibernateException
    {
      return x.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public Serializable disassemble (Object value)
        throws HibernateException
    {
      return (Serializable) value;
    }

    /**
     * {@inheritDoc}
     */
    public Object assemble (Serializable cached, Object owner)
        throws HibernateException
    {
      return cached;
    }

    /**
     * {@inheritDoc}
     */
    public Object replace (Object original, Object target, Object owner)
        throws HibernateException
    {
      return original;
    }
}
