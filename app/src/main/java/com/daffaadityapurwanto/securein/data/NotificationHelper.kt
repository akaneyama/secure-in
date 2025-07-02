package com.daffaadityapurwanto.securein.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daffaadityapurwanto.securein.MainActivity
import com.daffaadityapurwanto.securein.R

object NotificationHelper {

    private const val CHANNEL_ID = "10"
    private const val NOTIFICATION_ID = 101

    // Fungsi untuk membuat Channel Notifikasi (Wajib untuk Android 8.0 ke atas)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Password Expiration"
            val descriptionText = "Notifikasi untuk kata sandi yang akan kedaluwarsa"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(com.daffaadityapurwanto.securein.data.NotificationHelper.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Daftarkan channel ke sistem
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Fungsi untuk menampilkan notifikasi
    fun showExpirationWarningNotification(context: Context, expiringCount: Int) {
        // Intent yang akan dijalankan saat notifikasi di-klik (membuka aplikasi)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context,
            com.daffaadityapurwanto.securein.data.NotificationHelper.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.warning) // Kita akan buat ikon ini
            .setContentTitle("Peringatan Keamanan")
            .setContentText("Anda memiliki 12 kata sandi yang perlu diperbarui.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Beberapa kata sandi Anda sudah lebih dari 3 bulan. Demi keamanan, pertimbangkan untuk memperbaruinya."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Aksi saat di-klik
            .setAutoCancel(true) // Menghilangkan notifikasi setelah di-klik

        // Tampilkan notifikasi
        with(NotificationManagerCompat.from(context)) {
            // Cek lagi izin sebelum menampilkan, ini adalah praktik yang aman
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notify(com.daffaadityapurwanto.securein.data.NotificationHelper.NOTIFICATION_ID, builder.build())
            }
        }
    }
}