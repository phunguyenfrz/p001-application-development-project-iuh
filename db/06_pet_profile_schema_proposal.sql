-- Proposal only: review and approve before running against SQL Server.
-- Existing ThuCung.diUng is retained; the columns below add the remaining
-- pet profile attributes and soft-deactivation support used by the application.
IF COL_LENGTH(N'dbo.ThuCung', N'dacDiemNhanDang') IS NULL
BEGIN
    ALTER TABLE dbo.ThuCung
        ADD dacDiemNhanDang nvarchar(255) NULL;
END;
GO

IF COL_LENGTH(N'dbo.ThuCung', N'benhNen') IS NULL
BEGIN
    ALTER TABLE dbo.ThuCung
        ADD benhNen nvarchar(255) NULL;
END;
GO

IF COL_LENGTH(N'dbo.ThuCung', N'tinhCach') IS NULL
BEGIN
    ALTER TABLE dbo.ThuCung
        ADD tinhCach nvarchar(255) NULL;
END;
GO

IF COL_LENGTH(N'dbo.ThuCung', N'trangThai') IS NULL
BEGIN
    ALTER TABLE dbo.ThuCung
        ADD trangThai bit NOT NULL
            CONSTRAINT DF_ThuCung_trangThai DEFAULT (1) WITH VALUES;
END;
GO
