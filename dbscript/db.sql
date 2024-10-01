-- Creación de las tablas principales
CREATE TABLE expense_categories (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    description VARCHAR(255),
                                    created_datetime DATETIME default now(),
                                    created_user INT not null,
                                    last_updated_datetime DATETIME default now(),
                                    last_updated_user INT not null,
                                    enabled BOOLEAN default 1
);

CREATE TABLE expenses (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          description VARCHAR(255),
                          provider_id INT,
                          expense_date DATE not null,
                          file_id BINARY(16), -- UUID in MySQL as BINARY(16)
                          invoice_number INT,
                          expense_type VARCHAR(30) not null,
                          expense_category_id INT not null,
                          amount DECIMAL(11,2) not null,
                          installments INT not null,
                          created_datetime DATETIME default now(),
                          created_user INT not null,
                          last_updated_datetime DATETIME default now(),
                          last_updated_user INT not null,
                          enabled BOOLEAN default 1,
                          FOREIGN KEY (expense_category_id) REFERENCES expense_categories(id)
);

CREATE TABLE expense_distribution (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      owner_id INT not null,
                                      expense_id INT not null,
                                      proportion DECIMAL(3,2) not null,
                                      created_datetime DATETIME default now(),
                                      created_user INT not null,
                                      last_updated_datetime DATETIME default now(),
                                      last_updated_user INT not null,
                                      enabled BOOLEAN default 1,
                                      FOREIGN KEY (expense_id) REFERENCES expenses(id)
);

CREATE TABLE expense_installments (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      expense_id INT,
                                      payment_date DATE,
                                      installment_number INT,
                                      created_datetime DATETIME,
                                      created_user INT,
                                      last_updated_datetime DATETIME,
                                      last_updated_user INT not null,
                                      enabled BOOLEAN default 1,
                                      FOREIGN KEY (expense_id) REFERENCES expenses(id)
);

-- Creación de las tablas de auditoría
-- Tabla de auditoría para expense_categories
CREATE TABLE expense_categories_audit (
                                          id INT NOT NULL,
                                          description VARCHAR(255),
                                          created_datetime DATETIME DEFAULT now(),
                                          created_user INT NOT NULL,
                                          last_updated_datetime DATETIME DEFAULT now(),
                                          last_updated_user INT NOT NULL,
                                          enabled BOOLEAN DEFAULT 1,
                                          version INT NOT NULL,
                                          PRIMARY KEY (id, version)
);

-- Tabla de auditoría para expenses
CREATE TABLE expenses_audit (
                                id INT NOT NULL,
                                description VARCHAR(255),
                                provider_id INT,
                                expense_date DATE NOT NULL,
                                file_id BINARY(16), -- UUID in MySQL as BINARY(16)
                                invoice_number INT,
                                expense_type VARCHAR(30) NOT NULL,
                                expense_category_id INT NOT NULL,
                                amount DECIMAL(11,2) NOT NULL,
                                installments INT NOT NULL,
                                created_datetime DATETIME DEFAULT now(),
                                created_user INT NOT NULL,
                                last_updated_datetime DATETIME DEFAULT now(),
                                last_updated_user INT NOT NULL,
                                enabled BOOLEAN DEFAULT 1,
                                version INT NOT NULL,
                                PRIMARY KEY (id, version)
);

-- Tabla de auditoría para expense_distribution
CREATE TABLE expense_distribution_audit (
                                            id INT NOT NULL,
                                            owner_id INT NOT NULL,
                                            expense_id INT NOT NULL,
                                            proportion DECIMAL(3,2) NOT NULL,
                                            created_datetime DATETIME DEFAULT now(),
                                            created_user INT NOT NULL,
                                            last_updated_datetime DATETIME DEFAULT now(),
                                            last_updated_user INT NOT NULL,
                                            enabled BOOLEAN DEFAULT 1,
                                            version INT NOT NULL,
                                            PRIMARY KEY (id, version)
);

-- Tabla de auditoría para expense_installments
CREATE TABLE expense_installments_audit (
                                            id INT NOT NULL,
                                            expense_id INT,
                                            payment_date DATE,
                                            installment_number INT,
                                            created_datetime DATETIME DEFAULT now(),
                                            created_user INT NOT NULL,
                                            last_updated_datetime DATETIME DEFAULT now(),
                                            last_updated_user INT NOT NULL,
                                            enabled BOOLEAN DEFAULT 1,
                                            version INT NOT NULL,
                                            PRIMARY KEY (id, version)
);