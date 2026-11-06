package com.petshop.bus;

import com.petshop.dao.ThongKeDAO;
import com.petshop.entity.ThongKeTongQuan;

import java.util.Map;

/**
 * Dịch vụ nghiệp vụ cung cấp số liệu thống kê và biểu đồ cho màn hình Tổng quan (Dashboard).
 * Không import javafx.* trong tầng BUS.
 */
public class TongQuanBUS {

    private final ThongKeDAO thongKeDAO;

    public TongQuanBUS() {
        this.thongKeDAO = new ThongKeDAO();
    }

    public ThongKeTongQuan layThongKeHomNay() {
        return thongKeDAO.layThongKeHomNay();
    }

    public Map<String, Double> layDoanhThu7NgayGanNhat() {
        return thongKeDAO.layDoanhThu7NgayGanNhat();
    }

    public Map<String, Double> layCoCauDoanhThuBanHangVaDichVu() {
        return thongKeDAO.layCoCauDoanhThuBanHangVaDichVu();
    }
}
