package com.Daos;

import com.model.DetalleCompra;
import com.model.Lote;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoteDao {

    /** Lista los lotes con existencias que todavía pueden descontarse. */
    public List<Lote> listarActivosPorProducto(int idProducto) throws SQLException {
        if (idProducto <= 0) throw new IllegalArgumentException("El ID del producto no es válido.");
        String sql = "SELECT \"Id_Lote\", \"Fechas_Ingreso\", \"Fecha_Vencimiento\", "
                + "\"Cantidad_Inicial\", \"Cantidad_Actual\", \"Estado\", \"Total\", "
                + "\"Id_producto\", \"Id_Detalle\" FROM public.\"LOTE\" "
                + "WHERE \"Id_producto\" = ? AND \"Estado\" = 'Activo' AND \"Cantidad_Actual\" > 0 "
                + "ORDER BY \"Fecha_Vencimiento\" ASC NULLS LAST, \"Id_Lote\" ASC";
        List<Lote> lotes = new ArrayList<>();
        try (Connection conexion = com.Connection.ConexionDatabase.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, idProducto);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) lotes.add(mapear(resultado));
            }
        }
        return lotes;
    }

    /** Lee y bloquea un lote durante la transacción de baja. */
    public Lote obtenerParaActualizar(Connection conexion, int idLote, int idProducto)
            throws SQLException {
        String sql = "SELECT \"Id_Lote\", \"Fechas_Ingreso\", \"Fecha_Vencimiento\", "
                + "\"Cantidad_Inicial\", \"Cantidad_Actual\", \"Estado\", \"Total\", "
                + "\"Id_producto\", \"Id_Detalle\" FROM public.\"LOTE\" "
                + "WHERE \"Id_Lote\" = ? AND \"Id_producto\" = ? FOR UPDATE";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, idLote);
            consulta.setInt(2, idProducto);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("No existe el lote seleccionado para el producto indicado.");
                }
                return mapear(resultado);
            }
        }
    }

    /** Descuenta existencias y cambia el estado solo cuando el lote queda vacío. */
    public void descontarCantidad(Connection conexion, int idLote, int cantidad,
                                  String estadoSinExistencias) throws SQLException {
        if (idLote <= 0 || cantidad <= 0) {
            throw new IllegalArgumentException("El lote y la cantidad deben ser válidos.");
        }
        if (!"Agotado".equals(estadoSinExistencias) && !"Vencido".equals(estadoSinExistencias)) {
            throw new IllegalArgumentException("El estado final del lote no es válido.");
        }
        String sql = "UPDATE public.\"LOTE\" SET "
                + "\"Cantidad_Actual\" = \"Cantidad_Actual\" - ?, "
                + "\"Estado\" = CASE WHEN \"Cantidad_Actual\" - ? = 0 THEN ? ELSE \"Estado\" END "
                + "WHERE \"Id_Lote\" = ? AND \"Estado\" = 'Activo' AND \"Cantidad_Actual\" >= ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, cantidad);
            consulta.setInt(2, cantidad);
            consulta.setString(3, estadoSinExistencias);
            consulta.setInt(4, idLote);
            consulta.setInt(5, cantidad);
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No se pudo descontar la cantidad del lote.");
            }
        }
    }

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

    private Lote mapear(ResultSet resultado) throws SQLException {
        Date fechaIngreso = resultado.getDate("Fechas_Ingreso");
        Date fechaVencimiento = resultado.getDate("Fecha_Vencimiento");
        return new Lote(
                resultado.getInt("Id_Lote"),
                fechaIngreso != null ? fechaIngreso.toLocalDate() : null,
                fechaVencimiento != null ? fechaVencimiento.toLocalDate() : null,
                resultado.getInt("Cantidad_Inicial"),
                resultado.getInt("Cantidad_Actual"),
                resultado.getString("Estado"),
                resultado.getBigDecimal("Total"),
                resultado.getInt("Id_producto"),
                resultado.getInt("Id_Detalle"));
    }
}
