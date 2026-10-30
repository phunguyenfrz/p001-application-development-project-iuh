package com.petshop.view.nhanvien;

import com.petshop.controller.NhanVienController;
import com.petshop.model.NhanVien;
import com.petshop.model.TaiKhoan;
import com.petshop.service.DanhMucMenu;
import com.petshop.service.PhanQuyenService;
import com.petshop.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.*;

/**
 * Màn hình Phân quyền nhân viên (NV_PHAN_QUYEN).
 * Đáp ứng đầy đủ các tiêu chuẩn nghiệp vụ và giao diện:
 * 1. Bảng danh sách nhân viên kèm trạng thái tài khoản, vai trò và ghi chú an toàn (admin, chính mình, nhân viên đã nghỉ).
 * 2. Hộp thoại Chỉnh quyền trực quan (Modal):
 *    - Xem vai trò hiện tại và chọn vai trò mới (Quản lý, Bán hàng, Chăm sóc).
 *    - Danh sách nhóm chức năng dạng thẻ module có tỷ lệ quyền (x/y), [Chọn toàn bộ], [Bỏ toàn bộ].
 *    - Tìm kiếm chức năng nhanh thời gian thực.
 *    - Nút "Khôi phục quyền theo vai trò" để đồng bộ ngay lập tức.
 *    - Lưu quyền trực tiếp vào CSDL và hiển thị thông báo thành công.
 */
public class PhanQuyenNhanVienView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";

    private final NhanVienController controller = new NhanVienController();
    private final ObservableList<NhanVien> dsNhanVienGoc = FXCollections.observableArrayList();
    private FilteredList<NhanVien> dsNhanVienLoc;

    // Thanh công cụ
    private final TextField txtTimKiem = new TextField();
    private final Button btnTimKiem = new Button("Tìm kiếm");
    private final Button btnLamMoi = new Button("Làm mới");
    private final Button btnChinhQuyen = new Button("Chỉnh quyền");
    private final Label lblDaChon = new Label();
    private final Label lblTongSo = new Label();

    // Hộp thông báo cập nhật thành công (Toast)
    private final HBox pnlThongBaoToast = new HBox(10);
    private final Label lblNoiDungToast = new Label();

    // Bảng dữ liệu
    private final TableView<NhanVien> tblDanhSach = new TableView<>();

    public PhanQuyenNhanVienView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình kèm Toast thông báo góc phải
        HBox pnlDauTrang = taoDauTrang();

        // 2. Khung nội dung chính
        VBox pnlKhungChinh = new VBox(12);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlThanhTacVu = taoThanhTacVu();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlThanhTacVu, pnlBang);
        getChildren().addAll(pnlDauTrang, pnlKhungChinh);

        // Khởi tạo trạng thái nút
        capNhatTrangThaiNut(null);

        // Nạp dữ liệu ban đầu
        taiLaiBang();
    }

    private HBox taoDauTrang() {
        HBox pnlDau = new HBox(12);
        pnlDau.setAlignment(Pos.CENTER_LEFT);

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("PHÂN QUYỀN NHÂN VIÊN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Chọn nhân viên để thiết lập các chức năng và quyền hạn được phép sử dụng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Toast thông báo nhỏ xinh góc phải
        pnlThongBaoToast.setAlignment(Pos.CENTER_LEFT);
        pnlThongBaoToast.setPadding(new Insets(6, 12, 6, 12));
        pnlThongBaoToast.setStyle("-fx-background-color: #ecfdf5; -fx-border-color: #a7f3d0; -fx-border-radius: 20px; -fx-background-radius: 20px;");
        pnlThongBaoToast.setVisible(false);
        pnlThongBaoToast.setManaged(false);

        lblNoiDungToast.setStyle("-fx-font-weight: bold; -fx-text-fill: #065f46; -fx-font-size: 12px;");
        Button btnDongToast = new Button("✕");
        btnDongToast.setStyle("-fx-background-color: transparent; -fx-text-fill: #065f46; -fx-cursor: hand; -fx-padding: 0; -fx-font-weight: bold;");
        btnDongToast.setOnAction(e -> anToast());
        pnlThongBaoToast.getChildren().addAll(lblNoiDungToast, btnDongToast);

        pnlDau.getChildren().addAll(pnlTieuDe, spacer, pnlThongBaoToast);
        return pnlDau;
    }

    private void hienThiToast(String tinNhan) {
        lblNoiDungToast.setText("✔  " + tinNhan);
        pnlThongBaoToast.setVisible(true);
        pnlThongBaoToast.setManaged(true);
    }

    private void anToast() {
        pnlThongBaoToast.setVisible(false);
        pnlThongBaoToast.setManaged(false);
    }

    private HBox taoThanhTacVu() {
        HBox pnlThanh = new HBox(12);
        pnlThanh.getStyleClass().add("thanh-tac-vu");
        pnlThanh.setAlignment(Pos.CENTER_LEFT);
        pnlThanh.setMaxWidth(Double.MAX_VALUE);

        // Ô tìm kiếm
        txtTimKiem.setPromptText("Tìm theo mã nhân viên, họ tên hoặc số điện thoại...");
        txtTimKiem.setPrefWidth(350);
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> locDuLieu());
        txtTimKiem.setOnAction(e -> locDuLieu());

        btnTimKiem.getStyleClass().add("nut-phu");
        btnTimKiem.setOnAction(e -> locDuLieu());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> {
            txtTimKiem.clear();
            taiLaiBang();
            anToast();
        });

        lblDaChon.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Nút Chỉnh quyền
        btnChinhQuyen.getStyleClass().add("nut-chinh");
        btnChinhQuyen.setOnAction(e -> moCuaSoChinhQuyen());

        pnlThanh.getChildren().addAll(txtTimKiem, btnTimKiem, btnLamMoi, lblDaChon, spacer, btnChinhQuyen);
        return pnlThanh;
    }

    @SuppressWarnings("unchecked")
    private VBox taoKhungBang() {
        VBox pnlBang = new VBox(8);
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        tblDanhSach.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        // 1. Cột Mã nhân viên
        TableColumn<NhanVien, String> colMaNv = new TableColumn<>("Mã nhân viên");
        colMaNv.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaNhanVien()));
        colMaNv.setPrefWidth(150);
        colMaNv.setStyle("-fx-alignment: CENTER;");

        // 2. Cột Họ tên (hiển thị văn bản thường, không dùng avatar tròn)
        TableColumn<NhanVien, String> colHoTen = new TableColumn<>("Họ tên");
        colHoTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getHoTen()));
        colHoTen.setPrefWidth(180);

        // 3. Cột Số điện thoại
        TableColumn<NhanVien, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSoDienThoai()));
        colSdt.setPrefWidth(120);
        colSdt.setStyle("-fx-alignment: CENTER;");

        // 4. Cột Tài khoản đăng nhập (kèm ghi chú an toàn)
        TableColumn<NhanVien, NhanVien> colTaiKhoan = new TableColumn<>("Tài khoản đăng nhập");
        colTaiKhoan.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue()));
        colTaiKhoan.setPrefWidth(260);
        colTaiKhoan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(NhanVien nv, boolean empty) {
                super.updateItem(nv, empty);
                if (empty || nv == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox box = new VBox(2);
                    box.setAlignment(Pos.CENTER_LEFT);

                    if (nv.getTenDangNhap() != null && !nv.getTenDangNhap().isBlank()) {
                        String user = nv.getTenDangNhap();
                        if ("admin".equalsIgnoreCase(user)) {
                            HBox h = new HBox(8);
                            h.setAlignment(Pos.CENTER_LEFT);
                            Label lblU = new Label("admin - Admin");
                            lblU.setStyle("-fx-text-fill: #1e293b;");
                            Label badge = new Label("Toàn quyền");
                            badge.setPadding(new Insets(2, 6, 2, 6));
                            badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-background-radius: 4px; -fx-font-size: 10px;");
                            h.getChildren().addAll(lblU, badge);

                            Label note = new Label("🔒 Toàn quyền · Không thể chỉnh tài khoản Admin");
                            note.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #94a3b8;");
                            box.getChildren().addAll(h, note);
                        } else {
                            boolean laChinhMinh = false;
                            try {
                                var cur = SessionManager.getTaiKhoanHienTai();
                                if (cur != null && nv.getMaNhanVien().equalsIgnoreCase(cur.getMaNhanVien())) {
                                    laChinhMinh = true;
                                }
                            } catch (Exception ignored) {}

                            if (laChinhMinh) {
                                Label lblU = new Label(user + " · Đang đăng nhập");
                                lblU.setStyle("-fx-text-fill: #0369a1;");
                                Label note = new Label("🔒 Không thể chỉnh quyền của chính mình");
                                note.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #94a3b8;");
                                box.getChildren().addAll(lblU, note);
                            } else if (!nv.isDangLam()) {
                                Label lblU = new Label(user);
                                lblU.setStyle("-fx-text-fill: #64748b;");
                                Label note = new Label("🔒 Không thể chỉnh quyền nhân viên đã nghỉ");
                                note.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #94a3b8;");
                                box.getChildren().addAll(lblU, note);
                            } else {
                                Label lblU = new Label(user);
                                lblU.setStyle("-fx-text-fill: #0f172a;");
                                box.getChildren().add(lblU);
                            }
                        }
                    } else {
                        Label lblChua = new Label("Chưa có tài khoản");
                        lblChua.setStyle("-fx-font-style: italic; -fx-text-fill: #94a3b8;");
                        Label note = new Label("⚠ Cần tạo tài khoản trước khi phân quyền");
                        note.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #d97706;");
                        box.getChildren().addAll(lblChua, note);
                    }

                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // 5. Cột Vai trò - hiển thị văn bản thường căn giữa, không dùng badge màu
        TableColumn<NhanVien, String> colVaiTro = new TableColumn<>("Vai trò");
        colVaiTro.setCellValueFactory(cell -> {
            String role = cell.getValue().getPhanQuyen();
            if (role == null || role.isBlank()) {
                role = cell.getValue().getChucVu();
            }
            return new SimpleStringProperty(role);
        });
        colVaiTro.setPrefWidth(120);
        colVaiTro.setStyle("-fx-alignment: CENTER;");

        // 6. Cột Trạng thái (chữ thường thẳng hàng, không dùng badge màu)
        TableColumn<NhanVien, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colTrangThai.setPrefWidth(120);
        colTrangThai.setStyle("-fx-alignment: CENTER;");

        tblDanhSach.getColumns().addAll(colMaNv, colHoTen, colSdt, colTaiKhoan, colVaiTro, colTrangThai);

        // Hiệu ứng mờ dòng nhân viên đã nghỉ việc + Double click mở popup Chỉnh quyền
        tblDanhSach.setRowFactory(tv -> {
            TableRow<NhanVien> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldVal, nv) -> {
                if (nv != null && !nv.isDangLam()) {
                    row.setOpacity(0.55);
                } else {
                    row.setOpacity(1.0);
                }
            });

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moCuaSoChinhQuyen();
                }
            });
            return row;
        });

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, nv) -> capNhatTrangThaiNut(nv));

        // Thanh trạng thái chân bảng
        HBox pnlChanBang = new HBox(12);
        pnlChanBang.setAlignment(Pos.CENTER_LEFT);
        pnlChanBang.setPadding(new Insets(4, 0, 0, 0));

        lblTongSo.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        Region spacerChan = new Region();
        HBox.setHgrow(spacerChan, Priority.ALWAYS);

        Label lblGhiChu = new Label("Chỉ chỉnh nhân viên đang làm có tài khoản; không chỉnh chính mình hoặc Admin.");
        lblGhiChu.setStyle("-fx-font-style: italic; -fx-text-fill: #64748b; -fx-font-size: 11.5px;");

        pnlChanBang.getChildren().addAll(lblTongSo, spacerChan, lblGhiChu);
        pnlBang.getChildren().addAll(tblDanhSach, pnlChanBang);
        return pnlBang;
    }

    private void capNhatTrangThaiNut(NhanVien nv) {
        if (nv == null) {
            lblDaChon.setText("");
            btnChinhQuyen.setDisable(true);
            return;
        }

        lblDaChon.setText("✔ Đã chọn: " + nv.getHoTen());

        boolean daCoTaiKhoan = (nv.getTenDangNhap() != null && !nv.getTenDangNhap().isBlank());
        boolean laAdmin = "admin".equalsIgnoreCase(nv.getTenDangNhap());
        boolean laChinhMinh = false;
        try {
            var cur = SessionManager.getTaiKhoanHienTai();
            if (cur != null && nv.getMaNhanVien().equalsIgnoreCase(cur.getMaNhanVien())) {
                laChinhMinh = true;
            }
        } catch (Exception ignored) {}

        // Chỉ cho phép chỉnh quyền khi nhân viên ĐANG LÀM, ĐÃ CÓ TÀI KHOẢN, KHÔNG PHẢI ADMIN, KHÔNG PHẢI CHÍNH MÌNH
        boolean choPhepChinh = nv.isDangLam() && daCoTaiKhoan && !laAdmin && !laChinhMinh;
        btnChinhQuyen.setDisable(!choPhepChinh);
    }

    public void taiLaiBang() {
        try {
            List<NhanVien> ds = controller.layDanhSachNhanVien();
            dsNhanVienGoc.setAll(ds);
            dsNhanVienLoc = new FilteredList<>(dsNhanVienGoc, p -> true);
            tblDanhSach.setItems(dsNhanVienLoc);
            locDuLieu();
        } catch (Exception e) {
            hienThiLoi("Lỗi tải danh sách", e.getMessage());
        }
    }

    private void locDuLieu() {
        if (dsNhanVienLoc == null) return;
        String tuKhoa = (txtTimKiem.getText() != null) ? txtTimKiem.getText().trim().toLowerCase() : "";
        dsNhanVienLoc.setPredicate(nv -> {
            if (tuKhoa.isEmpty()) return true;
            boolean m1 = nv.getMaNhanVien() != null && nv.getMaNhanVien().toLowerCase().contains(tuKhoa);
            boolean m2 = nv.getHoTen() != null && nv.getHoTen().toLowerCase().contains(tuKhoa);
            boolean m3 = nv.getTenDangNhap() != null && nv.getTenDangNhap().toLowerCase().contains(tuKhoa);
            boolean m4 = nv.getSoDienThoai() != null && nv.getSoDienThoai().contains(tuKhoa);
            return m1 || m2 || m3 || m4;
        });

        int hienThi = dsNhanVienLoc.size();
        int tongSo = dsNhanVienGoc.size();
        lblTongSo.setText("Hiển thị " + hienThi + " / " + tongSo + " nhân viên");
    }

    // =========================================================================
    // HỘP THOẠI POPUP CHỈNH QUYỀN NHÂN VIÊN (Image 2)
    // =========================================================================

    private void moCuaSoChinhQuyen() {
        NhanVien nv = tblDanhSach.getSelectionModel().getSelectedItem();
        if (nv == null) {
            hienThiCanhBao("Chưa chọn nhân viên", "Vui lòng chọn một nhân viên để chỉnh quyền!");
            return;
        }

        if (nv.getTenDangNhap() == null || nv.getTenDangNhap().isBlank()) {
            hienThiCanhBao("Chưa có tài khoản", "Nhân viên '" + nv.getHoTen() + "' chưa được cấp tài khoản!\nVui lòng vào mục 'Tài khoản đăng nhập' để cấp tài khoản trước khi phân quyền.");
            return;
        }

        if (!nv.isDangLam()) {
            hienThiCanhBao("Nhân viên đã nghỉ", "Không thể chỉnh quyền cho nhân viên đã nghỉ việc!");
            return;
        }

        if ("admin".equalsIgnoreCase(nv.getTenDangNhap())) {
            hienThiCanhBao("Tài khoản Quản trị", "Không thể chỉnh quyền tài khoản Admin!");
            return;
        }

        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        Window owner = getScene() != null ? getScene().getWindow() : null;
        if (owner != null) cuaSo.initOwner(owner);
        cuaSo.setTitle("Phân quyền nhân viên - " + nv.getHoTen());

        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // 1. Thẻ thông tin nhân viên + ComboBox chọn vai trò
        HBox pnlTheHeader = taoTheHeaderChinhQuyen(nv);

        // 2. Thanh hướng dẫn & Tìm kiếm chức năng
        HBox pnlThanhCongCu = new HBox(12);
        pnlThanhCongCu.setAlignment(Pos.CENTER_LEFT);

        VBox boxMoTa = new VBox(2);
        Label lblMoTa1 = new Label("Thiết lập chức năng nhân viên được phép xem và sử dụng");
        lblMoTa1.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 13px;");
        Label lblMoTa2 = new Label("Quyền được chọn là quyền hiệu lực cuối cùng của nhân viên này.");
        lblMoTa2.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11.5px;");
        boxMoTa.getChildren().addAll(lblMoTa1, lblMoTa2);

        Region spacerCc = new Region();
        HBox.setHgrow(spacerCc, Priority.ALWAYS);

        TextField txtTimChucNang = new TextField();
        txtTimChucNang.setPromptText("🔍 Tìm chức năng...");
        txtTimChucNang.setPrefWidth(240);

        pnlThanhCongCu.getChildren().addAll(boxMoTa, spacerCc, txtTimChucNang);

        // 3. Khung danh sách các phân hệ và CheckBox quyền
        Map<String, CheckBox> mapCheckBoxes = new LinkedHashMap<>();
        Map<String, List<CheckBox>> mapTheoNhom = new LinkedHashMap<>();
        Map<String, Label> mapNhanTyLe = new LinkedHashMap<>();

        VBox pnlDanhSachNhom = new VBox(10);
        List<DanhMucMenu.NhomMenu> tatCaNhom = DanhMucMenu.layTatCaMenu();

        String vaiTroBanDau = (nv.getPhanQuyen() != null && !nv.getPhanQuyen().isBlank()) ? nv.getPhanQuyen() : nv.getChucVu();
        ComboBox<String> cboVaiTro = (ComboBox<String>) pnlTheHeader.getUserData();

        for (DanhMucMenu.NhomMenu nhom : tatCaNhom) {
            VBox pnlNhomCard = new VBox(8);
            pnlNhomCard.setPadding(new Insets(10, 14, 10, 14));
            pnlNhomCard.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");

            // Header của nhóm (Tên nhóm, tỷ lệ x/y, Chọn tất cả, Bỏ tất cả)
            HBox pnlHeaderNhom = new HBox(10);
            pnlHeaderNhom.setAlignment(Pos.CENTER_LEFT);

            Label lblTenNhom = new Label(nhom.getTieuDe());
            lblTenNhom.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");

            Label lblTyLe = new Label("0/0");
            lblTyLe.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b; -fx-font-weight: bold;");
            mapNhanTyLe.put(nhom.getMaNhom(), lblTyLe);

            Region spNhom = new Region();
            HBox.setHgrow(spNhom, Priority.ALWAYS);

            Hyperlink lnkChonTatCa = new Hyperlink("Chọn toàn bộ");
            lnkChonTatCa.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #0284c7;");

            Hyperlink lnkBoTatCa = new Hyperlink("Bỏ toàn bộ");
            lnkBoTatCa.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");

            pnlHeaderNhom.getChildren().addAll(lblTenNhom, lblTyLe, spNhom, lnkChonTatCa, lnkBoTatCa);

            // FlowPane chứa các CheckBox chức năng
            FlowPane pnlFlowChucNang = new FlowPane();
            pnlFlowChucNang.setHgap(20);
            pnlFlowChucNang.setVgap(8);

            List<CheckBox> listCbNhom = new ArrayList<>();

            for (DanhMucMenu.ChucNangMenu cn : nhom.getDanhSachChucNang()) {
                CheckBox cb = new CheckBox(cn.getTenChucNang());
                cb.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #334155;");
                cb.setPrefWidth(220);
                mapCheckBoxes.put(cn.getMaChucNang(), cb);
                listCbNhom.add(cb);
                pnlFlowChucNang.getChildren().add(cb);
            }
            mapTheoNhom.put(nhom.getMaNhom(), listCbNhom);

            lnkChonTatCa.setOnAction(e -> {
                listCbNhom.forEach(cb -> cb.setSelected(true));
                capNhatThongKe(mapTheoNhom, mapNhanTyLe, pnlGoc);
            });

            lnkBoTatCa.setOnAction(e -> {
                listCbNhom.forEach(cb -> cb.setSelected(false));
                capNhatThongKe(mapTheoNhom, mapNhanTyLe, pnlGoc);
            });

            pnlNhomCard.getChildren().addAll(pnlHeaderNhom, pnlFlowChucNang);
            pnlDanhSachNhom.getChildren().add(pnlNhomCard);
        }

        // Lọc chức năng trong popup
        txtTimChucNang.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = (newVal != null) ? newVal.trim().toLowerCase() : "";
            for (int i = 0; i < tatCaNhom.size(); i++) {
                DanhMucMenu.NhomMenu nhom = tatCaNhom.get(i);
                VBox card = (VBox) pnlDanhSachNhom.getChildren().get(i);
                boolean coKhop = filter.isEmpty() || nhom.getTieuDe().toLowerCase().contains(filter);
                List<CheckBox> cbs = mapTheoNhom.get(nhom.getMaNhom());
                for (CheckBox cb : cbs) {
                    boolean match = filter.isEmpty() || cb.getText().toLowerCase().contains(filter);
                    cb.setVisible(match);
                    cb.setManaged(match);
                    if (match) coKhop = true;
                }
                card.setVisible(coKhop);
                card.setManaged(coKhop);
            }
        });

        ScrollPane scrDanhSach = new ScrollPane(pnlDanhSachNhom);
        scrDanhSach.setFitToWidth(true);
        VBox.setVgrow(scrDanhSach, Priority.ALWAYS);
        scrDanhSach.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // 4. Thanh Footer
        HBox pnlFooter = new HBox(12);
        pnlFooter.setAlignment(Pos.CENTER_LEFT);
        pnlFooter.setPadding(new Insets(6, 0, 0, 0));

        VBox boxThongKe = new VBox(2);
        Label lblGhiChu = new Label("Chức năng không được cấp quyền sẽ được ẩn khi nhân viên sử dụng ứng dụng.");
        lblGhiChu.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        Label lblTongQuyen = new Label("Đã chọn: 0/0 quyền");
        lblTongQuyen.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7; -fx-font-size: 12.5px;");
        boxThongKe.getChildren().addAll(lblGhiChu, lblTongQuyen);
        pnlGoc.getProperties().put("lblTongQuyen", lblTongQuyen);

        Region spFooter = new Region();
        HBox.setHgrow(spFooter, Priority.ALWAYS);

        Button btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setMinWidth(Region.USE_PREF_SIZE);
        btnHuy.setOnAction(e -> cuaSo.close());

        Button btnKhoiPhuc = new Button("Khôi phục quyền theo vai trò");
        btnKhoiPhuc.getStyleClass().add("nut-phu");
        btnKhoiPhuc.setMinWidth(Region.USE_PREF_SIZE);

        Button btnLuu = new Button("Lưu quyền");
        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setMinWidth(Region.USE_PREF_SIZE);

        pnlFooter.getChildren().addAll(boxThongKe, spFooter, btnHuy, btnKhoiPhuc, btnLuu);

        // Kiểm tra xem nhân viên đã có cấu hình quyền tùy chỉnh trước đó chưa
        Set<String> quyenHienTai = PhanQuyenService.layDanhSachQuyen(nv.getMaNhanVien());
        boolean coQuyenTuyChinh = (quyenHienTai != null);

        // Hàm helper đồng bộ CheckBox theo vai trò mặc định
        Runnable dongBoTheoVaiTro = () -> {
            String roleName = cboVaiTro.getValue();
            try {
                TaiKhoan.VaiTro vt = TaiKhoan.VaiTro.tuTenHienThi(roleName);
                for (DanhMucMenu.NhomMenu nhom : tatCaNhom) {
                    for (DanhMucMenu.ChucNangMenu cn : nhom.getDanhSachChucNang()) {
                        CheckBox cb = mapCheckBoxes.get(cn.getMaChucNang());
                        if (cb != null) {
                            cb.setSelected(DanhMucMenu.kiemTraQuyen(vt, cn.getMaChucNang()));
                        }
                    }
                }
                capNhatThongKe(mapTheoNhom, mapNhanTyLe, pnlGoc);
            } catch (Exception ignored) {}
        };

        // Lắng nghe thay đổi CheckBox thủ công để cập nhật đếm số lượng
        mapCheckBoxes.values().forEach(cb -> cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
            capNhatThongKe(mapTheoNhom, mapNhanTyLe, pnlGoc);
        }));

        // Khởi tạo ban đầu
        cboVaiTro.setValue(vaiTroBanDau);
        if (coQuyenTuyChinh) {
            for (DanhMucMenu.NhomMenu nhom : tatCaNhom) {
                for (DanhMucMenu.ChucNangMenu cn : nhom.getDanhSachChucNang()) {
                    CheckBox cb = mapCheckBoxes.get(cn.getMaChucNang());
                    if (cb != null) {
                        cb.setSelected(quyenHienTai.contains(cn.getMaChucNang()));
                    }
                }
            }
            capNhatThongKe(mapTheoNhom, mapNhanTyLe, pnlGoc);
        } else {
            dongBoTheoVaiTro.run();
        }

        // Gán sự kiện khi đổi ComboBox vai trò hoặc bấm Khôi phục
        cboVaiTro.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                dongBoTheoVaiTro.run();
            }
        });

        btnKhoiPhuc.setOnAction(e -> {
            dongBoTheoVaiTro.run();
            hienThiToast("Đã khôi phục quyền mặc định theo vai trò '" + cboVaiTro.getValue() + "'");
        });

        // Xử lý lưu quyền
        btnLuu.setOnAction(e -> {
            try {
                String vaiTroMoi = cboVaiTro.getValue();
                boolean ok = controller.capNhatNhanVien(nv, vaiTroMoi, true);
                if (ok) {
                    // Thu thập toàn bộ chức năng được tích chọn
                    Set<String> dsQuyenMoi = new HashSet<>();
                    for (Map.Entry<String, CheckBox> entry : mapCheckBoxes.entrySet()) {
                        if (entry.getValue().isSelected()) {
                            dsQuyenMoi.add(entry.getKey());
                        }
                    }
                    // Lưu quyền chi tiết cho nhân viên vào CSDL và cache
                    PhanQuyenService.luuDanhSachQuyen(nv.getMaNhanVien(), dsQuyenMoi);

                    cuaSo.close();
                    taiLaiBang();
                    hienThiToast("Đã cập nhật phân quyền nhân viên '" + nv.getHoTen() + "' thành công!");
                } else {
                    hienThiLoi("Lỗi", "Không thể lưu cập nhật quyền vào cơ sở dữ liệu!");
                }
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi lưu quyền", ex.getMessage());
            }
        });

        pnlGoc.getChildren().addAll(pnlTheHeader, pnlThanhCongCu, scrDanhSach, pnlFooter);

        Scene scene = new Scene(pnlGoc, 920, 650);
        if (PhanQuyenNhanVienView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(PhanQuyenNhanVienView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        cuaSo.setScene(scene);
        cuaSo.setMinWidth(860);
        cuaSo.setMinHeight(580);
        cuaSo.setResizable(true);
        cuaSo.showAndWait();
    }

    private void capNhatThongKe(Map<String, List<CheckBox>> mapTheoNhom, Map<String, Label> mapNhanTyLe, VBox pnlGoc) {
        int tongChon = 0;
        int tongTatCa = 0;

        for (Map.Entry<String, List<CheckBox>> entry : mapTheoNhom.entrySet()) {
            List<CheckBox> list = entry.getValue();
            int c = (int) list.stream().filter(CheckBox::isSelected).count();
            tongChon += c;
            tongTatCa += list.size();

            Label lbl = mapNhanTyLe.get(entry.getKey());
            if (lbl != null) {
                lbl.setText(c + "/" + list.size());
            }
        }

        Label lblTongQuyen = (Label) pnlGoc.getProperties().get("lblTongQuyen");
        if (lblTongQuyen != null) {
            lblTongQuyen.setText("Đã chọn " + tongChon + "/" + tongTatCa + " quyền");
        }
    }

    private HBox taoTheHeaderChinhQuyen(NhanVien nv) {
        HBox the = new HBox(14);
        the.setAlignment(Pos.CENTER_LEFT);
        the.setPadding(new Insets(12, 16, 12, 16));
        the.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        VBox boxInfo = new VBox(2);
        Label lblTen = new Label(nv.getHoTen());
        lblTen.setStyle("-fx-font-weight: bold; -fx-font-size: 14.5px; -fx-text-fill: #0f172a;");
        Label lblSub = new Label(nv.getMaNhanVien() + "  •  Tài khoản: " + nv.getTenDangNhap());
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        boxInfo.getChildren().addAll(lblTen, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox boxRole = new VBox(4);
        boxRole.setAlignment(Pos.CENTER_RIGHT);
        Label lblTitleRole = new Label("Vai trò cấp quyền:");
        lblTitleRole.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        ComboBox<String> cboVaiTro = new ComboBox<>(FXCollections.observableArrayList("Quản lý", "Bán hàng", "Chăm sóc"));
        cboVaiTro.setPrefWidth(140);
        boxRole.getChildren().addAll(lblTitleRole, cboVaiTro);

        the.setUserData(cboVaiTro);
        the.getChildren().addAll(boxInfo, spacer, boxRole);
        return the;
    }

    private void hienThiCanhBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }

    private void hienThiLoi(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

