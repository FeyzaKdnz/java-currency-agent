package com.feyza.fxexposure.exposure;

import java.math.BigDecimal;

public record ExposureLine(String currency, BigDecimal originalAmount,
                           BigDecimal convertedAmount, BigDecimal percentage) {}
