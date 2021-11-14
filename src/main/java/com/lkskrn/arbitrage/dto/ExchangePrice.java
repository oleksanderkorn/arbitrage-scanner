package com.lkskrn.arbitrage.dto;

import java.math.BigDecimal;

public record ExchangePrice(Exchange exchange, BigDecimal price) {
}
