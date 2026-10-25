package com.petshop.view.dichvu;

import com.petshop.controller.DichVuChamSocController;
import com.petshop.model.BangGiaDichVu;
import com.petshop.model.DichVuChamSoc;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình tra cứu bảng giá dịch vụ theo bậc cân nặng và tính nhanh đơn giá theo số kg thú cưng (SRV_PRICING).
 * Việc thêm/sửa giá đã được thực hiện tập trung tại màn Cập nhật dịch vụ.
 */
public class BangGiaDichVuView extends VBox {

    private final DichVuChamSocController dichVuController;
    private final ObservableList<BangGiaDichVu> dsBangGia;
    private List<BangGiaDichVu> dsBangGiaGoc;
    private List<DichVuChamSoc> dsDichVu;

    // Điều khiển lọc bảng giá
    private ComboBox<String> cboLocDichVu;
    private ComboBox<String> cboLocKichThuoc;
    private Button btnLamMoiLoc;

    // Điều khiển tra cứu nhanh đơn giá theo cân nặng thú cưng
    private ComboBox<String> cboDichVuTraCuu;
    private TextField txtCanNang;
    private Button btnTinhGia;
    private Label lblKetQuaTraGia;

    // Bảng danh sách bảng giá (chỉ xem)
    private TableView<BangGiaDichVu> tblBangGia;

    public BangGiaDichVuView() {
        this.dichVuController = new DichVuChamSocController();
        this.dsBangGia = FXCollections.observableArrayList();
        this.dsBangGiaGoc = new ArrayList<>();
        khoiTaoGiaoDien();
        taiDanhSachDichVu();
        taiLaiBang();
    }

    private void khoiTaoGiaoDien() {
        getStyleClass().add("khung-phan-he");
        setSpacing(14);
        setPadding(new Insets(12));

        Label lblTieuDe = new Label("DỊCH VỤ CHĂM SÓC - BẢNG GIÁ THEO CÂN NẶNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        Label lblMoTa = new Label("Tra cứu đơn giá theo 3 bậc cân nặng ("
                + "Nhỏ: " + BangGiaDichVu.KHOANG_CAN_NANG_NHO + ", "
                + "Vừa: " + BangGiaDichVu.KHOANG_CAN_NANG_VUA + ", "
                + "Lớn: " + BangGiaDichVu.KHOANG_CAN_NANG_LON
                + ") và tính nhanh đơn giá theo cân nặng thú cưng.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        VBox pnlTraCuuNhanh = taoKhungTraCuuNhanh();
        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(lblTieuDe, lblMoTa, pnlTraCuuNhanh, pnlBang);
    }

    // Tạo khung tính nhanh đơn giá khi nhập số kg của thú cưng
    private VBox taoKhungTraCuuNhanh() {
        VBox pnlTraCuu = new VBox(10);
        pnlTraCuu.getStyleClass().add("hop-noi-dung");

        Label lblTieuDeTraCuu = new Label("Tính nhanh đơn giá theo cân nặng thú cưng");
        lblTieuDeTraCuu.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        HBox pnlHangNhap = new HBox(12);
        pnlHangNhap.setAlignment(Pos.CENTER_LEFT);

        cboDichVuTraCuu = new ComboBox<>();
        cboDichVuTraCuu.setPrefWidth(280);

        txtCanNang = new TextField();
        txtCanNang.setPromptText("Ví dụ: 4.5");
        txtCanNang.setPrefWidth(140);
        chiChoPhepNhapSoThuc(txtCanNang);
        txtCanNang.setOnAction(e -> xuLyTinhGiaTheoCanNang());

        btnTinhGia = new Button("Tính đơn giá");
        btnTinhGia.getStyleClass().add("nut-chinh");
        btnTinhGia.setOnAction(e -> xuLyTinhGiaTheoCanNang());

        pnlHangNhap.getChildren().addAll(
                new Label("Dịch vụ:"), cboDichVuTraCuu,
                new Label("Cân nặng (kg):"), txtCanNang,
                btnTinhGia
        );

        lblKetQuaTraGia = new Label("Kết quả: Chọn dịch vụ và nhập cân nặng thú cưng để xem bậc kích thước và đơn giá.");
        lblKetQuaTraGia.setWrapText(true);
        lblKetQuaTraGia.setStyle("-fx-font-weight: bold; -fx-text-fill: #0369a1;");

        pnlTraCuu.getChildren().addAll(lblTieuDeTraCuu, pnlHangNhap, lblKetQuaTraGia);
        return pnlTraCuu;
    }

    // Tạo khung bảng hiển thị danh sách bảng giá kèm bộ lọc theo dịch vụ và bậc kích thước
    private VBox taoKhungBang() {
        VBox pnlBang = new VBox(10);
        pnlBang.getStyleClass().add("hop-noi-dung");

        HBox pnlBoLoc = new HBox(10);
        pnlBoLoc.getStyleClass().add("thanh-tac-vu");
        pnlBoLoc.setAlignment(Pos.CENTER_LEFT);

        Label lblTieuDeBang = new Label("Danh sách bảng giá hiện hành");
        lblTieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        cboLocDichVu = new ComboBox<>();
        cboLocDichVu.setPrefWidth(240);
        cboLocDichVu.setOnAction(e -> apDungBoLoc());

        cboLocKichThuoc = new ComboBox<>();
        cboLocKichThuoc.getItems().addAll("Tất cả bậc", "Nhỏ", "Vừa", "Lớn");
        cboLocKichThuoc.setValue("Tất cả bậc");
        cboLocKichThuoc.setOnAction(e -> apDungBoLoc());

        btnLamMoiLoc = new Button("Làm mới");
        btnLamMoiLoc.getStyleClass().add("nut-phu");
        btnLamMoiLoc.setOnAction(e -> xuLyLamMoi());

        pnlBoLoc.getChildren().addAll(
                lblTieuDeBang, pnlKhoangTrong,
                new Label("Lọc dịch vụ:"), cboLocDichVu,
                new Label("Bậc kích thước:"), cboLocKichThuoc,
                btnLamMoiLoc
        );

        tblBangGia = new TableView<>(dsBangGia);
        tblBangGia.getStyleClass().add("bang-du-lieu");
        tblBangGia.setPlaceholder(new Label("Chưa có dữ liệu bảng giá"));
        tblBangGia.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<BangGiaDichVu, String> colMaBangGia = new TableColumn<>("Mã bảng giá");
        colMaBangGia.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaBangGia()));

        TableColumn<BangGiaDichVu, String> colMaDichVu = new TableColumn<>("Mã dịch vụ");
        colMaDichVu.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMaDichVu()));

        TableColumn<BangGiaDichVu, String> colTenDichVu = new TableColumn<>("Tên dịch vụ");
        colTenDichVu.setCellValueFactory(cell -> new SimpleStringProperty(
                timTenDichVuTheoMa(cell.getValue().getMaDichVu())
        ));

        TableColumn<BangGiaDichVu, String> colKichThuoc = new TableColumn<>("Bậc kích thước");
        colKichThuoc.setCellValueFactory(cell -> new SimpleStringProperty(
                hienThiBacKemKhoangCan(cell.getValue().getPhanLoaiKichThuoc())
        ));

        TableColumn<BangGiaDichVu, String> colGia = new TableColumn<>("Đơn giá");
        colGia.setStyle("-fx-alignment: CENTER-RIGHT;");
        colGia.setCellValueFactory(cell -> new SimpleStringProperty(
                dinhDangTien(cell.getValue().getGia())
        ));

        TableColumn<BangGiaDichVu, String> colNgayApDung = new TableColumn<>("Ngày áp dụng");
        colNgayApDung.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getNgayApDung() == null ? "" : cell.getValue().getNgayApDung().toString()
        ));

        TableColumn<BangGiaDichVu, String> colNgayHetHan = new TableColumn<>("Trạng thái hiệu lực");
        colNgayHetHan.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getNgayHetHieuLuc() == null ? "Đang áp dụng" : "Hết hạn: " + cell.getValue().getNgayHetHieuLuc()
        ));

        tblBangGia.getColumns().addAll(List.of(
                colMaBangGia, colMaDichVu, colTenDichVu, colKichThuoc, colGia, colNgayApDung, colNgayHetHan
        ));

        VBox.setVgrow(tblBangGia, Priority.ALWAYS);
        pnlBang.getChildren().addAll(pnlBoLoc, tblBangGia);
        return pnlBang;
    }

    private void chiChoPhepNhapSoThuc(TextField oNhap) {
        oNhap.textProperty().addListener((obs, giaTriCu, giaTriMoi) -> {
            if (giaTriMoi != null && !giaTriMoi.matches("\\d*(\\.\\d*)?")) {
                oNhap.setText(giaTriCu);
            }
        });
    }

    // Tải danh sách dịch vụ vào ComboBox tra cứu và ComboBox lọc
    private void taiDanhSachDichVu() {
        try {
            dsDichVu = dichVuController.layDanhSachDichVu();
            cboDichVuTraCuu.getItems().clear();
            cboLocDichVu.getItems().clear();
            cboLocDichVu.getItems().add("Tất cả dịch vụ");

            for (DichVuChamSoc dv : dsDichVu) {
                String hienThi = dv.getMaDichVu() + " - " + dv.getTenDichVu();
                cboDichVuTraCuu.getItems().add(hienThi);
                cboLocDichVu.getItems().add(hienThi);
            }

            cboLocDichVu.setValue("Tất cả dịch vụ");
            if (!cboDichVuTraCuu.getItems().isEmpty()) {
                cboDichVuTraCuu.getSelectionModel().selectFirst();
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi tải dịch vụ", e.getMessage());
        }
    }

    // Tải lại danh sách bảng giá từ CSDL
    private void taiLaiBang() {
        try {
            dsBangGiaGoc = dichVuController.layTatCaBangGia();
            apDungBoLoc();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi tải bảng giá", e.getMessage());
        }
    }

    // Lọc danh sách bảng giá theo dịch vụ và bậc kích thước đã chọn
    private void apDungBoLoc() {
        String maDichVuLoc = layMaDichVuTuCombo(cboLocDichVu);
        String bacLoc = cboLocKichThuoc.getValue();

        List<BangGiaDichVu> ketQua = new ArrayList<>();
        for (BangGiaDichVu bg : dsBangGiaGoc) {
            boolean khopDichVu = maDichVuLoc.isEmpty() || bg.getMaDichVu().equals(maDichVuLoc);
            boolean khopBac = bacLoc == null || "Tất cả bậc".equals(bacLoc) || bacLoc.equals(bg.getPhanLoaiKichThuoc());
            if (khopDichVu && khopBac) {
                ketQua.add(bg);
            }
        }
        dsBangGia.setAll(ketQua);
    }

    // Đặt lại bộ lọc và tải lại dữ liệu mới nhất
    private void xuLyLamMoi() {
        taiDanhSachDichVu();
        cboLocDichVu.setValue("Tất cả dịch vụ");
        cboLocKichThuoc.setValue("Tất cả bậc");
        txtCanNang.clear();
        lblKetQuaTraGia.setText("Kết quả: Chọn dịch vụ và nhập cân nặng thú cưng để xem bậc kích thước và đơn giá.");
        taiLaiBang();
    }

    // Xử lý khi bấm nút Tính đơn giá theo cân nặng
    private void xuLyTinhGiaTheoCanNang() {
        try {
            String maDichVu = layMaDichVuTuCombo(cboDichVuTraCuu);
            if (maDichVu.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn dịch vụ cần tra giá.");
            }

            String chuoiCanNang = txtCanNang.getText() == null ? "" : txtCanNang.getText().trim();
            if (chuoiCanNang.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập cân nặng thú cưng (> 0).");
            }

            double canNang = Double.parseDouble(chuoiCanNang);
            String kichThuoc = BangGiaDichVu.xacDinhKichThuoc(canNang);
            double donGia = dichVuController.traGiaTheoCanNang(maDichVu, canNang);

            if (donGia <= 0) {
                lblKetQuaTraGia.setText("Kết quả: Bậc [" + hienThiBacKemKhoangCan(kichThuoc) + "] chưa có bảng giá còn hiệu lực.");
            } else {
                lblKetQuaTraGia.setText("Kết quả: Cân nặng " + canNang + " kg -> Bậc ["
                        + hienThiBacKemKhoangCan(kichThuoc) + "] -> Đơn giá: " + dinhDangTien(donGia));
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            hienThongBao(Alert.AlertType.ERROR, "Lỗi tra giá theo cân nặng", e.getMessage());
        }
    }

    private String hienThiBacKemKhoangCan(String bac) {
        if ("Nhỏ".equals(bac)) {
            return "Nhỏ (" + BangGiaDichVu.KHOANG_CAN_NANG_NHO + ")";
        }
        if ("Vừa".equals(bac)) {
            return "Vừa (" + BangGiaDichVu.KHOANG_CAN_NANG_VUA + ")";
        }
        if ("Lớn".equals(bac)) {
            return "Lớn (" + BangGiaDichVu.KHOANG_CAN_NANG_LON + ")";
        }
        return bac == null ? "" : bac;
    }

    private String layMaDichVuTuCombo(ComboBox<String> combo) {
        String giaTri = combo.getValue();
        if (giaTri == null || !giaTri.contains(" - ")) {
            return "";
        }
        return giaTri.split(" - ")[0].trim();
    }

    private String timTenDichVuTheoMa(String maDichVu) {
        if (dsDichVu == null) {
            return "";
        }
        for (DichVuChamSoc dv : dsDichVu) {
            if (dv.getMaDichVu().equals(maDichVu)) {
                return dv.getTenDichVu();
            }
        }
        return "";
    }

    private String dinhDangTien(double soTien) {
        return ThemDichVuView.dinhDangTien(soTien);
    }

    private void hienThongBao(Alert.AlertType loai, String tieuDe, String noiDung) {
        Alert alert = new Alert(loai);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
