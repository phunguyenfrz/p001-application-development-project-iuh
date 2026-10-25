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
 * Màn hình cập nhật thông tin dịch vụ chăm sóc và bảng giá 3 bậc cân nặng (Nhỏ, Vừa, Lớn).
 */
public class CapNhatDichVuView extends VBox {

    private final DichVuChamSocController dichVuController = new DichVuChamSocController();

    private final TextField txtMaDichVu = new TextField();
    private final TextField txtTenDichVu = new TextField();
    private final TextField txtThoiLuong = new TextField();
    private final TextField txtGiaNho = new TextField();
    private final TextField txtGiaVua = new TextField();
    private final TextField txtGiaLon = new TextField();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();
    private final TextField txtMoTa = new TextField();

    private final Button btnCapNhat = new Button("Lưu cập nhật");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnMoManThem = new Button("Mở màn Thêm mới");

    private final TableView<DichVuChamSoc> tblDanhSach = new TableView<>();

    public CapNhatDichVuView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        HBox pnlDauTrang = new HBox(12);
        pnlDauTrang.setAlignment(Pos.CENTER_LEFT);

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("DỊCH VỤ CHĂM SÓC - CẬP NHẬT DỊCH VỤ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Chọn một dịch vụ trong bảng bên dưới để chỉnh sửa thông tin và đơn giá 3 bậc cân nặng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        btnMoManThem.getStyleClass().add("nut-phu");
        btnMoManThem.setOnAction(e -> chuyenSangManThem());

        pnlDauTrang.getChildren().addAll(pnlTieuDeMan, pnlKhoangTrong, btnMoManThem);

        VBox pnlForm = taoKhungNhapLieu();
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlDauTrang, pnlForm, pnlBang);

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
        txtMaDichVu.setPromptText("Chọn dịch vụ từ bảng bên dưới");

        txtTenDichVu.setPromptText("Tên dịch vụ");
        txtThoiLuong.setPromptText("Số phút (> 0)");
        txtGiaNho.setPromptText("Giá bậc Nhỏ (< 5kg)");
        txtGiaVua.setPromptText("Giá bậc Vừa (5-10kg)");
        txtGiaLon.setPromptText("Giá bậc Lớn (> 10kg)");
        txtMoTa.setPromptText("Mô tả dịch vụ");

        cboTrangThai.setItems(FXCollections.observableArrayList("Đang kinh doanh", "Ngừng kinh doanh"));
        cboTrangThai.setMaxWidth(Double.MAX_VALUE);

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
        Label lblTrangThai = new Label("Trạng thái:");
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

        pnlLuoi.add(lblTrangThai, 0, 2);
        pnlLuoi.add(cboTrangThai, 1, 2);
        pnlLuoi.add(lblMoTa, 2, 2);
        pnlLuoi.add(txtMoTa, 3, 2, 3, 1);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(120);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap, colNhan, colNhap, colNhan, colNhap);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnCapNhat.getStyleClass().add("nut-chinh");
        btnCapNhat.setOnAction(e -> xuLyCapNhat());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            xoaTrangForm();
            taiLaiBang();
        });

        pnlNut.getChildren().addAll(btnLamMoi, btnCapNhat);
        pnlKhung.getChildren().addAll(pnlLuoi, pnlNut);
        return pnlKhung;
    }

    private VBox taoKhungBangDanhSach() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblDanhSach = new Label("Danh sách dịch vụ (nhấp chọn một dòng để nạp lên form cập nhật)");
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

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> {
            if (moi != null) {
                datFormDichVu(moi);
            }
        });

        pnlKhung.getChildren().addAll(lblDanhSach, tblDanhSach);
        return pnlKhung;
    }

    private void chanNhapSoNguyen(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                txt.setText(cu);
            }
        });
    }

    private void chanNhapSoThuc(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d*)?")) {
                txt.setText(cu);
            }
        });
    }

    // Đổ thông tin dịch vụ được chọn trên bảng lên các ô nhập liệu
    private void datFormDichVu(DichVuChamSoc dichVu) {
        txtMaDichVu.setText(dichVu.getMaDichVu());
        txtTenDichVu.setText(dichVu.getTenDichVu());
        txtThoiLuong.setText(String.valueOf(dichVu.getThoiLuong()));
        txtGiaNho.setText(String.format("%.0f", dichVu.getGiaNho()));
        txtGiaVua.setText(String.format("%.0f", dichVu.getGiaVua()));
        txtGiaLon.setText(String.format("%.0f", dichVu.getGiaLon()));
        cboTrangThai.setValue(dichVu.isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh");
        txtMoTa.setText(dichVu.getMoTa() == null ? "" : dichVu.getMoTa());
    }

    // Đọc dữ liệu từ các ô nhập liệu thành đối tượng DichVuChamSoc để cập nhật
    private DichVuChamSoc docFormDichVu() {
        String ma = txtMaDichVu.getText() == null ? "" : txtMaDichVu.getText().trim();
        if (ma.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn một dịch vụ trong bảng để cập nhật!");
        }

        String ten = txtTenDichVu.getText() == null ? "" : txtTenDichVu.getText().trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống!");
        }

        int thoiLuong = docSoNguyen(txtThoiLuong.getText(), "Thời gian thực hiện");
        double giaNho = docSoThuc(txtGiaNho.getText(), "Giá bậc Nhỏ");
        double giaVua = docSoThuc(txtGiaVua.getText(), "Giá bậc Vừa");
        double giaLon = docSoThuc(txtGiaLon.getText(), "Giá bậc Lớn");
        boolean trangThai = "Đang kinh doanh".equals(cboTrangThai.getValue());
        String moTa = txtMoTa.getText() == null ? "" : txtMoTa.getText().trim();

        return new DichVuChamSoc(ma, ten, moTa, thoiLuong, trangThai, giaNho, giaVua, giaLon);
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

    private void xuLyCapNhat() {
        try {
            DichVuChamSoc dichVu = docFormDichVu();
            boolean thanhCong = dichVuController.capNhatDichVu(dichVu);
            if (thanhCong) {
                taiLaiBang();
                hienThiThongBao("Thành công", "Đã cập nhật dịch vụ: " + dichVu.getTenDichVu());
            } else {
                hienThiLoi("Không tìm thấy dịch vụ cần cập nhật!");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void xoaTrangForm() {
        txtMaDichVu.clear();
        txtTenDichVu.clear();
        txtThoiLuong.clear();
        txtGiaNho.clear();
        txtGiaVua.clear();
        txtGiaLon.clear();
        cboTrangThai.setValue("Đang kinh doanh");
        txtMoTa.clear();
        tblDanhSach.getSelectionModel().clearSelection();
    }

    private void taiLaiBang() {
        try {
            List<DichVuChamSoc> danhSach = dichVuController.layDanhSachDichVu();
            tblDanhSach.setItems(FXCollections.observableArrayList(danhSach));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void chuyenSangManThem() {
        if (getParent() instanceof Pane pnlCha) {
            pnlCha.getChildren().setAll(new ThemDichVuView());
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

