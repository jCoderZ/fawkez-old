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

<!-- main -->
<xsl:template match="/">
   <xsl:apply-templates/>
</xsl:template>


<xsl:template match="diagram">
   <xsl:variable name="f" select="concat('images/', @name)"/>
   <figure>
      <title><xsl:value-of select="description"/></title>
      <mediaobject  id="{concat('diagram-', @name)}">
         <imageobject  role="fo">
            <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
         </imageobject>
         <imageobject  role="html">
            <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
         </imageobject>
         <textobject>
            <phrase>Class diagram <xsl:value-of select="@name"/></phrase>
         </textobject>
      </mediaobject>
   </figure>
</xsl:template>

<xsl:template match="apidoc">
   <xsl:variable name="doc" select="document(concat($basedir, '/build/doc/sad/apidoc/', @name, '.xml'))"/>
   <xsl:apply-templates select="$doc/section/section"/>
</xsl:template>


</xsl:stylesheet>
