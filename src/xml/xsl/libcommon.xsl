<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id$

   Collects common XSL templates.

   Author: Michael Griffel
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema">

<!-- ===============================================================
     C O N S T A N T S
     =============================================================== -->
<xsl:variable name="lowercase-a_z" select="'abcdefghijklmnopqrstuvwxyz'"/>
<xsl:variable name="uppercase-a_z" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
<xsl:variable name="magic-hashes"  select="'##########################'"/>

<!-- ===============================================================
          _        _                             _   _
      ___| |_ _ __(_)_ __   __ _   ___  ___  ___| |_(_) ___  _ __
     / __| __| '__| | '_ \ / _` | / __|/ _ \/ __| __| |/ _ \| '_ \
     \__ \ |_| |  | | | | | (_| | \__ \  __/ (__| |_| | (_) | | | |
     |___/\__|_|  |_|_| |_|\__, | |___/\___|\___|\__|_|\___/|_| |_|
                           |___/
     =============================================================== -->
<!-- converts $s to upper case characters -->
<xsl:template name="toUpperCase">
   <xsl:param name="s"/>
   <xsl:value-of select="translate($s,
      $lowercase-a_z,
      $uppercase-a_z)"/>
</xsl:template>

<!-- converts $s to lower case characters -->
<xsl:template name="toLowerCase">
   <xsl:param name="s"/>
   <xsl:value-of select="translate($s,
      $uppercase-a_z,
      $lowercase-a_z)"/>
</xsl:template>

<!--
   Replaces the character 'char' w/ the string 'new'
  -->
<xsl:template name="replace-char">
   <xsl:param name="s" select="''"/>
   <xsl:param name="char" select="''"/>
   <xsl:param name="new" select="''"/>
   <xsl:param name="pos" select="1"/>
   <xsl:if test="$pos &lt;= string-length($s)">
         <!-- Contains upper case character at position $pos? -->
         <xsl:choose>
            <xsl:when test="substring($s, $pos, 1) = $char">
               <xsl:value-of select="$new"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="substring($s, $pos, 1)"/>
            </xsl:otherwise>
         </xsl:choose>
         <xsl:call-template name="replace-char">
            <xsl:with-param name="s" select="$s"/>
            <xsl:with-param name="char" select="$char"/>
            <xsl:with-param name="new" select="$new"/>
            <xsl:with-param name="pos" select="$pos + 1"/>
         </xsl:call-template>
   </xsl:if>
</xsl:template>

<!--
   Replaces the string 'old' w/ the string 'new'
   Note: 'old' must not be a substring of 'new'!
  -->
<xsl:template name="replace-string">
   <xsl:param name="s" select="''"/>
   <xsl:param name="old" select="''"/>
   <xsl:param name="new" select="''"/>
   <xsl:choose>
      <xsl:when test="contains($s, $old)">
         <xsl:variable name="next"><xsl:value-of select="substring-before($s, $old)"/>
         <xsl:value-of select="$new"/>
         <xsl:value-of select="substring-after($s, $old)"/>
         </xsl:variable>
         <xsl:call-template name="replace-string">
            <xsl:with-param name="s" select="$next"/>
            <xsl:with-param name="old" select="$old"/>
            <xsl:with-param name="new" select="$new"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="$s"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>


<!-- ===============================================================
        _                                  _   _
       (_) __ ___   ____ _   ___  ___  ___| |_(_) ___  _ __
       | |/ _` \ \ / / _` | / __|/ _ \/ __| __| |/ _ \| '_ \
       | | (_| |\ V / (_| | \__ \  __/ (__| |_| | (_) | | | |
      _/ |\__,_| \_/ \__,_| |___/\___|\___|\__|_|\___/|_| |_|
     |__/
     =============================================================== -->

<!-- ===============================================================
     Apply jCoderZ classname rule, e.g.e FooID -> FooId
     =============================================================== -->
<xsl:template name="asCamelCase">
   <xsl:param name="s"/>
   <xsl:param name="pos" select="'1'"/>
   <xsl:param name="c_last" select="'a'"/>
   <xsl:variable name="c" select="substring($s, $pos, 1)"/>
   <xsl:variable name="c_next" select="substring(concat($s, 'A'), $pos + 1, 1)"/>
      <xsl:if test="$c">
         <xsl:variable name="c_new">
            <xsl:choose>
               <xsl:when test="translate($c_last, $lowercase-a_z, $magic-hashes) = '#'">
                  <xsl:value-of select="$c"/>
               </xsl:when>
               <xsl:when test="translate($c_next, $lowercase-a_z, $magic-hashes) = '#'">
                  <xsl:value-of select="$c"/>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:value-of select="translate($c, $uppercase-a_z, $lowercase-a_z)"/>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:variable>
         <xsl:value-of select="$c_new"/>
         <xsl:call-template name="asCamelCase">
            <xsl:with-param name="s" select="$s"/>
            <xsl:with-param name="pos" select="$pos + 1"/>
            <xsl:with-param name="c_last" select="$c"/>
         </xsl:call-template>
   </xsl:if>
</xsl:template>

<!-- ===============================================================
     Converts a abbr. to a Java string, e.g. 'RTE' -> 'Rte'
     =============================================================== -->
<xsl:template name="shortnameToJava">
   <xsl:param name="s"/>
   <xsl:value-of select="substring($s, 1, 1)"/><xsl:call-template
      name="toLowerCase"><xsl:with-param name="s"><xsl:value-of
         select="substring($s, 2)"/></xsl:with-param></xsl:call-template>
</xsl:template>

<!--
   Converts a string to a Java constant name
   examples:
      red to RED
      BingoBongoFooBar to BINGO_BONGO_FOO_BAR
      E-LBX-EXPIRED to E_LBX_EXPIRED
      EL CORTE INGLES to EL_CORTE_INGLES
      Visa Electron to VISA_ELECTRON
-->
<xsl:template name="asJavaConstantName">
   <xsl:param name="value"/>
   <xsl:if test="contains('0123456789', substring($value, 1, 1))">
      <xsl:text>V_</xsl:text>
   </xsl:if>
   <xsl:choose>
      <!-- special for ABXtoXYZBingoRequest -->
      <xsl:when test="substring($value, 4, 2) = 'to'
         and not(contains($value, 'ProtocolEngine'))
         and not(contains($value, 'Customer'))">
         <xsl:variable name="mangled-value"><xsl:value-of
            select="substring($value,1,1)"/><xsl:call-template name="toLowerCase">
               <xsl:with-param name="s"><xsl:value-of
                  select="substring($value,2,2)"/></xsl:with-param>
            </xsl:call-template><xsl:text>To</xsl:text><xsl:value-of
               select="substring($value,6,1)"/><xsl:call-template
                  name="toLowerCase">
               <xsl:with-param name="s"><xsl:value-of
                  select="substring($value,7,2)"/></xsl:with-param>
            </xsl:call-template><xsl:value-of select="substring($value, 9)"/>
         </xsl:variable>
         <xsl:call-template name="asJavaConstantName">
            <xsl:with-param name="value"><xsl:value-of
               select="$mangled-value"></xsl:value-of></xsl:with-param>
         </xsl:call-template>
      </xsl:when>
      <!-- no lowercase characters? -->
      <xsl:when test="not(contains(translate($value,
                           $lowercase-a_z,
                           $magic-hashes), '#'))">
         <!-- replace '-', '.' or ' ' with '_' -->
         <xsl:value-of select="translate($value, '- .', '___')"/>
      </xsl:when>
      <!-- whitespaces, '.' or '-' ? -->
      <xsl:when test="contains(translate($value, ' .-', '###'), '#')">
         <!-- replace special chars with '_' and insert Underscore + toUpperCase -->
         <xsl:variable name="foo"><xsl:call-template name="insertUnderscoreBeforeUpperCaseCharacter">
            <xsl:with-param name="s" select="$value"/>
         </xsl:call-template></xsl:variable>
         <xsl:call-template name="toUpperCase">
            <xsl:with-param name="s"><xsl:value-of
               select="translate($foo, ' -.', '___')"/></xsl:with-param>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:call-template name="insertUnderscoreBeforeUpperCaseCharacter">
            <xsl:with-param name="s" select="$value"/>
         </xsl:call-template>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!--
   Converts a CamelCase string to a human readable name.
   examples:
      red to red
      BingoBongoFooBar to bingo bongo foo bar
      E-LBX-EXPIRED to e lbx expired
      EL CORTE INGLES to el corte ingles
      Visa Electron to visa electron
      Visa_Electron to visa electron
  -->
<xsl:template name="asDisplayName">
   <xsl:param name="name"/>
   <xsl:param name="pos">1</xsl:param>
   <xsl:variable name="s" select="translate($name, '_- ', '   ')"/>
   <xsl:if test="$pos &lt;= string-length($s)">
         <!-- Contains upper case character at position $pos? -->
         <xsl:if test="contains(translate(substring($s, $pos, 1),
                              $uppercase-a_z,
                              $magic-hashes),
                                 '#') and $pos != 1">
            <!-- ... and previous character is not uppercase -->
            <xsl:if test="not(contains(translate(substring($s, $pos - 1, 1),
                              $uppercase-a_z,
                              $magic-hashes),
                                 '#')) and not(contains(translate(substring($s, $pos - 1, 1),
                                             ' ', '#'), '#'))">
               <xsl:text> </xsl:text>
            </xsl:if>
         </xsl:if>
         <xsl:call-template name="toLowerCase">
            <xsl:with-param name="s" select="substring($s, $pos, 1)"/>
         </xsl:call-template>
      <xsl:call-template name="asDisplayName">
         <xsl:with-param name="name" select="$name"/>
         <xsl:with-param name="pos" select="$pos + 1"/>
      </xsl:call-template>
   </xsl:if>
</xsl:template>

<!--
   Converts a string to a Java identifier.
   examples:
      FOO_BAR to FooBar
  -->
<xsl:template name="asJavaIdentifier">
   <xsl:param name="name"/>
   <xsl:param name="pos">1</xsl:param>
   <!-- normalize -->
   <xsl:variable name="s" select="translate($name, '-_ ', '   ')"/>
   <xsl:if test="$pos &lt;= string-length($s)">
      <xsl:choose>
         <xsl:when test="$pos = 1"> <!-- First char: force to upper case -->
            <xsl:value-of select="translate(substring($s, 1, 1),
                  $lowercase-a_z, $uppercase-a_z)"/>
         </xsl:when>
         <xsl:when test="substring($s, $pos, 1) = ' '"> <!-- whitespace? -->
         </xsl:when>
         <xsl:when test="substring($s, $pos - 1, 1) = ' '"> <!-- previous whitespace? -->
            <xsl:value-of select="translate(substring($s, $pos, 1),
                  $lowercase-a_z, $uppercase-a_z)"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="translate(substring($s, $pos, 1),
                  $uppercase-a_z, $lowercase-a_z)"/>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:call-template name="asJavaIdentifier">
         <xsl:with-param name="name" select="$name"/>
         <xsl:with-param name="pos" select="$pos + 1"/>
      </xsl:call-template>
   </xsl:if>
</xsl:template>


<!--
   Converts a string to a Java parameter.
   examples:
      FOO_BAR to fooBar
  -->
<xsl:template name="asJavaParameter">
   <xsl:param name="name"/>
   <xsl:param name="pos">1</xsl:param>
   <!-- normalize -->
   <xsl:variable name="s" select="translate($name, '-_ ', '   ')"/>
   <xsl:if test="$pos &lt;= string-length($s)">
      <xsl:if test="$pos = 1"> <!-- First char: force to lower case -->
         <xsl:value-of select="translate(substring($s, 1, 1),
               $uppercase-a_z, $lowercase-a_z)"/>
      </xsl:if>
      <xsl:call-template name="asJavaIdentifier">
         <xsl:with-param name="name" select="$name"/>
         <xsl:with-param name="pos" select="$pos + 1"/>
      </xsl:call-template>
   </xsl:if>
</xsl:template>


<xsl:template name="insertUnderscoreBeforeUpperCaseCharacter">
   <xsl:param name="s"/>
   <xsl:param name="pos">1</xsl:param>
   <xsl:if test="$pos &lt;= string-length($s)">
      <xsl:choose>
         <!-- Contains upper case character at position $pos? -->
         <xsl:when test="contains(translate(substring($s, $pos, 1),
                              $uppercase-a_z,
                              $magic-hashes),
                                 '#') and $pos != 1">
            <!-- ... and previous character is not uppercase nor ' ', '-', '.' or '_' -->
            <xsl:if test="not(contains(translate(substring($s, $pos - 1, 1),
                              $uppercase-a_z,
                              $magic-hashes),
                                 '#'))
                          and not(contains(translate(substring($s, $pos - 1, 1),
                              ' -._', '####'), '#'))">
               <xsl:text>_</xsl:text>
            </xsl:if>
            <xsl:value-of select="substring($s, $pos, 1)"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:call-template name="toUpperCase">
               <xsl:with-param name="s" select="substring($s, $pos, 1)"/>
            </xsl:call-template>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:call-template name="insertUnderscoreBeforeUpperCaseCharacter">
         <xsl:with-param name="s" select="$s"/>
         <xsl:with-param name="pos" select="$pos + 1"/>
      </xsl:call-template>
   </xsl:if>
</xsl:template>

<!-- ===============================================================
     Generates a list of 'string constants' for each node in $items
     =============================================================== -->
<xsl:template name="java-string-constants">
   <xsl:param name="items"/>
   <xsl:param name="javadoc-prefix"/>

   <xsl:for-each select="$items">
   <xsl:sort select="."/>
   /**
    * <xsl:value-of select="$javadoc-prefix"/><xsl:text> &lt;code&gt;</xsl:text><xsl:value-of select="."/><xsl:text>&lt;/code&gt;.</xsl:text>
    */
   public static final String <xsl:call-template name="asJavaConstantName">
      <xsl:with-param name="value" select="."/>
   </xsl:call-template>
         = "<xsl:value-of select="."/>";
  </xsl:for-each>
</xsl:template>


<!-- ===============================================================
     escapes string to a java string.
     =============================================================== -->
<xsl:template name="java-string-escape">
   <xsl:param name="s" select="''"/>
   <xsl:variable name="stepOne">
   <xsl:call-template name="replace-char">
      <xsl:with-param name="s" select="$s"/>
      <xsl:with-param name="char" select="'\'"/>
      <xsl:with-param name="new" select="'\\'"/>
   </xsl:call-template>
   </xsl:variable>
   <xsl:call-template name="replace-char">
      <xsl:with-param name="s" select="$stepOne"/>
      <xsl:with-param name="char" select="'&quot;'"/>
      <xsl:with-param name="new" select="'\&quot;'"/>
   </xsl:call-template>
</xsl:template>


<!-- ===============================================================
     generates a single constant with string constructor
     =============================================================== -->
<xsl:template name="java-constant">
   <xsl:param name="type"/>
   <xsl:param name="name"/>
   <xsl:param name="value"/>
   <xsl:param name="comment"/>
   <xsl:param name="quote-char" select="'&quot;'"/>
   /**<xsl:text> </xsl:text><xsl:value-of select="normalize-space($comment)"/><xsl:text> </xsl:text>*/
   public static final <xsl:value-of select="$type"/><xsl:text> </xsl:text><xsl:value-of select="$name"/>
         = new <xsl:value-of select="$type"/>(<xsl:value-of select="$quote-char"/><xsl:value-of select="$value"/><xsl:value-of select="$quote-char"/>);
</xsl:template>


<!-- ===============================================================
     Value Object Generator
     =============================================================== -->
<xsl:template name="value-object-generator">
   <xsl:param name="classname"/>
   <xsl:param name="package"/>
   <xsl:param name="object"/>

   <xsl:variable name="name"><xsl:call-template
      name="asDisplayName"><xsl:with-param
         name="name" select="$classname"/></xsl:call-template></xsl:variable>
<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

import org.jcoderz.commons.util.HashCodeUtil;
import org.jcoderz.commons.util.ObjectUtil;

/**
 * <xsl:value-of select="$object/description"/><xsl:call-template name="generate-xdoclet">
   <xsl:with-param name="doc-text" select="$object/xdoclet" />
   <xsl:with-param name="indent"><xsl:text> </xsl:text></xsl:with-param>
</xsl:call-template>
 * @author generated
 */
public <xsl:if test="$object/@final = 'true'">final </xsl:if>class <xsl:value-of select="$classname"/><xsl:if test="$object/@serializable">
      implements java.io.Serializable</xsl:if>
{
<xsl:if test="$object/@serializable">
   private static final long serialVersionUID = 1L;</xsl:if>
   <xsl:for-each select="$object//member">
   private <xsl:if test="./@final = 'true' or ../@final = 'true'">final </xsl:if>
      <xsl:value-of select="./@type"/> m<xsl:call-template
      name="asJavaIdentifier">
         <xsl:with-param name="name" select="./@name"/></xsl:call-template>
      <xsl:if test="./@initial-value"> = <xsl:value-of
            select="./@initial-value"/></xsl:if>;</xsl:for-each>

   <xsl:variable name="minimum-argument-count"><xsl:copy-of
      select="count($object//member[not(@initial-value)][@final = 'true' or ../@final = 'true'])"/>
   </xsl:variable>
   <xsl:variable name="maximum-argument-count"><xsl:copy-of
      select="count($object//member[not(@initial-value)])"/>
   </xsl:variable>
   <xsl:if test="$minimum-argument-count != $maximum-argument-count">


   /**
    * Constructs a <xsl:value-of
      select="$classname"/> with the minimum arguments.<xsl:for-each
      select="$object//member[not(@initial-value)][@final = 'true' or ../@final = 'true']">
   <xsl:variable name="identifier"><xsl:call-template
      name="asJavaIdentifier">
         <xsl:with-param name="name" select="./@name"/></xsl:call-template>
   </xsl:variable>
    * @param a<xsl:value-of select="$identifier"/> The <xsl:value-of
    select="normalize-space(.)"/>.</xsl:for-each>
    */
    public <xsl:value-of select="$classname"/> (<xsl:for-each
      select="$object//member[not(@initial-value)][@final = 'true' or ../@final = 'true']">
       <xsl:variable name="identifier"><xsl:call-template
          name="asJavaIdentifier">
             <xsl:with-param name="name" select="./@name"/></xsl:call-template>
       </xsl:variable>
       <xsl:value-of select="./@type"/> a<xsl:value-of select="$identifier"/>
       <xsl:if test="position() != last()">,
       </xsl:if>
       </xsl:for-each>)
    {  <xsl:for-each select="$object//member[not(@initial-value)][@final = 'true' or ../@final = 'true']">
       <xsl:variable name="identifier"><xsl:call-template
          name="asJavaIdentifier">
             <xsl:with-param name="name" select="./@name"/></xsl:call-template>
       </xsl:variable>
       m<xsl:value-of select="$identifier"/> = a<xsl:value-of
            select="$identifier"/>;</xsl:for-each>
    }</xsl:if>

   /**
    * Constructs a <xsl:value-of
      select="$classname"/> with all arguments.<xsl:for-each
      select="$object//member[not(@initial-value)]">
   <xsl:variable name="identifier"><xsl:call-template
      name="asJavaIdentifier">
         <xsl:with-param name="name" select="./@name"/></xsl:call-template>
   </xsl:variable>
    * @param a<xsl:value-of select="$identifier"/> The <xsl:value-of
    select="normalize-space(.)"/>.</xsl:for-each>
    */
    public <xsl:value-of select="$classname"/> (<xsl:for-each
      select="$object//member[not(@initial-value)]">
       <xsl:variable name="identifier"><xsl:call-template
          name="asJavaIdentifier">
             <xsl:with-param name="name" select="./@name"/></xsl:call-template>
       </xsl:variable>
       <xsl:value-of select="./@type"/> a<xsl:value-of select="$identifier"/>
       <xsl:if test="position() != last()">,
       </xsl:if>
       </xsl:for-each>)
    {  <xsl:for-each select="member[not(@initial-value)]">
       <xsl:variable name="identifier"><xsl:call-template
          name="asJavaIdentifier">
             <xsl:with-param name="name" select="./@name"/></xsl:call-template>
       </xsl:variable>
       m<xsl:value-of select="$identifier"/> = a<xsl:value-of
            select="$identifier"/>;</xsl:for-each>
    }

   <xsl:for-each select="$object//member">
   <xsl:variable name="display-name"><xsl:call-template name="asDisplayName"><xsl:with-param
         name="name" select="./@name"/></xsl:call-template>
   </xsl:variable>
   <xsl:variable name="identifier"><xsl:call-template
      name="asJavaIdentifier">
         <xsl:with-param name="name" select="./@name"/></xsl:call-template>
   </xsl:variable>
   <xsl:variable name="doc"><xsl:value-of select="normalize-space(current())"/>
   </xsl:variable>
   /**
    * Returns the <xsl:value-of select="$doc"/>. <xsl:call-template name="generate-xdoclet">
   <xsl:with-param name="doc-text" select="current()/xdoclet" />
   <xsl:with-param name="indent"><xsl:text>    </xsl:text></xsl:with-param>
</xsl:call-template>
    * @return the <xsl:value-of select="$doc"/>.
    */
   public <xsl:value-of select="./@type"/> get<xsl:value-of select="$identifier"/> ()
   {
      return m<xsl:value-of select="$identifier"/>;
   }
   <xsl:if test="not(./@final = 'true' or ../@final = 'true')">

   <xsl:variable name="setter-visibility"><xsl:choose><xsl:when
    test="./@setter-visibility"><xsl:value-of select="./@setter-visibility"/></xsl:when>
    <xsl:otherwise>public</xsl:otherwise></xsl:choose></xsl:variable>

   /**
    * Sets the <xsl:value-of select="$doc"/>.
    * @param a<xsl:value-of select="$identifier"/> Sets the <xsl:value-of select="$doc"/>.
    */
   <xsl:value-of select="$setter-visibility"/> void set<xsl:value-of select="$identifier"/> (<xsl:value-of select="./@type"/> a<xsl:value-of select="$identifier"/>)
   {
      m<xsl:value-of select="$identifier"/> = a<xsl:value-of select="$identifier"/>;
   }</xsl:if>
   </xsl:for-each>
   public String toString()
   {
      final StringBuffer buffer = new StringBuffer();
      buffer.append("[<xsl:value-of select="$classname"/>:");<xsl:for-each
         select="$object//member"><xsl:variable
      name="identifier"><xsl:call-template
         name="asJavaIdentifier">
            <xsl:with-param name="name" select="./@name"/></xsl:call-template>
      </xsl:variable>
      buffer.append(" m<xsl:value-of select="$identifier"/>: ");
      buffer.append(m<xsl:value-of select="$identifier"/>);</xsl:for-each>
      buffer.append("]");
      return buffer.toString();
   }

   /**
    * Override hashCode.
    *
    * @return the Objects hashcode.
    */
   public int hashCode()
   {
      int hashCode = HashCodeUtil.SEED;<xsl:for-each
      select="$object//member[not(@identity-independent)]">
      <xsl:variable name="identifier"><xsl:call-template
         name="asJavaIdentifier">
            <xsl:with-param name="name" select="./@name"/></xsl:call-template>
      </xsl:variable>
      hashCode = HashCodeUtil.hash(hashCode, get<xsl:value-of
            select="$identifier"/>());</xsl:for-each>
      return hashCode;
   }

   /**
    * Returns <code>true</code> if this <code><xsl:value-of select="$classname"/></code>
    * is equal to <tt>object</tt>.
    * @param object the object to compare to.
    * @return <code>true</code> if this <code><xsl:value-of select="$classname"/></code>
    *       is equal to <tt>object</tt>.
    */
   public boolean equals (Object object)
   {
      final boolean result;
      if (this == object)
      {
         result = true;
      }
      else if (object instanceof <xsl:value-of select="$classname"/>)
      {
         final <xsl:value-of select="$classname"/> o = (<xsl:value-of select="$classname"/>) object;
         result = true <xsl:for-each select="$object//member[not(@identity-independent)]">
            <xsl:variable name="identifier"><xsl:call-template
                  name="asJavaIdentifier"><xsl:with-param
                  name="name" select="./@name"/></xsl:call-template>
            </xsl:variable>
               &amp;&amp; ObjectUtil.equals(get<xsl:value-of
                  select="$identifier"/>(), o.get<xsl:value-of
                  select="$identifier"/>())</xsl:for-each>;
      }
      else
      {
         result = false;
      }
      return result;
   }
}

</xsl:template>

<!-- ===============================================================
     Type-safe Enumeration Generator
     =============================================================== -->
<xsl:template name="simple-enum-generator">
   <xsl:param name="classname"/>
   <xsl:param name="package"/>
   <xsl:param name="values"/>
   <xsl:param name="javadoc"/>
   <xsl:variable name="name"><xsl:call-template
      name="asDisplayName"><xsl:with-param
         name="name" select="$classname"/></xsl:call-template></xsl:variable>
   <xsl:variable name="class-javadoc"><xsl:choose>
      <xsl:when test="$javadoc"><xsl:value-of select="$javadoc"/></xsl:when>
      <xsl:otherwise>Enumerated type of a <xsl:value-of select="$name"/>.</xsl:otherwise>
      </xsl:choose></xsl:variable>
   <xsl:variable name="numeric" select="boolean($values/@numeric)"/>
<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;


import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

<xsl:call-template name="simple-enum-generator-import-hook"/>

/**
 * <xsl:value-of select="$class-javadoc"/>
 *
 * Instances of this class are immutable.
 *
 * The following <xsl:value-of select="$name"/>s are defined:
 * &lt;ul&gt;<xsl:for-each select="$values">
 *    &lt;li&gt;<xsl:value-of select="$classname"/>.<xsl:call-template name="asJavaConstantName"><xsl:with-param name="value" select="."/></xsl:call-template><xsl:if test="@numeric"> = <xsl:value-of select="@numeric"/></xsl:if>&lt;/li&gt;</xsl:for-each>
 * &lt;/ul&gt;
 *
 * <xsl:if test="$numeric">The values of this enum have beside the internal
 * sequenicial integer representation that is used for serialization
 * dedicated assigned numeric values that are used in the
 * &lt;code>toInt()&lt;/code> and &lt;code>fromInt()&lt;/code> methods.</xsl:if>
 * <xsl:if test="not($numeric)">The values of this enum have a internal
 * sequenicial integer representation starting with '0'.</xsl:if>
 *
 * @author generated
 */
public final class <xsl:value-of select="$classname"/>
      implements Serializable, org.jcoderz.commons.StrongType
{
   /**
    * The name of this type.
    */
   public static final String TYPE_NAME = "<xsl:value-of select="$classname"/>";

   /** Ordinal of next <xsl:value-of select="$name"/> to be created. */
   private static int sNextOrdinal = 0;

   /** Maps a string representation to an enumerated value. */
   private static final Map
      FROM_STRING = new HashMap();
<xsl:for-each select="$values"><xsl:variable name="constant-name"><xsl:call-template name="asJavaConstantName"><xsl:with-param
      name="value" select="."/></xsl:call-template></xsl:variable><xsl:if test="$numeric">
   /** Numeric representation for <xsl:value-of select="$classname"/><xsl:text> </xsl:text><xsl:value-of select="."/>. */
   public static final int <xsl:value-of select="$constant-name"/>_NUMERIC = <xsl:value-of select="@numeric"/>;
<xsl:if test="not(@numeric)">// FIXME: No Numeric defined in input file for this value.<xsl:message
          >No numeric representation defined for <xsl:value-of select="."/> in enumeration type <xsl:value-of select="$classname"/>.</xsl:message></xsl:if>
</xsl:if><xsl:choose><xsl:when test="not(@description)">
   /** The <xsl:value-of select="$classname"/><xsl:text> </xsl:text><xsl:value-of select="."/>. */</xsl:when><xsl:otherwise>
   /** <xsl:value-of select="./@description"/> (value: <xsl:value-of select="."/>). */</xsl:otherwise></xsl:choose>
   public static final <xsl:value-of select="$classname"/><xsl:text> </xsl:text><xsl:call-template name="asJavaConstantName"><xsl:with-param name="value" select="."/></xsl:call-template>
      = new <xsl:value-of select="$classname"/>("<xsl:value-of select="."/>"<xsl:if
         test="$numeric">, <xsl:value-of select="$constant-name"/>_NUMERIC</xsl:if>);
</xsl:for-each>

   /** The serialVersionUID used for serialization. */
   static final long serialVersionUID = 1;

   /** Internal list of all available <xsl:value-of select="$classname"/>s */
   private static final <xsl:value-of select="$classname"/>[] PRIVATE_VALUES
         =
            {
               <xsl:for-each select="$values">
                  <xsl:value-of select="$classname"/>.<xsl:call-template name="asJavaConstantName"><xsl:with-param name="value" select="."/></xsl:call-template>
               <xsl:if test="position() != last()">
               <xsl:text>,
               </xsl:text>
               </xsl:if></xsl:for-each>
            };

   /** Immutable list of the <xsl:value-of select="$classname"/>s. */
   public static final List VALUES
         = Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));

   /** Assign a ordinal to this <xsl:value-of select="$name"/> */
   private final int mOrdinal = sNextOrdinal++;

   /** The name of the <xsl:value-of select="$name"/> */
   private final transient String mName;
<xsl:if test="$numeric">

   /** The numeric representation of the <xsl:value-of select="$name"/> */
   private final transient int mNumeric;
</xsl:if>
   /** Private Constructor */
   private <xsl:value-of select="$classname"/> (String name<xsl:if
    test="$numeric">, int numeric</xsl:if>)
   {
      mName = name;<xsl:if test="$numeric">
      mNumeric = numeric;</xsl:if>
      FROM_STRING.put(mName, this);
   }

<xsl:if test="not($numeric)">
   /**
    * Creates a <xsl:value-of select="$classname"/> object from its int representation.
    *
    * @param i the integer representation of the <xsl:value-of select="$name"/>.
    * @return the <xsl:value-of select="$classname"/> object represented by this int.
    * @throws ArgumentMalformedException If the assigned int value isn't
    *       listed in the internal <xsl:value-of select="$name"/> table.
    */
   public static <xsl:value-of select="$classname"/> fromInt (int i)
         throws ArgumentMalformedException
   {
      try
      {
         return PRIVATE_VALUES[i];
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         throw new ArgumentMalformedException(
               "<xsl:value-of select="$classname"/>",
               new Integer(i),
               "Illegal int representation of <xsl:value-of select="$classname"/>.");
      }
   }
</xsl:if>

<xsl:if test="$numeric">

   /**
    * Creates a <xsl:value-of select="$classname"/> object from its numeric
    * representation.
    *
    * @param i the integer representation of the <xsl:value-of select="$name"/>.
    * @return the <xsl:value-of select="$classname"/> object represented by this int.
    * @throws ArgumentMalformedException If the assigned int value isn't
    *       listed in the internal <xsl:value-of select="$name"/> table.
    */
   public static <xsl:value-of select="$classname"/> fromInt (int i)
         throws ArgumentMalformedException
   {
       final <xsl:value-of select="$classname"/> result;
       switch (i)
       {<xsl:for-each select="$values"><xsl:variable name="constant-name"><xsl:call-template name="asJavaConstantName"><xsl:with-param
          name="value" select="."/></xsl:call-template></xsl:variable>
           case <xsl:value-of select="constant-name"/>_NUMERIC:
               result = <xsl:value-of select="constant-name"/>;
               break;</xsl:for-each>
           default:
               throw new ArgumentMalformedException(
                     "<xsl:value-of select="$classname"/>",
                     new Integer(i),
                     "Illegal int representation of <xsl:value-of select="$classname"/>.");
      }
      return result;
   }
</xsl:if>

   /**
    * Creates a <xsl:value-of select="$classname"/> object from its String representation.
    *
    * @param str the string representation of the
    *       <xsl:value-of select="$name"/>.
    * @return the <xsl:value-of select="$classname"/> object represented by this str.
    * @throws ArgumentMalformedException If the given str value isn't
    *       listed in the internal <xsl:value-of select="$name"/> table.
    */
   public static <xsl:value-of select="$classname"/> fromString (String str)
         throws ArgumentMalformedException
   {
      final <xsl:value-of select="$classname"/> result
            = (<xsl:value-of select="$classname"/>) FROM_STRING.get(str);
      if (result == null)
      {
         throw new ArgumentMalformedException(
               "<xsl:value-of select="$classname"/>",
               str,
               "Illegal string representation of <xsl:value-of select="$classname"/>, only "
                  + VALUES + " are allowed.");
      }
      return result;
   }

   /**
    * Returns the int representation of this <xsl:value-of select="$name"/>.
    *
    * @return the int representation of this <xsl:value-of select="$name"/>.
    */
   public int toInt ()
   {<xsl:if test="$numeric">
        return mNumeric;</xsl:if><xsl:if test="not($numeric)">
        return mOrdinal;</xsl:if>
   }

   /**
    * Returns the String representation of this <xsl:value-of select="$name"/>.
    *
    * @return the String representation of this <xsl:value-of select="$name"/>.
    */
   public String toString ()
   {
      return mName;
   }

   /**
    * Resolves instances being deserialized to a single instance
    * per <xsl:value-of select="$name"/>.
    */
   private Object readResolve ()
   {
      return PRIVATE_VALUES[mOrdinal];
   }
}
</xsl:template>

<xsl:template name="simple-enum-generator-import-hook" priority="-1">
import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.util.Assert;
</xsl:template>

<!-- ===============================================================
     Restricted string generator
     =============================================================== -->
<xsl:template name="restricted-string">
   <xsl:param name="classname"/>
   <xsl:param name="package"/>
   <xsl:param name="min-length"/>
   <xsl:param name="max-length"/>
   <xsl:param name="constants"/>
   <xsl:param name="token-type" select="''"/>
   <xsl:param name="regex" select="''"/>
   <xsl:variable name="classname-constant">TYPE_NAME</xsl:variable>
   <xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;


import java.io.Serializable;
<xsl:call-template name="restricted-string-import-hook">
   <xsl:with-param name="token-type" select="$token-type"/>
   <xsl:with-param name="regex" select="$regex"/>
</xsl:call-template>

/**
 * Holds the <xsl:value-of select="$classname"/>.
 * &lt;pre&gt;
 * String.type[<xsl:value-of select="$min-length"/>..<xsl:value-of select="$max-length"/>].
<xsl:if test="$regex"> * regular expression: <xsl:value-of select="$regex"/>
</xsl:if> * &lt;/pre&gt;
 * Instances of this class are immutable.
 *
 * @author generated via stylesheet
 */
public final class <xsl:value-of select="$classname"/>
      implements Serializable, org.jcoderz.commons.StrongType
{
   /**
    * The name of this type.
    */
   public static final String TYPE_NAME = "<xsl:value-of select="$classname"/>";

   /** The minimal length of <xsl:value-of select="$classname"/>. */
   public static final int MIN_LENGTH = <xsl:value-of select="$min-length"/>;

   /** The maximal length of <xsl:value-of select="$classname"/>. */
   public static final int MAX_LENGTH = <xsl:value-of select="$max-length"/>;
<xsl:if test="$regex">

   /** The regular expression matching <xsl:value-of select="$classname"/>. */
   public static final String REGULAR_EXPRESSION
         = "<xsl:call-template name="java-string-escape"><xsl:with-param name="s" select="$regex"/></xsl:call-template>";

   /** The compiled pattern for the regular expression. */
   public static final Pattern REGULAR_EXPRESSION_PATTERN
         = Pattern.compile(REGULAR_EXPRESSION);
</xsl:if>

<xsl:for-each select="$constants">
   <xsl:call-template name="java-constant">
      <xsl:with-param name="type" select="$classname"/>
      <xsl:with-param name="name" select="./@name"/>
      <xsl:with-param name="value" select="./@value"/>
      <xsl:with-param name="comment" select="./@comment"/>
   </xsl:call-template>
</xsl:for-each>
   /** The serialVersionUID used for serialization. */
   static final long serialVersionUID = 1;

   /** Holds the <xsl:value-of select="$classname"/>. */
   private final String m<xsl:value-of select="$classname"/>;

   /**
    * Creates a new instance of a <xsl:value-of select="$classname"/>.
    *
    * @param str the <xsl:value-of select="$classname"/> as string representation
    * @throws ArgumentMalformedException If the given string <code>str</code>
    *         violates the restriction of this type.
    *         <xsl:value-of select="$classname"/>.
    */
   private <xsl:value-of select="$classname"/> (final String str)
         throws ArgumentMalformedException
   {
      Assert.notNull(str, TYPE_NAME);<xsl:if test="$min-length != 0">
      if (str.length() &lt; MIN_LENGTH)
      {
         throw new ArgumentMinLengthViolationException(
            <xsl:value-of select="$classname-constant"/>,
            str, new Integer(str.length()), new Integer(MIN_LENGTH),
            <xsl:value-of select="$classname"/>.class);
      }</xsl:if>
      if (str.length() &gt; MAX_LENGTH)
      {
         throw new ArgumentMaxLengthViolationException(
            <xsl:value-of select="$classname-constant"/>,
            str, new Integer(str.length()), new Integer(MAX_LENGTH),
            <xsl:value-of select="$classname"/>.class);
      }<xsl:if test="$regex">
      if (!REGULAR_EXPRESSION_PATTERN.matcher(str).matches())
      {
         throw new ArgumentPatternViolationException(
            <xsl:value-of select="$classname-constant"/>,
            str, REGULAR_EXPRESSION,
            <xsl:value-of select="$classname"/>.class);
      }</xsl:if><xsl:if test="$token-type">
      if (!XsdUtil.isValidToken(str))
      {
         throw new ArgumentMalformedException(
            <xsl:value-of select="$classname-constant"/>,
            str, "Token format restrictions violated.");
      }</xsl:if>
      m<xsl:value-of select="$classname"/> = str;
   }

   /**
    * Creates a <xsl:value-of select="$classname"/> object from the String representation.
    *
    * @param str The str representation of the <xsl:value-of select="$classname"/> to be returned.
    * @return The <xsl:value-of select="$classname"/> object represented by this str.
    * @throws ArgumentMalformedException If the given string <code>str</code>
    *         violates the restriction of this type.
    *         <xsl:value-of select="$classname"/>.
    */
   public static <xsl:value-of select="$classname"/> fromString (String str)
         throws ArgumentMalformedException
   {
      return new <xsl:value-of select="$classname"/>(str);
   }

   /**
    * Returns the String representation of this <xsl:value-of select="$classname"/>.
    *
    * @return The String representation of this <xsl:value-of select="$classname"/>.
    */
   public String toString ()
   {
      return m<xsl:value-of select="$classname"/>;
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    *
    * @param obj the object to compare to.
    * @return true if this object is the same as the obj argument; false
    *         otherwise.
    */
   public boolean equals (Object obj)
   {
      return (obj instanceof <xsl:value-of select="$classname"/>
            &amp;&amp; ((<xsl:value-of select="$classname"/>) obj).m<xsl:value-of select="$classname"/>.equals(
               m<xsl:value-of select="$classname"/>));
   }

   /**
    * Returns the hash code for the <xsl:value-of select="$classname"/>.
    *
    * @return the hash code for the <xsl:value-of select="$classname"/>.
    */
   public int hashCode ()
   {
      return m<xsl:value-of select="$classname"/>.hashCode();
   }
}
</xsl:template>

<xsl:template name="restricted-string-user-type">
   <xsl:param name="classname"/>
   <xsl:param name="type-classname"/>
   <xsl:param name="package"/>
   <xsl:param name="min-length"/>
   <xsl:param name="max-length"/>
   <xsl:variable name="classname-constant">TYPE_NAME</xsl:variable>
   <xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

/**
 * Hibernate user type for the  <xsl:value-of select="$type-classname"/>.
 *
 * @author generated via stylesheet
 */
public final class <xsl:value-of select="$classname"/>
      extends org.jcoderz.commons.util.StringUserTypeBase
{
   /**
    * Null or Empty constant
    */
   private static final <xsl:value-of select="$type-classname"/> EMPTY_OR_NULL
        = <xsl:choose><xsl:when test="$min-length = 0">
          <xsl:value-of select="$type-classname"/>.fromString("");</xsl:when>
          <xsl:otherwise>null;</xsl:otherwise>
          </xsl:choose>

  /**
   * {@inheritDoc}
   */
  public Object fromString(String value)
  {
    return <xsl:value-of select="$type-classname"/>.fromString(value);
  }

  /**
   * {@inheritDoc}
   */
  public Object getEmptyOrNull()
  {
    return EMPTY_OR_NULL;
  }

  /**
   * {@inheritDoc}
   */
  public Class returnedClass()
  {
    return <xsl:value-of select="$type-classname"/>.class;
  }
}
</xsl:template>

<xsl:template name="restricted-string-import-hook" priority="-1">
<xsl:param name="token-type" select="''"/>
<xsl:param name="regex" select="''"/><xsl:if test="$regex">
import java.util.regex.Pattern;
</xsl:if>
import org.jcoderz.commons.ArgumentMinLengthViolationException;
import org.jcoderz.commons.ArgumentMaxLengthViolationException;
import org.jcoderz.commons.ArgumentMalformedException;<xsl:if test="$regex">
import org.jcoderz.commons.ArgumentPatternViolationException;</xsl:if><xsl:if test="$token-type">
import org.jcoderz.commons.util.XsdUtil;</xsl:if>
import org.jcoderz.commons.util.Assert;
</xsl:template>

<!-- ===============================================================
     Restricted long generator
     =============================================================== -->
<xsl:template name="restricted-long">
   <xsl:param name="classname"/>
   <xsl:param name="package"/>
   <xsl:param name="min-value"/>
   <xsl:param name="max-value"/>
   <xsl:param name="constants"/>
   <xsl:variable name="classname-constant">TYPE_NAME</xsl:variable>
   <xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

import java.io.Serializable;

import org.jcoderz.commons.util.HashCodeUtil;
<xsl:call-template name="restricted-long-import-hook" />

/**
 * Holds the <xsl:value-of select="$classname"/>.
 * &lt;pre&gt;
 * long[<xsl:value-of select="$min-value"/>..<xsl:value-of select="$max-value"/>].
 * &lt;/pre&gt;
 * Instances of this class are immutable.
 *
 * @author generated via stylesheet
 */
public final class <xsl:value-of select="$classname"/>
      implements Serializable, org.jcoderz.commons.StrongType
{
   /**
    * The name of this type.
    */
   public static final String TYPE_NAME = "<xsl:value-of select="$classname"/>";

   /**
    * The minimum value of a <xsl:value-of select="$classname"/>.
    */
   public static final long MIN_VALUE = <xsl:value-of select="$min-value"/>;

   /**
    * The maximum value of a <xsl:value-of select="$classname"/>.
    */
   public static final long MAX_VALUE = <xsl:value-of select="$max-value"/>;

<xsl:for-each select="$constants">
   <xsl:call-template name="java-constant">
      <xsl:with-param name="type" select="$classname"/>
      <xsl:with-param name="name" select="./@name"/>
      <xsl:with-param name="value" select="./@value"/>
      <xsl:with-param name="comment" select="./@comment"/>
      <xsl:with-param name="quote-char" select="''"/>
   </xsl:call-template>
</xsl:for-each>
   /** The serialVersionUID used for serialization. */
   static final long serialVersionUID = 1;

   /** Holds the <xsl:value-of select="$classname"/>. */
   private final long m<xsl:value-of select="$classname"/>;
   /** Lazy initialized long object value. */
   private transient Long m<xsl:value-of select="$classname"/>LongObject;
   /** Lazy initialized hash code value. */
   private transient int mHashCode = 0;

   /**
    * Creates a new instance of a <xsl:value-of select="$classname"/>.
    *
    * @param id the <xsl:value-of select="$classname"/> as long representation
    * @throws ArgumentMalformedException If the given long <code>id</code>
    *         violates the restriction of the type
    *         <xsl:value-of select="$classname"/>.
    * @throws ArgumentMinValueViolationException If the value of the given
    *         long <code>id</code> is below <code>MIN_VALUE</code>.
    * @throws ArgumentMaxValueViolationException If the value of the given
    *         long <code>id</code> is above <code>MAX_VALUE</code>.
    */
   private <xsl:value-of select="$classname"/> (long id)
   {
      if (id &lt; MIN_VALUE)
      {
         throw new ArgumentMinValueViolationException(
               TYPE_NAME, new Long(id), new Long(MIN_VALUE),
               <xsl:value-of select="$classname"/>.class);
      }
      if (id &gt; MAX_VALUE)
      {
         throw new ArgumentMaxValueViolationException(
               TYPE_NAME, new Long(id), new Long(MAX_VALUE),
               <xsl:value-of select="$classname"/>.class);
      }
      m<xsl:value-of select="$classname"/> = id;
   }

   /**
    * Construct a <xsl:value-of select="$classname"/> object from its long representation.
    * @param id the long representation of the <xsl:value-of select="$classname"/>
    * @return the <xsl:value-of select="$classname"/> object represented by the given long
    * @throws ArgumentMalformedException If the given long <code>id</code>
    *         violates the restriction of the type
    *         <xsl:value-of select="$classname"/>.
    * @throws ArgumentMinValueViolationException If the value of the given
    *         long <code>id</code> is below <code>MIN_VALUE</code>.
    * @throws ArgumentMaxValueViolationException If the value of the given
    *         long <code>id</code> is above <code>MAX_VALUE</code>.
    */
   public static <xsl:value-of select="$classname"/> fromLong (long id)
         throws ArgumentMalformedException
   {
      return new <xsl:value-of select="$classname"/>(id);
   }

   /**
    * Construct a <xsl:value-of select="$classname"/> object from its string representation.
    * @param s the string representation of the <xsl:value-of select="$classname"/>
    * @return the <xsl:value-of select="$classname"/> object represented by the given string
    * @throws ArgumentMalformedException If the given string <code>s</code>
    *         violates the restriction of the type
    *         <xsl:value-of select="$classname"/>.
    * @throws ArgumentMinValueViolationException If the long value of the given
    *         string <code>s</code> is below <code>MIN_VALUE</code>.
    * @throws ArgumentMaxValueViolationException If the long value of the given
    *         string <code>s</code> is above <code>MAX_VALUE</code>.
    */
   public static <xsl:value-of select="$classname"/> fromString (String s)
         throws ArgumentMalformedException
   {
      final long id;
      try
      {
         id = Long.parseLong(s);
      }
      catch (NumberFormatException e)
      {
         throw new ArgumentMalformedException(
               TYPE_NAME, s, "Invalid string representation", e);
      }
      return new <xsl:value-of select="$classname"/>(id);
   }

   /**
    * Construct a <xsl:value-of select="$classname"/> object from its Long representation.
    * @param id the Long representation of the <xsl:value-of select="$classname"/>
    * @return the <xsl:value-of select="$classname"/> object represented by the given Long
    * @throws ArgumentMalformedException If the given Long <code>id</code>
    *         is null or violates the restriction of the type
    *         <xsl:value-of select="$classname"/>.
    * @throws ArgumentMinValueViolationException If the value of the given
    *         long <code>id</code> is below <code>MIN_VALUE</code>.
    * @throws ArgumentMaxValueViolationException If the value of the given
    *         long <code>id</code> is above <code>MAX_VALUE</code>.
    */
   public static <xsl:value-of select="$classname"/> fromLong (Long id)
         throws ArgumentMalformedException
   {
      Assert.notNull(id, "id");
      return new <xsl:value-of select="$classname"/>(id.longValue());
   }

   /**
    * Generates a random <xsl:value-of select="$classname"/> object.
    * @return a random <xsl:value-of select="$classname"/> object.
    */
   public static <xsl:value-of select="$classname"/> random ()
   {
      return new <xsl:value-of select="$classname"/>(RandomUtil.random(MIN_VALUE, MAX_VALUE));
   }

   /**
    * Returns the long representation of this <xsl:value-of select="$classname"/> object.
    * @return the long representation of this <xsl:value-of select="$classname"/> object.
    */
   public long toLong ()
   {
      return m<xsl:value-of select="$classname"/>;
   }

   /**
    * Returns the Long representation of this <xsl:value-of select="$classname"/> object.
    * @return the Long representation of this <xsl:value-of select="$classname"/> object.
    */
   public Long toLongObject ()
   {
      if (m<xsl:value-of select="$classname"/>LongObject == null)
      {
         m<xsl:value-of select="$classname"/>LongObject = new Long(m<xsl:value-of select="$classname"/>);
      }
      return m<xsl:value-of select="$classname"/>LongObject;
   }

   /** {@inheritDoc} */
   public String toString ()
   {
      return Long.toString(m<xsl:value-of select="$classname"/>);
   }

   /** {@inheritDoc} */
   public boolean equals (Object obj)
   {
      return (obj instanceof <xsl:value-of select="$classname"/>
            &amp;&amp; ((<xsl:value-of select="$classname"/>) obj).m<xsl:value-of select="$classname"/>
               == m<xsl:value-of select="$classname"/>);
   }

   /** {@inheritDoc} */
   public int hashCode ()
   {
      if (mHashCode == 0)
      {
         mHashCode = HashCodeUtil.hash(HashCodeUtil.SEED, m<xsl:value-of select="$classname"/>);
      }
      return mHashCode;
   }
}
</xsl:template>

<xsl:template name="restricted-int-user-type">
   <xsl:param name="classname"/>
   <xsl:param name="type-classname"/>
   <xsl:param name="package"/>
   <xsl:param name="min-length"/>
   <xsl:param name="max-length"/>
   <xsl:variable name="classname-constant">TYPE_NAME</xsl:variable>
   <xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

/**
 * Hibernate user type for the  <xsl:value-of select="$type-classname"/>.
 *
 * @author generated via stylesheet
 */
public final class <xsl:value-of select="$classname"/>
      extends org.jcoderz.commons.util.IntUserTypeBase
{
  /**
   * {@inheritDoc}
   */
  public Object fromInt(int value)
  {
    return <xsl:value-of select="$type-classname"/>.fromInt(value);
  }

  /**
   * {@inheritDoc}
   */
  public int toInt(Object value)
  {
    return ((<xsl:value-of select="$type-classname"/>) value).toInt();
  }

  /**
   * {@inheritDoc}
   */
  public Class returnedClass()
  {
    return <xsl:value-of select="$type-classname"/>.class;
  }
}
</xsl:template>

<xsl:template name="restricted-long-import-hook" priority="-1">
import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.ArgumentMinValueViolationException;
import org.jcoderz.commons.ArgumentMaxValueViolationException;
import org.jcoderz.commons.util.RandomUtil;
import org.jcoderz.commons.util.Assert;
</xsl:template>

<!-- ===============================================================
     String that must match a regex.
     =============================================================== -->
<xsl:template name="regex-string">
   <xsl:param name="classname"/>
   <xsl:param name="package"/>
   <xsl:param name="constants" select="''"/>
   <xsl:param name="regex" select="'FIXME'"/>

   <xsl:variable name="classname-constant"><xsl:text>TYPE_NAME</xsl:text></xsl:variable>

   <xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;


import java.io.Serializable;
import java.util.regex.Pattern;
<xsl:call-template name="regex-string-import-hook"/>

/**
 * Type-safe string type.
 * &lt;pre&gt;
 * regular expression: <xsl:value-of select="$regex"/>
 * &lt;/pre&gt;
 * Instances of this class are immutable.
 *
 * @author generated
 */
public final class <xsl:value-of select="$classname"/>
      implements Serializable, org.jcoderz.commons.StrongType
{
   /**
    * The name of this type.
    */
   public static final String TYPE_NAME = "<xsl:value-of select="$classname"/>";

   /** The regular expression matching <xsl:value-of select="$classname"/>. */
   public static final String REGULAR_EXPRESSION
         = "<xsl:call-template name="java-string-escape"><xsl:with-param name="s" select="$regex"/></xsl:call-template>";

   /** The compiled pattern for the regular expression. */
   public static final Pattern REGULAR_EXPRESSION_PATTERN
         = Pattern.compile(REGULAR_EXPRESSION);
<xsl:for-each select="$constants">
   <xsl:call-template name="java-constant">
      <xsl:with-param name="type" select="$classname"/>
      <xsl:with-param name="name" select="./@name"/>
      <xsl:with-param name="value" select="./@value"/>
      <xsl:with-param name="comment" select="./@comment"/>
   </xsl:call-template>
</xsl:for-each>
   /** The serialVersionUID used for serialization. */
   static final long serialVersionUID = 1;

   /** Holds the <xsl:value-of select="$classname"/>. */
   private final String m<xsl:value-of select="$classname"/>;

   /**
    * Creates a new instance of a <xsl:value-of select="$classname"/>.
    *
    * @param str the <xsl:value-of select="$classname"/> as string representation
    * @throws ArgumentMalformedException If the given string <code>str</code>
    *         does not conform to the Simpay Scheme representation of the
    *         <xsl:value-of select="$classname"/>.
    */
   private <xsl:value-of select="$classname"/> (final String str)
         throws ArgumentMalformedException
   {
      Assert.notNull(str, TYPE_NAME);
      if (!REGULAR_EXPRESSION_PATTERN.matcher(str).matches())
      {
         throw new ArgumentMalformedException(
            <xsl:value-of select="$classname-constant"/>,
            str,
            "Value must match regular expression " + REGULAR_EXPRESSION + ".");
      }

      m<xsl:value-of select="$classname"/> = str;
   }

   /**
    * Creates a <xsl:value-of select="$classname"/> object from SXP String representation.
    *
    * @param str The str representation of the <xsl:value-of select="$classname"/> to be returned.
    * @return The <xsl:value-of select="$classname"/> object represented by this str.
    * @throws ArgumentMalformedException If the given string <code>s</code>
    *         does not conform to the Simpay Interface representation of
    *         <xsl:value-of select="$classname"/>.
    */
   public static <xsl:value-of select="$classname"/> fromString (String str)
         throws ArgumentMalformedException
   {
      return new <xsl:value-of select="$classname"/>(str);
   }

   /**
    * Returns the SXP String representation of this <xsl:value-of select="$classname"/>.
    *
    * @return The SXP String representation of this <xsl:value-of select="$classname"/>.
    */
   public String toString ()
   {
      return m<xsl:value-of select="$classname"/>;
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    *
    * @param obj the object to compare to.
    * @return true if this object is the same as the obj argument; false
    *         otherwise.
    */
   public boolean equals (Object obj)
   {
      return (obj instanceof <xsl:value-of select="$classname"/>
            &amp;&amp; ((<xsl:value-of select="$classname"/>) obj).m<xsl:value-of select="$classname"/>.equals(
               m<xsl:value-of select="$classname"/>));
   }

   /**
    * Returns the hash code for the <xsl:value-of select="$classname"/>.
    *
    * @return the hash code for the <xsl:value-of select="$classname"/>.
    */
   public int hashCode ()
   {
      return m<xsl:value-of select="$classname"/>.hashCode();
   }
}
</xsl:template>

<xsl:template name="regex-string-import-hook" priority="-1">
import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.util.Assert;
</xsl:template>

<!-- ===============================================================
     Outputs the jCoderZ Java copyright header
     =============================================================== -->
<xsl:template name="java-copyright-header" priority="-1">
<xsl:text>/*
 * Generated source file, not in CVS repository
 * Copyright (C) 2006, The jCoderZ Project. All rights reserved.
 */</xsl:text>
</xsl:template>

<!--
  ** This template modifies an EJB-QL query replacing the abstract
  ** schema name with the given parameter $schemaName.
  ** The abstract schema name in the query is identified as the
  ** string between the 'FROM' and 'AS' keywords, like
  ** in "SELECT OBJECT(a) FROM abstractSchema AS a".
  -->
<xsl:template name="replace-schema-in-query">
   <xsl:param name="schemaName"/>
   <xsl:param name="query"/>
   <xsl:variable name="queryLowerCase">
      <xsl:call-template name="toLowerCase">
         <xsl:with-param name="s" select="$query"/>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="beforeFromIndex">
      <xsl:value-of select="string-length(substring-before($queryLowerCase, 'from'))"/>
   </xsl:variable>
   <xsl:variable name="beforeAsIndex">
      <xsl:value-of select="string-length(substring-before($queryLowerCase, 'as'))"/>
   </xsl:variable>
   <xsl:value-of select="substring($query, 1, $beforeFromIndex)"/> FROM <xsl:value-of select="$schemaName"/> <xsl:value-of select="substring($query, $beforeAsIndex)"/>
</xsl:template>

<!--
  ** This template extracts the package from a fully qualified class
  ** name.
  -->
<xsl:template name="package-from-class">
   <xsl:param name="class"/>
   <xsl:param name="count" select="0"/>
   <xsl:if test="contains($class, '.')">
      <xsl:if test="$count &gt; 0">
         <xsl:text>.</xsl:text>
      </xsl:if>
      <xsl:value-of select="substring-before($class, '.')"/>
      <xsl:call-template name="package-from-class">
         <xsl:with-param name="class" select="substring-after($class, '.')"/>
         <xsl:with-param name="count" select="$count + 1"/>
      </xsl:call-template>
   </xsl:if>
</xsl:template>

<!--
  ** Generate complex javadoc structure that might contain xdoclet tags
  ** as sublelements / attributes
  -->
<xsl:template name="generate-xdoclet">
  <xsl:param name="doc-text"/>
  <xsl:param name="indent"/>
  <xsl:apply-templates select="$doc-text" mode="generate-javadoc-">
    <xsl:with-param name="indent" select="$indent"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*" mode="generate-javadoc-">
  <xsl:param name="indent"/>
  <xsl:apply-templates mode="generate-javadoc-content"
    select="*|comment()|processing-instruction()">
      <xsl:with-param name="indent" select="$indent"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="node()" mode="generate-javadoc-content">
  <xsl:param name="indent"/>
<xsl:text>
</xsl:text><xsl:value-of select="$indent"/>* @<xsl:value-of select="name(.)" />
  <xsl:apply-templates
    select="@*" mode="generate-javadoc-attributes"/>
</xsl:template>

<xsl:template match="@*" mode="generate-javadoc-attributes">
 <xsl:text> </xsl:text><xsl:value-of select="name(.)"/><xsl:if
   test="string-length(.) != 0">="<xsl:value-of select="." />"</xsl:if>
</xsl:template>

</xsl:stylesheet>
