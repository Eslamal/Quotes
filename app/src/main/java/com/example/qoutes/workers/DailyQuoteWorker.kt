package com.example.qoutes.workers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.qoutes.R
import com.example.qoutes.repository.QuoteRepository
import com.example.qoutes.ui.QuotesActivity
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

class DailyQuoteWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Injector {
        fun getRepository(): QuoteRepository
    }

    private val injector = EntryPoints.get(context, Injector::class.java)
    private val quoteRepository = injector.getRepository()

    override suspend fun doWork(): Result {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }

        return try {
            val randomQuote = quoteRepository.getRandomQuoteForNotification()

            if (randomQuote != null) {
                val notificationIntent = Intent(context, QuotesActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val builder = NotificationCompat.Builder(
                    context,
                    context.getString(R.string.daily_notif_id)
                )
                    .setSmallIcon(R.drawable.ic_motivation)
                    .setContentTitle(context.getString(R.string.notif_title))
                    .setContentText(randomQuote.quote)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("${randomQuote.quote}\n\n- ${randomQuote.author}")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setAutoCancel(true)


                val managerCompat = NotificationManagerCompat.from(context)
                managerCompat.notify(1, builder.build())

                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}