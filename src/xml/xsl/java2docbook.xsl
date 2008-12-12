<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   version="1.0">

<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

<xsl:include href="libcommon.xsl"/>
<xsl:include href="html2docbook.xsl"/>

<xsl:param name="basedir" select="'.'"/>
<xsl:variable name="NEWLINE" select="string('&#xa;')"/>
<xsl:variable name="nbsp" select="string('&#160;')"/>

<xsl:template match="/">
   <!-- this section is not visible in the sad document! -->
   <section>
      <title>API Documentation</title>
      <xsl:apply-templates/>
   </section>
</xsl:template>

<xsl:template match="class">
   <section>
      <title>
         <xsl:value-of select="translate(substring(@type, 1, 1), $lowercase-a_z, $uppercase-a_z)"/>
         <xsl:value-of select="substring(@type, 2)"/>
         <xsl:text> </xsl:text>
         <xsl:value-of select="@name"/></title>
         <para><xsl:apply-templates select="doc"/></para>
      <programlisting format="java">
package <xsl:value-of select="../@name"/>;

<xsl:value-of select="@modifiers"/> class <xsl:value-of select="@name"/>
<!-- extends -->
<xsl:if test="@superclass">
<xsl:value-of select="$NEWLINE"/>
<xsl:text>      extends </xsl:text>
   <xsl:call-template name="classname">
      <xsl:with-param name="class" select="@superclass"/>
      <xsl:with-param name="package" select="../@name"/>
   </xsl:call-template>
</xsl:if>
<!-- implements -->
<xsl:if test=".//interface">
   <xsl:call-template name="implements">
      <xsl:with-param name="n" select=".//interface"/>
   </xsl:call-template>
</xsl:if>
{
<xsl:if test=".//field">
   <xsl:text>   // fields</xsl:text>
   <xsl:value-of select="$NEWLINE"/>
   <xsl:for-each select=".//field">
      <xsl:variable name="f">
         <xsl:if test="@modifiers">
            <xsl:value-of select="@modifiers"/>
            <xsl:text> </xsl:text>
         </xsl:if>
         <xsl:call-template name="classname">
            <xsl:with-param name="class" select="@type"/>
            <xsl:with-param name="package" select="../../@name"/>
         </xsl:call-template>
         <xsl:text> </xsl:text>
         <xsl:value-of select="@name"/>
         <xsl:if test="@value">
            <xsl:text> = </xsl:text>
            <xsl:value-of select="@value"/>
         </xsl:if>
         <xsl:text>;</xsl:text>
      </xsl:variable>
      <xsl:call-template name="java-formatter">
         <xsl:with-param name="line" select="$f"/>
      </xsl:call-template>
      <xsl:value-of select="$NEWLINE"/>
   </xsl:for-each>
</xsl:if>

<xsl:if test=".//method">
  <xsl:text>   // methods</xsl:text>
  <xsl:value-of select="$NEWLINE"/>
</xsl:if>
<xsl:for-each select=".//method">
   <xsl:variable name="m">
      <xsl:if test="@modifiers">
         <xsl:value-of select="@modifiers"/>
         <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:call-template name="classname">
         <xsl:with-param name="class" select="return/@type"/>
         <xsl:with-param name="package" select="../../@name"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <xsl:value-of select="@name"/>
      <xsl:text> </xsl:text>
      <xsl:call-template name="params">
         <xsl:with-param name="n" select=".//parameter"/>
      </xsl:call-template>
   </xsl:variable>
   <xsl:call-template name="java-formatter">
      <xsl:with-param name="line" select="$m"/>
   </xsl:call-template>
   <xsl:if test=".//throws">
      <xsl:call-template name="throws">
         <xsl:with-param name="n" select=".//throws"/>
      </xsl:call-template>
   </xsl:if>;<xsl:value-of select="$NEWLINE"/><xsl:value-of select="$NEWLINE"/>
</xsl:for-each>}</programlisting>

<!-- field details -->
<xsl:if test=".//field[doc/text()]">
   <section>
      <title>Fiels Details</title>
         <xsl:for-each select=".//field[doc/text()]">
            <section>
               <title><xsl:value-of select="@name"/></title>
               <para>
                  <methodname>
                     <xsl:value-of select="@modifiers"/>
                     <xsl:text> </xsl:text>
                     <xsl:call-template name="classname">
                        <xsl:with-param name="class" select="@type"/>
                        <xsl:with-param name="package" select="../../@name"/>
                     </xsl:call-template>
                     <xsl:text> </xsl:text>
                     <emphasis role="bold">
                        <xsl:value-of select="@name"/>
                     </emphasis>
                  </methodname>
               </para>
               <xsl:if test="doc/text()">
                  <para><xsl:apply-templates select="doc"/></para>
               </xsl:if>
               <xsl:if test=".//parameter[doc]">
                  <variablelist>
                     <title>Parameters</title>
                     <xsl:for-each select=".//parameter[doc]">
                        <varlistentry>
                           <term><xsl:value-of select="@name"/></term>
                           <listitem><xsl:apply-templates select="doc"/></listitem>
                        </varlistentry>
                     </xsl:for-each>
                  </variablelist>
               </xsl:if>
            </section>
         </xsl:for-each>
   </section>
</xsl:if>

<!-- method details -->
<xsl:if test=".//method[doc/text() or parameter/doc or throws/doc or return/doc]">
   <section>
      <title>Method Details</title>
         <xsl:for-each select=".//method[doc/text() or parameter/doc or throws/doc or return/doc]">
            <section>
               <title><xsl:value-of select="@name"/></title>
               <para>
                  <methodname>
                     <xsl:value-of select="@modifiers"/>
                     <xsl:text> </xsl:text>
                     <xsl:call-template name="classname">
                        <xsl:with-param name="class" select="return/@type"/>
                        <xsl:with-param name="package" select="../../@name"/>
                        <xsl:with-param name="remove-package" select="true()"/>
                     </xsl:call-template>
                     <xsl:text> </xsl:text>
                     <emphasis role="bold">
                        <xsl:value-of select="@name"/>
                     </emphasis>
                     <xsl:text> </xsl:text>
                     <xsl:call-template name="params">
                        <xsl:with-param name="n" select=".//parameter"/>
                        <xsl:with-param name="remove-package" select="true()"/>
                     </xsl:call-template>
                  </methodname>
               </para>
               <xsl:if test="doc/text()">
                  <para><xsl:apply-templates select="doc"/></para>
               </xsl:if>
               <xsl:if test=".//parameter[doc]">
                  <variablelist>
                     <title>Parameters</title>
                     <xsl:for-each select=".//parameter[doc]">
                        <varlistentry>
                           <term><xsl:value-of select="@name"/></term>
                           <listitem><xsl:apply-templates select="doc"/></listitem>
                        </varlistentry>
                     </xsl:for-each>
                  </variablelist>
               </xsl:if>
               <xsl:if test="./return[doc]">
                  <variablelist>
                     <title>Returns</title>
                     <varlistentry>
                        <term></term>
                        <listitem><xsl:apply-templates select="./return/doc"/></listitem>
                     </varlistentry>
                  </variablelist>
               </xsl:if>
               <xsl:if test=".//throws[doc][@type != 'java.rmi.RemoteException']">
                  <variablelist>
                     <title>Throws</title>
                     <xsl:for-each select=".//throws[doc][@type != 'java.rmi.RemoteException']">
                        <xsl:variable name="p">
                           <xsl:call-template name="package-from-class">
                              <xsl:with-param name="class" select="@type"/>
                           </xsl:call-template>
                        </xsl:variable>
                        <xsl:variable name="c">
                           <xsl:call-template name="classname">
                              <xsl:with-param name="class" select="@type"/>
                              <xsl:with-param name="package" select="$p"/>
                           </xsl:call-template>
                        </xsl:variable>
                        <varlistentry>
                           <term><classname><xsl:value-of select="$c"/></classname></term>
                           <listitem><xsl:apply-templates select="doc"/></listitem>
                        </varlistentry>
                     </xsl:for-each>
                  </variablelist>
               </xsl:if>
            </section>
         </xsl:for-each>
   </section>
</xsl:if>

   </section>
</xsl:template>


<xsl:template name="classname">
   <xsl:param name="class"/>
   <xsl:param name="package" select="'unknown'"/>
   <xsl:param name="remove-package" select="false()"/>
   <xsl:variable name="result">
      <xsl:choose>
         <xsl:when test="not($class)">
            <xsl:text>void</xsl:text>
         </xsl:when>
         <xsl:when test="starts-with($class, 'java.lang.')">
            <xsl:value-of select="substring-after($class, 'java.lang.')"/>
         </xsl:when>
         <xsl:when test="starts-with($class, $package)">
            <xsl:value-of select="substring-after(substring-after($class, $package), '.')"/>
         </xsl:when>
         <xsl:when test="$remove-package">
            <xsl:call-template name="classname-without-package">
               <xsl:with-param name="classname" select="$class"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$class"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:value-of select="$result"/>
</xsl:template>

<xsl:template name="params">
   <xsl:param name="n"/>
   <xsl:param name="remove-package" select="false()"/>
   <xsl:text>(</xsl:text>
   <xsl:for-each select="$n">
      <xsl:call-template name="classname">
         <xsl:with-param name="class" select="@type"/>
         <xsl:with-param name="package" select="../../../@name"/>
         <xsl:with-param name="remove-package" select="$remove-package"/>
      </xsl:call-template>
      <xsl:value-of select="$nbsp"/>
      <xsl:value-of select="@name"/>
      <xsl:if test="position() != last()">
         <xsl:text>, </xsl:text>
      </xsl:if>
   </xsl:for-each>
   <xsl:text>)</xsl:text>
</xsl:template>

<xsl:template name="throws">
   <xsl:param name="n"/>
   <xsl:value-of select="$NEWLINE"/>
   <xsl:text>      throws </xsl:text>
   <xsl:for-each select="$n">
      <xsl:call-template name="classname">
         <xsl:with-param name="class" select="@type"/>
         <xsl:with-param name="package" select="../../../@name"/>
      </xsl:call-template>
      <xsl:if test="position() != last()">
         <xsl:text>,</xsl:text>
         <xsl:value-of select="$NEWLINE"/>
         <xsl:text>         </xsl:text>
      </xsl:if>
   </xsl:for-each>
</xsl:template>

<xsl:template name="implements">
   <xsl:param name="n"/>
   <xsl:value-of select="$NEWLINE"/>
   <xsl:text>      implements </xsl:text>
   <xsl:for-each select="$n">
      <xsl:call-template name="classname">
         <xsl:with-param name="class" select="@name"/>
         <xsl:with-param name="package" select="../../@name"/>
      </xsl:call-template>
      <xsl:if test="position() != last()">
         <xsl:text>,</xsl:text>
         <xsl:value-of select="$NEWLINE"/>
         <xsl:text>         </xsl:text>
      </xsl:if>
   </xsl:for-each>
</xsl:template>

<xsl:template name="classname-without-package">
   <xsl:param name="classname"/>

   <xsl:choose>
      <xsl:when test="contains($classname, '.')">
         <xsl:call-template name="classname-without-package">
            <xsl:with-param name="classname" select="substring-after($classname, '.')"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="$classname"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="java-formatter">
   <xsl:param name="line"/>
   <xsl:param name="ident" select="'   '"/>
   <xsl:param name="width" select="'50'"/>
   <xsl:param name="level" select="'0'"/>
   <xsl:variable name="newIdent">
      <xsl:choose>
         <xsl:when test="$level = 0">
            <xsl:value-of select="concat($ident, '   ')"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$ident"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:choose>
      <!-- line < width -->
      <xsl:when test="string-length($line) + string-length($ident) &lt;= $width">
         <xsl:value-of select="$ident"/>
         <xsl:value-of select="$line"/>
      </xsl:when>
      <!-- field: break before '=' -->
      <xsl:when test="contains($line, ' = ')">
         <xsl:value-of select="$ident"/>
         <xsl:value-of select="substring-before($line, '=')"/>
         <xsl:value-of select="$NEWLINE"/>
         <xsl:call-template name="java-formatter">
            <xsl:with-param name="line" select="concat('=', substring-after($line, '='))"/>
            <xsl:with-param name="ident" select="$newIdent"/>
         </xsl:call-template>
      </xsl:when>
      <!-- split after comma -->
      <xsl:otherwise>
         <xsl:variable name="cindex">
            <xsl:call-template name="lastIndexOf">
               <xsl:with-param name="str" select="$line"/>
               <xsl:with-param name="index" select="$width"/>
               <xsl:with-param name="substr" select="','"/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:variable name="pindex">
            <xsl:call-template name="lastIndexOf">
               <xsl:with-param name="str" select="$line"/>
               <xsl:with-param name="index" select="$width"/>
               <xsl:with-param name="substr" select="'('"/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:variable name="windex">
            <xsl:call-template name="lastIndexOf">
               <xsl:with-param name="str" select="$line"/>
               <xsl:with-param name="index" select="$width"/>
               <xsl:with-param name="substr" select="' '"/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:variable name="dindex">
            <xsl:call-template name="lastIndexOf">
               <xsl:with-param name="str" select="$line"/>
               <xsl:with-param name="index" select="$width"/>
               <xsl:with-param name="substr" select="'.'"/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:variable name="index">
            <xsl:choose>
               <!-- split after open parent. '(' PRIO 1 -->
               <xsl:when test="$cindex != -1"><xsl:value-of select="$cindex + 1"/></xsl:when>
               <!-- split after open parent. '(' PRIO 2 -->
               <xsl:when test="$pindex != -1"><xsl:value-of select="$pindex"/></xsl:when>
               <!-- split between whitespaces. ' ' PRIO 3-->
               <xsl:when test="$windex != -1"><xsl:value-of select="$windex"/></xsl:when>
               <!-- split *before* dot. '.' PRIO 4 -->
               <xsl:when test="$dindex != -1 and not(starts-with($line, '.'))"><xsl:value-of select="$dindex - 1"/></xsl:when>
               <xsl:otherwise>
                  <!-- panic: don't know how to handle -->
                  <xsl:value-of select="string-length('$line')"/>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:variable>
         <xsl:value-of select="$ident"/>
         <xsl:value-of select="substring($line, 1, $index)"/>
         <xsl:value-of select="$NEWLINE"/>
         <xsl:call-template name="java-formatter">
            <xsl:with-param name="line" select="substring($line, $index + 1)"/>
            <xsl:with-param name="ident" select="$newIdent"/>
            <xsl:with-param name="level" select="$level + 1"/>
         </xsl:call-template>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>


<xsl:template name="lastIndexOf">
   <xsl:param name="str"/>
   <xsl:param name="index"/>
   <xsl:param name="substr"/>
   <xsl:choose>
      <xsl:when test="$index &lt; 1">
         <xsl:message terminate="yes">
            Index must be greater than zero. sub-string '<xsl:value-of select="$substr"/>'
            within '<xsl:value-of select="$str"/>'.
         </xsl:message>
      </xsl:when>
      <xsl:when test="not(contains(substring($str, 1, $index), $substr))">
         <xsl:value-of select="'-1'"/>
      </xsl:when>
      <xsl:when test="substring($str, $index, string-length($substr)) = $substr">
         <xsl:value-of select="$index"/>
      </xsl:when>
      <xsl:otherwise>
         <xsl:call-template name="lastIndexOf">
            <xsl:with-param name="str" select="$str"/>
            <xsl:with-param name="index" select="$index - 1"/>
            <xsl:with-param name="substr" select="$substr"/>
         </xsl:call-template>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

</xsl:stylesheet>
