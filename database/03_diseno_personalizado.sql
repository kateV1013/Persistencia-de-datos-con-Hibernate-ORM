-- Ejecutar una vez sobre la base Quilate existente ANTES de iniciar esta versión.
-- Es repetible y conserva todos los pedidos y demás registros.
BEGIN;
ALTER TABLE pedido_personalizado
    ADD COLUMN IF NOT EXISTS diseno_texto varchar(40),
    ADD COLUMN IF NOT EXISTS diseno_forma varchar(16),
    ADD COLUMN IF NOT EXISTS diseno_acabado varchar(16),
    ADD COLUMN IF NOT EXISTS diseno_fuente varchar(16),
    ADD COLUMN IF NOT EXISTS diseno_medida_mm numeric(5,2),
    ADD COLUMN IF NOT EXISTS diseno_ancho_mm numeric(5,2),
    ADD COLUMN IF NOT EXISTS diseno_letra_mm numeric(4,2);
COMMIT;
