package com.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Cabecera de un conteo físico de inventario. */
public class ConteoFisico {
    private long idConteo;
    private LocalDate fechaConteo;
    private String estado;
    private String observacion;
    private long idUsuario;
    private String nombreResponsable;
    private List<DetalleConteo> detalles = new ArrayList<>();

    public long getIdConteo() {
        return idConteo;
    }

    public void setIdConteo(long idConteo) {
        this.idConteo = idConteo;
    }

    public LocalDate getFechaConteo() {
        return fechaConteo;
    }

    public void setFechaConteo(LocalDate fechaConteo) {
        this.fechaConteo = fechaConteo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreResponsable() {
        return nombreResponsable;
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable;
    }

    public List<DetalleConteo> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleConteo> detalles) {
        this.detalles = detalles;
    }
}
