package com.petshop.view.dichvu;

import com.petshop.controller.DichVuChamSocController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * Màn hình báo cáo thống kê số lượt sử dụng và doanh thu theo từng dịch vụ chăm sóc (SRV_REPORT).
 */
public class BaoCaoDichVuView extends VBox {

    private final DichVuChamSocController dichVuController;
    private final ObservableList<String[]> dsBaoCao;

    private DatePicker dtpTuNgay;
    private DatePicker dtpDenNgay;
    private Button btnThongKe;
    private Button btnLamMoi;
    private Label lblTongKet;
    private TableView<String[]> tblBaoCao;

    public BaoCaoDichVuView() {
        this.dichVuController = new DichVuChamSocController();
        this.dsBaoCao = FXCollections.observableArrayList();
        khoiTaoGiaoDien();
        thucHienThongKe();
    }

    // Khởi tạo giao diện lọc khoảng ngày và bảng thống kê dịch vụ
    private void khoiTaoGiaoDien() {
        getStyleClass().add("khung-phan-he");
        setSpacing(14);
        setPadding(new Insets(12));

        Label lblTieuDe = new Label("DỊCH VỤ CHĂM SÓC - BÁO CÁO THỐNG KÊ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Thống kê số lượt thực hiện và doanh thu của từng dịch vụ chăm sóc theo khoảng thời gian.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        HBox pnlBoLoc = taoKhungBoLoc();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(lblTieuDe, lblMoTa, pnlBoLoc, pnlBang);
    }

    // Tạo thanh chọn Từ ngày - Đến ngày và nút Thống kê
    private HBox taoKhungBoLoc() {
        HBox pnlBoLoc = new HBox(12);
        pnlBoLoc.getStyleClass().add("hop-noi-dung");
        pnlBoLoc.setAlignment(Pos.CENTER_LEFT);

        dtpTuNgay = new DatePicker(LocalDate.now().withDayOfMonth(1));
        dtpDenNgay = new DatePicker(LocalDate.now());

        btnThongKe = new Button("Thống kê");
        btnThongKe.getStyleClass().add("nut-chinh");
        btnThongKe.setOnAction(e -> thucHienThongKe());

        btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xuLyLamMoi());

        pnlBoLoc.getChildren().addAll(
                new Label("Từ ngày:"), dtpTuNgay,
                new Label("Đến ngày:"), dtpDenNgay,
                btnThongKe, btnLamMoi
        );
        return pnlBoLoc;
    }

    // Tạo bảng kết quả thống kê dịch vụ
    private VBox taoKhungBang() {
        VBox pnlBang = new VBox(10);
        pnlBang.getStyleClass().add("hop-noi-dung");

        lblTongKet = new Label("Tổng kết: 0 lượt thực hiện | Tổng doanh thu: 0 đ");
        lblTongKet.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        tblBaoCao = new TableView<>(dsBaoCao);
        tblBaoCao.getStyleClass().add("bang-du-lieu");
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

        TableColumn<String[], String> colSoLuot = new TableColumn<>("Số lượt thực hiện");
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

        VBox.setVgrow(tblBaoCao, Priority.ALWAYS);
        pnlBang.getChildren().addAll(lblTongKet, tblBaoCao);
        return pnlBang;
    }

    // Thực hiện thống kê dịch vụ theo khoảng ngày đã chọn
    private void thucHienThongKe() {
        try {
            LocalDate tuNgay = dtpTuNgay.getValue();
            LocalDate denNgay = dtpDenNgay.getValue();
            List<String[]> ketQua = dichVuController.thongKeDichVu(tuNgay, denNgay);
            dsBaoCao.setAll(ketQua);
            capNhatDongTongKet(ketQua);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi thống kê dịch vụ", e.getMessage());
        }
    }

    // Đặt lại khoảng ngày mặc định (đầu tháng đến hôm nay) và thống kê lại
    private void xuLyLamMoi() {
        dtpTuNgay.setValue(LocalDate.now().withDayOfMonth(1));
        dtpDenNgay.setValue(LocalDate.now());
        thucHienThongKe();
    }

    // Tính tổng số lượt và tổng doanh thu hiển thị phía trên bảng
    private void capNhatDongTongKet(List<String[]> danhSach) {
        int tongSoLuot = 0;
        double tongDoanhThu = 0;

        for (String[] dong : danhSach) {
            tongSoLuot += Integer.parseInt(dong[4]);
            tongDoanhThu += Double.parseDouble(dong[5]);
        }

        lblTongKet.setText("Tổng kết: " + tongSoLuot + " lượt thực hiện | Tổng doanh thu: "
                + ThemDichVuView.dinhDangTien(tongDoanhThu));
    }

    // Hiện hộp thoại thông báo cho người dùng
    private void hienThongBao(Alert.AlertType loai, String tieuDe, String noiDung) {
        Alert alert = new Alert(loai);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
