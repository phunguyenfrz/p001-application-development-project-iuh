package com.petshop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp thực thể ReturnNote đại diện cho một phiếu đổi trả hàng hóa (bảng PhieuDoiTra).
 * Quản lý thông tin chung của phiếu, danh sách chi tiết sản phẩm cần hoàn trả,
 * cùng các quy tắc tính toán tổng tiền và trạng thái xử lý.
 *
 * Tiêu chuẩn tuân thủ:
 * - Tên lớp là danh từ (ReturnNote), tên phương thức là động từ.
 * - Có bình luận 1 dòng và nhiều dòng chuẩn Javadoc.
 */
public class ReturnNote {

    private String maPhieuDoiTra;      // PDT-YYYYMMDD-NNN
    private String maHoaDon;          // HD-YYYYMMDD-NNN
    private String maNhanVien;        // NV-YYYYMMDD-NNNNNN
    private String maKhachHang;       // KH-YYYYMMDD-NNNNNN
    private String tenKhachHang;      // Tên khách hàng hiển thị trên UI
    private LocalDate ngayLap;        // Ngày lập phiếu
    private String lyDo;              // Lý do đổi trả
    private int soLuong;              // Tổng số lượng sản phẩm hoàn trả
    private String trangThai;         // "Chờ xử lý", "Đã duyệt", "Đã từ chối"
    private double tongTienHoan;      // Tổng số tiền hoàn lại cho khách

    private List<ReturnNoteItem> danhSachChiTiet = new ArrayList<>();

    /**
     * Khởi tạo mặc định.
     */
    public ReturnNote() {
        this.ngayLap = LocalDate.now();
        this.trangThai = "Chờ xử lý";
    }

    /**
     * Khởi tạo đầy đủ các thuộc tính của phiếu đổi trả.
     *
     * @param maPhieuDoiTra Mã phiếu đổi trả (PDT-...)
     * @param maHoaDon      Mã hóa đơn gốc (HD-...)
     * @param maNhanVien    Mã nhân viên lập phiếu
     * @param maKhachHang   Mã khách hàng
     * @param tenKhachHang  Tên khách hàng
     * @param ngayLap       Ngày lập phiếu
     * @param lyDo          Lý do đổi trả
     * @param soLuong       Tổng số lượng sản phẩm đổi trả
     * @param trangThai     Trạng thái xử lý ("Chờ xử lý", "Đã duyệt", "Đã từ chối")
     * @param tongTienHoan  Tổng số tiền hoàn lại
     */
    public ReturnNote(String maPhieuDoiTra, String maHoaDon, String maNhanVien,
                      String maKhachHang, String tenKhachHang, LocalDate ngayLap,
                      String lyDo, int soLuong, String trangThai, double tongTienHoan) {
        this.maPhieuDoiTra = maPhieuDoiTra;
        this.maHoaDon = maHoaDon;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.ngayLap = (ngayLap != null) ? ngayLap : LocalDate.now();
        this.lyDo = lyDo;
        this.soLuong = soLuong;
        this.trangThai = (trangThai != null) ? trangThai : "Chờ xử lý";
        this.tongTienHoan = tongTienHoan;
    }

    /**
     * Tự động tính toán lại tổng tiền hoàn và tổng số lượng dựa trên danh sách chi tiết sản phẩm.
     */
    public void capNhatTongTienVaSoLuong() {
        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            this.tongTienHoan = 0;
            this.soLuong = 0;
            return;
        }

        double tongTien = 0;
        int tongSl = 0;
        for (ReturnNoteItem item : danhSachChiTiet) {
            tongTien += item.tinhThanhTien();
            tongSl += item.getSoLuong();
        }
        this.tongTienHoan = tongTien;
        this.soLuong = tongSl;
    }

    /**
     * Thêm một dòng chi tiết sản phẩm vào phiếu đổi trả.
     *
     * @param item Dòng sản phẩm chi tiết
     */
    public void themChiTiet(ReturnNoteItem item) {
        if (item != null) {
            this.danhSachChiTiet.add(item);
            capNhatTongTienVaSoLuong();
        }
    }

    /**
     * Xóa một dòng chi tiết sản phẩm khỏi phiếu đổi trả.
     *
     * @param item Dòng sản phẩm cần xóa
     */
    public void xoaChiTiet(ReturnNoteItem item) {
        if (item != null) {
            this.danhSachChiTiet.remove(item);
            capNhatTongTienVaSoLuong();
        }
    }

    // --- Getter và Setter ---

    public String getMaPhieuDoiTra() {
        return maPhieuDoiTra;
    }

    public void setMaPhieuDoiTra(String maPhieuDoiTra) {
        this.maPhieuDoiTra = maPhieuDoiTra;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public double getTongTienHoan() {
        return tongTienHoan;
    }

    public void setTongTienHoan(double tongTienHoan) {
        this.tongTienHoan = tongTienHoan;
    }

    public List<ReturnNoteItem> getDanhSachChiTiet() {
        return danhSachChiTiet;
    }

    public void setDanhSachChiTiet(List<ReturnNoteItem> danhSachChiTiet) {
        this.danhSachChiTiet = (danhSachChiTiet != null) ? danhSachChiTiet : new ArrayList<>();
        capNhatTongTienVaSoLuong();
    }

    @Override
    public String toString() {
        return "ReturnNote{" +
                "maPhieuDoiTra='" + maPhieuDoiTra + '\'' +
                ", maHoaDon='" + maHoaDon + '\'' +
                ", tenKhachHang='" + tenKhachHang + '\'' +
                ", tongTienHoan=" + tongTienHoan +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}

