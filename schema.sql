-- HIBERNATE
CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- TABLE: EXCHANGE RATE
CREATE TABLE exchange_rate (
    id bigint NOT NULL,
    block_nr_btc bigint,
    block_nr_eth bigint,
    creation_date timestamp without time zone NOT NULL,
    rate_btc numeric(19,2),
    rate_eth numeric(19,2)
);
ALTER TABLE ONLY exchange_rate ADD CONSTRAINT exchange_rate_pkey PRIMARY KEY (id);
CREATE INDEX block_nr_btc_idx ON exchange_rate USING btree (block_nr_btc);
CREATE INDEX block_nr_eth_idx ON exchange_rate USING btree (block_nr_eth);

-- TABLE: INVESTOR
CREATE TABLE investor (
    id bigint NOT NULL,
    creation_date timestamp without time zone NOT NULL,
    email character varying(255) NOT NULL,
    email_confirmation_token character varying(255) NOT NULL,
    pay_in_bitcoin_address character varying(255),
    pay_in_bitcoin_private_key character varying(255),
    pay_in_ether_address character varying(255),
    pay_in_ether_private_key character varying(255),
    refund_bitcoin_address character varying(255),
    refund_ether_address character varying(255),
    wallet_address character varying(255)
);
ALTER TABLE ONLY investor ADD CONSTRAINT investor_pkey PRIMARY KEY (id);
ALTER TABLE ONLY investor ADD CONSTRAINT uk_4k053milr6lu0imylnf3y50cd UNIQUE (pay_in_bitcoin_address);
ALTER TABLE ONLY investor ADD CONSTRAINT uk_4teiyank4csqihvfqn3gc7rkl UNIQUE (email);
ALTER TABLE ONLY investor ADD CONSTRAINT uk_bqjd9xfku41d55g7pf8te674l UNIQUE (pay_in_ether_address);
ALTER TABLE ONLY investor ADD CONSTRAINT uk_ghpff4u8022blk2vlgcug68es UNIQUE (email_confirmation_token);

--- TABLE: PAYIN
CREATE TABLE payin (
    id bigint NOT NULL,
    block_nr_btc bigint,
    block_nr_eth bigint,
    creation_date timestamp without time zone NOT NULL,
    satoshi bigint,
    "time" timestamp without time zone NOT NULL,
    wallet_address character varying(255),
    wei bigint
);
ALTER TABLE ONLY payin ADD CONSTRAINT payin_pkey PRIMARY KEY (id);
CREATE INDEX time_idx ON payin USING btree ("time");
CREATE INDEX wallet_address2_idx ON payin USING btree (wallet_address);

--- TABLE: KEYPAIRS
CREATE TABLE keypairs (
    id bigint NOT NULL,
    public_btc character(66),
    public_eth character(130)
);
CREATE SEQUENCE keypairs_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
ALTER SEQUENCE keypairs_id_seq OWNED BY keypairs.id;
ALTER TABLE ONLY keypairs ALTER COLUMN id SET DEFAULT nextval('keypairs_id_seq'::regclass);
ALTER TABLE ONLY keypairs ADD CONSTRAINT keypairs_pkey PRIMARY KEY (id);

--- MONITOR TRIGGER
CREATE FUNCTION notify_new_payin_address() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  PERFORM pg_notify(CAST('bitcoin' AS TEXT),NEW.pay_in_bitcoin_address);
  PERFORM pg_notify(CAST('ether' AS TEXT),NEW.pay_in_ether_address);
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
