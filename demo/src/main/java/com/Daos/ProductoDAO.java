package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para gestionar los productos del inventario en la base de datos.
 */
public class ProductoDAO {

    private static final String COLUMNAS =
            "\"Id_Producto\", \"Codigo\", \"Precio\", \"Stock_Inventario\", "
                    + "\"Descripcion\", \"Estado\", \"Stock_Minimo\"";

    private final DataRepository repo = DataRepository.getInstance();

    private Connection obtenerConexion() throws SQLException {
        return ConexionDatabase.conectar();
    }

    // Lista todos los productos registrados
    public List<Producto> listarTodos() throws SQLException {
        if (!DataRepository.isSupabaseDisponible()) {
            return repo.listarProductos();
        }

        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM \"PRODUCTOS\" ORDER BY \"Id_Producto\"";

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(mapear(rs));
            }
            return productos.isEmpty() ? repo.listarProductos() : productos;
        } catch (Exception e) {
            return repo.listarProductos();
        }
    }

    // Busca un producto por su ID
    public Producto obtenerPorId(int id) throws SQLException {
        if (!DataRepository.isSupabaseDisponible()) {
            return repo.obtenerProductoPorId(id);
        }

        String sql = "SELECT " + COLUMNAS + " FROM \"PRODUCTOS\" WHERE \"Id_Producto\" = ?";

        try (Connection conexion = obtenerConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (Exception e) {
            return repo.obtenerProductoPorId(id);
        }
        return repo.obtenerProductoPorId(id);
    }

    // Busca y filtra productos por texto, categoría y estado usando un ciclo for sencillo
    public List<Producto> buscarYFiltrar(String texto, String categoria, String estado) throws SQLException {
        String filtro = texto != null ? texto.trim().toLowerCase() : "";
        List<Producto> resultado = new ArrayList<>();

        for (Producto p : listarTodos()) {
            boolean coincideCat = (categoria == null || "Todas".equalsIgnoreCase(categoria))
                    || (p.getCategoria() != null && p.getCategoria().equalsIgnoreCase(categoria));

            boolean coincideEstado = (estado == null || "Todos".equalsIgnoreCase(estado))
                    || (p.getEstado() != null && p.getEstado().equalsIgnoreCase(estado));

            boolean coincideTexto = filtro.isEmpty()
                    || (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(filtro))
                    || (p.getNombre() != null && p.getNombre().toLowerCase().contains(filtro))
                    || (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(filtro))
                    || (p.getNombreProveedor() != null && p.getNombreProveedor().toLowerCase().contains(filtro));

            if (coincideCat && coincideEstado && coincideTexto) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // Inserta un nuevo producto en el inventario y en Supabase
    public boolean agregar(Producto producto) throws SQLException {
        try {
            repo.guardarProducto(producto);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }

        if (!DataRepository.isSupabaseDisponible()) {
            return true;
        }

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
        } catch (Exception e) {
            return true;
        }
    }

    // Actualiza los datos de un producto existente
    public boolean actualizar(Producto producto) throws SQLException {
        try {
            repo.guardarProducto(producto);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }

        if (!DataRepository.isSupabaseDisponible()) {
            return true;
        }

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
        } catch (Exception e) {
            return true;
        }
    }

    // Elimina o inactiva un producto por su ID
    public boolean eliminar(int id) throws SQLException {
        boolean ok;
        try {
            ok = repo.eliminarProducto(id);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        }

        if (DataRepository.isSupabaseDisponible()) {
            String sql = "UPDATE \"PRODUCTOS\" SET \"Estado\" = 'Inactivo' WHERE \"Id_Producto\" = ?";
            try (Connection conexion = obtenerConexion();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (Exception e) {
            }
        }
        return ok;
    }

    // Convierte una fila de la base de datos en un objeto Producto
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

    // Convierte el código a formato numérico para la columna de PostgreSQL
    private BigDecimal convertirCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(codigo.trim());
        } catch (NumberFormatException e) {
            String digitos = codigo.replaceAll("[^0-9]", "");
            if (!digitos.isEmpty()) {
                return new BigDecimal(digitos);
            }
            throw new SQLException("El código debe contener números.", e);
        }
    }
}
