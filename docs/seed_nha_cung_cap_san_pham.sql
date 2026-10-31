/*
 * Idempotent catalog seed for the five existing demo suppliers.
 * Keeps supplier records and historical transaction rows. Legacy products
 * with transaction history are soft-disabled; only products without history
 * are reassigned to a matching supplier. Missing demo product IDs are inserted.
 * Run this file on an existing database. Do not rerun the full bootstrap SQL,
 * which drops and recreates the tables.
 */
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF (SELECT COUNT(*) FROM NhaCungCap
        WHERE maNhaCungCap IN (
            'NCC-20261005-000001', 'NCC-20261005-000002', 'NCC-20261005-000003',
            'NCC-20261005-000004', 'NCC-20261005-000005'
        )) <> 5
    BEGIN
        THROW 51000, 'Không tìm thấy đủ 5 nhà cung cấp mẫu; không thay đổi dữ liệu.', 1;
    END;

    -- Giữ các sản phẩm có lịch sử trên đúng NCC cũ, chỉ ngừng kinh doanh.
    UPDATE SanPham
    SET trangThai = 0
    WHERE maSanPham IN ('SP-20261005-000003', 'SP-20261005-000004', 'SP-20261005-000005')
      AND maNhaCungCap = 'NCC-20261005-000002';

    -- Chuẩn hóa các mã demo chưa có giao dịch theo đúng danh mục.
    UPDATE SanPham
    SET maNhaCungCap = 'NCC-20261005-000002', tenSanPham = N'Hạt chó Royal vị gà 1kg',
        donViTinh = N'Túi', giaBan = 180000
    WHERE maSanPham = 'SP-20261005-000006' AND maNhaCungCap = 'NCC-20261005-000001';

    UPDATE SanPham
    SET maNhaCungCap = 'NCC-20261005-000002', tenSanPham = N'Snack thưởng dinh dưỡng cho chó'
    WHERE maSanPham = 'SP-20261005-000007' AND maNhaCungCap = 'NCC-20261005-000001';

    UPDATE SanPham
    SET maNhaCungCap = 'NCC-20261005-000005', tenSanPham = N'Bát ăn inox chống trượt size M'
    WHERE maSanPham = 'SP-20261005-000008' AND maNhaCungCap = 'NCC-20261005-000002';

    UPDATE SanPham
    SET maNhaCungCap = 'NCC-20261005-000005', tenSanPham = N'Bóng cao su đồ chơi cho chó'
    WHERE maSanPham = 'SP-20261005-000009' AND maNhaCungCap = 'NCC-20261005-000002';

    UPDATE SanPham
    SET maNhaCungCap = 'NCC-20261005-000005', tenSanPham = N'Lược chải lông chó mèo hai mặt'
    WHERE maSanPham = 'SP-20261005-000010' AND maNhaCungCap = 'NCC-20261005-000002';

    DECLARE @SanPhamDemo TABLE (
        maSanPham VARCHAR(20), maNhaCungCap VARCHAR(20), tenSanPham NVARCHAR(150),
        maVach VARCHAR(50), giaBan DECIMAL(18,2), soLuongTon INT,
        nguongTonToiThieu INT, donViTinh NVARCHAR(30), trangThai BIT
    );

    INSERT INTO @SanPhamDemo VALUES
    ('SP-20261031-000001', 'NCC-20261005-000004', N'Sữa tắm Bio-Groom Natural Oatmeal', '89320261031001', 290000, 24, 6, N'Chai', 1),
    ('SP-20261031-000002', 'NCC-20261005-000004', N'Sữa tắm Bio-Groom Fluffy Puppy', '89320261031002', 310000, 17, 5, N'Chai', 1),
    ('SP-20261031-000003', 'NCC-20261005-000004', N'Dầu xả lông Bio-Groom Silk', '89320261031003', 275000, 28, 7, N'Chai', 1),
    ('SP-20261031-000004', 'NCC-20261005-000004', N'Xịt dưỡng lông Bio-Groom Coat Polish', '89320261031004', 245000, 32, 8, N'Chai', 1),
    ('SP-20261031-000005', 'NCC-20261005-000004', N'Dung dịch vệ sinh tai thú cưng Bio-Groom', '89320261031005', 150000, 21, 5, N'Chai', 1),
    ('SP-20261031-000006', 'NCC-20261005-000004', N'Dung dịch vệ sinh mắt thú cưng Bio-Groom', '89320261031006', 145000, 14, 4, N'Chai', 1),
    ('SP-20261031-000007', 'NCC-20261005-000004', N'Phấn tắm khô Bio-Groom cho chó mèo', '89320261031007', 165000, 12, 4, N'Hộp', 1),
    ('SP-20261031-000008', 'NCC-20261005-000004', N'Sữa tắm khử mùi Bio-Groom cho chó mèo', '89320261031008', 255000, 38, 8, N'Chai', 1),
    ('SP-20261031-000009', 'NCC-20261005-000001', N'Hạt mèo MiuMeal vị gà 1kg', '89320261031009', 180000, 36, 8, N'Túi', 1),
    ('SP-20261031-000010', 'NCC-20261005-000001', N'Hạt mèo MiuMeal cho mèo con 1kg', '89320261031010', 195000, 21, 7, N'Túi', 1),
    ('SP-20261031-000011', 'NCC-20261005-000001', N'Pate mèo MiuMeal vị gà 85g', '89320261031011', 20000, 32, 10, N'Gói', 1),
    ('SP-20261031-000012', 'NCC-20261005-000001', N'Súp thưởng mèo MiuMeal vị cá hồi', '89320261031012', 12000, 47, 12, N'Gói', 1),
    ('SP-20261031-000013', 'NCC-20261005-000001', N'Snack thưởng mèo MiuMeal vị cá ngừ', '89320261031013', 65000, 25, 8, N'Túi', 1),
    ('SP-20261031-000014', 'NCC-20261005-000001', N'Sữa dinh dưỡng MiuMeal cho mèo 180ml', '89320261031014', 85000, 16, 5, N'Chai', 1),
    ('SP-20261031-000015', 'NCC-20261005-000001', N'Cát vệ sinh mèo MiuMeal hương cà phê 5L', '89320261031015', 125000, 30, 8, N'Túi', 1),
    ('SP-20261031-000016', 'NCC-20261005-000001', N'Cát vệ sinh mèo MiuMeal đậu nành 6L', '89320261031016', 155000, 18, 6, N'Túi', 1),
    ('SP-20261031-000017', 'NCC-20261005-000005', N'Chuột bông đồ chơi cho mèo', '89320261031017', 35000, 29, 7, N'Cái', 1),
    ('SP-20261031-000018', 'NCC-20261005-000005', N'Cần câu lông vũ cho mèo', '89320261031018', 42000, 23, 6, N'Cái', 1),
    ('SP-20261031-000019', 'NCC-20261005-000005', N'Vòng cổ chó có chuông', '89320261031019', 65000, 34, 8, N'Cái', 1),
    ('SP-20261031-000020', 'NCC-20261005-000005', N'Dây dắt chó loại 1.5m', '89320261031020', 85000, 20, 5, N'Cái', 1),
    ('SP-20261031-000021', 'NCC-20261005-000005', N'Bình nước di động cho chó', '89320261031021', 120000, 17, 5, N'Cái', 1),
    ('SP-20261031-000022', 'NCC-20261005-000005', N'Đệm nằm cho chó mèo', '89320261031022', 280000, 11, 4, N'Cái', 1),
    ('SP-20261031-000023', 'NCC-20261005-000005', N'Nhà ngủ bằng vải cho mèo', '89320261031023', 240000, 13, 4, N'Cái', 1),
    ('SP-20261031-000024', 'NCC-20261005-000005', N'Balo vận chuyển thú cưng', '89320261031024', 450000, 16, 5, N'Cái', 1),
    ('SP-20261031-000025', 'NCC-20261005-000005', N'Kềm cắt móng thú cưng', '89320261031025', 85000, 26, 6, N'Cái', 1),
    ('SP-20261031-000026', 'NCC-20261005-000003', N'Hạt chó trưởng thành vị bò 3kg', '89320261031026', 260000, 27, 7, N'Túi', 1),
    ('SP-20261031-000027', 'NCC-20261005-000003', N'Hạt chó con vị gà 1.5kg', '89320261031027', 185000, 16, 5, N'Túi', 1),
    ('SP-20261031-000028', 'NCC-20261005-000003', N'Pate chó vị bò 400g', '89320261031028', 45000, 22, 6, N'Lon', 1),
    ('SP-20261031-000029', 'NCC-20261005-000003', N'Xương gặm sạch răng cho chó', '89320261031029', 60000, 32, 8, N'Gói', 1),
    ('SP-20261031-000030', 'NCC-20261005-000003', N'Snack thưởng cho chó vị gà', '89320261031030', 70000, 25, 7, N'Túi', 1),
    ('SP-20261031-000031', 'NCC-20261005-000003', N'Tã lót vệ sinh thú cưng 45x60cm', '89320261031031', 95000, 30, 8, N'Gói', 1),
    ('SP-20261031-000032', 'NCC-20261005-000003', N'Khay vệ sinh cho mèo', '89320261031032', 175000, 18, 5, N'Cái', 1),
    ('SP-20261031-000033', 'NCC-20261005-000003', N'Xẻng xúc cát vệ sinh mèo', '89320261031033', 25000, 44, 10, N'Cái', 1),
    ('SP-20261031-000034', 'NCC-20261005-000003', N'Lồng vận chuyển thú cưng size M', '89320261031034', 320000, 12, 4, N'Cái', 1),
    ('SP-20261031-000035', 'NCC-20261005-000003', N'Chuồng sắt cho chó size L', '89320261031035', 850000, 10, 3, N'Cái', 1),
    ('SP-20261031-000036', 'NCC-20261005-000003', N'Túi đựng phân chó dạng cuộn', '89320261031036', 35000, 40, 10, N'Cuộn', 1),
    ('SP-20261031-000037', 'NCC-20261005-000003', N'Bình xịt khử mùi chuồng nuôi', '89320261031037', 130000, 19, 5, N'Chai', 1),
    ('SP-20261031-000038', 'NCC-20261005-000002', N'Hạt chó Royal dành cho chó con 2kg', '89320261031038', 330000, 15, 5, N'Túi', 1),
    ('SP-20261031-000039', 'NCC-20261005-000002', N'Hạt chó Royal dành cho chó trưởng thành 3kg', '89320261031039', 420000, 28, 7, N'Túi', 1),
    ('SP-20261031-000040', 'NCC-20261005-000002', N'Hạt mèo Royal vị cá hồi 1kg', '89320261031040', 225000, 24, 6, N'Túi', 1),
    ('SP-20261031-000041', 'NCC-20261005-000002', N'Hạt mèo Royal dành cho mèo con 1kg', '89320261031041', 205000, 17, 5, N'Túi', 1),
    ('SP-20261031-000042', 'NCC-20261005-000002', N'Pate chó Royal vị bò 400g', '89320261031042', 55000, 31, 8, N'Lon', 1),
    ('SP-20261031-000043', 'NCC-20261005-000002', N'Pate mèo Royal vị cá ngừ 85g', '89320261031043', 21000, 45, 10, N'Gói', 1),
    ('SP-20261031-000044', 'NCC-20261005-000002', N'Sữa bột dinh dưỡng Royal cho chó con', '89320261031044', 165000, 14, 4, N'Hộp', 1),
    ('SP-20261031-000045', 'NCC-20261005-000002', N'Sữa bột dinh dưỡng Royal cho mèo con', '89320261031045', 145000, 18, 5, N'Hộp', 1);

    INSERT INTO SanPham (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, soLuongTon, nguongTonToiThieu, donViTinh, trangThai)
    SELECT mau.maSanPham, mau.maNhaCungCap, mau.tenSanPham, mau.maVach, mau.giaBan,
           mau.soLuongTon, mau.nguongTonToiThieu, mau.donViTinh, mau.trangThai
    FROM @SanPhamDemo mau
    WHERE NOT EXISTS (
        SELECT 1 FROM SanPham sp
        WHERE sp.maSanPham = mau.maSanPham OR sp.maVach = mau.maVach
    );

    COMMIT TRANSACTION;

    SELECT ncc.maNhaCungCap, ncc.tenNhaCungCap, COUNT(sp.maSanPham) AS soSanPhamDangKinhDoanh
    FROM NhaCungCap ncc
    LEFT JOIN SanPham sp ON sp.maNhaCungCap = ncc.maNhaCungCap AND sp.trangThai = 1
    WHERE ncc.maNhaCungCap IN (
        'NCC-20261005-000001', 'NCC-20261005-000002', 'NCC-20261005-000003',
        'NCC-20261005-000004', 'NCC-20261005-000005'
    )
    GROUP BY ncc.maNhaCungCap, ncc.tenNhaCungCap
    ORDER BY ncc.maNhaCungCap;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
