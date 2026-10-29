package com.petshop.view.nhacungcap;

import com.petshop.controller.NhaCungCapController;
import com.petshop.model.NhaCungCap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
 * Màn hình danh sách và cập nhật thông tin nhà cung cấp.
 * Tuân thủ phong cách giao diện CapNhatKhuyenMaiView.
 */
public class CapNhatNhaCungCapView extends VBox {

    private final NhaCungCapController controller = new NhaCungCapController();
    private final TableView<NhaCungCap> tblDanhSach = new TableView<>();
    private final Button btnThem = new Button("Thêm mới");
    private final Button btnCapNhat = new Button("Cập nhật");
    private final Button btnNgung = new Button("Ngừng hợp tác");
    private final Button btnLamMoi = new Button("Làm mới");

    public CapNhatNhaCungCapView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("NHÀ CUNG CẤP - CẬP NHẬT THÔNG TIN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Chọn một nhà cung cấp trong danh sách để cập nhật hoặc ngừng hợp tác.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        getChildren().addAll(lblTieuDe, lblMoTa, taoKhungBang());
        capNhatTrangThaiNut(null);
        taiLaiBang();
    }

    private VBox taoKhungBang() {
        VBox khung = new VBox(10);
        khung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(khung, Priority.ALWAYS);

        HBox thanhNut = new HBox(10);
        thanhNut.getStyleClass().add("thanh-tac-vu");
        thanhNut.setAlignment(Pos.CENTER_LEFT);

        Label lblDanhSach = new Label("Danh sách nhà cung cấp");
        lblDanhSach.setStyle("-fx-font-weight: bold;");

        Region trong = new Region();
        HBox.setHgrow(trong, Priority.ALWAYS);

        btnThem.getStyleClass().add("nut-chinh");
        btnCapNhat.getStyleClass().add("nut-phu");
        btnNgung.getStyleClass().add("nut-xoa");
        btnLamMoi.getStyleClass().add("nut-phu");

        btnThem.setOnAction(e -> moCuaSoThem());
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());
        btnNgung.setOnAction(e -> xuLyNgungHopTac());
        btnLamMoi.setOnAction(e -> taiLaiBang());

        thanhNut.getChildren().addAll(lblDanhSach, trong, btnThem, btnCapNhat, btnNgung, btnLamMoi);

        taoCotBang();
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có nhà cung cấp nào"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));

        khung.getChildren().addAll(thanhNut, tblDanhSach);
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
        btnCapNhat.setDisable(ncc == null);
        btnNgung.setDisable(ncc == null || !ncc.isTrangThai());
    }

    public void moCuaSoThem() {
        Stage cuaSo = taoCuaSo("Thêm nhà cung cấp mới", new ThemNhaCungCapView(this::taiLaiBang), 580, 380);
        cuaSo.showAndWait();
    }

    public void moCuaSoCapNhat() {
        NhaCungCap chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiLoi("Vui lòng chọn nhà cung cấp cần cập nhật!");
            return;
        }
        moCuaSoCapNhat(chon);
    }

    public void moCuaSoCapNhat(NhaCungCap chon) {
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
        btnLuu.setOnAction(e -> luuCapNhat(chon, txtTen, txtSdt, txtEmail, txtDiaChi, cboTrangThai));

        return goc;
    }

    private void luuCapNhat(NhaCungCap chon, TextField txtTen, TextField txtSdt, TextField txtEmail,
                            TextField txtDiaChi, ComboBox<String> cboTrangThai) {
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
    }

    private void xuLyNgungHopTac() {
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

    public void taiLaiBang() {
        try {
            tblDanhSach.setItems(FXCollections.observableArrayList(controller.layTatCaNhaCungCap()));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
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

