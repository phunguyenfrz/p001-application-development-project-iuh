package com.petshop.gui.lichhen;

import com.petshop.bus.LichHenBUS;
import com.petshop.bus.ThuCungBUS;
import com.petshop.entity.ChiTietLichHen;
import com.petshop.entity.DichVuChamSoc;
import com.petshop.entity.KhachHang;
import com.petshop.entity.LichHen;
import com.petshop.entity.NhanVien;
import com.petshop.entity.TaiKhoan;
import com.petshop.entity.ThuCung;
import com.petshop.util.SessionManager;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Phân hệ Đặt lịch hẹn dịch vụ chăm sóc thú cưng (APT_CREATE / LH_TAO).
 * Tuân thủ 100% Bộ chỉ thị kỹ thuật IUH:
 * - Tra cứu SĐT trực tiếp dưới CSDL, không nạp RAM ComboBox khách hàng.
 * - Tự động gán nhân viên phụ trách từ phiên đăng nhập SessionManager.
 * - Chặn chọn ngày/giờ trong quá khứ.
 * - Sử dụng kiểu nguyên thủy double, không BigDecimal.
 */
public class DatLichHenView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/gui/css/chung.css";

    private final LichHenBUS controller = new LichHenBUS();
    private final Runnable khiDatThanhCong;

    // Đối tượng khách hàng và nhân viên hiện tại
    private KhachHang khachHangHienTai = null;
    private NhanVien nhanVienHienTai = null;

    // Danh sách dịch vụ được chọn trong lịch hẹn
    private final ObservableList<ChiTietLichHen> danhSachDichVuChon = FXCollections.observableArrayList();

    // Điều khiển thông tin lịch hẹn
    private final TextField txtMaLichHen = new TextField();
    private final TextField txtTimKhach = new TextField();
    private final Button btnTimSdt = new Button("Tra cứu");
    private final Button btnThemKhachNhanh = new Button("+ Khách mới");
    private final TextField txtKhachHangHienThi = new TextField();

    private final ComboBox<ThuCung> cboThuCung = new ComboBox<>();
    private final Button btnThemThuCungNhanh = new Button("+ Mới");

    private final TextField txtNhanVienHienThi = new TextField();
    private final DatePicker dpNgayHen = new DatePicker();
    private final ComboBox<String> cboGioHen = new ComboBox<>();
    private final TextArea txtGhiChu = new TextArea();

    // Điều khiển dịch vụ chăm sóc
    private final ComboBox<DichVuChamSoc> cboDichVu = new ComboBox<>();
    private final Button btnThemDichVu = new Button("+ Thêm vào bảng");
    private final TableView<ChiTietLichHen> tblDichVu = new TableView<>();

    // Nút chức năng
    private final Button btnLuu = new Button("Xác nhận đặt lịch");
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
        Label lblMoTa = new Label("Tiếp nhận đặt lịch hẹn chăm sóc thú cưng cho khách hàng (tra cứu SĐT trực tiếp, chặn ngày quá khứ).");
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

        // Bố cục 2 nửa đối xứng (Trái 50% - Phải 50%)
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

        ColumnConstraints cTrai1 = new ColumnConstraints(135);
        ColumnConstraints cTrai2 = new ColumnConstraints();
        cTrai2.setHgrow(Priority.ALWAYS);
        cTrai2.setFillWidth(true);
        pnlCotTrai.getColumnConstraints().addAll(cTrai1, cTrai2);

        txtMaLichHen.setEditable(false);
        txtMaLichHen.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
        txtMaLichHen.setMaxWidth(Double.MAX_VALUE);

        // SĐT khách hàng + Nút Tra cứu + Nút + Khách mới
        txtTimKhach.setPromptText("Nhập SĐT (10 số) rồi bấm Enter...");
        txtTimKhach.setMaxWidth(Double.MAX_VALUE);
        txtTimKhach.setOnAction(e -> xuLyNhapSdtKhachHang());

        btnTimSdt.getStyleClass().add("nut-phu");
        btnTimSdt.setMinWidth(Region.USE_PREF_SIZE);
        btnTimSdt.setOnAction(e -> xuLyNhapSdtKhachHang());

        btnThemKhachNhanh.getStyleClass().add("nut-phu");
        btnThemKhachNhanh.setMinWidth(Region.USE_PREF_SIZE);
        btnThemKhachNhanh.setOnAction(e -> moCuaSoThemKhachNhanh(txtTimKhach.getText()));

        HBox pnlSdt = new HBox(6, txtTimKhach, btnTimSdt, btnThemKhachNhanh);
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
        pnlCotTrai.add(new Label("SĐT khách (*):"), 0, 1);
        pnlCotTrai.add(pnlSdt, 1, 1);
        pnlCotTrai.add(new Label("Thú cưng (*):"), 0, 2);
        pnlCotTrai.add(pnlChonThuCung, 1, 2);

        // --- CỘT PHẢI (NV phụ trách, Khách hàng hiển thị, Ngày & Giờ) ---
        GridPane pnlCotPhai = new GridPane();
        pnlCotPhai.setHgap(10);
        pnlCotPhai.setVgap(8);
        pnlCotPhai.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints cPhai1 = new ColumnConstraints(135);
        ColumnConstraints cPhai2 = new ColumnConstraints();
        cPhai2.setHgrow(Priority.ALWAYS);
        cPhai2.setFillWidth(true);
        pnlCotPhai.getColumnConstraints().addAll(cPhai1, cPhai2);

        txtNhanVienHienThi.setEditable(false);
        txtNhanVienHienThi.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569;");
        txtNhanVienHienThi.setMaxWidth(Double.MAX_VALUE);

        txtKhachHangHienThi.setEditable(false);
        txtKhachHangHienThi.setPromptText("Chưa chọn khách hàng");
        txtKhachHangHienThi.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #0f172a; -fx-font-weight: bold;");
        txtKhachHangHienThi.setMaxWidth(Double.MAX_VALUE);

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

        pnlCotPhai.add(new Label("NV phụ trách:"), 0, 0);
        pnlCotPhai.add(txtNhanVienHienThi, 1, 0);
        pnlCotPhai.add(new Label("Khách hàng:"), 0, 1);
        pnlCotPhai.add(txtKhachHangHienThi, 1, 1);
        pnlCotPhai.add(new Label("Ngày & Giờ (*):"), 0, 2);
        pnlCotPhai.add(pnlThoiGian, 1, 2);

        // Đưa 2 cột vào pnlHaiCot
        GridPane.setHgrow(pnlCotTrai, Priority.ALWAYS);
        GridPane.setHgrow(pnlCotPhai, Priority.ALWAYS);
        pnlHaiCot.add(pnlCotTrai, 0, 0);
        pnlHaiCot.add(pnlCotPhai, 1, 0);

        // HÀNG DƯỚI: Ghi chú / Lưu ý
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
        colMaDV.setPrefWidth(130);
        colMaDV.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaDichVu()));

        TableColumn<ChiTietLichHen, String> colTenDV = new TableColumn<>("Tên dịch vụ chăm sóc");
        colTenDV.setStyle("-fx-alignment: CENTER-LEFT;");
        colTenDV.setPrefWidth(300);
        colTenDV.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDichVu()));

        TableColumn<ChiTietLichHen, String> colThuCung = new TableColumn<>("Thú cưng");
        colThuCung.setStyle("-fx-alignment: CENTER;");
        colThuCung.setPrefWidth(150);
        colThuCung.setCellValueFactory(c -> {
            String ten = c.getValue().getTenThuCung();
            if ((ten == null || ten.isEmpty()) && cboThuCung.getValue() != null) {
                ten = cboThuCung.getValue().getTenThuCung();
            }
            return new SimpleStringProperty(ten != null && !ten.isEmpty() ? ten : "-");
        });

        TableColumn<ChiTietLichHen, String> colDonGia = new TableColumn<>("Đơn giá dự kiến");
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDonGia.setPrefWidth(130);
        colDonGia.setCellValueFactory(c -> {
            double gia = c.getValue().getDonGia();
            return new SimpleStringProperty(gia > 0 ? String.format("%,.0f đ", gia) : "Báo giá tại quầy");
        });

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

        tblDichVu.getColumns().setAll(List.of(colMaDV, colTenDV, colThuCung, colDonGia, colXoa));
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

        Label lblNhacNho = new Label("💡 Bấm nút 'Xác nhận đặt lịch' để lưu vào hệ thống");
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
    // ĐĂNG KÝ SỰ KIỆN
    // -------------------------------------------------------------------------

    private void dangKySuKien() {
        cboDichVu.setConverter(new StringConverter<>() {
            @Override
            public String toString(DichVuChamSoc dv) {
                return dv != null ? dv.getTenDichVu() + " (" + dv.getThoiLuong() + " phút)" : "";
            }

            @Override
            public DichVuChamSoc fromString(String string) {
                return null;
            }
        });

        cboThuCung.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThuCung tc) {
                if (tc == null) return "";
                String loai = tc.getLoaiThuCung() != null ? tc.getLoaiThuCung() : "Thú cưng";
                String giong = tc.getGiongLoai() != null && !tc.getGiongLoai().isEmpty() ? " - " + tc.getGiongLoai() : "";
                return tc.getTenThuCung() + " [" + loai + giong + "]";
            }

            @Override
            public ThuCung fromString(String string) {
                return null;
            }
        });

        // Khóa cứng ô tìm khách: chỉ cho nhập số, tối đa 10 số
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

        // Khi chọn thú cưng: ghi chú dị ứng nếu có (không ghi đè các dòng dịch vụ của bé khác đã thêm)
        cboThuCung.setOnAction(e -> {
            ThuCung tc = cboThuCung.getValue();
            if (tc != null) {
                if (tc.getDiUng() != null && !tc.getDiUng().trim().isEmpty() && !"Không có".equalsIgnoreCase(tc.getDiUng().trim())) {
                    String luuY = "[Lưu ý dị ứng: " + tc.getDiUng().trim() + "] ";
                    if (!txtGhiChu.getText().contains(luuY)) {
                        txtGhiChu.setText(luuY + txtGhiChu.getText());
                    }
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
        if (dv == null) {
            hienThiCanhBao("Vui lòng chọn dịch vụ chăm sóc!");
            return;
        }

        if (danhSachDichVuChon.stream().anyMatch(ct -> ct.getMaDichVu().equals(dv.getMaDichVu()))) {
            hienThiCanhBao("Dịch vụ '" + dv.getTenDichVu() + "' đã có trong lịch hẹn!");
            return;
        }

        ThuCung tc = cboThuCung.getValue();
        if (tc == null) {
            hienThiCanhBao("Vui lòng chọn thú cưng trước khi thêm dịch vụ vào lịch hẹn!");
            cboThuCung.requestFocus();
            return;
        }

        // Kiểm tra dịch vụ đã có cho bé này chưa (cho phép bé khác chọn cùng dịch vụ)
        boolean daCoChoBe = danhSachDichVuChon.stream().anyMatch(ct ->
                ct.getMaDichVu().equals(dv.getMaDichVu()) &&
                ((ct.getThuCung() != null && ct.getThuCung().getMaThuCung().equals(tc.getMaThuCung())) ||
                 (ct.getTenThuCung() != null && ct.getTenThuCung().equalsIgnoreCase(tc.getTenThuCung())))
        );
        if (daCoChoBe) {
            hienThiCanhBao("Dịch vụ '" + dv.getTenDichVu() + "' đã có trong lịch hẹn cho bé " + tc.getTenThuCung() + "!");
            return;
        }

        double canNang = tc.getCanNang() > 0 ? tc.getCanNang() : 0.0;
        double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang > 0 ? canNang : 1.0);
        ChiTietLichHen ct = new ChiTietLichHen(txtMaLichHen.getText(), dv.getMaDichVu(), dv.getTenDichVu(), canNang, gia);
        ct.setThuCung(tc);
        ct.setTenThuCung(tc.getTenThuCung());
        danhSachDichVuChon.add(ct);
        cboDichVu.setValue(null);
    }

    private void xuLyDatLich() {
        try {
            if (cboDichVu.getValue() != null) {
                DichVuChamSoc dvChon = cboDichVu.getValue();
                boolean daCo = danhSachDichVuChon.stream().anyMatch(ct -> ct.getMaDichVu().equals(dvChon.getMaDichVu()));
                if (!daCo) {
                    xuLyThemDichVuVaoBang();
                }
            }

            if (khachHangHienTai == null) {
                throw new IllegalArgumentException("Vui lòng tra cứu hoặc thêm khách hàng đặt lịch!");
            }

            ThuCung tc = cboThuCung.getValue();
            if (tc == null) {
                throw new IllegalArgumentException("Vui lòng chọn thú cưng làm dịch vụ!");
            }

            if (nhanVienHienTai == null) {
                throw new IllegalArgumentException("Không xác định được nhân viên phụ trách từ phiên đăng nhập!");
            }

            LocalDate ngay = dpNgayHen.getValue();
            if (ngay == null) {
                throw new IllegalArgumentException("Vui lòng chọn ngày hẹn dịch vụ!");
            }

            String gioText = cboGioHen.getValue();
            if (gioText == null || gioText.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn giờ hẹn dịch vụ!");
            }

            if (danhSachDichVuChon.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một dịch vụ chăm sóc vào lịch hẹn!");
            }

            LocalTime gio = LocalTime.parse(gioText.trim());

            // Gom nhóm dịch vụ theo từng bé thú cưng
            java.util.Map<String, java.util.List<ChiTietLichHen>> nhomTheoPet = new java.util.LinkedHashMap<>();
            java.util.Map<String, ThuCung> mapPet = new java.util.LinkedHashMap<>();
            for (ChiTietLichHen ct : danhSachDichVuChon) {
                ThuCung pet = ct.getThuCung() != null ? ct.getThuCung() : tc;
                String maTc = pet.getMaThuCung();
                nhomTheoPet.computeIfAbsent(maTc, k -> new java.util.ArrayList<>()).add(ct);
                mapPet.put(maTc, pet);
            }

            java.util.List<String> dsMaThanhCong = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, java.util.List<ChiTietLichHen>> entry : nhomTheoPet.entrySet()) {
                ThuCung pet = mapPet.get(entry.getKey());
                java.util.List<ChiTietLichHen> dsCtCuaPet = entry.getValue();

                LichHen lh = new LichHen();
                // Nếu chỉ có 1 bé: dùng mã trên form, nếu nhiều bé: sinh mã tự động tiếp theo
                if (nhomTheoPet.size() == 1 && txtMaLichHen.getText() != null && !txtMaLichHen.getText().trim().isEmpty()) {
                    lh.setMaLichHen(txtMaLichHen.getText().trim());
                } else {
                    lh.setMaLichHen(controller.taoMaLichHenMoi());
                }
                lh.setKhachHang(khachHangHienTai);
                lh.setThuCung(pet);
                lh.setNhanVien(nhanVienHienTai);
                lh.setNgayHen(ngay);
                lh.setGioHen(gio);
                lh.setTrangThai("Đã đặt");
                lh.setGhiChu(txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : "");

                for (ChiTietLichHen ct : dsCtCuaPet) {
                    ct.setMaLichHen(lh.getMaLichHen());
                    if (ct.getCanNangThuCung() <= 0 && pet.getCanNang() > 0) {
                        ct.setCanNangThuCung(pet.getCanNang());
                        double gia = controller.traGiaDichVuTheoCanNang(ct.getMaDichVu(), pet.getCanNang());
                        if (gia > 0) ct.setDonGia(gia);
                    }
                }

                if (controller.datLichHen(lh, new java.util.ArrayList<>(dsCtCuaPet))) {
                    dsMaThanhCong.add(lh.getMaLichHen() + " (Bé " + pet.getTenThuCung() + " - " + dsCtCuaPet.size() + " dịch vụ)");
                } else {
                    throw new IllegalStateException("Không thể tạo lịch hẹn cho bé " + pet.getTenThuCung() + "!");
                }
            }

            if (!dsMaThanhCong.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            if (dsMaThanhCong.size() == 1) {
                sb.append("Đã đặt lịch hẹn mã: ").append(dsMaThanhCong.get(0)).append(" thành công!");
            } else {
                sb.append("Đã đặt lịch hẹn thành công cho ").append(dsMaThanhCong.size()).append(" bé thú cưng:\n");
                for (String ma : dsMaThanhCong) {
                    sb.append("• ").append(ma).append("\n");
                }
            }
            hienThiThongBao("Đặt lịch thành công", sb.toString());
            if (khiDatThanhCong != null) {
                khiDatThanhCong.run();
            }
            lamMoiForm();
        }
        } catch (IllegalArgumentException ex) {
            hienThiCanhBao(ex.getMessage() != null ? ex.getMessage() : "Dữ liệu không hợp lệ!");
        } catch (Throwable ex) {
            ex.printStackTrace();
            hienThiLoi(ex.getMessage() != null ? ex.getMessage() : ex.toString());
        }
    }

    public void lamMoiForm() {
        try {
            txtMaLichHen.setText(controller.taoMaLichHenMoi());
        } catch (Exception e) {
            txtMaLichHen.setText("LH-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-000001");
        }

        // Tự động gán nhân viên phụ trách từ phiên đăng nhập
        TaiKhoan tk = SessionManager.getTaiKhoanHienTai();
        if (tk != null && tk.getNhanVien() != null) {
            nhanVienHienTai = tk.getNhanVien();
            txtNhanVienHienThi.setText(nhanVienHienTai.getHoTen() + " (" + nhanVienHienTai.getMaNhanVien() + ")");
        } else {
            // Mặc định tìm nhân viên đầu tiên trong danh sách chăm sóc nếu chưa có phiên
            try {
                List<String[]> dsNv = controller.layDanhSachNhanVienChamSoc();
                if (!dsNv.isEmpty()) {
                    nhanVienHienTai = new NhanVien();
                    nhanVienHienTai.setMaNhanVien(dsNv.get(0)[0]);
                    nhanVienHienTai.setHoTen(dsNv.get(0)[1]);
                    txtNhanVienHienThi.setText(nhanVienHienTai.getHoTen() + " (" + nhanVienHienTai.getMaNhanVien() + ")");
                }
            } catch (Exception ignored) {}
        }

        khachHangHienTai = null;
        txtKhachHangHienThi.setText("Chưa chọn khách hàng");
        txtTimKhach.clear();
        cboThuCung.getItems().clear();

        try {
            cboDichVu.setItems(FXCollections.observableArrayList(controller.layDanhSachDichVu()));
        } catch (Exception ignored) {}

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

        if (!sdt.matches("^0\\d{9}$")) {
            hienThiCanhBao("Số điện thoại không hợp lệ! Vui lòng nhập đúng và đủ 10 chữ số (bắt đầu bằng số 0).\n(Hiện tại bạn đã nhập " + sdt.length() + "/10 số: " + sdt + ")");
            txtTimKhach.requestFocus();
            return;
        }

        try {
            KhachHang khKhop = controller.timKhachHangTheoSoDienThoai(sdt);
            if (khKhop != null) {
                khachHangHienTai = khKhop;
                txtKhachHangHienThi.setText(khKhop.getHoTen() + " (" + khKhop.getSoDienThoai() + ")");
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
                khachHangHienTai = null;
                txtKhachHangHienThi.setText("Chưa chọn khách hàng");
                cboThuCung.getItems().clear();
                hienThiCanhBao("Chưa có khách hàng với số điện thoại '" + sdt + "' trong hệ thống!\nVui lòng nhấn nút '+ Khách mới' để thêm khách hàng.");
                txtTimKhach.requestFocus();
            }
        } catch (IllegalArgumentException ex) {
            hienThiCanhBao(ex.getMessage() != null ? ex.getMessage() : "Dữ liệu không hợp lệ!");
        } catch (Throwable ex) {
            ex.printStackTrace();
            hienThiLoi(ex.getMessage() != null ? ex.getMessage() : ex.toString());
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

        // Ràng buộc họ tên: chữ cái tiếng Việt, số và khoảng trắng
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
            if (ten.isEmpty()) { hienThiCanhBao("Họ tên không được để trống!"); return; }
            if (!ten.matches("^[\\p{L}0-9\\s]+$")) {
                hienThiCanhBao("Họ và tên khách hàng không được chứa ký tự đặc biệt!");
                return;
            }
            if (!sdt.matches("^0\\d{9}$")) { hienThiCanhBao("Số điện thoại phải đúng 10 số bắt đầu bằng 0!"); return; }
            if (controller.kiemTraTrungSoDienThoai(sdt)) { hienThiCanhBao("Số điện thoại này đã tồn tại!"); return; }

            try {
                KhachHang khMoi = new KhachHang(controller.taoMaKhachHang(), ten, sdt, null, 0, txtDiaChiMoi.getText(), "Đặt qua điện thoại/FB", true);
                if (controller.themKhachHang(khMoi)) {
                    khachHangHienTai = khMoi;
                    txtKhachHangHienThi.setText(khMoi.getHoTen() + " (" + khMoi.getSoDienThoai() + ")");
                    txtTimKhach.setText(sdt);
                    taiDanhSachThuCung(khMoi.getMaKhachHang());

                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm nhanh khách hàng " + ten + " (" + sdt + ")!");

                    Platform.runLater(() -> {
                        Alert hoi = new Alert(Alert.AlertType.CONFIRMATION, "Khách hàng " + ten + " chưa có thú cưng. Bạn có muốn thêm nhanh thú cưng ngay không?",
                                new ButtonType("Thêm thú cưng", ButtonBar.ButtonData.YES), new ButtonType("Để sau", ButtonBar.ButtonData.NO));
                        hoi.setHeaderText(null);
                        hoi.showAndWait().ifPresent(c -> {
                            if (c.getButtonData() == ButtonBar.ButtonData.YES) {
                                moCuaSoThemThuCungNhanh();
                            }
                        });
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
        if (khachHangHienTai == null) {
            hienThiCanhBao("Vui lòng tra cứu hoặc thêm khách hàng trước khi thêm thú cưng!");
            return;
        }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) dlg.initOwner(getScene().getWindow());
        dlg.setTitle("Thêm nhanh thú cưng - " + khachHangHienTai.getHoTen());

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM NHANH HỒ SƠ THÚ CƯNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Thêm nhanh hồ sơ thú cưng cho khách hàng: " + khachHangHienTai.getHoTen() + " (" + khachHangHienTai.getSoDienThoai() + ")");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        GridPane luoi = new GridPane();
        luoi.setHgap(10);
        luoi.setVgap(10);

        ThuCungBUS tcCtrl = new ThuCungBUS();

        TextField txtMaThuCung = new TextField();
        try {
            txtMaThuCung.setText(tcCtrl.taoMaThuCungMoi());
        } catch (Exception e) {
            txtMaThuCung.setText("TC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-000001");
        }
        txtMaThuCung.setEditable(false);

        TextField txtChuNoi = new TextField(khachHangHienTai.getHoTen() + " (" + khachHangHienTai.getSoDienThoai() + ") - " + khachHangHienTai.getMaKhachHang());
        txtChuNoi.setEditable(false);

        TextField txtTenThuCung = new TextField();
        txtTenThuCung.setPromptText("Nhập tên thú cưng (VD: Mochi, Bống, Lu, Corgi...) (*)");
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

        Button btnLuuTc = new Button("Lưu thú cưng");
        btnLuuTc.getStyleClass().add("nut-chinh");
        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> dlg.close());

        btnLuuTc.setOnAction(e -> {
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

            double canNang = 0.0;
            String cnStr = txtCanNang.getText() != null ? txtCanNang.getText().trim() : "";
            if (!cnStr.isEmpty()) {
                try {
                    canNang = Double.parseDouble(cnStr);
                    if (canNang <= 0) {
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
            tc.setKhachHang(khachHangHienTai);
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
                    hienThiThongBao("Thành công", "Đã thêm nhanh hồ sơ thú cưng '" + tc.getTenThuCung() + "' cho khách hàng '" + khachHangHienTai.getHoTen() + "' thành công!");
                    taiDanhSachThuCung(khachHangHienTai.getMaKhachHang());
                }
            } catch (Exception ex) {
                hienThiLoi(ex.getMessage());
            }
        });

        HBox pnlNut = new HBox(8, btnLuuTc, btnHuy);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        pnlGoc.getChildren().addAll(pnlTieuDe, spCuon, pnlNut);

        Scene sc = new Scene(pnlGoc, 500, 540);
        if (DatLichHenView.class.getResource(CSS_CHUNG) != null) sc.getStylesheets().add(DatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        dlg.setScene(sc);
        dlg.showAndWait();
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
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
