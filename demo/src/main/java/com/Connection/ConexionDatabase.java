package com.Connection;
import java.sql.Connection; // para establecer la conexión con la base de datos
import java.sql.DriverManager; // para establecer la conexión con la base de datos
import java.sql.SQLException; // para manejar las excepciones de SQL

public class ConexionDatabase {
    private static final String URL = "jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?user=postgres.nnmmgfksbcyfadfnedrb&password=Slytherin_77";

    public static Connection conectar() throws SQLException {
        // La URL ya contiene el usuario de Supabase; no se debe reemplazar por "postgres".
        return DriverManager.getConnection(URL);
    }
}
