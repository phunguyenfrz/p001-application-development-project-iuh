package com.petshop.view.dichvu;

import com.petshop.controller.DichVuChamSocController;
import com.petshop.model.BangGiaDichVu;
import com.petshop.model.DichVuChamSoc;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Màn hình danh sách dịch vụ chăm sóc tích hợp thanh tác vụ Thêm mới (mở màn nhỏ), Cập nhật (mở màn nhỏ), Xóa mềm và Làm mới.
 */
public class CapNhatDichVuView extends VBox {

    private final DichVuChamSocController dichVuController = new DichVuChamSocController();

    private final Button btnThemMoi = new Button("Thêm mới");
    private final Button btnCapNhat = new Button("Cập nhật");
    private final Button btnXoa = new Button("Xóa");
    private final Button btnLamMoi = new Button("Làm mới");

    private final TableView<DichVuChamSoc> tblDanhSach = new TableView<>();

    public CapNhatDichVuView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("DỊCH VỤ CHĂM SÓC - CẬP NHẬT DỊCH VỤ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Bấm Thêm mới để mở cửa sổ thêm dịch vụ; nhấp chọn một dòng trong bảng để Cập nhật hoặc Xóa.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeMan, pnlBang);

        capNhatTrangThaiNut(null);
        taiLaiBang();
    }

    // Tạo thanh nút phía trên bảng: Thêm mới (xanh), Cập nhật, Xóa (đỏ), Làm mới
    private HBox taoThanhTacVu() {
        HBox pnlThanhNut = new HBox(10);
        pnlThanhNut.getStyleClass().add("thanh-tac-vu");
        pnlThanhNut.setAlignment(Pos.CENTER_LEFT);

        Label lblDanhSach = new Label("Danh sách dịch vụ chăm sóc");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        btnThemMoi.getStyleClass().add("nut-chinh");
        btnThemMoi.setOnAction(e -> moCuaSoThemMoi());

        btnCapNhat.getStyleClass().add("nut-phu");
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());

        btnXoa.getStyleClass().add("nut-xoa");
        btnXoa.setOnAction(e -> xuLyXoaMem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xuLyLamMoi());

        pnlThanhNut.getChildren().addAll(lblDanhSach, pnlKhoangTrong, btnThemMoi, btnCapNhat, btnXoa, btnLamMoi);
        return pnlThanhNut;
    }

    // Tạo khung bảng danh sách dịch vụ có cột Mô tả giãn hết phần còn lại và làm mờ dòng Ngừng kinh doanh
    private VBox taoKhungBangDanhSach() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        HBox pnlThanhNut = taoThanhTacVu();

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
        colThoiLuong.setPrefWidth(100);
        colThoiLuong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colThoiLuong.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getThoiLuong() + " phút"));

        TableColumn<DichVuChamSoc, String> colGiaNho = new TableColumn<>("Giá Nhỏ (" + BangGiaDichVu.KHOANG_CAN_NANG_NHO + ")");
        colGiaNho.setPrefWidth(135);
        colGiaNho.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaNho.setCellValueFactory(d -> new SimpleStringProperty(dinhDangTien(d.getValue().getGiaNho())));

        TableColumn<DichVuChamSoc, String> colGiaVua = new TableColumn<>("Giá Vừa (" + BangGiaDichVu.KHOANG_CAN_NANG_VUA + ")");
        colGiaVua.setPrefWidth(135);
        colGiaVua.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaVua.setCellValueFactory(d -> new SimpleStringProperty(dinhDangTien(d.getValue().getGiaVua())));

        TableColumn<DichVuChamSoc, String> colGiaLon = new TableColumn<>("Giá Lớn (" + BangGiaDichVu.KHOANG_CAN_NANG_LON + ")");
        colGiaLon.setPrefWidth(135);
        colGiaLon.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaLon.setCellValueFactory(d -> new SimpleStringProperty(dinhDangTien(d.getValue().getGiaLon())));

        TableColumn<DichVuChamSoc, String> colMoTa = new TableColumn<>("Mô tả");
        colMoTa.setMinWidth(180);
        colMoTa.prefWidthProperty().bind(
                tblDanhSach.widthProperty().subtract(150 + 190 + 100 + 135 + 135 + 135 + 130 + 22)
        );
        colMoTa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMoTa()));

        TableColumn<DichVuChamSoc, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh"
        ));

        tblDanhSach.getColumns().addAll(List.of(
                colMa, colTen, colThoiLuong, colGiaNho, colGiaVua, colGiaLon, colMoTa, colTrangThai
        ));

        // Làm mờ các dòng đã Ngừng kinh doanh và cho phép nhấp đúp để mở màn nhỏ Cập nhật
        tblDanhSach.setRowFactory(tv -> {
            TableRow<DichVuChamSoc> row = new TableRow<>() {
                @Override
                protected void updateItem(DichVuChamSoc item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setOpacity(1.0);
                    } else {
                        setOpacity(item.isTrangThai() ? 1.0 : 0.5);
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moCuaSoCapNhat();
                }
            });
            return row;
        });

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener(
                (obs, cu, moi) -> capNhatTrangThaiNut(moi)
        );

        pnlKhung.getChildren().addAll(pnlThanhNut, tblDanhSach);
        return pnlKhung;
    }

    // Bật/tắt nút Cập nhật và Xóa tùy theo dòng đang chọn trong bảng
    private void capNhatTrangThaiNut(DichVuChamSoc dichVuChon) {
        if (dichVuChon == null) {
            btnCapNhat.setDisable(true);
            btnXoa.setDisable(true);
            return;
        }
        btnCapNhat.setDisable(false);
        btnXoa.setDisable(!dichVuChon.isTrangThai());
    }

    // Mở cửa sổ nhỏ ThemDichVuView riêng biệt, đóng lại thì tải lại bảng
    private void moCuaSoThemMoi() {
        Stage dlgThem = new Stage();
        dlgThem.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlgThem.initOwner(getScene().getWindow());
        }
        dlgThem.setTitle("Thêm dịch vụ chăm sóc mới");

        ThemDichVuView manThem = new ThemDichVuView(this::taiLaiBang);
        Scene scene = new Scene(manThem, 560, 470);
        ganCssChung(scene);

        dlgThem.setScene(scene);
        dlgThem.setResizable(false);
        dlgThem.showAndWait();
    }

    // Mở cửa sổ nhỏ cập nhật cho dòng dịch vụ đang chọn trong bảng
    private void moCuaSoCapNhat() {
        DichVuChamSoc dichVuChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (dichVuChon == null) {
            hienThiLoi("Vui lòng chọn một dịch vụ trong bảng để cập nhật!");
            return;
        }

        Stage dlgCapNhat = new Stage();
        dlgCapNhat.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlgCapNhat.initOwner(getScene().getWindow());
        }
        dlgCapNhat.setTitle("Cập nhật dịch vụ chăm sóc");

        VBox pnlGoc = taoNoiDungCuaSoCapNhat(dichVuChon, dlgCapNhat);
        Scene scene = new Scene(pnlGoc, 560, 510);
        ganCssChung(scene);

        dlgCapNhat.setScene(scene);
        dlgCapNhat.setResizable(false);
        dlgCapNhat.showAndWait();
    }

    // Dựng nội dung form trong cửa sổ nhỏ Cập nhật dịch vụ
    private VBox taoNoiDungCuaSoCapNhat(DichVuChamSoc dichVuChon, Stage dlgCapNhat) {
        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("CẬP NHẬT DỊCH VỤ CHĂM SÓC");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblDangSua = new Label("Đang sửa: " + dichVuChon.getMaDichVu() + " " + dichVuChon.getTenDichVu());
        lblDangSua.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #0369a1;");

        VBox pnlForm = new VBox(14);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        TextField txtTenDichVu = new TextField(dichVuChon.getTenDichVu());
        TextField txtThoiLuong = new TextField(String.valueOf(dichVuChon.getThoiLuong()));
        TextField txtGiaNho = new TextField(dinhDangTien(dichVuChon.getGiaNho()));
        TextField txtGiaVua = new TextField(dinhDangTien(dichVuChon.getGiaVua()));
        TextField txtGiaLon = new TextField(dinhDangTien(dichVuChon.getGiaLon()));

        ComboBox<String> cboTrangThai = new ComboBox<>(
                FXCollections.observableArrayList("Đang kinh doanh", "Ngừng kinh doanh")
        );
        cboTrangThai.setValue(dichVuChon.isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh");
        cboTrangThai.setMaxWidth(Double.MAX_VALUE);

        TextArea txtMoTa = new TextArea(dichVuChon.getMoTa() == null ? "" : dichVuChon.getMoTa());
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
        pnlLuoi.add(new Label("Trạng thái:"), 0, 5);
        pnlLuoi.add(cboTrangThai, 1, 5);
        pnlLuoi.add(new Label("Mô tả:"), 0, 6);
        pnlLuoi.add(txtMoTa, 1, 6);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(190);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> dlgCapNhat.close());

        Button btnLuu = new Button("Lưu cập nhật");
        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> {
            try {
                DichVuChamSoc dichVuMoi = docFormCapNhat(
                        dichVuChon.getMaDichVu(),
                        txtTenDichVu.getText(),
                        txtThoiLuong.getText(),
                        txtGiaNho.getText(),
                        txtGiaVua.getText(),
                        txtGiaLon.getText(),
                        cboTrangThai.getValue(),
                        txtMoTa.getText()
                );
                boolean thanhCong = dichVuController.capNhatDichVu(dichVuMoi);
                if (thanhCong) {
                    taiLaiBang();
                    hienThiThongBao("Thành công", "Đã cập nhật dịch vụ: " + dichVuMoi.getTenDichVu());
                    dlgCapNhat.close();
                } else {
                    hienThiLoi("Không tìm thấy dịch vụ cần cập nhật!");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                hienThiLoi(ex.getMessage());
            }
        });

        pnlNut.getChildren().addAll(btnHuy, btnLuu);
        pnlForm.getChildren().addAll(lblDangSua, pnlLuoi, pnlNut);
        pnlGoc.getChildren().addAll(lblTieuDe, pnlForm);
        return pnlGoc;
    }

    private DichVuChamSoc docFormCapNhat(String maDichVu, String tenNhap, String thoiLuongNhap,
                                         String giaNhoNhap, String giaVuaNhap, String giaLonNhap,
                                         String trangThaiChon, String moTaNhap) {
        String ten = tenNhap == null ? "" : tenNhap.trim();
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống!");
        }

        int thoiLuong = docSoNguyen(thoiLuongNhap, "Thời gian thực hiện");
        double giaNho = docSoThuc(giaNhoNhap, BangGiaDichVu.NHAN_GIA_NHO);
        double giaVua = docSoThuc(giaVuaNhap, BangGiaDichVu.NHAN_GIA_VUA);
        double giaLon = docSoThuc(giaLonNhap, BangGiaDichVu.NHAN_GIA_LON);
        boolean trangThai = "Đang kinh doanh".equals(trangThaiChon);
        String moTa = moTaNhap == null ? "" : moTaNhap.trim();

        return new DichVuChamSoc(maDichVu, ten, moTa, thoiLuong, trangThai, giaNho, giaVua, giaLon);
    }

    // Xác nhận và xóa mềm dịch vụ đang chọn trong bảng
    private void xuLyXoaMem() {
        DichVuChamSoc dichVuChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (dichVuChon == null) {
            hienThiLoi("Vui lòng chọn một dịch vụ trong bảng để xóa!");
            return;
        }

        Alert dlgXacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        dlgXacNhan.setTitle("Xác nhận ngừng kinh doanh");
        dlgXacNhan.setHeaderText(null);
        dlgXacNhan.setContentText("Bạn có chắc chắn muốn ngừng kinh doanh dịch vụ \""
                + dichVuChon.getTenDichVu() + "\" (" + dichVuChon.getMaDichVu() + ") không?\n\n"
                + "Lưu ý: Dịch vụ sẽ chuyển sang trạng thái \"Ngừng kinh doanh\", "
                + "các lịch hẹn và hóa đơn cũ liên quan vẫn được giữ nguyên.");

        Optional<ButtonType> ketQua = dlgXacNhan.showAndWait();
        if (ketQua.isEmpty() || ketQua.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean thanhCong = dichVuController.xoaDichVu(dichVuChon.getMaDichVu());
            if (thanhCong) {
                taiLaiBang();
                hienThiThongBao("Thành công", "Đã chuyển dịch vụ \"" + dichVuChon.getTenDichVu() + "\" sang trạng thái Ngừng kinh doanh.");
            } else {
                hienThiLoi("Không thể ngừng kinh doanh dịch vụ này!");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    // Bỏ chọn dòng và tải lại bảng
    private void xuLyLamMoi() {
        tblDanhSach.getSelectionModel().clearSelection();
        capNhatTrangThaiNut(null);
        taiLaiBang();
    }

    public void taiLaiBang() {
        try {
            List<DichVuChamSoc> danhSach = dichVuController.layDanhSachDichVu();
            tblDanhSach.setItems(FXCollections.observableArrayList(danhSach));
            tblDanhSach.getSelectionModel().clearSelection();
            capNhatTrangThaiNut(null);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void chanNhapSoNguyen(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                txt.setText(cu);
            }
        });
    }

    // Dùng cùng hàm định dạng tiền Locale vi-VN cho ô nhập giá
    private void ganDinhDangTienChoO(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("[\\d.\\sđ]*")) {
                txt.setText(cu);
            }
        });
        txt.focusedProperty().addListener((obs, cu, dangChon) -> {
            if (!dangChon) {
                String chuoi = boKyTuDinhDangTien(txt.getText());
                if (!chuoi.isEmpty()) {
                    try {
                        double soTien = Double.parseDouble(chuoi);
                        if (soTien > 0) {
                            txt.setText(dinhDangTien(soTien));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        });
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

    // Định dạng tiền tệ "120.000 đ" bằng NumberFormat Locale vi-VN (dùng chung cho cả bảng và ô nhập)
    private String dinhDangTien(double soTien) {
        return ThemDichVuView.dinhDangTien(soTien);
    }

    private String boKyTuDinhDangTien(String chuoi) {
        return ThemDichVuView.boKyTuDinhDangTien(chuoi);
    }

    private void ganCssChung(Scene scene) {
        var cssUrl = getClass().getResource("/com/petshop/view/css/chung.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
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
