package com.petshop.view;

import com.petshop.model.TaiKhoan;
import com.petshop.service.DanhMucMenu;
import com.petshop.service.XacThucService;
import com.petshop.view.catruc.CapNhatCaTrucView;
import com.petshop.view.catruc.PhanCaNhanVienView;
import com.petshop.view.catruc.TimKiemCaTrucView;
import com.petshop.view.dichvu.BangGiaDichVuView;
import com.petshop.view.dichvu.BaoCaoDichVuView;
import com.petshop.view.dichvu.CapNhatDichVuView;
import com.petshop.view.dichvu.TimKiemDichVuView;
import com.petshop.view.khachhang.CapNhatKhachHangView;
import com.petshop.view.khachhang.LichSuGiaoDichKhachHangView;
import com.petshop.view.khachhang.TimKiemKhachHangView;
import com.petshop.view.lichhen.CapNhatLichHenView;
import com.petshop.view.lichhen.DatLichHenView;
import com.petshop.view.lichhen.TimKiemLichHenView;
import com.petshop.view.khuyenmai.CapNhatKhuyenMaiView;
import com.petshop.view.khuyenmai.TimKiemKhuyenMaiView;
import com.petshop.view.nhacungcap.CapNhatNhaCungCapView;
import com.petshop.view.nhacungcap.TimKiemNhaCungCapView;
import com.petshop.view.nhanvien.CapNhatNhanVienView;
import com.petshop.view.nhanvien.PhanQuyenNhanVienView;
import com.petshop.view.nhanvien.TaiKhoanDangNhapView;
import com.petshop.view.nhanvien.TimKiemNhanVienView;
import com.petshop.view.sanpham.BaoCaoSanPhamView;
import com.petshop.view.sanpham.CapNhatSanPhamView;
import com.petshop.view.sanpham.TheoDoiTonKhoView;
import com.petshop.view.sanpham.TimKiemSanPhamView;
import com.petshop.view.thucung.CapNhatThuCungView;
import com.petshop.view.thucung.LichSuChamSocThuCungView;
import com.petshop.view.thucung.TimKiemThuCungView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình chính của hệ thống Happy Tails.
 * Sidebar được dựng tự động từ DanhMucMenu theo đúng các phân hệ nghiệp vụ.
 */
public class ManHinhChinhView extends BorderPane {

    private final Stage stage;
    private final TaiKhoan taiKhoan;
    private final XacThucService xacThucService;

    // Vùng hiển thị nội dung chính
    private final StackPane pnlNoiDung;

    // Quản lý Accordion (chỉ mở 1 đối tượng tại 1 thời điểm)
    private final List<VBox> dsMenuCon = new ArrayList<>();
    private final List<Label> dsMuiTen = new ArrayList<>();

    // Thu gọn sidebar
    private boolean daThuGon = false;
    private StackPane pnlThanhDieuHuong;

    // Mục đang chọn
    private Node nutDangChon;
    private Button btnTongQuan;
    private String maNhomDangChon;
    private String maChucNangDangChon;
    private Label lblDuongDan;

    public ManHinhChinhView(Stage stage) {
        this.stage = stage;
        this.xacThucService = new XacThucService();
        this.taiKhoan = xacThucService.getTaiKhoanHienTai();

        getStyleClass().add("man-hinh-chinh");

        // 1. Sidebar bên trái
        this.pnlThanhDieuHuong = new StackPane();
        hienThiThanhDieuHuong();
        setLeft(pnlThanhDieuHuong);

        // 2. Nội dung chính
        VBox pnlChinh = new VBox();
        HBox.setHgrow(pnlChinh, Priority.ALWAYS);

        this.pnlNoiDung = new StackPane();
        VBox.setVgrow(pnlNoiDung, Priority.ALWAYS);

        // Mặc định hiển thị màn hình Tổng quan
        hienThiTongQuan();

        pnlChinh.getChildren().addAll(
                taoThanhTieuDe(),
                pnlNoiDung
        );

        setCenter(pnlChinh);
    }

    private void hienThiThanhDieuHuong() {
        pnlThanhDieuHuong.getChildren().clear();
        if (daThuGon) {
            pnlThanhDieuHuong.getChildren().add(taoThanhThuGon());
        } else {
            pnlThanhDieuHuong.getChildren().add(taoThanhMoRong());
        }
    }

    // =========================================================================
    // 1. SIDEBAR MỞ RỘNG
    // =========================================================================
    private VBox taoThanhMoRong() {
        VBox pnlThanhMoRong = new VBox();
        pnlThanhMoRong.getStyleClass().add("thanh-dieu-huong");

        nutDangChon = null;
        dsMenuCon.clear();
        dsMuiTen.clear();

        // Brand Logo + Nút thu gọn
        HBox pnlThuongHieu = new HBox(10);
        pnlThuongHieu.setAlignment(Pos.CENTER_LEFT);
        pnlThuongHieu.getStyleClass().add("hop-thuong-hieu");

        StackPane pnlLogo = new StackPane();
        pnlLogo.getChildren().add(taoLogoThuongHieu(34));
        pnlLogo.setOnMouseClicked(e -> hienThiTongQuan());
        pnlLogo.setStyle("-fx-cursor: hand;");

        VBox pnlTenThuongHieu = new VBox(1);
        Label lblTieuDe = new Label("Happy Tails");
        lblTieuDe.getStyleClass().add("tieu-de-thuong-hieu");
        lblTieuDe.setOnMouseClicked(e -> hienThiTongQuan());
        lblTieuDe.setStyle("-fx-cursor: hand;");

        Label lblPhuDe = new Label("Cửa hàng thú cưng");
        lblPhuDe.getStyleClass().add("phu-de-thuong-hieu");
        pnlTenThuongHieu.getChildren().addAll(lblTieuDe, lblPhuDe);

        Region pnlKhoangTrongThuongHieu = new Region();
        HBox.setHgrow(pnlKhoangTrongThuongHieu, Priority.ALWAYS);

        Button btnThuGon = new Button("«");
        btnThuGon.getStyleClass().add("nut-thu-gon");
        btnThuGon.setTooltip(new Tooltip("Thu gọn thanh điều hướng"));
        btnThuGon.setOnAction(e -> {
            daThuGon = true;
            hienThiThanhDieuHuong();
        });

        pnlThuongHieu.getChildren().addAll(pnlLogo, pnlTenThuongHieu, pnlKhoangTrongThuongHieu, btnThuGon);

        // Danh sách Menu
        VBox pnlDanhSachMenu = new VBox(4);
        pnlDanhSachMenu.setPadding(new Insets(10, 12, 10, 12));

        // Nút "Tổng quan"
        Button mnuTongQuan = new Button("Tổng quan");
        mnuTongQuan.setGraphic(taoIconMenu("TONG_QUAN", 18));
        mnuTongQuan.setGraphicTextGap(10);
        btnTongQuan = mnuTongQuan;
        mnuTongQuan.setMaxWidth(Double.MAX_VALUE);
        mnuTongQuan.getStyleClass().add(maChucNangDangChon == null ? "nut-dieu-huong-dang-chon" : "nut-dieu-huong");
        mnuTongQuan.setTooltip(new Tooltip("Tổng quan"));
        if (maChucNangDangChon == null) {
            nutDangChon = mnuTongQuan;
        }
        mnuTongQuan.setOnAction(e -> {
            dongTatCaMenu();
            hienThiTongQuan();
        });
        pnlDanhSachMenu.getChildren().add(mnuTongQuan);

        // Dựng Menu từ DanhMucMenu
        List<DanhMucMenu.NhomMenu> dsNhomMenu = DanhMucMenu.layMenuTheoTaiKhoan(taiKhoan);

        for (DanhMucMenu.NhomMenu nhom : dsNhomMenu) {
            VBox pnlMucMenu = taoMucMenu(nhom);
            pnlDanhSachMenu.getChildren().add(pnlMucMenu);
        }

        ScrollPane scrMenu = new ScrollPane(pnlDanhSachMenu);
        scrMenu.setFitToWidth(true);
        scrMenu.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrMenu, Priority.ALWAYS);

        pnlThanhMoRong.getChildren().addAll(pnlThuongHieu, scrMenu);
        return pnlThanhMoRong;
    }

    private VBox taoMucMenu(DanhMucMenu.NhomMenu nhom) {
        VBox pnlMucMenu = new VBox(2);
        pnlMucMenu.setPadding(new Insets(2, 0, 2, 0));

        HBox mnuTieuDeNhom = new HBox(8);
        mnuTieuDeNhom.setAlignment(Pos.CENTER_LEFT);
        mnuTieuDeNhom.getStyleClass().add("muc-dieu-huong");

        Node iconNhom = taoIconMenu(nhom.getLoaiIcon(), 18);

        Label lblTieuDe = new Label(nhom.getTieuDe());
        lblTieuDe.getStyleClass().add("chu-dieu-huong");
        lblTieuDe.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(lblTieuDe, Priority.ALWAYS);

        Tooltip goiY = new Tooltip(nhom.getTieuDe());
        Tooltip.install(mnuTieuDeNhom, goiY);

        Label lblMuiTen = new Label("▸");
        lblMuiTen.getStyleClass().add("mui-ten-dieu-huong");
        dsMuiTen.add(lblMuiTen);

        mnuTieuDeNhom.getChildren().addAll(iconNhom, lblTieuDe, lblMuiTen);

        // Danh sách chức năng con
        VBox pnlDanhSachMenuCon = new VBox(2);
        boolean coChucNangDangChon = false;
        for (DanhMucMenu.ChucNangMenu chucNang : nhom.getDanhSachChucNang()) {
            Button mnuChucNang = new Button(chucNang.getTenChucNang());
            mnuChucNang.setMaxWidth(Double.MAX_VALUE);
            mnuChucNang.getStyleClass().add("nut-chuc-nang-con");
            if (chucNang.getMaChucNang().equals(maChucNangDangChon)) {
                mnuChucNang.getStyleClass().remove("nut-chuc-nang-con");
                mnuChucNang.getStyleClass().add("nut-chuc-nang-con-dang-chon");
                nutDangChon = mnuChucNang;
                coChucNangDangChon = true;
            }
            if (chucNang.getPhimTat() != null) {
                mnuChucNang.setTooltip(new Tooltip(chucNang.getTenChucNang() + " (" + chucNang.getPhimTat() + ")"));
            } else {
                mnuChucNang.setTooltip(new Tooltip(chucNang.getTenChucNang()));
            }

            mnuChucNang.setOnAction(e -> {
                maNhomDangChon = nhom.getMaNhom();
                maChucNangDangChon = chucNang.getMaChucNang();
                chonNut(mnuChucNang);
                moManHinhPhanHe(nhom, chucNang);
            });
            pnlDanhSachMenuCon.getChildren().add(mnuChucNang);
        }

        pnlDanhSachMenuCon.setVisible(coChucNangDangChon);
        pnlDanhSachMenuCon.setManaged(coChucNangDangChon);
        if (coChucNangDangChon) {
            lblMuiTen.setText("▾");
        }
        dsMenuCon.add(pnlDanhSachMenuCon);

        // Accordion: chỉ mở 1 đối tượng tại một thời điểm
        mnuTieuDeNhom.setOnMouseClicked(e -> {
            boolean dangMo = pnlDanhSachMenuCon.isVisible();
            dongTatCaMenu();
            if (!dangMo) {
                pnlDanhSachMenuCon.setVisible(true);
                pnlDanhSachMenuCon.setManaged(true);
                lblMuiTen.setText("▾");
            }
        });

        pnlMucMenu.getChildren().addAll(mnuTieuDeNhom, pnlDanhSachMenuCon);
        return pnlMucMenu;
    }

    private void dongTatCaMenu() {
        for (int i = 0; i < dsMenuCon.size(); i++) {
            dsMenuCon.get(i).setVisible(false);
            dsMenuCon.get(i).setManaged(false);
            if (i < dsMuiTen.size()) dsMuiTen.get(i).setText("▸");
        }
    }

    // =========================================================================
    // 2. SIDEBAR THU GỌN
    // =========================================================================
    private VBox taoThanhThuGon() {
        VBox pnlThanhThuGon = new VBox();
        pnlThanhThuGon.getStyleClass().add("thanh-dieu-huong-thu-gon");
        pnlThanhThuGon.setAlignment(Pos.TOP_CENTER);
        nutDangChon = null;

        VBox pnlTrenCung = new VBox(10);
        pnlTrenCung.setAlignment(Pos.CENTER);
        pnlTrenCung.setPadding(new Insets(16, 0, 12, 0));

        StackPane pnlLogo = new StackPane();
        pnlLogo.getChildren().add(taoLogoThuongHieu(34));
        pnlLogo.setOnMouseClicked(e -> hienThiTongQuan());
        pnlLogo.setStyle("-fx-cursor: hand;");

        Button btnMoRong = new Button("»");
        btnMoRong.getStyleClass().add("nut-thu-gon");
        btnMoRong.setTooltip(new Tooltip("Mở rộng thanh điều hướng"));
        btnMoRong.setOnAction(e -> {
            daThuGon = false;
            hienThiThanhDieuHuong();
        });

        pnlTrenCung.getChildren().addAll(pnlLogo, btnMoRong);

        VBox pnlDanhSachMenu = new VBox(6);
        pnlDanhSachMenu.setAlignment(Pos.TOP_CENTER);
        pnlDanhSachMenu.setPadding(new Insets(8, 0, 8, 0));

        // Nút tổng quan thu gọn
        Button mnuTongQuanThuGon = new Button();
        mnuTongQuanThuGon.setGraphic(taoIconMenu("TONG_QUAN", 20));
        btnTongQuan = mnuTongQuanThuGon;
        mnuTongQuanThuGon.getStyleClass().add(maChucNangDangChon == null
            ? "nut-thu-gon-item-dang-chon" : "nut-thu-gon-item");
        if (maChucNangDangChon == null) {
            nutDangChon = mnuTongQuanThuGon;
        }
        mnuTongQuanThuGon.setTooltip(new Tooltip("Tổng quan"));
        mnuTongQuanThuGon.setOnAction(e -> hienThiTongQuan());
        pnlDanhSachMenu.getChildren().add(mnuTongQuanThuGon);

        List<DanhMucMenu.NhomMenu> dsNhomMenu = DanhMucMenu.layMenuTheoTaiKhoan(taiKhoan);

        for (DanhMucMenu.NhomMenu nhom : dsNhomMenu) {
            Button mnuNhom = new Button();
            mnuNhom.setGraphic(taoIconMenu(nhom.getLoaiIcon(), 20));
            boolean laNhomDangChon = nhom.getMaNhom().equals(maNhomDangChon);
            mnuNhom.getStyleClass().add(laNhomDangChon
                    ? "nut-thu-gon-item-dang-chon" : "nut-thu-gon-item");
            if (laNhomDangChon) {
                nutDangChon = mnuNhom;
            }
            mnuNhom.setTooltip(new Tooltip(nhom.getTieuDe()));
            mnuNhom.setOnAction(e -> {
                daThuGon = false;
                hienThiThanhDieuHuong();
            });
            pnlDanhSachMenu.getChildren().add(mnuNhom);
        }

        ScrollPane scrMenu = new ScrollPane(pnlDanhSachMenu);
        scrMenu.setFitToWidth(true);
        scrMenu.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrMenu, Priority.ALWAYS);

        pnlThanhThuGon.getChildren().addAll(pnlTrenCung, scrMenu);
        return pnlThanhThuGon;
    }

    private void chonNut(Node nutMoi) {
        if (nutDangChon != null) {
            var lopCu = nutDangChon.getStyleClass();
            boolean laNutThuGon = lopCu.contains("nut-thu-gon-item")
                    || lopCu.contains("nut-thu-gon-item-dang-chon");
            boolean laNutTongQuan = lopCu.contains("nut-dieu-huong")
                    || lopCu.contains("nut-dieu-huong-dang-chon");
            lopCu.removeAll("nut-dieu-huong-dang-chon", "nut-chuc-nang-con-dang-chon", "nut-thu-gon-item-dang-chon");
            lopCu.removeAll("nut-dieu-huong", "nut-chuc-nang-con", "nut-thu-gon-item");
            if (laNutThuGon) {
                lopCu.add("nut-thu-gon-item");
            } else if (laNutTongQuan) {
                lopCu.add("nut-dieu-huong");
            } else {
                lopCu.add("nut-chuc-nang-con");
            }
        }
        this.nutDangChon = nutMoi;
        var lopMoi = nutMoi.getStyleClass();
        lopMoi.removeAll("nut-dieu-huong", "nut-chuc-nang-con", "nut-thu-gon-item",
                "nut-dieu-huong-dang-chon", "nut-chuc-nang-con-dang-chon", "nut-thu-gon-item-dang-chon");
        if (nutMoi == btnTongQuan) {
            lopMoi.add(daThuGon ? "nut-thu-gon-item-dang-chon" : "nut-dieu-huong-dang-chon");
        } else if (daThuGon) {
            lopMoi.add("nut-thu-gon-item-dang-chon");
        } else {
            lopMoi.add("nut-chuc-nang-con-dang-chon");
        }
    }

    // =========================================================================
    // 3. THANH TIÊU ĐỀ (TOPBAR)
    // =========================================================================
    private HBox taoThanhTieuDe() {
        HBox pnlThanhTieuDe = new HBox(16);
        pnlThanhTieuDe.getStyleClass().add("thanh-tieu-de-tren");

        String hoTen = (taiKhoan != null && taiKhoan.getHoTenNhanVien() != null) ? taiKhoan.getHoTenNhanVien() : "Quản trị viên";
        String tenVaiTro = layTenVaiTroHienTai();

        lblDuongDan = new Label(tenVaiTro + " / Tổng quan");
        lblDuongDan.getStyleClass().add("duong-dan-trang");

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        // Profile Avatar + Thông tin tài khoản
        String chuCaiDau = layChuCaiDau(hoTen);

        StackPane pnlAnhDaiDien = new StackPane();
        Circle vongTron = new Circle(18, Color.web("#f97316"));
        Label lblAnhDaiDien = new Label(chuCaiDau);
        lblAnhDaiDien.getStyleClass().add("chu-anh-dai-dien");
        pnlAnhDaiDien.getChildren().addAll(vongTron, lblAnhDaiDien);

        VBox pnlChiTietTaiKhoan = new VBox(2);
        Label lblTenTaiKhoan = new Label(hoTen);
        lblTenTaiKhoan.getStyleClass().add("ten-tai-khoan");
        Label lblVaiTro = new Label(tenVaiTro);
        lblVaiTro.getStyleClass().add("vai-tro-tai-khoan");
        pnlChiTietTaiKhoan.getChildren().addAll(lblTenTaiKhoan, lblVaiTro);

        // Menu tài khoản
        MenuButton mnuTaiKhoan = new MenuButton();
        mnuTaiKhoan.getStyleClass().add("nhom-tai-khoan");
        HBox pnlThongTinTaiKhoan = new HBox(8, pnlAnhDaiDien, pnlChiTietTaiKhoan);
        mnuTaiKhoan.setGraphic(pnlThongTinTaiKhoan);
        mnuTaiKhoan.setTooltip(new Tooltip("Tài khoản"));

        MenuItem mnuDoiMatKhau = new MenuItem("Đổi mật khẩu");
        mnuDoiMatKhau.setOnAction(e -> hienThiHopThoaiDoiMatKhau());

        MenuItem mnuDangXuat = new MenuItem("Đăng xuất");
        mnuDangXuat.setOnAction(e -> dangXuat());

        SeparatorMenuItem mnuNganCach = new SeparatorMenuItem();
        mnuTaiKhoan.getItems().addAll(mnuDoiMatKhau, mnuNganCach, mnuDangXuat);

        pnlThanhTieuDe.getChildren().addAll(
                lblDuongDan,
                pnlKhoangTrong,
                mnuTaiKhoan
        );
        return pnlThanhTieuDe;
    }

    // =========================================================================
    // 4. ĐIỀU HƯỚNG MÀN HÌNH PHÂN HỆ & MÀN HÌNH TỔNG QUAN
    // =========================================================================
    private void moManHinhPhanHe(DanhMucMenu.NhomMenu nhom, DanhMucMenu.ChucNangMenu chucNang) {
        // Kiểm tra phân quyền ở tầng Service
        xacThucService.kiemTraQuyen(chucNang.getMaChucNang());

        Node manHinhDich = switch (nhom.getMaNhom()) {
            case "KHACH_HANG"          -> {
                if ("KH_CAP_NHAT".equals(chucNang.getMaChucNang())) {
                    yield new CapNhatKhachHangView();
                } else if ("KH_TIM_KIEM".equals(chucNang.getMaChucNang())) {
                    yield new TimKiemKhachHangView();
                } else if ("KH_LICH_SU".equals(chucNang.getMaChucNang())) {
                    yield new LichSuGiaoDichKhachHangView();
                }
                yield new DanhSachChucNangView(nhom.getTieuDe(), chucNang.getTenChucNang(), chucNang.getPhimTat());
            }
            case "THU_CUNG"               -> {
                if ("TC_TIM_KIEM".equals(chucNang.getMaChucNang())) {
                    yield new TimKiemThuCungView();
                } else if ("TC_CHAM_SOC".equals(chucNang.getMaChucNang())) {
                    yield new LichSuChamSocThuCungView();
                }
                yield new CapNhatThuCungView();
            }
            case "LICH_HEN"       -> {
                if ("LH_TAO".equals(chucNang.getMaChucNang())) {
                    yield new DatLichHenView();
                } else if ("LH_CAP_NHAT".equals(chucNang.getMaChucNang())) {
                    yield new CapNhatLichHenView();
                } else if ("LH_TIM_KIEM".equals(chucNang.getMaChucNang())) {
                    yield new TimKiemLichHenView();
                }
                yield new CapNhatLichHenView();
            }
            case "PHIEU_DICH_VU" -> new DanhSachChucNangView(nhom.getTieuDe(), chucNang.getTenChucNang(), chucNang.getPhimTat());
            case "NHAN_VIEN"          -> {
                if ("NV_TIM_KIEM".equals(chucNang.getMaChucNang())) {
                    yield new TimKiemNhanVienView();
                } else if ("NV_TAI_KHOAN".equals(chucNang.getMaChucNang())) {
                    yield new TaiKhoanDangNhapView();
                } else if ("NV_PHAN_QUYEN".equals(chucNang.getMaChucNang())) {
                    yield new PhanQuyenNhanVienView();
                }
                yield new CapNhatNhanVienView();
            }
            case "CA_TRUC"             -> {
                if ("CT_PHAN_CONG".equals(chucNang.getMaChucNang())) {
                    yield new PhanCaNhanVienView();
                } else if ("CT_XEM".equals(chucNang.getMaChucNang())) {
                    yield new TimKiemCaTrucView();
                }
                yield new CapNhatCaTrucView();
            }
            case "DICH_VU_CHAM_SOC"      -> {
                if ("DVCS_TIM_KIEM".equals(chucNang.getMaChucNang())) {
                    yield new TimKiemDichVuView();
                } else if ("DVCS_BANG_GIA".equals(chucNang.getMaChucNang())) {
                    yield new BangGiaDichVuView();
                } else if ("DVCS_BAO_CAO".equals(chucNang.getMaChucNang())) {
                    yield new BaoCaoDichVuView();
                }
                yield new CapNhatDichVuView();
            }
            case "NHA_CUNG_CAP"        -> {
                if ("NCC_TIM_KIEM".equals(chucNang.getMaChucNang())) {
                    yield new TimKiemNhaCungCapView();
                }
                yield new CapNhatNhaCungCapView();
            }
            case "DON_NHAP"      -> new DanhSachChucNangView(nhom.getTieuDe(), chucNang.getTenChucNang(), chucNang.getPhimTat());
            case "SAN_PHAM"           -> {
                if ("PRD_SEARCH".equals(chucNang.getMaChucNang())) {
                    yield new TimKiemSanPhamView();
                } else if ("PRD_INVENTORY".equals(chucNang.getMaChucNang())) {
                    yield new TheoDoiTonKhoView();
                } else if ("PRD_REPORT".equals(chucNang.getMaChucNang())) {
                    yield new BaoCaoSanPhamView();
                }
                yield new CapNhatSanPhamView();
            }
            case "KHUYEN_MAI"         -> {
                if ("KM_TIM_KIEM".equals(chucNang.getMaChucNang())) {
                    yield new TimKiemKhuyenMaiView();
                }
                yield new CapNhatKhuyenMaiView();
            }
            case "HOA_DON"           -> new DanhSachChucNangView(nhom.getTieuDe(), chucNang.getTenChucNang(), chucNang.getPhimTat());
            case "PHIEU_DOI_TRA"       -> new DanhSachChucNangView(nhom.getTieuDe(), chucNang.getTenChucNang(), chucNang.getPhimTat());
            default                  -> new DanhSachChucNangView(nhom.getTieuDe(), chucNang.getTenChucNang(), chucNang.getPhimTat());
        };

        lblDuongDan.setText(layTenVaiTroHienTai() + " / " + nhom.getTieuDe() + " / " + chucNang.getTenChucNang());
        pnlNoiDung.getChildren().setAll(manHinhDich);
    }

    public void hienThiTongQuan() {
        maNhomDangChon = null;
        maChucNangDangChon = null;
        if (lblDuongDan != null) {
            lblDuongDan.setText(layTenVaiTroHienTai() + " / Tổng quan");
        }
        if (btnTongQuan != null) {
            chonNut(btnTongQuan);
        }
        pnlNoiDung.getChildren().setAll(new TongQuanView());
    }

    private String layTenVaiTroHienTai() {
        return (taiKhoan != null && taiKhoan.getPhanQuyen() != null) ? taiKhoan.getPhanQuyen().getTenHienThi() : "Admin";
    }

    // =========================================================================
    // 5. TIá»†N ÃCH TÃ€I KHOáº¢N
    // =========================================================================
    public void hienThiHopThoaiDoiMatKhau() {
        new DoiMatKhauView().showAndWait();
    }

    public void dangXuat() {
        xacThucService.dangXuat();
        stage.setTitle("Happy Tails - Đăng nhập");
        Scene scene = new Scene(new DangNhapView(stage), 520, 400);
        scene.getStylesheets().addAll(
                getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm(),
                getClass().getResource("/com/petshop/view/css/dangnhap.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
    }

    private Node taoIconMenu(String loaiIcon, double size) {
        String duongVector = switch (loaiIcon) {
            case "TONG_QUAN" -> "M3 3h7v7H3z M14 3h7v7h-7z M14 14h7v7h-7z M3 14h7v7H3z";
            case "KHACH_HANG" -> "M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8z";
            case "THU_CUNG" -> "M8 10a2 2 0 1 0 0-4 2 2 0 0 0 0 4z M16 10a2 2 0 1 0 0-4 2 2 0 0 0 0 4z M5 15a2 2 0 1 0 0-4 2 2 0 0 0 0 4z M19 15a2 2 0 1 0 0-4 2 2 0 0 0 0 4z M12 12c-3 0-6 4-6 6.5A2.5 2.5 0 0 0 8.5 21c1.3 0 2.2-.8 3.5-.8s2.2.8 3.5.8a2.5 2.5 0 0 0 2.5-2.5C18 16 15 12 12 12z";
            case "LICH_HEN", "CA_TRUC" -> "M8 2v4 M16 2v4 M3 10h18 M5 4h14a2 2 0 0 1 2 2v14H3V6a2 2 0 0 1 2-2z M8 14h3 M8 18h3";
            case "NHAN_VIEN" -> "M16 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 M10 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z M20 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75";
            case "NHA_CUNG_CAP", "DON_NHAP" -> "m12 3 9 5-9 5-9-5 9-5z M3 8v8l9 5 9-5V8 M12 13v8 M7.5 5.5l9 5";
            case "SAN_PHAM" -> "M3 3h2l2.4 12.4A2 2 0 0 0 9.4 17H19a2 2 0 0 0 2-1.6L23 8H6 M10 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2z M18 21a1 1 0 1 0 0-2 1 1 0 0 0 0 2z";
            case "DICH_VU_CHAM_SOC" -> "M6 3a3 3 0 0 0 0 6h2l10 12a2 2 0 0 0 3-3L11 8 M8 9l3-3 M18 5l-6 6";
            case "KHUYEN_MAI" -> "M20.6 13.4 12 22l-9-9V3h10l7.6 7.6a2 2 0 0 1 0 2.8z M7.5 7.5h.01";
            case "HOA_DON" -> "M6 2h12v20l-3-2-3 2-3-2-3 2V2z M9 7h6 M9 11h6 M9 15h3";
            case "PHIEU_DOI_TRA" -> "M3 3v18h18 M8 16v-5 M13 16V6 M18 16v-8";
            default -> "M4 4h16v16H4z M8 8h8v8H8z";
        };

        SVGPath hinhVector = new SVGPath();
        hinhVector.setContent(duongVector);
        hinhVector.setFill(Color.TRANSPARENT);
        hinhVector.setStroke(Color.web("#98a2b3"));
        hinhVector.setStrokeWidth(1.8);
        hinhVector.setStrokeLineCap(StrokeLineCap.ROUND);
        hinhVector.setStrokeLineJoin(StrokeLineJoin.ROUND);
        hinhVector.setScaleX(size / 24.0);
        hinhVector.setScaleY(size / 24.0);

        StackPane icon = new StackPane(hinhVector);
        icon.setMinSize(size, size);
        icon.setPrefSize(size, size);
        icon.setMaxSize(size, size);
        icon.getStyleClass().add("icon-dieu-huong");
        return icon;
    }

    private Node taoLogoThuongHieu(double size) {
        try {
            var luongLogo = getClass().getResourceAsStream("/com/petshop/view/logo.png");
            if (luongLogo != null) {
                ImageView imgLogo = new ImageView(new Image(luongLogo));
                imgLogo.setFitWidth(size);
                imgLogo.setFitHeight(size);
                imgLogo.setPreserveRatio(true);
                imgLogo.setSmooth(true);
                return imgLogo;
            }
        } catch (Exception ignored) {}

        StackPane khungLogo = new StackPane();
        khungLogo.setStyle("-fx-background-color: #f97316; -fx-background-radius: 8px;");
        khungLogo.setMinSize(size, size);
        khungLogo.setPrefSize(size, size);
        khungLogo.setMaxSize(size, size);
        return khungLogo;
    }

    private String layChuCaiDau(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) return "HT";
        String[] cacTu = hoTen.trim().split("\\s+");
        if (cacTu.length == 1) return cacTu[0].substring(0, Math.min(2, cacTu[0].length())).toUpperCase();
        return ("" + cacTu[cacTu.length - 2].charAt(0) + cacTu[cacTu.length - 1].charAt(0)).toUpperCase();
    }
}

