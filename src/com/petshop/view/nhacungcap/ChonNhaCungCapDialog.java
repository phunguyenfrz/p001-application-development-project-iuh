package com.petshop.view.nhacungcap;

import com.petshop.controller.NhaCungCapController;
import com.petshop.model.NhaCungCap;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;

/**
 * Hộp thoại lựa chọn Nhà cung cấp (ChonNhaCungCapDialog).
 * - Phục vụ màn hình lập đơn nhập (#67) và các phân hệ khác.
 * - Cho phép các vai trò QUAN_LY, BAN_HANG, ADMIN tìm kiếm và chọn nhà cung cấp.
 * - Chỉ hiển thị nhà cung cấp đang hoạt động (trangThai = 1).
 */
public class ChonNhaCungCapDialog extends Stage {

    private final NhaCungCapController controller = new NhaCungCapController();

    private final TextField txtTuKhoa = new TextField();
    private final TableView<NhaCungCap> tblDanhSach = new TableView<>();
    private final Button btnChon = new Button("Chọn nhà cung cấp");
    private final Button btnHuy = new Button("Hủy bỏ");
    private final Label lblKetQua = new Label("Tổng số: 0 nhà cung cấp");

    private NhaCungCap nhaCungCapDuocChon = null;

    public ChonNhaCungCapDialog() {
        this(null);
    }

    public ChonNhaCungCapDialog(Window owner) {
        initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            initOwner(owner);
        }
        setTitle("Chọn nhà cung cấp");

        VBox root = new VBox(14);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("khung-phan-he");

        Label lblTieuDe = new Label("CHỌN NHÀ CUNG CẤP");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Tìm kiếm và chọn nhà cung cấp đối tác đang hoạt động để đưa vào chứng từ.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        VBox khungChinh = taoKhungChinh();
        VBox.setVgrow(khungChinh, Priority.ALWAYS);

        root.getChildren().addAll(lblTieuDe, lblMoTa, khungChinh);

        Scene scene = new Scene(root, 760, 500);
        try {
            scene.getStylesheets().add(getClass().getResource("/com/petshop/view/css/chung.css").toExternalForm());
        } catch (Exception ignored) {
        }
        setScene(scene);

        capNhatTrangThaiNut(null);
        taiDuLieu();
    }

    private VBox taoKhungChinh() {
        VBox khung = new VBox(12);
        khung.getStyleClass().add("hop-noi-dung");
        VBox.setVgrow(khung, Priority.ALWAYS);

        // Thanh tìm kiếm
        HBox thanhTim = new HBox(10);
        thanhTim.setAlignment(Pos.CENTER_LEFT);

        txtTuKhoa.setPromptText("Nhập mã, tên hoặc SĐT nhà cung cấp...");
        txtTuKhoa.setPrefWidth(320);

        Button btnTim = new Button("Tìm kiếm");
        btnTim.getStyleClass().add("nut-chinh");
        Button btnLamMoi = new Button("Làm mới");
        btnLamMoi.getStyleClass().add("nut-phu");

        btnTim.setOnAction(e -> xuLyTimKiem());
        txtTuKhoa.setOnAction(e -> xuLyTimKiem());
        btnLamMoi.setOnAction(e -> {
            txtTuKhoa.clear();
            taiDuLieu();
        });

        thanhTim.getChildren().addAll(new Label("Từ khóa:"), txtTuKhoa, btnTim, btnLamMoi);

        // Bảng dữ liệu kết quả
        taoCotBang();
        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Không tìm thấy nhà cung cấp nào đang hoạt động"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));

        // Nhấp đúp chuột để chọn nhanh
        tblDanhSach.setRowFactory(tv -> {
            TableRow<NhaCungCap> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    nhaCungCapDuocChon = row.getItem();
                    close();
                }
            });
            return row;
        });

        // Thanh nút điều khiển cuối hộp thoại
        HBox thanhNut = new HBox(10);
        thanhNut.setAlignment(Pos.CENTER_RIGHT);

        btnChon.getStyleClass().add("nut-chinh");
        btnHuy.getStyleClass().add("nut-phu");

        btnChon.setOnAction(e -> xuLyChon());
        btnHuy.setOnAction(e -> {
            nhaCungCapDuocChon = null;
            close();
        });

        Region khoangTrong = new Region();
        HBox.setHgrow(khoangTrong, Priority.ALWAYS);

        thanhNut.getChildren().addAll(lblKetQua, khoangTrong, btnHuy, btnChon);

        khung.getChildren().addAll(thanhTim, tblDanhSach, thanhNut);
        return khung;
    }

    private void taoCotBang() {
        TableColumn<NhaCungCap, String> colMa = new TableColumn<>("Mã NCC");
        colMa.setPrefWidth(160);
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaNhaCungCap()));

        TableColumn<NhaCungCap, String> colTen = new TableColumn<>("Tên nhà cung cấp");
        colTen.setPrefWidth(220);
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenNhaCungCap()));

        TableColumn<NhaCungCap, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setPrefWidth(120);
        colSdt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSoDienThoai()));

        TableColumn<NhaCungCap, String> colEmail = new TableColumn<>("Email");
        colEmail.setPrefWidth(180);
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getEmail() != null ? d.getValue().getEmail() : ""
        ));

        TableColumn<NhaCungCap, String> colDiaChi = new TableColumn<>("Địa chỉ");
        colDiaChi.setPrefWidth(200);
        colDiaChi.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDiaChi() != null ? d.getValue().getDiaChi() : ""
        ));

        tblDanhSach.getColumns().setAll(List.of(colMa, colTen, colSdt, colEmail, colDiaChi));
    }

    private void capNhatTrangThaiNut(NhaCungCap ncc) {
        btnChon.setDisable(ncc == null);
    }

    private void taiDuLieu() {
        try {
            List<NhaCungCap> danhSach = controller.layDanhSachDangHoatDong();
            tblDanhSach.setItems(FXCollections.observableArrayList(danhSach));
            lblKetQua.setText("Tổng số: " + danhSach.size() + " nhà cung cấp");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void xuLyTimKiem() {
        try {
            String tuKhoa = txtTuKhoa.getText();
            List<NhaCungCap> danhSach = controller.timKiemChoDialog(tuKhoa);
            tblDanhSach.setItems(FXCollections.observableArrayList(danhSach));
            lblKetQua.setText("Tổng số: " + danhSach.size() + " nhà cung cấp");
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            hienThiLoi(ex.getMessage());
        }
    }

    private void xuLyChon() {
        NhaCungCap chon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (chon == null) {
            hienThiLoi("Vui lòng chọn một nhà cung cấp từ bảng dữ liệu!");
            return;
        }
        this.nhaCungCapDuocChon = chon;
        close();
    }

    /**
     * Mở hộp thoại chọn và trả về Optional chứa NhaCungCap được chọn.
     * @return Optional<NhaCungCap> hoặc Optional.empty() nếu hủy bỏ
     */
    public Optional<NhaCungCap> hienThiVaChon() {
        showAndWait();
        return Optional.ofNullable(nhaCungCapDuocChon);
    }

    /**
     * Phương thức tiện ích tĩnh mở nhanh hộp thoại chọn nhà cung cấp từ cửa sổ cha.
     */
    public static Optional<NhaCungCap> moDialog(Window owner) {
        ChonNhaCungCapDialog dialog = new ChonNhaCungCapDialog(owner);
        return dialog.hienThiVaChon();
    }

    private void hienThiLoi(String noiDung) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Thông báo lỗi");
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}

