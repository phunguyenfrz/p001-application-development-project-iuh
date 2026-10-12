package com.petshop.dao;

import com.petshop.model.PurchaseOrder;

import java.util.List;

public interface PurchaseOrderRepository {
    List<PurchaseOrder> findAll();
    List<PurchaseOrder.Choice> suppliers();
    List<PurchaseOrder.Choice> products();
    void create(PurchaseOrder order);
    void receive(String id);
}
