package com.Daos;

import com.model.SolicitudCocina;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Consultas de cabecera para las solicitudes internas de cocina. */
public class SolicitudCocinaDao {
    public int insertar(Connection conexion, SolicitudCocina solicitud) throws SQLException {
        String sql = "INSERT INTO public.\"SOLICITUDES_COCINA\" "
                + "(\"Fecha_Soliciud\", \"Estado\", \"Prioridad\", \"Observacion\", \"Id_Usuario\", \"Id_Producto\") "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING \"Id_Solicitud\"";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setDate(1, Date.valueOf(LocalDateTime.now().toLocalDate()));
            consulta.setString(2, "Pendiente");
            consulta.setString(3, "Normal");
            consulta.setString(4, solicitud.getObservacion());
            consulta.setInt(5, solicitud.getIdUsuario());
            consulta.setInt(6, solicitud.getDetalles().get(0).getIdProducto());
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) throw new SQLException("La base de datos no devolvió el ID de la solicitud.");
                return resultado.getInt(1);
            }
        }
    }

    public List<SolicitudCocina> listar() throws SQLException {
        return listar(null);
    }

    public List<SolicitudCocina> listar(Integer idUsuario) throws SQLException {
        String sql = "SELECT s.\"Id_Solicitud\", s.\"Fecha_Soliciud\", s.\"Estado\", s.\"Observacion\", "
                + "s.\"Id_Usuario\", u.\"Nombre\", u.\"Apellido\" FROM public.\"SOLICITUDES_COCINA\" s "
                + "LEFT JOIN public.\"USUARIO\" u ON u.\"Id_Usuario\" = s.\"Id_Usuario\" "
                + (idUsuario == null ? "" : "WHERE s.\"Id_Usuario\" = ? ")
                + "ORDER BY s.\"Fecha_Soliciud\" DESC, s.\"Id_Solicitud\" DESC";
        List<SolicitudCocina> solicitudes = new ArrayList<>();
        try (Connection conexion = com.Connection.ConexionDatabase.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            if (idUsuario != null) consulta.setInt(1, idUsuario);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    SolicitudCocina solicitud = new SolicitudCocina();
                    solicitud.setIdSolicitud(resultado.getInt("Id_Solicitud"));
                    solicitud.setFechaSolicitud(resultado.getDate("Fecha_Soliciud").toLocalDate().atStartOfDay());
                    solicitud.setEstado(resultado.getString("Estado"));
                    solicitud.setObservacion(resultado.getString("Observacion"));
                    solicitud.setIdUsuario(resultado.getInt("Id_Usuario"));
                    solicitud.setNombreSolicitante(
                            (resultado.getString("Nombre") == null ? "" : resultado.getString("Nombre"))
                                    + (resultado.getString("Apellido") == null ? "" : " " + resultado.getString("Apellido")));
                    solicitudes.add(solicitud);
                }
            }
        }
        return solicitudes;
    }

    public void actualizarEstado(Connection conexion, int idSolicitud, String estado) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(
                "UPDATE public.\"SOLICITUDES_COCINA\" SET \"Estado\" = ? WHERE \"Id_Solicitud\" = ?")) {
            consulta.setString(1, estado);
            consulta.setInt(2, idSolicitud);
            if (consulta.executeUpdate() != 1) throw new SQLException("No se encontró la solicitud seleccionada.");
        }
    }
}
