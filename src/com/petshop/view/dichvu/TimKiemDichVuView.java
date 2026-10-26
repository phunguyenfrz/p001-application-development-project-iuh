package com.petshop.view.dichvu;

import com.petshop.controller.DichVuChamSocController;
import com.petshop.model.BangGiaDichVu;
import com.petshop.model.DichVuChamSoc;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình tra cứu, tìm kiếm dịch vụ chăm sóc theo mã/tên và lọc theo trạng thái kinh doanh (SRV_SEARCH).
 */
public class TimKiemDichVuView extends VBox {

    private final DichVuChamSocController dichVuController = new DichVuChamSocController();

    private final TextField txtTuKhoa = new TextField();
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Label lblKetQua = new Label("Tổng số: 0 dịch vụ");

    private final TableView<DichVuChamSoc> tblDanhSach = new TableView<>();

    public TimKiemDichVuView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("DỊCH VỤ CHĂM SÓC - TÌM KIẾM DỊCH VỤ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu danh mục dịch vụ theo mã hoặc tên dịch vụ và lọc theo trạng thái kinh doanh.");
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
        txtTuKhoa.setPrefWidth(260);
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

        pnlCongCu.getChildren().addAll(
                lblTim, txtTuKhoa,
                lblTrangThai, cboLocTrangThai,
                btnTimKiem, btnLamMoi
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
        colThoiLuong.setPrefWidth(100);
        colThoiLuong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colThoiLuong.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getThoiLuong() + " phút"));

        TableColumn<DichVuChamSoc, String> colGiaNho = new TableColumn<>("Giá Nhỏ (" + BangGiaDichVu.KHOANG_CAN_NANG_NHO + ")");
        colGiaNho.setPrefWidth(135);
        colGiaNho.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaNho.setCellValueFactory(d -> new SimpleStringProperty(ThemDichVuView.dinhDangTien(d.getValue().getGiaNho())));

        TableColumn<DichVuChamSoc, String> colGiaVua = new TableColumn<>("Giá Vừa (" + BangGiaDichVu.KHOANG_CAN_NANG_VUA + ")");
        colGiaVua.setPrefWidth(135);
        colGiaVua.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaVua.setCellValueFactory(d -> new SimpleStringProperty(ThemDichVuView.dinhDangTien(d.getValue().getGiaVua())));

        TableColumn<DichVuChamSoc, String> colGiaLon = new TableColumn<>("Giá Lớn (" + BangGiaDichVu.KHOANG_CAN_NANG_LON + ")");
        colGiaLon.setPrefWidth(135);
        colGiaLon.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaLon.setCellValueFactory(d -> new SimpleStringProperty(ThemDichVuView.dinhDangTien(d.getValue().getGiaLon())));

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

        tblDanhSach.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(DichVuChamSoc item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setOpacity(1.0);
                } else {
                    setOpacity(item.isTrangThai() ? 1.0 : 0.5);
                }
            }
        });

        lblKetQua.getStyleClass().add("mo-ta-phan-he");
        pnlBang.getChildren().addAll(tblDanhSach, lblKetQua);
        return pnlBang;
    }

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

    private void hienThiLoi(String noiDung) {
        Alert dlg = new Alert(Alert.AlertType.ERROR);
        dlg.setTitle("Thông báo lỗi");
        dlg.setHeaderText(null);
        dlg.setContentText(noiDung);
        dlg.showAndWait();
    }
}
