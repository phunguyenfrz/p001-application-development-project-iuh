package com.petshop.view.donnhap;

import com.petshop.controller.DonNhapHangController;
import com.petshop.model.ChiTietDonNhap;
import com.petshop.model.DonNhapHang;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
 * Cho phép xác nhận nhận hàng, hủy đơn, xem chi tiết đơn nhập khi double-click hoặc bấm nút.
 */
public class CapNhatDonNhapView extends VBox {

    // === Các thành phần giao diện ===
    private TableView<DonNhapHang> bangDonHang;
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
        setSpacing(14);
        setPadding(new Insets(18));

        // --- Phần tiêu đề ---
        VBox hopTieuDe = taoPhanTieuDe();

        // --- Phần nút chức năng ---
        HBox hopNut = taoPhanNut();

        // --- Bảng đơn nhập hàng chính ---
        bangDonHang = taoBangDonHang();
        VBox.setVgrow(bangDonHang, Priority.ALWAYS);

        // --- Phần trạng thái ---
        nhanTrangThai = new Label();
        nhanTrangThai.getStyleClass().add("mo-ta-phan-he");

        getChildren().addAll(hopTieuDe, hopNut, bangDonHang, nhanTrangThai);

        // Lắng nghe sự kiện chọn dòng trên bảng để bật/tắt nút
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

        Label moTa = new Label("Xác nhận nhận hàng hoặc hủy các đơn nhập hàng đang chờ xử lý. Nhấn đúp chuột vào đơn hàng để xem chi tiết.");
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

        // Nút xác nhận nhận hàng (có màu)
        nutXacNhan = new Button("Xác nhận nhận hàng");
        nutXacNhan.getStyleClass().add("nut-chinh");
        nutXacNhan.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        nutXacNhan.setOnAction(e -> xuLyXacNhanNhanHang());

        // Nút hủy đơn (màu đỏ)
        nutHuyDon = new Button("Hủy đơn");
        nutHuyDon.getStyleClass().add("nut-phu");
        nutHuyDon.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
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
        cotMa.setPrefWidth(130);

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
        cotTenHang.setPrefWidth(250);

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
        cotTongTien.setPrefWidth(125);
        cotTongTien.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Cột trạng thái (tô màu theo trạng thái)
        TableColumn<DonNhapHang, String> cotTrangThai = new TableColumn<>("Trạng thái");
        cotTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        cotTrangThai.setPrefWidth(130);
        cotTrangThai.setCellFactory(col -> new TableCell<DonNhapHang, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Chờ nhận hàng":
                            setStyle("-fx-text-fill: #D2691E; -fx-font-weight: bold;");
                            break;
                        case "Hoàn tất":
                            setStyle("-fx-text-fill: #228B22; -fx-font-weight: bold;");
                            break;
                        case "Đã hủy":
                            setStyle("-fx-text-fill: #DC143C; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
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
     * Cập nhật trạng thái bật/tắt các nút dựa trên dòng được chọn.
     */
    private void capNhatTrangThaiNut(DonNhapHang donHang) {
        if (donHang == null) {
            nutXacNhan.setDisable(true);
            nutHuyDon.setDisable(true);
            nutXemChiTiet.setDisable(true);
        } else {
            boolean laChoNhanHang = DonNhapHang.TRANG_THAI_CHO_NHAN.equals(donHang.getTrangThai());
            nutXacNhan.setDisable(!laChoNhanHang);
            nutHuyDon.setDisable(!laChoNhanHang);
            nutXemChiTiet.setDisable(false);
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
        lblTieuDeDlg.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

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
        cotTen.setPrefWidth(210);

        TableColumn<ChiTietDonNhap, Integer> cotSLDat = new TableColumn<>("SL Đặt");
        cotSLDat.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        cotSLDat.setPrefWidth(80);
        cotSLDat.setStyle("-fx-alignment: CENTER-RIGHT;");

        Label lblTongTienThucNhan = new Label();
        lblTongTienThucNhan.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Khung xử lý giao thiếu hàng (Phương án 3)
        VBox hopXuLyThieu = new VBox(8);
        hopXuLyThieu.setStyle("-fx-padding: 10; -fx-border-color: #cbd5e1; -fx-border-width: 1px; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label lblCanhBaoThieu = new Label("Phát hiện giao thiếu hàng so với đơn đặt. Vui lòng chọn cách xử lý phần thiếu:");
        lblCanhBaoThieu.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        ToggleGroup nhomXuLyThieu = new ToggleGroup();
        RadioButton radTaoDonBu = new RadioButton("Tách đơn nhập bù (Backorder) - Tự động tạo đơn mới cho phần thiếu để nhận đợt sau");
        radTaoDonBu.setToggleGroup(nhomXuLyThieu);
        radTaoDonBu.setSelected(true);
        radTaoDonBu.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        RadioButton radDongDon = new RadioButton("Đóng đơn (Hủy phần thiếu) - Không nhận tiếp, chốt theo số lượng thực tế");
        radDongDon.setToggleGroup(nhomXuLyThieu);
        radDongDon.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox hopNhapLyDo = new HBox(8);
        hopNhapLyDo.setAlignment(Pos.CENTER_LEFT);
        Label lblLyDoThieu = new Label("Lý do thiếu hàng:");
        lblLyDoThieu.setStyle("-fx-text-fill: #555;");
        ComboBox<String> cbLyDoThieu = new ComboBox<>();
        cbLyDoThieu.getItems().addAll(
                "Nhà cung cấp hết hàng",
                "Hàng cận date / không đạt chất lượng kiểm định",
                "Hàng bị bể vỡ / hư hỏng khi vận chuyển",
                "Cửa hàng hủy phần còn lại theo thỏa thuận"
        );
        cbLyDoThieu.setEditable(true);
        cbLyDoThieu.setPromptText("Chọn hoặc nhập lý do...");
        cbLyDoThieu.setPrefWidth(350);
        hopNhapLyDo.getChildren().addAll(lblLyDoThieu, cbLyDoThieu);
        hopNhapLyDo.setDisable(true);

        radDongDon.selectedProperty().addListener((obs, cu, moi) -> {
            hopNhapLyDo.setDisable(!moi);
            if (moi && (cbLyDoThieu.getValue() == null || cbLyDoThieu.getValue().isBlank())) {
                cbLyDoThieu.setValue("Nhà cung cấp hết hàng");
            }
        });

        hopXuLyThieu.getChildren().addAll(lblCanhBaoThieu, radTaoDonBu, radDongDon, hopNhapLyDo);
        hopXuLyThieu.setVisible(false);
        hopXuLyThieu.setManaged(false);

        Runnable tinhLaiTong = () -> {
            BigDecimal tong = BigDecimal.ZERO;
            int tongThieu = 0;
            for (ChiTietDonNhap ct : dsObservable) {
                int slDat = ct.getSoLuong();
                int slTN = ct.getSoLuongThucNhan() != null ? ct.getSoLuongThucNhan() : 0;
                BigDecimal gia = ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO;
                tong = tong.add(gia.multiply(BigDecimal.valueOf(slTN)));
                if (slTN < slDat) {
                    tongThieu += (slDat - slTN);
                }
            }
            lblTongTienThucNhan.setText("Tổng tiền thực nhận: " + dinhDangTien.format(tong) + " đ");
            if (tongThieu > 0) {
                lblCanhBaoThieu.setText("Phát hiện giao thiếu " + tongThieu + " sản phẩm so với đơn đặt. Vui lòng chọn cách xử lý phần thiếu:");
                hopXuLyThieu.setVisible(true);
                hopXuLyThieu.setManaged(true);
            } else {
                hopXuLyThieu.setVisible(false);
                hopXuLyThieu.setManaged(false);
            }
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
                    spinner.getEditor().textProperty().addListener((obs, cu, moi) -> {
                        try {
                            if (moi != null && !moi.isBlank()) {
                                int val = Integer.parseInt(moi.trim());
                                if (val >= 0 && val <= max) {
                                    ct.setSoLuongThucNhan(val);
                                    tinhLaiTong.run();
                                }
                            }
                        } catch (Exception ignored) {}
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

        // Khởi tạo nhãn tổng tiền và trạng thái thiếu hàng
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
            int tongThieu = 0;
            for (ChiTietDonNhap ct : dsObservable) {
                int slTN = ct.getSoLuongThucNhan() != null ? ct.getSoLuongThucNhan() : 0;
                int slDat = ct.getSoLuong();
                if (slTN > 0) {
                    coItNhatMot = true;
                }
                if (slTN < slDat) {
                    tongThieu += (slDat - slTN);
                }
            }
            if (!coItNhatMot) {
                hienThongBaoLoi("Số lượng không hợp lệ",
                        "Toàn bộ mặt hàng đều có số lượng thực nhận là 0.\nNếu không nhận bất kỳ hàng nào, vui lòng dùng chức năng Hủy đơn.");
                return;
            }

            try {
                if (tongThieu > 0) {
                    boolean taoDonBu = radTaoDonBu.isSelected();
                    String lyDo = cbLyDoThieu.getValue();
                    if (!taoDonBu && (lyDo == null || lyDo.trim().isEmpty())) {
                        hienThongBaoLoi("Thiếu lý do", "Vui lòng nhập lý do thiếu hàng khi chọn đóng đơn!");
                        return;
                    }

                    String res = controller.xacNhanNhanHangThieu(donHang.getMaDonHang(),
                            new ArrayList<>(dsObservable), taoDonBu, lyDo);

                    if (taoDonBu && res != null && !res.equals(donHang.getMaDonHang())) {
                        hienThongBaoThanhCong("Xác nhận thành công",
                                "Đã nhận hàng cho đơn " + donHang.getMaDonHang() + ".\n"
                                + "Hệ thống đã tự động tạo đơn nhập bù [" + res + "] cho " + tongThieu + " sản phẩm còn thiếu để nhận đợt sau.");
                    } else {
                        hienThongBaoThanhCong("Xác nhận thành công",
                                "Đã nhận hàng và đóng đơn " + donHang.getMaDonHang() + " theo số lượng thực nhận.\n"
                                + "Lý do thiếu hàng: " + (lyDo != null ? lyDo : ""));
                    }
                    dialog.close();
                    taiDuLieu();
                } else {
                    boolean ok = controller.xacNhanNhanHang(donHang.getMaDonHang(), new ArrayList<>(dsObservable));
                    if (ok) {
                        hienThongBaoThanhCong("Thành công",
                                "Đã xác nhận nhận đủ hàng và cập nhật tồn kho cho đơn: " + donHang.getMaDonHang());
                        dialog.close();
                        taiDuLieu();
                    } else {
                        hienThongBaoLoi("Không thể nhận hàng",
                                "Trạng thái đơn hàng đã thay đổi hoặc có lỗi trong quá trình cập nhật.");
                    }
                }
            } catch (Exception ex) {
                hienThongBaoLoi("Lỗi xác nhận nhận hàng", ex.getMessage());
            }
        });

        hopNutDlg.getChildren().addAll(lblTongTienThucNhan, khoangCach, btnXacNhanDlg, btnDongDlg);

        hopDialog.getChildren().addAll(lblTieuDeDlg, lblThongTin, bangKiemDem, hopXuLyThieu, hopNutDlg);

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
