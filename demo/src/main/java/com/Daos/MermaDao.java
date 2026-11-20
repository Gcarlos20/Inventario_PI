package com.Daos;

import com.model.Merma;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Inserta movimientos de baja dentro de la transacción del servicio. */
public class MermaDao {
    public void insertarMovimiento(Connection conexion, Merma merma, String motivo,
                                   int stockAnterior, int stockNuevo, int idUsuario)
            throws SQLException {
        String sql = "INSERT INTO public.\"MOVIMIENTOS\" "
                + "(\"Tipo_Movimiento\", \"Cantidad\", \"Stock_Anterior\", \"Stock_Nuevo\", "
                + "\"Motivo\", \"Fecha_Movimiento\", \"Observacion\", \"Id_Tipo_Movimiento\", "
                + "\"Id_Producto\", \"Id_Usuario\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, "Salida");
            consulta.setInt(2, merma.getCantidad());
            consulta.setInt(3, stockAnterior);
            consulta.setInt(4, stockNuevo);
            consulta.setString(5, motivo);
            consulta.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            consulta.setString(7, merma.getObservacion());
            consulta.setInt(8, merma.getIdTipoMovimiento());
            consulta.setInt(9, merma.getIdProducto());
            consulta.setInt(10, idUsuario);
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el movimiento de baja.");
            }
        }
    }
}
