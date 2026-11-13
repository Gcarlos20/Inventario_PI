package com.Daos;

import com.model.Compra;
import com.model.DetalleCompra;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO para consultar el detalle de los productos incluidos en cada compra.
 */
public class DetalleCompraDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Devuelve la lista de productos comprados en una factura específica
    public List<DetalleCompra> listarPorCompra(int idCompra) {
        for (Compra c : repo.listarCompras()) {
            if (c.getIdCompra() == idCompra) {
                return c.getDetalles();
            }
        }
        return new ArrayList<>();
    }

    // Lista todos los detalles de compra registrados
    public List<DetalleCompra> listarTodos() {
        List<DetalleCompra> resultado = new ArrayList<>();
        for (Compra c : repo.listarCompras()) {
            resultado.addAll(c.getDetalles());
        }
        return resultado;
    }
}
