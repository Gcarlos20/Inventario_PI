package com.inventario_pi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ListarProductos {

    public static void consultarProductos() {
        String url = "jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:6543/postgres?sslmode=require";
        String usuario = "postgres.nnmmgfksbcyfadfnedrb";
        String contrasena = "";

        // Consultamos la tabla PRODUCTOS
        String sql = "SELECT Id_Producto, Codigo, Precio, Stock_Inventario, Descripcion, Estado, Stock_Minimo, Nombre FROM\"PRODUCTOS\"";

        try (Connection conexion = DriverManager.getConnection(url, usuario, contrasena);
             Statement statement = conexion.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("\n--- LISTADO DE PRODUCTOS EN INVENTARIO ---");
            
            boolean hayProductos = false;
            while (resultSet.next()) {
                hayProductos = true;
                
                // Usamos los nombres exactos de las columnas de tu base de datos
                int id = resultSet.getInt("Id_Producto");
                String codigo = resultSet.getString("Codigo");
                double precio = resultSet.getDouble("Precio");
                int stock = resultSet.getInt("Stock_Inventario");
                String descripcion = resultSet.getString("Descripcion");
                String estado = resultSet.getString("Estado");
                int stockMinimo = resultSet.getInt("Stock_Minimo");
                String nombre = resultSet.getString("Nombre");

                System.out.println("ID: " + id + " | Código: " + codigo + " | Producto: " + descripcion + " | Stock: " + stock + " | Precio: $" + precio + " | Estado: " + estado + " | Stock Mínimo: " + stockMinimo + " | Nombre: " + nombre);
            }

            if (!hayProductos) {
                System.out.println("No se encontraron productos registrados en la tabla.");
            }

        } catch (Exception e) {
            System.out.println("Error al consultar los productos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}