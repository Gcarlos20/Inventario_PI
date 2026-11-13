package com.model;

/**
 * Entidad Producto para el Sistema de Gestión de Inventario Mi Pacífico.
 * Cumple con los requerimientos RF3, Tabla 20 y MER del proyecto.
 */
public class Producto {
    private int idProducto;
    private String codigo;
    private String nombre;
    private String categoria;
    private String descripcion;
    private double precio;
    private int stockInventario;
    private int stockMinimo;
    private Integer idProveedor;
    private String nombreProveedor;
    private String estado; // "Activo", "Inactivo"

    public Producto() {
        this.estado = "Activo";
    }

    /** Constructor compatible con código legado */
    public Producto(int idProducto, String codigo, double precio, int stockInventario,
                    String descripcion, String estado, int stockMinimo) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = descripcion; // compatibilidad
        this.precio = precio;
        this.stockInventario = stockInventario;
        this.descripcion = descripcion;
        this.estado = estado != null ? estado : "Activo";
        this.stockMinimo = stockMinimo;
        this.categoria = "General";
    }

    /** Constructor completo según especificación y mockup */
    public Producto(int idProducto, String codigo, String nombre, String categoria,
                    String descripcion, double precio, int stockInventario,
                    int stockMinimo, Integer idProveedor, String estado) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stockInventario = stockInventario;
        this.stockMinimo = stockMinimo;
        this.idProveedor = idProveedor;
        this.estado = estado != null ? estado : "Activo";
    }

    // Getters y Setters
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

    public String getNombre() {
        return nombre != null && !nombre.isEmpty() ? nombre : descripcion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria != null ? categoria : "General";
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
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

    public int getCantidad() {
        return stockInventario;
    }

    public void setCantidad(int cantidad) {
        this.stockInventario = cantidad;
    }

    public String getDescripcion() {
        return descripcion != null ? descripcion : nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado != null ? estado : "Activo";
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

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreProveedor() {
        return nombreProveedor;
    }

    public void setNombreProveedor(String nombreProveedor) {
        this.nombreProveedor = nombreProveedor;
    }

    public double getValorTotal() {
        return precio * stockInventario;
    }

    public boolean isStockBajo() {
        return stockInventario <= stockMinimo;
    }

    @Override
    public String toString() {
        return getNombre() + " (" + codigo + ")";
    }
}
