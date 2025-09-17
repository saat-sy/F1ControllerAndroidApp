# F1ControllerAndroidApp

This Android application acts as a controller for the F1ControllerServer (Python server available at [github.com/saat-sy/F1ControllerServer](https://github.com/saat-sy/F1ControllerServer)).

## Features
- Sends UDP packets to the Python server for real-time control.
- Customizable connection settings.
- Utilizes device sensors for input.

## Getting Started

### Prerequisites
- Android device (API 21+ recommended)
- Python server running [F1ControllerServer](https://github.com/saat-sy/F1ControllerServer)

### Installation
1. Clone this repository:
   ```zsh
   git clone https://github.com/saat-sy/F1ControllerAndroidApp.git
   ```
2. Open the project in Android Studio.
3. Build and run the app on your device.

### Usage
1. Start the Python server from [F1ControllerServer](https://github.com/saat-sy/F1ControllerServer).
2. Launch the Android app.
3. Enter the server's IP address and port in the app's connection dialog.
4. Begin sending control data via UDP packets.

## Project Structure
- `app/src/main/java/com/sayatech/f1controller/` - Main source code
- `app/src/main/res/` - Resources (layouts, drawables, etc.)
- `assets/` - App assets
