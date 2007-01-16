<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:uc="uc"
                xmlns:req="req"
                xmlns:xi="http://www.w3.org/2003/XInclude"
                xmlns:db="http://docbook.org/ns/docbook"
                exclude-result-prefixes="xsl xi db"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="req
                                http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                                uc
                                http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd">
   <xsl:output encoding="iso-8859-1" />

   <xsl:namespace-alias stylesheet-prefix="db" result-prefix=""/>

   <xsl:include href="libcommon.xsl"/>
   <xsl:include href="libxdoc.xsl"/>
   <xsl:include href="html2docbook.xsl"/>
   <xsl:include href="usecase-requirements.xsl"/>

   <xsl:param name="lang" select="default"/>
   <xsl:param name="basedir" select="'.'"/>

   <xsl:key name="primary-actor-group" match="uc:primary" use="uc:name"/>
   <xsl:key name="secondary-actor-group" match="uc:secondary" use="uc:name"/>
   <xsl:key name="issue-group" match="uc:usecase" use="@id"/>

    <xsl:template match="uc:usecases">
       <book lang="{info/@lang}" status="final">
          <xsl:apply-templates select="uc:info"/>

          <chapter id="usecases">
             <title>Use Cases</title>
             <xsl:if test="uc:usecase[@level='Summary' and not(@change_request)]">
                <section>
                   <title>Summary Level</title>
                   <xsl:for-each select="//uc:usecase[@level='Summary' and not(@change_request) and generate-id() = generate-id(key('scope-group', uc:scope))]">
                      <xsl:variable name="scope_id" select="uc:scope"/>
                      <section>
                         <title><xsl:value-of select="uc:scope"/></title>
                         <xsl:apply-templates select="//uc:usecase[@level='Summary' and not(@change_request) and uc:scope=$scope_id]"/>
                      </section>
                   </xsl:for-each>
                </section>
             </xsl:if>
             <xsl:if test="uc:usecase[@level='UserGoal' and not(@change_request)]">
             <section>
               <title>User Goal Level</title>
               <xsl:for-each select="//uc:usecase[@level='UserGoal' and not(@change_request) and generate-id() = generate-id(key('scope-group', uc:scope))]">
                  <xsl:variable name="scope_id" select="uc:scope"/>
                  <section>
                     <title><xsl:value-of select="uc:scope"/></title>
                     <xsl:apply-templates select="//uc:usecase[@level='UserGoal' and not(@change_request) and uc:scope=$scope_id]"/>
                  </section>
               </xsl:for-each>
             </section>
             </xsl:if>
             <xsl:if test="uc:usecase[@level='Component' and not(@change_request)]">
             <section>
               <title>Component Level</title>
               <xsl:for-each select="//uc:usecase[@level='Component' and not(@change_request) and generate-id() = generate-id(key('scope-group', uc:scope))]">
                  <xsl:variable name="scope_id" select="uc:scope"/>
                  <section>
                     <title><xsl:value-of select="uc:scope"/></title>
                     <xsl:apply-templates select="//uc:usecase[@level='Component' and not(@change_request) and uc:scope=$scope_id]"/>
                  </section>
               </xsl:for-each>
             </section>
             </xsl:if>
             <xsl:if test="uc:usecase[@change_request]">
             <section>
               <title>Change Requests</title>
               <xsl:apply-templates select="uc:usecase[@change_request]" >
                  <xsl:sort select="@change_request"/>
               </xsl:apply-templates>
             </section>
             </xsl:if>
          </chapter>
          
          <!-- Requirements -->
          <xsl:call-template name="req:requirements"/>

          <appendix id="Actors">
          <title>All Actors</title>
          <section id="all_primary_actors">
            <title>Primary Actors</title>
            <informaltable>
               <tgroup cols="2">
                 <tbody>
                     <xsl:call-template name="uc:list_primary_actors"/>
                 </tbody>
               </tgroup>
            </informaltable>
          </section>
          <section id="all_secondary_actors">
            <title>Secondary Actors</title>
            <informaltable>
               <tgroup cols="2">
                 <tbody>
                         <xsl:call-template name="uc:list_secondary_actors"/>
                 </tbody>
               </tgroup>
            </informaltable>
          </section>
        </appendix>

        <appendix id="Roles UC List">
          <title>Mapping Use Cases to Roles</title>
          <xsl:call-template name="uc:list_roles_usecases"/>
        </appendix>

        <appendix id="Use Case Revisions">
          <title>Use Case Revision</title>
          <informaltable>
               <tgroup cols="3">
                 <colspec colwidth="2.5cm"/>
                      <colspec colwidth="3cm"/>
                 <tbody>
                         <xsl:apply-templates select="//uc:usecase" mode="revision_list"/>
                 </tbody>
               </tgroup>
            </informaltable>
        </appendix>

        <appendix id="Open Issues">
          <title>Open Issues</title>
          <section>
             <title>Issues for Use Cases</title>
             <xsl:apply-templates select="//uc:usecases" mode="issue_list"/>
          </section>
          <section>
             <title>Issues for Requirements</title>
             <xsl:apply-templates select="//req:requirement" mode="issue_list"/>
          </section>
        </appendix>

        <appendix id="Priorities">
          <title>Priorities</title>
          <informaltable>
             <tgroup cols="2">
                <colspec colwidth="2.5cm"/>
                <tbody>
                   <xsl:apply-templates select="//uc:usecase" mode="priority_list"/>
                 </tbody>
             </tgroup>
          </informaltable>
        </appendix>

        <index></index>

       </book>
    </xsl:template>

   <xsl:template match="uc:usecase" mode="revision_list">
      <row>
         <entry><xsl:value-of select="@id"/></entry>
         <entry>
            <xsl:choose>
               <xsl:when test="starts-with(uc:version, '$Revision:')">
                  <xsl:value-of select="normalize-space(substring-before(
                                        substring-after(uc:version, '$Revision:'),
                                        '$'))"/>
               </xsl:when>
               <xsl:otherwise>n/a</xsl:otherwise>
            </xsl:choose>
         </entry>
         <entry>
            <xsl:call-template name="uc:format_commitlog">
               <xsl:with-param name="log" select="uc:commitlog"/>
            </xsl:call-template>
         </entry>
      </row>
   </xsl:template>

   <xsl:template name="uc:format_commitlog">
      <xsl:param name="log"/>

      <!-- remove first log line [$..$] -->
      <xsl:variable name="logWithoutHeader"
         select="substring-after(substring-after($log, '$'), '$')"/>
      <xsl:call-template name="uc:format_commitlogentry">
         <xsl:with-param name="lines"
            select="normalize-space($logWithoutHeader)"/>
      </xsl:call-template>
   </xsl:template>

   <xsl:template name="uc:format_commitlogentry">
      <xsl:param name="lines"/>
      <xsl:if test="starts-with($lines, 'Revision ')">
         <xsl:variable name="x" select="substring-after($lines, 'Revision ')"/>
            <xsl:choose>
               <xsl:when test="contains($x, 'Revision ')">
                  <para>
                     <xsl:value-of select="substring-before($x, 'Revision ')"/>
                  </para>
                  <xsl:call-template name="uc:format_commitlogentry">
                     <xsl:with-param name="lines"
                        select="concat('Revision ',
                        substring-after($x, 'Revision '))"/>
                  </xsl:call-template>
               </xsl:when>
          <xsl:otherwise>
                  <para>
                     <xsl:value-of select="$x"/>
                  </para>
          </xsl:otherwise>
            </xsl:choose>
      </xsl:if>
   </xsl:template>

   <xsl:template match="uc:usecase" mode="priority_list">
      <row>
         <entry><xsl:value-of select="@id"/></entry>
         <entry><xsl:value-of select="uc:priority"/></entry>
      </row>
   </xsl:template>


    <xsl:template match="uc:usecases" mode="issue_list">
      <itemizedlist>
         <xsl:for-each select="//uc:usecase[generate-id() = generate-id(key('issue-group', @id)[1])]">
            <xsl:if test="uc:open_issue">
               <listitem>
                  <para><xsl:text>[</xsl:text><xref linkend="{@id}"/><xsl:text>] : </xsl:text>
                     <itemizedlist>
                        <xsl:for-each select="key('issue-group', @id)">
                           <xsl:for-each select="uc:open_issue">
                              <listitem><para><xsl:value-of select="."/></para></listitem>
                           </xsl:for-each>
                        </xsl:for-each>
                     </itemizedlist>
                  </para>
               </listitem>
            </xsl:if>
         </xsl:for-each>
      </itemizedlist>
   </xsl:template>

   <xsl:template name="uc:list_primary_actors">
      <xsl:for-each select="//uc:primary[generate-id() = generate-id(key('primary-actor-group', uc:name)[1])]">
        <row id="primary_actor_list {uc:name}" label="{uc:name} as primary actor">
         <entry>
            <xsl:value-of select="uc:name"/><xsl:text> : </xsl:text>
         </entry>
           <entrytbl cols="1">
            <tbody>
               <xsl:for-each select="key('primary-actor-group', uc:name)">
                  <row>
                     <entry>
                        <xsl:text>[</xsl:text><xref linkend="{../../@id}"/><xsl:text>]</xsl:text>
                     </entry>
                  </row>
               </xsl:for-each>
            </tbody>
         </entrytbl>
       </row>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="uc:list_secondary_actors">
      <xsl:for-each select="//uc:secondary[generate-id() = generate-id(key('secondary-actor-group', uc:name)[1])]">
         <row id="secondary_actor_list {uc:name}" label="{uc:name} as secondary/supporting actor">
          <entry>
            <xsl:value-of select="uc:name"/><xsl:text> : </xsl:text>
         </entry>
         <entrytbl cols="1">
            <tbody>
               <xsl:for-each select="key('secondary-actor-group', uc:name)">
                 <row>
                     <entry>
                       <xsl:text>[</xsl:text><xref linkend="{../../@id}"/><xsl:text>]</xsl:text>
                      </entry>
                  </row>
               </xsl:for-each>
            </tbody>
         </entrytbl>
       </row>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="uc:usecase">
      <section id="{uc:name}">
         <title>
            [<xsl:value-of select="@id"/>] <xsl:value-of select="uc:name"/> <xsl:text> </xsl:text><xsl:value-of select="@change_request"/>
         </title>
         <xsl:apply-templates select="uc:scope"/>

         <section id="{uc:name}_overview">
          <title>
            Overview
          </title>
          <para><xsl:value-of select="uc:goal"/></para>
          <para>

             <xsl:variable name="f" select="concat('images/', @id)"/>
             <mediaobject  id="{concat('diagram-', uc:name)}">
                <imageobject  role="fo">
                   <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
                </imageobject>
                <imageobject  role="html">
                   <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
                </imageobject>
             </mediaobject>
             </para>
      </section>

      <xsl:apply-templates select="uc:trigger"/>

         <para role="Body">
         </para>

         <section id="{uc:name}_actors">
            <title>Actors</title>
            <xsl:call-template name="uc:actors" />
         </section>

         <section>
            <title>Preconditions</title>
            <itemizedlist>
              <xsl:apply-templates select="uc:precondition"/>
            </itemizedlist>
            <xsl:call-template name="list_referents">
               <xsl:with-param name="usecase_id" select="@id"/>
            </xsl:call-template>
         </section>

         <section>
            <title>Stakeholder</title>
            <itemizedlist>
              <xsl:apply-templates select="uc:stakeholder"/>
            </itemizedlist>
         </section>

         <section id="{@id}" xreflabel="{@id} {uc:name}">
            <title>Success</title>
            <xsl:apply-templates select="uc:success" />
         </section>

         <section>
            <title>Extensions</title>
            <xsl:for-each select="uc:extension">
               <section id="{../@id}-{@id}" xreflabel="{../@id}-{@id} {@name}">
                  <title><xsl:value-of select="@id"/>: <xsl:value-of select="@name"/></title>
                  <xsl:apply-templates select="." />
               </section>
            </xsl:for-each>
         </section>

         <section id="{uc:name}_guarantees">
            <title>Guarantees</title>
            <xsl:call-template name="uc:guarantees" />
         </section>

         <section>
            <title>Open Issues</title>
            <itemizedlist numeration="arabic">
            <xsl:apply-templates select="uc:open_issue"/>
            </itemizedlist>
         </section>
      </section>
   </xsl:template>

   <xsl:template match="uc:stakeholder|uc:precondition|uc:open_issue">
      <listitem><para><xsl:value-of select="."/></para></listitem>
   </xsl:template>

   <xsl:template match="uc:scope">
     <section>
         <title>Scope</title>
         <xsl:value-of select="."/>
      </section>
   </xsl:template>

   <xsl:template match="uc:trigger">
      <section>
          <title>Trigger</title>
          <xsl:value-of select="."/>
       </section>
   </xsl:template>

   <xsl:template name="uc:actors">
      <xsl:for-each select="uc:actors/uc:primary">
       <xsl:call-template name="uc:actor_list"/>
      </xsl:for-each>

       <xsl:for-each select="uc:actors/uc:secondary">
          <xsl:call-template name="uc:actor_list">
          <xsl:with-param name="actor_type" select="'Secondary'"/>
          </xsl:call-template>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="uc:actor_list">
     <xsl:param name="actor_type" select="'Primary'"/>
         <itemizedlist>
         <indexterm>
            <primary>Actor</primary>
            <secondary><xsl:value-of select="$actor_type"/></secondary>
            <tertiary><xsl:value-of select="uc:name"/></tertiary>
          </indexterm>
            <listitem><para><xsl:value-of select="$actor_type"/>: <xsl:value-of select="uc:name"/>
                <itemizedlist>
                   <xsl:for-each select="uc:channel">
                 <indexterm>
                      <primary>Channel</primary>
                      <secondary><xsl:value-of select="."/></secondary>
                    </indexterm>
                      <listitem><para>Channel: <xsl:value-of select="."/></para></listitem>
                   </xsl:for-each>
               </itemizedlist></para></listitem>
         </itemizedlist>
   </xsl:template>

   <xsl:template name="uc:guarantees">
      <xsl:for-each select="uc:guarantees/uc:success">
         <itemizedlist>
            <listitem><para>Success: <xsl:value-of select="."/></para></listitem>
         </itemizedlist>
      </xsl:for-each>

       <xsl:for-each select="uc:guarantees/uc:minimal">
         <itemizedlist>
            <listitem><para>Minimal: <xsl:value-of select="."/></para></listitem>
         </itemizedlist>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="uc:success|uc:extension">
         <itemizedlist numeration="arabic">
         <xsl:apply-templates select="uc:step"/>
         </itemizedlist>
   </xsl:template>

   <xsl:template match="uc:step">
           <xsl:variable name="uc_id">
            <xsl:choose>
               <xsl:when test="name(..) = 'uc:success'">
                   <xsl:value-of select="../../@id"/>-<xsl:value-of select="@id"/>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:value-of select="../../@id"/>-<xsl:value-of
                  select="../@id"/>-<xsl:value-of select="@id"/>
               </xsl:otherwise>
            </xsl:choose>
           </xsl:variable>
            <listitem id="{$uc_id}" xreflabel="{$uc_id} {../@name} {../../uc:name}">
               <para>
                  <xsl:value-of select="$uc_id"/>: <xsl:apply-templates/>
               </para>
            </listitem>
   </xsl:template>

   <xsl:template match="uc:ref">
     <xsl:text> [</xsl:text><xref linkend="{@id}"/><xsl:text>] </xsl:text>
   </xsl:template>

   <xsl:template match="uc:info">
      <xsl:choose>
         <xsl:when test="@template = 'true'">
            <bookinfo>
               <xsl:call-template name="title-page">
                  <xsl:with-param name="release" select="@version"/>
               </xsl:call-template>
      
               <title><xsl:value-of select="@project"/></title>
      
               <subtitle>Specification Document</subtitle>
      
               <!--xsl:apply-templates select="/uc:usecases/bookinfo/*"/-->
               <!-- TODO: revision history from cvs log src/doc/sad/sad.xml -->
            </bookinfo>
         </xsl:when>
         <xsl:otherwise>
            <xsl:apply-templates select="/uc:usecases/bookinfo"/>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="/uc:usecases/chapter"/>
   </xsl:template>
   
   <xsl:template name="list_referents">
      <xsl:param name="usecase_id"/>
      <xsl:if test="//uc:usecase//uc:ref[(@id = $usecase_id or contains(@id,concat($usecase_id, '-'))) and not(ancestor-or-self::uc:usecase/@id = $usecase_id)]">
         <para><emphasis role="bold">referencing use cases</emphasis></para>
         <itemizedlist>
            <xsl:for-each select="//uc:usecase//uc:ref[(@id = $usecase_id or contains(@id,concat($usecase_id, '-'))) and not(ancestor-or-self::uc:usecase/@id = $usecase_id)]">
               <listitem>
                  <para>
                     <xsl:variable name="source">
                        <xsl:if test="ancestor-or-self::uc:usecase"><xsl:value-of select="ancestor-or-self::uc:usecase/@id"/></xsl:if>
                        <xsl:if test="ancestor-or-self::uc:extension"><xsl:text>-</xsl:text><xsl:value-of select="ancestor-or-self::uc:extension/@id"/></xsl:if>
                        <xsl:if test="ancestor-or-self::uc:step"><xsl:text>-</xsl:text><xsl:value-of select="ancestor-or-self::uc:step/@id"/></xsl:if>
                     </xsl:variable>
                     <xsl:text> [</xsl:text><xref linkend="{$source}"/><xsl:text>] </xsl:text>
                  </para>
               </listitem>               
            </xsl:for-each>
         </itemizedlist>
      </xsl:if>
   </xsl:template>

      <!-- BEGIN: generic copy -->
   <xsl:template match="*">
      <xsl:copy>
         <xsl:apply-templates select="@*"/>
         <xsl:apply-templates
            select="*|comment()|text()|processing-instruction()"/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="@*">
      <xsl:copy/>
   </xsl:template>

   <xsl:template match="text()">
      <xsl:copy/>
   </xsl:template>

   <!--xsl:template match="node()">
      <xsl:element name="{local-name(.)}">
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates/>
      </xsl:element>
   </xsl:template-->
   <!-- END: generic copy -->
</xsl:stylesheet>