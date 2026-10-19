package com.petshop.service;

import com.petshop.dao.ProductDao;
import com.petshop.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Lớp dịch vụ nghiệp vụ quản lý sản phẩm và kho hàng (ProductService).
 * Thực hiện kiểm tra tính hợp lệ dữ liệu (validation), quy tắc nghiệp vụ,
 * tính duy nhất của mã vạch, và kết nối với tầng DAO.
 */
public class ProductService {

    private final ProductDao productDao;

    public ProductService() {
        this(new ProductDao());
    }

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    /**
     * Lấy toàn bộ danh sách sản phẩm trong kho.
     */
    public List<Product> getAllProducts() {
        return productDao.getAllProducts();
    }

    /**
     * Lấy danh sách sản phẩm đang mở bán.
     */
    public List<Product> getActiveProducts() {
        return productDao.getActiveProducts();
    }

    /**
     * Lấy danh sách sản phẩm sắp hết hàng (tồn kho <= ngưỡng an toàn).
     */
    public List<Product> getLowStockProducts() {
        return productDao.getLowStockProducts();
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa (Mã, Tên, Mã vạch, Nhà cung cấp).
     */
    public List<Product> searchProducts(String keyword, Boolean onlyActive) {
        return productDao.searchProducts(keyword, onlyActive);
    }

    /**
     * Tra cứu sản phẩm theo mã định danh.
     */
    public Optional<Product> getProductById(String maSanPham) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            return Optional.empty();
        }
        return productDao.getProductById(maSanPham.trim());
    }

    /**
     * Tra cứu sản phẩm bằng Mã vạch (Hỗ trợ máy quét và lập hóa đơn).
     */
    public Optional<Product> getProductByBarcode(String maVach) {
        if (maVach == null || maVach.trim().isEmpty()) {
            return Optional.empty();
        }
        return productDao.getProductByBarcode(maVach.trim());
    }

    /**
     * Thêm mới hoặc cập nhật sản phẩm cùng với quy tắc xác thực dữ liệu chặt chẽ.
     *
     * @param product Đối tượng sản phẩm
     * @param isCreate true nếu là thêm mới, false nếu là cập nhật
     */
    public boolean saveProduct(Product product, boolean isCreate) {
        validateProduct(product, isCreate);

        if (isCreate) {
            if (product.getMaSanPham() == null || product.getMaSanPham().trim().isEmpty()) {
                product.setMaSanPham(productDao.generateNextProductId());
            }
            return productDao.addProduct(product);
        } else {
            return productDao.updateProduct(product);
        }
    }

    /**
     * Cập nhật số lượng tồn kho (PRD_INVENTORY).
     */
    public boolean updateStock(String maSanPham, int soLuongTon) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống.");
        }
        if (soLuongTon < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
        }
        return productDao.updateStock(maSanPham.trim(), soLuongTon);
    }

    /**
     * Cập nhật ngưỡng tồn tối thiểu an toàn (PRD_INVENTORY & PRD_LOW_STOCK).
     */
    public boolean updateThreshold(String maSanPham, int nguongTonToiThieu) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống.");
        }
        if (nguongTonToiThieu < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn tối thiểu không được âm.");
        }
        return productDao.updateThreshold(maSanPham.trim(), nguongTonToiThieu);
    }

    /**
     * Ngừng kinh doanh sản phẩm (Xóa mềm - trangThai = 0).
     */
    public boolean deactivateProduct(String maSanPham) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống.");
        }
        return productDao.deactivateProduct(maSanPham.trim());
    }

    /**
     * Kích hoạt lại sản phẩm đang ngừng kinh doanh.
     */
    public boolean activateProduct(String maSanPham) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được để trống.");
        }
        return productDao.activateProduct(maSanPham.trim());
    }

    /**
     * Sinh mã sản phẩm tiếp theo.
     */
    public String generateNextProductId() {
        return productDao.generateNextProductId();
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu sản phẩm theo tiêu chuẩn OOAD và CSDL.
     */
    private void validateProduct(Product product, boolean isCreate) {
        if (product == null) {
            throw new IllegalArgumentException("Thông tin sản phẩm không được rỗng.");
        }

        // Tên sản phẩm
        if (product.getTenSanPham() == null || product.getTenSanPham().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        if (product.getTenSanPham().trim().length() > 150) {
            throw new IllegalArgumentException("Tên sản phẩm không được vượt quá 150 ký tự.");
        }

        // Mã vạch
        if (product.getMaVach() == null || product.getMaVach().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã vạch sản phẩm không được để trống.");
        }
        if (product.getMaVach().trim().length() > 50) {
            throw new IllegalArgumentException("Mã vạch không được vượt quá 50 ký tự.");
        }

        // Kiểm tra trùng mã vạch
        String excludeId = isCreate ? null : product.getMaSanPham();
        if (productDao.isBarcodeExists(product.getMaVach().trim(), excludeId)) {
            throw new IllegalArgumentException("Mã vạch '" + product.getMaVach().trim() + "' đã được sử dụng cho sản phẩm khác.");
        }

        // Đơn vị tính
        if (product.getDonViTinh() == null || product.getDonViTinh().trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị tính không được để trống.");
        }

        // Giá bán
        if (product.getGiaBan() < 0) {
            throw new IllegalArgumentException("Giá bán sản phẩm không được âm.");
        }

        // Số lượng tồn
        if (product.getSoLuongTon() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
        }

        // Ngưỡng tồn tối thiểu
        if (product.getNguongTonToiThieu() < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn tối thiểu không được âm.");
        }
    }
}

