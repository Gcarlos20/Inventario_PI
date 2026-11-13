package com.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Lote {
    private int idLote;
    private int idProducto;
    private String nombreProducto;
    private Integer idDetalleCompra;
    private LocalDate fechaIngreso;
    private LocalDate fechaVencimiento;
    private int cantidadInicial;
    private int cantidadActual;
    private double precioUnitario;
    private String estado; // "Vigente", "Agotado", "Vencido"

    public Lote() {
        this.fechaIngreso = LocalDate.now();
        this.estado = "Vigente";
    }

    public Lote(int idLote, int idProducto, String nombreProducto, LocalDate fechaIngreso,
                LocalDate fechaVencimiento, int cantidadInicial, int cantidadActual,
                double precioUnitario) {
        this.idLote = idLote;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.fechaIngreso = fechaIngreso;
        this.fechaVencimiento = fechaVencimiento;
        this.cantidadInicial = cantidadInicial;
        this.cantidadActual = cantidadActual;
        this.precioUnitario = precioUnitario;
        actualizarEstado();
    }

    public void actualizarEstado() {
        if (cantidadActual <= 0) {
            estado = "Agotado";
        } else if (isVencido()) {
            estado = "Vencido";
        } else {
            estado = "Vigente";
        }
    }

    public boolean isVencido() {
        if (fechaVencimiento == null) return false;
        return LocalDate.now().isAfter(fechaVencimiento);
    }

    public long getDiasParaVencer() {
        if (fechaVencimiento == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    public boolean isProximoAVencer(int diasThreshold) {
        if (fechaVencimiento == null || isVencido() || cantidadActual <= 0) return false;
        long dias = getDiasParaVencer();
        return dias >= 0 && dias <= diasThreshold;
    }

    public int getIdLote() {
        return idLote;
    }

    public void setIdLote(int idLote) {
        this.idLote = idLote;
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

    public Integer getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(Integer idDetalleCompra) {
        this.idDetalleCompra = idDetalleCompra;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
        actualizarEstado();
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
        actualizarEstado();
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getEstado() {
        actualizarEstado();
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Lote #" + idLote + " (" + nombreProducto + " - " + cantidadActual + " unds - Vence: " + fechaVencimiento + ")";
    }
}
