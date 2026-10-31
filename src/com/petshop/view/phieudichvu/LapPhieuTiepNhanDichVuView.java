package com.petshop.view.phieudichvu;

import com.petshop.controller.PhieuDichVuController;
import com.petshop.model.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Giao diện Lập phiếu tiếp nhận dịch vụ chăm sóc tại quầy (PDV_TAO).
 * Cho phép tiếp nhận khách vãng lai hoặc tiếp nhận theo lịch hẹn đã đặt trước.
 */
public class LapPhieuTiepNhanDichVuView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PhieuDichVuController controller = new PhieuDichVuController();
    private final Runnable khiLuuThanhCong;

    // Danh sách lưu trữ dữ liệu
    private final List<KhachHang> dsKhachHangGoc = new ArrayList<>();
    private final ObservableList<ChiTietPhieuDichVu> dsChiTietChon = FXCollections.observableArrayList();

    // Điều khiển chọn nguồn tiếp nhận
    private final ComboBox<LichHen> cboLichHen = new ComboBox<>();

    // Điều khiển thông tin phiếu
    private final TextField txtMaPhieuDichVu = new TextField();
    private final TextField txtTimKhach = new TextField();
    private final ComboBox<KhachHang> cboKhachHang = new ComboBox<>();
    private final ComboBox<String[]> cboNhanVien = new ComboBox<>();
    private final DatePicker dpNgayTiepNhan = new DatePicker();
    private final TextField txtGioTiepNhan = new TextField();
    private final TextArea txtGhiChu = new TextArea();

    // Điều khiển chọn dòng dịch vụ
    private final ComboBox<ThuCung> cboThuCung = new ComboBox<>();
    private final ComboBox<DichVuChamSoc> cboDichVu = new ComboBox<>();
    private final TextField txtDonGia = new TextField();
    private final Button btnThemDichVu = new Button("+ Thêm vào phiếu");
    private final TableView<ChiTietPhieuDichVu> tblChiTiet = new TableView<>();
    private final Label lblTongTien = new Label("Tổng tiền: 0 đ");

    // Nút tác vụ
    private final Button btnLuu = new Button("✔ Lập phiếu tiếp nhận");
    private final Button btnLamMoi = new Button("Làm mới");

    public LapPhieuTiepNhanDichVuView() {
        this(null);
    }

    public LapPhieuTiepNhanDichVuView(Runnable khiLuuThanhCong) {
        this.khiLuuThanhCong = khiLuuThanhCong;

        setSpacing(10);
        setPadding(new Insets(14, 20, 14, 20));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(2);
        Label lblTieuDe = new Label("TIẾP NHẬN DỊCH VỤ TẠI QUẦY");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Lập phiếu tiếp nhận thú cưng làm dịch vụ trực tiếp hoặc tiếp nhận từ lịch hẹn đã đặt.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Nội dung cuộn
        VBox pnlThongTin = taoKhungThongTinPhieu();
        VBox pnlDichVu = taoKhungDichVu();
        VBox.setVgrow(pnlDichVu, Priority.ALWAYS);

        VBox pnlCuon = new VBox(10, pnlThongTin, pnlDichVu);
        pnlCuon.setFillWidth(true);
        VBox.setVgrow(pnlCuon, Priority.ALWAYS);

        ScrollPane spCuon = new ScrollPane(pnlCuon);
        spCuon.setFitToWidth(true);
        spCuon.setFitToHeight(true);
        spCuon.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spCuon.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(spCuon, Priority.ALWAYS);

        // 3. Thanh nút thao tác
        HBox pnlTacVu = taoThanhTacVu();

        getChildren().addAll(pnlTieuDe, spCuon, pnlTacVu);

        caiDatBangChiTiet();
        caiDatSuKien();
        napDuLieuBanDau();
    }

    // Tạo khung nhập thông tin chung của phiếu tiếp nhận
    private VBox taoKhungThongTinPhieu() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.setStyle("-fx-background-color: #ffffff; -fx-padding: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        Label lblPhan1 = new Label("1. Thông tin tiếp nhận dịch vụ");
        lblPhan1.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        // Dòng chọn tiếp nhận từ lịch hẹn
        cboLichHen.setPromptText("-- Chọn lịch hẹn đến giờ tiếp nhận (nếu có) --");
        cboLichHen.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cboLichHen, Priority.ALWAYS);

        Button btnBoChonLich = new Button("Tiếp nhận trực tiếp");
        btnBoChonLich.getStyleClass().add("nut-phu");
        btnBoChonLich.setOnAction(e -> {
            cboLichHen.setValue(null);
            cboKhachHang.setDisable(false);
            txtTimKhach.setDisable(false);
        });

        HBox pnlLichHen = new HBox(8, new Label("Nguồn tiếp nhận:"), cboLichHen, btnBoChonLich);
        pnlLichHen.setAlignment(Pos.CENTER_LEFT);

        // Lưới thông tin chi tiết
        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(14);
        pnlLuoi.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints(260, 300, Double.MAX_VALUE);
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(120);
        ColumnConstraints col4 = new ColumnConstraints(260, 300, Double.MAX_VALUE);
        col4.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3, col4);

        // Hàng 0: Mã phiếu & Nhân viên tiếp nhận
        txtMaPhieuDichVu.setEditable(false);
        txtMaPhieuDichVu.setPromptText("Tự động sinh (PDV-yyyyMMdd-xxxxxx)");
        pnlLuoi.add(new Label("Mã phiếu:"), 0, 0);
        pnlLuoi.add(txtMaPhieuDichVu, 1, 0);

        cboNhanVien.setMaxWidth(Double.MAX_VALUE);
        cboNhanVien.setPromptText("Chọn nhân viên tiếp nhận...");
        pnlLuoi.add(new Label("Nhân viên (*):"), 2, 0);
        pnlLuoi.add(cboNhanVien, 3, 0);

        // Hàng 1: Tìm & Chọn khách hàng
        txtTimKhach.setPromptText("Tìm nhanh theo tên hoặc SĐT khách...");
        pnlLuoi.add(new Label("Tìm khách hàng:"), 0, 1);
        pnlLuoi.add(txtTimKhach, 1, 1);

        cboKhachHang.setMaxWidth(Double.MAX_VALUE);
        cboKhachHang.setPromptText("Chọn khách hàng (*)...");
        pnlLuoi.add(new Label("Khách hàng (*):"), 2, 1);
        pnlLuoi.add(cboKhachHang, 3, 1);

        // Hàng 2: Ngày tiếp nhận & Giờ tiếp nhận
        dpNgayTiepNhan.setValue(LocalDate.now());
        dpNgayTiepNhan.setMaxWidth(Double.MAX_VALUE);
        pnlLuoi.add(new Label("Ngày tiếp nhận (*):"), 0, 2);
        pnlLuoi.add(dpNgayTiepNhan, 1, 2);

        txtGioTiepNhan.setPromptText("HH:mm:ss (Mặc định giờ hiện tại)");
        txtGioTiepNhan.setText(LocalTime.now().format(DINH_DANG_GIO));
        pnlLuoi.add(new Label("Giờ tiếp nhận (*):"), 2, 2);
        pnlLuoi.add(txtGioTiepNhan, 3, 2);

        // Hàng 3: Ghi chú
        txtGhiChu.setPromptText("Ghi chú lúc tiếp nhận (tình trạng sức khỏe thú cưng, yêu cầu của khách...)");
        txtGhiChu.setPrefRowCount(2);
        pnlLuoi.add(new Label("Ghi chú tiếp nhận:"), 0, 3);
        pnlLuoi.add(txtGhiChu, 1, 3, 3, 1);

        pnlKhung.getChildren().addAll(lblPhan1, pnlLichHen, pnlLuoi);
        return pnlKhung;
    }

    // Tạo khung chọn thú cưng và các dịch vụ thực hiện
    private VBox taoKhungDichVu() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.setStyle("-fx-background-color: #ffffff; -fx-padding: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        Label lblPhan2 = new Label("2. Danh sách dịch vụ thực hiện trong phiếu");
        lblPhan2.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        // Dòng thêm dịch vụ
        cboThuCung.setPromptText("Chọn thú cưng...");
        cboThuCung.setPrefWidth(200);

        cboDichVu.setPromptText("Chọn dịch vụ chăm sóc...");
        cboDichVu.setPrefWidth(280);
        HBox.setHgrow(cboDichVu, Priority.ALWAYS);

        txtDonGia.setPromptText("Đơn giá (VNĐ)");
        txtDonGia.setPrefWidth(140);

        btnThemDichVu.getStyleClass().add("nut-chinh");

        HBox pnlThem = new HBox(8,
                new Label("Thú cưng:"), cboThuCung,
                new Label("Dịch vụ:"), cboDichVu,
                new Label("Đơn giá:"), txtDonGia,
                btnThemDichVu
        );
        pnlThem.setAlignment(Pos.CENTER_LEFT);

        // Bảng chi tiết dịch vụ
        tblChiTiet.getStyleClass().add("bang-du-lieu");
        tblChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblChiTiet.setPrefHeight(200);
        tblChiTiet.setPlaceholder(new Label("Chưa có dịch vụ nào được thêm vào phiếu"));
        VBox.setVgrow(tblChiTiet, Priority.ALWAYS);

        // Thanh tổng kết tiền
        lblTongTien.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        HBox pnlTong = new HBox(lblTongTien);
        pnlTong.setAlignment(Pos.CENTER_RIGHT);

        pnlKhung.getChildren().addAll(lblPhan2, pnlThem, tblChiTiet, pnlTong);
        return pnlKhung;
    }

    // Cài đặt các cột hiển thị cho bảng dịch vụ chi tiết
    private void caiDatBangChiTiet() {
        tblChiTiet.setItems(dsChiTietChon);

        TableColumn<ChiTietPhieuDichVu, Number> colSTT = new TableColumn<>("STT");
        colSTT.setStyle("-fx-alignment: CENTER;");
        colSTT.setPrefWidth(50);
        colSTT.setMaxWidth(60);
        colSTT.setCellValueFactory(c -> new SimpleIntegerProperty(dsChiTietChon.indexOf(c.getValue()) + 1));

        TableColumn<ChiTietPhieuDichVu, String> colThuCung = new TableColumn<>("Thú cưng");
        colThuCung.setPrefWidth(180);
        colThuCung.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenThuCung()));

        TableColumn<ChiTietPhieuDichVu, String> colDichVu = new TableColumn<>("Dịch vụ chăm sóc");
        colDichVu.setPrefWidth(260);
        colDichVu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDichVu()));

        TableColumn<ChiTietPhieuDichVu, String> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDonGia.setPrefWidth(140);
        colDonGia.setCellValueFactory(c -> {
            BigDecimal gia = c.getValue().getDonGia();
            return new SimpleStringProperty(gia != null ? DINH_DANG_TIEN.format(gia) : "0 đ");
        });

        TableColumn<ChiTietPhieuDichVu, Void> colXoa = new TableColumn<>("Thao tác");
        colXoa.setStyle("-fx-alignment: CENTER;");
        colXoa.setPrefWidth(80);
        colXoa.setMaxWidth(100);
        colXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");
            {
                btnXoa.getStyleClass().addAll("button", "nut-xoa");
                btnXoa.setOnAction(e -> {
                    ChiTietPhieuDichVu item = getTableView().getItems().get(getIndex());
                    dsChiTietChon.remove(item);
                    capNhatTongTien();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoa);
            }
        });

        tblChiTiet.getColumns().addAll(colSTT, colThuCung, colDichVu, colDonGia, colXoa);
    }

    // Tạo thanh nút tác vụ phía dưới
    private HBox taoThanhTacVu() {
        btnLuu.getStyleClass().add("nut-chinh");
        btnLamMoi.getStyleClass().add("nut-phu");

        HBox pnlTacVu = new HBox(10, btnLamMoi, btnLuu);
        pnlTacVu.setAlignment(Pos.CENTER_RIGHT);
        pnlTacVu.setPadding(new Insets(6, 0, 0, 0));
        return pnlTacVu;
    }

    // Cài đặt các bộ chuyển đổi hiển thị và bắt sự kiện
    private void caiDatSuKien() {
        // 1. Hiển thị ComboBox Lịch hẹn
        cboLichHen.setConverter(new StringConverter<>() {
            @Override
            public String toString(LichHen lh) {
                if (lh == null) return "";
                String tenKh = lh.getTenKhachHang() != null ? lh.getTenKhachHang() : lh.getMaKhachHang();
                String tenTc = lh.getTenThuCung() != null ? lh.getTenThuCung() : lh.getMaThuCung();
                String ngay = lh.getNgayHen() != null ? lh.getNgayHen().toString() : "";
                String gio = lh.getGioHen() != null ? lh.getGioHen().toString() : "";
                return lh.getMaLichHen() + " | " + tenKh + " - Bé " + tenTc + " (" + gio + " " + ngay + ")";
            }

            @Override
            public LichHen fromString(String string) {
                return null;
            }
        });

        // Khi chọn một lịch hẹn: tự động điền khách, thú cưng và chi tiết dịch vụ
        cboLichHen.setOnAction(e -> {
            LichHen lh = cboLichHen.getValue();
            if (lh == null) return;

            // Đồng bộ khách hàng
            for (KhachHang kh : dsKhachHangGoc) {
                if (kh.getMaKhachHang().equals(lh.getMaKhachHang())) {
                    cboKhachHang.setValue(kh);
                    break;
                }
            }
            cboKhachHang.setDisable(true);
            txtTimKhach.setDisable(true);

            // Nạp chi tiết dịch vụ từ lịch hẹn
            dsChiTietChon.clear();
            List<ChiTietLichHen> dsCtLh = controller.layChiTietLichHen(lh.getMaLichHen());
            for (ChiTietLichHen ctLh : dsCtLh) {
                ChiTietPhieuDichVu ctPdv = new ChiTietPhieuDichVu(
                        txtMaPhieuDichVu.getText(),
                        lh.getMaThuCung(),
                        lh.getTenThuCung(),
                        ctLh.getMaDichVu(),
                        ctLh.getTenDichVu(),
                        ctLh.getDonGia()
                );
                dsChiTietChon.add(ctPdv);
            }
            txtGhiChu.setText("Tiếp nhận theo lịch hẹn " + lh.getMaLichHen() + (lh.getGhiChu() != null ? " | " + lh.getGhiChu() : ""));
            capNhatTongTien();
        });

        // 2. Hiển thị ComboBox Khách hàng
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

        // Lọc nhanh danh sách khách hàng khi gõ
        txtTimKhach.textProperty().addListener((obs, cu, moi) -> {
            if (moi == null || moi.trim().isEmpty()) {
                cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
            } else {
                String tuKhoa = moi.trim().toLowerCase();
                List<KhachHang> loc = dsKhachHangGoc.stream()
                        .filter(k -> (k.getHoTen() != null && k.getHoTen().toLowerCase().contains(tuKhoa))
                                || (k.getSoDienThoai() != null && k.getSoDienThoai().contains(tuKhoa))
                                || (k.getMaKhachHang() != null && k.getMaKhachHang().toLowerCase().contains(tuKhoa)))
                        .toList();
                cboKhachHang.setItems(FXCollections.observableArrayList(loc));
                if (!loc.isEmpty()) {
                    cboKhachHang.setValue(loc.get(0));
                }
            }
        });

        // Khi khách hàng thay đổi: nạp danh sách thú cưng của khách đó
        cboKhachHang.setOnAction(e -> {
            KhachHang kh = cboKhachHang.getValue();
            if (kh != null) {
                List<ThuCung> dsTc = controller.layDanhSachThuCungTheoKhachHang(kh.getMaKhachHang());
                cboThuCung.setItems(FXCollections.observableArrayList(dsTc));
                if (!dsTc.isEmpty()) {
                    cboThuCung.setValue(dsTc.get(0));
                }
            } else {
                cboThuCung.getItems().clear();
            }
        });

        // 3. Hiển thị ComboBox Thú cưng
        cboThuCung.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThuCung tc) {
                if (tc == null) return "";
                String giong = tc.getGiongLoai() != null ? tc.getGiongLoai() : "";
                String can = tc.getCanNang() != null ? " - " + tc.getCanNang() + "kg" : "";
                return tc.getTenThuCung() + " (" + giong + can + ")";
            }

            @Override
            public ThuCung fromString(String string) {
                return null;
            }
        });

        // 4. Hiển thị ComboBox Dịch vụ
        cboDichVu.setConverter(new StringConverter<>() {
            @Override
            public String toString(DichVuChamSoc dv) {
                if (dv == null) return "";
                return dv.getTenDichVu() + " (" + dv.getMaDichVu() + ")";
            }

            @Override
            public DichVuChamSoc fromString(String string) {
                return null;
            }
        });

        // Tự động tính giá khi chọn thú cưng và dịch vụ
        cboDichVu.setOnAction(e -> tinhGiaTuDong());
        cboThuCung.setOnAction(e -> tinhGiaTuDong());

        // 5. Hiển thị ComboBox Nhân viên
        cboNhanVien.setConverter(new StringConverter<>() {
            @Override
            public String toString(String[] nv) {
                if (nv == null || nv.length < 2) return "";
                return nv[0] + " - " + nv[1] + (nv.length > 2 ? " (" + nv[2] + ")" : "");
            }

            @Override
            public String[] fromString(String string) {
                return null;
            }
        });

        // 6. Nút thêm dịch vụ vào bảng
        btnThemDichVu.setOnAction(e -> xuLyThemDichVu());

        // 7. Nút Lưu
        btnLuu.setOnAction(e -> xuLyLuuPhieu());

        // 8. Nút Làm mới
        btnLamMoi.setOnAction(e -> lamMoiForm());
    }

    // Tự tính giá dịch vụ dựa trên cân nặng thú cưng
    private void tinhGiaTuDong() {
        ThuCung tc = cboThuCung.getValue();
        DichVuChamSoc dv = cboDichVu.getValue();
        if (tc != null && dv != null) {
            double canNang = (tc.getCanNang() != null) ? tc.getCanNang().doubleValue() : 3.0;
            double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang);
            txtDonGia.setText(String.valueOf((long) gia));
        }
    }

    // Xử lý thêm một dòng dịch vụ vào bảng chi tiết
    private void xuLyThemDichVu() {
        ThuCung tc = cboThuCung.getValue();
        if (tc == null) {
            hienThiCanhBao("Chưa chọn thú cưng", "Vui lòng chọn thú cưng cần làm dịch vụ!");
            return;
        }

        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) {
            hienThiCanhBao("Chưa chọn dịch vụ", "Vui lòng chọn dịch vụ chăm sóc!");
            return;
        }

        String donGiaStr = txtDonGia.getText() != null ? txtDonGia.getText().trim() : "";
        BigDecimal donGia;
        try {
            donGia = new BigDecimal(donGiaStr);
            if (donGia.compareTo(BigDecimal.ZERO) < 0) {
                hienThiCanhBao("Đơn giá không hợp lệ", "Đơn giá không được nhỏ hơn 0!");
                return;
            }
        } catch (Exception ex) {
            hienThiCanhBao("Đơn giá không hợp lệ", "Vui lòng nhập đơn giá là số hợp lệ!");
            return;
        }

        // Kiểm tra xem dịch vụ này đã thêm cho thú cưng này chưa
        for (ChiTietPhieuDichVu ct : dsChiTietChon) {
            if (ct.getMaThuCung().equals(tc.getMaThuCung()) && ct.getMaDichVu().equals(dv.getMaDichVu())) {
                hienThiCanhBao("Trùng dịch vụ", "Dịch vụ '" + dv.getTenDichVu() + "' đã được thêm cho bé " + tc.getTenThuCung() + "!");
                return;
            }
        }

        ChiTietPhieuDichVu ct = new ChiTietPhieuDichVu(
                txtMaPhieuDichVu.getText(),
                tc.getMaThuCung(),
                tc.getTenThuCung(),
                dv.getMaDichVu(),
                dv.getTenDichVu(),
                donGia
        );
        dsChiTietChon.add(ct);
        capNhatTongTien();
    }

    // Cập nhật nhãn tổng tiền dự tính
    private void capNhatTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietPhieuDichVu ct : dsChiTietChon) {
            if (ct.getDonGia() != null) {
                tong = tong.add(ct.getDonGia());
            }
        }
        lblTongTien.setText("Tổng tiền dự tính: " + DINH_DANG_TIEN.format(tong));
    }

    // Xử lý lưu phiếu tiếp nhận xuống CSDL
    private void xuLyLuuPhieu() {
        try {
            KhachHang kh = cboKhachHang.getValue();
            String[] nv = cboNhanVien.getValue();

            LocalDate ngay = dpNgayTiepNhan.getValue();
            String gioStr = txtGioTiepNhan.getText() != null ? txtGioTiepNhan.getText().trim() : "";
            LocalTime gio;
            try {
                gio = LocalTime.parse(gioStr);
            } catch (Exception ex) {
                gio = LocalTime.now();
            }

            LichHen lh = cboLichHen.getValue();
            String maLichHen = (lh != null) ? lh.getMaLichHen() : null;

            PhieuDichVu phieu = new PhieuDichVu(
                    txtMaPhieuDichVu.getText().trim(),
                    kh != null ? kh.getMaKhachHang() : null,
                    nv != null ? nv[0] : null,
                    maLichHen,
                    ngay,
                    gio,
                    "Đang chờ",
                    txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : ""
            );

            List<ChiTietPhieuDichVu> danhSachChiTiet = new ArrayList<>(dsChiTietChon);
            boolean thanhCong = controller.taoPhieuDichVu(phieu, danhSachChiTiet);
            if (thanhCong) {
                hienThiThongBao("Tiếp nhận thành công", "Đã tạo phiếu tiếp nhận dịch vụ mã: " + phieu.getMaPhieuDichVu() + "\nTrạng thái: Đang chờ thực hiện");
                if (khiLuuThanhCong != null) {
                    khiLuuThanhCong.run();
                }
                lamMoiForm();
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiCanhBao("Không thể lập phiếu", ex.getMessage());
        } catch (Exception ex) {
            hienThiCanhBao("Lỗi hệ thống", "Đã xảy ra lỗi không mong muốn: " + ex.getMessage());
        }
    }

    // Nạp dữ liệu ban đầu cho các ComboBox
    private void napDuLieuBanDau() {
        lamMoiForm();
        try {
            // Nạp khách hàng
            dsKhachHangGoc.clear();
            dsKhachHangGoc.addAll(controller.layDanhSachKhachHang());
            cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));

            // Nạp nhân viên
            List<String[]> dsNv = controller.layDanhSachNhanVien();
            cboNhanVien.setItems(FXCollections.observableArrayList(dsNv));
            if (!dsNv.isEmpty()) {
                cboNhanVien.setValue(dsNv.get(0));
            }

            // Nạp dịch vụ
            List<DichVuChamSoc> dsDv = controller.layDanhSachDichVu();
            cboDichVu.setItems(FXCollections.observableArrayList(dsDv));

            // Nạp lịch hẹn chờ tiếp nhận
            napDanhSachLichHenCho();
        } catch (Exception ex) {
            System.err.println("Lỗi khi nạp dữ liệu ban đầu cho phiếu dịch vụ: " + ex.getMessage());
        }
    }

    // Nạp lại danh sách lịch hẹn chờ tiếp nhận
    private void napDanhSachLichHenCho() {
        try {
            List<LichHen> dsLh = controller.layDanhSachLichHenChoTiepNhan();
            cboLichHen.setItems(FXCollections.observableArrayList(dsLh));
        } catch (Exception ignored) {}
    }

    // Đặt lại dữ liệu form về trạng thái mới
    private void lamMoiForm() {
        try {
            txtMaPhieuDichVu.setText(controller.taoMaPhieuDichVuMoi());
        } catch (Exception ex) {
            txtMaPhieuDichVu.setText("");
        }
        cboLichHen.setValue(null);
        cboKhachHang.setValue(null);
        cboKhachHang.setDisable(false);
        txtTimKhach.clear();
        txtTimKhach.setDisable(false);
        cboThuCung.getItems().clear();
        cboThuCung.setValue(null);
        cboDichVu.setValue(null);
        txtDonGia.clear();
        dpNgayTiepNhan.setValue(LocalDate.now());
        txtGioTiepNhan.setText(LocalTime.now().format(DINH_DANG_GIO));
        txtGhiChu.clear();
        dsChiTietChon.clear();
        capNhatTongTien();
        napDanhSachLichHenCho();
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(tieuDe);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(tieuDe);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

