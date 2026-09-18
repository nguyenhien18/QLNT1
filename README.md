# Quản lý nhà trọ

Dự án gồm hai phần độc lập:

- backend/: Spring Boot REST API, Java 21, Maven Wrapper và MySQL.
- frontend/: React 19, Vite và React Router.

## Chạy ở máy cá nhân

1. Tạo database MySQL quanli_nha_tro6, hoặc đặt DB_URL trỏ đến database khác.
2. Mở PowerShell trong thư mục backend, cấu hình môi trường rồi chạy API:

    $env:DB_USERNAME = "ten_tai_khoan_mysql"
    $env:DB_PASSWORD = "mat_khau_mysql"
    $env:JWT_SECRET = "mot_chuoi_bi_mat_ngau_nhien_dai_it_nhat_32_byte"
    .\mvnw.cmd spring-boot:run

Nếu dùng Command Prompt, chuyển cả ổ đĩa bằng `cd /d` trước khi chạy:

    cd /d D:\QLNT\QLNT1\backend
    set DB_USERNAME=root
    set DB_PASSWORD=mat_khau_mysql
    set JWT_SECRET=mot_chuoi_bi_mat_ngau_nhien_dai_it_nhat_32_byte
    mvnw.cmd spring-boot:run

API mặc định ở http://localhost:8087.

3. Mở PowerShell khác trong thư mục frontend, cài dependency và chạy React:

    npm.cmd install
    npm.cmd run dev

Truy cập http://localhost:5500/.

## Cấu hình API

Frontend mặc định gọi http://localhost:8087. Khi cần địa chỉ khác, tạo tệp
frontend/.env.local từ .env.example:

    VITE_API_BASE=https://api.example.com

Đặt APP_CORS_ALLOWED_ORIGINS trên backend thành origin của frontend, ví dụ
https://app.example.com. Không đặt mật khẩu hay khóa JWT trong frontend.

## Kiểm tra

    cd backend
    .\mvnw.cmd test

    cd ..\frontend
    npm.cmd run build

Mã giao diện nằm trong frontend/src. Thư mục frontend chỉ giữ React, Vite
và các tệp cấu hình cần thiết.
