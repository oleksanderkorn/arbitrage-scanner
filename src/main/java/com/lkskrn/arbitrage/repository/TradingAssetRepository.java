package com.lkskrn.arbitrage.repository;

import com.lkskrn.arbitrage.model.TradingAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradingAssetRepository extends JpaRepository<TradingAsset, Long> {

    boolean existsByName(String name);
}
