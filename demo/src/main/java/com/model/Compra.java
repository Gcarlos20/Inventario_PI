package com.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Compra {
    private int idCompra;
    private LocalDateTime fechaCompra;
    private String numeroFactura;
    private double subtotal;
    private double totalCompra;
    private String metodoPago;
    private String estado; // "Completada", "Anulada"
    private String observacion;
    private int idProveedor;
    private String nombreProveedor;
    private int idUsuario;
    private String nombreUsuario;
    private List<DetalleCompra> detalles = new ArrayList<>();

    public Compra() {
        this.fechaCompra = LocalDateTime.now();
        this.estado = "Completada";
        this.metodoPago = "Transferencia";
    }

    public Compra(int idCompra, String numeroFactura, int idProveedor, String nombreProveedor,
                  int idUsuario, String nombreUsuario, String metodoPago, String observacion) {
        this.idCompra = idCompra;
        this.numeroFactura = numeroFactura;
        this.idProveedor = idProveedor;
        this.nombreProveedor = nombreProveedor;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.metodoPago = metodoPago;
        this.observacion = observacion;
        this.fechaCompra = LocalDateTime.now();
        this.estado = "Completada";
    }

    public void recalcularTotales() {
        this.totalCompra = 0;
        for (DetalleCompra d : detalles) {
            this.totalCompra += d.getSubtotal();
        }
        this.subtotal = this.totalCompra;
    }

    public void agregarDetalle(DetalleCompra detalle) {
        detalles.add(detalle);
        recalcularTotales();
    }

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(double totalCompra) {
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

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
        recalcularTotales();
    }
}
