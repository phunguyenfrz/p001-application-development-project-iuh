USE QLCH_HappyTails;
GO

-- Mở rộng hóa đơn mà không xóa dữ liệu hoặc tạo lại các bảng hiện có.
IF COL_LENGTH('dbo.HoaDon', 'maThuCung') IS NULL
BEGIN
    DECLARE @petIdType NVARCHAR(256);
    SELECT @petIdType = CASE
        WHEN t.name IN ('nvarchar', 'nchar')
            THEN t.name + '(' + CONVERT(VARCHAR(10), c.max_length / 2) + ')'
        ELSE t.name + '(' + CONVERT(VARCHAR(10), c.max_length) + ')'
    END + CASE WHEN c.collation_name IS NULL THEN '' ELSE ' COLLATE ' + c.collation_name END
    FROM sys.columns c
    JOIN sys.types t ON t.user_type_id = c.user_type_id
    WHERE c.object_id = OBJECT_ID('dbo.ThuCung') AND c.name = 'maThuCung';

    IF @petIdType IS NULL
        ;THROW 50001, 'Không tìm thấy dbo.ThuCung.maThuCung để cập nhật hóa đơn.', 1;

    DECLARE @addPetIdSql NVARCHAR(4000) =
        N'ALTER TABLE dbo.HoaDon ADD maThuCung ' + @petIdType + N' NULL;';
    EXEC sys.sp_executesql @addPetIdSql;
END;
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
DECLARE @invoiceIdType NVARCHAR(256);
DECLARE @productIdType NVARCHAR(256);
DECLARE @serviceIdType NVARCHAR(256);

SELECT @invoiceIdType = CASE
    WHEN t.name IN ('nvarchar', 'nchar')
        THEN t.name + '(' + CONVERT(VARCHAR(10), c.max_length / 2) + ')'
    ELSE t.name + '(' + CONVERT(VARCHAR(10), c.max_length) + ')'
END + CASE WHEN c.collation_name IS NULL THEN '' ELSE ' COLLATE ' + c.collation_name END
FROM sys.columns c
JOIN sys.types t ON t.user_type_id = c.user_type_id
WHERE c.object_id = OBJECT_ID('dbo.HoaDon') AND c.name = 'maHoaDon';

SELECT @productIdType = CASE
    WHEN t.name IN ('nvarchar', 'nchar')
        THEN t.name + '(' + CONVERT(VARCHAR(10), c.max_length / 2) + ')'
    ELSE t.name + '(' + CONVERT(VARCHAR(10), c.max_length) + ')'
END + CASE WHEN c.collation_name IS NULL THEN '' ELSE ' COLLATE ' + c.collation_name END
FROM sys.columns c
JOIN sys.types t ON t.user_type_id = c.user_type_id
WHERE c.object_id = OBJECT_ID('dbo.SanPham') AND c.name = 'maSanPham';

SELECT @serviceIdType = CASE
    WHEN t.name IN ('nvarchar', 'nchar')
        THEN t.name + '(' + CONVERT(VARCHAR(10), c.max_length / 2) + ')'
    ELSE t.name + '(' + CONVERT(VARCHAR(10), c.max_length) + ')'
END + CASE WHEN c.collation_name IS NULL THEN '' ELSE ' COLLATE ' + c.collation_name END
FROM sys.columns c
JOIN sys.types t ON t.user_type_id = c.user_type_id
WHERE c.object_id = OBJECT_ID('dbo.DichVuChamSoc') AND c.name = 'maDichVu';

IF @invoiceIdType IS NULL OR @productIdType IS NULL OR @serviceIdType IS NULL
    ;THROW 50002, 'Không tìm thấy cột mã hóa đơn, sản phẩm hoặc dịch vụ trong database.', 1;

IF OBJECT_ID('dbo.ChiTietHoaDon_SanPham', 'U') IS NULL
BEGIN
    DECLARE @createProductDetails NVARCHAR(MAX) =
        N'CREATE TABLE dbo.ChiTietHoaDon_SanPham (' +
        N'maHoaDon ' + @invoiceIdType + N' NOT NULL, ' +
        N'maSanPham ' + @productIdType + N' NOT NULL, ' +
        N'soLuong INT NOT NULL CHECK (soLuong > 0), ' +
        N'donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0), ' +
        N'tenHienThi NVARCHAR(200) NULL, ' +
        N'khoa BIT NOT NULL CONSTRAINT DF_CTHD_SanPham_Khoa DEFAULT (0), ' +
        N'CONSTRAINT PK_ChiTietHoaDon_SanPham PRIMARY KEY (maHoaDon, maSanPham), ' +
        N'CONSTRAINT FK_CTHDSanPham_HoaDon FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon), ' +
        N'CONSTRAINT FK_CTHDSanPham_SanPham FOREIGN KEY (maSanPham) REFERENCES dbo.SanPham(maSanPham));';
    EXEC sys.sp_executesql @createProductDetails;
END;

IF OBJECT_ID('dbo.ChiTietHoaDon_DichVu', 'U') IS NULL
BEGIN
    DECLARE @createServiceDetails NVARCHAR(MAX) =
        N'CREATE TABLE dbo.ChiTietHoaDon_DichVu (' +
        N'maHoaDon ' + @invoiceIdType + N' NOT NULL, ' +
        N'maDichVu ' + @serviceIdType + N' NOT NULL, ' +
        N'soLuong INT NOT NULL DEFAULT (1) CHECK (soLuong = 1), ' +
        N'donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0), ' +
        N'tenHienThi NVARCHAR(200) NULL, ' +
        N'khoa BIT NOT NULL CONSTRAINT DF_CTHD_DichVu_Khoa DEFAULT (0), ' +
        N'CONSTRAINT PK_ChiTietHoaDon_DichVu PRIMARY KEY (maHoaDon, maDichVu), ' +
        N'CONSTRAINT FK_CTHDDichVu_HoaDon FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon), ' +
        N'CONSTRAINT FK_CTHDDichVu_DichVu FOREIGN KEY (maDichVu) REFERENCES dbo.DichVuChamSoc(maDichVu));';
    EXEC sys.sp_executesql @createServiceDetails;
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
IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_HoaDon_ThuCung_KhachHang')
    ALTER TABLE dbo.HoaDon DROP CONSTRAINT FK_HoaDon_ThuCung_KhachHang;

DECLARE @petIdTypeForAlter NVARCHAR(256);
SELECT @petIdTypeForAlter = CASE
    WHEN t.name IN ('nvarchar', 'nchar')
        THEN t.name + '(' + CONVERT(VARCHAR(10), c.max_length / 2) + ')'
    ELSE t.name + '(' + CONVERT(VARCHAR(10), c.max_length) + ')'
END + CASE WHEN c.collation_name IS NULL THEN '' ELSE ' COLLATE ' + c.collation_name END
FROM sys.columns c
JOIN sys.types t ON t.user_type_id = c.user_type_id
WHERE c.object_id = OBJECT_ID('dbo.ThuCung') AND c.name = 'maThuCung';

DECLARE @alterPetIdSql NVARCHAR(4000) =
    N'ALTER TABLE dbo.HoaDon ALTER COLUMN maThuCung ' + @petIdTypeForAlter + N' NULL;';
EXEC sys.sp_executesql @alterPetIdSql;

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
        BEGIN
            DECLARE @dropStatusCheckSql NVARCHAR(4000) =
                N'ALTER TABLE dbo.LichHen DROP CONSTRAINT ' + QUOTENAME(@lichHenStatusCheck);
            EXEC sys.sp_executesql @dropStatusCheckSql;
        END;

    ALTER TABLE dbo.LichHen ADD CONSTRAINT CK_LichHen_TrangThai_Invoice
        CHECK (trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đã đến', N'Đang thực hiện',
                            N'Hoàn thành', N'Đã thanh toán', N'Đã hủy'));
END;
GO