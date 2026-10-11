package com.feyza.fxexposure.converter;

import java.math.BigDecimal;

/** Convertit un montant d'une devise vers une autre. Implémenté par un appel A2A à currency-agent. */
public interface CurrencyConverter {
    BigDecimal convert(BigDecimal amount, String from, String to);
}
