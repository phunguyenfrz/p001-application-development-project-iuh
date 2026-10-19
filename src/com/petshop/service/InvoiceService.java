package com.petshop.service;

import com.petshop.dao.InvoiceDao;
import com.petshop.model.Invoice;
import com.petshop.model.InvoiceItem;
import com.petshop.model.User;

import java.time.LocalDate;
import java.util.List;

/** Xử lý quy tắc lập, lưu chờ và thu tiền hóa đơn. */
public class InvoiceService {

    private final InvoiceDao invoiceDao;

    public InvoiceService() {
        this(new InvoiceDao());
    }

    public InvoiceService(InvoiceDao invoiceDao) {
        this.invoiceDao = invoiceDao;
    }

    public List<InvoiceDao.CustomerOption> findCustomers(String keyword) {
        return invoiceDao.findCustomers(keyword);
    }

    public List<InvoiceDao.PetOption> findPets(String customerId) {
        return invoiceDao.findPets(customerId);
    }

    public List<InvoiceDao.PromotionOption> findActivePromotions(LocalDate date) {
        return invoiceDao.findActivePromotions(date);
    }

    public List<InvoiceDao.ServiceOption> findServicesForPet(double weight, LocalDate date) {
        return invoiceDao.findServicesForPet(weight, date);
    }

    public InvoiceDao.AppointmentDraft findAppointmentDraft(String appointmentId) {
        return invoiceDao.findAppointmentDraft(appointmentId);
    }

    public String savePending(Invoice invoice) {
        validateInvoice(invoice);
        invoice.luuChoThanhToan();
        String invoiceId = invoiceDao.save(invoice);
        invoice.setMaHoaDon(invoiceId);
        return invoiceId;
    }

    public String pay(Invoice invoice, User currentUser, String paymentMethod, double cashReceived) {
        validateInvoice(invoice);
        invoice.thanhToan(currentUser, paymentMethod, cashReceived);
        String invoiceId = invoiceDao.save(invoice);
        invoice.setMaHoaDon(invoiceId);
        return invoiceId;
    }

    public User.Role getCurrentRole(User currentUser) {
        return currentUser == null || currentUser.getRole() == null
                ? User.Role.ADMIN : currentUser.getRole();
    }

    public boolean canAddProducts(User currentUser) {
        return getCurrentRole(currentUser) != User.Role.CHAM_SOC;
    }

    public boolean canCollect(Invoice invoice, User currentUser) {
        if (invoice == null) return false;
        User.Role role = getCurrentRole(currentUser);
        if (invoice.coSanPham()) {
            return role == User.Role.ADMIN || role == User.Role.BAN_HANG;
        }
        return true;
    }

    public double calculatePromotionDiscount(double subtotal, double percent) {
        if (subtotal < 0 || percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Giá trị khuyến mãi không hợp lệ.");
        }
        return subtotal * percent / 100.0;
    }

    private void validateInvoice(Invoice invoice) {
        if (invoice == null) throw new IllegalArgumentException("Hóa đơn không hợp lệ.");
        if (invoice.getMaNhanVien() == null || invoice.getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Không xác định được nhân viên lập hóa đơn.");
        }
        if (invoice.getNgayLap() == null) invoice.setNgayLap(LocalDate.now());
        if (invoice.getGiamGia() + invoice.getGiamTheoKhuyenMai() > invoice.tinhTamTinh()) {
            throw new IllegalArgumentException("Tổng giảm giá không được vượt quá tạm tính.");
        }
        if (invoice.getMaKhuyenMai() != null) {
            InvoiceDao.PromotionOption activePromotion = invoiceDao.findActivePromotions(invoice.getNgayLap()).stream()
                    .filter(option -> option.id().equals(invoice.getMaKhuyenMai()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Khuyến mãi không còn hiệu lực vào ngày lập hóa đơn."));
            double expectedDiscount = calculatePromotionDiscount(invoice.tinhTamTinh(), activePromotion.percent());
                    expectedDiscount = Math.min(expectedDiscount, Math.max(0, invoice.tinhTamTinh() - invoice.getGiamGia()));
            if (Math.abs(expectedDiscount - invoice.getGiamTheoKhuyenMai()) > 0.01) {
                throw new IllegalArgumentException("Số tiền giảm theo khuyến mãi đã thay đổi; vui lòng chọn lại mã.");
            }
        } else if (invoice.getGiamTheoKhuyenMai() > 0) {
            throw new IllegalArgumentException("Hóa đơn có giảm theo khuyến mãi nhưng chưa chọn mã.");
        }
        for (InvoiceItem item : invoice.getItems()) {
            if (item.getDonGia() < 0 || item.getSoLuong() < 1) {
                throw new IllegalArgumentException("Số lượng hoặc đơn giá của dòng hóa đơn không hợp lệ.");
            }
            if (item.getLoaiDong() == InvoiceItem.LineType.SAN_PHAM
                    && (item.getMaSanPham() == null || item.getMaSanPham().isBlank())) {
                throw new IllegalArgumentException("Dòng sản phẩm thiếu mã sản phẩm.");
            }
            if (item.getLoaiDong() == InvoiceItem.LineType.DICH_VU
                    && (item.getMaDichVu() == null || item.getMaDichVu().isBlank())) {
                throw new IllegalArgumentException("Dòng dịch vụ thiếu mã dịch vụ.");
            }
        }
    }
}