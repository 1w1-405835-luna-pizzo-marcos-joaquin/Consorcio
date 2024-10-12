-- First, ensure the categories exist (this part seems correct in your script)
INSERT INTO expense_categories (description, created_user, last_updated_user)
SELECT * FROM (
                  SELECT 'Mantenimiento' AS description, 1 AS created_user, 1 AS last_updated_user UNION ALL
                  SELECT 'Servicios', 1, 1 UNION ALL
                  SELECT 'Reparaciones', 1, 1 UNION ALL
                  SELECT 'Administración', 1, 1 UNION ALL
                  SELECT 'Seguridad', 1, 1
              ) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM expense_categories WHERE description = tmp.description
) LIMIT 5;

-- Now, insert the expenses with appropriate category IDs
INSERT INTO expenses (description, expense_date, expense_type, expense_category_id, amount, installments, created_user, last_updated_user)
SELECT
    tmp.description,
    tmp.expense_date,
    tmp.expense_type,
    COALESCE((SELECT id FROM expense_categories WHERE description = tmp.category), 1) AS expense_category_id,
    tmp.amount,
    tmp.installments,
    tmp.created_user,
    tmp.last_updated_user
FROM (
         SELECT 'Pintura de fachada' AS description, '2024-03-01' AS expense_date, 'COMUN' AS expense_type,
                'Mantenimiento' AS category, 5000.00 AS amount, 1 AS installments, 1 AS created_user, 1 AS last_updated_user
         UNION ALL
         SELECT 'Reparación ascensor', '2024-03-15', 'EXTRAORDINARIO',
                'Reparaciones', 8000.00, 2, 1, 1
         UNION ALL
         SELECT 'Servicio de limpieza', '2024-04-01', 'INDIVIDUAL',
                'Servicios', 3000.00, 1, 1, 1
     ) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM expenses WHERE description = tmp.description AND expense_date = tmp.expense_date
)
    LIMIT 3;

-- Insertar distribución de gastos si no existe
INSERT INTO expense_distribution (owner_id, expense_id, proportion, created_user, last_updated_user)
SELECT * FROM (
                  SELECT 1 AS owner_id, (SELECT id FROM expenses WHERE description = 'Pintura de fachada' LIMIT 1) AS expense_id, 0.25 AS proportion, 1 AS created_user, 1 AS last_updated_user UNION ALL
SELECT 2, (SELECT id FROM expenses WHERE description = 'Pintura de fachada' LIMIT 1), 0.25, 1, 1 UNION ALL
SELECT 3, (SELECT id FROM expenses WHERE description = 'Pintura de fachada' LIMIT 1), 0.25, 1, 1 UNION ALL
SELECT 4, (SELECT id FROM expenses WHERE description = 'Pintura de fachada' LIMIT 1), 0.25, 1, 1
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM expense_distribution WHERE expense_id = tmp.expense_id AND owner_id = tmp.owner_id
    ) LIMIT 4;

-- Insertar cuotas de gastos si no existen
INSERT INTO expense_installments (expense_id, payment_date, installment_number, created_user, last_updated_user)
SELECT * FROM (
                  SELECT (SELECT id FROM expenses WHERE description = 'Reparación ascensor' LIMIT 1) AS expense_id, '2024-03-15' AS payment_date, 1 AS installment_number, 1 AS created_user, 1 AS last_updated_user UNION ALL
SELECT (SELECT id FROM expenses WHERE description = 'Reparación ascensor' LIMIT 1), '2024-04-15', 2, 1, 1
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM expense_installments WHERE expense_id = tmp.expense_id AND installment_number = tmp.installment_number
    ) LIMIT 2;

-- Insertar registro de facturación si no existe
INSERT INTO bills_record (start_date, end_date, created_user, last_updated_user)
SELECT * FROM (
                  SELECT '2024-03-01' AS start_date, '2024-03-31' AS end_date, 1 AS created_user, 1 AS last_updated_user
              ) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM bills_record WHERE start_date = tmp.start_date AND end_date = tmp.end_date
) LIMIT 1;

-- Insertar propietarios de gastos de facturación si no existen
INSERT INTO bills_expense_owners (bill_record_id, owner_id, field_size, created_user, last_updated_user)
SELECT * FROM (
                  SELECT (SELECT id FROM bills_record WHERE start_date = '2024-03-01' LIMIT 1) AS bill_record_id, 1 AS owner_id, 100 AS field_size, 1 AS created_user, 1 AS last_updated_user UNION ALL
SELECT (SELECT id FROM bills_record WHERE start_date = '2024-03-01' LIMIT 1), 2, 100, 1, 1 UNION ALL
SELECT (SELECT id FROM bills_record WHERE start_date = '2024-03-01' LIMIT 1), 3, 100, 1, 1 UNION ALL
SELECT (SELECT id FROM bills_record WHERE start_date = '2024-03-01' LIMIT 1), 4, 100, 1, 1
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM bills_expense_owners WHERE bill_record_id = tmp.bill_record_id AND owner_id = tmp.owner_id
    ) LIMIT 4;