# p001 - Application Development Project

Đây là dự án đồ án chính bằng Java với backend Spring Boot. Frontend tách biệt và có thể tiếp tục phát triển trong `client/` nếu cần.

## Cấu trúc chính

- `server/`: ứng dụng backend Java Spring Boot
- `client/`: ứng dụng frontend React (tùy chọn)
- `docs/`: tài liệu đồ án
- `scripts/`: script hỗ trợ
- `.env.example`: mẫu cấu hình môi trường

## Chạy backend Java

1. Cài JDK 17 hoặc mới hơn
2. Vào thư mục backend:
   ```powershell
   cd server
   mvn spring-boot:run
   ```
3. Mở API:
   - `GET http://localhost:3000/api/hello`

## Chạy frontend (nếu cần)

1. Vào thư mục frontend:
   ```powershell
   cd client
   npm install
   npm start
   ```

## Ghi chú

- Backend Java sử dụng Spring Boot và Maven
- Frontend React chỉ là phần phụ nếu bạn muốn thêm giao diện
