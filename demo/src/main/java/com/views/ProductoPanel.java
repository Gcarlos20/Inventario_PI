package com.views;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** CRUD de productos embebido en el área de contenido de la ventana principal. */
public class ProductoPanel extends JPanel {
    private final ProductoDAO dao = new ProductoDAO();
    private final Runnable regresar;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Código", "Nombre del producto", "Precio", "Stock actual", "Estado", "Stock mínimo"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JTextField codigo = new JTextField();
    private final JTextField precio = new JTextField();
    private final JTextField stock = new JTextField();
    private final JTextField nombreProducto = new JTextField();
    private final JComboBox<String> estado =
            new JComboBox<>(new String[]{"Activo", "Disponible", "Poco stock"});
    private final JTextField minimo = new JTextField();
    private final JLabel estadoVista = new JLabel(" ");
    private Integer idSeleccionado;

    public ProductoPanel(Runnable regresar) {
        this.regresar = regresar;
        construirVista();
        recargar();
    }

    /** Distribuye formulario, tabla y acciones en una vista adaptable. */
    private void construirVista() {
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);
        // Bloque de título y ayuda para identificar la pantalla como catálogo de inventario.
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JButton volver = new JButton("←  Regresar al panel");
        volver.addActionListener(e -> regresar.run());
        barra.add(volver, BorderLayout.WEST);
        JLabel ayuda = new JLabel("Administra nombres, existencias y niveles mínimos");
        ayuda.setForeground(new Color(100, 110, 128));
        barra.add(ayuda, BorderLayout.EAST);
        add(barra, BorderLayout.NORTH);

        // Bloque de formulario vertical: cada etiqueta ocupa todo el ancho y no queda recortada.
        JPanel formulario = new JPanel(new GridLayout(0, 1, 0, 3));
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 231, 239)),
                BorderFactory.createEmptyBorder(16, 14, 16, 14)));
        agregarCampo(formulario, "Código", codigo);
        agregarCampo(formulario, "Nombre del producto", nombreProducto);
        agregarCampo(formulario, "Precio", precio);
        agregarCampo(formulario, "Stock actual", stock);
        agregarCampo(formulario, "Estado", estado);
        agregarCampo(formulario, "Stock mínimo", minimo);
        JButton guardar = new JButton("Guardar producto");
        JButton actualizar = new JButton("Actualizar");
        JButton eliminar = new JButton("Eliminar");
        JButton limpiar = new JButton("Nuevo / Limpiar");
        JButton refrescar = new JButton("Recargar");
        JPanel acciones = new JPanel(new GridLayout(0, 1, 0, 7));
        acciones.setOpaque(false);
        acciones.add(guardar); acciones.add(actualizar); acciones.add(eliminar);
        acciones.add(limpiar); acciones.add(refrescar);
        JPanel lado = new JPanel(new BorderLayout(0, 12));
        lado.setOpaque(false);
        lado.add(formulario, BorderLayout.NORTH);
        lado.add(acciones, BorderLayout.CENTER);
        JScrollPane ladoConDesplazamiento = new JScrollPane(lado);
        ladoConDesplazamiento.setBorder(null);
        ladoConDesplazamiento.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ladoConDesplazamiento.setPreferredSize(new Dimension(220, 0));

        // Bloque de tabla: conserva el encabezado nativo y evita que los títulos se compriman.
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(27);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        int[] anchosColumnas = {42, 70, 150, 65, 85, 85, 85};
        for (int i = 0; i < anchosColumnas.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchosColumnas[i]);
        }
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
                idSeleccionado = (Integer) modelo.getValueAt(fila, 0);
                codigo.setText(String.valueOf(modelo.getValueAt(fila, 1)));
                nombreProducto.setText(String.valueOf(modelo.getValueAt(fila, 2)));
                precio.setText(String.valueOf(modelo.getValueAt(fila, 3)));
                stock.setText(String.valueOf(modelo.getValueAt(fila, 4)));
                seleccionarEstado((String) modelo.getValueAt(fila, 5));
                minimo.setText(String.valueOf(modelo.getValueAt(fila, 6)));
                estadoVista.setText("Producto seleccionado: #" + idSeleccionado);
            }
        });
        JPanel tablaPanel = new JPanel(new BorderLayout(0, 8));
        tablaPanel.setOpaque(false);
        tablaPanel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        tablaPanel.add(estadoVista, BorderLayout.SOUTH);
        add(ladoConDesplazamiento, BorderLayout.WEST);
        add(tablaPanel, BorderLayout.CENTER);

        guardar.addActionListener(e -> guardar());
        actualizar.addActionListener(e -> actualizar());
        eliminar.addActionListener(e -> eliminar());
        limpiar.addActionListener(e -> limpiar());
        refrescar.addActionListener(e -> recargar());
    }

    /** Añade una etiqueta y su campo al formulario. */
    private void agregarCampo(JPanel panel, String etiqueta, JComponent campo) {
        panel.add(new JLabel(etiqueta + ":"));
        panel.add(campo);
    }

    /** Carga el catálogo desde el DAO y repinta la tabla. */
    private void recargar() {
        try {
            modelo.setRowCount(0);
            List<Producto> productos = dao.listarTodos();
            for (Producto p : productos) modelo.addRow(new Object[]{p.getIdProducto(), p.getCodigo(),
                    p.getDescripcion(), p.getPrecio(), p.getStockInventario(), p.getEstado(), p.getStockMinimo()});
            estadoVista.setText("Productos encontrados: " + productos.size());
        } catch (Exception ex) { mostrarError("No se pudieron cargar los productos.", ex); }
    }

    /** Valida el nombre, las existencias y el estado seleccionado antes de crear el producto. */
    private void guardar() {
        try {
            Producto p = leer(0);
            if (!dao.agregar(p)) {
                throw new IllegalStateException("La base de datos no confirmó el registro del producto.");
            }
            limpiar();
            recargar();
            mensaje("Producto agregado correctamente.");
        } catch (Exception ex) { mostrarError("No se pudo guardar el producto.", ex); }
    }

    /** Persiste los cambios del producto seleccionado. */
    private void actualizar() {
        if (idSeleccionado == null) { mensaje("Selecciona un producto de la tabla."); return; }
        try {
            if (dao.actualizar(leer(idSeleccionado))) { limpiar(); recargar(); }
        } catch (Exception ex) { mostrarError("No se pudo actualizar el producto.", ex); }
    }

    /** Confirma y elimina el producto seleccionado. */
    private void eliminar() {
        if (idSeleccionado == null) { mensaje("Selecciona un producto de la tabla."); return; }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el producto seleccionado?", "Confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { if (dao.eliminar(idSeleccionado)) { limpiar(); recargar(); } }
        catch (Exception ex) { mostrarError("No se pudo eliminar el producto.", ex); }
    }

    /** Convierte y valida los valores escritos en los campos. */
    private Producto leer(int id) {
        try {
            String cod = codigo.getText().trim();
            new BigDecimal(cod);
            double valor = Double.parseDouble(precio.getText().trim().replace(',', '.'));
            int cantidad = Integer.parseInt(stock.getText().trim());
            int stockMin = Integer.parseInt(minimo.getText().trim());
            String nombre = nombreProducto.getText().trim();
            String estadoSeleccionado = (String) estado.getSelectedItem();
            if (nombre.isEmpty() || estadoSeleccionado == null
                    || !Double.isFinite(valor) || valor < 0 || cantidad < 0 || stockMin < 0)
                throw new IllegalArgumentException("Completa los campos y usa valores no negativos.");
            return new Producto(id, cod, valor, cantidad, nombre, estadoSeleccionado, stockMin);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Código, precio, stock y stock mínimo deben tener un formato válido.", ex);
        }
    }

    // Bloque de selección de estado que conserva valores históricos al editar registros existentes.
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

    /** Limpia selección y campos para iniciar otro registro. */
    private void limpiar() {
        tabla.clearSelection(); idSeleccionado = null;
        codigo.setText(""); precio.setText(""); stock.setText(""); nombreProducto.setText("");
        estado.setSelectedItem("Activo"); minimo.setText(""); estadoVista.setText("Nuevo producto");
    }

    private void mensaje(String texto) { JOptionPane.showMessageDialog(this, texto, "Inventario", JOptionPane.INFORMATION_MESSAGE); }
    private void mostrarError(String texto, Exception ex) { JOptionPane.showMessageDialog(this, texto + "\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
}
