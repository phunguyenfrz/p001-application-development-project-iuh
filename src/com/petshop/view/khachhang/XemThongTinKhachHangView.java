package com.petshop.view.khachhang;

import com.petshop.model.KhachHang;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Hộp thoại (Modal Dialog) hiển thị thông tin chi tiết của khách hàng.
 * Chế độ chỉ đọc (Read-only): Toàn bộ các trường dữ liệu đều bị khóa chỉnh sửa (setEditable(false)),
 * chỉ có nút "Đóng" để quay lại màn hình trước.
 */
public class XemThongTinKhachHangView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";

    // Các ô hiển thị dữ liệu (chỉ đọc)
    private final TextField txtMaKhachHang = new TextField();
    private final TextField txtHoTen = new TextField();
    private final TextField txtSoDienThoai = new TextField();
    private final TextField txtEmail = new TextField();
    private final TextField txtDiemTichLuy = new TextField();
    private final TextField txtTrangThai = new TextField();
    private final TextField txtDiaChi = new TextField();
    private final TextArea txtGhiChu = new TextArea();

    // Nút đóng hộp thoại
    private final Button btnDong = new Button("Đóng");

    public XemThongTinKhachHangView(KhachHang khachHang) {
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề hộp thoại
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÔNG TIN CHI TIẾT KHÁCH HÀNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblDangXem = new Label("Mã khách hàng: " + (khachHang != null ? khachHang.getMaKhachHang() : "-"));
        lblDangXem.getStyleClass().add("nhan-dang-chon");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblDangXem);

        // 2. Khung nội dung hiển thị thông tin
        VBox pnlForm = taoKhungThongTin(khachHang);

        getChildren().addAll(pnlTieuDe, pnlForm);
    }

    /**
     * Khởi tạo giao diện lưới hiển thị thông tin chi tiết của khách hàng ở chế độ Chỉ đọc.
     */
    private VBox taoKhungThongTin(KhachHang kh) {
        VBox pnlKhung = new VBox(14);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        // Nạp dữ liệu vào các trường
        if (kh != null) {
            txtMaKhachHang.setText(kh.getMaKhachHang() != null ? kh.getMaKhachHang() : "");
            txtHoTen.setText(kh.getHoTen() != null ? kh.getHoTen() : "");
            txtSoDienThoai.setText(kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "");
            txtEmail.setText(kh.getEmail() != null ? kh.getEmail() : "(Chưa cập nhật)");
            txtDiemTichLuy.setText(kh.getDiemTichLuy() + " điểm");
            txtTrangThai.setText(kh.isTrangThai() ? "Đang hoạt động" : "Ngừng hoạt động");
            txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "(Chưa cập nhật)");
            txtGhiChu.setText(kh.getGhiChu() != null ? kh.getGhiChu() : "");
        }

        // =====================================================================
        // CHẾ ĐỘ CHỈ ĐỌC (READ-ONLY): Khóa toàn bộ các ô nhập dữ liệu
        // =====================================================================
        txtMaKhachHang.setDisable(true);
        txtMaKhachHang.getStyleClass().add("o-nhap-ma-tu-dong");

        txtHoTen.setEditable(false);
        txtSoDienThoai.setEditable(false);
        txtEmail.setEditable(false);
        txtDiemTichLuy.setEditable(false);
        txtTrangThai.setEditable(false);
        txtDiaChi.setEditable(false);

        txtGhiChu.setEditable(false);
        txtGhiChu.setPrefRowCount(3);
        txtGhiChu.setWrapText(true);

        // Thêm vào lưới hiển thị
        pnlLuoi.add(new Label("Mã khách hàng:"), 0, 0);
        pnlLuoi.add(txtMaKhachHang, 1, 0);

        pnlLuoi.add(new Label("Họ và tên:"), 0, 1);
        pnlLuoi.add(txtHoTen, 1, 1);

        pnlLuoi.add(new Label("Số điện thoại:"), 0, 2);
        pnlLuoi.add(txtSoDienThoai, 1, 2);

        pnlLuoi.add(new Label("Email:"), 0, 3);
        pnlLuoi.add(txtEmail, 1, 3);

        pnlLuoi.add(new Label("Điểm tích lũy:"), 0, 4);
        pnlLuoi.add(txtDiemTichLuy, 1, 4);

        pnlLuoi.add(new Label("Trạng thái:"), 0, 5);
        pnlLuoi.add(txtTrangThai, 1, 5);

        pnlLuoi.add(new Label("Địa chỉ:"), 0, 6);
        pnlLuoi.add(txtDiaChi, 1, 6);

        pnlLuoi.add(new Label("Ghi chú:"), 0, 7);
        pnlLuoi.add(txtGhiChu, 1, 7);

        // Định dạng độ rộng cột
        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(140);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        // Thanh nút đóng (căn góc phải)
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().add(btnDong);
        pnlKhung.getChildren().addAll(pnlLuoi, pnlNut);
        return pnlKhung;
    }

    /**
     * Đóng cửa sổ Stage chứa màn hình này.
     */
    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    /**
     * Phương thức tĩnh tiện ích mở nhanh hộp thoại xem thông tin khách hàng dạng modal dialog.
     *
     * @param khachHang Khách hàng cần xem thông tin
     * @param cuaSoCha Cửa sổ cha (Window owner) để hiển thị modal
     */
    public static void hienThi(KhachHang khachHang, Window cuaSoCha) {
        if (khachHang == null) {
            return;
        }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (cuaSoCha != null) {
            dlg.initOwner(cuaSoCha);
        }
        dlg.setTitle("Thông tin chi tiết khách hàng - " + khachHang.getMaKhachHang());

        XemThongTinKhachHangView pnlNoiDung = new XemThongTinKhachHangView(khachHang);
        Scene scene = new Scene(pnlNoiDung, 520, 520);

        if (XemThongTinKhachHangView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(XemThongTinKhachHangView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        dlg.setScene(scene);
        dlg.setResizable(false);
        dlg.showAndWait();
    }
}

