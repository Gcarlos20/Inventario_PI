package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Merma;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MermaDaoProcedureTest {

    @Test
    void daoInvocaProcedimientoConLosParametrosDeLaBaja() throws Exception {
        AtomicReference<String> sql = new AtomicReference<>();
        Map<Integer, Object> parametros = new HashMap<>();
        AtomicBoolean ejecutado = new AtomicBoolean();

        CallableStatement llamada = proxy(CallableStatement.class, (obj, metodo, args) -> {
            switch (metodo.getName()) {
                case "setInt", "setString" -> parametros.put((Integer) args[0], args[1]);
                case "execute" -> {
                    ejecutado.set(true);
                    return true;
                }
                default -> { }
            }
            return valorPrimitivo(metodo.getReturnType());
        });
        Connection conexion = proxy(Connection.class, (obj, metodo, args) -> {
            if (metodo.getName().equals("prepareCall")) {
                sql.set((String) args[0]);
                return llamada;
            }
            return valorPrimitivo(metodo.getReturnType());
        });

        new MermaDao().registrarBaja(conexion,
                new Merma(9, 4, 2, 6, "Producto dañado"),
                "Ajuste por daño", 8, 6, 3, "Agotado");

        assertTrue(sql.get().contains("public.registrar_baja_merma"));
        assertTrue(ejecutado.get());
        assertEquals(4, parametros.get(1)); // Producto
        assertEquals(9, parametros.get(2)); // Lote
        assertEquals(2, parametros.get(3)); // Cantidad
        assertEquals("Ajuste por daño", parametros.get(5));
        assertEquals(3, parametros.get(7)); // Usuario
        assertEquals(6, parametros.get(9)); // Stock final
    }

    @Test
    void procedimientoExisteYValidaCantidadSinModificarDatos() throws Exception {
        Assumptions.assumeTrue(ConexionDatabase.tienePasswordConfigurada(),
                "Configura DB_PASSWORD para ejecutar la prueba de integración.");

        try (Connection conexion = ConexionDatabase.conectar()) {
            conexion.setAutoCommit(false);
            try (CallableStatement llamada = conexion.prepareCall(
                    "{ call public.registrar_baja_merma(?, ?, ?, ?, "
                            + "CAST(? AS text), CAST(? AS text), ?, ?, ?, CAST(? AS text)) }")) {
                llamada.setInt(1, 1);
                llamada.setInt(2, 0);
                llamada.setInt(3, 0); // Inválido: el procedimiento debe rechazar antes de actualizar.
                llamada.setInt(4, 6);
                llamada.setString(5, "Prueba de integración");
                llamada.setString(6, "Sin cambios");
                llamada.setInt(7, 1);
                llamada.setInt(8, 0);
                llamada.setInt(9, 0);
                llamada.setString(10, "Agotado");

                SQLException error = assertThrows(SQLException.class, llamada::execute);
                assertTrue(error.getMessage().contains("cantidad o stock"), error.getMessage());
            } finally {
                conexion.rollback();
            }
        }
    }

    private static <T> T proxy(Class<T> tipo, java.lang.reflect.InvocationHandler handler) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(),
                new Class<?>[]{tipo}, handler));
    }

    private static Object valorPrimitivo(Class<?> tipo) {
        if (tipo == boolean.class) return false;
        if (tipo == int.class) return 0;
        if (tipo == long.class) return 0L;
        return null;
    }
}
