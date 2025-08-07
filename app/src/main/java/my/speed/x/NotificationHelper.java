package my.speed.x;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
	private static final String CHANNEL_ID = "SpeedometerChannel";
	private static final int NOTIFICATION_ID = 1;
	private Context context;
	private NotificationManager notificationManager;
	
	public NotificationHelper(Context context) {
		this.context = context;
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		createNotificationChannel();
	}
	
	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
			CHANNEL_ID,
			"Speedometer Service",
			NotificationManager.IMPORTANCE_LOW
			);
			channel.setDescription("Shows current speed information");
			notificationManager.createNotificationChannel(channel);
		}
	}
	
	public Notification buildNotification(String speed) {
		Intent notificationIntent = new Intent(context, MainActivity.class);
		// Use NEW_TASK and SINGLE_TOP to ensure the existing instance is re-used.
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(
		context,
		0,
		notificationIntent,
		PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
		.setContentTitle("Speedometer Active")
		.setContentText("Current Speed: " + speed)
		.setSmallIcon(android.R.drawable.ic_menu_compass)
		.setOngoing(true)
		.setContentIntent(pendingIntent);
		
		return builder.build();
	}
	
	public void updateNotification(String speed) {
		Notification notification = buildNotification(speed);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	public static int getNotificationId() {
		return NOTIFICATION_ID;
	}
}