package com.petshop.view.catruc;

import com.petshop.controller.CaTrucController;
import com.petshop.model.Ca;
import com.petshop.model.NhanVien;
import com.petshop.model.PhanCa;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Màn hình tra cứu và xem lịch ca trực theo dạng Thời khóa biểu tuần (Weekly Timetable).
 * Hỗ trợ màu sắc riêng biệt cho từng nhân viên, chuyển tuần, lọc theo nhân viên,
 * và hiển thị chi tiết ca trực kèm thao tác hủy ca có xác nhận.
 */
public class TimKiemCaTrucView extends VBox {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final CaTrucController caTrucController = new CaTrucController();

    // Điều khiển trên thanh công cụ
    private final ComboBox<String> cboNhanVien = new ComboBox<>();
    private final DatePicker dpChonNgay = new DatePicker(LocalDate.now());
    private final Button btnTuan = new Button("Tuần");
    private final Button btnThang = new Button("Tháng");

    private final Button btnTuanTruoc = new Button("<");
    private final Button btnTuanSau = new Button(">");
    private final Label lblKhoangNgay = new Label();
    private final Label lblThongKe = new Label();

    // Khung lịch và bảng chú thích màu
    private final VBox pnlLichContainer = new VBox();
    private final HBox pnlChuThichMau = new HBox(16);

    private LocalDate ngayDauTuanHienTai;
    private boolean cheDoXemTuan = true;

    private List<NhanVien> danhSachNhanVienGoc = new ArrayList<>();
    private final Map<String, MauSacNhanVien> bangMauNhanVien = new HashMap<>();

    // Bảng bảng màu dịu mắt (Pastel) với viền trái nổi bật chuẩn thiết kế
    private static final List<MauSacNhanVien> DANH_SACH_MAU = List.of(
            new MauSacNhanVien("#15803d", "#eafaf1", "#14532d", "#166534"), // Xanh lá (Lê Thị Hoa)
            new MauSacNhanVien("#2563eb", "#edf4fe", "#1e3a8a", "#1d4ed8"), // Xanh dương (Trần Minh Khang)
            new MauSacNhanVien("#7c3aed", "#f5edfd", "#581c87", "#6b21a8"), // Tím (Phạm Ngọc Linh)
            new MauSacNhanVien("#ea580c", "#fff4ed", "#9a3412", "#c2410c"), // Cam
            new MauSacNhanVien("#0d9488", "#edfdfb", "#134e4a", "#0f766e"), // Xanh ngọc
            new MauSacNhanVien("#e11d48", "#ffedf2", "#881337", "#be123c")  // Hồng cánh sen
    );

    public TimKiemCaTrucView() {
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // Xác định thứ Hai của tuần hiện tại
        LocalDate homNay = LocalDate.now();
        ngayDauTuanHienTai = homNay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        VBox pnlKhungChinh = new VBox(14);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        pnlKhungChinh.setPadding(new Insets(16, 20, 20, 20));
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlThanhDieuKhienTren = taoThanhDieuKhienTren();
        HBox pnlThanhChuyenTuan = taoThanhChuyenTuan();

        ScrollPane scrCuonLich = new ScrollPane(pnlLichContainer);
        scrCuonLich.setFitToWidth(true);
        scrCuonLich.setStyle("-fx-background-color: transparent; -fx-background: white; -fx-border-color: transparent;");
        VBox.setVgrow(scrCuonLich, Priority.ALWAYS);

        pnlChuThichMau.setAlignment(Pos.CENTER_LEFT);
        pnlChuThichMau.setPadding(new Insets(8, 4, 4, 4));

        pnlKhungChinh.getChildren().addAll(
                pnlThanhDieuKhienTren,
                pnlThanhChuyenTuan,
                scrCuonLich,
                pnlChuThichMau
        );

        getChildren().add(pnlKhungChinh);

        napDanhSachNhanVien();
        taiLaiLichCaTruc();
    }

    private HBox taoThanhDieuKhienTren() {
        HBox pnlThanhTren = new HBox(20);
        pnlThanhTren.setAlignment(Pos.CENTER_LEFT);

        VBox pnlLocNV = new VBox(4);
        Label lblTieuDeNV = new Label("Lọc theo nhân viên");
        lblTieuDeNV.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        cboNhanVien.setPrefWidth(240);
        cboNhanVien.setOnAction(e -> taiLaiLichCaTruc());
        pnlLocNV.getChildren().addAll(lblTieuDeNV, cboNhanVien);

        VBox pnlChonNgay = new VBox(4);
        Label lblTieuDeNgay = new Label("Chọn ngày");
        lblTieuDeNgay.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        dpChonNgay.setPrefWidth(180);
        dpChonNgay.setOnAction(e -> {
            LocalDate ngayChon = dpChonNgay.getValue();
            if (ngayChon != null) {
                ngayDauTuanHienTai = ngayChon.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                taiLaiLichCaTruc();
            }
        });
        pnlChonNgay.getChildren().addAll(lblTieuDeNgay, dpChonNgay);

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        HBox pnlNutCheDo = new HBox(0);
        pnlNutCheDo.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8px; -fx-padding: 3px;");
        pnlNutCheDo.setAlignment(Pos.CENTER);

        capNhatStyleNutCheDo(btnTuan, true);
        capNhatStyleNutCheDo(btnThang, false);

        btnTuan.setOnAction(e -> {
            cheDoXemTuan = true;
            capNhatStyleNutCheDo(btnTuan, true);
            capNhatStyleNutCheDo(btnThang, false);
            taiLaiLichCaTruc();
        });

        btnThang.setOnAction(e -> {
            cheDoXemTuan = false;
            capNhatStyleNutCheDo(btnTuan, false);
            capNhatStyleNutCheDo(btnThang, true);
            taiLaiLichCaTruc();
        });

        pnlNutCheDo.getChildren().addAll(btnTuan, btnThang);

        pnlThanhTren.getChildren().addAll(pnlLocNV, pnlChonNgay, pnlKhoangTrong, pnlNutCheDo);
        return pnlThanhTren;
    }

    private void capNhatStyleNutCheDo(Button btn, boolean dangChon) {
        if (dangChon) {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: #111827; -fx-font-weight: bold; "
                    + "-fx-background-radius: 6px; -fx-padding: 6 18; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 3, 0, 0, 1);");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-weight: normal; "
                    + "-fx-background-radius: 6px; -fx-padding: 6 18; -fx-cursor: hand;");
        }
    }

    private HBox taoThanhChuyenTuan() {
        HBox pnlThanh = new HBox(12);
        pnlThanh.setAlignment(Pos.CENTER_LEFT);
        pnlThanh.setPadding(new Insets(6, 0, 8, 0));

        btnTuanTruoc.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 6px; "
                + "-fx-background-radius: 6px; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand;");
        btnTuanSau.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 6px; "
                + "-fx-background-radius: 6px; -fx-font-weight: bold; -fx-padding: 5 12; -fx-cursor: hand;");

        btnTuanTruoc.setOnAction(e -> {
            if (cheDoXemTuan) {
                ngayDauTuanHienTai = ngayDauTuanHienTai.minusWeeks(1);
            } else {
                ngayDauTuanHienTai = ngayDauTuanHienTai.minusMonths(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            }
            dpChonNgay.setValue(ngayDauTuanHienTai);
            taiLaiLichCaTruc();
        });

        btnTuanSau.setOnAction(e -> {
            if (cheDoXemTuan) {
                ngayDauTuanHienTai = ngayDauTuanHienTai.plusWeeks(1);
            } else {
                ngayDauTuanHienTai = ngayDauTuanHienTai.plusMonths(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            }
            dpChonNgay.setValue(ngayDauTuanHienTai);
            taiLaiLichCaTruc();
        });

        lblKhoangNgay.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        lblThongKe.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");

        pnlThanh.getChildren().addAll(btnTuanTruoc, btnTuanSau, lblKhoangNgay, pnlKhoangTrong, lblThongKe);
        return pnlThanh;
    }

    private void napDanhSachNhanVien() {
        try {
            danhSachNhanVienGoc = caTrucController.layDanhSachNhanVienDangLamViec();
            List<String> tenNV = new ArrayList<>();
            tenNV.add("Tất cả nhân viên");
            for (NhanVien nv : danhSachNhanVienGoc) {
                tenNV.add(nv.getHoTen());
            }
            cboNhanVien.getItems().setAll(tenNV);
            cboNhanVien.setValue("Tất cả nhân viên");

            // Khởi tạo màu cố định cho từng nhân viên
            bangMauNhanVien.clear();
            for (int i = 0; i < danhSachNhanVienGoc.size(); i++) {
                NhanVien nv = danhSachNhanVienGoc.get(i);
                bangMauNhanVien.put(nv.getMaNhanVien(), DANH_SACH_MAU.get(i % DANH_SACH_MAU.size()));
            }
        } catch (Exception ignored) {
        }
    }

    private MauSacNhanVien layMauChoNhanVien(String maNhanVien, String tenNhanVien) {
        if (maNhanVien != null && bangMauNhanVien.containsKey(maNhanVien)) {
            return bangMauNhanVien.get(maNhanVien);
        }
        // Fallback theo hashcode tên để màu luôn cố định không đổi
        int hash = Math.abs((tenNhanVien != null ? tenNhanVien : "NV").hashCode());
        return DANH_SACH_MAU.get(hash % DANH_SACH_MAU.size());
    }

    private void taiLaiLichCaTruc() {
        if (cheDoXemTuan) {
            hienThiLichTuan();
        } else {
            hienThiLichThang();
        }
    }

    private void hienThiLichTuan() {
        LocalDate thuHai = ngayDauTuanHienTai;
        LocalDate chuNhat = thuHai.plusDays(6);

        // Cập nhật tiêu đề khoảng ngày: "19–25 tháng 10, 2026"
        if (thuHai.getMonthValue() == chuNhat.getMonthValue()) {
            lblKhoangNgay.setText(String.format("%d–%d tháng %d, %d",
                    thuHai.getDayOfMonth(), chuNhat.getDayOfMonth(), thuHai.getMonthValue(), thuHai.getYear()));
        } else {
            lblKhoangNgay.setText(String.format("%d/%d – %d/%d, %d",
                    thuHai.getDayOfMonth(), thuHai.getMonthValue(), chuNhat.getDayOfMonth(), chuNhat.getMonthValue(), chuNhat.getYear()));
        }

        // Tải danh sách phân ca trong tuần từ CSDL
        List<PhanCa> danhSachTatCa = caTrucController.timKiemPhanCa(null, null, null, PhanCa.TRANG_THAI_DA_PHAN_CONG);
        List<PhanCa> danhSachTuan = new ArrayList<>();

        String tenNVLoc = cboNhanVien.getValue();
        boolean locNV = tenNVLoc != null && !"Tất cả nhân viên".equalsIgnoreCase(tenNVLoc);

        Set<String> tapNhanVien = new HashSet<>();
        for (PhanCa pc : danhSachTatCa) {
            LocalDate ngay = pc.getNgayLam();
            if (ngay != null && !ngay.isBefore(thuHai) && !ngay.isAfter(chuNhat)) {
                if (!locNV || tenNVLoc.equalsIgnoreCase(pc.getTenNhanVien())) {
                    danhSachTuan.add(pc);
                    tapNhanVien.add(pc.getMaNhanVien());
                }
            }
        }

        lblThongKe.setText(danhSachTuan.size() + " ca trực · " + tapNhanVien.size() + " nhân viên");

        // Vẽ Grid Thời khóa biểu tuần
        GridPane grid = new GridPane();
        grid.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px;");
        grid.setGridLinesVisible(false);

        // Thiết lập kích thước các cột: Cột 0 (Giờ) rộng 75px, 7 cột còn lại chia đều
        ColumnConstraints colGio = new ColumnConstraints();
        colGio.setMinWidth(75);
        colGio.setPrefWidth(75);
        grid.getColumnConstraints().add(colGio);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints colNgay = new ColumnConstraints();
            colNgay.setPercentWidth(100.0 / 7.0);
            colNgay.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(colNgay);
        }

        // Dòng tiêu đề cột ngày
        VBox cellGmt = taoOHeader("GMT+7", "");
        grid.add(cellGmt, 0, 0);

        String[] tenThu = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ nhật"};
        for (int i = 0; i < 7; i++) {
            LocalDate ngayHienTai = thuHai.plusDays(i);
            VBox cellHeader = taoOHeader(tenThu[i], ngayHienTai.format(DINH_DANG_NGAY));
            grid.add(cellHeader, i + 1, 0);
        }

        // Định nghĩa 8 mốc thời gian hiển thị ở cột trái (như ảnh thiết kế): 08:00, 10:00, 12:00, 14:00, 16:00, 18:00, 20:00, 22:00
        int[] mocGio = {8, 10, 12, 14, 16, 18, 20, 22};
        int soDongGio = mocGio.length;

        for (int r = 0; r < soDongGio; r++) {
            int gio = mocGio[r];
            Label lblMocGio = new Label(String.format("%02d:00", gio));
            lblMocGio.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-padding: 6 0 0 10;");

            StackPane pnlMoc = new StackPane(lblMocGio);
            pnlMoc.setAlignment(Pos.TOP_LEFT);
            pnlMoc.setPrefHeight(68);
            pnlMoc.setStyle("-fx-border-color: #f3f4f6 transparent transparent transparent; -fx-border-width: 1px;");
            grid.add(pnlMoc, 0, r + 1);

            // Các ô nền trống của từng ngày để vẽ đường kẻ ngang
            for (int col = 1; col <= 7; col++) {
                Pane pnlNen = new Pane();
                pnlNen.setPrefHeight(68);
                pnlNen.setStyle("-fx-border-color: #f3f4f6 transparent transparent #f3f4f6; -fx-border-width: 1px;");
                grid.add(pnlNen, col, r + 1);
            }
        }

        // Nhóm các ca trực theo ngày và đưa vào các cột
        for (PhanCa pc : danhSachTuan) {
            LocalDate ngay = pc.getNgayLam();
            int colIndex = (int) (ngay.toEpochDay() - thuHai.toEpochDay()) + 1;
            if (colIndex < 1 || colIndex > 7) continue;

            LocalTime tBatDau = pc.getGioBatDau() != null ? pc.getGioBatDau() : LocalTime.of(8, 0);
            LocalTime tKetThuc = pc.getGioKetThuc() != null ? pc.getGioKetThuc() : LocalTime.of(12, 0);

            // Xác định dòng bắt đầu và số dòng chiếm dụng dựa vào giờ
            int rowIndex = 1;
            int gioBatDau = tBatDau.getHour();
            for (int r = 0; r < mocGio.length; r++) {
                if (gioBatDau >= mocGio[r]) {
                    rowIndex = r + 1;
                }
            }

            int gioKetThuc = tKetThuc.getHour();
            int spanDong = 1;
            if (gioKetThuc > gioBatDau) {
                spanDong = Math.max(1, (int) Math.round((gioKetThuc - gioBatDau) / 2.0));
            }
            if (rowIndex + spanDong - 1 > soDongGio) {
                spanDong = soDongGio - rowIndex + 1;
            }

            MauSacNhanVien mau = layMauChoNhanVien(pc.getMaNhanVien(), pc.getTenNhanVien());
            VBox theCaTruc = taoTheCaTruc(pc, mau);

            grid.add(theCaTruc, colIndex, rowIndex, 1, spanDong);
        }

        pnlLichContainer.getChildren().setAll(grid);
        capNhatChuThichMau(tapNhanVien);
    }

    private void hienThiLichThang() {
        LocalDate ngayDauThang = ngayDauTuanHienTai.withDayOfMonth(1);
        lblKhoangNgay.setText("Tháng " + ngayDauThang.getMonthValue() + ", " + ngayDauThang.getYear());

        List<PhanCa> danhSachTatCa = caTrucController.timKiemPhanCa(null, null, null, PhanCa.TRANG_THAI_DA_PHAN_CONG);
        Set<String> tapNhanVien = new HashSet<>();

        String tenNVLoc = cboNhanVien.getValue();
        boolean locNV = tenNVLoc != null && !"Tất cả nhân viên".equalsIgnoreCase(tenNVLoc);

        List<PhanCa> danhSachThang = new ArrayList<>();
        for (PhanCa pc : danhSachTatCa) {
            LocalDate ngay = pc.getNgayLam();
            if (ngay != null && ngay.getYear() == ngayDauThang.getYear() && ngay.getMonthValue() == ngayDauThang.getMonthValue()) {
                if (!locNV || tenNVLoc.equalsIgnoreCase(pc.getTenNhanVien())) {
                    danhSachThang.add(pc);
                    tapNhanVien.add(pc.getMaNhanVien());
                }
            }
        }
        lblThongKe.setText(danhSachThang.size() + " ca trực · " + tapNhanVien.size() + " nhân viên");

        // Grid xem tháng 7 cột
        GridPane gridThang = new GridPane();
        gridThang.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 8px;");

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7.0);
            gridThang.getColumnConstraints().add(col);
        }

        String[] tenThu = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ nhật"};
        for (int i = 0; i < 7; i++) {
            Label lblThu = new Label(tenThu[i]);
            lblThu.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b5563; -fx-padding: 8;");
            StackPane cell = new StackPane(lblThu);
            cell.setAlignment(Pos.CENTER);
            gridThang.add(cell, i, 0);
        }

        LocalDate ngayBatDauLich = ngayDauThang.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate cur = ngayBatDauLich;
        int row = 1;
        while (cur.isBefore(ngayDauThang.plusMonths(1)) || cur.getDayOfWeek() != DayOfWeek.MONDAY) {
            for (int col = 0; col < 7; col++) {
                VBox oNgay = new VBox(4);
                oNgay.setPadding(new Insets(6));
                oNgay.setMinHeight(90);
                oNgay.setStyle("-fx-border-color: #f3f4f6; -fx-border-width: 1px;");

                Label lblNgay = new Label(String.valueOf(cur.getDayOfMonth()));
                boolean cungThang = cur.getMonthValue() == ngayDauThang.getMonthValue();
                lblNgay.setStyle(cungThang ? "-fx-font-weight: bold; -fx-text-fill: #111827;" : "-fx-text-fill: #9ca3af;");
                oNgay.getChildren().add(lblNgay);

                // Thêm các ca trực của ngày đó
                for (PhanCa pc : danhSachThang) {
                    if (cur.equals(pc.getNgayLam())) {
                        MauSacNhanVien mau = layMauChoNhanVien(pc.getMaNhanVien(), pc.getTenNhanVien());
                        Label lblChip = new Label(pc.getTenNhanVien() + " (" + pc.getTenCa() + ")");
                        lblChip.setStyle("-fx-background-color: " + mau.nen + "; -fx-text-fill: " + mau.chu 
                                + "; -fx-font-size: 10px; -fx-padding: 2 4; -fx-background-radius: 4px;");
                        oNgay.getChildren().add(lblChip);
                    }
                }

                gridThang.add(oNgay, col, row);
                cur = cur.plusDays(1);
            }
            row++;
        }

        pnlLichContainer.getChildren().setAll(gridThang);
        capNhatChuThichMau(tapNhanVien);
    }

    private VBox taoOHeader(String dong1, String dong2) {
        VBox cell = new VBox(2);
        cell.setAlignment(Pos.CENTER);
        cell.setPadding(new Insets(10, 4, 10, 4));
        cell.setStyle("-fx-border-color: transparent transparent #e5e7eb #f3f4f6; -fx-border-width: 1px;");

        Label lbl1 = new Label(dong1);
        lbl1.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6b7280;");

        Label lbl2 = new Label(dong2);
        lbl2.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        cell.getChildren().addAll(lbl1, lbl2);
        return cell;
    }

    private VBox taoTheCaTruc(PhanCa pc, MauSacNhanVien mau) {
        VBox the = new VBox(4);
        the.setPadding(new Insets(8, 10, 8, 10));

        // Viền trái màu sắc đặc trưng dày 4.5px, nền pastel dịu nhẹ theo mẫu ảnh
        the.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-border-color: transparent transparent transparent %s; " +
                "-fx-border-width: 0 0 0 4.5px; " +
                "-fx-border-radius: 6px 0 0 6px; " +
                "-fx-background-radius: 6px; " +
                "-fx-cursor: hand;",
                mau.nen, mau.vien
        ));

        Label lblTenNV = new Label(pc.getTenNhanVien());
        lblTenNV.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + mau.chu + ";");

        Label lblTenCa = new Label(pc.getTenCa());
        lblTenCa.setStyle("-fx-font-size: 11px; -fx-text-fill: " + mau.chuPhu + ";");

        Label lblGio = new Label(pc.getKhungGio());
        lblGio.setStyle("-fx-font-size: 11px; -fx-text-fill: " + mau.chuPhu + ";");

        the.getChildren().addAll(lblTenNV, lblTenCa, lblGio);

        // Hiệu ứng hover nổi nhẹ
        the.setOnMouseEntered(e -> the.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-border-color: transparent transparent transparent %s; " +
                "-fx-border-width: 0 0 0 4.5px; " +
                "-fx-border-radius: 6px 0 0 6px; " +
                "-fx-background-radius: 6px; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);",
                mau.nen, mau.vien
        )));

        the.setOnMouseExited(e -> the.setStyle(String.format(
                "-fx-background-color: %s; " +
                "-fx-border-color: transparent transparent transparent %s; " +
                "-fx-border-width: 0 0 0 4.5px; " +
                "-fx-border-radius: 6px 0 0 6px; " +
                "-fx-background-radius: 6px; " +
                "-fx-cursor: hand;",
                mau.nen, mau.vien
        )));

        // Click để xem chi tiết và cho phép hủy ca trực
        the.setOnMouseClicked(e -> moCuaSoChiTietCa(pc, mau));

        GridPane.setMargin(the, new Insets(3, 4, 3, 4));
        return the;
    }

    private void moCuaSoChiTietCa(PhanCa pc, MauSacNhanVien mau) {
        Stage dlg = new Stage();
        dlg.setTitle("Chi tiết ca trực - " + pc.getMaPhanCa());
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setResizable(false);

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        Label lblTieuDe = new Label("THÔNG TIN PHÂN CÔNG CA TRỰC");
        lblTieuDe.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + mau.vien + ";");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        grid.add(new Label("Mã phân công:"), 0, 0);
        Label lblMa = new Label(pc.getMaPhanCa());
        lblMa.setStyle("-fx-font-weight: bold;");
        grid.add(lblMa, 1, 0);

        grid.add(new Label("Nhân viên:"), 0, 1);
        Label lblNV = new Label(pc.getTenNhanVien() + " (" + pc.getMaNhanVien() + ")");
        lblNV.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mau.chu + ";");
        grid.add(lblNV, 1, 1);

        grid.add(new Label("Ca làm việc:"), 0, 2);
        grid.add(new Label(pc.getTenCa()), 1, 2);

        grid.add(new Label("Khung giờ:"), 0, 3);
        grid.add(new Label(pc.getKhungGio()), 1, 3);

        grid.add(new Label("Ngày làm:"), 0, 4);
        grid.add(new Label(pc.getNgayLamDinhDang()), 1, 4);

        grid.add(new Label("Trạng thái:"), 0, 5);
        Label lblTT = new Label(pc.getTrangThai());
        lblTT.setStyle(pc.isDaPhanCong() ? "-fx-text-fill: #15803d; -fx-font-weight: bold;" : "-fx-text-fill: #b91c1c;");
        grid.add(lblTT, 1, 5);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnHuyCa = new Button("Hủy ca trực");
        btnHuyCa.getStyleClass().add("nut-xoa");
        btnHuyCa.setOnAction(e -> {
            Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION, 
                    "Bạn có chắc muốn hủy ca này của nhân viên " + pc.getTenNhanVien() + "?", ButtonType.YES, ButtonType.NO);
            xacNhan.setHeaderText("Xác nhận hủy phân ca");
            if (xacNhan.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try {
                    caTrucController.huyPhanCa(pc.getMaPhanCa());
                    dlg.close();
                    taiLaiLichCaTruc();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK).showAndWait();
                }
            }
        });

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dlg.close());

        pnlNut.getChildren().addAll(btnHuyCa, btnDong);
        root.getChildren().addAll(lblTieuDe, grid, pnlNut);

        Scene sc = new Scene(root, 420, 290);
        try {
            var css = getClass().getResource("/com/petshop/view/css/chung.css");
            if (css != null) sc.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}

        dlg.setScene(sc);
        dlg.centerOnScreen();
        dlg.showAndWait();
    }

    private void capNhatChuThichMau(Set<String> tapMaNhanVien) {
        pnlChuThichMau.getChildren().clear();

        Label lblTieuDe = new Label("Màu nhân viên");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #111827;");
        pnlChuThichMau.getChildren().add(lblTieuDe);

        List<NhanVien> dsHienThi = new ArrayList<>();
        if (!tapMaNhanVien.isEmpty()) {
            for (NhanVien nv : danhSachNhanVienGoc) {
                if (tapMaNhanVien.contains(nv.getMaNhanVien())) {
                    dsHienThi.add(nv);
                }
            }
        }
        if (dsHienThi.isEmpty()) {
            dsHienThi = danhSachNhanVienGoc;
        }

        for (NhanVien nv : dsHienThi) {
            MauSacNhanVien mau = layMauChoNhanVien(nv.getMaNhanVien(), nv.getHoTen());

            Region oMau = new Region();
            oMau.setPrefSize(12, 12);
            oMau.setStyle("-fx-background-color: " + mau.vien + "; -fx-background-radius: 3px;");

            Label lblTen = new Label(nv.getHoTen());
            lblTen.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");

            HBox item = new HBox(6, oMau, lblTen);
            item.setAlignment(Pos.CENTER_LEFT);
            pnlChuThichMau.getChildren().add(item);
        }
    }

    /**
     * Cấu trúc lưu mã màu sắc theo từng nhân viên.
     */
    public static class MauSacNhanVien {
        public final String vien;
        public final String nen;
        public final String chu;
        public final String chuPhu;

        public MauSacNhanVien(String vien, String nen, String chu, String chuPhu) {
            this.vien = vien;
            this.nen = nen;
            this.chu = chu;
            this.chuPhu = chuPhu;
        }
    }
}
