/*
 * $Id: ArraysUtil.java 107 2006-12-06 11:27:11Z amandel $
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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;

/**
 *
 * This class is used by the Hibernate binding for numeric strong types.
 *
 * @author Andreas Mandel
 */
public abstract class BigDecimalUserTypeBase
    extends UserTypeBase
{
    private static final int SQL_TYPE = Types.NUMERIC;
    private static final int[] SQL_TYPES = {SQL_TYPE};

    /**
     * {@inheritDoc}
     */
    public Object nullSafeGet (ResultSet resultSet, String[] types,
        Object owner)
        throws HibernateException, SQLException
    {
        final BigDecimal value = resultSet.getBigDecimal(types[0]);
        final Object result;
        if (resultSet.wasNull())
        {
            result = null;
        }
        else
        {
            result = fromBigDecimal(value);
        }
        return result;
    }

    /**
     * Implement this method for the particular StrongType.
     *
     * @param value the BigDecimal representation of the mapped class
     * @return the instance of the mapped class
     */
    public abstract Object fromBigDecimal (BigDecimal value);

    /**
     * Implement this method for the particular StrongType.
     *
     * @param value the StrongType to be converted into its
     *    BigDecimal representation.
     * @return the int representation of the value given.
     */
    public abstract BigDecimal toBigDecimal (Object value);

    /**
     * {@inheritDoc}
     */
    public void nullSafeSet (PreparedStatement statement, Object value,
        int index)
        throws HibernateException, SQLException
    {
        if (value != null)
        {
            statement.setBigDecimal(index, toBigDecimal(value));
        }
        else
        {
            statement.setNull(index, SQL_TYPE);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[] sqlTypes ()
    {
      return (int[]) SQL_TYPES.clone();
    }
}
