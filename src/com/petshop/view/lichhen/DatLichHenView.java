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
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Đặt lịch hẹn dịch vụ chăm sóc thú cưng (APT_CREATE / LH_TAO).
 * Tối ưu phục vụ nhân viên trực tổng đài / Fanpage tiếp nhận cả khách quen lẫn khách mới.
 */
public class DatLichHenView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    private final LichHenController controller = new LichHenController();
    private final Runnable khiDatThanhCong;

    // Biến cờ xác định chế độ mở (dialog modal hay nhúng trên màn hình chính)
    private boolean laHopThoaiModal = false;

    // Danh sách khách hàng gốc để phục vụ tìm kiếm / lọc nhanh theo SĐT hoặc tên
    private final List<KhachHang> dsKhachHangGoc = new ArrayList<>();

    // Danh sách dịch vụ khách hàng chọn trong lịch hẹn
    private final ObservableList<ChiTietLichHen> danhSachDichVuChon = FXCollections.observableArrayList();

    // Các điều khiển nhập liệu thông tin lịch hẹn
    private final TextField txtMaLichHen = new TextField();
    private final TextField txtTimKhach = new TextField();
    private final ComboBox<KhachHang> cboKhachHang = new ComboBox<>();
    private final Button btnThemKhachNhanh = new Button("+ Mới");

    private final ComboBox<ThuCung> cboThuCung = new ComboBox<>();
    private final Button btnThemThuCungNhanh = new Button("+ Mới");

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
    private final Label lblTongTien = new Label("Tổng tiền dự kiến (tạm tính): 0 đ");

    // Các nút chức năng
    private final Button btnLuu = new Button("Xác nhận đặt lịch");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnDong = new Button("Đóng");

    public DatLichHenView() {
        this(null);
    }

    public DatLichHenView(Runnable khiDatThanhCong) {
        this.khiDatThanhCong = khiDatThanhCong;

        // Giảm nhẹ spacing và padding để không bị tràn đáy màn hình
        setSpacing(10);
        setPadding(new Insets(14, 20, 14, 20));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình
        VBox pnlTieuDe = new VBox(2);
        Label lblTieuDe = new Label("ĐẶT LỊCH HẸN DỊCH VỤ CHĂM SÓC");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tiếp nhận đặt lịch hẹn chăm sóc thú cưng cho khách hàng (chặn chọn ngày quá khứ).");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung thông tin lịch hẹn
        VBox pnlThongTinLich = taoKhungThongTinLich();

        // 3. Khung chọn dịch vụ chăm sóc
        VBox pnlChonDichVu = taoKhungChonDichVu();

        // Đưa phần nội dung nhập liệu vào ScrollPane để thanh nút luôn ghim chắc chắn ở đáy
        VBox pnlNoiDungCuon = new VBox(10);
        pnlNoiDungCuon.getChildren().addAll(pnlThongTinLich, pnlChonDichVu);

        ScrollPane spCuon = new ScrollPane(pnlNoiDungCuon);
        spCuon.setFitToWidth(true);
        spCuon.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        spCuon.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        spCuon.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        VBox.setVgrow(spCuon, Priority.ALWAYS);

        // 4. Thanh nút bấm điều khiển ghim cố định ở đáy màn hình
        HBox pnlNut = taoThanhNutBam();

        getChildren().addAll(pnlTieuDe, spCuon, pnlNut);

        // Đăng ký sự kiện tương tác giữa các trường
        dangKySuKien();

        // Nạp dữ liệu ban đầu
        lamMoiForm();
    }

    // Khởi tạo phần nhập thông tin khách, thú cưng, nhân viên và thời gian hẹn
    private VBox taoKhungThongTinLich() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblPhanTieuDe = new Label("1. Thông tin lịch hẹn");
        lblPhanTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(8);

        txtMaLichHen.setEditable(false);
        txtMaLichHen.setStyle("-fx-background-color: #f1f5f9; -fx-font-weight: bold;");

        // Tìm nhanh khách hàng theo SĐT / Họ tên + Chọn khách + Nút thêm nhanh
        txtTimKhach.setPromptText("Gõ SĐT hoặc tên để lọc...");
        txtTimKhach.setPrefWidth(155);

        cboKhachHang.setPromptText("Chọn khách hàng");
        cboKhachHang.setMaxWidth(Double.MAX_VALUE);

        btnThemKhachNhanh.getStyleClass().add("nut-phu");
        btnThemKhachNhanh.setTooltip(new Tooltip("Thêm nhanh khách hàng mới gọi đặt lịch"));
        btnThemKhachNhanh.setOnAction(e -> moCuaSoThemKhachNhanh());

        HBox pnlChonKhach = new HBox(6);
        pnlChonKhach.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cboKhachHang, Priority.ALWAYS);
        pnlChonKhach.getChildren().addAll(txtTimKhach, cboKhachHang, btnThemKhachNhanh);

        // Chọn thú cưng + Nút thêm nhanh thú cưng
        cboThuCung.setPromptText("Chọn thú cưng");
        cboThuCung.setMaxWidth(Double.MAX_VALUE);

        btnThemThuCungNhanh.getStyleClass().add("nut-phu");
        btnThemThuCungNhanh.setTooltip(new Tooltip("Thêm nhanh thú cưng cho khách đang chọn"));
        btnThemThuCungNhanh.setOnAction(e -> moCuaSoThemThuCungNhanh());

        HBox pnlChonThuCung = new HBox(6);
        pnlChonThuCung.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(cboThuCung, Priority.ALWAYS);
        pnlChonThuCung.getChildren().addAll(cboThuCung, btnThemThuCungNhanh);

        cboNhanVien.setPromptText("Chọn nhân viên chăm sóc");
        cboNhanVien.setMaxWidth(Double.MAX_VALUE);

        // Cấu hình DatePicker định dạng dd/MM/yyyy và chặn ngày quá khứ
        dpNgayHen.setPromptText("dd/MM/yyyy");
        dpNgayHen.setPrefWidth(145);
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
        cboGioHen.setPrefWidth(115);

        // Khắc phục nhãn "Giờ:" bị cắt chữ bằng cách đặt setMinWidth(USE_PREF_SIZE)
        Label lblGio = new Label("Giờ:");
        lblGio.setMinWidth(Region.USE_PREF_SIZE);

        HBox pnlThoiGian = new HBox(8);
        pnlThoiGian.setAlignment(Pos.CENTER_LEFT);
        pnlThoiGian.getChildren().addAll(dpNgayHen, lblGio, cboGioHen);

        txtGhiChu.setPromptText("Ghi chú thêm (yêu cầu riêng của khách hàng, lưu ý thú cưng nhát nước...)");
        txtGhiChu.setPrefRowCount(2);
        txtGhiChu.setWrapText(true);

        // Đưa vào lưới layout
        pnlLuoi.add(new Label("Mã lịch hẹn:"), 0, 0);
        pnlLuoi.add(txtMaLichHen, 1, 0);
        pnlLuoi.add(new Label("Nhân viên phụ trách (*):"), 2, 0);
        pnlLuoi.add(cboNhanVien, 3, 0);

        pnlLuoi.add(new Label("Khách hàng (*):"), 0, 1);
        pnlLuoi.add(pnlChonKhach, 1, 1);
        pnlLuoi.add(new Label("Thú cưng (*):"), 2, 1);
        pnlLuoi.add(pnlChonThuCung, 3, 1);

        pnlLuoi.add(new Label("Ngày hẹn (*):"), 0, 2);
        pnlLuoi.add(pnlThoiGian, 1, 2);
        pnlLuoi.add(new Label("Ghi chú:"), 2, 2);
        pnlLuoi.add(txtGhiChu, 3, 2);

        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(135);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3, col4);

        pnlKhung.getChildren().addAll(lblPhanTieuDe, pnlLuoi);
        return pnlKhung;
    }

    // Khởi tạo phần chọn dịch vụ chăm sóc và bảng chi tiết dịch vụ
    private VBox taoKhungChonDichVu() {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        Label lblPhanTieuDe = new Label("2. Dịch vụ chăm sóc đăng ký");
        lblPhanTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        // Thanh công cụ chọn dịch vụ
        HBox pnlChon = new HBox(10);
        pnlChon.setAlignment(Pos.CENTER_LEFT);

        cboDichVu.setPromptText("Chọn dịch vụ chăm sóc");
        cboDichVu.setPrefWidth(240);

        // Đổi promptText ngắn gọn không bị che khuất
        txtCanNangThuCung.setPromptText("Để trống = 3kg");
        txtCanNangThuCung.setPrefWidth(120);

        txtDonGia.setPromptText("Đơn giá");
        txtDonGia.setEditable(false);
        txtDonGia.setPrefWidth(135);
        txtDonGia.setStyle("-fx-background-color: #f1f5f9; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");

        btnThemDichVu.getStyleClass().add("nut-phu");

        pnlChon.getChildren().addAll(
                new Label("Dịch vụ:"), cboDichVu,
                new Label("Cân nặng (kg):"), txtCanNangThuCung,
                new Label("Đơn giá:"), txtDonGia,
                btnThemDichVu
        );

        // Bảng danh sách dịch vụ chi tiết đã thêm (giới hạn chiều cao vừa vặn ~110px)
        tblDichVu.getStyleClass().add("bang-du-lieu");
        tblDichVu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblDichVu.setPrefHeight(110);
        tblDichVu.setMinHeight(95);
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

        TableColumn<ChiTietLichHen, String> colCanNang = new TableColumn<>("Cân nặng (ước tính)");
        colCanNang.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCanNangThuCung() != null ? c.getValue().getCanNangThuCung() + " kg" : "3.0 kg"
        ));
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

        // Thanh tổng tiền bên dưới bảng kèm lưu ý về đơn giá
        VBox pnlTongTien = new VBox(2);
        pnlTongTien.setAlignment(Pos.CENTER_RIGHT);
        lblTongTien.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        Label lblLuuY = new Label("(*) Đơn giá tạm tính theo mức chuẩn (bậc Nhỏ nếu chưa rõ cân nặng). Khi khách mang thú cưng đến quầy, nhân viên tiếp nhận sẽ cân lại và áp dụng đơn giá chính thức.");
        lblLuuY.setStyle("-fx-font-style: italic; -fx-text-fill: #64748b; -fx-font-size: 11.5px;");
        pnlTongTien.getChildren().addAll(lblTongTien, lblLuuY);

        pnlKhung.getChildren().addAll(lblPhanTieuDe, pnlChon, tblDichVu, pnlTongTien);
        return pnlKhung;
    }

    // Khởi tạo thanh nút thao tác dưới chân màn hình
    private HBox taoThanhNutBam() {
        HBox pnlNut = new HBox(12);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        pnlNut.setStyle("-fx-padding: 8 0 2 0; -fx-border-color: #cbd5e1 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Label lblNhacNho = new Label("💡 Bước cuối: Bấm nút '✔ Xác nhận đặt lịch' bên cạnh để lưu vào hệ thống");
        lblNhacNho.setStyle("-fx-text-fill: #0284c7; -fx-font-size: 12.5px; -fx-font-weight: bold;");

        Region khoangCach = new Region();
        HBox.setHgrow(khoangCach, Priority.ALWAYS);

        btnLuu.setText("✔ Xác nhận đặt lịch");
        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-padding: 8 22;");

        btnLamMoi.getStyleClass().add("nut-phu");
        btnDong.getStyleClass().add("nut-phu");

        btnLuu.setOnAction(e -> xuLyDatLich());
        btnLamMoi.setOnAction(e -> lamMoiForm());
        btnDong.setOnAction(e -> dongCuaSo());

        // Mặc định ẩn nút Đóng nếu nhúng trên màn hình chính
        btnDong.setVisible(false);
        btnDong.setManaged(false);

        pnlNut.getChildren().addAll(lblNhacNho, khoangCach, btnLamMoi, btnDong, btnLuu);
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

        // Tìm nhanh khách hàng theo SĐT hoặc Họ tên khi gõ vào ô txtTimKhach
        txtTimKhach.textProperty().addListener((obs, cu, tuKhoa) -> {
            if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
                cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
                return;
            }
            String tk = tuKhoa.trim().toLowerCase();
            List<KhachHang> dsLoc = new ArrayList<>();
            for (KhachHang kh : dsKhachHangGoc) {
                boolean khopTen = kh.getHoTen() != null && kh.getHoTen().toLowerCase().contains(tk);
                boolean khopSdt = kh.getSoDienThoai() != null && kh.getSoDienThoai().contains(tk);
                boolean khopMa = kh.getMaKhachHang() != null && kh.getMaKhachHang().toLowerCase().contains(tk);
                if (khopTen || khopSdt || khopMa) {
                    dsLoc.add(kh);
                }
            }
            cboKhachHang.setItems(FXCollections.observableArrayList(dsLoc));
            if (!dsLoc.isEmpty()) {
                cboKhachHang.setValue(dsLoc.get(0));
            } else {
                cboKhachHang.setValue(null);
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

        // Tùy biến hiển thị ComboBox Thú cưng (Tên - Loại thú cưng ví dụ: Chó, Mèo...)
        cboThuCung.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThuCung tc) {
                if (tc == null) return "";
                String loai = tc.getLoaiThuCung() != null ? tc.getLoaiThuCung() : "Thú cưng";
                String giong = tc.getGiongLoai() != null && !tc.getGiongLoai().isEmpty() ? " - " + tc.getGiongLoai() : "";
                String canNangStr = (tc.getCanNang() != null && tc.getCanNang().compareTo(BigDecimal.ZERO) > 0)
                        ? " (" + tc.getCanNang().stripTrailingZeros().toPlainString() + " kg)"
                        : " (chưa rõ cân nặng)";
                return tc.getTenThuCung() + " [" + loai + giong + "]" + canNangStr;
            }
            @Override
            public ThuCung fromString(String s) {
                return null;
            }
        });

        // Khi chọn thú cưng: điền cân nặng và tự động chèn cảnh báo Dị ứng vào ghi chú
        cboThuCung.setOnAction(e -> {
            ThuCung tc = cboThuCung.getValue();
            if (tc != null && tc.getCanNang() != null && tc.getCanNang().compareTo(BigDecimal.ZERO) > 0) {
                txtCanNangThuCung.setText(tc.getCanNang().stripTrailingZeros().toPlainString());
            } else {
                txtCanNangThuCung.clear();
            }
            tinhDonGiaDichVu();

            // Tự động nhắc lưu ý Dị ứng của thú cưng quen
            if (tc != null && tc.getDiUng() != null && !tc.getDiUng().trim().isEmpty() && !"Không có".equalsIgnoreCase(tc.getDiUng().trim())) {
                String luuY = "[Lưu ý dị ứng: " + tc.getDiUng().trim() + "] ";
                String ghiChuHienTai = txtGhiChu.getText() != null ? txtGhiChu.getText() : "";
                if (!ghiChuHienTai.contains(luuY)) {
                    txtGhiChu.setText(luuY + ghiChuHienTai);
                }
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

        // Tự động tính lại đơn giá khi đổi dịch vụ hoặc cân nặng
        cboDichVu.setOnAction(e -> tinhDonGiaDichVu());
        txtCanNangThuCung.textProperty().addListener((obs, cu, moi) -> tinhDonGiaDichVu());

        // Bấm nút thêm dịch vụ vào bảng
        btnThemDichVu.setOnAction(e -> xuLyThemDichVuVaoBang());
    }

    // Tự động tính đơn giá dịch vụ (ước tính theo cân nặng hoặc mức chuẩn nếu chưa rõ cân nặng)
    private void tinhDonGiaDichVu() {
        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) {
            txtDonGia.clear();
            return;
        }

        String chuoiCanNang = txtCanNangThuCung.getText() != null ? txtCanNangThuCung.getText().trim() : "";
        double canNang = 3.0; // Mức ước tính chuẩn (bậc Nhỏ)
        boolean laUocTinh = true;

        if (!chuoiCanNang.isEmpty()) {
            try {
                double cn = Double.parseDouble(chuoiCanNang);
                if (cn > 0) {
                    canNang = cn;
                    laUocTinh = false;
                }
            } catch (NumberFormatException ignored) {}
        } else {
            ThuCung tc = cboThuCung.getValue();
            if (tc != null && tc.getCanNang() != null && tc.getCanNang().compareTo(BigDecimal.ZERO) > 0) {
                canNang = tc.getCanNang().doubleValue();
                laUocTinh = false;
            }
        }

        double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), canNang);
        txtDonGia.setText(DINH_DANG_TIEN.format(gia) + (laUocTinh ? " (tạm tính)" : ""));
    }

    // Thêm dịch vụ đang chọn vào danh sách chi tiết của lịch hẹn
    private void xuLyThemDichVuVaoBang() {
        DichVuChamSoc dv = cboDichVu.getValue();
        if (dv == null) {
            hienThiLoi("Vui lòng chọn dịch vụ chăm sóc!");
            return;
        }

        // Kiểm tra xem dịch vụ này đã có trong bảng chưa
        for (ChiTietLichHen ct : danhSachDichVuChon) {
            if (ct.getMaDichVu().equals(dv.getMaDichVu())) {
                hienThiLoi("Dịch vụ '" + dv.getTenDichVu() + "' đã được thêm vào lịch hẹn!");
                return;
            }
        }

        String chuoiCanNang = txtCanNangThuCung.getText() != null ? txtCanNangThuCung.getText().trim() : "";
        BigDecimal canNang;

        if (chuoiCanNang.isEmpty()) {
            ThuCung tc = cboThuCung.getValue();
            if (tc != null && tc.getCanNang() != null && tc.getCanNang().compareTo(BigDecimal.ZERO) > 0) {
                canNang = tc.getCanNang();
            } else {
                canNang = BigDecimal.valueOf(3.0); // Mức ước tính mặc định khi khách đặt trước qua FB/điện thoại chưa báo cân nặng
            }
        } else {
            try {
                canNang = new BigDecimal(chuoiCanNang);
                if (canNang.compareTo(BigDecimal.ZERO) <= 0) {
                    canNang = BigDecimal.valueOf(3.0);
                }
            } catch (Exception e) {
                canNang = BigDecimal.valueOf(3.0);
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

    // Cập nhật nhãn tổng tiền tạm tính
    private void capNhatTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietLichHen ct : danhSachDichVuChon) {
            if (ct.getDonGia() != null) {
                tong = tong.add(ct.getDonGia());
            }
        }
        lblTongTien.setText("Tổng tiền dự kiến (tạm tính): " + DINH_DANG_TIEN.format(tong));
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
                hienThiThongBao("Đặt lịch thành công",
                        "✔ Đã đặt lịch hẹn mã: " + lh.getMaLichHen() + " cho bé " + tc.getTenThuCung() + " thành công!\n"
                        + "Lịch hẹn đã được lưu vào cơ sở dữ liệu. Bạn có thể chuyển sang màn hình 'Cập nhật lịch hẹn' hoặc 'Tìm kiếm lịch hẹn' để theo dõi.");
                if (khiDatThanhCong != null) {
                    khiDatThanhCong.run();
                }
                // Nếu là cửa sổ modal thì mới đóng, ngược lại (trên màn hình chính) thì làm mới form
                if (laHopThoaiModal) {
                    dongCuaSo();
                } else {
                    lamMoiForm();
                }
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
            String ngayChuoi = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            txtMaLichHen.setText("LH-" + ngayChuoi + "-000001");
        }

        // Tải danh sách khách hàng và lưu vào danh sách gốc để phục vụ tìm kiếm
        try {
            List<KhachHang> dsKh = controller.layDanhSachKhachHang();
            dsKhachHangGoc.clear();
            dsKhachHangGoc.addAll(dsKh);
            cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
            txtTimKhach.clear();
        } catch (Exception ignored) {}

        // Tải danh sách nhân viên - ưu tiên chọn chức vụ "Chăm sóc"
        try {
            List<String[]> dsNv = controller.layDanhSachNhanVienChamSoc();
            cboNhanVien.setItems(FXCollections.observableArrayList(dsNv));
            String[] nvMacDinh = null;
            for (String[] nv : dsNv) {
                if (nv.length > 2 && "Chăm sóc".equalsIgnoreCase(nv[2].trim())) {
                    nvMacDinh = nv;
                    break;
                }
            }
            if (nvMacDinh != null) {
                cboNhanVien.setValue(nvMacDinh);
            } else if (!dsNv.isEmpty()) {
                cboNhanVien.setValue(dsNv.get(0));
            }
        } catch (Exception ignored) {}

        // Tải danh sách dịch vụ chăm sóc
        try {
            List<DichVuChamSoc> dsDv = controller.layDanhSachDichVu();
            cboDichVu.setItems(FXCollections.observableArrayList(dsDv));
        } catch (Exception ignored) {}

        cboThuCung.getItems().clear();

        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(19, 30))) {
            dpNgayHen.setValue(LocalDate.now().plusDays(1));
            cboGioHen.setValue("09:00");
        } else {
            dpNgayHen.setValue(LocalDate.now());
            String gioPhuHop = "09:00";
            for (String g : cboGioHen.getItems()) {
                try {
                    LocalTime t = LocalTime.parse(g);
                    if (t.isAfter(now.plusMinutes(15))) {
                        gioPhuHop = g;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            cboGioHen.setValue(gioPhuHop);
        }

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

    // Hộp thoại thêm nhanh khách hàng mới khi khách lạ gọi điện/nhắn FB đặt lịch lần đầu
    private void moCuaSoThemKhachNhanh() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlg.initOwner(getScene().getWindow());
        }
        dlg.setTitle("Thêm nhanh khách hàng mới");

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("THÊM NHANH KHÁCH HÀNG MỚI");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #0284c7;");

        GridPane luoi = new GridPane();
        luoi.setHgap(10);
        luoi.setVgap(10);

        TextField txtHoTenMoi = new TextField();
        txtHoTenMoi.setPromptText("Họ và tên khách hàng (*)");

        TextField txtSdtMoi = new TextField();
        txtSdtMoi.setPromptText("Số điện thoại (10 số, bắt đầu bằng 0) (*)");

        TextField txtDiaChiMoi = new TextField();
        txtDiaChiMoi.setPromptText("Địa chỉ (tùy chọn)");

        luoi.add(new Label("Họ tên (*):"), 0, 0);
        luoi.add(txtHoTenMoi, 1, 0);
        luoi.add(new Label("Số điện thoại (*):"), 0, 1);
        luoi.add(txtSdtMoi, 1, 1);
        luoi.add(new Label("Địa chỉ:"), 0, 2);
        luoi.add(txtDiaChiMoi, 1, 2);

        HBox pnlNutDlg = new HBox(8);
        pnlNutDlg.setAlignment(Pos.CENTER_RIGHT);

        Button btnLuuKhach = new Button("Lưu khách hàng");
        btnLuuKhach.getStyleClass().add("nut-chinh");

        Button btnHuyDlg = new Button("Hủy");
        btnHuyDlg.getStyleClass().add("nut-phu");
        btnHuyDlg.setOnAction(ev -> dlg.close());

        btnLuuKhach.setOnAction(ev -> {
            String hoTen = txtHoTenMoi.getText() != null ? txtHoTenMoi.getText().trim() : "";
            String sdt = txtSdtMoi.getText() != null ? txtSdtMoi.getText().trim() : "";
            String diaChi = txtDiaChiMoi.getText() != null ? txtDiaChiMoi.getText().trim() : "";

            if (hoTen.isEmpty()) {
                hienThiLoi("Họ tên khách hàng không được để trống!");
                return;
            }
            if (!sdt.matches("^0\\d{9}$")) {
                hienThiLoi("Số điện thoại không hợp lệ! Vui lòng nhập đúng 10 chữ số bắt đầu bằng 0.");
                return;
            }
            if (controller.kiemTraTrungSoDienThoai(sdt)) {
                hienThiLoi("Số điện thoại này đã tồn tại trong hệ thống!");
                return;
            }

            try {
                String maKh = controller.taoMaKhachHang();
                KhachHang khMoi = new KhachHang(maKh, hoTen, sdt, null, 0, diaChi, "Đặt qua điện thoại/FB", true);
                boolean ok = controller.themKhachHang(khMoi);
                if (ok) {
                    dsKhachHangGoc.add(0, khMoi);
                    cboKhachHang.setItems(FXCollections.observableArrayList(dsKhachHangGoc));
                    cboKhachHang.setValue(khMoi);
                    txtTimKhach.clear();
                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm nhanh khách hàng " + hoTen + " (" + sdt + ")!");
                } else {
                    hienThiLoi("Không thể thêm khách hàng mới!");
                }
            } catch (Exception ex) {
                hienThiLoi(ex.getMessage());
            }
        });

        pnlNutDlg.getChildren().addAll(btnLuuKhach, btnHuyDlg);
        pnlGoc.getChildren().addAll(lblTieuDe, luoi, pnlNutDlg);

        Scene scene = new Scene(pnlGoc, 420, 220);
        if (DatLichHenView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(DatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    // Hộp thoại thêm nhanh thú cưng khi khách lạ đặt lịch chưa có hồ sơ thú cưng
    private void moCuaSoThemThuCungNhanh() {
        KhachHang kh = cboKhachHang.getValue();
        if (kh == null) {
            hienThiLoi("Vui lòng chọn khách hàng trước khi thêm thú cưng!");
            return;
        }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlg.initOwner(getScene().getWindow());
        }
        dlg.setTitle("Thêm nhanh thú cưng cho khách: " + kh.getHoTen());

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("THÊM NHANH THÚ CƯNG (" + kh.getHoTen() + ")");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #0284c7;");

        GridPane luoi = new GridPane();
        luoi.setHgap(10);
        luoi.setVgap(10);

        TextField txtTenTc = new TextField();
        txtTenTc.setPromptText("Tên thú cưng (*)");

        ComboBox<String> cboLoaiTc = new ComboBox<>(FXCollections.observableArrayList("Chó", "Mèo", "Khác"));
        cboLoaiTc.setValue("Chó");

        TextField txtGiongLoai = new TextField();
        txtGiongLoai.setPromptText("VD: Poodle, Corgi, Ba Tư... (tùy chọn)");

        ComboBox<String> cboGioiTinh = new ComboBox<>(FXCollections.observableArrayList("Đực", "Cái"));
        cboGioiTinh.setValue("Đực");

        TextField txtCanNangTc = new TextField("3.0");
        txtCanNangTc.setPromptText("Mặc định 3.0 kg nếu chưa cân");

        TextField txtDiUngTc = new TextField();
        txtDiUngTc.setPromptText("Lưu ý dị ứng, nhát nước, dữ... (tùy chọn)");

        luoi.add(new Label("Tên thú cưng (*):"), 0, 0);
        luoi.add(txtTenTc, 1, 0);
        luoi.add(new Label("Loài:"), 0, 1);
        luoi.add(cboLoaiTc, 1, 1);
        luoi.add(new Label("Giống loài:"), 0, 2);
        luoi.add(txtGiongLoai, 1, 2);
        luoi.add(new Label("Giới tính:"), 0, 3);
        luoi.add(cboGioiTinh, 1, 3);
        luoi.add(new Label("Cân nặng (kg):"), 0, 4);
        luoi.add(txtCanNangTc, 1, 4);
        luoi.add(new Label("Dị ứng / Lưu ý:"), 0, 5);
        luoi.add(txtDiUngTc, 1, 5);

        HBox pnlNutDlg = new HBox(8);
        pnlNutDlg.setAlignment(Pos.CENTER_RIGHT);

        Button btnLuuTc = new Button("Lưu thú cưng");
        btnLuuTc.getStyleClass().add("nut-chinh");

        Button btnHuyDlg = new Button("Hủy");
        btnHuyDlg.getStyleClass().add("nut-phu");
        btnHuyDlg.setOnAction(ev -> dlg.close());

        btnLuuTc.setOnAction(ev -> {
            String ten = txtTenTc.getText() != null ? txtTenTc.getText().trim() : "";
            if (ten.isEmpty()) {
                hienThiLoi("Tên thú cưng không được để trống!");
                return;
            }
            String loai = cboLoaiTc.getValue();
            String giong = txtGiongLoai.getText() != null ? txtGiongLoai.getText().trim() : "";
            String gioiTinh = cboGioiTinh.getValue();
            String diUng = txtDiUngTc.getText() != null ? txtDiUngTc.getText().trim() : "";

            BigDecimal cn;
            try {
                String cnStr = txtCanNangTc.getText() != null ? txtCanNangTc.getText().trim() : "";
                if (!cnStr.isEmpty()) {
                    cn = new BigDecimal(cnStr);
                    if (cn.compareTo(BigDecimal.ZERO) <= 0) {
                        cn = BigDecimal.valueOf(3.0);
                    }
                } else {
                    cn = BigDecimal.valueOf(3.0);
                }
            } catch (Exception ex) {
                cn = BigDecimal.valueOf(3.0);
            }

            try {
                String maTc = controller.taoMaThuCung();
                ThuCung tcMoi = new ThuCung(maTc, kh.getMaKhachHang(), ten, loai, giong, cn, null, diUng, gioiTinh);
                boolean ok = controller.themThuCung(tcMoi);
                if (ok) {
                    taiDanhSachThuCung(kh.getMaKhachHang());
                    cboThuCung.setValue(tcMoi);
                    txtCanNangThuCung.setText(cn.stripTrailingZeros().toPlainString());
                    dlg.close();
                    hienThiThongBao("Thành công", "Đã thêm nhanh thú cưng " + ten + " cho khách " + kh.getHoTen() + "!");
                } else {
                    hienThiLoi("Không thể thêm thú cưng mới!");
                }
            } catch (Exception ex) {
                hienThiLoi(ex.getMessage());
            }
        });

        pnlNutDlg.getChildren().addAll(btnLuuTc, btnHuyDlg);
        pnlGoc.getChildren().addAll(lblTieuDe, luoi, pnlNutDlg);

        Scene scene = new Scene(pnlGoc, 440, 320);
        if (DatLichHenView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(DatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    private void dongCuaSo() {
        if (laHopThoaiModal && getScene() != null && getScene().getWindow() instanceof Stage stage) {
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
        view.laHopThoaiModal = true;
        view.btnDong.setVisible(true);
        view.btnDong.setManaged(true);

        Scene scene = new Scene(view, 800, 660);

        if (DatLichHenView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(DatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        dlg.setScene(scene);
        dlg.setResizable(true);
        dlg.showAndWait();
    }
}
