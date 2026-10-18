USE QLCH_HappyTails;
GO

-- Bổ sung trạng thái theo dõi của hồ sơ thú cưng.
-- 1: Đang theo dõi; 0: Ngừng theo dõi.
-- Giữ nguyên dữ liệu và lịch sử chăm sóc hiện có.
IF COL_LENGTH('dbo.ThuCung', 'trangThai') IS NULL
BEGIN
    ALTER TABLE dbo.ThuCung
    ADD trangThai BIT NOT NULL
        CONSTRAINT DF_ThuCung_TrangThai DEFAULT (1) WITH VALUES;
END;
GO