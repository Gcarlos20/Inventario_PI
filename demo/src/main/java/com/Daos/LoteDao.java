package com.Daos;

import com.model.Lote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Clase DAO para gestionar los lotes de productos y sus fechas de vencimiento.
 */
public class LoteDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Lista todos los lotes ordenados desde el más antiguo al más reciente
    public List<Lote> listarTodos() {
        List<Lote> lista = new ArrayList<>(repo.listarLotes());
        lista.sort(Comparator.comparing(Lote::getFechaIngreso).thenComparing(Lote::getIdLote));
        return lista;
    }

    // Lista los lotes disponibles y no vencidos de un producto para despachar primero el más antiguo (FIFO)
    public List<Lote> listarVigentesPorProductoFIFO(int idProducto) {
        List<Lote> vigentes = new ArrayList<>();
        for (Lote l : listarTodos()) {
            if (l.getIdProducto() == idProducto && l.getCantidadActual() > 0 && !l.isVencido()) {
                vigentes.add(l);
            }
        }
        return vigentes;
    }

    // Lista los lotes de un producto que todavía tienen unidades en bodega
    public List<Lote> listarConStockPorProducto(int idProducto) {
        List<Lote> conStock = new ArrayList<>();
        for (Lote l : listarTodos()) {
            if (l.getIdProducto() == idProducto && l.getCantidadActual() > 0) {
                conStock.add(l);
            }
        }
        return conStock;
    }

    // Lista los lotes vencidos o que van a vencer en los próximos días indicados
    public List<Lote> listarProximosAVencer(int diasUmbral) {
        List<Lote> porVencer = new ArrayList<>();
        for (Lote l : repo.listarLotes()) {
            if (l.getCantidadActual() > 0 && (l.isVencido() || l.isProximoAVencer(diasUmbral))) {
                porVencer.add(l);
            }
        }
        return porVencer;
    }
}
