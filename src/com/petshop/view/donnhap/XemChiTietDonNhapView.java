package com.petshop.view.donnhap;

import com.petshop.model.DonNhap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class XemChiTietDonNhapView extends Dialog<Void> {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public XemChiTietDonNhapView(DonNhap donNhap, List<DonNhap.ChiTiet> chiTiet) {
        setTitle("Chi tiết đơn nhập " + donNhap.maDonHang());
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().setContent(taoNoiDung(donNhap, chiTiet));
    }

    private VBox taoNoiDung(DonNhap donNhap, List<DonNhap.ChiTiet> chiTiet) {
        GridPane thongTin = new GridPane();
        thongTin.setHgap(10);
        thongTin.setVgap(8);
        thongTin.setPadding(new Insets(4));
        thongTin.add(new Label("Mã đơn nhập:"), 0, 0);
        thongTin.add(new Label(donNhap.maDonHang()), 1, 0);
        thongTin.add(new Label("Nhà cung cấp:"), 0, 1);
        thongTin.add(new Label(donNhap.tenNhaCungCap()), 1, 1);
        thongTin.add(new Label("Ngày lập:"), 0, 2);
        thongTin.add(new Label(donNhap.ngayLap().format(DINH_DANG_NGAY)), 1, 2);

        TableView<DonNhap.ChiTiet> tblChiTiet = new TableView<>();
        tblChiTiet.getStyleClass().add("bang-du-lieu");
        tblChiTiet.setPlaceholder(new Label("Đơn nhập chưa có sản phẩm"));
        tblChiTiet.setItems(FXCollections.observableArrayList(chiTiet));
        tblChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<DonNhap.ChiTiet, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().maSanPham()));
        TableColumn<DonNhap.ChiTiet, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().tenSanPham()));
        TableColumn<DonNhap.ChiTiet, String> colSoLuong = new TableColumn<>("Số lượng");
        colSoLuong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colSoLuong.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().soLuong())));
        TableColumn<DonNhap.ChiTiet, String> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDonGia.setCellValueFactory(cell -> new SimpleStringProperty(dinhDangTien(cell.getValue().donGia())));
        TableColumn<DonNhap.ChiTiet, String> colThanhTien = new TableColumn<>("Thành tiền");
        colThanhTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        colThanhTien.setCellValueFactory(cell -> new SimpleStringProperty(dinhDangTien(
                cell.getValue().donGia().multiply(BigDecimal.valueOf(cell.getValue().soLuong())))));
        tblChiTiet.getColumns().addAll(List.of(colMa, colTen, colSoLuong, colDonGia, colThanhTien));
        tblChiTiet.setPrefHeight(320);

        Label lblTongTien = new Label("Tổng tiền: " + dinhDangTien(donNhap.tongTien()));
        lblTongTien.getStyleClass().add("tieu-de-phan-he");
        VBox noiDung = new VBox(12, thongTin, tblChiTiet, lblTongTien);
        noiDung.setPadding(new Insets(8));
        noiDung.setPrefWidth(760);
        return noiDung;
    }

    private static String dinhDangTien(BigDecimal soTien) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(LOCALE_VI_VN);
        numberFormat.setMaximumFractionDigits(0);
        return numberFormat.format(soTien) + " đ";
    }
}