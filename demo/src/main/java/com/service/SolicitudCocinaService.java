package com.service;

import com.Connection.ConexionDatabase;
import com.Daos.DetalleSolicitudCocinaDao;
import com.Daos.ProductoDAO;
import com.Daos.SolicitudCocinaDao;
import com.model.DetalleSolicitudCocina;
import com.model.SolicitudCocina;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/** Reglas y transacciones para solicitar y despachar insumos de cocina. */
public class SolicitudCocinaService {
    private static final int ID_TIPO_MOVIMIENTO_SALIDA = 2;
    private final SolicitudCocinaDao solicitudDao = new SolicitudCocinaDao();
    private final DetalleSolicitudCocinaDao detalleDao = new DetalleSolicitudCocinaDao();
    private final ProductoDAO productoDao = new ProductoDAO();

    public List<SolicitudCocina> listar() throws SQLException { return solicitudDao.listar(); }
    public List<SolicitudCocina> listarDeUsuario(int idUsuario) throws SQLException {
        if (idUsuario <= 0) throw new IllegalArgumentException("El usuario debe ser válido.");
        return solicitudDao.listar(idUsuario);
    }
    public List<DetalleSolicitudCocina> listarDetalles(int idSolicitud) throws SQLException {
        return detalleDao.listarPorSolicitud(idSolicitud);
    }

    public int crear(SolicitudCocina solicitud) throws SQLException {
        validar(solicitud);
        try (Connection conexion = ConexionDatabase.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int id = solicitudDao.insertar(conexion, solicitud);
                for (DetalleSolicitudCocina detalle : solicitud.getDetalles()) {
                    detalleDao.insertar(conexion, id, detalle);
                }
                conexion.commit();
                return id;
            } catch (SQLException | RuntimeException error) {
                rollback(conexion, error);
                throw error;
            }
        }
    }

    /** Entrega todo lo que haya disponible y deja pendiente el saldo sin existencias. */
    public String entregarDisponible(int idSolicitud, int idUsuario) throws SQLException {
        if (idSolicitud <= 0 || idUsuario <= 0) throw new IllegalArgumentException("Solicitud o usuario no válido.");
        try (Connection conexion = ConexionDatabase.conectar()) {
            conexion.setAutoCommit(false);
            try {
                String estado;
                try (PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT \"Estado\" FROM public.\"SOLICITUDES_COCINA\" "
                                + "WHERE \"Id_Solicitud\" = ? FOR UPDATE")) {
                    consulta.setInt(1, idSolicitud);
                    try (ResultSet resultado = consulta.executeQuery()) {
                        if (!resultado.next()) throw new SQLException("No existe la solicitud seleccionada.");
                        estado = resultado.getString(1);
                    }
                }
                if ("Rechazada".equals(estado) || "Entregada".equals(estado)) {
                    throw new SQLException("Esta solicitud ya está cerrada.");
                }

                List<DetalleSolicitudCocina> detalles = detalleDao.bloquearPendientes(conexion, idSolicitud);
                if (detalles.isEmpty()) throw new SQLException("La solicitud no contiene productos.");
                boolean entregoAlgo = false;
                boolean quedaPendiente = false;
                for (DetalleSolicitudCocina detalle : detalles) {
                    int restante = detalle.getCantidadSolicitada() - detalle.getCantidadEntregada();
                    if (restante <= 0) continue;
                    int entregado = Math.min(restante, detalle.getStockActual());
                    if (entregado > 0) {
                        int stockNuevo = detalle.getStockActual() - entregado;
                        productoDao.actualizarStock(conexion, detalle.getIdProducto(), stockNuevo);
                        detalleDao.actualizarEntrega(conexion, detalle.getIdDetalle(), entregado);
                        insertarMovimiento(conexion, idSolicitud, idUsuario, detalle, entregado, stockNuevo);
                        entregoAlgo = true;
                    }
                    if (entregado < restante) quedaPendiente = true;
                }
                if (!entregoAlgo) throw new SQLException("No hay existencias disponibles para entregar.");
                solicitudDao.actualizarEstado(conexion, idSolicitud, quedaPendiente ? "Parcial" : "Entregada");
                conexion.commit();
                return quedaPendiente ? "Parcial" : "Entregada";
            } catch (SQLException | RuntimeException error) {
                rollback(conexion, error);
                throw error;
            }
        }
    }

    public void rechazar(int idSolicitud) throws SQLException {
        try (Connection conexion = ConexionDatabase.conectar()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement consulta = conexion.prepareStatement(
                    "UPDATE public.\"SOLICITUDES_COCINA\" SET \"Estado\" = 'Rechazada' "
                            + "WHERE \"Id_Solicitud\" = ? AND \"Estado\" IN ('Pendiente', 'Parcial')")) {
                consulta.setInt(1, idSolicitud);
                if (consulta.executeUpdate() != 1) throw new SQLException("La solicitud no está pendiente.");
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                rollback(conexion, error);
                throw error;
            }
        }
    }

    private void insertarMovimiento(Connection conexion, int idSolicitud, int idUsuario,
                                    DetalleSolicitudCocina detalle, int cantidad, int stockNuevo)
            throws SQLException {
        String sql = "INSERT INTO public.\"MOVIMIENTOS\" "
                + "(\"Tipo_Movimiento\", \"Cantidad\", \"Stock_Anterior\", \"Stock_Nuevo\", "
                + "\"Motivo\", \"Fecha_Movimiento\", \"Observacion\", \"Id_Tipo_Movimiento\", "
                + "\"Id_Producto\", \"Id_Usuario\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, "Salida");
            consulta.setInt(2, cantidad);
            consulta.setInt(3, stockNuevo + cantidad);
            consulta.setInt(4, stockNuevo);
            consulta.setString(5, "Solicitud de cocina");
            consulta.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            consulta.setString(7, "Solicitud de cocina #" + idSolicitud);
            consulta.setInt(8, ID_TIPO_MOVIMIENTO_SALIDA);
            consulta.setInt(9, detalle.getIdProducto());
            consulta.setInt(10, idUsuario);
            if (consulta.executeUpdate() != 1) throw new SQLException("No se pudo registrar la salida de inventario.");
        }
    }

    private void validar(SolicitudCocina solicitud) {
        if (solicitud == null || solicitud.getIdUsuario() <= 0 || solicitud.getDetalles() == null
                || solicitud.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La solicitud requiere un usuario y al menos un producto.");
        }
        for (DetalleSolicitudCocina detalle : solicitud.getDetalles()) {
            if (detalle == null || detalle.getIdProducto() <= 0 || detalle.getCantidadSolicitada() <= 0) {
                throw new IllegalArgumentException("Cada producto debe tener una cantidad mayor que cero.");
            }
        }
    }

    private void rollback(Connection conexion, Exception error) {
        try { conexion.rollback(); } catch (SQLException fallo) { error.addSuppressed(fallo); }
    }
}
