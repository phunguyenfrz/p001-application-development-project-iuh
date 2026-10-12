package com.petshop.dao;

import java.util.List;

public interface CrudDao<T, ID> {
    List<T> findAll();

    List<T> search(String keyword);

    void add(T item);

    void update(T item);

    void deactivate(ID id);
}
