INSERT INTO sale_tier (`id`, `tier_no`, `description`, `start_date`, `end_date`, `discount`, `tokens_sold`, `token_max`, `is_active`)
VALUES (0, 0, 'pre-sale tier', '2018-03-01', '2018-03-10', 0.2, 0, 2000, TRUE);
INSERT INTO sale_tier (`id`, `tier_no`, `description`, `start_date`, `end_date`, `discount`, `tokens_sold`, `token_max`, `is_active`)
VALUES (1, 1, 'main tier', '2018-03-11', '2018-03-20', 0.1, 0, 20000, FALSE);
INSERT INTO sale_tier (`id`, `tier_no`, `description`, `start_date`, `end_date`, `discount`, `tokens_sold`, `token_max`, `is_active`)
VALUES (2, 2, 'main tier', '2018-03-21', '2018-03-30', 0.0, 0, 200000, FALSE);
