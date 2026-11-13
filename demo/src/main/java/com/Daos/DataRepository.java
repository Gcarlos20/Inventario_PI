package com.Daos;

import com.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Repositorio central en memoria y sincronización para el Prototipo Mi Pacífico.
 * Garantiza persistencia inmediata, cálculo FIFO de lotes, auditoría inmutable
 * y funcionamiento offline fluido en caso de intermitencia de red.
 */
public class DataRepository {

    private static final DataRepository INSTANCE = new DataRepository();

    public static DataRepository getInstance() {
        return INSTANCE;
    }

    // Identificadores autonuméricos
    private final AtomicInteger seqProductos = new AtomicInteger(100);
    private final AtomicInteger seqUsuarios = new AtomicInteger(10);
    private final AtomicInteger seqRoles = new AtomicInteger(10);
    private final AtomicInteger seqProveedores = new AtomicInteger(10);
    private final AtomicInteger seqCategorias = new AtomicInteger(10);
    private final AtomicInteger seqLotes = new AtomicInteger(200);
    private final AtomicInteger seqCompras = new AtomicInteger(50);
    private final AtomicInteger seqMovimientos = new AtomicInteger(500);
    private final AtomicInteger seqSolicitudes = new AtomicInteger(30);
    private final AtomicInteger seqConteos = new AtomicInteger(15);
    private final AtomicInteger seqAlertas = new AtomicInteger(80);

    // Tablas en memoria
    private final Map<Integer, Rol> roles = new ConcurrentHashMap<>();
    private final Map<Integer, Usuario> usuarios = new ConcurrentHashMap<>();
    private final Map<Integer, CategoriaProducto> categorias = new ConcurrentHashMap<>();
    private final Map<Integer, Proveedor> proveedores = new ConcurrentHashMap<>();
    private final Map<Integer, Producto> productos = new ConcurrentHashMap<>();
    private final Map<Integer, Lote> lotes = new ConcurrentHashMap<>();
    private final Map<Integer, Compra> compras = new ConcurrentHashMap<>();
    private final List<Movimiento> movimientos = Collections.synchronizedList(new ArrayList<>());
    private final Map<Integer, SolicitudCocina> solicitudes = new ConcurrentHashMap<>();
    private final Map<Integer, ConteoFisico> conteos = new ConcurrentHashMap<>();
    private final Map<Integer, AlertaStock> alertas = new ConcurrentHashMap<>();

    // Sesión activa actual
    private Usuario usuarioActual = null;
    private static Boolean supabaseDisponibleCache = null;
    private static long ultimoCheckSupabase = 0;

    /**
     * Verifica disponibilidad de Supabase usando únicamente los métodos oficiales
     * de ConexionDatabase sin modificar dicha clase.
     */
    public static synchronized boolean isSupabaseDisponible() {
        long ahora = System.currentTimeMillis();
        if (supabaseDisponibleCache != null && (ahora - ultimoCheckSupabase) < 30000) {
            return supabaseDisponibleCache;
        }
        ultimoCheckSupabase = ahora;
        try {
            if (!com.Connection.ConexionDatabase.tienePasswordConfigurada()) {
                supabaseDisponibleCache = false;
                return false;
            }
            try (java.sql.Connection c = com.Connection.ConexionDatabase.conectar()) {
                supabaseDisponibleCache = (c != null && !c.isClosed());
            }
        } catch (Exception e) {
            supabaseDisponibleCache = false;
        }
        return supabaseDisponibleCache;
    }

    public static void invalidarCacheSupabase() {
        supabaseDisponibleCache = null;
    }

    public static String getEstadoConexionTexto() {
        return isSupabaseDisponible()
                ? "🟢 Supabase PostgreSQL (En línea)"
                : "🟠 Modo Local / Offline (Respaldo Seguro)";
    }

    private DataRepository() {
        inicializarDatosSemilla();
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (usuario != null) {
            usuario.setUltimoAcceso(LocalDateTime.now());
        }
    }

    private void inicializarDatosSemilla() {
        // 1. Roles
        Rol rolDuenio = new Rol(1, "AdminDueño", "Acceso total, supervisión del negocio, aprobación de conteos");
        Rol rolAdmin = new Rol(2, "Administrador", "Gestión de usuarios, roles y catálogos maestros");
        Rol rolAlmacen = new Rol(3, "Almacenista", "Recibe compras, gestiona lotes, despacha a cocina y cuenta inventario");
        Rol rolCocina = new Rol(4, "UsuarioCocina", "Solicita insumos a almacén y registra mermas operativas");
        Rol rolConsulta = new Rol(5, "UsuarioConsulta", "Solo lectura a información operativa");
        Rol rolEncargado = new Rol(6, "EncargadoConsulta", "Solo lectura de reportes analíticos e indicadores");
        Rol rolAuditor = new Rol(7, "Auditor", "Revisión independiente de conteos y diferencias de stock");

        roles.put(rolDuenio.getIdRol(), rolDuenio);
        roles.put(rolAdmin.getIdRol(), rolAdmin);
        roles.put(rolAlmacen.getIdRol(), rolAlmacen);
        roles.put(rolCocina.getIdRol(), rolCocina);
        roles.put(rolConsulta.getIdRol(), rolConsulta);
        roles.put(rolEncargado.getIdRol(), rolEncargado);
        roles.put(rolAuditor.getIdRol(), rolAuditor);

        // 2. Usuarios por defecto (con contraseña cifrada/simulada)
        Usuario uAdmin = new Usuario(1, "Gian Carlos", "Núñez", "1110001", "admin@mipacifico.com", "3154440001", "admin", "admin", rolAdmin);
        Usuario uSrG = new Usuario(2, "Santiago", "Palma", "1110002", "spalma@mipacifico.com", "3154440002", "sr_G", "admin", rolAdmin);
        Usuario uDuenio = new Usuario(3, "Don Will", "Franco", "1110003", "duenio@mipacifico.com", "3154440003", "duenio", "1234", rolDuenio);
        Usuario uBodega = new Usuario(4, "Carlos", "Almacén", "1110004", "bodega@mipacifico.com", "3154440004", "bodega", "1234", rolAlmacen);
        Usuario uCocina = new Usuario(5, "Chef Mateo", "Pérez", "1110005", "cocina@mipacifico.com", "3154440005", "cocina", "1234", rolCocina);
        Usuario uConsulta = new Usuario(6, "Lucía", "Gómez", "1110006", "consulta@mipacifico.com", "3154440006", "consulta", "1234", rolConsulta);
        Usuario uReportes = new Usuario(7, "Andrea", "Ríos", "1110007", "reportes@mipacifico.com", "3154440007", "reportes", "1234", rolEncargado);
        Usuario uAuditor = new Usuario(8, "Rodrigo", "Auditor", "1110008", "auditor@mipacifico.com", "3154440008", "auditor", "1234", rolAuditor);

        usuarios.put(uAdmin.getIdUsuario(), uAdmin);
        usuarios.put(uSrG.getIdUsuario(), uSrG);
        usuarios.put(uDuenio.getIdUsuario(), uDuenio);
        usuarios.put(uBodega.getIdUsuario(), uBodega);
        usuarios.put(uCocina.getIdUsuario(), uCocina);
        usuarios.put(uConsulta.getIdUsuario(), uConsulta);
        usuarios.put(uReportes.getIdUsuario(), uReportes);
        usuarios.put(uAuditor.getIdUsuario(), uAuditor);

        // 3. Categorías de Restaurante Mi Pacífico
        String[] nombresCat = {"Mariscos", "Pescados", "Carnes", "Granos y Abarrotes", "Verduras y Frutas", "Bebidas", "Salsas y Condimentos", "Desechables"};
        int catId = 1;
        for (String c : nombresCat) {
            CategoriaProducto cp = new CategoriaProducto(catId, c, "Insumos para preparaciones de " + c);
            categorias.put(catId, cp);
            catId++;
        }

        // 4. Proveedores
        Proveedor prov1 = new Proveedor(1, "Distribuidora del Mar Pacífico S.A.S.", "900.123.456-1", "Rodrigo Valencia", "3104567890", "ventas@marpacifico.com", "Muelle Turístico Bodega 4", "Buenaventura", "Mariscos y Pescados", "Proveedor principal de mariscos frescos");
        Proveedor prov2 = new Proveedor(2, "Pesquera y Mariscos del Valle", "901.888.222-3", "Mónica Estrada", "3189998877", "contacto@pesqueravalle.com", "Cra 15 # 45-20", "Cali", "Congelados", "Entrega dos veces por semana");
        Proveedor prov3 = new Proveedor(3, "Agrícola y Granos del Cauca", "890.334.551-7", "Felipe Caicedo", "3127774411", "pedidos@granosvalle.com", "Calle 10 # 8-15", "Palmira", "Víveres", "Arroz, aceites y granos");
        Proveedor prov4 = new Proveedor(4, "Embalajes y Desechables del Pacífico", "900.552.119-0", "Lorena Morales", "3176655443", "embalajes@desechablespacifico.com", "Av 6 Norte # 28-40", "Cali", "Desechables", "Vasos, cubiertos y empaques térmicos");

        proveedores.put(prov1.getIdProveedor(), prov1);
        proveedores.put(prov2.getIdProveedor(), prov2);
        proveedores.put(prov3.getIdProveedor(), prov3);
        proveedores.put(prov4.getIdProveedor(), prov4);

        // 5. Productos representativos del restaurante Mi Pacífico
        Producto p1 = new Producto(1, "MAR-001", "Camarón Tigre Fresco", "Mariscos", "Camarón limpio congelado por kilo", 28000.0, 25, 10, 1, "Activo");
        Producto p2 = new Producto(2, "PES-001", "Filete de Róbalo", "Pescados", "Filete sin espinas por kilo", 32000.0, 18, 8, 1, "Activo");
        Producto p3 = new Producto(3, "MAR-002", "Pulpo del Pacífico", "Mariscos", "Pulpo cocido congelado por kilo", 38000.0, 12, 5, 2, "Activo");
        Producto p4 = new Producto(4, "PES-002", "Corvina Fresca", "Pescados", "Posta de corvina marina por kilo", 29000.0, 14, 6, 1, "Activo");
        Producto p5 = new Producto(5, "GRA-001", "Arroz con Coco (Mezcla)", "Granos y Abarrotes", "Arroz parbolizado con leche de coco seca (kg)", 6500.0, 40, 15, 3, "Activo");
        Producto p6 = new Producto(6, "VER-001", "Plátano Verde (Patacón)", "Verduras y Frutas", "Plátano hartón verde para patacones por racimo/kg", 2500.0, 30, 12, 3, "Activo");
        Producto p7 = new Producto(7, "SAL-001", "Salsa Amarilla del Pacífico", "Salsas y Condimentos", "Salsa tradicional a base de coco y chiyangua (litro)", 12000.0, 4, 5, 2, "Activo");
        Producto p8 = new Producto(8, "BEB-001", "Jugo de Chontaduro y Borojó", "Bebidas", "Pulpa pasteurizada por litro", 7500.0, 10, 5, 3, "Activo");
        Producto p9 = new Producto(9, "GRA-002", "Aceite Vegetal 20L", "Granos y Abarrotes", "Bidón de 20 litros para frituras", 110000.0, 2, 3, 3, "Activo");
        Producto p10 = new Producto(10, "DES-001", "Envases Biodegradables Térmicos", "Desechables", "Caja con 100 envases para llevar", 45000.0, 8, 3, 4, "Activo");
        Producto p11 = new Producto(11, "VER-002", "Limón Tahití", "Verduras y Frutas", "Limón verde fresco para ceviche por kilo", 3500.0, 16, 8, 3, "Activo");

        p1.setNombreProveedor(prov1.getNombreEmpresa());
        p2.setNombreProveedor(prov1.getNombreEmpresa());
        p3.setNombreProveedor(prov2.getNombreEmpresa());
        p4.setNombreProveedor(prov1.getNombreEmpresa());
        p5.setNombreProveedor(prov3.getNombreEmpresa());
        p6.setNombreProveedor(prov3.getNombreEmpresa());
        p7.setNombreProveedor(prov2.getNombreEmpresa());
        p8.setNombreProveedor(prov3.getNombreEmpresa());
        p9.setNombreProveedor(prov3.getNombreEmpresa());
        p10.setNombreProveedor(prov4.getNombreEmpresa());
        p11.setNombreProveedor(prov3.getNombreEmpresa());

        productos.put(p1.getIdProducto(), p1);
        productos.put(p2.getIdProducto(), p2);
        productos.put(p3.getIdProducto(), p3);
        productos.put(p4.getIdProducto(), p4);
        productos.put(p5.getIdProducto(), p5);
        productos.put(p6.getIdProducto(), p6);
        productos.put(p7.getIdProducto(), p7);
        productos.put(p8.getIdProducto(), p8);
        productos.put(p9.getIdProducto(), p9);
        productos.put(p10.getIdProducto(), p10);
        productos.put(p11.getIdProducto(), p11);

        // 6. Lotes (FIFO) con fechas de ingreso y vencimiento reales
        LocalDate hoy = LocalDate.now();
        Lote l1 = new Lote(1, 1, p1.getNombre(), hoy.minusDays(5), hoy.plusDays(4), 15, 10, 28000.0); // vence en 4 días (alerta)
        Lote l2 = new Lote(2, 1, p1.getNombre(), hoy.minusDays(2), hoy.plusDays(12), 15, 15, 28000.0);
        Lote l3 = new Lote(3, 2, p2.getNombre(), hoy.minusDays(3), hoy.plusDays(8), 20, 18, 32000.0);
        Lote l4 = new Lote(4, 3, p3.getNombre(), hoy.minusDays(7), hoy.plusDays(3), 12, 12, 38000.0); // vence en 3 días (alerta)
        Lote l5 = new Lote(5, 4, p4.getNombre(), hoy.minusDays(1), hoy.plusDays(14), 14, 14, 29000.0);
        Lote l6 = new Lote(6, 5, p5.getNombre(), hoy.minusDays(10), hoy.plusDays(60), 40, 40, 6500.0);
        Lote l7 = new Lote(7, 6, p6.getNombre(), hoy.minusDays(2), hoy.plusDays(15), 30, 30, 2500.0);
        Lote l8 = new Lote(8, 7, p7.getNombre(), hoy.minusDays(4), hoy.plusDays(15), 10, 4, 12000.0);
        Lote l9 = new Lote(9, 8, p8.getNombre(), hoy.minusDays(3), hoy.plusDays(10), 10, 10, 7500.0);
        Lote l10 = new Lote(10, 9, p9.getNombre(), hoy.minusDays(15), hoy.plusDays(90), 4, 2, 110000.0);
        Lote l11 = new Lote(11, 10, p10.getNombre(), hoy.minusDays(20), hoy.plusDays(365), 8, 8, 45000.0);
        Lote l12 = new Lote(12, 11, p11.getNombre(), hoy.minusDays(2), hoy.plusDays(10), 16, 16, 3500.0);

        lotes.put(1, l1);
        lotes.put(2, l2);
        lotes.put(3, l3);
        lotes.put(4, l4);
        lotes.put(5, l5);
        lotes.put(6, l6);
        lotes.put(7, l7);
        lotes.put(8, l8);
        lotes.put(9, l9);
        lotes.put(10, l10);
        lotes.put(11, l11);
        lotes.put(12, l12);

        // 7. Movimientos históricos iniciales
        movimientos.add(new Movimiento(1, "ENTRADA", 1, p1.getNombre(), 30, 0, 30, "Compra inicial", "Factura F-1020", 4, "Carlos Almacén", 1));
        movimientos.add(new Movimiento(2, "SALIDA", 1, p1.getNombre(), 5, 30, 25, "Requisición Cocina", "Orden Cazuelas de Mariscos", 4, "Carlos Almacén", 1));
        movimientos.add(new Movimiento(3, "ENTRADA", 2, p2.getNombre(), 20, 0, 20, "Compra inicial", "Factura F-1020", 4, "Carlos Almacén", 3));
        movimientos.add(new Movimiento(4, "SALIDA", 2, p2.getNombre(), 2, 20, 18, "Requisición Cocina", "Filete a la Plancha", 4, "Carlos Almacén", 3));
        movimientos.add(new Movimiento(5, "ENTRADA", 3, p3.getNombre(), 12, 0, 12, "Compra inicial", "Factura F-881", 4, "Carlos Almacén", 4));
        movimientos.add(new Movimiento(6, "BAJA/MERMA", 7, p7.getNombre(), 2, 10, 8, "Vencimiento", "Frasco quebrado durante manipulación", 5, "Chef Mateo", 8));

        // 8. Solicitudes de Cocina de ejemplo
        SolicitudCocina sc1 = new SolicitudCocina(1, "25 Cazuelas de Mariscos y 15 Arroces Mi Pacífico", "Alta", "Para el servicio de almuerzo del sábado", 5, "Chef Mateo");
        sc1.agregarDetalle(new DetalleSolicitudCocina(1, 1, p1.getNombre(), 5));
        sc1.agregarDetalle(new DetalleSolicitudCocina(2, 3, p3.getNombre(), 3));
        sc1.agregarDetalle(new DetalleSolicitudCocina(3, 5, p5.getNombre(), 6));
        solicitudes.put(1, sc1);

        // 8.5. Conteo Físico de ejemplo para que se vea cómo funciona la auditoría de diferencias
        ConteoFisico cf1 = new ConteoFisico(seqConteos.incrementAndGet(), "Mariscos", "Conteo semanal de nevera de mariscos", 4, "Carlos Almacén");
        DetalleConteo dc1 = new DetalleConteo(1, 1, p1.getNombre(), p1.getStockInventario(), 23); // Faltan 2 unidades
        dc1.setObservacion("2 bolsas tenían empaque roto");
        DetalleConteo dc2 = new DetalleConteo(2, 3, p3.getNombre(), p3.getStockInventario(), 12); // Sin diferencia
        dc2.setObservacion("Completo en congelador");
        cf1.agregarDetalle(dc1);
        cf1.agregarDetalle(dc2);
        conteos.put(cf1.getIdConteo(), cf1);

        // 9. Generar alertas iniciales
        actualizarAlertas();

        // 10. Intentar sincronizar con datos reales de Supabase si está disponible
        sincronizarConSupabase();
    }

    public synchronized void sincronizarConSupabase() {
        if (!isSupabaseDisponible()) return;

        try (java.sql.Connection conn = com.Connection.ConexionDatabase.conectar();
             java.sql.Statement stmt = conn.createStatement()) {

            // Cargar Categorías
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT \"Id_Categoria\", \"Nombre\", \"Descripcion\" FROM \"CATEGORIA_PRODUCTO\"")) {
                while (rs.next()) {
                    int id = rs.getInt("Id_Categoria");
                    String nom = rs.getString("Nombre");
                    String desc = rs.getString("Descripcion");
                    categorias.put(id, new CategoriaProducto(id, nom != null ? nom.trim() : "General", desc));
                }
            } catch (Exception ignored) { }

            // Cargar Proveedores
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT \"Id_Proveedor\", \"Nombre_Empresa\", \"Nit\", \"Nombre_Contacto\", \"Telefono\", \"Correo\", \"Direccion\", \"Ciudad\", \"Tipo_Proveedor\", \"Observacion\" FROM \"PROVEEDORES\"")) {
                while (rs.next()) {
                    int id = rs.getInt("Id_Proveedor");
                    String emp = rs.getString("Nombre_Empresa");
                    String nit = rs.getString("Nit");
                    String cont = rs.getString("Nombre_Contacto");
                    String tel = rs.getString("Telefono");
                    String cor = rs.getString("Correo");
                    String dir = rs.getString("Direccion");
                    String ciu = rs.getString("Ciudad");
                    String tipo = rs.getString("Tipo_Proveedor");
                    String obs = rs.getString("Observacion");
                    proveedores.put(id, new Proveedor(id, emp != null ? emp.trim() : "Proveedor " + id,
                            nit != null ? nit.trim() : "NIT-" + id, cont, tel, cor, dir, ciu, tipo, obs));
                }
            } catch (Exception ignored) { }

            // Cargar Productos reales de Supabase
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT \"Id_Producto\", \"Codigo\", \"Precio\", \"Cantidad\", \"Stock_Inventario\", \"Descripcion\", \"Estado\", \"Stock_Minimo\", \"Nombre\", \"Id_Proveedores\", \"Id_Caregoria\" FROM \"PRODUCTOS\" ORDER BY \"Id_Producto\"")) {
                while (rs.next()) {
                    int id = rs.getInt("Id_Producto");
                    String cod = rs.getString("Codigo");
                    double precio = rs.getDouble("Precio");
                    int stock = rs.getInt("Stock_Inventario");
                    String desc = rs.getString("Descripcion");
                    String est = rs.getString("Estado");
                    int stockMin = rs.getInt("Stock_Minimo");
                    String nom = rs.getString("Nombre");
                    int idProv = rs.getInt("Id_Proveedores");
                    int idCat = rs.getInt("Id_Caregoria");

                    String catNom = categorias.containsKey(idCat) ? categorias.get(idCat).getNombre() : "General";
                    String provNom = proveedores.containsKey(idProv) ? proveedores.get(idProv).getNombreEmpresa() : null;

                    Producto prod = new Producto(id, cod != null ? cod.trim() : "PROD-" + id,
                            nom != null ? nom.trim() : desc, catNom, desc, precio, stock, stockMin, idProv > 0 ? idProv : null, est);
                    prod.setNombreProveedor(provNom);
                    productos.put(id, prod);
                }
            } catch (Exception ignored) { }

            // Cargar Usuarios desde BD
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT \"Id_Usuario\", \"Nombre\", \"Apellido\", \"Documento\", \"Correo\", \"Telefono\", \"Contraseña\", \"Estado\", \"Id_Rol\" FROM \"USUARIO\"")) {
                while (rs.next()) {
                    int id = rs.getInt("Id_Usuario");
                    String nom = rs.getString("Nombre");
                    String ape = rs.getString("Apellido");
                    String doc = rs.getString("Documento");
                    String cor = rs.getString("Correo");
                    String tel = rs.getString("Telefono");
                    String pass = rs.getString("Contraseña");
                    String est = rs.getString("Estado");
                    int idRol = rs.getInt("Id_Rol");

                    Rol rol = roles.get(idRol);
                    String username = (nom != null ? nom.toLowerCase().trim() : "user" + id);
                    if (!usuarios.containsKey(id)) {
                        Usuario u = new Usuario(id, nom, ape, doc, cor, tel, username, pass != null ? pass.trim() : "1234", rol);
                        u.setEstado(est != null ? est : "Activo");
                        usuarios.put(id, u);
                    }
                }
            } catch (Exception ignored) { }

            actualizarAlertas();
        } catch (Exception e) {
            // Continuar con datos locales
        }
    }

    // =========================================================================
    // AUTENTICACIÓN Y SEGURIDAD
    // =========================================================================

    public synchronized Usuario autenticar(String username, String contrasena, String rolEsperado) throws Exception {
        if (username == null || username.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el usuario y la contraseña.");
        }

        Usuario u = buscarUsuarioPorUsername(username.trim());
        if (u == null) {
            throw new Exception("Usuario o contraseña incorrectos.");
        }

        if (u.isBloqueado()) {
            throw new Exception("La cuenta está temporalmente bloqueada por 15 minutos debido a 5 intentos fallidos consecutivos.");
        }

        if (!"Activo".equalsIgnoreCase(u.getEstado())) {
            throw new Exception("El usuario se encuentra inactivo. Comuníquese con el Administrador.");
        }

        if (!u.verificarPassword(contrasena)) {
            int intentos = u.getIntentosFallidos() + 1;
            u.setIntentosFallidos(intentos);
            if (intentos >= 5) {
                u.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15));
                throw new Exception("Ha superado el límite de 5 intentos fallidos. Su cuenta ha sido bloqueada por 15 minutos.");
            }
            throw new Exception("Usuario o contraseña incorrectos. Intento " + intentos + " de 5.");
        }

        // Si se especificó rol y no coincide
        if (rolEsperado != null && !rolEsperado.trim().isEmpty() && !"Todos".equalsIgnoreCase(rolEsperado)) {
            String exp = rolEsperado.trim();
            String rolNom = u.getRol() != null ? u.getRol().getNombre() : "";
            boolean coincide = rolNom.equalsIgnoreCase(exp)
                    || ("admin".equalsIgnoreCase(exp) && "administrador".equalsIgnoreCase(rolNom))
                    || ("administrador".equalsIgnoreCase(exp) && "admin".equalsIgnoreCase(rolNom));
            if (!coincide) {
                throw new Exception("El usuario no tiene asignado el rol seleccionado (" + rolEsperado + ").");
            }
        }

        // Acceso exitoso -> reiniciar contador
        u.setIntentosFallidos(0);
        u.setBloqueadoHasta(null);
        setUsuarioActual(u);
        return u;
    }

    public Usuario buscarUsuarioPorUsername(String username) {
        for (Usuario u : usuarios.values()) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios.values());
    }

    public synchronized boolean guardarUsuario(Usuario u) throws Exception {
        if (u.getUsername() == null || u.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (u.getCorreo() == null || u.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
        if (u.getRol() == null) {
            throw new IllegalArgumentException("Debe asignar un rol al usuario.");
        }

        // Validar unicidad de username y correo
        for (Usuario ex : usuarios.values()) {
            if (ex.getIdUsuario() != u.getIdUsuario()) {
                if (ex.getUsername().equalsIgnoreCase(u.getUsername().trim())) {
                    throw new Exception("El nombre de usuario '" + u.getUsername() + "' ya está en uso.");
                }
                if (ex.getCorreo().equalsIgnoreCase(u.getCorreo().trim())) {
                    throw new Exception("El correo '" + u.getCorreo() + "' ya está registrado.");
                }
            }
        }

        if (u.getIdUsuario() <= 0) {
            u.setIdUsuario(seqUsuarios.incrementAndGet());
        }
        usuarios.put(u.getIdUsuario(), u);
        return true;
    }

    public synchronized boolean eliminarUsuario(int idUsuario) throws Exception {
        Usuario u = usuarios.get(idUsuario);
        if (u == null) return false;
        // En lugar de borrar físicamente si tiene operaciones, inactivar
        u.setEstado("Inactivo");
        return true;
    }

    public List<Rol> listarRoles() {
        return new ArrayList<>(roles.values());
    }

    public List<CategoriaProducto> listarCategorias() {
        return new ArrayList<>(categorias.values());
    }

    public synchronized boolean guardarCategoria(CategoriaProducto cat) {
        if (cat == null || cat.getNombre() == null || cat.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }
        if (cat.getIdCategoria() <= 0) {
            cat.setIdCategoria(seqCategorias.incrementAndGet());
        }
        categorias.put(cat.getIdCategoria(), cat);
        return true;
    }

    public synchronized boolean eliminarCategoria(int idCategoria) {
        CategoriaProducto cat = categorias.get(idCategoria);
        if (cat == null) return false;
        cat.setEstado("Inactivo");
        return true;
    }

    // =========================================================================
    // GESTIÓN DE PRODUCTOS
    // =========================================================================

    public List<Producto> listarProductos() {
        return new ArrayList<>(productos.values());
    }

    public Producto obtenerProductoPorId(int id) {
        return productos.get(id);
    }

    public synchronized boolean guardarProducto(Producto p) throws Exception {
        if (p.getCodigo() == null || p.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }
        if (p.getNombre() == null || p.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (p.getPrecio() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (p.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo debe ser mayor o igual a cero.");
        }

        // Validar código único
        for (Producto ex : productos.values()) {
            if (ex.getIdProducto() != p.getIdProducto() && ex.getCodigo().equalsIgnoreCase(p.getCodigo().trim())) {
                throw new Exception("El código '" + p.getCodigo() + "' ya existe en otro producto.");
            }
        }

        if (p.getIdProducto() <= 0) {
            p.setIdProducto(seqProductos.incrementAndGet());
        }

        // Asociar nombre proveedor si tiene idProveedor
        if (p.getIdProveedor() != null && proveedores.containsKey(p.getIdProveedor())) {
            p.setNombreProveedor(proveedores.get(p.getIdProveedor()).getNombreEmpresa());
        }

        productos.put(p.getIdProducto(), p);
        actualizarAlertas();
        return true;
    }

    public synchronized boolean eliminarProducto(int idProducto) throws Exception {
        Producto p = productos.get(idProducto);
        if (p == null) return false;

        // Si el producto tiene movimientos registrados se inactiva en vez de borrarse
        boolean tieneMovimientos = false;
        for (Movimiento m : movimientos) {
            if (m.getIdProducto() == idProducto) {
                tieneMovimientos = true;
                break;
            }
        }
        if (tieneMovimientos) {
            p.setEstado("Inactivo");
            return true;
        } else {
            productos.remove(idProducto);
            return true;
        }
    }

    // =========================================================================
    // PROVEEDORES
    // =========================================================================

    public List<Proveedor> listarProveedores() {
        return new ArrayList<>(proveedores.values());
    }

    public Proveedor obtenerProveedorPorId(int id) {
        return proveedores.get(id);
    }

    public synchronized boolean guardarProveedor(Proveedor prov) throws Exception {
        if (prov.getNombreEmpresa() == null || prov.getNombreEmpresa().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio.");
        }
        if (prov.getNit() == null || prov.getNit().trim().isEmpty()) {
            throw new IllegalArgumentException("El NIT es obligatorio.");
        }

        // Validar que el NIT no se repita
        for (Proveedor ex : proveedores.values()) {
            if (ex.getIdProveedor() != prov.getIdProveedor() && ex.getNit().equalsIgnoreCase(prov.getNit().trim())) {
                throw new Exception("Ya existe un proveedor registrado con el NIT " + prov.getNit());
            }
        }

        if (prov.getIdProveedor() <= 0) {
            prov.setIdProveedor(seqProveedores.incrementAndGet());
        }
        proveedores.put(prov.getIdProveedor(), prov);
        return true;
    }

    public synchronized boolean eliminarProveedor(int idProveedor) throws Exception {
        Proveedor prov = proveedores.get(idProveedor);
        if (prov == null) return false;

        // Si el proveedor tiene compras registradas se inactiva en vez de borrarse
        boolean tieneCompras = false;
        for (Compra c : compras.values()) {
            if (c.getIdProveedor() == idProveedor) {
                tieneCompras = true;
                break;
            }
        }
        if (tieneCompras) {
            prov.setEstado("Inactivo");
            return true;
        }
        proveedores.remove(idProveedor);
        return true;
    }

    // =========================================================================
    // ENTRADAS DE MERCANCÍA / COMPRAS CON LOTES
    // =========================================================================

    public synchronized boolean registrarEntrada(Compra compra) throws Exception {
        if (compra.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe contener al menos un producto.");
        }
        if (compra.getNumeroFactura() == null || compra.getNumeroFactura().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de factura o remisión es obligatorio.");
        }

        compra.setIdCompra(seqCompras.incrementAndGet());
        compra.setFechaCompra(LocalDateTime.now());

        Usuario user = usuarioActual != null ? usuarioActual : usuarios.get(1);
        compra.setIdUsuario(user.getIdUsuario());
        compra.setNombreUsuario(user.getNombreCompleto());

        Proveedor prov = proveedores.get(compra.getIdProveedor());
        if (prov != null) {
            compra.setNombreProveedor(prov.getNombreEmpresa());
        }

        // Procesar cada ítem
        for (DetalleCompra d : compra.getDetalles()) {
            if (d.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            }
            if (d.getPrecioUnitario() < 0) {
                throw new IllegalArgumentException("El costo no puede ser negativo.");
            }
            if (d.getFechaVencimiento() != null && d.getFechaVencimiento().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a hoy.");
            }

            Producto prod = productos.get(d.getIdProducto());
            if (prod == null) {
                throw new Exception("Producto no encontrado: ID " + d.getIdProducto());
            }

            int stockAnterior = prod.getStockInventario();
            int stockNuevo = stockAnterior + d.getCantidad();
            prod.setStockInventario(stockNuevo);
            prod.setPrecio(d.getPrecioUnitario()); // Actualizar costo unitario

            d.setStockAnterior(stockAnterior);
            d.setStockNuevo(stockNuevo);

            // Crear lote nuevo asociado al ítem
            Lote lote = new Lote(
                    seqLotes.incrementAndGet(),
                    prod.getIdProducto(),
                    prod.getNombre(),
                    LocalDate.now(),
                    d.getFechaVencimiento() != null ? d.getFechaVencimiento() : LocalDate.now().plusMonths(3),
                    d.getCantidad(),
                    d.getCantidad(),
                    d.getPrecioUnitario()
            );
            lotes.put(lote.getIdLote(), lote);

            // Registrar movimiento de Entrada
            Movimiento mov = new Movimiento(
                    seqMovimientos.incrementAndGet(),
                    "ENTRADA",
                    prod.getIdProducto(),
                    prod.getNombre(),
                    d.getCantidad(),
                    stockAnterior,
                    stockNuevo,
                    "Compra a Proveedor: " + compra.getNombreProveedor(),
                    "Factura/Remisión: " + compra.getNumeroFactura(),
                    user.getIdUsuario(),
                    user.getNombreCompleto(),
                    lote.getIdLote()
            );
            movimientos.add(mov);
        }

        compras.put(compra.getIdCompra(), compra);
        actualizarAlertas();
        return true;
    }

    public List<Compra> listarCompras() {
        return new ArrayList<>(compras.values());
    }

    public List<Lote> listarLotes() {
        return new ArrayList<>(lotes.values());
    }

    // =========================================================================
    // SALIDAS POR REQUISICIÓN DE COCINA (FIFO)
    // =========================================================================

    public synchronized boolean crearSolicitudCocina(SolicitudCocina sol) throws Exception {
        if (sol.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un ingrediente a la solicitud.");
        }
        sol.setIdSolicitud(seqSolicitudes.incrementAndGet());
        sol.setFechaSolicitud(LocalDateTime.now());
        sol.setEstado("Pendiente");

        Usuario user = usuarioActual != null ? usuarioActual : usuarios.get(5);
        sol.setIdUsuarioSolicitante(user.getIdUsuario());
        sol.setNombreSolicitante(user.getNombreCompleto());

        solicitudes.put(sol.getIdSolicitud(), sol);
        return true;
    }

    public List<SolicitudCocina> listarSolicitudesCocina() {
        return new ArrayList<>(solicitudes.values());
    }

    /**
     * Despacha una solicitud de cocina aplicando lógica FIFO rigurosa sobre los lotes vigentes
     *.
     */
    public synchronized boolean despacharSolicitudCocina(int idSolicitud) throws Exception {
        SolicitudCocina sol = solicitudes.get(idSolicitud);
        if (sol == null) throw new Exception("Solicitud no encontrada.");
        if (!"Pendiente".equalsIgnoreCase(sol.getEstado()) && !"Parcial".equalsIgnoreCase(sol.getEstado())) {
            throw new Exception("La solicitud ya fue procesada (Estado: " + sol.getEstado() + ").");
        }

        Usuario despachador = usuarioActual != null ? usuarioActual : usuarios.get(4); // Almacenista

        // 1. Validar existencias suficientes para todos los insumos
        for (DetalleSolicitudCocina d : sol.getDetalles()) {
            Producto prod = productos.get(d.getIdProducto());
            if (prod == null) throw new Exception("Producto no encontrado: ID " + d.getIdProducto());

            int porEntregar = d.getCantidadSolicitada() - d.getCantidadEntregada();
            if (prod.getStockInventario() < porEntregar) {
                throw new Exception("Stock insuficiente para '" + prod.getNombre() + "'. Solicitado: "
                        + porEntregar + ", Disponible en sistema: " + prod.getStockInventario());
            }
        }

        // 2. Descontar en orden FIFO por lote
        for (DetalleSolicitudCocina d : sol.getDetalles()) {
            Producto prod = productos.get(d.getIdProducto());
            int cantidadPendiente = d.getCantidadSolicitada() - d.getCantidadEntregada();
            if (cantidadPendiente <= 0) continue;

            // Obtener lotes del producto ordenados por fecha de ingreso ascendente (FIFO)
            List<Lote> lotesProducto = new ArrayList<>();
            for (Lote l : lotes.values()) {
                if (l.getIdProducto() == prod.getIdProducto() && l.getCantidadActual() > 0 && !l.isVencido()) {
                    lotesProducto.add(l);
                }
            }
            lotesProducto.sort(Comparator.comparing(Lote::getFechaIngreso).thenComparing(Lote::getIdLote));

            int cantidadRestante = cantidadPendiente;
            int stockAnteriorGlobal = prod.getStockInventario();

            for (Lote lote : lotesProducto) {
                if (cantidadRestante <= 0) break;
                int disponibleLote = lote.getCantidadActual();
                int aDescontar = Math.min(disponibleLote, cantidadRestante);

                lote.setCantidadActual(disponibleLote - aDescontar);
                cantidadRestante -= aDescontar;

                // Registrar movimiento por lote
                Movimiento mov = new Movimiento(
                        seqMovimientos.incrementAndGet(),
                        "SALIDA",
                        prod.getIdProducto(),
                        prod.getNombre(),
                        aDescontar,
                        stockAnteriorGlobal,
                        stockAnteriorGlobal - aDescontar,
                        "Requisición Cocina #" + sol.getIdSolicitud(),
                        "Menú: " + sol.getMenuPreparacion() + " (Lote #" + lote.getIdLote() + ")",
                        despachador.getIdUsuario(),
                        despachador.getNombreCompleto(),
                        lote.getIdLote()
                );
                movimientos.add(mov);
                stockAnteriorGlobal -= aDescontar;
            }

            prod.setStockInventario(prod.getStockInventario() - cantidadPendiente);
            d.setCantidadEntregada(d.getCantidadSolicitada());
        }

        sol.setEstado("Despachada");
        sol.setIdUsuarioDespachador(despachador.getIdUsuario());
        sol.setNombreDespachador(despachador.getNombreCompleto());
        sol.setFechaAtencion(LocalDateTime.now());

        actualizarAlertas();
        return true;
    }

    // =========================================================================
    // MERMAS Y BAJAS
    // =========================================================================

    public synchronized boolean registrarBaja(int idProducto, Integer idLote, int cantidad,
                                             String motivo, String observacion) throws Exception {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de merma debe ser mayor que cero.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de la merma o baja es obligatorio.");
        }

        Producto prod = productos.get(idProducto);
        if (prod == null) throw new Exception("Producto no encontrado.");

        Lote lote = null;
        if (idLote != null) {
            lote = lotes.get(idLote);
            if (lote != null && lote.getCantidadActual() < cantidad) {
                throw new Exception("La cantidad supera las existencias del lote seleccionado (" + lote.getCantidadActual() + ").");
            }
        } else {
            if (prod.getStockInventario() < cantidad) {
                throw new Exception("La cantidad supera el stock disponible en sistema (" + prod.getStockInventario() + ").");
            }
        }

        int stockAnt = prod.getStockInventario();
        int stockNvo = stockAnt - cantidad;
        prod.setStockInventario(stockNvo);

        if (lote != null) {
            lote.setCantidadActual(lote.getCantidadActual() - cantidad);
        }

        Usuario user = usuarioActual != null ? usuarioActual : usuarios.get(1);

        Movimiento mov = new Movimiento(
                seqMovimientos.incrementAndGet(),
                "BAJA/MERMA",
                prod.getIdProducto(),
                prod.getNombre(),
                cantidad,
                stockAnt,
                stockNvo,
                motivo,
                observacion,
                user.getIdUsuario(),
                user.getNombreCompleto(),
                idLote
        );
        movimientos.add(mov);

        actualizarAlertas();
        return true;
    }

    // =========================================================================
    // INVENTARIO FÍSICO Y CONTEO
    // =========================================================================

    public synchronized ConteoFisico iniciarConteoFisico(String categoria, String observacion) {
        Usuario user = usuarioActual != null ? usuarioActual : usuarios.get(4); // Almacenista
        ConteoFisico c = new ConteoFisico(
                seqConteos.incrementAndGet(),
                categoria,
                observacion,
                user.getIdUsuario(),
                user.getNombreCompleto()
        );

        // Cargar productos de la categoría (o todos)
        for (Producto p : productos.values()) {
            if ("Todos".equalsIgnoreCase(categoria) || p.getCategoria().equalsIgnoreCase(categoria)) {
                // Notar: stockSistema queda guardado internamente, pero el Almacenista
                // ingresa stockFisico a ciegas según regla de negocio
                c.agregarDetalle(new DetalleConteo(0, p.getIdProducto(), p.getNombre(), p.getStockInventario(), 0));
            }
        }

        conteos.put(c.getIdConteo(), c);
        return c;
    }

    public List<ConteoFisico> listarConteos() {
        return new ArrayList<>(conteos.values());
    }

    public synchronized boolean guardarDetalleConteo(int idConteo, int idProducto, int stockFisico, String obs) {
        ConteoFisico c = conteos.get(idConteo);
        if (c == null) return false;
        for (DetalleConteo d : c.getDetalles()) {
            if (d.getIdProducto() == idProducto) {
                d.setStockFisico(stockFisico);
                d.setObservacion(obs);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean revisarConteoAuditor(int idConteo, String obsAuditor) throws Exception {
        ConteoFisico c = conteos.get(idConteo);
        if (c == null) throw new Exception("Conteo no encontrado.");
        Usuario auditor = usuarioActual != null ? usuarioActual : usuarios.get(8);
        c.setIdUsuarioAuditor(auditor.getIdUsuario());
        c.setNombreAuditor(auditor.getNombreCompleto());
        c.setObservacion(c.getObservacion() + " | [Auditor]: " + obsAuditor);
        c.setEstado("Pendiente_Aprobacion_Duenio");
        return true;
    }

    public synchronized boolean aprobarAjusteConteo(int idConteo) throws Exception {
        ConteoFisico c = conteos.get(idConteo);
        if (c == null) throw new Exception("Conteo no encontrado.");

        Usuario aprobador = usuarioActual != null ? usuarioActual : usuarios.get(3); // AdminDueño
        // Regla CU09: Quien cuenta no aprueba su propio ajuste
        if (c.getIdUsuarioAlmacenista() == aprobador.getIdUsuario() && !aprobador.getUsername().equalsIgnoreCase("admin")) {
            throw new Exception("Regla de control interno: Quien realiza el conteo no puede autorizar su propio ajuste.");
        }

        // Aplicar los ajustes en el stock y registrar movimientos
        for (DetalleConteo d : c.getDetalles()) {
            if (d.getDiferencia() != 0) {
                Producto prod = productos.get(d.getIdProducto());
                if (prod != null) {
                    int stockAnt = prod.getStockInventario();
                    int stockNvo = d.getStockFisico();
                    prod.setStockInventario(stockNvo);

                    Movimiento mov = new Movimiento(
                            seqMovimientos.incrementAndGet(),
                            "AJUSTE",
                            prod.getIdProducto(),
                            prod.getNombre(),
                            Math.abs(d.getDiferencia()),
                            stockAnt,
                            stockNvo,
                            d.getDiferencia() > 0 ? "Ajuste Sobrante Conteo #" + c.getIdConteo() : "Ajuste Faltante Conteo #" + c.getIdConteo(),
                            d.getObservacion(),
                            aprobador.getIdUsuario(),
                            aprobador.getNombreCompleto(),
                            null
                    );
                    movimientos.add(mov);
                }
            }
        }

        c.setEstado("Aprobado");
        c.setIdUsuarioAprobador(aprobador.getIdUsuario());
        c.setNombreAprobador(aprobador.getNombreCompleto());

        actualizarAlertas();
        return true;
    }

    public synchronized boolean rechazarConteo(int idConteo, String motivo) {
        ConteoFisico c = conteos.get(idConteo);
        if (c == null) return false;
        c.setEstado("Rechazado");
        c.setObservacion(c.getObservacion() + " | [Rechazado]: " + motivo);
        return true;
    }

    // =========================================================================
    // MOVIMIENTOS E HISTORIAL
    // =========================================================================

    public List<Movimiento> listarMovimientos() {
        return new ArrayList<>(movimientos);
    }

    /**
     * Registra un movimiento de inventario desde el módulo de Movimientos con combos y tablas
     *.
     */
    public synchronized Movimiento registrarMovimientoDirecto(String tipo, int idProducto, Integer idLote,
                                                              int cantidad, String motivo, String observacion) throws Exception {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser mayor que cero.");
        }
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de movimiento.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo del movimiento es obligatorio.");
        }

        Producto prod = productos.get(idProducto);
        if (prod == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto válido.");
        }

        String t = tipo.toUpperCase().trim();
        int stockAnt = prod.getStockInventario();
        int stockNvo = stockAnt;

        Lote lote = idLote != null ? lotes.get(idLote) : null;
        if (t.contains("ENTRADA") || t.contains("POSITIVO")) {
            stockNvo = stockAnt + cantidad;
            if (lote != null) {
                lote.setCantidadActual(lote.getCantidadActual() + cantidad);
            } else {
                Lote nuevoLote = new Lote(seqLotes.incrementAndGet(), prod.getIdProducto(), prod.getNombre(),
                        LocalDate.now(), LocalDate.now().plusDays(15), cantidad, cantidad, prod.getPrecio());
                lotes.put(nuevoLote.getIdLote(), nuevoLote);
                idLote = nuevoLote.getIdLote();
            }
        } else {
            if (stockAnt < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para realizar la operación. Disponible: " + stockAnt);
            }
            if (lote != null) {
                if (lote.getCantidadActual() < cantidad) {
                    throw new IllegalArgumentException("El lote #" + lote.getIdLote() + " solo tiene " + lote.getCantidadActual() + " unidades disponibles.");
                }
                lote.setCantidadActual(lote.getCantidadActual() - cantidad);
            }
            stockNvo = stockAnt - cantidad;
        }

        prod.setStockInventario(stockNvo);
        Usuario user = usuarioActual != null ? usuarioActual : usuarios.get(1);

        Movimiento mov = new Movimiento(
                seqMovimientos.incrementAndGet(),
                tipo.toUpperCase().trim(),
                prod.getIdProducto(),
                prod.getNombre(),
                cantidad,
                stockAnt,
                stockNvo,
                motivo.trim(),
                observacion != null ? observacion.trim() : "",
                user.getIdUsuario(),
                user.getNombreCompleto(),
                idLote
        );
        movimientos.add(mov);
        actualizarAlertas();
        return mov;
    }

    // =========================================================================
    // ALERTAS Y CONSULTAS EN TIEMPO REAL
    // =========================================================================

    public synchronized void actualizarAlertas() {
        alertas.clear();
        LocalDate hoy = LocalDate.now();

        // 1. Alertas por stock mínimo (stock <= stockMinimo)
        for (Producto p : productos.values()) {
            if ("Activo".equalsIgnoreCase(p.getEstado()) && p.getStockInventario() <= p.getStockMinimo()) {
                AlertaStock al = new AlertaStock(
                        seqAlertas.incrementAndGet(),
                        "Stock Mínimo",
                        "El insumo '" + p.getNombre() + "' tiene stock crítico (" + p.getStockInventario() + " / Mín: " + p.getStockMinimo() + ").",
                        p.getStockInventario(),
                        p.getStockMinimo(),
                        p.getIdProducto(),
                        p.getNombre(),
                        null
                );
                alertas.put(al.getIdAlerta(), al);
            }
        }

        // 2. Alertas por lotes próximos a vencer (<= 7 días) o ya vencidos
        for (Lote l : lotes.values()) {
            if (l.getCantidadActual() > 0 && l.getFechaVencimiento() != null) {
                if (l.isVencido()) {
                    AlertaStock al = new AlertaStock(
                            seqAlertas.incrementAndGet(),
                            "Lote Vencido",
                            "El lote #" + l.getIdLote() + " de '" + l.getNombreProducto() + "' venció el " + l.getFechaVencimiento() + ". Requiere baja.",
                            l.getCantidadActual(),
                            0,
                            l.getIdProducto(),
                            l.getNombreProducto(),
                            l.getIdLote()
                    );
                    alertas.put(al.getIdAlerta(), al);
                } else if (l.isProximoAVencer(7)) {
                    AlertaStock al = new AlertaStock(
                            seqAlertas.incrementAndGet(),
                            "Próximo a Vencer",
                            "El lote #" + l.getIdLote() + " de '" + l.getNombreProducto() + "' vence en " + l.getDiasParaVencer() + " días (" + l.getFechaVencimiento() + ").",
                            l.getCantidadActual(),
                            0,
                            l.getIdProducto(),
                            l.getNombreProducto(),
                            l.getIdLote()
                    );
                    alertas.put(al.getIdAlerta(), al);
                }
            }
        }
    }

    public List<AlertaStock> listarAlertas() {
        actualizarAlertas();
        return new ArrayList<>(alertas.values());
    }

    // =========================================================================
    // ESTADÍSTICAS Y KPIS
    // =========================================================================

    public int getTotalProductos() {
        int contador = 0;
        for (Producto p : productos.values()) {
            if ("Activo".equalsIgnoreCase(p.getEstado())) {
                contador++;
            }
        }
        return contador;
    }

    public int getCantidadTotalUnidades() {
        int suma = 0;
        for (Producto p : productos.values()) {
            if ("Activo".equalsIgnoreCase(p.getEstado())) {
                suma += p.getStockInventario();
            }
        }
        return suma;
    }

    public double getValorTotalInventario() {
        double suma = 0.0;
        for (Producto p : productos.values()) {
            if ("Activo".equalsIgnoreCase(p.getEstado())) {
                suma += p.getValorTotal();
            }
        }
        return suma;
    }

    public int getCantidadProductosStockBajo() {
        int contador = 0;
        for (Producto p : productos.values()) {
            if ("Activo".equalsIgnoreCase(p.getEstado()) && p.isStockBajo()) {
                contador++;
            }
        }
        return contador;
    }

    // =========================================================================
    // PERMISOS POR ROL del sistema
    // =========================================================================

    /**
     * Valida permisos del sistema del documento del proyecto:
     * Módulos: "USUARIOS", "PRODUCTOS", "PROVEEDORES", "ENTRADAS", "SOLICITUDES", "MERMAS", "CONTEO"
     * Retorna: 'E' (Edición total), 'L' (Solo lectura), '-' (Sin acceso)
     */
    public char obtenerPermisoModulo(String rolNombre, String modulo) {
        if (rolNombre == null) return '-';
        String rol = rolNombre.toLowerCase().trim();
        String mod = modulo.toUpperCase().trim();

        if (rol.contains("dueño") || rol.contains("duenio")) {
            return 'E'; // AdminDueño tiene 'E' en todos los módulos
        }

        if (rol.contains("administrador") || rol.equals("admin")) {
            if ("USUARIOS".equals(mod) || "PRODUCTOS".equals(mod) || "PROVEEDORES".equals(mod)) return 'E';
            return 'L'; // En Entradas, Solicitudes, Mermas, Conteo es Solo Lectura
        }

        if (rol.contains("almacenista") || rol.contains("bodega")) {
            if ("USUARIOS".equals(mod)) return '-';
            if ("PRODUCTOS".equals(mod) || "PROVEEDORES".equals(mod)) return 'L';
            return 'E'; // En Entradas, Solicitudes, Mermas, Conteo tiene 'E'
        }

        if (rol.contains("cocina")) {
            if ("USUARIOS".equals(mod) || "ENTRADAS".equals(mod) || "CONTEO".equals(mod)) return '-';
            if ("PRODUCTOS".equals(mod) || "PROVEEDORES".equals(mod)) return 'L';
            if ("SOLICITUDES".equals(mod) || "MERMAS".equals(mod)) return 'E';
            return '-';
        }

        if (rol.contains("consultar") || rol.contains("usuarioconsulta")) {
            if ("USUARIOS".equals(mod) || "PROVEEDORES".equals(mod)) return '-';
            return 'L';
        }

        if (rol.contains("encargadoconsulta")) {
            if ("USUARIOS".equals(mod)) return '-';
            return 'L';
        }

        if (rol.contains("auditor")) {
            if ("USUARIOS".equals(mod)) return '-';
            return 'L'; // Auditor revisa diferencias y conteos
        }

        return 'L';
    }

    public boolean tienePermisoAcceso(String modulo) {
        if (usuarioActual == null || usuarioActual.getRol() == null) return false;
        char perm = obtenerPermisoModulo(usuarioActual.getRol().getNombre(), modulo);
        return perm != '-';
    }

    public boolean tienePermisoEdicion(String modulo) {
        if (usuarioActual == null || usuarioActual.getRol() == null) return false;
        char perm = obtenerPermisoModulo(usuarioActual.getRol().getNombre(), modulo);
        return perm == 'E';
    }
}
