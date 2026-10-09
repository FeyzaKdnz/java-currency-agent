package com.feyza.mcpserver.tools;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.feyza.mcpserver.service.CurrencyService;

@Component
public class CurrencyTools {

    public record ConversionOutput(double amount, String from, String to, BigDecimal convertedAmount) {}

    private static final Logger log = LoggerFactory.getLogger(CurrencyTools.class);

    private final CurrencyService currencyService;

    public CurrencyTools(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Tool(description = "Convertit un montant d'une devise vers une autre avec le dernier taux de change "
            + "officiel de la Banque centrale européenne (mis à jour chaque jour ouvré).")
    public ConversionOutput convertCurrency(
            @ToolParam(description = "Le montant à convertir, par exemple 100") double amount,
            @ToolParam(description = "Code ISO de la devise source sur 3 lettres, par exemple USD") String from,
            @ToolParam(description = "Code ISO de la devise cible sur 3 lettres, par exemple EUR") String to) {
        log.info("TOOL APPELÉ : {} {} -> {}", amount, from, to);
        try {
            BigDecimal result = currencyService.convert(BigDecimal.valueOf(amount), from, to);
            log.info("TOOL RÉSULTAT : {}", result);
            return new ConversionOutput(amount, from.toUpperCase(), to.toUpperCase(), result);
        } catch (RuntimeException e) {
            log.error("TOOL ERREUR", e);
            throw e;
        }
    }
}