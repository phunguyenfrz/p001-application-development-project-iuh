package com.petshop.view.donnhap;

import com.petshop.controller.DonNhapHangController;
import com.petshop.controller.SanPhamController;
import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;
import com.petshop.model.SanPham;
import com.petshop.model.TaiKhoan;
import com.petshop.util.SessionManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Giao diện lập đơn nhập hàng (IMP_CREATE).
 * Cho phép nhân viên tạo đơn nhập mới với thông tin nhà cung cấp,
 * danh sách sản phẩm, số lượng và đơn giá nhập.
 */
public class LapDonNhapView extends VBox {

    // Định dạng số tiền
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0");

    // Controller
    private final DonNhapHangController donHangController = new DonNhapHangController();

    // --- Phần thông tin đơn hàng ---
    private final TextField txtMaDon = new TextField();
    private final DatePicker dpNgayLap = new DatePicker(LocalDate.now());
    private final Spinner<Integer> spGio = new Spinner<>(0, 23, LocalTime.now().getHour());
    private final Spinner<Integer> spPhut = new Spinner<>(0, 59, LocalTime.now().getMinute());
    private final Spinner<Integer> spGiay = new Spinner<>(0, 59, LocalTime.now().getSecond());
    private final TextField txtNhaCungCap = new TextField();
    private final TextField txtNhanVien = new TextField();

    // Nhà cung cấp được ghi nhận riêng theo từng sản phẩm để khi lưu tự tách đơn.
    private final Map<String, String> nhaCungCapTheoSanPham = new LinkedHashMap<>();
    private final Map<String, String> tenNhaCungCapTheoSanPham = new LinkedHashMap<>();

    // --- Bảng chi tiết sản phẩm ---
    private final TableView<ChiTietDonNhap> bangChiTiet = new TableView<>();
    private final ObservableList<ChiTietDonNhap> danhSachChiTiet = FXCollections.observableArrayList();

    // --- Tổng tiền ---
    private final Label lblTongTien = new Label("Tổng tiền: 0");

    /**
     * Khởi tạo giao diện lập đơn nhập hàng.
     */
    public LapDonNhapView() {
        getStyleClass().add("khung-phan-he");
        setSpacing(15);
        setPadding(new Insets(20));

        // Tải CSS
        try {
            getStylesheets().add(getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm());
        } catch (Exception ignored) {
        }

        // Tiêu đề
        Label tieuDe = new Label("LẬP ĐƠN NHẬP HÀNG");
        tieuDe.getStyleClass().add("tieu-de-phan-he");

        Label moTa = new Label("Chọn sản phẩm cần nhập; nhà cung cấp sẽ được lấy tự động từ sản phẩm.");
        moTa.getStyleClass().add("mo-ta-phan-he");
        moTa.setWrapText(true);

        // Phần form thông tin
        VBox hopThongTin = taoFormThongTin();

        // Phần bảng sản phẩm
        VBox hopBang = taoHopBangChiTiet();
        VBox.setVgrow(hopBang, Priority.ALWAYS);

        // Phần tổng tiền
        HBox hopTongTien = taoHopTongTien();

        // Phần nút hành động
        HBox hopNut = taoHopNutHanhDong();

        getChildren().addAll(tieuDe, moTa, hopThongTin, hopBang, hopTongTien, hopNut);

        // Khởi tạo dữ liệu mặc định
        khoiTaoDuLieuMacDinh();
    }

    /**
     * Tạo form nhập thông tin đơn hàng.
     */
    private VBox taoFormThongTin() {
        VBox hopThongTin = new VBox(12);
        hopThongTin.getStyleClass().add("hop-noi-dung");
        hopThongTin.setPadding(new Insets(15));

        // Hàng 1: Mã đơn + Ngày lập
        HBox hang1 = new HBox(20);
        hang1.setAlignment(Pos.CENTER_LEFT);

        // Mã đơn (tự sinh, chỉ đọc)
        VBox hopMaDon = new VBox(5);
        Label lblMaDon = new Label("Mã đơn nhập:");
        lblMaDon.setStyle("-fx-font-weight: bold;");
        txtMaDon.setEditable(false);
        txtMaDon.setPrefWidth(220);
        txtMaDon.setPromptText("Tự sinh theo nhà cung cấp sau khi lưu");
        txtMaDon.setStyle("-fx-background-color: #f0f0f0;");
        hopMaDon.getChildren().addAll(lblMaDon, txtMaDon);
        HBox.setHgrow(hopMaDon, Priority.ALWAYS);

        // Ngày lập
        VBox hopNgayLap = new VBox(5);
        Label lblNgayLap = new Label("Ngày lập:");
        lblNgayLap.setStyle("-fx-font-weight: bold;");
        dpNgayLap.setPrefWidth(220);
        hopNgayLap.getChildren().addAll(lblNgayLap, dpNgayLap);
        HBox.setHgrow(hopNgayLap, Priority.ALWAYS);

        hang1.getChildren().addAll(hopMaDon, hopNgayLap);

        // Hàng 2: Nhà cung cấp + Nhân viên lập
        HBox hang2 = new HBox(20);
        hang2.setAlignment(Pos.CENTER_LEFT);

        // Nhà cung cấp được tự điền khi chọn sản phẩm.
        VBox hopNCC = new VBox(5);
        Label lblNCC = new Label("Nhà cung cấp (tự động):");
        lblNCC.setStyle("-fx-font-weight: bold;");
        txtNhaCungCap.setEditable(false);
        txtNhaCungCap.setMaxWidth(Double.MAX_VALUE);
        txtNhaCungCap.setPromptText("Tự tách đơn theo nhà cung cấp của từng sản phẩm");
        txtNhaCungCap.setStyle("-fx-background-color: #f0f0f0;");
        hopNCC.getChildren().addAll(lblNCC, txtNhaCungCap);
        HBox.setHgrow(hopNCC, Priority.ALWAYS);

        // Nhân viên lập
        VBox hopNhanVien = new VBox(5);
        Label lblNhanVien = new Label("Nhân viên lập:");
        lblNhanVien.setStyle("-fx-font-weight: bold;");
        txtNhanVien.setEditable(false);
        txtNhanVien.setPrefWidth(220);
        txtNhanVien.setStyle("-fx-background-color: #f0f0f0;");
        hopNhanVien.getChildren().addAll(lblNhanVien, txtNhanVien);
        HBox.setHgrow(hopNhanVien, Priority.ALWAYS);

        hang2.getChildren().addAll(hopNCC, hopNhanVien);

        Label lblThoiGian = new Label("Thời gian lập (giờ : phút : giây):");
        lblThoiGian.setStyle("-fx-font-weight: bold;");
        HBox hangThoiGian = new HBox(6, spGio, new Label(":"), spPhut, new Label(":"), spGiay);
        hangThoiGian.setAlignment(Pos.CENTER_LEFT);
        for (Spinner<Integer> spinner : List.of(spGio, spPhut, spGiay)) {
            spinner.setEditable(false);
            spinner.setPrefWidth(78);
            spinner.getValueFactory().setConverter(new StringConverter<>() {
                @Override
                public String toString(Integer value) {
                    return value == null ? "00" : String.format("%02d", value);
                }

                @Override
                public Integer fromString(String value) {
                    return Integer.parseInt(value.trim());
                }
            });
        }
        VBox hopThoiGian = new VBox(5, lblThoiGian, hangThoiGian);

        hopThongTin.getChildren().addAll(hang1, hang2, hopThoiGian);
        return hopThongTin;
    }

    /**
     * Tạo hộp chứa bảng chi tiết sản phẩm và nút thêm sản phẩm.
     */
    @SuppressWarnings("unchecked")
    private VBox taoHopBangChiTiet() {
        VBox hopBang = new VBox(10);
        VBox.setVgrow(hopBang, Priority.ALWAYS);

        // Thanh tiêu đề + nút thêm sản phẩm
        HBox thanhTieuDe = new HBox(10);
        thanhTieuDe.setAlignment(Pos.CENTER_LEFT);

        Label tieuDeBang = new Label("Danh sách sản phẩm nhập");
        tieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        Button btnThemSP = new Button("Thêm sản phẩm");
        btnThemSP.getStyleClass().add("nut-chinh");
        btnThemSP.setOnAction(e -> xuLyThemSanPham());

        thanhTieuDe.getChildren().addAll(tieuDeBang, khoangTrong, btnThemSP);

        // Khởi tạo bảng
        bangChiTiet.getStyleClass().add("bang-du-lieu");
        bangChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        bangChiTiet.setPlaceholder(new Label("Chưa có sản phẩm nào. Nhấn \"Thêm sản phẩm\" để bắt đầu."));
        bangChiTiet.setItems(danhSachChiTiet);
        bangChiTiet.setEditable(true);
        VBox.setVgrow(bangChiTiet, Priority.ALWAYS);

        // Cột STT
        TableColumn<ChiTietDonNhap, Number> cotSTT = new TableColumn<>("STT");
        cotSTT.setMinWidth(50);
        cotSTT.setMaxWidth(60);
        cotSTT.setStyle("-fx-alignment: CENTER;");
        cotSTT.setSortable(false);
        cotSTT.setCellValueFactory(cellData -> {
            int index = bangChiTiet.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(index);
        });

        // Cột mã sản phẩm
        TableColumn<ChiTietDonNhap, String> cotMaSP = new TableColumn<>("Mã SP");
        cotMaSP.setMinWidth(130);
        cotMaSP.setCellValueFactory(new PropertyValueFactory<>("maSanPham"));

        // Cột tên sản phẩm
        TableColumn<ChiTietDonNhap, String> cotTenSP = new TableColumn<>("Tên sản phẩm");
        cotTenSP.setMinWidth(180);
        cotTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));

        TableColumn<ChiTietDonNhap, String> cotNhaCungCap = new TableColumn<>("Nhà cung cấp");
        cotNhaCungCap.setMinWidth(150);
        cotNhaCungCap.setCellValueFactory(cellData -> {
            String ten = tenNhaCungCapTheoSanPham.get(cellData.getValue().getMaSanPham());
            return new SimpleStringProperty(ten == null || ten.isBlank() ? "Chưa gán NCC" : ten);
        });

        // Cột đơn vị tính
        TableColumn<ChiTietDonNhap, String> cotDVT = new TableColumn<>("Đơn vị tính");
        cotDVT.setMinWidth(80);
        cotDVT.setStyle("-fx-alignment: CENTER;");
        cotDVT.setCellValueFactory(new PropertyValueFactory<>("donViTinh"));

        // Cột số lượng (chỉnh sửa bằng Spinner)
        TableColumn<ChiTietDonNhap, Integer> cotSoLuong = new TableColumn<>("Số lượng");
        cotSoLuong.setMinWidth(100);
        cotSoLuong.setStyle("-fx-alignment: CENTER;");
        cotSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        cotSoLuong.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, Integer.MAX_VALUE, 1);

            {
                spinner.setEditable(true);
                spinner.setPrefWidth(90);
                // Chỉ cho phép nhập số
                spinner.getEditor().textProperty().addListener((obs, cu, moi) -> {
                    if (moi != null && !moi.matches("\\d*")) {
                        spinner.getEditor().setText(cu);
                    }
                });
                spinner.valueProperty().addListener((obs, cu, moi) -> {
                    if (moi != null && getTableRow() != null && getTableRow().getItem() != null) {
                        getTableRow().getItem().setSoLuong(moi);
                        capNhatTongTien();
                        bangChiTiet.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    spinner.getValueFactory().setValue(item);
                    setGraphic(spinner);
                }
            }
        });

        // Cột đơn giá (chỉnh sửa bằng TextField)
        TableColumn<ChiTietDonNhap, BigDecimal> cotDonGia = new TableColumn<>("Đơn giá");
        cotDonGia.setMinWidth(130);
        cotDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        cotDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        cotDonGia.setCellFactory(col -> new TableCell<>() {
            private final TextField txtGia = new TextField();

            {
                txtGia.setPrefWidth(110);
                // Chỉ cho phép nhập số
                txtGia.textProperty().addListener((obs, cu, moi) -> {
                    if (moi != null && !moi.matches("\\d*")) {
                        txtGia.setText(cu);
                    }
                });
                txtGia.focusedProperty().addListener((obs, cu, moi) -> {
                    if (!moi && getTableRow() != null && getTableRow().getItem() != null) {
                        // Khi mất focus, cập nhật đơn giá
                        capNhatDonGia(getTableRow().getItem(), txtGia.getText());
                    }
                });
                txtGia.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        capNhatDonGia(getTableRow().getItem(), txtGia.getText());
                    }
                });
            }

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    txtGia.setText(item.toBigInteger().toString());
                    setGraphic(txtGia);
                }
            }
        });

        // Cột thành tiền
        TableColumn<ChiTietDonNhap, BigDecimal> cotThanhTien = new TableColumn<>("Thành tiền");
        cotThanhTien.setMinWidth(130);
        cotThanhTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        cotThanhTien.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getThanhTien()));
        cotThanhTien.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DINH_DANG_TIEN.format(item));
                }
            }
        });

        // Cột nút xóa
        TableColumn<ChiTietDonNhap, Void> cotXoa = new TableColumn<>("Xóa");
        cotXoa.setMinWidth(70);
        cotXoa.setMaxWidth(80);
        cotXoa.setStyle("-fx-alignment: CENTER;");
        cotXoa.setSortable(false);
        cotXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");

            {
                btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btnXoa.setOnAction(e -> {
                    ChiTietDonNhap ct = getTableView().getItems().get(getIndex());
                    danhSachChiTiet.remove(ct);
                    nhaCungCapTheoSanPham.remove(ct.getMaSanPham());
                    tenNhaCungCapTheoSanPham.remove(ct.getMaSanPham());
                    capNhatNhaCungCapHienThi();
                    capNhatTongTien();
                    bangChiTiet.refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnXoa);
                }
            }
        });

        bangChiTiet.getColumns().addAll(cotSTT, cotMaSP, cotTenSP, cotNhaCungCap, cotDVT, cotSoLuong, cotDonGia, cotThanhTien, cotXoa);

        hopBang.getChildren().addAll(thanhTieuDe, bangChiTiet);
        return hopBang;
    }

    /**
     * Cập nhật đơn giá từ TextField.
     */
    private void capNhatDonGia(ChiTietDonNhap chiTiet, String giaText) {
        try {
            if (giaText == null || giaText.trim().isEmpty()) {
                chiTiet.setDonGia(BigDecimal.ZERO);
            } else {
                chiTiet.setDonGia(new BigDecimal(giaText.trim()));
            }
        } catch (NumberFormatException ex) {
            chiTiet.setDonGia(BigDecimal.ZERO);
        }
        capNhatTongTien();
        bangChiTiet.refresh();
    }

    /**
     * Tạo hộp hiển thị tổng tiền.
     */
    private HBox taoHopTongTien() {
        HBox hopTongTien = new HBox();
        hopTongTien.setAlignment(Pos.CENTER_RIGHT);
        hopTongTien.setPadding(new Insets(5, 10, 5, 10));

        lblTongTien.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        hopTongTien.getChildren().add(lblTongTien);
        return hopTongTien;
    }

    /**
     * Tạo hộp chứa các nút hành động (Lưu đơn nhập, Làm mới).
     */
    private HBox taoHopNutHanhDong() {
        HBox hopNut = new HBox(12);
        hopNut.setAlignment(Pos.CENTER_RIGHT);
        hopNut.setPadding(new Insets(10, 0, 0, 0));

        Button btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiForm());

        Button btnLuuDon = new Button("Lưu đơn nhập");
        btnLuuDon.getStyleClass().add("nut-chinh");
        btnLuuDon.setOnAction(e -> xuLyLuuDonNhap());

        hopNut.getChildren().addAll(btnLamMoi, btnLuuDon);
        return hopNut;
    }

    /**
     * Khởi tạo dữ liệu mặc định cho form.
     */
    private void khoiTaoDuLieuMacDinh() {
        txtMaDon.setText("Tự sinh khi lưu (mỗi NCC một mã)");

        // Hiển thị nhân viên đang đăng nhập
        TaiKhoan taiKhoan = SessionManager.getTaiKhoanHienTai();
        if (taiKhoan != null) {
            String hoTen = taiKhoan.getHoTenNhanVien();
            String maNV = taiKhoan.getMaNhanVien();
            txtNhanVien.setText((hoTen != null ? hoTen : "") + " (" + (maNV != null ? maNV : "") + ")");
        }

        // Ngày lập mặc định hôm nay
        dpNgayLap.setValue(LocalDate.now());
        LocalTime gioHienTai = LocalTime.now();
        spGio.getValueFactory().setValue(gioHienTai.getHour());
        spPhut.getValueFactory().setValue(gioHienTai.getMinute());
        spGiay.getValueFactory().setValue(gioHienTai.getSecond());
    }

    // ========================= XỬ LÝ SỰ KIỆN =========================

    /**
     * Mở dialog chọn sản phẩm và thêm vào bảng chi tiết.
     */
    private void xuLyThemSanPham() {
        Window owner = getScene() != null ? getScene().getWindow() : null;
        ChonSanPhamDialog dialog = new ChonSanPhamDialog(owner);
        dialog.showAndWait();

        SanPham sanPhamDuocChon = dialog.getSanPhamDuocChon();
        if (sanPhamDuocChon == null) {
            return;
        }

        // Kiểm tra sản phẩm đã tồn tại trong bảng chưa
        for (ChiTietDonNhap ct : danhSachChiTiet) {
            if (ct.getMaSanPham().equals(sanPhamDuocChon.getMaSanPham())) {
                hienThiLoi("Sản phẩm \"" + sanPhamDuocChon.getTenSanPham()
                        + "\" đã có trong danh sách. Vui lòng chỉnh sửa số lượng trực tiếp trên bảng.");
                return;
            }
        }

        // Tạo chi tiết đơn hàng mới
        ChiTietDonNhap chiTietMoi = new ChiTietDonNhap();
        chiTietMoi.setMaSanPham(sanPhamDuocChon.getMaSanPham());
        chiTietMoi.setTenSanPham(sanPhamDuocChon.getTenSanPham());
        chiTietMoi.setDonViTinh(sanPhamDuocChon.getDonViTinh());
        chiTietMoi.setSoLuong(1);
        // Đơn giá nhập mặc định lấy giá bán làm gợi ý
        chiTietMoi.setDonGia(sanPhamDuocChon.getGiaBan() != null ? sanPhamDuocChon.getGiaBan() : BigDecimal.ZERO);

        danhSachChiTiet.add(chiTietMoi);
        nhaCungCapTheoSanPham.put(sanPhamDuocChon.getMaSanPham(), sanPhamDuocChon.getMaNhaCungCap());
        tenNhaCungCapTheoSanPham.put(sanPhamDuocChon.getMaSanPham(), sanPhamDuocChon.getTenNhaCungCap() != null
                ? sanPhamDuocChon.getTenNhaCungCap()
                : (sanPhamDuocChon.getMaNhaCungCap() == null ? "Chưa gán NCC" : sanPhamDuocChon.getMaNhaCungCap()));
        capNhatNhaCungCapHienThi();
        capNhatTongTien();
        bangChiTiet.refresh();
    }

    /**
     * Xử lý lưu đơn nhập hàng.
     */
    private void xuLyLuuDonNhap() {
        // Kiểm tra dữ liệu đầu vào
        if (dpNgayLap.getValue() == null) {
            hienThiLoi("Vui lòng chọn ngày lập đơn.");
            return;
        }
        if (danhSachChiTiet.isEmpty()) {
            hienThiLoi("Danh sách sản phẩm trống. Vui lòng thêm ít nhất một sản phẩm.");
            return;
        }
        // Kiểm tra số lượng và đơn giá hợp lệ
        for (ChiTietDonNhap ct : danhSachChiTiet) {
            if (ct.getSoLuong() <= 0) {
                hienThiLoi("Số lượng sản phẩm \"" + ct.getTenSanPham() + "\" phải lớn hơn 0.");
                return;
            }
            if (ct.getDonGia() == null || ct.getDonGia().compareTo(BigDecimal.ZERO) <= 0) {
                hienThiLoi("Đơn giá sản phẩm \"" + ct.getTenSanPham() + "\" phải lớn hơn 0.");
                return;
            }
        }

        // Kiểm tra lại dữ liệu nhà cung cấp/sản phẩm ngay trước khi lưu.
        Map<String, SanPham> sanPhamHienTai;
        Map<String, String> tenNhaCungCapHienTai;
        Set<String> nhaCungCapDangHoatDong;
        try {
            SanPhamController sanPhamController = new SanPhamController();
            sanPhamHienTai = sanPhamController.layDanhSachSanPham().stream()
                    .collect(Collectors.toMap(SanPham::getMaSanPham, sp -> sp, (a, b) -> a));
            List<com.petshop.model.NhaCungCap> dsNhaCungCap = sanPhamController.layDanhSachNhaCungCap();
            nhaCungCapDangHoatDong = dsNhaCungCap.stream()
                    .map(com.petshop.model.NhaCungCap::getMaNhaCungCap)
                    .collect(Collectors.toSet());
            tenNhaCungCapHienTai = dsNhaCungCap.stream().collect(Collectors.toMap(
                    com.petshop.model.NhaCungCap::getMaNhaCungCap,
                    com.petshop.model.NhaCungCap::getTenNhaCungCap,
                    (a, b) -> a));
        } catch (Exception ex) {
            hienThiLoi("Không thể kiểm tra trạng thái sản phẩm và nhà cung cấp: " + ex.getMessage());
            return;
        }

        Map<String, List<ChiTietDonNhap>> chiTietTheoNhaCungCap = new LinkedHashMap<>();
        for (ChiTietDonNhap ct : danhSachChiTiet) {
            String maNhaCungCap = nhaCungCapTheoSanPham.get(ct.getMaSanPham());
            SanPham hienTai = sanPhamHienTai.get(ct.getMaSanPham());
            if (hienTai == null || !hienTai.isTrangThai()) {
                hienThiLoi("Sản phẩm " + ct.getTenSanPham() + " không còn kinh doanh hoặc đã bị xóa.");
                return;
            }
            if (maNhaCungCap == null || maNhaCungCap.isBlank()) {
                hienThiLoi("Sản phẩm " + ct.getTenSanPham() + " chưa được gắn với nhà cung cấp.");
                return;
            }
            if (!maNhaCungCap.equals(hienTai.getMaNhaCungCap())) {
                hienThiLoi("Sản phẩm " + ct.getTenSanPham() + " không còn gắn với nhà cung cấp đã chọn. Hãy thêm lại sản phẩm.");
                return;
            }
            if (!nhaCungCapDangHoatDong.contains(maNhaCungCap)) {
                String tenNcc = tenNhaCungCapTheoSanPham.getOrDefault(ct.getMaSanPham(), maNhaCungCap);
                hienThiLoi("Nhà cung cấp " + tenNcc + " đã ngừng hợp tác nên không thể nhập " + ct.getTenSanPham() + ".");
                return;
            }
            chiTietTheoNhaCungCap.computeIfAbsent(maNhaCungCap, k -> new ArrayList<>()).add(ct);
        }

        // Lấy thông tin nhân viên
        TaiKhoan taiKhoan = SessionManager.getTaiKhoanHienTai();
        if (taiKhoan == null) {
            hienThiLoi("Không xác định được nhân viên đang đăng nhập. Vui lòng đăng nhập lại.");
            return;
        }

        List<DonNhapHang> danhSachDonHang = new ArrayList<>();
        for (Map.Entry<String, List<ChiTietDonNhap>> entry : chiTietTheoNhaCungCap.entrySet()) {
            DonNhapHang donHang = new DonNhapHang();
            donHang.setMaNhaCungCap(entry.getKey());
            donHang.setMaNhanVien(taiKhoan.getMaNhanVien());
            donHang.setNgayLap(dpNgayLap.getValue());
            donHang.setGioLap(LocalTime.of(spGio.getValue(), spPhut.getValue(), spGiay.getValue()));
            List<ChiTietDonNhap> dsBanSao = entry.getValue().stream()
                    .map(ct -> new ChiTietDonNhap(null, ct.getMaSanPham(), ct.getTenSanPham(),
                            ct.getDonViTinh(), ct.getSoLuong(), ct.getDonGia()))
                    .collect(Collectors.toList());
            donHang.setDanhSachChiTiet(dsBanSao);
            danhSachDonHang.add(donHang);
        }

        // Gọi controller để lưu
        try {
            boolean ketQua = donHangController.lapNhieuDonNhapHang(danhSachDonHang);
            if (ketQua) {
                String maDonDaTao = danhSachDonHang.stream()
                        .map(dh -> tenNhaCungCapHienTai.getOrDefault(dh.getMaNhaCungCap(), dh.getMaNhaCungCap())
                                + ": " + dh.getMaDonHang())
                        .collect(Collectors.joining("\n"));
                hienThiThongBao("Lập đơn nhập hàng thành công!\n" + maDonDaTao);
                lamMoiForm();
            } else {
                hienThiLoi("Không thể lưu đơn nhập hàng. Vui lòng kiểm tra lại thông tin.");
            }
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi lưu đơn nhập hàng: " + ex.getMessage());
        }
    }

    /**
     * Cập nhật nhãn tổng tiền.
     */
    private void capNhatTongTien() {
        BigDecimal tongTien = BigDecimal.ZERO;
        for (ChiTietDonNhap ct : danhSachChiTiet) {
            tongTien = tongTien.add(ct.getThanhTien());
        }
        lblTongTien.setText("Tổng tiền: " + DINH_DANG_TIEN.format(tongTien) + " đ");
    }

    private void capNhatNhaCungCapHienThi() {
        LinkedHashSet<String> tenNhaCungCap = danhSachChiTiet.stream()
                .map(ct -> tenNhaCungCapTheoSanPham.get(ct.getMaSanPham()))
                .filter(ten -> ten != null && !ten.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        txtNhaCungCap.setText(String.join(", ", tenNhaCungCap));
    }

    /**
     * Làm mới toàn bộ form về trạng thái ban đầu.
     */
    private void lamMoiForm() {
        danhSachChiTiet.clear();
        nhaCungCapTheoSanPham.clear();
        tenNhaCungCapTheoSanPham.clear();
        txtNhaCungCap.clear();
        dpNgayLap.setValue(LocalDate.now());
        LocalTime gioHienTai = LocalTime.now();
        spGio.getValueFactory().setValue(gioHienTai.getHour());
        spPhut.getValueFactory().setValue(gioHienTai.getMinute());
        spGiay.getValueFactory().setValue(gioHienTai.getSecond());
        capNhatTongTien();
        bangChiTiet.refresh();

        txtMaDon.setText("Tự sinh khi lưu (mỗi NCC một mã)");
    }

    // ========================= THÔNG BÁO =========================

    /**
     * Hiển thị thông báo lỗi.
     */
    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo thành công.
     */
    private void hienThiThongBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    // ========================= DIALOG CHỌN SẢN PHẨM (inner class) =========================

    /**
     * Hộp thoại chọn sản phẩm từ danh sách sản phẩm đang kinh doanh.
     * Hiển thị bảng sản phẩm để người dùng chọn và thêm vào đơn nhập hàng.
     */
    private static class ChonSanPhamDialog extends Stage {

        private final TableView<SanPham> tblSanPham = new TableView<>();
        private final TextField txtTimKiem = new TextField();
        private SanPham sanPhamDuocChon = null;

        // Danh sách sản phẩm đang kinh doanh
        private List<SanPham> tatCaSanPham = new ArrayList<>();
        private Set<String> maNhaCungCapDangHoatDong = Set.of();

        /**
         * Khởi tạo dialog chọn sản phẩm.
         *
         * @param owner cửa sổ cha
         */
        public ChonSanPhamDialog(Window owner) {
            initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                initOwner(owner);
            }
            setTitle("Chọn sản phẩm nhập hàng");

            VBox root = new VBox(14);
            root.setPadding(new Insets(18));
            root.getStyleClass().add("khung-phan-he");

            // Tiêu đề
            Label lblTieuDe = new Label("CHỌN SẢN PHẨM");
            lblTieuDe.getStyleClass().add("tieu-de-phan-he");
            Label lblMoTa = new Label("Chọn sản phẩm cần nhập. Nhà cung cấp của sản phẩm sẽ được liên kết tự động.");
            lblMoTa.getStyleClass().add("mo-ta-phan-he");
            lblMoTa.setWrapText(true);

            // Khung chính
            VBox khungChinh = taoKhungChinh();
            VBox.setVgrow(khungChinh, Priority.ALWAYS);

            root.getChildren().addAll(lblTieuDe, lblMoTa, khungChinh);

            // Tạo scene
            Scene scene = new Scene(root, 800, 520);
            try {
                scene.getStylesheets().add(getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm());
            } catch (Exception ignored) {
            }
            setScene(scene);

            // Tải dữ liệu
            taiDuLieu();
        }

        /**
         * Tạo khung nội dung chính của dialog.
         */
        @SuppressWarnings("unchecked")
        private VBox taoKhungChinh() {
            VBox khung = new VBox(12);
            khung.getStyleClass().add("hop-noi-dung");
            VBox.setVgrow(khung, Priority.ALWAYS);

            // Thanh tìm kiếm
            HBox thanhTim = new HBox(10);
            thanhTim.setAlignment(Pos.CENTER_LEFT);

            txtTimKiem.setPromptText("Nhập mã hoặc tên sản phẩm...");
            txtTimKiem.setPrefWidth(300);
            txtTimKiem.textProperty().addListener((obs, cu, moi) -> locSanPham(moi));

            Button btnLamMoi = new Button("Làm mới");
            btnLamMoi.getStyleClass().add("nut-phu");
            btnLamMoi.setOnAction(e -> {
                txtTimKiem.clear();
                tblSanPham.setItems(FXCollections.observableArrayList(tatCaSanPham));
            });

            thanhTim.getChildren().addAll(new Label("Tìm kiếm:"), txtTimKiem, btnLamMoi);

            // Bảng sản phẩm
            taoCotBang();
            tblSanPham.getStyleClass().add("bang-du-lieu");
            tblSanPham.setPlaceholder(new Label("Không tìm thấy sản phẩm đang kinh doanh"));
            VBox.setVgrow(tblSanPham, Priority.ALWAYS);

            // Nhấp đúp chuột để chọn nhanh
            tblSanPham.setRowFactory(tv -> {
                TableRow<SanPham> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        sanPhamDuocChon = row.getItem();
                        close();
                    }
                });
                return row;
            });

            // Thanh nút điều khiển
            HBox thanhNut = new HBox(10);
            thanhNut.setAlignment(Pos.CENTER_RIGHT);

            Button btnChon = new Button("Chọn sản phẩm");
            btnChon.getStyleClass().add("nut-chinh");
            btnChon.setDisable(true);
            btnChon.setOnAction(e -> {
                SanPham chon = tblSanPham.getSelectionModel().getSelectedItem();
                if (chon != null) {
                    sanPhamDuocChon = chon;
                    close();
                }
            });

            Button btnHuy = new Button("Hủy bỏ");
            btnHuy.getStyleClass().add("nut-phu");
            btnHuy.setOnAction(e -> {
                sanPhamDuocChon = null;
                close();
            });

            // Bật/tắt nút chọn khi có dòng được chọn
            tblSanPham.getSelectionModel().selectedItemProperty().addListener(
                    (obs, cu, moi) -> btnChon.setDisable(moi == null));

            Region khoangTrong = new Region();
            HBox.setHgrow(khoangTrong, Priority.ALWAYS);

            thanhNut.getChildren().addAll(khoangTrong, btnHuy, btnChon);

            khung.getChildren().addAll(thanhTim, tblSanPham, thanhNut);
            return khung;
        }

        /**
         * Tạo các cột cho bảng sản phẩm.
         */
        private void taoCotBang() {
            DecimalFormat dinhDangTien = new DecimalFormat("#,##0");

            TableColumn<SanPham, String> colMa = new TableColumn<>("Mã SP");
            colMa.setPrefWidth(140);
            colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaSanPham()));

            TableColumn<SanPham, String> colTen = new TableColumn<>("Tên sản phẩm");
            colTen.setPrefWidth(200);
            colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenSanPham()));

            TableColumn<SanPham, String> colNhaCungCap = new TableColumn<>("Nhà cung cấp");
            colNhaCungCap.setPrefWidth(190);
            colNhaCungCap.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getTenNhaCungCap() != null ? d.getValue().getTenNhaCungCap() : "Chưa gán"
            ));

            TableColumn<SanPham, String> colTrangThaiNhaCungCap = new TableColumn<>("Trạng thái NCC");
            colTrangThaiNhaCungCap.setPrefWidth(130);
            colTrangThaiNhaCungCap.setCellValueFactory(d -> {
                String maNcc = d.getValue().getMaNhaCungCap();
                String trangThai = maNcc == null || maNcc.isBlank() ? "Chưa gán NCC"
                        : (maNhaCungCapDangHoatDong.contains(maNcc) ? "Đang hợp tác" : "Ngừng hợp tác");
                return new SimpleStringProperty(trangThai);
            });

            TableColumn<SanPham, String> colDVT = new TableColumn<>("Đơn vị tính");
            colDVT.setPrefWidth(90);
            colDVT.setStyle("-fx-alignment: CENTER;");
            colDVT.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDonViTinh()));

            TableColumn<SanPham, String> colGia = new TableColumn<>("Giá bán");
            colGia.setPrefWidth(120);
            colGia.setStyle("-fx-alignment: CENTER-RIGHT;");
            colGia.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getGiaBan() != null ? dinhDangTien.format(d.getValue().getGiaBan()) : "0"
            ));

            TableColumn<SanPham, String> colTon = new TableColumn<>("SL Tồn");
            colTon.setPrefWidth(80);
            colTon.setStyle("-fx-alignment: CENTER;");
            colTon.setCellValueFactory(d -> new SimpleStringProperty(
                    String.valueOf(d.getValue().getSoLuongTon())
            ));

            tblSanPham.getColumns().setAll(List.of(colMa, colTen, colNhaCungCap, colTrangThaiNhaCungCap, colDVT, colGia, colTon));
        }

        /**
         * Tải danh sách sản phẩm đang kinh doanh từ DAO.
         */
        private void taiDuLieu() {
            try {
                SanPhamController sanPhamController = new SanPhamController();
                maNhaCungCapDangHoatDong = sanPhamController.layDanhSachNhaCungCap().stream()
                        .map(ncc -> ncc.getMaNhaCungCap())
                        .collect(Collectors.toSet());
                // Hiển thị mọi sản phẩm còn kinh doanh; trạng thái NCC được báo riêng để khi lưu giải thích rõ nếu NCC đã ngừng hợp tác.
                tatCaSanPham = sanPhamController.layDanhSachSanPham()
                        .stream()
                        .filter(SanPham::isTrangThai)
                        .collect(Collectors.toList());
                tblSanPham.setItems(FXCollections.observableArrayList(tatCaSanPham));
            } catch (Exception ex) {
                tblSanPham.setPlaceholder(new Label("Lỗi khi tải danh sách sản phẩm: " + ex.getMessage()));
            }
        }

        /**
         * Lọc sản phẩm theo từ khóa tìm kiếm.
         */
        private void locSanPham(String tuKhoa) {
            if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
                tblSanPham.setItems(FXCollections.observableArrayList(tatCaSanPham));
                return;
            }
            String tuKhoaLower = tuKhoa.trim().toLowerCase();
            List<SanPham> ketQua = tatCaSanPham.stream()
                    .filter(sp -> (sp.getMaSanPham() != null && sp.getMaSanPham().toLowerCase().contains(tuKhoaLower))
                            || (sp.getTenSanPham() != null && sp.getTenSanPham().toLowerCase().contains(tuKhoaLower))
                            || (sp.getTenNhaCungCap() != null && sp.getTenNhaCungCap().toLowerCase().contains(tuKhoaLower)))
                    .collect(Collectors.toList());
            tblSanPham.setItems(FXCollections.observableArrayList(ketQua));
        }

        /**
         * Trả về sản phẩm được chọn, hoặc null nếu hủy bỏ.
         */
        public SanPham getSanPhamDuocChon() {
            return sanPhamDuocChon;
        }
    }
}

