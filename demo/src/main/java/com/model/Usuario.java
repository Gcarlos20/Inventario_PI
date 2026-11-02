package com.model;

import java.time.LocalDateTime;

public class Usuario {
    private int idUsuario;
    private String nombre;
    private String apellido;
    private String documento;
    private String correo;
    private String telefono;
    private String username;
    private String contrasena; // hash o texto seguro
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
        this.contrasena = contrasena;
        this.rol = rol;
        this.estado = "Activo";
        this.fechaRegistro = LocalDateTime.now();
        this.intentosFallidos = 0;
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
        this.contrasena = contrasena;
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
