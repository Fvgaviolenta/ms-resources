package cl.catastrofescl.resources.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Inicializa FirebaseApp y expone {@link FirebaseAuth}.
 * Activo solo si {@code catastrofescl.firebase.enabled=true}.
 *
 * <p>Credenciales: si {@code catastrofescl.firebase.credentials-path} apunta a un JSON de cuenta
 * de servicio, se usa ese archivo. Si viene vacio, se usa
 * {@linkplain GoogleCredentials#getApplicationDefault() Application Default Credentials}
 * (variable de entorno {@code GOOGLE_APPLICATION_CREDENTIALS} o entorno GCP).</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "catastrofescl.firebase", name = "enabled", havingValue = "true")
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp(
            @Value("${catastrofescl.firebase.credentials-path:}") String rutaCredenciales,
            @Value("${catastrofescl.firebase.project-id:}") String projectId) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        FirebaseOptions.Builder constructor = FirebaseOptions.builder();
        if (StringUtils.hasText(rutaCredenciales)) {
            try (InputStream flujo = Files.newInputStream(Paths.get(rutaCredenciales.trim()))) {
                constructor.setCredentials(GoogleCredentials.fromStream(flujo));
            }
        } else {
            constructor.setCredentials(GoogleCredentials.getApplicationDefault());
        }

        if (StringUtils.hasText(projectId)) {
            constructor.setProjectId(projectId.trim());
        }

        return FirebaseApp.initializeApp(constructor.build());
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}

