DELIMITER $$

-- Trigger para expense_installments (INSERT)
-- Trigger for handling inserts before they occur, setting creation and update timestamps
CREATE TRIGGER before_insert_expense_installments
    BEFORE INSERT ON expense_installments
    FOR EACH ROW
BEGIN
    -- Set creation and last updated to NOW, and enabled to true
    SET NEW.created_datetime = NOW();
  SET NEW.last_updated_datetime = NOW();
  SET NEW.enabled = TRUE;
  -- Keep created_user as is, captured before insert
  SET NEW.created_user = NEW.created_user;
END $$

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
  DECLARE last_version INT;
    -- Update only the last updated timestamp, do not modify creation
    SET NEW.last_updated_datetime = NOW();
  -- created_user should not change
  SET NEW.created_user = OLD.created_user;
  -- Obtener la última versión y agregar una nueva versión con los datos actualizados

  SELECT MAX(version) INTO last_version FROM expense_installments_audit WHERE id = NEW.id;

  INSERT INTO expense_installments_audit (id, expense_id, payment_date, installment_number, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.expense_id, NEW.payment_date, NEW.installment_number, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
END$$

-- Trigger para expenses (INSERT)
-- Trigger for handling inserts before they occur, setting creation and update timestamps
CREATE TRIGGER before_insert_expenses
    BEFORE INSERT ON expenses
    FOR EACH ROW
BEGIN
    -- Set creation and last updated to NOW, and enabled to true
    SET NEW.created_datetime = NOW();
  SET NEW.last_updated_datetime = NOW();
  SET NEW.enabled = TRUE;
  -- Keep created_user as is, captured before insert
  SET NEW.created_user = NEW.created_user;
END $$

CREATE TRIGGER after_insert_expenses
AFTER INSERT ON expenses
FOR EACH ROW
BEGIN

  -- Insertar la primera versión en la tabla de auditoría
  INSERT INTO expenses_audit (id, description, provider_id, expense_date, file_id, invoice_number, expense_type, expense_category_id, amount, installments, created_datetime, created_user, last_updated_datetime, last_updated_user, note_credit,enabled, version)
  VALUES (NEW.id, NEW.description, NEW.provider_id, NEW.expense_date, NEW.file_id, NEW.invoice_number, NEW.expense_type, NEW.expense_category_id, NEW.amount, NEW.installments, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.note_credit,NEW.enabled, 1);
END$$

-- Trigger para expenses (UPDATE)
CREATE TRIGGER before_update_expenses
BEFORE UPDATE ON expenses
FOR EACH ROW
BEGIN
  DECLARE last_version INT;
    -- Update only the last updated timestamp, do not modify creation
    SET NEW.last_updated_datetime = NOW();
  -- created_user should not change
  SET NEW.created_user = OLD.created_user;
  -- Obtener la última versión y agregar una nueva versión con los datos actualizados

  SELECT MAX(version) INTO last_version FROM expenses_audit WHERE id = NEW.id;

  INSERT INTO expenses_audit (id, description, provider_id, expense_date, file_id, invoice_number, expense_type, expense_category_id, amount, installments, created_datetime, created_user, last_updated_datetime, last_updated_user, note_credit,enabled, version)
  VALUES (NEW.id, NEW.description, NEW.provider_id, NEW.expense_date, NEW.file_id, NEW.invoice_number, NEW.expense_type, NEW.expense_category_id, NEW.amount, NEW.installments, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.note_credit,NEW.enabled, last_version + 1);
END$$

-- Trigger para expense_categories (INSERT)
CREATE TRIGGER before_insert_expense_categories
    BEFORE INSERT ON expense_categories
    FOR EACH ROW
BEGIN
    -- Set creation and last updated to NOW, and enabled to true
    SET NEW.created_datetime = NOW();
  SET NEW.last_updated_datetime = NOW();
  SET NEW.enabled = TRUE;
  -- Keep created_user as is, captured before insert
  SET NEW.created_user = NEW.created_user;
END $$

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
  DECLARE last_version INT;
    -- Update only the last updated timestamp, do not modify creation
    SET NEW.last_updated_datetime = NOW();
  -- created_user should not change
  SET NEW.created_user = OLD.created_user;
  -- Obtener la última versión y agregar una nueva versión con los datos actualizados

  SELECT MAX(version) INTO last_version FROM expense_categories_audit WHERE id = NEW.id;

  INSERT INTO expense_categories_audit (id, description, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.description, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
END$$

-- Trigger para expense_distribution (INSERT)
-- Trigger for handling inserts before they occur, setting creation and update timestamps
CREATE TRIGGER before_insert_expense_distribution
    BEFORE INSERT ON expense_distribution
    FOR EACH ROW
BEGIN
    -- Set creation and last updated to NOW, and enabled to true
    SET NEW.created_datetime = NOW();
  SET NEW.last_updated_datetime = NOW();
  SET NEW.enabled = TRUE;
  -- Keep created_user as is, captured before insert
  SET NEW.created_user = NEW.created_user;
END $$

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
  DECLARE last_version INT;
    -- Update only the last updated timestamp, do not modify creation
    SET NEW.last_updated_datetime = NOW();
  -- created_user should not change
  SET NEW.created_user = OLD.created_user;
  -- Obtener la última versión y agregar una nueva versión con los datos actualizados

  SELECT MAX(version) INTO last_version FROM expense_distribution_audit WHERE id = NEW.id;

  INSERT INTO expense_distribution_audit (id, owner_id, expense_id, proportion, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.owner_id, NEW.expense_id, NEW.proportion, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
END$$
-- Trigger para bills_record (INSERT)
-- Trigger for handling inserts before they occur, setting creation and update timestamps
CREATE TRIGGER before_insert_bills_record
BEFORE INSERT ON bills_record
FOR EACH ROW
BEGIN
    -- Set creation and last updated to NOW, and enabled to true
    SET NEW.created_datetime = NOW();
  SET NEW.last_updated_datetime = NOW();
  SET NEW.enabled = TRUE;
  -- Keep created_user as is, captured before insert
  SET NEW.created_user = NEW.created_user;
END $$

CREATE TRIGGER after_insert_bills_record
AFTER INSERT ON bills_record
FOR EACH ROW
BEGIN
  -- Insertar la primera versión en la tabla de auditoría
  INSERT INTO bills_record_audit (id, start_date, end_date, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.start_date, NEW.end_date, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, 1);
END$$

-- Trigger para bills_record (UPDATE)
CREATE TRIGGER before_update_bills_record
BEFORE UPDATE ON bills_record
FOR EACH ROW
BEGIN
  DECLARE last_version INT;
    -- Update only the last updated timestamp, do not modify creation
    SET NEW.last_updated_datetime = NOW();
  -- created_user should not change
  SET NEW.created_user = OLD.created_user;
  -- Obtener la última versión y agregar una nueva versión con los datos actualizados

  SELECT MAX(version) INTO last_version FROM bills_record_audit WHERE id = NEW.id;

  INSERT INTO bills_record_audit (id, start_date, end_date, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.start_date, NEW.end_date, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
END$$

-- Trigger para bills_expense_owners (INSERT)
-- Trigger for handling inserts before they occur, setting creation and update timestamps
CREATE TRIGGER before_insert_bills_expense_owners
    BEFORE INSERT ON bills_expense_owners
    FOR EACH ROW
BEGIN
    -- Set creation and last updated to NOW, and enabled to true
    SET NEW.created_datetime = NOW();
  SET NEW.last_updated_datetime = NOW();
  SET NEW.enabled = TRUE;
  -- Keep created_user as is, captured before insert
  SET NEW.created_user = NEW.created_user;
END $$

CREATE TRIGGER after_insert_bills_expense_owners
AFTER INSERT ON bills_expense_owners
FOR EACH ROW
BEGIN
  -- Insertar la primera versión en la tabla de auditoría
  INSERT INTO bills_expense_owners_audit (id, bill_record_id, owner_id, field_size, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.bill_record_id, NEW.owner_id, NEW.field_size, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, 1);
END$$

-- Trigger para bills_expense_owners (UPDATE)
CREATE TRIGGER before_update_bills_expense_owners
BEFORE UPDATE ON bills_expense_owners
FOR EACH ROW
BEGIN
  DECLARE last_version INT;
    -- Update only the last updated timestamp, do not modify creation
    SET NEW.last_updated_datetime = NOW();
  -- created_user should not change
  SET NEW.created_user = OLD.created_user;
  -- Obtener la última versión y agregar una nueva versión con los datos actualizados

  SELECT MAX(version) INTO last_version FROM bills_expense_owners_audit WHERE id = NEW.id;

  INSERT INTO bills_expense_owners_audit (id, bill_record_id, owner_id, field_size, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.bill_record_id, NEW.owner_id, NEW.field_size, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
END$$

-- Trigger para bills_expense_fines (INSERT)
-- Trigger for handling inserts before they occur, setting creation and update timestamps
CREATE TRIGGER before_insert_bills_expense_fines
    BEFORE INSERT ON bills_expense_fines
    FOR EACH ROW
BEGIN
    -- Set creation and last updated to NOW, and enabled to true
    SET NEW.created_datetime = NOW();
  SET NEW.last_updated_datetime = NOW();
  SET NEW.enabled = TRUE;
  -- Keep created_user as is, captured before insert
  SET NEW.created_user = NEW.created_user;
END $$

CREATE TRIGGER after_insert_bills_expense_fines
AFTER INSERT ON bills_expense_fines
FOR EACH ROW
BEGIN
  -- Insertar la primera versión en la tabla de auditoría
  INSERT INTO bills_expense_fines_audit (id, description,bill_expense_owner_id, fine_id, plot_id, amount, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.description,NEW.bill_expense_owner_id, NEW.fine_id, NEW.plot_id, NEW.amount, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, 1);
END$$

-- Trigger para bills_expense_fines (UPDATE)
CREATE TRIGGER before_update_bills_expense_fines
BEFORE UPDATE ON bills_expense_fines
FOR EACH ROW
BEGIN
  DECLARE last_version INT;
    -- Update only the last updated timestamp, do not modify creation
    SET NEW.last_updated_datetime = NOW();
  -- created_user should not change
  SET NEW.created_user = OLD.created_user;
  -- Obtener la última versión y agregar una nueva versión con los datos actualizados

  SELECT MAX(version) INTO last_version FROM bills_expense_fines_audit WHERE id = NEW.id;

  INSERT INTO bills_expense_fines_audit (id, description,bill_expense_owner_id, fine_id, plot_id,amount, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id,NEW.description,NEW.bill_expense_owner_id, NEW.fine_id, NEW.plot_id,NEW.amount, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
END$$

-- Trigger para bills_expense_installments (INSERT)
-- Trigger for handling inserts before they occur, setting creation and update timestamps
CREATE TRIGGER before_insert_bills_expense_installments
    BEFORE INSERT ON bills_expense_installments
    FOR EACH ROW
BEGIN
    -- Set creation and last updated to NOW, and enabled to true
    SET NEW.created_datetime = NOW();
  SET NEW.last_updated_datetime = NOW();
  SET NEW.enabled = TRUE;
  -- Keep created_user as is, captured before insert
  SET NEW.created_user = NEW.created_user;
END $$

CREATE TRIGGER after_insert_bills_expense_installments
AFTER INSERT ON bills_expense_installments
FOR EACH ROW
BEGIN
  -- Insertar la primera versión en la tabla de auditoría
  INSERT INTO bills_expense_installments_audit (id, description,bill_expense_owner_id, expense_installment_id, amount, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.description,NEW.bill_expense_owner_id, NEW.expense_installment_id, NEW.amount, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, 1);
END$$

-- Trigger para bills_expense_installments (UPDATE)
CREATE TRIGGER before_update_bills_expense_installments
BEFORE UPDATE ON bills_expense_installments
FOR EACH ROW
BEGIN
  DECLARE last_version INT;
    -- Update only the last updated timestamp, do not modify creation
    SET NEW.last_updated_datetime = NOW();
  -- created_user should not change
  SET NEW.created_user = OLD.created_user;
  -- Obtener la última versión y agregar una nueva versión con los datos actualizados

  SELECT MAX(version) INTO last_version FROM bills_expense_installments_audit WHERE id = NEW.id;

  INSERT INTO bills_expense_installments_audit (id, description,bill_expense_owner_id, expense_installment_id, amount, created_datetime, created_user, last_updated_datetime, last_updated_user, enabled, version)
  VALUES (NEW.id, NEW.description,NEW.bill_expense_owner_id, NEW.expense_installment_id, NEW.amount, NEW.created_datetime, NEW.created_user, NEW.last_updated_datetime, NEW.last_updated_user, NEW.enabled, last_version + 1);
END$$
DELIMITER ;
