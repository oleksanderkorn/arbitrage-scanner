package com.lkskrn.arbitrage.dto;

import java.util.List;

public record ProductPair(String base, String quote, List<ExchangePrice> prices) {
}

