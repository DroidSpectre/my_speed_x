package my.speed.x;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripRecord implements LogEntry {
	private String startDate;
	private String stopDate;
	private String startTime;
	private String stopTime;
	private String duration;
	private double totalDistanceKm;
	private double totalDistanceMi;
	private double maxSpeedKph;
	private double maxSpeedMph;
	private double avgSpeedKph;
	private double avgSpeedMph;
	private String startAddress; // New field for start address
	private String stopAddress;  // New field for stop address
	
	public TripRecord(String startTime, String startAddress, double totalDistanceKm,
	double maxSpeedKph, Double avgSpeedKph, long startTimeMillis,
	long stopTimeMillis, String stopAddress) {
		this.startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
		.format(new Date(startTimeMillis));
		this.startTime = startTime;
		this.startAddress = startAddress; // Store start address
		
		this.stopDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
		.format(new Date(stopTimeMillis));
		this.stopTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
		.format(new Date(stopTimeMillis));
		this.stopAddress = stopAddress; // Store stop address
		
		this.totalDistanceKm = totalDistanceKm;
		this.totalDistanceMi = totalDistanceKm * 0.621371;
		this.maxSpeedKph = maxSpeedKph;
		this.maxSpeedMph = maxSpeedKph * 0.621371;
		this.avgSpeedKph = avgSpeedKph;
		this.avgSpeedMph = avgSpeedKph * 0.621371;
		
		long durationMillis = stopTimeMillis - startTimeMillis;
		long seconds = (durationMillis / 1000) % 60;
		long minutes = (durationMillis / (1000 * 60)) % 60;
		long hours = durationMillis / (1000 * 60 * 60);
		this.duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	@Override
	public String toString() {
		// Update to include addresses
		return String.format(
		"Start: (%s) at %s\n" +
		"Stop:  (%s) at %s\n" +
		"Duration:   %s\n" +
		"Mileage:    %.2f mi\n" +
		"Max Speed:  %.2f mph\n" +
		"Avg Speed:  %.2f mph",
		startDate, startAddress, stopDate, stopAddress,
		duration, totalDistanceMi, maxSpeedMph, avgSpeedMph
		);
	}
	
	@Override
	public String toExportString() {
		return "=========================\n"
		+ toString()
		+ "\n=========================\n\n";
	}
}