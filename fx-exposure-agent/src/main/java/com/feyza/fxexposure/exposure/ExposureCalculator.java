package com.feyza.fxexposure.exposure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Calcule le total et la part de chaque devise. Calcul déterministe en BigDecimal :
 * le modèle de langage ne fait jamais d'arithmétique.
 * Les pourcentages sont arrondis à 2 décimales (leur somme peut différer de 100 de quelques centièmes).
 */
@Component
public class ExposureCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public ExposureReport compute(String baseCurrency, List<ConvertedHolding> holdings) {
        if (holdings == null || holdings.isEmpty()) {
            throw new IllegalArgumentException("Aucune position fournie");
        }

        Map<String, ConvertedHolding> merged = new LinkedHashMap<>();
        for (ConvertedHolding h : holdings) {
            if (h.originalAmount().signum() < 0 || h.convertedAmount().signum() < 0) {
                throw new IllegalArgumentException("Les montants négatifs ne sont pas supportés : " + h.currency());
            }
            String currency = h.currency().trim().toUpperCase();
            merged.merge(currency,
                    new ConvertedHolding(currency, h.originalAmount(), h.convertedAmount()),
                    (a, b) -> new ConvertedHolding(currency,
                            a.originalAmount().add(b.originalAmount()),
                            a.convertedAmount().add(b.convertedAmount())));
        }

        BigDecimal total = merged.values().stream()
                .map(ConvertedHolding::convertedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.signum() <= 0) {
            throw new IllegalArgumentException("Le total converti doit être strictement positif");
        }

        List<ExposureLine> lines = merged.values().stream()
                .sorted(Comparator.comparing(ConvertedHolding::convertedAmount).reversed())
                .map(h -> new ExposureLine(
                        h.currency(),
                        h.originalAmount(),
                        h.convertedAmount(),
                        h.convertedAmount().multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP)))
                .toList();

        return new ExposureReport(baseCurrency.trim().toUpperCase(),
                total.setScale(2, RoundingMode.HALF_UP), lines);
    }
}
