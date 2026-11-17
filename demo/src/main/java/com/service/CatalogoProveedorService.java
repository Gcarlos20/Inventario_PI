package com.service;

import com.Daos.CatalogoProveedorDao;
import com.model.OfertaProveedor;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Reglas y operaciones para los productos y servicios ofrecidos por proveedores. */
public class CatalogoProveedorService {
    private final CatalogoProveedorDao catalogoDao;

    public CatalogoProveedorService() {
        this(new CatalogoProveedorDao());
    }

    CatalogoProveedorService(CatalogoProveedorDao catalogoDao) {
        this.catalogoDao = Objects.requireNonNull(catalogoDao);
    }

    public List<OfertaProveedor> listar(int idProveedor) throws SQLException {
        if (idProveedor <= 0) throw new IllegalArgumentException("Selecciona un proveedor válido.");
        return catalogoDao.listar(idProveedor);
    }

    public void crear(OfertaProveedor oferta) throws SQLException {
        validar(oferta);
        catalogoDao.insertar(oferta);
    }

    public boolean actualizar(OfertaProveedor oferta) throws SQLException {
        validar(oferta);
        if (oferta.getId() <= 0) throw new IllegalArgumentException("Selecciona una oferta válida.");
        return catalogoDao.actualizar(oferta);
    }

    public boolean eliminar(int idOferta, int idProveedor) throws SQLException {
        if (idOferta <= 0 || idProveedor <= 0) {
            throw new IllegalArgumentException("Selecciona una oferta válida.");
        }
        return catalogoDao.eliminar(idOferta, idProveedor);
    }

    private void validar(OfertaProveedor oferta) {
        Objects.requireNonNull(oferta, "La oferta es obligatoria.");
        if (oferta.getIdProveedor() <= 0) throw new IllegalArgumentException("Selecciona un proveedor.");
        if (oferta.getNombre() == null || oferta.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto o servicio es obligatorio.");
        }
        if (oferta.getPrecioReferencia() != null && oferta.getPrecioReferencia().signum() < 0) {
            throw new IllegalArgumentException("El precio debe ser igual o mayor que cero.");
        }
    }
}
