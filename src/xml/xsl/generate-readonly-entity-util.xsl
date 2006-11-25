<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
   xmlns:xalan2="http://xml.apache.org/xslt"
   extension-element-prefixes="redirect">

   <xsl:output
      method="text"
      omit-xml-declaration="yes"/>
   <xsl:include href="libcommon.xsl"/>

   <xsl:param name="srcdir" select="."/>

<xsl:template match="entity[contains(ejb-name, 'ReaderEntity')]">
   <xsl:variable name="package">
      <xsl:call-template name="package-from-class">
         <xsl:with-param name="class" select="home"/>
      </xsl:call-template>
   </xsl:variable>
   <xsl:variable name="classname"><xsl:value-of
      select="concat(ejb-name, 'Util')"/></xsl:variable>

   <xsl:variable name="package.dir"><xsl:value-of 
      select="$srcdir"/>/<xsl:value-of 
         select="translate($package, '.', '/')"/></xsl:variable> 

   <xsl:variable name="file"><xsl:value-of 
      select="$package.dir"/>/<xsl:value-of 
         select="$classname"/>.java</xsl:variable> 

   <redirect:write file="{$file}">

   <xsl:call-template name="reader-util">
      <xsl:with-param name="beanname" select="./ejb-name"/>
      <xsl:with-param name="package" select="$package"/>
      <xsl:with-param name="classname" select="$classname"/>
      <xsl:with-param name="entityhome" select="./home"/>
   </xsl:call-template>
   
   </redirect:write>
</xsl:template>

<xsl:template name="reader-util">
   <xsl:param name="beanname"/>
   <xsl:param name="package"/>
   <xsl:param name="classname"/>
   <xsl:param name="entityhome"/>

<xsl:call-template name="java-copyright-header"/>

package <xsl:value-of select="$package"/>;

/**
 * This class provides utility methods for the home lookup of the
 * <xsl:value-of select="$beanname"/> entity bean.
 * @author generated
 */
public class <xsl:value-of select="$classname"/>
{
   private static final String READER_COMP_NAME = "java:/comp/env/ejb/<xsl:value-of select="$beanname"/>";

   /**
    * Obtain remote home interface from default initial context.
    * @return Home interface for <xsl:value-of select="$beanname"/>, lookup using COMP_NAME
    */
   public static <xsl:value-of select="$entityhome"/> getHome ()
         throws javax.naming.NamingException
   {
      return (<xsl:value-of select="$entityhome"/>) lookupHome(
            null, READER_COMP_NAME, <xsl:value-of select="$entityhome"/>.class);
   }

   /**
    * Obtain remote home interface from parameterised initial context
    * @param environment Parameters to use for creating initial context
    * @return Home interface for <xsl:value-of select="$beanname"/>, lookup using COMP_NAME
    */
   public static <xsl:value-of select="$entityhome"/> getHome (java.util.Hashtable environment)
         throws javax.naming.NamingException
   {
      return (<xsl:value-of select="$entityhome"/>) lookupHome(
            environment, READER_COMP_NAME, <xsl:value-of select="$entityhome"/>.class);
   }

   private static Object lookupHome (
         java.util.Hashtable environment, String jndiName, Class narrowTo)
         throws javax.naming.NamingException
   {
      // Obtain initial context
      javax.naming.Context initialContext
            = new javax.naming.InitialContext(environment);
      final Object result;
      try
      {
         Object objRef = initialContext.lookup(jndiName);
         // only narrow if necessary
         if (narrowTo.isInstance(java.rmi.Remote.class))
         {
            result = javax.rmi.PortableRemoteObject.narrow(objRef, narrowTo);
         }
         else
         {
            result = objRef;
         }
      }
      finally
      {
         initialContext.close();
      }
      return result;
   }
}
</xsl:template>

</xsl:stylesheet>