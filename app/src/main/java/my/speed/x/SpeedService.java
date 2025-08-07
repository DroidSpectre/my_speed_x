package my.speed.x;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class SpeedService extends Service {
	private LocationManager locationManager;
	private LocationListener locationListener;
	private NotificationHelper notificationHelper;
	
	private static final int MIN_TIME_MS = 1000;
	private static final float MIN_DISTANCE_M = 0;
	
	// Static variables to hold the trip state so that the Activity can retrieve
	// them when it restarts.
	public static boolean serviceActive = false;
	public static long tripStartTime = -1;
	public static double totalDistanceKm = 0;
	public static double maxSpeed = 0;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		notificationHelper = new NotificationHelper(this);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				double speedKph = location.getSpeed() * 3.6;
				
				// Initialize tripStartTime if it hasn't been set yet.
				if (tripStartTime < 0) {
					tripStartTime = System.currentTimeMillis();
				}
				
				// Update total distance. Assuming the update is roughly every second,
				// we add (speedKph / 3600) kilometers.
				totalDistanceKm += speedKph / 3600;
				if (speedKph > maxSpeed) {
					maxSpeed = speedKph;
				}
				
				// Update the notification with current speed converted to mph.
				double speedMph = speedKph * 0.621371;
				String speedText = String.format("%.2f mph", speedMph);
				notificationHelper.updateNotification(speedText);
				
				// Broadcast update to any listening activity.
				Intent intent = new Intent(MainActivity.ACTION_SPEED_UPDATE);
				intent.putExtra("speedKph", speedKph);
				intent.putExtra("totalDistanceKm", totalDistanceKm);
				intent.putExtra("maxSpeed", maxSpeed);
				// Pass the current latitude and longitude for reverse geocoding.
				intent.putExtra("latitude", location.getLatitude());
				intent.putExtra("longitude", location.getLongitude());
				sendBroadcast(intent);
			}
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) { }
			
			@Override
			public void onProviderEnabled(String provider) { }
			
			@Override
			public void onProviderDisabled(String provider) {
				Toast.makeText(SpeedService.this, "Please enable GPS", Toast.LENGTH_SHORT).show();
			}
		};
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		serviceActive = true;
		// Start the service in the foreground with an initial speed of 0.
		startForeground(
		NotificationHelper.getNotificationId(),
		notificationHelper.buildNotification("0.00 mph")
		);
		
		try {
			locationManager.requestLocationUpdates(
			LocationManager.GPS_PROVIDER,
			MIN_TIME_MS,
			MIN_DISTANCE_M,
			locationListener
			);
			} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		serviceActive = false;
		tripStartTime = -1;
		totalDistanceKm = 0;
		maxSpeed = 0;
		if (locationManager != null) {
			try {
				locationManager.removeUpdates(locationListener);
				} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		stopForeground(true);
	}
}