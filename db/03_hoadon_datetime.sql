-- DashboardDao date comparisons updated for datetime2:
-- queryRevenueToday and queryInvoiceCountToday use CAST(ngayLap AS date)
-- queryWeeklyRevenue joins on CAST(hd.ngayLap AS date).
ALTER TABLE dbo.HoaDon
    ALTER COLUMN ngayLap datetime2 NOT NULL;
GO
