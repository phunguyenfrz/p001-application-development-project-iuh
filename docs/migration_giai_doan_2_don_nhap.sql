/*
 * MIGRATION SCRIPT: Giai đoạn 2 - Đơn nhập hàng
 * Bổ sung:
 *  - ChiTietDonNhap: soLuongThucNhan (INT, >= 0)
 *  - DonNhapHang: tongTienThucNhan (DECIMAL(18,2)), maNhanVienNhan, ngayGioNhan, maNhanVienHuy, lyDoHuy
 * An toàn, có thể chạy lại nhiều lần (idempotent), không làm mất dữ liệu.
 */

-- 1. Bổ sung cột vào ChiTietDonNhap (hoặc ChiTietDonHang nếu chưa đổi tên)
DECLARE @tblCT NVARCHAR(128) = CASE 
    WHEN OBJECT_ID('dbo.ChiTietDonNhap', 'U') IS NOT NULL THEN 'dbo.ChiTietDonNhap'
    ELSE 'dbo.ChiTietDonHang'
END;

IF COL_LENGTH(@tblCT, 'soLuongThucNhan') IS NULL
BEGIN
    DECLARE @sqlAddColCT NVARCHAR(MAX) = N'ALTER TABLE ' + @tblCT + N' ADD soLuongThucNhan INT NULL;';
    EXEC sp_executesql @sqlAddColCT;
    PRINT N'Đã thêm cột soLuongThucNhan vào ' + @tblCT;

    -- Khởi tạo dữ liệu cũ: soLuongThucNhan = soLuong
    DECLARE @sqlUpdateCT NVARCHAR(MAX) = N'UPDATE ' + @tblCT + N' SET soLuongThucNhan = soLuong WHERE soLuongThucNhan IS NULL;';
    EXEC sp_executesql @sqlUpdateCT;
    PRINT N'Đã cập nhật soLuongThucNhan = soLuong cho các bản ghi cũ.';
END
GO

-- 2. Bổ sung các cột vào DonNhapHang (hoặc DonHang nếu chưa đổi tên)
DECLARE @tblDH NVARCHAR(128) = CASE 
    WHEN OBJECT_ID('dbo.DonNhapHang', 'U') IS NOT NULL THEN 'dbo.DonNhapHang'
    ELSE 'dbo.DonHang'
END;

IF COL_LENGTH(@tblDH, 'tongTienThucNhan') IS NULL
BEGIN
    DECLARE @sqlAddTongTienTN NVARCHAR(MAX) = N'ALTER TABLE ' + @tblDH + N' ADD tongTienThucNhan DECIMAL(18,2) NULL;';
    EXEC sp_executesql @sqlAddTongTienTN;
    PRINT N'Đã thêm cột tongTienThucNhan vào ' + @tblDH;
END

IF COL_LENGTH(@tblDH, 'maNhanVienNhan') IS NULL
BEGIN
    DECLARE @sqlAddNVNhan NVARCHAR(MAX) = N'ALTER TABLE ' + @tblDH + N' ADD maNhanVienNhan VARCHAR(20) NULL;';
    EXEC sp_executesql @sqlAddNVNhan;
    PRINT N'Đã thêm cột maNhanVienNhan vào ' + @tblDH;
END

IF COL_LENGTH(@tblDH, 'ngayGioNhan') IS NULL
BEGIN
    DECLARE @sqlAddNgayGioNhan NVARCHAR(MAX) = N'ALTER TABLE ' + @tblDH + N' ADD ngayGioNhan DATETIME2 NULL;';
    EXEC sp_executesql @sqlAddNgayGioNhan;
    PRINT N'Đã thêm cột ngayGioNhan vào ' + @tblDH;
END

IF COL_LENGTH(@tblDH, 'maNhanVienHuy') IS NULL
BEGIN
    DECLARE @sqlAddNVHuy NVARCHAR(MAX) = N'ALTER TABLE ' + @tblDH + N' ADD maNhanVienHuy VARCHAR(20) NULL;';
    EXEC sp_executesql @sqlAddNVHuy;
    PRINT N'Đã thêm cột maNhanVienHuy vào ' + @tblDH;
END

IF COL_LENGTH(@tblDH, 'lyDoHuy') IS NULL
BEGIN
    DECLARE @sqlAddLyDoHuy NVARCHAR(MAX) = N'ALTER TABLE ' + @tblDH + N' ADD lyDoHuy NVARCHAR(255) NULL;';
    EXEC sp_executesql @sqlAddLyDoHuy;
    PRINT N'Đã thêm cột lyDoHuy vào ' + @tblDH;
END
GO

