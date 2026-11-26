CREATE OR REPLACE PROCEDURE public.actualizar_producto(
    IN p_id_producto integer,
    IN p_codigo text,
    IN p_precio double precision,
    IN p_stock_inventario integer,
    IN p_descripcion text,
    IN p_estado text,
    IN p_stock_minimo integer,
    OUT p_actualizado boolean
)
LANGUAGE plpgsql
AS $$
DECLARE
    filas_actualizadas integer;
BEGIN
    UPDATE public."PRODUCTOS"
    SET "Codigo" = p_codigo,
        "Precio" = p_precio,
        "Stock_Inventario" = p_stock_inventario,
        "Descripcion" = p_descripcion,
        "Estado" = p_estado,
        "Stock_Minimo" = p_stock_minimo
    WHERE "Id_Producto" = p_id_producto;

    GET DIAGNOSTICS filas_actualizadas = ROW_COUNT;
    p_actualizado := filas_actualizadas > 0;
END;
$$;
