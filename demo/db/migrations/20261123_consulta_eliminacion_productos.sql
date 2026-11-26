CREATE OR REPLACE FUNCTION public.consultar_producto(p_id_producto integer)
RETURNS SETOF public."PRODUCTOS"
LANGUAGE sql
STABLE
AS $$
    SELECT *
    FROM public."PRODUCTOS"
    WHERE "Id_Producto" = p_id_producto;
$$;

CREATE OR REPLACE PROCEDURE public.eliminar_producto(
    IN p_id_producto integer,
    OUT p_eliminado boolean
)
LANGUAGE plpgsql
AS $$
DECLARE
    filas_eliminadas integer;
BEGIN
    DELETE FROM public."PRODUCTOS"
    WHERE "Id_Producto" = p_id_producto;

    GET DIAGNOSTICS filas_eliminadas = ROW_COUNT;
    p_eliminado := filas_eliminadas > 0;
END;
$$;
