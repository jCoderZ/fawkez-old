<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id$

   Collects common XSL templates for Xtreme Documentation.

  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:java="http://xml.apache.org/xslt/java"
   exclude-result-prefixes="xsl xs java">

<xsl:include href="html2docbook.xsl"/>

<xsl:param name="cclabel"/>
<xsl:param name="user"/>
<xsl:param name="companyname"/>
<xsl:param name="companylogo"/>

<!-- ===============================================================
                         _              _
      ___ ___  _ __  ___| |_ __ _ _ __ | |_ ___
     / __/ _ \| '_ \/ __| __/ _` | '_ \| __/ __|
    | (_| (_) | | | \__ \ || (_| | | | | |_\__ \
     \___\___/|_| |_|___/\__\__,_|_| |_|\__|___/
     =============================================================== -->
<xsl:variable name="COMPANY_NAME"><xsl:value-of select="$companyname"/></xsl:variable>


<!-- ===============================================================
      _                       _       _
     | |_ ___ _ __ ___  _ __ | | __ _| |_ ___  ___
     | __/ _ \ '_ ` _ \| '_ \| |/ _` | __/ _ \/ __|
     | ||  __/ | | | | | |_) | | (_| | ||  __/\__ \
      \__\___|_| |_| |_| .__/|_|\__,_|\__\___||___/
                       |_|
     =============================================================== -->
<xsl:template name="datetime">
      <xsl:value-of
      select="java:format(java:java.text.SimpleDateFormat.new('yyyy-MM-dd HH:mm Z'), java:java.util.Date.new())"/>
</xsl:template>

<xsl:template name="title-page">
   <xsl:param name="release" select="'n/a'"/>

   <mediaobject>
      <imageobject  role="fo">
         <imagedata  format="SVG"  fileref="images/{$companylogo}.svg"/>
      </imageobject>
      <imageobject  role="html">
         <imagedata  format="SVG"  fileref="images/{$companylogo}.png"/>
      </imageobject>
   </mediaobject>

   <releaseinfo>
      <xsl:choose>
         <xsl:when test="string-length($cclabel) &gt; 0 and not(starts-with($cclabel, '${label}'))">
            <xsl:value-of select="$cclabel"/>
         </xsl:when>
         <xsl:when test="starts-with($release, '$Name:')
            and string-length($release) &gt; 10">
            <xsl:value-of select="normalize-space(substring-before(
                  substring-after($release, '$Name:'),
                  '$'))"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:call-template name="datetime"/>
            <xsl:text> by </xsl:text>
            <xsl:value-of select="$user"/>
         </xsl:otherwise>
      </xsl:choose>
   </releaseinfo>

   <affiliation>
      <orgname><xsl:value-of select="$companyname"/></orgname>
   </affiliation>

   <address>
      <othername><xsl:value-of select="$companyname"/></othername>
   </address>

   <copyright>
      <year>2002</year>
      <year>2003</year>
      <year>2004</year>
      <year>2005</year>
      <year>2006</year>
      <holder><xsl:value-of select="$companyname"/></holder>
   </copyright>

   <legalnotice><title>Important Notice</title>
      <xsl:call-template name="legal-notice"/>

      <formalpara>
         <title>Term Of Use</title>

         <para>All Rights Reserved.</para>
      </formalpara>
   </legalnotice>
</xsl:template>

<xsl:template name="legal-notice">
   <para>
   Copyright 2006, <xsl:value-of select="$companyname"/>. All rights reserved.
   </para>
   <para>
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
   </para>
   <itemizedlist>
     <listitem><para>Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.</para></listitem>
     <listitem><para>Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following
       disclaimer in the documentation and/or other materials
       provided with the distribution.</para></listitem>
     <listitem><para>Neither the name of <xsl:value-of select="$companyname"/> nor the names of
       its contributors may be used to endorse or promote products
       derived from this software without specific prior written
       permission.</para></listitem>
   </itemizedlist>
   <para>
   THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
   ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
   BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
   ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
   </para>
</xsl:template>

</xsl:stylesheet>
