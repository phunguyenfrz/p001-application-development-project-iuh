package com.petshop.service;

import com.petshop.dao.CareServiceDao;
import com.petshop.model.CareService;
import com.petshop.model.ServicePricing;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Lớp dịch vụ nghiệp vụ quản lý Dịch vụ chăm sóc và Bảng giá dịch vụ (CareServiceService).
 * Thực hiện kiểm tra tính hợp lệ dữ liệu (validation), các quy tắc nghiệp vụ,
 * và điều phối tương tác với tầng CareServiceDao.
 */
public class CareServiceService {

    private final CareServiceDao careServiceDao;

    public CareServiceService() {
        this(new CareServiceDao());
    }

    public CareServiceService(CareServiceDao careServiceDao) {
        this.careServiceDao = careServiceDao;
    }

    // =========================================================================
    // NGHIỆP VỤ DỊCH VỤ CHĂM SÓC (CareService)
    // =========================================================================

    /**
     * Tìm kiếm và lọc danh sách dịch vụ chăm sóc theo từ khóa và trạng thái hoạt động.
     */
    public List<CareService> searchCareServices(String keyword, Boolean activeFilter) {
        return careServiceDao.searchCareServices(keyword, activeFilter);
    }

    /**
     * Lấy toàn bộ danh sách dịch vụ chăm sóc.
     */
    public List<CareService> getAllCareServices() {
        return careServiceDao.searchCareServices(null, null);
    }

    /**
     * Lấy danh sách dịch vụ đang mở cung cấp (hoạt động).
     */
    public List<CareService> getActiveCareServices() {
        return careServiceDao.searchCareServices(null, true);
    }

    /**
     * Tra cứu dịch vụ chăm sóc theo mã định danh.
     */
    public Optional<CareService> getServiceById(String maDichVu) {
        if (maDichVu == null || maDichVu.trim().isEmpty()) {
            return Optional.empty();
        }
        return careServiceDao.getServiceById(maDichVu.trim());
    }

    /**
     * Thêm mới hoặc cập nhật thông tin dịch vụ chăm sóc.
     * Thực hiện xác thực dữ liệu chặt chẽ theo Definition of Done.
     *
     * @param service Đối tượng dịch vụ chăm sóc
     * @param isCreate true nếu là thêm mới, false nếu là cập nhật
     */
    public boolean saveCareService(CareService service, boolean isCreate) {
        validateCareService(service, isCreate);

        try {
            if (isCreate) {
                if (service.getMaDichVu() == null || service.getMaDichVu().trim().isEmpty()) {
                    service.setMaDichVu(careServiceDao.generateNextServiceId());
                }
                careServiceDao.addCareService(service);
            } else {
                careServiceDao.updateCareService(service);
            }
            return true;
        } catch (SQLException ex) {
            System.err.println("Lỗi lưu dịch vụ chăm sóc: " + ex.getMessage());
            throw new IllegalStateException("Lỗi cơ sở dữ liệu khi lưu dịch vụ: " + ex.getMessage(), ex);
        }
    }

    /**
     * Ngừng cung cấp dịch vụ (Xóa mềm - trangThai = 0).
     */
    public boolean deactivateCareService(String maDichVu) {
        if (maDichVu == null || maDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không được để trống.");
        }
        try {
            careServiceDao.deactivateCareService(maDichVu.trim());
            return true;
        } catch (SQLException ex) {
            System.err.println("Lỗi ngừng cung cấp dịch vụ: " + ex.getMessage());
            throw new IllegalStateException("Lỗi khi ngừng cung cấp dịch vụ: " + ex.getMessage(), ex);
        }
    }

    /**
     * Kích hoạt lại dịch vụ chăm sóc đã tạm ngừng (trangThai = 1).
     */
    public boolean activateCareService(String maDichVu) {
        if (maDichVu == null || maDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không được để trống.");
        }
        try {
            careServiceDao.activateCareService(maDichVu.trim());
            return true;
        } catch (SQLException ex) {
            System.err.println("Lỗi kích hoạt lại dịch vụ: " + ex.getMessage());
            throw new IllegalStateException("Lỗi khi kích hoạt lại dịch vụ: " + ex.getMessage(), ex);
        }
    }

    /**
     * Tự động tạo mã dịch vụ tiếp theo.
     */
    public String generateNextServiceId() {
        return careServiceDao.generateNextServiceId();
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu dịch vụ chăm sóc.
     */
    private void validateCareService(CareService service, boolean isCreate) {
        if (service == null) {
            throw new IllegalArgumentException("Thông tin dịch vụ chăm sóc không được null.");
        }

        if (!isCreate && (service.getMaDichVu() == null || service.getMaDichVu().trim().isEmpty())) {
            throw new IllegalArgumentException("Mã dịch vụ không được để trống khi cập nhật.");
        }

        if (service.getTenDichVu() == null || service.getTenDichVu().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }

        if (service.getTenDichVu().trim().length() > 255) {
            throw new IllegalArgumentException("Tên dịch vụ không được vượt quá 255 ký tự.");
        }

        if (service.getThoiLuong() <= 0) {
            throw new IllegalArgumentException("Thời lượng dịch vụ phải lớn hơn 0 phút.");
        }

        if (service.getThoiLuong() > 1440) {
            throw new IllegalArgumentException("Thời lượng dịch vụ không được vượt quá 1440 phút (24 giờ).");
        }
    }

    // =========================================================================
    // NGHIỆP VỤ BẢNG GIÁ DỊCH VỤ (ServicePricing)
    // =========================================================================

    /**
     * Tìm kiếm và lọc danh sách bảng giá theo từ khóa và bậc kích thước.
     */
    public List<ServicePricing> searchServicePricings(String keyword, String sizeFilter) {
        return careServiceDao.searchServicePricings(keyword, sizeFilter);
    }

    /**
     * Thêm mới hoặc cập nhật một mức giá dịch vụ.
     */
    public boolean saveServicePricing(ServicePricing pricing, boolean isCreate) {
        validateServicePricing(pricing, isCreate);

        try {
            if (isCreate) {
                if (pricing.getMaBangGia() == null || pricing.getMaBangGia().trim().isEmpty()) {
                    pricing.setMaBangGia(careServiceDao.generateNextPricingId());
                }
                careServiceDao.addServicePricing(pricing);
            } else {
                careServiceDao.updateServicePricing(pricing);
            }
            return true;
        } catch (SQLException ex) {
            System.err.println("Lỗi lưu bảng giá dịch vụ: " + ex.getMessage());
            throw new IllegalStateException("Lỗi cơ sở dữ liệu khi lưu bảng giá: " + ex.getMessage(), ex);
        }
    }

    /**
     * Tự động tạo mã bảng giá tiếp theo.
     */
    public String generateNextPricingId() {
        return careServiceDao.generateNextPricingId();
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu bảng giá dịch vụ.
     */
    private void validateServicePricing(ServicePricing pricing, boolean isCreate) {
        if (pricing == null) {
            throw new IllegalArgumentException("Thông tin bảng giá dịch vụ không được null.");
        }

        if (pricing.getMaDichVu() == null || pricing.getMaDichVu().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ áp dụng bảng giá.");
        }

        if (pricing.getGia() < 0) {
            throw new IllegalArgumentException("Đơn giá dịch vụ phải lớn hơn hoặc bằng 0.");
        }

        String size = pricing.getPhanLoaiKichThuoc();
        if (size == null || (!size.equals(ServicePricing.BAC_NHO) && !size.equals(ServicePricing.BAC_VUA) && !size.equals(ServicePricing.BAC_LON))) {
            throw new IllegalArgumentException("Phân loại kích thước không hợp lệ. Phải là 'Nhỏ', 'Vừa' hoặc 'Lớn'.");
        }

        if (pricing.getNgayApDung() == null) {
            throw new IllegalArgumentException("Ngày áp dụng bảng giá không được để trống.");
        }

        if (pricing.getNgayHetHieuLuc() != null && pricing.getNgayHetHieuLuc().isBefore(pricing.getNgayApDung())) {
            throw new IllegalArgumentException("Ngày hết hiệu lực phải sau hoặc bằng ngày áp dụng.");
        }
    }

    /**
     * Tra cứu giá dịch vụ theo cân nặng thú cưng.
     */
    public double getPriceForWeight(String maDichVu, double canNang, java.time.LocalDate date) {
        return careServiceDao.getPriceForWeight(maDichVu, canNang, date);
    }

    /**
     * Tra cứu giá dịch vụ theo phân loại kích thước ('Nhỏ', 'Vừa', 'Lớn').
     */
    public double getPriceForSize(String maDichVu, String size, java.time.LocalDate date) {
        return careServiceDao.getPriceForSize(maDichVu, size, date);
    }
}

