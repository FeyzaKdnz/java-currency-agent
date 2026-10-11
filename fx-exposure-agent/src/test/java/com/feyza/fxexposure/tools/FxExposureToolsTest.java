package com.feyza.fxexposure.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.feyza.fxexposure.converter.CurrencyConverter;
import com.feyza.fxexposure.exposure.ExposureCalculator;
import com.feyza.fxexposure.exposure.ExposureReport;

class FxExposureToolsTest {

    @Test
    void convertsForeignHoldingsAndSkipsBaseCurrency() {
        CurrencyConverter converter = mock(CurrencyConverter.class);
        when(converter.convert(any(BigDecimal.class), org.mockito.ArgumentMatchers.eq("USD"),
                org.mockito.ArgumentMatchers.eq("EUR"))).thenReturn(new BigDecimal("90"));
        FxExposureTools tools = new FxExposureTools(converter, new ExposureCalculator());

        ExposureReport report = tools.analyzeExposure("eur", List.of(
                new FxExposureTools.Holding("EUR", 100),
                new FxExposureTools.Holding("usd", 100))).report();

        assertThat(report.total()).isEqualByComparingTo("190.00");
        assertThat(report.lines()).hasSize(2);
        verify(converter, never()).convert(any(), org.mockito.ArgumentMatchers.eq("EUR"), any());
    }

    @Test
    void returnsErrorInsteadOfThrowingWhenConversionFails() {
        CurrencyConverter converter = mock(CurrencyConverter.class);
        when(converter.convert(any(BigDecimal.class), any(), any()))
                .thenThrow(new IllegalStateException("currency-agent indisponible"));
        FxExposureTools tools = new FxExposureTools(converter, new ExposureCalculator());

        FxExposureTools.ToolResult result = tools.analyzeExposure("EUR",
                List.of(new FxExposureTools.Holding("USD", 100)));

        assertThat(result.report()).isNull();
        assertThat(result.error()).contains("currency-agent indisponible");
    }
}
