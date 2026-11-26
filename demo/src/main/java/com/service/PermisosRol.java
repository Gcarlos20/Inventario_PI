package com.service;

import com.model.Usuario;
import java.text.Normalizer;
import java.util.Locale;

/** Reglas de consulta y operación para los roles de la aplicación. */
public final class PermisosRol {
    public enum Modulo {
        PANEL, CATALOGO, ESTADO, COMPRAS, PROVEEDORES, COCINA,
        MERMAS, CONTEO, REPORTES, HISTORIAL, GESTION_ROLES
    }

    public enum Accion { CONSULTAR, CREAR, ACTUALIZAR, ELIMINAR, PROCESAR }

    private PermisosRol() { }

    public static boolean permite(Usuario usuario, Modulo modulo, Accion accion) {
        String rol = normalizar(usuario == null || usuario.getRol() == null
                ? null : usuario.getRol().getNombre());
        boolean dueno = rol.equals("duenodelsistema") || rol.equals("duenosistema")
                || rol.equals("propietario") || rol.equals("admindueno");
        boolean administrador = rol.equals("administrador") || rol.equals("admin");
        if (dueno || administrador) {
            if (modulo == Modulo.GESTION_ROLES) return dueno && accion == Accion.CONSULTAR;
            return accion == Accion.CONSULTAR || admiteCambios(modulo);
        }

        switch (modulo) {
            case PANEL:
            case ESTADO:
                return accion == Accion.CONSULTAR;
            case CATALOGO:
                return catalogo(rol, accion);
            case COMPRAS:
                return esAlmacenista(rol)
                        || (accion == Accion.CONSULTAR && esAuditor(rol));
            case PROVEEDORES:
                return accion == Accion.CONSULTAR && (esAlmacenista(rol) || esAuditor(rol));
            case COCINA:
                if (esAlmacenista(rol)) return true;
                if (esCocina(rol)) return accion == Accion.CONSULTAR || accion == Accion.CREAR;
                return accion == Accion.CONSULTAR && esAuditor(rol);
            case MERMAS:
            case CONTEO:
                return esAlmacenista(rol)
                        || (accion == Accion.CONSULTAR && esAuditor(rol));
            case REPORTES:
                return accion == Accion.CONSULTAR && (esAlmacenista(rol) || esAuditor(rol)
                        || esConsulta(rol) || esEncargadoConsulta(rol));
            case HISTORIAL:
                return accion == Accion.CONSULTAR && (esAlmacenista(rol) || esAuditor(rol)
                        || esEncargadoConsulta(rol));
            case GESTION_ROLES:
                return false;
            default:
                return accion == Accion.CONSULTAR && esUsuarioBasico(rol);
        }
    }

    private static boolean catalogo(String rol, Accion accion) {
        if (accion == Accion.CONSULTAR) return esUsuarioBasico(rol) || esAlmacenista(rol)
                || esAuditor(rol) || esCocina(rol) || esConsulta(rol) || esEncargadoConsulta(rol);
        if (esAlmacenista(rol)) return accion == Accion.CREAR || accion == Accion.ACTUALIZAR;
        return false;
    }

    private static boolean admiteCambios(Modulo modulo) {
        return modulo != Modulo.PANEL && modulo != Modulo.ESTADO
                && modulo != Modulo.REPORTES && modulo != Modulo.HISTORIAL
                && modulo != Modulo.GESTION_ROLES;
    }

    private static boolean esAlmacenista(String rol) { return rol.equals("almacenista"); }
    private static boolean esAuditor(String rol) { return rol.equals("auditor"); }
    private static boolean esCocina(String rol) { return rol.equals("usuariococina"); }
    private static boolean esConsulta(String rol) {
        return rol.equals("usuarioconsulta") || rol.equals("usuarioconsuta");
    }
    private static boolean esEncargadoConsulta(String rol) {
        return rol.equals("encargadoconsulta") || rol.equals("engarcadoconsulta");
    }
    private static boolean esUsuarioBasico(String rol) {
        return rol.isEmpty() || rol.equals("usuario") || rol.equals("empleado")
                || rol.equals("auxiliar") || rol.equals("operador") || esCocina(rol)
                || esConsulta(rol) || esEncargadoConsulta(rol);
    }

    private static String normalizar(String nombre) {
        if (nombre == null) return "";
        return Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
