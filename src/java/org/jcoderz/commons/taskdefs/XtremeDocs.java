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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.jcoderz.commons.util.Constants;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.commons.util.StringUtil;


/**
 * Xtreme Documentation Ant task.
 *
 * @author Michael Griffel
 */
public class XtremeDocs
    extends Task
{
    private static final String FORMAT_PDF = "PDF";

    private static final String FORMAT_HTML = "HTML";

    private static final String FORMAT_ALL = "ALL";

    private static final String FORMAT_NONE = "NONE";

    private static final String TYPE_QUALITY_REPORT = "Quality-Report";
    
    private static final String TYPE_KPI_STATS = "KPI-Stats";
    
    private static final String TYPE_KPI_REPORT = "KPI-Report";

    private static final String TYPE_RELEASE_NOTES = "Release-Notes";

    private static final String TYPE_TEST_SPEC = "TestSpec";

    private static final String TYPE_USE_CASE = "UseCase";

    private static final String TYPE_SAD = "SAD";

    private static final String IMAGE_DIR = "images";

    private static final String APIDOC_DIR = "apidoc";

    private static final String DEFAULT_COMPANY_NAME = "jCoderZ.org";

    private static final String DEFAULT_COMPANY_LOGO = "jcoderz-org";

    private static final boolean DEFAULT_VALIDATION_ONLY_FLAG = false;

    /** The output directory. */
    private File mOutDir;

    /** XEP home directory. */
    private File mXepHome;

    /** The input file. */
    private File mInFile;

    /** terminate ant build on error. */
    private boolean mFailOnError;

    /** Type of document. (SAD|UseCase) */
    private String mType;

    /** Format of document. (NONE|HTML|PDF|ALL) */
    private String mFormat;

    private String mTypeLowerCase;

    private final Path mDocletPath = new Path(getProject());

    private final Path mClassPath = new Path(getProject());

    private final List mFormatters = new ArrayList();

    /** Source path - list of SourceDirectory. */
    private final List mSources = new ArrayList();

    /** Cruise Control label */
    private String mCcLabel;

    /** company name */
    private String mCompanyName = DEFAULT_COMPANY_NAME;

    /** company logo without suffix */
    private String mCompanyLogo = DEFAULT_COMPANY_LOGO;

    /** flag for execution of validation tasks only */
    private boolean mValidationOnly = DEFAULT_VALIDATION_ONLY_FLAG;

    /** List of Variables set as Properties in the Transformer context. */
    private final List mTransformerProperties = new ArrayList();

    private HibernateInfoData mHibernateInfoData;

    /**
     * Add the given property to be sent to the transformer.
     *
     * @param var the property to be sent to the transformer.
     */
    public void addParam (org.apache.tools.ant.types.Environment.Variable var)
    {
        mTransformerProperties.add(var);
    }

    void setXdocTransformerParams (Transformer transformer)
    {
        // TODO: Implement this in the corresponding first pass style
        // sheets
        // or remove this!
        transformer.setParameter("basedir", getProject().getBaseDir()
            .toString());
        transformer.setParameter("cclabel", mCcLabel);
        transformer.setParameter("user", System.getProperty("user.name"));
        transformer.setParameter("companyname", mCompanyName);
        transformer.setParameter("companylogo", mCompanyLogo);
        final Iterator i = mTransformerProperties.iterator();
        while (i.hasNext())
        {
            final org.apache.tools.ant.types.Environment.Variable var = (org.apache.tools.ant.types.Environment.Variable) i
                .next();
            transformer.setParameter(var.getKey(), var.getValue());
        }
    }

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
     * Set the XEP home directory.
     *
     * @param dir the name of the XEP home directory.
     */
    public void setXephome (File dir)
    {
        mXepHome = dir;
    }

    /**
     * Set the document type.
     *
     * @param type the document type.
     */
    public void setType (String type)
    {
        mType = type;
        mTypeLowerCase = type.toLowerCase(Constants.SYSTEM_LOCALE);
    }

    /**
     * Set the document format.
     *
     * @param format the document format.
     */
    public void setFormat (String format)
    {
        mFormat = format;
    }

    /**
     * Set the document type.
     *
     * @param label the cruise control label.
     */
    public void setCclabel (String label)
    {
        mCcLabel = label;
    }

    /**
     * Set the name of the company or organisation.
     *
     * @param companyName The mCompanyName to set.
     */
    public void setCompanyName (String companyName)
    {
        mCompanyName = companyName;
    }

    /**
     * Set the flag, whether only validation should be executed or not.
     *
     * @param validationOnly The mValidationOnly to set.
     */
    public void setValidationOnly (boolean validationOnly)
    {
        mValidationOnly = validationOnly;
    }

    /**
     * Set the name of the company logo without suffix.
     *
     * @param companyLogo The mCompanyLogo to set.
     */
    public void setCompanyLogo (String companyLogo)
    {
        mCompanyLogo = companyLogo;
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
     * Additional path that is used to find the javadoc doclets.
     *
     * @return a path.
     */
    public Path createDocletpath ()
    {
        return mDocletPath;
    }

    /**
     * The classpath that is used to find the classes for the DocBook
     * formatters.
     *
     * @return a path.
     */
    public Path createClasspath ()
    {
        return mClassPath;
    }

    /**
     * Creates a new FormatterInfoData object used as nested element to
     * describe the DocBook formatters.
     *
     * @return a new FormatterInfoData object.
     */
    public FormatterInfoData createFormatter ()
    {
        final FormatterInfoData result = FormatterInfoData.create();
        mFormatters.add(result);
        return result;
    }

    /**
     * Creates a new HibernateInfoData object used as nested element to
     * describe the Hibernate Generator options.
     *
     * @return a new FormatterInfoData object.
     */
    public HibernateInfoData createHibernate ()
    {
        final HibernateInfoData hibernateInfoData = HibernateInfoData.create();
        mHibernateInfoData = hibernateInfoData;
        return hibernateInfoData;
    }

    /**
     * Returns the classpath element.
     *
     * @return the classpath element.
     */
    public Path getClassPath ()
    {
        return mClassPath;
    }

    /**
     * Returns <tt>true</tt> if this task (or subtasks) should fail on
     * any error; <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if this task (or subtasks) should fail on
     *         any error; <tt>false</tt> otherwise.
     */
    public boolean failOnError ()
    {
        return mFailOnError;
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
            log("Generating documentation into directory " + mOutDir);
            final File imageDir = new File(mOutDir, IMAGE_DIR);
            // convertPackageHtml2DocBook();
            final File filePassOne = transformPassOne(mInFile);
            if (TYPE_SAD.equals(mType))
            {
                generateApiDocs(filePassOne);
                generateSadDiagrams(filePassOne);
            }
            else if (TYPE_USE_CASE.equals(mType))
            {
                if (!mValidationOnly)
                {
                    generateUseCaseDiagrams(filePassOne, imageDir);
                    //exportToXmi(filePassOne, imageDir);
                    AntTaskUtil.renderDotFiles(this, imageDir, mFailOnError);
                    exportToHbCfg(filePassOne);
                    exportToHbm(filePassOne);
                }
            }
            else if (TYPE_KPI_STATS.equals(mType))
            {
                final File kpiFile = transformPassTwo(filePassOne);
                File newFile = new File(AntTaskUtil.stripFileExtension(AntTaskUtil
                    .stripFileExtension(kpiFile.getAbsolutePath())));
                kpiFile.renameTo(newFile);
            }
            else if (TYPE_KPI_REPORT.equals(mType))
            {
                generateKeyPerformanceDiagrams(filePassOne, imageDir);
                AntTaskUtil.renderGnuplotFiles(this, imageDir, mFailOnError);
            }
            else if (TYPE_TEST_SPEC.equals(mType))
            {
                // Nothing to do
            }
            else if (TYPE_QUALITY_REPORT.equals(mType))
            {
                // Nothing to do
            }
            else if (TYPE_RELEASE_NOTES.equals(mType))
            {
                // Nothing to do
            }
            else
            {
                throw new RuntimeException("Unsupported type " + mType);
            }
            
            if (TYPE_KPI_STATS.equals(mType))
            {
                // do nothing
            }
            else
            {
                if (!mValidationOnly)
                {
                    if (isOutputEnabled(FORMAT_HTML))
                    {
                        rasterizeSvgFiles(imageDir);
                    }
                    if (isOutputEnabled(FORMAT_PDF))
                    {
                        scaleSvgImages(imageDir);
                    }
                }
            }
            
            if (TYPE_TEST_SPEC.equals(mType))
            {
                renderDocbookFilesFromPassOne(filePassOne);
            }
            else if (TYPE_KPI_STATS.equals(mType))
            {
                // do nothing
            }
            else if (TYPE_KPI_REPORT.equals(mType))
            {
                final File docBookFile = transformPassTwo(filePassOne);
                // rendering is placed here, thus PassTwo will also generate diagrams
                AntTaskUtil.renderGnuplotFiles(this, imageDir, mFailOnError);
                renderDocBook(docBookFile, mInFile);
            }
            else
            {
                final File docBookFile = transformPassTwo(filePassOne);
                renderDocBook(docBookFile, mInFile);
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

    private boolean isOutputEnabled (String format)
    {
        final boolean result;
        if (StringUtil.isEmptyOrNull(mFormat)
            || mFormat.equals(FORMAT_ALL) || mFormat.equals(format))
        {
            result = true;
        }
        else if (mFormat.equals(FORMAT_NONE))
        {
            result = false;
        }
        else
        {
            result = true;
        }
        return result;
    }

    File getXepHome ()
    {
        return mXepHome;
    }

    private void renderDocBook (File docBookFile, File inFile)
    {
        for (final Iterator i = mFormatters.iterator(); i.hasNext();)
        {
            final FormatterInfoData f = (FormatterInfoData) i.next();
            final Formatter formatter = Formatter.getInstance(f);
            final File out = new File(docBookFile.getParentFile(), AntTaskUtil
                .stripFileExtension(inFile.getName())
                + "." + formatter.getFileExtension());
            if (isOutputEnabled(formatter.getInfoData().getType()))
            {
                formatter.transform(this, docBookFile, out);
            }
        }
    }

    private void renderDocbookFilesFromPassOne (File filePassOne)
    {
        File docbookDir = new File(filePassOne.getParent());
        transformPassTwo(filePassOne);
        log("Search files to render in directory: " + docbookDir.getParent());
        final File[] docbookFiles = docbookDir.listFiles(new FilenameFilter()
        {
            public boolean accept (File dir, String name)
            {
                final boolean result;
                if (name.endsWith(".p2"))
                {
                    result = true;
                }
                else
                {
                    result = false;
                }
                return result;
            }
        });
        if (docbookFiles != null)
        {
            for (int i = 0; i < docbookFiles.length; i++)
            {
                final File docbookFile = docbookFiles[i];
                final File passOneFile = new File(AntTaskUtil
                    .stripFileExtension(AntTaskUtil
                        .stripFileExtension(docbookFile.getName())));
                renderDocBook(docbookFile, passOneFile);
                log("Will render file: " + docbookFile.getName(), 
                    Project.MSG_VERBOSE);
            }
        }
        else
        {
            log("No .xml files found to render", Project.MSG_VERBOSE);
        }
    }

    private File transformPassOne (File in)
    {
        final XsltBasedTask task = new XsltBasedTask()
        {
            String getDefaultStyleSheet ()
            {
                return mTypeLowerCase + "-pass-one.xsl";
            }

            void setAdditionalTransformerParameters (Transformer transformer)
            {
                setXdocTransformerParams(transformer);
            }
        };
        task.setProject(getProject());
        task.setTaskName(mTypeLowerCase + "-p1");
        task.setIn(in);
        task.setForce(true); // FIXME
        final File outFile = new File(mOutDir, in.getName() + ".p1");
        task.setOut(outFile);
        task.setFailonerror(mFailOnError);
        task.setDestdir(outFile.getParentFile());
        task.execute();
        return outFile;
    }

    private File transformPassTwo (File filePassOne)
    {
        final XsltBasedTask task = new XsltBasedTask()
        {
            String getDefaultStyleSheet ()
            {
                return mTypeLowerCase + "-pass-two.xsl";
            }

            void setAdditionalTransformerParameters (Transformer transformer)
            {
                setXdocTransformerParams(transformer);
            }
        };
        task.setProject(getProject());
        task.setTaskName(mTypeLowerCase + "-p2");
        task.setIn(filePassOne);
        task.setForce(true); // FIXME
        final File outFile = new File(mOutDir, filePassOne.getName() + ".p2");
        task.setOut(outFile);
        task.setFailonerror(mFailOnError);
        task.setDestdir(outFile.getParentFile());
        task.execute();
        return outFile;
    }

    private void generateUseCaseDiagrams (File filePassOne, final File imageDir)
    {
        if (isOutputEnabled(FORMAT_PDF) || isOutputEnabled(FORMAT_HTML))
        {
            final XsltBasedTask task = new XsltBasedTask()
            {
                String getDefaultStyleSheet ()
                {
                    return "usecase_diagrams.xsl";
                }

                void setAdditionalTransformerParameters (
                    Transformer transformer)
                {
                    transformer.setParameter(
                        "basedir", getProject().getBaseDir().toString());
                    transformer.setParameter("imagedir", imageDir.toString());
                }
            };
            task.setProject(getProject());
            task.setTaskName("diagrams");
            task.setIn(filePassOne);
            task.setForce(true); // FIXME
            final File outFile 
                = new File(mOutDir, "use-case-diagrams" + ".tmp");
            task.setOut(outFile);
            task.setFailonerror(mFailOnError);
            task.setDestdir(outFile.getParentFile());
            task.execute();
        }
    }
    
    private void generateKeyPerformanceDiagrams (
        File filePassOne, final File imageDir)
    {
        if (isOutputEnabled(FORMAT_PDF) || isOutputEnabled(FORMAT_HTML))
        {
            final XsltBasedTask task = new XsltBasedTask()
            {
                String getDefaultStyleSheet ()
                {
                    return "key-performance-diagrams.xsl";
                }

                void setAdditionalTransformerParameters (
                    Transformer transformer)
                {
                    setXdocTransformerParams(transformer);
                    transformer.setParameter("imagedir", imageDir.toString());
                }
            };
            task.setProject(getProject());
            task.setTaskName("diagrams");
            task.setIn(filePassOne);
            task.setForce(true); // FIXME
            final File outFile 
                = new File(mOutDir, "key-performance-diagrams" + ".tmp");
            task.setOut(outFile);
            task.setFailonerror(mFailOnError);
            task.setDestdir(outFile.getParentFile());
            task.execute();
        }
    }

    private void exportToXmi (File filePassOne, final File imageDir)
    {
        final XsltBasedTask task = new XsltBasedTask()
        {
            String getDefaultStyleSheet ()
            {
                return "usecase_xmi_export.xsl";
            }

            void setAdditionalTransformerParameters (Transformer transformer)
            {
                transformer.setParameter("basedir", getProject().getBaseDir()
                    .toString());
                transformer.setParameter("imagedir", imageDir.toString());
            }
        };
        task.setProject(getProject());
        task.setTaskName("uc-xmi");
        task.setIn(filePassOne);
        task.setForce(true); // FIXME
        final File outFile = new File(mOutDir, "use-case-xmi" + ".tmp");
        task.setOut(outFile);
        task.setFailonerror(mFailOnError);
        task.setDestdir(outFile.getParentFile());
        task.execute();
    }

    private void exportToHbm (File filePassOne)
    {
        final String targetdir = buildTargetDir(filePassOne.getName());
        File dir = new File(targetdir);
        dir.mkdir();

        final XsltBasedTask task = new XsltBasedTask()
        {
            String getDefaultStyleSheet ()
            {
                return "usecase_hbm_export.xsl";
            }

            void setAdditionalTransformerParameters (Transformer transformer)
            {
                transformer.setParameter("targetdir", targetdir);
                transformer.setParameter("package-prefix", 
                    mHibernateInfoData.getPackagePrefix());
                transformer.setParameter("package-suffix", 
                    mHibernateInfoData.getPackageSuffix());
                transformer.setParameter("tablename-prefix", 
                    mHibernateInfoData.getTableNamePrefix());
                transformer.setParameter("tablename-suffix", 
                    mHibernateInfoData.getTableNameSuffix());
                transformer.setParameter("foreign-key-prefix", 
                    mHibernateInfoData.getForeignKeyPrefix());
                transformer.setParameter("foreign-key-suffix", 
                    mHibernateInfoData.getForeignKeySuffix());
            }
        };
        task.setProject(getProject());
        task.setTaskName("uc-hbm");
        task.setIn(filePassOne);
        task.setForce(true); // FIXME
        final File outFile = new File(mOutDir, "use-case-hbm" + ".tmp");
        task.setOut(outFile);
        task.setFailonerror(mFailOnError);
        task.setDestdir(outFile.getParentFile());
        task.execute();
    }

    private void exportToHbCfg (File filePassOne)
    {
        final String targetdir = buildTargetDir(filePassOne.getName());
        File dir = new File(targetdir);
        dir.mkdir();

        final XsltBasedTask task = new XsltBasedTask()
        {
            String getDefaultStyleSheet ()
            {
                return "usecase_hbcfg_export.xsl";
            }

            void setAdditionalTransformerParameters (Transformer transformer)
            {
                transformer.setParameter("targetdir", targetdir);
                transformer.setParameter("session-factory", 
                    mHibernateInfoData.getSessionFactory());
            }
        };
        task.setProject(getProject());
        task.setTaskName("uc-hbm");
        task.setIn(filePassOne);
        task.setForce(true); // FIXME
        final File outFile = new File(mOutDir, "use-case-hbcfg" + ".tmp");
        task.setOut(outFile);
        task.setFailonerror(mFailOnError);
        task.setDestdir(outFile.getParentFile());
        task.execute();
    }

    private String buildTargetDir (String name)
    {
        String strippedName = name.substring(0, name.lastIndexOf(".xml.p1"));
        String dir = mOutDir + File.separator + strippedName
                       + File.separator + "hibernate";
        return dir;
    }

    private void generateSadDiagrams (File in)
    {
        final DiagramTask task = new DiagramTask();
        task.setTaskName(DiagramTask.NAME);
        task.setProject(getProject());
        task.setIn(in);
        for (final Iterator i = mSources.iterator(); i.hasNext();)
        {
            final SourceDirectory src = (SourceDirectory) i.next();
            task.addSrc(src);
        }
        task.setFailonerror(mFailOnError);
        final File out = new File(mOutDir, IMAGE_DIR);
        task.setOut(out);
        task.setDocletPath(mDocletPath);
        task.execute();
    }

    private void scaleSvgImages (File dir)
    {
        final XsltBatchProcessor x = new XsltBatchProcessor();
        x.setProject(getProject());
        x.setTaskName("svg-scale");
        x.setFailonerror(mFailOnError);
        x.setXsl("svg-image-transform.xsl");
        x.resolveExternalEntities(false);
        final FileSet fs = new FileSet();
        fs.setDir(dir);
        fs.setIncludes("*.svg");
        x.addFiles(fs);
        x.execute();
    }

    private void generateApiDocs (File in)
    {
        final ApiDocTask task = new ApiDocTask();
        task.setTaskName(ApiDocTask.NAME);
        task.setProject(getProject());
        task.setIn(in);
        for (final Iterator i = mSources.iterator(); i.hasNext();)
        {
            final SourceDirectory src = (SourceDirectory) i.next();
            task.addSrc(src);
        }
        task.setFailonerror(mFailOnError);
        final File out = new File(mOutDir, APIDOC_DIR);
        out.mkdirs();
        task.setOut(out);
        task.setDocletPath(mDocletPath);
        task.execute();
        // Transform to DocBook
        final XsltBatchProcessor x = new XsltBatchProcessor();
        x.setProject(getProject());
        x.setTaskName("java2docbook");
        x.setFailonerror(mFailOnError);
        x.setXsl("java2docbook.xsl");
        final FileSet fs = new FileSet();
        fs.setDir(out);
        fs.setIncludes("*.xml");
        x.addFiles(fs);
        x.execute();
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
        XsltBasedTask.checkXercesVersion(this);
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

    private void rasterizeSvgFiles (File directory)
    {
        final File[] svgFiles = directory.listFiles(new FilenameFilter()
        {
            public boolean accept (File dir, String name)
            {
                final boolean result;
                if (name.endsWith(".svg"))
                {
                    result = true;
                }
                else
                {
                    result = false;
                }
                return result;
            }
        });
        log("Creating raster images for " + svgFiles.length
            + " images", Project.MSG_INFO);
        for (int i = 0; i < svgFiles.length; i++)
        {
            final File svgFile = svgFiles[i];
            try
            {
                log("Creating raster image for '" + svgFile.getCanonicalPath()
                    + "'", Project.MSG_VERBOSE);
                /*
                 * final String[] args = new String[] { "-maxw",
                 * "700.0", "-scriptSecurityOff",
                 * svgFile.getCanonicalPath()}; final Main conv = new
                 * Main(args); // execute the conversion conv.execute();
                 */
                final File pngFile = new File(svgFile.getParentFile(), svgFile
                    .getName().substring(0, svgFile.getName().indexOf('.'))
                    + ".png");
                Rasterizer.rasterize(svgFile, pngFile);
            }
            catch (Exception ex)
            {
                throw new BuildException(
                    "Could not generate raster image for '" + svgFile.getName()
                        + "' (" + ex + ")");
            }
        }
    }

    private static final class Rasterizer
    {
        private static final PNGTranscoder TRANSCODER;
        static
        {
            TRANSCODER = new PNGTranscoder();
            // force Xerces as XML Reader
            TRANSCODER.addTranscodingHint(
                XMLAbstractTranscoder.KEY_XML_PARSER_CLASSNAME,
                "org.apache.xerces.parsers.SAXParser");
        }

        private Rasterizer ()
        {
            // utility class -- provides only static methods
        }

        public static void rasterize (File in, File out)
            throws TranscoderException, IOException
        {
            final OutputStream ostream = new FileOutputStream(out);
            try
            {
                // Create the transcoder input
                final TranscoderInput input = new TranscoderInput(
                    new FileInputStream(in));
                input.setURI(in.toURL().toExternalForm());
                // Create the transcoder output
                final TranscoderOutput output = new TranscoderOutput(ostream);
                // Transform the SVG document into a PNG image
                TRANSCODER.transcode(input, output);
                ostream.flush();
            }
            finally
            {
                IoUtil.close(ostream);
            }
        }
    }

    /**
     * The Class FormatterInfoData.
     */
    public static class FormatterInfoData
    {
        private File mStyleSheet;

        private File mCascadingStyleSheet;

        private String mType;

        /**
         * Gets the cascading style sheet.
         *
         * @return the cascading style sheet
         */
        public File getCascadingStyleSheet ()
        {
            return mCascadingStyleSheet;
        }

        /**
         * Sets the cascading style sheet.
         *
         * @param cascadingStyleSheet the new cascading style sheet
         */
        public void setCss (File cascadingStyleSheet)
        {
            mCascadingStyleSheet = cascadingStyleSheet;
        }

        /**
         * Gets the style sheet.
         *
         * @return the style sheet
         */
        public File getStyleSheet ()
        {
            return mStyleSheet;
        }

        /**
         * Sets the style.
         *
         * @param styleSheet the new style
         */
        public void setStyle (File styleSheet)
        {
            mStyleSheet = styleSheet;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public String getType ()
        {
            return mType;
        }

        /**
         * Sets the type.
         *
         * @param type the new type
         */
        public void setType (String type)
        {
            mType = type;
        }

        /**
         * Create the formatter info data.
         *
         * @return the formatter info data
         */
        public static FormatterInfoData create ()
        {
            return new FormatterInfoData();
        }
    }

    /**
     * The Class HibernateInfoData.
     */
    public static class HibernateInfoData
    {
        private static final String DEFAULT_PACKAGE_PREFIX 
            = "org.jcoderz.hibernate";
        private static final String DEFAULT_PACKAGE_SUFFIX = "";

        private static final String DEFAULT_TABLE_NAME_PREFIX = "JC_";
        private static final String DEFAULT_TABLE_NAME_SUFFIX = "S";

        private static final String DEFAULT_FOREIGN_KEY_PREFIX = "FK_";
        private static final String DEFAULT_FOREIGN_KEY_SUFFIX = "";

        private static final String DEFAULT_HIBERNATE_SESSION_FACTORY 
            = "Default";

        /** Package prefix. */
        private String mPackagePrefix = DEFAULT_PACKAGE_PREFIX;

        /** Package suffix. */
        private String mPackageSuffix = DEFAULT_PACKAGE_SUFFIX;

        /** Table name prefix. */
        private String mTableNamePrefix = DEFAULT_TABLE_NAME_PREFIX;

        /** Table name suffix. */
        private String mTableNameSuffix = DEFAULT_TABLE_NAME_SUFFIX;

        /** Foreign key name prefix. */
        private String mForeignKeyPrefix = DEFAULT_FOREIGN_KEY_PREFIX;

        /** Foreign key name suffix. */
        private String mForeignKeySuffix = DEFAULT_FOREIGN_KEY_SUFFIX;

        /** Hibernate session factory. */
        private String mHibernateSessionFactory = DEFAULT_HIBERNATE_SESSION_FACTORY;

        /**
         * Create the hibernate info data.
         *
         * @return the hibernate info data
         */
        public static HibernateInfoData create ()
        {
            return new HibernateInfoData();
        }

        /**
         * Sets the session factory.
         *
         * @param hibernateSessionFactory the new session factory
         */
        public void setSessionFactory (
            String hibernateSessionFactory)
        {
            this.mHibernateSessionFactory = hibernateSessionFactory;
        }

        /**
         * Gets the session factory.
         *
         * @return the session factory
         */
        public String getSessionFactory ()
        {
            return mHibernateSessionFactory;
        }

        /**
         * Sets the package prefix.
         *
         * @param packagePrefix the new package prefix
         */
        public void setPackagePrefix (String packagePrefix)
        {
            mPackagePrefix = packagePrefix;
        }

        /**
         * Gets the package prefix.
         *
         * @return the package prefix
         */
        public String getPackagePrefix ()
        {
            return mPackagePrefix;
        }

        /**
         * Sets the package suffix.
         *
         * @param packageSuffix the new package suffix
         */
        public void setPackageSuffix (String packageSuffix)
        {
            mPackageSuffix = packageSuffix;
        }

        /**
         * Gets the package suffix.
         *
         * @return the package suffix
         */
        public String getPackageSuffix ()
        {
            return mPackageSuffix;
        }

        /**
         * Sets the table name prefix.
         *
         * @param mTableNamePrefix the new table name prefix
         */
        public void setTableNamePrefix (String tableNamePrefix)
        {
            mTableNamePrefix = tableNamePrefix;
        }

        /**
         * Gets the table name prefix.
         *
         * @return the table name prefix
         */
        public String getTableNamePrefix ()
        {
            return mTableNamePrefix;
        }

        /**
         * Sets the table name suffix.
         *
         * @param mTableNameSuffix the new table name suffix
         */
        public void setTableNameSuffix (String tableNameSuffix)
        {
            mTableNameSuffix = tableNameSuffix;
        }

        /**
         * Gets the table name suffix.
         *
         * @return the table name suffix
         */
        public String getTableNameSuffix ()
        {
            return mTableNameSuffix;
        }

        /**
         * Sets the foreign key prefix.
         *
         * @param mForeignKeyPrefix the new foreign key prefix
         */
        public void setForeignKeyPrefix (String foreignKeyPrefix)
        {
            mForeignKeyPrefix = foreignKeyPrefix;
        }

        /**
         * Gets the foreign key prefix.
         *
         * @return the foreign key prefix
         */
        public String getForeignKeyPrefix ()
        {
            return mForeignKeyPrefix;
        }

        /**
         * Sets the foreign key suffix.
         *
         * @param mForeignKeySuffix the new foreign key suffix
         */
        public void setForeignKeySuffix (String foreignKeySuffix)
        {
            mForeignKeySuffix = foreignKeySuffix;
        }

        public String getForeignKeySuffix ()
        {
            return mForeignKeySuffix;
        }

    }

}
