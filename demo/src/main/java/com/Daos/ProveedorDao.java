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

    /** Devuelve proveedores para mostrarlos en un JComboBox. */
    public List<Proveedor> listarParaComboBox() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT \"Id_Proveedores\", \"Nombre_Empresa\" "
                + "FROM public.\"PROVEEDORES\" ORDER BY \"Nombre_Empresa\"";

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

    private Connection obtenerConexion() throws SQLException {
        Connection conexion = ConexionDatabase.conectar();
        if (conexion == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        return conexion;
    }
}
