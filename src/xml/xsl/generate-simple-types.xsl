<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id: generate-simple-types.xsl,v 1.8 2005/09/14 14:46:34 mgriffel Exp $

   Simple type generator. Support type-safe enumerations and restricted
   strings.

   Author: Michael Griffel
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
   Generating classes to directory <xsl:value-of select="$outdir"/>. 
   Found <xsl:value-of select="count(//enumeration)"/> enumerations, 
   <xsl:value-of select="count(//restrictedString)"/> restricted strings, 
   <xsl:value-of select="count(//regexString)"/> regex strings and
   <xsl:value-of select="count(//valueObject)"/> value objects.
   <xsl:apply-templates/>
</xsl:template>

<xsl:template match="enumeration">
   <xsl:variable name="package.dir"><xsl:value-of 
      select="$outdir"/>/<xsl:value-of 
         select="translate(@package, '.', '/')"/></xsl:variable> 

   <xsl:variable name="file"><xsl:value-of 
      select="$package.dir"/>/<xsl:value-of 
         select="@classname"/>.java</xsl:variable> 

   <redirect:write file="{$file}">         

   <xsl:call-template name="simple-enum-generator">
      <xsl:with-param name="classname" select="@classname"/>
      <xsl:with-param name="package" select="@package"/>
      <xsl:with-param name="values" select=".//value"/>
      <xsl:with-param name="javadoc" select="./description"/>
   </xsl:call-template>
   
   </redirect:write>
</xsl:template>

<xsl:template match="restrictedString">
   <xsl:variable name="package.dir"><xsl:value-of 
      select="$outdir"/>/<xsl:value-of 
         select="translate(@package, '.', '/')"/></xsl:variable> 

   <xsl:variable name="file"><xsl:value-of 
      select="$package.dir"/>/<xsl:value-of 
         select="@classname"/>.java</xsl:variable> 

   <redirect:write file="{$file}">         

   <xsl:call-template name="restricted-string">
      <xsl:with-param name="classname" select="@classname"/>
      <xsl:with-param name="package" select="@package"/>
      <xsl:with-param name="min-length" select="@min-length"/>
      <xsl:with-param name="max-length" select="@max-length"/>
      <xsl:with-param name="constants" select=".//constant"/>
      <xsl:with-param name="token-type" select="@token-type"/>
   </xsl:call-template>
   
   </redirect:write>
</xsl:template>


<xsl:template match="regexString">
   <xsl:variable name="package.dir"><xsl:value-of 
      select="$outdir"/>/<xsl:value-of 
         select="translate(@package, '.', '/')"/></xsl:variable> 

   <xsl:variable name="file"><xsl:value-of 
      select="$package.dir"/>/<xsl:value-of 
         select="@classname"/>.java</xsl:variable> 

  <redirect:write file="{$file}">

   <xsl:call-template name="regex-string">
      <xsl:with-param name="classname" select="@classname"/>
      <xsl:with-param name="package" select="@package"/>
      <xsl:with-param name="constants" select=".//constant"/>
      <xsl:with-param name="regex" select=".//regex"/>
   </xsl:call-template>
   
   </redirect:write>
</xsl:template>

<xsl:template match="valueObject">
   <xsl:variable name="package.dir"><xsl:value-of 
      select="$outdir"/>/<xsl:value-of 
         select="translate(@package, '.', '/')"/></xsl:variable> 

   <xsl:variable name="file"><xsl:value-of 
      select="$package.dir"/>/<xsl:value-of 
         select="@classname"/>.java</xsl:variable> 

  <redirect:write file="{$file}">

   <xsl:call-template name="value-object-generator">
      <xsl:with-param name="classname" select="@classname"/>
      <xsl:with-param name="package" select="@package"/>
      <xsl:with-param name="object" select="."/>
   </xsl:call-template>
   
   </redirect:write>

</xsl:template>


<xsl:template match="restrictedLong">
   <xsl:variable name="package.dir"><xsl:value-of 
      select="$outdir"/>/<xsl:value-of 
         select="translate(@package, '.', '/')"/></xsl:variable> 
   <xsl:variable name="file"><xsl:value-of 
      select="$package.dir"/>/<xsl:value-of 
         select="@classname"/>.java</xsl:variable> 

   <redirect:write file="{$file}">         

   <xsl:call-template name="restricted-long">
      <xsl:with-param name="classname" select="@classname"/>
      <xsl:with-param name="package" select="@package"/>
      <xsl:with-param name="min-value" select="@min-value"/>
      <xsl:with-param name="max-value" select="@max-value"/>
      <xsl:with-param name="constants" select=".//constant"/>
   </xsl:call-template>
   
   </redirect:write>
</xsl:template>


</xsl:stylesheet>