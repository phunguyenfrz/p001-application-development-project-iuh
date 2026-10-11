package com.petshop.controller;

import com.petshop.dao.CrudDao;
import com.petshop.model.Pet;

import java.util.Objects;

public final class PetController extends CrudControllerImpl<Pet, String> {
    public PetController(CrudDao<Pet, String> dao) {
        super(dao);
    }

    @Override
    public void add(Pet pet) {
        validate(pet);
        super.add(pet);
    }

    @Override
    public void update(Pet pet) {
        validate(pet);
        super.update(pet);
    }

    private void validate(Pet pet) {
        Objects.requireNonNull(pet, "Thông tin thú cưng không được để trống.");
        if (pet.getMaKhachHang() == null || pet.getMaKhachHang().isBlank()) {
            throw new IllegalArgumentException("Mã khách hàng là bắt buộc.");
        }
        if (pet.getTenThuCung() == null || pet.getTenThuCung().isBlank()) {
            throw new IllegalArgumentException("Tên thú cưng không được để trống.");
        }
        if (!Double.isFinite(pet.getCanNang()) || pet.getCanNang() <= 0
                || pet.getCanNang() > 999.99) {
            throw new IllegalArgumentException("Cân nặng phải lớn hơn 0 và tối đa 999,99 kg.");
        }
    }
}
