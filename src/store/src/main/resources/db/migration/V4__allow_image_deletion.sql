ALTER TABLE products
    ALTER COLUMN image_id DROP NOT NULL;

ALTER TABLE products
    DROP CONSTRAINT products_image_id_fkey;

ALTER TABLE products
    ADD CONSTRAINT products_image_id_fkey
        FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE SET NULL;
