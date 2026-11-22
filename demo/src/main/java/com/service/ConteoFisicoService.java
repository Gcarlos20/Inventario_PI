package com.service;

import com.Connection.ConexionDatabase;
import com.Daos.ConteoFisicoDao;
import com.Daos.ConteoFisicoDao.DatosAprobacion;
import com.model.ConteoFisico;
import com.model.DetalleConteo;
import com.model.Usuario;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Coordina el registro, revisión y aplicación transaccional de conteos físicos. */
public class ConteoFisicoService {
    private final ConteoFisicoDao conteoDao = new ConteoFisicoDao();

    public long registrarConteoFisico(ConteoFisico conteo, Usuario almacenista)
            throws SQLException {
        validarRegistro(conteo, almacenista);
        Connection conexion = obtenerConexion();
        try (conexion) {
            conexion.setAutoCommit(false);
            try {
                boolean requiereReconteo = false;
                for (DetalleConteo detalle : conteo.getDetalles()) {
                    BigDecimal stockSistema = conteoDao.obtenerStockSistema(
                            conexion, detalle.getIdProducto());
                    BigDecimal diferencia = detalle.getStockFisico().subtract(stockSistema);
                    detalle.setStockSistema(stockSistema);
                    detalle.setDiferencia(diferencia);
                    requiereReconteo |= requiereReconteo(stockSistema, diferencia);
                }

                String estado = requiereReconteo
                        ? "Requiere reconteo" : "Pendiente de revisión";
                long idConteo = conteoDao.insertarConteo(
                        conexion, conteo, almacenista.getIdUsuario(), estado);
                conteo.setIdConteo(idConteo);
                conteo.setEstado(estado);
                for (DetalleConteo detalle : conteo.getDetalles()) {
                    detalle.setIdConteo(idConteo);
                    conteoDao.insertarDetalle(conexion, idConteo, detalle);
                }
                conexion.commit();
                return idConteo;
            } catch (SQLException | RuntimeException error) {
                rollback(conexion, error);
                throw error;
            }
        }
    }

    public List<ConteoFisico> listarConteos() throws SQLException {
        return conteoDao.listarConteos();
    }

    public List<DetalleConteo> listarDetallesConteo(long idConteo) throws SQLException {
        if (idConteo <= 0) {
            throw new IllegalArgumentException("El ID del conteo debe ser válido.");
        }
        return conteoDao.listarDetallesConteo(idConteo);
    }

    public void revisarConteoAuditor(long idConteo, boolean conforme,
                                     String observacionAuditor, Usuario auditor)
            throws SQLException {
        validarIdConteo(idConteo);
        validarUsuarioActivo(auditor, "auditor");
        if (!conforme && (observacionAuditor == null || observacionAuditor.isBlank())) {
            throw new IllegalArgumentException("Indica la observación para devolver el conteo a reconteo.");
        }

        Connection conexion = obtenerConexion();
        try (conexion) {
            conexion.setAutoCommit(false);
            try {
                conteoDao.actualizarRevision(
                        conexion, idConteo, conforme, observacionAuditor);
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                rollback(conexion, error);
                throw error;
            }
        }
    }

    public void aprobarYAplicarAjuste(long idConteo, boolean aprobar, Usuario adminDueno)
            throws SQLException {
        validarIdConteo(idConteo);
        validarUsuarioActivo(adminDueno, "administrador");
        Connection conexion = obtenerConexion();
        try (conexion) {
            conexion.setAutoCommit(false);
            try {
                DatosAprobacion datos = conteoDao.obtenerDatosAprobacion(conexion, idConteo);
                Long idRegistrador = datos.getIdUsuarioRegistrador();
                if (idRegistrador != null
                        && idRegistrador.longValue() == adminDueno.getIdUsuario()) {
                    throw new IllegalArgumentException(
                            "El usuario que aprueba no puede ser quien registró el conteo.");
                }

                if ("Aprobado y aplicado".equalsIgnoreCase(datos.getEstado())) {
                    throw new SQLException("El conteo ya fue aprobado y aplicado.");
                }
                if (!aprobar) {
                    conteoDao.actualizarEstado(conexion, idConteo, "Rechazado");
                } else {
                    List<DetalleConteo> detalles =
                            conteoDao.listarDetallesConteo(conexion, idConteo);
                    if (detalles.isEmpty()) {
                        throw new SQLException("El conteo no contiene detalles para aplicar.");
                    }
                    for (DetalleConteo detalle : detalles) {
                        if (detalle.getDiferencia() == null || detalle.getStockFisico() == null) {
                            throw new SQLException("El detalle del conteo contiene cantidades incompletas.");
                        }
                        if (detalle.getDiferencia().compareTo(BigDecimal.ZERO) == 0) continue;

                        BigDecimal stockAnterior = conteoDao.obtenerStockSistema(
                                conexion, detalle.getIdProducto());
                        BigDecimal stockNuevo = detalle.getStockFisico();
                        int idTipoMovimiento = conteoDao.obtenerIdTipoAjuste(
                                conexion, detalle.getDiferencia().compareTo(BigDecimal.ZERO) > 0);
                        conteoDao.actualizarStockProducto(
                                conexion, detalle.getIdProducto(), stockNuevo);
                        conteoDao.insertarMovimientoAjuste(
                                conexion, detalle, stockAnterior, stockNuevo, idConteo,
                                adminDueno.getIdUsuario(), idTipoMovimiento);
                    }
                    conteoDao.actualizarEstado(conexion, idConteo, "Aprobado y aplicado");
                }
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                rollback(conexion, error);
                throw error;
            }
        }
    }

    static boolean requiereReconteo(BigDecimal stockSistema, BigDecimal diferencia) {
        return stockSistema.compareTo(BigDecimal.ZERO) > 0
                && diferencia.abs().multiply(BigDecimal.valueOf(100))
                        .compareTo(stockSistema.multiply(BigDecimal.TEN)) > 0;
    }

    private void validarRegistro(ConteoFisico conteo, Usuario almacenista) {
        Objects.requireNonNull(conteo, "El conteo físico es obligatorio.");
        validarUsuarioActivo(almacenista, "almacenista");
        if (conteo.getDetalles() == null || conteo.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El conteo debe contener al menos un producto.");
        }
        for (DetalleConteo detalle : conteo.getDetalles()) {
            Objects.requireNonNull(detalle, "La lista contiene un detalle nulo.");
            if (detalle.getIdProducto() <= 0 || detalle.getStockFisico() == null
                    || detalle.getStockFisico().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Cada detalle requiere un producto válido y un stock físico no negativo.");
            }
        }
    }

    private void validarUsuarioActivo(Usuario usuario, String rol) {
        if (usuario == null || usuario.getIdUsuario() <= 0
                || !"Activo".equalsIgnoreCase(usuario.getEstado())) {
            throw new IllegalArgumentException(
                    "Se requiere un " + rol + " activo con ID válido.");
        }
    }

    private void validarIdConteo(long idConteo) {
        if (idConteo <= 0) {
            throw new IllegalArgumentException("El ID del conteo debe ser válido.");
        }
    }

    private Connection obtenerConexion() throws SQLException {
        Connection conexion = ConexionDatabase.conectar();
        if (conexion == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        return conexion;
    }

    private void rollback(Connection conexion, Exception error) {
        try {
            conexion.rollback();
        } catch (SQLException errorRollback) {
            error.addSuppressed(errorRollback);
        }
    }
}
