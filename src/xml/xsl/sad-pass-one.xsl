<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xi="http://www.w3.org/2001/XInclude"
   xmlns:app="http://www.jcoderz.org/app-info-v1.0"
   xmlns:db="urn:docbook"
   exclude-result-prefixes="xsl xi app db"
   version="1.0">

<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

<xsl:include href="libcommon.xsl"/>
<xsl:include href="libxdoc.xsl"/>
<xsl:include href="html2docbook.xsl"/>

<!-- FIXME: The Transformation parameters set in XtremeDocs.java are ignored here!   -->
<xsl:param name="basedir" select="'.'"/>

<!-- BEGIN: generic copy -->
<xsl:template match="*">
   <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates
         select="*|comment()|text()|processing-instruction()"/>
   </xsl:copy>
</xsl:template>

<xsl:template match="@*">
   <xsl:copy/>
</xsl:template>

<xsl:template match="text()">
   <xsl:copy/>
</xsl:template>
<!-- END: generic copy -->

<xsl:variable name="app-info" select="document(concat($basedir, '/build/app-info.xml'))"/>

<!-- main -->
<xsl:template match="/">
   <book>
      <bookinfo>
         <xsl:call-template name="title-page">
            <xsl:with-param name="release" select="/sad/@release"/>
            <xsl:with-param name="legal-notice" select="/sad/@legal-notice"/>
         </xsl:call-template>

         <xsl:variable name="app-id" select="/sad/@app-id"/>
         <title><xsl:value-of
            select="$app-info//application[@id = $app-id]/@name"/></title>

         <subtitle><xsl:choose>
            <xsl:when test="/sad/@title"><xsl:value-of select="/sad/@title"/></xsl:when>
            <xsl:otherwise>Software Architecture Document</xsl:otherwise>
         </xsl:choose></subtitle>

         <xsl:apply-templates select="/sad/info/*"/>

         <!-- TODO: revision history from cvs log src/doc/sad/sad.xml -->
      </bookinfo>

      <xsl:apply-templates select="/sad/intro"/>
      <xsl:apply-templates select="/sad/architecture"/>
      <xsl:apply-templates select="/sad/components//component"/>
      <xsl:apply-templates select="/sad/components/following-sibling::*"/>
   </book>

</xsl:template>


<xsl:template match="intro">
   <chapter>
      <title>Introduction</title>
      <xsl:apply-templates select="./*"/>
   </chapter>
</xsl:template>

<xsl:template match="architecture">
   <chapter>
      <title>Architecture</title>
      <xsl:apply-templates select="quote"/>
      <xsl:apply-templates select="overview" mode="arch"/>
      <xsl:apply-templates select="goals" mode="arch"/>
      <xsl:apply-templates select="contraints" mode="arch"/>
   </chapter>
</xsl:template>


<xsl:template match="overview|goals|constraints" mode="arch">
   <section>
      <title>
         <xsl:call-template name="get-title">
            <xsl:with-param name="name" select="name(current())"/>
         </xsl:call-template>
      </title>
      <xsl:apply-templates select="./*"/>
   </section>
</xsl:template>


<xsl:template match="component">
   <xsl:variable name="title">
      <xsl:choose>
         <xsl:when test="@group-id">
            <xsl:variable name="group-id" select="@group-id"/>
            <xsl:variable name="app-id" select="/sad/@app-id"/>
            <xsl:value-of select="$app-info//application[@id = $app-id]/group[@id = $group-id]/@name"/>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <chapter>
      <title><xsl:value-of select="$title"/></title>

      <xsl:apply-templates select="./*"/>
   </chapter>
</xsl:template>


<xsl:template match="views|modules">
   <xsl:apply-templates/>
</xsl:template>


<xsl:template match="quote">
   <blockquote>
      <attribution><xsl:value-of select="@author"/>
      </attribution>
      <para><xsl:value-of select="text()"/></para>
   </blockquote>
</xsl:template>


<xsl:template match="package">
   <xsl:variable name="path">
      <xsl:call-template name="replace-char">
         <xsl:with-param name="s" select="@name"/>
         <xsl:with-param name="char" select="'.'"/>
         <xsl:with-param name="new" select="'/'"/>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="file"
      select="concat($basedir, '/src/java/', $path, '/package.xml')"/>
   <xsl:variable name="doc"
      select="document($file)/*"/>
   <xsl:if test="count($doc) = 0">
      <xsl:message terminate="yes">
+---------------------------------------------------------------+
| Failed to load external document:                             |
| <xsl:value-of select="$file"/>
| Either the document does not exists or is malformed.          |
+---------------------------------------------------------------+
      </xsl:message>
   </xsl:if>
   <xsl:apply-templates select="$doc"/>
</xsl:template>

<xsl:template match="include">
   <xsl:variable name="dir">
      <xsl:choose>
         <xsl:when test="@basedir">
            <xsl:value-of select="@basedir"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$basedir"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="file" select="concat($dir, '/', @href)"/>
   <xsl:variable name="doc"
      select="document($file)/*"/>
   <xsl:if test="count($doc) = 0">
      <xsl:message terminate="yes">
+---------------------------------------------------------------+
| Failed to load external document:                             |
| <xsl:value-of select="$file"/>
| Either the document does not exists or is malformed.          |
+---------------------------------------------------------------+
      </xsl:message>
   </xsl:if>
   <xsl:apply-templates select="$doc"/>
</xsl:template>

<!-- state diagram from enumeration class -->
<xsl:template match="diagram[@type = 'state'][@enumeration]">
   <xsl:variable name="package"><xsl:call-template
         name="package-from-class">
         <xsl:with-param name="class" select="@enumeration"/>
      </xsl:call-template></xsl:variable>
   <xsl:variable name="classname"
         select="substring-after(substring-after(@enumeration, $package), '.')"/>
   <diagram type="{@type}" name="{@name}">
      <xsl:apply-templates
         select="document(concat($basedir,
         '/src/xml/simple-types.xml'))//enumeration
            [@classname = $classname][@package = $package]"/>
      <xsl:apply-templates select="description"/>
   </diagram>
</xsl:template>
<xsl:template match="enumeration">
   <xsl:apply-templates select=".//state"/>
</xsl:template>


<!-- strip root element of package.xml -->
<xsl:template match="module|body">
   <xsl:apply-templates/>
</xsl:template>

<xsl:template name="get-title">
   <xsl:param name="name" select="''"/>
   <xsl:choose>
      <xsl:when test="$name = 'special'">Special Title String</xsl:when>
      <xsl:otherwise>
         <xsl:call-template name="toUpperCase">
            <xsl:with-param name="s" select="substring($name, 1, 1)"/>
         </xsl:call-template>
         <xsl:value-of select="substring($name, 2)"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>


</xsl:stylesheet>
