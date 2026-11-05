package com.petshop.gui;

import com.petshop.bus.XacThucBUS;
import com.petshop.entity.TaiKhoan;
import com.petshop.util.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.UnaryOperator;

/**
 * Màn hình Đăng nhập hệ thống (DangNhapView).
 * - Đăng nhập bằng Mã nhân viên (Bộ chỉ thị kỹ thuật IUH - Điều 5).
 * - Tích hợp đầy đủ chức năng "Quên mật khẩu" xác thực qua Mã nhân viên & SĐT chính chủ.
 * - Hỗ trợ inline validation tách riêng cho từng trường, viền đỏ và thông báo chi tiết bằng CSS.
 */
public class DangNhapView extends StackPane {

    private final Label lblTieuDe = new Label("CỬA HÀNG THÚ CƯNG HAPPY TAILS");
    private final Label lblTieuDePhu = new Label("HỆ THỐNG QUẢN LÝ CỬA HÀNG");
    private final Label lblTenDangNhap = new Label("Mã nhân viên:");
    private final Label lblMatKhau = new Label("Mật khẩu:");

    private final Label lblLoiTenDangNhap = new Label();
    private final Label lblLoiMatKhau = new Label();

    private final TextField txtTenDangNhap = new TextField();
    private final PasswordField pwdMatKhau = new PasswordField();
    private final TextField txtMatKhauHien = new TextField();
    private final Button btnAnHienMatKhau = new Button();
    private final SVGPath iconMatKhau = new SVGPath();

    private static final String SVG_MAT_MO = "M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z";
    private static final String SVG_MAT_GACH = "M9.88 9.88a3 3 0 1 0 4.24 4.24 M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68 M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61 M2 2l20 20";

    private final Hyperlink lnkQuenMatKhau = new Hyperlink("Quên mật khẩu?");
    private final Button btnDangNhap = new Button("Đăng nhập");

    private final XacThucBUS dangNhapController = new XacThucBUS();
    private final Stage stage;

    public DangNhapView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("man-hinh-dang-nhap");

        VBox pnlHopDangNhap = new VBox(14);
        pnlHopDangNhap.getStyleClass().add("hop-dang-nhap");
        pnlHopDangNhap.setAlignment(Pos.CENTER);

        VBox pnlTieuDe = new VBox(6);
        pnlTieuDe.setAlignment(Pos.CENTER);

        try {
            var luongIcon = getClass().getResourceAsStream("/com/petshop/gui/logo.png");
            if (luongIcon != null) {
                ImageView imgLogo = new ImageView(new Image(luongIcon));
                imgLogo.setFitWidth(48);
                imgLogo.setFitHeight(48);
                imgLogo.setPreserveRatio(true);
                pnlTieuDe.getChildren().add(imgLogo);
            }
        } catch (Exception ignored) {}

        lblTieuDe.getStyleClass().add("tieu-de-chinh");
        lblTieuDePhu.getStyleClass().add("tieu-de-phu");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblTieuDePhu);

        lblLoiTenDangNhap.getStyleClass().add("thong-bao-loi-inline");
        lblLoiTenDangNhap.setVisible(false);

        lblLoiMatKhau.getStyleClass().add("thong-bao-loi-inline");
        lblLoiMatKhau.setVisible(false);

        GridPane pnlLuoiNhap = new GridPane();
        pnlLuoiNhap.setHgap(12);
        pnlLuoiNhap.setVgap(6);
        pnlLuoiNhap.setAlignment(Pos.CENTER);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(115);
        colNhan.setPrefWidth(120);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoiNhap.getColumnConstraints().addAll(colNhan, colNhap);

        lblTenDangNhap.getStyleClass().add("nhan-nhap-lieu");
        lblTenDangNhap.setMinWidth(Region.USE_PREF_SIZE);
        lblMatKhau.getStyleClass().add("nhan-nhap-lieu");
        lblMatKhau.setMinWidth(Region.USE_PREF_SIZE);

        txtTenDangNhap.setPromptText("Nhập mã nhân viên (VD: 260001)");
        txtTenDangNhap.setPrefWidth(260);
        txtTenDangNhap.setMaxWidth(Double.MAX_VALUE);

        pwdMatKhau.setPromptText("Nhập mật khẩu");
        pwdMatKhau.setPrefWidth(260);
        pwdMatKhau.setMaxWidth(Double.MAX_VALUE);
        pwdMatKhau.setStyle("-fx-padding: 4 30 4 8;");

        txtMatKhauHien.setPromptText("Nhập mật khẩu");
        txtMatKhauHien.setPrefWidth(260);
        txtMatKhauHien.setMaxWidth(Double.MAX_VALUE);
        txtMatKhauHien.setStyle("-fx-padding: 4 30 4 8;");
        txtMatKhauHien.setVisible(false);
        txtMatKhauHien.setManaged(false);

        // Đồng bộ dữ liệu 2 chiều giữa PasswordField và TextField
        txtMatKhauHien.textProperty().bindBidirectional(pwdMatKhau.textProperty());

        // Cấu hình icon mắt chuẩn vector (mắt thường khi ẩn, mắt gạch chéo khi hiện)
        iconMatKhau.setContent(SVG_MAT_MO);
        iconMatKhau.setFill(Color.TRANSPARENT);
        iconMatKhau.setStroke(Color.web("#64748b"));
        iconMatKhau.setStrokeWidth(1.8);
        iconMatKhau.setStrokeLineCap(StrokeLineCap.ROUND);
        iconMatKhau.setStrokeLineJoin(StrokeLineJoin.ROUND);
        iconMatKhau.setScaleX(0.65);
        iconMatKhau.setScaleY(0.65);

        btnAnHienMatKhau.setGraphic(iconMatKhau);
        btnAnHienMatKhau.setFocusTraversable(false);
        btnAnHienMatKhau.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0 8 0 0;");
        btnAnHienMatKhau.setOnMouseEntered(e -> iconMatKhau.setStroke(Color.web("#0284c7")));
        btnAnHienMatKhau.setOnMouseExited(e -> iconMatKhau.setStroke(Color.web("#64748b")));
        btnAnHienMatKhau.setOnAction(e -> {
            boolean dangHien = txtMatKhauHien.isVisible();
            txtMatKhauHien.setVisible(!dangHien);
            txtMatKhauHien.setManaged(!dangHien);
            pwdMatKhau.setVisible(dangHien);
            pwdMatKhau.setManaged(dangHien);
            iconMatKhau.setContent(dangHien ? SVG_MAT_MO : SVG_MAT_GACH);
            if (!dangHien) {
                txtMatKhauHien.requestFocus();
                txtMatKhauHien.positionCaret(txtMatKhauHien.getText() != null ? txtMatKhauHien.getText().length() : 0);
            } else {
                pwdMatKhau.requestFocus();
                pwdMatKhau.positionCaret(pwdMatKhau.getText() != null ? pwdMatKhau.getText().length() : 0);
            }
        });

        StackPane pnlKhungMatKhau = new StackPane(pwdMatKhau, txtMatKhauHien, btnAnHienMatKhau);
        StackPane.setAlignment(btnAnHienMatKhau, Pos.CENTER_RIGHT);

        txtTenDangNhap.textProperty().addListener((obs, o, n) -> {
            xoaLoiTenDangNhap();
            xoaLoiMatKhau();
        });
        pwdMatKhau.textProperty().addListener((obs, o, n) -> {
            xoaLoiTenDangNhap();
            xoaLoiMatKhau();
        });

        txtTenDangNhap.setOnAction(e -> xuLyDangNhap());
        pwdMatKhau.setOnAction(e -> xuLyDangNhap());
        txtMatKhauHien.setOnAction(e -> xuLyDangNhap());

        pnlLuoiNhap.add(lblTenDangNhap, 0, 0);
        pnlLuoiNhap.add(txtTenDangNhap, 1, 0);
        pnlLuoiNhap.add(lblLoiTenDangNhap, 1, 1);

        pnlLuoiNhap.add(lblMatKhau, 0, 2);
        pnlLuoiNhap.add(pnlKhungMatKhau, 1, 2);
        pnlLuoiNhap.add(lblLoiMatKhau, 1, 3);

        // Thanh thao tác phía dưới: Liên kết Quên mật khẩu bên trái, Nút Đăng nhập bên phải
        HBox pnlNutThaoTac = new HBox(12);
        pnlNutThaoTac.setAlignment(Pos.CENTER_LEFT);

        lnkQuenMatKhau.setStyle("-fx-text-fill: #0284c7; -fx-font-size: 12.5px; -fx-cursor: hand; -fx-underline: true;");
        lnkQuenMatKhau.setOnAction(e -> moDialogQuenMatKhau());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnDangNhap.getStyleClass().add("nut-chinh");
        btnDangNhap.setPrefWidth(110);
        btnDangNhap.setOnAction(e -> xuLyDangNhap());

        pnlNutThaoTac.getChildren().addAll(lnkQuenMatKhau, spacer, btnDangNhap);

        pnlHopDangNhap.getChildren().addAll(
                pnlTieuDe,
                pnlLuoiNhap,
                pnlNutThaoTac
        );

        Label lblChanTrang = new Label("Khoa Công nghệ Thông tin - Trường Đại học Công nghiệp TP.HCM");
        lblChanTrang.getStyleClass().add("dong-chan-trang");
        StackPane.setAlignment(lblChanTrang, Pos.BOTTOM_CENTER);
        StackPane.setMargin(lblChanTrang, new Insets(0, 0, 14, 0));

        getChildren().addAll(pnlHopDangNhap, lblChanTrang);
    }

    private void xoaLoiTenDangNhap() {
        txtTenDangNhap.getStyleClass().remove("o-nhap-loi");
        if (lblLoiTenDangNhap.isVisible()) {
            lblLoiTenDangNhap.setVisible(false);
            lblLoiTenDangNhap.setText("");
        }
    }

    private void xoaLoiMatKhau() {
        pwdMatKhau.getStyleClass().remove("o-nhap-loi");
        txtMatKhauHien.getStyleClass().remove("o-nhap-loi");
        if (lblLoiMatKhau.isVisible()) {
            lblLoiMatKhau.setVisible(false);
            lblLoiMatKhau.setText("");
        }
    }

    private void hienLoiTenDangNhap(String thongBao) {
        if (!txtTenDangNhap.getStyleClass().contains("o-nhap-loi")) {
            txtTenDangNhap.getStyleClass().add("o-nhap-loi");
        }
        lblLoiTenDangNhap.setText(thongBao);
        lblLoiTenDangNhap.setVisible(true);
    }

    private void hienLoiMatKhau(String thongBao) {
        if (!pwdMatKhau.getStyleClass().contains("o-nhap-loi")) {
            pwdMatKhau.getStyleClass().add("o-nhap-loi");
        }
        if (!txtMatKhauHien.getStyleClass().contains("o-nhap-loi")) {
            txtMatKhauHien.getStyleClass().add("o-nhap-loi");
        }
        lblLoiMatKhau.setText(thongBao);
        lblLoiMatKhau.setVisible(true);
    }

    private void xuLyDangNhap() {
        xoaLoiTenDangNhap();
        xoaLoiMatKhau();

        String tenDangNhap = txtTenDangNhap.getText() != null ? txtTenDangNhap.getText().trim() : "";
        String matKhau = pwdMatKhau.getText() != null ? pwdMatKhau.getText() : "";

        boolean coLoi = false;
        
        if (tenDangNhap.isEmpty()) {
            hienLoiTenDangNhap("Vui lòng nhập mã nhân viên");
            txtTenDangNhap.requestFocus();
            coLoi = true;
        }

        if (matKhau.isEmpty()) {
            hienLoiMatKhau("Mật khẩu không được để trống");
            if (!coLoi) {
                if (txtMatKhauHien.isVisible()) {
                    txtMatKhauHien.requestFocus();
                } else {
                    pwdMatKhau.requestFocus();
                }
            }
            coLoi = true;
        }

        if (coLoi) return;

        TaiKhoan taiKhoan;
        try {
            taiKhoan = dangNhapController.xacThuc(tenDangNhap, matKhau);
        } catch (IllegalStateException ex) {
            hienThiCanhBao("Lỗi hệ thống", "Lỗi kết nối cơ sở dữ liệu SQL Server!");
            return;
        }

        // Sai thông tin đăng nhập
        if (taiKhoan == null) {
            pwdMatKhau.clear(); 
            
            if (!txtTenDangNhap.getStyleClass().contains("o-nhap-loi")) {
                txtTenDangNhap.getStyleClass().add("o-nhap-loi");
            }
            
            hienLoiMatKhau("Mã nhân viên hoặc mật khẩu không chính xác");
            
            if (txtMatKhauHien.isVisible()) {
                txtMatKhauHien.requestFocus();
            } else {
                pwdMatKhau.requestFocus();
            }
            return;
        }

        SessionManager.dangNhap(taiKhoan);

        stage.setTitle("Happy Tails - Quản lý cửa hàng thú cưng");
        Scene scene = new Scene(new ManHinhChinhView(stage), 1280, 760);
        scene.getStylesheets().addAll(
                getClass().getResource("/com/petshop/gui/css/chung.css").toExternalForm(),
                getClass().getResource("/com/petshop/gui/css/manhinhchinh.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    /**
     * Mở hộp thoại Modal Quên mật khẩu để nhân viên tự đặt lại mật khẩu mới
     * thông qua xác thực Mã nhân viên và Số điện thoại chính chủ.
     */
    private void moDialogQuenMatKhau() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(stage);
        dlg.setTitle("Khôi phục mật khẩu - Happy Tails");

        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(20));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // Tiêu đề Dialog
        Label lblTieuDeDlg = new Label("KHÔI PHỤC MẬT KHẨU");
        lblTieuDeDlg.getStyleClass().add("tieu-de-phan-he");
        Label lblHuongDan = new Label("Nhập Mã nhân viên và Số điện thoại đã đăng ký để xác minh và đặt mật khẩu mới.");
        lblHuongDan.getStyleClass().add("mo-ta-phan-he");

        VBox pnlForm = new VBox(12);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        TextField txtMaNv = new TextField();
        txtMaNv.setPromptText("VD: 260001");
        if (txtTenDangNhap.getText() != null && !txtTenDangNhap.getText().isBlank()) {
            txtMaNv.setText(txtTenDangNhap.getText().trim());
        }

        TextField txtSdt = new TextField();
        txtSdt.setPromptText("Nhập 10 chữ số (VD: 0912345678)");
        // Lọc chỉ cho nhập số tối đa 10 chữ số
        UnaryOperator<TextFormatter.Change> sdtFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,10}")) {
                return change;
            }
            return null;
        };
        txtSdt.setTextFormatter(new TextFormatter<>(sdtFilter));

        PasswordField pwdMoi = new PasswordField();
        pwdMoi.setPromptText("Tối thiểu 4 ký tự");

        PasswordField pwdXacNhan = new PasswordField();
        pwdXacNhan.setPromptText("Nhập lại mật khẩu mới");

        Label lblThongBaoLoi = new Label();
        lblThongBaoLoi.getStyleClass().add("thong-bao-loi-inline");
        lblThongBaoLoi.setVisible(false);

        pnlLuoi.add(new Label("Mã nhân viên (*):"), 0, 0);
        pnlLuoi.add(txtMaNv, 1, 0);
        pnlLuoi.add(new Label("Số điện thoại (*):"), 0, 1);
        pnlLuoi.add(txtSdt, 1, 1);
        pnlLuoi.add(new Label("Mật khẩu mới (*):"), 0, 2);
        pnlLuoi.add(pwdMoi, 1, 2);
        pnlLuoi.add(new Label("Xác nhận MK (*):"), 0, 3);
        pnlLuoi.add(pwdXacNhan, 1, 3);
        pnlLuoi.add(lblThongBaoLoi, 1, 4);

        ColumnConstraints col1 = new ColumnConstraints(130);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> dlg.close());

        Button btnXacNhan = new Button("Đặt lại mật khẩu");
        btnXacNhan.getStyleClass().add("nut-chinh");

        btnXacNhan.setOnAction(e -> {
            lblThongBaoLoi.setVisible(false);
            String ma = txtMaNv.getText() != null ? txtMaNv.getText().trim() : "";
            String sdt = txtSdt.getText() != null ? txtSdt.getText().trim() : "";
            String passMoi = pwdMoi.getText() != null ? pwdMoi.getText() : "";
            String passXn = pwdXacNhan.getText() != null ? pwdXacNhan.getText() : "";

            try {
                dangNhapController.datLaiMatKhau(ma, sdt, passMoi, passXn);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đặt lại mật khẩu thành công!\nBạn có thể đăng nhập ngay với mật khẩu mới.");
                alert.showAndWait();

                dlg.close();

                // Tự động điền mã nhân viên vào màn hình đăng nhập
                txtTenDangNhap.setText(ma);
                pwdMatKhau.clear();
                pwdMatKhau.requestFocus();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                lblThongBaoLoi.setText(ex.getMessage());
                lblThongBaoLoi.setVisible(true);
            }
        });

        pnlNut.getChildren().addAll(btnHuy, btnXacNhan);
        pnlForm.getChildren().addAll(pnlLuoi, pnlNut);
        pnlGoc.getChildren().addAll(lblTieuDeDlg, lblHuongDan, pnlForm);

        Scene scene = new Scene(pnlGoc, 480, 350);
        try {
            scene.getStylesheets().addAll(
                    getClass().getResource("/com/petshop/gui/css/chung.css").toExternalForm(),
                    getClass().getResource("/com/petshop/gui/css/dangnhap.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        dlg.setScene(scene);
        dlg.setResizable(false);
        dlg.showAndWait();
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
