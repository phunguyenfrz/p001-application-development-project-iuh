# CHƯƠNG 6: Ý TƯỞNG KHỞI NGHIỆP VỚI HỆ THỐNG

Từ kết quả phân tích, thiết kế và xây dựng đề tài học phần "Hệ thống quản lý cửa hàng thú cưng Happy Tails", nhóm nhận thấy phần mềm đã giải quyết được khá trọn vẹn các vướng mắc trong quy trình bán hàng và chăm sóc thú cưng tại tiệm. Thay vì chỉ dừng lại ở một đồ án môn học, nhóm định hướng tiếp tục hoàn thiện sản phẩm để phát triển thành một dự án khởi nghiệp thực tế trong lĩnh vực Công nghệ thông tin. Chương này trình bày cụ thể về tổ chức nhân lực của nhóm theo các mảng chuyên môn CNTT, kế hoạch triển khai theo từng giai đoạn, bảng dự trù kinh phí, cũng như những điểm sáng tạo mà nhóm đã cải tiến khi tìm hiểu các phần mềm hiện có trên thị trường để xây dựng lại hệ thống.

## 6.1. Kế hoạch phát triển dự án khởi nghiệp và tổ chức nhân lực

### 6.1.1. Bối cảnh thực tế và định hướng khởi nghiệp của nhóm
Trong quá trình đi khảo sát thực tế tại cửa hàng thú cưng Happy Tails và một số tiệm chăm sóc thú cưng (Pet Shop & Spa) quy mô nhỏ tại TP.HCM, nhóm nhận thấy phần lớn các cơ sở này vẫn đang quản lý công việc hàng ngày theo cách thủ công hoặc sử dụng các công cụ rời rạc:
- Đối với mảng dịch vụ tắm, vệ sinh và cắt tỉa lông: Nhân viên thường ghi lịch hẹn của khách vào sổ tay, bảng trắng hoặc nhắn tin qua Zalo. Cách làm này rất dễ dẫn đến việc quên lịch của khách, xếp trùng giờ làm của nhân viên chăm sóc, hoặc không nắm được thú cưng đó có tiền sử dị ứng sữa tắm hay bệnh lý gì không.
- Đối với mảng bán thức ăn và phụ kiện: Một số cửa hàng có trang bị phần mềm bán hàng tạp hóa phổ thông. Tuy nhiên, các phần mềm này chỉ quản lý được hàng hóa theo mã vạch chứ không lưu được hồ sơ thú cưng của từng khách, không hỗ trợ tính giá dịch vụ theo số cân nặng của chó mèo, khiến thu ngân phải tính nhẩm bên ngoài rồi cộng tay khi khách vừa làm dịch vụ vừa mua thêm đồ ăn.

Qua việc tìm hiểu các chương trình hỗ trợ khởi nghiệp dành cho sinh viên như Đề án 1665 của Bộ Giáo dục và Đào tạo (Cuộc thi Học sinh, sinh viên với ý tưởng khởi nghiệp – SV_STARTUP) và các hoạt động của Trung tâm Đổi mới sáng tạo và Khởi nghiệp – Trường Đại học Công nghiệp TP.HCM (IUH), nhóm xác định hướng đi phù hợp nhất với năng lực của sinh viên ngành Kỹ thuật phần mềm là khởi nghiệp trực tiếp bằng sản phẩm Công nghệ thông tin. Cụ thể, nhóm tập trung đóng gói hoàn chỉnh phần mềm quản lý cửa hàng thú cưng để cung cấp và cài đặt cho các tiệm thú cưng vừa và nhỏ với mức chi phí hợp lý, dễ tiếp cận.

### 6.1.2. Nhân lực dự án và phân công theo các mảng của ngành Công nghệ thông tin
Đội ngũ thực hiện dự án gồm 04 thành viên thuộc lớp DHKTPM20B, Khoa Công nghệ thông tin, Trường Đại học Công nghiệp TP.HCM. Để dự án vận hành hiệu quả và đúng tính chất của một nhóm phát triển phần mềm, công việc được phân chia rõ ràng theo 04 mảng chuyên môn trong ngành Công nghệ thông tin:

**Bảng 6.1: Danh sách nhân lực dự án và phân công mảng công việc ngành CNTT**

| STT | Họ và tên (MSSV) | Vai trò trong nhóm | Mảng công việc CNTT phụ trách | Nhiệm vụ cụ thể trong dự án |
| :-: | :--- | :--- | :--- | :--- |
| 1 | Trần Lê Tuấn Anh (23652051) | Trưởng nhóm | Thiết kế kiến trúc hệ thống và Thống kê dữ liệu | Thiết kế cấu trúc phần mềm theo mô hình 3 tầng, quản lý phân quyền màn hình chính; xây dựng màn hình tổng quan và biểu đồ thống kê doanh thu; phát triển chức năng quản lý ca trực, chương trình khuyến mãi và xử lý phiếu đổi trả hàng hóa. |
| 2 | Nguyễn Gia Phú (23641771) | Thành viên | Phân tích nghiệp vụ, Phát triển luồng dịch vụ và Kiểm thử (Tester/QA) | Khảo sát quy trình thực tế, quản lý ghép nối mã nguồn của nhóm trên Git; lập trình chức năng đặt lịch hẹn, kiểm tra chống trùng lịch, tiếp nhận thú cưng tại quầy và thiết lập bảng giá dịch vụ theo bậc cân nặng; xây dựng kịch bản kiểm thử cho toàn bộ 16 chức năng. |
| 3 | Lê Hồng Hoàng (23656461) | Thành viên | Phát triển giao diện bán hàng (POS), Tích hợp thiết bị và Bảo mật | Thiết kế màn hình lập hóa đơn bán hàng, xử lý sự kiện quét mã vạch sản phẩm, phím tắt thao tác nhanh và in phiếu thanh toán; xây dựng chức năng đăng nhập bằng mã nhân viên, mã hóa mật khẩu, quên mật khẩu; quản lý thông tin khách hàng, thú cưng và nhân viên. |
| 4 | Bùi Minh Luân (25643931) | Thành viên | Quản trị cơ sở dữ liệu (Database) và Nghiệp vụ kho hàng | Thiết kế cơ sở dữ liệu trên hệ quản trị SQL Server, viết truy vấn và kiểm soát giao dịch (Transaction) tránh sai lệch số liệu; phát triển chức năng quản lý sản phẩm (tách biệt giá nhập và giá bán), theo dõi cảnh báo tồn kho, quản lý nhà cung cấp và lập đơn nhập hàng. |

## 6.2. Giới thiệu dự án và kế hoạch triển khai

### 6.2.1. Giới thiệu dự án "Hệ thống quản lý cửa hàng thú cưng Happy Tails"
Dự án khởi nghiệp của nhóm lấy chính tên đề tài nghiên cứu là **"Hệ thống quản lý cửa hàng thú cưng Happy Tails"**.

Đây là phần mềm quản lý chuyên biệt dành cho các cửa hàng kinh doanh sản phẩm thú cưng kết hợp dịch vụ chăm sóc (Spa – Grooming). Thay vì phải dùng riêng sổ ghi lịch hẹn và phần mềm tính tiền tạp hóa, cửa hàng chỉ cần sử dụng một hệ thống duy nhất tại quầy để quản lý xuyên suốt từ lúc khách đặt lịch, đưa thú cưng đến chăm sóc, mua sắm phụ kiện cho đến khi thanh toán hóa đơn, tích điểm thành viên và kiểm soát xuất nhập kho hàng ngày.

### 6.2.2. Kế hoạch triển khai dự án (Các giai đoạn, nhân sự và thời gian hoàn tất)
Nhóm xây dựng kế hoạch phát triển và đưa sản phẩm ra thực tế trong tổng thời gian **12 tháng**, chia làm **03 giai đoạn** cụ thể với quy mô nhân sự tăng dần từ **04 đến 06 người**:

- **Giai đoạn 1 (Tháng 1 – Tháng 3): Hoàn thiện phần mềm cốt lõi và kiểm thử nội bộ**
  - *Quy mô nhân sự:* 04 người (4 thành viên ngành CNTT của nhóm).
  - *Nội dung thực hiện:* Hoàn tất toàn bộ mã nguồn của 16 phân hệ chức năng theo mô hình 3 tầng, tiến hành kiểm thử kỹ lưỡng từng màn hình theo danh sách kịch bản kiểm thử để sửa triệt để các lỗi phát sinh. Cuối giai đoạn 1, nhóm đóng gói phần mềm kèm kịch bản khởi tạo cơ sở dữ liệu thành bộ cài đặt hoàn chỉnh để có thể cài đặt nhanh trên máy tính Windows của cửa hàng.

- **Giai đoạn 2 (Tháng 4 – Tháng 7): Triển khai chạy thử thực tế tại cửa hàng và tối ưu thao tác**
  - *Quy mô nhân sự:* 05 người (4 thành viên CNTT trong nhóm và tuyển thêm 01 cộng tác viên phụ trách hướng dẫn sử dụng, ghi nhận phản hồi của khách hàng).
  - *Nội dung thực hiện:* Cài đặt và đưa phần mềm vào vận hành thực tế tại cửa hàng thú cưng Happy Tails cùng 04 tiệm thú cưng quy mô nhỏ tại quận Gò Vấp và quận Bình Thạnh (TP.HCM). Trong giai đoạn này, nhóm trực tiếp kiểm tra độ ổn định khi kết nối phần mềm với súng quét mã vạch, máy in hóa đơn nhiệt tại quầy, đồng thời lắng nghe ý kiến của nhân viên thu ngân để tinh chỉnh lại giao diện cho thuận tiện và dễ bấm hơn.

- **Giai đoạn 3 (Tháng 8 – Tháng 12): Thương mại hóa rộng rãi và hỗ trợ xem báo cáo từ xa**
  - *Quy mô nhân sự:* 06 người (4 thành viên CNTT phụ trách kỹ thuật và 02 nhân sự phụ trách giới thiệu sản phẩm, đi cài đặt và hỗ trợ khách hàng).
  - *Nội dung thực hiện:* Nâng cấp khả năng đồng bộ cơ sở dữ liệu lên máy chủ trực tuyến (VPS) giúp chủ cửa hàng có thể theo dõi doanh thu, tồn kho từ xa mà không cần có mặt tại tiệm; đồng thời triển khai cung cấp phần mềm có thu phí cho các cửa hàng thú cưng trên địa bàn TP.HCM và các khu vực lân cận, hướng tới mục tiêu đạt từ 35 đến 40 cửa hàng sử dụng khi hoàn tất 12 tháng.

### 6.2.3. Dự trù kinh phí và phương án thu hồi vốn
Do cả 04 thành viên trong nhóm đều trực tiếp đảm nhận việc lập trình, thiết kế cơ sở dữ liệu và kiểm thử nên dự án tiết kiệm được toàn bộ chi phí thuê nhân công viết phần mềm bên ngoài. Tổng kinh phí dự trù để vận hành dự án trong 12 tháng đầu tiên là **120.000.000 VNĐ**, phù hợp với khả năng huy động vốn tự có của nhóm sinh viên kết hợp doanh thu xoay vòng từ những khách hàng đầu tiên.

**Bảng 6.2: Bảng dự trù kinh phí triển khai dự án trong 12 tháng**

| STT | Hạng mục chi phí | Nội dung chi tiết | Thời gian thực hiện | Kinh phí dự trù (VNĐ) |
| :-: | :--- | :--- | :---: | ---: |
| 1 | Thuê máy chủ (VPS) và tên miền | Thuê máy chủ Windows VPS chạy SQL Server phục vụ demo, sao lưu dữ liệu trực tuyến và duy trì tên miền website giới thiệu sản phẩm | 12 tháng | 15.000.000 |
| 2 | Mua sắm thiết bị phần cứng thử nghiệm | Trang bị 02 máy quét mã vạch cầm tay, 02 máy in hóa đơn nhiệt K80 và 01 cân điện tử để kiểm thử tích hợp và mang đi demo tại các tiệm | Giai đoạn 1 – 2 | 10.000.000 |
| 3 | Chi phí đi lại khảo sát và triển khai kỹ thuật | Phụ cấp xăng xe, đi lại khảo sát thực tế, cài đặt tận nơi, hướng dẫn nhân viên cửa hàng và trực hỗ trợ kỹ thuật cho các thành viên | 12 tháng | 55.000.000 |
| 4 | Chi phí tiếp thị và giới thiệu sản phẩm | Thiết kế trang web giới thiệu phần mềm, in ấn tài liệu hướng dẫn sử dụng, chạy quảng cáo tiếp cận các chủ cửa hàng thú cưng trên mạng xã hội | Tháng 4 – Tháng 12 | 22.000.000 |
| 5 | Chi phí đăng ký bản quyền và pháp lý | Đăng ký quyền tác giả phần mềm tại Cục Bản quyền tác giả và làm thủ tục đăng ký hộ kinh doanh | Giai đoạn 2 | 8.000.000 |
| 6 | Chi phí dự phòng phát sinh | Dự phòng nâng cấp dung lượng máy chủ và các chi phí phát sinh trong quá trình đi triển khai thực tế | 12 tháng | 10.000.000 |
| | **TỔNG CỘNG** | **Hoàn tất triển khai trong 12 tháng với quy mô 4 – 6 nhân sự** | **12 tháng** | **120.000.000** |

**Phương án thu phí và thời điểm hoàn vốn:**
- Nhóm cung cấp 02 hình thức sử dụng để chủ tiệm dễ lựa chọn theo quy mô:
  1. *Gói cài đặt trọn gói (mua đứt một lần):* **3.500.000 VNĐ/cửa hàng** (cài đặt trực tiếp trên máy tính tại quầy, bao gồm 12 tháng bảo hành và hỗ trợ kỹ thuật miễn phí).
  2. *Gói thuê theo tháng:* **250.000 VNĐ/tháng** (dành cho các tiệm mới mở muốn tiết kiệm chi phí ban đầu hoặc cần lưu trữ dữ liệu trực tuyến).
- Với tổng chi phí dự trù 120.000.000 VNĐ, dự án chỉ cần triển khai thành công cho khoảng **35 cửa hàng** (35 cửa hàng x 3.500.000 VNĐ = 122.500.000 VNĐ), dự kiến đạt được vào khoảng **tháng thứ 8 đến tháng thứ 9**, là nhóm đã thu hồi đủ số vốn ban đầu và bắt đầu có lợi nhuận để tái đầu tư.

## 6.3. Điểm đặc biệt của dự án và những cải tiến khi phát triển lại từ các phần mềm hiện có

### 6.3.1. Nhìn nhận thực tế thị trường và cách tiếp cận của nhóm
Nhóm nhận thức rất rõ rằng đề tài phần mềm quản lý cửa hàng không phải là một ý tưởng hoàn toàn mới chưa từng có ai làm. Thực tế trên thị trường hiện nay đã có nhiều phần mềm quản lý bán hàng phổ biến như KiotViet, Sapo, PosApp, cũng như rất nhiều đồ án sinh viên từng làm về quản lý bán hàng.

Tuy nhiên, theo đúng định hướng học hỏi từ thực tế: nhóm đã tìm hiểu cách các phần mềm thương mại đang vận hành cũng như xem xét hạn chế của các đồ án thông thường, sau đó bắt tay vào thiết kế và làm lại hệ thống sao cho giải quyết đúng những điểm bất tiện tại cửa hàng thú cưng. Nếu các phần mềm bán lẻ đại trà rất mạnh về quét mã vạch tính tiền hàng hóa nhưng lại bỏ trống mảng quản lý thú cưng và dịch vụ theo cân nặng, thì các đồ án sinh viên trước đây lại thường thiết kế giao diện chọn danh sách thủ công, chưa sát với thao tác thực tế của thu ngân tại quầy.

**Bảng 6.3: Đối sánh giữa các phần mềm hiện có và những điểm nhóm đã cải tiến khi xây dựng lại**

| Tiêu chí so sánh | Phần mềm bán lẻ phổ thông (KiotViet, Sapo) | Các đồ án quản lý cửa hàng thông thường | Hệ thống Happy Tails (Nhóm kế thừa và phát triển thêm) |
| :--- | :--- | :--- | :--- |
| 1. Quản lý thông tin thú cưng và lưu ý sức khỏe | Không có mục quản lý thú cưng, chỉ lưu thông tin người mua hàng. | Có bảng thú cưng nhưng tách rời, ít gắn kết với lúc tiếp nhận dịch vụ. | Mỗi thú cưng gắn chặt với khách hàng; khi chọn khách sẽ lọc đúng thú cưng của khách đó kèm ghi chú dị ứng, tình trạng sức khỏe ngay trên màn hình tiếp nhận. |
| 2. Tính giá dịch vụ Spa theo cân nặng | Không hỗ trợ, nhân viên phải tự xem bảng giá giấy bên ngoài rồi nhập tay số tiền. | Thường chỉ lưu 1 mức giá cố định cho mỗi dịch vụ, không phân biệt thú cưng nhỏ hay lớn. | Thiết lập bảng giá theo từng khung cân nặng; khi nhân viên nhập số ký thực tế lúc cân tại quầy, phần mềm tự động tra cứu và áp đúng mức giá tương ứng. |
| 3. Thanh toán gộp dịch vụ và hàng mua lẻ | Phải tạo phiếu riêng hoặc coi dịch vụ như một mặt hàng tồn kho thông thường. | Tách riêng màn hình trả tiền dịch vụ và màn hình trả tiền mua hàng. | Tại màn hình lập hóa đơn, thu ngân có thể tìm và gộp luôn phiếu dịch vụ vừa làm xong vào chung giỏ hàng mua thức ăn để tính tiền và tích điểm một lần. |
| 4. Tốc độ xử lý khi dữ liệu lớn dần qua nhiều năm | Tốt, tìm kiếm trực tiếp theo mã vạch hoặc từ khóa. | Thường tải toàn bộ khách hàng và sản phẩm vào hộp chọn (ComboBox), gây đơ máy khi dữ liệu lên tới hàng chục ngàn dòng. | Loại bỏ việc tải toàn bộ dữ liệu vào hộp chọn; chuyển sang tìm trực tiếp dưới cơ sở dữ liệu theo số điện thoại và quét mã vạch sản phẩm cộng dồn số lượng tức thì. |
| 5. Kiểm soát trùng lịch hẹn chăm sóc | Không có chức năng kiểm tra xung đột lịch hẹn chuyên sâu. | Cho phép lưu trùng giờ, trùng nhân viên hoặc đặt lịch lùi về ngày trong quá khứ. | Tự động chặn đặt lịch trong quá khứ, kiểm tra không cho phép xếp trùng nhân viên chăm sóc hoặc trùng thú cưng trong cùng một khung giờ và chặn tiếp nhận 2 lần cho cùng 1 lịch hẹn. |
| 6. Quản lý giá vốn, đổi trả hàng và bảo mật | Đầy đủ và chặt chẽ. | Thường chỉ có 1 cột giá duy nhất; mật khẩu lưu dạng văn bản thường; nhân viên phải tự chọn tên mình khi lập phiếu. | Tách rõ giá nhập kho và giá bán niêm yết (khóa không cho thu ngân tự sửa giá bán); đổi trả trong 7 ngày tự động cộng lại kho và trừ điểm; đăng nhập theo mã nhân viên có mã hóa mật khẩu và tự động điền người lập phiếu. |

### 6.3.2. Những ý tưởng cải tiến cụ thể nhóm đã phát triển trong phần mềm
Khi tự tay xây dựng lại toàn bộ hệ thống cho cửa hàng thú cưng Happy Tails, nhóm đã tập trung phát triển 05 điểm cải tiến thiết thực sau đây:

1. **Tự động tính lại giá dịch vụ theo số cân thực tế tại quầy tiếp nhận:**
   Thực tế tại các tiệm thú cưng, khi khách gọi điện hoặc nhắn tin đặt lịch tắm, cắt tỉa lông, khách thường chỉ nhớ mang máng cân nặng của thú cưng (ví dụ báo khoảng 4kg nhưng khi mang đến tiệm cân thực tế là 5,5kg – đã bước sang mức giá khác). Để giải quyết vấn đề này, nhóm thiết kế màn hình Đặt lịch chỉ ghi nhận mức giá tạm tính ban đầu. Khi khách đưa thú cưng đến quầy tiếp nhận, nhân viên đặt thú cưng lên cân và nhập số cân thực tế vào phần mềm; hệ thống sẽ tự động tra cứu lại trong bảng giá theo bậc cân nặng để nhảy đúng đơn giá chính thức, đồng thời tự động cập nhật luôn số cân mới vào hồ sơ của thú cưng đó để tiện theo dõi cho những lần sau.

2. **Thiết kế lại màn hình lập hóa đơn theo đúng thao tác quầy thu ngân thực tế:**
   Học hỏi từ các phần mềm bán hàng siêu thị, nhóm đã bỏ hoàn toàn cách chọn khách hàng và sản phẩm bằng danh sách thả xuống (ComboBox) vốn rất bất tiện khi cửa hàng hoạt động 5 – 10 năm có hàng vạn dữ liệu. Thay vào đó, thu ngân chỉ cần dùng máy quét mã vạch (hoặc gõ mã sản phẩm rồi nhấn phím Enter) là mặt hàng tự động nhảy vào bảng chi tiết, nếu quét tiếp cùng mã đó thì số lượng tự động tăng thêm 1. Màn hình cũng hỗ trợ các phím tắt bàn phím (F2 để tìm số điện thoại khách, F4 để đưa chuột vào ô quét mã, F9 để thanh toán và in hóa đơn) cùng nút thêm nhanh khách hàng mới hoặc thú cưng mới ngay tại chỗ mà không cần phải thoát sang màn hình khác.

3. **Gộp nhanh phiếu dịch vụ chăm sóc vào hóa đơn bán lẻ sản phẩm:**
   Khi khách đến đón thú cưng sau khi tắm/cắt tỉa xong, khách rất hay mua thêm hạt thức ăn, sữa tắm hoặc phụ kiện mang về. Nhóm đã bổ sung chức năng "Gộp phiếu dịch vụ" ngay trên màn hình lập hóa đơn: thu ngân chỉ cần tìm theo số điện thoại của khách hoặc mã phiếu để nạp toàn bộ các dịch vụ chăm sóc vừa hoàn thành vào chung một hóa đơn với các mặt hàng mua thêm. Nhờ đó, cửa hàng chỉ cần in một tờ hóa đơn tổng hợp duy nhất, tự động tính khuyến mãi và cộng điểm tích lũy một lần cho khách.

4. **Tách biệt giá nhập – giá bán và đồng bộ tồn kho, điểm thưởng khi đổi trả hàng:**
   Trong cơ sở dữ liệu sản phẩm, nhóm tách bạch rõ cột Giá nhập (giá vốn khi nhập từ nhà cung cấp) và Giá bán (giá niêm yết cho khách). Tại màn hình bán hàng, đơn giá bán được khóa cứng để nhân viên thu ngân không thể tự ý sửa giá làm thất thoát doanh thu của tiệm. Bên cạnh đó, mọi thao tác thanh toán hóa đơn, nhập kho hay xử lý đổi trả hàng trong vòng 7 ngày đều được kiểm soát đồng bộ: khi duyệt một phiếu đổi trả hợp lệ, phần mềm sẽ tự động cộng trả số lượng hàng về kho và khấu trừ lại số điểm tích lũy đã cộng trước đó của khách hàng.

5. **Màn hình tổng quan có biểu đồ trực quan và bấm chuyển trực tiếp tới công việc cần xử lý:**
   Thay vì chỉ hiển thị các con số thống kê tĩnh, màn hình tổng quan của người quản lý được bổ sung biểu đồ cột theo dõi doanh thu 7 ngày gần nhất và biểu đồ tròn thể hiện tỷ lệ doanh thu giữa mảng bán lẻ sản phẩm và mảng dịch vụ chăm sóc. Đặc biệt, các thẻ thông báo trên màn hình tổng quan đều có thể bấm vào để xử lý ngay: ví dụ khi thấy thẻ báo có mặt hàng sắp hết trong kho, người quản lý chỉ cần bấm trực tiếp vào thẻ đó là phần mềm sẽ chuyển thẳng sang màn hình theo dõi tồn kho và lọc sẵn danh sách các sản phẩm chạm ngưỡng tối thiểu để lập đơn nhập hàng ngay lập tức.

## 6.4. Tóm tắt nội dung 3 trang Slide Khởi nghiệp và trả lời 3 câu hỏi trọng tâm

Để phục vụ cho bài thuyết trình bảo vệ đồ án (tối đa 20 trang slide, trong đó có 03 trang dành cho phần khởi nghiệp), nhóm tóm tắt nội dung cốt lõi của 3 trang slide và phần trả lời cho 3 câu hỏi của giảng viên như sau:

### 6.4.1. Nội dung tóm tắt trên 3 trang Slide Khởi nghiệp (Slide 18, 19, 20)
- **Trang Slide 18 (Khởi nghiệp 1/3) – Giới thiệu dự án & Tổ chức nhân lực CNTT:**
  - *Tên dự án:* Hệ thống quản lý cửa hàng thú cưng Happy Tails.
  - *Định hướng:* Phát triển phần mềm quản lý chuyên biệt cho các tiệm thú cưng và Spa quy mô vừa và nhỏ (tham khảo định hướng từ Đề án 1665 – SV_STARTUP và Trung tâm Khởi nghiệp IUH).
  - *Nhân sự (04 sinh viên CNTT – Lớp DHKTPM20B):* Tuấn Anh (Thiết kế hệ thống, thống kê & đổi trả), Gia Phú (Phân tích nghiệp vụ, luồng lịch hẹn/dịch vụ & kiểm thử), Hồng Hoàng (Giao diện bán hàng POS, thiết bị mã vạch & bảo mật), Minh Luân (Cơ sở dữ liệu SQL Server, quản lý kho & nhập hàng).
- **Trang Slide 19 (Khởi nghiệp 2/3) – Kế hoạch triển khai 3 giai đoạn & Dự trù kinh phí:**
  - *Thời gian hoàn tất:* 12 tháng, chia làm 3 giai đoạn:
    - Giai đoạn 1 (Tháng 1 – 3 | 4 người): Hoàn thiện 16 chức năng, kiểm thử theo Test Cases, đóng gói bộ cài.
    - Giai đoạn 2 (Tháng 4 – 7 | 5 người): Chạy thực tế tại cửa hàng Happy Tails và 4 tiệm thú cưng ở TP.HCM, kết nối máy quét mã vạch và máy in hóa đơn.
    - Giai đoạn 3 (Tháng 8 – 12 | 6 người): Đưa CSDL lên máy chủ VPS xem báo cáo từ xa, triển khai thương mại cho 35 – 40 cửa hàng.
  - *Dự trù kinh phí 12 tháng:* 120.000.000 VNĐ (Thuê VPS & tên miền: 15 triệu; Thiết bị phần cứng test: 10 triệu; Đi lại khảo sát & triển khai: 55 triệu; Quảng bá sản phẩm: 22 triệu; Bản quyền & pháp lý: 8 triệu; Dự phòng: 10 triệu).
  - *Thu hồi vốn:* Thu phí trọn gói 3.500.000 VNĐ/tiệm hoặc thuê bao 250.000 VNĐ/tháng, dự kiến hoàn vốn vào tháng thứ 8 – 9 khi đạt khoảng 35 cửa hàng.
- **Trang Slide 20 (Khởi nghiệp 3/3) – Điểm khác biệt và cải tiến khi làm lại hệ thống:**
  - Học hỏi thao tác quét mã vạch nhanh của phần mềm thương mại (KiotViet, Sapo) và khắc phục lỗi tải toàn bộ dữ liệu vào hộp chọn (ComboBox) của các đồ án cũ.
  - Phát triển thêm các tính năng riêng cho ngành thú cưng: Tự động tính lại giá dịch vụ theo số cân thực tế tại quầy tiếp nhận; lưu hồ sơ thú cưng kèm cảnh báo dị ứng/bệnh lý; kiểm soát chống trùng lịch hẹn; gộp phiếu dịch vụ chăm sóc vào chung hóa đơn bán lẻ sản phẩm; tách biệt giá nhập – giá bán và bấm điều hướng trực tiếp từ màn hình tổng quan.

### 6.4.2. Trả lời trực tiếp 3 câu hỏi ở cuối bài báo cáo
- **Câu hỏi 1: Nhân lực dự án gồm những ai, làm những mảng gì của ngành CNTT? Giải thích kế hoạch phát triển dự án khởi nghiệp?**
  - *Trả lời:* Nhóm gồm 04 sinh viên ngành Kỹ thuật phần mềm (Khoa CNTT – IUH), mỗi người phụ trách một mảng công việc rõ ràng của ngành CNTT: Trần Lê Tuấn Anh phụ trách thiết kế kiến trúc hệ thống và thống kê dữ liệu; Nguyễn Gia Phú phụ trách phân tích nghiệp vụ, lập trình luồng đặt lịch – dịch vụ và kiểm thử phần mềm; Lê Hồng Hoàng phụ trách giao diện bán hàng POS, kết nối thiết bị ngoại vi và bảo mật tài khoản; Bùi Minh Luân phụ trách thiết kế cơ sở dữ liệu SQL Server và nghiệp vụ quản lý kho, nhập hàng. Kế hoạch phát triển dự án đi từ việc hoàn thiện và đóng gói sản phẩm phần mềm trong 3 tháng đầu, sau đó cài đặt chạy thử nghiệm thực tế tại cửa hàng Happy Tails cùng các tiệm nhỏ ở TP.HCM từ tháng 4 đến tháng 7 để tinh chỉnh thao tác, rồi mới mở rộng thương mại hóa và hỗ trợ đồng bộ dữ liệu trực tuyến từ tháng 8 đến tháng 12.
- **Câu hỏi 2: Dự án của các em là gì? Trình bày kế hoạch cho dự án (gồm những giai đoạn gì, dự trù kinh phí, nhân sự bao nhiêu người, hoàn tất trong thời gian bao lâu)?**
  - *Trả lời:* Dự án của nhóm mang chính tên đề tài là **"Hệ thống quản lý cửa hàng thú cưng Happy Tails"** – phần mềm quản lý tích hợp cả bán lẻ sản phẩm và dịch vụ chăm sóc Spa cho thú cưng. Kế hoạch dự án được thực hiện và hoàn tất trong **12 tháng** qua **03 giai đoạn** (Giai đoạn 1 từ tháng 1–3 hoàn thiện phần mềm và kiểm thử; Giai đoạn 2 từ tháng 4–7 triển khai chạy thử thực tế tại tiệm; Giai đoạn 3 từ tháng 8–12 thương mại hóa và đồng bộ máy chủ VPS). Nhân sự bắt đầu với **04 thành viên CNTT** ở giai đoạn 1, tăng lên **05 người** ở giai đoạn 2 và **06 người** ở giai đoạn 3 (bổ sung thêm nhân sự đi giới thiệu sản phẩm và hỗ trợ kỹ thuật). Tổng kinh phí dự trù cho 12 tháng là **120.000.000 VNĐ**, dự kiến thu hồi vốn vào khoảng tháng thứ 8 đến tháng thứ 9 khi triển khai được cho 35 cửa hàng.
- **Câu hỏi 3: Dự án của các em có gì khác biệt so với người ta? Đề tài không phải làm mới hoàn toàn mà đã được làm nhiều trên thị trường, khi xem người ta làm và làm lại thì nhóm đã phát triển được những gì hay và sáng tạo?**
  - *Trả lời:* Nhóm nhìn nhận thẳng thắn rằng các phần mềm bán hàng trên thị trường như KiotViet hay Sapo đã làm rất tốt khâu quét mã vạch tính tiền hàng hóa, nhưng họ thiết kế chung cho tiệm tạp hóa nên không có hồ sơ thú cưng và không tính được giá dịch vụ theo cân nặng. Khi tìm hiểu cách họ làm và tự xây dựng lại hệ thống cho tiệm thú cưng Happy Tails, nhóm đã học hỏi cách thiết kế màn hình thu ngân thao tác nhanh bằng mã vạch, phím tắt và tìm trực tiếp theo số điện thoại (thay vì nạp hết dữ liệu lên ComboBox gây treo máy như các đồ án cũ), đồng thời phát triển thêm 04 điểm sáng tạo riêng: (1) Tự động tính lại đơn giá dịch vụ theo số cân thực tế ngay khi cân thú cưng tại quầy tiếp nhận và cập nhật lại cân nặng vào hồ sơ; (2) Gắn chặt hồ sơ từng thú cưng với chủ nuôi kèm cảnh báo dị ứng và kiểm tra chống trùng lịch hẹn; (3) Cho phép gộp phiếu dịch vụ chăm sóc vừa làm xong vào chung hóa đơn mua thức ăn để thanh toán và tích điểm một lần; (4) Tách biệt giá nhập – giá bán, tự động hoàn kho khi đổi trả hàng trong 7 ngày và cho phép bấm vào các thẻ cảnh báo trên màn hình tổng quan để chuyển thẳng tới màn hình xử lý.

---

## II. TÀI LIỆU THAM KHẢO

**Tiếng Việt:**

1. Bộ Giáo dục và Đào tạo, (03-2025), *Cổng thông tin Hỗ trợ học sinh, sinh viên khởi nghiệp quốc gia (Đề án 1665 – Cuộc thi Học sinh, sinh viên với ý tưởng khởi nghiệp SV_STARTUP)*, https://dean1665.vn
2. Công ty Cổ phần Công nghệ KiotViet, (01-2026), *Tài liệu hướng dẫn sử dụng phần mềm quản lý bán hàng và dịch vụ KiotViet*, https://www.kiotviet.vn
3. Khu Công nghệ Phần mềm Đại học Quốc gia TP.HCM (ITP), (09-2025), *Chương trình ươm tạo doanh nghiệp khởi nghiệp Công nghệ thông tin dành cho sinh viên*, https://itp.vn
4. Trung tâm Đổi mới sáng tạo và Khởi nghiệp – Trường Đại học Công nghiệp TP.HCM (IUH - IEC), (10-2025), *Cẩm nang hướng dẫn phát triển dự án khởi nghiệp đổi mới sáng tạo dành cho sinh viên IUH*, https://iec.iuh.edu.vn

**Tiếng Anh:**

5. Microsoft Corporation, (01-2026), *Microsoft JDBC Driver for SQL Server Documentation*, https://learn.microsoft.com/en-us/sql/connect/jdbc/microsoft-jdbc-driver-for-sql-server
6. OpenJFX Community, (01-2026), *JavaFX 17 Official Documentation and UI Controls Reference*, https://openjfx.io/javadoc/17
7. Osterwalder, A., & Pigneur, Y., (2020), *Business Model Generation: A Handbook for Visionaries, Game Changers, and Challengers*, John Wiley & Sons, New Jersey.
