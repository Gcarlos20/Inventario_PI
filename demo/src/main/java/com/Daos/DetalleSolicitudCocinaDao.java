package com.Daos;

import com.model.DetalleSolicitudCocina;
import com.model.SolicitudCocina;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para consultar los ingredientes solicitados en cada pedido de cocina.
 */
public class DetalleSolicitudCocinaDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Devuelve los productos pedidos dentro de una solicitud de cocina específica
    public List<DetalleSolicitudCocina> listarPorSolicitud(int idSolicitud) {
        for (SolicitudCocina s : repo.listarSolicitudesCocina()) {
            if (s.getIdSolicitud() == idSolicitud) {
                return s.getDetalles();
            }
        }
        return new ArrayList<>();
    }

    // Lista todos los detalles de solicitudes registrados
    public List<DetalleSolicitudCocina> listarTodos() {
        List<DetalleSolicitudCocina> lista = new ArrayList<>();
        for (SolicitudCocina s : repo.listarSolicitudesCocina()) {
            lista.addAll(s.getDetalles());
        }
        return lista;
    }
}
