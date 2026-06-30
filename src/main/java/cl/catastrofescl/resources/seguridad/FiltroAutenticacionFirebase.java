package cl.catastrofescl.resources.seguridad;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filtro de autenticacion para PROD con fallback de roles de laboratorio.
 */
@Slf4j
public class FiltroAutenticacionFirebase extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final FirebaseAuth firebaseAuth;
    private final ProveedorPermisos proveedorPermisos;
    private final String rolesPorDefectoSiSinClaims;

    public FiltroAutenticacionFirebase(FirebaseAuth firebaseAuth, ProveedorPermisos proveedorPermisos) {
        this(firebaseAuth, proveedorPermisos, "");
    }

    public FiltroAutenticacionFirebase(FirebaseAuth firebaseAuth,
                                       ProveedorPermisos proveedorPermisos,
                                       String rolesPorDefectoSiSinClaims) {
        this.firebaseAuth = firebaseAuth;
        this.proveedorPermisos = proveedorPermisos;
        this.rolesPorDefectoSiSinClaims = rolesPorDefectoSiSinClaims != null
                ? rolesPorDefectoSiSinClaims.trim()
                : "";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = header.substring(BEARER.length()).trim();
        try {
            FirebaseToken token = firebaseAuth.verifyIdToken(idToken);

            Set<String> rolesDeclarados = extraerRoles(token);
            if (rolesDeclarados.isEmpty() && StringUtils.hasText(rolesPorDefectoSiSinClaims)) {
                rolesDeclarados = leerRolesDesdeLista(rolesPorDefectoSiSinClaims);
                log.warn("Token Firebase sin roles; usando roles por defecto de laboratorio uid={} roles={}",
                        token.getUid(), rolesDeclarados);
            }
            Set<String> rolesInternos = MapeadorRolesFirebase.normalizar(rolesDeclarados);
            Set<String> permisos = proveedorPermisos.permisosPara(rolesDeclarados);
            UUID usuarioId = UUID.nameUUIDFromBytes(("firebase:" + token.getUid()).getBytes());

            UsuarioAutenticado principal =
                    new UsuarioAutenticado(token.getUid(), usuarioId, rolesInternos, permisos);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            permisos.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
            rolesInternos.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("Autenticacion Firebase aplicada uid={} roles={}", token.getUid(), rolesInternos);
        } catch (FirebaseAuthException ex) {
            log.warn("Token Firebase invalido: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Set<String> extraerRoles(FirebaseToken token) {
        Object claim = token.getClaims().get("roles");
        if (claim instanceof List<?> lista) {
            Set<String> roles = new HashSet<>();
            for (Object r : lista) {
                if (r != null) {
                    roles.add(r.toString());
                }
            }
            return roles;
        }
        Object legacy = token.getClaims().get("role");
        if (legacy instanceof String rol && StringUtils.hasText(rol)) {
            return Set.of(rol);
        }
        return Set.of();
    }

    private Set<String> leerRolesDesdeLista(String listaSeparadaPorComa) {
        return Arrays.stream(listaSeparadaPorComa.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
