# My Speed X 🚗📱

A simple yet comprehensive speedometer app for Android that provides GPS-based speed tracking with reverse geocoding capabilities.

## Features ⭐

- **Real-time Speed Tracking**: Accurate GPS-based speed measurement in both kilometers per hour (KPH) and miles per hour (MPH)
- **Trip Logging**: Complete trip recording system with start/stop events and detailed trip summaries
- **Distance Tracking**: Total distance calculation for each trip
- **Speed Analytics**: Track maximum speed and average speed for each journey
- **Reverse Geocoding**: Automatic address resolution for your current location
- **Background Service**: Continuous tracking with foreground notification service
- **Trip Export**: Export trip logs to text files for external analysis
- **Visual Indicators**: LED-style status indicators showing service status
- **Persistent Data**: Trip history saved locally using SharedPreferences

## Screenshots 📸

*Add screenshots of your app here*

## Technical Specifications 🔧

- **Platform**: Android (API Level 23+)
- **Language**: Java
- **GPS Provider**: Android LocationManager with GPS_PROVIDER
- **Dependencies**: 
  - Google Play Services Location (17.0.0)
  - Gson (2.9.1) for JSON serialization
  - AndroidX libraries for modern Android support

## Architecture 🏗️

The app follows a service-oriented architecture:

- **MainActivity**: Main UI controller handling user interactions and displaying real-time data
- **SpeedService**: Background foreground service for continuous GPS tracking
- **ReverseGeocoder**: Utility class for converting coordinates to addresses
- **Data Models**: EventRecord and TripRecord classes for structured trip logging
- **NotificationHelper**: Handles foreground service notifications

## Permissions Required 🔐

- `ACCESS_FINE_LOCATION`: Required for accurate GPS positioning
- `WRITE_EXTERNAL_STORAGE`: Required for exporting trip logs (Android 9 and below)

## Installation 📲

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/DroidSpectre/my_speed_x.git
   ```

2. Open the project in Android Studio

3. Build and run the project:
   - Ensure you have Android SDK 31 installed
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio

### APK Installation

*Add instructions for APK installation when available*

## Usage Guide 📋

### Starting a Trip
1. Grant location permissions when prompted
2. Tap the **Start** button to begin tracking
3. The app will show your current speed and start logging your journey

### During a Trip
- View real-time speed in both KPH and MPH
- Monitor total distance traveled
- See your current address (when data connection is available)
- Check service status via the indicator lights

### Ending a Trip
1. Tap the **Stop** button to end tracking
2. The app automatically saves a trip summary including:
   - Start and end addresses
   - Total distance
   - Maximum speed achieved
   - Average speed
   - Trip duration

### Managing Trip Data
- **Reset Log**: Clear all saved trip records
- **Export**: Save trip logs as text files to your device's Downloads folder

## Data Management 💾

### Trip Records
Each completed trip includes:
- Start time and address
- End time and address
- Distance traveled (kilometers)
- Maximum speed (KPH)
- Average speed (KPH)

### Event Records
The app also logs start events with:
- Event timestamp
- GPS coordinates
- Address information

### Export Format
Trip logs export as human-readable text files with detailed trip information and GPS coordinates.

## Privacy & Data 🔒

- All data is stored locally on your device
- No personal information is transmitted to external servers
- Location data is only used for speed calculation and address lookup
- Reverse geocoding requires internet connection but uses Android's built-in Geocoder service

## Known Limitations ⚠️

- GPS accuracy depends on device hardware and environmental conditions
- Reverse geocoding requires internet connectivity
- Speed calculations may vary based on GPS signal quality
- App is optimized for API Level 23+ (Android 6.0+)

## Contributing 🤝

Contributions are welcome! Please feel free to submit pull requests or open issues for:
- Bug fixes
- Feature enhancements
- Documentation improvements
- Code optimizations

## License 📄

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support 💬

If you encounter any issues or have questions:
- Open an issue on GitHub
- Check existing issues for similar problems
- Provide device information and Android version when reporting bugs

## Acknowledgments 🙏

- Android LocationManager API for GPS functionality
- Google Gson library for JSON handling
- Android Geocoder service for reverse geocoding

---

**Note**: This app requires GPS functionality and is intended for use in vehicles, bicycles, or other transportation methods. Always prioritize safety and follow traffic laws while using any speedometer application.
