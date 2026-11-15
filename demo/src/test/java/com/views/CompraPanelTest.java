package com.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.model.Producto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class CompraPanelTest {
    @Test
    void rechazaCantidadOCostoUnitarioNoPositivos() {
        assertThrows(IllegalArgumentException.class,
                () -> CompraPanel.validarCantidadYCosto(0, new BigDecimal("2.50")));
        assertThrows(IllegalArgumentException.class,
                () -> CompraPanel.validarCantidadYCosto(1, BigDecimal.ZERO));
    }

    @Test
    void consolidaImportesConPromedioPonderadoEnMismaFechaDeVencimiento() {
        LocalDate vencimiento = LocalDate.of(2027, 5, 1);
        Producto producto = new Producto(7, "P-7", 1.0, 3, "Producto", "Activo", 0);
        CompraPanel.LineaCompra linea = new CompraPanel.LineaCompra(
                producto, 2, new BigDecimal("10.00"), vencimiento);

        assertEquals(0, CompraPanel.buscarLinea(Arrays.asList(linea), 7, vencimiento));
        assertEquals(-1, CompraPanel.buscarLinea(
                Arrays.asList(linea), 7, vencimiento.plusDays(1)));

        linea.agregar(3, new BigDecimal("20.00"));

        assertEquals(5, linea.cantidad());
        assertEquals(0, new BigDecimal("16.00").compareTo(linea.costoUnitario()));
        assertEquals(0, new BigDecimal("80.00").compareTo(linea.subtotal()));
    }
}
