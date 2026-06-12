package cl.catastrofescl.resources.seguridad;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Expone el usuario autenticado extrayendolo del SecurityContext de Spring Security.
 * Lanza IllegalStateException si no hay usuario (los endpoints que la usan deben estar protegidos).
 */
@Component
public class ContextoUsuario {

    public UsuarioAutenticado actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UsuarioAutenticado principal)) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto");
        }
        return principal;
    }

    public UUID usuarioIdActual() {
        return actual().usuarioId();
    }
}

