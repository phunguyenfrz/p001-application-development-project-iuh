package com.petshop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NhaCungCap {
    private final String maNhaCungCap;
    private final String tenNhaCungCap;
    private final String soDienThoai;
    private final String diaChi;
    private final boolean ngungHoatDong;

    public NhaCungCap(
            String maNhaCungCap,
            String tenNhaCungCap,
            String soDienThoai,
            String diaChi,
            boolean ngungHoatDong) {
        this.maNhaCungCap = maNhaCungCap == null ? "" : maNhaCungCap.trim();
        this.tenNhaCungCap = tenNhaCungCap == null ? "" : tenNhaCungCap.trim();
        this.soDienThoai = soDienThoai == null ? "" : soDienThoai.trim();
        this.diaChi = diaChi == null ? "" : diaChi.trim();
        this.ngungHoatDong = ngungHoatDong;
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public boolean isNgungHoatDong() {
        return ngungHoatDong;
    }

    public NhaCungCap ngungHoatDong() {
        return new NhaCungCap(maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi, true);
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (maNhaCungCap.isBlank()) {
            errors.add("Mã nhà cung cấp không được để trống.");
        }
        if (tenNhaCungCap.isBlank()) {
            errors.add("Tên nhà cung cấp không được để trống.");
        }
        if (!soDienThoai.isBlank() && !soDienThoai.matches("[0-9+() .-]{8,20}")) {
            errors.add("Số điện thoại nhà cung cấp không hợp lệ.");
        }
        return List.copyOf(errors);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NhaCungCap supplier)) {
            return false;
        }
        return maNhaCungCap.equals(supplier.maNhaCungCap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maNhaCungCap);
    }
}
