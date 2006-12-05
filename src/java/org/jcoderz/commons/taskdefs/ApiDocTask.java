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
package org.jcoderz.commons.taskdefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Javadoc;
import org.apache.tools.ant.taskdefs.Javadoc.DocletInfo;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet.NameEntry;
import org.jcoderz.commons.util.IoUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Generates API documentation (DocBook format).
 *
 * @author Michael Griffel
 */
public class ApiDocTask
        extends Task
{
    /** Task name. */
    public static final String NAME = "apidoc";

    /** File name extension of Java files. */
    private static final String JAVA_EXTENSION = ".java";

    /** The output directory. */
    private File mOutDir;

    /** The input file. */
    private File mInFile;

    /** terminate ant build on error. */
    private boolean mFailOnError;

    /** Doclet path. */
    private Path mDocletPath;

    /** Source path - list of SourceDirectory. */
    private final List mSources = new ArrayList();

    /**
     * Sets the XML input file that contains the document.
     * 
     * @param f the XML input file (log message info).
     */
    public void setIn (File f)
    {
        mInFile = f;
    }

    /**
     * Set the destination directory into which the result files should
     * be copied to. This parameter is required.
     * 
     * @param dir the name of the destination directory.
     */
    public void setOut (File dir)
    {
        mOutDir = dir;
    }

    /**
     * Set whether we should fail on an error.
     * 
     * @param b Whether we should fail on an error.
     */
    public void setFailonerror (boolean b)
    {
        mFailOnError = b;
    }

    /**
     * Set the source path to be used for this task run.
     * 
     * @param src an Ant FileSet object containing the compilation
     *        source path.
     */
    public void addSrc (SourceDirectory src)
    {
        mSources.add(src);
    }

    /**
     * Set the doclet path to be used for this task run.
     * 
     * @param path an Ant Path object containing the compilation source
     *        path.
     */
    public void setDocletPath (Path path)
    {
        if (mDocletPath == null)
        {
            mDocletPath = path;
        }
        else
        {
            mDocletPath.add(path);
        }
    }

    /**
     * Execute this task.
     * 
     * @throws BuildException An building exception occurred.
     */
    public void execute ()
            throws BuildException
    {
        try
        {
            checkAttributes();
            final ApiDocSaxHandler handler = parse();
            log("APIs: " + handler.apiDocs().toString(), Project.MSG_DEBUG);
            final Iterator iterator = handler.apiDocs().iterator();
            while (iterator.hasNext())
            {
                final ApiDocType apiDoc = (ApiDocType) iterator.next();
                final File xmlFile = runXmlDoclet(apiDoc);
                log("Generated xmlFile " + xmlFile);
            }
        }
        catch (BuildException e)
        {
            if (mFailOnError)
            {
                throw e;
            }
            log(e.getMessage(), Project.MSG_ERR);
        }
    }

    private File runXmlDoclet (final ApiDocType apiDoc)
    {
        final Javadoc javadocTask = new Javadoc();
        javadocTask.setProject(getProject());
        javadocTask.setFailonerror(mFailOnError);
        javadocTask.setTaskName("xml-doclet");
        javadocTask.setPackage(true);
        javadocTask.setClasspath(mDocletPath);
        javadocTask.setClasspath(Path.systemClasspath);
        javadocTask.setDestdir(mOutDir);
        for (final Iterator i = mSources.iterator(); i.hasNext();)
        {
            final SourceDirectory fs = (SourceDirectory) i.next();
            javadocTask.addFileset(addClasses(apiDoc, fs.getDir()));
        }
        final DocletInfo info = javadocTask.createDoclet();
        info.setProject(getProject());
        info.setName("org.jcoderz.commons.doclet.XmlDoclet");
        info.setPath(mDocletPath);
        final File tmpFile = new File(mOutDir, "/javadoc.xml");
        javadocTask.execute();
        final File outFile = new File(mOutDir, apiDoc.getName() + ".xml");
        if (!tmpFile.renameTo(outFile))
        {
            try
            {
                // copy && delete
                IoUtil.copy(tmpFile, outFile);
                if (!tmpFile.delete())
                {
                    throw new BuildException("Cannot delete file " + tmpFile);
                }                
            }
            catch (IOException e)
            {
                throw new BuildException("Cannot move file " + tmpFile 
                        + " to " + outFile);
            }
        }
        try
        {
            IoUtil.copy(outFile, new File(outFile.getParent(), outFile
                    .getName()
                    + ".in"));
        }
        catch (IOException e)
        {
            throw new BuildException("Failed to copy file: " + outFile, e);
        }
        return outFile;
    }

    private FileSet addClasses (final ApiDocType diagram, File path)
    {
        final FileSet filez = new FileSet();
        filez.setDir(path);
        final Iterator i = diagram.classList().iterator();
        while (i.hasNext())
        {
            final String name = (String) i.next();
            final NameEntry entry = filez.createInclude();
            final String pathName = name.replaceAll("\\.", "/")
                    + JAVA_EXTENSION;
            log("Adding Source file " + pathName, Project.MSG_VERBOSE);
            entry.setName(pathName);
        }
        return filez;
    }

    private ApiDocSaxHandler parse ()
    {
        final ApiDocSaxHandler handler = new ApiDocSaxHandler();
        try
        {
            // create a new XML parser
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            final SAXParser parser = factory.newSAXParser();
            /*
             * parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
             * parser.setProperty(JAXP_SCHEMA_SOURCE,
             * AppInfoTask.class.getResource(APP_INFO_SCHEMA).toExternalForm());
             */
            parser
                    .parse(new InputSource(new FileInputStream(mInFile)),
                            handler);
            log(mInFile + " parsed successfully.", Project.MSG_INFO);
        }
        catch (Exception e)
        {
            throw new BuildException(
                    "Failed to parse " + mInFile + ": " + e, e);
        }
        return handler;
    }

    /**
     * Checks the attributes provided by this class.
     * 
     * @throws BuildException
     */
    private void checkAttributes ()
            throws BuildException
    {
        checkAttributeInFile();
    }

    private void checkAttributeInFile ()
    {
        if (mInFile == null)
        {
            throw new BuildException("Missing mandatory attribute 'in'.",
                    getLocation());
        }
        if (!mInFile.exists())
        {
            throw new BuildException("Input file '" + mInFile + "' not found.",
                    getLocation());
        }
    }

    private static class ApiDocSaxHandler
            extends DefaultHandler
    {
        private final StringBuffer mBuffer = new StringBuffer();

        private boolean mCaptureCharacters = false;

        private final List mApiDocElementList = new ArrayList();

        private ApiDocType mCurrentApiDocElement = null;

        /** {@inheritDoc} */
        public void startElement (String uri, String localName, String qName,
                Attributes attributes)
        {
            if ("apidoc".equals(localName))
            {
                mCurrentApiDocElement = new ApiDocType(attributes
                        .getValue("name"));
                mApiDocElementList.add(mCurrentApiDocElement);
            }
            else if ("class".equals(localName) && mCurrentApiDocElement != null)
            {
                mCurrentApiDocElement.add(attributes.getValue("name"));
            }
            else if ("description".equals(localName)
                    && mCurrentApiDocElement != null)
            {
                captureCharacters();
            }
        }

        /** {@inheritDoc} */
        public void endElement (String uri, String localName, String qName)
        {
            if ("apidoc".equals(localName))
            {
                mCurrentApiDocElement = null;
            }
            else if ("description".equals(localName)
                    && mCurrentApiDocElement != null)
            {
                mCurrentApiDocElement.setDescription(characters().trim());
            }
        }

        /** {@inheritDoc} */
        public void characters (char[] ch, int start, int length)
        {
            if (mCaptureCharacters)
            {
                mBuffer.append(ch, start, length);
            }
        }

        void captureCharacters ()
        {
            mCaptureCharacters = true;
        }

        /**
         * Returns the captured characters and <b>clears</b> the
         * internal buffer.
         * 
         * @return the captured characters.
         */
        String characters ()
        {
            final String result = mBuffer.toString();
            mBuffer.setLength(0);
            mCaptureCharacters = false;
            return result;
        }

        /**
         * Returns a list of {@link ApiDocType}.
         * 
         * @return a list of {@link ApiDocType}.
         */
        public List apiDocs ()
        {
            return Collections.unmodifiableList(mApiDocElementList);
        }
    }

    private static class ApiDocType
    {
        private final String mName;

        private final List mClasses = new ArrayList();

        private String mDescription = "";

        ApiDocType (String name)
        {
            mName = name;
        }

        void add (String clazz)
        {
            mClasses.add(clazz);
        }

        List classList ()
        {
            return Collections.unmodifiableList(mClasses);
        }

        String getName ()
        {
            return mName;
        }

        /** {@inheritDoc} */
        public String toString ()
        {
            final StringBuffer sb = new StringBuffer();
            sb.append("clazzes ");
            sb.append(mName);
            sb.append(" = ");
            sb.append(mClasses);
            sb.append(" description: '");
            sb.append(mDescription);
            sb.append("'");
            return sb.toString();
        }

        String getDescription ()
        {
            return mDescription;
        }

        void setDescription (String description)
        {
            mDescription = description;
        }
    }
}
