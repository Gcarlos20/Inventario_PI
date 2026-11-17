package com.service;

import com.Connection.ConexionDatabase;
import com.Daos.CompraDao;
import com.Daos.DetalleCompraDao;
import com.Daos.LoteDao;
import com.model.Compra;
import com.model.DetalleCompra;
import com.model.Lote;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Coordina las reglas y la transacción completa de una entrada de compra. */
public class CompraService {
    private final CompraDao compraDao = new CompraDao();
    private final DetalleCompraDao detalleDao = new DetalleCompraDao();
    private final LoteDao loteDao = new LoteDao();
    private final ProductoService productoService = new ProductoService();

    /** Guarda compra, detalles, lotes y movimientos en una única transacción. */
    public int registrarCompraCompleta(Compra compra, List<DetalleCompra> detalles,
                                       List<Lote> lotes, int idTipoMovimiento) throws SQLException {
        validarCompra(compra, detalles, lotes, idTipoMovimiento);
        Connection conexion = ConexionDatabase.conectar();
        if (conexion == null) throw new SQLException("No se pudo establecer conexión con la base de datos.");

        try (conexion) {
            conexion.setAutoCommit(false);
            try {
                int idCompra = compraDao.insertar(conexion, compra);
                for (int indice = 0; indice < detalles.size(); indice++) {
                    DetalleCompra detalle = detalles.get(indice);
                    Lote lote = lotes.get(indice);
                    validarCorrespondencia(detalle, lote);

                    int stockAnterior = productoService.obtenerStockParaActualizar(
                            conexion, detalle.getIdProducto());
                    int stockNuevo;
                    try {
                        stockNuevo = Math.addExact(stockAnterior, detalle.getCantidad());
                    } catch (ArithmeticException error) {
                        throw new SQLException("El stock resultante excede el límite permitido.", error);
                    }

                    int idDetalle = detalleDao.insertar(
                            conexion, detalle, idCompra, stockAnterior, stockNuevo);
                    loteDao.insertar(conexion, lote, detalle, idDetalle);
                    productoService.actualizarStock(conexion, detalle.getIdProducto(), stockNuevo);
                    compraDao.insertarMovimiento(conexion, compra, detalle, idTipoMovimiento,
                            stockAnterior, stockNuevo);
                }
                conexion.commit();
                return idCompra;
            } catch (SQLException | RuntimeException error) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    error.addSuppressed(errorRollback);
                }
                throw error;
            }
        }
    }

    /** Valida cantidad y costo usados al agregar una línea a la compra. */
    public static void validarCantidadYCosto(int unidades, BigDecimal precio) {
        if (unidades <= 0 || precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad y el costo unitario deben ser mayores que cero.");
        }
    }

    /** Suma los subtotales de una compra. */
    public BigDecimal calcularTotal(List<BigDecimal> subtotales) {
        Objects.requireNonNull(subtotales, "La lista de subtotales es obligatoria.");
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal subtotal : subtotales) total = total.add(subtotal);
        return total;
    }

    public static BigDecimal calcularSubtotal(BigDecimal precioUnitario, int cantidad) {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    /** Aplica el costo promedio cuando se agrega otra cantidad de la misma línea. */
    public static Consolidado consolidarLinea(int cantidadActual, BigDecimal importeActual,
                                                int cantidadNueva, BigDecimal costoNuevo) {
        int cantidadTotal;
        try {
            cantidadTotal = Math.addExact(cantidadActual, cantidadNueva);
        } catch (ArithmeticException error) {
            throw new IllegalArgumentException("La cantidad consolidada excede el límite permitido.", error);
        }
        BigDecimal importeTotal = importeActual.add(calcularSubtotal(costoNuevo, cantidadNueva));
        BigDecimal costoPromedio = importeTotal.divide(BigDecimal.valueOf(cantidadTotal), MathContext.DECIMAL128);
        return new Consolidado(cantidadTotal, importeTotal, costoPromedio);
    }

    public static final class Consolidado {
        private final int cantidad;
        private final BigDecimal importe;
        private final BigDecimal costoPromedio;

        private Consolidado(int cantidad, BigDecimal importe, BigDecimal costoPromedio) {
            this.cantidad = cantidad;
            this.importe = importe;
            this.costoPromedio = costoPromedio;
        }
        public int getCantidad() { return cantidad; }
        public BigDecimal getImporte() { return importe; }
        public BigDecimal getCostoPromedio() { return costoPromedio; }
    }

    private void validarCompra(Compra compra, List<DetalleCompra> detalles,
                               List<Lote> lotes, int idTipoMovimiento) {
        Objects.requireNonNull(compra, "La compra es obligatoria.");
        Objects.requireNonNull(detalles, "La lista de detalles es obligatoria.");
        Objects.requireNonNull(lotes, "La lista de lotes es obligatoria.");
        if (detalles.isEmpty()) throw new IllegalArgumentException("La compra debe contener al menos un detalle.");
        if (detalles.size() != lotes.size()) throw new IllegalArgumentException("Debe proporcionarse un lote por cada detalle.");
        if (compra.getIdUsuario() <= 0 || compra.getIdProveedores() <= 0) {
            throw new IllegalArgumentException("La compra requiere un usuario y un proveedor válidos.");
        }
        if (compra.getSubTotal() == null || compra.getTotalCompra() == null) {
            throw new IllegalArgumentException("El subtotal y el total de la compra son obligatorios.");
        }
        if (idTipoMovimiento <= 0) throw new IllegalArgumentException("El ID del tipo de movimiento debe ser válido.");

        for (DetalleCompra detalle : detalles) {
            Objects.requireNonNull(detalle, "La lista contiene un detalle nulo.");
            if (detalle.getIdProducto() <= 0 || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("Cada detalle requiere un producto y una cantidad mayores que cero.");
            }
            if (detalle.getPrecioDecimal() == null
                    || detalle.getPrecioDecimal().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio de cada detalle debe ser válido.");
            }
        }
        for (Lote lote : lotes) Objects.requireNonNull(lote, "La lista contiene un lote nulo.");
    }

    private void validarCorrespondencia(DetalleCompra detalle, Lote lote) {
        if (lote.getIdProducto() != 0 && lote.getIdProducto() != detalle.getIdProducto()) {
            throw new IllegalArgumentException("El lote y el detalle deben referirse al mismo producto.");
        }
    }
}
