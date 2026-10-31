package com.petshop.view.lichhen;

import com.petshop.controller.LichHenController;
import com.petshop.controller.ThuCungController;
import com.petshop.model.*;
import javafx.application.Platform;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Phân hệ Đặt lịch hẹn dịch vụ chăm sóc thú cưng (APT_CREATE / LH_TAO).
 * Hỗ trợ tiếp nhận thông tin khách hàng, thú cưng, chọn dịch vụ và tính tiền tự động.
 */
public class DatLichHenView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";

    private final LichHenController controller = new LichHenController();
    private final Runnable khiDatThanhCong;
    private boolean dangDongBo = false;

    // Danh sách dữ liệu liên kết
    private final List<KhachHang> dsKhachHangGoc = new ArrayList<>();
    private final ObservableList<ChiTietLichHen> danhSachDichVuChon = FXCollections.observableArrayList();

    // Điều khiển thông tin lịch hẹn
    private final TextField txtMaLichHen = new TextField();
    private final TextField txtTimKhach = new TextField();
    private final Button btnTimSdt = new Button("Kiểm tra");
    private final ComboBox<KhachHang> cboKhachHang = new ComboBox<>();

    private final ComboBox<ThuCung> cboThuCung = new ComboBox<>();
    private final Button btnThemThuCungNhanh = new Button("+ Mới");

    private final ComboBox<String[]> cboNhanVien = new ComboBox<>();
    private final DatePicker dpNgayHen = new DatePicker();
    private final ComboBox<String> cboGioHen = new ComboBox<>();
    private final TextArea txtGhiChu = new TextArea();

    // Điều khiển dịch vụ chăm sóc
    private final ComboBox<DichVuChamSoc> cboDichVu = new ComboBox<>();
    private final Button btnThemDichVu = new Button("+ Thêm vào bảng");
    private final TableView<ChiTietLichHen> tblDichVu = new TableView<>();

    // Nút chức năng
    private final Button btnLuu = new Button("✔ Xác nhận đặt lịch");
    private final Button btnLamMoi = new Button("Làm mới");

    public DatLichHenView() {
        this(null);
    }

    public DatLichHenView(Runnable khiDatThanhCong) {
        this.khiDatThanhCong = khiDatThanhCong;

        setSpacing(10);
        setPadding(new Insets(14, 20, 14, 20));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(2);
        Label lblTieuDe = new Label("ĐẶT LỊCH HẸN DỊCH VỤ CHĂM SÓC");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tiếp nhận đặt lịch hẹn chăm sóc thú cưng cho khách hàng (chặn chọn ngày quá khứ).");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Nội dung cuộn (Thông tin lịch + Dịch vụ)
        VBox pnlThongTinLich = taoKhungThongTinLich();
        VBox pnlChonDichVu = taoKhungChonDichVu();
        VBox.setVgrow(pnlChonDichVu, Priority.ALWAYS);

        VBox pnlCuon = new VBox(10, pnlThongTinLich, pnlChonDichVu);
        pnlCuon.setFillWidth(true);
        VBox.setVgrow(pnlCuon, Priority.ALWAYS);

        ScrollPane spCuon = new ScrollPane(pnlCuon);
        spCuon.setFitToWidth(true);
        spCuon.setFitToHeight(true);
        spCuon.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spCuon.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(spCuon, Priority.ALWAYS);

        // 3. Thanh nút đáy màn hình
        getChildren().addAll(pnlTieuDe, spCuon, taoThanhNutBam());

        dangKySuKien();
        lamMoiForm();
    }

    // -------------------------------------------------------------------------
    // BỐ CỤC GIAO DIỆN (UI LAYOUT)
    // -------------------------------------------------------------------------

    private VBox taoKhungThongTinLich() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblTieuDePhan = new Label("1. Thông tin lịch hẹn");
        lblTieuDePhan.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        // Bố cục 2 nửa đối xứng (Trái 50% - Phải 50%) khóa cứng tỷ lệ, không co giật kích thước khi nhập SĐT
        GridPane pnlHaiCot = new GridPane();
        pnlHaiCot.setHgap(20);
        pnlHaiCot.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints colTrai = new ColumnConstraints();
        colTrai.setPercentWidth(50.0);
        colTrai.setFillWidth(true);
        colTrai.setHgrow(Priority.ALWAYS);

        ColumnConstraints colPhai = new ColumnConstraints();
        colPhai.setPercentWidth(50.0);
        colPhai.setFillWidth(true);
        colPhai.setHgrow(Priority.ALWAYS);
        pnlHaiCot.getColumnConstraints().addAll(colTrai, colPhai);

        // --- CỘT TRÁI (Mã lịch hẹn, SĐT tra cứu, Thú cưng) ---
        GridPane pnlCotTrai = new GridPane();
        pnlCotTrai.setHgap(10);
        pnlCotTrai.setVgap(8);
        pnlCotTrai.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints cTrai1 = new ColumnConstraints();
        cTrai1.setMinWidth(135);
        cTrai1.setPrefWidth(135);
        cTrai1.setMaxWidth(135);

        ColumnConstraints cTrai2 = new ColumnConstraints();
        cTrai2.setHgrow(Priority.ALWAYS);
        cTrai2.setFillWidth(true);
        pnlCotTrai.getColumnConstraints().addAll(cTrai1, cTrai2);

        txtMaLichHen.setEditable(false);
        txtMaLichHen.setMaxWidth(Double.MAX_VALUE);

        // SĐT khách hàng + Nút Kiểm tra
        txtTimKhach.setPromptText("Nhập SĐT (10 số) rồi bấm Enter...");
        txtTimKhach.setMaxWidth(Double.MAX_VALUE);
        txtTimKhach.setOnAction(e -> xuLyNhapSdtKhachHang());

        btnTimSdt.getStyleClass().add("nut-phu");
        btnTimSdt.setMinWidth(Region.USE_PREF_SIZE);
        btnTimSdt.setOnAction(e -> xuLyNhapSdtKhachHang());

        HBox pnlSdt = new HBox(6, txtTimKhach, btnTimSdt);
        pnlSdt.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtTimKhach, Priority.ALWAYS);

        // Thú cưng + Nút "+ Mới"
        cboThuCung.setPromptText("Chọn thú cưng");
        cboThuCung.setMaxWidth(Double.MAX_VALUE);

        btnThemThuCungNhanh.getStyleClass().add("nut-phu");
        btnThemThuCungNhanh.setMinWidth(Region.USE_PREF_SIZE);
        btnThemThuCungNhanh.setOnAction(e -> moCuaSoThemThuCungNhanh());

        HBox pnlChonThuCung = new HBox(6, cboThuCung, btnThemThuCungNhanh);
        pnlChonThuCung.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cboThuCung, Priority.ALWAYS);

        pnlCotTrai.add(new Label("Mã lịch hẹn:"), 0, 0);
        pnlCotTrai.add(txtMaLichHen, 1, 0);
        pnlCotTrai.add(new Label("SĐT khách hàng (*):"), 0, 1);
        pnlCotTrai.add(pnlSdt, 1, 1);
        pnlCotTrai.add(new Label("Thú cưng (*):"), 0, 2);
        pnlCotTrai.add(pnlChonThuCung, 1, 2);

        // --- CỘT PHẢI (NV phụ trách, Khách hàng, Ngày & Giờ) ---
        GridPane pnlCotPhai = new GridPane();
        pnlCotPhai.setHgap(10);
        pnlCotPhai.setVgap(8);
        pnlCotPhai.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints cPhai1 = new ColumnConstraints();
        cPhai1.setMinWidth(135);
        cPhai1.setPrefWidth(135);
        cPhai1.setMaxWidth(135);

        ColumnConstraints cPhai2 = new ColumnConstraints();
        cPhai2.setHgrow(Priority.ALWAYS);
        cPhai2.setFillWidth(true);
        pnlCotPhai.getColumnConstraints().addAll(cPhai1, cPhai2);

        cboNhanVien.setPromptText("Chọn nhân viên chăm sóc");
        cboNhanVien.setMaxWidth(Double.MAX_VALUE);

        cboKhachHang.setPromptText("Chọn khách hàng");
        cboKhachHang.setMaxWidth(Double.MAX_VALUE);

        // Ngày & Giờ
        dpNgayHen.setPromptText("dd/MM/yyyy");
        dpNgayHen.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(dpNgayHen, Priority.ALWAYS);
        dpNgayHen.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        cboGioHen.getItems().clear();
        for (int h = 8; h <= 20; h++) {
            cboGioHen.getItems().add(String.format("%02d:00", h));
            if (h < 20) cboGioHen.getItems().add(String.format("%02d:30", h));
        }
        cboGioHen.setValue("09:00");
        cboGioHen.setPrefWidth(95);
        cboGioHen.setMinWidth(85);

        Label lblGio = new Label("Giờ:");
        lblGio.setMinWidth(Region.USE_PREF_SIZE);

        HBox pnlThoiGian = new HBox(8, dpNgayHen, lblGio, cboGioHen);
        pnlThoiGian.setAlignment(Pos.CENTER_LEFT);

        pnlCotPhai.add(new Label("NV phụ trách (*):"), 0, 0);
        pnlCotPhai.add(cboNhanVien, 1, 0);
        pnlCotPhai.add(new Label("Khách hàng (*):"), 0, 1);
        pnlCotPhai.add(cboKhachHang, 1, 1);
        pnlCotPhai.add(new Label("Ngày & Giờ (*):"), 0, 2);
        pnlCotPhai.add(pnlThoiGian, 1, 2);

        // Đưa 2 cột vào pnlHaiCot (50% - 50%)
        GridPane.setHgrow(pnlCotTrai, Priority.ALWAYS);
        GridPane.setHgrow(pnlCotPhai, Priority.ALWAYS);
        pnlHaiCot.add(pnlCotTrai, 0, 0);
        pnlHaiCot.add(pnlCotPhai, 1, 0);

        // --- HÀNG DƯỚI: Ghi chú / Lưu ý trải ngang toàn bộ chiều rộng ---
        HBox pnlGhiChu = new HBox(10);
        pnlGhiChu.setAlignment(Pos.CENTER_LEFT);

        Label lblGhiChu = new Label("Ghi chú / Lưu ý:");
        lblGhiChu.setMinWidth(135);
        lblGhiChu.setPrefWidth(135);
        lblGhiChu.setMaxWidth(135);

        txtGhiChu.setPromptText("Ghi chú thêm (yêu cầu riêng của khách hàng, lưu ý thú cưng nhát nước...)");
        txtGhiChu.setPrefRowCount(1);
        txtGhiChu.setPrefHeight(36);
        txtGhiChu.setMaxHeight(36);
        txtGhiChu.setWrapText(true);
        txtGhiChu.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(txtGhiChu, Priority.ALWAYS);

        pnlGhiChu.getChildren().addAll(lblGhiChu, txtGhiChu);

        pnlKhung.getChildren().addAll(lblTieuDePhan, pnlHaiCot, pnlGhiChu);
        return pnlKhung;
    }

    private VBox taoKhungChonDichVu() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        Label lblTieuDe = new Label("2. Dịch vụ chăm sóc đăng ký");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        cboDichVu.setPromptText("Chọn dịch vụ chăm sóc thú cưng...");
        cboDichVu.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cboDichVu, Priority.ALWAYS);

        btnThemDichVu.setText("+ Thêm vào bảng");
        btnThemDichVu.getStyleClass().add("nut-chinh");
        btnThemDichVu.setMinWidth(Region.USE_PREF_SIZE);

        HBox pnlChon = new HBox(10, new Label("Dịch vụ chăm sóc:"), cboDichVu, btnThemDichVu);
        pnlChon.setAlignment(Pos.CENTER_LEFT);

        // Bảng danh sách dịch vụ chi tiết
        tblDichVu.getStyleClass().add("bang-du-lieu");
        tblDichVu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblDichVu.setPrefHeight(260);
        tblDichVu.setMinHeight(180);
        tblDichVu.setPlaceholder(new Label("Chưa có dịch vụ nào được thêm vào lịch hẹn"));
        VBox.setVgrow(tblDichVu, Priority.ALWAYS);

        TableColumn<ChiTietLichHen, String> colMaDV = new TableColumn<>("Mã dịch vụ");
        colMaDV.setStyle("-fx-alignment: CENTER;");
        colMaDV.setPrefWidth(140);
        colMaDV.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaDichVu()));

        TableColumn<ChiTietLichHen, String> colTenDV = new TableColumn<>("Tên dịch vụ chăm sóc");
        colTenDV.setStyle("-fx-alignment: CENTER-LEFT;");
        colTenDV.setPrefWidth(500);
        colTenDV.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDichVu()));

        TableColumn<ChiTietLichHen, Void> colXoa = new TableColumn<>("Thao tác");
        colXoa.setStyle("-fx-alignment: CENTER;");
        colXoa.setPrefWidth(90);
        colXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoaDong = new Button("Xóa");
            {
                btnXoaDong.getStyleClass().addAll("button", "nut-xoa");
                btnXoaDong.setOnAction(e -> danhSachDichVuChon.remove(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoaDong);
            }
        });

        tblDichVu.getColumns().setAll(List.of(colMaDV, colTenDV, colXoa));
        tblDichVu.setItems(danhSachDichVuChon);

        Label lblGhiChuCuoi = new Label("💡 Tiếp nhận danh sách dịch vụ khách hẹn trước. Cân nặng thú cưng và đơn giá chính xác sẽ được xác định khi lập phiếu tiếp nhận tại quầy.");
        lblGhiChuCuoi.setStyle("-fx-font-style: italic; -fx-text-fill: #64748b; -fx-font-size: 11.5px;");

        pnlKhung.getChildren().addAll(lblTieuDe, pnlChon, tblDichVu, lblGhiChuCuoi);
        return pnlKhung;
    }

    private HBox taoThanhNutBam() {
        HBox pnlNut = new HBox(12);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        pnlNut.setStyle("-fx-padding: 8 0 2 0; -fx-border-color: #cbd5e1 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Label lblNhacNho = new Label("💡 Bấm nút '✔ Xác nhận đặt lịch' bên cạnh để lưu vào hệ thống");
        lblNhacNho.setStyle("-fx-text-fill: #0284c7; -fx-font-size: 12.5px; -fx-font-weight: bold;");
        Region khoangCach = new Region();
        HBox.setHgrow(khoangCach, Priority.ALWAYS);

        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-padding: 8 22;");
        btnLamMoi.getStyleClass().add("nut-phu");

        btnLuu.setOnAction(e -> xuLyDatLich());
        btnLamMoi.setOnAction(e -> lamMoiForm());

        pnlNut.getChildren().addAll(lblNhacNho, khoangCach, btnLamMoi, btnLuu);
        return pnlNut;
    }

    // -------------------------------------------------------------------------
    // ĐĂNG KÝ SỰ KIỆN & VALIDATION
    // -------------------------------------------------------------------------

    private void dangKySuKien() {
        cboKhachHang.setConverter(taoConverter(kh -> kh.getHoTen() + " (" + kh.getSoDienThoai() + ")"));
        cboNhanVien.setConverter(taoConverter(nv -> nv[1] + " (" + nv[2] + ")"));
        cboDichVu.setConverter(taoConverter(dv -> dv.getTenDichVu() + " (" + dv.getThoiLuong() + " phút)"));

        cboThuCung.setConverter(taoConverter(tc -> {
            String loai = tc.getLoaiThuCung() != null ? tc.getLoaiThuCung() : "Thú cưng";
            String giong = tc.getGiongLoai() != null && !tc.getGiongLoai().isEmpty() ? " - " + tc.getGiongLoai() : "";
            return tc.getTenThuCung() + " [" + loai + giong + "]";
        }));
        cboThuCung.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ThuCung tc, boolean empty) {
                super.updateItem(tc, empty);
                if (empty || tc == null) {
                    setText(null);
                } else {
                    String loai = tc.getLoaiThuCung() != null ? tc.getLoaiThuCung() : "Thú cưng";
                    String giong = tc.getGiongLoai() != null && !tc.getGiongLoai().isEmpty() ? " - " + tc.getGiongLoai() : "";
                    setText(tc.getTenThuCung() + " [" + loai + giong + "]");
                }
            }
        });

        // Khóa cứng ô tìm khách: chỉ nhập số, tối đa 10 số (không tự động chọn khách khi đang gõ)
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

            dangDongBo = true;
            try {
                if (moi.trim().isEmpty()) {
                    cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
                } else {
                    List<KhachHang> dsLoc = dsKhachHangGoc.stream()
                            .filter(kh -> kh.getSoDienThoai() != null && kh.getSoDienThoai().contains(moi.trim()))
                            .toList();
                    cboKhachHang.setItems(FXCollections.observableArrayList(dsLoc));
                }
            } finally {
                dangDongBo = false;
            }
        });

        // Khi chọn khách: nạp thú cưng tương ứng và tự động đồng bộ lại SĐT sang txtTimKhach
        cboKhachHang.setOnAction(e -> {
            if (dangDongBo) return;
            KhachHang kh = cboKhachHang.getValue();
            if (kh != null) {
                taiDanhSachThuCung(kh.getMaKhachHang());
                if (kh.getSoDienThoai() != null && !kh.getSoDienThoai().equals(txtTimKhach.getText())) {
                    dangDongBo = true;
                    txtTimKhach.setText(kh.getSoDienThoai());
                    dangDongBo = false;
                }
            } else {
                cboThuCung.getItems().clear();
            }
        });

        // Khi chọn thú cưng: ghi chú dị ứng nếu có
        cboThuCung.setOnAction(e -> {
            ThuCung tc = cboThuCung.getValue();
            if (tc != null && tc.getDiUng() != null && !tc.getDiUng().trim().isEmpty() && !"Không có".equalsIgnoreCase(tc.getDiUng().trim())) {
                String luuY = "[Lưu ý dị ứng: " + tc.getDiUng().trim() + "] ";
                if (!txtGhiChu.getText().contains(luuY)) {
                    txtGhiChu.setText(luuY + txtGhiChu.getText());
                }
            }
        });

        btnThemDichVu.setOnAction(e -> xuLyThemDichVuVaoBang());
    }

    // -------------------------------------------------------------------------
    // NGHIỆP VỤ TIẾP NHẬN ĐẶT LỊCH HẸN
    // -------------------------------------------------------------------------

    private void xuLyThemDichVuVaoBang() {
        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) { hienThiLoi("Vui lòng chọn dịch vụ chăm sóc!"); return; }

        if (danhSachDichVuChon.stream().anyMatch(ct -> ct.getMaDichVu().equals(dv.getMaDichVu()))) {
            hienThiLoi("Dịch vụ '" + dv.getTenDichVu() + "' đã có trong lịch hẹn!");
            return;
        }

        BigDecimal canNang = null;
        if (cboThuCung.getValue() != null && cboThuCung.getValue().getCanNang() != null && cboThuCung.getValue().getCanNang().compareTo(BigDecimal.ZERO) > 0) {
            canNang = cboThuCung.getValue().getCanNang();
        }
        double cnVal = canNang != null ? canNang.doubleValue() : 0.0;
        double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), cnVal);
        danhSachDichVuChon.add(new ChiTietLichHen(txtMaLichHen.getText(), dv.getMaDichVu(), dv.getTenDichVu(), canNang, BigDecimal.valueOf(gia)));
        cboDichVu.setValue(null);
    }

    private void xuLyDatLich() {
        try {
            // Nếu người dùng đã chọn dịch vụ trong ComboBox nhưng chưa bấm nút "Thêm vào bảng", tự động thêm vào
            if (cboDichVu.getValue() != null) {
                DichVuChamSoc dvChon = cboDichVu.getValue();
                boolean daCo = danhSachDichVuChon.stream().anyMatch(ct -> ct.getMaDichVu().equals(dvChon.getMaDichVu()));
                if (!daCo) {
                    xuLyThemDichVuVaoBang();
                }
            }

            KhachHang kh = cboKhachHang.getValue();
            ThuCung tc = cboThuCung.getValue();
            String[] nv = cboNhanVien.getValue();
            LocalDate ngay = dpNgayHen.getValue();
            String gioText = cboGioHen.getValue();

            if (kh == null) throw new IllegalArgumentException("Vui lòng chọn khách hàng đặt lịch!");
            if (tc == null) throw new IllegalArgumentException("Vui lòng chọn thú cưng làm dịch vụ!");
            if (nv == null) throw new IllegalArgumentException("Vui lòng chọn nhân viên chăm sóc phụ trách!");
            if (ngay == null) throw new IllegalArgumentException("Vui lòng chọn ngày hẹn dịch vụ!");
            if (gioText == null || gioText.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng chọn giờ hẹn!");
            if (danhSachDichVuChon.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn hoặc thêm ít nhất một dịch vụ chăm sóc vào lịch hẹn!");

            LichHen lh = new LichHen();
            lh.setMaLichHen(txtMaLichHen.getText().trim());
            lh.setMaKhachHang(kh.getMaKhachHang());
            lh.setMaThuCung(tc.getMaThuCung());
            lh.setMaNhanVien(nv[0]);
            lh.setNgayHen(ngay);
            lh.setGioHen(LocalTime.parse(gioText.trim()));
            lh.setTrangThai("Đã đặt");
            lh.setGhiChu(txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : "");

            danhSachDichVuChon.forEach(ct -> ct.setMaLichHen(lh.getMaLichHen()));

            if (controller.datLichHen(lh, danhSachDichVuChon)) {
                hienThiThongBao("Đặt lịch thành công", "✔ Đã đặt lịch hẹn mã: " + lh.getMaLichHen() + " cho bé " + tc.getTenThuCung() + " thành công!");
                if (khiDatThanhCong != null) khiDatThanhCong.run();
                lamMoiForm();
            } else {
                hienThiLoi("Không thể tạo lịch hẹn mới!");
            }
        } catch (Exception ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    public void lamMoiForm() {
        try { txtMaLichHen.setText(controller.taoMaLichHenMoi()); }
        catch (Exception e) { txtMaLichHen.setText("LH-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-000001"); }

        try {
            dsKhachHangGoc.clear();
            dsKhachHangGoc.addAll(controller.layDanhSachKhachHang());
            dangDongBo = true;
            cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
            cboKhachHang.setValue(null);
            txtTimKhach.clear();
            dangDongBo = false;
        } catch (Exception ignored) {}

        try {
            List<String[]> dsNv = controller.layDanhSachNhanVienChamSoc();
            cboNhanVien.setItems(FXCollections.observableArrayList(dsNv));
            dsNv.stream().filter(nv -> nv.length > 2 && "Chăm sóc".equalsIgnoreCase(nv[2].trim())).findFirst()
                    .ifPresentOrElse(cboNhanVien::setValue, () -> { if (!dsNv.isEmpty()) cboNhanVien.setValue(dsNv.get(0)); });
        } catch (Exception ignored) {}

        try {
            cboDichVu.setItems(FXCollections.observableArrayList(controller.layDanhSachDichVu()));
        } catch (Exception ignored) {}

        cboThuCung.getItems().clear();

        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(19, 30))) {
            dpNgayHen.setValue(LocalDate.now().plusDays(1));
            cboGioHen.setValue("09:00");
        } else {
            dpNgayHen.setValue(LocalDate.now());
            cboGioHen.getItems().stream()
                    .filter(g -> LocalTime.parse(g).isAfter(now.plusMinutes(15)))
                    .findFirst().ifPresentOrElse(cboGioHen::setValue, () -> cboGioHen.setValue("09:00"));
        }

        cboDichVu.setValue(null);
        txtGhiChu.clear();
        danhSachDichVuChon.clear();
    }

    private void taiDanhSachThuCung(String maKhachHang) {
        try {
            List<ThuCung> ds = controller.layDanhSachThuCungTheoKhachHang(maKhachHang);
            cboThuCung.setItems(FXCollections.observableArrayList(ds));
            if (!ds.isEmpty()) {
                ThuCung tc = ds.get(ds.size() - 1);
                cboThuCung.setValue(tc);
                if (tc.getDiUng() != null && !tc.getDiUng().trim().isEmpty() && !"Không có".equalsIgnoreCase(tc.getDiUng().trim())) {
                    String luuY = "[Lưu ý dị ứng: " + tc.getDiUng().trim() + "] ";
                    if (!txtGhiChu.getText().contains(luuY)) {
                        txtGhiChu.setText(luuY + txtGhiChu.getText());
                    }
                }
            } else {
                cboThuCung.setValue(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            cboThuCung.getItems().clear();
        }
    }

    private void xuLyNhapSdtKhachHang() {
        String sdt = txtTimKhach.getText() != null ? txtTimKhach.getText().trim() : "";
        if (sdt.isEmpty()) {
            hienThiCanhBao("Vui lòng nhập số điện thoại khách hàng!");
            txtTimKhach.requestFocus();
            return;
        }

        // Bắt buộc phải nhập đúng và đủ 10 chữ số bắt đầu bằng số 0
        if (!sdt.matches("^0\\d{9}$")) {
            hienThiLoi("Số điện thoại không hợp lệ! Vui lòng nhập đúng và đủ 10 chữ số (bắt đầu bằng 0).\n(Hiện tại bạn đã nhập " + sdt.length() + "/10 số: " + sdt + ")");
            txtTimKhach.requestFocus();
            return;
        }

        KhachHang khKhop = dsKhachHangGoc.stream()
                .filter(kh -> kh.getSoDienThoai() != null && kh.getSoDienThoai().trim().equals(sdt))
                .findFirst().orElse(null);

        if (khKhop != null) {
            dangDongBo = true;
            cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
            cboKhachHang.setValue(khKhop);
            txtTimKhach.setText(khKhop.getSoDienThoai());
            dangDongBo = false;

            // Tải danh sách thú cưng của khách hàng khớp SĐT
            taiDanhSachThuCung(khKhop.getMaKhachHang());

            if (cboThuCung.getItems().isEmpty()) {
                Alert hoiThem = new Alert(Alert.AlertType.CONFIRMATION,
                        "Khách hàng " + khKhop.getHoTen() + " (" + khKhop.getSoDienThoai() + ") chưa có hồ sơ thú cưng.\nBạn có muốn thêm nhanh thú cưng cho khách ngay bây giờ không?",
                        new ButtonType("Thêm thú cưng", ButtonBar.ButtonData.YES),
                        new ButtonType("Để sau", ButtonBar.ButtonData.NO));
                hoiThem.setTitle("Thêm thú cưng");
                hoiThem.setHeaderText(null);
                hoiThem.showAndWait().ifPresent(chon -> {
                    if (chon.getButtonData() == ButtonBar.ButtonData.YES) {
                        moCuaSoThemThuCungNhanh();
                    }
                });
            } else {
                cboThuCung.requestFocus();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Khách hàng mới");
            alert.setHeaderText("Khách hàng chưa có trong hệ thống!");
            alert.setContentText("Số điện thoại '" + sdt + "' chưa từng sử dụng dịch vụ tại cửa hàng.\nHệ thống sẽ mở hộp thoại thêm nhanh khách hàng mới.");
            alert.showAndWait();
            moCuaSoThemKhachNhanh(sdt);
        }
    }

    // -------------------------------------------------------------------------
    // HỘP THOẠI PHỤ (THÊM NHANH KHÁCH HÀNG & THÚ CƯNG)
    // -------------------------------------------------------------------------

    private void moCuaSoThemKhachNhanh(String thongTinBanDau) {
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
        luoi.setHgap(10); luoi.setVgap(10);

        TextField txtHoTenMoi = new TextField(); txtHoTenMoi.setPromptText("Họ và tên khách hàng (*)");
        TextField txtSdtMoi = new TextField(); txtSdtMoi.setPromptText("Số điện thoại (10 số, bắt đầu bằng 0) (*)");
        TextField txtDiaChiMoi = new TextField(); txtDiaChiMoi.setPromptText("Địa chỉ (tùy chọn)");

        if (thongTinBanDau != null && !thongTinBanDau.trim().isEmpty()) {
            if (thongTinBanDau.trim().matches("^0?\\d+$")) {
                txtSdtMoi.setText(thongTinBanDau.trim());
                Platform.runLater(txtHoTenMoi::requestFocus);
            } else {
                txtHoTenMoi.setText(thongTinBanDau.trim());
                Platform.runLater(txtSdtMoi::requestFocus);
            }
        }

        // Ràng buộc họ tên: Chỉ cho phép chữ cái tiếng Việt, số và khoảng trắng; chặn hoàn toàn ký tự đặc biệt (!@#$%^&*...)
        txtHoTenMoi.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            String locKyTu = moi.replaceAll("[^\\p{L}0-9\\s]", "");
            if (!locKyTu.equals(moi)) {
                txtHoTenMoi.setText(locKyTu);
            }
        });

        // Khóa cứng SĐT 10 chữ số
        txtSdtMoi.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            if (!moi.matches("\\d*")) txtSdtMoi.setText(cu != null ? cu : "");
            else if (moi.length() > 10) txtSdtMoi.setText(moi.substring(0, 10));
        });

        luoi.add(new Label("Họ tên (*):"), 0, 0); luoi.add(txtHoTenMoi, 1, 0);
        luoi.add(new Label("Số điện thoại (*):"), 0, 1); luoi.add(txtSdtMoi, 1, 1);
        luoi.add(new Label("Địa chỉ:"), 0, 2); luoi.add(txtDiaChiMoi, 1, 2);

        Button btnLuuKhach = new Button("Lưu khách hàng");
        btnLuuKhach.getStyleClass().add("nut-chinh");
        Button btnHuyDlg = new Button("Hủy");
        btnHuyDlg.getStyleClass().add("nut-phu");
        btnHuyDlg.setOnAction(e -> dlg.close());

        btnLuuKhach.setOnAction(e -> {
            String ten = txtHoTenMoi.getText() != null ? txtHoTenMoi.getText().trim() : "";
            String sdt = txtSdtMoi.getText() != null ? txtSdtMoi.getText().trim() : "";
            if (ten.isEmpty()) { hienThiLoi("Họ tên không được để trống!"); return; }
            if (!ten.matches("^[\\p{L}0-9\\s]+$")) {
                hienThiLoi("Họ và tên khách hàng không được chứa ký tự đặc biệt (!@#$%^&*...)!");
                return;
            }
            if (!sdt.matches("^0\\d{9}$")) { hienThiLoi("Số điện thoại phải đúng 10 số bắt đầu bằng 0!"); return; }
            if (controller.kiemTraTrungSoDienThoai(sdt)) { hienThiLoi("Số điện thoại này đã tồn tại!"); return; }

            try {
                KhachHang khMoi = new KhachHang(controller.taoMaKhachHang(), ten, sdt, null, 0, txtDiaChiMoi.getText(), "Đặt qua điện thoại/FB", true);
                if (controller.themKhachHang(khMoi)) {
                    dsKhachHangGoc.add(0, khMoi);
                    cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
                    cboKhachHang.setValue(khMoi);
                    dangDongBo = true;
                    txtTimKhach.setText(sdt);
                    dangDongBo = false;
                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm nhanh khách hàng " + ten + " (" + sdt + ")!");

                    // Gợi ý thêm thú cưng
                    Platform.runLater(() -> {
                        Alert hoi = new Alert(Alert.AlertType.CONFIRMATION, "Khách hàng " + ten + " chưa có thú cưng. Bạn có muốn thêm nhanh thú cưng ngay không?",
                                new ButtonType("Thêm thú cưng", ButtonBar.ButtonData.YES), new ButtonType("Để sau", ButtonBar.ButtonData.NO));
                        hoi.setHeaderText(null);
                        hoi.showAndWait().ifPresent(c -> { if (c.getButtonData() == ButtonBar.ButtonData.YES) moCuaSoThemThuCungNhanh(); });
                    });
                }
            } catch (Exception ex) { hienThiLoi(ex.getMessage()); }
        });

        HBox pnlNutDlg = new HBox(8, btnLuuKhach, btnHuyDlg);
        pnlNutDlg.setAlignment(Pos.CENTER_RIGHT);
        pnlGoc.getChildren().addAll(lblTieuDe, luoi, pnlNutDlg);

        Scene scene = new Scene(pnlGoc, 460, 240);
        if (DatLichHenView.class.getResource(CSS_CHUNG) != null) scene.getStylesheets().add(DatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    private void moCuaSoThemThuCungNhanh() {
        KhachHang kh = cboKhachHang.getValue();
        if (kh == null) { hienThiLoi("Vui lòng chọn khách hàng trước khi thêm thú cưng!"); return; }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) dlg.initOwner(getScene().getWindow());
        dlg.setTitle("Thêm nhanh thú cưng - " + kh.getHoTen());

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM NHANH HỒ SƠ THÚ CƯNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Thêm nhanh hồ sơ thú cưng cho khách hàng: " + kh.getHoTen() + " (" + kh.getSoDienThoai() + ")");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Lưới nhập liệu
        GridPane luoi = new GridPane();
        luoi.setHgap(10);
        luoi.setVgap(10);

        ThuCungController tcCtrl = new ThuCungController();

        TextField txtMaThuCung = new TextField();
        try {
            txtMaThuCung.setText(tcCtrl.taoMaThuCungMoi());
        } catch (Exception e) {
            txtMaThuCung.setText("TC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-000001");
        }
        txtMaThuCung.setEditable(false);

        // Chủ nuôi: Cố định là khách hàng vừa chọn/tạo, chỉ đọc, không có dòng tìm kiếm hay ComboBox
        TextField txtChuNoi = new TextField(kh.getHoTen() + " (" + kh.getSoDienThoai() + ") - " + kh.getMaKhachHang());
        txtChuNoi.setEditable(false);

        TextField txtTenThuCung = new TextField();
        txtTenThuCung.setPromptText("Nhập tên thú cưng (VD: Mochi, Bông, Lu, Corgi...) (*)");
        txtTenThuCung.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null) return;
            String loc = moi.replaceAll("[^\\p{L}0-9\\s]", "");
            if (!loc.equals(moi)) txtTenThuCung.setText(loc);
        });

        ComboBox<String> cboLoai = new ComboBox<>();
        cboLoai.getItems().addAll("Chó", "Mèo", "Thỏ", "Hamster", "Chim", "Khác");
        cboLoai.setValue("Chó");
        cboLoai.setMaxWidth(Double.MAX_VALUE);

        TextField txtLoaiKhac = new TextField();
        txtLoaiKhac.setPromptText("Nhập tên loài cụ thể...");
        txtLoaiKhac.setVisible(false);
        txtLoaiKhac.setManaged(false);

        cboLoai.valueProperty().addListener((obs, cu, moi) -> {
            boolean laKhac = "Khác".equalsIgnoreCase(moi);
            txtLoaiKhac.setVisible(laKhac);
            txtLoaiKhac.setManaged(laKhac);
            if (laKhac) txtLoaiKhac.requestFocus();
        });

        VBox boxLoai = new VBox(4, cboLoai, txtLoaiKhac);

        TextField txtGiongLoai = new TextField();
        txtGiongLoai.setPromptText("VD: Poodle Tiny, Mèo Anh lông ngắn, Corgi...");

        ComboBox<String> cboGioiTinh = new ComboBox<>();
        cboGioiTinh.getItems().addAll("Đực", "Cái");
        cboGioiTinh.setValue("Đực");
        cboGioiTinh.setMaxWidth(Double.MAX_VALUE);

        TextField txtCanNang = new TextField();
        txtCanNang.setPromptText("Tùy chọn (cập nhật sau tại quầy tiếp nhận)");
        txtCanNang.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d{0,2})?")) {
                txtCanNang.setText(cu != null ? cu : "");
            }
        });

        DatePicker dpNgaySinh = new DatePicker();
        dpNgaySinh.setPromptText("dd/MM/yyyy (tùy chọn)");
        dpNgaySinh.setMaxWidth(Double.MAX_VALUE);
        dpNgaySinh.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        TextArea txtDiUng = new TextArea();
        txtDiUng.setPromptText("Ghi chú dị ứng thức ăn/sữa tắm, tính cách của thú cưng...");
        txtDiUng.setPrefRowCount(2);
        txtDiUng.setWrapText(true);

        luoi.add(new Label("Mã thú cưng:"), 0, 0); luoi.add(txtMaThuCung, 1, 0);
        luoi.add(new Label("Chủ nuôi:"), 0, 1); luoi.add(txtChuNoi, 1, 1);
        luoi.add(new Label("Tên thú cưng (*):"), 0, 2); luoi.add(txtTenThuCung, 1, 2);
        luoi.add(new Label("Loài thú cưng:"), 0, 3); luoi.add(boxLoai, 1, 3);
        luoi.add(new Label("Giống loài:"), 0, 4); luoi.add(txtGiongLoai, 1, 4);
        luoi.add(new Label("Giới tính (*):"), 0, 5); luoi.add(cboGioiTinh, 1, 5);
        luoi.add(new Label("Cân nặng (kg):"), 0, 6); luoi.add(txtCanNang, 1, 6);
        luoi.add(new Label("Ngày sinh:"), 0, 7); luoi.add(dpNgaySinh, 1, 7);
        luoi.add(new Label("Dị ứng / Ghi chú:"), 0, 8); luoi.add(txtDiUng, 1, 8);

        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        luoi.getColumnConstraints().addAll(col1, col2);

        VBox pnlHop = new VBox(10, luoi);
        pnlHop.getStyleClass().add("hop-noi-dung");

        ScrollPane spCuon = new ScrollPane(pnlHop);
        spCuon.setFitToWidth(true);
        spCuon.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spCuon.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(spCuon, Priority.ALWAYS);

        // Nút lưu & Đóng cố định dưới đáy (không bị che khuất)
        Button btnLuu = new Button("Lưu thú cưng");
        btnLuu.getStyleClass().add("nut-chinh");
        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> dlg.close());

        btnLuu.setOnAction(e -> {
            String ten = txtTenThuCung.getText() != null ? txtTenThuCung.getText().trim() : "";
            if (ten.isEmpty()) {
                hienThiCanhBao("Vui lòng nhập tên cho thú cưng!");
                txtTenThuCung.requestFocus();
                return;
            }
            if (!ten.matches("^[\\p{L}0-9\\s]+$")) {
                hienThiCanhBao("Tên thú cưng không được chứa ký tự đặc biệt!");
                txtTenThuCung.requestFocus();
                return;
            }

            BigDecimal canNang = null;
            String cnStr = txtCanNang.getText() != null ? txtCanNang.getText().trim() : "";
            if (!cnStr.isEmpty()) {
                try {
                    canNang = new BigDecimal(cnStr);
                    if (canNang.compareTo(BigDecimal.ZERO) <= 0) {
                        hienThiCanhBao("Cân nặng thú cưng phải lớn hơn 0 kg!");
                        txtCanNang.requestFocus();
                        return;
                    }
                } catch (Exception ex) {
                    hienThiCanhBao("Cân nặng phải là số hợp lệ (VD: 3.5)!");
                    txtCanNang.requestFocus();
                    return;
                }
            }

            LocalDate ns = dpNgaySinh.getValue();
            if (ns != null && ns.isAfter(LocalDate.now())) {
                hienThiCanhBao("Ngày sinh không được ở tương lai!");
                dpNgaySinh.requestFocus();
                return;
            }

            String loai = cboLoai.getValue();
            if ("Khác".equalsIgnoreCase(loai)) {
                String lk = txtLoaiKhac.getText() != null ? txtLoaiKhac.getText().trim() : "";
                if (lk.isEmpty()) {
                    hienThiCanhBao("Vui lòng nhập loài thú cưng cụ thể!");
                    txtLoaiKhac.requestFocus();
                    return;
                }
                loai = lk;
            }

            ThuCung tc = new ThuCung();
            tc.setMaThuCung(txtMaThuCung.getText().trim());
            tc.setMaKhachHang(kh.getMaKhachHang());
            tc.setTenThuCung(ten);
            tc.setLoaiThuCung(loai);
            tc.setGiongLoai(txtGiongLoai.getText() != null ? txtGiongLoai.getText().trim() : "");
            tc.setGioiTinh(cboGioiTinh.getValue());
            tc.setCanNang(canNang);
            tc.setNgaySinh(ns);
            tc.setDiUng(txtDiUng.getText() != null ? txtDiUng.getText().trim() : "");

            try {
                if (tcCtrl.themThuCung(tc)) {
                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm nhanh hồ sơ thú cưng '" + tc.getTenThuCung() + "' cho khách hàng '" + kh.getHoTen() + "' thành công!");
                    taiDanhSachThuCung(kh.getMaKhachHang());
                    cboThuCung.getItems().stream()
                            .filter(t -> t.getMaThuCung().equals(tc.getMaThuCung()))
                            .findFirst().ifPresent(cboThuCung::setValue);
                }
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi thêm thú cưng: " + ex.getMessage());
            }
        });

        HBox pnlNut = new HBox(8, btnLuu, btnHuy);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        pnlGoc.getChildren().addAll(pnlTieuDe, spCuon, pnlNut);

        Scene scene = new Scene(pnlGoc, 500, 480);
        if (DatLichHenView.class.getResource(CSS_CHUNG) != null) scene.getStylesheets().add(DatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        dlg.setScene(scene);
        dlg.setMinWidth(460);
        dlg.setMinHeight(400);
        dlg.showAndWait();
    }

    private static <T> StringConverter<T> taoConverter(Function<T, String> fn) {
        return new StringConverter<>() {
            @Override public String toString(T obj) { return obj != null ? fn.apply(obj) : ""; }
            @Override public T fromString(String s) { return null; }
        };
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, noiDung);
        alert.setTitle(tieuDe); alert.setHeaderText(null); alert.showAndWait();
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING, noiDung);
        alert.setTitle("Cảnh báo"); alert.setHeaderText(null); alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR, noiDung);
        alert.setTitle("Thông báo lỗi"); alert.setHeaderText(null); alert.showAndWait();
    }
}
