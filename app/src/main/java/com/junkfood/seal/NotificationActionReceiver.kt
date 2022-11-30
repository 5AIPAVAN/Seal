package com.junkfood.seal

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.util.NotificationUtil
import com.junkfood.seal.util.TextUtil
import com.yausername.youtubedl_android.YoutubeDL

class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "CancelReceiver"
        const val ACTION_CANCEL_TASK = 0
        const val ACTION_ERROR_REPORT = 1
        const val ACTION_KEY = "action"
        const val TASK_ID_KEY = "taskId"
        const val NOTIFICATION_ID_KEY = "notificationId"
        const val ERROR_REPORT_KEY = "error_report"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        val notificationId = intent.getIntExtra(NOTIFICATION_ID_KEY, 0)

        when (intent.getIntExtra(ACTION_KEY, ACTION_CANCEL_TASK)) {
            ACTION_CANCEL_TASK -> {
                val taskId = intent.getStringExtra(TASK_ID_KEY)
                cancelTask(taskId, notificationId)
            }

            ACTION_ERROR_REPORT -> {
                val errorReport = intent.getStringExtra(ERROR_REPORT_KEY)
                if (!errorReport.isNullOrEmpty())
                    copyErrorReport(errorReport, notificationId)
            }
        }
    }

    private fun cancelTask(taskId: String?, notificationId: Int) {
        if (taskId.isNullOrEmpty()) return
        val result = YoutubeDL.getInstance().destroyProcessById(taskId)
        if (result) {
            Log.d(TAG, "Task (id:$taskId) was killed.")
            NotificationUtil.cancelNotification(notificationId)
        }
    }

    private fun copyErrorReport(error: String, notificationId: Int) {
        context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(
            ClipData.newPlainText(
                context.getString(R.string.copy_error_report),
                error
            )
        )
        context.let { TextUtil.makeToastSuspend(it.getString(R.string.error_copied)) }
        NotificationUtil.cancelNotification(notificationId)
    }
}