package com.feyza.mcpserver;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.feyza.mcpserver.tools.CurrencyTools;

@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    ToolCallbackProvider currencyToolProvider(CurrencyTools currencyTools) {
        return MethodToolCallbackProvider.builder().toolObjects(currencyTools).build();
    }
}
