# Pop Odyssey Ad Monitor Xposed v0.2

Android 16 / arm64-compatible **read-only diagnostic** starter for LSPosed/Xposed.

## V0.2 changes
- Records the **actual declared method signatures** of the exact `com.alex.*` wrapper classes recovered from the supplied APK analysis.
- Hooks only real wrapper methods whose names match conservative event words (`init`, `load`, `show`, `reward`, `bid`, etc.).
- Does not invent SDK method names and does not broad-hook third-party SDK namespaces.
- No DNS/hosts blocking, request modification, click automation, reward fabrication, or impression manipulation.

## Evidence boundary
The supplied analysis identifies AnyThink/TopOn plus AppLovin MAX, Pangle/ByteDance, InMobi, Vungle, Mintegral/MBridge, BIGO and Fyber/DT Exchange/Inneractive. It also explicitly says bundled adapter presence does not prove active mediation; runtime/server configuration is authoritative.

The analysis provides exact `com.alex.*` wrapper class names, but not a verified complete method-name table for every third-party SDK. Therefore V0.2 first inventories actual loaded wrapper methods instead of guessing them.

## Build
Open in Android Studio and build the `app` module. The project keeps `de.robv.android.xposed:api:82` as `compileOnly`.

## Test
1. Install the built module APK.
2. Enable it in LSPosed.
3. Scope it only to `com.terraform.popodyssey`.
4. Restart the game.
5. Inspect Logcat for `PO-AdMonitor`.

Useful records: `class`, `method`, `before`, `after`, `hook_failed`.
