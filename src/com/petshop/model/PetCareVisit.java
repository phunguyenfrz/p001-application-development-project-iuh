package com.petshop.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dữ liệu chỉ đọc của một lịch hẹn trong lịch sử chăm sóc.
 * Record của Java 17 tự tạo constructor và các hàm đọc cùng tên trường.
 * @param maLichHen Mã lịch hẹn.
 * @param maThuCung Mã thú cưng.
 * @param tenThuCung Tên thú cưng.
 * @param tenChuNuoi Tên chủ nuôi của lịch hẹn.
 * @param ngayHen Ngày hẹn.
 * @param gioHen Giờ hẹn.
 * @param tenNhanVien Nhân viên phụ trách.
 * @param trangThai Trạng thái lịch hẹn.
 * @param ghiChu Ghi chú lịch hẹn đã lưu.
 */
public record PetCareVisit(
        String maLichHen, String maThuCung, String tenThuCung,
        String tenChuNuoi, LocalDate ngayHen, LocalTime gioHen,
        String tenNhanVien, String trangThai, String ghiChu) {
}
