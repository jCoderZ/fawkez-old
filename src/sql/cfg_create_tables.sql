--
-- $Id$
--
-- Copyright 2006, The jCoderZ.org Project. All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without
-- modification, are permitted provided that the following conditions are
-- met:
--
--    * Redistributions of source code must retain the above copyright
--      notice, this list of conditions and the following disclaimer.
--    * Redistributions in binary form must reproduce the above
--      copyright notice, this list of conditions and the following
--      disclaimer in the documentation and/or other materials
--      provided with the distribution.
--    * Neither the name of the jCoderZ.org Project nor the names of
--      its contributors may be used to endorse or promote products
--      derived from this software without specific prior written
--      permission.
--
-- THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
-- ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
-- IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
-- PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
-- BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
-- CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
-- SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
-- BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
-- WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
-- OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
-- ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--

--
-- Script to create the Configuration Service tables
--


--
-- The Table holding the current configuration
--

-- @cmpgen.bean-name="Config"
/* @cmpgen.javadoc
 * @ejb.finder
 *    signature="java.util.Collection findAll ()"
 *    query="SELECT OBJECT(a) FROM CfgConfig AS a"
 */
CREATE TABLE cfg_config
(
   /* @@annotation
      ConfigurationKey's string representation to identify a configuration value.
      Not types because the extra data layer that hides database or propertyfile
      uses string type only.
    */
   config_key              VARCHAR2(255) NOT NULL,
   /* @@annotation
      contains the current valid value,
      is delivered whenever the config service is asked for the value for
      a given config_key
    */
   value          VARCHAR2(1000) NOT NULL,
--
   CONSTRAINT cfg_configkey_pk PRIMARY KEY (config_key) DISABLE
);

-- create primary key index for cfg_config
/* @@annotation
<para>
   This index is used to enforce the uniqueness of the primary key.
</para>
 */
CREATE UNIQUE INDEX cfg_configkey_idx
           ON cfg_config (config_key);

-- enable constraint now for cfg_config
ALTER TABLE cfg_config ENABLE PRIMARY KEY;

