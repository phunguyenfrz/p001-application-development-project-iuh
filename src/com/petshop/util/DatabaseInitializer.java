package com.petshop.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Kiểm tra kết nối database và tự động đồng bộ các cột/bảng bổ sung khi ứng dụng khởi động.
 * Schema và dữ liệu mẫu gốc được tạo bằng script QLCH_HappyTails_Database_Full.sql.
 */
public final class DatabaseInitializer {

    private DatabaseInitializer() {}

    /**
     * Thử mở kết nối tới database và tự động chạy các bản vá schema còn thiếu.
     * @return true nếu kết nối thành công.
     */
    public static boolean initialize() {
        try (Connection conn = DatabaseManager.getConnection()) {
            if (!conn.isValid(3)) {
                return false;
            }
            ensureMigrations(conn);
            return true;
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Không kết nối được database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đảm bảo các cột/bảng từ các bản nâng cấp (db/01..04, Đổi trả, Ca, Khuyến mãi, Nhà cung cấp)
     * luôn tồn tại trên CSDL hiện hành.
     */
    public static void ensureMigrations(Connection conn) {
        String[] migrations = {
            // 01_customer_status.sql: KhachHang.trangThai
            """
            IF COL_LENGTH('dbo.KhachHang', 'trangThai') IS NULL
                ALTER TABLE dbo.KhachHang ADD trangThai BIT NOT NULL CONSTRAINT DF_KhachHang_TrangThai DEFAULT (1) WITH VALUES;
            """,
            // 02_audit_log.sql: Bảng NhatKyThaoTac
            """
            IF OBJECT_ID('dbo.NhatKyThaoTac', 'U') IS NULL
            BEGIN
                CREATE TABLE dbo.NhatKyThaoTac (
                    maNhatKy BIGINT IDENTITY(1,1) PRIMARY KEY,
                    thoiGian DATETIME2 NOT NULL CONSTRAINT DF_NhatKyThaoTac_ThoiGian DEFAULT (SYSDATETIME()),
                    maNhanVienThucHien VARCHAR(20) NULL,
                    tenDangNhap VARCHAR(50) NULL,
                    loaiDoiTuong NVARCHAR(30) NOT NULL,
                    maDoiTuong VARCHAR(20) NOT NULL,
                    hanhDong NVARCHAR(50) NOT NULL,
                    moTa NVARCHAR(500) NULL
                );
            END
            """,
            // 03_pet_status.sql: ThuCung.trangThai
            """
            IF COL_LENGTH('dbo.ThuCung', 'trangThai') IS NULL
                ALTER TABLE dbo.ThuCung ADD trangThai BIT NOT NULL CONSTRAINT DF_ThuCung_TrangThai DEFAULT (1) WITH VALUES;
            """,
            // 04_employee_profile.sql: NhanVien (email, diaChi, ngayBatDau, anh)
            """
            IF COL_LENGTH('dbo.NhanVien', 'email') IS NULL
                ALTER TABLE dbo.NhanVien ADD email VARCHAR(100) NULL;
            IF COL_LENGTH('dbo.NhanVien', 'diaChi') IS NULL
                ALTER TABLE dbo.NhanVien ADD diaChi NVARCHAR(255) NULL;
            IF COL_LENGTH('dbo.NhanVien', 'ngayBatDau') IS NULL
                ALTER TABLE dbo.NhanVien ADD ngayBatDau DATE NULL;
            IF COL_LENGTH('dbo.NhanVien', 'anh') IS NULL
                ALTER TABLE dbo.NhanVien ADD anh NVARCHAR(255) NULL;
            """,
            // NhaCungCap: email, matHangCungCap, trangThai
            """
            IF COL_LENGTH('dbo.NhaCungCap', 'email') IS NULL
                ALTER TABLE dbo.NhaCungCap ADD email VARCHAR(100) NULL;
            IF COL_LENGTH('dbo.NhaCungCap', 'matHangCungCap') IS NULL
                ALTER TABLE dbo.NhaCungCap ADD matHangCungCap NVARCHAR(200) NULL;
            IF COL_LENGTH('dbo.NhaCungCap', 'trangThai') IS NULL
                ALTER TABLE dbo.NhaCungCap ADD trangThai BIT NOT NULL CONSTRAINT DF_NhaCungCap_TrangThai DEFAULT (1) WITH VALUES;
            """,
            // Ca.ngayCapNhat & KhuyenMai.ngayCapNhat & PhieuDoiTra.tongTienHoan
            """
            IF COL_LENGTH('dbo.Ca', 'ngayCapNhat') IS NULL
                ALTER TABLE dbo.Ca ADD ngayCapNhat DATE DEFAULT GETDATE() WITH VALUES;
            IF COL_LENGTH('dbo.KhuyenMai', 'ngayCapNhat') IS NULL
                ALTER TABLE dbo.KhuyenMai ADD ngayCapNhat DATE DEFAULT GETDATE() WITH VALUES;
            IF COL_LENGTH('dbo.PhieuDoiTra', 'tongTienHoan') IS NULL
                ALTER TABLE dbo.PhieuDoiTra ADD tongTienHoan DECIMAL(18,2) DEFAULT 0 WITH VALUES;
            """,
            // Bảng ChiTietPhieuDoiTra nếu CSDL cũ chưa có
            """
            IF OBJECT_ID('dbo.ChiTietPhieuDoiTra', 'U') IS NULL
            BEGIN
                CREATE TABLE dbo.ChiTietPhieuDoiTra (
                    maPhieuDoiTra VARCHAR(20) NOT NULL,
                    maHoaDon VARCHAR(20) NOT NULL,
                    maSanPham VARCHAR(20) NOT NULL,
                    soLuong INT NOT NULL CHECK (soLuong > 0),
                    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
                    PRIMARY KEY (maPhieuDoiTra, maSanPham),
                    CONSTRAINT FK_CTPhieuDoiTra_PhieuDoiTra FOREIGN KEY (maPhieuDoiTra) REFERENCES dbo.PhieuDoiTra(maPhieuDoiTra),
                    CONSTRAINT FK_CTPhieuDoiTra_ChiTietHDSanPham FOREIGN KEY (maHoaDon, maSanPham) REFERENCES dbo.ChiTietHoaDon_SanPham(maHoaDon, maSanPham)
                );
            END
            """
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : migrations) {
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {
                    System.err.println("[DatabaseInitializer] Lưu ý khi cập nhật schema: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Không thể tạo Statement cập nhật schema: " + e.getMessage());
        }
    }
}

