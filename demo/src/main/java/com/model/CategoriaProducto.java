package com.model;

import java.time.LocalDate;

public class CategoriaProducto {
    private int idCategoria;
    private String nombre;
    private String descripcion;
    private String estado;
    private LocalDate fechaCreacion;

    public CategoriaProducto() {
        this.estado = "Activo";
        this.fechaCreacion = LocalDate.now();
    }

    public CategoriaProducto(int idCategoria, String nombre, String descripcion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = "Activo";
        this.fechaCreacion = LocalDate.now();
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
