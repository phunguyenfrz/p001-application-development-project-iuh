package com.petshop.view.thucung;

import com.petshop.controller.ThuCungController;
import com.petshop.model.LichSuChamSoc;
import com.petshop.model.ThuCung;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Popup;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Lịch sử chăm sóc thú cưng (TC_CHAM_SOC).
 * Cho phép tra cứu các lần hẹn và dịch vụ chăm sóc đã thực hiện của từng thú cưng.
 */
public class LichSuChamSocThuCungView extends VBox {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ThuCungController thuCungController = new ThuCungController();
    private final ObservableList<LichSuChamSoc> danhSachLichHen = FXCollections.observableArrayList();

    // Thú cưng hiện tại đang được chọn
    private ThuCung thuCungHienTai = null;
    private boolean dangChonTuGoiY = false;

    // Tìm kiếm thú cưng
    private final TextField txtTimThuCung = new TextField();
    private final Button btnTimThuCung = new Button("Tìm kiếm");
    private final Button btnLamMoiThuCung = new Button("Làm mới");

    // Autocomplete popup
    private final Popup popupGoiY = new Popup();
    private final ListView<ThuCung> lstGoiY = new ListView<>();

    // Thẻ thông tin thú cưng đang chọn
    private final VBox theThuCung = new VBox(12);
    private final Label lblAvatar = new Label("TC");
    private final Label lblTenThuCung = new Label("Chưa chọn thú cưng");
    private final Label lblMaThuCung = new Label("—");
    private final Button btnDoiThuCung = new Button("Đổi thú cưng");

    private final Label lblChuNuoi = new Label("—");
    private final Label lblLoaiGiongGioiTinh = new Label("—");
    private final Label lblNgaySinh = new Label("—");
    private final Label lblCanNangHienTai = new Label("—");
    private final Label lblDiUng = new Label("—");

    // Bộ lọc lịch hẹn
    private final DatePicker dpTuNgay = new DatePicker();
    private final DatePicker dpDenNgay = new DatePicker();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();
    private final Button btnApDung = new Button("Áp dụng");
    private final Button btnXoaBoLoc = new Button("Xóa bộ lọc");

    // Bảng lịch sử chăm sóc
    private final TableView<LichSuChamSoc> tblLichHen = new TableView<>();
    private final Button btnXemChiTiet = new Button("Xem chi tiết");
    private final Label lblSoLuongHienThi = new Label("Trong kết quả đang hiển thị: 0 lịch hẹn");

    public LichSuChamSocThuCungView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề phân hệ
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("LỊCH SỬ CHĂM SÓC THÚ CƯNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu nhật ký chăm sóc dịch vụ và lịch hẹn của từng thú cưng theo thời gian.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung tìm kiếm thú cưng
        VBox pnlTimKiem = taoKhuVucTimKiemThuCung();

        // 3. Khởi tạo Autocomplete Popup
        khoiTaoPopupGoiY();

        // 4. Cấu hình thẻ tóm tắt thú cưng
        cauHinhTheThuCung();

        // 5. Khung danh sách lịch sử chăm sóc và bộ lọc
        VBox pnlDanhSachLichHen = taoKhungDanhSachLichHen();
        VBox.setVgrow(pnlDanhSachLichHen, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, pnlTimKiem, theThuCung, pnlDanhSachLichHen);

        // Tự động chọn thú cưng đầu tiên nếu có dữ liệu để màn hình không bị trống
        napThuCungMacDinh();
    }

    /**
     * Khởi tạo khu vực ô tìm kiếm thú cưng.
     */
    private VBox taoKhuVucTimKiemThuCung() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("Tìm kiếm thú cưng");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        HBox pnlNhap = new HBox(10);
        pnlNhap.setAlignment(Pos.CENTER_LEFT);

        txtTimThuCung.setPromptText("Tìm theo mã, tên thú cưng hoặc chủ nuôi...");
        txtTimThuCung.setPrefWidth(420);
        HBox.setHgrow(txtTimThuCung, Priority.ALWAYS);

        btnTimThuCung.getStyleClass().add("nut-chinh");
        btnTimThuCung.setOnAction(e -> thucHienTimThuCung());

        btnLamMoiThuCung.getStyleClass().add("nut-phu");
        btnLamMoiThuCung.setOnAction(e -> lamMoiTimThuCung());

        pnlNhap.getChildren().addAll(txtTimThuCung, btnTimThuCung, btnLamMoiThuCung);
        pnlKhung.getChildren().addAll(lblTieuDe, pnlNhap);

        // Lắng nghe sự kiện gõ phím để kích hoạt popup gợi ý tự động
        txtTimThuCung.textProperty().addListener((obs, cu, moi) -> {
            if (dangChonTuGoiY) return;
            if (moi == null || moi.trim().length() < 1) {
                popupGoiY.hide();
                return;
            }
            capNhatDanhSachGoiY(moi.trim());
        });

        txtTimThuCung.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN && popupGoiY.isShowing()) {
                lstGoiY.requestFocus();
                lstGoiY.getSelectionModel().selectFirst();
            } else if (event.getCode() == KeyCode.ENTER) {
                thucHienTimThuCung();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                popupGoiY.hide();
            }
        });

        return pnlKhung;
    }

    /**
     * Cấu hình popup autocomplete gợi ý danh sách thú cưng.
     */
    private void khoiTaoPopupGoiY() {
        popupGoiY.setAutoHide(true);
        lstGoiY.setPrefWidth(550);
        lstGoiY.setPrefHeight(220);
        lstGoiY.getStyleClass().add("bang-du-lieu");

        lstGoiY.setCellFactory(param -> new ListCell<>() {
            private final HBox pnlDong = new HBox(12);
            private final Label lblAvatarItem = new Label();
            private final Label lblTenItem = new Label();
            private final Label lblMaItem = new Label();
            private final Label lblChuNuoiItem = new Label();
            private final Label lblLoaiGiongItem = new Label();

            {
                pnlDong.setAlignment(Pos.CENTER_LEFT);
                pnlDong.setPadding(new Insets(6, 10, 6, 10));

                lblAvatarItem.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: center; -fx-background-radius: 50%; -fx-min-width: 36px; -fx-min-height: 36px;");

                VBox boxTrai = new VBox(2);
                lblTenItem.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
                lblMaItem.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                boxTrai.getChildren().addAll(lblTenItem, lblMaItem);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                VBox boxPhai = new VBox(2);
                boxPhai.setAlignment(Pos.CENTER_RIGHT);
                lblChuNuoiItem.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #334155;");
                lblLoaiGiongItem.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
                boxPhai.getChildren().addAll(lblChuNuoiItem, lblLoaiGiongItem);

                pnlDong.getChildren().addAll(lblAvatarItem, boxTrai, spacer, boxPhai);
            }

            @Override
            protected void updateItem(ThuCung tc, boolean empty) {
                super.updateItem(tc, empty);
                if (empty || tc == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String chuCai = (tc.getTenThuCung() != null && !tc.getTenThuCung().isEmpty())
                            ? tc.getTenThuCung().substring(0, 1).toUpperCase() : "TC";
                    lblAvatarItem.setText(chuCai);
                    lblTenItem.setText(tc.getTenThuCung());
                    lblMaItem.setText(tc.getMaThuCung());

                    String tenKh = (tc.getTenKhachHang() != null && !tc.getTenKhachHang().isEmpty())
                            ? tc.getTenKhachHang() : "Mã KH: " + tc.getMaKhachHang();
                    lblChuNuoiItem.setText("Chủ: " + tenKh);

                    String loaiGiong = (tc.getLoaiThuCung() != null ? tc.getLoaiThuCung() : "-") + " • " +
                            (tc.getGiongLoai() != null ? tc.getGiongLoai() : "-");
                    lblLoaiGiongItem.setText(loaiGiong);

                    setGraphic(pnlDong);
                }
            }
        });

        lstGoiY.setOnMouseClicked(event -> {
            ThuCung tcChon = lstGoiY.getSelectionModel().getSelectedItem();
            if (tcChon != null) {
                chonThuCung(tcChon);
                popupGoiY.hide();
            }
        });

        lstGoiY.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ThuCung tcChon = lstGoiY.getSelectionModel().getSelectedItem();
                if (tcChon != null) {
                    chonThuCung(tcChon);
                    popupGoiY.hide();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                popupGoiY.hide();
                txtTimThuCung.requestFocus();
            }
        });

        popupGoiY.getContent().add(lstGoiY);
    }

    private void capNhatDanhSachGoiY(String tuKhoa) {
        try {
            List<ThuCung> ketQua = thuCungController.timKiemThuCung(tuKhoa, null);
            if (ketQua.isEmpty()) {
                popupGoiY.hide();
                return;
            }

            lstGoiY.setItems(FXCollections.observableArrayList(ketQua));
            if (!popupGoiY.isShowing() && txtTimThuCung.getScene() != null && txtTimThuCung.getScene().getWindow() != null) {
                var bounds = txtTimThuCung.localToScreen(txtTimThuCung.getBoundsInLocal());
                if (bounds != null) {
                    popupGoiY.show(txtTimThuCung, bounds.getMinX(), bounds.getMaxY() + 4);
                }
            }
        } catch (Exception ignored) {
            popupGoiY.hide();
        }
    }

    /**
     * Cấu hình thẻ hiển thị tóm tắt thông tin thú cưng đang chọn.
     */
    private void cauHinhTheThuCung() {
        theThuCung.getStyleClass().add("hop-noi-dung");

        // Dòng trên: Avatar + Tên + Mã + Nút Đổi thú cưng
        HBox hangTren = new HBox(14);
        hangTren.setAlignment(Pos.CENTER_LEFT);

        lblAvatar.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 16px; -fx-alignment: center; -fx-background-radius: 50%; -fx-min-width: 48px; -fx-min-height: 48px;");

        VBox boxTenMa = new VBox(2);
        lblTenThuCung.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        lblMaThuCung.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        boxTenMa.getChildren().addAll(lblTenThuCung, lblMaThuCung);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnDoiThuCung.getStyleClass().add("nut-phu");
        btnDoiThuCung.setOnAction(e -> {
            txtTimThuCung.requestFocus();
            txtTimThuCung.selectAll();
        });

        hangTren.getChildren().addAll(lblAvatar, boxTenMa, spacer, btnDoiThuCung);

        // Phân cách nhẹ
        Separator line = new Separator();

        // Dòng dưới: Grid thông tin chi tiết
        GridPane pnlThongTin = new GridPane();
        pnlThongTin.setHgap(24);
        pnlThongTin.setVgap(4);

        VBox boxChuNuoi = taoCotThongTinThe("Chủ nuôi", lblChuNuoi);
        VBox boxLoaiGiong = taoCotThongTinThe("Loài • Giống • Giới tính", lblLoaiGiongGioiTinh);
        VBox boxNgaySinh = taoCotThongTinThe("Ngày sinh", lblNgaySinh);
        VBox boxCanNang = taoCotThongTinThe("Cân nặng hiện tại", lblCanNangHienTai);
        lblCanNangHienTai.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #059669;");
        VBox boxDiUng = taoCotThongTinThe("Dị ứng đã ghi nhận", lblDiUng);

        pnlThongTin.add(boxChuNuoi, 0, 0);
        pnlThongTin.add(boxLoaiGiong, 1, 0);
        pnlThongTin.add(boxNgaySinh, 2, 0);
        pnlThongTin.add(boxCanNang, 3, 0);
        pnlThongTin.add(boxDiUng, 4, 0);

        for (int i = 0; i < 5; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(20);
            pnlThongTin.getColumnConstraints().add(col);
        }

        theThuCung.getChildren().addAll(hangTren, line, pnlThongTin);
    }

    private VBox taoCotThongTinThe(String tieuDe, Label lblGiaTri) {
        VBox box = new VBox(3);
        Label lblTieuDe = new Label(tieuDe);
        lblTieuDe.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        lblGiaTri.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        box.getChildren().addAll(lblTieuDe, lblGiaTri);
        return box;
    }

    /**
     * Khởi tạo khung bộ lọc ngày / trạng thái và bảng TableView hiển thị các lần chăm sóc.
     */
    private VBox taoKhungDanhSachLichHen() {
        VBox pnlKhung = new VBox(12);
        pnlKhung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        // 1. Thanh bộ lọc
        VBox pnlBoLocToanPhan = new VBox(6);
        HBox pnlBoLoc = new HBox(12);
        pnlBoLoc.setAlignment(Pos.CENTER_LEFT);

        Label lblTuNgay = new Label("Từ ngày:");
        dpTuNgay.setPrefWidth(140);

        Label lblDenNgay = new Label("Đến ngày:");
        dpDenNgay.setPrefWidth(140);

        Label lblTrangThai = new Label("Trạng thái lịch hẹn:");
        cboTrangThai.setItems(FXCollections.observableArrayList(
                "Tất cả", "Hoàn thành", "Đang thực hiện", "Đã xác nhận", "Đã đặt", "Đã hủy"
        ));
        cboTrangThai.setValue("Hoàn thành");
        cboTrangThai.setPrefWidth(150);

        btnApDung.getStyleClass().add("nut-chinh");
        btnApDung.setOnAction(e -> taiDuLieuLichHen());

        btnXoaBoLoc.getStyleClass().add("nut-phu");
        btnXoaBoLoc.setOnAction(e -> xoaBoLoc());

        pnlBoLoc.getChildren().addAll(
                lblTuNgay, dpTuNgay,
                lblDenNgay, dpDenNgay,
                lblTrangThai, cboTrangThai,
                btnApDung, btnXoaBoLoc
        );

        Label lblGhiChuBoLoc = new Label("Để trống ngày để xem lịch sử không giới hạn khoảng ngày.");
        lblGhiChuBoLoc.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        pnlBoLocToanPhan.getChildren().addAll(pnlBoLoc, lblGhiChuBoLoc);

        // 2. Thanh tiêu đề bảng và nút Xem chi tiết
        HBox pnlTieuDeBang = new HBox(10);
        pnlTieuDeBang.setAlignment(Pos.CENTER_LEFT);

        VBox boxTieuDeBang = new VBox(2);
        Label lblTieuDeBang = new Label("Các lần chăm sóc đã hoàn thành");
        lblTieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        Label lblMoTaBang = new Label("Ngày và giờ hẹn mới nhất trước");
        lblMoTaBang.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        boxTieuDeBang.getChildren().addAll(lblTieuDeBang, lblMoTaBang);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXemChiTiet.setDisable(true);
        btnXemChiTiet.setOnAction(e -> moChiTietLanChamSoc());

        pnlTieuDeBang.getChildren().addAll(boxTieuDeBang, spacer, btnXemChiTiet);

        // 3. Bảng dữ liệu TableView
        tblLichHen.getStyleClass().add("bang-du-lieu");
        tblLichHen.setPlaceholder(new Label("Không có dữ liệu lịch hẹn chăm sóc phù hợp"));
        VBox.setVgrow(tblLichHen, Priority.ALWAYS);

        TableColumn<LichSuChamSoc, String> colMa = new TableColumn<>("Mã lịch hẹn");
        colMa.setPrefWidth(160);
        colMa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaLichHen()));
        colMa.setStyle("-fx-alignment: CENTER;");

        TableColumn<LichSuChamSoc, String> colNgay = new TableColumn<>("Ngày hẹn");
        colNgay.setPrefWidth(120);
        colNgay.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNgayHen() != null ? c.getValue().getNgayHen().format(DINH_DANG_NGAY) : "-"
        ));
        colNgay.setStyle("-fx-alignment: CENTER;");

        TableColumn<LichSuChamSoc, String> colGio = new TableColumn<>("Giờ hẹn");
        colGio.setPrefWidth(90);
        colGio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGioHen()));
        colGio.setStyle("-fx-alignment: CENTER;");

        TableColumn<LichSuChamSoc, String> colNhanVien = new TableColumn<>("Nhân viên phụ trách");
        colNhanVien.setPrefWidth(170);
        colNhanVien.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTenNhanVien() != null ? c.getValue().getTenNhanVien() : "-"
        ));
        colNhanVien.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<LichSuChamSoc, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTrangThai()));
        colTrangThai.setStyle("-fx-alignment: CENTER;");

        TableColumn<LichSuChamSoc, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setMinWidth(200);
        colGhiChu.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getGhiChu() != null && !c.getValue().getGhiChu().isBlank() ? c.getValue().getGhiChu() : "—"
        ));
        colGhiChu.setStyle("-fx-alignment: CENTER-LEFT;");

        tblLichHen.getColumns().addAll(List.of(colMa, colNgay, colGio, colNhanVien, colTrangThai, colGhiChu));
        tblLichHen.setItems(danhSachLichHen);

        tblLichHen.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            btnXemChiTiet.setDisable(moi == null);
        });

        tblLichHen.setRowFactory(tv -> {
            TableRow<LichSuChamSoc> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moChiTietLanChamSoc();
                }
            });
            return row;
        });

        // 4. Thanh hiển thị số lượng chân bảng
        lblSoLuongHienThi.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        pnlKhung.getChildren().addAll(pnlBoLocToanPhan, new Separator(), pnlTieuDeBang, tblLichHen, lblSoLuongHienThi);
        return pnlKhung;
    }

    /**
     * Nạp thú cưng mặc định khi mở màn hình.
     */
    private void napThuCungMacDinh() {
        try {
            List<ThuCung> ds = thuCungController.layDanhSachThuCung();
            if (!ds.isEmpty()) {
                chonThuCung(ds.get(0));
            }
        } catch (Exception ignored) {}
    }

    /**
     * Chọn và hiển thị thông tin chi tiết một thú cưng.
     */
    public void chonThuCung(ThuCung tc) {
        if (tc == null) return;
        this.thuCungHienTai = tc;

        dangChonTuGoiY = true;
        txtTimThuCung.setText(tc.getTenThuCung() + " - " + tc.getMaThuCung());
        dangChonTuGoiY = false;

        String chuCai = (tc.getTenThuCung() != null && !tc.getTenThuCung().isEmpty())
                ? tc.getTenThuCung().substring(0, 1).toUpperCase() : "TC";
        lblAvatar.setText(chuCai);
        lblTenThuCung.setText(tc.getTenThuCung());
        lblMaThuCung.setText(tc.getMaThuCung());

        lblChuNuoi.setText(tc.getTenKhachHang() != null ? tc.getTenKhachHang() : (tc.getMaKhachHang() != null ? tc.getMaKhachHang() : "—"));
        String loai = tc.getLoaiThuCung() != null ? tc.getLoaiThuCung() : "-";
        String giong = tc.getGiongLoai() != null ? tc.getGiongLoai() : "-";
        String gt = tc.getGioiTinh() != null ? tc.getGioiTinh() : "-";
        lblLoaiGiongGioiTinh.setText(loai + " • " + giong + " • " + gt);

        lblNgaySinh.setText(tc.getNgaySinh() != null ? tc.getNgaySinh().format(DINH_DANG_NGAY) : "—");
        BigDecimal cn = tc.getCanNang();
        lblCanNangHienTai.setText(cn != null ? String.format("%.2f kg", cn) : "—");
        lblDiUng.setText(tc.getDiUng() != null && !tc.getDiUng().isBlank() ? tc.getDiUng() : "Không có");

        // Tải danh sách lịch hẹn của thú cưng
        taiDuLieuLichHen();
    }

    /**
     * Tải danh sách lịch hẹn chăm sóc theo bộ lọc hiện tại.
     */
    private void taiDuLieuLichHen() {
        if (thuCungHienTai == null) {
            danhSachLichHen.clear();
            lblSoLuongHienThi.setText("Trong kết quả đang hiển thị: 0 lịch hẹn");
            btnXemChiTiet.setDisable(true);
            return;
        }

        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();
        String trangThai = cboTrangThai.getValue();

        try {
            List<LichSuChamSoc> ketQua = thuCungController.layDanhSachLichHenChamSoc(
                    thuCungHienTai.getMaThuCung(), tuNgay, denNgay, trangThai
            );
            danhSachLichHen.setAll(ketQua);
            String ttStr = (trangThai != null && !"Tất cả".equalsIgnoreCase(trangThai))
                    ? " " + trangThai.toLowerCase() : "";
            lblSoLuongHienThi.setText("Trong kết quả đang hiển thị: " + ketQua.size() + " lịch hẹn" + ttStr);
            btnXemChiTiet.setDisable(true);
        } catch (Exception e) {
            danhSachLichHen.clear();
            lblSoLuongHienThi.setText("Lỗi tải lịch hẹn: " + e.getMessage());
        }
    }

    private void thucHienTimThuCung() {
        String tuKhoa = txtTimThuCung.getText() != null ? txtTimThuCung.getText().trim() : "";
        if (tuKhoa.isEmpty()) {
            return;
        }

        try {
            List<ThuCung> ketQua = thuCungController.timKiemThuCung(tuKhoa, null);
            if (!ketQua.isEmpty()) {
                chonThuCung(ketQua.get(0));
                popupGoiY.hide();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.setContentText("Không tìm thấy thú cưng nào với từ khóa: \"" + tuKhoa + "\"");
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Lỗi tìm thú cưng: " + e.getMessage());
        }
    }

    private void lamMoiTimThuCung() {
        txtTimThuCung.clear();
        popupGoiY.hide();
        txtTimThuCung.requestFocus();
    }

    private void xoaBoLoc() {
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
        cboTrangThai.setValue("Hoàn thành");
        taiDuLieuLichHen();
    }

    private void moChiTietLanChamSoc() {
        LichSuChamSoc lsChon = tblLichHen.getSelectionModel().getSelectedItem();
        if (lsChon == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn một lần chăm sóc trên bảng để xem chi tiết!");
            alert.showAndWait();
            return;
        }

        ChiTietLanChamSocView.hienThi(lsChon, getScene() != null ? getScene().getWindow() : null);
    }
}

