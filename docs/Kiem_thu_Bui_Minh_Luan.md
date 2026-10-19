# Kiểm thử hộp đen - Bùi Minh Luân

Ngày lập: 16/10/2026  
Phạm vi: dịch vụ chăm sóc, lịch hẹn, hóa đơn.  
Điều kiện chạy: SQL Server đã chạy script `QLCH_HappyTails_Database_Full.sql`; tài khoản đăng nhập và dữ liệu mẫu hợp lệ.

| Mã màn | Dữ liệu vào / thao tác | Kết quả mong muốn | Kết quả thực tế |
| --- | --- | --- | --- |
| SRV_CRUD | Thêm dịch vụ tên “Tắm vệ sinh”, thời lượng 40 phút; sửa mô tả; chọn Ngừng cung cấp. | Dịch vụ được tạo/cập nhật; ngừng cung cấp đổi `trangThai` thành 0, không xóa dòng. | Chưa chạy - cần SQL Server. |
| SRV_SEARCH | Tìm theo một phần tên và theo mã dịch vụ. | Chỉ hiển thị dịch vụ khớp; tìm chuỗi không tồn tại trả danh sách rỗng. | Chưa chạy - cần SQL Server. |
| SRV_PRICING | Thêm giá cho dịch vụ ở các bậc Nhỏ/Vừa/Lớn, ngày áp dụng hợp lệ; thử ngày hết hạn trước ngày áp dụng. | Mức giá hợp lệ được lưu; khoảng ngày sai bị chặn; bảng hiển thị ngày hết hạn hoặc “Không thời hạn”. | Chưa chạy - cần SQL Server. |
| APT_CREATE | Chọn khách hàng, thú cưng thuộc khách đó, nhân viên đang làm, dịch vụ có giá hiệu lực; nhập ngày/giờ tương lai và cân nặng. | Tạo lịch trạng thái “Đã đặt” và chi tiết lịch chứa cân nặng, giá theo đúng bậc. Dữ liệu thiếu/sai hoặc không có giá bị từ chối. | Chưa chạy - cần SQL Server. |
| APT_CONFLICT | Tạo hai lịch cùng nhân viên hoặc cùng thú cưng có khoảng thời gian dịch vụ chồng nhau; lặp lại với hai lịch không chồng nhau. | Lịch chồng thời gian bị từ chối; lịch không chồng thời gian được tạo. Lịch đã hủy không chặn lịch mới. | Chưa chạy - cần SQL Server. |
| APT_STATUS | Chuyển lịch từ “Đã đặt” sang “Đã xác nhận”, tiếp đến “Đang thực hiện”, rồi “Hoàn thành”; thử cập nhật lịch đã kết thúc. | Cho phép các bước trạng thái hợp lệ; lịch kết thúc không được chuyển tiếp. | Chưa chạy - cần SQL Server. |
| APT_RESCHEDULE | Dời lịch đã đặt sang ngày/giờ tương lai; sau đó hủy lịch. Thử dời lịch đã hủy hoặc đang thực hiện. | Ngày/giờ mới được lưu; hủy đổi trạng thái thành “Đã hủy”; lịch không đủ điều kiện bị chặn. | Chưa chạy - cần SQL Server. |
| APT_SEARCH | Tìm theo mã lịch, tên/mã khách, tên/mã thú cưng và trạng thái. | Bảng trả các lịch khớp điều kiện, không sửa dữ liệu. | Chưa chạy - cần SQL Server. |
| INV_SEARCH | Tìm theo mã hóa đơn, tên khách hoặc trạng thái thanh toán. | Bảng hiển thị ngày, khách, nhân viên, tổng tiền, trạng thái và hình thức thanh toán phù hợp. | Chưa chạy - cần SQL Server. |
| INV_CREATE | Chọn ngày lập; quét mã vạch sản phẩm; chọn khuyến mãi đang hiệu lực; thêm sản phẩm và dịch vụ; thử số lượng vượt tồn; tạo hóa đơn chưa thanh toán. | Mã vạch thêm đúng sản phẩm; khuyến mãi hợp lệ được lưu và giảm tổng tiền; hóa đơn thanh toán trừ tồn kho; vượt tồn rollback toàn bộ; hóa đơn chưa thanh toán không trừ tồn. | Chưa chạy - cần SQL Server. |
| INV_CANCEL | Chọn hóa đơn chưa thanh toán để hủy; thử hủy hóa đơn đã thanh toán. | Hóa đơn chưa thanh toán chuyển “Đã hủy”; hóa đơn đã thanh toán bị chặn và hướng sang quy trình Phiếu đổi trả. | Chưa chạy - cần SQL Server. |

## Ghi chú bàn giao

- Kiểm tra biên định dạng và trạng thái được thực hiện ngay trên form; các ràng buộc khóa ngoại, giá hiệu lực và tồn kho được xác nhận ở DAO/CSDL.
- Maven `clean test` hoàn tất trên Java 17; repo hiện không có thư mục test hoặc báo cáo Surefire nên chưa có kết quả kiểm thử tự động.
- Review chéo cần một thành viên khác thực hiện trước khi merge; chưa thể ghi nhận trong phiên này.
