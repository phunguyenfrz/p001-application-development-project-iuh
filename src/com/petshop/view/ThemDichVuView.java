package com.petshop.view;

import com.petshop.controller.DichVuChamSocController;
import com.petshop.model.DichVuChamSoc;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Màn hình thêm mới dịch vụ chăm sóc thú cưng kèm bảng giá 3 bậc cân nặng (Nhỏ, Vừa, Lớn).
 */
public class ThemDichVuView extends VBox {

    private final DichVuChamSocController dichVuController = new DichVuChamSocController();

    private final TextField txtMaDichVu = new TextField();
    private final TextField txtTenDichVu = new TextField();
    private final TextField txtThoiLuong = new TextField();
    private final TextField txtGiaNho = new TextField();
    private final TextField txtGiaVua = new TextField();
    private final TextField txtGiaLon = new TextField();
    private final TextField txtMoTa = new TextField();

    private final Button btnThem = new Button("Thêm dịch vụ");
    private final Button btnLamMoi = new Button("Làm mới");

    private final TableView<DichVuChamSoc> tblDanhSach = new TableView<>();

    public ThemDichVuView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("DỊCH VỤ CHĂM SÓC - THÊM DỊCH VỤ MỚI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Nhập thông tin dịch vụ và đơn giá cho 3 bậc cân nặng (Nhỏ < 5kg, Vừa 5-10kg, Lớn > 10kg).");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlForm = taoKhungNhapLieu();
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeMan, pnlForm, pnlBang);

        xoaTrangForm();
        taiLaiBang();
    }

    private VBox taoKhungNhapLieu() {
        VBox pnlKhung = new VBox(12);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(14);
        pnlLuoi.setVgap(10);

        txtMaDichVu.setEditable(false);
        txtMaDichVu.setPromptText("Tự động sinh mã");

        txtTenDichVu.setPromptText("Nhập tên dịch vụ (VD: Tắm thảo dược)");
        txtThoiLuong.setPromptText("Số phút (> 0, VD: 45)");
        txtGiaNho.setPromptText("Giá bậc Nhỏ (< 5kg)");
        txtGiaVua.setPromptText("Giá bậc Vừa (5-10kg)");
        txtGiaLon.setPromptText("Giá bậc Lớn (> 10kg)");
        txtMoTa.setPromptText("Mô tả chi tiết dịch vụ");

        chanNhapSoNguyen(txtThoiLuong);
        chanNhapSoThuc(txtGiaNho);
        chanNhapSoThuc(txtGiaVua);
        chanNhapSoThuc(txtGiaLon);

        Label lblMa = new Label("Mã dịch vụ:");
        Label lblTen = new Label("Tên dịch vụ (*):");
        Label lblThoiLuong = new Label("Thời gian (phút) (*):");
        Label lblGiaNho = new Label("Giá bậc Nhỏ (*):");
        Label lblGiaVua = new Label("Giá bậc Vừa (*):");
        Label lblGiaLon = new Label("Giá bậc Lớn (*):");
        Label lblMoTa = new Label("Mô tả:");

        pnlLuoi.add(lblMa, 0, 0);
        pnlLuoi.add(txtMaDichVu, 1, 0);
        pnlLuoi.add(lblTen, 2, 0);
        pnlLuoi.add(txtTenDichVu, 3, 0);
        pnlLuoi.add(lblThoiLuong, 4, 0);
        pnlLuoi.add(txtThoiLuong, 5, 0);

        pnlLuoi.add(lblGiaNho, 0, 1);
        pnlLuoi.add(txtGiaNho, 1, 1);
        pnlLuoi.add(lblGiaVua, 2, 1);
        pnlLuoi.add(txtGiaVua, 3, 1);
        pnlLuoi.add(lblGiaLon, 4, 1);
        pnlLuoi.add(txtGiaLon, 5, 1);

        pnlLuoi.add(lblMoTa, 0, 2);
        pnlLuoi.add(txtMoTa, 1, 2, 5, 1);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(120);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap, colNhan, colNhap, colNhan, colNhap);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnThem.getStyleClass().add("nut-chinh");
        btnThem.setOnAction(e -> xuLyThem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            xoaTrangForm();
            taiLaiBang();
        });

        pnlNut.getChildren().addAll(btnLamMoi, btnThem);
        pnlKhung.getChildren().addAll(pnlLuoi, pnlNut);
        return pnlKhung;
    }

    private VBox taoKhungBangDanhSach() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblDanhSach = new Label("Danh sách dịch vụ hiện có");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có dịch vụ nào trong cơ sở dữ liệu"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<DichVuChamSoc, String> colMa = new TableColumn<>("Mã dịch vụ");
        colMa.setPrefWidth(150);
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaDichVu()));

        TableColumn<DichVuChamSoc, String> colTen = new TableColumn<>("Tên dịch vụ");
        colTen.setPrefWidth(190);
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenDichVu()));

        TableColumn<DichVuChamSoc, String> colThoiLuong = new TableColumn<>("Thời lượng");
        colThoiLuong.setPrefWidth(95);
        colThoiLuong.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getThoiLuong() + " phút"));

        TableColumn<DichVuChamSoc, String> colGiaNho = new TableColumn<>("Giá Nhỏ (<5kg)");
        colGiaNho.setPrefWidth(120);
        colGiaNho.setCellValueFactory(d -> new SimpleStringProperty(dinhDangTien(d.getValue().getGiaNho())));

        TableColumn<DichVuChamSoc, String> colGiaVua = new TableColumn<>("Giá Vừa (5-10kg)");
        colGiaVua.setPrefWidth(125);
        colGiaVua.setCellValueFactory(d -> new SimpleStringProperty(dinhDangTien(d.getValue().getGiaVua())));

        TableColumn<DichVuChamSoc, String> colGiaLon = new TableColumn<>("Giá Lớn (>10kg)");
        colGiaLon.setPrefWidth(120);
        colGiaLon.setCellValueFactory(d -> new SimpleStringProperty(dinhDangTien(d.getValue().getGiaLon())));

        TableColumn<DichVuChamSoc, String> colMoTa = new TableColumn<>("Mô tả");
        colMoTa.setPrefWidth(220);
        colMoTa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMoTa()));

        TableColumn<DichVuChamSoc, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh"
        ));

        tblDanhSach.getColumns().addAll(List.of(
                colMa, colTen, colThoiLuong, colGiaNho, colGiaVua, colGiaLon, colMoTa, colTrangThai
        ));

        pnlKhung.getChildren().addAll(lblDanhSach, tblDanhSach);
        return pnlKhung;
    }

    // Chặn người dùng gõ ký tự khác chữ số nguyên
    private void chanNhapSoNguyen(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                txt.setText(cu);
            }
        });
    }

    // Chặn người dùng gõ ký tự khác số thực dương
    private void chanNhapSoThuc(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d*)?")) {
                txt.setText(cu);
            }
        });
    }

    // Đọc dữ liệu từ các ô nhập trên form thành đối tượng DichVuChamSoc
    private DichVuChamSoc docFormDichVu() {
        String ten = txtTenDichVu.getText() == null ? "" : txtTenDichVu.getText().trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên dịch vụ!");
        }

        int thoiLuong = docSoNguyen(txtThoiLuong.getText(), "Thời gian thực hiện");
        double giaNho = docSoThuc(txtGiaNho.getText(), "Giá bậc Nhỏ");
        double giaVua = docSoThuc(txtGiaVua.getText(), "Giá bậc Vừa");
        double giaLon = docSoThuc(txtGiaLon.getText(), "Giá bậc Lớn");
        String moTa = txtMoTa.getText() == null ? "" : txtMoTa.getText().trim();

        return new DichVuChamSoc(
                txtMaDichVu.getText(),
                ten,
                moTa,
                thoiLuong,
                true,
                giaNho,
                giaVua,
                giaLon
        );
    }

    private int docSoNguyen(String chuoi, String tenTruong) {
        if (chuoi == null || chuoi.trim().isEmpty()) {
            throw new IllegalArgumentException(tenTruong + " không được để trống!");
        }
        try {
            int giaTri = Integer.parseInt(chuoi.trim());
            if (giaTri <= 0) {
                throw new IllegalArgumentException(tenTruong + " phải lớn hơn 0!");
            }
            return giaTri;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(tenTruong + " phải là số nguyên hợp lệ!");
        }
    }

    private double docSoThuc(String chuoi, String tenTruong) {
        if (chuoi == null || chuoi.trim().isEmpty()) {
            throw new IllegalArgumentException(tenTruong + " không được để trống!");
        }
        try {
            double giaTri = Double.parseDouble(chuoi.trim());
            if (giaTri <= 0) {
                throw new IllegalArgumentException(tenTruong + " phải lớn hơn 0!");
            }
            return giaTri;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(tenTruong + " phải là số hợp lệ!");
        }
    }

    private void xuLyThem() {
        try {
            DichVuChamSoc dichVu = docFormDichVu();
            boolean thanhCong = dichVuController.themDichVu(dichVu);
            if (thanhCong) {
                taiLaiBang();
                xoaTrangForm();
                hienThiThongBao("Thành công", "Đã thêm dịch vụ mới: " + dichVu.getTenDichVu());
            } else {
                hienThiLoi("Không thể thêm dịch vụ. Vui lòng kiểm tra lại!");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void xoaTrangForm() {
        txtTenDichVu.clear();
        txtThoiLuong.setText("30");
        txtGiaNho.clear();
        txtGiaVua.clear();
        txtGiaLon.clear();
        txtMoTa.clear();
        try {
            txtMaDichVu.setText(dichVuController.taoMaDichVu());
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            txtMaDichVu.setText("");
        }
    }

    private void taiLaiBang() {
        try {
            List<DichVuChamSoc> danhSach = dichVuController.layDanhSachDichVu();
            tblDanhSach.setItems(FXCollections.observableArrayList(danhSach));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private String dinhDangTien(double soTien) {
        return String.format("%,.0f đ", soTien);
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle(tieuDe);
        dlg.setHeaderText(null);
        dlg.setContentText(noiDung);
        dlg.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert dlg = new Alert(Alert.AlertType.ERROR);
        dlg.setTitle("Thông báo lỗi");
        dlg.setHeaderText(null);
        dlg.setContentText(noiDung);
        dlg.showAndWait();
    }
}

