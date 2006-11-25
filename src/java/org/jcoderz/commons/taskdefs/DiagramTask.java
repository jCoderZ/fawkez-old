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
import org.apache.tools.ant.taskdefs.Javadoc.DocletParam;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet.NameEntry;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import org.jcoderz.phoenix.sqlparser.SqlToXml;

/**
 * Generates UML diagrams.
 *
 * @author Michael Griffel
 */
public class DiagramTask
      extends Task
{
   /** Task name. */
   public static final String NAME = "diagram";
   /** File name extension of graphviz dot files. */
   private static final String DOTTY_EXTENSION = ".dot";
   /** File name extension of Java files. */
   private static final String JAVA_EXTENSION = ".java";
   /** Font name for graphviz. */
   private static final String DEFAULT_FONTNAME = "verdana";
   /** Font size for graphviz. */
   private static final String DEFAULT_FONTSIZE = "7";

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
    * @param f the XML input file (log message info).
    */
   public void setIn (File f)
   {
      mInFile = f;
   }

   /**
    * Set the destination directory into which the result
    * files should be copied to. This parameter is required.
    * @param dir the name of the destination directory.
    **/
   public void setOut (File dir)
   {
       mOutDir = dir;
   }

   /**
    * Set the document type.
    * @param type the document type.
    */
   public void setType (String type)
   {
      // TODO:
   }

   /**
    * Set whether we should fail on an error.
    * @param b Whether we should fail on an error.
    */
   public void setFailonerror (boolean b)
   {
      mFailOnError = b;
   }

   /**
    * Set the source path to be used for this task run.
    * @param src an Ant FileSet object containing the compilation
    *        source path.
    */
   public void addSrc (SourceDirectory src)
   {
      mSources.add(src);
   }

   /**
    * Set the doclet path to be used for this task run.
    * @param path an Ant Path object containing the compilation
    *        source path.
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

         final DiagramSaxHandler handler = parse();
         log("Diagrams: " + handler.diagrams().toString(), Project.MSG_DEBUG);

         final Iterator iterator = handler.diagrams().iterator();
         while (iterator.hasNext())
         {
            final Diagram diagram = (Diagram) iterator.next();
            if (diagram.getType().equals("class"))
            {
               generateUmlDiagram(diagram);
            }
            else if (diagram.getType().equals("ER"))
            {
               generateEntityRelationshipDiagram(diagram);
            }
         }
         generateStateDiagram();
         AntTaskUtil.renderDotFiles(this, mOutDir, mFailOnError);
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


   private void generateEntityRelationshipDiagram (Diagram diagram)
   {
      final File inFile = new File(
            getProject().getBaseDir(), diagram.getFile());
      final File tmpFile;
      try
      {
         tmpFile = File.createTempFile("xdoc", ".tmp");
      }
      catch (IOException e)
      {
         throw new BuildException("Failed to create temp file: " + e, e);
      }
      final SqlToXml sqlToXml
            = new SqlToXml(inFile.getAbsolutePath(), tmpFile.getAbsolutePath());
      try
      {
         sqlToXml.transformSqlToXml();
      }
      catch (Exception e)
      {
         throw new BuildException("Failed to transform SQL '" + inFile
               + "' file to XML: " + e, e);
      }

      final File outFile = new File(mOutDir, diagram.getName() + ".dot");
      final XsltBasedTask t = new XsltBasedTask()
      {
         String getDefaultStyleSheet ()
         {
            return "generate-er-diagram.xsl";
         }
      };
      t.setProject(getProject());
      t.setTaskName("xml2dot");
      t.setFailonerror(mFailOnError);
      t.setIn(tmpFile);
      t.setForce(true);
      t.setDestdir(mOutDir);
      t.setOut(outFile);
      log("Generating ER diagram " + outFile, Project.MSG_VERBOSE);
      t.execute();
   }

   private void generateStateDiagram ()
   {
      final XsltBasedTask t = new XsltBasedTask()
      {
         String getDefaultStyleSheet ()
         {
            return "generate-state-diagram.xsl";
         }
      };
      t.setProject(getProject());
      t.setTaskName("xml2dot");
      t.setFailonerror(mFailOnError);
      t.setIn(mInFile);
      t.setForce(true);
      t.setDestdir(mOutDir);
      try
      {
         t.setOut(File.createTempFile("xdoc", ".tmp"));
      }
      catch (IOException e)
      {
         throw new BuildException("Cannot create temp file: " + e, e);
      }
      log("Generating state diagrams from file "
            + mInFile, Project.MSG_VERBOSE);
      t.execute();
   }
/*
   private void generateUmlDiagram (final Diagram diagram)
   {
      final Javadoc javadocTask = new Javadoc();
      javadocTask.setProject(getProject());
      javadocTask.setFailonerror(mFailOnError);
      javadocTask.setTaskName("umlgraph");
      javadocTask.setPackage(true);
      javadocTask.setClasspath(mDocletPath);
      javadocTask.setClasspath(Path.systemClasspath);
      for (final Iterator i = mSources.iterator(); i.hasNext();)
      {
         final SourceDirectory fs = (SourceDirectory) i.next();
         javadocTask.addFileset(addClasses(diagram, fs.getDir()));
      }
      final DocletInfo info = javadocTask.createDoclet();
      info.setProject(getProject());
      info.setName("gr.spinellis.umlgraph.doclet.UmlGraph");
      info.setPath(mDocletPath);
      addDocletParam(info, "-operations");
      addDocletParam(info, "-visibility");
      addDocletParam(info, "-types");
//      addDocletParam(info, "-noguillemot");
      addDocletParam(info, "-nodefontname", DEFAULT_FONTNAME);
      addDocletParam(info, "-nodefontsize", DEFAULT_FONTSIZE);
      addDocletParam(info, "-nodefontabstractname", DEFAULT_FONTNAME);
      addDocletParam(info, "-edgefontname", DEFAULT_FONTNAME);
      addDocletParam(info, "-edgefontsize", DEFAULT_FONTSIZE);
//      final File dotFile
//            = new File(mOutDir, diagram.getName() + DOTTY_EXTENSION);
//      dotFile.getParentFile().mkdirs();
//      addDocletParam(info, "-output", dotFile.getAbsolutePath());
    addDocletParam(info, "-d", mOutDir.getAbsolutePath());
    addDocletParam(info, "-output", diagram.getName() + DOTTY_EXTENSION);
    mOutDir.mkdirs();
      javadocTask.execute();
   }
*/

   private void generateUmlDiagram (final Diagram diagram)
   {
      final Javadoc javadocTask = new Javadoc();
      javadocTask.setProject(getProject());
      javadocTask.setFailonerror(mFailOnError);
      javadocTask.setTaskName("umlgraph");
      javadocTask.setPackage(true);
      javadocTask.setClasspath(mDocletPath);
      javadocTask.setClasspath(Path.systemClasspath);
      for (final Iterator i = mSources.iterator(); i.hasNext();)
      {
         final SourceDirectory fs = (SourceDirectory) i.next();
         javadocTask.addFileset(addClasses(diagram, fs.getDir()));
      }
      final DocletInfo info = javadocTask.createDoclet();
      info.setProject(getProject());
      info.setName("UmlGraph");
      info.setPath(mDocletPath);
      addDocletParam(info, "-operations");
      addDocletParam(info, "-visibility");
      addDocletParam(info, "-types");
      addDocletParam(info, "-noguillemot");
      addDocletParam(info, "-nodefontname", DEFAULT_FONTNAME);
      addDocletParam(info, "-nodefontsize", DEFAULT_FONTSIZE);
      addDocletParam(info, "-nodefontabstractname", DEFAULT_FONTNAME);
      addDocletParam(info, "-edgefontname", DEFAULT_FONTNAME);
      addDocletParam(info, "-edgefontsize", DEFAULT_FONTSIZE);
      final File dotFile
            = new File(mOutDir, diagram.getName() + DOTTY_EXTENSION);
      dotFile.getParentFile().mkdirs();
      addDocletParam(info, "-output", dotFile.getAbsolutePath());
      javadocTask.execute();
   }

   
   private FileSet addClasses (final Diagram diagram, File path)
   {
      final FileSet filez = new FileSet();
      filez.setDir(path);
      final Iterator i = diagram.classList().iterator();
      while (i.hasNext())
      {
         final String name = (String) i.next();
         final NameEntry entry = filez.createInclude();
         final String pathName = name.replaceAll("\\.", "/") + JAVA_EXTENSION;
         log("Adding Source file " + pathName, Project.MSG_VERBOSE);
         entry.setName(pathName);
      }
      return filez;
   }

   private void addDocletParam (DocletInfo info, String key)
   {
      final DocletParam param = info.createParam();
      param.setName(key);
   }

   private void addDocletParam (DocletInfo info, String key, String value)
   {
      final DocletParam param = info.createParam();
      param.setName(key);
      param.setValue(value);
   }

   private DiagramSaxHandler parse ()
   {
      final DiagramSaxHandler handler = new DiagramSaxHandler();
      try
      {
         // create a new XML parser
         final SAXParserFactory factory = SAXParserFactory.newInstance();
         factory.setNamespaceAware(true);
         factory.setValidating(true);
         final SAXParser parser = factory.newSAXParser();
         /*
         parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
         parser.setProperty(JAXP_SCHEMA_SOURCE,
               AppInfoTask.class.getResource(APP_INFO_SCHEMA).toExternalForm());
               */
         parser.parse(new InputSource(new FileInputStream(mInFile)), handler);
         log(mInFile + " parsed successfully.", Project.MSG_INFO);
      }
      catch (Exception e)
      {
         throw new BuildException("Failed to parse " + mInFile + ": " + e, e);
      }
      return handler;
   }

   /**
    * Checks the attributes provided by this class.
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
         throw new BuildException(
               "Missing mandatory attribute 'in'.", getLocation());
      }
      if (!mInFile.exists())
      {
         throw new BuildException(
               "Input file '" + mInFile + "' not found.", getLocation());
      }
   }


   private static class DiagramSaxHandler
         extends DefaultHandler
   {
      private final StringBuffer mBuffer = new StringBuffer();
      private boolean mCaptureCharacters = false;

      private final List mDiagrams = new ArrayList();
      private Diagram mCurrentDiagram = null;

      /**
       * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
       */
      public void startElement (String uri, String localName, String qName,
            Attributes attributes)
      {
         if ("diagram".equals(localName))
         {
            mCurrentDiagram = new Diagram(
                  attributes.getValue("name"), attributes.getValue("type"));
            if (attributes.getValue("file") != null)
            {
               mCurrentDiagram.setFile(attributes.getValue("file"));
            }
            mDiagrams.add(mCurrentDiagram);
         }
         else if ("class".equals(localName) && mCurrentDiagram != null)
         {
            mCurrentDiagram.add(attributes.getValue("name"));
         }
         else if ("description".equals(localName) && mCurrentDiagram != null)
         {
            captureCharacters();
         }
      }

      /**
       * @see org.xml.sax.ContentHandler#endElement(String, String, String)
       */
      public void endElement (String uri, String localName, String qName)
      {
         if ("diagram".equals(localName))
         {
            mCurrentDiagram = null;
         }
         else if ("description".equals(localName) && mCurrentDiagram != null)
         {
            mCurrentDiagram.setDescription(characters().trim());
         }

      }

      /**
       * @see org.xml.sax.ContentHandler#characters(char[], int, int)
       */
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
       * Returns the captured characters and <b>clears</b> the internal
       * buffer.
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
       * Returns a list of {@link Diagram}.
       * @return a list of {@link Diagram}.
       */
      public List diagrams ()
      {
         return Collections.unmodifiableList(mDiagrams);
      }

   }

   private static class Diagram
   {
      private final String mName;
      private final String mType;
      private final List mClasses = new ArrayList();
      private String mDescription = "";
      private String mFile;

      Diagram (String name, String type)
      {
         mName = name;
         mType = type;
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

      String getType ()
      {
         return mType;
      }

      String getDescription ()
      {
         return mDescription;
      }

      void setDescription (String description)
      {
         mDescription = description;
      }

      String getFile ()
      {
         return mFile;
      }

      void setFile (String file)
      {
         mFile = file;
      }

      /**
       * @see Object#toString()
       */
      public String toString ()
      {
         final StringBuffer sb = new StringBuffer();
         sb.append("diagram ");
         sb.append(mName);
         sb.append(" (");
         sb.append(mType);
         sb.append(") = ");
         sb.append(mClasses);
         sb.append(" description: '");
         sb.append(mDescription);
         sb.append("'");
         return sb.toString();
      }
   }
}
