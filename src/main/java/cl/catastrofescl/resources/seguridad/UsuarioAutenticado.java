package cl.catastrofescl.resources.seguridad;

import java.util.Set;
import java.util.UUID;

/**
 * Datos del usuario autenticado en la peticion actual.
 * - firebaseUid: identificador externo de Firebase.
 * - usuarioId: UUID local que usan las tablas (se deriva o se pasa como override en DEV).
 * - roles: roles internos normalizados ({@link MapeadorRolesFirebase} mapea ADMIN -> ADMINISTRADOR).
 * - permisos: permisos derivados de esos roles mediante {@link ProveedorPermisos}.
 */
public record UsuarioAutenticado(
        String firebaseUid,
        UUID usuarioId,
        Set<String> roles,
        Set<String> permisos
) {
}

