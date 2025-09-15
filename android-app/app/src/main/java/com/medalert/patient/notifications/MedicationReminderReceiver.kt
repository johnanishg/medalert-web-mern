package com.medalert.patient.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.medalert.patient.MainActivity
import com.medalert.patient.MedAlertApplication
import com.medalert.patient.R

class MedicationReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicine_name") ?: "Your medication"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val instructions = intent.getStringExtra("instructions") ?: ""
        
        showNotification(context, medicineName, dosage, instructions)
    }
    
    private fun showNotification(context: Context, medicineName: String, dosage: String, instructions: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, MedAlertApplication.MEDICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_medication)
            .setContentTitle("Medication Reminder")
            .setContentText("Time to take $medicineName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Time to take $medicineName ($dosage)${if (instructions.isNotEmpty()) "\n\nInstructions: $instructions" else ""}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check,
                "Mark as Taken",
                createAdherenceIntent(context, medicineName, true)
            )
            .addAction(
                R.drawable.ic_close,
                "Mark as Missed",
                createAdherenceIntent(context, medicineName, false)
            )
            .build()
        
        notificationManager.notify(medicineName.hashCode(), notification)
    }
    
    private fun createAdherenceIntent(context: Context, medicineName: String, taken: Boolean): PendingIntent {
        val intent = Intent(context, AdherenceActionReceiver::class.java).apply {
            putExtra("medicine_name", medicineName)
            putExtra("taken", taken)
        }
        
        return PendingIntent.getBroadcast(
            context,
            (medicineName + taken.toString()).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}