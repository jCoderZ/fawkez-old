<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id: generate-config-validator.xsl,v 1.1 2005/04/22 15:11:34 lmenne Exp $

   Generator for Configuration Service Validator class that supports
   the Administration Tool by checking configuration keys and
   type consistency.

   Author: Lars Mennecke
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:redirect="http://xml.apache.org/xalan/redirect"
   xmlns:xalan2="http://xml.apache.org/xslt"
   extension-element-prefixes="redirect">

<xsl:include href="libcommon.xsl"/>

<xsl:output method="text" 
            encoding="ISO-8859-1"/>

<xsl:param name="package" select="'org.jcoderz.commons.config'"/>
<xsl:param name="classname" select="'ConfigurationValidator'"/>
<xsl:param name="outdir" select="'.'"/>

<xsl:template match="/">
   <!-- log to out -->
   <xsl:message terminate="no">
      Generating class to directory <xsl:value-of select="$outdir"/>.
   </xsl:message>
   
<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jcoderz.commons.ArgumentMalformedException;
import org.jcoderz.commons.util.Assert;
import org.jcoderz.commons.config.ConfigurationKey;

/**
 * Helper class for validation of ConfigurationKeys and value types.
 *
 * @author generated
 */
public class <xsl:value-of select="$classname"/>
{
   /**
    * Name of this class
    */
   private static final transient String CLASSNAME
      = <xsl:value-of select="$classname"/>.class.getName();

   /**
    * Logger for this class
    */
   private static final transient
      Logger logger = Logger.getLogger(CLASSNAME);
   protected static final transient Map mConfigMap;
   //protected static final transient Map mDeserializeMap;

   private <xsl:value-of select="$classname"/> ()
   {
   }  
   
   /**
    * asserts that the given configuration key is a valid key.
    * @param configKey the configuration key that should be tested
    * @throws ArgumentMalformedException if the key does not exist
    *       in current deployment or configValue is null
    */
   public static void assertValidKey (ConfigurationKey configKey)
         throws ArgumentMalformedException
   {
      Assert.notNull(configKey, "configKey");
      
      if (!mConfigMap.containsKey(configKey))
      {
         throw new ArgumentMalformedException (
               "configKey", configKey,
               "This key does not exist in current deployment");
      }
   }

   static
   {
      mConfigMap = new HashMap();
      //mDeserializeMap = new HashMap();
      <xsl:for-each select=".//configEntry">
         mConfigMap.put(
               ConfigurationKey.fromString("<xsl:value-of select="../@package"/>.<xsl:value-of select="./key"/>"),
               "<xsl:value-of select="./type"/>");
      //   mDeserializeMap.put(
      //         ConfigurationKey.fromString("<xsl:value-of select="../@package"/>.<xsl:value-of select="./key"/>"),
      //         "<xsl:value-of select="./deserializeMethod"/>");
      </xsl:for-each>     
   }
}
</xsl:template>


</xsl:stylesheet>