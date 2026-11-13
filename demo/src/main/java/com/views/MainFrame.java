package com.views;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/** Ventana principal del sistema de inventario. */
public class MainFrame extends JFrame {
    // Bloque de paleta azul: tonos consistentes para menú, fondo y métricas.
    private static final Color NAVY = new Color(20, 43, 78);
    private static final Color BLUE = new Color(38, 105, 207);
    private static final Color PAGE_BLUE = new Color(239, 245, 253);
    private final Runnable cerrarSesion;
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final JPanel contenido = new JPanel(new BorderLayout(18, 18));
    private final JLabel tituloSeccion = new JLabel("Panel principal");

    private static final List<String> MODULOS = Arrays.asList(
            "Panel principal", "Catálogo de inventario", "Estado del inventario",
            "Compras / Entradas", "Proveedores", "Solicitudes de cocina",
            "Mermas y bajas", "Conteo físico y ajustes", "Reportes e historial");

    public MainFrame(Runnable cerrarSesion) {
        super("Inventario · Panel principal");
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
        // Bloque de fondo claro para separar títulos y contenido sin perder contraste.
        area.setBackground(PAGE_BLUE);
        area.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        // Bloque de encabezado blanco para que el nombre de cada sección siempre destaque.
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Color.WHITE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(221, 231, 245)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        tituloSeccion.setFont(tituloSeccion.getFont().deriveFont(Font.BOLD, 25f));
        tituloSeccion.setForeground(new Color(27, 55, 96));
        encabezado.add(tituloSeccion, BorderLayout.WEST);
        JLabel usuario = new JLabel("Administrador  ·  Sesión activa");
        usuario.setForeground(new Color(100, 110, 128));
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
        JLabel icono = new JLabel("IP", SwingConstants.CENTER);
        icono.setOpaque(true);
        icono.setBackground(BLUE);
        icono.setForeground(Color.WHITE);
        icono.setFont(icono.getFont().deriveFont(Font.BOLD, 18f));
        icono.setPreferredSize(new Dimension(42, 42));
        marca.add(icono, BorderLayout.WEST);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        JLabel nombre = new JLabel("  Inventario PI");
        nombre.setForeground(Color.WHITE);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 16f));
        JLabel subtitulo = new JLabel("  Gestión de inventario");
        subtitulo.setForeground(new Color(173, 186, 210));
        textos.add(nombre);
        textos.add(subtitulo);
        marca.add(textos, BorderLayout.CENTER);
        menu.add(marca, BorderLayout.NORTH);

        JPanel opciones = new JPanel(new GridLayout(0, 1, 0, 7));
        opciones.setOpaque(false);
        String[] iconos = {"🏠", "🐟", "📦", "🛒", "⚓", "🍽", "🦐", "⚖", "📋"};
        for (int i = 0; i < MODULOS.size(); i++) {
            final String modulo = MODULOS.get(i);
            // Bloque de icono y texto: el símbolo gana tamaño sin agrandar demasiado las etiquetas.
            JButton boton = new JButton("<html><span style='font-size:18pt'>" + iconos[i]
                    + "</span>&nbsp;&nbsp;<span style='font-size:12pt'>" + modulo + "</span></html>");
            boton.setHorizontalAlignment(SwingConstants.LEFT);
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setOpaque(true);
            boton.setBackground(NAVY);
            boton.setForeground(new Color(224, 231, 243));
            boton.setBorder(BorderFactory.createEmptyBorder(11, 10, 11, 8));
            boton.setFont(boton.getFont().deriveFont(Font.PLAIN, 15f));
            boton.addActionListener(e -> seleccionarModulo(modulo));
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
        salir.setBackground(new Color(43, 59, 89));
        salir.setFocusPainted(false);
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
        aviso.setBackground(Color.WHITE);
        aviso.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel titulo = new JLabel(modulo);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        JLabel detalle = new JLabel("Este módulo está listo para integrarse al flujo de inventario.");
        detalle.setForeground(new Color(100, 110, 128));
        aviso.add(titulo, BorderLayout.NORTH);
        aviso.add(detalle, BorderLayout.CENTER);
        contenido.add(aviso, BorderLayout.NORTH);
        contenido.revalidate();
        contenido.repaint();
    }

    private void abrirProductos() {
        tituloSeccion.setText("Catálogo de inventario");
        contenido.removeAll();
        contenido.add(new ProductoPanel(this::mostrarInicio), BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }

    /** Pinta las tarjetas resumen con existencias actuales del DAO de productos. */
    private void mostrarInicio() {
        tituloSeccion.setText("Panel principal");
        contenido.removeAll();
        JPanel dashboard = new JPanel(new BorderLayout(0, 20));
        dashboard.setOpaque(false);
        // Bloque de bienvenida sobre una superficie blanca para que el título destaque.
        JPanel bienvenidaPanel = new JPanel(new BorderLayout());
        bienvenidaPanel.setBackground(Color.WHITE);
        bienvenidaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(221, 231, 245)),
                BorderFactory.createEmptyBorder(13, 17, 13, 17)));
        JLabel bienvenida = new JLabel("Resumen de existencias y productos registrados");
        bienvenida.setForeground(new Color(39, 57, 88));
        bienvenida.setFont(bienvenida.getFont().deriveFont(Font.BOLD, 15f));
        bienvenidaPanel.add(bienvenida, BorderLayout.CENTER);
        dashboard.add(bienvenidaPanel, BorderLayout.NORTH);
        try {
            List<Producto> productos = productoDAO.listarTodos();
            int unidades = 0;
            int bajoMinimo = 0;
            int activos = 0;
            for (Producto p : productos) {
                unidades += p.getStockInventario();
                if (esStockBajo(p)) bajoMinimo++;
                if (p.getEstado() != null && p.getEstado().equalsIgnoreCase("Activo")) activos++;
            }
            // Bloque compacto de métricas: cuatro tarjetas alineadas y agrupadas en horizontal.
            JPanel tarjetas = new JPanel(new GridLayout(1, 4, 12, 0));
            tarjetas.setOpaque(false);
            tarjetas.setPreferredSize(new Dimension(700, 112));
            tarjetas.add(tarjeta("Productos", String.valueOf(productos.size()), "En el catálogo", BLUE));
            tarjetas.add(tarjeta("Unidades", String.valueOf(unidades), "Existencias totales", new Color(25, 137, 112)));
            tarjetas.add(tarjeta("Stock bajo", String.valueOf(bajoMinimo), "En o bajo el mínimo", new Color(205, 111, 48)));
            tarjetas.add(tarjeta("Activos", String.valueOf(activos), "Disponibles", new Color(49, 112, 163)));
            JPanel contenedorTarjetas = new JPanel(new BorderLayout());
            contenedorTarjetas.setOpaque(false);
            contenedorTarjetas.add(tarjetas, BorderLayout.NORTH);
            dashboard.add(contenedorTarjetas, BorderLayout.CENTER);
        } catch (SQLException ex) {
            JPanel error = new JPanel(new BorderLayout(8, 8));
            error.setBackground(Color.WHITE);
            error.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
            JLabel titulo = new JLabel("No se pudo cargar el resumen del inventario");
            titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 17f));
            error.add(titulo, BorderLayout.NORTH);
            error.add(new JLabel("Verifica la conexión a la base de datos y vuelve a abrir el panel. " + ex.getMessage()), BorderLayout.CENTER);
            dashboard.add(error, BorderLayout.CENTER);
        }
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pie.setOpaque(false);
        JButton productos = new JButton("Abrir gestión de productos");
        productos.addActionListener(e -> abrirProductos());
        pie.add(productos);
        dashboard.add(pie, BorderLayout.SOUTH);
        contenido.add(dashboard, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }

    // Bloque de conteo: incluye umbrales numéricos y productos marcados manualmente con poco stock.
    static boolean esStockBajo(Producto producto) {
        return producto.getStockInventario() <= producto.getStockMinimo()
                || "Poco stock".equalsIgnoreCase(producto.getEstado());
    }

    /** Bloque visual de tarjeta compacta con jerarquía clara y cifra azul de alto contraste. */
    private JPanel tarjeta(String nombre, String valor, String detalle, Color acento) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 3));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, acento),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(221, 231, 245)),
                        BorderFactory.createEmptyBorder(9, 10, 9, 10))));
        JLabel encabezado = new JLabel(nombre);
        encabezado.setForeground(new Color(63, 78, 103));
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 12f));
        JLabel numero = new JLabel(valor);
        numero.setForeground(new Color(20, 65, 135));
        numero.setFont(numero.getFont().deriveFont(Font.BOLD, 29f));
        JLabel pie = new JLabel(detalle);
        pie.setForeground(new Color(84, 99, 123));
        pie.setFont(pie.getFont().deriveFont(10f));
        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(numero, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);
        return tarjeta;
    }
}
