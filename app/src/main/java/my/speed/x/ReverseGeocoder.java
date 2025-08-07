package my.speed.x;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ReverseGeocoder {
	
	// Callback interface for returning results
	public interface Callback {
		void onAddressFound(String address);
		void onError(String error);
	}
	
	// This static method performs reverse geocoding in a background thread.
	public static void getAddress(final Context context, final double latitude, final double longitude, final Callback callback) {
		new Thread(new Runnable() {
			public void run() {
				Geocoder geocoder = new Geocoder(context, Locale.getDefault());
				try {
					List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
					if (addresses != null && !addresses.isEmpty()) {
						Address addr = addresses.get(0);
						String addressLine = addr.getAddressLine(0);
						if (callback != null) {
							callback.onAddressFound(addressLine);
						}
						} else {
						if (callback != null) {
							callback.onError("No address found");
						}
					}
					} catch (IOException e) {
					e.printStackTrace();
					if (callback != null) {
						callback.onError(e.getMessage());
					}
				}
			}
		}).start();
	}
}