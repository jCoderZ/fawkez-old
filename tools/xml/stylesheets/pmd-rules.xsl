<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id: pmd-rules.xsl,v 1.1 2004/11/05 08:44:24 mgriffel Exp $
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" encoding="US-ASCII" omit-xml-declaration="yes"/>

<xsl:param name="type" select="''"/>

<xsl:template match="/">
   <xsl:comment> =================================================================== </xsl:comment>
   <xsl:comment><xsl:value-of select="substring-before($type, '.xml')"/></xsl:comment>
   <xsl:comment> =================================================================== </xsl:comment>
   <xsl:apply-templates select="//rule">
      <xsl:sort select="@name"/>
   </xsl:apply-templates>
</xsl:template>

<xsl:template match="rule">
   <xsl:if test="@name">
<!-- <xsl:comment> <xsl:value-of select="@message"/> </xsl:comment> -->
   <rule ref="rulesets/{$type}/{@name}"/>
   </xsl:if>
</xsl:template>

</xsl:stylesheet>