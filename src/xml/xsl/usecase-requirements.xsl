<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id$

   Generates the supplementary requirements specification chapter.

   Author: Michael Griffel
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:uc="uc"
   xmlns:req="req"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="req
                       http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                       uc
                       http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd">

<xsl:output method="xml"/>

   <xsl:key name="unique-category-primary-key" match="req:requirement/req:category/req:primary" use="."/>
   <xsl:key name="unique-category-secondary-key" match="req:requirement/req:category/req:secondary" use="."/>
   <xsl:key name="unique-category-tertiary-key" match="req:requirement/req:category/req:tertiary" use="."/>

   <xsl:key name="scope-group" match="uc:usecase" use="concat(@level, '-', uc:scope)"/>

   <!-- main -->
   <xsl:template name="req:requirements">
      <!--chapter>
         <title>Requirements</title-->

         <xsl:for-each select="//req:requirement/req:category/req:primary[generate-id() = generate-id(key('unique-category-primary-key', .))]">

           <xsl:variable name="category" select="."/>

           <chapter>
              <title>
                 <xsl:choose>
                   <xsl:when test="$category = 'Domain Model'"><xsl:value-of select="$strDomainModel"/></xsl:when>
                   <xsl:when test="starts-with($category, 'Role')"><xsl:value-of select="$strRoles"/></xsl:when>
                   <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
                 </xsl:choose>
              </title>

              <xsl:if test="$category = 'Domain Model'">
                  <section>
                    <title><xsl:value-of select="$strCompleteDomainModelDetailed"/></title>
                    <xsl:variable name="f" select="concat('images/', 'domain_model')"/>
                    <figure pgwide="1">
                      <xsl:attribute name="id">
                        <xsl:text>figure.domain_model_complete</xsl:text>
                      </xsl:attribute>
                      <title><xsl:value-of select="$strCompleteDomainModelDetailed"/></title>
                      <mediaobject id="{concat('diagram-', 'domain_model')}">
                        <imageobject role="fo">
                          <imagedata format="SVG" fileref="{concat($f, '.svg')}" align="center" valign="top" scalefit="1"/>
                        </imageobject>
                        <imageobject role="html">
                          <imagedata format="PNG" fileref="{concat($f, '.png')}"/>
                        </imageobject>
                      </mediaobject>
                    </figure>
                  </section>
              </xsl:if>

              <xsl:if test="starts-with($category, 'Role')">
                 <section>
                    <title><xsl:value-of select="$strCompleteRoleDependencies"/></title>
                    <para>
                       <xsl:variable name="f" select="concat('images/', 'roles_model')"/>
                       <figure pgwide="1">
                         <xsl:attribute name="id">
                           <xsl:text>figure.role_model_complete</xsl:text>
                         </xsl:attribute>
                         <title><xsl:value-of select="$strCompleteRoleDependencies"/></title>
                         <mediaobject id="{concat('diagram-', 'role_model')}">
                           <imageobject role="fo">
                             <imagedata format="SVG" fileref="{concat($f, '.svg')}" align="center" valign="top" scalefit="1"/>
                           </imageobject>
                           <imageobject role="html">
                             <imagedata format="PNG" fileref="{concat($f, '.png')}"/>
                           </imageobject>
                         </mediaobject>
                       </figure>
                     </para>
                  </section>
              </xsl:if>

              <xsl:apply-templates select="//req:requirement[req:category/req:primary = $category and not(req:category/req:secondary) and not(req:category/req:tertiary)]">
                <xsl:sort select="req:summary"/>
              </xsl:apply-templates>

              <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
                 <xsl:variable name="sec_category" select="."/>
                 <xsl:if test="//req:requirement[req:category/req:primary = $category and req:category/req:secondary = $sec_category]">
                    <section>
                      <title><xsl:value-of select="."/></title>
                      <xsl:choose>
                        <xsl:when test="$category = 'Domain Model'">
                          <xsl:variable name="f" select="concat('images/', $sec_category, '_domain_model')"/>
                          <figure pgwide="1">
                            <xsl:attribute name="id">
                              <xsl:value-of select="concat('figure.', $sec_category, '_domain_model')" />
                            </xsl:attribute>
                            <title><xsl:value-of select="$strDomainModelForCategory"/><xsl:value-of select="."/>.</title>
                            <mediaobject id="{concat('diagram-', $sec_category, '_domain_model')}">
                              <imageobject  role="fo">
                                <imagedata format="SVG" fileref="{concat($f, '.svg')}" align="center" valign="top" scalefit="1"/>
                              </imageobject>
                              <imageobject role="html">
                                <imagedata format="PNG" fileref="{concat($f, '.png')}"/>
                              </imageobject>
                            </mediaobject>
                          </figure>
                        </xsl:when>

                        <xsl:when test="starts-with($category, 'Role')">
                          <xsl:variable name="f" select="concat('images/', $sec_category, '_roles_model')"/>
                            <figure pgwide="1">
                              <xsl:attribute name="id">
                                <xsl:value-of select="concat('figure.', $sec_category, '_roles_model')" />
                              </xsl:attribute>
                              <title><xsl:value-of select="$strRoleModelForCategory"/><xsl:value-of select="."/>.</title>
                              <mediaobject id="{concat('diagram-', $sec_category, '_roles_model')}">
                                <imageobject role="fo">
                                  <imagedata format="SVG" fileref="{concat($f, '.svg')}" align="center" valign="top" scalefit="1"/>
                                </imageobject>
                                <imageobject role="html">
                                  <imagedata format="PNG" fileref="{concat($f, '.png')}"/>
                                </imageobject>
                              </mediaobject>
                            </figure>
                         </xsl:when>
                       </xsl:choose>

                       <xsl:apply-templates select="//req:requirement[req:category/req:primary = $category and req:category/req:secondary = $sec_category and not(req:category/req:tertiary)]">
                         <xsl:sort select="req:summary"/>
                       </xsl:apply-templates>

                       <xsl:for-each select="//req:requirement/req:category/req:tertiary[generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
                          <xsl:variable name="thi_category" select="."/>
                          <xsl:if test="//req:requirement[req:category/req:primary = $category and req:category/req:secondary = $sec_category and req:category/req:tertiary = $thi_category]">
                             <section>
                               <title><xsl:value-of select="."/></title>
                               <xsl:choose>
                                 <xsl:when test="$category = 'Domain Model'">
                                   <xsl:variable name="f" select="concat('images/', $sec_category, '_', $thi_category, '_domain_model')"/>
                                   <figure>
                                     <xsl:attribute name="id">
                                       <xsl:value-of select="concat('figure.', $sec_category, '_', $thi_category, '_domain_model')" />
                                     </xsl:attribute>
                                     <title><xsl:value-of select="$strDomainModelForCategory"/><xsl:value-of select="."/></title>
                                     <mediaobject id="{concat('diagram-', $sec_category, '_', $thi_category, '_domain_model')}">
                                        <imageobject role="fo">
                                            <imagedata format="SVG" fileref="{concat($f, '.svg')}" align="center" valign="top" scalefit="1"/>
                                         </imageobject>
                                         <imageobject role="html">
                                            <imagedata format="PNG" fileref="{concat($f, '.png')}"/>
                                         </imageobject>
                                      </mediaobject>
                                    </figure>
                                  </xsl:when>

                                  <xsl:when test="starts-with($category, 'Role')">
                                   <xsl:variable name="f" select="concat('images/', $sec_category, '_', $thi_category, '_roles_model')"/>
                                   <figure>
                                     <xsl:attribute name="id">
                                       <xsl:value-of select="concat('figure.', $sec_category, '_', $thi_category, '_roles_model')" />
                                     </xsl:attribute>
                                     <title><xsl:value-of select="$strRoleModelForCategory"/><xsl:value-of select="."/></title>
                                     <mediaobject id="{concat('diagram-', $sec_category, '_', $thi_category, '_roles_model')}">
                                       <imageobject role="fo">
                                         <imagedata format="SVG" fileref="{concat($f, '.svg')}" align="center" valign="top" scalefit="1"/>
                                       </imageobject>
                                       <imageobject role="html">
                                         <imagedata format="PNG" fileref="{concat($f, '.png')}"/>
                                       </imageobject>
                                     </mediaobject>
                                   </figure>
                                 </xsl:when>
                               </xsl:choose>

                               <xsl:apply-templates select="//req:requirement[req:category/req:primary = $category and req:category/req:secondary = $sec_category and req:category/req:tertiary = $thi_category]">
                                 <xsl:sort select="req:summary"/>
                               </xsl:apply-templates>
                             </section>
                          </xsl:if>
                       </xsl:for-each>
                    </section>
                 </xsl:if>
              </xsl:for-each>

           </chapter>
        </xsl:for-each>
      <!--/chapter-->
   </xsl:template>

<xsl:template match="req:requirement">
  <section id="{req:key}">
    <title>
      <xsl:value-of select="req:summary"/>
      <xsl:text> </xsl:text>
      <xsl:text>[</xsl:text><xsl:value-of select="req:key"/><xsl:text>]</xsl:text>
    </title>
    <para>
      <informaltable frame='none'>
      <tgroup cols='2' align='left' colsep='0' rowsep='0'>
      <colspec colwidth='1.5in'/>
      <colspec colwidth='4in'/>
      <tbody>
        <xsl:if test="req:priority">
          <xsl:if test="not(req:term)">
           <row><entry><emphasis role="bold"><xsl:value-of select="$strPriority"/>:</emphasis></entry><entry><xsl:value-of select="req:priority"/></entry></row>
          </xsl:if>
        </xsl:if>
        <xsl:if test="req:status">
           <row><entry><emphasis role="bold"><xsl:value-of select="$strStatus"/>:</emphasis></entry><entry><xsl:value-of select="req:status"/></entry></row>
        </xsl:if>
        <row><entry><emphasis role="bold"><xsl:value-of select="$strReleaseVersion"/>:</emphasis></entry><entry><xsl:value-of select="normalize-space(translate(substring-after(req:version, '$Revision:'), '$', ' '))"/></entry></row>
      </tbody>
      </tgroup>
      </informaltable>
    </para>
    <xsl:variable name="r" select="req:key"/>
    <xsl:if test="//uc:usecase//uc:ref[@id = $r]">
       <para>
          <xsl:value-of select="$strThisRequirementIsReferencedByTheFollowingUseCases"/>:
          <itemizedlist>
             <xsl:apply-templates select="//uc:usecase[descendant-or-self::uc:ref[@id = $r]]" mode="ref_to_req"/>
          </itemizedlist>
       </para>
    </xsl:if>
    <para><xsl:apply-templates select="req:description"/></para>
    <para><xsl:apply-templates select="req:entity"/></para>
    <para><xsl:apply-templates select="req:role"/></para>
    <para><xsl:apply-templates select="req:term"/></para>
    <xsl:if test="req:open_issue">
       <para>
          <itemizedlist>
             <title><xsl:value-of select="$strOpenIssues"/></title>
             <xsl:apply-templates select="req:open_issue"/>
          </itemizedlist>
       </para>
    </xsl:if>
  </section>
</xsl:template>

<xsl:template match="uc:usecase" mode="ref_to_req">
   <listitem><xref linkend="{@id}"/></listitem>
</xsl:template>

   <xsl:template match="req:entity">
      <table frame="all">
         <title><xsl:value-of select="$strEntitySummary"/></title>
         <tgroup cols='5' align='left' colsep='1' rowsep='1'>
            <colspec colname="c1"/>
            <colspec colname="c2"/>
            <colspec colname="c3"/>
            <colspec colname="c4"/>
            <colspec colname="c5"/>
            <spanspec spanname="hspan" namest="c2" nameend="c5" align="left"/>
            <spanspec spanname="hspan2" namest="c1" nameend="c5" align="center"/>
            <tbody>
               <row>
                  <entry spanname="hspan2">
                     <emphasis role="bold"><glossterm><xsl:value-of select="req:name"/></glossterm></emphasis>
                  </entry>
               </row>
               <xsl:if test="req:alternative_name">
                  <row>
                     <entry spanname="hspan2">
                        <xsl:apply-templates select="req:alternative_name"/>
                     </entry>
                  </row>
               </xsl:if>
               <row>
                  <entry>
                     <emphasis role="bold"><xsl:value-of select="$strConstraints"/></emphasis>
                  </entry>
                  <entry spanname="hspan">
                     <xsl:value-of select="req:constraints"/>
                  </entry>
               </row>
               <row>
                  <entry>
                     <emphasis role="bold"><xsl:value-of select="$strAttribute"/></emphasis>
                  </entry>
                  <entry>
                     <emphasis role="bold"><xsl:value-of select="$strReferenceTo"/></emphasis>
                  </entry>
                  <entry>
                     <emphasis role="bold"><xsl:value-of select="$strRelation"/></emphasis>
                  </entry>
                  <entry>
                     <emphasis role="bold"><xsl:value-of select="$strConstraints"/></emphasis>
                  </entry>
                  <entry>
                     <emphasis role="bold"><xsl:value-of select="$strPattern"/></emphasis>
                  </entry>
               </row>
               <xsl:for-each select="req:attribute">
                  <row title="{req:name}">
                     <xsl:choose>
                        <xsl:when test="req:description and req:alternative_name">
                           <entry morerows='2' valign='middle' id="{concat(concat(../../req:key,'-'),req:name)}" xreflabel="{concat(concat(../../req:key,'-'),req:name)}">
                              <glossterm><xsl:value-of select="req:name"/></glossterm>
                           </entry>
                        </xsl:when>
                        <xsl:when test="req:description or req:alternative_name">
                           <entry morerows='1' valign='middle' id="{concat(concat(../../req:key,'-'),req:name)}" xreflabel="{concat(concat(../../req:key,'-'),req:name)}">
                              <glossterm><xsl:value-of select="req:name"/></glossterm>
                           </entry>
                        </xsl:when>
                        <xsl:otherwise>
                           <entry id="{concat(concat(../../req:key,'-'),req:name)}" xreflabel="{concat(concat(../../req:key,'-'),req:name)}">
                              <glossterm><xsl:value-of select="req:name"/></glossterm>
                           </entry>
                        </xsl:otherwise>
                     </xsl:choose>
                     <entry>
                        <xsl:apply-templates select="req:objectreference"/>
                     </entry>
                     <entry>
                        <xsl:if test="req:objectreference">
                           <xsl:value-of select="req:objectreference/req:linkstart"/><xsl:text> : </xsl:text><xsl:value-of select="req:objectreference/req:linkend"/>
                        </xsl:if>
                     </entry>
                     <entry>
                        <xsl:value-of select="req:constraints"/>
                     </entry>
                     <entry>
                        <xsl:value-of select="req:pattern"/>
                     </entry>
                  </row>
                  <xsl:if test="req:alternative_name">
                     <row>
                        <entry spanname="hspan"><xsl:value-of select="req:alternative_name"/></entry>
                     </row>
                  </xsl:if>
                  <xsl:if test="req:description">
                     <row>
                        <entry spanname="hspan"><xsl:value-of select="req:description"/></entry>
                     </row>
                  </xsl:if>
               </xsl:for-each>
           </tbody>
         </tgroup>
      </table>

      <xsl:variable name="f" select="concat('images/', ../req:key)"/>
      <figure>
       <title><xsl:value-of select="$strDomainModelDiagramForEntity"/><xsl:value-of select="../req:key"/><xsl:text> </xsl:text><xsl:value-of select="req:name"/></title>
       <mediaobject id="{concat('diagram-', ../req:key)}">
         <imageobject role="fo">
           <imagedata format="SVG"  fileref="{concat($f, '.svg')}" align="center" valign="top" scalefit="1"/>
         </imageobject>
         <imageobject role="html">
           <imagedata format="PNG"  fileref="{concat($f, '.png')}"/>
         </imageobject>
       </mediaobject>
     </figure>
   </xsl:template>

   <xsl:template match="req:attribute">
     <listitem>
        <para>
           <xsl:value-of select="req:name"/><xsl:text> : </xsl:text><xsl:apply-templates select="req:objectreference"/>
        </para>
     </listitem>
   </xsl:template>

   <xsl:template match="req:objectreference">
        [<xsl:value-of select="req:linkstart"/>/<xsl:value-of select="req:linkend"/><xsl:text>] </xsl:text> <xref linkend="{req:ref/@id}"/>
   </xsl:template>

   <xsl:template match="req:alternative_name">
      <itemizedlist>
         <xsl:apply-templates select="req:name" mode="alternate"/>
      </itemizedlist>
   </xsl:template>

   <xsl:template match="req:name" mode="alternate">
      <listitem>
         <para>(<xsl:value-of select="@lang"/>)<xsl:text> </xsl:text><xsl:value-of select="."/><indexterm>
            <primary><xsl:value-of select="../../../req:title"/></primary>
            <secondary><xsl:value-of select="@lang"/></secondary>
            <tertiary><xsl:value-of select="."/></tertiary>
         </indexterm></para>
      </listitem>
   </xsl:template>

   <xsl:template match="req:superior">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="req:superior" mode="subordinate">
      <xsl:text> [</xsl:text><xref linkend="{../../req:key}"/><xsl:text>]
      </xsl:text>
   </xsl:template>

   <xsl:template name="uc:list_acting_use_cases_indirect">
      <xsl:param name="role_name"/>
      <xsl:for-each select="//req:role/req:superior/req:ref[@id = //req:role[req:name = $role_name]/../req:key]">
         <xsl:call-template name="uc:list_acting_use_cases">
            <xsl:with-param name="role_name" select="../../req:name"/>
         </xsl:call-template>
         <!-- deep recusrive list of use cases -->
         <xsl:call-template name="uc:list_acting_use_cases_indirect">
            <xsl:with-param name="role_name" select="../../req:name"/>
         </xsl:call-template>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="uc:list_acting_use_cases">
      <xsl:param name="role_name"/>
      <xsl:for-each select="//uc:usecase/uc:actors/uc:primary[uc:name = $role_name]">
         <xsl:text> [</xsl:text><xref linkend="{../../@id}"/><xsl:text>]
         </xsl:text>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="req:role">
      <table frame="all">
         <title><xsl:value-of select="$strRoleSummary"/></title>
         <tgroup cols='5' align='left' colsep='1' rowsep='1'>
            <colspec colname="c1"/>
            <colspec colname="c2"/>
            <colspec colname="c3"/>
            <colspec colname="c4"/>
            <colspec colname="c5"/>
            <spanspec spanname="hspan" namest="c2" nameend="c5" align="left"/>
            <spanspec spanname="hspan2" namest="c1" nameend="c5" align="center"/>
            <tbody>
               <row>
                  <entry spanname="hspan2">
                     <emphasis role="bold"><xsl:value-of select="req:name"/></emphasis>
                  </entry>
               </row>
               <xsl:if test="req:alternative_name/req:name">
                  <row>
                     <entry>
                        <emphasis role="bold"><xsl:value-of select="$strAlias"/></emphasis>
                     </entry>
                     <entry spanname="hspan">
                        <xsl:apply-templates select="req:alternative_name"/>
                     </entry>
                  </row>
               </xsl:if>
               <xsl:if test="req:superior">
                  <row>
                     <entry>
                        <emphasis role="bold"><xsl:value-of select="$strSuperior"/></emphasis>
                     </entry>
                     <entry spanname="hspan">
                        <xsl:apply-templates select="req:superior"/>
                     </entry>
                  </row>
               </xsl:if>
               <xsl:if test="../req:key = //req:superior/req:ref/@id">
                  <xsl:variable name="sup_key" select="../req:key"/>
                  <row>
                     <entry>
                        <emphasis role="bold"><xsl:value-of select="$strSubordinates"/></emphasis>
                     </entry>
                     <entry spanname="hspan">
                        <xsl:apply-templates select="//req:superior[req:ref/@id = $sup_key]"
                                             mode="subordinate"/>
                     </entry>
                  </row>
               </xsl:if>
               <row>
                  <entry>
                     <emphasis role="bold"><xsl:value-of select="$strActingUseCasesDirectly"/></emphasis>
                  </entry>
                  <entry spanname="hspan">
                     <xsl:variable name="role_name" select="req:name"/>
                     <xsl:for-each select="//uc:usecase/uc:actors/uc:primary[uc:name = $role_name]">
                        <xsl:text> [</xsl:text><xref linkend="{../../@id}"/><xsl:text>]</xsl:text>
                     </xsl:for-each>
                  </entry>
               </row>
               <row>
                  <entry>
                     <emphasis role="bold"><xsl:value-of select="$strActingUseCasesIndirectly"/></emphasis>
                  </entry>
                  <entry spanname="hspan">
                     <xsl:variable name="role_name" select="req:name"/>
                     <xsl:call-template name="uc:list_acting_use_cases_indirect">
                        <xsl:with-param name="role_name" select="$role_name"/>
                     </xsl:call-template>
                  </entry>
               </row>
           </tbody>
         </tgroup>
      </table>

      <xsl:variable name="f" select="concat('images/', ../req:key)"/>
      <figure>
       <title><xsl:value-of select="$strRoleDiagramForRole"/><xsl:value-of select="../req:key"/><xsl:text> </xsl:text><xsl:value-of select="req:name"/></title>
       <mediaobject id="{concat('diagram-', ../req:key)}">
         <imageobject role="fo">
             <imagedata format="SVG"  fileref="{concat($f, '.svg')}" align="center" valign="top" scalefit="1"/>
          </imageobject>
          <imageobject role="html">
             <imagedata format="PNG"  fileref="{concat($f, '.png')}"/>
          </imageobject>
        </mediaobject>
      </figure>
   </xsl:template>

   <xsl:template match="req:term">
      <indexterm>
        <primary><xsl:value-of select="req:name"/></primary>
      </indexterm>
      <xsl:if test="req:alternative_name/req:name">
         <table frame="all">
            <title><xsl:value-of select="$strTermSummary"/><xsl:value-of select="req:name"/></title>
            <tgroup cols='2' align='left' colsep='1' rowsep='1'>
               <colspec colname="c1"/>
               <colspec colname="c2"/>
               <spanspec spanname="hspan" namest="c2" nameend="c2" align="left"/>
               <spanspec spanname="hspan2" namest="c1" nameend="c1" align="center"/>
               <tbody>
                  <row>
                     <entry>
                        <emphasis role="bold"><xsl:value-of select="req:name"/></emphasis>
                     </entry>
                     <xsl:if test="req:acronym">
	                     <entry spanname="hspan">
	                       <xsl:value-of select="$strAcronym"/>: <emphasis role="bold"><xsl:value-of select="req:acronym"/></emphasis>
	                     </entry>
                     </xsl:if>
                  </row>
                  <xsl:if test="req:alternative_name/req:name">
                     <row>
                        <entry>
                           <emphasis role="bold"><xsl:value-of select="$strAlias"/></emphasis>
                        </entry>
                        <entry spanname="hspan">
                           <xsl:apply-templates select="req:alternative_name"/>
                        </entry>
                     </row>
                  </xsl:if>
              </tbody>
            </tgroup>
         </table>
      </xsl:if>
   </xsl:template>

   <xsl:template match="req:description">
      <xsl:value-of select="req:description"/><xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="req:open_issue">
      <listitem>
         <para>
            <xsl:apply-templates/>
         </para>
      </listitem>
   </xsl:template>


   <xsl:template name="uc:list_acting_use_cases_indirect_upwards">
      <xsl:param name="role_name"/>
      <xsl:for-each select="//req:role[../req:key = //req:role[normalize-space(req:name) = $role_name]/req:superior/req:ref/@id]">
         <xsl:text> [</xsl:text><xref linkend="{../req:key}"/><xsl:text>] </xsl:text>
         <!-- deep recusrive list of use cases -->
         <xsl:call-template name="uc:list_acting_use_cases_indirect_upwards">
            <xsl:with-param name="role_name" select="req:key"/>
         </xsl:call-template>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="uc:list_roles_usecases">
      <xsl:for-each select="//uc:usecase[generate-id() = generate-id(key('scope-group', concat(@level, '-', uc:scope)))]">
         <xsl:variable name="scope_name" select="uc:scope"/>
         <section>
            <title><xsl:value-of select="uc:scope"/></title>
            <informaltable>
               <tgroup cols="2">
                  <tbody>
                     <xsl:for-each select="//uc:usecase[uc:scope = $scope_name]">
                       <row id="role_uc_list {uc:id}" label="{uc:name}">
                        <entry>
                           <xsl:text> [</xsl:text><xref linkend="{@id}"/><xsl:text>] : </xsl:text>
                        </entry>
                        <entry>
                           <xsl:for-each select="uc:actors/uc:primary">
                              <xsl:variable name="role_name" select="normalize-space(uc:name)"/>
                              <xsl:text> [</xsl:text><xref linkend="{//req:requirement[normalize-space(req:role/req:name) = $role_name]/req:key}"/><xsl:text>] </xsl:text>
                              <xsl:call-template name="uc:list_acting_use_cases_indirect_upwards">
                                 <xsl:with-param name="role_name" select="$role_name"/>
                              </xsl:call-template>
                           </xsl:for-each>
                        </entry>
                      </row>
                     </xsl:for-each>
                  </tbody>
               </tgroup>
            </informaltable>
         </section>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="req:ref">
     <xsl:text> [</xsl:text><xref linkend="{@id}"/><xsl:text>] </xsl:text>
   </xsl:template>

   <xsl:template match="req:requirement" mode="issue_list">
      <xsl:if test="req:open_issue">
         <itemizedlist>
            <listitem>
               <para><xsl:text>[</xsl:text><xref linkend="{req:key}"/><xsl:text>] : </xsl:text>
                  <itemizedlist>
                     <xsl:apply-templates select="req:open_issue"/>
                  </itemizedlist>
               </para>
            </listitem>
         </itemizedlist>
      </xsl:if>
   </xsl:template>

</xsl:stylesheet>
