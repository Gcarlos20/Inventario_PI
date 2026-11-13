package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para gestionar los proveedores del restaurante.
 */
public class ProveedorDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Retorna la lista de todos los proveedores
    public List<Proveedor> listarTodos() {
        return repo.listarProveedores();
    }

    // Busca un proveedor por su ID
    public Proveedor obtenerPorId(int idProveedor) {
        return repo.obtenerProveedorPorId(idProveedor);
    }

    // Filtra los proveedores por texto de búsqueda, tipo y estado usando un ciclo for sencillo
    public List<Proveedor> filtrar(String texto, String tipoProveedor, String estado) {
        String busqueda = texto != null ? texto.trim().toLowerCase() : "";
        List<Proveedor> resultado = new ArrayList<>();

        for (Proveedor p : repo.listarProveedores()) {
            boolean coincideTipo = (tipoProveedor == null || "Todos".equalsIgnoreCase(tipoProveedor))
                    || (p.getTipoProveedor() != null && p.getTipoProveedor().equalsIgnoreCase(tipoProveedor));

            boolean coincideEstado = (estado == null || "Todos".equalsIgnoreCase(estado))
                    || (p.getEstado() != null && p.getEstado().equalsIgnoreCase(estado));

            boolean coincideTexto = busqueda.isEmpty()
                    || (p.getNombreEmpresa() != null && p.getNombreEmpresa().toLowerCase().contains(busqueda))
                    || (p.getNit() != null && p.getNit().toLowerCase().contains(busqueda))
                    || (p.getNombreContacto() != null && p.getNombreContacto().toLowerCase().contains(busqueda))
                    || (p.getCiudad() != null && p.getCiudad().toLowerCase().contains(busqueda));

            if (coincideTipo && coincideEstado && coincideTexto) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // Guarda un nuevo proveedor en el sistema y en Supabase
    public boolean agregar(Proveedor proveedor) throws Exception {
        boolean ok = repo.guardarProveedor(proveedor);

        if (DataRepository.isSupabaseDisponible()) {
            String sql = "INSERT INTO \"PROVEEDORES\" (\"Id_Proveedor\", \"Nombre_Empresa\", \"Nit\", "
                    + "\"Nombre_Contacto\", \"Telefono\", \"Correo\", \"Direccion\", \"Ciudad\", \"Tipo_Proveedor\", \"Observacion\") "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, proveedor.getIdProveedor());
                ps.setString(2, proveedor.getNombreEmpresa());
                ps.setString(3, proveedor.getNit());
                ps.setString(4, proveedor.getNombreContacto());
                ps.setString(5, proveedor.getTelefono());
                ps.setString(6, proveedor.getCorreo());
                ps.setString(7, proveedor.getDireccion());
                ps.setString(8, proveedor.getCiudad());
                ps.setString(9, proveedor.getTipoProveedor());
                ps.setString(10, proveedor.getObservacion());
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
        return ok;
    }

    // Actualiza los datos de un proveedor existente
    public boolean actualizar(Proveedor proveedor) throws Exception {
        boolean ok = repo.guardarProveedor(proveedor);

        if (DataRepository.isSupabaseDisponible()) {
            String sql = "UPDATE \"PROVEEDORES\" SET \"Nombre_Empresa\" = ?, \"Nit\" = ?, "
                    + "\"Nombre_Contacto\" = ?, \"Telefono\" = ?, \"Correo\" = ?, \"Direccion\" = ?, "
                    + "\"Ciudad\" = ?, \"Tipo_Proveedor\" = ?, \"Observacion\" = ? WHERE \"Id_Proveedor\" = ?";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, proveedor.getNombreEmpresa());
                ps.setString(2, proveedor.getNit());
                ps.setString(3, proveedor.getNombreContacto());
                ps.setString(4, proveedor.getTelefono());
                ps.setString(5, proveedor.getCorreo());
                ps.setString(6, proveedor.getDireccion());
                ps.setString(7, proveedor.getCiudad());
                ps.setString(8, proveedor.getTipoProveedor());
                ps.setString(9, proveedor.getObservacion());
                ps.setInt(10, proveedor.getIdProveedor());
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
        return ok;
    }

    // Elimina o inactiva un proveedor
    public boolean eliminar(int idProveedor) throws Exception {
        return repo.eliminarProveedor(idProveedor);
    }
}
