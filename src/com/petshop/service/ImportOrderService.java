package com.petshop.service;

import com.petshop.dao.ImportOrderDao;
import com.petshop.model.ImportOrder;
import com.petshop.model.ImportOrderItem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lớp dịch vụ nghiệp vụ (Service Layer) cho Đơn hàng nhập.
 * Chịu trách nhiệm kiểm tra tính hợp lệ dữ liệu và thực thi các quy tắc nghiệp vụ theo OOAD.
 */
public class ImportOrderService {

    private final ImportOrderDao importOrderDao;

    public ImportOrderService() {
        this.importOrderDao = new ImportOrderDao();
    }

    public ImportOrderService(ImportOrderDao dao) {
        this.importOrderDao = dao != null ? dao : new ImportOrderDao();
    }

    /**
     * Sinh mã đơn hàng nhập kế tiếp: DH-YYYYMMDD-NNNNNN.
     */
    public String generateNextOrderId() {
        return importOrderDao.generateNextOrderId();
    }

    /**
     * Tạo mới đơn hàng nhập sau khi kiểm tra đầy đủ tính hợp lệ nghiệp vụ.
     *
     * @param order Đối tượng đơn hàng nhập
     */
    public void createImportOrder(ImportOrder order) {
        validateOrder(order);
        try {
            order.setTrangThai(ImportOrder.TRANG_THAI_CHO_NHAN);
            order.tinhTongTien();
            importOrderDao.createImportOrder(order);
        } catch (SQLException ex) {
            throw new RuntimeException("Lỗi khi lưu đơn hàng nhập vào CSDL: " + ex.getMessage(), ex);
        }
    }

    /**
     * Xác nhận nhận hàng (IMP_CONFIRM / IMP_RECEIVE):
     * Cập nhật trạng thái thành 'Hoàn tất' và cộng số lượng tồn kho tương ứng.
     */
    public void confirmReceiveOrder(String maDonHang) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng nhập không được để trống.");
        }
        try {
            importOrderDao.confirmReceiveOrder(maDonHang.trim());
        } catch (SQLException ex) {
            throw new RuntimeException("Không thể nhận hàng: " + ex.getMessage(), ex);
        }
    }

    /**
     * Hủy đơn hàng nhập (IMP_CANCEL):
     * Thực hiện xóa mềm bằng cách đổi trạng thái đơn sang 'Đã hủy'.
     */
    public void cancelImportOrder(String maDonHang) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng nhập không được để trống.");
        }
        try {
            importOrderDao.cancelImportOrder(maDonHang.trim());
        } catch (SQLException ex) {
            throw new RuntimeException("Không thể hủy đơn hàng: " + ex.getMessage(), ex);
        }
    }

    /**
     * Cập nhật thông tin đơn hàng nhập (khi đơn còn ở trạng thái 'Chờ nhận hàng').
     */
    public void updateImportOrder(ImportOrder order) {
        validateOrder(order);
        try {
            order.tinhTongTien();
            importOrderDao.updateImportOrder(order);
        } catch (SQLException ex) {
            throw new RuntimeException("Không thể cập nhật đơn hàng: " + ex.getMessage(), ex);
        }
    }

    /**
     * Tìm kiếm và lọc danh sách đơn hàng nhập.
     */
    public List<ImportOrder> searchImportOrders(String keyword, String status, LocalDate fromDate, LocalDate toDate) {
        return importOrderDao.searchImportOrders(keyword, status, fromDate, toDate);
    }

    /**
     * Tra cứu một đơn hàng nhập kèm các mặt hàng chi tiết.
     */
    public Optional<ImportOrder> getOrderById(String maDonHang) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            return Optional.empty();
        }
        return importOrderDao.getOrderById(maDonHang.trim());
    }

    /**
     * Kiểm tra tính hợp lệ của đơn hàng nhập theo các ràng buộc nghiệp vụ.
     */
    private void validateOrder(ImportOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Thông tin đơn hàng nhập không được để trống.");
        }

        if (order.getMaNhaCungCap() == null || order.getMaNhaCungCap().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn Nhà cung cấp đối tác cho đơn hàng nhập.");
        }

        if (order.getMaNhanVien() == null || order.getMaNhanVien().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên lập đơn không hợp lệ.");
        }

        if (order.getNgayLap() == null) {
            throw new IllegalArgumentException("Ngày lập đơn nhập không được để trống.");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Đơn hàng nhập phải có ít nhất một mặt hàng sản phẩm.");
        }

        for (ImportOrderItem item : order.getItems()) {
            if (item.getMaSanPham() == null || item.getMaSanPham().trim().isEmpty()) {
                throw new IllegalArgumentException("Mã sản phẩm trong chi tiết đơn nhập không hợp lệ.");
            }
            if (item.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng nhập của sản phẩm '" + item.getTenSanPham() + "' phải lớn hơn 0.");
            }
            if (item.getDonGia() < 0) {
                throw new IllegalArgumentException("Đơn giá nhập của sản phẩm '" + item.getTenSanPham() + "' không được nhỏ hơn 0.");
            }
        }
    }
}

