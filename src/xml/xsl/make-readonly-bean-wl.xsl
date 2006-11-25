<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output
      method="xml"
      indent="yes"
      doctype-public="-//BEA Systems, Inc.//DTD WebLogic 8.1.0 EJB//EN"
      doctype-system="http://www.bea.com/servers/wls810/dtd/weblogic-ejb-jar.dtd"/>
	
   <xsl:include href="libcommon.xsl"/>
	<xsl:param name="bean-names" select="'||'"/>

   <xsl:template match="*|@*|comment()|text()|processing-instruction()">
      <xsl:copy>
         <xsl:apply-templates
            select="*|@*|comment()|text()|processing-instruction()"/>
      </xsl:copy>
   </xsl:template>
   
   <xsl:template match="weblogic-enterprise-bean[contains($bean-names, concat('|', ejb-name, '|'))]">
      <weblogic-enterprise-bean>
         <xsl:apply-templates mode="read-write"/>
      </weblogic-enterprise-bean><xsl:text>
   </xsl:text>
   <xsl:comment> This is the read only version of the <xsl:value-of select="./ejb-name"/> Bean. </xsl:comment>
   <xsl:text>
   </xsl:text>
   <weblogic-enterprise-bean>
         <xsl:apply-templates mode="read-only"/>
   </weblogic-enterprise-bean>
   </xsl:template>

   <!-- Generate stuff for the read-write bean -->
   <xsl:template match="persistence" mode="read-write">
      <persistence>
	      <xsl:apply-templates mode="read-write"/>
      </persistence>
      <invalidation-target>
         <ejb-name><xsl:value-of
            select="substring-before(../../ejb-name/text(), 'Entity')"/>ReaderEntity</ejb-name>
      </invalidation-target>
   </xsl:template>

   <xsl:template match="*|@*|comment()|text()|processing-instruction()"
      mode="read-write">
      <xsl:copy>
         <xsl:apply-templates
            select="*|@*|comment()|text()|processing-instruction()"
            mode="read-write"/>
      </xsl:copy>
   </xsl:template>

   <!-- Generate stuff for the read-only bean -->
   <xsl:template match="ejb-name" mode="read-only">
      <ejb-name><xsl:value-of select="substring-before(text(), 'Entity')"/>ReaderEntity</ejb-name>
   </xsl:template>

   <xsl:template match="jndi-name" mode="read-only">
      <jndi-name><xsl:value-of select="substring-before(text(), 'Entity')"/>ReaderEntity</jndi-name>
   </xsl:template>

   <xsl:template match="local-jndi-name" mode="read-only">
      <local-jndi-name><xsl:value-of select="substring-before(text(), 'Entity')"/>ReaderEntityLocal</local-jndi-name>
   </xsl:template>

   <xsl:template match="entity-descriptor" mode="read-only">
      <entity-descriptor>
         <entity-cache>
            <read-timeout-seconds>0</read-timeout-seconds>
            <concurrency-strategy>ReadOnly</concurrency-strategy>
         </entity-cache>
         <xsl:apply-templates mode="read-only"/>
      </entity-descriptor>
   </xsl:template>
   
   <xsl:template match="entity-cache" mode="read-only"/>

   <xsl:template match="*|@*|comment()|text()|processing-instruction()"
      mode="read-only">
      <xsl:copy>
         <xsl:apply-templates
            select="*|@*|comment()|text()|processing-instruction()"
            mode="read-only"/>
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>