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

   <xsl:output method="xml" encoding="UTF-8" indent="yes"
      doctype-public="-//Hibernate/Hibernate Configuration DTD 3.0//EN"
      doctype-system="http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd" />

   <xsl:output method="xml" encoding="UTF-8" indent="yes"
      doctype-public="-//Hibernate/Hibernate Mapping DTD 3.0//EN"
      doctype-system="http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd" />

   <xsl:template match="uc:usecases">
      <xsl:variable name="file">hibernate_cfg.xml</xsl:variable>

      <redirect:write file="{$file}" method="xml">
         <hibernate-configuration>
            <session-factory>
               <xsl:attribute name="name">
                  <xsl:value-of select="$session-factory" />
               </xsl:attribute>

               <xsl:apply-templates
                  select="//req:entity[not(../req:category/req:secondary) or ../req:category/req:secondary = '']"
                  mode="hbm-mapping" />

               <xsl:apply-templates
                  select="//req:entity[not(../req:category/req:tertiary) or ../req:category/req:tertiary = '']"
                  mode="hbm-mapping" />

               <xsl:for-each
                  select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">

               </xsl:for-each>
            </session-factory>

            <xsl:apply-templates
               select="//req:entity[not(../req:category/req:secondary) or ../req:category/req:secondary = '']"
               mode="hbm" />

            <xsl:apply-templates
               select="//req:entity[not(../req:category/req:tertiary) or ../req:category/req:tertiary = '']"
               mode="hbm" />

         </hibernate-configuration>

      </redirect:write>
      <xsl:apply-templates select="req:requirement[req:category/req:primary = 'Domain Model']"
         mode="hbm" />
   </xsl:template>

   <xsl:key name="unique-category-primary-key" match="req:requirement/req:category/req:primary"
      use="." />
   <xsl:key name="unique-category-secondary-key" match="req:requirement/req:category/req:secondary"
      use="." />
   <xsl:key name="unique-category-tertiary-key" match="req:requirement/req:category/req:tertiary"
      use="." />

   <xsl:key name="unique-pattern-key" match="//req:pattern" use="." />

   <!-- Hibernate model export for domain model -->

   <xsl:template match="req:entity" mode="hbm-mapping">
      <mapping>
         <xsl:attribute name="resource">
           <xsl:value-of select="req:name" />
           <xsl:text>.hbm.xml</xsl:text>
        </xsl:attribute>
      </mapping>
   </xsl:template>

   <xsl:template match="req:entity" mode="hbm">

      <xsl:variable name="file">
         <xsl:value-of select="req:name" />
         <xsl:text>.hbm.xml</xsl:text>
      </xsl:variable>

      <redirect:write file="{$file}" method="xml">

         <hibernate-mapping>
            <xsl:attribute name="package">
                 <xsl:value-of select="$package-root" />
              </xsl:attribute>

            <class>
               <xsl:attribute name="name">
                 <xsl:value-of select="req:name" />
              </xsl:attribute>
               <xsl:attribute name="table">
                 <xsl:value-of select="req:name" />
              </xsl:attribute>

               <id column="ID" name="id" type="integer">
                  <meta attribute="use-in-tostring">false</meta>
               </id>

               <xsl:for-each select="req:attribute">
                  <property>
                     <xsl:attribute name="name">
                 <xsl:value-of select="req:name" />
              </xsl:attribute>
                     <xsl:attribute name="column">
                 <xsl:value-of select="req:name" />
              </xsl:attribute>

                     <meta attribute="field-description">
                        <xsl:value-of select="req:description" />
                     </meta>
                     <meta attribute="use-in-tostring">true</meta>
                  </property>
               </xsl:for-each>

            </class>

         </hibernate-mapping>
      </redirect:write>

   </xsl:template>

</xsl:stylesheet>
