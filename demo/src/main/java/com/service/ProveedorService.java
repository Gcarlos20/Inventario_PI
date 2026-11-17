package com.service;

import com.Daos.ProveedorDao;
import com.model.Proveedor;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Reglas y operaciones de negocio para proveedores. */
public class ProveedorService {
    private final ProveedorDao proveedorDao;

    public ProveedorService() {
        this(new ProveedorDao());
    }

    ProveedorService(ProveedorDao proveedorDao) {
        this.proveedorDao = Objects.requireNonNull(proveedorDao);
    }

    public List<Proveedor> listarTodos() throws SQLException {
        return proveedorDao.listarTodos();
    }

    public List<Proveedor> listarActivos() throws SQLException {
        return proveedorDao.listarParaComboBox();
    }

    public int crear(Proveedor proveedor) throws SQLException {
        validar(proveedor);
        if (proveedor.getEstado() == null || proveedor.getEstado().isBlank()) {
            proveedor.setEstado("Activo");
        }
        return proveedorDao.insertar(proveedor);
    }

    public boolean actualizar(Proveedor proveedor) throws SQLException {
        if (proveedor.getIdProveedor() <= 0) {
            throw new IllegalArgumentException("Selecciona un proveedor válido.");
        }
        validar(proveedor);
        return proveedorDao.actualizar(proveedor);
    }

    public boolean cambiarEstado(int idProveedor, String estado) throws SQLException {
        if (idProveedor <= 0) throw new IllegalArgumentException("El ID del proveedor no es válido.");
        if (!"Activo".equals(estado) && !"Inactivo".equals(estado)) {
            throw new IllegalArgumentException("El estado debe ser Activo o Inactivo.");
        }
        return proveedorDao.cambiarEstado(idProveedor, estado);
    }

    public boolean eliminar(int idProveedor) throws SQLException {
        if (idProveedor <= 0) throw new IllegalArgumentException("El ID del proveedor no es válido.");
        return proveedorDao.eliminar(idProveedor);
    }

    private void validar(Proveedor proveedor) {
        Objects.requireNonNull(proveedor, "El proveedor es obligatorio.");
        if (proveedor.getNombreEmpresa() == null || proveedor.getNombreEmpresa().isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio.");
        }
    }
}
