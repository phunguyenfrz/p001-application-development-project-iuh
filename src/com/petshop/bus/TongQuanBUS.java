package com.petshop.bus;

import com.petshop.dao.ThongKeDAO;
import com.petshop.entity.HoatDongGanDay;
import com.petshop.entity.TaiKhoan;
import com.petshop.entity.ThongKeTongQuan;
import com.petshop.util.SessionManager;

import java.util.List;

/**
 * Lớp dịch vụ xử lý nghiệp vụ số liệu thống kê tổng quan và hoạt động gần đây.
 */
public class TongQuanBUS {

    private final ThongKeDAO thongKeDAO;

    public TongQuanBUS() {
        this.thongKeDAO = new ThongKeDAO();
    }

    public TongQuanBUS(ThongKeDAO thongKeDAO) {
        this.thongKeDAO = thongKeDAO;
    }

    /**
     * Lấy các chỉ số KPI thống kê trong ngày.
     * Mọi người dùng đã đăng nhập đều có thể xem màn Tổng quan.
     */
    public ThongKeTongQuan layThongKeTongQuan() {
        // TODO: nối HoaDonBUS, LichHenBUS, SanPhamBUS
        return thongKeDAO.layThongKeTongQuan();
    }

    /**
     * Lấy danh sách lịch hẹn và hoạt động gần đây trong ngày.
     */
    public List<HoatDongGanDay> layHoatDongGanDay() {
        // TODO: nối HoaDonBUS, LichHenBUS, SanPhamBUS
        return thongKeDAO.layHoatDongGanDay();
    }

    /**
     * Lấy thông tin tài khoản đang đăng nhập hiện tại từ SessionManager.
     */
    public TaiKhoan layTaiKhoanHienTai() {
        return SessionManager.getTaiKhoanHienTai();
    }
}
