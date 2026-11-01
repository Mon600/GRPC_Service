CREATE TABLE addresses (
    id      UUID PRIMARY KEY NOT NULL,
    country VARCHAR(30) NOT NULL,
    city    varchar(100) NOT NULL,
    street  varchar(150) NOT NULL
);

CREATE TABLE clients (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    surname             VARCHAR(100) NOT NULL,
    birthday            DATE NOT NULL,
    gender              VARCHAR(20) NOT NULL,
    registration_date   DATE NOT NULL,
    address_id          UUID NOT NULL REFERENCES addresses(id),
    CONSTRAINT constraint_client_gender CHECK ( gender IN ('MALE', 'FEMALE') )
);

CREATE TABLE images (
    id      UUID PRIMARY KEY,
    image   BYTEA NOT NULL
);

CREATE TABLE suppliers (
    id              UUID PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    address_id      UUID REFERENCES addresses(id),
    phone_number    VARCHAR(30) NOT NULL
);

CREATE TABLE products (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    category            VARCHAR(100) NOT NULL,
    price               NUMERIC(12, 2) NOT NULL,
    available_stock     INTEGER NOT NULL DEFAULT 0,
    last_update_date    TIMESTAMPTZ NOT NULL,
    supplier_id         UUID NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
    image_id            UUID NOT NULL REFERENCES images(id)
);

