USE QLCH_HappyTails;
GO

-- Bổ sung trạng thái khách hàng để hỗ trợ xóa mềm.
-- 1: Đang hoạt động; 0: Ngừng hoạt động.
IF COL_LENGTH('dbo.KhachHang', 'trangThai') IS NULL
BEGIN
    ALTER TABLE dbo.KhachHang
    ADD trangThai BIT NOT NULL
        CONSTRAINT DF_KhachHang_TrangThai DEFAULT (1) WITH VALUES;
END;
GO