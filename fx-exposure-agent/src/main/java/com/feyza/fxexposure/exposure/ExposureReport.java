package com.feyza.fxexposure.exposure;

import java.math.BigDecimal;
import java.util.List;

public record ExposureReport(String baseCurrency, BigDecimal total, List<ExposureLine> lines) {}
