package com.views;

import com.model.ConteoFisico;
import com.model.DetalleConteo;
import com.model.Producto;
import com.model.Usuario;
import com.service.ConteoFisicoService;
import com.service.ProductoService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/** Interfaz de captura ciega y revisión de conteos físicos. */
public class ConteoFisicoPanel extends JPanel {
    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color SUPERFICIE = new Color(30, 41, 59);
    private static final Color CAMPO = new Color(15, 23, 42);
    private static final Color BORDE = new Color(51, 65, 85);
    private static final Color TEXTO = new Color(226, 232, 240);
    private static final Color TEXTO_SUAVE = new Color(148, 163, 184);
    private static final Color AZUL = new Color(59, 130, 246);
    private static final Color VERDE = new Color(34, 197, 142);
    private static final Color ROJO = new Color(153, 53, 65);
    private static final Color VERDE_DIFERENCIA = new Color(74, 222, 128);
    private static final Color ROJO_DIFERENCIA = new Color(248, 113, 113);
    private static final DateTimeFormatter FECHA_TABLA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Usuario usuarioActivo;
    private final ConteoFisicoService conteoService = new ConteoFisicoService();
    private final ProductoService productoService = new ProductoService();
    private final JTabbedPane pestañas = new JTabbedPane(JTabbedPane.TOP);
    private final JTextField fechaConteo = new JTextField(LocalDate.now().toString());
    private final JTextField responsable = new JTextField();
    private final JTextField observacionGeneral = new JTextField();
    private final JTextField busquedaProducto = new JTextField();
    private final JComboBox<Producto> productosEncontrados = new JComboBox<>();
    private final JTextField stockFisico = new JTextField("0");
    private final JTextField observacionItem = new JTextField();
    private final JLabel mensajeConteo = new JLabel("Agrega los productos contados para preparar el borrador.");
    private final JLabel mensajeRevision = new JLabel("Selecciona un conteo para consultar sus detalles.");
    private final JButton buscarProductos = new JButton("Buscar");
    private final JButton agregarProducto = new JButton("Agregar producto contado");
    private final JButton eliminarFila = new JButton("Eliminar fila");
    private final JButton finalizarConteo = new JButton("Finalizar y enviar a revisión");
    private final JButton actualizarLista = new JButton("Actualizar lista");
    private final JButton devolverReconteo = new JButton("Devolver a reconteo");
    private final JButton marcarConforme = new JButton("Marcar conforme (Auditor)");
    private final JButton rechazarAjuste = new JButton("Rechazar ajuste");
    private final JButton aprobarAjuste = new JButton("Aprobar y aplicar ajuste al stock (AdminDueño)");
    private final DefaultTableModel modeloBorrador = modelo(
            "Código", "Producto", "Cantidad Contada", "Observación");
    private final DefaultTableModel modeloConteos = modelo(
            "ID", "Fecha", "Responsable", "Productos", "Estado", "Observación");
    private final DefaultTableModel modeloDetalles = modelo(
            "Código", "Producto", "Stock Sistema", "Conteo Físico", "Diferencia", "Observación");
    private final JTable tablaBorrador = new JTable(modeloBorrador);
    private final JTable tablaConteos = new JTable(modeloConteos);
    private final JTable tablaDetalles = new JTable(modeloDetalles);
    private final List<DetalleConteo> borrador = new ArrayList<>();
    private final Map<Long, ConteoFisico> conteosPorId = new HashMap<>();
    private boolean buscando;
    private boolean guardandoConteo;
    private boolean cargandoConteos;
    private boolean procesandoAccion;
    private int solicitudDetalle;

    public ConteoFisicoPanel(Usuario usuarioActivo) {
        if (usuarioActivo == null || usuarioActivo.getIdUsuario() <= 0
                || !"Activo".equalsIgnoreCase(usuarioActivo.getEstado())) {
            throw new IllegalArgumentException("Se requiere un usuario activo con ID válido.");
        }
        this.usuarioActivo = usuarioActivo;
        construirVista();
    }

    private void construirVista() {
        setLayout(new BorderLayout());
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        pestañas.setOpaque(true);
        pestañas.setBackground(FONDO);
        pestañas.setForeground(TEXTO);
        pestañas.setFont(pestañas.getFont().deriveFont(Font.BOLD, 13f));
        pestañas.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabInsets = new Insets(10, 16, 10, 16);
                contentBorderInsets = new Insets(1, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(java.awt.Graphics graphics, int placement,
                                              int index, int x, int y, int width, int height,
                                              boolean selected) {
                graphics.setColor(selected ? AZUL : SUPERFICIE);
                graphics.fillRect(x, y, width, height);
            }

            @Override
            protected void paintTabBorder(java.awt.Graphics graphics, int placement,
                                          int index, int x, int y, int width, int height,
                                          boolean selected) {
                graphics.setColor(BORDE);
                graphics.drawRect(x, y, width, height);
            }

            @Override
            protected void paintContentBorder(java.awt.Graphics graphics, int placement,
                                              int selectedIndex) {
                graphics.setColor(BORDE);
                int x = 0;
                int y = calculateTabAreaHeight(placement, runCount, maxTabHeight);
                graphics.drawRect(x, y, tabPane.getWidth() - 1, tabPane.getHeight() - y - 1);
            }
        });

        pestañas.addTab("Nuevo Conteo Físico (Almacenista)", crearPestanaConteo());
        pestañas.addTab("Revisión y Aprobación (Auditor / AdminDueño)", crearPestanaRevision());
        add(pestañas, BorderLayout.CENTER);

        responsable.setText(usuarioActivo.getNombreCompleto().trim());
        responsable.setEditable(false);
        estilizarCampo(fechaConteo);
        estilizarCampo(responsable);
        estilizarCampo(observacionGeneral);
        estilizarCampo(busquedaProducto);
        estilizarCampo(productosEncontrados);
        estilizarCampo(stockFisico);
        estilizarCampo(observacionItem);
        ((AbstractDocument) stockFisico.getDocument()).setDocumentFilter(new FiltroDecimalNoNegativo());
        configurarRenderizadorProducto();
        configurarEventos();
        configurarTablas();
    }

    private JPanel crearPestanaConteo() {
        JPanel pestaña = new JPanel(new BorderLayout(8, 8));
        pestaña.setBackground(FONDO);
        pestaña.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));

        JPanel datos = tarjeta("Datos del conteo");
        JPanel filaDatos = new JPanel(new GridLayout(1, 3, 10, 0));
        filaDatos.setOpaque(false);
        filaDatos.add(campo("Fecha de conteo (AAAA-MM-DD)", fechaConteo));
        filaDatos.add(campo("Responsable activo", responsable));
        filaDatos.add(campo("Observación general", observacionGeneral));
        datos.add(filaDatos, BorderLayout.CENTER);

        JPanel captura = tarjeta("Captura ciega · Ingresa la cantidad física contada");
        JPanel formulario = new JPanel(new GridLayout(2, 1, 8, 6));
        formulario.setOpaque(false);
        JPanel filaBusqueda = new JPanel(new BorderLayout(8, 0));
        filaBusqueda.setOpaque(false);
        filaBusqueda.add(busquedaProducto, BorderLayout.CENTER);
        estilizarBoton(buscarProductos, AZUL);
        filaBusqueda.add(buscarProductos, BorderLayout.EAST);
        formulario.add(campo("Buscar producto por código o nombre (sin tildes)", filaBusqueda));

        JPanel filaCaptura = new JPanel(new GridLayout(1, 4, 8, 0));
        filaCaptura.setOpaque(false);
        filaCaptura.add(campo("Producto encontrado", productosEncontrados));
        filaCaptura.add(campo("Conteo físico (cantidad real)", stockFisico));
        filaCaptura.add(campo("Observación por ítem (opcional)", observacionItem));
        estilizarBoton(agregarProducto, VERDE);
        filaCaptura.add(agregarProducto);
        formulario.add(filaCaptura);
        captura.add(formulario, BorderLayout.CENTER);

        JPanel detalle = tarjeta("Productos contados");
        JPanel encabezadoDetalle = new JPanel(new BorderLayout(8, 0));
        encabezadoDetalle.setOpaque(false);
        JLabel titulo = etiqueta("Borrador del conteo");
        encabezadoDetalle.add(titulo, BorderLayout.WEST);
        estilizarBoton(eliminarFila, ROJO);
        encabezadoDetalle.add(eliminarFila, BorderLayout.EAST);
        detalle.add(encabezadoDetalle, BorderLayout.NORTH);
        detalle.add(estilizarTabla(tablaBorrador), BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(8, 0));
        pie.setOpaque(false);
        mensajeConteo.setForeground(TEXTO_SUAVE);
        pie.add(mensajeConteo, BorderLayout.CENTER);
        estilizarBoton(finalizarConteo, AZUL);
        pie.add(finalizarConteo, BorderLayout.EAST);
        detalle.add(pie, BorderLayout.SOUTH);

        JSplitPane superior = new JSplitPane(JSplitPane.VERTICAL_SPLIT, datos, captura);
        superior.setResizeWeight(0.35);
        superior.setDividerLocation(110);
        superior.setBorder(null);
        superior.setOpaque(false);
        JSplitPane principal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, superior, detalle);
        principal.setResizeWeight(0.45);
        principal.setDividerLocation(280);
        principal.setBorder(null);
        principal.setOpaque(false);
        pestaña.add(principal, BorderLayout.CENTER);
        return pestaña;
    }

    private JPanel crearPestanaRevision() {
        JPanel pestaña = new JPanel(new BorderLayout(8, 8));
        pestaña.setBackground(FONDO);
        pestaña.setBorder(BorderFactory.createEmptyBorder(10, 4, 4, 4));

        JPanel lista = tarjeta("Conteos físicos registrados");
        JPanel encabezado = new JPanel(new BorderLayout(8, 0));
        encabezado.setOpaque(false);
        encabezado.add(etiqueta("Selecciona un conteo para revisar sus cantidades"), BorderLayout.CENTER);
        estilizarBoton(actualizarLista, AZUL);
        encabezado.add(actualizarLista, BorderLayout.EAST);
        lista.add(encabezado, BorderLayout.NORTH);
        lista.add(estilizarTabla(tablaConteos), BorderLayout.CENTER);

        JPanel detalle = tarjeta("Comparación de stock del sistema y conteo físico");
        detalle.add(estilizarTabla(tablaDetalles), BorderLayout.CENTER);
        mensajeRevision.setForeground(TEXTO_SUAVE);
        detalle.add(mensajeRevision, BorderLayout.SOUTH);

        JSplitPane tablas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lista, detalle);
        tablas.setResizeWeight(0.43);
        tablas.setDividerLocation(240);
        tablas.setBorder(null);
        tablas.setOpaque(false);

        JPanel acciones = new JPanel(new BorderLayout(8, 6));
        acciones.setOpaque(false);
        JPanel grupoAuditor = new JPanel(new GridLayout(1, 2, 8, 0));
        grupoAuditor.setOpaque(false);
        estilizarBoton(devolverReconteo, ROJO);
        estilizarBoton(marcarConforme, VERDE);
        grupoAuditor.add(devolverReconteo);
        grupoAuditor.add(marcarConforme);
        JPanel grupoAdministrador = new JPanel(new GridLayout(1, 2, 8, 0));
        grupoAdministrador.setOpaque(false);
        estilizarBoton(rechazarAjuste, ROJO);
        estilizarBoton(aprobarAjuste, AZUL);
        grupoAdministrador.add(rechazarAjuste);
        grupoAdministrador.add(aprobarAjuste);
        acciones.add(grupoAuditor, BorderLayout.NORTH);
        acciones.add(grupoAdministrador, BorderLayout.SOUTH);

        pestaña.add(tablas, BorderLayout.CENTER);
        pestaña.add(acciones, BorderLayout.SOUTH);
        return pestaña;
    }

    private void configurarRenderizadorProducto() {
        productosEncontrados.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean seleccionado, boolean enfocado) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, enfocado);
                if (valor instanceof Producto producto) {
                    setText(producto.getCodigo() + " · " + producto.getDescripcion());
                }
                return this;
            }
        });
    }

    private void configurarEventos() {
        buscarProductos.addActionListener(e -> buscarProductos());
        busquedaProducto.addActionListener(e -> buscarProductos());
        agregarProducto.addActionListener(e -> agregarProductoAlBorrador());
        eliminarFila.addActionListener(e -> eliminarFilaSeleccionada());
        finalizarConteo.addActionListener(e -> finalizarConteo());
        actualizarLista.addActionListener(e -> cargarConteosDesdeServicio());
        devolverReconteo.addActionListener(e -> devolverAReconteo());
        marcarConforme.addActionListener(e -> marcarConteoConforme());
        rechazarAjuste.addActionListener(e -> rechazarConteo());
        aprobarAjuste.addActionListener(e -> aprobarConteo());
        tablaConteos.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) mostrarDetallesSeleccionados();
        });
        pestañas.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent evento) {
                if (pestañas.getSelectedIndex() == 1) cargarConteosDesdeServicio();
            }
        });
    }

    private void configurarTablas() {
        estilizarTablaBase(tablaBorrador);
        estilizarTablaBase(tablaConteos);
        estilizarTablaBase(tablaDetalles);
        tablaDetalles.setDefaultRenderer(Object.class, new RenderizadorDiferencia());
        tablaDetalles.setDefaultRenderer(BigDecimal.class, new RenderizadorDiferencia());
        tablaDetalles.getColumnModel().getColumn(2).setCellRenderer(new RenderizadorDecimal());
        tablaDetalles.getColumnModel().getColumn(3).setCellRenderer(new RenderizadorDecimal());
        tablaDetalles.getColumnModel().getColumn(4).setCellRenderer(new RenderizadorDiferencia());
        tablaBorrador.setPreferredScrollableViewportSize(new Dimension(700, 150));
        actualizarBotonesSegunEstado();
    }

    private void buscarProductos() {
        if (buscando) return;
        String termino = busquedaProducto.getText().trim();
        if (termino.isEmpty()) {
            avisar("Escribe un código o nombre de producto para buscar.");
            busquedaProducto.requestFocusInWindow();
            return;
        }
        buscando = true;
        buscarProductos.setEnabled(false);
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return productoService.buscarPorCodigoONombre(termino);
            }

            @Override
            protected void done() {
                buscando = false;
                buscarProductos.setEnabled(!guardandoConteo);
                try {
                    List<Producto> resultados = get();
                    productosEncontrados.setModel(new DefaultComboBoxModel<>(
                            resultados.toArray(new Producto[0])));
                    if (resultados.isEmpty()) {
                        mensajeConteo.setText("No se encontraron productos.");
                    } else {
                        productosEncontrados.setSelectedIndex(0);
                        mensajeConteo.setText(resultados.size() + " producto(s) encontrado(s).");
                    }
                } catch (Exception error) {
                    mostrarError("No se pudieron buscar los productos", error);
                }
            }
        }.execute();
    }

    private void finalizarConteo() {
        if (guardandoConteo) return;
        if (modeloBorrador.getRowCount() == 0) {
            avisar("Agrega al menos un producto contado antes de enviar el conteo.");
            return;
        }
        String notaGeneral = observacionGeneral.getText().trim();
        if (notaGeneral.isEmpty()) {
            avisar("Escribe una observación general para el conteo.");
            observacionGeneral.requestFocusInWindow();
            return;
        }

        final LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaConteo.getText().trim());
        } catch (DateTimeParseException error) {
            avisar("La fecha debe tener el formato AAAA-MM-DD.");
            fechaConteo.requestFocusInWindow();
            return;
        }

        ConteoFisico conteo = new ConteoFisico();
        conteo.setFechaConteo(fecha);
        conteo.setObservacion(notaGeneral);
        conteo.setDetalles(copiarBorrador());
        guardandoConteo = true;
        setFormularioHabilitado(false);
        mensajeConteo.setText("Enviando el conteo a revisión…");

        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                return conteoService.registrarConteoFisico(conteo, usuarioActivo);
            }

            @Override
            protected void done() {
                guardandoConteo = false;
                setFormularioHabilitado(true);
                try {
                    long idConteo = get();
                    if ("Requiere reconteo".equalsIgnoreCase(conteo.getEstado())) {
                        JOptionPane.showMessageDialog(ConteoFisicoPanel.this,
                                "El conteo #" + idConteo + " superó el umbral permitido y quedó "
                                + "marcado para reconteo y revisión. El stock aún no se ha modificado.",
                                "Requiere reconteo", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ConteoFisicoPanel.this,
                                "El conteo #" + idConteo + " fue enviado a revisión. "
                                + "El stock aún no se ha modificado.",
                                "Conteo enviado", JOptionPane.INFORMATION_MESSAGE);
                    }
                    limpiarFormulario();
                } catch (Exception error) {
                    mensajeConteo.setText("No se pudo enviar el conteo.");
                    mostrarError("No se pudo registrar el conteo físico", error);
                }
            }
        }.execute();
    }

    private List<DetalleConteo> copiarBorrador() {
        List<DetalleConteo> detalles = new ArrayList<>();
        for (DetalleConteo origen : borrador) {
            DetalleConteo copia = new DetalleConteo();
            copia.setIdProducto(origen.getIdProducto());
            copia.setCodigoProducto(origen.getCodigoProducto());
            copia.setNombreProducto(origen.getNombreProducto());
            copia.setStockFisico(origen.getStockFisico());
            copia.setObservacion(origen.getObservacion());
            detalles.add(copia);
        }
        return detalles;
    }

    private void limpiarFormulario() {
        borrador.clear();
        modeloBorrador.setRowCount(0);
        fechaConteo.setText(LocalDate.now().toString());
        observacionGeneral.setText("");
        busquedaProducto.setText("");
        productosEncontrados.setModel(new DefaultComboBoxModel<>());
        stockFisico.setText("0");
        observacionItem.setText("");
        mensajeConteo.setText("Agrega los productos contados para preparar el borrador.");
    }

    private void setFormularioHabilitado(boolean habilitado) {
        fechaConteo.setEnabled(habilitado);
        observacionGeneral.setEnabled(habilitado);
        busquedaProducto.setEnabled(habilitado);
        buscarProductos.setEnabled(habilitado && !buscando);
        productosEncontrados.setEnabled(habilitado);
        stockFisico.setEnabled(habilitado);
        observacionItem.setEnabled(habilitado);
        agregarProducto.setEnabled(habilitado);
        eliminarFila.setEnabled(habilitado);
        finalizarConteo.setEnabled(habilitado);
    }

    private void agregarProductoAlBorrador() {
        Producto producto = (Producto) productosEncontrados.getSelectedItem();
        if (producto == null) {
            avisar("Busca y selecciona un producto antes de agregarlo.");
            return;
        }

        final BigDecimal cantidad;
        try {
            String textoCantidad = stockFisico.getText().trim().replace(',', '.');
            if (textoCantidad.isEmpty() || ".".equals(textoCantidad)) {
                throw new NumberFormatException();
            }
            cantidad = new BigDecimal(textoCantidad);
            if (cantidad.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("La cantidad contada no puede ser negativa.");
            }
        } catch (NumberFormatException error) {
            avisar("Ingresa una cantidad física numérica válida.");
            stockFisico.requestFocusInWindow();
            return;
        } catch (IllegalArgumentException error) {
            avisar(error.getMessage());
            stockFisico.requestFocusInWindow();
            return;
        }

        for (DetalleConteo detalle : borrador) {
            if (detalle.getIdProducto() == producto.getIdProducto()) {
                avisar("Este producto ya está en el borrador. Elimina la fila para volver a agregarlo.");
                return;
            }
        }

        DetalleConteo detalle = new DetalleConteo();
        detalle.setIdProducto(producto.getIdProducto());
        detalle.setCodigoProducto(producto.getCodigo());
        detalle.setNombreProducto(producto.getDescripcion());
        detalle.setStockFisico(cantidad);
        detalle.setObservacion(observacionItem.getText().trim());
        borrador.add(detalle);
        refrescarBorrador();
        mensajeConteo.setText(borrador.size() + " producto(s) en el borrador.");
        stockFisico.setText("0");
        observacionItem.setText("");
    }

    private void eliminarFilaSeleccionada() {
        int filaVista = tablaBorrador.getSelectedRow();
        if (filaVista < 0) {
            avisar("Selecciona una fila del borrador para eliminarla.");
            return;
        }
        int filaModelo = tablaBorrador.convertRowIndexToModel(filaVista);
        borrador.remove(filaModelo);
        refrescarBorrador();
        mensajeConteo.setText(borrador.isEmpty()
                ? "Agrega los productos contados para preparar el borrador."
                : borrador.size() + " producto(s) en el borrador.");
    }

    private void refrescarBorrador() {
        modeloBorrador.setRowCount(0);
        for (DetalleConteo detalle : borrador) {
            modeloBorrador.addRow(new Object[]{
                detalle.getCodigoProducto(),
                detalle.getNombreProducto(),
                detalle.getStockFisico().stripTrailingZeros().toPlainString(),
                detalle.getObservacion()
            });
        }
    }

    private void mostrarDetallesSeleccionados() {
        long idConteo = idConteoSeleccionado();
        actualizarBotonesSegunEstado();
        if (idConteo <= 0) {
            solicitudDetalle++;
            mostrarDetallesEnTabla(null);
            mensajeRevision.setText("Selecciona un conteo para consultar sus detalles.");
            return;
        }
        mensajeRevision.setText("Cargando detalles del conteo #" + idConteo + "…");
        int solicitudActual = ++solicitudDetalle;
        new SwingWorker<List<DetalleConteo>, Void>() {
            @Override
            protected List<DetalleConteo> doInBackground() throws Exception {
                return conteoService.listarDetallesConteo(idConteo);
            }

            @Override
            protected void done() {
                if (solicitudActual != solicitudDetalle || idConteo != idConteoSeleccionado()) return;
                try {
                    List<DetalleConteo> detalles = get();
                    ConteoFisico conteo = conteosPorId.get(idConteo);
                    if (conteo != null) conteo.setDetalles(detalles);
                    cargarDetallesSeleccionados(detalles);
                } catch (Exception error) {
                    mostrarDetallesEnTabla(null);
                    mensajeRevision.setText("No se pudieron cargar los detalles.");
                    mostrarError("No se pudieron cargar los detalles del conteo", error);
                }
            }
        }.execute();
    }

    private void mostrarDetallesEnTabla(List<DetalleConteo> detalles) {
        modeloDetalles.setRowCount(0);
        if (detalles == null) return;
        for (DetalleConteo detalle : detalles) {
            modeloDetalles.addRow(new Object[]{
                detalle.getCodigoProducto(),
                detalle.getNombreProducto(),
                detalle.getStockSistema(),
                detalle.getStockFisico(),
                detalle.getDiferencia(),
                detalle.getObservacion()
            });
        }
    }

    /** Carga los conteos devueltos por la capa de servicio en la tabla superior. */
    public void cargarConteos(List<ConteoFisico> conteos) {
        long seleccionAnterior = idConteoSeleccionado();
        modeloConteos.setRowCount(0);
        conteosPorId.clear();
        if (conteos != null) {
            for (ConteoFisico conteo : conteos) {
                if (conteo == null) continue;
                conteosPorId.put(conteo.getIdConteo(), conteo);
                modeloConteos.addRow(new Object[]{
                    conteo.getIdConteo(),
                    conteo.getFechaConteo() == null ? "" : conteo.getFechaConteo().format(FECHA_TABLA),
                    conteo.getNombreResponsable(),
                    conteo.getProductosResumen(),
                    conteo.getEstado(),
                    conteo.getObservacion()
                });
            }
        }
        if (modeloConteos.getRowCount() > 0) {
            int filaSeleccionada = 0;
            for (int fila = 0; fila < modeloConteos.getRowCount(); fila++) {
                if (((Number) modeloConteos.getValueAt(fila, 0)).longValue() == seleccionAnterior) {
                    filaSeleccionada = fila;
                    break;
                }
            }
            tablaConteos.setRowSelectionInterval(filaSeleccionada, filaSeleccionada);
        } else {
            solicitudDetalle++;
            mostrarDetallesEnTabla(null);
            mensajeRevision.setText("No hay conteos registrados.");
        }
        actualizarBotonesSegunEstado();
    }

    /** Actualiza la tabla inferior cuando se obtienen detalles para la selección actual. */
    public void cargarDetallesSeleccionados(List<DetalleConteo> detalles) {
        mostrarDetallesEnTabla(detalles);
        mensajeRevision.setText(detalles == null || detalles.isEmpty()
                ? "El conteo seleccionado no tiene detalles."
                : detalles.size() + " detalle(s) del conteo seleccionado.");
    }

    private void cargarConteosDesdeServicio() {
        if (cargandoConteos) return;
        cargandoConteos = true;
        actualizarLista.setEnabled(false);
        actualizarBotonesSegunEstado();
        mensajeRevision.setText("Cargando conteos registrados…");
        new SwingWorker<List<ConteoFisico>, Void>() {
            @Override
            protected List<ConteoFisico> doInBackground() throws Exception {
                return conteoService.listarConteos();
            }

            @Override
            protected void done() {
                cargandoConteos = false;
                actualizarLista.setEnabled(true);
                actualizarBotonesSegunEstado();
                try {
                    cargarConteos(get());
                } catch (Exception error) {
                    mensajeRevision.setText("No se pudieron cargar los conteos.");
                    mostrarError("No se pudieron cargar los conteos físicos", error);
                }
            }
        }.execute();
    }

    private void devolverAReconteo() {
        long idConteo = idConteoSeleccionado();
        if (idConteo <= 0) {
            avisar("Selecciona un conteo para devolverlo a reconteo.");
            return;
        }
        String observacion = JOptionPane.showInputDialog(this,
                "Indica el motivo para devolver el conteo a reconteo:",
                "Devolver a reconteo", JOptionPane.QUESTION_MESSAGE);
        if (observacion == null) return;
        if (observacion.isBlank()) {
            avisar("La observación del auditor es obligatoria.");
            return;
        }
        ejecutarAccionConteo("El conteo fue devuelto para reconteo.",
                () -> conteoService.revisarConteoAuditor(
                        idConteo, false, observacion.trim(), usuarioActivo));
    }

    private void marcarConteoConforme() {
        long idConteo = idConteoSeleccionado();
        if (idConteo <= 0) {
            avisar("Selecciona un conteo para marcarlo conforme.");
            return;
        }
        ejecutarAccionConteo("El conteo quedó marcado como revisado.",
                () -> conteoService.revisarConteoAuditor(
                        idConteo, true, null, usuarioActivo));
    }

    private void rechazarConteo() {
        long idConteo = idConteoSeleccionado();
        if (idConteo <= 0) {
            avisar("Selecciona un conteo para rechazarlo.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Rechazar el ajuste del conteo #" + idConteo + "? No se modificará el inventario.",
                "Confirmar rechazo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) return;
        ejecutarAccionConteo("El ajuste fue rechazado; el inventario no se modificó.",
                () -> conteoService.aprobarYAplicarAjuste(idConteo, false, usuarioActivo));
    }

    private void aprobarConteo() {
        long idConteo = idConteoSeleccionado();
        if (idConteo <= 0) {
            avisar("Selecciona un conteo para aprobarlo.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Aprobar y aplicar el ajuste del conteo #" + idConteo + " al inventario?",
                "Confirmar ajuste de stock", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) return;
        ejecutarAccionConteo("El ajuste fue aprobado y aplicado al inventario.",
                () -> conteoService.aprobarYAplicarAjuste(idConteo, true, usuarioActivo));
    }

    private void ejecutarAccionConteo(String mensajeExito, AccionConteo accion) {
        procesandoAccion = true;
        tablaConteos.setEnabled(false);
        actualizarBotonesSegunEstado();
        mensajeRevision.setText("Procesando acción…");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                accion.ejecutar();
                return null;
            }

            @Override
            protected void done() {
                procesandoAccion = false;
                tablaConteos.setEnabled(true);
                actualizarBotonesSegunEstado();
                try {
                    get();
                    JOptionPane.showMessageDialog(ConteoFisicoPanel.this, mensajeExito,
                            "Conteo físico", JOptionPane.INFORMATION_MESSAGE);
                    cargarConteosDesdeServicio();
                } catch (Exception error) {
                    mensajeRevision.setText("No se pudo completar la acción.");
                    mostrarError("No se pudo actualizar el conteo físico", error);
                }
            }
        }.execute();
    }

    private void actualizarBotonesSegunEstado() {
        String estado = estadoSeleccionado();
        boolean habilitado = !procesandoAccion && !cargandoConteos;
        boolean pendienteRevision = "Pendiente de revisión".equalsIgnoreCase(estado)
                || "Requiere reconteo".equalsIgnoreCase(estado)
                || "Devuelto para reconteo".equalsIgnoreCase(estado);
        boolean revisado = "Revisado".equalsIgnoreCase(estado);
        devolverReconteo.setEnabled(pendienteRevision && habilitado);
        marcarConforme.setEnabled(pendienteRevision && habilitado);
        rechazarAjuste.setEnabled(revisado && habilitado);
        aprobarAjuste.setEnabled(revisado && habilitado);
    }

    private String estadoSeleccionado() {
        int fila = tablaConteos.getSelectedRow();
        if (fila < 0) return "";
        int filaModelo = tablaConteos.convertRowIndexToModel(fila);
        Object estado = modeloConteos.getValueAt(filaModelo, 3);
        return estado == null ? "" : estado.toString();
    }

    private long idConteoSeleccionado() {
        int fila = tablaConteos.getSelectedRow();
        if (fila < 0) return -1;
        int filaModelo = tablaConteos.convertRowIndexToModel(fila);
        Object id = modeloConteos.getValueAt(filaModelo, 0);
        return id instanceof Number numero ? numero.longValue() : -1;
    }

    @FunctionalInterface
    private interface AccionConteo {
        void ejecutar() throws Exception;
    }

    private JPanel tarjeta(String titulo) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(SUPERFICIE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        panel.add(etiqueta(titulo), BorderLayout.NORTH);
        return panel;
    }

    private JLabel etiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(TEXTO);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 13f));
        return etiqueta;
    }

    private JPanel campo(String texto, JComponent componente) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(TEXTO);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 11f));
        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(componente, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane estilizarTabla(JTable tabla) {
        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.getViewport().setBackground(CAMPO);
        desplazamiento.setBorder(BorderFactory.createLineBorder(BORDE));
        desplazamiento.getVerticalScrollBar().setUnitIncrement(16);
        return desplazamiento;
    }

    private void estilizarTablaBase(JTable tabla) {
        tabla.setRowHeight(28);
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
        tabla.setDefaultRenderer(Object.class, new RenderizadorTabla(SwingConstants.LEFT));
        tabla.getTableHeader().setReorderingAllowed(false);
        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setBackground(new Color(39, 54, 75));
        cabecera.setForeground(TEXTO);
        cabecera.setFont(cabecera.getFont().deriveFont(Font.BOLD, 12f));
        cabecera.setPreferredSize(new Dimension(0, 36));
        cabecera.setDefaultRenderer(new RenderizadorEncabezado());
    }

    private void estilizarCampo(JComponent campo) {
        campo.setForeground(TEXTO);
        campo.setBackground(CAMPO);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));
        if (campo instanceof JTextField campoTexto) {
            campoTexto.setCaretColor(Color.WHITE);
        }
    }

    private void estilizarBoton(JButton boton, Color fondo) {
        boton.setForeground(Color.WHITE);
        boton.setBackground(fondo);
        boton.setFocusPainted(false);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 12f));
    }

    private void avisar(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Conteo físico", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String texto, Exception error) {
        Throwable causa = error instanceof ExecutionException && error.getCause() != null
                ? error.getCause() : error;
        String detalle = causa instanceof SQLException || causa instanceof RuntimeException
                ? causa.getMessage() : "Revisa la conexión a la base de datos.";
        JOptionPane.showMessageDialog(this, texto + ":\n" + detalle, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private static final class FiltroDecimalNoNegativo extends DocumentFilter {
        @Override
        public void insertString(FilterBypass bypass, int offset, String texto,
                                 AttributeSet atributos) throws BadLocationException {
            replace(bypass, offset, 0, texto, atributos);
        }

        @Override
        public void replace(FilterBypass bypass, int offset, int longitud, String texto,
                            AttributeSet atributos) throws BadLocationException {
            String actual = bypass.getDocument().getText(0, bypass.getDocument().getLength());
            String nuevo = actual.substring(0, offset) + (texto == null ? "" : texto)
                    + actual.substring(offset + longitud);
            if (nuevo.matches("\\d*(?:[.,]\\d*)?")) {
                bypass.replace(offset, longitud, texto, atributos);
            }
        }
    }

    private static class RenderizadorTabla extends DefaultTableCellRenderer {
        private final int alineacion;

        private RenderizadorTabla(int alineacion) {
            this.alineacion = alineacion;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {
            JLabel celda = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);
            celda.setOpaque(true);
            celda.setForeground(seleccionado ? Color.WHITE : TEXTO);
            celda.setBackground(seleccionado ? new Color(30, 64, 112) : CAMPO);
            celda.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            celda.setHorizontalAlignment(alineacion);
            return celda;
        }
    }

    private static final class RenderizadorDiferencia extends RenderizadorTabla {
        private RenderizadorDiferencia() {
            super(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {
            JLabel celda = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);
            if (valor instanceof BigDecimal numero) {
                celda.setText(numero.stripTrailingZeros().toPlainString());
            }
            if (!seleccionado && columna == 4 && valor instanceof BigDecimal diferencia) {
                if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
                    celda.setForeground(ROJO_DIFERENCIA);
                } else if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
                    celda.setForeground(VERDE_DIFERENCIA);
                }
            }
            return celda;
        }
    }

    private static final class RenderizadorDecimal extends RenderizadorTabla {
        private RenderizadorDecimal() {
            super(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {
            JLabel celda = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);
            if (valor instanceof BigDecimal numero) {
                celda.setText(numero.stripTrailingZeros().toPlainString());
            }
            return celda;
        }
    }

    private static final class RenderizadorEncabezado extends DefaultTableCellRenderer {
        private RenderizadorEncabezado() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionado, boolean tieneFoco, int fila, int columna) {
            JLabel celda = (JLabel) super.getTableCellRendererComponent(
                    tabla, valor, seleccionado, tieneFoco, fila, columna);
            celda.setOpaque(true);
            celda.setBackground(new Color(39, 54, 75));
            celda.setForeground(TEXTO);
            celda.setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
            celda.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, BORDE),
                    BorderFactory.createEmptyBorder(0, 6, 0, 6)));
            celda.setHorizontalAlignment(SwingConstants.CENTER);
            return celda;
        }
    }
}
