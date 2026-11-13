package com.Daos;

import com.model.AlertaStock;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para consultar las alertas de stock mínimo y lotes próximos a vencer.
 */
public class AlertaStockDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Lista todas las alertas activas del inventario
    public List<AlertaStock> listarActivas() {
        return repo.listarAlertas();
    }

    // Filtra las alertas según su tipo (Stock Mínimo, Próximo a Vencer o Lote Vencido)
    public List<AlertaStock> listarPorTipo(String tipoAlerta) {
        if (tipoAlerta == null || "Todas".equalsIgnoreCase(tipoAlerta)) {
            return repo.listarAlertas();
        }
        List<AlertaStock> filtradas = new ArrayList<>();
        for (AlertaStock a : repo.listarAlertas()) {
            if (a.getTipoAlerta() != null && a.getTipoAlerta().equalsIgnoreCase(tipoAlerta)) {
                filtradas.add(a);
            }
        }
        return filtradas;
    }
}
