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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.jcoderz.commons.types.Date;

/**
 * This is the Hibernate user type that maps a {@link Date} to a
 * timestamp in the Database.
 *
 * @author Andreas Mandel
 */
public class DateUserType
    extends UserTypeBase
{
    private static final long serialVersionUID = 1L;
    private static final int SQL_TYPE = Types.TIMESTAMP;
    private static final int[] SQL_TYPES = {SQL_TYPE};

    /**
     * Hibernate <tt>org.jcoderz.commons.types.Date</tt> type as mapped
     * from this UserType.
     * @return this UserType as org.hibernate.type.Type.
     */
    public static org.hibernate.type.Type getType ()
    {
       return TypeHolder.TYPE;
    }

    /** {@inheritDoc} */
    public Object nullSafeGet (
        ResultSet resultSet, String[] types, Object owner)
        throws HibernateException, SQLException
    {
        final Timestamp timestamp = resultSet.getTimestamp(types[0]);
        final Date result;
        if (timestamp == null || resultSet.wasNull())
        {
            result = null;
        }
        else
        {
            result = Date.fromSqlTimestamp(timestamp);
        }
        return result;
    }

    /** {@inheritDoc} */
    public void nullSafeSet (PreparedStatement statement, Object value,
        int index)
        throws HibernateException, SQLException
    {
        if (value != null)
        {
            statement.setTimestamp(index, ((Date) value).toSqlTimestamp());
        }
        else
        {
            statement.setNull(index, SQL_TYPE);
        }
    }

    /** {@inheritDoc} */
    public Class returnedClass ()
    {
        return Date.class;
    }

    /** {@inheritDoc} */
    public int[] sqlTypes ()
    {
        return (int[]) SQL_TYPES.clone();
    }

    /**
     * Class to lazy initialize the Hibernate Type adapter.
     */
    private static class TypeHolder
    {
       private static final org.hibernate.type.Type TYPE
         = new org.hibernate.type.CustomType(DateUserType.class, null);
    }
}
