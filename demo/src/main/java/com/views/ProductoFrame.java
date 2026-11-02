package com.views;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.awt.GridLayout;
import java.util.List;
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
    private final JTextField estadoField = new JTextField("Activo");
    private final JTextField stockMinimoField = new JTextField();

    public ProductoFrame() {
        super(new String[]{
                "ID", "Código", "Precio", "Stock", "Descripción", "Estado", "Stock mínimo"
        });
        iniciarInterfaz();
    }

    @Override
    protected JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 10));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.add(new JLabel("Código:"));
        panel.add(codigoField);
        panel.add(new JLabel("Precio:"));
        panel.add(precioField);
        panel.add(new JLabel("Stock:"));
        panel.add(stockField);
        panel.add(new JLabel("Descripción:"));
        panel.add(descripcionField);
        panel.add(new JLabel("Estado:"));
        panel.add(estadoField);
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
                producto.getPrecio(),
                producto.getStockInventario(),
                producto.getDescripcion(),
                producto.getEstado(),
                producto.getStockMinimo()
        };
    }

    @Override
    protected void cargarFormulario(Producto producto) {
        codigoField.setText(producto.getCodigo());
        precioField.setText(Double.toString(producto.getPrecio()));
        stockField.setText(Integer.toString(producto.getStockInventario()));
        descripcionField.setText(producto.getDescripcion());
        estadoField.setText(producto.getEstado());
        stockMinimoField.setText(Integer.toString(producto.getStockMinimo()));
    }

    @Override
    protected Producto leerFormulario(int id) {
        String codigo = codigoField.getText().trim();
        String descripcion = descripcionField.getText().trim();
        String estado = estadoField.getText().trim();
        if (codigo.isEmpty() || descripcion.isEmpty() || estado.isEmpty()) {
            throw new IllegalArgumentException("Código, descripción y estado son obligatorios.");
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
        estadoField.setText("Activo");
        stockMinimoField.setText("");
        codigoField.requestFocusInWindow();
    }
}
