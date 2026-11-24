package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.ConteoFisico;
import com.model.DetalleConteo;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Consultas de cabeceras, detalles y movimientos de conteos físicos. */
public class ConteoFisicoDao {
    public long insertarConteo(Connection conexion, ConteoFisico conteo, long idUsuario,
                               String estado) throws SQLException {
        String sql = "INSERT INTO public.\"CONTEO_FISICO\" "
                + "(\"Fecha_Conteo\", \"Estado\", \"Observacion\", \"Id_Usuario\") "
                + "VALUES (?, ?, ?, ?) RETURNING \"Id_Conteo\"";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            LocalDate fecha = conteo.getFechaConteo() != null
                    ? conteo.getFechaConteo() : LocalDate.now();
            consulta.setDate(1, Date.valueOf(fecha));
            consulta.setString(2, estado);
            consulta.setString(3, conteo.getObservacion());
            consulta.setLong(4, idUsuario);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("PostgreSQL no devolvió el ID del conteo físico.");
                }
                return resultado.getLong("Id_Conteo");
            }
        }
    }

    public BigDecimal obtenerStockSistema(Connection conexion, long idProducto)
            throws SQLException {
        String sql = "SELECT \"Stock_Inventario\" FROM public.\"PRODUCTOS\" "
                + "WHERE \"Id_Producto\" = ? FOR UPDATE";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, idProducto);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("No existe el producto con ID " + idProducto + ".");
                }
                BigDecimal stock = resultado.getBigDecimal("Stock_Inventario");
                if (stock == null) {
                    throw new SQLException("El producto " + idProducto + " no tiene un stock válido.");
                }
                return stock;
            }
        }
    }

    public void insertarDetalle(Connection conexion, long idConteo, DetalleConteo detalle)
            throws SQLException {
        String sql = "INSERT INTO public.\"DETALLE_CONTEO\" "
                + "(\"Stock_Sistema\", \"Stock_Fisico\", \"Diferencia\", \"Observacion\", "
                + "\"Id_Conteo\", \"Id_Producto\") VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setBigDecimal(1, detalle.getStockSistema());
            consulta.setBigDecimal(2, detalle.getStockFisico());
            consulta.setBigDecimal(3, detalle.getDiferencia());
            consulta.setString(4, detalle.getObservacion());
            consulta.setLong(5, idConteo);
            consulta.setLong(6, detalle.getIdProducto());
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar un detalle del conteo físico.");
            }
        }
    }

    public List<ConteoFisico> listarConteos() throws SQLException {
        String sql = "SELECT c.\"Id_Conteo\", c.\"Fecha_Conteo\", c.\"Estado\", "
                + "c.\"Observacion\", c.\"Id_Usuario\", u.\"Nombre\", u.\"Apellido\", "
                + "COALESCE(p.\"Productos_Resumen\", 'Sin productos') AS \"Productos_Resumen\" "
                + "FROM public.\"CONTEO_FISICO\" c "
                + "LEFT JOIN public.\"USUARIO\" u ON u.\"Id_Usuario\" = c.\"Id_Usuario\" "
                + "LEFT JOIN (SELECT d.\"Id_Conteo\", "
                + "COALESCE(STRING_AGG(p.\"Descripcion\", ', '), 'Sin productos') "
                + "AS \"Productos_Resumen\" "
                + "FROM public.\"DETALLE_CONTEO\" d "
                + "LEFT JOIN public.\"PRODUCTOS\" p ON p.\"Id_Producto\" = d.\"Id_Producto\" "
                + "GROUP BY d.\"Id_Conteo\") p ON p.\"Id_Conteo\" = c.\"Id_Conteo\" "
                + "ORDER BY c.\"Id_Conteo\" DESC";
        List<ConteoFisico> conteos = new ArrayList<>();
        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) {
                ConteoFisico conteo = new ConteoFisico();
                conteo.setIdConteo(resultado.getLong("Id_Conteo"));
                Date fecha = resultado.getDate("Fecha_Conteo");
                conteo.setFechaConteo(fecha != null ? fecha.toLocalDate() : null);
                conteo.setEstado(resultado.getString("Estado"));
                conteo.setObservacion(resultado.getString("Observacion"));
                conteo.setIdUsuario(resultado.getLong("Id_Usuario"));
                conteo.setNombreResponsable(nombreCompleto(
                        resultado.getString("Nombre"), resultado.getString("Apellido")));
                conteo.setProductosResumen(resultado.getString("Productos_Resumen"));
                conteos.add(conteo);
            }
        }
        return conteos;
    }

    public List<DetalleConteo> listarDetallesConteo(long idConteo) throws SQLException {
        try (Connection conexion = obtenerConexion()) {
            return listarDetallesConteo(conexion, idConteo);
        }
    }

    public List<DetalleConteo> listarDetallesConteo(Connection conexion, long idConteo)
            throws SQLException {
        String sql = "SELECT d.\"Id_Detalle_Conteo\", d.\"Stock_Sistema\", d.\"Stock_Fisico\", "
                + "d.\"Diferencia\", d.\"Observacion\", d.\"Id_Conteo\", d.\"Id_Producto\", "
                + "p.\"Codigo\", p.\"Descripcion\" "
                + "FROM public.\"DETALLE_CONTEO\" d "
                + "JOIN public.\"PRODUCTOS\" p ON p.\"Id_Producto\" = d.\"Id_Producto\" "
                + "WHERE d.\"Id_Conteo\" = ? ORDER BY d.\"Id_Detalle_Conteo\"";
        List<DetalleConteo> detalles = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, idConteo);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    DetalleConteo detalle = new DetalleConteo();
                    detalle.setIdDetalleConteo(resultado.getLong("Id_Detalle_Conteo"));
                    detalle.setStockSistema(resultado.getBigDecimal("Stock_Sistema"));
                    detalle.setStockFisico(resultado.getBigDecimal("Stock_Fisico"));
                    detalle.setDiferencia(resultado.getBigDecimal("Diferencia"));
                    detalle.setObservacion(resultado.getString("Observacion"));
                    detalle.setIdConteo(resultado.getLong("Id_Conteo"));
                    detalle.setIdProducto(resultado.getLong("Id_Producto"));
                    detalle.setCodigoProducto(resultado.getString("Codigo"));
                    detalle.setNombreProducto(resultado.getString("Descripcion"));
                    detalles.add(detalle);
                }
            }
        }
        return detalles;
    }

    public void actualizarRevision(Connection conexion, long idConteo, boolean conforme,
                                   String observacionAuditor) throws SQLException {
        String sql;
        if (conforme) {
            sql = "UPDATE public.\"CONTEO_FISICO\" SET \"Estado\" = 'Revisado' "
                    + "WHERE \"Id_Conteo\" = ?";
        } else {
            sql = "UPDATE public.\"CONTEO_FISICO\" SET \"Estado\" = 'Devuelto para reconteo', "
                    + "\"Observacion\" = CASE "
                    + "WHEN NULLIF(BTRIM(?), '') IS NULL THEN \"Observacion\" "
                    + "WHEN NULLIF(BTRIM(COALESCE(\"Observacion\", '')), '') IS NULL THEN ? "
                    + "ELSE \"Observacion\" || E'\\n' || ? END "
                    + "WHERE \"Id_Conteo\" = ?";
        }
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            if (conforme) {
                consulta.setLong(1, idConteo);
            } else {
                consulta.setString(1, observacionAuditor);
                consulta.setString(2, observacionAuditor);
                consulta.setString(3, observacionAuditor);
                consulta.setLong(4, idConteo);
            }
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No existe el conteo físico seleccionado.");
            }
        }
    }

    public DatosAprobacion obtenerDatosAprobacion(Connection conexion, long idConteo)
            throws SQLException {
        String sql = "SELECT \"Id_Usuario\", \"Estado\" FROM public.\"CONTEO_FISICO\" "
                + "WHERE \"Id_Conteo\" = ? FOR UPDATE";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, idConteo);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("No existe el conteo físico seleccionado.");
                }
                long idUsuario = resultado.getLong("Id_Usuario");
                Long idUsuarioRegistrador = resultado.wasNull() ? null : idUsuario;
                return new DatosAprobacion(idUsuarioRegistrador, resultado.getString("Estado"));
            }
        }
    }

    public void actualizarEstado(Connection conexion, long idConteo, String estado)
            throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(
                "UPDATE public.\"CONTEO_FISICO\" SET \"Estado\" = ? WHERE \"Id_Conteo\" = ?")) {
            consulta.setString(1, estado);
            consulta.setLong(2, idConteo);
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No existe el conteo físico seleccionado.");
            }
        }
    }

    public void actualizarStockProducto(Connection conexion, long idProducto, BigDecimal stockNuevo)
            throws SQLException {
        String sql = "UPDATE public.\"PRODUCTOS\" SET \"Stock_Inventario\" = ?, "
                + "\"Estado\" = CASE WHEN ? <= 0 THEN 'Agotado' "
                + "WHEN ? <= \"Stock_Minimo\" THEN 'Poco stock' ELSE 'Disponible' END "
                + "WHERE \"Id_Producto\" = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setBigDecimal(1, stockNuevo);
            consulta.setBigDecimal(2, stockNuevo);
            consulta.setBigDecimal(3, stockNuevo);
            consulta.setLong(4, idProducto);
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar el stock del producto " + idProducto + ".");
            }
        }
    }

    public int obtenerIdTipoAjuste(Connection conexion, boolean entrada) throws SQLException {
        String jsonLower = "LOWER(to_jsonb(tm)::text)";
        String direccion = entrada
                ? "(" + jsonLower + " LIKE '%sobrante%' OR " + jsonLower + " LIKE '%entrada%' "
                        + "OR " + jsonLower + " LIKE '%positivo%' OR " + jsonLower + " LIKE '%aumento%')"
                : "(" + jsonLower + " LIKE '%faltante%' OR " + jsonLower + " LIKE '%salida%' "
                        + "OR " + jsonLower + " LIKE '%negativo%' OR " + jsonLower
                        + " LIKE '%disminuci%' OR " + jsonLower + " LIKE '%merma%')";
        String sql = "SELECT tm.\"Id_Tipo_Movimiento\" FROM public.\"TIPO_MOVIMIENTO\" tm "
                + "WHERE " + jsonLower + " LIKE '%ajust%' AND " + direccion
                + " ORDER BY tm.\"Id_Tipo_Movimiento\" LIMIT 1";
        try (PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            if (resultado.next()) {
                return resultado.getInt("Id_Tipo_Movimiento");
            }
        }

        sql = "SELECT tm.\"Id_Tipo_Movimiento\" FROM public.\"TIPO_MOVIMIENTO\" tm "
                + "WHERE " + jsonLower + " LIKE '%ajust%' "
                + "ORDER BY tm.\"Id_Tipo_Movimiento\" LIMIT 1";
        try (PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            return resultado.next() ? resultado.getInt("Id_Tipo_Movimiento") : (entrada ? 1 : 6);
        }
    }

    public void insertarMovimientoAjuste(Connection conexion, DetalleConteo detalle,
                                         BigDecimal stockAnterior, BigDecimal stockNuevo,
                                         long idConteo, long idUsuario, int idTipoMovimiento)
            throws SQLException {
        boolean entrada = detalle.getDiferencia().compareTo(BigDecimal.ZERO) > 0;
        String sql = "INSERT INTO public.\"MOVIMIENTOS\" "
                + "(\"Tipo_Movimiento\", \"Cantidad\", \"Stock_Anterior\", \"Stock_Nuevo\", "
                + "\"Motivo\", \"Fecha_Movimiento\", \"Observacion\", \"Id_Tipo_Movimiento\", "
                + "\"Id_Producto\", \"Id_Usuario\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, entrada ? "Entrada" : "Salida");
            consulta.setBigDecimal(2, detalle.getDiferencia().abs());
            consulta.setBigDecimal(3, stockAnterior);
            consulta.setBigDecimal(4, stockNuevo);
            consulta.setString(5, "Ajuste de inventario por conteo físico #" + idConteo);
            consulta.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            consulta.setString(7, detalle.getObservacion());
            consulta.setInt(8, idTipoMovimiento);
            consulta.setLong(9, detalle.getIdProducto());
            consulta.setLong(10, idUsuario);
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el movimiento de ajuste.");
            }
        }
    }

    private Connection obtenerConexion() throws SQLException {
        Connection conexion = ConexionDatabase.conectar();
        if (conexion == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        return conexion;
    }

    private String nombreCompleto(String nombre, String apellido) {
        return (nombre != null ? nombre : "")
                + (apellido != null && !apellido.isBlank() ? " " + apellido : "");
    }

    public static final class DatosAprobacion {
        private final Long idUsuarioRegistrador;
        private final String estado;

        private DatosAprobacion(Long idUsuarioRegistrador, String estado) {
            this.idUsuarioRegistrador = idUsuarioRegistrador;
            this.estado = estado;
        }

        public Long getIdUsuarioRegistrador() {
            return idUsuarioRegistrador;
        }

        public String getEstado() {
            return estado;
        }
    }
}
