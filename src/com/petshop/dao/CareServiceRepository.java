package com.petshop.dao;

import com.petshop.model.CareServiceData;

import java.util.List;

public interface CareServiceRepository {
    List<CareServiceData> findAll();

    List<CareServiceData> search(String keyword);

    void add(CareServiceData service);

    void update(CareServiceData service);

    void deactivate(String id);
}
