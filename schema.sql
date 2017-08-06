-- HIBERNATE
CREATE SEQUENCE hibernate_sequence
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;

-- TABLE: EXCHANGE RATE
CREATE TABLE exchange_rate (
  id            BIGINT                      NOT NULL,
  block_nr_btc  BIGINT,
  block_nr_eth  BIGINT,
  creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  rate_btc      NUMERIC(19, 2),
  rate_eth      NUMERIC(19, 2)
);
ALTER TABLE ONLY exchange_rate
  ADD CONSTRAINT exchange_rate_pkey PRIMARY KEY (id);
CREATE INDEX block_nr_btc_idx
  ON exchange_rate USING BTREE (block_nr_btc);
CREATE INDEX block_nr_eth_idx
  ON exchange_rate USING BTREE (block_nr_eth);

-- TABLE: INVESTOR
CREATE TABLE investor (
  id                         BIGINT                      NOT NULL,
  creation_date              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  email                      CHARACTER VARYING(255)      NOT NULL,
  email_confirmation_token   CHARACTER VARYING(255)      NOT NULL,
  pay_in_bitcoin_address     CHARACTER VARYING(255),
  pay_in_bitcoin_private_key CHARACTER VARYING(255),
  pay_in_ether_address       CHARACTER VARYING(255),
  pay_in_ether_private_key   CHARACTER VARYING(255),
  refund_bitcoin_address     CHARACTER VARYING(255),
  refund_ether_address       CHARACTER VARYING(255),
  wallet_address             CHARACTER VARYING(255)
);
ALTER TABLE ONLY investor
  ADD CONSTRAINT investor_pkey PRIMARY KEY (id);
ALTER TABLE ONLY investor
  ADD CONSTRAINT uk_4k053milr6lu0imylnf3y50cd UNIQUE (pay_in_bitcoin_address);
ALTER TABLE ONLY investor
  ADD CONSTRAINT uk_4teiyank4csqihvfqn3gc7rkl UNIQUE (email);
ALTER TABLE ONLY investor
  ADD CONSTRAINT uk_bqjd9xfku41d55g7pf8te674l UNIQUE (pay_in_ether_address);
ALTER TABLE ONLY investor
  ADD CONSTRAINT uk_ghpff4u8022blk2vlgcug68es UNIQUE (email_confirmation_token);

--- TABLE: PAYIN
CREATE TABLE payin (
  id             BIGINT                      NOT NULL,
  block_nr_btc   BIGINT,
  block_nr_eth   BIGINT,
  creation_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  satoshi        BIGINT,
  "time"         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  wallet_address CHARACTER VARYING(255),
  wei            BIGINT
);
ALTER TABLE ONLY payin
  ADD CONSTRAINT payin_pkey PRIMARY KEY (id);
CREATE INDEX time_idx
  ON payin USING BTREE ("time");
CREATE INDEX wallet_address2_idx
  ON payin USING BTREE (wallet_address);

--- TABLE: KEYPAIRS
CREATE TABLE keypairs (
  id         BIGINT NOT NULL,
  public_btc CHARACTER VARYING(255) NOT NULL,
  public_eth CHARACTER VARYING(255) NOT NULL
);
CREATE SEQUENCE keypairs_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE keypairs_id_seq OWNED BY keypairs.id;
ALTER TABLE ONLY keypairs
  ALTER COLUMN id SET DEFAULT nextval('keypairs_id_seq' :: REGCLASS);
ALTER TABLE ONLY keypairs
  ADD CONSTRAINT keypairs_pkey PRIMARY KEY (id);
ALTER TABLE ONLY keypairs
  ADD CONSTRAINT uk_a3qdthqcbj3yndxp7t7p7pjm8 UNIQUE (public_btc);
ALTER TABLE ONLY keypairs
  ADD CONSTRAINT uk_hqy3luubfa88n459a0y2ur08y UNIQUE (public_eth);

--- MONITOR TRIGGER
CREATE FUNCTION notify_new_payin_address()
  RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  PERFORM pg_notify(CAST('bitcoin' AS TEXT), NEW.pay_in_bitcoin_address);
  PERFORM pg_notify(CAST('ether' AS TEXT), NEW.pay_in_ether_address);
  RETURN NEW;
END;
$$;

--- SEQUENCE: FRESH KEY
CREATE SEQUENCE fresh_key
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;
