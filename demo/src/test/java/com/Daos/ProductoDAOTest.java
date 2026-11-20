package com.Daos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.model.Producto;
import org.junit.jupiter.api.Test;

class ProductoDAOTest {
    @Test
    void buscaDescripcionSinImportarAcentosOMayusculas() {
        Producto producto = producto("MAR-1", "Camarón fresco");

        assertTrue(ProductoDAO.coincideBusqueda("camaron", producto));
        assertTrue(ProductoDAO.coincideBusqueda("CAMARÓN", producto));
        assertFalse(ProductoDAO.coincideBusqueda("calamar", producto));
    }

    @Test
    void buscaCoincidenciasParcialesEnElCodigo() {
        Producto producto = producto("CAM-042", "Producto del mar");

        assertTrue(ProductoDAO.coincideBusqueda("cam", producto));
        assertTrue(ProductoDAO.coincideBusqueda("042", producto));
    }

    @Test
    void terminoVacioCoincideConCualquierProducto() {
        assertTrue(ProductoDAO.coincideBusqueda("  ", producto("A-1", "Arroz")));
    }

    private Producto producto(String codigo, String descripcion) {
        return new Producto(1, codigo, 1.0, 1, descripcion, "Activo", 0);
    }
}
