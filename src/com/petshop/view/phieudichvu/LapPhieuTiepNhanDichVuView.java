package com.petshop.view.phieudichvu;

import com.petshop.controller.PhieuDichVuController;
import com.petshop.model.*;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Giao diện Lập phiếu tiếp nhận dịch vụ chăm sóc tại quầy (PDV_TAO).
 * Hỗ trợ tiếp nhận khách vãng lai, tra cứu SĐT, thêm nhanh khách hàng/thú cưng,
 * và tiếp nhận chuyển giao liền mạch từ Lịch hẹn đặt trước.
 */
public class LapPhieuTiepNhanDichVuView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PhieuDichVuController controller = new PhieuDichVuController();
    private final Runnable khiLuuThanhCong;

    // Danh sách lưu trữ dữ liệu
    private final List<KhachHang> dsKhachHangGoc = new ArrayList<>();
    private final ObservableList<ChiTietPhieuDichVu> dsChiTietChon = FXCollections.observableArrayList();
    private final Map<String, BigDecimal> mapCanNangDaCan = new HashMap<>();

    private BigDecimal donGiaHienHanh = BigDecimal.ZERO;
    private boolean dangDongBo = false;

    // Điều khiển chọn nguồn tiếp nhận
    private final ComboBox<LichHen> cboLichHen = new ComboBox<>();

    // Điều khiển thông tin phiếu (Phần 1)
    private final TextField txtMaPhieuDichVu = new TextField();
    private final TextField txtTimKhach = new TextField();
    private final Button btnKiemTraSdt = new Button("Kiểm tra");
    private final ComboBox<KhachHang> cboKhachHang = new ComboBox<>();
    private final ComboBox<String[]> cboNhanVien = new ComboBox<>();
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
        Label lblMoTa = new Label("Lập phiếu tiếp nhận thú cưng làm dịch vụ trực tiếp hoặc tiếp nhận từ lịch hẹn đã đặt.");
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

        // Dòng chọn tiếp nhận từ lịch hẹn
        cboLichHen.setPromptText("-- Chọn lịch hẹn đến giờ tiếp nhận (nếu có) --");
        cboLichHen.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cboLichHen, Priority.ALWAYS);

        Button btnBoChonLich = new Button("Tiếp nhận trực tiếp");
        btnBoChonLich.getStyleClass().add("nut-phu");
        btnBoChonLich.setOnAction(e -> {
            cboLichHen.setValue(null);
            cboKhachHang.setDisable(false);
            txtTimKhach.setDisable(false);
            btnKiemTraSdt.setDisable(false);
            dsChiTietChon.clear();
            capNhatTongTien();
            txtGhiChu.clear();
            ThuCung tc = cboThuCung.getValue();
            if (tc != null) kiemTraVaChenLuuYDiUng(tc);
        });

        HBox pnlLichHen = new HBox(8, new Label("Nguồn tiếp nhận:"), cboLichHen, btnBoChonLich);
        pnlLichHen.setAlignment(Pos.CENTER_LEFT);

        // Lưới thông tin chi tiết (3 hàng gọn gàng, không tràn màn hình)
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

        // Hàng 0: Mã phiếu & Nhân viên tiếp nhận
        txtMaPhieuDichVu.setEditable(false);
        txtMaPhieuDichVu.setPromptText("Tự động sinh (PDV-yyyyMMdd-xxxxxx)");
        txtMaPhieuDichVu.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
        pnlLuoi.add(new Label("Mã phiếu:"), 0, 0);
        pnlLuoi.add(txtMaPhieuDichVu, 1, 0);

        cboNhanVien.setMaxWidth(Double.MAX_VALUE);
        cboNhanVien.setPromptText("Chọn nhân viên tiếp nhận...");
        pnlLuoi.add(new Label("Nhân viên (*):"), 2, 0);
        pnlLuoi.add(cboNhanVien, 3, 0);

        // Hàng 1: SĐT tra cứu (nhấn Enter hoặc bấm Kiểm tra) & Khách hàng
        txtTimKhach.setPromptText("Nhập SĐT (10 số) rồi Enter...");
        HBox.setHgrow(txtTimKhach, Priority.ALWAYS);
        btnKiemTraSdt.setMinWidth(Region.USE_PREF_SIZE);
        btnKiemTraSdt.getStyleClass().add("nut-phu");
        HBox boxTimKhach = new HBox(6, txtTimKhach, btnKiemTraSdt);
        boxTimKhach.setAlignment(Pos.CENTER_LEFT);

        pnlLuoi.add(new Label("SĐT khách hàng (*):"), 0, 1);
        pnlLuoi.add(boxTimKhach, 1, 1);

        cboKhachHang.setMaxWidth(Double.MAX_VALUE);
        cboKhachHang.setPromptText("Chọn khách hàng (*)...");
        pnlLuoi.add(new Label("Khách hàng (*):"), 2, 1);
        pnlLuoi.add(cboKhachHang, 3, 1);

        // Hàng 2: Thời gian tiếp nhận (Ngày + Giờ) & Ghi chú tiếp nhận
        dpNgayTiepNhan.setValue(LocalDate.now());
        dpNgayTiepNhan.setDisable(true); // Khóa cố định ngày tiếp nhận hôm nay
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

        pnlKhung.getChildren().addAll(lblPhan1, pnlLichHen, pnlLuoi);
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
        cboThuCung.setPrefWidth(170);

        btnThemThuCungNhanh.setText("+ Mới");
        btnThemThuCungNhanh.getStyleClass().add("nut-phu");
        btnThemThuCungNhanh.setMinWidth(Region.USE_PREF_SIZE);

        HBox boxThuCung = new HBox(6, cboThuCung, btnThemThuCungNhanh);
        boxThuCung.setAlignment(Pos.CENTER_LEFT);

        // Ô nhập cân nặng thực tế tại quầy
        txtCanNangThucTe.setPromptText("Cân tại quầy (kg)");
        txtCanNangThucTe.setPrefWidth(110);

        // Chọn dịch vụ
        cboDichVu.setPromptText("Chọn dịch vụ chăm sóc...");
        cboDichVu.setPrefWidth(220);
        HBox.setHgrow(cboDichVu, Priority.ALWAYS);

        // Ô đơn giá tự động tính theo bậc cân nặng (chỉ đọc, nền xám)
        txtDonGia.setPromptText("Đơn giá áp dụng");
        txtDonGia.setPrefWidth(160);
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
        colDonGia.setCellValueFactory(c -> {
            BigDecimal gia = c.getValue().getDonGia();
            return new SimpleStringProperty(gia != null ? DINH_DANG_TIEN.format(gia) : "0 đ");
        });

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

    // Tạo thanh nút tác vụ phía dưới
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
        // 1. Hiển thị ComboBox Lịch hẹn
        cboLichHen.setConverter(new StringConverter<>() {
            @Override
            public String toString(LichHen lh) {
                if (lh == null) return "";
                String tenKh = lh.getTenKhachHang() != null ? lh.getTenKhachHang() : lh.getMaKhachHang();
                String tenTc = lh.getTenThuCung() != null ? lh.getTenThuCung() : lh.getMaThuCung();
                String ngay = lh.getNgayHen() != null ? lh.getNgayHen().toString() : "";
                String gio = lh.getGioHen() != null ? lh.getGioHen().toString() : "";
                return lh.getMaLichHen() + " | " + tenKh + " - Bé " + tenTc + " (" + gio + " " + ngay + ")";
            }

            @Override
            public LichHen fromString(String string) {
                return null;
            }
        });

        // Khi chọn một lịch hẹn: tự động đồng bộ khách hàng, nhân viên, thú cưng và chi tiết dịch vụ
        cboLichHen.setOnAction(e -> {
            LichHen lh = cboLichHen.getValue();
            if (lh != null) {
                dongBoTuLichHen(lh);
            }
        });

        // 2. Hiển thị ComboBox Khách hàng (gọn: Họ tên (SĐT))
        cboKhachHang.setConverter(new StringConverter<>() {
            @Override
            public String toString(KhachHang kh) {
                if (kh == null) return "";
                String sdt = (kh.getSoDienThoai() != null && !kh.getSoDienThoai().isBlank()) ? " (" + kh.getSoDienThoai() + ")" : "";
                return kh.getHoTen() + sdt;
            }

            @Override
            public KhachHang fromString(String string) {
                return null;
            }
        });

        // Tra cứu số điện thoại khi nhấn Enter hoặc bấm nút "Kiểm tra"
        txtTimKhach.setOnAction(e -> xuLyKiemTraSoDienThoai());
        btnKiemTraSdt.setOnAction(e -> xuLyKiemTraSoDienThoai());

        // Ràng buộc nhập số điện thoại (tối đa 10 chữ số) và lọc danh sách khách hàng
        txtTimKhach.textProperty().addListener((obs, cu, moi) -> {
            if (dangDongBo || moi == null) return;
            if (!moi.matches("\\d*")) {
                txtTimKhach.setText(cu != null ? cu : "");
                return;
            }
            if (moi.length() > 10) {
                txtTimKhach.setText(moi.substring(0, 10));
                return;
            }

            KhachHang khChinhXac = null;
            dangDongBo = true;
            try {
                if (moi.trim().isEmpty()) {
                    cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
                    cboKhachHang.setValue(null);
                    cboLichHen.setValue(null);
                } else {
                    List<KhachHang> loc = dsKhachHangGoc.stream()
                            .filter(k -> (k.getSoDienThoai() != null && k.getSoDienThoai().contains(moi.trim()))
                                    || (k.getHoTen() != null && k.getHoTen().toLowerCase().contains(moi.trim().toLowerCase())))
                            .toList();
                    cboKhachHang.setItems(FXCollections.observableArrayList(loc));
                    // Khi nhập đủ 10 số và khớp chính xác 1 khách hàng: ghi nhận để tự động đồng bộ
                    if (moi.length() == 10) {
                        for (KhachHang kh : loc) {
                            if (moi.equals(kh.getSoDienThoai())) {
                                khChinhXac = kh;
                                break;
                            }
                        }
                    }
                }
            } finally {
                dangDongBo = false;
            }

            // Đồng bộ thông tin khách hàng và tự động nạp lịch hẹn / dịch vụ đã đặt trước (nếu có)
            if (khChinhXac != null) {
                dongBoThongTinKhachHang(khChinhXac);
            }
        });

        // Khi khách hàng thay đổi: đồng bộ thông tin hoặc nạp danh sách thú cưng
        cboKhachHang.setOnAction(e -> {
            if (dangDongBo) return;
            KhachHang kh = cboKhachHang.getValue();
            if (kh != null) {
                dongBoThongTinKhachHang(kh);
            } else {
                cboThuCung.getItems().clear();
                cboThuCung.setValue(null);
                txtCanNangThucTe.clear();
                cboLichHen.setValue(null);
            }
        });

        // 3. Hiển thị ComboBox Thú cưng
        cboThuCung.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThuCung tc) {
                if (tc == null) return "";
                String giong = tc.getGiongLoai() != null ? tc.getGiongLoai() : "";
                String can = tc.getCanNang() != null ? " - " + tc.getCanNang() + "kg" : "";
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
                if (tc.getCanNang() != null && tc.getCanNang().doubleValue() > 0) {
                    txtCanNangThucTe.setText(tc.getCanNang().stripTrailingZeros().toPlainString());
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

        // 4. Hiển thị ComboBox Dịch vụ (gọn: Tên dịch vụ (thời lượng phút))
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

        // 5. Hiển thị ComboBox Nhân viên (gọn: Họ tên (Chức vụ))
        cboNhanVien.setConverter(new StringConverter<>() {
            @Override
            public String toString(String[] nv) {
                if (nv == null || nv.length < 2) return "";
                String chucVu = (nv.length > 2 && nv[2] != null && !nv[2].isBlank()) ? " (" + nv[2] + ")" : "";
                return nv[1] + chucVu;
            }

            @Override
            public String[] fromString(String string) {
                return null;
            }
        });

        // 6. Nút thêm dịch vụ vào bảng
        btnThemDichVu.setOnAction(e -> xuLyThemDichVu());

        // 7. Nút Lưu
        btnLuu.setOnAction(e -> xuLyLuuPhieu());

        // 8. Nút Làm mới
        btnLamMoi.setOnAction(e -> lamMoiForm());
    }

    // =========================================================================
    // 4. LOGIC TÍNH GIÁ TỰ ĐỘNG & CÂN THỰC TẾ TẠI QUẦY
    // =========================================================================

    // Lấy cân nặng thực tế hiện tại trên ô nhập hoặc từ hồ sơ thú cưng
    private double layCanNangThucTeHienTai() {
        String txt = txtCanNangThucTe.getText() != null ? txtCanNangThucTe.getText().trim() : "";
        try {
            double val = Double.parseDouble(txt);
            if (val > 0) return val;
        } catch (NumberFormatException ignored) {}

        ThuCung tc = cboThuCung.getValue();
        if (tc != null && tc.getCanNang() != null && tc.getCanNang().doubleValue() > 0) {
            return tc.getCanNang().doubleValue();
        }
        return 0; // Trả về 0 để yêu cầu bắt buộc nhập cân nặng thực tế tại quầy
    }

    // Tự động tính đơn giá cho dịch vụ đang chọn theo bậc cân nặng
    private void tinhGiaTuDong() {
        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) {
            txtDonGia.clear();
            donGiaHienHanh = BigDecimal.ZERO;
            return;
        }

        double canNang = layCanNangThucTeHienTai();
        if (canNang <= 0) {
            txtDonGia.setText("Cần nhập cân nặng (*)");
            donGiaHienHanh = BigDecimal.ZERO;
            return;
        }

        double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang);
        donGiaHienHanh = BigDecimal.valueOf(gia);
        String bac = BangGiaDichVu.xacDinhKichThuoc(canNang);
        txtDonGia.setText(DINH_DANG_TIEN.format(gia) + " (" + bac + ")");
    }

    // Khi nhân viên nhập/sửa cân nặng tại quầy: tự động cập nhật đơn giá trên form và các dòng trong bảng
    private void tinhLaiGiaTheoCanNangThucTe() {
        ThuCung tc = cboThuCung.getValue();
        double canNang = layCanNangThucTeHienTai();

        // 1. Cập nhật txtDonGia cho dịch vụ hiện tại
        tinhGiaTuDong();

        // 2. Cập nhật lại các dòng dịch vụ của thú cưng này trong bảng (đặc biệt là giá tạm tính từ Lịch hẹn)
        if (tc != null && canNang > 0) {
            boolean coThayDoi = false;
            for (ChiTietPhieuDichVu ct : dsChiTietChon) {
                if (tc.getMaThuCung().equals(ct.getMaThuCung())) {
                    double giaMoi = controller.traGiaDichVuTheoCanNang(ct.getMaDichVu(), canNang);
                    ct.setDonGia(BigDecimal.valueOf(giaMoi));
                    coThayDoi = true;
                }
            }
            if (coThayDoi) {
                tblChiTiet.refresh();
                capNhatTongTien();
            }

            // Ghi nhận cân nặng mới nhất để cập nhật vào bảng ThuCung khi lập phiếu
            mapCanNangDaCan.put(tc.getMaThuCung(), BigDecimal.valueOf(canNang));
        }
    }

    // Kiểm tra thông tin dị ứng của thú cưng và tự động chèn vào ghi chú nếu có
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

    // Đồng bộ toàn bộ thông tin từ lịch hẹn sang phiếu tiếp nhận (khách hàng, nhân viên, thú cưng, dịch vụ đã đặt)
    private void dongBoTuLichHen(LichHen lh) {
        if (lh == null) return;

        // Đồng bộ khách hàng
        KhachHang khKhop = null;
        for (KhachHang kh : dsKhachHangGoc) {
            if (kh.getMaKhachHang().equals(lh.getMaKhachHang())) {
                khKhop = kh;
                break;
            }
        }
        if (khKhop != null) {
            dangDongBo = true;
            try {
                cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
                cboKhachHang.setValue(khKhop);
                if (khKhop.getSoDienThoai() != null) {
                    txtTimKhach.setText(khKhop.getSoDienThoai());
                }
            } finally {
                dangDongBo = false;
            }
        }

        // Đồng bộ nhân viên phụ trách của lịch hẹn
        if (lh.getMaNhanVien() != null && !lh.getMaNhanVien().isBlank()) {
            for (String[] nv : cboNhanVien.getItems()) {
                if (nv.length > 0 && lh.getMaNhanVien().equals(nv[0])) {
                    cboNhanVien.setValue(nv);
                    break;
                }
            }
        }

        // Nạp danh sách thú cưng của khách và chọn đúng thú cưng của lịch hẹn
        if (khKhop != null) {
            List<ThuCung> dsTc = controller.layDanhSachThuCungTheoKhachHang(khKhop.getMaKhachHang());
            cboThuCung.setItems(FXCollections.observableArrayList(dsTc));
            ThuCung tcKhop = null;
            for (ThuCung tc : dsTc) {
                if (tc.getMaThuCung().equals(lh.getMaThuCung())) {
                    tcKhop = tc;
                    break;
                }
            }
            if (tcKhop != null) {
                cboThuCung.setValue(tcKhop);
                if (tcKhop.getCanNang() != null && tcKhop.getCanNang().doubleValue() > 0) {
                    txtCanNangThucTe.setText(tcKhop.getCanNang().stripTrailingZeros().toPlainString());
                } else {
                    txtCanNangThucTe.clear();
                }
                kiemTraVaChenLuuYDiUng(tcKhop);
            }
        }

        // Nạp chi tiết dịch vụ từ lịch hẹn (tính theo đơn giá thực tế theo cân nặng tại quầy)
        dsChiTietChon.clear();
        double canNang = layCanNangThucTeHienTai();
        List<ChiTietLichHen> dsCtLh = controller.layChiTietLichHen(lh.getMaLichHen());
        String tenTc = (lh.getTenThuCung() != null && !lh.getTenThuCung().isBlank())
                ? lh.getTenThuCung()
                : (cboThuCung.getValue() != null ? cboThuCung.getValue().getTenThuCung() : lh.getMaThuCung());

        for (ChiTietLichHen ctLh : dsCtLh) {
            // Nếu tại quầy chưa có cân nặng và chi tiết lịch hẹn có ghi cân nặng thì nạp lên
            if (canNang <= 0 && ctLh.getCanNangThuCung() != null && ctLh.getCanNangThuCung().doubleValue() > 0) {
                canNang = ctLh.getCanNangThuCung().doubleValue();
                txtCanNangThucTe.setText(ctLh.getCanNangThuCung().stripTrailingZeros().toPlainString());
            }

            double donGiaThucTe = controller.traGiaDichVuTheoCanNang(ctLh.getMaDichVu(), canNang);
            BigDecimal giaApDung = (donGiaThucTe > 0) ? BigDecimal.valueOf(donGiaThucTe) : ctLh.getDonGia();

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

        // Ghi chú tiếp nhận
        String ghiChuLh = "Tiếp nhận theo lịch hẹn " + lh.getMaLichHen() + (lh.getGhiChu() != null && !lh.getGhiChu().isBlank() ? " | " + lh.getGhiChu() : "");
        ThuCung tcHienTai = cboThuCung.getValue();
        if (tcHienTai != null && tcHienTai.getDiUng() != null && !tcHienTai.getDiUng().isBlank() && !"Không có".equalsIgnoreCase(tcHienTai.getDiUng().trim())) {
            ghiChuLh = "[Lưu ý dị ứng: " + tcHienTai.getDiUng().trim() + "] " + ghiChuLh;
        }
        txtGhiChu.setText(ghiChuLh);

        capNhatTongTien();
        tinhGiaTuDong();
    }

    // Đồng bộ thông tin khi tìm kiếm/chọn khách hàng (nếu có lịch hẹn chờ thì tự động nạp lịch hẹn & dịch vụ đã đặt)
    private void dongBoThongTinKhachHang(KhachHang kh) {
        if (kh == null) return;

        dangDongBo = true;
        try {
            cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
            cboKhachHang.setValue(kh);
            if (kh.getSoDienThoai() != null && !kh.getSoDienThoai().equals(txtTimKhach.getText())) {
                txtTimKhach.setText(kh.getSoDienThoai());
            }
        } finally {
            dangDongBo = false;
        }

        // Nạp lại danh sách lịch hẹn chờ tiếp nhận để đảm bảo dữ liệu mới nhất
        napDanhSachLichHenCho();

        // Kiểm tra xem khách hàng này có lịch hẹn chờ tiếp nhận (Đã đặt / Đã xác nhận) hay không
        LichHen lhKhop = null;
        for (LichHen lh : cboLichHen.getItems()) {
            if (lh != null && kh.getMaKhachHang().equals(lh.getMaKhachHang())) {
                lhKhop = lh;
                break;
            }
        }

        if (lhKhop != null) {
            cboLichHen.setValue(lhKhop);
            dongBoTuLichHen(lhKhop);
        } else {
            cboLichHen.setValue(null);
            List<ThuCung> dsTc = controller.layDanhSachThuCungTheoKhachHang(kh.getMaKhachHang());
            cboThuCung.setItems(FXCollections.observableArrayList(dsTc));
            if (!dsTc.isEmpty()) {
                cboThuCung.setValue(dsTc.get(0));
            } else {
                cboThuCung.setValue(null);
                txtCanNangThucTe.clear();
            }
        }
    }

    // Xử lý kiểm tra số điện thoại khi nhấn Enter hoặc bấm nút "Kiểm tra"
    private void xuLyKiemTraSoDienThoai() {
        String sdt = txtTimKhach.getText() != null ? txtTimKhach.getText().trim() : "";
        if (sdt.isEmpty()) {
            hienThiCanhBao("Chưa nhập số điện thoại", "Vui lòng nhập số điện thoại khách hàng cần tra cứu!");
            txtTimKhach.requestFocus();
            return;
        }

        KhachHang khKhop = null;
        for (KhachHang kh : dsKhachHangGoc) {
            if (sdt.equals(kh.getSoDienThoai())) {
                khKhop = kh;
                break;
            }
        }

        if (khKhop != null) {
            dongBoThongTinKhachHang(khKhop);
            cboKhachHang.requestFocus();
        } else {
            if (sdt.matches("^0[0-9]{9}$")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Khách hàng mới");
                alert.setHeaderText("Khách hàng chưa có trong hệ thống!");
                alert.setContentText("Số điện thoại '" + sdt + "' chưa từng sử dụng dịch vụ tại cửa hàng.\nHệ thống sẽ mở hộp thoại thêm nhanh khách hàng mới.");
                alert.showAndWait();
                moCuaSoThemKhachNhanh(sdt);
            } else {
                hienThiCanhBao("Số điện thoại không hợp lệ", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0 (Ví dụ: 0912345678)!");
                txtTimKhach.requestFocus();
            }
        }
    }

    // Hộp thoại popup Thêm nhanh khách hàng mới
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

        // Chặn ký tự đặc biệt ở họ tên
        txtHoTenMoi.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            String loc = moi.replaceAll("[^\\p{L}0-9\\s]", "");
            if (!loc.equals(moi)) txtHoTenMoi.setText(loc);
        });

        // Khóa đúng 10 số điện thoại
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
                    dsKhachHangGoc.add(0, khMoi);
                    cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
                    cboKhachHang.setValue(khMoi);

                    dangDongBo = true;
                    txtTimKhach.setText(sdt);
                    dangDongBo = false;

                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm nhanh khách hàng '" + ten + "' (" + sdt + ")!");

                    // Gợi ý thêm nhanh thú cưng cho khách
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

    // Hộp thoại popup Thêm nhanh hồ sơ thú cưng tại quầy
    private void moCuaSoThemThuCungNhanh() {
        KhachHang kh = cboKhachHang.getValue();
        if (kh == null) {
            hienThiCanhBao("Chưa chọn khách hàng", "Vui lòng chọn hoặc thêm khách hàng trước khi tạo thú cưng!");
            return;
        }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) dlg.initOwner(getScene().getWindow());
        dlg.setTitle("Thêm nhanh thú cưng - " + kh.getHoTen());

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("THÊM NHANH HỒ SƠ THÚ CƯNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(140);
        col1.setPrefWidth(140);
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

        TextField txtChuNuoi = new TextField(kh.getHoTen() + " (" + kh.getSoDienThoai() + ")");
        txtChuNuoi.setEditable(false);
        txtChuNuoi.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");

        TextField txtTenThuCung = new TextField();
        txtTenThuCung.setPromptText("Tên thú cưng (VD: Mochi, Bông, Lu, Corgi...) (*)");
        // Chặn ký tự đặc biệt ở tên thú cưng
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
        // Chặn ký tự đặc biệt ở giống loài
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
        // Lấy cân nặng đã gõ trên quầy (nếu có)
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

        luoi.add(new Label("Mã thú cưng:"), 0, 0);
        luoi.add(txtMaThuCung, 1, 0);
        luoi.add(new Label("Chủ nuôi:"), 0, 1);
        luoi.add(txtChuNuoi, 1, 1);
        luoi.add(new Label("Tên thú cưng (*):"), 0, 2);
        luoi.add(txtTenThuCung, 1, 2);
        luoi.add(new Label("Loài thú cưng:"), 0, 3);
        luoi.add(cboLoai, 1, 3);
        luoi.add(new Label("Giống loài:"), 0, 4);
        luoi.add(txtGiongLoai, 1, 4);
        luoi.add(new Label("Giới tính:"), 0, 5);
        luoi.add(cboGioiTinh, 1, 5);
        luoi.add(new Label("Cân nặng (kg) (*):"), 0, 6);
        luoi.add(txtCanNangMoi, 1, 6);
        luoi.add(new Label("Ngày sinh:"), 0, 7);
        luoi.add(dpNgaySinhTc, 1, 7);
        luoi.add(new Label("Dị ứng / Lưu ý:"), 0, 8);
        luoi.add(txtDiUngTc, 1, 8);

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

            BigDecimal canNangVal;
            try {
                canNangVal = new BigDecimal(cnStr);
                if (canNangVal.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new NumberFormatException();
                }
            } catch (Exception ex) {
                hienThiCanhBao("Cân nặng không hợp lệ", "Cân nặng thú cưng phải là số lớn hơn 0!");
                txtCanNangMoi.requestFocus();
                return;
            }

            try {
                ThuCung tc = new ThuCung(
                        txtMaThuCung.getText().trim(),
                        kh.getMaKhachHang(),
                        ten,
                        cboLoai.getValue(),
                        txtGiongLoai.getText() != null ? txtGiongLoai.getText().trim() : "",
                        canNangVal,
                        dpNgaySinhTc.getValue(),
                        txtDiUngTc.getText() != null ? txtDiUngTc.getText().trim() : "",
                        cboGioiTinh.getValue()
                );

                if (controller.themThuCung(tc)) {
                    List<ThuCung> dsMoi = controller.layDanhSachThuCungTheoKhachHang(kh.getMaKhachHang());
                    cboThuCung.setItems(FXCollections.observableArrayList(dsMoi));
                    cboThuCung.setValue(tc);
                    txtCanNangThucTe.setText(canNangVal.stripTrailingZeros().toPlainString());

                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm thú cưng '" + ten + "' cho khách hàng " + kh.getHoTen() + "!");
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

    // Xử lý thêm một dòng dịch vụ vào bảng chi tiết
    private void xuLyThemDichVu() {
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

        if (donGiaHienHanh == null || donGiaHienHanh.compareTo(BigDecimal.ZERO) <= 0) {
            double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang);
            if (gia > 0) {
                donGiaHienHanh = BigDecimal.valueOf(gia);
            } else {
                hienThiCanhBao("Đơn giá không hợp lệ", "Chưa xác định được đơn giá hợp lệ cho dịch vụ!");
                return;
            }
        }

        // Kiểm tra xem dịch vụ này đã thêm cho thú cưng này chưa
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

    // Cập nhật nhãn tổng tiền dự tính
    private void capNhatTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietPhieuDichVu ct : dsChiTietChon) {
            if (ct.getDonGia() != null) {
                tong = tong.add(ct.getDonGia());
            }
        }
        lblTongTien.setText("Tổng tiền dự tính: " + DINH_DANG_TIEN.format(tong));
    }

    // Xử lý lưu phiếu tiếp nhận xuống CSDL
    private void xuLyLuuPhieu() {
        try {
            KhachHang kh = cboKhachHang.getValue();
            if (kh == null) {
                hienThiCanhBao("Thiếu thông tin", "Vui lòng chọn hoặc tra cứu khách hàng tiếp nhận!");
                return;
            }

            String[] nv = cboNhanVien.getValue();
            if (nv == null || nv.length == 0) {
                hienThiCanhBao("Thiếu thông tin", "Vui lòng chọn nhân viên tiếp nhận!");
                return;
            }

            // Kiểm tra cân nặng thực tế bắt buộc tại quầy
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

            LichHen lh = cboLichHen.getValue();
            String maLichHen = (lh != null) ? lh.getMaLichHen() : null;

            PhieuDichVu phieu = new PhieuDichVu(
                    txtMaPhieuDichVu.getText().trim(),
                    kh.getMaKhachHang(),
                    nv[0],
                    maLichHen,
                    ngay,
                    gio,
                    "Đang chờ",
                    txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : ""
            );

            List<ChiTietPhieuDichVu> danhSachChiTiet = new ArrayList<>(dsChiTietChon);
            boolean thanhCong = controller.taoPhieuDichVu(phieu, danhSachChiTiet);
            if (thanhCong) {
                // Tự động cập nhật lại cân nặng mới nhất cho thú cưng trong CSDL nếu có
                ThuCung tcHienTai = cboThuCung.getValue();
                double cnThucTe = layCanNangThucTeHienTai();
                if (tcHienTai != null && cnThucTe > 0) {
                    mapCanNangDaCan.put(tcHienTai.getMaThuCung(), BigDecimal.valueOf(cnThucTe));
                }

                for (Map.Entry<String, BigDecimal> entry : mapCanNangDaCan.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
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
            hienThiCanhBao("Lỗi hệ thống", "Đã xảy ra lỗi không mong muốn: " + ex.getMessage());
        }
    }

    // =========================================================================
    // 7. KHỞI TẠO & LÀM MỚI DỮ LIỆU
    // =========================================================================

    // Nạp dữ liệu ban đầu cho các ComboBox
    private void napDuLieuBanDau() {
        lamMoiForm();
        try {
            // Nạp khách hàng
            dsKhachHangGoc.clear();
            dsKhachHangGoc.addAll(controller.layDanhSachKhachHang());
            cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));

            // Nạp nhân viên và chọn mặc định nhân viên Chăm sóc
            List<String[]> dsNv = controller.layDanhSachNhanVien();
            cboNhanVien.setItems(FXCollections.observableArrayList(dsNv));
            chonMacDinhNhanVienChamSoc();

            // Nạp dịch vụ
            List<DichVuChamSoc> dsDv = controller.layDanhSachDichVu();
            cboDichVu.setItems(FXCollections.observableArrayList(dsDv));
            if (!dsDv.isEmpty()) {
                cboDichVu.setValue(dsDv.get(0));
            }

            // Nạp lịch hẹn chờ tiếp nhận
            napDanhSachLichHenCho();
        } catch (Exception ex) {
            System.err.println("Lỗi khi nạp dữ liệu ban đầu cho phiếu dịch vụ: " + ex.getMessage());
        }
    }

    // Chọn nhân viên có chức vụ "Chăm sóc" đầu tiên làm mặc định
    private void chonMacDinhNhanVienChamSoc() {
        for (String[] nv : cboNhanVien.getItems()) {
            if (nv.length > 2 && nv[2] != null && nv[2].toLowerCase().contains("chăm sóc")) {
                cboNhanVien.setValue(nv);
                return;
            }
        }
        if (!cboNhanVien.getItems().isEmpty()) {
            cboNhanVien.setValue(cboNhanVien.getItems().get(0));
        }
    }

    // Nạp lại danh sách lịch hẹn chờ tiếp nhận
    private void napDanhSachLichHenCho() {
        try {
            List<LichHen> dsLh = controller.layDanhSachLichHenChoTiepNhan();
            cboLichHen.setItems(FXCollections.observableArrayList(dsLh));
        } catch (Exception ignored) {}
    }

    // Đặt lại dữ liệu form về trạng thái mới
    private void lamMoiForm() {
        try {
            txtMaPhieuDichVu.setText(controller.taoMaPhieuDichVuMoi());
        } catch (Exception ex) {
            txtMaPhieuDichVu.setText("");
        }
        dangDongBo = true;
        cboLichHen.setValue(null);
        cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
        cboKhachHang.setValue(null);
        cboKhachHang.setDisable(false);
        txtTimKhach.clear();
        txtTimKhach.setDisable(false);
        btnKiemTraSdt.setDisable(false);
        dangDongBo = false;

        cboThuCung.getItems().clear();
        cboThuCung.setValue(null);
        txtCanNangThucTe.clear();
        txtDonGia.clear();
        donGiaHienHanh = BigDecimal.ZERO;

        dpNgayTiepNhan.setValue(LocalDate.now());
        txtGioTiepNhan.setText(LocalTime.now().format(DINH_DANG_GIO));
        txtGhiChu.clear();
        dsChiTietChon.clear();
        mapCanNangDaCan.clear();

        chonMacDinhNhanVienChamSoc();
        capNhatTongTien();
        napDanhSachLichHenCho();
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
}
