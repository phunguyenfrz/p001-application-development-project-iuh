USE [QLCH_HappyTails];
GO

DECLARE @dateKey char(8) = CONVERT(char(8), GETDATE(), 112);
DECLARE @customer1 varchar(30) = CONCAT('KH-', @dateKey, '-000001');
DECLARE @customer2 varchar(30) = CONCAT('KH-', @dateKey, '-000002');
DECLARE @customer3 varchar(30) = CONCAT('KH-', @dateKey, '-000003');
DECLARE @pet1 varchar(30) = CONCAT('TC-', @dateKey, '-000001');
DECLARE @pet2 varchar(30) = CONCAT('TC-', @dateKey, '-000002');
DECLARE @pet3 varchar(30) = CONCAT('TC-', @dateKey, '-000003');
DECLARE @employee varchar(30) = CONCAT('NV-', @dateKey, '-000001');
DECLARE @supplier varchar(30) = CONCAT('NCC-', @dateKey, '-000001');
DECLARE @service varchar(30) = CONCAT('DV-', @dateKey, '-000001');
DECLARE @product1 varchar(30) = CONCAT('SP-', @dateKey, '-000001');
DECLARE @product2 varchar(30) = CONCAT('SP-', @dateKey, '-000002');
DECLARE @order varchar(30) = CONCAT('DH-', @dateKey, '-000001');
DECLARE @price1 varchar(30) = CONCAT('BGDV-', @dateKey, '-000001');
DECLARE @price2 varchar(30) = CONCAT('BGDV-', @dateKey, '-000002');
DECLARE @price3 varchar(30) = CONCAT('BGDV-', @dateKey, '-000003');
DECLARE @promotion varchar(30) = CONCAT('KM-', @dateKey, '-000001');
DECLARE @appointment1 varchar(30) = CONCAT('LH-', @dateKey, '-000001');
DECLARE @appointment2 varchar(30) = CONCAT('LH-', @dateKey, '-000002');
DECLARE @appointment3 varchar(30) = CONCAT('LH-', @dateKey, '-000003');
DECLARE @appointment4 varchar(30) = CONCAT('LH-', @dateKey, '-000004');
DECLARE @appointment5 varchar(30) = CONCAT('LH-', @dateKey, '-000005');
DECLARE @employeePhone varchar(10) = CONCAT('09', @dateKey);
DECLARE @customerPhone1 varchar(10) = CONCAT('08', @dateKey);
DECLARE @customerPhone2 varchar(10) = CONCAT('07', @dateKey);
DECLARE @customerPhone3 varchar(10) = CONCAT('06', @dateKey);
DECLARE @supplierPhone varchar(15) = CONCAT('028', @dateKey, '1');
DECLARE @barcode1 varchar(50) = CONCAT('893', @dateKey, '01');
DECLARE @barcode2 varchar(50) = CONCAT('893', @dateKey, '02');

IF NOT EXISTS (SELECT 1 FROM dbo.NhanVien WHERE maNhanVien = @employee)
    INSERT INTO dbo.NhanVien
        (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
    VALUES
        (@employee, N'Nhân viên demo', @employeePhone, N'Quản lý',
         '1995-05-20', 1, N'Đang làm');

IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = @customer1)
    INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu)
    VALUES (@customer1, N'Nguyễn Minh Anh', @customerPhone1, 'minhanh.demo@example.com', 12,
            N'Quận 1, TP. Hồ Chí Minh', N'Khách hàng demo');
IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = @customer2)
    INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu)
    VALUES (@customer2, N'Trần Gia Bảo', @customerPhone2, 'giabao.demo@example.com', 4,
            N'Quận 3, TP. Hồ Chí Minh', N'Khách hàng demo');
IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang WHERE maKhachHang = @customer3)
    INSERT INTO dbo.KhachHang (maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu)
    VALUES (@customer3, N'Lê Thu Hà', @customerPhone3, 'thuha.demo@example.com', 0,
            N'Thành phố Thủ Đức', N'Khách hàng demo');

IF NOT EXISTS (SELECT 1 FROM dbo.ThuCung WHERE maThuCung = @pet1)
    INSERT INTO dbo.ThuCung
        (maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai, canNang,
         ngaySinh, diUng, gioiTinh)
    VALUES (@pet1, @customer1, N'Milo', N'Chó', N'Poodle', 4.50,
            '2023-04-10', N'Không có', N'Đực');
IF NOT EXISTS (SELECT 1 FROM dbo.ThuCung WHERE maThuCung = @pet2)
    INSERT INTO dbo.ThuCung
        (maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai, canNang,
         ngaySinh, diUng, gioiTinh)
    VALUES (@pet2, @customer2, N'Míu', N'Mèo', N'Mèo Anh lông ngắn', 15.00,
            '2022-08-15', N'Thịt bò', N'Cái');
IF NOT EXISTS (SELECT 1 FROM dbo.ThuCung WHERE maThuCung = @pet3)
    INSERT INTO dbo.ThuCung
        (maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai, canNang,
         ngaySinh, diUng, gioiTinh)
    VALUES (@pet3, @customer3, N'Bông', N'Chó', N'Golden Retriever', 32.00,
            '2021-11-02', N'Không có', N'Cái');

IF NOT EXISTS (SELECT 1 FROM dbo.NhaCungCap WHERE maNhaCungCap = @supplier)
    INSERT INTO dbo.NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi)
    VALUES (@supplier, N'Nhà cung cấp PetCare Demo', @supplierPhone,
            N'Quận Bình Thạnh, TP. Hồ Chí Minh');

IF NOT EXISTS (SELECT 1 FROM dbo.DichVuChamSoc WHERE maDichVu = @service)
    INSERT INTO dbo.DichVuChamSoc (maDichVu, tenDichVu, moTa, thoiLuong, trangThai)
    VALUES (@service, N'Tắm và vệ sinh', N'Dịch vụ tắm, sấy và vệ sinh thú cưng', 60, 1);

IF NOT EXISTS (SELECT 1 FROM dbo.BangGiaDichVu WHERE maBangGia = @price1)
    INSERT INTO dbo.BangGiaDichVu
        (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung, ngayHetHieuLuc)
    VALUES (@price1, @service, 120000, N'Nhỏ', CAST(GETDATE() AS date), NULL);
IF NOT EXISTS (SELECT 1 FROM dbo.BangGiaDichVu WHERE maBangGia = @price2)
    INSERT INTO dbo.BangGiaDichVu
        (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung, ngayHetHieuLuc)
    VALUES (@price2, @service, 200000, N'Vừa', CAST(GETDATE() AS date), NULL);
IF NOT EXISTS (SELECT 1 FROM dbo.BangGiaDichVu WHERE maBangGia = @price3)
    INSERT INTO dbo.BangGiaDichVu
        (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung, ngayHetHieuLuc)
    VALUES (@price3, @service, 320000, N'Lớn', CAST(GETDATE() AS date), NULL);

IF NOT EXISTS (SELECT 1 FROM dbo.SanPham WHERE maSanPham = @product1)
    INSERT INTO dbo.SanPham
        (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, soLuongTon,
         donViTinh, trangThai, nguongTonToiThieu)
    VALUES (@product1, @supplier, N'Hạt dinh dưỡng chó 2kg', @barcode1,
            185000, 3, N'Túi', 1, 10);
IF NOT EXISTS (SELECT 1 FROM dbo.SanPham WHERE maSanPham = @product2)
    INSERT INTO dbo.SanPham
        (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, soLuongTon,
         donViTinh, trangThai, nguongTonToiThieu)
    VALUES (@product2, @supplier, N'Cát vệ sinh mèo 5kg', @barcode2,
            95000, 35, N'Bao', 1, 10);

IF NOT EXISTS (SELECT 1 FROM dbo.DonHang WHERE maDonHang = @order)
BEGIN
    INSERT INTO dbo.DonHang
        (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai)
    VALUES (@order, @supplier, @employee, CAST(GETDATE() AS date), 925000, N'Chờ nhận hàng');

    INSERT INTO dbo.ChiTietDonHang (maDonHang, maSanPham, soLuong, donGia)
    VALUES (@order, @product1, 5, 185000);
END;

IF NOT EXISTS (SELECT 1 FROM dbo.KhuyenMai WHERE maKhuyenMai = @promotion)
    INSERT INTO dbo.KhuyenMai
        (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai)
    VALUES (@promotion, N'Ưu đãi khách hàng demo', 10,
            CAST(GETDATE() AS date), DATEADD(day, 30, CAST(GETDATE() AS date)), 1);

IF NOT EXISTS (SELECT 1 FROM dbo.LichHen WHERE maLichHen = @appointment1)
BEGIN
    INSERT INTO dbo.LichHen
        (maLichHen, maKhachHang, maThuCung, maNhanVien, ngayHen, gioHen, trangThai, ghiChu)
    VALUES
        (@appointment1, @customer1, @pet1, @employee, CAST(GETDATE() AS date), '09:00', N'Đã đặt', N'Demo: đã đặt'),
        (@appointment2, @customer2, @pet2, @employee, CAST(GETDATE() AS date), '10:00', N'Đã xác nhận', N'Demo: đã xác nhận'),
        (@appointment3, @customer3, @pet3, @employee, CAST(GETDATE() AS date), '11:00', N'Đang thực hiện', N'Demo: đang thực hiện'),
        (@appointment4, @customer1, @pet1, @employee, CAST(GETDATE() AS date), '13:00', N'Hoàn thành', N'Demo: hoàn thành'),
        (@appointment5, @customer2, @pet2, @employee, CAST(GETDATE() AS date), '14:00', N'Đã hủy', N'Demo: đã hủy');

    INSERT INTO dbo.ChiTietLichHen (maLichHen, maDichVu, canNangThuCung, donGia)
    VALUES
        (@appointment1, @service, 4.50, 120000),
        (@appointment2, @service, 15.00, 200000),
        (@appointment3, @service, 32.00, 320000),
        (@appointment4, @service, 4.50, 120000),
        (@appointment5, @service, 15.00, 200000);
END;
GO
