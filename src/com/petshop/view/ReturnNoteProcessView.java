package com.petshop.view;

import com.petshop.controller.ReturnNoteController;
import com.petshop.model.ReturnNote;
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

import java.text.DecimalFormat;
import java.util.List;

/**
 * Màn hình ReturnNoteProcessView: Phiếu đổi trả > Xử lý đổi trả (Mẫu 3.2).
 * Cho phép kiểm tra yêu cầu đổi trả, lọc theo các tab trạng thái (Chờ xử lý, Đã duyệt, Đã từ chối),
 * tìm kiếm và mở modal duyệt hoặc từ chối phiếu.
 *
 * Tiêu chuẩn tuân thủ:
 * - Đặt tên điều khiển đúng tiền tố: btn, txt, lbl, tbl.
 * - Tên lớp là danh từ (ReturnNoteProcessView), tên phương thức là động từ.
 * - Thao tác từ chối phiếu tuân thủ xóa mềm/ngừng hoạt động (khớp tài liệu OOAD).
 * - Có bình luận 1 dòng và nhiều dòng chuẩn Javadoc.
 * - Khớp 100% mẫu thiết kế 3.2.
 */
public class ReturnNoteProcessView extends StackPane {

    private final ReturnNoteController returnNoteController;
    private final ObservableList<ReturnNote> danhSachKetQua = FXCollections.observableArrayList();
    private final ObservableList<ReturnNote> danhSachHienThiTrang = FXCollections.observableArrayList();

    // Tab trạng thái hiện tại ("Chờ xử lý", "Đã duyệt", "Đã từ chối")
    private String trangThaiTabHienTai = "Chờ xử lý";

    // Phân trang
    private int trangHienTai = 1;
    private int soDongMoiTrang = 10;
    private int tongSoTrang = 1;

    // Các thành phần giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;

    private Button btnTabChoXuLy;
    private Button btnTabDaDuyet;
    private Button btnTabDaTuChoi;

    private TextField txtTimKiem;
    private Button btnLamMoi;

    private TableView<ReturnNote> tblPhieuDoiTra;
    private TableColumn<ReturnNote, String> colMaPhieu;
    private TableColumn<ReturnNote, String> colHoaDon;
    private TableColumn<ReturnNote, String> colKhachHang;
    private TableColumn<ReturnNote, String> colTongTienHoan;
    private TableColumn<ReturnNote, String> colLyDo;
    private TableColumn<ReturnNote, String> colTrangThai;
    private TableColumn<ReturnNote, Void> colThaoTac;

    private Label lblTongBanGhi;
    private Label lblSoDongMoiTrang;
    private Button btnTrangTruoc;
    private Button btnTrangSau;

    private Label lblGhiChuHuongDan;

    // Lớp overlay hiển thị modal
    private StackPane pnlModalOverlay;

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    /**
     * Khởi tạo màn hình Xử lý đổi trả.
     */
    public ReturnNoteProcessView() {
        this.returnNoteController = new ReturnNoteController();

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieuTuCoSoDuLieu();
    }

    /**
     * Dựng cấu trúc giao diện theo mẫu thiết kế 3.2.
     */
    private void khoiTaoGiaoDien() {
        setStyle("-fx-background-color: #f3f6f3;");

        VBox pnlMainLayout = new VBox(16);
        pnlMainLayout.setPadding(new Insets(24, 28, 28, 28));

        // 1. Tiêu đề Breadcrumb & Mô tả
        VBox pnlHeaderBox = new VBox(4);
        lblTieuDe = new Label("Phiếu đổi trả > Xử lý đổi trả");
        lblTieuDe.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblPhuDe = new Label("Kiểm tra yêu cầu đổi trả, duyệt hoặc từ chối phiếu trước khi thực hiện hoàn tiền.");
        lblPhuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        pnlHeaderBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        // 2. Thẻ Card chính chứa Tabs, Search, Table, Pagination
        VBox pnlCard = new VBox(16);
        pnlCard.setPadding(new Insets(20, 22, 22, 22));
        pnlCard.getStyleClass().add("chart-panel");
        VBox.setVgrow(pnlCard, Priority.ALWAYS);

        // --- 2.1 Hàng Tabs / Filter Pills ---
        HBox pnlTabsRow = new HBox(10);
        pnlTabsRow.setAlignment(Pos.CENTER_LEFT);

        btnTabChoXuLy = new Button("Chờ xử lý (0)");
        btnTabChoXuLy.getStyleClass().add("tab-pill-active");

        btnTabDaDuyet = new Button("Đã duyệt (0)");
        btnTabDaDuyet.getStyleClass().add("tab-pill-inactive");

        btnTabDaTuChoi = new Button("Đã từ chối (0)");
        btnTabDaTuChoi.getStyleClass().add("tab-pill-inactive");

        pnlTabsRow.getChildren().addAll(btnTabChoXuLy, btnTabDaDuyet, btnTabDaTuChoi);

        // --- 2.2 Thanh công cụ tìm kiếm và làm mới ---
        HBox pnlSearchRow = new HBox(12);
        pnlSearchRow.setAlignment(Pos.CENTER_LEFT);

        HBox pnlSearchBox = new HBox(10);
        pnlSearchBox.setAlignment(Pos.CENTER_LEFT);
        pnlSearchBox.setStyle("""
                -fx-background-color: #ffffff;
                -fx-background-radius: 8px;
                -fx-border-color: #d1d5db;
                -fx-border-width: 1px;
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
        txtTimKiem.setPromptText("Tìm theo mã phiếu, hóa đơn, khách hàng...");
        txtTimKiem.getStyleClass().add("search-field");
        HBox.setHgrow(txtTimKiem, Priority.ALWAYS);

        pnlSearchBox.getChildren().addAll(searchIcon, txtTimKiem);

        btnLamMoi = new Button("⟳ Làm mới");
        btnLamMoi.getStyleClass().add("btn-action-secondary");

        pnlSearchRow.getChildren().addAll(pnlSearchBox, btnLamMoi);

        // --- 2.3 Bảng TableView Phiếu đổi trả ---
        tblPhieuDoiTra = new TableView<>();
        tblPhieuDoiTra.setPlaceholder(new Label("Không có phiếu đổi trả nào trong mục này."));
        tblPhieuDoiTra.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tblPhieuDoiTra, Priority.ALWAYS);

        colMaPhieu = new TableColumn<>("Mã phiếu");
        colMaPhieu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaPhieuDoiTra()));
        colMaPhieu.setPrefWidth(140);

        colHoaDon = new TableColumn<>("Hóa đơn");
        colHoaDon.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaHoaDon()));
        colHoaDon.setPrefWidth(140);

        colKhachHang = new TableColumn<>("Khách hàng");
        colKhachHang.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTenKhachHang() != null ? c.getValue().getTenKhachHang() : "Khách vãng lai"
        ));
        colKhachHang.setPrefWidth(160);

        colTongTienHoan = new TableColumn<>("Tổng tiền hoàn");
        colTongTienHoan.setCellValueFactory(c -> new SimpleStringProperty(DINH_DANG_TIEN.format(c.getValue().getTongTienHoan())));
        colTongTienHoan.setStyle("-fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
        colTongTienHoan.setPrefWidth(140);

        colLyDo = new TableColumn<>("Lý do");
        colLyDo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLyDo()));
        colLyDo.setPrefWidth(220);

        colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(120);
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    ReturnNote phieu = getTableRow().getItem();
                    Label lblStatus = new Label(phieu.getTrangThai());
                    switch (phieu.getTrangThai()) {
                        case "Đã duyệt" -> {
                            lblStatus.setStyle("-fx-text-fill: #15803d; -fx-font-weight: 600; -fx-background-color: #dcfce7; -fx-padding: 3px 8px; -fx-background-radius: 6px;");
                        }
                        case "Đã từ chối", "Từ chối" -> {
                            lblStatus.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: 600; -fx-background-color: #fee2e2; -fx-padding: 3px 8px; -fx-background-radius: 6px;");
                        }
                        default -> {
                            // Chờ xử lý
                            lblStatus.setStyle("-fx-text-fill: #c2410c; -fx-font-weight: 600;");
                        }
                    }
                    setGraphic(lblStatus);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Cột Thao tác: Duyệt & Từ chối
        colThaoTac = new TableColumn<>("Thao tác");
        colThaoTac.setPrefWidth(160);
        colThaoTac.setCellFactory(col -> new TableCell<>() {
            private final Button btnDuyet = new Button("Duyệt");
            private final Button btnTuChoi = new Button("Từ chối");
            private final HBox box = new HBox(8, btnDuyet, btnTuChoi);

            {
                btnDuyet.getStyleClass().add("btn-approve");
                btnTuChoi.getStyleClass().add("btn-reject");
                box.setAlignment(Pos.CENTER_LEFT);

                btnDuyet.setOnAction(e -> {
                    ReturnNote phieu = getTableView().getItems().get(getIndex());
                    hienThiXacNhanDuyet(phieu);
                });

                btnTuChoi.setOnAction(e -> {
                    ReturnNote phieu = getTableView().getItems().get(getIndex());
                    hienThiXacNhanTuChoi(phieu);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ReturnNote phieu = getTableRow().getItem();
                    if ("Chờ xử lý".equals(phieu.getTrangThai())) {
                        setGraphic(box);
                    } else {
                        // Khi đã duyệt hoặc đã từ chối thì ẩn nút thao tác
                        Label lblDaXuLy = new Label("Đã xử lý");
                        lblDaXuLy.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic; -fx-font-size: 12px;");
                        setGraphic(lblDaXuLy);
                    }
                }
            }
        });

        tblPhieuDoiTra.getColumns().addAll(List.of(
                colMaPhieu,
                colHoaDon,
                colKhachHang,
                colTongTienHoan,
                colLyDo,
                colTrangThai,
                colThaoTac
        ));
        tblPhieuDoiTra.setItems(danhSachHienThiTrang);

        // --- 2.4 Chân trang phân trang ---
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

        pnlFooter.getChildren().addAll(lblTongBanGhi, pnlFooterSpacer, lblSoDongMoiTrang, btnTrangTruoc, btnTrangSau);

        pnlCard.getChildren().addAll(pnlTabsRow, pnlSearchRow, tblPhieuDoiTra, pnlFooter);

        // 3. Khối ghi chú hướng dẫn dưới cùng
        lblGhiChuHuongDan = new Label("ⓘ Chọn Duyệt hoặc Từ chối để xem lại thông tin và xác nhận xử lý phiếu.");
        lblGhiChuHuongDan.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");

        pnlMainLayout.getChildren().addAll(pnlHeaderBox, pnlCard, lblGhiChuHuongDan);
        getChildren().add(pnlMainLayout);

        // Lớp hiển thị modal trên cùng
        pnlModalOverlay = new StackPane();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
        getChildren().add(pnlModalOverlay);
    }

    /**
     * Gắn bộ lắng nghe sự kiện.
     */
    private void ganSuKien() {
        // Chuyển Tabs
        btnTabChoXuLy.setOnAction(e -> chuyenTab("Chờ xử lý"));
        btnTabDaDuyet.setOnAction(e -> chuyenTab("Đã duyệt"));
        btnTabDaTuChoi.setOnAction(e -> chuyenTab("Đã từ chối"));

        // Tìm kiếm
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> {
            trangHienTai = 1;
            thucHienLocVaPhanTrang(newVal);
        });

        // Làm mới
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
    }

    /**
     * Chuyển tab trạng thái đang xem và nạp dữ liệu tương ứng.
     *
     * @param trangThaiMoi Tên trạng thái tab
     */
    private void chuyenTab(String trangThaiMoi) {
        this.trangThaiTabHienTai = trangThaiMoi;

        btnTabChoXuLy.getStyleClass().removeAll("tab-pill-active", "tab-pill-inactive");
        btnTabDaDuyet.getStyleClass().removeAll("tab-pill-active", "tab-pill-inactive");
        btnTabDaTuChoi.getStyleClass().removeAll("tab-pill-active", "tab-pill-inactive");

        btnTabChoXuLy.getStyleClass().add("Chờ xử lý".equals(trangThaiMoi) ? "tab-pill-active" : "tab-pill-inactive");
        btnTabDaDuyet.getStyleClass().add("Đã duyệt".equals(trangThaiMoi) ? "tab-pill-active" : "tab-pill-inactive");
        btnTabDaTuChoi.getStyleClass().add("Đã từ chối".equals(trangThaiMoi) ? "tab-pill-active" : "tab-pill-inactive");

        trangHienTai = 1;
        napDuLieuTuCoSoDuLieu();
    }

    /**
     * Nạp dữ liệu từ CSDL theo tab trạng thái hiện tại và cập nhật số đếm trên các Tab.
     */
    public void napDuLieuTuCoSoDuLieu() {
        try {
            // Cập nhật số lượng trên các tab
            int countChoXuLy = returnNoteController.demSoLuongTheoTrangThai("Chờ xử lý");
            int countDaDuyet = returnNoteController.demSoLuongTheoTrangThai("Đã duyệt");
            int countDaTuChoi = returnNoteController.demSoLuongTheoTrangThai("Đã từ chối");

            btnTabChoXuLy.setText("Chờ xử lý (" + countChoXuLy + ")");
            btnTabDaDuyet.setText("Đã duyệt (" + countDaDuyet + ")");
            btnTabDaTuChoi.setText("Đã từ chối (" + countDaTuChoi + ")");

            // Lấy danh sách theo tab
            List<ReturnNote> danhSach = returnNoteController.layPhieuTheoTrangThai(trangThaiTabHienTai);
            danhSachKetQua.setAll(danhSach);

            thucHienLocVaPhanTrang(txtTimKiem.getText());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi cơ sở dữ liệu");
            alert.setHeaderText(null);
            alert.setContentText("Không thể tải danh sách phiếu đổi trả: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Lọc danh sách theo từ khóa và phân trang.
     *
     * @param tuKhoa Từ khóa tìm kiếm
     */
    private void thucHienLocVaPhanTrang(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            capNhatHienThiPhanTrang();
            return;
        }

        String keyword = tuKhoa.trim().toLowerCase();
        List<ReturnNote> filtered = danhSachKetQua.stream()
                .filter(p -> p.getMaPhieuDoiTra().toLowerCase().contains(keyword)
                        || p.getMaHoaDon().toLowerCase().contains(keyword)
                        || (p.getTenKhachHang() != null && p.getTenKhachHang().toLowerCase().contains(keyword))
                        || p.getLyDo().toLowerCase().contains(keyword))
                .toList();

        tinhToanPhanTrang(filtered);
    }

    /**
     * Cập nhật hiển thị phân trang từ danh sách hiện hành.
     */
    private void capNhatHienThiPhanTrang() {
        tinhToanPhanTrang(danhSachKetQua);
    }

    /**
     * Tính toán chỉ số phân trang và cập nhật UI.
     *
     * @param danhSachToanBo Danh sách đang cần phân trang
     */
    private void tinhToanPhanTrang(List<ReturnNote> danhSachToanBo) {
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
     * Mở hộp thoại xác nhận duyệt phiếu đổi trả.
     *
     * @param phieu Phiếu cần duyệt
     */
    private void hienThiXacNhanDuyet(ReturnNote phieu) {
        if (phieu == null) return;
        ReturnNoteApproveDialog dialog = new ReturnNoteApproveDialog(
                phieu,
                () -> {
                    try {
                        returnNoteController.duyetPhieu(phieu.getMaPhieuDoiTra());
                        napDuLieuTuCoSoDuLieu();
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi duyệt phiếu");
                        alert.setHeaderText(null);
                        alert.setContentText("Không thể duyệt phiếu đổi trả: " + e.getMessage());
                        alert.showAndWait();
                    }
                },
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở hộp thoại xác nhận từ chối phiếu đổi trả.
     *
     * @param phieu Phiếu cần từ chối
     */
    private void hienThiXacNhanTuChoi(ReturnNote phieu) {
        if (phieu == null) return;
        ReturnNoteRejectDialog dialog = new ReturnNoteRejectDialog(
                phieu,
                () -> {
                    try {
                        returnNoteController.tuChoiPhieu(phieu.getMaPhieuDoiTra());
                        napDuLieuTuCoSoDuLieu();
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Lỗi từ chối phiếu");
                        alert.setHeaderText(null);
                        alert.setContentText("Không thể từ chối phiếu đổi trả: " + e.getMessage());
                        alert.showAndWait();
                    }
                },
                this::dongModal
        );
        moModal(dialog);
    }

    /**
     * Mở modal.
     */
    private void moModal(Node modalNode) {
        pnlModalOverlay.getChildren().setAll(modalNode);
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
}

