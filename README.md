# 🆘 Depsin – Local Network Based Assistance Application in Case of Disaster

<p align="center"> 
<img src="https://img.shields.io/badge/AFET--MODU-ACTIVE-red?style=for-the-badge"> 
<img src="https://img.shields.io/badge/OFFLINE--READY-YES-black?style=for-the-badge"> 
<img src="https://img.shields.io/badge/LOCAL--NETWORK-HOTSPOT-blue?style=for-the-badge">
</p>

<p align="center">
<img src="https://media.giphy.com/media/26tn33aiTi1jkl6H6/giphy.gif" style="width:50%; height:100;">
</p>

---

## 📌 Project Summary

**Depsin** is a mobile application focused on emergency assistance that enables devices to communicate with each other via a **local network (hotspot)** during disasters, even without internet or mobile network access.

The application aims to bring together those who need help and those who can help,

in the fastest and simplest way, on the same network.

---

## 🚀 Key Features

- 📡 **Hotspot-based device communication (works offline)**

- 🆘 **Quick action with “Get Help” / “Help” buttons**

- 💬 **Messaging between devices connected to the server**

- 🗃️ **On-device (local) data storage with SQLite**

- ⚡ **Low latency – fast data transmission**

---

## 🎬 Communication & Flow Logic

<p align="center">
<img src="https://media.giphy.com/media/3o7btPCcdNniyf0ArS/giphy.gif" width="300">
</p>

<p align="center">

<i>Local network-based device communication</i>

</p>

- A device **hotspot (local (network)** creates

- Other devices connect to this network

- The server listens for requests from devices on the same network

- Help calls and messages are broadcast **instantly** within the network

Thanks to this structure:

- Internet dependency is eliminated

- Communication continues in disaster conditions

- The system works quickly and stably

---

## 🧪 Test Environment

📌 The application is currently running with a **controlled test scenario**:

- 💻 **Server**: Active on the developer's computer

- 📶 **Network**: Hotspot created via the computer

- 📱 **Mobile devices**: Connect to this network and communicate with the server

✅ This method is used to simulate scenarios where the internet is completely cut off.

🔜 In the next stage, the server will be moved to a **static IP or cloud infrastructure**.

---

## 📱 Disaster Scenario

1. A user opens a hotspot

2. Other users connect to this network

3. Help calls are sent

4. They are instantly forwarded to devices on the same network

5. They are recorded in the local database

Goal: **To reach the right person as quickly as possible**

---

## ⚙️ Technologies Used

- 📱 Android (Java / Kotlin)
- 🌐 Local network (HTTP / Socket)
- 🗃️ SQLite (local database)
- 🔐 Planned: Encrypted messaging
- 🤖 Planned: AI-powered help prioritization

---

## 🛠 Setup

```bash
git clone https://github.com/nihatbayramm/DepSinApp.git
cd DepSinApp

```
## Developer:
**Server - Backend: @nihatbayramm**

**Frontend: @bilgeberfin, @selmaduzme**
