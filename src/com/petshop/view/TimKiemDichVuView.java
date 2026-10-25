package com.petshop.view;

import com.petshop.controller.DichVuChamSocController;
import com.petshop.model.DichVuChamSoc;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình tìm kiếm dịch vụ chăm sóc theo mã/tên và thực hiện xóa mềm (ngừng kinh doanh).
 */
public class TimKiemDichVuView extends VBox {

    private final DichVuChamSocController dichVuController = new DichVuChamSocController();

    private final TextField txtTuKhoa = new TextField();
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnXoa = new Button("Ngừng kinh doanh (Xóa)");
    private final Label lblKetQua = new Label("Tổng số: 0 dịch vụ");

    private final TableView<DichVuChamSoc> tblDanhSach = new TableView<>();

    public TimKiemDichVuView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("DỊCH VỤ CHĂM SÓC - TÌM KIẾM & NGỪNG KINH DOANH");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu dịch vụ theo mã hoặc tên dịch vụ; chọn dịch vụ và bấm Ngừng kinh doanh để xóa mềm.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlKhungChinh = new VBox(12);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlThanhCongCu = taoThanhTimKiem();
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlThanhCongCu, pnlBang);
        getChildren().addAll(pnlTieuDeMan, pnlKhungChinh);

        taiLaiBang();
    }

    private HBox taoThanhTimKiem() {
        HBox pnlCongCu = new HBox(10);
        pnlCongCu.getStyleClass().add("thanh-tac-vu");
        pnlCongCu.setAlignment(Pos.CENTER_LEFT);

        Label lblTim = new Label("Từ khóa:");
        txtTuKhoa.setPromptText("Nhập mã hoặc tên dịch vụ...");
        txtTuKhoa.setPrefWidth(250);
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());

        Label lblTrangThai = new Label("Trạng thái:");
        cboLocTrangThai.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang kinh doanh", "Ngừng kinh doanh"
        ));
        cboLocTrangThai.setValue("Tất cả");
        cboLocTrangThai.setOnAction(e -> xuLyTimKiem());

        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> xuLyTimKiem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiLaiBang());

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        btnXoa.getStyleClass().add("nut-xoa");
        btnXoa.setOnAction(e -> xuLyXoaMem());

        pnlCongCu.getChildren().addAll(
                lblTim, txtTuKhoa,
                lblTrangThai, cboLocTrangThai,
                btnTimKiem, btnLamMoi,
                pnlKhoangTrong, btnXoa
        );
        return pnlCongCu;
    }

    private VBox taoKhungBangDanhSach() {
        VBox pnlBang = new VBox(8);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy dịch vụ phù hợp"));
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

        lblKetQua.getStyleClass().add("mo-ta-phan-he");
        pnlBang.getChildren().addAll(tblDanhSach, lblKetQua);
        return pnlBang;
    }

    // Tìm kiếm dịch vụ theo từ khóa và lọc theo trạng thái kinh doanh
    private void xuLyTimKiem() {
        try {
            String tuKhoa = txtTuKhoa.getText() == null ? "" : txtTuKhoa.getText().trim();
            List<DichVuChamSoc> ketQuaGoc = dichVuController.timKiemDichVu(tuKhoa);
            List<DichVuChamSoc> ketQuaLoc = locTheoTrangThai(ketQuaGoc);

            tblDanhSach.setItems(FXCollections.observableArrayList(ketQuaLoc));
            lblKetQua.setText("Tổng số: " + ketQuaLoc.size() + " dịch vụ");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private List<DichVuChamSoc> locTheoTrangThai(List<DichVuChamSoc> danhSach) {
        String luaChon = cboLocTrangThai.getValue();
        if (luaChon == null || "Tất cả".equals(luaChon)) {
            return danhSach;
        }
        boolean dangKinhDoanh = "Đang kinh doanh".equals(luaChon);
        List<DichVuChamSoc> ketQua = new ArrayList<>();
        for (DichVuChamSoc dv : danhSach) {
            if (dv.isTrangThai() == dangKinhDoanh) {
                ketQua.add(dv);
            }
        }
        return ketQua;
    }

    // Xóa mềm dịch vụ đang chọn sau khi người dùng xác nhận trên hộp thoại
    private void xuLyXoaMem() {
        DichVuChamSoc dichVuChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (dichVuChon == null) {
            hienThiLoi("Vui lòng chọn một dịch vụ trong bảng để ngừng kinh doanh!");
            return;
        }

        if (!dichVuChon.isTrangThai()) {
            hienThiThongBao("Thông báo", "Dịch vụ [" + dichVuChon.getTenDichVu() + "] đã ở trạng thái ngừng kinh doanh.");
            return;
        }

        Alert dlgXacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        dlgXacNhan.setTitle("Xác nhận ngừng kinh doanh");
        dlgXacNhan.setHeaderText(null);
        dlgXacNhan.setContentText("Bạn có chắc chắn muốn ngừng kinh doanh dịch vụ \""
                + dichVuChon.getTenDichVu() + "\" (" + dichVuChon.getMaDichVu() + ") không?");

        Optional<ButtonType> ketQuaChon = dlgXacNhan.showAndWait();
        if (ketQuaChon.isEmpty() || ketQuaChon.get() != ButtonType.OK) {
            return;
        }

        try {
            boolean thanhCong = dichVuController.xoaDichVu(dichVuChon.getMaDichVu());
            if (thanhCong) {
                xuLyTimKiem();
                hienThiThongBao("Thành công", "Đã chuyển dịch vụ \"" + dichVuChon.getTenDichVu() + "\" sang trạng thái Ngừng kinh doanh.");
            } else {
                hienThiLoi("Không thể cập nhật trạng thái dịch vụ!");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void taiLaiBang() {
        txtTuKhoa.clear();
        cboLocTrangThai.setValue("Tất cả");
        try {
            List<DichVuChamSoc> danhSach = dichVuController.layDanhSachDichVu();
            tblDanhSach.setItems(FXCollections.observableArrayList(danhSach));
            lblKetQua.setText("Tổng số: " + danhSach.size() + " dịch vụ");
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

