package com.jack.springaiopenrouter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-key",
        "spring.ai.openai.base-url=https://openrouter.ai/api/v1"
})
class SpringAiOpenrouterDemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
