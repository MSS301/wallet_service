package com.wallet_svc.wallet.configuration;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "Wallet Service API",
                        version = "1.0",
                        description = "Wallet Service API for managing user wallets, transactions, and credit packages",
                        contact = @Contact(name = "Wallet Service Team", email = "wallet@example.com")),
        servers = {
            @Server(description = "API Gateway", url = "http://localhost:8080/wallet-service"),
            @Server(description = "Local Server", url = "http://localhost:8085/wallet")
        },
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Bearer Token Authentication",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {}
