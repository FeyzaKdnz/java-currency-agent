package com.feyza.a2aclient;

import io.a2a.A2A;
import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.http.A2ACardResolver;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import io.a2a.spec.TextPart;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class A2aTestClient {

    public static void main(String[] args) throws Exception {
        String baseUrl = System.getenv().getOrDefault("A2A_AGENT_URL", "http://localhost:10000/a2a");

        String root = System.getenv().getOrDefault("A2A_AGENT_ROOT", "http://localhost:10000");
        String cardPath = System.getenv().getOrDefault("A2A_CARD_PATH", "a2a/.well-known/agent-card.json");
        AgentCard card = A2A.getAgentCard(root, cardPath, null);

        System.out.println("Carte d'agent reçue : " + card);

        if (args.length == 0) {
            System.out.println("Aucune question fournie : seule la carte a été lue.");
            System.exit(0);
        }

        CompletableFuture<String> answer = new CompletableFuture<>();

        List<BiConsumer<ClientEvent, AgentCard>> consumers = List.of((event, agentCard) -> {
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

        Client client = Client.builder(card)
                .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                .addConsumers(consumers)
                .build();

        String question = String.join(" ", args);
        System.out.println("Question : " + question);
        client.sendMessage(A2A.toUserMessage(question));

        System.out.println("Réponse : " + answer.get(90, TimeUnit.SECONDS));
        System.exit(0);
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
