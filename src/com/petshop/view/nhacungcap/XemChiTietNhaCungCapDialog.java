package com.petshop.view.nhacungcap;

import com.petshop.controller.SanPhamController;
import com.petshop.model.NhaCungCap;
import com.petshop.model.SanPham;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

/** Hộp thoại chỉ đọc thông tin nhà cung cấp và các sản phẩm được gán cho NCC. */
public final class XemChiTietNhaCungCapDialog {

    private XemChiTietNhaCungCapDialog() {
    }

    public static void hienThi(NhaCungCap nhaCungCap, Window owner) {
        if (nhaCungCap == null) return;

        try {
            List<SanPham> sanPham = new SanPhamController().layTheoNhaCungCap(nhaCungCap.getMaNhaCungCap());
            // Không đưa sản phẩm đã ngừng kinh doanh (dữ liệu legacy có thể lỗi mã hóa) vào danh sách hiện tại.
            sanPham.removeIf(sp -> !sp.isTrangThai());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) stage.initOwner(owner);
            stage.setTitle("Chi tiết nhà cung cấp");

            VBox root = new VBox(12);
            root.setPadding(new Insets(18));
            root.getStyleClass().add("khung-phan-he");

            Label title = new Label("CHI TIẾT NHÀ CUNG CẤP");
            title.getStyleClass().add("tieu-de-phan-he");
            Label name = new Label(nhaCungCap.getTenNhaCungCap());
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0284c7;");

            GridPane info = new GridPane();
            info.setHgap(18);
            info.setVgap(7);
            info.addRow(0, new Label("Mã NCC:"), new Label(hoacGach(nhaCungCap.getMaNhaCungCap())),
                    new Label("Số điện thoại:"), new Label(hoacGach(nhaCungCap.getSoDienThoai())));
            info.addRow(1, new Label("Email:"), new Label(hoacGach(nhaCungCap.getEmail())),
                    new Label("Trạng thái:"), new Label(nhaCungCap.isTrangThai() ? "Đang hoạt động" : "Ngừng hợp tác"));
            info.addRow(2, new Label("Địa chỉ:"), new Label(hoacGach(nhaCungCap.getDiaChi())));

            Label lblSanPham = new Label("Sản phẩm của nhà cung cấp (" + sanPham.size() + ")");
            lblSanPham.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            TableView<SanPham> table = taoBangSanPham(sanPham);
            VBox.setVgrow(table, Priority.ALWAYS);

            Button close = new Button("Đóng");
            close.getStyleClass().add("nut-phu");
            close.setOnAction(e -> stage.close());
            HBox actions = new HBox(close);
            actions.setAlignment(Pos.CENTER_RIGHT);

            root.getChildren().addAll(title, name, info, lblSanPham, table, actions);
            Scene scene = new Scene(root, 850, 560);
            try {
                scene.getStylesheets().add(XemChiTietNhaCungCapDialog.class
                        .getResource("/com/petshop/view/css/chung.css").toExternalForm());
            } catch (Exception ignored) {
            }
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Không thể xem chi tiết");
            alert.setHeaderText(null);
            alert.setContentText(ex.getMessage() == null ? "Không thể tải sản phẩm của nhà cung cấp." : ex.getMessage());
            alert.showAndWait();
        }
    }

    private static TableView<SanPham> taoBangSanPham(List<SanPham> sanPham) {
        TableView<SanPham> table = new TableView<>();
        table.getStyleClass().add("bang-du-lieu");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Nhà cung cấp chưa được gán sản phẩm nào."));

        TableColumn<SanPham, String> ma = new TableColumn<>("Mã sản phẩm");
        ma.setCellValueFactory(d -> new SimpleStringProperty(hoacGach(d.getValue().getMaSanPham())));
        TableColumn<SanPham, String> ten = new TableColumn<>("Tên sản phẩm");
        ten.setCellValueFactory(d -> new SimpleStringProperty(hoacGach(d.getValue().getTenSanPham())));
        TableColumn<SanPham, String> donVi = new TableColumn<>("Đơn vị tính");
        donVi.setCellValueFactory(d -> new SimpleStringProperty(hoacGach(d.getValue().getDonViTinh())));
        TableColumn<SanPham, String> tonKho = new TableColumn<>("Tồn kho");
        tonKho.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoLuongTon())));
        TableColumn<SanPham, String> trangThai = new TableColumn<>("Trạng thái");
        trangThai.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isTrangThai() ? "Đang kinh doanh" : "Ngừng kinh doanh"));

        table.getColumns().setAll(List.of(ma, ten, donVi, tonKho, trangThai));
        table.setItems(FXCollections.observableArrayList(sanPham));
        return table;
    }

    private static String hoacGach(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
