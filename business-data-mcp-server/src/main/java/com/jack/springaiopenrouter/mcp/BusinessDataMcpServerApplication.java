package com.jack.springaiopenrouter.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.jack.springaiopenrouter")
public class BusinessDataMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessDataMcpServerApplication.class, args);
    }
}
