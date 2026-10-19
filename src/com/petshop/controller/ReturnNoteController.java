package com.petshop.controller;

import com.petshop.dao.ReturnNoteDao;
import com.petshop.model.ReturnNote;
import com.petshop.model.ReturnNoteItem;

import java.util.List;

/**
 * Lớp ReturnNoteController điều khiển luồng dữ liệu và thực thi các quy tắc
 * nghiệp vụ cho phân hệ Quản lý Phiếu đổi trả (Return Note).
 *
 * Tiêu chuẩn tuân thủ:
 * - Tên lớp là danh từ (ReturnNoteController), tên phương thức là động từ.
 * - Kiểm tra chặt chẽ tính hợp lệ dữ liệu trước khi ghi nhận xuống DAO.
 * - Có bình luận 1 dòng và nhiều dòng chuẩn Javadoc.
 */
public class ReturnNoteController {

    private final ReturnNoteDao returnNoteDao;

    /**
     * Khởi tạo ReturnNoteController cùng đối tượng ReturnNoteDao mặc định.
     */
    public ReturnNoteController() {
        this.returnNoteDao = new ReturnNoteDao();
    }

    /**
     * Khởi tạo với DAO tùy biến (hỗ trợ kiểm thử đơn vị hoặc mock).
     *
     * @param returnNoteDao Đối tượng ReturnNoteDao cần liên kết
     */
    public ReturnNoteController(ReturnNoteDao returnNoteDao) {
        this.returnNoteDao = returnNoteDao;
    }

    /**
     * Lấy toàn bộ danh sách phiếu đổi trả.
     *
     * @return Danh sách ReturnNote
     */
    public List<ReturnNote> layTatCaPhieuDoiTra() {
        return returnNoteDao.layTatCaPhieuDoiTra();
    }

    /**
     * Lấy danh sách phiếu đổi trả theo trạng thái ("Chờ xử lý", "Đã duyệt", "Đã từ chối").
     *
     * @param trangThai Trạng thái cần lọc
     * @return Danh sách các phiếu thỏa điều kiện
     */
    public List<ReturnNote> layPhieuTheoTrangThai(String trangThai) {
        return returnNoteDao.layPhieuDoiTraTheoTrangThai(trangThai);
    }

    /**
     * Đếm số lượng phiếu theo trạng thái phục vụ hiển thị số lượng trên Tab.
     *
     * @param trangThai Trạng thái cần đếm
     * @return Số lượng bản ghi
     */
    public int demSoLuongTheoTrangThai(String trangThai) {
        return returnNoteDao.demSoLuongTheoTrangThai(trangThai);
    }

    /**
     * Tìm kiếm phiếu đổi trả theo từ khóa và trạng thái lọc hiện hành.
     *
     * @param tuKhoa    Chuỗi từ khóa tra cứu
     * @param trangThai Trạng thái tab đang chọn
     * @return Danh sách kết quả phù hợp
     */
    public List<ReturnNote> timKiem(String tuKhoa, String trangThai) {
        return returnNoteDao.timKiemTheoTuKhoa(tuKhoa, trangThai);
    }

    /**
     * Lấy danh sách sản phẩm chi tiết của một phiếu đổi trả đã lưu.
     *
     * @param maPhieuDoiTra Mã phiếu đổi trả
     * @return Danh sách ReturnNoteItem
     */
    public List<ReturnNoteItem> layChiTietPhieu(String maPhieuDoiTra) {
        if (maPhieuDoiTra == null || maPhieuDoiTra.isBlank()) {
            return List.of();
        }
        return returnNoteDao.layChiTietPhieu(maPhieuDoiTra.trim());
    }

    /**
     * Tự động sinh mã phiếu đổi trả tiếp theo theo ngày: PDT-YYYYMMDD-NNN.
     *
     * @return Chuỗi mã phiếu mới
     */
    public String sinhMaPhieuMoi() {
        return returnNoteDao.sinhMaPhieuTuDong();
    }

    /**
     * Tra cứu thông tin hóa đơn và khách hàng theo mã hóa đơn.
     *
     * @param maHoaDon Mã hóa đơn (HD-...)
     * @return Mảng Object[] thông tin hóa đơn hoặc null nếu không tìm thấy
     */
    public Object[] timThongTinHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.isBlank()) {
            return null;
        }
        return returnNoteDao.timThongTinHoaDon(maHoaDon.trim());
    }

    /**
     * Lấy danh sách các sản phẩm có thể đổi trả từ một hóa đơn đã mua.
     *
     * @param maHoaDon Mã hóa đơn
     * @return Danh sách sản phẩm mua
     */
    public List<ReturnNoteItem> layDanhSachSanPhamTheoHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.isBlank()) {
            return List.of();
        }
        return returnNoteDao.layDanhSachSanPhamTheoHoaDon(maHoaDon.trim());
    }

    /**
     * Kiểm tra tính hợp lệ và thực hiện lập phiếu đổi trả mới.
     *
     * @param phieu            Thông tin chung phiếu đổi trả
     * @param danhSachChiTiet  Danh sách sản phẩm đổi trả
     * @return true nếu lập phiếu thành công
     */
    public boolean lapPhieuDoiTra(ReturnNote phieu, List<ReturnNoteItem> danhSachChiTiet) {
        kiemTraHopLe(phieu, danhSachChiTiet);

        // Tính toán lại tổng tiền hoàn và số lượng chính xác trước khi lưu
        phieu.setDanhSachChiTiet(danhSachChiTiet);
        phieu.capNhatTongTienVaSoLuong();
        phieu.setTrangThai("Chờ xử lý");

        return returnNoteDao.themPhieuDoiTra(phieu, danhSachChiTiet);
    }

    /**
     * Thực hiện duyệt phiếu đổi trả (chuyển trạng thái sang "Đã duyệt").
     *
     * @param maPhieuDoiTra Mã phiếu cần duyệt
     * @return true nếu duyệt thành công
     */
    public boolean duyetPhieu(String maPhieuDoiTra) {
        if (maPhieuDoiTra == null || maPhieuDoiTra.isBlank()) {
            throw new IllegalArgumentException("Mã phiếu đổi trả không được để trống.");
        }
        return returnNoteDao.capNhatTrangThai(maPhieuDoiTra.trim(), "Đã duyệt");
    }

    /**
     * Thực hiện từ chối phiếu đổi trả (chuyển trạng thái sang "Đã từ chối").
     *
     * @param maPhieuDoiTra Mã phiếu cần từ chối
     * @return true nếu từ chối thành công
     */
    public boolean tuChoiPhieu(String maPhieuDoiTra) {
        if (maPhieuDoiTra == null || maPhieuDoiTra.isBlank()) {
            throw new IllegalArgumentException("Mã phiếu đổi trả không được để trống.");
        }
        return returnNoteDao.capNhatTrangThai(maPhieuDoiTra.trim(), "Đã từ chối");
    }

    /**
     * Thẩm định tính hợp lệ toàn diện của dữ liệu phiếu đổi trả theo quy tắc nghiệp vụ:
     * - Mã hóa đơn không được để trống
     * - Lý do đổi trả không được để trống
     * - Danh sách sản phẩm đổi trả phải có ít nhất 1 sản phẩm
     * - Từng sản phẩm đổi trả phải có số lượng > 0
     *
     * @param phieu           Đối tượng phiếu đổi trả
     * @param danhSachChiTiet Danh sách chi tiết
     */
    public void kiemTraHopLe(ReturnNote phieu, List<ReturnNoteItem> danhSachChiTiet) {
        if (phieu == null) {
            throw new IllegalArgumentException("Thông tin phiếu đổi trả không được để trống.");
        }

        if (phieu.getMaHoaDon() == null || phieu.getMaHoaDon().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập hoặc chọn mã hóa đơn cần đổi trả.");
        }

        if (phieu.getLyDo() == null || phieu.getLyDo().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do đổi trả.");
        }

        if (danhSachChiTiet == null || danhSachChiTiet.isEmpty()) {
            throw new IllegalArgumentException("Phiếu đổi trả phải có ít nhất một sản phẩm cần đổi trả.");
        }

        for (ReturnNoteItem item : danhSachChiTiet) {
            if (item.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng đổi trả của sản phẩm [" +
                        item.getTenSanPham() + "] phải lớn hơn 0.");
            }
        }
    }
}

