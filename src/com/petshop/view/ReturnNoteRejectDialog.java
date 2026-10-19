package com.petshop.view;

import com.petshop.model.ReturnNote;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.text.DecimalFormat;

/**
 * Hộp thoại modal ReturnNoteRejectDialog: Xác nhận từ chối phiếu đổi trả (Mẫu 3.2A - Từ chối).
 * Cảnh báo người dùng trước khi chuyển trạng thái phiếu sang "Đã từ chối".
 *
 * Tiêu chuẩn tuân thủ:
 * - Đặt tên điều khiển đúng tiền tố: btn, lbl.
 * - Tên lớp là danh từ (ReturnNoteRejectDialog), tên phương thức là động từ.
 * - Thao tác từ chối tương đương hủy/ngừng hoạt động (khớp chuẩn OOAD).
 * - Có bình luận 1 dòng và nhiều dòng chuẩn Javadoc.
 */
public class ReturnNoteRejectDialog extends StackPane {

    private final ReturnNote returnNote;
    private final Runnable onXacNhan;
    private final Runnable onDong;

    // Các thành phần giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDong;

    private Label lblTongTienDeNghiTieuDe;
    private Label lblTongTienDeNghiGiaTri;

    private Label lblMaPhieuTieuDe;
    private Label lblMaPhieuGiaTri;
    private Label lblHoaDonTieuDe;
    private Label lblHoaDonGiaTri;
    private Label lblKhachHangTieuDe;
    private Label lblKhachHangGiaTri;

    private Label lblCanhBaoTuChoi;
    private Button btnHuy;
    private Button btnXacNhan;

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    /**
     * Khởi tạo hộp thoại xác nhận từ chối phiếu đổi trả.
     *
     * @param returnNote Phiếu đổi trả cần từ chối
     * @param onXacNhan  Callback khi người dùng bấm "Xác nhận"
     * @param onDong     Callback khi đóng hộp thoại hoặc bấm "Hủy"
     */
    public ReturnNoteRejectDialog(ReturnNote returnNote, Runnable onXacNhan, Runnable onDong) {
        this.returnNote = returnNote;
        this.onXacNhan = onXacNhan;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
    }

    /**
     * Dựng cấu trúc giao diện theo mẫu thiết kế 3.2A Xác nhận từ chối.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(16);
        pnlCard.getStyleClass().add("modal-card");
        pnlCard.setPrefWidth(480);
        pnlCard.setMaxWidth(480);

        // 1. Header (Icon tròn đỏ X + Tiêu đề + nút đóng)
        BorderPane pnlHeader = new BorderPane();

        HBox pnlHeaderLeft = new HBox(12);
        pnlHeaderLeft.setAlignment(Pos.CENTER_LEFT);

        // Icon tròn đỏ chứa biểu tượng dấu X
        StackPane pnlIconDanger = new StackPane();
        Circle circleBg = new Circle(18, Color.web("#fee2e2"));
        SVGPath xSvg = new SVGPath();
        xSvg.setContent("M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        xSvg.setFill(Color.web("#dc2626"));
        xSvg.setScaleX(0.65);
        xSvg.setScaleY(0.65);
        pnlIconDanger.getChildren().addAll(circleBg, xSvg);

        VBox pnlTitleBox = new VBox(3);
        lblTieuDe = new Label("Xác nhận từ chối");
        lblTieuDe.getStyleClass().add("modal-header-title");

        lblPhuDe = new Label("Bạn có chắc chắn muốn từ chối phiếu đổi trả này?");
        lblPhuDe.getStyleClass().add("modal-header-sub");
        pnlTitleBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        pnlHeaderLeft.getChildren().addAll(pnlIconDanger, pnlTitleBox);

        btnDong = new Button("✕");
        btnDong.getStyleClass().add("modal-close-button");

        pnlHeader.setLeft(pnlHeaderLeft);
        pnlHeader.setRight(btnDong);

        // 2. Thẻ lớn hiển thị "Tổng tiền hoàn đề nghị"
        VBox pnlHighlightCard = new VBox(4);
        pnlHighlightCard.getStyleClass().add("highlight-card-red");

        lblTongTienDeNghiTieuDe = new Label("Tổng tiền hoàn đề nghị");
        lblTongTienDeNghiTieuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #991b1b; -fx-font-weight: 500;");

        lblTongTienDeNghiGiaTri = new Label(DINH_DANG_TIEN.format(returnNote.getTongTienHoan()));
        lblTongTienDeNghiGiaTri.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1f2923;");

        pnlHighlightCard.getChildren().addAll(lblTongTienDeNghiTieuDe, lblTongTienDeNghiGiaTri);

        // 3. Khung tóm tắt chi tiết (Mã phiếu, Hóa đơn, Khách hàng)
        GridPane pnlSummaryGrid = new GridPane();
        pnlSummaryGrid.getStyleClass().add("summary-card");
        pnlSummaryGrid.setHgap(16);
        pnlSummaryGrid.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);
        pnlSummaryGrid.getColumnConstraints().addAll(col1, col2);

        lblMaPhieuTieuDe = new Label("Mã phiếu đổi trả");
        lblMaPhieuTieuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        lblMaPhieuGiaTri = new Label(returnNote.getMaPhieuDoiTra());
        lblMaPhieuGiaTri.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        lblHoaDonTieuDe = new Label("Hóa đơn");
        lblHoaDonTieuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        lblHoaDonGiaTri = new Label(returnNote.getMaHoaDon());
        lblHoaDonGiaTri.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        lblKhachHangTieuDe = new Label("Khách hàng");
        lblKhachHangTieuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");
        lblKhachHangGiaTri = new Label(returnNote.getTenKhachHang() != null ? returnNote.getTenKhachHang() : "Khách vãng lai");
        lblKhachHangGiaTri.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #111827;");

        pnlSummaryGrid.add(lblMaPhieuTieuDe, 0, 0);
        pnlSummaryGrid.add(lblMaPhieuGiaTri, 1, 0);
        pnlSummaryGrid.add(lblHoaDonTieuDe, 0, 1);
        pnlSummaryGrid.add(lblHoaDonGiaTri, 1, 1);
        pnlSummaryGrid.add(lblKhachHangTieuDe, 0, 2);
        pnlSummaryGrid.add(lblKhachHangGiaTri, 1, 2);

        // 4. Khối cảnh báo đỏ
        HBox pnlDangerCallout = new HBox(8);
        pnlDangerCallout.setAlignment(Pos.CENTER_LEFT);
        pnlDangerCallout.getStyleClass().add("callout-danger");

        Label lblDangerIcon = new Label("⚠");
        lblDangerIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

        String canhBaoText = String.format(
                "Phiếu sẽ chuyển sang trạng thái Đã từ chối. Số tiền đề nghị %s sẽ không được hoàn theo phiếu này.",
                DINH_DANG_TIEN.format(returnNote.getTongTienHoan())
        );
        lblCanhBaoTuChoi = new Label(canhBaoText);
        lblCanhBaoTuChoi.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626;");
        lblCanhBaoTuChoi.setWrapText(true);

        pnlDangerCallout.getChildren().addAll(lblDangerIcon, lblCanhBaoTuChoi);

        // 5. Thanh nút bấm: Hủy & Xác nhận (đỏ)
        HBox pnlActions = new HBox(12);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("btn-action-secondary");
        btnHuy.setPrefWidth(90);

        btnXacNhan = new Button("Xác nhận");
        btnXacNhan.getStyleClass().add("btn-danger");
        btnXacNhan.setPrefWidth(100);

        pnlActions.getChildren().addAll(btnHuy, btnXacNhan);

        // Ráp vào Card
        pnlCard.getChildren().addAll(
                pnlHeader,
                pnlHighlightCard,
                pnlSummaryGrid,
                pnlDangerCallout,
                pnlActions
        );

        getChildren().add(pnlCard);
    }

    /**
     * Gắn các bộ lắng nghe sự kiện.
     */
    private void ganSuKien() {
        btnDong.setOnAction(e -> dongHopThoai());
        btnHuy.setOnAction(e -> dongHopThoai());

        setOnMouseClicked(e -> {
            if (e.getTarget() == this) {
                dongHopThoai();
            }
        });

        btnXacNhan.setOnAction(e -> {
            if (onXacNhan != null) {
                onXacNhan.run();
            }
            dongHopThoai();
        });
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

