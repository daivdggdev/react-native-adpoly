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
    public void init(String type, final String appId) {
        Log.i("AD_DEMO", "init type = " + type);
        // if (type.equals("gdt")) {
        //     GDTADManager.getInstance().initWith(this.context, appId);
        // } else if (type.equals("tt")) {
        //     Handler mainHandler = new Handler(this.context.getMainLooper());
        //     Runnable myRunnable = new Runnable() {
        //         @Override
        //         public void run() {
        //             TTAdManagerHolder.init(context, appId);
        //         }
        //     };

        //     mainHandler.post(myRunnable);
        // }
    }

    @ReactMethod
    public void requestPermissionIfNecessary() {
        Log.i("AD_DEMO", "requestPermissionIfNecessary");
        //在合适的时机申请权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题
        //在开屏时候申请不太合适，因为该页面倒计时结束或者请求超时会跳转，在该页面申请权限，体验不好
        TTAdManagerHolder.get().requestPermissionIfNecessary(this.context);
    }

    @ReactMethod
    public void showSplash(String type, final String appKey, final String placementId) {
        Log.i("AD_DEMO", "type = " + type);
        if (type.equals("gdt")) {
            if (!GDTAdManagerHolder.isInitSuccess()) {
                GDTAdManagerHolder.init(context, appKey);
            }
            
            showGdtSplash(appKey, placementId);
        } else if (type.equals("tt")) {
            if (TTAdManagerHolder.isInitSuccess()) {
                showTTSplash(placementId);
            } else {
                Handler mainHandler = new Handler(this.context.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        TTAdManagerHolder.init(context, appKey);
                        showTTSplash(placementId);
                    }
                };
                mainHandler.post(myRunnable);
            }
        }
    }

    @ReactMethod
    public void loadFullScreenVideo(String type, String appKey, String placementId) {
        Log.i("AD_DEMO", "loadFullScreenVideo type = " + type);
        // if (type.equals("tt")) {
        //     showTTFullScreenVideo(placementId);
        // }
    }

    @ReactMethod
    public void showFullScreenVideo(String type, String appKey, String placementId) {
        Log.i("AD_DEMO", "showFullScreenVideo type = " + type);
        if (type.equals("tt")) {
            showTTFullScreenVideo(placementId);
        }
    }

    @ReactMethod
    public void loadRewardVideo(String type, String appKey, String placementId, String rewardName, String rewardAmount) {
        Log.i("AD_DEMO", "loadRewardVideo type = " + type);
        // if (type.equals("tt")) {
        //     showTTFullScreenVideo(placementId);
        // }
    }

    @ReactMethod
    public void showRewardVideo(String type, String appKey, String placementId, String rewardName, String rewardAmount) {
        Log.i("AD_DEMO", "showFullScreenVideo type = " + type);
        if (type.equals("tt")) {
            showTTRewardVideo(placementId, rewardName, rewardAmount);
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
        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, TTFullScreenVideoActivity.class);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showTTRewardVideo(String placementId, String rewardName, String rewardAmount) {
        Log.i("AD_DEMO", "showTTRewardVideo placementId = " + placementId);
        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, TTRewardVideoActivity.class);
        intent.putExtra("placementId", placementId);
        intent.putExtra("rewardName", rewardName);
        intent.putExtra("rewardAmount", rewardAmount);
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
