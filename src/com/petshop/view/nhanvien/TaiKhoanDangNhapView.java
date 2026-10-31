package com.petshop.view.nhanvien;

import com.petshop.controller.NhanVienController;
import com.petshop.model.NhanVien;
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

import java.util.List;

/**
 * Màn hình Quản lý Tài khoản đăng nhập của nhân viên (NV_TAI_KHOAN).
 * Đáp ứng đầy đủ các luồng nghiệp vụ:
 * 1. Danh sách nhân viên kèm trạng thái tài khoản hệ thống (Avatar viết tắt, tên đăng nhập, vai trò, trạng thái).
 * 2. Cấp mới tài khoản cho nhân viên đang làm việc (mật khẩu mặc định: 1111).
 * 3. Đặt lại mật khẩu (reset password) về 1111 có hộp thoại xác nhận.
 * 4. Xóa tài khoản đăng nhập có cảnh báo an toàn (chặn xóa Admin hoặc chính mình).
 */
public class TaiKhoanDangNhapView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";

    private final NhanVienController controller = new NhanVienController();
    private final ObservableList<NhanVien> dsNhanVienGoc = FXCollections.observableArrayList();
    private FilteredList<NhanVien> dsNhanVienLoc;

    // Các thành phần thanh công cụ
    private final TextField txtTimKiem = new TextField();
    private final Label lblDaChon = new Label();
    private final Button btnTaoTaiKhoan = new Button("+ Tạo tài khoản");
    private final Button btnDatLaiMatKhau = new Button("Đặt lại mật khẩu");
    private final Button btnXoaTaiKhoan = new Button("Xóa tài khoản");
    private final Label lblTongSo = new Label();

    // Bảng dữ liệu
    private final TableView<NhanVien> tblDanhSach = new TableView<>();

    public TaiKhoanDangNhapView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề màn hình
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("TÀI KHOẢN ĐĂNG NHẬP");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Quản lý cấp phát tài khoản, đặt lại mật khẩu và phân quyền truy cập cho nhân viên.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Khung nội dung chính
        VBox pnlKhungChinh = new VBox(12);
        pnlKhungChinh.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhungChinh, Priority.ALWAYS);

        HBox pnlThanhTacVu = taoThanhTacVu();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        pnlKhungChinh.getChildren().addAll(pnlThanhTacVu, pnlBang);
        getChildren().addAll(pnlTieuDe, pnlKhungChinh);

        // Khởi tạo trạng thái nút
        capNhatTrangThaiNut(null);

        // Nạp dữ liệu ban đầu
        taiLaiBang();
    }

    private HBox taoThanhTacVu() {
        HBox pnlThanh = new HBox(12);
        pnlThanh.getStyleClass().add("thanh-tac-vu");
        pnlThanh.setAlignment(Pos.CENTER_LEFT);
        pnlThanh.setMaxWidth(Double.MAX_VALUE);

        // Ô tìm kiếm nhanh
        txtTimKiem.setPromptText("Tìm kiếm nhanh theo mã, họ tên hoặc tên đăng nhập...");
        txtTimKiem.setPrefWidth(320);
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> locDuLieu());

        // Nhãn hiển thị nhân viên được chọn
        lblDaChon.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Nút Tạo tài khoản
        btnTaoTaiKhoan.getStyleClass().add("nut-chinh");
        btnTaoTaiKhoan.setOnAction(e -> xuLyTaoTaiKhoan());

        // Nút Đặt lại mật khẩu
        btnDatLaiMatKhau.getStyleClass().add("nut-thanh-cong");
        btnDatLaiMatKhau.setOnAction(e -> xuLyDatLaiMatKhau());

        // Nút Xóa tài khoản
        btnXoaTaiKhoan.getStyleClass().add("nut-nguy-hiem");
        btnXoaTaiKhoan.setOnAction(e -> xuLyXoaTaiKhoan());

        pnlThanh.getChildren().addAll(txtTimKiem, lblDaChon, spacer, btnTaoTaiKhoan, btnDatLaiMatKhau, btnXoaTaiKhoan);
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

        // 4. Cột Tài khoản đăng nhập
        TableColumn<NhanVien, NhanVien> colTaiKhoan = new TableColumn<>("Tài khoản đăng nhập");
        colTaiKhoan.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue()));
        colTaiKhoan.setPrefWidth(210);
        colTaiKhoan.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(NhanVien nv, boolean empty) {
                super.updateItem(nv, empty);
                if (empty || nv == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (nv.getTenDangNhap() != null && !nv.getTenDangNhap().isBlank()) {
                        String user = nv.getTenDangNhap();
                        if ("admin".equalsIgnoreCase(user)) {
                            Label lblUser = new Label("admin - Admin");
                            lblUser.setStyle("-fx-text-fill: #1e293b;");
                            Label lblBadge = new Label("Toàn quyền");
                            lblBadge.setPadding(new Insets(2, 6, 2, 6));
                            lblBadge.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #6b21a8; -fx-background-radius: 4px; -fx-font-size: 10.5px;");
                            HBox box = new HBox(8, lblUser, lblBadge);
                            box.setAlignment(Pos.CENTER_LEFT);
                            setGraphic(box);
                        } else {
                            Label lblUser = new Label(user);
                            lblUser.setStyle("-fx-text-fill: #0f172a;");
                            setGraphic(lblUser);
                        }
                    } else {
                        Label lblChua = new Label("Chưa có tài khoản");
                        lblChua.setStyle("-fx-font-style: italic; -fx-text-fill: #94a3b8;");
                        setGraphic(lblChua);
                    }
                    setText(null);
                }
            }
        });

        // 5. Cột Vai trò (Chức vụ) - hiển thị văn bản thường căn giữa, không dùng badge màu
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

        // 6. Cột Trạng thái
        TableColumn<NhanVien, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTrangThai()));
        colTrangThai.setPrefWidth(120);
        colTrangThai.setStyle("-fx-alignment: CENTER;");

        tblDanhSach.getColumns().addAll(colMaNv, colHoTen, colSdt, colTaiKhoan, colVaiTro, colTrangThai);

        // Hiệu ứng làm mờ dòng nhân viên đã nghỉ việc
        tblDanhSach.setRowFactory(tv -> {
            TableRow<NhanVien> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldVal, nv) -> {
                if (nv != null && !nv.isDangLam()) {
                    row.setOpacity(0.55);
                } else {
                    row.setOpacity(1.0);
                }
            });
            return row;
        });

        // Theo dõi chọn dòng trên bảng
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, nv) -> capNhatTrangThaiNut(nv));

        // Thanh trạng thái chân bảng
        HBox pnlChanBang = new HBox(12);
        pnlChanBang.setAlignment(Pos.CENTER_LEFT);
        pnlChanBang.setPadding(new Insets(4, 0, 0, 0));

        lblTongSo.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        Region spacerChan = new Region();
        HBox.setHgrow(spacerChan, Priority.ALWAYS);

        Label lblGhiChu = new Label("💡 Mật khẩu mặc định: 1111 | Chỉ cấp tài khoản cho nhân viên đang làm; Không xóa/reset Admin.");
        lblGhiChu.setStyle("-fx-font-style: italic; -fx-text-fill: #64748b; -fx-font-size: 11.5px;");

        pnlChanBang.getChildren().addAll(lblTongSo, spacerChan, lblGhiChu);

        pnlBang.getChildren().addAll(tblDanhSach, pnlChanBang);
        return pnlBang;
    }

    private void capNhatTrangThaiNut(NhanVien nv) {
        if (nv == null) {
            lblDaChon.setText("");
            btnTaoTaiKhoan.setDisable(false);
            btnDatLaiMatKhau.setDisable(true);
            btnXoaTaiKhoan.setDisable(true);
            return;
        }

        lblDaChon.setText("✔ Đã chọn: " + nv.getHoTen());

        boolean daCoTaiKhoan = (nv.getTenDangNhap() != null && !nv.getTenDangNhap().isBlank());
        boolean laAdmin = "admin".equalsIgnoreCase(nv.getTenDangNhap());
        boolean laChinhMinh = false;
        try {
            var tkHienTai = SessionManager.getTaiKhoanHienTai();
            if (tkHienTai != null && nv.getMaNhanVien().equalsIgnoreCase(tkHienTai.getMaNhanVien())) {
                laChinhMinh = true;
            }
        } catch (Exception ignored) {}

        if (daCoTaiKhoan) {
            btnTaoTaiKhoan.setDisable(true);
            if (laAdmin || laChinhMinh) {
                btnDatLaiMatKhau.setDisable(true);
                btnXoaTaiKhoan.setDisable(true);
            } else {
                btnDatLaiMatKhau.setDisable(false);
                btnXoaTaiKhoan.setDisable(false);
            }
        } else {
            btnTaoTaiKhoan.setDisable(!nv.isDangLam());
            btnDatLaiMatKhau.setDisable(true);
            btnXoaTaiKhoan.setDisable(true);
        }
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
            boolean matchMa = nv.getMaNhanVien() != null && nv.getMaNhanVien().toLowerCase().contains(tuKhoa);
            boolean matchTen = nv.getHoTen() != null && nv.getHoTen().toLowerCase().contains(tuKhoa);
            boolean matchUser = nv.getTenDangNhap() != null && nv.getTenDangNhap().toLowerCase().contains(tuKhoa);
            boolean matchSdt = nv.getSoDienThoai() != null && nv.getSoDienThoai().contains(tuKhoa);
            return matchMa || matchTen || matchUser || matchSdt;
        });

        int hienThi = dsNhanVienLoc.size();
        int tongSo = dsNhanVienGoc.size();
        lblTongSo.setText("Hiển thị " + hienThi + " / " + tongSo + " nhân viên");
    }

    // =========================================================================
    // 1. POPUP CẤP TÀI KHOẢN MỚI (Image 2)
    // =========================================================================

    private void xuLyTaoTaiKhoan() {
        NhanVien nvChon = tblDanhSach.getSelectionModel().getSelectedItem();

        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        Window owner = getScene() != null ? getScene().getWindow() : null;
        if (owner != null) cuaSo.initOwner(owner);
        cuaSo.setTitle("Xác nhận cấp tài khoản");

        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // Tiêu đề
        Label lblTieuDe = new Label("XÁC NHẬN CẤP TÀI KHOẢN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        // Thẻ nhân viên được chọn
        VBox pnlTheNhanVien = taoTheThongTinNhanVien(nvChon);

        // Khung form
        VBox pnlForm = new VBox(12);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(10);
        pnlLuoi.setVgap(10);

        ComboBox<NhanVien> cboNhanVien = new ComboBox<>();
        ObservableList<NhanVien> dsChuaCoTk = FXCollections.observableArrayList(
                dsNhanVienGoc.stream().filter(n -> n.isDangLam() && (n.getTenDangNhap() == null || n.getTenDangNhap().isBlank())).toList()
        );
        cboNhanVien.setItems(dsChuaCoTk);
        if (nvChon != null && (nvChon.getTenDangNhap() == null || nvChon.getTenDangNhap().isBlank())) {
            cboNhanVien.setValue(nvChon);
        } else if (!dsChuaCoTk.isEmpty()) {
            cboNhanVien.setValue(dsChuaCoTk.get(0));
        }

        TextField txtTenDangNhap = new TextField();
        txtTenDangNhap.setPromptText("Nhập tên đăng nhập...");
        if (cboNhanVien.getValue() != null) {
            String sdt = cboNhanVien.getValue().getSoDienThoai();
            txtTenDangNhap.setText(sdt != null && !sdt.isBlank() ? sdt.trim() : cboNhanVien.getValue().getMaNhanVien());
        }

        cboNhanVien.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String sdt = newVal.getSoDienThoai();
                txtTenDangNhap.setText(sdt != null && !sdt.isBlank() ? sdt.trim() : newVal.getMaNhanVien());
                capNhatTheThongTin(pnlTheNhanVien, newVal);
            }
        });

        pnlLuoi.add(new Label("Lựa chọn nhân viên:"), 0, 0);
        pnlLuoi.add(cboNhanVien, 1, 0);
        pnlLuoi.add(new Label("Tên đăng nhập:"), 0, 1);
        pnlLuoi.add(txtTenDangNhap, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints(140);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2);

        // Hộp thông báo màu cam vàng (mật khẩu mặc định 1111)
        HBox boxThongBao = new HBox(8);
        boxThongBao.setPadding(new Insets(10));
        boxThongBao.setAlignment(Pos.CENTER_LEFT);
        boxThongBao.setStyle("-fx-background-color: #fffbeb; -fx-border-color: #fde68a; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        Label lblMsg = new Label("ℹ  Tài khoản mới được cấp mật khẩu mặc định: 1111");
        lblMsg.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold; -fx-font-size: 12px;");
        boxThongBao.getChildren().add(lblMsg);

        // Nút bấm
        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnXacNhan = new Button("Xác nhận tạo tài khoản");
        btnXacNhan.getStyleClass().add("nut-chinh");

        Button btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> cuaSo.close());

        btnXacNhan.setOnAction(e -> {
            NhanVien targetNv = cboNhanVien.getValue();
            if (targetNv == null) {
                hienThiCanhBao("Chưa chọn nhân viên", "Vui lòng chọn một nhân viên để cấp tài khoản!");
                return;
            }
            String user = txtTenDangNhap.getText() != null ? txtTenDangNhap.getText().trim() : "";
            if (user.isEmpty()) {
                hienThiCanhBao("Tên đăng nhập trống", "Vui lòng nhập tên đăng nhập cho nhân viên!");
                txtTenDangNhap.requestFocus();
                return;
            }

            try {
                boolean ok = controller.themTaiKhoanChoNhanVien(targetNv.getMaNhanVien(), user, "1111", targetNv.getChucVu());
                if (ok) {
                    cuaSo.close();
                    taiLaiBang();
                    hienThiThongBao("Thành công", "Đã tạo tài khoản '" + user + "' cho nhân viên " + targetNv.getHoTen() + "!\nMật khẩu mặc định: 1111");
                }
            } catch (Exception ex) {
                hienThiLoi("Lỗi cấp tài khoản", ex.getMessage());
            }
        });

        pnlNut.getChildren().addAll(btnHuy, btnXacNhan);
        pnlForm.getChildren().addAll(pnlLuoi, boxThongBao, pnlNut);
        pnlGoc.getChildren().addAll(lblTieuDe, pnlTheNhanVien, pnlForm);

        Scene scene = new Scene(pnlGoc, 480, 360);
        if (TaiKhoanDangNhapView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(TaiKhoanDangNhapView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        cuaSo.setScene(scene);
        cuaSo.setResizable(false);
        cuaSo.showAndWait();
    }

    // =========================================================================
    // 2. POPUP ĐẶT LẠI MẬT KHẨU (Image 3)
    // =========================================================================

    private void xuLyDatLaiMatKhau() {
        NhanVien nv = tblDanhSach.getSelectionModel().getSelectedItem();
        if (nv == null || nv.getTenDangNhap() == null || nv.getTenDangNhap().isBlank()) {
            hienThiCanhBao("Chưa chọn tài khoản", "Vui lòng chọn một nhân viên đã có tài khoản để đặt lại mật khẩu!");
            return;
        }

        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        Window owner = getScene() != null ? getScene().getWindow() : null;
        if (owner != null) cuaSo.initOwner(owner);
        cuaSo.setTitle("Xác nhận đặt lại mật khẩu");

        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("XÁC NHẬN ĐẶT LẠI MẬT KHẨU");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        VBox pnlThe = taoTheThongTinNhanVien(nv);

        VBox pnlForm = new VBox(12);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(10);
        pnlLuoi.setVgap(10);

        TextField txtUser = new TextField(nv.getTenDangNhap());
        txtUser.setEditable(false);
        txtUser.setStyle("-fx-background-color: #f8fafc;");

        pnlLuoi.add(new Label("Tên đăng nhập:"), 0, 0);
        pnlLuoi.add(txtUser, 1, 0);

        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2);

        HBox boxThongBao = new HBox(8);
        boxThongBao.setPadding(new Insets(10));
        boxThongBao.setAlignment(Pos.CENTER_LEFT);
        boxThongBao.setStyle("-fx-background-color: #fffbeb; -fx-border-color: #fde68a; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        Label lblMsg = new Label("ℹ  Bạn có muốn đặt lại mật khẩu của tài khoản này về: 1111?");
        lblMsg.setStyle("-fx-text-fill: #b45309; -fx-font-weight: bold; -fx-font-size: 12px;");
        boxThongBao.getChildren().add(lblMsg);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnXacNhan = new Button("Xác nhận reset");
        btnXacNhan.getStyleClass().add("nut-thanh-cong");

        Button btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> cuaSo.close());

        btnXacNhan.setOnAction(e -> {
            try {
                boolean ok = controller.datLaiMatKhau(nv.getMaTaiKhoan(), "1111");
                if (ok) {
                    cuaSo.close();
                    hienThiThongBao("Thành công", "Đã đặt lại mật khẩu cho tài khoản '" + nv.getTenDangNhap() + "' về mặc định: 1111!");
                }
            } catch (Exception ex) {
                hienThiLoi("Lỗi reset mật khẩu", ex.getMessage());
            }
        });

        pnlNut.getChildren().addAll(btnHuy, btnXacNhan);
        pnlForm.getChildren().addAll(pnlLuoi, boxThongBao, pnlNut);
        pnlGoc.getChildren().addAll(lblTieuDe, pnlThe, pnlForm);

        Scene scene = new Scene(pnlGoc, 460, 320);
        if (TaiKhoanDangNhapView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(TaiKhoanDangNhapView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        cuaSo.setScene(scene);
        cuaSo.setResizable(false);
        cuaSo.showAndWait();
    }

    // =========================================================================
    // 3. POPUP XÓA TÀI KHOẢN (Image 4)
    // =========================================================================

    private void xuLyXoaTaiKhoan() {
        NhanVien nv = tblDanhSach.getSelectionModel().getSelectedItem();
        if (nv == null || nv.getTenDangNhap() == null || nv.getTenDangNhap().isBlank()) {
            hienThiCanhBao("Chưa chọn tài khoản", "Vui lòng chọn một tài khoản cần xóa!");
            return;
        }

        if ("admin".equalsIgnoreCase(nv.getTenDangNhap())) {
            hienThiCanhBao("Không thể xóa", "Không được phép xóa tài khoản Quản trị viên (Admin)!");
            return;
        }

        Stage cuaSo = new Stage();
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        Window owner = getScene() != null ? getScene().getWindow() : null;
        if (owner != null) cuaSo.initOwner(owner);
        cuaSo.setTitle("Xác nhận xóa tài khoản");

        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("XÁC NHẬN XÓA TÀI KHOẢN");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        VBox pnlThe = taoTheThongTinNhanVien(nv);

        VBox pnlForm = new VBox(12);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(10);
        pnlLuoi.setVgap(10);

        TextField txtUser = new TextField(nv.getTenDangNhap());
        txtUser.setEditable(false);
        txtUser.setStyle("-fx-background-color: #f8fafc;");

        pnlLuoi.add(new Label("Tên đăng nhập:"), 0, 0);
        pnlLuoi.add(txtUser, 1, 0);

        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2);

        HBox boxCanhBao = new HBox(8);
        boxCanhBao.setPadding(new Insets(10));
        boxCanhBao.setAlignment(Pos.CENTER_LEFT);
        boxCanhBao.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fecaca; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        Label lblMsg = new Label("⚠  Bạn chắc chắn muốn xóa tài khoản này khỏi hệ thống?");
        lblMsg.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-font-size: 12px;");
        boxCanhBao.getChildren().add(lblMsg);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnXoa = new Button("Xóa tài khoản");
        btnXoa.getStyleClass().add("nut-nguy-hiem");

        Button btnHuy = new Button("Hủy");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> cuaSo.close());

        btnXoa.setOnAction(e -> {
            try {
                boolean ok = controller.xoaTaiKhoan(nv.getMaTaiKhoan());
                if (ok) {
                    cuaSo.close();
                    taiLaiBang();
                    hienThiThongBao("Thành công", "Đã xóa tài khoản '" + nv.getTenDangNhap() + "' thành công!");
                }
            } catch (Exception ex) {
                hienThiLoi("Lỗi xóa tài khoản", ex.getMessage());
            }
        });

        pnlNut.getChildren().addAll(btnHuy, btnXoa);
        pnlForm.getChildren().addAll(pnlLuoi, boxCanhBao, pnlNut);
        pnlGoc.getChildren().addAll(lblTieuDe, pnlThe, pnlForm);

        Scene scene = new Scene(pnlGoc, 460, 320);
        if (TaiKhoanDangNhapView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(TaiKhoanDangNhapView.class.getResource(CSS_CHUNG).toExternalForm());
        }
        cuaSo.setScene(scene);
        cuaSo.setResizable(false);
        cuaSo.showAndWait();
    }

    // =========================================================================
    // HELPER UI: AVATAR VÀ THẺ NHÂN VIÊN
    // =========================================================================

    private VBox taoTheThongTinNhanVien(NhanVien nv) {
        VBox the = new VBox(6);
        the.setPadding(new Insets(10, 14, 10, 14));
        the.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        capNhatTheThongTin(the, nv);
        return the;
    }

    private void capNhatTheThongTin(VBox the, NhanVien nv) {
        the.getChildren().clear();
        if (nv == null) return;

        VBox boxText = new VBox(2);
        Label lblTen = new Label(nv.getHoTen());
        lblTen.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");
        Label lblMa = new Label(nv.getMaNhanVien() + " • " + nv.getChucVu());
        lblMa.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748b;");
        boxText.getChildren().addAll(lblTen, lblMa);

        HBox hang = new HBox(12, boxText);
        hang.setAlignment(Pos.CENTER_LEFT);
        the.getChildren().add(hang);
    }

    private void hienThiThongBao(String tieuDe, String noiDung) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
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
