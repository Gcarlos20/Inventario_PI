package com.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Lote {
    private int idLote;
    private LocalDate fechasIngreso;
    private LocalDate fechaVencimiento;
    private int cantidadInicial;
    private int cantidadActual;
    private String estado;
    private BigDecimal total;
    private int idProducto;
    private int idDetalle;

    public Lote() {
    }

    public Lote(int idLote, LocalDate fechasIngreso, LocalDate fechaVencimiento,
                int cantidadInicial, int cantidadActual, String estado, BigDecimal total,
                int idProducto, int idDetalle) {
        this.idLote = idLote;
        this.fechasIngreso = fechasIngreso;
        this.fechaVencimiento = fechaVencimiento;
        this.cantidadInicial = cantidadInicial;
        this.cantidadActual = cantidadActual;
        this.estado = estado;
        this.total = total;
        this.idProducto = idProducto;
        this.idDetalle = idDetalle;
    }

    public int getIdLote() {
        return idLote;
    }

    public void setIdLote(int idLote) {
        this.idLote = idLote;
    }

    public LocalDate getFechasIngreso() {
        return fechasIngreso;
    }

    public void setFechasIngreso(LocalDate fechasIngreso) {
        this.fechasIngreso = fechasIngreso;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getCantidadInicial() {
        return cantidadInicial;
    }

    public void setCantidadInicial(int cantidadInicial) {
        this.cantidadInicial = cantidadInicial;
    }

    public int getCantidadActual() {
        return cantidadActual;
    }

    public void setCantidadActual(int cantidadActual) {
        this.cantidadActual = cantidadActual;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(int idDetalle) {
        this.idDetalle = idDetalle;
    }
}
