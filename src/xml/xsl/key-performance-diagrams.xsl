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
                xmlns:redirect="http://xml.apache.org/xalan/redirect"
                xmlns:xalan2="http://xml.apache.org/xslt"
                extension-element-prefixes="redirect"
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
                                http://www.jcoderz.org/xsd/xdoc/key-performance-SNAPSHOT.xsd">
   <xsl:output encoding="iso-8859-1" method="xml" omit-xml-declaration="yes"/>

   <xsl:include href="libxdoc.xsl"/>
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
   
   <xsl:param name="imagedir" select="'images'"/>
   
   <xsl:key name="test-group"           match="//tc:test" use="traceability"/>
   <xsl:key name="test-scope-group"     match="//tc:test" use="tc:cut"/>
   <xsl:key name="scope-group"          match="//tc:cut" use="."/>
   <xsl:key name="entry-group"          match="//kpi:kpi_list//kpi:entry"     use="../../kpi:meta/kpi:timestamp"/>
   <xsl:key name="entry-version1-group" match="//kpi:kpi_list//kpi:entry"     use="../../kpi:meta/kpi:version1"/>
   <xsl:key name="entry-version2-group" match="//kpi:kpi_list//kpi:entry"     use="../../kpi:meta/kpi:version2"/>
   <xsl:key name="key-group"            match="//kpi:keys//kpi:key" use="."/>
   <xsl:key name="kpi-group"            match="//kpi:kpi_list" use="kpi:meta/kpi:timestamp"/>
   <xsl:key name="timestamp-group"      match="//kpi:timestamp" use="substring(., 1, 6)"/>
   
   <xsl:key name="version-group"        match="//cms:version" use="."/>
   
   <xsl:key name="key-current-group"    match="//kpi:entry[../../kpi:meta/kpi:timestamp = $timestamp]" use="kpi:key"/>
   
   <xsl:key name="efforttype-group"     match="//cms:efforttype" use="."/>
                                                      
   
   <!-- 
       *******
       Main ;)
       *******
    -->

   <xsl:template match="//root">
      
      <xsl:call-template name="data_time"/>
      <xsl:call-template name="csv_list"/>
      <xsl:call-template name="data_time_version1"/>
      <xsl:call-template name="data_time_monthly"/>
      <xsl:call-template name="data_time_branch"/>
      <xsl:call-template name="data_version1"/>
      <xsl:call-template name="data_version2"/>
      <xsl:call-template name="data_version_current_cms"/>
      <xsl:call-template name="data_version_current_cms_open"/>
      
      <xsl:call-template name="csv_list"/>
      
      <xsl:call-template name="gnuplot"/>
      <xsl:call-template name="gnuplot_testcases"/>
      <xsl:call-template name="gnuplot_efficiency"/>
      <xsl:call-template name="gnuplot_crs"/>
      <xsl:call-template name="gnuplot_crs_version"/>
      <xsl:call-template name="gnuplot_issues_current_histogram"/>
      <xsl:call-template name="gnuplot_affects_issues_current_histogram"/>
      <xsl:call-template name="gnuplot_coverage"/>
      <xsl:call-template name="gnuplot_release_criteria">
         <xsl:with-param name="source_file" select="'data_time_version1'"/>
         <xsl:with-param name="suffix" select="'version1'"/>
      </xsl:call-template>
      <xsl:call-template name="gnuplot_issues_version">
         <xsl:with-param name="source_file" select="'data_time_version1'"/>
         <xsl:with-param name="suffix" select="'version1'"/>
      </xsl:call-template>
      <xsl:call-template name="gnuplot_issues_version">
         <xsl:with-param name="source_file" select="'data_time_branch'"/>
         <xsl:with-param name="suffix" select="'branch'"/>
      </xsl:call-template>
      <xsl:call-template name="gnuplot_issues"/>
      
      <xsl:for-each select="//cms:efforttype[generate-id() = generate-id(key('efforttype-group', .))]">
         <xsl:variable name="effort_type_urified">
            <xsl:call-template name="make_uri">
               <xsl:with-param name="string" select="."/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:call-template name="gnuplot_efforts_type">
            <xsl:with-param name="source_file" select="concat($imagedir,'/data_time_version1')"/>
            <xsl:with-param name="suffix" select="$effort_type_urified"/>
            <xsl:with-param name="effort_type" select="."/>
         </xsl:call-template>
      </xsl:for-each>
      
      <xsl:for-each select="//cms:efforttype[generate-id() = generate-id(key('efforttype-group', .))]">
         <xsl:variable name="effort_type_urified">
            <xsl:call-template name="make_uri">
               <xsl:with-param name="string" select="."/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:call-template name="gnuplot_all_versions_efforts_type">
            <xsl:with-param name="source_file" select="concat($imagedir,'/data_time')"/>
            <xsl:with-param name="suffix" select="concat($effort_type_urified,'_all_versions')"/>
            <xsl:with-param name="effort_type" select="."/>
         </xsl:call-template>
      </xsl:for-each>
   
   </xsl:template>
   <!-- 
       **********
       Data Lists
       **********
    -->
    
   <xsl:template name="data_time">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/data_time</xsl:variable>

      <redirect:write file="{$file}">
      
      <xsl:apply-templates select="//kpi:kpi_list" mode="timestamp">
         <xsl:sort select="kpi:meta/kpi:timestamp" order="ascending" data-type="text"/>
      </xsl:apply-templates>

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="csv_list">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/../../docs/docbook/kpi_list.csv</xsl:variable>

      <redirect:write file="{$file}">
      
      <xsl:text></xsl:text><xsl:value-of select="'Date'"/><xsl:text> </xsl:text>
      <xsl:value-of select="'Revision1'"/><xsl:text> </xsl:text>
      <xsl:value-of select="'Revision2'"/><xsl:text> </xsl:text>
      <xsl:for-each select="//kpi:keys/kpi:key[generate-id() = generate-id(key('key-group', .))]">
         <xsl:variable name="kpi_name_urified">
            <xsl:call-template name="make_uri">
               <xsl:with-param name="string" select="."/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:value-of select="$kpi_name_urified"/><xsl:text> </xsl:text>
      </xsl:for-each>
      <xsl:text>
</xsl:text>
      <xsl:apply-templates select="//kpi:kpi_list" mode="csv_list">
         <xsl:sort select="kpi:meta/kpi:timestamp" order="ascending" data-type="text"/>
      </xsl:apply-templates>

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="data_time_version1">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/data_time_version1</xsl:variable>

      <redirect:write file="{$file}">
            
      <xsl:apply-templates select="//kpi:kpi_list[kpi:meta/kpi:version1 = $version]" mode="timestamp">
         <xsl:sort select="kpi:meta/kpi:timestamp" order="ascending" data-type="text"/>
      </xsl:apply-templates>

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="data_time_monthly">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/data_time_monthly</xsl:variable>

      <redirect:write file="{$file}">

         <xsl:for-each select="//kpi:timestamp[generate-id(.) = generate-id(key('timestamp-group', substring(.,1,6)))]">
            <xsl:sort select="." 
                      order="ascending" 
                      data-type="text"/>                         
               <xsl:variable name="first" select="."/>
               <xsl:apply-templates select="//kpi:kpi_list[kpi:meta/kpi:timestamp = $first]" 
                          mode="timestamp">
               </xsl:apply-templates>
         </xsl:for-each>

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="data_time_branch">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/data_time_branch</xsl:variable>

      <redirect:write file="{$file}">
      
      <xsl:apply-templates select="//kpi:kpi_list[starts-with(kpi:meta/kpi:version1,$branch)]" mode="timestamp">
         <xsl:sort select="kpi:meta/kpi:timestamp" order="ascending" data-type="text"/>
      </xsl:apply-templates>

      </redirect:write>
   </xsl:template>
      
   <xsl:template name="data_version1">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/data_version1</xsl:variable>

      <redirect:write file="{$file}">
      
      <xsl:apply-templates select="//kpi:kpi_list" mode="version1">
         <xsl:sort select="kpi:meta/kpi:version1" order="ascending" data-type="text"/>
      </xsl:apply-templates>

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="data_version2">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/data_version2</xsl:variable>

      <redirect:write file="{$file}">
      
      <xsl:apply-templates select="//kpi:kpi_list" mode="version2">
         <xsl:sort select="kpi:meta/kpi:version2" order="ascending" data-type="text"/>
      </xsl:apply-templates>

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="data_version_current_cms">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/data_current</xsl:variable>

      <redirect:write file="{$file}">
         <xsl:value-of select="'Version'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Bugs'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Change_Requests'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Internal_Bugs'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Tasks'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Open'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Open'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Open'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Open'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Resolved'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Resolved'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Resolved'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Resolved'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'External_Bugs'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Internal_Bugs'"/><xsl:text> </xsl:text>
         <xsl:text>
</xsl:text>
      <xsl:for-each select="//cms:version[generate-id() = generate-id(key('version-group', .))]">
         <xsl:sort select="." order="ascending" data-type="text"/>
         <xsl:variable name="version_name">
            <xsl:choose>
               <xsl:when test="not(. = '')"><xsl:value-of select="."/></xsl:when>
               <xsl:when test=". = ''"><xsl:value-of select="'No Version'"/></xsl:when>
            </xsl:choose>
         </xsl:variable>
         <xsl:variable name="version" select="."/>
         <xsl:value-of select="translate($version_name,' ','_')"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.cr.type and cms:state = $cms.state.closed])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and cms:external-id and cms:state = $cms.state.closed])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and not(cms:external-id) and cms:state = $cms.state.closed])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.task.type and cms:state = $cms.state.closed])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.cr.type and not(cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and cms:external-id and not(cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and not(cms:external-id) and not(cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.task.type and not(cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.cr.type and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted)])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and cms:external-id and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted)])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and not(cms:external-id) and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted)])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.task.type and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted)])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:affects-version = $version and cms:type = $cms.bug.type and cms:external-id])"/><xsl:text> </xsl:text>
         <xsl:value-of select="count(//cms:issue[cms:affects-version = $version and cms:type = $cms.bug.type and not(cms:external-id)])"/><xsl:text> </xsl:text>        
         <xsl:text>
</xsl:text>
      </xsl:for-each>

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="data_version_current_cms_open">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/data_current_open</xsl:variable>

      <redirect:write file="{$file}">
         <xsl:value-of select="'Version'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Bugs'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Change_Requests'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Internal_Bugs'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Tasks'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Open'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Open'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Open'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Open'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Resolved'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Resolved'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Resolved'"/><xsl:text> </xsl:text>
         <xsl:value-of select="'Resolved'"/><xsl:text> </xsl:text>        
         <xsl:text>
</xsl:text>
      <xsl:for-each select="//cms:version[generate-id() = generate-id(key('version-group', .))]">
         <xsl:sort select="." order="ascending" data-type="text"/>
         <xsl:variable name="version_name">
            <xsl:choose>
               <xsl:when test="not(. = '')"><xsl:value-of select="."/></xsl:when>
               <xsl:when test=". = ''"><xsl:value-of select="'No Version'"/></xsl:when>
            </xsl:choose>
         </xsl:variable>
         <xsl:variable name="version" select="."/>
         <xsl:if test="//cms:issue[cms:version = $version and not(cms:state = $cms.state.closed)]">
            <xsl:value-of select="translate($version_name,' ','_')"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.cr.type and cms:state = $cms.state.closed])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and cms:external-id and cms:state = $cms.state.closed])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and not(cms:external-id) and cms:state = $cms.state.closed])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.task.type and cms:state = $cms.state.closed])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.cr.type and not(cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and cms:external-id and not(cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and not(cms:external-id) and not(cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.task.type and not(cms:state = $cms.state.resolved or cms:state = $cms.state.accepted or cms:state = $cms.state.closed)])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.cr.type and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted)])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and cms:external-id and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted)])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.bug.type and not(cms:external-id) and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted)])"/><xsl:text> </xsl:text>
            <xsl:value-of select="count(//cms:issue[cms:version = $version and cms:type = $cms.task.type and (cms:state = $cms.state.resolved or cms:state = $cms.state.accepted)])"/><xsl:text> </xsl:text>        
            <xsl:text>
</xsl:text>
         </xsl:if>
      </xsl:for-each>

      </redirect:write>
   </xsl:template>
   
   <xsl:template match="kpi:kpi_list" mode="timestamp">
      <xsl:variable name="time" select="kpi:meta/kpi:timestamp"/>
      <xsl:value-of select="kpi:meta/kpi:timestamp"/><xsl:text> </xsl:text>
      <xsl:for-each select="//kpi:keys/kpi:key[generate-id() = generate-id(key('key-group', .))]">
         <xsl:variable name="key_name" select="."/>
         <xsl:choose>
            <xsl:when test="key('entry-group',$time)[kpi:key = $key_name]">
               <xsl:value-of select="key('entry-group',$time)[kpi:key = $key_name]/kpi:value"/><xsl:text> </xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="'-'"/><xsl:text> </xsl:text>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:for-each>
      <xsl:text>
</xsl:text>
   </xsl:template>
   
   <xsl:template match="kpi:kpi_list" mode="csv_list">
      <xsl:variable name="time"     select="kpi:meta/kpi:timestamp"/>
      <xsl:variable name="version1" select="kpi:meta/kpi:version1"/>
      <xsl:variable name="version2" select="kpi:meta/kpi:version2"/>
      <xsl:value-of select="kpi:meta/kpi:timestamp"/><xsl:text> </xsl:text>
      <xsl:value-of select="kpi:meta/kpi:version1"/><xsl:text> </xsl:text>
      <xsl:value-of select="kpi:meta/kpi:version2"/><xsl:text> </xsl:text>
      <xsl:for-each select="//kpi:keys/kpi:key[generate-id() = generate-id(key('key-group', .))]">
         <xsl:variable name="key_name" select="."/>
         <xsl:choose>
            <xsl:when test="key('entry-group',$time)[kpi:key = $key_name]">
               <xsl:value-of select="key('entry-group',$time)[kpi:key = $key_name]/kpi:value"/><xsl:text> </xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="'-'"/><xsl:text> </xsl:text>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:for-each>
      <xsl:text>
</xsl:text>
   </xsl:template>
   
   <xsl:template match="kpi:kpi_list" mode="version1">
      <xsl:variable name="version1" select="kpi:meta/kpi:version1"/>
      <xsl:value-of select="kpi:meta/kpi:version1"/><xsl:text> </xsl:text>
      <xsl:for-each select="//kpi:keys/kpi:key[generate-id() = generate-id(key('key-group', .))]">
         <xsl:variable name="key_name" select="."/>
         <xsl:choose>
            <xsl:when test="key('entry-version1-group',$version1)[kpi:key = $key_name]">
               <xsl:value-of select="key('entry-version1-group',$version1)[kpi:key = $key_name]/kpi:value"/><xsl:text> </xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="'-'"/><xsl:text> </xsl:text>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:for-each>
      <xsl:text>
</xsl:text>
   </xsl:template>
   
   <xsl:template match="kpi:kpi_list" mode="version2">
      <xsl:variable name="version2" select="kpi:meta/kpi:version2"/>
      <xsl:value-of select="kpi:meta/kpi:version2"/><xsl:text> </xsl:text>
      <xsl:for-each select="//kpi:keys/kpi:key[generate-id() = generate-id(key('key-group', .))]">
         <xsl:variable name="key_name" select="."/>
         <xsl:choose>
            <xsl:when test="key('entry-version2-group',$version2)[kpi:key = $key_name]">
               <xsl:value-of select="key('entry-version2-group',$version2)[kpi:key = $key_name]/kpi:value"/><xsl:text> </xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="'-'"/><xsl:text> </xsl:text>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:for-each>
      <xsl:text>
</xsl:text>
   </xsl:template>
   
   <!-- dump all data for versions, where unclosed issues exist -->
   <xsl:template match="kpi:kpi_list" mode="version2_open">
      <xsl:variable name="version2" select="kpi:meta/kpi:version2"/>
      <xsl:value-of select="kpi:meta/kpi:version2"/><xsl:text> </xsl:text>
      <xsl:for-each select="//kpi:keys/kpi:key[generate-id() = generate-id(key('key-group', .))]">
         <xsl:variable name="key_name" select="."/>
         <xsl:choose>
            <xsl:when test="key('entry-version2-group',$version2)[kpi:key = $key_name]">
               <xsl:value-of select="key('entry-version2-group',$version2)[kpi:key = $key_name]/kpi:value"/><xsl:text> </xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="'-'"/><xsl:text> </xsl:text>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:for-each>
      <xsl:text>
</xsl:text>
   </xsl:template>
   
   <!-- 
       *****************
       Gnuplot Templates
       ***************** 
    -->
    
   <xsl:template name="gnuplot_release_criteria">
      <xsl:param name="source_file"/>
      <xsl:param name="suffix"/>
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/release_criteria.gnuplot</xsl:variable>
                    
      <!-- acceptance criterias / percentage values-->
      <xsl:variable name="criteria_accepted" select="100"/>
      <xsl:variable name="criteria_passed" select="100"/>
      <xsl:variable name="criteria_automized" select="12"/>
      <xsl:variable name="criteria_coverage" select="60"/>
      <xsl:variable name="criteria_known_issues" select="0"/>
      <xsl:variable name="criteria_regression" select="100"/>
      
                    
      <xsl:variable name="number_issues" select="number(key('key-current-group',$kpi.jira.issue.bugs.number.version)/kpi:value) + number(key('key-current-group',$kpi.jira.issue.cr.number.version)/kpi:value) + number(key('key-current-group',$kpi.jira.issue.internal.number.version)/kpi:value)"/>
      <xsl:variable name="number_accepted_issues" select="number(key('key-current-group',$kpi.jira.issue.bugs.accepted.number.version)/kpi:value) + number(key('key-current-group',$kpi.jira.issue.crs.accepted.number.version)/kpi:value) + number(key('key-current-group',$kpi.jira.issue.internal.accepted.number.version)/kpi:value)"/>
      <xsl:variable name="ratio_accepted_issues">
         <xsl:choose>
            <xsl:when test="$number_accepted_issues > 0"><xsl:value-of select="$number_accepted_issues div $number_issues * 100"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="0"/></xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="number_usecase_main" select="number(key('key-current-group',$kpi.req.main.spec.usecase.number)/kpi:value)"/>
      <xsl:variable name="number_usecase_main_covered" select="number(key('key-current-group',$kpi.req.main.spec.usecase.covered.number)/kpi:value)"/>
      <xsl:variable name="ratio_usecase_main_covered" select="number($number_usecase_main_covered) div number($number_usecase_main) * 100"/>
      
      <xsl:variable name="number_executed_testspecs" select="number(key('key-current-group',$kpi.testresults.testcases.number.release)/kpi:value)"/>
      <xsl:variable name="number_executed_testspecs_passed" select="number(key('key-current-group',$kpi.testcases.executed.passed.version.number)/kpi:value)"/>
      
      <xsl:variable name="ratio_executed_testspecs_passed">
         <xsl:choose>
            <xsl:when test="$number_executed_testspecs > 0"><xsl:value-of select="$number_executed_testspecs_passed div $number_executed_testspecs * 100"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="0"/></xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="number_specified_tests" select="number(key('key-current-group',$kpi.testcases.number)/kpi:value)"/>
      <xsl:variable name="ratio_executed_testspecs">
         <xsl:choose>
            <xsl:when test="$number_specified_tests > 0"><xsl:value-of select="$number_executed_testspecs div $number_specified_tests * 100"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="0"/></xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="number_automated_tests" select="number(number(key('key-current-group',$kpi.testcases.automated.jmeter.number)/kpi:value) + number(key('key-current-group',$kpi.testcases.automated.selenium.number)/kpi:value))"/>
      <xsl:variable name="ratio_automated_tests">
         <xsl:choose>
            <xsl:when test="$number_specified_tests > 0"><xsl:value-of select="$number_automated_tests div $number_specified_tests * 100"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="0"/></xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      
         <redirect:write file="{$file}">
# DEBUG information: 
# number 1: <xsl:value-of select="number(key('key-current-group',$kpi.jira.issue.bugs.number.version)/kpi:value)"/>
# number 2: <xsl:value-of select="key('key-current-group',$kpi.jira.issue.bugs.number.version)/kpi:value"/>
# number 3: <xsl:value-of select="key('key-current-group',$kpi.jira.issue.bugs.number.version)"/>
# number 4: <xsl:value-of select="$kpi.jira.issue.bugs.number.version"/>
# number 5: <xsl:value-of select="//kpi:entry[../kpi:meta/kpi:timestamp = $timestamp and key = $kpi.jira.issue.bugs.number.version]/value"/>
# number 6: <xsl:value-of select="$timestamp"/>
# number issue: <xsl:value-of select="$number_issues"/>
# number accepted issue: <xsl:value-of select="$number_accepted_issues"/>
# ratio accepted issues: <xsl:value-of select="$ratio_accepted_issues"/>
# number usecases main: <xsl:value-of select="$number_usecase_main"/>
# number usecases main covered: <xsl:value-of select="$number_usecase_main_covered"/>
# ratio usecases main covered: <xsl:value-of select="$ratio_usecase_main_covered"/>
# number executed testspecs: <xsl:value-of select="$number_executed_testspecs"/>
# number executed testspecs passed: <xsl:value-of select="$number_executed_testspecs_passed"/>
# ratio executed testspecs passed: <xsl:value-of select="$ratio_executed_testspecs_passed"/>
# number_specified_tests: <xsl:value-of select="$number_specified_tests"/>
# ratio executed testspecs: <xsl:value-of select="$ratio_executed_testspecs"/>
# number_automated_tests: <xsl:value-of select="$number_automated_tests"/>
# ratio_automated_tests: <xsl:value-of select="$ratio_automated_tests"/>
      
set angles degrees
set polar

unset border
unset param
set title "Key Performance Values"

set style data line
set yrange [-120:120]
set xrange [-120:120]
set trange [-pi:pi]
set rrange [-120:120]

set style line 1 lt rgb "green" lw 3
set style line 2 lt rgb "red" lw 3

unset xtics
unset ytics

set output '<xsl:value-of select="$imagedir"/>/jpg/release_criteria.jpg'
set terminal jpeg size 800 600
plot "-" using 1:5 ls 1 title 'reached' with filledcurve, \
     "-" using 1:5 ls 2 title 'planned', \
     "-" using 1:5:7 with labels tc lt 3 rotate by +270 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 rotate by +270 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 notitle, \
     "-" using 1:5 notitle

set output '<xsl:value-of select="$imagedir"/>svg/release_criteria.svg'
set terminal jpeg size 400 320
plot "-" using 1:5 ls 1 title 'reached' with filledcurve, \
     "-" using 1:5 ls 2 title 'planned', \
     "-" using 1:5:7 with labels tc lt 3 rotate by +270 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 rotate by +270 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 notitle, \
     "-" using 1:5 notitle, \
     "-" using 1:5:7 with labels tc lt 3 notitle, \
     "-" using 1:5 notitle
     
#  theta(deg)    phi(deg)  E-theta(dB)  phase     E-phi(dB)    phase
   0  0  -120  0  -<xsl:value-of select="100 - $ratio_accepted_issues + 20"/>   0
   60 0  -120  0  -<xsl:value-of select="100 - $ratio_executed_testspecs_passed + 20"/>   0
   120   0  -120  0  -<xsl:value-of select="100 - $ratio_usecase_main_covered + 20"/>   0
   180   0  -120  0  -120   0
   240   0  -120  0  -<xsl:value-of select="100 - $ratio_executed_testspecs + 20"/>   0
   300   0  -120  0  -<xsl:value-of select="100 - $ratio_automated_tests + 20"/>   0
   360   0  -120  0  -<xsl:value-of select="100 - $ratio_accepted_issues + 20"/>   0
e
   0  0  -120  0  -<xsl:value-of select="100 - number($criteria_accepted) + 20"/>   0
   60 0  -120  0  -<xsl:value-of select="100 - number($criteria_passed) + 20"/>   -20
   120   0  -120  0  -<xsl:value-of select="100 - number($criteria_coverage) + 20"/>   0
   180   0  -120  0  -<xsl:value-of select="100 - number($criteria_known_issues) + 20"/>   0
   240   0  -120  0  -<xsl:value-of select="100 - number($criteria_regression) + 20"/>   0
   300   0  -120  0  -<xsl:value-of select="100 - number($criteria_automized) + 20"/>   0
   360   0  -120  0  -<xsl:value-of select="100 - number($criteria_accepted) + 20"/>   0
e
   1  0  -120  0  -120  0  
   0  0  -120  0  0  -10  Accepted
e
   1  0  -120  0  -120  0  
   0  0  -120  0  -20   0
e
   0  0  -120  0  -120  0  
   60 0  -120  0  0  -10  Passed
e
   0  0  -120  0  -120  0
   60 0  -120  0  -20   0
e
   0  0  -120  0  -120  0  
   120   0  -120  0  -10  0  Coverage
e
   0  0  -120  0  -120  0
   120   0  -120  0  -20   0
e
   0  0  -120  0  -120  0  
   180   0  -120  0  -10  0  "Known Issues Criteria"
e
   0  0  -120  0  -120  0
   180   0  -120  0  -20   0
e
   0  0  -120  0  -120  0  
   240   0  -120  0  -10  0  Regression
e
   0  0  -120  0  -120  0
   240   0  -120  0  -20   0
e
   0  0  -120  0  -120  0  
   300   0  -120  0  -10  0  Automization
e
   0  0  -120  0  -120  0
   300   0  -120  0  -20   0
      </redirect:write>
   </xsl:template>
    
   <xsl:template name="gnuplot">
      <xsl:for-each select="//kpi:keys/kpi:key[generate-id() = generate-id(key('key-group', .))]">
         <xsl:variable name="key_name" select="."/>
         <xsl:variable name="file"><xsl:value-of
                       select="$imagedir"/>/<xsl:value-of
                       select="$key_name"/>.gnuplot</xsl:variable>
   
         <redirect:write file="{$file}">
set output '<xsl:value-of select="$imagedir"/>/svg/<xsl:value-of select="$key_name"/>.svg'
set terminal svg size 400 320
set xdata time
set format x "%m/%y"
set timefmt "%Y%m%d%H%M"
set xtics nomirror rotate by -45
set yrange [0:]

set title "<xsl:value-of select="$key_name"/>"
show title
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of
                    select="position() + 1"/> w lines title 'kpi data'

set output '<xsl:value-of select="$imagedir"/>/jpg/<xsl:value-of select="$key_name"/>.jpg'
set terminal jpeg size 800 600
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of
                    select="position() + 1"/> w lines title 'kpi data'
         </redirect:write>
      </xsl:for-each>
   </xsl:template>
   
   <!-- chart showing all testspecification counts in a global view -->
   <xsl:template name="gnuplot_testcases">
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/summary_testcases.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set output '<xsl:value-of select="$imagedir"/>/svg/summary_testcases.svg'
set xdata time
set format x "%m/%y"
set timefmt "%Y%m%d%H%M"
set key outside

set style fill solid 1.0 border -1
set boxwidth 0.5 relative
set xtics nomirror rotate by -45

set title 'Testcases'
show title

set terminal svg size 1024 768 

plot '<xsl:value-of select="$imagedir"/>/data_time_monthly' \<xsl:text>
</xsl:text><xsl:for-each select="//tc:cut[generate-id() = generate-id(key('scope-group', .))]">
              <xsl:variable name="scope_text" select="."/>
              <xsl:variable name="position.t"><xsl:call-template name="get_position">
                 <xsl:with-param name="key" select="concat($kpi.testcases.number.module.prefix, $scope_text)"/>
              </xsl:call-template></xsl:variable>
               
               <xsl:text>using </xsl:text>
               <xsl:text>1:</xsl:text>
               <xsl:value-of select="$position.t"/>
               <xsl:text> title '</xsl:text>
               <xsl:value-of select="$scope_text"/>
               <xsl:text>'</xsl:text>
               <xsl:if test="not(position() = last())">
                  <xsl:text>, \
</xsl:text>
               </xsl:if>
           </xsl:for-each>

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_issues_version">
      <xsl:param name="source_file"/>
      <xsl:param name="suffix"/>
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/issues_version_<xsl:value-of
                    select="$suffix"/>.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set terminal svg size 400 320 fsize 8
set xdata time
set format x "%m/%y"
set xtics nomirror rotate by -45
set timefmt "%Y%m%d%H%M"
set key outside
set style fill solid 1.0 border -1
set boxwidth 0.5 relative
set yrange [0:]

set title "Bugs <xsl:value-of select="$version"/>"
show title
         <xsl:variable name="position.1"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.bug.open.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.2"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.bug.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.3"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.bug.resolved.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.4"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.number.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.5"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.bugs.resolved_or_accepted.number.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.6"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.bugs.number.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.7"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.bugs.resolved_or_accepted.number.version"/>
         </xsl:call-template></xsl:variable>
         
set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_<xsl:value-of
                    select="$suffix"/>_intbugs.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.2"/> w lines title 'Internal Bugs Found',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.3"/> w lines title 'Internal Bugs Found + Resolved'
set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_<xsl:value-of
                    select="$suffix"/>_internal.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.4"/> w lines title 'Internal Issues',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.3"/> w lines title 'Internal Issues Resolved'
set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_<xsl:value-of
                    select="$suffix"/>_bugs.svg'     
plot '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.6"/> w lines title 'External Bugs',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.7"/> w lines title 'External Bugs Resolved'

set terminal jpeg size 800 600     
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_<xsl:value-of
                    select="$suffix"/>_intbugs.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.2"/> w lines title 'Internal Bugs Found',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.3"/> w lines title 'Internal Bugs Found + Resolved'
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_<xsl:value-of
                    select="$suffix"/>_internal.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.4"/> w lines title 'Internal Issues',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.3"/> w lines title 'Internal Issues Resolved'
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_<xsl:value-of
                    select="$suffix"/>_bugs.jpg'     
plot '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.6"/> w lines title 'External Bugs',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.7"/> w lines title 'External Bugs Resolved'


      </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_efforts_type">
      <xsl:param name="source_file"/>
      <xsl:param name="suffix"/>
      <xsl:param name="effort_type"/>
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/efforts_version_<xsl:value-of
                    select="$suffix"/>.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set terminal svg size 1024 800 fsize 8
set xdata time
set format x "%m/%y"
set xtics nomirror rotate by -45
set timefmt "%Y%m%d%H%M"
set key outside
set style fill solid 1.0 border -1
set boxwidth 0.5 relative
set yrange [0:]

# setting style of the lines (ls 1 and ls 2)
set style line 1 lt rgb "#00FF00"
set style line 2 lt rgb "#FF0000"
set style line 3 lt rgb "#0000FF"

set title "Efforts for <xsl:value-of select="$effort_type"/> (<xsl:value-of select="$version"/>)"
show title
         <xsl:variable name="position.1"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.bugs.internal.effort.remaining.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.2"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.bugs.internal.effort.spent.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.3"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.bugs.effort.remaining.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.4"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.bugs.effort.spent.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.5"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.crs.effort.remaining.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.6"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.crs.effort.spent.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.7"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.tasks.effort.remaining.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.8"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.tasks.effort.spent.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         
set output '<xsl:value-of select="$imagedir"/>/svg/efforts_version_bugs_internal_<xsl:value-of
                          select="$suffix"/>.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.1"/>+$<xsl:value-of select="$position.2"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.2"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/svg/efforts_version_bugs_<xsl:value-of
                          select="$suffix"/>.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.3"/>+$<xsl:value-of select="$position.4"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.4"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/svg/efforts_version_crs_<xsl:value-of
                          select="$suffix"/>.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.5"/>+$<xsl:value-of select="$position.6"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.6"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/svg/efforts_version_tasks_<xsl:value-of
                          select="$suffix"/>.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.7"/>+$<xsl:value-of select="$position.8"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.8"/> w lines title 'Effort Spent'

set terminal jpeg size 800 600     
set output '<xsl:value-of select="$imagedir"/>/jpg/efforts_version_bugs_internal_<xsl:value-of
                          select="$suffix"/>.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.1"/>+$<xsl:value-of select="$position.2"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.2"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/jpg/efforts_version_bugs_<xsl:value-of
                          select="$suffix"/>.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.3"/>+$<xsl:value-of select="$position.4"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.4"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/jpg/efforts_version_crs_<xsl:value-of
                          select="$suffix"/>.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.5"/>+$<xsl:value-of select="$position.6"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.6"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/jpg/efforts_version_tasks_<xsl:value-of
                          select="$suffix"/>.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.7"/>+$<xsl:value-of select="$position.8"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.8"/> w lines title 'Effort Spent'

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_all_versions_efforts_type">
      <xsl:param name="source_file"/>
      <xsl:param name="suffix"/>
      <xsl:param name="effort_type"/>
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/efforts_version_<xsl:value-of
                    select="$suffix"/>.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set terminal svg size 1024 800 fsize 8
set xdata time
set format x "%m/%y"
set xtics nomirror rotate by -45
set timefmt "%Y%m%d%H%M"
set key outside
set style fill solid 1.0 border -1
set boxwidth 0.5 relative
set yrange [0:]

# setting style of the lines (ls 1 and ls 2)
set style line 1 lt rgb "#00FF00"
set style line 2 lt rgb "#FF0000"
set style line 3 lt rgb "#0000FF"

set title "Efforts for <xsl:value-of select="$effort_type"/>"
show title
         <xsl:variable name="position.1"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.bugs.internal.effort.remaining.all.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.2"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.bugs.internal.effort.spent.all.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.3"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.bugs.effort.remaining.all.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.4"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.bugs.effort.spent.all.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.5"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.crs.effort.remaining.all.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.6"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.crs.effort.spent.all.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.7"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.tasks.effort.remaining.all.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.8"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="concat($kpi.jira.issue.tasks.effort.spent.all.version.prefix, $effort_type)"/>
         </xsl:call-template></xsl:variable>
         
set output '<xsl:value-of select="$imagedir"/>/svg/efforts_version_bugs_internal_<xsl:value-of
                          select="$suffix"/>.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.1"/>+$<xsl:value-of select="$position.2"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.2"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/svg/efforts_version_bugs_<xsl:value-of
                          select="$suffix"/>.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.3"/>+$<xsl:value-of select="$position.4"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.4"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/svg/efforts_version_crs_<xsl:value-of
                          select="$suffix"/>.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.5"/>+$<xsl:value-of select="$position.6"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.6"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/svg/efforts_version_tasks_<xsl:value-of
                          select="$suffix"/>.svg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.7"/>+$<xsl:value-of select="$position.8"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.8"/> w lines title 'Effort Spent'

set terminal jpeg size 800 600     
set output '<xsl:value-of select="$imagedir"/>/jpg/efforts_version_bugs_internal_<xsl:value-of
                          select="$suffix"/>.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.1"/>+$<xsl:value-of select="$position.2"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.2"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/jpg/efforts_version_bugs_<xsl:value-of
                          select="$suffix"/>.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.3"/>+$<xsl:value-of select="$position.4"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.4"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/jpg/efforts_version_crs_<xsl:value-of
                          select="$suffix"/>.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.5"/>+$<xsl:value-of select="$position.6"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.6"/> w lines title 'Effort Spent'
set output '<xsl:value-of select="$imagedir"/>/jpg/efforts_version_tasks_<xsl:value-of
                          select="$suffix"/>.jpg'
plot '<xsl:value-of select="$source_file"/>' using 1:($<xsl:value-of select="$position.7"/>+$<xsl:value-of select="$position.8"/>) w lines title 'Complete Effort',\
     '<xsl:value-of select="$source_file"/>' using 1:<xsl:value-of select="$position.8"/> w lines title 'Effort Spent'

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_issues">
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/issues_version.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set terminal svg size 400 320 fsize 8
set xdata time
set format x "%m/%y"
set xtics nomirror rotate by -45
set timefmt "%Y%m%d%H%M"
set key outside
set style fill solid 1.0 border -1
set boxwidth 0.5 relative
set yrange [0:]

set title "Bugs"
show title
         <xsl:variable name="position.1"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.bug.open"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.2"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.3"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.resolved_or_accepted.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.4"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.open.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.5"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.bugs.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.6"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.bugs.resolved_or_accepted.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.7"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.bugs.open.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.8"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.bugs.unscheduled"/>
         </xsl:call-template></xsl:variable>

set output '<xsl:value-of select="$imagedir"/>/svg/issues_intbugs.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Open Internal Bugs'
set output '<xsl:value-of select="$imagedir"/>/svg/issues_internal.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.4"/> w lines title 'Open Internal Issues'
set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_bugs.svg'     
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.7"/> w lines title 'Open External Bugs'
set output '<xsl:value-of select="$imagedir"/>/svg/issues_allbugs.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Open Internal Bugs',\
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.7"/> w lines title 'Open External Bugs(Unclosed)',\
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.5"/> - $<xsl:value-of select="$position.6"/>) w lines title 'Open External Bugs(Unresolved)'
set output '<xsl:value-of select="$imagedir"/>/svg/issues_extbugs.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.7"/> w lines title 'Open External Bugs(Unclosed)',\
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.5"/> - $<xsl:value-of select="$position.6"/>) w lines title 'Open External Bugs(Unresolved)',\
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.8"/> w lines title 'Open External Bugs(Unscheduled)'

set terminal jpeg size 800 600     
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_intbugs.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Open Internal Bugs'
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_internal.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.4"/> w lines title 'Open Internal Issues'
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_bugs.jpg'     
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.7"/> w lines title 'Open External Bugs'
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_allbugs.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Open Internal Bugs',\
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.7"/> w lines title 'Open External Bugs(Unclosed)',\
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.5"/> - $<xsl:value-of select="$position.6"/>) w lines title 'Open External Bugs(Unresolved)'
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_extbugs.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.7"/> w lines title 'Open External Bugs(Unclosed)',\
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.5"/> - $<xsl:value-of select="$position.6"/>) w lines title 'Open External Bugs(Unresolved)',\
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.8"/> w lines title 'Open External Bugs(Unscheduled)'
      </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_crs">
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/crs.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set terminal svg size 400 320 fsize 8
set xdata time
set format x "%m/%y"
set xtics nomirror rotate by -45
set timefmt "%Y%m%d%H%M"
set key outside
set style fill solid 1.0 border -1
set boxwidth 0.5 relative
set yrange [0:]

set title "Change Requests"
show title
         <xsl:variable name="position.1"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.task.open"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.2"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.task"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.3"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.refactoring.open"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.4"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.refactoring"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.5"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.crs.resolved_or_accepted.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.6"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.cr.number"/>
         </xsl:call-template></xsl:variable>

set output '<xsl:value-of select="$imagedir"/>/svg/crs_tasks.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Open Internal Tasks'
set output '<xsl:value-of select="$imagedir"/>/svg/crs_refactoring.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.3"/> w lines title 'Open Refactoring Issues'
set output '<xsl:value-of select="$imagedir"/>/svg/crs_external.svg'     
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.6"/> - $<xsl:value-of select="$position.5"/>) w lines title 'Open External CRs'
set output '<xsl:value-of select="$imagedir"/>/svg/crs_internal.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.3"/> w lines title 'Open Refactoring Issues', \
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Open Internal Tasks'

set terminal jpeg size 800 600
set output '<xsl:value-of select="$imagedir"/>/jpg/crs_tasks.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Open Internal Tasks'
set output '<xsl:value-of select="$imagedir"/>/jpg/crs_refactoring.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.3"/> w lines title 'Open Refactoring Issues'
set output '<xsl:value-of select="$imagedir"/>/jpg/crs_external.jpg'     
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.6"/> - $<xsl:value-of select="$position.5"/>) w lines title 'Open External CRs'
set output '<xsl:value-of select="$imagedir"/>/jpg/crs_internal.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.3"/> w lines title 'Open Refactoring Issues', \
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Open Internal Tasks'
      </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_crs_version">
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/crs_version.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set terminal svg size 400 320 fsize 8
set xdata time
set format x "%m/%y"
set xtics nomirror rotate by -45
set timefmt "%Y%m%d%H%M"
set key outside
set style fill solid 1.0 border -1
set boxwidth 0.5 relative
set yrange [0:]

set title "Change Requests for <xsl:value-of select="$version"/>"
show title
         <xsl:variable name="position.1"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.task.open.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.2"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.task.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.3"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.refactoring.open.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.4"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.internal.class.refactoring.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.5"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.crs.resolved_or_accepted.number.version"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.6"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.jira.issue.cr.number.version"/>
         </xsl:call-template></xsl:variable>

set output '<xsl:value-of select="$imagedir"/>/svg/crs_version_tasks.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.2"/> w lines title 'Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:($<xsl:value-of select="$position.2"/> - $<xsl:value-of select="$position.1"/>) w lines title 'Resolved Internal Tasks'
set output '<xsl:value-of select="$imagedir"/>/svg/crs_version_refactoring.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.4"/> w lines title 'Refactoring Issues', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:($<xsl:value-of select="$position.4"/> - $<xsl:value-of select="$position.3"/>) w lines title 'Resolved Refactoring Issues'
set output '<xsl:value-of select="$imagedir"/>/svg/crs_version_external.svg'     
plot '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.6"/> w lines title 'External CRs', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.5"/> w lines title 'Resolved External CRs'

set output '<xsl:value-of select="$imagedir"/>/svg/crs_version_internal.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.2"/> w lines title 'Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:($<xsl:value-of select="$position.2"/> - $<xsl:value-of select="$position.1"/>) w lines title 'Resolved Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.4"/> w lines title 'Refactoring Issues', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:($<xsl:value-of select="$position.4"/> - $<xsl:value-of select="$position.3"/>) w lines title 'Resolved Refactoring Issues'

set title "Change Requests for Branch <xsl:value-of select="$branch"/>"     
set output '<xsl:value-of select="$imagedir"/>/svg/crs_branch_external.svg'     
plot '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:<xsl:value-of select="$position.6"/> w lines title 'External CRs', \
     '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:<xsl:value-of select="$position.5"/> w lines title 'Resolved External Crs'
set output '<xsl:value-of select="$imagedir"/>/svg/crs_branch_internal.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:<xsl:value-of select="$position.2"/> w lines title 'Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:($<xsl:value-of select="$position.2"/> - $<xsl:value-of select="$position.1"/>) w lines title 'Resolved Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:<xsl:value-of select="$position.4"/> w lines title 'Refactoring Issues', \
     '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:($<xsl:value-of select="$position.4"/> - $<xsl:value-of select="$position.3"/>) w lines title 'Resolved Refactoring Issues'

set terminal jpeg size 800 600     
set output '<xsl:value-of select="$imagedir"/>/jpg/crs_version_tasks.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.2"/> w lines title 'Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:($<xsl:value-of select="$position.2"/> - $<xsl:value-of select="$position.1"/>) w lines title 'Resolved Internal Tasks'
set output '<xsl:value-of select="$imagedir"/>/jpg/crs_version_refactoring.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.4"/> w lines title 'Refactoring Issues', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:($<xsl:value-of select="$position.4"/> - $<xsl:value-of select="$position.3"/>) w lines title 'Resolved Refactoring Issues'
set output '<xsl:value-of select="$imagedir"/>/jpg/crs_version_external.jpg'     
plot '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.6"/> w lines title 'External CRs', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.5"/> w lines title 'Resolved External CRs'

set output '<xsl:value-of select="$imagedir"/>/jpg/crs_version_internal.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.2"/> w lines title 'Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:($<xsl:value-of select="$position.2"/> - $<xsl:value-of select="$position.1"/>) w lines title 'Resolved Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:<xsl:value-of select="$position.4"/> w lines title 'Refactoring Issues', \
     '<xsl:value-of select="$imagedir"/>/data_time_version1' using 1:($<xsl:value-of select="$position.4"/> - $<xsl:value-of select="$position.3"/>) w lines title 'Resolved Refactoring Issues'

set title "Change Requests for Branch <xsl:value-of select="$branch"/>"     
set output '<xsl:value-of select="$imagedir"/>/jpg/crs_branch_external.jpg'     
plot '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:<xsl:value-of select="$position.6"/> w lines title 'External CRs', \
     '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:<xsl:value-of select="$position.5"/> w lines title 'Resolved External Crs'
set output '<xsl:value-of select="$imagedir"/>/jpg/crs_branch_internal.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:<xsl:value-of select="$position.2"/> w lines title 'Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:($<xsl:value-of select="$position.2"/> - $<xsl:value-of select="$position.1"/>) w lines title 'Resolved Internal Tasks', \
     '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:<xsl:value-of select="$position.4"/> w lines title 'Refactoring Issues', \
     '<xsl:value-of select="$imagedir"/>/data_time_branch' using 1:($<xsl:value-of select="$position.4"/> - $<xsl:value-of select="$position.3"/>) w lines title 'Resolved Refactoring Issues'
      </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_efficiency">
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/test_efficiency.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set terminal svg size 400 320 fsize 8
set xdata time
set ylabel 'min/avg'
set format x "%m/%y"
set xtics nomirror rotate by -45
set timefmt "%Y%m%d%H%M"
set key outside
set style fill solid 1.0 border -1
set boxwidth 0.5 relative
set yrange [0:]

set title "Test Efficiency"
show title
         <xsl:variable name="position.1"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.testresults.time.issues.minutes"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.2"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.testresults.issues.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.3"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.testresults.time.testcases.minutes"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.4"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.testresults.testcases.number"/>
         </xsl:call-template></xsl:variable>

set output '<xsl:value-of select="$imagedir"/>/svg/test_efficiency.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.1"/> / $<xsl:value-of select="$position.2"/>) w lines title 'Test Efficiency(Issues)', \
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.3"/> / $<xsl:value-of select="$position.4"/>) w lines title 'Test Efficiency(Testcases)'

set terminal jpeg size 800 600
set output '<xsl:value-of select="$imagedir"/>/jpg/test_efficiency.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.1"/> / $<xsl:value-of select="$position.2"/>) w lines title 'Test Efficiency(Issues)', \
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:($<xsl:value-of select="$position.3"/> / $<xsl:value-of select="$position.4"/>) w lines title 'Test Efficiency(Testcases)'

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_issues_current_histogram">
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/issues_version_histogram.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set border 3 front linetype -1 linewidth 1.000
set boxwidth 0.8 absolute
set style fill  solid 1.00 noborder
set style histogram rowstacked title  offset character 2, 0.25, 0
set datafile missing '-'

set style data histograms
set title "Open and Resolved Issues\n(histogram for each issue type)" 
set xtics border in scale 1,0.5 nomirror rotate by -45  offset character 0, 0, 0
set xlabel  offset character 0, -2, 0 font "" textcolor lt -1 norotate
set ylabel "Number of Issues" 

# setting style of the lines (ls 1 and ls 2)
set style line 1 lt rgb "#00FF00"
set style line 2 lt rgb "#FF0000"
set style line 3 lt rgb "#0000FF"

set terminal svg size 1024 768 
set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_histogram.svg'

plot newhistogram "Bugs", '<xsl:value-of select="$imagedir"/>/data_current' using 2:xtic(1) t 2 ls 1, '' u 10 t 10 ls 3, '' u 6 t 6 ls 2, \
     newhistogram "CRs", '<xsl:value-of select="$imagedir"/>/data_current' using 3:xtic(1) t 3 ls 1, '' u 11 t 11 ls 3, '' u 7 t 7 ls 2, \
     newhistogram "Internal Bugs", '<xsl:value-of select="$imagedir"/>/data_current' using 4:xtic(1) t 4 ls 1, '' u 12 t 12 ls 3, '' u 8 t 8 ls 2, \
     newhistogram "Tasks", '<xsl:value-of select="$imagedir"/>/data_current' using 5:xtic(1) t 5 ls 1, '' u 13 t 13 ls 3, '' u 9 t 9 ls 2
     
set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_histogram_open.svg'
plot newhistogram "Bugs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 2:xtic(1) t 2 ls 1, '' u 10 t 10 ls 3, '' u 6 t 6 ls 2, \
     newhistogram "CRs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 3:xtic(1) t 3 ls 1, '' u 11 t 11 ls 3, '' u 7 t 7 ls 2, \
     newhistogram "Internal Bugs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 4:xtic(1) t 4 ls 1, '' u 12 t 12 ls 3, '' u 8 t 8 ls 2, \
     newhistogram "Tasks", '<xsl:value-of select="$imagedir"/>/data_current_open' using 5:xtic(1) t 5 ls 1, '' u 13 t 13 ls 3, '' u 9 t 9 ls 2

set style histogram cluster gap 1
set output '<xsl:value-of select="$imagedir"/>/svg/issues_affects_version_histogram_bugs.svg'
plot newhistogram "Bugs", '<xsl:value-of select="$imagedir"/>/data_current' using 14:xtic(1) t 14 ls 2, '' u 15 t 15 ls 1
set style histogram rowstacked title  offset character 2, 0.25, 0

set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_histogram_open_bugs.svg'
plot newhistogram "Bugs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 2:xtic(1) t 2 ls 1, '' u 10 t 10 ls 3, '' u 6 t 6 ls 2
set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_histogram_open_crs.svg'
plot newhistogram "CRs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 3:xtic(1) t 3 ls 1, '' u 11 t 11 ls 3, '' u 7 t 7 ls 2
set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_histogram_open_internalbugs.svg'
plot newhistogram "Internal Bugs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 4:xtic(1) t 4 ls 1, '' u 12 t 12 ls 3, '' u 8 t 8 ls 2
set output '<xsl:value-of select="$imagedir"/>/svg/issues_version_histogram_open_tasks.svg'
plot newhistogram "Tasks", '<xsl:value-of select="$imagedir"/>/data_current_open' using 5:xtic(1) t 5 ls 1, '' u 13 t 13 ls 3, '' u 9 t 9 ls 2
     
set terminal jpeg size 1024 768
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_histogram.jpg'
plot newhistogram "Bugs", '<xsl:value-of select="$imagedir"/>/data_current' using 2:xtic(1) t 2 ls 1, '' u 10 t 10 ls 3, '' u 6 t 6 ls 2, \
     newhistogram "CRs", '<xsl:value-of select="$imagedir"/>/data_current' using 3:xtic(1) t 3 ls 1, '' u 11 t 11 ls 3, '' u 7 t 7 ls 2, \
     newhistogram "Internal Bugs", '<xsl:value-of select="$imagedir"/>/data_current' using 4:xtic(1) t 4 ls 1, '' u 12 t 12 ls 3, '' u 8 t 8 ls 2, \
     newhistogram "Tasks", '<xsl:value-of select="$imagedir"/>/data_current' using 5:xtic(1) t 5 ls 1, '' u 13 t 13 ls 3, '' u 9 t 9 ls 2
     
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_histogram_open.jpg'
plot newhistogram "Bugs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 2:xtic(1) t 2 ls 1, '' u 10 t 10 ls 3, '' u 6 t 6 ls 2, \
     newhistogram "CRs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 3:xtic(1) t 3 ls 1, '' u 11 t 11 ls 3, '' u 7 t 7 ls 2, \
     newhistogram "Internal Bugs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 4:xtic(1) t 4 ls 1, '' u 12 t 12 ls 3, '' u 8 t 8 ls 2, \
     newhistogram "Tasks", '<xsl:value-of select="$imagedir"/>/data_current_open' using 5:xtic(1) t 5 ls 1, '' u 13 t 13 ls 3, '' u 9 t 9 ls 2
     
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_histogram_open_bugs.jpg'
plot newhistogram "Bugs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 2:xtic(1) t 2 ls 1, '' u 10 t 10 ls 3, '' u 6 t 6 ls 2
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_histogram_open_crs.jpg'
plot newhistogram "CRs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 3:xtic(1) t 3 ls 1, '' u 11 t 11 ls 3, '' u 7 t 7 ls 2
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_histogram_open_internalbugs.jpg'
plot newhistogram "Internal Bugs", '<xsl:value-of select="$imagedir"/>/data_current_open' using 4:xtic(1) t 4 ls 1, '' u 12 t 12 ls 3, '' u 8 t 8 ls 2
set output '<xsl:value-of select="$imagedir"/>/jpg/issues_version_histogram_open_tasks.jpg'
plot newhistogram "Tasks", '<xsl:value-of select="$imagedir"/>/data_current_open' using 5:xtic(1) t 5 ls 1, '' u 13 t 13 ls 3, '' u 9 t 9 ls 2
  
     </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_affects_issues_current_histogram">
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/affects_issues_version_histogram.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set border 3 front linetype -1 linewidth 1.000
set boxwidth 0.8 absolute
set style fill  solid 1.00 noborder
set style histogram cluster gap 1
set datafile missing '-'

set style data histograms
set title "External and Internal Bugs found with Version" 
set xtics border in scale 1,0.5 nomirror rotate by -45  offset character 0, 0, 0
set xlabel  offset character 0, -2, 0 font "" textcolor lt -1 norotate
set ylabel "Number of Issues" 

# setting style of the lines (ls 1 and ls 2)
set style line 1 lt rgb "#00FF00"
set style line 2 lt rgb "#FF0000"
set style line 3 lt rgb "#0000FF"

set terminal svg size 1024 768 

set output '<xsl:value-of select="$imagedir"/>/svg/issues_affects_version_histogram_bugs.svg'
plot newhistogram "Bugs", '<xsl:value-of select="$imagedir"/>/data_current' using 14:xtic(1) t 14 ls 2, '' u 15 t 15 ls 1
     </redirect:write>
   </xsl:template>
   
   <xsl:template name="gnuplot_coverage">
      <xsl:variable name="file"><xsl:value-of
                    select="$imagedir"/>/coverage.gnuplot</xsl:variable>

      <redirect:write file="{$file}">
set terminal svg size 400 320 fsize 8
set xdata time
set format x "%m/%y"
set xtics nomirror rotate by -45
set timefmt "%Y%m%d%H%M"
set key outside
set style fill solid 1.0 border -1
set boxwidth 0.5 relative
set yrange [0:]

set title "Test Coverage"
show title
         <xsl:variable name="position.1"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.req.main.spec.usecase.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.2"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.req.main.spec.usecase.covered.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.3"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.req.all.spec.usecase.number"/>
         </xsl:call-template></xsl:variable>
         <xsl:variable name="position.4"><xsl:call-template name="get_position">
            <xsl:with-param name="key" select="$kpi.req.all.spec.usecase.covered.number"/>
         </xsl:call-template></xsl:variable>

set output '<xsl:value-of select="$imagedir"/>/svg/test_coverage.svg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Usecases in TACO', \
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.2"/> w lines title 'Covered Usecases'
     
set output '<xsl:value-of select="$imagedir"/>/svg/test_coverage_all.svg'     
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.3"/> w lines title 'Usecases over all CRs', \
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Covered Usecases'
     
set terminal jpeg size 800 600
set output '<xsl:value-of select="$imagedir"/>/jpg/test_coverage.jpg'
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Usecases in TACO', \
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.2"/> w lines title 'Covered Usecases'
     
set output '<xsl:value-of select="$imagedir"/>/jpg/test_coverage_all.jpg'     
plot '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.3"/> w lines title 'Usecases over all CRs', \
     '<xsl:value-of select="$imagedir"/>/data_time' using 1:<xsl:value-of select="$position.1"/> w lines title 'Covered Usecases'
      </redirect:write>
   </xsl:template>
         
   <!-- 
       ****************
       Helper templates
       ****************
    -->
   <xsl:template name="make_uri">
      <xsl:param name="string"/>
      <xsl:value-of select="translate(translate($string,' ','_'),':','_')"/>
   </xsl:template> 
    
   <xsl:template name="get_position">
      <xsl:param name="key"/>
      <xsl:for-each select="//kpi:keys/kpi:key[generate-id() = generate-id(key('key-group', .))]">
         <xsl:if test=". = $key">
            <xsl:value-of select="position() + 1"/>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>
   
   <xsl:template name="get_position_issues_version">
      <xsl:param name="key"/>
      <xsl:for-each select="//kpi:keys/kpi:key[generate-id() = generate-id(key('key-group', .)[contains(.,$version)])]">
         <xsl:if test=". = $key">
            <xsl:value-of select="position() + 1"/>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>
    
   <xsl:template name="entry">
      <xsl:param name="key"/>
      <xsl:param name="value"/>
      <entry>
         <key><xsl:value-of select="$key"/></key>
         <value><xsl:value-of select="$value"/></value>
      </entry>
      <xsl:text>
      </xsl:text>
   </xsl:template>
      
</xsl:stylesheet>