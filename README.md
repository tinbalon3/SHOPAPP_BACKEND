
# VNA Fruit Website - Tóm tắt Dự án

## Mô tả Dự án

VNA Fruit Website là một nền tảng thương mại điện tử được phát triển theo kiến trúc Model-View-Controller (MVC). Dự án cung cấp một thị trường trực tuyến cho việc mua bán trái cây chất lượng cao của Việt Nam. Website bao gồm các tính năng như duyệt sản phẩm, quản lý giỏ hàng, xử lý thanh toán, đánh giá sản phẩm và một bảng điều khiển quản trị cho việc quản lý sản phẩm.

## Công nghệ Sử dụng
- **Spring Boot**: Framework backend để xử lý các yêu cầu API và tương tác với cơ sở dữ liệu.
- **Angular**: Framework frontend để xây dựng giao diện người dùng động.
- **MySQL**: Cơ sở dữ liệu quan hệ để lưu trữ thông tin sản phẩm, người dùng và đơn hàng.
- **Redis**: Sử dụng để caching nhằm cải thiện hiệu suất.
- **Kafka**: Dùng để gửi email thông báo bất đồng bộ.
- **VNPay**: Cổng thanh toán để xử lý các giao dịch.

## Các Tính năng
- **Danh mục Sản phẩm**: Duyệt và lọc sản phẩm theo danh mục, giá và đánh giá.
- **Giỏ hàng**: Thêm sản phẩm vào giỏ hàng, thay đổi số lượng và thanh toán.
- **Cổng Thanh toán**: Xử lý thanh toán qua VNPay.
- **Đánh giá Sản phẩm**: Đánh giá sản phẩm và cung cấp phản hồi.
- **Quản trị viên**: Quản trị viên có thể thêm, sửa, xóa sản phẩm và quản lý đơn hàng.

## Cài đặt

### 1. Clone kho mã nguồn

```bash
git clone https://github.com/tinbalon3/SHOPAPP_BACKEND.git
git clone https://github.com/tinbalon3/SHOPAPP_FRONTEND.git
```

### 2. Cài đặt các thư viện frontend (Angular)

```bash
yarn install

```

### 3. Cài đặt backend (Spring Boot)
- Cấu hình `application.yml` cho MySQL, Redis và Kafka.
- File cấu hình kafka và redis nằm ở SHOPAPP_FRONTEND bạn có thể tham khảo

### 4. Cài đặt cơ sở dữ liệu
- Tạo các bảng cần thiết trong MySQL.
- Chạy migration của Flyway để thiết lập schema cơ sở dữ liệu.
- Cơ sở dữ liệu MYSQL nằm trong docker, trước khi khởi động BE thì hãy bật Docker và bật tất cả các phụ thuộc trước như 3 zookeeper, 1 broker, MYSQL, PHPMYADMIN, REDIS


### 5. File Docker
- Hãy gộp cả file shopapp_frontend và backend vào cùng 1 folder, sau đó kéo 3 file docker trong shopapp_backend ra nằm cùng cấp, trong các file đã có chú thích cách chạy
## Sử dụng

### 1. Khởi động Frontend

```bash
yarn start
```

Truy cập vào website tại `http://localhost:4200`.

### 2. Khởi động Backend
Backend có thể truy cập tại `http://localhost:8088`.
-Nếu chạy bằng docker thì:
Backend có thể truy cập tại `http://localhost:8099`.
Frontend thay đổi trong file enviroment thành 8099
## Kiểm thử

- **Kiểm thử đơn vị**: Chạy các bài kiểm thử đơn vị bằng JUnit.
- **Kiểm thử tích hợp**: Các bài kiểm thử tích hợp cho các API endpoints có sẵn trong backend.


## Lời cảm ơn
- **TS. Đỗ Như Tài** đã hướng dẫn và hỗ trợ.
- **Dương Văn Sìnl** và **Đặng Ngân Đông** đã cùng nhau phát triển và kiểm thử dự án.
- **Tác giả ** Dương Văn Sìnl: tinbalon3@gmail.com
