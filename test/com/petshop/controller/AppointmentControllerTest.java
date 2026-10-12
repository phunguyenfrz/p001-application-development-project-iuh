package com.petshop.controller;

import com.petshop.dao.AppointmentRepository;
import com.petshop.model.AppointmentData.Draft;
import com.petshop.model.AppointmentData.Lookups;
import com.petshop.model.AppointmentData.Row;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentControllerTest {
    @Test
    void advancesOnlyThroughAllowedAppointmentStatuses() {
        FakeRepository repository = new FakeRepository();
        AppointmentController controller = new AppointmentController(repository,
                (action, table, id, note) -> { });

        controller.advanceStatus(row("Đã đặt"));
        controller.advanceStatus(row("Đã xác nhận"));
        controller.advanceStatus(row("Đang thực hiện"));

        assertEquals(List.of("Đã xác nhận", "Đang thực hiện", "Hoàn thành"),
                repository.statuses);
        assertThrows(IllegalArgumentException.class,
                () -> controller.advanceStatus(row("Hoàn thành")));
    }

    @Test
    void cancelsWithoutDeletingAndRejectsPastAppointment() {
        FakeRepository repository = new FakeRepository();
        AppointmentController controller = new AppointmentController(repository,
                (action, table, id, note) -> { });

        controller.cancel(row("Đã xác nhận"));
        assertEquals(List.of("Đã hủy"), repository.statuses);

        Draft past = new Draft("KH-1", "TC-1", "NV-1",
                LocalDate.now().minusDays(1), LocalTime.of(9, 0), "", List.of("DV-1"));
        assertThrows(IllegalArgumentException.class, () -> controller.create(past));
    }

    private Row row(String status) {
        return new Row("LH-20261011-000001", "KH-1", "Khách", "TC-1", "Mèo",
                "NV-1", "Nhân viên", LocalDate.now(), LocalTime.of(9, 0),
                status, "", "Tắm");
    }

    private static final class FakeRepository implements AppointmentRepository {
        private final java.util.ArrayList<String> statuses = new java.util.ArrayList<>();

        @Override
        public List<Row> findByDate(LocalDate date) {
            return List.of();
        }

        @Override
        public Lookups loadLookups(LocalDate date) {
            return new Lookups(List.of(), List.of(), List.of());
        }

        @Override
        public List<com.petshop.model.AppointmentData.ServiceOption> loadServices(
                LocalDate date, java.math.BigDecimal petWeight) {
            return List.of();
        }

        @Override
        public String create(Draft draft) {
            return "LH-20261011-000001";
        }

        @Override
        public void updateStatus(String id, String expectedStatus, String status) {
            statuses.add(status);
        }
    }
}
