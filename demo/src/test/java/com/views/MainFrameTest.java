package com.views;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.model.Producto;
import org.junit.jupiter.api.Test;

class MainFrameTest {
    @Test
    void cuentaStockBajoSoloCuandoElStockEsPositivoYNoSuperaElMinimo() {
        Producto bajoMinimo = new Producto(1, "1", 1.0, 2, "Arroz", "Activo", 3);
        Producto marcadoPocoStock = new Producto(2, "2", 1.0, 8, "Aceite", "Poco stock", 3);
        Producto disponible = new Producto(3, "3", 1.0, 8, "Sal", "Disponible", 3);
        Producto agotado = new Producto(4, "4", 1.0, 0, "Harina", "Poco stock", 3);

        assertTrue(MainFrame.esStockBajo(bajoMinimo));
        assertFalse(MainFrame.esStockBajo(marcadoPocoStock));
        assertFalse(MainFrame.esStockBajo(disponible));
        assertFalse(MainFrame.esStockBajo(agotado));
    }
}
