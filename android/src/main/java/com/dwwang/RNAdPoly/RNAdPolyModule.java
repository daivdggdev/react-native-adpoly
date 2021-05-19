package com.dwwang.RNAdPoly;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;
import java.lang.Runnable;
import android.util.Log;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.content.Intent;

import com.qq.e.comm.managers.GDTADManager;

public class RNAdPolyModule extends ReactContextBaseJavaModule {

    ReactApplicationContext context;

    public RNAdPolyModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
        AdHelper.reactContext = context;
    }

    @Override
    public String getName() {
        return "RNAdPoly";
    }

    @ReactMethod
    public void init(String type, String appId) {
        Log.i("AD_DEMO", "init type = " + type);
        if (type.equals("gdt")) {
            GDTADManager.getInstance().initWith(this.context, appId);
        } else if (type.equals("tt")) {
            Handler mainHandler = new Handler(this.context.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    TTAdManagerHolder.init(this.context, appId);
                }
            };

            mainHandler.post(myRunnable);
        }
    }

    @ReactMethod
    public void showSplash(String type, String appKey, String placementId) {
        Log.i("AD_DEMO", "type = " + type);
        if (type.equals("gdt")) {
            showGdtSplash(appKey, placementId);
        } else if (type.equals("tt")) {
            showTTSplash(placementId);
        }
    }

    @ReactMethod
    public void showFullScreenVideo(String type, String appKey, String placementId) {
        Log.i("AD_DEMO", "type = " + type);
        if (type.equals("tt")) {
            showTTFullScreenVideo(placementId);
        }
    }

    private void showGdtSplash(String appKey, String placementId) {
        Log.i("AD_DEMO", "appKey = " + appKey);
        if (appKey == null || placementId == null) {
            return;
        }

        Log.i("AD_DEMO", "showGdtSplash");
        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra("appKey", appKey);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showTTSplash(String placementId) {
        Log.i("AD_DEMO", "showTTSplash placementId = " + placementId);
        if (TextUtils.isEmpty(placementId)) {
            return;
        }

        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, TTSplashActivity.class);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showTTFullScreenVideo(String placementId) {
        Log.i("AD_DEMO", "showTTFullScreenVideo placementId = " + placementId);
        if (TextUtils.isEmpty(placementId)) {
            return;
        }

        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, TTFullScreenVideoActivity.class);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("IsAndroid", true);
        return constants;
    }
}
