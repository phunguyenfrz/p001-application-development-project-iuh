# PetShopManagement

Khung desktop app JavaFX quan ly cua hang thu cung cho chu cua hang va nhan vien.

## Cau truc

```text
src/com/petshop/
├── main/Main.java
├── model/User.java
├── util/SessionManager.java
├── controller/LoginController.java
└── view/
    ├── LoginView.java
    └── MainDashboard.java

src/main/resources/com/petshop/view/styles.css
```

## Chay ung dung

Yeu cau JDK 25 va Maven:

```powershell
mvn clean compile
mvn javafx:run
```

Hoac chay class `com.petshop.main.Main` truc tiep trong VS Code/IDE co ho tro JavaFX.

## Tai khoan mau

| Tai khoan | Mat khau | Quyen |
| --- | --- | --- |
| `owner` | `owner123` | Chu cua hang |
| `staff` | `staff123` | Nhan vien |

Cac nut nghiep vu hien la placeholder de bo sung tung module sau.
