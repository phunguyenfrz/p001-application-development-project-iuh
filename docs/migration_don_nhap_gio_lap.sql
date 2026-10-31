/*
 * Add the time component to existing import orders.
 * Existing records have no saved time, so they are initialized to 00:00:00.
 * New app-created records save the time selected in LapDonNhapView.
 */
IF COL_LENGTH('dbo.DonHang', 'gioLap') IS NULL
BEGIN
    ALTER TABLE dbo.DonHang ADD gioLap TIME(0) NULL;

    UPDATE dbo.DonHang
    SET gioLap = '00:00:00'
    WHERE gioLap IS NULL;

    ALTER TABLE dbo.DonHang ALTER COLUMN gioLap TIME(0) NOT NULL;

    ALTER TABLE dbo.DonHang
        ADD CONSTRAINT DF_DonHang_gioLap DEFAULT ('00:00:00') FOR gioLap;
END;
GO
