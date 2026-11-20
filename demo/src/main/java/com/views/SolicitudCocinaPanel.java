package com.views;

import com.model.DetalleSolicitudCocina;
import com.model.Producto;
import com.model.SolicitudCocina;
import com.model.Usuario;
import com.service.ProductoService;
import com.service.SolicitudCocinaService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Captura solicitudes de cocina y permite despachar/rechazar solicitudes pendientes. */
public class SolicitudCocinaPanel extends JPanel {
    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color SUPERFICIE = new Color(30, 41, 59);
    private static final Color CAMPO = new Color(15, 23, 42);
    private static final Color BORDE = new Color(51, 65, 85);
    private static final Color TEXTO = new Color(226, 232, 240);
    private static final Color SUAVE = new Color(148, 163, 184);
    private final Usuario usuario;
    private final Runnable refrescarInventario;
    private final SolicitudCocinaService servicio = new SolicitudCocinaService();
    private final ProductoService productosService = new ProductoService();
    private final JComboBox<Producto> productos = new JComboBox<>();
    private final JTextField cantidad = new JTextField("1", 5);
    private final JTextField observacion = new JTextField();
    private final JLabel mensaje = new JLabel("Cargando productos y solicitudes…");
    private final DefaultTableModel modeloBorrador = modelo("Código", "Producto", "Cantidad");
    private final DefaultTableModel modeloSolicitudes = modelo("ID", "Fecha", "Solicitante", "Estado", "Nota");
    private final DefaultTableModel modeloDetalles = modelo("Código", "Producto", "Solicitado", "Entregado", "Stock", "Pendiente");
    private final JTable tablaBorrador = new JTable(modeloBorrador);
    private final JTable tablaSolicitudes = new JTable(modeloSolicitudes);
    private final JTable tablaDetalles = new JTable(modeloDetalles);
    private final List<DetalleSolicitudCocina> borrador = new ArrayList<>();
    private boolean guardando;

    public SolicitudCocinaPanel(Usuario usuario, Runnable refrescarInventario) {
        if (usuario == null || usuario.getIdUsuario() <= 0) throw new IllegalArgumentException("Se requiere un usuario activo.");
        this.usuario = usuario;
        this.refrescarInventario = refrescarInventario;
        construirVista();
        cargarProductos();
        recargarSolicitudes();
    }

    private void construirVista() {
        setLayout(new BorderLayout(10, 10));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        JPanel nueva = tarjeta("Nueva solicitud · Los productos se descuentan al registrar la entrega");
        JPanel acciones = new JPanel(new BorderLayout(8, 8));
        acciones.setOpaque(false);
        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setOpaque(false);
        productos.setPreferredSize(new Dimension(240, 34));
        productos.setRenderer(new DefaultListCellRenderer() {
            @Override public java.awt.Component getListCellRendererComponent(JList<?> lista, Object valor,
                    int indice, boolean seleccionado, boolean enfocado) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, enfocado);
                if (valor instanceof Producto p) setText(p.getCodigo() + " · " + p.getDescripcion() + " (stock " + p.getStockInventario() + ")");
                return this;
            }
        });
        estilizarCampo(productos);
        estilizarCampo(cantidad);
        JButton agregar = boton("Agregar producto", new Color(59, 130, 246));
        agregar.addActionListener(e -> agregarProducto());
        fila.add(productos, BorderLayout.CENTER);
        fila.add(cantidad, BorderLayout.WEST);
        fila.add(agregar, BorderLayout.EAST);
        acciones.add(fila, BorderLayout.NORTH);
        JPanel nota = new JPanel(new BorderLayout(8, 0));
        nota.setOpaque(false);
        observacion.setToolTipText("Motivo o turno de cocina");
        estilizarCampo(observacion);
        JButton quitar = boton("Quitar seleccionado", new Color(153, 53, 65));
        quitar.addActionListener(e -> quitarSeleccion());
        nota.add(observacion, BorderLayout.CENTER);
        nota.add(quitar, BorderLayout.EAST);
        acciones.add(nota, BorderLayout.CENTER);
        nueva.add(acciones, BorderLayout.NORTH);
        nueva.add(estilizarTabla(tablaBorrador), BorderLayout.CENTER);
        JPanel pieNueva = new JPanel(new BorderLayout(8, 0));
        pieNueva.setOpaque(false);
        mensaje.setForeground(SUAVE);
        JButton enviar = boton("Enviar solicitud", new Color(34, 197, 142));
        enviar.addActionListener(e -> crearSolicitud());
        pieNueva.add(mensaje, BorderLayout.CENTER);
        pieNueva.add(enviar, BorderLayout.EAST);
        nueva.add(pieNueva, BorderLayout.SOUTH);

        JPanel solicitudes = tarjeta("Solicitudes registradas");
        solicitudes.add(estilizarTabla(tablaSolicitudes), BorderLayout.CENTER);
        JPanel accionesSolicitud = new JPanel(new GridLayout(1, 3, 8, 0));
        accionesSolicitud.setOpaque(false);
        JButton actualizar = boton("Actualizar", new Color(59, 130, 246));
        JButton entregar = boton("Entregar disponible", new Color(34, 197, 142));
        JButton rechazar = boton("Rechazar", new Color(153, 53, 65));
        actualizar.addActionListener(e -> recargarSolicitudes());
        entregar.addActionListener(e -> entregar());
        rechazar.addActionListener(e -> rechazar());
        accionesSolicitud.add(actualizar);
        accionesSolicitud.add(entregar);
        accionesSolicitud.add(rechazar);
        solicitudes.add(accionesSolicitud, BorderLayout.SOUTH);

        JPanel detalle = tarjeta("Productos y cantidades de la solicitud seleccionada");
        detalle.add(estilizarTabla(tablaDetalles), BorderLayout.CENTER);
        JSplitPane parteInferior = new JSplitPane(JSplitPane.VERTICAL_SPLIT, solicitudes, detalle);
        parteInferior.setResizeWeight(0.53);
        parteInferior.setBorder(null);
        parteInferior.setOpaque(false);
        parteInferior.setDividerLocation(210);
        JSplitPane principal = new JSplitPane(JSplitPane.VERTICAL_SPLIT, nueva, parteInferior);
        principal.setResizeWeight(0.42);
        principal.setBorder(null);
        principal.setOpaque(false);
        principal.setDividerLocation(255);
        add(principal, BorderLayout.CENTER);
        tablaSolicitudes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarDetalleSeleccionado();
        });
    }

    private void cargarProductos() {
        new SwingWorker<List<Producto>, Void>() {
            @Override protected List<Producto> doInBackground() throws Exception { return productosService.listarTodos(); }
            @Override protected void done() {
                try { for (Producto p : get()) if ("Activo".equalsIgnoreCase(p.getEstado()) || "Disponible".equalsIgnoreCase(p.getEstado())) productos.addItem(p); }
                catch (Exception error) { mostrarError("No se pudieron cargar los productos", error); }
            }
        }.execute();
    }

    private void agregarProducto() {
        try {
            Producto producto = (Producto) productos.getSelectedItem();
            int unidades = Integer.parseInt(cantidad.getText().trim());
            if (producto == null || unidades <= 0) throw new IllegalArgumentException("Selecciona un producto y una cantidad mayor que cero.");
            DetalleSolicitudCocina existente = borrador.stream().filter(d -> d.getIdProducto() == producto.getIdProducto()).findFirst().orElse(null);
            if (existente == null) {
                DetalleSolicitudCocina detalle = new DetalleSolicitudCocina();
                detalle.setIdProducto(producto.getIdProducto());
                detalle.setCodigoProducto(producto.getCodigo());
                detalle.setNombreProducto(producto.getDescripcion());
                detalle.setCantidadSolicitada(unidades);
                borrador.add(detalle);
            } else existente.setCantidadSolicitada(Math.addExact(existente.getCantidadSolicitada(), unidades));
            refrescarBorrador();
            cantidad.setText("1");
        } catch (NumberFormatException | ArithmeticException error) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un entero positivo y válido.", "Cantidad no válida", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Revisa los datos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void quitarSeleccion() {
        int fila = tablaBorrador.getSelectedRow();
        if (fila >= 0) { borrador.remove(fila); refrescarBorrador(); }
    }

    private void crearSolicitud() {
        if (guardando) return;
        SolicitudCocina solicitud = new SolicitudCocina();
        solicitud.setIdUsuario(usuario.getIdUsuario());
        solicitud.setObservacion(observacion.getText().trim());
        solicitud.setDetalles(new ArrayList<>(borrador));
        if (solicitud.getDetalles().isEmpty()) { JOptionPane.showMessageDialog(this, "Agrega al menos un producto.", "Solicitud incompleta", JOptionPane.WARNING_MESSAGE); return; }
        guardando = true;
        mensaje.setText("Guardando solicitud…");
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception { return servicio.crear(solicitud); }
            @Override protected void done() {
                guardando = false;
                try {
                    int id = get();
                    borrador.clear(); observacion.setText(""); refrescarBorrador();
                    mensaje.setText("Solicitud #" + id + " enviada");
                    recargarSolicitudes();
                } catch (Exception error) { mensaje.setText("No se pudo guardar"); mostrarError("No se pudo crear la solicitud", error); }
            }
        }.execute();
    }

    private void recargarSolicitudes() {
        new SwingWorker<List<SolicitudCocina>, Void>() {
            @Override protected List<SolicitudCocina> doInBackground() throws Exception { return servicio.listar(); }
            @Override protected void done() {
                try {
                    int seleccionId = idSeleccionado();
                    modeloSolicitudes.setRowCount(0);
                    DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    for (SolicitudCocina s : get()) modeloSolicitudes.addRow(new Object[]{s.getIdSolicitud(), s.getFechaSolicitud().format(formato), s.getNombreSolicitante(), s.getEstado(), s.getObservacion()});
                    ocultarId();
                    for (int i = 0; i < modeloSolicitudes.getRowCount(); i++) if ((int) modeloSolicitudes.getValueAt(i, 0) == seleccionId) { tablaSolicitudes.setRowSelectionInterval(i, i); break; }
                    if (tablaSolicitudes.getSelectedRow() < 0 && modeloSolicitudes.getRowCount() > 0) tablaSolicitudes.setRowSelectionInterval(0, 0);
                } catch (Exception error) { mostrarError("No se pudieron cargar las solicitudes", error); }
            }
        }.execute();
    }

    private void cargarDetalleSeleccionado() {
        int id = idSeleccionado();
        if (id <= 0) return;
        new SwingWorker<List<DetalleSolicitudCocina>, Void>() {
            @Override protected List<DetalleSolicitudCocina> doInBackground() throws Exception { return servicio.listarDetalles(id); }
            @Override protected void done() {
                try {
                    modeloDetalles.setRowCount(0);
                    for (DetalleSolicitudCocina d : get()) modeloDetalles.addRow(new Object[]{d.getCodigoProducto(), d.getNombreProducto(), d.getCantidadSolicitada(), d.getCantidadEntregada(), d.getStockActual(), Math.max(0, d.getCantidadSolicitada() - d.getCantidadEntregada())});
                } catch (Exception error) { mostrarError("No se pudo cargar el detalle", error); }
            }
        }.execute();
    }

    private void entregar() {
        int id = idSeleccionado();
        if (id <= 0) { avisar("Selecciona una solicitud."); return; }
        if (JOptionPane.showConfirmDialog(this, "Se entregarán las cantidades disponibles y se descontarán del inventario. ¿Continuar?", "Confirmar entrega", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        ejecutarAccion(() -> servicio.entregarDisponible(id, usuario.getIdUsuario()), "No se pudo registrar la entrega");
    }

    private void rechazar() {
        int id = idSeleccionado();
        if (id <= 0) { avisar("Selecciona una solicitud."); return; }
        if (JOptionPane.showConfirmDialog(this, "¿Rechazar la solicitud #" + id + "?", "Confirmar rechazo", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        ejecutarAccion(() -> { servicio.rechazar(id); return "Rechazada"; }, "No se pudo rechazar la solicitud");
    }

    private void ejecutarAccion(AccionBaseDatos accion, String errorTexto) {
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return accion.ejecutar(); }
            @Override protected void done() {
                try { mensaje.setText("Solicitud " + get().toLowerCase()); if (refrescarInventario != null) refrescarInventario.run(); recargarSolicitudes(); }
                catch (Exception error) { mostrarError(errorTexto, error); }
            }
        }.execute();
    }

    private int idSeleccionado() {
        int fila = tablaSolicitudes.getSelectedRow();
        if (fila < 0) return -1;
        return (int) modeloSolicitudes.getValueAt(tablaSolicitudes.convertRowIndexToModel(fila), 0);
    }

    private void ocultarId() { if (tablaSolicitudes.getColumnModel().getColumnCount() > 0) tablaSolicitudes.getColumnModel().getColumn(0).setMinWidth(0); if (tablaSolicitudes.getColumnModel().getColumnCount() > 0) tablaSolicitudes.getColumnModel().getColumn(0).setMaxWidth(0); }
    private void refrescarBorrador() { modeloBorrador.setRowCount(0); for (DetalleSolicitudCocina d : borrador) modeloBorrador.addRow(new Object[]{d.getCodigoProducto(), d.getNombreProducto(), d.getCantidadSolicitada()}); }
    private DefaultTableModel modelo(String... columnas) { return new DefaultTableModel(columnas, 0) { @Override public boolean isCellEditable(int fila, int columna) { return false; } }; }
    private JPanel tarjeta(String titulo) { JPanel panel = new JPanel(new BorderLayout(8, 8)); panel.setBackground(SUPERFICIE); panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(8, 10, 8, 10))); JLabel etiqueta = new JLabel(titulo); etiqueta.setForeground(TEXTO); etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 13f)); panel.add(etiqueta, BorderLayout.NORTH); return panel; }
    private JScrollPane estilizarTabla(JTable tabla) { tabla.setRowHeight(24); tabla.setFillsViewportHeight(true); tabla.setBackground(CAMPO); tabla.setForeground(TEXTO); tabla.setSelectionBackground(new Color(30, 64, 112)); tabla.setSelectionForeground(Color.WHITE); tabla.setGridColor(BORDE); tabla.getTableHeader().setBackground(SUPERFICIE); tabla.getTableHeader().setForeground(TEXTO); JScrollPane scroll = new JScrollPane(tabla); scroll.getViewport().setBackground(CAMPO); scroll.setBorder(BorderFactory.createLineBorder(BORDE)); return scroll; }
    private void estilizarCampo(javax.swing.JComponent campo) { campo.setForeground(TEXTO); campo.setBackground(CAMPO); campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(5, 7, 5, 7))); }
    private JButton boton(String texto, Color fondo) { JButton boton = new JButton(texto); boton.setForeground(Color.WHITE); boton.setBackground(fondo); boton.setFocusPainted(false); return boton; }
    private void avisar(String texto) { JOptionPane.showMessageDialog(this, texto, "Solicitudes de cocina", JOptionPane.INFORMATION_MESSAGE); }
    private void mostrarError(String texto, Exception error) { Throwable causa = error instanceof ExecutionException && error.getCause() != null ? error.getCause() : error; String detalle = causa instanceof SQLException || causa instanceof RuntimeException ? causa.getMessage() : "Revisa la conexión a la base de datos."; JOptionPane.showMessageDialog(this, texto + ":\n" + detalle, "Error", JOptionPane.ERROR_MESSAGE); }
    @FunctionalInterface private interface AccionBaseDatos { String ejecutar() throws Exception; }
}
