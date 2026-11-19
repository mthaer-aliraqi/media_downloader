package com.example.clipnest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.yausername.youtubedl_android.YoutubeDL;

public class DownloadForegroundService extends Service {
    public static final int NOTIFICATION_ID = 7001;
    public static final String CHANNEL_ID = "clipnest_downloads";
    public static final String ACTION_CANCEL_DOWNLOAD = "com.example.clipnest.CANCEL_DOWNLOAD";
    public static final String EXTRA_PROCESS_ID = "process_id";
    private String processId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_CANCEL_DOWNLOAD.equals(intent.getAction())) {
            String processId = intent.getStringExtra(EXTRA_PROCESS_ID);
            if (processId != null && !processId.trim().isEmpty()) {
                new Thread(() -> YoutubeDL.getInstance().destroyProcessById(processId), "clipnest-notification-cancel").start();
            }
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (intent != null) processId = intent.getStringExtra(EXTRA_PROCESS_ID);
        createChannel();
        startForeground(NOTIFICATION_ID, buildNotification("Download running · 0%", 0));
        return START_NOT_STICKY;
    }

    private void createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "ClipNest downloads", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Download progress and cancellation controls");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String message, int progress) {
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID + 1, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent cancelIntent = new Intent(this, DownloadForegroundService.class);
        cancelIntent.setAction(ACTION_CANCEL_DOWNLOAD);
        cancelIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        cancelIntent.putExtra(EXTRA_PROCESS_ID, processId);
        PendingIntent cancelPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID + 2, cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("ClipNest download")
                .setContentText(message)
                .setContentIntent(openPendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setProgress(100, Math.max(0, Math.min(100, progress)), false)
                .addAction(new Notification.Action.Builder(null, "Cancel", cancelPendingIntent).build())
                .build();
    }

    @Override
    public void onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
