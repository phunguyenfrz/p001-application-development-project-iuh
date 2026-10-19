package com.petshop.view.shift;

import com.petshop.controller.ShiftAssignmentController;
import com.petshop.model.ShiftAssignment;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình ShiftAssignmentView: Ca trực > Phân công ca trực (Image 1).
 * Quản lý danh sách phân ca, hỗ trợ tìm kiếm, thêm mới, sửa và hủy phân ca làm việc.
 *
 * Tiêu chuẩn tuân thủ (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftAssignmentView), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (khoiTao..., ganSuKien..., xuLy...), viết theo chuẩn camelCase.
 * - Tiền tố điều khiển: btn, txt, lbl, tbl, col.
 * - Đầy đủ chú thích Javadoc và chú thích từng khối xử lý.
 */
public class ShiftAssignmentView extends StackPane {

    private final ShiftAssignmentController controller;
    private final ObservableList<ShiftAssignment> danhSachPhanCa = FXCollections.observableArrayList();
    private final ObservableList<ShiftAssignment> danhSachHienThiTrang = FXCollections.observableArrayList();

    // Các thành phần điều khiển giao diện theo đúng tiền tố chuẩn
    private Label lblTieuDe;
    private Label lblPhuDe;

    private TextField txtTimKiem;
    private Button btnThemPhanCa;
    private Button btnSua;
    private Button btnHuyPhanCa;
    private Button btnLamMoi;

    private TableView<ShiftAssignment> tblPhanCa;
    private TableColumn<ShiftAssignment, String> colMaPhanCa;
    private TableColumn<ShiftAssignment, String> colTenNhanVien;
    private TableColumn<ShiftAssignment, Void> colCaTruc;
    private TableColumn<ShiftAssignment, String> colNgayLam;
    private TableColumn<ShiftAssignment, String> colTrangThai;

    private Label lblTongBanGhi;
    private Label lblSoDongMoiTrang;
    private Button btnTrangTruoc;
    private Button btnTrangSau;

    // Khối hiển thị chi tiết phân ca đang chọn dưới chân bảng (Image 1)
    private VBox pnlDetailBox;
    private Label lblChiTietTieuDe;
    private Label lblChiTietNoiDung;

    // Quản lý phân trang
    private int trangHienTai = 1;
    private int soDongMoiTrang = 10;
    private int tongSoTrang = 1;

    // Lớp overlay modal
    private StackPane pnlModalOverlay;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo màn hình Phân công ca trực.
     */
    public ShiftAssignmentView() {
        this.controller = new ShiftAssignmentController();

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieuTuCoSoDuLieu();
    }

    /**
     * Dựng cấu trúc giao diện theo mẫu thiết kế Image 1 Ca trực > Phân công ca trực.
     */
    private void khoiTaoGiaoDien() {
        setStyle("-fx-background-color: #f3f6f3;");

        VBox pnlMainLayout = new VBox(16);
        pnlMainLayout.setPadding(new Insets(24, 28, 28, 28));

        // 1. Tiêu đề Breadcrumb và phụ đề mô tả
        VBox pnlHeaderBox = new VBox(4);
        lblTieuDe = new Label("Ca trực > Phân công ca trực");
        lblTieuDe.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblPhuDe = new Label("Danh sách phân công ca trực và các thao tác chính cho ca làm việc.");
        lblPhuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        pnlHeaderBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        // 2. Thẻ Card chứa bảng dữ liệu
        VBox pnlCard = new VBox(14);
        pnlCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-padding: 18px 20px; -fx-border-color: #e5eae6; -fx-border-radius: 12px;");
        VBox.setVgrow(pnlCard, Priority.ALWAYS);

        // --- 2.1 Thanh công cụ Toolbar (Tìm kiếm & Cụm nút thao tác) ---
        HBox pnlToolbar = new HBox(12);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        // Ô tìm kiếm nhanh với biểu tượng kính lúp
        HBox pnlSearchContainer = new HBox(8);
        pnlSearchContainer.setAlignment(Pos.CENTER_LEFT);
        pnlSearchContainer.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 0 10px;");
        pnlSearchContainer.setPrefWidth(280);

        SVGPath searchSvg = new SVGPath();
        searchSvg.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 14z");
        searchSvg.setFill(Color.web("#9ca89e"));
        searchSvg.setScaleX(0.65);
        searchSvg.setScaleY(0.65);

        txtTimKiem = new TextField();
        txtTimKiem.setPromptText("Tìm kiếm trong danh sách...");
        txtTimKiem.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 12.5px; -fx-prompt-text-fill: #9ca3af;");
        HBox.setHgrow(txtTimKiem, Priority.ALWAYS);

        pnlSearchContainer.getChildren().addAll(searchSvg, txtTimKiem);

        Region pnlToolbarSpacer = new Region();
        HBox.setHgrow(pnlToolbarSpacer, Priority.ALWAYS);

        // Nút + Thêm phân ca (Cam)
        btnThemPhanCa = new Button("+ Thêm phân ca");
        btnThemPhanCa.setStyle("-fx-background-color: #e07538; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 7px 16px; -fx-cursor: hand;");

        // Nút Sửa
        btnSua = new Button("Sửa");
        btnSua.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #374151; -fx-padding: 7px 16px; -fx-cursor: hand;");

        // Nút Hủy phân ca (Màu đỏ nhạt theo Image 1)
        btnHuyPhanCa = new Button("Hủy phân ca");
        btnHuyPhanCa.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fecaca; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #b91c1c; -fx-padding: 7px 16px; -fx-cursor: hand;");

        // Nút Làm mới
        btnLamMoi = new Button("Làm mới");
        btnLamMoi.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #374151; -fx-padding: 7px 16px; -fx-cursor: hand;");

        pnlToolbar.getChildren().addAll(
                pnlSearchContainer,
                pnlToolbarSpacer,
                btnThemPhanCa,
                btnSua,
                btnHuyPhanCa,
                btnLamMoi
        );

        // --- 2.2 Bảng TableView Phân ca ---
        tblPhanCa = new TableView<>();
        tblPhanCa.setPlaceholder(new Label("Không có phân công ca trực nào."));
        tblPhanCa.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblPhanCa, Priority.ALWAYS);

        // Cột 1: Mã phân ca
        colMaPhanCa = new TableColumn<>("Mã phân ca");
        colMaPhanCa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaPhanCa()));
        colMaPhanCa.setPrefWidth(160);

        // Cột 2: Tên nhân viên
        colTenNhanVien = new TableColumn<>("Tên nhân viên");
        colTenNhanVien.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenNhanVien()));
        colTenNhanVien.setPrefWidth(180);

        // Cột 3: Ca trực (Hiển thị 2 dòng: Dòng 1 Tên ca, Dòng 2 Khung giờ)
        colCaTruc = new TableColumn<>("Ca trực");
        colCaTruc.setPrefWidth(180);
        colCaTruc.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ShiftAssignment pa = getTableRow().getItem();
                    VBox pnlCaBox = new VBox(2);
                    pnlCaBox.setAlignment(Pos.CENTER_LEFT);

                    Label lblTen = new Label(pa.getTenCa() != null ? pa.getTenCa() : "");
                    lblTen.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #111827;");

                    String timeStr = ((pa.getGioBatDau() != null) ? pa.getGioBatDau().format(ShiftAssignment.DINH_DANG_GIO) : "")
                            + "–" + ((pa.getGioKetThuc() != null) ? pa.getGioKetThuc().format(ShiftAssignment.DINH_DANG_GIO) : "");
                    Label lblTime = new Label(timeStr);
                    lblTime.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6b7280;");

                    pnlCaBox.getChildren().addAll(lblTen, lblTime);
                    setGraphic(pnlCaBox);
                }
            }
        });

        // Cột 4: Ngày làm
        colNgayLam = new TableColumn<>("Ngày làm");
        colNgayLam.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().layNgayLamHienThi()));
        colNgayLam.setPrefWidth(130);

        // Cột 5: Trạng thái (Huy hiệu Badge xanh/đỏ)
        colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(140);
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ShiftAssignment pa = getTableRow().getItem();
                    String status = pa.getTrangThai();
                    Label lblBadge = new Label(status);
                    lblBadge.setStyle("-fx-padding: 3px 9px; -fx-background-radius: 12px; -fx-font-size: 11.5px; -fx-font-weight: 600;");

                    if ("Đã phân công".equalsIgnoreCase(status)) {
                        lblBadge.setStyle(lblBadge.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;");
                    } else {
                        lblBadge.setStyle(lblBadge.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
                    }

                    setGraphic(lblBadge);
                }
            }
        });

        tblPhanCa.getColumns().addAll(List.of(
                colMaPhanCa,
                colTenNhanVien,
                colCaTruc,
                colNgayLam,
                colTrangThai
        ));

        tblPhanCa.setItems(danhSachHienThiTrang);

        // --- 2.3 Chân trang phân trang (Footer Pagination) ---
        HBox pnlFooter = new HBox(12);
        pnlFooter.setAlignment(Pos.CENTER_LEFT);
        pnlFooter.setPadding(new Insets(8, 0, 0, 0));

        lblTongBanGhi = new Label("Tổng cộng 0 bản ghi");
        lblTongBanGhi.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");

        Region pnlFooterSpacer = new Region();
        HBox.setHgrow(pnlFooterSpacer, Priority.ALWAYS);

        lblSoDongMoiTrang = new Label("10 dòng / trang");
        lblSoDongMoiTrang.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");

        btnTrangTruoc = new Button("<");
        btnTrangTruoc.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: #374151; -fx-cursor: hand; -fx-padding: 3px 8px;");

        btnTrangSau = new Button(">");
        btnTrangSau.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: #374151; -fx-cursor: hand; -fx-padding: 3px 8px;");

        pnlFooter.getChildren().addAll(
                lblTongBanGhi,
                pnlFooterSpacer,
                lblSoDongMoiTrang,
                btnTrangTruoc,
                btnTrangSau
        );

        // --- 2.4 Khối thông tin chi tiết phân ca đang chọn (Image 1) ---
        pnlDetailBox = new VBox(4);
        pnlDetailBox.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 12px 16px;");

        lblChiTietTieuDe = new Label("Phân ca đang chọn · Chưa diễn ra");
        lblChiTietTieuDe.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #15803d;");

        lblChiTietNoiDung = new Label("Vui lòng chọn một dòng phân ca trong bảng để xem chi tiết hoặc thực hiện thao tác.");
        lblChiTietNoiDung.setStyle("-fx-font-size: 12px; -fx-text-fill: #166534;");
        pnlDetailBox.getChildren().addAll(lblChiTietTieuDe, lblChiTietNoiDung);

        Label lblGhiChuCuoi = new Label("Dữ liệu phân ca được đồng bộ thời gian thực với cơ sở dữ liệu Happy Tails.");
        lblGhiChuCuoi.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af; -fx-font-style: italic;");

        pnlCard.getChildren().addAll(pnlToolbar, tblPhanCa, pnlFooter, pnlDetailBox, lblGhiChuCuoi);
        pnlMainLayout.getChildren().addAll(pnlHeaderBox, pnlCard);

        getChildren().add(pnlMainLayout);

        // Lớp hiển thị modal trên cùng (Overlay)
        pnlModalOverlay = new StackPane();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
        getChildren().add(pnlModalOverlay);
    }

    /**
     * Gắn bộ lắng nghe sự kiện tương tác người dùng.
     */
    private void ganSuKien() {
        // Nút Thêm phân ca
        btnThemPhanCa.setOnAction(e -> hienThiHopThoaiThemMoi());

        // Nút Sửa
        btnSua.setOnAction(e -> {
            ShiftAssignment selected = tblPhanCa.getSelectionModel().getSelectedItem();
            if (selected == null) {
                hienThiCanhBao("Chưa chọn dòng", "Vui lòng chọn một bản ghi phân ca cần chỉnh sửa.");
                return;
            }
            hienThiHopThoaiCapNhat(selected);
        });

        // Nút Hủy phân ca
        btnHuyPhanCa.setOnAction(e -> {
            ShiftAssignment selected = tblPhanCa.getSelectionModel().getSelectedItem();
            if (selected == null) {
                hienThiCanhBao("Chưa chọn dòng", "Vui lòng chọn một bản ghi phân ca cần hủy.");
                return;
            }
            hienThiHopThoaiHuyPhanCa(selected);
        });

        // Nút Làm mới
        btnLamMoi.setOnAction(e -> {
            txtTimKiem.clear();
            napDuLieuTuCoSoDuLieu();
        });

        // Nhấp đúp chuột vào hàng để mở modal cập nhật
        tblPhanCa.setRowFactory(tv -> {
            TableRow<ShiftAssignment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    hienThiHopThoaiCapNhat(row.getItem());
                }
            });
            return row;
        });

        // Lắng nghe chọn dòng để cập nhật khối chi tiết xanh lá cây ở chân bảng
        tblPhanCa.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            capNhatThongTinChiTiet(newVal);
        });

        // Tìm kiếm tức thời khi gõ ký tự
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> {
            trangHienTai = 1;
            thucHienLocDanhSach(newVal);
        });

        // Phân trang
        btnTrangTruoc.setOnAction(e -> {
            if (trangHienTai > 1) {
                trangHienTai--;
                capNhatHienThiPhanTrang();
            }
        });

        btnTrangSau.setOnAction(e -> {
            if (trangHienTai < tongSoTrang) {
                trangHienTai++;
                capNhatHienThiPhanTrang();
            }
        });
    }

    /**
     * Nạp dữ liệu phân ca từ CSDL.
     */
    public void napDuLieuTuCoSoDuLieu() {
        try {
            List<ShiftAssignment> danhSach = controller.layTatCaPhanCa();
            danhSachPhanCa.setAll(danhSach);
            trangHienTai = 1;
            thucHienLocDanhSach(txtTimKiem.getText());

            // Tự động chọn dòng đầu tiên nếu có
            if (!danhSach.isEmpty()) {
                tblPhanCa.getSelectionModel().select(0);
                capNhatThongTinChiTiet(danhSach.get(0));
            }
        } catch (Exception e) {
            hienThiCanhBao("Lỗi nạp dữ liệu", "Không thể lấy danh sách phân ca: " + e.getMessage());
        }
    }

    /**
     * Cập nhật thông tin chi tiết phân ca đang chọn lên khung màu xanh ở chân bảng.
     */
    private void capNhatThongTinChiTiet(ShiftAssignment assignment) {
        if (assignment == null) {
            lblChiTietTieuDe.setText("Chưa chọn phân ca");
            lblChiTietNoiDung.setText("Vui lòng chọn một dòng phân ca trong bảng để xem chi tiết hoặc thực hiện thao tác.");
            return;
        }

        boolean chuaDienRa = assignment.kiemTraChuaDienRa();
        if (chuaDienRa) {
            lblChiTietTieuDe.setText("Phân ca đang chọn · Chưa diễn ra");
            lblChiTietTieuDe.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #15803d;");
        } else {
            lblChiTietTieuDe.setText("Phân ca đang chọn · Đã hoặc đang diễn ra");
            lblChiTietTieuDe.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold; -fx-text-fill: #b45309;");
        }

        lblChiTietNoiDung.setText(assignment.layDongThongTinChiTiet());
    }

    /**
     * Lọc danh sách phân ca theo từ khóa.
     *
     * @param tuKhoa Chuỗi tìm kiếm
     */
    private void thucHienLocDanhSach(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            tinhToanPhanTrangChoDanhSach(danhSachPhanCa);
            return;
        }

        String kw = tuKhoa.trim().toLowerCase();
        List<ShiftAssignment> filtered = danhSachPhanCa.stream()
                .filter(pa -> (pa.getMaPhanCa() != null && pa.getMaPhanCa().toLowerCase().contains(kw))
                        || (pa.getTenNhanVien() != null && pa.getTenNhanVien().toLowerCase().contains(kw))
                        || (pa.getTenCa() != null && pa.getTenCa().toLowerCase().contains(kw))
                        || (pa.layNgayLamHienThi().contains(kw)))
                .toList();

        tinhToanPhanTrangChoDanhSach(filtered);
    }

    /**
     * Cập nhật hiển thị phân trang.
     */
    private void capNhatHienThiPhanTrang() {
        thucHienLocDanhSach(txtTimKiem.getText());
    }

    /**
     * Tính toán chỉ số trang và cập nhật danh sách hiển thị.
     */
    private void tinhToanPhanTrangChoDanhSach(List<ShiftAssignment> danhSachToanBo) {
        int tongSoLuong = danhSachToanBo.size();
        lblTongBanGhi.setText("Tổng cộng " + tongSoLuong + " bản ghi");

        tongSoTrang = (int) Math.ceil((double) tongSoLuong / soDongMoiTrang);
        if (tongSoTrang < 1) tongSoTrang = 1;
        if (trangHienTai > tongSoTrang) trangHienTai = tongSoTrang;

        int viTriBatDau = (trangHienTai - 1) * soDongMoiTrang;
        int viTriKetThuc = Math.min(viTriBatDau + soDongMoiTrang, tongSoLuong);

        if (viTriBatDau < tongSoLuong) {
            danhSachHienThiTrang.setAll(danhSachToanBo.subList(viTriBatDau, viTriKetThuc));
        } else {
            danhSachHienThiTrang.clear();
        }

        btnTrangTruoc.setDisable(trangHienTai <= 1);
        btnTrangSau.setDisable(trangHienTai >= tongSoTrang);
    }

    /**
     * Mở hộp thoại thêm mới phân ca (Modal 4.2A).
     */
    private void hienThiHopThoaiThemMoi() {
        ShiftAssignmentEditorDialog dialog = new ShiftAssignmentEditorDialog(
                ShiftAssignmentEditorDialog.CheDo.THEM_MOI,
                controller,
                null,
                paMoi -> napDuLieuTuCoSoDuLieu(),
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở hộp thoại chỉnh sửa phân ca (Modal 4.2A · Cập nhật phân công).
     */
    private void hienThiHopThoaiCapNhat(ShiftAssignment assignment) {
        if (assignment == null) return;

        ShiftAssignmentEditorDialog dialog = new ShiftAssignmentEditorDialog(
                ShiftAssignmentEditorDialog.CheDo.CAP_NHAT,
                controller,
                assignment,
                paCapNhat -> napDuLieuTuCoSoDuLieu(),
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở hộp thoại xác nhận hủy phân ca (Modal 4.2B · Hủy phân công ca trực).
     */
    private void hienThiHopThoaiHuyPhanCa(ShiftAssignment assignment) {
        if (assignment == null) return;

        ShiftAssignmentCancelDialog dialog = new ShiftAssignmentCancelDialog(
                assignment,
                controller,
                this::napDuLieuTuCoSoDuLieu,
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở hiển thị modal.
     */
    private void moModal(Node modal) {
        pnlModalOverlay.getChildren().setAll(modal);
        pnlModalOverlay.setVisible(true);
        pnlModalOverlay.setManaged(true);
    }

    /**
     * Đóng modal.
     */
    private void dongModal() {
        pnlModalOverlay.getChildren().clear();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
    }

    /**
     * Hiển thị cảnh báo thông báo.
     */
    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}


