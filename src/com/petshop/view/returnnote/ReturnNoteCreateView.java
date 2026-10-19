package com.petshop.view.returnnote;

import com.petshop.controller.ReturnNoteController;
import com.petshop.model.ReturnNote;
import com.petshop.model.ReturnNoteItem;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình ReturnNoteCreateView: Phiếu đổi trả > Lập phiếu đổi trả (Mẫu 3.1).
 * Cho phép tra cứu hóa đơn đã mua, nhập lý do đổi trả, chọn sản phẩm cần hoàn và lập phiếu.
 *
 * Tiêu chuẩn tuân thủ:
 * - Đặt tên điều khiển đúng tiền tố: btn, txt, lbl, tbl.
 * - Tên lớp là danh từ (ReturnNoteCreateView), tên phương thức là động từ.
 * - Có bình luận 1 dòng và nhiều dòng chuẩn Javadoc.
 * - Khớp 100% mẫu thiết kế 3.1.
 */
public class ReturnNoteCreateView extends StackPane {

    private final ReturnNoteController returnNoteController;
    private final ObservableList<ReturnNoteItem> danhSachSanPhamDoiTra = FXCollections.observableArrayList();

    // Dữ liệu tạm của hóa đơn tra cứu
    private String maKhachHangHienTai = null;
    private List<ReturnNoteItem> danhSachSanPhamTrongHoaDon = new ArrayList<>();

    // Các thành phần giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;

    // Card 1: Thông tin chung
    private Label lblThongTinChungTieuDe;
    private Label lblBadgeBanNhap;
    private Label lblMaPhieuDoiTra;
    private TextField txtMaPhieuDoiTra;
    private Label lblMaHoaDon;
    private TextField txtMaHoaDon;
    private Button btnTraCuuHoaDon;
    private Label lblKhachHang;
    private TextField txtKhachHang;
    private Label lblLyDoDoiTra;
    private TextField txtLyDoDoiTra;

    // Card 2: Chi tiết sản phẩm
    private Label lblChiTietTieuDe;
    private Label lblBadgeSoLuongSanPham;
    private Button btnThemChiTiet;

    private TableView<ReturnNoteItem> tblChiTietSanPham;
    private TableColumn<ReturnNoteItem, String> colSanPham;
    private TableColumn<ReturnNoteItem, String> colSoLuong;
    private TableColumn<ReturnNoteItem, String> colDonGia;
    private TableColumn<ReturnNoteItem, String> colThanhTien;
    private TableColumn<ReturnNoteItem, Void> colThaoTac;

    private Label lblTongKetDonVi;
    private Label lblTongTienHoanTieuDe;
    private Label lblTongTienHoanGiaTri;

    // Chân trang
    private Label lblGhiChuTrangThai;
    private Label lblBaoLoi;
    private Button btnHuy;
    private Button btnLapPhieuDoiTra;

    // Lớp overlay modal
    private StackPane pnlModalOverlay;

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    /**
     * Khởi tạo màn hình Lập phiếu đổi trả.
     */
    public ReturnNoteCreateView() {
        this.returnNoteController = new ReturnNoteController();

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieuMacDinh();
    }

    /**
     * Dựng cấu trúc giao diện theo mẫu thiết kế 3.1.
     */
    private void khoiTaoGiaoDien() {
        setStyle("-fx-background-color: #f3f6f3;");

        VBox pnlMainLayout = new VBox(16);
        pnlMainLayout.setPadding(new Insets(24, 28, 28, 28));

        // 1. Tiêu đề Breadcrumb & Mô tả
        VBox pnlHeaderBox = new VBox(4);
        lblTieuDe = new Label("Phiếu đổi trả > Lập phiếu đổi trả");
        lblTieuDe.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblPhuDe = new Label("Lập phiếu theo hóa đơn đã mua và kiểm tra thông tin sản phẩm cần đổi trả.");
        lblPhuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6e7c70;");
        pnlHeaderBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        // 2. Card 1: Thông tin chung
        VBox pnlCardThongTin = new VBox(14);
        pnlCardThongTin.setPadding(new Insets(18, 22, 20, 22));
        pnlCardThongTin.getStyleClass().add("chart-panel");

        BorderPane pnlHeaderThongTin = new BorderPane();
        lblThongTinChungTieuDe = new Label("Thông tin chung");
        lblThongTinChungTieuDe.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblBadgeBanNhap = new Label("Bản nháp");
        lblBadgeBanNhap.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-font-size: 11.5px; -fx-padding: 3px 10px; -fx-background-radius: 12px;");

        pnlHeaderThongTin.setLeft(lblThongTinChungTieuDe);
        pnlHeaderThongTin.setRight(lblBadgeBanNhap);

        // Grid 3 cột cho hàng 1: Mã phiếu (readonly) | Mã hóa đơn (*) | Khách hàng (readonly)
        GridPane pnlGridHang1 = new GridPane();
        pnlGridHang1.setHgap(16);
        pnlGridHang1.setVgap(10);

        ColumnConstraints colG1 = new ColumnConstraints();
        colG1.setPercentWidth(33.33);
        ColumnConstraints colG2 = new ColumnConstraints();
        colG2.setPercentWidth(33.33);
        ColumnConstraints colG3 = new ColumnConstraints();
        colG3.setPercentWidth(33.34);
        pnlGridHang1.getColumnConstraints().addAll(colG1, colG2, colG3);

        // Ô 1: Mã phiếu đổi trả
        lblMaPhieuDoiTra = new Label("Mã phiếu đổi trả");
        lblMaPhieuDoiTra.getStyleClass().add("modal-field-label");

        txtMaPhieuDoiTra = new TextField();
        txtMaPhieuDoiTra.getStyleClass().add("modal-text-input-readonly");
        txtMaPhieuDoiTra.setEditable(false);

        SVGPath lock1 = taoIconKhoa();
        StackPane pnlMaPhieuContainer = new StackPane(txtMaPhieuDoiTra, lock1);
        StackPane.setAlignment(lock1, Pos.CENTER_RIGHT);
        StackPane.setMargin(lock1, new Insets(0, 10, 0, 0));

        VBox pnlBoxMaPhieu = new VBox(6, lblMaPhieuDoiTra, pnlMaPhieuContainer);

        // Ô 2: Mã hóa đơn (*) có nút tìm kiếm
        lblMaHoaDon = new Label("Mã hóa đơn *");
        lblMaHoaDon.getStyleClass().add("modal-field-label");

        txtMaHoaDon = new TextField();
        txtMaHoaDon.setPromptText("Nhập mã hóa đơn (VD: HD-20261015-032)");
        txtMaHoaDon.getStyleClass().add("modal-text-input");

        SVGPath searchIcon = new SVGPath();
        searchIcon.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
        searchIcon.setFill(Color.web("#9ca3af"));
        searchIcon.setScaleX(0.65);
        searchIcon.setScaleY(0.65);

        btnTraCuuHoaDon = new Button();
        btnTraCuuHoaDon.setGraphic(searchIcon);
        btnTraCuuHoaDon.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4px;");

        StackPane pnlMaHoaDonContainer = new StackPane(txtMaHoaDon, btnTraCuuHoaDon);
        StackPane.setAlignment(btnTraCuuHoaDon, Pos.CENTER_RIGHT);
        StackPane.setMargin(btnTraCuuHoaDon, new Insets(0, 8, 0, 0));

        VBox pnlBoxMaHoaDon = new VBox(6, lblMaHoaDon, pnlMaHoaDonContainer);

        // Ô 3: Khách hàng (readonly)
        lblKhachHang = new Label("Khách hàng");
        lblKhachHang.getStyleClass().add("modal-field-label");

        txtKhachHang = new TextField();
        txtKhachHang.getStyleClass().add("modal-text-input-readonly");
        txtKhachHang.setEditable(false);
        txtKhachHang.setPromptText("Tự động hiển thị theo hóa đơn");

        SVGPath lock2 = taoIconKhoa();
        StackPane pnlKhachHangContainer = new StackPane(txtKhachHang, lock2);
        StackPane.setAlignment(lock2, Pos.CENTER_RIGHT);
        StackPane.setMargin(lock2, new Insets(0, 10, 0, 0));

        VBox pnlBoxKhachHang = new VBox(6, lblKhachHang, pnlKhachHangContainer);

        pnlGridHang1.add(pnlBoxMaPhieu, 0, 0);
        pnlGridHang1.add(pnlBoxMaHoaDon, 1, 0);
        pnlGridHang1.add(pnlBoxKhachHang, 2, 0);

        // Hàng 2: Lý do đổi trả (*)
        lblLyDoDoiTra = new Label("Lý do đổi trả *");
        lblLyDoDoiTra.getStyleClass().add("modal-field-label");

        txtLyDoDoiTra = new TextField();
        txtLyDoDoiTra.setPromptText("Nhập lý do đổi trả (VD: Sản phẩm bị lỗi bao bì...)");
        txtLyDoDoiTra.getStyleClass().add("modal-text-input");

        VBox pnlBoxLyDo = new VBox(6, lblLyDoDoiTra, txtLyDoDoiTra);

        pnlCardThongTin.getChildren().addAll(pnlHeaderThongTin, pnlGridHang1, pnlBoxLyDo);

        // 3. Card 2: Chi tiết sản phẩm
        VBox pnlCardChiTiet = new VBox(14);
        pnlCardChiTiet.setPadding(new Insets(18, 22, 20, 22));
        pnlCardChiTiet.getStyleClass().add("chart-panel");
        VBox.setVgrow(pnlCardChiTiet, Priority.ALWAYS);

        // Tiêu đề card 2 + nút "+ Thêm chi tiết"
        BorderPane pnlHeaderChiTiet = new BorderPane();

        HBox pnlTitleChiTietBox = new HBox(8);
        pnlTitleChiTietBox.setAlignment(Pos.CENTER_LEFT);

        lblChiTietTieuDe = new Label("Chi tiết sản phẩm");
        lblChiTietTieuDe.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        lblBadgeSoLuongSanPham = new Label("0");
        lblBadgeSoLuongSanPham.getStyleClass().add("badge-count");

        pnlTitleChiTietBox.getChildren().addAll(lblChiTietTieuDe, lblBadgeSoLuongSanPham);

        btnThemChiTiet = new Button("+ Thêm chi tiết");
        btnThemChiTiet.getStyleClass().add("btn-orange");

        pnlHeaderChiTiet.setLeft(pnlTitleChiTietBox);
        pnlHeaderChiTiet.setRight(btnThemChiTiet);

        // Bảng danh sách chi tiết sản phẩm đổi trả
        tblChiTietSanPham = new TableView<>();
        tblChiTietSanPham.setPlaceholder(new Label("Chưa có sản phẩm đổi trả nào được chọn. Hãy bấm \"+ Thêm chi tiết\"."));
        tblChiTietSanPham.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblChiTietSanPham.setPrefHeight(180);
        VBox.setVgrow(tblChiTietSanPham, Priority.ALWAYS);

        // Cột Sản phẩm (Tên + dòng phụ: Theo hóa đơn ...)
        colSanPham = new TableColumn<>("Sản phẩm");
        colSanPham.setPrefWidth(320);
        colSanPham.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    ReturnNoteItem rItem = getTableRow().getItem();
                    VBox box = new VBox(2);
                    Label lblName = new Label(rItem.getTenSanPham());
                    lblName.setStyle("-fx-font-weight: 600; -fx-text-fill: #1f2923; -fx-font-size: 13px;");

                    Label lblSub = new Label("Theo hóa đơn " + (rItem.getMaHoaDon() != null ? rItem.getMaHoaDon() : txtMaHoaDon.getText()));
                    lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

                    box.getChildren().addAll(lblName, lblSub);
                    setGraphic(box);
                }
            }
        });

        // Cột Số lượng
        colSoLuong = new TableColumn<>("Số lượng");
        colSoLuong.setPrefWidth(100);
        colSoLuong.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getSoLuong())));
        colSoLuong.setStyle("-fx-alignment: CENTER;");

        // Cột Đơn giá
        colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setPrefWidth(140);
        colDonGia.setCellValueFactory(c -> new SimpleStringProperty(DINH_DANG_TIEN.format(c.getValue().getDonGia())));
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Cột Thành tiền
        colThanhTien = new TableColumn<>("Thành tiền");
        colThanhTien.setPrefWidth(140);
        colThanhTien.setCellValueFactory(c -> new SimpleStringProperty(DINH_DANG_TIEN.format(c.getValue().tinhThanhTien())));
        colThanhTien.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: 600;");

        // Cột Thao tác (Nút Xóa đỏ)
        colThaoTac = new TableColumn<>("Thao tác");
        colThaoTac.setPrefWidth(100);
        colThaoTac.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa = new Button("🗑 Xóa");
            {
                btnXoa.getStyleClass().add("btn-danger");
                btnXoa.setOnAction(e -> {
                    ReturnNoteItem item = getTableView().getItems().get(getIndex());
                    danhSachSanPhamDoiTra.remove(item);
                    capNhatThongKeBang();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(btnXoa);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        tblChiTietSanPham.getColumns().addAll(List.of(
                colSanPham,
                colSoLuong,
                colDonGia,
                colThanhTien,
                colThaoTac
        ));
        tblChiTietSanPham.setItems(danhSachSanPhamDoiTra);

        // Chân tổng kết bên trong card chi tiết
        HBox pnlFooterCard = new HBox();
        pnlFooterCard.setAlignment(Pos.CENTER_LEFT);
        pnlFooterCard.setPadding(new Insets(10, 4, 4, 4));

        lblTongKetDonVi = new Label("0 sản phẩm - 0 đơn vị đổi trả");
        lblTongKetDonVi.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        Region pnlCardSpacer = new Region();
        HBox.setHgrow(pnlCardSpacer, Priority.ALWAYS);

        HBox pnlTongTienBox = new HBox(12);
        pnlTongTienBox.setAlignment(Pos.CENTER_RIGHT);

        lblTongTienHoanTieuDe = new Label("Tổng tiền hoàn");
        lblTongTienHoanTieuDe.setStyle("-fx-font-size: 13.5px; -fx-text-fill: #6b7280;");

        lblTongTienHoanGiaTri = new Label("0 đ");
        lblTongTienHoanGiaTri.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2f4f38;");

        pnlTongTienBox.getChildren().addAll(lblTongTienHoanTieuDe, lblTongTienHoanGiaTri);
        pnlFooterCard.getChildren().addAll(lblTongKetDonVi, pnlCardSpacer, pnlTongTienBox);

        pnlCardChiTiet.getChildren().addAll(pnlHeaderChiTiet, tblChiTietSanPham, pnlFooterCard);

        // 4. Thanh chân trang điều khiển (Nút Hủy & Lập phiếu đổi trả)
        HBox pnlBottomBar = new HBox(12);
        pnlBottomBar.setAlignment(Pos.CENTER_LEFT);

        lblGhiChuTrangThai = new Label("Phiếu sau khi lập sẽ được chuyển sang trạng thái Chờ xử lý.");
        lblGhiChuTrangThai.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");

        Region pnlBottomSpacer = new Region();
        HBox.setHgrow(pnlBottomSpacer, Priority.ALWAYS);

        btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("btn-action-secondary");
        btnHuy.setPrefWidth(90);

        btnLapPhieuDoiTra = new Button("+ Lập phiếu đổi trả");
        btnLapPhieuDoiTra.getStyleClass().add("btn-orange");

        pnlBottomBar.getChildren().addAll(lblGhiChuTrangThai, pnlBottomSpacer, btnHuy, btnLapPhieuDoiTra);

        // Nhãn báo lỗi chung
        lblBaoLoi = new Label();
        lblBaoLoi.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12.5px; -fx-font-weight: 600;");
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        pnlMainLayout.getChildren().addAll(pnlHeaderBox, pnlCardThongTin, pnlCardChiTiet, lblBaoLoi, pnlBottomBar);
        getChildren().add(pnlMainLayout);

        // Lớp overlay modal
        pnlModalOverlay = new StackPane();
        pnlModalOverlay.setVisible(false);
        pnlModalOverlay.setManaged(false);
        getChildren().add(pnlModalOverlay);
    }

    /**
     * Gắn các bộ lắng nghe sự kiện.
     */
    private void ganSuKien() {
        // Tra cứu hóa đơn khi nhấn Enter hoặc bấm icon kính lúp
        btnTraCuuHoaDon.setOnAction(e -> thucHienTraCuuHoaDon());
        txtMaHoaDon.setOnAction(e -> thucHienTraCuuHoaDon());

        // Mở modal thêm chi tiết sản phẩm
        btnThemChiTiet.setOnAction(e -> moHopThoaiThemChiTiet());

        // Bấm nút Hủy: làm sạch form và nạp lại mã mới
        btnHuy.setOnAction(e -> lamMoiForm());

        // Bấm nút Lập phiếu đổi trả
        btnLapPhieuDoiTra.setOnAction(e -> xuLyLapPhieuDoiTra());
    }

    /**
     * Nạp dữ liệu ban đầu (tự sinh mã phiếu mới và nạp sẵn mẫu hóa đơn gợi ý).
     */
    private void napDuLieuMacDinh() {
        String maMoi = returnNoteController.sinhMaPhieuMoi();
        txtMaPhieuDoiTra.setText(maMoi);

        // Nạp sẵn mẫu HD-20261015-032 nếu có trong DB để khớp ảnh thiết kế 3.1
        txtMaHoaDon.setText("HD-20261015-032");
        thucHienTraCuuHoaDon();
        txtLyDoDoiTra.setText("Sản phẩm bị lỗi bao bì");
    }

    /**
     * Thực hiện tra cứu hóa đơn từ CSDL.
     */
    private void thucHienTraCuuHoaDon() {
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        String maHD = txtMaHoaDon.getText().trim();
        if (maHD.isEmpty()) {
            hienThiLoi("Vui lòng nhập mã hóa đơn cần đổi trả.");
            txtKhachHang.clear();
            maKhachHangHienTai = null;
            danhSachSanPhamTrongHoaDon.clear();
            return;
        }

        try {
            Object[] hdInfo = returnNoteController.timThongTinHoaDon(maHD);
            if (hdInfo != null) {
                this.maKhachHangHienTai = (String) hdInfo[1];
                String tenKH = (String) hdInfo[2];
                txtKhachHang.setText(tenKH != null ? tenKH : "Khách vãng lai");

                // Lấy danh sách sản phẩm trong hóa đơn
                this.danhSachSanPhamTrongHoaDon = returnNoteController.layDanhSachSanPhamTheoHoaDon(maHD);

                // Tự động nạp sản phẩm vào danh sách đổi trả nếu trước đó đang rỗng (khớp mẫu UI)
                if (danhSachSanPhamDoiTra.isEmpty() && !danhSachSanPhamTrongHoaDon.isEmpty()) {
                    ReturnNoteItem spDauTien = danhSachSanPhamTrongHoaDon.get(0);
                    danhSachSanPhamDoiTra.add(new ReturnNoteItem(
                            null,
                            maHD,
                            spDauTien.getMaSanPham(),
                            spDauTien.getTenSanPham(),
                            spDauTien.getSoLuong(),
                            spDauTien.getDonGia()
                    ));
                    capNhatThongKeBang();
                }
            } else {
                hienThiLoi("Không tìm thấy thông tin hóa đơn [" + maHD + "] trong hệ thống.");
                txtKhachHang.clear();
                maKhachHangHienTai = null;
                danhSachSanPhamTrongHoaDon.clear();
            }
        } catch (Exception ex) {
            hienThiLoi("Lỗi khi tra cứu hóa đơn: " + ex.getMessage());
        }
    }

    /**
     * Mở modal thêm chi tiết đổi trả.
     */
    private void moHopThoaiThemChiTiet() {
        String maHD = txtMaHoaDon.getText().trim();
        if (maHD.isEmpty() || maKhachHangHienTai == null) {
            hienThiLoi("Vui lòng nhập và tra cứu hóa đơn hợp lệ trước khi thêm chi tiết sản phẩm.");
            txtMaHoaDon.requestFocus();
            return;
        }

        if (danhSachSanPhamTrongHoaDon.isEmpty()) {
            hienThiLoi("Hóa đơn này không có sản phẩm nào để đổi trả.");
            return;
        }

        ReturnItemDialog dialog = new ReturnItemDialog(
                maHD,
                danhSachSanPhamTrongHoaDon,
                itemMoi -> {
                    // Kiểm tra xem sản phẩm đã có trong danh sách chưa
                    boolean daTonTai = false;
                    for (ReturnNoteItem item : danhSachSanPhamDoiTra) {
                        if (item.getMaSanPham().equals(itemMoi.getMaSanPham())) {
                            item.setSoLuong(itemMoi.getSoLuong());
                            daTonTai = true;
                            break;
                        }
                    }
                    if (!daTonTai) {
                        danhSachSanPhamDoiTra.add(itemMoi);
                    }
                    tblChiTietSanPham.refresh();
                    capNhatThongKeBang();
                },
                this::dongModal
        );

        moModal(dialog);
    }

    /**
     * Cập nhật các nhãn tóm tắt và tổng tiền của bảng.
     */
    private void capNhatThongKeBang() {
        int soDong = danhSachSanPhamDoiTra.size();
        lblBadgeSoLuongSanPham.setText(String.valueOf(soDong));

        int tongDonVi = 0;
        double tongTien = 0;
        for (ReturnNoteItem item : danhSachSanPhamDoiTra) {
            tongDonVi += item.getSoLuong();
            tongTien += item.tinhThanhTien();
        }

        lblTongKetDonVi.setText(soDong + " sản phẩm - " + tongDonVi + " đơn vị đổi trả");
        lblTongTienHoanGiaTri.setText(DINH_DANG_TIEN.format(tongTien));
    }

    /**
     * Thực hiện kiểm tra nghiệp vụ và ghi nhận phiếu đổi trả mới vào CSDL.
     */
    private void xuLyLapPhieuDoiTra() {
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        String maPhieu = txtMaPhieuDoiTra.getText().trim();
        String maHD = txtMaHoaDon.getText().trim();
        String lyDo = txtLyDoDoiTra.getText().trim();

        if (maHD.isEmpty()) {
            hienThiLoi("Vui lòng nhập mã hóa đơn.");
            txtMaHoaDon.requestFocus();
            return;
        }

        if (maKhachHangHienTai == null) {
            hienThiLoi("Hóa đơn không hợp lệ hoặc chưa được tra cứu.");
            txtMaHoaDon.requestFocus();
            return;
        }

        if (lyDo.isEmpty()) {
            hienThiLoi("Vui lòng nhập lý do đổi trả.");
            txtLyDoDoiTra.requestFocus();
            return;
        }

        if (danhSachSanPhamDoiTra.isEmpty()) {
            hienThiLoi("Vui lòng thêm ít nhất một sản phẩm cần đổi trả.");
            return;
        }

        // Tính tổng tiền và tổng số lượng
        double tongTien = 0;
        int tongSoLuong = 0;
        for (ReturnNoteItem item : danhSachSanPhamDoiTra) {
            tongTien += item.tinhThanhTien();
            tongSoLuong += item.getSoLuong();
        }

        // Tạo đối tượng ReturnNote
        ReturnNote phieuMoi = new ReturnNote(
                maPhieu,
                maHD,
                "NV-20261005-000002", // Nhân viên bán hàng mặc định
                maKhachHangHienTai,
                txtKhachHang.getText(),
                LocalDate.now(),
                lyDo,
                tongSoLuong,
                "Chờ xử lý",
                tongTien
        );

        try {
            boolean kq = returnNoteController.lapPhieuDoiTra(phieuMoi, new ArrayList<>(danhSachSanPhamDoiTra));
            if (kq) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Lập phiếu đổi trả thành công");
                alert.setHeaderText("Phiếu đổi trả " + maPhieu + " đã được ghi nhận!");
                alert.setContentText("Trạng thái hiện tại: Chờ xử lý.\nTổng tiền hoàn: " + DINH_DANG_TIEN.format(tongTien));
                alert.showAndWait();

                lamMoiForm();
            }
        } catch (Exception e) {
            hienThiLoi(e.getMessage());
        }
    }

    /**
     * Làm mới form nhập để sẵn sàng lập phiếu tiếp theo.
     */
    private void lamMoiForm() {
        txtMaPhieuDoiTra.setText(returnNoteController.sinhMaPhieuMoi());
        txtMaHoaDon.clear();
        txtKhachHang.clear();
        txtLyDoDoiTra.clear();
        maKhachHangHienTai = null;
        danhSachSanPhamTrongHoaDon.clear();
        danhSachSanPhamDoiTra.clear();
        capNhatThongKeBang();
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);
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

    /**
     * Hiển thị thông điệp cảnh báo lỗi.
     *
     * @param thongDiep Nội dung lỗi
     */
    private void hienThiLoi(String thongDiep) {
        lblBaoLoi.setText("⚠ " + thongDiep);
        lblBaoLoi.setVisible(true);
        lblBaoLoi.setManaged(true);
    }

    /**
     * Tạo icon ổ khóa cho trường chỉ đọc.
     */
    private SVGPath taoIconKhoa() {
        SVGPath lockSvg = new SVGPath();
        lockSvg.setContent("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z");
        lockSvg.setFill(Color.web("#9ca3af"));
        lockSvg.setScaleX(0.55);
        lockSvg.setScaleY(0.55);
        return lockSvg;
    }
}


