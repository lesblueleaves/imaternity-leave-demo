package com.iabc.springdemo.maternityleave.config;

import ai.z.openapi.ZhipuAiClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZhiPuConfig {

    @Bean
    public ZhipuAiClient zhipuAiClient(OpenAiChatModel chatModel) {
        ZhipuAiClient client = ZhipuAiClient.builder()
                .apiKey("495d20c9a893486299b7203ca958c0f6.RSjfVZ178fTg7orj")
                .build();
        return client;
    }

}
