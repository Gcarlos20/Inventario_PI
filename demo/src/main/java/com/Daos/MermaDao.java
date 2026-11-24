package com.Daos;

import com.model.Merma;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

/** Ejecuta el procedimiento de baja dentro de la transacción del servicio. */
public class MermaDao {
    public void registrarBaja(Connection conexion, Merma merma, String motivo,
                              int stockAnterior, int stockNuevo, int idUsuario,
                              String estadoLoteSinExistencias)
            throws SQLException {
        String sql = "{ call public.registrar_baja_merma(?, ?, ?, ?, "
                + "CAST(? AS text), CAST(? AS text), ?, ?, ?, CAST(? AS text)) }";
        try (CallableStatement llamada = conexion.prepareCall(sql)) {
            llamada.setInt(1, merma.getIdProducto());
            llamada.setInt(2, merma.getIdLote());
            llamada.setInt(3, merma.getCantidad());
            llamada.setInt(4, merma.getIdTipoMovimiento());
            llamada.setString(5, motivo);
            llamada.setString(6, merma.getObservacion());
            llamada.setInt(7, idUsuario);
            llamada.setInt(8, stockAnterior);
            llamada.setInt(9, stockNuevo);
            llamada.setString(10, estadoLoteSinExistencias);
            llamada.execute();
        }
    }
}
