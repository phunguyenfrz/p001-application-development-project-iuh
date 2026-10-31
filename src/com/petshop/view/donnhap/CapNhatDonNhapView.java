package com.petshop.view.donnhap;

import com.petshop.controller.DonNhapHangController;
import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Giao diện cập nhật đơn nhập hàng.
 * Cho phép xác nhận nhận hàng, hủy đơn, xem chi tiết mặt hàng và tra cứu tên hàng.
 */
public class CapNhatDonNhapView extends VBox {

    // === Các thành phần giao diện ===
    private TableView<DonNhapHang> bangDonHang;
    private TableView<ChiTietDonNhap> bangChiTietMatHang;
    private Label lblTieuDeChiTiet;
    private Button nutXacNhan;
    private Button nutHuyDon;
    private Button nutXemChiTiet;
    private Button nutLamMoi;
    private Label nhanTrangThai;

    // === Controller ===
    private final DonNhapHangController controller;

    // === Định dạng ===
    private final DecimalFormat dinhDangTien = new DecimalFormat("#,##0");
    private final DateTimeFormatter dinhDangNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dinhDangGio = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Khởi tạo giao diện cập nhật đơn nhập hàng.
     */
    public CapNhatDonNhapView() {
        controller = new DonNhapHangController();
        khoiTaoGiaoDien();
        taiDuLieu();
    }

    /**
     * Khởi tạo toàn bộ giao diện.
     */
    private void khoiTaoGiaoDien() {
        getStyleClass().add("khung-phan-he");
        setSpacing(12);
        setPadding(new Insets(16));

        // --- Phần tiêu đề ---
        VBox hopTieuDe = taoPhanTieuDe();

        // --- Phần nút chức năng ---
        HBox hopNut = taoPhanNut();

        // --- Bảng đơn nhập hàng chính ---
        bangDonHang = taoBangDonHang();

        // --- Bảng chi tiết mặt hàng phía dưới (Master - Detail) ---
        VBox hopChiTietMatHang = taoPhanChiTietMatHang();

        // Sử dụng SplitPane để người dùng có thể điều chỉnh độ cao hai bảng
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(bangDonHang, hopChiTietMatHang);
        splitPane.setDividerPositions(0.62);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // --- Phần trạng thái ---
        nhanTrangThai = new Label();
        nhanTrangThai.getStyleClass().add("mo-ta-phan-he");

        getChildren().addAll(hopTieuDe, hopNut, splitPane, nhanTrangThai);

        // Lắng nghe sự kiện chọn dòng trên bảng để bật/tắt nút và tải chi tiết mặt hàng
        bangDonHang.getSelectionModel().selectedItemProperty().addListener(
                (obs, dongCu, dongMoi) -> capNhatTrangThaiNut(dongMoi));

        // Mặc định tắt các nút khi chưa chọn dòng
        capNhatTrangThaiNut(null);
    }

    /**
     * Tạo phần tiêu đề của giao diện.
     */
    private VBox taoPhanTieuDe() {
        VBox hop = new VBox(4);

        Label tieuDe = new Label("CẬP NHẬT ĐƠN NHẬP HÀNG");
        tieuDe.getStyleClass().add("tieu-de-phan-he");

        Label moTa = new Label("Xác nhận nhận hàng (kiểm đếm số lượng thực nhận), hủy đơn và theo dõi chi tiết mặt hàng.");
        moTa.getStyleClass().add("mo-ta-phan-he");

        hop.getChildren().addAll(tieuDe, moTa);
        return hop;
    }

    /**
     * Tạo phần nút chức năng.
     */
    private HBox taoPhanNut() {
        HBox hop = new HBox(10);
        hop.setAlignment(Pos.CENTER_LEFT);

        // Nút xác nhận nhận hàng
        nutXacNhan = new Button("Xác nhận nhận hàng");
        nutXacNhan.getStyleClass().add("nut-chinh");
        nutXacNhan.setOnAction(e -> xuLyXacNhanNhanHang());

        // Nút hủy đơn (màu đỏ)
        nutHuyDon = new Button("Hủy đơn");
        nutHuyDon.getStyleClass().add("nut-phu");
        nutHuyDon.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        nutHuyDon.setOnAction(e -> xuLyHuyDon());

        // Nút xem chi tiết
        nutXemChiTiet = new Button("Xem chi tiết");
        nutXemChiTiet.getStyleClass().add("nut-phu");
        nutXemChiTiet.setOnAction(e -> xuLyXemChiTiet());

        // Nút làm mới
        nutLamMoi = new Button("Làm mới");
        nutLamMoi.getStyleClass().add("nut-phu");
        nutLamMoi.setOnAction(e -> taiDuLieu());

        hop.getChildren().addAll(nutXacNhan, nutHuyDon, nutXemChiTiet, nutLamMoi);
        return hop;
    }

    /**
     * Tạo bảng hiển thị danh sách đơn hàng.
     */
    @SuppressWarnings("unchecked")
    private TableView<DonNhapHang> taoBangDonHang() {
        TableView<DonNhapHang> bang = new TableView<>();
        bang.getStyleClass().add("bang-du-lieu");
        bang.setPlaceholder(new Label("Không có đơn nhập hàng nào."));

        // Cột mã đơn hàng
        TableColumn<DonNhapHang, String> cotMa = new TableColumn<>("Mã đơn hàng");
        cotMa.setCellValueFactory(new PropertyValueFactory<>("maDonHang"));
        cotMa.setPrefWidth(125);

        // Cột tên nhà cung cấp
        TableColumn<DonNhapHang, String> cotNCC = new TableColumn<>("Nhà cung cấp");
        cotNCC.setCellValueFactory(new PropertyValueFactory<>("tenNhaCungCap"));
        cotNCC.setPrefWidth(170);

        // Cột tên hàng (các mặt hàng trong đơn nhập)
        TableColumn<DonNhapHang, String> cotTenHang = new TableColumn<>("Tên hàng");
        cotTenHang.setCellValueFactory(cellData -> {
            String tenHang = cellData.getValue().getTenHang();
            return new SimpleStringProperty(tenHang != null ? tenHang : "");
        });
        cotTenHang.setPrefWidth(240);

        // Cột tên nhân viên
        TableColumn<DonNhapHang, String> cotNV = new TableColumn<>("Nhân viên");
        cotNV.setCellValueFactory(new PropertyValueFactory<>("tenNhanVien"));
        cotNV.setPrefWidth(140);

        // Cột ngày lập (định dạng dd/MM/yyyy)
        TableColumn<DonNhapHang, String> cotNgayLap = new TableColumn<>("Ngày lập");
        cotNgayLap.setCellValueFactory(cellData -> {
            LocalDate ngay = cellData.getValue().getNgayLap();
            String ngayStr = (ngay != null) ? ngay.format(dinhDangNgay) : "";
            return new SimpleStringProperty(ngayStr);
        });
        cotNgayLap.setPrefWidth(105);

        // Cột thời gian lập
        TableColumn<DonNhapHang, String> cotGioLap = new TableColumn<>("Thời gian");
        cotGioLap.setCellValueFactory(cellData -> {
            LocalTime gio = cellData.getValue().getGioLap();
            return new SimpleStringProperty(gio != null ? gio.format(dinhDangGio) : "");
        });
        cotGioLap.setPrefWidth(90);

        // Cột tổng tiền (định dạng #,##0)
        TableColumn<DonNhapHang, String> cotTongTien = new TableColumn<>("Tổng tiền (đ)");
        cotTongTien.setCellValueFactory(cellData -> {
            BigDecimal tongTien = cellData.getValue().getTongTien();
            String tienStr = (tongTien != null) ? dinhDangTien.format(tongTien) : "0";
            return new SimpleStringProperty(tienStr);
        });
        cotTongTien.setPrefWidth(120);
        cotTongTien.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Cột trạng thái (tô màu theo trạng thái)
        TableColumn<DonNhapHang, String> cotTrangThai = new TableColumn<>("Trạng thái");
        cotTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        cotTrangThai.setPrefWidth(130);
        cotTrangThai.setCellFactory(col -> new TableCell<DonNhapHang, String>() {
            @Override
            protected void updateItem(String trangThai, boolean empty) {
                super.updateItem(trangThai, empty);
                if (empty || trangThai == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(trangThai);
                    setStyle("-fx-font-weight: bold;");
                    if (DonNhapHang.TRANG_THAI_CHO_NHAN.equals(trangThai)) {
                        setTextFill(Color.ORANGE);
                    } else if (DonNhapHang.TRANG_THAI_HOAN_TAT.equals(trangThai)) {
                        setTextFill(Color.GREEN);
                    } else if (DonNhapHang.TRANG_THAI_DA_HUY.equals(trangThai)) {
                        setTextFill(Color.RED);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        bang.getColumns().addAll(cotMa, cotNCC, cotTenHang, cotNV, cotNgayLap, cotGioLap, cotTongTien, cotTrangThai);
        bang.setRowFactory(tv -> {
            TableRow<DonNhapHang> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    bang.getSelectionModel().select(row.getIndex());
                    xuLyXemChiTiet();
                }
            });
            return row;
        });
        return bang;
    }

    /**
     * Tạo phần bảng hiển thị chi tiết các mặt hàng của đơn hàng đang được chọn.
     */
    @SuppressWarnings("unchecked")
    private VBox taoPhanChiTietMatHang() {
        VBox hop = new VBox(6);
        hop.setPadding(new Insets(6, 0, 0, 0));

        lblTieuDeChiTiet = new Label("Danh sách mặt hàng của đơn nhập:");
        lblTieuDeChiTiet.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        bangChiTietMatHang = new TableView<>();
        bangChiTietMatHang.getStyleClass().add("bang-du-lieu");
        bangChiTietMatHang.setPlaceholder(new Label("Chọn một đơn nhập hàng ở bảng trên để xem danh sách mặt hàng."));

        TableColumn<ChiTietDonNhap, Number> cotSTT = new TableColumn<>("STT");
        cotSTT.setPrefWidth(45);
        cotSTT.setStyle("-fx-alignment: CENTER;");
        cotSTT.setCellValueFactory(cellData -> {
            int idx = bangChiTietMatHang.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(idx);
        });

        TableColumn<ChiTietDonNhap, String> cotMaSP = new TableColumn<>("Mã SP");
        cotMaSP.setCellValueFactory(new PropertyValueFactory<>("maSanPham"));
        cotMaSP.setPrefWidth(120);

        TableColumn<ChiTietDonNhap, String> cotTenSP = new TableColumn<>("Tên hàng");
        cotTenSP.setCellValueFactory(cellData -> {
            String ten = cellData.getValue().getTenSanPham();
            return new SimpleStringProperty(ten != null ? ten : cellData.getValue().getMaSanPham());
        });
        cotTenSP.setPrefWidth(220);

        TableColumn<ChiTietDonNhap, String> cotDVT = new TableColumn<>("ĐVT");
        cotDVT.setCellValueFactory(new PropertyValueFactory<>("donViTinh"));
        cotDVT.setPrefWidth(70);
        cotDVT.setStyle("-fx-alignment: CENTER;");

        TableColumn<ChiTietDonNhap, Integer> cotSL = new TableColumn<>("Số lượng đặt");
        cotSL.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        cotSL.setPrefWidth(100);
        cotSL.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<ChiTietDonNhap, String> cotSLTN = new TableColumn<>("Thực nhận");
        cotSLTN.setCellValueFactory(cellData -> {
            Integer tn = cellData.getValue().getSoLuongThucNhan();
            return new SimpleStringProperty(tn != null ? String.valueOf(tn) : "-");
        });
        cotSLTN.setPrefWidth(90);
        cotSLTN.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        TableColumn<ChiTietDonNhap, String> cotDonGia = new TableColumn<>("Đơn giá (đ)");
        cotDonGia.setCellValueFactory(cellData -> {
            BigDecimal gia = cellData.getValue().getDonGia();
            return new SimpleStringProperty(gia != null ? dinhDangTien.format(gia) : "0");
        });
        cotDonGia.setPrefWidth(120);
        cotDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<ChiTietDonNhap, String> cotThanhTien = new TableColumn<>("Thành tiền (đ)");
        cotThanhTien.setCellValueFactory(cellData -> {
            BigDecimal tt = cellData.getValue().getThanhTien();
            return new SimpleStringProperty(tt != null ? dinhDangTien.format(tt) : "0");
        });
        cotThanhTien.setPrefWidth(130);
        cotThanhTien.setStyle("-fx-alignment: CENTER-RIGHT;");

        bangChiTietMatHang.getColumns().addAll(cotSTT, cotMaSP, cotTenSP, cotDVT, cotSL, cotSLTN, cotDonGia, cotThanhTien);
        VBox.setVgrow(bangChiTietMatHang, Priority.ALWAYS);

        hop.getChildren().addAll(lblTieuDeChiTiet, bangChiTietMatHang);
        return hop;
    }

    /**
     * Cập nhật trạng thái bật/tắt các nút và tải bảng chi tiết mặt hàng dựa trên dòng được chọn.
     */
    private void capNhatTrangThaiNut(DonNhapHang donHang) {
        if (donHang == null) {
            nutXacNhan.setDisable(true);
            nutHuyDon.setDisable(true);
            nutXemChiTiet.setDisable(true);
            lblTieuDeChiTiet.setText("Danh sách mặt hàng của đơn nhập:");
            bangChiTietMatHang.getItems().clear();
        } else {
            boolean laChoNhanHang = DonNhapHang.TRANG_THAI_CHO_NHAN.equals(donHang.getTrangThai());
            nutXacNhan.setDisable(!laChoNhanHang);
            nutHuyDon.setDisable(!laChoNhanHang);
            nutXemChiTiet.setDisable(false);

            lblTieuDeChiTiet.setText("Danh sách mặt hàng của đơn nhập #" + donHang.getMaDonHang()
                    + " (" + (donHang.getTenNhaCungCap() != null ? donHang.getTenNhaCungCap() : donHang.getMaNhaCungCap()) + "):");
            try {
                List<ChiTietDonNhap> dsCT = controller.layChiTietTheoMaDonHang(donHang.getMaDonHang());
                bangChiTietMatHang.setItems(FXCollections.observableArrayList(dsCT));
            } catch (Exception ex) {
                bangChiTietMatHang.getItems().clear();
            }
        }
    }

    /**
     * Tải dữ liệu đơn hàng từ controller và hiển thị lên bảng.
     */
    private void taiDuLieu() {
        try {
            List<DonNhapHang> danhSach = controller.layTatCaDonHang();
            ObservableList<DonNhapHang> danhSachObservable = FXCollections.observableArrayList(danhSach);
            bangDonHang.setItems(danhSachObservable);

            long soChoNhan = danhSach.stream()
                    .filter(dh -> DonNhapHang.TRANG_THAI_CHO_NHAN.equals(dh.getTrangThai()))
                    .count();
            nhanTrangThai.setText("Tổng số đơn: " + danhSach.size()
                    + " | Đang chờ nhận hàng: " + soChoNhan
                    + " | Đã hoàn tất: " + (danhSach.size() - soChoNhan));

            bangDonHang.getSelectionModel().clearSelection();
            capNhatTrangThaiNut(null);
        } catch (Exception ex) {
            hienThongBaoLoi("Lỗi tải dữ liệu", "Không thể tải danh sách đơn nhập hàng.\n" + ex.getMessage());
        }
    }

    /**
     * Xử lý xác nhận nhận hàng với dialog kiểm đếm số lượng thực nhận (Phase 2).
     */
    @SuppressWarnings("unchecked")
    private void xuLyXacNhanNhanHang() {
        DonNhapHang donHang = bangDonHang.getSelectionModel().getSelectedItem();
        if (donHang == null) return;

        List<ChiTietDonNhap> dsChiTiet;
        try {
            dsChiTiet = controller.layChiTietTheoMaDonHang(donHang.getMaDonHang());
        } catch (Exception ex) {
            hienThongBaoLoi("Lỗi tải chi tiết", "Không thể tải chi tiết sản phẩm: " + ex.getMessage());
            return;
        }

        if (dsChiTiet == null || dsChiTiet.isEmpty()) {
            hienThongBaoLoi("Lỗi", "Đơn hàng không có sản phẩm nào để xác nhận!");
            return;
        }

        // Tạo dialog xác nhận có bảng chỉnh sửa thực nhận
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Xác Nhận Nhận Hàng - " + donHang.getMaDonHang());
        dialog.setMinWidth(720);
        dialog.setMinHeight(460);

        VBox hopDialog = new VBox(12);
        hopDialog.setPadding(new Insets(16));
        hopDialog.getStyleClass().add("khung-phan-he");

        Label lblTieuDeDlg = new Label("XÁC NHẬN NHẬN HÀNG");
        lblTieuDeDlg.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label lblThongTin = new Label("Đơn hàng: " + donHang.getMaDonHang()
                + " | Nhà cung cấp: " + (donHang.getTenNhaCungCap() != null ? donHang.getTenNhaCungCap() : donHang.getMaNhaCungCap())
                + "\nVui lòng kiểm tra và điều chỉnh số lượng thực nhận cho từng mặt hàng (thiếu/hỏng):");
        lblThongTin.setStyle("-fx-text-fill: #555;");

        TableView<ChiTietDonNhap> bangKiemDem = new TableView<>();
        bangKiemDem.getStyleClass().add("bang-du-lieu");
        ObservableList<ChiTietDonNhap> dsObservable = FXCollections.observableArrayList(dsChiTiet);
        bangKiemDem.setItems(dsObservable);
        VBox.setVgrow(bangKiemDem, Priority.ALWAYS);

        TableColumn<ChiTietDonNhap, String> cotMa = new TableColumn<>("Mã SP");
        cotMa.setCellValueFactory(new PropertyValueFactory<>("maSanPham"));
        cotMa.setPrefWidth(110);

        TableColumn<ChiTietDonNhap, String> cotTen = new TableColumn<>("Tên hàng");
        cotTen.setCellValueFactory(cellData -> {
            String ten = cellData.getValue().getTenSanPham();
            return new SimpleStringProperty(ten != null ? ten : cellData.getValue().getMaSanPham());
        });
        cotTen.setPrefWidth(200);

        TableColumn<ChiTietDonNhap, Integer> cotSLDat = new TableColumn<>("SL Đặt");
        cotSLDat.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        cotSLDat.setPrefWidth(80);
        cotSLDat.setStyle("-fx-alignment: CENTER-RIGHT;");

        Label lblTongTienThucNhan = new Label();
        lblTongTienThucNhan.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Runnable tinhLaiTong = () -> {
            BigDecimal tong = BigDecimal.ZERO;
            for (ChiTietDonNhap ct : dsObservable) {
                int slTN = ct.getSoLuongThucNhan() != null ? ct.getSoLuongThucNhan() : 0;
                BigDecimal gia = ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO;
                tong = tong.add(gia.multiply(BigDecimal.valueOf(slTN)));
            }
            lblTongTienThucNhan.setText("Tổng tiền thực nhận: " + dinhDangTien.format(tong) + " đ");
        };

        TableColumn<ChiTietDonNhap, Void> cotSLThucNhan = new TableColumn<>("SL Thực nhận");
        cotSLThucNhan.setPrefWidth(120);
        cotSLThucNhan.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>();
            {
                spinner.setEditable(true);
                spinner.setPrefWidth(95);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ChiTietDonNhap ct = getTableView().getItems().get(getIndex());
                    int max = ct.getSoLuong();
                    int hienTai = ct.getSoLuongThucNhan() != null ? ct.getSoLuongThucNhan() : max;
                    SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, max, Math.min(hienTai, max));
                    spinner.setValueFactory(factory);
                    spinner.valueProperty().addListener((obs, cu, moi) -> {
                        if (moi != null) {
                            ct.setSoLuongThucNhan(moi);
                            tinhLaiTong.run();
                        }
                    });
                    setGraphic(spinner);
                }
            }
        });

        TableColumn<ChiTietDonNhap, String> cotGia = new TableColumn<>("Đơn giá (đ)");
        cotGia.setCellValueFactory(cellData -> {
            BigDecimal g = cellData.getValue().getDonGia();
            return new SimpleStringProperty(g != null ? dinhDangTien.format(g) : "0");
        });
        cotGia.setPrefWidth(110);
        cotGia.setStyle("-fx-alignment: CENTER-RIGHT;");

        bangKiemDem.getColumns().addAll(cotMa, cotTen, cotSLDat, cotSLThucNhan, cotGia);

        // Khởi tạo nhãn tổng tiền
        tinhLaiTong.run();

        // Nút bấm
        HBox hopNutDlg = new HBox(10);
        hopNutDlg.setAlignment(Pos.CENTER_RIGHT);

        Region khoangCach = new Region();
        HBox.setHgrow(khoangCach, Priority.ALWAYS);

        Button btnXacNhanDlg = new Button("Xác nhận nhận hàng");
        btnXacNhanDlg.getStyleClass().add("nut-chinh");
        btnXacNhanDlg.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnDongDlg = new Button("Đóng");
        btnDongDlg.getStyleClass().add("nut-phu");
        btnDongDlg.setOnAction(e -> dialog.close());

        btnXacNhanDlg.setOnAction(e -> {
            // Kiểm tra: nếu toàn bộ dòng đều thực nhận = 0 thì chặn
            boolean coItNhatMot = false;
            for (ChiTietDonNhap ct : dsObservable) {
                if (ct.getSoLuongThucNhan() != null && ct.getSoLuongThucNhan() > 0) {
                    coItNhatMot = true;
                    break;
                }
            }
            if (!coItNhatMot) {
                hienThongBaoLoi("Số lượng không hợp lệ",
                        "Toàn bộ mặt hàng đều có số lượng thực nhận là 0.\nNếu không nhận bất kỳ hàng nào, vui lòng dùng chức năng Hủy đơn.");
                return;
            }

            try {
                boolean ok = controller.xacNhanNhanHang(donHang.getMaDonHang(), new ArrayList<>(dsObservable));
                if (ok) {
                    hienThongBaoThanhCong("Thành công",
                            "Đã xác nhận nhận hàng và cập nhật tồn kho cho đơn: " + donHang.getMaDonHang());
                    dialog.close();
                    taiDuLieu();
                } else {
                    hienThongBaoLoi("Không thể nhận hàng",
                            "Trạng thái đơn hàng đã thay đổi hoặc có lỗi trong quá trình cập nhật.");
                }
            } catch (Exception ex) {
                hienThongBaoLoi("Lỗi xác nhận nhận hàng", ex.getMessage());
            }
        });

        hopNutDlg.getChildren().addAll(lblTongTienThucNhan, khoangCach, btnXacNhanDlg, btnDongDlg);

        hopDialog.getChildren().addAll(lblTieuDeDlg, lblThongTin, bangKiemDem, hopNutDlg);

        Scene scene = new Scene(hopDialog);
        if (getClass().getResource("/com/petshop/view/css/chung.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Xử lý hủy đơn nhập hàng với lý do hủy bắt buộc (Phase 2).
     */
    private void xuLyHuyDon() {
        DonNhapHang donHang = bangDonHang.getSelectionModel().getSelectedItem();
        if (donHang == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Hủy đơn nhập hàng");
        dialog.setHeaderText("Hủy đơn nhập hàng: " + donHang.getMaDonHang());
        dialog.setContentText("Nhập lý do hủy đơn (bắt buộc):");

        Optional<String> lyDo = dialog.showAndWait();
        if (lyDo.isPresent()) {
            String noiDungLyDo = lyDo.get().trim();
            if (noiDungLyDo.isEmpty()) {
                hienThongBaoLoi("Lỗi", "Bạn phải nhập lý do hủy đơn!");
                return;
            }
            try {
                boolean daHuy = controller.huyDonNhapHang(donHang.getMaDonHang(), noiDungLyDo);
                if (daHuy) {
                    hienThongBaoThanhCong("Thành công", "Đã hủy đơn nhập hàng: " + donHang.getMaDonHang());
                    taiDuLieu();
                } else {
                    hienThongBaoLoi("Không thể hủy đơn", "Trạng thái đơn hàng đã thay đổi. Vui lòng tải lại danh sách.");
                }
            } catch (Exception ex) {
                hienThongBaoLoi("Lỗi hủy đơn", ex.getMessage());
            }
        }
    }

    /**
     * Xử lý xem chi tiết đơn nhập hàng được chọn.
     */
    private void xuLyXemChiTiet() {
        DonNhapHang donHang = bangDonHang.getSelectionModel().getSelectedItem();
        if (donHang == null) return;

        XemChiTietDonNhapView.moDialog(getScene().getWindow(), donHang);
    }

    /**
     * Hiển thị thông báo thành công.
     */
    private void hienThongBaoThanhCong(String tieuDe, String noiDung) {
        Alert thongBao = new Alert(Alert.AlertType.INFORMATION);
        thongBao.setTitle(tieuDe);
        thongBao.setHeaderText(null);
        thongBao.setContentText(noiDung);
        thongBao.showAndWait();
    }

    /**
     * Hiển thị thông báo lỗi.
     */
    private void hienThongBaoLoi(String tieuDe, String noiDung) {
        Alert thongBao = new Alert(Alert.AlertType.ERROR);
        thongBao.setTitle(tieuDe);
        thongBao.setHeaderText(null);
        thongBao.setContentText(noiDung);
        thongBao.showAndWait();
    }
}
