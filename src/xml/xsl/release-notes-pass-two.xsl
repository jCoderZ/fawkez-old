<?xml version="1.0"?>

<xsl:stylesheet
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xi="http://www.w3.org/2001/XInclude"
   xmlns:db="urn:docbook"
   xmlns:uc="uc"
   xmlns:req="req"
   xmlns:tc="http://jcoderz.org/test-specifications"
   xmlns:tr="http://jcoderz.org/test-results"
   xmlns:cms="http://jcoderz.org/generic-cms"
   exclude-result-prefixes="xsl db"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="req
                       http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                       uc
                       http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                       http://jcoderz.org/test-specifications
                       http://www.jcoderz.org/xsd/xdoc/test-specification-SNAPSHOT.xsd
                       http://jcoderz.org/test-results
                       http://www.jcoderz.org/xsd/xdoc/test-results-SNAPSHOT.xsd
                       http://jcoderz.org/generic-cms
                       http://www.jcoderz.org/xsd/xdoc/generic-cms-SNAPSHOT.xsd"
   version="1.0">
   
   <xsl:output encoding="iso-8859-1" method="xml"/>

   <!--xsl:include href="libcommon.xsl"/-->
   <xsl:include href="libxdoc.xsl" />
   <!--xsl:include href="html2docbook.xsl"/>
      <xsl:include href="usecase-requirements.xsl"/-->

   <xsl:param name="lang" select="default" />
   <xsl:param name="basedir" select="'.'" />
   <!-- version number of the application in qa reports scope.-->
   <xsl:param name="version" />
   
   <xsl:key name="component-group" 
            match="//cms:module[contains(../cms:version, $version)]" 
            use="."/>
   <!-- 
      *******
      Main ;)
      *******
   -->

   <xsl:template match="root">
      <book lang="en" status="final">
         <bookinfo>
            <title>Release Notes</title>
            <subtitle>Release <xsl:value-of select="$version"/></subtitle>
            <authorgroup>
               <author>
                  <firstname>Generated</firstname>
                  <surname>Automatically</surname>
               </author>
            </authorgroup>
            <releaseinfo>
               Version:
               <xsl:value-of select="$version" />
            </releaseinfo>
         </bookinfo>
         <chapter id="changed_modules_lists">
            <title>Modified Modules</title>
            <xsl:call-template name="component_list"/>
         </chapter>
         <chapter id="stats">
            <title>Statistics</title>
            <xsl:call-template name="statistics"/>
         </chapter>
         <chapter id="ext_issue_lists">
            <title>External Issues</title>
            <xsl:call-template name="ext_issue_lists"/>
         </chapter>
         <chapter id="bug_issue_lists">
            <title>Bug Issues</title>
            <xsl:call-template name="bug_issue_list"/>
         </chapter>
      </book>
   </xsl:template>

   <xsl:template name="ext_issue_lists">
      <xsl:call-template name="resolved_crs"/>
      <xsl:call-template name="resolved_bugs"/>
      <xsl:call-template name="untested_crs"/>
      <xsl:call-template name="untested_bugs"/>
      <xsl:call-template name="unresolved_crs"/>
      <xsl:call-template name="unresolved_bugs"/>
   </xsl:template>
      
   <xsl:template name="bug_issue_list">
      <xsl:call-template name="resolved_internals">
         <xsl:with-param name="category" select="'Bug'"/>
      </xsl:call-template>
      <xsl:call-template name="untested_internals">
         <xsl:with-param name="category" select="'Bug'"/>
      </xsl:call-template>
      <xsl:call-template name="unresolved_internals">
         <xsl:with-param name="category" select="'Bug'"/>
      </xsl:call-template>
   </xsl:template>
      
   <xsl:template name="component_list">
      <para>
      All components in this list have been changed. According to the list, you'll see what modules have
      been changed and have to be update within your system.
      </para>
      <para>
         <xsl:if test="//cms:module[generate-id() = generate-id(key('component-group', .))]">
            <itemizedlist>            
               <xsl:for-each select="//cms:module[generate-id() = generate-id(key('component-group', .))]">
                  <listitem>
                     <para>
                        <xsl:value-of select="."/>
                     </para>
                  </listitem>
               </xsl:for-each>
            </itemizedlist>
         </xsl:if>
      </para>
   </xsl:template>
   
   <xsl:template name="statistics">
      <section>
         <title>Statistics</title>
         <table frame="all">
            <title>Statistics</title>
            <tgroup cols="2" align="left" colsep="1" rowsep="1">
               <colspec colwidth="350" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <thead>
                  <row>
                     <entry>Category</entry>
                     <entry>Amount</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:variable name="number_issues" select="count(//cms:issue[cms:version = $version and (cms:type = 'Change Request' or cms:type = 'Bug')])"/>
                  <xsl:variable name="number_change_requests" select="count(//cms:issue[cms:version = $version and cms:type = 'Change Request' and cms:external-id])"/>
                  <xsl:variable name="number_bug_fixes" select="count(//cms:issue[cms:version = $version and cms:type = 'Bug' and cms:external-id])"/>
                  <xsl:variable name="number_change_requests_resolved" select="count(//cms:issue[cms:version = $version and cms:type = 'Change Request' and cms:state = 'Accepted' and cms:external-id])"/>
                  <xsl:variable name="number_bug_fixes_resolved" select="count(//cms:issue[cms:version = $version and cms:type = 'Bug' and cms:state = 'Accepted' and cms:external-id])"/>
                  <xsl:variable name="number_internals_resolved_bugs" select="count(//cms:issue[cms:version = $version and cms:type = 'Bug' and (cms:state = 'Accepted' or cms:state = 'Closed') and not(cms:external-id)])"/>
                  <row>
                     <entry><emphasis role="bold">Issues for <xsl:value-of select="$version"/></emphasis></entry>
                     <entry><xsl:value-of select="$number_issues"/></entry>
                  </row>
                  <row>
                     <entry><emphasis role="bold">Change Requests</emphasis></entry>
                     <entry><xsl:value-of select="$number_change_requests"/></entry>
                  </row>
                  <row>
                     <entry><emphasis role="bold">Bug Fixes</emphasis></entry>
                     <entry><xsl:value-of select="$number_bug_fixes"/></entry>
                  </row>
                  <row>
                     <entry><emphasis role="bold">Change Requests Resolved</emphasis></entry>
                     <entry><xsl:value-of select="$number_change_requests_resolved"/></entry>
                  </row>
                  <row>
                     <entry><emphasis role="bold">Bug Fixes Resolved</emphasis></entry>
                     <entry><xsl:value-of select="$number_bug_fixes_resolved"/></entry>
                  </row>
                  <row>
                     <entry><emphasis role="bold">Resolved Internal Bugs</emphasis></entry>
                     <entry><xsl:value-of select="$number_internals_resolved_bugs"/></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
   
   <xsl:template name="resolved_crs">
      <section>
         <title>Resolved Change Requests</title>
         <table frame="all">
            <title>Resolved Change Requests</title>
            <para>These change requests have been implemented, but are not accepted by quality assurance, yet.</para>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <colspec colwidth="90" colnum="1" colname="c3" align="right"/>
               <colspec colnum="1" colname="c4" align="right"/>
               <thead>
                  <row>
                     <entry>Issue-No.</entry>
                     <entry>Summary</entry>
                     <entry>Component</entry>
                     <entry>Prio</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates
                     select="//cms:issue[cms:version = $version and cms:type = 'Change Request' and cms:state = 'Accepted' and cms:external-id]"/>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
   
   <xsl:template name="resolved_bugs">
      <section>
         <title>Resolved Bug Fix Requests</title>
         <para>These bugs have been fixed and have been accepted by quality assurance.</para>
         <table frame="all">
            <title>Resolved Bug Fix Requests</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <colspec colwidth="90" colnum="1" colname="c3" align="right"/>
               <colspec colnum="1" colname="c4" align="right"/>
               <thead>
                  <row>
                     <entry>Issue-No.</entry>
                     <entry>Summary</entry>
                     <entry>Component</entry>
                     <entry>Prio</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates
                     select="//cms:issue[cms:version = $version and cms:type = 'Bug' and cms:state = 'Accepted' and cms:external-id]"/>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
   
   <xsl:template name="untested_crs">
      <section>
         <title>Resolved but Untested Change Requests</title>
         <para>
            The implementation of these issues is done already, but are not accepted by quality assurance, yet.
         </para>
         <table frame="all">
            <title>Resolved but Untested Change Requests</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <colspec colwidth="90" colnum="1" colname="c3" align="right"/>
               <colspec colnum="1" colname="c4" align="right"/>
               <thead>
                  <row>
                     <entry>Issue-No.</entry>
                     <entry>Summary</entry>
                     <entry>Component</entry>
                     <entry>Prio</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates
                     select="//cms:issue[cms:version = $version and cms:type = 'Change Request' and cms:state = 'Resolved' and cms:external-id]"/>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
   
   <xsl:template name="untested_bugs">
      <section>
         <title>Resolved but Untested Bug Fix Requests</title>
         <para>
            The implementation of these bug fixes is done already, but are not accepted by quality assurance, yet.
         </para>
         <table frame="all">
            <title>Resolved but Untested Bug Fix Requests</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <colspec colwidth="90" colnum="1" colname="c3" align="right"/>
               <colspec colnum="1" colname="c4" align="right"/>
               <thead>
                  <row>
                     <entry>Issue-No.</entry>
                     <entry>Summary</entry>
                     <entry>Component</entry>
                     <entry>Prio</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates
                     select="//cms:issue[cms:version = $version and cms:type = 'Bug' and cms:state = 'Resolved' and cms:external-id]"/>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
   
   <xsl:template name="unresolved_crs">
      <section>
         <title>Unresolved Change Requests</title>
         <para>
            These change requests are mapped to that release, but they are only implemented partially or not at all.
         </para>
         <table frame="all">
            <title>Unresolved Change Requests</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <colspec colwidth="90" colnum="1" colname="c3" align="right"/>
               <colspec colnum="1" colname="c4" align="right"/>
               <thead>
                  <row>
                     <entry>Issue-No.</entry>
                     <entry>Summary</entry>
                     <entry>Component</entry>
                     <entry>Prio</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates
                     select="//cms:issue[cms:version = $version and cms:type = 'Change Request' and not(cms:state = 'Resolved') and not(cms:state = 'Accepted') and cms:external-id]"/>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
   
   <xsl:template name="unresolved_bugs">
      <section>
         <title>Unresolved Bug Fix Requests</title>
         <para>
            These bug fixes are mapped to that release, but bug fixing has not been finished yet.
         </para>
         <table frame="all">
            <title>Unresolved Bug Fix Requests</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <colspec colwidth="90" colnum="1" colname="c3" align="right"/>
               <colspec colnum="1" colname="c4" align="right"/>
               <thead>
                  <row>
                     <entry>Issue-No.</entry>
                     <entry>Summary</entry>
                     <entry>Component</entry>
                     <entry>Prio</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates
                     select="//cms:issue[cms:version = $version and cms:type = 'Bug' and not(cms:state = 'Resolved') and not(cms:state = 'Accepted') and cms:external-id]"/>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
      
   <xsl:template name="resolved_internals">
      <xsl:param name="category"/>
      <section>
         <title>Resolved Internal Issues (<xsl:value-of select="$category"/>)</title>
         <para>Internal Issues of Category <xsl:value-of select="$category"/>, which have been fixed and accepted by Quality Assurance.</para>
         <table frame="all">
            <title>Resolved (<xsl:value-of select="$category"/>)</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <colspec colwidth="90" colnum="1" colname="c3" align="right"/>
               <colspec colnum="1" colname="c4" align="right"/>
               <thead>
                  <row>
                     <entry>Issue-No.</entry>
                     <entry>Summary</entry>
                     <entry>Component</entry>
                     <entry>Prio</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates
                     select="//cms:issue[cms:version = $version and cms:type = 'Bug' and (cms:state = 'Accepted' or cms:state = 'Closed') and not(cms:external-id)]"/>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
   
   <xsl:template name="untested_internals">
      <xsl:param name="category"/>
      <section>
         <title>Resolved but Untested Internal Issues (<xsl:value-of select="$category"/>)</title>
         <para>Internal Issues of Category <xsl:value-of select="$category"/>, which have been fixed, but not been accepted by Quality Assurance.</para>
         <table frame="all">
            <title>Untested (<xsl:value-of select="$category"/>)</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <colspec colwidth="90" colnum="1" colname="c3" align="right"/>
               <colspec colnum="1" colname="c4" align="right"/>
               <thead>
                  <row>
                     <entry>Issue-No.</entry>
                     <entry>Summary</entry>
                     <entry>Component</entry>
                     <entry>Prio</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates
                     select="//cms:issue[cms:version = $version and cms:type = 'Bug' and cms:state = 'Resolved' and not(cms:external-id)]"/>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
   
   <xsl:template name="unresolved_internals">
      <xsl:param name="category"/>
      <section>
         <title>Unresolved Internal Issues (<xsl:value-of select="$category"/>)</title>
         <para>Internal Issues of Category <xsl:value-of select="$category"/>, which have not been fixed, yet.</para>
         <table frame="all">
            <title>Unresolved (<xsl:value-of select="$category"/>)</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60" colnum="1" colname="c1"/>
               <colspec colnum="1" colname="c2" align="right"/>
               <colspec colwidth="90" colnum="1" colname="c3" align="right"/>
               <colspec colnum="1" colname="c4" align="right"/>
               <thead>
                  <row>
                     <entry>Issue-No.</entry>
                     <entry>Summary</entry>
                     <entry>Component</entry>
                     <entry>Prio</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates
                     select="//cms:issue[cms:version = $version and cms:type = 'Bug' and not(cms:state = 'Resolved' or cms:state = 'Accepted' or cms:state = 'Closed')  and not(cms:external-id)]"/>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template>
      
   <xsl:template match="cms:issue" mode="simple">
      <xsl:value-of select="cms:id" />
      ,
      <xsl:value-of select="cms:state" />
      <sbr />
   </xsl:template>
   
   <xsl:template match="cms:issue">
      <row>
         <xsl:choose>
            <xsl:when test="cms:external-id">
               <entry><xsl:for-each select="cms:external-id"><xsl:call-template name="link_to_cms">
                        <xsl:with-param name="issue_id" select="."/>
                     </xsl:call-template><xsl:if test="not(position() = last())">, </xsl:if></xsl:for-each></entry>
            </xsl:when>
            <xsl:otherwise>
               <entry>
                  <xsl:call-template name="link_to_cms">
                     <xsl:with-param name="issue_id" select="cms:id"/>
                  </xsl:call-template>
               </entry>
            </xsl:otherwise>
         </xsl:choose>
         <entry><xsl:value-of select="cms:summary" /></entry>
         <entry><xsl:value-of select="cms:module" /></entry>
         <entry><xsl:value-of select="cms:priority" /></entry>
      </row>
   </xsl:template>

</xsl:stylesheet>