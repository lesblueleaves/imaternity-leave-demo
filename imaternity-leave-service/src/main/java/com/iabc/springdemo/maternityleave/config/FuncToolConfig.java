package com.iabc.springdemo.maternityleave.config;

import com.iabc.springdemo.maternityleave.functools.LeaveDateTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FuncToolConfig {

    @Bean
    public LeaveDateTool leaveDateTool() {
        return new LeaveDateTool();
    }
}
