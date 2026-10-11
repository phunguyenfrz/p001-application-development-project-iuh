package com.petshop.controller;

import java.util.List;

public interface CrudController<T, ID> {
    List<T> findAll();

    List<T> search(String keyword);

    void add(T item);

    void update(T item);

    void deactivate(ID id);
}
