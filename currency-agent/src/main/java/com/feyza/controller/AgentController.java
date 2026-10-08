package com.feyza.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.feyza.tools.CurrencyTools;

@RestController
public class AgentController {

    private static final String SYSTEM_PROMPT = """
        Tu es un assistant spécialisé dans la conversion de devises.
        Pour toute conversion, appelle TOUJOURS l'outil convertCurrency.
        Dans ta réponse, reprends EXACTEMENT le montant du champ convertedAmount renvoyé par l'outil.
        Ne recalcule jamais et n'utilise jamais un taux issu de ta mémoire.
        Si la question ne concerne pas les devises, explique poliment que tu ne peux aider que sur ce sujet.
        Réponds en français, de façon concise.
        """;

    private final ChatClient chatClient;

    public AgentController(ChatClient.Builder builder, CurrencyTools currencyTools) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(currencyTools)
                .build();
    }

    @GetMapping("/agent")
    public String agent(@RequestParam String question) {
        return chatClient.prompt().user(question).call().content();
    }
}
