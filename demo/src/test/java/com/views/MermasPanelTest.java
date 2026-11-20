package com.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.model.Usuario;
import org.junit.jupiter.api.Test;

class MermasPanelTest {
    @Test
    void creaLosTresBloquesSinConectarConLaBaseDeDatos() {
        Usuario usuario = usuarioActivo();

        MermasPanel panel = new MermasPanel(usuario);

        assertEquals(3, panel.getComponentCount());
    }

    @Test
    void requiereUnUsuarioActivoConIdValido() {
        assertThrows(IllegalArgumentException.class, () -> new MermasPanel(null));
        assertThrows(IllegalArgumentException.class, () -> new MermasPanel(new Usuario()));
    }

    private Usuario usuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        usuario.setNombre("Carlos");
        usuario.setApellido("Gomez");
        return usuario;
    }
}
