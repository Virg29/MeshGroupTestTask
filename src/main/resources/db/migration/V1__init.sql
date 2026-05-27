CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(500) NOT NULL,
    date_of_birth DATE         NOT NULL,
    password      VARCHAR(500) NOT NULL
);

CREATE TABLE account (
    id      BIGSERIAL      PRIMARY KEY,
    user_id BIGINT         NOT NULL UNIQUE,
    balance DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE email_data (
    id      BIGSERIAL    PRIMARY KEY,
    user_id BIGINT       NOT NULL,
    email   VARCHAR(200) NOT NULL UNIQUE,
    CONSTRAINT fk_email_data_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE phone_data (
    id      BIGSERIAL   PRIMARY KEY,
    user_id BIGINT      NOT NULL,
    phone   VARCHAR(13) NOT NULL UNIQUE,
    CONSTRAINT fk_phone_data_user FOREIGN KEY (user_id) REFERENCES users (id)
);
