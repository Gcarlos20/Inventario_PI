```mermaid
classDiagram
    namespace Entidades {
        class Usuario {
            -idUsuario: int
            -nombre: String
            -apellido: String
            -documento: String
            -email: String
            -telefono: String
            -estado: String
            -fechaRegistro: Date
            -rol: Rol
            +getIdUsuario() int
            +getNombre() String
            +getRol() Rol
        }
        class Rol {
            -idRol: int
            -nombre: String
            -descripcion: String
            +getIdRol() int
            +getNombre() String
        }
        class Producto {
            -idProducto: int
            -codigo: String
            -nombre: String
            -categoria: String
            -descripcion: String
            -precio: double
            -cantidad: int
            -stockMinimo: int
            -stockMaximo: int
            -proveedorId: Integer
            +getIdProducto() int
            +getCodigo() String
            +getNombre() String
            +getCategoria() String
            +getPrecio() double
            +getCantidad() int
            +getStockMinimo() int
            +getProveedorId() Integer
        }
        class Proveedor {
            -idProveedor: int
            -nombreEmpresa: String
            -nit: String
            -contacto: String
            -telefono: String
            -email: String
            -direccion: String
            +getIdProveedor() int
            +getNombreEmpresa() String
        }
        class Movimiento {
            -idMovimiento: int
            -fechaMovimiento: Date
            -cantidad: int
            -producto: Producto
            -tipoMovimiento: TipoMovimiento
            -usuario: Usuario
            -lote: Lote
            +getIdMovimiento() int
            +getCantidad() int
        }
        class TipoMovimiento {
            -idTipoMovimiento: int
            -nombreMovimiento: String
            -naturaleza: String
            -descripcion: String
        }
        class Lote {
            -idLote: int
            -fechaIngreso: Date
            -fechaVencimiento: Date
            -cantidadInicial: int
            -cantidadActual: int
            -precioUnitario: double
            -producto: Producto
            +getIdLote() int
            +getCantidadActual() int
        }
        class AlertaStock {
            -idAlerta: int
            -tipoAlerta: String
            -mensaje: String
            -nivelPrioridad: String
            -producto: Producto
            +getIdAlerta() int
            +getMensaje() String
        }
        class SolicitudCocina {
            -idSolicitud: int
            -fechaSolicitud: Date
            -estado: String
            -usuario: Usuario
            -detalles: List~DetalleSolicitud~
            +getIdSolicitud() int
            +agregarDetalle(detalle: DetalleSolicitud)
        }
        class DetalleSolicitud {
            -idDetalle: int
            -cantidadSolicitada: int
            -cantidadEntregada: int
            -producto: Producto
        }
        class ConteoFisico {
            -idConteo: int
            -fechaConteo: Date
            -estado: String
            -usuario: Usuario
            -detalles: List~DetalleConteo~
        }
        class DetalleConteo {
            -idDetalleConteo: int
            -stockSistema: int
            -stockReal: int
            -diferencia: int
            -producto: Producto
        }
    }

    namespace AccesoDatos {
        class ConexionBD {
            -conexion: Connection
            +getConexion() Connection
            +desconectar() void
        }
        class ProductoDAO {
            +listarTodos() List~Producto~
            +agregar(p: Producto) boolean
            +actualizar(p: Producto) boolean
            +eliminar(id: int) boolean
            +obtenerPorId(id: int) Producto
        }
        class UsuarioDAO {
            +autenticar(user: String, pass: String) Usuario
            +listar() List~Usuario~
        }
        class MovimientoDAO {
            +registrarMovimiento(m: Movimiento) boolean
            +obtenerHistorial() List~Movimiento~
        }
        class SolicitudCocinaDAO {
            +crearSolicitud(s: SolicitudCocina) boolean
            +actualizarEstado(id: int, estado: String) boolean
        }
    }

    ProductoDAO ..> ConexionBD : conexion
    UsuarioDAO ..> ConexionBD : conexion
    MovimientoDAO ..> ConexionBD : conexion
    SolicitudCocinaDAO ..> ConexionBD : conexion

    ProductoDAO ..> Producto : manipula
    UsuarioDAO ..> Usuario : manipula
    MovimientoDAO ..> Movimiento : manipula
    SolicitudCocinaDAO ..> SolicitudCocina : manipula

    Usuario "1" --> "1" Rol : pertenece_a
    Producto "0..*" --> "0..1" Proveedor : suministrado_por
    Lote "0..*" --> "1" Producto : pertenece_a
    AlertaStock "0..*" --> "1" Producto : notifica
    Movimiento "0..*" --> "1" Producto : afecta
    Movimiento "0..*" --> "1" TipoMovimiento : clasificado_por
    Movimiento "0..*" --> "1" Usuario : registrado_por
    Movimiento "0..*" --> "0..1" Lote : descuenta_de

    SolicitudCocina "1" --> "1" Usuario : solicitada_por
    SolicitudCocina "1" *-- "1..*" DetalleSolicitud : compone
    DetalleSolicitud "0..*" --> "1" Producto : requiere

    ConteoFisico "1" --> "1" Usuario : realizado_por
    ConteoFisico "1" *-- "1..*" DetalleConteo : compone
    DetalleConteo "0..*" --> "1" Producto : audita
    ```