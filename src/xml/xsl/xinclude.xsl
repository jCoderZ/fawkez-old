<xsl:stylesheet 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xi="http://www.w3.org/2001/XInclude"
   xmlns:xi2="http://www.w3.org/2003/XInclude"
   version="1.0">
 
   <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

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

   <xsl:template match="xi:include">
      <xsl:apply-templates select="document(@href)/*"/>
   </xsl:template>

   <xsl:template match="xi2:include">
      <xsl:apply-templates select="document(@href)/*"/>
   </xsl:template>

</xsl:stylesheet>