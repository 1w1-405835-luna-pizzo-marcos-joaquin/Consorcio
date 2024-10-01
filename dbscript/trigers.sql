DELIMITER $$

-- Trigger para expense_installments (INSERT)
CREATE TRIGGER after_insert_expense_installments
    AFTER INSERT ON expense_installments
    FOR EACH ROW
BEGIN
    -- Insertar la primera versión en la tabla de auditoría
    INSERT INTO expense_installments_audit (id, expense_id, payment_date, installment_number, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
    VALUES (NEW.id, NEW.expense_id, NEW.payment_date, NEW.installment_number, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, 1);
    END$$

    -- Trigger para expense_installments (UPDATE)
    CREATE TRIGGER before_update_expense_installments
        BEFORE UPDATE ON expense_installments
        FOR EACH ROW
    BEGIN
        -- Obtener la última versión y agregar una nueva versión con los datos actualizados
        DECLARE last_version INT;
        SELECT MAX(version) INTO last_version FROM expense_installments_audit WHERE id = NEW.id;

        INSERT INTO expense_installments_audit (id, expense_id, payment_date, installment_number, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
        VALUES (NEW.id, NEW.expense_id, NEW.payment_date, NEW.installment_number, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
        END$$

        -- Trigger para expenses (INSERT)
        CREATE TRIGGER after_insert_expenses
            AFTER INSERT ON expenses
            FOR EACH ROW
        BEGIN
            -- Insertar la primera versión en la tabla de auditoría
            INSERT INTO expenses_audit (id, description, provider_id, expense_date, file_id, invoice_number, expense_type, expense_category_id, amount, installments, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
            VALUES (NEW.id, NEW.description, NEW.provider_id, NEW.expense_date, NEW.file_id, NEW.invoice_number, NEW.expense_type, NEW.expense_category_id, NEW.amount, NEW.installments, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, 1);
            END$$

            -- Trigger para expenses (UPDATE)
            CREATE TRIGGER before_update_expenses
                BEFORE UPDATE ON expenses
                FOR EACH ROW
            BEGIN
                -- Obtener la última versión y agregar una nueva versión con los datos actualizados
                DECLARE last_version INT;
                SELECT MAX(version) INTO last_version FROM expenses_audit WHERE id = NEW.id;

                INSERT INTO expenses_audit (id, description, provider_id, expense_date, file_id, invoice_number, expense_type, expense_category_id, amount, installments, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
                VALUES (NEW.id, NEW.description, NEW.provider_id, NEW.expense_date, NEW.file_id, NEW.invoice_number, NEW.expense_type, NEW.expense_category_id, NEW.amount, NEW.installments, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
                END$$

                -- Trigger para expense_categories (INSERT)
                CREATE TRIGGER after_insert_expense_categories
                    AFTER INSERT ON expense_categories
                    FOR EACH ROW
                BEGIN
                    -- Insertar la primera versión en la tabla de auditoría
                    INSERT INTO expense_categories_audit (id, description, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
                    VALUES (NEW.id, NEW.description, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, 1);
                    END$$

                    -- Trigger para expense_categories (UPDATE)
                    CREATE TRIGGER before_update_expense_categories
                        BEFORE UPDATE ON expense_categories
                        FOR EACH ROW
                    BEGIN
                        -- Obtener la última versión y agregar una nueva versión con los datos actualizados
                        DECLARE last_version INT;
                        SELECT MAX(version) INTO last_version FROM expense_categories_audit WHERE id = NEW.id;

                        INSERT INTO expense_categories_audit (id, description, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
                        VALUES (NEW.id, NEW.description, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
                        END$$

                        -- Trigger para expense_distribution (INSERT)
                        CREATE TRIGGER after_insert_expense_distribution
                            AFTER INSERT ON expense_distribution
                            FOR EACH ROW
                        BEGIN
                            -- Insertar la primera versión en la tabla de auditoría
                            INSERT INTO expense_distribution_audit (id, owner_id, expense_id, proportion, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
                            VALUES (NEW.id, NEW.owner_id, NEW.expense_id, NEW.proportion, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, 1);
                            END$$

                            -- Trigger para expense_distribution (UPDATE)
                            CREATE TRIGGER before_update_expense_distribution
                                BEFORE UPDATE ON expense_distribution
                                FOR EACH ROW
                            BEGIN
                                -- Obtener la última versión y agregar una nueva versión con los datos actualizados
                                DECLARE last_version INT;
                                SELECT MAX(version) INTO last_version FROM expense_distribution_audit WHERE id = NEW.id;

                                INSERT INTO expense_distribution_audit (id, owner_id, expense_id, proportion, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
                                VALUES (NEW.id, NEW.owner_id, NEW.expense_id, NEW.proportion, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
                                END$$

                                DELIMITER ;