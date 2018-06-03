-- Required inserts for TokenConversionServiceTest because inserting the data in the tests directly
-- doesn't make the tuples available in other threads than the main.
INSERT INTO sale_tier (`id`, `tier_no`, `version`, `description`, `start_date`, `end_date`,
`discount`, `token_max`, `tokens_sold`)
VALUES (1000, 1, 0, 'main tier', '2018-01-01', '2018-01-10', 0.5, 1000, 0);

INSERT INTO sale_tier (`id`, `tier_no`, `version`, `description`, `start_date`, `end_date`,
`discount`, `token_max`, `tokens_sold`)
VALUES (1001, 2, 0, 'second tier', '2018-01-11', '2018-01-20', 0.2, 2000, 0);

INSERT INTO sale_tier (`id`, `tier_no`, `version`, `description`, `start_date`, `end_date`,
`discount`, `token_max`, `tokens_sold`)
VALUES (1002, 3, 0, 'second tier', '2018-01-21', '2018-01-30', 0.1, 3000, 0);

-- Insert a default investor --
INSERT INTO investor (`id`, `creation_date`, `email`, `email_confirmation_token`, `ip_address`,
`pay_in_bitcoin_public_key`, `pay_in_ether_public_key`, `refund_bitcoin_address`, `refund_ether_address`,
`wallet_address`)
VALUES (0, '2018-01-21', 'email@email.com', 'confirmationToken', '127.0.0.1', 'bitcoinPubKey',
'04d761fb286f18d39223d3a7831292ba2c266ee71276e6259e1d93ba157256b8945b322246094144db26cac8fc6644c0eb8c23deac0933232c1166b5a069d929e5',
'refundAddress', 'refundETH', 'walletAddress')
