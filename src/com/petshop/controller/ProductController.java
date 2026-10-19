package com.petshop.controller;

import com.petshop.model.Product;
import com.petshop.service.ProductService;

import java.util.List;
import java.util.Optional;

/**
 * Controller điều phối giữa Giao diện người dùng (View) và Nghiệp vụ sản phẩm (Service).
 * Đảm bảo các View không truy vấn CSDL hay xử lý nghiệp vụ trực tiếp.
 */
public class ProductController {

    private final ProductService productService;

    public ProductController() {
        this(new ProductService());
    }

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    public List<Product> getActiveProducts() {
        return productService.getActiveProducts();
    }

    public List<Product> getLowStockProducts() {
        return productService.getLowStockProducts();
    }

    public List<Product> searchProducts(String keyword, Boolean onlyActive) {
        return productService.searchProducts(keyword, onlyActive);
    }

    public Optional<Product> getProductById(String maSanPham) {
        return productService.getProductById(maSanPham);
    }

    public Optional<Product> getProductByBarcode(String maVach) {
        return productService.getProductByBarcode(maVach);
    }

    public boolean saveProduct(Product product, boolean isCreate) {
        return productService.saveProduct(product, isCreate);
    }

    public boolean updateStock(String maSanPham, int soLuongTon) {
        return productService.updateStock(maSanPham, soLuongTon);
    }

    public boolean updateThreshold(String maSanPham, int nguongTonToiThieu) {
        return productService.updateThreshold(maSanPham, nguongTonToiThieu);
    }

    public boolean deactivateProduct(String maSanPham) {
        return productService.deactivateProduct(maSanPham);
    }

    public boolean activateProduct(String maSanPham) {
        return productService.activateProduct(maSanPham);
    }

    public String generateNextProductId() {
        return productService.generateNextProductId();
    }
}

