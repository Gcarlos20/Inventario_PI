package com.model;

/** Línea de baja de inventario asociada a un lote y un tipo de movimiento. */
public class Merma {
    private int idLote;
    private int idProducto;
    private int cantidad;
    private int idTipoMovimiento;
    private String observacion;

    public Merma() {
    }

    public Merma(int idLote, int idProducto, int cantidad, int idTipoMovimiento,
                 String observacion) {
        this.idLote = idLote;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.idTipoMovimiento = idTipoMovimiento;
        this.observacion = observacion;
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

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getIdTipoMovimiento() {
        return idTipoMovimiento;
    }

    public void setIdTipoMovimiento(int idTipoMovimiento) {
        this.idTipoMovimiento = idTipoMovimiento;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
