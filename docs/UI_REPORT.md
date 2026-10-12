# Báo cáo kiểm tra giao diện Happy Tails

**Phạm vi:** mã nguồn Java hiện có trong `src/com/petshop`, stylesheet và build/test Maven. Đây là kiểm tra tĩnh; chưa chạy ứng dụng với SQL Server.

## 1. Cấu trúc giao diện sau Prompt 2C

`com.petshop.view` hiện không có package con. Các lớp và giao diện hiện có:

| Kiểu | Kế thừa / triển khai | Trách nhiệm |
|---|---|---|
| `AppView` | interface | Hợp đồng Node, tiêu đề và hook vòng đời cho view. [AppView.java:5](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/AppView.java:5) |
| `DashboardView` | interface extends `AppView` | Đánh dấu view dashboard. [DashboardView.java:3](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/DashboardView.java:3) |
| `CrudView<T, ID>` | abstract `VBox`, `AppView` | Template cho tìm kiếm, bảng, form, validate, xác nhận và thông báo CRUD. [CrudView.java:26](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/CrudView.java:26) |
| `SupplierView` | `CrudView<NhaCungCap, String>` | View CRUD nhà cung cấp; lớp con CRUD duy nhất hiện đã đăng ký. [SupplierView.java:16](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/SupplierView.java:16) |
| `ManagerDashboardView` | `VBox`, `DashboardView` | Lắp thẻ chỉ số, biểu đồ doanh thu và bảng tồn/bán chạy. [ManagerDashboardView.java:16](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/ManagerDashboardView.java:16) |
| `SalesDashboardView` | `VBox`, `DashboardView` | Lắp bảng lịch chờ thanh toán, tồn kho, đơn nhập và hóa đơn. [SalesDashboardView.java:22](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/SalesDashboardView.java:22) |
| `CareDashboardView` | `VBox`, `DashboardView` | Lắp lịch hôm nay và bảng thú cưng có lưu ý sức khỏe. [CareDashboardView.java:15](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/CareDashboardView.java:15) |
| `MetricCard` | `VBox` | Hiển thị một chỉ số dashboard. [MetricCard.java:6](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/MetricCard.java:6) |
| `DataTablePanel` | `VBox` | Tiêu đề và bảng dữ liệu dạng chuỗi, có trạng thái rỗng. [DataTablePanel.java:17](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/DataTablePanel.java:17) |
| `RevenueChart` | `VBox` | Biểu đồ doanh thu theo ngày. [RevenueChart.java:19](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/RevenueChart.java:19) |
| `DashboardWidgets` | utility class | Chia sẻ welcome header, nút hóa đơn, định dạng và lỗi tải dashboard; không còn là lớp cha dashboard. [DashboardWidgets.java:21](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/DashboardWidgets.java:21) |
| `LoginView` | `StackPane` | Form đăng nhập, phụ thuộc `AuthenticationController` và `LoginListener`; không quản lý Scene. [LoginView.java:20](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/LoginView.java:20) |
| `LoginListener` | interface | Báo đăng nhập thành công cho composition ngoài view. [LoginListener.java:6](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/LoginListener.java:6) |
| `MainShell` | `BorderPane` | Ghép sidebar, topbar và ContentHost; đăng ký phím tắt Ctrl+K. [MainShell.java:15](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/MainShell.java:15) |
| `SidebarView` | `VBox` | Dựng nhóm/mục menu, trạng thái chọn và thu gọn. [SidebarView.java:23](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/SidebarView.java:23) |
| `TopbarView` | `HBox` | Thanh trên cùng, tìm kiếm, trợ giúp, thông báo và menu tài khoản. [TopbarView.java:16](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/TopbarView.java:16) |
| `ContentSink` | interface | Hợp đồng nhận nội dung và tiêu đề. [ContentSink.java:5](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/ContentSink.java:5) |
| `ContentHost` | `VBox`, `ContentSink` | Giữ nội dung và breadcrumb; quan sát màn hình hiện tại. [ContentHost.java:12](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/ContentHost.java:12) |
| `GlobalSearchPopup` | class | Popup kết quả tìm kiếm toàn cục. [GlobalSearchPopup.java:22](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/GlobalSearchPopup.java:22) |
| `ChangePasswordDialog` | class | Dialog đổi mật khẩu. [ChangePasswordDialog.java:17](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/ChangePasswordDialog.java:17) |
| `NotificationPopup` | class | Hiển thị thông báo dashboard theo dữ liệu controller. [NotificationPopup.java:7](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/NotificationPopup.java:7) |
| `PawIcon` | `Group` | Dựng biểu tượng bàn chân dùng chung, nhận kiểu/màu/kích thước. [PawIcon.java:7](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/PawIcon.java:7) |
| `PlaceholderView` | `VBox`, `AppView`, `SearchResultAware` | View dự phòng cho screen chưa triển khai nghiệp vụ. [PlaceholderView.java:8](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/PlaceholderView.java:8) |
| `ReportView` | `VBox`, `AppView` | View báo cáo hiện tại; chưa được nối nghiệp vụ báo cáo. [ReportView.java:7](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/ReportView.java:7) |
| `InvoiceCreationView` | `VBox`, `AppView` | View lập hóa đơn hiện tại; giao diện còn ở mức khung. [InvoiceCreationView.java:11](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/InvoiceCreationView.java:11) |
| `SearchResultAware` | interface | Cho phép view nhận ID kết quả tìm kiếm. [SearchResultAware.java:3](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/SearchResultAware.java:3) |

Các search provider nằm trong `com.petshop.search`, không thuộc `view`: `CustomerSearchProvider`, `PetSearchProvider`, `ProductSearchProvider`, `ServiceSearchProvider`, `AppointmentSearchProvider` kế thừa `AbstractSqlSearchProvider`, lớp này implement `SearchProvider`. [SearchProvider.java:9](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/search/SearchProvider.java:9), [AbstractSqlSearchProvider.java:14](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/search/AbstractSqlSearchProvider.java:14)

### Luồng tạo đối tượng và điều hướng

```text
Main.start(Stage)
  -> AppContext (composition root: DAO/controller/service)
  -> SceneManager.showLogin()
  -> LoginView(AuthenticationController, LoginListener)
  -> LoginListener gọi SceneManager.showMain(User)
  -> SceneManager tạo ContentHost + ScreenRegistry + NavigationController + MainShell
  -> MainShell ghép SidebarView + TopbarView + ContentHost
  -> NavigationController kiểm Role.canAccess, tạo AppView từ ScreenRegistry
  -> ContentHost.setContent(Node, title), view onShow/onHide
```

`Main` gọi composition root rồi `SceneManager.showLogin()` tại [Main.java:9](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/main/Main.java:9); wiring được đặt trong [AppContext.java:24](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/main/AppContext.java:24). Tạo/chuyển Scene, cấu hình Stage và stylesheet nằm trong [SceneManager.java:45](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/main/SceneManager.java:45) và [SceneManager.java:86](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/main/SceneManager.java:86). Điều hướng giữa screen thay nội dung qua [NavigationController.java:73](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/controller/NavigationController.java:73), không dựng Scene mới. Login/logout vẫn thay Scene qua SceneManager.

## 2. Kết quả kiểm tra kiến trúc

Các tìm kiếm dưới đây chạy trên `src/com/petshop` (riêng mục view giới hạn `src/com/petshop/view`), kết quả tính theo code hiện tại.

| Kiểm tra | Số lần / vị trí | Kết quả |
|---|---|---|
| `new Scene(` | 1: [SceneManager.java:86](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/main/SceneManager.java:86) | **ĐẠT** |
| import `Stage` hoặc `Scene` trong package view | 0 | **ĐẠT**; `Stage` chỉ được dùng ở `Main`/`SceneManager` |
| `new ...Dao(`/`new ...Controller(` trong package view | 0 | **ĐẠT** |
| `setStyle(`, `Color.web`, mã màu hex trong Java view | 0 | **ĐẠT**; các style nằm trong `src/main/resources/com/petshop/view/styles.css` |
| SQL hoặc `java.sql` trong package view | 0 | **ĐẠT** |
| `import javafx` trong package model và dao | 0 | **ĐẠT** |
| Switch theo `Screen` | 1, chỉ ở [ScreenRegistry.java:115](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/navigation/ScreenRegistry.java:115) | **ĐẠT** |
| Switch theo `Role` | 1, trong chính enum [Role.java:45](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/model/Role.java:45) để lấy hướng dẫn theo vai trò | **ĐẠT** |
| Switch theo nhóm menu | 0 | **ĐẠT**; nhóm do `MenuGroup` cung cấp |
| Lớp view dài hơn 300 dòng | 0 | **ĐẠT**; lớn nhất là `CrudView.java` 282 dòng |
| Biến static có thể thay đổi ngoài `SessionManager` | 0 được xác định trong source | **ĐẠT**; static còn lại là hằng hoặc phương thức |

**Code lặp:** các dashboard độc lập ghép các thành phần dùng chung `MetricCard`, `DataTablePanel`, `RevenueChart` và `DashboardWidgets`. [ManagerDashboardView.java:29](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/ManagerDashboardView.java:29), [SalesDashboardView.java:38](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/SalesDashboardView.java:38), [CareDashboardView.java:28](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/CareDashboardView.java:28). Việc lặp cấu trúc dựng bảng là có chủ ý qua `DataTablePanel`; chưa thực hiện phân tích định lượng token/code clone.

## 3. Screen, quyền và trạng thái giao diện

Registry đăng ký 16 screen tại [ScreenRegistry.java:45](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/navigation/ScreenRegistry.java:45). Những screen không có factory riêng dùng `PlaceholderView`; các ngoại lệ factory mặc định được khai báo trong [ScreenRegistry.java:115](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/navigation/ScreenRegistry.java:115).

| Screen | Nhãn / nhóm menu | Vai trò theo Role | View tạo ra / trạng thái |
|---|---|---|---|
| OVERVIEW | Tổng quan / ngoài nhóm | Cả ba | `ManagerDashboardView`, `SalesDashboardView`, `CareDashboardView` theo role; dashboard có dữ liệu từ controller |
| EMPLOYEES | Nhân viên / Tài khoản & Nhân sự | QUAN_LY | `PlaceholderView` |
| SHIFTS | Ca trực / Tài khoản & Nhân sự | QUAN_LY | `PlaceholderView` |
| SUPPLIERS | Nhà cung cấp / Danh mục | QUAN_LY | `SupplierView`; CRUD mẫu với DAO trong bộ nhớ |
| CARE_SERVICES | Dịch vụ chăm sóc / Danh mục | QUAN_LY | `PlaceholderView` |
| PROMOTIONS | Khuyến mãi / Danh mục | QUAN_LY | `PlaceholderView` |
| REPORTS | Thống kê & báo cáo / Báo cáo | QUAN_LY | `ReportView`; chỉ khung, chưa nối nghiệp vụ |
| PRODUCTS | Sản phẩm / Sản phẩm & Kho | BAN_HANG | `PlaceholderView` |
| INVENTORY | Tồn kho / Sản phẩm & Kho | BAN_HANG | `PlaceholderView` |
| PURCHASE_ORDERS | Đơn nhập / Sản phẩm & Kho | BAN_HANG | `PlaceholderView` |
| CUSTOMERS | Khách hàng / Khách hàng & Thú cưng | BAN_HANG, CHAM_SOC | `PlaceholderView` |
| PETS | Hồ sơ thú cưng / Khách hàng & Thú cưng | CHAM_SOC | `PlaceholderView` |
| APPOINTMENTS | Lịch hẹn / Khách hàng & Thú cưng | CHAM_SOC | `PlaceholderView` |
| INVOICE_CREATE | Lập hóa đơn / Bán hàng | BAN_HANG | `InvoiceCreationView`; khung, chưa xác minh nghiệp vụ lập hóa đơn |
| INVOICE_HISTORY | Lịch sử hóa đơn / Bán hàng | BAN_HANG | `PlaceholderView` |
| RETURNS | Đổi/trả hàng / Bán hàng | BAN_HANG | `PlaceholderView`; SRS use case đổi/trả chưa xác minh |

Ma trận quyền nằm trong từng hằng enum tại [Role.java:6](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/model/Role.java:6). Đối chiếu ma trận được yêu cầu: **không phát hiện lệch**. `OVERVIEW` được cấp cho cả ba; `GLOBAL_SEARCH` không còn là Screen vì tìm kiếm ở topbar; đổi mật khẩu cũng không còn là Screen.

### Dashboard và tìm kiếm

Dashboard được nạp qua `DashboardController`/`DashboardDao`: quản lý nhận doanh thu hôm nay, lịch hẹn hôm nay, tồn thấp, doanh số theo tuần và mặt hàng bán chạy; bán hàng nhận hóa đơn/doanh thu, tồn thấp, đơn nhập chờ, lịch đã hoàn thành chưa thanh toán và hóa đơn gần đây; chăm sóc nhận lịch của nhân viên hôm nay, lịch chờ xác nhận và thú cưng có ghi chú sức khỏe. Query được đặt trong [DashboardDao.java:63](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/dao/DashboardDao.java:63) trở đi. Phân nhánh quyền/loại thông báo nằm trong controller/Role, không có switch Role trong view.

Năm provider tìm kiếm và screen đích:

| Provider / nhóm | Screen đích | Quyền hiển thị |
|---|---|---|
| CustomerSearchProvider / Khách hàng | CUSTOMERS | BAN_HANG, CHAM_SOC |
| PetSearchProvider / Thú cưng | PETS | CHAM_SOC |
| ProductSearchProvider / Sản phẩm | PRODUCTS | BAN_HANG |
| ServiceSearchProvider / Dịch vụ | CARE_SERVICES | QUAN_LY |
| AppointmentSearchProvider / Lịch hẹn | APPOINTMENTS | CHAM_SOC |

Role lọc provider trước khi tìm ở [GlobalSearchService.java:23](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/search/GlobalSearchService.java:23). Mỗi provider có SQL riêng trong package `search`; search không đặt trong view. Mở kết quả được kiểm tra quyền bởi NavigationController.

### CRUD

`CrudView` yêu cầu lớp con cung cấp cột, form, validate, trích xuất đối tượng và kiểm tra khả năng deactivate (ví dụ hook ở [CrudView.java:243](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/CrudView.java:243)). `SupplierView` là lớp con đã dùng và override các hook chính, gồm `columns`, `createForm`, `validate`, `extractFromForm`, `canDeactivate` ([SupplierView.java:31](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/SupplierView.java:31), [SupplierView.java:82](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/SupplierView.java:82)). Chưa có lớp con CRUD khác.

## 4. Đối chiếu use case UC001–UC015

Không có tài liệu SRS/OOAD đối chiếu được trong workspace ở lần kiểm tra này; số UC gắn trong enum `Screen` là chú thích triển khai, chưa thay thế việc đọc đặc tả. Vì vậy không thể xác nhận đầy đủ trường, luồng phụ hay tiêu chí giao diện theo SRS.

| Use case | Màn hình/đầu mối hiện có | Thiếu hoặc chưa xác minh ở mức giao diện |
|---|---|---|
| UC001 | Chưa xác minh | Không tìm thấy ánh xạ trong code/tài liệu SRS hiện có |
| UC002 | EMPLOYEES | Placeholder; trường, nút và luồng chưa có |
| UC003 | SHIFTS | Placeholder; lịch/phân ca chưa có |
| UC004 | SUPPLIERS | Có CRUD mẫu; chưa xác minh đủ yêu cầu SRS |
| UC005 | CARE_SERVICES | Placeholder; quản lý bảng giá chưa có |
| UC006 | PROMOTIONS | Placeholder |
| UC007 | REPORTS | Có view thông báo khung; chưa có chọn loại/kỳ hoặc dữ liệu báo cáo |
| UC008 | Global search trên topbar | Có provider và popup; chưa kiểm thử runtime/từng quyền |
| UC009 | PRODUCTS | Placeholder |
| UC010 | INVENTORY | Placeholder; cảnh báo tồn chỉ có ở dashboard |
| UC011 | PURCHASE_ORDERS | Placeholder |
| UC012 | APPOINTMENTS | Placeholder |
| UC013 | PETS | Placeholder |
| UC014 | CUSTOMERS | Placeholder; chế độ giới hạn CHAM_SOC chưa xác minh |
| UC015 | INVOICE_CREATE, INVOICE_HISTORY; RETURNS được ghi chú là ngoài/chưa xác minh SRS | Lập hóa đơn ở mức khung; lịch sử và đổi/trả placeholder |

## 5. Vấn đề quan trọng còn lại

| Ưu tiên | Vấn đề đã xác minh | Đề xuất ngắn |
|---|---|---|
| Cao | 12 screen còn factory mặc định `PlaceholderView` ([ScreenRegistry.java:47](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/navigation/ScreenRegistry.java:47), [ScreenRegistry.java:118](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/navigation/ScreenRegistry.java:118)) | Triển khai view/controller/DAO theo từng UC trước khi xem là hoàn tất. |
| Cao | `ReportView` được tạo mặc định nhưng chỉ là view khung ([ScreenRegistry.java:116](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/navigation/ScreenRegistry.java:116), [ReportView.java:7](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/ReportView.java:7)) | Nối loại báo cáo, kỳ và dữ liệu thực. |
| Cao | `InvoiceCreationView` hiện là khung giao diện ([InvoiceCreationView.java:11](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/InvoiceCreationView.java:11)) | Hoàn thiện nhập/chọn sản phẩm, tính tiền, lưu và luồng từ lịch hẹn. |
| Vừa | Dashboard build lại dashboard factory và dùng `AtomicReference` để nối navigator trong `SceneManager` ([SceneManager.java:56](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/main/SceneManager.java:56)) | Cân nhắc factory dashboard có dependency điều hướng được đóng gói rõ hơn; hiện compile thành công. |
| Vừa | `DataTablePanel` dùng `TableView<ObservableList<String>>`, nên chỉ giữ dữ liệu chuỗi ([DataTablePanel.java:19](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/view/DataTablePanel.java:19)) | Khi thêm nghiệp vụ, dùng DTO/record có kiểu dữ liệu cho từng bảng. |
| Vừa | Role `CHAM_SOC` được phép vào CUSTOMERS theo screen-level permission; kiểm soát “chỉ tra cứu/thêm nhanh” chưa được xác minh ở placeholder ([Role.java:14](/D:/Study/HK8/PTUD/Git/p001-application-development-project-iuh/src/com/petshop/model/Role.java:14)) | Áp dụng chế độ hạn chế tại view/controller khi triển khai CUSTOMERS. |

## 6. Xác minh

| Lệnh | Kết quả |
|---|---|
| `mvn -q compile` | **Đạt** sau khi tách dashboard và thêm `AppContext`. |
| `mvn -q test` | **Đạt**, exit code 0. |
| `git diff --check` | **Đạt**, không có output lỗi. |

Chưa chạy ứng dụng JavaFX thật; chưa đăng nhập thử ba vai trò; chưa kết nối SQL Server; chưa xác minh hiển thị CSS trên runtime, truy vấn dữ liệu thực, nội dung popup, hay thao tác chuột/bàn phím ngoài kiểm tra mã và biên dịch. SRS UC001–UC015 đầy đủ cũng chưa được xác minh.
