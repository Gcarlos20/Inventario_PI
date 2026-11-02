package com.views;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

public class DashboardFrame extends JFrame {
    private static final Color NAV = new Color(25, 36, 54);
    private static final Color NAV_LIGHT = new Color(39, 53, 75);
    private static final Color BLUE = new Color(49, 95, 230);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color TEXT = new Color(35, 46, 65);
    private static final Color MUTED = new Color(112, 125, 145);
    private static final Color BORDER = new Color(228, 233, 241);
    private static final String HOME = "Panel principal";
    private static final String PRODUCTS = "Productos y categorías";
    private static final String[] MODULES = {
            HOME,
            "Usuarios y roles",
            PRODUCTS,
            "Proveedores",
            "Entradas y lotes",
            "Solicitudes de cocina",
            "Salidas / despacho",
            "Mermas y bajas",
            "Conteo físico",
            "Historial de movimientos",
            "Reportes y estadísticas"
    };

    private final String usuario;
    private final String rol;
    private final Runnable volverAlLogin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel paginas = new JPanel(cardLayout);
    private final Map<String, JPanel> paginasCreadas = new LinkedHashMap<>();
    private final Map<String, JButton> botonesModulo = new LinkedHashMap<>();
    private final JLabel tituloSeccion = new JLabel();
    private final JLabel productosValor = new JLabel("—");
    private final JLabel stockBajoValor = new JLabel("—");
    private final JPanel listaAlertas = new JPanel();

    public DashboardFrame(String usuario, String rol, Runnable volverAlLogin) {
        super("Inventario | Panel principal");
        this.usuario = usuario;
        this.rol = rol;
        this.volverAlLogin = volverAlLogin;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 560));
        setSize(1220, 790);
        setLocationRelativeTo(null);
        construirInterfaz();
        mostrarModulo(HOME);
        cargarResumen();
    }

    private void construirInterfaz() {
        add(crearBarraLateral(), BorderLayout.WEST);

        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(BACKGROUND);
        principal.add(crearBarraSuperior(), BorderLayout.NORTH);
        paginas.setBackground(BACKGROUND);
        paginasCreadas.put(HOME, crearPanelPrincipal());
        paginas.add(paginasCreadas.get(HOME), HOME);
        principal.add(paginas, BorderLayout.CENTER);
        add(principal, BorderLayout.CENTER);
    }

    private JPanel crearBarraLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(NAV);
        lateral.setPreferredSize(new Dimension(248, 700));
        lateral.setBorder(BorderFactory.createEmptyBorder(22, 16, 18, 16));

        JLabel marca = new JLabel("  INVENTARIO");
        marca.setForeground(Color.WHITE);
        marca.setFont(marca.getFont().deriveFont(Font.BOLD, 19f));
        marca.setAlignmentX(LEFT_ALIGNMENT);
        lateral.add(marca);
        lateral.add(Box.createVerticalStrut(23));

        JPanel perfil = new JPanel(new BorderLayout(10, 0));
        perfil.setBackground(NAV_LIGHT);
        perfil.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 8));
        perfil.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        JLabel avatar = new JLabel(iniciales(usuario), SwingConstants.CENTER);
        avatar.setOpaque(true);
        avatar.setBackground(new Color(74, 104, 164));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(avatar.getFont().deriveFont(Font.BOLD, 15f));
        avatar.setPreferredSize(new Dimension(42, 42));
        JLabel nombre = new JLabel("<html><b>" + escapar(usuario) + "</b><br>"
                + escapar(rol) + "</html>");
        nombre.setForeground(Color.WHITE);
        perfil.add(avatar, BorderLayout.WEST);
        perfil.add(nombre, BorderLayout.CENTER);
        lateral.add(perfil);
        lateral.add(Box.createVerticalStrut(24));

        JLabel modulos = new JLabel("MÓDULOS");
        modulos.setForeground(new Color(154, 169, 191));
        modulos.setFont(modulos.getFont().deriveFont(Font.BOLD, 11f));
        modulos.setBorder(BorderFactory.createEmptyBorder(0, 8, 9, 0));
        modulos.setAlignmentX(LEFT_ALIGNMENT);
        lateral.add(modulos);

        JPanel navegacion = new JPanel();
        navegacion.setLayout(new BoxLayout(navegacion, BoxLayout.Y_AXIS));
        navegacion.setBackground(NAV);
        for (int i = 0; i < MODULES.length; i++) {
            String modulo = MODULES[i];
            JButton boton = crearBotonModulo(String.format("%02d", i), modulo);
            botonesModulo.put(modulo, boton);
            navegacion.add(boton);
            navegacion.add(Box.createVerticalStrut(4));
        }
        JScrollPane scroll = new JScrollPane(navegacion);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        lateral.add(scroll);
        return lateral;
    }

    private JButton crearBotonModulo(String numero, String modulo) {
        JButton boton = new JButton("  " + numero + "   " + modulo);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setForeground(new Color(218, 226, 239));
        boton.setBackground(NAV);
        boton.setFont(boton.getFont().deriveFont(13f));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(11, 7, 11, 7));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        boton.setAlignmentX(LEFT_ALIGNMENT);
        boton.addActionListener(evento -> mostrarModulo(modulo));
        return boton;
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(14, 24, 14, 22)));

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        tituloSeccion.setForeground(TEXT);
        tituloSeccion.setFont(tituloSeccion.getFont().deriveFont(Font.BOLD, 18f));
        JLabel ruta = new JLabel("Inventario  /  Gestión");
        ruta.setForeground(MUTED);
        ruta.setFont(ruta.getFont().deriveFont(11f));
        titulos.add(tituloSeccion);
        titulos.add(Box.createVerticalStrut(3));
        titulos.add(ruta);
        barra.add(titulos, BorderLayout.WEST);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acciones.setOpaque(false);
        JLabel usuarioLabel = new JLabel(usuario + " · " + rol);
        usuarioLabel.setForeground(MUTED);
        JButton salir = new JButton("Cerrar sesión");
        salir.setFocusPainted(false);
        salir.addActionListener(evento -> {
            dispose();
            volverAlLogin.run();
        });
        acciones.add(usuarioLabel);
        acciones.add(salir);
        barra.add(acciones, BorderLayout.EAST);
        return barra;
    }

    private JPanel crearPanelPrincipal() {
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(BACKGROUND);
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 26, 26, 26));

        JLabel bienvenida = new JLabel("Resumen del inventario");
        bienvenida.setForeground(TEXT);
        bienvenida.setFont(bienvenida.getFont().deriveFont(Font.BOLD, 24f));
        JLabel subtitulo = new JLabel("Consulta el estado general y atiende las alertas pendientes.");
        subtitulo.setForeground(MUTED);
        subtitulo.setFont(subtitulo.getFont().deriveFont(13f));
        contenido.add(bienvenida);
        contenido.add(Box.createVerticalStrut(5));
        contenido.add(subtitulo);
        contenido.add(Box.createVerticalStrut(20));

        JPanel metricas = new JPanel(new GridLayout(1, 3, 14, 0));
        metricas.setOpaque(false);
        metricas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        metricas.add(crearTarjetaMetrica("Productos registrados", productosValor,
                "En el catálogo", BLUE));
        metricas.add(crearTarjetaMetrica("Stock bajo", stockBajoValor,
                "Requieren revisión", new Color(230, 151, 54)));
        metricas.add(crearTarjetaMetrica("Módulos", new JLabel("11"),
                "Gestión centralizada", new Color(47, 163, 117)));
        contenido.add(metricas);
        contenido.add(Box.createVerticalStrut(18));

        JPanel seccionAlertas = new JPanel(new BorderLayout(0, 12));
        seccionAlertas.setBackground(Color.WHITE);
        seccionAlertas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(17, 18, 16, 18)));
        seccionAlertas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 285));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel alertasTitulo = new JLabel("Alertas de inventario");
        alertasTitulo.setForeground(TEXT);
        alertasTitulo.setFont(alertasTitulo.getFont().deriveFont(Font.BOLD, 16f));
        JLabel etiqueta = new JLabel("STOCK");
        etiqueta.setForeground(new Color(174, 107, 30));
        etiqueta.setOpaque(true);
        etiqueta.setBackground(new Color(255, 244, 225));
        etiqueta.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));
        encabezado.add(alertasTitulo, BorderLayout.WEST);
        encabezado.add(etiqueta, BorderLayout.EAST);
        seccionAlertas.add(encabezado, BorderLayout.NORTH);

        listaAlertas.setLayout(new BoxLayout(listaAlertas, BoxLayout.Y_AXIS));
        listaAlertas.setBackground(Color.WHITE);
        JLabel cargando = new JLabel("Consultando el inventario...");
        cargando.setForeground(MUTED);
        cargando.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
        listaAlertas.add(cargando);
        seccionAlertas.add(listaAlertas, BorderLayout.CENTER);
        contenido.add(seccionAlertas);
        contenido.add(Box.createVerticalStrut(18));

        JPanel accesos = new JPanel(new BorderLayout(0, 11));
        accesos.setOpaque(false);
        JLabel accesosTitulo = new JLabel("Accesos rápidos");
        accesosTitulo.setForeground(TEXT);
        accesosTitulo.setFont(accesosTitulo.getFont().deriveFont(Font.BOLD, 16f));
        accesos.add(accesosTitulo, BorderLayout.NORTH);
        JPanel botones = new JPanel(new GridLayout(1, 3, 12, 0));
        botones.setOpaque(false);
        botones.add(crearAcceso("Productos", PRODUCTS));
        botones.add(crearAcceso("Proveedores", "Proveedores"));
        botones.add(crearAcceso("Registrar entrada", "Entradas y lotes"));
        accesos.add(botones, BorderLayout.CENTER);
        contenido.add(accesos);

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setBackground(BACKGROUND);
        envoltura.add(scroll, BorderLayout.CENTER);
        return envoltura;
    }

    private JPanel crearTarjetaMetrica(String titulo, JLabel valor, String nota, Color acento) {
        JPanel tarjeta = new JPanel(new BorderLayout(12, 0));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(13, 15, 13, 15)));
        JPanel barra = new JPanel();
        barra.setBackground(acento);
        barra.setPreferredSize(new Dimension(4, 60));
        tarjeta.add(barra, BorderLayout.WEST);

        JPanel texto = new JPanel();
        texto.setOpaque(false);
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));
        JLabel nombre = new JLabel(titulo);
        nombre.setForeground(MUTED);
        nombre.setFont(nombre.getFont().deriveFont(12f));
        valor.setForeground(TEXT);
        valor.setFont(valor.getFont().deriveFont(Font.BOLD, 23f));
        JLabel descripcion = new JLabel(nota);
        descripcion.setForeground(MUTED);
        descripcion.setFont(descripcion.getFont().deriveFont(11f));
        texto.add(nombre);
        texto.add(Box.createVerticalStrut(5));
        texto.add(valor);
        texto.add(Box.createVerticalStrut(2));
        texto.add(descripcion);
        tarjeta.add(texto, BorderLayout.CENTER);
        return tarjeta;
    }

    private JButton crearAcceso(String titulo, String modulo) {
        JButton boton = new JButton("  " + titulo + "   →");
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setBackground(Color.WHITE);
        boton.setForeground(TEXT);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 8)));
        boton.setFocusPainted(false);
        boton.addActionListener(evento -> mostrarModulo(modulo));
        return boton;
    }

    private JPanel crearPlaceholder(String modulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 28, 28));
        JPanel tarjeta = new JPanel();
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(35, 34, 35, 34)));

        JLabel icono = new JLabel("▦");
        icono.setForeground(BLUE);
        icono.setFont(icono.getFont().deriveFont(Font.BOLD, 38f));
        icono.setAlignmentX(LEFT_ALIGNMENT);
        JLabel titulo = new JLabel(modulo);
        titulo.setForeground(TEXT);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 25f));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel descripcion = new JLabel("Este espacio reunirá las herramientas de "
                + modulo.toLowerCase() + ".");
        descripcion.setForeground(MUTED);
        descripcion.setFont(descripcion.getFont().deriveFont(14f));
        descripcion.setAlignmentX(LEFT_ALIGNMENT);
        JLabel estado = new JLabel("MÓDULO EN PREPARACIÓN");
        estado.setOpaque(true);
        estado.setBackground(new Color(236, 241, 250));
        estado.setForeground(new Color(67, 91, 135));
        estado.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        estado.setAlignmentX(LEFT_ALIGNMENT);
        JButton volver = new JButton("Volver al panel principal");
        volver.setAlignmentX(LEFT_ALIGNMENT);
        volver.setFocusPainted(false);
        volver.addActionListener(evento -> mostrarModulo(HOME));

        tarjeta.add(icono);
        tarjeta.add(Box.createVerticalStrut(15));
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(8));
        tarjeta.add(descripcion);
        tarjeta.add(Box.createVerticalStrut(20));
        tarjeta.add(estado);
        tarjeta.add(Box.createVerticalStrut(24));
        tarjeta.add(volver);
        panel.add(tarjeta, BorderLayout.NORTH);
        return panel;
    }

    private void mostrarModulo(String modulo) {
        JPanel pagina = paginasCreadas.get(modulo);
        if (pagina == null) {
            if (PRODUCTS.equals(modulo)) {
                pagina = new ProductoFrame();
            } else {
                pagina = crearPlaceholder(modulo);
            }
            paginasCreadas.put(modulo, pagina);
            paginas.add(pagina, modulo);
        }
        cardLayout.show(paginas, modulo);
        tituloSeccion.setText(modulo);
        setTitle("Inventario | " + modulo);
        botonesModulo.forEach((nombre, boton) -> {
            boolean seleccionado = nombre.equals(modulo);
            boton.setBackground(seleccionado ? BLUE : NAV);
            boton.setForeground(Color.WHITE);
        });
    }

    private void cargarResumen() {
        new SwingWorker<List<Producto>, Void>() {
            @Override
            protected List<Producto> doInBackground() throws Exception {
                return new ProductoDAO().listarTodos();
            }

            @Override
            protected void done() {
                try {
                    List<Producto> productos = get();
                    productosValor.setText(Integer.toString(productos.size()));
                    List<Producto> bajoStock = new ArrayList<>();
                    for (Producto producto : productos) {
                        if (producto.getStockInventario() <= producto.getStockMinimo()) {
                            bajoStock.add(producto);
                        }
                    }
                    stockBajoValor.setText(Integer.toString(bajoStock.size()));
                    mostrarAlertas(bajoStock);
                } catch (Exception e) {
                    productosValor.setText("—");
                    stockBajoValor.setText("—");
                    listaAlertas.removeAll();
                    JLabel error = new JLabel("No se pudo consultar el inventario. Revisa la conexión.");
                    error.setForeground(MUTED);
                    error.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
                    listaAlertas.add(error);
                    listaAlertas.revalidate();
                    listaAlertas.repaint();
                }
            }
        }.execute();
    }

    private void mostrarAlertas(List<Producto> productos) {
        listaAlertas.removeAll();
        if (productos.isEmpty()) {
            JLabel ok = new JLabel("✓   No hay productos por debajo del stock mínimo.");
            ok.setForeground(new Color(42, 131, 94));
            ok.setBorder(BorderFactory.createEmptyBorder(9, 0, 9, 0));
            listaAlertas.add(ok);
        } else {
            for (int i = 0; i < Math.min(4, productos.size()); i++) {
                Producto producto = productos.get(i);
                JPanel fila = new JPanel(new BorderLayout(12, 0));
                fila.setOpaque(false);
                fila.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
                JLabel nombre = new JLabel(producto.getDescripcion() + "  ·  " + producto.getCodigo());
                nombre.setForeground(TEXT);
                JLabel stock = new JLabel("Stock " + producto.getStockInventario()
                        + " / mínimo " + producto.getStockMinimo());
                stock.setForeground(new Color(174, 107, 30));
                fila.add(nombre, BorderLayout.CENTER);
                fila.add(stock, BorderLayout.EAST);
                fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 39));
                listaAlertas.add(fila);
            }
        }
        listaAlertas.revalidate();
        listaAlertas.repaint();
    }

    private static String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }
        return (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
