package com.petshop.view.doitra;

import com.petshop.controller.PhieuDoiTraController;
import com.petshop.model.PhieuDoiTra;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình 3: Tìm kiếm & Tra cứu phiếu đổi trả (RET_SEARCH / DT_TIM_KIEM).
 * Sử dụng bộ màu mặc định chuẩn chung.css đồng bộ với các phân hệ Dịch vụ, Hóa đơn.
 */
public class TimKiemPhieuDoiTraView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 VNĐ");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhieuDoiTraController controller;

    // 1. Bộ lọc
    private final TextField txtTuKhoa = new TextField();
    private final DatePicker dpTuNgay = new DatePicker();
    private final DatePicker dpDenNgay = new DatePicker();
    private final ComboBox<String> cboTrangThai = new ComboBox<>(FXCollections.observableArrayList(
            "Tất cả trạng thái", "Chờ xử lý", "Đã duyệt", "Từ chối"
    ));
    private final ComboBox<String> cboLoaiXuLy = new ComboBox<>(FXCollections.observableArrayList(
            "Tất cả loại", "Hoàn tiền", "Đổi hàng"
    ));
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnDatLai = new Button("Đặt lại");
    private final Button btnXuatExcel = new Button("Xuất file");

    // 2. Thẻ KPI thống kê nhanh
    private final Label lblKpiTongSoPhieu = new Label("0");
    private final Label lblKpiTongTienHoan = new Label("0 VNĐ");
    private final Label lblKpiTongTienBu = new Label("0 VNĐ");
    private final Label lblKpiTyLeHoanThanh = new Label("0%");

    // 3. Bảng kết quả
    private final TableView<PhieuDoiTra> tblKetQua = new TableView<>();
    private final ObservableList<PhieuDoiTra> dsKetQua = FXCollections.observableArrayList();

    public TimKiemPhieuDoiTraView() {
        this.controller = new PhieuDoiTraController();
        khoiTaoGiaoDien();
        dangKySuKien();
        napDuLieuMacDinh();
    }

    private void khoiTaoGiaoDien() {
        this.setSpacing(14);
        this.getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("PHIẾU ĐỔI TRẢ - TÌM KIẾM PHIẾU ĐỔI TRẢ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu phiếu đổi trả theo mã phiếu, hóa đơn gốc, khách hàng, thời gian và trạng thái xử lý.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 1. Bộ lọc
        VBox cardBoLoc = taoCardBoLoc();

        // 2. KPI thống kê nhanh
        HBox pnlKpi = taoPnlKpi();

        // 3. Bảng kết quả
        VBox cardBang = taoCardBangKetQua();
        VBox.setVgrow(cardBang, Priority.ALWAYS);

        this.getChildren().addAll(pnlTieuDe, cardBoLoc, pnlKpi, cardBang);
    }

    private VBox taoCardBoLoc() {
        VBox card = new VBox(10);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("BỘ LỌC TRA CỨU PHIẾU ĐỔI TRẢ");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);

        txtTuKhoa.setPromptText("Mã phiếu, Mã HĐ, Tên KH, SĐT...");
        txtTuKhoa.setPrefWidth(220);

        dpTuNgay.setPromptText("Từ ngày");
        dpTuNgay.setPrefWidth(140);
        dpDenNgay.setPromptText("Đến ngày");
        dpDenNgay.setPrefWidth(140);

        cboTrangThai.setValue("Tất cả trạng thái");
        cboTrangThai.setPrefWidth(160);

        cboLoaiXuLy.setValue("Tất cả loại");
        cboLoaiXuLy.setPrefWidth(140);

        grid.add(new Label("Từ khóa:"), 0, 0);
        grid.add(txtTuKhoa, 1, 0);
        grid.add(new Label("Từ ngày:"), 2, 0);
        grid.add(dpTuNgay, 3, 0);
        grid.add(new Label("Đến ngày:"), 4, 0);
        grid.add(dpDenNgay, 5, 0);

        grid.add(new Label("Trạng thái:"), 0, 1);
        grid.add(cboTrangThai, 1, 1);
        grid.add(new Label("Loại xử lý:"), 2, 1);
        grid.add(cboLoaiXuLy, 3, 1);

        HBox hangNut = new HBox(10);
        hangNut.setAlignment(Pos.CENTER_LEFT);
        btnTimKiem.getStyleClass().add("nut-chinh");
        btnDatLai.getStyleClass().add("nut-phu");
        btnXuatExcel.getStyleClass().add("nut-phu");
        hangNut.getChildren().addAll(btnTimKiem, btnDatLai, btnXuatExcel);

        grid.add(hangNut, 4, 1, 2, 1);

        card.getChildren().addAll(lblTieuDe, grid);
        return card;
    }

    private HBox taoPnlKpi() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER);

        VBox kpi1 = taoTheKpi("TỔNG PHIẾU ĐỔI TRẢ", lblKpiTongSoPhieu);
        VBox kpi2 = taoTheKpi("TỔNG TIỀN ĐÃ HOÀN", lblKpiTongTienHoan);
        VBox kpi3 = taoTheKpi("TỔNG TIỀN KHÁCH BÙ", lblKpiTongTienBu);
        VBox kpi4 = taoTheKpi("TỶ LỆ ĐÃ DUYỆT", lblKpiTyLeHoanThanh);

        HBox.setHgrow(kpi1, Priority.ALWAYS);
        HBox.setHgrow(kpi2, Priority.ALWAYS);
        HBox.setHgrow(kpi3, Priority.ALWAYS);
        HBox.setHgrow(kpi4, Priority.ALWAYS);

        bar.getChildren().addAll(kpi1, kpi2, kpi3, kpi4);
        return bar;
    }

    private VBox taoTheKpi(String tieuDe, Label lblGiaTri) {
        VBox box = new VBox(6);
        box.getStyleClass().add("hop-noi-dung");
        box.setAlignment(Pos.CENTER);

        Label lblT = new Label(tieuDe);
        lblT.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        lblGiaTri.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

        box.getChildren().addAll(lblT, lblGiaTri);
        return box;
    }

    private VBox taoCardBangKetQua() {
        VBox card = new VBox(10);
        card.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("KẾT QUẢ TRA CỨU");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        tblKetQua.getStyleClass().add("bang-du-lieu");
        tblKetQua.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblKetQua, Priority.ALWAYS);

        TableColumn<PhieuDoiTra, String> colMa = new TableColumn<>("Mã phiếu");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaPhieuDoiTra()));
        colMa.setPrefWidth(130);

        TableColumn<PhieuDoiTra, String> colMaHD = new TableColumn<>("Hóa đơn gốc");
        colMaHD.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHoaDon()));
        colMaHD.setPrefWidth(125);

        TableColumn<PhieuDoiTra, String> colNgay = new TableColumn<>("Ngày lập");
        colNgay.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNgayLap() != null ? d.getValue().getNgayLap().format(DINH_DANG_NGAY) : "--"));
        colNgay.setPrefWidth(90);

        TableColumn<PhieuDoiTra, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTenKhachHang() != null ? d.getValue().getTenKhachHang() : "Khách lẻ"));
        colKhach.setPrefWidth(130);

        TableColumn<PhieuDoiTra, String> colSdt = new TableColumn<>("SĐT");
        colSdt.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSoDienThoaiKhachHang() != null ? d.getValue().getSoDienThoaiKhachHang() : "--"));
        colSdt.setPrefWidth(100);

        TableColumn<PhieuDoiTra, String> colLoai = new TableColumn<>("Loại xử lý");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLoaiXuLy()));
        colLoai.setPrefWidth(90);

        TableColumn<PhieuDoiTra, String> colTongTien = new TableColumn<>("Giá trị hàng trả");
        colTongTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().tinhTongTienHangTra())));
        colTongTien.setPrefWidth(105);

        TableColumn<PhieuDoiTra, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));
        colTrangThai.setPrefWidth(95);
        colTrangThai.setStyle("-fx-alignment: center; -fx-font-weight: bold;");

        TableColumn<PhieuDoiTra, Void> colThaoTac = new TableColumn<>("Thao tác");
        colThaoTac.setCellFactory(col -> new TableCell<>() {
            private final Button btnXem = new Button("Xem");
            {
                btnXem.getStyleClass().add("nut-phu");
                btnXem.setStyle("-fx-font-size: 11px; -fx-padding: 2 10;");
                btnXem.setOnAction(e -> {
                    PhieuDoiTra p = getTableView().getItems().get(getIndex());
                    moHopThoaiXemChiTiet(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXem);
            }
        });
        colThaoTac.setPrefWidth(70);

        tblKetQua.getColumns().addAll(colMa, colMaHD, colNgay, colKhach, colSdt, colLoai, colTongTien, colTrangThai, colThaoTac);
        tblKetQua.setItems(dsKetQua);

        card.getChildren().addAll(lblTieuDe, tblKetQua);
        return card;
    }

    private void dangKySuKien() {
        btnTimKiem.setOnAction(e -> thucHienTimKiem());
        txtTuKhoa.setOnAction(e -> thucHienTimKiem());

        btnDatLai.setOnAction(e -> {
            txtTuKhoa.clear();
            dpTuNgay.setValue(null);
            dpDenNgay.setValue(null);
            cboTrangThai.setValue("Tất cả trạng thái");
            cboLoaiXuLy.setValue("Tất cả loại");
            thucHienTimKiem();
        });

        btnXuatExcel.setOnAction(e -> xuatDuLieuExcel());

        tblKetQua.setRowFactory(tv -> {
            TableRow<PhieuDoiTra> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moHopThoaiXemChiTiet(row.getItem());
                }
            });
            return row;
        });
    }

    private void napDuLieuMacDinh() {
        thucHienTimKiem();
    }

    private void thucHienTimKiem() {
        String tuKhoa = txtTuKhoa.getText();
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();

        String tt = cboTrangThai.getValue();
        String trangThaiLoc = ("Tất cả trạng thái".equals(tt) || tt == null) ? null : tt;

        String loai = cboLoaiXuLy.getValue();
        String loaiXuLyLoc = ("Tất cả loại".equals(loai) || loai == null) ? null : loai;

        try {
            List<PhieuDoiTra> ds = controller.timKiemPhieuDoiTra(tuKhoa, tuNgay, denNgay, trangThaiLoc, loaiXuLyLoc);
            dsKetQua.clear();
            if (ds != null) {
                dsKetQua.addAll(ds);
            }
            capNhatKpi();
        } catch (Exception ex) {
            Alert al = new Alert(Alert.AlertType.ERROR);
            al.setTitle("Lỗi");
            al.setContentText("Lỗi tìm kiếm phiếu đổi trả: " + ex.getMessage());
            al.showAndWait();
        }
    }

    private void capNhatKpi() {
        int tongPhieu = dsKetQua.size();
        BigDecimal tongHoan = BigDecimal.ZERO;
        BigDecimal tongBu = BigDecimal.ZERO;
        int demHoanThanh = 0;

        for (PhieuDoiTra p : dsKetQua) {
            if ("Đã duyệt".equalsIgnoreCase(p.getTrangThai()) || "Hoàn thành".equalsIgnoreCase(p.getTrangThai())) {
                demHoanThanh++;
                BigDecimal tienTra = p.tinhTongTienHangTra();
                if (tienTra.compareTo(BigDecimal.ZERO) == 0 && p.getTienHoan() != null && p.getTienHoan().compareTo(BigDecimal.ZERO) > 0) {
                    tienTra = p.getTienHoan();
                }
                tongHoan = tongHoan.add(tienTra);
                if (p.getTienBu() != null) {
                    tongBu = tongBu.add(p.getTienBu());
                }
            }
        }

        lblKpiTongSoPhieu.setText(String.valueOf(tongPhieu));
        lblKpiTongTienHoan.setText(DINH_DANG_TIEN.format(tongHoan));
        lblKpiTongTienBu.setText(DINH_DANG_TIEN.format(tongBu));

        if (tongPhieu > 0) {
            BigDecimal tyLe = BigDecimal.valueOf(demHoanThanh)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(tongPhieu), 1, RoundingMode.HALF_UP);
            lblKpiTyLeHoanThanh.setText(tyLe + "%");
        } else {
            lblKpiTyLeHoanThanh.setText("0%");
        }
    }

    private void moHopThoaiXemChiTiet(PhieuDoiTra phieu) {
        if (phieu == null) return;
        if (phieu.getDanhSachSanPham() == null || phieu.getDanhSachSanPham().isEmpty()) {
            try {
                PhieuDoiTra phieuDayDu = controller.layPhieuDoiTraTheoMa(phieu.getMaPhieuDoiTra());
                if (phieuDayDu != null && phieuDayDu.getDanhSachSanPham() != null && !phieuDayDu.getDanhSachSanPham().isEmpty()) {
                    phieu = phieuDayDu;
                }
            } catch (Exception ignored) {}
        }
        XemChiTietPhieuDoiTraView dialog = new XemChiTietPhieuDoiTraView(phieu);
        dialog.showAndWait();
    }

    private void xuatDuLieuExcel() {
        if (dsKetQua.isEmpty()) {
            Alert al = new Alert(Alert.AlertType.WARNING);
            al.setTitle("Xuất file");
            al.setHeaderText(null);
            al.setContentText("Không có dữ liệu phiếu đổi trả nào để xuất!");
            al.showAndWait();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu danh sách phiếu đổi trả");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));
        fc.setInitialFileName("BaoCao_PhieuDoiTra_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");

        File file = fc.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file, StandardCharsets.UTF_8)) {
                pw.write('\ufeff');
                pw.println("Mã phiếu,Mã hóa đơn,Ngày lập,Khách hàng,Số điện thoại,Loại xử lý,Tổng tiền hàng trả,Trạng thái");
                for (PhieuDoiTra p : dsKetQua) {
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            p.getMaPhieuDoiTra(),
                            p.getMaHoaDon(),
                            p.getNgayLap() != null ? p.getNgayLap().format(DINH_DANG_NGAY) : "",
                            p.getTenKhachHang() != null ? p.getTenKhachHang() : "",
                            p.getSoDienThoaiKhachHang() != null ? p.getSoDienThoaiKhachHang() : "",
                            p.getLoaiXuLy() != null ? p.getLoaiXuLy() : "",
                            p.tinhTongTienHangTra().toString(),
                            p.getTrangThai() != null ? p.getTrangThai() : ""
                    );
                }
                Alert al = new Alert(Alert.AlertType.INFORMATION);
                al.setTitle("Thành công");
                al.setHeaderText(null);
                al.setContentText("Xuất danh sách phiếu đổi trả thành công vào file:\n" + file.getAbsolutePath());
                al.showAndWait();
            } catch (Exception ex) {
                Alert al = new Alert(Alert.AlertType.ERROR);
                al.setTitle("Lỗi");
                al.setContentText("Lỗi khi xuất file: " + ex.getMessage());
                al.showAndWait();
            }
        }
    }
}
