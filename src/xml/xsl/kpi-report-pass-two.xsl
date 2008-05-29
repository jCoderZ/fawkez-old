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
                xmlns:kpi="http://jcoderz.org/key-performance"
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
                                    http://www.jcoderz.org/xsd/xdoc/generic-cms-SNAPSHOT.xsd
                                    http://www.jcoderz.org/xsd/xdoc/key-performance-SNAPSHOT.xsd">
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
   
   
   <!-- 
       *******
       Main ;)
       *******
    -->

   <xsl:template match="root">
      <book lang="en" status="final">
         <bookinfo>
            <title>KPI Report</title>

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
         <chapter>
            <title>Title</title>
            <para>nothing</para>
         </chapter>
      </book>
   </xsl:template>
      
</xsl:stylesheet>