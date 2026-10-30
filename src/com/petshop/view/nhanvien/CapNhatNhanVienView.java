package com.petshop.view.nhanvien;

import com.petshop.controller.NhanVienController;
import com.petshop.model.NhanVien;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình Quản lý & Cập nhật thông tin nhân viên (EMP_CRUD).
 * Thiết kế giao diện và ràng buộc nghiệp vụ (form + real-time rules) đồng bộ hoàn toàn với CapNhatKhachHangView:
 * - Họ và tên: Chặn hoàn toàn ký tự đặc biệt (!@#$%^&*...) trong thời gian thực, cho phép chữ cái tiếng Việt, số và khoảng trắng.
 * - Số điện thoại: Ràng buộc nhập số thời gian thực, đúng 10 chữ số và bắt đầu bằng số 0.
 * - Ngày sinh: Định dạng chuẩn dd/MM/yyyy và kiểm tra tuổi tối thiểu >= 18.
 * - Mã nhân viên: Sinh tự động chuẩn NV-yyyyMMdd-xxxxxx và ở chế độ chỉ đọc.
 */
public class CapNhatNhanVienView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NhanVienController controller = new NhanVienController();
    private final ObservableList<NhanVien> danhSachHienThi = FXCollections.observableArrayList();

    // Các nút chức năng trên thanh tác vụ
    private final Button btnThem = new Button("Thêm mới");
    private final Button btnCapNhat = new Button("Cập nhật");
    private final Button btnNgung = new Button("Ngừng làm việc");
    private final Button btnLamMoi = new Button("Làm mới");

    // Bảng dữ liệu
    private final TableView<NhanVien> tblDanhSach = new TableView<>();

    public CapNhatNhanVienView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề phân hệ
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("QUẢN LÝ NHÂN VIÊN - CẬP NHẬT THÔNG TIN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Thêm mới, điều chỉnh hồ sơ nhân sự và quản lý trạng thái làm việc (xóa mềm).");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung bảng dữ liệu kèm thanh tác vụ
        VBox pnlBangDuLieu = taoKhungBang();
        VBox.setVgrow(pnlBangDuLieu, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, pnlBangDuLieu);

        // Cập nhật trạng thái nút và tải dữ liệu ban đầu
        capNhatTrangThaiNut(null);
        taiLaiBang();
    }

    private VBox taoKhungBang() {
        VBox khung = new VBox(10);
        khung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(khung, Priority.ALWAYS);

        // Thanh công cụ tác vụ
        HBox thanhTacVu = new HBox(10);
        thanhTacVu.getStyleClass().add("thanh-tac-vu");
        thanhTacVu.setAlignment(Pos.CENTER_LEFT);

        Label lblDanhSach = new Label("Danh sách nhân viên");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        Region trong = new Region();
        HBox.setHgrow(trong, Priority.ALWAYS);

        btnThem.getStyleClass().add("nut-chinh");
        btnThem.setOnAction(e -> moCuaSoThem());

        btnCapNhat.getStyleClass().add("nut-phu");
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());

        btnNgung.getStyleClass().add("nut-phu");
        btnNgung.setOnAction(e -> xuLyNgungLamViec());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiLaiBang());

        thanhTacVu.getChildren().addAll(lblDanhSach, trong, btnThem, btnCapNhat, btnNgung, btnLamMoi);

        // Cấu hình bảng hiển thị
        caiDatCacCotBang();
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        khung.getChildren().addAll(thanhTacVu, tblDanhSach);
        return khung;
    }

    private void caiDatCacCotBang() {
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có dữ liệu nhân viên nào trong hệ thống"));
        tblDanhSach.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // 1. Mã nhân viên
        TableColumn<NhanVien, String> colMaNv = new TableColumn<>("Mã nhân viên");
        colMaNv.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaNhanVien()));
        colMaNv.setPrefWidth(160);
        colMaNv.setStyle("-fx-alignment: CENTER;");

        // 2. Họ và tên
        TableColumn<NhanVien, String> colHoTen = new TableColumn<>("Họ và tên");
        colHoTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getHoTen()));
        colHoTen.setPrefWidth(200);

        // 3. Số điện thoại
        TableColumn<NhanVien, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSoDienThoai()));
        colSdt.setPrefWidth(130);
        colSdt.setStyle("-fx-alignment: CENTER;");

        // 4. Giới tính
        TableColumn<NhanVien, String> colGioiTinh = new TableColumn<>("Giới tính");
        colGioiTinh.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenGioiTinh()));
        colGioiTinh.setPrefWidth(90);
        colGioiTinh.setStyle("-fx-alignment: CENTER;");

        // 5. Ngày sinh
        TableColumn<NhanVien, String> colNgaySinh = new TableColumn<>("Ngày sinh");
        colNgaySinh.setCellValueFactory(cell -> {
            LocalDate ns = cell.getValue().getNgaySinh();
            return new SimpleStringProperty(ns != null ? ns.format(DINH_DANG_NGAY) : "");
        });
        colNgaySinh.setPrefWidth(110);
        colNgaySinh.setStyle("-fx-alignment: CENTER;");

        // 6. Tuổi
        TableColumn<NhanVien, Number> colTuoi = new TableColumn<>("Tuổi");
        colTuoi.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getTuoi()));
        colTuoi.setPrefWidth(70);
        colTuoi.setStyle("-fx-alignment: CENTER;");

        // 7. Chức vụ
        TableColumn<NhanVien, String> colChucVu = new TableColumn<>("Chức vụ");
        colChucVu.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getChucVu()));
        colChucVu.setPrefWidth(120);
        colChucVu.setStyle("-fx-alignment: CENTER;");

        // 8. Trạng thái làm việc
        TableColumn<NhanVien, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colTrangThai.setPrefWidth(130);
        colTrangThai.setStyle("-fx-alignment: CENTER;");

        tblDanhSach.getColumns().addAll(
                colMaNv, colHoTen, colSdt, colGioiTinh, colNgaySinh, colTuoi, colChucVu, colTrangThai
        );

        // Hiệu ứng làm mờ dòng nhân viên đã nghỉ việc + Double click mở popup sửa
        tblDanhSach.setRowFactory(tv -> {
            TableRow<NhanVien> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldVal, nv) -> {
                if (nv != null && !nv.isDangLam()) {
                    row.setOpacity(0.55);
                } else {
                    row.setOpacity(1.0);
                }
            });

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moCuaSoCapNhat();
                }
            });
            return row;
        });

        // Theo dõi chọn dòng
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, nv) -> capNhatTrangThaiNut(nv));
        tblDanhSach.setItems(danhSachHienThi);
    }

    private void capNhatTrangThaiNut(NhanVien nv) {
        boolean coChon = (nv != null);
        btnCapNhat.setDisable(!coChon);
        btnNgung.setDisable(!coChon);

        if (coChon) {
            if (nv.isDangLam()) {
                btnNgung.setText("Ngừng làm việc");
                btnNgung.getStyleClass().removeAll("nut-thanh-cong");
                btnNgung.getStyleClass().add("nut-nguy-hiem");
            } else {
                btnNgung.setText("Khôi phục làm việc");
                btnNgung.getStyleClass().removeAll("nut-nguy-hiem");
                btnNgung.getStyleClass().add("nut-thanh-cong");
            }
        } else {
            btnNgung.setText("Ngừng làm việc");
        }
    }

    public void taiLaiBang() {
        try {
            List<NhanVien> ds = controller.layDanhSachNhanVien();
            danhSachHienThi.setAll(ds);
            tblDanhSach.getSelectionModel().clearSelection();
            capNhatTrangThaiNut(null);
        } catch (Exception e) {
            hienThiLoi("Lỗi tải danh sách nhân viên", e.getMessage());
        }
    }

    // =========================================================================
    // HỘP THOẠI THÊM MỚI NHÂN VIÊN
    // =========================================================================

    private void moCuaSoThem() {
        ThemNhanVienView.hienThi(getScene() != null ? getScene().getWindow() : null, this::taiLaiBang);
    }

    // =========================================================================
    // HỘP THOẠI CẬP NHẬT THÔNG TIN NHÂN VIÊN
    // =========================================================================

    private void moCuaSoCapNhat() {
        NhanVien chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Chưa chọn nhân viên", "Vui lòng chọn một nhân viên trên bảng để tiến hành cập nhật!");
            return;
        }

        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        Window owner = getScene() != null ? getScene().getWindow() : null;
        if (owner != null) cuaSo.initOwner(owner);
        cuaSo.setTitle("Cập nhật thông tin nhân viên - " + chon.getMaNhanVien());

        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // Tiêu đề form cập nhật
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT THÔNG TIN NHÂN VIÊN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblDangSua = new Label("Đang chỉnh sửa: " + chon.getMaNhanVien() + " - " + chon.getHoTen());
        lblDangSua.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0284c7;");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblDangSua);

        // Khung nhập liệu hop-noi-dung
        VBox pnlForm = new VBox(14);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        TextField txtMaNv = new TextField(chon.getMaNhanVien());
        txtMaNv.setEditable(false);

        TextField txtHoTen = new TextField(chon.getHoTen());
        TextField txtSdt = new TextField(chon.getSoDienThoai());

        // --- RÀNG BUỘC HỌ VÀ TÊN: Dùng TextFormatter chặn ký tự đặc biệt (!@#$%^&*...) thời gian thực ---
        txtHoTen.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("[\\p{L}0-9\\s]*")) {
                return change;
            }
            return null;
        }));

        // --- RÀNG BUỘC SỐ ĐIỆN THOẠI: Dùng TextFormatter chỉ cho phép nhập số và tối đa 10 chữ số ---
        txtSdt.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d{0,10}")) {
                return change;
            }
            return null;
        }));

        // Giới tính Radio
        ToggleGroup grpGioiTinh = new ToggleGroup();
        RadioButton rdoNam = new RadioButton("Nam");
        RadioButton rdoNu = new RadioButton("Nữ");
        rdoNam.setToggleGroup(grpGioiTinh);
        rdoNu.setToggleGroup(grpGioiTinh);
        if (chon.isGioiTinh()) rdoNam.setSelected(true);
        else rdoNu.setSelected(true);
        HBox boxGioiTinh = new HBox(16, rdoNam, rdoNu);
        boxGioiTinh.setAlignment(Pos.CENTER_LEFT);

        // Ngày sinh DatePicker chuẩn dd/MM/yyyy
        DatePicker dpNgaySinh = new DatePicker(chon.getNgaySinh());
        ganDinhDangNgay(dpNgaySinh);
        if (chon.getNgaySinh() != null) {
            dpNgaySinh.getEditor().setText(DINH_DANG_NGAY.format(chon.getNgaySinh()));
        }
        Label lblTuoi = new Label("Tuổi: " + chon.getTuoi());
        lblTuoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        dpNgaySinh.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int t = java.time.Period.between(newVal, LocalDate.now()).getYears();
                lblTuoi.setText("Tuổi: " + t + (t < 18 ? " (Chưa đủ 18 tuổi!)" : ""));
                lblTuoi.setTextFill(t < 18 ? Color.RED : Color.web("#475569"));
            }
        });
        HBox boxNgaySinh = new HBox(10, dpNgaySinh, lblTuoi);
        boxNgaySinh.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cboChucVu = new ComboBox<>(FXCollections.observableArrayList("Quản lý", "Bán hàng", "Chăm sóc"));
        cboChucVu.setValue(chon.getChucVu());

        pnlLuoi.add(new Label("Mã nhân viên:"), 0, 0);
        pnlLuoi.add(txtMaNv, 1, 0);

        pnlLuoi.add(new Label("Họ và tên (*):"), 0, 1);
        pnlLuoi.add(txtHoTen, 1, 1);

        pnlLuoi.add(new Label("Số điện thoại (*):"), 0, 2);
        pnlLuoi.add(txtSdt, 1, 2);

        pnlLuoi.add(new Label("Giới tính:"), 0, 3);
        pnlLuoi.add(boxGioiTinh, 1, 3);

        pnlLuoi.add(new Label("Ngày sinh (*):"), 0, 4);
        pnlLuoi.add(boxNgaySinh, 1, 4);

        pnlLuoi.add(new Label("Chức vụ (*):"), 0, 5);
        pnlLuoi.add(cboChucVu, 1, 5);

        ColumnConstraints colNhan = new ColumnConstraints(120);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        // Nút điều khiển
        HBox pnlNutDieuKhien = new HBox(10);
        pnlNutDieuKhien.setAlignment(Pos.CENTER_RIGHT);

        Button btnLuu = new Button("Lưu thay đổi");
        btnLuu.getStyleClass().add("nut-chinh");

        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> cuaSo.close());

        btnLuu.setOnAction(e -> {
            try {
                String hoTen = txtHoTen.getText() != null ? txtHoTen.getText().trim() : "";
                String sdt = txtSdt.getText() != null ? txtSdt.getText().trim() : "";
                LocalDate ns = dpNgaySinh.getValue();
                boolean gioiTinh = rdoNam.isSelected();
                String cv = cboChucVu.getValue();
                String tt = chon.getTrangThai();

                if (hoTen.isEmpty()) {
                    hienThiCanhBao("Dữ liệu không hợp lệ", "Họ và tên nhân viên không được để trống!");
                    txtHoTen.requestFocus();
                    return;
                }
                if (sdt.isEmpty() || !sdt.matches("^0[0-9]{9}$")) {
                    hienThiCanhBao("Dữ liệu không hợp lệ", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0 (VD: 0912345678)!");
                    txtSdt.requestFocus();
                    return;
                }
                if (ns == null) {
                    hienThiCanhBao("Dữ liệu không hợp lệ", "Vui lòng chọn ngày sinh của nhân viên!");
                    return;
                }
                int tuoi = java.time.Period.between(ns, LocalDate.now()).getYears();
                if (tuoi < 18) {
                    hienThiCanhBao("Dữ liệu không hợp lệ", "Nhân viên phải từ đủ 18 tuổi trở lên (Tuổi hiện tại: " + tuoi + ")!");
                    return;
                }

                NhanVien nvMoi = new NhanVien(chon.getMaNhanVien(), hoTen, sdt, cv, ns, gioiTinh, tt);
                boolean ok = controller.capNhatNhanVien(nvMoi);
                if (ok) {
                    cuaSo.close();
                    taiLaiBang();
                    hienThiThongBao("Thành công", "Đã cập nhật thông tin nhân viên '" + hoTen + "' (" + chon.getMaNhanVien() + ") thành công!");
                } else {
                    hienThiLoi("Lỗi khi cập nhật", "Không thể lưu thay đổi vào cơ sở dữ liệu!");
                }
            } catch (IllegalArgumentException ex) {
                hienThiCanhBao("Dữ liệu không hợp lệ", ex.getMessage());
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi cập nhật", ex.getMessage());
            }
        });

        pnlNutDieuKhien.getChildren().addAll(btnLuu, btnHuy);
        pnlForm.getChildren().addAll(pnlLuoi, pnlNutDieuKhien);
        pnlGoc.getChildren().addAll(pnlTieuDe, pnlForm);

        Scene scene = new Scene(pnlGoc, 500, 460);
        if (CapNhatNhanVienView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(CapNhatNhanVienView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        cuaSo.setScene(scene);
        cuaSo.setResizable(false);
        cuaSo.showAndWait();
    }

    // =========================================================================
    // XỬ LÝ NGỪNG LÀM VIỆC (XÓA MỀM) HOẶC KHÔI PHỤC
    // =========================================================================

    private void xuLyNgungLamViec() {
        NhanVien chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Chưa chọn nhân viên", "Vui lòng chọn một nhân viên trong bảng!");
            return;
        }

        if (chon.isDangLam()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận ngừng làm việc");
            confirm.setHeaderText("Ngừng làm việc đối với nhân viên: " + chon.getHoTen() + " (" + chon.getMaNhanVien() + ")");
            confirm.setContentText("Hành động này sẽ chuyển trạng thái nhân viên thành 'Đã nghỉ việc' (Xóa mềm).\nBạn có chắc chắn muốn thực hiện?");

            Optional<ButtonType> opt = confirm.showAndWait();
            if (opt.isPresent() && opt.get() == ButtonType.OK) {
                try {
                    boolean ok = controller.ngungLamViec(chon.getMaNhanVien());
                    if (ok) {
                        taiLaiBang();
                        hienThiThongBao("Thành công", "Đã chuyển trạng thái nhân viên sang 'Đã nghỉ việc'!");
                    }
                } catch (Exception ex) {
                    hienThiLoi("Lỗi khi ngừng làm việc", ex.getMessage());
                }
            }
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận khôi phục nhân viên");
            confirm.setHeaderText("Khôi phục trạng thái làm việc cho: " + chon.getHoTen());
            confirm.setContentText("Chuyển trạng thái nhân viên thành 'Đang làm'?");

            Optional<ButtonType> opt = confirm.showAndWait();
            if (opt.isPresent() && opt.get() == ButtonType.OK) {
                try {
                    boolean ok = controller.khoiPhucLamViec(chon.getMaNhanVien(), true);
                    if (ok) {
                        taiLaiBang();
                        hienThiThongBao("Thành công", "Đã khôi phục nhân viên về trạng thái 'Đang làm'!");
                    }
                } catch (Exception ex) {
                    hienThiLoi("Lỗi khi khôi phục", ex.getMessage());
                }
            }
        }
    }

    /**
     * Helper thiết lập định dạng hiển thị dd/MM/yyyy cho DatePicker
     */
    private void ganDinhDangNgay(DatePicker datePicker) {
        datePicker.setPromptText("dd/MM/yyyy");
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DINH_DANG_NGAY.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    try {
                        return LocalDate.parse(string.trim(), DINH_DANG_NGAY);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            }
        });

        // Đảm bảo editor luôn đồng bộ định dạng dd/MM/yyyy khi mất tiêu điểm
        datePicker.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                String text = datePicker.getEditor().getText();
                if (text != null && !text.trim().isEmpty()) {
                    try {
                        LocalDate parsed = LocalDate.parse(text.trim(), DINH_DANG_NGAY);
                        datePicker.setValue(parsed);
                        datePicker.getEditor().setText(DINH_DANG_NGAY.format(parsed));
                    } catch (Exception e) {
                        if (datePicker.getValue() != null) {
                            datePicker.getEditor().setText(DINH_DANG_NGAY.format(datePicker.getValue()));
                        }
                    }
                }
            }
        });
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
