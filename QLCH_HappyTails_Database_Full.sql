USE [master]
GO
/****** Object:  Database [QLCH_HappyTails]    Script Date: 10/11/2026 12:22:31 PM ******/
CREATE DATABASE [QLCH_HappyTails]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'QLCH_HappyTails', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL17.SQLEXPRESS\MSSQL\DATA\QLCH_HappyTails.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'QLCH_HappyTails_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL17.SQLEXPRESS\MSSQL\DATA\QLCH_HappyTails_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO
ALTER DATABASE [QLCH_HappyTails] SET COMPATIBILITY_LEVEL = 170
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [QLCH_HappyTails].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [QLCH_HappyTails] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET ARITHABORT OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [QLCH_HappyTails] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [QLCH_HappyTails] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET  ENABLE_BROKER 
GO
ALTER DATABASE [QLCH_HappyTails] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [QLCH_HappyTails] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET RECOVERY SIMPLE 
GO
ALTER DATABASE [QLCH_HappyTails] SET  MULTI_USER 
GO
ALTER DATABASE [QLCH_HappyTails] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [QLCH_HappyTails] SET DB_CHAINING OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [QLCH_HappyTails] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [QLCH_HappyTails] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [QLCH_HappyTails] SET OPTIMIZED_LOCKING = OFF 
GO
ALTER DATABASE [QLCH_HappyTails] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
ALTER DATABASE [QLCH_HappyTails] SET QUERY_STORE = ON
GO
ALTER DATABASE [QLCH_HappyTails] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [QLCH_HappyTails]
GO
/****** Object:  User [petshop]    Script Date: 10/11/2026 12:22:31 PM ******/
CREATE USER [petshop] FOR LOGIN [petshop] WITH DEFAULT_SCHEMA=[dbo]
GO
ALTER ROLE [db_owner] ADD MEMBER [petshop]
GO
/****** Object:  Table [dbo].[BangGiaDichVu]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[BangGiaDichVu](
	[maBangGia] [varchar](30) NOT NULL,
	[maDichVu] [varchar](30) NOT NULL,
	[gia] [decimal](18, 2) NOT NULL,
	[phanLoaiKichThuoc] [nvarchar](20) NOT NULL,
	[ngayApDung] [date] NOT NULL,
	[ngayHetHieuLuc] [date] NULL,
PRIMARY KEY CLUSTERED 
(
	[maBangGia] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_BangGia_DichVu_Bac_Ngay] UNIQUE NONCLUSTERED 
(
	[maDichVu] ASC,
	[phanLoaiKichThuoc] ASC,
	[ngayApDung] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Ca]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Ca](
	[maCa] [varchar](30) NOT NULL,
	[tenCa] [nvarchar](50) NOT NULL,
	[gioBatDau] [time](7) NOT NULL,
	[gioKetThuc] [time](7) NOT NULL,
	[trangThai] [nvarchar](30) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maCa] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietDonHang]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietDonHang](
	[maDonHang] [varchar](30) NOT NULL,
	[maSanPham] [varchar](30) NOT NULL,
	[soLuong] [int] NOT NULL,
	[donGia] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maDonHang] ASC,
	[maSanPham] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietHoaDon_DichVu]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietHoaDon_DichVu](
	[maHoaDon] [varchar](30) NOT NULL,
	[maDichVu] [varchar](30) NOT NULL,
	[soLuong] [int] NOT NULL,
	[donGia] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maHoaDon] ASC,
	[maDichVu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietHoaDon_SanPham]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietHoaDon_SanPham](
	[maHoaDon] [varchar](30) NOT NULL,
	[maSanPham] [varchar](30) NOT NULL,
	[soLuong] [int] NOT NULL,
	[donGia] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maHoaDon] ASC,
	[maSanPham] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietLichHen]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietLichHen](
	[maLichHen] [varchar](30) NOT NULL,
	[maDichVu] [varchar](30) NOT NULL,
	[canNangThuCung] [decimal](5, 2) NOT NULL,
	[donGia] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maLichHen] ASC,
	[maDichVu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietPhieuDoiTra]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietPhieuDoiTra](
	[maPhieuDoiTra] [varchar](30) NOT NULL,
	[maSanPham] [varchar](30) NOT NULL,
	[soLuong] [int] NOT NULL,
	[donGia] [decimal](18, 2) NOT NULL,
	[maHoaDon] [varchar](30) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maPhieuDoiTra] ASC,
	[maSanPham] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DichVuChamSoc]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DichVuChamSoc](
	[maDichVu] [varchar](30) NOT NULL,
	[tenDichVu] [nvarchar](100) NOT NULL,
	[moTa] [nvarchar](255) NULL,
	[thoiLuong] [int] NOT NULL,
	[trangThai] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maDichVu] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DonHang]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DonHang](
	[maDonHang] [varchar](30) NOT NULL,
	[maNhaCungCap] [varchar](30) NOT NULL,
	[maNhanVien] [varchar](30) NOT NULL,
	[ngayLap] [date] NOT NULL,
	[tongTien] [decimal](18, 2) NOT NULL,
	[trangThai] [nvarchar](30) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maDonHang] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HoaDon]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HoaDon](
	[maHoaDon] [varchar](30) NOT NULL,
	[maLichHen] [varchar](30) NULL,
	[maNhanVien] [varchar](30) NOT NULL,
	[maKhachHang] [varchar](30) NULL,
	[ngayLap] [datetime2](7) NOT NULL,
	[hinhThucThanhToan] [nvarchar](30) NULL,
	[trangThaiThanhToan] [nvarchar](30) NOT NULL,
	[maKhuyenMai] [varchar](30) NULL,
	[tienGiam] [decimal](18, 2) NOT NULL,
	[tongTien] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maHoaDon] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[KhachHang]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[KhachHang](
	[maKhachHang] [varchar](30) NOT NULL,
	[hoTen] [nvarchar](100) NOT NULL,
	[soDienThoai] [varchar](10) NOT NULL,
	[email] [varchar](100) NULL,
	[diemTichLuy] [int] NOT NULL,
	[diaChi] [nvarchar](255) NULL,
	[ghiChu] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[maKhachHang] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[soDienThoai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[KhuyenMai]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[KhuyenMai](
	[maKhuyenMai] [varchar](30) NOT NULL,
	[tenChuongTrinh] [nvarchar](150) NOT NULL,
	[phanTramGiam] [decimal](5, 2) NOT NULL,
	[ngayBatDau] [date] NOT NULL,
	[ngayKetThuc] [date] NOT NULL,
	[trangThai] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maKhuyenMai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[LichHen]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[LichHen](
	[maLichHen] [varchar](30) NOT NULL,
	[maKhachHang] [varchar](30) NOT NULL,
	[maThuCung] [varchar](30) NOT NULL,
	[maNhanVien] [varchar](30) NOT NULL,
	[ngayHen] [date] NOT NULL,
	[gioHen] [time](7) NOT NULL,
	[trangThai] [nvarchar](30) NOT NULL,
	[ghiChu] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[maLichHen] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[NhaCungCap]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NhaCungCap](
	[maNhaCungCap] [varchar](30) NOT NULL,
	[tenNhaCungCap] [nvarchar](150) NOT NULL,
	[soDienThoai] [varchar](15) NOT NULL,
	[diaChi] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[maNhaCungCap] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[NhanVien]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NhanVien](
	[maNhanVien] [varchar](30) NOT NULL,
	[hoTen] [nvarchar](100) NOT NULL,
	[soDienThoai] [varchar](10) NOT NULL,
	[chucVu] [nvarchar](50) NOT NULL,
	[ngaySinh] [date] NOT NULL,
	[gioiTinh] [bit] NOT NULL,
	[trangThai] [nvarchar](30) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maNhanVien] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[soDienThoai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[NhatKyThaoTac]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NhatKyThaoTac](
	[maNhatKy] [varchar](36) NOT NULL,
	[maTaiKhoan] [varchar](30) NOT NULL,
	[hanhDong] [nvarchar](100) NOT NULL,
	[bangDoiTuong] [nvarchar](128) NOT NULL,
	[maDoiTuong] [varchar](100) NULL,
	[thoiGian] [datetime2](7) NOT NULL,
	[ghiChu] [nvarchar](1000) NULL,
 CONSTRAINT [PK_NhatKyThaoTac] PRIMARY KEY CLUSTERED 
(
	[maNhatKy] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PhanCa]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PhanCa](
	[maPhanCa] [varchar](30) NOT NULL,
	[maNhanVien] [varchar](30) NOT NULL,
	[maCa] [varchar](30) NOT NULL,
	[ngayLam] [date] NOT NULL,
	[trangThai] [nvarchar](30) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maPhanCa] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PhieuDoiTra]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PhieuDoiTra](
	[maPhieuDoiTra] [varchar](30) NOT NULL,
	[maHoaDon] [varchar](30) NOT NULL,
	[maNhanVien] [varchar](30) NOT NULL,
	[maKhachHang] [varchar](30) NOT NULL,
	[ngayLap] [date] NOT NULL,
	[lyDo] [nvarchar](255) NOT NULL,
	[trangThai] [nvarchar](30) NOT NULL,
	[tongTienHoan] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maPhieuDoiTra] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[SanPham]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[SanPham](
	[maSanPham] [varchar](30) NOT NULL,
	[maNhaCungCap] [varchar](30) NULL,
	[tenSanPham] [nvarchar](150) NOT NULL,
	[maVach] [varchar](50) NULL,
	[giaBan] [decimal](18, 2) NOT NULL,
	[soLuongTon] [int] NOT NULL,
	[donViTinh] [nvarchar](30) NOT NULL,
	[trangThai] [bit] NOT NULL,
	[nguongTonToiThieu] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maSanPham] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[maVach] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TaiKhoan]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TaiKhoan](
	[maTaiKhoan] [varchar](30) NOT NULL,
	[maNhanVien] [varchar](30) NOT NULL,
	[tenDangNhap] [varchar](50) NOT NULL,
	[matKhau] [varchar](255) NOT NULL,
	[phanQuyen] [nvarchar](30) NOT NULL,
	[trangThai] [bit] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[maTaiKhoan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[tenDangNhap] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[maNhanVien] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ThuCung]    Script Date: 10/11/2026 12:22:31 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ThuCung](
	[maThuCung] [varchar](30) NOT NULL,
	[maKhachHang] [varchar](30) NOT NULL,
	[tenThuCung] [nvarchar](100) NOT NULL,
	[loaiThuCung] [nvarchar](50) NULL,
	[giongLoai] [nvarchar](100) NULL,
	[canNang] [decimal](5, 2) NOT NULL,
	[ngaySinh] [date] NULL,
	[diUng] [nvarchar](255) NULL,
	[gioiTinh] [nvarchar](10) NULL,
PRIMARY KEY CLUSTERED 
(
	[maThuCung] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
 CONSTRAINT [UQ_ThuCung_Ma_KhachHang] UNIQUE NONCLUSTERED 
(
	[maThuCung] ASC,
	[maKhachHang] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_NhatKyThaoTac_TaiKhoan_ThoiGian]    Script Date: 10/11/2026 12:22:31 PM ******/
CREATE NONCLUSTERED INDEX [IX_NhatKyThaoTac_TaiKhoan_ThoiGian] ON [dbo].[NhatKyThaoTac]
(
	[maTaiKhoan] ASC,
	[thoiGian] DESC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_PhanCa_NhanVien_Ngay_Ca]    Script Date: 10/11/2026 12:22:31 PM ******/
CREATE UNIQUE NONCLUSTERED INDEX [UQ_PhanCa_NhanVien_Ngay_Ca] ON [dbo].[PhanCa]
(
	[maNhanVien] ASC,
	[maCa] ASC,
	[ngayLam] ASC
)
WHERE ([trangThai]=N'Đã phân công')
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Ca] ADD  DEFAULT (N'Áp dụng') FOR [trangThai]
GO
ALTER TABLE [dbo].[ChiTietHoaDon_DichVu] ADD  DEFAULT ((1)) FOR [soLuong]
GO
ALTER TABLE [dbo].[DichVuChamSoc] ADD  DEFAULT ((30)) FOR [thoiLuong]
GO
ALTER TABLE [dbo].[DichVuChamSoc] ADD  DEFAULT ((1)) FOR [trangThai]
GO
ALTER TABLE [dbo].[DonHang] ADD  DEFAULT ((0)) FOR [tongTien]
GO
ALTER TABLE [dbo].[DonHang] ADD  DEFAULT (N'Chờ nhận hàng') FOR [trangThai]
GO
ALTER TABLE [dbo].[HoaDon] ADD  DEFAULT (N'Chưa thanh toán') FOR [trangThaiThanhToan]
GO
ALTER TABLE [dbo].[HoaDon] ADD  DEFAULT ((0)) FOR [tienGiam]
GO
ALTER TABLE [dbo].[HoaDon] ADD  DEFAULT ((0)) FOR [tongTien]
GO
ALTER TABLE [dbo].[KhachHang] ADD  DEFAULT ((0)) FOR [diemTichLuy]
GO
ALTER TABLE [dbo].[KhuyenMai] ADD  DEFAULT ((1)) FOR [trangThai]
GO
ALTER TABLE [dbo].[LichHen] ADD  DEFAULT (N'Đã đặt') FOR [trangThai]
GO
ALTER TABLE [dbo].[NhanVien] ADD  DEFAULT (N'Đang làm') FOR [trangThai]
GO
ALTER TABLE [dbo].[NhatKyThaoTac] ADD  CONSTRAINT [DF_NhatKyThaoTac_maNhatKy]  DEFAULT (CONVERT([varchar](36),newid())) FOR [maNhatKy]
GO
ALTER TABLE [dbo].[NhatKyThaoTac] ADD  CONSTRAINT [DF_NhatKyThaoTac_thoiGian]  DEFAULT (sysdatetime()) FOR [thoiGian]
GO
ALTER TABLE [dbo].[PhanCa] ADD  DEFAULT (N'Đã phân công') FOR [trangThai]
GO
ALTER TABLE [dbo].[PhieuDoiTra] ADD  DEFAULT (N'Chờ xử lý') FOR [trangThai]
GO
ALTER TABLE [dbo].[PhieuDoiTra] ADD  DEFAULT ((0)) FOR [tongTienHoan]
GO
ALTER TABLE [dbo].[SanPham] ADD  DEFAULT ((0)) FOR [giaBan]
GO
ALTER TABLE [dbo].[SanPham] ADD  DEFAULT ((0)) FOR [soLuongTon]
GO
ALTER TABLE [dbo].[SanPham] ADD  DEFAULT ((1)) FOR [trangThai]
GO
ALTER TABLE [dbo].[SanPham] ADD  CONSTRAINT [DF_SanPham_NguongTon]  DEFAULT ((10)) FOR [nguongTonToiThieu]
GO
ALTER TABLE [dbo].[TaiKhoan] ADD  DEFAULT ((1)) FOR [trangThai]
GO
ALTER TABLE [dbo].[BangGiaDichVu]  WITH CHECK ADD  CONSTRAINT [FK_BangGia_DichVu] FOREIGN KEY([maDichVu])
REFERENCES [dbo].[DichVuChamSoc] ([maDichVu])
GO
ALTER TABLE [dbo].[BangGiaDichVu] CHECK CONSTRAINT [FK_BangGia_DichVu]
GO
ALTER TABLE [dbo].[ChiTietDonHang]  WITH CHECK ADD  CONSTRAINT [FK_CTDonHang_DonHang] FOREIGN KEY([maDonHang])
REFERENCES [dbo].[DonHang] ([maDonHang])
GO
ALTER TABLE [dbo].[ChiTietDonHang] CHECK CONSTRAINT [FK_CTDonHang_DonHang]
GO
ALTER TABLE [dbo].[ChiTietDonHang]  WITH CHECK ADD  CONSTRAINT [FK_CTDonHang_SanPham] FOREIGN KEY([maSanPham])
REFERENCES [dbo].[SanPham] ([maSanPham])
GO
ALTER TABLE [dbo].[ChiTietDonHang] CHECK CONSTRAINT [FK_CTDonHang_SanPham]
GO
ALTER TABLE [dbo].[ChiTietHoaDon_DichVu]  WITH CHECK ADD  CONSTRAINT [FK_CTHDDichVu_DichVu] FOREIGN KEY([maDichVu])
REFERENCES [dbo].[DichVuChamSoc] ([maDichVu])
GO
ALTER TABLE [dbo].[ChiTietHoaDon_DichVu] CHECK CONSTRAINT [FK_CTHDDichVu_DichVu]
GO
ALTER TABLE [dbo].[ChiTietHoaDon_DichVu]  WITH CHECK ADD  CONSTRAINT [FK_CTHDDichVu_HoaDon] FOREIGN KEY([maHoaDon])
REFERENCES [dbo].[HoaDon] ([maHoaDon])
GO
ALTER TABLE [dbo].[ChiTietHoaDon_DichVu] CHECK CONSTRAINT [FK_CTHDDichVu_HoaDon]
GO
ALTER TABLE [dbo].[ChiTietHoaDon_SanPham]  WITH CHECK ADD  CONSTRAINT [FK_CTHDSanPham_HoaDon] FOREIGN KEY([maHoaDon])
REFERENCES [dbo].[HoaDon] ([maHoaDon])
GO
ALTER TABLE [dbo].[ChiTietHoaDon_SanPham] CHECK CONSTRAINT [FK_CTHDSanPham_HoaDon]
GO
ALTER TABLE [dbo].[ChiTietHoaDon_SanPham]  WITH CHECK ADD  CONSTRAINT [FK_CTHDSanPham_SanPham] FOREIGN KEY([maSanPham])
REFERENCES [dbo].[SanPham] ([maSanPham])
GO
ALTER TABLE [dbo].[ChiTietHoaDon_SanPham] CHECK CONSTRAINT [FK_CTHDSanPham_SanPham]
GO
ALTER TABLE [dbo].[ChiTietLichHen]  WITH CHECK ADD  CONSTRAINT [FK_CTLichHen_DichVu] FOREIGN KEY([maDichVu])
REFERENCES [dbo].[DichVuChamSoc] ([maDichVu])
GO
ALTER TABLE [dbo].[ChiTietLichHen] CHECK CONSTRAINT [FK_CTLichHen_DichVu]
GO
ALTER TABLE [dbo].[ChiTietLichHen]  WITH CHECK ADD  CONSTRAINT [FK_CTLichHen_LichHen] FOREIGN KEY([maLichHen])
REFERENCES [dbo].[LichHen] ([maLichHen])
GO
ALTER TABLE [dbo].[ChiTietLichHen] CHECK CONSTRAINT [FK_CTLichHen_LichHen]
GO
ALTER TABLE [dbo].[ChiTietPhieuDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_CTPhieuDoiTra_ChiTietHDSanPham] FOREIGN KEY([maHoaDon], [maSanPham])
REFERENCES [dbo].[ChiTietHoaDon_SanPham] ([maHoaDon], [maSanPham])
GO
ALTER TABLE [dbo].[ChiTietPhieuDoiTra] CHECK CONSTRAINT [FK_CTPhieuDoiTra_ChiTietHDSanPham]
GO
ALTER TABLE [dbo].[ChiTietPhieuDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_CTPhieuDoiTra_PhieuDoiTra] FOREIGN KEY([maPhieuDoiTra])
REFERENCES [dbo].[PhieuDoiTra] ([maPhieuDoiTra])
GO
ALTER TABLE [dbo].[ChiTietPhieuDoiTra] CHECK CONSTRAINT [FK_CTPhieuDoiTra_PhieuDoiTra]
GO
ALTER TABLE [dbo].[ChiTietPhieuDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_CTPhieuDoiTra_SanPham] FOREIGN KEY([maSanPham])
REFERENCES [dbo].[SanPham] ([maSanPham])
GO
ALTER TABLE [dbo].[ChiTietPhieuDoiTra] CHECK CONSTRAINT [FK_CTPhieuDoiTra_SanPham]
GO
ALTER TABLE [dbo].[DonHang]  WITH CHECK ADD  CONSTRAINT [FK_DonHang_NhaCungCap] FOREIGN KEY([maNhaCungCap])
REFERENCES [dbo].[NhaCungCap] ([maNhaCungCap])
GO
ALTER TABLE [dbo].[DonHang] CHECK CONSTRAINT [FK_DonHang_NhaCungCap]
GO
ALTER TABLE [dbo].[DonHang]  WITH CHECK ADD  CONSTRAINT [FK_DonHang_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[DonHang] CHECK CONSTRAINT [FK_DonHang_NhanVien]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_KhachHang] FOREIGN KEY([maKhachHang])
REFERENCES [dbo].[KhachHang] ([maKhachHang])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_KhachHang]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_KhuyenMai] FOREIGN KEY([maKhuyenMai])
REFERENCES [dbo].[KhuyenMai] ([maKhuyenMai])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_KhuyenMai]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_LichHen] FOREIGN KEY([maLichHen])
REFERENCES [dbo].[LichHen] ([maLichHen])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_LichHen]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_NhanVien]
GO
ALTER TABLE [dbo].[LichHen]  WITH CHECK ADD  CONSTRAINT [FK_LichHen_KhachHang] FOREIGN KEY([maKhachHang])
REFERENCES [dbo].[KhachHang] ([maKhachHang])
GO
ALTER TABLE [dbo].[LichHen] CHECK CONSTRAINT [FK_LichHen_KhachHang]
GO
ALTER TABLE [dbo].[LichHen]  WITH CHECK ADD  CONSTRAINT [FK_LichHen_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[LichHen] CHECK CONSTRAINT [FK_LichHen_NhanVien]
GO
ALTER TABLE [dbo].[LichHen]  WITH CHECK ADD  CONSTRAINT [FK_LichHen_ThuCung] FOREIGN KEY([maThuCung], [maKhachHang])
REFERENCES [dbo].[ThuCung] ([maThuCung], [maKhachHang])
GO
ALTER TABLE [dbo].[LichHen] CHECK CONSTRAINT [FK_LichHen_ThuCung]
GO
ALTER TABLE [dbo].[NhatKyThaoTac]  WITH CHECK ADD  CONSTRAINT [FK_NhatKyThaoTac_TaiKhoan] FOREIGN KEY([maTaiKhoan])
REFERENCES [dbo].[TaiKhoan] ([maTaiKhoan])
GO
ALTER TABLE [dbo].[NhatKyThaoTac] CHECK CONSTRAINT [FK_NhatKyThaoTac_TaiKhoan]
GO
ALTER TABLE [dbo].[PhanCa]  WITH CHECK ADD  CONSTRAINT [FK_PhanCa_Ca] FOREIGN KEY([maCa])
REFERENCES [dbo].[Ca] ([maCa])
GO
ALTER TABLE [dbo].[PhanCa] CHECK CONSTRAINT [FK_PhanCa_Ca]
GO
ALTER TABLE [dbo].[PhanCa]  WITH CHECK ADD  CONSTRAINT [FK_PhanCa_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[PhanCa] CHECK CONSTRAINT [FK_PhanCa_NhanVien]
GO
ALTER TABLE [dbo].[PhieuDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_PhieuDoiTra_HoaDon] FOREIGN KEY([maHoaDon])
REFERENCES [dbo].[HoaDon] ([maHoaDon])
GO
ALTER TABLE [dbo].[PhieuDoiTra] CHECK CONSTRAINT [FK_PhieuDoiTra_HoaDon]
GO
ALTER TABLE [dbo].[PhieuDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_PhieuDoiTra_KhachHang] FOREIGN KEY([maKhachHang])
REFERENCES [dbo].[KhachHang] ([maKhachHang])
GO
ALTER TABLE [dbo].[PhieuDoiTra] CHECK CONSTRAINT [FK_PhieuDoiTra_KhachHang]
GO
ALTER TABLE [dbo].[PhieuDoiTra]  WITH CHECK ADD  CONSTRAINT [FK_PhieuDoiTra_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[PhieuDoiTra] CHECK CONSTRAINT [FK_PhieuDoiTra_NhanVien]
GO
ALTER TABLE [dbo].[SanPham]  WITH CHECK ADD  CONSTRAINT [FK_SanPham_NhaCungCap] FOREIGN KEY([maNhaCungCap])
REFERENCES [dbo].[NhaCungCap] ([maNhaCungCap])
GO
ALTER TABLE [dbo].[SanPham] CHECK CONSTRAINT [FK_SanPham_NhaCungCap]
GO
ALTER TABLE [dbo].[TaiKhoan]  WITH CHECK ADD  CONSTRAINT [FK_TaiKhoan_NhanVien] FOREIGN KEY([maNhanVien])
REFERENCES [dbo].[NhanVien] ([maNhanVien])
GO
ALTER TABLE [dbo].[TaiKhoan] CHECK CONSTRAINT [FK_TaiKhoan_NhanVien]
GO
ALTER TABLE [dbo].[ThuCung]  WITH CHECK ADD  CONSTRAINT [FK_ThuCung_KhachHang] FOREIGN KEY([maKhachHang])
REFERENCES [dbo].[KhachHang] ([maKhachHang])
GO
ALTER TABLE [dbo].[ThuCung] CHECK CONSTRAINT [FK_ThuCung_KhachHang]
GO
ALTER TABLE [dbo].[BangGiaDichVu]  WITH CHECK ADD CHECK  (([phanLoaiKichThuoc]=N'Lớn' OR [phanLoaiKichThuoc]=N'Vừa' OR [phanLoaiKichThuoc]=N'Nhỏ'))
GO
ALTER TABLE [dbo].[BangGiaDichVu]  WITH CHECK ADD CHECK  (([gia]>=(0)))
GO
ALTER TABLE [dbo].[BangGiaDichVu]  WITH CHECK ADD  CONSTRAINT [CK_BangGia_HieuLuc] CHECK  (([ngayHetHieuLuc] IS NULL OR [ngayHetHieuLuc]>=[ngayApDung]))
GO
ALTER TABLE [dbo].[BangGiaDichVu] CHECK CONSTRAINT [CK_BangGia_HieuLuc]
GO
ALTER TABLE [dbo].[Ca]  WITH CHECK ADD CHECK  (([trangThai]=N'Ngừng áp dụng' OR [trangThai]=N'Áp dụng'))
GO
ALTER TABLE [dbo].[Ca]  WITH CHECK ADD  CONSTRAINT [CK_Ca_Gio] CHECK  (([gioKetThuc]>[gioBatDau]))
GO
ALTER TABLE [dbo].[Ca] CHECK CONSTRAINT [CK_Ca_Gio]
GO
ALTER TABLE [dbo].[ChiTietDonHang]  WITH CHECK ADD CHECK  (([donGia]>=(0)))
GO
ALTER TABLE [dbo].[ChiTietDonHang]  WITH CHECK ADD CHECK  (([soLuong]>(0)))
GO
ALTER TABLE [dbo].[ChiTietHoaDon_DichVu]  WITH CHECK ADD CHECK  (([donGia]>=(0)))
GO
ALTER TABLE [dbo].[ChiTietHoaDon_DichVu]  WITH CHECK ADD CHECK  (([soLuong]=(1)))
GO
ALTER TABLE [dbo].[ChiTietHoaDon_SanPham]  WITH CHECK ADD CHECK  (([donGia]>=(0)))
GO
ALTER TABLE [dbo].[ChiTietHoaDon_SanPham]  WITH CHECK ADD CHECK  (([soLuong]>(0)))
GO
ALTER TABLE [dbo].[ChiTietLichHen]  WITH CHECK ADD CHECK  (([canNangThuCung]>(0)))
GO
ALTER TABLE [dbo].[ChiTietLichHen]  WITH CHECK ADD CHECK  (([donGia]>=(0)))
GO
ALTER TABLE [dbo].[ChiTietPhieuDoiTra]  WITH CHECK ADD CHECK  (([donGia]>=(0)))
GO
ALTER TABLE [dbo].[ChiTietPhieuDoiTra]  WITH CHECK ADD CHECK  (([soLuong]>(0)))
GO
ALTER TABLE [dbo].[DichVuChamSoc]  WITH CHECK ADD CHECK  (([thoiLuong]>(0)))
GO
ALTER TABLE [dbo].[DonHang]  WITH CHECK ADD CHECK  (([tongTien]>=(0)))
GO
ALTER TABLE [dbo].[DonHang]  WITH CHECK ADD CHECK  (([trangThai]=N'Đã hủy' OR [trangThai]=N'Hoàn tất' OR [trangThai]=N'Chờ nhận hàng'))
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD CHECK  (([hinhThucThanhToan]=N'POS' OR [hinhThucThanhToan]=N'Chuyển khoản' OR [hinhThucThanhToan]=N'Tiền mặt'))
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD CHECK  (([tienGiam]>=(0)))
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD CHECK  (([tongTien]>=(0)))
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD CHECK  (([trangThaiThanhToan]=N'Đã hủy' OR [trangThaiThanhToan]=N'Đã thanh toán' OR [trangThaiThanhToan]=N'Chưa thanh toán'))
GO
ALTER TABLE [dbo].[KhachHang]  WITH CHECK ADD CHECK  (([diemTichLuy]>=(0)))
GO
ALTER TABLE [dbo].[KhachHang]  WITH CHECK ADD CHECK  ((NOT [soDienThoai] like '%[^0-9]%'))
GO
ALTER TABLE [dbo].[KhuyenMai]  WITH CHECK ADD CHECK  (([phanTramGiam]>(0) AND [phanTramGiam]<=(100)))
GO
ALTER TABLE [dbo].[KhuyenMai]  WITH CHECK ADD  CONSTRAINT [CK_KhuyenMai_Ngay] CHECK  (([ngayKetThuc]>=[ngayBatDau]))
GO
ALTER TABLE [dbo].[KhuyenMai] CHECK CONSTRAINT [CK_KhuyenMai_Ngay]
GO
ALTER TABLE [dbo].[LichHen]  WITH CHECK ADD CHECK  (([trangThai]=N'Đã hủy' OR [trangThai]=N'Hoàn thành' OR [trangThai]=N'Đang thực hiện' OR [trangThai]=N'Đã xác nhận' OR [trangThai]=N'Đã đặt'))
GO
ALTER TABLE [dbo].[NhaCungCap]  WITH CHECK ADD CHECK  ((NOT [soDienThoai] like '%[^0-9]%'))
GO
ALTER TABLE [dbo].[NhanVien]  WITH CHECK ADD CHECK  ((NOT [soDienThoai] like '%[^0-9]%'))
GO
ALTER TABLE [dbo].[NhanVien]  WITH CHECK ADD CHECK  (([trangThai]=N'Đã nghỉ việc' OR [trangThai]=N'Đang làm'))
GO
ALTER TABLE [dbo].[NhanVien]  WITH CHECK ADD  CONSTRAINT [CK_NhanVien_ChucVu] CHECK  (([chucVu]=N'Chăm sóc' OR [chucVu]=N'Bán hàng' OR [chucVu]=N'Quản lý'))
GO
ALTER TABLE [dbo].[NhanVien] CHECK CONSTRAINT [CK_NhanVien_ChucVu]
GO
ALTER TABLE [dbo].[PhanCa]  WITH CHECK ADD CHECK  (([trangThai]=N'Đã hủy' OR [trangThai]=N'Đã phân công'))
GO
ALTER TABLE [dbo].[PhieuDoiTra]  WITH CHECK ADD CHECK  (([tongTienHoan]>=(0)))
GO
ALTER TABLE [dbo].[PhieuDoiTra]  WITH CHECK ADD CHECK  (([trangThai]=N'Từ chối' OR [trangThai]=N'Đã hoàn tiền' OR [trangThai]=N'Chờ xử lý'))
GO
ALTER TABLE [dbo].[SanPham]  WITH CHECK ADD CHECK  (([giaBan]>=(0)))
GO
ALTER TABLE [dbo].[SanPham]  WITH CHECK ADD CHECK  (([soLuongTon]>=(0)))
GO
ALTER TABLE [dbo].[SanPham]  WITH CHECK ADD  CONSTRAINT [CK_SanPham_NguongTon] CHECK  (([nguongTonToiThieu]>=(0)))
GO
ALTER TABLE [dbo].[SanPham] CHECK CONSTRAINT [CK_SanPham_NguongTon]
GO
ALTER TABLE [dbo].[TaiKhoan]  WITH CHECK ADD  CONSTRAINT [CK_TaiKhoan_PhanQuyen] CHECK  (([phanQuyen]=N'Chăm sóc' OR [phanQuyen]=N'Bán hàng' OR [phanQuyen]=N'Quản lý'))
GO
ALTER TABLE [dbo].[TaiKhoan] CHECK CONSTRAINT [CK_TaiKhoan_PhanQuyen]
GO
ALTER TABLE [dbo].[ThuCung]  WITH CHECK ADD CHECK  (([canNang]>(0)))
GO
ALTER TABLE [dbo].[ThuCung]  WITH CHECK ADD CHECK  (([gioiTinh]=N'Cái' OR [gioiTinh]=N'Đực'))
GO
USE [master]
GO
ALTER DATABASE [QLCH_HappyTails] SET  READ_WRITE 
GO
