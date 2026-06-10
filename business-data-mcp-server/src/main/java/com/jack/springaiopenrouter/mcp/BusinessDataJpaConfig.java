package com.jack.springaiopenrouter.mcp;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.jack.springaiopenrouter.entity")
@EnableJpaRepositories(basePackages = "com.jack.springaiopenrouter.repository")
public class BusinessDataJpaConfig {
}
