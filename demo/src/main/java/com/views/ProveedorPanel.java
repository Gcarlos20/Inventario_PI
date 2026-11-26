package com.views;

import com.model.OfertaProveedor;
import com.model.Proveedor;
import com.service.CatalogoProveedorService;
import com.service.ProveedorService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/** Administración de proveedores y su catálogo de productos o servicios. */
public class ProveedorPanel extends JPanel {
    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color SUPERFICIE = new Color(30, 41, 59);
    private static final Color CAMPO = new Color(15, 23, 42);
    private static final Color BORDE = new Color(51, 65, 85);
    private static final Color TEXTO = new Color(226, 232, 240);
    private static final Color SUAVE = new Color(148, 163, 184);
    private static final Color AZUL = new Color(59, 130, 246);
    private static final Color SELECCION = new Color(30, 64, 112);
    private static final Color ROJO = new Color(153, 53, 65);

    private final ProveedorService proveedorService = new ProveedorService();
    private final CatalogoProveedorService catalogoService = new CatalogoProveedorService();
    private final Runnable regresar;
    private final boolean editable;
    private final DefaultTableModel proveedoresModel = modelo("ID", "Empresa", "NIT", "Contacto", "Teléfono", "Correo", "Ciudad", "Tipo", "Estado");
    private final JTable proveedoresTabla = new JTable(proveedoresModel);
    private final JTextField filtroProveedores = campo();
    private final DefaultTableModel ofertasModel = modelo("ID", "Producto / servicio", "Tipo", "Precio referencia", "Descripción");
    private final JTable ofertasTabla = new JTable(ofertasModel);
    private final JTextField empresa = campo();
    private final JTextField nit = campo();
    private final JTextField contacto = campo();
    private final JTextField telefono = campo();
    private final JTextField correo = campo();
    private final JTextField direccion = campo();
    private final JTextField ciudad = campo();
    private final JComboBox<String> tipoProveedor = combo("Producto", "Servicio", "Mixto");
    private final JComboBox<String> estadoProveedor = combo("Activo", "Inactivo");
    private final JTextArea observacion = new JTextArea(3, 18);
    private final JLabel estadoProveedores = etiqueta("Cargando proveedores…");
    private final JComboBox<Proveedor> proveedorCatalogo = new JComboBox<>();
    private final JTextField ofertaNombre = campo();
    private final JComboBox<String> ofertaTipo = combo("Producto", "Servicio");
    private final JTextField ofertaPrecio = campo();
    private final JTextArea ofertaDescripcion = new JTextArea(3, 18);
    private final JLabel estadoCatalogo = etiqueta("Selecciona un proveedor para consultar su catálogo.");
    private Integer idProveedorSeleccionado;
    private Integer idOfertaSeleccionada;
    private List<Proveedor> proveedoresActuales;
    private boolean ocupado;
    private boolean cargandoEstado;

    public ProveedorPanel(Runnable regresar) {
        this(regresar, true);
    }

    public ProveedorPanel(Runnable regresar, boolean editable) {
        this.regresar = regresar;
        this.editable = editable;
        setLayout(new BorderLayout(12, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        add(crearBarra(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(SUPERFICIE);
        tabs.setForeground(TEXTO);
        tabs.addTab("Proveedores", crearVistaProveedores());
        tabs.addTab("Catálogo de productos y servicios", crearVistaCatalogo());
        add(tabs, BorderLayout.CENTER);
        cargarProveedores();
    }

    // ===== Encabezado y navegación =====
    private JPanel crearBarra() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JButton volver = boton("←  Volver al resumen", SUPERFICIE, TEXTO);
        volver.addActionListener(e -> regresar.run()); p.add(volver, BorderLayout.WEST);
        JLabel ayuda = etiqueta("Registra proveedores, controla su estado y consulta lo que ofrecen.");
        p.add(ayuda, BorderLayout.CENTER); return p;
    }

    // ===== Pestaña de proveedores: formulario arriba y tabla abajo, como Compras =====
    private JPanel crearVistaProveedores() {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setOpaque(false);
        JPanel editor = panel("Datos del proveedor", "Información de contacto y clasificación");
        JPanel form = formulario();
        estilizarArea(observacion);
        agregarCampo(form, "Empresa *", empresa, 0, 0, 2);
        agregarCampo(form, "NIT", nit, 2, 0, 1);
        agregarCampo(form, "Persona de contacto", contacto, 3, 0, 1);
        agregarCampo(form, "Teléfono", telefono, 0, 1, 1);
        agregarCampo(form, "Correo", correo, 1, 1, 1);
        agregarCampo(form, "Dirección", direccion, 2, 1, 1);
        agregarCampo(form, "Ciudad", ciudad, 3, 1, 1);
        agregarCampo(form, "Tipo de proveedor", tipoProveedor, 0, 2, 1);
        agregarCampo(form, "Estado", estadoProveedor, 1, 2, 1);
        agregarCampo(form, "Observación", observacion, 2, 2, 2);
        editor.add(form, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new GridBagLayout()); acciones.setOpaque(false);
        JButton nuevo = boton("Agregar", AZUL, Color.WHITE), guardar = boton("Guardar cambios", new Color(55, 75, 105), TEXTO);
        JButton eliminar = boton("Eliminar", ROJO, Color.WHITE);
        JButton limpiar = boton("Limpiar", new Color(55, 75, 105), TEXTO), recargar = boton("Actualizar lista", new Color(55, 75, 105), TEXTO);
        JButton[] botones = {nuevo, guardar, eliminar, limpiar, recargar};
        for (int i = 0; i < botones.length; i++) { GridBagConstraints c = gbc(i % 2, i / 2); c.fill = GridBagConstraints.HORIZONTAL; acciones.add(botones[i], c); }
        editor.add(acciones, BorderLayout.SOUTH);
        nuevo.addActionListener(e -> guardarProveedor(false)); guardar.addActionListener(e -> guardarProveedor(true));
        eliminar.addActionListener(e -> eliminarProveedor());
        estadoProveedor.addActionListener(e -> guardarEstadoSeleccionado());
        limpiar.addActionListener(e -> limpiarProveedor()); recargar.addActionListener(e -> cargarProveedores());

        JPanel lista = panel("Proveedores registrados", "Selecciona una fila para editarla o cambiar su estado");
        configurarTabla(proveedoresTabla);
        configurarFiltroProveedores();
        proveedoresTabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {45, 160, 100, 135, 105, 170, 100, 95, 85};
        for (int i = 0; i < widths.length; i++) proveedoresTabla.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        proveedoresTabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && proveedoresTabla.getSelectedRow() >= 0) seleccionarProveedor(proveedoresTabla.convertRowIndexToModel(proveedoresTabla.getSelectedRow()));
        });
        JPanel contenidoLista = new JPanel(new BorderLayout(0, 8));
        contenidoLista.setOpaque(false);
        JPanel filaBusqueda = new JPanel(new BorderLayout(8, 0));
        filaBusqueda.setOpaque(false);
        filaBusqueda.add(etiqueta("Buscar por ID o nombre:"), BorderLayout.WEST);
        filaBusqueda.add(filtroProveedores, BorderLayout.CENTER);
        contenidoLista.add(filaBusqueda, BorderLayout.NORTH);
        contenidoLista.add(new JScrollPane(proveedoresTabla), BorderLayout.CENTER);
        contenidoLista.add(estadoProveedores, BorderLayout.SOUTH);
        lista.add(contenidoLista, BorderLayout.CENTER);
        if (editable) p.add(editor, BorderLayout.NORTH);
        p.add(lista, BorderLayout.CENTER);
        return p;
    }

    // El filtro busca texto parcial en las columnas ID y Empresa sin volver a consultar la base.
    private void configurarFiltroProveedores() {
        TableRowSorter<DefaultTableModel> ordenador = new TableRowSorter<>(proveedoresModel);
        proveedoresTabla.setRowSorter(ordenador);
        filtroProveedores.setToolTipText("Escribe un ID o parte del nombre de la empresa");
        filtroProveedores.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void aplicarFiltro() {
                String texto = filtroProveedores.getText().trim();
                if (texto.isEmpty()) {
                    ordenador.setRowFilter(null);
                } else {
                    ordenador.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 0, 1));
                }
            }

            @Override public void insertUpdate(javax.swing.event.DocumentEvent evento) { aplicarFiltro(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent evento) { aplicarFiltro(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent evento) { aplicarFiltro(); }
        });
    }

    // ===== Pestaña de catálogo: formulario de oferta y tabla de resultados =====
    private JPanel crearVistaCatalogo() {
        JPanel p = new JPanel(new BorderLayout(0, 10)); p.setOpaque(false);
        JPanel editor = panel("Oferta del proveedor", "Añade productos o servicios al catálogo");
        JPanel form = formulario();
        proveedorCatalogo.setBackground(CAMPO); proveedorCatalogo.setForeground(TEXTO);
        estilizarArea(ofertaDescripcion);
        agregarCampo(form, "Proveedor", proveedorCatalogo, 0, 0, 2);
        agregarCampo(form, "Nombre *", ofertaNombre, 2, 0, 2);
        agregarCampo(form, "Tipo", ofertaTipo, 0, 1, 1);
        agregarCampo(form, "Precio de referencia", ofertaPrecio, 1, 1, 1);
        agregarCampo(form, "Descripción", ofertaDescripcion, 2, 1, 2);
        editor.add(form, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new GridBagLayout()); acciones.setOpaque(false);
        JButton agregar = boton("Agregar oferta", AZUL, Color.WHITE), guardar = boton("Guardar cambios", new Color(55, 75, 105), TEXTO);
        JButton borrar = boton("Eliminar", ROJO, Color.WHITE), limpiar = boton("Limpiar", new Color(55, 75, 105), TEXTO);
        JButton[] bs = {agregar, guardar, borrar, limpiar};
        for (int i = 0; i < bs.length; i++) { GridBagConstraints c = gbc(i % 2, i / 2); c.fill = GridBagConstraints.HORIZONTAL; acciones.add(bs[i], c); }
        editor.add(acciones, BorderLayout.SOUTH);
        agregar.addActionListener(e -> guardarOferta(false)); guardar.addActionListener(e -> guardarOferta(true));
        borrar.addActionListener(e -> eliminarOferta()); limpiar.addActionListener(e -> limpiarOferta());
        proveedorCatalogo.addActionListener(e -> { limpiarOferta(); cargarOfertas(); });

        JPanel lista = panel("Catálogo", "Productos y servicios asociados al proveedor seleccionado");
        configurarTabla(ofertasTabla); ofertasTabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {45, 180, 100, 130, 260};
        for (int i = 0; i < widths.length; i++) ofertasTabla.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        ofertasTabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && ofertasTabla.getSelectedRow() >= 0) seleccionarOferta(ofertasTabla.convertRowIndexToModel(ofertasTabla.getSelectedRow()));
        });
        lista.add(new JScrollPane(ofertasTabla), BorderLayout.CENTER);
        lista.add(estadoCatalogo, BorderLayout.SOUTH);
        if (editable) p.add(editor, BorderLayout.NORTH);
        p.add(lista, BorderLayout.CENTER);
        return p;
    }

    // ===== Lectura y selección de proveedores =====
    private void cargarProveedores() {
        ejecutar("Cargando proveedores…", proveedorService::listarTodos, lista -> {
            proveedoresActuales = lista; proveedoresModel.setRowCount(0);
            for (Proveedor v : lista) proveedoresModel.addRow(new Object[]{v.getIdProveedor(), v.getNombreEmpresa(), v.getNit(), v.getNombreContacto(), v.getTelefono(), v.getCorreo(), v.getCiudad(), v.getTipoProveedor(), v.getEstado()});
            proveedorCatalogo.removeAllItems();
            for (Proveedor v : lista) proveedorCatalogo.addItem(v);
            estadoProveedores.setText("Proveedores registrados: " + lista.size());
            cargarOfertas();
        });
    }

    private void seleccionarProveedor(int fila) {
        if (proveedoresActuales == null || fila >= proveedoresActuales.size()) return;
        Proveedor v = proveedoresActuales.get(fila); idProveedorSeleccionado = v.getIdProveedor();
        empresa.setText(valor(v.getNombreEmpresa())); nit.setText(valor(v.getNit())); contacto.setText(valor(v.getNombreContacto()));
        telefono.setText(valor(v.getTelefono())); correo.setText(valor(v.getCorreo())); direccion.setText(valor(v.getDireccion()));
        ciudad.setText(valor(v.getCiudad())); tipoProveedor.setSelectedItem(v.getTipoProveedor()); observacion.setText(valor(v.getObservacion()));
        cargandoEstado = true;
        estadoProveedor.setSelectedItem(v.getEstado());
        cargandoEstado = false;
        estadoProveedores.setText("Proveedor seleccionado: " + v.getNombreEmpresa() + " · " + v.getEstado());
        for (int i = 0; i < proveedorCatalogo.getItemCount(); i++) if (proveedorCatalogo.getItemAt(i).getIdProveedor() == v.getIdProveedor()) { proveedorCatalogo.setSelectedIndex(i); break; }
    }

    // ===== Operaciones CRUD y activación del proveedor =====
    private void guardarProveedor(boolean editar) {
        if (editar && idProveedorSeleccionado == null) { mensaje("Selecciona un proveedor para guardar los cambios.", JOptionPane.WARNING_MESSAGE); return; }
        Proveedor v = new Proveedor(); if (editar) v.setIdProveedor(idProveedorSeleccionado);
        v.setNombreEmpresa(empresa.getText().trim()); v.setNit(nit.getText().trim()); v.setNombreContacto(contacto.getText().trim());
        v.setTelefono(telefono.getText().trim()); v.setCorreo(correo.getText().trim()); v.setDireccion(direccion.getText().trim());
        v.setCiudad(ciudad.getText().trim()); v.setTipoProveedor((String) tipoProveedor.getSelectedItem());
        v.setEstado(editar ? estadoActual() : "Activo"); v.setObservacion(observacion.getText().trim());
        ejecutar(editar ? "Guardando cambios…" : "Registrando proveedor…", () -> {
            if (editar) return proveedorService.actualizar(v); proveedorService.crear(v); return true;
        }, ok -> { if (!ok) throw new IllegalStateException("No se encontró el proveedor seleccionado."); limpiarProveedor(); cargarProveedores(); });
    }

    private String estadoActual() {
        if (proveedoresActuales != null) for (Proveedor p : proveedoresActuales) if (p.getIdProveedor() == idProveedorSeleccionado) return p.getEstado();
        return "Activo";
    }

    private void guardarEstadoSeleccionado() {
        if (cargandoEstado || idProveedorSeleccionado == null) return;
        if (ocupado) {
            cargandoEstado = true;
            estadoProveedor.setSelectedItem(estadoActual());
            cargandoEstado = false;
            return;
        }
        String nuevoEstado = (String) estadoProveedor.getSelectedItem();
        if (nuevoEstado == null || nuevoEstado.equalsIgnoreCase(estadoActual())) return;

        int id = idProveedorSeleccionado;
        ejecutar("Actualizando estado…", () -> proveedorService.cambiarEstado(id, nuevoEstado), actualizado -> {
            if (!actualizado) throw new IllegalStateException("No se encontró el proveedor seleccionado.");
            actualizarEstadoLocal(id, nuevoEstado);
            estadoProveedores.setText("Estado actualizado: " + nuevoEstado);
        });
    }

    private void actualizarEstadoLocal(int id, String nuevoEstado) {
        if (proveedoresActuales != null) {
            for (Proveedor proveedor : proveedoresActuales) {
                if (proveedor.getIdProveedor() == id) proveedor.setEstado(nuevoEstado);
            }
        }
        for (int fila = 0; fila < proveedoresModel.getRowCount(); fila++) {
            if (((Integer) proveedoresModel.getValueAt(fila, 0)) == id) {
                proveedoresModel.setValueAt(nuevoEstado, fila, 8);
                break;
            }
        }
    }

    private void eliminarProveedor() {
        if (idProveedorSeleccionado == null) { mensaje("Selecciona un proveedor primero.", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar el proveedor seleccionado? Esta acción no se puede deshacer.", "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        int id = idProveedorSeleccionado;
        ejecutar("Eliminando proveedor…", () -> proveedorService.eliminar(id), ok -> { limpiarProveedor(); cargarProveedores(); });
    }

    // ===== Operaciones CRUD del catálogo =====
    private void cargarOfertas() {
        Proveedor elegido = (Proveedor) proveedorCatalogo.getSelectedItem();
        ofertasModel.setRowCount(0);
        if (elegido == null) { estadoCatalogo.setText("No hay proveedores registrados."); return; }
        int idConsultado = elegido.getIdProveedor();
        ejecutar("Cargando catálogo…", () -> catalogoService.listar(elegido.getIdProveedor()), lista -> {
            for (OfertaProveedor o : lista) ofertasModel.addRow(new Object[]{o.getId(), o.getNombre(), o.getTipo(), o.getPrecioReferencia(), o.getDescripcion()});
            estadoCatalogo.setText("Ofertas registradas: " + lista.size());
            Proveedor actual = (Proveedor) proveedorCatalogo.getSelectedItem();
            if (actual != null && actual.getIdProveedor() != idConsultado) cargarOfertas();
        });
    }

    private void seleccionarOferta(int fila) {
        idOfertaSeleccionada = (Integer) ofertasModel.getValueAt(fila, 0);
        ofertaNombre.setText(valor(ofertasModel.getValueAt(fila, 1))); ofertaTipo.setSelectedItem(ofertasModel.getValueAt(fila, 2));
        ofertaPrecio.setText(valor(ofertasModel.getValueAt(fila, 3))); ofertaDescripcion.setText(valor(ofertasModel.getValueAt(fila, 4)));
    }

    private void guardarOferta(boolean editar) {
        Proveedor proveedor = (Proveedor) proveedorCatalogo.getSelectedItem();
        if (proveedor == null) { mensaje("Selecciona un proveedor.", JOptionPane.WARNING_MESSAGE); return; }
        if (editar && idOfertaSeleccionada == null) { mensaje("Selecciona una oferta para editar.", JOptionPane.WARNING_MESSAGE); return; }
        BigDecimal precio = null;
        try { if (!ofertaPrecio.getText().isBlank()) { precio = new BigDecimal(ofertaPrecio.getText().trim()); } }
        catch (NumberFormatException e) { mensaje("El precio debe ser un número válido.", JOptionPane.WARNING_MESSAGE); return; }
        OfertaProveedor o = new OfertaProveedor(); o.setIdProveedor(proveedor.getIdProveedor()); o.setId(idOfertaSeleccionada == null ? 0 : idOfertaSeleccionada);
        o.setNombre(ofertaNombre.getText().trim()); o.setTipo((String) ofertaTipo.getSelectedItem()); o.setPrecioReferencia(precio); o.setDescripcion(ofertaDescripcion.getText().trim());
        ejecutar(editar ? "Guardando oferta…" : "Agregando oferta…", () -> { if (editar) return catalogoService.actualizar(o); catalogoService.crear(o); return true; }, ok -> { limpiarOferta(); cargarOfertas(); });
    }

    private void eliminarOferta() {
        Proveedor proveedor = (Proveedor) proveedorCatalogo.getSelectedItem();
        if (proveedor == null || idOfertaSeleccionada == null) { mensaje("Selecciona una oferta del catálogo.", JOptionPane.WARNING_MESSAGE); return; }
        int id = idOfertaSeleccionada;
        ejecutar("Eliminando oferta…", () -> catalogoService.eliminar(id, proveedor.getIdProveedor()), ok -> { limpiarOferta(); cargarOfertas(); });
    }

    private void limpiarProveedor() {
        proveedoresTabla.clearSelection(); idProveedorSeleccionado = null;
        for (JTextField t : new JTextField[]{empresa, nit, contacto, telefono, correo, direccion, ciudad}) t.setText("");
        observacion.setText(""); tipoProveedor.setSelectedIndex(0); estadoProveedores.setText("Formulario listo para un proveedor nuevo.");
        cargandoEstado = true;
        estadoProveedor.setSelectedItem("Activo");
        cargandoEstado = false;
    }
    private void limpiarOferta() {
        ofertasTabla.clearSelection(); idOfertaSeleccionada = null; ofertaNombre.setText(""); ofertaPrecio.setText(""); ofertaDescripcion.setText(""); ofertaTipo.setSelectedIndex(0);
    }

    // Ejecuta las consultas fuera de la interfaz para que la ventana siga respondiendo.
    private <T> void ejecutar(String mensaje, Callable<T> tarea, java.util.function.Consumer<T> exito) {
        if (ocupado) return; ocupado = true; estadoProveedores.setText(mensaje);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return tarea.call(); }
            @Override protected void done() {
                ocupado = false;
                try { exito.accept(get()); }
                catch (Exception e) {
                    Throwable causa = e.getCause() == null ? e : e.getCause();
                    mensaje("No se pudo completar la operación: " + causa.getMessage(), JOptionPane.ERROR_MESSAGE);
                    if (idProveedorSeleccionado != null) {
                        cargandoEstado = true;
                        estadoProveedor.setSelectedItem(estadoActual());
                        cargandoEstado = false;
                    }
                }
            }
        }.execute();
    }

    private JPanel panel(String titulo, String detalle) {
        JPanel p = new JPanel(new BorderLayout(0, 12)); p.setBackground(SUPERFICIE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(13, 13, 12, 13)));
        JPanel h = new JPanel(new BorderLayout(0, 4)); h.setOpaque(false);
        JLabel t = etiqueta(titulo); t.setFont(t.getFont().deriveFont(Font.BOLD, 16f));
        h.add(t, BorderLayout.NORTH); h.add(etiqueta(detalle), BorderLayout.SOUTH); p.add(h, BorderLayout.NORTH); return p;
    }
    private JPanel formulario() { JPanel p = new JPanel(new GridBagLayout()); p.setBackground(SUPERFICIE); p.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0)); return p; }
    // ===== Componentes reutilizables y estilos de la paleta del proyecto =====
    private void agregarCampo(JPanel p, String texto, java.awt.Component campo,
                              int columna, int fila, int ancho) {
        JPanel contenedor = new JPanel(new BorderLayout(0, 4));
        contenedor.setOpaque(false);
        JLabel label = etiqueta(texto);
        label.setForeground(TEXTO);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        contenedor.add(label, BorderLayout.NORTH);
        contenedor.add(campo, BorderLayout.CENTER);

        GridBagConstraints c = gbc(columna, fila);
        c.gridwidth = ancho;
        c.weightx = ancho;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 4, 4, 4);
        p.add(contenedor, c);
    }
    private static GridBagConstraints gbc(int x, int y) { GridBagConstraints c = new GridBagConstraints(); c.gridx=x; c.gridy=y; c.weightx=1; c.anchor=GridBagConstraints.WEST; return c; }
    private static DefaultTableModel modelo(String... columnas) { return new DefaultTableModel(columnas, 0) { @Override public boolean isCellEditable(int r,int c) { return false; } }; }
    private static JTextField campo() { JTextField t = new JTextField(); t.setBackground(CAMPO); t.setForeground(TEXTO); t.setCaretColor(TEXTO); t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(6, 7, 6, 7))); return t; }
    private static JComboBox<String> combo(String... valores) { JComboBox<String> c = new JComboBox<>(valores); c.setBackground(CAMPO); c.setForeground(TEXTO); return c; }
    private static JLabel etiqueta(String s) { JLabel l = new JLabel(s); l.setForeground(SUAVE); return l; }
    private static JButton boton(String texto, Color fondo, Color frente) { JButton b = new JButton(texto); b.setBackground(fondo); b.setForeground(frente); b.setFocusPainted(false); b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)); b.setBorder(BorderFactory.createEmptyBorder(8, 9, 8, 9)); return b; }
    private static void estilizarArea(JTextArea a) { a.setBackground(CAMPO); a.setForeground(TEXTO); a.setCaretColor(TEXTO); a.setLineWrap(true); a.setWrapStyleWord(true); a.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), BorderFactory.createEmptyBorder(5, 6, 5, 6))); }
    private static String valor(Object o) { return o == null ? "" : o.toString(); }
    private void configurarTabla(JTable tabla) {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); tabla.setRowHeight(29); tabla.setFont(tabla.getFont().deriveFont(12f));
        tabla.setFillsViewportHeight(true); tabla.setBackground(CAMPO); tabla.setForeground(TEXTO); tabla.setSelectionBackground(SELECCION);
        tabla.setSelectionForeground(Color.WHITE); tabla.setGridColor(BORDE); tabla.setShowGrid(true);
        JTableHeader h = tabla.getTableHeader(); h.setReorderingAllowed(false); h.setBackground(new Color(39, 54, 75)); h.setForeground(TEXTO); h.setFont(h.getFont().deriveFont(Font.BOLD, 12f)); h.setPreferredSize(new Dimension(0, 34));
    }
    private void mensaje(String texto, int tipo) { JOptionPane.showMessageDialog(this, texto, "Proveedores", tipo); }
}
