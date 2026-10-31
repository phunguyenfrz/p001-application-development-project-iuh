package com.petshop.view.nhanvien;

import com.petshop.controller.NhanVienController;
import com.petshop.model.NhanVien;
import javafx.collections.FXCollections;
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
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

/**
 * Hộp thoại (Modal Dialog) Thêm mới nhân viên vào hệ thống Happy Tails.
 * Tuân thủ thiết kế và ràng buộc nghiệp vụ:
 * - Họ tên: Cho phép chữ cái (kể cả tiếng Việt), số và khoảng trắng; chặn hoàn toàn ký tự đặc biệt (!@#$%^&*...) thời gian thực.
 * - Số điện thoại: Ràng buộc nhập số thời gian thực, chỉ cho phép số và tối đa đúng 10 chữ số (bắt đầu bằng 0).
 * - Ngày sinh: Chuẩn định dạng dd/MM/yyyy, tự động tính tuổi, nhân viên phải từ đủ 18 tuổi trở lên.
 * - Chức vụ: Quản lý, Bán hàng, Chăm sóc.
 * - Mã nhân viên: Sinh tự động NV-yyyyMMdd-xxxxxx và chỉ đọc.
 */
public class ThemNhanVienView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NhanVienController controller = new NhanVienController();
    private final Runnable callbackSauKhiLuu;

    // Các thành phần nhập liệu
    private final TextField txtMaNhanVien = new TextField();
    private final TextField txtHoTen = new TextField();
    private final TextField txtSoDienThoai = new TextField();
    private final ToggleGroup grpGioiTinh = new ToggleGroup();
    private final RadioButton rdoNam = new RadioButton("Nam");
    private final RadioButton rdoNu = new RadioButton("Nữ");
    private final DatePicker dpNgaySinh = new DatePicker();
    private final Label lblTuoi = new Label("Tuổi: 20");
    private final ComboBox<String> cboChucVu = new ComboBox<>(FXCollections.observableArrayList("Quản lý", "Bán hàng", "Chăm sóc"));

    // Nút bấm tác vụ
    private final Button btnLuu = new Button("Lưu nhân viên");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

    public ThemNhanVienView(Runnable callbackSauKhiLuu) {
        this.callbackSauKhiLuu = callbackSauKhiLuu;

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề hộp thoại
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM MỚI NHÂN VIÊN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Nhập đầy đủ thông tin nhân viên. Các trường đánh dấu (*) là bắt buộc.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung form nhập liệu
        VBox pnlForm = taoKhungNhapLieu();

        // 3. Thanh nút bấm
        HBox pnlNut = taoThanhNutBam();

        getChildren().addAll(pnlTieuDe, pnlForm, pnlNut);

        // Khởi tạo form ban đầu
        lamMoiForm();
    }

    /**
     * Khởi tạo giao diện lưới (GridPane) chứa các nhãn và ô nhập liệu thông tin nhân viên.
     */
    private VBox taoKhungNhapLieu() {
        VBox pnlKhung = new VBox(14);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        // Mã nhân viên sinh tự động, chỉ đọc
        txtMaNhanVien.setEditable(false);

        // Placeholder hướng dẫn người dùng
        txtHoTen.setPromptText("Nhập họ và tên đầy đủ (VD: Nguyễn Văn An)");
        txtSoDienThoai.setPromptText("Nhập 10 chữ số bắt đầu bằng 0 (VD: 0912345678)");

        // =====================================================================
        // RÀNG BUỘC HỌ TÊN: Dùng TextFormatter chặn ký tự đặc biệt (!@#$%^&*...) thời gian thực
        // =====================================================================
        UnaryOperator<TextFormatter.Change> hoTenFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[\\p{L}0-9\\s]*")) {
                return change;
            }
            return null; // Không cho phép nhập ký tự đặc biệt như @, #, $, %...
        };
        txtHoTen.setTextFormatter(new TextFormatter<>(hoTenFilter));

        // =====================================================================
        // RÀNG BUỘC SỐ ĐIỆN THOẠI: Dùng TextFormatter chỉ cho phép nhập số và tối đa 10 chữ số
        // =====================================================================
        UnaryOperator<TextFormatter.Change> sdtFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,10}")) {
                return change;
            }
            return null; // Không cho phép nhập chữ hoặc vượt quá 10 chữ số
        };
        txtSoDienThoai.setTextFormatter(new TextFormatter<>(sdtFilter));

        // Giới tính
        rdoNam.setToggleGroup(grpGioiTinh);
        rdoNu.setToggleGroup(grpGioiTinh);
        rdoNam.setSelected(true);
        HBox boxGioiTinh = new HBox(16, rdoNam, rdoNu);
        boxGioiTinh.setAlignment(Pos.CENTER_LEFT);

        // =====================================================================
        // NGÀY SINH: Chuẩn dd/MM/yyyy và tự động hiển thị tuổi
        // =====================================================================
        ganDinhDangNgay(dpNgaySinh);
        lblTuoi.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        dpNgaySinh.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int t = Period.between(newVal, LocalDate.now()).getYears();
                lblTuoi.setText("Tuổi: " + t + (t < 18 ? " (Chưa đủ 18 tuổi!)" : ""));
                lblTuoi.setTextFill(t < 18 ? Color.RED : Color.web("#475569"));
            }
        });
        HBox boxNgaySinh = new HBox(10, dpNgaySinh, lblTuoi);
        boxNgaySinh.setAlignment(Pos.CENTER_LEFT);

        // Chức vụ
        cboChucVu.setValue("Bán hàng");

        pnlLuoi.add(new Label("Mã nhân viên:"), 0, 0);
        pnlLuoi.add(txtMaNhanVien, 1, 0);

        pnlLuoi.add(new Label("Họ và tên (*):"), 0, 1);
        pnlLuoi.add(txtHoTen, 1, 1);

        pnlLuoi.add(new Label("Số điện thoại (*):"), 0, 2);
        pnlLuoi.add(txtSoDienThoai, 1, 2);

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

        pnlKhung.getChildren().add(pnlLuoi);
        return pnlKhung;
    }

    /**
     * Tạo thanh nút chức năng (Lưu, Làm mới, Đóng) ở chân hộp thoại.
     */
    private HBox taoThanhNutBam() {
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> xuLyLuuNhanVien());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiForm());

        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().addAll(btnLuu, btnLamMoi, btnDong);
        return pnlNut;
    }

    /**
     * Xử lý lưu hồ sơ nhân viên mới vào CSDL.
     */
    private void xuLyLuuNhanVien() {
        try {
            String hoTen = txtHoTen.getText() != null ? txtHoTen.getText().trim() : "";
            String sdt = txtSoDienThoai.getText() != null ? txtSoDienThoai.getText().trim() : "";
            LocalDate ns = dpNgaySinh.getValue();
            boolean gioiTinh = rdoNam.isSelected();
            String cv = cboChucVu.getValue();

            if (hoTen.isEmpty()) {
                hienThiCanhBao("Dữ liệu không hợp lệ", "Họ và tên nhân viên không được để trống!");
                txtHoTen.requestFocus();
                return;
            }

            if (sdt.isEmpty() || !sdt.matches("^0[0-9]{9}$")) {
                hienThiCanhBao("Dữ liệu không hợp lệ", "Số điện thoại phải gồm đúng 10 chữ số và bắt đầu bằng số 0 (VD: 0912345678)!");
                txtSoDienThoai.requestFocus();
                return;
            }

            if (ns == null) {
                hienThiCanhBao("Dữ liệu không hợp lệ", "Vui lòng chọn ngày sinh của nhân viên!");
                return;
            }

            int tuoi = Period.between(ns, LocalDate.now()).getYears();
            if (tuoi < 18) {
                hienThiCanhBao("Dữ liệu không hợp lệ", "Nhân viên phải từ đủ 18 tuổi trở lên (Tuổi hiện tại: " + tuoi + ")!");
                return;
            }

            NhanVien nv = new NhanVien();
            nv.setMaNhanVien(txtMaNhanVien.getText());
            nv.setHoTen(hoTen);
            nv.setSoDienThoai(sdt);
            nv.setGioiTinh(gioiTinh);
            nv.setNgaySinh(ns);
            nv.setChucVu(cv);
            nv.setTrangThai("Đang làm");

            boolean thanhCong = controller.themNhanVien(nv);
            if (thanhCong) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã thêm mới nhân viên '" + hoTen + "' (" + nv.getMaNhanVien() + ") vào hệ thống!");
                alert.showAndWait();

                if (callbackSauKhiLuu != null) {
                    callbackSauKhiLuu.run();
                }
                dongCuaSo();
            } else {
                hienThiLoi("Lỗi khi thêm", "Không thể thêm nhân viên mới vào cơ sở dữ liệu!");
            }
        } catch (IllegalArgumentException ex) {
            hienThiCanhBao("Dữ liệu không hợp lệ", ex.getMessage());
        } catch (Exception ex) {
            hienThiLoi("Lỗi hệ thống", ex.getMessage());
        }
    }

    /**
     * Làm mới form và tự động sinh mã nhân viên mới.
     */
    private void lamMoiForm() {
        try {
            txtMaNhanVien.setText(controller.taoMaNhanVienMoi());
        } catch (Exception e) {
            txtMaNhanVien.setText("NV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-000001");
        }
        txtHoTen.clear();
        txtSoDienThoai.clear();
        rdoNam.setSelected(true);
        cboChucVu.setValue("Bán hàng");

        LocalDate macDinh = LocalDate.now().minusYears(20);
        dpNgaySinh.setValue(macDinh);
        dpNgaySinh.getEditor().setText(DINH_DANG_NGAY.format(macDinh));
        lblTuoi.setText("Tuổi: 20");
        lblTuoi.setTextFill(Color.web("#475569"));

        txtHoTen.requestFocus();
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

    /**
     * Đóng cửa sổ Stage chứa form này.
     */
    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
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

    /**
     * Phương thức tĩnh tiện ích mở nhanh hộp thoại Thêm mới nhân viên dạng modal dialog.
     */
    public static void hienThi(Window cuaSoCha, Runnable callbackSauKhiLuu) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (cuaSoCha != null) {
            dlg.initOwner(cuaSoCha);
        }
        dlg.setTitle("Thêm nhân viên mới");

        ThemNhanVienView pnlNoiDung = new ThemNhanVienView(callbackSauKhiLuu);
        Scene scene = new Scene(pnlNoiDung, 520, 480);

        if (ThemNhanVienView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(ThemNhanVienView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        dlg.setScene(scene);
        dlg.setResizable(false);
        dlg.showAndWait();
    }
}

