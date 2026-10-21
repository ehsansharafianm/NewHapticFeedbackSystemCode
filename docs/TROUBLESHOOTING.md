# Troubleshooting & Known Issues

A running log of problems we've hit with the gait-training haptic-feedback system
and how we resolved them. This file is **documentation only** — it lives outside
`src/` and `res/`, so it has no effect on the build or the running app. Add a new
dated entry each time we chase down a real issue, so future-us knows the cause.

---

## Aim2 — IMU timer drift returned on Samsung; fixed by hotspot security mode (WPA2-only → WPA2/WPA3)

**Date resolved:** 2026-10-21
**Device:** Samsung Galaxy S26
**Study:** Aim2 Thigh Extension

### Symptom
The same streaming-IMU timer drift from the entry below came back on the S26 **even
with the hotspot already set to single-band 2.4 GHz.** On the table with the sensors
stationary the timers stayed locked and no packets dropped; the drift only appeared
once the sensors (still in their carrying case) were picked up / moved. Temporary
diagnostic logging confirmed it was again Bluetooth packet loss in transit (5–25%
dropped, uneven across sensors), not a sensor rate problem — every sensor produced a
clean ~60 Hz but the app received less.

### Root cause
The phone's **WiFi hotspot security was set to "WPA2-Personal" only.** On the S26,
WPA2-only degraded the 2.4 GHz link enough that the Bluetooth IMU connections lost
packets once the radio environment changed (sensors moved). This is a *different*
knob from the hotspot **band** (the fix in the entry below) — the band was already
correct this time; the **security mode** was the remaining problem.

### The fix
Set the hotspot **security to "WPA2/WPA3-Personal"** (the transitional mode), not
"WPA2-Personal" only.

- Samsung: Hotspot settings → **Security → WPA2/WPA3-Personal**.
- Keep the band on **2.4 GHz** (still required — see the entry below).
- The ESP-based haptic cells still connect fine in WPA2/WPA3 transitional mode on
  the S26. (This supersedes the older "use WPA2-only" note below for this phone — if
  the cells ever fail to join under transitional mode on a different phone, fall back
  to WPA2-Personal and re-test.)

### Quick checklist when the timers drift on Samsung
1. Hotspot **Band = 2.4 GHz** (not auto/dual).
2. Hotspot **Security = WPA2/WPA3-Personal** (not WPA2-only).
3. Keep phone and sensors close; avoid other 2.4 GHz congestion.

---

## Aim2 — Streaming IMU timers drift apart on Samsung (WiFi/Bluetooth interference)

**Date resolved:** 2026-08-25
**Devices:** Samsung Galaxy S26 (broke) vs Google Pixel 10 (worked)
**Study:** Aim2 Thigh Extension

### Symptom
During a trial, the four streaming IMU timers on the trial page
(`aim2_thigh_extension_study_trial_page_*DataOutputView` for Left/Right Thigh and
Left/Right Foot) started out aligned but drifted apart over time — after ~30 s to
2 min the spread grew large, and one sensor would reach the end of the trial while
the others were still counting. It worked fine on the Google Pixel 10 but not on
the Samsung. Earlier we also noticed the per-sensor Dot Log CSVs had **different
row counts** (two IMUs consistently had less data).

### Root cause
Bluetooth packet loss caused by **WiFi ↔ Bluetooth 2.4 GHz coexistence
interference on the phone.**

- Each IMU's on-screen timer is derived from the *count of Bluetooth data packets
  it has received* (`sampleCounter / outputFrequency`, with `outputFrequency = 60`),
  not from a real clock. So if Bluetooth drops packets for a sensor, that sensor's
  timer runs slow, and because the loss differs per sensor, the four timers drift
  apart.
- The packet loss came from running the **haptic-cell WiFi hotspot on the same
  phone** as the six Bluetooth IMU connections. WiFi and Bluetooth both use the
  2.4 GHz band and share the phone's radio ("coexistence"). The Samsung hotspot was
  in an **auto / dual-band mode**, which kept the radio busy across bands and
  starved the Bluetooth links. The Pixel's hotspot was single-band 2.4 GHz and its
  radio handled coexistence cleanly.
- The Movella DOT hardware "sync" feature does **not** fix this — sync aligns the
  sensors' internal clocks, not Bluetooth delivery, and the app times off received
  packet counts.

### How we diagnosed it
Temporary logging (since removed) recorded, per streaming IMU, the sensor's *own*
packet counter and hardware timestamp vs. the app's received count. This let us
compute packets dropped in transit:

- **Broken run (hotspot on, dual/auto band):** 11–25% of packets dropped per
  sensor; sensors were producing a clean ~60 Hz but the app only received
  ~45–53 Hz. Uneven loss → timers drifted (~22 s spread at the 2.5-min mark).
- **Fixed run (hotspot on, single 2.4 GHz band):** **0.0% dropped**, all four at
  exactly 60.00 Hz, over a 4-min baseline **and** a 6.7-min Error Feedback trial
  with haptic feedback firing the whole time. Timers stayed locked together.

### The fix
Set the **phone's WiFi hotspot to a single 2.4 GHz band** (not auto / dual /
2.4+5 GHz), matching how the Pixel was configured.

- Samsung: Hotspot settings → **Band → 2.4 GHz** (do **not** use auto/dual-band).
- Security: **WPA2/WPA3-Personal** (transitional mode). Note: this note originally
  recommended WPA2-only, but on the Samsung S26 WPA2-only degraded the Bluetooth link
  — see the 2026-10-21 entry above. The ESP-based haptic cells still connect under
  WPA2/WPA3 transitional mode; only fall back to WPA2-only if the cells can't join.

### If it comes back / other things to try
- Re-check the hotspot band first — a phone update or reset can flip it back to
  auto/dual-band.
- Move the WiFi off the phone entirely: run the haptic cells through a **separate
  2.4 GHz router** so WiFi and Bluetooth aren't on the same radio.
- Keep the phone and sensors close; avoid other 2.4 GHz congestion in the room.
- Last resort (code change, not done): time the trial off the sensor's own packet
  counter/timestamp instead of received-packet count. This keeps the timers aligned
  even with packet loss, but does **not** recover the lost samples, so fixing the
  interference is still the priority for data quality.

---

<!-- Add new issues above this line, newest first. -->
