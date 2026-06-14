package com.jack.springaiopenrouter.config;

import org.springaicommunity.mcp.security.client.McpClientOAuth2Configurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class McpClientSecurityConfig {

    @Bean
    SecurityFilterChain mcpClientOAuthSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Keep your existing app JWT security in your main SecurityConfig.
                // This config enables MCP OAuth2 client behavior.
                .with(McpClientOAuth2Configurer.mcpClientOAuth2(), Customizer.withDefaults())
                .build();
    }
}