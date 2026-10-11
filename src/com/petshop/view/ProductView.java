package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.ProductController;
import com.petshop.model.Product;
import com.petshop.util.Formats;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.List;

public final class ProductView extends CrudListView<Product, String> {
    private final ProductController controller;
    private final AuditController auditController;

    public ProductView(ProductController controller, AuditController auditController) {
        super("Sản phẩm", "UC009 · Quản lý danh mục, mã vạch và giá bán.",
                "SanPham", controller, auditController, true);
        this.controller = controller;
        this.auditController = auditController;
    }

    @Override
    protected List<TableColumn<Product, ?>> columns() {
        return List.of(
                column("Mã", Product::getMaSanPham),
                column("Tên sản phẩm", Product::getTenSanPham),
                column("Mã vạch", product -> display(product.getMaVach())),
                column("Giá bán", product -> Formats.money(product.getGiaBanDecimal())),
                column("Tồn kho", product -> product.getSoLuongTon() + " " + product.getDonViTinh()),
                column("Ngưỡng tồn", product -> String.valueOf(product.getNguongTonToiThieu())),
                column("Trạng thái", product -> product.isTrangThai()
                        ? "Đang kinh doanh" : "Ngừng kinh doanh"));
    }

    @Override
    protected void openAddDialog() {
        new ProductFormDialog(controller, auditController, null);
        refresh();
    }

    @Override
    protected void openEditDialog(Product product) {
        new ProductFormDialog(controller, auditController, product);
        refresh();
    }

    @Override
    protected String idOf(Product product) {
        return product.getMaSanPham();
    }

    @Override
    protected boolean canDeactivate(Product product) {
        return product.isTrangThai();
    }

    @Override
    protected List<String> filterOptions() {
        return List.of("Tất cả", "Đang kinh doanh", "Ngừng kinh doanh");
    }

    @Override
    protected List<Product> filter(List<Product> products, String selectedFilter) {
        return switch (selectedFilter) {
            case "Đang kinh doanh" -> products.stream().filter(Product::isTrangThai).toList();
            case "Ngừng kinh doanh" -> products.stream().filter(product -> !product.isTrangThai()).toList();
            default -> products;
        };
    }

    private TableColumn<Product, String> column(
            String heading, java.util.function.Function<Product, String> value) {
        TableColumn<Product, String> column = new TableColumn<>(heading);
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
