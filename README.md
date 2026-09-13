# Pop Odyssey Ad Monitor Xposed v0.3

Android 16 / arm64-compatible **read-only diagnostic** module for LSPosed/Xposed.

## V0.3 changes
- Keeps the exact `com.alex.*` wrapper allowlist from the supplied APK analysis.
- Inventories the **actual declared method signatures** at runtime.
- Hooks the exact `java.lang.reflect.Method` object discovered by reflection via `XposedBridge.hookMethod(...)`.
- This removes the previous name/parameter re-resolution step and should make hook failures easier to diagnose.
- Adds `hooked` and `class_summary` records.
- `hook_failed` now includes the exception message when available.
- Does not invent third-party SDK method names and does not broad-hook third-party SDK namespaces.
- Remains read-only: no DNS/hosts blocking, request modification, click automation, reward fabrication, impression manipulation, or traffic tampering.

## Runtime evidence used
The supplied runtime capture showed:
- LSPosed successfully loaded `com.popodyssey.admonitor`.
- The hook attached to `com.terraform.popodyssey`.
- Runtime method inventory was produced for the `com.alex.*` wrappers.
- `AlexMaxRewardedVideoAdapter` and `AlexMaxSplashAdapter` were observed in the runtime inventory.
- Some earlier `hook_failed` records were present; V0.3 changes the hook mechanism to target the already-discovered `Method` object directly.

The capture does **not** establish that every bundled mediation adapter is active, nor does it prove a complete third-party SDK method map. Runtime/server mediation configuration remains authoritative.

## Evidence boundary
The supplied APK analysis identifies AnyThink/TopOn plus AppLovin MAX, Pangle/ByteDance, InMobi, Vungle, Mintegral/MBridge, BIGO and Fyber/DT Exchange/Inneractive. Adapter presence alone is not proof of active mediation.

## Build
Open the project in Android Studio or use the included GitHub Actions workflow.

The workflow:
- JDK 17
- Android SDK platform/build-tools 35
- Gradle 8.7
- `de.robv.android.xposed:api:82` as `compileOnly`

## Install / test
1. Build the debug APK.
2. Install it.
3. Enable **Pop Odyssey Ad Monitor** in LSPosed.
4. Scope it only to `com.terraform.popodyssey`.
5. Restart Pop Odyssey.
6. Observe Logcat for `PO-AdMonitor`.

Useful records:
- `attached`
- `mode`
- `class`
- `method`
- `hooked`
- `before`
- `after`
- `hook_failed`
- `class_summary`

For a clean baseline, do not combine the diagnostic run with hosts/DNS blocking.

## Safety
This project is intentionally limited to diagnostics and observation. It does not automate ad clicks, fabricate rewards, manipulate impressions, alter ad requests, or block network traffic.
