package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SolicitudCocina {
    private int idSolicitud;
    private LocalDateTime fechaSolicitud;
    private String estado; // "Pendiente", "Despachada", "Rechazada", "Parcial"
    private String prioridad; // "Alta", "Normal", "Urgente"
    private String menuPreparacion; // Menú u orden de producción (ej: "30 Cazuelas de Mariscos")
    private String observacion;
    private int idUsuarioSolicitante;
    private String nombreSolicitante;
    private Integer idUsuarioDespachador;
    private String nombreDespachador;
    private LocalDateTime fechaAtencion;
    private List<DetalleSolicitudCocina> detalles = new ArrayList<>();

    public SolicitudCocina() {
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "Pendiente";
        this.prioridad = "Normal";
    }

    public SolicitudCocina(int idSolicitud, String menuPreparacion, String prioridad,
                           String observacion, int idUsuarioSolicitante, String nombreSolicitante) {
        this.idSolicitud = idSolicitud;
        this.menuPreparacion = menuPreparacion;
        this.prioridad = prioridad;
        this.observacion = observacion;
        this.idUsuarioSolicitante = idUsuarioSolicitante;
        this.nombreSolicitante = nombreSolicitante;
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "Pendiente";
    }

    public void agregarDetalle(DetalleSolicitudCocina detalle) {
        detalles.add(detalle);
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getMenuPreparacion() {
        return menuPreparacion;
    }

    public void setMenuPreparacion(String menuPreparacion) {
        this.menuPreparacion = menuPreparacion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public int getIdUsuarioSolicitante() {
        return idUsuarioSolicitante;
    }

    public void setIdUsuarioSolicitante(int idUsuarioSolicitante) {
        this.idUsuarioSolicitante = idUsuarioSolicitante;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public Integer getIdUsuarioDespachador() {
        return idUsuarioDespachador;
    }

    public void setIdUsuarioDespachador(Integer idUsuarioDespachador) {
        this.idUsuarioDespachador = idUsuarioDespachador;
    }

    public String getNombreDespachador() {
        return nombreDespachador;
    }

    public void setNombreDespachador(String nombreDespachador) {
        this.nombreDespachador = nombreDespachador;
    }

    public LocalDateTime getFechaAtencion() {
        return fechaAtencion;
    }

    public void setFechaAtencion(LocalDateTime fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }

    public List<DetalleSolicitudCocina> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleSolicitudCocina> detalles) {
        this.detalles = detalles;
    }
}
