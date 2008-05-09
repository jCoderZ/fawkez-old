<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:redirect="http://xml.apache.org/xalan/redirect" xmlns:xalan2="http://xml.apache.org/xslt"
   xmlns:uc="uc" xmlns:req="req" extension-element-prefixes="redirect"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" exclude-result-prefixes="uc req xsi xalan2"
   xsi:schemaLocation="uc
                       http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                       req
                       http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd">

   <xsl:param name="target-directory" select="'hibernate'" />
   <xsl:param name="package-root" select="'org.jcoderz.hibernate'" />
   <xsl:param name="tablename-prefix" select="'PPG_'" />

   <xsl:include href="libcommon.xsl" />

   <xsl:output method="xml" encoding="UTF-8" indent="yes"
      doctype-public="-//Hibernate/Hibernate Mapping DTD 3.0//EN"
      doctype-system="http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd" />

   <xsl:template match="uc:usecases">
      <xsl:apply-templates
         select="req:requirement[req:category/req:primary = 'Domain Model']/req:entity" mode="hbm" />

      <redirect:write file="{$target-directory}/Dummy.hbm.xml" method="xml">

         <hibernate-mapping>
            <xsl:attribute name="package">
           <xsl:value-of select="$package-root" />
        </xsl:attribute>

            <class name="Dummy" table="Dummy">
               <id column="ID" name="id" type="integer">
                  <meta attribute="use-in-tostring">false</meta>
               </id>
               <property name="dummy" column="DUMMY" type="string" length="5" />
            </class>
         </hibernate-mapping>
      </redirect:write>
   </xsl:template>

   <!-- Hibernate model export for domain model -->

   <xsl:template match="req:entity" mode="hbm">

      <xsl:variable name="name">
         <xsl:value-of select="../req:key" />
         <xsl:text>-</xsl:text>
         <xsl:value-of select="req:name" />
      </xsl:variable>

      <xsl:variable name="file">
         <xsl:call-template name="asCamelCase">
            <xsl:with-param name="s" select="$name" />
         </xsl:call-template>
         <xsl:text>.hbm.xml</xsl:text>
      </xsl:variable>

      <redirect:write file="{$target-directory}/{$file}" method="xml">

         <hibernate-mapping>
            <xsl:attribute name="package">
                 <xsl:value-of select="$package-root" />
              </xsl:attribute>

            <class>
               <xsl:attribute name="name">
                  <xsl:call-template name="asJavaIdentifier">
                     <xsl:with-param name="name" select="req:name" />
                  </xsl:call-template>
              </xsl:attribute>
               <xsl:attribute name="table">
                  <xsl:value-of select="$tablename-prefix" />
                  <xsl:call-template name="toUpperCase">
                     <xsl:with-param name="s" select="req:name" />
                  </xsl:call-template>
              </xsl:attribute>

               <meta attribute="field-description">
                  <xsl:value-of select="$name" />
                  <xsl:text>
                  </xsl:text>
                  <xsl:value-of select="normalize-space(req:description)" />
               </meta>

               <id column="ID" name="id" type="long">
                  <meta attribute="use-in-tostring">false</meta>
               </id>

               <xsl:for-each select="req:attribute">
                  <xsl:choose>
                     <xsl:when test="req:objectreference">
                        <xsl:variable name="ref_id" select="req:objectreference/req:ref/@id" />
                        <xsl:variable name="linkstart" select="req:objectreference/req:linkstart" />
                        <xsl:variable name="linkend" select="req:objectreference/req:linkend" />

                        <xsl:variable name="from-count">
                           <xsl:choose>
                              <xsl:when test="$linkstart = '1'">
                                 <xsl:text>one</xsl:text>
                              </xsl:when>
                              <xsl:when test="$linkstart = '0..1'">
                                 <xsl:text>one</xsl:text>
                              </xsl:when>
                              <xsl:when test="$linkstart = '0..*'">
                                 <xsl:text>many</xsl:text>
                              </xsl:when>
                              <xsl:when test="$linkstart = '1..*'">
                                 <xsl:text>many</xsl:text>
                              </xsl:when>
                              <xsl:otherwise>
                                 <xsl:text>one</xsl:text>
                              </xsl:otherwise>
                           </xsl:choose>
                        </xsl:variable>

                        <xsl:variable name="to-count">
                           <xsl:choose>
                              <xsl:when test="$linkstart = '1'">
                                 <xsl:text>one</xsl:text>
                              </xsl:when>
                              <xsl:when test="$linkstart = '0..1'">
                                 <xsl:text>one</xsl:text>
                              </xsl:when>
                              <xsl:when test="$linkstart = '0..*'">
                                 <xsl:text>many</xsl:text>
                              </xsl:when>
                              <xsl:when test="$linkstart = '1..*'">
                                 <xsl:text>many</xsl:text>
                              </xsl:when>
                              <xsl:otherwise>
                                 <xsl:text>one</xsl:text>
                              </xsl:otherwise>
                           </xsl:choose>
                        </xsl:variable>

                        <xsl:choose>
                           <xsl:when test="from-count = 'one' and to-count = 'one'">
                              <one-to-one>
                                 <xsl:attribute name="class">
                                    <xsl:call-template name="findClassForId">
                                       <xsl:with-param name="id" select="$ref_id" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                                 <xsl:attribute name="name">
                                    <xsl:call-template name="asJavaParameter">
                                       <xsl:with-param name="name" select="req:name" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                              </one-to-one>
                           </xsl:when>
                           <xsl:when test="from-count = 'many' and to-count = 'one'">
                              <many-to-one>
                                 <xsl:attribute name="class">
                                    <xsl:call-template name="findClassForId">
                                       <xsl:with-param name="id" select="$ref_id" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                                 <xsl:attribute name="name">
                                    <xsl:call-template name="asJavaParameter">
                                       <xsl:with-param name="name" select="req:name" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                              </many-to-one>
                           </xsl:when>
                           <xsl:when test="from-count = 'one' and to-count = 'many'">
                              <set>
                                 <xsl:attribute name="name">
                                    <xsl:call-template name="asJavaParameter">
                                       <xsl:with-param name="name" select="req:name" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                                 <key column="ID" />
                                 <one-to-many>
                                    <xsl:attribute name="class">
                                    <xsl:call-template name="findClassForId">
                                       <xsl:with-param name="id" select="$ref_id" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                                 </one-to-many>
                              </set>
                           </xsl:when>
                           <xsl:when test="from-count = 'many' and to-count = 'many'">
                              <set>
                                 <xsl:attribute name="name">
                                    <xsl:call-template name="asJavaParameter">
                                       <xsl:with-param name="name" select="req:name" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                                 <key column="ID" />
                                 <many-to-many>
                                    <xsl:attribute name="class">
                                    <xsl:call-template name="findClassForId">
                                       <xsl:with-param name="id" select="$ref_id" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                                 </many-to-many>
                              </set>
                           </xsl:when>
                        </xsl:choose>
                     </xsl:when>
                     <xsl:otherwise>
                        <property>
                           <xsl:apply-templates mode="attributes" select="." />
                        </property>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:for-each>

            </class>

         </hibernate-mapping>
      </redirect:write>

   </xsl:template>

   <xsl:template match="req:attribute" mode="attributes">
      <xsl:attribute name="name">
         <xsl:call-template name="asJavaParameter">
            <xsl:with-param name="name" select="req:name" />
         </xsl:call-template>
      </xsl:attribute>
      <!--
         <xsl:attribute name="column">
         <xsl:call-template name="asColumnName">
         <xsl:with-param name="name" select="req:name" />
         </xsl:call-template>
         </xsl:attribute>
      -->

      <xsl:choose>
         <xsl:when test="req:pattern = 'Integer'">
            <xsl:attribute name="type">integer</xsl:attribute>
         </xsl:when>
         <xsl:when test="req:pattern = 'Long'">
            <xsl:attribute name="type">long</xsl:attribute>
         </xsl:when>
         <xsl:when test="req:pattern = 'Date'">
            <xsl:attribute name="type">date</xsl:attribute>
         </xsl:when>
         <xsl:when test="starts-with(req:pattern,'String')">
            <xsl:attribute name="type">string</xsl:attribute>
         </xsl:when>
         <xsl:otherwise>
            <xsl:attribute name="type">string</xsl:attribute>
         </xsl:otherwise>
      </xsl:choose>

      <xsl:if test="req:description">
         <meta attribute="field-description">
            <xsl:value-of select="normalize-space(req:description)" />
         </meta>
      </xsl:if>
   </xsl:template>

   <xsl:template name="asColumnName">
      <xsl:param name="name" />

      <xsl:variable name="cookedName">
         <xsl:call-template name="asJavaParameter">
            <xsl:with-param name="name" select="$name" />
         </xsl:call-template>
      </xsl:variable>

      <xsl:call-template name="toUpperCase">
         <xsl:with-param name="s" select="translate($cookedName,' ','_')" />
      </xsl:call-template>
   </xsl:template>

   <xsl:template name="findClassForId">
      <xsl:param name="id" />

      <xsl:variable name="title">
         <xsl:value-of select="//req:requirement[req:key = $id]/req:title" />
      </xsl:variable>

      <xsl:choose>
         <xsl:when test="$title">
            <xsl:call-template name="asJavaIdentifier">
               <xsl:with-param name="name" select="$title" />
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>Dummy</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>


</xsl:stylesheet>
