package com.views;

import com.Connection.ConexionDatabase;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Consulta de solo lectura para módulos que muestran registros históricos. */
public class ConsultaRegistrosPanel extends JPanel {
    public enum Tipo { COMPRAS, MERMAS, HISTORIAL }

    private static final Color FONDO = new Color(15, 23, 42);
    private static final Color SUPERFICIE = new Color(30, 41, 59);
    private static final Color BORDE = new Color(51, 65, 85);
    private static final Color TEXTO = new Color(226, 232, 240);
    private static final Color SUAVE = new Color(148, 163, 184);
    private final Tipo tipo;
    private final DefaultTableModel modelo = new DefaultTableModel() {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel mensaje = new JLabel("Cargando registros…");

    public ConsultaRegistrosPanel(Tipo tipo) {
        this.tipo = tipo;
        setLayout(new BorderLayout(0, 10));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        JPanel encabezado = new JPanel(new BorderLayout(10, 0));
        encabezado.setBackground(SUPERFICIE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel titulo = new JLabel(titulo());
        titulo.setForeground(TEXTO);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        encabezado.add(titulo, BorderLayout.WEST);
        JButton actualizar = new JButton("Actualizar");
        actualizar.addActionListener(e -> cargar());
        encabezado.add(actualizar, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(28);
        tabla.setBackground(FONDO);
        tabla.setForeground(TEXTO);
        tabla.setGridColor(BORDE);
        tabla.setSelectionBackground(new Color(30, 64, 112));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.getTableHeader().setBackground(SUPERFICIE);
        tabla.getTableHeader().setForeground(TEXTO);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(FONDO);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        add(scroll, BorderLayout.CENTER);
        mensaje.setForeground(SUAVE);
        add(mensaje, BorderLayout.SOUTH);
        cargar();
    }

    private String titulo() {
        switch (tipo) {
            case COMPRAS: return "Compras registradas · solo consulta";
            case MERMAS: return "Mermas y bajas registradas · solo consulta";
            default: return "Historial de movimientos · solo consulta";
        }
    }

    private String consultaSql() {
        switch (tipo) {
            case COMPRAS:
                return "SELECT c.\"Fecha_Compras\", c.\"Numero_Factura\", p.\"Nombre_Empresa\", "
                        + "c.\"SubTotal\", c.\"TotalCompra\", c.\"Metodo_Pago\", c.\"Estado\", c.\"Observacion\" "
                        + "FROM public.\"COMPRAS\" c LEFT JOIN public.\"PROVEEDORES\" p "
                        + "ON p.\"Id_Proveedores\" = c.\"Id_Proveedores\" ORDER BY c.\"Fecha_Compras\" DESC";
            case MERMAS:
                return movimientosSql() + " WHERE m.\"Id_Tipo_Movimiento\" IN (5, 6)";
            default:
                return movimientosSql();
        }
    }

    private String movimientosSql() {
        return "SELECT m.\"Fecha_Movimiento\", m.\"Tipo_Movimiento\", p.\"Codigo\", "
                + "p.\"Descripcion\", m.\"Cantidad\", m.\"Stock_Anterior\", m.\"Stock_Nuevo\", "
                + "m.\"Motivo\", COALESCE(u.\"Nombre\", '') || ' ' || COALESCE(u.\"Apellido\", '') AS \"Usuario\" "
                + "FROM public.\"MOVIMIENTOS\" m LEFT JOIN public.\"PRODUCTOS\" p "
                + "ON p.\"Id_producto\" = m.\"Id_Producto\" LEFT JOIN public.\"USUARIO\" u "
                + "ON u.\"Id_Usuario\" = m.\"Id_Usuario\"";
    }

    private void cargar() {
        mensaje.setText("Cargando registros…");
        new SwingWorker<Object[][], Void>() {
            private String[] columnas;
            @Override protected Object[][] doInBackground() throws Exception {
                try (Connection conexion = ConexionDatabase.conectar();
                     PreparedStatement consulta = conexion.prepareStatement(consultaSql());
                     ResultSet resultado = consulta.executeQuery()) {
                    ResultSetMetaData meta = resultado.getMetaData();
                    columnas = new String[meta.getColumnCount()];
                    for (int i = 0; i < columnas.length; i++) columnas[i] = meta.getColumnLabel(i + 1);
                    java.util.List<Object[]> filas = new java.util.ArrayList<>();
                    while (resultado.next()) {
                        Object[] fila = new Object[columnas.length];
                        for (int i = 0; i < fila.length; i++) fila[i] = resultado.getObject(i + 1);
                        filas.add(fila);
                    }
                    return filas.toArray(new Object[0][]);
                }
            }
            @Override protected void done() {
                try {
                    Object[][] filas = get();
                    modelo.setDataVector(filas, columnas);
                    mensaje.setText("Registros encontrados: " + filas.length);
                } catch (Exception error) {
                    Throwable causa = error instanceof ExecutionException && error.getCause() != null
                            ? error.getCause() : error;
                    mensaje.setText("No se pudieron cargar los registros: " + causa.getMessage());
                }
            }
        }.execute();
    }
}
