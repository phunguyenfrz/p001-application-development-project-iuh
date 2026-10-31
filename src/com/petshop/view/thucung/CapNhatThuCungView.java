package com.petshop.view.thucung;

import com.petshop.controller.KhachHangController;
import com.petshop.controller.ThuCungController;
import com.petshop.model.KhachHang;
import com.petshop.model.ThuCung;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Màn hình Quản lý & Cập nhật hồ sơ thú cưng (TC_CAP_NHAT).
 * Cung cấp chức năng xem danh sách thú cưng, thêm mới thú cưng và chỉnh sửa thông tin kể cả cân nặng.
 * Đồng bộ phong cách với giao diện Happy Tails (không có nút xóa và cột trạng thái).
 */
public class CapNhatThuCungView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ThuCungController thuCungController = new ThuCungController();
    private final KhachHangController khachHangController = new KhachHangController();
    private final ObservableList<ThuCung> danhSachHienThi = FXCollections.observableArrayList();

    // Các nút chức năng trên thanh tác vụ
    private final Button btnThemMoi = new Button();
    private final Button btnCapNhat = new Button();
    private final Button btnLamMoi = new Button();

    // Bảng hiển thị danh sách thú cưng
    private final TableView<ThuCung> tblDanhSach = new TableView<>();

    public CapNhatThuCungView() {
        setSpacing(14);
        setPadding(new Insets(20, 24, 20, 24));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề phân hệ nghiệp vụ
        VBox pnlTieuDeManHinh = new VBox(4);
        Label lblTieuDe = new Label("QUẢN LÝ THÚ CƯNG - CẬP NHẬT HỒ SƠ");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Quản lý danh sách thú cưng của khách hàng, theo dõi cân nặng và cập nhật hồ sơ chăm sóc.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        pnlTieuDeManHinh.getChildren().addAll(lblTieuDe, lblMoTa);

        // 2. Bảng dữ liệu và thanh tác vụ
        VBox pnlBangDuLieu = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBangDuLieu, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeManHinh, pnlBangDuLieu);

        // Mặc định vô hiệu hóa nút Cập nhật khi chưa chọn dòng nào trên bảng
        capNhatTrangThaiNut(null);

        // Tải dữ liệu ban đầu từ CSDL lên bảng
        taiLaiDanhSach();
    }

    /**
     * Khởi tạo thanh tác vụ phía trên bảng danh sách (Thêm mới, Cập nhật, Làm mới).
     */
    private HBox taoThanhTacVu() {
        HBox pnlThanhNut = new HBox(10);
        pnlThanhNut.getStyleClass().add("thanh-tac-vu");
        pnlThanhNut.setAlignment(Pos.CENTER_LEFT);
        pnlThanhNut.setMaxWidth(Double.MAX_VALUE);

        Label lblDanhSach = new Label("Danh sách thú cưng");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        btnThemMoi.setText("Thêm mới");
        btnThemMoi.getStyleClass().add("nut-chinh");
        btnThemMoi.setOnAction(e -> moCuaSoThemMoi());

        btnCapNhat.setText("Cập nhật");
        btnCapNhat.getStyleClass().add("nut-phu");
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());

        btnLamMoi.setText("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> taiLaiDanhSach());

        pnlThanhNut.getChildren().addAll(lblDanhSach, pnlKhoangTrong, btnThemMoi, btnCapNhat, btnLamMoi);
        return pnlThanhNut;
    }

    /**
     * Khởi tạo khung chứa thanh tác vụ và bảng dữ liệu TableView.
     */
    private VBox taoKhungBangDanhSach() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        HBox pnlThanhNut = taoThanhTacVu();

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có hồ sơ thú cưng nào trong hệ thống"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        // --- Cột 1: Mã thú cưng ---
        TableColumn<ThuCung, String> colMa = new TableColumn<>("Mã thú cưng");
        colMa.setPrefWidth(150);
        colMa.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaThuCung()));
        colMa.setStyle("-fx-alignment: CENTER;");

        // --- Cột 2: Tên thú cưng ---
        TableColumn<ThuCung, String> colTen = new TableColumn<>("Tên thú cưng");
        colTen.setPrefWidth(150);
        colTen.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTenThuCung()));
        colTen.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 3: Loài ---
        TableColumn<ThuCung, String> colLoai = new TableColumn<>("Loài");
        colLoai.setPrefWidth(100);
        colLoai.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getLoaiThuCung() != null ? cell.getValue().getLoaiThuCung() : "-"
        ));
        colLoai.setStyle("-fx-alignment: CENTER;");

        // --- Cột 4: Giống loài ---
        TableColumn<ThuCung, String> colGiong = new TableColumn<>("Giống loài");
        colGiong.setPrefWidth(160);
        colGiong.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getGiongLoai() != null ? cell.getValue().getGiongLoai() : "-"
        ));
        colGiong.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 5: Giới tính ---
        TableColumn<ThuCung, String> colGioiTinh = new TableColumn<>("Giới tính");
        colGioiTinh.setPrefWidth(85);
        colGioiTinh.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getGioiTinh() != null ? cell.getValue().getGioiTinh() : "-"
        ));
        colGioiTinh.setStyle("-fx-alignment: CENTER;");

        // --- Cột 6: Cân nặng ---
        TableColumn<ThuCung, String> colCanNang = new TableColumn<>("Cân nặng");
        colCanNang.setPrefWidth(110);
        colCanNang.setCellValueFactory(cell -> {
            BigDecimal cn = cell.getValue().getCanNang();
            return new SimpleStringProperty(cn != null ? String.format("%.2f kg", cn) : "-");
        });
        colCanNang.setStyle("-fx-alignment: CENTER-RIGHT;");

        // --- Cột 7: Ngày sinh ---
        TableColumn<ThuCung, String> colNgaySinh = new TableColumn<>("Ngày sinh");
        colNgaySinh.setPrefWidth(115);
        colNgaySinh.setCellValueFactory(cell -> {
            LocalDate ns = cell.getValue().getNgaySinh();
            return new SimpleStringProperty(ns != null ? ns.format(DINH_DANG_NGAY) : "-");
        });
        colNgaySinh.setStyle("-fx-alignment: CENTER;");

        // --- Cột 8: Tên chủ nuôi ---
        TableColumn<ThuCung, String> colTenChuNuoi = new TableColumn<>("Tên chủ nuôi");
        colTenChuNuoi.setPrefWidth(160);
        colTenChuNuoi.setCellValueFactory(cell -> {
            ThuCung tc = cell.getValue();
            if (tc.getTenKhachHang() != null && !tc.getTenKhachHang().isEmpty()) {
                return new SimpleStringProperty(tc.getTenKhachHang());
            }
            return new SimpleStringProperty(tc.getMaKhachHang() != null ? tc.getMaKhachHang() : "-");
        });
        colTenChuNuoi.setStyle("-fx-alignment: CENTER-LEFT;");

        // --- Cột 9: SĐT chủ nuôi ---
        TableColumn<ThuCung, String> colSdtChuNuoi = new TableColumn<>("SĐT chủ nuôi");
        colSdtChuNuoi.setPrefWidth(120);
        colSdtChuNuoi.setCellValueFactory(cell -> {
            ThuCung tc = cell.getValue();
            return new SimpleStringProperty(
                    tc.getSoDienThoaiKhachHang() != null ? tc.getSoDienThoaiKhachHang() : "-"
            );
        });
        colSdtChuNuoi.setStyle("-fx-alignment: CENTER;");

        // --- Cột 10: Dị ứng / Ghi chú ---
        TableColumn<ThuCung, String> colDiUng = new TableColumn<>("Dị ứng / Ghi chú");
        colDiUng.setMinWidth(180);
        colDiUng.prefWidthProperty().bind(
                tblDanhSach.widthProperty().subtract(150 + 150 + 100 + 150 + 80 + 105 + 110 + 160 + 120 + 20)
        );
        colDiUng.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDiUng() != null ? cell.getValue().getDiUng() : "-"
        ));
        colDiUng.setStyle("-fx-alignment: CENTER-LEFT;");

        tblDanhSach.getColumns().addAll(List.of(
                colMa, colTen, colLoai, colGiong, colGioiTinh, colCanNang, colNgaySinh, colTenChuNuoi, colSdtChuNuoi, colDiUng
        ));
        tblDanhSach.setItems(danhSachHienThi);

        // Hỗ trợ nhấp đúp chuột vào dòng để mở nhanh popup Cập nhật
        tblDanhSach.setRowFactory(tv -> {
            TableRow<ThuCung> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moCuaSoCapNhat();
                }
            });
            return row;
        });

        // Lắng nghe sự kiện chọn dòng trên bảng để bật/tắt nút Cập nhật
        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));

        pnlKhung.getChildren().addAll(pnlThanhNut, tblDanhSach);
        return pnlKhung;
    }

    /**
     * Bật / tắt nút chức năng tùy theo trạng thái dòng được chọn trên bảng.
     */
    private void capNhatTrangThaiNut(ThuCung tcChon) {
        btnCapNhat.setDisable(tcChon == null);
    }

    /**
     * Tải lại toàn bộ danh sách thú cưng từ tầng Controller.
     */
    public void taiLaiDanhSach() {
        try {
            List<ThuCung> ds = thuCungController.layDanhSachThuCung();
            danhSachHienThi.setAll(ds);
            capNhatTrangThaiNut(null);
        } catch (Exception ex) {
            hienThiLoi("Lỗi tải dữ liệu", "Không thể tải danh sách thú cưng: " + ex.getMessage());
        }
    }

    /**
     * Mở hộp thoại ThemThuCungView dạng popup modal riêng biệt.
     */
    private void moCuaSoThemMoi() {
        Stage dlgThem = new Stage();
        dlgThem.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlgThem.initOwner(getScene().getWindow());
        }
        dlgThem.setTitle("Thêm thú cưng mới - Happy Tails");

        ThemThuCungView manThem = new ThemThuCungView(this::taiLaiDanhSach);
        Scene scene = new Scene(manThem, 580, 620);
        ganCssChung(scene);

        dlgThem.setScene(scene);
        dlgThem.setResizable(false);
        dlgThem.showAndWait();
    }

    /**
     * Mở hộp thoại Cập nhật thú cưng cho đối tượng đang được chọn.
     */
    private void moCuaSoCapNhat() {
        ThuCung tcChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (tcChon == null) {
            hienThiCanhBao("Chưa chọn thú cưng", "Vui lòng chọn một thú cưng trong danh sách để cập nhật!");
            return;
        }

        Stage dlgCapNhat = new Stage();
        dlgCapNhat.initModality(Modality.APPLICATION_MODAL);
        if (getScene() != null && getScene().getWindow() != null) {
            dlgCapNhat.initOwner(getScene().getWindow());
        }
        dlgCapNhat.setTitle("Cập nhật thông tin thú cưng - Happy Tails");

        VBox pnlGoc = taoNoiDungCuaSoCapNhat(tcChon, dlgCapNhat);
        Scene scene = new Scene(pnlGoc, 580, 620);
        ganCssChung(scene);

        dlgCapNhat.setScene(scene);
        dlgCapNhat.setResizable(false);
        dlgCapNhat.showAndWait();
    }

    /**
     * Tạo giao diện form trong cửa sổ Cập nhật thông tin thú cưng.
     */
    private VBox taoNoiDungCuaSoCapNhat(ThuCung tcChon, Stage cuaSo) {
        VBox pnlGoc = new VBox(14);
        pnlGoc.setPadding(new Insets(18));
        pnlGoc.getStyleClass().add("khung-phan-he");

        // Tiêu đề form cập nhật
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT THÔNG TIN THÚ CƯNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblDangSua = new Label("Đang chỉnh sửa: " + tcChon.getMaThuCung() + " - " + tcChon.getTenThuCung());
        lblDangSua.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0284c7;");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblDangSua);

        // Khung nhập liệu
        VBox pnlForm = new VBox(14);
        pnlForm.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        TextField txtMa = new TextField(tcChon.getMaThuCung());
        txtMa.setEditable(false);

        // Chọn chủ nuôi
        TextField txtLocKhachHang = new TextField();
        txtLocKhachHang.setPromptText("Lọc nhanh khách hàng chủ nuôi...");
        ComboBox<KhachHang> cboKhachHang = new ComboBox<>();
        cboKhachHang.setMaxWidth(Double.MAX_VALUE);

        ObservableList<KhachHang> dsKhachHangGoc = FXCollections.observableArrayList();
        FilteredList<KhachHang> dsKhachHangLoc;
        try {
            List<KhachHang> dsKh = khachHangController.layDanhSachKhachHang();
            dsKhachHangGoc.setAll(dsKh);
            dsKhachHangLoc = new FilteredList<>(dsKhachHangGoc, p -> true);
            cboKhachHang.setItems(dsKhachHangLoc);
            cboKhachHang.setConverter(new StringConverter<>() {
                @Override
                public String toString(KhachHang kh) {
                    if (kh == null) return "";
                    return kh.getMaKhachHang() + " - " + kh.getHoTen() + " (" + kh.getSoDienThoai() + ")";
                }

                @Override
                public KhachHang fromString(String string) {
                    return null;
                }
            });

            // Chọn khách hàng hiện tại
            for (KhachHang kh : dsKh) {
                if (kh.getMaKhachHang().equalsIgnoreCase(tcChon.getMaKhachHang())) {
                    cboKhachHang.setValue(kh);
                    break;
                }
            }

            txtLocKhachHang.textProperty().addListener((obs, cu, moi) -> {
                String tuKhoa = (moi != null) ? moi.trim().toLowerCase() : "";
                dsKhachHangLoc.setPredicate(kh -> {
                    if (tuKhoa.isEmpty()) return true;
                    return kh.getHoTen().toLowerCase().contains(tuKhoa)
                            || kh.getSoDienThoai().contains(tuKhoa)
                            || kh.getMaKhachHang().toLowerCase().contains(tuKhoa);
                });
            });
        } catch (Exception ignored) {}

        VBox boxChuNuoi = new VBox(4, txtLocKhachHang, cboKhachHang);

        TextField txtTen = new TextField(tcChon.getTenThuCung());
        ComboBox<String> cboLoai = new ComboBox<>(FXCollections.observableArrayList(
                "Chó", "Mèo", "Thỏ", "Hamster", "Chim", "Khác"
        ));
        cboLoai.setMaxWidth(Double.MAX_VALUE);

        TextField txtLoaiKhac = new TextField();
        txtLoaiKhac.setPromptText("Nhập tên loài thú cưng cụ thể (VD: Rùa, Sóc, Nhím, Kỳ nhông...)");

        String loaiHienTai = tcChon.getLoaiThuCung() != null ? tcChon.getLoaiThuCung() : "Chó";
        List<String> dsLoaiChuan = List.of("Chó", "Mèo", "Thỏ", "Hamster", "Chim");
        if (dsLoaiChuan.contains(loaiHienTai)) {
            cboLoai.setValue(loaiHienTai);
            txtLoaiKhac.setVisible(false);
            txtLoaiKhac.setManaged(false);
        } else {
            cboLoai.setValue("Khác");
            txtLoaiKhac.setText(loaiHienTai);
            txtLoaiKhac.setVisible(true);
            txtLoaiKhac.setManaged(true);
        }

        cboLoai.valueProperty().addListener((obs, cu, moi) -> {
            boolean laKhac = "Khác".equalsIgnoreCase(moi);
            txtLoaiKhac.setVisible(laKhac);
            txtLoaiKhac.setManaged(laKhac);
            if (laKhac) {
                txtLoaiKhac.requestFocus();
            }
        });

        VBox boxLoai = new VBox(4, cboLoai, txtLoaiKhac);

        TextField txtGiong = new TextField(tcChon.getGiongLoai() != null ? tcChon.getGiongLoai() : "");

        ComboBox<String> cboGioiTinh = new ComboBox<>(FXCollections.observableArrayList("Đực", "Cái"));
        cboGioiTinh.setValue(tcChon.getGioiTinh() != null ? tcChon.getGioiTinh() : "Đực");
        cboGioiTinh.setMaxWidth(Double.MAX_VALUE);

        // Cân nặng cho phép sửa, chỉ nhận số và thập phân
        TextField txtCanNang = new TextField(tcChon.getCanNang() != null ? tcChon.getCanNang().toPlainString() : "");
        txtCanNang.textProperty().addListener((obs, cu, moi) -> {
            if (moi != null && !moi.matches("\\d*(\\.\\d{0,2})?")) {
                txtCanNang.setText(cu != null ? cu : "");
            }
        });

        // Ngày sinh
        DatePicker dpNgaySinh = new DatePicker(tcChon.getNgaySinh());
        dpNgaySinh.setMaxWidth(Double.MAX_VALUE);
        dpNgaySinh.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date != null && date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8;");
                }
            }
        });

        TextArea txtDiUng = new TextArea(tcChon.getDiUng() != null ? tcChon.getDiUng() : "");
        txtDiUng.setPrefRowCount(3);
        txtDiUng.setWrapText(true);

        pnlLuoi.add(new Label("Mã thú cưng:"), 0, 0);
        pnlLuoi.add(txtMa, 1, 0);

        pnlLuoi.add(new Label("Chủ nuôi (*):"), 0, 1);
        pnlLuoi.add(boxChuNuoi, 1, 1);

        pnlLuoi.add(new Label("Tên thú cưng (*):"), 0, 2);
        pnlLuoi.add(txtTen, 1, 2);

        pnlLuoi.add(new Label("Loài thú cưng:"), 0, 3);
        pnlLuoi.add(boxLoai, 1, 3);

        pnlLuoi.add(new Label("Giống loài:"), 0, 4);
        pnlLuoi.add(txtGiong, 1, 4);

        pnlLuoi.add(new Label("Giới tính (*):"), 0, 5);
        pnlLuoi.add(cboGioiTinh, 1, 5);

        pnlLuoi.add(new Label("Cân nặng (kg) (*):"), 0, 6);
        pnlLuoi.add(txtCanNang, 1, 6);

        pnlLuoi.add(new Label("Ngày sinh:"), 0, 7);
        pnlLuoi.add(dpNgaySinh, 1, 7);

        pnlLuoi.add(new Label("Dị ứng / Ghi chú:"), 0, 8);
        pnlLuoi.add(txtDiUng, 1, 8);

        ColumnConstraints colNhan = new ColumnConstraints(130);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        // Thanh nút bấm điều khiển trong form sửa
        HBox pnlNutDieuKhien = new HBox(10);
        pnlNutDieuKhien.setAlignment(Pos.CENTER_RIGHT);

        Button btnLuu = new Button("Lưu thay đổi");
        btnLuu.getStyleClass().add("nut-chinh");
        btnLuu.setOnAction(e -> {
            try {
                KhachHang chuNuoi = cboKhachHang.getValue();
                if (chuNuoi == null) {
                    hienThiCanhBao("Chưa chọn chủ nuôi", "Vui lòng chọn khách hàng chủ nuôi cho thú cưng!");
                    cboKhachHang.requestFocus();
                    return;
                }

                String ten = txtTen.getText() != null ? txtTen.getText().trim() : "";
                if (ten.isEmpty()) {
                    hienThiCanhBao("Thiếu thông tin", "Tên thú cưng không được để trống!");
                    txtTen.requestFocus();
                    return;
                }

                String canNangStr = txtCanNang.getText() != null ? txtCanNang.getText().trim() : "";
                if (canNangStr.isEmpty()) {
                    hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập cân nặng của thú cưng!");
                    txtCanNang.requestFocus();
                    return;
                }

                BigDecimal canNang;
                try {
                    canNang = new BigDecimal(canNangStr);
                    if (canNang.compareTo(BigDecimal.ZERO) <= 0) {
                        hienThiCanhBao("Cân nặng không hợp lệ", "Cân nặng thú cưng phải lớn hơn 0 kg!");
                        txtCanNang.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    hienThiCanhBao("Cân nặng không hợp lệ", "Cân nặng phải là định dạng số hợp lệ (VD: 4.5)!");
                    txtCanNang.requestFocus();
                    return;
                }

                LocalDate ngaySinh = dpNgaySinh.getValue();
                if (ngaySinh != null && ngaySinh.isAfter(LocalDate.now())) {
                    hienThiCanhBao("Ngày sinh không hợp lệ", "Ngày sinh thú cưng không được ở tương lai!");
                    dpNgaySinh.requestFocus();
                    return;
                }

                String loai = cboLoai.getValue();
                if ("Khác".equalsIgnoreCase(loai)) {
                    String loaiNhap = txtLoaiKhac.getText() != null ? txtLoaiKhac.getText().trim() : "";
                    if (loaiNhap.isEmpty()) {
                        hienThiCanhBao("Thiếu thông tin", "Vui lòng nhập loài thú cưng cụ thể khi chọn 'Khác'!");
                        txtLoaiKhac.requestFocus();
                        return;
                    }
                    loai = loaiNhap;
                }

                ThuCung tcCapNhat = new ThuCung();
                tcCapNhat.setMaThuCung(tcChon.getMaThuCung());
                tcCapNhat.setMaKhachHang(chuNuoi.getMaKhachHang());
                tcCapNhat.setTenThuCung(ten);
                tcCapNhat.setLoaiThuCung(loai);
                tcCapNhat.setGiongLoai(txtGiong.getText() != null ? txtGiong.getText().trim() : "");
                tcCapNhat.setGioiTinh(cboGioiTinh.getValue());
                tcCapNhat.setCanNang(canNang);
                tcCapNhat.setNgaySinh(ngaySinh);
                tcCapNhat.setDiUng(txtDiUng.getText() != null ? txtDiUng.getText().trim() : "");

                boolean ok = thuCungController.capNhatThuCung(tcCapNhat);
                if (ok) {
                    cuaSo.close();
                    taiLaiDanhSach();
                    hienThiThongBao("Thành công", "Đã cập nhật hồ sơ thú cưng '" + tcChon.getMaThuCung() + "' thành công!");
                }
            } catch (IllegalArgumentException ex) {
                hienThiCanhBao("Dữ liệu không hợp lệ", ex.getMessage());
            } catch (Exception ex) {
                hienThiLoi("Lỗi khi cập nhật", ex.getMessage());
            }
        });

        Button btnHuy = new Button("Đóng");
        btnHuy.getStyleClass().add("nut-phu");
        btnHuy.setOnAction(e -> cuaSo.close());

        pnlNutDieuKhien.getChildren().addAll(btnLuu, btnHuy);
        pnlForm.getChildren().addAll(pnlLuoi, pnlNutDieuKhien);
        pnlGoc.getChildren().addAll(pnlTieuDe, pnlForm);

        return pnlGoc;
    }

    private void ganCssChung(Scene scene) {
        if (CapNhatThuCungView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(CapNhatThuCungView.class.getResource(CSS_CHUNG).toExternalForm());
        }
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

