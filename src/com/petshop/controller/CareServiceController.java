package com.petshop.controller;

import com.petshop.dao.CareServiceRepository;
import com.petshop.model.CareServiceData;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class CareServiceController implements CrudController<CareServiceData, String> {
    private final CareServiceRepository repository;

    public CareServiceController(CareServiceRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public List<CareServiceData> findAll() {
        return repository.findAll();
    }

    @Override
    public List<CareServiceData> search(String keyword) {
        return repository.search(keyword);
    }

    @Override
    public void add(CareServiceData service) {
        validate(service);
        repository.add(service);
    }

    @Override
    public void update(CareServiceData service) {
        validate(service);
        repository.update(service);
    }

    @Override
    public void deactivate(String id) {
        repository.deactivate(id);
    }

    private void validate(CareServiceData service) {
        Objects.requireNonNull(service, "Thông tin dịch vụ không được để trống.");
        if (service.name() == null || service.name().isBlank()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (service.name().length() > 100
                || service.description() != null && service.description().length() > 255) {
            throw new IllegalArgumentException("Tên hoặc mô tả dịch vụ vượt giới hạn dữ liệu.");
        }
        if (service.durationMinutes() <= 0) {
            throw new IllegalArgumentException("Thời lượng dịch vụ phải lớn hơn 0 phút.");
        }
        if (service.effectiveDate() == null
                || service.effectiveDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày áp dụng giá không được ở quá khứ.");
        }
        if (service.smallPrice() == null || service.mediumPrice() == null
                || service.largePrice() == null) {
            throw new IllegalArgumentException("Vui lòng nhập giá cho đủ ba bậc cân nặng.");
        }
        for (var price : List.of(service.smallPrice(), service.mediumPrice(), service.largePrice())) {
            if (price == null || price.signum() < 0 || price.scale() > 2
                    || price.precision() > 18) {
                throw new IllegalArgumentException(
                        "Giá từng bậc cân nặng phải từ 0 trở lên, tối đa 2 chữ số thập phân.");
            }
        }
    }
}
