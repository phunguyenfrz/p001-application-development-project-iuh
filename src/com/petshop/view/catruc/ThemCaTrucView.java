package com.petshop.view.catruc;

import com.petshop.controller.CaTrucController;
import com.petshop.model.Ca;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Màn hình (popup) thêm ca trực mới (SHF_CRUD).
 */
public class ThemCaTrucView extends VBox {

    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final CaTrucController caTrucController = new CaTrucController();
    private final Runnable khiThemThanhCong;

    private final TextField txtMaCa = new TextField();
    private final TextField txtTenCa = new TextField();
    private final TextField txtGioBatDau = new TextField();
    private final TextField txtGioKetThuc = new TextField();

    private final Button btnThem = new Button("Lưu thêm mới");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

    public ThemCaTrucView() {
        this(null);
    }

    public ThemCaTrucView(Runnable khiThemThanhCong) {
        this.khiThemThanhCong = khiThemThanhCong;
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM CA TRỰC MỚI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Nhập tên ca và thiết lập khung giờ làm việc bắt đầu, kết thúc.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlForm = taoKhungNhapLieu();
        getChildren().addAll(pnlTieuDe, pnlForm);

        xoaTrangForm();
    }

    private VBox taoKhungNhapLieu() {
        VBox pnlKhung = new VBox(14);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        txtMaCa.setEditable(false);
        txtMaCa.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;");
        txtMaCa.setPromptText("Hệ thống tự động sinh mã");

        txtTenCa.setPromptText("Nhập tên ca (VD: Ca Sáng, Ca Chiều, Ca Tối)");
        txtGioBatDau.setPromptText("Định dạng HH:mm (VD: 08:00)");
        txtGioKetThuc.setPromptText("Định dạng HH:mm (VD: 12:00)");

        pnlLuoi.add(new Label("Mã ca trực:"), 0, 0);
        pnlLuoi.add(txtMaCa, 1, 0);

        pnlLuoi.add(new Label("Tên ca trực (*):"), 0, 1);
        pnlLuoi.add(txtTenCa, 1, 1);

        pnlLuoi.add(new Label("Giờ bắt đầu (*):"), 0, 2);
        pnlLuoi.add(txtGioBatDau, 1, 2);

        pnlLuoi.add(new Label("Giờ kết thúc (*):"), 0, 3);
        pnlLuoi.add(txtGioKetThuc, 1, 3);

        Label lblGhiChu = new Label("💡 Giờ nhập theo định dạng HH:mm (24 giờ). Giờ kết thúc phải sau giờ bắt đầu.");
        lblGhiChu.setStyle("-fx-font-style: italic; -fx-text-fill: #4b5563; -fx-font-size: 11.5px;");
        pnlLuoi.add(lblGhiChu, 1, 4);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(150);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnThem.getStyleClass().add("nut-chinh");
        btnThem.setOnAction(e -> xuLyThemCa());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xoaTrangForm());

        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().addAll(btnThem, btnLamMoi, btnDong);
        pnlKhung.getChildren().addAll(pnlLuoi, pnlNut);
        return pnlKhung;
    }

    private void xuLyThemCa() {
        try {
            String tenCa = txtTenCa.getText() != null ? txtTenCa.getText().trim() : "";
            if (tenCa.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập tên ca trực!");
            }

            String strBatDau = txtGioBatDau.getText() != null ? txtGioBatDau.getText().trim() : "";
            String strKetThuc = txtGioKetThuc.getText() != null ? txtGioKetThuc.getText().trim() : "";

            if (strBatDau.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập giờ bắt đầu (HH:mm)!");
            }
            if (strKetThuc.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập giờ kết thúc (HH:mm)!");
            }

            LocalTime gioBatDau;
            LocalTime gioKetThuc;
            try {
                gioBatDau = LocalTime.parse(strBatDau, DINH_DANG_GIO);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Giờ bắt đầu không đúng định dạng HH:mm (VD: 08:00)!");
            }

            try {
                gioKetThuc = LocalTime.parse(strKetThuc, DINH_DANG_GIO);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Giờ kết thúc không đúng định dạng HH:mm (VD: 12:00)!");
            }

            String maCaMoi = caTrucController.taoMaCa();
            Ca caMoi = new Ca(maCaMoi, tenCa, gioBatDau, gioKetThuc, Ca.TRANG_THAI_AP_DUNG);

            boolean ketQua = caTrucController.themCa(caMoi);
            if (ketQua) {
                Alert tb = new Alert(Alert.AlertType.INFORMATION, "Thêm ca trực mới thành công!", ButtonType.OK);
                tb.setHeaderText(null);
                tb.showAndWait();

                if (khiThemThanhCong != null) {
                    khiThemThanhCong.run();
                }
                dongCuaSo();
            } else {
                throw new IllegalStateException("Không thể lưu ca trực vào cơ sở dữ liệu!");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            Alert tb = new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK);
            tb.setHeaderText("Thông báo");
            tb.showAndWait();
        } catch (Exception ex) {
            Alert tb = new Alert(Alert.AlertType.ERROR, "Lỗi không xác định: " + ex.getMessage(), ButtonType.OK);
            tb.setHeaderText("Lỗi");
            tb.showAndWait();
        }
    }

    private void xoaTrangForm() {
        txtMaCa.setText(caTrucController.taoMaCa());
        txtTenCa.clear();
        txtGioBatDau.setText("08:00");
        txtGioKetThuc.setText("12:00");
        txtTenCa.requestFocus();
    }

    private void dongCuaSo() {
        Stage stage = (Stage) getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}

