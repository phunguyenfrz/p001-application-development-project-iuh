package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CrudController;
import com.petshop.model.NhaCungCap;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.List;

public final class SupplierView extends CrudListView<NhaCungCap, String> {
    private final CrudController<NhaCungCap, String> controller;
    private final AuditController auditController;

    public SupplierView(
            CrudController<NhaCungCap, String> controller,
            AuditController auditController) {
        super("Nhà cung cấp", "Tra cứu và cập nhật thông tin nhà cung cấp.",
                "NhaCungCap", controller, auditController, true, false);
        this.controller = controller;
        this.auditController = auditController;
    }

    @Override
    protected List<TableColumn<NhaCungCap, ?>> columns() {
        return List.of(
                column("Mã", NhaCungCap::getMaNhaCungCap),
                column("Tên nhà cung cấp", NhaCungCap::getTenNhaCungCap),
                column("Số điện thoại", NhaCungCap::getSoDienThoai),
                column("Địa chỉ", NhaCungCap::getDiaChi),
                column("Trạng thái", supplier -> "Đang hợp tác"));
    }

    @Override
    protected void openAddDialog() {
        new SupplierFormDialog(controller, auditController, null);
        refresh();
    }

    @Override
    protected void openEditDialog(NhaCungCap item) {
        new SupplierFormDialog(controller, auditController, item);
        refresh();
    }

    @Override
    protected String idOf(NhaCungCap item) {
        return item.getMaNhaCungCap();
    }

    @Override
    protected boolean canDeactivate(NhaCungCap item) {
        return false;
    }

    @Override
    protected List<String> filterOptions() {
        return List.of("Tất cả", "Đang hoạt động", "Ngừng hoạt động");
    }

    @Override
    protected List<NhaCungCap> filter(List<NhaCungCap> values, String selectedFilter) {
        return switch (selectedFilter) {
            case "Đang hoạt động" -> values.stream()
                    .filter(item -> !item.isNgungHoatDong()).toList();
            case "Ngừng hoạt động" -> values.stream()
                    .filter(NhaCungCap::isNgungHoatDong).toList();
            default -> values;
        };
    }

    private TableColumn<NhaCungCap, String> column(
            String heading, java.util.function.Function<NhaCungCap, String> value) {
        TableColumn<NhaCungCap, String> column = new TableColumn<>(heading);
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
