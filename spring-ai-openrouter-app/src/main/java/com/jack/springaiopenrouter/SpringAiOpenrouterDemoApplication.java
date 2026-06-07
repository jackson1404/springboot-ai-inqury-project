package com.jack.springaiopenrouter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringAiOpenrouterDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiOpenrouterDemoApplication.class, args);
    }
}
