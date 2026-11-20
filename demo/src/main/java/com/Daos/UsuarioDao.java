package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Rol;
import com.model.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acceso a usuarios habilitados en la base de datos. */
public class UsuarioDao {
    /** Autentica por correo, ya que la tabla USUARIO no tiene campo username. */
    public Usuario autenticar(String correo, char[] clave) throws SQLException {
        if (correo == null || correo.isBlank() || clave == null || clave.length == 0) return null;
        String sql = "SELECT u.\"Id_Usuario\", u.\"Nombre\", u.\"Apellido\", u.\"Correo\", "
                + "u.\"Contraseña\", u.\"Id_Rol\", r.\"Nombre\" AS \"Nombre_Rol\", "
                + "COALESCE(NULLIF(BTRIM(r.\"Descripcion\"), ''), r.\"Nombre\") AS \"Etiqueta_Rol\" "
                + "FROM public.\"USUARIO\" u JOIN public.\"ROLES\" r ON r.\"Id_Rol\" = u.\"Id_Rol\" "
                + "WHERE LOWER(BTRIM(u.\"Correo\")) = LOWER(BTRIM(?)) "
                + "AND UPPER(BTRIM(COALESCE(u.\"Estado\", ''))) = 'ACTIVO' "
                + "AND UPPER(BTRIM(COALESCE(r.\"Estado\", ''))) = 'ACTIVO'";
        try (Connection conexion = ConexionDatabase.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, correo.trim());
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next() || !coincideClave(resultado.getString("Contraseña"), clave)) return null;
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(resultado.getInt("Id_Usuario"));
                usuario.setNombre(resultado.getString("Nombre"));
                usuario.setApellido(resultado.getString("Apellido"));
                usuario.setCorreo(resultado.getString("Correo"));
                usuario.setUsername(resultado.getString("Correo"));
                Rol rol = new Rol();
                rol.setIdRol(resultado.getInt("Id_Rol"));
                rol.setNombre(resultado.getString("Etiqueta_Rol"));
                usuario.setRol(rol);
                return usuario;
            }
        }
    }

    private boolean coincideClave(String guardada, char[] ingresada) {
        if (guardada == null) return false;
        byte[] entrada = new String(ingresada).getBytes(StandardCharsets.UTF_8);
        byte[] valorGuardado = guardada.getBytes(StandardCharsets.UTF_8);
        if (MessageDigest.isEqual(valorGuardado, entrada)) return true;

        // Acepta contraseñas SHA-256 hexadecimales si alguna cuenta existente usa ese formato.
        if (guardada.matches("(?i)[0-9a-f]{64}")) {
            try {
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(entrada);
                return MessageDigest.isEqual(guardada.toLowerCase().getBytes(StandardCharsets.US_ASCII),
                        hexadecimal(hash).getBytes(StandardCharsets.US_ASCII));
            } catch (NoSuchAlgorithmException imposible) {
                throw new IllegalStateException("SHA-256 no está disponible en esta instalación.", imposible);
            }
        }
        return false;
    }

    private String hexadecimal(byte[] bytes) {
        StringBuilder salida = new StringBuilder(bytes.length * 2);
        for (byte valor : bytes) salida.append(String.format("%02x", valor & 0xff));
        return salida.toString();
    }
}
