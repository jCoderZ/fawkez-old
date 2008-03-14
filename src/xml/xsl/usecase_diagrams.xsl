<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:redirect="http://xml.apache.org/xalan/redirect"
   xmlns:xalan2="http://xml.apache.org/xslt"
   xmlns:uc="uc"
   xmlns:req="req"
   extension-element-prefixes="redirect"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="uc
                       http://www.jcoderz.org/xsd/xdoc/usecase-SNAPSHOT.xsd
                       req
                       http://www.jcoderz.org/xsd/xdoc/requirements-SNAPSHOT.xsd">

   <xsl:param name="basedir" select="'.'"/>
   <xsl:param name="imagedir" select="'.'"/>
   <!--  Our default language is english. -->
   <xsl:param name="lang" select="'en'"/>

   <xsl:output method="text" encoding="UTF-8"/>

   <xsl:include href="usecase_i18n.xsl"/>

   <xsl:key name="unique-category-primary-key" match="req:requirement/req:category/req:primary" use="."/>
   <xsl:key name="unique-category-secondary-key" match="req:requirement/req:category/req:secondary" use="."/>
   <xsl:key name="unique-category-tertiary-key" match="req:requirement/req:category/req:tertiary" use="."/>
   <xsl:key name="usecase-references-out" 
            match="//uc:ref[starts-with(@id, 'UC-')]" use="ancestor-or-self::uc:usecase/@id"/>
   <xsl:key name="usecase-references-precondition" 
            match="//uc:ref[starts-with(@id, 'UC-') and ancestor-or-self::uc:precondition]" use="ancestor-or-self::uc:usecase/@id"/>

   <xsl:variable name="dummy" select="sdfdsfdsfsdfsdfsdfsdf"/>

   <xsl:template match="uc:usecases">
      <xsl:apply-templates select="uc:usecase"/>
      <xsl:apply-templates select="req:requirement[req:category/req:primary = 'Domain Model']"/>
      <xsl:apply-templates select="req:requirement[req:category/req:primary = 'Domain Model']" mode="xmi"/>
      <xsl:apply-templates select="//uc:usecases" mode="dm"/>
      <xsl:apply-templates select="//uc:usecases" mode="dm_category"/>
      <xsl:apply-templates select="//uc:usecases" mode="dm_global_cat"/>
      <xsl:apply-templates select="//uc:usecases" mode="roles"/>
      <xsl:apply-templates select="//uc:usecases" mode="roles_category"/>
      <xsl:apply-templates select="//uc:usecases" mode="uc_dep"/>
      <xsl:apply-templates select="//uc:usecase" mode="uc_dep_single"/>
      <xsl:apply-templates select="req:requirement[starts-with(req:category/req:primary, 'Role')]"
                           mode="role"/>
   </xsl:template>

   <!-- Roles Dependency and usecases diagram (complete) -->

   <xsl:template match="uc:usecases" mode="roles">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/roles_model.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Sans"
    fontsize = 8
    rankdir = "LR"

    node [
            fontname = "Sans",
            fontsize = 8,
            shape = "record"
    ]

    edge [
            fontname = "Sans"
            fontsize = 8
            weight = 10
    ]

    <xsl:apply-templates select="//req:role[not(../req:category/req:secondary)]" mode="complete">
       <xsl:with-param name="suppress_uc" select="'true'"/>
    </xsl:apply-templates>

    <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
       <xsl:variable name="sec_cat" select="."/>
       <xsl:if test="//req:role[../req:category/req:secondary = $sec_cat and starts-with(../req:category/req:primary, 'Role')]">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$sec_cat"/>";
          <xsl:apply-templates select="//req:role[../req:category/req:secondary = $sec_cat]" mode="complete">
             <xsl:with-param name="suppress_uc" select="'true'"/>
          </xsl:apply-templates>
          }
       </xsl:if>
    </xsl:for-each>


}
       </redirect:write>
   </xsl:template>

   <xsl:template match="req:role" mode="complete">
       <xsl:param name="suppress_uc" select="'false'"/>
       "<xsl:value-of select="../req:key"/>" [
                label = "<xsl:value-of select="normalize-space(req:name)"/>"
        ]
        <xsl:variable name="role_name" select="normalize-space(req:name)"/>

      <xsl:for-each select="//uc:usecase/uc:actors/uc:primary/uc:name">
         <xsl:variable name="actor_id" select="normalize-space(.)"/>
         <xsl:variable name="role_id" select="//req:requirement[normalize-space(req:role/req:name) = normalize-space($actor_id)]/req:key"/>

         <!-- only show entities, if referenced entity is within documents scope (referenced in root file) -->
         <xsl:if test="$role_name = $actor_id">
           <xsl:if test="not($suppress_uc = 'true')">
              "<xsl:value-of select="../../../@id"/>" [
                   label = "<xsl:value-of select="../../../@id"/><xsl:text> </xsl:text><xsl:value-of select="../../../uc:name"/>"
                   shape = "ellipse"
              ]


              <xsl:call-template name="create_edge">
                 <xsl:with-param name="link_from"      select="$role_id"/>
                 <xsl:with-param name="link_to"        select="../../../@id"/>
                 <xsl:with-param name="link_end"       select="''"/>
                 <xsl:with-param name="link_start"     select="''"/>
                 <xsl:with-param name="link_arrowhead" select="'normal'"/>
              </xsl:call-template>
           </xsl:if>
        </xsl:if>

      </xsl:for-each>

      <xsl:for-each select="req:superior/req:ref">
         <xsl:variable name="superior_id" select="@id"/>
         <xsl:variable name="superior_name" select="//req:role/req:name[../../req:key = $superior_id]"/>
         <xsl:variable name="role_id" select="//req:requirement[normalize-space(req:role/req:name) = $role_name]/req:key"/>

         <xsl:call-template name="create_edge">
            <xsl:with-param name="link_from"      select="$superior_id"/>
            <xsl:with-param name="link_to"        select="$role_id"/>
            <xsl:with-param name="link_end"       select="''"/>
            <xsl:with-param name="link_start"     select="''"/>
            <xsl:with-param name="link_arrowhead" select="'normal'"/>
         </xsl:call-template>

      </xsl:for-each>
   </xsl:template>

   <xsl:template match="uc:usecases" mode="roles_category">
      <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
         <xsl:if test="//req:requirement/req:category/req:secondary[starts-with(../req:primary, 'Role')]">
            <xsl:variable name="sec_cat" select="."/>
            <xsl:call-template name="roles_model">
               <xsl:with-param name="secondary_category" select="."/>
               <xsl:with-param name="tertiary_category" select="''"/>
            </xsl:call-template>
            <xsl:for-each select="//req:requirement/req:category/req:tertiary[generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
               <xsl:if test="//req:requirement/req:category/req:tertiary[starts-with(../req:primary,'Role') and ../req:secondary = $sec_cat]">
                  <xsl:call-template name="roles_model">
                     <xsl:with-param name="secondary_category" select="$sec_cat"/>
                     <xsl:with-param name="tertiary_category" select="."/>
                  </xsl:call-template>
               </xsl:if>
            </xsl:for-each>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="roles_model">
      <xsl:param name="secondary_category"/>
      <xsl:param name="tertiary_category"/>
      <xsl:variable name="file">
         <xsl:choose>
            <xsl:when test="$tertiary_category = ''"><xsl:value-of
                  select="$imagedir"/>/<xsl:value-of
                  select="$secondary_category"/>_roles_model.dot</xsl:when>
            <xsl:otherwise><xsl:value-of
                  select="$imagedir"/>/<xsl:value-of
                  select="$secondary_category"/>_<xsl:value-of
                  select="$tertiary_category"/>_roles_model.dot</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>


       <redirect:write file="{$file}">

digraph G {
    fontname = "Sans"
    fontsize = 8
    rankdir = "LR"

    node [
            fontname = "Sans"
            fontsize = 8
            shape = "record"
            fillcolor = "yellow"
    ]

    edge [
            fontname = "Sans"
            fontsize = 8
    ]

    <xsl:if test="$tertiary_category = ''">
       <xsl:if test="//req:role[../req:category/req:secondary = $secondary_category and starts-with(../req:category/req:primary, 'Role')]">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$secondary_category"/>";
          <xsl:apply-templates select="//req:role[../req:category/req:secondary = $secondary_category]" mode="complete"/>
          }
          <xsl:for-each select="//req:role[not(../req:category/req:secondary = $secondary_category) and ../req:key = //req:role[../req:category/req:secondary = $secondary_category]/req:superior/req:ref/@id]">
             "<xsl:value-of select="../req:key"/>" [
                label = "<xsl:value-of select="normalize-space(req:name)"/>"
             ]
          </xsl:for-each>
       </xsl:if>
    </xsl:if>
    <xsl:if test="not($tertiary_category = '')">
       <xsl:if test="//req:role[../req:category/req:secondary = $secondary_category and starts-with(../req:category/req:primary, 'Role')]">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$secondary_category"/>";
          <xsl:apply-templates select="//req:role[../req:category/req:secondary = $secondary_category  and ../req:category/req:tertiary = $tertiary_category]" mode="complete"/>
          }
          <xsl:for-each select="//req:role[not(../req:category/req:secondary = $secondary_category  and ../req:category/req:tertiary = $tertiary_category) and ../req:key = //req:role[../req:category/req:secondary = $secondary_category  and ../req:category/req:tertiary = $tertiary_category]/req:superior/req:ref/@id]">
             "<xsl:value-of select="../req:key"/>" [
                label = "<xsl:value-of select="normalize-space(req:name)"/>"
             ]
          </xsl:for-each>
       </xsl:if>
    </xsl:if>
}
       </redirect:write>
   </xsl:template>

   <!-- Roles UML class diagram (single) -->

   <xsl:template match="req:requirement" mode="role">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/<xsl:value-of
         select="req:key"/>.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Sans"
    fontsize = 8
    rankdir = "LR"

    node [
            fontname = "Sans"
            fontsize = 8
            shape = "record"
    ]

    edge [
            fontname = "Sans"
            fontsize = 8
    ]

    <xsl:apply-templates select="req:role" mode="complete"/>

    <xsl:variable name="role_id" select="req:key"/>
    <xsl:for-each select="req:role/req:superior">
       <xsl:variable name="superior_id" select="req:ref/@id"/>
       "<xsl:value-of select="$superior_id"/>" [
                label = "<xsl:value-of select="//req:requirement[req:key = $superior_id]/req:role/req:name"/>"
        ]
    </xsl:for-each>

    <xsl:for-each select="//req:superior[req:ref/@id = $role_id]">
       <xsl:variable name="subordinate_id" select="../../req:key"/>
       "<xsl:value-of select="$subordinate_id"/>" [
                label = "<xsl:value-of select="//req:requirement[req:key = $subordinate_id]/req:role/req:name"/>"
        ]
        <xsl:call-template name="create_edge">
            <xsl:with-param name="link_from"      select="$role_id"/>
            <xsl:with-param name="link_to"        select="$subordinate_id"/>
            <xsl:with-param name="link_end"       select="''"/>
            <xsl:with-param name="link_start"     select="''"/>
            <xsl:with-param name="link_arrowhead" select="'normal'"/>
         </xsl:call-template>
    </xsl:for-each>

}
       </redirect:write>
   </xsl:template>

   <!-- Requirements UML class diagram (complete) -->

   <xsl:template match="uc:usecases" mode="dm">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/domain_model.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Sans"
    fontsize = 8

    node [
            fontname = "Sans",
            fontsize = 8,
            shape = "record",
            fillcolor = "#EEEEEE",
            style = "filled"
    ]

    edge [
            fontname = "Sans"
            fontsize = 8
            weight = 10
    ]

    <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
       <xsl:variable name="sec_cat" select="."/>
       <xsl:if test="//req:entity[../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$sec_cat"/>";
          <xsl:apply-templates select="//req:entity[../req:category/req:secondary = $sec_cat]"/>
          }
       </xsl:if>
    </xsl:for-each>
    <xsl:apply-templates select="//req:entity[not(../req:category/req:secondary)]" mode="complete"/>
    <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
       <xsl:variable name="sec_cat" select="."/>
       <xsl:if test="//req:entity[../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']">
          <xsl:apply-templates select="//req:entity[../req:category/req:secondary = $sec_cat]" mode="complete"/>
       </xsl:if>
    </xsl:for-each>
    <xsl:call-template name="check_duplicate_references"/>

}
       </redirect:write>
   </xsl:template>

   <xsl:template name="check_duplicate_references">
      <xsl:for-each select="//req:objectreference">
         <xsl:variable name="dm_root_id" select="../../../req:key"/>
         <xsl:variable name="link_to"  select="req:ref/@id"/>
         <xsl:if test="//req:objectreference[not(../../../req:key = $dm_root_id) and req:ref/@id = $dm_root_id and ../../../req:key = $link_to]">
            <xsl:comment>WARNING: entities <xsl:value-of select="$dm_root_id"/> and <xsl:value-of select="$link_to"/> are referencing to each other</xsl:comment>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="req:entity" mode="complete">
       "<xsl:value-of select="../req:key"/>" [
                label = "{<xsl:value-of select="req:name"/>|<xsl:apply-templates select="req:attribute[not(req:objectreference)]"/>|<xsl:apply-templates select="req:attribute[req:objectreference]"/>}"
        ]

      <xsl:for-each select="req:attribute[req:objectreference]">
         <xsl:variable name="dm_id" select="req:objectreference/req:ref/@id"/>

         <!-- only show entities, if referenced entity is within documents scope (referenced in root file) -->
         <xsl:if test="//req:requirement[req:key = $dm_id]/req:entity">
            <!-- xsl:apply-templates select="//req:requirement[req:key = $dm_id]/req:entity"/-->

           <xsl:call-template name="create_edge">
              <xsl:with-param name="link_from"  select="../../req:key"/>
              <xsl:with-param name="link_to"    select="$dm_id"/>
              <xsl:with-param name="link_end"   select="req:objectreference/req:linkend"/>
              <xsl:with-param name="link_start" select="req:objectreference/req:linkstart"/>
           </xsl:call-template>
        </xsl:if>

      </xsl:for-each>
   </xsl:template>

   <!-- Requirements UML class diagram (global categories) -->

   <xsl:template match="uc:usecases" mode="dm_global_cat">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/domain_model_global_cat.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Sans"
    fontsize = 8

    node [
            fontname = "Sans",
            fontsize = 8,
            shape = "record",
            fillcolor = "#EEEEEE",
            style = "filled"
    ]

    edge [
            fontname = "Sans"
            fontsize = 8
            weight = 10
    ]

    <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
       <xsl:variable name="sec_cat" select="."/>
       <xsl:if test="//req:entity[../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$sec_cat"/>";
             <xsl:variable name="pos" select="position()"/>

             <xsl:choose>
                <xsl:when test="//req:requirement/req:category/req:tertiary[../req:secondary = $sec_cat and ../req:primary = 'Domain Model']">
                   <xsl:for-each select="//req:requirement/req:category/req:tertiary[../req:secondary = $sec_cat and generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
                      <xsl:variable name="ter_cat" select="."/>
                      <xsl:if test="//req:entity[../req:category/req:tertiary = $ter_cat and ../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']">
                         "<xsl:value-of select="$sec_cat"/>" [label="", color="white"];
                         subgraph cluster<xsl:value-of select="concat($pos, position())"/> {
                            label = "<xsl:value-of select="$ter_cat"/>";
                            "<xsl:value-of select="$sec_cat"/>_<xsl:value-of select="$ter_cat"/>" [label="", color="white"];
                         }
                      </xsl:if>
                   </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                   "<xsl:value-of select="$sec_cat"/>" [label="", color="white"];
                </xsl:otherwise>
             </xsl:choose>
          }
       </xsl:if>
    </xsl:for-each>

    <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
       <xsl:variable name="sec_cat" select="."/>
       <xsl:if test="//req:entity[../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']">
          <xsl:variable name="pos" select="position()"/>

          <xsl:choose>
             <xsl:when test="//req:requirement/req:category/req:tertiary[../req:secondary = $sec_cat and ../req:primary = 'Domain Model']">
                <xsl:for-each select="//req:requirement/req:category/req:tertiary[../req:secondary = $sec_cat and generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
                   <xsl:variable name="ter_cat" select="."/>


                   <xsl:call-template name="link_categories">
                      <xsl:with-param name="sec_cat_in" select="$sec_cat"/>
                      <xsl:with-param name="ter_cat_in" select="$ter_cat"/>
                   </xsl:call-template>
                   <!--
                   <xsl:if test="//req:entity[../req:category/req:tertiary = $ter_cat and ../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']">
                      <xsl:apply-templates select="//req:entity[../req:category/req:tertiary = $ter_cat and ../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']"
                                           mode="category_link"/>
                   </xsl:if>
                    -->

                </xsl:for-each>
             </xsl:when>
             <xsl:otherwise>
                <xsl:call-template name="link_categories">
                      <xsl:with-param name="sec_cat_in" select="$sec_cat"/>
                   </xsl:call-template>
             </xsl:otherwise>
          </xsl:choose>
       </xsl:if>
    </xsl:for-each>

}
       </redirect:write>
   </xsl:template>

   <xsl:template name="link_categories">
      <xsl:param name="sec_cat_in" select="$dummy"/>
      <xsl:param name="ter_cat_in" select="$dummy"/>

      <xsl:for-each select="//req:requirement/req:category/req:secondary[../req:primary = 'Domain Model' and not(. = $sec_cat_in)]">
         <xsl:variable name="sec_cat" select="."/>
         <xsl:variable name="key" select="../../req:key"/>

         <xsl:choose>
             <xsl:when test="../req:tertiary[not(. = $ter_cat_in)]">
                <xsl:for-each select="../req:tertiary[not(. = $ter_cat_in)]">
                   <xsl:variable name="ter_cat" select="."/>
                   <xsl:choose>
                      <xsl:when test="//req:requirement[req:category/req:primary = 'Domain Model' and req:category/req:secondary = $sec_cat_in and req:category/req:tertiary = $ter_cat_in]/req:entity//req:ref[@id = $key]">
                         "<xsl:value-of select="$sec_cat"/>_<xsl:value-of select="$ter_cat"/>" -&gt; "<xsl:value-of select="$sec_cat_in"/>_<xsl:value-of select="$ter_cat_in"/>";
                      </xsl:when>
                      <xsl:when test="//req:requirement[req:category/req:primary = 'Domain Model' and req:category/req:secondary = $sec_cat_in]/req:entity//req:ref[@id = $key]">
                         "<xsl:value-of select="$sec_cat"/>_<xsl:value-of select="$ter_cat"/>" -&gt; "<xsl:value-of select="$sec_cat_in"/>";
                      </xsl:when>
                      <!-- xsl:when test="//req:requirement[req:category/req:primary = 'Domain Model']/req:entity//req:ref[@id = $key]">
                         "<xsl:value-of select="$sec_cat"/>_<xsl:value-of select="$ter_cat"/>" -&gt; "Global Entities (without categories)";
                      </xsl:when -->
                   </xsl:choose>
                </xsl:for-each>
             </xsl:when>
             <xsl:otherwise>
                <xsl:choose>
                   <xsl:when test="//req:requirement[req:category/req:primary = 'Domain Model' and req:category/req:secondary = $sec_cat_in and req:category/req:tertiary = $ter_cat_in]/req:entity//req:ref[@id = $key]">
                      "<xsl:value-of select="$sec_cat"/>" -&gt; "<xsl:value-of select="$sec_cat_in"/>_<xsl:value-of select="$ter_cat_in"/>";
                   </xsl:when>
                   <xsl:when test="//req:requirement[req:category/req:primary = 'Domain Model' and req:category/req:secondary = $sec_cat_in]/req:entity//req:ref[@id = $key]">
                      "<xsl:value-of select="$sec_cat"/>" -&gt; "<xsl:value-of select="$sec_cat_in"/>";
                   </xsl:when>
                   <!--xsl:when test="//req:requirement[req:category/req:primary = 'Domain Model']/req:entity//req:ref[@id = $key]">
                      "<xsl:value-of select="$sec_cat"/>" -&gt; "Global Entities (without categories)";
                   </xsl:when-->
                </xsl:choose>
             </xsl:otherwise>
          </xsl:choose>

      </xsl:for-each>

   </xsl:template>

   <!-- Requirements UML class diagram (every category and sub-category) -->

   <xsl:template match="uc:usecases" mode="dm_category">
      <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
         <xsl:if test="//req:requirement/req:category/req:secondary[../req:primary = 'Domain Model']">
            <xsl:variable name="sec_cat" select="."/>
            <xsl:call-template name="domain_model">
               <xsl:with-param name="secondary_category" select="."/>
               <xsl:with-param name="tertiary_category" select="''"/>
            </xsl:call-template>
            <xsl:for-each select="//req:requirement/req:category/req:tertiary[generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
               <xsl:if test="//req:requirement/req:category/req:tertiary[../req:primary = 'Domain Model' and ../req:secondary = $sec_cat]">
                  <xsl:call-template name="domain_model">
                     <xsl:with-param name="secondary_category" select="$sec_cat"/>
                     <xsl:with-param name="tertiary_category" select="."/>
                  </xsl:call-template>
               </xsl:if>
            </xsl:for-each>
         </xsl:if>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="req:entity" mode="dm_category">
       "<xsl:value-of select="../req:key"/>" [
                label = "{<xsl:value-of select="req:name"/>|<xsl:apply-templates select="req:attribute[not(req:objectreference)]"/>|<xsl:apply-templates select="req:attribute[req:objectreference]"/>}"
        ]

      <xsl:for-each select="req:attribute[req:objectreference]">
         <xsl:variable name="dm_id" select="req:objectreference/req:ref/@id"/>

         <!-- only show entities, if referenced entity is within documents scope (referenced in root file) -->
         <xsl:if test="//req:requirement[req:key = $dm_id]/req:entity">
            <xsl:apply-templates select="//req:requirement[req:key = $dm_id]/req:entity"/>

           <xsl:call-template name="create_edge">
              <xsl:with-param name="link_from"  select="../../req:key"/>
              <xsl:with-param name="link_to"    select="$dm_id"/>
              <xsl:with-param name="link_end"   select="req:objectreference/req:linkend"/>
              <xsl:with-param name="link_start" select="req:objectreference/req:linkstart"/>
           </xsl:call-template>
        </xsl:if>

      </xsl:for-each>
   </xsl:template>

   <xsl:template name="domain_model">
      <xsl:param name="secondary_category"/>
      <xsl:param name="tertiary_category"/>
      <xsl:variable name="file">
         <xsl:choose>
            <xsl:when test="$tertiary_category = ''"><xsl:value-of
                  select="$imagedir"/>/<xsl:value-of
                  select="$secondary_category"/>_domain_model.dot</xsl:when>
            <xsl:otherwise><xsl:value-of
                  select="$imagedir"/>/<xsl:value-of
                  select="$secondary_category"/>_<xsl:value-of
                  select="$tertiary_category"/>_domain_model.dot</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>


       <redirect:write file="{$file}">

digraph G {
    fontname = "Sans"
    fontsize = 8

    node [
            fontname = "Sans",
            fontsize = 8,
            shape = "record",
            fillcolor = "#EEEEEE",
            style = "filled"
    ]

    edge [
            fontname = "Sans"
            fontsize = 8
    ]

    <xsl:if test="$tertiary_category = ''">
       <xsl:apply-templates select="//req:entity[../req:category/req:secondary = $secondary_category]" mode="dm_category"/>
    </xsl:if>
    <xsl:if test="not($tertiary_category = '')">
       <xsl:apply-templates select="//req:entity[../req:category/req:secondary = $secondary_category and ../req:category/req:tertiary = $tertiary_category]" mode="dm_category"/>
    </xsl:if>
}
       </redirect:write>
   </xsl:template>

   <!-- Requirements UML class diagram -->

   <xsl:template match="req:requirement">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/<xsl:value-of
         select="req:key"/>.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Sans"
    fontsize = 8

    node [
            fontname = "Sans",
            fontsize = 8,
            shape = "record",
            fillcolor = "#EEEEEE",
            style = "filled"
    ]

    edge [
            fontname = "Sans"
            fontsize = 8
    ]

    <xsl:apply-templates select="req:entity"/>

    <xsl:apply-templates select="req:entity" mode="others">
       <xsl:with-param name="dm_root_id" select="req:key"/>
    </xsl:apply-templates>


}
       </redirect:write>
   </xsl:template>

   <xsl:template match="req:entity">
      "<xsl:value-of select="../req:key"/>" [
                label = "{<xsl:value-of select="req:name"/>|<xsl:apply-templates select="req:attribute[not(req:objectreference)]"/>|<xsl:apply-templates select="req:attribute[req:objectreference]"/>}"
        ]
   </xsl:template>

   <xsl:template match="req:entity" mode="others">
      <xsl:param name="dm_root_id"/>
      <xsl:for-each select="req:attribute[req:objectreference]">
         <xsl:variable name="dm_id" select="req:objectreference/req:ref/@id"/>
         <xsl:apply-templates select="//req:requirement[req:key = $dm_id]/req:entity"/>

        <xsl:call-template name="create_edge">
           <xsl:with-param name="link_from"  select="../../req:key"/>
           <xsl:with-param name="link_to"    select="$dm_id"/>
           <xsl:with-param name="link_end"   select="req:objectreference/req:linkend"/>
           <xsl:with-param name="link_start" select="req:objectreference/req:linkstart"/>
        </xsl:call-template>

      </xsl:for-each>

      <xsl:for-each select="//req:objectreference[req:ref/@id = $dm_root_id]">
         <xsl:variable name="dm_id" select="../../../req:key"/>
         <xsl:apply-templates select="//req:requirement[req:key = $dm_id]/req:entity"/>

        <xsl:call-template name="create_edge">
           <xsl:with-param name="link_from"  select="$dm_id"/>
           <xsl:with-param name="link_to"    select="$dm_root_id"/>
           <xsl:with-param name="link_end"   select="req:linkend"/>
           <xsl:with-param name="link_start" select="req:linkstart"/>
        </xsl:call-template>

      </xsl:for-each>

      <xsl:for-each select="//req:objectreference[req:ref/@id = $dm_root_id or ../../../req:key = $dm_root_id]">
         <xsl:variable name="dm_id" select="../../../req:key"/>
         <xsl:variable name="ref_id" select="req:ref/@id"/>

         <xsl:if test="$dm_id = $dm_root_id">
            <xsl:for-each select="//req:objectreference[../../../req:key = $ref_id]">
               <xsl:call-template name="check_link">
                  <xsl:with-param name="referencing_node" select="$ref_id"/>
                  <xsl:with-param name="referenced_node" select="req:ref/@id"/>
                  <xsl:with-param name="dm_root_id" select="$dm_root_id"/>
               </xsl:call-template>
            </xsl:for-each>
         </xsl:if>
         <xsl:if test="$dm_id != $dm_root_id">
            <xsl:for-each select="//req:objectreference[../../../req:key = $dm_id and not(req:ref/@id = $dm_root_id)]">
               <xsl:call-template name="check_link">
                  <xsl:with-param name="referencing_node" select="$dm_id"/>
                  <xsl:with-param name="referenced_node" select="req:ref/@id"/>
                  <xsl:with-param name="dm_root_id" select="$dm_root_id"/>
               </xsl:call-template>
            </xsl:for-each>
         </xsl:if>
      </xsl:for-each>

   </xsl:template>

   <xsl:template name="check_link">
      <xsl:param name="referenced_node"/>
      <xsl:param name="referencing_node"/>
      <xsl:param name="dm_root_id"/>

      <xsl:for-each select="//req:objectreference[../../../req:key = $referenced_node]">
         <xsl:if test="req:ref/@id = $dm_root_id">
           <xsl:call-template name="create_edge">
              <xsl:with-param name="link_from"  select="$referencing_node"/>
              <xsl:with-param name="link_to"    select="$referenced_node"/>
              <xsl:with-param name="link_end"   select="//req:objectreference[../../../req:key = $referencing_node and req:ref/@id = $referenced_node]/req:linkend"/>
              <xsl:with-param name="link_start" select="//req:objectreference[../../../req:key = $referencing_node and req:ref/@id = $referenced_node]/req:linkstart"/>
           </xsl:call-template>
         </xsl:if>
      </xsl:for-each>
      <xsl:for-each select="//req:objectreference[../../../req:key = $dm_root_id and req:ref/@id = $referenced_node]">
           <xsl:call-template name="create_edge">
              <xsl:with-param name="link_from"  select="$referencing_node"/>
              <xsl:with-param name="link_to"    select="$referenced_node"/>
              <xsl:with-param name="link_end"   select="//req:objectreference[../../../req:key = $referencing_node and req:ref/@id = $referenced_node]/req:linkend"/>
              <xsl:with-param name="link_start" select="//req:objectreference[../../../req:key = $referencing_node and req:ref/@id = $referenced_node]/req:linkstart"/>
           </xsl:call-template>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="create_edge">
      <xsl:param name="link_from"/>
      <xsl:param name="link_to"/>
      <xsl:param name="link_start"/>
      <xsl:param name="link_end"/>
      <xsl:param name="link_arrowhead" select="'none'"/>
      edge [
                arrowhead = "<xsl:value-of select="$link_arrowhead"/>"

                headlabel = "<xsl:value-of select="$link_end"/>"
                taillabel = "<xsl:value-of select="$link_start"/>"
                ]

      "<xsl:value-of select="$link_from"/>" -&gt; "<xsl:value-of select="$link_to"/>";
   </xsl:template>

   <xsl:template match="req:attribute[not(req:objectreference)]"><xsl:text>+ </xsl:text><xsl:value-of select="req:name"/><xsl:text> : </xsl:text><xsl:value-of select="normalize-space(req:pattern)"/><xsl:text>\l</xsl:text></xsl:template>
   <xsl:template match="req:attribute[req:objectreference]"><xsl:text>+ </xsl:text><xsl:value-of select="req:name"/><xsl:text> : </xsl:text><xsl:value-of select="req:objectreference/req:linkstart"/>/<xsl:value-of select="req:objectreference/req:linkend"/><xsl:text>\l</xsl:text></xsl:template>

   <!-- Use Case flow diagram -->

   <xsl:template match="uc:usecase">

     <xsl:choose>
       <xsl:when test="@suppress_diagrams = 'true'"/>
       <xsl:otherwise>
	      <xsl:variable name="file"><xsl:value-of
	         select="$imagedir"/>/<xsl:value-of
	         select="@id"/>.dot</xsl:variable>

	      <redirect:write file="{$file}">

digraph G {
    graph [rankdir = TB, center = true, fontsize=12];
    edge [fontname="Sans",fontsize=12,labelfontname="Sans",labelfontsize=12];
    node [fontname="Sans",fontsize=12];
    bgcolor = "#dfdfff";

    subgraph cluster0 {
       node [style=filled];
       color=lightgrey;

   <xsl:apply-templates select="uc:actors"/>
        label = "<xsl:value-of select="$strActors"/>";
    }
   label = "<xsl:value-of select="@id"/><xsl:text> </xsl:text><xsl:value-of select="uc:name"/>";

   <xsl:apply-templates select="uc:success"/>

   <xsl:apply-templates select="uc:extension"/>

   <xsl:call-template name="display_referents">
      <xsl:with-param name="usecase_id" select="@id"/>
   </xsl:call-template>
}

       </redirect:write>
      </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template match="uc:actors">
     <xsl:apply-templates select="uc:primary"/>
     <xsl:apply-templates select="uc:secondary"/>
   </xsl:template>

   <xsl:template match="uc:primary">
     "<xsl:value-of select="uc:name"/>" [shape=box]; /* actor */
     "<xsl:value-of select="uc:name"/>" -&gt; "<xsl:value-of select="../../@id"/>"
   </xsl:template>

   <xsl:template match="uc:secondary">
     "<xsl:value-of select="uc:name"/>" [shape=box]; /* actor */
     "<xsl:value-of select="uc:name"/>" -&gt; "<xsl:value-of select="../../@id"/>"
   </xsl:template>


   <xsl:template match="uc:success">
     <xsl:apply-templates select="uc:step" mode="list"/>
     <xsl:apply-templates select="uc:step" mode="success_sequence"/>
     <xsl:apply-templates select="uc:step"/>
   </xsl:template>

   <xsl:template match="uc:extension">
     "<xsl:value-of select="../@id"/>-<xsl:value-of select="@id"/>" [
         shape = "record",
         style = "rounded",
         fillcolor = "#c0c0c0",
         style = "filled",
         label = "{<xsl:value-of select="../@id"/>-<xsl:value-of select="@id"/>|<xsl:value-of select="@name"/>}"
         ];
     <xsl:apply-templates select="uc:step"/>
   </xsl:template>

   <xsl:template match="uc:step" mode="list">
     "<xsl:value-of select="../../@id"/>-<xsl:value-of select="@id"/>" [
         shape = "record",
         style = "rounded",
         fillcolor = "#EEEEEE",
         style = "filled",
         label = "{<xsl:value-of select="../../@id"/>-<xsl:value-of select="@id"/>|<xsl:value-of select="@desc"/>}"
      ];
   </xsl:template>

   <xsl:template match="uc:step" mode="success_sequence">
     <xsl:variable name="prev" select="position() - 1"/>
     <xsl:variable name="this_uc_id" select="../../@id"/>
     <xsl:choose>
        <xsl:when test="$prev != 0">
           "<xsl:value-of select="../../@id"/>-<xsl:value-of
                 select="../uc:step[position() = $prev]/@id"/>" -> "<xsl:value-of
                 select="../../@id"/>-<xsl:value-of select="@id"/>";
         </xsl:when>
         <xsl:otherwise>
            "<xsl:value-of select="../../@id"/>" -> "<xsl:value-of select="../../@id"/>-1";
         </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template match="uc:step">
     <xsl:variable name="next" select="position() + 1"/>
     <xsl:apply-templates select="descendant-or-self::uc:ref[starts-with(@id,'UC')]">
        <xsl:with-param name="next" select="$next"/>
     </xsl:apply-templates>
   </xsl:template>

   <xsl:template match="uc:success/uc:step//uc:ref">
      <xsl:param name="next"/>
     <!-- if you are coming from the basic path, the actor is in the relationship, otherwise the underlying extension path -->
      <xsl:choose>
         <xsl:when test="contains(@id, '-E')">
            <!-- extension relation from extension to use case -->
            <xsl:variable name="description">
               <xsl:call-template name="lookup_desc_of_extension">
                  <xsl:with-param name="destination" select="@id"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="target_name">
               <xsl:call-template name="lookup_name_only">
                  <xsl:with-param name="key" select="@id"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:if test="not(substring-before(@id,'-E') = ancestor-or-self::uc:usecase/@id)">
               "<xsl:value-of select="@id"/>" [
                   shape = "record",
                   style = "rounded",
                   fillcolor = "#c0c0c0",
                   style = "filled",
                   label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="$target_name"/>}"
                  ];
            </xsl:if>
            "<xsl:value-of select="ancestor-or-self::uc:usecase/@id"/>-<xsl:value-of select="ancestor-or-self::uc:step/@id"/>" -&gt; "<xsl:value-of select="@id"/>" [headlabel = "<xsl:value-of select="$description"/>"]

            <xsl:if test="@actor">
               "<xsl:value-of select="@actor"/>" -&gt; "<xsl:value-of select="@id"/>"
            </xsl:if>
        </xsl:when>
        <xsl:otherwise>
           <xsl:variable name="source_name">
               <xsl:call-template name="lookup_name_only">
                  <xsl:with-param name="key" select="@id"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:if test="not(substring-before(@id,'-E') = ancestor-or-self::uc:usecase/@id)">
           "<xsl:value-of select="@id"/>" [
                shape = "record",
                style = "rounded",
                fillcolor = "#EEEEEE",
                style = "filled",
                label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="$source_name"/>}"
               ];
            </xsl:if>
           "<xsl:value-of select="ancestor-or-self::uc:usecase/@id"/>-<xsl:value-of select="ancestor-or-self::uc:step/@id"/>" -&gt; "<xsl:value-of select="@id"/>"
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>;</xsl:text>
   </xsl:template>

   <xsl:template match="uc:extension/uc:step//uc:ref">
     <!-- if you are coming from the basic path, the actor is in the relationship, otherwise the underlying extension path -->
      <xsl:choose>
        <xsl:when test="contains(@id, '-E')">
           <xsl:variable name="description">
               <xsl:call-template name="lookup_desc_of_extension">
                  <xsl:with-param name="destination" select="@id"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="target_name">
               <xsl:call-template name="lookup_name_only">
                  <xsl:with-param name="key" select="@id"/>
               </xsl:call-template>
            </xsl:variable>
           "<xsl:value-of select="@id"/>" [
                shape = "record",
                style = "rounded",
                fillcolor = "#c0c0c0",
                style = "filled",
                label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="$target_name"/>}"
               ];
           "<xsl:value-of select="ancestor-or-self::uc:usecase/@id"/>-<xsl:value-of select="ancestor-or-self::uc:extension/@id"/>" -&gt; "<xsl:value-of select="@id"/>" [headlabel = "<xsl:value-of select="$description"/>"]
           <!-- relation from 'secondary actor' to extension path -->
           <xsl:if test="@actor">
              "<xsl:value-of select="@actor"/>" -&gt; "<xsl:value-of select="@id"/>";
           </xsl:if>
        </xsl:when>
        <xsl:otherwise>
           <xsl:variable name="source_name">
               <xsl:call-template name="lookup_name_only">
                  <xsl:with-param name="key" select="@id"/>
               </xsl:call-template>
            </xsl:variable>
          "<xsl:value-of select="@id"/>" [
                shape = "record",
                style = "rounded",
                fillcolor = "#EEEEEE",
                style = "filled",
                label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="$source_name"/>}"
               ];
          "<xsl:value-of select="ancestor-or-self::uc:usecase/@id"/>-<xsl:value-of select="ancestor-or-self::uc:extension/@id"/>" -&gt; "<xsl:value-of select="@id"/>"
        </xsl:otherwise>
     </xsl:choose>
      <xsl:text>;</xsl:text>
   </xsl:template>

   <xsl:template name="display_referents">
      <xsl:param name="usecase_id"/>
      <xsl:if test="//uc:usecase//uc:step//uc:ref[(@id = $usecase_id or contains(@id,concat($usecase_id, '-'))) and not(ancestor-or-self::uc:usecase/@id = $usecase_id)]">
         <xsl:for-each select="//uc:usecase//uc:step//uc:ref[(@id = $usecase_id or contains(@id,concat($usecase_id, '-'))) and not(ancestor-or-self::uc:usecase/@id = $usecase_id)]">
            <xsl:variable name="destination">
               <xsl:call-template name="lookup_name">
                  <xsl:with-param name="key" select="@id"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="source_id">
               <xsl:if test="ancestor-or-self::uc:usecase"><xsl:value-of select="ancestor-or-self::uc:usecase/@id"/></xsl:if>
               <xsl:if test="ancestor-or-self::uc:extension"><xsl:text>-</xsl:text><xsl:value-of select="ancestor-or-self::uc:extension/@id"/></xsl:if>
               <xsl:if test="ancestor-or-self::uc:step"><xsl:text>-</xsl:text><xsl:value-of select="ancestor-or-self::uc:step/@id"/></xsl:if>
            </xsl:variable>
            <xsl:variable name="source">
               <xsl:call-template name="lookup_name">
                  <xsl:with-param name="key" select="$source_id"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="source_name">
               <xsl:call-template name="lookup_name_only">
                  <xsl:with-param name="key" select="$source_id"/>
               </xsl:call-template>
            </xsl:variable>
            "<xsl:value-of select="$source"/>" [
                shape = "record",
                style = "rounded",
                <xsl:choose><xsl:when test="contains($source_id,'-E')">fillcolor = "#c0c0c0"</xsl:when><xsl:otherwise>fillcolor = "#EEEEEE"</xsl:otherwise></xsl:choose>,
                style = "filled",
                label = "{<xsl:value-of select="$source_id"/>|<xsl:value-of select="$source_name"/>}"
               ];
            "<xsl:value-of select="$source"/>" -> "<xsl:value-of select="$destination"/>";
         </xsl:for-each>
      </xsl:if>
   </xsl:template>

   <xsl:template name="lookup_name">
     <xsl:param name="key"/>
     <xsl:variable name="from_uc">
        <xsl:value-of select="concat('UC-', substring-before(substring-after($key, '-'), '-'))"/>
     </xsl:variable>
     <xsl:choose>
        <xsl:when test="not(starts-with($key, $from_uc))">
           <xsl:value-of select="$key"/>
        </xsl:when>
        <xsl:when test="contains($key,'-E')">
           <xsl:variable name="ext" select="concat('E', substring-after($key, '-E'))"/>
           <xsl:value-of select="$key"/>
        </xsl:when>
        <xsl:when test="not(contains(substring-after($key,'UC-'),'-'))">
           <xsl:value-of select="$key"/>
        </xsl:when>
        <xsl:otherwise>
           <xsl:variable name="ext" select="substring-after(substring-after($key, '-'), '-')"/>
           <xsl:value-of select="$key"/>
        </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template name="lookup_name_only">
     <xsl:param name="key"/>
     <xsl:variable name="from_uc">
        <xsl:choose>
           <xsl:when test="contains(substring-after($key, '-'), '-')">
              <xsl:value-of select="concat('UC-', substring-before(substring-after($key, '-'), '-'))"/>
           </xsl:when>
           <xsl:otherwise>
              <xsl:value-of select="concat('UC-', substring-after($key, '-'))"/>
           </xsl:otherwise>
        </xsl:choose>
     </xsl:variable>
     <xsl:choose>
        <xsl:when test="not(starts-with($key, $from_uc))">
           <xsl:value-of select="$key"/>
        </xsl:when>
        <xsl:when test="contains($key,'-E')">
           <xsl:variable name="ext" select="concat('E', substring-after($key, '-E'))"/>
           <xsl:choose>
              <xsl:when test="contains($ext,'-')">
                 <xsl:variable name="ext_id" select="substring-before($ext, '-')"/>
                 <xsl:variable name="ext_step" select="substring-after($ext, '-')"/>
                 <xsl:value-of select="//uc:usecase[@id = $from_uc]/uc:extension[@id = $ext_id]/@name"/>
              </xsl:when>
              <xsl:otherwise>
                 <xsl:value-of select="//uc:usecase[@id = $from_uc]/uc:extension[@id = $ext]/@name"/>
              </xsl:otherwise>
           </xsl:choose>
        </xsl:when>
        <xsl:when test="not(contains(substring-after($key,'UC-'),'-'))">
           <xsl:value-of select="//uc:usecase[@id = $from_uc]/uc:name"/>
        </xsl:when>
        <xsl:otherwise>
           <xsl:variable name="ext" select="substring-after(substring-after($key, '-'), '-')"/>
           <xsl:value-of select="//uc:usecase[@id = $from_uc]/uc:success/uc:step[@id = $ext]/@desc"/>
        </xsl:otherwise>
     </xsl:choose>
   </xsl:template>
   
   <xsl:template name="lookup_desc_of_extension">
      <xsl:param name="destination"/>
      <xsl:variable name="uc_id">
         <xsl:value-of select="substring-before(substring-after($destination, 'UC-'), '-')"/>    
      </xsl:variable>
      <xsl:variable name="ext_id_bulk">
         <xsl:value-of select="substring-after(substring-after($destination, concat('UC-', $uc_id)), '-')"/>    
      </xsl:variable>
      <xsl:variable name="ext_id">
         <xsl:choose>
           <xsl:when test="contains($ext_id_bulk, '-')">
              <xsl:value-of select="substring-before($ext_id_bulk, '-')"/>    
           </xsl:when>
           <xsl:otherwise>
              <xsl:value-of select="$ext_id_bulk"/>
           </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:value-of select="//uc:usecase[@id = concat('UC-', $uc_id)]/uc:extension[$ext_id = @id]/@desc"/>
   </xsl:template>
   
   
   <!-- Usecase dependy diagram -->
   
   <xsl:template match="uc:usecases" mode="uc_dep">

      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/usecase_dependencies.dot</xsl:variable>

      <redirect:write file="{$file}">

digraph G {
    edge [fontname="Sans",fontsize=12,labelfontname="Sans",labelfontsize=12];
    node [fontname="Sans",fontsize=12];
    bgcolor = "#dfdfff";

    label = "<xsl:value-of select="'Use Case Dependencies'"/>";

    
      <xsl:apply-templates select="//uc:usecase" mode="uc_dep_list_uc"/>
      
      <xsl:apply-templates select="//uc:usecase" mode="uc_dep_ref_out"/>
      <xsl:apply-templates select="//uc:usecase" mode="uc_dep_ref_precondition"/>
}

      </redirect:write>
   </xsl:template>
   
   <xsl:template match="uc:usecase" mode="uc_dep_list_uc">
      <xsl:variable name="uc_id" select="@id"/>
      
      <!-- find out whether this usecase is referred by another UC or referring to another one. -->
      <xsl:variable name="referred">
         <xsl:for-each select="key('usecase-references-out', $uc_id)">
            <xsl:variable name="from_uc">
               <xsl:call-template name="get_uc_name">
                  <xsl:with-param name="key" select="@id"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:if test="not($from_uc = $uc_id)">
               true
            </xsl:if>
         </xsl:for-each>
         <xsl:for-each select="//uc:ref[not(ancestor-or-self::uc:usecase/@id = substring-after($uc_id, 'UC-'))]">
            <xsl:if test="ancestor-or-self::uc:usecase/@id = substring-after($uc_id, 'UC-')">
               true
            </xsl:if>
         </xsl:for-each>
      </xsl:variable>
      
      <!-- only create a node, if usecase has a reference to another usecase -->
      <xsl:if test="not(normalize-space($referred) = '')">
         "<xsl:value-of select="@id"/>" [
            shape = "record",
            style = "rounded",
            fillcolor = "#EEEEEE",
            style = "filled",
            label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="uc:name"/>}"
         ];
      </xsl:if>
   </xsl:template>
   
   <xsl:template match="uc:usecase" mode="uc_dep_ref_out">
      <xsl:variable name="uc_id" select="@id"/>
      <xsl:for-each select="key('usecase-references-out', $uc_id)">
         <xsl:variable name="from_uc">
           <xsl:choose>
              <xsl:when test="contains(substring-after(substring-after(@id, '-'), '-'), '-')">
                 <xsl:value-of select="concat('UC-', substring-before(substring-after(substring-after(@id, '-'), '-'), '-'))"/>
              </xsl:when>
              <xsl:when test="contains(substring-after(@id, '-'), '-')">
                 <xsl:value-of select="concat('UC-', substring-before(substring-after(@id, '-'), '-'))"/>
              </xsl:when>
              <xsl:otherwise>
                 <xsl:value-of select="concat('UC-', substring-after(@id, '-'))"/>
              </xsl:otherwise>
           </xsl:choose>
        </xsl:variable>
        <xsl:if test="not($uc_id = $from_uc)">
           "<xsl:value-of select="$uc_id"/>" -&gt; "<xsl:value-of select="$from_uc"/>";
        </xsl:if>
      </xsl:for-each> 
   </xsl:template>
   
   <xsl:template match="uc:usecase" mode="uc_dep_ref_precondition">
      <xsl:variable name="uc_id" select="@id"/>
      <xsl:for-each select="key('usecase-references-precondition', $uc_id)">
        <xsl:variable name="from_uc">
           <xsl:call-template name="get_uc_name">
              <xsl:with-param name="key" select="@id"/>
           </xsl:call-template>
        </xsl:variable>
        <xsl:if test="not($uc_id = $from_uc)">
           "<xsl:value-of select="$uc_id"/>" -&gt; "<xsl:value-of select="$from_uc"/>" [label = "precondition",style=dotted];
        </xsl:if>
      </xsl:for-each>
   </xsl:template>
   
   <xsl:template name="get_uc_name">
     <xsl:param name="key"/>
     <xsl:choose>
        <xsl:when test="contains(substring-after(substring-after(@id, '-'), '-'), '-')">
           <xsl:value-of select="concat('UC-', substring-before(substring-after(substring-after(@id, '-'), '-'), '-'))"/>
        </xsl:when>
        <xsl:when test="contains(substring-after(@id, '-'), '-')">
           <xsl:value-of select="concat('UC-', substring-before(substring-after(@id, '-'), '-'))"/>
        </xsl:when>
        <xsl:otherwise>
           <xsl:value-of select="concat('UC-', substring-after(@id, '-'))"/>
        </xsl:otherwise>
     </xsl:choose>
   </xsl:template>
   
   <!-- Usecase dependency diagram for single usecase -->
   <xsl:template match="uc:usecase" mode="uc_dep_single">

      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/<xsl:value-of
         select="concat(@id, '-dependencies')"/>.dot</xsl:variable>

      <redirect:write file="{$file}">

digraph G {
    edge [fontname="Sans",fontsize=12,labelfontname="Sans",labelfontsize=12];
    node [fontname="Sans",fontsize=12];
    bgcolor = "#dfdfff";

    label = "<xsl:value-of select="concat(@id, ' - Use Case Dependencies')"/>";
    
    "<xsl:value-of select="@id"/>" [
            shape = "record",
            style = "rounded",
            fillcolor = "#EEEEEE",
            style = "filled",
            label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="uc:name"/>}"
     ];
     
      <xsl:variable name="uc_id" select="@id"/>
      <xsl:for-each select="//uc:usecase[not(@id = $uc_id)]">
         <xsl:call-template name="uc_dep_single_list_uc">
            <xsl:with-param name="uc_id" select="$uc_id"/>
         </xsl:call-template>
      </xsl:for-each>
      
     
      <!-- 
         <xsl:apply-templates select="//uc:usecase" mode="uc_dep_ref_precondition"/>
       -->
}

      </redirect:write>
   </xsl:template>
   
   <xsl:template name="uc_dep_single_list_uc">
      <xsl:param name="uc_id"/>
      <xsl:variable name="this_uc_id" select="@id"/>
      
      <!-- find out whether this usecase is referred by another UC or referring to another one. -->
      <xsl:variable name="referred_out">
         <xsl:for-each select="key('usecase-references-out', $this_uc_id)">
            <xsl:variable name="from_uc">
               <xsl:call-template name="get_uc_name">
                  <xsl:with-param name="key" select="@id"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$from_uc = $uc_id">
               true
            </xsl:if>
         </xsl:for-each>
      </xsl:variable>
      <xsl:variable name="referred_in">
         <xsl:for-each select="//uc:ref[not(ancestor-or-self::uc:usecase/@id = $uc_id)]">
            <xsl:if test="ancestor-or-self::uc:usecase/@id = $uc_id">
               true
            </xsl:if>
         </xsl:for-each>
      </xsl:variable>
      
      <!-- only create a node, if usecase has a reference to another usecase -->
      <xsl:if test="not(normalize-space($referred_in) = '') or not(normalize-space($referred_out) = '')">
         "<xsl:value-of select="@id"/>" [
            shape = "record",
            style = "rounded",
            fillcolor = "#EEEEEE",
            style = "filled",
            label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="uc:name"/>}"
         ];
      </xsl:if>
      
      <xsl:if test="not(normalize-space($referred_in) = '')">
         "<xsl:value-of select="$uc_id"/>" -&gt; "<xsl:value-of select="$this_uc_id"/>"
      </xsl:if>
      <xsl:if test="not(normalize-space($referred_out) = '')">
         "<xsl:value-of select="$this_uc_id"/>" -&gt; "<xsl:value-of select="$uc_id"/>"
      </xsl:if>
      
   </xsl:template>

</xsl:stylesheet>
