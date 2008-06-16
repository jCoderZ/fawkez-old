<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id$

   Generator for Confiuration Service Classes including
   ServiceConfiguration interfaces and impls, 
   service specific ConfigurationKeys.

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
<xsl:param name="outdir" select="'.'"/>

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
            
      <!-- Service Factories -->
      <xsl:if test="./@service != 'NONE'">
         <xsl:variable name="withServiceConfiguration" select="boolean(.//configEntry)">
         </xsl:variable>
         
         <xsl:variable name="clientFactoryFile">
            <xsl:value-of select="$package.dir"/>/<xsl:value-of select="$serviceName"/>
            <xsl:text>ClientFactory.java</xsl:text>
         </xsl:variable>
         <redirect:write file="{$clientFactoryFile}">
         <xsl:call-template name="client-factory-generator">
            <xsl:with-param name="servicename" select="$serviceName"/>
            <xsl:with-param name="package" select="./@package"/>
            <xsl:with-param name="withServiceConfiguration" select="$withServiceConfiguration"/>
            <xsl:with-param name="serviceInterfaces" select="./@service"/>
         </xsl:call-template>
         </redirect:write>
      
         <xsl:variable name="containerFactoryFile">
            <xsl:value-of select="$package.dir"/>/<xsl:value-of select="$serviceName"/>
            <xsl:text>ContainerFactory.java</xsl:text>
         </xsl:variable>
         <redirect:write file="{$containerFactoryFile}">
         <xsl:call-template name="container-factory-generator">
            <xsl:with-param name="servicename" select="$serviceName"/>
            <xsl:with-param name="package" select="./@package"/>
            <xsl:with-param name="withServiceConfiguration" select="$withServiceConfiguration"/>
            <xsl:with-param name="serviceInterfaces" select="./@service"/>
         </xsl:call-template>
         </redirect:write>
      
      </xsl:if> <!-- service != NONE -->

      <xsl:if test="boolean(.//configEntry)">      
         <!-- Configuration Key (constants) -->
         <xsl:variable name="classname" 
            select="concat($serviceName,'ConfigurationKeys')"/>
         <xsl:variable name="file"><xsl:value-of 
            select="$package.dir"/>/<xsl:value-of 
            select="$classname"/>.java</xsl:variable> 
         <redirect:write file="{$file}">
            <xsl:call-template name="service-configuration-keys">
               <xsl:with-param name="classname" select="$classname"/>
               <xsl:with-param name="package" select="@package"/>
               <xsl:with-param name="items" 
                  select=".//configEntry"/>
            </xsl:call-template>
         </redirect:write>
      
         <!-- type safe service configuration interfaces and impls -->
         <xsl:variable name="configFile">
            <xsl:value-of select="$package.dir"/>/<xsl:value-of select="$serviceName"/>
            <xsl:text>Configuration.java</xsl:text>
         </xsl:variable>
         
         <redirect:write file="{$configFile}">
         <xsl:call-template name="config-interface-generator">
            <xsl:with-param name="servicename" select="$serviceName"/>
            <xsl:with-param name="package" select="./@package"/>
            <xsl:with-param name="entries" select=".//configEntry"/>
         </xsl:call-template>
         </redirect:write>
      
         <xsl:variable name="configImplFile">
            <xsl:value-of select="$package.dir"/>/<xsl:value-of select="$serviceName"/>
            <xsl:text>ConfigurationImpl.java</xsl:text>
         </xsl:variable>
      
         <redirect:write file="{$configImplFile}">
         <xsl:call-template name="config-impl-generator">
            <xsl:with-param name="servicename" select="$serviceName"/>
            <xsl:with-param name="package" select="./@package"/>
            <xsl:with-param name="entries" select=".//configEntry"/>
         </xsl:call-template>
         </redirect:write>
      </xsl:if> <!-- configEntry available -->
      
   </xsl:for-each>
</xsl:template>


</xsl:stylesheet>