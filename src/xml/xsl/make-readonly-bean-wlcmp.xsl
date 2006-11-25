<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output
      method="xml"
      indent="yes"
      doctype-public="-//BEA Systems, Inc.//DTD WebLogic 8.1.0 EJB RDBMS Persistence//EN"
      doctype-system="http://www.bea.com/servers/wls810/dtd/weblogic-rdbms20-persistence-810.dtd"/>

   <xsl:include href="libcommon.xsl"/>

	<xsl:param name="bean-names" select="'||'"/>

   <xsl:template match="*|@*|comment()|text()|processing-instruction()">
      <xsl:copy>
         <xsl:apply-templates
            select="*|@*|comment()|text()|processing-instruction()"/>
      </xsl:copy>
   </xsl:template>
   
   <xsl:template match="weblogic-rdbms-bean[contains($bean-names, concat('|', ejb-name, '|'))]">
      <weblogic-rdbms-bean>
         <xsl:apply-templates/>
      </weblogic-rdbms-bean><xsl:text>
      </xsl:text>
      <xsl:comment> This is the read only version of the <xsl:value-of select="./ejb-name"/> Bean. </xsl:comment>
      <xsl:text>
      </xsl:text>
      <weblogic-rdbms-bean>
         <xsl:apply-templates mode="read-only"/>
      </weblogic-rdbms-bean>
   </xsl:template>
   
   <xsl:template match="ejb-name" mode="read-only">
      <ejb-name><xsl:value-of select="substring-before(text(), 'Entity')"/>ReaderEntity</ejb-name>
   </xsl:template>

   <xsl:template match="weblogic-ql" mode="read-only">
      <xsl:variable name="readonly-schema-name"><xsl:value-of
         select="substring-before(../../ejb-name/text(), 'Entity')"/>ReaderSchema</xsl:variable>
      <weblogic-ql>
         <xsl:call-template name="replace-schema-in-query">
            <xsl:with-param name="schemaName" select="$readonly-schema-name"/>
            <xsl:with-param name="query" select="text()"/>
         </xsl:call-template>
      </weblogic-ql>
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