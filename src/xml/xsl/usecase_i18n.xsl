<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id: libxdoc.xsl 1 2006-11-25 14:41:52Z amandel $

   Contains literal strings and their translations according to
   the given language parameter.

  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   exclude-result-prefixes="xsl">

   <xsl:variable name="strRole">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Rolle</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Role</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strDomainModel">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Domänen Model</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Domain Model</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strActors">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Aktoren</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Actors</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strSuccessPaths">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Erfolgspfade</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Success Paths</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strExtensionPaths">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Erweiterungspfade</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Extension Paths</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strAllActors">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Alle Aktoren</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>All Actors</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strPrimaryActors">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Primäre Aktoren</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Primary Actors</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strSecondaryActors">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Sekundäre Aktoren</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Secondary Actors</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strMappingUseCasesToRoles">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Anwendungsfall-Rollen Abbildung</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Mapping Use Cases to Roles</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strUseCaseRevision">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Historie Anwendungsfälle</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Use Case Revision</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strOpenIssues">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Offene Probleme</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Open Issues</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strIssuesForRequirements">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Offene Anforderungs-Probleme</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Issues for Requirements</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strPriorities">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Prioritäten</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Priorities</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strEntities">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Entitäten</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Entities</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strOverview">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Überblick</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Overview</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strPreconditions">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Vorbedingungen</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Preconditions</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strStakeholder">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Interessenvertreter</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Stakeholder</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strSuccess">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Erfolgsfall</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Success</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strExtensions">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Erweiterungen</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Extensions</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strGuarantees">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Zusicherungen</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Guarantees</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strScope">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Geltungsbereich</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Scope</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strTrigger">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Auslöser</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Trigger</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

</xsl:stylesheet>
