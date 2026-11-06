package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {
// bloque de código para la clase ProductoDAO
    private static final String COLUMNAS =
            "\"Id_Producto\", \"Codigo\", \"Precio\", \"Stock_Inventario\", "
                    + "\"Descripcion\", \"Estado\", \"Stock_Minimo\"";

// bloque de codigo conectar a la base de datos y realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) para la entidad Producto
    private Connection obtenerConexion() throws SQLException {
        Connection conexion = ConexionDatabase.conectar();
        if (conexion == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        return conexion;
    }
// bloque de código para listar todos los productos
    public List<Producto> listarTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM \"PRODUCTOS\" ORDER BY \"Id_Producto\"";

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(mapear(rs));
            }
        }
        return productos;
    }
// bloque de código para obtener un producto por su ID
    public Producto obtenerPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM \"PRODUCTOS\" WHERE \"Id_Producto\" = ?";

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }
// bloque de código para agregar un nuevo producto
    public boolean agregar(Producto producto) throws SQLException {
        String sql = "INSERT INTO \"PRODUCTOS\" "
                + "(\"Codigo\", \"Precio\", \"Stock_Inventario\", \"Descripcion\", \"Estado\", \"Stock_Minimo\") "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBigDecimal(1, convertirCodigo(producto.getCodigo()));
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getStockInventario());
            ps.setString(4, producto.getDescripcion());
            ps.setString(5, producto.getEstado());
            ps.setInt(6, producto.getStockMinimo());
            return ps.executeUpdate() > 0;
        }
    }
// bloque de código para actualizar un producto existente
    public boolean actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE \"PRODUCTOS\" SET \"Codigo\" = ?, \"Precio\" = ?, \"Stock_Inventario\" = ?, "
                + "\"Descripcion\" = ?, \"Estado\" = ?, \"Stock_Minimo\" = ? "
                + "WHERE \"Id_Producto\" = ?";

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setBigDecimal(1, convertirCodigo(producto.getCodigo()));
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getStockInventario());
            ps.setString(4, producto.getDescripcion());
            ps.setString(5, producto.getEstado());
            ps.setInt(6, producto.getStockMinimo());
            ps.setInt(7, producto.getIdProducto());
            return ps.executeUpdate() > 0;
        }
    }
// bloque de código para eliminar un producto por su ID
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM \"PRODUCTOS\" WHERE \"Id_Producto\" = ?";

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
// bloque de código para mapear un ResultSet a un objeto Producto
    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("Id_Producto"),
                rs.getString("Codigo"),
                rs.getDouble("Precio"),
                rs.getInt("Stock_Inventario"),
                rs.getString("Descripcion"),
                rs.getString("Estado"),
                rs.getInt("Stock_Minimo"));
    }
    // bloque de código para convertir el código del producto a BigDecimal 
    private BigDecimal convertirCodigo(String codigo) throws SQLException {
        try {
            return new BigDecimal(codigo.trim());
        } catch (NumberFormatException e) {
            throw new SQLException("El código debe contener solo un valor numérico.", e);
        }
    }
}
