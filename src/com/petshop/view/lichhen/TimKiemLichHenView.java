package com.petshop.view.lichhen;

import com.petshop.controller.LichHenController;
import com.petshop.model.LichHen;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

/**
 * Phân hệ Tìm kiếm & Tra cứu lịch hẹn chăm sóc thú cưng (APT_SEARCH / LH_TIM_KIEM).
 * Hỗ trợ tra cứu đa tiêu chí: từ khóa (khách/thú cưng/mã), khoảng ngày, trạng thái, nhân viên phụ trách.
 */
public class TimKiemLichHenView extends VBox {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final LichHenController controller = new LichHenController();
    private final ObservableList<LichHen> danhSachHienThi = FXCollections.observableArrayList();

    // Điều khiển bộ lọc
    private final TextField txtTuKhoa = new TextField();
    private final DatePicker dpTuNgay = new DatePicker();
    private final DatePicker dpDenNgay = new DatePicker();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");

    // Nhãn tổng số lượng
    private final Label lblTongSo = new Label("Tổng số: 0 lịch hẹn");

    // Bảng kết quả
    private final TableView<LichHen> tblDanhSach = new TableView<>();

    public TimKiemLichHenView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("LỊCH HẸN - TÌM KIẾM & TRA CỨU");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tra cứu lịch hẹn theo khách hàng, thú cưng, khoảng ngày và trạng thái.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Bộ lọc & Bảng dữ liệu
        VBox pnlBang = taoKhungBangKetQua();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, taoKhungBoLoc(), pnlBang);

        xuLyTimKiem();
    }

    // -------------------------------------------------------------------------
    // BỘ LỌC TÌM KIẾM
    // -------------------------------------------------------------------------

    private VBox taoKhungBoLoc() {
        VBox khung = new VBox(10);
        khung.getStyleClass().add("hop-noi-dung");

        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);
        luoi.setAlignment(Pos.CENTER_LEFT);

        txtTuKhoa.setPromptText("Mã lịch, tên/SĐT khách, tên thú cưng...");
        txtTuKhoa.setPrefWidth(240);
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());

        dpTuNgay.setPromptText("Từ ngày"); dpTuNgay.setPrefWidth(140);
        dpDenNgay.setPromptText("Đến ngày"); dpDenNgay.setPrefWidth(140);

        cboTrangThai.getItems().addAll("Tất cả", "Đã đặt", "Đã xác nhận", "Đang thực hiện", "Hoàn thành", "Đã hủy");
        cboTrangThai.setValue("Tất cả");
        cboTrangThai.setPrefWidth(145);

        btnTimKiem.getStyleClass().add("nut-chinh");
        btnTimKiem.setOnAction(e -> xuLyTimKiem());
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> lamMoiBoLoc());

        luoi.add(new Label("Từ khóa:"), 0, 0); luoi.add(txtTuKhoa, 1, 0);
        luoi.add(new Label("Từ ngày:"), 2, 0); luoi.add(dpTuNgay, 3, 0);
        luoi.add(new Label("Đến ngày:"), 4, 0); luoi.add(dpDenNgay, 5, 0);

        luoi.add(new Label("Trạng thái:"), 0, 1); luoi.add(cboTrangThai, 1, 1);

        HBox thanhNutLoc = new HBox(10, btnTimKiem, btnLamMoi);
        luoi.add(thanhNutLoc, 2, 1, 2, 1);

        khung.getChildren().add(luoi);
        return khung;
    }

    // -------------------------------------------------------------------------
    // BẢNG KẾT QUẢ TÌM KIẾM
    // -------------------------------------------------------------------------

    private VBox taoKhungBangKetQua() {
        VBox khung = new VBox(10);
        khung.getStyleClass().add("hop-noi-dung");

        HBox thanhTacVu = new HBox(10);
        thanhTacVu.getStyleClass().add("thanh-tac-vu");
        thanhTacVu.setAlignment(Pos.CENTER_LEFT);

        lblTongSo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
        thanhTacVu.getChildren().add(lblTongSo);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy lịch hẹn phù hợp"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        tblDanhSach.getColumns().addAll(List.of(
                taoCot("Mã lịch hẹn", 150, d -> new SimpleStringProperty(d.getValue().getMaLichHen())),
                taoCot("Khách hàng", 200, d -> {
                    String ten = d.getValue().getTenKhachHang() != null ? d.getValue().getTenKhachHang() : d.getValue().getMaKhachHang();
                    String sdt = d.getValue().getSoDienThoai() != null ? " (" + d.getValue().getSoDienThoai() + ")" : "";
                    return new SimpleStringProperty(ten + sdt);
                }),
                taoCot("Thú cưng", 140, d -> new SimpleStringProperty(
                        d.getValue().getTenThuCung() != null ? d.getValue().getTenThuCung() : d.getValue().getMaThuCung())),
                taoCot("Ngày hẹn", 115, d -> new SimpleStringProperty(
                        d.getValue().getNgayHen() != null ? d.getValue().getNgayHen().format(DINH_DANG_NGAY) : "")),
                taoCot("Giờ hẹn", 90, d -> new SimpleStringProperty(
                        d.getValue().getGioHen() != null ? d.getValue().getGioHen().format(DINH_DANG_GIO) : "")),
                taoCot("Dịch vụ thực hiện", 250, d -> new SimpleStringProperty(d.getValue().getChuoiDichVu())),
                taoCot("Trạng thái", 140, d -> new SimpleStringProperty(d.getValue().getTrangThai())),
                taoCot("Ghi chú / Lý do hủy", 220, d -> new SimpleStringProperty(
                        d.getValue().getGhiChu() != null ? d.getValue().getGhiChu() : ""))
        ));

        tblDanhSach.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LichHen item, boolean empty) {
                super.updateItem(item, empty);
                setOpacity(empty || item == null ? 1.0 :
                        ("Hoàn thành".equalsIgnoreCase(item.getTrangThai()) || "Đã hủy".equalsIgnoreCase(item.getTrangThai())) ? 0.55 : 1.0);
            }
        });

        tblDanhSach.setItems(danhSachHienThi);

        khung.getChildren().addAll(thanhTacVu, tblDanhSach);
        return khung;
    }

    // -------------------------------------------------------------------------
    // XỬ LÝ SỰ KIỆN TÌM KIẾM
    // -------------------------------------------------------------------------

    public void xuLyTimKiem() {
        try {
            String tuKhoa = txtTuKhoa.getText() != null ? txtTuKhoa.getText().trim() : "";
            LocalDate tuNgay = dpTuNgay.getValue();
            LocalDate denNgay = dpDenNgay.getValue();
            String trangThai = cboTrangThai.getValue();

            List<LichHen> ketQua = controller.timKiemLichHen(tuKhoa, tuNgay, denNgay, trangThai, null);
            danhSachHienThi.setAll(ketQua);
            lblTongSo.setText("Tổng số: " + ketQua.size() + " lịch hẹn");
            tblDanhSach.getSelectionModel().clearSelection();
        } catch (Exception ex) {
            hienThiCanhBao("Lỗi khi tìm kiếm lịch hẹn: " + ex.getMessage());
        }
    }

    private void lamMoiBoLoc() {
        txtTuKhoa.clear();
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
        cboTrangThai.setValue("Tất cả");
        xuLyTimKiem();
    }

    private static TableColumn<LichHen, String> taoCot(String tieuDe, double rong, Function<TableColumn.CellDataFeatures<LichHen, String>, SimpleStringProperty> map) {
        TableColumn<LichHen, String> col = new TableColumn<>(tieuDe);
        col.setPrefWidth(rong);
        col.setCellValueFactory(map::apply);
        return col;
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING, noiDung);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
