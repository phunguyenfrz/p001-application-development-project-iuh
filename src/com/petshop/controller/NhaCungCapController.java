package com.petshop.controller;

import com.petshop.model.NhaCungCap;
import com.petshop.service.NhaCungCapService;

import java.util.List;

/**
 * Lớp điều phối dữ liệu Nhà cung cấp giữa giao diện (View) và tầng nghiệp vụ (NhaCungCapService).
 * Tuân thủ phong cách của KhuyenMaiController.
 */
public class NhaCungCapController {

    private final NhaCungCapService nhaCungCapService;

    public NhaCungCapController() {
        this.nhaCungCapService = new NhaCungCapService();
    }

    public NhaCungCapController(NhaCungCapService nhaCungCapService) {
        this.nhaCungCapService = nhaCungCapService;
    }

    /**
     * Thêm nhà cung cấp mới.
     */
    public boolean themNhaCungCap(NhaCungCap nhaCungCap) {
        return nhaCungCapService.themNhaCungCap(nhaCungCap);
    }

    /**
     * Cập nhật thông tin nhà cung cấp.
     */
    public boolean capNhatNhaCungCap(NhaCungCap nhaCungCap) {
        return nhaCungCapService.capNhatNhaCungCap(nhaCungCap);
    }

    /**
     * Xóa mềm nhà cung cấp.
     */
    public boolean xoaMemNhaCungCap(String maNhaCungCap) {
        return nhaCungCapService.xoaMemNhaCungCap(maNhaCungCap);
    }

    /**
     * Lấy danh sách nhà cung cấp đang hoạt động (quản lý).
     */
    public List<NhaCungCap> layDanhSachNhaCungCap() {
        return nhaCungCapService.layDanhSachNhaCungCap();
    }

    /**
     * Lấy toàn bộ danh sách nhà cung cấp (kể cả ngừng hoạt động).
     */
    public List<NhaCungCap> layTatCaNhaCungCap() {
        return nhaCungCapService.layTatCaNhaCungCap();
    }

    /**
     * Tra cứu nhà cung cấp theo mã.
     */
    public NhaCungCap layTheoMa(String maNhaCungCap) {
        return nhaCungCapService.layTheoMa(maNhaCungCap);
    }

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa (mã, tên, SĐT hoặc email).
     */
    public List<NhaCungCap> timKiemNhaCungCap(String tuKhoa) {
        return nhaCungCapService.timKiemNhaCungCap(tuKhoa);
    }

    /**
     * Tìm kiếm theo từ khóa và trạng thái (null để lấy tất cả).
     */
    public List<NhaCungCap> timKiemNhaCungCap(String tuKhoa, Boolean trangThai) {
        return nhaCungCapService.timKiemNhaCungCap(tuKhoa, trangThai);
    }

    // =========================================================================
    // CÁC HÀM CHO DIALOG CHỌN NHÀ CUNG CẤP (QUAN_LY, BAN_HANG, ADMIN)
    // =========================================================================

    /**
     * Lấy danh sách nhà cung cấp đang hoạt động cho Dialog chọn NCC.
     */
    public List<NhaCungCap> layDanhSachDangHoatDong() {
        return nhaCungCapService.layDanhSachDangHoatDong();
    }

    /**
     * Tìm kiếm nhà cung cấp đang hoạt động cho Dialog chọn NCC.
     */
    public List<NhaCungCap> timKiemChoDialog(String tuKhoa) {
        return nhaCungCapService.timKiemChoDialog(tuKhoa);
    }
}
