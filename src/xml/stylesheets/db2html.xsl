<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id$
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="yes" encoding="US-ASCII"/>

<xsl:variable name="apos">&#39;</xsl:variable>
<xsl:variable name="quoted-apos">\&#39;</xsl:variable>
<xsl:variable name="quot">&#34;</xsl:variable>
<xsl:variable name="lt">&lt;</xsl:variable>
<xsl:variable name="cr">&#x0d;</xsl:variable>
<xsl:variable name="lf">&#x0a;</xsl:variable>

<xsl:template match="*">
</xsl:template>

<xsl:template match="/result">
   <html>
      <head>
      <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
      <style type="text/css">
      body {  font-family: Arial, Helvetica, sans-serif; font-size: 8pt}
      th   {  font-family: Arial, Helvetica, sans-serif; font-size: 8pt; font-weight: bold; background-color: #D3DCE3;}
      td   {  font-family:monaco,courier,"courier new"; font-size: 8pt; }
      form   {  font-family: Arial, Helvetica, sans-serif; font-size: 8pt}
      h1   {  font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 16pt; font-weight: bold}
      A:link    {  font-family: Arial, Helvetica, sans-serif; font-size: 8pt; text-decoration: none; color: blue}
      A:visited {  font-family: Arial, Helvetica, sans-serif; font-size: 8pt; text-decoration: none; color: blue}
      A:hover   {  font-family: Arial, Helvetica, sans-serif; font-size: 8pt; text-decoration: underline; color: red}
      A:link.nav {  font-family: Verdana, Arial, Helvetica, sans-serif; color: #000000}
      A:visited.nav {  font-family: Verdana, Arial, Helvetica, sans-serif; color: #000000}
      A:hover.nav {  font-family: Verdana, Arial, Helvetica, sans-serif; color: red;}
      .nav {  font-family: Verdana, Arial, Helvetica, sans-serif; color: #000000}
      .evenrow   {  background-color:#CCCCCC;}
      .oddrow   {  background-color:#DDDDDD;}

      #box a { text-decoration:none; width:15px; color:black;}
      #box a:hover { color:black; background:#ddd8b7;}
      #box a span {display:none;}
      #box a:hover span { text-decoration:none; position:fixed; top:10px; right:10px; z-index:3; display:block; 
          color:black; background:#ffffff; border:1px solid black; font-size:6pt; font-family:courier; padding:10px; }

      </style>
      </head>
      <body>
         <table>
            <xsl:apply-templates select="*"/>
         </table>

      </body>   
   </html>   

</xsl:template>

<xsl:template match="meta-data">
   <tr>
      <xsl:apply-templates select="*" mode="head"/>
   </tr>
</xsl:template>


<xsl:template match="meta-data">
   <tr>
      <xsl:apply-templates select="*" mode="head"/>
   </tr>
</xsl:template>

<xsl:template match="column" mode="head">
   <th>
      <xsl:value-of select="translate(@display-name, '_', ' ')"/>
   </th>
</xsl:template>


<xsl:template match="result-set">
      <xsl:apply-templates select="row" mode="data"/>
</xsl:template>

<xsl:template match="row" mode="data">
   <tr>
      <xsl:if test="position() mod 2 = 0">
          <xsl:attribute name="class">oddrow</xsl:attribute>
      </xsl:if>
      <xsl:if test="position() mod 2 != 0">
          <xsl:attribute name="class">evenrow</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="*" mode="data"/>
   </tr>   
</xsl:template>

<xsl:template match="column" mode="data">

   <td>
      <xsl:choose>
         <xsl:when test="display">
            <xsl:variable name="row" select="../@row-number"/>
            <xsl:variable name="column" select="position()"/>
            <xsl:choose>
               <xsl:when test="/result/meta-data/column[$column]/@display-size &gt; 30">
                  <xsl:variable name="method" select="concat('popup_',$row, '_', $column)"/>
                  <xsl:variable name="text_lt_escaped"><xsl:call-template name="replace-char-fast">
                                               <xsl:with-param name="s" select="display"/>
                                               <xsl:with-param name="old" select="$lt"/>
                                               <xsl:with-param name="new" select="'&amp;lt;'"/>
                                             </xsl:call-template></xsl:variable>
                  <xsl:variable name="text_display"><xsl:call-template name="replace-char-fast">
                                               <xsl:with-param name="s" select="translate($text_lt_escaped, '&#x0a;', $lt)"/>
                                               <xsl:with-param name="old" select="$lt"/>
                                               <xsl:with-param name="new" select="'&lt;br />'"/>
                                             </xsl:call-template></xsl:variable>
                  <div id="box"><a href="#">[DATA]<span>
                     <pre><xsl:value-of select="$text_display" disable-output-escaping="yes"/></pre></span></a></div>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:value-of select="display"/>
               </xsl:otherwise>   
            </xsl:choose>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="raw"/>
         </xsl:otherwise>
      </xsl:choose>
   </td>
</xsl:template>


<!-- Note: new string must not contain old string! -->
<xsl:template name="replace-char-fast">
   <xsl:param name="s" select="''"/>
   <xsl:param name="old" select="'asdlfjansdf'"/>
   <xsl:param name="new" select="''"/>
   <xsl:choose>
      <xsl:when test="contains($s, $old)">
         <xsl:value-of select="substring-before($s, $old)"/>
         <xsl:value-of select="$new"/>
         <xsl:call-template name="replace-char-fast">
            <xsl:with-param name="s" select="substring-after($s, $old)"/>
            <xsl:with-param name="old" select="$old"/>
            <xsl:with-param name="new" select="$new"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="$s"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

</xsl:stylesheet>