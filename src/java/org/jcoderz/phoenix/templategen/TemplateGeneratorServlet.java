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
package org.jcoderz.phoenix.templategen;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Template Generator Servlet.
 * 
 * @web.servlet name="templategen"
 * @web.servlet-mapping url-pattern="/templategen"
 * 
 * @author Albrecht Messner
 */
public class TemplateGeneratorServlet
      extends HttpServlet
{
    private static final long serialVersionUID = 1017987995368589070L;
    private static final String TEMPLATE_DIR = "templates";
   
   /**
    * @see javax.servlet.http.HttpServlet#doPost(
    *       javax.servlet.http.HttpServletRequest,
    *       javax.servlet.http.HttpServletResponse)
    */
   protected void doPost (
         HttpServletRequest request,
         HttpServletResponse response)
         throws ServletException, IOException
   {
      doService(request, response);
   }

   /**
    * @see javax.servlet.http.HttpServlet#doGet(
    *       javax.servlet.http.HttpServletRequest,
    *       javax.servlet.http.HttpServletResponse)
    */
   protected void doGet (
         HttpServletRequest request,
         HttpServletResponse response)
      throws ServletException, IOException
   {
      doService(request, response);
   }

   /**
    * @see javax.servlet.http.HttpServlet#doGet(
    *       HttpServletRequest,
    * HttpServletResponse)
    */
   protected void doService (
         HttpServletRequest request,
         HttpServletResponse response)
         throws ServletException, IOException
   {
      final String action = request.getParameter("action");
      if (action == null)
      {
         // give a selection of all registered templates
         listTemplates(response);
      }
      else if (action.equals("showform"))
      {
         // show the form for one template
         showTemplateForm(request, response);
      }
      else if (action.equals("parametrize"))
      {
         // parametrize the current template
         parametrizeTemplate(request, response);
      }
      else
      {
         // hey! unknown action!
         writeError(response,
               "Parameter 'action' has unknown value '" + action + "'");
      }
   }
   
   private void writeError (HttpServletResponse response, String errorText)
         throws IOException
   {
      response.setContentType("text/html");

      final PrintWriter pw = new PrintWriter(response.getOutputStream());
      pw.println("<HTML><HEAD><TITLE>Error</TITLE></HEAD>");
      pw.println("<BODY><H1>Error:</H1>");
      pw.println(errorText);
      pw.println("</BODY></HTML>");
      pw.flush();
   }
   
   private void parametrizeTemplate (
         HttpServletRequest request,
         HttpServletResponse response) throws ServletException, IOException
   {
      final TemplateGenerator templateGen = getTemplateGenerator(request);
      final String template = request.getParameter("template");

      final PrintWriter pw = new PrintWriter(response.getOutputStream());
      pw.println("Parametrizing template " + template);

      final Map paramMap = new HashMap();
      for (final Iterator it = templateGen.getParameterList().iterator();
            it.hasNext(); )
      {
         final Parameter param = (Parameter) it.next();
         final String paramValue = request.getParameter(param.getName());
         if (paramValue == null || paramValue.length() == 0)
         {
            writeError(response, "ERROR: value for parameter "
                  + param.getName() + " missing");
            return;
         }
         else
         {
            paramMap.put(param.getName(), paramValue);
         }
      }
      
      try
      {
         final byte[] templateZip = templateGen.parametrizeTemplates(paramMap);
         response.setContentType("application/zip");
         response.setHeader("Content-Disposition",
            "attachment; filename=\"" + template + ".zip\"");
         response.setContentLength(templateZip.length);
         response.getOutputStream().write(templateZip);
         response.getOutputStream().flush();
      }
      catch (TemplateGeneratorException e)
      {
         throw new ServletException("Template Generator Error", e);
      }
   }
   
   private void showTemplateForm (
         HttpServletRequest request,
         HttpServletResponse response) throws IOException, ServletException
   {

      final TemplateGenerator templateGen = getTemplateGenerator(request);
      final String template = request.getParameter("template");

      response.setContentType("text/html");
      final PrintWriter pw = new PrintWriter(response.getOutputStream());
      pw.println("<HTML><HEAD><TITLE>" + template + " Template Form"
            + "</TITLE></HEAD>");
      pw.println("<BODY>");
      pw.println("<H1>Template " + template + "</H1>");
      pw.println("<P>" + templateGen.getTemplateDescription() + "</P>");
      
      pw.println("<FORM ACTION='templategen?action=parametrize&template="
            + template + "' "
            + "METHOD='POST'>");
      pw.println("<table border='0'>");
      pw.println("<tr><th>Parameter</th><th>Value</th>"
            + "<th>Description</th></tr>");
      
      for (final Iterator it = templateGen.getParameterList().iterator();
            it.hasNext(); )
      {
         final Parameter param = (Parameter) it.next();
         
         pw.println("<tr>");
         pw.println("<td>" + param.getName() + "</td>");
         pw.println("<td>");
         
         if (param.isMultiLine())
         {
            pw.print("<textarea name='" + param.getName() + "'");
            pw.print(" rows='5' cols='30'>");
            if (param.getDefaultValue() != null)
            {
               pw.print(param.getDefaultValue());
            }
            pw.print("</textarea>");
         }
         else
         {
            pw.print("<input name='" + param.getName() + "'");
            pw.print(" size='30'");
            if (param.getDefaultValue() != null)
            {
               pw.print(" value='" + param.getDefaultValue() + "'");
            }
            pw.print(" maxlength='" + param.getMaxLength() + "'>");
         }

         pw.println("</td>");
         pw.println("<td>" + param.getDescription() + "</td>");
         pw.println("<tr>");
      }
      pw.println("</table>");
      pw.println("<INPUT TYPE='submit' VALUE='Do it'>");
      pw.println("</FORM>");
      
      pw.println("</BODY>");
      pw.println("</HTML>");
      pw.flush();
   }

   private void listTemplates (HttpServletResponse response)
         throws IOException, ServletException
   {
      response.setContentType("text/html");

      final File templateDir = getTemplateDirectory();
         
      final FileFilter templateFilter = new FileFilter()
      {
         public boolean accept (File file)
         {
            return file.getName().endsWith(".zip");
         }
      };
      final File[] templates = templateDir.listFiles(templateFilter);
         
      final PrintWriter pw = new PrintWriter(response.getOutputStream());
      pw.println("<HTML><HEAD><TITLE>Template List</TITLE></HEAD>");
      pw.println("<BODY>");
      if (templates != null && templates.length > 0)
      {
         pw.println("<Table width='60%'><tr><th>Template Name</th>"
               + "<th>Description</th></tr>");
         for (int i = 0; i < templates.length; i++)
         {
            pw.println("<tr>");
            final String template = templates[i].getName();
            final String baseName
                  = template.substring(
                        0, template.length() - ".zip".length());
               
            pw.print("<td><a href='templategen?action=showform&template="
                  + baseName + "'>");
            pw.print(baseName);
            pw.println("</a></td>");
            
            final TemplateZip tz 
                = new TemplateZip(templates[i].getAbsolutePath());
            try
            {
               tz.readTemplateFile();
            }
            catch (Exception e)
            {
               throw new ServletException("Failed to read template file "
                  + templates[i], e);
            }
            pw.println("<td>");
            pw.println(tz.getDescription().getDescription());
            pw.println("<td>");
            pw.println("</tr>");
         }
         pw.println("</table>");
      }
      else
      {
         pw.println("No templates found.");
      }
      pw.println("</BODY></HTML>");
      pw.flush();

   }

   private File getTemplateDirectory ()
   {
      final String realPath = getServletContext().getRealPath("/");
      final File baseDir = new File(realPath);
      final File templateDir = new File(baseDir, TEMPLATE_DIR);
      return templateDir;
   }
   
   private TemplateGenerator getTemplateGenerator (HttpServletRequest request)
         throws ServletException
   {
      final String template = request.getParameter("template");
      if (template == null)
      {
         throw new ServletException("Parameter 'template' missing");
      }

      final String templateFileName = template + ".zip";
      final File templateFile 
          = new File(getTemplateDirectory(), templateFileName);

      if (! templateFile.exists())
      {
         throw new ServletException("Template file " + templateFileName
               + " does not exist");
      }

      TemplateGenerator templateGen;
      try
      {
         templateGen = new TemplateGenerator(templateFile.getAbsolutePath());
      }
      catch (Exception e)
      {
         throw new ServletException("Failed to open template file", e);
      }
      return templateGen;
   }
}























