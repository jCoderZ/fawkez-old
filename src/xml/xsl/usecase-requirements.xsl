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
   
   <xsl:key name="scope-group" match="uc:usecase" use="uc:scope"/>

   <!-- main -->
   <xsl:template name="req:requirements">
      <!--chapter>
         <title>Requirements</title-->

         <xsl:for-each select="//req:requirement/req:category/req:primary[generate-id() = generate-id(key('unique-category-primary-key', .))]">
           <chapter>
              <title><xsl:value-of select="."/></title>
              <xsl:variable name="category" select="."/>
              
              <xsl:if test="$category = 'Domain Model'">
                 <section>
                    <title>Complete Domain Model</title>
                    <para>
                       <xsl:variable name="f" select="concat('images/', 'domain_model')"/>
                       <mediaobject  id="{concat('diagram-', 'domain_model')}">
                          <imageobject  role="fo">
                              <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
                           </imageobject>
                           <imageobject  role="html">
                              <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
                           </imageobject>
                        </mediaobject>
                     </para>
                  </section>
              </xsl:if>
              
              <xsl:if test="starts-with($category, 'Role')">
                 <section>
                    <title>Complete Role Dependencies</title>
                    <para>
                       <xsl:variable name="f" select="concat('images/', 'roles_model')"/>
                       <mediaobject  id="{concat('diagram-', 'role_model')}">
                          <imageobject  role="fo">
                              <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
                           </imageobject>
                           <imageobject  role="html">
                              <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
                           </imageobject>
                        </mediaobject>
                     </para>
                  </section>
              </xsl:if>
              
              <xsl:apply-templates select="//req:requirement[req:category/req:primary = $category and not(req:category/req:secondary) and not(req:category/req:tertiary)]">
                 <xsl:sort select="text()"/>
              </xsl:apply-templates>
                            
              <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
                 <xsl:variable name="sec_category" select="."/>
                 <xsl:if test="//req:requirement[req:category/req:primary = $category and req:category/req:secondary = $sec_category]">
                    <section>
                       <title><xsl:value-of select="."/></title>
                       
                       <xsl:if test="$category = 'Domain Model'">
                          <xsl:variable name="f" select="concat('images/', $sec_category, '_domain_model')"/>
                          <mediaobject  id="{concat('diagram-', $sec_category, '_domain_model')}">
                             <title>Domain model for category <xsl:value-of select="."/>.</title>
                             <imageobject  role="fo">
                                 <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
                              </imageobject>
                              <imageobject  role="html">
                                 <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
                              </imageobject>
                           </mediaobject>
                        </xsl:if>
                        
                        <xsl:if test="starts-with($category, 'Role')">
                          <xsl:variable name="f" select="concat('images/', $sec_category, '_roles_model')"/>
                          <mediaobject  id="{concat('diagram-', $sec_category, '_roles_model')}">
                             <title>Role model for category <xsl:value-of select="."/>.</title>
                             <imageobject  role="fo">
                                 <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
                              </imageobject>
                              <imageobject  role="html">
                                 <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
                              </imageobject>
                           </mediaobject>
                        </xsl:if>
                       
                       <xsl:apply-templates select="//req:requirement[req:category/req:primary = $category and req:category/req:secondary = $sec_category and not(req:category/req:tertiary)]">
                          <xsl:sort select="text()"/>
                       </xsl:apply-templates>
                       
                       <xsl:for-each select="//req:requirement/req:category/req:tertiary[generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
                          <xsl:variable name="thi_category" select="."/>
                          <xsl:if test="//req:requirement[req:category/req:primary = $category and req:category/req:secondary = $sec_category and req:category/req:tertiary = $thi_category]">
                             <section>
                                <title><xsl:value-of select="."/></title>
                                
                                <xsl:if test="$category = 'Domain Model'">
                                   <xsl:variable name="f" select="concat('images/', $sec_category, '_', $thi_category, '_domain_model')"/>
                                   <title>Domain model for category <xsl:value-of select="."/></title>
                                   <mediaobject  id="{concat('diagram-', $sec_category, '_', $thi_category, '_domain_model')}">
                                      <caption>Domain model for category <xsl:value-of select="."/>.</caption>
                                      <imageobject  role="fo">
                                          <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
                                       </imageobject>
                                       <imageobject  role="html">
                                          <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
                                       </imageobject>
                                    </mediaobject>
                                 </xsl:if>
                                 
                                 <xsl:if test="starts-with($category, 'Role')">
                                   <xsl:variable name="f" select="concat('images/', $sec_category, '_', $thi_category, '_roles_model')"/>
                                   <title>Domain model for category <xsl:value-of select="."/></title>
                                   <mediaobject  id="{concat('diagram-', $sec_category, '_', $thi_category, '_roles_model')}">
                                      <caption>Domain model for category <xsl:value-of select="."/>.</caption>
                                      <imageobject  role="fo">
                                          <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
                                       </imageobject>
                                       <imageobject  role="html">
                                          <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
                                       </imageobject>
                                    </mediaobject>
                                 </xsl:if>
                                
                                <xsl:apply-templates select="//req:requirement[req:category/req:primary = $category and req:category/req:secondary = $sec_category and req:category/req:tertiary = $thi_category]">
                                   <xsl:sort select="text()"/>
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
      <xsl:text>[</xsl:text><xsl:value-of select="req:key"/><xsl:text>]</xsl:text>
      <xsl:text> </xsl:text>
      <xsl:value-of select="req:summary"/>
    </title>
    <para>
      <informaltable frame='none'>
      <tgroup cols='2' align='left' colsep='0' rowsep='0'>
      <colspec colwidth='1.5in'/>
      <colspec colwidth='4in'/>
      <tbody>
        <row><entry><emphasis role="bold">Priority:</emphasis></entry><entry><xsl:value-of select="req:priority"/></entry></row>
        <row><entry><emphasis role="bold">Status:</emphasis></entry><entry><xsl:value-of select="req:status"/></entry></row>
        <row><entry><emphasis role="bold">Release Version:</emphasis></entry><entry><xsl:value-of select="req:version"/></entry></row>
      </tbody>
      </tgroup>
      </informaltable>
    </para>
    <xsl:variable name="r" select="req:key"/>
    <xsl:if test="//uc:usecase//uc:ref[@id = $r]">
       <para>
          This requirement is referenced by following use cases:
          <itemizedlist>
             <xsl:apply-templates select="//uc:usecase[descendant-or-self::uc:ref[@id = $r]]" mode="ref_to_req"/>
          </itemizedlist>
       </para>
    </xsl:if>
    <para><xsl:apply-templates select="req:description"/></para>
    <para><xsl:apply-templates select="req:entity"/></para>
    <para><xsl:apply-templates select="req:role"/></para>
    <xsl:if test="req:open_issue">
       <para>
          <itemizedlist>
             <title>Open Issues</title>
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
         <title>entity summary</title>
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
               <row>
                  <entry>
                     <emphasis role="bold">Constraints</emphasis>
                  </entry>
                  <entry spanname="hspan">
                     <xsl:value-of select="req:constraints"/>
                  </entry>
               </row>
               <row>
                  <entry>
                     <emphasis role="bold">Attribute</emphasis>
                  </entry>
                  <entry>
                     <emphasis role="bold">Reference To</emphasis>
                  </entry>
                  <entry>
                     <emphasis role="bold">Relation</emphasis>
                  </entry>
                  <entry>
                     <emphasis role="bold">Constraints</emphasis>
                  </entry>
                  <entry>
                     <emphasis role="bold">Pattern</emphasis>
                  </entry>
               </row>
               <xsl:for-each select="req:attribute">
                  <row>
                     <xsl:choose>
                        <xsl:when test="req:description">
                           <entry morerows='1' valign='middle'>
                              <xsl:value-of select="req:name"/>
                           </entry>
                        </xsl:when>
                        <xsl:otherwise>
                           <entry>
                              <xsl:value-of select="req:name"/>
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
      <mediaobject  id="{concat('diagram-', ../req:key)}">
         <imageobject  role="fo">
             <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
          </imageobject>
          <imageobject  role="html">
             <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
          </imageobject>
          <caption>
             <para>
                Domain Model diagram for entity <xsl:value-of select="../req:key"/><xsl:text> </xsl:text><xsl:value-of select="req:name"/>.
             </para>
          </caption>
       </mediaobject>
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
         <para>(<xsl:value-of select="@lang"/>)<xsl:text> </xsl:text><xsl:value-of select="."/></para>
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
         <title>role summary</title>
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
                        <emphasis role="bold">Alias</emphasis>
                     </entry>
                     <entry spanname="hspan">
                        <xsl:apply-templates select="req:alternative_name"/>
                     </entry>
                  </row>
               </xsl:if>
               <xsl:if test="req:superior">
                  <row>
                     <entry>
                        <emphasis role="bold">Superior</emphasis>
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
                        <emphasis role="bold">Subordinates</emphasis>
                     </entry>
                     <entry spanname="hspan">
                        <xsl:apply-templates select="//req:superior[req:ref/@id = $sup_key]"
                                             mode="subordinate"/>
                     </entry>
                  </row>
               </xsl:if>
               <row>
                  <entry>
                     <emphasis role="bold">Acting use cases (directly)</emphasis>
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
                     <emphasis role="bold">Acting use cases (indirectly)</emphasis>
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
      <mediaobject  id="{concat('diagram-', ../req:key)}">
         <imageobject  role="fo">
             <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
          </imageobject>
          <imageobject  role="html">
             <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
          </imageobject>
          <caption>
             <para>
                Role diagram for role <xsl:value-of select="../req:key"/><xsl:text> </xsl:text><xsl:value-of select="req:name"/>.
             </para>
          </caption>
       </mediaobject>
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
      <xsl:for-each select="//uc:usecase[generate-id() = generate-id(key('scope-group', uc:scope))]">
         <xsl:variable name="scope_name" select="uc:scope"/>
         <section>
            <title><xsl:value-of select="uc:scope"/></title>
            <informaltable>
               <tgroup cols="2">
                  <tbody>
                     <xsl:for-each select="//uc:usecase[uc:scope = $scope_name]">
                       <row id="role_uc_list {uc:name}" label="{uc:name}">
                        <entry>
                           <xsl:value-of select="uc:name"/><xsl:text> : </xsl:text>
                        </entry>
                        <entry>
                           <xsl:variable name="role_name" select="normalize-space(uc:actors/uc:primary/uc:name)"/>
                           <xsl:text> [</xsl:text><xref linkend="{//req:requirement[normalize-space(req:role/req:name) = $role_name]/req:key}"/><xsl:text>] </xsl:text>
                           <xsl:call-template name="uc:list_acting_use_cases_indirect_upwards">
                              <xsl:with-param name="role_name" select="$role_name"/>
                           </xsl:call-template>
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
      <itemizedlist>
         <xsl:if test="req:open_issue">
            <listitem>
               <para><xsl:text>[</xsl:text><xref linkend="{req:key}"/><xsl:text>] : </xsl:text>
                  <itemizedlist>
                     <xsl:apply-templates select="req:open_issue"/>
                  </itemizedlist>
               </para>
            </listitem>
         </xsl:if>
      </itemizedlist>
   </xsl:template>

</xsl:stylesheet>
