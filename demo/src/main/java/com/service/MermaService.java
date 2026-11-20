package com.service;

import com.Connection.ConexionDatabase;
import com.Daos.LoteDao;
import com.Daos.MermaDao;
import com.Daos.ProductoDAO;
import com.model.Lote;
import com.model.Merma;
import com.model.Usuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Coordina las bajas de lotes, stock y movimientos en una única transacción. */
public class MermaService {
    public static final int ID_TIPO_VENCIMIENTO = 5;
    public static final int ID_TIPO_MERMA_DANO = 6;

    private final LoteDao loteDao = new LoteDao();
    private final MermaDao mermaDao = new MermaDao();
    private final ProductoDAO productoDao = new ProductoDAO();

    /** Registra cada línea de baja y devuelve la cantidad de movimientos insertados. */
    public int registrarMermas(List<Merma> mermas, Usuario usuarioActivo) throws SQLException {
        validar(mermas, usuarioActivo);
        List<Merma> ordenadas = new ArrayList<>(mermas);
        ordenadas.sort(Comparator.comparingInt(Merma::getIdProducto)
                .thenComparingInt(Merma::getIdLote));

        try (Connection conexion = ConexionDatabase.conectar()) {
            conexion.setAutoCommit(false);
            try {
                for (Merma merma : ordenadas) registrarUna(conexion, merma, usuarioActivo.getIdUsuario());
                conexion.commit();
                return ordenadas.size();
            } catch (SQLException | RuntimeException error) {
                rollback(conexion, error);
                throw error;
            }
        }
    }

    private void registrarUna(Connection conexion, Merma merma, int idUsuario) throws SQLException {
        int stockAnterior = productoDao.obtenerStockParaActualizar(conexion, merma.getIdProducto());
        if (merma.getIdLote() > 0) {
            Lote lote = loteDao.obtenerParaActualizar(conexion, merma.getIdLote(), merma.getIdProducto());
            if (!"Activo".equalsIgnoreCase(lote.getEstado()) || lote.getCantidadActual() <= 0) {
                throw new SQLException("El lote seleccionado no está activo o no tiene existencias.");
            }
            if (merma.getCantidad() > lote.getCantidadActual()) {
                throw new SQLException("La cantidad de baja supera las existencias del lote.");
            }
        }
        if (merma.getCantidad() > stockAnterior) {
            throw new SQLException("La cantidad de baja supera el stock actual del producto.");
        }

        int stockNuevo = stockAnterior - merma.getCantidad();
        String motivo = motivoPara(merma.getIdTipoMovimiento());
        String estadoSinExistencias = merma.getIdTipoMovimiento() == ID_TIPO_VENCIMIENTO
                ? "Vencido" : "Agotado";
        productoDao.actualizarStock(conexion, merma.getIdProducto(), stockNuevo);
        if (merma.getIdLote() > 0) {
            loteDao.descontarCantidad(conexion, merma.getIdLote(), merma.getCantidad(), estadoSinExistencias);
        }
        mermaDao.insertarMovimiento(conexion, merma, motivo, stockAnterior, stockNuevo, idUsuario);
    }

    private void validar(List<Merma> mermas, Usuario usuarioActivo) {
        if (usuarioActivo == null || usuarioActivo.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("Se requiere un usuario activo con ID válido.");
        }
        Objects.requireNonNull(mermas, "La lista de mermas es obligatoria.");
        if (mermas.isEmpty()) throw new IllegalArgumentException("Agrega al menos una baja.");
        for (Merma merma : mermas) {
            Objects.requireNonNull(merma, "La lista contiene una baja nula.");
            if (merma.getIdProducto() <= 0 || merma.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada baja requiere un producto y una cantidad válidos.");
            }
            if (merma.getIdTipoMovimiento() != ID_TIPO_VENCIMIENTO
                    && merma.getIdTipoMovimiento() != ID_TIPO_MERMA_DANO) {
                throw new IllegalArgumentException("El tipo de movimiento debe ser 5 (vencimiento) o 6 (merma o daño).");
            }
        }
    }

    private String motivoPara(int idTipoMovimiento) {
        return idTipoMovimiento == ID_TIPO_VENCIMIENTO
                ? "Baja de producto por vencimiento o caducidad"
                : "Ajuste de inventario por merma o daño";
    }

    private void rollback(Connection conexion, Exception error) {
        try {
            conexion.rollback();
        } catch (SQLException fallo) {
            error.addSuppressed(fallo);
        }
    }
}
