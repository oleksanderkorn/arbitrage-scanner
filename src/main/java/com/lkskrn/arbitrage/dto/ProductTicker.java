package com.lkskrn.arbitrage.dto;


import java.math.BigDecimal;

//{
//    "trade_id": 235388150,
//    "price": "64467.99",
//    "size": "0.00148372",
//    "time": "2021-11-12T21:19:08.455346Z",
//    "bid": "64467.98",
//    "ask": "64467.99",
//    "volume": "13420.6947733"
//}
public record ProductTicker(BigDecimal price) {
}
