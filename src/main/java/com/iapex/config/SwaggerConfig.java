package com.iapex.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "API-REST IAPEX",
                description = "",
                termsOfService = "terminos_y_condiciones",
                version = "1.0.0",
                contact = @Contact(
                        name = "Misrael Florentino",
                        url = "",
                        email = "misraelaltamirano@mail.com"
                ),
                license = @License(
                        name = "Standard Software Use License for Misrael"                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080"
                )
        },
        security = @SecurityRequirement(
                name = "Security IAPEX"
        )
)


public class SwaggerConfig {
}