package com.petshop.view.phieudichvu;

import com.petshop.controller.PhieuDichVuController;
import com.petshop.model.ChiTietPhieuDichVu;
import com.petshop.model.DichVuChamSoc;
import com.petshop.model.PhieuDichVu;
import com.petshop.model.ThuCung;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Giao diện Cập nhật chi tiết và theo dõi tiến độ phiếu dịch vụ tại quầy (PDV_CAP_NHAT).
 * Hỗ trợ chuyển trạng thái tiến độ: Đang chờ -> Đang thực hiện -> Hoàn thành hoặc Hủy phiếu.
 */
public class CapNhatPhieuDichVuView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0 'đ'");
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final PhieuDichVuController controller = new PhieuDichVuController();
    private final ObservableList<PhieuDichVu> danhSachHienThi = FXCollections.observableArrayList();
    private List<PhieuDichVu> dsGoc = new ArrayList<>();

    // Các điều khiển thanh tác vụ
    private final ComboBox<String> cboLocTrangThai = new ComboBox<>();
    private final Button btnChuyenTienDo = new Button("Chuyển tiến độ");
    private final Button btnSuaChiTiet = new Button("Sửa chi tiết phiếu");
    private final Button btnHuyPhieu = new Button("Hủy phiếu");
    private final Button btnLamMoi = new Button("Làm mới");

    // Bảng danh sách phiếu dịch vụ
    private final TableView<PhieuDichVu> tblDanhSach = new TableView<>();

    public CapNhatPhieuDichVuView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề phân hệ
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT PHIẾU DỊCH VỤ & TIẾN ĐỘ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Theo dõi tiến độ thực hiện chăm sóc thú cưng: cập nhật dịch vụ, chuyển trạng thái hoặc hủy phiếu.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Bảng dữ liệu và thanh tác vụ
        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDe, pnlBang);

        capNhatTrangThaiNut(null);
        taiLaiDanhSach();
    }

    // Tạo khung chứa bảng danh sách và thanh nút thao tác
    private VBox taoKhungBangDanhSach() {
        VBox khung = new VBox(10);
        khung.setStyle("-fx-background-color: #ffffff; -fx-padding: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 4px; -fx-background-radius: 4px;");

        // Thanh công cụ trên bảng
        HBox thanhNut = new HBox(10);
        thanhNut.setAlignment(Pos.CENTER_LEFT);

        Label lblLoc = new Label("Lọc trạng thái:");
        cboLocTrangThai.getItems().addAll("Tất cả", "Đang chờ", "Đang thực hiện", "Hoàn thành", "Đã hủy");
        cboLocTrangThai.setValue("Tất cả");
        cboLocTrangThai.setOnAction(e -> apDungBoLoc());

        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        btnChuyenTienDo.getStyleClass().add("nut-chinh");
        btnChuyenTienDo.setOnAction(e -> xuLyChuyenTienDo());

        btnSuaChiTiet.getStyleClass().add("nut-phu");
        btnSuaChiTiet.setOnAction(e -> moCuaSoCapNhat());

        btnHuyPhieu.getStyleClass().add("nut-xoa");
        btnHuyPhieu.setOnAction(e -> xuLyHuyPhieu());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiLaiDanhSach());

        thanhNut.getChildren().addAll(lblLoc, cboLocTrangThai, khoangTrong, btnChuyenTienDo, btnSuaChiTiet, btnHuyPhieu, btnLamMoi);

        // Cấu hình các cột bảng hiển thị
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có phiếu dịch vụ nào"));
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

        TableColumn<PhieuDichVu, String> colTrangThai = new TableColumn<>("Tiến độ");
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setPrefWidth(120);
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));

        TableColumn<PhieuDichVu, String> colGhiChu = new TableColumn<>("Ghi chú");
        colGhiChu.setPrefWidth(160);
        colGhiChu.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getGhiChu() != null ? d.getValue().getGhiChu() : ""));

        tblDanhSach.getColumns().addAll(colMa, colKhach, colNgay, colGio, colNv, colDv, colTongTien, colTrangThai, colGhiChu);

        // Định dạng làm mờ các dòng đã hoàn thành hoặc đã hủy
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

    // Cập nhật trạng thái các nút bấm theo dòng phiếu đang chọn
    private void capNhatTrangThaiNut(PhieuDichVu pdv) {
        if (pdv == null) {
            btnChuyenTienDo.setDisable(true);
            btnChuyenTienDo.setText("Chuyển tiến độ");
            btnSuaChiTiet.setDisable(true);
            btnHuyPhieu.setDisable(true);
            return;
        }

        String trangThai = pdv.getTrangThai();
        boolean daKetThuc = "Hoàn thành".equalsIgnoreCase(trangThai) || "Đã hủy".equalsIgnoreCase(trangThai);

        btnSuaChiTiet.setDisable(daKetThuc);
        btnHuyPhieu.setDisable(daKetThuc);

        if ("Đang chờ".equalsIgnoreCase(trangThai)) {
            btnChuyenTienDo.setDisable(false);
            btnChuyenTienDo.setText("▶ Bắt đầu (Đang thực hiện)");
        } else if ("Đang thực hiện".equalsIgnoreCase(trangThai)) {
            btnChuyenTienDo.setDisable(false);
            btnChuyenTienDo.setText("✔ Hoàn thành dịch vụ");
        } else {
            btnChuyenTienDo.setDisable(true);
            btnChuyenTienDo.setText("Đã chốt tiến độ");
        }
    }

    // Xử lý chuyển bước trạng thái tiếp theo cho phiếu
    private void xuLyChuyenTienDo() {
        PhieuDichVu chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) return;

        String trangThaiMoi = null;
        if ("Đang chờ".equalsIgnoreCase(chon.getTrangThai())) {
            trangThaiMoi = "Đang thực hiện";
        } else if ("Đang thực hiện".equalsIgnoreCase(chon.getTrangThai())) {
            trangThaiMoi = "Hoàn thành";
        }

        if (trangThaiMoi == null) return;

        Alert xacNhan = new Alert(Alert.AlertType.CONFIRMATION);
        xacNhan.setTitle("Xác nhận chuyển tiến độ");
        xacNhan.setHeaderText("Chuyển trạng thái phiếu " + chon.getMaPhieuDichVu() + " sang '" + trangThaiMoi + "'?");
        xacNhan.setContentText("Hành động này sẽ cập nhật tiến độ thực hiện dịch vụ cho thú cưng.");

        Optional<ButtonType> ketQua = xacNhan.showAndWait();
        if (ketQua.isPresent() && ketQua.get() == ButtonType.OK) {
            try {
                controller.capNhatTrangThai(chon.getMaPhieuDichVu(), trangThaiMoi);
                hienThiThongBao("Đã cập nhật trạng thái phiếu thành: " + trangThaiMoi);
                taiLaiDanhSach();
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi chuyển trạng thái: " + ex.getMessage());
            }
        }
    }

    // Xử lý hủy phiếu tiếp nhận dịch vụ
    private void xuLyHuyPhieu() {
        PhieuDichVu chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một phiếu dịch vụ cần hủy!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Xác nhận hủy phiếu dịch vụ");
        dialog.setHeaderText("Hủy phiếu tiếp nhận: " + chon.getMaPhieuDichVu() + "\nKhách hàng: " + chon.getTenKhachHang());
        dialog.setContentText("Nhập lý do hủy phiếu (*):");

        Optional<String> ketQua = dialog.showAndWait();
        if (ketQua.isPresent()) {
            String lyDo = ketQua.get().trim();
            if (lyDo.isEmpty()) {
                hienThiCanhBao("Vui lòng nhập lý do hủy phiếu dịch vụ!");
                return;
            }
            try {
                controller.huyPhieuDichVu(chon.getMaPhieuDichVu(), lyDo);
                hienThiThongBao("Đã hủy phiếu dịch vụ thành công!");
                taiLaiDanhSach();
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi hủy phiếu: " + ex.getMessage());
            }
        }
    }

    // Mở cửa sổ modal cập nhật chi tiết dòng dịch vụ
    private void moCuaSoCapNhat() {
        PhieuDichVu chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiCanhBao("Vui lòng chọn một phiếu trong bảng để cập nhật!");
            return;
        }
        if ("Hoàn thành".equalsIgnoreCase(chon.getTrangThai()) || "Đã hủy".equalsIgnoreCase(chon.getTrangThai())) {
            hienThiCanhBao("Không thể chỉnh sửa phiếu dịch vụ đã hoàn thành hoặc đã bị hủy!");
            return;
        }

        Window owner = getScene() != null ? getScene().getWindow() : null;
        moPopupCapNhatChiTiet(owner, chon, this::taiLaiDanhSach);
    }

    // -------------------------------------------------------------------------
    // POPUP CẬP NHẬT CHI TIẾT PHIẾU DỊCH VỤ
    // -------------------------------------------------------------------------

    public static void moPopupCapNhatChiTiet(Window owner, PhieuDichVu phieuGoc, Runnable onThanhCong) {
        PhieuDichVuController ctrl = new PhieuDichVuController();
        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) cuaSo.initOwner(owner);
        cuaSo.setTitle("Cập nhật phiếu dịch vụ - " + phieuGoc.getMaPhieuDichVu());

        VBox pnlGoc = new VBox(12);
        pnlGoc.setPadding(new Insets(16));
        pnlGoc.setStyle("-fx-background-color: #f4f6f8;");

        // 1. Tiêu đề
        Label lblTieuDe = new Label("CẬP NHẬT CHI TIẾT PHIẾU DỊCH VỤ");
        lblTieuDe.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        Label lblPhu = new Label("Mã phiếu: " + phieuGoc.getMaPhieuDichVu() + "  |  Trạng thái: " + phieuGoc.getTrangThai());
        lblPhu.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        // 2. Thông tin chung
        GridPane luoi = new GridPane();
        luoi.setHgap(10);
        luoi.setVgap(8);
        luoi.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #cbd5e1; -fx-border-radius: 4px;");

        TextField txtKhach = new TextField(phieuGoc.getTenKhachHang() != null ? phieuGoc.getTenKhachHang() : phieuGoc.getMaKhachHang());
        txtKhach.setEditable(false);

        ComboBox<String[]> cboNv = new ComboBox<>();
        List<String[]> dsNv = ctrl.layDanhSachNhanVien();
        cboNv.getItems().setAll(dsNv);
        cboNv.setConverter(taoConverter(nv -> nv[1] + " (" + nv[2] + ")"));
        dsNv.stream().filter(n -> n[0].equalsIgnoreCase(phieuGoc.getMaNhanVien())).findFirst().ifPresent(cboNv::setValue);

        TextField txtGhiChu = new TextField(phieuGoc.getGhiChu() != null ? phieuGoc.getGhiChu() : "");
        txtGhiChu.setPromptText("Ghi chú bổ sung...");

        luoi.add(new Label("Khách hàng:"), 0, 0); luoi.add(txtKhach, 1, 0);
        luoi.add(new Label("Nhân viên (*):"), 2, 0); luoi.add(cboNv, 3, 0);
        luoi.add(new Label("Ghi chú:"), 0, 1); luoi.add(txtGhiChu, 1, 1, 3, 1);

        // 3. Khung thêm bớt dịch vụ
        VBox pnlDichVu = new VBox(8);
        pnlDichVu.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #cbd5e1; -fx-border-radius: 4px;");
        VBox.setVgrow(pnlDichVu, Priority.ALWAYS);

        ComboBox<ThuCung> cboTc = new ComboBox<>();
        cboTc.setPromptText("Chọn thú cưng...");
        List<ThuCung> dsThuCung = ctrl.layDanhSachThuCungTheoKhachHang(phieuGoc.getMaKhachHang());
        cboTc.getItems().setAll(dsThuCung);
        cboTc.setConverter(taoConverter(t -> t.getTenThuCung() + " (" + (t.getCanNang() != null ? t.getCanNang() + "kg" : "") + ")"));
        if (!dsThuCung.isEmpty()) cboTc.setValue(dsThuCung.get(0));

        ComboBox<DichVuChamSoc> cboDv = new ComboBox<>();
        cboDv.setPromptText("Chọn dịch vụ chăm sóc...");
        List<DichVuChamSoc> dsDv = ctrl.layDanhSachDichVu();
        cboDv.getItems().setAll(dsDv);
        cboDv.setConverter(taoConverter(DichVuChamSoc::getTenDichVu));

        TextField txtGia = new TextField();
        txtGia.setPromptText("Đơn giá");
        txtGia.setPrefWidth(120);

        // Tự động tính giá khi chọn thú cưng và dịch vụ
        Runnable tinhGia = () -> {
            ThuCung tc = cboTc.getValue();
            DichVuChamSoc dv = cboDv.getValue();
            if (tc != null && dv != null) {
                double can = tc.getCanNang() != null ? tc.getCanNang().doubleValue() : 3.0;
                double gia = ctrl.traGiaDichVuTheoCanNang(dv.getMaDichVu(), can);
                txtGia.setText(String.valueOf((long) gia));
            }
        };
        cboTc.setOnAction(e -> tinhGia.run());
        cboDv.setOnAction(e -> tinhGia.run());

        Button btnThem = new Button("+ Thêm dòng");
        btnThem.getStyleClass().add("nut-chinh");

        HBox pnlThem = new HBox(8, new Label("Bé:"), cboTc, new Label("Dịch vụ:"), cboDv, new Label("Giá:"), txtGia, btnThem);
        pnlThem.setAlignment(Pos.CENTER_LEFT);

        // Bảng chi tiết
        ObservableList<ChiTietPhieuDichVu> dsChiTiet = FXCollections.observableArrayList();
        if (phieuGoc.getDanhSachChiTiet() != null) {
            dsChiTiet.addAll(phieuGoc.getDanhSachChiTiet());
        } else {
            dsChiTiet.addAll(ctrl.layChiTietPhieuDichVu(phieuGoc.getMaPhieuDichVu()));
        }

        Label lblTongTienPopup = new Label();
        lblTongTienPopup.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
        Runnable capNhatTong = () -> {
            BigDecimal tong = BigDecimal.ZERO;
            for (ChiTietPhieuDichVu ct : dsChiTiet) {
                if (ct.getDonGia() != null) tong = tong.add(ct.getDonGia());
            }
            lblTongTienPopup.setText("Tổng tiền: " + DINH_DANG_TIEN.format(tong));
        };
        capNhatTong.run();

        TableView<ChiTietPhieuDichVu> tblChiTiet = new TableView<>(dsChiTiet);
        tblChiTiet.getStyleClass().add("bang-du-lieu");
        tblChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblChiTiet.setPrefHeight(180);
        VBox.setVgrow(tblChiTiet, Priority.ALWAYS);

        TableColumn<ChiTietPhieuDichVu, String> colTc = new TableColumn<>("Thú cưng");
        colTc.setPrefWidth(160);
        colTc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenThuCung()));

        TableColumn<ChiTietPhieuDichVu, String> colDv = new TableColumn<>("Dịch vụ chăm sóc");
        colDv.setPrefWidth(220);
        colDv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenDichVu()));

        TableColumn<ChiTietPhieuDichVu, String> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colDonGia.setPrefWidth(120);
        colDonGia.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDonGia() != null ? DINH_DANG_TIEN.format(c.getValue().getDonGia()) : "0 đ"));

        TableColumn<ChiTietPhieuDichVu, Void> colXoa = new TableColumn<>("Thao tác");
        colXoa.setStyle("-fx-alignment: CENTER;");
        colXoa.setPrefWidth(80);
        colXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Xóa");
            {
                btn.getStyleClass().add("nut-xoa");
                btn.setOnAction(e -> {
                    dsChiTiet.remove(getTableView().getItems().get(getIndex()));
                    capNhatTong.run();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tblChiTiet.getColumns().addAll(colTc, colDv, colDonGia, colXoa);

        btnThem.setOnAction(e -> {
            ThuCung tc = cboTc.getValue();
            DichVuChamSoc dv = cboDv.getValue();
            if (tc == null || dv == null) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng chọn thú cưng và dịch vụ!").showAndWait();
                return;
            }
            for (ChiTietPhieuDichVu ct : dsChiTiet) {
                if (ct.getMaThuCung().equals(tc.getMaThuCung()) && ct.getMaDichVu().equals(dv.getMaDichVu())) {
                    new Alert(Alert.AlertType.WARNING, "Dịch vụ này đã có trong phiếu cho bé " + tc.getTenThuCung() + "!").showAndWait();
                    return;
                }
            }
            BigDecimal gia;
            try {
                gia = new BigDecimal(txtGia.getText().trim());
            } catch (Exception ex) {
                gia = BigDecimal.ZERO;
            }
            ChiTietPhieuDichVu dongMoi = new ChiTietPhieuDichVu(
                    phieuGoc.getMaPhieuDichVu(),
                    tc.getMaThuCung(), tc.getTenThuCung(),
                    dv.getMaDichVu(), dv.getTenDichVu(),
                    gia
            );
            dsChiTiet.add(dongMoi);
            capNhatTong.run();
        });

        pnlDichVu.getChildren().addAll(pnlThem, tblChiTiet, lblTongTienPopup);

        // 4. Thanh nút bấm xác nhận
        Button btnLuu = new Button("✔ Lưu thay đổi");
        btnLuu.getStyleClass().add("nut-chinh");
        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> cuaSo.close());

        btnLuu.setOnAction(e -> {
            String[] nv = cboNv.getValue();
            if (nv == null) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng chọn nhân viên phụ trách!").showAndWait();
                return;
            }
            if (dsChiTiet.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Phiếu phải có ít nhất một dịch vụ chăm sóc!").showAndWait();
                return;
            }

            phieuGoc.setMaNhanVien(nv[0]);
            phieuGoc.setGhiChu(txtGhiChu.getText().trim());

            try {
                boolean ok = ctrl.capNhatPhieuDichVu(phieuGoc, new ArrayList<>(dsChiTiet));
                if (ok) {
                    new Alert(Alert.AlertType.INFORMATION, "Cập nhật phiếu dịch vụ thành công!").showAndWait();
                    if (onThanhCong != null) onThanhCong.run();
                    cuaSo.close();
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Lỗi khi cập nhật phiếu: " + ex.getMessage()).showAndWait();
            }
        });

        HBox pnlNut = new HBox(10, btnDong, btnLuu);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        pnlGoc.getChildren().addAll(lblTieuDe, lblPhu, luoi, pnlDichVu, pnlNut);

        Scene scene = new Scene(pnlGoc, 760, 560);
        try {
            scene.getStylesheets().add(CapNhatPhieuDichVuView.class.getResource(CSS_CHUNG).toExternalForm());
        } catch (Exception ignored) {}
        cuaSo.setScene(scene);
        cuaSo.showAndWait();
    }

    // -------------------------------------------------------------------------
    // HÀM TIỆN ÍCH HỖ TRỢ
    // -------------------------------------------------------------------------

    private static <T> StringConverter<T> taoConverter(Function<T, String> hienThi) {
        return new StringConverter<>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : hienThi.apply(object);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }

    private void apDungBoLoc() {
        String trangThai = cboLocTrangThai.getValue();
        if (trangThai == null || "Tất cả".equalsIgnoreCase(trangThai)) {
            danhSachHienThi.setAll(dsGoc);
        } else {
            danhSachHienThi.setAll(dsGoc.stream().filter(p -> trangThai.equalsIgnoreCase(p.getTrangThai())).toList());
        }
    }

    public void taiLaiDanhSach() {
        try {
            dsGoc = controller.layDanhSachPhieuDichVu();
            apDungBoLoc();
            tblDanhSach.getSelectionModel().clearSelection();
            capNhatTrangThaiNut(null);
        } catch (Exception e) {
            hienThiLoi("Lỗi khi tải danh sách phiếu dịch vụ: " + e.getMessage());
        }
    }

    private void hienThiCanhBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiThongBao(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
