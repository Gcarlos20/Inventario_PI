package com.views;

import com.Daos.CompraDao;
import com.Daos.ProductoDAO;
import com.Daos.ProveedorDao;
import com.model.Compra;
import com.model.DetalleCompra;
import com.model.Lote;
import com.model.Producto;
import com.model.Proveedor;
import com.model.Usuario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/** Captura de compras con una lista temporal de productos, todavía sin persistencia. */
public class CompraPanel extends JPanel {
    private static final int ID_TIPO_MOVIMIENTO_ENTRADA = 1;
    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color SUPERFICIE = new Color(30, 41, 59);
    private static final Color CAMPO = new Color(15, 23, 42);
    private static final Color BORDE = new Color(51, 65, 85);
    private static final Color TEXTO = new Color(226, 232, 240);
    private static final Color TEXTO_SUAVE = new Color(148, 163, 184);
    private static final Color AZUL = new Color(59, 130, 246);
    private static final Color AZUL_SELECCION = new Color(30, 64, 112);
    private static final Color VERDE = new Color(34, 197, 142);
    private static final Color ROJO = new Color(153, 53, 65);

    private final ProveedorDao proveedorDao = new ProveedorDao();
    private final ProductoDAO productoDao = new ProductoDAO();
    private final CompraDao compraDao = new CompraDao();
    private final Usuario usuarioActivo;
    private final Runnable refrescarDatos;

    private final JComboBox<Proveedor> proveedor = new JComboBox<>();
    private final JTextField numeroFactura = new JTextField();
    private final JComboBox<String> metodoPago =
            new JComboBox<>(new String[]{"Efectivo", "Transferencia", "Crédito"});
    private final JTextField fechaCompra = new JTextField(LocalDate.now().toString());
    private final JTextField observacion = new JTextField();

    private final JTextField busquedaProducto = new JTextField();
    private final JComboBox<Producto> productosEncontrados = new JComboBox<>();
    private final JLabel stockActual = new JLabel("Selecciona un producto");
    private final JTextField cantidad = new JTextField("1");
    private final JTextField costoUnitario = new JTextField();
    private final JTextField fechaVencimiento = new JTextField();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Producto", "Cantidad", "Costo Unitario",
                    "Fecha Vencimiento", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final List<LineaCompra> lineasCompra = new ArrayList<>();
    private final JLabel subtotal = new JLabel("$ 0.00");
    private final JLabel total = new JLabel("$ 0.00");
    private final JLabel estado = new JLabel("Productos de la compra: 0");
    private final JButton buscar = new JButton("Buscar");
    private final JButton agregar = new JButton("Agregar a la lista");
    private final JButton eliminar = new JButton("Eliminar fila");
    private final JButton registrar = new JButton("Registrar entrada");
    private boolean buscandoProductos;
    private boolean guardando;

    public CompraPanel(Usuario usuarioActivo, Runnable refrescarDatos) {
        if (usuarioActivo == null || usuarioActivo.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("Se requiere un usuario activo con ID válido.");
        }
        this.usuarioActivo = usuarioActivo;
        this.refrescarDatos = Objects.requireNonNull(
                refrescarDatos, "La acción de refresco de inventario es obligatoria.");
        construirVista();
        cargarProveedores();
        actualizarEstadoRegistro();
    }

    private void construirVista() {
        setLayout(new BorderLayout(12, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        JPanel bloquesSuperiores = new JPanel(new java.awt.GridLayout(2, 1, 0, 10));
        bloquesSuperiores.setOpaque(false);
        bloquesSuperiores.add(crearBloqueGeneral());
        bloquesSuperiores.add(crearBloqueProductos());
        add(bloquesSuperiores, BorderLayout.NORTH);
        add(crearBloqueDetalle(), BorderLayout.CENTER);

        configurarCampos();
        configurarTabla();
        configurarEventos();
    }

    private JPanel crearBloqueGeneral() {
        JPanel bloque = crearTarjeta("Datos generales de la compra");
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);

        agregarCampo(formulario, "Proveedor", proveedor, 0, 0, 2);
        agregarCampo(formulario, "Número de factura", numeroFactura, 2, 0, 1);
        agregarCampo(formulario, "Método de pago", metodoPago, 3, 0, 1);
        agregarCampo(formulario, "Fecha de compra (AAAA-MM-DD)", fechaCompra, 0, 1, 1);
        agregarCampo(formulario, "Observación general", observacion, 1, 1, 3);
        bloque.add(formulario, BorderLayout.CENTER);
        return bloque;
    }

    private JPanel crearBloqueProductos() {
        JPanel bloque = crearTarjeta("Agregar productos");
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);

        agregarCampo(formulario, "Buscar por código o nombre", busquedaProducto, 0, 0, 2);
        agregarComponente(formulario, buscar, 2, 0, 1, 1, 0);
        agregarCampo(formulario, "Producto encontrado", productosEncontrados, 3, 0, 2);
        configurarStockActual();
        agregarCampo(formulario, "Stock actual", stockActual, 5, 0, 1);
        agregarCampo(formulario, "Cantidad a ingresar", cantidad, 0, 1, 1);
        agregarCampo(formulario, "Costo unitario", costoUnitario, 1, 1, 1);
        agregarCampo(formulario, "Fecha de vencimiento (AAAA-MM-DD)", fechaVencimiento, 2, 1, 2);
        agregarComponente(formulario, agregar, 4, 1, 2, 1, 0);
        bloque.add(formulario, BorderLayout.CENTER);
        return bloque;
    }

    private JPanel crearBloqueDetalle() {
        JPanel bloque = crearTarjeta("Detalle de la compra");
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Detalle de la compra");
        titulo.setForeground(TEXTO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 14f));
        encabezado.add(titulo, BorderLayout.WEST);
        estilizarBoton(eliminar, ROJO, Color.WHITE);
        encabezado.add(eliminar, BorderLayout.EAST);
        bloque.add(encabezado, BorderLayout.NORTH);

        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.getViewport().setBackground(CAMPO);
        desplazamiento.setBorder(BorderFactory.createLineBorder(BORDE));
        desplazamiento.getVerticalScrollBar().setUnitIncrement(16);
        bloque.add(desplazamiento, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(10, 0));
        pie.setOpaque(false);
        estado.setForeground(TEXTO_SUAVE);
        pie.add(estado, BorderLayout.WEST);

        JPanel resumen = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
        resumen.setOpaque(false);
        resumen.add(crearTotal("SubTotal", subtotal, TEXTO));
        resumen.add(crearTotal("Total", total, VERDE));
        estilizarBoton(registrar, AZUL, Color.WHITE);
        resumen.add(registrar);
        pie.add(resumen, BorderLayout.EAST);
        bloque.add(pie, BorderLayout.SOUTH);
        return bloque;
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(SUPERFICIE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel encabezado = new JLabel(titulo);
        encabezado.setForeground(TEXTO);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 15f));
        tarjeta.add(encabezado, BorderLayout.NORTH);
        return tarjeta;
    }

    private JPanel crearTotal(String titulo, JLabel valor, Color color) {
        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setOpaque(false);
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setForeground(TEXTO_SUAVE);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 11f));
        valor.setForeground(color);
        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 21f));
        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(valor, BorderLayout.CENTER);
        return panel;
    }

    private void agregarCampo(JPanel panel, String texto, JComponent campo,
                              int columna, int fila, int ancho) {
        JPanel contenedor = new JPanel(new BorderLayout(0, 4));
        contenedor.setOpaque(false);
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(TEXTO);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 11f));
        contenedor.add(etiqueta, BorderLayout.NORTH);
        contenedor.add(campo, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columna;
        c.gridy = fila;
        c.gridwidth = ancho;
        c.weightx = ancho;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(fila == 0 ? 2 : 7, 4, 3, 4);
        panel.add(contenedor, c);
    }

    private void agregarComponente(JPanel panel, JComponent componente,
                                   int columna, int fila, int ancho, int alto, int relleno) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columna;
        c.gridy = fila;
        c.gridwidth = ancho;
        c.gridheight = alto;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(14, 4, 3, 4);
        c.ipadx = relleno;
        panel.add(componente, c);
    }

    private void configurarCampos() {
        configurarCampo(numeroFactura);
        configurarCampo(fechaCompra);
        configurarCampo(observacion);
        configurarCampo(busquedaProducto);
        configurarCampo(cantidad);
        configurarCampo(costoUnitario);
        configurarCampo(fechaVencimiento);
        estilizarBoton(buscar, new Color(55, 75, 105), TEXTO);
        estilizarBoton(agregar, VERDE, Color.WHITE);
        configurarCombo(proveedor);
        configurarCombo(metodoPago);
        configurarCombo(productosEncontrados);
        proveedor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean enfocado) {
                JLabel etiqueta = (JLabel) super.getListCellRendererComponent(
                        lista, valor, indice, seleccionado, enfocado);
                estilizarElementoLista(etiqueta, seleccionado);
                if (valor == null) {
                    etiqueta.setText("Selecciona un proveedor");
                }
                return etiqueta;
            }
        });
        metodoPago.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean enfocado) {
                JLabel etiqueta = (JLabel) super.getListCellRendererComponent(
                        lista, valor, indice, seleccionado, enfocado);
                estilizarElementoLista(etiqueta, seleccionado);
                return etiqueta;
            }
        });
        productosEncontrados.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean enfocado) {
                JLabel etiqueta = (JLabel) super.getListCellRendererComponent(
                        lista, valor, indice, seleccionado, enfocado);
                estilizarElementoLista(etiqueta, seleccionado);
                if (valor instanceof Producto) {
                    Producto producto = (Producto) valor;
                    etiqueta.setText(producto.getCodigo() + " — " + producto.getDescripcion());
                }
                return etiqueta;
            }
        });
    }

    private void configurarTabla() {
        tabla.setRowHeight(30);
        tabla.setFont(tabla.getFont().deriveFont(13f));
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
        tabla.setDefaultRenderer(Object.class, new RenderizadorDetalle());
        tabla.getTableHeader().setReorderingAllowed(false);
        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setBackground(new Color(39, 54, 75));
        cabecera.setForeground(TEXTO);
        cabecera.setFont(cabecera.getFont().deriveFont(Font.BOLD, 12f));
        cabecera.setPreferredSize(new Dimension(0, 36));
        cabecera.setDefaultRenderer(new RenderizadorEncabezado());
        int[] anchos = {90, 220, 85, 120, 140, 110};
        for (int columna = 0; columna < anchos.length; columna++) {
            tabla.getColumnModel().getColumn(columna).setPreferredWidth(anchos[columna]);
        }
    }

    private void configurarEventos() {
        buscar.addActionListener(evento -> buscarProductos());
        busquedaProducto.addActionListener(evento -> buscarProductos());
        productosEncontrados.addActionListener(evento -> mostrarStockSeleccionado());
        agregar.addActionListener(evento -> agregarProducto());
        eliminar.addActionListener(evento -> eliminarProductoSeleccionado());
        registrar.addActionListener(evento -> guardarCompra());
        proveedor.addActionListener(evento -> actualizarEstadoRegistro());
        numeroFactura.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent evento) {
                actualizarEstadoRegistro();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent evento) {
                actualizarEstadoRegistro();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent evento) {
                actualizarEstadoRegistro();
            }
        });
    }

    private void cargarProveedores() {
        estado.setText("Cargando proveedores…");
        new SwingWorker<List<Proveedor>, Void>() {
            @Override
            protected List<Proveedor> doInBackground() throws Exception {
                return proveedorDao.listarParaComboBox();
            }

            @Override
            protected void done() {
                try {
                    List<Proveedor> proveedores = get();
                    proveedor.removeAllItems();
                    proveedor.addItem(null);
                    for (Proveedor valor : proveedores) {
                        proveedor.addItem(valor);
                    }
                    proveedor.setSelectedIndex(0);
                    estado.setText(proveedores.isEmpty()
                            ? "No hay proveedores disponibles."
                            : "Proveedores cargados: " + proveedores.size());
                    actualizarEstadoRegistro();
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    mostrarError("Se interrumpió la carga de proveedores.", error);
                } catch (ExecutionException error) {
                    mostrarError("No se pudieron cargar los proveedores.", causa(error));
                }
            }
        }.execute();
    }

    private void buscarProductos() {
        String termino = busquedaProducto.getText().trim();
        if (termino.isEmpty()) {
            mostrarError("Escribe un código o nombre para buscar productos.", null);
            return;
        }
        if (buscandoProductos) {
            return;
        }
        if (guardando) {
            return;
        }

        buscandoProductos = true;
        buscar.setEnabled(false);
        estado.setText("Buscando productos…");
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return productoDao.buscarPorCodigoONombre(termino);
            }

            @Override
            protected void done() {
                try {
                    List<Producto> encontrados = get();
                    productosEncontrados.removeAllItems();
                    for (Producto producto : encontrados) {
                        productosEncontrados.addItem(producto);
                    }
                    if (encontrados.isEmpty()) {
                        stockActual.setText("Sin resultados");
                        estado.setText("No se encontraron productos.");
                    } else {
                        productosEncontrados.setSelectedIndex(0);
                        estado.setText("Productos encontrados: " + encontrados.size());
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    mostrarError("Se interrumpió la búsqueda de productos.", error);
                } catch (ExecutionException error) {
                    mostrarError("No se pudieron buscar productos.", causa(error));
                } finally {
                    buscandoProductos = false;
                    buscar.setEnabled(!guardando);
                }
            }
        }.execute();
    }

    private void mostrarStockSeleccionado() {
        Producto producto = (Producto) productosEncontrados.getSelectedItem();
        stockActual.setText(producto == null
                ? "Selecciona un producto"
                : producto.getStockInventario() + " unidades");
    }

    private void agregarProducto() {
        if (guardando) {
            return;
        }
        Producto producto = (Producto) productosEncontrados.getSelectedItem();
        if (producto == null) {
            mostrarError("Busca y selecciona un producto antes de agregarlo.", null);
            return;
        }

        try {
            int unidades = Integer.parseInt(cantidad.getText().trim());
            BigDecimal precio = new BigDecimal(costoUnitario.getText().trim().replace(',', '.'));
            LocalDate vencimiento = leerFechaVencimiento();
            validarCantidadYCosto(unidades, precio);

            int filaExistente = buscarLinea(producto.getIdProducto(), vencimiento);
            if (filaExistente >= 0) {
                LineaCompra linea = lineasCompra.get(filaExistente);
                linea.agregar(unidades, precio);
                actualizarFila(filaExistente, linea);
                estado.setText("Cantidad consolidada en la fila existente.");
            } else {
                LineaCompra linea = new LineaCompra(producto, unidades, precio, vencimiento);
                lineasCompra.add(linea);
                modelo.addRow(valoresFila(linea));
            }
            actualizarTotales();
            cantidad.setText("1");
            fechaVencimiento.setText("");
            if (filaExistente < 0) {
                estado.setText("Producto agregado. Filas en la compra: " + lineasCompra.size());
            }
            actualizarEstadoRegistro();
        } catch (NumberFormatException error) {
            mostrarError("Cantidad y costo unitario deben tener un formato numérico válido.", error);
        } catch (DateTimeParseException error) {
            mostrarError("La fecha de vencimiento debe tener el formato AAAA-MM-DD.", error);
        } catch (IllegalArgumentException error) {
            mostrarError(error.getMessage(), error);
        }
    }

    private LocalDate leerFechaVencimiento() {
        String texto = fechaVencimiento.getText().trim();
        return texto.isEmpty() ? null : LocalDate.parse(texto);
    }

    private void eliminarProductoSeleccionado() {
        if (guardando) {
            return;
        }
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            mostrarError("Selecciona una fila de la tabla para eliminarla.", null);
            return;
        }
        int fila = tabla.convertRowIndexToModel(filaVista);
        modelo.removeRow(fila);
        lineasCompra.remove(fila);
        actualizarTotales();
        estado.setText("Producto eliminado. Filas en la compra: " + lineasCompra.size());
        actualizarEstadoRegistro();
    }

    private void actualizarTotales() {
        BigDecimal suma = BigDecimal.ZERO;
        for (LineaCompra linea : lineasCompra) {
            suma = suma.add(linea.subtotal());
        }
        String importe = formatear(suma);
        subtotal.setText(importe);
        total.setText(importe);
    }

    private int buscarLinea(int idProducto, LocalDate vencimiento) {
        return buscarLinea(lineasCompra, idProducto, vencimiento);
    }

    static int buscarLinea(List<LineaCompra> lineas, int idProducto, LocalDate vencimiento) {
        for (int indice = 0; indice < lineas.size(); indice++) {
            LineaCompra linea = lineas.get(indice);
            if (linea.producto.getIdProducto() == idProducto
                    && Objects.equals(linea.fechaVencimiento, vencimiento)) {
                return indice;
            }
        }
        return -1;
    }

    private Object[] valoresFila(LineaCompra linea) {
        return new Object[]{linea.producto.getCodigo(), linea.producto.getDescripcion(),
                linea.cantidad, formatear(linea.costoUnitario),
                linea.fechaVencimiento == null ? "—" : linea.fechaVencimiento.toString(),
                formatear(linea.subtotal())};
    }

    private void actualizarFila(int indice, LineaCompra linea) {
        Object[] valores = valoresFila(linea);
        for (int columna = 0; columna < valores.length; columna++) {
            modelo.setValueAt(valores[columna], indice, columna);
        }
    }

    private void actualizarEstadoRegistro() {
        registrar.setEnabled(!guardando && !lineasCompra.isEmpty()
                && proveedor.getSelectedItem() != null
                && !numeroFactura.getText().trim().isEmpty());
    }

    private void guardarCompra() {
        if (guardando) {
            return;
        }
        if (lineasCompra.isEmpty()) {
            mostrarError("Agrega al menos un producto antes de registrar la entrada.", null);
            return;
        }
        Proveedor proveedorSeleccionado = (Proveedor) proveedor.getSelectedItem();
        if (proveedorSeleccionado == null) {
            mostrarError("Selecciona un proveedor antes de registrar la entrada.", null);
            return;
        }
        String factura = numeroFactura.getText().trim();
        if (factura.isEmpty()) {
            mostrarError("El número de factura es obligatorio.", null);
            return;
        }

        final Compra compra;
        final List<DetalleCompra> detalles = new ArrayList<>();
        final List<Lote> lotes = new ArrayList<>();
        try {
            LocalDate fecha = LocalDate.parse(fechaCompra.getText().trim());
            BigDecimal importe = obtenerImporteTotal();
            compra = new Compra();
            compra.setFechaCompras(fecha);
            compra.setNumeroFactura(factura);
            compra.setSubTotal(importe);
            compra.setTotalCompra(importe);
            compra.setMetodoPago((String) metodoPago.getSelectedItem());
            compra.setEstado("Registrada");
            compra.setObservacion(observacion.getText().trim());
            compra.setIdUsuario(usuarioActivo.getIdUsuario());
            compra.setIdProveedores(proveedorSeleccionado.getIdProveedor());

            for (LineaCompra linea : lineasCompra) {
                DetalleCompra detalle = new DetalleCompra();
                detalle.setCantidad(linea.cantidad);
                detalle.setPrecioDecimal(linea.costoUnitario);
                detalle.setFecha(fecha);
                detalle.setObservacion(observacion.getText().trim());
                detalle.setIdProducto(linea.producto.getIdProducto());
                detalles.add(detalle);

                Lote lote = new Lote();
                lote.setFechasIngreso(fecha);
                lote.setFechaVencimiento(linea.fechaVencimiento);
                lote.setIdProducto(linea.producto.getIdProducto());
                lote.setTotal(linea.subtotal());
                lotes.add(lote);
            }
        } catch (DateTimeParseException error) {
            mostrarError("La fecha de compra debe tener el formato AAAA-MM-DD.", error);
            return;
        }

        guardando = true;
        setCapturaHabilitada(false);
        estado.setText("Registrando compra y actualizando inventario…");
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return compraDao.registrarCompraCompleta(
                        compra, detalles, lotes, ID_TIPO_MOVIMIENTO_ENTRADA);
            }

            @Override
            protected void done() {
                try {
                    int idCompra = get();
                    JOptionPane.showMessageDialog(CompraPanel.this,
                            "La entrada se registró correctamente. Número de compra: " + idCompra,
                            "Compra registrada", JOptionPane.INFORMATION_MESSAGE);
                    limpiarFormulario();
                    cargarProveedores();
                    refrescarDatos.run();
                    estado.setText("Compra registrada. Las búsquedas siguientes mostrarán el stock actualizado.");
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    mostrarError("Se interrumpió el registro de la compra.", error);
                    estado.setText("No se completó el registro.");
                } catch (ExecutionException error) {
                    Throwable causa = causa(error);
                    mostrarError("No se pudo registrar la compra.", causa);
                    estado.setText("No se completó el registro.");
                } finally {
                    guardando = false;
                    setCapturaHabilitada(true);
                }
            }
        }.execute();
    }

    private BigDecimal obtenerImporteTotal() {
        BigDecimal importe = BigDecimal.ZERO;
        for (LineaCompra linea : lineasCompra) {
            importe = importe.add(linea.subtotal());
        }
        return importe;
    }

    private void limpiarFormulario() {
        proveedor.setSelectedIndex(0);
        numeroFactura.setText("");
        metodoPago.setSelectedIndex(0);
        fechaCompra.setText(LocalDate.now().toString());
        observacion.setText("");
        busquedaProducto.setText("");
        productosEncontrados.removeAllItems();
        stockActual.setText("Selecciona un producto");
        cantidad.setText("1");
        costoUnitario.setText("");
        fechaVencimiento.setText("");
        tabla.clearSelection();
        modelo.setRowCount(0);
        lineasCompra.clear();
        actualizarTotales();
        actualizarEstadoRegistro();
    }

    private void setCapturaHabilitada(boolean habilitada) {
        proveedor.setEnabled(habilitada);
        numeroFactura.setEnabled(habilitada);
        metodoPago.setEnabled(habilitada);
        fechaCompra.setEnabled(habilitada);
        observacion.setEnabled(habilitada);
        busquedaProducto.setEnabled(habilitada);
        productosEncontrados.setEnabled(habilitada);
        cantidad.setEnabled(habilitada);
        costoUnitario.setEnabled(habilitada);
        fechaVencimiento.setEnabled(habilitada);
        buscar.setEnabled(habilitada && !buscandoProductos);
        agregar.setEnabled(habilitada);
        eliminar.setEnabled(habilitada);
        tabla.setEnabled(habilitada);
        actualizarEstadoRegistro();
    }

    static void validarCantidadYCosto(int unidades, BigDecimal precio) {
        if (unidades <= 0 || precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad y el costo unitario deben ser mayores que cero.");
        }
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
        campo.setPreferredSize(new Dimension(120, 34));
    }

    private void configurarCombo(JComboBox<?> combo) {
        combo.setBackground(CAMPO);
        combo.setForeground(TEXTO);
        combo.setFont(combo.getFont().deriveFont(13f));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));
    }

    private void configurarStockActual() {
        stockActual.setForeground(new Color(56, 189, 248));
        stockActual.setFont(stockActual.getFont().deriveFont(Font.BOLD, 13f));
        stockActual.setBorder(BorderFactory.createEmptyBorder(7, 2, 7, 2));
    }

    private void estilizarElementoLista(JLabel etiqueta, boolean seleccionado) {
        etiqueta.setBackground(seleccionado ? AZUL_SELECCION : CAMPO);
        etiqueta.setForeground(TEXTO);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
    }

    private void estilizarBoton(JButton boton, Color fondo, Color texto) {
        boton.setOpaque(true);
        boton.setBackground(fondo);
        boton.setForeground(texto);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 12f));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void mostrarError(String mensaje, Throwable error) {
        String detalle = error == null || error.getMessage() == null
                ? "" : "\n" + error.getMessage();
        JOptionPane.showMessageDialog(this, mensaje + detalle,
                "Compras / Entradas", JOptionPane.ERROR_MESSAGE);
    }

    private Throwable causa(ExecutionException error) {
        return error.getCause() == null ? error : error.getCause();
    }

    private String formatear(BigDecimal importe) {
        return "$ " + importe.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static class RenderizadorDetalle extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {
            JLabel etiqueta = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);
            etiqueta.setOpaque(true);
            etiqueta.setForeground(seleccionado ? Color.WHITE : TEXTO);
            etiqueta.setBackground(seleccionado ? AZUL_SELECCION : CAMPO);
            etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            etiqueta.setHorizontalAlignment(columna == 2 || columna == 3 || columna == 5
                    ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return etiqueta;
        }
    }

    private static class RenderizadorEncabezado extends DefaultTableCellRenderer {
        private RenderizadorEncabezado() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {
            JLabel etiqueta = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);
            etiqueta.setOpaque(true);
            etiqueta.setBackground(new Color(39, 54, 75));
            etiqueta.setForeground(TEXTO);
            etiqueta.setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
            etiqueta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, BORDE),
                    BorderFactory.createEmptyBorder(0, 6, 0, 6)));
            etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
            return etiqueta;
        }
    }

    static final class LineaCompra {
        private final Producto producto;
        private int cantidad;
        private BigDecimal costoUnitario;
        private final LocalDate fechaVencimiento;
        private BigDecimal importeTotal;

        LineaCompra(Producto producto, int cantidad, BigDecimal costoUnitario,
                    LocalDate fechaVencimiento) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.costoUnitario = costoUnitario;
            this.fechaVencimiento = fechaVencimiento;
            this.importeTotal = costoUnitario.multiply(BigDecimal.valueOf(cantidad));
        }

        void agregar(int cantidadNueva, BigDecimal costoNuevo) {
            int cantidadActualizada;
            try {
                cantidadActualizada = Math.addExact(cantidad, cantidadNueva);
            } catch (ArithmeticException error) {
                throw new IllegalArgumentException("La cantidad consolidada excede el límite permitido.", error);
            }
            importeTotal = importeTotal.add(costoNuevo.multiply(BigDecimal.valueOf(cantidadNueva)));
            cantidad = cantidadActualizada;
            costoUnitario = importeTotal.divide(BigDecimal.valueOf(cantidad), MathContext.DECIMAL128);
        }

        int cantidad() {
            return cantidad;
        }

        BigDecimal costoUnitario() {
            return costoUnitario;
        }

        BigDecimal subtotalTotal() {
            return importeTotal;
        }

        BigDecimal subtotal() {
            return importeTotal;
        }
    }
}
