package com.petshop.view;

import com.petshop.controller.DangNhapController;
import com.petshop.model.TaiKhoan;
import com.petshop.util.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Màn hình Đăng nhập hệ thống (DangNhapView).
 * Hỗ trợ inline validation tách riêng cho từng trường, viền đỏ và thông báo chi tiết bằng CSS.
 */
public class DangNhapView extends StackPane {

    private final Label lblTieuDe = new Label("CỬA HÀNG THÚ CƯNG HAPPY TAILS");
    private final Label lblTieuDePhu = new Label("HỆ THỐNG QUẢN LÝ CỬA HÀNG");
    private final Label lblTenDangNhap = new Label("Tên đăng nhập:");
    private final Label lblMatKhau = new Label("Mật khẩu:");

    private final Label lblLoiTenDangNhap = new Label();
    private final Label lblLoiMatKhau = new Label();

    private final TextField txtTenDangNhap = new TextField();
    private final PasswordField pwdMatKhau = new PasswordField();

    private final Button btnDangNhap = new Button("Đăng nhập");

    private final DangNhapController dangNhapController = new DangNhapController();
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
            var luongIcon = getClass().getResourceAsStream("/com/petshop/view/logo.png");
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

        lblTenDangNhap.getStyleClass().add("nhan-nhap-lieu");
        lblMatKhau.getStyleClass().add("nhan-nhap-lieu");

        txtTenDangNhap.setPromptText("Nhập tên đăng nhập");
        txtTenDangNhap.setPrefWidth(300);

        pwdMatKhau.setPromptText("Nhập mật khẩu");
        pwdMatKhau.setPrefWidth(300);

        txtTenDangNhap.textProperty().addListener((obs, o, n) -> {
            xoaLoiTenDangNhap();
            xoaLoiMatKhau(); // Xóa luôn lỗi chung nếu có
        });
        pwdMatKhau.textProperty().addListener((obs, o, n) -> {
            xoaLoiTenDangNhap(); // Xóa viền đỏ ô trên nếu có
            xoaLoiMatKhau();
        });
        txtTenDangNhap.setOnAction(e -> xuLyDangNhap());
        pwdMatKhau.setOnAction(e -> xuLyDangNhap());

        pnlLuoiNhap.add(lblTenDangNhap, 0, 0);
        pnlLuoiNhap.add(txtTenDangNhap, 1, 0);
        pnlLuoiNhap.add(lblLoiTenDangNhap, 1, 1);

        pnlLuoiNhap.add(lblMatKhau, 0, 2);
        pnlLuoiNhap.add(pwdMatKhau, 1, 2);
        pnlLuoiNhap.add(lblLoiMatKhau, 1, 3);

        HBox pnlNutThaoTac = new HBox(12);
        pnlNutThaoTac.setAlignment(Pos.CENTER_RIGHT);

        btnDangNhap.getStyleClass().add("nut-chinh");
        btnDangNhap.setPrefWidth(110);
        btnDangNhap.setOnAction(e -> xuLyDangNhap());

        pnlNutThaoTac.getChildren().add(btnDangNhap);

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
            hienLoiTenDangNhap("Vui lòng nhập tên đăng nhập");
            txtTenDangNhap.requestFocus();
            coLoi = true;
        }

        if (matKhau.isEmpty()) {
            hienLoiMatKhau("Mật khẩu không được để trống");
            if (!coLoi) {
                pwdMatKhau.requestFocus();
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
            // 1. Phải clear mật khẩu trước để nó kích hoạt xóa lỗi cũ (nếu có)
            pwdMatKhau.clear(); 
            
            // 2. Sau đó mới bôi đỏ viền ô Tên đăng nhập
            if (!txtTenDangNhap.getStyleClass().contains("o-nhap-loi")) {
                txtTenDangNhap.getStyleClass().add("o-nhap-loi");
            }
            
            // 3. Hiện lỗi và bôi đỏ ô Mật khẩu
            hienLoiMatKhau("Tên đăng nhập hoặc mật khẩu không chính xác");
            
            // 4. Đưa con trỏ chuột vào ô mật khẩu
            pwdMatKhau.requestFocus();
            return;
        }

        SessionManager.dangNhap(taiKhoan);

        stage.setTitle("Happy Tails - Quản lý cửa hàng thú cưng");
        Scene scene = new Scene(new ManHinhChinhView(stage), 1280, 760);
        scene.getStylesheets().addAll(
                getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm(),
                getClass().getResource("/com/petshop/view/css/manhinhchinh.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}