<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id: generate-config-server-test.xsl,v 1.2 2005/04/07 13:28:01 lmenne Exp $

   Generator for Configuration Service Test Cases.

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
<xsl:include href="config-test-template.xsl"/>

<xsl:output method="text" 
            encoding="ISO-8859-1"/>

<xsl:param name="application-short-name" select="."/>
<xsl:param name="application-name" select="."/>
<xsl:param name="group-short-name" select="''"/>
<xsl:param name="outdir" select="'.'"/>
<xsl:param name="server-test-class" select="'org.jcoderz.cssng.ServerTestCase'"/>

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
   <!-- log to out -->
   Generating classes to directory <xsl:value-of select="$outdir"/>. 
   Found <xsl:value-of select="$group.count"/> groups for application <xsl:value-of select="$application-short-name"/>.
   And groups (<xsl:value-of select="$group-short-name"/>).
   
   <xsl:for-each select="//group[../@short-name = $application-short-name 
                               or ../@name       = $application-name]
                              [contains($group-short-name, @short-name)]">
      <xsl:variable name="package.dir"><xsl:value-of
         select="$outdir"/>/<xsl:value-of
            select="translate(@package, '.', '/')"/></xsl:variable>
      <xsl:variable name="serviceName">
         <xsl:call-template name="asJavaIdentifier">
            <xsl:with-param name="name" select="@name"/>
         </xsl:call-template>
      </xsl:variable>
            
      <!-- TestCase -->
      <xsl:if test="boolean(.//configEntry)">    
         <xsl:variable name="testFile">
            <xsl:value-of select="$package.dir"/>/<xsl:value-of select="$serviceName"/>
            <xsl:text>ConfigurationServerTest.java</xsl:text>
         </xsl:variable>
         <redirect:write file="{$testFile}">
         <xsl:call-template name="config-interface-test-generator">
            <xsl:with-param name="servicename" select="$serviceName"/>
            <xsl:with-param name="package" select="./@package"/>
            <xsl:with-param name="entries" select=".//configEntry"/>
            <xsl:with-param name="serviceInterfaces" select="./@service"/>
            <xsl:with-param name="server-test-class" select="$server-test-class"/>
         </xsl:call-template>
         </redirect:write>
      </xsl:if> <!-- configEntry -->
   </xsl:for-each>
</xsl:template>


</xsl:stylesheet>