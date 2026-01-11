package com.example.qoutes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import com.example.qoutes.util.JsonDataLoader
import com.example.qoutes.workers.DailyQuoteWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class QuotesApplication : Application() {

    @Inject
    lateinit var dataLoader: JsonDataLoader

    override fun onCreate() {
        super.onCreate()

        dataLoader.loadDataIfNeeded()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                this.getString(R.string.daily_notif_id),
                this.getString(R.string.daily_notif_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            channel.description = this.getString(R.string.daily_notif_desc)

            notificationManager.createNotificationChannel(channel)
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequest
            .Builder(DailyQuoteWorker::class.java, 1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                getString(R.string.daily_notif_id),
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }
}