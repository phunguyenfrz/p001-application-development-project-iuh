package com.petshop.view.promotion;

import com.petshop.model.Promotion;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Hộp thoại modal PromotionConfirmEditDialog xác nhận các thay đổi
 * trước khi lưu vào CSDL, hiển thị danh sách sự khác biệt giữa
 * dữ liệu cũ và dữ liệu mới (theo đúng mẫu ảnh thiết kế).
 *
 * Tuân thủ quy ước đặt tên:
 * - Tiền tố điều khiển: btn, lbl.
 * - Tên lớp là danh từ (PromotionConfirmEditDialog), tên phương thức là động từ.
 */
public class PromotionConfirmEditDialog extends StackPane {

    private final Promotion promotionCu;
    private final Promotion promotionMoi;
    private final Runnable onXacNhan;
    private final Runnable onQuayLai;

    // Các thành phần điều khiển
    private Label lblTieuDe;
    private Label lblMoTa;

    private Label lblDanhSachThayDoi;

    private Button btnQuayLai;
    private Button btnXacNhan;

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Khởi tạo hộp thoại xác nhận chỉnh sửa.
     *
     * @param promotionCu   Dữ liệu khuyến mãi trước khi chỉnh sửa
     * @param promotionMoi  Dữ liệu khuyến mãi sau khi người dùng chỉnh sửa
     * @param onXacNhan     Callback khi người dùng nhấn "Xác nhận" → thực hiện lưu
     * @param onQuayLai     Callback khi người dùng nhấn "Quay lại" → trở về form chỉnh sửa
     */
    public PromotionConfirmEditDialog(Promotion promotionCu, Promotion promotionMoi,
                                      Runnable onXacNhan, Runnable onQuayLai) {
        this.promotionCu = promotionCu;
        this.promotionMoi = promotionMoi;
        this.onXacNhan = onXacNhan;
        this.onQuayLai = onQuayLai;

        getStyleClass().add("modal-overlay");

        khoiTaoGiaoDien();
        ganSuKien();
    }

    /**
     * Dựng cấu trúc giao diện xác nhận thay đổi theo mẫu ảnh thiết kế.
     */
    private void khoiTaoGiaoDien() {
        VBox pnlCard = new VBox(18);
        pnlCard.getStyleClass().add("modal-card");

        // 1. Tiêu đề hộp thoại
        lblTieuDe = new Label("Xác nhận chỉnh sửa");
        lblTieuDe.getStyleClass().add("modal-header-title");

        // 2. Dòng mô tả ngữ cảnh (tên chương trình và mã định danh)
        String moTaText = String.format(
                "Bạn có chắc chắn muốn cập nhật các thông tin đã thay đổi cho chương trình \"%s\" (%s) không?",
                promotionCu.getTenChuongTrinh(),
                promotionCu.getMaKhuyenMai()
        );
        lblMoTa = new Label(moTaText);
        lblMoTa.getStyleClass().add("modal-header-sub");
        lblMoTa.setWrapText(true);

        // 3. Danh sách các thay đổi được so sánh và hiển thị
        List<String> danhSachThayDoi = laySoSanhThayDoi();

        VBox pnlThayDoiCard = new VBox(8);
        pnlThayDoiCard.getStyleClass().add("summary-card");

        if (danhSachThayDoi.isEmpty()) {
            // Trường hợp không có thay đổi gì
            Label lblKhongCoThayDoi = new Label("Không có thay đổi nào được phát hiện.");
            lblKhongCoThayDoi.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
            pnlThayDoiCard.getChildren().add(lblKhongCoThayDoi);
        } else {
            // Hiển thị từng thay đổi dạng bullet point
            for (String thayDoi : danhSachThayDoi) {
                Label lblDong = new Label("• " + thayDoi);
                lblDong.setStyle("-fx-font-size: 13px; -fx-text-fill: #1f2937;");
                lblDong.setWrapText(true);
                pnlThayDoiCard.getChildren().add(lblDong);
            }
        }

        // 4. Thanh nút bấm: Quay lại (trắng) & Xác nhận (xanh đậm)
        HBox pnlActions = new HBox(12);
        pnlActions.setAlignment(Pos.CENTER_RIGHT);

        btnQuayLai = new Button("Quay lại");
        btnQuayLai.getStyleClass().add("btn-action-secondary");
        btnQuayLai.setPrefWidth(100);

        btnXacNhan = new Button("Xác nhận");
        btnXacNhan.getStyleClass().add("btn-green-search");
        btnXacNhan.setPrefWidth(100);
        // Vô hiệu hóa nút Xác nhận nếu không có thay đổi gì
        btnXacNhan.setDisable(danhSachThayDoi.isEmpty());

        pnlActions.getChildren().addAll(btnQuayLai, btnXacNhan);

        pnlCard.getChildren().addAll(lblTieuDe, lblMoTa, pnlThayDoiCard, pnlActions);
        getChildren().add(pnlCard);
    }

    /**
     * Gắn sự kiện nhấn nút Quay lại và Xác nhận.
     */
    private void ganSuKien() {
        btnQuayLai.setOnAction(e -> quayLaiFormChinhSua());
        btnXacNhan.setOnAction(e -> xacNhanLuuThayDoi());
    }

    /**
     * So sánh dữ liệu cũ và mới, trả về danh sách các thay đổi
     * theo định dạng "Tên thuộc tính: Giá trị cũ → Giá trị mới".
     *
     * @return Danh sách chuỗi mô tả thay đổi
     */
    private List<String> laySoSanhThayDoi() {
        List<String> danhSach = new ArrayList<>();

        // So sánh tên chương trình
        String tenCu = promotionCu.getTenChuongTrinh() != null ? promotionCu.getTenChuongTrinh() : "";
        String tenMoi = promotionMoi.getTenChuongTrinh() != null ? promotionMoi.getTenChuongTrinh() : "";
        if (!tenCu.equals(tenMoi)) {
            danhSach.add("Tên chương trình: " + tenCu + " → " + tenMoi);
        }

        // So sánh phần trăm giảm
        int phanTramCu = (int) promotionCu.getPhanTramGiam();
        int phanTramMoi = (int) promotionMoi.getPhanTramGiam();
        if (phanTramCu != phanTramMoi) {
            danhSach.add("Phần trăm giảm: " + phanTramCu + "% → " + phanTramMoi + "%");
        }

        // So sánh ngày bắt đầu
        String batDauCu = (promotionCu.getNgayBatDau() != null) ? promotionCu.getNgayBatDau().format(DINH_DANG_NGAY) : "";
        String batDauMoi = (promotionMoi.getNgayBatDau() != null) ? promotionMoi.getNgayBatDau().format(DINH_DANG_NGAY) : "";
        if (!batDauCu.equals(batDauMoi)) {
            danhSach.add("Ngày bắt đầu: " + batDauCu + " → " + batDauMoi);
        }

        // So sánh ngày kết thúc
        String ketThucCu = (promotionCu.getNgayKetThuc() != null) ? promotionCu.getNgayKetThuc().format(DINH_DANG_NGAY) : "";
        String ketThucMoi = (promotionMoi.getNgayKetThuc() != null) ? promotionMoi.getNgayKetThuc().format(DINH_DANG_NGAY) : "";
        if (!ketThucCu.equals(ketThucMoi)) {
            danhSach.add("Ngày kết thúc: " + ketThucCu + " → " + ketThucMoi);
        }

        // So sánh trạng thái hoạt động
        String trangThaiCu = promotionCu.layTrangThaiHienThi();
        String trangThaiMoi = promotionMoi.layTrangThaiHienThi();
        if (!trangThaiCu.equals(trangThaiMoi)) {
            danhSach.add("Trạng thái: " + trangThaiCu + " → " + trangThaiMoi);
        }

        return danhSach;
    }

    /**
     * Thực hiện xác nhận và gọi callback lưu dữ liệu vào CSDL.
     */
    private void xacNhanLuuThayDoi() {
        if (onXacNhan != null) {
            onXacNhan.run();
        }
    }

    /**
     * Đóng hộp thoại xác nhận và trở về form chỉnh sửa.
     */
    private void quayLaiFormChinhSua() {
        if (onQuayLai != null) {
            onQuayLai.run();
        }
    }
}


