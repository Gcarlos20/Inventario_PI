package com.Connection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionDatabase {

    private static final String URL =
            "jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:5432/postgres";
    private static final String USUARIO = "postgres.nnmmgfksbcyfadfnedrb";
    private static volatile String passwordSesion;

    private ConexionDatabase() {
    }

    public static void configurarPasswordSesion(String password) {
        passwordSesion = password;
    }

    public static boolean tienePasswordConfigurada() throws SQLException {
        return resolverPassword() != null;
    }

    public static Connection conectar() throws SQLException {
        String password = resolverPassword();
        if (password == null || password.isBlank()) {
            throw new SQLException("Configura la contraseña de Supabase antes de conectar.");
        }

        DriverManager.setLoginTimeout(3);
        return DriverManager.getConnection(URL, USUARIO, password);
    }

    private static String resolverPassword() throws SQLException {
        String password = System.getenv("DB_PASSWORD");
        if (password != null && !password.isBlank()) {
            return password;
        }

        Path configPath = encontrarConfigProperties();
        if (configPath != null) {
            Properties properties = new Properties();
            try (var reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException e) {
                throw new SQLException("No se pudo leer el archivo config.properties.", e);
            }
            password = properties.getProperty("DB_PASSWORD");
            if (password != null && !password.isBlank()) {
                return password;
            }
        }

        return passwordSesion;
    }

    private static Path encontrarConfigProperties() {
        Path directorioActual = Path.of(System.getProperty("user.dir"));
        Path configLocal = directorioActual.resolve("config.properties");
        if (Files.isRegularFile(configLocal)) {
            return configLocal;
        }

        Path configDemo = directorioActual.resolve("demo").resolve("config.properties");
        return Files.isRegularFile(configDemo) ? configDemo : null;
    }
}
