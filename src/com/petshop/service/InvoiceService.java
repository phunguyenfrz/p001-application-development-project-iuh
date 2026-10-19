package com.petshop.service;

import com.petshop.dao.InvoiceDao;
import com.petshop.dao.ProductDao;
import com.petshop.model.Invoice;
import com.petshop.model.InvoiceItem;
import com.petshop.model.Product;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lớp dịch vụ nghiệp vụ quản lý Hóa đơn (InvoiceService).
 * Thực hiện kiểm tra tính hợp lệ dữ liệu (validation), các quy tắc nghiệp vụ bán hàng,
 * tính toán giảm giá khuyến mãi, kiểm tra tồn kho và kết nối với tầng InvoiceDao.
 */
public class InvoiceService {

    private final InvoiceDao invoiceDao;
    private final ProductDao productDao;

    public InvoiceService() {
        this(new InvoiceDao(), new ProductDao());
    }

    public InvoiceService(InvoiceDao invoiceDao, ProductDao productDao) {
        this.invoiceDao = invoiceDao;
        this.productDao = productDao;
    }

    /**
     * Tự động sinh mã hóa đơn mới theo mẫu chuẩn HD-YYYYMMDD-NNNNNN.
     */
    public String generateNextInvoiceId() {
        return invoiceDao.generateNextInvoiceId();
    }

    /**
     * Lập mới một hóa đơn bán hàng cùng với các kiểm tra ràng buộc nghiêm ngặt.
     *
     * @param invoice Đối tượng hóa đơn cần lập
     * @return true nếu lập hóa đơn thành công
     */
    public boolean createInvoice(Invoice invoice) {
        validateInvoice(invoice);

        try {
            if (invoice.getMaHoaDon() == null || invoice.getMaHoaDon().trim().isEmpty()) {
                invoice.setMaHoaDon(invoiceDao.generateNextInvoiceId());
            }
            invoiceDao.addInvoice(invoice);
            return true;
        } catch (SQLException ex) {
            System.err.println("Lỗi lưu hóa đơn: " + ex.getMessage());
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * Hủy hóa đơn (Xóa mềm - chuyển sang trạng thái 'Đã hủy' và hoàn trả tồn kho).
     *
     * @param maHoaDon Mã hóa đơn cần hủy
     * @return true nếu hủy thành công
     */
    public boolean cancelInvoice(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống.");
        }

        try {
            invoiceDao.cancelInvoice(maHoaDon.trim());
            return true;
        } catch (SQLException ex) {
            System.err.println("Lỗi hủy hóa đơn: " + ex.getMessage());
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * Tìm kiếm và lọc hóa đơn đa tiêu chí (từ khóa, trạng thái, khoảng ngày lập).
     */
    public List<Invoice> searchInvoices(String keyword, String statusFilter, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc.");
        }
        return invoiceDao.searchInvoices(keyword, statusFilter, fromDate, toDate);
    }

    /**
     * Lấy toàn bộ danh sách hóa đơn gần nhất.
     */
    public List<Invoice> getAllInvoices() {
        return invoiceDao.searchInvoices(null, null, null, null);
    }

    /**
     * Tra cứu một hóa đơn cụ thể theo mã định danh.
     */
    public Optional<Invoice> getInvoiceById(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            return Optional.empty();
        }
        return invoiceDao.getInvoiceById(maHoaDon.trim());
    }

    /**
     * Lấy danh sách các hóa đơn đã thanh toán thành công (phục vụ bàn giao cho Phiếu đổi trả).
     */
    public List<Invoice> getPaidInvoices(String keyword) {
        return invoiceDao.getPaidInvoices(keyword);
    }

    /**
     * Kiểm tra tính hợp lệ dữ liệu hóa đơn trước khi lưu vào CSDL.
     */
    private void validateInvoice(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Thông tin hóa đơn không được null.");
        }

        if (invoice.getMaNhanVien() == null || invoice.getMaNhanVien().trim().isEmpty()) {
            throw new IllegalArgumentException("Nhân viên lập hóa đơn không được để trống.");
        }

        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất 1 mặt hàng (sản phẩm hoặc dịch vụ).");
        }

        if (invoice.getNgayLap() == null) {
            invoice.setNgayLap(LocalDate.now());
        }

        // Kiểm tra từng mặt hàng
        for (InvoiceItem item : invoice.getItems()) {
            if (item.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng cho mặt hàng [" + item.getTenHang() + "] phải lớn hơn 0.");
            }

            if (item.getDonGia() < 0) {
                throw new IllegalArgumentException("Đơn giá cho mặt hàng [" + item.getTenHang() + "] không được âm.");
            }

            // Nếu là dịch vụ: theo quy định DB soLuong phải là 1
            if (item.isService() && item.getSoLuong() != 1) {
                throw new IllegalArgumentException("Dịch vụ chăm sóc [" + item.getTenHang() + "] chỉ áp dụng số lượng là 1.");
            }

            // Nếu là sản phẩm: kiểm tra số lượng tồn kho
            if (item.getMaSanPham() != null && !item.getMaSanPham().trim().isEmpty()) {
                Optional<Product> pOpt = productDao.getProductById(item.getMaSanPham().trim());
                if (pOpt.isPresent()) {
                    Product p = pOpt.get();
                    if (invoice.getStatus() == Invoice.Status.PAID && p.getSoLuongTon() < item.getSoLuong()) {
                        throw new IllegalArgumentException("Sản phẩm [" + p.getTenSanPham() +
                                "] chỉ còn " + p.getSoLuongTon() + " trong kho, không đủ bán số lượng " + item.getSoLuong() + ".");
                    }
                }
            }
        }

        // Kiểm tra hình thức thanh toán
        if (invoice.getStatus() == Invoice.Status.PAID) {
            String method = invoice.getHinhThucThanhToan();
            if (method == null || method.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn hình thức thanh toán khi xác nhận thanh toán.");
            }
            if (!List.of("Tiền mặt", "Chuyển khoản", "POS").contains(method.trim())) {
                throw new IllegalArgumentException("Hình thức thanh toán phải là 'Tiền mặt', 'Chuyển khoản' hoặc 'POS'.");
            }
        }
    }
}

