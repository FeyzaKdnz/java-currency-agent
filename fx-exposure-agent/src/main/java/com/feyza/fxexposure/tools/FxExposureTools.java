package com.feyza.fxexposure.tools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.feyza.fxexposure.converter.CurrencyConverter;
import com.feyza.fxexposure.exposure.ConvertedHolding;
import com.feyza.fxexposure.exposure.ExposureCalculator;
import com.feyza.fxexposure.exposure.ExposureReport;

@Component
public class FxExposureTools {

    public record Holding(String currency, double amount) {}

    private final CurrencyConverter converter;
    private final ExposureCalculator calculator;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FxExposureTools.class);


    public FxExposureTools(CurrencyConverter converter, ExposureCalculator calculator) {
        this.converter = converter;
        this.calculator = calculator;
    }

    public record ToolResult(ExposureReport report, String error) {}

    @Tool(description = "Analyse l'exposition de change d'un ensemble de positions : convertit chaque position "
            + "dans la devise de référence, puis calcule le total et la part de chaque devise.")
    public ToolResult analyzeExposure(
            @ToolParam(description = "Devise de référence du rapport, code ISO sur 3 lettres, par exemple EUR") String baseCurrency,
            @ToolParam(description = "Liste des positions : code ISO de la devise et montant") List<Holding> holdings) {
        try {
            String base = normalize(baseCurrency);
            List<ConvertedHolding> converted = new ArrayList<>();
            for (Holding h : holdings) {
                String currency = normalize(h.currency());
                BigDecimal original = BigDecimal.valueOf(h.amount());
                BigDecimal inBase = currency.equals(base) ? original : converter.convert(original, currency, base);
                converted.add(new ConvertedHolding(currency, original, inBase));
            }
            return new ToolResult(calculator.compute(base, converted), null);
        } catch (RuntimeException e) {
            log.error("analyzeExposure en échec", e);
            return new ToolResult(null, "Analyse impossible : " + e.getMessage());
        }
    }

    private static String normalize(String code) {
        if (code == null || !code.trim().matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("Code devise invalide (3 lettres attendues) : " + code);
        }
        return code.trim().toUpperCase();
    }
}
