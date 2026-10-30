package com.petshop.view.thucung;

import com.petshop.controller.KhachHangController;
import com.petshop.controller.ThuCungController;
import com.petshop.model.KhachHang;
import com.petshop.model.ThuCung;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình phụ (Popup) dùng để thêm mới hồ sơ thú cưng vào hệ thống Happy Tails.
 * Hỗ trợ chọn chủ nuôi từ danh sách khách hàng, giới hạn cân nặng kiểu số và ngày sinh không ở tương lai.
 */
public class ThemThuCungView extends VBox {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ThuCungController thuCungController = new ThuCungController();
    private final KhachHangController khachHangController = new KhachHangController();
    private final Runnable callbackSauKhiLuu;

    // Các thành phần nhập liệu
    private final TextField txtMaThuCung = new TextField();
    private final TextField txtTenThuCung = new TextField();
    private final ComboBox<String> cboLoai = new ComboBox<>();
    private final TextField txtLoaiKhac = new TextField();
    private final TextField txtGiongLoai = new TextField();
    private final ComboBox<String> cboGioiTinh = new ComboBox<>();
    private final TextField txtCanNang = new TextField();
    private final DatePicker dpNgaySinh = new DatePicker();
    private final TextArea txtDiUng = new TextArea();

    // Chọn chủ nuôi
    private final ComboBox<KhachHang> cboKhachHang = new ComboBox<>();
    private final TextField txtLocKhachHang = new TextField();
    private final ObservableList<KhachHang> dsKhachHangGoc = FXCollections.observableArrayList();
    private FilteredList<KhachHang> dsKhachHangLoc;

    // Các nút bấm
    private final Button btnLuu = new Button("Lưu thú cưng");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

    public ThemThuCungView() {
        this(null, null);
    }

    public ThemThuCungView(Runnable callbackSauKhiLuu) {
        this(null, callbackSauKhiLuu);
    }

    public ThemThuCungView(String maKhachHangMacDinh, Runnable callbackSauKhiLuu) {
        this.callbackSauKhiLuu = callbackSauKhiLuu;

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề hộp thoại
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÊM MỚI HỒ SƠ THÚ CƯNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Nhập thông tin hồ sơ thú cưng và chọn khách hàng sở hữu. Các trường (*) là bắt buộc.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Tải danh sách khách hàng để chọn chủ nuôi
        napDanhSachKhachHang(maKhachHangMacDinh);

        // 3. Khung form nhập liệu
        VBox pnlForm = taoKhungNhapLieu();

        // 4. Thanh nút bấm
        HBox pnlNut = taoThanhNutBam();

        getChildren().addAll(pnlTieuDe, pnlForm, pnlNut);

        // Khởi tạo mã tự động ban đầu
        lamMoiForm();
    }

    /**
     * Nạp dữ liệu danh sách khách hàng từ cơ sở dữ liệu để đưa vào ComboBox chọn chủ nuôi.
     */
    private void napDanhSachKhachHang(String maKhachHangMacDinh) {
        try {
            List<KhachHang> ds = khachHangController.layDanhSachKhachHang();
            dsKhachHangGoc.setAll(ds);
            dsKhachHangLoc = new FilteredList<>(dsKhachHangGoc, p -> true);
            cboKhachHang.setItems(dsKhachHangLoc);

            cboKhachHang.setConverter(new StringConverter<>() {
                @Override
                public String toString(KhachHang kh) {
                    if (kh == null) return "";
                    return kh.getMaKhachHang() + " - " + kh.getHoTen() + " (" + kh.getSoDienThoai() + ")";
                }

                @Override
                public KhachHang fromString(String string) {
                    return null;
                }
            });

            // Chọn mặc định nếu có truyền vào
            if (maKhachHangMacDinh != null) {
                for (KhachHang kh : ds) {
                    if (maKhachHangMacDinh.equalsIgnoreCase(kh.getMaKhachHang())) {
                        cboKhachHang.setValue(kh);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi nạp danh sách khách hàng cho thú cưng: " + e.getMessage());
        }
    }

    /**
     * Khởi tạo giao diện lưới (GridPane) chứa các nhãn và ô nhập liệu thông tin thú cưng.
     */
    private VBox taoKhungNhapLieu() {
        VBox pnlKhung = new VBox(14);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        // Mã thú cưng sinh tự động, chỉ đọc
        txtMaThuCung.setEditable(false);

        // Tên thú cưng
        txtTenThuCung.setPromptText("Nhập tên thú cưng (VD: Mochi, Bông, Lu, Corgi...)");

        // Chọn chủ nuôi kèm ô lọc nhanh
        txtLocKhachHang.setPromptText("Gõ tên, SĐT hoặc mã KH để lọc nhanh...");
        txtLocKhachHang.textProperty().addListener((obs, cu, moi) -> {
            if (dsKhachHangLoc != null) {
                String tuKhoa = (moi != null) ? moi.trim().toLowerCase() : "";
                dsKhachHangLoc.setPredicate(kh -> {
                    if (tuKhoa.isEmpty()) return true;
                    return kh.getHoTen().toLowerCase().contains(tuKhoa)
                            || kh.getSoDienThoai().contains(tuKhoa)
                            || kh.getMaKhachHang().toLowerCase().contains(tuKhoa);
                });
            }
        });
        cboKhachHang.setPromptText("-- Chọn khách hàng chủ nuôi (*) --");
        cboKhachHang.setMaxWidth(Double.MAX_VALUE);

        VBox boxChuNuoi = new VBox(4, txtLocKhachHang, cboKhachHang);

        // Loài thú cưng
        cboLoai.setItems(FXCollections.observableArrayList("Chó", "Mèo", "Thỏ", "Hamster", "Chim", "Khác"));
        cboLoai.setValue("Chó");
        cboLoai.setMaxWidth(Double.MAX_VALUE);

        // Ô nhập loài cụ thể khi chọn "Khác"
        txtLoaiKhac.setPromptText("Nhập tên loài thú cưng cụ thể (VD: Rùa, Sóc, Nhím, Kỳ nhông...)");
        txtLoaiKhac.setVisible(false);
        txtLoaiKhac.setManaged(false);

        cboLoai.valueProperty().addListener((obs, cu, moi) -> {
            boolean laKhac = "Khác".equalsIgnoreCase(moi);
            txtLoaiKhac.setVisible(laKhac);
            txtLoaiKhac.setManaged(laKhac);
            if (laKhac) {
                txtLoaiKhac.requestFocus();
            }
        });

        VBox boxLoai = new VBox(4, cboLoai, txtLoaiKhac);

        // Giống loài
        txtGiongLoai.setPromptText("VD: Poodle Tiny, Mèo Anh lông ngắn, Corgi...");

        // Giới tính
        cboGioiTinh.setItems(FXCollections.observableArrayList("Đực", "Cái"));
        cboGioiTinh.setValue("Đực");
        cboGioiTinh.setMaxWidth(Double.MAX_VALUE);

        // Cân nặng (kg) - Tùy chọn (để trống tự động nhận 3.0 kg, có thể cập nhật sau tại quầy)
        txtCanNang.setPromptText("Tùy chọn (để trống = 3.0 kg, cập nhật sau tại quầy)");
        txtCanNang.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d{0,2})?")) {
                txtCanNang.setText(cu != null ? cu : "");
            }
        });

        // Ngày sinh - DatePicker chặn ngày tương lai
        dpNgaySinh.setPromptText("dd/MM/yyyy (tùy chọn)");
        dpNgaySinh.setMaxWidth(Double.MAX_VALUE);
        dpNgaySinh.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8;");
                }
            }
        });

        // Tiền sử dị ứng / Lưu ý đặc biệt
        txtDiUng.setPromptText("Ghi chú về tiền sử bệnh lý, dị ứng thức ăn/sữa tắm, tính cách của thú cưng...");
        txtDiUng.setPrefRowCount(3);
        txtDiUng.setWrapText(true);

        // Đưa các thành phần vào lưới
        pnlLuoi.add(new Label("Mã thú cưng:"), 0, 0);
        pnlLuoi.add(txtMaThuCung, 1, 0);

        pnlLuoi.add(new Label("Chủ nuôi (*):"), 0, 1);
        pnlLuoi.add(boxChuNuoi, 1, 1);

        pnlLuoi.add(new Label("Tên thú cưng (*):"), 0, 2);
        pnlLuoi.add(txtTenThuCung, 1, 2);

        pnlLuoi.add(new Label("Loài thú cưng:"), 0, 3);
        pnlLuoi.add(boxLoai, 1, 3);

        pnlLuoi.add(new Label("Giống loài:"), 0, 4);
        pnlLuoi.add(txtGiongLoai, 1, 4);

        pnlLuoi.add(new Label("Giới tính (*):"), 0, 5);
        pnlLuoi.add(cboGioiTinh, 1, 5);

        pnlLuoi.add(new Label("Cân nặng (kg):"), 0, 6);
        pnlLuoi.add(txtCanNang, 1, 6);

        pnlLuoi.add(new Label("Ngày sinh:"), 0, 7);
        pnlLuoi.add(dpNgaySinh, 1, 7);

        pnlLuoi.add(new Label("Dị ứng / Ghi chú:"), 0, 8);
        pnlLuoi.add(txtDiUng, 1, 8);

        ColumnConstraints colNhan = new ColumnConstraints(130);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        pnlKhung.getChildren().add(pnlLuoi);
        return pnlKhung;
    }

    /**
     * Khởi tạo thanh nút bấm điều khiển form (Lưu, Làm mới, Đóng).
     */
    private HBox taoThanhNutBam() {
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> xuLyLuuThuCung());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiForm());

        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().addAll(btnLuu, btnLamMoi, btnDong);
        return pnlNut;
    }

    /**
     * Thu thập dữ liệu từ form và gọi ThuCungController để lưu vào CSDL.
     */
    private void xuLyLuuThuCung() {
        try {
            KhachHang chuNuoi = cboKhachHang.getValue();
            if (chuNuoi == null) {
                hienThiCanhBao("Chưa chọn chủ nuôi", "Vui lòng chọn khách hàng chủ nuôi cho thú cưng!");
                cboKhachHang.requestFocus();
                return;
            }

            String ten = txtTenThuCung.getText() != null ? txtTenThuCung.getText().trim() : "";
            if (ten.isEmpty()) {
                hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập tên cho thú cưng!");
                txtTenThuCung.requestFocus();
                return;
            }

            String canNangStr = txtCanNang.getText() != null ? txtCanNang.getText().trim() : "";
            BigDecimal canNang;
            if (canNangStr.isEmpty()) {
                // Tùy chọn khi tiếp nhận nhanh: mặc định 3.0 kg để đáp ứng CSDL và tính giá tạm tính
                canNang = BigDecimal.valueOf(3.0);
            } else {
                try {
                    canNang = new BigDecimal(canNangStr);
                    if (canNang.compareTo(BigDecimal.ZERO) <= 0) {
                        hienThiCanhBao("Cân nặng không hợp lệ", "Cân nặng thú cưng phải lớn hơn 0 kg!");
                        txtCanNang.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    hienThiCanhBao("Cân nặng không hợp lệ", "Cân nặng phải là định dạng số hợp lệ (VD: 3.5)!");
                    txtCanNang.requestFocus();
                    return;
                }
            }

            LocalDate ngaySinh = dpNgaySinh.getValue();
            if (ngaySinh != null && ngaySinh.isAfter(LocalDate.now())) {
                hienThiCanhBao("Ngày sinh không hợp lệ", "Ngày sinh của thú cưng không được ở tương lai!");
                dpNgaySinh.requestFocus();
                return;
            }

            String loai = cboLoai.getValue();
            if ("Khác".equalsIgnoreCase(loai)) {
                String loaiNhap = txtLoaiKhac.getText() != null ? txtLoaiKhac.getText().trim() : "";
                if (loaiNhap.isEmpty()) {
                    hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập loài thú cưng cụ thể khi chọn 'Khác'!");
                    txtLoaiKhac.requestFocus();
                    return;
                }
                loai = loaiNhap;
            }

            ThuCung tc = new ThuCung();
            tc.setMaThuCung(txtMaThuCung.getText().trim());
            tc.setMaKhachHang(chuNuoi.getMaKhachHang());
            tc.setTenThuCung(ten);
            tc.setLoaiThuCung(loai);
            tc.setGiongLoai(txtGiongLoai.getText() != null ? txtGiongLoai.getText().trim() : "");
            tc.setGioiTinh(cboGioiTinh.getValue());
            tc.setCanNang(canNang);
            tc.setNgaySinh(ngaySinh);
            tc.setDiUng(txtDiUng.getText() != null ? txtDiUng.getText().trim() : "");

            boolean thanhCong = thuCungController.themThuCung(tc);
            if (thanhCong) {
                hienThiThongBao("Thành công", "Đã thêm mới hồ sơ thú cưng '" + tc.getTenThuCung() +
                        "' (" + tc.getMaThuCung() + ") cho chủ nuôi '" + chuNuoi.getHoTen() + "' thành công!");

                if (callbackSauKhiLuu != null) {
                    callbackSauKhiLuu.run();
                }
                dongCuaSo();
            }
        } catch (IllegalArgumentException ex) {
            hienThiCanhBao("Dữ liệu không hợp lệ", ex.getMessage());
        } catch (Exception ex) {
            hienThiLoi("Lỗi lưu thú cưng", ex.getMessage());
        }
    }

    /**
     * Làm mới toàn bộ ô nhập liệu và sinh mã thú cưng mới.
     */
    public void lamMoiForm() {
        try {
            txtMaThuCung.setText(thuCungController.taoMaThuCungMoi());
        } catch (Exception e) {
            txtMaThuCung.setText("TC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-000001");
        }

        txtTenThuCung.clear();
        cboLoai.setValue("Chó");
        txtLoaiKhac.clear();
        txtLoaiKhac.setVisible(false);
        txtLoaiKhac.setManaged(false);
        txtGiongLoai.clear();
        cboGioiTinh.setValue("Đực");
        txtCanNang.clear();
        dpNgaySinh.setValue(null);
        txtDiUng.clear();
        txtLocKhachHang.clear();

        txtTenThuCung.requestFocus();
    }

    /**
     * Đóng cửa sổ Stage hiện tại.
     */
    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() != null) {
            ((Stage) getScene().getWindow()).close();
        }
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

