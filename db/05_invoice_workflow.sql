USE QLCH_HappyTails;
GO

-- Mở rộng hóa đơn mà không xóa dữ liệu hoặc tạo lại các bảng hiện có.
IF COL_LENGTH('dbo.HoaDon', 'maThuCung') IS NULL
    ALTER TABLE dbo.HoaDon ADD maThuCung VARCHAR(20) NULL;
IF COL_LENGTH('dbo.HoaDon', 'maNhanVienThu') IS NULL
    ALTER TABLE dbo.HoaDon ADD maNhanVienThu VARCHAR(20) NULL;
IF COL_LENGTH('dbo.HoaDon', 'giamGia') IS NULL
    ALTER TABLE dbo.HoaDon ADD giamGia DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_HoaDon_GiamGia DEFAULT (0) WITH VALUES;
IF COL_LENGTH('dbo.HoaDon', 'giamTheoKhuyenMai') IS NULL
    ALTER TABLE dbo.HoaDon ADD giamTheoKhuyenMai DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_HoaDon_GiamTheoKhuyenMai DEFAULT (0) WITH VALUES;
IF COL_LENGTH('dbo.HoaDon', 'thue') IS NULL
    ALTER TABLE dbo.HoaDon ADD thue DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_HoaDon_Thue DEFAULT (0) WITH VALUES;
IF COL_LENGTH('dbo.HoaDon', 'ghiChu') IS NULL
    ALTER TABLE dbo.HoaDon ADD ghiChu NVARCHAR(500) NULL;
IF COL_LENGTH('dbo.HoaDon', 'ngayThanhToan') IS NULL
    ALTER TABLE dbo.HoaDon ADD ngayThanhToan DATETIME2 NULL;
IF COL_LENGTH('dbo.HoaDon', 'ngayHuy') IS NULL
    ALTER TABLE dbo.HoaDon ADD ngayHuy DATETIME2 NULL;
IF COL_LENGTH('dbo.HoaDon', 'nguoiHuy') IS NULL
    ALTER TABLE dbo.HoaDon ADD nguoiHuy VARCHAR(20) NULL;
IF COL_LENGTH('dbo.HoaDon', 'lyDoHuy') IS NULL
    ALTER TABLE dbo.HoaDon ADD lyDoHuy NVARCHAR(500) NULL;
GO

-- Tạo bảng chi tiết nếu database cũ chưa có; bảng đã có sẽ được giữ nguyên.
IF OBJECT_ID('dbo.ChiTietHoaDon_SanPham', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiTietHoaDon_SanPham (
        maHoaDon VARCHAR(20) NOT NULL,
        maSanPham VARCHAR(20) NOT NULL,
        soLuong INT NOT NULL CHECK (soLuong > 0),
        donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
        tenHienThi NVARCHAR(200) NULL,
        khoa BIT NOT NULL CONSTRAINT DF_CTHD_SanPham_Khoa DEFAULT (0),
        CONSTRAINT PK_ChiTietHoaDon_SanPham PRIMARY KEY (maHoaDon, maSanPham),
        CONSTRAINT FK_CTHDSanPham_HoaDon FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon),
        CONSTRAINT FK_CTHDSanPham_SanPham FOREIGN KEY (maSanPham) REFERENCES dbo.SanPham(maSanPham)
    );
END;

IF OBJECT_ID('dbo.ChiTietHoaDon_DichVu', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiTietHoaDon_DichVu (
        maHoaDon VARCHAR(20) NOT NULL,
        maDichVu VARCHAR(20) NOT NULL,
        soLuong INT NOT NULL DEFAULT (1) CHECK (soLuong = 1),
        donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
        tenHienThi NVARCHAR(200) NULL,
        khoa BIT NOT NULL CONSTRAINT DF_CTHD_DichVu_Khoa DEFAULT (0),
        CONSTRAINT PK_ChiTietHoaDon_DichVu PRIMARY KEY (maHoaDon, maDichVu),
        CONSTRAINT FK_CTHDDichVu_HoaDon FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon),
        CONSTRAINT FK_CTHDDichVu_DichVu FOREIGN KEY (maDichVu) REFERENCES dbo.DichVuChamSoc(maDichVu)
    );
END;
GO

-- Lưu tên tại thời điểm bán và đánh dấu dòng dịch vụ lấy từ lịch hẹn.
IF COL_LENGTH('dbo.ChiTietHoaDon_SanPham', 'tenHienThi') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon_SanPham ADD tenHienThi NVARCHAR(200) NULL;
IF COL_LENGTH('dbo.ChiTietHoaDon_SanPham', 'khoa') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon_SanPham ADD khoa BIT NOT NULL
        CONSTRAINT DF_CTHD_SanPham_Khoa DEFAULT (0) WITH VALUES;
IF COL_LENGTH('dbo.ChiTietHoaDon_DichVu', 'tenHienThi') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon_DichVu ADD tenHienThi NVARCHAR(200) NULL;
IF COL_LENGTH('dbo.ChiTietHoaDon_DichVu', 'khoa') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon_DichVu ADD khoa BIT NOT NULL
        CONSTRAINT DF_CTHD_DichVu_Khoa DEFAULT (0) WITH VALUES;
GO

-- Ràng buộc thú cưng phải thuộc đúng khách đã chọn trên hóa đơn.
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_HoaDon_ThuCung_KhachHang')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT FK_HoaDon_ThuCung_KhachHang
        FOREIGN KEY (maThuCung, maKhachHang)
        REFERENCES dbo.ThuCung (maThuCung, maKhachHang);
GO

-- Thay check cũ bằng danh sách trạng thái lịch hẹn được nghiệp vụ sử dụng.
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_LichHen_TrangThai_Invoice')
BEGIN
        DECLARE @lichHenStatusCheck SYSNAME;
        SELECT TOP (1) @lichHenStatusCheck = cc.name
        FROM sys.check_constraints cc
        JOIN sys.columns col ON col.object_id = cc.parent_object_id
        WHERE cc.parent_object_id = OBJECT_ID(N'dbo.LichHen')
            AND col.name = N'trangThai'
            AND cc.definition LIKE N'%trangThai%';

        IF @lichHenStatusCheck IS NOT NULL
                EXEC (N'ALTER TABLE dbo.LichHen DROP CONSTRAINT ' + QUOTENAME(@lichHenStatusCheck));

    ALTER TABLE dbo.LichHen ADD CONSTRAINT CK_LichHen_TrangThai_Invoice
        CHECK (trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã đến', N'Đang thực hiện',
                            N'Hoàn thành', N'Đã thanh toán', N'Đã hủy'));
END;
GO