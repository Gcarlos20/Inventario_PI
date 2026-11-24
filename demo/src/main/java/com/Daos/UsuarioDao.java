package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Rol;
import com.model.Usuario;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acceso a usuarios habilitados en la base de datos. */
public class UsuarioDao {
    private static final String FORMATO_HASH = "$pbkdf2-sha256$";
    private static final int ITERACIONES = 210_000;
    private static final int TAMANO_HASH_BITS = 256;
    private static final int TAMANO_SALT_BYTES = 16;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    /** Registra una cuenta nueva con un rol de operación no administrativo. */
    public void registrar(String nombre, String apellido, String correo, char[] clave) throws SQLException {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()
                || correo == null || correo.isBlank() || clave == null || clave.length < 8) {
            throw new IllegalArgumentException("Completa todos los campos y usa una contraseña de al menos 8 caracteres.");
        }

        char[] claveTemporal = Arrays.copyOf(clave, clave.length);
        String hash;
        try {
            hash = hashClave(claveTemporal);
        } finally {
            Arrays.fill(claveTemporal, '\0');
        }

        String sqlRol = "SELECT \"Id_Rol\" FROM public.\"ROLES\" "
                + "WHERE UPPER(BTRIM(COALESCE(\"Estado\", ''))) = 'ACTIVO' "
                + "AND LOWER(BTRIM(\"Nombre\")) IN ('usuario', 'empleado', 'almacenista', 'auxiliar', 'operador') "
                + "ORDER BY CASE LOWER(BTRIM(\"Nombre\")) WHEN 'usuario' THEN 1 WHEN 'empleado' THEN 2 "
                + "WHEN 'almacenista' THEN 3 WHEN 'auxiliar' THEN 4 ELSE 5 END, \"Id_Rol\" LIMIT 1";
        String sqlDuplicado = "SELECT 1 FROM public.\"USUARIO\" WHERE LOWER(BTRIM(\"Correo\")) = LOWER(BTRIM(?))";
        String sqlInsertar = "INSERT INTO public.\"USUARIO\" "
                + "(\"Nombre\", \"Apellido\", \"Correo\", \"Contraseña\", \"Id_Rol\", \"Estado\") "
                + "VALUES (?, ?, ?, ?, ?, 'Activo')";
        try (Connection conexion = ConexionDatabase.conectar()) {
            int idRol;
            try (PreparedStatement consulta = conexion.prepareStatement(sqlRol);
                 ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("No existe un rol activo de usuario/empleado para asignar las cuentas nuevas.");
                }
                idRol = resultado.getInt("Id_Rol");
            }
            try (PreparedStatement consulta = conexion.prepareStatement(sqlDuplicado)) {
                consulta.setString(1, correo.trim());
                try (ResultSet resultado = consulta.executeQuery()) {
                    if (resultado.next()) throw new SQLException("Ya existe una cuenta registrada con ese correo.");
                }
            }
            try (PreparedStatement consulta = conexion.prepareStatement(sqlInsertar)) {
                consulta.setString(1, nombre.trim());
                consulta.setString(2, apellido.trim());
                consulta.setString(3, correo.trim());
                consulta.setString(4, hash);
                consulta.setInt(5, idRol);
                consulta.executeUpdate();
            }
        }
    }

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
                if (!resultado.next()) return null;
                String claveGuardada = resultado.getString("Contraseña");
                if (!coincideClave(claveGuardada, clave)) return null;
                if (claveGuardada != null && !claveGuardada.startsWith(FORMATO_HASH)) {
                    actualizarHash(conexion, resultado.getInt("Id_Usuario"), clave);
                }
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
        if (guardada.startsWith(FORMATO_HASH)) return verificarHash(guardada, ingresada);
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

    private void actualizarHash(Connection conexion, int idUsuario, char[] clave) throws SQLException {
        String sql = "UPDATE public.\"USUARIO\" SET \"Contraseña\" = ? WHERE \"Id_Usuario\" = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, hashClave(clave));
            consulta.setInt(2, idUsuario);
            consulta.executeUpdate();
        }
    }

    private String hashClave(char[] clave) {
        byte[] salt = new byte[TAMANO_SALT_BYTES];
        ALEATORIO.nextBytes(salt);
        byte[] hash = derivar(clave, salt, ITERACIONES);
        return FORMATO_HASH + ITERACIONES + "$"
                + Base64.getEncoder().withoutPadding().encodeToString(salt) + "$"
                + Base64.getEncoder().withoutPadding().encodeToString(hash);
    }

    private boolean verificarHash(String guardada, char[] ingresada) {
        try {
            String[] partes = guardada.split("\\$", -1);
            if (partes.length != 5 || !"pbkdf2-sha256".equals(partes[1])) return false;
            int iteraciones = Integer.parseInt(partes[2]);
            if (iteraciones < 1 || iteraciones > 2_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(partes[3]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[4]);
            if (salt.length < 8 || hashEsperado.length == 0) return false;
            return MessageDigest.isEqual(hashEsperado, derivar(ingresada, salt, iteraciones));
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    private byte[] derivar(char[] clave, byte[] salt, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(clave, salt, iteraciones, TAMANO_HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException("PBKDF2 no está disponible en esta instalación.", error);
        } finally {
            spec.clearPassword();
        }
    }

    private String hexadecimal(byte[] bytes) {
        StringBuilder salida = new StringBuilder(bytes.length * 2);
        for (byte valor : bytes) salida.append(String.format("%02x", valor & 0xff));
        return salida.toString();
    }
}
