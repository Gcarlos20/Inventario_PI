package com.Daos;

import com.model.DetalleCompra;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DetalleCompraDao {

    /** Inserta un detalle usando la conexión transaccional del llamador. */
    public int insertar(Connection conexion, DetalleCompra detalle, int idCompra,
                        int stockAnterior, int stockNuevo) throws SQLException {
        String sql = "INSERT INTO public.\"DETALLE_COMPRA\" "
                + "(\"Cantidad\", \"Precio_Decimal\", \"Stock_Anterior\", \"Stock_Nuevo\", "
                + "\"Fecha\", \"Observacion\", \"Id_Compra\", \"Id_Producto\") "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING \"Id_Detalle\"";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, detalle.getCantidad());
            consulta.setBigDecimal(2, detalle.getPrecioDecimal());
            consulta.setString(3, Integer.toString(stockAnterior));
            consulta.setString(4, Integer.toString(stockNuevo));
            LocalDate fecha = detalle.getFecha() != null ? detalle.getFecha() : LocalDate.now();
            consulta.setDate(5, Date.valueOf(fecha));
            consulta.setString(6, detalle.getObservacion());
            consulta.setInt(7, idCompra);
            consulta.setInt(8, detalle.getIdProducto());

            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("PostgreSQL no devolvió el ID del detalle de compra.");
                }
                return resultado.getInt("Id_Detalle");
            }
        }
    }
}
