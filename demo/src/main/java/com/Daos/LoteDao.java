package com.Daos;

import com.model.DetalleCompra;
import com.model.Lote;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class LoteDao {

    /** Inserta un lote ligado al detalle y producto usando la conexión del llamador. */
    public void insertar(Connection conexion, Lote lote, DetalleCompra detalle,
                         int idDetalle) throws SQLException {
        String sql = "INSERT INTO public.\"LOTE\" "
                + "(\"Fechas_Ingreso\", \"Fecha_Vencimiento\", \"Cantidad_Inicial\", "
                + "\"Cantidad_Actual\", \"Estado\", \"Total\", \"Id_producto\", \"Id_Detalle\") "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            LocalDate fechaIngreso = lote.getFechasIngreso() != null
                    ? lote.getFechasIngreso() : LocalDate.now();
            consulta.setDate(1, Date.valueOf(fechaIngreso));
            if (lote.getFechaVencimiento() != null) {
                consulta.setDate(2, Date.valueOf(lote.getFechaVencimiento()));
            } else {
                consulta.setNull(2, java.sql.Types.DATE);
            }
            consulta.setInt(3, detalle.getCantidad());
            consulta.setInt(4, detalle.getCantidad());
            consulta.setString(5, "Activo");
            BigDecimal total = lote.getTotal() != null
                    ? lote.getTotal()
                    : detalle.getPrecioDecimal().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            consulta.setBigDecimal(6, total);
            consulta.setInt(7, detalle.getIdProducto());
            consulta.setInt(8, idDetalle);

            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el lote de la compra.");
            }
        }
    }
}
