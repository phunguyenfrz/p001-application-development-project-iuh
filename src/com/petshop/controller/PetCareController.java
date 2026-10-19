package com.petshop.controller;

import com.petshop.model.*;
import com.petshop.service.PetCareService;
import java.time.LocalDate;
import java.util.List;

/** Tiếp nhận yêu cầu từ View và chuyển tới Service chăm sóc. */
public class PetCareController {
    private final PetCareService care_service = new PetCareService();

    /**
     * @param pet_id Mã thú cưng.
     * @param from_date Ngày bắt đầu hoặc null.
     * @param to_date Ngày kết thúc hoặc null.
     * @param status Trạng thái cần xem.
     * @return Danh sách lịch hẹn phù hợp.
     */
    public List<PetCareVisit> loadVisits(String pet_id, LocalDate from_date,
            LocalDate to_date, String status) {
        return care_service.findVisits(pet_id, from_date, to_date, status);
    }

    /** @param pet_id Mã thú cưng. @return Thông tin chăm sóc hiện tại. */
    public PetCareProfile loadProfile(String pet_id) { return care_service.findProfile(pet_id); }

    /** @param visit_id Mã lịch hẹn. @param pet_id Mã thú cưng. @return Dịch vụ đã lưu. */
    public List<PetCareServiceItem> loadServices(String visit_id, String pet_id) {
        return care_service.findServices(visit_id, pet_id);
    }

    /**
     * @param pet_id Mã thú cưng.
     * @param weight_text Cân nặng nhập.
     * @param allergy Dị ứng.
     */
    public void updateProfile(String pet_id, String weight_text, String allergy) {
        care_service.updateProfile(pet_id, weight_text, allergy);
    }
}
