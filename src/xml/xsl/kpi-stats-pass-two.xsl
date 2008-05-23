<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xi="http://www.w3.org/2003/XInclude"  version="1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:uc="uc"
                xmlns:req="req"
                xmlns:tc="http://jcoderz.org/test-specifications"
                xmlns:tr="http://jcoderz.org/test-results"
                xmlns:cms="http://jcoderz.org/generic-cms"
                xmlns:kpi="http://jcoderz.org/key-performance"
                exclude-result-prefixes="xsl xi"
                xsi:schemaLocation="req
                                    http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                                    uc
                                    http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                                    http://jcoderz.org/test-specifications
                                    http://www.jcoderz.org/xsd/xdoc/test-specification-SNAPSHOT.xsd
                                    http://jcoderz.org/test-results
                                    http://www.jcoderz.org/xsd/xdoc/test-results-SNAPSHOT.xsd
                                    http://jcoderz.org/generic-cms
                                    http://www.jcoderz.org/xsd/xdoc/generic-cms-SNAPSHOT.xsd
                                    http://jcoderz.org/key-performance
                                    http://www.jcoderz.org/xsd/xdoc/key-performance-SNAPSHOT.xsd">
   <xsl:output encoding="iso-8859-1" method="xml"/>

   <xsl:include href="libconstants.xsl"/>

   <xsl:param name="lang" select="default"/>
   <xsl:param name="basedir" select="'.'"/>
   <!-- version number of the application in qa reports scope.-->
   <xsl:param name="version"/>
   <xsl:param name="version.releasecandidate"/>
   <xsl:param name="branch"/>
   <!-- wether it is an "internal" or "external" report. -->
   <xsl:param name="type" select="'internal'"/>
   <!-- cruise control timestamp. -->
   <xsl:param name="timestamp" select="'STARTED MANUALLY'"/>
   
   <xsl:key name="usecases-group"   match="uc:usecases" use="."/>
   
   <xsl:key name="test-group"                         match="//tc:test" use="tc:traceability"/>
   <xsl:key name="usecase-group"                      match="//uc:usecase" use="@id"/>
   <xsl:key name="testresult-group"                   match="//tr:testresult[starts-with(tr:version,$version)]" use="."/>
   <xsl:key name="testresult-executor-group"          match="//tr:testresult[tr:version = $version.releasecandidate]" use="tr:executor"/>
   <xsl:key name="testresult-executor-passed-group"   match="//tr:testresult[tr:version = $version.releasecandidate and tr:result = 'passed']" use="tr:executor"/>
   <xsl:key name="testresult-testcase-group"          match="//tr:testresult[starts-with(tr:version,$version)]" use="tr:testcase"/>
   <xsl:key name="testresult-shortname-group"         match="//tr:testresult[starts-with(tr:version,$version)]" use="tr:shortname"/>
   <xsl:key name="testresult-passed-testcase-group"   match="//tr:testresult[starts-with(tr:version,$version) and tr:result = 'passed']" use="tr:testcase"/>
   <xsl:key name="testresult-passed-shortname-group"  match="//tr:testresult[starts-with(tr:version,$version) and tr:result = 'passed']" use="tr:shortname"/>
   
   <xsl:key name="issues-internal-reporter-group"     match="//channel//item[type = $jira.internal.type]" 
                                                      use="reporter/@username"/>
   <xsl:key name="issues-impl-reporter-group"         match="//channel//item[type = $jira.impl.type]" 
                                                      use="reporter/@username"/>
   <xsl:key name="issues-internal-reporter-version-group"     match="//channel//item[type = $jira.internal.type and contains(fixVersion,$version)]" 
                                                              use="reporter/@username"/>
   <xsl:key name="issues-impl-reporter-version-group"         match="//channel//item[type = $jira.impl.type and contains(fixVersion,$version)]" 
                                                              use="reporter/@username"/>
   
   <!-- 
       *******
       Main ;)
       *******
    -->

   <xsl:template match="root">
      <kpi:kpi_list>
         <kpi:meta>
            <kpi:timestamp><xsl:value-of select="$timestamp"/></kpi:timestamp>
            <kpi:version1><xsl:value-of select="$version"/></kpi:version1>
            <kpi:version2><xsl:value-of select="$version.releasecandidate"/></kpi:version2>
            <kpi:version3><xsl:value-of select="$branch"/></kpi:version3>
         </kpi:meta>
         <kpi:entries>
            <xsl:text>
            </xsl:text>
            <kpi:keys>
               <xsl:call-template name="kpikeys"/>
            </kpi:keys>
            <xsl:text>
            </xsl:text>
            <xsl:call-template name="testcases"/>
            <xsl:text>
            </xsl:text>
            <xsl:call-template name="jira"/>
            <xsl:text>
            </xsl:text>
            <xsl:call-template name="efficiency"/>
            <xsl:text>
            </xsl:text>
            <xsl:call-template name="efficiency_release"/>
         </kpi:entries>
      </kpi:kpi_list>
   </xsl:template>
   
   <!-- 
       ******
       Header
       ******
    -->
    
   <xsl:template name="kpikeys">
      <kpi:key><xsl:value-of select="$kpi.testcases.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.automated.jmeter.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.automated.selenium.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.automated.jmeter.passed.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.automated.selenium.passed.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.executed.version.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.executed.passed.version.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.executed.version.rc.number"/></kpi:key><xsl:text></xsl:text>
      
      <kpi:key><xsl:value-of select="$kpi.jira.issue.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.bugs.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.cr.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.testing.st.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.impl.st.number"/></kpi:key><xsl:text></xsl:text>      
      <kpi:key><xsl:value-of select="$kpi.jira.issue.bugs.open.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.cr.open.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.open.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.created.qa.number"/></kpi:key><xsl:text></xsl:text>
      
      <kpi:key><xsl:value-of select="$kpi.jira.issue.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.bugs.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.cr.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.bugs.open.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.cr.open.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.open.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.created.qa.number.version"/></kpi:key><xsl:text></xsl:text>
      
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.resolved_or_accepted.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.accepted.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.bugs.resolved_or_accepted.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.bugs.accepted.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.crs.resolved_or_accepted.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.crs.accepted.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.resolved_or_accepted.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.accepted.number.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.found.number.affected.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.found.number.affected.version"/></kpi:key><xsl:text></xsl:text>
      
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.testcamp.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.testcamp.number.planned"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.testcamp.number.resolved"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.testcamp.number.progress"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.testcamp.number.to_be_verified"/></kpi:key><xsl:text></xsl:text>
      
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.resolved_or_accepted.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.accepted.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.bugs.resolved_or_accepted.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.bugs.accepted.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.bugs.unscheduled"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.crs.resolved_or_accepted.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.crs.accepted.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.resolved_or_accepted.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.accepted.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.found.number.affected"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.found.number.affected"/></kpi:key><xsl:text></xsl:text>

      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.bug.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.bug"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.refactoring.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.refactoring"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.task.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.task"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.bug.open.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.bug.resolved.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.bug.open"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.refactoring.open.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.refactoring.open"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.task.open.version"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.internal.class.task.open"/></kpi:key><xsl:text></xsl:text>
      
      <kpi:key><xsl:value-of select="$kpi.testcases.priority.high.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.priority.medium.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.priority.low.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testcases.issues.covered.number"/></kpi:key><xsl:text></xsl:text>
            
      <kpi:key><xsl:value-of select="$kpi.req.main.spec.usecase.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.req.main.spec.usecase.covered.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.req.all.spec.usecase.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.req.all.spec.usecase.covered.number"/></kpi:key><xsl:text></xsl:text>
      
      <kpi:key><xsl:value-of select="$kpi.testresults.number.release"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.issues.number.release"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.testcases.number.release"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.time.minutes.release"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.time.issues.minutes.release"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.time.testcases.minutes.release"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.time.average.minutes.release"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.time.issues.average.minutes.release"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.time.testcases.average.minutes.release"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.time.issues.minutes"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.issues.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.time.testcases.minutes"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.testresults.testcases.number"/></kpi:key><xsl:text></xsl:text>
      
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.bugs.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.bugs.verifikation.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.bugs.verified.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.bugs.inprogress.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.bugs.resolved.number"/></kpi:key><xsl:text></xsl:text>
      <kpi:key><xsl:value-of select="$kpi.jira.issue.external.bugs.released.number"/></kpi:key><xsl:text></xsl:text>      
   </xsl:template>
   
   <!-- 
       *********************
       Statistical templates
       ********************* 
    -->
   <xsl:template name="jira">
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.number"/>
         <xsl:with-param name="value" select="count(//cms:issue)"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.bugs.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.cr.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.cr.type])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.testing.st.number"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.testing.type])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.impl.st.number"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.impl.type])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.bugs.open.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and not(cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.cr.open.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.cr.type and not(cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.open.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id) and not(cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.created.qa.number"/>
         <xsl:with-param name="value" select="count(//item)"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.impl.created.qa.number"/>
         <xsl:with-param name="value" select="count(//item)"/>
      </xsl:call-template>
      
      <!-- Release depending issue number counts. -->
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.bugs.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.cr.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.cr.type and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.bugs.open.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and not(cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.cr.open.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.cr.type and not(cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.open.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id) and not(cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.created.qa.number.version"/>
         <xsl:with-param name="value" select="count(//item)"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.impl.created.qa.number.version"/>
         <xsl:with-param name="value" select="count(//item)"/>
      </xsl:call-template>
      
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.resolved_or_accepted.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.cr.type or cms:type = $cms.bug.type) and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.accepted.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.cr.type or cms:type = $cms.bug.type) and (cms:state = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.bugs.resolved_or_accepted.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.bug.type) and (cms:state = $cms.state.resolved or cms:status = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.bugs.accepted.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.bug.type) and (cms:state = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.crs.resolved_or_accepted.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.cr.type) and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.crs.accepted.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.cr.type) and (cms:state = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.resolved_or_accepted.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id) and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.accepted.number.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id) and (cms:state = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.found.number.affected.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id) and contains(cms:affects-version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.found.number.affected.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and contains(cms:affects-version,$version)])"/>
      </xsl:call-template>
      
      
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.resolved_or_accepted.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.cr.type or type = $cms.bug.type) and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.accepted.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.cr.type or type = $cms.bug.type) and (cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.bugs.resolved_or_accepted.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.bugs.unscheduled"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.bug.type) and (cms:version = '' or not(cms:version) and not(cms:state = $cms.state.closed))])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.bugs.accepted.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.bug.type) and (cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.crs.resolved_or_accepted.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.cr.type) and (cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.crs.accepted.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[(cms:type = $cms.cr.type) and (cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.resolved_or_accepted.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id) and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.accepted.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id) and (status = 'Accepted' or status = 'Closed')])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.found.number.affected"/>
         <xsl:with-param name="value" select="count(//cms:issue[not(cms:external-id) and cms:affects-version = $version])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.found.number.affected"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and cms:affects-version = $version])"/>
      </xsl:call-template>      
      
      
      <!-- Internal Bugs found with for a release (affected versions)  -->
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.bug.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and not(cms:external-id) and contains(cms:affects-version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.bug"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and not(cms:external-id)])"/>
      </xsl:call-template>      
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.refactoring.version"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.internal.type and contains(fixVersion,$version) and customfields/customfield[customfieldname='Classification']/customfieldvalues[customfieldvalue = 'Refactoring']])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.refactoring"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.internal.type and customfields/customfield[customfieldname='Classification']/customfieldvalues[customfieldvalue = 'Refactoring']])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.task.version"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.internal.type and contains(fixVersion,$version) and customfields/customfield[customfieldname='Classification']/customfieldvalues[customfieldvalue = 'Task']])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.task"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.internal.type and customfields/customfield[customfieldname='Classification']/customfieldvalues[customfieldvalue = 'Task']])"/>
      </xsl:call-template>
      
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.bug.open.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and not(cms:external-id) and not(cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.bug.resolved.version"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and not(cms:external-id) and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed) and contains(cms:affects-version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.bug.open"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and not(cms:external-id) and not(cms:state = $cms.state.accepted or cms:state = $cms.state.closed) ])"/>
      </xsl:call-template>      
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.refactoring.open.version"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.internal.type and not(status = 'Accepted' or status = 'Closed') and contains(fixVersion,$version) and customfields/customfield[customfieldname='Classification']/customfieldvalues[customfieldvalue = 'Refactoring']])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.refactoring.open"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.internal.type and not(status = 'Accepted' or status = 'Closed') and customfields/customfield[customfieldname='Classification']/customfieldvalues[customfieldvalue = 'Refactoring']])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.task.open.version"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.internal.type and not(status = 'Accepted' or status = 'Closed') and contains(fixVersion,$version) and customfields/customfield[customfieldname='Classification']/customfieldvalues[customfieldvalue = 'Task']])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.task.open"/>
         <xsl:with-param name="value" select="count(//item[type = $jira.internal.type and not(status = 'Accepted' or status = 'Closed') and customfields/customfield[customfieldname='Classification']/customfieldvalues[customfieldvalue = 'Task']])"/>
      </xsl:call-template>
      
      <!-- external Jira issues -->
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.bugs.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and cms:external-id])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.bugs.verifikation.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and cms:external-id and cms:state = $cms.state.reported])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.bugs.verified.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and cms:external-id and cms:state = $cms.state.verified])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.bugs.inprogress.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and cms:external-id and cms:state = $cms.state.open])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.bugs.resolved.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and cms:external-id and cms:state = $cms.state.resolved])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.jira.issue.external.bugs.released.number"/>
         <xsl:with-param name="value" select="count(//cms:issue[cms:type = $cms.bug.type and cms:external-id and (cms:state = $cms.state.accepted or cms:state = $cms.state.released)])"/>
      </xsl:call-template>      
   </xsl:template>
          
   <xsl:template name="testcases">
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.number"/>
         <xsl:with-param name="value" select="count(//tc:test)"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.automated.jmeter.number"/>
         <xsl:with-param name="value" select="count(key('testresult-executor-group','JMeter'))"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.automated.jmeter.passed.number"/>
         <xsl:with-param name="value" select="count(key('testresult-executor-passed-group','JMeter'))"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.automated.selenium.number"/>
         <xsl:with-param name="value" select="count(key('testresult-executor-group','Selenium'))"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.automated.selenium.passed.number"/>
         <xsl:with-param name="value" select="count(key('testresult-executor-passed-group','Selenium'))"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.executed.version.number"/>
         <xsl:with-param name="value" select="count(//tr:testresult[starts-with(tr:version,$version)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.executed.passed.version.number"/>
         <xsl:with-param name="value" select="count(//tr:testresult[starts-with(tr:version,$version) and tr:result = 'passed'])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.executed.version.rc.number"/>
         <xsl:with-param name="value" select="count(//tr:testresult[tr:version = $version.releasecandidate])"/>
      </xsl:call-template>      
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.automated.executed.version.rc.number"/>
         <xsl:with-param name="value" select="count(//tr:testresult[tr:version = $version.releasecandidate and tr:executor = 'JMeter'])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.automated.executed.version.rc.passed.number"/>
         <xsl:with-param name="value" select="count(//tr:testresult[tr:version = $version.releasecandidate and tr:executor = 'JMeter' and tr:result = 'passed'])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.issue.executed.version.number"/>
         <xsl:with-param name="value" select="count(//tr:testresult[starts-with(tr:version,$version) and not(tr:executor = 'JMeter')])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.issue.executed.version.passed.number"/>
         <xsl:with-param name="value" select="count(//tr:testresult[starts-with(tr:version,$version) and not(tr:executor = 'JMeter') and tr:result = 'passed'])"/>
      </xsl:call-template>

      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.priority.high.number"/>
         <xsl:with-param name="value" select="count(//tc:test[tc:priority = $priority.high])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.priority.medium.number"/>
         <xsl:with-param name="value" select="count(//tc:test[tc:priority = $priority.medium])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.priority.low.number"/>
         <xsl:with-param name="value" select="count(//tc:test[tc:priority = $priority.low])"/>
      </xsl:call-template>
      
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testcases.issues.covered.number"/>
         <xsl:with-param name="value" select="count(//tc:scrno[not(.='') and not(.='none')])"/>
      </xsl:call-template>
      
      
      <!-- Test Coverage -->
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.req.main.spec.usecase.number"/>
         <xsl:with-param name="value" select="count(//uc:usecase[../uc:info/@project = 'TACO Specification'])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.req.main.spec.usecase.covered.number"/>
         <xsl:with-param name="value" select="count(//uc:usecase[../uc:info/@project = 'TACO Specification' and key('test-group',@id)])"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.req.all.spec.usecase.number"/>
         <xsl:with-param name="value" select="count(//uc:usecase)"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.req.all.spec.usecase.covered.number"/>
         <xsl:with-param name="value" select="count(//uc:usecase[key('test-group',@id)])"/>
      </xsl:call-template>
   </xsl:template>  
   
   <xsl:template name="efficiency">
      <xsl:variable name="tr_at_all"           select="count(//tr:testresult[number(tr:time) = number(tr:time)])"/>
      <xsl:variable name="tr_issue_at_all"     select="count(//tr:testresult[tr:issue and not(tr:issue = '') and number(tr:time) = number(tr:time)])"/>
      <xsl:variable name="tr_test_at_all"      select="count(//tr:testresult[tr:testcase and not(tr:testcase = '') and number(tr:time) = number(tr:time)])"/>
      <xsl:variable name="tr_time_spent"       select="sum(//tr:testresult/tr:time[number(.)=number(.)])"/>
      <xsl:variable name="tr_issue_time_spent" select="sum(//tr:testresult[tr:issue and not(tr:issue = '')]/tr:time[number(.) = number(.)])"/>
      <xsl:variable name="tr_test_time_spent"  select="sum(//tr:testresult[tr:testcase and not(tr:testcase = '')]/tr:time[number(.) = number(.)])"/>
      <xsl:variable name="tr_avg_time"         select="$tr_time_spent div $tr_at_all"/>
      <xsl:variable name="tr_issue_avg_time"   select="$tr_issue_time_spent div $tr_issue_at_all"/>
      <xsl:variable name="tr_test_avg_time"    select="$tr_test_time_spent div $tr_test_at_all"/>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.number"/>
         <xsl:with-param name="value" select="$tr_at_all"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.issues.number"/>
         <xsl:with-param name="value" select="$tr_issue_at_all"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.testcases.number"/>
         <xsl:with-param name="value" select="$tr_test_at_all"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.minutes"/>
         <xsl:with-param name="value" select="$tr_time_spent"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.issues.minutes"/>
         <xsl:with-param name="value" select="$tr_issue_time_spent"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.testcases.minutes"/>
         <xsl:with-param name="value" select="$tr_test_time_spent"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.average.minutes"/>
         <xsl:with-param name="value" select="$tr_avg_time"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.issues.average.minutes"/>
         <xsl:with-param name="value" select="$tr_issue_avg_time"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.testcases.average.minutes"/>
         <xsl:with-param name="value" select="$tr_test_avg_time"/>
      </xsl:call-template>
   </xsl:template>
   
   <xsl:template name="efficiency_release">
      <xsl:variable name="tr_at_all"           select="count(//tr:testresult[starts-with(tr:version,$version) and number(tr:time) = number(tr:time)])"/>
      <xsl:variable name="tr_issue_at_all"     select="count(//tr:testresult[starts-with(tr:version,$version) and tr:issue and not(tr:issue = '') and number(tr:time) = number(tr:time)])"/>
      <xsl:variable name="tr_test_at_all"      select="count(//tr:testresult[starts-with(tr:version,$version) and tr:testcase and not(tr:testcase = '') and number(tr:time) = number(tr:time)])"/>
      <xsl:variable name="tr_time_spent"       select="sum(//tr:testresult[starts-with(tr:version,$version)]/tr:time[number(.)=number(.)])"/>
      <xsl:variable name="tr_issue_time_spent" select="sum(//tr:testresult[starts-with(tr:version,$version) and tr:issue and not(tr:issue = '')]/tr:time[number(.) = number(.)])"/>
      <xsl:variable name="tr_test_time_spent"  select="sum(//tr:testresult[starts-with(tr:version,$version) and tr:testcase and not(tr:testcase = '')]/tr:time[number(.) = number(.)])"/>
      <xsl:variable name="tr_avg_time"         select="$tr_time_spent div $tr_at_all"/>
      <xsl:variable name="tr_issue_avg_time"   select="$tr_issue_time_spent div $tr_issue_at_all"/>
      <xsl:variable name="tr_test_avg_time"    select="$tr_issue_time_spent div $tr_test_at_all"/>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.number.release"/>
         <xsl:with-param name="value" select="$tr_at_all"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.issues.number.release"/>
         <xsl:with-param name="value" select="$tr_issue_at_all"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.testcases.number.release"/>
         <xsl:with-param name="value" select="$tr_test_at_all"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.minutes.release"/>
         <xsl:with-param name="value" select="$tr_time_spent"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.issues.minutes.release"/>
         <xsl:with-param name="value" select="$tr_issue_time_spent"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.testcases.minutes.release"/>
         <xsl:with-param name="value" select="$tr_test_time_spent"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.average.minutes.release"/>
         <xsl:with-param name="value" select="$tr_avg_time"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.issues.average.minutes.release"/>
         <xsl:with-param name="value" select="$tr_issue_avg_time"/>
      </xsl:call-template>
      <xsl:call-template name="entry">
         <xsl:with-param name="key" select="$kpi.testresults.time.testcases.average.minutes.release"/>
         <xsl:with-param name="value" select="$tr_test_avg_time"/>
      </xsl:call-template>
   </xsl:template>
         
   <!-- 
       ****************
       Helper templates
       ****************
    -->
    
   <xsl:template name="entry">
      <xsl:param name="key"/>
      <xsl:param name="value"/>
      <kpi:entry>
         <kpi:key><xsl:value-of select="$key"/></kpi:key>
         <kpi:value><xsl:value-of select="$value"/></kpi:value>
      </kpi:entry>
      <xsl:text>
      </xsl:text>
   </xsl:template>
   
   <xsl:template name="lookup_testcase_id">
      <xsl:param name="shortname"/>
      <xsl:choose>
          <xsl:when test="//test[shortname = $shortname]"><xsl:for-each select="//test[shortname = $shortname]"><xsl:value-of select="id"/></xsl:for-each></xsl:when>
          <xsl:otherwise>STEPS</xsl:otherwise>
      </xsl:choose>
   </xsl:template>
</xsl:stylesheet>