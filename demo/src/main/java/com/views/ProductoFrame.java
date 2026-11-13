package com.views;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ProductoFrame extends AbstractCrudFrame<Producto> {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final JTextField codigoField = new JTextField();
    private final JTextField precioField = new JTextField();
    private final JTextField stockField = new JTextField();
    private final JTextField descripcionField = new JTextField();
    private final JComboBox<String> estadoCombo =
            new JComboBox<>(new String[]{"Activo", "Disponible", "Poco stock"});
    private final JTextField stockMinimoField = new JTextField();

    public ProductoFrame() {
        this(() -> { });
    }

    public ProductoFrame(Runnable cerrarSesion) {
        super("Catálogo de inventario", new String[]{
                "ID", "Código", "Nombre del producto", "Precio", "Stock actual", "Estado", "Stock mínimo"
        }, cerrarSesion);
        configurarAnchosColumnas(45, 85, 190, 80, 95, 105, 100);
        iniciarInterfaz();
    }

    @Override
    protected JPanel crearFormulario() {
        // Bloque de formulario en una columna para mostrar cada etiqueta completa.
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Código:"));
        panel.add(codigoField);
        panel.add(new JLabel("Nombre del producto:"));
        panel.add(descripcionField);
        panel.add(new JLabel("Precio:"));
        panel.add(precioField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockField);
        panel.add(new JLabel("Estado:"));
        panel.add(estadoCombo);
        panel.add(new JLabel("Stock mínimo:"));
        panel.add(stockMinimoField);
        return panel;
    }

    @Override
    protected List<Producto> listarRegistros() throws Exception {
        return productoDAO.listarTodos();
    }

    @Override
    protected Object[] convertirFila(Producto producto) {
        return new Object[]{
                producto.getIdProducto(),
                producto.getCodigo(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStockInventario(),
                producto.getEstado(),
                producto.getStockMinimo()
        };
    }

    @Override
    protected void cargarFormulario(Producto producto) {
        codigoField.setText(producto.getCodigo());
        descripcionField.setText(producto.getDescripcion());
        precioField.setText(Double.toString(producto.getPrecio()));
        stockField.setText(Integer.toString(producto.getStockInventario()));
        seleccionarEstado(producto.getEstado());
        stockMinimoField.setText(Integer.toString(producto.getStockMinimo()));
    }

    @Override
    protected Producto leerFormulario(int id) {
        String codigo = codigoField.getText().trim();
        String descripcion = descripcionField.getText().trim();
        String estado = (String) estadoCombo.getSelectedItem();
        if (codigo.isEmpty() || descripcion.isEmpty() || estado == null || estado.isEmpty()) {
            throw new IllegalArgumentException("Código, nombre del producto y estado son obligatorios.");
        }
        try {
            new BigDecimal(codigo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El código debe ser numérico porque así está definido en la base de datos.", e);
        }

        double precio;
        int stock;
        int stockMinimo;
        try {
            precio = Double.parseDouble(precioField.getText().trim().replace(',', '.'));
            stock = Integer.parseInt(stockField.getText().trim());
            stockMinimo = Integer.parseInt(stockMinimoField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Precio debe ser decimal y stock/stock mínimo deben ser números enteros.", e);
        }
        if (!Double.isFinite(precio) || precio < 0 || stock < 0 || stockMinimo < 0) {
            throw new IllegalArgumentException("Precio y existencias no pueden ser negativos.");
        }

        return new Producto(id, codigo, precio, stock, descripcion, estado, stockMinimo);
    }

    // Bloque de selección que mantiene visible el estado guardado, incluso si es un valor antiguo.
    private void seleccionarEstado(String valor) {
        if (valor == null) {
            estadoCombo.setSelectedItem("Activo");
            return;
        }
        for (int i = 0; i < estadoCombo.getItemCount(); i++) {
            if (estadoCombo.getItemAt(i).equalsIgnoreCase(valor)) {
                estadoCombo.setSelectedIndex(i);
                return;
            }
        }
        estadoCombo.addItem(valor);
        estadoCombo.setSelectedItem(valor);
    }

    @Override
    protected void insertar(Producto producto) throws Exception {
        if (!productoDAO.agregar(producto)) {
            throw new IllegalStateException("La base de datos no insertó el producto.");
        }
    }

    @Override
    protected boolean persistirActualizacion(Producto producto) throws Exception {
        return productoDAO.actualizar(producto);
    }

    @Override
    protected boolean eliminarRegistro(int id) throws Exception {
        return productoDAO.eliminar(id);
    }

    @Override
    protected int obtenerId(Producto producto) {
        return producto.getIdProducto();
    }

    @Override
    protected void limpiarCampos() {
        codigoField.setText("");
        precioField.setText("");
        stockField.setText("");
        descripcionField.setText("");
        estadoCombo.setSelectedItem("Activo");
        stockMinimoField.setText("");
        codigoField.requestFocusInWindow();
    }
}
