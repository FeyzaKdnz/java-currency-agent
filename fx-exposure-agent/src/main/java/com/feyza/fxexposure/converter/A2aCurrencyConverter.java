package com.feyza.fxexposure.converter;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import io.a2a.A2A;
import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import io.a2a.spec.TextPart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class A2aCurrencyConverter implements CurrencyConverter {

    private final String root;
    private final String cardPath;
    private Client client;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(A2aCurrencyConverter.class);

    public A2aCurrencyConverter(@Value("${currency-agent.url}") String root,
                                @Value("${currency-agent.card-path}") String cardPath) {
        this.root = root;
        this.cardPath = cardPath;
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        String question = ("Convertis %s %s en %s. Réponds uniquement par le nombre converti, "
                + "avec un point comme séparateur décimal, sans symbole, sans espace et sans texte.")
                .formatted(amount.toPlainString(), from, to);
        return AmountParser.parse(ask(question));
    }

    private String ask(String question) {
        CompletableFuture<String> answer = new CompletableFuture<>();
        List<BiConsumer<ClientEvent, AgentCard>> consumers = List.of((event, card) -> {
            if (event instanceof MessageEvent me) {
                answer.complete(textOf(me.getMessage().getParts()));
            } else if (event instanceof TaskEvent te) {
                StringBuilder sb = new StringBuilder();
                if (te.getTask().getArtifacts() != null) {
                    te.getTask().getArtifacts().forEach(a -> sb.append(textOf(a.parts())));
                }
                answer.complete(sb.length() > 0 ? sb.toString() : te.getTask().toString());
            }
        });
        try {
            client().sendMessage(A2A.toUserMessage(question), consumers, answer::completeExceptionally);
            return answer.get(90, TimeUnit.SECONDS);
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            log.error("Appel A2A vers currency-agent en échec", e);
            throw new IllegalStateException("Échec de l'appel A2A à currency-agent : "
                    + root.getClass().getSimpleName() + " - " + root.getMessage(), e);
        }
    }

    private synchronized Client client() throws Exception {
        if (client == null) {
            AgentCard card = A2A.getAgentCard(root, cardPath, null);
            client = Client.builder(card)
                    .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                    .addConsumers(List.of())
                    .build();
        }
        return client;
    }

    private static String textOf(List<?> parts) {
        StringBuilder sb = new StringBuilder();
        for (Object part : parts) {
            if (part instanceof TextPart tp) {
                sb.append(tp.getText());
            }
        }
        return sb.toString();
    }
}
