package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.model.TradingAsset;
import com.lkskrn.arbitrage.repository.TradingAssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradingAssetService {
    TradingAssetRepository repository;

    @Autowired
    public TradingAssetService(TradingAssetRepository repository) {
        this.repository = repository;
    }

    public void save(TradingAsset pair) {
        repository.save(pair);
    }

    public List<TradingAsset> findAll() {
        return repository.findAll();
    }

    public boolean assetExists(String name) {
        return repository.existsByName(name);
    }
}
