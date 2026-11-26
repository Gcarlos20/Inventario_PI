package com.Daos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import com.model.Producto;
import org.junit.jupiter.api.Test;

class ProductoDAOTest {
    @Test
    void buscaDescripcionSinImportarAcentosOMayusculas() {
        Producto producto = producto("MAR-1", "Camarón fresco");

        assertTrue(ProductoDAO.coincideBusqueda("camaron", producto));
        assertTrue(ProductoDAO.coincideBusqueda("CAMARÓN", producto));
        assertFalse(ProductoDAO.coincideBusqueda("calamar", producto));
    }

    @Test
    void buscaCoincidenciasParcialesEnElCodigo() {
        Producto producto = producto("CAM-042", "Producto del mar");

        assertTrue(ProductoDAO.coincideBusqueda("cam", producto));
        assertTrue(ProductoDAO.coincideBusqueda("042", producto));
    }

    @Test
    void terminoVacioCoincideConCualquierProducto() {
        assertTrue(ProductoDAO.coincideBusqueda("  ", producto("A-1", "Arroz")));
    }

    @Test
    void insertarLlamaRutinaYRecibeId() throws Exception {
        Fixture f = new Fixture();
        assertEquals(42, f.dao().agregarYObtenerId(producto("A-1", "Arroz")));
        assertTrue(f.sql.contains("insertar_producto"));
        assertEquals("A-1", f.parametros.get(1));
        assertEquals(7, f.parametroSalida);
    }

    @Test
    void consultarPasaIdYMapeaResultado() throws Exception {
        Fixture f = new Fixture();
        Producto resultado = f.dao().obtenerPorId(21);
        assertTrue(f.sql.contains("consultar_producto"));
        assertEquals(21, f.parametros.get(1));
        assertEquals("Arroz", resultado.getDescripcion());
    }

    @Test
    void actualizarPasaIdYLeeSalida() throws Exception {
        Fixture f = new Fixture();
        f.salida = true;
        assertTrue(f.dao().actualizar(new Producto(8, "P-8", 2.0, 7,
                "Arroz", "Disponible", 2)));
        assertTrue(f.sql.contains("actualizar_producto"));
        assertEquals(8, f.parametros.get(1));
        assertEquals(8, f.parametroSalida);
    }

    @Test
    void eliminarPasaIdYLeeSalida() throws Exception {
        Fixture f = new Fixture();
        f.salida = true;
        assertTrue(f.dao().eliminar(8));
        assertTrue(f.sql.contains("eliminar_producto"));
        assertEquals(8, f.parametros.get(1));
        assertEquals(2, f.parametroSalida);
    }

    private Producto producto(String codigo, String descripcion) {
        return new Producto(1, codigo, 1.0, 1, descripcion, "Activo", 0);
    }

    private static final class Fixture {
        private final Map<Integer, Object> parametros = new HashMap<>();
        private String sql;
        private int parametroSalida;
        private Object salida = 42;
        private int siguienteFila;

        private ProductoDAO dao() {
            Connection conexion = proxy(Connection.class, (metodo, args) -> {
                if (metodo.equals("prepareCall") || metodo.equals("prepareStatement")) {
                    sql = (String) args[0];
                    Class<?> tipo = metodo.equals("prepareCall")
                            ? CallableStatement.class : PreparedStatement.class;
                    return proxy(tipo, this::statement);
                }
                return valorPrimitivo(metodo);
            });
            return new ProductoDAO(() -> conexion);
        }

        private Object statement(String metodo, Object[] args) {
            if (metodo.startsWith("set") && args[0] instanceof Integer) {
                parametros.put((Integer) args[0], args[1]);
            } else if (metodo.equals("registerOutParameter")) {
                parametroSalida = (Integer) args[0];
            } else if (metodo.equals("executeQuery")) {
                return proxy(ResultSet.class, this::fila);
            } else if (metodo.equals("getInt") && args[0] instanceof Integer) {
                return salida;
            } else if (metodo.equals("getBoolean")) {
                return salida;
            }
            return valorPrimitivo(metodo);
        }

        private Object fila(String metodo, Object[] args) {
            if (metodo.equals("next")) return siguienteFila++ == 0;
            if (metodo.equals("getInt")) {
                return switch ((String) args[0]) {
                    case "Id_Producto" -> 21;
                    case "Stock_Inventario" -> 7;
                    case "Stock_Minimo" -> 2;
                    default -> 0;
                };
            }
            if (metodo.equals("getDouble")) return 2.5;
            if (metodo.equals("getString")) {
                return switch ((String) args[0]) {
                    case "Codigo" -> "P-21";
                    case "Descripcion" -> "Arroz";
                    case "Estado" -> "Disponible";
                    default -> null;
                };
            }
            return valorPrimitivo(metodo);
        }

        private static <T> T proxy(Class<T> tipo, Handler handler) {
            return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo},
                    (obj, method, args) -> handler.call(method.getName(), args == null ? new Object[0] : args)));
        }

        private static Object valorPrimitivo(String metodo) {
            return switch (metodo) {
                case "execute" -> true;
                case "wasNull" -> false;
                case "close" -> null;
                default -> null;
            };
        }

        @FunctionalInterface
        private interface Handler {
            Object call(String method, Object[] args) throws Throwable;
        }
    }
}
