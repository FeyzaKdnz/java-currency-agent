package com.feyza.fxexposure.exposure;

import java.math.BigDecimal;

/** Une position dans sa devise d'origine, avec sa valeur convertie dans la devise de référence. */
public record ConvertedHolding(String currency, BigDecimal originalAmount, BigDecimal convertedAmount) {}
