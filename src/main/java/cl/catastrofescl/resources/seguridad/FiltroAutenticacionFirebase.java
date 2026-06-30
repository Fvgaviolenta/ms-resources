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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Filtro de autenticacion para PROD / laboratorio con Firebase.
 */
@Slf4j
public class FiltroAutenticacionFirebase extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final FirebaseAuth firebaseAuth;
    private final ProveedorPermisos proveedorPermisos;
    private final String rolesPorDefectoSiSinClaims;
    private final boolean confiarHeadersGateway;

    public FiltroAutenticacionFirebase(FirebaseAuth firebaseAuth, ProveedorPermisos proveedorPermisos) {
        this(firebaseAuth, proveedorPermisos, "", false);
    }

    public FiltroAutenticacionFirebase(FirebaseAuth firebaseAuth,
                                       ProveedorPermisos proveedorPermisos,
                                       String rolesPorDefectoSiSinClaims,
                                       boolean confiarHeadersGateway) {
        this.firebaseAuth = firebaseAuth;
        this.proveedorPermisos = proveedorPermisos;
        this.rolesPorDefectoSiSinClaims = rolesPorDefectoSiSinClaims != null
                ? rolesPorDefectoSiSinClaims.trim()
                : "";
        this.confiarHeadersGateway = confiarHeadersGateway;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticarDesdeBearer(request);
            if (SecurityContextHolder.getContext().getAuthentication() == null && confiarHeadersGateway) {
                autenticarDesdeGateway(request);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void autenticarDesdeBearer(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER)) {
            return;
        }

        String idToken = header.substring(BEARER.length()).trim();
        try {
            FirebaseToken token = firebaseAuth.verifyIdToken(idToken);
            Set<String> rolesDeclarados = resolverRoles(token, request);
            establecerAutenticacion(token.getUid(), rolesDeclarados);
            log.debug("Autenticacion Firebase aplicada uid={} roles={}", token.getUid(), rolesDeclarados);
        } catch (FirebaseAuthException ex) {
            log.warn("Token Firebase invalido: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    private void autenticarDesdeGateway(HttpServletRequest request) {
        String uid = request.getHeader(FiltroAutenticacionDev.HEADER_GATEWAY_FIREBASE_UID);
        if (!StringUtils.hasText(uid)) {
            return;
        }

        Set<String> rolesDeclarados = new HashSet<>();
        rolesDeclarados.addAll(LectorRolesDeclarados.desdeListaSeparadaPorComa(
                request.getHeader(FiltroAutenticacionDev.HEADER_ROLES)));
        if (rolesDeclarados.isEmpty() && StringUtils.hasText(rolesPorDefectoSiSinClaims)) {
            rolesDeclarados.addAll(LectorRolesDeclarados.desdeListaSeparadaPorComa(rolesPorDefectoSiSinClaims));
            log.warn("Autenticacion por gateway sin roles; usando roles por defecto uid={} roles={}",
                    uid, rolesDeclarados);
        }
        if (rolesDeclarados.isEmpty()) {
            return;
        }

        establecerAutenticacion(uid.trim(), rolesDeclarados);
    }

    private Set<String> resolverRoles(FirebaseToken token, HttpServletRequest request) {
        Set<String> rolesDeclarados = new HashSet<>(LectorRolesDeclarados.desdeClaimsFirebase(token.getClaims()));
        if (rolesDeclarados.isEmpty()) {
            rolesDeclarados.addAll(LectorRolesDeclarados.desdeListaSeparadaPorComa(
                    request.getHeader(FiltroAutenticacionDev.HEADER_ROLES)));
        }
        if (rolesDeclarados.isEmpty() && StringUtils.hasText(rolesPorDefectoSiSinClaims)) {
            rolesDeclarados.addAll(LectorRolesDeclarados.desdeListaSeparadaPorComa(rolesPorDefectoSiSinClaims));
            log.warn("Token Firebase sin roles; usando roles por defecto de laboratorio uid={} roles={}",
                    token.getUid(), rolesDeclarados);
        }
        return rolesDeclarados;
    }

    private void establecerAutenticacion(String uid, Set<String> rolesDeclarados) {
        Set<String> rolesInternos = MapeadorRolesFirebase.normalizar(rolesDeclarados);
        Set<String> permisos = proveedorPermisos.permisosPara(rolesDeclarados);
        UUID usuarioId = UUID.nameUUIDFromBytes(("firebase:" + uid).getBytes());

        UsuarioAutenticado principal = new UsuarioAutenticado(uid, usuarioId, rolesInternos, permisos);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        permisos.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        rolesInternos.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
