package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CustomerController;
import com.petshop.model.Customer;
import com.petshop.model.Role;
import com.petshop.util.SessionManager;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class CustomerView extends CrudListView<Customer, String> {
    private final CustomerController controller;
    private final AuditController audit;

    public CustomerView(CustomerController controller, AuditController audit) {
        this(controller, audit, SessionManager.getCurrentUser() != null
                && SessionManager.getCurrentUser().getRole() == Role.BAN_HANG);
    }

    public CustomerView(CustomerController controller, AuditController audit, boolean allowEdit) {
        super("Khách hàng", "UC014 · Tra cứu và cập nhật hồ sơ khách hàng.",
                "KhachHang", controller, audit, allowEdit, false);
        this.controller = controller;
        this.audit = audit;
    }

    @Override
    protected List<TableColumn<Customer, ?>> columns() {
        return List.of(
                column("Mã khách hàng", Customer::getMaKhachHang),
                column("Họ tên", Customer::getHoTen),
                column("Điện thoại", Customer::getSoDienThoai),
                column("Email", Customer::getEmail),
                column("Điểm tích lũy", customer -> String.valueOf(customer.getDiemTichLuy())),
                column("Địa chỉ", Customer::getDiaChi));
    }

    @Override
    protected void openAddDialog() {
        edit(null);
    }

    @Override
    protected void openEditDialog(Customer customer) {
        edit(customer);
    }

    @Override
    protected String idOf(Customer customer) {
        return customer.getMaKhachHang();
    }

    private void edit(Customer existing) {
        CustomerFormDialog.show(controller, audit, existing);
        refresh();
    }

    private TableColumn<Customer, String> column(
            String title, Function<Customer, String> value) {
        TableColumn<Customer, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(nullSafe(value.apply(cell.getValue()))));
        return column;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    static String newCode(String prefix) {
        return prefix + "-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
    }
}
