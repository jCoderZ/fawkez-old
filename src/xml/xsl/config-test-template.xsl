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

<xsl:template name="config-interface-test-generator">
   <xsl:param name="servicename"/>
   <xsl:param name="package"/>
   <xsl:param name="entries"/>
   <xsl:param name="serviceInterfaces"/>
   <xsl:param name="server-test-class"/>
   <xsl:variable name="classname">
      <xsl:value-of select="$servicename"/><xsl:text>ConfigurationServerTest</xsl:text>
   </xsl:variable>

<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

import java.util.logging.Logger;
import java.rmi.RemoteException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jcoderz.commons.config.ConfigurationServiceContainerFactory;
import <xsl:value-of select="$package"/>.<xsl:value-of select="$servicename"/>Configuration;
<xsl:if test="$serviceInterfaces != 'NONE'">
import <xsl:value-of select="$package"/>.<xsl:value-of select="$servicename"/>ContainerFactory;
</xsl:if>
import <xsl:value-of select="$server-test-class"/>;

/**
 * JUnit tests to test all methods of class "<xsl:value-of select="$servicename"/>ConfigurationImpl"
 * via it's Interface "<xsl:value-of select="$servicename"/>Configuration".
 * These tests are executed in an EJB container environment.
 *
 * @author generated
 */
public class <xsl:value-of select="$classname"/>
      extends ServerTestCase
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

   /**
    * The service configuration (cache) object.
    */
   private <xsl:value-of select="$servicename"/>Configuration mSrvConfig = null;

   /**
    * Standard JUnit suite method.
    * @return Test
    */
   public static Test suite ()
   {
      return new TestSuite(<xsl:value-of select="$classname"/>.class);
   }

   /**
    * Allcoate common ressources.
    * @throws RemoteException in case of remote call error
    * @throws Exception in case of errors from super.setUp() call
    */
   public void setUp ()
         throws RemoteException, Exception
   {
      super.setUp();
      <xsl:choose>
         <xsl:when test="$serviceInterfaces = 'NONE'">
            mSrvConfig = (<xsl:value-of select="$servicename"/>Configuration)
                  ConfigurationServiceContainerFactory.createLocalService().getServiceConfiguration(
                  "<xsl:value-of select="$package"/>.<xsl:value-of select="$servicename"/>Impl");
         </xsl:when>
         <xsl:otherwise>
            mSrvConfig = <xsl:value-of select="$servicename"/>ContainerFactory.getLocalServiceConfiguration();
         </xsl:otherwise>
      </xsl:choose>
   }
   
   /**
    * Free ressources allocated by setUp().
    * @throws Exception if super.tearDown() throws an exception
    */
   public void tearDown () 
         throws Exception
   {
      super.tearDown();
      mSrvConfig = null;
   }

   <xsl:for-each select="$entries">
      <xsl:call-template name="interfaceTestMethod">
         <xsl:with-param name="entry" select="."/>
         <xsl:with-param name="servicename" select="$servicename"/>
      </xsl:call-template>
   </xsl:for-each>
}
</xsl:template>

<xsl:template name="interfaceTestMethod">
   <xsl:param name="entry"/>
   <xsl:param name="servicename"/>

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
   <xsl:variable name="testMethodName">
      <xsl:text>test</xsl:text>
      <xsl:call-template name="toUpperCase">
         <xsl:with-param name="s"><xsl:value-of select="substring($methodName, 1, 1)"/></xsl:with-param>
      </xsl:call-template>
      <xsl:value-of select="substring($methodName, 2)"/>
   </xsl:variable>

   <xsl:variable name="refKey">
      <xsl:if test="string-length($entry/key/@reference) = 0">
         <xsl:value-of select="$entry/key"/>
      </xsl:if>
      <xsl:if test="string-length($entry/key/@reference) > 0">
         <xsl:value-of select="//configEntry[key = $entry/key/@reference]/key"/>
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="refType">
      <xsl:if test="string-length($entry/key/@reference) = 0">
         <xsl:value-of select="$entry/type"/>
      </xsl:if>
      <xsl:if test="string-length($entry/key/@reference) > 0">
         <xsl:value-of select="//configEntry[key = $entry/key/@reference]/type"/>
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="refDeserializeMethod">
      <xsl:if test="string-length($entry/key/@reference) = 0">
         <xsl:value-of select="$entry/deserializeMethod"/>
      </xsl:if>
      <xsl:if test="string-length($entry/key/@reference) > 0">
         <xsl:value-of select="//configEntry[key = $entry/key/@reference]/deserializeMethod"/>
      </xsl:if>
   </xsl:variable>
   /**
    * Tests <xsl:value-of select="$servicename"/>Configuration.<xsl:value-of select="$methodName"/>().
    */
   public void <xsl:value-of select="$testMethodName"/> ()
   {
      final <xsl:value-of select="$refType"/> value 
            = mSrvConfig.<xsl:value-of select="$methodName"/>();
            
      logger.fine("<xsl:value-of select="$methodName"/>() delivered value: "
                   + value);
      // check against default value from configuration.xml
/**
 *
      assertEquals("<xsl:value-of select="$methodName"/>() delivered unexpected value.",
                   expectedValue, value);
 */
   }
</xsl:template>

</xsl:stylesheet>
