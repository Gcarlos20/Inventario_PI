package com.Daos;

import com.Connection.ConexionDatabase;
import com.model.Producto;
import java.text.Normalizer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Consultas de acceso a datos para el catálogo de productos. */
public class ProductoDAO {
    // Se usa una sola lista de columnas para mantener iguales todas las lecturas del catálogo.
    private static final String COLUMNAS =
            "\"Id_Producto\", \"Codigo\", \"Precio\", \"Stock_Inventario\", "
                    + "\"Descripcion\", \"Estado\", \"Stock_Minimo\"";

    private final ConnectionProvider connectionProvider;

    public ProductoDAO() {
        this(ConexionDatabase::conectar);
    }

    ProductoDAO(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    private Connection obtenerConexion() throws SQLException {
        Connection conexion = connectionProvider.conectar();
        if (conexion == null) {
            throw new SQLException("No se pudo establecer conexión con la base de datos.");
        }
        return conexion;
    }

    /** Devuelve el catálogo ordenado por su identificador interno. */
    public List<Producto> listarTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS
                + " FROM public.\"PRODUCTOS\" ORDER BY \"Id_Producto\"";

        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultados = consulta.executeQuery()) {
            while (resultados.next()) {
                productos.add(mapear(resultados));
            }
        }
        return productos;
    }

    /** Busca un producto por el ID interno numérico. */
    public Producto obtenerPorId(int id) throws SQLException {
        // Este bloque consulta un producto por ID; el resultado llega en el ResultSet.
        String sql = "SELECT * FROM public.consultar_producto(?)";

        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            try (ResultSet resultados = consulta.executeQuery()) {
                return resultados.next() ? mapear(resultados) : null;
            }
        }
    }

    /** Inserta un producto y devuelve el ID interno generado por PostgreSQL. */
    public int agregarYObtenerId(Producto producto) throws SQLException {
        // Envía los datos del producto (IN) y recibe el ID creado (OUT).
        String sql = "{ call public.insertar_producto(CAST(? AS text), ?, ?, "
                + "CAST(? AS text), CAST(? AS text), ?, ?) }";

        try (Connection conexion = obtenerConexion();
             CallableStatement llamada = conexion.prepareCall(sql)) {
            llamada.setString(1, producto.getCodigo().trim());
            llamada.setDouble(2, producto.getPrecio());
            llamada.setInt(3, producto.getStockInventario());
            llamada.setString(4, producto.getDescripcion());
            llamada.setString(5, producto.getEstado());
            llamada.setInt(6, producto.getStockMinimo());
            llamada.registerOutParameter(7, Types.INTEGER);
            llamada.execute();
            int id = llamada.getInt(7);
            if (llamada.wasNull()) {
                throw new SQLException("La base de datos no devolvió el ID del producto creado.");
            }
            return id;
        }
    }

    /** Conserva el método booleano usado por las pantallas CRUD antiguas. */
    public boolean agregar(Producto producto) throws SQLException {
        return agregarYObtenerId(producto) > 0;
    }

    /** Actualiza todos los datos editables del producto indicado por su ID interno. */
    public boolean actualizar(Producto producto) throws SQLException {
        // Envía el ID y los campos nuevos (IN); recibe si se actualizó (OUT).
        String sql = "{ call public.actualizar_producto(?, CAST(? AS text), ?, ?, "
                + "CAST(? AS text), CAST(? AS text), ?, ?) }";

        try (Connection conexion = obtenerConexion();
             CallableStatement llamada = conexion.prepareCall(sql)) {
            llamada.setInt(1, producto.getIdProducto());
            llamada.setString(2, producto.getCodigo().trim());
            llamada.setDouble(3, producto.getPrecio());
            llamada.setInt(4, producto.getStockInventario());
            llamada.setString(5, producto.getDescripcion());
            llamada.setString(6, producto.getEstado());
            llamada.setInt(7, producto.getStockMinimo());
            llamada.registerOutParameter(8, Types.BOOLEAN);
            llamada.execute();
            return llamada.getBoolean(8);
        }
    }

    /** Elimina el producto identificado por su ID interno. */
    public boolean eliminar(int id) throws SQLException {
        // Envía el ID del producto (IN) y recibe si se eliminó (OUT).
        String sql = "{ call public.eliminar_producto(?, ?) }";

        try (Connection conexion = obtenerConexion();
             CallableStatement llamada = conexion.prepareCall(sql)) {
            llamada.setInt(1, id);
            llamada.registerOutParameter(2, Types.BOOLEAN);
            llamada.execute();
            return llamada.getBoolean(2);
        }
    }

    /** Busca por coincidencia parcial o devuelve todo el catálogo cuando el término está vacío. */
    public List<Producto> buscarPorCodigoONombre(String termino) throws SQLException {
        String textoNormalizado = normalizar(termino);
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM public.\"PRODUCTOS\" "
                + "ORDER BY LOWER(\"Descripcion\"), \"Id_Producto\"";

        try (Connection conexion = obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            try (ResultSet resultados = consulta.executeQuery()) {
                while (resultados.next()) {
                    Producto producto = mapear(resultados);
                    if (coincideBusquedaNormalizada(textoNormalizado, producto)) {
                        productos.add(producto);
                    }
                }
            }
        }
        return productos;
    }

    static boolean coincideBusqueda(String termino, Producto producto) {
        return coincideBusquedaNormalizada(normalizar(termino), producto);
    }

    private static boolean coincideBusquedaNormalizada(String terminoNormalizado, Producto producto) {
        return terminoNormalizado.isEmpty()
                || normalizar(producto.getDescripcion()).contains(terminoNormalizado)
                || normalizar(producto.getCodigo()).contains(terminoNormalizado);
    }

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }

    /** Lee y bloquea la fila para que el stock previo no cambie durante la compra. */
    public int obtenerStockParaActualizar(Connection conexion, int idProducto) throws SQLException {
        String sql = "SELECT \"Stock_Inventario\" FROM public.\"PRODUCTOS\" "
                + "WHERE \"Id_Producto\" = ? FOR UPDATE";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, idProducto);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SQLException("No existe el producto con ID " + idProducto + ".");
                }
                return resultado.getInt("Stock_Inventario");
            }
        }
    }

    /** Actualiza el stock dentro de la transacción que entrega el llamador. */
    public void actualizarStock(Connection conexion, int idProducto, int stockNuevo)
            throws SQLException {
        String sql = "UPDATE public.\"PRODUCTOS\" SET \"Stock_Inventario\" = ?, "
                + "\"Estado\" = CASE WHEN ? <= 0 THEN 'Agotado' "
                + "WHEN ? <= \"Stock_Minimo\" THEN 'Poco stock' ELSE 'Disponible' END "
                + "WHERE \"Id_Producto\" = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, stockNuevo);
            consulta.setInt(2, stockNuevo);
            consulta.setInt(3, stockNuevo);
            consulta.setInt(4, idProducto);
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar el stock del producto " + idProducto + ".");
            }
        }
    }

    /** Convierte una fila SQL en el modelo usado por las pantallas. */
    private Producto mapear(ResultSet resultados) throws SQLException {
        return new Producto(
                resultados.getInt("Id_Producto"),
                resultados.getString("Codigo"),
                resultados.getDouble("Precio"),
                resultados.getInt("Stock_Inventario"),
                resultados.getString("Descripcion"),
                resultados.getString("Estado"),
                resultados.getInt("Stock_Minimo"));
    }
}

@FunctionalInterface
interface ConnectionProvider {
    Connection conectar() throws SQLException;
}
