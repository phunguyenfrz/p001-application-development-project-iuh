package com.petshop.view.phieudichvu;

import com.petshop.controller.PhieuDichVuController;
import com.petshop.model.PhieuDichVu;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Giao diện Tìm kiếm & Tra cứu phiếu tiếp nhận dịch vụ (PDV_TIM_KIEM).
 * Hỗ trợ tra cứu đa tiêu chí: mã phiếu/tên/SĐT khách hàng, khoảng ngày và trạng thái.
 */
public class TimKiemPhieuDichVuView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final PhieuDichVuController controller = new PhieuDichVuController();
    private final ObservableList<PhieuDichVu> danhSachHienThi = FXCollections.observableArrayList();

    // Điều khiển bộ lọc
    private final TextField txtTuKhoa = new TextField();
    private final DatePicker dpTuNgay = new DatePicker();
    private final DatePicker dpDenNgay = new DatePicker();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới bộ lọc");

    // Thanh tác vụ và nhãn số lượng
    private final Label lblTongSo = new Label("Tổng số: 0 phiếu dịch vụ");

    // Bảng kết quả
    private final TableView<PhieuDichVu> tblDanhSach = new TableView<>();

    public TimKiemPhieuDichVuView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("PHIẾU DỊCH VỤ - TÌM KIẾM & TRA CỨU");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu phiếu tiếp nhận dịch vụ chăm sóc theo khách hàng, mã phiếu, khoảng ngày và trạng thái.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Bộ lọc & Bảng dữ liệu
        VBox pnlBoLoc = taoKhungBoLoc();
        VBox pnlBang = taoKhungBangKetQua();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, pnlBoLoc, pnlBang);

        xuLyTimKiem();
    }

    // Tạo khung điều khiển bộ lọc tìm kiếm
    private VBox taoKhungBoLoc() {
        VBox khung = new VBox(10);
        khung.setStyle("-fx-background-color: #ffffff; -fx-padding: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);
        luoi.setAlignment(Pos.CENTER_LEFT);

        txtTuKhoa.setPromptText("Mã phiếu, tên khách, SĐT, mã khách...");
        txtTuKhoa.setPrefWidth(260);
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());

        dpTuNgay.setPromptText("Từ ngày");
        dpTuNgay.setPrefWidth(140);
        dpDenNgay.setPromptText("Đến ngày");
        dpDenNgay.setPrefWidth(140);

        cboTrangThai.getItems().addAll("Tất cả", "Đang chờ", "Đang thực hiện", "Hoàn thành", "Đã hủy");
        cboTrangThai.setValue("Tất cả");
        cboTrangThai.setPrefWidth(150);

        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> xuLyTimKiem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiBoLoc());

        luoi.add(new Label("Từ khóa:"), 0, 0);
        luoi.add(txtTuKhoa, 1, 0);
        luoi.add(new Label("Từ ngày:"), 2, 0);
        luoi.add(dpTuNgay, 3, 0);
        luoi.add(new Label("Đến ngày:"), 4, 0);
        luoi.add(dpDenNgay, 5, 0);

        luoi.add(new Label("Trạng thái:"), 0, 1);
        luoi.add(cboTrangThai, 1, 1);

        HBox thanhNut = new HBox(10, btnTimKiem, btnLamMoi);
        luoi.add(thanhNut, 2, 1, 4, 1);

        khung.getChildren().add(luoi);
        return khung;
    }

    // Tạo khung bảng kết quả tra cứu
    private VBox taoKhungBangKetQua() {
        VBox khung = new VBox(10);
        khung.setStyle("-fx-background-color: #ffffff; -fx-padding: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        HBox thanhTacVu = new HBox(10);
        thanhTacVu.setAlignment(Pos.CENTER_LEFT);

        lblTongSo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        thanhTacVu.getChildren().add(lblTongSo);

        // Cấu hình bảng hiển thị
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy phiếu dịch vụ phù hợp"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<PhieuDichVu, String> colMa = new TableColumn<>("Mã phiếu");
        colMa.setStyle("-fx-alignment: CENTER;");
        colMa.setPrefWidth(150);
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaPhieuDichVu()));

        TableColumn<PhieuDichVu, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setPrefWidth(180);
        colKhach.setCellValueFactory(d -> {
            String ten = d.getValue().getTenKhachHang() != null ? d.getValue().getTenKhachHang() : d.getValue().getMaKhachHang();
            String sdt = (d.getValue().getSoDienThoai() != null && !d.getValue().getSoDienThoai().isEmpty())
                    ? " (" + d.getValue().getSoDienThoai() + ")" : "";
            return new SimpleStringProperty(ten + sdt);
        });

        TableColumn<PhieuDichVu, String> colNgay = new TableColumn<>("Ngày tiếp nhận");
        colNgay.setStyle("-fx-alignment: CENTER;");
        colNgay.setPrefWidth(115);
        colNgay.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNgayTiepNhan() != null ? d.getValue().getNgayTiepNhan().format(DINH_DANG_NGAY) : ""));

        TableColumn<PhieuDichVu, String> colGio = new TableColumn<>("Giờ tiếp nhận");
        colGio.setStyle("-fx-alignment: CENTER;");
        colGio.setPrefWidth(95);
        colGio.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getGioTiepNhan() != null ? d.getValue().getGioTiepNhan().format(DINH_DANG_GIO) : ""));

        TableColumn<PhieuDichVu, String> colNv = new TableColumn<>("Nhân viên");
        colNv.setPrefWidth(150);
        colNv.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTenNhanVien() != null ? d.getValue().getTenNhanVien() : d.getValue().getMaNhanVien()));

        TableColumn<PhieuDichVu, String> colDv = new TableColumn<>("Dịch vụ thực hiện");
        colDv.setPrefWidth(250);
        colDv.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getChuoiDichVu()));

        TableColumn<PhieuDichVu, String> colTongTien = new TableColumn<>("Tổng tiền");
        colTongTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTongTien.setPrefWidth(120);
        colTongTien.setCellValueFactory(d -> new SimpleStringProperty(DINH_DANG_TIEN.format(d.getValue().getTongTien())));

        TableColumn<PhieuDichVu, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setPrefWidth(120);
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));

        TableColumn<PhieuDichVu, String> colGhiChu = new TableColumn<>("Ghi chú / Lý do hủy");
        colGhiChu.setPrefWidth(180);
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getGhiChu() != null ? d.getValue().getGhiChu() : ""));

        tblDanhSach.getColumns().addAll(colMa, colKhach, colNgay, colGio, colNv, colDv, colTongTien, colTrangThai, colGhiChu);

        // Làm mờ các dòng đã hoàn thành hoặc đã hủy
        tblDanhSach.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PhieuDichVu item, boolean empty) {
                super.updateItem(item, empty);
                setOpacity(empty || item == null ? 1.0 :
                        ("Hoàn thành".equalsIgnoreCase(item.getTrangThai()) || "Đã hủy".equalsIgnoreCase(item.getTrangThai())) ? 0.6 : 1.0);
            }
        });

        tblDanhSach.setItems(danhSachHienThi);

        khung.getChildren().addAll(thanhTacVu, tblDanhSach);
        return khung;
    }

    // Thực hiện tìm kiếm theo các tiêu chí đã chọn
    public void xuLyTimKiem() {
        try {
            String tuKhoa = txtTuKhoa.getText() != null ? txtTuKhoa.getText().trim() : "";
            LocalDate tuNgay = dpTuNgay.getValue();
            LocalDate denNgay = dpDenNgay.getValue();
            String trangThai = cboTrangThai.getValue();

            List<PhieuDichVu> ketQua = controller.timKiemPhieuDichVu(tuKhoa, tuNgay, denNgay, trangThai);
            danhSachHienThi.setAll(ketQua);
            lblTongSo.setText("Tổng số: " + ketQua.size() + " phiếu dịch vụ");
            tblDanhSach.getSelectionModel().clearSelection();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiCanhBao("Lỗi tìm kiếm", ex.getMessage());
        } catch (Exception ex) {
            hienThiCanhBao("Lỗi hệ thống", "Đã xảy ra lỗi khi tìm kiếm: " + ex.getMessage());
        }
    }

    // Làm mới bộ lọc
    private void lamMoiBoLoc() {
        txtTuKhoa.clear();
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
        cboTrangThai.setValue("Tất cả");
        xuLyTimKiem();
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(tieuDe);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

