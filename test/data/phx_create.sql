--
-- creates a test table
--
CREATE TABLE phx_cmpgen_test
(
   -- @cmpgen.java-type="org.jcoderz.test.LongContainer"
   -- @cmpgen.store-method="toLong()"
   -- @cmpgen.load-method="fromLong(long)"
   id numeric(4) NOT NULL,
   last_name varchar2(50) NOT NULL CONSTRAINT xyz UNIQUE,
   first_name varchar2(50) DEFAULT (4/3)*3.141592 NOT NULL
      CONSTRAINT bla CHECK ( first_name IN ('blub', 'egal')),
   -- @cmpgen.java-type="org.jcoderz.ipp.Msisdn"
   -- @cmpgen.store-method="toString()"
   -- @cmpgen.load-method="fromString(java.lang.String)"
   msisdn varchar2 (16),
   balance numeric(6,2),
   CONSTRAINT bingo CHECK (last_name IN ('foo', 'bar', 'baz')),
   CONSTRAINT bongo PRIMARY KEY (id,msisdn)
);
