package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Clase DAO para gestionar las operaciones de base de datos de los usuarios.
 */
public class UsuarioDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Valida el usuario, la contraseña y el rol al iniciar sesión
    public Usuario autenticar(String username, String passwordPlano, String rolEsperado) throws Exception {
        return repo.autenticar(username, passwordPlano, rolEsperado);
    }

    // Lista todos los usuarios registrados
    public List<Usuario> listarTodos() {
        return repo.listarUsuarios();
    }

    // Busca un usuario por su nombre de cuenta (username)
    public Usuario buscarPorUsername(String username) {
        return repo.buscarUsuarioPorUsername(username);
    }

    // Registra un nuevo usuario en el sistema y en la base de datos
    public boolean agregar(Usuario usuario) throws Exception {
        usuario.setContrasena(Usuario.asegurarHashSha256(usuario.getContrasena()));
        boolean guardado = repo.guardarUsuario(usuario);

        if (DataRepository.isSupabaseDisponible()) {
            String sql = "INSERT INTO \"USUARIO\" (\"Id_Usuario\", \"Nombre\", \"Apellido\", \"Documento\", "
                    + "\"Correo\", \"Telefono\", \"Contraseña\", \"Estado\", \"Id_Rol\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, usuario.getIdUsuario());
                ps.setString(2, usuario.getNombre());
                ps.setString(3, usuario.getApellido());
                ps.setString(4, usuario.getDocumento());
                ps.setString(5, usuario.getCorreo());
                ps.setString(6, usuario.getTelefono());
                ps.setString(7, usuario.getContrasena());
                ps.setBoolean(8, "Activo".equalsIgnoreCase(usuario.getEstado()));
                ps.setInt(9, usuario.getRol() != null ? usuario.getRol().getIdRol() : 2);
                ps.executeUpdate();
            } catch (SQLException e) {
                // Si falla la conexión remota, se conserva en el repositorio local
            }
        }
        return guardado;
    }

    // Actualiza los datos de un usuario existente
    public boolean actualizar(Usuario usuario) throws Exception {
        usuario.setContrasena(Usuario.asegurarHashSha256(usuario.getContrasena()));
        boolean actualizado = repo.guardarUsuario(usuario);

        if (DataRepository.isSupabaseDisponible()) {
            String sql = "UPDATE \"USUARIO\" SET \"Nombre\" = ?, \"Apellido\" = ?, \"Documento\" = ?, "
                    + "\"Correo\" = ?, \"Telefono\" = ?, \"Contraseña\" = ?, \"Estado\" = ?, \"Id_Rol\" = ? "
                    + "WHERE \"Id_Usuario\" = ?";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, usuario.getNombre());
                ps.setString(2, usuario.getApellido());
                ps.setString(3, usuario.getDocumento());
                ps.setString(4, usuario.getCorreo());
                ps.setString(5, usuario.getTelefono());
                ps.setString(6, usuario.getContrasena());
                ps.setBoolean(7, "Activo".equalsIgnoreCase(usuario.getEstado()));
                ps.setInt(8, usuario.getRol() != null ? usuario.getRol().getIdRol() : 2);
                ps.setInt(9, usuario.getIdUsuario());
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
        return actualizado;
    }

    // Cambia el estado del usuario a Inactivo
    public boolean eliminar(int idUsuario) throws Exception {
        boolean inactivado = repo.eliminarUsuario(idUsuario);

        if (DataRepository.isSupabaseDisponible()) {
            String sql = "UPDATE \"USUARIO\" SET \"Estado\" = false WHERE \"Id_Usuario\" = ?";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
        return inactivado;
    }
}
