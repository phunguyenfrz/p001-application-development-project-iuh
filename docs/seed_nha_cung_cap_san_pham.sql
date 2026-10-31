/*
 * Seed demo an toàn cho cơ sở dữ liệu QLCH_HappyTails đã khởi tạo.
 * Chạy file này thay vì chạy lại QLCH_HappyTails_Database_Full.sql,
 * vì file khởi tạo đầy đủ sẽ xóa và tạo lại các bảng.
 * Chỉ đồng bộ tên hai NCC demo theo bộ dữ liệu MiuMeal/PetCare,
 * chuẩn hóa tên các sản phẩm demo đã có và thêm các mã sản phẩm còn thiếu.
 */
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNhaCungCap = 'NCC-20261005-000001')
    BEGIN
        INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi)
        VALUES ('NCC-20261005-000001', N'Công ty Thức ăn thú cưng MiuMeal', '0283812345', N'KCN Tân Bình, TP.HCM');
    END
    ELSE
    BEGIN
        UPDATE NhaCungCap
        SET tenNhaCungCap = N'Công ty Thức ăn thú cưng MiuMeal'
        WHERE maNhaCungCap = 'NCC-20261005-000001';
    END

    IF NOT EXISTS (SELECT 1 FROM NhaCungCap WHERE maNhaCungCap = 'NCC-20261005-000002')
    BEGIN
        INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi)
        VALUES ('NCC-20261005-000002', N'Nhà phân phối phụ kiện PetCare', '0283898765', N'Quận 10, TP.HCM');
    END;
    ELSE
    BEGIN
        UPDATE NhaCungCap
        SET tenNhaCungCap = N'Nhà phân phối phụ kiện PetCare'
        WHERE maNhaCungCap = 'NCC-20261005-000002';
    END;

    UPDATE SanPham
    SET tenSanPham = N'Hạt mèo MiuMeal vị cá hồi 2kg'
    WHERE maSanPham = 'SP-20261005-000001' AND maNhaCungCap = 'NCC-20261005-000001';
    UPDATE SanPham
    SET tenSanPham = N'Pate mèo MiuMeal cá ngừ 85g'
    WHERE maSanPham = 'SP-20261005-000002' AND maNhaCungCap = 'NCC-20261005-000001';
    UPDATE SanPham
    SET tenSanPham = N'Cát vệ sinh PetCare gỗ thông 6L'
    WHERE maSanPham = 'SP-20261005-000003' AND maNhaCungCap = 'NCC-20261005-000002';
    UPDATE SanPham
    SET tenSanPham = N'Dây dắt PetCare kèm yếm size M'
    WHERE maSanPham = 'SP-20261005-000004' AND maNhaCungCap = 'NCC-20261005-000002';
    UPDATE SanPham
    SET tenSanPham = N'Sữa tắm PetCare dịu nhẹ 530ml'
    WHERE maSanPham = 'SP-20261005-000005' AND maNhaCungCap = 'NCC-20261005-000002';

    DECLARE @SanPhamDemo TABLE (
        maSanPham VARCHAR(20), maNhaCungCap VARCHAR(20), tenSanPham NVARCHAR(150),
        maVach VARCHAR(50), giaBan DECIMAL(18,2), soLuongTon INT,
        nguongTonToiThieu INT, donViTinh NVARCHAR(30), trangThai BIT
    );

    INSERT INTO @SanPhamDemo VALUES
    ('SP-20261005-000006', 'NCC-20261005-000001', N'Hạt chó MiuMeal vị gà 3kg', '893123456706', 480000, 22, 6, N'Gói', 1),
    ('SP-20261005-000007', 'NCC-20261005-000001', N'Snack thưởng MiuMeal gà sấy 100g', '893123456707', 75000, 40, 10, N'Gói', 1),
    ('SP-20261005-000008', 'NCC-20261005-000002', N'Bát ăn inox PetCare chống trượt M', '893123456708', 95000, 18, 5, N'Cái', 1),
    ('SP-20261005-000009', 'NCC-20261005-000002', N'Bóng cao su PetCare cho chó', '893123456709', 55000, 32, 8, N'Cái', 1),
    ('SP-20261005-000010', 'NCC-20261005-000002', N'Lược chải lông PetCare hai mặt', '893123456710', 78000, 14, 4, N'Cái', 1);

    INSERT INTO SanPham (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, soLuongTon, nguongTonToiThieu, donViTinh, trangThai)
    SELECT mau.maSanPham, mau.maNhaCungCap, mau.tenSanPham, mau.maVach, mau.giaBan,
           mau.soLuongTon, mau.nguongTonToiThieu, mau.donViTinh, mau.trangThai
    FROM @SanPhamDemo mau
    WHERE NOT EXISTS (
        SELECT 1 FROM SanPham sp
        WHERE sp.maSanPham = mau.maSanPham OR sp.maVach = mau.maVach
    );

    COMMIT TRANSACTION;

    SELECT ncc.maNhaCungCap, ncc.tenNhaCungCap,
           COUNT(sp.maSanPham) AS soSanPham
    FROM NhaCungCap ncc
    LEFT JOIN SanPham sp ON sp.maNhaCungCap = ncc.maNhaCungCap
    WHERE ncc.maNhaCungCap IN ('NCC-20261005-000001', 'NCC-20261005-000002')
    GROUP BY ncc.maNhaCungCap, ncc.tenNhaCungCap
    ORDER BY ncc.maNhaCungCap;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
