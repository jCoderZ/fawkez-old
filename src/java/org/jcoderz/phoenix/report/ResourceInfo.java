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
package org.jcoderz.phoenix.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.util.Assert;
import org.jcoderz.commons.util.HashCode;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.commons.util.ObjectUtil;
import org.jcoderz.commons.util.StringUtil;

/**
 * This class holds resource information about a Java class.
 *
 * @author Michael Griffel
 */
public final class ResourceInfo
{
   /** holds a map from resource name to ResourceInfo */
    private static final Map RESOURCES = new HashMap();

    private static final transient String CLASSNAME = ResourceInfo.class
            .getName();
    private static final transient Logger logger = Logger.getLogger(CLASSNAME);

    private final String mResourceName;
    private final String mPackage;
    private final String mSourcDir;
    private final String mClassname;

    /** Lazy initialized number of source lines value. */
    private int mLinesOfCode = -1;
    /** Lazy initialized hash code value. */
    private int mHashCode = -1;

    private ResourceInfo (String name, String pkg, String sourceDir)
    {
        Assert.notNull(name, "name");
        Assert.notNull(sourceDir, "sourceDir");

        mResourceName = name;
        if (pkg != null)
        {
            mPackage = pkg;
        }
        else
        {
            mPackage = StringUtil.EMPTY_STRING;
        }
        mSourcDir = sourceDir;
        mClassname = determineClassName(name);
    }

    /**
     * Registers the a new resource with the given parameters.
     * @param name the name of the resource.
     * @param pkg the Java package of the resource.
     * @param sourceDir the source directory of the resource.
     * @return the registered resource info.
     */
    public static ResourceInfo register (String name, String pkg,
            String sourceDir)
    {
        final ResourceInfo result;
        if (RESOURCES.get(name) == null)
        {
            result = new ResourceInfo(name, pkg, sourceDir);
            add(name, result);
        }
        else
        {
            result = (ResourceInfo) RESOURCES.get(name);
            final ResourceInfo newInfo = new ResourceInfo(name, pkg, sourceDir);
            // sanity check
            if (!newInfo.equals(result))
            {
                throw new RuntimeException("Ups, the ResourceInfo w/ the name "
                        + name
                        + " is already registered with different parameters: "
                        + result);
            }
        }
        return result;
    }

    /**
     * Locates the resource with the given name.
     *
     * @param name resource name.
     * @return the resource for the given name or <tt>null</tt> if not found.
     */
    public static ResourceInfo lookup (String name)
    {
        if (!RESOURCES.containsKey(name))
        {
            logger.finer("### ResourceInfo not found for '" + name + "'");
        }
        return (ResourceInfo) RESOURCES.get(name);
    }

    /**
     * Returns the number of lines for the given file <tt>filename</tt>.
     * @param fileName the name of the file.
     * @return the number of lines.
     * @throws IOException in case of an I/O problem.
     * @throws FileNotFoundException in case the named file does
     *      not exists or is a directory.
     */
    public static int countLinesOfCode (String fileName)
            throws IOException, FileNotFoundException
    {
        int counter = 0;
        final BufferedReader reader
                = new BufferedReader(new FileReader(fileName));
        try
        {
            while (reader.readLine() != null)
            {
                ++counter;
            }
        }
        finally
        {
            IoUtil.close(reader);
        }
        return counter;
    }

    /** {@inheritDoc} */
    public boolean equals (Object obj)
    {
        boolean result = false;
        if (this == obj)
        {
            result = true;
        }
        else if (obj instanceof ResourceInfo)
        {
            final ResourceInfo o = (ResourceInfo) obj;
            result = ObjectUtil.equals(mResourceName, o.getResourceName())
                && ObjectUtil.equals(mPackage, o.getPackage())
                && ObjectUtil.equals(mSourcDir, o.getSourcDir());
        }
        else
        {
            result = false;
        }
        return result;
    }

    /** {@inheritDoc} */
    public int hashCode ()
    {
        if (mHashCode == -1)
        {
            final HashCode hashCode = new HashCode();
            hashCode.hash(mResourceName);
            hashCode.hash(mPackage);
            hashCode.hash(mSourcDir);
            mHashCode  = hashCode.hashCode();
        }
        return mHashCode;
    }

    /**
     * Returns the linesOfCode.
     *
     * @return the linesOfCode.
     */
    public final int getLinesOfCode ()
    {
        if (mLinesOfCode == -1)
        {
            try
            {
                mLinesOfCode = countLinesOfCode(mResourceName);
            }
            catch (IOException e)
            {
                mLinesOfCode = 0;
                logger.log(Level.FINER,
                        "Cannot read the resource with the name "
                                + mResourceName, e);
            }
        }
        return mLinesOfCode;
    }

    /**
     * Returns the package.
     *
     * @return the package.
     */
    public final String getPackage ()
    {
        return mPackage;
    }

    /**
     * Returns the resourceName.
     *
     * @return the resourceName.
     */
    public final String getResourceName ()
    {
        return mResourceName;
    }

    /**
     * Returns the sourcDir.
     *
     * @return the sourcDir.
     */
    public final String getSourcDir ()
    {
        return mSourcDir;
    }

    /** {@inheritDoc} */
    public String toString ()
    {
        return "[ResourceInfo: name=" + mResourceName + ", pkg=" + mPackage
                + ", sourceDir=" + mSourcDir + "]";
    }

    /**
     * Returns the class name.
     * @return the class name.
     */
    public final String getClassname ()
    {
        return mClassname;
    }


    private String determineClassName (String name)
    {
        String result = null;

        final String magic = ".java";
        if (name.endsWith(magic))
        {
            final int lastSlashPos = name.lastIndexOf(File.separator);
            if (lastSlashPos != -1)
            {
                result = name.substring(lastSlashPos + File.separator.length());
                result = result.substring(0, result.indexOf(magic));
            }
        }
        return result;
    }

    private static void add (String name, ResourceInfo info)
    {
        RESOURCES.put(name, info);
    }

}
