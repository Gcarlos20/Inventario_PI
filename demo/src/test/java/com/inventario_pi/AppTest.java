package com.inventario_pi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.model.Producto;
import org.junit.jupiter.api.Test;

public class AppTest {
    @Test
    public void shouldCreateProductoWithProvidedValues() {
        Producto producto = new Producto(
                1, "PROD-001", 12.5, 8, "Producto de prueba", "Activo", 2);

        assertEquals(1, producto.getIdProducto());
        assertEquals("PROD-001", producto.getCodigo());
        assertEquals(12.5, producto.getPrecio(), 0.001);
        assertEquals(8, producto.getStockInventario());
        assertEquals("Producto de prueba", producto.getDescripcion());
        assertEquals("Activo", producto.getEstado());
        assertEquals(2, producto.getStockMinimo());
    }
}
