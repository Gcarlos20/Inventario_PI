package com.Daos;

import com.model.Compra;
import com.model.DetalleCompra;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Ejecuta sentencias SQL de compra y movimiento con la conexión del servicio. */
public class CompraDao {
    /** Inserta la cabecera de compra usando la conexión transaccional del servicio. */
    public int insertar(Connection conexion, Compra compra) throws SQLException {
        String sql = "INSERT INTO public.\"COMPRAS\" "
                + "(\"Fecha_Compras\", \"Numero_Factura\", \"SubTotal\", \"TotalCompra\", "
                + "\"Metodo_Pago\", \"Estado\", \"Observacion\", \"Id_Usuario\", \"Id_Proveedores\") "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING \"Id_Compra\"";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            LocalDate fecha = compra.getFechaCompras() != null
                    ? compra.getFechaCompras() : LocalDate.now();
            consulta.setDate(1, Date.valueOf(fecha));
            consulta.setString(2, compra.getNumeroFactura());
            consulta.setBigDecimal(3, compra.getSubTotal());
            consulta.setBigDecimal(4, compra.getTotalCompra());
            consulta.setString(5, compra.getMetodoPago());
            consulta.setString(6, compra.getEstado());
            consulta.setString(7, compra.getObservacion());
            consulta.setInt(8, compra.getIdUsuario());
            consulta.setInt(9, compra.getIdProveedores());

            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("PostgreSQL no devolvió el ID de la compra.");
                }
                return resultado.getInt("Id_Compra");
            }
        }
    }

    /** Inserta el movimiento de inventario usando la conexión transaccional del servicio. */
    public void insertarMovimiento(Connection conexion, Compra compra, DetalleCompra detalle,
                                    int idTipoMovimiento, int stockAnterior, int stockNuevo)
            throws SQLException {
        String sql = "INSERT INTO public.\"MOVIMIENTOS\" "
                + "(\"Tipo_Movimiento\", \"Cantidad\", \"Stock_Anterior\", \"Stock_Nuevo\", "
                + "\"Motivo\", \"Fecha_Movimiento\", \"Observacion\", "
                + "\"Id_Tipo_Movimiento\", \"Id_Producto\", \"Id_Usuario\") "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, "Entrada");
            consulta.setInt(2, detalle.getCantidad());
            consulta.setInt(3, stockAnterior);
            consulta.setInt(4, stockNuevo);
            consulta.setString(5, "Compra");
            consulta.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            consulta.setString(7, detalle.getObservacion());
            consulta.setInt(8, idTipoMovimiento);
            consulta.setInt(9, detalle.getIdProducto());
            consulta.setInt(10, compra.getIdUsuario());

            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el movimiento de entrada.");
            }
        }
    }

}
