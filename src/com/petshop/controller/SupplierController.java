package com.petshop.controller;

import com.petshop.model.NhaCungCap;

public final class SupplierController extends CrudControllerImpl<NhaCungCap, String> {
    public SupplierController(com.petshop.dao.CrudDao<NhaCungCap, String> dao) {
        super(dao);
    }
}
