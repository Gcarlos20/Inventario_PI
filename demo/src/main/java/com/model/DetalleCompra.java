package com.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DetalleCompra {
    private int idDetalle;
    private int cantidad;
    private BigDecimal precioDecimal;
    private int stockAnterior;
    private int stockNuevo;
    private LocalDate fecha;
    private String observacion;
    private int idCompra;
    private int idProducto;

    public DetalleCompra() {
    }

    public DetalleCompra(int idDetalle, int cantidad, BigDecimal precioDecimal,
                         int stockAnterior, int stockNuevo, LocalDate fecha,
                         String observacion, int idCompra, int idProducto) {
        this.idDetalle = idDetalle;
        this.cantidad = cantidad;
        this.precioDecimal = precioDecimal;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = stockNuevo;
        this.fecha = fecha;
        this.observacion = observacion;
        this.idCompra = idCompra;
        this.idProducto = idProducto;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioDecimal() {
        return precioDecimal;
    }

    public void setPrecioDecimal(BigDecimal precioDecimal) {
        this.precioDecimal = precioDecimal;
    }

    public int getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(int stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public int getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(int stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }
}
