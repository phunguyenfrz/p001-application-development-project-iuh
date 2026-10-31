package com.petshop.view.thucung;

import com.petshop.controller.KhachHangController;
import com.petshop.controller.ThuCungController;
import com.petshop.model.KhachHang;
import com.petshop.model.ThuCung;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại (Modal Dialog) hiển thị toàn bộ hồ sơ chi tiết thú cưng kèm thông tin chủ nuôi.
 * Cho phép bôi đen (chọn văn bản bằng chuột) trên từng ô thông tin để sao chép thuận tiện.
 */
public class XemChiTietThuCungView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ThuCungController thuCungController = new ThuCungController();
    private final KhachHangController khachHangController = new KhachHangController();

    // Các ô hiển thị dữ liệu thú cưng (chỉ đọc, bôi đen copy)
    private final TextField txtMaThuCung = new TextField();
    private final TextField txtTenThuCung = new TextField();
    private final TextField txtLoai = new TextField();
    private final TextField txtGiong = new TextField();
    private final TextField txtGioiTinh = new TextField();
    private final TextField txtCanNang = new TextField();
    private final TextField txtNgaySinh = new TextField();
    private final TextArea txtDiUng = new TextArea();

    // Các ô hiển thị dữ liệu chủ nuôi (chỉ đọc, bôi đen copy)
    private final TextField txtMaChuNuoi = new TextField();
    private final TextField txtTenChuNuoi = new TextField();
    private final TextField txtSdtChuNuoi = new TextField();
    private final TextField txtEmailChuNuoi = new TextField();
    private final TextField txtDiaChiChuNuoi = new TextField();

    // Nút đóng
    private final Button btnDong = new Button("Đóng");

    public XemChiTietThuCungView(ThuCung tc) {
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề hộp thoại
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("HỒ SƠ CHI TIẾT THÚ CƯNG & CHỦ NUÔI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        String maTC = (tc != null) ? tc.getMaThuCung() : "-";
        String tenTC = (tc != null) ? tc.getTenThuCung() : "-";
        Label lblDangXem = new Label("Mã thú cưng: " + maTC + " - " + tenTC);
        lblDangXem.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblDangXem);

        // 2. Lấy thông tin chủ nuôi
        KhachHang chuNuoi = null;
        if (tc != null && tc.getMaKhachHang() != null) {
            try {
                chuNuoi = khachHangController.layKhachHangTheoMa(tc.getMaKhachHang());
            } catch (Exception ignored) {}
        }

        // 3. Khung thông tin Thú cưng
        VBox pnlThongTinThuCung = taoKhungThongTinThuCung(tc);

        // 4. Khung thông tin Chủ nuôi
        VBox pnlThongTinChuNuoi = taoKhungThongTinChuNuoi(chuNuoi, tc);

        // 5. Thanh nút bấm
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());
        pnlNut.getChildren().add(btnDong);

        getChildren().addAll(pnlTieuDe, pnlThongTinThuCung, pnlThongTinChuNuoi, pnlNut);
    }

    /**
     * Khởi tạo khung hiển thị thông tin thú cưng.
     */
    private VBox taoKhungThongTinThuCung(ThuCung tc) {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblTieuDePhan = new Label("Thông tin thú cưng");
        lblTieuDePhan.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(14);
        pnlLuoi.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(115);
        col1.setPrefWidth(115);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(220);
        col2.setPrefWidth(240);
        col2.setHgrow(Priority.ALWAYS);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setMinWidth(115);
        col3.setPrefWidth(115);

        ColumnConstraints col4 = new ColumnConstraints();
        col4.setMinWidth(220);
        col4.setPrefWidth(240);
        col4.setHgrow(Priority.ALWAYS);

        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3, col4);

        if (tc != null) {
            txtMaThuCung.setText(tc.getMaThuCung() != null ? tc.getMaThuCung() : "-");
            txtTenThuCung.setText(tc.getTenThuCung() != null ? tc.getTenThuCung() : "-");
            txtLoai.setText(tc.getLoaiThuCung() != null ? tc.getLoaiThuCung() : "-");
            txtGiong.setText(tc.getGiongLoai() != null ? tc.getGiongLoai() : "-");
            txtGioiTinh.setText(tc.getGioiTinh() != null ? tc.getGioiTinh() : "-");
            BigDecimal cn = tc.getCanNang();
            txtCanNang.setText(cn != null ? String.format("%.2f kg", cn) : "-");
            LocalDate ns = tc.getNgaySinh();
            txtNgaySinh.setText(ns != null ? ns.format(DINH_DANG_NGAY) : "-");
            txtDiUng.setText(tc.getDiUng() != null ? tc.getDiUng() : "Không có");
        }

        khoaChiDoc(txtMaThuCung);
        khoaChiDoc(txtTenThuCung);
        khoaChiDoc(txtLoai);
        khoaChiDoc(txtGiong);
        khoaChiDoc(txtGioiTinh);
        khoaChiDoc(txtCanNang);
        khoaChiDoc(txtNgaySinh);
        khoaChiDoc(txtDiUng);
        txtDiUng.setPrefRowCount(2);
        txtDiUng.setWrapText(true);

        pnlLuoi.add(new Label("Mã thú cưng:"), 0, 0);
        pnlLuoi.add(txtMaThuCung, 1, 0);
        pnlLuoi.add(new Label("Tên thú cưng:"), 2, 0);
        pnlLuoi.add(txtTenThuCung, 3, 0);

        pnlLuoi.add(new Label("Loài thú cưng:"), 0, 1);
        pnlLuoi.add(txtLoai, 1, 1);
        pnlLuoi.add(new Label("Giống loài:"), 2, 1);
        pnlLuoi.add(txtGiong, 3, 1);

        pnlLuoi.add(new Label("Giới tính:"), 0, 2);
        pnlLuoi.add(txtGioiTinh, 1, 2);
        pnlLuoi.add(new Label("Cân nặng:"), 2, 2);
        pnlLuoi.add(txtCanNang, 3, 2);

        pnlLuoi.add(new Label("Ngày sinh:"), 0, 3);
        pnlLuoi.add(txtNgaySinh, 1, 3);
        pnlLuoi.add(new Label("Dị ứng / Lưu ý:"), 2, 3);
        pnlLuoi.add(txtDiUng, 3, 3);

        pnlKhung.getChildren().addAll(lblTieuDePhan, pnlLuoi);
        return pnlKhung;
    }

    /**
     * Khởi tạo khung hiển thị thông tin chủ nuôi.
     */
    private VBox taoKhungThongTinChuNuoi(KhachHang chuNuoi, ThuCung tc) {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblTieuDePhan = new Label("Thông tin chủ nuôi (Khách hàng sở hữu)");
        lblTieuDePhan.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(14);
        pnlLuoi.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(115);
        col1.setPrefWidth(115);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(220);
        col2.setPrefWidth(240);
        col2.setHgrow(Priority.ALWAYS);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setMinWidth(115);
        col3.setPrefWidth(115);

        ColumnConstraints col4 = new ColumnConstraints();
        col4.setMinWidth(220);
        col4.setPrefWidth(240);
        col4.setHgrow(Priority.ALWAYS);

        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3, col4);

        if (chuNuoi != null) {
            txtMaChuNuoi.setText(chuNuoi.getMaKhachHang());
            txtTenChuNuoi.setText(chuNuoi.getHoTen());
            txtSdtChuNuoi.setText(chuNuoi.getSoDienThoai());
            txtEmailChuNuoi.setText(chuNuoi.getEmail() != null ? chuNuoi.getEmail() : "—");
            txtDiaChiChuNuoi.setText(chuNuoi.getDiaChi() != null ? chuNuoi.getDiaChi() : "—");
        } else if (tc != null) {
            txtMaChuNuoi.setText(tc.getMaKhachHang() != null ? tc.getMaKhachHang() : "-");
            txtTenChuNuoi.setText(tc.getTenKhachHang() != null ? tc.getTenKhachHang() : "-");
            txtSdtChuNuoi.setText(tc.getSoDienThoaiKhachHang() != null ? tc.getSoDienThoaiKhachHang() : "-");
            txtEmailChuNuoi.setText("—");
            txtDiaChiChuNuoi.setText("—");
        }

        khoaChiDoc(txtMaChuNuoi);
        khoaChiDoc(txtTenChuNuoi);
        khoaChiDoc(txtSdtChuNuoi);
        khoaChiDoc(txtEmailChuNuoi);
        khoaChiDoc(txtDiaChiChuNuoi);

        pnlLuoi.add(new Label("Mã chủ nuôi:"), 0, 0);
        pnlLuoi.add(txtMaChuNuoi, 1, 0);
        pnlLuoi.add(new Label("Họ và tên:"), 2, 0);
        pnlLuoi.add(txtTenChuNuoi, 3, 0);

        pnlLuoi.add(new Label("Số điện thoại:"), 0, 1);
        pnlLuoi.add(txtSdtChuNuoi, 1, 1);
        pnlLuoi.add(new Label("Email:"), 2, 1);
        pnlLuoi.add(txtEmailChuNuoi, 3, 1);

        pnlLuoi.add(new Label("Địa chỉ:"), 0, 2);
        pnlLuoi.add(txtDiaChiChuNuoi, 1, 2, 3, 1);

        pnlKhung.getChildren().addAll(lblTieuDePhan, pnlLuoi);
        return pnlKhung;
    }

    private void khoaChiDoc(TextInputControl oNhap) {
        oNhap.setEditable(false);
        oNhap.setStyle("-fx-opacity: 0.95; -fx-background-color: #f1f5f9; -fx-text-fill: #1e293b;");
    }

    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    /**
     * Mở nhanh hộp thoại xem thông tin chi tiết thú cưng.
     */
    public static void hienThi(ThuCung tc, Window cuaSoCha) {
        if (tc == null) return;

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (cuaSoCha != null) {
            dlg.initOwner(cuaSoCha);
        }
        dlg.setTitle("Hồ sơ thú cưng - " + tc.getMaThuCung() + " (" + tc.getTenThuCung() + ")");

        XemChiTietThuCungView pnlNoiDung = new XemChiTietThuCungView(tc);
        Scene scene = new Scene(pnlNoiDung, 780, 580);

        if (XemChiTietThuCungView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(XemChiTietThuCungView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        dlg.setScene(scene);
        dlg.setResizable(false);
        dlg.showAndWait();
    }
}
