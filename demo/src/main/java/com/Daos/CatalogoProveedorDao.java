package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.OfertaProveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Persistencia del catálogo de productos y servicios ofrecidos por proveedor. */
public class CatalogoProveedorDao {
    public List<OfertaProveedor> listar(int idProveedor) throws SQLException {
        List<OfertaProveedor> ofertas = new ArrayList<>();
        String sql = "SELECT \"Id_Oferta\", \"Id_Proveedores\", \"Nombre\", \"Tipo\", \"Descripcion\", "
                + "\"Precio_Referencia\" FROM public.\"PROVEEDOR_CATALOGO\" WHERE \"Id_Proveedores\"=? "
                + "ORDER BY \"Nombre\"";
        try (Connection c = obtenerConexion(); PreparedStatement q = c.prepareStatement(sql)) {
            q.setInt(1, idProveedor);
            try (ResultSet r = q.executeQuery()) {
                while (r.next()) {
                    OfertaProveedor o = new OfertaProveedor();
                    o.setId(r.getInt("Id_Oferta")); o.setIdProveedor(r.getInt("Id_Proveedores"));
                    o.setNombre(r.getString("Nombre")); o.setTipo(r.getString("Tipo"));
                    o.setDescripcion(r.getString("Descripcion")); o.setPrecioReferencia(r.getBigDecimal("Precio_Referencia"));
                    ofertas.add(o);
                }
            }
        }
        return ofertas;
    }

    public void insertar(OfertaProveedor o) throws SQLException {
        String sql = "INSERT INTO public.\"PROVEEDOR_CATALOGO\" (\"Id_Proveedores\", \"Nombre\", \"Tipo\", "
                + "\"Descripcion\", \"Precio_Referencia\") VALUES (?, ?, ?, ?, ?)";
        try (Connection c = obtenerConexion(); PreparedStatement q = c.prepareStatement(sql)) {
            asignar(q, o); q.executeUpdate();
        }
    }

    public boolean actualizar(OfertaProveedor o) throws SQLException {
        String sql = "UPDATE public.\"PROVEEDOR_CATALOGO\" SET \"Nombre\"=?, \"Tipo\"=?, \"Descripcion\"=?, "
                + "\"Precio_Referencia\"=? WHERE \"Id_Oferta\"=? AND \"Id_Proveedores\"=?";
        try (Connection c = obtenerConexion(); PreparedStatement q = c.prepareStatement(sql)) {
            q.setString(1, o.getNombre().trim()); q.setString(2, o.getTipo()); q.setString(3, o.getDescripcion());
            q.setBigDecimal(4, o.getPrecioReferencia()); q.setInt(5, o.getId()); q.setInt(6, o.getIdProveedor());
            return q.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id, int idProveedor) throws SQLException {
        String sql = "DELETE FROM public.\"PROVEEDOR_CATALOGO\" WHERE \"Id_Oferta\"=? AND \"Id_Proveedores\"=?";
        try (Connection c = obtenerConexion(); PreparedStatement q = c.prepareStatement(sql)) {
            q.setInt(1, id); q.setInt(2, idProveedor); return q.executeUpdate() > 0;
        }
    }

    private void asignar(PreparedStatement q, OfertaProveedor o) throws SQLException {
        q.setInt(1, o.getIdProveedor()); q.setString(2, o.getNombre().trim()); q.setString(3, o.getTipo());
        q.setString(4, o.getDescripcion()); q.setBigDecimal(5, o.getPrecioReferencia());
    }

    private Connection obtenerConexion() throws SQLException {
        Connection c = ConexionDatabase.conectar();
        if (c == null) throw new SQLException("No se pudo establecer conexión con la base de datos.");
        return c;
    }
}
