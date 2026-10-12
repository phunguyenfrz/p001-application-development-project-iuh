package com.petshop.controller;

import com.petshop.model.InvoiceData.InvoiceReceipt;

public final class InvoiceSavedAuditException extends IllegalStateException {
    private final InvoiceReceipt receipt;

    public InvoiceSavedAuditException(InvoiceReceipt receipt, IllegalStateException cause) {
        super("Hóa đơn đã được lưu nhưng nhật ký thao tác chưa ghi được.", cause);
        this.receipt = receipt;
    }

    public InvoiceReceipt receipt() {
        return receipt;
    }
}
