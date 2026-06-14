package com.example.hoctiengtrung2.utils

import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Date

data class SM2State(
    val repetitions: Int,
    val interval: Int,
    val easinessFactor: Double,
    val ngayOnTapTiepTheo: Timestamp
)

object SM2Algorithm {
    fun calculate(
        laDung: Boolean,
        prevRepetitions: Int,
        prevInterval: Int,
        prevEasinessFactor: Double
    ): SM2State {
        // Ánh xạ kết quả Đúng/Sai sang điểm Chất lượng (Quality - q từ 0 đến 5)
        // 4: Nhớ đúng sau một chút phân vân (laDung = true)
        // 1: Trả lời sai nhưng khi nhìn đáp án thì nhớ ra (laDung = false)
        val quality = if (laDung) 4 else 1

        val repetitions: Int
        val interval: Int
        var easinessFactor = prevEasinessFactor

        if (quality >= 3) {
            repetitions = prevRepetitions + 1
            interval = when (repetitions) {
                1 -> 1
                2 -> 6
                else -> Math.round(prevInterval * easinessFactor).toInt()
            }
        } else {
            repetitions = 0
            interval = 1
        }

        // Cập nhật hệ số dễ (easiness factor - EF) theo công thức SM-2
        easinessFactor = easinessFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        if (easinessFactor < 1.3) {
            easinessFactor = 1.3
        }

        // Tính ngày ôn tập tiếp theo
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, interval)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val ngayOnTapTiepTheo = Timestamp(calendar.time)

        return SM2State(repetitions, interval, easinessFactor, ngayOnTapTiepTheo)
    }
}
