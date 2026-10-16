package com.petshop.service;

import com.petshop.dao.SupplierDao;
import com.petshop.model.Supplier;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Lớp Service xử lý nghiệp vụ và kiểm tra tính hợp lệ dữ liệu cho đối tượng Nhà cung cấp.
 * Thực thi kiểm tra phân quyền, Regex số điện thoại (0 + 9 chữ số), Regex email và xóa mềm.
 */
public class SupplierService {

    /** Regex kiểm tra mã nhà cung cấp: NCC-YYYYMMDD-NNNNNN */
    public static final Pattern REGEX_SUPPLIER_ID = Pattern.compile("^NCC-\\d{8}-\\d{6}$");

    /** Regex kiểm tra số điện thoại: bắt đầu bằng số 0 và theo sau bởi 9 chữ số (tổng 10 số) */
    public static final Pattern REGEX_PHONE = Pattern.compile("^0\\d{9}$");

    /** Regex kiểm tra định dạng email hợp lệ */
    public static final Pattern REGEX_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final SupplierDao supplierDao;
    private final AuthService authService;

    public SupplierService() {
        this.supplierDao = new SupplierDao();
        this.authService = new AuthService();
    }

    /**
     * Tìm kiếm hoặc lấy danh sách nhà cung cấp theo từ khóa và trạng thái.
     *
     * @param keyword      Từ khóa tìm kiếm (rỗng nếu lấy tất cả)
     * @param activeFilter null: tất cả, true: Đang hợp tác, false: Ngừng hoạt động
     */
    public List<Supplier> searchSuppliers(String keyword, Boolean activeFilter) {
        return supplierDao.searchSuppliers(keyword, activeFilter);
    }

    /**
     * Sinh mã nhà cung cấp mới tự động theo chuẩn NCC-YYYYMMDD-NNNNNN.
     */
    public String generateNextSupplierId() {
        return supplierDao.generateNextSupplierId();
    }

    /**
     * Thêm mới nhà cung cấp sau khi kiểm tra quyền và xác thực dữ liệu bằng Regex.
     */
    public void addSupplier(Supplier supplier) {
        authService.checkPermission("SUP_UPDATE");
        validateSupplierData(supplier);

        if (supplierDao.findById(supplier.getMaNhaCungCap()) != null) {
            throw new IllegalArgumentException("Mã nhà cung cấp [" + supplier.getMaNhaCungCap() + "] đã tồn tại!");
        }

        supplierDao.insertSupplier(supplier);
    }

    /**
     * Cập nhật thông tin nhà cung cấp sau khi kiểm tra quyền và xác thực Regex.
     */
    public void updateSupplier(Supplier supplier) {
        authService.checkPermission("SUP_UPDATE");
        validateSupplierData(supplier);

        if (supplierDao.findById(supplier.getMaNhaCungCap()) == null) {
            throw new IllegalArgumentException("Không tìm thấy nhà cung cấp có mã [" + supplier.getMaNhaCungCap() + "]!");
        }

        supplierDao.updateSupplier(supplier);
    }

    /**
     * Ngừng hoạt động nhà cung cấp (Xóa mềm - đặt trangThai = false).
     */
    public void deactivateSupplier(String maNhaCungCap) {
        authService.checkPermission("SUP_UPDATE");
        if (maNhaCungCap == null || maNhaCungCap.isBlank()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không được để trống!");
        }
        supplierDao.deactivateSupplier(maNhaCungCap.trim());
    }

    /**
     * Kiểm tra tính hợp lệ của thông tin nhà cung cấp bằng biểu thức chính quy (Regex).
     */
    public void validateSupplierData(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("Dữ liệu nhà cung cấp không hợp lệ!");
        }

        String id = supplier.getMaNhaCungCap() != null ? supplier.getMaNhaCungCap().trim() : "";
        if (!REGEX_SUPPLIER_ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Mã nhà cung cấp phải đúng định dạng NCC-YYYYMMDD-NNNNNN!");
        }
        supplier.setMaNhaCungCap(id);

        String name = supplier.getTenNhaCungCap() != null ? supplier.getTenNhaCungCap().trim() : "";
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống!");
        }
        if (name.length() > 150) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được vượt quá 150 ký tự!");
        }
        supplier.setTenNhaCungCap(name);

        String phone = supplier.getSoDienThoai() != null ? supplier.getSoDienThoai().trim() : "";
        if (!REGEX_PHONE.matcher(phone).matches()) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ! Phải bắt đầu bằng số 0 và gồm đúng 10 chữ số.");
        }
        supplier.setSoDienThoai(phone);

        String email = supplier.getEmail() != null ? supplier.getEmail().trim() : "";
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống!");
        }
        if (!REGEX_EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Địa chỉ email không đúng định dạng (ví dụ: ten@domain.com)!");
        }
        supplier.setEmail(email);

        String address = supplier.getDiaChi() != null ? supplier.getDiaChi().trim() : "";
        supplier.setDiaChi(address);

        String productItems = supplier.getMatHangCungCap() != null ? supplier.getMatHangCungCap().trim() : "";
        if (productItems.isEmpty()) {
            throw new IllegalArgumentException("Mặt hàng cung cấp không được để trống!");
        }
        supplier.setMatHangCungCap(productItems);
    }
}
