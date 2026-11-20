package com.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.model.Merma;
import com.model.Usuario;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class MermaServiceTest {
    private final MermaService servicio = new MermaService();

    @Test
    void requiereUsuarioActivoConIdValido() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarMermas(List.of(mermaValida()), null));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarMermas(List.of(mermaValida()), new Usuario()));
    }

    @Test
    void rechazaListaVaciaOCantidadNoPositivaAntesDeConectar() {
        Usuario usuario = usuarioValido();
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarMermas(Collections.emptyList(), usuario));

        Merma merma = mermaValida();
        merma.setCantidad(0);
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarMermas(List.of(merma), usuario));
    }

    @Test
    void soloAceptaLosTiposDeMovimientoConfirmados() {
        Merma merma = mermaValida();
        merma.setIdTipoMovimiento(2);
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarMermas(List.of(merma), usuarioValido()));
    }

    private Merma mermaValida() {
        return new Merma(1, 1, 1, MermaService.ID_TIPO_VENCIMIENTO, "");
    }

    private Usuario usuarioValido() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        return usuario;
    }
}
