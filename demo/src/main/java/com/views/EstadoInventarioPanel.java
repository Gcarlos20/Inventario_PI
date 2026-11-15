package com.views;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/** Vista de consulta para revisar y filtrar las existencias registradas. */
public class EstadoInventarioPanel extends JPanel {
    // La paleta conserva el estilo oscuro usado por el resto del sistema.
    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color SUPERFICIE = new Color(30, 41, 59);
    private static final Color CAMPO = new Color(15, 23, 42);
    private static final Color BORDE = new Color(51, 65, 85);
    private static final Color TEXTO = new Color(226, 232, 240);
    private static final Color SUAVE = new Color(148, 163, 184);
    private static final Color VERDE = new Color(34, 197, 142);
    private static final Color AZUL = new Color(56, 189, 248);
    private static final Color AMBAR = new Color(245, 158, 66);

    private final ProductoDAO dao = new ProductoDAO();
    private final JTextField busqueda = new JTextField();
    private final JComboBox<String> filtroEstado = new JComboBox<>(
            new String[]{"Todos los estados", "Activo", "Disponible", "Poco stock"});
    private final JLabel mensaje = new JLabel("Cargando inventario…");
    private final JLabel cantidadProductos = new JLabel("0");
    private final JLabel cantidadUnidades = new JLabel("0");
    private final JLabel cantidadBajo = new JLabel("0");

    // La primera columna muestra un número consecutivo; las demás contienen los datos del producto.
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"N.º", "ID", "Código", "Nombre del producto", "Precio", "Stock", "Estado", "Stock mínimo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> ordenador = new TableRowSorter<>(modelo);

    public EstadoInventarioPanel() {
        construirVista();
        recargar();
    }

    /** Ensambla búsqueda, indicadores de color y la tabla de productos. */
    private void construirVista() {
        setLayout(new BorderLayout(12, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        JPanel parteSuperior = new JPanel(new BorderLayout(0, 12));
        parteSuperior.setOpaque(false);
        parteSuperior.add(crearBarraBusqueda(), BorderLayout.NORTH);
        parteSuperior.add(crearIndicadores(), BorderLayout.CENTER);
        add(parteSuperior, BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);

        // Actualiza los resultados en cuanto cambia el texto o el estado elegido.
        busqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent evento) { aplicarFiltros(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent evento) { aplicarFiltros(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent evento) { aplicarFiltros(); }
        });
        filtroEstado.addActionListener(evento -> aplicarFiltros());
    }

    /** Crea la búsqueda por ID/nombre, el filtro por estado y la acción de recarga. */
    private JPanel crearBarraBusqueda() {
        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setBackground(SUPERFICIE);
        barra.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(11, 13, 11, 13)));

        JPanel controles = new JPanel(new BorderLayout(8, 0));
        controles.setOpaque(false);
        JLabel etiquetaBusqueda = new JLabel("Buscar por ID o nombre");
        etiquetaBusqueda.setForeground(TEXTO);
        etiquetaBusqueda.setFont(etiquetaBusqueda.getFont().deriveFont(Font.BOLD, 13f));
        busqueda.setToolTipText("Escribe el ID o el nombre del producto");
        busqueda.setForeground(TEXTO);
        busqueda.setCaretColor(TEXTO);
        busqueda.setBackground(CAMPO);
        busqueda.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(8, 9, 8, 9)));
        JPanel campoBusqueda = new JPanel(new BorderLayout(0, 5));
        campoBusqueda.setOpaque(false);
        campoBusqueda.add(etiquetaBusqueda, BorderLayout.NORTH);
        campoBusqueda.add(busqueda, BorderLayout.CENTER);
        controles.add(campoBusqueda, BorderLayout.CENTER);

        JPanel campoEstado = new JPanel(new BorderLayout(0, 5));
        campoEstado.setOpaque(false);
        JLabel etiquetaEstado = new JLabel("Estado");
        etiquetaEstado.setForeground(TEXTO);
        etiquetaEstado.setFont(etiquetaEstado.getFont().deriveFont(Font.BOLD, 13f));
        filtroEstado.setForeground(TEXTO);
        filtroEstado.setBackground(CAMPO);
        campoEstado.add(etiquetaEstado, BorderLayout.NORTH);
        campoEstado.add(filtroEstado, BorderLayout.CENTER);
        campoEstado.setPreferredSize(new Dimension(170, 54));
        controles.add(campoEstado, BorderLayout.EAST);
        barra.add(controles, BorderLayout.CENTER);

        JButton actualizar = new JButton("Actualizar");
        actualizar.setForeground(Color.WHITE);
        actualizar.setBackground(new Color(59, 130, 246));
        actualizar.setFocusPainted(false);
        actualizar.addActionListener(evento -> recargar());
        barra.add(actualizar, BorderLayout.EAST);
        return barra;
    }

    /** Muestra cifras rápidas; el color relaciona cada número con su tipo de dato. */
    private JPanel crearIndicadores() {
        JPanel indicadores = new JPanel(new java.awt.GridLayout(1, 3, 10, 0));
        indicadores.setOpaque(false);
        indicadores.add(crearIndicador("Productos registrados", cantidadProductos, AZUL));
        indicadores.add(crearIndicador("Unidades en inventario", cantidadUnidades, VERDE));
        indicadores.add(crearIndicador("Con poco stock", cantidadBajo, AMBAR));
        return indicadores;
    }

    /** Arma una tarjeta de resumen con una cifra destacada y acento de color. */
    private JPanel crearIndicador(String titulo, JLabel numero, Color acento) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 3));
        tarjeta.setBackground(SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, acento),
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                        BorderFactory.createEmptyBorder(8, 11, 8, 11))));
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setForeground(SUAVE);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 11f));
        numero.setForeground(acento);
        numero.setFont(numero.getFont().deriveFont(Font.BOLD, 25f));
        tarjeta.add(etiqueta, BorderLayout.NORTH);
        tarjeta.add(numero, BorderLayout.CENTER);
        return tarjeta;
    }

    /** Configura la tabla con identificadores, existencias y estados legibles por color. */
    private JPanel crearTabla() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(SUPERFICIE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(11, 12, 10, 12)));
        JLabel titulo = new JLabel("Detalle de productos en inventario");
        titulo.setForeground(TEXTO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(titulo, BorderLayout.NORTH);

        tabla.setRowSorter(ordenador);
        tabla.setRowHeight(30);
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(CAMPO);
        tabla.setForeground(TEXTO);
        tabla.setSelectionBackground(new Color(30, 64, 112));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setGridColor(BORDE);
        tabla.setShowGrid(true);
        tabla.setFont(tabla.getFont().deriveFont(12f));
        tabla.getTableHeader().setReorderingAllowed(false);
        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setBackground(new Color(39, 54, 75));
        encabezado.setForeground(TEXTO);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 12f));
        encabezado.setPreferredSize(new Dimension(0, 34));
        int[] anchos = {48, 55, 105, 190, 90, 75, 105, 95};
        for (int columna = 0; columna < anchos.length; columna++) {
            tabla.getColumnModel().getColumn(columna).setPreferredWidth(anchos[columna]);
        }
        tabla.getColumnModel().getColumn(6).setCellRenderer(new RenderizadorEstado());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(CAMPO);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        panel.add(scroll, BorderLayout.CENTER);
        mensaje.setForeground(SUAVE);
        mensaje.setBorder(BorderFactory.createEmptyBorder(3, 1, 0, 0));
        panel.add(mensaje, BorderLayout.SOUTH);
        return panel;
    }

    /** Carga todos los productos fuera del hilo gráfico y actualiza tabla e indicadores. */
    private void recargar() {
        mensaje.setText("Actualizando inventario…");
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return dao.listarTodos();
            }

            @Override
            protected void done() {
                try {
                    List<Producto> productos = get();
                    modelo.setRowCount(0);
                    int unidades = 0;
                    int bajoStock = 0;
                    int consecutivo = 1;
                    for (Producto producto : productos) {
                        String estado = obtenerEstadoVisible(producto);
                        unidades += producto.getStockInventario();
                        if ("Poco stock".equalsIgnoreCase(estado)) bajoStock++;
                        modelo.addRow(new Object[]{consecutivo++, producto.getIdProducto(), producto.getCodigo(),
                                producto.getDescripcion(), producto.getPrecio(), producto.getStockInventario(),
                                estado, producto.getStockMinimo()});
                    }
                    cantidadProductos.setText(String.valueOf(productos.size()));
                    cantidadUnidades.setText(String.valueOf(unidades));
                    cantidadBajo.setText(String.valueOf(bajoStock));
                    aplicarFiltros();
                    mensaje.setText("Productos encontrados: " + tabla.getRowCount());
                } catch (Exception error) {
                    mensaje.setText("No se pudo cargar el inventario. Revisa la conexión y vuelve a actualizar.");
                }
            }
        }.execute();
    }

    /** Convierte en “Poco stock” cualquier artículo que alcanzó su mínimo configurado. */
    private String obtenerEstadoVisible(Producto producto) {
        if (producto.getStockInventario() <= producto.getStockMinimo()) return "Poco stock";
        String estado = producto.getEstado();
        return estado == null || estado.trim().isEmpty() ? "Sin estado" : estado;
    }

    /** Combina la búsqueda parcial por ID/nombre con el estado seleccionado. */
    private void aplicarFiltros() {
        final String consulta = busqueda.getText().trim().toLowerCase(Locale.ROOT);
        final String estadoElegido = (String) filtroEstado.getSelectedItem();
        ordenador.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> fila) {
                String id = String.valueOf(fila.getValue(1)).toLowerCase(Locale.ROOT);
                String nombre = String.valueOf(fila.getValue(3)).toLowerCase(Locale.ROOT);
                String estado = String.valueOf(fila.getValue(6));
                boolean coincideTexto = consulta.isEmpty() || id.contains(consulta) || nombre.contains(consulta);
                boolean coincideEstado = estadoElegido == null || "Todos los estados".equals(estadoElegido)
                        || estadoElegido.equalsIgnoreCase(estado);
                return coincideTexto && coincideEstado;
            }
        });
        if (tabla.getRowCount() > 0) {
            mensaje.setText("Productos encontrados: " + tabla.getRowCount());
        } else if (modelo.getRowCount() > 0) {
            mensaje.setText("No hay productos que coincidan con la búsqueda.");
        }
    }

    /** Colorea el texto del estado según su significado para facilitar el escaneo de filas. */
    private static class RenderizadorEstado extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                                                                 boolean tieneFoco, int fila, int columna) {
            JLabel etiqueta = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);
            etiqueta.setHorizontalAlignment(CENTER);
            etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD));
            String estado = valor == null ? "" : valor.toString();
            if (!seleccionado) {
                etiqueta.setForeground("Poco stock".equalsIgnoreCase(estado) ? AMBAR
                        : "Disponible".equalsIgnoreCase(estado) ? VERDE
                        : "Activo".equalsIgnoreCase(estado) ? AZUL : SUAVE);
                etiqueta.setBackground(CAMPO);
            }
            return etiqueta;
        }
    }
}
