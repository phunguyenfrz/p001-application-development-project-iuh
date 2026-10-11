package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CareServiceController;
import com.petshop.model.CareServiceData;
import com.petshop.util.Formats;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.List;
import java.util.function.Function;

public final class CareServiceView extends CrudListView<CareServiceData, String> {
    private final CareServiceController controller;
    private final AuditController audit;

    public CareServiceView(CareServiceController controller, AuditController audit) {
        super("Dịch vụ chăm sóc", "UC005 · Quản lý dịch vụ và bảng giá theo ba bậc cân nặng.",
                "DichVuChamSoc", controller, audit, true);
        this.controller = controller;
        this.audit = audit;
    }

    @Override
    protected List<TableColumn<CareServiceData, ?>> columns() {
        return List.of(
                column("Mã", CareServiceData::id),
                column("Dịch vụ", CareServiceData::name),
                column("Thời lượng", service -> service.durationMinutes() + " phút"),
                column("Nhỏ (<5 kg)", service -> money(service.smallPrice())),
                column("Vừa (5–10 kg)", service -> money(service.mediumPrice())),
                column("Lớn (>10 kg)", service -> money(service.largePrice())),
                column("Trạng thái", service -> service.active()
                        ? "Đang áp dụng" : "Ngừng áp dụng"));
    }

    @Override
    protected void openAddDialog() {
        edit(null);
    }

    @Override
    protected void openEditDialog(CareServiceData item) {
        edit(item);
    }

    @Override
    protected String idOf(CareServiceData item) {
        return item.id();
    }

    @Override
    protected boolean canDeactivate(CareServiceData item) {
        return item.active();
    }

    @Override
    protected List<String> filterOptions() {
        return List.of("Tất cả", "Đang áp dụng", "Ngừng áp dụng");
    }

    @Override
    protected List<CareServiceData> filter(List<CareServiceData> values, String filter) {
        return switch (filter) {
            case "Đang áp dụng" -> values.stream().filter(CareServiceData::active).toList();
            case "Ngừng áp dụng" -> values.stream().filter(value -> !value.active()).toList();
            default -> values;
        };
    }

    private void edit(CareServiceData service) {
        CareServiceFormDialog.show(controller, audit, service);
        refresh();
    }

    private TableColumn<CareServiceData, String> column(
            String title, Function<CareServiceData, String> value) {
        TableColumn<CareServiceData, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private String money(java.math.BigDecimal amount) {
        return amount == null ? "Chưa có giá" : Formats.money(amount);
    }
}
