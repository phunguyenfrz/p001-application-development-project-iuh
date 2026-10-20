IF OBJECT_ID(N'dbo.NhatKyThaoTac', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.NhatKyThaoTac
    (
        maNhatKy varchar(36) NOT NULL
            CONSTRAINT DF_NhatKyThaoTac_maNhatKy DEFAULT (CONVERT(varchar(36), NEWID())),
        maTaiKhoan varchar(20) NOT NULL,
        hanhDong nvarchar(100) NOT NULL,
        bangDoiTuong nvarchar(128) NOT NULL,
        maDoiTuong varchar(100) NULL,
        thoiGian datetime2 NOT NULL
            CONSTRAINT DF_NhatKyThaoTac_thoiGian DEFAULT (SYSDATETIME()),
        ghiChu nvarchar(1000) NULL,
        CONSTRAINT PK_NhatKyThaoTac PRIMARY KEY (maNhatKy),
        CONSTRAINT FK_NhatKyThaoTac_TaiKhoan FOREIGN KEY (maTaiKhoan)
            REFERENCES dbo.TaiKhoan (maTaiKhoan)
    );

    CREATE INDEX IX_NhatKyThaoTac_TaiKhoan_ThoiGian
        ON dbo.NhatKyThaoTac (maTaiKhoan, thoiGian DESC);
END;
GO
