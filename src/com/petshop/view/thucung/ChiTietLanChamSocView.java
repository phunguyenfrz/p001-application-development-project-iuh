package com.petshop.view.thucung;

import com.petshop.controller.ThuCungController;
import com.petshop.model.ChiTietDichVuLichHen;
import com.petshop.model.LichSuChamSoc;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Hộp thoại (Modal Dialog) hiển thị chi tiết một lần chăm sóc dịch vụ của thú cưng theo lịch hẹn.
 * Ánh xạ từ thông tin lịch hẹn và danh sách dịch vụ đã thực hiện.
 */
public class ChiTietLanChamSocView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    private final ThuCungController thuCungController = new ThuCungController();

    // Bảng danh sách dịch vụ đã sử dụng
    private final TableView<ChiTietDichVuLichHen> tblDichVu = new TableView<>();
    private final Button btnDong = new Button("Đóng");

    public ChiTietLanChamSocView(LichSuChamSoc ls) {
        setSpacing(14);
        setPadding(new Insets(20));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề hộp thoại
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("Chi tiết lần chăm sóc");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        lblTieuDe.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        pnlTieuDe.getChildren().add(lblTieuDe);

        // 2. Khung thông tin lần hẹn
        VBox pnlThongTinLichHen = taoKhungThongTinLichHen(ls);

        // 3. Khung bảng dịch vụ đã sử dụng
        VBox pnlDichVu = taoKhungDichVuSuDung(ls);
        VBox.setVgrow(pnlDichVu, Priority.ALWAYS);

        // 4. Thanh nút bấm
        HBox pnlNut = new HBox();
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setPrefWidth(90);
        btnDong.setOnAction(e -> dongCuaSo());
        pnlNut.getChildren().add(btnDong);

        getChildren().addAll(pnlTieuDe, pnlThongTinLichHen, pnlDichVu, pnlNut);
    }

    /**
     * Khởi tạo khung lưới thông tin lịch hẹn (Mã, Ngày, Giờ, Thú cưng, Chủ nuôi, NV phụ trách, Trạng thái, Ghi chú).
     */
    private VBox taoKhungThongTinLichHen(LichSuChamSoc ls) {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(20);
        pnlLuoi.setVgap(10);

        // Cột 1: Mã lịch hẹn & Thú cưng
        VBox boxMa = taoOThongTin("Mã lịch hẹn", ls != null ? ls.getMaLichHen() : "-");
        String thongTinThuCung = (ls != null)
                ? (ls.getTenThuCung() != null ? ls.getTenThuCung() : "") + " • " + ls.getMaThuCung()
                : "-";
        VBox boxThuCung = taoOThongTin("Thú cưng", thongTinThuCung);
        VBox boxTrangThai = taoOThongTin("Trạng thái", ls != null ? ls.getTrangThai() : "Hoàn thành");

        // Cột 2: Ngày hẹn & Chủ nuôi & Ghi chú lịch hẹn
        String ngayHenStr = (ls != null && ls.getNgayHen() != null) ? ls.getNgayHen().format(DINH_DANG_NGAY) : "-";
        VBox boxNgay = taoOThongTin("Ngày hẹn", ngayHenStr);
        VBox boxChuNuoi = taoOThongTin("Chủ nuôi", (ls != null && ls.getTenKhachHang() != null) ? ls.getTenKhachHang() : "-");
        VBox boxGhiChu = taoOThongTin("Ghi chú lịch hẹn", (ls != null && ls.getGhiChu() != null && !ls.getGhiChu().isBlank()) ? ls.getGhiChu() : "Không có ghi chú");

        // Cột 3: Giờ hẹn & Nhân viên phụ trách
        VBox boxGio = taoOThongTin("Giờ hẹn", ls != null ? ls.getGioHen() : "-");
        VBox boxNhanVien = taoOThongTin("Nhân viên phụ trách", (ls != null && ls.getTenNhanVien() != null) ? ls.getTenNhanVien() : "-");

        pnlLuoi.add(boxMa, 0, 0);
        pnlLuoi.add(boxNgay, 1, 0);
        pnlLuoi.add(boxGio, 2, 0);

        pnlLuoi.add(boxThuCung, 0, 1);
        pnlLuoi.add(boxChuNuoi, 1, 1);
        pnlLuoi.add(boxNhanVien, 2, 1);

        pnlLuoi.add(boxTrangThai, 0, 2);
        pnlLuoi.add(boxGhiChu, 1, 2, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(37);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(30);
        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3);

        pnlKhung.getChildren().add(pnlLuoi);
        return pnlKhung;
    }

    private VBox taoOThongTin(String tieuDe, String noiDung) {
        VBox box = new VBox(2);
        Label lblTieuDe = new Label(tieuDe);
        lblTieuDe.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");

        Label lblGiaTri = new Label(noiDung);
        lblGiaTri.setWrapText(true);
        if ("Trạng thái".equals(tieuDe) && "Hoàn thành".equalsIgnoreCase(noiDung)) {
            lblGiaTri.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #16a34a;");
        } else {
            lblGiaTri.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
        }
        box.getChildren().addAll(lblTieuDe, lblGiaTri);
        return box;
    }

    /**
     * Khởi tạo khung bảng các dịch vụ đã sử dụng.
     */
    private VBox taoKhungDichVuSuDung(LichSuChamSoc ls) {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblTieuDe = new Label("Dịch vụ đã sử dụng");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        tblDichVu.getStyleClass().add("bang-du-lieu");
        tblDichVu.setPlaceholder(new Label("Không có dữ liệu chi tiết dịch vụ cho lịch hẹn này"));
        tblDichVu.setPrefHeight(150);

        TableColumn<ChiTietDichVuLichHen, String> colTen = new TableColumn<>("Tên dịch vụ");
        colTen.setPrefWidth(280);
        colTen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDichVu()));
        colTen.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<ChiTietDichVuLichHen, String> colCanNang = new TableColumn<>("Cân nặng ghi nhận (kg)");
        colCanNang.setPrefWidth(180);
        colCanNang.setCellValueFactory(c -> {
            BigDecimal cn = c.getValue().getCanNangThuCung();
            return new SimpleStringProperty(cn != null && cn.compareTo(BigDecimal.ZERO) > 0 ? String.format("%.2f kg", cn) : "-");
        });
        colCanNang.setStyle("-fx-alignment: CENTER;");

        TableColumn<ChiTietDichVuLichHen, String> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setPrefWidth(160);
        colDonGia.setCellValueFactory(c -> new SimpleStringProperty(DINH_DANG_TIEN.format(c.getValue().getDonGia())));
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");

        tblDichVu.getColumns().addAll(List.of(colTen, colCanNang, colDonGia));

        // Nạp dữ liệu chi tiết
        if (ls != null && ls.getMaLichHen() != null) {
            try {
                List<ChiTietDichVuLichHen> ds = thuCungController.layChiTietDichVuLichHen(ls.getMaLichHen());
                tblDichVu.setItems(FXCollections.observableArrayList(ds));
            } catch (Exception e) {
                System.err.println("Ghi nhận: Chưa thể tải chi tiết dịch vụ lịch hẹn: " + e.getMessage());
            }
        }

        // Dòng ghi chú chân bảng
        Label lblLuuY = new Label("Cân nặng và đơn giá được lưu tại lần sử dụng dịch vụ này, không thay đổi bảng giá hiện tại. Đơn giá không phải tổng thanh toán thực tế.");
        lblLuuY.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        lblLuuY.setWrapText(true);

        pnlKhung.getChildren().addAll(lblTieuDe, tblDichVu, lblLuuY);
        return pnlKhung;
    }

    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    /**
     * Mở hộp thoại Chi tiết lần chăm sóc.
     */
    public static void hienThi(LichSuChamSoc ls, Window cuaSoCha) {
        if (ls == null) return;

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (cuaSoCha != null) {
            dlg.initOwner(cuaSoCha);
        }
        dlg.setTitle("Chi tiết lần chăm sóc - " + ls.getMaLichHen());

        ChiTietLanChamSocView pnlNoiDung = new ChiTietLanChamSocView(ls);
        Scene scene = new Scene(pnlNoiDung, 720, 520);

        if (ChiTietLanChamSocView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(ChiTietLanChamSocView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        dlg.setScene(scene);
        dlg.setResizable(false);
        dlg.showAndWait();
    }
}

