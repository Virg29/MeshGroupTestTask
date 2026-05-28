ALTER TABLE account ADD COLUMN target_balance_stockpiling DECIMAL(19, 2);

UPDATE account SET target_balance_stockpiling = ROUND(balance * 2.07, 2);

ALTER TABLE account ALTER COLUMN target_balance_stockpiling SET NOT NULL;
