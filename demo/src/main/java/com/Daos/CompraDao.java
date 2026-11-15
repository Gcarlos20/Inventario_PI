package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Compra;
import com.model.DetalleCompra;
import com.model.Lote;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** Registra compras y todos sus efectos de inventario en una sola transacción. */
public class CompraDao {
    private final DetalleCompraDao detalleCompraDao = new DetalleCompraDao();
    private final LoteDao loteDao = new LoteDao();
    private final ProductoDAO productoDAO = new ProductoDAO();

    /**
     * Registra una compra completa. Cada lote debe corresponder al detalle del mismo índice;
     * se usa del lote su fecha de vencimiento y de ingreso, si fueron informadas.
     *
     * @return ID generado para la compra
     */
    public int registrarCompraCompleta(Compra compra, List<DetalleCompra> detalles,
                                       List<Lote> lotes, int idTipoMovimiento) throws SQLException {
        validarCompra(compra, detalles, lotes, idTipoMovimiento);

        try (Connection conexion = obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                int idCompra = insertarCompra(conexion, compra);
                for (int indice = 0; indice < detalles.size(); indice++) {
                    DetalleCompra detalle = detalles.get(indice);
                    Lote lote = lotes.get(indice);
                    validarCorrespondencia(detalle, lote);

                    int stockAnterior = productoDAO.obtenerStockParaActualizar(
                            conexion, detalle.getIdProducto());
                    int stockNuevo;
                    try {
                        stockNuevo = Math.addExact(stockAnterior, detalle.getCantidad());
                    } catch (ArithmeticException e) {
                        throw new SQLException("El stock resultante excede el límite permitido.", e);
                    }

                    int idDetalle = detalleCompraDao.insertar(
                            conexion, detalle, idCompra, stockAnterior, stockNuevo);
                    loteDao.insertar(conexion, lote, detalle, idDetalle);
                    productoDAO.actualizarStock(conexion, detalle.getIdProducto(), stockNuevo);
                    insertarMovimiento(conexion, compra, detalle, idTipoMovimiento,
                            stockAnterior, stockNuevo);
                }

                conexion.commit();
                return idCompra;
            } catch (SQLException | RuntimeException e) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    e.addSuppressed(errorRollback);
                }
                throw e;
            }
        }
    }

    private int insertarCompra(Connection conexion, Compra compra) throws SQLException {
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

    private void insertarMovimiento(Connection conexion, Compra compra, DetalleCompra detalle,
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

    private void validarCompra(Compra compra, List<DetalleCompra> detalles,
                               List<Lote> lotes, int idTipoMovimiento) {
        Objects.requireNonNull(compra, "La compra es obligatoria.");
        Objects.requireNonNull(detalles, "La lista de detalles es obligatoria.");
        Objects.requireNonNull(lotes, "La lista de lotes es obligatoria.");
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("La compra debe contener al menos un detalle.");
        }
        if (detalles.size() != lotes.size()) {
            throw new IllegalArgumentException("Debe proporcionarse un lote por cada detalle.");
        }
        if (compra.getIdUsuario() <= 0 || compra.getIdProveedores() <= 0) {
            throw new IllegalArgumentException("La compra requiere un usuario y un proveedor válidos.");
        }
        if (compra.getSubTotal() == null || compra.getTotalCompra() == null) {
            throw new IllegalArgumentException("El subtotal y el total de la compra son obligatorios.");
        }
        if (idTipoMovimiento <= 0) {
            throw new IllegalArgumentException("El ID del tipo de movimiento debe ser válido.");
        }

        for (DetalleCompra detalle : detalles) {
            Objects.requireNonNull(detalle, "La lista contiene un detalle nulo.");
            if (detalle.getIdProducto() <= 0 || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "Cada detalle requiere un producto y una cantidad mayores que cero.");
            }
            if (detalle.getPrecioDecimal() == null
                    || detalle.getPrecioDecimal().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio de cada detalle debe ser válido.");
            }
        }
        for (Lote lote : lotes) {
            Objects.requireNonNull(lote, "La lista contiene un lote nulo.");
        }
    }

    private void validarCorrespondencia(DetalleCompra detalle, Lote lote) {
        if (lote.getIdProducto() != 0 && lote.getIdProducto() != detalle.getIdProducto()) {
            throw new IllegalArgumentException(
                    "El lote y el detalle deben referirse al mismo producto.");
        }
    }

    private Connection obtenerConexion() throws SQLException {
        Connection conexion = ConexionDatabase.conectar();
        if (conexion == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        return conexion;
    }
}
