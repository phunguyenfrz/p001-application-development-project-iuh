package com.petshop.gui.phieudichvu;

import com.petshop.bus.PhieuDichVuBUS;
import com.petshop.entity.BangGiaDichVu;
import com.petshop.entity.ChiTietLichHen;
import com.petshop.entity.ChiTietPhieuDichVu;
import com.petshop.entity.DichVuChamSoc;
import com.petshop.entity.KhachHang;
import com.petshop.entity.LichHen;
import com.petshop.entity.NhanVien;
import com.petshop.entity.PhieuDichVu;
import com.petshop.entity.TaiKhoan;
import com.petshop.entity.ThuCung;
import com.petshop.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Giao diện Lập phiếu tiếp nhận dịch vụ chăm sóc tại quầy (PDV_TAO).
 * Tuân thủ 100% Bộ chỉ thị kỹ thuật IUH:
 * - Không nạp RAM ComboBox khách hàng: Tra cứu SĐT trực tiếp hoặc chọn Lịch hẹn hôm nay.
 * - Nhân viên tự động lấy từ SessionManager (khóa ô chọn).
 * - Cân nặng thực tế tự động áp đúng bảng giá dịch vụ theo bậc (Nhỏ, Vừa, Lớn).
 * - Sử dụng kiểu nguyên thủy double, không BigDecimal.
 */
public class LapPhieuTiepNhanDichVuView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/gui/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PhieuDichVuBUS controller = new PhieuDichVuBUS();
    private final Runnable khiLuuThanhCong;

    // Danh sách lưu trữ dữ liệu
    private final ObservableList<ChiTietPhieuDichVu> dsChiTietChon = FXCollections.observableArrayList();
    private final Map<String, Double> mapCanNangDaCan = new HashMap<>();

    private double donGiaHienHanh = 0.0;
    private KhachHang khachHangHienTai = null;
    private NhanVien nhanVienHienTai = null;

    // Lịch hẹn liên kết hiện tại (nếu tiếp nhận theo lịch hẹn)
    private LichHen lichHenHienTai = null;

    // Điều khiển thông tin phiếu (Phần 1)
    private final TextField txtMaPhieuDichVu = new TextField();
    private final TextField txtTimKhach = new TextField();
    private final Button btnKiemTraSdt = new Button("Tra cứu");
    private final Button btnXemLichHomNay = new Button("📋 Lịch hôm nay");
    private final Button btnThemKhachNhanh = new Button("+ Khách mới");
    private final TextField txtKhachHangHienThi = new TextField();
    private final HBox pnlBadgeLichHen = new HBox(6);
    private final Label lblThongTinLichHen = new Label();
    private final Button btnHuyLienKetLich = new Button("✕ Bỏ lịch");
    private final TextField txtNhanVienHienThi = new TextField();
    private final DatePicker dpNgayTiepNhan = new DatePicker();
    private final TextField txtGioTiepNhan = new TextField();
    private final TextArea txtGhiChu = new TextArea();

    // Điều khiển chọn dòng dịch vụ (Phần 2)
    private final ComboBox<ThuCung> cboThuCung = new ComboBox<>();
    private final Button btnThemThuCungNhanh = new Button("+ Mới");
    private final TextField txtCanNangThucTe = new TextField();
    private final ComboBox<DichVuChamSoc> cboDichVu = new ComboBox<>();
    private final TextField txtDonGia = new TextField();
    private final Button btnThemDichVu = new Button("+ Thêm vào phiếu");
    private final TableView<ChiTietPhieuDichVu> tblChiTiet = new TableView<>();
    private final Label lblTongTien = new Label("Tổng tiền: 0 đ");

    // Nút tác vụ chân trang
    private final Button btnLuu = new Button("Lập phiếu tiếp nhận");
    private final Button btnLamMoi = new Button("Làm mới");

    public LapPhieuTiepNhanDichVuView() {
        this(null);
    }

    public LapPhieuTiepNhanDichVuView(Runnable khiLuuThanhCong) {
        this.khiLuuThanhCong = khiLuuThanhCong;

        setSpacing(10);
        setPadding(new Insets(14, 20, 14, 20));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(2);
        Label lblTieuDe = new Label("TIẾP NHẬN DỊCH VỤ TẠI QUẦY");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Lập phiếu tiếp nhận thú cưng làm dịch vụ trực tiếp tại quầy hoặc từ lịch hẹn đã đặt.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Nội dung các phần
        VBox pnlThongTin = taoKhungThongTinPhieu();
        VBox pnlDichVu = taoKhungDichVu();
        VBox.setVgrow(pnlDichVu, Priority.ALWAYS);

        VBox pnlCuon = new VBox(10, pnlThongTin, pnlDichVu);
        pnlCuon.setFillWidth(true);
        VBox.setVgrow(pnlCuon, Priority.ALWAYS);

        ScrollPane spCuon = new ScrollPane(pnlCuon);
        spCuon.setFitToWidth(true);
        spCuon.setFitToHeight(true);
        spCuon.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spCuon.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(spCuon, Priority.ALWAYS);

        // 3. Thanh nút thao tác chân trang
        HBox pnlTacVu = taoThanhTacVu();

        getChildren().addAll(pnlTieuDe, spCuon, pnlTacVu);

        caiDatBangChiTiet();
        caiDatSuKien();
        napDuLieuBanDau();
    }

    // =========================================================================
    // 1. KHUNG THÔNG TIN PHIẾU TIẾP NHẬN
    // =========================================================================
    private VBox taoKhungThongTinPhieu() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.setStyle("-fx-background-color: #ffffff; -fx-padding: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        Label lblPhan1 = new Label("1. Thông tin tiếp nhận dịch vụ");
        lblPhan1.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(14);
        pnlLuoi.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints(130);
        ColumnConstraints col2 = new ColumnConstraints(260, 300, Double.MAX_VALUE);
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(130);
        ColumnConstraints col4 = new ColumnConstraints(260, 300, Double.MAX_VALUE);
        col4.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3, col4);

        // Hàng 0: Mã phiếu & Nhân viên tiếp nhận (Khóa cứng, lấy từ phiên)
        txtMaPhieuDichVu.setEditable(false);
        txtMaPhieuDichVu.setPromptText("Tự động sinh (PDV-yyyyMMdd-xxxxxx)");
        txtMaPhieuDichVu.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
        pnlLuoi.add(new Label("Mã phiếu:"), 0, 0);
        pnlLuoi.add(txtMaPhieuDichVu, 1, 0);

        txtNhanVienHienThi.setEditable(false);
        txtNhanVienHienThi.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
        pnlLuoi.add(new Label("Nhân viên (*):"), 2, 0);
        pnlLuoi.add(txtNhanVienHienThi, 3, 0);

        // Hàng 1: SĐT tra cứu (nhấn Enter hoặc bấm Tra cứu) & Khách hàng
        txtTimKhach.setPromptText("Nhập SĐT (10 số) rồi Enter...");
        HBox.setHgrow(txtTimKhach, Priority.ALWAYS);
        btnKiemTraSdt.setMinWidth(Region.USE_PREF_SIZE);
        btnKiemTraSdt.getStyleClass().add("nut-phu");
        btnXemLichHomNay.setMinWidth(Region.USE_PREF_SIZE);
        btnXemLichHomNay.getStyleClass().add("nut-phu");
        btnXemLichHomNay.setOnAction(e -> moCuaSoDanhSachLichHenHomNay());
        btnThemKhachNhanh.setMinWidth(Region.USE_PREF_SIZE);
        btnThemKhachNhanh.getStyleClass().add("nut-phu");
        btnThemKhachNhanh.setOnAction(e -> moCuaSoThemKhachNhanh(txtTimKhach.getText()));

        HBox boxTimKhach = new HBox(6, txtTimKhach, btnKiemTraSdt, btnXemLichHomNay, btnThemKhachNhanh);
        boxTimKhach.setAlignment(Pos.CENTER_LEFT);

        pnlLuoi.add(new Label("SĐT khách hàng (*):"), 0, 1);
        pnlLuoi.add(boxTimKhach, 1, 1);

        txtKhachHangHienThi.setEditable(false);
        txtKhachHangHienThi.setPromptText("Chưa chọn khách hàng (*)");
        txtKhachHangHienThi.setStyle("-fx-background-color: #f1f5f9; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        // Huy hiệu hiển thị lịch hẹn liên kết
        pnlBadgeLichHen.setAlignment(Pos.CENTER_LEFT);
        lblThongTinLichHen.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-padding: 2 8; -fx-background-radius: 4px; -fx-font-weight: bold; -fx-font-size: 11.5px;");
        btnHuyLienKetLich.setStyle("-fx-font-size: 11px; -fx-padding: 2 6; -fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-cursor: hand; -fx-border-radius: 3px; -fx-background-radius: 3px;");
        btnHuyLienKetLich.setOnAction(e -> huyLienKetLichHen());
        pnlBadgeLichHen.getChildren().addAll(lblThongTinLichHen, btnHuyLienKetLich);
        pnlBadgeLichHen.setVisible(false);
        pnlBadgeLichHen.setManaged(false);

        VBox boxKhachHang = new VBox(3, txtKhachHangHienThi, pnlBadgeLichHen);
        pnlLuoi.add(new Label("Khách hàng (*):"), 2, 1);
        pnlLuoi.add(boxKhachHang, 3, 1);

        // Hàng 2: Thời gian tiếp nhận (Ngày + Giờ) & Ghi chú tiếp nhận
        dpNgayTiepNhan.setValue(LocalDate.now());
        dpNgayTiepNhan.setDisable(true);
        dpNgayTiepNhan.setStyle("-fx-opacity: 0.9;");
        dpNgayTiepNhan.setPrefWidth(140);
        HBox.setHgrow(dpNgayTiepNhan, Priority.ALWAYS);

        txtGioTiepNhan.setEditable(false);
        txtGioTiepNhan.setText(LocalTime.now().format(DINH_DANG_GIO));
        txtGioTiepNhan.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
        txtGioTiepNhan.setPrefWidth(110);

        HBox boxThoiGian = new HBox(8, dpNgayTiepNhan, txtGioTiepNhan);
        boxThoiGian.setAlignment(Pos.CENTER_LEFT);

        pnlLuoi.add(new Label("Thời gian tiếp nhận:"), 0, 2);
        pnlLuoi.add(boxThoiGian, 1, 2);

        txtGhiChu.setPromptText("Ghi chú lúc tiếp nhận (tình trạng sức khỏe, dị ứng, yêu cầu...)");
        txtGhiChu.setPrefRowCount(1);
        txtGhiChu.setMaxHeight(36);
        pnlLuoi.add(new Label("Ghi chú tiếp nhận:"), 2, 2);
        pnlLuoi.add(txtGhiChu, 3, 2);

        pnlKhung.getChildren().addAll(lblPhan1, pnlLuoi);
        return pnlKhung;
    }

    // =========================================================================
    // 2. KHUNG CHỌN THÚ CƯNG & DỊCH VỤ THỰC HIỆN
    // =========================================================================
    private VBox taoKhungDichVu() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.setStyle("-fx-background-color: #ffffff; -fx-padding: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        Label lblPhan2 = new Label("2. Danh sách dịch vụ thực hiện trong phiếu");
        lblPhan2.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        // Chọn thú cưng + Nút thêm nhanh
        cboThuCung.setPromptText("Chọn thú cưng...");
        cboThuCung.setPrefWidth(180);

        btnThemThuCungNhanh.setText("+ Mới");
        btnThemThuCungNhanh.getStyleClass().add("nut-phu");
        btnThemThuCungNhanh.setMinWidth(Region.USE_PREF_SIZE);

        HBox boxThuCung = new HBox(6, cboThuCung, btnThemThuCungNhanh);
        boxThuCung.setAlignment(Pos.CENTER_LEFT);

        // Ô nhập cân nặng thực tế tại quầy
        txtCanNangThucTe.setPromptText("Cân tại quầy (kg)");
        txtCanNangThucTe.setPrefWidth(120);

        // Chọn dịch vụ
        cboDichVu.setPromptText("Chọn dịch vụ chăm sóc...");
        cboDichVu.setPrefWidth(220);
        HBox.setHgrow(cboDichVu, Priority.ALWAYS);

        // Ô đơn giá tự động tính theo bậc cân nặng (chỉ đọc, nền xám)
        txtDonGia.setPromptText("Đơn giá áp dụng");
        txtDonGia.setPrefWidth(170);
        txtDonGia.setEditable(false);
        txtDonGia.setStyle("-fx-background-color: #f1f5f9; -fx-font-weight: bold;");

        btnThemDichVu.getStyleClass().add("nut-chinh");
        btnThemDichVu.setMinWidth(Region.USE_PREF_SIZE);

        HBox pnlThem = new HBox(8,
                new Label("Thú cưng:"), boxThuCung,
                new Label("Cân nặng (*):"), txtCanNangThucTe,
                new Label("Dịch vụ:"), cboDichVu,
                new Label("Đơn giá:"), txtDonGia,
                btnThemDichVu
        );
        pnlThem.setAlignment(Pos.CENTER_LEFT);

        // Bảng chi tiết dịch vụ
        tblChiTiet.getStyleClass().add("bang-du-lieu");
        tblChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblChiTiet.setPrefHeight(200);
        tblChiTiet.setPlaceholder(new Label("Chưa có dịch vụ nào được thêm vào phiếu"));
        VBox.setVgrow(tblChiTiet, Priority.ALWAYS);

        // Thanh tổng kết tiền
        lblTongTien.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        HBox pnlTong = new HBox(lblTongTien);
        pnlTong.setAlignment(Pos.CENTER_RIGHT);

        pnlKhung.getChildren().addAll(lblPhan2, pnlThem, tblChiTiet, pnlTong);
        return pnlKhung;
    }

    // Cài đặt các cột hiển thị cho bảng dịch vụ chi tiết
    private void caiDatBangChiTiet() {
        tblChiTiet.setItems(dsChiTietChon);

        TableColumn<ChiTietPhieuDichVu, Number> colSTT = new TableColumn<>("STT");
        colSTT.setStyle("-fx-alignment: CENTER;");
        colSTT.setPrefWidth(50);
        colSTT.setMaxWidth(60);
        colSTT.setCellValueFactory(c -> new SimpleIntegerProperty(dsChiTietChon.indexOf(c.getValue()) + 1));

        TableColumn<ChiTietPhieuDichVu, String> colThuCung = new TableColumn<>("Thú cưng");
        colThuCung.setPrefWidth(180);
        colThuCung.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenThuCung()));

        TableColumn<ChiTietPhieuDichVu, String> colDichVu = new TableColumn<>("Dịch vụ chăm sóc");
        colDichVu.setPrefWidth(260);
        colDichVu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDichVu()));

        TableColumn<ChiTietPhieuDichVu, String> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDonGia.setPrefWidth(140);
        colDonGia.setCellValueFactory(c -> new SimpleStringProperty(DINH_DANG_TIEN.format(c.getValue().getDonGia())));

        TableColumn<ChiTietPhieuDichVu, Void> colXoa = new TableColumn<>("Thao tác");
        colXoa.setStyle("-fx-alignment: CENTER;");
        colXoa.setPrefWidth(80);
        colXoa.setMaxWidth(100);
        colXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");
            {
                btnXoa.getStyleClass().addAll("button", "nut-xoa");
                btnXoa.setOnAction(e -> {
                    ChiTietPhieuDichVu item = getTableView().getItems().get(getIndex());
                    dsChiTietChon.remove(item);
                    capNhatTongTien();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoa);
            }
        });

        tblChiTiet.getColumns().addAll(colSTT, colThuCung, colDichVu, colDonGia, colXoa);
    }

    private HBox taoThanhTacVu() {
        btnLuu.getStyleClass().add("nut-chinh");
        btnLamMoi.getStyleClass().add("nut-phu");

        HBox pnlTacVu = new HBox(10, btnLamMoi, btnLuu);
        pnlTacVu.setAlignment(Pos.CENTER_RIGHT);
        pnlTacVu.setPadding(new Insets(6, 0, 0, 0));
        return pnlTacVu;
    }

    // =========================================================================
    // 3. CÀI ĐẶT SỰ KIỆN & BỘ CHUYỂN ĐỔI HIỂN THỊ
    // =========================================================================
    private void caiDatSuKien() {


        // Tra cứu số điện thoại khi nhấn Enter hoặc bấm nút "Tra cứu"
        txtTimKhach.setOnAction(e -> xuLyKiemTraSoDienThoai());
        btnKiemTraSdt.setOnAction(e -> xuLyKiemTraSoDienThoai());

        // Ràng buộc nhập số điện thoại (tối đa 10 chữ số)
        txtTimKhach.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            if (!moi.matches("\\d*")) {
                txtTimKhach.setText(cu != null ? cu : "");
                return;
            }
            if (moi.length() > 10) {
                txtTimKhach.setText(moi.substring(0, 10));
            }
        });

        // Hiển thị ComboBox Thú cưng
        cboThuCung.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThuCung tc) {
                if (tc == null) return "";
                String giong = tc.getGiongLoai() != null ? tc.getGiongLoai() : "";
                String can = tc.getCanNang() > 0 ? " - " + tc.getCanNang() + "kg" : "";
                return tc.getTenThuCung() + " (" + giong + can + ")";
            }

            @Override
            public ThuCung fromString(String string) {
                return null;
            }
        });

        // Khi chọn thú cưng: điền cân nặng hiện có, kiểm tra dị ứng, tính giá tự động
        cboThuCung.setOnAction(e -> {
            ThuCung tc = cboThuCung.getValue();
            if (tc != null) {
                if (tc.getCanNang() > 0) {
                    txtCanNangThucTe.setText(String.valueOf(tc.getCanNang()));
                } else {
                    txtCanNangThucTe.clear();
                }
                kiemTraVaChenLuuYDiUng(tc);
            } else {
                txtCanNangThucTe.clear();
            }
            tinhGiaTuDong();
        });

        // Nút thêm thú cưng nhanh tại quầy
        btnThemThuCungNhanh.setOnAction(e -> moCuaSoThemThuCungNhanh());

        // Ràng buộc số cho ô Cân nặng thực tế & Tự động áp giá khi nhân viên cân lại tại quầy
        txtCanNangThucTe.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d{0,2})?")) {
                txtCanNangThucTe.setText(cu != null ? cu : "");
                return;
            }
            tinhLaiGiaTheoCanNangThucTe();
        });

        // Hiển thị ComboBox Dịch vụ
        cboDichVu.setConverter(new StringConverter<>() {
            @Override
            public String toString(DichVuChamSoc dv) {
                if (dv == null) return "";
                String tl = dv.getThoiLuong() > 0 ? " (" + dv.getThoiLuong() + " phút)" : "";
                return dv.getTenDichVu() + tl;
            }

            @Override
            public DichVuChamSoc fromString(String string) {
                return null;
            }
        });
        cboDichVu.setOnAction(e -> tinhGiaTuDong());

        // Nút thêm dịch vụ vào bảng
        btnThemDichVu.setOnAction(e -> xuLyThemDichVu());

        // Nút Lưu
        btnLuu.setOnAction(e -> xuLyLuuPhieu());

        // Nút Làm mới
        btnLamMoi.setOnAction(e -> lamMoiForm());
    }

    // =========================================================================
    // 4. LOGIC TÍNH GIÁ TỰ ĐỘNG & CÂN THỰC TẾ TẠI QUẦY
    // =========================================================================

    private double layCanNangThucTeHienTai() {
        String txt = txtCanNangThucTe.getText() != null ? txtCanNangThucTe.getText().trim() : "";
        try {
            double val = Double.parseDouble(txt);
            if (val > 0) return val;
        } catch (NumberFormatException ignored) {}

        ThuCung tc = cboThuCung.getValue();
        if (tc != null && tc.getCanNang() > 0) {
            return tc.getCanNang();
        }
        return 0.0;
    }

    private void tinhGiaTuDong() {
        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) {
            txtDonGia.clear();
            donGiaHienHanh = 0.0;
            return;
        }

        double canNang = layCanNangThucTeHienTai();
        if (canNang <= 0) {
            txtDonGia.setText("Cần nhập cân nặng (*)");
            donGiaHienHanh = 0.0;
            return;
        }

        double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang);
        donGiaHienHanh = gia;
        String bac = BangGiaDichVu.xacDinhKichThuoc(canNang);
        txtDonGia.setText(DINH_DANG_TIEN.format(gia) + " (" + bac + ")");
    }

    private void tinhLaiGiaTheoCanNangThucTe() {
        ThuCung tc = cboThuCung.getValue();
        double canNang = layCanNangThucTeHienTai();

        tinhGiaTuDong();

        if (tc != null && canNang > 0) {
            boolean coThayDoi = false;
            for (ChiTietPhieuDichVu ct : dsChiTietChon) {
                if (tc.getMaThuCung().equals(ct.getMaThuCung())) {
                    double giaMoi = controller.traGiaDichVuTheoCanNang(ct.getMaDichVu(), canNang);
                    ct.setDonGia(giaMoi);
                    coThayDoi = true;
                }
            }
            if (coThayDoi) {
                tblChiTiet.refresh();
                capNhatTongTien();
            }

            mapCanNangDaCan.put(tc.getMaThuCung(), canNang);
        }
    }

    private void kiemTraVaChenLuuYDiUng(ThuCung tc) {
        if (tc == null || tc.getDiUng() == null || tc.getDiUng().isBlank() || "Không có".equalsIgnoreCase(tc.getDiUng().trim())) {
            return;
        }
        String tag = "[Lưu ý dị ứng: " + tc.getDiUng().trim() + "]";
        String ghiChu = txtGhiChu.getText() != null ? txtGhiChu.getText() : "";
        if (!ghiChu.contains(tag)) {
            if (ghiChu.isBlank()) {
                txtGhiChu.setText(tag);
            } else {
                txtGhiChu.setText(tag + " " + ghiChu);
            }
        }
    }

    // =========================================================================
    // 5. XỬ LÝ TRA CỨU SĐT, ĐỒNG BỘ LỊCH HẸN & HỘP THOẠI THÊM NHANH
    // =========================================================================

    private void huyLienKetLichHen() {
        this.lichHenHienTai = null;
        pnlBadgeLichHen.setVisible(false);
        pnlBadgeLichHen.setManaged(false);
        lblThongTinLichHen.setText("");
        dsChiTietChon.clear();
        capNhatTongTien();
        txtGhiChu.clear();
        ThuCung tc = cboThuCung.getValue();
        if (tc != null) kiemTraVaChenLuuYDiUng(tc);
    }

    private void dongBoTuLichHen(LichHen lh) {
        if (lh == null) return;
        this.lichHenHienTai = lh;

        String tenTcBadge = (lh.getTenThuCung() != null && !lh.getTenThuCung().isBlank()) ? lh.getTenThuCung() : lh.getMaThuCung();
        String gioBadge = (lh.getGioHen() != null) ? lh.getGioHen().toString() : "";
        lblThongTinLichHen.setText("📅 Lịch: " + lh.getMaLichHen() + " (Bé " + tenTcBadge + " - " + gioBadge + ")");
        pnlBadgeLichHen.setVisible(true);
        pnlBadgeLichHen.setManaged(true);

        // Đồng bộ khách hàng
        KhachHang kh = lh.getKhachHang();
        if (kh == null && lh.getMaKhachHang() != null) {
            kh = controller.timKhachHangTheoSoDienThoai(lh.getSoDienThoai());
        }
        if (kh != null) {
            khachHangHienTai = kh;
            txtKhachHangHienThi.setText(kh.getHoTen() + " (" + kh.getSoDienThoai() + ")");
            txtTimKhach.setText(kh.getSoDienThoai());
        } else {
            txtKhachHangHienThi.setText(lh.getTenKhachHang() + " (" + lh.getSoDienThoai() + ")");
        }

        // Nạp thú cưng của lịch hẹn
        if (khachHangHienTai != null) {
            List<ThuCung> dsTc = controller.layDanhSachThuCungTheoKhachHang(khachHangHienTai.getMaKhachHang());
            cboThuCung.setItems(FXCollections.observableArrayList(dsTc));
            ThuCung tcKhop = dsTc.stream()
                    .filter(tc -> tc.getMaThuCung().equals(lh.getMaThuCung()))
                    .findFirst().orElse(null);
            if (tcKhop != null) {
                cboThuCung.setValue(tcKhop);
                if (tcKhop.getCanNang() > 0) {
                    txtCanNangThucTe.setText(String.valueOf(tcKhop.getCanNang()));
                } else {
                    txtCanNangThucTe.clear();
                }
                kiemTraVaChenLuuYDiUng(tcKhop);
            }
        }

        // Nạp chi tiết dịch vụ từ lịch hẹn
        dsChiTietChon.clear();
        double canNang = layCanNangThucTeHienTai();
        List<ChiTietLichHen> dsCtLh = controller.layChiTietLichHen(lh.getMaLichHen());
        String tenTc = (lh.getTenThuCung() != null && !lh.getTenThuCung().isBlank())
                ? lh.getTenThuCung()
                : (cboThuCung.getValue() != null ? cboThuCung.getValue().getTenThuCung() : lh.getMaThuCung());

        for (ChiTietLichHen ctLh : dsCtLh) {
            if (canNang <= 0 && ctLh.getCanNangThuCung() > 0) {
                canNang = ctLh.getCanNangThuCung();
                txtCanNangThucTe.setText(String.valueOf(canNang));
            }

            double donGiaThucTe = controller.traGiaDichVuTheoCanNang(ctLh.getMaDichVu(), canNang > 0 ? canNang : 1.0);
            double giaApDung = (donGiaThucTe > 0) ? donGiaThucTe : ctLh.getDonGia();

            ChiTietPhieuDichVu ctPdv = new ChiTietPhieuDichVu(
                    txtMaPhieuDichVu.getText(),
                    lh.getMaThuCung(),
                    tenTc,
                    ctLh.getMaDichVu(),
                    ctLh.getTenDichVu(),
                    giaApDung
            );
            dsChiTietChon.add(ctPdv);
        }

        String ghiChuLh = "Tiếp nhận theo lịch hẹn " + lh.getMaLichHen() + (lh.getGhiChu() != null && !lh.getGhiChu().isBlank() ? " | " + lh.getGhiChu() : "");
        ThuCung tcHienTai = cboThuCung.getValue();
        if (tcHienTai != null && tcHienTai.getDiUng() != null && !tcHienTai.getDiUng().isBlank() && !"Không có".equalsIgnoreCase(tcHienTai.getDiUng().trim())) {
            ghiChuLh = "[Lưu ý dị ứng: " + tcHienTai.getDiUng().trim() + "] " + ghiChuLh;
        }
        txtGhiChu.setText(ghiChuLh);

        capNhatTongTien();
        tinhGiaTuDong();
    }

    private void xuLyKiemTraSoDienThoai() {
        String sdt = txtTimKhach.getText() != null ? txtTimKhach.getText().trim() : "";
        if (sdt.isEmpty()) {
            hienThiCanhBao("Chưa nhập số điện thoại", "Vui lòng nhập số điện thoại khách hàng cần tra cứu!");
            txtTimKhach.requestFocus();
            return;
        }

        if (!sdt.matches("^0\\d{9}$")) {
            hienThiCanhBao("SĐT không hợp lệ", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0 (Ví dụ: 0912345678)!");
            txtTimKhach.requestFocus();
            return;
        }

        try {
            KhachHang khKhop = controller.timKhachHangTheoSoDienThoai(sdt);
            if (khKhop != null) {
                khachHangHienTai = khKhop;
                txtKhachHangHienThi.setText(khKhop.getHoTen() + " (" + khKhop.getSoDienThoai() + ")");
                txtTimKhach.setText(khKhop.getSoDienThoai());

                // Nạp thú cưng của khách
                List<ThuCung> dsTc = controller.layDanhSachThuCungTheoKhachHang(khKhop.getMaKhachHang());
                cboThuCung.setItems(FXCollections.observableArrayList(dsTc));
                if (!dsTc.isEmpty()) {
                    cboThuCung.setValue(dsTc.get(0));
                } else {
                    cboThuCung.setValue(null);
                    txtCanNangThucTe.clear();
                }

                // Kiểm tra xem khách có lịch hẹn chờ tiếp nhận hôm nay không
                List<LichHen> dsLh = controller.layDanhSachLichHenChoTheoKhachHang(khKhop.getMaKhachHang());
                if (dsLh.isEmpty()) {
                    huyLienKetLichHen();
                    if (!dsTc.isEmpty()) {
                        cboThuCung.setValue(dsTc.get(0));
                    } else {
                        cboThuCung.setValue(null);
                        txtCanNangThucTe.clear();
                    }
                } else if (dsLh.size() == 1) {
                    dongBoTuLichHen(dsLh.get(0));
                } else {
                    moCuaSoChonLichHenCuaKhach(dsLh);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Khách hàng mới");
                alert.setHeaderText("Khách hàng chưa có trong hệ thống!");
                alert.setContentText("Số điện thoại '" + sdt + "' chưa từng sử dụng dịch vụ tại cửa hàng.\nHệ thống sẽ mở hộp thoại thêm nhanh khách hàng mới.");
                alert.showAndWait();
                moCuaSoThemKhachNhanh(sdt);
            }
        } catch (IllegalArgumentException ex) {
            hienThiCanhBao("Lỗi tra cứu", ex.getMessage());
        } catch (Exception ex) {
            hienThiLoi("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // Hộp thoại chọn lịch hẹn khi một khách hàng có nhiều lịch hẹn trong ngày (ví dụ 2 bé trở lên)
    private void moCuaSoChonLichHenCuaKhach(List<LichHen> dsLh) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlg.initOwner(getScene().getWindow());
        }
        dlg.setTitle("Chọn lịch hẹn tiếp nhận - " + (khachHangHienTai != null ? khachHangHienTai.getHoTen() : ""));

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.setStyle("-fx-background-color: #f8fafc;");

        Label lblTieuDe = new Label("KHÁCH HÀNG CÓ " + dsLh.size() + " LỊCH HẸN HÔM NAY");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0369a1;");

        Label lblMoTa = new Label("Khách hàng đang có nhiều lịch hẹn. Vui lòng chọn bé thú cưng cần tiếp nhận cho phiếu này:");
        lblMoTa.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #475569;");

        VBox pnlDanhSach = new VBox(10);
        for (LichHen lh : dsLh) {
            HBox card = new HBox(12);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(10, 14, 10, 14));
            card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; -fx-background-radius: 6px;");

            VBox info = new VBox(4);
            HBox.setHgrow(info, Priority.ALWAYS);

            String tenTc = lh.getTenThuCung() != null ? lh.getTenThuCung() : "Thú cưng";
            String gio = lh.getGioHen() != null ? lh.getGioHen().toString() : "";
            Label lblTen = new Label("🐾 Bé " + tenTc + "   |   Giờ hẹn: " + gio + " (" + lh.getMaLichHen() + ")");
            lblTen.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

            List<ChiTietLichHen> dsCt = controller.layChiTietLichHen(lh.getMaLichHen());
            StringBuilder sbDv = new StringBuilder("Dịch vụ hẹn: ");
            for (int i = 0; i < dsCt.size(); i++) {
                sbDv.append(dsCt.get(i).getTenDichVu());
                if (i < dsCt.size() - 1) sbDv.append(", ");
            }
            Label lblDichVu = new Label(sbDv.toString());
            lblDichVu.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

            info.getChildren().addAll(lblTen, lblDichVu);

            Button btnChon = new Button("Tiếp nhận bé này");
            btnChon.getStyleClass().add("nut-chinh");
            btnChon.setStyle("-fx-padding: 6 14; -fx-font-weight: bold; -fx-font-size: 12px;");
            btnChon.setOnAction(e -> {
                dlg.close();
                dongBoTuLichHen(lh);
            });

            card.getChildren().addAll(info, btnChon);
            pnlDanhSach.getChildren().add(card);
        }

        ScrollPane spCuon = new ScrollPane(pnlDanhSach);
        spCuon.setFitToWidth(true);
        spCuon.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        spCuon.setMaxHeight(300);

        Button btnKhachVangLai = new Button("Tiếp nhận trực tiếp (không theo lịch)");
        btnKhachVangLai.getStyleClass().add("nut-phu");
        btnKhachVangLai.setOnAction(e -> {
            dlg.close();
            huyLienKetLichHen();
            List<ThuCung> dsTc = controller.layDanhSachThuCungTheoKhachHang(khachHangHienTai.getMaKhachHang());
            if (!dsTc.isEmpty()) cboThuCung.setValue(dsTc.get(0));
        });

        HBox pnlNut = new HBox(btnKhachVangLai);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        pnlGoc.getChildren().addAll(lblTieuDe, lblMoTa, spCuon, pnlNut);

        Scene sc = new Scene(pnlGoc, 580, Math.min(460, 160 + dsLh.size() * 80));
        if (LapPhieuTiepNhanDichVuView.class.getResource(CSS_CHUNG) != null) {
            sc.getStylesheets().add(LapPhieuTiepNhanDichVuView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        dlg.setScene(sc);
        dlg.showAndWait();
    }

    // Hộp thoại xem toàn bộ danh sách lịch hẹn hôm nay đang chờ tiếp nhận (thay thế cho ComboBox 200 dòng)
    private void moCuaSoDanhSachLichHenHomNay() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlg.initOwner(getScene().getWindow());
        }
        dlg.setTitle("Danh sách lịch hẹn hôm nay chờ tiếp nhận");

        VBox pnlGoc = new VBox(10);
        pnlGoc.setPadding(new Insets(14));
        pnlGoc.setStyle("-fx-background-color: #f8fafc;");

        Label lblTieuDe = new Label("DANH SÁCH LỊCH HẸN HÔM NAY CHỜ TIẾP NHẬN");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 14.5px; -fx-text-fill: #0369a1;");

        HBox pnlThanhTim = new HBox(10);
        pnlThanhTim.setAlignment(Pos.CENTER_LEFT);

        TextField txtTimKiem = new TextField();
        txtTimKiem.setPromptText("🔍 Tìm theo SĐT, tên khách hàng, tên bé thú cưng, mã lịch...");
        HBox.setHgrow(txtTimKiem, Priority.ALWAYS);

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dlg.close());
        pnlThanhTim.getChildren().addAll(txtTimKiem, btnDong);

        TableView<LichHen> tbl = new TableView<>();
        tbl.getStyleClass().add("bang-du-lieu");
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tbl, Priority.ALWAYS);

        TableColumn<LichHen, String> colGio = new TableColumn<>("Giờ hẹn");
        colGio.setPrefWidth(80);
        colGio.setStyle("-fx-alignment: CENTER;");
        colGio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGioHen() != null ? c.getValue().getGioHen().toString() : ""));

        TableColumn<LichHen, String> colMa = new TableColumn<>("Mã lịch");
        colMa.setPrefWidth(135);
        colMa.setStyle("-fx-alignment: CENTER;");
        colMa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaLichHen()));

        TableColumn<LichHen, String> colKh = new TableColumn<>("Khách hàng");
        colKh.setPrefWidth(150);
        colKh.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenKhachHang()));

        TableColumn<LichHen, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setPrefWidth(110);
        colSdt.setStyle("-fx-alignment: CENTER;");
        colSdt.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSoDienThoai()));

        TableColumn<LichHen, String> colTc = new TableColumn<>("Thú cưng");
        colTc.setPrefWidth(120);
        colTc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenThuCung()));

        TableColumn<LichHen, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setPrefWidth(160);
        colGhiChu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGhiChu() != null ? c.getValue().getGhiChu() : ""));

        tbl.getColumns().addAll(List.of(colGio, colMa, colKh, colSdt, colTc, colGhiChu));

        List<LichHen> dsGoc = controller.layDanhSachLichHenChoTiepNhan();
        ObservableList<LichHen> dsHienThi = FXCollections.observableArrayList(dsGoc);
        tbl.setItems(dsHienThi);

        txtTimKiem.textProperty().addListener((obs, cu, moi) -> {
            String tuKhoa = (moi != null) ? moi.trim().toLowerCase() : "";
            if (tuKhoa.isEmpty()) {
                tbl.setItems(FXCollections.observableArrayList(dsGoc));
            } else {
                List<LichHen> loc = dsGoc.stream().filter(lh ->
                        (lh.getSoDienThoai() != null && lh.getSoDienThoai().contains(tuKhoa)) ||
                        (lh.getTenKhachHang() != null && lh.getTenKhachHang().toLowerCase().contains(tuKhoa)) ||
                        (lh.getTenThuCung() != null && lh.getTenThuCung().toLowerCase().contains(tuKhoa)) ||
                        (lh.getMaLichHen() != null && lh.getMaLichHen().toLowerCase().contains(tuKhoa))
                ).toList();
                tbl.setItems(FXCollections.observableArrayList(loc));
            }
        });

        tbl.setRowFactory(tv -> {
            TableRow<LichHen> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    LichHen lh = row.getItem();
                    dlg.close();
                    dongBoTuLichHen(lh);
                }
            });
            return row;
        });

        Button btnTiepNhan = new Button("✔ Tiếp nhận lịch hẹn được chọn");
        btnTiepNhan.getStyleClass().add("nut-chinh");
        btnTiepNhan.setOnAction(e -> {
            LichHen lh = tbl.getSelectionModel().getSelectedItem();
            if (lh == null) {
                hienThiCanhBao("Chưa chọn lịch hẹn", "Vui lòng chọn một lịch hẹn trong bảng để tiếp nhận!");
                return;
            }
            dlg.close();
            dongBoTuLichHen(lh);
        });

        HBox pnlNut = new HBox(btnTiepNhan);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Label lblHuongDan = new Label("💡 Gõ SĐT hoặc tên để tìm nhanh. Nhấp đúp chuột vào một dòng để nạp vào phiếu tiếp nhận.");
        lblHuongDan.setStyle("-fx-font-style: italic; -fx-text-fill: #64748b; -fx-font-size: 11.5px;");

        HBox pnlDay = new HBox(10, lblHuongDan, new Region(), pnlNut);
        HBox.setHgrow(pnlDay.getChildren().get(1), Priority.ALWAYS);
        pnlDay.setAlignment(Pos.CENTER_LEFT);

        pnlGoc.getChildren().addAll(lblTieuDe, pnlThanhTim, tbl, pnlDay);

        Scene sc = new Scene(pnlGoc, 820, 500);
        if (LapPhieuTiepNhanDichVuView.class.getResource(CSS_CHUNG) != null) {
            sc.getStylesheets().add(LapPhieuTiepNhanDichVuView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        dlg.setScene(sc);
        dlg.showAndWait();
    }
    private void moCuaSoThemKhachNhanh(String sdtBanDau) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) dlg.initOwner(getScene().getWindow());
        dlg.setTitle("Thêm nhanh khách hàng mới");

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("THÊM NHANH KHÁCH HÀNG MỚI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        luoi.getColumnConstraints().addAll(col1, col2);

        TextField txtHoTenMoi = new TextField();
        txtHoTenMoi.setPromptText("Họ và tên khách hàng (*)");

        TextField txtSdtMoi = new TextField();
        txtSdtMoi.setPromptText("Số điện thoại (10 số, bắt đầu bằng 0) (*)");
        if (sdtBanDau != null && !sdtBanDau.isBlank()) {
            txtSdtMoi.setText(sdtBanDau.trim());
            Platform.runLater(txtHoTenMoi::requestFocus);
        }

        TextField txtDiaChiMoi = new TextField();
        txtDiaChiMoi.setPromptText("Địa chỉ (tùy chọn)");

        txtHoTenMoi.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            String loc = moi.replaceAll("[^\\p{L}0-9\\s]", "");
            if (!loc.equals(moi)) txtHoTenMoi.setText(loc);
        });

        txtSdtMoi.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            if (!moi.matches("\\d*")) txtSdtMoi.setText(cu != null ? cu : "");
            else if (moi.length() > 10) txtSdtMoi.setText(moi.substring(0, 10));
        });

        luoi.add(new Label("Họ tên (*):"), 0, 0);
        luoi.add(txtHoTenMoi, 1, 0);
        luoi.add(new Label("Số điện thoại (*):"), 0, 1);
        luoi.add(txtSdtMoi, 1, 1);
        luoi.add(new Label("Địa chỉ:"), 0, 2);
        luoi.add(txtDiaChiMoi, 1, 2);

        Button btnLuuKhach = new Button("Lưu khách hàng");
        btnLuuKhach.getStyleClass().add("nut-chinh");
        Button btnHuyDlg = new Button("Hủy");
        btnHuyDlg.getStyleClass().add("nut-phu");
        btnHuyDlg.setOnAction(e -> dlg.close());

        btnLuuKhach.setOnAction(e -> {
            String ten = txtHoTenMoi.getText() != null ? txtHoTenMoi.getText().trim() : "";
            String sdt = txtSdtMoi.getText() != null ? txtSdtMoi.getText().trim() : "";
            if (ten.isEmpty()) {
                hienThiCanhBao("Thiếu thông tin", "Họ tên khách hàng không được để trống!");
                return;
            }
            if (!ten.matches("^[\\p{L}0-9\\s]+$")) {
                hienThiCanhBao("Họ tên không hợp lệ", "Họ tên khách hàng không được chứa ký tự đặc biệt!");
                txtHoTenMoi.requestFocus();
                return;
            }
            if (!sdt.matches("^0[0-9]{9}$")) {
                hienThiCanhBao("SĐT không hợp lệ", "Số điện thoại phải đúng 10 chữ số bắt đầu bằng 0!");
                return;
            }
            if (controller.kiemTraTrungSoDienThoai(sdt)) {
                hienThiCanhBao("Trùng số điện thoại", "Số điện thoại này đã được sử dụng trong hệ thống!");
                return;
            }

            try {
                KhachHang khMoi = new KhachHang(
                        controller.taoMaKhachHang(),
                        ten,
                        sdt,
                        null,
                        0,
                        txtDiaChiMoi.getText() != null ? txtDiaChiMoi.getText().trim() : "",
                        "Tiếp nhận tại quầy",
                        true
                );

                if (controller.themKhachHang(khMoi)) {
                    khachHangHienTai = khMoi;
                    txtKhachHangHienThi.setText(khMoi.getHoTen() + " (" + khMoi.getSoDienThoai() + ")");
                    txtTimKhach.setText(sdt);

                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm nhanh khách hàng '" + ten + "' (" + sdt + ")!");

                    Platform.runLater(() -> {
                        Alert hoi = new Alert(Alert.AlertType.CONFIRMATION,
                                "Khách hàng '" + ten + "' chưa có hồ sơ thú cưng. Bạn có muốn thêm nhanh thú cưng ngay bây giờ không?",
                                new ButtonType("Thêm thú cưng", ButtonBar.ButtonData.YES),
                                new ButtonType("Để sau", ButtonBar.ButtonData.NO));
                        hoi.setTitle("Thêm thú cưng");
                        hoi.setHeaderText(null);
                        hoi.showAndWait().ifPresent(c -> {
                            if (c.getButtonData() == ButtonBar.ButtonData.YES) {
                                moCuaSoThemThuCungNhanh();
                            }
                        });
                    });
                }
            } catch (Exception ex) {
                hienThiCanhBao("Lỗi thêm khách hàng", ex.getMessage());
            }
        });

        HBox pnlNutDlg = new HBox(8, btnLuuKhach, btnHuyDlg);
        pnlNutDlg.setAlignment(Pos.CENTER_RIGHT);
        pnlGoc.getChildren().addAll(lblTieuDe, luoi, pnlNutDlg);

        dlg.setMinWidth(480);
        dlg.setMinHeight(260);
        Scene scene = new Scene(pnlGoc, 500, 260);
        if (LapPhieuTiepNhanDichVuView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(LapPhieuTiepNhanDichVuView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    private void moCuaSoThemThuCungNhanh() {
        if (khachHangHienTai == null) {
            hienThiCanhBao("Chưa chọn khách hàng", "Vui lòng tra cứu hoặc thêm khách hàng trước khi tạo thú cưng!");
            return;
        }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) dlg.initOwner(getScene().getWindow());
        dlg.setTitle("Thêm nhanh thú cưng - " + khachHangHienTai.getHoTen());

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("THÊM NHANH HỒ SƠ THÚ CƯNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints(140);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        luoi.getColumnConstraints().addAll(col1, col2);

        TextField txtMaThuCung = new TextField();
        try {
            txtMaThuCung.setText(controller.taoMaThuCungMoi());
        } catch (Exception e) {
            txtMaThuCung.setText("TC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-000001");
        }
        txtMaThuCung.setEditable(false);
        txtMaThuCung.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");

        TextField txtChuNuoi = new TextField(khachHangHienTai.getHoTen() + " (" + khachHangHienTai.getSoDienThoai() + ")");
        txtChuNuoi.setEditable(false);
        txtChuNuoi.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");

        TextField txtTenThuCung = new TextField();
        txtTenThuCung.setPromptText("Tên thú cưng (VD: Mochi, Bống, Lu, Corgi...) (*)");
        txtTenThuCung.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            String loc = moi.replaceAll("[^\\p{L}0-9\\s]", "");
            if (!loc.equals(moi)) txtTenThuCung.setText(loc);
        });

        ComboBox<String> cboLoai = new ComboBox<>();
        cboLoai.getItems().addAll("Chó", "Mèo", "Thỏ", "Hamster", "Chim", "Khác");
        cboLoai.setValue("Chó");
        cboLoai.setMaxWidth(Double.MAX_VALUE);

        TextField txtGiongLoai = new TextField();
        txtGiongLoai.setPromptText("VD: Poodle Tiny, Mèo Anh lông ngắn, Corgi...");
        txtGiongLoai.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            String loc = moi.replaceAll("[^\\p{L}0-9\\s]", "");
            if (!loc.equals(moi)) txtGiongLoai.setText(loc);
        });

        ComboBox<String> cboGioiTinh = new ComboBox<>();
        cboGioiTinh.getItems().addAll("Đực", "Cái");
        cboGioiTinh.setValue("Đực");
        cboGioiTinh.setMaxWidth(Double.MAX_VALUE);

        TextField txtCanNangMoi = new TextField();
        txtCanNangMoi.setPromptText("Cân nặng thực tế (kg) (*)");
        if (txtCanNangThucTe.getText() != null && !txtCanNangThucTe.getText().isBlank()) {
            txtCanNangMoi.setText(txtCanNangThucTe.getText().trim());
        }
        txtCanNangMoi.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d{0,2})?")) {
                txtCanNangMoi.setText(cu != null ? cu : "");
            }
        });

        DatePicker dpNgaySinhTc = new DatePicker();
        dpNgaySinhTc.setMaxWidth(Double.MAX_VALUE);
        dpNgaySinhTc.setPromptText("dd/MM/yyyy (tùy chọn)");

        TextArea txtDiUngTc = new TextArea();
        txtDiUngTc.setPromptText("Ghi chú dị ứng thức ăn/sữa tắm, tính cách thú cưng...");
        txtDiUngTc.setPrefRowCount(2);

        luoi.add(new Label("Mã thú cưng:"), 0, 0); luoi.add(txtMaThuCung, 1, 0);
        luoi.add(new Label("Chủ nuôi:"), 0, 1); luoi.add(txtChuNuoi, 1, 1);
        luoi.add(new Label("Tên thú cưng (*):"), 0, 2); luoi.add(txtTenThuCung, 1, 2);
        luoi.add(new Label("Loài thú cưng:"), 0, 3); luoi.add(cboLoai, 1, 3);
        luoi.add(new Label("Giống loài:"), 0, 4); luoi.add(txtGiongLoai, 1, 4);
        luoi.add(new Label("Giới tính:"), 0, 5); luoi.add(cboGioiTinh, 1, 5);
        luoi.add(new Label("Cân nặng (kg) (*):"), 0, 6); luoi.add(txtCanNangMoi, 1, 6);
        luoi.add(new Label("Ngày sinh:"), 0, 7); luoi.add(dpNgaySinhTc, 1, 7);
        luoi.add(new Label("Dị ứng / Lưu ý:"), 0, 8); luoi.add(txtDiUngTc, 1, 8);

        Button btnLuuTc = new Button("Lưu hồ sơ");
        btnLuuTc.getStyleClass().add("nut-chinh");
        Button btnHuyTc = new Button("Hủy");
        btnHuyTc.getStyleClass().add("nut-phu");
        btnHuyTc.setOnAction(e -> dlg.close());

        btnLuuTc.setOnAction(e -> {
            String ten = txtTenThuCung.getText() != null ? txtTenThuCung.getText().trim() : "";
            if (ten.isEmpty()) {
                hienThiCanhBao("Thiếu thông tin", "Tên thú cưng không được để trống!");
                return;
            }
            if (!ten.matches("^[\\p{L}0-9\\s]+$")) {
                hienThiCanhBao("Tên không hợp lệ", "Tên thú cưng không được chứa ký tự đặc biệt!");
                txtTenThuCung.requestFocus();
                return;
            }

            String cnStr = txtCanNangMoi.getText() != null ? txtCanNangMoi.getText().trim() : "";
            if (cnStr.isEmpty()) {
                hienThiCanhBao("Thiếu thông tin", "Cân nặng của thú cưng là bắt buộc! Vui lòng nhập cân nặng.");
                txtCanNangMoi.requestFocus();
                return;
            }

            double canNangVal;
            try {
                canNangVal = Double.parseDouble(cnStr);
                if (canNangVal <= 0) throw new NumberFormatException();
            } catch (Exception ex) {
                hienThiCanhBao("Cân nặng không hợp lệ", "Cân nặng thú cưng phải là số lớn hơn 0!");
                txtCanNangMoi.requestFocus();
                return;
            }

            try {
                ThuCung tc = new ThuCung(
                        txtMaThuCung.getText().trim(),
                        khachHangHienTai.getMaKhachHang(),
                        ten,
                        cboLoai.getValue(),
                        txtGiongLoai.getText() != null ? txtGiongLoai.getText().trim() : "",
                        canNangVal,
                        dpNgaySinhTc.getValue(),
                        txtDiUngTc.getText() != null ? txtDiUngTc.getText().trim() : "",
                        cboGioiTinh.getValue()
                );
                tc.setKhachHang(khachHangHienTai);

                if (controller.themThuCung(tc)) {
                    List<ThuCung> dsMoi = controller.layDanhSachThuCungTheoKhachHang(khachHangHienTai.getMaKhachHang());
                    cboThuCung.setItems(FXCollections.observableArrayList(dsMoi));
                    cboThuCung.setValue(tc);
                    txtCanNangThucTe.setText(String.valueOf(canNangVal));

                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm thú cưng '" + ten + "' cho khách hàng " + khachHangHienTai.getHoTen() + "!");
                    kiemTraVaChenLuuYDiUng(tc);
                }
            } catch (Exception ex) {
                hienThiCanhBao("Lỗi thêm thú cưng", ex.getMessage());
            }
        });

        HBox pnlNutDlg = new HBox(8, btnLuuTc, btnHuyTc);
        pnlNutDlg.setAlignment(Pos.CENTER_RIGHT);
        ScrollPane spCuon = new ScrollPane(luoi);
        spCuon.setFitToWidth(true);
        spCuon.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spCuon.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        spCuon.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(spCuon, Priority.ALWAYS);

        pnlGoc.getChildren().addAll(lblTieuDe, spCuon, pnlNutDlg);

        dlg.setMinWidth(560);
        dlg.setMinHeight(580);
        Scene scene = new Scene(pnlGoc, 560, 600);
        if (LapPhieuTiepNhanDichVuView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(LapPhieuTiepNhanDichVuView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    // =========================================================================
    // 6. THAO TÁC THÊM DÒNG DỊCH VỤ & LƯU PHIẾU
    // =========================================================================

    private void xuLyThemDichVu() {
        if (khachHangHienTai == null) {
            hienThiCanhBao("Chưa chọn khách hàng", "Vui lòng tra cứu hoặc thêm khách hàng trước khi thêm dịch vụ!");
            return;
        }
        ThuCung tc = cboThuCung.getValue();
        if (tc == null) {
            hienThiCanhBao("Chưa chọn thú cưng", "Vui lòng chọn thú cưng cần làm dịch vụ!");
            return;
        }

        String strCanNang = txtCanNangThucTe.getText() != null ? txtCanNangThucTe.getText().trim() : "";
        if (strCanNang.isEmpty()) {
            hienThiCanhBao("Chưa nhập cân nặng", "Cân nặng thực tế tại quầy là bắt buộc! Vui lòng cân thú cưng và nhập cân nặng.");
            txtCanNangThucTe.requestFocus();
            return;
        }

        double canNang;
        try {
            canNang = Double.parseDouble(strCanNang);
            if (canNang <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            hienThiCanhBao("Cân nặng không hợp lệ", "Cân nặng thú cưng phải là một số lớn hơn 0 kg!");
            txtCanNangThucTe.requestFocus();
            return;
        }

        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) {
            hienThiCanhBao("Chưa chọn dịch vụ", "Vui lòng chọn dịch vụ chăm sóc!");
            return;
        }

        if (donGiaHienHanh <= 0) {
            double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang);
            if (gia > 0) {
                donGiaHienHanh = gia;
            } else {
                hienThiCanhBao("Đơn giá không hợp lệ", "Chưa xác định được đơn giá hợp lệ cho dịch vụ!");
                return;
            }
        }

        for (ChiTietPhieuDichVu ct : dsChiTietChon) {
            if (ct.getMaThuCung().equals(tc.getMaThuCung()) && ct.getMaDichVu().equals(dv.getMaDichVu())) {
                hienThiCanhBao("Trùng dịch vụ", "Dịch vụ '" + dv.getTenDichVu() + "' đã được thêm cho bé " + tc.getTenThuCung() + "!");
                return;
            }
        }

        ChiTietPhieuDichVu ct = new ChiTietPhieuDichVu(
                txtMaPhieuDichVu.getText(),
                tc.getMaThuCung(),
                tc.getTenThuCung(),
                dv.getMaDichVu(),
                dv.getTenDichVu(),
                donGiaHienHanh
        );
        dsChiTietChon.add(ct);
        capNhatTongTien();
    }

    private void capNhatTongTien() {
        double tong = 0.0;
        for (ChiTietPhieuDichVu ct : dsChiTietChon) {
            tong += ct.getDonGia();
        }
        lblTongTien.setText("Tổng tiền dự tính: " + DINH_DANG_TIEN.format(tong));
    }

    private void xuLyLuuPhieu() {
        try {
            if (khachHangHienTai == null) {
                hienThiCanhBao("Thiếu thông tin", "Vui lòng tra cứu hoặc thêm khách hàng tiếp nhận!");
                return;
            }

            if (nhanVienHienTai == null) {
                hienThiCanhBao("Thiếu thông tin", "Không xác định được nhân viên tiếp nhận từ phiên đăng nhập!");
                return;
            }

            String strCanNang = txtCanNangThucTe.getText() != null ? txtCanNangThucTe.getText().trim() : "";
            if (strCanNang.isEmpty()) {
                hienThiCanhBao("Chưa nhập cân nặng", "Cân nặng thực tế của thú cưng tại quầy là bắt buộc! Vui lòng cân thú cưng và nhập cân nặng.");
                txtCanNangThucTe.requestFocus();
                return;
            }
            try {
                double cn = Double.parseDouble(strCanNang);
                if (cn <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                hienThiCanhBao("Cân nặng không hợp lệ", "Cân nặng thú cưng phải là một số lớn hơn 0 kg!");
                txtCanNangThucTe.requestFocus();
                return;
            }

            if (dsChiTietChon.isEmpty()) {
                hienThiCanhBao("Chưa có dịch vụ", "Vui lòng thêm ít nhất một dịch vụ chăm sóc vào phiếu tiếp nhận!");
                return;
            }

            LocalDate ngay = dpNgayTiepNhan.getValue() != null ? dpNgayTiepNhan.getValue() : LocalDate.now();
            String gioStr = txtGioTiepNhan.getText() != null ? txtGioTiepNhan.getText().trim() : "";
            LocalTime gio;
            try {
                gio = LocalTime.parse(gioStr);
            } catch (Exception ex) {
                gio = LocalTime.now();
            }

            LichHen lh = lichHenHienTai;
            String maLichHen = (lh != null) ? lh.getMaLichHen() : null;

            PhieuDichVu phieu = new PhieuDichVu(
                    txtMaPhieuDichVu.getText().trim(),
                    khachHangHienTai,
                    nhanVienHienTai,
                    lh,
                    ngay,
                    gio,
                    "Đang chờ",
                    txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : ""
            );

            List<ChiTietPhieuDichVu> danhSachChiTiet = new ArrayList<>(dsChiTietChon);
            boolean thanhCong = controller.taoPhieuDichVu(phieu, danhSachChiTiet);
            if (thanhCong) {
                ThuCung tcHienTai = cboThuCung.getValue();
                double cnThucTe = layCanNangThucTeHienTai();
                if (tcHienTai != null && cnThucTe > 0) {
                    mapCanNangDaCan.put(tcHienTai.getMaThuCung(), cnThucTe);
                }

                for (Map.Entry<String, Double> entry : mapCanNangDaCan.entrySet()) {
                    if (entry.getValue() != null && entry.getValue() > 0) {
                        try {
                            controller.capNhatCanNangThuCung(entry.getKey(), entry.getValue());
                        } catch (Exception ex) {
                            System.err.println("Không thể cập nhật cân nặng cho thú cưng " + entry.getKey() + ": " + ex.getMessage());
                        }
                    }
                }

                hienThiThongBao("Tiếp nhận thành công", "Đã tạo phiếu tiếp nhận dịch vụ mã: " + phieu.getMaPhieuDichVu() + "\nTrạng thái: Đang chờ thực hiện");
                if (khiLuuThanhCong != null) {
                    khiLuuThanhCong.run();
                }
                lamMoiForm();
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiCanhBao("Không thể lập phiếu", ex.getMessage());
        } catch (Exception ex) {
            hienThiLoi("Lỗi hệ thống: " + ex.getMessage());
        }
    }

    // =========================================================================
    // 7. KHỞI TẠO & LÀM MỚI DỮ LIỆU
    // =========================================================================

    private void napDuLieuBanDau() {
        lamMoiForm();
        try {
            List<DichVuChamSoc> dsDv = controller.layDanhSachDichVu();
            cboDichVu.setItems(FXCollections.observableArrayList(dsDv));
            if (!dsDv.isEmpty()) {
                cboDichVu.setValue(dsDv.get(0));
            }
        } catch (Exception ex) {
            System.err.println("Lỗi khi nạp dữ liệu ban đầu cho phiếu dịch vụ: " + ex.getMessage());
        }
    }



    private void lamMoiForm() {
        try {
            txtMaPhieuDichVu.setText(controller.taoMaPhieuDichVuMoi());
        } catch (Exception ex) {
            txtMaPhieuDichVu.setText("");
        }

        // Tự động gán nhân viên phụ trách từ phiên đăng nhập
        TaiKhoan tk = SessionManager.getTaiKhoanHienTai();
        if (tk != null && tk.getNhanVien() != null) {
            nhanVienHienTai = tk.getNhanVien();
            txtNhanVienHienThi.setText(nhanVienHienTai.getHoTen() + " (" + nhanVienHienTai.getMaNhanVien() + ")");
        } else {
            try {
                List<String[]> dsNv = controller.layDanhSachNhanVien();
                if (!dsNv.isEmpty()) {
                    nhanVienHienTai = new NhanVien();
                    nhanVienHienTai.setMaNhanVien(dsNv.get(0)[0]);
                    nhanVienHienTai.setHoTen(dsNv.get(0)[1]);
                    txtNhanVienHienThi.setText(nhanVienHienTai.getHoTen() + " (" + nhanVienHienTai.getMaNhanVien() + ")");
                }
            } catch (Exception ignored) {}
        }

        huyLienKetLichHen();
        khachHangHienTai = null;
        txtKhachHangHienThi.setText("Chưa chọn khách hàng (*)");
        txtTimKhach.clear();
        txtTimKhach.setDisable(false);
        btnKiemTraSdt.setDisable(false);
        btnThemKhachNhanh.setDisable(false);

        cboThuCung.getItems().clear();
        cboThuCung.setValue(null);
        txtCanNangThucTe.clear();
        txtDonGia.clear();
        donGiaHienHanh = 0.0;

        dpNgayTiepNhan.setValue(LocalDate.now());
        txtGioTiepNhan.setText(LocalTime.now().format(DINH_DANG_GIO));
        txtGhiChu.clear();
        dsChiTietChon.clear();
        mapCanNangDaCan.clear();

        capNhatTongTien();
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(tieuDe);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(tieuDe);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
