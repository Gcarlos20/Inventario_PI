package com.Daos;

import com.model.Rol;
import java.util.List;

/**
 * Clase DAO para consultar los roles y permisos de los usuarios.
 */
public class RolesDao {

    private final DataRepository repo = DataRepository.getInstance();

    // Lista todos los roles registrados en el sistema
    public List<Rol> listarTodos() {
        return repo.listarRoles();
    }

    // Busca un rol por su ID usando un ciclo for sencillo
    public Rol obtenerPorId(int idRol) {
        for (Rol r : repo.listarRoles()) {
            if (r.getIdRol() == idRol) {
                return r;
            }
        }
        return null;
    }

    // Devuelve el permiso ('E' escritura, 'L' lectura, '-' sin acceso) para un rol y módulo
    public char obtenerPermisoModulo(String nombreRol, String modulo) {
        return repo.obtenerPermisoModulo(nombreRol, modulo);
    }
}
