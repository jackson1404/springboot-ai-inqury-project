package com.jack.springaiopenrouter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-key",
        "spring.ai.openai.base-url=https://openrouter.ai/api/v1",
        "spring.ai.mcp.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:app-test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.ai.chat.memory.repository.jdbc.initialize-schema=always",
        "app.seed.enabled=false"
})
class SpringAiOpenrouterDemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
