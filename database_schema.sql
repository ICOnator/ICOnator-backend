-- HIBERNATE
CREATE SEQUENCE hibernate_sequence
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;

-- TABLE: EXCHANGE RATE
CREATE TABLE exchange_rate (
  id            SERIAL PRIMARY KEY,
  block_nr_btc  BIGINT,
  block_nr_eth  BIGINT,
  creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  rate_btc      NUMERIC(19, 2),
  rate_eth      NUMERIC(19, 2),
  rate_eth_bitfinex NUMERIC(19, 2),
  rate_btc_bitfinex NUMERIC(19, 2)
);
CREATE INDEX block_nr_btc_idx
  ON exchange_rate USING BTREE (block_nr_btc);
CREATE INDEX block_nr_eth_idx
  ON exchange_rate USING BTREE (block_nr_eth);

-- TABLE: INVESTOR
CREATE TABLE investor (
  id                        BIGINT                      NOT NULL,
  creation_date             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  email                     CHARACTER VARYING(255)      NOT NULL,
  email_confirmation_token  CHARACTER VARYING(255)      NOT NULL,
  pay_in_bitcoin_public_key CHARACTER VARYING(255),
  pay_in_ether_public_key   CHARACTER VARYING(255),
  refund_bitcoin_address    CHARACTER VARYING(255),
  refund_ether_address      CHARACTER VARYING(255),
  wallet_address            CHARACTER VARYING(255)
);
ALTER TABLE ONLY investor
  ADD CONSTRAINT investor_pkey PRIMARY KEY (id);
ALTER TABLE ONLY investor
  ADD CONSTRAINT uk_pq634155ri1eyk0jda0dy7pe0 UNIQUE (pay_in_bitcoin_public_key);
ALTER TABLE ONLY investor
  ADD CONSTRAINT uk_2ewhqslx5pmaq4nso6ghssamn UNIQUE (pay_in_ether_public_key);
ALTER TABLE ONLY investor
  ADD CONSTRAINT uk_4teiyank4csqihvfqn3gc7rkl UNIQUE (email);
ALTER TABLE ONLY investor
  ADD CONSTRAINT uk_ghpff4u8022blk2vlgcug68es UNIQUE (email_confirmation_token);
CREATE INDEX pay_in_bitcoin_public_key_idx
  ON investor USING BTREE (pay_in_bitcoin_public_key);
CREATE INDEX pay_in_ether_public_key_idx
  ON investor USING BTREE (pay_in_ether_public_key);

--- TABLE: PAYIN
CREATE TABLE payin (
  id             SERIAL PRIMARY KEY,
  block_nr_btc   BIGINT,
  block_nr_eth   BIGINT,
  creation_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  satoshi        BIGINT,
  "time"         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  wallet_address CHARACTER VARYING(255),
  wei            BIGINT
);
CREATE INDEX time_idx
  ON payin USING BTREE ("time");
CREATE INDEX wallet_address2_idx
  ON payin USING BTREE (wallet_address);

--- TABLE: PAYMENT_LOG
CREATE TABLE payment_log (
  tx_identifier  CHARACTER VARYING(255) PRIMARY KEY,
  creation_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  currency       CHARACTER VARYING(255) NOT NULL,
  paymentvalue   BIGINT NOT NULL,
  fx_rate        NUMERIC(19, 2),
  usd            NUMERIC(19, 2),
  blocktime      TIMESTAMP WITHOUT TIME ZONE,
  email          CHARACTER VARYING(255)
);
CREATE INDEX email_payment_idx
  ON payment_log USING BTREE ("email");

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
  PERFORM pg_notify(CAST('bitcoin' AS TEXT), NEW.pay_in_bitcoin_public_key);
  PERFORM pg_notify(CAST('ether' AS TEXT), NEW.pay_in_ether_public_key);
  RETURN NEW;
END;
$$;
CREATE TRIGGER notify_new_payin_address
AFTER UPDATE OF pay_in_ether_public_key, pay_in_bitcoin_public_key ON investor
FOR EACH ROW
EXECUTE PROCEDURE notify_new_payin_address();

--- SEQUENCE: FRESH KEY
CREATE SEQUENCE fresh_key
START WITH 1
INCREMENT BY 1
NO MINVALUE
NO MAXVALUE
CACHE 1;
