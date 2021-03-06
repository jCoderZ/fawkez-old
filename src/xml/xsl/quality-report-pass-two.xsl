<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:xi="http://www.w3.org/2001/XInclude"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:db="urn:docbook"
                xmlns:uc="uc"
                xmlns:req="req"
                xmlns:tc="http://jcoderz.org/test-specifications"
                xmlns:tr="http://jcoderz.org/test-results"
                xmlns:cms="http://jcoderz.org/generic-cms"
                exclude-result-prefixes="xsl xi db"
                xsi:schemaLocation="req
                                    http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                                    uc
                                    http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                                    http://jcoderz.org/test-specifications
                                    http://www.jcoderz.org/xsd/xdoc/test-specification-SNAPSHOT.xsd
                                    http://jcoderz.org/test-results
                                    http://www.jcoderz.org/xsd/xdoc/test-results-SNAPSHOT.xsd
                                    http://jcoderz.org/generic-cms
                                    http://www.jcoderz.org/xsd/xdoc/generic-cms-SNAPSHOT.xsd">
   <xsl:output encoding="UTF-8" method="xml" indent="yes"/>

   <!--xsl:include href="libcommon.xsl"/-->
   <xsl:include href="libxdoc.xsl"/>
   <xsl:include href="libconstants.xsl"/>

   <xsl:param name="lang" select="default"/>
   <xsl:param name="basedir" select="'.'"/>
   <!-- version number of the application in qa reports scope.-->
   <xsl:param name="version"/>
   <xsl:param name="version.releasecandidate"/>
   <!-- wether it is an "internal" or "external" report. -->
   <xsl:param name="type" select="'internal'"/>
   
   <xsl:key name="usecases-group"          match="uc:usecases" use="."/>
   
   <xsl:key name="test-group"                         match="//tc:test" use="tc:traceability"/>
   <xsl:key name="test-group-final"                   match="//tc:test[tc:state = 'final' or tc:state = '' or not(tc:state)]" use="tc:traceability"/>
   <xsl:key name="test-group-draft"                   match="//tc:test[tc:state = 'draft']" use="tc:traceability"/>
   <xsl:key name="test-shortname-group"               match="//tc:test" use="tc:shortname"/>
   <xsl:key name="usecase-group"                      match="//uc:usecase" use="@id"/>
   <xsl:key name="usecase-scope-group"                match="//uc:usecase" use="uc:scope"/>
   <xsl:key name="usecase-issue-group"                match="//uc:usecase" use="../uc:info/@issue"/>
   <xsl:key name="scope-group"                        match="//uc:scope" use="."/>
   <xsl:key name="scope-issue-group"                  match="//uc:scope" use="../../uc:info/@issue"/>
   <xsl:key name="testresult-group"                   match="//tr:testresult[starts-with(tr:version,$version)]" use="."/>
   <xsl:key name="testresult-testcase-group"          match="//tr:testresult[starts-with(tr:version,$version)]" use="tr:testcase"/>
   <xsl:key name="testresult-shortname-group"         match="//tr:testresult[starts-with(tr:version,$version)]" use="tr:shortname"/>
   
   <xsl:key name="testresult-testcase-final-group"    match="//tr:testresult[starts-with(tr:version,$version)][tr:testcase = //tc:id[../tc:state = 'final']]" use="tr:testcase"/>
   <xsl:key name="testresult-shortname-final-group"   match="//tr:testresult[starts-with(tr:version,$version)][tr:shortname = //tc:shortname[../tc:state = 'final']]" use="tr:shortname"/>
   <xsl:key name="testresult-testcase-draft-group"    match="//tr:testresult[starts-with(tr:version,$version)][tr:testcase = //tc:id[../tc:state = 'draft']]" use="tr:testcase"/>
   <xsl:key name="testresult-shortname-draft-group"   match="//tr:testresult[starts-with(tr:version,$version)][tr:shortname = //tc:shortname[../tc:state = 'draft']]" use="tr:shortname"/>
   
   <xsl:key name="testresult-passed-testcase-group"   match="//tr:testresult[starts-with(tr:version,$version) and tr:result = 'passed']" use="normalize-space(tr:testcase)"/>
   <xsl:key name="testresult-passed-shortname-group"  match="//tr:testresult[starts-with(tr:version,$version) and tr:result = 'passed']" use="normalize-space(tr:shortname)"/>
   
   <xsl:key name="issue-group"                        match="//tr:issue[starts-with(../tr:version,$version)]" use="."/>
   <xsl:key name="scarab-id"                          match="//cms:issues//cms:issue"
                                                      use="cms:id"/>

   <xsl:key name="mappings-group"                     match="//mappings/mapping" use="shortname"/>
   
   <!-- 
       *******
       Main ;)
       *******
    -->

   <xsl:template match="root">
      <book lang="en" status="final">
         <bookinfo>
            <title>QA Report</title>

            <subtitle>Report Document</subtitle>
            <authorgroup>
               <author>
                  <firstname>Automatically Generated</firstname>
                  <surname></surname>
               </author>
               <author>
                  <firstname>Dino</firstname>
                  <surname>Coppola</surname>
               </author>
            </authorgroup>
            <releaseinfo>
               <xsl:text>
               </xsl:text>Version: <xsl:value-of select="$version"/>,<xsl:text>
               </xsl:text>Release Candidate: <xsl:value-of select="$version.releasecandidate"/>
            </releaseinfo>
         </bookinfo>
         <xsl:call-template name="overview"/>
         <xsl:variable name="cols_by_type">
            <xsl:if test="$type = 'internal'">6</xsl:if>
            <xsl:if test="$type = 'external'">5</xsl:if>
         </xsl:variable>
         
         <chapter id="results">
            <title>Test Results</title>
                        
            <xsl:if test="$type = 'internal'">
               <section>
                 <title>Complete Bug Overview</title>
                 <para>
                    Diagram shows an overview of all external and internal bugs,
                    which are unresolved and/or not closed, yet.
                    <itemizedlist>
                       <listitem>
                          <para>
                             <emphasis role="bold">Open External Issues(Unclosed): </emphasis> are all external
                             issues/tickets, which does not have been closed by customer in external change
                             management system, yet. These issues may have to be still verified or have been
                             resolved and delivered already.
                          </para>
                       </listitem>
                       <listitem>
                          <para>
                             <emphasis role="bold">Open External Bugs(Unresolved): </emphasis> are all external
                             issues/tickets, which does not have resolved by development team, yet. These issues
                             may have to be still verified or have, but they have not been resolved internally.
                             The gap between <emphasis role="italic">Open External Bugs(Unresolved)</emphasis>
                             and <emphasis role="italic">Open External Bugs(Unclosed)</emphasis> are the number
                             of already resolved external tickets, which have to be released to or accepted by
                             the customer.
                          </para>
                       </listitem>
                       <listitem>
                          <para>
                             <emphasis role="bold">Open Internal Bugs: </emphasis> are all internally found
                             issues, which does not have been resolved by development team, yet. These issues
                             may have to be still verified or have, but they have not been resolved internally.
                          </para>
                       </listitem>
                    </itemizedlist>
                    <mediaobject>
                       <imageobject role="fo">
                          <imagedata fileref="../../generated-sdocbook/images/svg/issues_allbugs.svg" format="SVG" />
                       </imageobject>
                       <imageobject role="html">
                          <imagedata fileref="images/jpg/issues_allbugs.jpg" format="JPG" />
                       </imageobject>
                    </mediaobject>
                 </para>
              </section>
           </xsl:if>
           <xsl:if test="$type = 'external'">
               <section>
                 <title>Complete Bug Overview</title>
                 <para>
                    Diagram shows an overview of all external bugs and wrong implementations,
                    which are unresolved and/or not closed, yet.
                    <itemizedlist>
                       <listitem>
                          <para>
                             <emphasis role="bold">Open External Issues(Unclosed): </emphasis> are all external
                             issues/tickets, which does not have been closed by customer in external change
                             management system, yet. These issues may have to be still verified or have been
                             resolved and delivered already.
                          </para>
                       </listitem>
                       <listitem>
                          <para>
                             <emphasis role="bold">Open External Bugs(Unresolved): </emphasis> are all external
                             issues/tickets, which does not have resolved by development team, yet. These issues
                             may have to be still verified or have, but they have not been resolved internally.
                             The gap between <emphasis role="italic">Open External Bugs(Unresolved)</emphasis>
                             and <emphasis role="italic">Open External Bugs(Unclosed)</emphasis> are the number
                             of already resolved external tickets, which have to be released to or accepted by
                             the customer.
                          </para>
                       </listitem>
                       <listitem>
                          <para>
                             <emphasis role="bold">Open External Bugs(Unresolved): </emphasis> are all externally found
                             issues, which does not have been scheduled, yet. These issues may have to be still verified
                             or have or they have not been resolved internally.
                          </para>
                       </listitem>
                    </itemizedlist>
                    <mediaobject>
                       <imageobject role="fo">
                          <imagedata fileref="../../generated-sdocbook/images/svg/issues_extbugs.svg" format="SVG" />
                       </imageobject>
                       <imageobject role="html">
                          <imagedata fileref="images/jpg/issues_extbugs.jpg" format="JPG" />
                       </imageobject>
                    </mediaobject>
                 </para>
              </section>
            </xsl:if>
            
            <section>
               <title>Issue/Tickets for Release <xsl:value-of select="$version"/> for external issues.</title>
               <para>
                  Issues and Tickets, which have to be tested for the release.
               </para>
               <table frame="all">
                  <title>Test results for issues</title>
                  <tgroup cols="6" align="left" colsep="1" rowsep="1">
                     <colspec colwidth="60pt" colnum="1" colname="c1"/>
                     <colspec colwidth="60pt" colnum="2" colname="c2"/>
                     <colspec colwidth="60pt" colnum="3" colname="c3"/>
                     <colspec colnum="4" colname="c4"/>
                     <colspec colwidth="45pt" colnum="5" colname="c5"/>
                     <colspec colwidth="45pt" colnum="6" colname="c6"/>
                     <thead>
                        <row>
                           <entry>Internal Issue No.</entry>
                           <entry>External Issue No.</entry>
                           <entry>Testcase-ID</entry>
                           <entry>Summary</entry>
                           <entry>Version</entry>
                           <entry>Test Result</entry>
                        </row>
                     </thead>
                     <tbody>
                        <xsl:apply-templates select="//cms:issue[contains(cms:version,$version) and (cms:type = $cms.cr.type or cms:type = $cms.bug.type) and cms:external-id]" mode="tested">
                           <xsl:sort select="cms:id" order="ascending" data-type="text"/>
                        </xsl:apply-templates>
                        <row>
                           <entry></entry>
                           <entry></entry>
                           <entry></entry>
                           <entry></entry>
                           <entry></entry>
                           <entry></entry>
                        </row>
                     </tbody>
                  </tgroup>
               </table>
            </section>

            <section>
               <title>Issue/Tickets for Release <xsl:value-of select="$version"/> INTERNAL.</title>
               <para>
                  Issues and Tickets, which have to be tested for the release.
               </para>
               <table frame="all">
                  <title>Test results for issues</title>
                  <tgroup cols="6" align="left" colsep="1" rowsep="1">
                     <colspec colwidth="60pt" colnum="1" colname="c1"/>
                     <colspec colwidth="60pt" colnum="2" colname="c2"/>
                     <colspec colwidth="60pt" colnum="3" colname="c3"/>
                     <colspec colnum="4" colname="c4"/>
                     <colspec colwidth="45pt" colnum="5" colname="c5"/>
                     <colspec colwidth="45pt" colnum="6" colname="c6"/>
                     <thead>
                        <row>
                           <entry>Internal Issue No.</entry>
                           <entry>External Issue No.</entry>
                           <entry>Testcase-ID</entry>
                           <entry>Summary</entry>
                           <entry>Version</entry>
                           <entry>Test Result</entry>
                        </row>
                     </thead>
                     <tbody>
                        <xsl:apply-templates select="//cms:issue[contains(cms:version,$version) and (cms:type = $cms.cr.type or cms:type = $cms.bug.type) and not(cms:external-id)]" mode="tested">
                           <xsl:sort select="cms:id" order="ascending" data-type="text"/>
                        </xsl:apply-templates>
                        <row>
                           <entry></entry>
                           <entry></entry>
                           <entry></entry>
                           <entry></entry>
                           <entry></entry>
                           <entry></entry>
                        </row>
                     </tbody>
                  </tgroup>
               </table>
            </section>
            
            <xsl:if test="$type = 'internal'"> 
               <section>
                  <title>Automated Testcases without Test Specs</title>
                  <para>
                     <table frame="all">
                        <title>Tests without Testspec</title>
                        <tgroup cols="4" align="left" colsep="1" rowsep="1">
                           <colspec colwidth="100pt" colnum="1" colname="c1"/>
                           <colspec colwidth="45pt" colnum="2" colname="c2"/>
                           <colspec colnum="3" colname="c3"/>
                           <colspec colwidth="45pt" colnum="4" colname="c4"/>
                           <thead>
                              <row>
                                 <entry>Shortname</entry>
                                 <entry>Executor</entry>
                                 <entry>Comment</entry>
                                 <entry>Test Result</entry>
                              </row>
                           </thead>
                           <tbody>
                              <xsl:for-each select="//tr:testresult[tr:version = $version.releasecandidate and not(tr:shortname = '' or not(tr:shortname))]">
                                 <xsl:sort select="key" order="ascending" data-type="text"/>
                                 <xsl:variable name="shortname" select="tr:shortname"/>
                                 <xsl:variable name="executor"  select="tr:executor"/>
                                 <xsl:variable name="comment"   select="tr:comment"/>
                                 <xsl:variable name="result"    select="tr:result"/>
                                 <xsl:if test="not(//tc:test[tc:shortname = $shortname])">
                                     <row>
                                        <entry><xsl:value-of select="$shortname"/></entry>
                                        <entry><xsl:value-of select="$executor"/></entry>
                                        <entry><xsl:value-of select="$comment"/></entry>
                                        <entry><xsl:value-of select="$result"/></entry>
                                     </row>
                                 </xsl:if>
                              </xsl:for-each>
                              <row>
                                 <entry></entry>
                                 <entry></entry>
                                 <entry></entry>
                                 <entry></entry>
                              </row>
                           </tbody>
                        </tgroup>
                     </table>
                  </para>
               </section>
            </xsl:if>
            
            <section>
               <title>Specified Test Cases (Tested)</title>
               <xsl:call-template name="result_list_specified">
                  <xsl:with-param name="testcase_filter" select="''"/>
               </xsl:call-template>
            </section>
            
            <section>
               <title>Specified Test Cases (NOT Tested)</title>
               <para>
               Specified test cases, which have not been executed for the release candidate.
               </para>
               <xsl:call-template name="result_list_untested">
                  <xsl:with-param name="testcase_filter" select="''"/>
               </xsl:call-template>
            </section>
            
         </chapter>
          
         <xsl:if test="$type = 'internal'"> 
            <chapter>
               <title>Test Coverage</title>
               
               <xsl:call-template name="coverage_stats"/>
               <xsl:apply-templates select="//uc:usecases[uc:info/@project = 'Main Specification']" mode="simple_coverage"/>
               
               <section>
                  <title>Change Requests / Specifications</title>
                  <section>
                     <title>Description</title>
                     <para>
                        The coverage statistics gives transparency about the current quality of the specified
                        use cases for the software. It displays several levels of coverage and quality. The
                        rows of the coverage tables are defined as:
                        <itemizedlist>
                           <listitem>
                              <para>
                                 First row is the use case ID giving you a fix reference to the use case. 
                              </para>
                           </listitem>
                           <listitem>
                              <para>
                                 Second row is the name or short description of the use case giving you
                                 hint about the functionality. 
                              </para>
                           </listitem>
                           <listitem>
                              <para>
                                 Third row is the list of test specifications covering this use case. If they
                                 are displayed within brackets e.g. (TEST_0001) it is a draft test specification
                                 not yet finalized and useful for regression testing. Without brackets it is a full
                                 test specification within the regression test suite.
                              </para>
                           </listitem>
                           <listitem>
                              <para>
                                 Fourth row is the count of test specifications covering this use case. The number
                                 outside of the brackets are the final test specifications. The number within the
                                 brackets are the draft ones. 
                              </para>
                           </listitem>
                           <listitem>
                              <para>
                                 The fifth row is showing how many test specifications have been executed for this
                                 release. 
                              </para>
                           </listitem>
                           <listitem>
                              <para>
                                 The last row is showing how many of the executed test specifications have been set to
                                 passed. The have been executed without raising any erroneous behaviour. 
                              </para>
                           </listitem>
                        </itemizedlist> 
                     </para>
                  </section>
                  <xsl:apply-templates select="//uc:usecases[not(uc:info/@project = 'Main Specification')]" mode="simple_coverage"/>
               </section>
            </chapter>
         </xsl:if>
         
         <xsl:if test="$type = 'internal'"> 
            <chapter>
               <title>Test Specifications without traceability</title>
               <para>
                  Test specifications in that list does not have a traceability tag set to a valid use
                  case or the use cases defined in the traceability does not exist within any specification document.
               </para>
               <table frame="all">
                  <title>Tests without traceability</title>
                  <tgroup cols="3" align="left" colsep="1" rowsep="1">
                     <colspec colnum="1" colname="c1"/>
                     <colspec colwidth="250pt" colnum="2" colname="c2"/>
                     <colspec colwidth="50pt" colnum="3" colname="c3"/>
                     <thead>
                        <row>
                           <entry>Test ID</entry>
                           <entry>Shortname</entry>
                           <entry>Usecase ID</entry>
                        </row>
                     </thead>
                     <tbody>
                        <xsl:apply-templates select="//tc:test[not(key('usecase-group',tc:traceability))]"/>
                        <row>
                           <entry></entry>
                           <entry></entry>
                           <entry></entry>
                        </row>               
                     </tbody>
                  </tgroup>
               </table>               
            </chapter>
         </xsl:if>
         
         <xsl:if test="'a' = 'b' and $type = 'internal'"> 
            <chapter>
               <title>Efficiency</title>
               <xsl:apply-templates select="//root" mode="test_efficiency"/>               
            </chapter>
         </xsl:if>
         
      </book>
   </xsl:template>
   
   <!-- 
       *********************
       Statistical templates
       ********************* 
    -->
   
   <xsl:template name="overview">
      <chapter>
         <title>Statistical Overview</title>
         <para>
            <table frame="all">
               <title>Statistics</title>
               <tgroup cols="2" align="left" colsep="1" rowsep="1">
                  <colspec colwidth="250pt" colnum="1" colname="c1"/>
                  <colspec colnum="2" colname="c2" align="right"/>
                  <thead>
                     <row>
                        <entry>Category</entry>
                        <entry>Value</entry>
                     </row>
                  </thead>
                  <tbody>
                     <xsl:variable name="number_specified_tests" select="count(//tc:test[not(tc:state = 'draft')])"/>
                     <xsl:variable name="number_specified_tests_draft" select="count(//tc:test[tc:state = 'draft'])"/>
                     <xsl:variable name="number_executed_tests" select="count(key('testresult-group',.))"/>
                     <xsl:variable name="number_executed_testspecs_final" select="count(//tc:test[key('testresult-testcase-final-group',tc:id) or key('testresult-shortname-final-group',tc:shortname)])"/>
                     <xsl:variable name="number_executed_testspecs_final_passed" select="count(//tc:test[key('testresult-testcase-final-group',tc:id)[tr:result = 'passed'] or key('testresult-shortname-final-group',tc:shortname)[tr:result = 'passed']])"/>
                     <xsl:variable name="number_executed_testspecs_draft" select="count(//tc:test[key('testresult-testcase-draft-group',tc:id) or key('testresult-shortname-draft-group',tc:shortname)])"/>
                     <xsl:variable name="number_executed_testspecs_draft_passed" select="count(//tc:test[key('testresult-testcase-draft-group',tc:id)[tr:result = 'passed'] or key('testresult-shortname-draft-group',tc:shortname)[tr:result = 'passed']])"/>
                     <xsl:variable name="number_issues" select="count(//cms:issue[(cms:type = $cms.bug.type or cms:type = $cms.cr.type) and contains(cms:version,$version)])"/>
                     <xsl:variable name="number_accepted_issues" select="count(//cms:issue[(cms:type = $cms.bug.type or cms:type = $cms.cr.type) and (cms:state = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
                     <xsl:variable name="number_tests" select="count(//tr:testresult[starts-with(tr:version,$version)])"/>
                     <xsl:variable name="number_tests_passed" select="count(//tr:testresult[tr:result = 'passed' and starts-with(tr:version,$version)])"/>
                     <xsl:variable name="number_tests_failed" select="count(//tr:testresult[tr:result = 'failed' and starts-with(tr:version,$version)])"/>
                     <xsl:variable name="number_automated_jmeter_tests" select="count(//tr:testresult[string-length(tr:shortname) &gt; 0 and tr:executor = 'JMeter'])"/>
                     <xsl:variable name="number_automated_jmeter_tests_passed" select="count(//tr:testresult[tr:result = 'passed' and string-length(tr:shortname) &gt; 0 and tr:executor = 'JMeter'])"/>
                     <xsl:variable name="number_automated_jmeter_tests_failed" select="count(//tr:testresult[tr:result = 'failed' and string-length(tr:shortname) &gt; 0 and tr:executor = 'JMeter'])"/>
                     <xsl:variable name="number_automated_selenium_tests" select="count(//tr:testresult[string-length(tr:shortname) &gt; 0 and tr:executor = 'Selenium'])"/>
                     <xsl:variable name="number_automated_selenium_tests_passed" select="count(//tr:testresult[tr:result = 'passed' and string-length(tr:shortname) &gt; 0 and tr:executor = 'Selenium'])"/>
                     <xsl:variable name="number_automated_selenium_tests_failed" select="count(//tr:testresult[tr:result = 'failed' and string-length(tr:shortname) &gt; 0 and tr:executor = 'Selenium'])"/>
                     <row>
                        <entry><emphasis role="bold">Test Specifications (final/draft)</emphasis></entry>
                        <entry><xsl:value-of select="$number_specified_tests"/>/<xsl:value-of select="$number_specified_tests_draft"/></entry>
                     </row><xsl:text>
                     </xsl:text>
                     <row>
                        <entry><emphasis role="bold">Test Specifications executed (final)</emphasis></entry>
                        <entry><xsl:value-of select="$number_executed_testspecs_final"/> (<xsl:value-of select="round($number_executed_testspecs_final div $number_specified_tests * 1000) div 10"/> %)</entry>
                     </row><xsl:text>
                     </xsl:text>
                     <row>
                        <entry><emphasis role="bold">Test Specifications passed (final)</emphasis></entry>
                        <entry><xsl:value-of select="$number_executed_testspecs_final_passed"/> (<xsl:value-of select="round($number_executed_testspecs_final_passed div $number_executed_testspecs_final * 1000) div 10"/> %)</entry>
                     </row><xsl:text>
                     </xsl:text>
                     <row>
                        <entry><emphasis role="bold">Test Specifications executed (draft)</emphasis></entry>
                        <entry><xsl:value-of select="$number_executed_testspecs_draft"/> (<xsl:value-of select="round($number_executed_testspecs_draft div $number_specified_tests_draft * 1000) div 10"/> %)</entry>
                     </row><xsl:text>
                     </xsl:text>
                     <row>
                        <entry><emphasis role="bold">Test Specifications passed (draft)</emphasis></entry>
                        <entry><xsl:value-of select="$number_executed_testspecs_draft_passed"/> (<xsl:value-of select="round($number_executed_testspecs_draft_passed div $number_executed_testspecs_draft * 1000) div 10"/> %)</entry>
                     </row><xsl:text>
                     </xsl:text>
                     <row>
                        <entry><emphasis role="bold">Issues in <xsl:value-of select="$version"/></emphasis></entry>
                        <entry><xsl:value-of select="$number_issues"/></entry>
                     </row><xsl:text>
                     </xsl:text>
                     <row>
                        <entry><emphasis role="bold">Issues Accepted</emphasis></entry>
                        <entry><xsl:value-of select="$number_accepted_issues"/> (<xsl:value-of select="round($number_accepted_issues div $number_issues * 1000) div 10"/> %)</entry>
                     </row><xsl:text>
                     </xsl:text>
                  </tbody>
               </tgroup>
            </table>
           </para>
           <xsl:if test="$type = 'internal'">
              <para>
                 The acceptance criterias provide an overview of the current state of the
                 release. They are defined key indicators covering particular scopes of
                 the acceptance progress, test execution and coverage, test automization
                 and a benchmark about the amount of known bugs in the application.
              </para>
              <para>
                 To give you a quick overview on the criteria a net diagram showing all criteria
                 has been provided below. Each acceptance criteria represented by one axis
                 contains a "planned" criteria limit. These planned criteria limits are connected
                 as a ring. The curren value of the criterias are connected with each other shown
                 as a filled area starting from the center of the diagram. If A value has passed
                 or fits exactly to the criterias limit it is defined as "reached".
              </para>
              <para>
                 The criteria are defined as:
                 <itemizedlist>
                    <listitem>
                       <para><emphasis role="bold">Acceptance: </emphasis>
                          The percentage of issues within the change management system assigned
                          to the release under test, which have been accepted by the quality assurance.
                       </para>
                    </listitem>
                    <listitem>
                       <para><emphasis role="bold">Passed: </emphasis>
                          The percentage of executed test specifications and test cases, which have
                          been passed.
                       </para>
                    </listitem>
                    <listitem>
                       <para><emphasis role="bold">Regression: </emphasis>
                          The percentage of specified test cases, which have been executed for
                          this release.
                       </para>
                    </listitem>
                    <listitem>
                       <para><emphasis role="bold">Coverage: </emphasis>
                          The percentage of of all use cases specified, which are covered by
                          at least one test specification.
                       </para>
                    </listitem>
                    <listitem>
                       <para><emphasis role="bold">Automization: </emphasis>
                          The percentage of test specifications, which have been automated.
                       </para>
                    </listitem>
                    <listitem>
                       <para><emphasis role="bold">Know Issue Criteria (TO BE DEFINED): </emphasis>
                          This value should benchmark about the known issues and criticality to
                          show bug prone the application is at the moment.
                       </para>
                    </listitem>
                 </itemizedlist>
                 <mediaobject>
                    <imageobject role="fo">
                       <imagedata fileref="../../generated-sdocbook/images/svg/release_criteria.svg" format="SVG" />
                    </imageobject>
                    <imageobject role="html">
                       <imagedata fileref="images/jpg/release_criteria.jpg" format="JPG" />
                    </imageobject>
                 </mediaobject>
              </para>
           </xsl:if>
           <para>
              Diagram shows an overview of all testcases for that branch. If you are looking
              at a developer release qa-report, you will see the current number of testcases.
              If you are looking at a qa-report of a branch release you will see the current
              statistic only including the testcases for that branch. 
              <mediaobject>
                 <imageobject role="fo">
                    <imagedata fileref="../../generated-sdocbook/images/svg/summary_testcases.svg" format="SVG" />
                 </imageobject>
                 <imageobject role="html">
                    <imagedata fileref="images/jpg/summary_testcases.jpg" format="JPG" />
                 </imageobject>
              </mediaobject>
           </para>
           <xsl:if test="$type = 'internal'">           
              <para>
                 The diagram below indicates the progress about test coverage of the main specification
                 document of the application. It lists the numbers of use cases over time and how many
                 use cases of the main specification have been covered by specified test cases.
                 <mediaobject>
                    <imageobject role="fo">
                       <imagedata fileref="../../generated-sdocbook/images/svg/test_coverage.svg" format="SVG" />
                    </imageobject>
                    <imageobject role="html">
                       <imagedata fileref="images/jpg/test_coverage.jpg" format="JPG" />
                    </imageobject>
                 </mediaobject>
              </para>
              <para>
                 The diagram below indicates the progress about test coverage of all use cases specified
                 including all finished and unfinished change requests. It lists the numbers of use cases
                 over time and how many use cases have been covered by specified test cases.
                 <mediaobject>
                    <imageobject role="fo">
                       <imagedata fileref="../../generated-sdocbook/images/svg/test_coverage_all.svg" format="SVG" />
                    </imageobject>
                    <imageobject role="html">
                       <imagedata fileref="images/jpg/test_coverage_all.jpg" format="JPG" />
                    </imageobject>
                 </mediaobject>
              </para>
           </xsl:if>
      </chapter>
   </xsl:template>
   
   <xsl:template name="coverage_stats">
      <section>
         <title>Coverage Statistics</title>
         <section>
            <title>Description</title>
            <para>
               The coverage statistics gives transparency about the current quality of the specified
               use cases for the software. It displays several levels of coverage and quality. The
               rows of the coverage tables are defined as:
               <itemizedlist>
                  <listitem>
                     <para>
                        First row is the name of the specification document. As available you will
                        get also the information of the issue number including a reference to the
                        change management system as well as the version number of the issue related to. 
                     </para>
                  </listitem>
                  <listitem>
                     <para>
                        Second row is the count of the use cases within the document.
                     </para>
                  </listitem>
                  <listitem>
                     <para>
                        Third row is the count of use cases covered by the test specifications. The
                        values outside the brackets are showing the use cases covered by at least one
                        test specification. The values inside the brackets shows the number of use
                        cases covered by draft test specifications. There may be use cases, for
                        which you have draft and final test specifications. Thes use cases will be
                        counted for both draft and final coverage.  
                     </para>
                  </listitem>
                  <listitem>
                     <para>
                        The fourth row path coverage is not supported, yet. 
                     </para>
                  </listitem>
                  <listitem>
                     <para>
                        The fifth row test coverage is not supported, yet. 
                     </para>
                  </listitem>
               </itemizedlist> 
            </para>
         </section>
         <section>
            <title>Change Requests for Version <xsl:value-of select="$version"/></title>
            <table frame="all">
               <title>Covered Use Cases</title>
               <tgroup cols="5" align="left" colsep="1" rowsep="1">
                  <colspec colwidth="250pt" colnum="1" colname="c1"/>
                  <colspec colnum="2" colname="c2"/>
                  <colspec colwidth="50pt" colnum="3" colname="c3"/>
                  <colspec colwidth="50pt" colnum="4" colname="c4"/>
                  <colspec colwidth="50pt" colnum="5" colname="c5"/>
                  <thead>
                     <row>
                        <entry>Root File</entry>
                        <entry>Use Cases</entry>
                        <entry>Use Case Coverage (draft/final)</entry>
                        <entry>Path Coverage</entry>
                        <entry>Test Coverage</entry>
                     </row>
                  </thead>
                  <tbody>
                     <xsl:for-each select="//uc:usecases[generate-id() = generate-id(key('usecases-group', .))]">
                        <xsl:variable name="cr_id"      select="uc:info/@issue"/>
                        <xsl:variable name="cr_version">
                           <xsl:choose>
                              <xsl:when test="//cms:issue[cms:external-id = $cr_id]/cms:version 
                                              and not(//cms:issue[cms:external-id = $cr_id]/cms:version = '')">
                                 <xsl:value-of select="//cms:issue[cms:external-id = $cr_id]/cms:version"/>
                              </xsl:when>
                              <xsl:otherwise>unplanned</xsl:otherwise>
                           </xsl:choose>
                        </xsl:variable>
                        <xsl:if test="$cr_version = $version">
                           <row>
                              <xsl:variable name="uc_number"        select="count(uc:usecase)"/>
                              <xsl:variable name="uc_covered"       select="count(uc:usecase[key('test-group-final',@id)])"/>
                              <xsl:variable name="uc_covered_draft" select="count(uc:usecase[key('test-group-draft',@id)])"/>
                              <entry>
                                 <xsl:value-of select="uc:info/@project"/><xsl:text> </xsl:text>(<xsl:value-of select="$cr_version"/>)
                                 <xsl:call-template name="link_to_cms">
                                     <xsl:with-param name="issue_id" select="uc:info/@issue"/>
                                 </xsl:call-template>
                              </entry>
                              <entry><xsl:value-of select="$uc_number"/></entry>
                              <xsl:choose>
                                 <xsl:when test="not($uc_number = 0)">
                                    <entry><xsl:value-of select="$uc_covered"/>/<xsl:value-of select="$uc_covered_draft"/><xsl:text> (~ </xsl:text><xsl:value-of select="round(($uc_covered div $uc_number) * 100)"/><xsl:value-of select="' %)'"/>/<xsl:value-of select="round(($uc_covered_draft div $uc_number) * 100)"/><xsl:value-of select="' %)'"/></entry>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <entry><xsl:value-of select="$uc_covered"/><xsl:text> (0 %)</xsl:text></entry>
                                 </xsl:otherwise>
                              </xsl:choose>
                              <entry>N/A</entry>
                              <entry>N/A</entry>
                           </row>
                        </xsl:if>
                     </xsl:for-each>
                     <row>
                        <entry></entry>
                        <entry></entry>
                        <entry></entry>
                        <entry></entry>
                        <entry></entry>
                     </row>
                  </tbody>
               </tgroup>
            </table>
         </section>
         <section>
            <title>Change Requests for other Versions</title>
            <table frame="all">
               <title>Covered Use Cases</title>
               <tgroup cols="5" align="left" colsep="1" rowsep="1">
                  <colspec colwidth="250pt" colnum="1" colname="c1"/>
                  <colspec colnum="2" colname="c2"/>
                  <colspec colwidth="50pt" colnum="3" colname="c3"/>
                  <colspec colwidth="50pt" colnum="4" colname="c4"/>
                  <colspec colwidth="50pt" colnum="5" colname="c5"/>
                  <thead>
                     <row>
                        <entry>Root File</entry>
                        <entry>Use Cases</entry>
                        <entry>Use Case Coverage (final/draft)</entry>
                        <entry>Path Coverage</entry>
                        <entry>Test Coverage</entry>
                     </row>
                  </thead>
                  <tbody>
                     <xsl:for-each select="//uc:usecases[generate-id() = generate-id(key('usecases-group', .))]">
                        <xsl:variable name="cr_id"      select="uc:info/@issue"/>
                        <xsl:variable name="cr_version">
                           <xsl:choose>
                              <xsl:when test="//cms:issue[cms:external-id = $cr_id]/cms:version 
                                              and not(//cms:issue[cms:external-id = $cr_id]/cms:version = '')">
                                 <xsl:value-of select="//cms:issue[cms:external-id = $cr_id]/cms:version"/>
                              </xsl:when>
                              <xsl:otherwise>unplanned</xsl:otherwise>
                           </xsl:choose>
                        </xsl:variable>
                        <xsl:if test="not($cr_version = $version)">
                           <row>
                              <xsl:variable name="uc_number"        select="count(uc:usecase)"/>
                              <xsl:variable name="uc_covered"       select="count(uc:usecase[key('test-group-final',@id)])"/>
                              <xsl:variable name="uc_covered_draft" select="count(uc:usecase[key('test-group-draft',@id)])"/>
                              <entry>
                                 <xsl:value-of select="uc:info/@project"/><xsl:text> </xsl:text>(<xsl:value-of select="$cr_version"/>)
                                 <xsl:call-template name="link_to_cms">
                                     <xsl:with-param name="issue_id" select="uc:info/@issue"/>
                                 </xsl:call-template>, Version: <xsl:value-of select="$cr_version"/>
                              </entry>
                              <entry><xsl:value-of select="$uc_number"/></entry>
                              <xsl:choose>
                                 <xsl:when test="not($uc_number = 0)">
                                    <entry><xsl:value-of select="$uc_covered"/>/<xsl:value-of select="$uc_covered_draft"/><xsl:text> (~ </xsl:text><xsl:value-of select="round(($uc_covered div $uc_number) * 100)"/><xsl:value-of select="' %)'"/>/<xsl:value-of select="round(($uc_covered_draft div $uc_number) * 100)"/><xsl:value-of select="' %)'"/></entry>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <entry><xsl:value-of select="$uc_covered"/><xsl:text> (0 %)</xsl:text></entry>
                                 </xsl:otherwise>
                              </xsl:choose>
                              <entry>N/A</entry>
                              <entry>N/A</entry>
                           </row>
                        </xsl:if>
                     </xsl:for-each>
                     <row>
                        <entry></entry>
                        <entry></entry>
                        <entry></entry>
                        <entry></entry>
                        <entry></entry>
                     </row>
                  </tbody>
               </tgroup>
            </table>
         </section>
      </section>
   </xsl:template>
   
   <xsl:template match="root" mode="test_efficiency">
      <xsl:variable name="tr_at_all"           select="count(//tr:testresult[number(tr:time) = number(tr:time)])"/>
      <xsl:variable name="tr_issue_at_all"     select="count(//tr:testresult[tr:issue and not(tr:issue = '') and number(tr:time) = number(tr:time)])"/>
      <xsl:variable name="tr_test_at_all"      select="count(//tr:testresult[tr:testcase and not(tr:testcase = '') and number(tr:time) = number(tr:time)])"/>
      <xsl:variable name="tr_time_spent"       select="sum(//tr:testresult/tr:time[number(.)=number(.)])"/>
      <xsl:variable name="tr_issue_time_spent" select="sum(//tr:testresult[tr:issue and not(tr:issue = '')]/tr:time[number(.) = number(.)])"/>
      <xsl:variable name="tr_test_time_spent"  select="sum(//tr:testresult[tr:testcase and not(tr:testcase = '')]/tr:time[number(.) = number(.)])"/>
      <xsl:variable name="tr_avg_time"         select="$tr_time_spent div $tr_at_all"/>
      <xsl:variable name="tr_issue_avg_time"   select="$tr_issue_time_spent div $tr_issue_at_all"/>
      <xsl:variable name="tr_test_avg_time"    select="$tr_issue_time_spent div $tr_test_at_all"/>
      <table frame="all">
         <title>Test Efficieny</title>
         <tgroup cols="2" align="left" colsep="1" rowsep="1">
            <colspec colwidth="250pt" colnum="1" colname="c1"/>
            <colspec colnum="2" colname="c2"/>
            <thead>
               <row>
                  <entry>Efficiency Type</entry>
                  <entry>Value</entry>
               </row>
            </thead>
            <tbody>
               <row>
                  <entry>Number of test results</entry>
                  <entry><xsl:value-of select="$tr_at_all"/></entry>
               </row>
               <row>
                  <entry>Number of test results for issues</entry>
                  <entry><xsl:value-of select="$tr_issue_at_all"/></entry>
               </row>
               <row>
                  <entry>Number of test results for test specifications</entry>
                  <entry><xsl:value-of select="$tr_test_at_all"/></entry>
               </row>
               <row>
                  <entry>Total time spent</entry>
                  <entry><xsl:value-of select="$tr_time_spent"/></entry>
               </row>
               <row>
                  <entry>Total time spent for issue testing</entry>
                  <entry><xsl:value-of select="$tr_issue_time_spent"/></entry>
               </row>
               <row>
                  <entry>Total time spent for test cases (specified)</entry>
                  <entry><xsl:value-of select="$tr_test_time_spent"/></entry>
               </row>
               <row>
                  <entry>Average time spent</entry>
                  <entry><xsl:value-of select="$tr_avg_time"/></entry>
               </row>
               <row>
                  <entry>Average time spent for testing issues</entry>
                  <entry><xsl:value-of select="$tr_issue_avg_time"/></entry>
               </row>
               <row>
                  <entry>Average time spent test cases (specified)</entry>
                  <entry><xsl:value-of select="$tr_test_avg_time"/></entry>
               </row>
            </tbody>
         </tgroup>
      </table>
   </xsl:template>
      
   <!-- 
       **************************
       test result list templates
       **************************
    -->
   
   <xsl:template name="result_list_specified">
      <xsl:param name="testcase_filter" select="'NO FILTER SET'"/>
      <xsl:variable name="cols_by_type">
         <xsl:if test="$type = 'internal'">7</xsl:if>
         <xsl:if test="$type = 'external'">5</xsl:if>
      </xsl:variable>
      
      <section>
         <title><xsl:value-of select="$testcase_filter"/>XXXXX</title>
         <table frame="all">
            <title>specified test cases for </title>
            <tgroup cols="{$cols_by_type}" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60pt" colnum="1" colname="c1"/>
               <colspec colwidth="100pt" colnum="2" colname="c2"/>
               <colspec colnum="3" colname="c3"/>
               <colspec colwidth="45pt" colnum="4" colname="c4"/>
               <colspec colwidth="45pt" colnum="5" colname="c5"/>
               <xsl:if test="$type = 'internal'">
                  <colspec colnum="6" colname="c6"/>
               </xsl:if>
               <xsl:if test="$type = 'internal'">
                  <colspec colwidth="30pt" colnum="7" colname="c7"/>
               </xsl:if>
               <thead>
                  <row>
                     <entry>Testcase-ID</entry>
                     <entry>Short Name</entry>
                     <entry>Comment</entry>
                     <entry>version</entry>
                     <xsl:if test="$type = 'internal'">
                        <entry>executor</entry>
                     </xsl:if>
                     <entry>Test Result</entry>
                     <xsl:if test="$type = 'internal'">
                        <entry>Log</entry>
                     </xsl:if>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates select="//tr:testresult[starts-with(tr:version,$version)]" mode="specified">
                     <xsl:sort select="tr:testcase" order="ascending" data-type="text"/>
                     <xsl:with-param name="testcase_filter" select="$testcase_filter"/>
                  </xsl:apply-templates>
                  <row>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <entry></entry>
                     <xsl:if test="$type = 'internal'">
                        <entry></entry>
                     </xsl:if>
                     <entry></entry>
                  </row>
               </tbody>
            </tgroup>
         </table>
      </section>
   </xsl:template> 
   
   <xsl:template name="result_list_untested">
      <xsl:param name="testcase_filter"  select="'NO FILTER SET'"/>
      <xsl:variable name="cols_by_type">
         <xsl:if test="$type = 'internal'">5</xsl:if>
         <xsl:if test="$type = 'external'">4</xsl:if>
      </xsl:variable>
      
      <section>
         <title><xsl:value-of select="$testcase_filter"/>XXXXX</title>
         <table frame="all">
            <title>untested test cases (specified)</title>
            <tgroup cols="4" align="left" colsep="1" rowsep="1">
               <colspec colwidth="60pt" colnum="1" colname="c1"/>
               <colspec colwidth="100pt" colnum="2" colname="c2"/>
               <colspec colnum="3" colname="c3"/>
               <colspec colwidth="60pt" colnum="4" colname="c4"/>
               <thead>
                  <row>
                     <entry>Testcase-ID</entry>
                     <entry>Short Name</entry>
                     <entry>Issue/Ticket No's</entry>
                     <entry>Test Result</entry>
                  </row>
               </thead>
               <tbody>
                  <xsl:apply-templates select="//tc:test[contains(tc:id, $testcase_filter)]" mode="untested">
                     <xsl:sort select="tc:id" order="ascending" data-type="text"/>
                  </xsl:apply-templates>
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
   
   <xsl:template match="tr:testresult" mode="specified">
      <xsl:param name="this_shortname" select="tr:shortname"/>
      <xsl:param name="testcase_filter" select="'NO FILTER SET'"/>
      <xsl:variable name="testcase_id">
         <xsl:choose>
            <xsl:when test="string-length(tr:testcase) &gt; 0"><xsl:value-of select="tr:testcase"/></xsl:when>
            <xsl:when test="key('test-shortname-group',$this_shortname)"><xsl:value-of select="key('test-shortname-group',$this_shortname)/tc:id"/></xsl:when>
            <xsl:otherwise>STEPS</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:if test="not($testcase_id = 'STEPS') and contains($testcase_id, $testcase_filter)">
         <row>
            <entry>
               <ulink url="all_testspec.html#{$testcase_id}">
                  <citetitle><xsl:value-of select="$testcase_id"/></citetitle>
               </ulink>
            </entry>
            <entry>
               <xsl:call-template name="makebreak">
                  <xsl:with-param name="text" select="$this_shortname" />
               </xsl:call-template>           
            </entry>
            <entry>
               <xsl:value-of select="tr:comment"/>            
            </entry>
            <entry>
               <xsl:choose>
                  <xsl:when test="tr:version = $version.releasecandidate">
<xsl:text disable-output-escaping="yes">&lt;?dbhtml bgcolor="Lime" ?&gt;&lt;?dbfo bgcolor="Lime" ?&gt;</xsl:text>
                     <xsl:value-of select="tr:version"/>
                  </xsl:when>
                  <xsl:otherwise>
<xsl:text disable-output-escaping="yes">&lt;?dbhtml bgcolor="yellow" ?&gt;&lt;?dbfo bgcolor="yellow" ?&gt;</xsl:text>
                     <xsl:value-of select="tr:version"/>
                  </xsl:otherwise>            
               </xsl:choose>
            </entry>
            <xsl:if test="$type = 'internal'">
               <entry>
                  <xsl:value-of select="tr:executor"/>            
               </entry>
            </xsl:if>
            <entry>
               <xsl:apply-templates select="tr:result"/>
            </entry>
            <xsl:if test="$type = 'internal'">
               <entry>
                  <xsl:variable name="shortname_temp" select="//tc:test[tc:id = $testcase_id]/tc:shortname"/>
                  <xsl:choose>
                     <xsl:when test="key('mappings-group', $shortname_temp)/session_id">
                        <xsl:for-each select="key('mappings-group', $shortname_temp)/session_id">
                           <ulink url="log.html#{.}">
                              <citetitle>Log</citetitle>
                           </ulink><xsl:if test="position() != last()"><xsl:text>, </xsl:text></xsl:if>
                        </xsl:for-each>
                     </xsl:when>
                     <xsl:otherwise>N/A</xsl:otherwise>
                  </xsl:choose>
               </entry>
            </xsl:if>
         </row>
      </xsl:if>
   </xsl:template>
   
   <xsl:template match="tr:testresult" mode="unspecified">
      <xsl:param name="this_shortname" select="tr:shortname"/>
      <xsl:variable name="testcase_id">
         <xsl:choose>
            <xsl:when test="string-length(tr:testcase) &gt; 0"><xsl:value-of select="tr:testcase"/></xsl:when>
            <xsl:when test="key('test-shortname-group',$this_shortname)"><xsl:call-template name="lookup_testcase_id"><xsl:with-param name="shortname" select="tr:shortname"/></xsl:call-template></xsl:when>
            <xsl:otherwise>STEPS</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:if test="$testcase_id = 'STEPS'">
         <row>
            <entry>
               <xsl:call-template name="makebreak">
                  <xsl:with-param name="text" select="$this_shortname" />
               </xsl:call-template>
            </entry>
            <entry>
               <xsl:for-each select="tr:issue"><xsl:call-template name="link_to_cms">
                  <xsl:with-param name="issue_id" select="."/>
               </xsl:call-template><xsl:if test="not(position() = last())">, </xsl:if></xsl:for-each>
            </entry>
            <entry>
               <xsl:value-of select="tr:comment"/>
            </entry>
            <entry>
               <xsl:choose>
                  <xsl:when test="tr:version = $version.releasecandidate">
<xsl:text disable-output-escaping="yes">&lt;?dbhtml bgcolor="Lime" ?&gt;&lt;?dbfo bgcolor="Lime" ?&gt;</xsl:text>
                     <xsl:value-of select="tr:version"/>
                  </xsl:when>
                  <xsl:otherwise>
<xsl:text disable-output-escaping="yes">&lt;?dbhtml bgcolor="yellow" ?&gt;&lt;?dbfo bgcolor="yellow" ?&gt;</xsl:text>
                     <xsl:value-of select="tr:version"/>
                  </xsl:otherwise>            
               </xsl:choose>           
            </entry>
            <xsl:if test="$type = 'internal'">
               <entry>
                  <xsl:value-of select="tr:executor"/>
               </entry>
            </xsl:if>
            <entry>
               <xsl:apply-templates select="tr:result"/>
            </entry>
         </row>
      </xsl:if>
   </xsl:template>
   
   <xsl:template match="tc:test" mode="untested">
      <xsl:param name="this_shortname" select="tc:shortname"/>
      <xsl:param name="this_id" select="tc:id"/>
      <xsl:if test="not(key('testresult-testcase-group',$this_id)) and not(key('testresult-shortname-group',$this_shortname))">
         <row>
<xsl:text disable-output-escaping="yes">&lt;?dbhtml bgcolor="yellow" ?&gt;&lt;?dbfo bgcolor="yellow" ?&gt;</xsl:text>
            <entry>
               <ulink url="all_testspec.html#{tc:id}">
                  <citetitle><xsl:value-of select="tc:id"/></citetitle>
               </ulink>   
            </entry>
            <entry>
               <xsl:call-template name="makebreak">
                  <xsl:with-param name="text" select="$this_shortname" />
               </xsl:call-template>
            </entry>
            <entry>
               <xsl:for-each select="tc:scrno"><xsl:call-template name="link_to_cms">
                  <xsl:with-param name="issue_id" select="."/>
               </xsl:call-template><xsl:if test="not(position() = last())">, </xsl:if>
               </xsl:for-each>
            </entry>
            <entry>
               no test result
            </entry>
         </row><xsl:text>
         </xsl:text>
      </xsl:if> 
   </xsl:template>   
   
   <xsl:template match="cms:issue" mode="tested">
      <xsl:param name="key_local" select="cms:id"/>
      <xsl:param name="key_local_unmodified" select="cms:id"/>
      <xsl:param name="summary_local" select="cms:summary"/>
      <xsl:param name="status_local" select="cms:state"/>
      
      <!-- if the state of the issue is beyond resolved -->
      <xsl:if test="$status_local = $cms.state.accepted 
                    or $status_local = $cms.state.resolved
                    or $status_local = $cms.state.released
                    or $status_local = $cms.state.closed">
      
         <!-- if there is a test result for this Jira issue without any existing testspec for this Jira issue-->
         <xsl:if test="key('issue-group',$key_local)[(not(../tr:testcase) or ../tr:testcase = '' or not(tr:testcase = //tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]/../tc:id))] and not(//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)])">
            <!-- for each test result for test result for this Jira issue without any existing testspec for this Jira issue-->
            <xsl:for-each select="key('issue-group',$key_local)[(not(../tr:testcase) or ../tr:testcase = '' or not(tr:testcase = //tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]/../tc:id)) and not(//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)])]">
               <row>
                  <entry>
                     <xsl:call-template name="link_to_cms">
                         <xsl:with-param name="issue_id" select="$key_local_unmodified"/>
                     </xsl:call-template><xsl:if test="$type = 'internal'">(<xsl:value-of select="$status_local"/>)</xsl:if>           
                  </entry>
                  <entry>
                     <xsl:choose>
                        <xsl:when test="//cms:issue[cms:id = $key_local]/cms:external-id">
                           <xsl:for-each select="//cms:issue[cms:id = $key_local]/cms:external-id">
                              <xsl:call-template name="link_to_cms">
                                  <xsl:with-param name="issue_id" select="."/>
                              </xsl:call-template>
                           </xsl:for-each>        
                        </xsl:when>
                        <xsl:otherwise>none</xsl:otherwise> 
                     </xsl:choose>   
                  </entry>
                  <entry>
                     <xsl:choose>
                        <xsl:when test="//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]">
                           <xsl:for-each select="//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]">
                              <xsl:variable name="t_id" select="../tc:id"/>
                              <ulink url="all_testspec.html#{$t_id}">
                                 <citetitle><xsl:value-of select="$t_id"/></citetitle>
                              </ulink>
                              <xsl:if test="not(position() = last()) "><xsl:text>,</xsl:text><sbr/></xsl:if>
                           </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="../tr:testcase and not(../tr:testcase = '')">
                           <ulink url="all_testspec.html#{../tr:testcase}">
                              <citetitle><xsl:value-of select="../tr:testcase"/></citetitle>
                           </ulink>
                        </xsl:when>
                        <xsl:otherwise>
                           none
                        </xsl:otherwise>
                     </xsl:choose>
                  </entry>
                  <entry>
                     <xsl:value-of select="../tr:comment"/>
                  </entry>
                  <entry>
                     <xsl:choose>
                        <xsl:when test="../tr:version = $version.releasecandidate">
   <xsl:text disable-output-escaping="yes">
   &lt;?dbhtml bgcolor="Lime" ?&gt;&lt;?dbfo bgcolor="Lime" ?&gt;</xsl:text>
                           <xsl:value-of select="../tr:version"/>
                        </xsl:when>
                        <xsl:otherwise>
   <xsl:text disable-output-escaping="yes">
   &lt;?dbhtml bgcolor="yellow" ?&gt;&lt;?dbfo bgcolor="yellow" ?&gt;</xsl:text>
                           <xsl:value-of select="../tr:version"/>
                        </xsl:otherwise>            
                     </xsl:choose>
                  </entry>
                  <entry>
                     <xsl:choose>
                        <xsl:when test="//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]">
                           <!--
                           TODO: find a way to decide, when to paint entry green, red or yellow.
                           <xsl:choose>
                              <xsl:when test="count(key('issue-group', $key_local)[../testcase = $t_id]/../result[. = passed]) = count(key('issue-group', $key_local)[../testcase = $t_id]/../result) and count(key('issue-group', $key_local)[../testcase = $t_id]/../result) > 0"><xsl:text disable-output-escaping="yes"> &lt;?dbhtml bgcolor="Lime" ?&gt;&lt;?dbfo bgcolor="Lime" ?&gt;</xsl:text></xsl:when>
                              <xsl:when test="count(key('issue-group', $key_local)[../testcase = $t_id]/../result) > count(key('issue-group', $key_local)[../testcase = $t_id]/../result[. = passed]) and count(key('issue-group', $key_local)[../testcase = $t_id]/../result) > 0"><xsl:text disable-output-escaping="yes"> &lt;?dbhtml bgcolor="Yellow" ?&gt;&lt;?dbfo bgcolor="Yellow" ?&gt;</xsl:text></xsl:when>
                           </xsl:choose>
                           -->
   <xsl:text disable-output-escaping="yes">
   &lt;?dbhtml bgcolor="white" ?&gt;&lt;?dbfo bgcolor="white" ?&gt;</xsl:text>
                           <xsl:for-each select="//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]">
                              <xsl:variable name="t_id" select="../tc:id"/>
                              <xsl:choose>
                                 <xsl:when test="key('issue-group', $key_local)[../tr:testcase = $t_id]">
                                    <xsl:apply-templates select="key('issue-group', $key_local)[../tr:testcase = $t_id]/../tr:result"/>
                                 </xsl:when>
                                 <xsl:otherwise>
                                     no test result
                                 </xsl:otherwise>
                              </xsl:choose>
                              <xsl:if test="not(position() = last())"><xsl:text>,</xsl:text><sbr/></xsl:if>
                           </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:apply-templates select="../tr:result"/>
                        </xsl:otherwise>
                     </xsl:choose>   
                  </entry>
               </row><xsl:text>
               </xsl:text>
            </xsl:for-each>
         </xsl:if>
         
         <!-- if there is/are test specification(s) for this Jira issue -->
         <xsl:if test="//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]">
            <!-- for each test specification for this Jira issue -->
            <xsl:for-each select="//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]">
               <xsl:variable name="testcase_id" select="../tc:id"/>
               <xsl:choose>
                  <xsl:when test="key('testresult-testcase-group',$testcase_id)">
                     <xsl:for-each select="key('testresult-testcase-group',$testcase_id)">
                        <row>
                           <entry>
                              <xsl:call-template name="link_to_cms">
                                  <xsl:with-param name="issue_id" select="$key_local_unmodified"/>
                              </xsl:call-template><xsl:if test="$type = 'internal'">(<xsl:value-of select="$status_local"/>)</xsl:if>      
                           </entry>
                           <entry>
                              <xsl:choose>
                                 <xsl:when test="//cms:issue[cms:id = $key_local]/cms:external-id">
                                    <xsl:for-each select="//cms:issue[cms:id = $key_local]/cms:external-id">
                                       <xsl:call-template name="link_to_cms">
                                           <xsl:with-param name="issue_id" select="."/>
                                       </xsl:call-template>
                                    </xsl:for-each>        
                                 </xsl:when>
                                 <xsl:otherwise>none</xsl:otherwise> 
                              </xsl:choose>
                           </entry>
                           <entry>
                              <ulink url="all_testspec.html#{$testcase_id}">
                                 <citetitle><xsl:value-of select="$testcase_id"/></citetitle>
                              </ulink>
                           </entry>
                           <entry>
                              <xsl:value-of select="tr:comment"/>
                           </entry>
                           <entry>
                              <xsl:choose>
                                 <xsl:when test="tr:version = $version.releasecandidate">
                                    <xsl:text disable-output-escaping="yes">
   &lt;?dbhtml bgcolor="Lime" ?&gt;&lt;?dbfo bgcolor="Lime" ?&gt;</xsl:text>
                                    <xsl:value-of select="tr:version"/>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:text disable-output-escaping="yes">
   &lt;?dbhtml bgcolor="yellow" ?&gt;&lt;?dbfo bgcolor="yellow" ?&gt;</xsl:text>
                                    <xsl:value-of select="tr:version"/>
                                 </xsl:otherwise>            
                              </xsl:choose>
                           </entry>
                           <entry>
                              <xsl:apply-templates select="tr:result"/>
                           </entry>
                        </row><xsl:text>
                        </xsl:text>
                     </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                     <row>
                        <xsl:text disable-output-escaping="yes">
   &lt;?dbhtml bgcolor="yellow" ?&gt;&lt;?dbfo bgcolor="yellow" ?&gt;</xsl:text>
                        <entry>
                           <xsl:call-template name="link_to_cms">
                               <xsl:with-param name="issue_id" select="$key_local_unmodified"/>
                           </xsl:call-template><xsl:if test="$type = 'internal'">(<xsl:value-of select="$status_local"/>)</xsl:if>
                        </entry>
                        <entry>
                           <xsl:choose>
                              <xsl:when test="cms:external-id">
                                 <xsl:for-each select="cms:external-id">
                                    <xsl:call-template name="link_to_cms">
                                        <xsl:with-param name="issue_id" select="."/>
                                    </xsl:call-template>
                                 </xsl:for-each>        
                              </xsl:when>
                              <xsl:otherwise>none</xsl:otherwise> 
                           </xsl:choose>
                        </entry>
                        <entry>
                           <ulink url="all_testspec.html#{$testcase_id}">
                              <citetitle><xsl:value-of select="$testcase_id"/></citetitle>
                           </ulink>            
                        </entry>
                        <entry>
                           <xsl:value-of select="$summary_local"/>
                        </entry>
                        <entry>
                        </entry>
                        <entry>
                           no test result
                        </entry>
                     </row><xsl:text>
                     </xsl:text>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:for-each>
         </xsl:if>
         
         <!-- if there is no test result for Jira issue and no test spec -->
         <xsl:if test="not(
                           key('issue-group',$key_local)
                           [
                                 not(../testcase) 
                                 or ../testcase = '' 
                                 or not(
                                    testcase = //scrno
                                       [
                                          contains(. , $key_local) 
                                          or contains(. , $key_local_unmodified)
                                       ]/../id
                                 )
                           ]
                        )
                        and not(
                           //scrno
                           [
                                contains(. , $key_local)
                                or contains(. , $key_local_unmodified)
                           ]
                        )">         
            <row>
               <xsl:text disable-output-escaping="yes">
   &lt;?dbhtml bgcolor="yellow" ?&gt;&lt;?dbfo bgcolor="yellow" ?&gt;</xsl:text>
               <entry>
                  <xsl:call-template name="link_to_cms">
                      <xsl:with-param name="issue_id" select="$key_local_unmodified"/>
                  </xsl:call-template><xsl:if test="$type = 'internal'">(<xsl:value-of select="$status_local"/>)</xsl:if>
               </entry>
               <entry>
                  <xsl:choose>
                     <xsl:when test="//cms:issue[cms:id = $key_local]/cms:external-id">
                        <xsl:for-each select="//cms:issue[cms:id = $key_local]/cms:external-id">
                           <xsl:call-template name="link_to_cms">
                               <xsl:with-param name="issue_id" select="."/>
                           </xsl:call-template>
                        </xsl:for-each>        
                     </xsl:when>
                     <xsl:otherwise>none</xsl:otherwise> 
                  </xsl:choose>
               </entry>
               <entry>
                  <xsl:choose>
                     <xsl:when test="//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]">
                        <xsl:variable name="t_id" select="//tc:scrno[contains(. , $key_local) or contains(. , $key_local_unmodified)]/../id"/>
                        <ulink url="all_testspec.html#{$t_id}">
                           <citetitle><xsl:value-of select="$t_id"/></citetitle>
                        </ulink>
                     </xsl:when>
                     <xsl:otherwise>
                        none
                     </xsl:otherwise>
                  </xsl:choose>            
               </entry>
               <entry>
                  <xsl:value-of select="$summary_local"/>
               </entry>
               <entry></entry>
               <entry>
                  no test result
               </entry>
            </row><xsl:text>
            </xsl:text>
         </xsl:if>
      </xsl:if>
   </xsl:template>
   
   <xsl:template match="uc:usecases" mode="result_stats">
      <!--xsl:param name="tc_passed" select="count(uc:usecase[@id = //test/traceability[//testresult[starts-with(version,$version) and result = 'passed']/testcase = ../id]])"/>
      <xsl:param name="tc_failed" select="count(uc:usecase[@id = //test/traceability[//testresult[starts-with(version,$version) and (result = 'failed' or result = 'blocked')]/testcase = ../id]])"/>
      <xsl:param name="tc_untested" select="count(uc:usecase[@id = //test/traceability[not(key('testresult-testcase-group',../id))]])"/-->
      <xsl:variable name="root_file_name" select="uc:info/@project"/>
      <xsl:variable name="tc_passed" select="count(//tr:testresult[starts-with(tr:version,$version) and tr:result = 'passed' and (tr:testcase = //tc:test[tc:traceability = //uc:usecase[../uc:info/@project = $root_file_name]]/tc:id)])"/>
      <xsl:variable name="tc_failed" select="count(//tr:testresult[starts-with(tr:version,$version) and (tr:result = 'failed' or tr: result = 'blocked') and (tr:testcase = //tc:test[tc:traceability = //uc:usecase[../uc:info/@project = $root_file_name]]/tc:id)])"/>
      <xsl:variable name="tc_untested" select="20"/>
      <para>
         <itemizedlist>
            <listitem>
               <para><emphasis role="bold">Passed: </emphasis><xsl:value-of select="$tc_passed"/></para>
            </listitem>
            <listitem>
               <para><emphasis role="bold">Failed: </emphasis><xsl:value-of select="$tc_failed"/></para>
            </listitem>
            <listitem>
               <para><emphasis role="bold">No Test Result: </emphasis><xsl:value-of select="$tc_untested"/></para>
            </listitem>
         </itemizedlist>
      </para>
      <table frame="none">
         <title><xsl:value-of select="uc:info/@project"/></title>
         <tgroup cols="30" align="left" colsep="0" rowsep="0">
            <colspec colnum="1" colname="c1"/>
            <colspec colnum="1" colname="c2"/>
            <colspec colnum="1" colname="c3"/>
            <tbody>
               <row>
                  <xsl:call-template name="result_dot">
                     <xsl:with-param name="repeat"      select="number(30)"/>
                     <xsl:with-param name="repeat_init" select="number(30)"/>
                     <xsl:with-param name="tc_passed" select="$tc_passed"/>
                     <xsl:with-param name="tc_failed" select="$tc_failed"/>
                     <xsl:with-param name="tc_untested" select="$tc_untested"/>
                  </xsl:call-template>
               </row>
            </tbody>
         </tgroup>
      </table>
   </xsl:template>
   
   <xsl:template name="result_dot">
      <xsl:param name="repeat">0</xsl:param>
      <xsl:param name="repeat_init">0</xsl:param>
      <xsl:param name="tc_passed"/>
      <xsl:param name="tc_failed"/>
      <xsl:param name="tc_untested"/>
      
      <xsl:variable name="sum" select="$tc_passed + $tc_failed + $tc_untested"/>
      <xsl:variable name="quot_passed" select="($tc_passed div $sum) * $repeat_init"/>
      <xsl:variable name="quot_failed" select="($tc_failed div $sum) * $repeat_init"/>
      <xsl:variable name="quot_untested" select="($tc_untested div $sum) * $repeat_init"/>
      
      <xsl:choose>
         <xsl:when test="$repeat > ($quot_passed + $quot_failed)">
            <entry><xsl:text disable-output-escaping="yes">
&lt;?dbhtml bgcolor="grey" ?&gt;&lt;?dbfo bgcolor="grey" ?&gt;</xsl:text>#</entry>
         </xsl:when>
         <xsl:when test="$repeat > $quot_passed">
            <entry><xsl:text disable-output-escaping="yes">
&lt;?dbhtml bgcolor="orangered" ?&gt;&lt;?dbfo bgcolor="orangered" ?&gt;</xsl:text>#</entry>
         </xsl:when>
         <xsl:otherwise>
            <entry><xsl:text disable-output-escaping="yes">
&lt;?dbhtml bgcolor="Lime" ?&gt;&lt;?dbfo bgcolor="Lime" ?&gt;</xsl:text>#</entry>
         </xsl:otherwise>
      </xsl:choose>
      
      <xsl:if test="number($repeat) >= 1">
         <xsl:call-template name="result_dot">
            <xsl:with-param name="repeat" select="$repeat - 1"/>
            <xsl:with-param name="repeat_init" select="$repeat_init"/>
            <xsl:with-param name="tc_passed" select="$tc_passed"/>
            <xsl:with-param name="tc_failed" select="$tc_failed"/>
            <xsl:with-param name="tc_untested" select="$tc_untested"/>
         </xsl:call-template>
     </xsl:if>
   </xsl:template>
   
   <xsl:template match="tr:result">
      <xsl:choose>
          <xsl:when test=". = 'passed'"><xsl:text disable-output-escaping="yes">
&lt;?dbhtml bgcolor="Lime" ?&gt;&lt;?dbfo bgcolor="Lime" ?&gt;</xsl:text><xsl:value-of select="."/></xsl:when>
          <xsl:when test=". = 'failed'"><xsl:text disable-output-escaping="yes">
&lt;?dbhtml bgcolor="orangered" ?&gt;&lt;?dbfo bgcolor="orangered" ?&gt;</xsl:text><xsl:value-of select="."/></xsl:when>
          <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
   <!-- 
       *************
       Test Coverage
       *************
    -->
   <xsl:template match="tc:test">
      <row>
         <entry><ulink url="all_testspec.html#{tc:id}">
                  <citetitle><xsl:value-of select="tc:id"/></citetitle>
         </ulink></entry>
         <entry><xsl:value-of select="tc:shortname"/></entry>
         <entry><xsl:value-of select="tc:traceability"/></entry>
      </row>
   </xsl:template>
    
   <xsl:template match="uc:usecases" mode="simple_coverage">
      <xsl:variable name="cr_id"      select="uc:info/@issue"/>
      <xsl:variable name="cr_version">
         <xsl:choose>
            <xsl:when test="//cms:issue[cms:external-id = $cr_id]/cms:version 
                            and not(//cms:issue[cms:external-id = $cr_id]/cms:version = '')">
               <xsl:value-of select="//cms:issue[cms:external-id = $cr_id]/cms:version"/>
            </xsl:when>
            <xsl:otherwise>unplanned</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <section>
         <title><xsl:value-of select="uc:info/@project"/><xsl:text> </xsl:text>(<xsl:value-of select="uc:info/@issue"/>/<xsl:value-of select="$cr_version"/>)</title>
         <para>
            Simple list of use cases with coverage of specified test cases for issue: 
            <xsl:call-template name="link_to_cms">
                <xsl:with-param name="issue_id" select="uc:info/@issue"/>
            </xsl:call-template>
         </para>
         <xsl:for-each select="//uc:scope[generate-id() = generate-id(key('scope-group', .))]">
            <xsl:variable name="this_scope" select="."/>
            <xsl:if test="//uc:scope[. = $this_scope and ../../uc:info/@issue = $cr_id]">
               <xsl:choose>
                  <xsl:when test="key('usecase-scope-group', $this_scope)">
                     <para>
                        <table frame="all">
                           <title>Use Cases - Simple Coverage (<xsl:value-of select="$this_scope"/>)</title>
                           <tgroup cols="6" align="left" colsep="1" rowsep="1">
                              <colspec colnum="1" colname="c1"/>
                              <colspec colwidth="250pt" colnum="2" colname="c2"/>
                              <colspec colnum="3" colname="c3"/>
                              <colspec colwidth="30pt" colnum="4" colname="c4"/>
                              <colspec colwidth="30pt" colnum="5" colname="c5"/>
                              <colspec colwidth="30pt" colnum="6" colname="c6"/>
                              <thead>
                                 <row>
                                    <entry>UC-ID</entry>
                                    <entry>Description</entry>
                                    <entry>Test Cases</entry>
                                    <entry>Count</entry>
                                    <entry>TC Executed</entry>
                                    <entry>TC Passed</entry>
                                 </row>
                              </thead>
                              <tbody>
                                 <xsl:apply-templates select="key('usecase-issue-group', $cr_id)[uc:scope = $this_scope]" mode="simple_coverage">
                                    <xsl:sort select="@id" order="ascending" data-type="text"/>
                                 </xsl:apply-templates>
                                 <row>
                                    <entry></entry>
                                    <entry></entry>
                                    <entry></entry>
                                    <entry></entry>
                                    <entry></entry>
                                    <entry></entry>
                                 </row>
                              </tbody>
                           </tgroup>
                        </table>
                     </para>
                  </xsl:when>
                  <xsl:otherwise>
                     <para>
                        No use cases found.
                     </para>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:if>
         </xsl:for-each>
      </section>
   </xsl:template>
   
   <xsl:template match="uc:usecase" mode="simple_coverage">
      <xsl:param name="uc_id" select="@id"/>
      <row>
         <xsl:variable name="cover_num" select="count(key('test-group-final',$uc_id))"/>
         <xsl:variable name="cover_num_draft" select="count(key('test-group-draft',$uc_id))"/>
         <xsl:variable name="tc_executed" select="count(key('test-group-final',$uc_id)[key('testresult-testcase-group',normalize-space(tc:id)) or key('testresult-shortname-group',normalize-space(tc:shortname))])"/>
         <xsl:variable name="tc_passed"   select="count(key('test-group-final',$uc_id)[key('testresult-passed-testcase-group',normalize-space(tc:id)) or key('testresult-passed-shortname-group',normalize-space(tc:shortname))])"/>
         <xsl:choose>
            <xsl:when test="$cover_num = 0">
               <xsl:text disable-output-escaping="yes">
&lt;?dbhtml bgcolor="DeepSkyBlue" ?&gt;&lt;?dbfo bgcolor="DeepSkyBlue" ?&gt;</xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:text disable-output-escaping="yes">
&lt;?dbhtml bgcolor="LightSkyBlue " ?&gt;&lt;?dbfo bgcolor="LightSkyBlue "  ?&gt;</xsl:text>
            </xsl:otherwise>
         </xsl:choose>
         <entry>
            <xsl:value-of select="$uc_id"/>            
         </entry>
         <entry>
            <xsl:value-of select="uc:name"/>
         </entry>
         <entry>
            <xsl:for-each select="key('test-group',$uc_id)">
               <ulink url="all_testspec.html#{tc:id}">
                  <citetitle><xsl:if test="tc:state = 'draft'">(</xsl:if>
                  <xsl:value-of select="tc:id"/></citetitle>
                  <xsl:if test="tc:state = 'draft'">)</xsl:if>
               </ulink><xsl:if test="not(position() = last())"><xsl:text>, 
               </xsl:text></xsl:if>
            </xsl:for-each>
         </entry>
         <entry>
            <xsl:value-of select="$cover_num"/>/ (<xsl:value-of select="$cover_num_draft"/>)
         </entry>
         <entry>
            <xsl:choose>
               <xsl:when test="$cover_num > $tc_executed and $cover_num > 0"><xsl:text disable-output-escaping="yes"> &lt;?dbhtml bgcolor="DeepSkyBlue" ?&gt;&lt;?dbfo bgcolor="DeepSkyBlue" ?&gt;</xsl:text></xsl:when>
               <xsl:otherwise><xsl:text disable-output-escaping="yes"> &lt;?dbhtml bgcolor="LightSkyBlue" ?&gt;&lt;?dbfo bgcolor="LightSkyBlue" ?&gt;</xsl:text></xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="$tc_executed"/>
         </entry>
         <entry>
            <xsl:choose>
               <xsl:when test="$tc_executed = 0"><xsl:text disable-output-escaping="yes"> &lt;?dbhtml bgcolor="DeepSkyBlue" ?&gt;&lt;?dbfo bgcolor="DeepSkyBlue" ?&gt;</xsl:text></xsl:when>
               <xsl:when test="$tc_executed > $tc_passed"><xsl:text disable-output-escaping="yes"> &lt;?dbhtml bgcolor="yellow" ?&gt;&lt;?dbfo bgcolor="yellow" ?&gt;</xsl:text></xsl:when>
               <xsl:otherwise><xsl:text disable-output-escaping="yes"> &lt;?dbhtml bgcolor="Lime" ?&gt;&lt;?dbfo bgcolor="Lime" ?&gt;</xsl:text></xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="$tc_passed"/>
         </entry>
      </row><xsl:text>
      </xsl:text>
   </xsl:template>
   
   <xsl:template name="makebreak">
      <xsl:param name="text" />
      <xsl:variable name="textafterbreak" select="substring($text, 21)" />
      <xsl:choose>
        <xsl:when test="string-length($text) &gt; 20">
           <xsl:value-of select="substring($text, 1, 20)" /><xsl:text>
           </xsl:text>
           <xsl:call-template name="makebreak">
              <xsl:with-param name="text" select="$textafterbreak" />
           </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
           <xsl:value-of select="$text" />
        </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
</xsl:stylesheet>