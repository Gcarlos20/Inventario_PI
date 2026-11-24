package com.views;

import com.Connection.ConexionDatabase;
import com.Daos.UsuarioDao;
import com.model.Usuario;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;

/** Autenticación contra las cuentas activas registradas en Supabase. */
public class LoginFrame extends JFrame {
    private static final Color NAVY = new Color(28, 39, 48);
    private static final Color ACCENT = new Color(151, 169, 157);
    private static final Color INK = new Color(42, 52, 57);
    private final JTextField usuarioField = new RoundedTextField();
    private final JPasswordField claveField = new RoundedPasswordField();
    private final JButton entrar = new JButton("Ingresar");
    private final JButton registrarse = new JButton("Crear cuenta");

    public LoginFrame() {
        super("Mar de Inventario · Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 480));
        setSize(820, 540);
        setLocationRelativeTo(null);
        construirInterfaz();
    }

    private void construirInterfaz() {
        // Panel de marca oscuro con una ilustración vectorial de ancla.
        JPanel contenido = new JPanel(new GridLayout(1, 2, 0, 0));
        JPanel marca = new JPanel(new BorderLayout(8, 14)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setPaint(new GradientPaint(0, 0, NAVY, getWidth(), getHeight(),
                        new Color(47, 62, 67)));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        marca.setBorder(BorderFactory.createEmptyBorder(38, 30, 38, 30));
        JPanel ancla = new AnchorIllustration();
        marca.add(ancla, BorderLayout.CENTER);

        JLabel marcaTitulo = new JLabel("InvenTech", SwingConstants.CENTER);
        marcaTitulo.setForeground(Color.WHITE);
        marcaTitulo.setFont(marcaTitulo.getFont().deriveFont(Font.BOLD, 24f));
        JLabel marcaTexto = new JLabel("Gestión segura, rumbo claro", SwingConstants.CENTER);
        marcaTexto.setForeground(new Color(210, 216, 212));
        JPanel identidad = new JPanel(new GridLayout(2, 1, 0, 7));
        identidad.setOpaque(false);
        identidad.add(marcaTitulo);
        identidad.add(marcaTexto);
        marca.add(identidad, BorderLayout.SOUTH);
        contenido.add(marca);

        // Formulario sobre una tarjeta clara; se conserva el flujo existente de autenticación.
        JPanel accesoPanel = new JPanel(new BorderLayout(12, 18));
        accesoPanel.setBackground(new Color(239, 238, 232));
        accesoPanel.setBorder(BorderFactory.createEmptyBorder(38, 34, 38, 34));

        JPanel tarjeta = new RoundedPanel(new Color(250, 249, 246), 24);
        tarjeta.setLayout(new BorderLayout(0, 22));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel encabezado = new JPanel(new GridLayout(2, 1, 0, 7));
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Bienvenido", SwingConstants.LEFT);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 26f));
        titulo.setForeground(INK);
        JLabel subtitulo = new JLabel("Ingresa tus datos para continuar", SwingConstants.LEFT);
        subtitulo.setForeground(new Color(112, 119, 116));
        encabezado.add(titulo);
        encabezado.add(subtitulo);
        tarjeta.add(encabezado, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(4, 1, 7, 9));
        campos.setOpaque(false);
        JLabel usuarioLabel = new JLabel("Correo electrónico");
        usuarioLabel.setForeground(new Color(77, 85, 82));
        campos.add(usuarioLabel);
        estilizarCampo(usuarioField);
        campos.add(usuarioField);
        JLabel claveLabel = new JLabel("Contraseña");
        claveLabel.setForeground(new Color(77, 85, 82));
        campos.add(claveLabel);
        estilizarCampo(claveField);
        campos.add(claveField);
        tarjeta.add(campos, BorderLayout.CENTER);

        entrar.setBackground(NAVY);
        entrar.setForeground(Color.WHITE);
        entrar.setOpaque(true);
        entrar.setFocusPainted(false);
        entrar.setBorderPainted(false);
        entrar.setBorder(BorderFactory.createEmptyBorder(13, 12, 13, 12));
        entrar.setFont(entrar.getFont().deriveFont(Font.BOLD, 14f));
        entrar.addActionListener(evento -> iniciarSesion());
        claveField.addActionListener(evento -> iniciarSesion());
        registrarse.setFocusPainted(false);
        registrarse.addActionListener(evento -> registrarUsuario());
        JPanel acciones = new JPanel(new GridLayout(2, 1, 0, 8));
        acciones.setOpaque(false);
        acciones.add(entrar);
        acciones.add(registrarse);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        accesoPanel.add(tarjeta, BorderLayout.CENTER);
        contenido.add(accesoPanel);
        getRootPane().setDefaultButton(entrar);
        add(contenido);
    }

    // Estilo común de los campos redondeados.
    private void estilizarCampo(JTextField campo) {
        campo.setFont(campo.getFont().deriveFont(14f));
        campo.setBackground(new Color(255, 255, 253));
        campo.setBorder(new RoundedBorder(new Color(204, 207, 199), 14));
    }

    private void iniciarSesion() {
        String correo = usuarioField.getText().trim();
        char[] clave = claveField.getPassword();
        if (correo.isBlank() || clave.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa el correo y la contraseña.", "Datos requeridos", JOptionPane.WARNING_MESSAGE);
            Arrays.fill(clave, '\0');
            return;
        }
        if (!solicitarPasswordSupabase()) {
            Arrays.fill(clave, '\0');
            return;
        }

        entrar.setEnabled(false);
        usuarioField.setEnabled(false);
        claveField.setEnabled(false);
        entrar.setText("Verificando…");
        new SwingWorker<Usuario, Void>() {
            @Override protected Usuario doInBackground() throws Exception {
                return new UsuarioDao().autenticar(correo, clave);
            }

            @Override protected void done() {
                Arrays.fill(clave, '\0');
                entrar.setEnabled(true);
                usuarioField.setEnabled(true);
                claveField.setEnabled(true);
                entrar.setText("Ingresar");
                try {
                    Usuario usuarioActivo = get();
                    if (usuarioActivo == null) {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Correo o contraseña incorrectos, o la cuenta está inactiva.",
                                "Acceso denegado", JOptionPane.WARNING_MESSAGE);
                        claveField.setText("");
                        claveField.requestFocusInWindow();
                        return;
                    }
                    new MainFrame(() -> {
                        ConexionDatabase.configurarPasswordSesion(null);
                        new LoginFrame().setVisible(true);
                    }, usuarioActivo).setVisible(true);
                    dispose();
                } catch (Exception error) {
                    Throwable causa = error instanceof java.util.concurrent.ExecutionException
                            && error.getCause() != null ? error.getCause() : error;
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "No se pudo validar el acceso en la base de datos. " + causa.getMessage(),
                            "Error de conexión", JOptionPane.ERROR_MESSAGE);
                    claveField.setText("");
                }
            }
        }.execute();
    }

    private void registrarUsuario() {
        JTextField nombre = new JTextField(20);
        JTextField apellido = new JTextField(20);
        JTextField correo = new JTextField(20);
        JPasswordField clave = new JPasswordField(20);
        JPasswordField confirmarClave = new JPasswordField(20);
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Apellido:"));
        formulario.add(apellido);
        formulario.add(new JLabel("Correo:"));
        formulario.add(correo);
        formulario.add(new JLabel("Contraseña (mínimo 8 caracteres):"));
        formulario.add(clave);
        formulario.add(new JLabel("Confirmar contraseña:"));
        formulario.add(confirmarClave);

        int opcion = JOptionPane.showConfirmDialog(this, formulario,
                "Crear cuenta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;

        char[] password = clave.getPassword();
        char[] confirmacion = confirmarClave.getPassword();
        if (!Arrays.equals(password, confirmacion)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.",
                    "Revisa los datos", JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            Arrays.fill(confirmacion, '\0');
            return;
        }
        if (correo.getText().isBlank() || !correo.getText().contains("@")) {
            JOptionPane.showMessageDialog(this, "Ingresa un correo válido.",
                    "Revisa los datos", JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            Arrays.fill(confirmacion, '\0');
            return;
        }
        Arrays.fill(confirmacion, '\0');
        if (!solicitarPasswordSupabase()) {
            Arrays.fill(password, '\0');
            return;
        }

        String nombreIngresado = nombre.getText().trim();
        String apellidoIngresado = apellido.getText().trim();
        String correoIngresado = correo.getText().trim();
        registrarse.setEnabled(false);
        entrar.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                new UsuarioDao().registrar(nombreIngresado, apellidoIngresado, correoIngresado, password);
                return null;
            }

            @Override protected void done() {
                Arrays.fill(password, '\0');
                registrarse.setEnabled(true);
                entrar.setEnabled(true);
                try {
                    get();
                    usuarioField.setText(correoIngresado);
                    claveField.setText("");
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Cuenta creada. Ya puedes ingresar con tu correo y contraseña.",
                            "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception error) {
                    Throwable causa = error instanceof java.util.concurrent.ExecutionException
                            && error.getCause() != null ? error.getCause() : error;
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "No se pudo crear la cuenta. " + causa.getMessage(),
                            "Error de registro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
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

    private static class RoundedTextField extends JTextField {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintRoundedBackground(graphics, this);
            super.paintComponent(graphics);
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintRoundedBackground(graphics, this);
            super.paintComponent(graphics);
        }
    }

    private static void paintRoundedBackground(Graphics graphics, JTextField field) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(field.getBackground());
        g.fillRoundRect(0, 0, field.getWidth() - 1, field.getHeight() - 1, 14, 14);
        g.dispose();
    }

    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(java.awt.Component component, Graphics graphics,
                int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g.dispose();
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component component) {
            return new java.awt.Insets(10, 12, 10, 12);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color color;
        private final int radius;

        RoundedPanel(Color color, int radius) {
            this.color = color;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class AnchorIllustration extends JPanel {
        AnchorIllustration() {
            setOpaque(false);
        }
// bloque de código para dibujar un ancla estilizada en el panel de marca.
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(ACCENT);
            g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g.drawOval(centerX - 12, centerY - 72, 24, 24);
            g.drawLine(centerX, centerY - 48, centerX, centerY + 45);
            g.drawLine(centerX - 28, centerY - 34, centerX + 28, centerY - 34);

            Path2D brazos = new Path2D.Double();
            brazos.moveTo(centerX - 36, centerY + 13);
            brazos.curveTo(centerX - 36, centerY + 43, centerX - 13, centerY + 57, centerX, centerY + 57);
            brazos.curveTo(centerX + 13, centerY + 57, centerX + 36, centerY + 43, centerX + 36, centerY + 13);
            g.draw(brazos);
            g.drawLine(centerX - 36, centerY + 13, centerX - 48, centerY + 26);
            g.drawLine(centerX + 36, centerY + 13, centerX + 48, centerY + 26);
            g.dispose();
        }
    }
}
