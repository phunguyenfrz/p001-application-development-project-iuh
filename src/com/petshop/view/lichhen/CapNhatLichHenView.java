package com.petshop.view.lichhen;

import com.petshop.controller.LichHenController;
import com.petshop.model.ChiTietLichHen;
import com.petshop.model.DichVuChamSoc;
import com.petshop.model.LichHen;
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
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Phân hệ Quản lý & Cập nhật lịch hẹn chăm sóc thú cưng (APT_RESCHEDULE).
 * Hỗ trợ cập nhật thông tin lịch hẹn, đổi ngày giờ/dịch vụ, hoàn thành hoặc hủy lịch.
 */
public class CapNhatLichHenView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final LichHenController controller = new LichHenController();
    private final ObservableList<LichHen> danhSachHienThi = FXCollections.observableArrayList();

    // Các nút chức năng thanh tác vụ
    private final Button btnCapNhat = new Button("Cập nhật lịch hẹn");
    private final Button btnHuyLich = new Button("Hủy lịch");
    private final Button btnLamMoi = new Button("Làm mới");

    // Bảng danh sách lịch hẹn
    private final TableView<LichHen> tblDanhSach = new TableView<>();

    public CapNhatLichHenView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề phân hệ
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT LỊCH HẸN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Quản lý danh sách lịch hẹn: cập nhật ngày giờ/dịch vụ hoặc hủy lịch.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Bảng dữ liệu và thanh tác vụ
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, pnlBang);

        capNhatTrangThaiNut(null);
        taiLaiDanhSach();
    }

    // -------------------------------------------------------------------------
    // KHỞI TẠO BẢNG DỮ LIỆU VÀ THANH TÁC VỤ
    // -------------------------------------------------------------------------

    private VBox taoKhungBangDanhSach() {
        VBox khung = new VBox(10);
        khung.getStyleClass().add("hop-noi-dung");

        HBox thanhNut = new HBox(10);
        thanhNut.getStyleClass().add("thanh-tac-vu");
        thanhNut.setAlignment(Pos.CENTER_LEFT);

        Label lblDanhSach = new Label("Danh sách lịch hẹn");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");
        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        btnCapNhat.getStyleClass().add("nut-chinh");
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());

        btnHuyLich.getStyleClass().add("nut-xoa");
        btnHuyLich.setOnAction(e -> xuLyHuyLich());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiLaiDanhSach());

        thanhNut.getChildren().addAll(lblDanhSach, khoangTrong, btnCapNhat, btnHuyLich, btnLamMoi);

        // Cấu hình bảng hiển thị
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có lịch hẹn nào"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        tblDanhSach.getColumns().addAll(List.of(
                taoCot("Mã lịch hẹn", 150, d -> new SimpleStringProperty(d.getValue().getMaLichHen())),
                taoCot("Khách hàng", 180, d -> {
                    String ten = d.getValue().getTenKhachHang() != null ? d.getValue().getTenKhachHang() : d.getValue().getMaKhachHang();
                    String sdt = d.getValue().getSoDienThoai() != null ? " (" + d.getValue().getSoDienThoai() + ")" : "";
                    return new SimpleStringProperty(ten + sdt);
                }),
                taoCot("Thú cưng", 130, d -> new SimpleStringProperty(
                        d.getValue().getTenThuCung() != null ? d.getValue().getTenThuCung() : d.getValue().getMaThuCung())),
                taoCot("Ngày hẹn", 115, d -> new SimpleStringProperty(
                        d.getValue().getNgayHen() != null ? d.getValue().getNgayHen().format(DINH_DANG_NGAY) : "")),
                taoCot("Giờ hẹn", 90, d -> new SimpleStringProperty(
                        d.getValue().getGioHen() != null ? d.getValue().getGioHen().format(DINH_DANG_GIO) : "")),
                taoCot("Người chăm sóc", 160, d -> new SimpleStringProperty(
                        d.getValue().getTenNhanVien() != null ? d.getValue().getTenNhanVien() : d.getValue().getMaNhanVien())),
                taoCot("Dịch vụ thực hiện", 240, d -> new SimpleStringProperty(d.getValue().getChuoiDichVu())),
                taoCot("Trạng thái", 130, d -> new SimpleStringProperty(d.getValue().getTrangThai())),
                taoCot("Ghi chú", 160, d -> new SimpleStringProperty(
                        d.getValue().getGhiChu() != null ? d.getValue().getGhiChu() : ""))
        ));

        // Định dạng mờ dòng đã hoàn thành/hủy, nhấp đúp để đổi lịch
        tblDanhSach.setRowFactory(tv -> {
            TableRow<LichHen> row = new TableRow<>() {
                @Override
                protected void updateItem(LichHen item, boolean empty) {
                    super.updateItem(item, empty);
                    setOpacity(empty || item == null ? 1.0 :
                            ("Hoàn thành".equalsIgnoreCase(item.getTrangThai()) || "Đã hủy".equalsIgnoreCase(item.getTrangThai())) ? 0.55 : 1.0);
                }
            };
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) moCuaSoCapNhat();
            });
            return row;
        });

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));
        tblDanhSach.setItems(danhSachHienThi);

        khung.getChildren().addAll(thanhNut, tblDanhSach);
        return khung;
    }

    // -------------------------------------------------------------------------
    // XỬ LÝ SỰ KIỆN & NGHIỆP VỤ
    // -------------------------------------------------------------------------

    private void capNhatTrangThaiNut(LichHen lh) {
        if (lh == null) {
            btnCapNhat.setDisable(true);
            btnHuyLich.setDisable(true);
            return;
        }
        boolean daKetThuc = "Hoàn thành".equalsIgnoreCase(lh.getTrangThai()) || "Đã hủy".equalsIgnoreCase(lh.getTrangThai());
        btnCapNhat.setDisable(daKetThuc);
        btnHuyLich.setDisable(daKetThuc);
    }

    private void xuLyHuyLich() {
        LichHen chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một lịch hẹn cần hủy!");
            return;
        }
        if ("Hoàn thành".equalsIgnoreCase(chon.getTrangThai()) || "Đã hủy".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Không thể hủy lịch hẹn đã hoàn thành hoặc đã bị hủy trước đó!");
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
                new Alert(Alert.AlertType.INFORMATION, "Đã hủy lịch hẹn thành công!").showAndWait();
                taiLaiDanhSach();
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi hủy lịch hẹn: " + ex.getMessage());
            }
        }
    }

    public void taiLaiDanhSach() {
        try {
            danhSachHienThi.setAll(controller.layDanhSachLichHen());
            tblDanhSach.getSelectionModel().clearSelection();
            capNhatTrangThaiNut(null);
        } catch (Exception e) {
            hienThiLoi("Lỗi khi tải danh sách lịch hẹn: " + e.getMessage());
        }
    }

    private void moCuaSoCapNhat() {
        LichHen chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một lịch hẹn trong bảng để cập nhật!");
            return;
        }
        if ("Hoàn thành".equalsIgnoreCase(chon.getTrangThai()) || "Đã hủy".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Không thể sửa đổi thông tin lịch hẹn đã hoàn thành hoặc đã bị hủy!");
            return;
        }
        Window owner = getScene() != null ? getScene().getWindow() : null;
        moPopupCapNhat(owner, chon, this::taiLaiDanhSach);
    }

    // -------------------------------------------------------------------------
    // HỘP THOẠI POPUP ĐỔI LỊCH HẸN (APT_RESCHEDULE)
    // -------------------------------------------------------------------------

    public static void moPopupCapNhat(Window owner, LichHen lichHen, Runnable onThanhCong) {
        LichHenController ctrl = new LichHenController();
        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) cuaSo.initOwner(owner);
        cuaSo.setTitle("Cập nhật lịch hẹn - " + lichHen.getMaLichHen());

        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề
        Label lblTieuDe = new Label("CẬP NHẬT LỊCH HẸN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblPhu = new Label("Mã lịch: " + lichHen.getMaLichHen() + "  |  Trạng thái hiện tại: " + lichHen.getTrangThai());
        lblPhu.getStyleClass().add("mo-ta-phan-he");

        // 2. Thông tin lịch hẹn
        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);

        TextField txtKhach = new TextField((lichHen.getTenKhachHang() != null ? lichHen.getTenKhachHang() : lichHen.getMaKhachHang())
                + (lichHen.getSoDienThoai() != null ? " - " + lichHen.getSoDienThoai() : ""));
        txtKhach.setEditable(false);

        TextField txtThuCung = new TextField(lichHen.getTenThuCung() != null ? lichHen.getTenThuCung() : lichHen.getMaThuCung());
        txtThuCung.setEditable(false);

        DatePicker dpNgay = new DatePicker(lichHen.getNgayHen());
        dpNgay.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        ComboBox<String> cboGio = new ComboBox<>();
        for (int h = 8; h <= 20; h++) {
            cboGio.getItems().add(String.format("%02d:00", h));
            if (h < 20) cboGio.getItems().add(String.format("%02d:30", h));
        }
        if (lichHen.getGioHen() != null) cboGio.setValue(lichHen.getGioHen().format(DINH_DANG_GIO));

        ComboBox<String[]> cboNv = new ComboBox<>();
        List<String[]> dsNv = ctrl.layDanhSachNhanVienChamSoc();
        cboNv.getItems().setAll(dsNv);
        cboNv.setConverter(taoConverter(nv -> nv[1] + " (" + nv[2] + ")"));
        dsNv.stream().filter(n -> n[0].equalsIgnoreCase(lichHen.getMaNhanVien())).findFirst().ifPresent(cboNv::setValue);

        TextField txtGhiChu = new TextField(lichHen.getGhiChu() != null ? lichHen.getGhiChu() : "");
        txtGhiChu.setPromptText("Nhập ghi chú yêu cầu thêm nếu có...");

        luoi.add(new Label("Khách hàng:"), 0, 0); luoi.add(txtKhach, 1, 0);
        luoi.add(new Label("Thú cưng:"), 2, 0); luoi.add(txtThuCung, 3, 0);
        luoi.add(new Label("Ngày hẹn mới (*):"), 0, 1); luoi.add(dpNgay, 1, 1);
        luoi.add(new Label("Giờ hẹn mới (*):"), 2, 1); luoi.add(cboGio, 3, 1);
        luoi.add(new Label("Người chăm sóc (*):"), 0, 2); luoi.add(cboNv, 1, 2);
        luoi.add(new Label("Ghi chú:"), 2, 2); luoi.add(txtGhiChu, 3, 2);

        VBox pnlThongTin = new VBox(10, luoi);
        pnlThongTin.getStyleClass().add("hop-noi-dung");

        // 3. Danh sách dịch vụ
        VBox pnlDichVu = new VBox(10);
        pnlDichVu.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlDichVu, Priority.ALWAYS);

        ComboBox<DichVuChamSoc> cboDv = new ComboBox<>();
        cboDv.setPromptText("Chọn dịch vụ chăm sóc...");
        cboDv.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cboDv, Priority.ALWAYS);
        cboDv.getItems().setAll(ctrl.layDanhSachDichVu());
        cboDv.setConverter(taoConverter(DichVuChamSoc::getTenDichVu));

        Button btnThemDv = new Button("+ Thêm vào bảng");
        btnThemDv.getStyleClass().add("nut-chinh");
        btnThemDv.setMinWidth(Region.USE_PREF_SIZE);

        HBox thanhChonDv = new HBox(8, new Label("Dịch vụ chăm sóc:"), cboDv, btnThemDv);
        thanhChonDv.setAlignment(Pos.CENTER_LEFT);

        // Bảng chi tiết dịch vụ
        ObservableList<ChiTietLichHen> dsChiTiet = FXCollections.observableArrayList();
        if (lichHen.getDanhSachChiTiet() != null) {
            dsChiTiet.addAll(lichHen.getDanhSachChiTiet());
        } else {
            dsChiTiet.addAll(ctrl.layChiTietLichHen(lichHen.getMaLichHen()));
        }

        TableView<ChiTietLichHen> tblChiTiet = new TableView<>(dsChiTiet);
        tblChiTiet.getStyleClass().add("bang-du-lieu");
        tblChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblChiTiet.setPrefHeight(180);
        VBox.setVgrow(tblChiTiet, Priority.ALWAYS);

        TableColumn<ChiTietLichHen, String> colTenDv = new TableColumn<>("Tên dịch vụ chăm sóc");
        colTenDv.setPrefWidth(450);
        colTenDv.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenDichVu()));

        TableColumn<ChiTietLichHen, Void> colXoa = new TableColumn<>("Thao tác");
        colXoa.setPrefWidth(90);
        colXoa.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Xóa");
            {
                btn.getStyleClass().add("nut-xoa");
                btn.setOnAction(e -> dsChiTiet.remove(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tblChiTiet.getColumns().addAll(List.of(colTenDv, colXoa));

        btnThemDv.setOnAction(e -> {
            DichVuChamSoc dv = cboDv.getValue();
            if (dv == null) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng chọn dịch vụ chăm sóc trước khi thêm!").showAndWait();
                return;
            }
            if (dsChiTiet.stream().anyMatch(ct -> ct.getMaDichVu().equals(dv.getMaDichVu()))) {
                new Alert(Alert.AlertType.WARNING, "Dịch vụ '" + dv.getTenDichVu() + "' đã có trong lịch hẹn!").showAndWait();
                return;
            }
            BigDecimal cn = dsChiTiet.stream()
                    .map(ChiTietLichHen::getCanNangThuCung)
                    .filter(c -> c != null && c.compareTo(BigDecimal.ZERO) > 0)
                    .findFirst()
                    .orElse(null);
            double gia = ctrl.traGiaDichVuTheoCanNang(dv.getMaDichVu(), cn != null ? cn.doubleValue() : 0.0);
            dsChiTiet.add(new ChiTietLichHen(lichHen.getMaLichHen(), dv.getMaDichVu(), dv.getTenDichVu(), cn, BigDecimal.valueOf(gia)));
            cboDv.setValue(null);
        });

        pnlDichVu.getChildren().addAll(new Label("Danh sách dịch vụ chăm sóc"), thanhChonDv, tblChiTiet);

        // 4. Nút bấm thao tác
        Button btnLuu = new Button("Lưu thay đổi");
        btnLuu.getStyleClass().add("nut-chinh");
        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> cuaSo.close());

        btnLuu.setOnAction(e -> {
            try {
                if (cboDv.getValue() != null) {
                    DichVuChamSoc dv = cboDv.getValue();
                    if (dsChiTiet.stream().noneMatch(ct -> ct.getMaDichVu().equals(dv.getMaDichVu()))) {
                        BigDecimal cn = dsChiTiet.stream()
                                .map(ChiTietLichHen::getCanNangThuCung)
                                .filter(c -> c != null && c.compareTo(BigDecimal.ZERO) > 0)
                                .findFirst()
                                .orElse(null);
                        double gia = ctrl.traGiaDichVuTheoCanNang(dv.getMaDichVu(), cn != null ? cn.doubleValue() : 0.0);
                        dsChiTiet.add(new ChiTietLichHen(lichHen.getMaLichHen(), dv.getMaDichVu(), dv.getTenDichVu(), cn, BigDecimal.valueOf(gia)));
                    }
                }
                LocalDate ngayMoi = dpNgay.getValue();
                if (ngayMoi == null) throw new IllegalArgumentException("Vui lòng chọn ngày hẹn mới!");
                String gioMoiStr = cboGio.getValue();
                if (gioMoiStr == null || gioMoiStr.trim().isEmpty()) throw new IllegalArgumentException("Vui lòng chọn khung giờ hẹn mới!");
                String[] nvChon = cboNv.getValue();
                if (nvChon == null) throw new IllegalArgumentException("Vui lòng chọn người chăm sóc phụ trách!");
                if (dsChiTiet.isEmpty()) throw new IllegalArgumentException("Lịch hẹn phải có ít nhất một dịch vụ chăm sóc!");

                lichHen.setNgayHen(ngayMoi);
                lichHen.setGioHen(LocalTime.parse(gioMoiStr, DINH_DANG_GIO));
                lichHen.setMaNhanVien(nvChon[0]);
                lichHen.setGhiChu(txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : "");

                ctrl.capNhatLichHen(lichHen, new ArrayList<>(dsChiTiet));
                new Alert(Alert.AlertType.INFORMATION, "Cập nhật lịch hẹn thành công!").showAndWait();
                cuaSo.close();
                if (onThanhCong != null) onThanhCong.run();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
            }
        });

        HBox pnlNut = new HBox(10, btnLuu, btnHuy);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        pnlGoc.getChildren().addAll(new VBox(4, lblTieuDe, lblPhu), pnlThongTin, pnlDichVu, pnlNut);
        Scene scene = new Scene(pnlGoc, 780, 560);
        if (CapNhatLichHenView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(CapNhatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        cuaSo.setScene(scene);
        cuaSo.showAndWait();
    }

    // -------------------------------------------------------------------------
    // CÁC HÀM TIỆN ÍCH DỰNG BẢNG & CONVERTER
    // -------------------------------------------------------------------------

    private static TableColumn<LichHen, String> taoCot(String tieuDe, double rong, Function<TableColumn.CellDataFeatures<LichHen, String>, SimpleStringProperty> map) {
        TableColumn<LichHen, String> col = new TableColumn<>(tieuDe);
        col.setPrefWidth(rong);
        col.setCellValueFactory(map::apply);
        return col;
    }

    public static <T> StringConverter<T> taoConverter(Function<T, String> display) {
        return new StringConverter<>() {
            @Override public String toString(T object) { return object != null ? display.apply(object) : ""; }
            @Override public T fromString(String string) { return null; }
        };
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
