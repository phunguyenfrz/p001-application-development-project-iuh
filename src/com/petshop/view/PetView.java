package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.CustomerController;
import com.petshop.controller.PetController;
import com.petshop.dao.CustomerDao;
import com.petshop.model.Pet;
import com.petshop.model.Role;
import com.petshop.util.SessionManager;
import com.petshop.util.TextSearch;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.List;
import java.util.function.Function;

public final class PetView extends CrudListView<Pet, String> {
    private final PetController controller;
    private final CustomerController customers;

    public PetView(PetController controller, AuditController audit) {
        this(controller, new CustomerController(new CustomerDao()), audit,
                SessionManager.getCurrentUser() != null
                        && SessionManager.getCurrentUser().getRole() == Role.BAN_HANG);
    }

    public PetView(PetController controller, AuditController audit, boolean allowEdit) {
        this(controller, new CustomerController(new CustomerDao()), audit, allowEdit);
    }

    public PetView(PetController controller, CustomerController customers,
                   AuditController audit, boolean allowEdit) {
        super("Hồ sơ thú cưng", "UC013 · Quản lý thú cưng và thông tin sức khỏe cơ bản.",
                "ThuCung", controller, audit, allowEdit, allowEdit);
        this.controller = controller;
        this.customers = customers;
    }

    @Override
    protected List<TableColumn<Pet, ?>> columns() {
        return List.of(
                column("Mã thú cưng", Pet::getMaThuCung),
                column("Tên thú cưng", Pet::getTenThuCung),
                column("Loài", Pet::getLoaiThuCung),
                column("Giống", Pet::getGiongLoai),
                column("Giới tính", Pet::getGioiTinh),
                column("Ngày sinh", pet -> pet.getNgaySinh() == null
                        ? "" : pet.getNgaySinh().toString()),
                column("Cân nặng (kg)", pet -> String.valueOf(pet.getCanNang())),
                column("Chủ nuôi", Pet::getTenChu),
                column("Ghi chú sức khỏe", pet -> String.join(" · ",
                        nonBlank(pet.getDiUng()), nonBlank(pet.getBenhNen()),
                        nonBlank(pet.getTinhCach())).replaceAll("^( · )+|( · )+$", "")),
                column("Trạng thái", pet -> pet.isActive() ? "Đang hoạt động" : "Đã ngừng"));
    }

    @Override
    protected void openAddDialog() {
        edit(null);
    }

    @Override
    protected void openEditDialog(Pet pet) {
        edit(pet);
    }

    @Override
    protected String idOf(Pet pet) {
        return pet.getMaThuCung();
    }

    @Override
    protected boolean canDeactivate(Pet pet) {
        return pet.isActive();
    }

    @Override
    protected boolean auditChangesInController() {
        return true;
    }

    @Override
    protected boolean showDetailsOnDoubleClick() {
        return true;
    }

    @Override
    protected boolean matches(Pet pet, String keyword) {
        return TextSearch.matchesAll(keyword, pet.getMaThuCung(), pet.getTenThuCung(),
                pet.getLoaiThuCung(), pet.getGiongLoai(), pet.getTenChu(),
                pet.getDienThoaiChu());
    }

    @Override
    protected void openReadOnlyDialog(Pet pet) {
        PetDetailView.show(pet, controller);
    }

    private void edit(Pet existing) {
        PetFormDialog.show(controller, customers, existing, null);
        refresh();
    }

    private TableColumn<Pet, String> column(String title, Function<Pet, String> value) {
        TableColumn<Pet, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                value.apply(cell.getValue()) == null ? "" : value.apply(cell.getValue())));
        return column;
    }

    private String nonBlank(String value) {
        return value == null ? "" : value.trim();
    }
}
