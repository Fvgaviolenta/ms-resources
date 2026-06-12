package cl.catastrofescl.resources.seguridad;

import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Convierte los codigos de rol que suelen usarse en custom claims de Firebase
 * ({@code ADMIN}, {@code AUTHORITY}, etc.) a los codigos internos del dominio
 * ({@code ADMINISTRADOR}, {@code AUTORIDAD}, ...). Los roles que ya vienen en
 * forma interna se dejan igual.
 */
public final class MapeadorRolesFirebase {

    private static final Map<String, String> ALIAS_FIREBASE_A_INTERNO = Map.ofEntries(
            Map.entry("ADMIN", ProveedorPermisos.ROL_ADMINISTRADOR),
            Map.entry("AUTHORITY", ProveedorPermisos.ROL_AUTORIDAD),
            Map.entry("OPERATOR", ProveedorPermisos.ROL_OPERADOR),
            Map.entry("CITIZEN", ProveedorPermisos.ROL_PARTICULAR),
            Map.entry("VOLUNTEER", ProveedorPermisos.ROL_VOLUNTARIO)
    );

    private static final Set<String> ROLES_INTERNOS = Set.of(
            ProveedorPermisos.ROL_ADMINISTRADOR,
            ProveedorPermisos.ROL_AUTORIDAD,
            ProveedorPermisos.ROL_OPERADOR,
            ProveedorPermisos.ROL_PARTICULAR,
            ProveedorPermisos.ROL_VOLUNTARIO
    );

    private MapeadorRolesFirebase() {
    }

    /**
     * @param rolesCrudos valores del claim {@code roles} o {@code role} (pueden mezclar alias e internos)
     * @return conjunto de roles internos unicos, sin nulos ni vacios
     */
    public static Set<String> normalizar(Collection<String> rolesCrudos) {
        Set<String> resultado = new HashSet<>();
        if (rolesCrudos == null) {
            return resultado;
        }
        for (String crudo : rolesCrudos) {
            String interno = normalizarUnRol(crudo);
            if (interno != null) {
                resultado.add(interno);
            }
        }
        return resultado;
    }

    /**
     * @param rolCrudo un solo rol o alias
     * @return codigo interno o null si no es reconocible
     */
    public static String normalizarUnRol(String rolCrudo) {
        if (!StringUtils.hasText(rolCrudo)) {
            return null;
        }
        String t = rolCrudo.trim();
        if (ROLES_INTERNOS.contains(t)) {
            return t;
        }
        String mayus = t.toUpperCase();
        if (ROLES_INTERNOS.contains(mayus)) {
            return mayus;
        }
        return ALIAS_FIREBASE_A_INTERNO.getOrDefault(mayus, null);
    }
}

