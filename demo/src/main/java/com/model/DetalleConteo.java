package com.model;

public class DetalleConteo {
    private int idDetalle;
    private int idConteo;
    private int idProducto;
    private String nombreProducto;
    private int stockSistema;
    private int stockFisico;
    private int diferencia; // stockFisico - stockSistema
    private String observacion;

    public DetalleConteo() {
    }

    public DetalleConteo(int idDetalle, int idProducto, String nombreProducto,
                         int stockSistema, int stockFisico) {
        this.idDetalle = idDetalle;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.stockSistema = stockSistema;
        this.stockFisico = stockFisico;
        this.diferencia = stockFisico - stockSistema;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getIdConteo() {
        return idConteo;
    }

    public void setIdConteo(int idConteo) {
        this.idConteo = idConteo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getStockSistema() {
        return stockSistema;
    }

    public void setStockSistema(int stockSistema) {
        this.stockSistema = stockSistema;
        this.diferencia = this.stockFisico - this.stockSistema;
    }

    public int getStockFisico() {
        return stockFisico;
    }

    public void setStockFisico(int stockFisico) {
        this.stockFisico = stockFisico;
        this.diferencia = this.stockFisico - this.stockSistema;
    }

    public int getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(int diferencia) {
        this.diferencia = diferencia;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
