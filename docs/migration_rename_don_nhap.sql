/*
 * MIGRATION SCRIPT: Đổi tên bảng DonHang -> DonNhapHang và ChiTietDonHang -> ChiTietDonNhap
 * Hệ quản trị: Microsoft SQL Server
 * An toàn, có thể chạy lại nhiều lần (idempotent), không làm mất dữ liệu.
 */

-- 1. Đổi tên bảng DonHang -> DonNhapHang (nếu chưa đổi)
IF OBJECT_ID('dbo.DonHang', 'U') IS NOT NULL AND OBJECT_ID('dbo.DonNhapHang', 'U') IS NULL
BEGIN
    EXEC sp_rename 'dbo.DonHang', 'DonNhapHang';
    PRINT N'Đã đổi tên bảng DonHang thành DonNhapHang thành công.';
END
GO

-- 2. Đổi tên bảng ChiTietDonHang -> ChiTietDonNhap (nếu chưa đổi)
IF OBJECT_ID('dbo.ChiTietDonHang', 'U') IS NOT NULL AND OBJECT_ID('dbo.ChiTietDonNhap', 'U') IS NULL
BEGIN
    EXEC sp_rename 'dbo.ChiTietDonHang', 'ChiTietDonNhap';
    PRINT N'Đã đổi tên bảng ChiTietDonHang thành ChiTietDonNhap thành công.';
END
GO

-- 3. Cập nhật ràng buộc CHECK trên maDonHang (hỗ trợ cả mã cũ DH- và mã mới PN-)
-- Trước hết xóa constraint cũ nếu có
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_DonNhapHang_maDonHang' OR parent_object_id = OBJECT_ID('dbo.DonNhapHang'))
BEGIN
    DECLARE @chkName NVARCHAR(256);
    SELECT TOP 1 @chkName = name 
    FROM sys.check_constraints 
    WHERE parent_object_id = OBJECT_ID('dbo.DonNhapHang') 
      AND definition LIKE '%maDonHang%';
    IF @chkName IS NOT NULL
    BEGIN
        DECLARE @dropSql NVARCHAR(MAX) = N'ALTER TABLE dbo.DonNhapHang DROP CONSTRAINT ' + QUOTENAME(@chkName);
        EXEC sp_executesql @dropSql;
        PRINT N'Đã gỡ constraint kiểm tra định dạng cũ: ' + @chkName;
    END
END
GO

-- Thêm constraint mới cho phép cả PN- và DH-
IF OBJECT_ID('dbo.DonNhapHang', 'U') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_DonNhapHang_maDonHang' AND parent_object_id = OBJECT_ID('dbo.DonNhapHang'))
BEGIN
    ALTER TABLE dbo.DonNhapHang ADD CONSTRAINT CK_DonNhapHang_maDonHang 
        CHECK (maDonHang LIKE 'PN-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]' 
            OR maDonHang LIKE 'DH-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]');
    PRINT N'Đã thêm constraint CK_DonNhapHang_maDonHang hỗ trợ cả PN- và DH-.';
END
GO

