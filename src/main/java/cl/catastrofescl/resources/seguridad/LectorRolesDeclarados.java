package cl.catastrofescl.resources.seguridad;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Normaliza roles provenientes de custom claims Firebase, headers HTTP o listas CSV.
 */
public final class LectorRolesDeclarados {

    private LectorRolesDeclarados() {
    }

    public static Set<String> desdeClaim(Object claim) {
        if (claim == null) {
            return Set.of();
        }
        if (claim instanceof Collection<?> coleccion) {
            return desdeColeccion(coleccion);
        }
        if (claim.getClass().isArray()) {
            return desdeArreglo((Object[]) claim);
        }
        if (claim instanceof String texto) {
            return desdeListaSeparadaPorComa(texto);
        }
        return Set.of(claim.toString().trim());
    }

    public static Set<String> desdeListaSeparadaPorComa(String listaSeparadaPorComa) {
        if (!StringUtils.hasText(listaSeparadaPorComa)) {
            return Set.of();
        }
        return Arrays.stream(listaSeparadaPorComa.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    public static Set<String> desdeClaimsFirebase(Map<String, Object> claims) {
        if (claims == null || claims.isEmpty()) {
            return Set.of();
        }
        Set<String> roles = new HashSet<>(desdeClaim(claims.get("roles")));
        if (!roles.isEmpty()) {
            return roles;
        }
        return desdeClaim(claims.get("role"));
    }

    private static Set<String> desdeColeccion(Collection<?> coleccion) {
        Set<String> roles = new HashSet<>();
        for (Object item : coleccion) {
            if (item == null) {
                continue;
            }
            if (item instanceof Collection<?> anidada) {
                roles.addAll(desdeColeccion(anidada));
            } else if (item.getClass().isArray()) {
                roles.addAll(desdeArreglo((Object[]) item));
            } else {
                String texto = item.toString().trim();
                if (StringUtils.hasText(texto)) {
                    roles.add(texto);
                }
            }
        }
        return roles;
    }

    private static Set<String> desdeArreglo(Object[] arreglo) {
        Set<String> roles = new HashSet<>();
        for (Object item : arreglo) {
            if (item != null && StringUtils.hasText(item.toString())) {
                roles.add(item.toString().trim());
            }
        }
        return roles;
    }
}
