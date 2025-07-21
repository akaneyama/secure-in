package com.daffaadityapurwanto.securein.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.daffaadityapurwanto.securein.MainDashboard
import com.daffaadityapurwanto.securein.R

object NotificationHelper {

    // --- Channel & ID untuk Notifikasi Peringatan Kadaluwarsa ---
    private const val EXPIRATION_CHANNEL_ID = "expiration_channel"
    private const val EXPIRATION_NOTIFICATION_ID = 101

    // --- Channel & ID untuk Notifikasi Backup & Restore ---
    private const val BACKUP_CHANNEL_ID = "backup_channel"
    private const val BACKUP_NOTIFICATION_ID = 102

    /**
     * Membuat Channel Notifikasi. Wajib untuk Android 8.0 (Oreo) ke atas.
     * Fungsi ini aman dipanggil berkali-kali, sistem akan mengabaikannya jika channel sudah ada.
     */
    private fun createNotificationChannel(context: Context, channelId: String, channelName: String, channelDescription: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Menampilkan notifikasi untuk kata sandi yang akan kedaluwarsa.
     */
    fun showExpirationWarningNotification(context: Context, expiringCount: Int) {
        // Buat channel-nya dulu, pastikan sudah ada
        createNotificationChannel(
            context,
            EXPIRATION_CHANNEL_ID,
            "Peringatan Password",
            "Notifikasi untuk kata sandi yang akan kedaluwarsa"
        )

        val intent = Intent(context, MainDashboard::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Gunakan expiringCount untuk membuat teks dinamis
        val contentText = "Anda memiliki $expiringCount kata sandi yang perlu diperbarui."
        val bigText = "Beberapa kata sandi Anda sudah lebih dari 3 bulan. Demi keamanan, pertimbangkan untuk memperbaruinya."

        val builder = NotificationCompat.Builder(context, EXPIRATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.warning)
            .setContentTitle("Peringatan Keamanan")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        showNotification(context, EXPIRATION_NOTIFICATION_ID, builder)
    }

    /**
     * Menampilkan notifikasi untuk status proses backup atau restore.
     */
    fun showBackupNotification(context: Context, status: String, action: String = "Backup") {
        // Buat channel-nya dulu
        createNotificationChannel(
            context,
            BACKUP_CHANNEL_ID,
            "Status Sinkronisasi",
            "Notifikasi untuk status proses backup dan restore"
        )

        val title: String
        val content: String

        // Tentukan judul dan isi notifikasi berdasarkan status
        if (status.equals("Success", ignoreCase = true)) {
            title = "$action Berhasil"
            content = "Data Anda telah berhasil di-$action ke server."
        } else {
            title = "$action Gagal"
            content = "Terjadi kesalahan saat mencoba melakukan $action data."
        }

        val builder = NotificationCompat.Builder(context, BACKUP_CHANNEL_ID)
            .setSmallIcon(if (status.equals("Success", ignoreCase = true)) R.drawable.suksesbro else R.drawable.warning)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        showNotification(context, BACKUP_NOTIFICATION_ID, builder)
    }

    /**
     * Fungsi terpusat untuk menampilkan notifikasi setelah memeriksa izin.
     */
    private fun showNotification(context: Context, notificationId: Int, builder: NotificationCompat.Builder) {
        with(NotificationManagerCompat.from(context)) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(notificationId, builder.build())
            }
        }
    }
}