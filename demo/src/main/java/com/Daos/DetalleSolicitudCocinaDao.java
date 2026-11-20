package com.Daos;

import com.model.DetalleSolicitudCocina;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas de líneas solicitadas y entregas ligadas al inventario. */
public class DetalleSolicitudCocinaDao {
    public void insertar(Connection conexion, int idSolicitud, DetalleSolicitudCocina detalle) throws SQLException {
        String sql = "INSERT INTO public.\"DETALLE_SOLICITUD_COCINA\" "
                + "(\"id_solicitud_cocina\", \"Id_Producto\", \"Cantidad_Solicitada\", \"Cantidad_Entregada\") "
                + "VALUES (?, ?, ?, 0)";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, idSolicitud);
            consulta.setInt(2, detalle.getIdProducto());
            consulta.setInt(3, detalle.getCantidadSolicitada());
            consulta.executeUpdate();
        }
    }

    public List<DetalleSolicitudCocina> listarPorSolicitud(int idSolicitud) throws SQLException {
        String sql = "SELECT d.\"Id_Detalle\", d.\"Id_Producto\", p.\"Codigo\", p.\"Descripcion\", "
                + "d.\"Cantidad_Solicitada\", d.\"Cantidad_Entregada\", p.\"Stock_Inventario\" "
                + "FROM public.\"DETALLE_SOLICITUD_COCINA\" d "
                + "JOIN public.\"PRODUCTOS\" p ON p.\"Id_Producto\" = d.\"Id_Producto\" "
                + "WHERE d.\"id_solicitud_cocina\" = ? ORDER BY d.\"Id_Detalle\"";
        List<DetalleSolicitudCocina> detalles = new ArrayList<>();
        try (Connection conexion = com.Connection.ConexionDatabase.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, idSolicitud);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    DetalleSolicitudCocina detalle = new DetalleSolicitudCocina();
                    detalle.setIdDetalle(resultado.getInt("Id_Detalle"));
                    detalle.setIdProducto(resultado.getInt("Id_Producto"));
                    detalle.setCodigoProducto(resultado.getString("Codigo"));
                    detalle.setNombreProducto(resultado.getString("Descripcion"));
                    detalle.setCantidadSolicitada(resultado.getInt("Cantidad_Solicitada"));
                    detalle.setCantidadEntregada(resultado.getInt("Cantidad_Entregada"));
                    detalle.setStockActual(resultado.getInt("Stock_Inventario"));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    public List<DetalleSolicitudCocina> bloquearPendientes(Connection conexion, int idSolicitud) throws SQLException {
        String sql = "SELECT d.\"Id_Detalle\", d.\"Id_Producto\", d.\"Cantidad_Solicitada\", "
                + "d.\"Cantidad_Entregada\", p.\"Stock_Inventario\" "
                + "FROM public.\"DETALLE_SOLICITUD_COCINA\" d "
                + "JOIN public.\"PRODUCTOS\" p ON p.\"Id_Producto\" = d.\"Id_Producto\" "
                + "WHERE d.\"id_solicitud_cocina\" = ? ORDER BY d.\"Id_Detalle\" FOR UPDATE OF d, p";
        List<DetalleSolicitudCocina> detalles = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, idSolicitud);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    DetalleSolicitudCocina detalle = new DetalleSolicitudCocina();
                    detalle.setIdDetalle(resultado.getInt("Id_Detalle"));
                    detalle.setIdProducto(resultado.getInt("Id_Producto"));
                    detalle.setCantidadSolicitada(resultado.getInt("Cantidad_Solicitada"));
                    detalle.setCantidadEntregada(resultado.getInt("Cantidad_Entregada"));
                    detalle.setStockActual(resultado.getInt("Stock_Inventario"));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    public void actualizarEntrega(Connection conexion, int idDetalle, int cantidadNueva) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(
                "UPDATE public.\"DETALLE_SOLICITUD_COCINA\" "
                        + "SET \"Cantidad_Entregada\" = \"Cantidad_Entregada\" + ? WHERE \"Id_Detalle\" = ?")) {
            consulta.setInt(1, cantidadNueva);
            consulta.setInt(2, idDetalle);
            if (consulta.executeUpdate() != 1) throw new SQLException("No se pudo actualizar la entrega.");
        }
    }
}
