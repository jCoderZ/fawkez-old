/*
 * $Id: ApiDocTask.java 1011 2008-06-16 17:57:36Z amandel $
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
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jcoderz.commons.util.IoUtil;
import org.xml.sax.InputSource;

/**
 * This task allows to set a property based on an xpath expression,
 * evaluated on a given xml file.
 * 
 * <p>The tasks needs 3 properties to be set:
 * 
 * <dl>
 * <dt><code>name</code></dt><dd>The name of the property to be set.</dd>  
 * <dt><code>file</code></dt><dd>The xml file to be parsed.</dd>  
 * <dt><code>xpath</code></dt><dd>The xpath expression 
 *   to be evaluated on the given file.</dd>
 * </dl></p>  
 *
 * <p>The task can be defined using the following taskdef:
 * <pre>
 *     <taskdef name="xpathproperty" 
 *         classname="org.jcoderz.commons.taskdefs.XpathPropertyTask"
 *         classpath="fawkez-all.jar"/>
 * </pre></p>
 * 
 * <p>A possible use would be:
 * <pre>
 *     <xpathproperty 
 *        name="xpathtest" 
 *        xpath="/project/@name" 
 *        file="build.xml"/>
 * </pre>
 * Which would set the property <code>xpathtest</code> to <tt>fawkeZ</tt>
 * is applied to the fawkez build.xml.</p>
 * 
 * <p>The task requires java5 to function properly.</p>
 *
 * @author Andreas Mandel
 */
public class XpathPropertyTask
    extends Task
{
    /** Task name. */
    public static final String NAME = "XpathProperty";

    private File mFile;
    private String mXpath;
    private String mName;
    
    /**
     * Set the file to be parsed.
     * The file must exist and be readable.
     * @param xmlFile the file to be parsed.
     */
    public void setFile (File xmlFile)
    {
        mFile = xmlFile;
    }
    
    /**
     * Set the xpath expression to be evaluated.
     * See <a href="http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/xpath/package-summary.html">javadoc</a>
     * or <a href="http://www.w3.org/TR/xpath">XPath Recommendation</a> for 
     * details about XPath.
     * @param xpath the xpath expression to be evaluated.
     */
    public void setXpath (String xpath)
    {
        mXpath = xpath;
    }

    /**
     * Set the name of the property to be set.
     * @param name the name of the property to be set.
     */
    public void setName (String name)
    {
        mName = name;
    }
    
    /** Perform the evaluation. */
    public void execute ()
    {
        validate();
        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xPath = factory.newXPath();
        InputStream in = null;
        String result = null; 
        try
        {
            in = new FileInputStream(mFile);
            result = xPath.evaluate(
                mXpath, new InputSource(in));
        }
        catch (IOException ex)
        {
            throw new BuildException(
                "Coud not read '" + mFile 
                + "'. (" + ex.getMessage() + ")", ex);
        }
        catch (XPathExpressionException e)
        {
            throw new BuildException(
                "Coud not evauate xpath expression '" + mXpath 
                + "'. (" + e.getMessage() + ")", e);
        }
        finally
        {
            IoUtil.close(in);
        }
        getProject().setNewProperty(mName, result);
    }
    
    /** Plain validation of the attributes set. */
    private void validate ()
    {
        if (mFile == null || !mFile.exists() || !mFile.canRead())
        {
            throw new BuildException(
                "The file attribute must be set to an existing readable file.");
        }
        if (mXpath == null)
        {
            throw new BuildException(
                "The xpath attribute must be set.");
        }
        if (mName == null)
        {
            throw new BuildException(
                "The name attribute must be set.");
        }
    }
    
    
    
}
