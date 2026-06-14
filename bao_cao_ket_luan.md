# Báo cáo kết thúc dự án: Ứng dụng LearnFlash
## Kết luận, Hạn chế và Hướng phát triển (Đã hiệu chỉnh & Cập nhật logic Thống kê)

---

### I. KẾT LUẬN (CONCLUSION)

Sau một thời gian nghiêm túc nghiên cứu và phát triển, nhóm đã hoàn thành toàn bộ các mục tiêu đặt ra cho dự án **LearnFlash**. Các kết quả cụ thể đạt được bao gồm:

#### 1. Về sản phẩm ứng dụng
* **Ứng dụng di động LearnFlash:** Xây dựng thành công ứng dụng hoạt động mượt mà trên nền tảng Android.
* **Giao diện hiện đại:** Toàn bộ logic giao diện được thiết kế hiện đại bằng công nghệ Jetpack Compose chuẩn Material Design 3, đem lại trải nghiệm trực quan, mượt mà và nhất quán.

#### 2. Về mặt kỹ thuật và tích hợp
* **Đồng bộ hóa dữ liệu thời gian thực (Real-time Cloud Database):** Tích hợp dịch vụ Cloud Firestore của Firebase để tải cơ sở dữ liệu mẫu khi khởi chạy lần đầu và lưu trữ dữ liệu cá nhân của người dùng (từ vựng, Flashcard cá nhân, mục tiêu, chuỗi ngày học streak) đồng bộ trên đám mây.
* **Cải tiến cơ chế thống kê từ đã học (Atomic Daily Stats):** Triển khai logic thống kê số từ mới học trong ngày (`LichSuHoatDong`) một cách chặt chẽ. Từ vựng chỉ được tính là đã học khi người dùng **trả lời trắc nghiệm đúng ít nhất 1 lần** (thay vì tính ngay khi xem thẻ Flashcard). Tiến trình này được xử lý nguyên tử (atomic) thông qua Firestore Transaction, giúp tránh hoàn toàn hiện tượng đếm trùng lặp (double counting).
* **Quản lý phiên và cấu hình cục bộ (Local Session Management):** Khởi tạo `SessionManager` sử dụng `SharedPreferences` để lưu trữ và duy trì các trạng thái cơ bản cục bộ như thông tin tài khoản người dùng (`idNguoiDung`) và cài đặt giao diện Sáng/Tối (Dark Mode) xuyên suốt ứng dụng.
* **Làm giàu thông tin với Retrofit:** Tích hợp thành công thư viện Retrofit để kết nối với các REST API dịch thuật và tra cứu trực tuyến (bao gồm từ điển Hán-Việt HVDict, câu ví dụ Tatoeba và dữ liệu ThiVien), hỗ trợ tra cứu song song để hiển thị đầy đủ thông tin từ vựng.
* **Hệ thống xác thực an toàn:** Sử dụng Firebase Authentication phục vụ cho các chức năng đăng ký, đăng nhập, đổi mật khẩu và bảo vệ thông tin cá nhân của người học.

#### 3. Kiến thức và kỹ năng đạt được
* **Kỹ năng chuyên môn:** Các thành viên trong nhóm đã nắm vững quy trình phát triển ứng dụng di động Android hiện đại, hiểu sâu về kiến trúc MVVM (Model-View-ViewModel), sử dụng thành thạo các thư viện Jetpack nâng cao (Navigation, State/Flow) và làm việc trực tiếp với nền tảng đám mây Firebase.
* **Kỹ năng mềm:** Củng cố các kỹ năng quan trọng bao gồm làm việc nhóm hiệu quả, quản lý mã nguồn chuyên nghiệp bằng Git/GitHub và kỹ năng viết tài liệu báo cáo kỹ thuật.

---

### II. HẠN CHẾ (LIMITATIONS)

Dù đã đạt được các mục tiêu cốt lõi, dự án LearnFlash vẫn tồn tại một số hạn chế kỹ thuật sau:

#### 1. Sự lệ thuộc vào kết nối Internet (Online Dependency)
* **Chưa hỗ trợ học ngoại tuyến (Offline Mode):** Ứng dụng chưa sử dụng cơ sở dữ liệu cục bộ (như Room Database). Mọi thao tác tải bài học đề xuất, lưu từ vựng mới, cập nhật tiến độ hay tạo Flashcard cá nhân đều yêu cầu thiết bị phải trực tuyến để kết nối với Cloud Firestore.
* **Trải nghiệm tra cứu bị ảnh hưởng khi mạng yếu:** Do làm giàu thông tin từ vựng thông qua các REST API ngoài qua Retrofit và hiển thị dữ liệu trực tiếp từ đám mây, nếu đường truyền Internet không ổn định hoặc các API bên thứ ba gặp sự cố ngắt kết nối, người dùng sẽ không thể tra cứu hoặc lưu từ vựng.

#### 2. Giới hạn lưu trữ cục bộ
* **SharedPreferences hạn chế:** Bộ nhớ SharedPreferences hiện tại chỉ phù hợp để lưu trữ các giá trị cấu hình nhỏ gọn (User ID, trạng thái Dark Mode). Ứng dụng chưa có bộ đệm (cache) cục bộ cho nội dung bài học, nghĩa là mỗi lần mở màn hình bài học hoặc tra từ cũ, ứng dụng đều phải gửi yêu cầu mạng mới lên Firestore/REST API.

#### 3. Hạn chế về tính năng học tập
* **Thiếu âm thanh phát âm:** Ứng dụng chưa hỗ trợ phát âm mẫu (Text-to-Speech) đối với từ vựng mới và câu ví dụ, hạn chế khả năng học nghe/nói của người dùng.
* **Dạng bài tập kiểm tra còn đơn giản:** Hệ thống trắc nghiệm chủ yếu xoay quanh việc nhận diện nghĩa từ vựng, chưa có các dạng bài nâng cao như sắp xếp câu hoặc điền từ vào ô trống.

---

### III. HƯỚNG PHÁT TRIỂN (FUTURE DIRECTIONS)

Để hoàn thiện LearnFlash thành một sản phẩm toàn diện, định hướng phát triển trong tương lai bao gồm:

#### 1. Triển khai kiến trúc Offline-First với Room Database
* **Tích hợp Room Database:** Xây dựng cơ sở dữ liệu Room cục bộ để lưu trữ và tải bộ đệm (cache) bài học, từ vựng và lịch sử học tập trên thiết bị.
* **Đồng bộ hóa bất đồng bộ hai chiều (Two-Way Sync):** Sử dụng `WorkManager` phối hợp với Room DB để hỗ trợ người dùng học tập hoàn toàn ngoại tuyến. Khi có kết nối mạng trở lại, ứng dụng sẽ tự động đồng bộ hóa tiến độ, từ vựng tự tạo và streak từ Room lên Firebase Firestore mà không làm gián đoạn trải nghiệm học.

#### 2. Tối ưu hóa hiệu năng và bộ đệm (Caching) cho REST API
* **Bộ đệm thông tin làm giàu:** Lưu trữ lại kết quả trả về từ các REST API ngoài thông qua Retrofit vào một bảng cache trong Room DB. Nhờ đó, người dùng có thể xem lại đầy đủ thông tin chi tiết của từ vựng đã tra cứu trước đó ngay cả khi không có mạng.
* **Xử lý luồng tối ưu:** Sử dụng các cơ chế quản lý dữ liệu bất đồng bộ nâng cao để giảm tối đa thời gian chờ đợi tải dữ liệu.

#### 3. Nâng cấp trải nghiệm học tập tương tác đa phương tiện
* **Tích hợp Text-to-Speech (TTS):** Sử dụng thư viện TTS của Google hoặc API phát âm để cung cấp âm thanh đọc mẫu chuẩn xác cho từng từ vựng và câu ví dụ.
* **Hỗ trợ vẽ nét chữ Hán/Ký tự đặc biệt:** Tích hợp thư viện đồ họa cho phép hướng dẫn thứ tự viết nét và hỗ trợ vẽ tay trên màn hình cảm ứng để ghi nhớ mặt chữ tốt hơn.
* **Trò chơi hóa (Gamification):** Phát triển thêm các dạng bài tập trắc nghiệm tương tác mới, hệ thống điểm thưởng và chuỗi ngày học (streak) đồng bộ đám mây để kích thích người học duy trì thói quen học tập.

---

### IV. CÁC SƠ ĐỒ LUỒNG HOẠT ĐỘNG VÀ THUẬT TOÁN HỆ THỐNG

Dưới đây là các sơ đồ luồng hoạt động cốt lõi của ứng dụng nhằm mô tả chi tiết quy trình nghiệp vụ và thuật toán lưu trữ:

#### 1. Sơ đồ Luồng học bài mới
*   **Liên kết sơ đồ**: [sodo_flashcard.svg](sodo_flashcard.svg)
*   **Mô tả**: Quy trình học từ mới kiểm soát chặt chẽ việc tiếp thu và khởi tạo tiến trình từ vựng dựa trên kết quả kiểm tra trắc nghiệm của người học. Khi người học hoàn thành Flashcard và làm trắc nghiệm, nếu trả lời đúng (`laDung = true`), hệ thống cập nhật trạng thái đã thuộc (`daHoc = true`), áp dụng thuật toán SM-2 để dời ngày ôn tập tiếp theo ra xa, đồng thời cộng dồn số từ học mới trong ngày (`soTuMoi` và `tongtudahoc` thông qua Firestore Transaction). Ngược lại nếu trả lời sai (`laDung = false`), hệ thống coi như từ vựng đó chưa được học (`daHoc = false`) và người học phải thực hiện học lại từ đó (không lưu lịch ôn tập).

#### 2. Sơ đồ Luồng ôn tập từ vựng
*   **Liên kết sơ đồ**: [sodo_ontap.svg](sodo_ontap.svg)
*   **Mô tả**: Thuật toán Spaced Repetition System (SRS) kiểm soát chặt chẽ tần suất lặp lại từ vựng dựa trên kết quả kiểm tra trắc nghiệm của người học. Khi người học ôn tập, nếu trả lời đúng (`laDung = true`), hệ thống tăng số lần lặp lại (`repetitions`) thêm 1 đơn vị và lùi ngày ôn tập tiếp theo ra xa dựa trên khoảng cách ôn tập mới (`interval` được tính toán dựa trên hệ số dễ EF). Ngược lại nếu trả lời sai (`laDung = false`), hệ thống lập tức reset số lần lặp lại (`repetitions`) về 0, đặt khoảng cách ôn tập mặc định là 1 ngày và lên lịch ôn tập lại vào ngày mai (Ngày hiện tại + 24 giờ) để củng cố lại trí nhớ.

#### 3. Sơ đồ Luồng ôn tập trắc nghiệm SRS
*   **Liên kết sơ đồ**: [sodo_tracnghiem_srs.svg](sodo_tracnghiem_srs.svg)
*   **Mô tả**: Quy trình trắc nghiệm ôn tập SRS kiểm soát chặt chẽ việc đánh giá kiến thức và cập nhật tham số Spaced Repetition của người học. Khi người học trả lời câu hỏi trắc nghiệm, nếu chọn đáp án đúng (`laDung = true`), hệ thống tăng số lần lặp lại, áp dụng thuật toán SM-2 để cập nhật khoảng cách ôn tập dài hơn và hẹn lịch ôn tập tiếp theo. Ngược lại nếu trả lời sai (`laDung = false`), hệ thống sẽ reset số lần lặp lại về 0, giảm hệ số dễ và tự động lên lịch ôn tập lại vào ngày mai nhằm củng cố kịp thời trí nhớ cho người học.
