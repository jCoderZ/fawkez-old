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
   <xsl:param name="lang" select="/uc:usecases/uc:info/@lang"/>

   <xsl:output method="text" encoding="UTF-8"/>

   <xsl:include href="usecase_i18n.xsl"/>

   <xsl:key name="unique-usecase-scope-key" match="uc:usecase/uc:scope" use="."/>

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
      <xsl:apply-templates select="/uc:usecases" mode="dm"/>
      <xsl:apply-templates select="/uc:usecases" mode="dm_category"/>
      <xsl:apply-templates select="/uc:usecases" mode="dm_global_cat"/>
      <xsl:apply-templates select="/uc:usecases" mode="roles"/>
      <xsl:apply-templates select="/uc:usecases" mode="roles_category"/>
      <xsl:apply-templates select="/uc:usecases" mode="uc_dep"/>
      <!--
      <xsl:apply-templates select="/uc:usecases" mode="uc_scope_dep"/>
       -->
      <xsl:apply-templates select="uc:usecase" mode="uc_dep_single"/>
      <xsl:apply-templates select="req:requirement[starts-with(req:category/req:primary, 'Role')]"
                           mode="role"/>
   </xsl:template>

   <!-- Roles Dependency and usecases diagram (complete) -->

   <xsl:template match="uc:usecases" mode="roles">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/roles_model<xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 8
    rankdir = "LR"
    penwidth = "0.0001"

    node [
            fontname = "Helvetica",
            fontsize = 8,
            shape = "record",
            style=filled,
            fillcolor="#EEEED1"
    ]

    edge [
            fontname = "Helvetica"
            fontsize = 8
            weight = 10
    ]

    <xsl:apply-templates select="/uc:usecases/req:requirement//req:role[not(../req:category/req:secondary)]" mode="complete">
       <xsl:with-param name="suppress_uc" select="'true'"/>
    </xsl:apply-templates>

    <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
       <xsl:variable name="sec_cat" select="."/>
       <xsl:if test="/uc:usecases/req:requirement//req:role[../req:category/req:secondary = $sec_cat and starts-with(../req:category/req:primary, 'Role')]">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$sec_cat"/>";
          <xsl:apply-templates select="/uc:usecases/req:requirement//req:role[../req:category/req:secondary = $sec_cat]" mode="complete">
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

      <xsl:for-each select="/uc:usecases/uc:usecase/uc:actors/uc:primary/uc:name">
         <xsl:variable name="actor_id" select="normalize-space(.)"/>
         <xsl:variable name="role_id" select="/uc:usecases/req:requirement[normalize-space(req:role/req:name) = normalize-space($actor_id)]/req:key"/>

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
         <xsl:variable name="superior_name" select="/uc:usecases/req:requirement//req:role/req:name[../../req:key = $superior_id]"/>
         <xsl:variable name="role_id" select="/uc:usecases/req:requirement[normalize-space(req:role/req:name) = $role_name]/req:key"/>

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
      <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
         <xsl:if test="/uc:usecases/req:requirement/req:category/req:secondary[starts-with(../req:primary, 'Role')]">
            <xsl:variable name="sec_cat" select="."/>
            <xsl:call-template name="roles_model">
               <xsl:with-param name="secondary_category" select="."/>
               <xsl:with-param name="tertiary_category" select="''"/>
            </xsl:call-template>
            <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:tertiary[generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
               <xsl:if test="/uc:usecases/req:requirement/req:category/req:tertiary[starts-with(../req:primary,'Role') and ../req:secondary = $sec_cat]">
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
                  select="$secondary_category"/>_roles_model<xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:when>
            <xsl:otherwise><xsl:value-of
                  select="$imagedir"/>/<xsl:value-of
                  select="$secondary_category"/>_<xsl:value-of
                  select="$tertiary_category"/>_roles_model<xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>


       <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 8
    rankdir = "LR"
    penwidth = "0.0001"

    node [
            fontname = "Helvetica",
            fontsize = 8,
            shape = "record",
            style=filled,
            fillcolor="#EEEED1"
    ]

    edge [
            fontname = "Helvetica"
            fontsize = 8
    ]

    <xsl:if test="$tertiary_category = ''">
       <xsl:if test="/uc:usecases/req:requirement//req:role[../req:category/req:secondary = $secondary_category and starts-with(../req:category/req:primary, 'Role')]">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$secondary_category"/>";
          <xsl:apply-templates select="/uc:usecases/req:requirement//req:role[../req:category/req:secondary = $secondary_category]" mode="complete"/>
          }
          <xsl:for-each select="/uc:usecases/req:requirement//req:role[not(../req:category/req:secondary = $secondary_category) and ../req:key = //req:role[../req:category/req:secondary = $secondary_category]/req:superior/req:ref/@id]">
             "<xsl:value-of select="../req:key"/>" [
                label = "<xsl:value-of select="normalize-space(req:name)"/>"
             ]
          </xsl:for-each>
       </xsl:if>
    </xsl:if>
    <xsl:if test="not($tertiary_category = '')">
       <xsl:if test="/uc:usecases/req:requirement//req:role[../req:category/req:secondary = $secondary_category and starts-with(../req:category/req:primary, 'Role')]">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$secondary_category"/>";
          <xsl:apply-templates select="/uc:usecases/req:requirement//req:role[../req:category/req:secondary = $secondary_category  and ../req:category/req:tertiary = $tertiary_category]" mode="complete"/>
          }
          <xsl:for-each select="/uc:usecases/req:requirement//req:role[not(../req:category/req:secondary = $secondary_category  and ../req:category/req:tertiary = $tertiary_category) and ../req:key = //req:role[../req:category/req:secondary = $secondary_category  and ../req:category/req:tertiary = $tertiary_category]/req:superior/req:ref/@id]">
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
         select="req:key"/><xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 8
    rankdir = "LR"
    penwidth = "0.0001"

    node [
            fontname = "Helvetica"
            fontsize = 8
            shape = "record"
            style="filled"
            fillcolor="#EEEED1"
    ]

    edge [
            fontname = "Helvetica"
            fontsize = 8
    ]

    <xsl:apply-templates select="req:role" mode="complete"/>

    <xsl:variable name="role_id" select="req:key"/>
    <xsl:for-each select="req:role/req:superior">
       <xsl:variable name="superior_id" select="req:ref/@id"/>
       "<xsl:value-of select="$superior_id"/>" [
                label = "<xsl:value-of select="/uc:usecases/req:requirement[req:key = $superior_id]/req:role/req:name"/>"
        ]
    </xsl:for-each>

    <xsl:for-each select="//req:superior[req:ref/@id = $role_id]">
       <xsl:variable name="subordinate_id" select="../../req:key"/>
       "<xsl:value-of select="$subordinate_id"/>" [
                label = "<xsl:value-of select="/uc:usecases/req:requirement[req:key = $subordinate_id]/req:role/req:name"/>"
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
         select="$imagedir"/>/domain_model<xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 8
    penwidth = "0.0001"

    node [
            fontname = "Helvetica",
            fontsize = 8,
            shape = "record",
            fillcolor = "#ffffbb",
            style = "filled"
    ]

    edge [
            fontname = "Helvetica"
            fontsize = 8
            weight = 10
    ]

    <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
       <xsl:variable name="sec_cat" select="."/>
       <xsl:if test="//req:entity[../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$sec_cat"/>";
          <xsl:apply-templates select="//req:entity[../req:category/req:secondary = $sec_cat]"/>
          }
       </xsl:if>
    </xsl:for-each>
    <xsl:apply-templates select="//req:entity[not(../req:category/req:secondary)]" mode="complete"/>
    <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
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
         <xsl:if test="/uc:usecases/req:requirement[req:key = $dm_id]/req:entity">
            <!-- xsl:apply-templates select="/uc:usecases/req:requirement[req:key = $dm_id]/req:entity"/-->

						<xsl:variable name="arrowhead">
						   <xsl:call-template name="derive_arrowhead">
						     <xsl:with-param name="link_start" select="req:objectreference/req:linkstart"/>
						   </xsl:call-template>
						</xsl:variable>

           <xsl:call-template name="create_edge">
              <xsl:with-param name="link_from"      select="../../req:key"/>
              <xsl:with-param name="link_to"        select="$dm_id"/>
              <xsl:with-param name="link_end"       select="req:objectreference/req:linkend"/>
              <xsl:with-param name="link_start"     select="req:objectreference/req:linkstart"/>
              <xsl:with-param name="link_arrowhead" select="$arrowhead"/>
           </xsl:call-template>
        </xsl:if>

      </xsl:for-each>
   </xsl:template>

   <!-- Requirements UML class diagram (global categories) -->

   <xsl:template match="uc:usecases" mode="dm_global_cat">
      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/domain_model_global_cat<xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 8
    penwidth = "0.0001"

    node [
            fontname = "Helvetica",
            fontsize = 8,
            shape = "record",
            fillcolor = "#ffffbb",
            style = "filled"
    ]

    edge [
            fontname = "Helvetica"
            fontsize = 8
            weight = 10
    ]

    <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
       <xsl:variable name="sec_cat" select="."/>
       <xsl:if test="//req:entity[../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']">
          subgraph cluster<xsl:value-of select="position()"/> {
             label = "<xsl:value-of select="$sec_cat"/>";
             <xsl:variable name="pos" select="position()"/>

             <xsl:choose>
                <xsl:when test="/uc:usecases/req:requirement/req:category/req:tertiary[../req:secondary = $sec_cat and ../req:primary = 'Domain Model']">
                   <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:tertiary[../req:secondary = $sec_cat and generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
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

    <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
       <xsl:variable name="sec_cat" select="."/>
       <xsl:if test="//req:entity[../req:category/req:secondary = $sec_cat and ../req:category/req:primary = 'Domain Model']">
          <xsl:variable name="pos" select="position()"/>

          <xsl:choose>
             <xsl:when test="/uc:usecases/req:requirement/req:category/req:tertiary[../req:secondary = $sec_cat and ../req:primary = 'Domain Model']">
                <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:tertiary[../req:secondary = $sec_cat and generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
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

      <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:secondary[../req:primary = 'Domain Model' and not(. = $sec_cat_in)]">
         <xsl:variable name="sec_cat" select="."/>
         <xsl:variable name="key" select="../../req:key"/>

         <xsl:choose>
             <xsl:when test="../req:tertiary[not(. = $ter_cat_in)]">
                <xsl:for-each select="../req:tertiary[not(. = $ter_cat_in)]">
                   <xsl:variable name="ter_cat" select="."/>
                   <xsl:choose>
                      <xsl:when test="/uc:usecases/req:requirement[req:category/req:primary = 'Domain Model' and req:category/req:secondary = $sec_cat_in and req:category/req:tertiary = $ter_cat_in]/req:entity//req:ref[@id = $key]">
                         "<xsl:value-of select="$sec_cat"/>_<xsl:value-of select="$ter_cat"/>" -&gt; "<xsl:value-of select="$sec_cat_in"/>_<xsl:value-of select="$ter_cat_in"/>";
                      </xsl:when>
                      <xsl:when test="/uc:usecases/req:requirement[req:category/req:primary = 'Domain Model' and req:category/req:secondary = $sec_cat_in]/req:entity//req:ref[@id = $key]">
                         "<xsl:value-of select="$sec_cat"/>_<xsl:value-of select="$ter_cat"/>" -&gt; "<xsl:value-of select="$sec_cat_in"/>";
                      </xsl:when>
                      <!-- xsl:when test="/uc:usecases/req:requirement[req:category/req:primary = 'Domain Model']/req:entity//req:ref[@id = $key]">
                         "<xsl:value-of select="$sec_cat"/>_<xsl:value-of select="$ter_cat"/>" -&gt; "Global Entities (without categories)";
                      </xsl:when -->
                   </xsl:choose>
                </xsl:for-each>
             </xsl:when>
             <xsl:otherwise>
                <xsl:choose>
                   <xsl:when test="/uc:usecases/req:requirement[req:category/req:primary = 'Domain Model' and req:category/req:secondary = $sec_cat_in and req:category/req:tertiary = $ter_cat_in]/req:entity//req:ref[@id = $key]">
                      "<xsl:value-of select="$sec_cat"/>" -&gt; "<xsl:value-of select="$sec_cat_in"/>_<xsl:value-of select="$ter_cat_in"/>";
                   </xsl:when>
                   <xsl:when test="/uc:usecases/req:requirement[req:category/req:primary = 'Domain Model' and req:category/req:secondary = $sec_cat_in]/req:entity//req:ref[@id = $key]">
                      "<xsl:value-of select="$sec_cat"/>" -&gt; "<xsl:value-of select="$sec_cat_in"/>";
                   </xsl:when>
                   <!--xsl:when test="/uc:usecases/req:requirement[req:category/req:primary = 'Domain Model']/req:entity//req:ref[@id = $key]">
                      "<xsl:value-of select="$sec_cat"/>" -&gt; "Global Entities (without categories)";
                   </xsl:when-->
                </xsl:choose>
             </xsl:otherwise>
          </xsl:choose>

      </xsl:for-each>

   </xsl:template>

   <!-- Requirements UML class diagram (every category and sub-category) -->

   <xsl:template match="uc:usecases" mode="dm_category">
      <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">
         <xsl:if test="/uc:usecases/req:requirement/req:category/req:secondary[../req:primary = 'Domain Model']">
            <xsl:variable name="sec_cat" select="."/>
            <xsl:call-template name="domain_model">
               <xsl:with-param name="secondary_category" select="."/>
               <xsl:with-param name="tertiary_category" select="''"/>
            </xsl:call-template>
            <xsl:for-each select="/uc:usecases/req:requirement/req:category/req:tertiary[generate-id() = generate-id(key('unique-category-tertiary-key', .))]">
               <xsl:if test="/uc:usecases/req:requirement/req:category/req:tertiary[../req:primary = 'Domain Model' and ../req:secondary = $sec_cat]">
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
         <xsl:if test="/uc:usecases/req:requirement[req:key = $dm_id]/req:entity">
            <xsl:apply-templates select="/uc:usecases/req:requirement[req:key = $dm_id]/req:entity"/>

						<xsl:variable name="arrowhead">
						   <xsl:call-template name="derive_arrowhead">
						     <xsl:with-param name="link_start" select="req:objectreference/req:linkstart"/>
						   </xsl:call-template>
						</xsl:variable>

           <xsl:call-template name="create_edge">
              <xsl:with-param name="link_from"      select="../../req:key"/>
              <xsl:with-param name="link_to"        select="$dm_id"/>
              <xsl:with-param name="link_end"       select="req:objectreference/req:linkend"/>
              <xsl:with-param name="link_start"     select="req:objectreference/req:linkstart"/>
              <xsl:with-param name="link_arrowhead" select="$arrowhead"/>
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
                  select="$secondary_category"/>_domain_model<xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:when>
            <xsl:otherwise><xsl:value-of
                  select="$imagedir"/>/<xsl:value-of
                  select="$secondary_category"/>_<xsl:value-of
                  select="$tertiary_category"/>_domain_model<xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>


       <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 8
    penwidth = "0.0001"

    node [
            fontname = "Helvetica",
            fontsize = 8,
            shape = "record",
            fillcolor = "#ffffbb",
            style = "filled"
    ]

    edge [
            fontname = "Helvetica"
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
         select="req:key"/><xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:variable>

       <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 8
    penwidth = "0.0001"

    node [
            fontname = "Helvetica",
            fontsize = 8,
            shape = "record",
            fillcolor = "#ffffbb",
            style = "filled"
    ]

    edge [
            fontname = "Helvetica"
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

         <!-- Avoid a repeated self reference here -->
         <xsl:if test="$dm_id != $dm_root_id">
            <xsl:apply-templates select="/uc:usecases/req:requirement[req:key = $dm_id]/req:entity"/>
         </xsl:if>

						<xsl:variable name="arrowhead">
						   <xsl:call-template name="derive_arrowhead">
						     <xsl:with-param name="link_start" select="req:objectreference/req:linkstart"/>
						   </xsl:call-template>
						</xsl:variable>

        <xsl:call-template name="create_edge">
           <xsl:with-param name="link_from"      select="../../req:key"/>
           <xsl:with-param name="link_to"        select="$dm_id"/>
           <xsl:with-param name="link_end"       select="req:objectreference/req:linkend"/>
           <xsl:with-param name="link_start"     select="req:objectreference/req:linkstart"/>
           <xsl:with-param name="link_arrowhead" select="$arrowhead"/>
        </xsl:call-template>

      </xsl:for-each>

      <xsl:for-each select="//req:objectreference[req:ref/@id = $dm_root_id]">
         <xsl:variable name="dm_id" select="../../../req:key"/>

         <!-- Avoid a repeated self reference here -->
         <xsl:if test="$dm_id != $dm_root_id">
            <xsl:apply-templates select="/uc:usecases/req:requirement[req:key = $dm_id]/req:entity"/>

						<xsl:variable name="arrowhead">
						   <xsl:call-template name="derive_arrowhead">
						     <xsl:with-param name="link_start" select="req:linkstart"/>
						   </xsl:call-template>
						</xsl:variable>

              <xsl:call-template name="create_edge">
              <xsl:with-param name="link_from"      select="$dm_id"/>
              <xsl:with-param name="link_to"        select="$dm_root_id"/>
              <xsl:with-param name="link_end"       select="req:linkend"/>
              <xsl:with-param name="link_start"     select="req:linkstart"/>
              <xsl:with-param name="link_arrowhead" select="$arrowhead"/>
           </xsl:call-template>
        </xsl:if>

      </xsl:for-each>

      <!-- find out links from referring or referrenced entities of dm_root to each other -->
      <xsl:for-each select="//req:objectreference[req:ref/@id = $dm_root_id or ../../../req:key = $dm_root_id]">
         <xsl:variable name="dm_id" select="../../../req:key"/>
         <xsl:variable name="ref_id" select="req:ref/@id"/>

         <!-- look out for all entities, which are referenced by dm_root -->
         <xsl:if test="$dm_id = $dm_root_id">
            <xsl:for-each select="//req:objectreference[../../../req:key = $ref_id]">
               <!-- Avoid a repeated self reference here -->
               <xsl:if test="$ref_id != req:ref/@id">
                  <xsl:if test="$ref_id != $dm_root_id and req:ref/@id != $dm_root_id">
                     <xsl:call-template name="check_link">
                        <xsl:with-param name="referencing_node" select="$ref_id"/>
                        <xsl:with-param name="referenced_node" select="req:ref/@id"/>
                        <xsl:with-param name="dm_root_id" select="$dm_root_id"/>
                     </xsl:call-template>
                  </xsl:if>
               </xsl:if>
            </xsl:for-each>
         </xsl:if>
         <!-- look out for all entities (exclusive of dm_root) referring to dm_root -->
         <xsl:if test="$dm_id != $dm_root_id">
            <xsl:for-each select="//req:objectreference[../../../req:key = $dm_id and not(req:ref/@id = $dm_root_id)]">
               <xsl:if test="$dm_id != $dm_root_id and req:ref/@id = $dm_root_id">
                  <xsl:call-template name="check_link">
                     <xsl:with-param name="referencing_node" select="$dm_id"/>
                     <xsl:with-param name="referenced_node" select="req:ref/@id"/>
                     <xsl:with-param name="dm_root_id" select="$dm_root_id"/>
                  </xsl:call-template>
               </xsl:if>
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

            <xsl:variable name="target" select="//req:objectreference[../../../req:key = $referencing_node and req:ref/@id = $referenced_node]"/>

						<xsl:variable name="arrowhead">
						   <xsl:call-template name="derive_arrowhead">
						     <xsl:with-param name="link_start" select="$target/req:linkstart"/>
						   </xsl:call-template>
						</xsl:variable>

           <xsl:call-template name="create_edge">
              <xsl:with-param name="link_from"      select="$referencing_node"/>
              <xsl:with-param name="link_to"        select="$referenced_node"/>
              <xsl:with-param name="link_end"       select="$target/req:linkend"/>
              <xsl:with-param name="link_start"     select="$target/req:linkstart"/>
              <xsl:with-param name="link_arrowhead" select="$arrowhead"/>
           </xsl:call-template>
         </xsl:if>
      </xsl:for-each>
      <xsl:for-each select="//req:objectreference[../../../req:key = $dm_root_id and req:ref/@id = $referenced_node]">

           <xsl:variable name="target" select="//req:objectreference[../../../req:key = $referencing_node and req:ref/@id = $referenced_node]"/>

						<xsl:variable name="arrowhead">
						   <xsl:call-template name="derive_arrowhead">
						     <xsl:with-param name="link_start" select="$target/req:linkstart"/>
						   </xsl:call-template>
						</xsl:variable>

           <xsl:call-template name="create_edge">
              <xsl:with-param name="link_from"      select="$referencing_node"/>
              <xsl:with-param name="link_to"        select="$referenced_node"/>
              <xsl:with-param name="link_end"       select="$target/req:linkend"/>
              <xsl:with-param name="link_start"     select="$target/req:linkstart"/>
              <xsl:with-param name="link_arrowhead" select="$arrowhead"/>
           </xsl:call-template>
      </xsl:for-each>
   </xsl:template>

   <xsl:template name="derive_arrowhead">
      <xsl:param name="link_start"/>

      <xsl:choose>
        <xsl:when test="$link_start = 'extends'">
          <xsl:text>empty</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>normal</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
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
           select="@id"/><xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:variable>

        <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 14
    graph [rankdir = TB, center = true ];
    edge [fontname="Helvetica",fontsize=12,labelfontname="Helvetica",labelfontsize=12];
    node [fontname="Helvetica",fontsize=12];
    bgcolor = "#dee1e8";
    penwidth = "0.0001"

    subgraph cluster0 {
       rankdir=LR;
       node [style=filled, fillcolor="#EEEED1"];
       color=black;
       bgcolor="#dee1e8";
       fillcolor="#dee1e8";
       style = "filled";

   <xsl:apply-templates select="uc:actors"/>
        label = "<xsl:value-of select="$strActors"/>";
    }
   label = "\n<xsl:value-of select="@id"/><xsl:text> </xsl:text><xsl:value-of select="uc:name"/>";

   <xsl:apply-templates select="uc:success"/>

   <xsl:apply-templates select="uc:extension"/>

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
      <xsl:variable name="wrapped_name">
       <xsl:call-template name="wrap-name">
         <xsl:with-param name="name" select="@name" />
       </xsl:call-template>
      </xsl:variable>
     "<xsl:value-of select="../@id"/>-<xsl:value-of select="@id"/>" [
         shape = "record",
         style = "rounded",
         fillcolor = "#E2E0E0",
         style = "filled",
         label = "{<xsl:value-of select="../@id"/>-<xsl:value-of select="@id"/>|<xsl:value-of select="$wrapped_name"/>}"
         ];
     <xsl:apply-templates select="uc:step"/>
   </xsl:template>

   <xsl:template match="uc:step" mode="list">
      <xsl:variable name="wrapped_desc">
       <xsl:call-template name="wrap-name">
         <xsl:with-param name="name" select="@desc" />
       </xsl:call-template>
      </xsl:variable>
     "<xsl:value-of select="../../@id"/>-<xsl:value-of select="@id"/>" [
         shape = "record",
         style = "rounded",
         fillcolor = "#eaedf4",
         style = "filled",
         label = "{<xsl:value-of select="../../@id"/>-<xsl:value-of select="@id"/>|<xsl:value-of select="$wrapped_desc"/>}"
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
                   fillcolor = "#E2E0E0",
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
                fillcolor = "#eaedf4",
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
                fillcolor = "#E2E0E0",
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
                fillcolor = "#eaedf4",
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
      <xsl:if test="/uc:usecases/uc:usecase//uc:step//uc:ref[(@id = $usecase_id or contains(@id,concat($usecase_id, '-'))) and not(ancestor-or-self::uc:usecase/@id = $usecase_id)]">
         <xsl:for-each select="/uc:usecases/uc:usecase//uc:step//uc:ref[(@id = $usecase_id or contains(@id,concat($usecase_id, '-'))) and not(ancestor-or-self::uc:usecase/@id = $usecase_id)]">
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
                <xsl:choose><xsl:when test="contains($source_id,'-E')">fillcolor = "#E2E0E0"</xsl:when><xsl:otherwise>fillcolor = "#eaedf4"</xsl:otherwise></xsl:choose>,
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
                 <xsl:value-of select="/uc:usecases/uc:usecase[@id = $from_uc]/uc:extension[@id = $ext_id]/@name"/>
              </xsl:when>
              <xsl:otherwise>
                 <xsl:value-of select="/uc:usecases/uc:usecase[@id = $from_uc]/uc:extension[@id = $ext]/@name"/>
              </xsl:otherwise>
           </xsl:choose>
        </xsl:when>
        <xsl:when test="not(contains(substring-after($key,'UC-'),'-'))">
           <xsl:value-of select="/uc:usecases/uc:usecase[@id = $from_uc]/uc:name"/>
        </xsl:when>
        <xsl:otherwise>
           <xsl:variable name="ext" select="substring-after(substring-after($key, '-'), '-')"/>
           <xsl:value-of select="/uc:usecases/uc:usecase[@id = $from_uc]/uc:success/uc:step[@id = $ext]/@desc"/>
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
      <xsl:value-of select="/uc:usecases/uc:usecase[@id = concat('UC-', $uc_id)]/uc:extension[$ext_id = @id]/@desc"/>
   </xsl:template>


   <!-- Usecase dependency diagram -->

   <xsl:template match="uc:usecases" mode="uc_dep">

      <xsl:variable name="file"><xsl:value-of
         select="$imagedir"/>/usecase_dependencies<xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:variable>

      <redirect:write file="{$file}">

digraph G {

    fontname = "Helvetica"
    fontsize = 14
    graph [rankdir = "LR"]
    edge [fontname="Helvetica",fontsize=12,labelfontname="Helvetica",labelfontsize=12]
    node [fontname="Helvetica",fontsize=12, style="filled", fillcolor="#EEEED1"]
    bgcolor = "#dee1e8"
    penwidth = "0.0001"

    label = "\n<xsl:value-of select="$strUseCaseDependencies"/>";

    <xsl:apply-templates select="/uc:usecases/uc:usecase" mode="uc_dep_list_uc"/>
    <xsl:apply-templates select="/uc:usecases/uc:usecase" mode="uc_dep_ref_out"/>
    <xsl:apply-templates select="/uc:usecases/uc:usecase" mode="uc_dep_ref_precondition"/>
}

      </redirect:write>
   </xsl:template>

   <xsl:template match="uc:usecase" mode="uc_dep_list_uc">
      <xsl:variable name="uc_id" select="@id"/>

      <xsl:variable name="wrapped_name">
       <xsl:call-template name="wrap-name">
         <xsl:with-param name="name" select="uc:name" />
       </xsl:call-template>
      </xsl:variable>

      <xsl:choose>
         <xsl:when test="//uc:ref[not(ancestor-or-self::uc:usecase/@id = $uc_id) and
                       (@id = $uc_id or starts-with(@id, concat($uc_id, '-')))]">
            "<xsl:value-of select="@id"/>" [
               shape = "record",
               style = "rounded",
               fillcolor = "#eaedf4",
               style = "filled",
               label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="$wrapped_name"/>}"
            ];
         </xsl:when>
         <xsl:when test=".//uc:ref[not(@id = $uc_id) and not(starts-with(@id, concat($uc_id, '-')))]">
            "<xsl:value-of select="@id"/>" [
               shape = "record",
               style = "rounded",
               fillcolor = "#eaedf4",
               style = "filled",
               label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="$wrapped_name"/>}"
            ];
         </xsl:when>
      </xsl:choose>
   </xsl:template>

   <xsl:template match="uc:usecase" mode="uc_dep_ref_out">
      <xsl:variable name="uc_id" select="@id"/>

      <xsl:for-each select="/uc:usecases/uc:usecase[not(@id = $uc_id)]">
         <xsl:variable name="target_uc_id" select="@id"/>

         <xsl:if test="/uc:usecases/uc:usecase[@id = $uc_id]//uc:ref
                           [@id = $target_uc_id or starts-with(@id, concat($target_uc_id, '-'))]">
            "<xsl:value-of select="$uc_id"/>" -&gt; "<xsl:value-of select="$target_uc_id"/>";
         </xsl:if>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="uc:usecase" mode="uc_dep_ref_precondition">
      <xsl:variable name="uc_id" select="@id"/>

      <xsl:for-each select="/uc:usecases/uc:usecase[not(@id = $uc_id)]">
         <xsl:variable name="target_uc_id" select="@id"/>

         <xsl:if test="/uc:usecases/uc:usecase[@id = $uc_id]/uc:precondition//uc:ref
                           [@id = $target_uc_id or starts-with(@id, concat($target_uc_id, '-'))]">
                       "<xsl:value-of select="$uc_id"/>" -&gt; "<xsl:value-of select="$target_uc_id"/>" [label = "precondition",style=dotted];
         </xsl:if>
      </xsl:for-each>
   </xsl:template>

   <!-- scope depending usecase dependency diagrams -->
   <xsl:template match="uc:usecases" mode="uc_scope_dep">

      <xsl:for-each select="/uc:usecases/uc:usecase/uc:scope[generate-id() = generate-id(key('unique-usecase-scope-key', .))]">
         <xsl:variable name="file"><xsl:value-of
            select="$imagedir"/>/usecase_<xsl:value-of
            select="."/>_dependencies<xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:variable>

         <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 14
    graph [rankdir = "LR"]
    edge [fontname="Helvetica",fontsize=12,labelfontname="Helvetica",labelfontsize=12]
    node [fontname="Helvetica",fontsize=12, style="filled", fillcolor="#EEEED1"]
    bgcolor = "#dee1e8"
    penwidth = "0.0001"

    label = "\n<xsl:value-of select="$strUseCaseDependencies"/> '<xsl:value-of select="."/>'";

            <xsl:variable name="uc_scope" select="."/>

            <xsl:apply-templates select="/uc:usecases/uc:usecase[uc:scope = $uc_scope]"
                                 mode="uc_dep_list_uc"/>

            <xsl:apply-templates select="/uc:usecases/uc:usecase[uc:scope = $uc_scope]"
                                 mode="uc_dep_ref_out"/>
            <xsl:apply-templates select="/uc:usecases/uc:usecase[uc:scope = $uc_scope]"
                                 mode="uc_dep_ref_precondition"/>
}

         </redirect:write>
      </xsl:for-each>
   </xsl:template>


   <!-- helper methods -->

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
         select="concat(@id, '-dependencies')"/><xsl:value-of
         select="concat('_', $lang)"/>.dot</xsl:variable>

      <xsl:variable name="wrapped_name">
       <xsl:call-template name="wrap-name">
         <xsl:with-param name="name" select="uc:name" />
       </xsl:call-template>
      </xsl:variable>

      <redirect:write file="{$file}">

digraph G {
    fontname = "Helvetica"
    fontsize = 14
    graph[rankdir = "LR"]
    edge [fontname="Helvetica",fontsize=12,labelfontname="Helvetica",labelfontsize=12];
    node [fontname="Helvetica",fontsize=12, style="filled", fillcolor="#EEEED1"];
    bgcolor = "#dee1e8";
    penwidth = "0.0001"

    label = "\n<xsl:value-of select="concat(@id, concat(' - ', $strUseCaseDependencies))"/>";

    "<xsl:value-of select="@id"/>" [
            shape = "record",
            style = "rounded",
            fillcolor = "#eaedf4",
            style = "filled",
            label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="$wrapped_name"/>}"
     ];

      <xsl:variable name="uc_id" select="@id"/>
      <xsl:for-each select="/uc:usecases/uc:usecase[not(@id = $uc_id)]">
         <xsl:call-template name="uc_dep_single_list_uc">
            <xsl:with-param name="uc_id" select="$uc_id"/>
         </xsl:call-template>
      </xsl:for-each>


      <!--
         <xsl:apply-templates select="/uc:usecases/uc:usecase" mode="uc_dep_ref_precondition"/>
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
        <xsl:variable name="wrapped_name">
          <xsl:call-template name="wrap-name">
            <xsl:with-param name="name" select="uc:name" />
          </xsl:call-template>
        </xsl:variable>
         "<xsl:value-of select="@id"/>" [
            shape = "record",
            style = "rounded",
            fillcolor = "#eaedf4",
            style = "filled",
            label = "{<xsl:value-of select="@id"/>|<xsl:value-of select="$wrapped_name" />}"
         ];
      </xsl:if>

      <xsl:if test="not(normalize-space($referred_in) = '')">
         "<xsl:value-of select="$uc_id"/>" -&gt; "<xsl:value-of select="$this_uc_id"/>"
      </xsl:if>
      <xsl:if test="not(normalize-space($referred_out) = '')">
         "<xsl:value-of select="$this_uc_id"/>" -&gt; "<xsl:value-of select="$uc_id"/>"
      </xsl:if>

   </xsl:template>

   <xsl:template name="wrap-name">
     <xsl:param name="name" />
     <xsl:variable name="wrapped_name">
       <xsl:call-template name="word-wrap">
         <xsl:with-param name="tobewrapped" select="normalize-space($name)" />
         <xsl:with-param name="size" select="0" />
         <xsl:with-param name="indent" select="'\n'"/>
       </xsl:call-template>
     </xsl:variable>
     <xsl:value-of select="substring-after($wrapped_name,'\n')"/>
   </xsl:template>

   <xsl:template name="word-wrap">
      <xsl:param name="tobewrapped" />
      <xsl:param name="size" select="0" />
      <xsl:param name="indent" />
      <xsl:variable name="maxlength" select="26" />

      <xsl:choose>
         <xsl:when test="contains($tobewrapped,' ')">
            <xsl:variable name="word" select="substring-before($tobewrapped,' ')" />
            <xsl:variable name="length" select="string-length($word)" />
            <xsl:choose>
               <xsl:when test="$size=0">
                  <xsl:value-of select="$indent" />
                  <xsl:value-of select="$word" />
                  <xsl:call-template name="word-wrap">
                     <xsl:with-param name="tobewrapped" select="substring-after($tobewrapped,' ')" />
                     <xsl:with-param name="size" select="string-length(concat($indent,$word))" />
                     <xsl:with-param name="indent" select="$indent" />
                  </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:choose>
                     <xsl:when test="($size + $length + 1) > $maxlength">
                        <xsl:text></xsl:text>
                        <xsl:value-of select="$indent" />
                        <xsl:value-of select="$word" />
                        <xsl:call-template name="word-wrap">
                           <xsl:with-param name="tobewrapped"
                              select="substring-after($tobewrapped,' ')" />
                           <xsl:with-param name="size" select="string-length(concat($indent,$word))" />
                           <xsl:with-param name="indent" select="$indent" />
                        </xsl:call-template>
                     </xsl:when>
                     <xsl:otherwise>
                        <xsl:value-of select="concat(' ',$word)" />
                        <xsl:call-template name="word-wrap">
                           <xsl:with-param name="tobewrapped"
                              select="substring-after($tobewrapped,' ')" />
                           <xsl:with-param name="size" select="$size + 1 + string-length($word)" />
                           <xsl:with-param name="indent" select="$indent" />
                        </xsl:call-template>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:when>
         <xsl:otherwise>
            <xsl:choose>
               <xsl:when test="$size=0">
                  <xsl:value-of select="$indent" />
               </xsl:when>
               <xsl:otherwise>
                  <xsl:choose>
                     <xsl:when test="$size + string-length($tobewrapped) > $maxlength">
                        <xsl:text></xsl:text>
                        <xsl:value-of select="$indent" />
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:if test="$size > 0">
                         <xsl:text> </xsl:text>
                       </xsl:if>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="$tobewrapped" />
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
