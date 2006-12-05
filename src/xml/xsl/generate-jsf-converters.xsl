<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id$

   Simple type generator. Support type-safe enumerations and restricted
   strings.

   Author: Michael Rumpf
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:redirect="http://xml.apache.org/xalan/redirect"
   xmlns:xalan2="http://xml.apache.org/xslt"
   extension-element-prefixes="redirect">

<xsl:include href="libcommon.xsl"/>

<xsl:output method="text"
            encoding="ISO-8859-1"/>

<xsl:strip-space elements="*"/>

<xsl:param name="outdir" select="'.'"/>

<xsl:template match="/">
   <!-- log to out -->
   Generating converters to directory <xsl:value-of select="$outdir"/>.
   Found <xsl:value-of select="count(//jsfConverter)"/> converters.
   <xsl:apply-templates/>
</xsl:template>

<xsl:template match="jsfConverter">
   <xsl:variable name="package.dir"><xsl:value-of
      select="$outdir"/>/<xsl:value-of
         select="translate(@package, '.', '/')"/></xsl:variable>

   <xsl:variable name="file"><xsl:value-of
      select="$package.dir"/>/<xsl:value-of
         select="@classname"/>.java</xsl:variable>

   <redirect:write file="{$file}">

   <xsl:call-template name="jsf-converter-generator">
      <xsl:with-param name="classname" select="@classname"/>
      <xsl:with-param name="package" select="@package"/>
      <xsl:with-param name="type" select="@type"/>
   </xsl:call-template>

   </redirect:write>
</xsl:template>

<!-- ===============================================================
     Value Object Generator
     =============================================================== -->
<xsl:template name="jsf-converter-generator">
   <xsl:param name="classname"/>
   <xsl:param name="package"/>
   <xsl:param name="type"/>
   <xsl:param name="javadoc"/>

   <xsl:variable name="name">
      <xsl:call-template name="asDisplayName">
         <xsl:with-param name="name" select="$classname"/>
      </xsl:call-template>
   </xsl:variable>

<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * Converts a <xsl:value-of select="$type"/> from and into a String.
 *
 * @jsf.converter name="<xsl:value-of select="$package"/>.<xsl:value-of select="$classname"/>"
 *
 * @author generated
 */
public class <xsl:value-of select="$classname"/>
      implements Converter
{
   private static final String CLASSNAME = <xsl:value-of select="$classname"/>.class.getName();
   private static final Logger logger = Logger.getLogger(CLASSNAME);

   /** {@inheritDoc} */
   public Object getAsObject (FacesContext context, UIComponent component,
         String value)
   {
      logger.entering(CLASSNAME, "getAsObject",
            new Object[] {context, component, value});

      if (value == null || value.length() == 0)
      {
         final FacesMessage msg = new FacesMessage(
               "A conversion error occured", "The value must not be empty!");
         msg.setSeverity(FacesMessage.SEVERITY_ERROR);
         throw new ConverterException(msg);
      }

      Object result = null;
      try
      {
         result = <xsl:value-of select="$type"/>.fromString(value);
      }
      catch (Exception ex)
      {
         final FacesMessage msg = new FacesMessage(
               "A conversion error occured", "The String '" + value
                  + "' can not be converted into an instance of type"
                  + " '<xsl:value-of select="$type"/>'!");
         msg.setSeverity(FacesMessage.SEVERITY_ERROR);
         throw new ConverterException(msg);
      }

      logger.exiting(CLASSNAME, "getAsObject", result);
      return result;
   }

   /** {@inheritDoc} */
   public String getAsString (FacesContext context, UIComponent component,
         Object value)
   {
      logger.entering(CLASSNAME, "getAsString",
            new Object[] {context, component, value});

      String result = null;
      try
      {
         if (value instanceof <xsl:value-of select="$type"/>)
         {
            result = ((<xsl:value-of select="$type"/>) value).toString();
         }
         else
         {
            logger.log(Level.WARNING, "Unknown type: "
                  + value.getClass().getName());
         }
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "An unexpected exception occured", ex);
      }

      logger.exiting(CLASSNAME, "getAsString", result);
      return result;
   }
}

</xsl:template>

</xsl:stylesheet>
