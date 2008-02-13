<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:uc="uc"
                xmlns:req="req"
                xmlns:xi="http://www.w3.org/2001/XInclude"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="xsl xi db uc req xsi"
                xsi:schemaLocation="req
                                http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                                uc
                                http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd">
   <xsl:output encoding="iso-8859-1" />

   <xsl:namespace-alias stylesheet-prefix="db" result-prefix=""/>

   <xsl:param name="basedir" select="'.'"/>
   <!--  Our default language is english. -->
   <xsl:param name="lang" select="/uc:usecases/uc:info/@lang"/>

   <xsl:include href="libcommon.xsl"/>
   <xsl:include href="libxdoc.xsl"/>
   <xsl:include href="html2docbook.xsl"/>
   <xsl:include href="usecase_i18n.xsl"/>
   <xsl:include href="usecase-requirements.xsl"/>

   <xsl:key name="primary-actor-group" match="uc:primary" use="uc:name"/>
   <xsl:key name="secondary-actor-group" match="uc:secondary" use="uc:name"/>
   <xsl:key name="issue-group" match="uc:usecase" use="@id"/>

   <xsl:key name="entity-group" match="req:entity" use="req:name"/>

    <xsl:template match="uc:usecases">
       <xsl:variable name="book_state">
          <xsl:choose>
             <xsl:when test="uc:info/@state"><xsl:value-of select="uc:info/@state"/></xsl:when>
             <xsl:otherwise>draft</xsl:otherwise>
          </xsl:choose>
       </xsl:variable>
       <book lang="{uc:info/@lang}" status="{$book_state}">
          <xsl:apply-templates select="uc:info"/>

          <xsl:if test="//uc:usecase">
             <chapter id="usecases">
                <title><xsl:value-of select="$strUseCases"/></title>
                <xsl:if test="uc:usecase[@level='Summary' and not(@change_request)]">
                   <section>
                      <title><xsl:value-of select="$strSummaryLevel"/></title>
                      <xsl:for-each select="//uc:usecase[@level='Summary' and not(@change_request) and generate-id() = generate-id(key('scope-group', uc:scope))]">
                         <xsl:variable name="scope_id" select="uc:scope"/>
                         <section>
                            <title><xsl:value-of select="uc:scope"/></title>
                            <para>
                              <xsl:apply-templates select="//uc:usecase[@level='Summary' and not(@change_request) and uc:scope=$scope_id]"/>
                            </para>
                         </section>
                      </xsl:for-each>
                   </section>
                </xsl:if>
                <xsl:if test="uc:usecase[@level='UserGoal' and not(@change_request)]">
                <section>
                  <title><xsl:value-of select="$strUserGoalLevel"/></title>
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
                  <title><xsl:value-of select="$strComponentLevel"/></title>
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
                  <title><xsl:value-of select="$strChangeRequests"/></title>
                  <xsl:apply-templates select="uc:usecase[@change_request]" >
                     <xsl:sort select="@change_request"/>
                  </xsl:apply-templates>
                </section>
                </xsl:if>
             </chapter>
          </xsl:if>

          <!-- Requirements -->
          <xsl:call-template name="req:requirements"/>

          <xsl:if test="//uc:actors">
             <appendix id="Actors">
             <title><xsl:value-of select="$strAllActors"/></title>
             <section id="all_primary_actors">
               <title><xsl:value-of select="$strPrimaryActors"/></title>
               <informaltable>
                  <tgroup cols="2">
                    <tbody>
                       <xsl:call-template name="uc:list_primary_actors"/>
                       <row>
                          <entry></entry>
                          <entry></entry>
                       </row>
                    </tbody>
                  </tgroup>
               </informaltable>
             </section>
             <xsl:if test="//uc:secondary">
                <section id="all_secondary_actors">
                  <title><xsl:value-of select="$strSecondaryActors"/></title>
                  <informaltable>
                     <tgroup cols="2">
                       <tbody>
                          <xsl:call-template name="uc:list_secondary_actors"/>
                         <row>
                             <entry></entry>
                             <entry></entry>
                             <entry></entry>
                         </row>
                       </tbody>
                     </tgroup>
                  </informaltable>
                </section>
             </xsl:if>
           </appendix>
        </xsl:if>

        <xsl:if test="//req:role">
           <appendix id="Roles UC List">
             <title><xsl:value-of select="$strMappingUseCasesToRoles"/></title>
             <xsl:call-template name="uc:list_roles_usecases"/>
           </appendix>
        </xsl:if>

        <xsl:if test="//uc:usecase">
           <appendix id="Use Case Revisions">
             <title><xsl:value-of select="$strUseCaseRevision"/></title>
             <informaltable>
                <tgroup cols="3">
                   <colspec colwidth="2.5cm"/>
                   <colspec colwidth="3cm"/>
                   <tbody>
                      <xsl:apply-templates select="//uc:usecase" mode="revision_list"/>
                      <row>
                          <entry></entry>
                          <entry></entry>
                          <entry></entry>
                      </row>
                   </tbody>
                 </tgroup>
              </informaltable>
           </appendix>
        </xsl:if>

        <xsl:if test="//uc:open_issue or //req:open_issue">
           <appendix id="Open Issues">
             <title><xsl:value-of select="$strOpenIssues"/></title>
             <xsl:if test="//uc:open_issue">
                <section>
                   <title>Issues for Use Cases</title>
                   <xsl:apply-templates select="//uc:usecases" mode="issue_list"/>
                </section>
             </xsl:if>
             <xsl:if test="//req:open_issue">
                <section>
                   <title><xsl:value-of select="$strIssuesForRequirements"/></title>
                   <xsl:apply-templates select="//req:requirement" mode="issue_list"/>
                </section>
             </xsl:if>
           </appendix>
        </xsl:if>

        <xsl:if test="//uc:usecase">
           <appendix id="Priorities">
             <title><xsl:value-of select="$strPriorities"/></title>
             <informaltable>
                <tgroup cols="2">
                   <colspec colwidth="2.5cm"/>
                   <tbody>
                      <xsl:apply-templates select="//uc:usecase" mode="priority_list"/>
                      <row>
                          <entry></entry>
                          <entry></entry>
                      </row>
                    </tbody>
                </tgroup>
             </informaltable>
           </appendix>
        </xsl:if>

        <!-- if no element is available for the glossary, don't show glossary at all -->
        <xsl:if test="//req:entity or //req:role or //req:term">
           <glossary>
              <!-- if no term is available for the glossary -->
              <xsl:if test="//req:term">
                 <glossdiv>
                    <title><xsl:value-of select="$strTerms"/></title>
                    <xsl:apply-templates select="//req:term" mode="glossary">
                       <xsl:sort data-type="text" select="req:name" order="ascending" />
                    </xsl:apply-templates>
                 </glossdiv>
              </xsl:if>
              <!-- if no entity is available for the glossary -->
              <xsl:if test="//req:entity">
                 <glossdiv>
                    <title><xsl:value-of select="$strEntities"/></title>
                    <xsl:apply-templates select="//req:entity" mode="glossary">
                       <xsl:sort data-type="text" select="req:name" order="ascending" />
                    </xsl:apply-templates>
                 </glossdiv>
              </xsl:if>
              <!-- if no role is available for the glossary -->
              <xsl:if test="//req:role">
                 <glossdiv>
                    <title><xsl:value-of select="$strRoles"/></title>
                    <xsl:apply-templates select="//req:role" mode="glossary">
                       <xsl:sort data-type="text" select="req:name" order="ascending" />
                    </xsl:apply-templates>
                 </glossdiv>
              </xsl:if>
           </glossary>
        </xsl:if>
        <index></index>

       </book>
    </xsl:template>

    <xsl:template match="req:term" mode="glossary">
       <glossentry id="glossary_{../req:key}">
          <glossterm><xsl:value-of select="req:name"/></glossterm>
          <acronym><xsl:value-of select="req:acronym"/></acronym>
          <glossdef>
             <xsl:if test="req:acronym">
               <para>
                 <xsl:value-of select="$strAcronym"/><xsl:text>: </xsl:text>
                 <acronym><emphasis role="bold"><xsl:value-of select="req:acronym"/></emphasis></acronym>
               </para>
             </xsl:if>
             <para><xsl:value-of select="../req:description"/></para>
             <glossseealso otherterm="{../req:key}"><xsl:value-of select="../req:key"/></glossseealso>
          </glossdef>
       </glossentry>
    </xsl:template>

    <xsl:template match="req:entity" mode="glossary">
       <glossentry id="glossary_{../req:key}">
          <glossterm><xsl:value-of select="req:name"/></glossterm>
          <acronym><xsl:value-of select="req:name"/></acronym>
          <glossdef>
             <para><xsl:value-of select="../req:description"/></para>
             <glossseealso otherterm="{../req:key}"><xsl:value-of select="../req:key"/></glossseealso>
          </glossdef>
       </glossentry>
    </xsl:template>

    <xsl:template match="req:role" mode="glossary">
       <glossentry id="glossary_{../req:key}">
          <glossterm><xsl:value-of select="req:name"/></glossterm>
          <acronym><xsl:value-of select="req:name"/></acronym>
          <glossdef>
             <para><xsl:value-of select="../req:description"/></para>
             <glossseealso otherterm="{../req:key}"><xsl:value-of select="../req:key"/></glossseealso>
          </glossdef>
       </glossentry>
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
      <xsl:if test="count(//uc:usecase[generate-id() = generate-id(key('issue-group', @id)[1])]) > 0">
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
      </xsl:if>
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
         <xsl:if test="uc:priority">
            <xsl:apply-templates select="uc:priority"/>
         </xsl:if>
         <xsl:if test="uc:scope">
            <xsl:apply-templates select="uc:scope"/>
         </xsl:if>

         <section id="{uc:name}_overview">
          <title><xsl:value-of select="$strOverview"/></title>
          <para>
             <xsl:apply-templates select="uc:goal"/>
          </para>
          <xsl:if test="uc:description">
             <para>
                <xsl:apply-templates select="uc:description"/>
             </para>
          </xsl:if>
          <xsl:variable name="f" select="concat('images/', @id)"/>
          <figure pgwide="1">
             <title><xsl:value-of select="$strUseCaseDiagramForUseCase"/><xsl:value-of select="@id"/></title>
             <mediaobject  id="{concat('diagram-', uc:name)}">
                <imageobject  role="fo">
                   <imagedata  format="SVG"  fileref="{concat($f, '.svg')}"/>
                </imageobject>
                <imageobject  role="html">
                   <imagedata  format="PNG"  fileref="{concat($f, '.png')}"/>
                </imageobject>
             </mediaobject>
          </figure>
      </section>

      <xsl:apply-templates select="uc:trigger[not(normalize-space(.) = '')]"/>
      <xsl:apply-templates select="uc:response_time[not(normalize-space(.) = '')]"/>
      <xsl:apply-templates select="uc:frequency_of_use[not(normalize-space(.) = '')]"/>

         <para role="Body">
         </para>

         <section id="{uc:name}_actors">
            <title><xsl:value-of select="$strActors"/></title>
            <xsl:call-template name="uc:actors" />
         </section>

         <section>
            <title><xsl:value-of select="$strPreconditions"/></title>
            <xsl:if test="uc:precondition">
               <itemizedlist>
                 <xsl:apply-templates select="uc:precondition"/>
               </itemizedlist>
            </xsl:if>
            <xsl:call-template name="list_referents">
               <xsl:with-param name="usecase_id" select="@id"/>
            </xsl:call-template>
         </section>

         <xsl:if test="uc:stakeholder">
            <section>
               <title><xsl:value-of select="$strStakeholder"/></title>
                  <itemizedlist>
                    <xsl:apply-templates select="uc:stakeholder"/>
                  </itemizedlist>
            </section>
         </xsl:if>

         <section id="{@id}" xreflabel="{@id} {uc:name}">
            <title><xsl:value-of select="$strSuccess"/></title>
            <xsl:apply-templates select="uc:success" />
         </section>

         <xsl:if test="count(uc:extension) > 0">
            <section>
               <title><xsl:value-of select="$strExtensions"/></title>
               <xsl:for-each select="uc:extension">
                  <section id="{../@id}-{@id}" xreflabel="{../@id}-{@id} {@name}">
                     <title><xsl:value-of select="@id"/>: <xsl:value-of select="@name"/></title>
                     <xsl:apply-templates select="." />
                  </section>
               </xsl:for-each>
            </section>
         </xsl:if>

         <section id="{uc:name}_guarantees">
            <title><xsl:value-of select="$strGuarantees"/></title>
            <xsl:call-template name="uc:guarantees" />
         </section>

         <xsl:if test="uc:test-annotations">
            <section>
               <title><xsl:value-of select="$strTestAnnotations"/></title>
               <orderedlist numeration="arabic">
                  <xsl:apply-templates select="uc:test-annotations"/>
               </orderedlist>
            </section>
         </xsl:if>

         <xsl:if test="count(uc:open_issue) > 0">
            <section>
               <title><xsl:value-of select="$strOpenIssues"/></title>
               <orderedlist numeration="arabic">
               <xsl:apply-templates select="uc:open_issue"/>
               </orderedlist>
            </section>
         </xsl:if>

      </section>
   </xsl:template>

   <xsl:template match="uc:stakeholder|uc:precondition|uc:open_issue">
      <listitem><para><xsl:value-of select="."/></para></listitem>
   </xsl:template>

   <xsl:template match="uc:priority">
     <section>
         <title><xsl:value-of select="$strPriority"/></title>
         <para>
           <xsl:choose>
              <xsl:when test=". = 'High'">
                 <xsl:value-of select="$strPriorityHigh"/>
              </xsl:when>
              <xsl:when test=". = 'Medium'">
                 <xsl:value-of select="$strPriorityMedium"/>
              </xsl:when>
              <xsl:when test=". = 'Low'">
                 <xsl:value-of select="$strPriorityLow"/>
              </xsl:when>
           </xsl:choose>
         </para>
      </section>
   </xsl:template>

   <xsl:template match="uc:test-annotations">
      <listitem>
         <para>
            <xsl:apply-templates/>
         </para>
      </listitem>
   </xsl:template>

   <xsl:template match="uc:goal">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="uc:description">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="uc:scope">
     <section>
         <title><xsl:value-of select="$strScope"/></title>
         <para>
           <xsl:value-of select="."/>
         </para>
      </section>
   </xsl:template>

   <xsl:template match="uc:trigger">
      <section>
         <title><xsl:value-of select="$strTrigger"/></title>
         <para>
           <xsl:value-of select="."/>
         </para>
       </section>
   </xsl:template>

   <xsl:template match="uc:response_time">
      <section>
         <title><xsl:value-of select="$strResponseTimes"/></title>
         <para>
           <xsl:value-of select="."/>
         </para>
       </section>
   </xsl:template>

   <xsl:template match="uc:frequency_of_use">
      <section>
         <title><xsl:value-of select="$strFrequencyOfUse"/></title>
         <para>
           <xsl:value-of select="."/>
         </para>
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
     <xsl:variable name="actor_name" select="uc:name"/>
     <itemizedlist>
        <indexterm>
           <primary><xsl:value-of select="$strActor"/></primary>
           <secondary><xsl:value-of select="$actor_type"/></secondary>
           <tertiary><xsl:value-of select="uc:name"/></tertiary>
        </indexterm>
        <listitem>
           <para>
              <xsl:choose>
                  <xsl:when test="//req:role[req:name = $actor_name]"><xsl:value-of select="$actor_type"/>: <xsl:text> [</xsl:text><xref linkend="{//req:role[req:name = $actor_name]/../req:key}"/><xsl:text>]</xsl:text></xsl:when>
                  <xsl:otherwise><xsl:value-of select="$actor_type"/>: no role for actor <xsl:value-of select="$actor_name"/></xsl:otherwise>
              </xsl:choose>
              <xsl:if test="uc:channel">
                 <itemizedlist>
                    <xsl:for-each select="uc:channel">
                       <indexterm>
                          <primary><xsl:value-of select="$strChannel"/></primary>
                          <secondary><xsl:value-of select="."/></secondary>
                       </indexterm>
                       <listitem>
                          <para><xsl:value-of select="$strChannel"/>: <xsl:value-of select="."/></para>
                       </listitem>
                    </xsl:for-each>
                 </itemizedlist>
              </xsl:if>
            </para>
         </listitem>
      </itemizedlist>
   </xsl:template>

   <xsl:template name="uc:guarantees">
      <xsl:for-each select="uc:guarantees/uc:success">
         <itemizedlist>
            <listitem><para><xsl:value-of select="$strSuccess"/>: <xsl:value-of select="."/></para></listitem>
         </itemizedlist>
      </xsl:for-each>

       <xsl:for-each select="uc:guarantees/uc:minimal">
         <itemizedlist>
            <listitem><para><xsl:value-of select="$strMinimal"/>: <xsl:value-of select="."/></para></listitem>
         </itemizedlist>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="uc:success|uc:extension">
      <xsl:choose>
         <xsl:when test="uc:step">
            <orderedlist numeration="arabic">
            <xsl:apply-templates select="uc:step"/>
            </orderedlist>
         </xsl:when>
         <xsl:otherwise><para></para></xsl:otherwise>
      </xsl:choose>
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

               <subtitle><xsl:value-of select="$strSpecificationDocument"/></subtitle>

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
         <para><emphasis role="bold"><xsl:value-of select="$strReferencingUseCases"/>:</emphasis></para>
         <xsl:if test="//uc:usecase//uc:ref[(@id = $usecase_id or contains(@id,concat($usecase_id, '-'))) and not(ancestor-or-self::uc:usecase/@id = $usecase_id)]">
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
      </xsl:if>
   </xsl:template>

   <xsl:template match="releasenotes|releaseinfo">
      <releaseinfo>
         <xsl:choose>
            <xsl:when test="string-length($cclabel) &gt; 0 and not(starts-with($cclabel, '${label}'))">
               <xsl:value-of select="$cclabel"/><xsl:text> at </xsl:text><xsl:call-template name="datetime"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:call-template name="datetime"/>
               <xsl:text> by </xsl:text>
               <xsl:value-of select="$user"/>
            </xsl:otherwise>
         </xsl:choose>
      </releaseinfo>
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
