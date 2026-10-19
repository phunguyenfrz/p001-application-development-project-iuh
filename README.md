# PetShopManagement

Khung desktop app JavaFX quan ly cua hang thu cung cho chu cua hang va nhan vien.

## Cau truc

```text
src/
├── com/petshop/
│   ├── controller/
│   │   └── LoginController.java
│   ├── dao/
│   │   └── UserDao.java
│   ├── main/
│   │   └── Main.java
│   ├── model/
│   │   ├── Customer.java
│   │   ├── Invoice.java
│   │   ├── InvoiceItem.java
│   │   ├── Pet.java
│   │   ├── Product.java
│   │   └── User.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── MenuRegistry.java
│   ├── util/
│   │   ├── DatabaseInitializer.java
│   │   ├── DatabaseManager.java
│   │   └── SessionManager.java
│   └── view/
│       ├── LoginView.java
│       ├── MainDashboard.java
│       └── ModuleListView.java
└── main/resources/com/petshop/view/
    └── styles.css
```

## Chay ung dung

Yeu cau JDK 17 va Maven:

```powershell
mvn clean compile
mvn javafx:run
```

Hoac chay class `com.petshop.main.Main` truc tiep trong VS Code/IDE co ho tro JavaFX.

## Co so du lieu

Ung dung ket noi SQL Server (database `QLCH_HappyTails`) qua TCP/IP cong 1433.

1. Chay script `QLCH_HappyTails_Database_Full.sql` trong SSMS (luu y: script DROP va tao lai toan bo bang).
2. Neu can, dat bien moi truong `DB_URL`, `DB_USER`, `DB_PASSWORD` (mac dinh trong `DatabaseManager`: `localhost:1433`, user `petshop`, password `Petshop@123`).

## Tai khoan mau

| Tai khoan | Mat khau | Quyen |
| --- | --- | --- |
| `admin` | `admin123` | Admin (Toan quyen) |
| `quanly` | `123456` | Quan ly |
| `banhang` | `123456` | Ban hang |
| `chamsoc` | `123456` | Cham soc |

Cac nut nghiep vu hien la placeholder (`ModuleListView`) de cac thanh vien bo sung tung module.


## Cập nhật database cho xóa mềm khách hàng

Sau khi pull code, mở `db/01_customer_status.sql` trong SSMS và chạy trên
database `QLCH_HappyTails` trước khi mở ứng dụng. Không cần chạy lại script
tạo toàn bộ database. Script này bổ sung cột `KhachHang.trangThai` nếu chưa có,
giữ nguyên dữ liệu hiện có và đặt trạng thái mặc định bằng 1.

- Trang Cập nhật chỉ hiển thị khách hàng đang hoạt động.
- Ngừng hoạt động đặt trạng thái bằng 0, không xóa khách hàng hoặc dữ liệu liên quan.
- Trang Tìm kiếm và Lịch sử vẫn tra cứu được khách đã ngừng hoạt động.
- Số điện thoại của khách ngừng hoạt động vẫn được giữ theo ràng buộc UNIQUE.