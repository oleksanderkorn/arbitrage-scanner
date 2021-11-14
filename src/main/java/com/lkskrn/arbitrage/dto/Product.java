package com.lkskrn.arbitrage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

//{
//        "id": "CHZ-USDT",
//        "base_currency": "CHZ",
//        "quote_currency": "USDT",
//        "base_min_size": "3",
//        "base_max_size": "900000",
//        "quote_increment": "0.0001",
//        "base_increment": "0.1",
//        "display_name": "CHZ/USDT",
//        "min_market_funds": "1",
//        "max_market_funds": "100000",
//        "margin_enabled": false,
//        "fx_stablecoin": false,
//        "max_slippage_percentage": "0.10000000",
//        "post_only": false,
//        "limit_only": false,
//        "cancel_only": false,
//        "trading_disabled": false,
//        "status": "online",
//        "status_message": "",
//        "auction_mode": false
//}
public record Product(String id, @JsonProperty("base_currency") String baseCurrency,
                      @JsonProperty("quote_currency") String quoteCurrency, String status) {
}
