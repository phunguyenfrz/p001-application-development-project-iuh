package com.petshop.controller;

import com.petshop.model.HoatDongGanDay;
import com.petshop.model.ThongKeTongQuan;
import com.petshop.service.TongQuanService;

import java.util.List;

/**
 * Lớp điều khiển màn hình Tổng quan Dashboard.
 */
public class TongQuanController {

    private final TongQuanService tongQuanService;

    public TongQuanController() {
        this.tongQuanService = new TongQuanService();
    }

    public TongQuanController(TongQuanService tongQuanService) {
        this.tongQuanService = tongQuanService;
    }

    /**
     * Lấy dữ liệu KPI thống kê cho màn hình Tổng quan.
     */
    public ThongKeTongQuan layThongKeTongQuan() {
        return tongQuanService.layThongKeTongQuan();
    }

    /**
     * Lấy danh sách hoạt động và lịch hẹn gần đây trong ngày.
     */
    public List<HoatDongGanDay> layHoatDongGanDay() {
        return tongQuanService.layHoatDongGanDay();
    }
}

