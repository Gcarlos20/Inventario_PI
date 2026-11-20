package com.service;

import com.model.Producto;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Consultas y cálculos comunes para el estado y el resumen del inventario. */
public class InventarioService {
    private final ProductoService productoService;

    public InventarioService() {
        this(new ProductoService());
    }

    InventarioService(ProductoService productoService) {
        this.productoService = Objects.requireNonNull(productoService);
    }

    public List<Producto> listarProductos() throws SQLException {
        return productoService.listarTodos();
    }

    public String obtenerEstadoVisible(Producto producto) {
        int stock = producto.getStockInventario();
        if (stock <= 0) return "Agotado";
        if (stock <= producto.getStockMinimo()) return "Poco stock";
        return "Disponible";
    }

    public boolean esStockBajo(Producto producto) {
        int stock = producto.getStockInventario();
        return stock > 0 && stock <= producto.getStockMinimo();
    }

    /** Agrupa las cifras que muestra el resumen principal. */
    public Resumen crearResumen(List<Producto> productos) {
        int unidades = 0;
        int stockBajo = 0;
        int activos = 0;
        for (Producto producto : productos) {
            unidades += producto.getStockInventario();
            if (esStockBajo(producto)) stockBajo++;
            if (producto.getEstado() != null && producto.getEstado().equalsIgnoreCase("Activo")) activos++;
        }
        return new Resumen(productos.size(), unidades, stockBajo, activos);
    }

    /** Agrupa las cifras de existencias que muestra la pantalla de estado. */
    public ResumenEstado crearResumenEstado(List<Producto> productos) {
        int unidades = 0;
        int bajoStock = 0;
        for (Producto producto : productos) {
            String estado = obtenerEstadoVisible(producto);
            unidades += producto.getStockInventario();
            if ("Poco stock".equalsIgnoreCase(estado)) bajoStock++;
        }
        return new ResumenEstado(productos.size(), unidades, bajoStock);
    }

    public static final class Resumen {
        private final int productos;
        private final int unidades;
        private final int stockBajo;
        private final int activos;

        private Resumen(int productos, int unidades, int stockBajo, int activos) {
            this.productos = productos; this.unidades = unidades;
            this.stockBajo = stockBajo; this.activos = activos;
        }
        public int getProductos() { return productos; }
        public int getUnidades() { return unidades; }
        public int getStockBajo() { return stockBajo; }
        public int getActivos() { return activos; }
    }

    public static final class ResumenEstado {
        private final int productos;
        private final int unidades;
        private final int bajoStock;

        private ResumenEstado(int productos, int unidades, int bajoStock) {
            this.productos = productos; this.unidades = unidades; this.bajoStock = bajoStock;
        }
        public int getProductos() { return productos; }
        public int getUnidades() { return unidades; }
        public int getBajoStock() { return bajoStock; }
    }
}
