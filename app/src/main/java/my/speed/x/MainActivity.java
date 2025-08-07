package my.speed.x;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
// Imports for file export
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.Build;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import java.io.OutputStream;

public class MainActivity extends Activity {
	private static final int PERMISSION_REQUEST_CODE = 1;
	private static final String[] REQUIRED_PERMISSIONS = new String[]{
		Manifest.permission.ACCESS_FINE_LOCATION,
		Manifest.permission.WRITE_EXTERNAL_STORAGE
	};
	private static final String PREFS_NAME = "trip_log_prefs";
	private static final String KEY_TRIP_RECORDS = "trip_records";
	
	private TextView speedTextView;
	private TextView speedMphTextView;
	private TextView tripLogTextView;
	private TextView serviceStatusTextView;
	private TextView addressTextView;
	private View indicator1;
	private View indicator2;
	
	private Button startButton;
	private Button stopButton;
	private Button resetButton;
	private Button exportButton;
	
	// Now holds both EventRecord and TripRecord
	private ArrayList<LogEntry> logEntries;
	private boolean startLogged = false;
	
	private double totalDistanceKm;
	private long startTimeMillis;
	private double maxSpeed;
	
	public static final String ACTION_SPEED_UPDATE = "my.speed.x.ACTION_SPEED_UPDATE";
	
	private BroadcastReceiver speedUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			if (intent != null && intent.getAction().equals(ACTION_SPEED_UPDATE)) {
				double speedKph = intent.getDoubleExtra("speedKph", 0);
				totalDistanceKm = intent.getDoubleExtra("totalDistanceKm", totalDistanceKm);
				maxSpeed = intent.getDoubleExtra("maxSpeed", maxSpeed);
				updateUI(speedKph);
				
				// Fire a single "START" marker on the first fix after Start button
				if (SpeedService.serviceActive && ! startLogged) {
					startLogged = true;
					double latitude = intent.getDoubleExtra("latitude", 0);
					double longitude = intent.getDoubleExtra("longitude", 0);
					// strip the "Address: " prefix
					String addr = addressTextView.getText().toString();
					if (addr.startsWith("Address: ")) {
						addr = addr.substring("Address: ".length());
					}
					EventRecord startEvt = new EventRecord(
					EventRecord.Type.START,
					System.currentTimeMillis(),
					latitude,
					longitude,
					addr
					);
					logEntries.add(startEvt);
					updateLogView();
					saveLog();
				}
				
				// Reverse‑geocode update (as before)
				final double latitude = intent.getDoubleExtra("latitude", 0);
				final double longitude = intent.getDoubleExtra("longitude", 0);
				ReverseGeocoder.getAddress(MainActivity.this,
				latitude,
				longitude,
				new ReverseGeocoder.Callback() {
					@Override
					public void onAddressFound(final String address) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								addressTextView.setText("Address: " + address);
							}
						});
					}
					@Override
					public void onError(final String error) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								addressTextView.setText("Address: " + error);
							}
						});
					}
				});
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		speedTextView      = (TextView) findViewById(R.id.speedTextView);
		speedMphTextView   = (TextView) findViewById(R.id.speedMphTextView);
		tripLogTextView    = (TextView) findViewById(R.id.tripLogTextView);
		serviceStatusTextView = (TextView) findViewById(R.id.serviceStatusTextView);
		addressTextView    = (TextView) findViewById(R.id.addressTextView);
		indicator1         = findViewById(R.id.indicator1);
		indicator2         = findViewById(R.id.indicator2);
		
		logEntries = new ArrayList<LogEntry>();
		loadLog();   // now safe even if old JSON is present
		
		startButton = (Button) findViewById(R.id.startButton);
		stopButton  = (Button) findViewById(R.id.stopButton);
		resetButton = (Button) findViewById(R.id.resetButton);
		exportButton= (Button) findViewById(R.id.exportButton);
		
		speedMphTextView.setTextColor(Color.RED);
		
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { startTrip(); }
		});
		stopButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { stopTrip(); }
		});
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { resetLog(); }
		});
		exportButton.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { exportTripLog(); }
		});
		
		checkPermissions();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(speedUpdateReceiver, new IntentFilter(ACTION_SPEED_UPDATE));
		
		if (SpeedService.serviceActive) {
			serviceStatusTextView.setText("Service is running");
			indicator1.setBackgroundColor(Color.GREEN);
			indicator2.setBackgroundColor(Color.GREEN);
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
			totalDistanceKm = SpeedService.totalDistanceKm;
			startTimeMillis = SpeedService.tripStartTime;
			maxSpeed        = SpeedService.maxSpeed;
			} else {
			serviceStatusTextView.setText("Service is not running");
			indicator1.setBackgroundColor(Color.RED);
			indicator2.setBackgroundColor(Color.RED);
			startButton.setEnabled(true);
			stopButton.setEnabled(false);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(speedUpdateReceiver);
	}
	
	private void checkPermissions() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			boolean allGranted = true;
			for (String permission : REQUIRED_PERMISSIONS) {
				if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
					allGranted = false;
					break;
				}
			}
			if (! allGranted) {
				requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
			}
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
	String[] permissions,
	int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_CODE) {
			boolean allGranted = true;
			for (int result : grantResults) {
				if (result != PackageManager.PERMISSION_GRANTED) {
					allGranted = false;
					break;
				}
			}
			if (! allGranted) {
				Toast.makeText(this,
				"Required permissions are needed",
				Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}
	
	private void startTrip() {
		startLogged = false;
		startTimeMillis = System.currentTimeMillis();
		totalDistanceKm = 0;
		maxSpeed = 0;
		
		serviceStatusTextView.setText("Service is running");
		indicator1.setBackgroundColor(Color.GREEN);
		indicator2.setBackgroundColor(Color.GREEN);
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
		
		SpeedService.serviceActive = true;
		startService(new Intent(this, SpeedService.class));
	}
	
	private void stopTrip() {
		long stopTimeMillis = System.currentTimeMillis();
		double avgSpeed = totalDistanceKm /
		((stopTimeMillis - startTimeMillis) / 3600000.0);
		
		// Capture the stop address from the TextView
		String stopAddress = addressTextView.getText().toString().replace("Address: ", "");
		
		TripRecord trip = new TripRecord(
		new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(startTimeMillis)),
		addressTextView.getText().toString().replace("Address: ", ""),
		totalDistanceKm,
		maxSpeed,
		avgSpeed,
		startTimeMillis,
		stopTimeMillis,
		stopAddress
		);
		
		logEntries.add(trip);
		updateLogView();
		saveLog();
		stopService(new Intent(this, SpeedService.class));
		
		serviceStatusTextView.setText("Service is not running");
		indicator1.setBackgroundColor(Color.RED);
		indicator2.setBackgroundColor(Color.RED);
		stopButton.setEnabled(false);
		startButton.setEnabled(true);
		SpeedService.serviceActive = false;
	}
	
	private void updateUI(double speedKph) {
		double totalDistanceMi = totalDistanceKm * 0.621371;
		speedTextView.setText(
		String.format("Mileage: %.2f mi", totalDistanceMi)
		);
		speedMphTextView.setText(
		String.format("Speed: %.2f mph", speedKph * 0.621371)
		);
		speedMphTextView.setTextColor(Color.RED);
	}
	
	private void updateLogView() {
		StringBuilder sb = new StringBuilder();
		for (LogEntry entry : logEntries) {
			sb.append(entry.toString());
			sb.append("\n\n");
		}
		tripLogTextView.setText(sb.toString());
	}
	
	private void resetLog() {
		logEntries.clear();
		tripLogTextView.setText("");
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		prefs.edit().remove(KEY_TRIP_RECORDS).apply();
	}
	
	/***
	* Load existing JSON.  If it’s already in the new mixed‐type format,
	* we use our RuntimeTypeAdapterFactory.  If it’s old TripRecord‐only JSON,
	* we catch the parse exception, migrate those records to LogEntry, and re‐save.
	***/
	private void loadLog() {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String json = prefs.getString(KEY_TRIP_RECORDS, "");
		if (json.isEmpty()) {
			return;
		}
		
		try {
			// Try new mixed‐type format
			Gson gson = getGson();
			Type listType = new TypeToken<ArrayList<LogEntry>>(){}.getType();
			List<LogEntry> loaded = gson.fromJson(json, listType);
			if (loaded != null) {
				logEntries.clear();
				logEntries.addAll(loaded);
			}
			} catch (JsonParseException e) {
			e.printStackTrace();
			// Fallback: old TripRecord[] JSON
			try {
				ArrayList<TripRecord> oldList = new Gson()
				.fromJson(json, new TypeToken<ArrayList<TripRecord>>(){}.getType());
				if (oldList != null) {
					for (TripRecord tr : oldList) {
						logEntries.add(tr);
					}
				}
				// Now save in new format so next start is clean
				saveLog();
				} catch (Exception ex) {
				ex.printStackTrace();
				// bad data: just skip
				logEntries.clear();
			}
		}
		
		updateLogView();
	}
	
	private void saveLog() {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String json = getGson().toJson(logEntries);
		prefs.edit().putString(KEY_TRIP_RECORDS, json).apply();
	}
	
	private Gson getGson() {
		RuntimeTypeAdapterFactory<LogEntry> rta =
		RuntimeTypeAdapterFactory.of(LogEntry.class, "entryType")
		.registerSubtype(EventRecord.class, "event")
		.registerSubtype(TripRecord.class,  "trip");
		return new GsonBuilder()
		.registerTypeAdapterFactory(rta)
		.create();
	}
	
	private void exportTripLog() {
		if (logEntries.isEmpty()) {
			Toast.makeText(this,
			"No trip records to export",
			Toast.LENGTH_SHORT).show();
			return;
		}
		
		StringBuilder content = new StringBuilder();
		for (LogEntry entry : logEntries) {
			content.append(entry.toExportString());
		}
		
		String fileName = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
		.format(new Date()) + ".txt";
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ContentValues values = new ContentValues();
			values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
			values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain");
			values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
			Environment.DIRECTORY_DOWNLOADS);
			ContentResolver resolver = getContentResolver();
			Uri uri = resolver.insert(
			android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
			values
			);
			if (uri != null) {
				OutputStream outputStream = null;
				try {
					outputStream = resolver.openOutputStream(uri);
					if (outputStream != null) {
						outputStream.write(content.toString().getBytes());
						Toast.makeText(this,
						"Trip log exported to Downloads/" + fileName,
						Toast.LENGTH_LONG).show();
					}
					} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(this,
					"Error exporting trip log: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
					} finally {
					if (outputStream != null) {
						try {
							outputStream.close();
							} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			} else {
			FileOutputStream fos = null;
			try {
				File downloadsDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				if (! downloadsDir.exists()) {
					downloadsDir.mkdirs();
				}
				File file = new File(downloadsDir, fileName);
				fos = new FileOutputStream(file);
				fos.write(content.toString().getBytes());
				Toast.makeText(this,
				"Trip log exported to " + file.getAbsolutePath(),
				Toast.LENGTH_LONG).show();
				} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(this,
				"Error exporting trip log: " + e.getMessage(),
				Toast.LENGTH_LONG).show();
				} finally {
				if (fos != null) {
					try {
						fos.close();
						} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}