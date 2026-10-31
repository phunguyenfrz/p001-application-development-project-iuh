USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'QLCH_HappyTails')
BEGIN
    CREATE DATABASE QLCH_HappyTails;
END
GO

USE QLCH_HappyTails;
GO

-- Xóa bảng cũ nếu đã tồn tại (theo thứ tự khóa ngoại)
IF OBJECT_ID('ChiTietPhieuDoiTra', 'U') IS NOT NULL DROP TABLE ChiTietPhieuDoiTra;
IF OBJECT_ID('PhieuDoiTra', 'U') IS NOT NULL DROP TABLE PhieuDoiTra;
IF OBJECT_ID('ChiTietHoaDon_DichVu', 'U') IS NOT NULL DROP TABLE ChiTietHoaDon_DichVu;
IF OBJECT_ID('ChiTietHoaDon_SanPham', 'U') IS NOT NULL DROP TABLE ChiTietHoaDon_SanPham;
IF OBJECT_ID('ChiTietHoaDon', 'U') IS NOT NULL DROP TABLE ChiTietHoaDon;
IF OBJECT_ID('HoaDon', 'U') IS NOT NULL DROP TABLE HoaDon;
IF OBJECT_ID('ChiTietPhieuDichVu', 'U') IS NOT NULL DROP TABLE ChiTietPhieuDichVu;
IF OBJECT_ID('PhieuDichVu', 'U') IS NOT NULL DROP TABLE PhieuDichVu;
IF OBJECT_ID('KhuyenMai', 'U') IS NOT NULL DROP TABLE KhuyenMai;
IF OBJECT_ID('ChiTietDonNhap', 'U') IS NOT NULL DROP TABLE ChiTietDonNhap;
IF OBJECT_ID('ChiTietDonHang', 'U') IS NOT NULL DROP TABLE ChiTietDonHang;
IF OBJECT_ID('DonNhapHang', 'U') IS NOT NULL DROP TABLE DonNhapHang;
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


-- =======================================================
-- 1. CỤM KHÁCH HÀNG & THÚ CƯNG
-- =======================================================

-- Khách hàng
CREATE TABLE KhachHang (
    maKhachHang VARCHAR(20) PRIMARY KEY CHECK (maKhachHang LIKE 'KH-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    hoTen NVARCHAR(100) NOT NULL CHECK (LEN(LTRIM(RTRIM(hoTen))) > 0),
    soDienThoai VARCHAR(10) NOT NULL UNIQUE CHECK (soDienThoai LIKE '0[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'),
    email VARCHAR(100) CHECK (email IS NULL OR (email LIKE '%_@__%.__%')),
    diemTichLuy INT NOT NULL DEFAULT 0 CHECK (diemTichLuy >= 0),
    diaChi NVARCHAR(255),
    ghiChu NVARCHAR(255)
);
GO

-- Thú cưng của khách
CREATE TABLE ThuCung (
    maThuCung VARCHAR(20) PRIMARY KEY CHECK (maThuCung LIKE 'TC-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maKhachHang VARCHAR(20) NOT NULL,
    tenThuCung NVARCHAR(100) NOT NULL CHECK (LEN(LTRIM(RTRIM(tenThuCung))) > 0),
    loaiThuCung NVARCHAR(50),
    giongLoai NVARCHAR(100),
    canNang DECIMAL(5,2) NOT NULL CHECK (canNang > 0),
    ngaySinh DATE CHECK (ngaySinh IS NULL OR ngaySinh <= CAST(GETDATE() AS DATE)),
    diUng NVARCHAR(255),
    gioiTinh NVARCHAR(10) CHECK (gioiTinh IN (N'Đực', N'Cái')),
    CONSTRAINT FK_ThuCung_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT UQ_ThuCung_Ma_KhachHang UNIQUE (maThuCung, maKhachHang)
);
GO


-- =======================================================
-- 2. CỤM NHÂN VIÊN, TÀI KHOẢN & PHÂN CA
-- =======================================================

-- Nhân viên
CREATE TABLE NhanVien (
    maNhanVien VARCHAR(20) PRIMARY KEY CHECK (maNhanVien LIKE 'NV-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    hoTen NVARCHAR(100) NOT NULL CHECK (LEN(LTRIM(RTRIM(hoTen))) > 0),
    soDienThoai VARCHAR(10) NOT NULL UNIQUE CHECK (soDienThoai LIKE '0[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'),
    chucVu NVARCHAR(50) NOT NULL CHECK (chucVu IN (N'Quản lý', N'Bán hàng', N'Chăm sóc')),
    ngaySinh DATE NOT NULL CHECK (DATEDIFF(year, ngaySinh, GETDATE()) >= 18),
    gioiTinh BIT NOT NULL, -- 1: Nam, 0: Nữ
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang làm' CHECK (trangThai IN (N'Đang làm', N'Đã nghỉ việc'))
);
GO

-- Tài khoản đăng nhập
CREATE TABLE TaiKhoan (
    maTaiKhoan VARCHAR(20) PRIMARY KEY CHECK (maTaiKhoan LIKE 'TK-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maNhanVien VARCHAR(20) NOT NULL UNIQUE,
    tenDangNhap VARCHAR(50) NOT NULL UNIQUE CHECK (LEN(LTRIM(RTRIM(tenDangNhap))) > 0),
    matKhau VARCHAR(255) NOT NULL,
    phanQuyen NVARCHAR(30) NOT NULL CHECK (phanQuyen IN (N'Admin', N'Quản lý', N'Bán hàng', N'Chăm sóc')),
    trangThai BIT NOT NULL DEFAULT 1,
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

-- Ca làm việc
CREATE TABLE Ca (
    maCa VARCHAR(20) PRIMARY KEY CHECK (maCa LIKE 'CA-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    tenCa NVARCHAR(50) NOT NULL CHECK (LEN(LTRIM(RTRIM(tenCa))) > 0),
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Áp dụng' CHECK (trangThai IN (N'Áp dụng', N'Ngừng áp dụng')),
    CONSTRAINT CK_Ca_Gio CHECK (gioKetThuc > gioBatDau)
);
GO

-- Phân ca làm việc
CREATE TABLE PhanCa (
    maPhanCa VARCHAR(20) PRIMARY KEY CHECK (maPhanCa LIKE 'PC-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maNhanVien VARCHAR(20) NOT NULL,
    maCa VARCHAR(20) NOT NULL,
    ngayLam DATE NOT NULL,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đã phân công' CHECK (trangThai IN (N'Đã phân công', N'Đã hủy')),
    CONSTRAINT FK_PhanCa_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_PhanCa_Ca FOREIGN KEY (maCa) REFERENCES Ca(maCa)
);
GO

CREATE UNIQUE INDEX UQ_PhanCa_NhanVien_Ngay_Ca
    ON PhanCa (maNhanVien, maCa, ngayLam)
    WHERE trangThai = N'Đã phân công';
GO


-- =======================================================
-- 3. CỤM DỊCH VỤ, LỊCH HẸN & TIẾP NHẬN
-- =======================================================

-- Danh mục dịch vụ spa / chăm sóc
CREATE TABLE DichVuChamSoc (
    maDichVu VARCHAR(20) PRIMARY KEY CHECK (maDichVu LIKE 'DV-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    tenDichVu NVARCHAR(100) NOT NULL CHECK (LEN(LTRIM(RTRIM(tenDichVu))) > 0),
    moTa NVARCHAR(255),
    thoiLuong INT NOT NULL DEFAULT 30 CHECK (thoiLuong > 0), -- phút
    trangThai BIT NOT NULL DEFAULT 1
);
GO

-- Bảng giá dịch vụ theo kích cỡ thú cưng
CREATE TABLE BangGiaDichVu (
    maBangGia VARCHAR(20) PRIMARY KEY CHECK (maBangGia LIKE 'BG-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maDichVu VARCHAR(20) NOT NULL,
    gia DECIMAL(18,2) NOT NULL CHECK (gia >= 0),
    phanLoaiKichThuoc NVARCHAR(20) NOT NULL CHECK (phanLoaiKichThuoc IN (N'Nhỏ', N'Vừa', N'Lớn')),
    ngayApDung DATE NOT NULL,
    ngayHetHieuLuc DATE,
    CONSTRAINT FK_BangGia_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVuChamSoc(maDichVu),
    CONSTRAINT CK_BangGia_HieuLuc CHECK (ngayHetHieuLuc IS NULL OR ngayHetHieuLuc >= ngayApDung),
    CONSTRAINT UQ_BangGia_DichVu_Bac_Ngay UNIQUE (maDichVu, phanLoaiKichThuoc, ngayApDung)
);
GO

-- Lịch hẹn trước của khách
CREATE TABLE LichHen (
    maLichHen VARCHAR(20) PRIMARY KEY CHECK (maLichHen LIKE 'LH-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maKhachHang VARCHAR(20) NOT NULL,
    maThuCung VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
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

-- Chi tiết các dịch vụ trong lịch hẹn
CREATE TABLE ChiTietLichHen (
    maLichHen VARCHAR(20) NOT NULL,
    maDichVu VARCHAR(20) NOT NULL,
    canNangThuCung DECIMAL(5,2) NOT NULL CHECK (canNangThuCung > 0),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    PRIMARY KEY (maLichHen, maDichVu),
    CONSTRAINT FK_CTLichHen_LichHen FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen),
    CONSTRAINT FK_CTLichHen_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVuChamSoc(maDichVu)
);
GO

-- Phiếu tiếp nhận thú cưng làm dịch vụ
CREATE TABLE PhieuDichVu (
    maPhieuDichVu VARCHAR(20) PRIMARY KEY CHECK (maPhieuDichVu LIKE 'PDV-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maKhachHang VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
    maLichHen VARCHAR(20) NULL,
    ngayTiepNhan DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    gioTiepNhan TIME NOT NULL DEFAULT CAST(GETDATE() AS TIME),
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang chờ' 
        CHECK (trangThai IN (N'Đang chờ', N'Đang thực hiện', N'Hoàn thành', N'Đã hủy')),
    ghiChu NVARCHAR(255),
    CONSTRAINT FK_PhieuDichVu_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_PhieuDichVu_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_PhieuDichVu_LichHen FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen)
);
GO

-- Chi tiết phiếu tiếp nhận
CREATE TABLE ChiTietPhieuDichVu (
    maPhieuDichVu VARCHAR(20) NOT NULL,
    maThuCung VARCHAR(20) NOT NULL,
    maDichVu VARCHAR(20) NOT NULL,
    donGia DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (donGia >= 0),
    PRIMARY KEY (maPhieuDichVu, maThuCung, maDichVu),
    CONSTRAINT FK_CTPDV_PhieuDichVu FOREIGN KEY (maPhieuDichVu) REFERENCES PhieuDichVu(maPhieuDichVu) ON DELETE CASCADE,
    CONSTRAINT FK_CTPDV_ThuCung FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),
    CONSTRAINT FK_CTPDV_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVuChamSoc(maDichVu)
);
GO


-- =======================================================
-- 4. CỤM HÀNG HÓA, HÓA ĐƠN & ĐỔI TRẢ
-- =======================================================

-- Nhà cung cấp
CREATE TABLE NhaCungCap (
    maNhaCungCap VARCHAR(20) PRIMARY KEY CHECK (maNhaCungCap LIKE 'NCC-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    tenNhaCungCap NVARCHAR(150) NOT NULL CHECK (LEN(LTRIM(RTRIM(tenNhaCungCap))) > 0),
    soDienThoai VARCHAR(10) NOT NULL CHECK (soDienThoai LIKE '0[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'),
    diaChi NVARCHAR(255),
    email NVARCHAR(100) NULL,
    trangThai BIT NOT NULL DEFAULT 1
);
GO

-- Hàng hóa / Sản phẩm
CREATE TABLE SanPham (
    maSanPham VARCHAR(20) PRIMARY KEY CHECK (maSanPham LIKE 'SP-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maNhaCungCap VARCHAR(20),
    tenSanPham NVARCHAR(150) NOT NULL CHECK (LEN(LTRIM(RTRIM(tenSanPham))) > 0),
    maVach VARCHAR(50) NOT NULL UNIQUE CHECK (LEN(LTRIM(RTRIM(maVach))) > 0),
    giaBan DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (giaBan >= 0),
    soLuongTon INT NOT NULL DEFAULT 0 CHECK (soLuongTon >= 0),
    nguongTonToiThieu INT NOT NULL DEFAULT 10 CHECK (nguongTonToiThieu >= 0),
    donViTinh NVARCHAR(30) NOT NULL CHECK (LEN(LTRIM(RTRIM(donViTinh))) > 0),
    trangThai BIT NOT NULL DEFAULT 1,
    CONSTRAINT FK_SanPham_NhaCungCap FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap)
);
GO

-- Đơn nhập hàng
CREATE TABLE DonNhapHang (
    maDonHang VARCHAR(20) PRIMARY KEY CHECK (maDonHang LIKE 'PN-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]' OR maDonHang LIKE 'DH-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maNhaCungCap VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
    ngayLap DATE NOT NULL,
    gioLap TIME(0) NOT NULL CONSTRAINT DF_DonHang_gioLap DEFAULT ('00:00:00'),
    tongTien DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tongTien >= 0),
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ nhận hàng' 
        CHECK (trangThai IN (N'Chờ nhận hàng', N'Hoàn tất', N'Đã hủy')),
    CONSTRAINT FK_DonHang_NhaCungCap FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap),
    CONSTRAINT FK_DonHang_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

-- Chi tiết đơn nhập hàng
CREATE TABLE ChiTietDonNhap (
    maDonHang VARCHAR(20) NOT NULL,
    maSanPham VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL CHECK (soLuong > 0),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    PRIMARY KEY (maDonHang, maSanPham),
    CONSTRAINT FK_CTDonHang_DonHang FOREIGN KEY (maDonHang) REFERENCES DonNhapHang(maDonHang),
    CONSTRAINT FK_CTDonHang_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
);
GO

-- Khuyến mãi
CREATE TABLE KhuyenMai (
    maKhuyenMai VARCHAR(20) PRIMARY KEY CHECK (maKhuyenMai LIKE 'KM-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    tenChuongTrinh NVARCHAR(150) NOT NULL CHECK (LEN(LTRIM(RTRIM(tenChuongTrinh))) > 0),
    phanTramGiam DECIMAL(5,2) NOT NULL CHECK (phanTramGiam > 0 AND phanTramGiam <= 100),
    ngayBatDau DATE NOT NULL,
    ngayKetThuc DATE NOT NULL,
    trangThai BIT NOT NULL DEFAULT 1,
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (ngayKetThuc >= ngayBatDau)
);
GO

-- Hóa đơn thanh toán
CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY CHECK (maHoaDon LIKE 'HD-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maLichHen VARCHAR(20),
    maPhieuDichVu VARCHAR(20),
    maNhanVien VARCHAR(20) NOT NULL,
    maKhachHang VARCHAR(20),
    ngayLap DATE NOT NULL,
    hinhThucThanhToan NVARCHAR(30) CHECK (hinhThucThanhToan IN (N'Tiền mặt', N'Chuyển khoản', N'POS')),
    trangThaiThanhToan NVARCHAR(30) NOT NULL DEFAULT N'Chưa thanh toán' 
        CHECK (trangThaiThanhToan IN (N'Chưa thanh toán', N'Đã thanh toán', N'Đã hủy')),
    maKhuyenMai VARCHAR(20),
    tongTien DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tongTien >= 0),
    CONSTRAINT FK_HoaDon_LichHen FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen),
    CONSTRAINT FK_HoaDon_PhieuDichVu FOREIGN KEY (maPhieuDichVu) REFERENCES PhieuDichVu(maPhieuDichVu),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai),
    CONSTRAINT CK_HoaDon_HinhThucThanhToan CHECK (
        (trangThaiThanhToan = N'Đã thanh toán' AND hinhThucThanhToan IS NOT NULL) OR
        (trangThaiThanhToan <> N'Đã thanh toán')
    )
);
GO

-- Chi tiết hóa đơn: Sản phẩm
CREATE TABLE ChiTietHoaDon_SanPham (
    maHoaDon VARCHAR(20) NOT NULL,
    maSanPham VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL CHECK (soLuong > 0),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    PRIMARY KEY (maHoaDon, maSanPham),
    CONSTRAINT FK_CTHDSanPham_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_CTHDSanPham_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
);
GO

-- Chi tiết hóa đơn: Dịch vụ
CREATE TABLE ChiTietHoaDon_DichVu (
    maHoaDon VARCHAR(20) NOT NULL,
    maDichVu VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL DEFAULT 1 CHECK (soLuong = 1),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    PRIMARY KEY (maHoaDon, maDichVu),
    CONSTRAINT FK_CTHDDichVu_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_CTHDDichVu_DichVu FOREIGN KEY (maDichVu) REFERENCES DichVuChamSoc(maDichVu)
);
GO

-- Phiếu đổi trả
CREATE TABLE PhieuDoiTra (
    maPhieuDoiTra VARCHAR(20) PRIMARY KEY CHECK (maPhieuDoiTra LIKE 'PDT-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]-[0-9][0-9][0-9][0-9][0-9][0-9]'),
    maHoaDon VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
    maKhachHang VARCHAR(20) NOT NULL,
    ngayLap DATE NOT NULL,
    lyDo NVARCHAR(255) NOT NULL CHECK (LEN(LTRIM(RTRIM(lyDo))) > 0),
    soLuong INT NOT NULL CHECK (soLuong > 0),
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xử lý' 
        CHECK (trangThai IN (N'Chờ xử lý', N'Đã hoàn tiền', N'Từ chối')),
    CONSTRAINT FK_PhieuDoiTra_HoaDon FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    CONSTRAINT FK_PhieuDoiTra_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_PhieuDoiTra_KhachHang FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang)
);
GO

-- Chi tiết phiếu đổi trả
CREATE TABLE ChiTietPhieuDoiTra (
    maPhieuDoiTra VARCHAR(20) NOT NULL,
    maHoaDon VARCHAR(20) NOT NULL,
    maSanPham VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL CHECK (soLuong > 0),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    PRIMARY KEY (maPhieuDoiTra, maSanPham),
    CONSTRAINT FK_CTPhieuDoiTra_PhieuDoiTra FOREIGN KEY (maPhieuDoiTra) REFERENCES PhieuDoiTra(maPhieuDoiTra),
    CONSTRAINT FK_CTPhieuDoiTra_ChiTietHDSanPham FOREIGN KEY (maHoaDon, maSanPham) REFERENCES ChiTietHoaDon_SanPham(maHoaDon, maSanPham)
);
GO


-- =======================================================
-- DỮ LIỆU MẪU
-- =======================================================

-- 1. Khách hàng & Thú cưng
INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, email, diemTichLuy, diaChi, ghiChu)
VALUES 
('KH-20261005-000001', N'Nguyễn Thu Trang', '0912345678', 'trang.nguyen@gmail.com', 120, N'12 Nguyễn Văn Bảo, Gò Vấp', N'Khách quen, hay đặt lịch cuối tuần'),
('KH-20261005-000002', N'Lê Hoàng Nam',     '0923456789', 'nam.le@gmail.com', 50, N'45 Lê Đức Thọ, Gò Vấp', N'Nuôi 2 bé chó Poodle'),
('KH-20261005-000003', N'Võ Thị Mai',       '0934567890', 'mai.vo@gmail.com', 0, N'78 Quang Trung, Gò Vấp', N'Khách mới');

INSERT INTO ThuCung (maThuCung, maKhachHang, tenThuCung, loaiThuCung, giongLoai, canNang, ngaySinh, diUng, gioiTinh)
VALUES 
('TC-20261005-000001', 'KH-20261005-000001', N'Mochi', N'Mèo', N'Mèo Anh lông ngắn', 4.50, '2023-04-10', N'Da nhạy cảm, dùng sữa tắm không mùi', N'Cái'),
('TC-20261005-000002', 'KH-20261005-000002', N'Bông',  N'Chó', N'Poodle Tiny', 3.20, '2022-11-05', N'Không có', N'Đực'),
('TC-20261005-000003', 'KH-20261005-000002', N'Lu',    N'Chó', N'Poodle Standard', 8.50, '2021-08-15', N'Nhát nước, cần tắm nhẹ nhàng', N'Đực');

-- 2. Nhân viên, Tài khoản & Ca trực
INSERT INTO NhanVien (maNhanVien, hoTen, soDienThoai, chucVu, ngaySinh, gioiTinh, trangThai)
VALUES 
('NV-20261005-000000', N'Quản trị hệ thống', '0909000000', N'Quản lý', '1990-01-01', 1, N'Đang làm'),
('NV-20261005-000001', N'Nguyễn Thị Lan',    '0901234567', N'Quản lý', '1985-05-15', 0, N'Đang làm'),
('NV-20261005-000002', N'Trần Văn Minh',    '0902345678', N'Bán hàng', '1998-08-20', 1, N'Đang làm'),
('NV-20261005-000003', N'Phạm Thị Hoa',     '0903456789', N'Chăm sóc', '1995-12-10', 0, N'Đang làm');

INSERT INTO TaiKhoan (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, phanQuyen, trangThai)
VALUES 
('TK-20261005-000000', 'NV-20261005-000000', 'admin',   'admin123', N'Admin', 1),
('TK-20261005-000001', 'NV-20261005-000001', 'quanly',  '123456',   N'Quản lý', 1),
('TK-20261005-000002', 'NV-20261005-000002', 'banhang', '123456',   N'Bán hàng', 1),
('TK-20261005-000003', 'NV-20261005-000003', 'chamsoc', '123456',   N'Chăm sóc', 1);

INSERT INTO Ca (maCa, tenCa, gioBatDau, gioKetThuc, trangThai)
VALUES 
('CA-20261005-000001', N'Ca Sáng',  '08:00:00', '12:00:00', N'Áp dụng'),
('CA-20261005-000002', N'Ca Chiều', '13:00:00', '17:00:00', N'Áp dụng'),
('CA-20261005-000003', N'Ca Tối',   '18:00:00', '21:00:00', N'Áp dụng');

INSERT INTO PhanCa (maPhanCa, maNhanVien, maCa, ngayLam, trangThai)
VALUES 
('PC-20261005-000001', 'NV-20261005-000002', 'CA-20261005-000001', '2026-10-06', N'Đã phân công'),
('PC-20261005-000002', 'NV-20261005-000003', 'CA-20261005-000001', '2026-10-06', N'Đã phân công');

-- 3. Dịch vụ & Lịch hẹn
INSERT INTO DichVuChamSoc (maDichVu, tenDichVu, moTa, thoiLuong, trangThai)
VALUES 
('DV-20261005-000001', N'Tắm vệ sinh',           N'Tắm sấy, chải lông, vắt tuyến hôi', 40, 1),
('DV-20261005-000002', N'Grooming tạo kiểu',     N'Tắm, sấy và cắt tỉa tạo kiểu thẩm mỹ', 90, 1),
('DV-20261005-000003', N'Cắt tỉa lông cơ bản',   N'Cắt tỉa gọn gàng lông thừa, lông chân', 60, 1),
('DV-20261005-000004', N'Cắt mài móng',          N'Bấm móng và dũa móng an toàn', 15, 1),
('DV-20261005-000005', N'Vệ sinh tai',           N'Nhổ lông tai và nhỏ thuốc khử khuẩn', 15, 1);

INSERT INTO BangGiaDichVu (maBangGia, maDichVu, gia, phanLoaiKichThuoc, ngayApDung)
VALUES 
('BG-20261005-000001', 'DV-20261005-000001', 80000,  N'Nhỏ', '2026-01-01'),
('BG-20261005-000002', 'DV-20261005-000001', 120000, N'Vừa', '2026-01-01'),
('BG-20261005-000003', 'DV-20261005-000001', 180000, N'Lớn', '2026-01-01'),
('BG-20261005-000004', 'DV-20261005-000002', 150000, N'Nhỏ', '2026-01-01'),
('BG-20261005-000005', 'DV-20261005-000002', 220000, N'Vừa', '2026-01-01'),
('BG-20261005-000006', 'DV-20261005-000002', 320000, N'Lớn', '2026-01-01'),
('BG-20261005-000007', 'DV-20261005-000003', 100000, N'Nhỏ', '2026-01-01'),
('BG-20261005-000008', 'DV-20261005-000003', 150000, N'Vừa', '2026-01-01'),
('BG-20261005-000009', 'DV-20261005-000003', 220000, N'Lớn', '2026-01-01'),
('BG-20261005-000010', 'DV-20261005-000004', 30000,  N'Nhỏ', '2026-01-01'),
('BG-20261005-000011', 'DV-20261005-000004', 30000,  N'Vừa', '2026-01-01'),
('BG-20261005-000012', 'DV-20261005-000004', 40000,  N'Lớn', '2026-01-01'),
('BG-20261005-000013', 'DV-20261005-000005', 30000,  N'Nhỏ', '2026-01-01'),
('BG-20261005-000014', 'DV-20261005-000005', 30000,  N'Vừa', '2026-01-01'),
('BG-20261005-000015', 'DV-20261005-000005', 40000,  N'Lớn', '2026-01-01');

INSERT INTO LichHen (maLichHen, maKhachHang, maThuCung, maNhanVien, ngayHen, gioHen, trangThai, ghiChu)
VALUES
('LH-20261005-000001', 'KH-20261005-000001', 'TC-20261005-000001', 'NV-20261005-000003', '2026-10-06', '09:00:00', N'Đã xác nhận', N'Bé Mochi đặt lịch tắm');

INSERT INTO ChiTietLichHen (maLichHen, maDichVu, canNangThuCung, donGia)
VALUES
('LH-20261005-000001', 'DV-20261005-000001', 4.50, 80000);

INSERT INTO PhieuDichVu (maPhieuDichVu, maKhachHang, maNhanVien, maLichHen, ngayTiepNhan, gioTiepNhan, trangThai, ghiChu)
VALUES
('PDV-20261005-000001', 'KH-20261005-000001', 'NV-20261005-000003', 'LH-20261005-000001', '2026-10-06', '08:55:00', N'Đang thực hiện', N'Tiếp nhận bé Mochi từ lịch hẹn');

INSERT INTO ChiTietPhieuDichVu (maPhieuDichVu, maThuCung, maDichVu, donGia)
VALUES
('PDV-20261005-000001', 'TC-20261005-000001', 'DV-20261005-000001', 80000);

-- 4. Hàng hóa, Nhập kho & Bán lẻ
INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, soDienThoai, diaChi)
VALUES 
('NCC-20261005-000001', N'Công ty CP Thức Ăn Thú Cưng Royal Canin VN', '0283812345', N'KCN Tân Bình, TP.HCM'),
('NCC-20261005-000002', N'Nhà phân phối Phụ kiện thú cưng PetCare',     '0283898765', N'Quận 10, TP.HCM');

INSERT INTO SanPham (maSanPham, maNhaCungCap, tenSanPham, maVach, giaBan, soLuongTon, nguongTonToiThieu, donViTinh, trangThai)
VALUES 
('SP-20261005-000001', 'NCC-20261005-000001', N'Hạt Royal Canin Indoor 2kg', '893123456701', 350000, 45, 10, N'Gói', 1),
('SP-20261005-000002', 'NCC-20261005-000001', N'Pate Whiskas Cá Biển 85g',   '893123456702', 20000,  120, 20, N'Gói', 1),
('SP-20261005-000003', 'NCC-20261005-000002', N'Cát vệ sinh Cature gỗ 6L',   '893123456703', 140000, 30, 10, N'Túi', 1),
('SP-20261005-000004', 'NCC-20261005-000002', N'Dây dắt kèm yếm ngực size M', '893123456704', 85000,  15, 5,  N'Cái', 1),
('SP-20261005-000005', 'NCC-20261005-000002', N'Sữa tắm thú cưng SOS 530ml', '893123456705', 120000, 25, 8,  N'Chai', 1);

INSERT INTO DonNhapHang (maDonHang, maNhaCungCap, maNhanVien, ngayLap, tongTien, trangThai)
VALUES
('DH-20261005-000001', 'NCC-20261005-000001', 'NV-20261005-000001', '2026-10-02', 17500000, N'Hoàn tất');

INSERT INTO ChiTietDonNhap (maDonHang, maSanPham, soLuong, donGia)
VALUES
('DH-20261005-000001', 'SP-20261005-000001', 50, 350000);

INSERT INTO KhuyenMai (maKhuyenMai, tenChuongTrinh, phanTramGiam, ngayBatDau, ngayKetThuc, trangThai)
VALUES 
('KM-20261005-000001', N'Ưu đãi Khai trương Spa', 10.00, '2026-10-01', '2026-10-31', 1),
('KM-20261005-000002', N'Tri ân Thành viên Gold',  15.00, '2026-10-01', '2026-12-31', 1);

INSERT INTO HoaDon (maHoaDon, maLichHen, maPhieuDichVu, maNhanVien, maKhachHang, ngayLap, hinhThucThanhToan, trangThaiThanhToan, maKhuyenMai, tongTien)
VALUES 
('HD-20261005-000001', NULL, NULL, 'NV-20261005-000002', 'KH-20261005-000001', '2026-10-05', N'Tiền mặt', N'Đã thanh toán', NULL, 350000);

INSERT INTO ChiTietHoaDon_SanPham (maHoaDon, maSanPham, soLuong, donGia)
VALUES 
('HD-20261005-000001', 'SP-20261005-000001', 1, 350000);
GO
