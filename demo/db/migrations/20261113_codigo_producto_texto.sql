-- Permite usar códigos de producto como AB-123 o PROD-001.
-- Los códigos numéricos que ya existen se convierten a texto sin perder su valor.
ALTER TABLE "PRODUCTOS"
    ALTER COLUMN "Codigo" TYPE TEXT
    USING "Codigo"::TEXT;
