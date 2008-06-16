<?xml version="1.0" encoding="utf-8"?>

<!-- This stylesheet performs customizations of html2db templates -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:h="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="h"
                version="1.0">
  <xsl:import href="html2db.xsl"/>

  <xsl:template match="h:div">
    <section>
      <xsl:apply-templates select="@id"/>
      <xsl:apply-templates mode="inline"/>
    </section>
  </xsl:template>

</xsl:stylesheet>
