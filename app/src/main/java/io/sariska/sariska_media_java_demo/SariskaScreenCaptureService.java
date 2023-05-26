package io.sariska.sariska_media_java_demo;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import java.util.Objects;

public class SariskaScreenCaptureService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("We are inside on start command");
        Pair<Integer, Notification> notification = NotificationUtils.getNotification(this);
        startForeground(notification.first, notification.second);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
