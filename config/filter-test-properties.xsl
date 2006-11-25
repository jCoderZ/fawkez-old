<?xml version="1.0" encoding="UTF-8"?>
<xs:stylesheet xmlns:xs="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	
	<xs:output method="xml"/>

   <xs:template match="*|@*|text()|processing-instruction()">
      <xs:copy>
         <xs:apply-templates select=" *|@*|comment()|text()|processing-instruction()"/>
      </xs:copy>
   </xs:template>

   <xs:template match="testsuite/properties">
      <properties/>
   </xs:template>
	
</xs:stylesheet>