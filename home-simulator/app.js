/**
 * Smart Home Physical Simulator
 * Listens to Firebase Realtime Database and renders the home side visually.
 * Syncs in real time with the Android mobile app.
 */

const FIREBASE_CONFIG = {
  apiKey: "AIzaSyBmSbT-wf6Z92lZiH6lVsC0Lrfno1tci3E",
  authDomain: "smart-home-monitor-7c214.firebaseapp.com",
  databaseURL: "https://smart-home-monitor-7c214-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "smart-home-monitor-7c214",
  storageBucket: "smart-home-monitor-7c214.firebasestorage.app",
  messagingSenderId: "367699008164",
  appId: "1:367699008164:web:home-simulator",
};

firebase.initializeApp(FIREBASE_CONFIG);
const auth = firebase.auth();
const database = firebase.database();

let previousDevices = new Map();
let activeTimers = new Map(); // Stores { timeoutId, endTime }
let floorsListener = null;
let floorsRef = null;

const loginScreen = document.getElementById("login-screen");
const simulatorScreen = document.getElementById("simulator-screen");
const loginForm = document.getElementById("login-form");
const loginError = document.getElementById("login-error");
const loginBtn = document.getElementById("login-btn");
const logoutBtn = document.getElementById("logout-btn");
const userLabel = document.getElementById("user-label");
const houseView = document.getElementById("house-view");
const summaryText = document.getElementById("summary-text");
const lastUpdate = document.getElementById("last-update");
const activityLog = document.getElementById("activity-log");

auth.onAuthStateChanged((user) => {
  if (user) {
    showSimulator(user);
    startListening(user.uid);
  } else {
    showLogin();
    stopListening();
  }
});

loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  loginError.classList.add("hidden");
  loginBtn.disabled = true;
  loginBtn.textContent = "Connecting…";

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  try {
    await auth.signInWithEmailAndPassword(email, password);
  } catch (err) {
    loginError.textContent = friendlyAuthError(err.code);
    loginError.classList.remove("hidden");
  } finally {
    loginBtn.disabled = false;
    loginBtn.textContent = "Connect to Home";
  }
});

logoutBtn.addEventListener("click", () => auth.signOut());

houseView.addEventListener("click", async (e) => {
  const switchChip = e.target.closest(".switch-chip");
  const deviceCard = e.target.closest(".device-card");

  if (!auth.currentUser || !deviceCard) return;
  const userId = auth.currentUser.uid;

  const { floorId, areaIdx, deviceIdx } = deviceCard.dataset;
  const device = previousDevices.get(deviceCard.dataset.deviceId);
  if (!device) return;

  if (switchChip) {
    const switchIdx = switchChip.dataset.switchIdx;
    if (device.switches && device.switches[switchIdx]) {
      const newState = !device.switches[switchIdx].isOn;
      database.ref(`users/${userId}/floors/${floorId}/areas/${areaIdx}/devices/${deviceIdx}/switches/${switchIdx}/isOn`).set(newState);
    }
    return;
  }

  // Toggle main device state
  if (device.type === "CAMERA") {
    database.ref(`users/${userId}/floors/${floorId}/areas/${areaIdx}/devices/${deviceIdx}/isStreaming`).set(!device.isStreaming);
  } else if (device.type === "MULTI_SWITCH") {
    // Multi-switch main card click could toggle all, but typically we want chip clicks
  } else {
    const newState = device.state === "ON" ? "OFF" : "ON";
    database.ref(`users/${userId}/floors/${floorId}/areas/${areaIdx}/devices/${deviceIdx}/state`).set(newState);
  }
});

function friendlyAuthError(code) {
  const messages = {
    "auth/invalid-email": "Invalid email address.",
    "auth/user-disabled": "This account has been disabled.",
    "auth/user-not-found": "No account found with this email.",
    "auth/wrong-password": "Incorrect password.",
    "auth/invalid-credential": "Invalid email or password.",
    "auth/too-many-requests": "Too many attempts. Try again later.",
  };
  return messages[code] || "Login failed. Check your credentials.";
}

function showLogin() {
  loginScreen.classList.remove("hidden");
  simulatorScreen.classList.add("hidden");
}

function showSimulator(user) {
  loginScreen.classList.add("hidden");
  simulatorScreen.classList.remove("hidden");
  userLabel.textContent = user.email || "Connected";
}

function startListening(userId) {
  stopListening();
  previousDevices.clear();
  activeTimers.forEach((t) => clearTimeout(t.timeoutId));
  activeTimers.clear();

  floorsRef = database.ref(`users/${userId}/floors`);
  floorsListener = (snapshot) => {
    const floors = parseFloors(snapshot);
    renderHouse(floors);
    updateSummary(floors);
    lastUpdate.textContent = `Updated ${formatTime(new Date())}`;
  };
  floorsRef.on("value", floorsListener);
}

function stopListening() {
  if (floorsRef && floorsListener) {
    floorsRef.off("value", floorsListener);
    floorsListener = null;
    floorsRef = null;
  }
  activeTimers.forEach((t) => clearTimeout(t.timeoutId));
  activeTimers.clear();
}

function parseFloors(snapshot) {
  if (!snapshot.exists()) return [];

  const floors = [];
  snapshot.forEach((child) => {
    const data = child.val();
    if (data) {
      floors.push({
        id: data.id || child.key,
        name: data.name || "Unnamed Floor",
        areas: Array.isArray(data.areas) ? data.areas : [],
      });
    }
  });
  return floors;
}

function renderHouse(floors) {
  if (floors.length === 0) {
    houseView.innerHTML = `
      <div class="empty-house">
        <span class="empty-icon">🏡</span>
        <p>No floors yet. Add floors and devices in the mobile app.</p>
      </div>`;
    return;
  }

  houseView.innerHTML = floors
    .map((floor) => {
      const areasHtml =
        floor.areas.length === 0
          ? `<p class="no-devices">No areas on this floor</p>`
          : `<div class="areas-grid">${floor.areas.map((area, areaIdx) => renderRoom(area, areaIdx, floor.id)).join("")}</div>`;

      return `
        <div class="floor-block" data-floor-id="${escapeHtml(floor.id)}">
          <div class="floor-label">
            <h4>${escapeHtml(floor.name)}</h4>
            <span class="floor-badge">${floor.areas.length} area${floor.areas.length !== 1 ? "s" : ""}</span>
          </div>
          ${areasHtml}
        </div>`;
    })
    .join("");
}

function renderRoom(area, areaIdx, floorId) {
  const devices = Array.isArray(area.devices) ? area.devices : [];
  const hasActive = devices.some((d) => isDeviceActive(d));

  const devicesHtml =
    devices.length === 0
      ? `<p class="no-devices">No devices in this room</p>`
      : `<div class="devices-list">${devices.map((device, deviceIdx) => renderDevice(device, deviceIdx, areaIdx, floorId)).join("")}</div>`;

  return `
    <div class="room ${hasActive ? "has-active-devices" : ""}" data-area-id="${escapeHtml(area.id)}">
      <div class="room-header">
        <span class="room-name">${escapeHtml(area.name)}</span>
        <span class="room-type">${escapeHtml(area.type || "Room")}</span>
      </div>
      ${devicesHtml}
    </div>`;
}

function renderDevice(device, deviceIdx, areaIdx, floorId) {
  detectChange(device, deviceIdx, areaIdx, floorId);

  const state = device.state || "OFF";
  const icon = getDeviceIcon(device);
  const kindClass = getKindClass(device);
  const isLightOn = device.type === "SCHEDULED_DEVICE" && device.deviceKind === "LIGHT" && state === "ON";
  const isStreaming = device.type === "CAMERA" && device.isStreaming;

  let meta = getDeviceTypeLabel(device);
  if (device.type === "SCHEDULED_DEVICE" && device.maxDurationMinutes > 0) {
    meta += ` · auto-off ${device.maxDurationMinutes}m`;
  }

  let switchesHtml = "";
  if (device.type === "MULTI_SWITCH" && Array.isArray(device.switches)) {
    switchesHtml = `<div class="switches-row">${device.switches
      .map(
        (s, switchIdx) =>
          `<span class="switch-chip ${s.isOn ? "on" : ""}" data-switch-idx="${switchIdx}">${escapeHtml(s.name || "Switch")}: ${s.isOn ? "ON" : "OFF"}</span>`
      )
      .join("")}</div>`;
  }

  return `
    <div class="device-card state-${state}"
         data-device-id="${escapeHtml(device.id)}"
         data-device-idx="${deviceIdx}"
         data-area-idx="${areaIdx}"
         data-floor-id="${escapeHtml(floorId)}"
         id="device-${escapeHtml(device.id)}">
      <div class="device-icon-wrap ${isLightOn ? "light-on" : ""}">
        <span class="device-icon kind-${kindClass}">${icon}</span>
        ${isStreaming ? '<span class="camera-rec-dot"></span>' : ""}
      </div>
      <div class="device-info">
        <div class="device-name">${escapeHtml(device.name)}</div>
        <div class="device-meta">${escapeHtml(meta)}</div>
        ${switchesHtml}
      </div>
      <span class="device-state-badge ${state}">${state}</span>
    </div>`;
}

function isDeviceActive(device) {
  if (device.state === "ON") return true;
  if (device.type === "MULTI_SWITCH" && Array.isArray(device.switches)) {
    return device.switches.some((s) => s.isOn);
  }
  if (device.type === "CAMERA" && device.isStreaming) return true;
  return false;
}

function getDeviceIcon(device) {
  switch (device.type) {
    case "OUTLET":
      return "🔌";
    case "MULTI_SWITCH":
      return "🔘";
    case "SCHEDULED_DEVICE":
      switch (device.deviceKind) {
        case "IRON":
          return "♨️";
        case "FAN":
          return "🌀";
        default:
          return "💡";
      }
    case "CAMERA":
      return "📷";
    default:
      return "⚡";
  }
}

function getKindClass(device) {
  if (device.type === "SCHEDULED_DEVICE") {
    return device.deviceKind || "LIGHT";
  }
  return device.type || "OUTLET";
}

function getDeviceTypeLabel(device) {
  switch (device.type) {
    case "OUTLET":
      return "Power outlet";
    case "MULTI_SWITCH":
      return "Multi switch";
    case "SCHEDULED_DEVICE":
      return (device.deviceKind || "LIGHT").toLowerCase();
    case "CAMERA":
      return device.isStreaming ? "Camera · streaming" : "Camera";
    default:
      return "Device";
  }
}

function updateSummary(floors) {
  let areaCount = 0;
  let deviceCount = 0;
  let onCount = 0;

  floors.forEach((floor) => {
    floor.areas.forEach((area) => {
      areaCount++;
      const devices = area.devices || [];
      deviceCount += devices.length;
      devices.forEach((d) => {
        if (isDeviceActive(d)) onCount++;
      });
    });
  });

  summaryText.textContent = `${floors.length} floor${floors.length !== 1 ? "s" : ""}, ${areaCount} area${areaCount !== 1 ? "s" : ""}, ${deviceCount} device${deviceCount !== 1 ? "s" : ""} · ${onCount} active`;
}

function detectChange(device, deviceIdx, areaIdx, floorId) {
  const key = device.id;
  const prev = previousDevices.get(key);

  if (prev !== undefined) {
    const prevState = getEffectiveState(prev);
    const newState = getEffectiveState(device);

    if (prevState !== newState) {
      logActivity(device, prevState, newState);
      flashDevice(device.id);
    } else if (device.type === "MULTI_SWITCH") {
      const prevSwitches = JSON.stringify(prev.switches || []);
      const newSwitches = JSON.stringify(device.switches || []);
      if (prevSwitches !== newSwitches) {
        logSwitchActivity(device);
        flashDevice(device.id);
      }
    } else if (device.type === "CAMERA" && prev.isStreaming !== device.isStreaming) {
      logActivity(
        device,
        device.isStreaming ? "OFF" : "ON",
        device.isStreaming ? "ON" : "OFF",
        "stream"
      );
      flashDevice(device.id);
    }
  }

  manageAutoOff(device, floorId, areaIdx, deviceIdx);
  previousDevices.set(key, { ...device, switches: device.switches ? [...device.switches] : [] });
}

function manageAutoOff(device, floorId, areaIdx, deviceIdx) {
  const timerKey = device.id;
  if (!timerKey) return;

  const existing = activeTimers.get(timerKey);
  const duration = parseFloat(device.maxDurationMinutes);
  const isAutoDevice = device.type === "SCHEDULED_DEVICE" && !isNaN(duration) && duration > 0;
  const isOn = device.state === "ON";

  if (isAutoDevice && isOn) {
    if (!existing) {
      const durationMs = Math.floor(duration * 60 * 1000);
      const endTime = Date.now() + durationMs;
      const userId = auth.currentUser ? auth.currentUser.uid : null;

      const timeoutId = setTimeout(() => {
        if (userId) {
          database
            .ref(`users/${userId}/floors/${floorId}/areas/${areaIdx}/devices/${deviceIdx}/state`)
            .set("OFF")
            .catch(err => console.error("Auto-off failed:", err));
          logTimerExpiry(device);
        }
        activeTimers.delete(timerKey);
      }, durationMs);

      activeTimers.set(timerKey, { timeoutId, endTime });
    }
  } else {
    if (existing) {
      clearTimeout(existing.timeoutId);
      activeTimers.delete(timerKey);
    }
  }
}

function updateTimersUI() {
  const now = Date.now();
  activeTimers.forEach((timer, deviceId) => {
    const deviceEl = document.getElementById(`device-${deviceId}`);
    if (!deviceEl) return;

    const metaEl = deviceEl.querySelector(".device-meta");
    const device = previousDevices.get(deviceId);

    if (metaEl && device && device.state === "ON") {
      const secondsLeft = Math.max(0, Math.ceil((timer.endTime - now) / 1000));
      const label = getDeviceTypeLabel(device);

      if (secondsLeft > 0) {
        metaEl.textContent = `${label} · auto-off in ${formatRemaining(secondsLeft)}`;
        if (secondsLeft <= 10) {
          metaEl.style.color = "#f44336";
          metaEl.style.fontWeight = "bold";
        } else {
          metaEl.style.color = "";
          metaEl.style.fontWeight = "";
        }
      }
    }
  });
}

function formatRemaining(seconds) {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return m > 0 ? `${m}m ${s}s` : `${s}s`;
}

setInterval(updateTimersUI, 1000);

function logTimerExpiry(device) {
  const item = document.createElement("li");
  item.className = "activity-item off-event";
  item.innerHTML = `
    <strong>${escapeHtml(device.name)}</strong> auto-off triggered
    <span class="time">${formatTime(new Date())} · ${device.maxDurationMinutes}m expired</span>`;
  prependActivity(item);
}

function getEffectiveState(device) {
  if (device.type === "MULTI_SWITCH" && Array.isArray(device.switches)) {
    return device.switches.some((s) => s.isOn) ? "ON" : "OFF";
  }
  return device.state || "OFF";
}

function logActivity(device, fromState, toState, kind = "power") {
  const isOn = toState === "ON";
  const action =
    kind === "stream"
      ? isOn
        ? "started streaming"
        : "stopped streaming"
      : isOn
        ? "turned ON"
        : "turned OFF";

  const item = document.createElement("li");
  item.className = `activity-item ${isOn ? "on-event" : "off-event"}`;
  item.innerHTML = `
    <strong>${escapeHtml(device.name)}</strong> ${action}
    <span class="time">${formatTime(new Date())}</span>`;

  prependActivity(item);
}

function logSwitchActivity(device) {
  const onSwitches = (device.switches || []).filter((s) => s.isOn).map((s) => s.name);
  const item = document.createElement("li");
  item.className = "activity-item on-event";
  item.innerHTML = `
    <strong>${escapeHtml(device.name)}</strong> switches updated
    <span class="time">${formatTime(new Date())} · ${onSwitches.length ? "ON: " + onSwitches.join(", ") : "all OFF"}</span>`;

  prependActivity(item);
}

function prependActivity(item) {
  const muted = activityLog.querySelector(".muted");
  if (muted) muted.remove();

  activityLog.insertBefore(item, activityLog.firstChild);

  while (activityLog.children.length > 30) {
    activityLog.removeChild(activityLog.lastChild);
  }
}

function flashDevice(deviceId) {
  requestAnimationFrame(() => {
    const el = document.getElementById(`device-${deviceId}`);
    if (el) {
      el.classList.add("just-changed");
      setTimeout(() => el.classList.remove("just-changed"), 600);
    }
  });
}

function formatTime(date) {
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" });
}

function escapeHtml(str) {
  if (str == null) return "";
  const div = document.createElement("div");
  div.textContent = String(str);
  return div.innerHTML;
}
