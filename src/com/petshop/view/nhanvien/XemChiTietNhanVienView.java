package com.petshop.view.nhanvien;

import com.petshop.model.NhanVien;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại (Modal Dialog) Xem chi tiết hồ sơ nhân viên và tài khoản liên kết.
 * Thiết kế đồng bộ hoàn toàn với XemChiTietKhachHangView:
 * - Hỗ trợ bôi đen (chọn văn bản bằng chuột) trên từng ô thông tin để sao chép (copy) dễ dàng.
 * - Hiển thị đầy đủ thông tin nhân sự và trạng thái tài khoản hệ thống.
 */
public class XemChiTietNhanVienView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Các ô hiển thị dữ liệu nhân viên (chỉ đọc, cho phép bôi đen copy)
    private final TextField txtMaNhanVien = new TextField();
    private final TextField txtHoTen = new TextField();
    private final TextField txtSoDienThoai = new TextField();
    private final TextField txtGioiTinh = new TextField();
    private final TextField txtNgaySinh = new TextField();
    private final TextField txtTuoi = new TextField();
    private final TextField txtChucVu = new TextField();
    private final TextField txtTrangThai = new TextField();

    // Thông tin tài khoản
    private final TextField txtTenDangNhap = new TextField();
    private final TextField txtTrangThaiTk = new TextField();

    // Nút đóng
    private final Button btnDong = new Button("Đóng");

    public XemChiTietNhanVienView(NhanVien nv) {
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề hộp thoại
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÔNG TIN CHI TIẾT NHÂN VIÊN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        String maNv = (nv != null && nv.getMaNhanVien() != null) ? nv.getMaNhanVien() : "-";
        String tenNv = (nv != null && nv.getHoTen() != null) ? nv.getHoTen() : "";
        Label lblDangXem = new Label("Mã nhân viên: " + maNv + (tenNv.isEmpty() ? "" : " - " + tenNv));
        lblDangXem.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblDangXem);

        // 2. Khung thông tin cá nhân & công việc
        VBox pnlThongTin = taoKhungThongTinNhanVien(nv);

        // 3. Khung thông tin tài khoản (nếu có)
        VBox pnlTaiKhoan = taoKhungThongTinTaiKhoan(nv);

        // 4. Nút Đóng
        HBox pnlNut = new HBox();
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());
        pnlNut.getChildren().add(btnDong);

        getChildren().addAll(pnlTieuDe, pnlThongTin, pnlTaiKhoan, pnlNut);
    }

    private VBox taoKhungThongTinNhanVien(NhanVien nv) {
        VBox pnlKhung = new VBox(11);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblTieuDePhan = new Label("Thông tin nhân sự (có thể bôi đen sao chép)");
        lblTieuDePhan.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(14);
        pnlLuoi.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints(110);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(110);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3, col4);

        caiDatOChiDoc(txtMaNhanVien);
        caiDatOChiDoc(txtHoTen);
        caiDatOChiDoc(txtSoDienThoai);
        caiDatOChiDoc(txtGioiTinh);
        caiDatOChiDoc(txtNgaySinh);
        caiDatOChiDoc(txtTuoi);
        caiDatOChiDoc(txtChucVu);
        caiDatOChiDoc(txtTrangThai);

        if (nv != null) {
            txtMaNhanVien.setText(nv.getMaNhanVien() != null ? nv.getMaNhanVien() : "-");
            txtHoTen.setText(nv.getHoTen() != null ? nv.getHoTen() : "-");
            txtSoDienThoai.setText(nv.getSoDienThoai() != null ? nv.getSoDienThoai() : "-");
            txtGioiTinh.setText(nv.getTenGioiTinh());
            txtNgaySinh.setText(nv.getNgaySinh() != null ? nv.getNgaySinh().format(DINH_DANG_NGAY) : "-");
            txtTuoi.setText(nv.getTuoi() + " tuổi");
            txtChucVu.setText(nv.getChucVu() != null ? nv.getChucVu() : "-");
            txtTrangThai.setText(nv.getTrangThai() != null ? nv.getTrangThai() : "-");
        }

        // Hàng 0: Mã NV & Họ tên
        pnlLuoi.add(new Label("Mã nhân viên:"), 0, 0);
        pnlLuoi.add(txtMaNhanVien, 1, 0);
        pnlLuoi.add(new Label("Họ và tên:"), 2, 0);
        pnlLuoi.add(txtHoTen, 3, 0);

        // Hàng 1: SĐT & Giới tính
        pnlLuoi.add(new Label("Số điện thoại:"), 0, 1);
        pnlLuoi.add(txtSoDienThoai, 1, 1);
        pnlLuoi.add(new Label("Giới tính:"), 2, 1);
        pnlLuoi.add(txtGioiTinh, 3, 1);

        // Hàng 2: Ngày sinh & Tuổi
        pnlLuoi.add(new Label("Ngày sinh:"), 0, 2);
        pnlLuoi.add(txtNgaySinh, 1, 2);
        pnlLuoi.add(new Label("Tuổi:"), 2, 2);
        pnlLuoi.add(txtTuoi, 3, 2);

        // Hàng 3: Chức vụ & Trạng thái làm việc
        pnlLuoi.add(new Label("Chức vụ:"), 0, 3);
        pnlLuoi.add(txtChucVu, 1, 3);
        pnlLuoi.add(new Label("Trạng thái:"), 2, 3);
        pnlLuoi.add(txtTrangThai, 3, 3);

        pnlKhung.getChildren().addAll(lblTieuDePhan, pnlLuoi);
        return pnlKhung;
    }

    private VBox taoKhungThongTinTaiKhoan(NhanVien nv) {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblTieuDePhan = new Label("Tài khoản hệ thống liên kết");
        lblTieuDePhan.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(14);
        pnlLuoi.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints(110);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(110);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3, col4);

        caiDatOChiDoc(txtTenDangNhap);
        caiDatOChiDoc(txtTrangThaiTk);

        if (nv != null && nv.getTenDangNhap() != null && !nv.getTenDangNhap().isBlank()) {
            txtTenDangNhap.setText(nv.getTenDangNhap());
            txtTrangThaiTk.setText(nv.isTaiKhoanHoatDong() ? "Hoạt động" : "Bị khóa");
        } else {
            txtTenDangNhap.setText("(Chưa cấp tài khoản)");
            txtTrangThaiTk.setText("-");
        }

        pnlLuoi.add(new Label("Tên đăng nhập:"), 0, 0);
        pnlLuoi.add(txtTenDangNhap, 1, 0);
        pnlLuoi.add(new Label("Trạng thái TK:"), 2, 0);
        pnlLuoi.add(txtTrangThaiTk, 3, 0);

        pnlKhung.getChildren().addAll(lblTieuDePhan, pnlLuoi);
        return pnlKhung;
    }

    private void caiDatOChiDoc(TextField tf) {
        tf.setEditable(false);
        tf.setFocusTraversable(true);
        tf.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 4px;");
    }

    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    public static void hienThi(Window owner, NhanVien nv) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) stage.initOwner(owner);

        String ma = nv != null ? nv.getMaNhanVien() : "";
        stage.setTitle("Chi tiết nhân viên - " + ma);

        XemChiTietNhanVienView view = new XemChiTietNhanVienView(nv);
        Scene scene = new Scene(view, 660, 450);
        try {
            if (XemChiTietNhanVienView.class.getResource(CSS_CHUNG) != null) {
                scene.getStylesheets().add(XemChiTietNhanVienView.class.getResource(CSS_CHUNG).toExternalForm());
            }
        } catch (Exception ignored) {}

        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
    }
}

