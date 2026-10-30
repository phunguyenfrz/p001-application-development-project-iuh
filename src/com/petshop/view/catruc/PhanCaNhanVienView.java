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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Màn hình phân công ca trực cho nhân viên (SHF_ASSIGN).
 *
 * Hỗ trợ:
 * - Phân ca đơn lẻ (1 ngày, 1 hoặc nhiều ca) hoặc phân ca hàng loạt (khoảng ngày + thứ trong tuần).
 * - Chọn nhiều ca trực cùng lúc (CheckBox), miễn không trùng khung giờ.
 * - Chặn phân ca vào ngày quá khứ.
 */
public class PhanCaNhanVienView extends VBox {

    private final CaTrucController caTrucController = new CaTrucController();

    // --- Chế độ phân ca ---
    private final ToggleGroup chonCheDo = new ToggleGroup();
    private final RadioButton rdoDonLe = new RadioButton("Phân ca đơn lẻ (1 ngày)");
    private final RadioButton rdoHangLoat = new RadioButton("Phân ca hàng loạt (khoảng ngày)");

    // --- Phân ca đơn lẻ ---
    private final DatePicker dpNgayLam = new DatePicker(LocalDate.now());

    // --- Phân ca hàng loạt ---
    private final DatePicker dpTuNgay = new DatePicker(LocalDate.now());
    private final DatePicker dpDenNgay = new DatePicker(LocalDate.now().plusDays(6));
    private final CheckBox chkThu2 = new CheckBox("Thứ 2");
    private final CheckBox chkThu3 = new CheckBox("Thứ 3");
    private final CheckBox chkThu4 = new CheckBox("Thứ 4");
    private final CheckBox chkThu5 = new CheckBox("Thứ 5");
    private final CheckBox chkThu6 = new CheckBox("Thứ 6");
    private final CheckBox chkThu7 = new CheckBox("Thứ 7");
    private final CheckBox chkChuNhat = new CheckBox("Chủ nhật");
    private VBox pnlHangLoat;
    private VBox pnlDonLe;

    // --- Chọn nhân viên ---
    private final ComboBox<NhanVien> cboNhanVien = new ComboBox<>();

    // --- Chọn nhiều ca trực (CheckBox) ---
    private final VBox pnlDanhSachCa = new VBox(6);
    private final List<CheckBox> danhSachCheckCa = new ArrayList<>();
    private final Map<CheckBox, Ca> mapCheckCa = new LinkedHashMap<>();

    // --- Nút ---
    private final Button btnPhanCa = new Button("Phân công ca");
    private final Button btnHuyPhanCa = new Button("Hủy phân ca");
    private final Button btnLamMoi = new Button("Làm mới");

    // --- Bảng ---
    private final TableView<PhanCa> tblDanhSach = new TableView<>();

    public PhanCaNhanVienView() {
        setSpacing(14);
        getStyleClass().add("khung-phan-he");

        VBox pnlTieuDeMan = new VBox(4);
        Label lblTieuDe = new Label("CA TRỰC - PHÂN CÔNG CA LÀM VIỆC");
        lblTieuDe.getStyleClass().add("tieu-de-phan-he");
        Label lblMoTa = new Label("Phân ca đơn lẻ hoặc hàng loạt. Hỗ trợ chọn nhiều ca/ngày, kiểm tra trùng giờ và chặn ngày quá khứ.");
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

    // =======================================================
    //  TẠO KHUNG PHÂN CÔNG
    // =======================================================

    private VBox taoKhungPhanCong() {
        VBox pnlHop = new VBox(12);
        pnlHop.getStyleClass().add("hop-noi-dung");

        Label lblTieuDePhanCong = new Label("Thông tin phân công");
        lblTieuDePhanCong.setStyle("-fx-font-weight: bold; -fx-font-size: 13.5px;");

        // --- Chế độ phân ca ---
        HBox pnlCheDo = new HBox(20);
        pnlCheDo.setAlignment(Pos.CENTER_LEFT);
        rdoDonLe.setToggleGroup(chonCheDo);
        rdoHangLoat.setToggleGroup(chonCheDo);
        rdoDonLe.setSelected(true);
        pnlCheDo.getChildren().addAll(new Label("Chế độ:"), rdoDonLe, rdoHangLoat);

        // --- Panel đơn lẻ ---
        pnlDonLe = taoKhungDonLe();

        // --- Panel hàng loạt ---
        pnlHangLoat = taoKhungHangLoat();
        pnlHangLoat.setVisible(false);
        pnlHangLoat.setManaged(false);

        // Chuyển chế độ
        chonCheDo.selectedToggleProperty().addListener((obs, cu, moi) -> {
            boolean hangLoat = moi == rdoHangLoat;
            pnlDonLe.setVisible(!hangLoat);
            pnlDonLe.setManaged(!hangLoat);
            pnlHangLoat.setVisible(hangLoat);
            pnlHangLoat.setManaged(hangLoat);
        });

        // --- Chọn nhân viên ---
        HBox pnlNhanVien = new HBox(10);
        pnlNhanVien.setAlignment(Pos.CENTER_LEFT);
        cboNhanVien.setPromptText("-- Chọn nhân viên --");
        cboNhanVien.setPrefWidth(300);
        pnlNhanVien.getChildren().addAll(new Label("Nhân viên (*):"), cboNhanVien);

        // --- Chọn nhiều ca trực ---
        VBox pnlCa = taoKhungChonNhieuCa();

        // --- Nút ---
        HBox pnlThanhNut = new HBox(10);
        pnlThanhNut.setAlignment(Pos.CENTER_RIGHT);

        btnPhanCa.getStyleClass().add("nut-chinh");
        btnPhanCa.setOnAction(e -> xuLyPhanCa());

        btnHuyPhanCa.getStyleClass().add("nut-xoa");
        btnHuyPhanCa.setOnAction(e -> xuLyHuyPhanCa());

        btnLamMoi.getStyleClass().add("nut-phu");
        btnLamMoi.setOnAction(e -> xuLyLamMoi());

        pnlThanhNut.getChildren().addAll(btnPhanCa, btnHuyPhanCa, btnLamMoi);

        pnlHop.getChildren().addAll(lblTieuDePhanCong, pnlCheDo, pnlDonLe, pnlHangLoat, pnlNhanVien, pnlCa, pnlThanhNut);
        return pnlHop;
    }

    /**
     * Khung phân ca đơn lẻ: chỉ chọn 1 ngày.
     */
    private VBox taoKhungDonLe() {
        VBox pnl = new VBox(8);
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        dpNgayLam.setPrefWidth(200);
        // Chặn chọn ngày quá khứ trên DatePicker
        dpNgayLam.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #999999;");
                }
            }
        });
        dpNgayLam.valueProperty().addListener((obs, cu, moi) -> {
            if (moi != null) {
                taiLaiBangTheoNgay(moi);
            }
        });
        row.getChildren().addAll(new Label("Ngày làm việc (*):"), dpNgayLam);
        pnl.getChildren().add(row);
        return pnl;
    }

    /**
     * Khung phân ca hàng loạt: chọn khoảng ngày + các thứ trong tuần.
     */
    private VBox taoKhungHangLoat() {
        VBox pnl = new VBox(10);
        pnl.setStyle("-fx-padding: 8; -fx-border-color: #d0d0d0; -fx-border-radius: 4; -fx-background-color: #fafafa; -fx-background-radius: 4;");

        Label lblTieuDe = new Label("Phân ca hàng loạt theo khoảng ngày");
        lblTieuDe.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #1976D2;");

        // Khoảng ngày
        HBox pnlNgay = new HBox(10);
        pnlNgay.setAlignment(Pos.CENTER_LEFT);
        dpTuNgay.setPrefWidth(160);
        dpDenNgay.setPrefWidth(160);

        // Chặn chọn ngày quá khứ
        dpTuNgay.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #999999;");
                }
            }
        });
        dpDenNgay.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #999999;");
                }
            }
        });

        pnlNgay.getChildren().addAll(new Label("Từ ngày (*):"), dpTuNgay, new Label("Đến ngày (*):"), dpDenNgay);

        // Chọn thứ trong tuần
        HBox pnlThu = new HBox(12);
        pnlThu.setAlignment(Pos.CENTER_LEFT);
        pnlThu.getChildren().addAll(new Label("Các thứ (*):"), chkThu2, chkThu3, chkThu4, chkThu5, chkThu6, chkThu7, chkChuNhat);
        // Mặc định chọn Thứ 2 - Thứ 6
        chkThu2.setSelected(true);
        chkThu3.setSelected(true);
        chkThu4.setSelected(true);
        chkThu5.setSelected(true);
        chkThu6.setSelected(true);

        // Nút chọn nhanh
        HBox pnlChonNhanh = new HBox(8);
        pnlChonNhanh.setAlignment(Pos.CENTER_LEFT);
        Button btnChonTatCa = new Button("Chọn tất cả");
        btnChonTatCa.getStyleClass().add("nut-phu");
        btnChonTatCa.setStyle("-fx-font-size: 11px;");
        btnChonTatCa.setOnAction(e -> {
            chkThu2.setSelected(true); chkThu3.setSelected(true); chkThu4.setSelected(true);
            chkThu5.setSelected(true); chkThu6.setSelected(true); chkThu7.setSelected(true);
            chkChuNhat.setSelected(true);
        });
        Button btnBoChon = new Button("Bỏ chọn tất cả");
        btnBoChon.getStyleClass().add("nut-phu");
        btnBoChon.setStyle("-fx-font-size: 11px;");
        btnBoChon.setOnAction(e -> {
            chkThu2.setSelected(false); chkThu3.setSelected(false); chkThu4.setSelected(false);
            chkThu5.setSelected(false); chkThu6.setSelected(false); chkThu7.setSelected(false);
            chkChuNhat.setSelected(false);
        });
        pnlChonNhanh.getChildren().addAll(btnChonTatCa, btnBoChon);

        pnl.getChildren().addAll(lblTieuDe, pnlNgay, pnlThu, pnlChonNhanh);
        return pnl;
    }

    /**
     * Khung chọn nhiều ca trực bằng CheckBox (thay vì ComboBox chỉ chọn 1).
     */
    private VBox taoKhungChonNhieuCa() {
        VBox pnl = new VBox(6);
        Label lblCa = new Label("Ca trực (*) — chọn một hoặc nhiều ca (không trùng giờ):");
        lblCa.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        pnlDanhSachCa.setPadding(new Insets(4, 0, 4, 16));

        pnl.getChildren().addAll(lblCa, pnlDanhSachCa);
        return pnl;
    }

    // =======================================================
    //  TẠO BẢNG DANH SÁCH
    // =======================================================

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

    // =======================================================
    //  XỬ LÝ SỰ KIỆN
    // =======================================================

    private void capNhatTrangThaiNut(PhanCa phanCaDuocChon) {
        boolean coChon = phanCaDuocChon != null;
        btnHuyPhanCa.setDisable(!coChon || !phanCaDuocChon.isDaPhanCong());
    }

    /**
     * Xử lý phân ca: tùy chế độ đơn lẻ hoặc hàng loạt.
     */
    private void xuLyPhanCa() {
        try {
            NhanVien nv = cboNhanVien.getValue();
            if (nv == null) {
                throw new IllegalArgumentException("Vui lòng chọn nhân viên!");
            }

            // Lấy danh sách ca được chọn
            List<Ca> dsCaDuocChon = layDanhSachCaDuocChon();
            if (dsCaDuocChon.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một ca trực!");
            }

            if (rdoHangLoat.isSelected()) {
                // --- Phân ca hàng loạt ---
                xuLyPhanCaHangLoat(nv, dsCaDuocChon);
            } else {
                // --- Phân ca đơn lẻ ---
                xuLyPhanCaDonLe(nv, dsCaDuocChon);
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

    /**
     * Phân ca đơn lẻ: cho phép chọn nhiều ca trong 1 ngày.
     */
    private void xuLyPhanCaDonLe(NhanVien nv, List<Ca> dsCaDuocChon) {
        LocalDate ngay = dpNgayLam.getValue();
        if (ngay == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày làm việc!");
        }
        if (ngay.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể phân ca cho ngày đã qua ("
                    + ngay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + ")! Chỉ được phân ca từ ngày hôm nay trở đi.");
        }

        int thanhCong = 0;
        List<String> loi = new ArrayList<>();

        for (Ca ca : dsCaDuocChon) {
            try {
                String maPhanCa = caTrucController.taoMaPhanCa();
                PhanCa phanCaMoi = new PhanCa(maPhanCa, nv.getMaNhanVien(), ca.getMaCa(), ngay, PhanCa.TRANG_THAI_DA_PHAN_CONG);
                phanCaMoi.setTenNhanVien(nv.getHoTen());
                phanCaMoi.setTenCa(ca.getTenCa());
                phanCaMoi.setGioBatDau(ca.getGioBatDau());
                phanCaMoi.setGioKetThuc(ca.getGioKetThuc());

                caTrucController.phanCa(phanCaMoi);
                thanhCong++;
            } catch (IllegalArgumentException ex) {
                loi.add("• " + ca.getTenCa() + ": " + ex.getMessage());
            }
        }

        // Hiển thị kết quả
        if (loi.isEmpty()) {
            Alert tb = new Alert(Alert.AlertType.INFORMATION,
                    "Phân " + thanhCong + " ca cho nhân viên '" + nv.getHoTen() + "' ngày "
                            + ngay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " thành công!",
                    ButtonType.OK);
            tb.setHeaderText(null);
            tb.showAndWait();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Thành công: ").append(thanhCong).append(" ca.\n");
            sb.append("Bỏ qua ").append(loi.size()).append(" ca:\n");
            loi.forEach(l -> sb.append(l).append("\n"));
            Alert tb = new Alert(Alert.AlertType.WARNING, sb.toString(), ButtonType.OK);
            tb.setHeaderText("Kết quả phân ca");
            tb.showAndWait();
        }

        taiLaiBangTheoNgay(ngay);
    }

    /**
     * Phân ca hàng loạt: khoảng ngày + thứ trong tuần + nhiều ca.
     */
    private void xuLyPhanCaHangLoat(NhanVien nv, List<Ca> dsCaDuocChon) {
        LocalDate tuNgay = dpTuNgay.getValue();
        LocalDate denNgay = dpDenNgay.getValue();

        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException("Vui lòng chọn khoảng ngày (Từ ngày - Đến ngày)!");
        }
        if (denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu!");
        }
        if (tuNgay.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày bắt đầu không thể là ngày đã qua! Chỉ được phân ca từ ngày hôm nay trở đi.");
        }

        Set<DayOfWeek> cacThu = layDanhSachThuDuocChon();
        if (cacThu.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một thứ trong tuần!");
        }

        // Xác nhận trước khi phân hàng loạt
        long soNgay = tuNgay.datesUntil(denNgay.plusDays(1))
                .filter(d -> cacThu.contains(d.getDayOfWeek()))
                .count();
        long tongPhanCa = soNgay * dsCaDuocChon.size();

        Alert hopXacNhan = new Alert(Alert.AlertType.CONFIRMATION,
                "Sẽ phân ca hàng loạt cho nhân viên '" + nv.getHoTen() + "':\n"
                        + "• Khoảng ngày: " + tuNgay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " → " + denNgay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n"
                        + "• Các thứ: " + tenCacThuDuocChon(cacThu) + "\n"
                        + "• Số ca/ngày: " + dsCaDuocChon.size() + "\n"
                        + "• Tổng dự kiến: ~" + tongPhanCa + " phân ca\n\n"
                        + "Bạn có muốn tiếp tục?",
                ButtonType.YES, ButtonType.NO);
        hopXacNhan.setHeaderText("Xác nhận phân ca hàng loạt");
        Optional<ButtonType> ketQua = hopXacNhan.showAndWait();

        if (ketQua.isEmpty() || ketQua.get() != ButtonType.YES) {
            return;
        }

        List<String> ketQuaPhanCa = caTrucController.phanCaHangLoat(
                nv.getMaNhanVien(), nv.getHoTen(), dsCaDuocChon, tuNgay, denNgay, cacThu);

        // Hiển thị kết quả
        StringBuilder sb = new StringBuilder();
        for (String dong : ketQuaPhanCa) {
            sb.append(dong).append("\n");
        }

        boolean coLoi = ketQuaPhanCa.stream().anyMatch(s -> s.startsWith("⚠"));
        Alert tb = new Alert(coLoi ? Alert.AlertType.WARNING : Alert.AlertType.INFORMATION,
                sb.toString(), ButtonType.OK);
        tb.setHeaderText("Kết quả phân ca hàng loạt");
        tb.getDialogPane().setMinWidth(500);
        tb.showAndWait();

        // Reload bảng
        if (dpNgayLam.getValue() != null) {
            taiLaiBangTheoNgay(dpNgayLam.getValue());
        } else {
            taiLaiBangTatCa();
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
        // Reset chế độ
        rdoDonLe.setSelected(true);
        dpNgayLam.setValue(LocalDate.now());
        dpTuNgay.setValue(LocalDate.now());
        dpDenNgay.setValue(LocalDate.now().plusDays(6));
        chkThu2.setSelected(true); chkThu3.setSelected(true); chkThu4.setSelected(true);
        chkThu5.setSelected(true); chkThu6.setSelected(true);
        chkThu7.setSelected(false); chkChuNhat.setSelected(false);

        // Bỏ chọn tất cả ca
        danhSachCheckCa.forEach(chk -> chk.setSelected(false));

        if (dpNgayLam.getValue() != null) {
            taiLaiBangTheoNgay(dpNgayLam.getValue());
        } else {
            taiLaiBang();
        }
        tblDanhSach.getSelectionModel().clearSelection();
    }

    // =======================================================
    //  DỮ LIỆU / HELPER
    // =======================================================

    /**
     * Nạp danh sách ca trực dưới dạng checkbox và danh sách nhân viên.
     */
    private void napDuLieuDropdown() {
        try {
            // Nạp ca trực thành CheckBox
            List<Ca> dsCa = caTrucController.layDanhSachCaDangApDung();
            pnlDanhSachCa.getChildren().clear();
            danhSachCheckCa.clear();
            mapCheckCa.clear();

            for (Ca ca : dsCa) {
                CheckBox chk = new CheckBox(ca.getTenCa() + "  (" + ca.getKhungGio() + ")");
                chk.setStyle("-fx-font-size: 12px;");
                danhSachCheckCa.add(chk);
                mapCheckCa.put(chk, ca);
                pnlDanhSachCa.getChildren().add(chk);
            }

            if (dsCa.isEmpty()) {
                pnlDanhSachCa.getChildren().add(new Label("(Chưa có ca trực nào đang áp dụng)"));
            }

            // Nạp nhân viên
            List<NhanVien> dsNV = caTrucController.layDanhSachNhanVienDangLamViec();
            cboNhanVien.setItems(FXCollections.observableArrayList(dsNV));
            if (!dsNV.isEmpty()) {
                cboNhanVien.setValue(dsNV.get(0));
            }
        } catch (Exception ex) {
            // Dropdown load error handled gracefully
        }
    }

    /**
     * Lấy danh sách Ca được check bởi người dùng.
     */
    private List<Ca> layDanhSachCaDuocChon() {
        List<Ca> ds = new ArrayList<>();
        for (Map.Entry<CheckBox, Ca> entry : mapCheckCa.entrySet()) {
            if (entry.getKey().isSelected()) {
                ds.add(entry.getValue());
            }
        }
        return ds;
    }

    /**
     * Lấy tập hợp DayOfWeek từ các CheckBox thứ trong tuần.
     */
    private Set<DayOfWeek> layDanhSachThuDuocChon() {
        Set<DayOfWeek> cacThu = new LinkedHashSet<>();
        if (chkThu2.isSelected()) cacThu.add(DayOfWeek.MONDAY);
        if (chkThu3.isSelected()) cacThu.add(DayOfWeek.TUESDAY);
        if (chkThu4.isSelected()) cacThu.add(DayOfWeek.WEDNESDAY);
        if (chkThu5.isSelected()) cacThu.add(DayOfWeek.THURSDAY);
        if (chkThu6.isSelected()) cacThu.add(DayOfWeek.FRIDAY);
        if (chkThu7.isSelected()) cacThu.add(DayOfWeek.SATURDAY);
        if (chkChuNhat.isSelected()) cacThu.add(DayOfWeek.SUNDAY);
        return cacThu;
    }

    /**
     * Chuyển tập DayOfWeek thành chuỗi tiếng Việt để hiển thị.
     */
    private String tenCacThuDuocChon(Set<DayOfWeek> cacThu) {
        Map<DayOfWeek, String> tenThu = new LinkedHashMap<>();
        tenThu.put(DayOfWeek.MONDAY, "T2");
        tenThu.put(DayOfWeek.TUESDAY, "T3");
        tenThu.put(DayOfWeek.WEDNESDAY, "T4");
        tenThu.put(DayOfWeek.THURSDAY, "T5");
        tenThu.put(DayOfWeek.FRIDAY, "T6");
        tenThu.put(DayOfWeek.SATURDAY, "T7");
        tenThu.put(DayOfWeek.SUNDAY, "CN");

        return cacThu.stream()
                .map(d -> tenThu.getOrDefault(d, d.name()))
                .collect(Collectors.joining(", "));
    }

    // =======================================================
    //  TẢI DỮ LIỆU BẢNG
    // =======================================================

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
