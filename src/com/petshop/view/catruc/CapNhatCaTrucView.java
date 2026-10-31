package com.petshop.view.catruc;

import com.petshop.controller.CaTrucController;
import com.petshop.model.Ca;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình danh sách và quản lý ca trực: Thêm mới, Sửa ca trực, Ngừng áp dụng (xóa mềm).
 * Tuân thủ chuẩn thiết kế phân hệ Dịch vụ của Happy Tails.
 */
public class CapNhatCaTrucView extends VBox {

    private static final DateTimeFormatter DINH_DANG_GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final CaTrucController caTrucController = new CaTrucController();

    private final Button btnThemMoi = new Button("Thêm mới");
    private final Button btnCapNhat = new Button("Cập nhật");
    private final Button btnXoa = new Button("Xóa");
    private final Button btnLamMoi = new Button("Làm mới");

    private final TableView<Ca> tblDanhSach = new TableView<>();

    public CapNhatCaTrucView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("CA TRỰC - QUẢN LÝ DANH MỤC CA TRỰC");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Bấm Thêm mới để tạo ca trực; chọn một dòng trong bảng để Cập nhật hoặc Xóa (ngừng áp dụng).");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeMan, pnlBang);

        capNhatTrangThaiNut(null);
        taiLaiBang();
    }

    private HBox taoThanhTacVu() {
        HBox pnlThanhNut = new HBox(10);
        pnlThanhNut.getStyleClass().add("thanh-tac-vu");
        pnlThanhNut.setAlignment(Pos.CENTER_LEFT);

        Label lblDanhSach = new Label("Danh sách ca trực");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        btnThemMoi.getStyleClass().add("nut-chinh");
        btnThemMoi.setOnAction(e -> moCuaSoThemMoi());

        btnCapNhat.getStyleClass().add("nut-phu");
        btnCapNhat.setOnAction(e -> moCuaSoCapNhat());

        btnXoa.getStyleClass().add("nut-xoa");
        btnXoa.setOnAction(e -> xuLyXoaMem());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xuLyLamMoi());

        pnlThanhNut.getChildren().addAll(lblDanhSach, pnlKhoangTrong, btnThemMoi, btnCapNhat, btnXoa, btnLamMoi);
        return pnlThanhNut;
    }

    private VBox taoKhungBangDanhSach() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        HBox pnlThanhNut = taoThanhTacVu();

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có ca trực nào trong cơ sở dữ liệu"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<Ca, String> colMa = new TableColumn<>("Mã ca");
        colMa.setPrefWidth(180);
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaCa()));

        TableColumn<Ca, String> colTen = new TableColumn<>("Tên ca trực");
        colTen.setPrefWidth(220);
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenCa()));

        TableColumn<Ca, String> colGioBatDau = new TableColumn<>("Giờ bắt đầu");
        colGioBatDau.setPrefWidth(140);
        colGioBatDau.setStyle("-fx-alignment: CENTER;");
        colGioBatDau.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGioBatDauDinhDang()));

        TableColumn<Ca, String> colGioKetThuc = new TableColumn<>("Giờ kết thúc");
        colGioKetThuc.setPrefWidth(140);
        colGioKetThuc.setStyle("-fx-alignment: CENTER;");
        colGioKetThuc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGioKetThucDinhDang()));

        TableColumn<Ca, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(150);
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));

        tblDanhSach.getColumns().addAll(colMa, colTen, colGioBatDau, colGioKetThuc, colTrangThai);
        tblDanhSach.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tblDanhSach.setRowFactory(tv -> {
            TableRow<Ca> row = new TableRow<>();
            row.itemProperty().addListener((obs, cu, moi) -> {
                if (moi == null) {
                    row.setStyle("");
                } else if (!moi.isDangApDung()) {
                    row.setStyle("-fx-opacity: 0.55;");
                } else {
                    row.setStyle("");
                }
            });
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    moCuaSoCapNhat();
                }
            });
            return row;
        });

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));

        pnlKhung.getChildren().addAll(pnlThanhNut, tblDanhSach);
        return pnlKhung;
    }

    private void capNhatTrangThaiNut(Ca caDuocChon) {
        boolean coChon = caDuocChon != null;
        btnCapNhat.setDisable(!coChon);
        btnXoa.setDisable(!coChon || !caDuocChon.isDangApDung());
    }

    private void moCuaSoThemMoi() {
        ThemCaTrucView viewThem = new ThemCaTrucView(this::taiLaiBang);
        Stage cuaSo = new Stage();
        cuaSo.setTitle("Thêm ca trực mới");
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        cuaSo.setResizable(false);

        Scene scene = new Scene(viewThem, 560, 360);
        ganCssChung(scene);
        cuaSo.setScene(scene);
        cuaSo.centerOnScreen();
        cuaSo.showAndWait();
    }

    private void moCuaSoCapNhat() {
        Ca caDuocChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (caDuocChon == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một ca trực trong bảng để cập nhật!", ButtonType.OK).showAndWait();
            return;
        }

        Stage cuaSo = new Stage();
        cuaSo.setTitle("Cập nhật ca trực - " + caDuocChon.getMaCa());
        cuaSo.initModality(Modality.APPLICATION_MODAL);
        cuaSo.setResizable(false);

        VBox pnlKhung = new VBox(14);
        pnlKhung.setPadding(new Insets(18));
        pnlKhung.getStyleClass().add("khung-phan-he");

        VBox pnlTieuDe = new VBox(4);
        Label lblTieuDe = new Label("CẬP NHẬT CA TRỰC");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Chỉnh sửa thông tin tên ca và khung giờ làm việc.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDe.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlHop = new VBox(14);
        pnlHop.getStyleClass().add("hop-noi-dung");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(12);
        pnlLuoi.setVgap(10);

        TextField txtMa = new TextField(caDuocChon.getMaCa());
        txtMa.setEditable(false);
        txtMa.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280;");

        TextField txtTen = new TextField(caDuocChon.getTenCa());
        TextField txtBatDau = new TextField(caDuocChon.getGioBatDauDinhDang());
        TextField txtKetThuc = new TextField(caDuocChon.getGioKetThucDinhDang());

        ComboBox<String> cboTrangThai = new ComboBox<>(FXCollections.observableArrayList(
                Ca.TRANG_THAI_AP_DUNG, Ca.TRANG_THAI_NGUNG
        ));
        cboTrangThai.setValue(caDuocChon.getTrangThai());

        pnlLuoi.add(new Label("Mã ca trực:"), 0, 0);
        pnlLuoi.add(txtMa, 1, 0);
        pnlLuoi.add(new Label("Tên ca trực (*):"), 0, 1);
        pnlLuoi.add(txtTen, 1, 1);
        pnlLuoi.add(new Label("Giờ bắt đầu (*):"), 0, 2);
        pnlLuoi.add(txtBatDau, 1, 2);
        pnlLuoi.add(new Label("Giờ kết thúc (*):"), 0, 3);
        pnlLuoi.add(txtKetThuc, 1, 3);
        pnlLuoi.add(new Label("Trạng thái:"), 0, 4);
        pnlLuoi.add(cboTrangThai, 1, 4);

        ColumnConstraints colNhan = new ColumnConstraints();
        colNhan.setMinWidth(150);
        ColumnConstraints colNhap = new ColumnConstraints();
        colNhap.setHgrow(Priority.ALWAYS);
        pnlLuoi.getColumnConstraints().addAll(colNhan, colNhap);

        HBox pnlNut = new HBox(10);
        pnlNut.setAlignment(Pos.CENTER_RIGHT);

        Button btnLuu = new Button("Lưu thay đổi");
        btnLuu.getStyleClass().add("nut-chinh");

        Button btnDong = new Button("Đóng");
        btnDong.getStyleClass().add("nut-phu");
        btnDong.setOnAction(e -> cuaSo.close());

        btnLuu.setOnAction(e -> {
            try {
                String ten = txtTen.getText() != null ? txtTen.getText().trim() : "";
                String strBatDau = txtBatDau.getText() != null ? txtBatDau.getText().trim() : "";
                String strKetThuc = txtKetThuc.getText() != null ? txtKetThuc.getText().trim() : "";

                if (ten.isEmpty()) throw new IllegalArgumentException("Tên ca trực không được để trống!");
                if (strBatDau.isEmpty()) throw new IllegalArgumentException("Giờ bắt đầu không được để trống!");
                if (strKetThuc.isEmpty()) throw new IllegalArgumentException("Giờ kết thúc không được để trống!");

                LocalTime gioBatDau;
                LocalTime gioKetThuc;
                try {
                    gioBatDau = LocalTime.parse(strBatDau, DINH_DANG_GIO);
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("Giờ bắt đầu không đúng định dạng HH:mm (VD: 08:00)!");
                }
                try {
                    gioKetThuc = LocalTime.parse(strKetThuc, DINH_DANG_GIO);
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("Giờ kết thúc không đúng định dạng HH:mm (VD: 12:00)!");
                }

                Ca caCapNhat = new Ca(caDuocChon.getMaCa(), ten, gioBatDau, gioKetThuc, cboTrangThai.getValue());
                boolean ok = caTrucController.capNhatCa(caCapNhat);
                if (ok) {
                    Alert tb = new Alert(Alert.AlertType.INFORMATION, "Cập nhật ca trực thành công!", ButtonType.OK);
                    tb.setHeaderText(null);
                    tb.showAndWait();
                    taiLaiBang();
                    cuaSo.close();
                } else {
                    throw new IllegalStateException("Không thể cập nhật ca trực!");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                Alert tb = new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK);
                tb.setHeaderText("Thông báo");
                tb.showAndWait();
            }
        });

        pnlNut.getChildren().addAll(btnLuu, btnDong);
        pnlHop.getChildren().addAll(pnlLuoi, pnlNut);
        pnlKhung.getChildren().addAll(pnlTieuDe, pnlHop);

        Scene scene = new Scene(pnlKhung, 560, 380);
        ganCssChung(scene);
        cuaSo.setScene(scene);
        cuaSo.centerOnScreen();
        cuaSo.showAndWait();
    }

    private void xuLyXoaMem() {
        Ca caDuocChon = tblDanhSach.getSelectionModel().getSelectedItem();
        if (caDuocChon == null) return;

        Alert hopThoai = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Bạn có chắc chắn muốn ngừng áp dụng ca trực '" + caDuocChon.getTenCa() + "' (" + caDuocChon.getKhungGio() + ")?\n"
                        + "Sau khi ngừng áp dụng, ca này sẽ không thể phân công cho nhân viên.",
                ButtonType.YES, ButtonType.NO
        );
        hopThoai.setHeaderText("Xác nhận ngừng áp dụng ca trực");
        Optional<ButtonType> ketQua = hopThoai.showAndWait();

        if (ketQua.isPresent() && ketQua.get() == ButtonType.YES) {
            try {
                boolean thanhCong = caTrucController.xoaMemCa(caDuocChon.getMaCa());
                if (thanhCong) {
                    Alert tb = new Alert(Alert.AlertType.INFORMATION, "Đã ngừng áp dụng ca trực thành công!", ButtonType.OK);
                    tb.setHeaderText(null);
                    tb.showAndWait();
                    taiLaiBang();
                } else {
                    throw new IllegalStateException("Không thể cập nhật trạng thái ca trực!");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                Alert tb = new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK);
                tb.setHeaderText("Thông báo");
                tb.showAndWait();
            }
        }
    }

    private void xuLyLamMoi() {
        taiLaiBang();
        tblDanhSach.getSelectionModel().clearSelection();
    }

    private void taiLaiBang() {
        try {
            List<Ca> danhSach = caTrucController.layDanhSachCa();
            tblDanhSach.setItems(FXCollections.observableArrayList(danhSach));
            capNhatTrangThaiNut(tblDanhSach.getSelectionModel().getSelectedItem());
        } catch (Exception ex) {
            Alert tb = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải danh sách ca trực: " + ex.getMessage(), ButtonType.OK);
            tb.setHeaderText("Lỗi tải dữ liệu");
            tb.showAndWait();
        }
    }

    private void ganCssChung(Scene scene) {
        try {
            var cssChung = getClass().getResource("/com/petshop/view/css/chung.css");
            if (cssChung != null) {
                scene.getStylesheets().add(cssChung.toExternalForm());
            }
        } catch (Exception ignored) {
        }
    }
}

