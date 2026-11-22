package com.model;

import java.math.BigDecimal;

/** Línea de producto con las cantidades del conteo y su diferencia de stock. */
public class DetalleConteo {
    private long idDetalleConteo;
    private BigDecimal stockSistema;
    private BigDecimal stockFisico;
    private BigDecimal diferencia;
    private String observacion;
    private long idConteo;
    private long idProducto;
    private String codigoProducto;
    private String nombreProducto;

    public long getIdDetalleConteo() {
        return idDetalleConteo;
    }

    public void setIdDetalleConteo(long idDetalleConteo) {
        this.idDetalleConteo = idDetalleConteo;
    }

    public BigDecimal getStockSistema() {
        return stockSistema;
    }

    public void setStockSistema(BigDecimal stockSistema) {
        this.stockSistema = stockSistema;
    }

    public BigDecimal getStockFisico() {
        return stockFisico;
    }

    public void setStockFisico(BigDecimal stockFisico) {
        this.stockFisico = stockFisico;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public long getIdConteo() {
        return idConteo;
    }

    public void setIdConteo(long idConteo) {
        this.idConteo = idConteo;
    }

    public long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(long idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
}
