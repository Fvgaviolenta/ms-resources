package cl.catastrofescl.resources.seguridad;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Resuelve permisos del modulo RECURSOS a partir de roles internos.
 *
 * <p>Carga {@code classpath:rbac/permisos-por-rol-resources.yml}. Los tokens Firebase
 * suelen traer aliases ({@code ADMIN}) que {@link MapeadorRolesFirebase} convierte a
 * internos ({@code ADMINISTRADOR}) antes de consultar esta matriz.</p>
 *
 * <p>Cuando ms-identity exponga roles/permisos en BD + Redis, se puede sustituir esta
 * implementacion conservando el contrato de {@link #permisosPara(Collection)}.</p>
 */
@Component
public class ProveedorPermisos {

    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROL_AUTORIDAD = "AUTORIDAD";
    public static final String ROL_OPERADOR = "OPERADOR";
    public static final String ROL_PARTICULAR = "PARTICULAR";
    public static final String ROL_VOLUNTARIO = "VOLUNTARIO";

    public static final String CENTRO_CREAR = "CENTRO_CREAR";
    public static final String CENTRO_GESTIONAR = "CENTRO_GESTIONAR";
    public static final String INVENTARIO_GESTIONAR = "INVENTARIO_GESTIONAR";
    public static final String INVENTARIO_UMBRALES = "INVENTARIO_UMBRALES";
    public static final String CENTRO_ASIGNAR_OPERADOR = "CENTRO_ASIGNAR_OPERADOR";
    public static final String INVENTARIO_SUGERENCIAS = "INVENTARIO_SUGERENCIAS";

    private static final String UBICACION_RBAC = "classpath:rbac/permisos-por-rol-resources.yml";

    private final Map<String, Set<String>> permisosPorRol;

    public ProveedorPermisos(ResourceLoader resourceLoader) throws IOException {
        Objects.requireNonNull(resourceLoader, "resourceLoader");
        Resource recurso = resourceLoader.getResource(UBICACION_RBAC);
        if (!recurso.exists()) {
            throw new IllegalStateException("RBAC ausente o ilegible: " + UBICACION_RBAC);
        }
        try (InputStream entrada = recurso.getInputStream()) {
            this.permisosPorRol = Map.copyOf(leerYaml(entrada));
        }
    }

    /** Solo para tests que no montan el classpath del modulo completo (evitar usar en produccion). */
    ProveedorPermisos(Map<String, Set<String>> permisosPorRolOverrides) {
        this.permisosPorRol = Map.copyOf(permisosPorRolOverrides);
    }

    private static Map<String, Set<String>> leerYaml(InputStream entrada) {
        Yaml yaml = new Yaml();
        Map<String, Object> raiz = yaml.load(entrada);
        if (raiz == null || raiz.isEmpty()) {
            throw new IllegalStateException("El YAML de RBAC esta vacio");
        }
        Map<String, Set<String>> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, Object> par : raiz.entrySet()) {
            String codigoRol = par.getKey().trim();
            Object valor = par.getValue();
            Set<String> lista = new HashSet<>();
            if (valor instanceof Collection<?> cols) {
                for (Object item : cols) {
                    if (item != null && StringUtils.hasText(item.toString())) {
                        lista.add(item.toString().trim());
                    }
                }
            } else if (valor != null) {
                throw new IllegalStateException("Rol '" + codigoRol + "': lista de permisos invalida");
            }
            resultado.put(codigoRol, Set.copyOf(lista));
        }
        return resultado;
    }

    /**
     * @param rolesDesdeClaims roles internos o alias Firebase ({@link MapeadorRolesFirebase})
     */
    public Set<String> permisosPara(Collection<String> rolesCodigoInterno) {
        Set<String> permisos = new HashSet<>();
        if (rolesCodigoInterno == null) {
            return permisos;
        }
        Set<String> internos = MapeadorRolesFirebase.normalizar(rolesCodigoInterno);
        for (String rol : internos) {
            Set<String> delRol = permisosPorRol.get(rol);
            if (delRol != null && !delRol.isEmpty()) {
                permisos.addAll(delRol);
            }
        }
        return permisos;
    }
}

