package com.views;

import com.Connection.ConexionDatabase;
import java.awt.BorderLayout;
import java.awt.Dimension;
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
    private final JTextField usuarioField = new JTextField();
    private final JPasswordField claveField = new JPasswordField();

    public LoginFrame() {
        super("Inventario - Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(320, 300));
        setSize(400, 360);
        setLocationRelativeTo(null);
        construirInterfaz();
    }

    private void construirInterfaz() {
        JPanel contenido = new JPanel(new BorderLayout(12, 18));
        contenido.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        JLabel titulo = new JLabel("Inventario", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(24f));
        contenido.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(4, 1, 6, 8));
        campos.add(new JLabel("Usuario"));
        campos.add(usuarioField);
        campos.add(new JLabel("Contraseña"));
        campos.add(claveField);
        contenido.add(campos, BorderLayout.CENTER);

        JButton entrar = new JButton("Ingresar");
        entrar.addActionListener(evento -> iniciarSesion());
        claveField.addActionListener(evento -> iniciarSesion());
        JPanel pie = new JPanel(new BorderLayout(0, 8));
        JLabel acceso = new JLabel("Acceso inicial: admin / admin", SwingConstants.CENTER);
        acceso.setForeground(new java.awt.Color(100, 100, 100));
        pie.add(acceso, BorderLayout.NORTH);
        pie.add(entrar, BorderLayout.SOUTH);
        contenido.add(pie, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(entrar);
        add(contenido);
    }

    private void iniciarSesion() {
        String usuario = usuarioField.getText().trim();
        String clave = new String(claveField.getPassword());
        if ("admin".equals(usuario) && "admin".equals(clave)) {
            if (!solicitarPasswordSupabase()) {
                return;
            }
            new ProductoFrame(() -> {
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
