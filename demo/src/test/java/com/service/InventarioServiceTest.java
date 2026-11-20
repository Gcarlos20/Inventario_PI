package com.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.model.Producto;
import java.util.List;
import org.junit.jupiter.api.Test;

class InventarioServiceTest {
    private final InventarioService servicio = new InventarioService();

    @Test
    void obtieneEstadoSegunStockYStockMinimo() {
        assertEquals("Agotado", servicio.obtenerEstadoVisible(producto(0, 3, "Poco stock")));
        assertEquals("Agotado", servicio.obtenerEstadoVisible(producto(-1, 3, "Disponible")));
        assertEquals("Poco stock", servicio.obtenerEstadoVisible(producto(3, 3, "Disponible")));
        assertEquals("Disponible", servicio.obtenerEstadoVisible(producto(4, 3, "Poco stock")));
    }

    @Test
    void soloCuentaComoPocoStockLasExistenciasPositivasDentroDelMinimo() {
        assertFalse(servicio.esStockBajo(producto(0, 3, "Poco stock")));
        assertTrue(servicio.esStockBajo(producto(1, 3, "Disponible")));
        assertFalse(servicio.esStockBajo(producto(4, 3, "Poco stock")));

        InventarioService.ResumenEstado resumen = servicio.crearResumenEstado(List.of(
                producto(0, 3, "Poco stock"), producto(1, 3, "Disponible"), producto(4, 3, "Poco stock")));

        assertEquals(1, resumen.getBajoStock());
    }

    private Producto producto(int stock, int stockMinimo, String estado) {
        return new Producto(1, "SKU-1", 1.0, stock, "Producto", estado, stockMinimo);
    }
}
