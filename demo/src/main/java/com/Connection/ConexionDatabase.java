package com.Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestión de la conexión a la base de datos PostgreSQL (Supabase)
 * para el Restaurante Mi Pacífico.
 * Soporta configuración por propiedades del sistema, variables de entorno,
 * credenciales oficiales y fallback seguro según RNF-005.
 */
public class ConexionDatabase {

    public static final String URL = "jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?user=postgres.nnmmgfksbcyfadfnedrb&password=Slytherin_77";

    private static String urlActual = System.getProperty("DB_URL",
            System.getenv().getOrDefault("DB_URL",
                    "jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres?user=postgres.nnmmgfksbcyfadfnedrb&password="
                            + System.getenv().getOrDefault("DB_PASSWORD", "Slytherin_77")));

    private static Boolean supabaseDisponible = null;
    private static long ultimoCheck = 0;

    public static synchronized Connection conectar() throws SQLException {
        // Establecer timeout corto para evitar congelar la interfaz si no hay internet
        DriverManager.setLoginTimeout(3);
        return DriverManager.getConnection(urlActual != null ? urlActual : URL);
    }

    public static synchronized boolean isSupabaseDisponible() {
        long ahora = System.currentTimeMillis();
        // Cachear estado por 30 segundos
        if (supabaseDisponible != null && (ahora - ultimoCheck) < 30000) {
            return supabaseDisponible;
        }

        ultimoCheck = ahora;
        try (Connection c = conectar()) {
            supabaseDisponible = (c != null && !c.isClosed());
        } catch (Exception e) {
            supabaseDisponible = false;
        }
        return supabaseDisponible;
    }

    public static void setUrl(String nuevaUrl) {
        urlActual = nuevaUrl;
        supabaseDisponible = null;
    }

    public static String getUrl() {
        return urlActual != null ? urlActual : URL;
    }

    public static String getEstadoConexionTexto() {
        return isSupabaseDisponible() ? "🟢 Supabase PostgreSQL (En línea)" : "🟠 Modo Local / Offline (Respaldo Seguro)";
    }
}
