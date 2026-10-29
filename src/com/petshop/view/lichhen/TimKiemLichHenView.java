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
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình Tìm kiếm & Tra cứu lịch hẹn chăm sóc thú cưng (APT_SEARCH).
 * Hỗ trợ tìm kiếm theo từ khóa khách hàng, thú cưng, khoảng ngày, trạng thái và nhân viên; hủy lịch có xác nhận.
 */
public class TimKiemLichHenView extends VBox {

    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final LichHenController controller = new LichHenController();
    private final ObservableList<LichHen> danhSachHienThi = FXCollections.observableArrayList();

    // Các điều khiển tìm kiếm
    private final TextField txtTuKhoa = new TextField();
    private final DatePicker dpTuNgay = new DatePicker();
    private final DatePicker dpDenNgay = new DatePicker();
    private final ComboBox<String> cboTrangThai = new ComboBox<>();
    private final ComboBox<String[]> cboNhanVien = new ComboBox<>();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");

    // Các nút thao tác
    private final Button btnThemMoi = new Button("Đặt lịch mới");
    private final Button btnDoiLich = new Button("Đổi lịch hẹn");
    private final Button btnHuyLich = new Button("Hủy lịch");
    private final Button btnLamMoiBang = new Button("Làm mới");
    private final Label lblTongSo = new Label("Tổng số: 0 lịch hẹn");

    // Bảng kết quả
    private final TableView<LichHen> tblDanhSach = new TableView<>();

    public TimKiemLichHenView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề phân hệ
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("LỊCH HẸN - TÌM KIẾM & TRA CỨU");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Tra cứu lịch hẹn theo khách hàng, thú cưng, khoảng ngày, trạng thái và nhân viên phụ trách.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung bộ lọc tìm kiếm
        VBox pnlBoLoc = taoKhungBoLoc();

        // 3. Khung bảng kết quả
        VBox pnlBang = taoKhungBangKetQua();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, pnlBoLoc, pnlBang);

        capNhatTrangThaiNut(null);
        xuLyTimKiem();
    }

    // Tạo khung điều khiển bộ lọc tìm kiếm
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

        dpTuNgay.setPromptText("Từ ngày");
        dpTuNgay.setPrefWidth(140);

        dpDenNgay.setPromptText("Đến ngày");
        dpDenNgay.setPrefWidth(140);

        cboTrangThai.getItems().addAll("Tất cả", "Đã đặt", "Đã xác nhận", "Đang thực hiện", "Hoàn thành", "Đã hủy");
        cboTrangThai.setValue("Tất cả");
        cboTrangThai.setPrefWidth(145);

        // Nạp danh sách nhân viên phụ trách
        cboNhanVien.setPrefWidth(180);
        cboNhanVien.getItems().add(new String[]{"", "Tất cả nhân viên", ""});
        List<String[]> dsNv = controller.layDanhSachNhanVienChamSoc();
        cboNhanVien.getItems().addAll(dsNv);
        cboNhanVien.setValue(cboNhanVien.getItems().get(0));
        cboNhanVien.setConverter(new StringConverter<>() {
            @Override
            public String toString(String[] nv) {
                if (nv == null) return "";
                if (nv[0].isEmpty()) return nv[1];
                return nv[1] + " (" + nv[2] + ")";
            }

            @Override
            public String[] fromString(String string) {
                return null;
            }
        });

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
        luoi.add(new Label("Người chăm sóc:"), 2, 1);
        luoi.add(cboNhanVien, 3, 1);

        HBox thanhNutLoc = new HBox(10);
        thanhNutLoc.getChildren().addAll(btnTimKiem, btnLamMoi);
        luoi.add(thanhNutLoc, 4, 1, 2, 1);

        khung.getChildren().add(luoi);
        return khung;
    }

    // Tạo bảng hiển thị kết quả tìm kiếm
    private VBox taoKhungBangKetQua() {
        VBox khung = new VBox(10);
        khung.getStyleClass().add("hop-noi-dung");

        HBox thanhTacVu = new HBox(10);
        thanhTacVu.getStyleClass().add("thanh-tac-vu");
        thanhTacVu.setAlignment(Pos.CENTER_LEFT);

        lblTongSo.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        btnThemMoi.getStyleClass().add("nut-chinh");
        btnThemMoi.setOnAction(e -> moCuaSoThemMoi());

        btnDoiLich.getStyleClass().add("nut-phu");
        btnDoiLich.setOnAction(e -> moCuaSoDoiLich());

        btnHuyLich.getStyleClass().add("nut-xoa");
        btnHuyLich.setOnAction(e -> xuLyHuyLich());

        btnLamMoiBang.getStyleClass().add("nut-phu");
        btnLamMoiBang.setOnAction(e -> xuLyTimKiem());

        thanhTacVu.getChildren().addAll(lblTongSo, khoangTrong, btnThemMoi, btnDoiLich, btnHuyLich, btnLamMoiBang);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy lịch hẹn phù hợp"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<LichHen, String> colMa = new TableColumn<>("Mã lịch hẹn");
        colMa.setPrefWidth(150);
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaLichHen()));

        TableColumn<LichHen, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setPrefWidth(170);
        colKhach.setCellValueFactory(d -> {
            String ten = d.getValue().getTenKhachHang() != null ? d.getValue().getTenKhachHang() : d.getValue().getMaKhachHang();
            String sdt = d.getValue().getSoDienThoai() != null ? " (" + d.getValue().getSoDienThoai() + ")" : "";
            return new SimpleStringProperty(ten + sdt);
        });

        TableColumn<LichHen, String> colThuCung = new TableColumn<>("Thú cưng");
        colThuCung.setPrefWidth(120);
        colThuCung.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTenThuCung() != null ? d.getValue().getTenThuCung() : d.getValue().getMaThuCung()
        ));

        TableColumn<LichHen, String> colNgay = new TableColumn<>("Ngày hẹn");
        colNgay.setPrefWidth(110);
        colNgay.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNgayHen() != null ? d.getValue().getNgayHen().format(DINH_DANG_NGAY) : ""
        ));

        TableColumn<LichHen, String> colGio = new TableColumn<>("Giờ hẹn");
        colGio.setPrefWidth(85);
        colGio.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getGioHen() != null ? d.getValue().getGioHen().format(DINH_DANG_GIO) : ""
        ));

        TableColumn<LichHen, String> colNhanVien = new TableColumn<>("Người chăm sóc");
        colNhanVien.setPrefWidth(150);
        colNhanVien.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTenNhanVien() != null ? d.getValue().getTenNhanVien() : d.getValue().getMaNhanVien()
        ));

        TableColumn<LichHen, String> colDichVu = new TableColumn<>("Dịch vụ thực hiện");
        colDichVu.setPrefWidth(190);
        colDichVu.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getChuoiDichVu()));

        TableColumn<LichHen, String> colTongTien = new TableColumn<>("Tổng tiền");
        colTongTien.setPrefWidth(115);
        colTongTien.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTongTien.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTongTien() != null ? DINH_DANG_TIEN.format(d.getValue().getTongTien()) : "0 đ"
        ));

        TableColumn<LichHen, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(130);
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));

        TableColumn<LichHen, String> colGhiChu = new TableColumn<>("Ghi chú / Lý do hủy");
        colGhiChu.setMinWidth(160);
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getGhiChu() != null ? d.getValue().getGhiChu() : ""
        ));

        tblDanhSach.getColumns().addAll(List.of(
                colMa, colKhach, colThuCung, colNgay, colGio, colNhanVien, colDichVu, colTongTien, colTrangThai, colGhiChu
        ));

        // Làm mờ các dòng đã Hoàn thành hoặc Đã hủy
        tblDanhSach.setRowFactory(tv -> {
            TableRow<LichHen> row = new TableRow<>() {
                @Override
                protected void updateItem(LichHen item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setOpacity(1.0);
                    } else if ("Hoàn thành".equalsIgnoreCase(item.getTrangThai()) || "Đã hủy".equalsIgnoreCase(item.getTrangThai())) {
                        setOpacity(0.55);
                    } else {
                        setOpacity(1.0);
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moCuaSoDoiLich();
                }
            });
            return row;
        });

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));
        tblDanhSach.setItems(danhSachHienThi);

        khung.getChildren().addAll(thanhTacVu, tblDanhSach);
        return khung;
    }

    // Bật tắt nút thao tác
    private void capNhatTrangThaiNut(LichHen lh) {
        if (lh == null) {
            btnDoiLich.setDisable(true);
            btnHuyLich.setDisable(true);
            return;
        }

        boolean daKetThuc = "Hoàn thành".equalsIgnoreCase(lh.getTrangThai()) || "Đã hủy".equalsIgnoreCase(lh.getTrangThai());
        btnDoiLich.setDisable(daKetThuc);
        btnHuyLich.setDisable(daKetThuc);
    }

    // Thực hiện tìm kiếm theo các tiêu chí đã nhập
    public void xuLyTimKiem() {
        try {
            String tuKhoa = txtTuKhoa.getText() != null ? txtTuKhoa.getText().trim() : "";
            LocalDate tuNgay = dpTuNgay.getValue();
            LocalDate denNgay = dpDenNgay.getValue();
            String trangThai = cboTrangThai.getValue();
            String maNv = null;
            if (cboNhanVien.getValue() != null && !cboNhanVien.getValue()[0].isEmpty()) {
                maNv = cboNhanVien.getValue()[0];
            }

            List<LichHen> ketQua = controller.timKiemLichHen(tuKhoa, tuNgay, denNgay, trangThai, maNv);
            danhSachHienThi.setAll(ketQua);
            lblTongSo.setText("Tổng số: " + ketQua.size() + " lịch hẹn");
            tblDanhSach.getSelectionModel().clearSelection();
            capNhatTrangThaiNut(null);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    // Đặt lại toàn bộ bộ lọc
    private void lamMoiBoLoc() {
        txtTuKhoa.clear();
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
        cboTrangThai.setValue("Tất cả");
        cboNhanVien.setValue(cboNhanVien.getItems().get(0));
        xuLyTimKiem();
    }

    // Mở popup Đặt lịch hẹn mới
    private void moCuaSoThemMoi() {
        Window owner = getScene() != null ? getScene().getWindow() : null;
        DatLichHenView.hienThi(owner, this::xuLyTimKiem);
    }

    // Mở popup đổi lịch hẹn cho dòng đang chọn
    private void moCuaSoDoiLich() {
        LichHen chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một lịch hẹn trong bảng!");
            return;
        }

        if ("Hoàn thành".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Lịch hẹn đã hoàn tất, không thể đổi lịch!");
            return;
        }
        if ("Đã hủy".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Lịch hẹn này đã bị hủy, không thể đổi lịch!");
            return;
        }

        CapNhatLichHenView.moPopupCapNhat(getScene() != null ? getScene().getWindow() : null, chon, this::xuLyTimKiem);
    }

    // Hủy lịch hẹn có xác nhận và nhập lý do
    private void xuLyHuyLich() {
        LichHen chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một lịch hẹn cần hủy!");
            return;
        }

        if ("Hoàn thành".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Lịch hẹn đã hoàn thành, không thể hủy!");
            return;
        }
        if ("Đã hủy".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Lịch hẹn này đã bị hủy trước đó!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Xác nhận hủy lịch hẹn");
        dialog.setHeaderText("Hủy lịch hẹn mã: " + chon.getMaLichHen() + "\nKhách hàng: " + chon.getTenKhachHang());
        dialog.setContentText("Nhập lý do hủy lịch hẹn (*):");

        Optional<String> ketQua = dialog.showAndWait();
        if (ketQua.isPresent()) {
            String lyDo = ketQua.get().trim();
            if (lyDo.isEmpty()) {
                hienThiCanhBao("Vui lòng nhập lý do hủy lịch hẹn!");
                return;
            }

            try {
                controller.huyLichHen(chon.getMaLichHen(), lyDo);
                Alert thongBao = new Alert(Alert.AlertType.INFORMATION, "Đã hủy lịch hẹn thành công!");
                thongBao.setTitle("Thông báo");
                thongBao.setHeaderText(null);
                thongBao.showAndWait();
                xuLyTimKiem();
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                hienThiLoi(ex.getMessage());
            }
        }
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING, noiDung);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR, noiDung);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

