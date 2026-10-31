package com.petshop.view.phieudichvu;

import com.petshop.controller.PhieuDichVuController;
import com.petshop.model.ChiTietPhieuDichVu;
import com.petshop.model.PhieuDichVu;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Giao diện Tìm kiếm & Tra cứu phiếu tiếp nhận dịch vụ (PDV_TIM_KIEM).
 * Hỗ trợ tra cứu đa tiêu chí: mã phiếu/tên/SĐT khách hàng, khoảng ngày và trạng thái.
 * Hỗ trợ thao tác xóa mềm (hủy phiếu có xác nhận) và xem chi tiết phiếu.
 */
public class TimKiemPhieuDichVuView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
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
    private final Button btnXemChiTiet = new Button("Xem chi tiết");
    private final Button btnHuyPhieu = new Button("Hủy phiếu");

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

        capNhatTrangThaiNut(null);
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
        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        btnXemChiTiet.getStyleClass().add("nut-phu");
        btnXemChiTiet.setOnAction(e -> moDialogXemChiTiet());

        btnHuyPhieu.getStyleClass().add("nut-xoa");
        btnHuyPhieu.setOnAction(e -> xuLyHuyPhieu());

        thanhTacVu.getChildren().addAll(lblTongSo, khoangTrong, btnXemChiTiet, btnHuyPhieu);

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
        tblDanhSach.setRowFactory(tv -> {
            TableRow<PhieuDichVu> row = new TableRow<>() {
                @Override
                protected void updateItem(PhieuDichVu item, boolean empty) {
                    super.updateItem(item, empty);
                    setOpacity(empty || item == null ? 1.0 :
                            ("Hoàn thành".equalsIgnoreCase(item.getTrangThai()) || "Đã hủy".equalsIgnoreCase(item.getTrangThai())) ? 0.6 : 1.0);
                }
            };
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    moDialogXemChiTiet();
                }
            });
            return row;
        });

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));
        tblDanhSach.setItems(danhSachHienThi);

        khung.getChildren().addAll(thanhTacVu, tblDanhSach);
        return khung;
    }

    // Cập nhật trạng thái các nút thao tác
    private void capNhatTrangThaiNut(PhieuDichVu pdv) {
        if (pdv == null) {
            btnXemChiTiet.setDisable(true);
            btnHuyPhieu.setDisable(true);
            return;
        }
        btnXemChiTiet.setDisable(false);
        boolean daKetThuc = "Hoàn thành".equalsIgnoreCase(pdv.getTrangThai()) || "Đã hủy".equalsIgnoreCase(pdv.getTrangThai());
        btnHuyPhieu.setDisable(daKetThuc);
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
            capNhatTrangThaiNut(null);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiCanhBao("Lỗi tìm kiếm", ex.getMessage());
        } catch (Exception ex) {
            hienThiCanhBao("Lỗi hệ thống", "Đã xảy ra lỗi khi tìm kiếm: " + ex.getMessage());
        }
    }

    // Xóa mềm phiếu dịch vụ (Hủy phiếu có xác nhận)
    private void xuLyHuyPhieu() {
        PhieuDichVu chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Chưa chọn phiếu", "Vui lòng chọn một phiếu dịch vụ cần hủy!");
            return;
        }

        if ("Hoàn thành".equalsIgnoreCase(chon.getTrangThai()) || "Đã hủy".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Không thể hủy", "Phiếu dịch vụ đã hoàn thành hoặc đã bị hủy trước đó!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Xác nhận hủy phiếu dịch vụ");
        dialog.setHeaderText("Hủy phiếu dịch vụ: " + chon.getMaPhieuDichVu() + "\nKhách hàng: " + chon.getTenKhachHang());
        dialog.setContentText("Nhập lý do hủy phiếu (*):");

        Optional<String> ketQua = dialog.showAndWait();
        if (ketQua.isPresent()) {
            String lyDo = ketQua.get().trim();
            if (lyDo.isEmpty()) {
                hienThiCanhBao("Thiếu lý do", "Vui lòng nhập lý do hủy phiếu dịch vụ!");
                return;
            }
            try {
                controller.huyPhieuDichVu(chon.getMaPhieuDichVu(), lyDo);
                hienThiThongBao("Thành công", "Đã hủy phiếu dịch vụ mã " + chon.getMaPhieuDichVu() + "!");
                xuLyTimKiem();
            } catch (Exception ex) {
                hienThiCanhBao("Lỗi khi hủy phiếu", ex.getMessage());
            }
        }
    }

    // Mở Modal xem chi tiết các dòng dịch vụ của phiếu
    private void moDialogXemChiTiet() {
        PhieuDichVu chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) return;

        Window owner = getScene() != null ? getScene().getWindow() : null;
        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) cuaSo.initOwner(owner);
        cuaSo.setTitle("Chi tiết phiếu dịch vụ - " + chon.getMaPhieuDichVu());

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.setStyle("-fx-background-color: #f4f6f8;");

        Label lblTieuDe = new Label("CHI TIẾT PHIẾU TIẾP NHẬN DỊCH VỤ");
        lblTieuDe.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        Label lblPhu = new Label("Mã phiếu: " + chon.getMaPhieuDichVu() + "  |  Trạng thái: " + chon.getTrangThai());
        lblPhu.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        // Bảng thông tin tóm tắt
        GridPane luoi = new GridPane();
        luoi.setHgap(10);
        luoi.setVgap(8);
        luoi.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #cbd5e1; -fx-border-radius: 4px;");

        luoi.add(new Label("Khách hàng:"), 0, 0);
        Label lblKhach = new Label(chon.getTenKhachHang() + (chon.getSoDienThoai() != null ? " (" + chon.getSoDienThoai() + ")" : ""));
        lblKhach.setStyle("-fx-font-weight: bold;");
        luoi.add(lblKhach, 1, 0);

        luoi.add(new Label("Nhân viên:"), 2, 0);
        luoi.add(new Label(chon.getTenNhanVien() != null ? chon.getTenNhanVien() : chon.getMaNhanVien()), 3, 0);

        luoi.add(new Label("Thời gian tiếp nhận:"), 0, 1);
        String ngayGio = (chon.getGioTiepNhan() != null ? chon.getGioTiepNhan().format(DINH_DANG_GIO) : "")
                + " " + (chon.getNgayTiepNhan() != null ? chon.getNgayTiepNhan().format(DINH_DANG_NGAY) : "");
        luoi.add(new Label(ngayGio), 1, 1);

        luoi.add(new Label("Ghi chú:"), 2, 1);
        luoi.add(new Label(chon.getGhiChu() != null ? chon.getGhiChu() : ""), 3, 1);

        // Bảng dịch vụ chi tiết
        List<ChiTietPhieuDichVu> dsCt = chon.getDanhSachChiTiet();
        if (dsCt == null || dsCt.isEmpty()) {
            dsCt = controller.layChiTietPhieuDichVu(chon.getMaPhieuDichVu());
        }

        TableView<ChiTietPhieuDichVu> tblCt = new TableView<>(FXCollections.observableArrayList(dsCt));
        tblCt.getStyleClass().add("bang-du-lieu");
        tblCt.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblCt.setPrefHeight(180);

        TableColumn<ChiTietPhieuDichVu, String> colTc = new TableColumn<>("Thú cưng");
        colTc.setPrefWidth(160);
        colTc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenThuCung()));

        TableColumn<ChiTietPhieuDichVu, String> colDv = new TableColumn<>("Dịch vụ chăm sóc");
        colDv.setPrefWidth(240);
        colDv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDichVu()));

        TableColumn<ChiTietPhieuDichVu, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGia.setPrefWidth(120);
        colGia.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDonGia() != null ? DINH_DANG_TIEN.format(c.getValue().getDonGia()) : "0 đ"));

        tblCt.getColumns().addAll(colTc, colDv, colGia);

        Label lblTongTien = new Label("Tổng tiền thanh toán: " + DINH_DANG_TIEN.format(chon.getTongTien()));
        lblTongTien.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #0284c7;");
        HBox pnlTong = new HBox(lblTongTien);
        pnlTong.setAlignment(Pos.CENTER_RIGHT);

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> cuaSo.close());

        HBox pnlNut = new HBox(btnDong);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        pnlGoc.getChildren().addAll(lblTieuDe, lblPhu, luoi, tblCt, pnlTong, pnlNut);

        Scene scene = new Scene(pnlGoc, 680, 480);
        try {
            scene.getStylesheets().add(TimKiemPhieuDichVuView.class.getResource(CSS_CHUNG).toExternalForm());
        } catch (Exception ignored) {}
        cuaSo.setScene(scene);
        cuaSo.showAndWait();
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

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(tieuDe);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

