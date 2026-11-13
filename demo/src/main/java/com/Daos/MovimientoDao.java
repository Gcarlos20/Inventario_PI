package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Movimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Clase DAO para registrar y consultar el historial de movimientos del inventario
 * (entradas, salidas, bajas y ajustes).
 */
public class MovimientoDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Lista todos los movimientos desde el más reciente hasta el más antiguo
    public List<Movimiento> listarTodos() {
        List<Movimiento> lista = new ArrayList<>(repo.listarMovimientos());
        lista.sort(Comparator.comparing(Movimiento::getIdMovimiento).reversed());
        return lista;
    }

    // Filtra el historial de movimientos por tipo, producto, usuario y texto
    public List<Movimiento> filtrar(String tipo, Integer idProducto, String usuario, String texto) {
        String busqueda = texto != null ? texto.trim().toLowerCase() : "";
        List<Movimiento> resultado = new ArrayList<>();

        for (Movimiento m : listarTodos()) {
            boolean coincideTipo = (tipo == null || "Todos".equalsIgnoreCase(tipo))
                    || (m.getTipoMovimiento() != null && m.getTipoMovimiento().toUpperCase().contains(tipo.toUpperCase()));

            boolean coincideProd = (idProducto == null || idProducto <= 0)
                    || (m.getIdProducto() == idProducto);

            boolean coincideUsuario = (usuario == null || "Todos".equalsIgnoreCase(usuario))
                    || (m.getNombreUsuario() != null && m.getNombreUsuario().equalsIgnoreCase(usuario));

            boolean coincideTexto = busqueda.isEmpty()
                    || (m.getNombreProducto() != null && m.getNombreProducto().toLowerCase().contains(busqueda))
                    || (m.getMotivo() != null && m.getMotivo().toLowerCase().contains(busqueda))
                    || (m.getObservacion() != null && m.getObservacion().toLowerCase().contains(busqueda))
                    || (m.getNombreUsuario() != null && m.getNombreUsuario().toLowerCase().contains(busqueda));

            if (coincideTipo && coincideProd && coincideUsuario && coincideTexto) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    // Registra una baja o merma de un producto por daño, pérdida o vencimiento
    public boolean registrarMermaOBaja(int idProducto, Integer idLote, int cantidad,
                                       String motivo, String observacion) throws Exception {
        return repo.registrarBaja(idProducto, idLote, cantidad, motivo, observacion);
    }

    // Registra un movimiento de inventario y lo guarda en la base de datos
    public Movimiento registrarMovimiento(String tipo, int idProducto, Integer idLote,
                                          int cantidad, String motivo, String observacion) throws Exception {
        Movimiento mov = repo.registrarMovimientoDirecto(tipo, idProducto, idLote, cantidad, motivo, observacion);

        if (DataRepository.isSupabaseDisponible()) {
            String sql = "INSERT INTO \"MOVIMIENTOS\" (\"Id_Movientos\", \"Tipo_Movimiento\", \"Cantidad\", "
                    + "\"Stock_Anterior\", \"Stock_nuevo\", \"Motivo\", \"Observacion\", "
                    + "\"PRODUCTOS_Id_Producto\", \"USUARIO_Id_Usuario\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, mov.getIdMovimiento());
                ps.setInt(2, obtenerCodigoTipo(mov.getTipoMovimiento()));
                ps.setInt(3, mov.getCantidad());
                ps.setInt(4, mov.getStockAnterior());
                ps.setInt(5, mov.getStockNuevo());
                ps.setString(6, mov.getMotivo());
                ps.setString(7, mov.getObservacion());
                ps.setInt(8, mov.getIdProducto());
                ps.setInt(9, mov.getIdUsuario());
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
        return mov;
    }

    // Convierte el nombre del tipo de movimiento en su código numérico para la base de datos
    private int obtenerCodigoTipo(String tipo) {
        if (tipo == null) return 1;
        String t = tipo.toUpperCase();
        if (t.contains("ENTRADA")) return 1;
        if (t.contains("SALIDA")) return 2;
        if (t.contains("BAJA") || t.contains("MERMA")) return 3;
        return 4;
    }
}
