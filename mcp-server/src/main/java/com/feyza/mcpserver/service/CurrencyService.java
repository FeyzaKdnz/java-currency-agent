package com.feyza.mcpserver.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class CurrencyService {

    public record ConversionResult(BigDecimal amount, String base, String date,
                                   Map<String, BigDecimal> rates) {}

    private final RestClient restClient;

    public CurrencyService(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://api.frankfurter.dev/v1").build();
    }

    public BigDecimal convert(BigDecimal amount, String from, String to) {
        String base = normalize(from);
        String target = normalize(to);
        try {
            ConversionResult result = restClient.get()
                    .uri("/latest?amount={amount}&base={base}&symbols={target}",
                            amount.toPlainString(), base, target)
                    .retrieve()
                    .body(ConversionResult.class);
            if (result == null || result.rates() == null || !result.rates().containsKey(target)) {
                throw new IllegalStateException("Réponse inattendue de l'API de taux de change");
            }
            return result.rates().get(target);
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("Devise inconnue ou requête invalide : " + base + " -> " + target, e);
        }
    }

    private static String normalize(String code) {
        if (code == null || !code.trim().matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("Code devise invalide (3 lettres attendues) : " + code);
        }
        return code.trim().toUpperCase();
    }
}
