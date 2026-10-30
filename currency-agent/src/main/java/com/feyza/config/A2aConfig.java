package com.feyza.config;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import org.springaicommunity.a2a.server.executor.DefaultAgentExecutor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class A2aConfig {

    private static final String SYSTEM_PROMPT = """
        Tu es un assistant spécialisé dans la conversion de devises.
        Pour toute conversion, appelle TOUJOURS l'outil de conversion de devises disponible.
        Dans ta réponse, reprends EXACTEMENT le montant du champ convertedAmount renvoyé par l'outil.
        Ne recalcule jamais et n'utilise jamais un taux issu de ta mémoire.
        Si la question ne concerne pas les devises, explique poliment que tu ne peux aider que sur ce sujet.
        Réponds en français, de façon concise.
        """;

    @Bean
    public AgentCard agentCard(@Value("${A2A_PUBLIC_URL:http://localhost:10000/a2a/}") String publicUrl) {
        return new AgentCard.Builder()
                .name("Currency Agent")
                .description("Convertit des montants entre devises avec les taux de référence de la BCE")
                .url(publicUrl)
                .version("1.0.0")
                .capabilities(new AgentCapabilities.Builder().streaming(false).build())
                .defaultInputModes(List.of("text"))
                .defaultOutputModes(List.of("text"))
                .skills(List.of(new AgentSkill.Builder()
                        .id("convert_currency")
                        .name("Conversion de devises")
                        .description("Convertit un montant d'une devise vers une autre")
                        .tags(List.of("currency", "exchange-rate"))
                        .build()))
                .protocolVersion("0.3.0")
                .build();
    }

    @Bean
    public AgentExecutor agentExecutor(ChatClient.Builder builder, ToolCallbackProvider mcpTools) {
        ChatClient chatClient = builder.clone()
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(mcpTools)
                .build();

        return new DefaultAgentExecutor(chatClient, (chat, ctx) -> {
            String userMessage = DefaultAgentExecutor.extractTextFromMessage(ctx.getMessage());
            return chat.prompt().user(userMessage).call().content();
        });
    }
}
