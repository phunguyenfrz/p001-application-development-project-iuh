package com.petshop.view.nhacungcap;

import com.petshop.controller.NhaCungCapController;
import com.petshop.model.NhaCungCap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Màn hình tìm kiếm nhà cung cấp theo mã, tên hoặc số điện thoại.
 * Tích hợp nút Sửa (mở form cập nhật) và nút Xóa mềm (có hộp thoại xác nhận).
 * Tuân thủ phong cách giao diện TimKiemKhuyenMaiView.
 */
public class TimKiemNhaCungCapView extends VBox {

    private final NhaCungCapController controller = new NhaCungCapController();
    private final ObservableList<NhaCungCap> danhSachHienThi = FXCollections.observableArrayList();

    private final TextField txtTuKhoa = new TextField();
    private final TableView<NhaCungCap> tblDanhSach = new TableView<>();
    private final Button btnTim = new Button("Tìm kiếm");
    private final Button btnSua = new Button("Sửa");
    private final Button btnXoaMem = new Button("Ngừng hợp tác");
    private final Label lblKetQua = new Label("Tổng số: 0 nhà cung cấp");

    public TimKiemNhaCungCapView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("NHÀ CUNG CẤP - TÌM KIẾM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tìm kiếm đối tác cung cấp theo mã, tên hoặc số điện thoại (chỉ hiển thị NCC đang hoạt động).");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        getChildren().addAll(lblTieuDe, lblMoTa, taoKhungChinh());
        tblDanhSach.setItems(danhSachHienThi);
        capNhatTrangThaiNut(null);
        taiLaiBang();
    }

    private VBox taoKhungChinh() {
        VBox khung = new VBox(12);
        khung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(khung, Priority.ALWAYS);

        HBox congCu = new HBox(10);
        congCu.setAlignment(Pos.CENTER_LEFT);

        txtTuKhoa.setPromptText("Nhập mã, tên hoặc SĐT nhà cung cấp...");
        txtTuKhoa.setPrefWidth(300);

        btnTim.getStyleClass().add("nut-chinh");
        btnSua.getStyleClass().add("nut-phu");
        btnXoaMem.getStyleClass().add("nut-xoa");
        Button btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");

        btnTim.setOnAction(e -> xuLyTimKiem());
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());
        btnLamMoi.setOnAction(e -> {
            txtTuKhoa.clear();
            xuLyTimKiem();
        });
        btnSua.setOnAction(e -> moCuaSoSua());
        btnXoaMem.setOnAction(e -> xuLyXoaMem());

        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        congCu.getChildren().addAll(
                new Label("Từ khóa:"), txtTuKhoa, btnTim, btnLamMoi,
                khoangTrong, btnSua, btnXoaMem
        );

        taoCotBang();
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy nhà cung cấp phù hợp"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));

        khung.getChildren().addAll(congCu, tblDanhSach, lblKetQua);
        return khung;
    }

    private void taoCotBang() {
        TableColumn<NhaCungCap, String> colMa = new TableColumn<>("Mã NCC");
        colMa.setPrefWidth(160);
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaNhaCungCap()));

        TableColumn<NhaCungCap, String> colTen = new TableColumn<>("Tên nhà cung cấp");
        colTen.setPrefWidth(240);
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenNhaCungCap()));

        TableColumn<NhaCungCap, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setPrefWidth(120);
        colSdt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSoDienThoai()));

        TableColumn<NhaCungCap, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(180);
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getEmail() != null ? d.getValue().getEmail() : ""
        ));

        TableColumn<NhaCungCap, String> colDiaChi = new TableColumn<>("Địa chỉ");
        colDiaChi.setPrefWidth(220);
        colDiaChi.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDiaChi() != null ? d.getValue().getDiaChi() : ""
        ));

        TableColumn<NhaCungCap, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isTrangThai() ? "Đang hoạt động" : "Ngừng hợp tác"
        ));

        tblDanhSach.getColumns().setAll(List.of(colMa, colTen, colSdt, colEmail, colDiaChi, colTrangThai));
    }

    private void capNhatTrangThaiNut(NhaCungCap ncc) {
        btnSua.setDisable(ncc == null);
        btnXoaMem.setDisable(ncc == null || !ncc.isTrangThai());
    }

    private void xuLyTimKiem() {
        try {
            String tuKhoa = txtTuKhoa.getText();
            List<NhaCungCap> ketQua = controller.timKiemNhaCungCap(tuKhoa);
            danhSachHienThi.setAll(ketQua);
            tblDanhSach.getSelectionModel().clearSelection();
            tblDanhSach.refresh();
            lblKetQua.setText("Tổng số: " + ketQua.size() + " nhà cung cấp");
            capNhatTrangThaiNut(null);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void taiLaiBang() {
        xuLyTimKiem();
    }

    private void moCuaSoSua() {
        NhaCungCap chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiLoi("Vui lòng chọn nhà cung cấp cần sửa!");
            return;
        }

        VBox noiDung = taoFormCapNhat(chon);
        Stage cuaSo = taoCuaSo("Cập nhật nhà cung cấp", noiDung, 580, 430);
        cuaSo.showAndWait();
    }

    private VBox taoFormCapNhat(NhaCungCap chon) {
        VBox goc = new VBox(14);
        goc.setPadding(new Insets(18));
        goc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("CẬP NHẬT NHÀ CUNG CẤP");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);

        TextField txtTen = new TextField(chon.getTenNhaCungCap());
        TextField txtSdt = new TextField(chon.getSoDienThoai());
        TextField txtEmail = new TextField(chon.getEmail() != null ? chon.getEmail() : "");
        TextField txtDiaChi = new TextField(chon.getDiaChi() != null ? chon.getDiaChi() : "");
        ComboBox<String> cboTrangThai = new ComboBox<>(FXCollections.observableArrayList("Đang hoạt động", "Ngừng hợp tác"));
        cboTrangThai.setValue(chon.isTrangThai() ? "Đang hoạt động" : "Ngừng hợp tác");

        txtSdt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                txtSdt.setText(cu);
            }
        });

        luoi.add(new Label("Tên nhà cung cấp (*):"), 0, 0);
        luoi.add(txtTen, 1, 0);
        luoi.add(new Label("Số điện thoại (*):"), 0, 1);
        luoi.add(txtSdt, 1, 1);
        luoi.add(new Label("Email:"), 0, 2);
        luoi.add(txtEmail, 1, 2);
        luoi.add(new Label("Địa chỉ:"), 0, 3);
        luoi.add(txtDiaChi, 1, 3);
        luoi.add(new Label("Trạng thái:"), 0, 4);
        luoi.add(cboTrangThai, 1, 4);

        GridPane.setHgrow(txtTen, Priority.ALWAYS);
        GridPane.setHgrow(txtSdt, Priority.ALWAYS);
        GridPane.setHgrow(txtEmail, Priority.ALWAYS);
        GridPane.setHgrow(txtDiaChi, Priority.ALWAYS);
        GridPane.setHgrow(cboTrangThai, Priority.ALWAYS);

        HBox nut = new HBox(10);
        nut.setAlignment(Pos.CENTER_RIGHT);
        Button btnLuu = new Button("Lưu thay đổi");
        Button btnDong = new Button("Đóng");

        btnLuu.getStyleClass().add("nut-chinh");
        btnDong.getStyleClass().add("nut-phu");
        nut.getChildren().addAll(btnDong, btnLuu);

        VBox khung = new VBox(14);
        khung.getStyleClass().add("hop-noi-dung");
        khung.getChildren().addAll(luoi, nut);
        goc.getChildren().addAll(lblTieuDe, khung);

        btnDong.setOnAction(e -> ((Stage) btnDong.getScene().getWindow()).close());
        btnLuu.setOnAction(e -> {
            try {
                String ten = txtTen.getText() == null ? "" : txtTen.getText().trim();
                String sdt = txtSdt.getText() == null ? "" : txtSdt.getText().trim();
                String email = txtEmail.getText() == null ? null : txtEmail.getText().trim();
                String diaChi = txtDiaChi.getText() == null ? null : txtDiaChi.getText().trim();
                boolean trangThai = "Đang hoạt động".equals(cboTrangThai.getValue());

                NhaCungCap moi = new NhaCungCap(chon.getMaNhaCungCap(), ten, sdt, diaChi, email, trangThai);

                if (controller.capNhatNhaCungCap(moi)) {
                    hienThiThongBao("Thành công", "Đã cập nhật nhà cung cấp: " + chon.getMaNhaCungCap());
                    Stage stage = (Stage) txtTen.getScene().getWindow();
                    stage.close();
                    taiLaiBang();
                } else {
                    hienThiLoi("Không thể cập nhật nhà cung cấp!");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                hienThiLoi(ex.getMessage());
            }
        });

        return goc;
    }

    private void xuLyXoaMem() {
        NhaCungCap chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) return;

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận ngừng hợp tác");
        xacNhan.setHeaderText(null);
        xacNhan.setContentText("Bạn có chắc chắn muốn ngừng hợp tác với nhà cung cấp:\n"
                + chon.getTenNhaCungCap() + " (" + chon.getMaNhaCungCap() + ")?");

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                if (controller.xoaMemNhaCungCap(chon.getMaNhaCungCap())) {
                    hienThiThongBao("Thành công", "Đã ngừng hợp tác với nhà cung cấp: " + chon.getMaNhaCungCap());
                    taiLaiBang();
                } else {
                    hienThiLoi("Không thể ngừng hợp tác với nhà cung cấp!");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                hienThiLoi(ex.getMessage());
            }
        }
    }

    private Stage taoCuaSo(String tieuDe, Parent noiDung, double rong, double cao) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            stage.initOwner(getScene().getWindow());
        }
        stage.setTitle(tieuDe);
        Scene scene = new Scene(noiDung, rong, cao);
        try {
            scene.getStylesheets().add(getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm());
        } catch (Exception ignored) {
        }
        stage.setScene(scene);
        return stage;
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
