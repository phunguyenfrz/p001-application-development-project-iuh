package com.petshop.controller;

import com.petshop.dao.InventoryRepository;
import com.petshop.model.InventoryItem;

import java.util.List;
import java.util.Objects;

public final class InventoryController {
    private final InventoryRepository repository;

    public InventoryController(InventoryRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<InventoryItem> findAll() {
        return repository.findAll();
    }

    public List<InventoryItem> search(String keyword) {
        return repository.search(keyword);
    }

    public List<InventoryItem> findBelowMinimum() {
        return repository.findBelowMinimum();
    }
}
