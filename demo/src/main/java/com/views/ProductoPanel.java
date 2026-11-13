package com.views;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.table.JTableHeader;

/** Catálogo de productos que se muestra dentro del área principal de la aplicación. */
public class ProductoPanel extends JPanel {
    // La paleta propia mantiene el contraste aunque el sistema operativo use modo oscuro.
    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color SUPERFICIE = new Color(30, 41, 59);
    private static final Color CAMPO = new Color(15, 23, 42);
    private static final Color BORDE = new Color(51, 65, 85);
    private static final Color TEXTO = new Color(226, 232, 240);
    private static final Color TEXTO_SUAVE = new Color(148, 163, 184);
    private static final Color AZUL = new Color(59, 130, 246);
    private static final Color AZUL_SELECCION = new Color(30, 64, 112);
    private static final Color ROJO = new Color(153, 53, 65);

    private final ProductoDAO dao = new ProductoDAO();
    private final Runnable regresar;

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Código", "Nombre del producto", "Precio", "Stock actual", "Estado", "Stock mínimo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);
    private final JTextField codigo = new JTextField();
    private final JTextField precio = new JTextField();
    private final JTextField stock = new JTextField();
    private final JTextField nombreProducto = new JTextField();
    private final JComboBox<String> estado =
            new JComboBox<>(new String[]{"Activo", "Disponible", "Poco stock"});
    private final JTextField minimo = new JTextField();
    private final JLabel estadoVista = new JLabel("Preparando catálogo…");

    private final JButton guardar = new JButton("Agregar producto");
    private final JButton actualizar = new JButton("Guardar cambios");
    private final JButton eliminar = new JButton("Eliminar");
    private final JButton limpiar = new JButton("Nuevo / Limpiar");
    private final JButton refrescar = new JButton("Actualizar lista");

    private Integer idSeleccionado;
    private boolean tareaEnCurso;

    public ProductoPanel(Runnable regresar) {
        this.regresar = regresar;
        construirVista();
        recargar();
    }

    /** Construye las tres zonas del catálogo: navegación, edición y tabla. */
    private void construirVista() {
        setLayout(new BorderLayout(12, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);
        JButton volver = new JButton("←  Volver al resumen");
        estilizarBoton(volver, SUPERFICIE, TEXTO);
        volver.addActionListener(e -> regresar.run());
        barra.add(volver, BorderLayout.WEST);

        JLabel ayuda = new JLabel("Busca un producto en la tabla o completa el formulario para agregarlo");
        ayuda.setForeground(TEXTO_SUAVE);
        barra.add(ayuda, BorderLayout.CENTER);
        add(barra, BorderLayout.NORTH);

        JPanel editor = crearEditor();
        JScrollPane editorConScroll = new JScrollPane(editor);
        editorConScroll.setBorder(null);
        editorConScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        editorConScroll.getViewport().setBackground(FONDO);
        editorConScroll.getVerticalScrollBar().setUnitIncrement(16);
        editorConScroll.setPreferredSize(new Dimension(290, 0));

        JPanel tablaPanel = crearPanelTabla();
        JSplitPane contenido = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorConScroll, tablaPanel);
        contenido.setResizeWeight(0.30);
        contenido.setDividerSize(10);
        contenido.setBorder(null);
        contenido.setOpaque(false);
        contenido.setBackground(FONDO);
        add(contenido, BorderLayout.CENTER);

        actualizarEstadoBotones();
    }

    /** Arma el formulario con etiquetas arriba de cada campo para que se lea con claridad. */
    private JPanel crearEditor() {
        JPanel editor = new JPanel(new BorderLayout(0, 14));
        editor.setBackground(SUPERFICIE);
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(16, 15, 15, 15)));

        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Datos del producto");
        titulo.setForeground(TEXTO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 17f));
        JLabel detalle = new JLabel("Completa la información del artículo");
        detalle.setForeground(TEXTO_SUAVE);
        encabezado.add(titulo, BorderLayout.NORTH);
        encabezado.add(detalle, BorderLayout.SOUTH);
        editor.add(encabezado, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        agregarCampo(formulario, 0, "Código del producto", codigo);
        agregarCampo(formulario, 1, "Nombre del producto", nombreProducto);
        agregarCampo(formulario, 2, "Precio", precio);
        agregarCampo(formulario, 3, "Stock actual", stock);
        agregarCampo(formulario, 4, "Estado", estado);
        agregarCampo(formulario, 5, "Stock mínimo", minimo);
        editor.add(formulario, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new GridBagLayout());
        acciones.setOpaque(false);
        acciones.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        estilizarBoton(guardar, AZUL, Color.WHITE);
        estilizarBoton(actualizar, new Color(55, 75, 105), TEXTO);
        estilizarBoton(eliminar, ROJO, Color.WHITE);
        estilizarBoton(limpiar, new Color(55, 75, 105), TEXTO);
        estilizarBoton(refrescar, new Color(55, 75, 105), TEXTO);
        agregarAccion(acciones, guardar, 0, 0);
        agregarAccion(acciones, actualizar, 1, 0);
        agregarAccion(acciones, eliminar, 0, 1);
        agregarAccion(acciones, limpiar, 1, 1);
        agregarAccion(acciones, refrescar, 0, 2);
        GridBagConstraints espacio = new GridBagConstraints();
        espacio.gridx = 1;
        espacio.gridy = 2;
        espacio.weightx = 1;
        espacio.fill = GridBagConstraints.HORIZONTAL;
        JPanel espacioVacio = new JPanel();
        espacioVacio.setOpaque(false);
        acciones.add(espacioVacio, espacio);
        editor.add(acciones, BorderLayout.SOUTH);

        guardar.addActionListener(e -> guardar());
        actualizar.addActionListener(e -> actualizar());
        eliminar.addActionListener(e -> eliminar());
        limpiar.addActionListener(e -> limpiar());
        refrescar.addActionListener(e -> recargar());
        configurarCampos();
        return editor;
    }

    /** Presenta la lista y mantiene visibles sus encabezados en ventanas angostas. */
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SUPERFICIE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(13, 13, 11, 13)));

        JLabel titulo = new JLabel("Productos registrados");
        titulo.setForeground(TEXTO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(titulo, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(30);
        tabla.setFont(tabla.getFont().deriveFont(13f));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(CAMPO);
        tabla.setForeground(TEXTO);
        tabla.setSelectionBackground(AZUL_SELECCION);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setGridColor(BORDE);
        tabla.setShowGrid(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));
        tabla.setRowMargin(1);
        tabla.setOpaque(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setBackground(new Color(39, 54, 75));
        cabecera.setForeground(TEXTO);
        cabecera.setFont(cabecera.getFont().deriveFont(Font.BOLD, 12f));
        cabecera.setPreferredSize(new Dimension(0, 36));

        int[] anchosColumnas = {52, 110, 205, 90, 100, 105, 100};
        for (int i = 0; i < anchosColumnas.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchosColumnas[i]);
        }

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int filaVista = tabla.getSelectedRow();
            if (filaVista < 0) {
                idSeleccionado = null;
                actualizarEstadoBotones();
                return;
            }

            int fila = tabla.convertRowIndexToModel(filaVista);
            idSeleccionado = (Integer) modelo.getValueAt(fila, 0);
            codigo.setText(String.valueOf(modelo.getValueAt(fila, 1)));
            nombreProducto.setText(String.valueOf(modelo.getValueAt(fila, 2)));
            precio.setText(String.valueOf(modelo.getValueAt(fila, 3)));
            stock.setText(String.valueOf(modelo.getValueAt(fila, 4)));
            seleccionarEstado((String) modelo.getValueAt(fila, 5));
            minimo.setText(String.valueOf(modelo.getValueAt(fila, 6)));
            estadoVista.setText("Editando el producto interno #" + idSeleccionado);
            actualizarEstadoBotones();
        });

        JScrollPane tablaConScroll = new JScrollPane(tabla);
        tablaConScroll.getViewport().setBackground(CAMPO);
        tablaConScroll.setBorder(BorderFactory.createLineBorder(BORDE));
        tablaConScroll.getVerticalScrollBar().setUnitIncrement(16);
        tablaConScroll.getHorizontalScrollBar().setUnitIncrement(16);
        panel.add(tablaConScroll, BorderLayout.CENTER);

        estadoVista.setForeground(TEXTO_SUAVE);
        estadoVista.setBorder(BorderFactory.createEmptyBorder(3, 1, 0, 0));
        panel.add(estadoVista, BorderLayout.SOUTH);
        return panel;
    }

    /** Establece el color y espaciado de los campos para que el texto siempre contraste. */
    private void configurarCampos() {
        configurarCampo(codigo);
        configurarCampo(nombreProducto);
        configurarCampo(precio);
        configurarCampo(stock);
        configurarCampo(minimo);
        estado.setBackground(CAMPO);
        estado.setForeground(TEXTO);
        estado.setFont(estado.getFont().deriveFont(13f));
        estado.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean enfocado) {
                JLabel etiqueta = (JLabel) super.getListCellRendererComponent(
                        lista, valor, indice, seleccionado, enfocado);
                etiqueta.setBackground(seleccionado ? AZUL_SELECCION : CAMPO);
                etiqueta.setForeground(TEXTO);
                etiqueta.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
                return etiqueta;
            }
        });
        estado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));
    }

    private void configurarCampo(JTextField campo) {
        campo.setBackground(CAMPO);
        campo.setForeground(TEXTO);
        campo.setCaretColor(TEXTO);
        campo.setSelectionColor(AZUL_SELECCION);
        campo.setSelectedTextColor(Color.WHITE);
        campo.setFont(campo.getFont().deriveFont(13f));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evento) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AZUL),
                        BorderFactory.createEmptyBorder(7, 8, 7, 8)));
            }

            @Override
            public void focusLost(FocusEvent evento) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE),
                        BorderFactory.createEmptyBorder(7, 8, 7, 8)));
            }
        });
    }

    /** Ubica cada etiqueta y su campo en una fila de ancho completo. */
    private void agregarCampo(JPanel formulario, int fila, String texto, JComponent campo) {
        GridBagConstraints etiqueta = new GridBagConstraints();
        etiqueta.gridx = 0;
        etiqueta.gridy = fila * 2;
        etiqueta.weightx = 1;
        etiqueta.fill = GridBagConstraints.HORIZONTAL;
        etiqueta.insets = new Insets(fila == 0 ? 0 : 11, 0, 4, 0);
        etiqueta.anchor = GridBagConstraints.WEST;
        JLabel titulo = new JLabel(texto);
        titulo.setForeground(TEXTO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 12f));
        formulario.add(titulo, etiqueta);

        GridBagConstraints valor = new GridBagConstraints();
        valor.gridx = 0;
        valor.gridy = fila * 2 + 1;
        valor.weightx = 1;
        valor.fill = GridBagConstraints.HORIZONTAL;
        valor.insets = new Insets(0, 0, 2, 0);
        formulario.add(campo, valor);
    }

    /** Coloca un botón de acción con el mismo ancho que los demás. */
    private void agregarAccion(JPanel panel, JButton boton, int columna, int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columna;
        c.gridy = fila;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, columna == 0 ? 0 : 4, 4, columna == 0 ? 4 : 0);
        panel.add(boton, c);
    }

    private void estilizarBoton(JButton boton, Color fondo, Color texto) {
        boton.setOpaque(true);
        boton.setBackground(fondo);
        boton.setForeground(texto);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 12f));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boton.setRolloverEnabled(true);
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    /** Lee el catálogo fuera del hilo de la interfaz para que navegar siga siendo inmediato. */
    private void recargar() {
        if (tareaEnCurso) return;
        estadoVista.setText("Cargando productos…");
        iniciarTarea(dao::listarTodos, productos -> {
            modelo.setRowCount(0);
            for (Producto producto : productos) agregarFila(producto);
            estadoVista.setText("Productos encontrados: " + productos.size());
        });
    }

    /** Envía un producto nuevo y añade su ID devuelto sin volver a descargar toda la tabla. */
    private void guardar() {
        final Producto producto;
        try {
            producto = leer(0);
        } catch (IllegalArgumentException ex) {
            mostrarError("Revisa los datos del producto.", ex);
            return;
        }

        estadoVista.setText("Guardando producto en el inventario…");
        iniciarTarea(() -> dao.agregarYObtenerId(producto), id -> {
            producto.setIdProducto(id);
            agregarFila(producto);
            limpiar();
            estadoVista.setText("Producto agregado. Total: " + modelo.getRowCount());
        });
    }

    /** Guarda los cambios del producto seleccionado y reemplaza la fila actual. */
    private void actualizar() {
        if (idSeleccionado == null) return;
        final int id = idSeleccionado;
        final Producto producto;
        try {
            producto = leer(id);
        } catch (IllegalArgumentException ex) {
            mostrarError("Revisa los datos del producto.", ex);
            return;
        }

        estadoVista.setText("Guardando cambios…");
        iniciarTarea(() -> dao.actualizar(producto), actualizado -> {
            if (!actualizado) {
                estadoVista.setText("No se encontró el producto para actualizar.");
                return;
            }
            actualizarFila(producto);
            estadoVista.setText("Cambios guardados para el producto interno #" + id);
        });
    }

    /** Pide confirmación antes de borrar el registro seleccionado. */
    private void eliminar() {
        if (idSeleccionado == null) return;
        int id = idSeleccionado;
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el producto interno #" + id + "?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion != JOptionPane.YES_OPTION) return;

        estadoVista.setText("Eliminando producto…");
        iniciarTarea(() -> dao.eliminar(id), eliminado -> {
            if (!eliminado) {
                estadoVista.setText("No se encontró el producto para eliminar.");
                return;
            }
            int fila = buscarFila(id);
            if (fila >= 0) modelo.removeRow(fila);
            limpiar();
            estadoVista.setText("Producto eliminado. Total: " + modelo.getRowCount());
        });
    }

    /** Ejecuta una operación de base de datos en segundo plano y devuelve el resultado a Swing. */
    private <T> void iniciarTarea(Callable<T> tarea, Consumer<T> alCompletar) {
        tareaEnCurso = true;
        actualizarEstadoBotones();
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            @Override
            protected void done() {
                try {
                    alCompletar.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    estadoVista.setText("La operación fue interrumpida.");
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    mostrarError("No se pudo completar la operación.", causa);
                    estadoVista.setText("La operación no se completó.");
                } finally {
                    tareaEnCurso = false;
                    actualizarEstadoBotones();
                }
            }
        }.execute();
    }

    private void agregarFila(Producto producto) {
        modelo.addRow(convertirFila(producto));
    }

    private void actualizarFila(Producto producto) {
        int fila = buscarFila(producto.getIdProducto());
        if (fila < 0) return;
        Object[] valores = convertirFila(producto);
        for (int columna = 0; columna < valores.length; columna++) {
            modelo.setValueAt(valores[columna], fila, columna);
        }
    }

    private Object[] convertirFila(Producto producto) {
        return new Object[]{producto.getIdProducto(), producto.getCodigo(), producto.getDescripcion(),
                producto.getPrecio(), producto.getStockInventario(), producto.getEstado(), producto.getStockMinimo()};
    }

    private int buscarFila(int id) {
        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            if (((Integer) modelo.getValueAt(fila, 0)) == id) return fila;
        }
        return -1;
    }

    /** Valida los valores del formulario y crea el objeto que se enviará al DAO. */
    private Producto leer(int id) {
        try {
            String cod = codigo.getText().trim();
            double valor = Double.parseDouble(precio.getText().trim().replace(',', '.'));
            int cantidad = Integer.parseInt(stock.getText().trim());
            int stockMinimo = Integer.parseInt(minimo.getText().trim());
            String nombre = nombreProducto.getText().trim();
            String estadoSeleccionado = (String) estado.getSelectedItem();
            if (cod.isEmpty() || nombre.isEmpty() || estadoSeleccionado == null
                    || !Double.isFinite(valor) || valor < 0 || cantidad < 0 || stockMinimo < 0) {
                throw new IllegalArgumentException(
                        "Completa el código, el nombre y los valores numéricos no negativos.");
            }
            return new Producto(id, cod, valor, cantidad, nombre, estadoSeleccionado, stockMinimo);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Precio, stock y stock mínimo deben tener un formato numérico válido.", ex);
        }
    }

    /** Mantiene estados antiguos visibles cuando se modifica un registro existente. */
    private void seleccionarEstado(String valor) {
        if (valor == null) {
            estado.setSelectedItem("Activo");
            return;
        }
        for (int i = 0; i < estado.getItemCount(); i++) {
            if (estado.getItemAt(i).equalsIgnoreCase(valor)) {
                estado.setSelectedIndex(i);
                return;
            }
        }
        estado.addItem(valor);
        estado.setSelectedItem(valor);
    }

    /** Limpia el formulario y vuelve al modo de creación. */
    private void limpiar() {
        tabla.clearSelection();
        idSeleccionado = null;
        codigo.setText("");
        precio.setText("");
        stock.setText("");
        nombreProducto.setText("");
        estado.setSelectedItem("Activo");
        minimo.setText("");
        actualizarEstadoBotones();
    }

    private void actualizarEstadoBotones() {
        guardar.setEnabled(!tareaEnCurso);
        actualizar.setEnabled(!tareaEnCurso && idSeleccionado != null);
        eliminar.setEnabled(!tareaEnCurso && idSeleccionado != null);
        limpiar.setEnabled(!tareaEnCurso);
        refrescar.setEnabled(!tareaEnCurso);
    }

    private void mostrarError(String texto, Throwable ex) {
        String detalle = ex.getMessage() == null ? "Ocurrió un error inesperado." : ex.getMessage();
        JOptionPane.showMessageDialog(this, texto + "\n" + detalle,
                "Catálogo de inventario", JOptionPane.ERROR_MESSAGE);
    }
}
