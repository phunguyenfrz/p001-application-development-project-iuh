package com.petshop.controller;

import com.petshop.dao.PurchaseOrderRepository;
import com.petshop.model.PurchaseOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class PurchaseOrderController {
    private final PurchaseOrderRepository repository;
    private final AuditController audit;

    public PurchaseOrderController(PurchaseOrderRepository repository, AuditController audit) {
        this.repository = Objects.requireNonNull(repository);
        this.audit = Objects.requireNonNull(audit);
    }

    public List<PurchaseOrder> findAll() {
        return repository.findAll();
    }

    public List<PurchaseOrder.Choice> suppliers() {
        return repository.suppliers();
    }

    public List<PurchaseOrder.Choice> products() {
        return repository.products();
    }

    public void create(PurchaseOrder order) {
        Objects.requireNonNull(order);
        if (order.id() == null || order.supplierId() == null || order.employeeId() == null
                || order.createdOn() == null || order.lines().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đủ nhà cung cấp và ít nhất một mặt hàng.");
        }
        for (PurchaseOrder.Line line : order.lines()) {
            if (line.productId() == null || line.quantity() <= 0 || line.unitCost() == null
                    || line.unitCost().signum() < 0 || line.unitCost().scale() > 2) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0 và đơn giá không âm.");
            }
        }
        BigDecimal total = order.lines().stream().map(PurchaseOrder.Line::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        PurchaseOrder complete = new PurchaseOrder(order.id(), order.supplierId(),
                order.supplierName(), order.employeeId(), order.employeeName(),
                LocalDate.now(), total, "Chờ nhận hàng", order.lines());
        repository.create(complete);
        audit.log("Lập đơn nhập", "DonHang", complete.id(), null);
    }

    public void receive(PurchaseOrder order) {
        if (order == null || !"Chờ nhận hàng".equals(order.status())) {
            throw new IllegalArgumentException("Chỉ xác nhận được đơn đang Chờ nhận hàng.");
        }
        repository.receive(order.id());
        audit.log("Xác nhận nhận hàng", "DonHang", order.id(), null);
    }
}
