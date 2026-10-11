package com.feyza.fxexposure.exposure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class ExposureCalculatorTest {

    private final ExposureCalculator calculator = new ExposureCalculator();

    @Test
    void computesTotalAndPercentages() {
        ExposureReport report = calculator.compute("eur", List.of(
                new ConvertedHolding("EUR", new BigDecimal("100"), new BigDecimal("100")),
                new ConvertedHolding("USD", new BigDecimal("100"), new BigDecimal("90"))));

        assertThat(report.baseCurrency()).isEqualTo("EUR");
        assertThat(report.total()).isEqualByComparingTo("190.00");
        assertThat(report.lines()).hasSize(2);
        assertThat(report.lines().get(0).currency()).isEqualTo("EUR");
        assertThat(report.lines().get(0).percentage()).isEqualByComparingTo("52.63");
        assertThat(report.lines().get(1).currency()).isEqualTo("USD");
        assertThat(report.lines().get(1).percentage()).isEqualByComparingTo("47.37");
    }

    @Test
    void mergesPositionsInTheSameCurrency() {
        ExposureReport report = calculator.compute("EUR", List.of(
                new ConvertedHolding("usd", new BigDecimal("50"), new BigDecimal("45")),
                new ConvertedHolding("USD", new BigDecimal("50"), new BigDecimal("45"))));

        assertThat(report.lines()).hasSize(1);
        assertThat(report.lines().get(0).originalAmount()).isEqualByComparingTo("100");
        assertThat(report.lines().get(0).convertedAmount()).isEqualByComparingTo("90");
        assertThat(report.lines().get(0).percentage()).isEqualByComparingTo("100.00");
    }

    @Test
    void rejectsEmptyHoldings() {
        assertThatThrownBy(() -> calculator.compute("EUR", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThatThrownBy(() -> calculator.compute("EUR", List.of(
                new ConvertedHolding("USD", new BigDecimal("-10"), new BigDecimal("-9")))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
