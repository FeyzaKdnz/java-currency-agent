package com.feyza.fxexposure.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class AmountParserTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "223.49|223.49",
            "223,49 euros|223.49",
            "Environ 223.49 EUR|223.49",
            "1 234,56|1234.56",
            "1,234.56|1234.56",
            "1.234,56|1234.56",
            "5000000|5000000"
    })
    void parsesCommonFormats(String text, String expected) {
        assertThat(AmountParser.parse(text)).isEqualByComparingTo(expected);
    }

    @Test
    void failsWhenNoNumber() {
        assertThatThrownBy(() -> AmountParser.parse("je ne sais pas"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
