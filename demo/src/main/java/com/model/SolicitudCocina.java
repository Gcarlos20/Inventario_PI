package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Cabecera y líneas de una solicitud interna de productos para cocina. */
public class SolicitudCocina {
    private int idSolicitud;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private String observacion;
    private int idUsuario;
    private String nombreSolicitante;
    private List<DetalleSolicitudCocina> detalles = new ArrayList<>();

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreSolicitante() { return nombreSolicitante; }
    public void setNombreSolicitante(String nombreSolicitante) { this.nombreSolicitante = nombreSolicitante; }
    public List<DetalleSolicitudCocina> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleSolicitudCocina> detalles) { this.detalles = detalles; }
}
