<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:redirect="http://xml.apache.org/xalan/redirect" xmlns:saxon="http://icl.com/saxon"
   xmlns:xalan2="http://xml.apache.org/xslt" xmlns:uc="uc" xmlns:req="req"
   extension-element-prefixes="redirect" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   exclude-result-prefixes="uc req xsi xalan2 saxon"
   xsi:schemaLocation="uc
                       http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                       req
                       http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd">

   <xsl:param name="targetdir" />
   <xsl:param name="package-root" select="'org.jcoderz.hibernate'" />
   <xsl:param name="tablename-prefix" select="'S0IR_PPG_'" />

   <xsl:include href="libcommon.xsl" />

   <xsl:output method="xml" encoding="UTF-8" indent="yes"
      doctype-public="-//Hibernate/Hibernate Mapping DTD 3.0//EN"
      doctype-system="http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd"
      xalan2:indent-amount="2" saxon:indent-spaces="2" />

   <xsl:template match="uc:usecases">
      <xsl:apply-templates
         select="req:requirement[req:category/req:primary = 'Domain Model']/req:entity" mode="hbm" />

      <redirect:write file="{$targetdir}/Dummy.hbm.xml" method="xml">

         <hibernate-mapping>
            <xsl:attribute name="package">
           <xsl:value-of select="$package-root" />
        </xsl:attribute>

            <class name="Dummy" table="DUMMY">
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

      <xsl:variable name="package">
         <xsl:value-of select="../req:category/req:secondary" />
      </xsl:variable>

      <xsl:variable name="name">
         <xsl:value-of select="../req:key" />
         <xsl:text>-</xsl:text>
         <xsl:value-of select="req:name" />
      </xsl:variable>

      <xsl:variable name="file">
         <xsl:if test="$package">
            <xsl:call-template name="toLowerCase">
               <xsl:with-param name="s">
                  <xsl:call-template name="asJavaIdentifier">
                     <xsl:with-param name="name" select="$package" />
                  </xsl:call-template>
               </xsl:with-param>
            </xsl:call-template>
            <xsl:text>/</xsl:text>
         </xsl:if>
         <xsl:call-template name="asCamelCase">
            <xsl:with-param name="s" select="$name" />
         </xsl:call-template>
         <xsl:text>.hbm.xml</xsl:text>
      </xsl:variable>

      <redirect:write file="{$targetdir}/{$file}" method="xml">

         <hibernate-mapping>
            <xsl:attribute name="package">
               <xsl:value-of select="$package-root" />
               <xsl:if test="$package">
               <xsl:text>.</xsl:text>
               <xsl:call-template name="toLowerCase">
                  <xsl:with-param name="s">
                     <xsl:call-template name="asJavaIdentifier">
                        <xsl:with-param name="name" select="$package" />
                     </xsl:call-template>
                  </xsl:with-param>
               </xsl:call-template>
               </xsl:if>
            </xsl:attribute>

            <xsl:variable name="tableName">
               <xsl:value-of select="$tablename-prefix" />
               <xsl:call-template name="asColumnName">
                  <xsl:with-param name="name" select="req:name" />
               </xsl:call-template>
               <xsl:text>S</xsl:text>
            </xsl:variable>

            <class>
               <xsl:attribute name="name">
                  <xsl:call-template name="asJavaIdentifier">
                     <xsl:with-param name="name" select="req:name" />
                  </xsl:call-template>
               </xsl:attribute>
               <xsl:attribute name="table">
                  <xsl:value-of select="$tableName" />
               </xsl:attribute>
               <meta attribute="class-description">
                  <xsl:text>This class has been auto-generated from the entity </xsl:text>
                  <xsl:value-of select="$name" />
                  <xsl:text>.</xsl:text>
                  <xsl:text>
</xsl:text>
                  <xsl:text>DO NOT EDIT !!! ALL CHANGES WILL BE OVERWRITTEN !!!</xsl:text>
                  <xsl:text>
</xsl:text>
                  <xsl:if test="req:description">
                     <xsl:value-of select="normalize-space(req:description)" />
                  </xsl:if>
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
                              <xsl:when test="$linkend = '1'">
                                 <xsl:text>one</xsl:text>
                              </xsl:when>
                              <xsl:when test="$linkend = '0..1'">
                                 <xsl:text>one</xsl:text>
                              </xsl:when>
                              <xsl:when test="$linkend = '0..*'">
                                 <xsl:text>many</xsl:text>
                              </xsl:when>
                              <xsl:when test="$linkend = '1..*'">
                                 <xsl:text>many</xsl:text>
                              </xsl:when>
                              <xsl:otherwise>
                                 <xsl:text>one</xsl:text>
                              </xsl:otherwise>
                           </xsl:choose>
                        </xsl:variable>

                        <xsl:choose>
                           <xsl:when test="$from-count = 'one' and $to-count = 'one'">
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
                           <xsl:when test="$from-count = 'many' and $to-count = 'one'">
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
                                 <xsl:attribute name="column">
                                    <xsl:value-of select="'ID'"/>
                                 </xsl:attribute>
                              </many-to-one>
                           </xsl:when>
                           <xsl:when test="$from-count = 'one' and $to-count = 'many'">
                              <set>
                                 <xsl:attribute name="name">
                                    <xsl:call-template name="asJavaParameter">
                                       <xsl:with-param name="name" select="req:name" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                                 <xsl:attribute name="table">
                                    <xsl:value-of select="$tableName" />
                                    <xsl:text>_</xsl:text>
                                    <xsl:call-template name="asColumnName">
                                       <xsl:with-param name="name" select="req:name" />
                                    </xsl:call-template>
                                    <xsl:text>S</xsl:text>
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
                           <xsl:when test="$from-count = 'many' and $to-count = 'many'">
                              <set>
                                 <xsl:attribute name="name">
                                    <xsl:call-template name="asJavaParameter">
                                       <xsl:with-param name="name" select="req:name" />
                                    </xsl:call-template>
                                 </xsl:attribute>
                                 <xsl:attribute name="table">
                                    <xsl:value-of select="$tableName" />
                                    <xsl:text>_</xsl:text>
                                    <xsl:call-template name="asColumnName">
                                       <xsl:with-param name="name" select="req:name" />
                                    </xsl:call-template>
                                    <xsl:text>S</xsl:text>
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

      <xsl:variable name="unifiedName">
         <xsl:call-template name="asJavaConstantName">
            <xsl:with-param name="value" select="req:name" />
         </xsl:call-template>
      </xsl:variable>

      <xsl:attribute name="name">
       <xsl:call-template name="asJavaParameter">
         <xsl:with-param name="name" select="$unifiedName" />
       </xsl:call-template>
      </xsl:attribute>

      <xsl:variable name="patternType">
         <xsl:choose>
            <xsl:when test="contains(req:pattern, ' ')">
               <xsl:call-template name="toLowerCase">
                  <xsl:with-param name="s" select="substring-before(req:pattern, ' ')" />
               </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
               <xsl:call-template name="toLowerCase">
                  <xsl:with-param name="s" select="req:pattern" />
               </xsl:call-template>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="patternLength">
         <xsl:value-of select="substring-after(substring-before(req:pattern, ')'), '(')" />
         <!--
            <xsl:call-template name="extractMultiplicity">
            <xsl:with-param name="source" select="req:pattern" />
            </xsl:call-template>
         -->
      </xsl:variable>


      <xsl:choose>
         <xsl:when test="$patternType = 'number'">
            <xsl:attribute name="type">big_decimal</xsl:attribute>
            <xsl:if test="$patternLength != ''">
               <xsl:attribute name="length">
            <xsl:value-of select="$patternLength" />
          </xsl:attribute>
            </xsl:if>
         </xsl:when>
         <xsl:when test="$patternType = 'float'">
            <xsl:attribute name="type">float</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'double'">
            <xsl:attribute name="type">double</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'boolean'">
            <xsl:attribute name="type">boolean</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'integer'">
            <xsl:attribute name="type">integer</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'long'">
            <xsl:attribute name="type">long</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'short'">
            <xsl:attribute name="type">short</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'date'">
            <xsl:attribute name="type">date</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'time'">
            <xsl:attribute name="type">time</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'timestamp'">
            <xsl:attribute name="type">timestamp</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'calendar'">
            <xsl:attribute name="type">calendar</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'calendardate'">
            <xsl:attribute name="type">calendardate</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'locale'">
            <xsl:attribute name="type">locale</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'timezone'">
            <xsl:attribute name="type">timezone</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'currency'">
            <xsl:attribute name="type">currency</xsl:attribute>
         </xsl:when>
         <xsl:when test="$patternType = 'binary'">
            <xsl:attribute name="type">binary</xsl:attribute>
            <xsl:if test="$patternLength != ''">
               <xsl:attribute name="length">
            <xsl:value-of select="$patternLength" />
          </xsl:attribute>
            </xsl:if>
         </xsl:when>
         <xsl:when test="$patternType = 'text'">
            <xsl:attribute name="type">text</xsl:attribute>
            <xsl:if test="$patternLength != ''">
               <xsl:attribute name="length">
            <xsl:value-of select="$patternLength" />
          </xsl:attribute>
            </xsl:if>
         </xsl:when>
         <xsl:when test="$patternType = 'string'">
            <xsl:attribute name="type">string</xsl:attribute>
            <xsl:if test="$patternLength != ''">
               <xsl:attribute name="length">
            <xsl:value-of select="$patternLength" />
          </xsl:attribute>
            </xsl:if>
         </xsl:when>
         <xsl:otherwise>
            <xsl:attribute name="type">string</xsl:attribute>
         </xsl:otherwise>
      </xsl:choose>

      <xsl:attribute name="column">
         <xsl:call-template name="asColumnName">
            <xsl:with-param name="name" select="req:name" />
            <xsl:with-param name="type" select="$patternType" />
         </xsl:call-template>
      </xsl:attribute>

      <xsl:if test="req:description">
         <meta attribute="field-description">
            <xsl:value-of select="normalize-space(req:description)" />
         </meta>
      </xsl:if>
   </xsl:template>

   <xsl:template name="asColumnName">
      <xsl:param name="name" />
      <xsl:param name="type" />

      <xsl:choose>
         <xsl:when test="$type = 'boolean'">
           <xsl:text>F_</xsl:text>
         </xsl:when>
         <xsl:when test="$type = 'date'">
           <xsl:text>DT_</xsl:text>
         </xsl:when>
         <xsl:when test="$type = 'calendardate'">
           <xsl:text>DT_</xsl:text>
         </xsl:when>
      </xsl:choose>

      <xsl:variable name="cookedName">
         <xsl:call-template name="asJavaConstantName">
            <xsl:with-param name="value" select="$name" />
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

      <xsl:variable name="package">
         <xsl:value-of select="//req:requirement[req:key = $id]/req:category/req:secondary" />
      </xsl:variable>

      <xsl:choose>
         <xsl:when test="$title">
            <xsl:value-of select="$package-root" />
            <xsl:text>.</xsl:text>
            <xsl:if test="$package">
               <xsl:call-template name="toLowerCase">
                  <xsl:with-param name="s">
                     <xsl:call-template name="asJavaIdentifier">
                        <xsl:with-param name="name" select="$package" />
                     </xsl:call-template>
                  </xsl:with-param>
               </xsl:call-template>
               <xsl:text>.</xsl:text>
            </xsl:if>
            <xsl:call-template name="asJavaIdentifier">
               <xsl:with-param name="name" select="$title" />
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$package-root" />
            <xsl:text>.</xsl:text>
            <xsl:text>Dummy</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
