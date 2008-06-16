<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id$

   Generator for Configuration Service Properties file or database init script.

   Author: Michael Griffel, Lars Mennecke
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:redirect="http://xml.apache.org/xalan/redirect"
   xmlns:xalan2="http://xml.apache.org/xslt"
   extension-element-prefixes="redirect">

<xsl:include href="libcommon.xsl"/>
<xsl:include href="factory-template.xsl"/>
<xsl:include href="config-template.xsl"/>

<xsl:output method="text" 
            encoding="ISO-8859-1"/>

<xsl:param name="application-short-name" select="."/>
<xsl:param name="application-name" select="."/>
<xsl:param name="group-short-name" select="''"/>
<xsl:param name="mode" select="'PROPERTIES'"/>
<xsl:param name="tablename" select="'cfg_config'"/>

<xsl:template match="/">
   <xsl:variable name="group.count" 
         select="count(//group[../@short-name = $application-short-name 
                               or ../@name       = $application-name]
                              [contains($group-short-name, @short-name)])"/>
   <xsl:if test="not($group.count)">
      <xsl:message terminate="yes">Cannot find any group ELEMENT for 
      application name '<xsl:value-of select="$application-short-name"/>'
      and groups '<xsl:value-of select="string($group-short-name)"/>'.
      </xsl:message>
   </xsl:if>
   
   <xsl:for-each select="//group[../@short-name = $application-short-name 
                               or ../@name       = $application-name]
                              [contains($group-short-name, @short-name)]">
      <xsl:if test="$mode = 'PROPERTIES'"># <xsl:value-of select="@name"/> #
<xsl:for-each select=".//configEntry">
            <!-- do not write a property entry if the entry is a reference entry -->
            <xsl:if test="string-length(./key/@reference) = 0">
              <xsl:if test="./defaultValue = '_EMPTY_'">
                <xsl:message terminate="yes">
                  Error: Value _EMPTY_ is an internal value and is not allowed as defaultValue
                         (see key "<xsl:value-of select="./key"/>").
                </xsl:message>
</xsl:if><xsl:value-of select="../@package"/>.<xsl:value-of select="./key"/>=<xsl:choose>
                <xsl:when test="string-length(./defaultValue) = 0">
                  <xsl:text>_EMPTY_</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="./defaultValue"/>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:text>
</xsl:text>
            </xsl:if>
         </xsl:for-each>
      </xsl:if>
      <xsl:if test="$mode = 'SQL'">-- <xsl:value-of select="@name"/> --
<xsl:for-each select=".//configEntry">
            <!-- do not write an insert sql statement if the entry is a reference entry -->
            <xsl:if test="string-length(./key/@reference) = 0">
              <xsl:if test="./defaultValue = '_EMPTY_'">
                <xsl:message terminate="yes">
                  Error: Value _EMPTY_ is an internal value and is not allowed as defaultValue
                         (see key "<xsl:value-of select="./key"/>").
               </xsl:message>
</xsl:if>INSERT INTO <xsl:value-of select="$tablename"/> (config_key, value) VALUES
            ('<xsl:value-of select="../@package"/>.<xsl:value-of select="./key"/>
              <xsl:text>', '</xsl:text>
              <xsl:choose>
                <xsl:when test="string-length(./defaultValue) = 0">
                  <xsl:text>_EMPTY_</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="./defaultValue"/>
                </xsl:otherwise>
              </xsl:choose>');
</xsl:if>
      </xsl:for-each>commit;
</xsl:if>
   </xsl:for-each> <!-- group -->
</xsl:template>


</xsl:stylesheet>