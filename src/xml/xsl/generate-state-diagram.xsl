<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id$
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:redirect="http://xml.apache.org/xalan/redirect"
   xmlns:xalan2="http://xml.apache.org/xslt"
   extension-element-prefixes="redirect">

<xsl:include href="libcommon.xsl"/>

<xsl:output method="text" 
            encoding="ISO-8859-1"/>

<xsl:param name="outdir" select="'.'"/>

<xsl:template match="/">
   <xsl:apply-templates select="//diagram[@type = 'state']"/>
</xsl:template>

<xsl:template match="diagram[@type = 'state']">
   <xsl:variable name="file"><xsl:value-of 
      select="$outdir"/>/<xsl:value-of 
         select="translate(@name, ' ', '-')"/>.dot</xsl:variable> 

   <redirect:write file="{$file}">         
# Generated file - do not edit.
digraph G {
	node [fontname="verdana",fontsize=10,shape=box];
   
   <xsl:apply-templates select=".//state"/>
   <xsl:apply-templates select=".//transition"/>
}
   
   </redirect:write>

</xsl:template>

<xsl:template match="state[not(@type)]">
   <xsl:value-of select="@name"/> [style=rounded];
</xsl:template>
   
<xsl:template match="state[@type = 'start']">
   <xsl:value-of select="@name"/> [shape=circle];  
</xsl:template>

<xsl:template match="state[@type = 'end']">
   <xsl:value-of select="@name"/> [shape=doublecircle];  
</xsl:template>
   
<xsl:template match="transition">
   <xsl:value-of select="../@name"/> -&gt; <xsl:value-of select="@target"/> [ label = "<xsl:value-of select='@name'/>", fontname="verdana",fontsize=8 ];
</xsl:template>
   
</xsl:stylesheet>            
