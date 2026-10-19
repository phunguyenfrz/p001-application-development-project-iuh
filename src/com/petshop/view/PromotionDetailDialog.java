package com.petshop.view;

import com.petshop.model.Promotion;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại modal PromotionDetailDialog hiển thị thông tin chi tiết một chương trình khuyến mãi.
 * Được kích hoạt khi nhấn vào liên kết "Chi tiết" trong màn hình Tìm kiếm khuyến mãi.
 *
 * Tuân thủ quy ước đặt tên:
 * - Tiền tố điều khiển: btn, lbl.
 * - Tên lớp là danh từ (PromotionDetailDialog), tên phương thức là động từ.
 */
public class PromotionDetailDialog extends StackPane {

    private final Promotion promotion;
    private final Runnable onDong;

    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDongHeader;
    private Button btnDongFooter;

    private Label lblMaKhuyenMai;
    private Label lblGiaTriMa;

    private Label lblTenChuongTrinh;
    private Label lblGiaTriTen;

    private Label lblMucGiam;
    private Label lblGiaTriMucGiam;

    private Label lblThoiGianApDung;
    private Label lblGiaTriThoiGian;

    private Label lblTrangThai;
    private Label lblBadgeTrangThai;

    private Label lblNgayCapNhat;
    private Label lblGiaTriNgayCapNhat;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo hộp thoại xem chi tiết khuyến mãi.
     *
     * @param promotion Khuyến mãi cần xem chi tiết
     * @param onDong    Hàm callback khi đóng hộp thoại
     */
    public PromotionDetailDialog(Promotion promotion, Runnable onDong) {
        this.promotion = promotion;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieu();
    }

    /**
     * Dựng cấu trúc giao diện hộp thoại chi tiết.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(16);
        pnlCard.getStyleClass().add("modal-card");

        // 1. Header
        BorderPane pnlHeader = new BorderPane();

        VBox pnlTitleBox = new VBox(3);
        lblTieuDe = new Label("Chi tiết khuyến mãi");
        lblTieuDe.getStyleClass().add("modal-header-title");

        lblPhuDe = new Label("Xem toàn bộ thông tin chi tiết của chương trình ưu đãi.");
        lblPhuDe.getStyleClass().add("modal-header-sub");
        pnlTitleBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        btnDongHeader = new Button("✕");
        btnDongHeader.getStyleClass().add("modal-close-button");

        pnlHeader.setLeft(pnlTitleBox);
        pnlHeader.setRight(btnDongHeader);

        // 2. Nội dung chi tiết trong Card
        VBox pnlContentCard = new VBox(12);
        pnlContentCard.getStyleClass().add("summary-card");

        GridPane pnlGrid = new GridPane();
        pnlGrid.setHgap(16);
        pnlGrid.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(140);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        pnlGrid.getColumnConstraints().addAll(col1, col2);

        lblMaKhuyenMai = taoNhanTieuDe("Mã định danh:");
        lblGiaTriMa = taoNhanGiaTri("");

        lblTenChuongTrinh = taoNhanTieuDe("Tên chương trình:");
        lblGiaTriTen = taoNhanGiaTri("");
        lblGiaTriTen.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");

        lblMucGiam = taoNhanTieuDe("Phần trăm giảm:");
        lblGiaTriMucGiam = taoNhanGiaTri("");

        lblThoiGianApDung = taoNhanTieuDe("Thời gian áp dụng:");
        lblGiaTriThoiGian = taoNhanGiaTri("");

        lblTrangThai = taoNhanTieuDe("Trạng thái:");
        lblBadgeTrangThai = new Label();

        lblNgayCapNhat = taoNhanTieuDe("Ngày cập nhật:");
        lblGiaTriNgayCapNhat = taoNhanGiaTri("");

        pnlGrid.add(lblMaKhuyenMai, 0, 0);
        pnlGrid.add(lblGiaTriMa, 1, 0);

        pnlGrid.add(lblTenChuongTrinh, 0, 1);
        pnlGrid.add(lblGiaTriTen, 1, 1);

        pnlGrid.add(lblMucGiam, 0, 2);
        pnlGrid.add(lblGiaTriMucGiam, 1, 2);

        pnlGrid.add(lblThoiGianApDung, 0, 3);
        pnlGrid.add(lblGiaTriThoiGian, 1, 3);

        pnlGrid.add(lblTrangThai, 0, 4);
        pnlGrid.add(lblBadgeTrangThai, 1, 4);

        pnlGrid.add(lblNgayCapNhat, 0, 5);
        pnlGrid.add(lblGiaTriNgayCapNhat, 1, 5);

        pnlContentCard.getChildren().add(pnlGrid);

        // 3. Footer
        HBox pnlFooter = new HBox();
        pnlFooter.setAlignment(Pos.CENTER_RIGHT);

        btnDongFooter = new Button("Đóng");
        btnDongFooter.getStyleClass().add("btn-action-secondary");
        btnDongFooter.setPrefWidth(90);

        pnlFooter.getChildren().add(btnDongFooter);

        pnlCard.getChildren().addAll(pnlHeader, pnlContentCard, pnlFooter);
        getChildren().add(pnlCard);
    }

    /**
     * Gắn sự kiện đóng cho các nút bấm.
     */
    private void ganSuKien() {
        btnDongHeader.setOnAction(e -> dongHopThoai());
        btnDongFooter.setOnAction(e -> dongHopThoai());

        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                dongHopThoai();
            }
        });
    }

    /**
     * Nạp dữ liệu khuyến mãi vào các Label tương ứng.
     */
    private void napDuLieu() {
        if (promotion != null) {
            lblGiaTriMa.setText(promotion.getMaKhuyenMai());
            lblGiaTriTen.setText(promotion.getTenChuongTrinh());
            lblGiaTriMucGiam.setText((int) promotion.getPhanTramGiam() + "%");

            String batDau = (promotion.getNgayBatDau() != null) ? promotion.getNgayBatDau().format(DINH_DANG_NGAY) : "N/A";
            String ketThuc = (promotion.getNgayKetThuc() != null) ? promotion.getNgayKetThuc().format(DINH_DANG_NGAY) : "N/A";
            lblGiaTriThoiGian.setText(batDau + " – " + ketThuc);

            String statusStr = promotion.layTrangThaiHienThi();
            lblBadgeTrangThai.setText(statusStr);
            switch (statusStr) {
                case "Đang áp dụng" -> lblBadgeTrangThai.getStyleClass().setAll("status-badge-active");
                case "Sắp áp dụng" -> lblBadgeTrangThai.getStyleClass().setAll("status-badge-upcoming");
                default -> lblBadgeTrangThai.getStyleClass().setAll("status-badge-inactive");
            }

            String capNhat = (promotion.getNgayCapNhat() != null) ? promotion.getNgayCapNhat().format(DINH_DANG_NGAY) : batDau;
            lblGiaTriNgayCapNhat.setText(capNhat);
        }
    }

    private void dongHopThoai() {
        if (onDong != null) {
            onDong.run();
        }
    }

    private Label taoNhanTieuDe(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280;");
        return lbl;
    }

    private Label taoNhanGiaTri(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f2937;");
        return lbl;
    }
}

