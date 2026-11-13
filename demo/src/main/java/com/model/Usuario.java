package com.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * Entidad Usuario del sistema de inventario.
 * 
 * RF2 (autenticación y bloqueo tras 5 intentos fallidos por 15 minutos)
 * y RF19 (control de acceso por rol).
 */
public class Usuario {
    private int idUsuario;
    private String nombre;
    private String apellido;
    private String documento;
    private String correo;
    private String telefono;
    private String username;
    private String contrasena; // Hash SHA-256 de 64 caracteres hexadecimales
    private String estado; // "Activo", "Inactivo", "Bloqueado"
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;
    private Rol rol;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;

    public Usuario() {
        this.estado = "Activo";
        this.fechaRegistro = LocalDateTime.now();
        this.intentosFallidos = 0;
    }

    public Usuario(int idUsuario, String nombre, String apellido, String documento,
                   String correo, String telefono, String username, String contrasena,
                   Rol rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.correo = correo;
        this.telefono = telefono;
        this.username = username;
        this.contrasena = asegurarHashSha256(contrasena);
        this.rol = rol;
        this.estado = "Activo";
        this.fechaRegistro = LocalDateTime.now();
        this.intentosFallidos = 0;
    }

    /**
     * Calcula el hash SHA-256 en formato hexadecimal para una contraseña en texto plano.
     */
    public static String hashPassword(String passwordPlano) {
        if (passwordPlano == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(passwordPlano.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 no disponible en la JVM", e);
        }
    }

    /**
     * Determina si una cadena ya se encuentra en formato SHA-256 hexadecimal (64 caracteres hex).
     */
    public static boolean esHashSha256(String valor) {
        return valor != null && valor.matches("^[0-9a-fA-F]{64}$");
    }

    /**
     * Garantiza que la contraseña almacenada esté siempre cifrada con SHA-256.
     */
    public static String asegurarHashSha256(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "";
        }
        return esHashSha256(valor) ? valor.toLowerCase() : hashPassword(valor);
    }

    /**
     * Verifica si la contraseña ingresada coincide con el hash SHA-256 almacenado.
     */
    public boolean verificarPassword(String passwordIngresada) {
        if (passwordIngresada == null || this.contrasena == null) {
            return false;
        }
        String hashIngresado = asegurarHashSha256(passwordIngresada);
        String hashAlmacenado = asegurarHashSha256(this.contrasena);
        return hashAlmacenado.equalsIgnoreCase(hashIngresado);
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + (apellido != null ? " " + apellido : "");
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = asegurarHashSha256(contrasena);
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public boolean isBloqueado() {
        if (bloqueadoHasta == null) return false;
        if (LocalDateTime.now().isBefore(bloqueadoHasta)) {
            return true;
        }
        // Expiró el bloqueo
        bloqueadoHasta = null;
        intentosFallidos = 0;
        return false;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + (rol != null ? rol.getNombre() : "Sin Rol") + ")";
    }
}
