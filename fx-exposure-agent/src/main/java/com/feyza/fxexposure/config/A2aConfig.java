package com.feyza.fxexposure.config;

import java.util.List;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import org.springaicommunity.a2a.server.executor.DefaultAgentExecutor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.feyza.fxexposure.tools.FxExposureTools;


@Configuration
public class A2aConfig {

    private static final String SYSTEM_PROMPT = """
        Tu es un assistant d'analyse d'exposition de change.
        L'utilisateur décrit des positions en plusieurs devises. Extrais la liste (code ISO de la devise, montant)
        et la devise de référence (EUR si elle n'est pas précisée), puis appelle UNE SEULE FOIS l'outil analyzeExposure.
        Présente ensuite le rapport en reprenant EXACTEMENT les chiffres renvoyés : total, montant converti et
        pourcentage de chaque devise. Ne recalcule jamais, n'arrondis pas toi-même, n'utilise aucun taux de ta mémoire.
        Termine par une phrase indiquant que les taux sont des taux de référence de la BCE, à titre informatif,
        et que ce n'est pas un conseil en investissement.
        Si l'outil renvoie un champ error, explique simplement à l'utilisateur que l'analyse n'a pas pu être faite
        et pourquoi, sans inventer aucun chiffre.
        Si la demande ne concerne pas l'exposition de change, explique poliment que tu ne peux pas aider.
        Réponds en français, de façon claire et concise.
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
                        .id("analyze_fx_exposure")
                        .name("Analyse d'exposition de change")
                        .description("Calcule le total et la répartition par devise de positions multi-devises")
                        .tags(List.of("fx", "exposure", "treasury"))
                        .build()))
                .protocolVersion("0.3.0")
                .build();
    }

    @Bean
    public AgentExecutor agentExecutor(ChatClient.Builder builder, FxExposureTools tools) {
        ChatClient chatClient = builder.clone()
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(tools)
                .build();

        return new DefaultAgentExecutor(chatClient, (chat, ctx) -> {
            String userMessage = DefaultAgentExecutor.extractTextFromMessage(ctx.getMessage());
            return chat.prompt().user(userMessage).call().content();
        });
    }
}
