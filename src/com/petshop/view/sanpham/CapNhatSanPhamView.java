package com.petshop.view.sanpham;

import com.petshop.controller.SanPhamController;
import com.petshop.model.SanPham;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CapNhatSanPhamView extends VBox {

    private static final Locale LOCALE_VI_VN = new Locale("vi", "VN");

    private final SanPhamController sanPhamController;

    private final TextField txtTimKiemNhanh = new TextField();
    private final Button btnThemMoi = new Button("Thêm sản phẩm mới");
    private final Button btnSua = new Button("Chỉnh sửa");
    private final Button btnTrangThai = new Button("Ngừng kinh doanh");
    private final Button btnXoa = new Button("Xóa");
    private final Button btnLamMoi = new Button("Làm mới");

    private final TableView<SanPham> tblDanhSach = new TableView<>();

    public CapNhatSanPhamView() {
        this(new SanPhamController());
    }

    public CapNhatSanPhamView(SanPhamController sanPhamController) {
        this.sanPhamController = sanPhamController;

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT SẢN PHẨM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Chọn sản phẩm trong danh sách và bấm 'Chỉnh sửa' hoặc bấm đúp dòng để cập nhật thông tin.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, pnlBang);

        taiDanhSach();
    }

    private VBox taoKhungBang() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        HBox pnlTacVu = new HBox(10);
        pnlTacVu.setAlignment(Pos.CENTER_LEFT);

        txtTimKiemNhanh.setPromptText("Lọc nhanh theo mã, tên, mã vạch...");
        txtTimKiemNhanh.setPrefWidth(260);
        txtTimKiemNhanh.textProperty().addListener((obs, oldV, newV) -> taiDanhSach());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnThemMoi.getStyleClass().add("nut-chinh");
        btnThemMoi.setOnAction(e -> moCuaSoThemMoi());

        btnSua.getStyleClass().add("nut-phu");
        btnSua.disableProperty().bind(tblDanhSach.getSelectionModel().selectedItemProperty().isNull());
        btnSua.setOnAction(e -> moCuaSoCapNhat());

        btnTrangThai.getStyleClass().add("nut-phu");
        btnTrangThai.disableProperty().bind(tblDanhSach.getSelectionModel().selectedItemProperty().isNull());
        btnTrangThai.setOnAction(e -> xuLyDoiTrangThai());

        btnXoa.getStyleClass().add("nut-xoa");
        btnXoa.disableProperty().bind(tblDanhSach.getSelectionModel().selectedItemProperty().isNull());
        btnXoa.setOnAction(e -> xuLyXoa());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            txtTimKiemNhanh.clear();
            taiDanhSach();
        });

        pnlTacVu.getChildren().addAll(txtTimKiemNhanh, spacer, btnThemMoi, btnSua, btnTrangThai, btnXoa, btnLamMoi);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy sản phẩm nào"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã SP");
        colMa.setPrefWidth(160);
        colMa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(220);
        colTen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colMaVach = new TableColumn<>("Mã vạch");
        colMaVach.setPrefWidth(130);
        colMaVach.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaVach()));

        TableColumn<SanPham, String> colNhaCungCap = new TableColumn<>("Nhà cung cấp");
        colNhaCungCap.setPrefWidth(200);
        colNhaCungCap.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTenNhaCungCap() != null ? c.getValue().getTenNhaCungCap() : c.getValue().getMaNhaCungCap()));

        TableColumn<SanPham, String> colGiaBan = new TableColumn<>("Giá bán");
        colGiaBan.setPrefWidth(120);
        colGiaBan.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaBan.setCellValueFactory(c -> new SimpleStringProperty(dinhDangTien(c.getValue().getGiaBan())));

        TableColumn<SanPham, String> colDonVi = new TableColumn<>("ĐVT");
        colDonVi.setPrefWidth(80);
        colDonVi.setStyle("-fx-alignment: CENTER;");
        colDonVi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colTonKho = new TableColumn<>("Tồn kho");
        colTonKho.setPrefWidth(90);
        colTonKho.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTonKho.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getSoLuongTon())));

        TableColumn<SanPham, String> colNguong = new TableColumn<>("Ngưỡng tối thiểu");
        colNguong.setPrefWidth(120);
        colNguong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNguong.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNguongTonToiThieu())));

        TableColumn<SanPham, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(120);
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isTrangThai() ? "Đang bán" : "Ngừng bán"));

        tblDanhSach.getColumns().addAll(List.of(colMa, colTen, colMaVach, colNhaCungCap, colGiaBan, colDonVi, colTonKho, colNguong, colTrangThai));

        tblDanhSach.setRowFactory(tv -> {
            TableRow<SanPham> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moCuaSoCapNhat();
                }
            });
            return row;
        });
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, item) ->
                btnTrangThai.setText(item != null && !item.isTrangThai() ? "Kinh doanh lại" : "Ngừng kinh doanh"));

        pnlKhung.getChildren().addAll(pnlTacVu, tblDanhSach);
        return pnlKhung;
    }

    public void taiDanhSach() {
        try {
            String tuKhoa = txtTimKiemNhanh.getText() != null ? txtTimKiemNhanh.getText().trim() : "";
            List<SanPham> ds = sanPhamController.timKiemSanPham(tuKhoa, null, null);
            tblDanhSach.setItems(FXCollections.observableArrayList(ds));
        } catch (Exception e) {
            hienThiLoi("Lỗi tải danh sách sản phẩm: " + e.getMessage());
        }
    }

    private void moCuaSoThemMoi() {
        Stage stageThem = new Stage();
        stageThem.setTitle("Thêm sản phẩm mới");
        stageThem.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            stageThem.initOwner(getScene().getWindow());
        }
        ThemSanPhamView viewThem = new ThemSanPhamView(sanPhamController, () -> {
            taiDanhSach();
            stageThem.close();
        });
        stageThem.setScene(new Scene(viewThem, 650, 480));
        stageThem.showAndWait();
    }

    private void moCuaSoCapNhat() {
        SanPham chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            return;
        }

        Stage stageSua = new Stage();
        stageSua.setTitle("Chỉnh sửa sản phẩm: " + chon.getMaSanPham());
        stageSua.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            stageSua.initOwner(getScene().getWindow());
        }

        VBox formSua = taoFormSua(chon, stageSua);
        stageSua.setScene(new Scene(formSua, 620, 460));
        stageSua.showAndWait();
    }

    private VBox taoFormSua(SanPham sp, Stage stage) {
        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("CẬP NHẬT THÔNG TIN SẢN PHẨM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMa = new Label("Mã sản phẩm: " + sp.getMaSanPham());
        lblMa.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976d2;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField txtTen = new TextField(sp.getTenSanPham());
        ComboBox<SanPham.NhaCungCapSimple> cboNcc = new ComboBox<>();
        cboNcc.setMaxWidth(Double.MAX_VALUE);
        List<SanPham.NhaCungCapSimple> dsNcc = sanPhamController.layDanhSachNhaCungCap();
        cboNcc.getItems().setAll(dsNcc);
        for (SanPham.NhaCungCapSimple ncc : dsNcc) {
            if (ncc.maNhaCungCap().equals(sp.getMaNhaCungCap())) {
                cboNcc.getSelectionModel().select(ncc);
                break;
            }
        }

        TextField txtMv = new TextField(sp.getMaVach());
        TextField txtGia = new TextField(sp.getGiaBan() != null ? sp.getGiaBan().toPlainString() : "0");
        TextField txtDvt = new TextField(sp.getDonViTinh());
        TextField txtNguong = new TextField(String.valueOf(sp.getNguongTonToiThieu()));

        chanNhapSo(txtGia, false);
        chanNhapSo(txtNguong, true);

        grid.add(new Label("Tên sản phẩm (*):"), 0, 0);
        grid.add(txtTen, 1, 0);
        grid.add(new Label("Nhà cung cấp (*):"), 0, 1);
        grid.add(cboNcc, 1, 1);
        grid.add(new Label("Mã vạch (*):"), 0, 2);
        grid.add(txtMv, 1, 2);
        grid.add(new Label("Giá bán (VNĐ) (*):"), 0, 3);
        grid.add(txtGia, 1, 3);
        grid.add(new Label("Đơn vị tính (*):"), 0, 4);
        grid.add(txtDvt, 1, 4);
        grid.add(new Label("Ngưỡng tồn tối thiểu:"), 0, 5);
        grid.add(txtNguong, 1, 5);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(170);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> stage.close());

        Button btnLuu = new Button("Lưu thay đổi");
        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> {
            String ten = txtTen.getText() != null ? txtTen.getText().trim() : "";
            SanPham.NhaCungCapSimple nccChon = cboNcc.getSelectionModel().getSelectedItem();
            String mv = txtMv.getText() != null ? txtMv.getText().trim() : "";
            String chuoiGia = txtGia.getText() != null ? txtGia.getText().trim() : "";
            String dvt = txtDvt.getText() != null ? txtDvt.getText().trim() : "";
            String chuoiNguong = txtNguong.getText() != null ? txtNguong.getText().trim() : "10";

            if (ten.isEmpty() || mv.isEmpty() || chuoiGia.isEmpty() || dvt.isEmpty() || nccChon == null) {
                hienThiLoi("Vui lòng điền đầy đủ các thông tin bắt buộc (*)");
                return;
            }

            try {
                BigDecimal giaBan = new BigDecimal(chuoiGia);
                int nguong = Integer.parseInt(chuoiNguong);

                sp.setTenSanPham(ten);
                sp.setMaNhaCungCap(nccChon.maNhaCungCap());
                sp.setMaVach(mv);
                sp.setGiaBan(giaBan);
                sp.setDonViTinh(dvt);
                sp.setNguongTonToiThieu(nguong);

                if (sanPhamController.capNhatSanPham(sp)) {
                    Alert ok = new Alert(Alert.AlertType.INFORMATION, "Cập nhật sản phẩm thành công!");
                    ok.initOwner(stage);
                    ok.showAndWait();
                    taiDanhSach();
                    stage.close();
                } else {
                    hienThiLoi("Không tìm thấy sản phẩm để cập nhật.");
                }
            } catch (Exception ex) {
                hienThiLoi(ex.getMessage());
            }
        });

        pnlNut.getChildren().addAll(btnDong, btnLuu);
        root.getChildren().addAll(lblTieuDe, lblMa, grid, pnlNut);
        return root;
    }

    private void chanNhapSo(TextField tf, boolean soNguyen) {
        tf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;
            if (soNguyen) {
                if (!newVal.matches("\\d*")) {
                    tf.setText(oldVal);
                }
            } else {
                if (!newVal.matches("\\d*(\\.\\d*)?")) {
                    tf.setText(oldVal);
                }
            }
        });
    }

    private void xuLyDoiTrangThai() {
        SanPham sp = tblDanhSach.getSelectionModel().getSelectedItem();
        if (sp == null) return;

        boolean trangThaiMoi = !sp.isTrangThai();
        String hanhDong = trangThaiMoi ? "kinh doanh lại" : "ngừng kinh doanh";
        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle(trangThaiMoi ? "Xác nhận kinh doanh lại" : "Xác nhận ngừng kinh doanh");
        xacNhan.setHeaderText(null);
        xacNhan.setContentText("Bạn có chắc muốn " + hanhDong + " sản phẩm \"" + sp.getTenSanPham()
                + "\" không? Lịch sử giao dịch được giữ nguyên.");

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                if (sanPhamController.capNhatTrangThaiSanPham(sp.getMaSanPham(), trangThaiMoi)) {
                    taiDanhSach();
                } else {
                    hienThiLoi("Không thể cập nhật trạng thái sản phẩm.");
                }
            } catch (Exception ex) {
                hienThiLoi("Lỗi cập nhật trạng thái: " + ex.getMessage());
            }
        }
    }

    private void xuLyXoa() {
        SanPham sp = tblDanhSach.getSelectionModel().getSelectedItem();
        if (sp == null) return;

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận xóa sản phẩm");
        xacNhan.setHeaderText(null);
        xacNhan.setContentText("Xóa hẳn sản phẩm \"" + sp.getTenSanPham()
                + "\"? Chỉ xóa được khi tồn kho bằng 0 và chưa phát sinh giao dịch.");

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                if (sanPhamController.xoaSanPham(sp.getMaSanPham())) {
                    taiDanhSach();
                } else {
                    hienThiLoi("Không thể xóa sản phẩm.");
                }
            } catch (Exception ex) {
                hienThiLoi("Không thể xóa sản phẩm: " + ex.getMessage());
            }
        }
    }

    private String dinhDangTien(BigDecimal soTien) {
        if (soTien == null) return "0 đ";
        NumberFormat nf = NumberFormat.getNumberInstance(LOCALE_VI_VN);
        nf.setMaximumFractionDigits(0);
        return nf.format(soTien) + " đ";
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        if (getScene() != null && getScene().getWindow() != null) {
            alert.initOwner(getScene().getWindow());
        }
        alert.showAndWait();
    }
}
