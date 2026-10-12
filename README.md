# Happy Tails

Ung dung desktop JavaFX quan ly cua hang thu cung, su dung Java 17, JavaFX 21,
Maven va SQL Server.

## Cau truc

```text
src/
├── com/petshop/
│   ├── controller/
│   │   ├── CrudController.java, CrudControllerImpl.java
│   │   ├── NavigationController.java, ScreenNavigator.java
│   │   ├── DashboardController.java, GlobalSearchController.java
│   │   └── LoginController.java, PasswordController.java, AuditController.java
│   ├── dao/
│   │   ├── *Repository.java (DAO interfaces)
│   │   ├── DashboardDao.java, GlobalSearchDao.java, UserDao.java
│   │   └── InMemorySupplierDao.java, SqlAuditDao.java
│   ├── navigation/
│   │   ├── ScreenDefinition.java
│   │   ├── ScreenRegistry.java
│   │   └── MenuModel.java
│   ├── main/
│   │   ├── Main.java
│   │   └── ApplicationControllers.java
│   ├── model/
│   │   ├── Role.java, Screen.java, User.java, NhaCungCap.java
│   │   ├── DashboardData.java, GlobalSearchResult.java
│   │   ├── Customer.java, Invoice.java, InvoiceItem.java, Pet.java, Product.java
│   │   └── package-info.java (OOAD model TODOs)
│   ├── util/
│   │   ├── DatabaseInit.java
│   │   ├── DbConfig.java
│   │   ├── DbConnection.java
│   │   ├── AuditService.java, Formats.java
│   │   └── SessionManager.java
│   └── view/
│       ├── AppView.java, CrudView.java, SupplierView.java
│       ├── ManagerDashboardView.java, SalesDashboardView.java, CareDashboardView.java
│       ├── DashboardWidgets.java, PlaceholderView.java
│       └── LoginView.java, MainDashboard.java, InvoiceCreationView.java, ReportView.java
└── main/resources/com/petshop/view/
    └── styles.css
```

Dieu huong dang ky factory theo `ScreenRegistry`; `Role` tu khai bao tap quyen.
Khung `CrudView<T, ID>` duoc minh hoa bang man hinh nha cung cap voi du lieu
in-memory; cac man hinh nghiep vu con lai hien dang la placeholder.

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
2. Copy `db.properties.example` thanh `src/db.properties`, sau do sua URL, user, password va ten chi nhanh theo SQL Server/cua hang cua ban.
3. Chay cac script can thiet trong `db/` theo thu tu. Ung dung khong tu chay cac script nay.
4. De bam lai mat khau cu (chua co BCrypt), chay class `com.petshop.util.RehashPasswords` thu cong sau khi da backup database. Class nay khong tu chay khi khoi dong ung dung.

Cac nut nghiep vu hien la placeholder de bo sung tung module sau.
