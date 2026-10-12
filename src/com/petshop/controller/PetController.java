package com.petshop.controller;

import com.petshop.dao.PetDao;
import com.petshop.model.Pet;
import com.petshop.model.Permission;
import com.petshop.model.Screen;
import com.petshop.model.User;
import com.petshop.navigation.ScreenRegistry;
import com.petshop.util.SessionManager;

import java.time.LocalDate;
import java.util.Objects;

public final class PetController extends CrudControllerImpl<Pet, String> {
    private final AuditController audit;
    private final PetDao pets;

    public PetController(PetDao dao, AuditController audit) {
        super(dao);
        this.pets = Objects.requireNonNull(dao);
        this.audit = Objects.requireNonNull(audit);
    }

    public java.util.List<Pet> findByCustomer(String customerId) {
        return pets.findByCustomer(customerId);
    }

    public java.util.List<String> findCareHistory(String petId) {
        return pets.findCareHistory(petId);
    }

    @Override
    public void add(Pet pet) {
        requirePermission(Permission.CREATE);
        validate(pet);
        super.add(pet);
        audit.log("Thêm", "ThuCung", pet.getMaThuCung(), null);
    }

    @Override
    public void update(Pet pet) {
        requirePermission(Permission.UPDATE);
        validate(pet);
        super.update(pet);
        audit.log("Sửa", "ThuCung", pet.getMaThuCung(), null);
    }

    @Override
    public void deactivate(String id) {
        requirePermission(Permission.DEACTIVATE);
        super.deactivate(id);
        audit.log("Ngừng", "ThuCung", id, null);
    }

    private void validate(Pet pet) {
        Objects.requireNonNull(pet, "Thông tin thú cưng không được để trống.");
        if (pet.getMaKhachHang() == null || pet.getMaKhachHang().isBlank()) {
            throw new IllegalArgumentException("Mã khách hàng là bắt buộc.");
        }
        if (pet.getTenThuCung() == null || pet.getTenThuCung().isBlank()) {
            throw new IllegalArgumentException("Tên thú cưng không được để trống.");
        }
        if (pet.getNgaySinh() != null && pet.getNgaySinh().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh thú cưng không được ở tương lai.");
        }
        if (!Double.isFinite(pet.getCanNang()) || pet.getCanNang() <= 0
                || pet.getCanNang() > 999.99) {
            throw new IllegalArgumentException("Cân nặng phải lớn hơn 0 và tối đa 999,99 kg.");
        }
    }

    private void requirePermission(Permission permission) {
        User user = SessionManager.getCurrentUser();
        if (user == null || !ScreenRegistry.hasPermission(
                Screen.PETS, user.getRole(), permission)) {
            throw new IllegalStateException(
                    "Bạn không có quyền " + permission.name().toLowerCase()
                            + " hồ sơ thú cưng.");
        }
    }
}
