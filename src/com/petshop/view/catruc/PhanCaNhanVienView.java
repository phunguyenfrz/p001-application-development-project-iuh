package com.petshop.view.catruc;

import com.petshop.controller.CaTrucController;
import com.petshop.model.Ca;
import com.petshop.model.NhanVien;
import com.petshop.model.PhanCa;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Màn hình phân công ca trực cho nhân viên theo ngày (SHF_ASSIGN).
 * Thiết kế giao diện và hệ thống nút chức năng chuẩn theo phân hệ Dịch vụ của Happy Tails.
 */
public class PhanCaNhanVienView extends VBox {

    private final CaTrucController caTrucController = new CaTrucController();

    private final DatePicker dpNgayLam = new DatePicker(LocalDate.now());
    private final ComboBox<Ca> cboCaTruc = new ComboBox<>();
    private final ComboBox<NhanVien> cboNhanVien = new ComboBox<>();

    private final Button btnPhanCa = new Button("Phân công ca");
    private final Button btnHuyPhanCa = new Button("Hủy phân ca");
    private final Button btnLamMoi = new Button("Làm mới");

    private final TableView<PhanCa> tblDanhSach = new TableView<>();

    public PhanCaNhanVienView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("CA TRỰC - PHÂN CÔNG CA LÀM VIỆC");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Chọn ngày, ca trực và nhân viên để phân công. Hệ thống tự động kiểm tra trùng ca.");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");
        pnlTieuDeMan.getChildren().addAll(lblTieuDe, lblMoTa);

        VBox pnlKhungNhap = taoKhungPhanCong();
        VBox pnlKhungBang = taoKhungBangDanhSach();
        VBox.setVgrow(pnlKhungBang, Priority.ALWAYS);

        getChildren().addAll(pnlTieuDeMan, pnlKhungNhap, pnlKhungBang);

        napDuLieuDropdown();
        taiLaiBang();
        capNhatTrangThaiNut(null);
    }

    private VBox taoKhungPhanCong() {
        VBox pnlHop = new VBox(12);
        pnlHop.getStyleClass().add("hop-noi-dung");

        Label lblTieuDePhanCong = new Label("Thông tin phân công");
        lblTieuDePhanCong.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        GridPane pnlLuoi = new GridPane();
        pnlLuoi.setHgap(16);
        pnlLuoi.setVgap(10);

        dpNgayLam.setPrefWidth(200);
        dpNgayLam.valueProperty().addListener((obs, cu, moi) -> {
            if (moi != null) {
                taiLaiBangTheoNgay(moi);
            }
        });

        cboCaTruc.setPromptText("-- Chọn ca trực --");
        cboCaTruc.setPrefWidth(260);

        cboNhanVien.setPromptText("-- Chọn nhân viên --");
        cboNhanVien.setPrefWidth(280);

        pnlLuoi.add(new Label("Ngày làm việc (*):"), 0, 0);
        pnlLuoi.add(dpNgayLam, 1, 0);

        pnlLuoi.add(new Label("Ca trực (*):"), 2, 0);
        pnlLuoi.add(cboCaTruc, 3, 0);

        pnlLuoi.add(new Label("Nhân viên (*):"), 4, 0);
        pnlLuoi.add(cboNhanVien, 5, 0);

        HBox pnlThanhNut = new HBox(10);
        pnlThanhNut.setAlignment(Pos.CENTER_RIGHT);

        btnPhanCa.getStyleClass().add("nut-chinh");
        btnPhanCa.setOnAction(e -> xuLyPhanCa());

        btnHuyPhanCa.getStyleClass().add("nut-xoa");
        btnHuyPhanCa.setOnAction(e -> xuLyHuyPhanCa());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xuLyLamMoi());

        pnlThanhNut.getChildren().addAll(btnPhanCa, btnHuyPhanCa, btnLamMoi);

        pnlHop.getChildren().addAll(lblTieuDePhanCong, pnlLuoi, pnlThanhNut);
        return pnlHop;
    }

    private VBox taoKhungBangDanhSach() {
        VBox pnlKhung = new VBox(10);
        pnlKhung.getStyleClass().add("hop-noi-dung");

        HBox pnlThanhTacVu = new HBox(10);
        pnlThanhTacVu.getStyleClass().add("thanh-tac-vu");
        pnlThanhTacVu.setAlignment(Pos.CENTER_LEFT);

        Label lblDanhSach = new Label("Danh sách phân ca trong ngày");
        lblDanhSach.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        Region pnlKhoangTrong = new Region();
        HBox.setHgrow(pnlKhoangTrong, Priority.ALWAYS);

        Button btnXemTatCa = new Button("Xem tất cả ngày");
        btnXemTatCa.getStyleClass().add("nut-phu");
        btnXemTatCa.setOnAction(e -> taiLaiBangTatCa());

        pnlThanhTacVu.getChildren().addAll(lblDanhSach, pnlKhoangTrong, btnXemTatCa);

        tblDanhSach.getStyleClass().add("bang-du-lieu");
        tblDanhSach.setPlaceholder(new Label("Chưa có lịch phân ca trong ngày được chọn"));
        VBox.setVgrow(tblDanhSach, Priority.ALWAYS);

        TableColumn<PhanCa, String> colMa = new TableColumn<>("Mã phân công");
        colMa.setPrefWidth(160);
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaPhanCa()));

        TableColumn<PhanCa, String> colNgay = new TableColumn<>("Ngày làm");
        colNgay.setPrefWidth(120);
        colNgay.setStyle("-fx-alignment: CENTER;");
        colNgay.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNgayLamDinhDang()));

        TableColumn<PhanCa, String> colCa = new TableColumn<>("Ca trực");
        colCa.setPrefWidth(150);
        colCa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenCa()));

        TableColumn<PhanCa, String> colKhungGio = new TableColumn<>("Khung giờ");
        colKhungGio.setPrefWidth(140);
        colKhungGio.setStyle("-fx-alignment: CENTER;");
        colKhungGio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKhungGio()));

        TableColumn<PhanCa, String> colNV = new TableColumn<>("Nhân viên");
        colNV.setPrefWidth(220);
        colNV.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTenNhanVien() + " (" + d.getValue().getMaNhanVien() + ")"
        ));

        TableColumn<PhanCa, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setPrefWidth(140);
        colTrangThai.setStyle("-fx-alignment: CENTER;");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));

        tblDanhSach.getColumns().addAll(colMa, colNgay, colCa, colKhungGio, colNV, colTrangThai);
        tblDanhSach.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tblDanhSach.setRowFactory(tv -> {
            TableRow<PhanCa> row = new TableRow<>();
            row.itemProperty().addListener((obs, cu, moi) -> {
                if (moi == null) {
                    row.setStyle("");
                } else if (!moi.isDaPhanCong()) {
                    row.setStyle("-fx-opacity: 0.55;");
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });

        tblDanhSach.getSelectionModel().selectedItemProperty().addListener((obs, cu, moi) -> capNhatTrangThaiNut(moi));

        pnlKhung.getChildren().addAll(pnlThanhTacVu, tblDanhSach);
        return pnlKhung;
    }

    private void capNhatTrangThaiNut(PhanCa phanCaDuocChon) {
        boolean coChon = phanCaDuocChon != null;
        btnHuyPhanCa.setDisable(!coChon || !phanCaDuocChon.isDaPhanCong());
    }

    private void xuLyPhanCa() {
        try {
            LocalDate ngay = dpNgayLam.getValue();
            if (ngay == null) {
                throw new IllegalArgumentException("Vui lòng chọn ngày làm việc!");
            }

            Ca ca = cboCaTruc.getValue();
            if (ca == null) {
                throw new IllegalArgumentException("Vui lòng chọn ca trực!");
            }

            NhanVien nv = cboNhanVien.getValue();
            if (nv == null) {
                throw new IllegalArgumentException("Vui lòng chọn nhân viên!");
            }

            String maPhanCa = caTrucController.taoMaPhanCa();
            PhanCa phanCaMoi = new PhanCa(maPhanCa, nv.getMaNhanVien(), ca.getMaCa(), ngay, PhanCa.TRANG_THAI_DA_PHAN_CONG);
            phanCaMoi.setTenNhanVien(nv.getHoTen());
            phanCaMoi.setTenCa(ca.getTenCa());
            phanCaMoi.setGioBatDau(ca.getGioBatDau());
            phanCaMoi.setGioKetThuc(ca.getGioKetThuc());

            boolean ok = caTrucController.phanCa(phanCaMoi);
            if (ok) {
                Alert tb = new Alert(Alert.AlertType.INFORMATION, "Phân ca cho nhân viên '" + nv.getHoTen() + "' thành công!", ButtonType.OK);
                tb.setHeaderText(null);
                tb.showAndWait();

                taiLaiBangTheoNgay(ngay);
            } else {
                throw new IllegalStateException("Không thể ghi nhận phân ca vào cơ sở dữ liệu!");
            }
        } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
            Alert tb = new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK);
            tb.setHeaderText("Thông báo");
            tb.showAndWait();
        } catch (Exception ex) {
            Alert tb = new Alert(Alert.AlertType.ERROR, "Lỗi không xác định: " + ex.getMessage(), ButtonType.OK);
            tb.setHeaderText("Lỗi");
            tb.showAndWait();
        }
    }

    private void xuLyHuyPhanCa() {
        PhanCa pc = tblDanhSach.getSelectionModel().getSelectedItem();
        if (pc == null) return;

        Alert hopThoai = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn hủy phân ca '" + pc.getTenCa() + "' của nhân viên '" + pc.getTenNhanVien() + "' ngày " + pc.getNgayLamDinhDang() + "?",
                ButtonType.YES, ButtonType.NO
        );
        hopThoai.setHeaderText("Xác nhận hủy phân công ca trực");
        Optional<ButtonType> ketQua = hopThoai.showAndWait();

        if (ketQua.isPresent() && ketQua.get() == ButtonType.YES) {
            try {
                boolean ok = caTrucController.huyPhanCa(pc.getMaPhanCa());
                if (ok) {
                    Alert tb = new Alert(Alert.AlertType.INFORMATION, "Đã hủy phân ca thành công!", ButtonType.OK);
                    tb.setHeaderText(null);
                    tb.showAndWait();

                    if (dpNgayLam.getValue() != null) {
                        taiLaiBangTheoNgay(dpNgayLam.getValue());
                    } else {
                        taiLaiBang();
                    }
                } else {
                    throw new IllegalStateException("Không thể cập nhật trạng thái phân ca!");
                }
            } catch (IllegalArgumentException | IllegalStateException | SecurityException ex) {
                Alert tb = new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK);
                tb.setHeaderText("Thông báo");
                tb.showAndWait();
            }
        }
    }

    private void xuLyLamMoi() {
        napDuLieuDropdown();
        if (dpNgayLam.getValue() != null) {
            taiLaiBangTheoNgay(dpNgayLam.getValue());
        } else {
            taiLaiBang();
        }
        tblDanhSach.getSelectionModel().clearSelection();
    }

    private void napDuLieuDropdown() {
        try {
            List<Ca> dsCa = caTrucController.layDanhSachCaDangApDung();
            cboCaTruc.setItems(FXCollections.observableArrayList(dsCa));
            if (!dsCa.isEmpty()) {
                cboCaTruc.setValue(dsCa.get(0));
            }

            List<NhanVien> dsNV = caTrucController.layDanhSachNhanVienDangLamViec();
            cboNhanVien.setItems(FXCollections.observableArrayList(dsNV));
            if (!dsNV.isEmpty()) {
                cboNhanVien.setValue(dsNV.get(0));
            }
        } catch (Exception ex) {
            // Dropdown load error handled gracefully
        }
    }

    private void taiLaiBangTheoNgay(LocalDate ngay) {
        try {
            List<PhanCa> danhSach = caTrucController.layPhanCaTheoNgay(ngay);
            tblDanhSach.setItems(FXCollections.observableArrayList(danhSach));
            capNhatTrangThaiNut(tblDanhSach.getSelectionModel().getSelectedItem());
        } catch (Exception ex) {
            Alert tb = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải danh sách phân ca: " + ex.getMessage(), ButtonType.OK);
            tb.setHeaderText("Lỗi tải dữ liệu");
            tb.showAndWait();
        }
    }

    private void taiLaiBangTatCa() {
        try {
            List<PhanCa> danhSach = caTrucController.layDanhSachPhanCa();
            tblDanhSach.setItems(FXCollections.observableArrayList(danhSach));
            capNhatTrangThaiNut(tblDanhSach.getSelectionModel().getSelectedItem());
        } catch (Exception ex) {
            Alert tb = new Alert(Alert.AlertType.ERROR, "Lỗi khi tải tất cả phân ca: " + ex.getMessage(), ButtonType.OK);
            tb.setHeaderText("Lỗi tải dữ liệu");
            tb.showAndWait();
        }
    }

    private void taiLaiBang() {
        if (dpNgayLam.getValue() != null) {
            taiLaiBangTheoNgay(dpNgayLam.getValue());
        } else {
            taiLaiBangTatCa();
        }
    }
}

