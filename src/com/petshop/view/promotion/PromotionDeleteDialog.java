package com.petshop.view.promotion;

import com.petshop.controller.PromotionController;
import com.petshop.model.Promotion;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại modal PromotionDeleteDialog hiển thị cảnh báo xác nhận xóa mềm
 * (ngừng hoạt động) chương trình khuyến mãi theo đúng thiết kế 2.1B và tài liệu OOAD.
 *
 * Tuân thủ quy ước đặt tên:
 * - Tiền tố điều khiển: btn, lbl.
 * - Tên lớp là danh từ (PromotionDeleteDialog), tên phương thức là động từ.
 */
public class PromotionDeleteDialog extends StackPane {

    private final Promotion promotion;
    private final PromotionController promotionController;
    private final Runnable onXoaThanhCong;
    private final Runnable onDong;

    // Các thành phần điều khiển
    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDong;

    private Label lblTieuDeChuongTrinh;
    private Label lblTenChuongTrinh;
    private Label lblNhanMa;
    private Label lblGiaTriMa;
    private Label lblNhanMucGiam;
    private Label lblGiaTriMucGiam;
    private Label lblNhanThoiGian;
    private Label lblGiaTriThoiGian;

    private Label lblCanhBao;
    private Label lblBaoLoi;

    private Button btnHuy;
    private Button btnXacNhanXoa;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo hộp thoại xác nhận xóa khuyến mãi.
     *
     * @param promotion           Chương trình khuyến mãi cần xóa mềm
     * @param promotionController Bộ điều khiển khuyến mãi
     * @param onXoaThanhCong      Hàm callback khi xóa thành công
     * @param onDong              Hàm callback khi đóng hộp thoại
     */
    public PromotionDeleteDialog(Promotion promotion, PromotionController promotionController,
                                 Runnable onXoaThanhCong, Runnable onDong) {
        this.promotion = promotion;
        this.promotionController = promotionController;
        this.onXoaThanhCong = onXoaThanhCong;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieu();
    }

    /**
     * Xây dựng cấu trúc giao diện theo mẫu thiết kế 2.1B.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(16);
        pnlCard.getStyleClass().add("modal-card");

        // 1. Header (Icon thùng rác đỏ + Tiêu đề + Phụ đề + Nút đóng X)
        HBox pnlHeader = new HBox(14);
        pnlHeader.setAlignment(Pos.CENTER_LEFT);

        StackPane pnlTrashIconCircle = new StackPane();
        pnlTrashIconCircle.getStyleClass().add("icon-circle-danger");

        SVGPath trashSvg = new SVGPath();
        trashSvg.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        trashSvg.setFill(Color.web("#dc2626"));
        trashSvg.setScaleX(0.7);
        trashSvg.setScaleY(0.7);
        pnlTrashIconCircle.getChildren().add(trashSvg);

        VBox pnlTitleBox = new VBox(2);
        lblTieuDe = new Label("Ngừng/Xóa khuyến mãi");
        lblTieuDe.getStyleClass().add("modal-header-title");

        lblPhuDe = new Label("Hành động này không thể hoàn tác.");
        lblPhuDe.getStyleClass().add("modal-header-sub");
        pnlTitleBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        Region pnlHeaderSpacer = new Region();
        HBox.setHgrow(pnlHeaderSpacer, Priority.ALWAYS);

        btnDong = new Button("✕");
        btnDong.getStyleClass().add("modal-close-button");

        pnlHeader.getChildren().addAll(pnlTrashIconCircle, pnlTitleBox, pnlHeaderSpacer, btnDong);

        // 2. Thẻ tóm tắt thông tin chương trình khuyến mãi (Summary Card)
        VBox pnlSummaryCard = new VBox(10);
        pnlSummaryCard.getStyleClass().add("summary-card");

        lblTieuDeChuongTrinh = new Label("Chương trình khuyến mãi");
        lblTieuDeChuongTrinh.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6b7280;");

        lblTenChuongTrinh = new Label();
        lblTenChuongTrinh.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        // Bảng chi tiết 3 dòng
        GridPane pnlDetailsGrid = new GridPane();
        pnlDetailsGrid.setHgap(20);
        pnlDetailsGrid.setVgap(6);

        lblNhanMa = new Label("Mã khuyến mãi");
        lblNhanMa.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        lblGiaTriMa = new Label();
        lblGiaTriMa.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        lblNhanMucGiam = new Label("Mức giảm");
        lblNhanMucGiam.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        lblGiaTriMucGiam = new Label();
        lblGiaTriMucGiam.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        lblNhanThoiGian = new Label("Thời gian");
        lblNhanThoiGian.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        lblGiaTriThoiGian = new Label();
        lblGiaTriThoiGian.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        pnlDetailsGrid.add(lblNhanMa, 0, 0);
        pnlDetailsGrid.add(lblGiaTriMa, 1, 0);
        pnlDetailsGrid.add(lblNhanMucGiam, 0, 1);
        pnlDetailsGrid.add(lblGiaTriMucGiam, 1, 1);
        pnlDetailsGrid.add(lblNhanThoiGian, 0, 2);
        pnlDetailsGrid.add(lblGiaTriThoiGian, 1, 2);

        ColumnConstraints colLabel = new ColumnConstraints();
        colLabel.setPrefWidth(120);
        pnlDetailsGrid.getColumnConstraints().add(colLabel);

        pnlSummaryCard.getChildren().addAll(lblTieuDeChuongTrinh, lblTenChuongTrinh, pnlDetailsGrid);

        // 3. Khối cảnh báo màu đỏ (Danger Callout Box)
        HBox pnlDangerBox = new HBox(8);
        pnlDangerBox.setAlignment(Pos.CENTER_LEFT);
        pnlDangerBox.getStyleClass().add("callout-danger");

        Label lblWarnIcon = new Label("⚠");
        lblWarnIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

        lblCanhBao = new Label("Chương trình sẽ bị xóa và không còn được áp dụng cho hóa đơn mới. Vui lòng kiểm tra trước khi xác nhận.");
        lblCanhBao.setStyle("-fx-font-size: 12px; -fx-text-fill: #b91c1c;");
        lblCanhBao.setWrapText(true);

        pnlDangerBox.getChildren().addAll(lblWarnIcon, lblCanhBao);

        // Thông báo lỗi nếu xảy ra ngoại lệ khi xóa
        lblBaoLoi = new Label();
        lblBaoLoi.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        // 4. Thanh nút bấm xác nhận
        HBox pnlActions = new HBox(12);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("btn-action-secondary");
        btnHuy.setPrefWidth(80);

        btnXacNhanXoa = new Button("🗑  Xác nhận xóa");
        btnXacNhanXoa.getStyleClass().add("btn-danger");

        pnlActions.getChildren().addAll(btnHuy, btnXacNhanXoa);

        pnlCard.getChildren().addAll(pnlHeader, pnlSummaryCard, pnlDangerBox, lblBaoLoi, pnlActions);
        getChildren().add(pnlCard);
    }

    /**
     * Gắn bộ lắng nghe sự kiện đóng hộp thoại và xác nhận xóa.
     */
    private void ganSuKien() {
        btnDong.setOnAction(e -> dongHopThoai());
        btnHuy.setOnAction(e -> dongHopThoai());

        // Bấm ra nền đen để đóng
        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                dongHopThoai();
            }
        });

        // Xử lý xác nhận xóa mềm
        btnXacNhanXoa.setOnAction(e -> xuLyXacNhanXoa());
    }

    /**
     * Nạp dữ liệu khuyến mãi đã chọn lên thẻ tóm tắt.
     */
    private void napDuLieu() {
        if (promotion != null) {
            lblTenChuongTrinh.setText(promotion.getTenChuongTrinh());
            lblGiaTriMa.setText(promotion.getMaKhuyenMai());
            lblGiaTriMucGiam.setText((int) promotion.getPhanTramGiam() + "%");

            String batDauStr = (promotion.getNgayBatDau() != null) ? promotion.getNgayBatDau().format(DINH_DANG_NGAY) : "";
            String ketThucStr = (promotion.getNgayKetThuc() != null) ? promotion.getNgayKetThuc().format(DINH_DANG_NGAY) : "";
            lblGiaTriThoiGian.setText(batDauStr + "–" + ketThucStr);
        }
    }

    /**
     * Thực hiện ngừng hoạt động (xóa mềm) qua Controller.
     */
    private void xuLyXacNhanXoa() {
        if (promotion == null) {
            return;
        }

        try {
            boolean thanhCong = promotionController.xoaKhuyenMai(promotion.getMaKhuyenMai());
            if (thanhCong) {
                if (onXoaThanhCong != null) {
                    onXoaThanhCong.run();
                }
                dongHopThoai();
            } else {
                hienThiLoi("Không thể xóa khuyến mãi do lỗi CSDL.");
            }
        } catch (Exception e) {
            hienThiLoi(e.getMessage());
        }
    }

    /**
     * Hiển thị thông báo lỗi lên giao diện.
     *
     * @param thongDiep Lời nhắn lỗi
     */
    private void hienThiLoi(String thongDiep) {
        lblBaoLoi.setText("⚠ " + thongDiep);
        lblBaoLoi.setVisible(true);
        lblBaoLoi.setManaged(true);
    }

    /**
     * Đóng hộp thoại xác nhận.
     */
    private void dongHopThoai() {
        if (onDong != null) {
            onDong.run();
        }
    }
}


