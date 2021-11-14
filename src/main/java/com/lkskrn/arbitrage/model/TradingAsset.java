package com.lkskrn.arbitrage.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
@ToString
@NoArgsConstructor
public class TradingAsset {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private Long id = -1L;

    @Basic(optional = false)
    @Column(nullable = false, unique = true)
    private String name;

    public TradingAsset(String name) {
        this.name = name;
    }
}
