package com.feyza.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CurrencyServiceTest {

    @Test
    void convertsAmountUsingFrankfurterResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CurrencyService service = new CurrencyService(builder);

        server.expect(requestTo("https://api.frankfurter.dev/v1/latest?amount=100&base=USD&symbols=EUR"))
              .andRespond(withSuccess(
                  "{\"amount\":100.0,\"base\":\"USD\",\"date\":\"2026-10-06\",\"rates\":{\"EUR\":86.5}}",
                  MediaType.APPLICATION_JSON));

        BigDecimal result = service.convert(new BigDecimal("100"), "usd", "eur");

        assertThat(result).isEqualByComparingTo("86.5");
        server.verify();
    }

    @Test
    void rejectsInvalidCurrencyCode() {
        CurrencyService service = new CurrencyService(RestClient.builder());

        assertThatThrownBy(() -> service.convert(BigDecimal.TEN, "DOLLAR", "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
