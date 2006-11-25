<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"
            omit-xml-declaration="no"
            doctype-public="-//The jCoderZ Project//Chart2D//EN"
            doctype-system="chart2d.dtd"
            standalone="no"
            indent="yes"/>

<xsl:strip-space elements="*"/>

<xsl:variable name="modulus">10</xsl:variable>
<xsl:variable name="size">
    <xsl:choose>
       <xsl:when test="count(//statistic/locsummary) &lt; 80">
          <xsl:value-of select="(floor(count(//statistic/locsummary) div $modulus)) * $modulus"/>
       </xsl:when>
       <xsl:otherwise>
          80
       </xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<xsl:variable name="start">
    <xsl:choose>
       <xsl:when test="count(//statistic/locsummary) > $size">
          <xsl:value-of select="count(//statistic/locsummary) - $size"/>
       </xsl:when>
       <xsl:otherwise>
          0
       </xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<xsl:variable name="end">
  <xsl:value-of select="count(//statistic/locsummary)"/>
</xsl:variable>

<xsl:variable name="max-z">
    <xsl:call-template name="max-z-getter"/>
</xsl:variable>

<xsl:template match="/">
  <Chart2D Width="800" Height="400" Type="png" Filename="foo.png">
    <LBChart2D>
    <Object2DProperties ObjectTitleText="New Lines Of Code [LOC] (Snapshot)" ObjectTitleFontPointModel="18" ObjectBackgroundColor="0xe7e7ff"/>
    <Chart2DProperties ChartDataLabelsPrecision="38" />
    <GraphChart2DProperties LabelsAxisTitleText="Build" 
                            NumbersAxisTitleText="Lines"
                            LabelsAxisTicksAlignment="CENTERED">
    <xsl:apply-templates select="//statistic/locsummary/parent::*" mode="axis"/>
    </GraphChart2DProperties>
    <LegendProperties>
       <xsl:call-template name="draw-z-label">
           <xsl:with-param name="z" select="$max-z"/>
    </xsl:call-template>
<!--       <xsl:apply-templates
         select="//statistic[last()]/locsummary/package" mode="axis"/> -->
    </LegendProperties>
    
    <Dataset DoConvertToStacked="true">
    
    <xsl:call-template name="draw-z">
        <xsl:with-param name="z" select="$max-z"/>
    </xsl:call-template>
    
    
    </Dataset>

    <MultiColorsProperties/>

    <GraphProperties GraphAllowComponentAlignment="true"
                     GraphBarsRoundingRatio="0f"
                     GraphOutlineComponentsExistence="false"
                     />
    </LBChart2D>
  </Chart2D>
</xsl:template>
            
<xsl:template match="statistic" mode="axis">
   <xsl:if test="position() > $start">
   <xsl:if test="(position() - $start) mod $modulus = 1">
   <AxisLabelText>
      <xsl:value-of select="substring(builddate, 6,2)"/>/<xsl:value-of select="substring(builddate, 9,2)"/>
      (<xsl:value-of select="buildnumber"/>)
   </AxisLabelText>
   </xsl:if>
   </xsl:if>
</xsl:template>            


<xsl:template match="*" mode="content">
   <xsl:if test="position() > $start">
   <xsl:if test="(position() - $start) mod $modulus = 1">
            <xsl:text disable-output-escaping="yes">&lt;Category&gt;</xsl:text>
         </xsl:if>
            <Data><xsl:value-of select="@loc"/></Data>
         <xsl:if test="(position() - $start) mod $modulus = 0 or position() = last()">
            <xsl:text disable-output-escaping="yes">&lt;/Category&gt;</xsl:text>
         </xsl:if>
   </xsl:if>
</xsl:template>            

<xsl:template match="package" mode="axis">
   <LegendLabelsTexts>
      <xsl:choose>
         <xsl:when test="substring-after(@name, 'imppgw')">
            <xsl:value-of select="substring-after(@name, 'imppgw.')"/>
         </xsl:when>
         <xsl:otherwise>imppgw</xsl:otherwise>
      </xsl:choose>
   </LegendLabelsTexts>
</xsl:template>           



<xsl:template name="data-getter">
   <xsl:param name="x"/> <!-- build number -->
   <xsl:param name="z-name"/> <!-- package name -->
   <xsl:value-of select="//statistic[$x]/locsummary/package[@name = $z-name]/@loc"/>
</xsl:template>

<xsl:template name="z-name-getter">
   <xsl:param name="z"/> <!-- z-pos -->
   <xsl:value-of select="//statistic[last()]/locsummary/package[$z]/@name"/>
</xsl:template>

<xsl:template name="z-label-getter">
   <xsl:param name="z"/> <!-- z-pos -->
   <xsl:variable name="package">
      <xsl:call-template name="z-name-getter">
         <xsl:with-param name="z" select="$z"/>
      </xsl:call-template>
   </xsl:variable>
   <xsl:choose>
      <xsl:when test="substring-after($package, 'imppgw')">
         <xsl:value-of select="substring-after($package, 'imppgw.')"/>
      </xsl:when>
      <xsl:otherwise>imppgw</xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="max-z-getter">
   <xsl:value-of select="count(//statistic[last()]/locsummary/package)"/>
</xsl:template>

<xsl:template name="max-x-getter">
       <xsl:value-of select="count(//statistic/locsummary)"/>
</xsl:template>

<xsl:template name="draw-x">
   <xsl:param name="x"/>
   <xsl:param name="z-name"/>

   <xsl:variable name="position-relative">
       <xsl:value-of select="1 + $size - $x"/>
   </xsl:variable>
   
   <xsl:if test="$x > 0">
      <xsl:variable name="data">
         <xsl:call-template name="data-getter">
            <xsl:with-param name="x" select="$start + $position-relative"/>
            <xsl:with-param name="z-name" select="$z-name"/>
         </xsl:call-template>
      </xsl:variable>
      <xsl:if test="$position-relative mod $modulus = 1">
         <xsl:text disable-output-escaping="yes">&lt;Category&gt;</xsl:text>
      </xsl:if>
            
      <xsl:choose>
         <xsl:when test="$data != ''">
            <Data><xsl:value-of select="$data"/></Data>
         </xsl:when>
         <xsl:otherwise>
            <Data>0</Data>
         </xsl:otherwise>
      </xsl:choose>
            
      <xsl:if test="$position-relative mod $modulus = 0 or $position-relative = $size">
         <xsl:text disable-output-escaping="yes">&lt;/Category&gt;</xsl:text>
      </xsl:if>
      
      <xsl:call-template name="draw-x">
         <xsl:with-param name="x" select="$x - 1"/>
         <xsl:with-param name="z-name" select="$z-name"/>
      </xsl:call-template>
   </xsl:if>

</xsl:template>


<xsl:template name="draw-z">
   <xsl:param name="z"/>

   <xsl:if test="$z > 0">
      <xsl:variable name="z-pos" select="$max-z - $z"/>
    
      <Set>
         <xsl:variable name="z-name">
            <xsl:call-template name="z-name-getter">
               <xsl:with-param name="z" select="$z-pos"/>
            </xsl:call-template>
         </xsl:variable>
         
         <xsl:call-template name="draw-x">
            <xsl:with-param name="x" select="$size"/>
            <xsl:with-param name="z-name" select="$z-name"/>
         </xsl:call-template>
      </Set>

      <xsl:call-template name="draw-z">
         <xsl:with-param name="z" select="$z - 1"/>
      </xsl:call-template>
   </xsl:if>

</xsl:template>

<xsl:template name="draw-z-label">
   <xsl:param name="z"/>

   <xsl:if test="$z > 0">
      <xsl:variable name="z-pos" select="$max-z - $z"/>
    
      <LegendLabelsTexts>
         <xsl:call-template name="z-label-getter">
            <xsl:with-param name="z" select="$z-pos"/>
         </xsl:call-template>
      </LegendLabelsTexts>

      <xsl:call-template name="draw-z-label">
         <xsl:with-param name="z" select="$z - 1"/>
      </xsl:call-template>
   </xsl:if>

</xsl:template>


</xsl:stylesheet>
