package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Proveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Consultas de acceso a datos para proveedores. */
public class ProveedorDao {

    private static final String COLUMNAS = "\"Id_Proveedores\", \"Nombre_Empresa\", \"Nit\", "
            + "\"Nombre_Contacto\", \"Telefono\", \"Correo\", \"Direccion\", \"Ciudad\", "
            + "\"Tipo_Proveedor\", \"Estado\", \"Fecha_Registro\", \"Observacion\"";

    /** Lista todos los proveedores para la pantalla de administración. */
    public List<Proveedor> listarTodos() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM public.\"PROVEEDORES\" ORDER BY \"Nombre_Empresa\"";
        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultados = consulta.executeQuery()) {
            while (resultados.next()) proveedores.add(mapear(resultados));
        }
        return proveedores;
    }

    /** Devuelve proveedores para mostrarlos en un JComboBox. */
    public List<Proveedor> listarParaComboBox() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT \"Id_Proveedores\", \"Nombre_Empresa\" "
                + "FROM public.\"PROVEEDORES\" WHERE COALESCE(\"Estado\", 'Activo') = 'Activo' "
                + "ORDER BY \"Nombre_Empresa\"";

        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultados = consulta.executeQuery()) {
            while (resultados.next()) {
                Proveedor proveedor = new Proveedor();
                proveedor.setIdProveedor(resultados.getInt("Id_Proveedores"));
                proveedor.setNombreEmpresa(resultados.getString("Nombre_Empresa"));
                proveedores.add(proveedor);
            }
        }
        return proveedores;
    }

    public int insertar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO public.\"PROVEEDORES\" (\"Nombre_Empresa\", \"Nit\", \"Nombre_Contacto\", "
                + "\"Telefono\", \"Correo\", \"Direccion\", \"Ciudad\", \"Tipo_Proveedor\", \"Estado\", "
                + "\"Fecha_Registro\", \"Observacion\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "RETURNING \"Id_Proveedores\"";
        try (Connection c = obtenerConexion(); PreparedStatement q = c.prepareStatement(sql)) {
            asignar(q, p);
            try (ResultSet rs = q.executeQuery()) {
                if (!rs.next()) throw new SQLException("La base de datos no devolvió el ID del proveedor.");
                return rs.getInt(1);
            }
        }
    }

    public boolean actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE public.\"PROVEEDORES\" SET \"Nombre_Empresa\"=?, \"Nit\"=?, \"Nombre_Contacto\"=?, "
                + "\"Telefono\"=?, \"Correo\"=?, \"Direccion\"=?, \"Ciudad\"=?, \"Tipo_Proveedor\"=?, "
                + "\"Estado\"=?, \"Observacion\"=? WHERE \"Id_Proveedores\"=?";
        try (Connection c = obtenerConexion(); PreparedStatement q = c.prepareStatement(sql)) {
            q.setString(1, p.getNombreEmpresa().trim()); q.setString(2, p.getNit());
            q.setString(3, p.getNombreContacto()); q.setString(4, p.getTelefono());
            q.setString(5, p.getCorreo()); q.setString(6, p.getDireccion()); q.setString(7, p.getCiudad());
            q.setString(8, p.getTipoProveedor()); q.setString(9, p.getEstado());
            q.setString(10, p.getObservacion()); q.setInt(11, p.getIdProveedor());
            return q.executeUpdate() > 0;
        }
    }

    public boolean cambiarEstado(int id, String estado) throws SQLException {
        String sql = "UPDATE public.\"PROVEEDORES\" SET \"Estado\"=? WHERE \"Id_Proveedores\"=?";
        try (Connection c = obtenerConexion(); PreparedStatement q = c.prepareStatement(sql)) {
            q.setString(1, estado); q.setInt(2, id); return q.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM public.\"PROVEEDORES\" WHERE \"Id_Proveedores\"=?";
        try (Connection c = obtenerConexion(); PreparedStatement q = c.prepareStatement(sql)) {
            q.setInt(1, id); return q.executeUpdate() > 0;
        }
    }

    private void asignar(PreparedStatement q, Proveedor p) throws SQLException {
        q.setString(1, p.getNombreEmpresa().trim()); q.setString(2, p.getNit());
        q.setString(3, p.getNombreContacto()); q.setString(4, p.getTelefono());
        q.setString(5, p.getCorreo()); q.setString(6, p.getDireccion()); q.setString(7, p.getCiudad());
        q.setString(8, p.getTipoProveedor()); q.setString(9, p.getEstado());
        q.setDate(10, java.sql.Date.valueOf(p.getFechaRegistro())); q.setString(11, p.getObservacion());
    }

    private Proveedor mapear(ResultSet r) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdProveedor(r.getInt("Id_Proveedores")); p.setNombreEmpresa(r.getString("Nombre_Empresa"));
        p.setNit(r.getString("Nit")); p.setNombreContacto(r.getString("Nombre_Contacto"));
        p.setTelefono(r.getString("Telefono")); p.setCorreo(r.getString("Correo"));
        p.setDireccion(r.getString("Direccion")); p.setCiudad(r.getString("Ciudad"));
        p.setTipoProveedor(r.getString("Tipo_Proveedor")); p.setEstado(r.getString("Estado"));
        java.sql.Date fecha = r.getDate("Fecha_Registro");
        if (fecha != null) p.setFechaRegistro(fecha.toLocalDate());
        p.setObservacion(r.getString("Observacion"));
        return p;
    }

    private Connection obtenerConexion() throws SQLException {
        Connection conexion = ConexionDatabase.conectar();
        if (conexion == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        return conexion;
    }
}
