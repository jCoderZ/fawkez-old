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
    <para><xsl:apply-templates select="req:description"/></para>
    <para><xsl:apply-templates select="req:entity"/></para>
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

   <xsl:template match="req:entity">
      <itemizedlist>
         <title>referenced entities</title>
         <xsl:apply-templates select="req:attribute[req:objectreference]"/>
      </itemizedlist>
      
      <xsl:variable name="f" select="concat('images/', ../req:key)"/>
      <mediaobject  id="{concat('diagram-', ../req:key)}">
         <imageobject  role="fo">
             <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
          </imageobject>
          <imageobject  role="html">
             <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
          </imageobject>
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
