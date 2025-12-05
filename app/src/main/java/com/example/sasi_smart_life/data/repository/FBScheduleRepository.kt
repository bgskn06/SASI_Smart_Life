package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.example.sasi_smart_life.data.models.Schedule
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FBScheduleRepository {
    // Menggunakan Firebase Realtime Database
    private val db = FirebaseDatabase.getInstance(
        "https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference

    val tag = "FBScheduleRepo_SASI"

    suspend fun updateDeviceSchedule(
        roomId: String,
        deviceId: String,
        day: String,
        schedule: Schedule
    ) {
        if (roomId.isBlank()) {
            Log.e(tag, "Room ID kosong. Tidak dapat memperbarui jadwal.")
            return
        }

        try {
            val schedulePath = db.child("status")
                .child(roomId)
                .child("schedule")
                .child(day)
                .child(deviceId)

            schedulePath.setValue(schedule).await()
            Log.d(tag, "Berhasil memperbarui jadwal untuk perangkat $deviceId pada hari $day.")
        } catch (e: Exception) {
            Log.e(tag, "Error saat memperbarui jadwal", e)
            throw e
        }
    }

    /**
     * Menghapus semua entri jadwal untuk perangkat tertentu dari semua hari.
     * Fungsi ini harus dipanggil saat perangkat di-unlink atau dihapus.
     */
    suspend fun deleteAllSchedulesForDevice(roomId: String, deviceId: String) {
        if (roomId.isBlank()) {
            Log.e(tag, "Room ID kosong. Tidak dapat menghapus jadwal.")
            return
        }
        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        try {
            // Di Realtime Database, kita melakukan update massal dengan membuat map
            val updates = mutableMapOf<String, Any?>()

            daysOfWeek.forEach { day ->
                // Path ke entri jadwal perangkat untuk hari tertentu
                val schedulePath = "/status/$roomId/schedule/$day/$deviceId"
                // Menyetel nilainya menjadi null akan menghapusnya dari database
                updates[schedulePath] = null
            }

            // Menjalankan semua penghapusan dalam satu operasi atomik
            db.updateChildren(updates).await()

            Log.d(tag, "Berhasil menghapus semua jadwal untuk perangkat $deviceId.")
        } catch (e: Exception) {
            Log.e(tag, "Error saat menghapus jadwal untuk perangkat $deviceId", e)
        }
    }
}
