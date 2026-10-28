package com.petshop.view.khuyenmai;

import com.petshop.controller.KhuyenMaiController;
import com.petshop.model.KhuyenMai;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Màn hình tìm kiếm khuyến mãi theo mã, tên và trạng thái.
 */
public class TimKiemKhuyenMaiView extends VBox {

    private final KhuyenMaiController controller = new KhuyenMaiController();
    private final TextField txtMa = new TextField();
    private final TextField txtTen = new TextField();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();
    private final TableView<KhuyenMai> tblDanhSach = new TableView<>();
    private final Button btnTim = new Button("Tìm kiếm");
    private final Label lblKetQua = new Label("Tổng số: 0 khuyến mãi");

    public TimKiemKhuyenMaiView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");
        Label lblTieuDe = new Label("KHUYẾN MÃI - TÌM KIẾM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tìm theo mã, tên chương trình hoặc trạng thái áp dụng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        getChildren().addAll(lblTieuDe, lblMoTa, taoKhungChinh());
        taiLaiBang();
    }

    private VBox taoKhungChinh() {
        VBox khung = new VBox(12);
        khung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(khung, Priority.ALWAYS);

        HBox congCu = new HBox(10);
        congCu.setAlignment(Pos.CENTER_LEFT);
        txtMa.setPromptText("Mã khuyến mãi");
        txtTen.setPromptText("Tên chương trình");
        cboTrangThai.setItems(FXCollections.observableArrayList("Tất cả", "Đang áp dụng", "Ngừng áp dụng"));
        cboTrangThai.setValue("Tất cả");
        txtMa.setPrefWidth(180);
        txtTen.setPrefWidth(220);
        btnTim.getStyleClass().add("nut-chinh");
        Button btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnTim.setOnAction(e -> xuLyTimKiem());
        btnLamMoi.setOnAction(e -> taiLaiBang());
        txtMa.setOnAction(e -> xuLyTimKiem());
        txtTen.setOnAction(e -> xuLyTimKiem());
        cboTrangThai.setOnAction(e -> xuLyTimKiem());
        congCu.getChildren().addAll(new Label("Mã:"), txtMa, new Label("Tên:"), txtTen,
                new Label("Trạng thái:"), cboTrangThai, btnTim, btnLamMoi);

        taoCotBang();
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy khuyến mãi phù hợp"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);
        khung.getChildren().addAll(congCu, tblDanhSach, lblKetQua);
        return khung;
    }

    private void taoCotBang() {
        TableColumn<KhuyenMai, String> colMa = new TableColumn<>("Mã khuyến mãi");
        colMa.setPrefWidth(170);
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaKhuyenMai()));
        TableColumn<KhuyenMai, String> colTen = new TableColumn<>("Tên chương trình");
        colTen.setPrefWidth(260);
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenChuongTrinh()));
        TableColumn<KhuyenMai, String> colGiam = new TableColumn<>("Mức giảm");
        colGiam.setPrefWidth(100);
        colGiam.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhanTramGiam().stripTrailingZeros().toPlainString() + "%"));
        TableColumn<KhuyenMai, String> colBatDau = new TableColumn<>("Ngày bắt đầu");
        colBatDau.setPrefWidth(130);
        colBatDau.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNgayBatDau().toString()));
        TableColumn<KhuyenMai, String> colKetThuc = new TableColumn<>("Ngày kết thúc");
        colKetThuc.setPrefWidth(130);
        colKetThuc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNgayKetThuc().toString()));
        TableColumn<KhuyenMai, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(140);
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isTrangThai() ? "Đang áp dụng" : "Ngừng áp dụng"));
        tblDanhSach.getColumns().setAll(List.of(colMa, colTen, colGiam, colBatDau, colKetThuc, colTrangThai));
    }

    private void xuLyTimKiem() {
        try {
            Boolean trangThai = null;
            if ("Đang áp dụng".equals(cboTrangThai.getValue())) trangThai = true;
            if ("Ngừng áp dụng".equals(cboTrangThai.getValue())) trangThai = false;
            List<KhuyenMai> ketQua = controller.timKiemKhuyenMai(txtMa.getText(), txtTen.getText(), trangThai);
            tblDanhSach.setItems(FXCollections.observableArrayList(ketQua));
            lblKetQua.setText("Tổng số: " + ketQua.size() + " khuyến mãi");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void taiLaiBang() {
        txtMa.clear();
        txtTen.clear();
        cboTrangThai.setValue("Tất cả");
        xuLyTimKiem();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
