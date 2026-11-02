package com.model;

public class Producto {
    private int idProducto;
    private String codigo;
    private double precio;
    private int stockInventario;
    private String descripcion;
    private String estado;
    private int stockMinimo;

    public Producto() {
    }

    public Producto(int idProducto, String codigo, double precio, int stockInventario,
                    String descripcion, String estado, int stockMinimo) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.precio = precio;
        this.stockInventario = stockInventario;
        this.descripcion = descripcion;
        this.estado = estado;
        this.stockMinimo = stockMinimo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStockInventario() {
        return stockInventario;
    }

    public void setStockInventario(int stockInventario) {
        this.stockInventario = stockInventario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
}
