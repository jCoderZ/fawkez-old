<?xml version="1.0"?>
<xsl:stylesheet
   version="1.0"
   exclude-result-prefixes="xalan2"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xalan2="http://xml.apache.org/xslt"
   xmlns:redirect="http://xml.apache.org/xalan/redirect"
   extension-element-prefixes="redirect">

   <xsl:include href="libcommon.xsl"/>
   <xsl:output method="xml"
      omit-xml-declaration="yes"
      standalone="no"
      indent="yes"
      />
   <xsl:param name="outdir" select="'.'"/>

   <xsl:template match="/">

   <xsl:variable name="application" select="//application"/>
   <xsl:variable name="app-short" select="$application/@short-name"/>
     <xsl:variable name="main-file"
       select="concat($outdir, '/', $app-short, '-errorcodes.xml')"/>
    <redirect:write file="{$main-file}">
      <chapter>
        <title>Errorcodes for
          <xsl:value-of select="$application/@name"/></title>
        <para>All the errorcodes listed below belong to the
          appication <xsl:value-of select="$application/@name"/>.
        </para>
        <xsl:apply-templates select="//group"/>
      </chapter>
     </redirect:write>
   </xsl:template>

<xsl:template match="group">
   <xsl:variable name="app-short" select="../@short-name"/>
   <xsl:variable name="prefix" select="@short-name" />
   <xsl:variable name="service" select="@name"/>
   <xsl:variable name="baseId"><xsl:call-template
         name="asUniqueNumber">
            <xsl:with-param name="application-id" select="../@id"/>
            <xsl:with-param name="group-id" select="@id"/>
            <xsl:with-param name="message-id" select="'0'"/>
   </xsl:call-template></xsl:variable>
   <xsl:variable name="file-name"
     select="concat($app-short, '-', $prefix, '-errorcodes.xml')"/>
   <xsl:variable name="file"
     select="concat($outdir, '/', $file-name)"/>
  <xsl:element name="include" namespace="http://www.w3.org/2001/XInclude">
    <xsl:attribute name="href"><xsl:value-of select="$file-name"/></xsl:attribute>
  </xsl:element>

   <redirect:write file="{$file}">
     <section>
        <title>
           <xsl:value-of select="$service"/>
        </title>
        <para>
            This chapter includes all of the error codes
            issued by the <xsl:value-of select="$service"/> component.
            The shortname of this component
            is <xsl:value-of select="../@short-name"/>_<xsl:value-of select="./@short-name"/>
            and is used as prefix for the error codes defined here.
            <sbr/>
            The IDs for these error codes start at <xsl:value-of select="$baseId"/>.
        </para>

        <xsl:apply-templates select="message"/>
     </section>
   </redirect:write>
</xsl:template>



<xsl:template match="message">
   <section>
      <title>
         <xsl:value-of select="normalize-space(@name)"/>
         <xsl:if test="@changed = 'true' or @changed = 'yes'">
            <xsl:text> (CHANGED)</xsl:text>
         </xsl:if>
      </title>
      <informaltable frame = "topbot" colsep = "0" rowsep = "1">
         <tgroup cols = "2" tgroupstyle = "AttributeTable" colsep = "0" rowsep = "1">
         <colspec colnum = "1" colname = "1" colwidth = "1.575in" colsep = "0"/>
         <colspec colnum = "2" colname = "2" colwidth = "3.635in" colsep = "0"/>

         <xsl:call-template name="table-body"/>

         </tgroup>

      </informaltable>
   </section>
   <para></para>
</xsl:template>


<xsl:template name="table-body">
   <tbody>
   <xsl:variable name="id"><xsl:call-template
         name="asUniqueNumber">
            <xsl:with-param name="application-id" select="../../@id"/>
            <xsl:with-param name="group-id" select="../@id"/>
            <xsl:with-param name="message-id" select="@id"/>
        </xsl:call-template></xsl:variable>
   <xsl:variable name="baseIdAsHex"><xsl:call-template
         name="dec2hex">
            <xsl:with-param name="dec" select="$id"/>
        </xsl:call-template></xsl:variable>
   <xsl:variable name="symbol"><xsl:value-of
            select="../../@short-name"/>_<xsl:value-of
            select="../@short-name"/>_<xsl:value-of
            select="normalize-space(@name)"/></xsl:variable>

      <row rowsep = "1">
         <entry colname = "1"><emphasis role='bold'>Name</emphasis></entry>
         <entry colname = "2"><xsl:value-of select="$symbol"/></entry>
      </row>
      <row rowsep = "1">
         <entry colname = "1"><emphasis role='bold'>ID</emphasis></entry>
         <entry colname = "2"><xsl:value-of select="$id"/> (0x<xsl:value-of select="$baseIdAsHex"/>)</entry>
      </row>

      <row rowsep="1">
         <entry colname="1"><emphasis role='bold'>
         <xsl:choose>
            <xsl:when test="@category='FLOW' and not(solution)">
               Message Text
            </xsl:when>
            <xsl:when test="@category='AUDIT' and not(solution)">
               Audit Text
            </xsl:when>
            <xsl:when test="@category='BUSINESS' and not(@base-exception)">
               Message Text
            </xsl:when>
            <xsl:otherwise>
               Error Text
            </xsl:otherwise>
         </xsl:choose>
         </emphasis></entry>
         <entry colname="2">
            <xsl:variable name="apos2">'</xsl:variable>
            <xsl:variable name="apos" select='"&apos;"' />
            <xsl:variable name="aposDouble" select='"&apos;&apos;"' />
            <xsl:variable name="text">
               <xsl:call-template name="replace-string">
                  <xsl:with-param name="s" select="text"/>
                  <xsl:with-param name="old" select="'$aposDouble'"/>
                  <xsl:with-param name="new" select="'$apos'"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:call-template name="table-output">
               <xsl:with-param name="output" select="$text"/>
            </xsl:call-template>
         </entry>
      </row>
      <xsl:if test="description">
      <row rowsep="1">
         <entry colname="1"><emphasis role='bold'>Error Description</emphasis></entry>
         <entry colname="2">
            <xsl:call-template name="table-output">
               <xsl:with-param name="output" select="description"/>
            </xsl:call-template>
         </entry>
      </row>
      </xsl:if>
      <xsl:if test="solution">
      <row rowsep="1">
         <entry colname="1"><emphasis role='bold'>Error Solution</emphasis></entry>
         <entry colname="2">
            <xsl:call-template name="table-output">
               <xsl:with-param name="output" select="solution"/>
            </xsl:call-template>
         </entry>
      </row>
      </xsl:if>
      <xsl:if test="procedure">
      <row rowsep="1">
         <entry colname="1"><emphasis role='bold'>Error Procedure</emphasis></entry>
         <entry colname="2">
            <xsl:call-template name="table-output">
               <xsl:with-param name="output" select="procedure"/>
            </xsl:call-template>
         </entry>
      </row>
      </xsl:if>
      <xsl:if test="verification">
      <row rowsep="1">
         <entry colname="1"><emphasis role='bold'>Fix Verification</emphasis></entry>
         <entry colname="2">
            <xsl:call-template name="table-output">
               <xsl:with-param name="output" select="verification"/>
            </xsl:call-template>
         </entry>
      </row>
      </xsl:if>

      <row rowsep="1">
         <entry colname = "1"><emphasis role='bold'>Level</emphasis></entry>
         <entry colname = "2"><xsl:value-of select="@level"/></entry>
      </row>

      <xsl:variable name="business-impact">
         <xsl:choose>
            <xsl:when test="@business-impact"><xsl:value-of select="@business-impact"/></xsl:when>
            <xsl:otherwise>UNDEFINED</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:if test="not(@business-impact='NONE')">
      <row rowsep="1">
         <entry colname = "1"><emphasis role='bold'>Business Impact</emphasis></entry>
         <entry colname = "2"><xsl:value-of select="$business-impact"/></entry>
      </row>
      </xsl:if>

      <xsl:variable name="category">
         <xsl:choose>
            <xsl:when test="@category"><xsl:value-of select="@category"/></xsl:when>
            <xsl:otherwise>TECHNICAL</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <row rowsep="1">
         <entry colname = "1"><emphasis role='bold'>Category</emphasis></entry>
         <entry colname = "2"><xsl:value-of select="$category"/></entry>
      </row>
   </tbody>
</xsl:template>


<xsl:template name="list-item">
  <xsl:param name="item-text"/>
  <listitem>
    <para>
      <xsl:value-of select="normalize-space($item-text)"/>
    </para>
  </listitem>
</xsl:template>


<xsl:template name="table-output">
  <xsl:param name="output"/>
  <xsl:choose>
     <xsl:when test="contains($output,'{')">
        <xsl:call-template name="mark-variables">
           <xsl:with-param name="s" select="normalize-space($output)"/>
        </xsl:call-template>
     </xsl:when>
     <xsl:otherwise>
        <xsl:value-of select="normalize-space($output)"/>
     </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="mark-variables">
   <xsl:param name="s"/>
   <xsl:choose>
   <xsl:when test="contains($s, '{') and contains($s, '}')">
      <xsl:variable name="raw-token"
         select="substring-before(substring-after($s, '{'), '}')"/>
      <xsl:variable name="token">
         <xsl:choose>
            <xsl:when test="contains($raw-token, ':') and contains($raw-token, ',')">
               <xsl:value-of select="substring-after(substring-before($raw-token, ','), ':')"/>
            </xsl:when>
            <xsl:when test="contains($raw-token, ':')">
               <xsl:value-of select="substring-after($raw-token, ':')"/>
            </xsl:when>
            <xsl:when test="contains($raw-token, ',')">
               <xsl:value-of select="substring-before($raw-token, ',')"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="$raw-token"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:value-of select="substring-before($s, '{')"/><command>{<xsl:value-of select="$token"/>}</command>
      <xsl:call-template name="mark-variables">
         <xsl:with-param name="s" select="substring-after($s, '}')"/>
      </xsl:call-template>
   </xsl:when>
   <xsl:otherwise>
      <xsl:value-of select="$s"/>
   </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="asUniqueNumber">
   <xsl:param name="application-id" select="'0'"/>
   <xsl:param name="group-id" select="'0'"/>
   <xsl:param name="message-id" select="'0'"/>
   <xsl:value-of select="format-number(
        ($application-id * 256 * 256 * 256)
      + ($group-id * 256 * 256)
      + $message-id,
         '##########')"/>
</xsl:template>

<xsl:template name="dec2hex">
   <xsl:param name="dec" select="0"/>
   <xsl:variable name="div" select="floor($dec div 16)"/>
   <xsl:variable name="rem" select="$dec - ($div * 16)"/>
   <xsl:choose>
      <xsl:when test="$dec = 0">0</xsl:when>
      <xsl:when test="$dec = 1">1</xsl:when>
      <xsl:when test="$dec = 2">2</xsl:when>
      <xsl:when test="$dec = 3">3</xsl:when>
      <xsl:when test="$dec = 4">4</xsl:when>
      <xsl:when test="$dec = 5">5</xsl:when>
      <xsl:when test="$dec = 6">6</xsl:when>
      <xsl:when test="$dec = 7">7</xsl:when>
      <xsl:when test="$dec = 8">8</xsl:when>
      <xsl:when test="$dec = 9">9</xsl:when>
      <xsl:when test="$dec = 10">A</xsl:when>
      <xsl:when test="$dec = 11">B</xsl:when>
      <xsl:when test="$dec = 12">C</xsl:when>
      <xsl:when test="$dec = 13">D</xsl:when>
      <xsl:when test="$dec = 14">E</xsl:when>
      <xsl:when test="$dec = 15">F</xsl:when>
      <xsl:otherwise>
         <xsl:call-template name="dec2hex">
            <xsl:with-param name="dec" select="$div"/>
         </xsl:call-template>
      </xsl:otherwise>
   </xsl:choose>
   <xsl:if test="$div">
      <xsl:call-template name="dec2hex">
        <xsl:with-param name="dec" select="$rem"/>
      </xsl:call-template>
   </xsl:if>
</xsl:template>

</xsl:stylesheet>
