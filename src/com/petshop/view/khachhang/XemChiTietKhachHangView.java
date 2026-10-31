package com.petshop.view.khachhang;

import com.petshop.dao.ThuCungDAO;
import com.petshop.model.KhachHang;
import com.petshop.model.ThuCung;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Hộp thoại (Modal Dialog) Xem chi tiết khách hàng và danh sách thú cưng của khách hàng.
 * Phụ thuộc Issue #63: lấy danh sách thú cưng theo mã khách hàng qua ThuCungDAO.
 * Hỗ trợ bôi đen (chọn văn bản bằng chuột) trên từng ô thông tin để sao chép (copy) dễ dàng.
 */
public class XemChiTietKhachHangView extends VBox {

    private static final String CSS_CHUNG = "/com/petshop/view/css/chung.css";
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // TODO: nối ThuCungService khi phân hệ Thú cưng merge theo Quy ước #60 (Mục 7.3)
    private final ThuCungDAO thuCungDAO = new ThuCungDAO();

    // Các ô hiển thị dữ liệu khách hàng (chỉ đọc, cho phép bôi đen copy)
    private final TextField txtMaKhachHang = new TextField();
    private final TextField txtHoTen = new TextField();
    private final TextField txtSoDienThoai = new TextField();
    private final TextField txtEmail = new TextField();
    private final TextField txtDiemTichLuy = new TextField();
    private final TextField txtDiaChi = new TextField();
    private final TextArea txtGhiChu = new TextArea();

    // Bảng danh sách thú cưng
    private final TableView<ThuCung> tblThuCung = new TableView<>();

    // Nút đóng hộp thoại
    private final Button btnDong = new Button("Đóng");

    public XemChiTietKhachHangView(KhachHang khachHang) {
        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("khung-phan-he");

        // 1. Tiêu đề hộp thoại
        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("THÔNG TIN CHI TIẾT KHÁCH HÀNG & THÚ CƯNG");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");

        String maKH = khachHang != null ? khachHang.getMaKhachHang() : "-";
        Label lblDangXem = new Label("Mã khách hàng: " + maKH);
        lblDangXem.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblDangXem);

        // 2. Khung thông tin cá nhân khách hàng
        VBox pnlThongTinKhachHang = taoKhungThongTinKhachHang(khachHang);

        // 3. Khung bảng danh sách thú cưng của khách hàng
        VBox pnlKhungThuCung = taoKhungDanhSachThuCung(khachHang);

        // 4. Nút Đóng
        HBox pnlNut = new HBox();
        pnlNut.setAlignment(Pos.CENTER_RIGHT);
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> dongCuaSo());
        pnlNut.getChildren().add(btnDong);

        getChildren().addAll(pnlTieuDe, pnlThongTinKhachHang, pnlKhungThuCung, pnlNut);
    }

    /**
     * Tạo khung lưới hiển thị thông tin chi tiết của khách hàng ở chế độ Chỉ đọc (cho phép bôi đen copy).
     */
    private VBox taoKhungThongTinKhachHang(KhachHang kh) {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        Label lblTieuDePhan = new Label("Thông tin cá nhân (có thể bôi đen sao chép)");
        lblTieuDePhan.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(14);
        pnlLuoi.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints(110);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(110);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(col1, col2, col3, col4);

        if (kh != null) {
            txtMaKhachHang.setText(kh.getMaKhachHang());
            txtHoTen.setText(kh.getHoTen());
            txtSoDienThoai.setText(kh.getSoDienThoai());
            txtEmail.setText(kh.getEmail() != null ? kh.getEmail() : "—");
            txtDiemTichLuy.setText(kh.getDiemTichLuy() + " điểm");
            txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "—");
            txtGhiChu.setText(kh.getGhiChu() != null ? kh.getGhiChu() : "—");
        }

        khoaChiDoc(txtMaKhachHang);
        khoaChiDoc(txtHoTen);
        khoaChiDoc(txtSoDienThoai);
        khoaChiDoc(txtEmail);
        khoaChiDoc(txtDiemTichLuy);
        khoaChiDoc(txtDiaChi);
        khoaChiDoc(txtGhiChu);

        txtGhiChu.setPrefRowCount(2);
        txtGhiChu.setWrapText(true);

        pnlLuoi.add(new Label("Mã khách hàng:"), 0, 0);
        pnlLuoi.add(txtMaKhachHang, 1, 0);
        pnlLuoi.add(new Label("Họ và tên:"), 2, 0);
        pnlLuoi.add(txtHoTen, 3, 0);

        pnlLuoi.add(new Label("Số điện thoại:"), 0, 1);
        pnlLuoi.add(txtSoDienThoai, 1, 1);
        pnlLuoi.add(new Label("Email:"), 2, 1);
        pnlLuoi.add(txtEmail, 3, 1);

        pnlLuoi.add(new Label("Điểm tích lũy:"), 0, 2);
        pnlLuoi.add(txtDiemTichLuy, 1, 2);
        pnlLuoi.add(new Label("Địa chỉ:"), 2, 2);
        pnlLuoi.add(txtDiaChi, 3, 2);

        pnlLuoi.add(new Label("Ghi chú:"), 0, 3);
        pnlLuoi.add(txtGhiChu, 1, 3, 3, 1);

        pnlKhung.getChildren().addAll(lblTieuDePhan, pnlLuoi);
        return pnlKhung;
    }

    /**
     * Tạo khung danh sách thú cưng của khách hàng.
     * Từng ô dữ liệu trong bảng cho phép nhấp chuột bôi đen trực tiếp văn bản để copy.
     */
    private VBox taoKhungDanhSachThuCung(KhachHang kh) {
        VBox pnlKhung = new VBox(8);
        pnlKhung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(pnlKhung, Priority.ALWAYS);

        Label lblTieuDeBang = new Label("Danh sách thú cưng sở hữu (có thể bôi đen sao chép từng ô)");
        lblTieuDeBang.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px; -fx-text-fill: #1e293b;");

        tblThuCung.getStyleClass().add("bang-du-lieu");
        tblThuCung.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblThuCung.setPrefHeight(160);
        tblThuCung.setPlaceholder(new Label("Khách hàng này hiện chưa đăng ký thú cưng nào."));

        TableColumn<ThuCung, String> colMaTC = new TableColumn<>("Mã thú cưng");
        colMaTC.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaThuCung()));
        colMaTC.setCellFactory(col -> taoOTextBoiDen(Pos.CENTER, true));
        colMaTC.setPrefWidth(140);

        TableColumn<ThuCung, String> colTenTC = new TableColumn<>("Tên thú cưng");
        colTenTC.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTenThuCung()));
        colTenTC.setCellFactory(col -> taoOTextBoiDen(Pos.CENTER_LEFT, true));
        colTenTC.setPrefWidth(120);

        TableColumn<ThuCung, String> colLoai = new TableColumn<>("Loài");
        colLoai.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLoaiThuCung() != null ? c.getValue().getLoaiThuCung() : "—"));
        colLoai.setCellFactory(col -> taoOTextBoiDen(Pos.CENTER, false));
        colLoai.setPrefWidth(90);

        TableColumn<ThuCung, String> colGiong = new TableColumn<>("Giống loài");
        colGiong.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGiongLoai() != null ? c.getValue().getGiongLoai() : "—"));
        colGiong.setCellFactory(col -> taoOTextBoiDen(Pos.CENTER_LEFT, false));
        colGiong.setPrefWidth(120);

        TableColumn<ThuCung, String> colGioiTinh = new TableColumn<>("Giới tính");
        colGioiTinh.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGioiTinh() != null ? c.getValue().getGioiTinh() : "—"));
        colGioiTinh.setCellFactory(col -> taoOTextBoiDen(Pos.CENTER, false));
        colGioiTinh.setPrefWidth(80);

        TableColumn<ThuCung, String> colCanNang = new TableColumn<>("Cân nặng");
        colCanNang.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCanNang() + " kg"));
        colCanNang.setCellFactory(col -> taoOTextBoiDen(Pos.CENTER_RIGHT, false));
        colCanNang.setPrefWidth(90);

        TableColumn<ThuCung, String> colNgaySinh = new TableColumn<>("Ngày sinh");
        colNgaySinh.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNgaySinh() != null ? c.getValue().getNgaySinh().format(DINH_DANG_NGAY) : "—"));
        colNgaySinh.setCellFactory(col -> taoOTextBoiDen(Pos.CENTER, false));
        colNgaySinh.setPrefWidth(100);

        TableColumn<ThuCung, String> colDiUng = new TableColumn<>("Dị ứng / Ghi chú");
        colDiUng.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDiUng() != null ? c.getValue().getDiUng() : "—"));
        colDiUng.setCellFactory(col -> taoOTextBoiDen(Pos.CENTER_LEFT, false));
        colDiUng.setPrefWidth(140);

        tblThuCung.getColumns().addAll(colMaTC, colTenTC, colLoai, colGiong, colGioiTinh, colCanNang, colNgaySinh, colDiUng);

        // Nạp dữ liệu thú cưng từ ThuCungDAO
        // TODO: nối ThuCungService khi phân hệ Thú cưng merge theo Quy ước #60 (Mục 7.3)
        if (kh != null && kh.getMaKhachHang() != null) {
            try {
                List<ThuCung> ds = thuCungDAO.layDanhSachTheoKhachHang(kh.getMaKhachHang());
                tblThuCung.setItems(FXCollections.observableArrayList(ds));
            } catch (Exception e) {
                tblThuCung.setItems(FXCollections.observableArrayList());
            }
        }

        pnlKhung.getChildren().addAll(lblTieuDeBang, tblThuCung);
        return pnlKhung;
    }

    /**
     * Tạo TableCell chứa ô văn bản TextField trong suốt, cho phép người dùng bôi đen chữ để copy.
     */
    private static TableCell<ThuCung, String> taoOTextBoiDen(Pos canLe, boolean inDam) {
        return new TableCell<>() {
            private final TextField txt = new TextField();

            {
                txt.setEditable(false);
                String style = "-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-border-width: 0; -fx-text-fill: #1e293b;";
                if (inDam) {
                    style += " -fx-font-weight: bold;";
                }
                txt.setStyle(style);
                txt.setAlignment(canLe);
                setAlignment(canLe);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    txt.setText(item);
                    setGraphic(txt);
                    setText(null);
                }
            }
        };
    }

    /**
     * Khóa chế độ chỉ đọc cho TextInputControl, vẫn cho phép nhấp chuột bôi đen và sao chép văn bản.
     */
    private void khoaChiDoc(TextInputControl oNhap) {
        oNhap.setEditable(false);
        oNhap.setStyle("-fx-opacity: 0.95; -fx-background-color: #f1f5f9; -fx-text-fill: #1e293b;");
    }

    /**
     * Đóng cửa sổ Stage chứa màn hình này.
     */
    private void dongCuaSo() {
        if (getScene() != null && getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    /**
     * Phương thức tĩnh tiện ích mở nhanh hộp thoại xem thông tin chi tiết khách hàng và thú cưng.
     *
     * @param khachHang Khách hàng cần xem thông tin
     * @param cuaSoCha Cửa sổ cha (Window owner) để hiển thị modal
     */
    public static void hienThi(KhachHang khachHang, Window cuaSoCha) {
        if (khachHang == null) {
            return;
        }

        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        if (cuaSoCha != null) {
            dlg.initOwner(cuaSoCha);
        }
        dlg.setTitle("Chi tiết khách hàng & Thú cưng - " + khachHang.getMaKhachHang());

        XemChiTietKhachHangView pnlNoiDung = new XemChiTietKhachHangView(khachHang);
        Scene scene = new Scene(pnlNoiDung, 750, 570);

        if (XemChiTietKhachHangView.class.getResource(CSS_CHUNG) != null) {
            scene.getStylesheets().add(XemChiTietKhachHangView.class.getResource(CSS_CHUNG).toExternalForm());
        }

        dlg.setScene(scene);
        dlg.setResizable(false);
        dlg.showAndWait();
    }
}
