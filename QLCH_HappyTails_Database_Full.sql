USE master;
GO

-- 1. Tạo Database QLCH_HappyTails nếu chưa có
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'QLCH_HappyTails')
BEGIN
    CREATE DATABASE QLCH_HappyTails;
END
GO

USE QLCH_HappyTails;
GO

-- Xóa các bảng cũ theo thứ tự phụ thuộc nếu cần tạo lại sạch sẽ
IF OBJECT_ID('ChiTietPhieuDoiTra', 'U') IS NOT NULL DROP TABLE ChiTietPhieuDoiTra;
IF OBJECT_ID('PhieuDoiTra', 'U') IS NOT NULL DROP TABLE PhieuDoiTra;
IF OBJECT_ID('ChiTietHoaDon', 'U') IS NOT NULL DROP TABLE ChiTietHoaDon;
IF OBJECT_ID('HoaDon', 'U') IS NOT NULL DROP TABLE HoaDon;
IF OBJECT_ID('KhuyenMai', 'U') IS NOT NULL DROP TABLE KhuyenMai;
IF OBJECT_ID('ChiTietDonHang', 'U') IS NOT NULL DROP TABLE ChiTietDonHang;
IF OBJECT_ID('DonHang', 'U') IS NOT NULL DROP TABLE DonHang;
IF OBJECT_ID('SanPham', 'U') IS NOT NULL DROP TABLE SanPham;
IF OBJECT_ID('NhaCungCap', 'U') IS NOT NULL DROP TABLE NhaCungCap;
IF OBJECT_ID('ChiTietLichHen', 'U') IS NOT NULL DROP TABLE ChiTietLichHen;
IF OBJECT_ID('LichHen', 'U') IS NOT NULL DROP TABLE LichHen;
IF OBJECT_ID('BangGiaDichVu', 'U') IS NOT NULL DROP TABLE BangGiaDichVu;
IF OBJECT_ID('DichVuChamSoc', 'U') IS NOT NULL DROP TABLE DichVuChamSoc;
IF OBJECT_ID('PhanCa', 'U') IS NOT NULL DROP TABLE PhanCa;
IF OBJECT_ID('Ca', 'U') IS NOT NULL DROP TABLE Ca;
IF OBJECT_ID('ThuCung', 'U') IS NOT NULL DROP TABLE ThuCung;
IF OBJECT_ID('KhachHang', 'U') IS NOT NULL DROP TABLE KhachHang;
IF OBJECT_ID('TaiKhoan', 'U') IS NOT NULL DROP TABLE TaiKhoan;
IF OBJECT_ID('NhanVien', 'U') IS NOT NULL DROP TABLE NhanVien;
GO

-- =========================================================================
-- PHẦN 1: CÁC THỰC THỂ ĐỘC LẬP (KHÔNG CÓ KHÓA NGOẠI)
-- =========================================================================

-- 1. Bảng NhanVien (2.3 OOAD)
CREATE TABLE NhanVien (
    maNhanVien VARCHAR(30) PRIMARY KEY, -- NV-YYYYMMDD-NNNNNN
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(10) NOT NULL UNIQUE CHECK (soDienThoai NOT LIKE '%[^0-9]%'),
    chucVu NVARCHAR(50) NOT NULL CHECK (chucVu IN (N'Quản lý', N'Bán hàng', N'Chăm sóc', N'Thu ngân')),
    ngaySinh DATE NOT NULL,
    gioiTinh BIT NOT NULL, -- 1: Nam, 0: Nữ
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang làm' CHECK (trangThai IN (N'Đang làm', N'Đã nghỉ việc'))
);
GO

-- 2. Bảng KhachHang (2.1 OOAD)
CREATE TABLE KhachHang (
    maKhachHang VARCHAR(30) PRIMARY KEY, -- KH-YYYYMMDD-NNNNNN
    hoTen NVARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(10) NOT NULL UNIQUE CHECK (soDienThoai NOT LIKE '%[^0-9]%'),
    email VARCHAR(100),
    diemTichLuy INT NOT NULL DEFAULT 0 CHECK (diemTichLuy >= 0),
    diaChi NVARCHAR(255),
    ghiChu NVARCHAR(255)
);
GO

-- 3. Bảng Ca (2.6 OOAD)
CREATE TABLE Ca (
    maCa VARCHAR(30) PRIMARY KEY, -- CA-YYYYMMDD-NNNNNN
    tenCa NVARCHAR(50) NOT NULL,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Áp dụng' CHECK (trangThai IN (N'Áp dụng', N'Ngừng áp dụng')),
    CONSTRAINT CK_Ca_Gio CHECK (gioKetThuc > gioBatDau)
);
GO

-- 4. Bảng DichVuChamSoc (2.10 OOAD)
CREATE TABLE DichVuChamSoc (
    maDichVu VARCHAR(30) PRIMARY KEY, -- DV-YYYYMMDD-NNNNNN
    tenDichVu NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(255),
    thoiLuong INT NOT NULL DEFAULT 30 CHECK (thoiLuong > 0), -- Số phút ước tính
    trangThai BIT NOT NULL DEFAULT 1 -- 1: Đang cung cấp, 0: Tạm ngừng
);
GO

-- 5. Bảng NhaCungCap (2.14 OOAD)
CREATE TABLE NhaCungCap (
    maNhaCungCap VARCHAR(30) PRIMARY KEY, -- NCC-YYYYMMDD-NNNNNN
    tenNhaCungCap NVARCHAR(150) NOT NULL,
    soDienThoai VARCHAR(15) NOT NULL CHECK (soDienThoai NOT LIKE '%[^0-9]%'),
    diaChi NVARCHAR(255)
);
GO

-- 6. Bảng KhuyenMai (2.17 OOAD)
CREATE TABLE KhuyenMai (
    maKhuyenMai VARCHAR(30) PRIMARY KEY, -- KM-YYYYMMDD-NNNNNN
    tenChuongTrinh NVARCHAR(150) NOT NULL,
    phanTramGiam DECIMAL(5,2) NOT NULL CHECK (phanTramGiam > 0 AND phanTramGiam <= 100),
    ngayBatDau DATE NOT NULL,
    ngayKetThuc DATE NOT NULL,
    trangThai BIT NOT NULL DEFAULT 1, -- 1: Đang chạy, 0: Ngừng
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (ngayKetThuc >= ngayBatDau)
);
GO

-- =========================================================================
-- PHẦN 2: CÁC THỰC THỂ PHỤ THUỘC BẬC 1 (CÓ 1 KHÓA NGOẠI)
-- =========================================================================

-- 7. Bảng TaiKhoan (2.4 OOAD - Quan hệ 1-1 với NhanVien)
CREATE TABLE TaiKhoan (
    maTaiKhoan VARCHAR(30) PRIMARY KEY, -- TK-YYYYMMDD-NNNNNN
    maNhanVien VARCHAR(30) NOT NULL UNIQUE,
    tenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    matKhau VARCHAR(255) NOT NULL,
    phanQuyen NVARCHAR(30) NOT NULL CHECK (phanQuyen IN (N'Quản lý', N'Bán hàng', N'Chăm sóc', N'Thu ngân')),
    trangThai BIT NOT NULL DEFAULT 1, -- 1: Hoạt động, 0: Bị khóa
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

-- 8. Bảng ThuCung (2.2 OOAD - Thuộc về KhachHang)
CREATE TABLE ThuCung (
    maThuCung VARCHAR(30) PRIMARY KEY, -- TC-YYYYMMDD-NNNNNN
    maKhachHang VARCHAR(30) NOT NULL,
    tenThuCung NVARCHAR(100) NOT NULL,
    loaiThuCung NVARCHAR(50), -- Chó, Mèo...
    giongLoai NVARCHAR(100),
    canNang DECIMAL(5,2) NOT NULL CHECK (canNang > 0),
    ngaySinh DATE,
    diUng NVARCHAR(255), -- Dị ứng, bệnh nền, ghi chú tính cách
    gioiTinh NVARCHAR(10) CHECK (gioiTinh IN (N'Đực', N'Cái')),
    CONSTRAINT FK_ThuCung_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT UQ_ThuCung_Ma_KhachHang UNIQUE (maThuCung, maKhachHang)
);
GO

-- 9. Bảng PhanCa (2.7 OOAD - Phân ca làm việc cho NhanVien)
CREATE TABLE PhanCa (
    maPhanCa VARCHAR(30) PRIMARY KEY, -- PC-YYYYMMDD-NNNNNN
    maNhanVien VARCHAR(30) NOT NULL,
    maCa VARCHAR(30) NOT NULL,
    ngayLam DATE NOT NULL,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đã phân công' CHECK (trangThai IN (N'Đã phân công', N'Đã hủy')),
    CONSTRAINT FK_PhanCa_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_PhanCa_Ca FOREIGN KEY (maCa) REFERENCES Ca(maCa)
);
GO

-- Chỉ chặn trùng với các phân ca còn hiệu lực (cho phép phân lại sau khi hủy)
CREATE UNIQUE INDEX UQ_PhanCa_NhanVien_Ngay_Ca
    ON PhanCa (maNhanVien, maCa, ngayLam)
    WHERE trangThai = N'Đã phân công';
GO

-- 10. Bảng BangGiaDichVu (2.11 OOAD - 3 bậc giá cân nặng của dịch vụ)
CREATE TABLE BangGiaDichVu (
    maBangGia VARCHAR(30) PRIMARY KEY, -- BG-YYYYMMDD-NNNNNN
    maDichVu VARCHAR(30) NOT NULL,
    gia DECIMAL(18,2) NOT NULL CHECK (gia >= 0),
    phanLoaiKichThuoc NVARCHAR(20) NOT NULL CHECK (phanLoaiKichThuoc IN (N'Nhỏ', N'Vừa', N'Lớn')), -- Nhỏ (<5kg), Vừa (5-10kg), Lớn (>10kg)
    ngayApDung DATE NOT NULL,
    ngayHetHieuLuc DATE,
    CONSTRAINT FK_BangGia_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVuChamSoc(maDichVu),
    CONSTRAINT CK_BangGia_HieuLuc CHECK (ngayHetHieuLuc IS NULL OR ngayHetHieuLuc >= ngayApDung),
    CONSTRAINT UQ_BangGia_DichVu_Bac_Ngay UNIQUE (maDichVu, phanLoaiKichThuoc, ngayApDung)
);
GO

-- 11. Bảng SanPham (2.13 OOAD - Hàng hóa cửa hàng)
CREATE TABLE SanPham (
    maSanPham VARCHAR(30) PRIMARY KEY, -- SP-YYYYMMDD-NNNNNN
    maNhaCungCap VARCHAR(30),
    tenSanPham NVARCHAR(150) NOT NULL,
    maVach VARCHAR(50) UNIQUE,
    giaBan DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (giaBan >= 0),
    soLuongTon INT NOT NULL DEFAULT 0 CHECK (soLuongTon >= 0),
    donViTinh NVARCHAR(30) NOT NULL,
    trangThai BIT NOT NULL DEFAULT 1, -- 1: Đang bán, 0: Ngừng kinh doanh
    CONSTRAINT FK_SanPham_NhaCungCap FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap)
);
GO

-- =========================================================================
-- PHẦN 3: NGHIỆP VỤ LỊCH HẸN & ĐƠN HÀNG NHẬP
-- =========================================================================

-- 12. Bảng LichHen (2.8 OOAD)
CREATE TABLE LichHen (
    maLichHen VARCHAR(30) PRIMARY KEY, -- LH-YYYYMMDD-NNNNNN
    maKhachHang VARCHAR(30) NOT NULL,
    maThuCung VARCHAR(30) NOT NULL,
    maNhanVien VARCHAR(30) NOT NULL,
    ngayHen DATE NOT NULL,
    gioHen TIME NOT NULL,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đã đặt' 
        CHECK (trangThai IN (N'Đã đặt', N'Đã xác nhận', N'Đang thực hiện', N'Hoàn thành', N'Đã hủy')),
    ghiChu NVARCHAR(255),
    CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_LichHen_ThuCung FOREIGN KEY (maThuCung, maKhachHang) REFERENCES ThuCung(maThuCung, maKhachHang),
    CONSTRAINT FK_LichHen_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

-- 13. Bảng ChiTietLichHen (2.9 OOAD)
CREATE TABLE ChiTietLichHen (
    maLichHen VARCHAR(30) NOT NULL,
    maDichVu VARCHAR(30) NOT NULL,
    canNangThuCung DECIMAL(5,2) NOT NULL CHECK (canNangThuCung > 0),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    PRIMARY KEY (maLichHen, maDichVu),
    CONSTRAINT FK_CTLichHen_LichHen FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen),
    CONSTRAINT FK_CTLichHen_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVuChamSoc(maDichVu)
);
GO

-- 14. Bảng DonHang (2.15 OOAD - Đơn hàng nhập từ Nhà cung cấp)
CREATE TABLE DonHang (
    maDonHang VARCHAR(30) PRIMARY KEY, -- DH-YYYYMMDD-NNNNNN
    maNhaCungCap VARCHAR(30) NOT NULL,
    maNhanVien VARCHAR(30) NOT NULL,
    ngayLap DATE NOT NULL,
    tongTien DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tongTien >= 0),
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ nhận hàng' 
        CHECK (trangThai IN (N'Chờ nhận hàng', N'Hoàn tất', N'Đã hủy')),
    CONSTRAINT FK_DonHang_NhaCungCap FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap),
    CONSTRAINT FK_DonHang_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

-- 15. Bảng ChiTietDonHang (2.16 OOAD)
CREATE TABLE ChiTietDonHang (
    maDonHang VARCHAR(30) NOT NULL,
    maSanPham VARCHAR(30) NOT NULL,
    soLuong INT NOT NULL CHECK (soLuong > 0),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    PRIMARY KEY (maDonHang, maSanPham),
    CONSTRAINT FK_CTDonHang_DonHang FOREIGN KEY (maDonHang) REFERENCES DonHang(maDonHang),
    CONSTRAINT FK_CTDonHang_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
);
GO

-- =========================================================================
-- PHẦN 4: HÓA ĐƠN BÁN LẺ & ĐỔI TRẢ
-- =========================================================================

-- 16. Bảng HoaDon (2.18 OOAD)
CREATE TABLE HoaDon (
    maHoaDon VARCHAR(30) PRIMARY KEY, -- HD-YYYYMMDD-NNNNNN
    maLichHen VARCHAR(30), -- Nullable nếu chỉ mua sản phẩm lẻ tại quầy
    maNhanVien VARCHAR(30) NOT NULL,
    maKhachHang VARCHAR(30), -- Nullable nếu khách vãng lai không đăng ký
    ngayLap DATE NOT NULL,
    hinhThucThanhToan NVARCHAR(30) CHECK (hinhThucThanhToan IN (N'Tiền mặt', N'Chuyển khoản', N'POS')),
    trangThaiThanhToan NVARCHAR(30) NOT NULL DEFAULT N'Chưa thanh toán' 
        CHECK (trangThaiThanhToan IN (N'Chưa thanh toán', N'Đã thanh toán', N'Đã hủy')),
    maKhuyenMai VARCHAR(30),
    tienGiam DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienGiam >= 0), -- Số tiền giảm từ khuyến mãi
    tongTien DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tongTien >= 0), -- Tổng tiền SAU khi trừ tienGiam
    CONSTRAINT FK_HoaDon_LichHen FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai)
);
GO

-- 17. Bảng ChiTietHoaDon (2.19 OOAD - Dòng bán sản phẩm HOẶC dịch vụ)
CREATE TABLE ChiTietHoaDon (
    maChiTietHD VARCHAR(30) PRIMARY KEY, -- CT-YYYYMMDD-NNNNNN
    maHoaDon VARCHAR(30) NOT NULL,
    maSanPham VARCHAR(30),
    maDichVu VARCHAR(30),
    soLuong INT NOT NULL DEFAULT 1 CHECK (soLuong > 0),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    CONSTRAINT FK_CTHoaDon_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_CTHoaDon_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
    CONSTRAINT FK_CTHoaDon_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVuChamSoc(maDichVu),
    -- Ràng buộc: Mỗi dòng phải là Sản phẩm HOẶC Dịch vụ (không đồng thời null hoặc đồng thời có cả hai)
    CONSTRAINT CK_CTHoaDon_LoaiHang CHECK (
        (maSanPham IS NOT NULL AND maDichVu IS NULL) OR 
        (maSanPham IS NULL AND maDichVu IS NOT NULL)
    )
);
GO

-- 18. Bảng PhieuDoiTra (2.20 OOAD)
CREATE TABLE PhieuDoiTra (
    maPhieuDoiTra VARCHAR(30) PRIMARY KEY, -- PDT-YYYYMMDD-NNNNNN
    maHoaDon VARCHAR(30) NOT NULL,
    maNhanVien VARCHAR(30) NOT NULL,
    maKhachHang VARCHAR(30) NOT NULL,
    ngayLap DATE NOT NULL,
    lyDo NVARCHAR(255) NOT NULL,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xử lý' 
        CHECK (trangThai IN (N'Chờ xử lý', N'Đã hoàn tiền', N'Từ chối')),
    tongTienHoan DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tongTienHoan >= 0), -- Số tiền hoàn lại cho khách
    CONSTRAINT FK_PhieuDoiTra_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_PhieuDoiTra_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_PhieuDoiTra_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang)
);
GO

-- 19. Bảng ChiTietPhieuDoiTra (2.21 OOAD)
CREATE TABLE ChiTietPhieuDoiTra (
    maPhieuDoiTra VARCHAR(30) NOT NULL,
    maSanPham VARCHAR(30) NOT NULL,
    soLuong INT NOT NULL CHECK (soLuong > 0),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    PRIMARY KEY (maPhieuDoiTra, maSanPham),
    CONSTRAINT FK_CTPhieuDoiTra_PhieuDoiTra FOREIGN KEY (maPhieuDoiTra) REFERENCES PhieuDoiTra(maPhieuDoiTra),
    CONSTRAINT FK_CTPhieuDoiTra_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
);
GO

-- =========================================================================
-- NẠP DỮ LIỆU MẪU BAN ĐẦU CHUẨN ĐỒ ÁN HAPPY TAILS
-- =========================================================================

-- 1. NhanVien
INSERT INTO NhanVien (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
VALUES 
('NV-20261005-000001', N'Nguyễn Thị Lan', '0901234567', N'Quản lý', '1985-05-15', 0, N'Đang làm'),
('NV-20261005-000002', N'Trần Văn Minh', '0902345678', N'Bán hàng', '1998-08-20', 1, N'Đang làm'),
('NV-20261005-000003', N'Phạm Thị Hoa',  '0903456789', N'Chăm sóc', '1995-12-10', 0, N'Đang làm'),
('NV-20261005-000004', N'Lê Thị Thắm',   '0904567890', N'Thu ngân', '2000-03-25', 0, N'Đang làm'),
('NV-20261005-000005', N'Hoàng Thu Hà',  '0905678901', N'Thu ngân', '2001-07-18', 0, N'Đang làm');

-- 2. TaiKhoan (cho 4 vai trò)
INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
VALUES 
('TK-20261005-000001', 'NV-20261005-000001', 'quanly',  '123456', N'Quản lý', 1),
('TK-20261005-000002', 'NV-20261005-000002', 'banhang', '123456', N'Bán hàng', 1),
('TK-20261005-000003', 'NV-20261005-000003', 'chamsoc', '123456', N'Chăm sóc', 1),
('TK-20261005-000004', 'NV-20261005-000004', 'thungan', '123456', N'Thu ngân', 1),
('TK-20261005-000005', 'NV-20261005-000005', 'thungan2', '123456', N'Thu ngân', 1);

-- 3. KhachHang
INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu)
VALUES 
('KH-20261005-000001', N'Nguyễn Thu Trang', '0912345678', 'trang.nguyen@gmail.com', 120, N'12 Nguyễn Văn Bảo, Gò Vấp', N'Khách quen, hay đặt lịch cuối tuần'),
('KH-20261005-000002', N'Lê Hoàng Nam',     '0923456789', 'nam.le@gmail.com', 50, N'45 Lê Đức Thọ, Gò Vấp', N'Nuôi 2 bé chó Poodle'),
('KH-20261005-000003', N'Võ Thị Mai',       '0934567890', 'mai.vo@gmail.com', 0, N'78 Quang Trung, Gò Vấp', N'Khách mới');

-- 4. ThuCung
INSERT INTO ThuCung (maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai, canNang, ngaySinh, diUng, gioiTinh)
VALUES 
('TC-20261005-000001', 'KH-20261005-000001', N'Mochi', N'Mèo', N'Mèo Anh lông ngắn', 4.50, '2023-04-10', N'Da nhạy cảm, dùng sữa tắm không mùi', N'Cái'),
('TC-20261005-000002', 'KH-20261005-000002', N'Bông',  N'Chó', N'Poodle Tiny', 3.20, '2022-11-05', N'Không có', N'Đực'),
('TC-20261005-000003', 'KH-20261005-000002', N'Lu',    N'Chó', N'Poodle Standard', 8.50, '2021-08-15', N'Nhát nước, cần tắm nhẹ nhàng', N'Đực');

-- 5. Ca
INSERT INTO Ca (maCa, tenCa, gioBatDau, gioKetThuc, trangThai)
VALUES 
('CA-20261005-000001', N'Ca Sáng',  '08:00:00', '12:00:00', N'Áp dụng'),
('CA-20261005-000002', N'Ca Chiều', '13:00:00', '17:00:00', N'Áp dụng'),
('CA-20261005-000003', N'Ca Tối',   '18:00:00', '21:00:00', N'Áp dụng');

-- 6. PhanCa
INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
VALUES 
('PC-20261005-000001', 'NV-20261005-000002', 'CA-20261005-000001', '2026-10-06', N'Đã phân công'),
('PC-20261005-000002', 'NV-20261005-000003', 'CA-20261005-000001', '2026-10-06', N'Đã phân công'),
('PC-20261005-000003', 'NV-20261005-000004', 'CA-20261005-000001', '2026-10-06', N'Đã phân công');

-- 7. DichVuChamSoc (5 dịch vụ chính của Happy Tails theo Q&A mục 1.4.5)
INSERT INTO DichVuChamSoc (maDichVu, tenDichVu, moTa, thoiLuong, trangThai)
VALUES 
('DV-20261005-000001', N'Tắm vệ sinh',           N'Tắm sấy, chải lông, vắt tuyến hôi', 40, 1),
('DV-20261005-000002', N'Grooming tạo kiểu',     N'Tắm, sấy và cắt tỉa tạo kiểu thẩm mỹ', 90, 1),
('DV-20261005-000003', N'Cắt tỉa lông cơ bản',   N'Cắt tỉa gọn gàng lông thừa, lông chân', 60, 1),
('DV-20261005-000004', N'Cắt mài móng',          N'Bấm móng và dũa móng an toàn', 15, 1),
('DV-20261005-000005', N'Vệ sinh tai',           N'Nhổ lông tai và nhỏ thuốc khử khuẩn', 15, 1);

-- 8. BangGiaDichVu (3 bậc cân nặng: Nhỏ <5kg, Vừa 5-10kg, Lớn >10kg theo Q&A mục 1.4.5)
INSERT INTO BangGiaDichVu (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung)
VALUES 
-- Tắm
('BG-20261005-000001', 'DV-20261005-000001', 80000,  N'Nhỏ', '2026-01-01'),
('BG-20261005-000002', 'DV-20261005-000001', 120000, N'Vừa', '2026-01-01'),
('BG-20261005-000003', 'DV-20261005-000001', 180000, N'Lớn', '2026-01-01'),
-- Grooming
('BG-20261005-000004', 'DV-20261005-000002', 150000, N'Nhỏ', '2026-01-01'),
('BG-20261005-000005', 'DV-20261005-000002', 220000, N'Vừa', '2026-01-01'),
('BG-20261005-000006', 'DV-20261005-000002', 320000, N'Lớn', '2026-01-01'),
-- Cắt tỉa
('BG-20261005-000007', 'DV-20261005-000003', 100000, N'Nhỏ', '2026-01-01'),
('BG-20261005-000008', 'DV-20261005-000003', 150000, N'Vừa', '2026-01-01'),
('BG-20261005-000009', 'DV-20261005-000003', 220000, N'Lớn', '2026-01-01'),
-- Cắt móng
('BG-20261005-000010', 'DV-20261005-000004', 30000,  N'Nhỏ', '2026-01-01'),
('BG-20261005-000011', 'DV-20261005-000004', 30000,  N'Vừa', '2026-01-01'),
('BG-20261005-000012', 'DV-20261005-000004', 40000,  N'Lớn', '2026-01-01'),
-- Vệ sinh tai
('BG-20261005-000013', 'DV-20261005-000005', 30000,  N'Nhỏ', '2026-01-01'),
('BG-20261005-000014', 'DV-20261005-000005', 30000,  N'Vừa', '2026-01-01'),
('BG-20261005-000015', 'DV-20261005-000005', 40000,  N'Lớn', '2026-01-01');

-- 9. NhaCungCap
INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi)
VALUES 
('NCC-20261005-000001', N'Công ty CP Thức Ăn Thú Cưng Royal Canin VN', '02838123456', N'KCN Tân Bình, TP.HCM'),
('NCC-20261005-000002', N'Nhà phân phối Phụ kiện thú cưng PetCare',     '02838987654', N'Quận 10, TP.HCM');

-- 10. SanPham
INSERT INTO SanPham (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, soLuongTon, donViTinh, trangThai)
VALUES 
('SP-20261005-000001', 'NCC-20261005-000001', N'Hạt Royal Canin Indoor 2kg', '893123456701', 350000, 45, N'Gói', 1),
('SP-20261005-000002', 'NCC-20261005-000001', N'Pate Whiskas Cá Biển 85g',   '893123456702', 20000,  120, N'Gói', 1),
('SP-20261005-000003', 'NCC-20261005-000002', N'Cát vệ sinh Cature gỗ 6L',   '893123456703', 140000, 30, N'Túi', 1),
('SP-20261005-000004', 'NCC-20261005-000002', N'Dây dắt kèm yếm ngực size M', '893123456704', 85000,  15, N'Cái', 1),
('SP-20261005-000005', 'NCC-20261005-000002', N'Sữa tắm thú cưng SOS 530ml', '893123456705', 120000, 25, N'Chai', 1);

-- 11. KhuyenMai
INSERT INTO KhuyenMai (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai)
VALUES 
('KM-20261005-000001', N'Ưu đãi Khai trương Spa', 10.00, '2026-10-01', '2026-10-31', 1),
('KM-20261005-000002', N'Tri ân Thành viên Gold',  15.00, '2026-10-01', '2026-12-31', 1);
GO
