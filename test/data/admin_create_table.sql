-- ADMIN SERVICE DATA TABLE
CREATE TABLE ADM_AUDIT_LOG (
   PARTITION_NUMBER          NUMBER DEFAULT(0) NOT NULL,
   AUDIT_LOG_ID              NUMBER            NOT NULL,
   ACTION_TIMESTAMP          TIMESTAMP         NOT NULL,
   USER_NAME                 VARCHAR2(255)     NOT NULL,
   ROLE                      VARCHAR2(350)     NOT NULL,
   SERVICE                   VARCHAR2(32)      NOT NULL,
   FUNCTION                  VARCHAR2(32)      NOT NULL,
   PARAMETERS                VARCHAR2(1024),
   RESULT                    NUMBER(1)         NOT NULL,
   ERROR_REASON              VARCHAR2(255),
--   
   CONSTRAINT adm_audit_log_pk PRIMARY KEY (AUDIT_LOG_ID) DISABLE,
--
   CONSTRAINT adm_check_result_type CHECK (RESULT IN (0, 1))
)
PARTITION BY LIST (PARTITION_NUMBER)
(PARTITION P0 VALUES (0));

-- create primary key index for adm_audit_log
create unique index adm_idx_audit_log_pk
           on adm_audit_log (AUDIT_LOG_ID);
           
-- enable constraint now for adm_audit_log
ALTER TABLE adm_audit_log ENABLE PRIMARY KEY;


-- ADMIN SERVICE STATE TABLE
CREATE TABLE ADM_AUDITLOG_STATE (
   PARTITION_NAME       VARCHAR2(32) NOT NULL,
   AUDITLOG_START       TIMESTAMP    NOT NULL,
   AUDITLOG_END         TIMESTAMP,
   STATE                VARCHAR2(32) NOT NULL,
--
   CONSTRAINT adm_state_pk PRIMARY KEY (PARTITION_NAME) DISABLE,
--
      CONSTRAINT adm_check_state
      CHECK (STATE IN
            ('CREATED',
             'AUDITLOG IN USE',
             'AUDITLOG FINISHED',
             'BACKUP FINISHED',
             'DROPPED',
             'UNKNOWN'))
);

-- create primary key index for adm_auditlog_state
create unique index adm_idx_auditlog_state_pk
           on adm_auditlog_state (PARTITION_NAME);
           
-- enable constraint now for adm_audit_log
ALTER TABLE adm_audit_log ENABLE PRIMARY KEY;

-- insert first partition into state table
INSERT INTO adm_auditlog_state (Partition_Name,auditlog_Start,State) VALUES
   ('p0',SYSDATE,'AUDITLOG IN USE');


-- ADMIN SERVICE AUDIT LOG TABLE PARTITIONING SEQUENCE
CREATE SEQUENCE ADM_PARNUMBER_SEQUENCE
   START WITH 1
   INCREMENT BY 1
   NOCACHE
   NOCYCLE
   ORDER;

-- ADMIN SERVICE AUDIT LOG TABLE LINE ID SEQUENCE
CREATE SEQUENCE ADM_LINE_ID_SEQUENCE
   START WITH 1
   INCREMENT BY 1
   NOCACHE
   NOCYCLE
   ORDER;

