package com.service;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Reglas y operaciones del catálogo de productos. */
public class ProductoService {
    private final ProductoDAO productoDao;

    public ProductoService() {
        this(new ProductoDAO());
    }

    ProductoService(ProductoDAO productoDao) {
        this.productoDao = Objects.requireNonNull(productoDao);
    }

    public List<Producto> listarTodos() throws SQLException { return productoDao.listarTodos(); }
    public Producto obtenerPorId(int id) throws SQLException { return productoDao.obtenerPorId(id); }
    public List<Producto> buscarPorCodigoONombre(String texto) throws SQLException {
        return productoDao.buscarPorCodigoONombre(texto);
    }

    public int crear(Producto producto) throws SQLException {
        validar(producto);
        return productoDao.agregarYObtenerId(producto);
    }

    public boolean actualizar(Producto producto) throws SQLException {
        validar(producto);
        if (producto.getIdProducto() <= 0) throw new IllegalArgumentException("El ID del producto no es válido.");
        return productoDao.actualizar(producto);
    }

    public boolean eliminar(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("El ID del producto no es válido.");
        return productoDao.eliminar(id);
    }

    /** Convierte los campos del formulario y conserva sus validaciones actuales. */
    public Producto desdeFormulario(int id, String codigo, String precio, String stock,
                                    String descripcion, String estado, String stockMinimo) {
        try {
            String codigoLimpio = codigo.trim();
            String nombreLimpio = descripcion.trim();
            double precioNumero = Double.parseDouble(precio.trim().replace(',', '.'));
            int stockNumero = Integer.parseInt(stock.trim());
            int minimoNumero = Integer.parseInt(stockMinimo.trim());
            Producto producto = new Producto(id, codigoLimpio, precioNumero, stockNumero,
                    nombreLimpio, estado, minimoNumero);
            validar(producto);
            return producto;
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(
                    "Precio, stock y stock mínimo deben tener un formato numérico válido.", error);
        }
    }

    public void validar(Producto producto) {
        Objects.requireNonNull(producto, "El producto es obligatorio.");
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()
                || producto.getDescripcion() == null || producto.getDescripcion().isBlank()
                || producto.getEstado() == null || producto.getEstado().isBlank()) {
            throw new IllegalArgumentException("Completa el código, el nombre y el estado del producto.");
        }
        if (!Double.isFinite(producto.getPrecio()) || producto.getPrecio() < 0
                || producto.getStockInventario() < 0 || producto.getStockMinimo() < 0) {
            throw new IllegalArgumentException("Precio y existencias no pueden ser negativos.");
        }
    }

    // La compra conserva el uso de la conexión transaccional compartida.
    public int obtenerStockParaActualizar(java.sql.Connection conexion, int idProducto) throws SQLException {
        return productoDao.obtenerStockParaActualizar(conexion, idProducto);
    }

    public void actualizarStock(java.sql.Connection conexion, int idProducto, int stockNuevo) throws SQLException {
        productoDao.actualizarStock(conexion, idProducto, stockNuevo);
    }
}
