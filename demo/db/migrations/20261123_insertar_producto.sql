CREATE OR REPLACE PROCEDURE public.insertar_producto(
    IN p_codigo text,
    IN p_precio double precision,
    IN p_stock_inventario integer,
    IN p_descripcion text,
    IN p_estado text,
    IN p_stock_minimo integer,
    OUT p_id_producto integer
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO public."PRODUCTOS" (
        "Codigo", "Precio", "Stock_Inventario", "Descripcion", "Estado", "Stock_Minimo"
    ) VALUES (
        p_codigo, p_precio, p_stock_inventario, p_descripcion, p_estado, p_stock_minimo
    ) RETURNING "Id_Producto" INTO p_id_producto;
END;
$$;
