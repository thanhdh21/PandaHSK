package com.example.hoctiengtrung2.data.repository

import com.example.hoctiengtrung2.data.model.*
import com.example.hoctiengtrung2.data.remote.HomeRemoteDataSource

/**
 * Dữ liệu tổng hợp cần thiết để hiển thị màn hình trang chủ (Home Screen).
 *
 * Được tạo ra trong [HomeRepository.getHomeData] sau khi tổng hợp nhiều
 * nguồn dữ liệu (người dùng, cấp độ, thống kê, lịch sử) thành một
 * đối tượng duy nhất, giúp ViewModel chỉ cần quan sát một luồng dữ liệu.
 *
 * @property nguoiDung       Thông tin tài khoản của người dùng đang đăng nhập.
 * @property capDo           Cấp độ HSK hiện tại của người dùng.
 * @property tongTuCapDo     Tổng số từ vựng có trong cấp độ hiện tại.
 * @property tongTuDaHoc     Tổng số từ vư người dùng đã học qua.
 * @property tuHocHomNay     Số từ học trong ngày hôm nay.
 * @property tuHocTrongTuan  Số từ học trong tuần hiện tại.
 * @property baiHocDeXuat    Danh sách bài học gợi ý phù hợp với trình độ người dùng.
 * @property soTuCanOnTap    Số từ đến hạn ôn tập (theo thuật toán lặp lại có giãn cách - SRS).
 * @property lichSuHoc       Ánh xạ ngày (yyyy-MM-dd) -> số từ cũ học trong ngày đó.
 * @property thongKeHSK      Thống kê theo khung thời gian và cấp độ: Timeframe -> {Level -> Count}.
 * @property lichSuHoatDong  Danh sách hoạt động chi tiết (mới) thay thế cho [lichSuHoc].
 */
data class HomeData(
    val nguoiDung: NguoiDung,
    val capDo: CapDo,
    val tongTuCapDo: Int,
    val tongTuDaHoc: Int,
    val tuHocHomNay: Int,
    val tuHoc7Ngay: Int,
    val baiHocDeXuat: List<BaiHoc>,
    val soTuCanOnTap: Int,
    val soFlashcardCanOnTap: Int = 0,
    val lichSuHoc: Map<String, Int>, // yyyy-MM-dd -> count (từ cũ)
    val thongKeHSK: Map<String, Map<String, Int>>, // Timeframe -> {Level -> Count}
    val lichSuHoatDong: List<LichSuHoatDong> // Thống kê chi tiết mới
)

/**
 * Repository lấy và phối hợp dữ liệu cho màn hình Home.
 *
 * Đóng vai trò là Single Source of Truth (SSOT) giữa các ViewModel
 * và lớp dữ liệu remote ([HomeRemoteDataSource]). Mọi yêu cầu dữ liệu
 * liên quan đến trang chủ đều nên đi qua lớp này.
 *
 * @param remoteDataSource Nguồn dữ liệu remote (Firestore, API) cung cấp
 *                         các hàm fetch thô chưa qua xử lý.
 */
class HomeRepository(private val remoteDataSource: HomeRemoteDataSource) {
    /**
     * Lấy toàn bộ dữ liệu cần thiết cho trang chủ và đóng gói vào [HomeData].
     *
     * Hàm này thực hiện nhiều lời gọi song song hoặc tuần tự đến
     * [HomeRemoteDataSource] để tổng hợp dữ liệu thành một khối duy nhất.
     * Nếu người dùng không tồn tại, hàm trả về null để ViewModel
     * có thể chuyển hướng về màn hình đăng nhập.
     *
     * @param idND Định danh duy nhất của người dùng (Firestore document ID).
     * @return [HomeData] nếu lấy dữ liệu thành công, hoặc null nếu người dùng không tồn tại.
     */
    suspend fun getHomeData(idND: String): HomeData? {
        val nguoiDung = remoteDataSource.getNguoiDung(idND) ?: return null
        val capDo = remoteDataSource.getCapDo(nguoiDung.idCapDo)?: CapDo(tenCapDo = "HSK1", soLuongTu = 150)
        val baiHocDeXuat = remoteDataSource.getBaiHocDeXuat(idND)
        val thongKe = remoteDataSource.getThongKeTuVung(idND)
        val lichSu = remoteDataSource.getLichSuHoc(idND)
        val thongKeHSK = remoteDataSource.getThongKeHSK(idND)
        val lichSuMoi = remoteDataSource.getThongKeHoatDong(idND, days = 365)

        return HomeData(
            nguoiDung = nguoiDung,
            capDo = capDo,
            tongTuCapDo = capDo.soLuongTu,
            tongTuDaHoc = thongKe["tongTu"] ?: 0,
            tuHocHomNay = thongKe["tuHomNay"] ?: 0,
            tuHoc7Ngay = thongKe["tuTrongTuan"] ?: 0,
            baiHocDeXuat = baiHocDeXuat,
            soTuCanOnTap = thongKe["soTuCanOnTap"] ?: 0,
            soFlashcardCanOnTap = thongKe["soFlashcardCanOnTap"] ?: 0,
            lichSuHoc = lichSu,
            thongKeHSK = thongKeHSK,
            lichSuHoatDong = lichSuMoi
        )
    }

    /**
     * Lấy danh sách tất cả cấc độ HSK hiện có trong hệ thống.
     *
     * Được gọi bởi [CapDoViewModel] để hiển thị các thẻ chọn trình độ.
     *
     * @return Danh sách [CapDo]; rỗng nếu chưa có cấp độ nào được cấu hình.
     */
    suspend fun getTatCaCapDo(): List<CapDo> {
        return remoteDataSource.getTatCaCapDo()
    }

    /**
     * Lấy danh sách bài học được đề xuất riêng cho người dùng.
     *
     * Bài học đề xuất dựa trên cấp độ hiện tại và tiến độ học của người dùng.
     *
     * @param idND Định danh duy nhất của người dùng.
     * @return Danh sách [BaiHoc] phù hợp; rỗng nếu không có bài nào khớp.
     */
    suspend fun getBaiHocDeXuat(idND: String): List<BaiHoc> {
        return remoteDataSource.getBaiHocDeXuat(idND)
    }
    /**
     * Lấy danh sách tất cả bài học trong hệ thống (không phân biệt người dùng).
     *
     * Dùng trong các màn hình liệt kê toàn bộ bài học (ví dụ: bảng danh sách bài học).
     *
     * @return Danh sách toàn bộ [BaiHoc] trong Firestore.
     */
    suspend fun getTatCaBaiHoc(): List<BaiHoc> {
        return remoteDataSource.getTatCaBaiHoc()
    }

    /**
     * Lấy danh sách tất cả từ vựng trong hệ thống.
     *
     * Dùng để tải từ vựng xuống cho màn hình ôn tập, kiểm tra hoặc tra cứu.
     *
     * @return Danh sách toàn bộ [TuVung] trong Firestore.
     */
    suspend fun getTatCaTuVung(): List<TuVung> {
        return remoteDataSource.getTatCaTuVung()
    }

    /**
     * Lấy tiến độ học từng từ vựng của một người dùng cụ thể.
     *
     * Kết quả chứa thông tin SRS (ease factor, số lần đã xem, ngày ôn tiếp
     * theo) để thuật toán lấy ra danh sách từ cần ôn.
     *
     * @param idND Định danh duy nhất của người dùng.
     * @return Danh sách [NguoiDungTuVung] với thông tin tiến độ từng từ.
     */
    suspend fun getTienDoTuVungNguoiDung(idND: String): List<NguoiDungTuVung> {
        return remoteDataSource.getTienDoTuVungNguoiDung(idND)
    }

    /**
     * Tạo dữ liệu ảo cho 6 ngày trước đó để hiển thị biểu đồ.
     */
    suspend fun taoDuLieuMau(idND: String) {
        remoteDataSource.taoDuLieuMau(idND)
    }
}
