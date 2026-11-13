package com.views;

import com.Connection.ConexionDatabase;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Acceso local sencillo mientras el proyecto no cuenta con usuarios en la base de datos. */
public class LoginFrame extends JFrame {
    private static final Color NAVY = new Color(20, 43, 78);
    private static final Color BLUE = new Color(38, 105, 207);
    private final JTextField usuarioField = new JTextField();
    private final JPasswordField claveField = new JPasswordField();

    public LoginFrame() {
        super("Mar de Inventario · Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(650, 430));
        setSize(760, 500);
        setLocationRelativeTo(null);
        construirInterfaz();
    }

    private void construirInterfaz() {
        // Bloque visual de marca: azul profundo y textos claros para separar las dos áreas.
        JPanel contenido = new JPanel(new GridLayout(1, 2));
        JPanel marca = new JPanel(new BorderLayout(8, 14));
        marca.setBackground(NAVY);
        marca.setBorder(BorderFactory.createEmptyBorder(38, 30, 38, 30));
        JLabel simbolo = new JLabel("🦐", SwingConstants.CENTER);
        simbolo.setFont(simbolo.getFont().deriveFont(62f));
        JLabel marcaTitulo = new JLabel("InvenTech", SwingConstants.CENTER);
        marcaTitulo.setForeground(Color.WHITE);
        marcaTitulo.setFont(marcaTitulo.getFont().deriveFont(Font.BOLD, 24f));
        JLabel marcaTexto = new JLabel("Control claro para cada ingrediente", SwingConstants.CENTER);
        marcaTexto.setForeground(new Color(210, 225, 248));
        JPanel identidad = new JPanel(new GridLayout(2, 1, 0, 5));
        identidad.setOpaque(false);
        identidad.add(marcaTitulo);
        identidad.add(marcaTexto);
        marca.add(simbolo, BorderLayout.CENTER);
        marca.add(identidad, BorderLayout.SOUTH);
        contenido.add(marca);

        // Bloque visual de acceso: superficie clara, encabezado contrastante y campos definidos.
        JPanel accesoPanel = new JPanel(new BorderLayout(12, 18));
        accesoPanel.setBackground(new Color(243, 247, 253));
        accesoPanel.setBorder(BorderFactory.createEmptyBorder(42, 34, 32, 34));
        JLabel titulo = new JLabel("Bienvenido", SwingConstants.LEFT);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 25f));
        titulo.setForeground(new Color(31, 55, 94));
        accesoPanel.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(4, 1, 6, 8));
        campos.setOpaque(false);
        JLabel usuarioLabel = new JLabel("Usuario");
        usuarioLabel.setForeground(new Color(61, 76, 101));
        campos.add(usuarioLabel);
        estilizarCampo(usuarioField);
        campos.add(usuarioField);
        JLabel claveLabel = new JLabel("Contraseña");
        claveLabel.setForeground(new Color(61, 76, 101));
        campos.add(claveLabel);
        estilizarCampo(claveField);
        campos.add(claveField);
        accesoPanel.add(campos, BorderLayout.CENTER);

        JButton entrar = new JButton("Ingresar");
        entrar.setBackground(BLUE);
        entrar.setForeground(Color.WHITE);
        entrar.setOpaque(true);
        entrar.setFocusPainted(false);
        entrar.setBorderPainted(false);
        entrar.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
        entrar.setFont(entrar.getFont().deriveFont(Font.BOLD, 14f));
        entrar.addActionListener(evento -> iniciarSesion());
        claveField.addActionListener(evento -> iniciarSesion());
        JPanel pie = new JPanel(new BorderLayout(0, 8));
        JLabel acceso = new JLabel("Acceso inicial: admin / admin", SwingConstants.CENTER);
        acceso.setForeground(new java.awt.Color(100, 100, 100));
        pie.add(acceso, BorderLayout.NORTH);
        pie.add(entrar, BorderLayout.SOUTH);
        accesoPanel.add(pie, BorderLayout.SOUTH);
        contenido.add(accesoPanel);
        getRootPane().setDefaultButton(entrar);
        add(contenido);
    }

    // Bloque de estilo compartido para que ambos campos mantengan el mismo contraste y espaciado.
    private void estilizarCampo(JTextField campo) {
        campo.setFont(campo.getFont().deriveFont(14f));
        campo.setBackground(Color.WHITE);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(197, 211, 232)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
    }

    // Bloque de código para iniciar sesión y validar credenciales.
    private void iniciarSesion() {
        String usuario = usuarioField.getText().trim();
        String clave = new String(claveField.getPassword());
        if ("admin".equals(usuario) && "admin".equals(clave)) {
            if (!solicitarPasswordSupabase()) {
                return;
            }
            new MainFrame(() -> {
                ConexionDatabase.configurarPasswordSesion(null);
                new LoginFrame().setVisible(true);
            }).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos.", "Acceso denegado", JOptionPane.WARNING_MESSAGE);
            claveField.setText("");
            claveField.requestFocusInWindow();
        }
    }

    private boolean solicitarPasswordSupabase() {
        try {
            if (ConexionDatabase.tienePasswordConfigurada()) {
                return true;
            }
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Error de configuración",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        JPasswordField passwordField = new JPasswordField(20);
        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.add(new JLabel("Contraseña de la base de datos Supabase:"), BorderLayout.NORTH);
        contenido.add(passwordField, BorderLayout.CENTER);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                contenido,
                "Conexión a Supabase",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return false;
        }

        char[] password = passwordField.getPassword();
        try {
            if (password.length == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "La contraseña de Supabase no puede estar vacía.",
                        "Contraseña requerida",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
            ConexionDatabase.configurarPasswordSesion(new String(password));
            return true;
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
