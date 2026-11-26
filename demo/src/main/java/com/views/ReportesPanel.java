package com.views;

import com.model.Producto;
import com.service.InventarioService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Resumen de existencias accesible como consulta para los roles autorizados. */
public class ReportesPanel extends JPanel {
    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color SUPERFICIE = new Color(30, 41, 59);
    private static final Color BORDE = new Color(51, 65, 85);
    private static final Color TEXTO = new Color(226, 232, 240);
    private static final InventarioService SERVICIO = new InventarioService();
    private final JLabel mensaje = new JLabel("Preparando reporte…");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Estado", "Productos", "Unidades"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };

    public ReportesPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(SUPERFICIE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel titulo = new JLabel("Reporte de existencias por estado");
        titulo.setForeground(TEXTO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        encabezado.add(titulo, BorderLayout.WEST);
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> cargar());
        encabezado.add(actualizar, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);
        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(30);
        tabla.setBackground(FONDO);
        tabla.setForeground(TEXTO);
        tabla.setGridColor(BORDE);
        tabla.getTableHeader().setBackground(SUPERFICIE);
        tabla.getTableHeader().setForeground(TEXTO);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(FONDO);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        add(scroll, BorderLayout.CENTER);
        mensaje.setForeground(new Color(148, 163, 184));
        add(mensaje, BorderLayout.SOUTH);
        cargar();
    }

    private void cargar() {
        mensaje.setText("Actualizando reporte…");
        new SwingWorker<Map<String, int[]>, Void>() {
            @Override protected Map<String, int[]> doInBackground() throws Exception {
                List<Producto> productos = SERVICIO.listarProductos();
                Map<String, int[]> resumen = new LinkedHashMap<>();
                for (Producto producto : productos) {
                    String estado = SERVICIO.obtenerEstadoVisible(producto);
                    int[] datos = resumen.computeIfAbsent(estado, ignorado -> new int[2]);
                    datos[0]++;
                    datos[1] += producto.getStockInventario();
                }
                return resumen;
            }
            @Override protected void done() {
                try {
                    Map<String, int[]> resumen = get();
                    modelo.setRowCount(0);
                    resumen.forEach((estado, datos) -> modelo.addRow(new Object[]{estado, datos[0], datos[1]}));
                    mensaje.setText("Estados incluidos: " + resumen.size());
                } catch (Exception error) {
                    mensaje.setText("No se pudo cargar el reporte. Revisa la conexión y vuelve a actualizar.");
                }
            }
        }.execute();
    }
}
