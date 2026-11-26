-- Asigna teléfonos ficticios a la demo y actualiza la numeración provisional.
BEGIN;

WITH demo AS (
    SELECT id_cliente,
           substring(correo from '^quilate[.]demo[.]([0-9]{3})@example[.]com$') AS numero
    FROM cliente
    WHERE correo ~ '^quilate[.]demo[.](00[1-9]|0[1-5][0-9]|060)@example[.]com$'
)
UPDATE cliente AS c
SET telefono = (84000000 + (demo.numero::integer * 79193 + 1736512) % 6000000)::text
FROM demo
WHERE c.id_cliente = demo.id_cliente
  AND (c.telefono IS NULL OR btrim(c.telefono) = '' OR c.telefono = '10000' || demo.numero);

COMMIT;
