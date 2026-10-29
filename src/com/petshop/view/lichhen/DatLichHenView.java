package com.petshop.view.lichhen;

import com.petshop.controller.LichHenController;
import com.petshop.model.*;
import com.petshop.view.thucung.ThemThuCungView;
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
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    private final LichHenController controller = new LichHenController();
    private final Runnable khiDatThanhCong;
    private boolean laHopThoaiModal = false;
    private boolean dangDongBo = false;

    // Danh sách dữ liệu liên kết
    private final List<KhachHang> dsKhachHangGoc = new ArrayList<>();
    private final ObservableList<ChiTietLichHen> danhSachDichVuChon = FXCollections.observableArrayList();

    // Điều khiển thông tin lịch hẹn
    private final TextField txtMaLichHen = new TextField();
    private final TextField txtTimKhach = new TextField();
    private final Button btnTimSdt = new Button("Kiểm tra");
    private final ComboBox<KhachHang> cboKhachHang = new ComboBox<>();
    private final Button btnThemKhachNhanh = new Button("+ Mới");

    private final ComboBox<ThuCung> cboThuCung = new ComboBox<>();
    private final Button btnThemThuCungNhanh = new Button("+ Mới");

    private final ComboBox<String[]> cboNhanVien = new ComboBox<>();
    private final DatePicker dpNgayHen = new DatePicker();
    private final ComboBox<String> cboGioHen = new ComboBox<>();
    private final TextArea txtGhiChu = new TextArea();

    // Điều khiển dịch vụ chăm sóc
    private final ComboBox<DichVuChamSoc> cboDichVu = new ComboBox<>();
    private final TextField txtCanNangThuCung = new TextField();
    private final TextField txtDonGia = new TextField();
    private final Button btnThemDichVu = new Button("Thêm dịch vụ");
    private final TableView<ChiTietLichHen> tblDichVu = new TableView<>();
    private final Label lblTongTien = new Label("Tổng tiền dự kiến (tạm tính): 0 đ");

    // Nút chức năng
    private final Button btnLuu = new Button("✔ Xác nhận đặt lịch");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

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
        VBox pnlCuon = new VBox(10, taoKhungThongTinLich(), taoKhungChonDichVu());
        ScrollPane spCuon = new ScrollPane(pnlCuon);
        spCuon.setFitToWidth(true);
        spCuon.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spCuon.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        spCuon.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
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

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(8);

        txtMaLichHen.setEditable(false);
        txtMaLichHen.setStyle("-fx-background-color: #f1f5f9; -fx-font-weight: bold;");

        cboNhanVien.setPromptText("Chọn nhân viên chăm sóc");
        cboNhanVien.setMaxWidth(Double.MAX_VALUE);

        // Hàng 1 - Cột trái: SĐT khách hàng + Nút Kiểm tra
        txtTimKhach.setPromptText("Nhập SĐT (10 số) rồi bấm Enter...");
        txtTimKhach.setOnAction(e -> xuLyNhapSdtKhachHang());

        btnTimSdt.getStyleClass().add("nut-phu");
        btnTimSdt.setMinWidth(Region.USE_PREF_SIZE);
        btnTimSdt.setOnAction(e -> xuLyNhapSdtKhachHang());

        HBox pnlSdt = new HBox(6, txtTimKhach, btnTimSdt);
        pnlSdt.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtTimKhach, Priority.ALWAYS);

        // Hàng 1 - Cột phải: ComboBox Khách hàng (trọn cột 3)
        cboKhachHang.setPromptText("Chọn khách hàng");
        cboKhachHang.setMaxWidth(Double.MAX_VALUE);

        // Hàng 2 - Cột trái: Thú cưng + Nút "+ Mới"
        cboThuCung.setPromptText("Chọn thú cưng");
        cboThuCung.setMaxWidth(Double.MAX_VALUE);

        btnThemThuCungNhanh.getStyleClass().add("nut-phu");
        btnThemThuCungNhanh.setMinWidth(Region.USE_PREF_SIZE);
        btnThemThuCungNhanh.setOnAction(e -> moCuaSoThemThuCungNhanh());

        btnThemKhachNhanh.setMinWidth(Region.USE_PREF_SIZE);

        HBox pnlChonThuCung = new HBox(6, cboThuCung, btnThemThuCungNhanh);
        pnlChonThuCung.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cboThuCung, Priority.ALWAYS);

        // Hàng 2 - Cột phải: Ngày & Giờ
        dpNgayHen.setPromptText("dd/MM/yyyy");
        dpNgayHen.setPrefWidth(145);
        dpNgayHen.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #94a3b8;");
                }
            }
        });

        cboGioHen.getItems().clear();
        for (int h = 8; h <= 20; h++) {
            cboGioHen.getItems().add(String.format("%02d:00", h));
            if (h < 20) cboGioHen.getItems().add(String.format("%02d:30", h));
        }
        cboGioHen.setValue("09:00");
        cboGioHen.setPrefWidth(115);

        Label lblGio = new Label("Giờ:");
        lblGio.setMinWidth(Region.USE_PREF_SIZE);

        HBox pnlThoiGian = new HBox(8, dpNgayHen, lblGio, cboGioHen);
        pnlThoiGian.setAlignment(Pos.CENTER_LEFT);

        // Hàng 3: Ghi chú / Lưu ý trải ngang 3 cột
        txtGhiChu.setPromptText("Ghi chú thêm (yêu cầu riêng của khách hàng, lưu ý thú cưng nhát nước...)");
        txtGhiChu.setPrefRowCount(1);
        txtGhiChu.setPrefHeight(36);
        txtGhiChu.setMaxHeight(36);
        txtGhiChu.setWrapText(true);

        // Sắp xếp vào lưới 4 hàng rõ ràng, cân đối
        // Hàng 0:
        pnlLuoi.add(new Label("Mã lịch hẹn:"), 0, 0);
        pnlLuoi.add(txtMaLichHen, 1, 0);
        pnlLuoi.add(new Label("NV phụ trách (*):"), 2, 0);
        pnlLuoi.add(cboNhanVien, 3, 0);

        // Hàng 1:
        pnlLuoi.add(new Label("SĐT khách hàng (*):"), 0, 1);
        pnlLuoi.add(pnlSdt, 1, 1);
        pnlLuoi.add(new Label("Khách hàng (*):"), 2, 1);
        pnlLuoi.add(cboKhachHang, 3, 1);

        // Hàng 2:
        pnlLuoi.add(new Label("Thú cưng (*):"), 0, 2);
        pnlLuoi.add(pnlChonThuCung, 1, 2);
        pnlLuoi.add(new Label("Ngày & Giờ (*):"), 2, 2);
        pnlLuoi.add(pnlThoiGian, 3, 2);

        // Hàng 3:
        pnlLuoi.add(new Label("Ghi chú / Lưu ý:"), 0, 3);
        pnlLuoi.add(txtGhiChu, 1, 3);
        GridPane.setColumnSpan(txtGhiChu, 3);

        // Cấu hình ColumnConstraints chuẩn để nhãn và control thoáng, không bị cắt chữ
        ColumnConstraints c1 = new ColumnConstraints(135);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints(150);
        ColumnConstraints c4 = new ColumnConstraints();
        c4.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(c1, c2, c3, c4);

        pnlKhung.getChildren().addAll(lblTieuDePhan, pnlLuoi);
        return pnlKhung;
    }

    private VBox taoKhungChonDichVu() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        Label lblTieuDe = new Label("2. Dịch vụ chăm sóc đăng ký");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        cboDichVu.setPromptText("Chọn dịch vụ chăm sóc");
        cboDichVu.setPrefWidth(240);

        txtCanNangThuCung.setPromptText("Để trống = 3kg");
        txtCanNangThuCung.setPrefWidth(120);

        txtDonGia.setPromptText("Đơn giá");
        txtDonGia.setEditable(false);
        txtDonGia.setPrefWidth(135);
        txtDonGia.setStyle("-fx-background-color: #f1f5f9; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");

        btnThemDichVu.setText("+ Thêm vào bảng");
        btnThemDichVu.getStyleClass().add("nut-chinh");
        btnThemDichVu.setMinWidth(Region.USE_PREF_SIZE);
        HBox pnlChon = new HBox(10, new Label("Dịch vụ:"), cboDichVu, new Label("Cân nặng (kg):"), txtCanNangThuCung, new Label("Đơn giá:"), txtDonGia, btnThemDichVu);
        pnlChon.setAlignment(Pos.CENTER_LEFT);

        // Bảng danh sách dịch vụ chi tiết
        tblDichVu.getStyleClass().add("bang-du-lieu");
        tblDichVu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblDichVu.setPrefHeight(110);
        tblDichVu.setMinHeight(95);
        tblDichVu.setPlaceholder(new Label("Chưa có dịch vụ nào được thêm vào lịch hẹn"));
        VBox.setVgrow(tblDichVu, Priority.ALWAYS);

        TableColumn<ChiTietLichHen, String> colMaDV = new TableColumn<>("Mã dịch vụ");
        colMaDV.setStyle("-fx-alignment: CENTER;"); colMaDV.setPrefWidth(120);
        colMaDV.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaDichVu()));

        TableColumn<ChiTietLichHen, String> colTenDV = new TableColumn<>("Tên dịch vụ");
        colTenDV.setStyle("-fx-alignment: CENTER-LEFT;"); colTenDV.setPrefWidth(220);
        colTenDV.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDichVu()));

        TableColumn<ChiTietLichHen, String> colCanNang = new TableColumn<>("Cân nặng (ước tính)");
        colCanNang.setStyle("-fx-alignment: CENTER;"); colCanNang.setPrefWidth(130);
        colCanNang.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCanNangThuCung() != null ? c.getValue().getCanNangThuCung() + " kg" : "3.0 kg"));

        TableColumn<ChiTietLichHen, String> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;"); colDonGia.setPrefWidth(130);
        colDonGia.setCellValueFactory(c -> new SimpleStringProperty(DINH_DANG_TIEN.format(c.getValue().getDonGia())));

        TableColumn<ChiTietLichHen, Void> colXoa = new TableColumn<>("Thao tác");
        colXoa.setStyle("-fx-alignment: CENTER;"); colXoa.setPrefWidth(90);
        colXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoaDong = new Button("Xóa");
            {
                btnXoaDong.getStyleClass().addAll("button", "nut-xoa");
                btnXoaDong.setOnAction(e -> {
                    danhSachDichVuChon.remove(getTableView().getItems().get(getIndex()));
                    capNhatTongTien();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoaDong);
            }
        });

        tblDichVu.getColumns().addAll(List.of(colMaDV, colTenDV, colCanNang, colDonGia, colXoa));
        tblDichVu.setItems(danhSachDichVuChon);

        lblTongTien.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        Label lblLuuY = new Label("(*) Đơn giá tạm tính theo mức chuẩn (bậc Nhỏ nếu chưa rõ cân nặng). Nhân viên tiếp nhận sẽ cân lại khi làm dịch vụ.");
        lblLuuY.setStyle("-fx-font-style: italic; -fx-text-fill: #64748b; -fx-font-size: 11.5px;");
        VBox pnlTongTien = new VBox(2, lblTongTien, lblLuuY);
        pnlTongTien.setAlignment(Pos.CENTER_RIGHT);

        pnlKhung.getChildren().addAll(lblTieuDe, pnlChon, tblDichVu, pnlTongTien);
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
        btnDong.getStyleClass().add("nut-phu");

        btnLuu.setOnAction(e -> xuLyDatLich());
        btnLamMoi.setOnAction(e -> lamMoiForm());
        btnDong.setOnAction(e -> dongCuaSo());
        btnDong.setVisible(false);
        btnDong.setManaged(false);

        pnlNut.getChildren().addAll(lblNhacNho, khoangCach, btnLamMoi, btnDong, btnLuu);
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
            String cn = (tc.getCanNang() != null && tc.getCanNang().compareTo(BigDecimal.ZERO) > 0)
                    ? " (" + tc.getCanNang().stripTrailingZeros().toPlainString() + " kg)" : " (chưa rõ cân nặng)";
            return tc.getTenThuCung() + " [" + loai + giong + "]" + cn;
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
                    String cn = (tc.getCanNang() != null && tc.getCanNang().compareTo(BigDecimal.ZERO) > 0)
                            ? " (" + tc.getCanNang().stripTrailingZeros().toPlainString() + " kg)" : " (chưa rõ cân nặng)";
                    setText(tc.getTenThuCung() + " [" + loai + giong + "]" + cn);
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
                txtCanNangThuCung.clear();
            }
        });

        // Khi chọn thú cưng: điền cân nặng & ghi chú dị ứng
        cboThuCung.setOnAction(e -> {
            ThuCung tc = cboThuCung.getValue();
            if (tc != null && tc.getCanNang() != null && tc.getCanNang().compareTo(BigDecimal.ZERO) > 0) {
                txtCanNangThuCung.setText(tc.getCanNang().stripTrailingZeros().toPlainString());
            } else {
                txtCanNangThuCung.clear();
            }
            tinhDonGiaDichVu();

            if (tc != null && tc.getDiUng() != null && !tc.getDiUng().trim().isEmpty() && !"Không có".equalsIgnoreCase(tc.getDiUng().trim())) {
                String luuY = "[Lưu ý dị ứng: " + tc.getDiUng().trim() + "] ";
                if (!txtGhiChu.getText().contains(luuY)) {
                    txtGhiChu.setText(luuY + txtGhiChu.getText());
                }
            }
        });

        cboDichVu.setOnAction(e -> tinhDonGiaDichVu());
        txtCanNangThuCung.textProperty().addListener((obs, cu, moi) -> tinhDonGiaDichVu());
        txtCanNangThuCung.setOnAction(e -> xuLyThemDichVuVaoBang());
        btnThemDichVu.setOnAction(e -> xuLyThemDichVuVaoBang());
    }

    // -------------------------------------------------------------------------
    // NGHIỆP VỤ ĐẶT LỊCH HẸN & TÍNH TOÁN
    // -------------------------------------------------------------------------

    private void tinhDonGiaDichVu() {
        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) { txtDonGia.clear(); return; }

        double canNang = 3.0;
        boolean laUocTinh = true;
        try {
            String s = txtCanNangThuCung.getText() != null ? txtCanNangThuCung.getText().trim() : "";
            if (!s.isEmpty() && Double.parseDouble(s) > 0) {
                canNang = Double.parseDouble(s);
                laUocTinh = false;
            } else if (cboThuCung.getValue() != null && cboThuCung.getValue().getCanNang() != null) {
                canNang = cboThuCung.getValue().getCanNang().doubleValue();
                laUocTinh = false;
            }
        } catch (Exception ignored) {}

        double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang);
        txtDonGia.setText(DINH_DANG_TIEN.format(gia) + (laUocTinh ? " (tạm tính)" : ""));
    }

    private void xuLyThemDichVuVaoBang() {
        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) { hienThiLoi("Vui lòng chọn dịch vụ chăm sóc!"); return; }

        if (danhSachDichVuChon.stream().anyMatch(ct -> ct.getMaDichVu().equals(dv.getMaDichVu()))) {
            hienThiLoi("Dịch vụ '" + dv.getTenDichVu() + "' đã có trong lịch hẹn!");
            return;
        }

        BigDecimal canNang = BigDecimal.valueOf(3.0);
        try {
            String s = txtCanNangThuCung.getText() != null ? txtCanNangThuCung.getText().trim() : "";
            if (!s.isEmpty()) canNang = new BigDecimal(s);
            else if (cboThuCung.getValue() != null && cboThuCung.getValue().getCanNang() != null) {
                canNang = cboThuCung.getValue().getCanNang();
            }
        } catch (Exception ignored) {}

        double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang.doubleValue());
        danhSachDichVuChon.add(new ChiTietLichHen(txtMaLichHen.getText(), dv.getMaDichVu(), dv.getTenDichVu(), canNang, BigDecimal.valueOf(gia)));
        capNhatTongTien();
        cboDichVu.setValue(null);
        txtDonGia.clear();
    }

    private void capNhatTongTien() {
        BigDecimal tong = danhSachDichVuChon.stream().map(ct -> ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTongTien.setText("Tổng tiền dự kiến (tạm tính): " + DINH_DANG_TIEN.format(tong));
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
                if (laHopThoaiModal) dongCuaSo();
                else lamMoiForm();
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

        txtCanNangThuCung.clear();
        txtDonGia.clear();
        txtGhiChu.clear();
        danhSachDichVuChon.clear();
        capNhatTongTien();
    }

    private void taiDanhSachThuCung(String maKhachHang) {
        try {
            List<ThuCung> ds = controller.layDanhSachThuCungTheoKhachHang(maKhachHang);
            cboThuCung.setItems(FXCollections.observableArrayList(ds));
            if (!ds.isEmpty()) {
                ThuCung tc = ds.get(ds.size() - 1);
                cboThuCung.setValue(tc);
                if (tc.getCanNang() != null && tc.getCanNang().compareTo(BigDecimal.ZERO) > 0) {
                    txtCanNangThuCung.setText(tc.getCanNang().stripTrailingZeros().toPlainString());
                } else {
                    txtCanNangThuCung.clear();
                }
                tinhDonGiaDichVu();
                if (tc.getDiUng() != null && !tc.getDiUng().trim().isEmpty() && !"Không có".equalsIgnoreCase(tc.getDiUng().trim())) {
                    String luuY = "[Lưu ý dị ứng: " + tc.getDiUng().trim() + "] ";
                    if (!txtGhiChu.getText().contains(luuY)) {
                        txtGhiChu.setText(luuY + txtGhiChu.getText());
                    }
                }
            } else {
                cboThuCung.setValue(null);
                txtCanNangThuCung.clear();
                tinhDonGiaDichVu();
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
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #0284c7;");

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

        ThemThuCungView pnlThem = new ThemThuCungView(kh.getMaKhachHang(), () -> taiDanhSachThuCung(kh.getMaKhachHang()));
        Scene scene = new Scene(pnlThem, 580, 520);
        if (DatLichHenView.class.getResource(CSS_CHUNG) != null) scene.getStylesheets().add(DatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    private void dongCuaSo() {
        if (laHopThoaiModal && getScene() != null && getScene().getWindow() instanceof Stage stage) stage.close();
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

    public static void hienThi(Window cuaSoCha, Runnable khiDatThanhCong) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (cuaSoCha != null) dlg.initOwner(cuaSoCha);
        dlg.setTitle("Đặt lịch hẹn chăm sóc thú cưng - Happy Tails");

        DatLichHenView view = new DatLichHenView(khiDatThanhCong);
        view.laHopThoaiModal = true;
        view.btnDong.setVisible(true);
        view.btnDong.setManaged(true);

        Scene scene = new Scene(view, 800, 660);
        if (DatLichHenView.class.getResource(CSS_CHUNG) != null) scene.getStylesheets().add(DatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        dlg.setScene(scene);
        dlg.showAndWait();
    }
}
