<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xi="http://www.w3.org/2003/XInclude"  version="1.0"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:uc="uc"
                xmlns:req="req"
                xmlns:kpi="http://jcoderz.org/key-performance"
                xsi:schemaLocation="req
                                http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd
                                uc
                                http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                                http://jcoderz.org/key-performance
                                http://www.jcoderz.org/xsd/xdoc/key-performance-SNAPSHOT.xsd">
   <xsl:output encoding="iso-8859-1" method="xml"/>
   <!-- 
       **********
       priorities
       **********
    -->
    <xsl:variable name="priority.high"         select="'High'"/>
    <xsl:variable name="priority.medium"       select="'Medium'"/>
    <xsl:variable name="priority.low"          select="'Low'"/>
    
   <!-- 
       ************
       module names
       ************
    -->
    
   <!-- constants -->

   
   <!-- 
       ***************
       JIRA
       ***************
    -->
    
    <xsl:variable name="cms.bug.type"          select="'Bug'"/>
    <xsl:variable name="cms.cr.type"           select="'Change Request'"/>
    <xsl:variable name="cms.task.type"         select="'Task'"/>
   
    <xsl:variable name="cms.state.draft"       select="'Draft'"/>
    <xsl:variable name="cms.state.reported"    select="'Reported'"/>
    <xsl:variable name="cms.state.verified"    select="'Verified'"/>
    <xsl:variable name="cms.state.open"        select="'Open'"/>
    <xsl:variable name="cms.state.accepted"    select="'Accepted'"/>
    <xsl:variable name="cms.state.resolved"    select="'Resolved'"/>
    <xsl:variable name="cms.state.released"    select="'Released'"/>
    <xsl:variable name="cms.state.closed"      select="'Closed'"/>
   
    <xsl:variable name="jira.bug.type"          select="'Bug Fix'"/>
    <xsl:variable name="jira.cr.type"           select="'Change Request'"/>
    <xsl:variable name="jira.internal.type"     select="'Internal Issue'"/>
    <xsl:variable name="jira.testing.type"      select="'Testing Sub-Task'"/>
    <xsl:variable name="jira.impl.type"         select="'Implementation Sub-Task'"/>
    <xsl:variable name="jira.cr.external.type"  select="'External Change Request'"/>
    <xsl:variable name="jira.bug.external.type" select="'External Bug Fix Request'"/>
    
   <!-- 
       ***************
       KPI DEFINITIONS
       ***************
    -->
   
   <!--  *************** TESTCASES ***************-->
   <!-- number of specified test cases -->
   <xsl:variable name="kpi.testcases.number" select="'KPI_NUMBER_TESTCASES'"/>
   <!-- number of automated test cases -->
   <xsl:variable name="kpi.testcases.automated.jmeter.number" select="'KPI_NUMBER_AUTOMATED_JMETER_TESTCASES'"/>
   <!-- number of automated jmeter test cases -->
   <xsl:variable name="kpi.testcases.automated.jmeter.passed.number" select="'KPI_NUMBER_AUTOMATED_JMETER_PASSED_TESTCASES'"/>
   <!-- number of automated test cases -->
   <xsl:variable name="kpi.testcases.automated.selenium.number" select="'KPI_NUMBER_AUTOMATED_SELENIUM_TESTCASES'"/>
   <!-- number of passed selenium test cases -->
   <xsl:variable name="kpi.testcases.automated.selenium.passed.number" select="'KPI_NUMBER_AUTOMATED_SELENIUM_PASSED_TESTCASES'"/>
   <!-- number of executed test cases for the dc version -->
   <xsl:variable name="kpi.testcases.executed.version.number" select="'KPI_NUMBER_EXECUTED_TESTCASES'"/>
   <!-- number of executed test cases for the dc version that passed-->
   <xsl:variable name="kpi.testcases.executed.passed.version.number" select="'KPI_NUMBER_EXECUTED_PASSED_TESTCASES'"/>
   <!-- number of executed test cases for the release candidate version -->
   <xsl:variable name="kpi.testcases.executed.version.rc.number" select="'KPI_NUMBER_EXECUTED_RC_TESTCASES'"/>
   <!-- number of executed test automated test cases for the release candidate version -->
   <xsl:variable name="kpi.testcases.automated.executed.version.rc.number" select="'KPI_NUMBER_EXECUTED_AUTOMATED_RC_TESTCASES'"/>
   <!-- number of executed test automated test cases for the release candidate version with result passed -->
   <xsl:variable name="kpi.testcases.automated.executed.version.rc.passed.number" select="'KPI_NUMBER_EXECUTED_AUTOMATED_RC_PASSED_TESTCASES'"/>
   <!-- number of executed issue driven tests for the version -->
   <xsl:variable name="kpi.testcases.issue.executed.version.number" select="'KPI_NUMBER_EXECUTED_MANUAL_TESTCASES'"/>
   <!-- number of executed issue driven tests for the version with result passed -->
   <xsl:variable name="kpi.testcases.issue.executed.version.passed.number" select="'KPI_NUMBER_EXECUTED_MANUAL_PASSED_TESTCASES'"/>
   
   <!-- number of specified test cases modules-->
   <xsl:variable name="kpi.testcases.number.module."        select="'KPI_NUMBER_TESTCASES_MODULE_'"/>
   
   <xsl:variable name="kpi.testcases.priority.high.number"   select="'KPI_NUMBER_TESTCASES_PRIORITY_HIGH'"/>
   <xsl:variable name="kpi.testcases.priority.medium.number" select="'KPI_NUMBER_TESTCASES_PRIORITY_MEDIUM'"/>
   <xsl:variable name="kpi.testcases.priority.low.number"    select="'KPI_NUMBER_TESTCASES_PRIORITY_LOW'"/>
   <xsl:variable name="kpi.testcases.issues.covered.number"  select="'KPI_NUMBER_TESTCASES_ISSUES_COVERED'"/>
   
   <!--  *************** TIME EFFICIENCY ALL RELEASES ***************-->
   <!-- number test results -->
   <xsl:variable name="kpi.testresults.number" select="'KPI_NUMBER_TESTRESULTS'"/>
   <!-- number of issue based test results -->
   <xsl:variable name="kpi.testresults.issues.number" select="'KPI_NUMBER_ISSUES_TESTRESULTS'"/>
   <!-- number of specified test case results -->
   <xsl:variable name="kpi.testresults.testcases.number" select="'KPI_NUMBER_TESTCASES_TESTRESULTS'"/>
   <!-- minutes spent for testing in total -->
   <xsl:variable name="kpi.testresults.time.minutes" select="'KPI_MINUTES_TESTRESULTS'"/>
   <!-- minutes for issue testing spent in total -->
   <xsl:variable name="kpi.testresults.time.issues.minutes" select="'KPI_MINUTES_ISSUES_TESTRESULTS'"/>
   <!-- minutes spent for test specification testing in total -->
   <xsl:variable name="kpi.testresults.time.testcases.minutes" select="'KPI_MINUTES_TESTCASES_TESTRESULTS'"/>
   <!-- minutes spent for testing in total average -->
   <xsl:variable name="kpi.testresults.time.average.minutes" select="'KPI_MINUTES_AVERAGE_TESTRESULTS'"/>
   <!-- minutes for issue testing spent average -->
   <xsl:variable name="kpi.testresults.time.issues.average.minutes" select="'KPI_MINUTES_ISSUES_AVERAGE_TESTRESULTS'"/>
   <!-- minutes spent for test specification testing average -->
   <xsl:variable name="kpi.testresults.time.testcases.average.minutes" select="'KPI_MINUTES_TESTCASES_AVERAGE_TESTRESULTS'"/>
   
   <!--  *************** TIME EFFICIENCY ACTUAL RELEASES ***************-->
   <!-- number test results -->
   <xsl:variable name="kpi.testresults.number.release" select="'KPI_NUMBER_TESTRESULTS_RELEASE'"/>
   <!-- number of issue based test results -->
   <xsl:variable name="kpi.testresults.issues.number.release" select="'KPI_NUMBER_ISSUES_TESTRESULTS_RELEASE'"/>
   <!-- number of specified test case results -->
   <xsl:variable name="kpi.testresults.testcases.number.release" select="'KPI_NUMBER_TESTCASES_TESTRESULTS_RELEASE'"/>
   <!-- minutes spent for testing in total -->
   <xsl:variable name="kpi.testresults.time.minutes.release" select="'KPI_MINUTES_TESTRESULTS_RELEASE'"/>
   <!-- minutes for issue testing spent in total -->
   <xsl:variable name="kpi.testresults.time.issues.minutes.release" select="'KPI_MINUTES_ISSUES_TESTRESULTS_RELEASE'"/>
   <!-- minutes spent for test specification testing in total -->
   <xsl:variable name="kpi.testresults.time.testcases.minutes.release" select="'KPI_MINUTES_TESTCASES_TESTRESULTS_RELEASE'"/>
   <!-- minutes spent for testing in total average -->
   <xsl:variable name="kpi.testresults.time.average.minutes.release" select="'KPI_MINUTES_AVERAGE_TESTRESULTS_RELEASE'"/>
   <!-- minutes for issue testing spent average -->
   <xsl:variable name="kpi.testresults.time.issues.average.minutes.release" select="'KPI_MINUTES_ISSUES_AVERAGE_TESTRESULTS_RELEASE'"/>
   <!-- minutes spent for test specification testing average -->
   <xsl:variable name="kpi.testresults.time.testcases.average.minutes.release" select="'KPI_MINUTES_TESTCASES_AVERAGE_TESTRESULTS_RELEASE'"/>
   
   <!--  *************** JIRA ***************-->
   <!-- number of jira issues -->
   <xsl:variable name="kpi.jira.issue.number" select="'KPI_JIRA_ISSUE_NUMBER'"/>
   <!-- number of jira bug issues -->
   <xsl:variable name="kpi.jira.issue.bugs.number" select="'KPI_JIRA_ISSUE_BUG_NUMBER'"/>
   <!-- number of jira cr issues -->
   <xsl:variable name="kpi.jira.issue.cr.number" select="'KPI_JIRA_ISSUE_CR_NUMBER'"/>
   <!-- number of jira internal issues -->
   <xsl:variable name="kpi.jira.issue.internal.number" select="'KPI_JIRA_ISSUE_INTERNAL_NUMBER'"/>
   <!-- number of jira testing sub-tasks -->
   <xsl:variable name="kpi.jira.issue.testing.st.number" select="'KPI_JIRA_ISSUE_TESTING_NUMBER'"/>
   <!-- number of jira implementation sub-tasks -->
   <xsl:variable name="kpi.jira.issue.impl.st.number" select="'KPI_JIRA_ISSUE_IMPLEMENTATION_NUMBER'"/>
   <!-- number of jira unclosed/unresolved bug issues -->
   <xsl:variable name="kpi.jira.issue.bugs.open.number" select="'KPI_JIRA_ISSUE_BUG_OPEN_NUMBER'"/>
   <!-- number of jira unclosed/unresolved cr issues -->
   <xsl:variable name="kpi.jira.issue.cr.open.number" select="'KPI_JIRA_ISSUE_CR_OPEN_NUMBER'"/>
   <!-- number of jira unclosed/unresolved internal issues -->
   <xsl:variable name="kpi.jira.issue.internal.open.number" select="'KPI_JIRA_ISSUE_INTERNAL_OPEN_NUMBER'"/>
   <!-- number of jira issues created by QA -->
   <xsl:variable name="kpi.jira.issue.internal.created.qa.number" select="'KPI_JIRA_ISSUE_INTERNAL_QA_CREATED_NUMBER'"/>
   <!-- number of jira issues created by QA -->
   <xsl:variable name="kpi.jira.issue.impl.created.qa.number" select="'KPI_JIRA_ISSUE_IMPL_QA_CREATED_NUMBER'"/>
   
   <xsl:variable name="kpi.jira.issue.number.version" select="'KPI_JIRA_ISSUE_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.bugs.number.version" select="'KPI_JIRA_ISSUE_BUG_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.cr.number.version" select="'KPI_JIRA_ISSUE_CR_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.number.version" select="'KPI_JIRA_ISSUE_INTERNAL_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.bugs.open.number.version" select="'KPI_JIRA_ISSUE_BUG_OPEN_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.cr.open.number.version" select="'KPI_JIRA_ISSUE_CR_OPEN_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.open.number.version" select="'KPI_JIRA_ISSUE_INTERNAL_OPEN_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.created.qa.number.version" select="'KPI_JIRA_ISSUE_INTERNAL_QA_CREATED_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.impl.created.qa.number.version" select="'KPI_JIRA_ISSUE_IMPL_QA_CREATED_NUMBER_VERSION'"/>
   
   <xsl:variable name="kpi.jira.issue.internal.testcamp.number" select="'KPI_JIRA_ISSUE_INTERNAL_TESTCAMP_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.internal.testcamp.number.planned" select="'KPI_JIRA_ISSUE_INTERNAL_TESTCAMP_NUMBER_PLANNED'"/>
   <xsl:variable name="kpi.jira.issue.internal.testcamp.number.resolved" select="'KPI_JIRA_ISSUE_INTERNAL_TESTCAMP_NUMBER_RESOLVED'"/>
   <xsl:variable name="kpi.jira.issue.internal.testcamp.number.progress" select="'KPI_JIRA_ISSUE_INTERNAL_TESTCAMP_NUMBER_PROGRESSED'"/>
   <xsl:variable name="kpi.jira.issue.internal.testcamp.number.to_be_verified" select="'KPI_JIRA_ISSUE_INTERNAL_TESTCAMP_NUMBER_TO_BE_VERIFIED'"/>
   
   <xsl:variable name="kpi.jira.issue.external.resolved_or_accepted.number.version" select="'KPI_JIRA_ISSUE_EXTERNAL_RESOLVED_ACCEPTED_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.external.accepted.number.version" select="'KPI_JIRA_ISSUE_EXTERNAL_ACCEPTED_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.bugs.resolved_or_accepted.number.version" select="'KPI_JIRA_ISSUE_BUGS_RESOLVED_ACCEPTED_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.bugs.accepted.number.version" select="'KPI_JIRA_ISSUE_BUGS_ACCEPTED_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.crs.resolved_or_accepted.number.version" select="'KPI_JIRA_ISSUE_CRS_RESOLVED_ACCEPTED_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.crs.accepted.number.version" select="'KPI_JIRA_ISSUE_CRS_ACCEPTED_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.resolved_or_accepted.number.version" select="'KPI_JIRA_ISSUE_INTERNAL_RESOLVED_ACCEPTED_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.accepted.number.version" select="'KPI_JIRA_ISSUE_INTERNAL_ACCEPTED_NUMBER_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.found.number.affected.version" select="'KPI_JIRA_ISSUE_INTERNAL_FOUND_NUMBER_AFFECTED_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.external.found.number.affected.version" select="'KPI_JIRA_ISSUE_EXTERNAL_FOUND_NUMBER_AFFECTED_VERSION'"/>
   
   <xsl:variable name="kpi.jira.issue.external.resolved_or_accepted.number" select="'KPI_JIRA_ISSUE_EXTERNAL_RESOLVED_ACCEPTED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.external.accepted.number" select="'KPI_JIRA_ISSUE_EXTERNAL_ACCEPTED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.bugs.resolved_or_accepted.number" select="'KPI_JIRA_ISSUE_BUGS_RESOLVED_ACCEPTED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.bugs.unscheduled" select="'KPI_JIRA_ISSUE_BUGS_UNSCHEDULED'"/>
   <xsl:variable name="kpi.jira.issue.bugs.accepted.number" select="'KPI_JIRA_ISSUE_BUGS_ACCEPTED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.crs.resolved_or_accepted.number" select="'KPI_JIRA_ISSUE_CRS_RESOLVED_ACCEPTED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.crs.accepted.number" select="'KPI_JIRA_ISSUE_CRS_ACCEPTED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.internal.resolved_or_accepted.number" select="'KPI_JIRA_ISSUE_INTERNAL_RESOLVED_ACCEPTED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.internal.accepted.number" select="'KPI_JIRA_ISSUE_INTERNAL_ACCEPTED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.internal.found.number.affected" select="'KPI_JIRA_ISSUE_INTERNAL_FOUND_NUMBER_AFFECTED'"/>
   <xsl:variable name="kpi.jira.issue.external.found.number.affected" select="'KPI_JIRA_ISSUE_EXTERNAL_FOUND_NUMBER_AFFECTED'"/>
   
   <xsl:variable name="kpi.jira.issue.internal.class.bug.version" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_BUG_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.class.bug" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_BUG'"/>   
   <xsl:variable name="kpi.jira.issue.internal.class.refactoring.version" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_REFACTORING_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.class.refactoring" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_REFACTORING'"/>
   <xsl:variable name="kpi.jira.issue.internal.class.task.version" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_TASK_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.class.task" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_TASK'"/>
   
   <xsl:variable name="kpi.jira.issue.internal.class.bug.open.version" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_BUG_OPEN_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.class.bug.resolved.version" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_BUG_RESOLVED_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.class.bug.open" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_BUG_OPEN'"/>   
   <xsl:variable name="kpi.jira.issue.internal.class.refactoring.open.version" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_REFACTORING_OPEN_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.class.refactoring.open" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_REFACTORING_OPEN'"/>
   <xsl:variable name="kpi.jira.issue.internal.class.task.open.version" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_TASK_OPEN_VERSION'"/>
   <xsl:variable name="kpi.jira.issue.internal.class.task.open" select="'KPI_JIRA_ISSUE_INTERNAL_CLASS_TASK_OPEN'"/>
   
   <!-- **************** Effort ******** -->
   <xsl:variable name="kpi.jira.issue.crs.effort.remaining.version.prefix" select="'KPI_JIRA_ISSUE_CRS_EFFORT_REMAINING_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.bugs.effort.remaining.version.prefix" select="'KPI_JIRA_ISSUE_BUGS_EFFORT_REMAINING_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.bugs.internal.effort.remaining.version.prefix" select="'KPI_JIRA_ISSUE_BUGS_INTERNAL_EFFORT_REMAINING_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.tasks.effort.remaining.version.prefix" select="'KPI_JIRA_ISSUE_TASKS_EFFORT_REMAINING_VERSION_'"/>
   
   <xsl:variable name="kpi.jira.issue.crs.effort.spent.version.prefix" select="'KPI_JIRA_ISSUE_CRS_EFFORT_SPENT_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.bugs.effort.spent.version.prefix" select="'KPI_JIRA_ISSUE_BUGS_EFFORT_SPENT_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.bugs.internal.effort.spent.version.prefix" select="'KPI_JIRA_ISSUE_BUGS_INTERNAL_EFFORT_SPENT_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.tasks.effort.spent.version.prefix" select="'KPI_JIRA_ISSUE_TASKS_EFFORT_SPENT_VERSION_'"/>
   
   <xsl:variable name="kpi.jira.issue.crs.effort.remaining.all.version.prefix" select="'KPI_JIRA_ISSUE_CRS_EFFORT_REMAINING_ALL_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.bugs.effort.remaining.all.version.prefix" select="'KPI_JIRA_ISSUE_BUGS_EFFORT_REMAINING_ALL_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.bugs.internal.effort.remaining.all.version.prefix" select="'KPI_JIRA_ISSUE_BUGS_INTERNAL_EFFORT_REMAINING_ALL_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.tasks.effort.remaining.all.version.prefix" select="'KPI_JIRA_ISSUE_TASKS_EFFORT_REMAINING_ALL_VERSION_'"/>
   
   <xsl:variable name="kpi.jira.issue.crs.effort.spent.all.version.prefix" select="'KPI_JIRA_ISSUE_CRS_EFFORT_SPENT_ALL_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.bugs.effort.spent.all.version.prefix" select="'KPI_JIRA_ISSUE_BUGS_EFFORT_SPENT_ALL_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.bugs.internal.effort.spent.all.version.prefix" select="'KPI_JIRA_ISSUE_BUGS_INTERNAL_EFFORT_SPENT_ALL_VERSION_'"/>
   <xsl:variable name="kpi.jira.issue.tasks.effort.spent.all.version.prefix" select="'KPI_JIRA_ISSUE_TASKS_EFFORT_SPENT_ALL_VERSION_'"/>      
   
   <!-- **************** Test Coverage ******** -->
   <xsl:variable name="kpi.req.main.spec.usecase.number" select="'KPI_REQ_MAIN_SPEC_USECASE_NUMBER'"/>
   <xsl:variable name="kpi.req.main.spec.usecase.covered.number" select="'KPI_REQ_MAIN_SPEC_USECASE_COVERED_NUMBER'"/>
   <xsl:variable name="kpi.req.all.spec.usecase.number" select="'KPI_REQ_ALL_SPEC_USECASE_NUMBER'"/>
   <xsl:variable name="kpi.req.all.spec.usecase.covered.number" select="'KPI_REQ_ALL_SPEC_USECASE_COVERED_NUMBER'"/>
   
   <!--  *************** SCARAB ***************-->
   <!-- number of scarab issues -->
   <xsl:variable name="kpi.scarab.issue.number" select="'KPI_SCARAB_ISSUE_NUMBER'"/>
   <!-- number of scarab bug issues -->
   <xsl:variable name="kpi.scarab.issue.bugs.number" select="'KPI_SCARAB_ISSUE_BUG_NUMBER'"/>
   <!-- number of scarab cr issues -->
   <xsl:variable name="kpi.scarab.issue.cr.number" select="'KPI_SCARAB_ISSUE_CR_NUMBER'"/>
   <!-- number of scarab wrong impl issues -->
   <xsl:variable name="kpi.scarab.issue.wrong.number" select="'KPI_SCARAB_ISSUE_WRONG_NUMBER'"/>
   <!-- number of scarab unclosed/unresolved bug issues -->
   <xsl:variable name="kpi.scarab.issue.bugs.open.number" select="'KPI_SCARAB_ISSUE_BUG_OPEN_NUMBER'"/>
   <!-- number of scarab unclosed/unresolved cr issues -->
   <xsl:variable name="kpi.scarab.issue.cr.open.number" select="'KPI_SCARAB_ISSUE_CR_OPEN_NUMBER'"/>
   <!-- number of scarab unclosed/unresolved wrong impl issues -->
   <xsl:variable name="kpi.scarab.issue.wrong.open.number" select="'KPI_SCARAB_ISSUE_WRONG_OPEN_NUMBER'"/>
   
   <!--  *************** JIRA Extern ***************-->
   <!-- external Jira module (issues created by external party/customer) -->
   <xsl:variable name="kpi.jira.issue.external.bugs.number"              select="'KPI_JIRA_ISSUE_EXTERNAL_BUGS_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.external.bugs.verifikation.number" select="'KPI_JIRA_ISSUE_EXTERNAL_BUGS_VERIFIKATION_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.external.bugs.verified.number"     select="'KPI_JIRA_ISSUE_EXTERNAL_BUGS_VERIFIED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.external.bugs.inprogress.number"   select="'KPI_JIRA_ISSUE_EXTERNAL_BUGS_IN_PROGRESS_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.external.bugs.resolved.number"     select="'KPI_JIRA_ISSUE_EXTERNAL_BUGS_RESOLVED_NUMBER'"/>
   <xsl:variable name="kpi.jira.issue.external.bugs.released.number"     select="'KPI_JIRA_ISSUE_EXTERNAL_BUGS_RELEASED_NUMBER'"/>
</xsl:stylesheet>