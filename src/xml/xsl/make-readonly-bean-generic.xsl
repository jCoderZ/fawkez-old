<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output
      method="xml"
      indent="yes"
      doctype-public="-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN"
      doctype-system="http://java.sun.com/dtd/ejb-jar_2_0.dtd"/>
	
   <xsl:include href="libcommon.xsl"/>
	<xsl:param name="bean-names" select="'||'"/>

   <xsl:template match="*|@*|comment()|text()|processing-instruction()">
      <xsl:copy>
         <xsl:apply-templates
            select="*|@*|comment()|text()|processing-instruction()"/>
      </xsl:copy>
   </xsl:template>
   
   <xsl:template match="ejb-ref[contains($bean-names, concat('|', substring-after(ejb-ref-name, 'ejb/'), '|'))]">
      <ejb-ref>
         <xsl:apply-templates/>
      </ejb-ref>
      <ejb-ref>
         <xsl:apply-templates mode="read-only"/>
      </ejb-ref>
   </xsl:template>
   
   <xsl:template match="entity[contains($bean-names, concat('|', ejb-name, '|'))]">
      <entity>
         <xsl:apply-templates/>
      </entity><xsl:text>
      </xsl:text>
      <xsl:comment> This is the read only version of the <xsl:value-of select="./ejb-name"/> Bean. </xsl:comment>
      <xsl:text>
      </xsl:text>
      <entity>
         <xsl:apply-templates mode="read-only"/>
      </entity>
   </xsl:template>
   
   <xsl:template match="ejb-ref-name" mode="read-only">
      <ejb-ref-name><xsl:value-of select="substring-before(text(), 'Entity')"/>ReaderEntity</ejb-ref-name>
   </xsl:template>

   <xsl:template match="ejb-link" mode="read-only">
      <ejb-link><xsl:value-of select="substring-before(text(), 'Entity')"/>ReaderEntity</ejb-link>
   </xsl:template>
   
   <xsl:template match="ejb-name" mode="read-only">
      <ejb-name><xsl:value-of select="substring-before(text(), 'Entity')"/>ReaderEntity</ejb-name>
   </xsl:template>

   <xsl:template match="abstract-schema-name" mode="read-only">
      <abstract-schema-name><xsl:value-of
         select="substring-before(../ejb-name/text(), 'Entity')"/>ReaderSchema</abstract-schema-name>
   </xsl:template>
   
   <xsl:template match="ejb-ql" mode="read-only">
      <xsl:variable name="readonly-schema-name"><xsl:value-of
         select="substring-before(../../ejb-name/text(), 'Entity')"/>ReaderSchema</xsl:variable>
      <ejb-ql>
         <xsl:call-template name="replace-schema-in-query">
            <xsl:with-param name="schemaName" select="$readonly-schema-name"/>
            <xsl:with-param name="query" select="text()"/>
         </xsl:call-template>
      </ejb-ql>
   </xsl:template>
   

   <xsl:template match="*|@*|comment()|text()|processing-instruction()"
      mode="read-only">
      <xsl:copy>
         <xsl:apply-templates
            select="*|@*|comment()|text()|processing-instruction()"
            mode="read-only"/>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>