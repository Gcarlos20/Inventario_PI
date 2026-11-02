package com.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public abstract class AbstractCrudFrame<T> extends JFrame {

    private final DefaultTableModel tableModel;
    private final JTable tabla;
    private final JButton actualizarButton = new JButton("Actualizar");
    private final JButton eliminarButton = new JButton("Eliminar");
    private final JButton guardarButton = new JButton("Guardar");
    private final JButton limpiarButton = new JButton("Nuevo / Limpiar");
    private final JButton recargarButton = new JButton("Recargar");
    private final JLabel estadoLabel = new JLabel(" ");
    private final Runnable cerrarSesion;
    private List<T> registros = new ArrayList<>();
    private Integer idSeleccionado;

    protected AbstractCrudFrame(String titulo, String[] columnas, Runnable cerrarSesion) {
        super(titulo);
        this.cerrarSesion = cerrarSesion;
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(tableModel);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    protected final void iniciarInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(680, 460));
        setSize(1050, 650);
        setLocationRelativeTo(null);

        JPanel formularioPanel = crearFormulario();
        formularioPanel.setPreferredSize(new Dimension(300, 400));

        JPanel tablaPanel = new JPanel(new BorderLayout(8, 8));
        tablaPanel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JSplitPane contenido = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, formularioPanel, tablaPanel);
        contenido.setResizeWeight(0.32);
        add(contenido, BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(0, 3, 8, 6));
        botones.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));
        botones.add(guardarButton);
        botones.add(actualizarButton);
        botones.add(eliminarButton);
        botones.add(limpiarButton);
        botones.add(recargarButton);
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.add(estadoLabel, BorderLayout.NORTH);
        inferior.add(botones, BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);

        JPanel encabezado = new JPanel(new BorderLayout(10, 0));
        JLabel titulo = new JLabel("  " + getTitle());
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 10, 0));
        titulo.setFont(titulo.getFont().deriveFont(18f));
        encabezado.add(titulo, BorderLayout.CENTER);
        if (cerrarSesion != null) {
            JButton salir = new JButton("Cerrar sesión");
            salir.addActionListener(evento -> {
                dispose();
                cerrarSesion.run();
            });
            encabezado.add(salir, BorderLayout.EAST);
        }
        add(encabezado, BorderLayout.NORTH);

        actualizarButton.setEnabled(false);
        eliminarButton.setEnabled(false);
        conectarEventos();
        try {
            recargarTabla();
        } catch (Exception e) {
            mostrarError("No se pudieron cargar los productos.", e);
        }
    }

    protected abstract JPanel crearFormulario();

    protected abstract List<T> listarRegistros() throws Exception;

    protected abstract Object[] convertirFila(T registro);

    protected abstract void cargarFormulario(T registro);

    protected abstract T leerFormulario(int id) throws Exception;

    protected abstract void insertar(T registro) throws Exception;

    protected abstract boolean persistirActualizacion(T registro) throws Exception;

    protected abstract boolean eliminarRegistro(int id) throws Exception;

    protected abstract int obtenerId(T registro);

    protected abstract void limpiarCampos();

    private void conectarEventos() {
        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (evento.getValueIsAdjusting()) {
                return;
            }
            int fila = tabla.getSelectedRow();
            if (fila >= 0 && fila < registros.size()) {
                T registro = registros.get(fila);
                idSeleccionado = obtenerId(registro);
                cargarFormulario(registro);
                actualizarButton.setEnabled(true);
                eliminarButton.setEnabled(true);
                estadoLabel.setText("Producto seleccionado: #" + idSeleccionado);
            }
        });

        guardarButton.addActionListener(evento -> guardar());
        actualizarButton.addActionListener(evento -> actualizar());
        eliminarButton.addActionListener(evento -> eliminar());
        limpiarButton.addActionListener(evento -> limpiar());
        recargarButton.addActionListener(evento -> {
            try {
                recargarTabla();
            } catch (Exception e) {
                mostrarError("No se pudieron cargar los productos.", e);
            }
        });
    }

    private void guardar() {
        try {
            insertar(leerFormulario(0));
            limpiar();
            recargarTabla();
            mostrarMensaje("Producto guardado correctamente.");
        } catch (Exception e) {
            mostrarError("No se pudo guardar el producto.", e);
        }
    }

    private void actualizar() {
        if (idSeleccionado == null) {
            mostrarMensaje("Selecciona un producto de la tabla para actualizarlo.");
            return;
        }
        try {
            if (!persistirActualizacion(leerFormulario(idSeleccionado))) {
                mostrarMensaje("No se encontró el producto para actualizar.");
                return;
            }
            limpiar();
            recargarTabla();
            mostrarMensaje("Producto actualizado correctamente.");
        } catch (Exception e) {
            mostrarError("No se pudo actualizar el producto.", e);
        }
    }

    private void eliminar() {
        if (idSeleccionado == null) {
            mostrarMensaje("Selecciona un producto de la tabla para eliminarlo.");
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Seguro que deseas eliminar el producto #" + idSeleccionado + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (!eliminarRegistro(idSeleccionado)) {
                mostrarMensaje("No se encontró el producto para eliminar.");
                return;
            }
            limpiar();
            recargarTabla();
            mostrarMensaje("Producto eliminado correctamente.");
        } catch (Exception e) {
            mostrarError("No se pudo eliminar el producto.", e);
        }
    }

    private void limpiar() {
        tabla.clearSelection();
        idSeleccionado = null;
        actualizarButton.setEnabled(false);
        eliminarButton.setEnabled(false);
        limpiarCampos();
        estadoLabel.setText(" ");
    }

    private void recargarTabla() throws Exception {
        tabla.clearSelection();
        idSeleccionado = null;
        actualizarButton.setEnabled(false);
        eliminarButton.setEnabled(false);
        registros = listarRegistros();
        tableModel.setRowCount(0);
        for (T registro : registros) {
            tableModel.addRow(convertirFila(registro));
        }
        estadoLabel.setText("Productos encontrados: " + registros.size());
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Inventario", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje, Exception error) {
        if (!(error instanceof IllegalArgumentException)) {
            error.printStackTrace();
        }
        JOptionPane.showMessageDialog(
                this,
                mensaje + "\n" + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
