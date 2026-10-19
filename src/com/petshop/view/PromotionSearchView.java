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
 * Màn hình PromotionSearchView: Khuyến mãi > Tìm kiếm khuyến mãi.
 * Cung cấp chức năng tra cứu thông tin khuyến mãi theo mã, tên, trạng thái,
 * xem chi tiết chương trình ưu đãi và phân trang dữ liệu.
 *
 * Tiêu chuẩn tuân thủ:
 * - Đặt tên điều khiển đúng tiền tố: btn, txt, lbl, tbl.
 * - Tên lớp là danh từ (PromotionSearchView), tên phương thức là động từ.
 * - Có bình luận 1 dòng và nhiều dòng.
 * - Khớp hoàn toàn mẫu thiết kế Image 5.
 */
public class PromotionSearchView extends StackPane {

    private final PromotionController promotionController;
    private final ObservableList<Promotion> danhSachKetQua = FXCollections.observableArrayList();
    private final ObservableList<Promotion> danhSachHienThiTrang = FXCollections.observableArrayList();

    // Các điều khiển giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;

    private Label lblTieuDeKhuVuc;
    private Label lblSoLuongKetQua;
    private TextField txtTimKiem;
    private Button btnTimKiem;
    private Button btnLamMoi;

    private TableView<Promotion> tblKhuyenMai;
    private TableColumn<Promotion, String> colMaKhuyenMai;
    private TableColumn<Promotion, String> colThongTinChinh;
    private TableColumn<Promotion, String> colNgayCapNhat;
    private TableColumn<Promotion, String> colTrangThai;
    private TableColumn<Promotion, Void> colChiTiet;

    private Label lblTongBanGhi;
    private Label lblSoDongMoiTrang;
    private Button btnTrangTruoc;
    private Button btnTrangSau;

    // Phân trang: hiển thị 5 dòng / trang theo mẫu thiết kế
    private int trangHienTai = 1;
    private int soDongMoiTrang = 5;
    private int tongSoTrang = 1;

    // Lớp overlay modal
    private StackPane pnlModalOverlay;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo màn hình Tìm kiếm khuyến mãi.
     */
    public PromotionSearchView() {
        this.promotionController = new PromotionController();

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieuTuCoSoDuLieu();
    }

    /**
     * Dựng cấu trúc giao diện theo mẫu thiết kế Image 5.
     */
    private void khoiTaoGiaoDien() {
        setStyle("-fx-background-color: #f3f6f3;");

        VBox pnlMainLayout = new VBox(16);
        pnlMainLayout.setPadding(new Insets(24, 28, 28, 28));

        // 1. Tiêu đề Breadcrumb & Mô tả
        VBox pnlHeaderBox = new VBox(4);
        lblTieuDe = new Label("Khuyến mãi > Tìm kiếm khuyến mãi");
        lblTieuDe.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblPhuDe = new Label("Phân hệ quản lý Khuyến mãi (Sơ đồ hướng đối tượng chuẩn).");
        lblPhuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        pnlHeaderBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        // 2. Thẻ Card chứa form tìm kiếm và danh sách kết quả
        VBox pnlCard = new VBox(16);
        pnlCard.setPadding(new Insets(20, 22, 22, 22));
        pnlCard.getStyleClass().add("chart-panel");
        VBox.setVgrow(pnlCard, Priority.ALWAYS);

        // --- 2.1 Tiêu đề khu vực tìm kiếm + Huy hiệu số lượng ---
        HBox pnlSearchHeader = new HBox(8);
        pnlSearchHeader.setAlignment(Pos.CENTER_LEFT);

        lblTieuDeKhuVuc = new Label("Tìm kiếm khuyến mãi");
        lblTieuDeKhuVuc.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblSoLuongKetQua = new Label("0");
        lblSoLuongKetQua.getStyleClass().add("badge-count");

        pnlSearchHeader.getChildren().addAll(lblTieuDeKhuVuc, lblSoLuongKetQua);

        // --- 2.2 Thanh công cụ tìm kiếm ---
        HBox pnlSearchRow = new HBox(12);
        pnlSearchRow.setAlignment(Pos.CENTER_LEFT);

        // Khung ô nhập tìm kiếm rộng có viền xanh lá nhạt
        HBox pnlSearchBox = new HBox(10);
        pnlSearchBox.setAlignment(Pos.CENTER_LEFT);
        pnlSearchBox.setStyle("""
                -fx-background-color: #ffffff;
                -fx-background-radius: 8px;
                -fx-border-color: #3e634c;
                -fx-border-width: 1.2px;
                -fx-border-radius: 8px;
                -fx-padding: 0 14px;
                -fx-pref-height: 40px;
                """);
        HBox.setHgrow(pnlSearchBox, Priority.ALWAYS);

        SVGPath searchIcon = new SVGPath();
        searchIcon.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
        searchIcon.setFill(Color.web("#9ca89e"));
        searchIcon.setScaleX(0.7);
        searchIcon.setScaleY(0.7);

        txtTimKiem = new TextField();
        txtTimKiem.setPromptText("Tìm kiếm theo mã, tên, loại...");
        txtTimKiem.getStyleClass().add("search-field");
        HBox.setHgrow(txtTimKiem, Priority.ALWAYS);

        pnlSearchBox.getChildren().addAll(searchIcon, txtTimKiem);

        // Nút Tìm kiếm (Màu xanh đậm theo thiết kế)
        btnTimKiem = new Button("Tìm kiếm");
        btnTimKiem.getStyleClass().add("btn-green-search");

        // Nút Làm mới
        btnLamMoi = new Button("⟳ Làm mới");
        btnLamMoi.getStyleClass().add("btn-action-secondary");

        pnlSearchRow.getChildren().addAll(pnlSearchBox, btnTimKiem, btnLamMoi);

        // --- 2.3 Bảng TableView Kết quả tìm kiếm ---
        tblKhuyenMai = new TableView<>();
        tblKhuyenMai.setPlaceholder(new Label("Không tìm thấy chương trình khuyến mãi nào phù hợp."));
        tblKhuyenMai.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblKhuyenMai, Priority.ALWAYS);

        colMaKhuyenMai = new TableColumn<>("Mã định danh");
        colMaKhuyenMai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaKhuyenMai()));
        colMaKhuyenMai.setPrefWidth(140);

        colThongTinChinh = new TableColumn<>("Thông tin chính");
        colThongTinChinh.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenChuongTrinh()));
        colThongTinChinh.setPrefWidth(320);

        colNgayCapNhat = new TableColumn<>("Ngày cập nhật");
        colNgayCapNhat.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getNgayCapNhat();
            return new SimpleStringProperty((d != null) ? d.format(DINH_DANG_NGAY) : "");
        });
        colNgayCapNhat.setPrefWidth(140);

        colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(150);
        colTrangThai.setCellFactory(col -> new TableCell<>() {
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

        // Cột Hành động "Chi tiết"
        colChiTiet = new TableColumn<>("");
        colChiTiet.setPrefWidth(100);
        colChiTiet.setCellFactory(col -> new TableCell<>() {
            private final Button btnChiTiet = new Button("Chi tiết");
            {
                btnChiTiet.getStyleClass().add("btn-detail-link");
                btnChiTiet.setOnAction(e -> {
                    Promotion km = getTableView().getItems().get(getIndex());
                    hienThiChiTietKhuyenMai(km);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btnChiTiet);
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });

        tblKhuyenMai.getColumns().addAll(List.of(
                colMaKhuyenMai,
                colThongTinChinh,
                colNgayCapNhat,
                colTrangThai,
                colChiTiet
        ));
        tblKhuyenMai.setItems(danhSachHienThiTrang);

        // --- 2.4 Chân trang phân trang (5 dòng / trang) ---
        HBox pnlFooter = new HBox(12);
        pnlFooter.setAlignment(Pos.CENTER_LEFT);
        pnlFooter.setPadding(new Insets(10, 4, 0, 4));

        lblTongBanGhi = new Label("Tổng cộng 0 bản ghi");
        lblTongBanGhi.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");

        Region pnlFooterSpacer = new Region();
        HBox.setHgrow(pnlFooterSpacer, Priority.ALWAYS);

        lblSoDongMoiTrang = new Label("5 dòng / trang");
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

        pnlCard.getChildren().addAll(pnlSearchHeader, pnlSearchRow, tblKhuyenMai, pnlFooter);
        pnlMainLayout.getChildren().addAll(pnlHeaderBox, pnlCard);

        getChildren().add(pnlMainLayout);

        // Lớp hiển thị modal chi tiết trên cùng
        pnlModalOverlay = new StackPane();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
        getChildren().add(pnlModalOverlay);
    }

    /**
     * Gắn bộ lắng nghe sự kiện tìm kiếm và điều hướng trang.
     */
    private void ganSuKien() {
        // Nút Tìm kiếm và phím Enter: Tải lại toàn bộ từ CSDL rồi lọc
        btnTimKiem.setOnAction(e -> thucHienTimKiem());
        txtTimKiem.setOnAction(e -> thucHienTimKiem());

        // Gõ phím thời gian thực: lọc trên bộ đệm đã tải (tránh gọi CSDL mỗi ký tự)
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> {
            trangHienTai = 1;
            thucHienLocVaPhanTrang(newVal);
        });

        // Nút Làm mới
        btnLamMoi.setOnAction(e -> {
            txtTimKiem.clear();
            napDuLieuTuCoSoDuLieu();
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

        // Double-click mở chi tiết
        tblKhuyenMai.setRowFactory(tv -> {
            TableRow<Promotion> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    hienThiChiTietKhuyenMai(row.getItem());
                }
            });
            return row;
        });
    }

    /**
     * Nạp toàn bộ danh sách khuyến mãi từ CSDL.
     */
    public void napDuLieuTuCoSoDuLieu() {
        try {
            List<Promotion> danhSach = promotionController.layDanhSachKhuyenMai();
            danhSachKetQua.setAll(danhSach);
            trangHienTai = 1;
            thucHienLocVaPhanTrang(txtTimKiem.getText());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi cơ sở dữ liệu");
            alert.setHeaderText(null);
            alert.setContentText("Không thể tải danh sách khuyến mãi: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Tải lại dữ liệu từ CSDL, sau đó lọc trong bộ nhớ theo từ khóa.
     * Đảm bảo dữ liệu của màn hình Tìm kiếm và Cập nhật luôn đồng nhất từ cùng một nguồn.
     */
    private void thucHienTimKiem() {
        try {
            // Luôn lấy lại toàn bộ danh sách từ CSDL trước khi lọc
            List<Promotion> tatCa = promotionController.layDanhSachKhuyenMai();
            danhSachKetQua.setAll(tatCa);
            trangHienTai = 1;
            // Sau đó lọc trong bộ nhớ theo từ khóa hiện tại
            thucHienLocVaPhanTrang(txtTimKiem.getText());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi tìm kiếm");
            alert.setHeaderText(null);
            alert.setContentText("Không thể tìm kiếm: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Lọc danh sách tại bộ nhớ theo từ khóa và phân trang.
     *
     * @param tuKhoa Từ khóa tìm kiếm
     */
    private void thucHienLocVaPhanTrang(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            capNhatHienThiPhanTrang();
            return;
        }

        String keyword = tuKhoa.trim().toLowerCase();
        List<Promotion> filtered = danhSachKetQua.stream()
                .filter(km -> km.getMaKhuyenMai().toLowerCase().contains(keyword)
                        || km.getTenChuongTrinh().toLowerCase().contains(keyword)
                        || km.layTrangThaiHienThi().toLowerCase().contains(keyword))
                .toList();

        tinhToanPhanTrang(filtered);
    }

    /**
     * Cập nhật danh sách hiển thị trên trang hiện hành.
     */
    private void capNhatHienThiPhanTrang() {
        tinhToanPhanTrang(danhSachKetQua);
    }

    /**
     * Tính toán chỉ số phân trang và cập nhật UI.
     *
     * @param danhSachToanBo Danh sách đang cần phân trang
     */
    private void tinhToanPhanTrang(List<Promotion> danhSachToanBo) {
        int tongSoLuong = danhSachToanBo.size();
        lblSoLuongKetQua.setText(String.valueOf(tongSoLuong));
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
     * Mở hộp thoại hiển thị chi tiết một chương trình khuyến mãi.
     *
     * @param km Khuyến mãi cần xem chi tiết
     */
    private void hienThiChiTietKhuyenMai(Promotion km) {
        if (km == null) return;
        PromotionDetailDialog dialog = new PromotionDetailDialog(km, this::dongModal);
        moModal(dialog);
    }

    /**
     * Mở modal trên lớp overlay.
     *
     * @param modalNode Node giao diện modal
     */
    private void moModal(Node modalNode) {
        pnlModalOverlay.getChildren().setAll(modalNode);
        pnlModalOverlay.setVisible(true);
        pnlModalOverlay.setManaged(true);
    }

    /**
     * Đóng modal trên lớp overlay.
     */
    private void dongModal() {
        pnlModalOverlay.getChildren().clear();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
    }
}

