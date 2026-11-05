# Quản Lý Cửa Hàng Thú Cưng Happy Tails 🐾

> **Đồ án môn học:** Phát triển Ứng dụng (DOANPTUD)  
> **Đơn vị:** Khoa Công nghệ Thông tin - Trường Đại học Công nghiệp TP.HCM (IUH)  
> **Công nghệ:** Java 17, JavaFX, Maven, Microsoft SQL Server  
> **Kiến trúc:** 3-Tier Architecture (GUI - BUS - DAO - Entity) tuân thủ 100% Bộ Chỉ thị Kỹ thuật IUH

---

## 📌 1. Bộ Chỉ Thị Kỹ Thuật Bắt Buộc (IUH)

Dự án được xây dựng và refactor tuân thủ tuyệt đối 7 điều chuẩn mực kỹ thuật của IUH:
1. **Đồng nhất kiểu dữ liệu:** 100% các trường tiền tệ (`donGia`, `giaNhap`, `giaBan`, `tongTien`, `thanhTien`, `tienGiam`, `tienHoan`, `tienBu`...) và cân nặng (`canNang`) sử dụng kiểu nguyên thủy `double`. Tuyệt đối không dùng `BigDecimal`.
2. **Chuẩn hóa OOP & Thuộc tính dẫn xuất:**
   - Khóa ngoại dùng Object Reference (`KhachHang khachHang`, `NhanVien nhanVien`, `SanPham sanPham`, `HoaDon hoaDon`...).
   - Giữ lại Delegate Getters (`getMaKhachHang()`, `getTenKhachHang()`...) để không làm gãy TableView.
   - Thuộc tính dẫn xuất không lưu biến thành viên/setter mà viết thành phương thức tự tính toán (`tinhThanhTien()`, `tinhTienHoan()`, `tinhTienBu()`, `tinhTienGiam()`).
3. **Kiến trúc 3 tầng nghiêm ngặt:**
   - **GUI (`com.petshop.gui`):** Bắt sự kiện, hiển thị, gọi BUS. Không viết SQL, không tính toán tiền trong GUI.
   - **BUS (`com.petshop.bus`):** Xử lý logic, validate, tính toán, gọi DAO. Tuyệt đối 0% import `javafx.*` trong BUS.
   - **DAO (`com.petshop.dao`):** Chỉ truy xuất CSDL SQL Server. Các nghiệp vụ nhiều bảng bắt buộc dùng JDBC Transaction (`setAutoCommit(false)`, `commit()`, `rollback()`).
   - **Entity (`com.petshop.entity`):** Lớp POJO thuần túy đại diện cho bảng CSDL.
4. **Tối ưu RAM & Quầy POS:**
   - Tuyệt đối không dùng ComboBox nạp toàn bộ CSDL lên RAM.
   - Tra cứu khách hàng theo SĐT trực tiếp dưới DB (`WHERE soDienThoai = ?`) + nút "+ Thêm mới nhanh".
   - Quét mã vạch `txtMaVachSanPham` + sự kiện phím Enter bắn vào giỏ hàng (trùng mã tự động `+1` số lượng, khóa cứng giá bán).
5. **Phân quyền & Bảo mật (RBAC):**
   - Đăng nhập bằng **Mã nhân viên** (`maNhanVien`). Mật khẩu băm an toàn SHA-256 qua `HashUtil.sha256()`.
   - Nhân viên lập giao dịch tự động lấy từ `SessionManager.getTaiKhoanHienTai().getNhanVien()`.
6. **Regex & Xử lý ngoại lệ:**
   - Số điện thoại chuẩn 10 số (`^0\d{9}$`), Email chuẩn. Vi phạm quy tắc nghiệp vụ ném `IllegalArgumentException`.
7. **Kiểm thử & Biên dịch:**
   - Toàn bộ mã nguồn đảm bảo `mvn clean compile` đạt **BUILD SUCCESS (0 lỗi)**.

---

## 🔑 2. Danh Sách Tài Khoản Đăng Nhập Demo

Hệ thống định danh người dùng bằng **Mã nhân viên** riêng biệt. Khi đăng nhập bằng Mã nhân viên, hệ thống tự động nhận diện chức vụ và phân quyền truy cập:

| Mã NV / Tên đăng nhập | Họ và tên | Chức vụ / Vai trò | Mật khẩu mặc định | Quyền hạn trong hệ thống |
| :--- | :--- | :--- | :--- | :--- |
| **`260000`** *(hoặc `admin`)* | Quản trị hệ thống | **Admin** | `admin123` | Toàn quyền cấu hình, tài khoản và phân quyền |
| **`260001`** | Nguyễn Thị Lan | **Quản lý** | `123456` | Khuyến mãi, Duyệt đổi trả, Báo cáo & Thống kê |
| **`260002`** | Trần Văn Minh | **Bán hàng** | `123456` | Lập hóa đơn POS, Tra cứu sản phẩm, Lập phiếu đổi trả |
| **`260003`** | Phạm Thị Hoa | **Chăm sóc** | `123456` | Tiếp nhận thú cưng, Quản lý lịch hẹn Spa & Dịch vụ |
| *Nhân viên thêm mới* | Tự động sinh `26xxxx` | Theo chức vụ chọn | `1111` | Tự động cấp tài khoản khi thêm nhân viên |

> 💡 **Tính năng "Quên mật khẩu":**  
> Tích hợp trực tiếp trên màn hình đăng nhập `DangNhapView`. Nhân viên có thể tự đặt lại mật khẩu mới thông qua xác minh 2 lớp: **Mã nhân viên** và **Số điện thoại** chính chủ đã đăng ký trong hồ sơ nhân sự.

---

## 🏗️ 3. Cấu Trúc Mã Nguồn (3 Tầng)

```text
src/
├── com/petshop/
│   ├── entity/          # Các thực thể POJO thuần túy (Object Reference, 0% BigDecimal)
│   │   ├── KhachHang.java, ThuCung.java, NhanVien.java, TaiKhoan.java
│   │   ├── SanPham.java, DichVu.java, LichHen.java, PhieuDichVu.java
│   │   ├── DonHang.java, HoaDon.java, PhieuDoiTra.java, ChiTietPhieuDoiTra.java
│   │   ├── KhuyenMai.java, Ca.java, PhanCa.java, ThongKeTongQuan.java...
│   ├── dao/             # Tầng truy xuất CSDL SQL Server (Transaction commit/rollback)
│   │   ├── KhachHangDAO.java, NhanVienDAO.java, TaiKhoanDAO.java
│   │   ├── SanPhamDAO.java, HoaDonDAO.java, PhieuDoiTraDAO.java
│   │   ├── KhuyenMaiDAO.java, CaDAO.java, PhanCaDAO.java, ThongKeDAO.java...
│   ├── bus/             # Tầng nghiệp vụ, logic & validate (0% import javafx.*)
│   │   ├── XacThucBUS.java, DoiMatKhauBUS.java, NhanVienBUS.java
│   │   ├── KhachHangBUS.java, SanPhamBUS.java, HoaDonBUS.java
│   │   ├── PhieuDoiTraBUS.java, KhuyenMaiBUS.java, CaTrucBUS.java, TongQuanBUS.java...
│   ├── gui/             # Tầng giao diện người dùng JavaFX (Bắt sự kiện & gọi BUS)
│   │   ├── DangNhapView.java       # Màn hình đăng nhập Mã NV & Dialog Quên mật khẩu
│   │   ├── ManHinhChinhView.java   # Khung điều hướng chính (Menu & Phân quyền)
│   │   ├── TongQuanView.java       # Dashboard: 3 thẻ KPI làm sống + BarChart & PieChart
│   │   ├── doitra/                 # Phân hệ đổi trả hàng (LapPhieu, XuLy, TimKiem)
│   │   ├── khuyenmai/              # Phân hệ khuyến mãi (Them, CapNhat, TimKiem)
│   │   ├── catruc/                 # Phân hệ ca trực & phân công ca trực
│   │   ├── hoadon/                 # Quầy thanh toán POS & Báo cáo doanh thu
│   │   ├── nhanvien/               # Hồ sơ nhân sự & cấp phát tài khoản
│   │   ├── sanpham/, dichvu/, lichhen/, khachhang/...
│   ├── util/            # Tiện ích dùng chung
│   │   ├── DatabaseManager.java    # Quản lý kết nối JDBC SQL Server
│   │   ├── SessionManager.java     # Quản lý phiên làm việc sau đăng nhập
│   │   └── HashUtil.java           # Tiện ích băm mật khẩu chuẩn SHA-256
│   └── main/
│       └── Main.java               # Điểm khởi chạy ứng dụng (JavaFX Application)
└── main/resources/
    ├── com/petshop/gui/css/        # File định kiểu giao diện (chung.css, dangnhap.css...)
    └── com/petshop/gui/logo.png    # Logo thương hiệu Happy Tails
```

---

## 🚀 4. Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### 4.1. Yêu cầu môi trường
- **Java Development Kit (JDK):** Phiên bản **17** trở lên.
- **Apache Maven:** Phiên bản **3.8+**.
- **Hệ quản trị CSDL:** **Microsoft SQL Server** (2016 trở lên).

### 4.2. Cài đặt Cơ sở dữ liệu
1. Mở SQL Server Management Studio (SSMS).
2. Mở file [`QLCH_HappyTails_Database_Full.sql`](file:///d:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/QLCH_HappyTails_Database_Full.sql) và nhấn **Execute (F5)** để khởi tạo Database `QLCH_HappyTails` cùng toàn bộ bảng và dữ liệu mẫu chuẩn hóa.
3. Cấu hình kết nối trong file [`DatabaseManager.java`](file:///d:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/util/DatabaseManager.java) (hoặc qua biến môi trường):
   - **Host:** `localhost:1433`
   - **Database:** `QLCH_HappyTails`
   - **User mặc định:** `petshop` (hoặc `sa`)
   - **Password mặc định:** `Petshop@123`

### 4.3. Biên dịch và Khởi chạy

Chạy ứng dụng bằng terminal lệnh:

```powershell
# 1. Biên dịch dự án (Đảm bảo 0 lỗi)
mvn clean compile

# 2. Khởi chạy ứng dụng JavaFX
mvn javafx:run
```

Hoặc chạy trực tiếp class [`com.petshop.main.Main`](file:///d:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/main/Main.java) từ các IDE (IntelliJ IDEA, Eclipse, VS Code).

---

## ✨ 5. Các Phân Hệ & Chức Năng Nổi Bật

1. **Dashboard Tổng quan trực quan:**
   - 3 Thẻ KPI tương tác sống động (`Doanh thu`, `Lịch hẹn`, `Tồn kho`) có hiệu ứng hover và click điều hướng trực tiếp sang màn hình chuyên trách tương ứng.
   - 2 Biểu đồ JavaFX: `BarChart` thống kê doanh thu 7 ngày gần nhất và `PieChart` cơ cấu doanh thu Bán hàng vs Dịch vụ Spa.
2. **Quy trình Đổi trả hàng hóa chuẩn nghiệp vụ:**
   - Kiểm tra điều kiện nghiêm ngặt: Hóa đơn trong vòng 7 ngày, trạng thái "Đã thanh toán", chỉ cho phép đổi trả sản phẩm (không đổi trả dịch vụ).
   - Quản lý JDBC Transaction: Tự động hoàn trả số lượng tồn kho và khấu trừ điểm tích lũy khách hàng khi duyệt phiếu.
3. **Quản lý Khuyến mãi & Ca trực:**
   - Ràng buộc tỷ lệ giảm giá 1% - 100%, kiểm tra ngày bắt đầu/kết thúc hợp lệ.
   - Kiểm tra và ngăn chặn tuyệt đối tình trạng phân trùng ca trực của nhân viên trong cùng ngày.
4. **Quầy POS Bán hàng hiện đại:**
   - Quét mã vạch nhanh, tự động tính tổng tiền và chiết khấu điểm tích lũy, xuất hóa đơn tức thì.
