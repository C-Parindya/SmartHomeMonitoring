# Home Simulator

A web-based **physical home view** for the SmartHomeMonitoring Android app. It connects to the same Firebase Realtime Database and shows, in real time, when devices are turned on or off from the mobile app.

Use this for demos, presentations, or testing — open it on a laptop or second screen while controlling devices from your phone.

## What it shows

- **Floors and rooms** matching your mobile app layout
- **Devices** (bulbs, fans, irons, outlets, switches, cameras) with live state
- **Visual effects** — lights glow when ON, fans spin, rooms light up when active
- **Activity log** — every toggle from the mobile app appears instantly

## Quick start

### 1. One-time Firebase setup (if web login fails)

If you get an auth error when logging in, add a Web app in Firebase Console:

1. Open [Firebase Console](https://console.firebase.google.com/) → your project `smart-home-monitor-7c214`
2. Click **Add app** → **Web** (`</>`)
3. Register the app (nickname e.g. "Home Simulator")
4. Copy the `firebaseConfig` values — they should match what's already in `app.js`
5. In **Authentication → Sign-in method**, ensure **Email/Password** is enabled
6. In **Realtime Database → Rules**, ensure authenticated users can read/write their own data:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid"
      }
    }
  }
}
```

### 2. Run the simulator

**Option A — Simple (open file directly)**

Double-click `index.html` or open it in Chrome/Edge.

**Option B — Local server (recommended)**

```powershell
cd home-simulator
npx --yes serve .
```

Then open `http://localhost:3000` in your browser.

### 3. Demo flow

1. Open the simulator in a browser and log in with the **same email/password** as the Android app
2. On your phone, open the SmartHomeMonitoring app
3. Navigate to **Floors → pick a floor → pick an area**
4. Tap the **power icon** on a device (bulb, outlet, etc.)
5. Watch the simulator update — the light glows, fan spins, activity log shows the change

## Architecture

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│  Android App    │ ─write─▶│  Firebase RTDB   │◀─listen─│  Home Simulator │
│  (remote control)│         │  users/{uid}/    │         │  (physical view)│
└─────────────────┘         │  floors          │         └─────────────────┘
                            └──────────────────┘
```

The mobile app writes device state to Firebase. The simulator listens on the same path and renders the "home side" — what would happen at the physical house if real hardware were connected.

## Files

| File | Purpose |
|------|---------|
| `index.html` | Login screen + house layout UI |
| `styles.css` | Dark theme, room/device visuals, animations |
| `app.js` | Firebase auth, real-time listener, rendering |
| `README.md` | This file |

## Device visuals

| Device | When ON |
|--------|---------|
| Bulb (LIGHT) | Yellow glow effect |
| Fan | Spinning animation |
| Iron | Heat glow |
| Outlet | Green ON badge |
| Multi-switch | Individual switch chips |
| Camera | Red recording dot when streaming |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Login fails | Enable Email/Password auth; add Web app in Firebase Console |
| Empty house | Add floors/areas/devices in the mobile app first |
| No updates | Check internet; confirm same Firebase account on both |
| CORS errors | Use a local server (`npx serve .`) instead of opening file directly |
