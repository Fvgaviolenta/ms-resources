package cl.catastrofescl.resources.config;

import cl.catastrofescl.resources.seguridad.FiltroAutenticacionDev;
import cl.catastrofescl.resources.seguridad.FiltroAutenticacionFirebase;
import cl.catastrofescl.resources.seguridad.ProveedorPermisos;
import com.google.firebase.auth.FirebaseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableMethodSecurity
public class SeguridadConfig {

    @Value("${catastrofescl.auth.dev-mode:false}")
    private boolean devMode;

    @Value("${catastrofescl.auth.dev-trust-gateway-firebase-headers:false}")
    private boolean devTrustGatewayFirebaseHeaders;

    @Value("${catastrofescl.auth.dev-default-role-for-gateway:}")
    private String devDefaultRoleForGateway;

    @Value("${catastrofescl.firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ProveedorPermisos proveedorPermisos,
                                                   ObjectProvider<FirebaseAuth> firebaseAuthProvider)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/centros").permitAll()
                        .requestMatchers(HttpMethod.GET, "/centros/cercanos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/centros/map-data").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/catalogo/items").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                );

        if (firebaseEnabled) {
            FirebaseAuth firebaseAuth = firebaseAuthProvider.getIfAvailable();
            if (firebaseAuth == null) {
                throw new IllegalStateException(
                        "catastrofescl.firebase.enabled=true pero no se pudo inicializar FirebaseAuth");
            }
            log.info("Seguridad ms-resources con FiltroAutenticacionFirebase");
            http.addFilterBefore(
                    new FiltroAutenticacionFirebase(
                            firebaseAuth,
                            proveedorPermisos,
                            devDefaultRoleForGateway,
                            devTrustGatewayFirebaseHeaders),
                    UsernamePasswordAuthenticationFilter.class);
        } else if (devMode) {
            log.warn("Seguridad ms-resources en MODO DEV (X-Dev-* / gateway). NO usar en produccion.");
            http.addFilterBefore(
                    new FiltroAutenticacionDev(proveedorPermisos, devTrustGatewayFirebaseHeaders, devDefaultRoleForGateway),
                    UsernamePasswordAuthenticationFilter.class);
        } else {
            log.warn("Seguridad ms-resources sin filtro configurado. Endpoints protegidos responderan 401.");
        }

        return http.build();
    }
}
