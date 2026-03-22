<h1 align="center">BLE Hound</h1>

<p align="center">
Android wireless situational awareness scanner focused on BLE tracking detection, device classification, and real-time signal visibility.
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

BLE Hound is an Android Bluetooth Low Energy scanner built for fast wireless awareness and field use.

It detects and classifies nearby devices in real time, with focus on:

- tracking devices  
- cyber gadgets  
- drones  
- and MORE!  

The interface is optimized for rapid scanning, quick identification, and live signal tracking.

---

## Core Features

- Real-time BLE scanning  
- Live device classification   
- Live header category counters:
  - TRACKERS  
  - GADGETS  
  - DRONES  
  - FEDS (Federally licensed/contracted)  
- Color-matched classification system  
- Device detail screen with live RSSI + raw advertisement data  
- Optional vibration alerts  
- Optional sound alerts  
- Background monitoring mode  
- Persistent Android notification with live counts  
- Lock-screen visible monitoring  
- Settings / About screen with links and documentation  

---

## Device Classification

BLE Hound identifies devices directly instead of generic labels.

### Trackers
- AirTag  
- Find My  
- Tile  
- Galaxy Tag  

### Gadgets
- Flipper Zero  
- Pwnagotchi  
- Card Skimmer patterns  
- ESP32 / Arduino dev boards  
- WiFi Pineapple patterns  

### Drones
- DJI  
- Parrot  
- Skydio  
- Autel  
- BLE Remote ID broadcasts  

### Feds
- Axon  
- Flock  

---

## Interface

| RSSI | MAC | MFG | CLASS |
|------|-----|-----|-------|

Each device includes a name row:
NAME: DeviceName

Color system:
- Yellow: Trackers  
- Orange: Gadgets  
- Purple: Drones  
- Blue: Feds  

---

## Controls

START: Begins scanning  
STOP: Freezes list  
VIBRATE: Toggle vibration alerts  
SOUND: Toggle sound alerts  

---

## Background Monitoring

- Persistent notification  
- Live category counts  
- Lock-screen visibility   

Example:
Trackers: 2   Gadgets: 1   Drones: 0   Feds: 0

---

## RSSI

RSSI = Received Signal Strength Indicator  

Used for:
- proximity estimation  
- locating strongest signal  
- walking heat mapping  

---

## Settings / About

- Background monitoring toggle  
- Category explanations  
- RSSI explanation  
- Creator info  
- Links  

---

## Creator

GH0ST3CH  
https://github.com/GH0ST3CH  
https://www.buymeacoffee.com/ghostechrepair  

---

## Inspiration

HaleHound  
https://github.com/JesseCHale/HaleHound-CYD  

ESP32 Marauder  
https://github.com/justcallmekoko/ESP32Marauder  

---

## Download

https://github.com/GH0ST3CH/BLE-Hound/releases  

---

## License

MIT  
https://github.com/GH0ST3CH/BLE-Hound/blob/main/LICENSE  
