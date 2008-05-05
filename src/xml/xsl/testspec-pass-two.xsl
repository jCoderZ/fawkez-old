<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:uc="uc"
                xmlns:req="req"
                xmlns:redirect="http://xml.apache.org/xalan/redirect"
                xmlns:tc="http://jcoderz.org/test-specifications"
                xmlns:xi="http://www.w3.org/2001/XInclude"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="xsl xi db uc req xsi"
                extension-element-prefixes="redirect"
                xsi:schemaLocation="req
                                http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                                uc
                                http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                                http://jcoderz.org/test-specifications
                                http://www.jcoderz.org/xsd/xdoc/test-specification-SNAPSHOT.xsd"  
                version="1.0">
   <xsl:output encoding="UTF-8"/>

   <!--xsl:include href="libcommon.xsl"/-->
   <xsl:include href="libxdoc.xsl"/>
   <!--xsl:include href="html2docbook.xsl"/>
   <xsl:include href="usecase-requirements.xsl"/-->

   <xsl:param name="lang" select="default"/>
   <xsl:param name="basedir" select="'.'"/>

   <xsl:key name="test-component-group" match="tc:test" use="tc:cut"/>
   <xsl:key name="test-issue-group"     match="tc:scrno" use="."/>
   <xsl:key name="component-group"      match="tc:cut" use="."/>
   <xsl:key name="test-state-group"     match="//tc:test" use="tc:state"/>
   
   <xsl:template match="/">
      <xsl:choose>
         <xsl:when test="count(//tc:test) &gt; 100">
            <xsl:apply-templates select="//tc:testspecs" mode="chunked"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:apply-templates select="//tc:testspecs"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
   <xsl:template match="tc:testspecs" mode="chunked">
      <book lang="en" status="final">
          <xsl:apply-templates select="//info"/>
          <chapter>
             <title>Statistics</title>
             <xsl:call-template name="statistics"/>
          </chapter>
          
          <chapter>
             <title>Referenced Test Specifications</title>
             <para>
                <table frame="all" tabstyle="striped"><title>Test Specification Documents</title>
                  <tgroup cols="2" align="left" colsep="1" rowsep="1">
                     <colspec colwidth="100" colnum="1" colname="c1"/>
                     <thead>
                        <row>
                          <entry>Module</entry>
                          <entry>Document</entry>
                        </row>
                     </thead>
                     <tbody>
                       <xsl:for-each select="//tc:cut[generate-id() = generate-id(key('component-group', .))]">
                         <row>
                           <entry><xsl:value-of select="."/></entry>
                           <entry>
                             <xsl:variable name="file_name">
                                <xsl:call-template name="component_to_file_name">
                                   <xsl:with-param name="component" select="."/>
                                </xsl:call-template>
                             </xsl:variable>
                             <xsl:variable name="pdf_name"
                                           select="concat(substring-before($file_name, '.p1.p2'), '.pdf')"/>
                             <ulink url="{$pdf_name}">
                                <citetitle><xsl:value-of select="$pdf_name"/></citetitle>
                             </ulink>
                           </entry>
                         </row>
                       </xsl:for-each>
                       <row>
                          <entry></entry>
                          <entry></entry>
                        </row>
                    </tbody>
                 </tgroup>
               </table>
             </para>
          </chapter>

          <xsl:if test="//tc:test">
             <chapter>
                <title>Issue Mapping</title>
                <xsl:call-template name="issue_list"/>
             </chapter>
          </xsl:if>
          
      </book>
      
      <xsl:for-each select="//tc:cut[generate-id() = generate-id(key('component-group', .))]">
         <xsl:variable name="file">
            <xsl:call-template name="component_to_file_name">
               <xsl:with-param name="component" select="."/>
            </xsl:call-template>
         </xsl:variable>
      
         <redirect:write file="{$file}">
            <book lang="en" status="final">
               <xsl:apply-templates select="//info"/>
               <chapter>
                  <title>Statistics</title>
                  <xsl:call-template name="statistics"/>
               </chapter>
             
               <chapter id="specifications">
                  <title><xsl:value-of select="."/></title>
                  <para></para>
                     <xsl:call-template name="single_chunk">
                        <xsl:with-param name="component" select="."/>
                     </xsl:call-template>
               </chapter>
            </book>
         </redirect:write>
      </xsl:for-each>
   </xsl:template>
   
   <xsl:template name="single_chunk">
      <xsl:param name="component"/>
      
       <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='System']">
          <section>
             <title>System Level / Functional</title>
             <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='System' and tc:priority='High']">
                <section>
                   <title>Priority: High</title>
                   <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='System' and tc:priority='High']" mode="main">
                      <xsl:sort select="tc:id"/>
                   </xsl:apply-templates>
                </section>
             </xsl:if>
             <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='System' and tc:priority='Medium']">
                <section>
                   <title>Priority: Medium</title>
                   <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='System' and tc:priority='Medium']" mode="main">
                      <xsl:sort select="tc:id"/>
                   </xsl:apply-templates>
                </section>
             </xsl:if>
             <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='System' and tc:priority='Low']">
                <section>
                   <title>Priority: Low</title>
                   <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='System' and tc:priority='Low']" mode="main">
                      <xsl:sort select="tc:id"/>
                   </xsl:apply-templates>
                </section>
             </xsl:if>
             <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='System' and not(tc:priority='Low') and not(tc:priority='Medium') and not(tc:priority='High')]">
                <section>
                    <title>Unpriorized</title>
                   <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='System' and not(tc:priority='Low') and not(tc:priority='Medium') and not(tc:priority='High')]" mode="main">
                      <xsl:sort select="tc:id"/>
                   </xsl:apply-templates>
                </section>
             </xsl:if>
          </section>
        </xsl:if>

          <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='System']">
             <section>
                <title>System Level / Non-Funtional</title>
                <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='System' and tc:priority='High']">
                  <section>
                     <title>Priority: High</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='System' and tc:priority='High']" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='System' and tc:priority='Medium']">
                  <section>
                     <title>Priority: Medium</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='System' and tc:priority='Medium']" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='System' and tc:priority='Low']">
                  <section>
                     <title>Priority: Low</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='System' and tc:priority='Low']" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='System' and not(tc:priority='Low') and not(tc:priority='Medium') and not(tc:priority='High')]">
                  <section>
                     <title>Unpriorized</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='System' and not(tc:priority='Low') and not(tc:priority='Medium') and not(tc:priority='High')]" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
             </section>
          </xsl:if>

          <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='Integration']">
            <section>
               <title>Integration Level / Functional</title>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='Integration' and tc:priority='High']">
                  <section>
                     <title>Priority: High</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='Integration' and tc:priority='High']" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='Integration' and tc:priority='Medium']">
                  <section>
                     <title>Priority: Medium</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='Integration' and tc:priority='Medium']" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='Integration' and tc:priority='Low']">
                  <section>
                     <title>Priority: Low</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='Integration' and tc:priority='Low']" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='Integration' and not(tc:priority='Low') and not(tc:priority='Medium') and not(tc:priority='High')]">
                  <section>
                     <title>Unpriorized</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Functional' and tc:level='Integration' and not(tc:priority='Low') and not(tc:priority='Medium') and not(tc:priority='High')]" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
            </section>
          </xsl:if>

          <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='Integration']">
             <section>
                <title>Integration Level / Non-Funtional</title>
                <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='Integration' and tc:priority='High']">
                  <section>
                     <title>Priority: High</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='Integration' and tc:priority='High']" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='Integration' and tc:priority='Medium']">
                  <section>
                     <title>Priority: Medium</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='Integration' and tc:priority='Medium']" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='Integration' and tc:priority='Low']">
                  <section>
                     <title>Priority: Low</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='Integration' and tc:priority='Low']" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
               <xsl:if test="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='Integration' and not(tc:priority='Low') and not(tc:priority='Medium') and not(tc:priority='High')]">
                  <section>
                     <title>Unpriorized</title>
                     <xsl:apply-templates select="key('test-component-group', $component)[tc:areatopic='Non Functional' and tc:level='Integration' and not(tc:priority='Low') and not(tc:priority='Medium') and not(tc:priority='High')]" mode="main">
                        <xsl:sort select="tc:id"/>
                     </xsl:apply-templates>
                  </section>
               </xsl:if>
             </section>
          </xsl:if>
   </xsl:template>

   <xsl:template match="tc:testspecs">
      <book lang="en" status="final">
          <xsl:apply-templates select="info"/>
          <chapter>
             <title>Statistics</title>
             <xsl:call-template name="statistics"/>
          </chapter>

          <xsl:if test="//tc:test">
             <chapter id="specifications">
                <title>Test Specifications</title>
                <para></para>
                <xsl:for-each select="//tc:test[generate-id() = generate-id(key('test-component-group', tc:cut)[1])]">
                   <section>
                      <title><xsl:value-of select="tc:cut"/></title>
                      <xsl:call-template name="single_chunk">
                         <xsl:with-param name="component" select="tc:cut"/>
                      </xsl:call-template>
                   </section>
                </xsl:for-each>
             </chapter>
          </xsl:if>

          <xsl:if test="//tc:test">
             <chapter>
                <title>Issue Mapping</title>
                <xsl:call-template name="issue_list"/>
             </chapter>
          </xsl:if>

        <index></index>

       </book>
    </xsl:template>

    <xsl:template name="statistics">
      <para>
         <table frame="all" tabstyle="striped">
           <title>Global Statistics</title>
           <tgroup cols="2" align="left" colsep="1" rowsep="1">
              <colspec colnum="1" colname="c1"/>
              <colspec colwidth="60" colnum="1" colname="c2"/>
              <thead>
                 <row>
                    <entry></entry>
                    <entry>(final / draft)</entry>
                 </row>
              </thead>
              <tbody>
                <row>
                   <entry>
                      Test Cases
                   </entry>
                   <entry>
                      <xsl:value-of select="count(key('test-state-group', 'final'))"/>
                      <xsl:text> / </xsl:text>
                      <xsl:value-of select="count(key('test-state-group', 'draft'))"/> 
                   </entry>
                </row>
                <row>
                   <entry>
                      Issues covered by Test Cases
                   </entry>
                   <entry>
                      <!-- TODO is this count correct? -->
                      <xsl:value-of select="count(key('test-state-group', 'final')/tc:scrno[not(.='') and not(.='none')])"/>
                      <xsl:text> / </xsl:text>
                      <xsl:value-of select="count(key('test-state-group', 'draft')/tc:scrno[not(.='') and not(.='none')])"/>
                   </entry>
                </row>
                <row>
                   <entry>
                      Numbers of test cases, prio High
                   </entry>
                   <entry>
                      <xsl:value-of select="count(key('test-state-group', 'final')[tc:priority='High'])"/>
                      <xsl:text> / </xsl:text>
                      <xsl:value-of select="count(key('test-state-group', 'draft')[tc:priority='High'])"/>
                   </entry>
                </row>
                <row>
                   <entry>
                      Numbers of test cases, prio Medium
                   </entry>
                   <entry>
                      <xsl:value-of select="count(key('test-state-group', 'final')[tc:priority='Medium'])"/>
                      <xsl:text> / </xsl:text>
                      <xsl:value-of select="count(key('test-state-group', 'draft')[tc:priority='Medium'])"/>
                   </entry>
                </row>
                <row>
                   <entry>
                      Numbers of test cases, prio Low
                   </entry>
                   <entry>
                      <xsl:value-of select="count(key('test-state-group', 'final')[tc:priority='Low'])"/>
                      <xsl:text> / </xsl:text>
                      <xsl:value-of select="count(key('test-state-group', 'draft')[tc:priority='Low'])"/>
                   </entry>
                </row>
              </tbody>
           </tgroup>
         </table>
      </para>

      <para>
        <table frame="all" tabstyle="striped">
           <title>Test Cases for Components Under Test</title>
           <tgroup cols="2" align="left" colsep="1" rowsep="1">
              <colspec colnum="1" colname="c1"/>
              <colspec colwidth="60" colnum="1" colname="c2"/>
              <thead>
                 <row>
                   <entry>Component Under Test</entry>
                   <entry>Number of Test Cases (final / draft)</entry>
                 </row>
              </thead>
              <tbody>
                 <xsl:for-each select="//tc:test[generate-id() = generate-id(key('test-component-group', tc:cut)[1])]">
                    <row>
                       <entry>
                          <xsl:value-of select="tc:cut"/>
                       </entry>
                       <entry>
                          <xsl:value-of select="count(key('test-component-group', tc:cut)[tc:state = 'final'])"/>
                          <xsl:text> / </xsl:text>
                          <xsl:value-of select="count(key('test-component-group', tc:cut)[tc:state = 'draft'])"/>
                       </entry>
                    </row>
                 </xsl:for-each>
              </tbody>
           </tgroup>
        </table>
     </para>
    </xsl:template>

    <xsl:template name="issue_list">
       <para>
       List of issues and their mapping to test cases.
       </para>
       <para>
         <xsl:choose>
           <xsl:when test="//tc:scrno[generate-id() = generate-id(key('test-issue-group', .))]">
            <table frame="all" tabstyle="striped"><title>Elements</title>
               <tgroup cols="2" align="left" colsep="1" rowsep="1">
                  <colspec colwidth="75" colnum="1" colname="c1"/>
                  <thead>
                     <row>
                       <entry>Issue No.</entry>
                       <entry>Test Case IDs</entry>
                     </row>
                  </thead>
                  <tfoot>
                     <row>
                       <entry>Issue No.</entry>
                       <entry>Test Case IDs</entry>
                     </row>
                  </tfoot>
                  <tbody>
                    <xsl:for-each select="//tc:scrno[generate-id() = generate-id(key('test-issue-group', .))]">
                      <xsl:if test="not(.='') and not(.='none')">
                         <row>
                           <entry><xsl:value-of select="."/></entry>
                           <entry>
                             <xsl:call-template name="issues_for_scrno">
                                <xsl:with-param name="issue_nr" select="."/>
                             </xsl:call-template>
                           </entry>
                         </row>
                       </xsl:if>
                    </xsl:for-each>
                    <row>
                       <entry></entry>
                       <entry></entry>
                     </row>
                 </tbody>
              </tgroup>
            </table>
           </xsl:when>
           <xsl:otherwise>
              No issues/tickets covered, yet
           </xsl:otherwise>
         </xsl:choose>
       </para>
    </xsl:template>
    
    <xsl:template name="issues_for_scrno">
       <xsl:param name="issue_nr"/>
       <itemizedlist>
         <xsl:apply-templates select="//tc:test/tc:scrno[.=$issue_nr]" mode="issue_list"/>
       </itemizedlist>
    </xsl:template>

    <xsl:template match="tc:scrno" mode="issue_list">
      <listitem>
        <para>
          <xsl:text>[</xsl:text><xref linkend="{../tc:id}"/><xsl:text>]</xsl:text>
        </para>
      </listitem>
    </xsl:template>

   <xsl:template match="tc:test" mode="main">
      <section id="{tc:id}" xreflabel="{tc:id} {tc:shortname}">
         <title>
            [<xsl:value-of select="tc:id"/>] <xsl:value-of select="tc:shortname"/>
         </title>

         <table frame="all" tabstyle="striped"><title>Elements</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colname="c1"/>
               <colspec colname="c2"/>
               <colspec colname="c3"/>
               <colspec colname="c4"/>
               <spanspec spanname="hspan" namest="c2" nameend="c4" align="center"/>
               <spanspec spanname="hspan2" namest="c1" nameend="c4" align="center"/>
               <!--thead>
                  <row>
                    <entry>Horizontal Span</entry>
                    <entry>a3</entry>
                    <entry>a4</entry>
                    <entry>a5</entry>
                  </row>
               </thead>
               <tfoot>
                  <row>
                    <entry>f1</entry>
                    <entry>f2</entry>
                    <entry>f3</entry>
                    <entry>f4</entry>
                    <entry>f5</entry>
                  </row>
               </tfoot-->
               <tbody>
                  <row>
                    <entry><emphasis role="bold">Test Type</emphasis></entry>
                    <entry><xsl:value-of select="tc:type"/></entry>
                    <entry><emphasis role="bold">Area Topic</emphasis></entry>
                    <entry><xsl:value-of select="tc:areatopic"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Priority</emphasis></entry>
                    <entry><xsl:value-of select="tc:priority"/></entry>
                    <entry><emphasis role="bold">Variety</emphasis></entry>
                    <entry><xsl:value-of select="tc:variety"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Document Base</emphasis></entry>
                    <entry spanname="hspan"><xsl:value-of select="tc:docbase"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Issue no.</emphasis></entry>
                    <entry>
                    <xsl:for-each select="tc:scrno">
                      <xsl:value-of select="."/><xsl:text> </xsl:text>
                    </xsl:for-each>
                    </entry>
                    <entry><emphasis role="bold">Depends on</emphasis></entry>
                    <entry><xsl:value-of select="tc:depends"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Execution</emphasis></entry>
                    <entry><xsl:value-of select="tc:execution"/></entry>
                    <entry><emphasis role="bold">Evaluation</emphasis></entry>
                    <entry><xsl:value-of select="tc:evaluation"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Method</emphasis></entry>
                    <entry><xsl:value-of select="tc:method"/></entry>
                    <entry><emphasis role="bold">Message</emphasis></entry>
                    <entry><xsl:value-of select="tc:message"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Component Under Test</emphasis></entry>
                    <entry><xsl:value-of select="tc:cut"/></entry>
                    <entry><emphasis role="bold">State</emphasis></entry>
                    <entry><xsl:value-of select="tc:state"/></entry>
                  </row>
                  
                  <row>
                    <entry><emphasis role="bold">Note</emphasis></entry>
                    <entry spanname="hspan"><xsl:apply-templates select="tc:note"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Revision</emphasis></entry>
                    <entry><xsl:value-of select="tc:revision"/></entry>
                    <entry><emphasis role="bold">Traceability</emphasis></entry>
                    <entry><xsl:value-of select="tc:traceability"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Description</emphasis></entry>
                    <entry spanname="hspan"><xsl:apply-templates select="tc:description"/></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>

         <xsl:apply-templates select="tc:steps"/>

         <para role="Body">
         </para>
      </section>
   </xsl:template>
   
   <xsl:template match="tc:test" mode="nf-tests">
      <section id="{tc:id}" xreflabel="{tc:id} {tc:shortname}">
         <title>
            [<xsl:value-of select="tc:id"/>] <xsl:value-of select="tc:shortname"/>
         </title>

         <table frame="all" tabstyle="striped"><title>Elements</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colname="c1"/>
               <colspec colname="c2"/>
               <colspec colname="c3"/>
               <colspec colname="c4"/>
               <spanspec spanname="hspan" namest="c2" nameend="c4" align="center"/>
               <spanspec spanname="hspan2" namest="c1" nameend="c4" align="center"/>
               <tbody>
                  <row>
                    <entry><emphasis role="bold">Test Type</emphasis></entry>
                    <entry><xsl:value-of select="tc:type"/></entry>
                    <entry><emphasis role="bold">Area Topic</emphasis></entry>
                    <entry><xsl:value-of select="tc:areatopic"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Priority</emphasis></entry>
                    <entry><xsl:value-of select="tc:priority"/></entry>
                    <entry><emphasis role="bold">Variety</emphasis></entry>
                    <entry><xsl:value-of select="tc:variety"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Document Base</emphasis></entry>
                    <entry spanname="hspan"><xsl:value-of select="tc:docbase"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Issue no.</emphasis></entry>
                    <entry>
                    <xsl:for-each select="tc:scrno">
                      <xsl:value-of select="."/><xsl:text> </xsl:text>
                    </xsl:for-each>
                    </entry>
                    <entry><emphasis role="bold">Depends on</emphasis></entry>
                    <entry><xsl:value-of select="tc:depends"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Execution</emphasis></entry>
                    <entry><xsl:value-of select="tc:execution"/></entry>
                    <entry><emphasis role="bold">Evaluation</emphasis></entry>
                    <entry><xsl:value-of select="tc:evaluation"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Method</emphasis></entry>
                    <entry><xsl:value-of select="tc:method"/></entry>
                    <entry><emphasis role="bold">Message</emphasis></entry>
                    <entry><xsl:value-of select="tc:message"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Component Under Test</emphasis></entry>
                    <entry><xsl:value-of select="tc:cut"/></entry>
                    <entry><emphasis role="bold">State</emphasis></entry>
                    <entry><xsl:value-of select="tc:state"/></entry>
                  </row>
                  
                  <row>
                    <entry><emphasis role="bold">Note</emphasis></entry>
                    <entry spanname="hspan"><xsl:apply-templates select="tc:note"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Revision</emphasis></entry>
                    <entry><xsl:value-of select="tc:revision"/></entry>
                    <entry><emphasis role="bold">Traceability</emphasis></entry>
                    <entry><xsl:value-of select="tc:traceability"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Description</emphasis></entry>
                    <entry spanname="hspan"><xsl:apply-templates select="tc:description"/></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>

         <xsl:apply-templates select="tc:steps" mode="nf-tests"/>

      </section>
   </xsl:template>

   <xsl:template match="tc:note">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="tc:action">
      <xsl:value-of select="."/><!--xsl:apply-templates/-->
   </xsl:template>

   <xsl:template match="tc:input">
      <xsl:value-of select="."/><!--xsl:apply-templates/-->
   </xsl:template>

   <xsl:template match="tc:expected">
      <xsl:value-of select="."/><!--xsl:apply-templates select="segmentedlist|table|itemizedlist"/-->
   </xsl:template>

   <xsl:template match="tc:description">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="tc:precondition">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="tc:postcondition">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="tc:steps">
     <table frame="all" tabstyle="striped"><title>Test Steps</title>
        <tgroup cols="4" align="left" colsep="1" rowsep="1">
           <colspec colname="c1" colwidth="1.0cm"/>
           <colspec colname="c2"/>
           <colspec colname="c3"/>
           <colspec colname="c4"/>
           <spanspec spanname="hspan" namest="c2" nameend="c4" align="center"/>
           <spanspec spanname="hspan2" namest="c1" nameend="c4" align="center"/>
           <tbody>
             <row>
               <entry spanname="hspan2"><emphasis role="bold">Precondition</emphasis></entry>
             </row>
             <row>
               <entry spanname="hspan2"><xsl:apply-templates select="../tc:precondition"/></entry>
             </row>
             <row>
               <entry><emphasis role="bold">ID</emphasis></entry>
               <entry><emphasis role="bold">Action</emphasis></entry>
               <entry><emphasis role="bold">Input Values</emphasis></entry>
               <entry><emphasis role="bold">Expected Results</emphasis></entry>
             </row>

             <xsl:apply-templates select="tc:step"/>

             <row>
               <entry spanname="hspan2"><emphasis role="bold">Postconditon</emphasis></entry>
             </row>
             <row>
               <entry spanname="hspan2"><xsl:apply-templates select="../tc:postcondition"/></entry>
             </row>
         </tbody>
       </tgroup>
     </table>
   </xsl:template>
   
   <xsl:template match="tc:steps" mode="nf-tests">
     <table frame="all" tabstyle="striped"><title>Test Groups</title>
        <tgroup cols="4" align="left" colsep="1" rowsep="1">
           <colspec colname="c1" colwidth="1.0cm"/>
           <colspec colname="c2"/>
           <colspec colname="c3"/>
           <colspec colname="c4"/>
           <spanspec spanname="hspan" namest="c2" nameend="c4" align="center"/>
           <spanspec spanname="hspan2" namest="c1" nameend="c4" align="center"/>
           <tbody>
             <row>
               <entry spanname="hspan2"><emphasis role="bold">Precondition</emphasis></entry>
             </row>
             <row>
               <entry spanname="hspan2"><xsl:apply-templates select="../tc:precondition"/></entry>
             </row>
             <row>
               <entry><emphasis role="bold">Group-ID</emphasis></entry>
               <entry><emphasis role="bold">Action</emphasis></entry>
               <entry><emphasis role="bold">Input Values</emphasis></entry>
               <entry><emphasis role="bold">Expected Results</emphasis></entry>
             </row>

             <xsl:apply-templates select="tc:step"/>

             <row>
               <entry spanname="hspan2"><emphasis role="bold">Postconditon</emphasis></entry>
             </row>
             <row>
               <entry spanname="hspan2"><xsl:apply-templates select="../tc:postcondition"/></entry>
             </row>
         </tbody>
       </tgroup>
     </table>
   </xsl:template>

   <xsl:template match="tc:step">
      <row>
        <entry><xsl:value-of select="tc:id"/></entry>
        <entry><xsl:apply-templates select="tc:action"/></entry>
        <entry><xsl:apply-templates select="tc:input"/></entry>
        <entry><xsl:apply-templates select="tc:expected"/></entry>
      </row>
   </xsl:template>
   
   <xsl:template name="component_to_file_name">
      <xsl:param name="component"/>
      <xsl:variable name="file_name_before" 
                    select="concat(concat('test-specification-',$component), '.p1.p2')"/>
      <!-- replace spaces and double points by underline -->
      <xsl:value-of select="translate(translate($file_name_before, ':', '_'), ' ', '_')"/>
   </xsl:template>

   <xsl:template match="info">

      <bookinfo>
         <xsl:call-template name="title-page">
            <xsl:with-param name="release" select="@version"/>
         </xsl:call-template>

         <title><xsl:value-of select="@project"/></title>

         <subtitle>Test Specification Document</subtitle>

         <xsl:apply-templates select="//bookinfo"/>
      </bookinfo>
      <xsl:apply-templates select="//chapter"/>
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
   <!-- END: generic copy -->

</xsl:stylesheet>