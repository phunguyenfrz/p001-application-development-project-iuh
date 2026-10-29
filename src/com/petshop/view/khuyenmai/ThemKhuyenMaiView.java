package com.petshop.view.khuyenmai;

import com.petshop.controller.KhuyenMaiController;
import com.petshop.model.KhuyenMai;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;

/**
 * Màn hình nhỏ nhập chương trình khuyến mãi mới.
 */
public class ThemKhuyenMaiView extends VBox {

    private final KhuyenMaiController controller = new KhuyenMaiController();
    private final Runnable khiThemThanhCong;
    private final TextField txtTenChuongTrinh = new TextField();
    private final TextField txtMucGiam = new TextField();
    private final DatePicker dpNgayBatDau = new DatePicker();
    private final DatePicker dpNgayKetThuc = new DatePicker();
    private final Button btnThem = new Button("Lưu thêm mới");

    public ThemKhuyenMaiView() {
        this(null);
    }

    public ThemKhuyenMaiView(Runnable khiThemThanhCong) {
        this.khiThemThanhCong = khiThemThanhCong;
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("THÊM CHƯƠNG TRÌNH KHUYẾN MÃI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Khuyến mãi được áp dụng cho tổng hóa đơn; mỗi hóa đơn tối đa một mã.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        getChildren().addAll(lblTieuDe, lblMoTa, taoKhungNhapLieu());

        txtMucGiam.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d{0,2})?")) {
                txtMucGiam.setText(cu);
            }
        });
    }

    private VBox taoKhungNhapLieu() {
        VBox khung = new VBox(14);
        khung.getStyleClass().add("hop-noi-dung");
        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);

        txtTenChuongTrinh.setPromptText("Nhập tên chương trình khuyến mãi");
        txtMucGiam.setPromptText("Ví dụ: 10 hoặc 12.5");
        dpNgayBatDau.setPromptText("Ngày bắt đầu");
        dpNgayKetThuc.setPromptText("Ngày kết thúc");

        luoi.add(new Label("Tên chương trình (*):"), 0, 0);
        luoi.add(txtTenChuongTrinh, 1, 0);
        luoi.add(new Label("Mức giảm (%)(*):"), 0, 1);
        luoi.add(txtMucGiam, 1, 1);
        luoi.add(new Label("Ngày bắt đầu (*):"), 0, 2);
        luoi.add(dpNgayBatDau, 1, 2);
        luoi.add(new Label("Ngày kết thúc (*):"), 0, 3);
        luoi.add(dpNgayKetThuc, 1, 3);
        GridPane.setHgrow(txtTenChuongTrinh, Priority.ALWAYS);
        GridPane.setHgrow(txtMucGiam, Priority.ALWAYS);
        GridPane.setHgrow(dpNgayBatDau, Priority.ALWAYS);
        GridPane.setHgrow(dpNgayKetThuc, Priority.ALWAYS);

        HBox nut = new HBox(10);
        nut.setAlignment(Pos.CENTER_RIGHT);
        Button btnLamMoi = new Button("Làm mới");
        Button btnDong = new Button("Đóng");
        btnThem.getStyleClass().add("nut-chinh");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnDong.getStyleClass().add("nut-phu");
        btnThem.setOnAction(e -> xuLyThem());
        btnLamMoi.setOnAction(e -> xoaTrangForm());
        btnDong.setOnAction(e -> dongCuaSo());
        nut.getChildren().addAll(btnLamMoi, btnDong, btnThem);
        khung.getChildren().addAll(luoi, nut);
        return khung;
    }

    private KhuyenMai docForm() {
        String ten = txtTenChuongTrinh.getText() == null ? "" : txtTenChuongTrinh.getText().trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên chương trình khuyến mãi!");
        }
        String chuoiGiam = txtMucGiam.getText() == null ? "" : txtMucGiam.getText().trim();
        if (chuoiGiam.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mức giảm!");
        }
        BigDecimal mucGiam;
        try {
            mucGiam = new BigDecimal(chuoiGiam);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mức giảm phải là số hợp lệ!");
        }
        if (dpNgayBatDau.getValue() == null || dpNgayKetThuc.getValue() == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ ngày bắt đầu và ngày kết thúc!");
        }
        return new KhuyenMai(null, ten, mucGiam, dpNgayBatDau.getValue(), dpNgayKetThuc.getValue(), true);
    }

    private void xuLyThem() {
        try {
            KhuyenMai khuyenMai = docForm();
            if (controller.themKhuyenMai(khuyenMai)) {
                hienThiThongBao("Thành công", "Đã thêm chương trình: " + khuyenMai.getTenChuongTrinh());
                if (khiThemThanhCong != null) {
                    khiThemThanhCong.run();
                }
                dongCuaSo();
            } else {
                hienThiLoi("Không thể thêm chương trình khuyến mãi!");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void xoaTrangForm() {
        txtTenChuongTrinh.clear();
        txtMucGiam.clear();
        dpNgayBatDau.setValue(null);
        dpNgayKetThuc.setValue(null);
        txtTenChuongTrinh.requestFocus();
    }

    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
