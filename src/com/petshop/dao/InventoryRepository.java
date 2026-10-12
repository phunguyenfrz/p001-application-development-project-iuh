package com.petshop.dao;

import com.petshop.model.InventoryItem;

import java.util.List;

public interface InventoryRepository {
    List<InventoryItem> findAll();

    List<InventoryItem> search(String keyword);

    List<InventoryItem> findBelowMinimum();
}
