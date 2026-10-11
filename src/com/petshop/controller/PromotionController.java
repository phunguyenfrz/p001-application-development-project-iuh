package com.petshop.controller;

import com.petshop.dao.CrudDao;
import com.petshop.controller.CrudControllerImpl;
import com.petshop.model.Promotion;

import java.util.Objects;

public final class PromotionController extends CrudControllerImpl<Promotion, String> {
    public PromotionController(CrudDao<Promotion, String> dao) {
        super(dao);
    }

    @Override
    public void add(Promotion promotion) {
        validate(promotion);
        super.add(promotion);
    }

    @Override
    public void update(Promotion promotion) {
        validate(promotion);
        super.update(promotion);
    }

    private void validate(Promotion promotion) {
        Objects.requireNonNull(promotion, "Thông tin khuyến mãi không được để trống.");
        if (promotion.name() == null || promotion.name().isBlank()
                || promotion.name().length() > 150) {
            throw new IllegalArgumentException("Tên chương trình là bắt buộc, tối đa 150 ký tự.");
        }
        if (promotion.discountPercent() == null
                || promotion.discountPercent().signum() <= 0
                || promotion.discountPercent().compareTo(java.math.BigDecimal.valueOf(100)) > 0
                || promotion.discountPercent().scale() > 2) {
            throw new IllegalArgumentException("Phần trăm giảm phải lớn hơn 0 và tối đa 100.");
        }
        if (promotion.startsOn() == null || promotion.endsOn() == null
                || promotion.endsOn().isBefore(promotion.startsOn())) {
            throw new IllegalArgumentException("Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
        }
    }
}
