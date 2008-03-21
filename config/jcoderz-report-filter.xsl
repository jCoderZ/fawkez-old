<?xml version="1.0" encoding="UTF-8"?>
<!--
    $Id$

    Copyright 2006, The jCoderZ.org Project. All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are
    met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above
          copyright notice, this list of conditions and the following
          disclaimer in the documentation and/or other materials
          provided with the distribution.
        * Neither the name of the jCoderZ.org Project nor the names of
          its contributors may be used to endorse or promote products
          derived from this software without specific prior written
          permission.

    THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
    PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
    BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
    BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
    OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
    ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 -->

<!--
  This file is maintained by amandel (Andreas Mandel)
  and mgriffel (Michael Griffel).

  Do not modify this file without approval from
  the maintainers!
  -->

<xs:stylesheet xmlns:xs="http://www.w3.org/1999/XSL/Transform"
   version="1.0">

   <xs:output method="xml"/>

   <xs:template match="*|@*|comment()|text()|processing-instruction()">
      <xs:copy>
         <xs:apply-templates select=" *|@*|comment()|text()|processing-instruction()"/>
      </xs:copy>
   </xs:template>

   <!-- *** General filters *** -->

    <!-- Never filter samples... -->
   <xs:template
       match="/report/file[@package = 'org.jcoderz.phoenix.report.samples']/item"
       priority="2">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="message"><xs:value-of select="concat(@message, ' (sample)')"/></xs:attribute>
      </item>
   </xs:template>

   <!-- Some tests are not relevant for testcases....  -->
   <xs:template
      match="/report/file[@level = 'test']/
                     item[@finding-type = 'SignatureDeclareThrowsException'
                       or @finding-type = 'DE_MIGHT_IGNORE'
                       or @finding-type = 'EC_UNRELATED_TYPES'
                       or @finding-type = 'EC_NULL_ARG'
                       or @finding-type = 'CS_ILLEGAL_CATCH'
                       or @finding-type = 'EqualsNull'
                       or @finding-type = 'CouplingBetweenObjects'
                       or @finding-type = 'ExcessivePublicCount'
                       or @finding-type = 'CouplingBetweenObjects'
                       or @finding-type = 'AvoidCatchingNPE']"
      priority="1">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Not required for testcode.</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@level = 'test']/item[@severity = 'error']"
      priority="0">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">warning</xs:attribute>
         <xs:attribute name="severity-reason">Decreased severity from 'error' for testcode.</xs:attribute>
         <xs:attribute name="message"><xs:value-of select="concat(@message, ' (test code)')"/></xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@level = 'test']/item[@severity = 'warning']"
      priority="0">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">info</xs:attribute>
         <xs:attribute name="severity-reason">Decreased severity from 'warning' for testcode.</xs:attribute>
         <xs:attribute name="message"><xs:value-of select="concat(@message, ' (test code)')"/></xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@level = 'test']/item[@severity = 'info']"
      priority="0">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="message"><xs:value-of select="concat(@message, ' (test code)')"/></xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@classname = 'AmountInterface']/
                   item[@finding-type = 'SuspiciousConstantFieldName']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Constant declared in interface false positive.</xs:attribute>
      </item>
   </xs:template>

   <!-- FILTER -->
   <xs:template
      match="/report/file/
                   item[@finding-type = 'EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">info</xs:attribute>
         <xs:attribute name="severity-reason">Many false positives.</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@classname = 'BaseRuntimeException'
                       or @classname = 'BaseException'
                       or @classname = 'LogEvent']/
                   item[@finding-type = 'cpd']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Delegation code can not be externalized further.</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@package != 'org.jcoderz.phoenix.report']/
                   item[@finding-type = 'LSYC_LOCAL_SYNCHRONIZED_COLLECTION']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">We are still on Java 1.4 here.</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@classname = 'StringUtil']/
                   item[@finding-type = 'ES_COMPARING_STRINGS_WITH_EQ']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Ok.</xs:attribute>
      </item>
   </xs:template>

   <!-- Main type classes. -->
   <xs:template
      match="/report/file[@classname = 'DbView'
                       or @classname = 'SqlTransformer'
                       or @classname = 'CmpGenerator'
                       or @classname = 'SqlToXml'
                       or @classname = 'JavaCodeSnippets'
                       or @classname = 'LogViewer'
                       or @classname = 'Chart2DHandlerImpl'
                       or @classname = 'SqlScanner'
                       or @classname = 'TemplateZip'
                       or @classname = 'JDepend']/
                   item[@finding-type = 'SystemPrintln'
                     or @finding-type = 'AvoidPrintStackTrace']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">main class</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@classname = 'StringUtilTest'
                       or @classname = 'IoUtilTest']/
                   item[@finding-type = 'UseAssertEqualsInsteadOfAssertTrue']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Ok.</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@classname = 'LoggingProxy']/
                   item[@finding-type = 'MoreThanOneLogger'
                     or @finding-type = 'LoggerIsNotStaticFinal']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Ok.</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@classname = 'AmountBaseTest']/
                   item[@finding-type = 'CS_MAGIC_NUMBER']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Ok.</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@classname = 'LuhnAlgorithm']/
                   item[@finding-type = 'CS_MAGIC_NUMBER']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Ok for algorithms.</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@classname = 'Base64Util'
                       or @classname = 'Base64UtilTest']/
                   item[@finding-type = 'CS_MAGIC_NUMBER']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Ok.</xs:attribute>
      </item>
   </xs:template>

   <xs:template
      match="/report/file[@classname = 'Severity'
                       or @classname = 'TokenType'
                       or @classname = 'CheckstyleFindingType']/
                   item[@finding-type = 'CS_DECLARATION_ORDER']">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Ok.</xs:attribute>
      </item>
   </xs:template>

   <!-- *** Specific false positive *** -->
   <xs:template
      match="/report/file[@classname = 'LoggableImpl']/
              item[@finding-type = 'SE_BAD_FIELD']
                  [contains(@message, 'mParameters')]">
      <item>
         <xs:apply-templates select="@*"/>
         <xs:attribute name="severity">filtered</xs:attribute>
         <xs:attribute name="severity-reason">Map implementation is assumed to be always serializable.</xs:attribute>
      </item>
   </xs:template>


</xs:stylesheet>
