CREATE OR REPLACE PROCEDURE public.registrar_baja_merma(
    IN p_id_producto integer,
    IN p_id_lote integer,
    IN p_cantidad integer,
    IN p_id_tipo_movimiento integer,
    IN p_motivo text,
    IN p_observacion text,
    IN p_id_usuario integer,
    IN p_stock_anterior integer,
    IN p_stock_nuevo integer,
    IN p_estado_lote_sin_existencias text
)
LANGUAGE plpgsql
AS $$
DECLARE
    filas_actualizadas integer;
BEGIN
    IF p_cantidad <= 0 OR p_stock_nuevo <> p_stock_anterior - p_cantidad THEN
        RAISE EXCEPTION 'Los datos de cantidad o stock de la baja no son válidos.';
    END IF;

    UPDATE public."PRODUCTOS"
    SET "Stock_Inventario" = p_stock_nuevo,
        "Estado" = CASE
            WHEN p_stock_nuevo <= 0 THEN 'Agotado'
            WHEN p_stock_nuevo <= "Stock_Minimo" THEN 'Poco stock'
            ELSE 'Disponible'
        END
    WHERE "Id_Producto" = p_id_producto
      AND "Stock_Inventario" = p_stock_anterior;

    GET DIAGNOSTICS filas_actualizadas = ROW_COUNT;
    IF filas_actualizadas <> 1 THEN
        RAISE EXCEPTION 'No se pudo actualizar el stock del producto %.', p_id_producto;
    END IF;

    IF p_id_lote > 0 THEN
        UPDATE public."LOTE"
        SET "Cantidad_Actual" = "Cantidad_Actual" - p_cantidad,
            "Estado" = CASE
                WHEN "Cantidad_Actual" - p_cantidad = 0
                    THEN p_estado_lote_sin_existencias
                ELSE "Estado"
            END
        WHERE "Id_Lote" = p_id_lote
          AND "Id_producto" = p_id_producto
          AND "Estado" = 'Activo'
          AND "Cantidad_Actual" >= p_cantidad;

        GET DIAGNOSTICS filas_actualizadas = ROW_COUNT;
        IF filas_actualizadas <> 1 THEN
            RAISE EXCEPTION 'No se pudo descontar la cantidad del lote.';
        END IF;
    END IF;

    INSERT INTO public."MOVIMIENTOS" (
        "Tipo_Movimiento", "Cantidad", "Stock_Anterior", "Stock_Nuevo",
        "Motivo", "Fecha_Movimiento", "Observacion", "Id_Tipo_Movimiento",
        "Id_Producto", "Id_Usuario"
    ) VALUES (
        'Salida', p_cantidad, p_stock_anterior, p_stock_nuevo,
        p_motivo, CURRENT_TIMESTAMP, p_observacion, p_id_tipo_movimiento,
        p_id_producto, p_id_usuario
    );
END;
$$;
