package com.petshop.view.returnnote;

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
 * Hộp thoại modal ReturnNoteApproveDialog: Xác nhận duyệt phiếu đổi trả (Mẫu 3.2A - Duyệt).
 * Hiển thị tóm tắt thông tin phiếu, số tiền hoàn lại và yêu cầu người dùng xác nhận.
 *
 * Tiêu chuẩn tuân thủ:
 * - Đặt tên điều khiển đúng tiền tố: btn, lbl.
 * - Tên lớp là danh từ (ReturnNoteApproveDialog), tên phương thức là động từ.
 * - Có bình luận 1 dòng và nhiều dòng chuẩn Javadoc.
 */
public class ReturnNoteApproveDialog extends StackPane {

    private final ReturnNote returnNote;
    private final Runnable onXacNhan;
    private final Runnable onDong;

    // Các thành phần giao diện theo đúng tiền tố
    private Label lblTieuDe;
    private Label lblPhuDe;
    private Button btnDong;

    private Label lblTongTienTieuDe;
    private Label lblTongTienGiaTri;

    private Label lblMaPhieuTieuDe;
    private Label lblMaPhieuGiaTri;
    private Label lblHoaDonTieuDe;
    private Label lblHoaDonGiaTri;
    private Label lblKhachHangTieuDe;
    private Label lblKhachHangGiaTri;

    private Label lblGhiChuHuongDan;
    private Button btnHuy;
    private Button btnXacNhan;

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");

    /**
     * Khởi tạo hộp thoại xác nhận duyệt phiếu đổi trả.
     *
     * @param returnNote Phiếu đổi trả cần duyệt
     * @param onXacNhan  Callback khi người dùng bấm "Xác nhận"
     * @param onDong     Callback khi đóng hộp thoại hoặc bấm "Hủy"
     */
    public ReturnNoteApproveDialog(ReturnNote returnNote, Runnable onXacNhan, Runnable onDong) {
        this.returnNote = returnNote;
        this.onXacNhan = onXacNhan;
        this.onDong = onDong;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
    }

    /**
     * Dựng cấu trúc giao diện theo mẫu thiết kế 3.2A Xác nhận duyệt.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(16);
        pnlCard.getStyleClass().add("modal-card");
        pnlCard.setPrefWidth(480);
        pnlCard.setMaxWidth(480);

        // 1. Header (Icon tích xanh + Tiêu đề + nút đóng X)
        BorderPane pnlHeader = new BorderPane();

        HBox pnlHeaderLeft = new HBox(12);
        pnlHeaderLeft.setAlignment(Pos.CENTER_LEFT);

        // Icon tròn xanh lá cây chứa dấu check
        StackPane pnlIconSuccess = new StackPane();
        Circle circleBg = new Circle(18, Color.web("#dcfce7"));
        SVGPath checkSvg = new SVGPath();
        checkSvg.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
        checkSvg.setFill(Color.web("#15803d"));
        checkSvg.setScaleX(0.7);
        checkSvg.setScaleY(0.7);
        pnlIconSuccess.getChildren().addAll(circleBg, checkSvg);

        VBox pnlTitleBox = new VBox(3);
        lblTieuDe = new Label("Xác nhận duyệt phiếu đổi trả");
        lblTieuDe.getStyleClass().add("modal-header-title");

        lblPhuDe = new Label("Vui lòng kiểm tra thông tin trước khi duyệt yêu cầu.");
        lblPhuDe.getStyleClass().add("modal-header-sub");
        pnlTitleBox.getChildren().addAll(lblTieuDe, lblPhuDe);

        pnlHeaderLeft.getChildren().addAll(pnlIconSuccess, pnlTitleBox);

        btnDong = new Button("✕");
        btnDong.getStyleClass().add("modal-close-button");

        pnlHeader.setLeft(pnlHeaderLeft);
        pnlHeader.setRight(btnDong);

        // 2. Thẻ lớn nổi bật hiển thị "Tổng tiền hoàn"
        VBox pnlHighlightCard = new VBox(4);
        pnlHighlightCard.getStyleClass().add("highlight-card-green");

        lblTongTienTieuDe = new Label("Tổng tiền hoàn");
        lblTongTienTieuDe.setStyle("-fx-font-size: 13px; -fx-text-fill: #15803d; -fx-font-weight: 500;");

        lblTongTienGiaTri = new Label(DINH_DANG_TIEN.format(returnNote.getTongTienHoan()));
        lblTongTienGiaTri.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #15803d;");

        pnlHighlightCard.getChildren().addAll(lblTongTienTieuDe, lblTongTienGiaTri);

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
        lblMaPhieuGiaTri.setAlignment(Pos.CENTER_RIGHT);

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

        // 4. Khối lưu ý hướng dẫn
        HBox pnlInfoCallout = new HBox(8);
        pnlInfoCallout.setAlignment(Pos.CENTER_LEFT);
        pnlInfoCallout.getStyleClass().add("callout-info");

        Label lblInfoIcon = new Label("ⓘ");
        lblInfoIcon.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        lblGhiChuHuongDan = new Label("Duyệt phiếu sẽ xác nhận yêu cầu đổi trả và số tiền hoàn đề nghị. Vui lòng thực hiện hoàn tiền theo quy trình của cửa hàng.");
        lblGhiChuHuongDan.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        lblGhiChuHuongDan.setWrapText(true);

        pnlInfoCallout.getChildren().addAll(lblInfoIcon, lblGhiChuHuongDan);

        // 5. Thanh nút bấm: Hủy & Xác nhận
        HBox pnlActions = new HBox(12);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("btn-action-secondary");
        btnHuy.setPrefWidth(90);

        btnXacNhan = new Button("Xác nhận");
        btnXacNhan.getStyleClass().add("btn-green-search");
        btnXacNhan.setPrefWidth(100);

        pnlActions.getChildren().addAll(btnHuy, btnXacNhan);

        // Ráp vào Card
        pnlCard.getChildren().addAll(
                pnlHeader,
                pnlHighlightCard,
                pnlSummaryGrid,
                pnlInfoCallout,
                pnlActions
        );

        getChildren().add(pnlCard);
    }

    /**
     * Gắn sự kiện cho các nút điều khiển.
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


