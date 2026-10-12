package com.petshop.controller;

import com.petshop.dao.CrudDao;
import com.petshop.model.Product;

import java.math.BigDecimal;
import java.util.Objects;

public final class ProductController extends CrudControllerImpl<Product, String> {
    public ProductController(CrudDao<Product, String> dao) {
        super(dao);
    }

    @Override
    public void add(Product product) {
        validate(product, true);
        super.add(product);
    }

    @Override
    public void update(Product product) {
        validate(product, false);
        super.update(product);
    }

    private void validate(Product product, boolean adding) {
        Objects.requireNonNull(product, "Thông tin sản phẩm không được để trống.");
        if (product.getTenSanPham() == null || product.getTenSanPham().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        if (product.getDonViTinh() == null || product.getDonViTinh().isBlank()) {
            throw new IllegalArgumentException("Đơn vị tính không được để trống.");
        }
        BigDecimal price = product.getGiaBanDecimal();
        if (price == null || price.signum() < 0 || price.precision() > 18
                || price.scale() > 2) {
            throw new IllegalArgumentException("Giá bán phải từ 0 trở lên và tối đa 2 chữ số thập phân.");
        }
        if (adding && product.getSoLuongTon() < 0) {
            throw new IllegalArgumentException("Tồn kho ban đầu không được âm.");
        }
        if (product.getNguongTonToiThieu() < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn tối thiểu không được âm.");
        }
        if (product.getMaVach() != null && product.getMaVach().length() > 50) {
            throw new IllegalArgumentException("Mã vạch không được vượt quá 50 ký tự.");
        }
    }
}
