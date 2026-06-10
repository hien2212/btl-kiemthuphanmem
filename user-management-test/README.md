# User Management Test Suite

Bộ kiểm thử tự động cho hệ thống User Management sử dụng:
- **TestNG** — framework tổ chức và chạy test
- **RestAssured** — kiểm thử REST API
- **Selenium WebDriver** — kiểm thử giao diện UI
- **Extent Reports** — báo cáo kết quả đẹp

---

## Yêu cầu

| Thành phần | Phiên bản |
|---|---|
| Java | 21+ |
| Maven | 3.8+ |
| Google Chrome | Mới nhất |
| App đang chạy | http://localhost:8080 |

> WebDriverManager tự động tải ChromeDriver phù hợp, không cần cài tay.

---

## Cách chạy

### Bước 1: Đảm bảo app đang chạy
```bash
# Terminal 1 - chạy app
cd D:\user-management-app\user-management-app
mvn spring-boot:run
```

### Bước 2: Chạy toàn bộ bộ test
```bash
# Terminal 2 - chạy test
cd D:\user-management-test
mvn test
```

### Chạy chỉ API tests
```bash
mvn test -Dtest="AuthApiTest,UserApiTest"
```

### Chạy chỉ UI tests
```bash
mvn test -Dtest="LoginUITest,RegisterUITest,AdminUITest"
```

### Chạy 1 test class cụ thể
```bash
mvn test -Dtest="AuthApiTest"
```

---

## Xem báo cáo

Sau khi chạy xong, mở file:
```
test-output/TestReport.html
```
Báo cáo hiển thị toàn bộ kết quả: pass/fail, thời gian, mô tả từng test.

---

## Cấu trúc project

```
user-management-test/
├── pom.xml
└── src/test/
    ├── java/com/usermgmt/test/
    │   ├── base/
    │   │   └── BaseTest.java          ← Setup chung: driver, RestAssured, token helper
    │   ├── api/
    │   │   ├── AuthApiTest.java        ← Test POST /api/auth/register & /login
    │   │   └── UserApiTest.java        ← Test CRUD /api/admin/users/**
    │   └── ui/
    │       ├── LoginUITest.java        ← Test trang /login (Selenium)
    │       ├── RegisterUITest.java     ← Test trang /register (Selenium)
    │       └── AdminUITest.java        ← Test trang /admin/users + phân quyền
    └── resources/
        └── testng.xml                 ← Cấu hình thứ tự chạy test
```

---

## Danh sách test cases

### API Tests (RestAssured)

| # | Test | Mô tả |
|---|---|---|
| 1 | Register thành công | POST /api/auth/register với data hợp lệ |
| 2 | Register trùng username | Trả về 400 |
| 3 | Register trùng email | Trả về 400 |
| 4 | Register password không khớp | Trả về 400 |
| 5 | Register field trống | Trả về 400 |
| 6 | Register email sai format | Trả về 400 |
| 7 | Register password ngắn | Trả về 400 |
| 8 | Login admin thành công | Nhận JWT token |
| 9 | Login user thường | Nhận JWT với ROLE_USER |
| 10 | Login sai password | Trả về 401 |
| 11 | Login user không tồn tại | Trả về 401 |
| 12 | Login kiểm tra cấu trúc response | Có đủ token, id, role... |
| 13 | Admin lấy danh sách users | GET /api/admin/users |
| 14 | User thường bị 403 | Khi gọi admin API |
| 15 | Không có token bị 401/403 | Khi gọi protected API |
| 16 | Lấy user theo ID | GET /api/admin/users/{id} |
| 17 | Lấy user ID không tồn tại | Trả về 404 |
| 18 | Đổi role sang ADMIN | PUT /api/admin/users/{id}/role |
| 19 | Đổi role về USER | Kiểm tra thay đổi |
| 20 | Đổi role không hợp lệ | Trả về 400 |
| 21 | User thường đổi role bị 403 | Phân quyền |
| 22 | Disable user | PUT /api/admin/users/{id}/toggle |
| 23 | Enable user | Toggle lại |
| 24 | Lấy profile bản thân (user) | GET /api/users/me |
| 25 | Lấy profile bản thân (admin) | GET /api/users/me |
| 26 | Lấy profile không có token | Trả về 401/403 |
| 27 | Xoá user | DELETE /api/admin/users/{id} |
| 28 | Xoá user không tồn tại | Trả về 404 |
| 29 | User thường xoá user bị 403 | Phân quyền |

### UI Tests (Selenium)

| # | Test | Mô tả |
|---|---|---|
| 30 | Login page elements | Có đủ username, password, button |
| 31 | Login admin thành công | Redirect về dashboard |
| 32 | Login user thường | Redirect về dashboard |
| 33 | Login sai password | Hiển thị lỗi, ở lại trang login |
| 34 | Login username không tồn tại | Ở lại trang login |
| 35 | Login form trống | Không submit được |
| 36 | Login có link đến Register | Click chuyển trang đúng |
| 37 | Admin thấy menu Manage Users | Phân quyền sidebar |
| 38 | User thường không thấy menu Admin | Phân quyền sidebar |
| 39 | Register page elements | Có đủ 4 field + button |
| 40 | Register thành công | Redirect về login |
| 41 | Register trùng username | Hiển thị lỗi |
| 42 | Register trùng email | Hiển thị lỗi |
| 43 | Register password không khớp | Hiển thị lỗi |
| 44 | Register có link về Login | Click chuyển trang |
| 45 | Register xong đăng nhập được | End-to-end flow |
| 46 | Admin truy cập /admin/users | Thành công |
| 47 | User thường vào /admin/users | Bị từ chối |
| 48 | Chưa login vào /admin/users | Redirect về login |
| 49 | Bảng users có đủ cột | Username, Email, Role, Status |
| 50 | Danh sách có ít nhất 2 users | admin + user1 |
| 51 | Badge role đúng | Admin/User badge |
| 52 | Đổi role user qua UI | Admin action |
| 53 | Disable user qua UI | Admin action |
| 54 | Nút Logout hoạt động | Redirect về login |

**Tổng: 54 test cases**
