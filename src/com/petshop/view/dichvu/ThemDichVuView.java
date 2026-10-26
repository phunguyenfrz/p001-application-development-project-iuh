package com.petshop.view.dichvu;

import com.petshop.controller.DichVuChamSocController;
import com.petshop.model.BangGiaDichVu;
import com.petshop.model.DichVuChamSoc;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Màn hình nhỏ (popup) dùng để thêm mới dịch vụ chăm sóc và đơn giá 3 bậc cân nặng.
 */
public class ThemDichVuView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");

    private final DichVuChamSocController dichVuController = new DichVuChamSocController();
    private final Runnable khiThemThanhCong;

    private final TextField txtTenDichVu = new TextField();
    private final TextField txtThoiLuong = new TextField();
    private final TextField txtGiaNho = new TextField();
    private final TextField txtGiaVua = new TextField();
    private final TextField txtGiaLon = new TextField();
    private final TextArea txtMoTa = new TextArea();

    private final Button btnThem = new Button("Lưu thêm mới");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

    public ThemDichVuView() {
        this(null);
    }

    public ThemDichVuView(Runnable khiThemThanhCong) {
        this.khiThemThanhCong = khiThemThanhCong;
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM DỊCH VỤ CHĂM SÓC MỚI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Nhập thông tin dịch vụ và đơn giá cho 3 bậc cân nặng.");
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

        txtTenDichVu.setPromptText("Nhập tên dịch vụ (VD: Tắm thảo dược)");
        txtThoiLuong.setPromptText("Nhập số phút (> 0)");
        txtGiaNho.setPromptText("Nhập đơn giá (VNĐ)");
        txtGiaVua.setPromptText("Nhập đơn giá (VNĐ)");
        txtGiaLon.setPromptText("Nhập đơn giá (VNĐ)");

        txtMoTa.setPromptText("Nhập mô tả chi tiết dịch vụ...");
        txtMoTa.setPrefRowCount(3);
        txtMoTa.setWrapText(true);

        chanNhapSoNguyen(txtThoiLuong);
        ganDinhDangTienChoO(txtGiaNho);
        ganDinhDangTienChoO(txtGiaVua);
        ganDinhDangTienChoO(txtGiaLon);

        pnlLuoi.add(new Label("Tên dịch vụ (*):"), 0, 0);
        pnlLuoi.add(txtTenDichVu, 1, 0);
        pnlLuoi.add(new Label("Thời gian (phút) (*):"), 0, 1);
        pnlLuoi.add(txtThoiLuong, 1, 1);
        pnlLuoi.add(new Label(BangGiaDichVu.NHAN_GIA_NHO + " (*):"), 0, 2);
        pnlLuoi.add(txtGiaNho, 1, 2);
        pnlLuoi.add(new Label(BangGiaDichVu.NHAN_GIA_VUA + " (*):"), 0, 3);
        pnlLuoi.add(txtGiaVua, 1, 3);
        pnlLuoi.add(new Label(BangGiaDichVu.NHAN_GIA_LON + " (*):"), 0, 4);
        pnlLuoi.add(txtGiaLon, 1, 4);
        pnlLuoi.add(new Label("Mô tả:"), 0, 5);
        pnlLuoi.add(txtMoTa, 1, 5);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(190);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnThem.getStyleClass().add("nut-chinh");
        btnThem.setOnAction(e -> xuLyThem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xoaTrangForm());

        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().addAll(btnLamMoi, btnDong, btnThem);
        pnlKhung.getChildren().addAll(pnlLuoi, pnlNut);
        return pnlKhung;
    }

    private void chanNhapSoNguyen(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                txt.setText(cu);
            }
        });
    }

    // Chặn nhập chữ và tự định dạng tiền vi-VN khi rời ô nhập giá
    private void ganDinhDangTienChoO(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("[\\d.\\sđ]*")) {
                txt.setText(cu);
            }
        });
        txt.focusedProperty().addListener((obs, cu, dangChon) -> {
            if (!dangChon) {
                dinhDangLaiO(txt);
            }
        });
    }

    private void dinhDangLaiO(TextField txt) {
        String chuoi = boKyTuDinhDangTien(txt.getText());
        if (chuoi.isEmpty()) {
            return;
        }
        try {
            double soTien = Double.parseDouble(chuoi);
            if (soTien > 0) {
                txt.setText(dinhDangTien(soTien));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private DichVuChamSoc docFormDichVu() {
        String ten = txtTenDichVu.getText() == null ? "" : txtTenDichVu.getText().trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên dịch vụ!");
        }

        int thoiLuong = docSoNguyen(txtThoiLuong.getText(), "Thời gian thực hiện");
        double giaNho = docSoThuc(txtGiaNho.getText(), BangGiaDichVu.NHAN_GIA_NHO);
        double giaVua = docSoThuc(txtGiaVua.getText(), BangGiaDichVu.NHAN_GIA_VUA);
        double giaLon = docSoThuc(txtGiaLon.getText(), BangGiaDichVu.NHAN_GIA_LON);
        String moTa = txtMoTa.getText() == null ? "" : txtMoTa.getText().trim();

        return new DichVuChamSoc(null, ten, moTa, thoiLuong, true, giaNho, giaVua, giaLon);
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
        String chuoiSo = boKyTuDinhDangTien(chuoi);
        if (chuoiSo.isEmpty()) {
            throw new IllegalArgumentException(tenTruong + " không được để trống!");
        }
        try {
            double giaTri = Double.parseDouble(chuoiSo);
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
                hienThiThongBao("Thành công", "Đã thêm dịch vụ mới: " + dichVu.getTenDichVu());
                if (khiThemThanhCong != null) {
                    khiThemThanhCong.run();
                }
                dongCuaSo();
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
    }

    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    // Dùng NumberFormat Locale vi-VN để hiển thị dạng "120.000 đ"
    public static String dinhDangTien(double soTien) {
        NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_VI_VN);
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(0);
        return nf.format(Math.round(soTien)) + " đ";
    }

    public static String boKyTuDinhDangTien(String chuoi) {
        if (chuoi == null) {
            return "";
        }
        return chuoi.replace(".", "").replace(",", "").replace("đ", "").replace(" ", "").trim();
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
