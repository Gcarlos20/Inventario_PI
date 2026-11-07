package com.model;

import java.time.LocalDateTime;

public class AlertaStock {
    private int idAlerta;
    private String tipoAlerta; // "Stock Mínimo", "Próximo a Vencer", "Vencido"
    private String mensaje;
    private int stockActual;
    private int stockMinimo;
    private LocalDateTime fechaAlerta;
    private String estado; // "Activa", "Atendida"
    private int idProducto;
    private String nombreProducto;
    private Integer idLote;

    public AlertaStock() {
        this.fechaAlerta = LocalDateTime.now();
        this.estado = "Activa";
    }

    public AlertaStock(int idAlerta, String tipoAlerta, String mensaje, int stockActual,
                       int stockMinimo, int idProducto, String nombreProducto, Integer idLote) {
        this.idAlerta = idAlerta;
        this.tipoAlerta = tipoAlerta;
        this.mensaje = mensaje;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.idLote = idLote;
        this.fechaAlerta = LocalDateTime.now();
        this.estado = "Activa";
    }

    public int getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(int idAlerta) {
        this.idAlerta = idAlerta;
    }

    public String getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(String tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public LocalDateTime getFechaAlerta() {
        return fechaAlerta;
    }

    public void setFechaAlerta(LocalDateTime fechaAlerta) {
        this.fechaAlerta = fechaAlerta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }
}
