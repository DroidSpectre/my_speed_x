package my.speed.x;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventRecord implements LogEntry {
	public enum Type { START, STOP }
	
	private Type type;
	private long timestamp;
	private double latitude;
	private double longitude;
	private String address;
	
	public EventRecord(Type type,
	long timestamp,
	double latitude,
	double longitude,
	String address) {
		this.type = type;
		this.timestamp = timestamp;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
	}
	
	private String formatDateTime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
		.format(new Date(timestamp));
	}
	
	@Override
	public String toString() {
		return String.format(
		"%s @ %s\n" +
		"Location: %.5f, %.5f\n" +
		"Address: %s",
		type,
		formatDateTime(),
		latitude,
		longitude,
		address == null ? "unknown" : address
		);
	}
	
	@Override
	public String toExportString() {
		return "===== " + type + " =====\n"
		+ toString()
		+ "\n\n";
	}
}