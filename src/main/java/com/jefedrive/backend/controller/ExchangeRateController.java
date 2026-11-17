
package com.jefedrive.backend.controller;

import com.jefedrive.backend.service.ExchangeRateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(
            ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/CNY")
    public ExchangeRateService.ExchangeRateResponse getCnyRate() {
        return exchangeRateService.getCnyRate();
    }
}

