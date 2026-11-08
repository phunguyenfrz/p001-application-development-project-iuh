package com.petshop.gui.nhacungcap;

import com.petshop.bus.NhaCungCapBUS;
import com.petshop.entity.NhaCungCap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;

/**
 * Màn hình tra cứu nhà cung cấp. Chỉ tìm kiếm, lọc và xem thông tin.
 */
public class TimKiemNhaCungCapView extends VBox {

    private final NhaCungCapBUS controller = new NhaCungCapBUS();
    private final ObservableList<NhaCungCap> danhSachHienThi = FXCollections.observableArrayList();

    private final TextField txtTuKhoa = new TextField();
    private final TextField txtDiaChi = new TextField();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();
    private final TableView<NhaCungCap> tblDanhSach = new TableView<>();
    private final Button btnXemChiTiet = new Button("Xem chi tiết");
    private final Label lblKetQua = new Label("Tổng số: 0 nhà cung cấp");

    public TimKiemNhaCungCapView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("TÌM KIẾM & TRA CỨU NHÀ CUNG CẤP");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu theo mã, tên, số điện thoại, email hoặc địa chỉ; lọc theo trạng thái hợp tác.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        VBox khungChinh = new VBox(12);
        khungChinh.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(khungChinh, Priority.ALWAYS);

        khungChinh.getChildren().addAll(taoThanhTimKiem(), taoBang());
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);
        getChildren().addAll(lblTieuDe, lblMoTa, khungChinh);

        btnXemChiTiet.setDisable(true);
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener(
                (obs, cu, moi) -> btnXemChiTiet.setDisable(moi == null));
        tblDanhSach.setRowFactory(tv -> {
            TableRow<NhaCungCap> row = new TableRow<>();
            row.itemProperty().addListener((obs, cu, moi) -> {
                if (moi == null) {
                    row.setStyle("");
                } else if (!moi.isTrangThai()) {
                    row.setStyle("-fx-opacity: 0.55;");
                } else {
                    row.setStyle("");
                }
            });
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moXemChiTiet();
                }
            });
            return row;
        });
        taiDuLieu();
    }

    private VBox taoThanhTimKiem() {
        VBox thanhLoc = new VBox(10);

        // Hàng 1: Từ khóa + Địa chỉ + Trạng thái
        HBox hang1 = new HBox(10);
        hang1.setAlignment(Pos.CENTER_LEFT);

        Label lblTuKhoa = new Label("Từ khóa:");
        lblTuKhoa.setMinWidth(Region.USE_PREF_SIZE);

        txtTuKhoa.setPromptText("Mã, tên, SĐT, email...");
        txtTuKhoa.setPrefWidth(260);
        txtTuKhoa.setOnAction(e -> taiDuLieu());
        txtTuKhoa.textProperty().addListener((obs, cu, moi) -> taiDuLieu());

        Label lblDiaChi = new Label("Địa chỉ:");
        lblDiaChi.setMinWidth(Region.USE_PREF_SIZE);

        txtDiaChi.setPromptText("Nhập địa chỉ...");
        txtDiaChi.setPrefWidth(200);
        txtDiaChi.setOnAction(e -> taiDuLieu());
        txtDiaChi.textProperty().addListener((obs, cu, moi) -> taiDuLieu());

        Label lblTrangThai = new Label("Trạng thái:");
        lblTrangThai.setMinWidth(Region.USE_PREF_SIZE);

        cboTrangThai.setItems(FXCollections.observableArrayList(
                "Đang hoạt động", "Ngừng hợp tác", "Tất cả trạng thái"));
        cboTrangThai.setValue("Đang hoạt động");
        cboTrangThai.setPrefWidth(160);
        cboTrangThai.setMinWidth(Region.USE_PREF_SIZE);
        cboTrangThai.setOnAction(e -> taiDuLieu());

        hang1.getChildren().addAll(lblTuKhoa, txtTuKhoa, lblDiaChi, txtDiaChi, lblTrangThai, cboTrangThai);

        // Hàng 2: Tìm kiếm + Làm mới + Xem chi tiết + Kết quả
        HBox hang2 = new HBox(10);
        hang2.setAlignment(Pos.CENTER_LEFT);

        Button btnTim = new Button("Tìm kiếm");
        btnTim.getStyleClass().add("nut-chinh");
        btnTim.setMinWidth(Region.USE_PREF_SIZE);
        btnTim.setOnAction(e -> taiDuLieu());

        Button btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setMinWidth(Region.USE_PREF_SIZE);
        btnLamMoi.setOnAction(e -> {
            txtTuKhoa.clear();
            txtDiaChi.clear();
            cboTrangThai.setValue("Đang hoạt động");
            taiDuLieu();
            txtTuKhoa.requestFocus();
        });

        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXemChiTiet.setMinWidth(Region.USE_PREF_SIZE);
        btnXemChiTiet.setOnAction(e -> moXemChiTiet());

        lblKetQua.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-font-size: 13px;");
        lblKetQua.setMinWidth(Region.USE_PREF_SIZE);

        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        hang2.getChildren().addAll(btnTim, btnLamMoi, khoangTrong, btnXemChiTiet, lblKetQua);

        thanhLoc.getChildren().addAll(hang1, hang2);
        return thanhLoc;
    }

    private TableView<NhaCungCap> taoBang() {
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy nhà cung cấp phù hợp"));
        tblDanhSach.setItems(danhSachHienThi);

        TableColumn<NhaCungCap, String> colMa = new TableColumn<>("Mã NCC");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaNhaCungCap()));
        TableColumn<NhaCungCap, String> colTen = new TableColumn<>("Tên nhà cung cấp");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenNhaCungCap()));
        TableColumn<NhaCungCap, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSoDienThoai()));
        TableColumn<NhaCungCap, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(giaTriHoacGach(d.getValue().getEmail())));
        TableColumn<NhaCungCap, String> colDiaChi = new TableColumn<>("Địa chỉ");
        colDiaChi.setCellValueFactory(d -> new SimpleStringProperty(giaTriHoacGach(d.getValue().getDiaChi())));
        TableColumn<NhaCungCap, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isTrangThai() ? "Đang hoạt động" : "Ngừng hợp tác"));

        tblDanhSach.getColumns().setAll(List.of(colMa, colTen, colSdt, colEmail, colDiaChi, colTrangThai));
        return tblDanhSach;
    }

    private void taiDuLieu() {
        try {
            Boolean trangThai = switch (cboTrangThai.getValue() == null ? "Đang hoạt động" : cboTrangThai.getValue()) {
                case "Ngừng hợp tác" -> false;
                case "Tất cả trạng thái" -> null;
                default -> true;
            };
            List<NhaCungCap> ketQua = controller.timKiemNhaCungCap(txtTuKhoa.getText(), txtDiaChi.getText(), trangThai);
            danhSachHienThi.setAll(ketQua);
            tblDanhSach.getSelectionModel().clearSelection();
            lblKetQua.setText("Tổng số: " + ketQua.size() + " nhà cung cấp");
            btnXemChiTiet.setDisable(true);
        } catch (Exception ex) {
            danhSachHienThi.clear();
            lblKetQua.setText("Tổng số: 0 nhà cung cấp");
            hienThiLoi(ex.getMessage());
        }
    }

    private void moXemChiTiet() {
        NhaCungCap ncc = tblDanhSach.getSelectionModel().getSelectedItem();
        if (ncc == null) return;
        Window owner = getScene() != null ? getScene().getWindow() : null;
        XemChiTietNhaCungCapDialog.hienThi(ncc, owner);
    }

    private String giaTriHoacGach(String giaTri) {
        return giaTri == null || giaTri.isBlank() ? "-" : giaTri;
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung == null ? "Không thể tìm kiếm nhà cung cấp." : noiDung);
        alert.showAndWait();
    }
}
