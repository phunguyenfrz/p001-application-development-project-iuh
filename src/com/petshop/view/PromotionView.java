package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.PromotionController;
import com.petshop.model.Promotion;
import com.petshop.util.Formats;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.List;
import java.util.function.Function;

public final class PromotionView extends CrudListView<Promotion, String> {
    private final PromotionController controller;
    private final AuditController audit;

    public PromotionView(PromotionController controller, AuditController audit) {
        super("Khuyến mãi", "UC006 · Quản lý chương trình và thời hạn áp dụng.",
                "KhuyenMai", controller, audit, true);
        this.controller = controller;
        this.audit = audit;
    }

    @Override
    protected List<TableColumn<Promotion, ?>> columns() {
        return List.of(
                column("Mã", Promotion::id),
                column("Chương trình", Promotion::name),
                column("Giảm", value -> value.discountPercent() + "%"),
                column("Ngày bắt đầu", value -> Formats.date(value.startsOn())),
                column("Ngày kết thúc", value -> Formats.date(value.endsOn())),
                column("Trạng thái", value -> value.active() ? "Đang áp dụng" : "Ngừng áp dụng"));
    }

    @Override
    protected void openAddDialog() {
        PromotionFormDialog.show(controller, audit, null);
        refresh();
    }

    @Override
    protected void openEditDialog(Promotion promotion) {
        PromotionFormDialog.show(controller, audit, promotion);
        refresh();
    }

    @Override
    protected String idOf(Promotion promotion) {
        return promotion.id();
    }

    @Override
    protected boolean canDeactivate(Promotion promotion) {
        return promotion.active();
    }

    @Override
    protected List<String> filterOptions() {
        return List.of("Tất cả", "Đang áp dụng", "Ngừng áp dụng");
    }

    @Override
    protected List<Promotion> filter(List<Promotion> values, String selectedFilter) {
        return switch (selectedFilter) {
            case "Đang áp dụng" -> values.stream().filter(Promotion::active).toList();
            case "Ngừng áp dụng" -> values.stream().filter(value -> !value.active()).toList();
            default -> values;
        };
    }

    private TableColumn<Promotion, String> column(
            String title, Function<Promotion, String> value) {
        TableColumn<Promotion, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
