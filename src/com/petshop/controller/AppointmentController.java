package com.petshop.controller;

import com.petshop.dao.AppointmentRepository;
import com.petshop.model.AppointmentData.Draft;
import com.petshop.model.AppointmentData.CreationResult;
import com.petshop.model.AppointmentData.Lookups;
import com.petshop.model.AppointmentData.Row;
import com.petshop.model.AppointmentData.ServiceOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AppointmentController {
    private static final Map<String, String> NEXT_STATUS = Map.of(
            "Đã đặt", "Đã xác nhận",
            "Đã xác nhận", "Đang thực hiện",
            "Đang thực hiện", "Hoàn thành");

    private final AppointmentRepository repository;
    private final AuditController audit;

    public AppointmentController(AppointmentRepository repository, AuditController audit) {
        this.repository = Objects.requireNonNull(repository);
        this.audit = Objects.requireNonNull(audit);
    }

    public List<Row> findByDate(LocalDate date) {
        return repository.findByDate(Objects.requireNonNull(date));
    }

    public Lookups loadLookups(LocalDate date) {
        return repository.loadLookups(Objects.requireNonNull(date));
    }

    public List<ServiceOption> loadServices(LocalDate date, BigDecimal petWeight) {
        return repository.loadServices(Objects.requireNonNull(date),
                Objects.requireNonNull(petWeight));
    }

    public CreationResult create(Draft draft) {
        validate(draft);
        String id = repository.create(draft);
        try {
            audit.log("Đặt lịch", "LichHen", id, null);
            return new CreationResult(id, true);
        } catch (RuntimeException exception) {
            return new CreationResult(id, false);
        }
    }

    public void advanceStatus(Row appointment) {
        Objects.requireNonNull(appointment);
        String next = NEXT_STATUS.get(appointment.status());
        if (next == null) {
            throw new IllegalArgumentException("Lịch hẹn không thể chuyển sang trạng thái tiếp theo.");
        }
        repository.updateStatus(appointment.id(), appointment.status(), next);
        logStatus(appointment, next);
    }

    public void cancel(Row appointment) {
        Objects.requireNonNull(appointment);
        if ("Đã hủy".equals(appointment.status()) || "Hoàn thành".equals(appointment.status())) {
            throw new IllegalArgumentException("Lịch hẹn đã hủy hoặc hoàn thành, không thể hủy lại.");
        }
        repository.updateStatus(appointment.id(), appointment.status(), "Đã hủy");
        logStatus(appointment, "Đã hủy");
    }

    private void logStatus(Row appointment, String next) {
        try {
            audit.log("Đổi trạng thái", "LichHen", appointment.id(),
                    appointment.status() + " → " + next);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Đã đổi trạng thái lịch hẹn "
                    + appointment.id() + " nhưng không ghi được nhật ký.", exception);
        }
    }

    private void validate(Draft draft) {
        Objects.requireNonNull(draft, "Thông tin lịch hẹn không được để trống.");
        if (blank(draft.customerId()) || blank(draft.petId())
                || blank(draft.employeeId()) || draft.date() == null || draft.time() == null) {
            throw new IllegalArgumentException("Hãy nhập đủ khách hàng, thú cưng, nhân viên và thời gian.");
        }
        if (draft.date().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể đặt lịch ở ngày đã qua.");
        }
        if (draft.serviceIds().isEmpty()) {
            throw new IllegalArgumentException("Hãy chọn ít nhất một dịch vụ.");
        }
        if (draft.note() != null && draft.note().length() > 255) {
            throw new IllegalArgumentException("Ghi chú không được vượt quá 255 ký tự.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
