<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id$

   Config Service Configuration Interface Generator Template.

   Author: Lars Mennecke
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema">
   
<xsl:template name="packageName">
   <xsl:param name="value"/>
   <xsl:if test="contains($value, '.')">
      <xsl:variable name="rest"><xsl:value-of
      select="substring-after($value, '.')"/></xsl:variable>.<xsl:value-of
      select="substring-before($value, '.')"/>
      <xsl:call-template
         name="packageName">
         <xsl:with-param name="value" select="$rest"/>
      </xsl:call-template>
   </xsl:if>
</xsl:template>
   
<xsl:template name="classNameFromPackage">
   <xsl:param name="value"/>
   <xsl:choose>
      <xsl:when test="contains($value, '.')">
         <xsl:variable name="rest"><xsl:value-of
         select="substring-after($value, '.')"/></xsl:variable>
         <xsl:call-template
            name="classNameFromPackage">
            <xsl:with-param name="value" select="$rest"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="$value"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>
   
<xsl:template name="getServiceName">
   <xsl:param name="name"/>
   <xsl:variable name="serviceNameWithoutBlanks">
      <xsl:call-template name="replace-char">
         <xsl:with-param name="s" select="$name"/>
         <xsl:with-param name="char" select="' '"/>
         <xsl:with-param name="new" select="''"/>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="serviceName">
      <xsl:call-template name="replace-string">
         <xsl:with-param name="s" select="$serviceNameWithoutBlanks"/>
         <xsl:with-param name="old" select="'FWK'"/>
         <xsl:with-param name="new" select="''"/>
      </xsl:call-template>
   </xsl:variable>
   <xsl:value-of select="$serviceName"/>
</xsl:template>

<xsl:template name="config-interface-generator">
   <xsl:param name="servicename"/>
   <xsl:param name="package"/>
   <xsl:param name="entries"/>
   <xsl:variable name="classname">
      <xsl:value-of select="$servicename"/>
      <xsl:text>Configuration</xsl:text>
   </xsl:variable>
   <xsl:variable name="class-javadoc">
      <xsl:text>The configuration ServiceConfiguration interface of the </xsl:text>
      <xsl:value-of select="$servicename"/>
      <xsl:text>.</xsl:text>
   </xsl:variable>

<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;


/**
 * <xsl:value-of select="$class-javadoc"/>
 *
 * @author generated
 */
public interface <xsl:value-of select="$classname"/>
      extends org.jcoderz.commons.config.ServiceConfiguration
{
   <xsl:for-each select="$entries">
      <xsl:call-template name="typedMethodInterface">
         <xsl:with-param name="entry" select="."/>
      </xsl:call-template>
   </xsl:for-each>
}
</xsl:template>

<xsl:template name="typedMethodInterface">
   <xsl:param name="entry"/>
   <xsl:variable name="method">
      <xsl:call-template name="classNameFromPackage">
         <xsl:with-param name="value"><xsl:value-of select="$entry/key"/></xsl:with-param>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="methodName">
      <xsl:text>get</xsl:text>
      <xsl:call-template name="toUpperCase">
         <xsl:with-param name="s"><xsl:value-of select="substring($method, 1, 1)"/></xsl:with-param>
      </xsl:call-template>
      <xsl:value-of select="substring($method, 2)"/>
   </xsl:variable>
   <xsl:variable name="refDescription">
      <xsl:if test="string-length($entry/key/@reference) = 0">
         <xsl:value-of select="$entry/description"/>
      </xsl:if>
      <xsl:if test="string-length($entry/key/@reference) > 0">
         <xsl:value-of select="//configEntry[key = $entry/key/@reference]/description"/>
      </xsl:if>
   </xsl:variable>
   /**
    * Gets <xsl:value-of select="$refDescription"/>.
    *
    * @return <xsl:value-of select="$refDescription"/>
    */
   <xsl:if test="string-length($entry/key/@reference) = 0">
             <xsl:value-of select="$entry/type"/>
          </xsl:if>
          <xsl:if test="string-length($entry/key/@reference) > 0">
             <xsl:value-of select="//configEntry[key = $entry/key/@reference]/type"/>
          </xsl:if>
   <xsl:text> </xsl:text>
   <xsl:value-of select="$methodName"/> ();
</xsl:template>

<xsl:template name="config-impl-generator">
   <xsl:param name="servicename"/>
   <xsl:param name="package"/>
   <xsl:param name="entries"/>
   <xsl:variable name="classname">
      <xsl:value-of select="$servicename"/><xsl:text>Configuration</xsl:text>
   </xsl:variable>
   <xsl:variable name="class-javadoc">
      <xsl:text>The configuration ServiceConfiguration implementation of the </xsl:text>
      <xsl:value-of select="$servicename"/>
      <xsl:text>.</xsl:text>
   </xsl:variable>

<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

import java.rmi.RemoteException;
import java.io.Serializable;

import org.jcoderz.commons.config.*;

/**
 * <xsl:value-of select="$class-javadoc"/>
 *
 * @author generated
 */
public final class <xsl:value-of select="$classname"/>Impl
      implements ConfigurationListener, <xsl:value-of select="$classname"/>,
                 Serializable
{
   /** 
    * The class fingerprint that is set to indicate serialization 
    * compatibility with a previous version of the class. 
    */
   static final long serialVersionUID = 1L;
   
   /** the local Config Service instance */
   private static final ConfigurationServiceInterface CONFIG_SERVICE
         = ConfigurationServiceContainerFactory.createLocalService();

   /** Message String constants to make checkstyle happy. */
   private static final String DESCRIPTION_TAG
      = "Description";
   private static final String DESCRIPTION_1
      = "There is a problem getting and maybe deserializing the value for "
      + "config key";
   private static final String DESCRIPTION_2
      = "from ConfigurationService's service interface.";
   
   /** The default caching mode for the type interface */   
   private static final boolean DEFAULT_CACHING_MODE = true;
   
   /** The actually used caching mode. */
   private boolean mCaching = DEFAULT_CACHING_MODE;

   <xsl:for-each select="$entries">
      <xsl:call-template name="configImplMember">
         <xsl:with-param name="entry" select="."/>
      </xsl:call-template>
   </xsl:for-each>

   /**
    * Public constructor for <xsl:value-of select="$classname"/>Impl.
    *
    * @throws RemoteException  in case of a remote exception
    */
   public <xsl:value-of select="$classname"/>Impl ()
         throws RemoteException
   {
      CONFIG_SERVICE.addConfigurationListener(this);

      // trigger initial loading of the configuration data
      updateConfiguration(new ConfigUpdateEvent(this, ConfigUpdateEvent.CACHE_UPDATED));
   }

   /**
    * Callback method to update the configuration. Trigger by the ConfigService.
    * @param event The event object which holds the data to update
    */
   public void updateConfiguration (ConfigUpdateEvent event)
   {
      if (event.getId() == ConfigUpdateEvent.CACHE_UPDATED)
      {
<xsl:for-each select="$entries">
   <xsl:call-template name="configImplLocalMember">
      <xsl:with-param name="entry" select="."/>
   </xsl:call-template>
</xsl:for-each>

   <xsl:for-each select="$entries">
      <xsl:call-template name="updateLocalMember">
         <xsl:with-param name="entry" select="."/>
      </xsl:call-template>
   </xsl:for-each>

   <xsl:for-each select="$entries">
      <xsl:call-template name="updateMember">
         <xsl:with-param name="entry" select="."/>
      </xsl:call-template>
   </xsl:for-each>
      }
   }

   <xsl:for-each select="$entries">
      <xsl:call-template name="configImplMethod">
         <xsl:with-param name="entry" select="."/>
      </xsl:call-template>
   </xsl:for-each>
   
    /** {@inheritDoc} */
   public void enableCaching (boolean doCaching)
   {
      if (mCaching != doCaching)
      {
         mCaching = doCaching;
         if (mCaching)
         {
            // reload config in cache
            updateConfiguration(
                  new ConfigUpdateEvent(this, ConfigUpdateEvent.CACHE_UPDATED));
         }
      }
      else
      {
         // no changes need to be done
      }
   }
}
</xsl:template>

<xsl:template name="configImplMember">
   <xsl:param name="entry"/>
   <xsl:variable name="member">
      <xsl:call-template name="classNameFromPackage">
         <xsl:with-param name="value"><xsl:value-of select="$entry/key"/></xsl:with-param>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="memberName">
      <xsl:text>m</xsl:text>
      <xsl:call-template name="toUpperCase">
         <xsl:with-param name="s"><xsl:value-of select="substring($member, 1, 1)"/></xsl:with-param>
      </xsl:call-template>
      <xsl:value-of select="substring($member, 2)"/>
   </xsl:variable>

<xsl:if test="string-length($entry/key/@reference) = 0">
   /** <xsl:value-of select="$entry/description"/> */
   private <xsl:value-of select="$entry/type"/>
</xsl:if>
<xsl:if test="string-length($entry/key/@reference) > 0">
   /** <xsl:value-of select="//configEntry[key = $entry/key/@reference]/description"/> */
   private <xsl:value-of select="//configEntry[key = $entry/key/@reference]/type"/>
</xsl:if>
   <xsl:text> </xsl:text>
   <xsl:value-of select="$memberName"/>;
</xsl:template>

<xsl:template name="configImplLocalMember">
   <xsl:param name="entry"/>
   <xsl:variable name="memberName">
      <xsl:call-template name="classNameFromPackage">
         <xsl:with-param name="value"><xsl:value-of select="$entry/key"/></xsl:with-param>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="refType">
      <xsl:if test="string-length($entry/key/@reference) = 0">
         <xsl:value-of select="$entry/type"/>
      </xsl:if>
      <xsl:if test="string-length($entry/key/@reference) > 0">
         <xsl:value-of select="//configEntry[key = $entry/key/@reference]/type"/>
      </xsl:if>
   </xsl:variable>

   <xsl:text>         </xsl:text>
   <xsl:value-of select="$refType"/>
   <xsl:text> </xsl:text>
   <xsl:value-of select="$memberName"/>
   <xsl:text> = </xsl:text>
   <xsl:choose>
      <xsl:when test="$refType = 'boolean'">false</xsl:when>
      <xsl:when test="$refType = 'int'">0</xsl:when>
      <xsl:when test="$refType = 'long'">0</xsl:when>
      <xsl:otherwise>null</xsl:otherwise>
   </xsl:choose>;

</xsl:template>

<xsl:template name="updateLocalMember">
   <xsl:param name="entry"/>
   <xsl:variable name="memberName">
      <xsl:call-template name="classNameFromPackage">
         <xsl:with-param name="value"><xsl:value-of select="$entry/key"/></xsl:with-param>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="refType">
      <xsl:if test="string-length($entry/key/@reference) = 0">
         <xsl:value-of select="$entry/type"/>
      </xsl:if>
      <xsl:if test="string-length($entry/key/@reference) > 0">
         <xsl:value-of select="//configEntry[key = $entry/key/@reference]/type"/>
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="refKeyAsJavaConstantName">
      <xsl:call-template name="asJavaConstantName">
         <xsl:with-param name="value">
            <xsl:if test="string-length($entry/key/@reference) = 0">
               <xsl:value-of select="$entry/key"/>
            </xsl:if>
            <xsl:if test="string-length($entry/key/@reference) > 0">
               <xsl:value-of select="$entry/key/@reference"/>
            </xsl:if>
         </xsl:with-param>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="serviceName">
      <xsl:call-template name="asJavaIdentifier">
         <xsl:with-param name="name" select="../@name"/>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="configKeyClass" 
      select="concat($serviceName,'ConfigurationKeys')"/>
         try
         {
            <xsl:value-of select="$memberName"/> 
                  = <xsl:choose>
                  <xsl:when test="$refType = 'boolean'">
                     <xsl:text>CONFIG_SERVICE.getBoolean(</xsl:text>
                     <xsl:value-of select="$configKeyClass"/>.
                     <xsl:value-of select="$refKeyAsJavaConstantName"/>
                  </xsl:when>
                  <xsl:when test="$refType = 'int'">
                     <xsl:text>CONFIG_SERVICE.getInt(</xsl:text>
                     <xsl:value-of select="$configKeyClass"/>.
                     <xsl:value-of select="$refKeyAsJavaConstantName"/>
                  </xsl:when>
                  <xsl:when test="$refType = 'long'">
                     <xsl:text>CONFIG_SERVICE.getLong(</xsl:text>
                     <xsl:value-of select="$configKeyClass"/>.
                     <xsl:value-of select="$refKeyAsJavaConstantName"/>
                  </xsl:when>
                  <xsl:when test="$refType = 'String'">
                     <xsl:text>CONFIG_SERVICE.getString(</xsl:text>
                     <xsl:value-of select="$configKeyClass"/>.
                     <xsl:value-of select="$refKeyAsJavaConstantName"/>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:choose>
                        <!-- constructor -->
                        <xsl:when test="$entry/deserializeMethod = 'new'"> 
                           new <xsl:value-of select="$refType"/>
                        </xsl:when>
                        <!-- factory method -->
                        <xsl:otherwise> 
                           <xsl:if test="string-length($entry/key/@reference) = 0">
                              <xsl:value-of select="$refType"/>.<xsl:value-of 
                                 select="$entry/deserializeMethod"/>
                           </xsl:if>
                           <xsl:if test="string-length($entry/key/@reference) > 0">
                              <xsl:choose>
                                 <!-- constructor -->
                                 <xsl:when test="//configEntry[key = $entry/key/@reference]/deserializeMethod = 'new'"> 
                                    new <xsl:value-of select="$refType"/>
                                 </xsl:when>
                                 <!-- factory method -->
                                 <xsl:otherwise>
                                    <xsl:value-of select="$refType"/>
                                    <xsl:text>.</xsl:text>
                                    <xsl:value-of 
                                       select="//configEntry[key = $entry/key/@reference]/deserializeMethod"/>
                                 </xsl:otherwise>
                              </xsl:choose>
                           </xsl:if>
                        </xsl:otherwise>
                     </xsl:choose>
                     <xsl:text>(CONFIG_SERVICE.getString(</xsl:text>
                     <xsl:value-of select="$configKeyClass"/>.
                     <xsl:value-of select="$refKeyAsJavaConstantName"/>
                     <xsl:text>)</xsl:text>
                  </xsl:otherwise>
               </xsl:choose>);
         }
         catch (Exception e)
         {
            final ConfigurationInitializationFailedException configIniEx
                  = new ConfigurationInitializationFailedException(e);
            configIniEx.addParameter(DESCRIPTION_TAG, DESCRIPTION_1
                  + " <xsl:value-of select="$refKeyAsJavaConstantName"/> "
                  + DESCRIPTION_2);
            throw configIniEx;
         }
</xsl:template>

<xsl:template name="updateMember">
   <xsl:param name="entry"/>
   <xsl:variable name="localMember">
      <xsl:call-template name="classNameFromPackage">
         <xsl:with-param name="value"><xsl:value-of select="$entry/key"/></xsl:with-param>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="memberName">
      <xsl:text>m</xsl:text>
      <xsl:call-template name="toUpperCase">
         <xsl:with-param name="s"><xsl:value-of select="substring($localMember, 1, 1)"/></xsl:with-param>
      </xsl:call-template>
      <xsl:value-of select="substring($localMember, 2)"/>
   </xsl:variable>

   <xsl:text>         </xsl:text>
   <xsl:value-of select="$memberName"/>
   <xsl:text> = </xsl:text>
   <xsl:value-of select="$localMember"/>;
</xsl:template>

<xsl:template name="configImplMethod">
   <xsl:param name="entry"/>
   <xsl:variable name="method">
      <xsl:call-template name="classNameFromPackage">
         <xsl:with-param name="value"><xsl:value-of select="$entry/key"/></xsl:with-param>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="methodName">
      <xsl:text>get</xsl:text>
      <xsl:call-template name="toUpperCase">
         <xsl:with-param name="s"><xsl:value-of select="substring($method, 1, 1)"/></xsl:with-param>
      </xsl:call-template>
      <xsl:value-of select="substring($method, 2)"/>
   </xsl:variable>
   <xsl:variable name="memberName">
      <xsl:text>m</xsl:text>
      <xsl:call-template name="toUpperCase">
         <xsl:with-param name="s"><xsl:value-of select="substring($method, 1, 1)"/></xsl:with-param>
      </xsl:call-template>
      <xsl:value-of select="substring($method, 2)"/>
   </xsl:variable>
   <xsl:variable name="refDescription">
      <xsl:if test="string-length($entry/key/@reference) = 0">
         <xsl:value-of select="$entry/description"/>
      </xsl:if>
      <xsl:if test="string-length($entry/key/@reference) > 0">
         <xsl:value-of select="//configEntry[key = $entry/key/@reference]/description"/>
      </xsl:if>
   </xsl:variable>

   /**
    * <xsl:value-of select="normalize-space($refDescription)"/>.
    * @return <xsl:value-of select="normalize-space($refDescription)"/>
    */
   public <xsl:if test="string-length($entry/key/@reference) = 0">
             <xsl:value-of select="$entry/type"/>
          </xsl:if>
          <xsl:if test="string-length($entry/key/@reference) > 0">
             <xsl:value-of select="//configEntry[key = $entry/key/@reference]/type"/>
          </xsl:if>
          <xsl:text> </xsl:text>
          <xsl:value-of select="$methodName"/> ()
   {
      if (!mCaching)
      {
         // load config parameter directly from config service interface
         <xsl:call-template name="configImplLocalMember">
            <xsl:with-param name="entry" select="."/>
         </xsl:call-template>
         <xsl:call-template name="updateLocalMember">
            <xsl:with-param name="entry" select="."/>
         </xsl:call-template>
         <xsl:call-template name="updateMember">
            <xsl:with-param name="entry" select="."/>
         </xsl:call-template>
      }
      return <xsl:value-of select="$memberName"/>;
   }
</xsl:template>

<xsl:template name="service-configuration-keys">
   <xsl:param name="classname"/>
   <xsl:param name="package"/>
   <xsl:param name="items"/>
   <xsl:param name="javadoc-prefix"/>

<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;


import org.jcoderz.commons.config.ConfigurationKey;

/**
 * This class defines all configuration keys 
 * for the <xsl:value-of select="@name"/> service.
 *
 * @author generated
 */
public final class <xsl:value-of select="$classname"/>
{
   private <xsl:value-of select="$classname"/> ()
   {
      // instances not allowed -- provides only constants
   }
   
   <xsl:for-each select="$items">
   <xsl:sort select="."/>
   /**
    * <xsl:value-of select="normalize-space(./description)"/>
    */
   public static final ConfigurationKey <xsl:call-template name="asJavaConstantName">
      <xsl:with-param name="value" select="./key"/>
   </xsl:call-template>
         = ConfigurationKey.fromString("<xsl:value-of select="$package"/>.<xsl:value-of select="./key"/>");
  </xsl:for-each>
}
</xsl:template>

</xsl:stylesheet>
