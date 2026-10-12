package com.petshop.controller;

import com.petshop.dao.CrudDao;

import java.util.List;
import java.util.Objects;

public class CrudControllerImpl<T, ID> implements CrudController<T, ID> {
    private final CrudDao<T, ID> dao;

    public CrudControllerImpl(CrudDao<T, ID> dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    @Override
    public List<T> findAll() {
        return dao.findAll();
    }

    @Override
    public List<T> search(String keyword) {
        return dao.search(keyword);
    }

    @Override
    public void add(T item) {
        dao.add(item);
    }

    @Override
    public void update(T item) {
        dao.update(item);
    }

    @Override
    public void deactivate(ID id) {
        dao.deactivate(id);
    }
}
