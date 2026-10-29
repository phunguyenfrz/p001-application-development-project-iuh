package com.petshop.view.lichhen;

import com.petshop.controller.LichHenController;
import com.petshop.model.ChiTietLichHen;
import com.petshop.model.DichVuChamSoc;
import com.petshop.model.KhachHang;
import com.petshop.model.LichHen;
import com.petshop.model.ThuCung;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Đặt lịch hẹn dịch vụ chăm sóc thú cưng (APT_CREATE / LH_TAO).
 */
public class DatLichHenView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LichHenController controller = new LichHenController();
    private final Runnable khiDatThanhCong;

    // Danh sách dịch vụ khách hàng chọn trong lịch hẹn
    private final ObservableList<ChiTietLichHen> danhSachDichVuChon = FXCollections.observableArrayList();

    // Các điều khiển nhập liệu thông tin lịch hẹn
    private final TextField txtMaLichHen = new TextField();
    private final ComboBox<KhachHang> cboKhachHang = new ComboBox<>();
    private final ComboBox<ThuCung> cboThuCung = new ComboBox<>();
    private final ComboBox<String[]> cboNhanVien = new ComboBox<>();
    private final DatePicker dpNgayHen = new DatePicker();
    private final ComboBox<String> cboGioHen = new ComboBox<>();
    private final TextArea txtGhiChu = new TextArea();

    // Các điều khiển chọn dịch vụ
    private final ComboBox<DichVuChamSoc> cboDichVu = new ComboBox<>();
    private final TextField txtCanNangThuCung = new TextField();
    private final TextField txtDonGia = new TextField();
    private final Button btnThemDichVu = new Button("Thêm dịch vụ");
    private final TableView<ChiTietLichHen> tblDichVu = new TableView<>();
    private final Label lblTongTien = new Label("Tổng tiền dự kiến: 0 đ");

    // Các nút chức năng
    private final Button btnLuu = new Button("Xác nhận đặt lịch");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

    public DatLichHenView() {
        this(null);
    }

    public DatLichHenView(Runnable khiDatThanhCong) {
        this.khiDatThanhCong = khiDatThanhCong;

        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("ĐẶT LỊCH HẸN DỊCH VỤ CHĂM SÓC");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tiếp nhận đặt lịch hẹn chăm sóc thú cưng cho khách hàng (chặn chọn ngày quá khứ).");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung thông tin lịch hẹn
        VBox pnlThongTinLich = taoKhungThongTinLich();

        // 3. Khung chọn dịch vụ chăm sóc
        VBox pnlChonDichVu = taoKhungChonDichVu();

        // 4. Thanh nút bấm điều khiển
        HBox pnlNut = taoThanhNutBam();

        getChildren().addAll(pnlTieuDe, pnlThongTinLich, pnlChonDichVu, pnlNut);

        // Đăng ký sự kiện tương tác giữa các trường
        dangKySuKien();

        // Nạp dữ liệu ban đầu
        lamMoiForm();
    }

    // Khởi tạo phần nhập thông tin khách, thú cưng, nhân viên và thời gian hẹn
    private VBox taoKhungThongTinLich() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblPhanTieuDe = new Label("1. Thông tin lịch hẹn");
        lblPhanTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(14);
        pnlLuoi.setVgap(10);

        txtMaLichHen.setEditable(false);
        txtMaLichHen.setStyle("-fx-background-color: #f1f5f9; -fx-font-weight: bold;");

        cboKhachHang.setPromptText("Chọn khách hàng");
        cboKhachHang.setMaxWidth(Double.MAX_VALUE);

        cboThuCung.setPromptText("Chọn thú cưng");
        cboThuCung.setMaxWidth(Double.MAX_VALUE);

        cboNhanVien.setPromptText("Chọn nhân viên chăm sóc");
        cboNhanVien.setMaxWidth(Double.MAX_VALUE);

        // Cấu hình DatePicker định dạng dd/MM/yyyy và chặn ngày quá khứ
        dpNgayHen.setPromptText("dd/MM/yyyy");
        dpNgayHen.setPrefWidth(160);
        dpNgayHen.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #94a3b8;");
                }
            }
        });

        // Khung giờ làm việc từ 08:00 đến 20:00
        cboGioHen.getItems().addAll(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
                "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00"
        );
        cboGioHen.setValue("09:00");
        cboGioHen.setPrefWidth(130);

        HBox pnlThoiGian = new HBox(10);
        pnlThoiGian.setAlignment(Pos.CENTER_LEFT);
        pnlThoiGian.getChildren().addAll(dpNgayHen, new Label("Giờ:"), cboGioHen);

        txtGhiChu.setPromptText("Ghi chú thêm (yêu cầu riêng của khách hàng, lưu ý thú cưng nhát nước...)");
        txtGhiChu.setPrefRowCount(2);
        txtGhiChu.setWrapText(true);

        // Đưa vào lưới layout
        pnlLuoi.add(new Label("Mã lịch hẹn:"), 0, 0);
        pnlLuoi.add(txtMaLichHen, 1, 0);
        pnlLuoi.add(new Label("Nhân viên phụ trách (*):"), 2, 0);
        pnlLuoi.add(cboNhanVien, 3, 0);

        pnlLuoi.add(new Label("Khách hàng (*):"), 0, 1);
        pnlLuoi.add(cboKhachHang, 1, 1);
        pnlLuoi.add(new Label("Thú cưng (*):"), 2, 1);
        pnlLuoi.add(cboThuCung, 3, 1);

        pnlLuoi.add(new Label("Ngày hẹn (*):"), 0, 2);
        pnlLuoi.add(pnlThoiGian, 1, 2);
        pnlLuoi.add(new Label("Ghi chú:"), 2, 2);
        pnlLuoi.add(txtGhiChu, 3, 2);

        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(140);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3, col4);

        pnlKhung.getChildren().addAll(lblPhanTieuDe, pnlLuoi);
        return pnlKhung;
    }

    // Khởi tạo phần chọn dịch vụ chăm sóc và bảng chi tiết dịch vụ
    private VBox taoKhungChonDichVu() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        Label lblPhanTieuDe = new Label("2. Dịch vụ chăm sóc đăng ký");
        lblPhanTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        // Thanh công cụ chọn dịch vụ
        HBox pnlChon = new HBox(10);
        pnlChon.setAlignment(Pos.CENTER_LEFT);

        cboDichVu.setPromptText("Chọn dịch vụ chăm sóc");
        cboDichVu.setPrefWidth(240);

        txtCanNangThuCung.setPromptText("Cân nặng (kg)");
        txtCanNangThuCung.setPrefWidth(110);

        txtDonGia.setPromptText("Đơn giá");
        txtDonGia.setEditable(false);
        txtDonGia.setPrefWidth(130);
        txtDonGia.setStyle("-fx-background-color: #f1f5f9; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");

        btnThemDichVu.getStyleClass().add("nut-phu");

        pnlChon.getChildren().addAll(
                new Label("Dịch vụ:"), cboDichVu,
                new Label("Cân nặng (kg):"), txtCanNangThuCung,
                new Label("Đơn giá:"), txtDonGia,
                btnThemDichVu
        );

        // Bảng danh sách dịch vụ chi tiết đã thêm
        tblDichVu.getStyleClass().add("bang-du-lieu");
        tblDichVu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblDichVu.setPrefHeight(150);
        tblDichVu.setPlaceholder(new Label("Chưa có dịch vụ nào được thêm vào lịch hẹn"));
        VBox.setVgrow(tblDichVu, Priority.ALWAYS);

        TableColumn<ChiTietLichHen, String> colMaDV = new TableColumn<>("Mã dịch vụ");
        colMaDV.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaDichVu()));
        colMaDV.setStyle("-fx-alignment: CENTER;");
        colMaDV.setPrefWidth(120);

        TableColumn<ChiTietLichHen, String> colTenDV = new TableColumn<>("Tên dịch vụ");
        colTenDV.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDichVu()));
        colTenDV.setStyle("-fx-alignment: CENTER-LEFT;");
        colTenDV.setPrefWidth(220);

        TableColumn<ChiTietLichHen, String> colCanNang = new TableColumn<>("Cân nặng thú cưng");
        colCanNang.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCanNangThuCung() + " kg"));
        colCanNang.setStyle("-fx-alignment: CENTER;");
        colCanNang.setPrefWidth(130);

        TableColumn<ChiTietLichHen, String> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setCellValueFactory(c -> new SimpleStringProperty(DINH_DANG_TIEN.format(c.getValue().getDonGia())));
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colDonGia.setPrefWidth(130);

        // Cột nút xóa dịch vụ khỏi lịch
        TableColumn<ChiTietLichHen, Void> colXoa = new TableColumn<>("Thao tác");
        colXoa.setStyle("-fx-alignment: CENTER;");
        colXoa.setPrefWidth(90);
        colXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoaDong = new Button("Xóa");
            {
                btnXoaDong.getStyleClass().addAll("button", "nut-xoa");
                btnXoaDong.setOnAction(e -> {
                    ChiTietLichHen item = getTableView().getItems().get(getIndex());
                    danhSachDichVuChon.remove(item);
                    capNhatTongTien();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoaDong);
            }
        });

        tblDichVu.getColumns().addAll(colMaDV, colTenDV, colCanNang, colDonGia, colXoa);
        tblDichVu.setItems(danhSachDichVuChon);

        // Thanh tổng tiền bên dưới bảng
        HBox pnlTongTien = new HBox();
        pnlTongTien.setAlignment(Pos.CENTER_RIGHT);
        lblTongTien.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        pnlTongTien.getChildren().add(lblTongTien);

        pnlKhung.getChildren().addAll(lblPhanTieuDe, pnlChon, tblDichVu, pnlTongTien);
        return pnlKhung;
    }

    // Khởi tạo thanh nút thao tác dưới chân màn hình
    private HBox taoThanhNutBam() {
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        btnLuu.getStyleClass().add("nut-chinh");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnDong.getStyleClass().add("nut-phu");

        btnLuu.setOnAction(e -> xuLyDatLich());
        btnLamMoi.setOnAction(e -> lamMoiForm());
        btnDong.setOnAction(e -> dongCuaSo());

        pnlNut.getChildren().addAll(btnLamMoi, btnDong, btnLuu);
        return pnlNut;
    }

    // Đăng ký sự kiện tương tác giữa các trường nhập liệu
    private void dangKySuKien() {
        // Tùy biến hiển thị ComboBox Khách hàng (Họ tên - SĐT)
        cboKhachHang.setConverter(new StringConverter<>() {
            @Override
            public String toString(KhachHang kh) {
                return kh != null ? kh.getHoTen() + " (" + kh.getSoDienThoai() + ")" : "";
            }
            @Override
            public KhachHang fromString(String s) {
                return null;
            }
        });

        // Khi chọn khách hàng: tự động tải danh sách thú cưng của khách đó
        cboKhachHang.setOnAction(e -> {
            KhachHang kh = cboKhachHang.getValue();
            if (kh != null) {
                taiDanhSachThuCung(kh.getMaKhachHang());
            } else {
                cboThuCung.getItems().clear();
                txtCanNangThuCung.clear();
            }
        });

        // Tùy biến hiển thị ComboBox Thú cưng
        cboThuCung.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThuCung tc) {
                return tc != null ? tc.getTenThuCung() + " (" + tc.getLoaiThuCung() + " - " + tc.getCanNang() + "kg)" : "";
            }
            @Override
            public ThuCung fromString(String s) {
                return null;
            }
        });

        // Khi chọn thú cưng: tự động điền cân nặng vào ô nhập cân nặng
        cboThuCung.setOnAction(e -> {
            ThuCung tc = cboThuCung.getValue();
            if (tc != null && tc.getCanNang() != null) {
                txtCanNangThuCung.setText(tc.getCanNang().stripTrailingZeros().toPlainString());
                tinhDonGiaDichVu();
            }
        });

        // Tùy biến hiển thị ComboBox Nhân viên (Họ tên - Chức vụ)
        cboNhanVien.setConverter(new StringConverter<>() {
            @Override
            public String toString(String[] nv) {
                return nv != null ? nv[1] + " (" + nv[2] + ")" : "";
            }
            @Override
            public String[] fromString(String s) {
                return null;
            }
        });

        // Tùy biến hiển thị ComboBox Dịch vụ chăm sóc
        cboDichVu.setConverter(new StringConverter<>() {
            @Override
            public String toString(DichVuChamSoc dv) {
                return dv != null ? dv.getTenDichVu() + " (" + dv.getThoiLuong() + " phút)" : "";
            }
            @Override
            public DichVuChamSoc fromString(String s) {
                return null;
            }
        });

        // Khi chọn dịch vụ hoặc thay đổi cân nặng: tự động tính lại đơn giá
        cboDichVu.setOnAction(e -> tinhDonGiaDichVu());
        txtCanNangThuCung.textProperty().addListener((obs, cu, moi) -> tinhDonGiaDichVu());

        // Bấm nút thêm dịch vụ vào bảng
        btnThemDichVu.setOnAction(e -> xuLyThemDichVuVaoBang());
    }

    // Tự động tính đơn giá dịch vụ theo bảng giá dựa trên cân nặng thú cưng
    private void tinhDonGiaDichVu() {
        DichVuChamSoc dv = cboDichVu.getValue();
        String chuoiCanNang = txtCanNangThuCung.getText() != null ? txtCanNangThuCung.getText().trim() : "";
        if (dv == null || chuoiCanNang.isEmpty()) {
            txtDonGia.clear();
            return;
        }

        try {
            double canNang = Double.parseDouble(chuoiCanNang);
            if (canNang > 0) {
                double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang);
                txtDonGia.setText(DINH_DANG_TIEN.format(gia));
            } else {
                txtDonGia.clear();
            }
        } catch (NumberFormatException e) {
            txtDonGia.clear();
        }
    }

    // Thêm dịch vụ đang chọn vào danh sách chi tiết của lịch hẹn
    private void xuLyThemDichVuVaoBang() {
        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) {
            hienThiLoi("Vui lòng chọn dịch vụ chăm sóc!");
            return;
        }

        String chuoiCanNang = txtCanNangThuCung.getText() != null ? txtCanNangThuCung.getText().trim() : "";
        if (chuoiCanNang.isEmpty()) {
            hienThiLoi("Vui lòng nhập cân nặng thú cưng!");
            return;
        }

        BigDecimal canNang;
        try {
            canNang = new BigDecimal(chuoiCanNang);
            if (canNang.compareTo(BigDecimal.ZERO) <= 0) {
                hienThiLoi("Cân nặng thú cưng phải lớn hơn 0 kg!");
                return;
            }
        } catch (Exception e) {
            hienThiLoi("Cân nặng thú cưng phải là số hợp lệ!");
            return;
        }

        // Kiểm tra xem dịch vụ này đã có trong bảng chưa
        for (ChiTietLichHen ct : danhSachDichVuChon) {
            if (ct.getMaDichVu().equals(dv.getMaDichVu())) {
                hienThiLoi("Dịch vụ '" + dv.getTenDichVu() + "' đã được thêm vào lịch hẹn!");
                return;
            }
        }

        double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang.doubleValue());
        ChiTietLichHen chiTiet = new ChiTietLichHen(
                txtMaLichHen.getText(),
                dv.getMaDichVu(),
                dv.getTenDichVu(),
                canNang,
                BigDecimal.valueOf(gia)
        );

        danhSachDichVuChon.add(chiTiet);
        capNhatTongTien();
        cboDichVu.setValue(null);
        txtDonGia.clear();
    }

    // Cập nhật nhãn tổng tiền dự kiến
    private void capNhatTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietLichHen ct : danhSachDichVuChon) {
            if (ct.getDonGia() != null) {
                tong = tong.add(ct.getDonGia());
            }
        }
        lblTongTien.setText("Tổng tiền dự kiến: " + DINH_DANG_TIEN.format(tong));
    }

    // Đọc form và thực hiện đặt lịch hẹn
    private void xuLyDatLich() {
        try {
            KhachHang kh = cboKhachHang.getValue();
            ThuCung tc = cboThuCung.getValue();
            String[] nv = cboNhanVien.getValue();
            LocalDate ngay = dpNgayHen.getValue();
            String gioText = cboGioHen.getValue();

            if (kh == null) throw new IllegalArgumentException("Vui lòng chọn khách hàng đặt lịch!");
            if (tc == null) throw new IllegalArgumentException("Vui lòng chọn thú cưng làm dịch vụ!");
            if (nv == null) throw new IllegalArgumentException("Vui lòng chọn nhân viên chăm sóc phụ trách!");
            if (ngay == null) throw new IllegalArgumentException("Vui lòng chọn ngày hẹn dịch vụ!");
            if (gioText == null || gioText.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng chọn giờ hẹn!");

            LocalTime gio = LocalTime.parse(gioText.trim());
            String ghiChu = txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : "";

            if (danhSachDichVuChon.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng thêm ít nhất một dịch vụ chăm sóc vào lịch hẹn!");
            }

            LichHen lh = new LichHen();
            lh.setMaLichHen(txtMaLichHen.getText().trim());
            lh.setMaKhachHang(kh.getMaKhachHang());
            lh.setMaThuCung(tc.getMaThuCung());
            lh.setMaNhanVien(nv[0]);
            lh.setNgayHen(ngay);
            lh.setGioHen(gio);
            lh.setTrangThai("Đã đặt");
            lh.setGhiChu(ghiChu);

            // Gán mã lịch hẹn vào từng chi tiết
            for (ChiTietLichHen ct : danhSachDichVuChon) {
                ct.setMaLichHen(lh.getMaLichHen());
            }

            boolean thanhCong = controller.datLichHen(lh, danhSachDichVuChon);
            if (thanhCong) {
                hienThiThongBao("Thành công", "Đã đặt lịch hẹn mã: " + lh.getMaLichHen() + " cho bé " + tc.getTenThuCung() + " thành công!");
                if (khiDatThanhCong != null) {
                    khiDatThanhCong.run();
                }
                dongCuaSo();
            } else {
                hienThiLoi("Không thể tạo lịch hẹn mới!");
            }

        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    // Làm mới toàn bộ form đặt lịch
    public void lamMoiForm() {
        try {
            txtMaLichHen.setText(controller.taoMaLichHenMoi());
        } catch (Exception e) {
            txtMaLichHen.setText("LH-" + System.currentTimeMillis());
        }

        // Tải danh sách khách hàng
        try {
            List<KhachHang> dsKh = controller.layDanhSachKhachHang();
            cboKhachHang.setItems(FXCollections.observableArrayList(dsKh));
        } catch (Exception ignored) {}

        // Tải danh sách nhân viên
        try {
            List<String[]> dsNv = controller.layDanhSachNhanVienChamSoc();
            cboNhanVien.setItems(FXCollections.observableArrayList(dsNv));
            if (!dsNv.isEmpty()) {
                cboNhanVien.setValue(dsNv.get(0));
            }
        } catch (Exception ignored) {}

        // Tải danh sách dịch vụ chăm sóc
        try {
            List<DichVuChamSoc> dsDv = controller.layDanhSachDichVu();
            cboDichVu.setItems(FXCollections.observableArrayList(dsDv));
        } catch (Exception ignored) {}

        cboThuCung.getItems().clear();
        dpNgayHen.setValue(LocalDate.now());
        cboGioHen.setValue("09:00");
        txtCanNangThuCung.clear();
        txtDonGia.clear();
        txtGhiChu.clear();
        danhSachDichVuChon.clear();
        capNhatTongTien();
    }

    // Tải danh sách thú cưng của khách hàng được chọn
    private void taiDanhSachThuCung(String maKhachHang) {
        try {
            List<ThuCung> ds = controller.layDanhSachThuCungTheoKhachHang(maKhachHang);
            cboThuCung.setItems(FXCollections.observableArrayList(ds));
            if (!ds.isEmpty()) {
                cboThuCung.setValue(ds.get(0));
            } else {
                cboThuCung.setValue(null);
                txtCanNangThuCung.clear();
            }
        } catch (Exception e) {
            cboThuCung.getItems().clear();
        }
    }

    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    // Phương thức tĩnh hiển thị nhanh DatLichHenView dạng hộp thoại Modal
    public static void hienThi(Window cuaSoCha, Runnable khiDatThanhCong) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (cuaSoCha != null) {
            dlg.initOwner(cuaSoCha);
        }
        dlg.setTitle("Đặt lịch hẹn chăm sóc thú cưng - Happy Tails");

        DatLichHenView view = new DatLichHenView(khiDatThanhCong);
        Scene scene = new Scene(view, 800, 660);

        if (DatLichHenView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(DatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        dlg.setScene(scene);
        dlg.setResizable(true);
        dlg.showAndWait();
    }
}

