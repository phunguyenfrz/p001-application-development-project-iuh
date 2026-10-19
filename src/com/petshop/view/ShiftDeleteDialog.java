package com.petshop.view;

import com.petshop.controller.ShiftController;
import com.petshop.model.Shift;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Hộp thoại modal ShiftDeleteDialog: Modal 1.1B · Xóa ca trực.
 * Hiển thị cảnh báo xác nhận ngừng hoạt động (xóa mềm) ca trực theo đúng thiết kế
 * và tài liệu OOAD.
 *
 * Tiêu chuẩn tuân thủ (Review 3_OOP & Review 4_Coding Convention):
 * - Tên lớp là danh từ (ShiftDeleteDialog), viết theo chuẩn PascalCase.
 * - Tên phương thức là động từ (khoiTao..., ganSuKien..., xuLy...), viết theo chuẩn camelCase.
 * - Tiền tố điều khiển: btn, lbl.
 * - Đầy đủ chú thích Javadoc và chú thích một dòng.
 */
public class ShiftDeleteDialog extends StackPane {

    private final Shift shift;
    private final ShiftController shiftController;
    private final Runnable onXoaThanhCong;
    private final Runnable onDong;

    // Các thành phần điều khiển giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDong;

    private Label lblGiaTriMaCa;
    private Label lblTenCa;
    private Label lblNhanGioBatDau;
    private Label lblGiaTriGioBatDau;
    private Label lblNhanGioKetThuc;
    private Label lblGiaTriGioKetThuc;

    private Label lblCanhBao;
    private Label lblBaoLoi;

    private Button btnHuy;
    private Button btnXacNhanXoa;

    /**
     * Khởi tạo hộp thoại xác nhận xóa ca trực.
     *
     * @param shift           Đối tượng ca trực cần xóa mềm
     * @param shiftController Bộ điều khiển ca trực
     * @param onXoaThanhCong  Hàm callback thực thi khi xóa thành công
     * @param onDong          Hàm callback thực thi khi đóng modal
     */
    public ShiftDeleteDialog(Shift shift, ShiftController shiftController,
                             Runnable onXoaThanhCong, Runnable onDong) {
        this.shift = shift;
        this.shiftController = shiftController;
        this.onXoaThanhCong = onXoaThanhCong;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
        napDuLieu();
    }

    /**
     * Dựng cấu trúc giao diện theo mẫu thiết kế Modal 1.1B Xóa ca trực.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(16);
        pnlCard.getStyleClass().add("modal-card");
        pnlCard.setMaxWidth(480);

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
        lblTieuDe = new Label("Xóa ca trực");
        lblTieuDe.getStyleClass().add("modal-header-title");

        lblPhuDe = new Label("Hành động này không thể hoàn tác.");
        lblPhuDe.getStyleClass().add("modal-header-sub");
        pnlTitleBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        Region pnlHeaderSpacer = new Region();
        HBox.setHgrow(pnlHeaderSpacer, Priority.ALWAYS);

        btnDong = new Button("✕");
        btnDong.getStyleClass().add("modal-close-button");

        pnlHeader.getChildren().addAll(pnlTrashIconCircle, pnlTitleBox, pnlHeaderSpacer, btnDong);

        // 2. Thẻ tóm tắt thông tin ca trực (Summary Card)
        VBox pnlSummaryCard = new VBox(10);
        pnlSummaryCard.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 8px; -fx-padding: 14px; -fx-border-color: #e5e7eb; -fx-border-radius: 8px;");

        // Phần trên của summary card: Mã ca & Tên ca
        VBox pnlCodeNameBox = new VBox(4);
        lblGiaTriMaCa = new Label();
        lblGiaTriMaCa.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #6b7280;");

        lblTenCa = new Label();
        lblTenCa.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        pnlCodeNameBox.getChildren().addAll(lblGiaTriMaCa, lblTenCa);

        // Phần dưới của summary card: Khối giờ bắt đầu / giờ kết thúc
        VBox pnlTimeBox = new VBox(8);
        pnlTimeBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 6px; -fx-padding: 10px 14px; -fx-border-color: #e5e7eb; -fx-border-radius: 6px;");

        // Dòng 1: Giờ bắt đầu
        HBox pnlStartTimeRow = new HBox();
        pnlStartTimeRow.setAlignment(Pos.CENTER_LEFT);
        lblNhanGioBatDau = new Label("Giờ bắt đầu");
        lblNhanGioBatDau.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Region pnlStartSpacer = new Region();
        HBox.setHgrow(pnlStartSpacer, Priority.ALWAYS);
        lblGiaTriGioBatDau = new Label();
        lblGiaTriGioBatDau.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #111827;");
        pnlStartTimeRow.getChildren().addAll(lblNhanGioBatDau, pnlStartSpacer, lblGiaTriGioBatDau);

        // Dòng 2: Giờ kết thúc
        HBox pnlEndTimeRow = new HBox();
        pnlEndTimeRow.setAlignment(Pos.CENTER_LEFT);
        lblNhanGioKetThuc = new Label("Giờ kết thúc");
        lblNhanGioKetThuc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Region pnlEndSpacer = new Region();
        HBox.setHgrow(pnlEndSpacer, Priority.ALWAYS);
        lblGiaTriGioKetThuc = new Label();
        lblGiaTriGioKetThuc.setStyle("-fx-font-size: 12.5px; -fx-font-weight: 600; -fx-text-fill: #111827;");
        pnlEndTimeRow.getChildren().addAll(lblNhanGioKetThuc, pnlEndSpacer, lblGiaTriGioKetThuc);

        pnlTimeBox.getChildren().addAll(pnlStartTimeRow, pnlEndTimeRow);
        pnlSummaryCard.getChildren().addAll(pnlCodeNameBox, pnlTimeBox);

        // 3. Khối cảnh báo màu đỏ (Danger Callout Box)
        HBox pnlDangerBox = new HBox(8);
        pnlDangerBox.setAlignment(Pos.CENTER_LEFT);
        pnlDangerBox.getStyleClass().add("callout-danger");

        Label lblWarnIcon = new Label("⚠");
        lblWarnIcon.setStyle("-fx-font-size: 14px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

        lblCanhBao = new Label("Bạn có chắc chắn muốn xóa ca trực này? Thông tin ca trực sẽ bị xóa khỏi danh sách.");
        lblCanhBao.setStyle("-fx-font-size: 12px; -fx-text-fill: #b91c1c;");
        lblCanhBao.setWrapText(true);

        pnlDangerBox.getChildren().addAll(lblWarnIcon, lblCanhBao);

        // Thông báo lỗi nếu xảy ra sự cố
        lblBaoLoi = new Label();
        lblBaoLoi.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px; -fx-font-weight: 600;");
        lblBaoLoi.setVisible(false);
        lblBaoLoi.setManaged(false);

        // 4. Thanh nút bấm thao tác (Hủy & Xác nhận xóa)
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
     * Gắn bộ lắng nghe sự kiện thao tác.
     */
    private void ganSuKien() {
        btnDong.setOnAction(e -> dongHopThoai());
        btnHuy.setOnAction(e -> dongHopThoai());

        // Bấm ra vùng backdrop xám bên ngoài để đóng modal
        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                dongHopThoai();
            }
        });

        // Xử lý xác nhận ngừng hoạt động (xóa mềm)
        btnXacNhanXoa.setOnAction(e -> xuLyXacNhanXoa());
    }

    /**
     * Nạp dữ liệu ca trực lên các thành phần giao diện.
     */
    private void napDuLieu() {
        if (shift != null) {
            lblGiaTriMaCa.setText(shift.getMaCa());
            lblTenCa.setText(shift.getTenCa());
            lblGiaTriGioBatDau.setText(shift.layGioBatDauHienThi());
            lblGiaTriGioKetThuc.setText(shift.layGioKetThucHienThi());
        }
    }

    /**
     * Thực hiện ngừng hoạt động (xóa mềm) ca trực thông qua ShiftController.
     */
    private void xuLyXacNhanXoa() {
        if (shift == null) {
            return;
        }

        try {
            boolean thanhCong = shiftController.xoaCaTruc(shift.getMaCa());
            if (thanhCong) {
                if (onXoaThanhCong != null) {
                    onXoaThanhCong.run();
                }
                dongHopThoai();
            } else {
                hienThiLoi("Không thể xóa ca trực do lỗi CSDL.");
            }
        } catch (Exception e) {
            hienThiLoi(e.getMessage());
        }
    }

    /**
     * Hiển thị thông báo lỗi lên giao diện modal.
     *
     * @param thongDiep Nội dung lỗi
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

