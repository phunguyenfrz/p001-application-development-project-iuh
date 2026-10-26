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
 * Màn hình xem và lọc bảng giá dịch vụ chăm sóc theo 3 bậc cân nặng (SRV_PRICING).
 */
public class BangGiaDichVuView extends VBox {

    private final DichVuChamSocController dichVuController;
    private final ObservableList<BangGiaDichVu> dsBangGia;
    private List<BangGiaDichVu> dsBangGiaGoc;
    private List<DichVuChamSoc> dsDichVu;

    private ComboBox<String> cboLocDichVu;
    private ComboBox<String> cboLocKichThuoc;
    private Button btnLamMoiLoc;
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

        Label lblMoTa = new Label("Xem bảng giá dịch vụ chăm sóc theo 3 bậc cân nặng ("
                + "Nhỏ: " + BangGiaDichVu.KHOANG_CAN_NANG_NHO + ", "
                + "Vừa: " + BangGiaDichVu.KHOANG_CAN_NANG_VUA + ", "
                + "Lớn: " + BangGiaDichVu.KHOANG_CAN_NANG_LON + ").");
        lblMoTa.getStyleClass().add("mo-ta-phan-he");

        VBox pnlBang = taoKhungBang();
        VBox.setVgrow(pnlBang, Priority.ALWAYS);

        getChildren().addAll(lblTieuDe, lblMoTa, pnlBang);
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
        cboLocDichVu.setPrefWidth(250);
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
                ThemDichVuView.dinhDangTien(cell.getValue().getGia())
        ));

        TableColumn<BangGiaDichVu, String> colNgayApDung = new TableColumn<>("Ngày áp dụng");
        colNgayApDung.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getNgayApDung() == null ? "" : cell.getValue().getNgayApDung().toString()
        ));

        TableColumn<BangGiaDichVu, String> colNgayHetHan = new TableColumn<>("Trạng thái hiệu lực");
        colNgayHetHan.setCellValueFactory(cell -> new SimpleStringProperty(
                hienThiTrangThaiHieuLuc(cell.getValue())
        ));

        tblBangGia.getColumns().addAll(List.of(
                colMaBangGia, colMaDichVu, colTenDichVu, colKichThuoc, colGia, colNgayApDung, colNgayHetHan
        ));

        // Làm mờ các dòng bảng giá của dịch vụ đã ngừng kinh doanh hoặc đã hết hiệu lực
        tblBangGia.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(BangGiaDichVu item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setOpacity(1.0);
                } else {
                    boolean conHieuLuc = laDichVuDangKinhDoanh(item.getMaDichVu()) && item.getNgayHetHieuLuc() == null;
                    setOpacity(conHieuLuc ? 1.0 : 0.5);
                }
            }
        });

        VBox.setVgrow(tblBangGia, Priority.ALWAYS);
        pnlBang.getChildren().addAll(pnlBoLoc, tblBangGia);
        return pnlBang;
    }

    // Tải danh sách dịch vụ vào ComboBox lọc
    private void taiDanhSachDichVu() {
        try {
            dsDichVu = dichVuController.layDanhSachDichVu();
            cboLocDichVu.getItems().clear();
            cboLocDichVu.getItems().add("Tất cả dịch vụ");

            for (DichVuChamSoc dv : dsDichVu) {
                cboLocDichVu.getItems().add(dv.getMaDichVu() + " - " + dv.getTenDichVu());
            }
            cboLocDichVu.setValue("Tất cả dịch vụ");
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
        taiLaiBang();
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

    private boolean laDichVuDangKinhDoanh(String maDichVu) {
        if (dsDichVu == null) {
            return true;
        }
        for (DichVuChamSoc dv : dsDichVu) {
            if (dv.getMaDichVu().equals(maDichVu)) {
                return dv.isTrangThai();
            }
        }
        return true;
    }

    private String hienThiTrangThaiHieuLuc(BangGiaDichVu bg) {
        if (!laDichVuDangKinhDoanh(bg.getMaDichVu())) {
            if (bg.getNgayHetHieuLuc() != null) {
                return "Ngừng kinh doanh (" + bg.getNgayHetHieuLuc() + ")";
            }
            return "Ngừng kinh doanh";
        }
        if (bg.getNgayHetHieuLuc() == null) {
            return "Đang áp dụng";
        }
        return "Hết hạn: " + bg.getNgayHetHieuLuc();
    }

    private void hienThongBao(Alert.AlertType loai, String tieuDe, String noiDung) {
        Alert alert = new Alert(loai);
        alert.setTitle(tieuDe);
        alert.setHeaderText(null);
        alert.setContentText(noiDung);
        alert.showAndWait();
    }
}
