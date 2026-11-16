package com.model;

import java.math.BigDecimal;

/** Producto o servicio ofrecido por un proveedor. */
public class OfertaProveedor {
    private int id;
    private int idProveedor;
    private String nombre;
    private String tipo;
    private String descripcion;
    private BigDecimal precioReferencia;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecioReferencia() { return precioReferencia; }
    public void setPrecioReferencia(BigDecimal precioReferencia) { this.precioReferencia = precioReferencia; }
}
