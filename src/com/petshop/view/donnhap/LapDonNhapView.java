package com.petshop.view.donnhap;

import com.petshop.controller.DonHangController;
import com.petshop.controller.SanPhamController;
import com.petshop.model.ChiTietDonHang;
import com.petshop.model.DonHang;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final DonHangController donHangController = new DonHangController();

    // --- Phần thông tin đơn hàng ---
    private final TextField txtMaDon = new TextField();
    private final DatePicker dpNgayLap = new DatePicker(LocalDate.now());
    private final TextField txtNhaCungCap = new TextField();
    private final TextField txtNhanVien = new TextField();

    // Nhà cung cấp được tự xác định từ sản phẩm đầu tiên trong đơn.
    private String maNhaCungCapDaChon = null;

    // --- Bảng chi tiết sản phẩm ---
    private final TableView<ChiTietDonHang> bangChiTiet = new TableView<>();
    private final ObservableList<ChiTietDonHang> danhSachChiTiet = FXCollections.observableArrayList();

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
        Label lblMaDon = new Label("Mã đơn:");
        lblMaDon.setStyle("-fx-font-weight: bold;");
        txtMaDon.setEditable(false);
        txtMaDon.setPrefWidth(220);
        txtMaDon.setPromptText("Tự động sinh...");
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
        Label lblNCC = new Label("Nhà cung cấp:");
        lblNCC.setStyle("-fx-font-weight: bold;");
        txtNhaCungCap.setEditable(false);
        txtNhaCungCap.setMaxWidth(Double.MAX_VALUE);
        txtNhaCungCap.setPromptText("Tự động điền sau khi chọn sản phẩm");
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

        hopThongTin.getChildren().addAll(hang1, hang2);
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
        TableColumn<ChiTietDonHang, Number> cotSTT = new TableColumn<>("STT");
        cotSTT.setMinWidth(50);
        cotSTT.setMaxWidth(60);
        cotSTT.setStyle("-fx-alignment: CENTER;");
        cotSTT.setSortable(false);
        cotSTT.setCellValueFactory(cellData -> {
            int index = bangChiTiet.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(index);
        });

        // Cột mã sản phẩm
        TableColumn<ChiTietDonHang, String> cotMaSP = new TableColumn<>("Mã SP");
        cotMaSP.setMinWidth(130);
        cotMaSP.setCellValueFactory(new PropertyValueFactory<>("maSanPham"));

        // Cột tên sản phẩm
        TableColumn<ChiTietDonHang, String> cotTenSP = new TableColumn<>("Tên sản phẩm");
        cotTenSP.setMinWidth(180);
        cotTenSP.setCellValueFactory(new PropertyValueFactory<>("tenSanPham"));

        // Cột đơn vị tính
        TableColumn<ChiTietDonHang, String> cotDVT = new TableColumn<>("Đơn vị tính");
        cotDVT.setMinWidth(80);
        cotDVT.setStyle("-fx-alignment: CENTER;");
        cotDVT.setCellValueFactory(new PropertyValueFactory<>("donViTinh"));

        // Cột số lượng (chỉnh sửa bằng Spinner)
        TableColumn<ChiTietDonHang, Integer> cotSoLuong = new TableColumn<>("Số lượng");
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
        TableColumn<ChiTietDonHang, BigDecimal> cotDonGia = new TableColumn<>("Đơn giá");
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
        TableColumn<ChiTietDonHang, BigDecimal> cotThanhTien = new TableColumn<>("Thành tiền");
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
        TableColumn<ChiTietDonHang, Void> cotXoa = new TableColumn<>("Xóa");
        cotXoa.setMinWidth(70);
        cotXoa.setMaxWidth(80);
        cotXoa.setStyle("-fx-alignment: CENTER;");
        cotXoa.setSortable(false);
        cotXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");

            {
                btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btnXoa.setOnAction(e -> {
                    ChiTietDonHang ct = getTableView().getItems().get(getIndex());
                    danhSachChiTiet.remove(ct);
                    if (danhSachChiTiet.isEmpty()) {
                        maNhaCungCapDaChon = null;
                        txtNhaCungCap.clear();
                    }
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

        bangChiTiet.getColumns().addAll(cotSTT, cotMaSP, cotTenSP, cotDVT, cotSoLuong, cotDonGia, cotThanhTien, cotXoa);

        hopBang.getChildren().addAll(thanhTieuDe, bangChiTiet);
        return hopBang;
    }

    /**
     * Cập nhật đơn giá từ TextField.
     */
    private void capNhatDonGia(ChiTietDonHang chiTiet, String giaText) {
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
        // Sinh mã đơn hàng tự động
        try {
            String maDon = donHangController.taoMaDonHang();
            txtMaDon.setText(maDon);
        } catch (Exception ex) {
            txtMaDon.setText("");
        }

        // Hiển thị nhân viên đang đăng nhập
        TaiKhoan taiKhoan = SessionManager.getTaiKhoanHienTai();
        if (taiKhoan != null) {
            String hoTen = taiKhoan.getHoTenNhanVien();
            String maNV = taiKhoan.getMaNhanVien();
            txtNhanVien.setText((hoTen != null ? hoTen : "") + " (" + (maNV != null ? maNV : "") + ")");
        }

        // Ngày lập mặc định hôm nay
        dpNgayLap.setValue(LocalDate.now());
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

        if (sanPhamDuocChon.getMaNhaCungCap() == null || sanPhamDuocChon.getMaNhaCungCap().isBlank()) {
            hienThiLoi("Sản phẩm này chưa được gắn với nhà cung cấp nên chưa thể lập đơn nhập. Vui lòng cập nhật nhà cung cấp cho sản phẩm trước.");
            return;
        }

        if (maNhaCungCapDaChon != null
                && !maNhaCungCapDaChon.equals(sanPhamDuocChon.getMaNhaCungCap())) {
            String tenNhaCungCap = sanPhamDuocChon.getTenNhaCungCap() != null
                    ? sanPhamDuocChon.getTenNhaCungCap() : sanPhamDuocChon.getMaNhaCungCap();
            hienThiLoi("Sản phẩm này thuộc nhà cung cấp " + tenNhaCungCap
                    + ". Mỗi đơn nhập chỉ gắn với một nhà cung cấp; hãy lập đơn riêng cho sản phẩm này.");
            return;
        }

        // Kiểm tra sản phẩm đã tồn tại trong bảng chưa
        for (ChiTietDonHang ct : danhSachChiTiet) {
            if (ct.getMaSanPham().equals(sanPhamDuocChon.getMaSanPham())) {
                hienThiLoi("Sản phẩm \"" + sanPhamDuocChon.getTenSanPham()
                        + "\" đã có trong danh sách. Vui lòng chỉnh sửa số lượng trực tiếp trên bảng.");
                return;
            }
        }

        // Tạo chi tiết đơn hàng mới
        ChiTietDonHang chiTietMoi = new ChiTietDonHang();
        chiTietMoi.setMaSanPham(sanPhamDuocChon.getMaSanPham());
        chiTietMoi.setTenSanPham(sanPhamDuocChon.getTenSanPham());
        chiTietMoi.setDonViTinh(sanPhamDuocChon.getDonViTinh());
        chiTietMoi.setSoLuong(1);
        // Đơn giá nhập mặc định lấy giá bán làm gợi ý
        chiTietMoi.setDonGia(sanPhamDuocChon.getGiaBan() != null ? sanPhamDuocChon.getGiaBan() : BigDecimal.ZERO);

        danhSachChiTiet.add(chiTietMoi);
        if (maNhaCungCapDaChon == null) {
            maNhaCungCapDaChon = sanPhamDuocChon.getMaNhaCungCap();
            txtNhaCungCap.setText(sanPhamDuocChon.getTenNhaCungCap() != null
                    ? sanPhamDuocChon.getTenNhaCungCap() : maNhaCungCapDaChon);
        }
        capNhatTongTien();
        bangChiTiet.refresh();
    }

    /**
     * Xử lý lưu đơn nhập hàng.
     */
    private void xuLyLuuDonNhap() {
        // Kiểm tra dữ liệu đầu vào
        if (maNhaCungCapDaChon == null || maNhaCungCapDaChon.isBlank()) {
            hienThiLoi("Vui lòng chọn nhà cung cấp trước khi lưu đơn nhập.");
            return;
        }
        if (dpNgayLap.getValue() == null) {
            hienThiLoi("Vui lòng chọn ngày lập đơn.");
            return;
        }
        if (danhSachChiTiet.isEmpty()) {
            hienThiLoi("Danh sách sản phẩm trống. Vui lòng thêm ít nhất một sản phẩm.");
            return;
        }
        // Kiểm tra số lượng và đơn giá hợp lệ
        for (ChiTietDonHang ct : danhSachChiTiet) {
            if (ct.getSoLuong() <= 0) {
                hienThiLoi("Số lượng sản phẩm \"" + ct.getTenSanPham() + "\" phải lớn hơn 0.");
                return;
            }
            if (ct.getDonGia() == null || ct.getDonGia().compareTo(BigDecimal.ZERO) <= 0) {
                hienThiLoi("Đơn giá sản phẩm \"" + ct.getTenSanPham() + "\" phải lớn hơn 0.");
                return;
            }
        }

        // Lấy thông tin nhân viên
        TaiKhoan taiKhoan = SessionManager.getTaiKhoanHienTai();
        if (taiKhoan == null) {
            hienThiLoi("Không xác định được nhân viên đang đăng nhập. Vui lòng đăng nhập lại.");
            return;
        }

        // Tính tổng tiền
        BigDecimal tongTien = BigDecimal.ZERO;
        for (ChiTietDonHang ct : danhSachChiTiet) {
            tongTien = tongTien.add(ct.getThanhTien());
        }

        // Tạo đối tượng DonHang
        DonHang donHang = new DonHang();
        donHang.setMaDonHang(txtMaDon.getText());
        donHang.setMaNhaCungCap(maNhaCungCapDaChon);
        donHang.setMaNhanVien(taiKhoan.getMaNhanVien());
        donHang.setNgayLap(dpNgayLap.getValue());
        donHang.setTongTien(tongTien);
        donHang.setTrangThai(DonHang.TRANG_THAI_CHO_NHAN);

        // Gán mã đơn hàng cho từng chi tiết
        List<ChiTietDonHang> dsBanSao = new ArrayList<>();
        for (ChiTietDonHang ct : danhSachChiTiet) {
            ChiTietDonHang banSao = new ChiTietDonHang(
                    donHang.getMaDonHang(),
                    ct.getMaSanPham(),
                    ct.getTenSanPham(),
                    ct.getDonViTinh(),
                    ct.getSoLuong(),
                    ct.getDonGia()
            );
            dsBanSao.add(banSao);
        }

        // Gọi controller để lưu
        try {
            boolean ketQua = donHangController.lapDonNhapHang(donHang, dsBanSao);
            if (ketQua) {
                hienThiThongBao("Lập đơn nhập hàng thành công!\nMã đơn: " + donHang.getMaDonHang());
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
        for (ChiTietDonHang ct : danhSachChiTiet) {
            tongTien = tongTien.add(ct.getThanhTien());
        }
        lblTongTien.setText("Tổng tiền: " + DINH_DANG_TIEN.format(tongTien) + " đ");
    }

    /**
     * Làm mới toàn bộ form về trạng thái ban đầu.
     */
    private void lamMoiForm() {
        danhSachChiTiet.clear();
        maNhaCungCapDaChon = null;
        txtNhaCungCap.clear();
        dpNgayLap.setValue(LocalDate.now());
        capNhatTongTien();
        bangChiTiet.refresh();

        // Sinh mã đơn mới
        try {
            String maDonMoi = donHangController.taoMaDonHang();
            txtMaDon.setText(maDonMoi);
        } catch (Exception ex) {
            txtMaDon.setText("");
        }
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

            tblSanPham.getColumns().setAll(List.of(colMa, colTen, colNhaCungCap, colDVT, colGia, colTon));
        }

        /**
         * Tải danh sách sản phẩm đang kinh doanh từ DAO.
         */
        private void taiDuLieu() {
            try {
                SanPhamController sanPhamController = new SanPhamController();
                // Lấy tất cả sản phẩm và lọc chỉ lấy đang kinh doanh (trangThai = true)
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

