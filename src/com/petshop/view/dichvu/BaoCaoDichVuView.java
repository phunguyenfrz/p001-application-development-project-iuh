package com.petshop.view.dichvu;

import com.petshop.controller.DichVuChamSocController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình báo cáo thống kê dịch vụ chăm sóc theo ngày / khoảng ngày, có khung chi tiết bên phải (SRV_REPORT).
 */
public class BaoCaoDichVuView extends VBox {

    private static final DateTimeFormatter DINH_DANG_NGAY_VN = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DichVuChamSocController dichVuController;
    private final ObservableList<String[]> dsBaoCao;
    private final ObservableList<String[]> dsChiTietLuot;

    // Điều khiển bộ lọc thời gian
    private ComboBox<String> cboKieuThoiGian;
    private Label lblDenNgay;
    private DatePicker dtpTuNgay;
    private DatePicker dtpDenNgay;
    private Button btnThongKe;
    private Button btnLamMoi;

    // Bảng tổng hợp bên trái
    private TableView<String[]> tblBaoCao;

    // Khung chi tiết thống kê bên phải
    private Label lblKhoangThoiGian;
    private Label lblTongLuotToanKy;
    private Label lblTongDoanhThuToanKy;
    private Label lblDichVuNoiBat;

    private Label lblDichVuDangChon;
    private Label lblChiTietSoLuot;
    private Label lblChiTietDoanhThu;
    private Label lblChiTietTyTrong;
    private TableView<String[]> tblChiTietLuot;

    private double tongDoanhThuHienTai = 0;

    public BaoCaoDichVuView() {
        this.dichVuController = new DichVuChamSocController();
        this.dsBaoCao = FXCollections.observableArrayList();
        this.dsChiTietLuot = FXCollections.observableArrayList();
        khoiTaoGiaoDien();
        thucHienThongKe();
    }

    private void khoiTaoGiaoDien() {
        getStyleClass().add("khung-phan-he");
        setSpacing(14);
        setPadding(new Insets(12));

        Label lblTieuDe = new Label("DỊCH VỤ CHĂM SÓC - BÁO CÁO THỐNG KÊ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Thống kê theo 1 ngày, nhiều ngày gần đây hoặc khoảng thời gian; nhấp chọn dịch vụ bên trái để xem chi tiết bên phải.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        HBox pnlBoLoc = taoKhungBoLoc();

        HBox pnlNoiDungChinh = new HBox(14);
        VBox pnlBenTrai = taoKhungBangTongHop();
        VBox pnlBenPhai = taoKhungChiTietBenPhai();
        HBox.setHgrow(pnlBenTrai, Priority.ALWAYS);
        VBox.setVgrow(pnlNoiDungChinh, Priority.ALWAYS);

        pnlNoiDungChinh.getChildren().addAll(pnlBenTrai, pnlBenPhai);
        getChildren().addAll(lblTieuDe, lblMoTa, pnlBoLoc, pnlNoiDungChinh);
    }

    // Tạo thanh lọc chọn nhanh theo ngày / mấy ngày / khoảng thời gian
    private HBox taoKhungBoLoc() {
        HBox pnlBoLoc = new HBox(10);
        pnlBoLoc.getStyleClass().add("hop-noi-dung");
        pnlBoLoc.setAlignment(Pos.CENTER_LEFT);

        cboKieuThoiGian = new ComboBox<>(FXCollections.observableArrayList(
                "Tháng này",
                "Hôm nay (1 ngày)",
                "Chọn 1 ngày cụ thể",
                "7 ngày gần đây",
                "30 ngày gần đây",
                "Năm nay",
                "Khoảng thời gian tùy chọn"
        ));
        cboKieuThoiGian.setValue("Tháng này");
        cboKieuThoiGian.setOnAction(e -> xuLyThayDoiKieuThoiGian());

        dtpTuNgay = new DatePicker(LocalDate.now().withDayOfMonth(1));
        dtpTuNgay.setPrefWidth(135);
        dtpTuNgay.setOnAction(e -> {
            if ("Chọn 1 ngày cụ thể".equals(cboKieuThoiGian.getValue())) {
                dtpDenNgay.setValue(dtpTuNgay.getValue());
            }
        });

        lblDenNgay = new Label("Đến ngày:");
        dtpDenNgay = new DatePicker(LocalDate.now());
        dtpDenNgay.setPrefWidth(135);

        btnThongKe = new Button("Thống kê");
        btnThongKe.getStyleClass().add("nut-chinh");
        btnThongKe.setOnAction(e -> thucHienThongKe());

        btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xuLyLamMoi());

        pnlBoLoc.getChildren().addAll(
                new Label("Kiểu thống kê:"), cboKieuThoiGian,
                new Label("Từ ngày:"), dtpTuNgay,
                lblDenNgay, dtpDenNgay,
                btnThongKe, btnLamMoi
        );
        return pnlBoLoc;
    }

    // Tạo bảng tổng hợp theo từng dịch vụ nằm bên trái màn hình
    private VBox taoKhungBangTongHop() {
        VBox pnlBang = new VBox(10);
        pnlBang.getStyleClass().add("hop-noi-dung");

        Label lblTieuDeBang = new Label("Bảng tổng hợp theo dịch vụ (chọn 1 dòng để xem chi tiết bên phải)");
        lblTieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        tblBaoCao = new TableView<>(dsBaoCao);
        tblBaoCao.getStyleClass().add("bang-du-lieu");
        tblBaoCao.setPlaceholder(new Label("Chưa có dữ liệu dịch vụ"));
        tblBaoCao.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<String[], String> colMaDichVu = new TableColumn<>("Mã dịch vụ");
        colMaDichVu.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[0]));

        TableColumn<String[], String> colTenDichVu = new TableColumn<>("Tên dịch vụ");
        colTenDichVu.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[1]));

        TableColumn<String[], String> colThoiLuong = new TableColumn<>("Thời lượng");
        colThoiLuong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colThoiLuong.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[2] + " phút"));

        TableColumn<String[], String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[3]));

        TableColumn<String[], String> colSoLuot = new TableColumn<>("Số lượt");
        colSoLuot.setStyle("-fx-alignment: CENTER-RIGHT;");
        colSoLuot.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[4]));

        TableColumn<String[], String> colDoanhThu = new TableColumn<>("Tổng doanh thu");
        colDoanhThu.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDoanhThu.setCellValueFactory(cell -> {
            double doanhThu = Double.parseDouble(cell.getValue()[5]);
            return new SimpleStringProperty(ThemDichVuView.dinhDangTien(doanhThu));
        });

        tblBaoCao.getColumns().addAll(List.of(
                colMaDichVu, colTenDichVu, colThoiLuong, colTrangThai, colSoLuot, colDoanhThu
        ));

        tblBaoCao.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            hienThiChiTietDichVuChon(moi);
        });

        VBox.setVgrow(tblBaoCao, Priority.ALWAYS);
        pnlBang.getChildren().addAll(lblTieuDeBang, tblBaoCao);
        return pnlBang;
    }

    // Tạo phần chi tiết thống kê nằm cố định ở bên phải màn hình
    private VBox taoKhungChiTietBenPhai() {
        VBox pnlPhai = new VBox(12);
        pnlPhai.setPrefWidth(430);
        pnlPhai.setMinWidth(400);

        // Khung 1: Tổng kết toàn kỳ
        VBox pnlTongKet = new VBox(8);
        pnlTongKet.getStyleClass().add("hop-noi-dung");

        Label lblTieuDeTongKet = new Label("Tổng kết kỳ thống kê");
        lblTieuDeTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #0369a1;");

        lblKhoangThoiGian = new Label("Thời gian: -");
        lblTongLuotToanKy = new Label("Tổng số lượt thực hiện: 0 lượt");
        lblTongDoanhThuToanKy = new Label("Tổng doanh thu dịch vụ: 0 đ");
        lblTongDoanhThuToanKy.setStyle("-fx-font-weight: bold;");
        lblDichVuNoiBat = new Label("Dịch vụ nhiều lượt nhất: Chưa có");

        pnlTongKet.getChildren().addAll(
                lblTieuDeTongKet,
                lblKhoangThoiGian,
                lblTongLuotToanKy,
                lblTongDoanhThuToanKy,
                lblDichVuNoiBat
        );

        // Khung 2: Chi tiết của dịch vụ đang chọn và các lượt thực hiện theo ngày
        VBox pnlChiTietDichVu = new VBox(8);
        pnlChiTietDichVu.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlChiTietDichVu, Priority.ALWAYS);

        Label lblTieuDeChiTiet = new Label("Chi tiết dịch vụ đang chọn");
        lblTieuDeChiTiet.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #0369a1;");

        lblDichVuDangChon = new Label("Dịch vụ: (Chọn một dòng bên trái)");
        lblDichVuDangChon.setStyle("-fx-font-weight: bold;");
        lblChiTietSoLuot = new Label("Số lượt trong kỳ: 0 lượt");
        lblChiTietDoanhThu = new Label("Doanh thu dịch vụ này: 0 đ");
        lblChiTietTyTrong = new Label("Tỷ trọng doanh thu: 0%");

        Label lblTieuDeBangLuot = new Label("Các lượt thực hiện theo ngày:");
        lblTieuDeBangLuot.setStyle("-fx-font-weight: bold; -fx-padding: 4 0 0 0;");

        tblChiTietLuot = new TableView<>(dsChiTietLuot);
        tblChiTietLuot.getStyleClass().add("bang-du-lieu");
        tblChiTietLuot.setPlaceholder(new Label("Không có lượt thực hiện nào trong kỳ này"));
        tblChiTietLuot.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tblChiTietLuot, Priority.ALWAYS);

        TableColumn<String[], String> colNgay = new TableColumn<>("Ngày");
        colNgay.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[0]));

        TableColumn<String[], String> colMaPhieu = new TableColumn<>("Mã phiếu");
        colMaPhieu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[1]));

        TableColumn<String[], String> colThuCung = new TableColumn<>("Thú cưng");
        colThuCung.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[2]));

        TableColumn<String[], String> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDonGia.setCellValueFactory(c -> {
            double gia = Double.parseDouble(c.getValue()[4]);
            return new SimpleStringProperty(ThemDichVuView.dinhDangTien(gia));
        });

        tblChiTietLuot.getColumns().addAll(List.of(colNgay, colMaPhieu, colThuCung, colDonGia));

        pnlChiTietDichVu.getChildren().addAll(
                lblTieuDeChiTiet,
                lblDichVuDangChon,
                lblChiTietSoLuot,
                lblChiTietDoanhThu,
                lblChiTietTyTrong,
                lblTieuDeBangLuot,
                tblChiTietLuot
        );

        pnlPhai.getChildren().addAll(pnlTongKet, pnlChiTietDichVu);
        return pnlPhai;
    }

    // Tự động gán Từ ngày - Đến ngày khi chọn kiểu thống kê
    private void xuLyThayDoiKieuThoiGian() {
        String kieu = cboKieuThoiGian.getValue();
        LocalDate homNay = LocalDate.now();
        boolean laMotNgay = "Chọn 1 ngày cụ thể".equals(kieu) || "Hôm nay (1 ngày)".equals(kieu);

        lblDenNgay.setDisable(laMotNgay);
        dtpDenNgay.setDisable(laMotNgay);

        if ("Hôm nay (1 ngày)".equals(kieu)) {
            dtpTuNgay.setValue(homNay);
            dtpDenNgay.setValue(homNay);
        } else if ("Chọn 1 ngày cụ thể".equals(kieu)) {
            if (dtpTuNgay.getValue() == null) {
                dtpTuNgay.setValue(homNay);
            }
            dtpDenNgay.setValue(dtpTuNgay.getValue());
        } else if ("7 ngày gần đây".equals(kieu)) {
            dtpTuNgay.setValue(homNay.minusDays(6));
            dtpDenNgay.setValue(homNay);
        } else if ("30 ngày gần đây".equals(kieu)) {
            dtpTuNgay.setValue(homNay.minusDays(29));
            dtpDenNgay.setValue(homNay);
        } else if ("Tháng này".equals(kieu)) {
            dtpTuNgay.setValue(homNay.withDayOfMonth(1));
            dtpDenNgay.setValue(homNay);
        } else if ("Năm nay".equals(kieu)) {
            dtpTuNgay.setValue(homNay.withDayOfYear(1));
            dtpDenNgay.setValue(homNay);
        }

        thucHienThongKe();
    }

    // Thực hiện thống kê dịch vụ theo khoảng ngày đã chọn
    private void thucHienThongKe() {
        try {
            if ("Chọn 1 ngày cụ thể".equals(cboKieuThoiGian.getValue()) || "Hôm nay (1 ngày)".equals(cboKieuThoiGian.getValue())) {
                dtpDenNgay.setValue(dtpTuNgay.getValue());
            }

            LocalDate tuNgay = dtpTuNgay.getValue();
            LocalDate denNgay = dtpDenNgay.getValue();
            List<String[]> ketQua = dichVuController.thongKeDichVu(tuNgay, denNgay);
            dsBaoCao.setAll(ketQua);
            capNhatKhungTongKet(ketQua, tuNgay, denNgay);

            if (!ketQua.isEmpty()) {
                tblBaoCao.getSelectionModel().selectFirst();
            } else {
                hienThiChiTietDichVuChon(null);
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi thống kê dịch vụ", e.getMessage());
        }
    }

    // Đặt lại bộ lọc về mặc định (Tháng này) và thống kê lại
    private void xuLyLamMoi() {
        cboKieuThoiGian.setValue("Tháng này");
        lblDenNgay.setDisable(false);
        dtpDenNgay.setDisable(false);
        dtpTuNgay.setValue(LocalDate.now().withDayOfMonth(1));
        dtpDenNgay.setValue(LocalDate.now());
        thucHienThongKe();
    }

    // Cập nhật khung Tổng kết kỳ thống kê bên phải
    private void capNhatKhungTongKet(List<String[]> danhSach, LocalDate tuNgay, LocalDate denNgay) {
        int tongSoLuot = 0;
        double tongDoanhThu = 0;
        String tenNoiBat = "Chưa có lượt thực hiện";
        int luotCaoNhat = 0;

        for (String[] dong : danhSach) {
            int soLuot = Integer.parseInt(dong[4]);
            double doanhThu = Double.parseDouble(dong[5]);
            tongSoLuot += soLuot;
            tongDoanhThu += doanhThu;

            if (soLuot > luotCaoNhat) {
                luotCaoNhat = soLuot;
                tenNoiBat = dong[1] + " (" + soLuot + " lượt)";
            }
        }

        tongDoanhThuHienTai = tongDoanhThu;

        if (tuNgay.equals(denNgay)) {
            lblKhoangThoiGian.setText("Thời gian: Ngày " + tuNgay.format(DINH_DANG_NGAY_VN));
        } else {
            lblKhoangThoiGian.setText("Thời gian: " + tuNgay.format(DINH_DANG_NGAY_VN)
                    + " - " + denNgay.format(DINH_DANG_NGAY_VN));
        }
        lblTongLuotToanKy.setText("Tổng số lượt thực hiện: " + tongSoLuot + " lượt");
        lblTongDoanhThuToanKy.setText("Tổng doanh thu dịch vụ: " + ThemDichVuView.dinhDangTien(tongDoanhThu));
        lblDichVuNoiBat.setText("Dịch vụ nhiều lượt nhất: " + tenNoiBat);
    }

    // Hiển thị chi tiết và danh sách lượt làm theo ngày của dịch vụ đang chọn bên phải
    private void hienThiChiTietDichVuChon(String[] dongChon) {
        if (dongChon == null) {
            lblDichVuDangChon.setText("Dịch vụ: (Chọn một dòng bên trái)");
            lblChiTietSoLuot.setText("Số lượt trong kỳ: 0 lượt");
            lblChiTietDoanhThu.setText("Doanh thu dịch vụ này: 0 đ");
            lblChiTietTyTrong.setText("Tỷ trọng doanh thu: 0%");
            dsChiTietLuot.clear();
            return;
        }

        String maDichVu = dongChon[0];
        String tenDichVu = dongChon[1];
        int soLuot = Integer.parseInt(dongChon[4]);
        double doanhThu = Double.parseDouble(dongChon[5]);
        double tyTrong = tongDoanhThuHienTai > 0 ? (doanhThu * 100.0 / tongDoanhThuHienTai) : 0;

        lblDichVuDangChon.setText("Dịch vụ: " + maDichVu + " - " + tenDichVu);
        lblChiTietSoLuot.setText("Số lượt trong kỳ: " + soLuot + " lượt (" + dongChon[2] + " phút/lượt)");
        lblChiTietDoanhThu.setText("Doanh thu dịch vụ này: " + ThemDichVuView.dinhDangTien(doanhThu));
        lblChiTietTyTrong.setText(String.format("Tỷ trọng doanh thu: %.1f%%", tyTrong));

        try {
            List<String[]> chiTiet = dichVuController.layChiTietThongKeTheoDichVu(
                    maDichVu, dtpTuNgay.getValue(), dtpDenNgay.getValue()
            );
            dsChiTietLuot.setAll(chiTiet);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            dsChiTietLuot.clear();
        }
    }

    private void hienThongBao(Alert.AlertType loai, String tieuDe, String noiDung) {
        Alert alert = new Alert(loai);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
