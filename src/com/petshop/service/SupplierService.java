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

    /**
     * Khởi tạo SupplierService mặc định.
     */
    public SupplierService() {
        this.supplierDao = new SupplierDao();
        this.authService = new AuthService();
    }

    /**
     * Khởi tạo SupplierService với DAO và AuthService tùy chỉnh.
     */
    public SupplierService(SupplierDao supplierDao, AuthService authService) {
        this.supplierDao = supplierDao;
        this.authService = authService;
    }

    /**
     * Lấy danh sách tất cả nhà cung cấp (bao gồm cả ngừng hoạt động nếu cần).
     *
     * @param includeInactive true để lấy cả nhà cung cấp đã ngừng hoạt động
     * @return Danh sách Supplier
     */
    public List<Supplier> getAllSuppliers(boolean includeInactive) {
        return supplierDao.findAll(includeInactive);
    }

    /**
     * Lấy danh sách các nhà cung cấp đang hợp tác (phục vụ hộp thoại chọn NCC khi lập đơn nhập).
     *
     * @return Danh sách Supplier đang hoạt động
     */
    public List<Supplier> getActiveSuppliers() {
        return supplierDao.findAll(false);
    }

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa và trạng thái.
     *
     * @param keyword      Từ khóa tìm kiếm (mã, tên, SĐT, email, địa chỉ)
     * @param activeFilter null: tất cả, true: Đang hợp tác, false: Ngừng hoạt động
     * @return Danh sách kết quả
     */
    public List<Supplier> searchSuppliers(String keyword, Boolean activeFilter) {
        return supplierDao.searchSuppliers(keyword, activeFilter);
    }

    /**
     * Sinh mã nhà cung cấp mới tự động theo chuẩn NCC-YYYYMMDD-NNNNNN.
     *
     * @return Mã nhà cung cấp mới
     */
    public String generateNextSupplierId() {
        return supplierDao.generateNextSupplierId();
    }

    /**
     * Thêm mới nhà cung cấp sau khi kiểm tra quyền và xác thực dữ liệu bằng Regex.
     *
     * @param supplier Đối tượng nhà cung cấp cần thêm
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     * @throws SecurityException        nếu tài khoản không có quyền SUP_UPDATE
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
     *
     * @param supplier Đối tượng nhà cung cấp cần cập nhật
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     * @throws SecurityException        nếu tài khoản không có quyền SUP_UPDATE
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
     *
     * @param maNhaCungCap Mã nhà cung cấp cần ngừng hoạt động
     * @throws SecurityException nếu tài khoản không có quyền SUP_UPDATE
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
     *
     * @param supplier Đối tượng nhà cung cấp cần kiểm tra
     * @throws IllegalArgumentException nếu có trường vi phạm ràng buộc
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
        if (!email.isEmpty() && !REGEX_EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Địa chỉ email không đúng định dạng (ví dụ: ten@domain.com)!");
        }
        supplier.setEmail(email);

        String address = supplier.getDiaChi() != null ? supplier.getDiaChi().trim() : "";
        if (address.isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ nhà cung cấp không được để trống!");
        }
        supplier.setDiaChi(address);
    }
}

