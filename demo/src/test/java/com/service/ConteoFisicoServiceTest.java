package com.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.model.ConteoFisico;
import com.model.DetalleConteo;
import com.model.Usuario;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConteoFisicoServiceTest {
    private final ConteoFisicoService servicio = new ConteoFisicoService();

    @Test
    void requiereReconteoSoloCuandoLaVariacionSuperaElDiezPorCiento() {
        assertTrue(ConteoFisicoService.requiereReconteo(
                new BigDecimal("100"), new BigDecimal("-10.01")));
        assertFalse(ConteoFisicoService.requiereReconteo(
                new BigDecimal("100"), new BigDecimal("10")));
        assertFalse(ConteoFisicoService.requiereReconteo(
                BigDecimal.ZERO, new BigDecimal("25")));
    }

    @Test
    void rechazaConteoSinUsuarioActivoODetallesValidosAntesDeConectar() {
        ConteoFisico conteo = conteoValido();
        Usuario usuario = usuarioActivo();
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConteoFisico(conteo, null));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConteoFisico(conteo, new Usuario()));
        Usuario usuarioInactivo = usuarioActivo();
        usuarioInactivo.setEstado("Inactivo");
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConteoFisico(conteo, usuarioInactivo));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConteoFisico(conteoVacio(), usuario));

        DetalleConteo detalle = detalleValido();
        detalle.setStockFisico(new BigDecimal("-0.01"));
        ConteoFisico conteoNegativo = new ConteoFisico();
        conteoNegativo.setDetalles(List.of(detalle));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarConteoFisico(conteoNegativo, usuario));
    }

    private ConteoFisico conteoValido() {
        ConteoFisico conteo = new ConteoFisico();
        conteo.setDetalles(List.of(detalleValido()));
        return conteo;
    }

    private ConteoFisico conteoVacio() {
        ConteoFisico conteo = new ConteoFisico();
        conteo.setDetalles(Collections.emptyList());
        return conteo;
    }

    private DetalleConteo detalleValido() {
        DetalleConteo detalle = new DetalleConteo();
        detalle.setIdProducto(1);
        detalle.setStockFisico(BigDecimal.ONE);
        return detalle;
    }

    private Usuario usuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        return usuario;
    }
}
