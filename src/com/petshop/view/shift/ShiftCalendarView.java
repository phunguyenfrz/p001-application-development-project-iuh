package com.petshop.view.shift;

import com.petshop.controller.ShiftAssignmentController;
import com.petshop.model.ShiftAssignment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Màn hình ShiftCalendarView: Ca trực > Lịch ca trực.
 * Theo dõi lịch làm việc trong tuần/tháng, phân biệt màu sắc ca trực theo từng nhân viên.
 *
 * Tiêu chuẩn tuân thủ (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftCalendarView), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (khoiTao..., ganSuKien..., veLich..., chuyen...), viết theo chuẩn camelCase.
 * - Tiền tố điều khiển: btn, txt, lbl, cbo, dtp.
 * - Đầy đủ chú thích Javadoc và chú thích từng bước dựng giao diện.
 */
public class ShiftCalendarView extends StackPane {

    private final ShiftAssignmentController controller;
    private final List<ShiftAssignment> danhSachPhanCaToanBo = new ArrayList<>();

    // Các thành phần điều khiển giao diện theo đúng tiền tố chuẩn
    private Label lblTieuDe;
    private Label lblPhuDe;

    private Label lblLocNhanVien;
    private ComboBox<String> cboLocNhanVien;

    private Label lblLocNgay;
    private DatePicker dtpLocNgay;

    private Button btnXemTuan;
    private Button btnXemThang;

    private Button btnTuanTruoc;
    private Button btnTuanSau;
    private Label lblKhoangThoiGian;
    private Label lblThongKeCaTruc;

    // Vùng chứa bảng lịch thời gian biểu
    private ScrollPane scrLichContainer;
    private GridPane pnlLichGrid;

    // Thanh chú thích màu nhân viên dưới chân trang
    private HBox pnlLegendBox;
    private Label lblGhiChuDesigner;

    // Quản lý thời gian và chế độ hiển thị
    private LocalDate ngayDangChon;
    private boolean cheDoXemTuan = true;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    // Bảng màu phân biệt cho từng nhân viên (Khớp 100% với Image)
    private static final Map<String, String[]> BANG_MAU_NHAN_VIEN = Map.of(
            "Lê Thị Hoa", new String[]{"#15803d", "#f0fdf4", "#86efac", "#166534"}, // Stripe, Bg, Border, Text
            "Trần Minh Khang", new String[]{"#2563eb", "#eff6ff", "#93c5fd", "#1e40af"},
            "Phạm Ngọc Linh", new String[]{"#9333ea", "#faf5ff", "#d8b4fe", "#6b21a8"}
    );

    private static final String[] MAU_MAC_DINH = new String[]{"#475569", "#f8fafc", "#cbd5e1", "#334155"};

    /**
     * Khởi tạo màn hình Lịch ca trực.
     */
    public ShiftCalendarView() {
        this.controller = new ShiftAssignmentController();
        // Mặc định chọn tuần có dữ liệu mẫu: 19/10/2026
        this.ngayDangChon = LocalDate.of(2026, 10, 19);

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieuTuCoSoDuLieu();
    }

    /**
     * Dựng toàn bộ cấu trúc giao diện theo mẫu thiết kế Ca trực > Lịch ca trực.
     */
    private void khoiTaoGiaoDien() {
        setStyle("-fx-background-color: #f3f6f3;");

        VBox pnlMainLayout = new VBox(16);
        pnlMainLayout.setPadding(new Insets(24, 28, 28, 28));

        // 1. Tiêu đề Breadcrumb và phụ đề
        VBox pnlHeaderBox = new VBox(4);
        lblTieuDe = new Label("Ca trực > Lịch ca trực");
        lblTieuDe.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblPhuDe = new Label("Theo dõi lịch làm việc trong tuần, phân biệt ca trực theo từng nhân viên.");
        lblPhuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        pnlHeaderBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        // 2. Thẻ Card chính chứa lịch
        VBox pnlCard = new VBox(16);
        pnlCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-padding: 20px 22px; -fx-border-color: #e5eae6; -fx-border-radius: 12px;");
        VBox.setVgrow(pnlCard, Priority.ALWAYS);

        // --- 2.1 Thanh bộ lọc trên cùng (Lọc nhân viên, Chọn ngày, Nút Tuần/Tháng) ---
        HBox pnlFilterRow = new HBox(20);
        pnlFilterRow.setAlignment(Pos.CENTER_LEFT);

        // Khối 1: Lọc theo nhân viên
        VBox pnlEmpFilterBox = new VBox(5);
        lblLocNhanVien = new Label("Lọc theo nhân viên");
        lblLocNhanVien.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        cboLocNhanVien = new ComboBox<>();
        cboLocNhanVien.setPrefWidth(220);
        cboLocNhanVien.getStyleClass().add("modal-text-input");
        cboLocNhanVien.getItems().add("Tất cả nhân viên");
        cboLocNhanVien.setValue("Tất cả nhân viên");
        pnlEmpFilterBox.getChildren().addAll(lblLocNhanVien, cboLocNhanVien);

        // Khối 2: Chọn ngày
        VBox pnlDateFilterBox = new VBox(5);
        lblLocNgay = new Label("Chọn ngày");
        lblLocNgay.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        dtpLocNgay = new DatePicker(ngayDangChon);
        dtpLocNgay.setPrefWidth(180);
        dtpLocNgay.getStyleClass().add("modal-text-input");
        pnlDateFilterBox.getChildren().addAll(lblLocNgay, dtpLocNgay);

        Region pnlFilterSpacer = new Region();
        HBox.setHgrow(pnlFilterSpacer, Priority.ALWAYS);

        // Khối 3: Nút chuyển chế độ Tuần / Tháng
        HBox pnlViewModePill = new HBox(0);
        pnlViewModePill.setAlignment(Pos.CENTER);
        pnlViewModePill.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8px; -fx-padding: 3px; -fx-border-color: #e5e7eb; -fx-border-radius: 8px;");

        btnXemTuan = new Button("Tuần");
        btnXemTuan.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 5px 16px; -fx-font-size: 12px; -fx-cursor: hand;");

        btnXemThang = new Button("Tháng");
        btnXemThang.setStyle("-fx-background-color: transparent; -fx-text-fill: #4b5563; -fx-font-weight: 500; -fx-background-radius: 6px; -fx-padding: 5px 16px; -fx-font-size: 12px; -fx-cursor: hand;");

        pnlViewModePill.getChildren().addAll(btnXemTuan, btnXemThang);

        pnlFilterRow.getChildren().addAll(pnlEmpFilterBox, pnlDateFilterBox, pnlFilterSpacer, pnlViewModePill);

        // --- 2.2 Thanh điều hướng tuần và thống kê (< > 19–25 tháng 10, 2026 | 11 ca trực · 3 nhân viên) ---
        HBox pnlNavRow = new HBox(12);
        pnlNavRow.setAlignment(Pos.CENTER_LEFT);

        btnTuanTruoc = new Button("<");
        btnTuanTruoc.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #374151; -fx-cursor: hand; -fx-padding: 4px 10px; -fx-font-weight: bold;");

        btnTuanSau = new Button(">");
        btnTuanSau.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #374151; -fx-cursor: hand; -fx-padding: 4px 10px; -fx-font-weight: bold;");

        lblKhoangThoiGian = new Label("19–25 tháng 10, 2026");
        lblKhoangThoiGian.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Region pnlNavSpacer = new Region();
        HBox.setHgrow(pnlNavSpacer, Priority.ALWAYS);

        lblThongKeCaTruc = new Label("11 ca trực · 3 nhân viên");
        lblThongKeCaTruc.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");

        pnlNavRow.getChildren().addAll(btnTuanTruoc, btnTuanSau, lblKhoangThoiGian, pnlNavSpacer, lblThongKeCaTruc);

        // --- 2.3 Bảng Lịch (Weekly Timetable Grid) ---
        pnlLichGrid = new GridPane();
        pnlLichGrid.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 8px;");

        scrLichContainer = new ScrollPane(pnlLichGrid);
        scrLichContainer.setFitToWidth(true);
        scrLichContainer.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrLichContainer, Priority.ALWAYS);

        // --- 2.4 Chú thích màu sắc nhân viên chân trang (Legend) ---
        HBox pnlFooterBox = new HBox(16);
        pnlFooterBox.setAlignment(Pos.CENTER_LEFT);
        pnlFooterBox.setPadding(new Insets(6, 0, 0, 0));

        Label lblLegendTitle = new Label("Màu nhân viên");
        lblLegendTitle.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        pnlLegendBox = new HBox(16);
        pnlLegendBox.setAlignment(Pos.CENTER_LEFT);

        pnlFooterBox.getChildren().addAll(lblLegendTitle, pnlLegendBox);

        // Dòng chú thích designer
        lblGhiChuDesigner = new Label("cmbLocNhanVien · dtpLocNgay — Ca sáng 08:00–16:00 · Ca chiều 16:00–22:00. Tuần/Tháng và bộ lọc cập nhật trực tiếp theo CSDL.");
        lblGhiChuDesigner.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af; -fx-font-style: italic;");

        pnlCard.getChildren().addAll(pnlFilterRow, pnlNavRow, scrLichContainer, pnlFooterBox, lblGhiChuDesigner);
        pnlMainLayout.getChildren().addAll(pnlHeaderBox, pnlCard);

        getChildren().add(pnlMainLayout);
    }

    /**
     * Gắn bộ lắng nghe sự kiện tương tác người dùng.
     */
    private void ganSuKien() {
        // Nút chuyển tuần trước / tuần sau
        btnTuanTruoc.setOnAction(e -> chuyenSangTuanTruoc());
        btnTuanSau.setOnAction(e -> chuyenSangTuanSau());

        // Chọn ngày trong DatePicker
        dtpLocNgay.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ngayDangChon = newVal;
                veLich();
            }
        });

        // Lọc theo nhân viên
        cboLocNhanVien.valueProperty().addListener((obs, oldVal, newVal) -> {
            veLich();
        });

        // Chuyển chế độ xem Tuần / Tháng
        btnXemTuan.setOnAction(e -> {
            cheDoXemTuan = true;
            btnXemTuan.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 5px 16px; -fx-font-size: 12px; -fx-cursor: hand;");
            btnXemThang.setStyle("-fx-background-color: transparent; -fx-text-fill: #4b5563; -fx-font-weight: 500; -fx-background-radius: 6px; -fx-padding: 5px 16px; -fx-font-size: 12px; -fx-cursor: hand;");
            veLich();
        });

        btnXemThang.setOnAction(e -> {
            cheDoXemTuan = false;
            btnXemThang.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 5px 16px; -fx-font-size: 12px; -fx-cursor: hand;");
            btnXemTuan.setStyle("-fx-background-color: transparent; -fx-text-fill: #4b5563; -fx-font-weight: 500; -fx-background-radius: 6px; -fx-padding: 5px 16px; -fx-font-size: 12px; -fx-cursor: hand;");
            veLich();
        });
    }

    /**
     * Nạp toàn bộ dữ liệu phân ca từ CSDL.
     */
    public void napDuLieuTuCoSoDuLieu() {
        try {
            List<ShiftAssignment> danhSach = controller.layTatCaPhanCa();
            danhSachPhanCaToanBo.clear();
            danhSachPhanCaToanBo.addAll(danhSach);

            // Cập nhật danh sách ComboBox nhân viên
            Set<String> danhSachTenNhanVien = danhSach.stream()
                    .map(ShiftAssignment::getTenNhanVien)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(TreeSet::new));

            cboLocNhanVien.getItems().clear();
            cboLocNhanVien.getItems().add("Tất cả nhân viên");
            cboLocNhanVien.getItems().addAll(danhSachTenNhanVien);
            cboLocNhanVien.setValue("Tất cả nhân viên");

            capNhatThanhChuThichMau(danhSachTenNhanVien);
            veLich();
        } catch (Exception e) {
            System.err.println("[ShiftCalendarView] Lỗi nạp dữ liệu: " + e.getMessage());
        }
    }

    /**
     * Cập nhật dải màu chú thích dưới chân trang tương ứng với các nhân viên.
     */
    private void capNhatThanhChuThichMau(Set<String> danhSachNhanVien) {
        pnlLegendBox.getChildren().clear();

        for (String tenNv : danhSachNhanVien) {
            String[] mau = BANG_MAU_NHAN_VIEN.getOrDefault(tenNv, MAU_MAC_DINH);

            HBox pnlItem = new HBox(6);
            pnlItem.setAlignment(Pos.CENTER_LEFT);

            Rectangle rect = new Rectangle(12, 12);
            rect.setArcWidth(3);
            rect.setArcHeight(3);
            rect.setFill(Color.web(mau[0]));

            Label lblName = new Label(tenNv);
            lblName.setStyle("-fx-font-size: 12px; -fx-text-fill: #4b5563;");

            pnlItem.getChildren().addAll(rect, lblName);
            pnlLegendBox.getChildren().add(pnlItem);
        }
    }

    /**
     * Chuyển sang tuần trước đó (-7 ngày).
     */
    private void chuyenSangTuanTruoc() {
        ngayDangChon = ngayDangChon.minusWeeks(1);
        dtpLocNgay.setValue(ngayDangChon);
        veLich();
    }

    /**
     * Chuyển sang tuần tiếp theo (+7 ngày).
     */
    private void chuyenSangTuanSau() {
        ngayDangChon = ngayDangChon.plusWeeks(1);
        dtpLocNgay.setValue(ngayDangChon);
        veLich();
    }

    /**
     * Vẽ lại lưới thời khóa biểu theo tuần hoặc theo tháng.
     */
    private void veLich() {
        if (cheDoXemTuan) {
            veLichTuan();
        } else {
            veLichThang();
        }
    }

    /**
     * Vẽ bảng lịch làm việc chi tiết theo 7 ngày trong tuần khớp chính xác với Image.
     */
    private void veLichTuan() {
        pnlLichGrid.getChildren().clear();
        pnlLichGrid.getColumnConstraints().clear();
        pnlLichGrid.getRowConstraints().clear();

        // Xác định ngày Thứ Hai đầu tuần của tuần đang chọn
        LocalDate thuHai = ngayDangChon.with(DayOfWeek.MONDAY);
        LocalDate chuNhat = thuHai.plusDays(6);

        // Cập nhật nhãn khoảng thời gian: "19–25 tháng 10, 2026"
        lblKhoangThoiGian.setText(thuHai.getDayOfMonth() + "–" + chuNhat.getDayOfMonth()
                + " tháng " + thuHai.getMonthValue() + ", " + thuHai.getYear());

        // Lọc danh sách phân ca trong tuần này
        String nhanVienLoc = cboLocNhanVien.getValue();
        List<ShiftAssignment> danhSachTuan = danhSachPhanCaToanBo.stream()
                .filter(pa -> pa.getNgayLam() != null
                        && !pa.getNgayLam().isBefore(thuHai)
                        && !pa.getNgayLam().isAfter(chuNhat)
                        && !"Đã hủy".equalsIgnoreCase(pa.getTrangThai())
                        && (nhanVienLoc == null || "Tất cả nhân viên".equals(nhanVienLoc) || nhanVienLoc.equals(pa.getTenNhanVien())))
                .toList();

        // Đếm số lượng ca và số nhân viên tham gia trong tuần
        long soCa = danhSachTuan.size();
        long soNv = danhSachTuan.stream().map(ShiftAssignment::getTenNhanVien).distinct().count();
        lblThongKeCaTruc.setText(soCa + " ca trực · " + soNv + " nhân viên");

        // Cấu hình tỷ lệ 8 cột: Cột 0 (Giờ - 60px) + 7 Cột ngày (chia đều phần còn lại)
        ColumnConstraints colGio = new ColumnConstraints();
        colGio.setPrefWidth(70);
        colGio.setMinWidth(70);
        pnlLichGrid.getColumnConstraints().add(colGio);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints colNgay = new ColumnConstraints();
            colNgay.setPercentWidth(100.0 / 7);
            colNgay.setHgrow(Priority.ALWAYS);
            pnlLichGrid.getColumnConstraints().add(colNgay);
        }

        // --- Hàng 0: Header ngày trong tuần ---
        StackPane pnlHeaderGmt = new StackPane(new Label("GMT+7"));
        pnlHeaderGmt.setStyle("-fx-background-color: #f9fafb; -fx-padding: 10px 4px; -fx-border-color: #e5e7eb; -fx-border-width: 0 1px 1px 0;");
        Label lblGmt = (Label) pnlHeaderGmt.getChildren().get(0);
        lblGmt.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
        pnlLichGrid.add(pnlHeaderGmt, 0, 0);

        String[] tenThu = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ nhật"};
        for (int d = 0; d < 7; d++) {
            LocalDate ngay = thuHai.plusDays(d);
            VBox pnlDayHeader = new VBox(2);
            pnlDayHeader.setAlignment(Pos.CENTER);
            pnlDayHeader.setStyle("-fx-background-color: #f9fafb; -fx-padding: 10px 4px; -fx-border-color: #e5e7eb; -fx-border-width: 0 1px 1px 0;");

            Label lblThu = new Label(tenThu[d]);
            lblThu.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6b7280;");

            Label lblNgayThang = new Label(ngay.getDayOfMonth() + "/" + ngay.getMonthValue());
            lblNgayThang.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

            pnlDayHeader.getChildren().addAll(lblThu, lblNgayThang);
            pnlLichGrid.add(pnlDayHeader, d + 1, 0);
        }

        // --- 8 Hàng mốc thời gian (08:00 đến 22:00) ---
        String[] mocGio = {"08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00"};
        for (int r = 0; r < mocGio.length; r++) {
            // Cột mốc giờ
            StackPane pnlSlotLabel = new StackPane();
            pnlSlotLabel.setAlignment(Pos.TOP_CENTER);
            pnlSlotLabel.setStyle("-fx-padding: 6px 2px; -fx-border-color: #e5e7eb; -fx-border-width: 0 1px 1px 0; -fx-min-height: 52px;");

            Label lblTime = new Label(mocGio[r]);
            lblTime.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");
            pnlSlotLabel.getChildren().add(lblTime);
            pnlLichGrid.add(pnlSlotLabel, 0, r + 1);

            // Các ô trống nền của 7 ngày
            for (int d = 0; d < 7; d++) {
                Pane pnlCellBg = new Pane();
                pnlCellBg.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-width: 0 1px 1px 0; -fx-min-height: 52px;");
                pnlLichGrid.add(pnlCellBg, d + 1, r + 1);
            }
        }

        // --- Đặt các thẻ ca trực vào đúng cột ngày và hàng thời gian ---
        for (ShiftAssignment pa : danhSachTuan) {
            LocalDate ngay = pa.getNgayLam();
            int colIndex = (int) (ngay.toEpochDay() - thuHai.toEpochDay()) + 1;
            if (colIndex < 1 || colIndex > 7) continue;

            // Xác định hàng bắt đầu và độ cao tính theo số hàng (span)
            int rowIndex = 1; // 08:00
            int rowSpan = 4;  // Mặc định ca 8 tiếng chiếm 4 hàng (tương đương 08:00 -> 16:00)

            if (pa.getGioBatDau() != null) {
                int hour = pa.getGioBatDau().getHour();
                if (hour >= 16) {
                    rowIndex = 5; // 16:00
                    rowSpan = 3;  // 16:00 -> 22:00
                } else if (hour <= 8) {
                    rowIndex = 1;
                    rowSpan = 4;
                }
            }

            VBox pnlTheCa = taoTheCaTruc(pa);
            pnlLichGrid.add(pnlTheCa, colIndex, rowIndex, 1, rowSpan);
        }
    }

    /**
     * Tạo thẻ ca trực dạng hộp bo góc với dải màu viền trái theo nhân viên khớp 100% với Image.
     */
    private VBox taoTheCaTruc(ShiftAssignment pa) {
        String empName = (pa.getTenNhanVien() != null) ? pa.getTenNhanVien() : "Nhân viên";
        String[] mau = BANG_MAU_NHAN_VIEN.getOrDefault(empName, MAU_MAC_DINH);

        VBox pnlCard = new VBox(3);
        pnlCard.setPadding(new Insets(10, 10, 10, 12));
        pnlCard.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1px 1px 1px 4px; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-cursor: hand;",
                mau[1], mau[0]
        ));

        // Tên nhân viên in đậm
        Label lblName = new Label(empName);
        lblName.setStyle(String.format("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: %s;", mau[3]));

        // Tên ca trực
        Label lblShift = new Label(pa.getTenCa() != null ? pa.getTenCa() : "Ca trực");
        lblShift.setStyle("-fx-font-size: 11px; -fx-text-fill: #4b5563;");

        // Giờ làm việc (VD: 08:00–16:00)
        String timeStr = ((pa.getGioBatDau() != null) ? pa.getGioBatDau().format(DINH_DANG_GIO) : "")
                + "–" + ((pa.getGioKetThuc() != null) ? pa.getGioKetThuc().format(DINH_DANG_GIO) : "");
        Label lblTime = new Label(timeStr);
        lblTime.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        pnlCard.getChildren().addAll(lblName, lblShift, lblTime);

        // Hiệu ứng hover nổi bật
        pnlCard.setOnMouseEntered(e -> pnlCard.setStyle(pnlCard.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"));
        pnlCard.setOnMouseExited(e -> pnlCard.setStyle(pnlCard.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);", "")));

        return pnlCard;
    }

    /**
     * Vẽ bảng lịch dạng tháng (Month view) tổng quan.
     */
    private void veLichThang() {
        pnlLichGrid.getChildren().clear();
        pnlLichGrid.getColumnConstraints().clear();
        pnlLichGrid.getRowConstraints().clear();

        int month = ngayDangChon.getMonthValue();
        int year = ngayDangChon.getYear();
        lblKhoangThoiGian.setText("Tháng " + month + ", " + year);

        LocalDate dauThang = LocalDate.of(year, month, 1);
        int soNgayTrongThang = dauThang.lengthOfMonth();

        // 7 cột các thứ trong tuần
        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setHgrow(Priority.ALWAYS);
            pnlLichGrid.getColumnConstraints().add(col);
        }

        String[] tenThu = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ nhật"};
        for (int d = 0; d < 7; d++) {
            StackPane pnlHeader = new StackPane(new Label(tenThu[d]));
            pnlHeader.setStyle("-fx-background-color: #f9fafb; -fx-padding: 8px; -fx-border-color: #e5e7eb; -fx-border-width: 0 1px 1px 0;");
            Label lbl = (Label) pnlHeader.getChildren().get(0);
            lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");
            pnlLichGrid.add(pnlHeader, d, 0);
        }

        // Bắt đầu từ thứ của ngày đầu tháng
        int startCol = dauThang.getDayOfWeek().getValue() - 1; // 0: Thứ 2 ... 6: Chủ nhật
        int currentRow = 1;
        int currentCol = startCol;

        for (int day = 1; day <= soNgayTrongThang; day++) {
            LocalDate ngay = LocalDate.of(year, month, day);

            VBox pnlDayCell = new VBox(4);
            pnlDayCell.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-width: 0 1px 1px 0; -fx-padding: 6px; -fx-min-height: 90px;");

            Label lblDayNum = new Label(String.valueOf(day));
            lblDayNum.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
            pnlDayCell.getChildren().add(lblDayNum);

            // Tìm các ca trực trong ngày này
            List<ShiftAssignment> caTrongNgay = danhSachPhanCaToanBo.stream()
                    .filter(pa -> ngay.equals(pa.getNgayLam()) && !"Đã hủy".equalsIgnoreCase(pa.getTrangThai()))
                    .toList();

            for (ShiftAssignment pa : caTrongNgay) {
                String emp = (pa.getTenNhanVien() != null) ? pa.getTenNhanVien() : "NV";
                String[] mau = BANG_MAU_NHAN_VIEN.getOrDefault(emp, MAU_MAC_DINH);

                Label lblBadge = new Label(emp + " (" + (pa.getTenCa() != null ? pa.getTenCa() : "") + ")");
                lblBadge.setStyle(String.format(
                        "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-size: 10px; -fx-padding: 2px 5px; -fx-background-radius: 4px;",
                        mau[1], mau[3]
                ));
                pnlDayCell.getChildren().add(lblBadge);
            }

            pnlLichGrid.add(pnlDayCell, currentCol, currentRow);

            currentCol++;
            if (currentCol > 6) {
                currentCol = 0;
                currentRow++;
            }
        }
    }
}


