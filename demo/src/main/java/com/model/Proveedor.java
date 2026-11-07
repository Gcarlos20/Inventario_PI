package com.model;

import java.time.LocalDate;

public class Proveedor {
    private int idProveedor;
    private String nombreEmpresa;
    private String nit;
    private String nombreContacto;
    private String telefono;
    private String correo;
    private String direccion;
    private String ciudad;
    private String tipoProveedor;
    private String estado; // "Activo", "Inactivo"
    private LocalDate fechaRegistro;
    private String observacion;

    public Proveedor() {
        this.estado = "Activo";
        this.fechaRegistro = LocalDate.now();
    }

    public Proveedor(int idProveedor, String nombreEmpresa, String nit, String nombreContacto,
                     String telefono, String correo, String direccion, String ciudad,
                     String tipoProveedor, String observacion) {
        this.idProveedor = idProveedor;
        this.nombreEmpresa = nombreEmpresa;
        this.nit = nit;
        this.nombreContacto = nombreContacto;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.tipoProveedor = tipoProveedor;
        this.estado = "Activo";
        this.fechaRegistro = LocalDate.now();
        this.observacion = observacion;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getTipoProveedor() {
        return tipoProveedor;
    }

    public void setTipoProveedor(String tipoProveedor) {
        this.tipoProveedor = tipoProveedor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    @Override
    public String toString() {
        return nombreEmpresa + " (NIT: " + nit + ")";
    }
}
