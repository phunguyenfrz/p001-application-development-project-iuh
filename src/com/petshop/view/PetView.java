package com.petshop.view;

import com.petshop.controller.AuditController;
import com.petshop.controller.PetController;
import com.petshop.model.Pet;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.List;
import java.util.function.Function;

public final class PetView extends CrudListView<Pet, String> {
    private final PetController controller;
    private final AuditController audit;

    public PetView(PetController controller, AuditController audit) {
        super("Hồ sơ thú cưng", "UC013 · Quản lý thú cưng và thông tin sức khỏe cơ bản.",
                "ThuCung", controller, audit, true, false);
        this.controller = controller;
        this.audit = audit;
    }

    @Override
    protected List<TableColumn<Pet, ?>> columns() {
        return List.of(
                column("Mã thú cưng", Pet::getMaThuCung),
                column("Tên", Pet::getTenThuCung),
                column("Chủ sở hữu", Pet::getMaKhachHang),
                column("Loài", Pet::getLoaiThuCung),
                column("Giống", Pet::getGiongLoai),
                column("Cân nặng", pet -> pet.getCanNang() + " kg"),
                column("Dị ứng", Pet::getDiUng));
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

    private void edit(Pet existing) {
        PetFormDialog.show(controller, audit, existing);
        refresh();
    }

    private TableColumn<Pet, String> column(String title, Function<Pet, String> value) {
        TableColumn<Pet, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                value.apply(cell.getValue()) == null ? "" : value.apply(cell.getValue())));
        return column;
    }
}
