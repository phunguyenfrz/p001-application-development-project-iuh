package com.petshop.view.khuyenmai;

import com.petshop.controller.KhuyenMaiController;
import com.petshop.model.KhuyenMai;
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
import javafx.scene.control.DatePicker;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình danh sách, cập nhật thông tin và ngừng chương trình khuyến mãi.
 */
public class CapNhatKhuyenMaiView extends VBox {

    private final KhuyenMaiController controller = new KhuyenMaiController();
    private final TableView<KhuyenMai> tblDanhSach = new TableView<>();
    private final Button btnThem = new Button("Thêm mới");
    private final Button btnCapNhat = new Button("Cập nhật");
    private final Button btnNgung = new Button("Ngừng áp dụng");
    private final Button btnLamMoi = new Button("Làm mới");

    public CapNhatKhuyenMaiView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");
        Label lblTieuDe = new Label("KHUYẾN MÃI - CẬP NHẬT CHƯƠNG TRÌNH");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Chọn một dòng để cập nhật hoặc ngừng chương trình khuyến mãi.");
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
        Label lblDanhSach = new Label("Danh sách khuyến mãi");
        lblDanhSach.setStyle("-fx-font-weight: bold;");
        Region trong = new Region();
        HBox.setHgrow(trong, Priority.ALWAYS);
        btnThem.getStyleClass().add("nut-chinh");
        btnCapNhat.getStyleClass().add("nut-phu");
        btnNgung.getStyleClass().add("nut-xoa");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnThem.setOnAction(e -> moCuaSoThem());
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());
        btnNgung.setOnAction(e -> xuLyNgung());
        btnLamMoi.setOnAction(e -> taiLaiBang());
        thanhNut.getChildren().addAll(lblDanhSach, trong, btnThem, btnCapNhat, btnNgung, btnLamMoi);

        taoCotBang();
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có chương trình khuyến mãi"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));
        khung.getChildren().addAll(thanhNut, tblDanhSach);
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

    private void capNhatTrangThaiNut(KhuyenMai khuyenMai) {
        btnCapNhat.setDisable(khuyenMai == null);
        btnNgung.setDisable(khuyenMai == null || !khuyenMai.isTrangThai());
    }

    private void moCuaSoThem() {
        Stage cuaSo = taoCuaSo("Thêm khuyến mãi mới", new ThemKhuyenMaiView(this::taiLaiBang), 560, 390);
        cuaSo.showAndWait();
    }

    private void moCuaSoCapNhat() {
        KhuyenMai chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiLoi("Vui lòng chọn khuyến mãi cần cập nhật!");
            return;
        }
        VBox noiDung = taoFormCapNhat(chon);
        Stage cuaSo = taoCuaSo("Cập nhật khuyến mãi", noiDung, 560, 430);
        cuaSo.showAndWait();
    }

    private VBox taoFormCapNhat(KhuyenMai chon) {
        VBox goc = new VBox(14);
        goc.setPadding(new Insets(18));
        goc.getStyleClass().add("khung-phan-he");
        Label lblTieuDe = new Label("CẬP NHẬT KHUYẾN MÃI");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);
        TextField txtTen = new TextField(chon.getTenChuongTrinh());
        TextField txtGiam = new TextField(chon.getPhanTramGiam().stripTrailingZeros().toPlainString());
        DatePicker dpBatDau = new DatePicker(chon.getNgayBatDau());
        DatePicker dpKetThuc = new DatePicker(chon.getNgayKetThuc());
        ComboBox<String> cboTrangThai = new ComboBox<>(FXCollections.observableArrayList("Đang áp dụng", "Ngừng áp dụng"));
        cboTrangThai.setValue(chon.isTrangThai() ? "Đang áp dụng" : "Ngừng áp dụng");
        luoi.add(new Label("Tên chương trình (*):"), 0, 0);
        luoi.add(txtTen, 1, 0);
        luoi.add(new Label("Mức giảm (%)(*):"), 0, 1);
        luoi.add(txtGiam, 1, 1);
        luoi.add(new Label("Ngày bắt đầu (*):"), 0, 2);
        luoi.add(dpBatDau, 1, 2);
        luoi.add(new Label("Ngày kết thúc (*):"), 0, 3);
        luoi.add(dpKetThuc, 1, 3);
        luoi.add(new Label("Trạng thái:"), 0, 4);
        luoi.add(cboTrangThai, 1, 4);

        txtGiam.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d{0,2})?")) txtGiam.setText(cu);
        });
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
        btnLuu.setOnAction(e -> luuCapNhat(chon, txtTen, txtGiam, dpBatDau, dpKetThuc, cboTrangThai));
        return goc;
    }

    private void luuCapNhat(KhuyenMai chon, TextField txtTen, TextField txtGiam, DatePicker dpBatDau,
                           DatePicker dpKetThuc, ComboBox<String> cboTrangThai) {
        try {
            String ten = txtTen.getText() == null ? "" : txtTen.getText().trim();
            BigDecimal giam;
            try {
                giam = new BigDecimal(txtGiam.getText().trim());
            } catch (Exception e) {
                throw new IllegalArgumentException("Mức giảm phải là số hợp lệ!");
            }
            if (dpBatDau.getValue() == null || dpKetThuc.getValue() == null) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ ngày bắt đầu và ngày kết thúc!");
            }
            boolean trangThai = "Đang áp dụng".equals(cboTrangThai.getValue());
            KhuyenMai moi = new KhuyenMai(chon.getMaKhuyenMai(), ten, giam,
                    dpBatDau.getValue(), dpKetThuc.getValue(), trangThai);
            if (controller.capNhatKhuyenMai(moi)) {
                hienThiThongBao("Thành công", "Đã cập nhật khuyến mãi: " + chon.getMaKhuyenMai());
                Stage stage = (Stage) txtTen.getScene().getWindow();
                stage.close();
                taiLaiBang();
            } else {
                hienThiLoi("Không thể cập nhật khuyến mãi!");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void xuLyNgung() {
        KhuyenMai chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) return;
        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận ngừng áp dụng");
        xacNhan.setHeaderText(null);
        xacNhan.setContentText("Ngừng chương trình " + chon.getTenChuongTrinh() + "?");
        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                if (controller.xoaKhuyenMai(chon.getMaKhuyenMai())) {
                    hienThiThongBao("Thành công", "Đã ngừng khuyến mãi: " + chon.getMaKhuyenMai());
                    taiLaiBang();
                } else {
                    hienThiLoi("Không thể ngừng khuyến mãi!");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                hienThiLoi(ex.getMessage());
            }
        }
    }

    private Stage taoCuaSo(String tieuDe, Parent noiDung, double rong, double cao) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) stage.initOwner(getScene().getWindow());
        stage.setTitle(tieuDe);
        Scene scene = new Scene(noiDung, rong, cao);
        try {
            scene.getStylesheets().add(getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm());
        } catch (Exception ignored) {
        }
        stage.setScene(scene);
        return stage;
    }

    private void taiLaiBang() {
        try {
            tblDanhSach.setItems(FXCollections.observableArrayList(controller.layDanhSachKhuyenMai()));
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
