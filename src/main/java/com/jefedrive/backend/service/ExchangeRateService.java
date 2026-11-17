
package com.jefedrive.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Service
public class ExchangeRateService {

    private final RestClient restClient;

    public ExchangeRateService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://open.er-api.com/v6/latest/XAF")
                .build();
    }

    public ExchangeRateResponse getCnyRate() {
        try {
            ApiResponse response = restClient.get()
                    .retrieve()
                    .body(ApiResponse.class);

            if (response == null
                    || !"success".equals(response.result())
                    || response.rates() == null
                    || response.rates().get("CNY") == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Exchange rate unavailable");
            }

            BigDecimal xafPerCny = BigDecimal.ONE
                    .divide(
                            response.rates().get("CNY"),
                            8,
                            java.math.RoundingMode.HALF_UP);

            return new ExchangeRateResponse(
                    "XAF",
                    "CNY",
                    xafPerCny,
                    response.lastUpdateUtc(),
                    response.nextUpdateUtc()
            );

        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to retrieve exchange rate");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiResponse(
            String result,
            Map<String, BigDecimal> rates,
            @JsonProperty("time_last_update_utc") String lastUpdateUtc,
            @JsonProperty("time_next_update_utc") String nextUpdateUtc
    ) {}

    public record ExchangeRateResponse(
            String baseCurrency,
            String targetCurrency,
            BigDecimal rate,
            String lastUpdated,
            String nextUpdate
    ) {}
}

