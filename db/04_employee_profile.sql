USE QLCH_HappyTails;
GO
-- Bổ sung các trường trong form; không tự điền dữ liệu giả cho nhân viên cũ.
IF COL_LENGTH('dbo.NhanVien', 'email') IS NULL
    ALTER TABLE dbo.NhanVien ADD email VARCHAR(100) NULL;
IF COL_LENGTH('dbo.NhanVien', 'diaChi') IS NULL
    ALTER TABLE dbo.NhanVien ADD diaChi NVARCHAR(255) NULL;
IF COL_LENGTH('dbo.NhanVien', 'ngayBatDau') IS NULL
    ALTER TABLE dbo.NhanVien ADD ngayBatDau DATE NULL;
IF COL_LENGTH('dbo.NhanVien', 'anh') IS NULL
    ALTER TABLE dbo.NhanVien ADD anh VARBINARY(MAX) NULL;
GO
