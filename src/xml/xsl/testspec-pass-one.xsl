<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xi="http://www.w3.org/2001/XInclude"
   xmlns:app="http://www.jcoderz.org/app-info-v1.0"
   xmlns:db="urn:docbook"
   xmlns:uc="uc"
   xmlns:req="req"
   exclude-result-prefixes="xsl xi app db"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="req
                       http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                       uc
                       http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd"
   version="1.0">

<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

<xsl:include href="libcommon.xsl"/>
<xsl:include href="libxdoc.xsl"/>
<xsl:include href="html2docbook.xsl"/>

<!-- FIXME: The Transformation parameters set in XtremeDocs.java are ignored here!   -->
<xsl:param name="basedir" select="'.'"/>

<!-- BEGIN: generic copy -->
<xsl:template match="*">
   <xsl:if test="(@issue and @issue = //uc:usecases/uc:info/@issue) or (not(@issue) or @issue='')">
      <xsl:copy>
         <xsl:apply-templates select="@*"/>
         <xsl:apply-templates
            select="*|comment()|text()|processing-instruction()"/>
      </xsl:copy>
   </xsl:if>
   <xsl:if test="@issue and (not(//uc:usecases/uc:info/@issue) or //uc:usecases/uc:info/@issue = '')">
      <xsl:message>[ERROR] It is not allowed to use "issue" attributes without having defined the "issue" attribute in the root file!</xsl:message>
   </xsl:if>
</xsl:template>

<xsl:template match="@*">
   <xsl:copy/>
</xsl:template>

<xsl:template match="text()">
   <xsl:copy/>
</xsl:template>
<!-- END: generic copy -->

<!-- main -->
<xsl:template match="/">
   <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>
