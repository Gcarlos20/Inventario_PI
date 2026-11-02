package com.inventario_pi;

import com.views.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Swing conserva su estilo predeterminado si no puede cargar el del sistema.
            }
            new LoginFrame().setVisible(true);
        });
    }
}

