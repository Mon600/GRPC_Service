INSERT INTO addresses (id, country, city, street)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'Russia', 'Moscow', 'Tverskaya Street, 12'),
    ('00000000-0000-0000-0000-000000000102', 'Russia', 'Saint Petersburg', 'Nevsky Prospect, 28'),
    ('00000000-0000-0000-0000-000000000103', 'Russia', 'Kazan', 'Bauman Street, 7'),
    ('00000000-0000-0000-0000-000000000104', 'Russia', 'Yekaterinburg', 'Lenina Avenue, 45'),
    ('00000000-0000-0000-0000-000000000105', 'Kazakhstan', 'Almaty', 'Abay Avenue, 15');

INSERT INTO clients (
    id,
    name,
    surname,
    birthday,
    gender,
    registration_date,
    address_id
)
VALUES
    (
        '00000000-0000-0000-0000-000000000201',
        'Ivan',
        'Petrov',
        DATE '1992-04-15',
        'MALE',
        DATE '2025-01-10',
        '00000000-0000-0000-0000-000000000101'
    ),
    (
        '00000000-0000-0000-0000-000000000202',
        'Anna',
        'Smirnova',
        DATE '1988-11-03',
        'FEMALE',
        DATE '2025-03-22',
        '00000000-0000-0000-0000-000000000102'
    ),
    (
        '00000000-0000-0000-0000-000000000203',
        'Timur',
        'Karimov',
        DATE '2000-07-27',
        'MALE',
        DATE '2026-02-05',
        '00000000-0000-0000-0000-000000000103'
    );

-- A valid 1x1 PNG placeholder. The schema stores only the image bytes and has
-- no columns for a file name or media type, so the same neutral image is used
-- for all test records.
INSERT INTO images (id, image)
VALUES
    (
        '00000000-0000-0000-0000-000000000301',
        decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=', 'base64')
    ),
    (
        '00000000-0000-0000-0000-000000000302',
        decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=', 'base64')
    ),
    (
        '00000000-0000-0000-0000-000000000303',
        decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=', 'base64')
    );

INSERT INTO suppliers (id, name, address_id, phone_number)
VALUES
    (
        '00000000-0000-0000-0000-000000000401',
        'Ural Electronics',
        '00000000-0000-0000-0000-000000000104',
        '+7-343-555-01-01'
    ),
    (
        '00000000-0000-0000-0000-000000000402',
        'Volga Home',
        '00000000-0000-0000-0000-000000000103',
        '+7-843-555-02-02'
    ),
    (
        '00000000-0000-0000-0000-000000000403',
        'Central Asia Trade',
        '00000000-0000-0000-0000-000000000105',
        '+7-727-555-03-03'
    );

INSERT INTO products (
    id,
    name,
    category,
    price,
    available_stock,
    last_update_date,
    supplier_id,
    image_id
)
VALUES
    (
        '00000000-0000-0000-0000-000000000501',
        'Wireless Keyboard',
        'Electronics',
        3490.00,
        24,
        DATE '2026-09-01',
        '00000000-0000-0000-0000-000000000401',
        '00000000-0000-0000-0000-000000000301'
    ),
    (
        '00000000-0000-0000-0000-000000000502',
        'USB-C Hub',
        'Electronics',
        4290.50,
        15,
        DATE '2026-09-10',
        '00000000-0000-0000-0000-000000000401',
        '00000000-0000-0000-0000-000000000302'
    ),
    (
        '00000000-0000-0000-0000-000000000503',
        'Electric Kettle',
        'Home Appliances',
        2799.90,
        38,
        DATE '2026-08-18',
        '00000000-0000-0000-0000-000000000402',
        '00000000-0000-0000-0000-000000000303'
    ),
    (
        '00000000-0000-0000-0000-000000000504',
        'Desk Lamp',
        'Home',
        1890.00,
        0,
        DATE '2026-09-15',
        '00000000-0000-0000-0000-000000000403',
        '00000000-0000-0000-0000-000000000301'
    );
