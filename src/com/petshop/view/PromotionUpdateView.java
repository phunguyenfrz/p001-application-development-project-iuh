package com.petshop.view;

import com.petshop.controller.PromotionController;
import com.petshop.model.Promotion;
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
 * Màn hình PromotionUpdateView: Khuyến mãi > Cập nhật khuyến mãi.
 * Cho phép xem danh sách, tìm kiếm, thêm mới, sửa và xóa mềm chương trình ưu đãi.
 *
 * Tiêu chuẩn tuân thủ:
 * - Đặt tên điều khiển đúng tiền tố: btn, txt, lbl, tbl, dtp, cbo.
 * - Tên lớp là danh từ (PromotionUpdateView), tên phương thức là động từ.
 * - Thao tác xóa là ngừng hoạt động (xóa mềm).
 * - Tích hợp đầy đủ theo thiết kế đồ họa ảnh 1, 2, 3, 4.
 */
public class PromotionUpdateView extends StackPane {

    private final PromotionController promotionController;
    private final ObservableList<Promotion> danhSachKhuyenMai = FXCollections.observableArrayList();
    private final ObservableList<Promotion> danhSachHienThiTrang = FXCollections.observableArrayList();

    // Các thành phần giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;

    private Label lblDanhSach;
    private Label lblSoLuongBanGhi;
    private TextField txtTimKiem;

    private Button btnThemMoi;
    private Button btnSua;
    private Button btnXoa;
    private Button btnLamMoi;

    private TableView<Promotion> tblKhuyenMai;
    private TableColumn<Promotion, String> colMaKhuyenMai;
    private TableColumn<Promotion, String> colThongTinChinh;
    private TableColumn<Promotion, String> colPhanTramGiam;
    private TableColumn<Promotion, String> colNgayBatDau;
    private TableColumn<Promotion, String> colNgayKetThuc;
    private TableColumn<Promotion, String> colTrangThai;
    private TableColumn<Promotion, String> colNgayCapNhat;

    private Label lblTongBanGhi;
    private Label lblSoDongMoiTrang;
    private Button btnTrangTruoc;
    private Button btnTrangSau;

    // Phân trang
    private int trangHienTai = 1;
    private int soDongMoiTrang = 10;
    private int tongSoTrang = 1;

    // Lớp overlay hiển thị modal dialog
    private StackPane pnlModalOverlay;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo màn hình Cập nhật khuyến mãi.
     */
    public PromotionUpdateView() {
        this.promotionController = new PromotionController();

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieuTuCoSoDuLieu();
    }

    /**
     * Dựng bố cục và cây giao diện cho màn hình Cập nhật khuyến mãi.
     */
    private void khoiTaoGiaoDien() {
        setStyle("-fx-background-color: #f3f6f3;");

        VBox pnlMainLayout = new VBox(16);
        pnlMainLayout.setPadding(new Insets(24, 28, 28, 28));

        // 1. Tiêu đề và đường dẫn Breadcrumb
        VBox pnlHeaderBox = new VBox(4);
        lblTieuDe = new Label("Khuyến mãi > Cập nhật khuyến mãi");
        lblTieuDe.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblPhuDe = new Label("Chỉnh sửa chương trình khuyến mãi và theo dõi danh sách đang áp dụng.");
        lblPhuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        pnlHeaderBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        // 2. Thẻ trắng trung tâm chứa thanh công cụ và bảng dữ liệu
        VBox pnlCard = new VBox(14);
        pnlCard.setPadding(new Insets(18, 20, 20, 20));
        pnlCard.getStyleClass().add("chart-panel");
        VBox.setVgrow(pnlCard, Priority.ALWAYS);

        // --- 2.1 Hàng tiêu đề bảng & Thanh công cụ ---
        HBox pnlToolbar = new HBox(12);
        pnlToolbar.setAlignment(Pos.CENTER_LEFT);

        // Nhãn danh sách kèm huy hiệu số lượng
        HBox pnlTitleBadge = new HBox(8);
        pnlTitleBadge.setAlignment(Pos.CENTER_LEFT);

        lblDanhSach = new Label("Danh sách chương trình");
        lblDanhSach.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblSoLuongBanGhi = new Label("0");
        lblSoLuongBanGhi.getStyleClass().add("badge-count");
        pnlTitleBadge.getChildren().addAll(lblDanhSach, lblSoLuongBanGhi);

        Region pnlToolbarSpacer = new Region();
        HBox.setHgrow(pnlToolbarSpacer, Priority.ALWAYS);

        // Ô tìm kiếm nhanh
        HBox pnlSearchContainer = new HBox(8);
        pnlSearchContainer.setAlignment(Pos.CENTER_LEFT);
        pnlSearchContainer.getStyleClass().add("search-box");
        pnlSearchContainer.setPrefWidth(240);

        SVGPath searchSvg = new SVGPath();
        searchSvg.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
        searchSvg.setFill(Color.web("#9ca89e"));
        searchSvg.setScaleX(0.65);
        searchSvg.setScaleY(0.65);

        txtTimKiem = new TextField();
        txtTimKiem.setPromptText("Tìm kiếm chương trình...");
        txtTimKiem.getStyleClass().add("search-field");
        HBox.setHgrow(txtTimKiem, Priority.ALWAYS);

        pnlSearchContainer.getChildren().addAll(searchSvg, txtTimKiem);

        // Nút + Thêm mới (Màu cam)
        btnThemMoi = new Button("+ Thêm mới");
        btnThemMoi.getStyleClass().add("btn-orange");

        // Nút Sửa
        btnSua = new Button("Sửa");
        btnSua.getStyleClass().add("btn-action-secondary");

        // Nút Xóa (Màu đỏ với biểu tượng thùng rác)
        btnXoa = new Button("🗑 Xóa");
        btnXoa.getStyleClass().add("btn-danger");

        // Nút Làm mới
        btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("btn-action-secondary");

        pnlToolbar.getChildren().addAll(
                pnlTitleBadge,
                pnlToolbarSpacer,
                pnlSearchContainer,
                btnThemMoi,
                btnSua,
                btnXoa,
                btnLamMoi
        );

        // --- 2.2 Bảng TableView Khuyến mãi ---
        tblKhuyenMai = new TableView<>();
        tblKhuyenMai.setPlaceholder(new Label("Không có dữ liệu khuyến mãi nào."));
        tblKhuyenMai.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblKhuyenMai, Priority.ALWAYS);

        colMaKhuyenMai = new TableColumn<>("Mã định danh");
        colMaKhuyenMai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaKhuyenMai()));
        colMaKhuyenMai.setPrefWidth(120);

        colThongTinChinh = new TableColumn<>("Thông tin chính");
        colThongTinChinh.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenChuongTrinh()));
        colThongTinChinh.setPrefWidth(220);

        colPhanTramGiam = new TableColumn<>("% Giảm");
        colPhanTramGiam.setCellValueFactory(c -> new SimpleStringProperty((int) c.getValue().getPhanTramGiam() + "%"));
        colPhanTramGiam.setPrefWidth(80);

        colNgayBatDau = new TableColumn<>("Ngày bắt đầu");
        colNgayBatDau.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getNgayBatDau();
            return new SimpleStringProperty((d != null) ? d.format(DINH_DANG_NGAY) : "");
        });
        colNgayBatDau.setPrefWidth(100);

        colNgayKetThuc = new TableColumn<>("Ngày kết thúc");
        colNgayKetThuc.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getNgayKetThuc();
            return new SimpleStringProperty((d != null) ? d.format(DINH_DANG_NGAY) : "");
        });
        colNgayKetThuc.setPrefWidth(100);

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
                    Promotion km = getTableRow().getItem();
                    String status = km.layTrangThaiHienThi();
                    Label lblBadge = new Label(status);
                    switch (status) {
                        case "Đang áp dụng" -> lblBadge.getStyleClass().add("status-badge-active");
                        case "Sắp áp dụng" -> lblBadge.getStyleClass().add("status-badge-upcoming");
                        default -> lblBadge.getStyleClass().add("status-badge-inactive");
                    }
                    setGraphic(lblBadge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        colNgayCapNhat = new TableColumn<>("Ngày cập nhật");
        colNgayCapNhat.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getNgayCapNhat();
            return new SimpleStringProperty((d != null) ? d.format(DINH_DANG_NGAY) : "");
        });
        colNgayCapNhat.setPrefWidth(110);

        tblKhuyenMai.getColumns().addAll(List.of(
                colMaKhuyenMai,
                colThongTinChinh,
                colPhanTramGiam,
                colNgayBatDau,
                colNgayKetThuc,
                colTrangThai,
                colNgayCapNhat
        ));
        tblKhuyenMai.setItems(danhSachHienThiTrang);

        // --- 2.3 Chân trang phân trang (Pagination Footer) ---
        HBox pnlFooter = new HBox(12);
        pnlFooter.setAlignment(Pos.CENTER_LEFT);
        pnlFooter.setPadding(new Insets(10, 4, 0, 4));

        lblTongBanGhi = new Label("Tổng cộng 0 bản ghi");
        lblTongBanGhi.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");

        Region pnlFooterSpacer = new Region();
        HBox.setHgrow(pnlFooterSpacer, Priority.ALWAYS);

        lblSoDongMoiTrang = new Label("10 dòng / trang");
        lblSoDongMoiTrang.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");

        btnTrangTruoc = new Button("<");
        btnTrangTruoc.getStyleClass().add("pagination-btn");

        btnTrangSau = new Button(">");
        btnTrangSau.getStyleClass().add("pagination-btn");

        pnlFooter.getChildren().addAll(
                lblTongBanGhi,
                pnlFooterSpacer,
                lblSoDongMoiTrang,
                btnTrangTruoc,
                btnTrangSau
        );

        pnlCard.getChildren().addAll(pnlToolbar, tblKhuyenMai, pnlFooter);
        pnlMainLayout.getChildren().addAll(pnlHeaderBox, pnlCard);

        getChildren().add(pnlMainLayout);

        // Lớp hiển thị modal trên cùng
        pnlModalOverlay = new StackPane();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
        getChildren().add(pnlModalOverlay);
    }

    /**
     * Gắn các sự kiện thao tác người dùng.
     */
    private void ganSuKien() {
        // Nút Thêm mới
        btnThemMoi.setOnAction(e -> hienThiHopThoaiThemMoi());

        // Nút Sửa
        btnSua.setOnAction(e -> hienThiHopThoaiCapNhat());

        // Double click trên bảng để mở sửa
        tblKhuyenMai.setRowFactory(tv -> {
            TableRow<Promotion> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    hienThiHopThoaiCapNhat();
                }
            });
            return row;
        });

        // Nút Xóa (Xóa mềm)
        btnXoa.setOnAction(e -> hienThiHopThoaiXoaMem());

        // Nút Làm mới
        btnLamMoi.setOnAction(e -> {
            txtTimKiem.clear();
            napDuLieuTuCoSoDuLieu();
        });

        // Tìm kiếm nhanh khi gõ ký tự
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
     * Nạp toàn bộ danh sách khuyến mãi từ CSDL.
     */
    public void napDuLieuTuCoSoDuLieu() {
        try {
            List<Promotion> danhSach = promotionController.layDanhSachKhuyenMai();
            danhSachKhuyenMai.setAll(danhSach);
            trangHienTai = 1;
            thucHienLocDanhSach(txtTimKiem.getText());
        } catch (Exception e) {
            hienThiCanhBao("Lỗi tải dữ liệu", "Không thể lấy danh sách khuyến mãi: " + e.getMessage());
        }
    }

    /**
     * Lọc danh sách theo từ khóa tìm kiếm.
     *
     * @param tuKhoa Chuỗi tìm kiếm
     */
    private void thucHienLocDanhSach(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            capNhatHienThiPhanTrang();
            return;
        }

        String keyword = tuKhoa.trim().toLowerCase();
        List<Promotion> filtered = danhSachKhuyenMai.stream()
                .filter(km -> km.getMaKhuyenMai().toLowerCase().contains(keyword)
                        || km.getTenChuongTrinh().toLowerCase().contains(keyword))
                .toList();

        tinhToanPhanTrangChoDanhSach(filtered);
    }

    /**
     * Cập nhật danh sách hiển thị trên trang hiện hành.
     */
    private void capNhatHienThiPhanTrang() {
        tinhToanPhanTrangChoDanhSach(danhSachKhuyenMai);
    }

    /**
     * Tính toán chỉ số phân trang và cập nhật UI.
     *
     * @param danhSachToanBo Danh sách đang cần phân trang
     */
    private void tinhToanPhanTrangChoDanhSach(List<Promotion> danhSachToanBo) {
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
     * Mở hộp thoại thêm mới khuyến mãi (Modal 2.1A).
     */
    private void hienThiHopThoaiThemMoi() {
        PromotionEditorDialog dialog = new PromotionEditorDialog(
                PromotionEditorDialog.CheDo.THEM_MOI,
                promotionController,
                null,
                kmMoi -> napDuLieuTuCoSoDuLieu(),
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở hộp thoại chỉnh sửa khuyến mãi đã chọn (Modal 2.1A · Sửa khuyến mãi).
     */
    private void hienThiHopThoaiCapNhat() {
        Promotion kmDaChon = tblKhuyenMai.getSelectionModel().getSelectedItem();
        if (kmDaChon == null) {
            hienThiCanhBao("Chưa chọn dòng", "Vui lòng chọn một chương trình khuyến mãi trong danh sách để chỉnh sửa.");
            return;
        }

        PromotionEditorDialog dialog = new PromotionEditorDialog(
                PromotionEditorDialog.CheDo.CAP_NHAT,
                promotionController,
                kmDaChon,
                kmCapNhat -> napDuLieuTuCoSoDuLieu(),
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở hộp thoại xác nhận xóa mềm khuyến mãi (Modal 2.1B · Ngừng-Xóa khuyến mãi).
     */
    private void hienThiHopThoaiXoaMem() {
        Promotion kmDaChon = tblKhuyenMai.getSelectionModel().getSelectedItem();
        if (kmDaChon == null) {
            hienThiCanhBao("Chưa chọn dòng", "Vui lòng chọn chương trình khuyến mãi cần ngừng hoạt động (xóa).");
            return;
        }

        PromotionDeleteDialog dialog = new PromotionDeleteDialog(
                kmDaChon,
                promotionController,
                this::napDuLieuTuCoSoDuLieu,
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Hiển thị một nội dung Node vào lớp Modal Overlay.
     *
     * @param modalNode Hộp thoại modal cần hiển thị
     */
    private void moModal(Node modalNode) {
        pnlModalOverlay.getChildren().setAll(modalNode);
        pnlModalOverlay.setVisible(true);
        pnlModalOverlay.setManaged(true);
    }

    /**
     * Đóng hộp thoại Modal Overlay.
     */
    private void dongModal() {
        pnlModalOverlay.getChildren().clear();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
    }

    /**
     * Hiển thị cảnh báo thông tin cho người dùng khi cần nhắc nhở.
     *
     * @param tieuDe   Tiêu đề cảnh báo
     * @param noiDung  Nội dung thông điệp
     */
    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

