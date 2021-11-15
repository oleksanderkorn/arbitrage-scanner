package com.lkskrn.arbitrage.dto;

import java.math.BigDecimal;

public record BasePointsEventData(String id, BigDecimal basePoints, BigDecimal binancePrice, BigDecimal binanceBips,
        BigDecimal coinbasePrice, BigDecimal coinbaseBips) {

}
