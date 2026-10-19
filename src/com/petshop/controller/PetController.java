package com.petshop.controller;

import com.petshop.model.Pet;
import com.petshop.service.PetService;

import java.util.List;

/**
 * Tiếp nhận thao tác hồ sơ thú cưng từ giao diện.
 * Chuyển yêu cầu đến PetService để xử lý nghiệp vụ.
 */
public class PetController {

    // Đối tượng xử lý nghiệp vụ thú cưng.
    private final PetService pet_service = new PetService();

    /**
     * Lấy danh sách thú cưng đang được theo dõi.
     *
     * @return Các hồ sơ đang theo dõi.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<Pet> loadActivePets() {
        return pet_service.findActive();
    }

    /**
     * Lấy toàn bộ hồ sơ để tra cứu, kể cả hồ sơ ngừng theo dõi.
     *
     * @return Danh sách toàn bộ thú cưng.
     * @throws IllegalStateException Khi không đọc được dữ liệu.
     */
    public List<Pet> loadPets() {
        return pet_service.findAll();
    }

    /**
     * Yêu cầu thêm hồ sơ thú cưng mới.
     * Service kiểm tra dữ liệu và tạo mã hồ sơ.
     *
     * @param pet Thông tin thú cưng nhập trên form.
     * @throws IllegalArgumentException Khi dữ liệu không hợp lệ.
     * @throws IllegalStateException Khi lưu dữ liệu thất bại.
     */
    public void addPet(Pet pet) {
        pet_service.insert(pet);
    }

    /**
     * Yêu cầu sửa hồ sơ thú cưng đang được theo dõi.
     *
     * @param pet Thú cưng chứa mã hồ sơ và thông tin mới.
     * @throws IllegalArgumentException Khi dữ liệu không hợp lệ.
     * @throws IllegalStateException Khi cập nhật thất bại.
     */
    public void updatePet(Pet pet) {
        pet_service.update(pet);
    }

    /**
     * Yêu cầu ngừng theo dõi hồ sơ bằng xóa mềm.
     *
     * @param pet_id Mã thú cưng cần ngừng theo dõi.
     * @throws IllegalArgumentException Khi mã không hợp lệ.
     * @throws IllegalStateException Khi cập nhật trạng thái thất bại.
     */
    public void deactivatePet(String pet_id) {
        pet_service.deactivate(pet_id);
    }
}