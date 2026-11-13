package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas de acceso a datos para el catálogo de productos. */
public class ProductoDAO {
    // Se usa una sola lista de columnas para mantener iguales todas las lecturas del catálogo.
    private static final String COLUMNAS =
            "\"Id_Producto\", \"Codigo\", \"Precio\", \"Stock_Inventario\", "
                    + "\"Descripcion\", \"Estado\", \"Stock_Minimo\"";

    private Connection obtenerConexion() throws SQLException {
        Connection conexion = ConexionDatabase.conectar();
        if (conexion == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        return conexion;
    }

    /** Devuelve el catálogo ordenado por su identificador interno. */
    public List<Producto> listarTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM \"PRODUCTOS\" ORDER BY \"Id_Producto\"";

        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultados = consulta.executeQuery()) {
            while (resultados.next()) {
                productos.add(mapear(resultados));
            }
        }
        return productos;
    }

    /** Busca un producto por el ID interno numérico. */
    public Producto obtenerPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM \"PRODUCTOS\" WHERE \"Id_Producto\" = ?";

        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            try (ResultSet resultados = consulta.executeQuery()) {
                return resultados.next() ? mapear(resultados) : null;
            }
        }
    }

    /** Inserta un producto y devuelve el ID interno generado por PostgreSQL. */
    public int agregarYObtenerId(Producto producto) throws SQLException {
        String sql = "INSERT INTO \"PRODUCTOS\" "
                + "(\"Codigo\", \"Precio\", \"Stock_Inventario\", \"Descripcion\", \"Estado\", \"Stock_Minimo\") "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING \"Id_Producto\"";

        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, producto.getCodigo().trim());
            consulta.setDouble(2, producto.getPrecio());
            consulta.setInt(3, producto.getStockInventario());
            consulta.setString(4, producto.getDescripcion());
            consulta.setString(5, producto.getEstado());
            consulta.setInt(6, producto.getStockMinimo());
            try (ResultSet resultados = consulta.executeQuery()) {
                if (!resultados.next()) {
                    throw new SQLException("La base de datos no devolvió el ID del producto creado.");
                }
                return resultados.getInt(1);
            }
        }
    }

    /** Conserva el método booleano usado por las pantallas CRUD antiguas. */
    public boolean agregar(Producto producto) throws SQLException {
        return agregarYObtenerId(producto) > 0;
    }

    /** Actualiza todos los datos editables del producto indicado por su ID interno. */
    public boolean actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE \"PRODUCTOS\" SET \"Codigo\" = ?, \"Precio\" = ?, \"Stock_Inventario\" = ?, "
                + "\"Descripcion\" = ?, \"Estado\" = ?, \"Stock_Minimo\" = ? "
                + "WHERE \"Id_Producto\" = ?";

        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, producto.getCodigo().trim());
            consulta.setDouble(2, producto.getPrecio());
            consulta.setInt(3, producto.getStockInventario());
            consulta.setString(4, producto.getDescripcion());
            consulta.setString(5, producto.getEstado());
            consulta.setInt(6, producto.getStockMinimo());
            consulta.setInt(7, producto.getIdProducto());
            return consulta.executeUpdate() > 0;
        }
    }

    /** Elimina el producto identificado por su ID interno. */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM \"PRODUCTOS\" WHERE \"Id_Producto\" = ?";

        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            return consulta.executeUpdate() > 0;
        }
    }

    /** Convierte una fila SQL en el modelo usado por las pantallas. */
    private Producto mapear(ResultSet resultados) throws SQLException {
        return new Producto(
                resultados.getInt("Id_Producto"),
                resultados.getString("Codigo"),
                resultados.getDouble("Precio"),
                resultados.getInt("Stock_Inventario"),
                resultados.getString("Descripcion"),
                resultados.getString("Estado"),
                resultados.getInt("Stock_Minimo"));
    }
}
