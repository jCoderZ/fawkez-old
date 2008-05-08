<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:redirect="http://xml.apache.org/xalan/redirect" xmlns:xalan2="http://xml.apache.org/xslt"
   xmlns:uc="uc" xmlns:req="req" extension-element-prefixes="redirect"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="uc req xsi xalan2"
   xsi:schemaLocation="uc
                       http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                       req
                       http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd">

   <xsl:param name="targetdir" select="'.'" />
   <xsl:param name="session-factory" select="'default'" />
   <xsl:param name="package-root" select="'org.jcoderz.hibernate'" />

   <xsl:include href="libcommon.xsl"/>

   <xsl:output method="xml" encoding="UTF-8" indent="yes"
      doctype-public="-//Hibernate/Hibernate Configuration DTD 3.0//EN"
      doctype-system="http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd" />

   <xsl:template match="uc:usecases">
      <xsl:variable name="file">hibernate.cfg.xml</xsl:variable>

      <redirect:write file="{$file}" method="xml">
         <hibernate-configuration>
            <session-factory>
               <xsl:attribute name="name">
                  <xsl:value-of select="$session-factory" />
               </xsl:attribute>
               <xsl:apply-templates select="req:requirement[req:category/req:primary = 'Domain Model']/req:entity"
                  mode="hbm-mapping" />
               <mapping resource="Dummy.hbm.xml" />
            </session-factory>
         </hibernate-configuration>
      </redirect:write>
   </xsl:template>

   <xsl:template match="req:entity" mode="hbm-mapping">
      <mapping>
         <xsl:variable name="name">
            <xsl:value-of select="../req:key" />
            <xsl:text>-</xsl:text>
            <xsl:value-of select="req:name" />
         </xsl:variable>

         <xsl:variable name="file">
            <xsl:call-template name="asCamelCase">
               <xsl:with-param name="s" select="$name"/>
            </xsl:call-template>
            <xsl:text>.hbm.xml</xsl:text>
         </xsl:variable>

         <xsl:attribute name="resource">
           <xsl:value-of select="$file" />
        </xsl:attribute>
      </mapping>
   </xsl:template>

</xsl:stylesheet>
