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

   <xsl:variable name="strRoles">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Rollen</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Roles</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strDomainModel">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Domänen-Model</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Domain Model</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strActor">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Aktor</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Actor</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strChannel">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Kanal</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Channel</xsl:text>
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

   <xsl:variable name="strPriority">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Priorität</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Priority</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strPriorityHigh">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Hoch</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>High</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strPriorityMedium">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Mittel</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Medium</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strPriorityLow">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Niedrig</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Low</xsl:text>
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

   <xsl:variable name="strDescription">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Beschreibung</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Description</xsl:text>
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
          <xsl:text>Beteiligte</xsl:text>
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

   <xsl:variable name="strTestAnnotations">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Testhinweise / -beispiele</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Testing Annotations / Examples</xsl:text>
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

   <xsl:variable name="strResponseTimes">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Antwortzeiten</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Response Times</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strFrequencyOfUse">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Häufigkeit</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Frequency Of Use</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strSpecificationDocument">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Spezifikationsdokument</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Specification Document</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strReferencingUseCases">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Aufrufende Anwendungsfälle</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Referencing use cases</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strMinimal">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Minimal</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Minimal</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strUseCases">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Anwendungsfälle</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Use Cases</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strIssuesForUseCases">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Offene Anwendungsfall-Probleme</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Issues for Use Cases</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strSummaryLevel">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Übersicht</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Summary Level</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strUserGoalLevel">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Anwendersicht</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>User Goal Level</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strComponentLevel">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Komponenten</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Component Level</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strChangeRequests">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Change Requests</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Change Requests</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strTerms">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Begriffe</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Terms</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strAcronym">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Akronym</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Acronym</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strRoleSummary">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Rollenübersicht</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Role Summary</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strAlias">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Alias</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Alias</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strSuperior">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Vorgesetzter</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Superior</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strSubordinates">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Untergebene</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Subordinates</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strActingUseCasesDirectly">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Anwendungsfälle mit direkter Beteiligung</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Acting use cases (directly)</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strActingUseCasesIndirectly">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Anwendungsfälle mit indirekter Beteiligung</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Acting use cases (indirectly)</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strStatus">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Status</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Status</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strReleaseVersion">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Freigabeversion</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Release Version</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strThisRequirementIsReferencedByTheFollowingUseCases">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Diese Anforderung wird in folgenden Anwendungsfällen referenziert</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>This requirement is referenced by the following use cases</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strEntitySummary">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Entitäten-Übersicht</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Entity Summary</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strAttribute">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Attribut</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Attribute</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strReferenceTo">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Referenz auf</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Reference To</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strRelation">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Bezug</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Relation</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strConstraints">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Einschränkungen</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Constraints</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strPattern">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Kardinalität</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Pattern</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strDomainModelDiagramForEntity">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Domänenmodel-Diagramm für Entität </xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Domain model diagram for entity </xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strRoleDiagramForRole">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Rollen-Diagramm für Rolle </xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Role diagram for role </xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strCompleteRoleDependencies">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Übersicht aller Rollenbeziehungen</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Complete Role Dependencies</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strCompleteDomainModelDetailed">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Detaillierte Übersicht des Domänenmodells</xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Complete Domain Model - Detailed</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strDomainModelForCategory">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Domänenmodell für Kategorie </xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Domain model for category </xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>

   <xsl:variable name="strRoleModelForCategory">
     <xsl:choose>
       <xsl:when test="$lang='de'">
          <xsl:text>Rollenmodell für Kategorie </xsl:text>
       </xsl:when>
       <xsl:otherwise>
          <xsl:text>Role model for category </xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:variable>


</xsl:stylesheet>
