<h1 align="center">BLE Hound</h1>

<p align="center">
Android Bluetooth Low Energy scanner designed to detect and highlight nearby tracking devices such as AirTag, Tile, and Galaxy SmartTag.
</p>

<p align="center">
  <a href="https://github.com/GH0ST3CH/BLE-Hound/releases">
    <img src="https://img.shields.io/badge/Download-APK-red?style=for-the-badge&logo=android">
  </a>
  <a href="https://github.com/GH0ST3CH/BLE-Hound/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge">
  </a>
</p>

---

## Overview

BLE Hound is an Android Bluetooth Low Energy scanner built to identify nearby BLE devices with a focus on tracking devices such as:

- Apple AirTag
- Apple Find My network devices
- Tile trackers
- Samsung Galaxy SmartTag

The application analyzes Bluetooth advertisement packets and highlights tracker-class devices visually during active scans.

The goal is to provide quick situational awareness of nearby Bluetooth activity.

---

## Core Features

- Real-time Bluetooth Low Energy scanning
- Detection of common tracking devices including:
  - Apple AirTag
  - Apple Find My network devices
  - Tile trackers
  - Samsung Galaxy SmartTag
- Device classification based on BLE advertisement data
- Visual tracker highlighting in the device list
- Optional vibration alerts when trackers are detected
- Optional sound alerts when trackers are detected
- Device detail screen with continuously updating RSSI
- High-contrast interface optimized for rapid scanning

---

## Tracker Identification

BLE Hound visually marks trackers in the scanner list.

Indicators include:

- Orange `CLASS` text
- Yellow outline around the device row
- Optional vibration alert
- Optional notification sound

These indicators allow trackers to be quickly identified even during dense scans.

---

## Interface Layout

The scanner screen displays a structured BLE table.

| RSSI | MAC | MFG | CLASS |
|------|-----|-----|-------|
| Signal strength | Bluetooth MAC address | Manufacturer ID | Device classification |

Each row also displays a dedicated second line for the name field:

```text
NAME: DeviceNameHere
```

Tapping any device row opens the detailed device screen with continuously updating RSSI and advertisement data.

---

## Alert Controls

The control panel at the top of the interface contains the following controls:

**START**  
Begins BLE scanning.

**STOP**  
Freezes the list so individual devices can be inspected.

**VIBRATE**  
Enables vibration alerts when a tracker is detected.

**SOUND**  
Enables notification alerts when a tracker is detected.

Alerts are rate limited to prevent repeated notifications during dense scans.

---

## Download APK

All APK releases are available from the Releases page.

```bash
https://github.com/GH0ST3CH/BLE-Hound/releases
```

---

## Requirements

Android device with Bluetooth Low Energy support.

Recommended Android version:

Android 8.0 or newer.

Required permissions:

- Bluetooth
- Bluetooth Scan
- Bluetooth Connect
- Vibrate

---

## Credits

This project draws inspiration from the following open source wireless tools.

### HaleHound

BLE scanning concepts and device classification logic.

https://github.com/JesseCHale/HaleHound-CYD

### ESP32 Marauder â JustCallMeKoko

Wireless reconnaissance techniques and tracker detection logic.

https://github.com/justcallmekoko/ESP32Marauder

---

## License

This project is licensed under the MIT License.

https://github.com/GH0ST3CH/BLE-Hound/blob/main/LICENSE

---

<p align="center">
  <a href="https://www.buymeacoffee.com/ghostechrepair">
    <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee">
  </a>
</p>
