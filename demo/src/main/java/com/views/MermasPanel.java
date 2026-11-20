package com.views;

import com.Daos.LoteDao;
import com.model.Lote;
import com.model.Merma;
import com.model.Producto;
import com.model.Usuario;
import com.service.MermaService;
import com.service.ProductoService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/** Captura bajas de productos y mantiene sus líneas en una lista temporal. */
public class MermasPanel extends JPanel {
    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color SUPERFICIE = new Color(30, 41, 59);
    private static final Color CAMPO = new Color(15, 23, 42);
    private static final Color BORDE = new Color(51, 65, 85);
    private static final Color TEXTO = new Color(226, 232, 240);
    private static final Color TEXTO_SUAVE = new Color(148, 163, 184);
    private static final Color AZUL = new Color(59, 130, 246);
    private static final Color VERDE = new Color(34, 197, 142);
    private static final Color ROJO = new Color(153, 53, 65);
    private static final DateTimeFormatter FECHA_TABLA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Usuario usuarioActivo;
    private final MermaService mermaService = new MermaService();
    private final ProductoService productoService = new ProductoService();
    private final LoteDao loteDao = new LoteDao();
    private final JTextField busquedaProducto = new JTextField();
    private final JButton buscar = new JButton("Buscar");
    private final JComboBox<Producto> productosEncontrados = new JComboBox<>();
    private final JLabel stockActual = new JLabel("Busca y selecciona un producto");
    private final JComboBox<Lote> lotesDisponibles = new JComboBox<>();
    private final JComboBox<TipoBaja> tipoBaja = new JComboBox<>(TipoBaja.values());
    private final JTextField fechaRegistro = new JTextField(LocalDate.now().toString());
    private final JTextField observacion = new JTextField();
    private final JTextField cantidad = new JTextField("1");
    private final JLabel estado = new JLabel("No hay bajas agregadas");
    private final JButton agregar = new JButton("Agregar a la lista");
    private final JButton eliminar = new JButton("Eliminar fila");
    private final JButton registrar = new JButton("Registrar merma / baja");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código", "Producto", "Lote / Vencimiento", "Tipo / Motivo", "Cantidad"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final List<LineaTemporal> lineas = new ArrayList<>();
    private boolean cargandoProductos;
    private boolean buscando;
    private boolean guardando;
    private int solicitudLotes;
    private String mensajeLotes = "Selecciona un producto";

    public MermasPanel(Usuario usuarioActivo) {
        if (usuarioActivo == null || usuarioActivo.getIdUsuario() <= 0) {
            throw new IllegalArgumentException("Se requiere un usuario activo con ID válido.");
        }
        this.usuarioActivo = usuarioActivo;
        construirVista();
    }

    private void construirVista() {
        setLayout(new GridBagLayout());
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        GridBagConstraints bloque = new GridBagConstraints();
        bloque.gridx = 0;
        bloque.fill = GridBagConstraints.BOTH;
        bloque.weightx = 1;
        bloque.insets = new Insets(0, 0, 10, 0);

        bloque.gridy = 0;
        bloque.weighty = 0.2;
        add(crearBloqueDatos(), bloque);
        bloque.gridy = 1;
        bloque.weighty = 0.3;
        add(crearBloqueProducto(), bloque);
        bloque.gridy = 2;
        bloque.weighty = 0.5;
        bloque.insets = new Insets(0, 0, 0, 0);
        add(crearBloqueDetalle(), bloque);

        configurarCampos();
        configurarTabla();
        configurarEventos();
    }

    private JPanel crearBloqueDatos() {
        JPanel bloque = crearTarjeta("Datos de la merma / baja");
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        agregarCampo(formulario, "Tipo de baja", tipoBaja, 0, 0, 1);
        agregarCampo(formulario, "Fecha de registro (AAAA-MM-DD)", fechaRegistro, 1, 0, 1);
        agregarCampo(formulario, "Observación o justificación obligatoria", observacion, 2, 0, 2);
        bloque.add(formulario, BorderLayout.CENTER);
        return bloque;
    }

    private JPanel crearBloqueProducto() {
        JPanel bloque = crearTarjeta("Seleccionar producto y lote afectado");
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);

        agregarCampo(formulario, "Buscar por código o nombre", busquedaProducto, 0, 0, 2);
        estilizarBoton(buscar, AZUL, Color.WHITE);
        buscar.addActionListener(e -> buscarProductos());
        agregarComponente(formulario, buscar, 2, 0, 1, 1, 0);
        agregarCampo(formulario, "Producto encontrado", productosEncontrados, 3, 0, 2);
        agregarCampo(formulario, "Stock total actual", stockActual, 5, 0, 1);

        agregarCampo(formulario, "Lote activo (ID / vencimiento / disponible)", lotesDisponibles, 0, 1, 4);
        agregarCampo(formulario, "Cantidad a dar de baja", cantidad, 4, 1, 1);
        estilizarBoton(agregar, VERDE, Color.WHITE);
        agregarComponente(formulario, agregar, 5, 1, 1, 1, 0);
        bloque.add(formulario, BorderLayout.CENTER);
        return bloque;
    }

    private JPanel crearBloqueDetalle() {
        JPanel bloque = crearTarjeta("Detalle y confirmación");
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Bajas por registrar");
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
        estilizarBoton(registrar, AZUL, Color.WHITE);
        registrar.setEnabled(false);
        pie.add(registrar, BorderLayout.EAST);
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

    private void agregarCampo(JPanel panel, String texto, JComponent campo,
                              int columna, int fila, int ancho) {
        JPanel contenedor = new JPanel(new BorderLayout(0, 4));
        contenedor.setOpaque(false);
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(TEXTO);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 11f));
        contenedor.add(etiqueta, BorderLayout.NORTH);
        contenedor.add(campo, BorderLayout.CENTER);
        agregarComponente(panel, contenedor, columna, fila, ancho, 1, ancho);
    }

    private void agregarComponente(JPanel panel, JComponent componente,
                                   int columna, int fila, int ancho, int alto, double peso) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = columna;
        c.gridy = fila;
        c.gridwidth = ancho;
        c.gridheight = alto;
        c.weightx = peso;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(fila == 0 ? 2 : 7, 4, 3, 4);
        panel.add(componente, c);
    }

    private void configurarCampos() {
        fechaRegistro.setToolTipText("Formato AAAA-MM-DD");
        observacion.setToolTipText("Describe el motivo de la baja");
        cantidad.setToolTipText("Ingresa una cantidad entera mayor que cero");
        estiloCampo(tipoBaja);
        estiloCampo(fechaRegistro);
        estiloCampo(observacion);
        estiloCampo(busquedaProducto);
        estiloCampo(productosEncontrados);
        estiloCampo(lotesDisponibles);
        estiloCampo(cantidad);
        stockActual.setForeground(TEXTO_SUAVE);
        stockActual.setFont(stockActual.getFont().deriveFont(Font.BOLD, 13f));

        productosEncontrados.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean seleccionado, boolean enfocado) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, enfocado);
                if (valor instanceof Producto producto) {
                    setText(producto.getCodigo() + " · " + producto.getDescripcion());
                }
                return this;
            }
        });
        lotesDisponibles.setRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean seleccionado, boolean enfocado) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, enfocado);
                if (valor instanceof Lote lote) {
                    if (lote.getIdLote() <= 0) {
                        setText("Sin lote asignado (Stock general)");
                    } else {
                        String vencimiento = lote.getFechaVencimiento() == null
                                ? "Sin fecha" : lote.getFechaVencimiento().format(FECHA_TABLA);
                        setText("#" + lote.getIdLote() + " · vence " + vencimiento
                                + " · disponible " + lote.getCantidadActual());
                    }
                } else setText(mensajeLotes);
                return this;
            }
        });
    }

    private void configurarTabla() {
        tabla.setRowHeight(30);
        tabla.setFont(tabla.getFont().deriveFont(13f));
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(CAMPO);
        tabla.setForeground(TEXTO);
        tabla.setSelectionBackground(new Color(30, 64, 112));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setGridColor(BORDE);
        tabla.setShowGrid(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));
        tabla.setRowMargin(1);
        tabla.setDefaultRenderer(Object.class, new RenderizadorDetalle());
        tabla.getTableHeader().setReorderingAllowed(false);
        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setBackground(new Color(39, 54, 75));
        cabecera.setForeground(TEXTO);
        cabecera.setFont(cabecera.getFont().deriveFont(Font.BOLD, 12f));
        cabecera.setPreferredSize(new Dimension(0, 36));
        cabecera.setDefaultRenderer(new RenderizadorEncabezado());
    }

    private void configurarEventos() {
        productosEncontrados.addActionListener(e -> {
            if (!cargandoProductos) cargarLotesDelSeleccionado();
        });
        agregar.addActionListener(e -> agregarALaLista());
        eliminar.addActionListener(e -> eliminarSeleccionado());
        registrar.addActionListener(e -> registrarMermas());
        busquedaProducto.addActionListener(e -> buscarProductos());
    }

    private void buscarProductos() {
        if (buscando) return;
        String termino = busquedaProducto.getText().trim();
        buscando = true;
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return productoService.buscarPorCodigoONombre(termino);
            }

            @Override
            protected void done() {
                buscando = false;
                try {
                    List<Producto> resultados = get();
                    cargandoProductos = true;
                    productosEncontrados.setModel(new DefaultComboBoxModel<>(
                            resultados.toArray(new Producto[0])));
                    if (resultados.isEmpty()) {
                        cargandoProductos = false;
                        stockActual.setText("Sin resultados");
                        vaciarLotes("No hay lotes");
                        estado.setText("No se encontraron productos");
                    } else {
                        productosEncontrados.setSelectedIndex(0);
                        cargandoProductos = false;
                        cargarLotesDelSeleccionado();
                    }
                } catch (Exception error) {
                    cargandoProductos = false;
                    mostrarError("No se pudieron buscar los productos", error);
                }
            }
        }.execute();
    }

    private void cargarLotesDelSeleccionado() {
        cargarLotesDelSeleccionado(null);
    }

    private void cargarLotesDelSeleccionado(Runnable alCompletar) {
        Producto producto = (Producto) productosEncontrados.getSelectedItem();
        if (producto == null) {
            stockActual.setText("Busca y selecciona un producto");
            vaciarLotes("Selecciona un producto");
            if (alCompletar != null) alCompletar.run();
            return;
        }
        stockActual.setText(String.valueOf(producto.getStockInventario()));
        int solicitudActual = ++solicitudLotes;
        mensajeLotes = "Cargando lotes…";
        lotesDisponibles.removeAllItems();
        lotesDisponibles.addItem(null);
        lotesDisponibles.setSelectedItem(null);
        int idProducto = producto.getIdProducto();
        new SwingWorker<List<Lote>, Void>() {
            @Override
            protected List<Lote> doInBackground() throws Exception {
                return loteDao.listarActivosPorProducto(idProducto);
            }

            @Override
            protected void done() {
                if (solicitudActual != solicitudLotes) {
                    if (alCompletar != null) alCompletar.run();
                    return;
                }
                try {
                    List<Lote> resultados = get();
                    lotesDisponibles.removeAllItems();
                    for (Lote lote : resultados) lotesDisponibles.addItem(lote);
                    if (resultados.isEmpty()) {
                        Producto productoActual = (Producto) productosEncontrados.getSelectedItem();
                        if (productoActual.getStockInventario() > 0) {
                            lotesDisponibles.addItem(new Lote(
                                    0, null, null, productoActual.getStockInventario(),
                                    productoActual.getStockInventario(), "Stock general",
                                    null, productoActual.getIdProducto(), 0));
                            estado.setText("Sin lotes activos; se usará el stock general del producto");
                        } else {
                            mensajeLotes = "No hay lotes activos";
                            lotesDisponibles.addItem(null);
                            estado.setText("El producto no tiene lotes activos ni stock disponible");
                        }
                    } else {
                        estado.setText(resultados.size() + " lote(s) activo(s) disponibles");
                    }
                } catch (Exception error) {
                    vaciarLotes("No se pudieron cargar");
                    mostrarError("No se pudieron cargar los lotes", error);
                } finally {
                    if (alCompletar != null) alCompletar.run();
                }
            }
        }.execute();
    }

    private void vaciarLotes(String texto) {
        solicitudLotes++;
        mensajeLotes = texto;
        lotesDisponibles.removeAllItems();
        lotesDisponibles.addItem(null);
    }

    private void agregarALaLista() {
        Producto producto = (Producto) productosEncontrados.getSelectedItem();
        Lote lote = (Lote) lotesDisponibles.getSelectedItem();
        if (producto == null || lote == null) {
            avisar("Selecciona un producto y un lote activo o la opción de stock general.");
            return;
        }

        final int unidades;
        final LocalDate fecha;
        try {
            unidades = Integer.parseInt(cantidad.getText().trim());
            if (unidades <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            fecha = LocalDate.parse(fechaRegistro.getText().trim());
        } catch (NumberFormatException error) {
            avisar("La cantidad debe ser un entero positivo y válido.");
            return;
        } catch (DateTimeParseException error) {
            avisar("La fecha de registro debe tener el formato AAAA-MM-DD.");
            return;
        } catch (IllegalArgumentException error) {
            avisar(error.getMessage());
            return;
        }

        String nota = observacion.getText().trim();
        if (nota.isEmpty()) {
            avisar("La observación o justificación es obligatoria.");
            observacion.requestFocusInWindow();
            return;
        }
        int cantidadEnBorrador = 0;
        int cantidadProductoEnBorrador = 0;
        for (LineaTemporal linea : lineas) {
            if (linea.merma.getIdProducto() == producto.getIdProducto()) {
                try {
                    cantidadProductoEnBorrador = Math.addExact(
                            cantidadProductoEnBorrador, linea.merma.getCantidad());
                } catch (ArithmeticException error) {
                    avisar("La cantidad acumulada para este producto excede el límite permitido.");
                    return;
                }
            }
            if (lote.getIdLote() > 0 && linea.merma.getIdLote() == lote.getIdLote()) {
                try {
                    cantidadEnBorrador = Math.addExact(cantidadEnBorrador, linea.merma.getCantidad());
                } catch (ArithmeticException error) {
                    avisar("La cantidad acumulada para este lote excede el límite permitido.");
                    return;
                }
            }
        }
        if (lote.getIdLote() > 0) {
            int disponible = lote.getCantidadActual() - cantidadEnBorrador;
            if (unidades > disponible) {
                avisar("La cantidad supera las existencias disponibles del lote ("
                        + Math.max(disponible, 0) + ").");
                return;
            }
        }
        int stockDisponible = producto.getStockInventario() - cantidadProductoEnBorrador;
        if (unidades > stockDisponible) {
            avisar("La cantidad supera el stock disponible del producto ("
                    + Math.max(stockDisponible, 0) + ").");
            return;
        }

        TipoBaja tipo = (TipoBaja) tipoBaja.getSelectedItem();
        if (tipo == null) {
            avisar("Selecciona el tipo de baja.");
            return;
        }
        Merma merma = new Merma(lote.getIdLote(), producto.getIdProducto(), unidades,
                tipo.idTipoMovimiento, nota);
        LineaTemporal linea = new LineaTemporal(
                merma, producto.getCodigo(), producto.getDescripcion(), lote, tipo, fecha);
        lineas.add(linea);
        modelo.addRow(new Object[]{linea.codigo, linea.producto, textoLote(lote),
                tipo.etiqueta + " · " + tipo.motivo, unidades});
        cantidad.setText("1");
        registrar.setEnabled(true);
        estado.setText(lineas.size() + " baja(s) en la lista temporal · " + usuarioActivo.getNombreCompleto().trim());
    }

    private String textoLote(Lote lote) {
        if (lote.getIdLote() <= 0) return "Sin lote asignado (Stock general)";
        String vencimiento = lote.getFechaVencimiento() == null
                ? "Sin fecha" : lote.getFechaVencimiento().format(FECHA_TABLA);
        return "#" + lote.getIdLote() + " / " + vencimiento;
    }

    private void eliminarSeleccionado() {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada < 0) {
            avisar("Selecciona una fila para eliminar.");
            return;
        }
        int fila = tabla.convertRowIndexToModel(filaSeleccionada);
        modelo.removeRow(fila);
        lineas.remove(fila);
        registrar.setEnabled(!lineas.isEmpty());
        estado.setText(lineas.isEmpty()
                ? "No hay bajas agregadas" : lineas.size() + " baja(s) en la lista temporal");
    }

    private void registrarMermas() {
        if (guardando) return;
        if (lineas.isEmpty()) {
            avisar("Agrega al menos una baja a la lista.");
            return;
        }
        String justificacion = observacion.getText().trim();
        if (justificacion.isEmpty()) {
            avisar("La observación o justificación es obligatoria antes de guardar.");
            observacion.requestFocusInWindow();
            return;
        }

        List<Merma> mermas = new ArrayList<>();
        for (LineaTemporal linea : lineas) {
            Merma original = linea.merma;
            mermas.add(new Merma(original.getIdLote(), original.getIdProducto(),
                    original.getCantidad(), original.getIdTipoMovimiento(), justificacion));
        }
        Producto productoSeleccionado = (Producto) productosEncontrados.getSelectedItem();
        int idProductoRefrescar = mermas.get(0).getIdProducto();
        if (productoSeleccionado != null) {
            for (Merma merma : mermas) {
                if (merma.getIdProducto() == productoSeleccionado.getIdProducto()) {
                    idProductoRefrescar = productoSeleccionado.getIdProducto();
                    break;
                }
            }
        }
        final int productoParaRefrescar = idProductoRefrescar;

        guardando = true;
        setControlesHabilitados(false);
        estado.setText("Registrando bajas…");
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return mermaService.registrarMermas(mermas, usuarioActivo);
            }

            @Override
            protected void done() {
                final int movimientos;
                try {
                    movimientos = get();
                } catch (Exception error) {
                    guardando = false;
                    setControlesHabilitados(true);
                    registrar.setEnabled(!lineas.isEmpty());
                    estado.setText("No se pudieron registrar las bajas");
                    mostrarError("No se pudieron registrar las mermas o bajas", error);
                    return;
                }

                guardando = false;
                lineas.clear();
                modelo.setRowCount(0);
                observacion.setText("");
                registrar.setEnabled(false);
                estado.setText("Bajas registradas correctamente");
                JOptionPane.showMessageDialog(MermasPanel.this,
                        "Se registraron correctamente " + movimientos + " movimiento(s) de baja.",
                        "Mermas y bajas", JOptionPane.INFORMATION_MESSAGE);
                recargarProductoYLotes(productoParaRefrescar);
            }
        }.execute();
    }

    private void setControlesHabilitados(boolean habilitados) {
        buscar.setEnabled(habilitados);
        busquedaProducto.setEnabled(habilitados);
        productosEncontrados.setEnabled(habilitados);
        lotesDisponibles.setEnabled(habilitados);
        tipoBaja.setEnabled(habilitados);
        fechaRegistro.setEnabled(habilitados);
        observacion.setEnabled(habilitados);
        cantidad.setEnabled(habilitados);
        agregar.setEnabled(habilitados);
        eliminar.setEnabled(habilitados);
        registrar.setEnabled(habilitados && !guardando && !lineas.isEmpty());
    }

    private void recargarProductoYLotes(int idProducto) {
        new SwingWorker<Producto, Void>() {
            @Override
            protected Producto doInBackground() throws Exception {
                return productoService.obtenerPorId(idProducto);
            }

            @Override
            protected void done() {
                try {
                    Producto productoActualizado = get();
                    if (productoActualizado == null) {
                        throw new SQLException("No se encontró el producto después de registrar la baja.");
                    }
                    cargandoProductos = true;
                    productosEncontrados.setModel(
                            new DefaultComboBoxModel<>(new Producto[]{productoActualizado}));
                    productosEncontrados.setSelectedIndex(0);
                    cargandoProductos = false;
                    cargarLotesDelSeleccionado(() -> setControlesHabilitados(true));
                } catch (Exception error) {
                    cargandoProductos = false;
                    setControlesHabilitados(true);
                    mostrarError("La baja se guardó, pero no se pudo actualizar el producto y sus lotes", error);
                }
            }
        }.execute();
    }

    private void estiloCampo(JComponent campo) {
        campo.setForeground(TEXTO);
        campo.setBackground(CAMPO);
        campo.setFont(campo.getFont().deriveFont(13f));
        if (campo instanceof JTextField campoTexto) {
            campoTexto.setCaretColor(TEXTO);
            campoTexto.setSelectionColor(new Color(30, 64, 112));
            campoTexto.setSelectedTextColor(Color.WHITE);
        }
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(7, 8, 7, 8)));
    }

    private JButton boton(String texto, Color fondo) {
        JButton boton = new JButton(texto);
        estilizarBoton(boton, fondo, Color.WHITE);
        return boton;
    }

    private void estilizarBoton(JButton boton, Color fondo, Color texto) {
        boton.setOpaque(true);
        boton.setForeground(texto);
        boton.setBackground(fondo);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 12f));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    private void avisar(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Mermas y bajas", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String texto, Exception error) {
        Throwable causa = error instanceof ExecutionException && error.getCause() != null
                ? error.getCause() : error;
        String detalle = causa instanceof SQLException || causa instanceof RuntimeException
                ? causa.getMessage() : "Revisa la conexión a la base de datos.";
        JOptionPane.showMessageDialog(this, texto + ":\n" + detalle, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private enum TipoBaja {
        VENCIMIENTO(5, "Vencimiento o caducidad (ID 5)",
                "Baja de producto por vencimiento o caducidad"),
        MERMA_DANO(6, "Merma o daño físico (ID 6)",
                "Ajuste de inventario por merma o daño");

        private final int idTipoMovimiento;
        private final String etiqueta;
        private final String motivo;

        TipoBaja(int idTipoMovimiento, String etiqueta, String motivo) {
            this.idTipoMovimiento = idTipoMovimiento;
            this.etiqueta = etiqueta;
            this.motivo = motivo;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private static final class LineaTemporal {
        private final Merma merma;
        private final String codigo;
        private final String producto;
        private final Lote lote;
        private final TipoBaja tipo;
        private final LocalDate fechaRegistro;

        private LineaTemporal(Merma merma, String codigo, String producto,
                              Lote lote, TipoBaja tipo, LocalDate fechaRegistro) {
            this.merma = merma;
            this.codigo = codigo;
            this.producto = producto;
            this.lote = lote;
            this.tipo = tipo;
            this.fechaRegistro = fechaRegistro;
        }
    }

    private static class RenderizadorDetalle extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {
            JLabel etiqueta = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);
            etiqueta.setOpaque(true);
            etiqueta.setForeground(seleccionado ? Color.WHITE : TEXTO);
            etiqueta.setBackground(seleccionado ? new Color(30, 64, 112) : CAMPO);
            etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            etiqueta.setHorizontalAlignment(columna == 4 ? SwingConstants.RIGHT : SwingConstants.LEFT);
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
}
