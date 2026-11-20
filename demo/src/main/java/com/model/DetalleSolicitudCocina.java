package com.model;

/** Producto solicitado y cantidad que ya salió del inventario. */
public class DetalleSolicitudCocina {
    private int idDetalle;
    private int idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private int cantidadSolicitada;
    private int cantidadEntregada;
    private int stockActual;

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public int getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(int cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public int getCantidadEntregada() { return cantidadEntregada; }
    public void setCantidadEntregada(int cantidadEntregada) { this.cantidadEntregada = cantidadEntregada; }
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
}
