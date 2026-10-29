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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình Quản lý & Cập nhật / Đổi lịch hẹn dịch vụ chăm sóc thú cưng (APT_RESCHEDULE).
 */
public class CapNhatLichHenView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final LichHenController controller = new LichHenController();
    private final ObservableList<LichHen> danhSachHienThi = FXCollections.observableArrayList();

    // Các nút chức năng thanh tác vụ
    private final Button btnThemMoi = new Button("Đặt lịch mới");
    private final Button btnCapNhat = new Button("Đổi lịch hẹn");
    private final Button btnXacNhan = new Button("Xác nhận lịch");
    private final Button btnHoanThanh = new Button("Hoàn thành");
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
        Label lblTieuDe = new Label("LỊCH HẸN - CẬP NHẬT & TIẾP NHẬN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Quản lý danh sách lịch hẹn: đổi ngày giờ/dịch vụ, xác nhận tiếp nhận, hoàn thành hoặc hủy lịch.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Bảng dữ liệu và thanh tác vụ
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, pnlBang);

        capNhatTrangThaiNut(null);
        taiLaiDanhSach();
    }

    // Tạo thanh tác vụ phía trên bảng
    private HBox taoThanhTacVu() {
        HBox thanhNut = new HBox(10);
        thanhNut.getStyleClass().add("thanh-tac-vu");
        thanhNut.setAlignment(Pos.CENTER_LEFT);

        Label lblDanhSach = new Label("Danh sách lịch hẹn");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        btnThemMoi.getStyleClass().add("nut-chinh");
        btnThemMoi.setOnAction(e -> moCuaSoThemMoi());

        btnCapNhat.getStyleClass().add("nut-phu");
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());

        btnXacNhan.getStyleClass().add("nut-phu");
        btnXacNhan.setOnAction(e -> xuLyChuyenTrangThai("Đã xác nhận", "Xác nhận tiếp nhận lịch hẹn này?"));

        btnHoanThanh.getStyleClass().add("nut-chinh");
        btnHoanThanh.setOnAction(e -> xuLyChuyenTrangThai("Hoàn thành", "Xác nhận đã hoàn thành toàn bộ dịch vụ cho lịch hẹn này?"));

        btnHuyLich.getStyleClass().add("nut-xoa");
        btnHuyLich.setOnAction(e -> xuLyHuyLich());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiLaiDanhSach());

        thanhNut.getChildren().addAll(
                lblDanhSach, khoangTrong,
                btnThemMoi, btnCapNhat, btnXacNhan, btnHoanThanh, btnHuyLich, btnLamMoi
        );
        return thanhNut;
    }

    // Tạo bảng danh sách lịch hẹn
    private VBox taoKhungBangDanhSach() {
        VBox khung = new VBox(10);
        khung.getStyleClass().add("hop-noi-dung");

        HBox thanhNut = taoThanhTacVu();

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có lịch hẹn nào"));
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
        colTrangThai.setPrefWidth(125);
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));

        TableColumn<LichHen, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setMinWidth(150);
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
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moCuaSoCapNhat();
                }
            });
            return row;
        });

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));
        tblDanhSach.setItems(danhSachHienThi);

        khung.getChildren().addAll(thanhNut, tblDanhSach);
        return khung;
    }

    // Bật/tắt các nút thao tác tương ứng theo trạng thái của dòng đang chọn
    private void capNhatTrangThaiNut(LichHen lh) {
        if (lh == null) {
            btnCapNhat.setDisable(true);
            btnXacNhan.setDisable(true);
            btnHoanThanh.setDisable(true);
            btnHuyLich.setDisable(true);
            return;
        }
        boolean daKetThuc = "Hoàn thành".equalsIgnoreCase(lh.getTrangThai()) || "Đã hủy".equalsIgnoreCase(lh.getTrangThai());
        btnCapNhat.setDisable(daKetThuc);
        btnXacNhan.setDisable(!"Đã đặt".equalsIgnoreCase(lh.getTrangThai()));
        btnHoanThanh.setDisable(!"Đã xác nhận".equalsIgnoreCase(lh.getTrangThai()) && !"Đang thực hiện".equalsIgnoreCase(lh.getTrangThai()));
        btnHuyLich.setDisable(daKetThuc);
    }

    // Chuyển đổi trạng thái lịch hẹn (Xác nhận, Hoàn thành)
    private void xuLyChuyenTrangThai(String trangThaiMoi, String loiNhac) {
        LichHen chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một lịch hẹn trong bảng!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, loiNhac, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận trạng thái");
        alert.setHeaderText("Lịch hẹn: " + chon.getMaLichHen() + " - Khách hàng: " + chon.getTenKhachHang());

        Optional<ButtonType> ketQua = alert.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.YES) {
            try {
                controller.capNhatTrangThai(chon.getMaLichHen(), trangThaiMoi);
                taiLaiDanhSach();
            } catch (Exception ex) {
                hienThiLoi("Lỗi cập nhật trạng thái: " + ex.getMessage());
            }
        }
    }

    // Hủy lịch hẹn có nhập lý do
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
                taiLaiDanhSach();
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi hủy lịch hẹn: " + ex.getMessage());
            }
        }
    }

    // Tải lại toàn bộ dữ liệu lịch hẹn từ CSDL
    public void taiLaiDanhSach() {
        try {
            List<LichHen> danhSach = controller.layDanhSachLichHen();
            danhSachHienThi.setAll(danhSach);
            tblDanhSach.getSelectionModel().clearSelection();
            capNhatTrangThaiNut(null);
        } catch (Exception e) {
            hienThiLoi("Lỗi khi tải danh sách lịch hẹn: " + e.getMessage());
        }
    }

    // Mở popup Đặt lịch hẹn mới
    private void moCuaSoThemMoi() {
        Window owner = getScene() != null ? getScene().getWindow() : null;
        DatLichHenView.hienThi(owner, this::taiLaiDanhSach);
    }

    // Mở popup cập nhật đổi lịch cho dòng đang chọn
    private void moCuaSoCapNhat() {
        LichHen chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một lịch hẹn trong bảng để cập nhật!");
            return;
        }

        if ("Hoàn thành".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Lịch hẹn đã hoàn thành, không thể sửa đổi thông tin!");
            return;
        }
        if ("Đã hủy".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Lịch hẹn đã bị hủy, không thể sửa đổi thông tin!");
            return;
        }

        Window owner = getScene() != null ? getScene().getWindow() : null;
        moPopupCapNhat(owner, chon, this::taiLaiDanhSach);
    }

    // =========================================================================
    // CỬA SỔ POPUP CẬP NHẬT / ĐỔI LỊCH HẸN (APT_RESCHEDULE)
    // =========================================================================

    /**
     * Mở hộp thoại popup đổi lịch hẹn cho một đối tượng LichHen cụ thể.
     */
    public static void moPopupCapNhat(Window owner, LichHen lichHen, Runnable onThanhCong) {
        LichHenController controller = new LichHenController();

        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            cuaSo.initOwner(owner);
        }
        cuaSo.setTitle("Đổi lịch hẹn - " + lichHen.getMaLichHen());

        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // Tiêu đề popup
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT / ĐỔI LỊCH HẸN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblPhu = new Label("Mã lịch: " + lichHen.getMaLichHen() + "  |  Trạng thái hiện tại: " + lichHen.getTrangThai());
        lblPhu.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblPhu);

        // Khung thông tin lịch hẹn
        VBox pnlThongTin = new VBox(10);
        pnlThongTin.getStyleClass().add("hop-noi-dung");

        GridPane luoi = new GridPane();
        luoi.setHgap(12);
        luoi.setVgap(10);

        // Khách hàng & Thú cưng (chỉ xem, không đổi đối tượng đặt)
        TextField txtKhachHang = new TextField(
                (lichHen.getTenKhachHang() != null ? lichHen.getTenKhachHang() : lichHen.getMaKhachHang())
                        + (lichHen.getSoDienThoai() != null ? " - " + lichHen.getSoDienThoai() : "")
        );
        txtKhachHang.setEditable(false);
        txtKhachHang.setStyle("-fx-background-color: #f1f5f9;");

        TextField txtThuCung = new TextField(
                lichHen.getTenThuCung() != null ? lichHen.getTenThuCung() : lichHen.getMaThuCung()
        );
        txtThuCung.setEditable(false);
        txtThuCung.setStyle("-fx-background-color: #f1f5f9;");

        // Ngày hẹn mới (chặn ngày quá khứ)
        DatePicker dpNgayHen = new DatePicker(lichHen.getNgayHen());
        dpNgayHen.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8;");
                }
            }
        });

        // Giờ hẹn mới
        ComboBox<String> cboGioHen = new ComboBox<>();
        for (int h = 8; h <= 20; h++) {
            cboGioHen.getItems().add(String.format("%02d:00", h));
            if (h < 20) {
                cboGioHen.getItems().add(String.format("%02d:30", h));
            }
        }
        if (lichHen.getGioHen() != null) {
            cboGioHen.setValue(lichHen.getGioHen().format(DINH_DANG_GIO));
        }

        // Người chăm sóc phụ trách
        ComboBox<String[]> cboNhanVien = new ComboBox<>();
        List<String[]> dsNv = controller.layDanhSachNhanVienChamSoc();
        cboNhanVien.getItems().setAll(dsNv);
        cboNhanVien.setConverter(new StringConverter<>() {
            @Override
            public String toString(String[] nv) {
                if (nv == null) return "";
                return nv[1] + " (" + nv[2] + ")";
            }

            @Override
            public String[] fromString(String string) {
                return null;
            }
        });
        for (String[] nv : dsNv) {
            if (nv[0].equalsIgnoreCase(lichHen.getMaNhanVien())) {
                cboNhanVien.setValue(nv);
                break;
            }
        }

        // Ghi chú
        TextField txtGhiChu = new TextField(lichHen.getGhiChu() != null ? lichHen.getGhiChu() : "");
        txtGhiChu.setPromptText("Nhập ghi chú yêu cầu thêm nếu có...");

        luoi.add(new Label("Khách hàng:"), 0, 0);
        luoi.add(txtKhachHang, 1, 0);
        luoi.add(new Label("Thú cưng:"), 2, 0);
        luoi.add(txtThuCung, 3, 0);

        luoi.add(new Label("Ngày hẹn mới (*):"), 0, 1);
        luoi.add(dpNgayHen, 1, 1);
        luoi.add(new Label("Giờ hẹn mới (*):"), 2, 1);
        luoi.add(cboGioHen, 3, 1);

        luoi.add(new Label("Người chăm sóc (*):"), 0, 2);
        luoi.add(cboNhanVien, 1, 2);
        luoi.add(new Label("Ghi chú:"), 2, 2);
        luoi.add(txtGhiChu, 3, 2);

        pnlThongTin.getChildren().add(luoi);

        // Khung danh sách dịch vụ chăm sóc
        VBox pnlDichVu = new VBox(10);
        pnlDichVu.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlDichVu, Priority.ALWAYS);

        Label lblTieuDeDv = new Label("Danh sách dịch vụ chăm sóc");
        lblTieuDeDv.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Các điều khiển thêm dịch vụ vào lịch hẹn
        HBox thanhChonDv = new HBox(8);
        thanhChonDv.setAlignment(Pos.CENTER_LEFT);

        ComboBox<DichVuChamSoc> cboDichVu = new ComboBox<>();
        cboDichVu.setPromptText("Chọn dịch vụ chăm sóc...");
        cboDichVu.setPrefWidth(220);
        List<DichVuChamSoc> dsDichVu = controller.layDanhSachDichVu();
        cboDichVu.getItems().setAll(dsDichVu);
        cboDichVu.setConverter(new StringConverter<>() {
            @Override
            public String toString(DichVuChamSoc dv) {
                return dv != null ? dv.getTenDichVu() : "";
            }

            @Override
            public DichVuChamSoc fromString(String string) {
                return null;
            }
        });

        // Lấy cân nặng mặc định của thú cưng từ chi tiết hiện tại hoặc nhập mới
        double canNangBanDau = 3.0;
        if (lichHen.getDanhSachChiTiet() != null && !lichHen.getDanhSachChiTiet().isEmpty()) {
            ChiTietLichHen ctDau = lichHen.getDanhSachChiTiet().get(0);
            if (ctDau.getCanNangThuCung() != null) {
                canNangBanDau = ctDau.getCanNangThuCung().doubleValue();
            }
        }

        TextField txtCanNang = new TextField(String.valueOf(canNangBanDau));
        txtCanNang.setPromptText("Ước tính (kg)");
        txtCanNang.setPrefWidth(110);

        TextField txtDonGia = new TextField();
        txtDonGia.setPromptText("Đơn giá");
        txtDonGia.setEditable(false);
        txtDonGia.setPrefWidth(130);
        txtDonGia.setStyle("-fx-background-color: #f1f5f9; -fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        Button btnThemDv = new Button("Thêm dịch vụ");
        btnThemDv.getStyleClass().add("nut-phu");

        thanhChonDv.getChildren().addAll(
                new Label("Dịch vụ:"), cboDichVu,
                new Label("Cân nặng (kg, ước tính):"), txtCanNang,
                new Label("Đơn giá:"), txtDonGia,
                btnThemDv
        );

        // Bảng danh sách dịch vụ chi tiết của lịch hẹn
        TableView<ChiTietLichHen> tblChiTiet = new TableView<>();
        tblChiTiet.getStyleClass().add("bang-du-lieu");
        tblChiTiet.setPrefHeight(180);

        ObservableList<ChiTietLichHen> dsChiTiet = FXCollections.observableArrayList();
        if (lichHen.getDanhSachChiTiet() != null) {
            dsChiTiet.addAll(lichHen.getDanhSachChiTiet());
        } else {
            dsChiTiet.addAll(controller.layChiTietLichHen(lichHen.getMaLichHen()));
        }

        TableColumn<ChiTietLichHen, String> colTenDv = new TableColumn<>("Tên dịch vụ");
        colTenDv.setPrefWidth(220);
        colTenDv.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenDichVu()));

        TableColumn<ChiTietLichHen, String> colCanNang = new TableColumn<>("Cân nặng áp dụng");
        colCanNang.setPrefWidth(140);
        colCanNang.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCanNang.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCanNangThuCung() != null ? d.getValue().getCanNangThuCung() + " kg" : ""
        ));

        TableColumn<ChiTietLichHen, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setPrefWidth(140);
        colGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDonGia() != null ? DINH_DANG_TIEN.format(d.getValue().getDonGia()) : "0 đ"
        ));

        // Cột xóa dòng dịch vụ
        TableColumn<ChiTietLichHen, Void> colXoa = new TableColumn<>("Thao tác");
        colXoa.setPrefWidth(90);
        colXoa.setCellFactory(param -> new TableCell<>() {
            private final Button btnXoaDong = new Button("Xóa");
            {
                btnXoaDong.getStyleClass().add("nut-xoa");
                btnXoaDong.setOnAction(e -> {
                    ChiTietLichHen ct = getTableView().getItems().get(getIndex());
                    dsChiTiet.remove(ct);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoaDong);
            }
        });

        tblChiTiet.getColumns().addAll(List.of(colTenDv, colCanNang, colGia, colXoa));
        tblChiTiet.setItems(dsChiTiet);

        // Nhãn tổng tiền
        Label lblTongTien = new Label();
        lblTongTien.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #0369a1;");

        Runnable tinhTongTien = () -> {
            BigDecimal tong = BigDecimal.ZERO;
            for (ChiTietLichHen ct : dsChiTiet) {
                if (ct.getDonGia() != null) {
                    tong = tong.add(ct.getDonGia());
                }
            }
            lblTongTien.setText("Tổng tiền dự kiến: " + DINH_DANG_TIEN.format(tong));
        };
        tinhTongTien.run();
        dsChiTiet.addListener((javafx.collections.ListChangeListener<ChiTietLichHen>) c -> tinhTongTien.run());

        // Tự động tính đơn giá khi chọn dịch vụ hoặc đổi cân nặng (ước tính theo cân nặng hoặc mức chuẩn bậc Nhỏ)
        Runnable capNhatDonGia = () -> {
            DichVuChamSoc dv = cboDichVu.getValue();
            if (dv == null) {
                txtDonGia.clear();
                return;
            }
            double cn = 3.0;
            boolean laUocTinh = true;
            try {
                String cnStr = txtCanNang.getText() != null ? txtCanNang.getText().trim() : "";
                if (!cnStr.isEmpty()) {
                    double val = Double.parseDouble(cnStr);
                    if (val > 0) {
                        cn = val;
                        laUocTinh = false;
                    }
                }
            } catch (Exception ignored) {}
            double gia = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), cn);
            txtDonGia.setText(DINH_DANG_TIEN.format(gia) + (laUocTinh ? " (tạm tính)" : ""));
        };

        cboDichVu.setOnAction(e -> capNhatDonGia.run());
        txtCanNang.textProperty().addListener((obs, cu, moi) -> capNhatDonGia.run());

        btnThemDv.setOnAction(e -> {
            DichVuChamSoc dv = cboDichVu.getValue();
            if (dv == null) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng chọn dịch vụ chăm sóc trước khi thêm!").showAndWait();
                return;
            }
            double cn = 3.0;
            try {
                String cnStr = txtCanNang.getText() != null ? txtCanNang.getText().trim() : "";
                if (!cnStr.isEmpty()) {
                    double val = Double.parseDouble(cnStr);
                    if (val > 0) {
                        cn = val;
                    }
                }
            } catch (Exception ignored) {}

            double donGiaSo = controller.traGiaDichVuTheoCanNang(dv.getMaDichVu(), cn);
            ChiTietLichHen chiTietMoi = new ChiTietLichHen(
                    lichHen.getMaLichHen(),
                    dv.getMaDichVu(),
                    dv.getTenDichVu(),
                    BigDecimal.valueOf(cn),
                    BigDecimal.valueOf(donGiaSo)
            );
            dsChiTiet.add(chiTietMoi);
        });

        HBox pnlDuoiDv = new HBox(10);
        pnlDuoiDv.setAlignment(Pos.CENTER_RIGHT);
        pnlDuoiDv.getChildren().add(lblTongTien);

        pnlDichVu.getChildren().addAll(lblTieuDeDv, thanhChonDv, tblChiTiet, pnlDuoiDv);

        // Hàng nút Lưu và Thoát
        HBox pnlNutThaoTac = new HBox(10);
        pnlNutThaoTac.setAlignment(Pos.CENTER_RIGHT);

        Button btnLuu = new Button("Lưu thay đổi");
        btnLuu.getStyleClass().add("nut-chinh");

        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> cuaSo.close());

        btnLuu.setOnAction(e -> {
            try {
                LocalDate ngayMoi = dpNgayHen.getValue();
                if (ngayMoi == null) {
                    new Alert(Alert.AlertType.WARNING, "Vui lòng chọn ngày hẹn mới!").showAndWait();
                    return;
                }

                String gioMoiStr = cboGioHen.getValue();
                if (gioMoiStr == null || gioMoiStr.trim().isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Vui lòng chọn khung giờ hẹn mới!").showAndWait();
                    return;
                }
                LocalTime gioMoi = LocalTime.parse(gioMoiStr, DINH_DANG_GIO);

                String[] nvChon = cboNhanVien.getValue();
                if (nvChon == null) {
                    new Alert(Alert.AlertType.WARNING, "Vui lòng chọn người chăm sóc phụ trách!").showAndWait();
                    return;
                }

                if (dsChiTiet.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Lịch hẹn phải có ít nhất một dịch vụ chăm sóc!").showAndWait();
                    return;
                }

                // Gán dữ liệu vào đối tượng LichHen
                lichHen.setNgayHen(ngayMoi);
                lichHen.setGioHen(gioMoi);
                lichHen.setMaNhanVien(nvChon[0]);
                lichHen.setGhiChu(txtGhiChu.getText() != null ? txtGhiChu.getText().trim() : "");

                // Gọi cập nhật qua Controller
                controller.capNhatLichHen(lichHen, new ArrayList<>(dsChiTiet));

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đổi lịch hẹn thành công!");
                alert.setTitle("Thông báo");
                alert.setHeaderText(null);
                alert.showAndWait();

                cuaSo.close();
                if (onThanhCong != null) {
                    onThanhCong.run();
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        pnlNutThaoTac.getChildren().addAll(btnLuu, btnHuy);

        pnlGoc.getChildren().addAll(pnlTieuDe, pnlThongTin, pnlDichVu, pnlNutThaoTac);

        Scene scene = new Scene(pnlGoc, 780, 620);
        if (CapNhatLichHenView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(CapNhatLichHenView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        cuaSo.setScene(scene);
        cuaSo.showAndWait();
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

