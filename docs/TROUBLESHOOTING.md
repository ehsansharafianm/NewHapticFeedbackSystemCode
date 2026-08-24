# Troubleshooting & Known Issues

A running log of problems we've hit with the gait-training haptic-feedback system
and how we resolved them. This file is **documentation only** — it lives outside
`src/` and `res/`, so it has no effect on the build or the running app. Add a new
dated entry each time we chase down a real issue, so future-us knows the cause.

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
Temporary logging (on the `claude/aim2-samsung-s26-testing` branch) recorded, per
streaming IMU, the sensor's *own* packet counter and hardware timestamp vs. the
app's received count. This let us compute packets dropped in transit:

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
- Security: **WPA2-Personal** (best compatibility with the ESP-based haptic cells;
  WPA3 or mixed mode can stop the cells from connecting).

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
