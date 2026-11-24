package com.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.model.ConteoFisico;
import com.model.Usuario;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class ConteoFisicoPanelTest {
    @Test
    void creaLasDosPestanasYConservaElConteoCiegoEnLaTablaDeCaptura() {
        ConteoFisicoPanel panel = new ConteoFisicoPanel(usuarioActivo());
        JTabbedPane pestañas = (JTabbedPane) panel.getComponent(0);

        assertEquals(2, pestañas.getTabCount());
        assertEquals("Nuevo Conteo Físico (Almacenista)", pestañas.getTitleAt(0));
        assertEquals("Revisión y Aprobación (Auditor / AdminDueño)", pestañas.getTitleAt(1));

        JTable tablaCaptura = buscarTablas(pestañas.getComponentAt(0)).get(0);
        assertEquals(List.of("Código", "Producto", "Cantidad Contada", "Observación"),
                nombresColumnas(tablaCaptura));
        assertTrue(nombresColumnas(tablaCaptura).stream()
                .noneMatch(nombre -> nombre.contains("Sistema") || nombre.contains("Diferencia")));

        List<JTable> tablasRevision = buscarTablas(pestañas.getComponentAt(1));
        assertEquals(2, tablasRevision.size());
        assertEquals(List.of("ID", "Fecha", "Responsable", "Productos", "Estado", "Observación"),
                nombresColumnas(tablasRevision.get(0)));
        assertEquals(List.of("Código", "Producto", "Stock Sistema", "Conteo Físico",
                        "Diferencia", "Observación"),
                nombresColumnas(tablasRevision.get(1)));
    }

    @Test
    void requiereUsuarioActivoConIdValido() {
        assertThrows(IllegalArgumentException.class, () -> new ConteoFisicoPanel(null));
        assertThrows(IllegalArgumentException.class, () -> new ConteoFisicoPanel(new Usuario()));

        Usuario inactivo = usuarioActivo();
        inactivo.setEstado("Inactivo");
        assertThrows(IllegalArgumentException.class, () -> new ConteoFisicoPanel(inactivo));
    }

    @Test
    void muestraElResumenDeProductosEnLaTablaSuperior() {
        ConteoFisicoPanel panel = new ConteoFisicoPanel(usuarioActivo());
        ConteoFisico conteo = new ConteoFisico();
        conteo.setIdConteo(12);
        conteo.setProductosResumen("Arroz, Frijoles");

        panel.cargarConteos(List.of(conteo));

        JTabbedPane pestañas = (JTabbedPane) panel.getComponent(0);
        JTable tablaSuperior = buscarTablas(pestañas.getComponentAt(1)).get(0);
        assertEquals("Arroz, Frijoles", tablaSuperior.getValueAt(0, 3));
    }

    @Test
    void camposEditablesUsanCaretVisibleSobreElFondoOscuro() {
        ConteoFisicoPanel panel = new ConteoFisicoPanel(usuarioActivo());
        List<JTextField> editables = buscarCamposTexto(panel).stream()
                .filter(JTextField::isEditable)
                .toList();

        assertEquals(5, editables.size());
        for (JTextField campo : editables) {
            assertEquals(Color.WHITE, campo.getCaretColor());
        }
    }

    private List<JTable> buscarTablas(Component componente) {
        List<JTable> tablas = new ArrayList<>();
        if (componente instanceof JTable tabla) {
            tablas.add(tabla);
        } else if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                tablas.addAll(buscarTablas(hijo));
            }
        }
        return tablas;
    }

    private List<JTextField> buscarCamposTexto(Component componente) {
        List<JTextField> campos = new ArrayList<>();
        if (componente instanceof JTextField campo) {
            campos.add(campo);
        } else if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                campos.addAll(buscarCamposTexto(hijo));
            }
        }
        return campos;
    }

    private List<String> nombresColumnas(JTable tabla) {
        List<String> nombres = new ArrayList<>();
        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            nombres.add(tabla.getColumnName(columna));
        }
        return nombres;
    }

    private Usuario usuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        usuario.setNombre("Carlos");
        usuario.setApellido("Gomez");
        return usuario;
    }
}
