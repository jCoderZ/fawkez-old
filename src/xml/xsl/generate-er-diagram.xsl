<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id: generate-er-diagram.xsl,v 1.2 2005/08/10 09:19:01 mgriffel Exp $
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="libcommon.xsl"/>

<xsl:output method="text" 
            encoding="ISO-8859-1"/>

<xsl:param name="outdir" select="'.'"/>

<!--
  ** Toplevel template:
  **    - writes headers and footers
  **    - selects /tables/table
  -->
<xsl:template match="/">
# Generated file - do not edit.
digraph G {
   graph [rankdir = LR, center = true, concentrate = true, fontsize=8];
	edge [fontname="verdana",fontsize=8,labelfontname="verdana",labelfontsize=8];
	node [fontname="verdana",fontsize=8,shape=record];<xsl:apply-templates select="/tables/table"/>
   <xsl:apply-templates select="/tables/table/fk"/>}
</xsl:template>

<xsl:template match="table">
   <xsl:text>
	</xsl:text>
   <xsl:value-of select="@name"/> [label="<xsl:value-of select="@name"/><xsl:text>|{</xsl:text>
   <xsl:apply-templates select="./column" mode="cols"/>
   <xsl:text>|</xsl:text>
   <xsl:apply-templates select="./column" mode="attrib"/>
   <xsl:text>}", fontname="verdana", fontcolor="black", fontsize=8.0];</xsl:text>
</xsl:template>

<xsl:template match="column" mode="cols">
   <xsl:value-of select="@name"/><xsl:text> : </xsl:text><xsl:value-of select="@type"/>
   <xsl:text>\l</xsl:text>   
</xsl:template>

<xsl:template match="column" mode="attrib">
   <xsl:if test="./primarykey">
      <xsl:text>PK </xsl:text>
   </xsl:if>
   <xsl:if test="./unique">
      <xsl:text>UK </xsl:text>
   </xsl:if>
   <xsl:if test="./notnull">
      <xsl:text>NN </xsl:text>
   </xsl:if>
   <xsl:if test="../fk/columns/col = @name">
      <xsl:text>FK </xsl:text>
   </xsl:if>
   <xsl:text>\l</xsl:text>
</xsl:template>

<xsl:template match="fk">
   <xsl:text>	</xsl:text>
   <xsl:value-of select="../@name"/> -> <xsl:value-of select="@references"/>;<xsl:text>
</xsl:text>
</xsl:template>
</xsl:stylesheet>            
