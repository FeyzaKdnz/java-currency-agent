package com.feyza.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.feyza.service.CurrencyService;

class CurrencyToolsTest {

    @Test
    void returnsStructuredConversionResult() {
        CurrencyService service = mock(CurrencyService.class);
        when(service.convert(any(BigDecimal.class), any(), any())).thenReturn(new BigDecimal("86.5"));

        CurrencyTools.ConversionOutput output = new CurrencyTools(service).convertCurrency(100, "usd", "eur");

        assertThat(output.amount()).isEqualTo(100.0);
        assertThat(output.from()).isEqualTo("USD");
        assertThat(output.to()).isEqualTo("EUR");
        assertThat(output.convertedAmount()).isEqualByComparingTo("86.5");
    }
}