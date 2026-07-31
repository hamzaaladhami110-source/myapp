package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MonthlyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "com.example.ACTION_MONTHLY_REMINDER") {
            NotificationHelper.createNotificationChannels(context)
            NotificationHelper.showMonthlySalaryReminder(context)
            // Schedule for next month again
            NotificationHelper.scheduleNextMonthlyReminder(context)
        }
    }
}
