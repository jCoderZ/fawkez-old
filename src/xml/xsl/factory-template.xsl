<?xml version="1.0" encoding="UTF-8"?>
<!--
   $Id: factory-template.xsl,v 1.4 2005/04/01 07:39:01 cloroff Exp $

   Service Factory Generator Template for Client and Container Factories.

   Author: Lars Mennecke
  -->
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema">

<xsl:template name="client-factory-generator">
   <xsl:param name="application" select="''"/>
   <xsl:param name="servicename"/>
   <xsl:param name="package"/>
   <xsl:param name="withServiceConfiguration"/>
   <xsl:param name="serviceInterfaces"/>
   <xsl:variable name="classname">
      <xsl:value-of select="$servicename"/>
   </xsl:variable>
   <xsl:variable name="class-javadoc">
      <xsl:text>The Container Factory for the </xsl:text>
      <xsl:value-of select="$servicename"/>
      <xsl:text>.</xsl:text>
   </xsl:variable>

<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.jcoderz.commons.util.LoggingProxy;
import org.jcoderz.commons.RemoteCallFailureException;
import org.jcoderz.commons.config.ConfigurationServiceClientFactory;
import org.jcoderz.commons.config.ConfigurationServiceInterface;


/**
 * Client factory to create the <xsl:value-of select="$classname"/> (<xsl:value-of select="$classname"/>Interface) and
 * the <xsl:value-of select="$classname"/>Admin (<xsl:value-of select="$classname"/>AdminInterface).
 *
 * The factory can only be used in a standalone EJB client.
 * Do not use this factory in a J2EE container context (EJB, Web, ...).
 *
 * @author generated
 */
public final class <xsl:value-of select="$classname"/>ClientFactory
{
   /** No instances allowed -- only static factory functions */
   private <xsl:value-of select="$classname"/>ClientFactory ()
   {
   }

<xsl:if test="contains('ALL ONLINE', $serviceInterfaces)">
   /**
    * Returns a remote instance of the <xsl:value-of select="$classname"/>Interface.
    *
    * @return a remote instance of the <xsl:value-of select="$classname"/>Interface.
    * @throws RemoteException communication-related exceptions that may occur
    *                         during the execution of a remote method call.
    * @throws CreateException if the creation the EJB object failed.
    * @throws NamingException if the lookup of the EJB object failed.
    */
   public static <xsl:value-of select="$classname"/>Interface createService ()
         throws RemoteException, CreateException, NamingException
   {
      final <xsl:value-of select="$classname"/>Interface service
            = <xsl:value-of select="$classname"/>JNDIUtil.getHome().create();
      return (<xsl:value-of select="$classname"/>Interface) LoggingProxy.getProxy(service);
   }
</xsl:if>

<xsl:if test="contains('ALL ADMIN', $serviceInterfaces)">
   /**
    * Returns a remote instance of the <xsl:value-of select="$classname"/>AdminInterface.
    *
    * @return a remote instance of the <xsl:value-of select="$classname"/>AdminInterface.
    * @throws RemoteException communication-related exceptions that may occur
    *                         during the execution of a remote method call.
    * @throws CreateException if the creation the EJB object failed.
    * @throws NamingException if the lookup of the EJB object failed.
    */
   public static <xsl:value-of select="$classname"/>AdminInterface createAdmin ()
         throws RemoteException, CreateException, NamingException
   {
      final <xsl:value-of select="$classname"/>AdminInterface adminService
            = <xsl:value-of select="$classname"/>AdminJNDIUtil.getHome().create();
      return (<xsl:value-of select="$classname"/>AdminInterface) LoggingProxy.getProxy(adminService);
   }
</xsl:if>

<xsl:if test="$withServiceConfiguration = 'true'">
   /**
    * Returns the ServiceConfiguration interface for the <xsl:value-of select="$classname"/>
    * service. The configuration service is called remotely.
    * @return the <xsl:value-of select="$classname"/> service configuration interface
    * @throws RemoteException communication-related exceptions that may occur
    *                         during the execution of a remote method call.
    * @throws CreateException if the creation the Configuration Service EJB object failed.
    * @throws NamingException if the lookup of the Configuration Service EJB object failed.
    */
   public static <xsl:value-of select="$application"/><xsl:value-of select="$classname"/>Configuration getServiceConfiguration ()
         throws RemoteException, CreateException, NamingException
   {
      final ConfigurationServiceInterface configService
            = ConfigurationServiceClientFactory.createService();
      return (<xsl:value-of select="$application"/><xsl:value-of select="$classname"/>Configuration) configService.getServiceConfiguration(
            "<xsl:value-of select="$package"/>.<xsl:value-of select="$classname"/>Configuration");
   }
</xsl:if> <!-- end if config params defined for this service -->
}
</xsl:template>


<xsl:template name="container-factory-generator">
   <xsl:param name="application" select="''"/>
   <xsl:param name="servicename"/>
   <xsl:param name="package"/>
   <xsl:param name="withServiceConfiguration"/>
   <xsl:param name="serviceInterfaces"/>
   <xsl:variable name="classname">
      <xsl:value-of select="$servicename"/>
   </xsl:variable>
   <xsl:variable name="class-javadoc">
      <xsl:text>The Container Factory for the </xsl:text>
      <xsl:value-of select="$servicename"/>
      <xsl:text>.</xsl:text>
   </xsl:variable>

<xsl:call-template name="java-copyright-header"/>
package <xsl:value-of select="$package"/>;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import org.jcoderz.commons.util.LoggingProxy;
import org.jcoderz.commons.RemoteCallFailureException;
import org.jcoderz.commons.config.ConfigurationServiceContainerFactory;
import org.jcoderz.commons.config.ConfigurationServiceInterface;

/**
 * Container factory to create the <xsl:value-of select="$classname"/> (<xsl:value-of select="$classname"/>Interface) and
 * the <xsl:value-of select="$classname"/>AdminService (<xsl:value-of select="$classname"/>AdminInterface).
 *
 * The factory can only be used in a J2EE container context (EJB, Web, ...).
 * Do not use this factory in a standalone EJB client.
 *
 * @author generated
 */
public final class <xsl:value-of select="$classname"/>ContainerFactory
{
   /** No instances allowed -- only static factory functions */
   private <xsl:value-of select="$classname"/>ContainerFactory ()
   {
   }

<xsl:if test="contains('ALL ONLINE', $serviceInterfaces)">
   /**
    * Returns a local instance of the <xsl:value-of select="$classname"/>Interface.
    * @return a local instance of the <xsl:value-of select="$classname"/>Interface.
    */
   public static <xsl:value-of select="$classname"/>Interface createLocalService ()
   {
      final <xsl:value-of select="$classname"/>Interface service
            = new <xsl:value-of select="$classname"/>Impl();
      return (<xsl:value-of select="$classname"/>Interface) LoggingProxy.getProxy(service);
   }

   /**
    * Returns a remote instance of the <xsl:value-of select="$classname"/>Interface.
    *
    * This method uses the <code>"java:comp/env"</code> naming context that
    * is only available in a J2EE container.
    *
    * @return a remote instance of the <xsl:value-of select="$classname"/>Interface.
    * @throws RemoteException communication-related exceptions that may occur
    *                         during the execution of a remote method call.
    * @throws CreateException if the creation the EJB object failed.
    * @throws NamingException if the lookup of the EJB object failed.
    */
   public static <xsl:value-of select="$classname"/>Interface createRemoteService ()
         throws RemoteException, CreateException, NamingException
   {
      final <xsl:value-of select="$classname"/>Interface service
            = <xsl:value-of select="$classname"/>Util.getHome().create();
      return (<xsl:value-of select="$classname"/>Interface) LoggingProxy.getProxy(service);
   }
</xsl:if>

<xsl:if test="contains('ALL ADMIN', $serviceInterfaces)">
   /**
    * Returns a remote instance of the <xsl:value-of select="$classname"/>AdminInterface.
    * @return a remote instance of the <xsl:value-of select="$classname"/>AdminInterface.
    */
   public static <xsl:value-of select="$classname"/>AdminInterface createLocalAdmin ()
   {
      final <xsl:value-of select="$classname"/>AdminInterface adminService
            = new <xsl:value-of select="$classname"/>AdminImpl();
      return (<xsl:value-of select="$classname"/>AdminInterface) LoggingProxy.getProxy(adminService);
   }

   /**
    * Returns a remote instance of the <xsl:value-of select="$classname"/>AdminInterface.
    *
    * This method uses the <code>"java:comp/env"</code> naming context that
    * is only available in a J2EE container.
    *
    * @return a remote instance of the <xsl:value-of select="$classname"/>AdminInterface.
    * @throws RemoteException communication-related exceptions that may occur
    *                         during the execution of a remote method call.
    * @throws CreateException if the creation the EJB object failed.
    * @throws NamingException if the lookup of the EJB object failed.
    */
   public static <xsl:value-of select="$classname"/>AdminInterface createRemoteAdmin ()
         throws RemoteException, CreateException, NamingException
   {
      final <xsl:value-of select="$classname"/>AdminInterface adminService
            = <xsl:value-of select="$classname"/>AdminUtil.getHome().create();
      return (<xsl:value-of select="$classname"/>AdminInterface) LoggingProxy.getProxy(adminService);
   }
</xsl:if>
<xsl:if test="$withServiceConfiguration = 'true'">
   <xsl:variable name="resultType"><xsl:value-of
       select="$application"/><xsl:value-of
       select="$classname"/>Configuration</xsl:variable>
   /**
    * Returns the <xsl:value-of select="$resultType"/> interface for the <xsl:value-of select="$classname"/>
    * service. The Configuration Service is used locally.
    * @return the <xsl:value-of select="$resultType"/> service configuration interface
    */
   public static <xsl:value-of select="$resultType"/> getLocalServiceConfiguration ()
   {
      final ConfigurationServiceInterface configService
            = ConfigurationServiceContainerFactory.createLocalService();
      return getLocalServiceConfiguration(configService);
   }

   /**
    * Returns the <xsl:value-of select="$resultType"/> interface for the <xsl:value-of select="$classname"/>
    * service. The Configuration Service is used locally.
    * @param configService an instance of the configuration service.
    * @return the <xsl:value-of select="$resultType"/> service configuration interface
    */
   public static <xsl:value-of select="$resultType"/> getLocalServiceConfiguration (ConfigurationServiceInterface configService)
   {
     <xsl:variable name="resultType"><xsl:value-of
         select="$application"/><xsl:value-of
         select="$classname"/>Configuration</xsl:variable>
      final <xsl:value-of select="$resultType"/> result;
      try
      {
         result = (<xsl:value-of select="$resultType"/>) configService.getServiceConfiguration(
            "<xsl:value-of select="$package"/>.<xsl:value-of select="$classname"/>Configuration");
      }
      catch (RemoteException e)
      {
         throw new RemoteCallFailureException(e);
      }
      return result;
   }

   /**
    * Returns the <xsl:value-of select="$resultType"/> interface for the <xsl:value-of select="$classname"/>
    * service. The configuration service is called remotely.
    * @return the <xsl:value-of select="$resultType"/> service configuration interface
    * @throws RemoteException communication-related exceptions that may occur
    *                         during the execution of a remote method call.
    * @throws CreateException if the creation the Configuration Service EJB object failed.
    * @throws NamingException if the lookup of the Configuration Service EJB object failed.
    */
   public static <xsl:value-of select="$resultType"/> getRemoteServiceConfiguration ()
         throws RemoteException, CreateException, NamingException
   {
      final ConfigurationServiceInterface configService
            = ConfigurationServiceContainerFactory.createRemoteService();
      return (<xsl:value-of select="$resultType"/>) configService.getServiceConfiguration(
            "<xsl:value-of select="$package"/>.<xsl:value-of select="$classname"/>Configuration");
   }
</xsl:if> <!-- end if config params defined for this service -->

}
</xsl:template>

</xsl:stylesheet>
