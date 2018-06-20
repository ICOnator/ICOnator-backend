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
