package com.Connection;
import java.sql.Connection; // para establecer la conexión con la base de datos
import java.sql.DriverManager; // para establecer la conexión con la base de datos
import java.sql.SQLException; // para manejar las excepciones de SQL

public class ConexionDatabase {
    private static final String URL = "jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres";
    private static final String USUARIO = "postgres.nnmmgfksbcyfadfnedrb";

    public static Connection conectar() throws SQLException {
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new SQLException("Falta configurar DB_PASSWORD con la contraseña de Supabase.");
        }
        return DriverManager.getConnection(URL, USUARIO, password);
    }
}
