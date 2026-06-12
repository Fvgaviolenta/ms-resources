package cl.catastrofescl.resources.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
 * Filtro de autenticacion para ENTORNO DE DESARROLLO.
 *
 * <p>Reemplaza a Firebase cuando todavia no se integra. Lee dos headers:</p>
 * <ul>
 *   <li><b>X-Dev-Firebase-Uid</b>: UID simulado del usuario (obligatorio).</li>
 *   <li><b>X-Dev-Roles</b>: roles separados por coma; acepta internos o aliases Firebase ({@code ADMINISTRADOR} o {@code ADMIN}).</li>
 *   <li><b>X-Dev-Usuario-Id</b>: UUID interno opcional. Si no se envia se genera uno estable a partir del UID.</li>
 * </ul>
 *
 * <p>Si los headers no estan presentes, la peticion sigue sin autenticacion
 * y los endpoints protegidos responderan 401/403.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class FiltroAutenticacionDev extends OncePerRequestFilter {

    public static final String HEADER_UID = "X-Dev-Firebase-Uid";
    public static final String HEADER_ROLES = "X-Dev-Roles";
    public static final String HEADER_USUARIO_ID = "X-Dev-Usuario-Id";

    private final ProveedorPermisos proveedorPermisos;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uid = request.getHeader(HEADER_UID);
        if (!StringUtils.hasText(uid)) {
            filterChain.doFilter(request, response);
            return;
        }

        Set<String> declarados = leerRoles(request.getHeader(HEADER_ROLES));
        Set<String> rolesInternos = MapeadorRolesFirebase.normalizar(declarados);
        Set<String> permisos = proveedorPermisos.permisosPara(declarados);
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
        // UUID determinista en base al UID para que repetir el UID siempre apunte al mismo usuarioId
        return UUID.nameUUIDFromBytes(("dev:" + uidFallback).getBytes());
    }
}

