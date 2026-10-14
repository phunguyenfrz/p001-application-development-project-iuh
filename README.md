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
│   ├── util/
│   │   ├── DatabaseInit.java
│   │   ├── DatabaseManager.java
│   │   └── SessionManager.java
│   └── view/
│       ├── LoginView.java
│       └── MainDashboard.java
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

Ung dung ket noi SQL Server (database `QLCH_HappyTails`).

1. Chay script `QLCH_HappyTails_Database_Full.sql` trong SSMS (luu y: script DROP va tao lai toan bo bang).
2. Neu can, dat bien moi truong `DB_URL`, `DB_USER`, `DB_PASSWORD` (mac dinh: localhost:1433, user `sa`).

## Tai khoan mau

| Tai khoan | Mat khau | Quyen |
| --- | --- | --- |
| `quanly` | `123456` | Quan ly |
| `banhang` | `123456` | Ban hang |
| `chamsoc` | `123456` | Cham soc |
| `admin` | `admin123` | tai khoan admin | 

Cac nut nghiep vu hien la placeholder de bo sung tung module sau.
