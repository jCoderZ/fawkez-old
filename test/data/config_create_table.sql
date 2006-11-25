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
-- Configuration service SQL create tables script
--


--
-- table cfg_config
--
CREATE TABLE cfg_config (
   the_key                   VARCHAR2(50)    NOT NULL,
   value                     VARCHAR2(255)   NOT NULL,
   new_value                 VARCHAR2(255),
   old_value                 VARCHAR2(255),
   modification_time         TIMESTAMP,
   admin_name                VARCHAR2 (50),
   modified                  NUMBER          NOT NULL,
   last_committed            NUMBER          NOT NULL,
--
   CONSTRAINT cfg_config_pk PRIMARY KEY (the_key) DISABLE,
--
   CONSTRAINT cfg_check_mod_flag CHECK (modified IN (0, 1)),
   CONSTRAINT cfg_check_commit_flag CHECK (last_committed IN (0, 1))
);

-- create primary key index for cfg_config
CREATE UNIQUE INDEX cfg_idx_config_pk
           ON cfg_config (the_key);

-- enable constraint now for cfg_config
ALTER TABLE cfg_config ENABLE PRIMARY KEY;


--
-- table cfg_app_server_line
--
CREATE TABLE cfg_app_server_line (
   name                      VARCHAR2(25)      NOT NULL,
   nsior                     VARCHAR2(3000)            ,
   host                      VARCHAR2(25)              ,
   port                      VARCHAR2(5)               ,
   status                    NUMBER            NOT NULL,
--
   CONSTRAINT cfg_app_server_line_pk PRIMARY KEY (name) DISABLE,
   CONSTRAINT app_server_line_status_check
      CHECK (status in (0, 1))
);

-- enable constraint now for cfg_config
ALTER TABLE cfg_app_server_line ENABLE PRIMARY KEY;

