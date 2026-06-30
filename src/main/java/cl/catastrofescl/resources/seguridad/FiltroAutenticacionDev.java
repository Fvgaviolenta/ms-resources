package cl.catastrofescl.resources.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filtro de autenticacion para ENTORNO DE DESARROLLO / laboratorio.
 */
@Slf4j
public class FiltroAutenticacionDev extends OncePerRequestFilter {

    public static final String HEADER_UID = "X-Dev-Firebase-Uid";
    public static final String HEADER_ROLES = "X-Dev-Roles";
    public static final String HEADER_USUARIO_ID = "X-Dev-Usuario-Id";
    public static final String HEADER_GATEWAY_FIREBASE_UID = "X-Firebase-Uid";

    private final ProveedorPermisos proveedorPermisos;
    private final boolean trustGatewayFirebaseHeaders;
    private final String devDefaultRoleForGateway;

    public FiltroAutenticacionDev(ProveedorPermisos proveedorPermisos) {
        this(proveedorPermisos, false, ProveedorPermisos.ROL_AUTORIDAD);
    }

    public FiltroAutenticacionDev(ProveedorPermisos proveedorPermisos,
                                  boolean trustGatewayFirebaseHeaders,
                                  String devDefaultRoleForGateway) {
        this.proveedorPermisos = proveedorPermisos;
        this.trustGatewayFirebaseHeaders = trustGatewayFirebaseHeaders;
        this.devDefaultRoleForGateway = devDefaultRoleForGateway != null && !devDefaultRoleForGateway.isBlank()
                ? devDefaultRoleForGateway.trim()
                : ProveedorPermisos.ROL_AUTORIDAD;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uid = request.getHeader(HEADER_UID);
        if (!StringUtils.hasText(uid) && trustGatewayFirebaseHeaders) {
            uid = request.getHeader(HEADER_GATEWAY_FIREBASE_UID);
        }
        if (!StringUtils.hasText(uid)) {
            filterChain.doFilter(request, response);
            return;
        }

        Set<String> rolesDeclarados = leerRoles(request.getHeader(HEADER_ROLES));
        if (rolesDeclarados.isEmpty() && trustGatewayFirebaseHeaders
                && StringUtils.hasText(request.getHeader(HEADER_GATEWAY_FIREBASE_UID))
                && !StringUtils.hasText(request.getHeader(HEADER_UID))) {
            rolesDeclarados = leerRoles(devDefaultRoleForGateway);
        }
        Set<String> rolesInternos = MapeadorRolesFirebase.normalizar(rolesDeclarados);
        Set<String> permisos = proveedorPermisos.permisosPara(rolesDeclarados);
        UUID usuarioId = resolverUsuarioId(request.getHeader(HEADER_USUARIO_ID), uid);

        UsuarioAutenticado principal = new UsuarioAutenticado(uid, usuarioId, rolesInternos, permisos);

        List<SimpleGrantedAuthority> authorities = permisos.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toCollection(java.util.ArrayList::new));
        rolesInternos.forEach(rol -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rol)));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        log.debug("Autenticacion DEV aplicada uid={} usuarioId={} roles={}", uid, usuarioId, rolesInternos);

        filterChain.doFilter(request, response);
    }

    private Set<String> leerRoles(String header) {
        if (!StringUtils.hasText(header)) {
            return Set.of();
        }
        return Arrays.stream(header.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private UUID resolverUsuarioId(String headerExplicito, String uidFallback) {
        if (StringUtils.hasText(headerExplicito)) {
            try {
                return UUID.fromString(headerExplicito.trim());
            } catch (IllegalArgumentException ex) {
                log.warn("X-Dev-Usuario-Id invalido, se ignorara: {}", headerExplicito);
            }
        }
        return UUID.nameUUIDFromBytes(("dev:" + uidFallback).getBytes());
    }
}
