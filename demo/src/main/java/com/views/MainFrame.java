package com.views;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.SwingConstants;

/** Ventana principal del sistema de inventario. */
public class MainFrame extends JFrame {
    // Una paleta oscura y fija conserva el contraste sin depender del modo del sistema.
    private static final Color NAVY = new Color(11, 18, 32);
    private static final Color BLUE = new Color(59, 130, 246);
    private static final Color PAGE_BLUE = new Color(15, 23, 42);
    private static final Color SURFACE = new Color(30, 41, 59);
    private static final Color BORDER = new Color(51, 65, 85);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color MUTED = new Color(148, 163, 184);
    private final Runnable cerrarSesion;
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final JPanel contenido = new JPanel(new BorderLayout(18, 18));
    private final JLabel tituloSeccion = new JLabel("Panel principal");
    private final Map<String, JButton> botonesModulo = new LinkedHashMap<>();
    private String moduloActual = "Panel principal";
    private ProductoPanel catalogoProductos;

    private static final List<String> MODULOS = Arrays.asList(
            "Panel principal", "Catálogo de inventario", "Estado del inventario",
            "Compras / Entradas", "Proveedores", "Solicitudes de cocina",
            "Mermas y bajas", "Conteo físico y ajustes", "Reportes e historial");

    public MainFrame(Runnable cerrarSesion) {
        super("Gestión de inventario · Panel principal");
        this.cerrarSesion = cerrarSesion;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 560));
        setSize(1120, 740);
        setLocationRelativeTo(null);
        construirInterfaz();
        mostrarInicio();
    }

    /** Construye el marco común, menú lateral y área de trabajo. */
    private void construirInterfaz() {
        setLayout(new BorderLayout());
        add(crearMenu(), BorderLayout.WEST);

        JPanel area = new JPanel(new BorderLayout(18, 18));
        // El fondo oscuro y las superficies ligeramente más claras ordenan el contenido.
        area.setBackground(PAGE_BLUE);
        area.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(SURFACE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        tituloSeccion.setFont(tituloSeccion.getFont().deriveFont(Font.BOLD, 25f));
        tituloSeccion.setForeground(TEXT);
        encabezado.add(tituloSeccion, BorderLayout.WEST);
        JLabel usuario = new JLabel("Administrador  ·  Sesión activa");
        usuario.setForeground(MUTED);
        encabezado.add(usuario, BorderLayout.EAST);
        area.add(encabezado, BorderLayout.NORTH);
        contenido.setOpaque(false);
        area.add(contenido, BorderLayout.CENTER);
        add(area, BorderLayout.CENTER);
    }

    private JScrollPane crearMenu() {
        JPanel menu = new JPanel(new BorderLayout(0, 18));
        menu.setBackground(NAVY);
        menu.setPreferredSize(new Dimension(240, 0));
        menu.setBorder(BorderFactory.createEmptyBorder(22, 14, 16, 14));
        JPanel marca = new JPanel(new BorderLayout(0, 4));
        marca.setOpaque(false);
        JLabel icono = new JLabel("GI", SwingConstants.CENTER);
        icono.setOpaque(true);
        icono.setBackground(BLUE);
        icono.setForeground(Color.WHITE);
        icono.setFont(icono.getFont().deriveFont(Font.BOLD, 18f));
        icono.setPreferredSize(new Dimension(42, 42));
        marca.add(icono, BorderLayout.WEST);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        JLabel nombre = new JLabel("  Inventario");
        nombre.setForeground(Color.WHITE);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 16f));
        JLabel subtitulo = new JLabel("  Control de existencias");
        subtitulo.setForeground(MUTED);
        textos.add(nombre);
        textos.add(subtitulo);
        marca.add(textos, BorderLayout.CENTER);
        menu.add(marca, BorderLayout.NORTH);

        JPanel opciones = new JPanel(new GridLayout(0, 1, 0, 7));
        opciones.setOpaque(false);
        String[] iconos = {"🏠", "🐟", "📦", "🛒", "⚓", "🍽", "🦐", "⚖", "📋"};
        for (int i = 0; i < MODULOS.size(); i++) {
            final String modulo = MODULOS.get(i);
            JButton boton = new JButton("<html><span style='font-size:18pt'>" + iconos[i]
                    + "</span>&nbsp;&nbsp;<span style='font-size:12pt'>" + modulo + "</span></html>");
            boton.setHorizontalAlignment(SwingConstants.LEFT);
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setOpaque(true);
            boton.setBackground(NAVY);
            boton.setForeground(TEXT);
            boton.setBorder(BorderFactory.createEmptyBorder(11, 10, 11, 8));
            boton.setFont(boton.getFont().deriveFont(Font.PLAIN, 15f));
            boton.setRolloverEnabled(true);
            boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            boton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent evento) {
                    if (!modulo.equals(moduloActivo())) boton.setBackground(SURFACE);
                }

                @Override
                public void mouseExited(MouseEvent evento) {
                    if (!modulo.equals(moduloActivo())) boton.setBackground(NAVY);
                }
            });
            boton.addActionListener(e -> seleccionarModulo(modulo));
            botonesModulo.put(modulo, boton);
            opciones.add(boton);
        }
        JPanel interior = new JPanel(new BorderLayout());
        interior.setBackground(NAVY);
        interior.add(opciones, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(interior);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(NAVY);
        menu.add(scroll, BorderLayout.CENTER);

        JButton salir = new JButton("Cerrar sesión");
        salir.setForeground(Color.WHITE);
        salir.setBackground(SURFACE);
        salir.setFocusPainted(false);
        salir.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        salir.addActionListener(e -> {
            dispose();
            cerrarSesion.run();
        });
        menu.add(salir, BorderLayout.SOUTH);
        JScrollPane envoltura = new JScrollPane(menu);
        envoltura.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        envoltura.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        envoltura.setBorder(null);
        envoltura.setPreferredSize(new Dimension(240, 0));
        return envoltura;
    }

    /** Cambia el contenido central según el módulo elegido. */
    private void seleccionarModulo(String modulo) {
        marcarModuloActivo(modulo);
        if ("Catálogo de inventario".equals(modulo)) {
            abrirProductos();
            return;
        }
        if ("Panel principal".equals(modulo)) {
            mostrarInicio();
            return;
        }
        tituloSeccion.setText(modulo);
        contenido.removeAll();
        JPanel aviso = new JPanel(new BorderLayout(8, 8));
        aviso.setBackground(SURFACE);
        aviso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        JLabel titulo = new JLabel(modulo);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(TEXT);
        JLabel detalle = new JLabel("Este módulo está listo para integrarse al flujo de inventario.");
        detalle.setForeground(MUTED);
        aviso.add(titulo, BorderLayout.NORTH);
        aviso.add(detalle, BorderLayout.CENTER);
        contenido.add(aviso, BorderLayout.NORTH);
        contenido.revalidate();
        contenido.repaint();
    }

    private void abrirProductos() {
        marcarModuloActivo("Catálogo de inventario");
        tituloSeccion.setText("Catálogo de inventario");
        contenido.removeAll();
        if (catalogoProductos == null) {
            catalogoProductos = new ProductoPanel(this::mostrarInicio);
        }
        contenido.add(catalogoProductos, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }

    /** Pinta las tarjetas resumen con existencias actuales del DAO de productos. */
    private void mostrarInicio() {
        tituloSeccion.setText("Panel principal");
        marcarModuloActivo("Panel principal");
        contenido.removeAll();
        JPanel dashboard = new JPanel(new BorderLayout(0, 20));
        dashboard.setOpaque(false);
        JPanel bienvenidaPanel = new JPanel(new BorderLayout());
        bienvenidaPanel.setBackground(SURFACE);
        bienvenidaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(13, 17, 13, 17)));
        JLabel bienvenida = new JLabel("Resumen de existencias y productos registrados");
        bienvenida.setForeground(TEXT);
        bienvenida.setFont(bienvenida.getFont().deriveFont(Font.BOLD, 15f));
        bienvenidaPanel.add(bienvenida, BorderLayout.CENTER);
        dashboard.add(bienvenidaPanel, BorderLayout.NORTH);
        JLabel cargando = new JLabel("Cargando resumen del inventario…");
        cargando.setForeground(MUTED);
        JPanel contenedorTarjetas = new JPanel(new BorderLayout());
        contenedorTarjetas.setOpaque(false);
        contenedorTarjetas.add(cargando, BorderLayout.NORTH);
        dashboard.add(contenedorTarjetas, BorderLayout.CENTER);
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        JButton productos = new JButton("Abrir gestión de productos");
        productos.addActionListener(e -> abrirProductos());
        pie.add(productos);
        dashboard.add(pie, BorderLayout.SOUTH);
        contenido.add(dashboard, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();

        // La conexión remota corre fuera del hilo gráfico para mantener ágil la navegación.
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return productoDAO.listarTodos();
            }

            @Override
            protected void done() {
                // Descarta resultados de una pantalla que el usuario ya dejó atrás.
                if (contenido.getComponentCount() == 0 || contenido.getComponent(0) != dashboard) return;
                try {
                    contenedorTarjetas.removeAll();
                    contenedorTarjetas.add(crearTarjetas(get()), BorderLayout.NORTH);
                } catch (Exception ex) {
                    JLabel error = new JLabel("No se pudo cargar el resumen. Revisa la conexión y vuelve a intentarlo.");
                    error.setForeground(MUTED);
                    contenedorTarjetas.removeAll();
                    contenedorTarjetas.add(error, BorderLayout.NORTH);
                }
                contenedorTarjetas.revalidate();
                contenedorTarjetas.repaint();
            }
        }.execute();
    }

    /** Calcula y dibuja las cuatro métricas del inventario a partir de la lista recibida. */
    private JPanel crearTarjetas(List<Producto> productos) {
        int unidades = 0;
        int bajoMinimo = 0;
        int activos = 0;
        for (Producto producto : productos) {
            unidades += producto.getStockInventario();
            if (esStockBajo(producto)) bajoMinimo++;
            if (producto.getEstado() != null && producto.getEstado().equalsIgnoreCase("Activo")) activos++;
        }

        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 12, 0));
        tarjetas.setOpaque(false);
        tarjetas.setPreferredSize(new Dimension(700, 112));
        tarjetas.add(tarjeta("Productos", String.valueOf(productos.size()), "En el catálogo", BLUE));
        tarjetas.add(tarjeta("Unidades", String.valueOf(unidades), "Existencias totales", new Color(34, 197, 142)));
        tarjetas.add(tarjeta("Stock bajo", String.valueOf(bajoMinimo), "En o bajo el mínimo", new Color(245, 158, 66)));
        tarjetas.add(tarjeta("Activos", String.valueOf(activos), "Disponibles", new Color(56, 189, 248)));
        return tarjetas;
    }

    /** Resalta la sección activa y deja el resto de opciones listas para navegar. */
    private void marcarModuloActivo(String moduloActivo) {
        moduloActual = moduloActivo;
        botonesModulo.forEach((modulo, boton) -> {
            boolean activo = modulo.equals(moduloActivo);
            boton.setBackground(activo ? new Color(30, 64, 112) : NAVY);
            boton.setForeground(activo ? Color.WHITE : TEXT);
        });
    }

    private String moduloActivo() {
        return moduloActual;
    }

    // Bloque de conteo: incluye umbrales numéricos y productos marcados manualmente con poco stock.
    static boolean esStockBajo(Producto producto) {
        return producto.getStockInventario() <= producto.getStockMinimo()
                || "Poco stock".equalsIgnoreCase(producto.getEstado());
    }

    /** Tarjeta compacta con fondo oscuro y acento de color en el borde. */
    private JPanel tarjeta(String nombre, String valor, String detalle, Color acento) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 3));
        tarjeta.setBackground(SURFACE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, acento),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(9, 10, 9, 10))));
        JLabel encabezado = new JLabel(nombre);
        encabezado.setForeground(MUTED);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 12f));
        JLabel numero = new JLabel(valor);
        numero.setForeground(TEXT);
        numero.setFont(numero.getFont().deriveFont(Font.BOLD, 29f));
        JLabel pie = new JLabel(detalle);
        pie.setForeground(MUTED);
        pie.setFont(pie.getFont().deriveFont(10f));
        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(numero, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);
        return tarjeta;
    }
}
