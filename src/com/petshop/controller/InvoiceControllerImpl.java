package com.petshop.controller;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.petshop.dao.InvoiceRepository;
import com.petshop.model.InvoiceData.AppointmentOption;
import com.petshop.model.InvoiceData.CustomerOption;
import com.petshop.model.InvoiceData.InvoiceDraft;
import com.petshop.model.InvoiceData.InvoiceReceipt;
import com.petshop.model.InvoiceData.ProductOption;
import com.petshop.model.User;
import com.petshop.util.Formats;
import com.petshop.util.SessionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class InvoiceControllerImpl implements InvoiceController {
    private final InvoiceRepository repository;
    private final AuditController auditController;

    public InvoiceControllerImpl(
            InvoiceRepository repository, AuditController auditController) {
        this.repository = Objects.requireNonNull(repository);
        this.auditController = Objects.requireNonNull(auditController);
    }

    @Override
    public List<CustomerOption> findCustomers() {
        return repository.findCustomers();
    }

    @Override
    public List<ProductOption> searchProducts(String keyword) {
        return repository.searchProducts(keyword);
    }

    @Override
    public List<AppointmentOption> findCompletedAppointments() {
        return repository.findCompletedAppointments();
    }

    @Override
    public InvoiceReceipt createInvoice(InvoiceDraft draft) {
        User actor = Objects.requireNonNull(SessionManager.getCurrentUser(),
                "Phiên đăng nhập không hợp lệ.");
        InvoiceReceipt receipt = repository.create(draft, actor.getMaNhanVien());
        try {
            auditController.log("Lập hóa đơn", "HoaDon", receipt.id(),
                    "Thanh toán bằng " + receipt.paymentMethod());
        } catch (IllegalStateException exception) {
            throw new InvoiceSavedAuditException(receipt, exception);
        }
        return receipt;
    }

    @Override
    public void exportPdf(InvoiceReceipt receipt, Path target) {
        Objects.requireNonNull(receipt);
        Objects.requireNonNull(target);
        try {
            Path parent = target.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Document document = new Document();
            try (var output = Files.newOutputStream(target)) {
                PdfWriter.getInstance(document, output);
                document.open();
                Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                Paragraph heading = new Paragraph("HÓA ĐƠN HAPPY TAILS", title);
                heading.setAlignment(Element.ALIGN_CENTER);
                document.add(heading);
                document.add(new Paragraph("Mã hóa đơn: " + receipt.id()));
                document.add(new Paragraph("Thời gian: " + Formats.dateTime(receipt.createdAt())));
                document.add(new Paragraph("Khách hàng: " + receipt.customerName()));
                document.add(new Paragraph("Thanh toán: " + receipt.paymentMethod()));
                PdfPTable table = new PdfPTable(new float[] {4, 1, 2, 2});
                table.setWidthPercentage(100);
                for (String headingText : List.of("Mặt hàng", "SL", "Đơn giá", "Thành tiền")) {
                    table.addCell(new Phrase(headingText));
                }
                for (var line : receipt.lines()) {
                    table.addCell(line.description());
                    table.addCell(Integer.toString(line.quantity()));
                    table.addCell(Formats.money(line.unitPrice()));
                    table.addCell(Formats.money(line.amount()));
                }
                document.add(table);
                document.add(new Paragraph("Tổng tiền: " + Formats.money(receipt.total())));
            } finally {
                document.close();
            }
        } catch (IOException | com.lowagie.text.DocumentException exception) {
            throw new IllegalStateException("Không thể xuất hóa đơn PDF.", exception);
        }
    }
}
