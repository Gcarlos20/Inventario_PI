package com.inventario_pi;

import com.Daos.ProductoDAO;
import com.model.Producto;
import java.sql.SQLException;

public class ListarProductos {

    public static void consultarProductos() {
        try {
            var productos = new ProductoDAO().listarTodos();
            System.out.println("\n--- LISTADO DE PRODUCTOS EN INVENTARIO ---");
            if (productos.isEmpty()) {
                System.out.println("No se encontraron productos registrados en la tabla.");
                return;
            }

            for (Producto producto : productos) {
                System.out.println("ID: " + producto.getIdProducto()
                        + " | Código: " + producto.getCodigo()
                        + " | Descripción: " + producto.getDescripcion()
                        + " | Stock: " + producto.getStockInventario()
                        + " | Precio: $" + producto.getPrecio()
                        + " | Estado: " + producto.getEstado()
                        + " | Stock mínimo: " + producto.getStockMinimo());
            }
        } catch (SQLException | IllegalStateException e) {
            System.err.println("Error al consultar los productos: " + e.getMessage());
        }
    }
}
