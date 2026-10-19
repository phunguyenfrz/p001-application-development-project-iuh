package com.petshop.view.shift;

import com.petshop.controller.ShiftAssignmentController;
import com.petshop.model.ShiftAssignment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Hộp thoại modal ShiftAssignmentCancelDialog: Modal 4.2B · Hủy phân công ca trực (Image 5).
 * Hiển thị cảnh báo xác nhận hủy phân ca (xóa mềm/ngừng áp dụng) theo đúng thiết kế
 * và tài liệu phân tích OOAD.
 *
 * Tiêu chuẩn tuân thủ (Review 3_OOP và Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftAssignmentCancelDialog), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (khoiTao..., ganSuKien..., xuLy...), viết theo chuẩn camelCase.
 * - Tiền tố điều khiển: btn, lbl.
 * - Đầy đủ chú thích Javadoc và chú thích một dòng.
 */
public class ShiftAssignmentCancelDialog extends StackPane {

    private final ShiftAssignment assignment;
    private final ShiftAssignmentController controller;
    private final Runnable onHuyThanhCong;
    private final Runnable onDong;

    // Các thành phần điều khiển giao diện theo đúng tiền tố chuẩn
    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDong;

    private Label lblNhanMaPhanCa;
    private Label lblGiaTriMaPhanCa;
    private Label lblNhanTenNhanVien;
    private Label lblGiaTriTenNhanVien;
    private Label lblNhanCaTruc;
    private Label lblGiaTriCaTruc;
    private Label lblNhanNgayLam;
    private Label lblGiaTriNgayLam;

    private Label lblCanhBao;
    private Label lblBaoLoi;

    private Button btnHuy;
    private Button btnXacNhanHuy;

    /**
     * Khởi tạo hộp thoại xác nhận hủy phân công ca trực.
     *
     * @param assignment      Phân ca cần hủy
     * @param controller      Bộ điều khiển phân ca
     * @param onHuyThanhCong  Callback khi hủy thành công
     * @param onDong          Callback khi đóng modal
     */
    public ShiftAssignmentCancelDialog(ShiftAssignment assignment, ShiftAssignmentController controller,
                                       Runnable onHuyThanhCong, Runnable onDong) {
        this.assignment = assignment;
        this.controller = controller;
        this.onHuyThanhCong = onHuyThanhCong;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieu();
    }

    /**
     * Dựng cấu trúc giao diện theo mẫu thiết kế Modal 4.2B Hủy phân công.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(16);
        pnlCard.getStyleClass().add("modal-card");
        pnlCard.setMaxWidth(480);
        pnlCard.setPrefWidth(480);

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
        lblTieuDe = new Label("Hủy phân công ca trực");
        lblTieuDe.getStyleClass().add("modal-header-title");

        lblPhuDe = new Label("Hành động này không thể hoàn tác.");
        lblPhuDe.getStyleClass().add("modal-header-sub");
        pnlTitleBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        Region pnlHeaderSpacer = new Region();
        HBox.setHgrow(pnlHeaderSpacer, Priority.ALWAYS);

        btnDong = new Button("✕");
        btnDong.getStyleClass().add("modal-close-button");

        pnlHeader.getChildren().addAll(pnlTrashIconCircle, pnlTitleBox, pnlHeaderSpacer, btnDong);

        // 2. Thẻ tóm tắt thông tin phân ca (Summary Card)
        VBox pnlSummaryCard = new VBox(10);
        pnlSummaryCard.getStyleClass().add("summary-card");

        GridPane pnlDetailsGrid = new GridPane();
        pnlDetailsGrid.setHgap(20);
        pnlDetailsGrid.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(120);
        pnlDetailsGrid.getColumnConstraints().add(col1);

        lblNhanMaPhanCa = new Label("Mã phân ca");
        lblNhanMaPhanCa.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        lblGiaTriMaPhanCa = new Label();
        lblGiaTriMaPhanCa.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        lblNhanTenNhanVien = new Label("Tên nhân viên");
        lblNhanTenNhanVien.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        lblGiaTriTenNhanVien = new Label();
        lblGiaTriTenNhanVien.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        lblNhanCaTruc = new Label("Ca trực");
        lblNhanCaTruc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        lblGiaTriCaTruc = new Label();
        lblGiaTriCaTruc.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        lblNhanNgayLam = new Label("Ngày làm");
        lblNhanNgayLam.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        lblGiaTriNgayLam = new Label();
        lblGiaTriNgayLam.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        pnlDetailsGrid.add(lblNhanMaPhanCa, 0, 0);
        pnlDetailsGrid.add(lblGiaTriMaPhanCa, 1, 0);

        pnlDetailsGrid.add(lblNhanTenNhanVien, 0, 1);
        pnlDetailsGrid.add(lblGiaTriTenNhanVien, 1, 1);

        pnlDetailsGrid.add(lblNhanCaTruc, 0, 2);
        pnlDetailsGrid.add(lblGiaTriCaTruc, 1, 2);

        pnlDetailsGrid.add(lblNhanNgayLam, 0, 3);
        pnlDetailsGrid.add(lblGiaTriNgayLam, 1, 3);

        pnlSummaryCard.getChildren().add(pnlDetailsGrid);

        // 3. Khối cảnh báo màu đỏ (Danger Callout Box)
        HBox pnlDangerBox = new HBox(8);
        pnlDangerBox.setAlignment(Pos.CENTER_LEFT);
        pnlDangerBox.getStyleClass().add("callout-danger");

        Label lblWarnIcon = new Label("⚠");
        lblWarnIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

        lblCanhBao = new Label("Bạn có chắc chắn muốn hủy phân công này? Ca trực chưa diễn ra nên có thể hủy phân công.");
        lblCanhBao.setStyle("-fx-font-size: 12px; -fx-text-fill: #b91c1c;");
        lblCanhBao.setWrapText(true);

        pnlDangerBox.getChildren().addAll(lblWarnIcon, lblCanhBao);

        // Thông báo lỗi nếu có
        lblBaoLoi = new Label();
        lblBaoLoi.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        // 4. Thanh nút thao tác (Hủy & Xác nhận hủy)
        HBox pnlActions = new HBox(12);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("btn-action-secondary");
        btnHuy.setPrefWidth(80);

        btnXacNhanHuy = new Button("Xác nhận hủy");
        btnXacNhanHuy.getStyleClass().add("btn-danger");

        pnlActions.getChildren().addAll(btnHuy, btnXacNhanHuy);

        pnlCard.getChildren().addAll(pnlHeader, pnlSummaryCard, pnlDangerBox, lblBaoLoi, pnlActions);
        getChildren().add(pnlCard);
    }

    /**
     * Gắn bộ lắng nghe sự kiện tương tác.
     */
    private void ganSuKien() {
        btnDong.setOnAction(e -> dongHopThoai());
        btnHuy.setOnAction(e -> dongHopThoai());

        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                dongHopThoai();
            }
        });

        btnXacNhanHuy.setOnAction(e -> xuLyXacNhanHuy());
    }

    /**
     * Nạp dữ liệu phân ca đã chọn lên tóm tắt.
     */
    private void napDuLieu() {
        if (assignment != null) {
            lblGiaTriMaPhanCa.setText(assignment.getMaPhanCa());
            lblGiaTriTenNhanVien.setText(assignment.getTenNhanVien());
            lblGiaTriCaTruc.setText(assignment.layCaVaGioHienThiMotDong());
            lblGiaTriNgayLam.setText(assignment.layNgayLamHienThi());
        }
    }

    /**
     * Thực hiện hủy phân ca (xóa mềm) thông qua ShiftAssignmentController.
     */
    private void xuLyXacNhanHuy() {
        if (assignment == null) return;

        try {
            boolean thanhCong = controller.huyPhanCa(assignment.getMaPhanCa());
            if (thanhCong) {
                if (onHuyThanhCong != null) {
                    onHuyThanhCong.run();
                }
                dongHopThoai();
            } else {
                hienThiLoi("Không thể hủy phân ca do lỗi CSDL.");
            }
        } catch (Exception ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    /**
     * Hiển thị thông báo lỗi lên modal.
     */
    private void hienThiLoi(String thongDiep) {
        lblBaoLoi.setText("⚠ " + thongDiep);
        lblBaoLoi.setVisible(true);
        lblBaoLoi.setManaged(true);
    }

    /**
     * Đóng hộp thoại.
     */
    private void dongHopThoai() {
        if (onDong != null) {
            onDong.run();
        }
    }
}


