package com.petshop.view.sanpham;

import com.petshop.controller.SanPhamController;
import com.petshop.model.NhaCungCap;
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
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình danh sách sản phẩm tích hợp thanh tác vụ Thêm mới (mở màn nhỏ), Cập nhật (mở màn nhỏ), Xóa mềm và Làm mới.
 * Thiết kế giao diện đồng bộ hoàn toàn theo cách bố trí của phân hệ Dịch vụ (CapNhatDichVuView).
 */
public class CapNhatSanPhamView extends VBox {

    private final SanPhamController sanPhamController = new SanPhamController();

    private final Button btnThemMoi = new Button("Thêm mới");
    private final Button btnCapNhat = new Button("Cập nhật");
    private final Button btnXoa = new Button("Xóa");
    private final Button btnLamMoi = new Button("Làm mới");

    private final TableView<SanPham> tblDanhSach = new TableView<>();

    public CapNhatSanPhamView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("SẢN PHẨM - CẬP NHẬT SẢN PHẨM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Bấm Thêm mới để mở cửa sổ thêm sản phẩm; nhấp chọn một dòng trong bảng để Cập nhật hoặc Xóa.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeMan, pnlBang);

        capNhatTrangThaiNut(null);
        taiLaiBang();
    }

    // Tạo thanh nút phía trên bảng: Thêm mới (xanh), Cập nhật, Xóa (đỏ), Làm mới
    private HBox taoThanhTacVu() {
        HBox pnlThanhNut = new HBox(10);
        pnlThanhNut.getStyleClass().add("thanh-tac-vu");
        pnlThanhNut.setAlignment(Pos.CENTER_LEFT);

        Label lblDanhSach = new Label("Danh sách sản phẩm trong kho");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        btnThemMoi.getStyleClass().add("nut-chinh");
        btnThemMoi.setOnAction(e -> moCuaSoThemMoi());

        btnCapNhat.getStyleClass().add("nut-phu");
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());

        btnXoa.getStyleClass().add("nut-xoa");
        btnXoa.setOnAction(e -> xuLyXoaMem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xuLyLamMoi());

        pnlThanhNut.getChildren().addAll(lblDanhSach, pnlKhoangTrong, btnThemMoi, btnCapNhat, btnXoa, btnLamMoi);
        return pnlThanhNut;
    }

    private VBox taoKhungBangDanhSach() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        HBox pnlThanhNut = taoThanhTacVu();

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có sản phẩm nào trong cơ sở dữ liệu"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<SanPham, String> colMa = new TableColumn<>("Mã sản phẩm");
        colMa.setPrefWidth(140);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaSanPham()));

        TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
        colTen.setPrefWidth(220);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenSanPham()));

        TableColumn<SanPham, String> colLoai = new TableColumn<>("Loại / ĐVT");
        colLoai.setPrefWidth(90);
        colLoai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonViTinh()));

        TableColumn<SanPham, String> colGiaBan = new TableColumn<>("Giá bán");
        colGiaBan.setPrefWidth(120);
        colGiaBan.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGiaBan.setCellValueFactory(cell -> new SimpleStringProperty(
                ThemSanPhamView.dinhDangTien(cell.getValue().getGiaBan().doubleValue())
        ));

        TableColumn<SanPham, String> colTonKho = new TableColumn<>("Tồn kho");
        colTonKho.setPrefWidth(90);
        colTonKho.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTonKho.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getSoLuongTon())));

        TableColumn<SanPham, String> colNguong = new TableColumn<>("Ngưỡng an toàn");
        colNguong.setPrefWidth(110);
        colNguong.setStyle("-fx-alignment: CENTER-RIGHT;");
        colNguong.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getNguongTonToiThieu())));

        TableColumn<SanPham, String> colNhaCungCap = new TableColumn<>("Nhà cung cấp");
        colNhaCungCap.setPrefWidth(180);
        colNhaCungCap.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTenNhaCungCap() != null ? cell.getValue().getTenNhaCungCap() : "Chưa xác định"
        ));

        TableColumn<SanPham, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh"
        ));

        tblDanhSach.getColumns().setAll(List.of(
                colMa, colTen, colLoai, colGiaBan, colTonKho, colNguong, colNhaCungCap, colTrangThai
        ));

        // Làm mờ các dòng đã Ngừng kinh doanh và cho phép nhấp đúp để mở màn nhỏ Cập nhật
        tblDanhSach.setRowFactory(tv -> {
            TableRow<SanPham> row = new TableRow<>() {
                @Override
                protected void updateItem(SanPham item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setOpacity(1.0);
                    } else {
                        setOpacity(item.isTrangThai() ? 1.0 : 0.5);
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moCuaSoCapNhat();
                }
            });
            return row;
        });

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener(
                (obs, cu, moi) -> capNhatTrangThaiNut(moi)
        );

        pnlKhung.getChildren().addAll(pnlThanhNut, tblDanhSach);
        return pnlKhung;
    }

    public void taiLaiBang() {
        try {
            List<SanPham> ds = sanPhamController.layDanhSachSanPham();
            tblDanhSach.setItems(FXCollections.observableArrayList(ds));
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiCanhBao("Lỗi tải dữ liệu: " + ex.getMessage());
        }
    }

    private void xuLyLamMoi() {
        taiLaiBang();
        capNhatTrangThaiNut(null);
    }

    // Bật/tắt nút Cập nhật và Xóa tùy theo dòng đang chọn trong bảng
    private void capNhatTrangThaiNut(SanPham spChon) {
        if (spChon == null) {
            btnCapNhat.setDisable(true);
            btnXoa.setDisable(true);
            return;
        }
        btnCapNhat.setDisable(false);
        btnXoa.setDisable(!spChon.isTrangThai());
    }

    // Mở cửa sổ nhỏ ThemSanPhamView riêng biệt, đóng lại thì tải lại bảng
    private void moCuaSoThemMoi() {
        Stage dlgThem = new Stage();
        dlgThem.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlgThem.initOwner(getScene().getWindow());
        }
        dlgThem.setTitle("Thêm sản phẩm mới");

        ThemSanPhamView manThem = new ThemSanPhamView(this::taiLaiBang);
        Scene scene = new Scene(manThem, 560, 480);
        ganCssChung(scene);

        dlgThem.setScene(scene);
        dlgThem.setResizable(false);
        dlgThem.showAndWait();
    }

    // Mở cửa sổ nhỏ cập nhật cho dòng sản phẩm đang chọn trong bảng
    private void moCuaSoCapNhat() {
        SanPham spChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (spChon == null) {
            hienThiLoi("Vui lòng chọn một sản phẩm trong bảng để cập nhật!");
            return;
        }

        Stage dlgCapNhat = new Stage();
        dlgCapNhat.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlgCapNhat.initOwner(getScene().getWindow());
        }
        dlgCapNhat.setTitle("Cập nhật thông tin sản phẩm");

        VBox pnlGoc = taoNoiDungCuaSoCapNhat(spChon, dlgCapNhat);
        Scene scene = new Scene(pnlGoc, 560, 520);
        ganCssChung(scene);

        dlgCapNhat.setScene(scene);
        dlgCapNhat.setResizable(false);
        dlgCapNhat.showAndWait();
    }

    // Dựng nội dung form trong cửa sổ nhỏ Cập nhật sản phẩm
    private VBox taoNoiDungCuaSoCapNhat(SanPham spChon, Stage dlgCapNhat) {
        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("CẬP NHẬT SẢN PHẨM");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblDangSua = new Label("Đang sửa: " + spChon.getMaSanPham() + " - " + spChon.getTenSanPham());
        lblDangSua.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #0369a1;");

        VBox pnlForm = new VBox(14);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        TextField txtTenSanPham = new TextField(spChon.getTenSanPham());

        ComboBox<String> cboLoaiSanPham = new ComboBox<>(
                FXCollections.observableArrayList("Lọ", "Gói", "Hộp", "Chai", "Túi", "Cái")
        );
        if (spChon.getDonViTinh() != null && !spChon.getDonViTinh().isBlank()) {
            if (!cboLoaiSanPham.getItems().contains(spChon.getDonViTinh())) {
                cboLoaiSanPham.getItems().add(spChon.getDonViTinh());
            }
            cboLoaiSanPham.setValue(spChon.getDonViTinh());
        } else {
            cboLoaiSanPham.setValue("Lọ");
        }
        cboLoaiSanPham.setMaxWidth(Double.MAX_VALUE);

        ComboBox<NhaCungCap> cboNcc = new ComboBox<>();
        cboNcc.setPromptText("-- Chọn nhà cung cấp --");
        cboNcc.setMaxWidth(Double.MAX_VALUE);
        try {
            List<NhaCungCap> dsNcc = sanPhamController.layDanhSachNhaCungCap();
            cboNcc.getItems().setAll(dsNcc);
            if (spChon.getMaNhaCungCap() != null) {
                for (NhaCungCap ncc : dsNcc) {
                    if (ncc.getMaNhaCungCap().equalsIgnoreCase(spChon.getMaNhaCungCap())) {
                        cboNcc.setValue(ncc);
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            cboNcc.getItems().clear();
        }

        TextField txtGiaBan = new TextField(ThemSanPhamView.dinhDangTien(spChon.getGiaBan().doubleValue()));
        TextField txtSoLuongTon = new TextField(String.valueOf(spChon.getSoLuongTon()));
        TextField txtNguongTon = new TextField(String.valueOf(spChon.getNguongTonToiThieu()));

        ComboBox<String> cboTrangThai = new ComboBox<>(
                FXCollections.observableArrayList("Đang kinh doanh", "Ngừng kinh doanh")
        );
        cboTrangThai.setValue(spChon.isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh");
        cboTrangThai.setMaxWidth(Double.MAX_VALUE);

        chanNhapSoNguyen(txtSoLuongTon);
        chanNhapSoNguyen(txtNguongTon);
        ganDinhDangTienChoO(txtGiaBan);

        pnlLuoi.add(new Label("Mã sản phẩm:"), 0, 0);
        Label lblMaSp = new Label(spChon.getMaSanPham());
        lblMaSp.setStyle("-fx-font-weight: bold;");
        pnlLuoi.add(lblMaSp, 1, 0);

        pnlLuoi.add(new Label("Tên sản phẩm (*):"), 0, 1);
        pnlLuoi.add(txtTenSanPham, 1, 1);

        pnlLuoi.add(new Label("Loại sản phẩm (*):"), 0, 2);
        pnlLuoi.add(cboLoaiSanPham, 1, 2);

        pnlLuoi.add(new Label("Nhà cung cấp:"), 0, 3);
        pnlLuoi.add(cboNcc, 1, 3);

        pnlLuoi.add(new Label("Giá bán (VNĐ) (*):"), 0, 4);
        pnlLuoi.add(txtGiaBan, 1, 4);

        pnlLuoi.add(new Label("Số lượng tồn:"), 0, 5);
        pnlLuoi.add(txtSoLuongTon, 1, 5);

        pnlLuoi.add(new Label("Ngưỡng an toàn (*):"), 0, 6);
        pnlLuoi.add(txtNguongTon, 1, 6);

        pnlLuoi.add(new Label("Trạng thái:"), 0, 7);
        pnlLuoi.add(cboTrangThai, 1, 7);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(170);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dlgCapNhat.close());

        Button btnLuu = new Button("Lưu cập nhật");
        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> xuLyLuuCapNhat(spChon, dlgCapNhat, txtTenSanPham, cboLoaiSanPham, cboNcc, txtGiaBan, txtSoLuongTon, txtNguongTon, cboTrangThai));

        pnlNut.getChildren().addAll(btnDong, btnLuu);
        pnlForm.getChildren().addAll(pnlLuoi, pnlNut);
        pnlGoc.getChildren().addAll(lblTieuDe, lblDangSua, pnlForm);
        return pnlGoc;
    }

    private void xuLyLuuCapNhat(SanPham spChon, Stage dlgCapNhat,
                                TextField txtTen, ComboBox<String> cboLoai, ComboBox<NhaCungCap> cboNcc,
                                TextField txtGia, TextField txtTon, TextField txtNguong, ComboBox<String> cboTrangThai) {
        String ten = txtTen.getText() == null ? "" : txtTen.getText().trim();
        if (ten.isEmpty()) {
            hienThiCanhBao("Tên sản phẩm không được để trống!");
            return;
        }

        String loaiSp = cboLoai.getValue();
        if (loaiSp == null || loaiSp.isBlank()) {
            hienThiCanhBao("Vui lòng chọn loại sản phẩm!");
            return;
        }

        String chuoiGia = ThemSanPhamView.boKyTuDinhDangTien(txtGia.getText());
        if (chuoiGia.isEmpty()) {
            hienThiCanhBao("Giá bán không được để trống!");
            return;
        }

        BigDecimal giaBan;
        try {
            giaBan = new BigDecimal(chuoiGia);
            if (giaBan.compareTo(BigDecimal.ZERO) < 0) {
                hienThiCanhBao("Giá bán phải lớn hơn hoặc bằng 0!");
                return;
            }
        } catch (NumberFormatException ex) {
            hienThiCanhBao("Giá bán không hợp lệ!");
            return;
        }

        int soLuongTon;
        try {
            soLuongTon = Integer.parseInt(txtTon.getText().trim());
            if (soLuongTon < 0) {
                hienThiCanhBao("Số lượng tồn không được âm!");
                return;
            }
        } catch (NumberFormatException ex) {
            hienThiCanhBao("Số lượng tồn phải là số nguyên!");
            return;
        }

        int nguongTon;
        try {
            nguongTon = Integer.parseInt(txtNguong.getText().trim());
            if (nguongTon < 0) {
                hienThiCanhBao("Ngưỡng an toàn không được âm!");
                return;
            }
        } catch (NumberFormatException ex) {
            hienThiCanhBao("Ngưỡng an toàn phải là số nguyên!");
            return;
        }

        try {
            spChon.setTenSanPham(ten);
            spChon.setDonViTinh(loaiSp);
            spChon.setGiaBan(giaBan);
            spChon.setSoLuongTon(soLuongTon);
            spChon.setNguongTonToiThieu(nguongTon);

            NhaCungCap nccChon = cboNcc.getValue();
            spChon.setMaNhaCungCap(nccChon != null ? nccChon.getMaNhaCungCap() : null);
            spChon.setTenNhaCungCap(nccChon != null ? nccChon.getTenNhaCungCap() : null);

            boolean dangKinhDoanh = "Đang kinh doanh".equals(cboTrangThai.getValue());
            spChon.setTrangThai(dangKinhDoanh);

            boolean thanhCong = sanPhamController.capNhatSanPham(spChon);
            if (thanhCong) {
                hienThiThongBao("Cập nhật thông tin sản phẩm thành công!");
                taiLaiBang();
                dlgCapNhat.close();
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            hienThiCanhBao("Dữ liệu không hợp lệ: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            hienThiLoi("Lỗi cơ sở dữ liệu: " + ex.getMessage());
        }
    }

    private void xuLyXoaMem() {
        SanPham sp = tblDanhSach.getSelectionModel().getSelectedItem();
        if (sp == null) return;

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận ngừng kinh doanh");
        xacNhan.setHeaderText(null);
        xacNhan.setContentText("Bạn có chắc chắn muốn chuyển sản phẩm \"" + sp.getTenSanPham() + "\" sang trạng thái Ngừng kinh doanh không?");

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                boolean thanhCong = sanPhamController.xoaMemSanPham(sp.getMaSanPham());
                if (thanhCong) {
                    hienThiThongBao("Đã chuyển sản phẩm sang ngừng kinh doanh!");
                    taiLaiBang();
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                hienThiCanhBao("Lỗi: " + ex.getMessage());
            }
        }
    }

    private void chanNhapSoNguyen(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*")) {
                txt.setText(cu);
            }
        });
    }

    private void ganDinhDangTienChoO(TextField txt) {
        txt.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("[\\d.\\sđ]*")) {
                txt.setText(cu);
            }
        });
        txt.focusedProperty().addListener((obs, cu, dangChon) -> {
            if (!dangChon) {
                String chuoi = ThemSanPhamView.boKyTuDinhDangTien(txt.getText());
                if (!chuoi.isEmpty()) {
                    try {
                        double soTien = Double.parseDouble(chuoi);
                        if (soTien >= 0) {
                            txt.setText(ThemSanPhamView.dinhDangTien(soTien));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        });
    }

    private void ganCssChung(Scene scene) {
        if (scene == null) return;
        URL css = getClass().getResource("/com/petshop/view/css/chung.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    private void hienThiThongBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Dữ liệu không hợp lệ");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
