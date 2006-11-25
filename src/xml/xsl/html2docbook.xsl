<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id: html2docbook.xsl,v 1.8 2005/09/09 11:44:09 mgriffel Exp $

   Provides templates to convert HTML tags to DocBook.

   Author: Michael Griffel
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   exclude-result-prefixes="xsl">
   
<xsl:template match="h1|h2|h3">
    
   <xsl:message terminate="yes">
+---------------------------------------------------------------+
| WARNING: HTML header tags (e.g. h1, h2, etc) are not allowed! |    
| <xsl:value-of select="name(.)"/>:<xsl:value-of select="."/>
+---------------------------------------------------------------+
   </xsl:message>
</xsl:template>

<xsl:template match="ul">
   <itemizedlist>
      <xsl:apply-templates/>
   </itemizedlist>
</xsl:template>

<xsl:template match="ol">
   <orderedlist>
      <xsl:apply-templates/>
   </orderedlist>
</xsl:template>

<xsl:template match="dl">
   <variablelist>
      <xsl:apply-templates select="dt"/>
   </variablelist>
</xsl:template>


<xsl:template match="dt">
   <varlistentry>
      <term><xsl:apply-templates/></term>
      <listitem>
         <para>
            <xsl:apply-templates select="following::dd[1]"/>
         </para>
      </listitem>
   </varlistentry>
</xsl:template>
<xsl:template match="dd">
   <xsl:apply-templates/>
</xsl:template>

<xsl:template match="li">
   <listitem>
      <para>
         <xsl:apply-templates/>
      </para>
   </listitem>
</xsl:template>

<xsl:template match="pre">
   <informalexample>
      <programlisting>
         <xsl:apply-templates/>
      </programlisting>
   </informalexample>
</xsl:template>


<xsl:template match="p">
   <para>
      <xsl:apply-templates/>
   </para>
</xsl:template>

<xsl:template match="code|tt">
   <literal>
      <xsl:apply-templates/>
   </literal>
</xsl:template>

<xsl:template match="i">
   <emphasis>
      <xsl:apply-templates/>
   </emphasis>
</xsl:template>

<xsl:template match="b|strong">
   <emphasis role="bold">
      <xsl:apply-templates/>
   </emphasis>
</xsl:template>

<xsl:template match="a[@href]">
   <ulink url="{@href}">
      <xsl:value-of select="text()"/>
   </ulink>
</xsl:template>

<xsl:template match="img[@src]">
   <xsl:variable name="id">
      <xsl:choose>
         <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="translate(@href, '/', '.')"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="fileWithOutExt">
      <!-- FIXME -->
      <xsl:value-of select="substring-before(@src, '.')"/>
   </xsl:variable>
   <mediaobject  id="{@src}">
      <imageobject role="fo">
         <imagedata  format="SVG"  fileref="{concat($fileWithOutExt, '.svg')}"/>
      </imageobject>
      <imageobject  role="html">
         <imagedata  format="PNG"  fileref="{concat($fileWithOutExt, '.png')}"/>
      </imageobject>
   </mediaobject>
</xsl:template>

<!-- (simple) HTML tables not DocBook tables -->
<xsl:template match="table[tr]">
   <informaltable frame='all'>
   <tgroup cols='{count(tr[1]/td)}'>
   <tbody>
      <xsl:apply-templates/>
   </tbody>
   </tgroup>
   </informaltable>
</xsl:template>
<xsl:template match="tr|th">
   <row>
      <xsl:apply-templates select="./@*"/>
      <xsl:apply-templates select="./*"/>
   </row>
</xsl:template>
<xsl:template match="td">
   <entry>
      <xsl:apply-templates select="./@*"/>
      <xsl:apply-templates select="./*|./text()"/>
   </entry>
</xsl:template>

</xsl:stylesheet>