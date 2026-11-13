package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.SolicitudCocina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para gestionar las solicitudes de insumos desde la cocina y su despacho en almacén.
 */
public class SolicitudCocinaDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Crea un nuevo pedido de insumos desde cocina
    public boolean crearSolicitud(SolicitudCocina solicitud) throws Exception {
        boolean ok = repo.crearSolicitudCocina(solicitud);

        if (DataRepository.isSupabaseDisponible()) {
            String sql = "INSERT INTO \"SOLICITUDES_COCINA\" (\"Id_Solicitud\", \"Menu_Preparacion\", "
                    + "\"Prioridad\", \"Estado\", \"Observacion\", \"USUARIO_Id_Usuario\") VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, solicitud.getIdSolicitud());
                ps.setString(2, solicitud.getMenuPreparacion());
                ps.setString(3, solicitud.getPrioridad());
                ps.setString(4, solicitud.getEstado());
                ps.setString(5, solicitud.getObservacion());
                ps.setInt(6, solicitud.getIdUsuarioSolicitante());
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
        return ok;
    }

    // Lista todas las solicitudes de cocina registradas
    public List<SolicitudCocina> listarTodas() {
        return repo.listarSolicitudesCocina();
    }

    // Filtra las solicitudes según su estado (Pendiente o Despachada) usando un ciclo for sencillo
    public List<SolicitudCocina> listarPorEstado(String estado) {
        if (estado == null || "Todas".equalsIgnoreCase(estado)) {
            return repo.listarSolicitudesCocina();
        }
        List<SolicitudCocina> resultado = new ArrayList<>();
        for (SolicitudCocina s : repo.listarSolicitudesCocina()) {
            if (estado.equalsIgnoreCase(s.getEstado())) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    // Despacha una solicitud descontando primero de los lotes más antiguos (FIFO)
    public boolean despacharSolicitudFIFO(int idSolicitud) throws Exception {
        return repo.despacharSolicitudCocina(idSolicitud);
    }
}
