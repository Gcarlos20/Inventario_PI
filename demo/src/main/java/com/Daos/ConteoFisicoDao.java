package com.Daos;

import com.model.ConteoFisico;
import java.util.List;

/**
 * Clase DAO para gestionar los conteos físicos de bodega, la revisión del auditor
 * y la aprobación de ajustes de stock.
 */
public class ConteoFisicoDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Crea una nueva hoja de conteo físico para una categoría de productos
    public ConteoFisico iniciarConteo(String categoria, String observacion) {
        return repo.iniciarConteoFisico(categoria, observacion);
    }

    // Lista todos los conteos físicos realizados
    public List<ConteoFisico> listarTodos() {
        return repo.listarConteos();
    }

    // Guarda la cantidad física contada en bodega para un producto
    public boolean registrarCantidadContada(int idConteo, int idProducto, int stockFisico, String observacion) {
        return repo.guardarDetalleConteo(idConteo, idProducto, stockFisico, observacion);
    }

    // Registra la revisión de diferencias por parte del Auditor
    public boolean revisarPorAuditor(int idConteo, String observacionAuditor) throws Exception {
        return repo.revisarConteoAuditor(idConteo, observacionAuditor);
    }

    // Aprueba el conteo físico y actualiza el stock de los productos en el inventario
    public boolean aprobarYAplicarAjuste(int idConteo) throws Exception {
        return repo.aprobarAjusteConteo(idConteo);
    }

    // Rechaza un conteo físico sin modificar el stock
    public boolean rechazarConteo(int idConteo, String motivo) {
        return repo.rechazarConteo(idConteo, motivo);
    }
}
