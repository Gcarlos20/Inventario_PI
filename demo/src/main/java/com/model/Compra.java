package com.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Compra {
    private int idCompra;
    private LocalDate fechaCompras;
    private String numeroFactura;
    private BigDecimal subTotal;
    private BigDecimal totalCompra;
    private String metodoPago;
    private String estado;
    private String observacion;
    private int idUsuario;
    private int idProveedores;

    public Compra() {
    }

    public Compra(int idCompra, LocalDate fechaCompras, String numeroFactura,
                  BigDecimal subTotal, BigDecimal totalCompra, String metodoPago,
                  String estado, String observacion, int idUsuario, int idProveedores) {
        this.idCompra = idCompra;
        this.fechaCompras = fechaCompras;
        this.numeroFactura = numeroFactura;
        this.subTotal = subTotal;
        this.totalCompra = totalCompra;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.observacion = observacion;
        this.idUsuario = idUsuario;
        this.idProveedores = idProveedores;
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public LocalDate getFechaCompras() {
        return fechaCompras;
    }

    public void setFechaCompras(LocalDate fechaCompras) {
        this.fechaCompras = fechaCompras;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(BigDecimal totalCompra) {
        this.totalCompra = totalCompra;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
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

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdProveedores() {
        return idProveedores;
    }

    public void setIdProveedores(int idProveedores) {
        this.idProveedores = idProveedores;
    }
}
