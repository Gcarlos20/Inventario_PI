package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Compra;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Clase DAO para registrar y consultar las compras o entradas de mercancía.
 */
public class CompraDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Registra una nueva compra y actualiza el stock de los productos comprados
    public boolean registrarCompra(Compra compra) throws Exception {
        boolean ok = repo.registrarEntrada(compra);

        if (DataRepository.isSupabaseDisponible()) {
            String sql = "INSERT INTO \"COMPRAS\" (\"Id_Compra\", \"Numero_Factura\", \"Total_Compra\", "
                    + "\"Metodo_Pago\", \"Observacion\", \"PROVEEDORES_Id_Proveedor\", \"USUARIO_Id_Usuario\") "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = ConexionDatabase.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, compra.getIdCompra());
                ps.setString(2, compra.getNumeroFactura());
                ps.setDouble(3, compra.getTotalCompra());
                ps.setString(4, compra.getMetodoPago());
                ps.setString(5, compra.getObservacion());
                ps.setInt(6, compra.getIdProveedor());
                ps.setInt(7, compra.getIdUsuario());
                ps.executeUpdate();
            } catch (SQLException e) {
            }
        }
        return ok;
    }

    // Lista todas las compras realizadas
    public List<Compra> listarTodas() {
        return repo.listarCompras();
    }

    // Busca una compra por su ID usando un ciclo for sencillo
    public Compra obtenerPorId(int idCompra) {
        for (Compra c : repo.listarCompras()) {
            if (c.getIdCompra() == idCompra) {
                return c;
            }
        }
        return null;
    }
}
