package com.popodyssey.admonitor;

import android.util.Log;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "PO-AdMonitor";
    private static final String TARGET = "com.terraform.popodyssey";

    /*
     * Exact wrapper classes recovered from the supplied APK analysis.
     * Third-party SDK namespaces are intentionally not broad-hooked.
     */
    private static final String[] WRAPPER_CLASSES = {
        "com.alex.AlexMaxInitManager",
        "com.alex.AlexMaxBannerAdapter",
        "com.alex.AlexMaxInterstitialAdapter",
        "com.alex.AlexMaxRewardedVideoAdapter",
        "com.alex.AlexMaxNativeAdapter",
        "com.alex.AlexMaxManualNativeAd",
        "com.alex.AlexMaxNativeAd",
        "com.alex.AlexMaxNativeAdView",
        "com.alex.AlexMaxRewardAd",
        "com.alex.AlexMaxSplashAdapter",
        "com.alex.AlexMaxBiddingInfo",
        "com.alex.AlexMaxConst"
    };

    /*
     * Conservative event-name matching. This is applied only to the
     * exact wrapper methods discovered at runtime; no method names are guessed.
     */
    private static final Set<String> METHOD_WORDS = new HashSet<>(Arrays.asList(
        "init", "initialize", "load", "preload", "show", "display",
        "close", "dismiss", "destroy", "reward", "onreward", "onad",
        "adload", "adshow", "callback", "bid", "waterfall"
    ));

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET.equals(lpparam.packageName)) return;

        XposedBridge.log(TAG + ": attached to " + TARGET);
        log("mode", "inventory+safe-wrapper-hooks-v0.3");

        int available = 0;
        for (String name : WRAPPER_CLASSES) {
            if (inspectAndHook(lpparam.classLoader, name)) available++;
        }
        log("summary", "wrapper_classes_available=" + available
                + "/" + WRAPPER_CLASSES.length);
    }

    private static boolean inspectAndHook(ClassLoader cl, String className) {
        try {
            Class<?> c = Class.forName(className, false, cl);
            Method[] methods = c.getDeclaredMethods();

            log("class", className + "|declared_methods=" + methods.length);

            int hookCandidates = 0;
            int hooked = 0;

            // Inventory first: report exactly what exists in the loaded class.
            for (Method m : methods) {
                if (m.isSynthetic() || m.isBridge()) continue;
                log("method", signature(m));
            }

            // Hook the exact Method object discovered above.
            // This avoids re-resolving overloaded methods by name/parameter varargs.
            for (Method m : methods) {
                if (m.isSynthetic() || m.isBridge()) continue;

                String lowerName = m.getName().toLowerCase(Locale.ROOT);
                if (!matchesEventName(lowerName)) continue;

                hookCandidates++;
                try {
                    XposedBridge.hookMethod(m, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam p) {
                            log("before", signature(p.method)
                                    + "|args=" + summarizeArgs(p.args));
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam p) {
                            log("after", signature(p.method)
                                    + "|result=" + safe(p.getResult()));
                        }
                    });
                    hooked++;
                    log("hooked", signature(m));
                } catch (Throwable t) {
                    log("hook_failed", signature(m)
                            + "|" + t.getClass().getSimpleName()
                            + "|" + safe(t.getMessage()));
                }
            }

            log("class_summary", className
                    + "|hook_candidates=" + hookCandidates
                    + "|hooked=" + hooked);

            return true;
        } catch (Throwable t) {
            log("class_unavailable", className
                    + "|" + t.getClass().getSimpleName()
                    + "|" + safe(t.getMessage()));
            return false;
        }
    }

    private static boolean matchesEventName(String n) {
        for (String word : METHOD_WORDS) {
            if (n.contains(word)) return true;
        }
        return false;
    }

    private static String signature(java.lang.reflect.Member member) {
        StringBuilder b = new StringBuilder();
        b.append(member.getDeclaringClass().getName())
         .append(".").append(member.getName()).append("(");

        if (member instanceof Method) {
            Method m = (Method) member;
            Class<?>[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) b.append(",");
                b.append(params[i].getTypeName());
            }
            return b.append("):")
                    .append(m.getReturnType().getTypeName())
                    .toString();
        }

        return b.append("):<member>").toString();
    }

    private static String summarizeArgs(Object[] args) {
        if (args == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) b.append(", ");
            String s = safe(args[i]);
            if (s.length() > 300) s = s.substring(0, 300) + "...";
            b.append(i).append("=").append(s);
        }
        return b.toString();
    }

    private static String safe(Object o) {
        if (o == null) return "null";
        try {
            String s = String.valueOf(o);
            return s.length() > 500 ? s.substring(0, 500) + "..." : s;
        } catch (Throwable t) {
            return "<toString-error>";
        }
    }

    private static void log(String event, String detail) {
        String line = TAG + "|" + event + "|" + TARGET + "|" + detail;
        Log.i(TAG, line);
        XposedBridge.log(line);
    }
}
