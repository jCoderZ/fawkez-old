<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:uc="uc"
                xmlns:req="req"
                xmlns:tc="http://jcoderz.org/test-specifications"
                xmlns:xi="http://www.w3.org/2001/XInclude"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="xsl xi db uc req xsi"
                xsi:schemaLocation="req
                                http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                                uc
                                http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                                http://jcoderz.org/test-specifications
                                http://www.jcoderz.org/xsd/xdoc/test-specifications-SNAPSHOT.xsd"  
                version="1.0">
   <xsl:output encoding="UTF-8"/>

   <!--xsl:include href="libcommon.xsl"/-->
   <xsl:include href="libxdoc.xsl"/>
   <!--xsl:include href="html2docbook.xsl"/>
   <xsl:include href="usecase-requirements.xsl"/-->

   <xsl:param name="lang" select="default"/>
   <xsl:param name="basedir" select="'.'"/>

   <xsl:key name="test-component-group" match="test" use="cut"/>
   <xsl:key name="test-issue-group" match="scrno" use="."/>

    <xsl:template match="testspecs">
       <book lang="en" status="final">
          <xsl:apply-templates select="info"/>

          <chapter>
             <title>Statistics</title>
             <xsl:call-template name="statistics"/>
          </chapter>

          <chapter id="specifications">
             <title>Test Specifications</title>
             <xsl:for-each select="//test[generate-id() = generate-id(key('test-component-group', cut)[1])]">
                <section>
                   <title><xsl:value-of select="cut"/></title>
                   <xsl:if test="key('test-component-group', cut)[areatopic='Functional' and level='System']">
                     <section>
                        <title>System Level / Functional</title>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Functional' and level='System' and priority='High']">
                           <section>
                              <title>Priority: High</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Functional' and level='System' and priority='High']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Functional' and level='System' and priority='Medium']">
                           <section>
                              <title>Priority: Medium</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Functional' and level='System' and priority='Medium']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Functional' and level='System' and priority='Low']">
                           <section>
                              <title>Priority: Low</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Functional' and level='System' and priority='Low']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Functional' and level='System' and not(priority='Low') and not(priority='Medium') and not(priority='High')]">
                           <section>
                              <title>Unpriorized</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Functional' and level='System' and not(priority='Low') and not(priority='Medium') and not(priority='High')]" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                     </section>
                   </xsl:if>

                   <xsl:if test="test[areatopic='Non Functional' and level='System']">
                      <section>
                         <title>System Level / Non-Funtional</title>
                         <xsl:if test="key('test-component-group', cut)[areatopic='Non Functional' and level='System' and priority='High']">
                           <section>
                              <title>Priority: High</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Non Functional' and level='System' and priority='High']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Non Functional' and level='System' and priority='Medium']">
                           <section>
                              <title>Priority: Medium</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Non Functional' and level='System' and priority='Medium']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Non Functional' and level='System' and priority='Low']">
                           <section>
                              <title>Priority: Low</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Non Functional' and level='System' and priority='Low']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Non Functional' and level='System' and not(priority='Low') and not(priority='Medium') and not(priority='High')]">
                           <section>
                              <title>Unpriorized</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Non Functional' and level='System' and not(priority='Low') and not(priority='Medium') and not(priority='High')]" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                      </section>
                   </xsl:if>

                   <xsl:if test="test[areatopic='Functional' and level='Integration']">
                     <section>
                        <title>Integration Level / Functional</title>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Functional' and level='Integration' and priority='High']">
                           <section>
                              <title>Priority: High</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Functional' and level='Integration' and priority='High']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Functional' and level='Integration' and priority='Medium']">
                           <section>
                              <title>Priority: Medium</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Functional' and level='Integration' and priority='Medium']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Functional' and level='Integration' and priority='Low']">
                           <section>
                              <title>Priority: Low</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Functional' and level='Integration' and priority='Low']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Functional' and level='Integration' and not(priority='Low') and not(priority='Medium') and not(priority='High')]">
                           <section>
                              <title>Unpriorized</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Functional' and level='Integration' and not(priority='Low') and not(priority='Medium') and not(priority='High')]" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                     </section>
                   </xsl:if>

                   <xsl:if test="test[areatopic='Non Functional' and level='Integration']">
                      <section>
                         <title>Integration Level / Non-Funtional</title>
                         <xsl:if test="key('test-component-group', cut)[areatopic='Non Functional' and level='Integration' and priority='High']">
                           <section>
                              <title>Priority: High</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Non Functional' and level='Integration' and priority='High']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Non Functional' and level='Integration' and priority='Medium']">
                           <section>
                              <title>Priority: Medium</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Non Functional' and level='Integration' and priority='Medium']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Non Functional' and level='Integration' and priority='Low']">
                           <section>
                              <title>Priority: Low</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Non Functional' and level='Integration' and priority='Low']" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                        <xsl:if test="key('test-component-group', cut)[areatopic='Non Functional' and level='Integration' and not(priority='Low') and not(priority='Medium') and not(priority='High')]">
                           <section>
                              <title>Unpriorized</title>
                              <xsl:apply-templates select="key('test-component-group', cut)[areatopic='Non Functional' and level='Integration' and not(priority='Low') and not(priority='Medium') and not(priority='High')]" mode="main">
                                 <xsl:sort select="id"/>
                              </xsl:apply-templates>
                           </section>
                        </xsl:if>
                      </section>
                   </xsl:if>
                </section>
             </xsl:for-each>

          </chapter>

          <chapter>
             <title>Issue Mapping</title>
             <xsl:call-template name="issue_list"/>
          </chapter>

        <index></index>

       </book>
    </xsl:template>

    <xsl:template name="statistics">
      <para>
         <table frame="all">
           <title>Global Statistics</title>
           <tgroup cols="2" align="left" colsep="1" rowsep="1">
              <colspec colnum="1" colname="c1"/>
              <colspec colwidth="60" colnum="1" colname="c2"/>
              <tbody>
                <row>
                   <entry>
                      Test Cases
                   </entry>
                   <entry>
                      <xsl:value-of select="count(//test)"/>
                   </entry>
                </row>
                <row>
                   <entry>
                      Issues covered by Test Cases
                   </entry>
                   <entry>
                      <xsl:value-of select="count(//test/scrno[not(.='') and not(.='none')])"/>
                   </entry>
                </row>
                <row>
                   <entry>
                      Numbers of test cases, prio High
                   </entry>
                   <entry>
                      <xsl:value-of select="count(//test[priority='High'])"/>
                   </entry>
                </row>
                <row>
                   <entry>
                      Numbers of test cases, prio Medium
                   </entry>
                   <entry>
                      <xsl:value-of select="count(//test[priority='Medium'])"/>
                   </entry>
                </row>
                <row>
                   <entry>
                      Numbers of test cases, prio Low
                   </entry>
                   <entry>
                      <xsl:value-of select="count(//test[priority='Low'])"/>
                   </entry>
                </row>
              </tbody>
           </tgroup>
         </table>
      </para>

      <para>
        <table frame="all">
           <title>Test Cases for Components Under Test</title>
           <tgroup cols="2" align="left" colsep="1" rowsep="1">
              <colspec colnum="1" colname="c1"/>
              <colspec colwidth="60" colnum="1" colname="c2"/>
              <thead>
                 <row>
                   <entry>Component Under Test</entry>
                   <entry>Number of Test Cases</entry>
                 </row>
              </thead>
              <tbody>
                 <xsl:for-each select="//test[generate-id() = generate-id(key('test-component-group', cut)[1])]">
                    <row>
                       <entry>
                          <xsl:value-of select="cut"/>
                       </entry>
                       <entry>
                          <xsl:value-of select="count(key('test-component-group', cut))"/>
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
           <xsl:when test="//scrno[generate-id() = generate-id(key('test-issue-group', .))]">
            <table frame="all"><title>Elements</title>
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
                    <xsl:for-each select="//scrno[generate-id() = generate-id(key('test-issue-group', .))]">
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
         <xsl:apply-templates select="//test/scrno[.=$issue_nr]" mode="issue_list"/>
       </itemizedlist>
    </xsl:template>

    <xsl:template match="scrno" mode="issue_list">
      <listitem>
        <para>
          <xsl:text>[</xsl:text><xref linkend="{../id}"/><xsl:text>]</xsl:text>
        </para>
      </listitem>
    </xsl:template>

   <xsl:template match="test" mode="main">
      <section id="{id}" xreflabel="{id} {shortname}">
         <title>
            [<xsl:value-of select="id"/>] <xsl:value-of select="shortname"/>
         </title>

         <table frame="all"><title>Elements</title>
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
                    <entry><xsl:value-of select="type"/></entry>
                    <entry><emphasis role="bold">Area Topic</emphasis></entry>
                    <entry><xsl:value-of select="areatopic"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Priority</emphasis></entry>
                    <entry><xsl:value-of select="priority"/></entry>
                    <entry><emphasis role="bold">Variety</emphasis></entry>
                    <entry><xsl:value-of select="variety"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Document Base</emphasis></entry>
                    <entry spanname="hspan"><xsl:value-of select="docbase"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Issue no.</emphasis></entry>
                    <entry>
                    <xsl:for-each select="scrno">
                      <xsl:value-of select="."/><xsl:text> </xsl:text>
                    </xsl:for-each>
                    </entry>
                    <entry><emphasis role="bold">Depends on</emphasis></entry>
                    <entry><xsl:value-of select="depends"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Execution</emphasis></entry>
                    <entry><xsl:value-of select="execution"/></entry>
                    <entry><emphasis role="bold">Evaluation</emphasis></entry>
                    <entry><xsl:value-of select="evaluation"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Method</emphasis></entry>
                    <entry><xsl:value-of select="method"/></entry>
                    <entry><emphasis role="bold">Message</emphasis></entry>
                    <entry><xsl:value-of select="message"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Component Under Test</emphasis></entry>
                    <entry><xsl:value-of select="cut"/></entry>
                    <entry><emphasis role="bold">Note</emphasis></entry>
                    <entry><xsl:apply-templates select="note"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Revision</emphasis></entry>
                    <entry><xsl:value-of select="revision"/></entry>
                    <entry><emphasis role="bold">Traceability</emphasis></entry>
                    <entry><xsl:value-of select="traceability"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Description</emphasis></entry>
                    <entry spanname="hspan"><xsl:apply-templates select="description"/></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>

         <xsl:apply-templates select="steps"/>

         <para role="Body">
         </para>
      </section>
   </xsl:template>
   
   <xsl:template match="test" mode="nf-tests">
      <section id="{id}" xreflabel="{id} {shortname}">
         <title>
            [<xsl:value-of select="id"/>] <xsl:value-of select="shortname"/>
         </title>

         <table frame="all"><title>Elements</title>
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
                    <entry><xsl:value-of select="type"/></entry>
                    <entry><emphasis role="bold">Area Topic</emphasis></entry>
                    <entry><xsl:value-of select="areatopic"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Priority</emphasis></entry>
                    <entry><xsl:value-of select="priority"/></entry>
                    <entry><emphasis role="bold">Variety</emphasis></entry>
                    <entry><xsl:value-of select="variety"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Document Base</emphasis></entry>
                    <entry spanname="hspan"><xsl:value-of select="docbase"/></entry>
                  </row>
                  <row>
                    <entry><emphasis role="bold">Issue no.</emphasis></entry>
                    <entry>
                    <xsl:for-each select="scrno">
                      <xsl:value-of select="."/><xsl:text> </xsl:text>
                    </xsl:for-each>
                    </entry>
                    <entry><emphasis role="bold">Depends on</emphasis></entry>
                    <entry><xsl:value-of select="depends"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Execution</emphasis></entry>
                    <entry><xsl:value-of select="execution"/></entry>
                    <entry><emphasis role="bold">Evaluation</emphasis></entry>
                    <entry><xsl:value-of select="evaluation"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Method</emphasis></entry>
                    <entry><xsl:value-of select="method"/></entry>
                    <entry><emphasis role="bold">Message</emphasis></entry>
                    <entry><xsl:value-of select="message"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Component Under Test</emphasis></entry>
                    <entry><xsl:value-of select="cut"/></entry>
                    <entry><emphasis role="bold">Note</emphasis></entry>
                    <entry><xsl:apply-templates select="note"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Revision</emphasis></entry>
                    <entry><xsl:value-of select="revision"/></entry>
                    <entry><emphasis role="bold">Traceability</emphasis></entry>
                    <entry><xsl:value-of select="traceability"/></entry>
                  </row>

                  <row>
                    <entry><emphasis role="bold">Description</emphasis></entry>
                    <entry spanname="hspan"><xsl:apply-templates select="description"/></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>

         <xsl:apply-templates select="steps" mode="nf-tests"/>

      </section>
   </xsl:template>

   <xsl:template match="note">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="action">
      <xsl:value-of select="."/><!--xsl:apply-templates/-->
   </xsl:template>

   <xsl:template match="input">
      <xsl:value-of select="."/><!--xsl:apply-templates/-->
   </xsl:template>

   <xsl:template match="expected">
      <xsl:value-of select="."/><!--xsl:apply-templates select="segmentedlist|table|itemizedlist"/-->
   </xsl:template>

   <xsl:template match="description">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="precondition">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="postcondition">
      <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="steps">
     <table frame="all"><title>Test Steps</title>
        <tgroup cols="4" align="left" colsep="1" rowsep="1">
           <colspec colname="c1"/>
           <colspec colname="c2"/>
           <colspec colname="c3"/>
           <colspec colname="c4"/>
           <spanspec spanname="hspan" namest="c2" nameend="c4" align="center"/>
           <spanspec spanname="hspan2" namest="c1" nameend="c4" align="center"/>
           <tbody>
             <row>
               <entry><emphasis role="bold">Precondition</emphasis></entry>
               <entry spanname="hspan"><xsl:apply-templates select="../precondition"/></entry>
             </row>
             <row>
               <entry><emphasis role="bold">Step-ID</emphasis></entry>
               <entry><emphasis role="bold">Action</emphasis></entry>
               <entry><emphasis role="bold">Input Values</emphasis></entry>
               <entry><emphasis role="bold">Expected Results</emphasis></entry>
             </row>

             <xsl:apply-templates select="step"/>

             <row>
               <entry><emphasis role="bold">Postconditon</emphasis></entry>
               <entry spanname="hspan"><xsl:apply-templates select="../postcondition"/></entry>
             </row>
         </tbody>
       </tgroup>
     </table>
   </xsl:template>
   
   <xsl:template match="steps" mode="nf-tests">
     <table frame="all"><title>Test Groups</title>
        <tgroup cols="4" align="left" colsep="1" rowsep="1">
           <colspec colname="c1"/>
           <colspec colname="c2"/>
           <colspec colname="c3"/>
           <colspec colname="c4"/>
           <spanspec spanname="hspan" namest="c2" nameend="c4" align="center"/>
           <spanspec spanname="hspan2" namest="c1" nameend="c4" align="center"/>
           <tbody>
             <row>
               <entry><emphasis role="bold">Precondition</emphasis></entry>
               <entry spanname="hspan"><xsl:apply-templates select="../precondition"/></entry>
             </row>
             <row>
               <entry><emphasis role="bold">Group-ID</emphasis></entry>
               <entry><emphasis role="bold">Action</emphasis></entry>
               <entry><emphasis role="bold">Input Values</emphasis></entry>
               <entry><emphasis role="bold">Expected Results</emphasis></entry>
             </row>

             <xsl:apply-templates select="step"/>

             <row>
               <entry><emphasis role="bold">Postconditon</emphasis></entry>
               <entry spanname="hspan"><xsl:apply-templates select="../postcondition"/></entry>
             </row>
         </tbody>
       </tgroup>
     </table>
   </xsl:template>

   <xsl:template match="step">
      <row>
        <entry><xsl:value-of select="id"/></entry>
        <entry><xsl:apply-templates select="action"/></entry>
        <entry><xsl:apply-templates select="input"/></entry>
        <entry><xsl:apply-templates select="expected"/></entry>
      </row>
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