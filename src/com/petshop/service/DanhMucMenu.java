package com.petshop.service;

import com.petshop.model.TaiKhoan;

import java.util.*;

/**
 * Danh mục các phân hệ đối tượng và quyền hạn truy cập trên thanh điều hướng:
 * Khách hàng, Thú cưng, Lịch hẹn, Tiếp nhận dịch vụ, Nhân viên, Ca trực,
 * Nhà cung cấp, Đơn nhập hàng, Sản phẩm, Dịch vụ chăm sóc, Khuyến mãi, Hóa đơn, Phiếu đổi trả.
 */
public class DanhMucMenu {

    public static class ChucNangMenu {
        private final String maChucNang;
        private final String tenChucNang;
        private final String phimTat;

        public ChucNangMenu(String maChucNang, String tenChucNang, String phimTat) {
            this.maChucNang = maChucNang;
            this.tenChucNang = tenChucNang;
            this.phimTat = phimTat;
        }

        public String getMaChucNang() { return maChucNang; }
        public String getTenChucNang() { return tenChucNang; }
        public String getPhimTat() { return phimTat; }
    }

    public static class NhomMenu {
        private final String maNhom;
        private final String tieuDe;
        private final String loaiIcon;
        private final List<ChucNangMenu> danhSachChucNang;

        public NhomMenu(String maNhom, String tieuDe, String loaiIcon, List<ChucNangMenu> danhSachChucNang) {
            this.maNhom = maNhom;
            this.tieuDe = tieuDe;
            this.loaiIcon = loaiIcon;
            this.danhSachChucNang = danhSachChucNang;
        }

        public String getMaNhom() { return maNhom; }
        public String getTieuDe() { return tieuDe; }
        public String getLoaiIcon() { return loaiIcon; }
        public List<ChucNangMenu> getDanhSachChucNang() { return danhSachChucNang; }
    }

    private static final List<NhomMenu> TAT_CA_MENU = new ArrayList<>();
    private static final Map<String, Set<TaiKhoan.VaiTro>> BANG_PHAN_QUYEN = new HashMap<>();

    static {
        // 1. Khách hàng
        TAT_CA_MENU.add(new NhomMenu("KHACH_HANG", "Khách hàng", "KHACH_HANG", List.of(
                new ChucNangMenu("KH_CAP_NHAT", "Cập nhật khách hàng", "Alt+C"),
                new ChucNangMenu("KH_TIM_KIEM", "Tìm kiếm khách hàng", "Ctrl+F"),
                new ChucNangMenu("KH_LICH_SU", "Lịch sử giao dịch", null)
        )));

        // 2. Thú cưng
        TAT_CA_MENU.add(new NhomMenu("THU_CUNG", "Thú cưng", "THU_CUNG", List.of(
                new ChucNangMenu("TC_CAP_NHAT", "Cập nhật thú cưng", "Alt+P"),
                new ChucNangMenu("TC_TIM_KIEM", "Tìm kiếm thú cưng", "Ctrl+F"),
                new ChucNangMenu("TC_CHAM_SOC", "Lịch sử chăm sóc", null)
        )));

        // 3. Lịch hẹn
        TAT_CA_MENU.add(new NhomMenu("LICH_HEN", "Lịch hẹn", "LICH_HEN", List.of(
                new ChucNangMenu("LH_TAO", "Đặt lịch hẹn", null),
                new ChucNangMenu("LH_CAP_NHAT", "Cập nhật lịch hẹn", null),
                new ChucNangMenu("LH_TIM_KIEM", "Tìm kiếm lịch hẹn", "Ctrl+F")
        )));

        // 4. Phiếu dịch vụ (Tiếp nhận thú cưng làm dịch vụ tại quầy)
        TAT_CA_MENU.add(new NhomMenu("PHIEU_DICH_VU", "Phiếu dịch vụ", "DICH_VU_CHAM_SOC", List.of(
                new ChucNangMenu("PDV_TAO", "Tạo phiếu tiếp nhận", null),
                new ChucNangMenu("PDV_CAP_NHAT", "Cập nhật phiếu dịch vụ", null),
                new ChucNangMenu("PDV_TIM_KIEM", "Tìm kiếm phiếu dịch vụ", "Ctrl+F")
        )));

        // 5. Nhân viên
        TAT_CA_MENU.add(new NhomMenu("NHAN_VIEN", "Nhân viên", "NHAN_VIEN", List.of(
                new ChucNangMenu("NV_CAP_NHAT", "Cập nhật nhân viên", "Alt+E"),
                new ChucNangMenu("NV_TIM_KIEM", "Tìm kiếm nhân viên", "Ctrl+F"),
                new ChucNangMenu("NV_PHAN_QUYEN", "Phân quyền", null),
                new ChucNangMenu("NV_TAI_KHOAN", "Tài khoản đăng nhập", null)
        )));

        // 6. Ca trực
        TAT_CA_MENU.add(new NhomMenu("CA_TRUC", "Ca trực", "CA_TRUC", List.of(
                new ChucNangMenu("CT_CAP_NHAT", "Cập nhật ca trực", null),
                new ChucNangMenu("CT_PHAN_CONG", "Phân công ca trực", null),
                new ChucNangMenu("CT_XEM", "Lịch ca trực", null)
        )));

        // 7. Nhà cung cấp
        TAT_CA_MENU.add(new NhomMenu("NHA_CUNG_CAP", "Nhà cung cấp", "NHA_CUNG_CAP", List.of(
                new ChucNangMenu("NCC_CAP_NHAT", "Cập nhật nhà cung cấp", null),
                new ChucNangMenu("NCC_TIM_KIEM", "Tìm kiếm nhà cung cấp", "Ctrl+F")
        )));

        // 8. Đơn hàng nhập
        TAT_CA_MENU.add(new NhomMenu("DON_NHAP", "Đơn hàng nhập", "DON_NHAP", List.of(
                new ChucNangMenu("DN_TAO", "Lập đơn nhập", null),
                new ChucNangMenu("DN_NHAN_HANG", "Nhận hàng", null),
                new ChucNangMenu("DN_CAP_NHAT", "Cập nhật đơn nhập", null),
                new ChucNangMenu("DN_TIM_KIEM", "Tìm kiếm đơn nhập", "Ctrl+F")
        )));

        // 9. Sản phẩm
        TAT_CA_MENU.add(new NhomMenu("SAN_PHAM", "Sản phẩm", "SAN_PHAM", List.of(
                new ChucNangMenu("PRD_CRUD", "Cập nhật sản phẩm", null),
                new ChucNangMenu("PRD_SEARCH", "Tìm kiếm sản phẩm", "Ctrl+F"),
                new ChucNangMenu("PRD_INVENTORY", "Tồn kho", null),
                new ChucNangMenu("PRD_REPORT", "Báo cáo sản phẩm", null)
        )));

        // 10. Dịch vụ chăm sóc
        TAT_CA_MENU.add(new NhomMenu("DICH_VU_CHAM_SOC", "Dịch vụ chăm sóc", "DICH_VU_CHAM_SOC", List.of(
                new ChucNangMenu("DVCS_CAP_NHAT", "Cập nhật dịch vụ", null),
                new ChucNangMenu("DVCS_TIM_KIEM", "Tìm kiếm dịch vụ", "Ctrl+F"),
                new ChucNangMenu("DVCS_BANG_GIA", "Bảng giá dịch vụ", null),
                new ChucNangMenu("DVCS_BAO_CAO", "Báo cáo dịch vụ", null)
        )));

        // 11. Khuyến mãi
        TAT_CA_MENU.add(new NhomMenu("KHUYEN_MAI", "Khuyến mãi", "KHUYEN_MAI", List.of(
                new ChucNangMenu("KM_CAP_NHAT", "Cập nhật khuyến mãi", null),
                new ChucNangMenu("KM_TIM_KIEM", "Tìm kiếm khuyến mãi", "Ctrl+F")
        )));

        // 12. Hóa đơn
        TAT_CA_MENU.add(new NhomMenu("HOA_DON", "Hóa đơn", "HOA_DON", List.of(
                new ChucNangMenu("HD_TAO", "Lập hóa đơn", null),
                new ChucNangMenu("HD_TIM_KIEM", "Tìm kiếm hóa đơn", "Ctrl+F"),
                new ChucNangMenu("HD_CAP_NHAT", "Cập nhật hóa đơn", null),
                new ChucNangMenu("HD_DOANH_THU", "Báo cáo doanh thu", null)
        )));

        // 13. Phiếu đổi trả
        TAT_CA_MENU.add(new NhomMenu("PHIEU_DOI_TRA", "Phiếu đổi trả", "PHIEU_DOI_TRA", List.of(
                new ChucNangMenu("DT_TAO", "Lập phiếu đổi trả", null),
                new ChucNangMenu("DT_XU_LY", "Xử lý đổi trả", null),
                new ChucNangMenu("DT_TIM_KIEM", "Tìm kiếm phiếu đổi trả", "Ctrl+F")
        )));

        // Khởi tạo bảng ma trận phân quyền theo vai trò
        for (NhomMenu nhom : TAT_CA_MENU) {
            for (ChucNangMenu chucNang : nhom.getDanhSachChucNang()) {
                Set<TaiKhoan.VaiTro> dsVaiTro = new HashSet<>();
                dsVaiTro.add(TaiKhoan.VaiTro.ADMIN);

                // Quản lý: Quản trị nhân sự, ca trực, danh mục dịch vụ, khuyến mãi và báo cáo
                if (nhom.getMaNhom().equals("NHAN_VIEN") || nhom.getMaNhom().equals("CA_TRUC") ||
                    nhom.getMaNhom().equals("NHA_CUNG_CAP") || nhom.getMaNhom().equals("DICH_VU_CHAM_SOC") ||
                    nhom.getMaNhom().equals("KHUYEN_MAI") || nhom.getMaNhom().equals("SAN_PHAM") ||
                    chucNang.getMaChucNang().equals("HD_DOANH_THU")) {
                    dsVaiTro.add(TaiKhoan.VaiTro.QUAN_LY);
                }

                // Bán hàng: Khách hàng, sản phẩm, đơn nhập, hóa đơn, đổi trả
                if (nhom.getMaNhom().equals("KHACH_HANG") || nhom.getMaNhom().equals("SAN_PHAM") ||
                    nhom.getMaNhom().equals("DON_NHAP") || nhom.getMaNhom().equals("PHIEU_DOI_TRA") ||
                    (nhom.getMaNhom().equals("HOA_DON") && !chucNang.getMaChucNang().equals("HD_DOANH_THU"))) {
                    dsVaiTro.add(TaiKhoan.VaiTro.BAN_HANG);
                }

                // Chăm sóc: Lịch hẹn, phiếu dịch vụ, thú cưng, tra cứu khách hàng và dịch vụ
                if (nhom.getMaNhom().equals("LICH_HEN") || nhom.getMaNhom().equals("PHIEU_DICH_VU") ||
                    nhom.getMaNhom().equals("THU_CUNG") || chucNang.getMaChucNang().equals("KH_TIM_KIEM") || chucNang.getMaChucNang().equals("DVCS_TIM_KIEM")) {
                    dsVaiTro.add(TaiKhoan.VaiTro.CHAM_SOC);
                }

                BANG_PHAN_QUYEN.put(chucNang.getMaChucNang(), dsVaiTro);
            }
        }
        BANG_PHAN_QUYEN.put("PRD_LOW_STOCK", Set.of(TaiKhoan.VaiTro.ADMIN, TaiKhoan.VaiTro.QUAN_LY, TaiKhoan.VaiTro.BAN_HANG));
        // Các service sản phẩm vẫn kiểm tra quyền bằng mã SP_*, song song với mã PRD_* trên menu.
        Set<TaiKhoan.VaiTro> quyenSanPham = Set.of(
                TaiKhoan.VaiTro.ADMIN,
                TaiKhoan.VaiTro.QUAN_LY,
                TaiKhoan.VaiTro.BAN_HANG
        );
        BANG_PHAN_QUYEN.put("SP_CAP_NHAT", quyenSanPham);
        BANG_PHAN_QUYEN.put("SP_TIM_KIEM", quyenSanPham);
        BANG_PHAN_QUYEN.put("SP_TON_KHO", quyenSanPham);
    }

    public static List<NhomMenu> layTatCaMenu() {
        return Collections.unmodifiableList(TAT_CA_MENU);
    }

    public static List<NhomMenu> layMenuTheoVaiTro(TaiKhoan.VaiTro vaiTro) {
        if (vaiTro == null) return Collections.emptyList();
        if (vaiTro == TaiKhoan.VaiTro.ADMIN) return layTatCaMenu();

        List<NhomMenu> ketQua = new ArrayList<>();
        for (NhomMenu nhom : TAT_CA_MENU) {
            List<ChucNangMenu> chucNangDuocPhep = new ArrayList<>();
            for (ChucNangMenu chucNang : nhom.getDanhSachChucNang()) {
                if (kiemTraQuyen(vaiTro, chucNang.getMaChucNang())) {
                    chucNangDuocPhep.add(chucNang);
                }
            }
            if (!chucNangDuocPhep.isEmpty()) {
                ketQua.add(new NhomMenu(nhom.getMaNhom(), nhom.getTieuDe(), nhom.getLoaiIcon(), chucNangDuocPhep));
            }
        }
        return ketQua;
    }

    public static List<NhomMenu> layMenuTheoTaiKhoan(TaiKhoan taiKhoan) {
        if (taiKhoan == null) return Collections.emptyList();
        if (taiKhoan.getPhanQuyen() == TaiKhoan.VaiTro.ADMIN) return layTatCaMenu();

        Set<String> quyenTuyChinh = PhanQuyenService.layDanhSachQuyen(taiKhoan.getMaNhanVien());
        if (quyenTuyChinh == null) {
            return layMenuTheoVaiTro(taiKhoan.getPhanQuyen());
        }

        List<NhomMenu> ketQua = new ArrayList<>();
        for (NhomMenu nhom : TAT_CA_MENU) {
            List<ChucNangMenu> chucNangDuocPhep = new ArrayList<>();
            for (ChucNangMenu chucNang : nhom.getDanhSachChucNang()) {
                if (quyenTuyChinh.contains(chucNang.getMaChucNang())) {
                    chucNangDuocPhep.add(chucNang);
                }
            }
            if (!chucNangDuocPhep.isEmpty()) {
                ketQua.add(new NhomMenu(nhom.getMaNhom(), nhom.getTieuDe(), nhom.getLoaiIcon(), chucNangDuocPhep));
            }
        }
        return ketQua;
    }

    public static boolean kiemTraQuyen(TaiKhoan taiKhoan, String maChucNang) {
        if (taiKhoan == null) return false;
        if (taiKhoan.getPhanQuyen() == TaiKhoan.VaiTro.ADMIN) return true;
        Set<String> quyenTuyChinh = PhanQuyenService.layDanhSachQuyen(taiKhoan.getMaNhanVien());
        if (quyenTuyChinh != null) {
            return quyenTuyChinh.contains(maChucNang);
        }
        return kiemTraQuyen(taiKhoan.getPhanQuyen(), maChucNang);
    }

    public static boolean kiemTraQuyen(TaiKhoan.VaiTro vaiTro, String maChucNang) {
        if (vaiTro == TaiKhoan.VaiTro.ADMIN) return true;
        Set<TaiKhoan.VaiTro> duocPhep = BANG_PHAN_QUYEN.get(maChucNang);
        return duocPhep != null && duocPhep.contains(vaiTro);
    }
}

