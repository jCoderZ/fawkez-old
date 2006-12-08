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
package org.jcoderz.commons.doclet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.util.ArraysUtil;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.commons.util.StringUtil;
import org.jcoderz.commons.util.XmlUtil;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;

/**
 * A generic doclet that writes out all javadoc information as XML.
 *
 * @author Andreas Mandel
 */
public class XmlDoclet
      extends Doclet
{
   /** The full qualified name of this class. */
   private static final String CLASSNAME = XmlDoclet.class.getName();

   /** The logger to use. */
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   /** Static collector for doclet configuration. */
   private static XmlDocletConfig sConfig = new XmlDocletConfig();

   /** Concrete configuration for this xml doclet instance. */
   private final XmlDocletConfig mConfig;

   /** Output writer for current doclet. */
   private Writer mOut = null;

   private RootDoc mRootDoc;

   /**
    * Creates a new XmlDoclet with the given (fix) configuration.
    * @param config the config to use.
    */
   public XmlDoclet (XmlDocletConfig config)
   {
      try
      {
         mConfig = (XmlDocletConfig) config.clone();
      }
      catch (CloneNotSupportedException e)
      {
         throw new RuntimeException("Clone must be supported by config.", e);
      }
   }

   /** {@inheritDoc} */
   public static boolean start (RootDoc root)
   {
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, "start(RootDoc)", root);
      }
      boolean result = true;
      try
      {
         final XmlDoclet handler = new XmlDoclet(sConfig);
         handler.handle(root);
      }
      catch (Exception ex)
      {
         // CHECKME:
         result = false;
         root.printError(ex.getMessage());
         throw new RuntimeException("Internal processing error.", ex);
      }
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, "start(RootDoc)", Boolean.valueOf(result));
      }
      return result;
   }

   /** @see XmlDocletConfig#optionLength(String) */
   public static int optionLength (String option)
   {
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME, "optionLength(String)", option);
      }
      final int result = sConfig.optionLength(option);
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME, "optionLength(String)",
               new Integer(result));
      }
      return result;
   }

   public static boolean validOptions (String[][] arguments,
         DocErrorReporter reporter)
   {
      if (logger.isLoggable(Level.FINER))
      {
         logger.entering(CLASSNAME,
               "validOptions(String[][], DocErrorReporter)",
               new Object[] {ArraysUtil.toString(arguments), reporter});
      }
      final boolean result = true;
      final Iterator i = Arrays.asList(arguments).iterator();
      while (i.hasNext())
      {
         final String[] arg = (String[]) i.next();
         if (logger.isLoggable(Level.FINER))
         {
            logger.finest("About to parse argument: "
                  + ArraysUtil.toString(arg));
         }
         final String[] options = new String[arg.length - 1];
         System.arraycopy(arg, 1, options, 0, options.length);
         try
         {
            sConfig.parseOption(arg[0], options);
         }
         catch (ArgumentMalformedException ex)
         {
            // reporter.printError(ex.getMessage());
            // result = false;
            reporter.printNotice(ex.getMessage());
         }
      }
      if (logger.isLoggable(Level.FINER))
      {
         logger.exiting(CLASSNAME,
               "validOptions(String[][], DocErrorReporter)",
               Boolean.valueOf(result));
      }
      return result;
   }


   private void handle (RootDoc root)
         throws IOException
   {
      // create XML file
      FileOutputStream streamOut = null;
      mRootDoc = root;
      try
      {
         streamOut = new FileOutputStream(
               new File(mConfig.getOutputDirectory(), "javadoc.xml"), false);

         mOut = new OutputStreamWriter(streamOut, "utf-8");

         mOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         mOut.write("<javadoc>\n");

         final Set handledPackages = new HashSet();

         final PackageDoc[] packages = root.specifiedPackages();
         for (int i = 0; i < packages.length; ++i)
         {
            generatePackageElement(packages[i]);
            handledPackages.add(packages[i].name());
         }
         final ClassDoc[] classes = root.specifiedClasses();
         for (int i = 0; i < classes.length; ++i)
         {
            if (!handledPackages.contains(
                  classes[i].containingPackage().name()))
            {
               generatePackageElement(classes[i].containingPackage());
               handledPackages.add(classes[i].containingPackage().name());
            }
         }
         mOut.write("</javadoc>");


      }
      finally
      {
         IoUtil.close(mOut);
         IoUtil.close(streamOut);
         mOut = null;
      }
   }

   private void generatePackageElement (PackageDoc pkg)
         throws IOException
   {
      mOut.write("<package");
      generateDocHeader(pkg);
      mOut.write(">");
      generateDocBody(pkg);
      final ClassDoc[] classes = pkg.allClasses();
      for (int i = 0; i < classes.length; ++i)
      {
         generateClassElement(classes[i]);
      }
      mOut.write("</package>");
   }

   private void generateClassElement (ClassDoc cd)
         throws IOException
   {
      mOut.write("<class");
      generateClassHeader(cd);
      mOut.write(">\n");
      generateClassBody(cd);
      mOut.write("</class>\n");
   }

   private void generateClassBody (ClassDoc cd)
         throws IOException
   {
      generateProgramElementBody(cd);

      // Interfaces
      final ClassDoc[] interfaces = cd.interfaces();
      for (int i = 0; i < interfaces.length; ++i)
      {
         mOut.write("<interface");
         addAttribute("name", interfaces[i].qualifiedName());
         mOut.write("/>\n");
      }

      // InnerClasses
      final ClassDoc[] innerClass = cd.innerClasses();
      for (int i = 0; i < innerClass.length; ++i)
      {
         mOut.write("<inner");
         addAttribute("name", innerClass[i].qualifiedName());
         mOut.write("/>\n");
      }


      // Fields
      final FieldDoc[] fields = cd.fields();
      for (int i = 0; i < fields.length; ++i)
      {
         final FieldDoc field = fields[i];
         generateFieldHeader(field);
         generateFieldBody(field);
         mOut.write("</field>\n");
      }

      // Constructors
      final ConstructorDoc[] constructors = cd.constructors();
      for (int i = 0; i < constructors.length; ++i)
      {
         final ConstructorDoc constructor = constructors[i];
         generateConstructorHeader(constructor);
         generateConstructorBody(constructor);
         mOut.write("</constructor>\n");
      }


      // Methods.
      final MethodDoc[] methods = cd.methods();
      for (int i = 0; i < methods.length; ++i)
      {
         final MethodDoc method = methods[i];
         generateMethodHeader(method);
         generateMethodBody(method);
         mOut.write("</method>\n");
      }
   }

   private void generateMethodHeader (MethodDoc method)
         throws IOException
   {
      mOut.write("<method");

      addAttribute("abstract", method.isAbstract());
      if (method.overriddenClass() != null)
      {
         addAttribute("overridden-class",
               method.overriddenClass().qualifiedName());
      }

      if (method.overriddenMethod() != null)
      {
         addAttribute("overridden-method",
               method.overriddenMethod().qualifiedName());
      }

      generateExecutableMemberHeader(method);
      mOut.write(">\n");
   }

   private void generateMethodBody (MethodDoc method)
         throws IOException
   {
      if (method.returnType() != null
            && !method.returnType().qualifiedTypeName().equals("void"))
      {
         mOut.write("<return");
         addAttribute("type",
               method.returnType().qualifiedTypeName()
                  + method.returnType().dimension());
         mOut.write(">");
         final Tag[] returnTag = method.tags("return");
         if (returnTag != null && returnTag.length > 0)
         {
            generatedTagedTextElement(returnTag[0].inlineTags());
         }
         mOut.write("</return>\n");
      }
      generateExecutableMemberDocBody(method);
   }

   private void generatedTagedTextElement (final Tag[] tags)
         throws IOException
   {
      mOut.write("<doc>");
      generateTagedDoc(tags);
      mOut.write("</doc>\n");
   }

   private void generateConstructorBody (ConstructorDoc constructor)
         throws IOException
   {
      generateExecutableMemberDocBody(constructor);
   }

   private void generateExecutableMemberDocBody (
         ExecutableMemberDoc executableMemberDoc)
         throws IOException
   {
      generateMemberBody(executableMemberDoc);

      // Parameter
      final ParamTag[] tags = executableMemberDoc.paramTags();
      final Map parameterTags = new HashMap();
      for (int i = 0; i < tags.length; ++i)
      {
         if (parameterTags.containsKey(tags[i].parameterName()))
         {
            warn(tags[i].position(),
                  "Duplicate parameter ignored. " + tags[i],
                  executableMemberDoc);
         }
         else
         {
            // ONLY on tag per parametername
            parameterTags.put(tags[i].parameterName(), tags[i]);
         }
      }

      final Parameter[] parameters = executableMemberDoc.parameters();
      for (int i = 0; i < parameters.length; ++i)
      {
         final ParamTag parameterTag
               = (ParamTag) parameterTags.get(parameters[i].name());
//         if (parameterTag == null)
//         {
//            warn("No tag for parameter " + parameters[i].hashCode(),
//                  executableMemberDoc);
//         }
         generateParameterHeader(parameters[i], parameterTag);
         generateParameterBody(parameters[i], parameterTag);
         mOut.write("</parameter>\n");
         parameterTags.remove(parameters[i].name());
      }

      if (!parameterTags.isEmpty())
      {
         warn(executableMemberDoc.position(),
               "Unused parameter tags: " + parameterTags, executableMemberDoc);
      }

      // Exceptions
      final ThrowsTag[] tTags = executableMemberDoc.throwsTags();
      final Map throwsTags = new HashMap();
      for (int i = 0; i < tTags.length; ++i)
      {
         if (throwsTags.containsKey(tTags[i].exceptionName()))
         {
            warn(tTags[i].position(),
                  "Duplicate throws Tags ignored." + tTags[i].exceptionName(),
                  executableMemberDoc);
         }
         else
         {
            throwsTags.put(tTags[i].exceptionName(), tTags[i]);
         }
      }

      final ClassDoc[] exceptions = executableMemberDoc.thrownExceptions();
      for (int i = 0; i < exceptions.length; ++i)
      {
         ThrowsTag exceptionTag
               = (ThrowsTag) throwsTags.remove(exceptions[i].qualifiedName());
         if (exceptionTag == null)
         {
            exceptionTag = (ThrowsTag) throwsTags.remove(exceptions[i].name());
         }
         if (exceptionTag == null)
         {
//            warn(executableMemberDoc.position(),
//                  "No tag for exception " + exceptions[i].name(),
//                  executableMemberDoc);
         }
         generateThrowsHeader(exceptions[i], exceptionTag);
         generateThrowsBody(exceptions[i], exceptionTag);
         mOut.write("</throws>\n");
      }

      if (!throwsTags.isEmpty())
      {
         warn(executableMemberDoc.position(),
               "Unused throws tags: " + throwsTags, executableMemberDoc);
      }
   }

   private void generateMemberBody (MemberDoc member)
         throws IOException
   {
      generateProgramElementBody(member);
   }

   private void generateProgramElementBody (ProgramElementDoc programElement)
         throws IOException
   {
      generateDocBody(programElement);
   }

   private void generateThrowsHeader (ClassDoc parameter,
         ThrowsTag exceptionTag)
         throws IOException
   {
      mOut.write("<throws");
      addAttribute("type", parameter.qualifiedName());
      generateTagHeader(exceptionTag);
      mOut.write(">");
   }

   private void generateThrowsBody (ClassDoc parameter,
         ThrowsTag exceptionTag)
         throws IOException
   {
      if (exceptionTag != null)
      {
         generateTagBody(exceptionTag);
      }
   }

   private void generateParameterBody (Parameter parameter,
         ParamTag parameterTag)
         throws IOException
   {
      if (parameterTag != null)
      {
         generateTagBody(parameterTag);
      }
   }

   private void generateParameterHeader (Parameter parameter,
         ParamTag parameterTag)
         throws IOException
   {
      mOut.write("<parameter");
      addAttribute("name", parameter.name());
      addAttribute("type",
            parameter.type().qualifiedTypeName()
               + parameter.type().dimension());
      mOut.write(">");
   }

   private void generateConstructorHeader (ConstructorDoc cd)
         throws IOException
   {
      mOut.write("<constructor");
      generateExecutableMemberHeader(cd);
      mOut.write(">");
   }

   private void generateExecutableMemberHeader (ExecutableMemberDoc emd)
         throws IOException
   {
      addAttribute("flat-signature", emd.flatSignature());
      addAttribute("signature", emd.signature());
      addAttribute("native", emd.isNative());
      addAttribute("synchronized", emd.isSynchronized());
      generateMemberHeader(emd);
   }

   private void generateMemberHeader (MemberDoc md)
         throws IOException
   {
      addAttribute("synthetic", md.isSynthetic());
      generateProgramElementHeader(md);
   }

   private void generateFieldBody (FieldDoc fd)
         throws IOException
   {
      generateMemberBody(fd);
      // TODO: cd.serialFieldTags()
   }

   private void generateFieldHeader (FieldDoc cd)
         throws IOException
   {
      mOut.write("<field");
      addAttribute("type",
            cd.type().qualifiedTypeName() + cd.type().dimension());

      final String constant = cd.constantValueExpression();
      if (constant != null)
      {
         addAttribute("constant", true);
         addAttribute("value", constant);
      }
      addAttribute("volatile", cd.isVolatile());
      addAttribute("transient", cd.isTransient());
      generateMemberHeader(cd);
      mOut.write(">");
   }

   private void generateDocBody (Doc doc)
         throws IOException
   {
      mOut.write("<doc>");
      generateTagedDoc(doc.inlineTags());
      mOut.write("</doc>");

      final Tag[] tags = doc.tags();
      for (int i = 0; i < tags.length; i++)
      {
         final Tag tag = tags[i];
         // these tags are handled directly
         if (!tag.kind().equals("@param")
               && !tag.kind().equals("@return")
               && !tag.kind().equals("@throws"))
         {
            generateTagElement(tag);
         }
      }
   }

   private void generateTagHeader (Tag tag)
         throws IOException
   {
      if (tag != null)
      {
         if (tag.position() != null)
         {
            generateSourcePositionHeader(tag.position());
         }
         addAttribute("name", tag.name());

         if (tag instanceof SeeTag)
         {
            final SeeTag seeTag = (SeeTag) tag;
            addAttribute("label", seeTag.label());
            addAttribute("referenced-class", seeTag.referencedClassName());
            addAttribute("referenced-member", seeTag.referencedMemberName());
            if (seeTag.referencedPackage() != null)
            {
               addAttribute("referenced-package",
                     seeTag.referencedPackage().name());
            }
         }
         else
         {
            addAttribute("kind", tag.kind());
         }
      }
   }

   private void generateTagElement (Tag tag)
         throws IOException
   {
      if (tag instanceof SeeTag)
      {
         generateSeeElement((SeeTag) tag);
      }
      else if (tag != null)
      {
         mOut.write("<tag");
         generateTagHeader(tag);
         mOut.write(">");
         generateTagBody(tag);
         mOut.write("</tag>");
      }
   }

   private void generateTagBody (Tag tag)
         throws IOException
   {
      mOut.write("<doc>");
      generateTagedDoc(tag.inlineTags());
      mOut.write("</doc>");
   }

   private void generateTagedDoc (Tag[] tags)
         throws IOException
   {
      // build string to be tidied up
      final StringBuffer data = new StringBuffer();
      for (int i = 0; i < tags.length; i++)
      {
         final Tag inlineTag = tags[i];
         if (inlineTag instanceof SeeTag)
         {
            data.append("<a id='SEE_TAG_");
            data.append(Integer.toString(i));
            data.append("'></a>");
         }
         else
         {
            data.append(inlineTag.text());
         }
      }
      final String input = data.toString();
      final String clean;
      if (input.indexOf('<') != -1 || input.indexOf('&') != -1)
      {
         final HtmlCleaner cleaner = new HtmlCleaner();
         clean = cleaner.clean(data);
         final String warnings = cleaner.getWarnings();
         if (!StringUtil.isEmptyOrNull(warnings))
         {
            mRootDoc.printWarning("Input:" + input);
            mRootDoc.printWarning("Clean:" + clean);
            if (cleaner.hasErrors())
            {
               mRootDoc.printWarning(tags[0].position(), warnings);
               // mRootDoc.printError(tags[0].position(), warnings);
            }
            else
            {
               mRootDoc.printWarning(tags[0].position(), warnings);
            }
         }
      }
      else
      {
         clean = input;
      }
      data.setLength(0);
      data.append(clean);

      // now replace the a tag back...
      int pos;
      while ((pos = data.indexOf("<a id='SEE_TAG_")) != -1)
      {
         mOut.write(data.substring(0, pos));
         final int i = Integer.parseInt(data.substring(pos
               + "<a id='SEE_TAG_".length(), data.indexOf("'></a>", pos)));
         generateSeeElement((SeeTag) tags[i]);
         data.delete(0, data.indexOf("'></a>", pos) + "'></a>".length());
      }
      mOut.write(data.toString());
   }

   private void generateSeeElement (SeeTag tag)
         throws IOException
   {
      mOut.write("<see ");
      generateTagHeader(tag);
      mOut.write(">");
      final String label;
      if (!StringUtil.isNullOrEmpty(tag.label()))
      {
         label = tag.label();
      }
      else if (tag.referencedClassName() != null
            && tag.referencedMemberName() != null)
      {
         label = tag.referencedClassName() + "#" + tag.referencedMemberName();
      }
      else if (tag.referencedClassName() != null)
      {
         label = tag.referencedClassName();
      }
      else if (tag.referencedPackage() != null)
      {
         label = tag.referencedPackage().name();
      }
      else if (tag.referencedMemberName() != null)
      {
         label = tag.referencedMemberName();
      }
      else
      {
         label = "";
      }
      XmlUtil.escape(label);
      mOut.write("</see>");
   }

   private void generateSourcePositionHeader (SourcePosition position)
         throws IOException
   {
      if (position.file() != null)
      {
         addAttribute("file", position.file().getPath());
      }
      addAttribute("line", position.line());
      addAttribute("column", position.column());
   }

   private void generateClassHeader (ClassDoc cd)
         throws IOException
   {
      generateProgramElementHeader(cd);
      addAttribute("abstract", cd.isAbstract());
      addAttribute("externalizable", cd.isExternalizable());
      addAttribute("serializable", cd.isSerializable());
      if (cd.superclass() != null)
      {
         addAttribute("superclass", cd.superclass().qualifiedName());
      }
   }

   private void generateProgramElementHeader (ProgramElementDoc pe)
         throws IOException
   {
      addAttribute("qualified-name", pe.qualifiedName());

      if (pe.containingClass() != null)
      {
         addAttribute("containing-class",
               pe.containingClass().qualifiedName());
      }

      final String visibility;
      if (pe.isPrivate())
      {
         visibility = "private";
      }
      else if (pe.isProtected())
      {
         visibility = "protected";
      }
      else if (pe.isPackagePrivate())
      {
         visibility = "package private";
      }
      else if (pe.isPublic())
      {
         visibility = "public";
      }
      else
      {
         throw new RuntimeException("Could not detect access right " + pe);
      }
      addAttribute("visibility", visibility);
      addAttribute("static", pe.isStatic());
      addAttribute("final", pe.isFinal());
      addAttribute("modifiers", pe.modifiers());
      addAttribute("modifier-specifier",
            String.valueOf(pe.modifierSpecifier()));

      generateDocHeader(pe);
   }

   private void generateDocHeader (Doc doc)
         throws IOException
   {
      addAttribute("name", doc.name());
      addAttribute("exception", doc.isException());
      addAttribute("error", doc.isError());
      addAttribute("ordinary-class", doc.isOrdinaryClass());
      if (doc.position() != null)
      {
         generateSourcePositionHeader(doc.position());
      }
      if (doc.isInterface())
      {
         addAttribute("type", "interface");
      }
      else if (doc.isClass())
      {
         addAttribute("type", "class");
      }
   }

   private void addAttribute (String name, boolean value)
         throws IOException
   {
      if (value)
      {
         mOut.write(" ");
         mOut.write(name);
         mOut.write("='");
         mOut.write(name);
         mOut.write("'");
      }
   }

   private void addAttribute (String name, int value)
         throws IOException
   {
      addAttribute(name, Integer.toString(value));
   }

   private void addAttribute (String name, String value)
         throws IOException
   {
      if (!StringUtil.isEmptyOrNull(value))
      {
         mOut.write(" ");
         mOut.write(name);
         mOut.write("='");
         XmlUtil.attributeEscape(value);
         mOut.write("'");
      }
   }

   private void warn (SourcePosition pos, String string, Object parameter)
   {
      mRootDoc.printWarning(pos, string + " at " + parameter);
   }

}
