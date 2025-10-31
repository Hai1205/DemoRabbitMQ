# RabbitMQ Topic Demo with Spring Boot

Dự án này là một demo minh họa cách sử dụng RabbitMQ với Spring Boot để triển khai hai mô hình giao tiếp: **RPC (Request-Response)** và **Event-Driven (Publish-Subscribe)**. Dự án mô phỏng quy trình đăng nhập (login) trong một hệ thống microservices.

## Kiến trúc tổng quan

Dự án bao gồm 4 microservices:

- **auth**: Service xử lý đăng nhập, gửi request RPC và publish event.
- **user**: Service xử lý xác thực người dùng (RPC consumer).
- **mailer**: Service gửi email chào mừng (event consumer).
- **notifier**: Service gửi thông báo (event consumer).

Các service giao tiếp qua RabbitMQ sử dụng topic exchange.

## Cài đặt và chạy

### Yêu cầu

- Docker và Docker Compose
- Java 21+
- Maven

### Chạy dự án

```bash
docker-compose up --build -d
```

Các service sẽ chạy trên các port:

- auth: 8081
- user: 8082
- mailer: 8083
- notifier: 8084
- RabbitMQ Management: 15672 (user: guest, pass: guest)

### Test đăng nhập

```bash
curl -s -X POST -H "Content-Type: application/json" -d '{\"email\":\"hai@example.com\",\"password\":\"password123\"}' http://localhost:8081/login
```

## Phần 1: RPC (Request-Response) - 1-1

### Mô tả

Trong mô hình RPC, auth service gửi một request xác thực đến user service và chờ nhận response. Đây là giao tiếp đồng bộ (synchronous) giữa hai service.

### Luồng hoạt động chi tiết

1. **Client gửi request**: Client gửi POST request đến `/login` của auth service với JSON body chứa email và password.

2. **AuthController xử lý**: `AuthController.login()` nhận `AuthRequest` và gọi `authProducer.authenticate(req)` trong một `CompletableFuture` để xử lý bất đồng bộ.

3. **AuthProducer gửi RPC request**:

   - `AuthProducer.authenticate()` sử dụng `RabbitTemplate.convertSendAndReceive()` để gửi message.
   - **Exchange**: `auth.user.exchange` (topic).
   - **Routing Key**: `auth.user.authenticate.request`.
   - **Reply Queue**: Không cần khởi tạo thủ công. Spring AMQP và RabbitMQ tự động tạo một reply queue tạm thời (temporary, exclusive) cho mỗi RPC request. Queue này sẽ bị xóa sau khi response được nhận hoặc timeout.
   - Message body: `AuthRequest` object (được convert sang JSON bởi `Jackson2JsonMessageConverter`).
   - Method chờ response với timeout 120 giây (cấu hình trong `RabbitTemplate`).

4. **User service nhận và xử lý**:

   - `UserListener.handleAuth()` được khai báo với `@RabbitListener`:
     - **Queue**: `auth.user.user-service.queue` (durable=true, auto-declared).
     - **Exchange**: `auth.user.exchange` (topic).
     - **Binding Key**: `auth.user.authenticate.request` (chỉ nhận message với routing key chính xác này).
   - Delay 15 giây để mô phỏng xử lí: User service kiểm tra credentials.
   - Nếu thành công, trả về `UserDto`.
   - Nếu thất bại, trả về `null`.

5. **Auth service nhận response**:

   - `convertSendAndReceive()` nhận `UserDto` hoặc `null` từ reply queue (auto-managed bởi RabbitMQ).
   - Nếu response != null, gọi `authProducer.publishLoginSuccess(user)` và trả về HTTP 200 với UserDto.
   - Nếu response == null, trả về HTTP 401 "Invalid credentials".

6. **Client nhận kết quả**: Client nhận response từ auth service (UserDto hoặc error).

### Đặc điểm

- **Đồng bộ**: Auth service chờ response từ user service (thời gian chờ tối đa là 120s).
- **1-1**: Một request chỉ được xử lý bởi một consumer (user service).
- **Blocking**: Client phải chờ toàn bộ quá trình (bao gồm cả delay 15 giây trong user service).

## Phần 2: Event-Driven (Publish-Subscribe) - 1-N

### Mô tả

Sau khi đăng nhập thành công, auth service publish một event để thông báo cho các service khác. Đây là giao tiếp bất đồng bộ (asynchronous) theo mô hình publish-subscribe.

### Luồng hoạt động chi tiết

1. **Sau khi RPC thành công**: Auth service nhận `UserDto` từ user service, gọi `authProducer.publishLoginSuccess(user)`.

2. **AuthProducer publish event**:

   - `AuthProducer.publishLoginSuccess()` sử dụng `RabbitTemplate.convertAndSend()` để gửi message (fire-and-forget).
   - **Exchange**: `auth.authenticate.exchange` (topic type, auto-declared bởi Spring Boot).
   - **Routing Key**: `auth.authenticate.login.success`.
   - Message body: `UserDto` object (convert sang JSON).
   - Không chờ response, method return ngay lập tức.

3. **Mailer service subscribe event**:

   - `MailListener.handle()` được khai báo với `@RabbitListener`:
     - **Queue**: `auth.authenticate.mailer-service.queue` (durable=true, auto-declared).
     - **Exchange**: `auth.authenticate.exchange` (topic).
     - **Binding Key**: `auth.authenticate.login.*` (nhận tất cả routing key bắt đầu bằng `auth.authenticate.login.`).
   - Delay 15 giây để mô phỏng gửi email tới user vừa đăng nhập.

4. **Notifier service nhận event**:

   - `NotifyListener.handle()` được khai báo với `@RabbitListener`:
     - **Queue**: `auth.authenticate.notifier-service.queue` (durable=true, auto-declared).
     - **Exchange**: `auth.authenticate.exchange` (topic).
     - **Binding Key**: `auth.authenticate.#` (nhận tất cả routing key bắt đầu bằng `auth.authenticate.`).
   - Delay 15 giây để mô phỏng gửi thông báo tới user vừa đăng nhập.

5. **Xử lý song song**: Mailer và Notifier xử lý event độc lập và song song, không ảnh hưởng lẫn nhau.

### Đặc điểm

- **Bất đồng bộ**: Auth service không chờ response, tiếp tục xử lý ngay lập tức.
- **1-N**: Một event được xử lý bởi nhiều consumer (mailer và notifier) song song.
- **Non-blocking**: Client nhận response ngay sau RPC, các event được xử lý nền (background).
- **Decoupling**: Auth service không biết về mailer/notifier, chỉ publish event.

## RabbitMQ Configuration

### Auto-Declaration bởi Spring Boot

Spring Boot tự động khai báo exchanges, queues và bindings dựa trên:

- `@RabbitListener` annotations trong consumer services.
- Việc sử dụng exchange names trong `RabbitTemplate.convertSendAndReceive()` hoặc `convertAndSend()`.

Khi service start, Spring AMQP sẽ tạo:

- Exchanges nếu chưa tồn tại.
- Queues nếu chưa tồn tại.
- Bindings giữa queues và exchanges với routing keys được chỉ định.

### Exchanges

- `auth.user.exchange`: Topic exchange cho RPC (xác thực user).
- `auth.authenticate.exchange`: Topic exchange cho events (đăng nhập thành công).

### Queues

- `auth.user.user-service.queue`: Queue cho user service xử lý RPC.
- `auth.authenticate.mailer-service.queue`: Queue cho mailer service.
- `auth.authenticate.notifier-service.queue`: Queue cho notifier service.

### Routing Keys

- `auth.user.authenticate.request`: Cho RPC request.
- `auth.authenticate.login.success`: Cho event login success.

## Demo Users

- hai@example.com / password123
- alice@example.com / alicepwd

## Lưu ý

- Các service có delay 15 giây để mô phỏng xử lý thực tế.
- Sử dụng DeferredResult để xử lý async trong Spring MVC.
- RabbitMQ management UI có thể truy cập tại http://localhost:15672
