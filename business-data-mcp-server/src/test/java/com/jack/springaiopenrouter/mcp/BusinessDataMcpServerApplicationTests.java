package com.jack.springaiopenrouter.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:mcp-server-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.ai.mcp.server.protocol=STREAMABLE",
                "spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp",
                "app.seed.enabled=true"
        }
)
class BusinessDataMcpServerApplicationTests {

    @Test
    void contextLoads() {
    }
}
