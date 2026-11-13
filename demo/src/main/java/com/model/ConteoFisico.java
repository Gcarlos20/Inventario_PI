package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConteoFisico {
    private int idConteo;
    private LocalDateTime fechaConteo;
    private String categoria;
    private String estado; // "Borrador", "Pendiente_Revision", "Aprobado", "Rechazado"
    private String observacion;
    private int idUsuarioAlmacenista;
    private String nombreAlmacenista;
    private Integer idUsuarioAuditor;
    private String nombreAuditor;
    private Integer idUsuarioAprobador;
    private String nombreAprobador;
    private List<DetalleConteo> detalles = new ArrayList<>();

    public ConteoFisico() {
        this.fechaConteo = LocalDateTime.now();
        this.estado = "Pendiente_Revision";
    }

    public ConteoFisico(int idConteo, String categoria, String observacion,
                        int idUsuarioAlmacenista, String nombreAlmacenista) {
        this.idConteo = idConteo;
        this.categoria = categoria;
        this.observacion = observacion;
        this.idUsuarioAlmacenista = idUsuarioAlmacenista;
        this.nombreAlmacenista = nombreAlmacenista;
        this.fechaConteo = LocalDateTime.now();
        this.estado = "Pendiente_Revision";
    }

    public void agregarDetalle(DetalleConteo detalle) {
        detalles.add(detalle);
    }

    public int getIdConteo() {
        return idConteo;
    }

    public void setIdConteo(int idConteo) {
        this.idConteo = idConteo;
    }

    public LocalDateTime getFechaConteo() {
        return fechaConteo;
    }

    public void setFechaConteo(LocalDateTime fechaConteo) {
        this.fechaConteo = fechaConteo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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

    public int getIdUsuarioAlmacenista() {
        return idUsuarioAlmacenista;
    }

    public void setIdUsuarioAlmacenista(int idUsuarioAlmacenista) {
        this.idUsuarioAlmacenista = idUsuarioAlmacenista;
    }

    public String getNombreAlmacenista() {
        return nombreAlmacenista;
    }

    public void setNombreAlmacenista(String nombreAlmacenista) {
        this.nombreAlmacenista = nombreAlmacenista;
    }

    public Integer getIdUsuarioAuditor() {
        return idUsuarioAuditor;
    }

    public void setIdUsuarioAuditor(Integer idUsuarioAuditor) {
        this.idUsuarioAuditor = idUsuarioAuditor;
    }

    public String getNombreAuditor() {
        return nombreAuditor;
    }

    public void setNombreAuditor(String nombreAuditor) {
        this.nombreAuditor = nombreAuditor;
    }

    public Integer getIdUsuarioAprobador() {
        return idUsuarioAprobador;
    }

    public void setIdUsuarioAprobador(Integer idUsuarioAprobador) {
        this.idUsuarioAprobador = idUsuarioAprobador;
    }

    public String getNombreAprobador() {
        return nombreAprobador;
    }

    public void setNombreAprobador(String nombreAprobador) {
        this.nombreAprobador = nombreAprobador;
    }

    public List<DetalleConteo> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleConteo> detalles) {
        this.detalles = detalles;
    }

    public int getTotalDiferencias() {
        int total = 0;
        for (DetalleConteo d : detalles) {
            if (d.getDiferencia() != 0) total++;
        }
        return total;
    }
}
