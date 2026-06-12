package cl.catastrofescl.resources.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Firebase ID Token (perfil prod)");

        SecurityScheme devUid = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Dev-Firebase-Uid")
                .description("Perfil dev: UID simulado");

        SecurityScheme devRoles = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Dev-Roles")
                .description("Perfil dev: roles separados por coma (ADMINISTRADOR,AUTORIDAD,...)");

        return new OpenAPI()
                .info(new Info()
                        .title("ms-resources API")
                        .description("Microservicio de Operaciones de Recursos - CatastrofesCL")
                        .version("1.0.0")
                        .contact(new Contact().name("Equipo Desastres").email("equipo@catastrofescl.cl"))
                        .license(new License().name("Proyecto academico DUOC UC")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearer)
                        .addSecuritySchemes("X-Dev-Firebase-Uid", devUid)
                        .addSecuritySchemes("X-Dev-Roles", devRoles))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("X-Dev-Firebase-Uid")
                        .addList("X-Dev-Roles"));
    }
}

