CREATE OR REPLACE FUNCTION sync_products_sold_out()
RETURNS TRIGGER AS $$
BEGIN
    NEW.sold_out := NEW.available_quantity <= 0;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

UPDATE products
SET sold_out = (available_quantity <= 0)
WHERE sold_out <> (available_quantity <= 0);

DROP TRIGGER IF EXISTS trg_products_sold_out ON products;

CREATE TRIGGER trg_products_sold_out
BEFORE INSERT OR UPDATE ON products
FOR EACH ROW EXECUTE FUNCTION sync_products_sold_out();

ALTER TABLE products
ADD CONSTRAINT ck_products_sold_out_matches_quantity
CHECK (sold_out = (available_quantity <= 0));

CREATE INDEX IF NOT EXISTS idx_products_catalog
ON products (active, sold_out, created_at DESC);
