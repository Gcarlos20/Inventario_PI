package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.CategoriaProducto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Clase DAO para gestionar las categorías de los productos.
 */
public class CategoriaProductoDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Lista todas las categorías disponibles
    public List<CategoriaProducto> listarTodas() {
        return repo.listarCategorias();
    }

    // Agrega una nueva categoría en el sistema y en la base de datos
    public boolean agregar(CategoriaProducto categoria) {
        boolean ok = repo.guardarCategoria(categoria);
        if (DataRepository.isSupabaseDisponible()) {
            String sql = "INSERT INTO \"CATEGORIA_PRODUCTO\" (\"Id_Categoria\", \"Nombre\", \"Descripcion\", \"Estado\") VALUES (?, ?, ?, ?)";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, categoria.getIdCategoria());
                ps.setString(2, categoria.getNombre());
                ps.setString(3, categoria.getDescripcion());
                ps.setString(4, categoria.getEstado());
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
        return ok;
    }

    // Actualiza una categoría existente
    public boolean actualizar(CategoriaProducto categoria) {
        boolean ok = repo.guardarCategoria(categoria);
        if (DataRepository.isSupabaseDisponible()) {
            String sql = "UPDATE \"CATEGORIA_PRODUCTO\" SET \"Nombre\" = ?, \"Descripcion\" = ?, \"Estado\" = ? WHERE \"Id_Categoria\" = ?";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, categoria.getNombre());
                ps.setString(2, categoria.getDescripcion());
                ps.setString(3, categoria.getEstado());
                ps.setInt(4, categoria.getIdCategoria());
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
        return ok;
    }

    // Inactiva o elimina una categoría por su ID
    public boolean eliminar(int idCategoria) {
        return repo.eliminarCategoria(idCategoria);
    }
}
