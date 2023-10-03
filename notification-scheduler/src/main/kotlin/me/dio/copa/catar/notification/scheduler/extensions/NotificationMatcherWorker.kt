package me.dio.copa.catar.notification.scheduler.extensions

import android.content.Context
import android.provider.MediaStore
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import me.dio.copa.catar.domain.model.MatchDomain
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.LocalDateTime

private const val NOTIFICATION_TITLE_KEY = "NOTIFICATION_TITLE_KEY"
private const val NOTIFICATION_CONTENT_KEY = "NOTIFICATION_CONTENT_KEY"

class NotificationMatcherWorker(private val context:Context, workParameters: WorkerParameters) : Worker(context, workParameters) {
    override fun doWork(): Result {
        val title = inputData.getString(NOTIFICATION_TITLE_KEY) ?: throw IllegalArgumentException("title is required")
        val content = inputData.getString(NOTIFICATION_CONTENT_KEY) ?: throw IllegalArgumentException("content is required")

        context.showNotification(title, content)

        return Result.success()
    }

    companion object{
        fun start(context:Context, match: MatchDomain){

            val (id, _, _, team1, team2, matchDate) = match

            val initDelay = Duration.between(LocalDateTime.now(), matchDate).minusMinutes(5)
            val inputData = workDataOf(
                NOTIFICATION_CONTENT_KEY to "Se prepare que o jogo vai começar",
                NOTIFICATION_CONTENT_KEY to "Hoje Tem ${team1.flag} x ${team2.flag}"
            )
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    id,
                    ExistingWorkPolicy.KEEP,
                    createRequest(initDelay, inputData)
                )
        }

        fun cancel(context: Context, match:MatchDomain){
            WorkManager.getInstance(context)
                .cancelUniqueWork(match.id)
        }

        fun createRequest(initiDelay: Duration, inputData: Data):OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<NotificationMatcherWorker>()
                .setInitialDelay(initiDelay)
                .setInputData(inputData)
                .build()
    }
}