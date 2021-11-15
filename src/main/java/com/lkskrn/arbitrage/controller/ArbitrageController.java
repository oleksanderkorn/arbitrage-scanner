package com.lkskrn.arbitrage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class ArbitrageController {

    @GetMapping("/")
    public ResponseEntity<String> get() {
        return ResponseEntity.ok("hello");
    }

}