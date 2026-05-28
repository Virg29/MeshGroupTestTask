INSERT INTO users (name, date_of_birth, password) VALUES
    ('Иван Иванов',    '1990-03-15', '$2b$10$s64YZR09BOIn7ADycIfOfO8KDVw7gfvIU8I/NZCTiUuVlx0I4Op5W'),
    ('Мария Петрова',  '1985-07-22', '$2b$10$BVhYV23CmTofY4IieLvaJeUbmuu7vaGvQrSldVet5q95drqbASFEe'),
    ('Алексей Сидоров','1995-11-01', '$2b$10$/.n8Ty5vKmS.UDJMuexoiunO2dV/W4K3Un/Wjv4tyyZArXYP/nW.G');

INSERT INTO account (user_id, balance) VALUES
    (1, 1000.00),
    (2, 2500.50),
    (3,  500.75);

INSERT INTO email_data (user_id, email) VALUES
    (1, 'ivan.ivanov@gmail.com'),
    (2, 'maria.petrova@gmail.com'),
    (3, 'alexey.sidorov@mail.ru');

INSERT INTO phone_data (user_id, phone) VALUES
    (1, '79201234567'),
    (2, '79157654321'),
    (3, '79269876543');
