package com.petshop.view;

import com.petshop.controller.ShiftController;
import com.petshop.model.Shift;
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
 * Màn hình ShiftUpdateView: Ca trực > Cập nhật ca trực (Screen 1).
 * Cho phép xem bảng danh sách ca trực, tìm kiếm, mở modal thêm mới ca trực,
 * sửa thông tin ca trực và ngừng hoạt động (xóa mềm) theo đúng thiết kế đồ họa.
 *
 * Tiêu chuẩn tuân thủ (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftUpdateView), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (khoiTao..., ganSuKien..., xuLy...), viết theo chuẩn camelCase.
 * - Tiền tố điều khiển: btn, txt, lbl, tbl, col, cbo.
 * - Thao tác xóa là ngừng hoạt động (xóa mềm), khớp tài liệu OOAD.
 * - Đầy đủ chú thích Javadoc và chú thích một dòng.
 */
public class ShiftUpdateView extends StackPane {

    private final ShiftController shiftController;
    private final ObservableList<Shift> danhSachCaTruc = FXCollections.observableArrayList();
    private final ObservableList<Shift> danhSachHienThiTrang = FXCollections.observableArrayList();

    // Các thành phần giao diện theo đúng tiền tố chuẩn
    private Label lblTieuDe;
    private Label lblPhuDe;

    private Label lblBangDuLieu;
    private Label lblSoLuongBanGhi;
    private TextField txtTimKiem;

    private Button btnThemCaTruc;
    private Button btnLamMoi;

    private TableView<Shift> tblCaTruc;
    private TableColumn<Shift, String> colMaDinhDanh;
    private TableColumn<Shift, String> colTenCa;
    private TableColumn<Shift, String> colGioBatDau;
    private TableColumn<Shift, String> colGioKetThuc;
    private TableColumn<Shift, String> colTrangThai;
    private TableColumn<Shift, String> colNgayCapNhat;
    private TableColumn<Shift, Void> colThaoTac;

    private Label lblTongBanGhi;
    private Label lblSoDongMoiTrang;
    private Button btnTrangTruoc;
    private Button btnTrangSau;

    // Quản lý trạng thái phân trang (Pagination)
    private int trangHienTai = 1;
    private int soDongMoiTrang = 10;
    private int tongSoTrang = 1;

    // Lớp phủ hiển thị modal dialog
    private StackPane pnlModalOverlay;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo màn hình Quản lý Cập nhật ca trực.
     */
    public ShiftUpdateView() {
        this.shiftController = new ShiftController();

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieuTuCoSoDuLieu();
    }

    /**
     * Dựng cấu trúc giao diện theo mẫu thiết kế Screen 1 Ca trực > Cập nhật ca trực.
     */
    private void khoiTaoGiaoDien() {
        setStyle("-fx-background-color: #f3f6f3;");

        VBox pnlMainLayout = new VBox(16);
        pnlMainLayout.setPadding(new Insets(24, 28, 28, 28));

        // 1. Tiêu đề Breadcrumb và phụ đề mô tả
        VBox pnlHeaderBox = new VBox(4);
        lblTieuDe = new Label("Ca trực > Cập nhật ca trực");
        lblTieuDe.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblPhuDe = new Label("Cập nhật thông tin ca trực, phân công nhân viên và theo dõi lịch trực.");
        lblPhuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        pnlHeaderBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        // 2. Thẻ Card chứa bảng dữ liệu ca trực
        VBox pnlCard = new VBox(14);
        pnlCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-padding: 18px 20px; -fx-border-color: #e5eae6; -fx-border-radius: 12px;");
        VBox.setVgrow(pnlCard, Priority.ALWAYS);

        // --- 2.1 Thanh công cụ Toolbar (Tiêu đề card, Tìm kiếm, Thêm ca trực, Làm mới) ---
        HBox pnlToolbar = new HBox(12);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        HBox pnlTitleBadge = new HBox(8);
        pnlTitleBadge.setAlignment(Pos.CENTER_LEFT);
        lblBangDuLieu = new Label("Bảng dữ liệu ca trực");
        lblBangDuLieu.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblSoLuongBanGhi = new Label("0");
        lblSoLuongBanGhi.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 2px 7px; -fx-background-radius: 10px;");
        pnlTitleBadge.getChildren().addAll(lblBangDuLieu, lblSoLuongBanGhi);

        Region pnlToolbarSpacer = new Region();
        HBox.setHgrow(pnlToolbarSpacer, Priority.ALWAYS);

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

        // Nút + Thêm ca trực (Màu cam đặc trưng Happy Tails)
        btnThemCaTruc = new Button("+ Thêm ca trực");
        btnThemCaTruc.getStyleClass().add("btn-orange");
        btnThemCaTruc.setStyle("-fx-background-color: #e07538; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-padding: 7px 14px; -fx-cursor: hand;");

        // Nút Làm mới
        btnLamMoi = new Button("Làm mới");
        btnLamMoi.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-text-fill: #374151; -fx-padding: 7px 14px; -fx-cursor: hand;");

        pnlToolbar.getChildren().addAll(
                pnlTitleBadge,
                pnlToolbarSpacer,
                pnlSearchContainer,
                btnThemCaTruc,
                btnLamMoi
        );

        // --- 2.2 Bảng TableView Ca trực ---
        tblCaTruc = new TableView<>();
        tblCaTruc.setPlaceholder(new Label("Không tìm thấy ca trực nào trong hệ thống."));
        tblCaTruc.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblCaTruc, Priority.ALWAYS);

        // Cột 1: Mã định danh
        colMaDinhDanh = new TableColumn<>("Mã định danh");
        colMaDinhDanh.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaCa()));
        colMaDinhDanh.setPrefWidth(160);

        // Cột 2: Tên ca
        colTenCa = new TableColumn<>("Tên ca");
        colTenCa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenCa()));
        colTenCa.setPrefWidth(130);

        // Cột 3: Giờ bắt đầu
        colGioBatDau = new TableColumn<>("Giờ bắt đầu");
        colGioBatDau.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().layGioBatDauHienThi()));
        colGioBatDau.setPrefWidth(100);

        // Cột 4: Giờ kết thúc
        colGioKetThuc = new TableColumn<>("Giờ kết thúc");
        colGioKetThuc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().layGioKetThucHienThi()));
        colGioKetThuc.setPrefWidth(100);

        // Cột 5: Trạng thái (hiển thị Badge màu tương ứng)
        colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Shift ca = getTableRow().getItem();
                    String trangThaiStr = ca.layTrangThaiHienThi();
                    Label lblBadge = taoHuyHieuTrangThai(trangThaiStr);
                    setGraphic(lblBadge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Cột 6: Ngày cập nhật
        colNgayCapNhat = new TableColumn<>("Ngày cập nhật");
        colNgayCapNhat.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getNgayCapNhat();
            return new SimpleStringProperty((d != null) ? d.format(DINH_DANG_NGAY) : "");
        });
        colNgayCapNhat.setPrefWidth(110);

        // Cột 7: Thao tác (chứa 2 nút bấm Sửa và Xóa trong từng dòng)
        colThaoTac = new TableColumn<>("Thao tác");
        colThaoTac.setPrefWidth(140);
        colThaoTac.setSortable(false);
        colThaoTac.setCellFactory(column -> taoCotThaoTac());

        tblCaTruc.getColumns().addAll(List.of(
                colMaDinhDanh,
                colTenCa,
                colGioBatDau,
                colGioKetThuc,
                colTrangThai,
                colNgayCapNhat,
                colThaoTac
        ));

        tblCaTruc.setItems(danhSachHienThiTrang);

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

        pnlCard.getChildren().addAll(pnlToolbar, tblCaTruc, pnlFooter);
        pnlMainLayout.getChildren().addAll(pnlHeaderBox, pnlCard);

        getChildren().add(pnlMainLayout);

        // Lớp hiển thị modal trên cùng (Overlay)
        pnlModalOverlay = new StackPane();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
        getChildren().add(pnlModalOverlay);
    }

    /**
     * Tạo TableCell chứa hai nút thao tác inline: "Sửa" và "🗑 Xóa" cho mỗi dòng.
     *
     * @return TableCell chứa cụm nút hành động
     */
    private TableCell<Shift, Void> taoCotThaoTac() {
        return new TableCell<>() {
            private final Button btnSua = new Button("Sửa");
            private final Button btnXoa = new Button("🗑 Xóa");
            private final HBox pnlButtons = new HBox(6, btnSua, btnXoa);

            {
                pnlButtons.setAlignment(Pos.CENTER_LEFT);

                btnSua.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-text-fill: #374151; -fx-font-size: 11.5px; -fx-padding: 4px 10px; -fx-cursor: hand;");
                btnXoa.setStyle("-fx-background-color: #dc2626; -fx-text-fill: #ffffff; -fx-border-color: transparent; -fx-background-radius: 4px; -fx-font-size: 11.5px; -fx-padding: 4px 10px; -fx-cursor: hand; -fx-font-weight: 500;");

                btnSua.setOnAction(e -> {
                    Shift ca = getTableView().getItems().get(getIndex());
                    hienThiHopThoaiCapNhat(ca);
                });

                btnXoa.setOnAction(e -> {
                    Shift ca = getTableView().getItems().get(getIndex());
                    hienThiHopThoaiXoaMem(ca);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(pnlButtons);
                }
            }
        };
    }

    /**
     * Tạo nhãn badge trạng thái với màu sắc trực quan tương ứng thiết kế Image 1.
     *
     * @param trangThai Chuỗi mô tả trạng thái
     * @return Label badge có màu nền và chữ định kiểu sẵn
     */
    private Label taoHuyHieuTrangThai(String trangThai) {
        Label lblBadge = new Label(trangThai);
        lblBadge.setStyle("-fx-padding: 3px 9px; -fx-background-radius: 12px; -fx-font-size: 11.5px; -fx-font-weight: 600;");

        switch (trangThai) {
            case "Đang hoạt động" -> lblBadge.setStyle(lblBadge.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;");
            case "Sắp bắt đầu" -> lblBadge.setStyle(lblBadge.getStyle() + "-fx-background-color: #ffedd5; -fx-text-fill: #c2410c;");
            case "Đã kết thúc" -> lblBadge.setStyle(lblBadge.getStyle() + "-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563;");
            default -> lblBadge.setStyle(lblBadge.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
        }
        return lblBadge;
    }

    /**
     * Gắn bộ lắng nghe sự kiện thao tác người dùng.
     */
    private void ganSuKien() {
        // Nút Thêm ca trực
        btnThemCaTruc.setOnAction(e -> hienThiHopThoaiThemMoi());

        // Nút Làm mới
        btnLamMoi.setOnAction(e -> {
            txtTimKiem.clear();
            napDuLieuTuCoSoDuLieu();
        });

        // Double click trên hàng của bảng để mở modal cập nhật
        tblCaTruc.setRowFactory(tv -> {
            TableRow<Shift> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    hienThiHopThoaiCapNhat(row.getItem());
                }
            });
            return row;
        });

        // Tìm kiếm tức thì khi gõ ký tự
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
     * Nạp toàn bộ danh sách ca trực từ CSDL.
     */
    public void napDuLieuTuCoSoDuLieu() {
        try {
            List<Shift> danhSach = shiftController.layTatCaCaTruc();
            danhSachCaTruc.setAll(danhSach);
            trangHienTai = 1;
            thucHienLocDanhSach(txtTimKiem.getText());
        } catch (Exception e) {
            hienThiCanhBao("Lỗi nạp dữ liệu", "Không thể lấy danh sách ca trực: " + e.getMessage());
        }
    }

    /**
     * Lọc danh sách theo từ khóa mã ca hoặc tên ca.
     *
     * @param tuKhoa Chuỗi tìm kiếm
     */
    private void thucHienLocDanhSach(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            tinhToanPhanTrangChoDanhSach(danhSachCaTruc);
            return;
        }

        String keyword = tuKhoa.trim().toLowerCase();
        List<Shift> filtered = danhSachCaTruc.stream()
                .filter(c -> c.getMaCa().toLowerCase().contains(keyword)
                        || c.getTenCa().toLowerCase().contains(keyword))
                .toList();

        tinhToanPhanTrangChoDanhSach(filtered);
    }

    /**
     * Cập nhật danh sách hiển thị trên trang hiện hành.
     */
    private void capNhatHienThiPhanTrang() {
        thucHienLocDanhSach(txtTimKiem.getText());
    }

    /**
     * Tính toán chỉ số phân trang và cập nhật UI.
     *
     * @param danhSachToanBo Danh sách ca trực đang cần phân trang
     */
    private void tinhToanPhanTrangChoDanhSach(List<Shift> danhSachToanBo) {
        int tongSoLuong = danhSachToanBo.size();
        lblSoLuongBanGhi.setText(String.valueOf(tongSoLuong));
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
     * Mở hộp thoại thêm mới ca trực (Modal 1.1A).
     */
    private void hienThiHopThoaiThemMoi() {
        ShiftEditorDialog dialog = new ShiftEditorDialog(
                ShiftEditorDialog.CheDo.THEM_MOI,
                shiftController,
                null,
                caMoi -> napDuLieuTuCoSoDuLieu(),
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở hộp thoại chỉnh sửa ca trực (Modal 1.1A · Sửa ca trực).
     *
     * @param caTruc Ca trực cần cập nhật
     */
    private void hienThiHopThoaiCapNhat(Shift caTruc) {
        if (caTruc == null) {
            hienThiCanhBao("Chưa chọn dòng", "Vui lòng chọn một ca trực trong danh sách để chỉnh sửa.");
            return;
        }

        ShiftEditorDialog dialog = new ShiftEditorDialog(
                ShiftEditorDialog.CheDo.CAP_NHAT,
                shiftController,
                caTruc,
                caCapNhat -> napDuLieuTuCoSoDuLieu(),
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở hộp thoại xác nhận xóa mềm ca trực (Modal 1.1B · Xóa ca trực).
     *
     * @param caTruc Ca trực cần ngừng hoạt động
     */
    private void hienThiHopThoaiXoaMem(Shift caTruc) {
        if (caTruc == null) {
            hienThiCanhBao("Chưa chọn dòng", "Vui lòng chọn ca trực cần ngừng hoạt động (xóa).");
            return;
        }

        ShiftDeleteDialog dialog = new ShiftDeleteDialog(
                caTruc,
                shiftController,
                this::napDuLieuTuCoSoDuLieu,
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở và hiển thị một lớp Modal trên giao diện chính.
     *
     * @param modal Cây Node của modal
     */
    private void moModal(Node modal) {
        pnlModalOverlay.getChildren().setAll(modal);
        pnlModalOverlay.setVisible(true);
        pnlModalOverlay.setManaged(true);
    }

    /**
     * Đóng modal đang hiển thị.
     */
    private void dongModal() {
        pnlModalOverlay.getChildren().clear();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
    }

    /**
     * Hiển thị hộp thoại cảnh báo thông báo nhanh.
     *
     * @param tieuDe  Tiêu đề thông báo
     * @param noiDung Nội dung cảnh báo
     */
    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

