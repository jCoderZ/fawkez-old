<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:svg="http://www.w3.org/2000/svg"
   version="1.0"
   exclude-result-prefixes="xml">

<xsl:output method="xml" indent="yes" encoding="UTF-8"/>


<xsl:param name="basedir" select="'.'"/>

<xsl:variable name="MAX_WIDTH">300</xsl:variable>
<xsl:variable name="MAX_HEIGHT">600</xsl:variable>
<xsl:variable name="PT2PX_FACTOR">1.25</xsl:variable>

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

<xsl:template match="svg:svg|svg">
   <!-- Get Width in pixel (px) -->
   <xsl:variable name="width">
      <xsl:choose>
         <xsl:when test="contains(@width, 'px')">
            <xsl:value-of
               select="substring-before(@width, 'px')"/>
         </xsl:when>
         <xsl:when test="contains(@width, 'pt')">
            <xsl:value-of
               select="floor(substring-before(@width, 'pt') * $PT2PX_FACTOR)"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="@width"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <!-- Get Height in pixel (px) -->
   <xsl:variable name="height">
      <xsl:choose>
         <xsl:when test="contains(@height, 'px')">
            <xsl:value-of
               select="substring-before(@height, 'px')"/>
         </xsl:when>
         <xsl:when test="contains(@height, 'pt')">
            <xsl:value-of
               select="floor(substring-before(@height, 'pt') * $PT2PX_FACTOR)"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="@height"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <!-- calculate scale factor -->
   <xsl:variable name="scale">
      <xsl:choose>
         <xsl:when test="$width &gt; $MAX_WIDTH and $height &gt; $MAX_HEIGHT">
            <xsl:call-template name="min">
               <xsl:with-param name="x" select="$MAX_WIDTH div $width"/>
               <xsl:with-param name="y" select="$MAX_HEIGHT div $height"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:when test="$width &gt; $MAX_WIDTH">
            <xsl:value-of select="$MAX_WIDTH div $width"/>
         </xsl:when>
         <xsl:when test="$height &gt; $MAX_HEIGHT">
            <xsl:value-of select="$MAX_HEIGHT div $height"/>
         </xsl:when>
         <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <!-- need to scale image ? -->
   <xsl:choose>
      <xsl:when test="$scale != 1">
         <xsl:copy>
            <xsl:variable name="newWidth" select="floor($width * $scale)"/>
            <xsl:variable name="newHeight" select="floor($height * $scale)"/>
            <xsl:apply-templates select="./@*"/>
            <xsl:attribute name="width">
               <xsl:value-of select="concat($newWidth, 'px')"/>
            </xsl:attribute>
            <xsl:attribute name="height">
               <xsl:value-of select="concat($newHeight, 'px')"/>
            </xsl:attribute>
            <xsl:attribute name="scale">
               <xsl:value-of select="$scale"/>
            </xsl:attribute>
            <xsl:if test="not(@viewBox)">
               <xsl:attribute name="viewBox">
                  <xsl:value-of select="concat('0, 0, ', $width, ', ', $height)"/>
               </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="./*"/>
         </xsl:copy>
      </xsl:when>
      <!-- dont modify SVG -->
      <xsl:otherwise>
         <xsl:copy>
            <xsl:apply-templates select="./@*"/>
            <xsl:apply-templates select="./*"/>
         </xsl:copy>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="max">
   <xsl:param name="x"/>
   <xsl:param name="y"/>
   <xsl:choose>
      <xsl:when test="$x &gt; $y">
         <xsl:value-of select="$x"/>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="$y"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="min">
   <xsl:param name="x"/>
   <xsl:param name="y"/>
   <xsl:choose>
      <xsl:when test="$x &lt; $y">
         <xsl:value-of select="$x"/>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="$y"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

</xsl:stylesheet>
