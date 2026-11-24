ALTER TABLE public."USUARIO"
    ALTER COLUMN "Contraseña" TYPE text
    USING "Contraseña"::text;
