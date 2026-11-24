package com.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.model.ConteoFisico;
import com.model.Usuario;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
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
    void permiteDeseleccionarLaFilaSeleccionadaConOtroClic() {
        ConteoFisicoPanel panel = new ConteoFisicoPanel(usuarioActivo());
        panel.cargarConteos(List.of(conteo(12)));
        JTable tablaSuperior = tablaSuperior(panel);
        JTable tablaDetalles = tablaDetalles(panel);
        ((DefaultTableModel) tablaDetalles.getModel()).addRow(new Object[]{"123", "Arroz"});
        List<JButton> botonesAccion = buscarBotones(panel).stream()
                .filter(boton -> List.of("Devolver a reconteo", "Marcar conforme (Auditor)",
                        "Rechazar ajuste", "Aprobar y aplicar ajuste al stock (AdminDueño)")
                        .contains(boton.getText()))
                .toList();
        botonesAccion.forEach(boton -> boton.setEnabled(true));

        MouseEvent clic = enviarPresion(tablaSuperior, 5, tablaSuperior.getRowHeight() / 2);

        assertEquals(-1, tablaSuperior.getSelectedRow());
        assertEquals(0, tablaDetalles.getRowCount());
        assertEquals(4, botonesAccion.size());
        assertFalse(botonesAccion.stream().anyMatch(JButton::isEnabled));
        assertTrue(clic.isConsumed());
    }

    @Test
    void limpiaLaSeleccionAlHacerClicDebajoDeLasFilas() {
        ConteoFisicoPanel panel = new ConteoFisicoPanel(usuarioActivo());
        panel.cargarConteos(List.of(conteo(12)));
        JTable tablaSuperior = tablaSuperior(panel);
        JTable tablaDetalles = tablaDetalles(panel);
        ((DefaultTableModel) tablaDetalles.getModel()).addRow(new Object[]{"123", "Arroz"});
        tablaSuperior.setRowSelectionInterval(0, 0);

        MouseEvent clic = enviarPresion(tablaSuperior, 5, tablaSuperior.getRowHeight() * 2);

        assertEquals(-1, tablaSuperior.getSelectedRow());
        assertEquals(0, tablaDetalles.getRowCount());
        assertTrue(clic.isConsumed());
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

    private List<JButton> buscarBotones(Component componente) {
        List<JButton> botones = new ArrayList<>();
        if (componente instanceof JButton boton) {
            botones.add(boton);
        } else if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                botones.addAll(buscarBotones(hijo));
            }
        }
        return botones;
    }

    private List<String> nombresColumnas(JTable tabla) {
        List<String> nombres = new ArrayList<>();
        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            nombres.add(tabla.getColumnName(columna));
        }
        return nombres;
    }

    private JTable tablaSuperior(ConteoFisicoPanel panel) {
        JTabbedPane pestañas = (JTabbedPane) panel.getComponent(0);
        return buscarTablas(pestañas.getComponentAt(1)).get(0);
    }

    private JTable tablaDetalles(ConteoFisicoPanel panel) {
        JTabbedPane pestañas = (JTabbedPane) panel.getComponent(0);
        return buscarTablas(pestañas.getComponentAt(1)).get(1);
    }

    private MouseEvent enviarPresion(JTable tabla, int x, int y) {
        MouseEvent presionado = new MouseEvent(tabla, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), 0, x, y, 1, false, MouseEvent.BUTTON1);
        for (var listener : tabla.getMouseListeners()) listener.mousePressed(presionado);
        return presionado;
    }

    private ConteoFisico conteo(long id) {
        ConteoFisico conteo = new ConteoFisico();
        conteo.setIdConteo(id);
        return conteo;
    }

    private Usuario usuarioActivo() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        usuario.setNombre("Carlos");
        usuario.setApellido("Gomez");
        return usuario;
    }
}
