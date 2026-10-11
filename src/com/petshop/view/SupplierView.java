package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CrudController;
import com.petshop.model.NhaCungCap;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

public final class SupplierView extends CrudView<NhaCungCap, String> {
    private TextField codeField;
    private TextField nameField;
    private TextField phoneField;
    private TextField addressField;
    private boolean inactive;

    public SupplierView(
            CrudController<NhaCungCap, String> controller,
            AuditController auditController) {
        super("Nhà cung cấp", "Tra cứu và cập nhật thông tin nhà cung cấp.",
                "NhaCungCap", controller, auditController);
    }

    @Override
    protected List<TableColumn<NhaCungCap, ?>> columns() {
        TableColumn<NhaCungCap, String> code = column("Mã", NhaCungCap::getMaNhaCungCap);
        TableColumn<NhaCungCap, String> name = column("Tên nhà cung cấp", NhaCungCap::getTenNhaCungCap);
        TableColumn<NhaCungCap, String> phone = column("Số điện thoại", NhaCungCap::getSoDienThoai);
        TableColumn<NhaCungCap, String> address = column("Địa chỉ", NhaCungCap::getDiaChi);
        TableColumn<NhaCungCap, String> status = column("Trạng thái",
                supplier -> supplier.isNgungHoatDong() ? "Ngừng hoạt động" : "Đang hoạt động");
        return List.of(code, name, phone, address, status);
    }

    @Override
    protected Node createForm(NhaCungCap existing) {
        inactive = existing != null && existing.isNgungHoatDong();
        codeField = field("Mã nhà cung cấp");
        nameField = field("Tên nhà cung cấp");
        phoneField = field("Số điện thoại");
        addressField = field("Địa chỉ");
        if (existing != null) {
            inactive = existing.isNgungHoatDong();
            codeField.setText(existing.getMaNhaCungCap());
            codeField.setDisable(true);
            nameField.setText(existing.getTenNhaCungCap());
            phoneField.setText(existing.getSoDienThoai());
            addressField.setText(existing.getDiaChi());
        }
        VBox form = new VBox(8,
                labeled("Mã", codeField),
                labeled("Tên", nameField),
                labeled("Số điện thoại", phoneField),
                labeled("Địa chỉ", addressField));
        form.setPadding(new Insets(4));
        return form;
    }

    @Override
    protected List<String> validate(NhaCungCap item) {
        return item.validate();
    }

    @Override
    protected NhaCungCap extractFromForm() {
        return new NhaCungCap(codeField.getText(), nameField.getText(),
                phoneField.getText(), addressField.getText(), inactive);
    }

    @Override
    protected String idOf(NhaCungCap item) {
        return item.getMaNhaCungCap();
    }

    @Override
    protected boolean canDeactivate(NhaCungCap item) {
        return !item.isNgungHoatDong();
    }

    @Override
    protected List<String> filterOptions() {
        return List.of("Tất cả", "Đang hoạt động", "Ngừng hoạt động");
    }

    @Override
    protected List<NhaCungCap> filter(List<NhaCungCap> values, String selectedFilter) {
        return switch (selectedFilter) {
            case "Đang hoạt động" -> values.stream().filter(item -> !item.isNgungHoatDong()).toList();
            case "Ngừng hoạt động" -> values.stream().filter(NhaCungCap::isNgungHoatDong).toList();
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

    private TextField field(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        return field;
    }

    private Node labeled(String caption, Node control) {
        return new VBox(4, new Label(caption), control);
    }
}
