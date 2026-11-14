package pe.edu.usat.ecorutapac.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import pe.edu.usat.ecorutapac.R;

public class NotificationUtils {

    private final Context mContext;

    public NotificationUtils(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void showNotificationMessage(String title, String message, Intent intent) {
        if (TextUtils.isEmpty(message)) return;

        int icon = R.mipmap.ic_launcher;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, Config.CHANNEL_ID)
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Config.CHANNEL_ID,
                    Config.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones de rutas, alertas e incidencias");
            channel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
