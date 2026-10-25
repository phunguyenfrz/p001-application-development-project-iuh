package com.petshop.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Màn hình danh sách chuẩn cho các phân hệ nghiệp vụ (DanhSachChucNangView).
 * Bố cục chuẩn ứng dụng Desktop: Tiêu đề, Thanh công cụ (Thêm mới, Cập nhật, Xóa, Làm mới) và Bảng dữ liệu.
 */
public class DanhSachChucNangView extends VBox {

    public DanhSachChucNangView(String tieuDeDoiTuong, String tenChucNang, String phimTat) {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình
        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label(tieuDeDoiTuong.toUpperCase() + " - " + tenChucNang.toUpperCase());
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Phân hệ quản lý " + tieuDeDoiTuong.toLowerCase() + ".");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung chức năng & Bảng dữ liệu
        VBox pnlKhung = new VBox(12);
        pnlKhung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        HBox pnlCongCu = new HBox(10);
        pnlCongCu.getStyleClass().add("thanh-tac-vu");

        TextField txtTimKiem = new TextField();
        txtTimKiem.setPromptText("Tìm kiếm trong danh sách " + tieuDeDoiTuong + "...");
        txtTimKiem.setPrefWidth(260);

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        Button btnThem = new Button("Thêm mới");
        btnThem.getStyleClass().add("nut-chinh");
        btnThem.setOnAction(e -> hienThiHopThoai("Thêm mới " + tieuDeDoiTuong));

        Button btnCapNhat = new Button("Cập nhật");
        btnCapNhat.getStyleClass().add("nut-phu");
        btnCapNhat.setOnAction(e -> hienThiHopThoai("Cập nhật " + tieuDeDoiTuong));

        Button btnXoa = new Button("Xóa");
        btnXoa.getStyleClass().add("nut-xoa");
        btnXoa.setOnAction(e -> hienThiHopThoai("Xóa " + tieuDeDoiTuong));

        Button btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");

        pnlCongCu.getChildren().addAll(txtTimKiem, pnlKhoangTrong, btnThem, btnCapNhat, btnXoa, btnLamMoi);

        // Bảng dữ liệu (TableView)
        TableView<String> tblDanhSach = new TableView<>();
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        Label lblKhongCoDuLieu = new Label("Không có dữ liệu");
        lblKhongCoDuLieu.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        tblDanhSach.setPlaceholder(lblKhongCoDuLieu);
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<String, String> colMaDinhDanh = new TableColumn<>("Mã định danh");
        colMaDinhDanh.setPrefWidth(140);
        TableColumn<String, String> colThongTinChinh = new TableColumn<>("Thông tin chính");
        colThongTinChinh.setPrefWidth(300);
        TableColumn<String, String> colNgayCapNhat = new TableColumn<>("Ngày cập nhật");
        colNgayCapNhat.setPrefWidth(160);
        TableColumn<String, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(140);

        tblDanhSach.getColumns().addAll(java.util.List.of(colMaDinhDanh, colThongTinChinh, colNgayCapNhat, colTrangThai));

        pnlKhung.getChildren().addAll(pnlCongCu, tblDanhSach);
        getChildren().addAll(pnlTieuDeMan, pnlKhung);
    }

    private void hienThiHopThoai(String tieuDe) {
        Alert dlgThongBao = new Alert(Alert.AlertType.INFORMATION);
        dlgThongBao.setTitle(tieuDe);
        dlgThongBao.setHeaderText(null);
        dlgThongBao.setContentText("Chức năng đang được cập nhật.");
        dlgThongBao.showAndWait();
    }
}

