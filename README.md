# PetShopManagement

Khung desktop app Java Swing để quản lý cửa hàng thú cưng cho chủ cửa hàng và nhân viên.

## Cấu trúc

```text
src/com/petshop/
├── main/Main.java
├── model/User.java
├── util/SessionManager.java
├── controller/LoginController.java
└── view/
    ├── LoginFrame.java
    └── MainDashboard.java
```

## Chạy ứng dụng

Yêu cầu JDK 17 và Maven:

```powershell
mvn clean compile
mvn exec:java
```

Hoặc chạy class `com.petshop.main.Main` trực tiếp trong VS Code/IDE.

## Tài khoản mẫu

| Tài khoản | Mật khẩu | Quyền |
| --- | --- | --- |
| `owner` | `owner123` | Chủ cửa hàng |
| `staff` | `staff123` | Nhân viên |

Các nút nghiệp vụ hiện là placeholder để bổ sung từng module sau.
