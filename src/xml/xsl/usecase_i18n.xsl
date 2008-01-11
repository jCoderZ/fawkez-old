<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id: libxdoc.xsl 1 2006-11-25 14:41:52Z amandel $

   Contains literal strings and their translations according to
   the given language parameter.

  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:java="http://xml.apache.org/xslt/java"
   exclude-result-prefixes="xsl xs java">

  <!--  Our default language is english. -->
  <xsl:param name="lang" value="en"/>

   <xsl:variable name="strRole">
     <xsl:choose>
       <xsl:when test="$lang = 'de'">
          <xsl:text>Rolle</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Role</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strDomainModel">
     <xsl:choose>
       <xsl:when test="$lang = 'de'">
          <xsl:text>Domänen Model</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Domain Model</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strActors">
     <xsl:choose>
       <xsl:when test="$lang = 'de'">
          <xsl:text>Aktoren</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Actors</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strSuccessPaths">
     <xsl:choose>
       <xsl:when test="$lang = 'de'">
          <xsl:text>Erfolgspfade</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Success Paths</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strExtensionPaths">
     <xsl:choose>
       <xsl:when test="$lang = 'de'">
          <xsl:text>Erweiterungspfade</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Extension Paths</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

</xsl:stylesheet>