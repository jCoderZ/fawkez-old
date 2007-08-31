<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:redirect="http://xml.apache.org/xalan/redirect"
   xmlns:xalan2="http://xml.apache.org/xslt"
   xmlns:UML = 'org.omg.xmi.namespace.UML'
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

   <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
   
   <xsl:template match="uc:usecases">
      <xsl:apply-templates select="req:requirement[req:category/req:primary = 'Domain Model']" mode="xmi"/>
   </xsl:template>
   
   <xsl:key name="unique-category-primary-key" match="req:requirement/req:category/req:primary" use="."/>
   <xsl:key name="unique-category-secondary-key" match="req:requirement/req:category/req:secondary" use="."/>
   <xsl:key name="unique-category-tertiary-key" match="req:requirement/req:category/req:tertiary" use="."/>
   
   <xsl:key name="unique-pattern-key" match="//req:pattern" use="."/>
   
   <!-- xmi export for domain model -->
   
   <xsl:template match="req:requirement" mode="xmi">
      <xsl:variable name="file">domain_model.xmi</xsl:variable>

       
       <redirect:write file="{$file}" method="xml">
       <XMI xmi.version = '1.2' xmlns:UML = 'org.omg.xmi.namespace.UML' timestamp = 'Thu Aug 30 08:53:32 CEST 2007'>
       <XMI.header>    
          <XMI.documentation>
             <XMI.exporter>Fawkez</XMI.exporter>
             <XMI.exporterVersion></XMI.exporterVersion>
          </XMI.documentation>
          <XMI.metamodel xmi.name="UML" xmi.version="1.4"/>
       </XMI.header>
       <XMI.content>
    
          <UML:Model xmi.id = 'Domain Model'
                     name = 'Domain Model'
                     isSpecification = 'false'
                     isRoot = 'false'
                     isLeaf = 'false'
                     isAbstract = 'false'>
              <UML:Namespace.ownedElement>
              
              <xsl:apply-templates select="//req:entity[not(../req:category/req:secondary) or ../req:category/req:secondary = '']"
                                            mode="xmi"/>
              
              <xsl:for-each select="//req:requirement/req:category/req:secondary[generate-id() = generate-id(key('unique-category-secondary-key', .))]">    
                 <xsl:variable name="sec_cat" select="."/>
                   <xsl:if test="//req:entity[../req:category/req:secondary = $sec_cat and starts-with(../req:category/req:primary, 'Domain Model')]">
                   

                                
                    <UML:Package xmi.id = 'Second Category {$sec_cat}'
                                 name = '{$sec_cat}'
                                 isSpecification = 'false' 
                                 isRoot = 'false' 
                                 isLeaf = 'false' 
                                 isAbstract = 'false'>
                       <UML:Namespace.ownedElement>
                     
                       <xsl:apply-templates select="//req:entity[../req:category/req:secondary = $sec_cat and not(../req:category/req:tertiary)]"
                                            mode="xmi"/>       

                       
                         <xsl:for-each select="//req:requirement/req:category/req:tertiary[generate-id() = generate-id(key('unique-category-tertiary-key', .))]">    
                            <xsl:variable name="ter_cat" select="."/>
                            <xsl:if test="//req:entity[../req:category/req:tertiary = $ter_cat and ../req:category/req:secondary = $sec_cat and starts-with(../req:category/req:primary, 'Domain Model')]">
                                         
                             <UML:Package xmi.id = 'Second Category {$sec_cat}/Tertiary Category {$ter_cat}'
                                          name = '{$sec_cat}/{$ter_cat}'
                                          isSpecification = 'false' 
                                          isRoot = 'false' 
                                          isLeaf = 'false' 
                                          isAbstract = 'false'>
                                <UML:Namespace.ownedElement>
                              
                                   <xsl:apply-templates select="//req:entity[../req:category/req:secondary = $sec_cat and ../req:category/req:tertiary = $ter_cat]"
                                                        mode="xmi"/>       
         
                                </UML:Namespace.ownedElement>
                             </UML:Package>
                               
                            </xsl:if>
                         </xsl:for-each>
                    
                      </UML:Namespace.ownedElement>
                    </UML:Package>
                      
                   </xsl:if>
                   
                </xsl:for-each>

                 <xsl:for-each select="//req:pattern[generate-id() = generate-id(key('unique-pattern-key', .))]">
                    <UML:DataType xmi.id = '{.}'
                                  name = '{.}' 
                                  isSpecification = 'false' 
                                  isRoot = 'false' 
                                  isLeaf = 'false'
                                  isAbstract = 'false'/>
                 </xsl:for-each>
                 
                 <xsl:apply-templates select="//req:objectreference" mode="xmi"/>
              </UML:Namespace.ownedElement>
          </UML:Model>
       
       </XMI.content>
       </XMI>
    
       </redirect:write>
   </xsl:template>
   
   <xsl:template match="req:entity" mode="xmi">
      <UML:Class xmi.id = 'Entity {../req:key}'
              name = '{../req:key} {req:name}' visibility = 'public' isSpecification = 'false' isRoot = 'false'
              isLeaf = 'false' isAbstract = 'false' isActive = 'false'>
         <UML:Classifier.feature>
         
            <xsl:apply-templates select="req:attribute" mode="xmi"/>
            
         </UML:Classifier.feature>
      </UML:Class>
   </xsl:template>
   
   <xsl:template match="req:attribute" mode="xmi">
      <UML:Attribute xmi.id = '{../../req:key}-{req:name}'
                     name = '{req:name}' 
                     visibility = 'public' 
                     isSpecification = 'false' 
                     ownerScope = 'instance'
                     changeability = 'changeable' 
                     targetScope = 'instance'>

        <xsl:choose>
           <xsl:when test="req:pattern and not(req:pattern = '')">
              <xsl:apply-templates select="req:pattern" mode="xmi_ref"/>
           </xsl:when>
           <xsl:when test="req:objectreference">
              <xsl:apply-templates select="req:objectreference" mode="xmi_ref"/>
           </xsl:when>
        </xsl:choose>
      </UML:Attribute>
   </xsl:template>
   
   <xsl:template match="req:pattern" mode="xmi">
     <!-- TODO find out why the multiplicity block -->
     <!-- 
     <UML:StructuralFeature.multiplicity>
       <UML:Multiplicity xmi.id = '{../../req:key}-{../req:name} multi {.}'>
         <UML:Multiplicity.range>
           <UML:MultiplicityRange xmi.id = '{../../req:key}-{../req:name} multi {.} range'
             lower = '1' upper = '1'/>
         </UML:Multiplicity.range>
       </UML:Multiplicity>
     </UML:StructuralFeature.multiplicity>
      -->
     <UML:StructuralFeature.type>
       <UML:DataType xmi.idref = '{.}'/>
     </UML:StructuralFeature.type>
   </xsl:template>
   
   <xsl:template match="req:objectreference" mode="xmi_ref">
     <!-- TODO find out why the multiplicity block -->
     <!--
      <UML:StructuralFeature.multiplicity>
       <UML:Multiplicity xmi.id = '{../../req:key}-{../req:name} multi {.}'>
         <UML:Multiplicity.range>
           <UML:MultiplicityRange xmi.id = '{../../req:key}-{../req:name} multi {.} range'
             lower = '1' upper = '1'/>
         </UML:Multiplicity.range>
       </UML:Multiplicity>
     </UML:StructuralFeature.multiplicity>
      -->
      <UML:StructuralFeature.type>
        <UML:Class xmi.idref = 'Entity {req:ref/@id}'/>
      </UML:StructuralFeature.type>
   </xsl:template>
   
   <xsl:template match="req:objectreference" mode="xmi">
     <UML:Association xmi.id = '{../../../req:key} {../req:name} reference to {req:ref/@id}'
          name = '{../../../req:key} to {req:ref/@id} ({req:linkstart} {req:linkend})' isSpecification = 'false' isRoot = 'false' isLeaf = 'false'
          isAbstract = 'false'>
          
          <UML:Association.connection>
            <UML:AssociationEnd xmi.id = '{../../../req:key} {../req:name} reference to {req:ref/@id} end'
              name = '{req:linkstart}' visibility = 'public' isSpecification = 'false' isNavigable = 'true'
              ordering = 'unordered' aggregation = 'none' targetScope = 'instance' changeability = 'changeable'>
              <UML:AssociationEnd.multiplicity>
                <UML:Multiplicity>
                  <UML:Multiplicity.range>
                    <UML:MultiplicityRange xmi.id = '{../../../req:key} {../req:name} to {req:ref/@id} range'
                      lower = '1' upper = '1'/>
                  </UML:Multiplicity.range>
                </UML:Multiplicity>
              </UML:AssociationEnd.multiplicity>
              <UML:AssociationEnd.participant>
                <UML:Class xmi.idref = 'Entity {../../../req:key}'/>
              </UML:AssociationEnd.participant>
            </UML:AssociationEnd>
            
            <UML:AssociationEnd xmi.id = '{../../../req:key} {../req:name} reference to {req:ref/@id} end2'
              name = '{req:linkend}' visibility = 'public' isSpecification = 'false' isNavigable = 'true'
              ordering = 'unordered' aggregation = 'none' targetScope = 'instance' changeability = 'changeable'>
              <UML:AssociationEnd.multiplicity>
                <UML:Multiplicity>
                  <UML:Multiplicity.range>
                    <UML:MultiplicityRange xmi.id = '{../../../req:key} {../req:name} from {req:ref/@id} range2'
                      lower = '1' upper = '1'/>
                  </UML:Multiplicity.range>
                </UML:Multiplicity>
              </UML:AssociationEnd.multiplicity>
              <UML:AssociationEnd.participant>
                <UML:Class xmi.idref = 'Entity {req:ref/@id}'/>
              </UML:AssociationEnd.participant>
            </UML:AssociationEnd>
            
            <!-- 
            <UML:Association isAbstract="false" isLeaf="false" isRoot="false"
               isSpecification="false" name="nonsens"
               xmi.id="D-90005 Pointer1 reference to D-90006">
               <UML:Association.connection>
                  <UML:AssociationEnd changeability="changeable"
                     targetScope="instance" aggregation="none"
                     ordering="unordered" isNavigable="true"
                     isSpecification="false" visibility="public" name="*"
                     xmi.id="D-90005 Pointer1 reference to D-90006 end">
                     <UML:AssociationEnd.multiplicity>
                        <UML:Multiplicity>
                           <UML:Multiplicity.range>
                              <UML:MultiplicityRange upper="1" lower="1"
                                 xmi.id="D-90005 Pointer1 reference to D-90006 end multi" />
                           </UML:Multiplicity.range>
                        </UML:Multiplicity>
                     </UML:AssociationEnd.multiplicity>
                     <UML:AssociationEnd.participant>
                        <UML:Class xmi.idref="Entity D-90021" />
                     </UML:AssociationEnd.participant>
                  </UML:AssociationEnd>
                  
               </UML:Association.connection>
            </UML:Association>
             -->
            
            <!-- 
            <UML:AssociationEnd xmi.id = '{../../../req:key}'
              name = 'hallo' visibility = 'public' isSpecification = 'false' isNavigable = 'true'
              ordering = 'unordered' aggregation = 'none' targetScope = 'instance' changeability = 'changeable'>
              <UML:AssociationEnd.multiplicity>
                <UML:Multiplicity xmi.id = '{../../../req:key}'
                  <UML:Multiplicity.range>
                    <UML:MultiplicityRange xmi.id = '{../../../req:key}'
                      lower = '1' upper = '1'/>
                  </UML:Multiplicity.range>
                </UML:Multiplicity>
              </UML:AssociationEnd.multiplicity>
              <UML:AssociationEnd.participant>
                <UML:Class xmi.idref = '{../../../req:key}'
              </UML:AssociationEnd.participant>
            </UML:AssociationEnd>
            -->
          </UML:Association.connection>
      </UML:Association>
   </xsl:template>
   
</xsl:stylesheet>
