package com.petshop.service;

import com.petshop.dao.ThongKeDAO;
import com.petshop.model.HoatDongGanDay;
import com.petshop.model.ThongKeTongQuan;

import java.util.List;

/**
 * Lớp dịch vụ xử lý nghiệp vụ số liệu thống kê tổng quan và hoạt động gần đây.
 */
public class TongQuanService {

    private final ThongKeDAO thongKeDAO;

    public TongQuanService() {
        this.thongKeDAO = new ThongKeDAO();
    }

    public TongQuanService(ThongKeDAO thongKeDAO) {
        this.thongKeDAO = thongKeDAO;
    }

    /**
     * Lấy các chỉ số KPI thống kê trong ngày.
     * Mọi người dùng đã đăng nhập đều có thể xem màn Tổng quan.
     */
    public ThongKeTongQuan layThongKeTongQuan() {
        return thongKeDAO.layThongKeTongQuan();
    }

    /**
     * Lấy danh sách lịch hẹn và hoạt động gần đây trong ngày.
     */
    public List<HoatDongGanDay> layHoatDongGanDay() {
        return thongKeDAO.layHoatDongGanDay();
    }
}

